 package se.umu.cs.jsgajn.gcom.groupcommunication;
 
 import java.io.Serializable;
 import java.rmi.AlreadyBoundException;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.util.concurrent.BlockingQueue;
 
 public class ReceiverImpl implements Receiver, Serializable {
    private static final long serialVersionUID = 1L;
     // This will not be sent when object is serialized
     private transient BlockingQueue<Message> q;
 
     public ReceiverImpl(BlockingQueue<Message> q) 
     throws RemoteException, AlreadyBoundException, NotBoundException{
         this.q = q;
     } 
 
     public void receive(Message m) throws RemoteException {
         // Simply add message to blockingQueue, if queue is busy, it will block.
         try {
             q.put(m);
         } catch (InterruptedException e) {
             // If queue is interrupted while waiting for insertion into queue.
             // TODO: How shall we handle this?
             e.printStackTrace();
         }
     }
 }
