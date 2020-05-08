 package stockwatch;
 
 import java.util.Timer;
 
 public class Server {
     private static final int REPEAT_AFTER = 30 * 1000;
     private static final int INIT_DELAY = 0;
 
     public static void main(String args[]) {
         Timer timer = new Timer();
         // refresh quotes every 5 minutes
         timer.schedule(new StockMarket(), INIT_DELAY, REPEAT_AFTER);
     }
 }
