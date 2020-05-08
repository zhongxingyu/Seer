 package eu.uberdust.lights;
 
 import eu.uberdust.MainApp;
 import eu.uberdust.communication.rest.RestClient;
 import eu.uberdust.communication.websocket.readings.WSReadingsClient;
 import eu.uberdust.lights.tasks.*;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import java.util.Timer;
 
 /**
  * Basic functionality.
  */
 public final class LightController {
 
     /**
      * Static Logger.
      */
     private static final Logger LOGGER = Logger.getLogger(LightController.class);
 
     private boolean zone1;
 
     private boolean zone2;
 
     private boolean zone3;
 
     private boolean isScreenLocked ;
 
     private boolean flag;
 
     public static final int MAX_TRIES = 3;
 
     public static final int WINDOW = 10;
 
     public static double[] Lum = new double[WINDOW];
 
     private static int i = WINDOW-1;
 
 
     /**
      * Pir timer.
      */
     private final Timer timer;
 
     /**
      * static instance(ourInstance) initialized as null.
      */
     private static LightController ourInstance = null;
 
     public static final double LUM_THRESHOLD_1 = 350;                       //350
 
     public static final double LUM_THRESHOLD_2 = 200;                         //200
 
     private double lastLumReading;
 
     private double Median;
 
     private long lastPirReading;
 
     private long zone1TurnedOnTimestamp = 0;
 
     private long zone2TurnedOnTimestamp = 0;
 
     private long firstCall = 0;
 
     /**
      * LightController is loaded on the first execution of LightController.getInstance()
      * or the first access to LightController.ourInstance, not before.
      *
      * @return ourInstance
      */
     public static LightController getInstance() {
         synchronized (LightController.class) {
             if (ourInstance == null) {
                 ourInstance = new LightController();
             }
         }
         return ourInstance;
     }
 
     /**
      * Private constructor suppresses generation of a (public) default constructor.
      */
     private LightController() {
         PropertyConfigurator.configure(this.getClass().getClassLoader().getResource("log4j.properties"));
         LOGGER.info("Light Controller initialized");
         timer = new Timer();
 
         //setLastPirReading(Long.valueOf(RestClient.getInstance().callRestfulWebService(MainApp.SENSOR_PIR_REST).split("\t")[0]));
         setLum(RestClient.getInstance().callRestfulWebService(MainApp.SENSOR_LIGHT_READINGS_REST));
         setLastLumReading(Double.valueOf(RestClient.getInstance().callRestfulWebService(MainApp.SENSOR_LIGHT_EXT_REST).split("\t")[1]));
         setScreenLocked((Double.valueOf(RestClient.getInstance().callRestfulWebService(MainApp.SENSOR_SCREENLOCK_REST).split("\t")[1]) == 1)||(Double.valueOf(RestClient.getInstance().callRestfulWebService(MainApp.SENSOR_SCREENLOCK_REST).split("\t")[1]) == 3));
 
 
         LOGGER.info("lastLumReading -- " + lastLumReading);
         LOGGER.info("isScreenLocked -- " + isScreenLocked);
         zone1 = false;
         zone2 = false;
         zone3 = false;
         flag = false;
 
 
         WSReadingsClient.getInstance().setServerUrl("ws://uberdust.cti.gr:80/readings.ws");
 
         //Subscription for notifications.
         WSReadingsClient.getInstance().subscribe(MainApp.URN_SENSOR_PIR, MainApp.CAPABILITY_PIR);
         WSReadingsClient.getInstance().subscribe(MainApp.URN_SENSOR_LIGHT, MainApp.CAPABILITY_LIGHT);
         WSReadingsClient.getInstance().subscribe(MainApp.URN_SENSOR_SCREENLOCK, MainApp.CAPABILITY_SCREENLOCK);
 
         //Adding Observer for the last readings
         WSReadingsClient.getInstance().addObserver(new LastReadingsObserver());
 
         timer.schedule(new KeepLightsOn(timer), KeepLightsOn.DELAY);
 
     }
 
     public void setLum(final String readings){
 
         for(int k=0,j=1; k<=WINDOW-1; k++,j=j+3){
 
             Lum[k] = Double.valueOf(readings.split("\t")[j]);
             LOGGER.info("Lum["+k+"]: "+Lum[k]);
 
         }
 
     }
 
 
     public void setLastLumReading(final double thatReading) {
         double sum = 0;
 
         LOGGER.info(" i : "+i);
 
         if(i == 0) {
             Lum[i] = thatReading;
             i = WINDOW-1;
         } else {
             Lum[i] = thatReading;
             i--;
         }
 
         LOGGER.info("thatReading : "+thatReading);
         LOGGER.info(" i : "+i);
 
         for(int k=0; k<=WINDOW-1; k++){
             LOGGER.info("Lum["+k+"]: "+Lum[k]);
             sum+=Lum[k];
         }
 
         LOGGER.info("Median : "+(sum/WINDOW));
         this.Median = sum/WINDOW    ;
 
         this.lastLumReading = thatReading;
 
         if (!isScreenLocked) {
 
             if (Median < LUM_THRESHOLD_1 && lastLumReading > LUM_THRESHOLD_2) {
                 controlLight(true, 3);
 
             } else if (Median < LUM_THRESHOLD_2) {
                 controlLight(true, 2);
                 controlLight(true, 3);
             } else if (Median > LUM_THRESHOLD_1) {
                 controlLight(false, -1);
             }
         }
     }
 
 
     public void setFlag(final boolean thatFlag) {
         this.flag = thatFlag;
 
     }
 
     public void setLastPirReading(final long thatReading) {
 
         this.lastPirReading = thatReading;
         updateLightsState();
 
     }
 
 
     public void setScreenLocked(final boolean screenLocked) {
         this.isScreenLocked = screenLocked;
         updateLight3();
     }
 
 
     public long getLastPirReading() {
         return this.lastPirReading;
     }
 
     public double getLastLumReading() {
         return this.lastLumReading;
     }
 
     public double getMedian() {
         return this.Median;
     }
 
     public long getZone2TurnedOnTimestamp() {
         return this.zone2TurnedOnTimestamp;
     }
 
     public synchronized void updateLight3() {
         if (!isScreenLocked) {
             if (Median < LUM_THRESHOLD_1 && lastLumReading > LUM_THRESHOLD_2) {
                 controlLight(true, 3);
             } else if (Median < LUM_THRESHOLD_2) {
                 controlLight(true, 2);
                 controlLight(true, 3);
             }
         } else if (isScreenLocked) {
             timer.schedule(new TurnOffTask_2(timer), TurnOffTask_2.DELAY);
             controlLight(false, 3);
         }
 
     }
 
 
     public synchronized void updateLightsState() {
 
 
         if (isScreenLocked) {
 
             if (Median < LUM_THRESHOLD_1) {
                 //turn on lights
                 turnOnLights();
 
             } /*else {
                 //turn off lights
                 controlLight(false, -1);
             } */
 
         } else if (!isScreenLocked) {
 
             if (Median < LUM_THRESHOLD_1) {
 
                 turnOnLight_1();
 
             } /*else {
                 //turn off lights
                 controlLight(false, -1);
             }  */
 
         }
 
     }
 
     public synchronized void turnOnLight_1() {
 
         LOGGER.info("turnOnLight_1()");
 
         if (!flag) {
             firstCall = lastPirReading;
             flag = true;
             timer.schedule(new TurnOffTask_3(timer), TurnOffTask_3.DELAY);
         } else if (!zone1) {
             LOGGER.info("lastPirReading - firstCall = " + (lastPirReading - firstCall));
             if (lastPirReading - firstCall > 15000) {
                 controlLight(true, 1);
                 timer.schedule(new TurnOffTask_4(timer), TurnOffTask_4.DELAY);
             }
         }
 
     }
 
 
     private synchronized void turnOnLights() {
 
         if (!zone1) {
             controlLight(true, 1);
             zone1TurnedOnTimestamp = lastPirReading;
             timer.schedule(new LightTask(timer), LightTask.DELAY);
         } else if (!zone2) {
             controlLight(true, 1);
             if (lastPirReading - zone1TurnedOnTimestamp > 15000) {
                 controlLight(true, 2);
                 zone2TurnedOnTimestamp = lastPirReading;
             }
         } else {
             controlLight(true, 2);
         }
 
     }
 
 
     public boolean isScreenLocked() {
         return this.isScreenLocked;
     }
 
     public boolean isZone1() {
         return zone1;
     }
 
     public boolean isZone2() {
         return zone2;
     }
 
     public boolean isFlag() {
         return this.flag;
     }
 
     public synchronized void controlLight(final boolean value, final int zone) {
         if (zone == 1) {
             zone1 = value;
         } else if (zone == 2) {
             zone2 = value;
         } else {
             zone3 = value;
         }
         
         final String zonef;
         
         if(zone == -1){zonef = "ff";}
         else{zonef = ""+zone;}
 
         final String link = new StringBuilder(MainApp.LIGHT_CONTROLLER).append(zonef).append(",").append(value ? 1 : 0).toString();
 
         LOGGER.info(link);
         try {
             restCall(link);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
     }
 
     public synchronized void restCall(final String link) throws InterruptedException {
 
         for (int i = 0; i < MAX_TRIES; i++) {
             RestClient.getInstance().callRestfulWebService(link);
             Thread.sleep(500);
         }
 
     }
 
     public static void main(final String[] args) {
         LightController.getInstance();
        // RestClient.getInstance().callRestfulWebService("http://uberdust.cti.gr/rest/sendCommand/destination/urn:wisebed:ctitestbed:0x99c/payload/7f,69,70,1,3,1");
     }
 
}
