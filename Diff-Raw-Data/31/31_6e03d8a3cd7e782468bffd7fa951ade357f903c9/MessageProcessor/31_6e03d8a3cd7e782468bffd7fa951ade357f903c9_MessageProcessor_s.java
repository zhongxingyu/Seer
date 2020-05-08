 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package vrjavareplica;
 
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.Socket;
import java.sql.PreparedStatement;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author karol
  */
 public class MessageProcessor {
     
     Replica replica;
     
     public MessageProcessor(Replica replica) {
         this.replica = replica;
     }
     
    public void processMessage(int messageID, Object message, Socket clientSocket) {
         switch(messageID) {
             case Constants.REQUEST : 
                 MessageRequest request = (MessageRequest) message;
                 processRequest(request, clientSocket);
                 break;
             case Constants.PREPARE : 
                 MessagePrepare prepare = (MessagePrepare) message;
                 processPrepare(prepare);
                 break;
             case Constants.PREPAREOK : 
                 MessagePrepareOK prepareOK = (MessagePrepareOK) message;
                 processPrepareOK2(prepareOK);
                 break;
         }
     }
     
     private void processRequest(MessageRequest request, Socket clientSocket) {
         LogWriter.log(replica.getReplicaID(), "Processing REQUEST...");
         
         replica.incrementOpNumber();
         
         replica.getLog().addLast(new ReplicaLogEntry(request, replica.getOpNumber(), clientSocket));
         MessagePrepare prepare = new MessagePrepare(
                 request,
                 replica.getViewNumber(),
                 replica.getOpNumber(),
                 replica.getLastCommited()
                 );
         sendMessage(prepare);        
     }
     
     private void processPrepare(MessagePrepare prepare) {
         try {
             Thread.sleep(1000*(long)Math.random()*15);
         } catch (InterruptedException ex) {
             Logger.getLogger(MessageProcessor.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         LogWriter.log(replica.getReplicaID(), "Processing PREPARE...");
         
         replica.getLog().addLast(new ReplicaLogEntry(prepare.getRequest(), prepare.getOperationNumber()));
         MessagePrepareOK prepareOK = new MessagePrepareOK(
                 replica.getViewNumber(), 
                 prepare.getOperationNumber(), 
                 replica.getReplicaID());
         sendMessagePrepareOK(prepareOK);
         
         processLastCommited(prepare.getLastCommited());
     }
     
     private void processPrepareOK2(MessagePrepareOK prepareOK) {
         LogWriter.log(replica.getReplicaID(), "Processing PREPAREOK...");
         ReplicaLogEntry entry = replica.getLog().findEntry(prepareOK.getOperationNumber());
         if(entry == null) {
             //this is late prepareOK, not needed
             LogWriter.log(replica.getReplicaID(), "PREPAREOK - no such entry!");
             return;
         } else {
             entry.increaseCommitsNumber();
             LogWriter.log(replica.getReplicaID(), 
                     "Operation " + entry.getOperationNumber() + " commits " 
                     + entry.getCommitsNumber() + "/" + (replica.getReplicaTable().size() - 1));
             if(isCommitsSufficient(entry)) {
                 if(isFirstInLog(entry)) {
                     replica.executeRequest(entry);
                     replica.setLastCommited(prepareOK.getOperationNumber());
                 } else {
                     entry.setIsCommited(true);
                 }
             }
         }
     }
     
 //    private void processPrepareOK(MessagePrepareOK prepareOK) {
 //        LogWriter.log(replica.getReplicaID(), "Processing PREPAREOK...");
 //        ReplicaLogEntry entry = replica.getLog().findEntry(prepareOK.getOperationNumber());
 //        if(entry == null) {
 //            return;
 //        } else {
 //            entry.increaseCommitsNumber();
 //            LogWriter.log(replica.getReplicaID(), 
 //                    "Operation " + entry.getOperationNumber() + " commits " 
 //                    + entry.getCommitsNumber() + "/" + replica.getReplicaTable().size());
 ////            double treshold = entry.getCommitsNumber()/replica.getReplicaTable().size();
 ////            if(treshold > 0.5) {
 //                if(entry.isIsExecuted() == false) {
 //                    boolean canBeExecuted = isCommitsSufficient(replica.getLog().findEntry(prepareOK.getOperationNumber()));
 //                    if(canBeExecuted) {
 //                        replica.executeRequest(entry);
 //                        replica.setLastCommited(prepareOK.getOperationNumber());
 //                        entry.setIsExecuted(true);
 //                    }
 //                }
 ////            }
 //        }
 //        
 //    }
     
     private void processLastCommited(int lastCommited) {
         if(lastCommited > 0) {
             if(!replica.getIpAddress().equals(replica.getPrimary().getIpAddress()) 
            && replica.getPort() != replica.getPrimary().getPort()) {
                 ReplicaLogEntry entry = replica.getLog().findEntry(lastCommited);
                 if(isFirstInLog(entry)) {
                     replica.executeRequest(entry);
                 } else {
                     entry.setIsCommited(true);
                 }
             }
         }
     }
     
     public void sendMessage(MessageReply reply, Socket clientSocket) {
         DataOutputStream dataOutput = null;
         try {
             LogWriter.log(replica.getReplicaID(), "Sending message:" + Constants.NEWLINE + reply.toString());
             
             dataOutput = new DataOutputStream(clientSocket.getOutputStream());
             byte[] messageIDBytes = MyByteUtils.toByteArray(reply.getMessageID());
             dataOutput.writeInt(messageIDBytes.length);
             dataOutput.write(messageIDBytes);
             byte[] viewNumberBytes = MyByteUtils.toByteArray(reply.getViewNumber());
             dataOutput.writeInt(viewNumberBytes.length);
             dataOutput.write(viewNumberBytes);
             byte[] requestNumberBytes = MyByteUtils.toByteArray(reply.getRequestNumber());
             dataOutput.writeInt(requestNumberBytes.length);
             dataOutput.write(requestNumberBytes);
             byte[] resultBytes = MyByteUtils.toByteArray(reply.getResult());
             dataOutput.writeInt(resultBytes.length);
             dataOutput.write(resultBytes);
             
         } catch (IOException ex) {
             Logger.getLogger(ServerRunnable.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             try {
                 if(dataOutput != null) {
                     dataOutput.close();
                 }
             } catch (IOException ex) {
                 Logger.getLogger(ServerRunnable.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
     
     private void sendMessage(MessagePrepare prepare) {
         for(int i = 0; i < replica.getReplicaTable().size(); i++) {
             if(i+1 != replica.getReplicaID()) { //not sending to primary
                 new Thread(new ReplicaClientRunnable(
                         replica,
                         replica.getReplicaTable().get(i).getIpAddress(), 
                         replica.getReplicaTable().get(i).getPort(), 
                         Constants.PREPARE, 
                         prepare)
                     ).start();
             }
         }
     }
     
     private void sendMessagePrepareOK(MessagePrepareOK prepareOK) {
         new Thread(new ReplicaClientRunnable(
                 replica,
                 replica.getPrimary().getIpAddress(), 
                 replica.getPrimary().getPort(), 
                 Constants.PREPAREOK, 
                 prepareOK)
             ).start();
     }
     
     private boolean isCommitsSufficient( ReplicaLogEntry entry ) {
         if(entry.isIsCommited()) {
             return true;
         } else {
             //check if number of commits is sufficient
             boolean isCommitsSufficient = (entry.getCommitsNumber()+1) > (replica.getReplicaTable().size()/2);
             if(isCommitsSufficient) {
                 entry.setIsCommited(true);
             }
             return isCommitsSufficient;
         }
     }
     
     private boolean isFirstInLog(ReplicaLogEntry entry) {
         if(entry == null) {
             return false;
         } else {
             boolean isFirst = entry.equals(replica.getLog().getFirst());
             return isFirst;
         }
     }
 }
