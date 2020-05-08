 import java.io.BufferedReader;
 import java.io.IOException;
import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.lang.reflect.InvocationTargetException;
 import java.net.Socket;
 
 
 public class CommSlaveListenThread extends Thread {
     private boolean debug = false;
   
     private Socket socket = null;
     private String receivingcontent = new String();
     private RunningProcessTable rpt;
     private SlaveTable st;
     private Serializer ser;
     
     private PrintWriter out;
     private BufferedReader in;
     private InputStream is;
     private OutputStream os;
     
     public CommSlaveListenThread(Socket socket, RunningProcessTable rpt, SlaveTable st) {
       this.socket = socket;
       this.st = st;
       this.rpt = rpt;
 
     }
 
     public void printDebugInfo(String s){
       if(debug)
         System.out.println("CommSlaveListenThread: " + s);
     }
     
     public void run() {
       printDebugInfo("Slave receving..");
       try{
           is = socket.getInputStream();
           in = new BufferedReader( new InputStreamReader(is));
           
           //read one line from socket
           receivingcontent = in.readLine();
           
           printDebugInfo("received: " + receivingcontent);
           
           os = socket.getOutputStream();
           out = new PrintWriter(os, true);
           if(receivingcontent != null){
             
             if(receivingcontent.equals("Alive?")){
               printDebugInfo("sending current process number: ");
               out.println(rpt.size());
               out.flush();
               
               
               for(MigratableProcess mp: rpt.keySet()){
                 printDebugInfo(rpt.get(mp));
               }
               
 
             }
             else if(receivingcontent.split("&")[0].equals("Process:")){
               
               printDebugInfo("CommSlaveListen: start deserializing");
               
               // TODO: DESERIALIZE()
               ser = new Serializer();
               String processes[] = receivingcontent.split("&");
               for(String process: processes){
                 printDebugInfo("CommSlaveListen: " + process);
               }
               for(int i = 1; i < processes.length; ++i){
                 String serprocess = processes[i];
                 String command = processes[++i];
                 MigratableProcess mp = ser.deserialize(serprocess);
                 printDebugInfo("CommSlaveListen: finish deserializing: " + command);
                 
                 try{
                   ProcessRunner pr = new ProcessRunner(mp, command, rpt);
                   Thread t = new Thread(pr);
                   t.start();
                 }catch (InvocationTargetException e) {
                   System.out.println("");
                   System.out.println("Invalid arguments for input command.");
                 } catch (Exception e) {
                   e.printStackTrace();
                 }
               }
               
             }
             else if(receivingcontent.split(" ")[0].equals("Move:")){
               printDebugInfo("CommSlaveListen: start to serialize..");
               ser = new Serializer();
               String dest[] = receivingcontent.split(" ");
               String destname = dest[1];
               String message = new String("Process:");
               int destport = Integer.parseInt(dest[2]);
               int movenum = Integer.parseInt(dest[3]);
               while(rpt.size() > 0 && movenum > 0){
                 MigratableProcess mp = rpt.getOne();
                 String args = rpt.get(mp);
                 rpt.removeprocess(mp);
                 printDebugInfo("CommSlaveListen: start to serialize.." + args);
                 message += ("&" + (ser.serialize(mp) + "&" + args));
                 System.out.println(message);
                 
                 -- movenum;
               }
               printDebugInfo("CommSlaveListen: " + message);
               CommSender csender = new CommSender(destname, destport, message);
               csender.run();
             }
           }
           out.close();
           os.close();
           in.close();
           is.close();
           socket.close();
     
       } catch (IOException e) {
           e.printStackTrace();
       }
    }
 }
