 package org.mathonasphere;
 
 import java.util.Queue;
 import java.util.concurrent.ArrayBlockingQueue;
 //import java.lang.Thread;
 import java.io.BufferedReader;
 import java.io.FileOutputStream;
 
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.codec.binary.Base64;
 
 
 public class StubServlet extends HttpServlet {
   int counter = 0;
   int TOTAL_FRAMES = 100;
   SOSConnection sos;
   ArrayBlockingQueue<byte[]> dataQueue;
 
   public void init() {
       sos = new SOSConnection("localhost", 2468);
       dataQueue = new ArrayBlockingQueue(10,true);
       //sos.connect();
       //System.out.println(sos.sendCommand("enable"));
       //System.out.println(sos.sendCommand("play images"));
       Thread t = new Thread() 
           {
               int lastFN = 0;
               public void run() 
               {
                   while(true)
                   {
                       try
                       {
                           int fn = 0;
                           // int fn = sos.sendCommand("get_frame_number");
                           if(fn == lastFN)
                           {
                               FileOutputStream fos = null;
                              fos = new FileOutputStream(System.getenv("MOS_IMAGES")+"/"+counter+".png");
                              byte[] data = null;
                              if((data = dataQueue.peek()) != null)
                               {
                                  fos.write(data);
                                  if(dataQueue.size() > 1)
                                  {
                                      dataQueue.take();
                                  }
                               }
                              fos.close();
                              
                           }
                           counter = (counter == TOTAL_FRAMES) ? 0 : counter+1;
                           sleep(100);
                       }
                       catch(Exception e) 
                       {
                          e.printStackTrace();
                       }
                   }
               }
           };
 
       try
       {
           t.start();
       }
       catch(Exception e)
       {
           System.out.println("Failed to start thread.");
       }
   }
 
   public void service(ServletRequest req, ServletResponse res) {
     HttpServletRequest hreq = (HttpServletRequest) req;
     System.out.println(hreq.getMethod() + ": " + req.getContentLength());
     if (hreq.getMethod() == "POST") {
         // sos.sendCommand("get_frame_number");
       decodePostRequest(req);
     }
   }
 
   private void decodePostRequest(ServletRequest req) {
       //FileOutputStream fos = null;
     BufferedReader br = null;
     try {
       br = req.getReader();
       String header = br.readLine();
       String body = br.readLine();
       byte[] bytes = Base64.decodeBase64(body);
       dataQueue.offer(bytes);
       //      fos = new FileOutputStream("tomcat/webapps/StubClient-0.1/output_"+counter+".png");
       //fos.write(bytes);
     }
     catch (Exception e) {
       e.printStackTrace(System.err);
     }
     finally {
       try {
         br.close();
       }
       catch (Exception e) {
           e.printStackTrace(System.err);
       }
     }
   }
 }
