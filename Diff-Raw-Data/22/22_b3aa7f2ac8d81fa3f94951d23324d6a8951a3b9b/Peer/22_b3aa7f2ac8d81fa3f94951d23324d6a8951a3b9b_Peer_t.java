 package com.hstefan.distrib.t2;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.InetAddress;
 import java.net.MulticastSocket;
 import java.net.UnknownHostException;
 
 /**
  * {@link IPeer} implementation, calls method {@link receiveMessage} whenever a 
  * {@link ReceiverThread} receives a message from the multi-cast group.
  * @author hstefan
  */
 public abstract class Peer implements IPeer {
     private MulticastSocket mMulSocket;
     private InetAddress mGroupAdress;
     private ReceiverThread mReceiverThread;
     
     public static final int MULTICAST_SOCKET_PORT = 6450;
     public static final String GROUP_ADDRESS = "228.5.6.7";
     
     public static final int BUFFER_SIZE = 1024;
 
     
     public Peer() {
         try {
             mGroupAdress = InetAddress.getByName(GROUP_ADDRESS);
             mMulSocket = new MulticastSocket(MULTICAST_SOCKET_PORT);
             mReceiverThread = new ReceiverThread();
         } catch (UnknownHostException ex) {
             //TODO
         } catch (IOException ex) {
             //TODO
         }
     }
 
     public void registraPeer() throws IOException {
         mMulSocket.joinGroup(mGroupAdress);
         mReceiverThread.start();
     }
 
     public void removePeer() throws IOException {
         mMulSocket.leaveGroup(mGroupAdress);
        mReceiverThread.stopThread();
     }
 
     public void broadcastMensagem(String msg) throws IOException {
         DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(),
                 MULTICAST_SOCKET_PORT);
         mMulSocket.send(packet);
     }
     
     public abstract void receiveMessage(DatagramPacket message);
     
     private class ReceiverThread extends Thread {
         public static final int SLEEP_MS = 1000;
        private boolean mContinue;
       
        public void stopThread() {
            mContinue = false;
        }
   
        @Override
        public void start() {
            mContinue = true;
            super.start();
        }
         
         @Override
         public void run() {
            while(!mContinue) {
                 try {
                     byte[] buf = new byte[BUFFER_SIZE];
                     DatagramPacket packet = new DatagramPacket(buf, buf.length);
                     mMulSocket.receive(packet);
                     
                     receiveMessage(packet);
                 } catch (IOException ex) {
                     //TODO
                 }
             }
         }
     }
 }
