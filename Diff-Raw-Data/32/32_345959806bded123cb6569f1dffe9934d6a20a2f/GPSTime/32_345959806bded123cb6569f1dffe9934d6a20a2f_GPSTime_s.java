 /*
  * $Id: $
  *
  * Copyright (C) 2009 by Brockmann Consult (info@brockmann-consult.de)
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the
  * Free Software Foundation. This program is distributed in the hope it will
  * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
  * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.esa.beam.chris.operators;
 
import org.esa.beam.util.DateTimeUtils;
 import org.esa.beam.util.io.CsvReader;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Handles GPS time reading and calculation
  *
  * @author Marco Zuehlke
  * @version $Revision$ $Date$
  */
 class GPSTime {
     
     private static final double GPS_JD_OFFSET = Conversions.julianDate(1980, 5, 1);
     
     final double posX;
     final double posY;
     final double posZ;
     final double velX;
     final double velY;
     final double velZ;
     final double jd;
     
     GPSTime(double posX, double posY, double posZ, double velX, double velY, double velZ, double jd) {
         this.posX = posX;
         this.posY = posY;
         this.posZ = posZ;
         this.velX = velX;
         this.velY = velY;
         this.velZ = velZ;
         this.jd = jd;
     }
     
     static List<GPSTime> create(List<String[]> records, double dTgps, double delay) {
         List<GPSTime> gpsTimes = new ArrayList<GPSTime>(records.size());
         for (String[] record : records) {
             double posX = getDouble(record, GPS.POSX);
             double posY = getDouble(record, GPS.POSY);
             double posZ = getDouble(record, GPS.POSZ);
             double velX = getDouble(record, GPS.VELOX);
             double velY = getDouble(record, GPS.VELOY);
             double velZ = getDouble(record, GPS.VELOZ);
             
             double gpsSec = getDouble(record, GPS.SECONDS) - delay - dTgps;
             double gpsWeek = getDouble(record, GPS.WEEK);
             double jd = gpsTime2datTime(gpsWeek, gpsSec);
             
             GPSTime gpstime = new GPSTime(posX, posY, posZ, velX, velY, velZ, jd);
             gpsTimes.add(gpstime);
         }
         return gpsTimes;
         
     }
     
     private static double gpsTime2datTime(double gpsWeek, double gpsSec) {
        double gpsDays = gpsSec / DateTimeUtils.SECONDS_PER_DAY;
         double jd = GPS_JD_OFFSET + gpsWeek * 7 + gpsDays;
         return jd;
     }
 
     private static double getDouble(String[] record, GPS index) {
         return Double.parseDouble(record[index.index]);
     }
     
     static class GPSReader {
 
         private final List<String[]> records;
 
         GPSReader(InputStream is) {
             Reader reader = new InputStreamReader(is);
             char[] separators = new char[]{'\t'};
             CsvReader csvReader = new CsvReader(reader, separators, true, "TIME");
             List<String[]> readRecords = null;
             try {
                 readRecords = readRecords(csvReader);
             } catch (IOException e) {
                 try {
                     is.close();
                 } catch (IOException e1) {
                     // ignore
                 }
             }
             records = readRecords;
         }
 
         private List<String[]> readRecords(CsvReader csvReader) throws IOException {
             List<String[]> readStringRecords = csvReader.readStringRecords();
             List<String[]> cleanedRecords = new ArrayList<String[]>(readStringRecords.size());
             String lastSecondsEntry = "";
             for (String[] strings : readStringRecords) {
                 String secondsEntry = strings[GPS.SECONDS.index];
                 if (!secondsEntry.equals(lastSecondsEntry)) {
                     cleanedRecords.add(strings);
                     lastSecondsEntry = secondsEntry;
                 }
             }
             return cleanedRecords;
         }
 
         List<String[]> getReadRecords() {
             return records;
         }
     }
     
     private enum GPS {
 
         TIME(0),
         PKT(1),
         POSX_PKT(2),
         POSX(3),
         POSY_PKT(4),
         POSY(5),
         POSZ_PKT(6),
         POSZ(7),
         WEEK_PKT(8),
         WEEK(9),
         SECONDS_PKT(10),
         SECONDS(11),
         VELOX_PKT(12),
         VELOX(13),
         VELOY_PKT(14),
         VELOY(15),
         VELOZ_PKT(16),
         VELOZ(17);
 
         final int index;
 
         private GPS(int index) {
             this.index = index;
         }
     }
 }
