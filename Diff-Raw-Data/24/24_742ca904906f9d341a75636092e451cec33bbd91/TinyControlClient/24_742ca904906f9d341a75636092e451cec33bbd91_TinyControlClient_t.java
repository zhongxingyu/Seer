 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package tinycontrolclient;
 
 import java.net.*;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.util.ArrayList;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 
 /**
  *
  * @author guilherme
  */
 public class TinyControlClient {
 
     static final int NDUPACK = 3;
     static final int SMAX = 2^32;
     
     static History history;
     static double lossRate;
     static double recRate;
     static int rtt;
     static long recTime;
     static boolean noRecFlag;
     static boolean noSentFlag;
     static ScheduledThreadPoolExecutor fbTimer;
     
     static DatagramSocket clientSocket;
     static InetAddress IPAddress;
     static int port;
     
     static class ExpireTimer implements Runnable {
         @Override
         public void run() {
             if(!noRecFlag) {
                 lossRate = calcLossRate();
                 recRate = calcRecRate();
                 try {sendFbPkt(); }
                 catch (Exception e) {};
                 noSentFlag = false;
             }
             else noSentFlag = true;
             fbTimer.schedule(new ExpireTimer(), rtt, TimeUnit.SECONDS);
         }
     }
     
     /**
      * @param args the command line arguments
      */
     
     public static void main(String[] args)  throws Exception{
         
         clientSocket = new DatagramSocket();
         IPAddress = InetAddress.getByName("localhost");
         port = 54321;
         
         byte[] receiveData = new byte[1012];
         byte[] sendData = new byte[16];
         history = new History();
         
         fbTimer = new ScheduledThreadPoolExecutor(1);
         
         //Connection
         //clientSocket.connect(IPAddress, port);
         DatagramPacket connPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
         clientSocket.send(connPacket);
 
         
         boolean firstPkt = true;
         while(true) {
             //Receive
             DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
             clientSocket.receive(receivePacket);
                         
             //Process
             recTime = System.nanoTime();
             noRecFlag = false;
             byte[] bytes = receivePacket.getData();
             ByteBuffer bb = ByteBuffer.wrap(bytes);
             bb.order(ByteOrder.LITTLE_ENDIAN);
             int seqNum = bb.getInt();
             int tStamp = bb.getInt();
             rtt = bb.getInt();
            if(rtt<0) rtt=2;
 
             if(firstPkt) {
                history.setCurrentSeq(seqNum);
                history.setCurrentTimeStamp(tStamp);
                 lossRate = 0;
                 recRate = 0;
                 sendFbPkt();
                 fbTimer.schedule(new ExpireTimer(), rtt, TimeUnit.SECONDS);
                 firstPkt = false;
             }
             
             else {
                if((!updateHistory(seqNum, tStamp)) && (!noSentFlag))
                     continue;
 
                if(lossRate < (lossRate = calcLossRate())) {
                     //Antecipates feedback timer expiration
                     fbTimer.remove(fbTimer.getQueue().peek());
                     fbTimer.schedule(new ExpireTimer(), 0, TimeUnit.NANOSECONDS);
                 }
             }
             
         }
     }
     
     private static boolean updateHistory(int seqNum, int tStamp) {
         
         boolean lossEvent = false;
         
         //Arrival of the expected pkt (no loss)
        if(seqNum-history.getCurrentSeq() == 1) {
             history.setCurrentSeq(seqNum);
            history.setCurrentTimeStamp(tStamp);
            //System.out.println(seqNum);
        }
            
         //Pkt out of sequence
         else if(seqNum-history.getCurrentSeq() > 1) {
             for(int i=history.getCurrentSeq()+1; i<seqNum; i++) {
                 history.addEntry(i, history.getCurrentSeq(), seqNum, 
                         history.getCurrentTimeStamp(), tStamp);
                 if(lossTime(history.getLossStarterEntry()) + rtt 
                         >= lossTime(history.getLastEntry())) {
                     history.addInterval(history.getLastEntry().getSeq());
                     lossEvent = true;
                 }
             }
             history.setCurrentSeq(seqNum);
            history.setCurrentTimeStamp(tStamp);
         }
 
         //Previously lost pkt
         else {
             history.removeEntry(seqNum);
         }
         
         return lossEvent;
     }
     
     private static int lossTime(HistoryEntry h) {
         return h.gettBefore() + ((h.gettAfter()-h.gettBefore()) * 
                 distance(h.getSeq(), h.getPrev()) / distance(h.getNext(), h.getPrev()));
     }
     
     private static int distance(int sa, int sb) {
         return (sa + SMAX -sb) % SMAX;
     }
     
     private static double calcLossRate() {
         double weights[] = {1.0, 1.0, 1.0, 1.0, 0.8, 0.6, 0.4};
         double i_tot = 0.0;
         double w_tot = 0.0;
         ArrayList<Integer> intervals = history.getIntervals();
         
         if(intervals.isEmpty()) return i_tot;
         
         for(int i=0; i<intervals.size() && i<8; i++) {
             
             w_tot += weights[i];
             if(i==intervals.size()-1)
                 i_tot += weights[i]*(history.getCurrentSeq()-intervals.get(i)+1);
             else
                 i_tot += weights[i]*(intervals.get(i+1)-intervals.get(i));
         }
         
         return (w_tot/i_tot);
     }
     
     private static double calcRecRate() {
         return 1.2;
     }
     
     private static void sendFbPkt() throws Exception{
         
         byte[] sendData = new byte[16];
         ByteBuffer bb = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);  
         bb.putInt(history.getCurrentTimeStamp());
         bb.putInt((int)(System.nanoTime()-recTime));
         bb.putFloat((float)recRate);
         bb.putFloat((float)lossRate);
         sendData = bb.array();
        System.out.println(history.getCurrentSeq() + " " + recRate + " " + lossRate);
 
         //Feedback
         DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
         clientSocket.send(sendPacket);
         noRecFlag = true;
     }
 }
