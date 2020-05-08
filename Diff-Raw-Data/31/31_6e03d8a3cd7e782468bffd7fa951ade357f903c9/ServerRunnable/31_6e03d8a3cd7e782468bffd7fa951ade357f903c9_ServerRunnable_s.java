 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package vrjavareplica;
 
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.net.Socket;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author karol
  */
 public class ServerRunnable implements Runnable{
 
     protected int replicaID;
     protected Socket clientSocket = null;
     protected MessageProcessor messageProcessor;
 
     public ServerRunnable(int replicaID, Socket clientSocket, MessageProcessor messageProcessor) {
         this.replicaID = replicaID;
         this.clientSocket = clientSocket;
         this.messageProcessor = messageProcessor;
     }
 
     public void run() {
         try {            
             LogWriter.log(replicaID, "Receiving message...");
             DataInputStream dataInput = new DataInputStream(clientSocket.getInputStream());
             int size = dataInput.readInt();
             byte[] requestIDBytes = new byte[size];
             dataInput.read(requestIDBytes, 0, size);
             int messageID = MyByteUtils.byteArrayToInt(requestIDBytes);
             Object objectMessage = receiveMessage(messageID);
             
             messageProcessor.processMessage(messageID, objectMessage, clientSocket);
 //            dataInput.close();
 //            clientSocket.close();
             
         } catch (IOException e) {
             //report exception somewhere.
             e.printStackTrace();
         }
     }
     
    private Object receiveMessage(int messageID) {
         Object object = null;
         switch(messageID) {
             case Constants.REQUEST :
                 object = receiveRequest();
                 break;
             case Constants.PREPARE :
                 object = receivePrepare();
                 break;
             case Constants.PREPAREOK :
             object = receivePrepareOK();
             break;
         }
         return object;
     }
     
     private MessageRequest receiveRequest() {
         DataInputStream dataInput = null;
         MessageRequest request = null;
         try {
             dataInput = new DataInputStream(clientSocket.getInputStream());
             int size = dataInput.readInt();
             byte[] operationIDBytes = new byte[size];
             dataInput.read(operationIDBytes, 0, size);
             size = dataInput.readInt();
             byte[] operationPathBytes = new byte[size];
             dataInput.read(operationPathBytes, 0, size);
             byte[] operationFile = null;
             if(MyByteUtils.byteArrayToInt(operationIDBytes) == 1) {
                 size = dataInput.readInt();
                 if(size != 1) {
                     operationFile = new byte[size];
                     if(operationFile != null) {
                         dataInput.read(operationFile, 0, size);
                     }
                 } else {
                     dataInput.read();
                 }
             }
             size = dataInput.readInt();
             byte[] clientIDBytes = new byte[size];
             dataInput.read(clientIDBytes, 0, size);
             size = dataInput.readInt();
             byte[] requestNumberBytes = new byte[size];
             dataInput.read(requestNumberBytes, 0, size);
             size = dataInput.readInt();
             byte[] viewNumberBytes = new byte[size];
             dataInput.read(viewNumberBytes, 0, size);
             
             
             Operation operation = null;
             int operationID = MyByteUtils.byteArrayToInt(operationIDBytes);
             String path = new String(operationPathBytes, "UTF-8");
             if(path.contains("\n")) {
                 path = path.replaceAll("\n", ".");
             }
             switch(operationID) {
                 case 1 :
                     operation = new Operation(
                             path,
                             operationFile);
                     break;
                 case 2 :
                     operation = new Operation( new String(operationPathBytes));
                     break;
             }
 
             request = new MessageRequest(
                     operation,
                     MyByteUtils.byteArrayToInt(clientIDBytes),
                     MyByteUtils.byteArrayToInt(requestNumberBytes),
                     MyByteUtils.byteArrayToInt(viewNumberBytes));
 
             LogWriter.log(replicaID, "Received request:" + Constants.NEWLINE + request.toString());
         } catch (IOException ex) {
             Logger.getLogger(ServerRunnable.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             return request;
         }
     }
     
     private MessagePrepare receivePrepare() {
         DataInputStream dataInput = null;
         MessagePrepare prepare = null;
         try {
             dataInput = new DataInputStream(clientSocket.getInputStream());
             //receive whole request message
             int size = dataInput.readInt();
             byte[] operationIDBytes = new byte[size];
             dataInput.read(operationIDBytes, 0, size);
             size = dataInput.readInt();
             byte[] operationPathBytes = new byte[size];
             dataInput.read(operationPathBytes, 0, size);
             byte[] operationFile = null;
             if(MyByteUtils.byteArrayToInt(operationIDBytes) == 1) {
                 size = dataInput.readInt();
                 if(size != 1) {
                     operationFile = new byte[size];
                     if(operationFile != null) {
                         dataInput.read(operationFile, 0, size);
                     }
                 } else {
                     dataInput.read();
                 }
             }
             size = dataInput.readInt();
             byte[] clientIDBytes = new byte[size];
             dataInput.read(clientIDBytes, 0, size);
             size = dataInput.readInt();
             byte[] requestNumberBytes = new byte[size];
             dataInput.read(requestNumberBytes, 0, size);
             size = dataInput.readInt();
             byte[] viewNumberBytes = new byte[size];
             dataInput.read(viewNumberBytes, 0, size);
             //receive rest of prepare message
             size = dataInput.readInt();
             byte[] replicaViewNumberBytes = new byte[size];
             dataInput.read(replicaViewNumberBytes, 0, size);
             size = dataInput.readInt();
             byte[] replicaOperationNumberBytes = new byte[size];
             dataInput.read(replicaOperationNumberBytes, 0, size);
             size = dataInput.readInt();
             byte[] replicaLastCommitedBytes = new byte[size];
             dataInput.read(replicaLastCommitedBytes, 0, size);
             
             
             Operation operation = null;
             int operationID = MyByteUtils.byteArrayToInt(operationIDBytes);
             String path = new String(operationPathBytes, "UTF-8");
             if(path.contains("\n")) {
                 path = path.replaceAll("\n", ".");
             }
             
             switch(operationID) {
                 case 1 :
                     operation = new Operation(
                             path,
                             operationFile);
                     break;
                 case 2 :
                     operation = new Operation( new String(operationPathBytes));
                     break;
             }
 
             MessageRequest request = new MessageRequest(
                     operation,
                     MyByteUtils.byteArrayToInt(clientIDBytes),
                     MyByteUtils.byteArrayToInt(requestNumberBytes),
                     MyByteUtils.byteArrayToInt(viewNumberBytes));
             
             prepare = new MessagePrepare(
                     request, 
                     MyByteUtils.byteArrayToInt(replicaViewNumberBytes), 
                     MyByteUtils.byteArrayToInt(replicaOperationNumberBytes), 
                     MyByteUtils.byteArrayToInt(replicaLastCommitedBytes));
             
             LogWriter.log(replicaID, "Received PREPARE:" + Constants.NEWLINE + prepare.toString());
             
         } catch (IOException ex) {
             Logger.getLogger(ServerRunnable.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             return prepare;
         }
     }
     
     private MessagePrepareOK receivePrepareOK() {
         DataInputStream dataInput;
         MessagePrepareOK prepareOK = null;
         try {
             dataInput = new DataInputStream(clientSocket.getInputStream());
             int size = dataInput.readInt();
             byte[] viewNumberBytes = new byte[size];
             dataInput.read(viewNumberBytes, 0, size);
             size = dataInput.readInt();
             byte[] operationNumberBytes = new byte[size];
             dataInput.read(operationNumberBytes, 0, size);
             size = dataInput.readInt();
             byte[] replicaIDBytes = new byte[size];
             dataInput.read(replicaIDBytes, 0, size);
             
             prepareOK = new MessagePrepareOK(
                     MyByteUtils.byteArrayToInt(viewNumberBytes), 
                     MyByteUtils.byteArrayToInt(operationNumberBytes), 
                     MyByteUtils.byteArrayToInt(replicaIDBytes));
             
             LogWriter.log(replicaID, "Received PREPAREOK:" + Constants.NEWLINE + prepareOK.toString());
             
         } catch (IOException ex) {
             Logger.getLogger(ServerRunnable.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             return prepareOK;
         }
     }
 }
