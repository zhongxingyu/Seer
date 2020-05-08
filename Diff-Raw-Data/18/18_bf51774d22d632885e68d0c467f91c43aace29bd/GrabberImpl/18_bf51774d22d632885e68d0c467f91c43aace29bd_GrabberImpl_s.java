 package edu.sl.grabalyze.grabber;
 
 import edu.sl.grabalyze.execution.LoadController;
 import edu.sl.grabalyze.grabber.strategy.GrabberStrategy;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.text.DecimalFormat;
 
 public class GrabberImpl implements Runnable {
 
     private GrabberStrategy strategy;
     private int id;
 
     private final DecimalFormat df = new DecimalFormat("#.##");
 
     public void setStrategy(GrabberStrategy strategy) {
         this.strategy = strategy;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public void run() {
         int errors = 0;
         while (strategy.hasUrl()) {
             LoadController.get().takeControl();
             String urlInput = strategy.nextUrl();
             try {
                 String html = requestHttp(urlInput);
                 try {
                     strategy.processHtml(html);
                     System.out.println("Grabber #" + id + " : " + df.format(strategy.getProgress() * 100) + "%");
                 } catch (Exception ex) {
                     System.err.println("Error in " + urlInput);
                     System.err.println(ex);
                     ex.printStackTrace();
                     errors++;
                 }
             } catch (Exception e) {
                System.err.println("Error in :" + urlInput);
                 System.err.println(e);
                 errors++;
                 try {
                     Thread.sleep(1000);
                 } catch (InterruptedException e1) {
                     e1.printStackTrace();
                 }
             }
         }
         System.out.println("Grabber #" + id + " : done! (with " + errors + " errors)");
     }
 
     private String requestHttp(String urlInput) throws IOException {
         //System.out.println("Processing " + urlInput);
         URL url;
         HttpURLConnection conn;
         BufferedReader rd;
         String line;
         String result = "";
         url = new URL(urlInput);
         conn = (HttpURLConnection) url.openConnection();
             conn.setRequestMethod("GET");
             rd = new BufferedReader(
                     new InputStreamReader(conn.getInputStream(), "UTF8"));
             while ((line = rd.readLine()) != null) {
                 result += line;
             }
             rd.close();
         
         return result;
     }
 }
