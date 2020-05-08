 /*
  * Created on Oct 13, 2005
  *
  */
 package org.tom.weather;
 
 import java.util.Date;
 import java.util.TimeZone;
 
 public class BaseArchiveEntryImpl implements ArchiveEntry {
   /**
    * 
    */
   private static final long serialVersionUID = 416982042595680801L;
 
   public double getAvgInTemp() {
     // TODO Auto-generated method stub
     return 0;
   }
 
   public double getAvgOutTemp() {
     // TODO Auto-generated method stub
     return 0;
   }
 
   public double getHiOutTemp() {
     // TODO Auto-generated method stub
     return 0;
   }
 
   public double getLowOutTemp() {
     // TODO Auto-generated method stub
     return 0;
   }
 
   public double getRain() {
     // TODO Auto-generated method stub
     return 0;
   }
 
   public int getAvgWindSpeed() {
     // TODO Auto-generated method stub
     return 0;
   }
 
   public Direction getWindDirection() {
     // TODO Auto-generated method stub
     return null;
   }
 
   public int getNativeWindDirection() {
     // TODO Auto-generated method stub
     return 0;
   }
 
   public int getWindGust() {
     // TODO Auto-generated method stub
     return 0;
   }
 
   public Date getDate() {
     // TODO Auto-generated method stub
     return null;
   }
 
   public double getBarometer() {
     // TODO Auto-generated method stub
     return 0;
   }
 
   public int getInHumidity() {
     // TODO Auto-generated method stub
     return 0;
   }
 
   public int getOutHumidity() {
     // TODO Auto-generated method stub
     return 0;
   }
 
   public Date getDate(TimeZone tz) {
     // TODO Auto-generated method stub
     return null;
   }
   
   public boolean isValid() {
     return true;
   }
 
   public int getAverageUVIndex() {
     // TODO Auto-generated method stub
     return 0;
   }
 
   public int getHighUVIndex() {
     // TODO Auto-generated method stub
     return 0;
   }
 
   public int getAverageSolarRadiation() {
     // TODO Auto-generated method stub
     return 0;
   }
 
   public int getHighSolarRadiation() {
     // TODO Auto-generated method stub
     return 0;
   }
 
   public int getNumberOfWindSamples() {
     return 0;
   }
 
   public double getHighRainRate() {
     return 0;
   }
 
   public Direction getHighWindSpeedDirection() {
     return null;
   }
   
   public int getExtraTemp1() {
     return 0;
   }
 
 }
