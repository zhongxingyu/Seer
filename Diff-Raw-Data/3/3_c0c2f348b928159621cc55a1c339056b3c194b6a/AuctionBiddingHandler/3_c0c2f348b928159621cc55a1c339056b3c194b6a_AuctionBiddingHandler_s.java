 package at.ac.tuwien.dslab2.service.loadTest;
 
 import at.ac.tuwien.dslab2.service.biddingClient.BiddingClientService;
 
 import java.io.IOException;
 import java.util.*;
 import java.util.concurrent.BlockingQueue;
 import java.util.regex.Pattern;
 
 public class AuctionBiddingHandler extends TimerTask {
 
     private final BiddingClientService biddingClientService;
     private final long currentTime;
     private final BlockingQueue<String> listQueue;
     private final BlockingQueue<String> biddingQueue;
     private Thread timerThread;
     private final TimerNotifications timerNotifications;
     private Random random;
 
     public AuctionBiddingHandler(BiddingClientService biddingClientService, BlockingQueue<String> listQueue, BlockingQueue<String> biddingQueue, TimerNotifications timerNotifications, Random random) throws IOException {
         this.biddingClientService = biddingClientService;
         this.listQueue = listQueue;
         this.biddingQueue = biddingQueue;
         this.currentTime = System.currentTimeMillis();
         this.timerNotifications = timerNotifications;
         this.random = random;
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
             List<Integer> auctions = new ArrayList<Integer>();
 
             double price = (System.currentTimeMillis() - currentTime) / 1000;
 
             while (scanner.hasNextInt()) {
                 int auctionId = scanner.nextInt();
                 auctions.add(auctionId);
             }
 
             int index = 0;
 
             //here special case because random.nextInt(0)
             //would throw IllegalArgumentException
             if (auctions.size() > 1) {
                 index = random.nextInt(auctions.size() - 1);
             }
 
             int auctionId = auctions.get(index);
             biddingClientService.submitCommand("!bid " + auctionId + " " + String.format("%.2f", price));
             String response = biddingQueue.take();
             this.timerNotifications.newBid(response);
 
 
 
        } catch (Exception e) {
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
