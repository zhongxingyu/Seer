 package stockwatch;
 
 import java.util.Timer;
 
 public class Server {
     private static final int REPEAT_AFTER = 30 * 1000;
     private static final int INIT_DELAY = 0;
 
     public static void main(String args[]) {
    	System.setProperty( "proxySet", "true" );
    	System.setProperty( "http.proxyHost", "10.144.1.10" );
    	System.setProperty( "http.proxyPort", Integer.toString( 8080 ) );
         Timer timer = new Timer();
         // refresh quotes every 5 minutes
         timer.schedule(new StockMarket(), INIT_DELAY, REPEAT_AFTER);
     }
 }
