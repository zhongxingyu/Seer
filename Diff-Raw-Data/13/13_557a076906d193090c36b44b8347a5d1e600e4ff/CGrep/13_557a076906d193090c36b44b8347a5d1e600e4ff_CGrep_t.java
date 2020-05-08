 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 /**
  * @author Yin
  * @author sst8696
  * @author peter
  */
 public class CGrep {
 
     private static final int THREADPOOLAMOUNT = 3;
 
     /**
      * Main method
      * 
      * @param args
      *            - Input arguments
      */
     public static void main(String[] args) {
 
        if (args.length >= 1) {
             ExecutorService executor = Executors
                     .newFixedThreadPool(THREADPOOLAMOUNT);
 
             String pattern = args[0].toString();
             for (int i = 1; i < args.length; i++) {
                 String argument = args[i].toString();
                 File file = new File(argument);
 
                 Callable<Found> searcher = null;
                 Future<Found> result = null;
 
                 if (file.exists()) {
                     try {
                        searcher = new FileSearch(file, pattern);
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                     result = executor.submit(searcher);
                 } else {
                     InputStream input = System.in;
                     searcher = new FileSearch(input, pattern);
                     result = executor.submit(searcher);
                 }
 
                 try {
                     System.out.println(result.get());
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 } catch (ExecutionException e) {
                     e.printStackTrace();
                 }
             }
 
             executor.shutdown();
         }
     }
 }
