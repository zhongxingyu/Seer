 package edu.ch.unifr.diuf.testing_tool;
 
 import edu.ch.unifr.diuf.testing_tool.Machine.Status;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.schmizz.sshj.SSHClient;
 import net.schmizz.sshj.transport.TransportException;
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.PropertiesConfiguration;
 
 /**
  *
  * @author Teodor Macicas
  */
 public class MachineManager 
 {
     private final static Logger LOGGER = Logger.getLogger(
             MachineManager.class.getName());
     
     private ArrayList<Client> clients;
     private ArrayList<SSHClient> sshClients;
     private Server server;
     private List<TestParams> tests;
     
     private MachineConnectivityThread cct;
     private WriteStatusThread wst;
     private CheckMessagesThread cmt;
     private CheckRunningPIDsThread crpt;
     private FaultTolerantThread ftt;
     public static int testNum = 0;
 
     // it regulary checks if the machines are still reachable
     protected class MachineConnectivityThread extends Thread 
     {
         // sleep this time between two consecutive checks
         private int delay;
         private boolean finish;
         private ArrayList<SSHClient> sshClients;
         
         public MachineConnectivityThread(int delay) {
             this.delay = delay;
             this.finish = false;
             this.sshClients = createSSHConnectionsToClients();
         }
         
         public void setFinished(boolean finish) {
             this.finish = finish;
         }
         
         // regulary check the connection status and update a flag accordingly
         public void run() {
             while( !finish ) {
                 // now check client connectivity 
                 for(Iterator it=clients.iterator(); it.hasNext(); ) {
                     Client c = (Client)it.next();
                     try {
                        c.checkSSHConnection(sshClients.get(c.getId()+1));
                        c.setStatusConnection(Machine.Status.OK);
                     } catch (SSHConnectionException ex) {         
                         c.setStatusConnection(Machine.Status.SSH_CONN_PROBLEMS);
                         LOGGER.log(Level.SEVERE, "Connectivity problems with the client "
                                 +c.getIpAddress()+"). Check logs. "
                                 + "Retry in "+delay+"ms.", ex);
                     }
                 }
                 try {
                     // now sleep a bit until next check
                     Thread.sleep(delay);
                 } catch (InterruptedException ex) {
                     ex.printStackTrace();
                 }
             }
             disconnectSSHClients(sshClients);
         }
     }
       
     // it regulary writes to a log file the status of the running machines
     protected class WriteStatusThread extends Thread 
     {
         // sleep this time between two consecutive checks
         private int delay;
         private boolean finish;
         private FileOutputStream fis;
         private DateFormat dateFormat;
         
         
         public WriteStatusThread(int delay, String filePath) {
             this.delay = delay;
             this.finish = false;
             try {
                 File f = new File(filePath);
                 if( f.exists() ) 
                     f.delete();
                 f.createNewFile();
                 this.fis = new FileOutputStream(f);
             } catch (IOException ex) {
                 LOGGER.log(Level.SEVERE, null, ex);
             }
             this.dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
         }
         
         public void setFinished(boolean finish) {
             this.finish = finish;
         }
         
         //regulary write the status on an output file
         @Override
         public void run() {
             while( !finish ) {
                 StringBuilder sb = new StringBuilder();
                 sb.append("\nMachine status @ ");
                 sb.append(dateFormat.format(new Date())+"\n");
                 
                 // write clients status
                 sb.append("Clients: \n\t\t");
                 for(Iterator it=clients.iterator(); it.hasNext(); ) {
                     Client c = (Client)it.next();
                     sb.append(c.getStatusMessage()+"\n\t\t");
                 }
                 
                 try {
                     // write to file 
                     fis.write(sb.toString().getBytes());
                 } catch (IOException ex) {
                     LOGGER.log(Level.SEVERE, "Exception while writing status to "
                             + "output file.", ex);
                 }
                 
                 try {
                     // now sleep a bit 
                     Thread.sleep(delay);
                 } catch (InterruptedException ex) {
                     ex.printStackTrace();
                 }
             }
         }
     }
     
     // it regulary checks remotely for messages (e.g. if the clients are ready to 
     // start requests or if they are finished )
     protected class CheckMessagesThread extends Thread 
     {
         // sleep this time between two consecutive checks
         private int delay;
         private boolean finish;
         private FileOutputStream fis;
         private ArrayList<SSHClient> sshClients;
         
         public CheckMessagesThread(int delay) {
             this.delay = delay;
             this.finish = false;
             this.sshClients = createSSHConnectionsToClients();
         }
         
         public void setFinished(boolean finish) {
             this.finish = finish;
         }
         
         /*
          * regulary check the status of remote clients and set it accordingly to 
          * out local objects
          */
         @Override
         public void run() {
             int r;
             while( !finish ) {
                 try {
                     for(Iterator it=clients.iterator(); it.hasNext(); ) {
                         Client c = (Client)it.next();
                         // check if the remote client is done with the requests
                         r = SSHCommands.testRemoteFileExists(c, 
                                 Utils.getClientRemoteDoneFilename(c), sshClients.get(c.getId()+1));
                         if( r == 0 ) {
                             c.setStatusSynch(Status.DONE);
                             continue;
                         }
                         // if not, test if the cleint is still sending requests 
                         r = SSHCommands.testRemoteFileExists(c, 
                                 Utils.getClientRemoteStartRequestsFilename(c), sshClients.get(c.getId()+1));
                         if( r == 0 ) {
                             c.setStatusSynch(Status.RUNNING_REQUESTS);
                             continue;
                         }
                         // if not, check if the client has its threads ready for warmup
                         r = SSHCommands.testRemoteFileExists(c, 
                                 Utils.getClientRemoteReadyWarmupFilename(c), sshClients.get(c.getId()+1));
                         if( r == 0 ) {
                             c.setStatusSynch(Status.READY_WARMUP);
                             continue;
                         }
                         // if not, then no info yet about client synch
                         c.setStatusSynch(Status.NOT_INIT);
                     }
                 } catch (TransportException ex) {
                     ex.printStackTrace();
                 } catch (IOException ex) {
                     ex.printStackTrace();
                 }
                 try {
                     // now sleep a bit 
                     Thread.sleep(delay);
                 } catch (InterruptedException ex) {
                     ex.printStackTrace();
                 }
             }
             disconnectSSHClients(sshClients);
         }
     }
     
     // it regulary checks if server and client PIDs are still running
     // and it changes the status accordingly 
     protected class CheckRunningPIDsThread extends Thread 
     {
         // sleep this time between two consecutive checks
         private int delay;
         private boolean finish;
         private FileOutputStream fis;
         private ArrayList<SSHClient> sshClients;
         
         public CheckRunningPIDsThread(int delay) {
             this.delay = delay;
             this.finish = false;
             this.sshClients = createSSHConnectionsToClients();
         }
         
         public void setFinished(boolean finish) {
             this.finish = finish;
         }
         
         /*
          * regulary check the status of remote clients and set it accordingly to 
          * out local objects
          */
         @Override
         public void run() {
             int r;
             while( !finish ) {
                 try {
                     // check server
                     if( server.getPID() != 0 ) {
                         r = SSHCommands.checkIfRemotePIDIsRunning(server, 
                                 server.getPID(), sshClients.get(0));
                         if( r == 0 )
                             server.setStatusProcess(Status.PID_RUNNING);
                         else
                             server.setStatusProcess(Status.PID_NOT_RUNNING);
                     }
                     else
                         server.setStatusProcess(Status.NOT_INIT);
                     
                     // check clients 
                     for(Iterator it=clients.iterator(); it.hasNext(); ) {
                         Client c = (Client)it.next();
                         // check only if the program has been started and the PID gathered
                         if( c.getPID() != 0 ) {
                             r = SSHCommands.checkIfRemotePIDIsRunning(c,
                                     c.getPID(), sshClients.get(c.getId()+1));
                             if( r == 0 )
                                 c.setStatusProcess(Status.PID_RUNNING);
                             else
                                 c.setStatusProcess(Status.PID_NOT_RUNNING);
                         }
                         else 
                             c.setStatusProcess(Status.NOT_INIT);
                     }
                 } catch (TransportException ex) {
                     ex.printStackTrace();
                 } catch (IOException ex) {
                     ex.printStackTrace();
                 }
                 try {
                     // now sleep a bit 
                     Thread.sleep(delay);
                 } catch (InterruptedException ex) {
                     ex.printStackTrace();
                 }
             }
             disconnectSSHClients(sshClients);
         }
     }
     
     // if the failing condition is met, then it interrupts the main thread
     protected class FaultTolerantThread extends Thread 
     {
         // sleep this time between two consecutive checks
         private int delay;
         private boolean finish;
         private Thread runningTestThread;
         private double failingClients;
         private ArrayList<SSHClient> sshClients;
         
         public FaultTolerantThread(int delay, Thread runningTestThread) {
             this.delay = delay;
             this.finish = false;
             this.runningTestThread = runningTestThread;
             this.sshClients = createSSHConnectionsToClients();
         }
         
         public void setFinished(boolean finish) {
             this.finish = finish;
         }
         
         private boolean failingCondition() throws TransportException, IOException { 
             int failing_clients=0;
             if( server.status_process == Status.PID_NOT_RUNNING ) 
                 return true;
             
             for(Iterator it=clients.iterator(); it.hasNext(); ) {
                 Client c = (Client)it.next();
                 
                 if( (c.status_process == Status.PID_NOT_RUNNING && 
                     c.status_synch != Status.DONE) ) {
                     System.out.println("[FAILURE] Client PID NOT RUNNING and status NOT DONE");
                     failing_clients++;
                     continue;
                 } 
                 // client running, but lately no data in the log
                 if( c.status_process == Status.PID_RUNNING && 
                         !c.isProgressing(sshClients.get(c.getId()+1)) ) {
                     System.out.println("[FAILURE] Client PID RUNNING, but no progress on the log file. "
                             + "Last progress it was " + c.getLastLogModification() + " seconds ago ...");
                     failing_clients++;
                 }
             }
             if( failing_clients == 0 ) 
                 return false;
             failingClients = ((double)failing_clients/clients.size());
             //System.out.println("Failing clients percentage: " + failingClients);
             if( failingClients >= 
                     clients.get(0).getRestartConditionPropThreadsDead() ) {
                 return true;
             }
             return false;
         }
         
         /*
          * regulary check the status of remote clients and interrupt the one 
          * running the test if the failing condition is met 
          */
         @Override
         public void run() {
             while( !finish ) {
                 try {
                     // now sleep a bit 
                     Thread.sleep(delay);
                     if( failingCondition() && runningTestThread.isAlive() ) {
                         // if clients failed, then notify main thread and wait that 
                         // the server and clients are restarted
                         System.out.println("[FAILURE] " + (failingClients*100) + "% of the clients "
                                 + "have failed. Therefore, restart the test ...");
                         runningTestThread.interrupt();
                         Thread.sleep(15000);
                     }
                 } catch (Exception ex) {
                     ex.printStackTrace();
                 }
             }
             disconnectSSHClients(sshClients);
         }
     }
     
     public MachineManager() {
         this.clients = new ArrayList<Client>();
         this.tests = new ArrayList<TestParams>();
         this.server = null;
     }
     
      /**
      *
      * @param a new client machine
      */
     public void addNewClient(Client new_client) throws ClientNotProperlyInitException { 
         if( new_client.clientProperlyCreated() )
             this.clients.add(new_client);
         else 
             throw new ClientNotProperlyInitException("Client " + new_client.getIpAddress()
                     + " does not have all needed params set up.");
     }
     
      /**
      *
      * @return a client from the list of them
      */
     public Client getClientNo(int no) { 
         if( no < 0 || no > clients.size() ) 
             return null;
         return clients.get(no);
     }
 
     /**
      * 
      * @return 
      */
     public int getClientsNum() {
         return clients.size();
     }
     
      /**
      *
      * @param the server that will be used by the clients
      */
     public void setServer(Server s) { 
         this.server = s; 
     }
     
      /**
      *
      * @return server
      */
     public Server getServer() { 
         return this.server;
     }
 
 
     public List<TestParams> getTests() {
         return tests;
     }
 
     /**
      * 
      * @return 
      */
     public ArrayList<SSHClient> getSSHClients() { 
         return this.sshClients;
     }
     
     /**
      * Parse the .properties file and create the server and clients objects.
      * 
      * @throws ConfigurationException
      * @throws WrongIpAddressException
      * @throws WrongPortNumberException
      * @throws ClientNotProperlyInitException 
      * @throws FileNotFoundException
      */
     public void parsePropertiesFile() throws ConfigurationException, 
             WrongIpAddressException, WrongPortNumberException, ClientNotProperlyInitException, 
             FileNotFoundException, UnwritableWorkingDirectoryException, TransportException, IOException { 
         
         Configuration config = new PropertiesConfiguration(Utils.PROPERTIES_FILENAME);
         List serverListenHostPort = config.getList("server.listenHostPort");
         String serverFaultTolerant = config.getString("server.faultTolerant");
         String serverRestartAttempts = config.getString("server.restartAttempts");
         String serverTxSupport = config.getString("server.tx-support", "default");
         /*StringTokenizer st = new StringTokenizer(serverListenHostPort, ":");
         if( !serverListenHostPort.contains(":") || st.countTokens() != 2 ) {
             throw new ConfigurationException("Parsing error of server.listenHostPort. "
                     + "Please pass data using the pattern IP:PORT. " 
                     + serverListenHostPort);
         } */
         this.server = new Server();
 
         StringTokenizer st;
         for(Iterator it=serverListenHostPort.iterator(); it.hasNext(); ) {
             String server_listen_host = (String)it.next();
             st = new StringTokenizer(server_listen_host, ":");
            if( !server_listen_host.contains(":") || st.countTokens() != 2 ) {
                 throw new ConfigurationException("Parsing error of server.listenHostPort. "
                     + "Please pass data using the pattern IP:PORT. "
                     + server_listen_host);
             }
             this.server.setServerHTTPListenAddress(st.nextToken());
             this.server.setServerHttpPort(Integer.valueOf(st.nextToken()));
         }
         this.server.setFaultTolerant(serverFaultTolerant.trim());
         try {
             int no_rest = Integer.valueOf(serverRestartAttempts);
             if( no_rest < 0 ) {
                 throw new ConfigurationException("Parsing error of server.restartAttempts. "
                         + "Please check the right format in the properties file and re-run this.");
             }
         } catch (NumberFormatException ex) { 
             throw new ConfigurationException("Parsing error of server.restartAttempts. "
                         + "Please pass integer value to this parameter.");
         }
         this.server.setRestartAttempts(Integer.valueOf(serverRestartAttempts));        
         this.server.setTxSupport(serverTxSupport);
         
         
         // get clients program filename 
         Utils.CLIENT_PROGRAM_LOCAL_FILENAME = config.getString("clients.programJarFile");
         if( Utils.CLIENT_PROGRAM_LOCAL_FILENAME == null || 
             Utils.CLIENT_PROGRAM_LOCAL_FILENAME.isEmpty() ||
             ! new File(Utils.CLIENT_PROGRAM_LOCAL_FILENAME).exists() ) {
             throw new FileNotFoundException("Client local program file (" + 
                     ((Utils.CLIENT_PROGRAM_LOCAL_FILENAME==null)?"null":Utils.CLIENT_PROGRAM_LOCAL_FILENAME)
                     + ") does not exist. Please pass an already existing "
                     + "file or a correct path.");
         }
         Utils.CLIENT_PROGRAM_LOCAL_FILENAME = Utils.CLIENT_PROGRAM_LOCAL_FILENAME.trim();
         
         // this is used by the clients 
         List clientsUserHostPort = config.getList("clients.sshUserHostPort");
         String clientsWorkingDir = config.getString("clients.workingDirectory");
         String clientsRestartCondition = config.getString("clients.restartConditionPropThreadsDead");
         double clients_rest_cond = 0;
         try {
             clients_rest_cond = Double.valueOf(clientsRestartCondition);
             if( clients_rest_cond < 0 || clients_rest_cond > 1 ) { 
                 throw new ConfigurationException("Please give restartConditionPropThreadsDead parameter"
                         + " as double between 0 and 1 inclusive. ");
             }
         }catch (NumberFormatException ex) {
             throw new ConfigurationException("Parsing error of clients.restartConditionPropThreadsDead. "
                         + "Please pass double value to this parameter.");
         }
         String clientsTimeoutSec = config.getString("clients.timeoutSeconds");
         int clients_timeout_s;
         try {
             clients_timeout_s = Integer.valueOf(clientsTimeoutSec);
         } catch (NumberFormatException ex) {
             throw new ConfigurationException("Parsing error of clients.timeoutSeconds. "
                         + "Please pass double value to this parameter.");
         }
         List clientsTests = config.getList("clients.tests");
 
         // iterate the list and create the clients
         int counter = -1;
         for(Iterator it=clientsUserHostPort.iterator(); it.hasNext(); ) {
             ++counter;
             String client_host = (String)it.next();
             if( ! client_host.contains("@") ) {
                 throw new ConfigurationException("Parsing error of clients.sshUserHostPort. "
                         + "Please pass data using the pattern Host@IP[:PORT]. ");
             }   
             String clientSSHUsername = client_host.substring(0, client_host.indexOf("@"));
             String clientSSHIpPort = client_host.substring(client_host.indexOf("@")+1);
             st = new StringTokenizer(clientSSHIpPort, ":");
             Client c;
             if( clientSSHIpPort.contains(":") ) {
                 if( st.countTokens() != 2 ) { 
                     throw new ConfigurationException("Parsing error of client.sshUserHostPort. "
                         + "Please pass data using the pattern Host@IP[:PORT]. " 
                         + clientSSHIpPort);
                 }
                 // create the server here
                 c = new Client(st.nextToken(), Integer.valueOf(st.nextToken()), 
                     clientSSHUsername, counter);
             }
             else {
                 // use default SSH port (22)
                 c = new Client(st.nextToken(), 22, clientSSHUsername, counter);
             }
             // set server info 
             c.setServerInfo(server.getServerHTTPListenAddress(counter),
                     server.getServerHttpPort(counter));
             c.setRestartConditionPropThreadsDead(clients_rest_cond);
             c.setTimeoutSec(clients_timeout_s);
             c.setWorkingDirectory(clientsWorkingDir);
             
             // add tests
             for( Iterator it2=clientsTests.iterator(); it2.hasNext(); ) {
                 c.addNewTest((String)it2.next());
             }
             // store this client 
             this.addNewClient(c);
         }
 
         // parse tests files
         String[] testsPropertyFileNames = config.getString("clients.tests").split("\\s+");
         for(String name : testsPropertyFileNames) {
             String nameWithExtension = name + ".properties";
             Configuration testConfig = new PropertiesConfiguration(nameWithExtension);
             TestParams tp = new TestParams();
             tp.setNumClients(clients.size());
             tp.setTestName(name);
             
             // server related params
             String serverGraphSourceName = testConfig.getString("server.graph.source");
             if( serverGraphSourceName == null || serverGraphSourceName.isEmpty() ) { 
                 throw new ConfigurationException("Source graph name for test " + nameWithExtension 
                         + " is null or empty.");
             }
             String serverGraphDestName = testConfig.getString("server.graph.dest");
             if( serverGraphDestName == null || serverGraphDestName.isEmpty() ) { 
                 throw new ConfigurationException("Dest graph name for test " + nameWithExtension 
                         + " is null or empty.");
             }
             tp.setTestServerSourceGraphName(serverGraphSourceName);
             tp.setTestServerDestGraphName(serverGraphDestName);
             tp.setTestServerGraphReset(testConfig.getInt("server.graph.reset", 0));
             tp.setGraphSnapshot(testConfig.getInt("server.graph.snapshot", 0));
             tp.setTestReadCons(testConfig.getString("server.read.consistency", "one" ));
             tp.setTestWriteCons(testConfig.getString("server.write.consistency", "one" ));
             tp.setTransLockGran(testConfig.getString("server.trans.locking.granularity", "epv"));
             tp.setReplicationFactor(testConfig.getInt("server.replication.factor", 1));
             tp.setCheckMyWritesMode(testConfig.getString("server.consistency.check_my_writes", "on"));
             
             // client related params
             tp.setTestNum(testConfig.getInt("test.num", 1));
             List testThreadNumList = testConfig.getList("test.thread.num", Arrays.asList(1));
             for(Iterator it=testThreadNumList.iterator(); it.hasNext(); ) {
                 Integer threadNum = Integer.valueOf((String)it.next());
                 tp.addTestThreadNum(threadNum);                
             }
             tp.setTestWarmupPer(testConfig.getInt("test.period.warmup", 1));
             tp.setTestRunningPer(testConfig.getInt("test.period.running", 10));
             tp.setTestOperationType(testConfig.getInt("test.operation.type", 0));
             tp.setTestOperationNum(testConfig.getInt("test.operation.num", 1));
             tp.setTestTransRetrials(testConfig.getInt("test.transaction.retrials", 5));
             tp.setConflictsParameter(testConfig.getString("test.conflicts", "no"));
             tp.setDiffEnt(testConfig.getInt("test.num.diff.e", 10));
             tp.setDiffPropPerEnt(testConfig.getInt("test.num.diff.p.per.e", 5));
             tp.setDiffValuesPerProp(testConfig.getInt("test.num.diff.v.per.p", 1));
             
             // add the new test scenario
             tests.add(tp);
         }
     }
     
     /**
      * 
      */
     public void createSSHClients() {
         // now create also the ssh 
         this.sshClients = createSSHConnectionsToClients();
     }
     
     /**
      * 
      * @return 
      */
     public ArrayList<SSHClient> createSSHConnectionsToClients() {
         ArrayList<SSHClient> ssh_clients = new ArrayList<SSHClient>();
         try {
             /*server_ssh.loadKnownHosts();
             server_ssh.connect(server.getIpAddress(), server.getPort());
             server_ssh.authPublickey(server.getSSHUsername());*/
             ssh_clients.add(0, null);
             //System.out.println("add server ");
             
             SSHClient client_ssh; 
             for(Iterator it=clients.iterator(); it.hasNext(); ) {
                 Client c = (Client) it.next(); 
                 //System.out.println("add client " + c.getIpAddress());
                 client_ssh = new SSHClient();
                 client_ssh.loadKnownHosts();
                 client_ssh.connect(c.getIpAddress(), c.getPort());
                 client_ssh.authPublickey(c.getSSHUsername());
                 ssh_clients.add(client_ssh);
             }
         } catch (IOException ex) {
             LOGGER.log(Level.SEVERE, null, ex);
         }
         return ssh_clients;
     }
     
     /**
      * 
      * @param ssh_clients 
      */
     public void disconnectSSHClients(ArrayList<SSHClient> ssh_clients) { 
         for(Iterator it=ssh_clients.iterator(); it.hasNext(); ) {
             SSHClient s = (SSHClient) it.next();
             // just a trick to skip the server ssh client
             if( s == null ) 
                 continue;
             try {
                 s.disconnect();
             } catch (IOException ex) {
                 LOGGER.log(Level.SEVERE, null, ex);
             }
         }
     }
     
     
     /**
      *
      * @return false if either the server or clients are not yet set
      */
     public boolean checkIfClientsSet() { 
         if( this.clients.isEmpty() ) 
             return false;
         return true;
     }
     
     /**
      * Either all or none ip addresses are from 127.0.0.0/8 subnetwork.
      * 
      * @return true if either all or none are loopback
      */
     public boolean checkIfAllOrNoneLoopbackAddresses() { 
         boolean all_loopback = true;
         if( ! Utils.isLoopbackIpAddress(server.getServerHTTPListenAddress(0)) )
             all_loopback = false;
         for(Iterator it=clients.iterator(); it.hasNext(); ) {
             Client c = (Client)it.next();
             if( all_loopback && ! Utils.isLoopbackIpAddress(c.getIpAddress()) ) {
                 // at least one is not loopback, but the others are loopback
                 return false;
             }
             if( !all_loopback && Utils.isLoopbackIpAddress(c.getIpAddress()) ) {
                 // at least one is loopback, but the others are not
                 return false;
             }
         }
         return true;
     }
     
     
     /**
      * All clients must have network connection with the server. Check it by 
      * remotely running a ping command. 
      * 
      * @return 
      */
     public boolean checkClientsCanAccessServer()
             throws TransportException, IOException { 
         int r;
         // iterate through clients and check whether they can ping the server
         for(Iterator it=clients.iterator(); it.hasNext(); ) {
             Client c = (Client) it.next(); 
             r = SSHCommands.clientPingServer(c, sshClients.get(c.getId()+1));
             if( r != 0 )
                 return false;
         }
         return true;
     }
     
     
     /**
      * Upload the program to the clients
      * 
      * @throws FileNotFoundException
      * @throws IOException 
      */
     public void uploadProgramToClients() 
             throws FileNotFoundException, IOException {        
         for(Iterator it=clients.iterator(); it.hasNext(); ) {
             Client c = (Client)it.next(); 
             c.uploadProgram(Utils.CLIENT_PROGRAM_LOCAL_FILENAME, sshClients.get(c.getId()+1));
             // test the remote file exists
             int r = SSHCommands.testRemoteFileExists(c, 
                     Utils.getClientProgramRemoteFilename(c), sshClients.get(c.getId()+1));
             if( r == 0 )
                 System.out.println("[INFO] Remote file successfuly uploaded on client " + 
                         c.getIpAddress() + ".");
             else 
                 System.out.println("[INFO] Remote file could not be uploaded on client " + 
                         c.getIpAddress() + "." + " Exit code: " + r);
         }
     }
     
     /**
      * 
      * @return true if all connectivity statuses are OK
      */
     public boolean allAreConnectionsOK() {
         for(Iterator it=clients.iterator(); it.hasNext(); ) {
             Client c = (Client)it.next();
             if( c.getStatusConnection() != Status.OK ) {
                 System.err.println("[ERROR] Client "+c.getIpAddress()
                         +" has network connection problem.");
                 return false;
             }
         }
        return true;
     }
    
     
     /**
      * 
      * @throws TransportException
      * @throws IOException 
      */
     public void startAllClients() throws TransportException, IOException, InterruptedException {
         int counter = -1;
         for(Iterator it=clients.iterator(); it.hasNext(); ) {
             ++counter;
             Client c = (Client)it.next();
             c.runClientRemotely(this.server, sshClients.get(c.getId()+1), counter);
         }
     }    
     
     /**
      * Start only the connectivity thread firstly.
      */
     public void startConnectivityThread() {
          this.cct = new MachineConnectivityThread(Utils.DELAY_CHECK_CONN_MS);
          this.cct.start();
     }
     
     /**
      * 
      * Just run the status, check messages and check PIDs threads. 
      */
     public void startOtherThreads() {
         this.wst = new WriteStatusThread(Utils.DELAY_WRITE_STATUS_MS, Utils.STATUS_FILENAME);
         this.cmt = new CheckMessagesThread(Utils.DELAY_CHECK_MESSAGES);
         this.crpt = new CheckRunningPIDsThread(Utils.DELAY_CHECK_RUNNING_PIDS);
         try {
              // write status 
              this.wst.start();
              Thread.sleep(200);
              // check messages 
              this.cmt.start();
              Thread.sleep(200);
              // check PIDs
              this.crpt.start();
              Thread.sleep(400);
         } catch (InterruptedException ex) {
             ex.printStackTrace();
         }
     }
     
     /**
      * 
      * @return  true if all clients are synchronized
      */
     public boolean checkClientsSynch() { 
         //check every client synch status
         for(Iterator it=clients.iterator(); it.hasNext(); ) { 
             Client c = (Client)it.next();
             if( c.getStatusSynch() != Status.READY_WARMUP ) 
                 return false;
         }
         return true;
     }
     
     /**
      * 
      * @return
      * @throws TransportException
      * @throws IOException 
      * @return null means no client is in trouble
      */
     public Client checkClientsRemoteDatafiles() throws TransportException, IOException { 
         //check every client if the data file exists remotely
         for(Iterator it=clients.iterator(); it.hasNext(); ) { 
             Client c = (Client)it.next();
             if( SSHCommands.testRemoteFileExists(c, Utils.getClientRemoteDataFilename(c), 
                     sshClients.get(c.getId()+1)) != 0 ) 
                 return c;
         }
         return null;
     }
     
     /**
      * 
      * @throws TransportException
      * @throws IOException 
      */
     public void killClients() throws TransportException, IOException { 
         for(Iterator it=clients.iterator(); it.hasNext(); ) { 
             Client c = (Client)it.next();
             c.killClient(sshClients.get(c.getId()+1));
         }
     }
     
     /**
      * 
      * @throws TransportException
      * @throws IOException 
      */
     public void sendClientsMsgToStartRequests() throws TransportException, IOException { 
         for(Iterator it=clients.iterator(); it.hasNext(); ) { 
             Client c = (Client)it.next();
             SSHCommands.createRemoteFile(c, Utils.getClientRemoteStartRequestsFilename(c),
                     sshClients.get(c.getId()+1));
         }
     }
     
     /**
      * 
      * @return  true if all clients finished their tests
      */
     public boolean checkTestsCompletion() { 
         //check every client synch status
         for(Iterator it=clients.iterator(); it.hasNext(); ) { 
             Client c = (Client)it.next();
             if( c.getStatusSynch() != Status.DONE ) 
                 return false;
         }
         return true;
     }
     
     /**
      * Download on local folder all the logs from server and clients.
      * @throws IOException 
      */
     public void downloadAllLogs(String resultFile, int testNo) throws IOException, InterruptedException { 
         String currentDir = new java.io.File( "." ).getCanonicalPath();
 
         int counter=-1;
         for(Iterator it=clients.iterator(); it.hasNext(); ) { 
             Client c = (Client)it.next();
             String testRunDir = currentDir+"/"+server.getFullTestName()+"/"+testNo+"/";
             
             Runtime.getRuntime().exec(new String[]{"/bin/bash","-c", "mkdir " + testRunDir}).waitFor();
             
             String localFilename = testRunDir+Utils.getClientLocalFilename(c, ++counter, testNo);
             SSHCommands.downloadRemoteFile(c, Utils.getClientLogRemoteFilename(c),
                     localFilename, sshClients.get(c.getId()+1));
             
             /*Runtime.getRuntime().exec(new String[]{"/bin/bash",
                 "-c", "tail -n1 " + localFilename + " >> " + currentDir+"/"+server.getFullTestName()+"/"+resultFile}).waitFor();*/
             Runtime.getRuntime().exec(new String[]{"/bin/bash",
                 "-c", "cat " + localFilename + " | grep -A1 'No threads' | tail -n1 >> " +
                 currentDir+"/"+server.getFullTestName()+"/"+resultFile}).waitFor();
             Runtime.getRuntime().exec(new String[]{"/bin/bash",
                 "-c", "cat " + localFilename + " | grep -A1 'total no entities' | tail -n1 >> " +
                 currentDir+"/"+server.getFullTestName()+"/"+resultFile+".vers"}).waitFor();
         }
         testNum++;
     }
     
     
     /**
      * 
      * Join the status threads;
      */
     public void joinAllThreads() {
         this.cct.setFinished(true);
         this.wst.setFinished(true);
         this.cmt.setFinished(true);
         this.crpt.setFinished(true);
         try {
             //System.out.println("Join cct");
             this.cct.join(4000);
             //System.out.println("Join wst");
             this.wst.join(4000);
             //System.out.println("Join cmt");
             this.cmt.join(4000);
             //System.out.println("Join crpt");
             this.crpt.join(4000);
             if( this.ftt != null ) {
                 //System.out.println("Join FTT");
                 this.ftt.setFinished(true);
                 this.ftt.join(4000);
             }
         } catch (InterruptedException ex) {
             ex.printStackTrace();
         }
     }
 
     /**
      * 
      * @param testThread 
      */
     public void startFaultTolerantThread(Thread testThread) { 
         this.ftt = new FaultTolerantThread(Utils.DELAY_CHECK_FAULT_MS, testThread);
         this.ftt.start();
     }
     
     /**
      * 
      * @throws InterruptedException 
      */
     public void joinFaultTolerantThread() throws InterruptedException {
         if (ftt != null) {
             this.ftt.setFinished(true);
             this.ftt.join(4000);
         }
     }
     
     /**
      * 
      * @throws TransportException
      * @throws IOException 
      */
     public void deleteClientPreviouslyMessages() 
             throws TransportException, IOException { 
         for(Iterator it=clients.iterator(); it.hasNext(); ) {
             Client c = (Client)it.next();
             c.deletePreviousRemoteMessages(sshClients.get(c.getId()+1));
         }
     }
     
     /**
      * 
      * @throws TransportException
      * @throws IOException 
      */
     public void deleteClientLogs() throws TransportException, IOException {
         for(Iterator it=clients.iterator(); it.hasNext(); ) {
             Client c = (Client)it.next();
             SSHCommands.deleteRemoteFile(c, c.getWorkingDirectory()+"/log*.data", 
                     sshClients.get(c.getId()+1));
         }
     }
     
     /**
      * 
      * @throws TransportException
      * @throws IOException 
      */
     public void deleteServerLogs() 
             throws TransportException, IOException { 
         SSHCommands.deleteRemoteFile(server, server.getWorkingDirectory()+"/log*.data", 
                 sshClients.get(0));
     }
     
     /**
      * Print information about server and all clients.
      * 
      * @return 
      */
     public String printMachines() { 
         StringBuilder sb = new StringBuilder();
         sb.append("\nServer: "); 
         sb.append("\n\t http server info: ");
         for( int i=0; i<server.getServerNoAddresses(); ++i ) {
             sb.append(server.getServerHTTPListenAddress(i)).append(":");
             sb.append(server.getServerHttpPort(i));
             sb.append(",");
         }
         sb.append("\n\t fault tolerance: "); 
         sb.append(server.getFaultTolerant()).append(" ");
         sb.append(server.getRestartAttempts()).append("retrials ");
         sb.append("\nClients:");
         
         int counter = -1;
         for(Iterator it=clients.iterator(); it.hasNext(); ) {
             Client c = (Client)it.next();
             ++counter;
             sb.append("\n\tClient ").append(counter); 
             sb.append("\n\t\t ssh info: ");
             sb.append(c.getSSHUsername()).append("@");
             sb.append(c.getIpAddress()).append(":").append(c.getPort());
             sb.append("\n\t\t server info: ");
             sb.append(c.getServerIpAddress()).append(" ").append(c.getServerPort());
             sb.append("\n\t\t running tests: "); 
             sb.append(c.testsToString());
             sb.append("\n\t\t fault tolerance: "); 
             sb.append(c.getRestartConditionPropThreadsDead()).append(" percentage ");
             sb.append("of needed dead clients to restart test");
             sb.append("\n\t\t fault tolerance timeout: after "); 
             sb.append(c.getTimeoutSec()).append("seconds of no log activity client is considered failed");
         }
         sb.append("\n");
         return sb.toString();
     }
     
     /**
      * 
      * @return 
      */
     public String printTests() { 
         StringBuilder sb = new StringBuilder();
         for(int i=0; i<tests.size(); ++i) {
             TestParams t = tests.get(i);
             sb.append("Test: ").append(t.testId).append("\n");
             sb.append(t.toString());
         }
         sb.append("\n");
         return sb.toString();
     }
 
     /**
      * Initialize new working threads to be able to reuse Machine Manager
      * 
      **/
     public void updateWorkingThreads() {
         this.cct = new MachineConnectivityThread(Utils.DELAY_CHECK_CONN_MS);
         this.wst = new WriteStatusThread(Utils.DELAY_CHECK_CONN_MS, Utils.STATUS_FILENAME);
         this.cmt = new CheckMessagesThread(Utils.DELAY_CHECK_CONN_MS);
         this.crpt = new CheckRunningPIDsThread(Utils.DELAY_CHECK_CONN_MS);
     }
 }
