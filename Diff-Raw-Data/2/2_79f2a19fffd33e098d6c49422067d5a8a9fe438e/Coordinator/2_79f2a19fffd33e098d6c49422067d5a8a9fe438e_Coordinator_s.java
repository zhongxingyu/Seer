 package org.h2o.eval;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.rmi.AccessException;
 import java.rmi.NotBoundException;
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.h2.util.NetUtils;
 import org.h2o.H2OLocator;
 import org.h2o.eval.coordinator.KillMonitorThread;
 import org.h2o.eval.interfaces.ICoordinatorLocal;
 import org.h2o.eval.interfaces.ICoordinatorRemote;
 import org.h2o.eval.interfaces.IWorker;
 import org.h2o.eval.printing.CSVPrinter;
 import org.h2o.eval.script.coord.CoordinationScriptExecutor;
 import org.h2o.eval.script.coord.instructions.Instruction;
 import org.h2o.eval.script.coord.instructions.MachineInstruction;
 import org.h2o.eval.script.coord.instructions.WorkloadInstruction;
 import org.h2o.eval.script.workload.Workload;
 import org.h2o.eval.script.workload.WorkloadResult;
 import org.h2o.util.H2OPropertiesWrapper;
 import org.h2o.util.exceptions.ShutdownException;
 import org.h2o.util.exceptions.StartupException;
 import org.h2o.util.exceptions.WorkloadParseException;
 
 import uk.ac.standrews.cs.nds.madface.HostDescriptor;
 import uk.ac.standrews.cs.nds.madface.JavaProcessDescriptor;
 import uk.ac.standrews.cs.nds.util.CommandLineArgs;
 import uk.ac.standrews.cs.nds.util.Diagnostic;
 import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
 import uk.ac.standrews.cs.nds.util.ErrorHandling;
 import uk.ac.standrews.cs.nds.util.FileUtil;
 import uk.ac.standrews.cs.nds.util.NetworkUtil;
 import uk.ac.standrews.cs.nds.util.PrettyPrinter;
 
 public class Coordinator implements ICoordinatorRemote, ICoordinatorLocal {
 
     /*
      * Registry fields.
      */
     private static final String REGISTRY_BIND_NAME = "h2oEvaluationCoordinator";
     private Registry registry;
 
     private final String databaseName;
 
     /*
      * Worker Fields
      */
     private final Set<InetAddress> workerLocations;
     private final List<IWorker> inactiveWorkers = new LinkedList<IWorker>();
     private final Set<IWorker> activeWorkers = new HashSet<IWorker>();
 
     /**
      * Mapping from an integer ID to worker node for H2O instances that have been started through
      * a co-ordination script.
      */
     private final Map<Integer, IWorker> scriptedInstances = new HashMap<Integer, IWorker>();
 
     private final Map<IWorker, Integer> workersWithActiveWorkloads = Collections.synchronizedMap(new HashMap<IWorker, Integer>());
 
     /*
      * Locator server fields.
      */
     private boolean locatorServerStarted = false;
     private H2OPropertiesWrapper descriptorFile;
     private KillMonitorThread killMonitor;
 
     private static final DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
     private static final String DEFAULT_DATABASE_NAME = "MY_EVALUATION_DATABASE";
 
     private final Date startDate = new Date();
     private final List<WorkloadResult> workloadResults = new LinkedList<WorkloadResult>();
 
     /**
      * 
      * @param databaseName Name of the evaluation database system this coordinator will create.
      * @param workerLocations IP addresses or hostnames of machines which are running worker nodes.
      */
     public Coordinator(final String databaseName, final InetAddress... workerLocations) {
 
         this(databaseName, Arrays.asList(workerLocations));
     }
 
     public Coordinator(final String databaseName, final String... workerLocationsStr) {
 
         this(databaseName, convertFromStringToInetAddress(workerLocationsStr));
     }
 
     public Coordinator(final String databaseName, final List<InetAddress> workerLocations) {
 
         this.databaseName = databaseName;
         this.workerLocations = new HashSet<InetAddress>();
         this.workerLocations.addAll(workerLocations);
         bindToRegistry();
     }
 
     public void bindToRegistry() {
 
         try {
             registry = LocateRegistry.getRegistry();
 
             final Remote stub = UnicastRemoteObject.exportObject(this, 0);
             registry.bind(REGISTRY_BIND_NAME, stub);
         }
         catch (final Exception e) {
             e.printStackTrace();
         }
     }
 
     /*
      * ICoordinatorLocal methods... 
      */
     @Override
     public int startH2OInstances(final int numberToStart) throws StartupException {
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "About to start H2O instance on " + numberToStart + " workers.");
 
         if (!locatorServerStarted) { throw new StartupException("The locator server has not yet been started."); }
 
         if (numberToStart <= 0) { return 0; }
 
         scanForWorkerNodes(workerLocations);
 
         if (inactiveWorkers.size() == 0) { return 0; }
 
         int numberStarted = 0;
 
         for (final IWorker worker : inactiveWorkers) {
 
             if (numberStarted == numberToStart) {
                 break;
             }
 
             try {
                 Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Starting H2O instance on worker at " + worker.getHostname());
             }
             catch (final RemoteException e2) {
                 //Doesn't matter because it's just a diagnostic. Handle the exception on a proper call to the worker.
             }
 
             try {
                 worker.startH2OInstance(descriptorFile);
 
                 activeWorkers.add(worker);
 
                 numberStarted++;
             }
             catch (final Exception e) {
                 try {
                     ErrorHandling.exceptionError(e, "Failed to start instance on worker at " + worker.getHostname());
                 }
                 catch (final RemoteException e1) {
                     ErrorHandling.exceptionError(e, "Failed to start instance on worker " + worker);
                 }
             }
         }
 
         inactiveWorkers.removeAll(activeWorkers);
 
         return numberStarted;
     }
 
     /**
      * Start a single instance on a worker at the specified hostname.
      * @param hostname Where the H2O instance should be started.
      * @param createConnectionPropertiesFile Whether to create a benchmarkSQL properties file specifying how to connect to this instance.
      * @return the JDBC connections string for the recently started database, or null if it wasn't successfully started.
      * @throws StartupException
      */
     public String startH2OInstance(final InetAddress hostname) throws StartupException {
 
         if (!locatorServerStarted) { throw new StartupException("The locator server has not yet been started."); }
 
         scanForWorkerNodes(workerLocations);
 
         for (final IWorker worker : getAllWorkers()) {
             try {
                 if (worker.getHostname().equals(hostname)) {
 
                     final String jdbcConnectionString = worker.startH2OInstance(descriptorFile);
 
                     swapWorkerToActiveSet(worker);
 
                     return jdbcConnectionString;
 
                 }
 
             }
             catch (final Exception e) {
                 ErrorHandling.exceptionError(e, "Failed to start instance " + worker);
             }
         }
         return null;
     }
 
     /**
      * Add a worker to the set of active workers, and removed it from the set of inactive workers.
      * @param worker
      */
     public void swapWorkerToActiveSet(final IWorker worker) {
 
         activeWorkers.add(worker);
         final boolean removed = inactiveWorkers.remove(worker);
 
         if (!removed) {
             ErrorHandling.errorNoEvent("Worker was not correctly removed from the list of active workers.");
         }
     }
 
     /**
      * Connect to all known workers and terminate any active H2O instances. Also remove the state of those instances.
      */
     private void obliterateExtantInstances() {
 
         scanForWorkerNodes(workerLocations);
 
         for (final IWorker worker : getAllWorkers()) {
             try {
                 worker.terminateH2OInstance();
                 worker.deleteH2OInstanceState();
             }
             catch (final Exception e) {
                 ErrorHandling.exceptionError(e, "Failed to terminate instance " + worker);
             }
         }
 
     }
 
     public Set<IWorker> getAllWorkers() {
 
         final Set<IWorker> allWorkers = new HashSet<IWorker>();
         allWorkers.addAll(activeWorkers);
         allWorkers.addAll(inactiveWorkers);
         return allWorkers;
     }
 
     private IWorker startH2OInstance() throws StartupException, RemoteException {
 
         if (inactiveWorkers.size() == 0) {
             scanForWorkerNodes(workerLocations);
 
             if (inactiveWorkers.size() == 0) { throw new StartupException("Could not instantiated another H2O instance."); }
         }
 
         final IWorker worker = inactiveWorkers.get(0);
 
         worker.startH2OInstance(descriptorFile);
 
         swapWorkerToActiveSet(worker);
 
         return worker;
     }
 
     /**
      * Go through the set of worker hosts and look for active worker instances in the registry on each host. In testing in particular there may
      * be multiple workers on each host.
      * @param workerLocationsLocal IP addresses or hostnames of machines which are running worker nodes. You can provide the {@link #workerLocations} field
      * as a parameter, or something else.
      */
     private void scanForWorkerNodes(final Set<InetAddress> workerLocationsLocal) {
 
         for (final InetAddress location : workerLocationsLocal) {
 
             try {
                 final Registry remoteRegistry = LocateRegistry.getRegistry(location.getHostName());
 
                 findActiveWorkersAtThisLocation(remoteRegistry);
             }
             catch (final Exception e) {
                 Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Failed to connect to a registry at '" + location + "'.");
             }
 
         }
     }
 
     /**
      * Find workers that exist at the registry located at the specified location.
 
      * @param remoteRegistry
      * @throws RemoteException
      * @throws AccessException
      */
     private void findActiveWorkersAtThisLocation(final Registry remoteRegistry) throws RemoteException, AccessException {
 
         final String[] remoteEntries = remoteRegistry.list();
 
         for (final String entry : remoteEntries) {
             if (entry.startsWith(Worker.REGISTRY_PREFIX)) {
                 try {
                     final IWorker workerNode = (IWorker) remoteRegistry.lookup(entry);
 
                     if (workerNode != null && !activeWorkers.contains(workerNode) && !inactiveWorkers.contains(workerNode)) {
                         try {
 
                             workerNode.initiateConnection(NetUtils.getLocalAddress(), REGISTRY_BIND_NAME);
 
                             inactiveWorkers.add(workerNode);
                         }
                         catch (final RemoteException e) {
                             ErrorHandling.exceptionError(e, "Failed to connect to worker at " + workerNode + ".");
                         }
                     }
                 }
                 catch (final NotBoundException e) {
                     ErrorHandling.exceptionError(e, "Expected RMI registry entry '" + entry + "' was not bound.");
                 }
             }
         }
     }
 
     /*
      * ICoordinatorRemote calls... 
      */
 
     @Override
     public synchronized void collateMonitoringResults(final WorkloadResult workloadResult) throws RemoteException {
 
         removeFromSetOfActiveWorkloads(workloadResult.getWorkloadID());
         System.out.println(workloadResult);
 
         workloadResults.add(workloadResult);
 
         if (workloadResult.getException() != null) {
             workloadResult.getException().printStackTrace();
         }
     }
 
     private synchronized void removeFromSetOfActiveWorkloads(final IWorker worker) {
 
         Integer count = workersWithActiveWorkloads.get(worker);
         if (count == 1) {
             workersWithActiveWorkloads.remove(worker);
         }
         else {
             workersWithActiveWorkloads.put(worker, --count);
         }
 
     }
 
     @Override
     public void startLocatorServer(final int locatorPort) throws IOException, StartupException {
 
         if (locatorServerStarted) { throw new StartupException("Locator server has already been started. It cannot be started twice."); }
 
         final List<String> args = getLocatorArgs(locatorPort);
 
         try {
             new HostDescriptor().getProcessManager().runProcess(new JavaProcessDescriptor().classToBeInvoked(H2OLocator.class).args(args));
 
         }
         catch (final Exception e) {
             e.printStackTrace();
             throw new StartupException("Failed to create new H2O locator process: " + e.getMessage());
         }
 
         try {
             Thread.sleep(3000); //wait for locator to start up.
         }
         catch (final InterruptedException e) {
         }
 
         final String descriptorFileLocation = Worker.PATH_TO_H2O_DATABASE + File.separator + databaseName + ".h2od";
 
         descriptorFile = H2OPropertiesWrapper.getWrapper(descriptorFileLocation);
         descriptorFile.loadProperties();
         locatorServerStarted = true;
 
     }
 
     public List<String> getLocatorArgs(final int locatorPort) {
 
         final List<String> args = new LinkedList<String>();
 
         args.add("-n" + databaseName);
         args.add("-p" + locatorPort);
         args.add("-dtrue");
         args.add("-f" + Worker.PATH_TO_H2O_DATABASE);
         args.add("-D6");
         return args;
     }
 
     @Override
     public void executeWorkload(final String workloadFileLocation) throws StartupException {
 
         final IWorker worker = getActiveWorker();
 
         executeWorkload(worker, workloadFileLocation, 0);
 
     }
 
     private void executeWorkload(final String id, final String workloadFileLocation, final long duration) throws StartupException {
 
         final IWorker worker = scriptedInstances.get(Integer.valueOf(id));
 
         executeWorkload(worker, workloadFileLocation, duration);
 
     }
 
     private void executeWorkload(final IWorker worker, final String workloadFileLocation, final long duration) throws StartupException {
 
         Workload workload;
         try {
             workload = new Workload(workloadFileLocation, duration);
         }
         catch (final FileNotFoundException e) {
             ErrorHandling.exceptionError(e, "Couldn't find the workload file specified: " + workloadFileLocation);
             throw new StartupException("Couldn't find the workload file specified: " + workloadFileLocation);
         }
         catch (final IOException e) {
             ErrorHandling.exceptionError(e, "Couldn't read from the workload file specified: " + workloadFileLocation);
             throw new StartupException("Couldn't read from the workload file specified: " + workloadFileLocation);
         }
 
         try {
             worker.startWorkload(workload);
         }
         catch (final RemoteException e) {
             ErrorHandling.exceptionError(e, "Couldn't connect to remote worker instance.");
             throw new StartupException("Couldn't connect to remote worker instance.");
         }
         catch (final WorkloadParseException e) {
             ErrorHandling.exceptionError(e, "Error parsing workload in " + workloadFileLocation);
         }
         catch (final SQLException e) {
             ErrorHandling.exceptionError(e, "Error creating SQL statement for workload execution. Workload was never started.");
         }
 
         addWorkloadToRecords(worker);
     }
 
     private synchronized void addWorkloadToRecords(final IWorker worker) {
 
         if (workersWithActiveWorkloads.containsKey(worker)) {
             Integer currentCount = workersWithActiveWorkloads.get(worker);
             workersWithActiveWorkloads.put(worker, ++currentCount);
         }
         else {
             workersWithActiveWorkloads.put(worker, 1);
         }
     }
 
     /**
      * Get an active worker from the set of active workers.
      * @return null if there are no active workers.
      */
     private IWorker getActiveWorker() {
 
         for (final IWorker worker : activeWorkers) {
             return worker;
         }
 
         return null;
     }
 
     @Override
     public void blockUntilWorkloadsComplete() throws RemoteException {
 
         while (areThereActiveWorkloads()) {
 
             try {
                 Thread.sleep(1000);
             }
             catch (final InterruptedException e) {
             }
 
         };
 
         if (killMonitor != null) {
             killMonitor.setRunning(false);
         }
 
         try {
             CSVPrinter.printResults("generatedWorkloads" + File.separator + dateFormatter.format(startDate) + "-results.csv", workloadResults);
         }
         catch (final FileNotFoundException e) {
             ErrorHandling.exceptionError(e, "Failed to create file to save results to.");
         }
     }
 
     private synchronized boolean areThereActiveWorkloads() {
 
         return workersWithActiveWorkloads.size() > 0;
     }
 
     @Override
     public void executeCoordinatorScript(final String configFileLocation) throws RemoteException, FileNotFoundException, WorkloadParseException, StartupException, SQLException, IOException {
 
         final List<String> script = FileUtil.readAllLines(configFileLocation);
 
         killMonitor = new KillMonitorThread(this);
 
         if (killMonitor.isRunning()) {
             killMonitor.setRunning(false);
             killMonitor = new KillMonitorThread(this);
         }
 
         killMonitor.start();
 
         for (final String action : script) {
             if (action.startsWith("{start_machine")) {
 
                 final MachineInstruction startInstruction = CoordinationScriptExecutor.parseStartMachine(action);
 
                 final IWorker worker = startH2OInstance();
 
                 scriptedInstances.put(startInstruction.id, worker);
                 Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "CSCRIPT: Starting machine with ID '" + startInstruction.id + "'");
 
                 if (startInstruction.fail_after != null) {
                     killMonitor.addKillOrder(startInstruction.id, System.currentTimeMillis() + startInstruction.fail_after);
                 }
             }
             else if (action.startsWith("{terminate_machine")) {
 
                 System.err.println("TERMINATING MACHINE.");
 
                 final MachineInstruction terminateInstruction = CoordinationScriptExecutor.parseTerminateMachine(action);
 
                 try {
                     killInstance(terminateInstruction.id);
                 }
                 catch (final ShutdownException e) {
                     ErrorHandling.exceptionError(e, "Failed to shutdown instance with ID " + terminateInstruction.id + ".");
                 }
                 Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "CSCRIPT: Terminated machine with ID '" + terminateInstruction.id + "'");
 
             }
             else if (action.startsWith("{sleep=")) {
                 final int sleepTime = CoordinationScriptExecutor.parseSleepOperation(action);
 
                 try {
                     Thread.sleep(sleepTime);
                 }
                 catch (final InterruptedException e) {
                 }
 
                 Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "CSCRIPT: Sleeping for '" + sleepTime + "'");
 
             }
             else {
                 final Instruction instruction = CoordinationScriptExecutor.parseQuery(action);
 
                 if (instruction.isWorkload()) {
                     final WorkloadInstruction wi = (WorkloadInstruction) instruction;
                     executeWorkload(wi.id, wi.workloadFile, wi.duration);
 
                     Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "CSCRIPT: Executing workload '" + wi.workloadFile + "' for '" + wi.duration + "', on '" + wi.id + "'.");
 
                 }
                 else {
                     executeQuery(instruction.id, instruction.getData());
 
                     Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "CSCRIPT: Executing query '" + instruction.getData() + "' on '" + instruction.id + "'.");
 
                 }
             }
         }
     }
 
     public void executeCoordinationScript(final List<String> script) throws WorkloadParseException, RemoteException, StartupException, SQLException {
 
     }
 
     private void executeQuery(final String workerID, final String query) throws RemoteException, SQLException {
 
         final IWorker worker = scriptedInstances.get(Integer.valueOf(workerID));
 
         worker.executeQuery(query);
     }
 
     public void killInstance(final Integer workerID) throws RemoteException, ShutdownException {
 
         final IWorker worker = scriptedInstances.get(Integer.valueOf(workerID));
 
         worker.terminateH2OInstance();
     }
 
     /**
      * 
      * @param args
      *            <ul>
      *            <li><em>-n<name></em>. The name of the database system (i.e. the name of the database in the descriptor file, the global system).</li>
      *            <li><em>-w<name></em>. A list of all the locations where worker nodes may be running, delimited by a semi-colon (e.g. "hostname1;hostname2;hostname3"). It is assumed that
      *            an instance will be started on the local instance, so this hostname does not need to be included.</li>
      *            <li><em>-c<name></em>. The number of H2O instances that are to be started. This number must be less than or equal to the number of active worker instances.</li>
      *            <li><em>-t<name></em>. Optional. Whether to terminate any existing instances running at all workers (including stored state), before doing anything else.</li>
      *            <li><em>-p<name></em>. Optional. The path/name of the properties file to create stating how to connect to the system table.</li>
      *            <li><em>-r<name></em>. Optional. The system-wide replication factor for user tables..</li>
      *            </ul>
      * @throws StartupException Thrown if a required parameter was not specified.
      * @throws IOException 
      */
     public static void main(final String[] args) throws StartupException, IOException {
 
         Diagnostic.setLevel(DiagnosticLevel.FINAL);
 
         final Map<String, String> arguments = CommandLineArgs.parseCommandLineArgs(args);
 
         final String databaseName = processDatabaseName(arguments.get("-n"));
         final String[] workerLocationsStr = processWorkerLocations(arguments.get("-w"));
 
         final List<InetAddress> workerLocationsInet = convertFromStringToInetAddress(workerLocationsStr);
         workerLocationsInet.add(NetworkUtil.getLocalIPv4Address());
 
         final int h2oInstancesToStart = processNumberOfInstances(arguments.get("-c"));
 
         final boolean obliterateExistingInstances = processTerminatesExistingInstances(arguments.get("-t"));
         final String connectionPropertiesFile = arguments.get("-p");
 
         final Coordinator coord = new Coordinator(databaseName, workerLocationsInet);
 
         if (obliterateExistingInstances) {
             coord.obliterateExtantInstances();
         }
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Starting locator server.");
 
         coord.startLocatorServer(34000);
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Setting system-wide replication factor to " + h2oInstancesToStart);
         coord.setReplicationFactor(h2oInstancesToStart);
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Starting primary H2O instance on " + NetworkUtil.getLocalIPv4Address().getHostName());
 
         InetAddress host = null;
         try {
             host = NetworkUtil.getLocalIPv4Address();
         }
         catch (final UnknownHostException e1) {
             throw new StartupException("Couldn't create local InetAddress.");
         }
 
        Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Starting secondary H2O instances on " + h2oInstancesToStart + " of the following nodes: " + PrettyPrinter.toString(workerLocationsStr));
 
         final String connectionString = coord.startH2OInstance(host); //start an instance locally as the system table.
 
         if (connectionString == null) { throw new StartupException("Failed to start the local H2O instance that is intended to become the System Table."); }
 
         final int started = coord.startH2OInstances(h2oInstancesToStart - 1);
 
         if (started != h2oInstancesToStart - 1) { throw new StartupException("Failed to start the correct number of instances. Started " + started + 1 + ", but needed to start " + h2oInstancesToStart + "."); }
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Started " + h2oInstancesToStart + " H2O instances.");
 
         if (connectionPropertiesFile != null) {
             writeConnectionStringToPropertiesFile(connectionString, connectionPropertiesFile);
         }
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "BenchmarkSQL connection information successfully written to '" + connectionPropertiesFile + "'.");
         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Stopping co-ordinator.");
 
         System.exit(0);
     }
 
     /**
      * Sets the desired replication factor in the database descriptor file. This will only be used if it is set before a database is started up.
      * @param replicationFactor How many copies of each table the system should aim to create.
      * @throws StartupException 
      */
     private void setReplicationFactor(final int replicationFactor) throws StartupException {
 
         if (descriptorFile == null) { throw new StartupException("Descriptor file has not been create yet. Call startLocatorServer() first."); }
 
         descriptorFile.setProperty("RELATION_REPLICATION_FACTOR", replicationFactor + "");
     }
 
     private static List<InetAddress> convertFromStringToInetAddress(final String[] hostnames) {
 
         final List<InetAddress> inetAddresses = new LinkedList<InetAddress>();
 
         for (final String hostname : hostnames) {
             if (hostname != null && !hostname.equals("")) {
                 try {
                     inetAddresses.add(InetAddress.getByName(hostname));
                 }
                 catch (final UnknownHostException e) {
                     ErrorHandling.errorNoEvent("Failed to convert from hostname '" + hostname + "' to InetAddress: " + e.getMessage());
                 }
             }
         }
 
         return inetAddresses;
     }
 
     /**
      * Writes the connection string to a properties file formatted for benchmarkSQL.
      * @param connectionString
      * @param propertiesFileLocation 
      * @throws FileNotFoundException 
      */
     private static void writeConnectionStringToPropertiesFile(final String connectionString, final String propertiesFileLocation) throws FileNotFoundException {
 
         final File f = new File(propertiesFileLocation);
 
         f.getParentFile().mkdirs();
 
         final StringBuilder prop = new StringBuilder();
 
         prop.append("name=H2O\n");
         prop.append("driver=org.h2.Driver\n");
         prop.append("conn=" + connectionString + "\n");
         prop.append("user=sa\n");
         prop.append("password=");
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Writing to properties file at: " + propertiesFileLocation);
 
         FileUtil.writeToFile(propertiesFileLocation, prop.toString());
     }
 
     private static boolean processTerminatesExistingInstances(final String arg) {
 
         return arg == null ? false : Boolean.valueOf(arg);
     }
 
     private static int processNumberOfInstances(final String numberOfInstances) throws StartupException {
 
         if (numberOfInstances != null) {
             return Integer.parseInt(numberOfInstances);
         }
         else {
             throw new StartupException("Number of instances to start was not specified.");
         }
     }
 
     /**
      * 
      * @param locations Delimited by semi-colons.
      * @return
      * @throws StartupException 
      */
     private static String[] processWorkerLocations(final String locations) throws StartupException {
 
         if (locations != null) {
             return locations.split(";");
         }
         else {
             throw new StartupException("The locations of worker instances were not specified.");
         }
     }
 
     private static String processDatabaseName(final String arg) {
 
         return arg == null ? DEFAULT_DATABASE_NAME : arg;
     }
 
 }
