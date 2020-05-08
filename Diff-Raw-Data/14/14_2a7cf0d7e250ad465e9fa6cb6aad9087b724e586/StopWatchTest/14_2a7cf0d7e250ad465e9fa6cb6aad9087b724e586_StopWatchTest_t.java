 package poc;
 
 import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 /**
  * @author psrinivasan
  *         Date: 8/27/12
  *         Time: 11:33 AM
  */
 public class StopWatchTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(StopWatchTest.class);

     public static void main(String[] args) {
        LOGGER.info("in constructor");
         StopWatch stopWatch = new StopWatch();
         stopWatch.start();
         try {
             Thread.sleep(3000);
         } catch (InterruptedException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         stopWatch.split();
         try {
             Thread.sleep(3000);
         } catch (InterruptedException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         stopWatch.stop();
         //System.out.println("split time " + stopWatch.toSplitString());
         System.out.println("time elapsed is " + stopWatch.getTime());
 
     }
 }
