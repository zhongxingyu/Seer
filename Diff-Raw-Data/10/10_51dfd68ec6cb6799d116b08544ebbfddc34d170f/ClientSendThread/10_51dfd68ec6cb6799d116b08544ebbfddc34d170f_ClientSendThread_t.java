 package org.th3falc0n.nodenet.server;
 
 import java.io.IOException;
 import java.util.Arrays;
 
 import org.th3falc0n.nodenet.helper.ByteArrayHelper;
 import org.th3falc0n.nodenet.helper.LogWriter;
 
 public class ClientSendThread extends Thread {
   volatile ClientThread aclithr = null;
   LogWriter logger = new LogWriter("sendthr");
 
   public ClientSendThread(ClientThread att) {
     super();
 
     aclithr = att;
   }
 
   @Override
   public void run() {
     super.run();
    
     while (true) {
       if (aclithr.sendBuffer.size() > 0) { // are there any packet's waiting for
                                            // the client?
         byte[] buffer = aclithr.sendBuffer.get(0);
         logger.println("Sending element from send stack... PID=" + buffer[0] + " ADDR=" + ByteArrayHelper.addressToString(Arrays.copyOfRange(buffer, 8, 16)));
         try {
           aclithr.send(buffer);
         } catch (IOException e) {
           logger.println("Exception: " + e.getMessage());
         }
         aclithr.sendBuffer.remove(0);
       }
      else {
        try {
	      sleep(100);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
     }
   }
 }
