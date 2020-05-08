 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package gpresenter;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.InputStreamReader;
 import javafx.async.RunnableFuture;
 
 /**
  *
  * @author kent
  */
 public class TimerTask implements RunnableFuture{
 
     public boolean finished = false;
     private FXListener listener;
     private long statTime;
     public  TimerTask(long startTime, FXListener listener){
         this.listener = listener;
         this.statTime = startTime;
     }
 
     @Override
     public void run() throws Exception {
         while(!finished){
             long diff = System.currentTimeMillis() - statTime;
             diff = diff /1000;
             long seconds = diff % 60;
             diff = diff / 60;
             long minutes = diff % 60;
             long hours = diff / 60;
             StringBuilder sb = new StringBuilder();
            sb.append(hours).append(":").append(minutes).append(":").append(seconds);
             listener.callback(sb.toString());
             Thread.sleep(100);
             
         }
 
     }
 
 
 
     public void stop(){
         finished = true;
     }
 }
