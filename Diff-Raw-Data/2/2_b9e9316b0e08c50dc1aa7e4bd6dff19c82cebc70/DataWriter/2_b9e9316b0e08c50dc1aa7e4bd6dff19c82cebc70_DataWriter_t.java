 /*
  * Copyright (C) 2012 Jérémy Compostella
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.oux.SmartGPSLogger;
 
 import android.os.Environment;
 import android.location.Location;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.BufferedWriter;
 import android.text.format.DateFormat;
 import android.util.Log;
 
 public class DataWriter
 {
     private final String DIR = "/sdcard/SmartGPSLogger/";
     private final String SUFFIX = ".txt";
     private final String CURRENT = DIR + "current" + SUFFIX;
     private final String FMT = "yyyy-MM-dd";
 
     private String lastLocDate = null;
     private File file;
     private BufferedWriter writer;
 
     private void openCurrent() throws java.io.IOException
     {
         file = new File(CURRENT);
         writer = new BufferedWriter(new FileWriter(file, true));
     }
 
     public DataWriter () throws java.io.IOException
     {
         File root = Environment.getExternalStorageDirectory();
         if (root.canWrite())
         {
             File dir = new File(DIR);
             if (!dir.exists())
                 dir.mkdir();
             openCurrent();
         }
     }
 
     public void write(Location loc) throws java.io.IOException
     {
         String newLocDate = DateFormat.format(FMT, loc.getTime()).toString();
 
         if (lastLocDate == null)
             lastLocDate = DateFormat.format(FMT, loc.getTime()).toString();
 
         if (!lastLocDate.equals(newLocDate))
         {
             writer.close();
             file.renameTo(new File(DIR + lastLocDate + ".txt"));
             openCurrent();
         }
 
         writer.write(DateFormat.format("yyyy:MM:dd", loc.getTime()) +
                     "," + DateFormat.format("kk:mm:ss", loc.getTime()) +
                      "," + loc.getLatitude() +
                      "," + loc.getLongitude() +
                      "," + loc.getSpeed() +
                      "," + loc.getAltitude() + "\n");
         writer.flush();         // TODO: Flush on each line is fine
                                 // but use a BufferedWriter to flush
                                 // on each line is ugly
 
         Log.d("DateWriter", DateFormat.format("yyyy:MM:dd", loc.getTime()) +
               "," + DateFormat.format("hh:mm:ss", loc.getTime()) +
               "," + loc.getLatitude() +
               "," + loc.getLongitude() +
               "," + loc.getSpeed() +
               "," + loc.getAltitude() + " wroten");
     }
 }
