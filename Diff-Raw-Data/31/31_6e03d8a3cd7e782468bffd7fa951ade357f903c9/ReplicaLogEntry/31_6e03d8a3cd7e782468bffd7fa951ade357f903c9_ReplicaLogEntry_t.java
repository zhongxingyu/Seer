 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package vrjavareplica;
 
 import java.net.Socket;
 
 /**
  *
  * @author karol
  */
 public class ReplicaLogEntry {
     
     private MessageRequest request;
     private int operationNumber;
     /**
      * Number of replicas that sent PREPAREOK message for this request
      */
     private int commitsNumber;
     private boolean isCommited;
     private Socket clientsSocket;
     
     public ReplicaLogEntry(MessageRequest request, int operationNumber) {
         this.request = request;
         this.operationNumber = operationNumber;
         this.commitsNumber = 0;
         this.isCommited = false;
     }
     
     public ReplicaLogEntry(MessageRequest request, int operationNumber, Socket clientSocket) {
         this.request = request;
         this.operationNumber = operationNumber;
         this.commitsNumber = 0;
         this.isCommited = false;
         this.clientsSocket = clientSocket;
     }
     
     public void increaseCommitsNumber() {
         commitsNumber++;
     }
 
     public MessageRequest getRequest() {
         return request;
     }
 
     public int getOperationNumber() {
         return operationNumber;
     }
 
     public int getCommitsNumber() {
         return commitsNumber;
     }
 
    public boolean isIsCommited() {
         return isCommited;
     }
 
    public void setIsCommited(boolean isCommited) {
         this.isCommited = isCommited;
     }
 
     public Socket getClientsSocket() {
         return clientsSocket;
     }
 
     public void setClientsSocket(Socket clientsSocket) {
         this.clientsSocket = clientsSocket;
     }
     
     
 }
