 package deeva;
 
import java.io.OutputStream;
import java.io.PrintStream;
 import java.util.concurrent.BlockingQueue;
 
 class StdInRedirectThread extends Thread {
     private final BlockingQueue<String> resQueue;
     private final PrintStream out;
 
     StdInRedirectThread(String name, OutputStream os,
                         BlockingQueue<String> resQueue)
     {
         super(name);
         this.out = new PrintStream(os);
         this.resQueue = resQueue;
         setPriority(Thread.MAX_PRIORITY-1);
     }
 
     @Override
     public void run() {
         System.err.println("Running STDINREDTHREAD");
 
         try {
             /* We want to write to the OutputStream (which is
              * connected to the vm process's stdin */
             /* We only get Strings from the queue, let PrintStream
              * handle buffering etc. */
 
             while (true) {
                 try {
                     /* Get next string off the queue - Blocking */
                     System.err.println("STDINTHREAD: Getting item off queue");
                     String s = resQueue.take();
                     System.err.println("STDINTHREAD: Item retrieved");
 
                     /* Write to the debuggee's stdin */
                     out.print(s);
 
                     //TODO: Remove later
                     System.err.println("STDINTHREAD: " + s);
 
                     /* Force this to be pushed to `stdin' */
                     out.flush();
                 } catch (InterruptedException e) {
 
                 }
             }
         } finally {
 
         }
 
 
     }
 }
