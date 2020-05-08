 package edu.ucsc.barrel.cdf_gen;
 
 import java.math.BigInteger;
 
 /*
 DataHolder.java 13.02.28
 
 Description:
    Stores the data frames that are being processed
 
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
    v13.02.28
       -Reformatted the data structures to work better with putHyperData function
 
    v13.01.04
       -Changed "ms_since_epoch" or "ms_since_sys_epoch" for clairity
       
    v12.11.27
       -Added members to hold time model info
       -Added holder for quality flag
       -Fixed fspc extraction
       
    v12.11.22
       -Save ms_of_week variable when reading it when processing a gps time frame
 
    v12.11.20
       -Changed references to Level_Generator to CDF_Gen
       -Changed many of the objects to primitave types
       -Changed raw_* to *_raw
       
    v12.11.05
       -Added a number of raw variables so the level 1 files could all
       be int or long values
       -Added static "constant" variables to index different mod values
 
    v12.10.11
       -Takes a BigInteger frame as input, breaks the frame apart, and stores the 
       different data types as public members
 
 */
 
 public class DataHolder{
    ///Largest number of frames we can store.
    //Need to add error checking for to make sure that 
    // the total amount of data never exceeds this
    final static int MAX_FRAMES = 172800;
    
    //Index references for 2d arrays that hold different data types
    static public final int 
       //gps index
       ALT = 0, TIME = 1, LAT = 2, LON = 3, 
       //housekeeping index
       V0 = 0, I0 = 1, V1 = 2, I1 = 3, V2 = 4, I2 = 5, V3 = 6, I3 = 7, V4 = 8, 
       I4 = 9, V5 = 10, I5 = 11, V6 = 12, I6 = 13, V7 = 14, I7 = 15, T0 = 16, 
       T8 = 17, T1 = 18, T9 = 19, T2 = 20, T10 = 21, T3 = 22, T11 = 23, T4 = 24, 
       T12 = 25, T5 = 26, T13 = 27, T6 = 28, T14 = 29, T7 = 30, T15 = 31, 
       V8 = 32, V9 = 33, V10 = 34, V11 = 35, 
       SATSOFF = 36, WEEK = 37, CMDCNT = 38, MDMCNT = 39,
       //rate counter index
       INTER = 0, LL = 1, PD = 2, HL = 3;
   
    static public float[] hkpg_scale = new float[36];
    static public float[] hkpg_offset = new float[36];
    static public String[] hkpg_label = new String[36];
    
    static public final String[] rc_label = {
 	   "Interrupt", "LowLevel", "PeakDet", "HighLevel"
    };
       
    public short[] 
       payID = new short[MAX_FRAMES], 
       ver = new short[MAX_FRAMES],
       sats = new short[MAX_FRAMES / 40],
       offset = new short[MAX_FRAMES / 40],
       termStat = new short[MAX_FRAMES / 40],
       modemCnt = new short[MAX_FRAMES / 40],
       dcdCnt = new short[MAX_FRAMES / 40];
    public long[]
       epoch_1Hz = new long[MAX_FRAMES],
       epoch_4Hz = new long[MAX_FRAMES * 4],
       epoch_20Hz = new long[MAX_FRAMES * 20],
       epoch_mod4 = new long[MAX_FRAMES / 4],
       epoch_mod32 = new long[MAX_FRAMES / 32],
       epoch_mod40 = new long[MAX_FRAMES / 40];
   public int[]
       ms_of_week = new int[MAX_FRAMES / 4];
    public long[][]
       hkpg_raw = new long[40][MAX_FRAMES / 40],
       rcnt_raw = new long[4][MAX_FRAMES / 4];
    public int[][]
       gps_raw = new int[4][MAX_FRAMES / 4];
    public int[]
       magx_raw = new int[MAX_FRAMES * 4],
       magy_raw = new int[MAX_FRAMES * 4],
       magz_raw = new int[MAX_FRAMES * 4];
    public double[]
       time_model_rate = new double[MAX_FRAMES],
       time_model_offset = new double[MAX_FRAMES],
       ms_since_j2000 = new double[MAX_FRAMES];
    public double[][]
       hkpg = new double[36][MAX_FRAMES / 40];
    public int[]
       frame_1Hz = new int[MAX_FRAMES],
       frame_4Hz = new int[MAX_FRAMES * 4],
       frame_20Hz = new int[MAX_FRAMES * 20],
       frame_mod4 = new int[MAX_FRAMES / 4],
       frame_mod32 = new int[MAX_FRAMES / 32],
       frame_mod40 = new int[MAX_FRAMES / 40],
       weeks = new int[MAX_FRAMES / 40],
       pps = new int[MAX_FRAMES],
       cmdCnt = new int[MAX_FRAMES / 40];
    public int[]
       gps_q = new int[MAX_FRAMES / 4],
       pps_q = new int[MAX_FRAMES],
       magn_q = new int[MAX_FRAMES * 4],
       hkpg_q = new int[MAX_FRAMES / 40],
       rcnt_q = new int[MAX_FRAMES / 4],
       fspc_q = new int[MAX_FRAMES * 20],
       mspc_q = new int[MAX_FRAMES / 4],
       sspc_q = new int[MAX_FRAMES / 32],
       time_q = new int[MAX_FRAMES];
    public int[][]
       mspc_raw = new int[MAX_FRAMES / 4][48],
       sspc_raw = new int[MAX_FRAMES / 32][256];
    public int[]
       lc1_raw = new int[MAX_FRAMES * 20],
       lc2_raw = new int[MAX_FRAMES * 20],
       lc3_raw = new int[MAX_FRAMES * 20],
       lc4_raw = new int[MAX_FRAMES * 20];
    public int 
       //record numbers are incrimented on the first record so
       //they start at -1
       rec_num_1Hz = -1, rec_num_4Hz = -1, rec_num_20Hz = -1,
       rec_num_mod4 = -1, rec_num_mod32 = -1, rec_num_mod40 = -1;
    public long firstFC = 0;
 
    public int 
       size_1Hz = 0, size_4Hz = 0, size_20Hz = 0, 
       size_mod4 = 0, size_mod32 = 0, size_mod40 = 0;
 
    public DataHolder(){
       //fill the housekeeping reference arrays
       hkpg_scale[V0] = 0.0003052f;
       hkpg_scale[V1] = 0.0003052f;
       hkpg_scale[V2] = 0.0006104f;
       hkpg_scale[V3] = 0.0001526f;
       hkpg_scale[V4] = 0.0001526f;
       hkpg_scale[V5] = 0.0003052f;
       hkpg_scale[V6] = -0.0001526f;
       hkpg_scale[V7] = -0.0001526f;
       hkpg_scale[V8] = 0.0001526f;
       hkpg_scale[V9] = 0.0006104f;
       hkpg_scale[V10] = 0.0006104f;
       hkpg_scale[V11] = 0.0006104f;
       hkpg_scale[I0] = 0.05086f;
       hkpg_scale[I1] = 0.06104f;
       hkpg_scale[I2] = 0.06104f;
       hkpg_scale[I3] = 0.01017f;
       hkpg_scale[I4] = 0.001017f;
       hkpg_scale[I5] = 0.05086f;
       hkpg_scale[I6] = -0.0001261f;
       hkpg_scale[I7] = -0.001017f;
       hkpg_scale[T0] = 0.007629f;
       hkpg_scale[T1] = 0.007629f;
       hkpg_scale[T2] = 0.007629f;
       hkpg_scale[T3] = 0.007629f;
       hkpg_scale[T4] = 0.007629f;
       hkpg_scale[T5] = 0.007629f;
       hkpg_scale[T6] = 0.007629f;
       hkpg_scale[T7] = 0.007629f;
       hkpg_scale[T8] = 0.007629f;
       hkpg_scale[T9] = 0.007629f;
       hkpg_scale[T10] = 0.007629f;
       hkpg_scale[T11] = 0.007629f;
       hkpg_scale[T12] = 0.007629f;
       hkpg_scale[T13] = 0.0003052f;
       hkpg_scale[T14] = 0.0003052f;
       hkpg_scale[T15] = 0.0001526f;
 
       hkpg_offset[T0] = -273.15f;
       hkpg_offset[T1] = -273.15f;
       hkpg_offset[T2] = -273.15f;
       hkpg_offset[T3] = -273.15f;
       hkpg_offset[T4] = -273.15f;
       hkpg_offset[T5] = -273.15f;
       hkpg_offset[T6] = -273.15f;
       hkpg_offset[T7] = -273.15f;
       hkpg_offset[T8] = -273.15f;
       hkpg_offset[T9] = -273.15f;
       hkpg_offset[T10] = -273.15f;
       hkpg_offset[T11] = -273.15f;
       hkpg_offset[T12] = -273.15f;
 
       hkpg_label[V0] = "V0_VoltAtLoad";
       hkpg_label[V1] = "V1_Battery";
       hkpg_label[V2] = "V2_Solar1";
       hkpg_label[V3] = "V3_POS_DPU";
       hkpg_label[V4] = "V4_POS_XRayDet";
       hkpg_label[V5] = "V5_Modem";
       hkpg_label[V6] = "V6_NEG_XRayDet";
       hkpg_label[V7] = "V7_NEG_DPU";
       hkpg_label[V8] = "V8_Mag";
       hkpg_label[V9] = "V9_Solar2";
       hkpg_label[V10] = "V10_Solar3";
       hkpg_label[V11] = "V11_Solar4";
       hkpg_label[I0] = "I0_TotalLoad";
       hkpg_label[I1] = "I1_TotalSolar";
       hkpg_label[I2] = "I2_Solar1";
       hkpg_label[I3] = "I3_POS_DPU";
       hkpg_label[I4] = "I4_POS_XRayDet";
       hkpg_label[I5] = "I5_Modem";
       hkpg_label[I6] = "I6_NEG_XRayDet";
       hkpg_label[I7] = "I7_NEG_DPU";
       hkpg_label[T0] = "T0_Scint";
       hkpg_label[T1] = "T1_Mag";
       hkpg_label[T2] = "T2_ChargeCont";
       hkpg_label[T3] = "T3_Battery";
       hkpg_label[T4] = "T4_PowerConv";
       hkpg_label[T5] = "T5_DPU";
       hkpg_label[T6] = "T6_Modem";
       hkpg_label[T7] = "T7_Structure";
       hkpg_label[T8] = "T8_Solar1";
       hkpg_label[T9] = "T9_Solar2";
       hkpg_label[T10] = "T10_Solar3";
       hkpg_label[T11] = "T11_Solar4";
       hkpg_label[T12] = "T12_TermTemp";
       hkpg_label[T13] = "T13_TermBatt";
       hkpg_label[T14] = "T14_TermCap";
       hkpg_label[T15] = "T15_CCStat";
    }
 
    public int getSize(String cadence){
       if(cadence == "1Hz"){
          return rec_num_1Hz + 1;
       }else if(cadence == "4Hz"){
          return rec_num_4Hz;
       }else if(cadence == "20Hz"){
          return rec_num_20Hz;
       }else if(cadence == "mod4"){
          return rec_num_mod4;
       }else if(cadence == "mod32"){
          return rec_num_mod32;
       }else{
          return rec_num_mod40;
       }
    }
    
    public void addFrame(BigInteger frame){
       int mod4 = 0, mod32 = 0, mod40 = 0;
       long tmpFC = 0;
       short tmpVer = 0, tmpPayID = 0;
       
       //Breakdown frame counter words: 
       //save the frame counter parts as temp variables,
       //they will be written to the main structure once rec_num is calculated.
       //First 5 bits are version, next 6 are id, last 21 are FC
       tmpVer = 
          frame.shiftRight(1691).and(BigInteger.valueOf(31)).shortValue();
       tmpPayID = 
          frame.shiftRight(1685).and(BigInteger.valueOf(63)).shortValue();
       tmpFC = 
          frame.shiftRight(1664).and(BigInteger.valueOf(2097151)).intValue();
       //get multiplex info
       mod4 = (int)tmpFC % 4;
       mod32 = (int)tmpFC % 32;
       mod40 = (int)tmpFC % 40;
 
       //sets the current record number
       rec_num_1Hz++;
      rec_num_4Hz = (rec_num_1Hz) * 4;
      rec_num_20Hz = (rec_num_1Hz) * 20;
       try{
          if((tmpFC - mod4) != frame_mod4[rec_num_mod4]){rec_num_mod4++;}
          if((tmpFC - mod32) != frame_mod32[rec_num_mod32]){rec_num_mod32++;}
          if((tmpFC - mod40) != frame_mod40[rec_num_mod40]){rec_num_mod40++;}
       }catch(ArrayIndexOutOfBoundsException ex){
          rec_num_mod4 = 0;
          rec_num_mod32 = 0;
          rec_num_mod40 = 0;
       }
 
       //save the info from the frame counter word
       ver[rec_num_1Hz] = tmpVer;
       payID[rec_num_1Hz] = tmpPayID;
       frame_1Hz[rec_num_1Hz] = (int)tmpFC;
       //figure out the other time scale frame counters
      for(int rec_i = rec_num_4Hz; rec_i < rec_num_4Hz + 4; rec_i++){
          frame_4Hz[rec_i] = frame_1Hz[rec_num_1Hz];
       }
      for(int rec_i = rec_num_20Hz; rec_i < rec_num_20Hz + 20; rec_i++){
          frame_20Hz[rec_i] = frame_1Hz[rec_num_1Hz];
       }
      
       //calculate and save the first frame number of the current group
       frame_mod4[rec_num_mod4] = frame_1Hz[rec_num_1Hz] - mod4;
       frame_mod32[rec_num_mod32] = frame_1Hz[rec_num_1Hz] - mod32;
       frame_mod40[rec_num_mod40] = frame_1Hz[rec_num_1Hz] - mod40;
       
       //get gps info: 32 bits of mod4 gps data followed by 16 bits of pps data
       gps_raw[mod4][rec_num_mod4] =
          frame.shiftRight(1632).and(BigInteger.valueOf(4294967295L)).
             intValue();
 
       //save the time variable separately for the epoch calculation 
       if(mod4 == 1){
          ms_of_week[rec_num_mod4] = gps_raw[mod4][rec_num_mod4];
       }
 
       //fill the quality flag with a 0 for now
       gps_q[rec_num_mod4] = 0;
 
       //GPS PPS
       pps[rec_num_1Hz] = 
          frame.shiftRight(1616).and(BigInteger.valueOf(65535)).intValue();
       pps_q[rec_num_1Hz] = 0;
       
       //mag data 4 sets of xyz vectors. 24 bits/component
       magx_raw[rec_num_4Hz] = 
          frame.shiftRight(1592).and(BigInteger.valueOf(16777215)).intValue();
       magy_raw[rec_num_4Hz] = 
          frame.shiftRight(1568).and(BigInteger.valueOf(16777215)).intValue();
       magz_raw[rec_num_4Hz] = 
          frame.shiftRight(1544).and(BigInteger.valueOf(16777215)).intValue();
       magx_raw[rec_num_4Hz + 1] = 
          frame.shiftRight(1520).and(BigInteger.valueOf(16777215)).intValue();
       magy_raw[rec_num_4Hz + 1] = 
          frame.shiftRight(1496).and(BigInteger.valueOf(16777215)).intValue();
       magz_raw[rec_num_4Hz + 1] = 
          frame.shiftRight(1472).and(BigInteger.valueOf(16777215)).intValue();
       magx_raw[rec_num_4Hz + 2] = 
          frame.shiftRight(1448).and(BigInteger.valueOf(16777215)).intValue();
       magy_raw[rec_num_4Hz + 2] = 
          frame.shiftRight(1424).and(BigInteger.valueOf(16777215)).intValue();
       magz_raw[rec_num_4Hz + 2] = 
          frame.shiftRight(1400).and(BigInteger.valueOf(16777215)).intValue();
       magx_raw[rec_num_4Hz + 3] = 
          frame.shiftRight(1376).and(BigInteger.valueOf(16777215)).intValue();
       magy_raw[rec_num_4Hz + 3] = 
          frame.shiftRight(1352).and(BigInteger.valueOf(16777215)).intValue();
       magz_raw[rec_num_4Hz + 3] = 
          frame.shiftRight(1328).and(BigInteger.valueOf(16777215)).intValue();
       magn_q[rec_num_1Hz] = 0;
       
       //mod40 housekeeping data: 16bits
       hkpg_raw[mod40][rec_num_mod40] = 
          frame.shiftRight(1312).and(BigInteger.valueOf(65535)).longValue();
       switch(mod40){
          case 36:
             sats[rec_num_mod40] = 
                (short)(hkpg_raw[mod40][rec_num_mod40] >> 8);
             offset[rec_num_mod40] = 
                (short)(hkpg_raw[mod40][rec_num_mod40] & 255);
             break;
          case 37:
             weeks[rec_num_mod40] = 
                (int)hkpg_raw[mod40][rec_num_mod40];
             break;
          case 38:
             termStat[rec_num_mod40] = 
                (short)(hkpg_raw[mod40][rec_num_mod40] >> 15);
             cmdCnt[rec_num_mod40] = 
                (int)(hkpg_raw[mod40][rec_num_mod40] & 32768);
             break;
          case 39:
             dcdCnt[rec_num_mod40] = 
                (short)(hkpg_raw[mod40][rec_num_mod40] >> 8);
             modemCnt[rec_num_mod40] = 
                (short)(hkpg_raw[mod40][rec_num_mod40] & 255);
             break;
          default:
             break;
       }
       hkpg_q[rec_num_mod40] = 0;
          
       //fast spectra: 20 sets of 4 channel data. 
       //ch1 and ch2 are 16 bits, ch3 and ch4 are 8bits 
       for(int lc_i = 0; lc_i < 20; lc_i++){
          lc1_raw[rec_num_20Hz + lc_i] =
             frame.shiftRight(1296 - (48 * lc_i))
                .and(BigInteger.valueOf(65535)).intValue();
          lc2_raw[rec_num_20Hz + lc_i] =
             frame.shiftRight(1280 - (48 * lc_i))
                .and(BigInteger.valueOf(65535)).intValue();
          lc3_raw[rec_num_20Hz + lc_i] =
             frame.shiftRight(1272 - (48 * lc_i))
                .and(BigInteger.valueOf(255)).intValue();
          lc4_raw[rec_num_20Hz + lc_i] =
             frame.shiftRight(1264 - (48 * lc_i))
                .and(BigInteger.valueOf(255)).intValue();
       }
       fspc_q[rec_num_1Hz] = 0;
        
       //medium spectra: 12 channels per frame, 16 bits/channels
       for(int mspc_i = 0; mspc_i < 12; mspc_i++){
          mspc_raw[rec_num_mod4][(mod4 * 12) + mspc_i] =
                frame.shiftRight(336 - (16 * mspc_i))
                   .and(BigInteger.valueOf(65535)).intValue();
       }
       mspc_q[rec_num_mod4] = 0;
 
       //slow spectra: 8 channels per frame, 16 bits/channels
       for(int sspc_i = 0; sspc_i < 8; sspc_i++){
          sspc_raw[rec_num_mod32][(mod32 * 8) + sspc_i] =
                frame.shiftRight(144 - (16 * sspc_i))
                   .and(BigInteger.valueOf(65535)).intValue();
       }
       sspc_q[rec_num_mod32] = 0;
       
       //rate counter: mod4 data, 16bits
       rcnt_raw[mod4][rec_num_mod4] = 
          frame.shiftRight(16).and(BigInteger.valueOf(65535)).longValue();
       rcnt_q[rec_num_mod4] = 0;
    }
 
    public void finalizeFrames(){
       //Sorts through each of the CDF Variable arrays and removes frame gaps
       //Sets the size properties
       
       int old_i, new_i;
 
       //start with 1Hz data
       //we assume there is no data in the rec_i=0 slot
       for(old_i = 1, new_i = 0; old_i < frame_1Hz.length; old_i++){
          if(frame_1Hz[old_i] > 0){
             //the next data point was found at the old_i index,
             //copy it to the new_i index of all the 1Hz arrays
             //and clear the data from the old_i position
             frame_1Hz[new_i] = frame_1Hz[old_i];
             frame_1Hz[old_i] = 0;
             pps[new_i] = pps[old_i];
             pps[old_i] = 0;
             ver[new_i] = ver[old_i];
             ver[old_i] = 0;
             payID[new_i] = payID[old_i];
             payID[old_i] = 0;
             pps_q[new_i] = pps_q[old_i];
             pps_q[old_i] = 0;
 
             //incriment the new array index
             new_i++;
          }
       }
       size_1Hz = new_i;
 
       for(old_i = 1, new_i = 0; old_i < frame_4Hz.length; old_i++){
          if(frame_4Hz[old_i] > 0){
             frame_4Hz[new_i] = frame_4Hz[old_i];
             frame_4Hz[old_i] = 0;
             magx_raw[new_i] = magx_raw[old_i];
             magx_raw[old_i] = 0;
             magy_raw[new_i] = magy_raw[old_i];
             magy_raw[old_i] = 0;
             magz_raw[new_i] = magz_raw[old_i];
             magz_raw[old_i] = 0;
             magn_q[new_i] = magn_q[old_i];
             magn_q[old_i] = 0;
             new_i++;
          }
       }
       size_4Hz = new_i;
 
       for(old_i = 1, new_i = 0; old_i < frame_20Hz.length; old_i++){
          if(frame_20Hz[old_i] > 0){
             frame_20Hz[new_i] = frame_20Hz[old_i];
             frame_20Hz[old_i] = 0;
             lc1_raw[new_i] = lc1_raw[old_i];
             lc1_raw[old_i] = 0;
             lc2_raw[new_i] = lc2_raw[old_i];
             lc2_raw[old_i] = 0;
             lc3_raw[new_i] = lc3_raw[old_i];
             lc3_raw[old_i] = 0;
             lc4_raw[new_i] = lc4_raw[old_i];
             lc4_raw[old_i] = 0;
             fspc_q[new_i] = fspc_q[old_i];
             fspc_q[old_i] = 0;
             new_i++;
          }
       }
       size_20Hz = new_i;
 
       for(old_i = 1, new_i = 0; old_i < frame_mod4.length; old_i++){
          if(frame_mod4[old_i] > 0){
             frame_mod4[new_i] = frame_mod4[old_i];
             frame_mod4[old_i] = 0;
             gps_raw[0][new_i] = gps_raw[0][old_i];
             gps_raw[0][old_i] = 0;
             gps_raw[1][new_i] = gps_raw[1][old_i];
             gps_raw[1][old_i] = 0;
             gps_raw[2][new_i] = gps_raw[2][old_i];
             gps_raw[2][old_i] = 0;
             gps_raw[3][new_i] = gps_raw[3][old_i];
             gps_raw[3][old_i] = 0;
             gps_q[new_i] = gps_q[old_i];
             gps_q[old_i] = 0;
             rcnt_raw[0][new_i] = rcnt_raw[0][old_i];
             rcnt_raw[0][old_i] = 0;
             rcnt_raw[1][new_i] = rcnt_raw[1][old_i];
             rcnt_raw[1][old_i] = 0;
             rcnt_raw[2][new_i] = rcnt_raw[2][old_i];
             rcnt_raw[2][old_i] = 0;
             rcnt_raw[3][new_i] = rcnt_raw[3][old_i];
             rcnt_raw[3][old_i] = 0;
             rcnt_q[new_i] = rcnt_q[old_i];
             rcnt_q[old_i] = 0;
             mspc_raw[new_i] = mspc_raw[old_i];
             mspc_raw[old_i] = new int[48];
             mspc_q[new_i] = mspc_q[old_i];
             mspc_q[old_i] = 0;
             new_i++;
          }
       }
       size_mod4 = new_i;
 
       for(old_i = 1, new_i = 0; old_i < frame_mod32.length; old_i++){
          if(frame_mod32[old_i] > 0){
             frame_mod32[new_i] = frame_mod32[old_i];
             frame_mod32[old_i] = 0;
             sspc_raw[new_i] = sspc_raw[old_i];
             sspc_raw[old_i] = new int[256];
             sspc_q[new_i] = sspc_q[old_i];
             sspc_q[old_i] = 0;
             new_i++;
          }
       }
       size_mod32 = new_i;
 
       for(old_i = 1, new_i = 0; old_i < frame_mod40.length; old_i++){
          if(frame_mod40[old_i] > 0){
             frame_mod40[new_i] = frame_mod40[old_i];
             frame_mod40[old_i] = 0;
 
             //loop through all of the houskeeping values
             for(int hkpg_i = 0; hkpg_i < 40; hkpg_i++){
                hkpg_raw[hkpg_i][new_i] = hkpg_raw[hkpg_i][old_i];
                hkpg_raw[hkpg_i][old_i] = 0; 
             }
             weeks[new_i] = weeks[old_i];
             weeks[old_i] = 0;
             cmdCnt[new_i] = cmdCnt[old_i];
             cmdCnt[old_i] = 0;
             sats[new_i] = sats[old_i];
             sats[old_i] = 0;
             offset[new_i] = offset[old_i];
             offset[old_i] = 0;
             termStat[new_i] = termStat[old_i];
             termStat[old_i] = 0;
             modemCnt[new_i] = modemCnt[old_i];
             modemCnt[old_i] = 0;
             dcdCnt[new_i] = dcdCnt[old_i];
             dcdCnt[old_i] = 0;
             hkpg_q[new_i] = hkpg_q[old_i];
             hkpg_q[old_i] = 0;
             new_i++;
          }
       }
       size_mod40 = new_i;
    }
 }
