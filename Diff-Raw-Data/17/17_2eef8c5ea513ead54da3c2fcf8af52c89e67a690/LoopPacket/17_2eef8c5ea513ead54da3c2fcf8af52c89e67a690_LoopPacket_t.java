 /*
  * Created on 12-Sep-2004
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package uk.me.jstott.jweatherstation;
 
 import java.util.Date;
 import org.apache.log4j.Logger;
 import org.tom.weather.Converter;
 import org.tom.weather.Direction;
 import org.tom.weather.SnapShot;
 import uk.me.jstott.jweatherstation.datatypes.Humidity;
 import uk.me.jstott.jweatherstation.datatypes.Pressure;
 import uk.me.jstott.jweatherstation.datatypes.Temperature;
 import uk.me.jstott.jweatherstation.datatypes.Wind;
 import uk.me.jstott.jweatherstation.sql.SQLManager;
 import uk.me.jstott.jweatherstation.util.CRC;
 import uk.me.jstott.jweatherstation.util.Process;
 import uk.me.jstott.jweatherstation.util.UnsignedByte;
 
 /**
  * @author Jonathan Stott
  * 
  */
 public class LoopPacket implements SnapShot {
   private static final Logger LOGGER = Logger.getLogger(LoopPacket.class);
   private boolean valid = true;
   private Date date = new Date();
   private Pressure pressure;
   private Temperature insideTemperature;
   private Humidity insideHumidity;
   private Temperature outsideTemperature;
   private Wind wind;
   private Humidity outsideHumidity;
   private double rainRate;
   private int uv;
   private int solarRadiation;
   private int stormRain;
   private int startDateOfCurrentStorm;
   private float dayRain;
   private int monthRain;
   private int yearRain;
   private int dayET;
   private int monthET;
   private int yearET;
   private int transmitterBatteryStatus;
   private int consoleBatteryVoltage;
   private int forecastIcon;
   private int forecastRuleNumber;
   private boolean isRaining;
   private int timeOfSunrise;
   private int timeOfSunset;
   private int crc;
   private UnsignedByte[] unsignedData;
 
   public LoopPacket(byte[] data) throws IllegalArgumentException {
     UnsignedByte[] unsignedPacket = UnsignedByte.getUnsignedBytes(data);
     unsignedData = unsignedPacket;
     int pressureTrend = unsignedPacket[3].getByte();
     pressure = new Pressure((unsignedPacket[8].getByte() * 256)
         + unsignedPacket[7].getByte(), pressureTrend);
     outsideTemperature = new Temperature((unsignedPacket[13].getByte() * 256)
         + unsignedPacket[12].getByte());
     solarRadiation = (unsignedPacket[45].getByte() * 256)
         + unsignedPacket[44].getByte();
     outsideHumidity = new Humidity(unsignedPacket[33].getByte());
     uv = unsignedPacket[43].getByte();
     wind = new Wind(unsignedPacket[14].getByte(), unsignedPacket[15].getByte(),
         (unsignedPacket[17].getByte() * 256) + unsignedPacket[16].getByte());
     rainRate = ((((unsignedPacket[42].getByte() * 256) + unsignedPacket[41]
         .getByte()) / 100.0D));
     setDayRain((float)((((unsignedPacket[51].getByte() * 256) + unsignedPacket[50].getByte()) / 100.0D)));
     crc = ((unsignedPacket[97].getByte() * 256) + unsignedPacket[98].getByte());
     CRC calcCRC = new CRC();
     calcCRC.updateCRC(data);
     int checkCRC = calcCRC.getCrc();
     if (rainRate > 0.0) {
       isRaining = true;
     } else {
       isRaining = false;
     }
     checkValid();
   }
 
   private void checkValid() {
     // int rawCRC = CRC.getCRC(unsignedData);
     if (getWind().getSpeed() > 200) {
       valid = false;
     }
     if (getOutsideHumidity() > 100) {
       valid = false;
     }
     if (getOutsideTemperature().getTemperatureFahrenheit() > 130.0) {
       valid = false;
     }
     if (rainRate > 100.0) {
       valid = false;
     }
   }
 
   public Date getDate() {
     return date;
   }
 
   public String toString() {
     StringBuffer sb = new StringBuffer();
     if (isValid() == false) {
       sb.append("*** not valid *** : ");
     }
     sb.append(date);
     sb.append("\t Temp: " + outsideTemperature);
     sb.append("\t Pressure: " + pressure);
     sb.append("\t Humidity: " + outsideHumidity);
     sb.append("\t Dewpoint: " + getDewpoint());
     sb.append("\t Wind: " + wind);
     if (isRaining()) {
       sb.append("\n**Raining at " + rainRate + " inches per hour");
     }
     return sb.toString();
   }
 
   /**
    * @return Returns the outsideTemperature.
    */
   public Temperature getOutsideTemperature() {
     return outsideTemperature;
   }
 
   /**
    * @return Returns the outsideHumidity.
    */
   public float getOutsideHumidity() {
     return outsideHumidity.getHumidity();
   }
 
   public float getPressure() {
     return (float) pressure.getPressureInches();
   }
 
   public Wind getWind() {
     return wind;
   }
 
   public double getRainRate() {
     return rainRate;
   }
 
   public boolean isRaining() {
     return isRaining;
   }
 
   public float getDewpoint() {
     return (float) (Converter.getDewpoint((float) getOutsideHumidity(),
         (float) getOutsideTemperature().getTemperatureFahrenheit()));
   }
 
   public boolean isValid() {
     return valid;
   }
 
   public String getBarStatus() {
     return pressure.getTrend();
   }
 
   public float getOutsideTemp() {
     return (float) outsideTemperature.getTemperatureFahrenheit();
   }
 
   public float getWindchill() {
     return Converter.getWindChill((int)getWindspeed(), getOutsideTemp());
   }
 
   public int getWindspeed() {
     return wind.getSpeed();
   }
 
   public Direction getWindDirection() {
     return wind.getDirection();
   }
 
   public String shortToString() {
     if (isValid()) {
     return SQLManager.getSqlDate(getDate()) + " : " + getOutsideTemp() +
       " : " + getWindspeed() + " : " + getWindDirection().toShortString() +
       " : " + getPressure() + " : " + getBarStatus() +
       (isRaining() ? " : " + "raining at " + getRainRate() : "");
     } else {
       return "invalid";
     }
   }
 
   public void setDayRain(float dayRain) {
     this.dayRain = dayRain;
   }
 
   public float getDayRain() {
     return dayRain;
   }
 
   public int getTenMinAvgWind() {
     return wind.getTenMinAverage();
   }
 
   public int getUV() {
     return uv;
   }
 
   public int getSolarRadiation() {
     return solarRadiation;
   }
 }
