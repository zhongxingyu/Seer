 package eu.uberdust.lights;
 
 import eu.uberdust.MainApp;
 import eu.uberdust.communication.rest.RestClient;
 import eu.uberdust.communication.websocket.readings.WSReadingsClient;
 import eu.uberdust.lights.tasks.KeepLightsOn;
 import eu.uberdust.lights.tasks.TurnOffTask_2;
 import eu.uberdust.lights.tasks.TurnOffTask_3;
 import eu.uberdust.lights.tasks.TurnOffTask_4;
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
 
     private boolean zone4;
 
     private boolean isBrownLocked;
 
     private boolean isAmberLocked;
 
     private boolean isMoinLocked;
 
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
 
         setLum(RestClient.getInstance().callRestfulWebService(MainApp.SENSOR_LIGHT_READINGS_REST));
         //setLastPirReading(Long.valueOf(RestClient.getInstance().callRestfulWebService(MainApp.SENSOR_PIR_REST).split("\t")[0]));
         setLastLumReading(Double.valueOf(RestClient.getInstance().callRestfulWebService(MainApp.SENSOR_LIGHT_EXT_REST).split("\t")[1]));
        setBrownLocked(Double.valueOf(RestClient.getInstance().callRestfulWebService(MainApp.SENSOR_SCREENLOCK_BROWN_REST).split("\t")[1]) == 1);
        setAmberLocked(Double.valueOf(RestClient.getInstance().callRestfulWebService(MainApp.SENSOR_SCREENLOCK_AMBER_REST).split("\t")[1]) == 1);
        //setMoinLocked(Double.valueOf(RestClient.getInstance().callRestfulWebService(MainApp.SENSOR_SCREENLOCK_MOIN_REST).split("\t")[1]) == 1);
        setMoinLocked(true);
 
 
         LOGGER.info("lastLumReading -- " + lastLumReading);
         LOGGER.info("isBrownLocked -- " + isBrownLocked);
         LOGGER.info("isAmberLocked -- " + isAmberLocked);
         LOGGER.info("isMoinLocked -- " + isMoinLocked);
 
         zone1 = false;
         zone2 = false;
         zone4 = false;
         flag = false;
 
 
         WSReadingsClient.getInstance().setServerUrl("ws://uberdust.cti.gr:80/readings.ws");
 
         //Subscription for notifications.
        // WSReadingsClient.getInstance().subscribe(MainApp.URN_SENSOR_PIR, MainApp.CAPABILITY_PIR);
         WSReadingsClient.getInstance().subscribe(MainApp.URN_SENSOR_LIGHT, MainApp.CAPABILITY_LIGHT);
         WSReadingsClient.getInstance().subscribe(MainApp.URN_SENSOR_SCREENLOCK_BROWN, MainApp.CAPABILITY_SCREENLOCK);
         WSReadingsClient.getInstance().subscribe(MainApp.URN_SENSOR_SCREENLOCK_AMBER, MainApp.CAPABILITY_SCREENLOCK);
         WSReadingsClient.getInstance().subscribe(MainApp.URN_SENSOR_SCREENLOCK_MOIN, MainApp.CAPABILITY_SCREENLOCK);
 
 
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
 
         if (Median < LUM_THRESHOLD_1) {
             if (!isBrownLocked) {
                 controlLight(true, 5);
             }
             if (!isAmberLocked) {
                 controlLight(true, 2);
             }
             if (!isMoinLocked) {
                 controlLight(true, 1);
             }
 
         } else {
             controlLight(false, -1);
         }
     }
 
 
     public void setFlag(final boolean thatFlag) {
         this.flag = thatFlag;
 
     }
 
     public void setLastPirReading(final long thatReading) {
 
         this.lastPirReading = thatReading;
         updateLightsState();
 
     }
 
 
     public void setBrownLocked(final boolean screenLocked) {
         this.isBrownLocked = screenLocked;
 
         if (!isBrownLocked) {
             if (Median  < LUM_THRESHOLD_1) {
                 controlLight(true, 5);
             }
         } else if (isBrownLocked) {
             //timer.schedule(new TurnOffTask_2(timer, 5, "brown"), TurnOffTask_2.DELAY);
             controlLight(false,5);
         }
 
     }
 
     public void setAmberLocked(final boolean screenLocked) {
         this.isAmberLocked = screenLocked;
 
         if (!isAmberLocked) {
             if (Median < LUM_THRESHOLD_1) {
                 controlLight(true, 2);
             }
         } else if (isAmberLocked) {
             //timer.schedule(new TurnOffTask_2(timer, 2, "amber"), TurnOffTask_2.DELAY);
             controlLight(false,2);
         }
     }
 
     public void setMoinLocked(final boolean screenLocked) {
         this.isMoinLocked = screenLocked;
 
         if (!isMoinLocked) {
             if (Median < LUM_THRESHOLD_1) {
                 controlLight(true, 1);
             }
         } else if (isMoinLocked) {
            // timer.schedule(new TurnOffTask_2(timer, 1, "moin"), TurnOffTask_2.DELAY);
             controlLight(false,1);
         }
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
 
 
     public synchronized void updateLightsState() {
 
         if (Median < LUM_THRESHOLD_1) {
             //turn on lights
             turnOnLight_1();
 
 
         } /*else {
             //turn off lights
             controlLight(false, -1);
         } */
 
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
                 controlLight(true, 4);
                 timer.schedule(new TurnOffTask_4(timer), TurnOffTask_4.DELAY);
             }
         }
 
     }
 
 
     public boolean isBrownLocked() {
         return this.isBrownLocked;
     }
 
     public boolean isAmberLocked() {
         return this.isAmberLocked;
     }
 
     public boolean isMoinLocked() {
         return this.isMoinLocked;
     }
 
 
     public boolean isZone1() {
         return zone1;
     }
 
     public boolean isZone4() {
         return zone4;
     }
 
     public synchronized void controlLight(final boolean value, final int zone) {
         if (zone == 1) {
             zone1 = value;
         } else if (zone == 2) {
             zone2 = value;
         } else if (zone == 4)  {
             zone4 = value;
         }
 
         final String zonef;
 
         if (zone == -1) {
             zonef = "ff";
         } else {
             zonef = "" + zone;
         }
 
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
             Thread.sleep(200);
         }
 
     }
 
     public static void main(final String[] args) {
         LightController.getInstance();
     }
 
 }
