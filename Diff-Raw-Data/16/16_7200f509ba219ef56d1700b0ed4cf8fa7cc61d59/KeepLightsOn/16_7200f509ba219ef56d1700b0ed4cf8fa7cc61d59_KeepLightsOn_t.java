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
        LOGGER.info("KeepLightsOn Lum:"+LightController.getInstance().getMedian());
        LOGGER.info("KeepLightsOn: isBlackLocked -- "+LightController.getInstance().isScreenLocked());
 
         if (!LightController.getInstance().isScreenLocked()) {
             if (LightController.getInstance().getMedian() < LightController.LUM_THRESHOLD_1 && LightController.getInstance().getLastLumReading() > LightController.LUM_THRESHOLD_2) {
                 LightController.getInstance().controlLight(true, 3);
 
             } else if (LightController.getInstance().getLastLumReading() < LightController.LUM_THRESHOLD_2) {
                 LightController.getInstance().controlLight(true, 2);
                 LightController.getInstance().controlLight(true, 3);
             } else if (LightController.getInstance().getLastLumReading() > LightController.LUM_THRESHOLD_1) {
                 LightController.getInstance().controlLight(false, -1);
             }
 
         }
         this.timer.schedule(new KeepLightsOn(timer), DELAY);
     }
 }
