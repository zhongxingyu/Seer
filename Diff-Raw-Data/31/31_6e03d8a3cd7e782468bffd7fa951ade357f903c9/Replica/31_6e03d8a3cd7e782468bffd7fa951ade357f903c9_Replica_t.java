 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package vrjavareplica;
 
 import java.util.ArrayList;
 import java.util.NoSuchElementException;
 
 /**
  *
  * @author karol
  */
 public class Replica {
 
     public int getLastCommited() {
         return lastCommited;
     }
 
     public void setLastCommited(int lastCommited) {
         this.lastCommited = lastCommited;
     }
     
     public enum ReplicaState { Normal, ViewChange, Recovering };
     private static final String PARAMETER_FILE_NAME = "Parameters.txt";
     private static final String HOSTS_FILE_NAME = "Hosts.txt";
     private static final String NEWLINE = System.getProperty("line.separator");
     
     
     private int replicaID;
     private String ipAddress;
     private int port;
     private ReplicaInfo primary;
     private ReplicaTable replicaTable;
     private int viewNumber;
     private ReplicaState state;
     private int opNumber;
     private ClientTable clientTable;
     private ReplicaLog log;
     private int lastCommited;
     private MessageProcessor messageProcessor;
     
     public Replica() {
         replicaTable = new ReplicaTable();
         clientTable = new ClientTable();
         loadParameters();
         loadHosts();
         primary = replicaTable.get(0);
         viewNumber = 1;
         opNumber = 0;
         log = new ReplicaLog();
         lastCommited = 0;
         System.out.println(identify());
         
         messageProcessor = new MessageProcessor(this);
         VRCode vrCode = new VRCode(replicaID, port, messageProcessor);
         new Thread(vrCode).start();
     }
     
     public Replica(int id, String address, int port) {
         this.replicaID = id;
         this.ipAddress = address;
         this.port = port;
         replicaTable = new ReplicaTable();
         clientTable = new ClientTable();
         loadHosts();
         primary = replicaTable.get(0);
         viewNumber = 1;
         opNumber = 0;
         log = new ReplicaLog();
         lastCommited = 0;
         System.out.println(identify());
         
         messageProcessor = new MessageProcessor(this);
         VRCode vrCode = new VRCode(replicaID, port, messageProcessor);
         new Thread(vrCode).start();
     }
     
     public String identify() {
         String s = "";
         s += "Replica App" + NEWLINE;
         s += "ID: " + replicaID + NEWLINE;
         s += "ipAddress: " + ipAddress + NEWLINE;
         s += "port: " + port + NEWLINE;
         s += "Hosts: " + NEWLINE;
         for(ReplicaInfo rep : replicaTable) {
             s += "Replica[" + rep.getReplicaID() + "] " + rep.getIpAddress() + ":" + rep.getPort() + NEWLINE;
         }
         return s;
     }
     
     public void incrementOpNumber() {
         opNumber++;
     }
             
     private boolean loadParameters() {
         ArrayList<ArrayList<String>> tokenizedLines = MyFileUtils.loadFile(PARAMETER_FILE_NAME);
         if(tokenizedLines.size() >= 3) {
             int repID = Integer.valueOf(tokenizedLines.get(0).get(0));
             String address = tokenizedLines.get(1).get(0);
             int portNumber = Integer.valueOf(tokenizedLines.get(2).get(0));
             this.replicaID = repID;
             this.ipAddress = address;
             this.port = portNumber;
             return true;
         } else {
             return false;
         }
     }
     
     public boolean  testLoadParameters() {
         return loadParameters();
     }
     
     private boolean loadHosts() {
         ArrayList<ArrayList<String>> tokenizedLines = MyFileUtils.loadFile(HOSTS_FILE_NAME);
         if(tokenizedLines.size() > 0) {
             for(int i = 0; i < tokenizedLines.size(); i++) {
                 ArrayList<String> tokens = tokenizedLines.get(i);
                 if(tokens.size() >= 2) {
                     int replicaID = i+1;
                     String address = tokens.get(0);                
                     int port = Integer.valueOf(tokens.get(1));
                     replicaTable.add(new ReplicaInfo(replicaID, address, port));
                 }
             }
         } 
         boolean result = (replicaTable.size() > 0) ? true : false;
         return result;
     }
     
     public boolean  testloadHosts() {
         return loadHosts();
     }
     
     public int getReplicaID() {
         return replicaID;
     }
 
     public void setReplicaID(int replicaID) {
         this.replicaID = replicaID;
     }
 
     public String getIpAddress() {
         return ipAddress;
     }
 
     public void setIpAddress(String ipAddress) {
         this.ipAddress = ipAddress;
     }
 
     public ReplicaInfo getPrimary() {
         return primary;
     }
 
     public void setPrimary(ReplicaInfo primary) {
         this.primary = primary;
     }
 
     public ReplicaTable getReplicaTable() {
         return replicaTable;
     }
 
     public void setReplicaTable(ReplicaTable replicaTable) {
         this.replicaTable = replicaTable;
     }
 
     public int getViewNumber() {
         return viewNumber;
     }
 
     public void setViewNumber(int viewNumber) {
         this.viewNumber = viewNumber;
     }
 
     public ReplicaState getState() {
         return state;
     }
 
     public void setState(ReplicaState state) {
         this.state = state;
     }
 
     public int getOpNumber() {
         return opNumber;
     }
 
     public void setOpNumber(int opNumber) {
         this.opNumber = opNumber;
     }
 
     public ClientTable getClientTable() {
         return clientTable;
     }
 
     public void setClientTable(ClientTable clientTable) {
         this.clientTable = clientTable;
     }
 
     public ReplicaLog getLog() {
         return log;
     }
 
     public void setLog(ReplicaLog log) {
         this.log = log;
     }
 
     public int getPort() {
         return port;
     }
 
     public void setPort(int port) {
         this.port = port;
     }
     
    public synchronized boolean executeRequest(ReplicaLogEntry entry) {
         int operation = entry.getRequest().getOperation().getOperationID();
         boolean result = false;
         switch(operation) {
             case Constants.COPY : 
                 result = executeCopy(entry.getRequest().getOperation());
                 break;
             case Constants.DELETE : 
                 result = executeDelete(entry.getRequest().getOperation());
                 break;
         }
         
         if(result) {
             try {
                 ReplicaLogEntry firstEntry = log.peek(); //retrieves but doesn't remove first element in the list
                 if(entry.equals(firstEntry)) {
                     log.removeFirst();
                 }
                 if(checkIsPrimary()) {
                     MessageReply reply = new MessageReply(this.getViewNumber(), entry.getRequest().getRequestNumber(), result);
                     messageProcessor.sendMessage(reply, entry.getClientsSocket());
                 }
                 ReplicaLogEntry nextEntry = log.peek(); //retrieves but doesn't remove first element in the list
                 if(nextEntry != null && nextEntry.isIsCommited()) {
                     executeRequest(nextEntry);
                 }
             } catch (NoSuchElementException e) {
                 e.printStackTrace();
             }
         }
         return result;
     }
     
     private boolean executeCopy(Operation operation) {
         String path = replicaID + "-" + operation.getPath();
         boolean result = MyFileUtils.saveFile(path, operation.getFile());
         LogWriter.log(replicaID, "Executed copy file " + operation.getPath() + " with result = " + result);
         return result;
     }
     
     private boolean executeDelete(Operation operation) {
         String path = replicaID + "-" + operation.getPath();
         boolean result = MyFileUtils.deleteFile(path);
         LogWriter.log(replicaID, "Executed delete file " + operation.getPath() + " with result = " + result);
         return result;
     }
     
     public boolean checkIsPrimary() {
         if(this.ipAddress.equals(this.primary.getIpAddress()) && this.port == this.primary.getPort()) {
             return true;
         } else {
             return false;
         }
     }
 }
