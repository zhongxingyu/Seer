 package eu.uberdust.lights.tasks;
 
 /**
  * Created by IntelliJ IDEA.
  * User: dimitris
  * Date: 5/3/12
  * Time: 12:48 AM
  * To change this template use File | Settings | File Templates.
  */
 
 import eu.uberdust.lights.LightController;
 import org.apache.log4j.Logger;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 public class KeepLightsOn extends TimerTask {
 
     private static final Logger LOGGER = Logger.getLogger(KeepLightsOn.class);
 
 
     private final Timer timer;
 
     public static final long DELAY = 60000;
 
     public KeepLightsOn(final Timer thatTimer) {
         super();
         this.timer = thatTimer;
 
     }
 
     @Override
     public void run() {
         LOGGER.info("KeepLightsOn: initiated");

         if (LightController.getInstance().getMedian() < LightController.LUM_THRESHOLD_1) {
             if (!LightController.getInstance().isBrownLocked()) {
                 LightController.getInstance().controlLight(true, 5);
             }
             if (!LightController.getInstance().isAmberLocked()) {
                 LightController.getInstance().controlLight(true, 2);
             }
             if (!LightController.getInstance().isMoinLocked()) {
                 LightController.getInstance().controlLight(true, 1);
             }
 
         } else {
             LightController.getInstance().controlLight(false, -1);
         }
         this.timer.schedule(new KeepLightsOn(timer), DELAY);
     }
 }
