 /**
  * Copyright (C) 2012 SINTEF <franck.fleurey@sintef.no>
  *
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * 	http://www.gnu.org/licenses/lgpl-3.0.txt
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.thingml.traale.desktop;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Locale;
 import java.util.Locale;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.thingml.traale.driver.Traale;
 import org.thingml.traale.driver.TraaleListener;
 
 /**
  *
  * @author ffl
  */
 public class TraaleFileLogger implements TraaleListener {
     
     private SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
     private String SEPARATOR = "\t";
     
     protected File folder;
     
     protected Traale traale;
     
     protected boolean logging = false;
     protected boolean request_start = false;
     protected long startTime = 0;
     protected long last_ski = 0;
     protected long last_hum = 0;
     protected long last_mag = 0;
     protected long last_imu = 0;
     protected long last_qat = 0;
     
     
     protected PrintWriter log;
     protected PrintWriter ski;
     protected PrintWriter imu;
     protected PrintWriter qat;
     protected PrintWriter hum;
     protected PrintWriter mag;
     
     public TraaleFileLogger(File folder, Traale traale) {
         this.folder = folder;
         this.traale = traale;
     }
     
     public boolean isLogging() {
         return logging;
     }
     
    public void startLoggingInFolder(File sFolder) {
        try {
            log = new PrintWriter(new FileWriter(new File(sFolder, "Traale_log.txt")));
            log.println("# This file contains one line per data received from the traale unit.");
            
            ski = new PrintWriter(new FileWriter(new File(sFolder, "Traale_ski.txt")));
            ski.println("RXTime" + SEPARATOR + "CorrTime" + SEPARATOR + "RawTime" + SEPARATOR + "dT" + SEPARATOR + "Skin Temperature (°C)");
            
            hum = new PrintWriter(new FileWriter(new File(sFolder, "Traale_hum.txt")));
            hum.println("RXTime" + SEPARATOR + "CorrTime" + SEPARATOR + "RawTime" + SEPARATOR + "dT" + SEPARATOR + "T1" + SEPARATOR + "H1" + SEPARATOR + "T2" + SEPARATOR + "H2");
            
            mag = new PrintWriter(new FileWriter(new File(sFolder, "Traale_mag.txt")));
            mag.println("RXTime" + SEPARATOR + "CorrTime" + SEPARATOR + "RawTime" + SEPARATOR + "dT" + SEPARATOR + "Mag. X" + SEPARATOR + "Mag. Y" + SEPARATOR + "Mag. Z");
            
            
            imu = new PrintWriter(new FileWriter(new File(sFolder, "Traale_imu.txt")));
            imu.println("RXTime" + SEPARATOR + "CorrTime" + SEPARATOR + "RawTime" + SEPARATOR + "dT" + SEPARATOR +  "Acc. X" + SEPARATOR + "Acc. Y" + SEPARATOR + "Acc. Z" + SEPARATOR + "Gyro. X" + SEPARATOR + "Gyro. Y" + SEPARATOR + "Gyro. Z");
 
            qat = new PrintWriter(new FileWriter(new File(sFolder, "Traale_qat.txt")));
            qat.println("RXTime" + SEPARATOR + "CorrTime" + SEPARATOR + "RawTime" + SEPARATOR + "dT" + SEPARATOR + "Quad. W" + SEPARATOR + "Quad. X" + SEPARATOR + "Quad. Y" + SEPARATOR + "Quad. Z" + SEPARATOR + "Pitch" + SEPARATOR + "Roll" + SEPARATOR + "Yaw");
 
            
        } catch (IOException ex) {
            Logger.getLogger(TraaleFileLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
        last_ski = System.currentTimeMillis();
        last_hum = System.currentTimeMillis();
        last_mag = System.currentTimeMillis();
        last_imu = System.currentTimeMillis();
        startTime = System.currentTimeMillis();
        logging = true;
     }
     
    public void startLogging() {
       String sName = createSessionName(); 
       File sFolder = new File(folder, sName);
       
       // To avoid overwriting an exiting folder (in case several logs are created at the same time)
       int i=1;
       while (sFolder.exists()) {
           sFolder = new File(folder, sName + "-" + i);
           i++;
       }
       
       sFolder.mkdir();
       startLoggingInFolder(sFolder);
    }
    
     public void stopLogging() {
         if (logging) {
             logging = false;
             log.close();
             ski.close();
             imu.close();
             mag.close();
             hum.close();
             log = null;
             ski = null;
             mag = null;
             imu = null;
             mag = null;
         }
     }
     
     public String createSessionName() {
         SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
         return timestampFormat.format( Calendar.getInstance().getTime());
     }
     
     public String currentTimeStamp(int timestamp) {
         if (timestamp<0 || traale == null) return "" + System.currentTimeMillis() + SEPARATOR + "?" + SEPARATOR + "?";
         else return "" + System.currentTimeMillis() + SEPARATOR + traale.getEpochTimestamp(timestamp) + SEPARATOR + timestamp;
     }
         
     private DecimalFormat tempFormat = new DecimalFormat("0.00");
     @Override
     public void skinTemperature(double temp, int timestamp) {
         if (logging) {
             ski.println(currentTimeStamp(timestamp) + SEPARATOR + (System.currentTimeMillis() - last_ski) + SEPARATOR + tempFormat.format(temp));
             log.println(currentTimeStamp(timestamp) + SEPARATOR + "[skinTemperature]" + SEPARATOR + tempFormat.format(temp));
             last_ski = System.currentTimeMillis();
         }
     }
 
     @Override
     public void skinTemperatureInterval(int value) {
         if (logging) log.println(currentTimeStamp(-1) + SEPARATOR + "[skinTemperatureInterval]" + SEPARATOR + value);
     }
 
     @Override
     public void humidity(int t1, int h1, int t2, int h2, int timestamp) {
         if (logging) {
             hum.println(currentTimeStamp(timestamp) + SEPARATOR + (System.currentTimeMillis() - last_hum) + SEPARATOR + tempFormat.format(t1/100.0)+ SEPARATOR + tempFormat.format(h1/100.0)+ SEPARATOR + tempFormat.format(t2/100.0)+ SEPARATOR + tempFormat.format(h2/100.0));
             log.println(currentTimeStamp(timestamp) + SEPARATOR + "[humidity]" + SEPARATOR + tempFormat.format(t1/100.0)+ SEPARATOR + tempFormat.format(h1/100.0)+ SEPARATOR + tempFormat.format(t2/100.0)+ SEPARATOR + tempFormat.format(h2/100.0));
             last_hum = System.currentTimeMillis();
         }
     }
 
     @Override
     public void humidityInterval(int value) {
         if (logging) log.println(currentTimeStamp(-1) + SEPARATOR + "[humidityInterval]" + SEPARATOR + value);
     }
 
     @Override
     public void imu(int ax, int ay, int az, int gx, int gy, int gz, int timestamp) {
         if (logging) {
               
             imu.println(currentTimeStamp(timestamp) + SEPARATOR + (System.currentTimeMillis() - last_imu) 
                                            + SEPARATOR + ax + SEPARATOR + ay + SEPARATOR + az
                                            + SEPARATOR + gx + SEPARATOR + gy + SEPARATOR + gz);
             log.println(currentTimeStamp(timestamp) + SEPARATOR + "[imu]" 
                                            + SEPARATOR + ax + SEPARATOR + ay + SEPARATOR + az
                                            + SEPARATOR + gx + SEPARATOR + gy + SEPARATOR + gz);
             last_imu = System.currentTimeMillis();
         }
     }
     
      @Override
     public void quaternion(int qw, int qx, int qy, int qz, int timestamp) {
         if (logging) {
                        
             double w = ((double)qw) / (1<<15);
             double x = ((double)qx) / (1<<15);
             double y = ((double)qy) / (1<<15);
             double z = ((double)qz) / (1<<15);
 
             double heading, attitude, bank;
 
             double sqw = w*w;
             double sqx = x*x;
             double sqy = y*y;
             double sqz = z*z;
 
             double unit = sqx + sqy + sqz + sqw; // if normalised is one, otherwise is correction factor
             double test = x*y + z*w;
 
             if (test > 0.499*unit) { // singularity at north pole
                     heading = 2 * Math.atan2(x,w);
                     attitude = Math.PI/2;
                     bank = 0;
             }
             else if (test < -0.499*unit) { // singularity at south pole
                     heading = -2 * Math.atan2(x,w);
                     attitude = -Math.PI/2;
                     bank = 0;
             }
             else {
                 heading = Math.atan2(2*y*w-2*x*z , sqx - sqy - sqz + sqw);
                 attitude = Math.asin(2*test/unit);
                 bank = Math.atan2(2*x*w-2*y*z , -sqx + sqy - sqz + sqw);
             }
             
             int pi = (int)(heading* 180 / Math.PI);
             int ro = (int)(bank* 180 / Math.PI);
             int ya = (int)(attitude* 180 / Math.PI);
             
             qat.println(currentTimeStamp(timestamp) + SEPARATOR + (System.currentTimeMillis() - last_qat) + SEPARATOR + qw + SEPARATOR + qx + SEPARATOR + qy + SEPARATOR + qz
                                            + SEPARATOR + pi + SEPARATOR + ro + SEPARATOR + ya);
             log.println(currentTimeStamp(timestamp) + SEPARATOR + "[qat]" + SEPARATOR + qw + SEPARATOR + qx + SEPARATOR + qy + SEPARATOR + qz
                                            + SEPARATOR + pi + SEPARATOR + ro + SEPARATOR + ya);
             last_qat = System.currentTimeMillis();
         }
     }
 
     @Override
     public void imuMode(int value) {
         if (logging) log.println(currentTimeStamp(-1) + SEPARATOR + "[imuMode]" + SEPARATOR + value);
     }
 
     @Override
     public void magnetometer(int x, int y, int z, int timestamp) {
        if (logging) {
            mag.println(currentTimeStamp(timestamp) + SEPARATOR + (System.currentTimeMillis() - last_mag) + SEPARATOR + x + SEPARATOR + y + SEPARATOR + z);
            log.println(currentTimeStamp(timestamp) + SEPARATOR + "[magnetometer]" + SEPARATOR + x + SEPARATOR + y + SEPARATOR + z);
            last_mag = System.currentTimeMillis();
        }
     }
 
     @Override
     public void magnetometerInterval(int value) {
         if (logging) log.println(currentTimeStamp(-1) + SEPARATOR + "[magnetometerInterval]" + SEPARATOR + value);
     }
 
     @Override
     public void battery(int battery, int timestamp) {
         if (logging) log.println(currentTimeStamp(timestamp) + SEPARATOR + "[battery]" + SEPARATOR + battery + "%" + SEPARATOR + timestamp);
     }
 
     @Override
     public void manufacturer(String value) {
         if (logging) log.println(currentTimeStamp(-1) + SEPARATOR + "[manufacturer]" + SEPARATOR + value);
     }
 
     @Override
     public void model_number(String value) {
         if (logging) log.println(currentTimeStamp(-1) + SEPARATOR + "[model_number]" + SEPARATOR + value);
     }
 
     @Override
     public void serial_number(String value) {
         if (logging) log.println(currentTimeStamp(-1) + SEPARATOR + "[serial_number]" + SEPARATOR + value);
     }
 
     @Override
     public void hw_revision(String value) {
         if (logging) log.println(currentTimeStamp(-1) + SEPARATOR + "[hw_revision]" + SEPARATOR + value);
     }
 
     @Override
     public void fw_revision(String value) {
         if (logging) log.println(currentTimeStamp(-1) + SEPARATOR + "[fw_revision]" + SEPARATOR + value);
     }
 
     @Override
     public void imuInterrupt(int value) {
         if (logging) log.println(currentTimeStamp(-1) + SEPARATOR + "[imuInterrupt]" + SEPARATOR + value);
     }
 
     @Override
     public void testPattern(byte[] data, int timestamp) {
     }
 
     @Override
     public void timeSync(int seq, int timestamp) {
 
     }
 
     @Override
     public void alertLevel(int value) {
         
     }
     
 }
