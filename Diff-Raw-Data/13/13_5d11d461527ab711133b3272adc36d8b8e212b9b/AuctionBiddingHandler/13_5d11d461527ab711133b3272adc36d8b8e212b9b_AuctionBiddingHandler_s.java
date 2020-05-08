 package at.ac.tuwien.dslab2.service.loadTest;
 
 import at.ac.tuwien.dslab2.service.biddingClient.BiddingClientService;
 
 import java.io.IOException;
 import java.util.Scanner;
 import java.util.TimerTask;
 import java.util.concurrent.BlockingQueue;
 import java.util.regex.Pattern;
 
 public class AuctionBiddingHandler extends TimerTask {
 
     private final BiddingClientService biddingClientService;
     private final long currentTime;
     private final BlockingQueue<String> listQueue;
     private final BlockingQueue<String> biddingQueue;
     private Thread timerThread;
 
     public AuctionBiddingHandler(BiddingClientService biddingClientService, BlockingQueue<String> listQueue, BlockingQueue<String> biddingQueue) throws IOException {
         this.biddingClientService = biddingClientService;
         this.listQueue = listQueue;
         this.biddingQueue = biddingQueue;
         this.currentTime = System.currentTimeMillis();
     }
 
     @Override
     public void run() {
         try {
             this.timerThread = Thread.currentThread();
             biddingClientService.submitCommand("!list");
             String reply = listQueue.take();
             Scanner scanner = new Scanner(reply.trim());
             scanner.useDelimiter(Pattern.compile("\\.\\s+.*\\n?\\s*"));
             scanner.skip(Pattern.compile("\\s*"));
 
            while (scanner.hasNext()) {
                 int auctionId = scanner.nextInt();
                 double price = (System.currentTimeMillis() - currentTime) / 1000;
                 biddingClientService.submitCommand("!bid " + auctionId + " " + String.format("%.2f", price));
                 String response = biddingQueue.take();
                 //System.out.println(Thread.currentThread().getName() + ": !bid");
                 //System.out.println(response);
             }
             scanner.close();
         } catch (InterruptedException e) {
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public boolean cancel() {
         if (this.timerThread != null && this.timerThread.isAlive()) {
             this.timerThread.interrupt();
         }
         return super.cancel();
     }
 }
