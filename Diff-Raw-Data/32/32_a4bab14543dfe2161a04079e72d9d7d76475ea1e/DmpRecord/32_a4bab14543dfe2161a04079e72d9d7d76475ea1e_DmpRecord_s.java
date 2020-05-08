 /*
  * Created on 18-Oct-2004
  */
 package org.tom.weather.davis.vp;
 
 import java.sql.Time;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.TimeZone;
 import org.apache.log4j.Logger;
 import org.tom.util.DateUtils;
 import org.tom.weather.ArchiveEntry;
 import org.tom.weather.Converter;
 import org.tom.weather.Direction;
 import uk.me.jstott.jweatherstation.datatypes.Humidity;
 import uk.me.jstott.jweatherstation.datatypes.Pressure;
 import uk.me.jstott.jweatherstation.datatypes.Temperature;
 import uk.me.jstott.jweatherstation.util.Process;
 import uk.me.jstott.jweatherstation.util.UnsignedByte;
 
 /**
  * 
  * 
  * @author Jonathan Stott
  * @version 1.1
  * @since 1.0
  */
 public class DmpRecord implements ArchiveEntry {
   /**
    * 
    */
   private static final long serialVersionUID = 1091094347739181210L;
   private static final Logger LOGGER = Logger.getLogger(DmpRecord.class);
   private boolean valid = true;
   private List invalidReasons = new ArrayList();
   private UnsignedByte[] unsignedRawData;
   private Calendar date;
   private Temperature outsideTemperature;
   private Temperature highOutsideTemperature;
   private Temperature lowOutsideTemperature;
   private int rainfall;
   private int highRainfallRate;
   private Pressure pressure;
   private int solarRadiation;
   private int numberOfWindSamples;
   private Temperature insideTemperature;
   private Humidity insideHumidity;
   private Humidity outsideHumidity;
   private int averageWindSpeed;
   private int highWindSpeed;
   private Direction highWindSpeedDirection;
   private Direction windDirection;
   private int averageUVIndex;
   private int ET;
   private int highSolarRadiation;
   private int highUVIndex;
   private int forecastRule;
   private int leafTemperature1;
   private int leafTemperature2;
   private int leafWetness1;
   private int leafWetness2;
   private int soilTemperature1;
   private int soilTemperature2;
   private int soilTemperature3;
   private int soilTemperature4;
   private int downloadRecordType;
   private Humidity extraHumidity1;
   private Humidity extraHumidity2;
   private int extraTemperature1;
   private int extraTemperature2;
   private int extraTemperature3;
   private int soilMoisture1;
   private int soilMoisture2;
   private int soilMoisture3;
   private int soilMoisture4;
 
   public DmpRecord(byte[] data) {
     UnsignedByte[] unsignedData = UnsignedByte.getUnsignedBytes(data);
     unsignedRawData = unsignedData;// Process.unsign(data);
     date = DateUtils.parseDate(unsignedRawData[0], unsignedRawData[1],
         unsignedRawData[2], unsignedRawData[3]);
     outsideTemperature = new Temperature(unsignedData[4], unsignedData[5]);
     highOutsideTemperature = new Temperature(unsignedData[6], unsignedData[7]);
     lowOutsideTemperature = new Temperature(unsignedData[8], unsignedData[9]);
     if (unsignedData[10].getByte() != 0 || unsignedData[11].getByte() != 0) {
       if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("rain data[10] " + unsignedData[10]);
        LOGGER.debug("rain data[11] " + unsignedData[11]);
       }
     }
     rainfall = Process.bytesToInt(unsignedData[10], unsignedData[11]);
     // rainfall = 1; // FIXME
     highRainfallRate = Process.bytesToInt(unsignedData[12], unsignedData[13]);
     //highRainfallRate = 1;
     if (LOGGER.isDebugEnabled()) {
       LOGGER.debug("rain rate: " + highRainfallRate);
     }
     pressure = new Pressure(Process.bytesToInt(unsignedData[14],
         unsignedData[15]));
     solarRadiation = Process.bytesToInt(unsignedData[16], unsignedData[17]);
     numberOfWindSamples = Process
         .bytesToInt(unsignedData[18], unsignedData[19]);
     insideTemperature = new Temperature(unsignedData[20], unsignedData[21]);
     insideHumidity = new Humidity(unsignedData[22].getByte());
     outsideHumidity = new Humidity(unsignedData[23].getByte());
     averageWindSpeed = unsignedData[24].getByte();
     highWindSpeed = unsignedData[25].getByte();
     highWindSpeedDirection = new Direction((int)((unsignedData[26].getByte()) * 22.5));
     int prevailingWindDirection = unsignedData[27].getByte();
     windDirection = new Direction((int)(prevailingWindDirection * 22.5)); // values
     // are
     // 0-15
     averageUVIndex = unsignedData[28].getByte() == 255 ? -9999 : unsignedData[28].getByte();
     ET = unsignedData[29].getByte();
     highSolarRadiation = Process.bytesToInt(unsignedData[30], unsignedData[31]);
     highUVIndex = unsignedData[32].getByte() == 255 ? -9999 : unsignedData[32].getByte();
     forecastRule = unsignedData[33].getByte();
     leafTemperature1 = unsignedData[34].getByte();
     leafTemperature2 = unsignedData[35].getByte();
     leafWetness1 = unsignedData[36].getByte();
     leafWetness2 = unsignedData[37].getByte();
     soilTemperature1 = unsignedData[38].getByte();
     soilTemperature2 = unsignedData[39].getByte();
     soilTemperature3 = unsignedData[40].getByte();
     soilTemperature4 = unsignedData[41].getByte();
     downloadRecordType = unsignedData[42].getByte();
     extraHumidity1 = new Humidity(unsignedData[43].getByte());
     extraHumidity2 = new Humidity(unsignedData[44].getByte());
     extraTemperature1 = unsignedData[45].getByte();
     extraTemperature2 = unsignedData[46].getByte();
     extraTemperature3 = unsignedData[47].getByte();
     soilMoisture1 = unsignedData[48].getByte();
     soilMoisture2 = unsignedData[49].getByte();
     soilMoisture3 = unsignedData[50].getByte();
     soilMoisture4 = unsignedData[51].getByte();
     checkValid();
     if (isValid()) {
       cleanupData();
     }
   }
 
   private void cleanupData() {
     double hiOutTemp = getHiOutTemp();
     double lowOutTemp = getLowOutTemp();
     
     if (hiOutTemp > 150) {
       if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("overriding hiOutTemp of: "
             + hiOutTemp + " with: " + getOutsideTemperature());
       }
       highOutsideTemperature = getOutsideTemperature();
     }
     
     if (lowOutTemp > 150) {
       if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("overriding lowOutTemp of: "
             + lowOutTemp + " with: " + getOutsideTemperature());
       }
       lowOutsideTemperature = getOutsideTemperature();
     }
     
     if (getWindGust() == 0 && getAvgWindSpeed() == 0) {
       if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("overriding wind directions of: "
             + windDirection.getDegrees() + " with: " + 0);
       }
       windDirection = new Direction(0);
       highWindSpeedDirection = new Direction(0);
     }
 
     if (averageWindSpeed > 100) {
       if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("overriding wind speed of: "
             + averageWindSpeed + " with: " + 0);
       }
       averageWindSpeed = 0;
       windDirection = new Direction(0);
       highWindSpeedDirection = new Direction(0); // if the average is garbage, so must be the max!
     }
   }
 
   private void checkValid() {
     // check CRC
 
     if (date.getTime().getTime() > new Date().getTime()) {
         valid = false;
         setInvalidReason("date in future");
     }
     
     if (outsideTemperature.getTemperatureFahrenheit() > 150) {
       valid = false;
       setInvalidReason("temp: " + outsideTemperature.getTemperatureFahrenheit());
     }
 
     if (outsideHumidity.getHumidity() > 110) {
       valid = false;
       setInvalidReason("humidity: " + outsideHumidity.getHumidity());
     }
   }
 
   /**
    * 
    * 
    * @since 1.0
    */
   public String toString() {
     Date sqlDate = new java.sql.Date(date.getTimeInMillis());
     Time sqlTime = new java.sql.Time(date.getTimeInMillis());
 
     String str = new String();
 
     if (!isValid()) {
       
       for (Iterator i = getInvalidReasons().iterator(); 
               i.hasNext();) {
         String reason = (String)i.next();
         str += "\ninvalid DmpRecord: " + reason;
       }
       str += "\n";
     }
 
     str += sqlDate.toString() + "\t" + sqlTime.toString() + "\t"
         + outsideTemperature.getTemperatureFahrenheit() + "\t"
         + insideTemperature.getTemperatureFahrenheit() + "\t"
         + getAvgWindSpeed() + "\t" + getWindDirection();
     return str;
   }
 
   /**
    * 
    * 
    * @return
    * @since 1.0
    */
   public String toSQLUpdate() {
     java.sql.Date sqlDate = new java.sql.Date(date.getTimeInMillis());
     java.sql.Time sqlTime = new java.sql.Time(date.getTimeInMillis());
     return "INSERT INTO dmprecords (date, recordedDate, outsideTemperature, highOutTemperature, "
         + "lowOutTemperature, rainfall, highRainRate, barometer, solarR"
         + "adiation, numberOfWindSamples, insideTemperature, insideHumidity,"
         + "outsideHumidity, averageWindSpeed, highWindSpeed, directionOfHiWi"
         + "ndSpeed, prevailingWindDirection, averageUVIndex, ET, highSolarRa"
         + "diation, highUVIndex, forecastRule, leafTemperature1, leafTempera"
         + "ture2, leafWetness1, leafWetness2, soilTemperature1, soilTemperat"
         + "ure2, soilTemperature3, soilTemperature4, downloadRecordType, ext"
         + "raHumidity1, extraHumidity2, extraTemperature1, extraTemperature2"
         + ", extraTemperature3, soilMoisture1, soilMoisture2, soilMoisture3,"
         + "soilMoisture4, dewpoint, windchill) VALUES ('"
         + sqlDate.toString()
         + " "
         + sqlTime.toString()
         + "',"
         + "'"
         + new java.sql.Date(new Date().getTime()).toString()
         + " "
         + new java.sql.Time(new Date().getTime()).toString()
         + "',"
         + outsideTemperature.getTemperatureFahrenheit()
         + ","
         + highOutsideTemperature.getTemperatureFahrenheit()
         + ","
         + lowOutsideTemperature.getTemperatureFahrenheit()
         + ","
         + getRain()
         + ","
         + getHighRainRate()
         + ","
         + pressure.getPressureInches()
         + ","
         + solarRadiation
         + ","
         + numberOfWindSamples
         + ","
         + insideTemperature.getTemperatureFahrenheit()
         + ","
         + insideHumidity.getHumidity()
         + ","
         + outsideHumidity.getHumidity()
         + ","
         + averageWindSpeed
         + ","
         + highWindSpeed
         + ","
         + getHighWindSpeedDirection().getDegrees()
         + ","
         + getWindDirection().getDegrees()
         + ","
         + averageUVIndex
         + ","
         + ET
         + ","
         + highSolarRadiation
         + ","
         + highUVIndex
         + ","
         + forecastRule
         + ","
         + leafTemperature1
         + ","
         + leafTemperature2
         + ","
         + leafWetness1
         + ","
         + leafWetness2
         + ","
         + soilTemperature1
         + ","
         + soilTemperature2
         + ","
         + soilTemperature3
         + ","
         + soilTemperature4
         + ","
         + downloadRecordType
         + ","
         + extraHumidity1.getHumidity()
         + ","
         + extraHumidity2.getHumidity()
         + ","
         + extraTemperature1
         + ","
         + extraTemperature2
         + ","
         + extraTemperature3
         + ","
         + soilMoisture1
         + "," + soilMoisture2 + "," + soilMoisture3 + "," + soilMoisture4 
         + "," + Converter.getDewpoint((float)getOutHumidity(), (float)getOutsideTemperature().getTemperatureFahrenheit())
         + "," + Converter.getWindChill(getWindGust(), (float)getOutsideTemperature().getTemperatureFahrenheit())    
         +  ")";
   }
 
   public Date getDate() {
     return date.getTime();
   }
 
   public Temperature getOutsideTemperature() {
     return outsideTemperature;
   }
 
   public boolean isValid() {
     return valid;
   }
 
   public double getAvgInTemp() {
     return insideTemperature.getTemperatureFahrenheit();
   }
 
   public double getAvgOutTemp() {
     return outsideTemperature.getTemperatureFahrenheit();
   }
 
   public double getHiOutTemp() {
     return highOutsideTemperature.getTemperatureFahrenheit();
   }
 
   public double getLowOutTemp() {
     return lowOutsideTemperature.getTemperatureFahrenheit();
   }
 
   public double getRain() {
    return rainfall / 100.0;
   }
   
   public double getHighRainRate() {
    return highRainfallRate / 100.0;
   }
 
   public int getAvgWindSpeed() {
     return averageWindSpeed;
   }
 
   public Direction getWindDirection() {
     return windDirection;
   }
 
   public int getWindGust() {
     return highWindSpeed;
   }
 
   public double getBarometer() {
     return pressure.getPressureInches();
   }
 
   public int getInHumidity() {
     return insideHumidity.getHumidity();
   }
 
   public int getOutHumidity() {
     return outsideHumidity.getHumidity();
   }
 
   public java.util.Date getDate(TimeZone tz) {
     Calendar c = (Calendar) date.clone();
     c.setTimeZone(tz);
     return c.getTime();
   }
 
 //  @SuppressWarnings("unchecked")
   private void setInvalidReason(String invalidReason) {
     if (LOGGER.isDebugEnabled()) {
       LOGGER.debug("invalid archive entry: " + invalidReason);
       LOGGER.debug("entry: " + this);
     }
     invalidReasons.add(invalidReason);
   }
 
   private List getInvalidReasons() {
     return invalidReasons;
   }
 
   public int getAverageUVIndex() {
     return averageUVIndex;
   }
 
   public int getHighUVIndex() {
     return highUVIndex;
   }
 
   public int getHighSolarRadiation() {
     return highSolarRadiation;
   }
 
   public int getAverageSolarRadiation() {
     return solarRadiation;
   }
 
   public int getExtraTemp1() {
     return extraTemperature1-90;
   }
 
 
   public int getNumberOfWindSamples() {
     return numberOfWindSamples;
   }
 
     /**
      * @return the highWindSpeedDirection
      */
     public Direction getHighWindSpeedDirection() {
         return highWindSpeedDirection;
     }
 
     /**
      * @param highWindSpeedDirection the highWindSpeedDirection to set
      */
     public void setHighWindSpeedDirection(Direction highWindSpeedDirection) {
         this.highWindSpeedDirection = highWindSpeedDirection;
     }
 }
