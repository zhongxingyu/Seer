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
 
 import org.esa.beam.chris.operators.GPSTime.GPSReader;
 
 import java.io.InputStream;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 
 public class GpsTimeTest extends TestCase {
 
     public void testReadGPSTimes() {
         final InputStream is = GpsTimeTest.class.getResourceAsStream(
                 "CHRIS_13350_13354_PROBA1_GPS_Data.txt");
         
         GPSReader reader = new GPSTime.GPSReader(is);
         List<String[]> readRecords = reader.getReadRecords();
         assertNotNull(readRecords);
         assertEquals(183, readRecords.size());
         
         List<GPSTime> gpsTimeList = GPSTime.create(readRecords, 0, 0);
         assertNotNull(gpsTimeList);
         assertEquals(183, gpsTimeList.size());
         
         GPSTime gpsTime = gpsTimeList.get(0);
         assertNotNull(gpsTime);
        assertEquals(2452919.465436449, gpsTime.jd, 0.000001);
         
     }
 }
