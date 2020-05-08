 package uk.org.smithfamily.mslogger.ecuDef;
 
 import uk.org.smithfamily.mslogger.GPSLocationManager;
 import android.text.format.DateFormat;
 
 /**
  * Megasquirt utilities class heavily used in the ECU definition Java class
  */
 public class MSUtils
 {
     /**
      * Get the long for the specified index in the buffer
      * 
      * @param ochBuffer
      * @param i
      * @return
      */
     public static int getLong(byte[] ochBuffer, int i)
     {
         return getWord(ochBuffer, i) * 65536 + getWord(ochBuffer, i + 2);
     }
 
     /**
      * Get the word for the specified index in the buffer
      * 
      * @param ochBuffer
      * @param i
      * @return
      */
     public static int getWord(byte[] ochBuffer, int i)
     {
        return (getByte(ochBuffer,i) * 256 + getByte(ochBuffer,i+1));
     }
 
     /**
      * 
      * Get the byte for the specified index in the buffer
      * 
      * @param ochBuffer
      * @param i
      * @return
      */
     public static int getByte(byte[] ochBuffer, int i)
     {
         return (int) ochBuffer[i] & 0xFF;
     }
 
     /**
      * Get the signed long for the specified index in the buffer
      * 
      * @param ochBuffer
      * @param i
      * @return
      */
     public static int getSignedLong(byte[] ochBuffer, int i)
     {
         int x = getLong(ochBuffer, i);
         if (x > 2 << 32 - 1)
         {
             x = 2 << 32 - x;
         }
         return x;
     }
 
     /**
      * Get the signed byte for the specified index in the buffer
      * 
      * @param ochBuffer
      * @param i
      * @return
      */
     public static int getSignedByte(byte[] ochBuffer, int i)
     {
         int x = getByte(ochBuffer, i);
         if (x > 127)
         {
             x = 256 - x;
         }
         return x;
     }
 
     /**
      * Get the signed word for the specified index in the buffer
      * 
      * @param ochBuffer
      * @param i
      * @return
      */
     public static int getSignedWord(byte[] ochBuffer, int i)
     {
         int x = getWord(ochBuffer, i);
         if (x > 32767)
         {
             x = 32768 - x;
         }
         return x;
     }
     
     /**
      * Get bits at the specified index for the page buffer 
      * 
      * @param pageBuffer    Page buffer of data
      * @param i             Index where the value is
      * @param _bitLo
      * @param _bitHi
      * @param bitOffset
      * @return
      */
     public static int getBits(byte[] pageBuffer, int i, int _bitLo, int _bitHi,int bitOffset)
     {
         int val = 0;
         byte b = pageBuffer[i];
 
         long mask = ((1 << (_bitHi - _bitLo + 1)) - 1) << _bitLo;
         val = (int) ((b & mask) >> _bitLo) + bitOffset;
 
         return val;
     }
     
     /**
      * @return The latitude of the last known location
      */
     public static double getLatitude()
     {
         return GPSLocationManager.INSTANCE.getLastKnownLocation().getLatitude();
     }
     
     /**
      * @return The longitude of the last known location
      */
     public static double getLongitude()
     {
         return GPSLocationManager.INSTANCE.getLastKnownLocation().getLongitude();
     }
     
     /**
      * @return The speed of the last known location
      */
     public static double getSpeed()
     {
         return GPSLocationManager.INSTANCE.getLastKnownLocation().getSpeed();
     }
     
     /**
      * @return The bearing of the last known location
      */
     public static double getBearing()
     {
         return GPSLocationManager.INSTANCE.getLastKnownLocation().getBearing();
     }
     
     /**
      * @return The accuracy of the last known location
      */
     public static double getAccuracy()
     {
         return GPSLocationManager.INSTANCE.getLastKnownLocation().getAccuracy();
     }
     
     /**
      * @return The time of the last known location
      */
     public static String getTime()
     {
         long time = GPSLocationManager.INSTANCE.getLastKnownLocation().getTime();
         return DateFormat.format("hh:mm:ss", time).toString();
     }
 
     /**
      * @return The location header used in datalog
      */
     public static String getLocationLogHeader()
     {
         return "Lat\tLong\tSpeed\tBearing\tAccuracy\tGPSTime";
     }
     
     /**
      * @return A datalog row of location information
      */
     public static String getLocationLogRow()
     {
         return getLatitude() + "\t" + 
                getLongitude() + "\t" +
                getSpeed() + "\t" +
                getBearing() + "\t" +
                getAccuracy() + "\t" +
                getTime();
     }
 }
