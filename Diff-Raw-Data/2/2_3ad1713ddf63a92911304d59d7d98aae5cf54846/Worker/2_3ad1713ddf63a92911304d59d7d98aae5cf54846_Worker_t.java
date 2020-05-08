 package org.h2o.eval;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.rmi.AlreadyBoundException;
 import java.rmi.NotBoundException;
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.FutureTask;
 
 import org.h2.tools.DeleteDbFiles;
 import org.h2.util.NetUtils;
 import org.h2o.H2O;
 import org.h2o.db.id.DatabaseURL;
 import org.h2o.eval.interfaces.ICoordinatorRemote;
 import org.h2o.eval.interfaces.IWorker;
 import org.h2o.eval.interfaces.IWorkload;
 import org.h2o.eval.script.workload.WorkloadResult;
 import org.h2o.eval.script.workload.WorkloadThreadFactory;
 import org.h2o.test.fixture.MultiProcessTestBase;
 import org.h2o.util.H2OPropertiesWrapper;
 import org.h2o.util.exceptions.ShutdownException;
 import org.h2o.util.exceptions.StartupException;
 import org.h2o.util.exceptions.WorkloadParseException;
 
 import uk.ac.standrews.cs.nds.madface.HostDescriptor;
 import uk.ac.standrews.cs.nds.madface.JavaProcessDescriptor;
 import uk.ac.standrews.cs.nds.madface.PlatformDescriptor;
 import uk.ac.standrews.cs.nds.madface.ProcessDescriptor;
 import uk.ac.standrews.cs.nds.util.Diagnostic;
 import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
 import uk.ac.standrews.cs.nds.util.ErrorHandling;
 import uk.ac.standrews.cs.nds.util.NetworkUtil;
 
 public class Worker extends Thread implements IWorker {
 
     public static final String PATH_TO_H2O_DATABASE = "eval";
 
     public static final String REGISTRY_PREFIX = "H2O_WORKER::";
 
     private static final long CHECKER_SLEEP_TIME = 2000;
 
     private static final boolean LOGGING_TO_FILE_ENABLED = true;
 
     private static final int DATABASE_DIAGNOSTIC_LEVEL = 6; // 0 - full, 6 - none.
 
     private static String LOG_FILE_LOCATION;
 
     /**
      * Handle on the H2O process this worker is running.
      */
     private Process h2oProcess = null;
 
     /**
      * The name which will be given to the H2O instance being managed by this worker.
      */
     private final String h2oInstanceName;
 
     /**
      * JDBC connection string needed to communicate with the H2O database process.
      */
     private String connectionString;
 
     /**
      * JDBC connection to the running database process.
      */
     private Connection connection;
 
     /**
      * Count added to the end of the H2O instance created locally to make it possible to test this class with multiple instances running on the same machine.
      */
     private static int instanceCount = 0;
 
     private static ExecutorService workloadExecutor = Executors.newCachedThreadPool(new WorkloadThreadFactory() {
 
         @Override
         public Thread newThread(final Runnable r) {
 
             return new Thread(r);
         }
     });
 
     private ICoordinatorRemote remoteCoordinator;
 
     private final List<FutureTask<WorkloadResult>> executingWorkloads = new LinkedList<FutureTask<WorkloadResult>>();
 
     private boolean running = true;
 
     /**
      * ID fields (used in hashCode and equals).
      */
     private final InetAddress hostname;
 
     private final long randomID;
 
     @Override
     public void run() {
 
         final List<FutureTask<WorkloadResult>> toRemove = new LinkedList<FutureTask<WorkloadResult>>();
 
         while (isRunning()) {
 
             for (final FutureTask<WorkloadResult> workloadTask : executingWorkloads) {
 
                 if (workloadTask.isDone()) {
 
                     try {
                         remoteCoordinator.collateMonitoringResults(workloadTask.get());
                         toRemove.add(workloadTask);
                     }
                     catch (final InterruptedException e) {
                         e.printStackTrace();
                     }
                     catch (final ExecutionException e) {
                         e.printStackTrace();
                     }
                     catch (final RemoteException e) {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
                     }
                 }
 
             }
 
             executingWorkloads.removeAll(toRemove);
             toRemove.clear();
 
             try {
                 Thread.sleep(CHECKER_SLEEP_TIME);
             }
             catch (final InterruptedException e) {
             }
         }
     }
 
     public Worker() throws RemoteException, AlreadyBoundException, UnknownHostException {
 
         final String platform_name = System.getProperty("os.name");
 
         setLogFileLocation(platform_name);
 
         /*
          * Generate ID for this worker.
          */
         hostname = NetworkUtil.getLocalIPv4Address();
         randomID = new Random(System.currentTimeMillis()).nextLong();
 
         //Name the h2o instance after the machine it is being run on + a static counter number to enable same machine testing.
         h2oInstanceName = hostname.getHostName().replaceAll("-", "").replaceAll("\\.", "") + instanceCount++;
 
         Registry registry = null;
 
         try {
             registry = LocateRegistry.createRegistry(1099);
         }
         catch (final Exception e) {
             registry = LocateRegistry.getRegistry();
         }
 
         registry.bind(REGISTRY_PREFIX + h2oInstanceName, UnicastRemoteObject.exportObject(this, 0));
 
         start();
     }
 
     public void setLogFileLocation(final String platform_name) {
 
         if (platform_name.startsWith(PlatformDescriptor.NAME_WINDOWS)) {
             LOG_FILE_LOCATION = "/tmp";
         }
         else {
             LOG_FILE_LOCATION = "/tmp";
         }
     }
 
     @Override
     public void initiateConnection(final String hostname, final String bindName) throws RemoteException, NotBoundException {
 
         final Registry remoteRegistry = LocateRegistry.getRegistry(hostname);
 
         final Remote remoteCoordinatorAsRemote = remoteRegistry.lookup(bindName);
 
         remoteCoordinator = (ICoordinatorRemote) remoteCoordinatorAsRemote;
 
     }
 
     @Override
     public String startH2OInstance(final H2OPropertiesWrapper descriptor, final boolean startInRemoteDebug) throws RemoteException, StartupException {
 
         if (h2oProcess != null) {
             //Check if its still running.
             throw new StartupException("Couldn't start H2O instance. Another instance is already running at " + getHostname().getHostName());
         }
 
         final String descriptorFileLocation = PATH_TO_H2O_DATABASE + File.separator + "descriptor.h2od";
         saveDescriptorToDisk(descriptor, descriptorFileLocation);
 
         h2oProcess = startH2OProcess(setupBootstrapArgs(descriptorFileLocation), startInRemoteDebug);
 
         sleep(10000);// wait for the database to start up.
 
         createConnectionString();
 
         connection = MultiProcessTestBase.createConnectionToDatabase(connectionString);
 
         final boolean isRunning = checkDatabaseIsActive();
 
         if (!isRunning) { throw new StartupException("New H2O process couldn't be contacted once it had been created."); }
 
         return connectionString;
     }
 
     /**
      * Create the JDBC connection string needed to connect to the H2O instance that has been started locally.
      */
     public void createConnectionString() {
 
         final int jdbc_port = H2O.getDatabasesJDBCPort(PATH_TO_H2O_DATABASE + "/" + DatabaseURL.getPropertiesFileName(PATH_TO_H2O_DATABASE) + "_" + h2oInstanceName + ".properties");
         connectionString = "jdbc:h2:tcp://" + NetUtils.getLocalAddress() + ":" + jdbc_port + "/" + PATH_TO_H2O_DATABASE + "/" + h2oInstanceName;
     }
 
     @Override
     public void stopH2OInstance() throws RemoteException, ShutdownException {
 
         if (h2oProcess == null) { throw new ShutdownException("Couldn't stop H2O process because the reference to it was null."); }
 
         try {
             final Statement stat = connection.createStatement();
             stat.executeUpdate("SHUTDOWN IMMEDIATELY;");
         }
         catch (final SQLException e) {
             throw new ShutdownException("The SHUTDOWN query attempting to shutdown the database instance failed: " + e.getMessage());
         }
     }
 
     @Override
     public void terminateH2OInstance() throws RemoteException, ShutdownException {
 
         if (h2oProcess == null) {
 
             final String platform_name = System.getProperty("os.name");
 
             if (platform_name.startsWith(PlatformDescriptor.NAME_LINUX)) {
                 try {
 
                     final String killH2OInstance = "/usr/bin/pkill -9 -f '.*" + H2O.class.getCanonicalName() + ".*'";
                     Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Killing H2O instance: " + killH2OInstance);
                     Runtime.getRuntime().exec(killH2OInstance);
                 }
                 catch (final IOException e) {
                     throw new ShutdownException(e.getMessage());
                 }
             }
             else {
                 //There is nothing else we can do.
                 throw new ShutdownException("Couldn't terminate H2O process because the reference to it was null.");
             }
         }
         else {
             h2oProcess.destroy();
         }
     }
 
     @Override
     public void deleteH2OInstanceState() throws RemoteException, ShutdownException {
 
         try {
             DeleteDbFiles.execute(Worker.PATH_TO_H2O_DATABASE, null, true);
         }
         catch (final SQLException e) {
             throw new ShutdownException("Failed to delete database files: " + e.getMessage());
         }
 
     }
 
     @Override
     public boolean isH2OInstanceRunning() throws RemoteException {
 
         return checkDatabaseIsActive();
     }
 
     @Override
     public boolean startWorkload(final IWorkload workload) throws RemoteException, WorkloadParseException, SQLException {
 
         workload.initialiseOnWorker(connection, this);
 
         final FutureTask<WorkloadResult> future = new FutureTask<WorkloadResult>(new Callable<WorkloadResult>() {
 
             @Override
             public WorkloadResult call() {
 
                 return workload.executeWorkload();
             }
         });
 
         executingWorkloads.add(future);
 
         workloadExecutor.execute(future);
 
         return true;
     }
 
     @Override
     public boolean stopWorkload(final IWorkload workload) throws RemoteException {
 
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public boolean isWorkloadRunning(final IWorkload workload) throws RemoteException {
 
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public boolean isWorkloadRunning() throws RemoteException {
 
         for (final FutureTask<WorkloadResult> workloadTask : executingWorkloads) {
             if (!workloadTask.isDone()) { return true; }
         }
 
         return false;
     }
 
     @Override
     public void stopWorkloadChecker() throws RemoteException {
 
         setRunning(false);
     }
 
     /*
      * H2O Utility Methods.
      */
 
     /**
      * Start a new H2O instance locally, with the provided command line arguments.
      * @param args Arguments to be passed to {@link H2O} when it is started.
      * @return The process handle of the newly started process.
      * @throws StartupException If the process could not be started.
      */
     public Process startH2OProcess(final List<String> args) throws StartupException {
 
         return startH2OProcess(args, false);
     }
 
     public Process startH2OProcess(final List<String> args, final boolean startInDebug) throws StartupException {
 
         try {
             final ProcessDescriptor pd = new JavaProcessDescriptor().classToBeInvoked(H2O.class).jvmParams(addDebugParams(startInDebug)).args(args);
             return new HostDescriptor().getProcessManager().runProcess(pd);
         }
         catch (final Exception e) {
             e.printStackTrace();
             throw new StartupException("Failed to create new H2O process: " + e.getMessage());
         }
     }
 
     public List<String> addDebugParams(final boolean startInDebug) {
 
         if (startInDebug) {
             final List<String> jvmParams = new LinkedList<String>();
             jvmParams.add("-Xdebug -Xrunjdwp:transport=dt_socket,address=8998,server=y");
             return jvmParams;
         }
         else {
             return null;
         }
     }
 
     /**
      * Connects to the database instance using the {@link #connection} instance, and issues a read query. If the read
      * query fails a startup exception is thrown because the database is not active.
      * @throws StartupException If the database does not respond correctly to the query.
      */
     public boolean checkDatabaseIsActive() {
 
         try {
 
             if (connection == null) { return false; }
 
             final Statement createStatement = connection.createStatement();
            createStatement.executeUpdate("SET WRITE_DELAY 0");
             return true;
         }
         catch (final SQLException e) {
             return false;
         }
     }
 
     /**
      * Sleep for the specified amount of time, ignoring InterruptedExceptions.
      * @param sleepTime in milliseconds.
      */
     public void sleep(final int sleepTime) {
 
         try {
             Thread.sleep(sleepTime);
         }
         catch (final InterruptedException e) {
         }
     }
 
     /**
      * Create a list of arguments needed to start {@link H2O}.
      * @param descriptorFileLocation The location of the descriptor file for this database system.
      * @return The completed list of command line arguments.
      */
     public List<String> setupBootstrapArgs(final String descriptorFileLocation) {
 
         final List<String> args = new LinkedList<String>();
         args.add("-i" + h2oInstanceName);
         args.add("-f" + PATH_TO_H2O_DATABASE);
 
         args.add("-d" + descriptorFileLocation);
 
         if (LOGGING_TO_FILE_ENABLED) {
             args.add("-o" + LOG_FILE_LOCATION + File.separator + "h2o-stdout.log");
             args.add("-e" + LOG_FILE_LOCATION + File.separator + "h2o-stderr.log");
         }
 
         args.add("-D" + DATABASE_DIAGNOSTIC_LEVEL);
 
         return args;
     }
 
     /**
      * Save the descriptor file to disk at the specified location.
      * @param descriptor The properties file to be saved to disk.
      * @param descriptorFileLocation Where the properties file should be saved.
      */
     public void saveDescriptorToDisk(final H2OPropertiesWrapper descriptor, final String descriptorFileLocation) {
 
         try {
             final FileOutputStream descriptorFile = new FileOutputStream(descriptorFileLocation);
             descriptor.saveAndClose(descriptorFile);
         }
         catch (final IOException e1) {
             ErrorHandling.exceptionErrorNoEvent(e1, "Couldn't save descriptor file locally.");
         }
     }
 
     public synchronized void setRunning(final boolean running) {
 
         this.running = running;
     }
 
     public synchronized boolean isRunning() {
 
         return running;
     }
 
     @Override
     public void executeQuery(final String query) throws RemoteException, SQLException {
 
         final Statement stat = connection.createStatement();
         stat.execute(query);
 
     }
 
     /* (non-Javadoc)
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
 
         return "ID:" + h2oInstanceName;
     }
 
     @Override
     public InetAddress getHostname() throws RemoteException {
 
         return hostname;
 
     }
 
     @Override
     public int hashCode() {
 
         final int prime = 31;
         int result = 1;
         result = prime * result + (hostname == null ? 0 : hostname.hashCode());
         result = prime * result + (int) (randomID ^ randomID >>> 32);
         return result;
     }
 
     @Override
     public boolean equals(final Object obj) {
 
         if (this == obj) { return true; }
         if (obj == null) { return false; }
         if (getClass() != obj.getClass()) { return false; }
         final Worker other = (Worker) obj;
         if (hostname == null) {
             if (other.hostname != null) { return false; }
         }
         else if (!hostname.equals(other.hostname)) { return false; }
         if (randomID != other.randomID) { return false; }
         return true;
     }
 
     /**
      * Start an evaluation worker.
      * @param args N/A.
      * @throws RemoteException The worker fails to contact the local RMI registry.
      * @throws AlreadyBoundException The worker tries to bind itself to the local RMI registry and fails.
      * @throws UnknownHostException 
      */
     public static void main(final String[] args) throws RemoteException, AlreadyBoundException, UnknownHostException {
 
         Diagnostic.setLevel(DiagnosticLevel.FULL);
         new Worker();
     }
 }
