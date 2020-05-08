 package fi.leif.java.kindlet.sailersensor;
 
 import fi.leif.java.kindlet.sailersensor.sensordisplay.CompassSensorDisplay;
 import fi.leif.java.kindlet.sailersensor.sensordisplay.GpsHeadSensorDisplay;
 import fi.leif.java.kindlet.sailersensor.sensordisplay.PitchSpeedSensorDisplay;
 import fi.leif.java.kindlet.sailersensor.sensordisplay.DebugSensorDisplay;
 import fi.leif.java.kindlet.sailersensor.sensordisplay.SensorDisplay;
 
 
 public class Config 
 {
   public final SensorDisplay[][] SENSOR_DISPLAY_PAGES = new SensorDisplay[][] {
     // Page 1
     {
       new CompassSensorDisplay(this),
       // new DebugSensorDisplay(this),
       new GpsHeadSensorDisplay(this),
       new PitchSpeedSensorDisplay(this)
     }/*,
 			
      // Page 2
      {
      new HeadingSensorDisplay(),
      new HeadingSensorDisplay(),
      new HeadingSensorDisplay()
      } */
 			
   };
 
   public final int MAX_PAGE_ROWS = 3;
   public final int PAGE_SHOW_MS = 3000;
 
   public final int TITLE_FONT_SIZE = 50;
  public final int DATA_FONT_SIZE = 200;
   public final int DATA_FONT_SIZE_2 = 120;
   public final int DATA_FONT_SIZE_3 = 60;
   public final String FONTFAMILY = null;
 	
   public final int SOCKET_PORT = 9000;
   public final int SOCKET_SLEEP_ON_ERROR = 5000;
 	
   public final String MSG_SEPARATOR = " ";
   public final int MSG_ID_LEN = 2;
 	
   public Config() {}
 }
