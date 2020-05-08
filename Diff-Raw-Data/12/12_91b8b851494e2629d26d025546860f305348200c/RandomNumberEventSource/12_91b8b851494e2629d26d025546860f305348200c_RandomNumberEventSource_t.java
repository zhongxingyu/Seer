 package com.adeptusproductions.sse.random;
 
 import org.eclipse.jetty.servlets.EventSource;
 
 import java.io.IOException;
 import java.util.Random;
 
 
 /* originally based on http://blog.frankel.ch/simplest-push (which was wrong)
 * and https://github.com/jetty-project/jetty-eventsource-servlet/wiki */
 
 /* see also
 http://html5doctor.com/server-sent-events/
 http://today.java.net/article/2010/03/31/html5-server-push-technologies-part-1
 http://www.html5rocks.com/en/tutorials/eventsource/basics/
 http://www.w3.org/TR/eventsource/
 
  */
 
 class RandomNumberEventSource implements EventSource, Runnable {
   private Emitter emitter;
   Random random = new Random();
 
   @Override
   public void onOpen(Emitter emitter) throws IOException {
     System.out.println("Connection opened.");
     this.emitter = emitter;
 
    emitEvent("hallo thar!\n");
    emitter.comment("foo\n");
 
     this.run();
   }
 
   public void emitEvent(String dataToSend) throws IOException {
    System.out.print("Sending: " + dataToSend);
     this.emitter.data(dataToSend);
   }
 
   @Override
   public void onClose() {
     System.out.println("Connection closed.");
   }
 
   @Override
   public void run() {
     // send a random number every second until there is an IOException, ie client leaves.
     while(true) {
       try {
        sleepThenSend(1000, String.valueOf(random.nextInt(100) + 1) + "\n");
       } catch (IOException e) {
         System.out.println("IOException sending message. Exiting.");
	  	emitter.close();
         break;
       }
     }
   }
 
   private void sleepThenSend(int time, String message) throws IOException {
     try {
       Thread.sleep(time);
       emitEvent(message);
     } catch (InterruptedException e) {
       e.printStackTrace();
     }
   }
 }
