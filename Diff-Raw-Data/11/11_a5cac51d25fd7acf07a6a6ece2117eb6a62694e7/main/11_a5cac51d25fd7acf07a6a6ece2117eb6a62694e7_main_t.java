 package ueb06.a2;
/**
 import utils.Utils;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Scanner;
 import java.util.concurrent.*;
 

  * User: Julian
  * Date: 04.12.13
  * Time: 19:34

 public class main {
 
     public static String downloadContents(URL url) throws IOException {
         try(InputStream input = url.openStream()) {
             Scanner s = new Scanner(input);
             StringBuilder b = new StringBuilder();
             while (s.hasNext()){
                 b.append(s.next());
             }
             return b.toString();
         }
     }
     static ExecutorService pool;
     public static Future<String> startDownloading(final URL url){
         return pool.submit(new Callable<String>(){
             @Override
             public String call() throws Exception {
                 try(InputStream input = url.openStream()) {
                     Scanner s = new Scanner(input);
                     StringBuilder b = new StringBuilder();
                     while (s.hasNext()){
                         b.append(s.next());
                     }
                     return b.toString();
                 }
             }
         });
     }
 
     public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
 
         Utils.stopwatchStart();
         final String contents = downloadContents(new URL("http://www.example.com"));
         Utils.stopwatchEnd();
         // ~ 277 millis..
 
         pool = Executors.newCachedThreadPool();
 
         Utils.stopwatchStart();
         final Future<String> contentsFuture = startDownloading(new URL("http://www.example.com"));
         final String contentsFromFuture = contentsFuture.get(); // blocks
         Utils.stopwatchEnd();
 
         System.out.println(contentsFromFuture);
 
         Server server = null;
 
         server.fork(4711);
         final String result = server.op().get();
 
 
         pool.shutdown();
 
 
     }
 }
 */
