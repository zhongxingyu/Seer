 package co.raawr.tempest.core;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.Socket;
import java.util.LinkedList;
 import java.util.NoSuchElementException;
 import java.util.Queue;
 
 /**
  *
  * @author Raawr Tempest Labs
  */
 public class Out extends Thread {
 
     Socket sock;
     Core core;
     BufferedWriter out;
    Queue<String> queue = new LinkedList<>();
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
            if (!queue.isEmpty()) {
                try {
                    sendLine(queue.remove());
                    Thread.sleep(delay);
                } catch (InterruptedException | NoSuchElementException ex) {
                }
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
