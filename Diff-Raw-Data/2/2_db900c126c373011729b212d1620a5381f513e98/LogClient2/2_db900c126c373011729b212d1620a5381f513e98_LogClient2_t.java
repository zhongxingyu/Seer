 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.List;
 import java.util.concurrent.Semaphore;
 
 /**
  * Created with IntelliJ IDEA.
  * User: chenjipan
  * Date: 8/31/13
  * Time: 5:22 PM
  * To change this template use File | Settings | File Templates.
  */
 public class LogClient2 extends LogClient {
    //Message msg;
     LogClient2(String address, int port, List<String> msgQueue, Message msg, int num){
         super(address, port, msg, msgQueue, num);
         //this.msg = msg;
     }
     @Override
     public void run(){
         try{
             this.s = new Socket(address, port);
             this.in =
                     new BufferedReader(new InputStreamReader(s.getInputStream()));
             this.out = new PrintWriter(s.getOutputStream(), true);
             String nextline;
             StringBuilder msgResult;
             while(s.isConnected()){
                 synchronized (msg){
                 msg.wait();
                 //System.out.println(this.machineNum);
                 //System.out.println(msg.getContent());
                 if(!msg.inSet(this.machineNum)) continue;
                 this.out.println(msg.getContent());
                 this.out.println("MSG_END");
                 msgResult = new StringBuilder();
                 while (!(nextline = this.in.readLine()).equals("MSG_END")) {
                     msgResult.append(nextline);
                     msgResult.append("\n");
                 }
 
                 this.msgQueue.add(msgResult.toString());
                 msgResult = new StringBuilder();
                 }
             }
             System.err.format("LogClient2: Remote Server: %s Diconnected",this.s.getRemoteSocketAddress());
             this.out.close();
             this.in.close();
             this.s.close();
 
         }catch (Exception e){
             this.msgQueue.add(address+": DISCONNECTED ERROR" );
             e.printStackTrace();
         }
     }
 }
