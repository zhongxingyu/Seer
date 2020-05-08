 /*
 LevelOne.java
 
 Description:
    Creates level one CDF files from DataHolder.java object.
 
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
 */
 
 package edu.ucsc.barrel.cdf_gen;
 
 import gsfc.nssdc.cdf.CDF;
 import gsfc.nssdc.cdf.CDFException;
 import gsfc.nssdc.cdf.util.CDFTT2000;
 import gsfc.nssdc.cdf.Variable;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Vector;
 import java.util.Arrays;
 
 public class LevelOne{
    String outputPath;
    int lastFrame = -1;
    long ms_of_week = 0;
    int weeks = 0;
    String
       id = "00",
       flt = "00",
       stn = "0",
       revNum = "00";
    int today, yesterday, tomorrow;
    Calendar dateObj = Calendar.getInstance();
    
    SpectrumExtract spectrum;
    
    short INCOMPLETE_GROUP = 8196;
    
    private DataHolder data;
    
    public LevelOne(
       final String d, final String p, 
       final String f, final String s
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
 
       //calculate yesterday and tomorrow from today's date
       int year, month, day;
       year = today/10000;
       month = (today - (year * 10000)) / 100;
       day = today - (year * 10000) - (month * 100);
       dateObj.clear();
       dateObj.set(year, month - 1, day);
       dateObj.add(Calendar.DATE, -1);
 
       yesterday = 
          (dateObj.get(Calendar.YEAR) * 10000) + 
          ((dateObj.get(Calendar.MONTH) + 1) * 100) + 
          dateObj.get(Calendar.DATE);
 
       dateObj.add(Calendar.DATE, 2);
 
       tomorrow = 
          (dateObj.get(Calendar.YEAR) * 10000) + 
          ((dateObj.get(Calendar.MONTH) + 1) * 100) + 
          dateObj.get(Calendar.DATE);
 
       //get the data storage object
       data = CDF_Gen.getDataSet();
      
       //create the spectrum rebinning object
       spectrum = new SpectrumExtract();
      
       //set output path
       outputPath = CDF_Gen.L1_Dir;
       
       //get data from DataHolder and save them to CDF files
       try{
          writeData();
       }catch(CDFException ex){
          System.out.println(ex.getMessage());
       }
    }
    
    //Saveve the EPHM data to CDF the file
    public void doGpsCdf(int first, int last, int date) throws CDFException{
       CDF cdf;
       Variable var;
       int numOfRecs = last - first;
       int[] 
          frameGroup = new int[numOfRecs],
          q = new int[numOfRecs]; 
       long[] epoch = new long[numOfRecs];
       int[][] gps = new int[4][numOfRecs];
 
       System.out.println("\nSaving EPHM Level One CDF...");
 
       //select values for this date
       for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
         //convert lat and lon to physical units
         gps[0][rec_i] = data.gps_raw[0][data_i];
         gps[1][rec_i] = data.gps_raw[1][data_i];
         gps[2][rec_i] = data.gps_raw[2][data_i];
         gps[3][rec_i] = data.gps_raw[3][data_i];
         frameGroup[rec_i] = data.frame_mod4[data_i];
         epoch[rec_i] = data.epoch_mod4[data_i];
         q[rec_i] = data.gps_q[data_i];
       }
 
       //make sure there is a CDF file to open
       //(CDF_Gen.copyFile will not clobber an existing file)
       String srcName = 
          "cdf_skels/l1/barCLL_PP_S_l1_ephm_YYYYMMDD_v++.cdf";
       String destName = 
          outputPath + "/" + date + "/" + "bar1" + flt + "_" + id + "_" + stn + 
          "_l1_" + "ephm" + "_20" + date +  "_v" + revNum + ".cdf";
 
       CDF_Gen.copyFile(new File(srcName), new File(destName), false);
 
       //open EPHM CDF and save the reference in the cdf variable
       cdf = CDF_Gen.openCDF(destName);
       
       var = cdf.getVariable("GPS_Alt");
       System.out.println("GPS_Alt...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1,
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          gps[0]
       );
 
       var = cdf.getVariable("ms_of_week");
       System.out.println("ms_of_week...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          gps[1]
       );
 
       var = cdf.getVariable("GPS_Lat");
       System.out.println("GPS_Lat...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          gps[2] 
       );
 
       var = cdf.getVariable("GPS_Lon");
       System.out.println("GPS_Lon...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          gps[3]
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
 
       System.out.println("Done with EPHM!");
       //close current cdf
       cdf.close();
    }
    
    //write the pps file
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
 
       System.out.println("\nSaving PPS Level One CDF...");
 
       for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
         pps[rec_i] = data.pps[data_i];
         version[rec_i] = data.ver[data_i];
         payID[rec_i] = data.payID[data_i];
         frameGroup[rec_i] = data.frame_1Hz[data_i];
         epoch[rec_i] = data.epoch_1Hz[data_i];
         q[rec_i] = data.pps_q[data_i];
       }
 
       String srcName = 
          "cdf_skels/l1/barCLL_PP_S_l1_pps-_YYYYMMDD_v++.cdf";
       String destName = 
          outputPath  + "/" + date + "/" + "bar1" + flt + "_" + id + "_" + stn + 
          "_l1_" + "pps-" + "_20" + date +  "_v" + revNum + ".cdf";
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
          q = new int[numOfRecs], 
          magx = new int[numOfRecs],
          magy = new int[numOfRecs],
          magz = new int[numOfRecs];
       long[] epoch = new long[numOfRecs];
 
       System.out.println("\nSaving Magnetometer Level One CDF...");
 
       String srcName = 
          "cdf_skels/l1/barCLL_PP_S_l1_magn_YYYYMMDD_v++.cdf";
       String destName = 
          outputPath + "/" + date + "/" + "bar1" + flt + "_" + id + "_" + stn + 
          "_l1_" + "magn" + "_20" + date +  "_v" + revNum + ".cdf";
       CDF_Gen.copyFile(new File(srcName), new File(destName), false);
 
       cdf = CDF_Gen.openCDF(destName);
      
       for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
          magx[rec_i] = data.magx_raw[data_i];
          magy[rec_i] = data.magy_raw[data_i];
          magz[rec_i] = data.magz_raw[data_i];
          frameGroup[rec_i] = data.frame_4Hz[data_i];
          epoch[rec_i] = data.epoch_4Hz[data_i];
          q[rec_i] = data.magn_q[data_i];
       }
 
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
       long[] hkpg = new long[numOfRecs];
 
       System.out.println("\nSaving HKPG...");
 
       String srcName = 
          "cdf_skels/l1/barCLL_PP_S_l1_hkpg_YYYYMMDD_v++.cdf";
       String destName = 
          outputPath + "/" + date + "/" + "bar1" + flt + "_" + id + "_" + stn 
          + "_l1_" + "hkpg" + "_20" + date +  "_v" + revNum + ".cdf";
 
       CDF_Gen.copyFile(new File(srcName), new File(destName), false);
 
       cdf = CDF_Gen.openCDF(destName);
          
       for(int var_i = 0; var_i < data.hkpg.length; var_i++){
          hkpg = new long[numOfRecs];
          for(int rec_i = 0; rec_i < numOfRecs; rec_i++){
             hkpg[rec_i] =  data.hkpg_raw[var_i][rec_i];
          }
 
          var = cdf.getVariable(data.hkpg_label[var_i]);
          System.out.println(data.hkpg_label[var_i] + "...");
          var.putHyperData(
             var.getNumWrittenRecords(), numOfRecs, 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             hkpg
          );
       }
 
       for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
          sats[rec_i] = data.sats[data_i];
          offset[rec_i] = data.offset[data_i];
          termStat[rec_i] = data.termStat[data_i];
          modemCnt[rec_i] = data.modemCnt[data_i];
          dcdCnt[rec_i] = data.dcdCnt[data_i];
          cmdCnt[rec_i] = data.cmdCnt[data_i];
          frameGroup[rec_i] = data.frame_mod40[data_i];
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
 
       int[] 
          frameGroup = new int[numOfRecs],
          q = new int[numOfRecs];
       long[] epoch = new long[numOfRecs];
       int[][]
          lc = new int[4][numOfRecs];
 
       System.out.println("\nSaving FSPC...");
 
       String srcName = 
          "cdf_skels/l1/barCLL_PP_S_l1_fspc_YYYYMMDD_v++.cdf";
       String destName = 
          outputPath + "/" + date + "/" + "bar1" + flt + "_" + id + "_" + stn 
          + "_l1_" + "fspc" + "_20" + date +  "_v" + revNum + ".cdf";
       CDF_Gen.copyFile(new File(srcName), new File(destName), false);
 
       cdf = CDF_Gen.openCDF(destName);
       
       for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
          frameGroup[rec_i] = data.frame_20Hz[data_i];
          epoch[rec_i] = data.epoch_20Hz[data_i];
          q[rec_i] = data.fspc_q[data_i];
          lc[0][rec_i] = data.lc1_raw[data_i];
          lc[1][rec_i] = data.lc2_raw[data_i];
          lc[2][rec_i] = data.lc3_raw[data_i];
          lc[3][rec_i] = data.lc4_raw[data_i];
       }
 
       var = cdf.getVariable("LC1");
       System.out.println("LC1...");
       var.putHyperData(
         0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          lc[0]
       );
       
       var = cdf.getVariable("LC2");
       System.out.println("LC2...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          lc[1]
       );
 
       var = cdf.getVariable("LC3");
       System.out.println("LC3...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          lc[2]
       );
 
       var = cdf.getVariable("LC4");
       System.out.println("LC4...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          lc[3]
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
       
       int numOfRecs = last - first;
       int[] 
          frameGroup = new int[numOfRecs],
          q = new int[numOfRecs];
       long[] epoch = new long[numOfRecs];
 
       int[][] mspc = new int[numOfRecs][48];
       
       for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
          frameGroup[rec_i] = data.frame_mod4[data_i];
          epoch[rec_i] = data.epoch_mod4[data_i];
          q[rec_i] = data.mspc_q[data_i];
          mspc[rec_i] = data.mspc_raw[data_i];
       }
 
       System.out.println("\nSaving MSPC...");
 
       String srcName = 
          "cdf_skels/l1/barCLL_PP_S_l1_mspc_YYYYMMDD_v++.cdf";
       String destName = 
          outputPath  + "/" + date + "/"+ "bar1" + flt + "_" + id + "_" + stn 
          + "_l1_" + "mspc" + "_20" + date +  "_v" + revNum + ".cdf";
 
       CDF_Gen.copyFile(new File(srcName), new File(destName), false);
 
       cdf = CDF_Gen.openCDF(destName);
 
       var = cdf.getVariable("MSPC");
       System.out.println("Spectrum Arrays...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0, 0}, 
          new long[] {48, 1}, 
          new long[] {1, 1}, 
          mspc
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
       
       int numOfRecs = last - first;
       int[] 
          frameGroup = new int[numOfRecs],
          q = new int[numOfRecs];
       long[] epoch = new long[numOfRecs];
       int[][] sspc = new int[numOfRecs][256];
 
       for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
          frameGroup[rec_i] = data.frame_mod32[data_i];
          epoch[rec_i] = data.epoch_mod32[data_i];
          q[rec_i] = data.sspc_q[data_i];
          sspc[rec_i] = data.sspc_raw[data_i];
       }
 
       System.out.println("\nSaving SSPC...");
 
       String srcName = 
          "cdf_skels/l1/barCLL_PP_S_l1_sspc_YYYYMMDD_v++.cdf";
       String destName = 
          outputPath + "/" + date + "/" + "bar1" + flt + "_" + id + "_" + stn +
          "_l1_" + "sspc" + "_20" + date +  "_v" + revNum + ".cdf";
       CDF_Gen.copyFile(new File(srcName), new File(destName), false);
 
       cdf = CDF_Gen.openCDF(destName);
 
       var = cdf.getVariable("SSPC");
       System.out.println("Spectrum Arrays...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {256, 1}, 
          new long[] {1}, 
          sspc
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
       int[] 
          frameGroup = new int[numOfRecs],
          q = new int[numOfRecs];
       long[] epoch = new long[numOfRecs];
       long[][] rc = new long[4][numOfRecs];
 
       for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
          frameGroup[rec_i] = data.frame_mod4[data_i];
          epoch[rec_i] = data.epoch_mod4[data_i];
          q[rec_i] = data.rcnt_q[data_i];
          rc[0][rec_i] = data.rcnt_raw[0][rec_i];
          rc[1][rec_i] = data.rcnt_raw[1][rec_i];
          rc[2][rec_i] = data.rcnt_raw[2][rec_i];
          rc[3][rec_i] = data.rcnt_raw[3][rec_i];
       }
          
       System.out.println("\nSaving RCNT...");
 
       String srcName = 
          "cdf_skels/l1/barCLL_PP_S_l1_rcnt_YYYYMMDD_v++.cdf";
       String destName = 
          outputPath + "/" + date + "/"  + "bar1" + flt + "_" + id + "_" + stn
          + "_l1_" + "rcnt" + "_20" + date +  "_v" + revNum + ".cdf";
 
       CDF_Gen.copyFile(new File(srcName), new File(destName), false);
 
       cdf = CDF_Gen.openCDF(destName);
 
       var = cdf.getVariable("Interrupt");
       System.out.println("Interrupt...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          rc[0]
       );
 
       var = cdf.getVariable("LowLevel");
       System.out.println("LowLevel...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          rc[1]
       );
 
       var = cdf.getVariable("PeakDet");
       System.out.println("PeakDet...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          rc[2]
       );
 
       var = cdf.getVariable("HighLevel");
       System.out.println("HighLevel...");
       var.putHyperData(
          var.getNumWrittenRecords(), numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          rc[3]
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
          "Creating Level One... (" + data.getSize("1Hz") + " frames)"
       );
       
       //make sure the needed output directories exist
       outDir = new File(outputPath + "/" + yesterday);
       if(!outDir.exists()){outDir.mkdirs();}
       outDir = new File(outputPath + "/" + today);
       if(!outDir.exists()){outDir.mkdirs();}
       outDir = new File(outputPath + "/" + tomorrow);
       if(!outDir.exists()){outDir.mkdirs();}
 
       //fill CDF files for yesterday, today, and tomorrow
       doAllCdf(yesterday);
       doAllCdf(today);
       doAllCdf(tomorrow);
 
       System.out.println("Created Level One.");
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
