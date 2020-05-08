 package poc;
 
 import org.apache.commons.lang3.time.StopWatch;
 
 /**
  * @author psrinivasan
  *         Date: 8/27/12
  *         Time: 11:33 AM
  */
 public class StopWatchTest {
     public static void main(String[] args) {
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
