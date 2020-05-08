 package se.umu.cs.jsgajn.gcom.groupcommunication;
 
 import java.rmi.RemoteException;
 
 import se.umu.cs.jsgajn.gcom.groupmanagement.GroupView;
 
 public class BasicMulticast implements Multicast {
 
     /**
      * Send message to all members of the group.
      * TODO: move method to interface or Ordering-layer.
      * @param m The message to send.
      */
     public void multicast(Message m, GroupView g) {
         for (Receiver member : g) {
             try {
                 member.receive(m);
             } catch (RemoteException e) {
                 // TODO Auto-generated catch block
                e.printStackTrace();
             }
         }
     }
 
     public boolean deliverCheck(Message m, GroupView g) {
         // TODO Auto-generated method stub
         return true;
     }
 }
