 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.Socket;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: chenjipan
  * Date: 8/31/13
  * Time: 1:41 PM
  * To change this template use File | Settings | File Templates.
  */
 public class DistributedLog implements Runnable{
     //static String[] addresses = {"127.0.0.1","192.168.1.102"};
 
     //public static void main(String[] args) throws Exception{
     //     frequent(2);
     //}
     int pattern; //0:infrequent 1:frequent
     DistributedLog(int pattern){
        this.pattern = pattern;
     }
     public void run(){
         try{
         if(pattern==0) infrequent();
         else frequent();
         }catch(Exception e){
             e.printStackTrace();
         }
     }
     public void infrequent() throws Exception{
         Thread serverThread = new Thread(new LogServer(Config.port, 0));
         serverThread.start();
         System.out.println("Log server started");
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
         String cmd;
         List<String> msgQueue = Collections.synchronizedList(new LinkedList<String>());
         while(true){
             System.out.println("Enter your command: \n");
             cmd = br.readLine();
             Message msg = new Message("");
             MSGHandler.updateMSG(cmd,msg);
             for(String address: Config.ipAddresses){
                 new Thread(new LogClient(address, Config.port, msg , msgQueue,Config.machineNumMap.get(address))).start();
 
             }
            int threshold;
            System.out.println(msg.commSets.size());
            if(msg.commSets.size()==0) threshold = Config.ipAddresses.length;
            else threshold = msg.commSets.size();
            while(msgQueue.size() < threshold){
                System.out.println(msg.commSets.size());
                System.out.println(msgQueue.size());
                 Thread.sleep(100);
             }
             for(String s: msgQueue){
                 System.out.print(s);
             }
             msgQueue.clear();
         }
     }
     public void frequent() throws Exception{
         Thread serverThread = new Thread(new LogServer(Config.port, 1));
         serverThread.start();
         System.out.println("Log server started");
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
         String cmd;
         List<String> msgQueue = Collections.synchronizedList(new LinkedList<String>());;
 
         Message msg = new Message("");
         HashMap<String, Thread> threadMap = new HashMap<String, Thread>();
         long lastCheckTime = System.currentTimeMillis();
         for(String address: Config.ipAddresses){
             Thread newThread = new Thread(new LogClient2(address, Config.port, msgQueue, msg, Config.machineNumMap.get(address)));
             threadMap.put(address, newThread);
             newThread.start();
 
         }
 
         while(true){
             if(System.currentTimeMillis() - lastCheckTime > Config.checkInterval){
                 lastCheckTime = System.currentTimeMillis();
                 for(String address: Config.ipAddresses){
                     if(!threadMap.get(address).isAlive())
                         threadMap.get(address).start();
                 }
             }
             System.out.println("Enter your command:");
             cmd = br.readLine();
             //msg.setContent(cmd);
             MSGHandler.updateMSG(cmd, msg);
             //System.out.println(msg.valid);
             if(!msg.valid) {
                 System.out.println("Invalid input");
                 continue;
             }
             synchronized (msg){
                 msg.notifyAll();
             }
            int threshold;
            System.out.println(msg.commSets.size());
            if(msg.commSets.size()==0) threshold = Config.ipAddresses.length;
            else threshold = msg.commSets.size();
            while(msgQueue.size() < threshold){
                 Thread.sleep(100);
             }
             for(String s: msgQueue){
                 System.out.print(s);
             }
             msgQueue.clear();
             msg.clear();
 
         }
     }
 }
