 package com.jckimble.jsonprotocol;
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 /**
  *
  * @author jckimble
  */
 public class JSONServer extends Thread implements Closeable {
     protected int PORT;
     protected JSONCallback callback;
     protected int THREADS=1000;
     protected ExecutorService thread=null;
     protected ServerSocket ss=null;
 
     public JSONServer(int port){
         PORT=port;
     }
     public void setCallBack(JSONCallback callback){
         this.callback=callback;
     }
     public void setThreadCount(int threads){
         this.THREADS=threads;
     }
     @Override
     public void run(){
         try {
             ss=new ServerSocket(PORT);
             while(!ss.isClosed()){
                 JSONClient client=new JSONClient(ss.accept());
                 client.setCallBack(callback);
                 if(THREADS==0)
                    new Thread(client).start();
                 else if(THREADS==1)
                     client.run();
                 else{
                     if(thread==null)
                         thread=Executors.newFixedThreadPool(THREADS);
                     thread.execute(client);
                 }
             }
         } catch (IOException ex) {
             callback.JSONError(null, ex);
         } finally {
             try {
                 if(ss != null)
                     ss.close();
             } catch (IOException ex) {
                 callback.JSONError(null, ex);
             }
         }
     }
 
     public void close() {
         try {
             if(thread != null){
                 thread.shutdown();
                 thread=null;
             }
             if(ss != null){
                 ss.close();
                 ss=null;
             }
         } catch (IOException ex) {
             callback.JSONError(null, ex);
         }
     }
 }
