 package edu.ch.unifr.diuf.testing_tool;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import net.schmizz.sshj.SSHClient;
 import net.schmizz.sshj.transport.TransportException;
 
 /**
  *
  * @author Teodor Macicas
  */
 public class Client extends Machine 
 {
     private String serverIpAddress;
     private int serverPort;
     
     private Integer clientId;
     private int lastLogAccessSec;
     
     private String inputFilename; 
     private int noThreads; 
     private int warmupPeriod; 
     private int runningPeriod; 
     private int operationType;
     private int operationNum;
     private int transRetrials;
 
     private String conflictsFlag;
     private int diffE; 
     private int diffPperE;
     private int diffVperP;
     
     private double restartCond;
     private int timeoutSec;
     
     // set here the name of the tests to be run
     private List<String> tests;
     
     public Client(String ipAddress, int port, String sshUsername, Integer id) 
             throws WrongIpAddressException, WrongPortNumberException {
         super();
         this.setIpAddress(ipAddress);
         this.setPort(port);
         this.setSSHUsername(sshUsername);
         this.tests = new ArrayList<String>();
         this.serverIpAddress = "0.0.0.0";
         this.serverPort = 0;
         // save here some DEFAULT values
         this.inputFilename = "";
         this.noThreads = 1;
         this.warmupPeriod = 1;
         this.runningPeriod = 10;
         this.operationType = 0;
         this.operationNum = 1;
         this.transRetrials = 5;
         this.clientId = id;
     }
     
     /**
      * 
      * @param ipAddress
      * @throws WrongIpAddressException 
      */
     public void setServerIpAddress(String ipAddress) throws WrongIpAddressException { 
         if( Utils.validateIpAddress(ipAddress) )
                this.serverIpAddress = ipAddress;
         else
             throw new WrongIpAddressException(ipAddress + " cannot be set due to"
                     + " validation errors");
     }
     
     /**
      * 
      * @return 
      */
     public String getServerIpAddress() {
         return this.serverIpAddress;
     }
     
     /**
      * 
      * @param port 
      */
     public void setServerPort(Integer port) throws WrongPortNumberException { 
          if( Utils.validateRemotePort(port) ) 
             this.serverPort = port;
         else
             throw new WrongPortNumberException(port + " cannot be set due to "
                     + "validation errors.");
     }
     
     /**
      * 
      * @return 
      */
     public int getServerPort() { 
         return this.serverPort;
     }
     
     /**
      * 
      * @param inputFilename 
      */
     public void setInputFilename(String inputFilename) {
         this.inputFilename = inputFilename;
     }
     
     /**
      * 
      * @return 
      */
     public String getInputFilename() { 
         return this.inputFilename;
     }
     
     /**
      * 
      * @param noThreads 
      */
     public void setNoThreads(int noThreads) {
         this.noThreads = noThreads;
     }
     
     /**
      * 
      * @return 
      */
     public int getNoThreads() { 
         return this.noThreads;
     }
     
     /**
      * 
      * @param period
      */
     public void setWarmupPeriod(int period) { 
         this.warmupPeriod = period;
     }
     
     /**
      * 
      * @return 
      */
     public int getWarmupPeriod() { 
         return this.warmupPeriod;
     }
     
     /**
      * 
      * @param period
      */
     public void setRunningPeriod(int period) { 
         this.runningPeriod = period;
     }
     
     /**
      * 
      * @return 
      */
     public int getRunningPeriod() { 
         return this.runningPeriod;
     }
     
     /**
      * 
      * @param operType 
      */
     public void setOperationType(int operType) { 
         this.operationType = operType;
     }
     
     /**
      * 
      * @return 
      */
     public int getOperationType() { 
         return this.operationType;
     }
     
     /**
      * 
      * @param operNum 
      */
     public void setOperationNum(int operNum) {
         this.operationNum = operNum;
     }
     
     /**
      * 
      * @return 
      */
     public int getOperationNum() {
         return this.operationNum;
     }
     
     /**
      * 
      * @param retrials 
      */
     public void setTransRetrials(int retrials) { 
         this.transRetrials = retrials;
     }
     
     /**
      * 
      * @return 
      */
     public int getTransRetrials() { 
         return this.transRetrials;
     }
 
     /**
      *
      * @param test
      */
     public void addNewTest(String test) {
         this.tests.add(test);
     }
 
     /**
      *
      * @return
      */
     public List<String> getTests() {
         return this.tests;
     }
 
     /**
      * 
      * @return 
      */
     public Integer getId() { 
         return this.clientId;
     }
     
     /**
      * 
      * @param sshClient
      * @throws TransportException
      * @throws IOException 
      */
     public int killClient(SSHClient ssh_client) throws TransportException, IOException { 
         if( getPID() != 0 )
             return SSHCommands.killProgram(this, ssh_client);
         return 1;
     }
     
     /**
      * 
      * @param serverIpAddress
      * @param serverPort
      * @throws WrongIpAddressException
      * @throws WrongPortNumberException 
      */
     public void setServerInfo(String serverIpAddress, Integer serverPort) 
             throws WrongIpAddressException, WrongPortNumberException { 
         this.setServerIpAddress(serverIpAddress);
         this.setServerPort(serverPort);
     }
     
     /**
      * 
      * @param threshold 
      */
     public void setRestartConditionPropThreadsDead(double threshold) { 
         this.restartCond = threshold;
     }
     
     /**
      * 
      * @return 
      */
     public double getRestartConditionPropThreadsDead() { 
         return this.restartCond;
     }
     
     
     /**
      * 
      * @param timeout 
      */
     public void setTimeoutSec(int timeout) { 
         this.timeoutSec = timeout;
     }
     
     /**
      * 
      * @return 
      */
     public int getTimeoutSec() { 
         return this.timeoutSec;
     }
     
     /**
      * 
      * @return 
      */
     public int getLastLogModification() { 
         return this.lastLogAccessSec;
     }
     
     /**
      * 
      * @return 
      */
     public String getConflictsFlag() { 
         return this.conflictsFlag;
     }
     
     /**
      * 
      * @param flag 
      */
     public void setConflictsFlag(String flag) { 
         this.conflictsFlag = flag;
     }
     
     /**
      * 
      * @param diffE 
      */
     public void setDiffE(int diffE) { 
         this.diffE = diffE;
     }
     
     /**
      * 
      * @return 
      */
     public int getDiffE() {
         return this.diffE;
     }
     
     /**
      * 
      * @param diffPperE 
      */
     public void setDiffPperE(int diffPperE) { 
         this.diffPperE = diffPperE;
     }
     
     /**
      * 
      * @return 
      */
     public int getDiffPperE() {
         return this.diffPperE;
     }
     
     /**
      * 
      * @param diffVperE 
      */
     public void setDiffVperP(int diffVperE) { 
         this.diffVperP = diffVperE;
     }
     
     /**
      * 
      * @return 
      */
     public int getDiffVperP() {
         return this.diffVperP;
     }
     
     /**
      * 
      * @param file
      * @param sshclient
      */
     public void uploadProgram(String file, SSHClient ssh_client) 
             throws FileNotFoundException, IOException {
         SSHCommands.createRemoteFolder(this, this.workingDirectory, ssh_client);
         this.uploadFile(file, Utils.getClientProgramRemoteFilename(this), ssh_client);
     }              
 
     /**
      *
      * @return
      */
     public String testsToString() {
         StringBuilder sb = new StringBuilder();
         for(int i=0; i<tests.size(); ++i) {
             sb.append(tests.get(i)).append(" ");
         }
         return sb.toString();
     }
 
     /**
      * Actually delete some empty files that might have been previously used.
      * 
      * @param sshClient
      */
     public void deletePreviousRemoteMessages(SSHClient ssh_client) 
             throws TransportException, IOException { 
         SSHCommands.deleteRemoteFile(this, this.getWorkingDirectory()+"/*"+Utils.CLIENT_REMOTE_FILENAME_SUFFIX_READY_WARMUP, 
                 ssh_client);
         SSHCommands.deleteRemoteFile(this, this.getWorkingDirectory()+"/*"+Utils.CLIENT_REMOTE_FILENAME_SUFFIX_START_SENDING_REQUESTS,
                 ssh_client);
         SSHCommands.deleteRemoteFile(this, this.getWorkingDirectory()+"/*"+Utils.CLIENT_REMOTE_FILENAME_SUFFIX_FINISHED,
                 ssh_client);
     }
     
     /**
      * 
      * @param sshClient
      * @return
      * @throws TransportException
      * @throws IOException 
      */
     public int runClientRemotely(Server server, SSHClient ssh_client)
             throws TransportException, IOException, InterruptedException {
         int r = SSHCommands.startClientProgram(this, server, ssh_client);
         if( r != 0 ) { 
             System.out.println("[ERROR] Client could not be properly started! "
                     + "Exit code: " + r);
             return -1;
         }
        /* KIND OF BAD WORKAROUND .... */
        int pid = -1;
        while( true ) {
            try {
                pid = SSHCommands.getProgramPID(this, ssh_client);
                break;
            } catch( NumberFormatException e ) {
                Thread.sleep(1000);
            }
        }
        /* END */
        this.setPID(pid);
         return 0;
     }
     
     /**
      * 
      * @param sshClient
      * @return
      * @throws TransportException
      * @throws IOException 
      */
     public boolean isProgressing(SSHClient ssh_client) throws TransportException, IOException { 
         lastLogAccessSec = SSHCommands.getTimeSinceLastLogModification(this, 
                 Utils.getClientLogRemoteFilename(this), ssh_client);
         if( lastLogAccessSec > this.timeoutSec ) { 
             return false;
         }
         return true;
     }
     
     /**
      * 
      * @returns a more comprehensible status message 
      */
     public String getStatusMessage() { 
         StringBuilder sb = new StringBuilder();
         sb.append("Client ").append(this.getIpAddress());
         sb.append(":").append(this.getPort()).append("\n\t\tCONNECTION: ");
         if( status_connection == Status.OK )
             sb.append("up and running.");
         else if( status_connection == Status.SSH_CONN_PROBLEMS )
             sb.append("SSH connectivity problems.");
         else 
             sb.append("no connection status known for machine yet.");
         
         sb.append("\n\t\tPROGRAM STATUS: ");
         if( status_process == Status.PID_RUNNING ) 
             sb.append("running with PID " + this.getPID());
         else if ( status_process == Status.PID_NOT_RUNNING ) 
             sb.append("not running yet.");
         else
             sb.append("no info available yet.");
         
         sb.append("\n\t\tSYNCH STATUS: ");
         if( status_synch == Status.READY_WARMUP ) 
             sb.append("ready for warmup.");
         else if( status_synch == Status.RUNNING_REQUESTS ) 
             sb.append("sending requests (including warmup period) ...");
         else 
             sb.append("no info available yet.");
 
         sb.append("\n\t\tTESTS: ");
         sb.append(testsToString());
 
         sb.append("\n\t\tFAULT TOLERANCE: ");
         sb.append(this.getRestartConditionPropThreadsDead()+ " percentage of "
                 + "needed dead clients to restart.");
         
         return sb.toString();
     }
 }
 
 class ClientNotProperlyInitException extends Exception 
 {
     public ClientNotProperlyInitException(String string) {
         super(string);
     }
 }
