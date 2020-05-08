 package edu.ucsc.barrel.cdf_gen;
 
 import gsfc.nssdc.cdf.CDF;
 import gsfc.nssdc.cdf.CDFException;
 import gsfc.nssdc.cdf.util.CDFTT2000;
 import gsfc.nssdc.cdf.Variable;
 
 import java.io.InputStreamReader;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.nio.channels.FileChannel;
 import java.util.Calendar;
 import java.util.Vector;
 import java.util.Arrays;
 
 /*
 LevelTwo.java v13.02.28
 
 Description:
    LevelTwo.java pulls data from the DataHolder.java object and processes it 
    into physical units (when needed) and outputs CDF files in the L2 directory.
 
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    This file is part of The BARREL CDF Generator.
 
    The BARREL CDF Generator is free software: you can redistribute it and/or 
    modify it under the terms of the GNU General Public License as published 
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
 
    The BARREL CDF Generator is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License along with 
    The BARREL CDF Generator.  If not, see <http://www.gnu.org/licenses/>.
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 
 Change Log:
    v13.03.28
       -Added rebin routines to MSPC and SSPC
    v13.02.28
       -Now outputs correct L2 values for all variables except spectra (Still
          needs rebin)
    v13.02.15
       -Updated to match the current version of Level One
    
    v13.02.06
       -New version of Level Two. An exact copy of Level One for now...
 */
 
 
 public class LevelTwo{
    String outputPath;
    int lastFrame = -1;
    long ms_of_week = 0;
    int weeks = 0;
    String
       id = "00",
       flt = "00",
       stn = "0",
       revNum = "00",
      mag_gen_program = "";
    int today = 0;
    Calendar dateObj = Calendar.getInstance();
    
    SpectrumExtract spectrum;
    
    short INCOMPLETE_GROUP = 8196;
    
    private DataHolder data;
    
    public LevelTwo(
       final String d, final String p, 
       final String f, final String s, final String m
    ) throws IOException
    {
       //get file revision number
       if(CDF_Gen.getSetting("rev") != null){
          revNum = CDF_Gen.getSetting("rev");
       }
       
       //save input arguments
       id = p;
       flt = f;
       stn = s;
       today = Integer.valueOf(d);
      mag_gen_program = m;
 
       //get the data storage object
       data = CDF_Gen.getDataSet();
      
       //create the spectrum rebinning object
       spectrum = new SpectrumExtract();
      
       //set output path
       outputPath = CDF_Gen.L2_Dir;
       
       //get data from DataHolder and save them to CDF files
       try{
          writeData();
       }catch(CDFException ex){
          System.out.println(ex.getMessage());
       }
    }
    
    //Convert the GPS data and save it to CDF files
    public void doGpsCdf(int first, int last, int date) throws CDFException{
       CDF cdf;
       Variable var;
       Calendar d = Calendar.getInstance();
       String line;
       int 
          year, month, day, day_of_year,
          numOfRecs = last - first;
       double
          sec_of_day = 0;
       float
          east_lon = 0;
       String[] 
          mag_coords = new String[3];
       float[] 
          lat = new float[numOfRecs], 
          lon = new float[numOfRecs], 
          alt = new float[numOfRecs],
          mlt = new float[numOfRecs],
          l = new float[numOfRecs];
       int[] 
          ms = new int[numOfRecs],
          frameGroup = new int[numOfRecs],
          q = new int[numOfRecs]; 
       long[] 
          epoch_parts = new long[9],
          epoch = new long[numOfRecs];
 
       System.out.println("\nSaving GPS Level Two CDF...");
 
       //calculate the day of year
       year = date / 10000;
       month = (date - (year * 10000)) / 100;
       day = date - (year * 10000) - (month * 100);
       d.set(Calendar.YEAR, year + 2000);
       d.set(Calendar.MONTH, month - 1);
       d.set(Calendar.DAY_OF_MONTH, day);
       day_of_year = d.get(Calendar.DAY_OF_YEAR);
 
       //convert lat, lon, and alt values and select values for this date
       for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
          //convert mm to km
          alt[rec_i] = (float)data.gps_raw[0][data_i] / 1000000;
 
          //convert lat and lon to physical units
          lat[rec_i] = (float)data.gps_raw[2][data_i];
          lat[rec_i] *= 
             Float.intBitsToFloat(Integer.valueOf("33B40000", 16).intValue());
 
          lon[rec_i] = (float)data.gps_raw[3][data_i];
          lon[rec_i] *= 
             Float.intBitsToFloat(Integer.valueOf("33B40000", 16).intValue());
 
          //save the values from the other variables
          ms[rec_i] = data.gps_raw[1][data_i];
          frameGroup[rec_i] = data.frame_mod4[data_i];
          epoch[rec_i] = data.epoch_mod4[data_i];
          q[rec_i] = data.gps_q[data_i];
 
          //make sure we have a complete gps record before generating mag coords
          if((alt[rec_i] != 0) && (lat[rec_i] != 0) && (lon[rec_i] != 0)){
             try{
                //calculate the current time in seconds of day
                epoch_parts = CDFTT2000.breakdown(epoch[rec_i]);
                sec_of_day = 
                   (epoch_parts[3] * 3600) + // hours
                   (epoch_parts[4] * 60) + //minutes
                   epoch_parts[5] + //seconds
                   (epoch_parts[6] * 0.001) + //ms
                   (epoch_parts[7] * 0.000001) + //us
                   (epoch_parts[8] * 0.000000001); //ns
                //convert signed longitude to east longitude
                east_lon = (lon[rec_i] > 0) ? lon[rec_i] : lon[rec_i] + 360;
 
                //get the magnetic field info for this location
                String command = 
                  mag_gen_program + " " + 
                   frameGroup[rec_i] +" "+ 
                   alt[rec_i] +" "+ lat[rec_i] +" "+ lon[rec_i] +" "+ 
                   (year + 2000)  +" "+  day_of_year +" "+ sec_of_day;
 
                Process p = Runtime.getRuntime().exec(command);
                BufferedReader input = 
                   new BufferedReader(new InputStreamReader(p.getInputStream()));
 
                //read and save the mag coords
                line = input.readLine();
                mag_coords = line.split("\\s+");
                l[rec_i] = Math.abs(Float.parseFloat(mag_coords[2]));
                mlt[rec_i] = Float.parseFloat(mag_coords[3]);
                input.close();
             }catch(Exception ex){
                //something went wrong, so dont save any coords for this record
                System.out.println("Could not generate mag coords:");
                System.out.println(ex.getMessage());
                continue;
             }
          }
       }
 
       //make sure there is a CDF file to open
       //(CDF_Gen.copyFile will not clobber an existing file)
       String srcName = 
          "cdf_skels/l2/barCLL_PP_S_l2_gps-_YYYYMMDD_v++.cdf";
       String destName = 
          outputPath + "/" + date + "/" + "bar1" + flt + "_" + id + "_" + stn + 
          "_l2_" + "gps-" + "_20" + date +  "_v" + revNum + ".cdf";
 
       CDF_Gen.copyFile(new File(srcName), new File(destName), false);
 
       //open GPS CDF and save the reference in the cdf variable
       cdf = CDF_Gen.openCDF(destName);
       
       var = cdf.getVariable("GPS_Alt");
       System.out.println("GPS_Alt...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1,
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          alt
       );
 
       var = cdf.getVariable("ms_of_week");
       System.out.println("ms_of_week...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          ms
       );
 
       var = cdf.getVariable("GPS_Lat");
       System.out.println("GPS_Lat...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          lat 
       );
 
       var = cdf.getVariable("GPS_Lon");
       System.out.println("GPS_Lon...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          lon
       );
 
       var = cdf.getVariable("L");
       System.out.println("ms_of_week...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          l
       );
 
       var = cdf.getVariable("MLT");
       System.out.println("ms_of_week...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          mlt
       );
 
       var = cdf.getVariable("FrameGroup");
       System.out.println("FrameGroup...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          frameGroup
       );
 
       var = cdf.getVariable("Epoch");
       System.out.println("Epoch...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          epoch
       );
       
       var = cdf.getVariable("Q");
       System.out.println("Q...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          q
       );
 
       System.out.println("Done with GPS!");
       //close current cdf
       cdf.close();
    }
    
    //write the pps file, no processing needed
    public void doPpsCdf(int first, int last, int date) throws CDFException{
       CDF cdf;
       Variable var;
       
       int numOfRecs = last - first;
       short[] 
          version = new short[numOfRecs],
          payID = new short[numOfRecs];
       int[] 
          frameGroup = new int[numOfRecs],
          q = new int[numOfRecs],
          pps = new int[numOfRecs];
       long[] epoch = new long[numOfRecs];
 
       System.out.println("\nSaving PPS Level Two CDF...");
 
       for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
         pps[rec_i] = data.pps[data_i];
         version[rec_i] = data.ver[data_i];
         payID[rec_i] = data.payID[data_i];
         frameGroup[rec_i] = data.frame_1Hz[data_i];
         epoch[rec_i] = data.epoch_1Hz[data_i];
         q[rec_i] = data.pps_q[data_i];
       }
 
       String srcName = 
          "cdf_skels/l2/barCLL_PP_S_l2_pps-_YYYYMMDD_v++.cdf";
       String destName = 
          outputPath  + "/" + date + "/" + "bar1" + flt + "_" + id + "_" + stn + 
          "_l2_" + "pps-" + "_20" + date +  "_v" + revNum + ".cdf";
       CDF_Gen.copyFile(new File(srcName), new File(destName), false);
 
       cdf = CDF_Gen.openCDF(destName);
       
       var = cdf.getVariable("GPS_PPS");
       System.out.println("GPS_PPS...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1L, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          pps
       );
 
       var = cdf.getVariable("Version");
       System.out.println("Version...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1L, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          version
       );
 
       var = cdf.getVariable("Payload_ID");
       System.out.println("Payload_ID...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1L, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          payID
       );
 
       var = cdf.getVariable("FrameGroup");
       System.out.println("FrameGroup...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1L, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          frameGroup 
       );
       var = cdf.getVariable("Epoch");
       System.out.println("Epoch...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1L,
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          epoch
       );
 
       var = cdf.getVariable("Q");
       System.out.println("Q...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1L, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          q
       );
 
       cdf.close();
    }
    
    public void doMagCdf(int first, int last, int date) throws CDFException{
       CDF cdf;
       Variable var;
       
       int numOfRecs = last - first;
       int[] 
          frameGroup = new int[numOfRecs],
          q = new int[numOfRecs]; 
       long[] epoch = new long[numOfRecs];
 
       float[] 
          magx = new float[numOfRecs],
          magy = new float[numOfRecs],
          magz = new float[numOfRecs],
          magTot = new float[numOfRecs];
 
       System.out.println("\nSaving Magnetometer Level Two CDF...");
 
       String srcName = 
          "cdf_skels/l2/barCLL_PP_S_l2_magn_YYYYMMDD_v++.cdf";
       String destName = 
          outputPath + "/" + date + "/" + "bar1" + flt + "_" + id + "_" + stn + 
          "_l2_" + "magn" + "_20" + date +  "_v" + revNum + ".cdf";
       CDF_Gen.copyFile(new File(srcName), new File(destName), false);
 
       cdf = CDF_Gen.openCDF(destName);
      
       //extract the nominal magnetometer value and calculate |B|
       for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
          magx[rec_i] = (data.magx_raw[data_i] - 8388608.0f) / 83886.070f;
          magy[rec_i] = (data.magy_raw[data_i] - 8388608.0f) / 83886.070f;
          magz[rec_i] = (data.magz_raw[data_i] - 8388608.0f) / 83886.070f;
 
          magTot[rec_i] = 
             (float)Math.sqrt(
                (magx[rec_i] * magx[rec_i]) + 
                (magy[rec_i] * magy[rec_i]) +
                (magz[rec_i] * magz[rec_i]) 
             );
 
          frameGroup[rec_i] = data.frame_4Hz[data_i];
          epoch[rec_i] = data.epoch_4Hz[data_i];
          q[rec_i] = data.magn_q[data_i];
       }
 
       //store the nominal mag values
       var = cdf.getVariable("MAG_X");
       System.out.println("MAG_X... ");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          magx 
       );
 
       var = cdf.getVariable("MAG_Y");
       System.out.println("MAG_Y...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          magy
       );
 
       var = cdf.getVariable("MAG_Z");
       System.out.println("MAG_Z...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          magz
       );
 
       var = cdf.getVariable("Total");
       System.out.println("Field Magnitude...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          magTot 
       );
 
       var = cdf.getVariable("FrameGroup");
       System.out.println("FrameGroup...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          frameGroup
       );
 
       var = cdf.getVariable("Epoch");
       System.out.println("Epoch...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          epoch
       );
 
       var = cdf.getVariable("Q");
       System.out.println("Q...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          q
       );
 
       cdf.close();
 
    }
    
    public void doHkpgCdf(int first, int last, int date) throws CDFException{
       CDF cdf;
       Variable var;
       
       int numOfRecs = last - first;
       short []
          sats = new short[numOfRecs],
          offset = new short[numOfRecs],
          termStat = new short[numOfRecs],
          modemCnt = new short[numOfRecs],
          dcdCnt = new short[numOfRecs];
       int[] 
          frameGroup = new int[numOfRecs],
          q = new int[numOfRecs],
          cmdCnt = new int[numOfRecs],
          weeks = new int[numOfRecs];
       long[] epoch = new long[numOfRecs];
 
       System.out.println("\nSaving HKPG...");
 
       String srcName = 
          "cdf_skels/l2/barCLL_PP_S_l2_hkpg_YYYYMMDD_v++.cdf";
       String destName = 
          outputPath + "/" + date + "/" + "bar1" + flt + "_" + id + "_" + stn 
          + "_l2_" + "hkpg" + "_20" + date +  "_v" + revNum + ".cdf";
 
       CDF_Gen.copyFile(new File(srcName), new File(destName), false);
 
       cdf = CDF_Gen.openCDF(destName);
          
       for(int var_i = 0; var_i < data.hkpg_scale.length; var_i++){
          //scale all the records for this variable
          double[] hkpg_scaled = new double[numOfRecs];
          for(int rec_i = 0; rec_i < numOfRecs; rec_i++){
             hkpg_scaled[rec_i] = 
                (data.hkpg_raw[var_i][rec_i] * data.hkpg_scale[var_i]) + 
                data.hkpg_offset[var_i];
          }
 
          var = cdf.getVariable(data.hkpg_label[var_i]);
          System.out.println(data.hkpg_label[var_i] + "...");
          var.putHyperData(
             var.getNumWrittenRecords(), numOfRecs, 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             hkpg_scaled
          );
       }
 
       for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
          sats[rec_i] = data.sats[data_i];
          offset[rec_i] = data.offset[data_i];
          termStat[rec_i] = data.termStat[data_i];
          modemCnt[rec_i] = data.modemCnt[data_i];
          dcdCnt[rec_i] = data.dcdCnt[data_i];
          cmdCnt[rec_i] = data.cmdCnt[data_i];
          weeks[rec_i] = data.weeks[data_i];
          epoch[rec_i] = data.epoch_mod40[data_i];
          q[rec_i] = data.hkpg_q[data_i];
       }
 
       var = cdf.getVariable("numOfSats");
       System.out.println("numOfSats...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          sats
       );
 
       var = cdf.getVariable("timeOffset");
       System.out.println("timeOffset...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          offset
       );
       
       var = cdf.getVariable("termStatus");
       System.out.println("termStatus...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          termStat
       );
 
       var = cdf.getVariable("cmdCounter");
       System.out.println("cmdCounter...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          cmdCnt
       );
 
       var = cdf.getVariable("modemCounter");
       System.out.println("modemCounter...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          modemCnt
       );
 
       var = cdf.getVariable("dcdCounter");
       System.out.println("dcdCounter...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          dcdCnt
       );
 
       var = cdf.getVariable("weeks");
       System.out.println("weeks...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          weeks
       );
 
       var = cdf.getVariable("FrameGroup");
       System.out.println("FrameGroup...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          frameGroup
       );
 
       var = cdf.getVariable("Epoch");
       System.out.println("Epoch...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          epoch
       );
 
       var = cdf.getVariable("Q");
       System.out.println("Q...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          q
       );
 
       cdf.close();
    }
 
    public void doFspcCdf(int first, int last, int date) throws CDFException{
       CDF cdf;
       Variable var;
       int numOfRecs = last - first;
 
       double[][] 
          chan_edges = new double[numOfRecs][5],
          lc_scaled = new double[4][numOfRecs];
       int[] tempLC = new int[4];
       double scint_temp = 20, dpu_temp = 20, peak = -1;
       
       int[] 
          frameGroup = new int[numOfRecs],
          q = new int[numOfRecs];
       long[] epoch = new long[numOfRecs];
 
       System.out.println("\nSaving FSPC...");
 
       String srcName = 
          "cdf_skels/l2/barCLL_PP_S_l2_fspc_YYYYMMDD_v++.cdf";
       String destName = 
          outputPath + "/" + date + "/" + "bar1" + flt + "_" + id + "_" + stn 
          + "_l2_" + "fspc" + "_20" + date +  "_v" + revNum + ".cdf";
       CDF_Gen.copyFile(new File(srcName), new File(destName), false);
 
       cdf = CDF_Gen.openCDF(destName);
       
       //convert the light curves counts to cnts/sec and 
       //figure out the channel width
       for(int lc_rec = 0, hkpg_rec = 0; lc_rec < numOfRecs; lc_rec++){
 
          //get temperatures
          hkpg_rec = lc_rec / 20 / 40; //convert from 20Hz to mod40
          if(data.hkpg_raw[data.T0][hkpg_rec] != 0){
             scint_temp = 
                (data.hkpg_raw[data.T0][hkpg_rec] * data.hkpg_scale[data.T0]) + 
                data.hkpg_offset[data.T0];
          }else{
             scint_temp = 20;
          }
          if(data.hkpg_raw[data.T5][hkpg_rec] != 0){
             dpu_temp = 
                (data.hkpg_raw[data.T5][hkpg_rec] * data.hkpg_scale[data.T5]) + 
                data.hkpg_offset[data.T5];
          }else{
             dpu_temp = 20;
          }
          
          //find the bin that contains the 511 line
          //peak = spectrum.find511(mspc_rebin[mspc_rec], offset);
 
          //get the adjusted bin edges
          chan_edges[lc_rec] = 
             spectrum.createBinEdges(0, scint_temp, dpu_temp, peak);
 
          //write the spectrum to the new array
          lc_scaled[0][lc_rec] = data.lc1_raw[lc_rec] * 20;
          lc_scaled[1][lc_rec] = data.lc2_raw[lc_rec] * 20;
          lc_scaled[2][lc_rec] = data.lc3_raw[lc_rec] * 20;
          lc_scaled[3][lc_rec] = data.lc4_raw[lc_rec] * 20;
       }
 
       for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
          frameGroup[rec_i] = data.frame_20Hz[data_i];
          epoch[rec_i] = data.epoch_20Hz[data_i];
          q[rec_i] = data.fspc_q[data_i];
       }
 
       var = cdf.getVariable("LC1");
       System.out.println("LC1...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          lc_scaled[0]
       );
       
       var = cdf.getVariable("LC2");
       System.out.println("LC2...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          lc_scaled[1]
       );
 
       var = cdf.getVariable("LC3");
       System.out.println("LC3...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          lc_scaled[2]
       );
 
       var = cdf.getVariable("LC4");
       System.out.println("LC4...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          lc_scaled[3]
       );
 
       var = cdf.getVariable("FrameGroup");
       System.out.println("FrameGroup...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          frameGroup
       );
 
       var = cdf.getVariable("Epoch");
       System.out.println("Epoch...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          epoch
       );
 
       var = cdf.getVariable("Q");
       System.out.println("Q...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          q
       );
 
       cdf.close();
 
    }
 
    public void doMspcCdf(int first, int last, int date) throws CDFException{
       CDF cdf;
       Variable var;
       
       double peak = -1, scint_temp = 0, dpu_temp = 0;
       
       int offset = 90;
 
       int numOfRecs = last - first;
       int[] 
          frameGroup = new int[numOfRecs],
          q = new int[numOfRecs];
       long[] epoch = new long[numOfRecs];
 
       double[][] mspc_rebin = new double[numOfRecs][48];
       double[] old_edges = new double[48];
       double[] std_edges = SpectrumExtract.stdEdges(1, 2.4414);
 
       
       //rebin the mspc spectra
       for(int mspc_rec = 0, hkpg_rec = 0; mspc_rec < numOfRecs; mspc_rec++){
          
          //get temperatures
          hkpg_rec = mspc_rec * 4 / 40; //convert from mod4 to mod40
          if(data.hkpg_raw[data.T0][hkpg_rec] != 0){
             scint_temp = 
                (data.hkpg_raw[data.T0][hkpg_rec] * data.hkpg_scale[data.T0]) + 
                data.hkpg_offset[data.T0];
          }else{
             scint_temp = 20;
          }
          if(data.hkpg_raw[data.T5][hkpg_rec] != 0){
             dpu_temp = 
                (data.hkpg_raw[data.T5][hkpg_rec] * data.hkpg_scale[data.T5]) + 
                data.hkpg_offset[data.T5];
          }else{
             dpu_temp = 20;
          }
          
          //find the bin that contains the 511 line
          //peak = spectrum.find511(mspc_rebin[mspc_rec], offset);
       
          //get the adjusted bin edges
          old_edges = spectrum.createBinEdges(1, scint_temp, dpu_temp, peak);
 
          //rebin the spectrum
          mspc_rebin[mspc_rec] = spectrum.rebin(
             data.mspc_raw[mspc_rec], old_edges, std_edges, 49, 49, true 
          );
 
          //divide counts by bin width and adjust the time scale
          for(int bin_i = 0; bin_i < mspc_rebin[mspc_rec].length; bin_i++){
             mspc_rebin[mspc_rec][bin_i] /= 
                std_edges[bin_i + 1] - std_edges[bin_i];
 
             mspc_rebin[mspc_rec][bin_i] /= 4;
          }
       }
 
       for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
          frameGroup[rec_i] = data.frame_mod4[data_i];
          epoch[rec_i] = data.epoch_mod4[data_i];
          q[rec_i] = data.mspc_q[data_i];
       }
 
       System.out.println("\nSaving MSPC...");
 
       String srcName = 
          "cdf_skels/l2/barCLL_PP_S_l2_mspc_YYYYMMDD_v++.cdf";
       String destName = 
          outputPath  + "/" + date + "/"+ "bar1" + flt + "_" + id + "_" + stn 
          + "_l2_" + "mspc" + "_20" + date +  "_v" + revNum + ".cdf";
 
       CDF_Gen.copyFile(new File(srcName), new File(destName), false);
 
       cdf = CDF_Gen.openCDF(destName);
 
       var = cdf.getVariable("MSPC");
       System.out.println("Spectrum Arrays...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0, 0}, 
          new long[] {48, 1}, 
          new long[] {1, 1}, 
          mspc_rebin
       );
 
       var = cdf.getVariable("FrameGroup");
       System.out.println("FrameGroup...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          frameGroup
       );
 
       var = cdf.getVariable("Epoch");
       System.out.println("Epoch...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          epoch
       );
 
       var = cdf.getVariable("Q");
       System.out.println("Q...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          q
       );
 
       cdf.close();
    }
 
    public void doSspcCdf(int first, int last, int date) throws CDFException{
       CDF cdf;
       Variable var;
       
       double peak = -1, scint_temp = 0, dpu_temp = 0;
       
       int offset = 90;
 
       int numOfRecs = last - first;
       double[][] sspc_rebin = new double[numOfRecs][256];
       double[] old_edges = new double[257];
       double[] std_edges = SpectrumExtract.stdEdges(2, 2.4414);
       
       int[] 
          frameGroup = new int[numOfRecs],
          q = new int[numOfRecs];
       long[] epoch = new long[numOfRecs];
 
       //rebin the sspc spectra
       for(int sspc_rec = 0, hkpg_rec = 0; sspc_rec < numOfRecs; sspc_rec++){
          //get temperatures
          hkpg_rec = sspc_rec * 32 / 40; //convert from mod32 to mod40
          if(data.hkpg_raw[data.T0][hkpg_rec] != 0){
             scint_temp = 
                (data.hkpg_raw[data.T0][hkpg_rec] * data.hkpg_scale[data.T0]) + 
                data.hkpg_offset[data.T0];
          }else{
             scint_temp = 20;
          }
          if(data.hkpg_raw[data.T5][hkpg_rec] != 0){
             dpu_temp = 
                (data.hkpg_raw[data.T5][hkpg_rec] * data.hkpg_scale[data.T5]) + 
                data.hkpg_offset[data.T5];
          }else{
             dpu_temp = 20;
          }
 
          //find the bin that contains the 511 line
          //peak = spectrum.find511(sspc_rebin[sspc_rec], offset);
       
          //get the adjusted bin edges
          old_edges = spectrum.createBinEdges(2, scint_temp, dpu_temp, peak);
          
          //rebin the spectum
          sspc_rebin[sspc_rec] = spectrum.rebin(
             data.sspc_raw[sspc_rec], old_edges, std_edges, 257, 257, false
          );
 
          //divide counts by bin width and convert the time scale to /sec
          for(int bin_i = 0; bin_i < sspc_rebin[sspc_rec].length; bin_i++){
             sspc_rebin[sspc_rec][bin_i] /= 
                std_edges[bin_i + 1] - std_edges[bin_i];
             sspc_rebin[sspc_rec][bin_i] /= 32;
          }
       }
 
       for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
          frameGroup[rec_i] = data.frame_mod32[data_i];
          epoch[rec_i] = data.epoch_mod32[data_i];
          q[rec_i] = data.sspc_q[data_i];
       }
 
       System.out.println("\nSaving SSPC...");
 
       String srcName = 
          "cdf_skels/l2/barCLL_PP_S_l2_sspc_YYYYMMDD_v++.cdf";
       String destName = 
          outputPath + "/" + date + "/" + "bar1" + flt + "_" + id + "_" + stn +
          "_l2_" + "sspc" + "_20" + date +  "_v" + revNum + ".cdf";
       CDF_Gen.copyFile(new File(srcName), new File(destName), false);
 
       cdf = CDF_Gen.openCDF(destName);
 
       var = cdf.getVariable("SSPC");
       System.out.println("Spectrum Arrays...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {256, 1}, 
          new long[] {1}, 
          sspc_rebin
       );
 
       var = cdf.getVariable("FrameGroup");
       System.out.println("FrameGroup...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          frameGroup
       );
 
       var = cdf.getVariable("Epoch");
       System.out.println("Epoch...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          epoch
       );
 
       var = cdf.getVariable("Q");
       System.out.println("Q...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          q
       );
 
       cdf.close();
    }
 
    public void doRcntCdf(int first, int last, int date) throws CDFException{
       CDF cdf;
       Variable var;
       
       int numOfRecs = last - first;
       double[][] rc_timeScaled = new double[4][numOfRecs];
       int[] 
          frameGroup = new int[numOfRecs],
          q = new int[numOfRecs];
       long[] epoch = new long[numOfRecs];
 
       //change all the units from cnts/4sec to cnts/sec
       for(int var_i = 0; var_i < 4; var_i++){
          for(int rec_i = 0; rec_i < numOfRecs; rec_i++){
             rc_timeScaled[var_i][rec_i] = data.rcnt_raw[var_i][rec_i] / 4;
          }
       }
 
       for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
          frameGroup[rec_i] = data.frame_mod4[data_i];
          epoch[rec_i] = data.epoch_mod4[data_i];
          q[rec_i] = data.rcnt_q[data_i];
       }
          
       System.out.println("\nSaving RCNT...");
 
       String srcName = 
          "cdf_skels/l2/barCLL_PP_S_l2_rcnt_YYYYMMDD_v++.cdf";
       String destName = 
          outputPath + "/" + date + "/"  + "bar1" + flt + "_" + id + "_" + stn
          + "_l2_" + "rcnt" + "_20" + date +  "_v" + revNum + ".cdf";
 
       CDF_Gen.copyFile(new File(srcName), new File(destName), false);
 
       cdf = CDF_Gen.openCDF(destName);
 
       var = cdf.getVariable("Interrupt");
       System.out.println("Interrupt...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          rc_timeScaled[0]
       );
 
       var = cdf.getVariable("LowLevel");
       System.out.println("LowLevel...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          rc_timeScaled[1]
       );
 
       var = cdf.getVariable("PeakDet");
       System.out.println("PeakDet...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          rc_timeScaled[2]
       );
 
       var = cdf.getVariable("HighLevel");
       System.out.println("HighLevel...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          rc_timeScaled[3]
       );
 
       var = cdf.getVariable("FrameGroup");
       System.out.println("FrameGroup...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          frameGroup
       );
 
       var = cdf.getVariable("Epoch");
       System.out.println("Epoch...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1},
          epoch
       );
 
       var = cdf.getVariable("Q");
       System.out.println("Q...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          q
       );
 
       cdf.close();
    }
 
    private void writeData() throws CDFException{
       File outDir;
 
       System.out.println(
          "Creating Level Two... (" + data.getSize("1Hz") + " frames)"
       );
       
       //make sure the needed output directories exist
       outDir = new File(outputPath + "/" + (today - 1));
       if(!outDir.exists()){outDir.mkdirs();}
       outDir = new File(outputPath + "/" + today);
       if(!outDir.exists()){outDir.mkdirs();}
       outDir = new File(outputPath + "/" + (today + 1));
       if(!outDir.exists()){outDir.mkdirs();}
 
       //fill CDF files for yesterday, today, and tomorrow
       doAllCdf(today - 1);
       doAllCdf(today);
       doAllCdf(today + 1);
 
       System.out.println("Created Level Two.");
    }
 
    private void doAllCdf(int date) throws CDFException{
       int first_i, last_i;
       long rec_date = 0;
       long[] tt2000_parts; 
 
       //find the first and last indicies for this day for the 1Hz file
       first_i = -1;
       for(last_i = 0; last_i < data.getSize("1Hz"); last_i++){
          tt2000_parts = CDFTT2000.breakdown(data.epoch_1Hz[last_i]);
          rec_date = 
             tt2000_parts[2] + //day
             (100 * tt2000_parts[1]) + //month
             (10000 * (tt2000_parts[0] - 2000)); //year
          if(first_i == -1) {
             if(rec_date == date){
                //found the first_i index
                first_i = last_i;
             }
          }else if(rec_date > date){
             break;
          }
       }
       //make sure we have a valid start and stop index
       if(first_i != -1){
          doPpsCdf(first_i, last_i, date);
       }
 
       //...for the mod4 file
       first_i = -1;
       for(last_i = 0; last_i < data.getSize("mod4"); last_i++){
          tt2000_parts = CDFTT2000.breakdown(data.epoch_mod4[last_i]);
          rec_date = 
             tt2000_parts[2] + //day
             (100 * tt2000_parts[1]) + //month
             (10000 * (tt2000_parts[0] - 2000)); //year
          if(first_i == -1) {
             if(rec_date == date){
                //found the first_i index
                first_i = last_i;
             }
          }else if(rec_date > date){
             break;
          }
       }
       if(first_i != -1){
          doGpsCdf(first_i, last_i, date);
          doMspcCdf(first_i, last_i, date);
          doRcntCdf(first_i, last_i, date);  
       }
 
       //...for the mod32 file
       first_i = -1;
       for(last_i = 0; last_i < data.getSize("mod32"); last_i++){
          tt2000_parts = CDFTT2000.breakdown(data.epoch_mod32[last_i]);
          rec_date = 
             tt2000_parts[2] + //day
             (100 * tt2000_parts[1]) + //month
             (10000 * (tt2000_parts[0] - 2000)); //year
          if(first_i == -1) {
             if(rec_date == date){
                //found the first_i index
                first_i = last_i;
             }
          }else if(rec_date > date){
             break;
          }
       }
       if(first_i != -1){
          doSspcCdf(first_i, last_i, date);  
       }
 
       //...for the mod40 file
       first_i = -1;
       for(last_i = 0; last_i < data.getSize("mod40"); last_i++){
          tt2000_parts = CDFTT2000.breakdown(data.epoch_mod40[last_i]);
          rec_date = 
             tt2000_parts[2] + //day
             (100 * tt2000_parts[1]) + //month
             (10000 * (tt2000_parts[0] - 2000)); //year
          if(first_i == -1) {
             if(rec_date == date){
                //found the first_i index
                first_i = last_i;
             }
          }else if(rec_date > date){
             break;
          }
       }
       if(first_i != -1){
          doHkpgCdf(first_i, last_i, date);  
       }
 
       //...for the 4Hz file
       first_i = -1;
       for(last_i = 0; last_i < data.getSize("4Hz"); last_i++){
          tt2000_parts = CDFTT2000.breakdown(data.epoch_4Hz[last_i]);
          rec_date = 
             tt2000_parts[2] + //day
             (100 * tt2000_parts[1]) + //month
             (10000 * (tt2000_parts[0] - 2000)); //year
          if(first_i == -1) {
             if(rec_date == date){
                //found the first_i index
                first_i = last_i;
             }
          }else if(rec_date > date){
             break;
          }
       }
       if(first_i != -1){
          doMagCdf(first_i, last_i, date);
       }
 
       //...for the 20Hz file
       first_i = -1;
       for(last_i = 0; last_i < data.getSize("20Hz"); last_i++){
          tt2000_parts = CDFTT2000.breakdown(data.epoch_20Hz[last_i]);
          rec_date = 
             tt2000_parts[2] + //day
             (100 * tt2000_parts[1]) + //month
             (10000 * (tt2000_parts[0] - 2000)); //year
          if(first_i == -1) {
             if(rec_date == date){
                //found the first_i index
                first_i = last_i;
             }
          }else if(rec_date > date){
             break;
          }
       }
       if(first_i != -1){
          doFspcCdf(first_i, last_i, date); 
       }
    }
  }
