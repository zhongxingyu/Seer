 package co.raawr.tempest.core;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.NoSuchElementException;
 import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
 
 /**
  *
  * @author Raawr Tempest Labs
  */
 public class Out extends Thread {
 
     Socket sock;
     Core core;
     BufferedWriter out;
    LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
     volatile int delay = 100;
 
     public Out(Socket s, Core c) {
         try {
             sock = s;
             core = c;
             out = new BufferedWriter(new PrintWriter(sock.getOutputStream()));
         } catch (IOException ex) {
             core.err("I/O exception.");
         }
     }
 
     @Override
     public void run() {
         //Begin sending messages from the queue
         while (core.listening) {
            try {
                sendLine(queue.take());
            } catch (InterruptedException ex) {
                Logger.getLogger(Out.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     public void queueLine(String line) {
         queue.add(line);
     }
 
     private void sendLine(String line) {
         try {
             core.print(">>> " + line);
             out.write(line + "\r\n");
             out.flush();
         } catch (IOException ex) {
             core.err("Unable to send line.");
         }
     }
 
     public void forceLine(String line) {
         sendLine(line);
     }
 }
