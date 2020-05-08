 /*
 DataHolder.java
 
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
 */
 
 package edu.ucsc.barrel.cdf_gen;
 
 import java.math.BigInteger;
 
 public class DataHolder{
    ///Largest number of frames we can store.
    //Need to add error checking for to make sure that 
    // the total amount of data never exceeds this
    final static int MAX_FRAMES = 172800;
    
    static public float[] hkpg_scale = new float[36];
    static public float[] hkpg_offset = new float[36];
    static public String[] hkpg_label = new String[36];
    
    static public final String[] rc_label = {
 	   "Interrupt", "LowLevel", "PeakDet", "HighLevel"
    };
       
    private float min_alt;
    private boolean low_alt = true;
 
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
       time_model_slope = new double[MAX_FRAMES],
       time_model_intercept = new double[MAX_FRAMES];
    public double[][]
       hkpg = new double[36][MAX_FRAMES / 40];
    public int[]
       frame_1Hz = new int[MAX_FRAMES],
       frame_4Hz = new int[MAX_FRAMES * 4],
       frame_20Hz = new int[MAX_FRAMES * 20],
       frame_mod4 = new int[MAX_FRAMES / 4],
       frame_mod32 = new int[MAX_FRAMES / 32],
       frame_mod40 = new int[MAX_FRAMES / 40];
    public int[] 
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
       hkpg_scale[Constants.V0] = 0.0003052f;
       hkpg_scale[Constants.V1] = 0.0003052f;
       hkpg_scale[Constants.V2] = 0.0006104f;
       hkpg_scale[Constants.V3] = 0.0001526f;
       hkpg_scale[Constants.V4] = 0.0001526f;
       hkpg_scale[Constants.V5] = 0.0003052f;
       hkpg_scale[Constants.V6] = -0.0001526f;
       hkpg_scale[Constants.V7] = -0.0001526f;
       hkpg_scale[Constants.V8] = 0.0001526f;
       hkpg_scale[Constants.V9] = 0.0006104f;
       hkpg_scale[Constants.V10] = 0.0006104f;
       hkpg_scale[Constants.V11] = 0.0006104f;
       hkpg_scale[Constants.I0] = 0.05086f;
       hkpg_scale[Constants.I1] = 0.06104f;
       hkpg_scale[Constants.I2] = 0.06104f;
       hkpg_scale[Constants.I3] = 0.01017f;
       hkpg_scale[Constants.I4] = 0.001017f;
       hkpg_scale[Constants.I5] = 0.05086f;
       hkpg_scale[Constants.I6] = -0.0001261f;
       hkpg_scale[Constants.I7] = -0.001017f;
       hkpg_scale[Constants.T0] = 0.007629f;
       hkpg_scale[Constants.T1] = 0.007629f;
       hkpg_scale[Constants.T2] = 0.007629f;
       hkpg_scale[Constants.T3] = 0.007629f;
       hkpg_scale[Constants.T4] = 0.007629f;
       hkpg_scale[Constants.T5] = 0.007629f;
       hkpg_scale[Constants.T6] = 0.007629f;
       hkpg_scale[Constants.T7] = 0.007629f;
       hkpg_scale[Constants.T8] = 0.007629f;
       hkpg_scale[Constants.T9] = 0.007629f;
       hkpg_scale[Constants.T10] = 0.007629f;
       hkpg_scale[Constants.T11] = 0.007629f;
       hkpg_scale[Constants.T12] = 0.007629f;
       hkpg_scale[Constants.T13] = 0.0003052f;
       hkpg_scale[Constants.T14] = 0.0003052f;
       hkpg_scale[Constants.T15] = 0.0001526f;
 
       hkpg_offset[Constants.T0] = -273.15f;
       hkpg_offset[Constants.T1] = -273.15f;
       hkpg_offset[Constants.T2] = -273.15f;
       hkpg_offset[Constants.T3] = -273.15f;
       hkpg_offset[Constants.T4] = -273.15f;
       hkpg_offset[Constants.T5] = -273.15f;
       hkpg_offset[Constants.T6] = -273.15f;
       hkpg_offset[Constants.T7] = -273.15f;
       hkpg_offset[Constants.T8] = -273.15f;
       hkpg_offset[Constants.T9] = -273.15f;
       hkpg_offset[Constants.T10] = -273.15f;
       hkpg_offset[Constants.T11] = -273.15f;
       hkpg_offset[Constants.T12] = -273.15f;
 
       hkpg_label[Constants.V0] = "V0_VoltAtLoad";
       hkpg_label[Constants.V1] = "V1_Battery";
       hkpg_label[Constants.V2] = "V2_Solar1";
       hkpg_label[Constants.V3] = "V3_POS_DPU";
       hkpg_label[Constants.V4] = "V4_POS_XRayDet";
       hkpg_label[Constants.V5] = "V5_Modem";
       hkpg_label[Constants.V6] = "V6_NEG_XRayDet";
       hkpg_label[Constants.V7] = "V7_NEG_DPU";
       hkpg_label[Constants.V8] = "V8_Mag";
       hkpg_label[Constants.V9] = "V9_Solar2";
       hkpg_label[Constants.V10] = "V10_Solar3";
       hkpg_label[Constants.V11] = "V11_Solar4";
       hkpg_label[Constants.I0] = "I0_TotalLoad";
       hkpg_label[Constants.I1] = "I1_TotalSolar";
       hkpg_label[Constants.I2] = "I2_Solar1";
       hkpg_label[Constants.I3] = "I3_POS_DPU";
       hkpg_label[Constants.I4] = "I4_POS_XRayDet";
       hkpg_label[Constants.I5] = "I5_Modem";
       hkpg_label[Constants.I6] = "I6_NEG_XRayDet";
       hkpg_label[Constants.I7] = "I7_NEG_DPU";
       hkpg_label[Constants.T0] = "T0_Scint";
       hkpg_label[Constants.T1] = "T1_Mag";
       hkpg_label[Constants.T2] = "T2_ChargeCont";
       hkpg_label[Constants.T3] = "T3_Battery";
       hkpg_label[Constants.T4] = "T4_PowerConv";
       hkpg_label[Constants.T5] = "T5_DPU";
       hkpg_label[Constants.T6] = "T6_Modem";
       hkpg_label[Constants.T7] = "T7_Structure";
       hkpg_label[Constants.T8] = "T8_Solar1";
       hkpg_label[Constants.T9] = "T9_Solar2";
       hkpg_label[Constants.T10] = "T10_Solar3";
       hkpg_label[Constants.T11] = "T11_Solar4";
       hkpg_label[Constants.T12] = "T12_TermTemp";
       hkpg_label[Constants.T13] = "T13_TermBatt";
       hkpg_label[Constants.T14] = "T14_TermCap";
       hkpg_label[Constants.T15] = "T15_CCStat";
 
       //fill all of the storage arrays with fill values
       for(int i = 0; i < MAX_FRAMES; i++){
          payID[i] = Constants.PAYID_FILL;
          ver[i] = Constants.VER_FILL;
          frame_1Hz[i] = Constants.FC_FILL;
          pps[i] = Constants.PPS_FILL;
 
          for(int j = 0; j < 4; j++){
             frame_4Hz[(i * 4) + j] = Constants.FC_FILL;
             magx_raw[(i * 4) + j] = Constants.MAG_FILL;
             magy_raw[(i * 4) + j] = Constants.MAG_FILL;
             magz_raw[(i * 4) + j] = Constants.MAG_FILL;
          }
          for(int j = 0; j < 20; j++){
             frame_20Hz[(i * 20) + j] = Constants.UINT2_FILL;
             lc1_raw[(i * 20) + j] = Constants.FSPC_RAW_FILL;
             lc2_raw[(i * 20) + j] = Constants.FSPC_RAW_FILL;
             lc3_raw[(i * 20) + j] = Constants.FSPC_RAW_FILL;
             lc4_raw[(i * 20) + j] = Constants.FSPC_RAW_FILL;
          }
       }
       for(int i = 0; i < (MAX_FRAMES / 4); i++){
          frame_mod4[i] = Constants.FC_FILL;
          ms_of_week[i] = Constants.MS_WEEK_FILL;
         gps_raw[Constants.TIME_I][i] = Constants.MS_WEEK_FILL;
          gps_raw[Constants.LAT_I][i] = Constants.LAT_RAW_FILL;
          gps_raw[Constants.LON_I][i] = Constants.LON_RAW_FILL;
          gps_raw[Constants.ALT_I][i] = Constants.ALT_RAW_FILL;
 
          for(int j = 0; j < 4; j++){
             rcnt_raw[j][i] = Constants.RCNT_FILL;
          }
          for(int j = 0; j < 48; j++){
             mspc_raw[i][j] = Constants.MSPC_RAW_FILL;
          }
       }
       for(int i = 0; i < (MAX_FRAMES / 32); i++){
          frame_mod32[i] = Constants.FC_FILL;
          for(int j = 0; j < 256; j++){
             sspc_raw[i][j] = Constants.SSPC_RAW_FILL;
          }
       }
       for(int i = 0; i < (MAX_FRAMES / 40); i++){
          frame_mod40[i] = Constants.FC_FILL;
          sats[i] = Constants.SATS_FILL;
          offset[i] = Constants.LEAP_SEC_FILL;
          termStat[i] = Constants.TERM_STAT_FILL;
          modemCnt[i] = Constants.MODEM_CNT_FILL;
          dcdCnt[i] = Constants.DCD_CNT_FILL;
          weeks[i] = Constants.WEEKS_FILL;
          cmdCnt[i] = Constants.CMD_CNT_FILL;
 
          for(int j = 0; j < 36; j++){
             hkpg[j][i] = Constants.DOUBLE_FILL;
             hkpg_raw[j][i] = Constants.HKPG_FILL;
          }
       }
 
       //set minimum altitude based on either command line argument or
       //default setting in the Constants class
       if(CDF_Gen.getSetting("min_alt").equals("")){
          min_alt = Constants.ALT_MIN;
       }else{
          min_alt = Float.parseFloat(CDF_Gen.getSetting("min_alt"));
       }
 
       System.out.println("Rejecting data bellow " + min_alt + " kilometers.");
    }
 
    public int getSize(String cadence){
       if(cadence.equals("1Hz")){
          return rec_num_1Hz + 1;
       }else if(cadence.equals("4Hz")){
          return rec_num_4Hz;
       }else if(cadence.equals("20Hz")){
          return rec_num_20Hz;
       }else if(cadence.equals("mod4")){
          return rec_num_mod4;
       }else if(cadence.equals("mod32")){
          return rec_num_mod32;
       }else{
          return rec_num_mod40;
       }
    }
   
    public int convertIndex(int old_i, long fc, String old_cad, String new_cad){
       long target_fc;
       int fc_offset = 0, new_i;
       double multiplier;
       int[] frames;
 
       //figure out the index multiplier, fc_offset, and 
       //get the new frameset based on input cadence
       if(new_cad.equals("mod40")){
          multiplier = 0.025;
          frames = frame_mod40;
          fc_offset = (int)fc % 40;
       }
       else if(new_cad.equals("mod32")){
          multiplier = 0.03125;
          frames = frame_mod32;
          fc_offset = (int)fc % 32;
       }
       else if(new_cad.equals("mod4")){
          multiplier = 0.25;
          frames = frame_mod4;
          fc_offset = (int)fc % 4;
       }
       else if(new_cad.equals("1Hz")){
          multiplier = 1;
          frames = frame_1Hz;
       }
       else if(new_cad.equals("4Hz")){
          multiplier = 4;
          frames = frame_4Hz;
       }
       else{
          multiplier = 20;
          frames = frame_20Hz;
       }
       if(old_cad.equals("mod40")){multiplier /= 0.025;}
       else if(old_cad.equals("mod32")){multiplier /= 0.03125;}
       else if(old_cad.equals("mod4")){multiplier /= 0.25;}
       else if(old_cad.equals("4Hz")){multiplier /= 4;}
       else{multiplier /= 20;}
 
       //figure out the target frame number 
       //this will determine the first frame number of a multiplexed group
       target_fc = fc - fc_offset;
 
       //get initial guess for the new index
       new_i = (int)(old_i * multiplier);
 
       //correct new_i based on frame number
       while((new_i < frames.length) && (frames[new_i] < target_fc)){
          new_i++;
       }
       if(new_i == frames.length){new_i--;}
       while((new_i > 0) && (frames[new_i] > target_fc)){
          new_i--;
       }
 
       return new_i;
    }
 
    public void addFrame(BigInteger frame, int dpu_id){
       //Breakdown frame counter words: 
       //save the frame counter parts as temp variables,
       //they will be written to the main structure once rec_num is calculated.
       //First 5 bits are version, next 6 are id, last 21 are FC
       short tmpVer = 
          frame.shiftRight(1691).and(BigInteger.valueOf(31)).shortValue();
       short tmpPayID = 
          frame.shiftRight(1685).and(BigInteger.valueOf(63)).shortValue();
       int tmpFC = 
          frame.shiftRight(1664).and(BigInteger.valueOf(2097151)).intValue();
       int tmpGPS = 
          frame.shiftRight(1632).and(BigInteger.valueOf(4294967295L)).intValue();
 
       //check to make sure we have a frame from the correct payload
       if(dpu_id != tmpPayID){return;}
 
       //get multiplex info
       int mod4 = (int)tmpFC % 4;
       int mod32 = (int)tmpFC % 32;
       int mod40 = (int)tmpFC % 40;
 
       //check the payload is above the minimum altitude
       if(low_alt){
          if((mod4 == Constants.ALT_I) && ((tmpGPS / 1000000) >= min_alt)){
             low_alt = false;
          }else{return;}
       }else{
          if((mod4 == Constants.ALT_I) && ((tmpGPS / 1000000) < min_alt)){
             low_alt = true;
             return;
          }
       }
 
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
 
       switch(mod4){
          case Constants.ALT_I: 
             if(
                (gps_raw[mod4][rec_num_mod4] < Constants.ALT_RAW_MIN) ||
                (gps_raw[mod4][rec_num_mod4] > Constants.ALT_RAW_MAX)
             ){
                gps_raw[mod4][rec_num_mod4] = Constants.ALT_RAW_FILL;
                gps_q[rec_num_mod4] |= Constants.OUT_OF_RANGE;  
             }
             break;
          case Constants.TIME_I: 
             if(
                (gps_raw[mod4][rec_num_mod4] < Constants.MS_WEEK_MIN) ||
                (gps_raw[mod4][rec_num_mod4] > Constants.MS_WEEK_MAX)
             ){
                gps_raw[mod4][rec_num_mod4] = Constants.MS_WEEK_FILL;
                gps_q[rec_num_mod4] |= Constants.OUT_OF_RANGE;  
             }
 
             ms_of_week[rec_num_mod4] = gps_raw[Constants.TIME_I][rec_num_mod4];
             break;
          case Constants.LAT_I: 
             if(
                (gps_raw[mod4][rec_num_mod4] < Constants.LAT_RAW_MIN) ||
                (gps_raw[mod4][rec_num_mod4] > Constants.LAT_RAW_MAX)
             ){
                gps_raw[mod4][rec_num_mod4] = Constants.LAT_RAW_FILL;
                gps_q[rec_num_mod4] |= Constants.OUT_OF_RANGE;  
             }
             break;
          case Constants.LON_I: 
             if(
                (gps_raw[mod4][rec_num_mod4] < Constants.LON_RAW_MIN) ||
                (gps_raw[mod4][rec_num_mod4] > Constants.LON_RAW_MAX)
             ){
                gps_raw[mod4][rec_num_mod4] = Constants.LON_RAW_FILL;
                gps_q[rec_num_mod4] |= Constants.OUT_OF_RANGE;  
             }
             break;
       }
 
       //GPS PPS
       pps[rec_num_1Hz] = 
          frame.shiftRight(1616).and(BigInteger.valueOf(65535)).intValue();
       if(
          (pps[rec_num_1Hz] < Constants.PPS_MIN) ||
          (pps[rec_num_1Hz] > Constants.PPS_MAX)
       ){
          //make sure the value is not out of range because of an early pps
          if(pps[rec_num_1Hz] != 65535){
             pps[rec_num_1Hz] = Constants.PPS_FILL;
             pps_q[rec_num_1Hz] |= Constants.OUT_OF_RANGE;
          }
       }
 
       //flag potentially bad gps and pps records
 
       if(
          mod4 > 0 && // make sure there is a previous record to compare to
          pps[rec_num_1Hz] == 65535 && //possible bad pps
          ms_of_week[rec_num_mod4] == ms_of_week[rec_num_mod4 - 1] //gps repeat
       ){
          pps_q[rec_num_1Hz] |= Constants.NO_GPS;
          gps_q[rec_num_mod4] |= Constants.NO_GPS;
       }
 
       //mag data 4 sets of xyz vectors. 24 bits/component
       for(int i = 0; i < 4; i++){
          magx_raw[rec_num_4Hz + i] = 
             frame.shiftRight(1592 - (72 * i)).
             and(BigInteger.valueOf(16777215)).intValue();
          magy_raw[rec_num_4Hz + i] = 
             frame.shiftRight(1568 - (72 * i)).
             and(BigInteger.valueOf(16777215)).intValue();
          magz_raw[rec_num_4Hz] = 
             frame.shiftRight(1544 - (72 * i)).
             and(BigInteger.valueOf(16777215)).intValue();
          magx_raw[rec_num_4Hz + i] = 
             frame.shiftRight(1592 - (72 * i)).
             and(BigInteger.valueOf(16777215)).intValue();
          magy_raw[rec_num_4Hz + i] = 
             frame.shiftRight(1568 - (72 * i)).
             and(BigInteger.valueOf(16777215)).intValue();
          magz_raw[rec_num_4Hz + i] = 
             frame.shiftRight(1544 - (72 * i)).
             and(BigInteger.valueOf(16777215)).intValue();
          magx_raw[rec_num_4Hz + i] = 
             frame.shiftRight(1592 - (72 * i)).
             and(BigInteger.valueOf(16777215)).intValue();
          magy_raw[rec_num_4Hz + i] = 
             frame.shiftRight(1568 - (72 * i)).
             and(BigInteger.valueOf(16777215)).intValue();
          magz_raw[rec_num_4Hz + i] = 
             frame.shiftRight(1544 - (72 * i)).
             and(BigInteger.valueOf(16777215)).intValue();
          magx_raw[rec_num_4Hz + i] = 
             frame.shiftRight(1592 - (72 * i)).
             and(BigInteger.valueOf(16777215)).intValue();
          magy_raw[rec_num_4Hz + i] = 
             frame.shiftRight(1568 - (72 * i)).
             and(BigInteger.valueOf(16777215)).intValue();
          magz_raw[rec_num_4Hz + i] = 
             frame.shiftRight(1544 - (72 * i)).
             and(BigInteger.valueOf(16777215)).intValue();
          
          if(
             (magx_raw[rec_num_4Hz] < Constants.MAG_MIN) ||
             (magx_raw[rec_num_4Hz] > Constants.MAG_MAX)
          ){
             magx_raw[rec_num_4Hz] = Constants.MAG_FILL;
             magn_q[rec_num_4Hz] |= Constants.OUT_OF_RANGE;
          }
          if(
             (magy_raw[rec_num_4Hz] < Constants.MAG_MIN) ||
             (magy_raw[rec_num_4Hz] > Constants.MAG_MAX)
          ){
             magy_raw[rec_num_4Hz] = Constants.MAG_FILL;
             magn_q[rec_num_4Hz] |= Constants.OUT_OF_RANGE;
          }
          if(
             (magz_raw[rec_num_4Hz] < Constants.MAG_MIN) ||
             (magz_raw[rec_num_4Hz] > Constants.MAG_MAX)
          ){
             magz_raw[rec_num_4Hz] = Constants.MAG_FILL;
             magn_q[rec_num_4Hz] |= Constants.OUT_OF_RANGE;
          }
       }
 
       
       //mod40 housekeeping data: 16bits
       hkpg_raw[mod40][rec_num_mod40] = 
          frame.shiftRight(1312).and(BigInteger.valueOf(65535)).longValue();
       if(
          (hkpg_raw[mod40][rec_num_mod40] < Constants.HKPG_MIN) ||
          (hkpg_raw[mod40][rec_num_mod40] > Constants.HKPG_MAX)
       ){
          hkpg_raw[mod40][rec_num_mod40] = Constants.HKPG_FILL;
          hkpg_q[rec_num_mod40] |= Constants.OUT_OF_RANGE;
          CDF_Gen.log.writeln(mod40 + "");
       }
 
       switch(mod40){
          case 36:
             sats[rec_num_mod40] = 
                (short)(hkpg_raw[mod40][rec_num_mod40] >> 8);
             offset[rec_num_mod40] = 
                (short)(hkpg_raw[mod40][rec_num_mod40] & 255);
 
             if(
                (sats[rec_num_mod40] < Constants.SATS_MIN) ||
                (sats[rec_num_mod40] > Constants.SATS_MAX)
             ){
                sats[rec_num_mod40] = Constants.SATS_FILL;
                hkpg_q[rec_num_mod40] |= Constants.OUT_OF_RANGE;
          CDF_Gen.log.writeln("Sats");
             }
             if(
                (offset[rec_num_mod40] < Constants.LEAP_SEC_MIN) ||
                (offset[rec_num_mod40] > Constants.LEAP_SEC_MAX)
             ){
                offset[rec_num_mod40] = Constants.LEAP_SEC_FILL;
                hkpg_q[rec_num_mod40] |= Constants.OUT_OF_RANGE;
          CDF_Gen.log.writeln("Leap");
             }
             break;
          case 37:
             weeks[rec_num_mod40] = 
                (int)hkpg_raw[mod40][rec_num_mod40];
             if(
                (weeks[rec_num_mod40] < Constants.WEEKS_MIN) ||
                (weeks[rec_num_mod40] > Constants.WEEKS_MAX)
             ){
                weeks[rec_num_mod40] = Constants.WEEKS_FILL;
                hkpg_q[rec_num_mod40] |= Constants.OUT_OF_RANGE;
          CDF_Gen.log.writeln("Weeks");
             }
             break;
          case 38:
             termStat[rec_num_mod40] = 
                (short)(hkpg_raw[mod40][rec_num_mod40] >> 15);
             cmdCnt[rec_num_mod40] = 
                (int)(hkpg_raw[mod40][rec_num_mod40] & 32767);
             if(
                (termStat[rec_num_mod40] < Constants.TERM_STAT_MIN) ||
                (termStat[rec_num_mod40] > Constants.TERM_STAT_MAX)
             ){
                termStat[rec_num_mod40] = Constants.TERM_STAT_FILL;
                hkpg_q[rec_num_mod40] |= Constants.OUT_OF_RANGE;
          CDF_Gen.log.writeln("TermStat");
             }
             if(
                (cmdCnt[rec_num_mod40] < Constants.CMD_CNT_MIN) ||
                (cmdCnt[rec_num_mod40] > Constants.CMD_CNT_MAX)
             ){
                cmdCnt[rec_num_mod40] = Constants.CMD_CNT_FILL;
                hkpg_q[rec_num_mod40] |= Constants.OUT_OF_RANGE;
          CDF_Gen.log.writeln("CMD " + cmdCnt[rec_num_mod40] + " " + Long.toBinaryString(hkpg_raw[mod40][rec_num_mod40]) + " " + Long.toBinaryString(32767L));
             }
             break;
          case 39:
             dcdCnt[rec_num_mod40] = 
                (short)(hkpg_raw[mod40][rec_num_mod40] >> 8);
             modemCnt[rec_num_mod40] = 
                (short)(hkpg_raw[mod40][rec_num_mod40] & 255);
             if(
                (dcdCnt[rec_num_mod40] < Constants.DCD_CNT_MIN) ||
                (dcdCnt[rec_num_mod40] > Constants.DCD_CNT_MAX)
             ){
                dcdCnt[rec_num_mod40] = Constants.DCD_CNT_FILL;
                hkpg_q[rec_num_mod40] |= Constants.OUT_OF_RANGE;
          CDF_Gen.log.writeln("DCD");
             }
             if(
                (modemCnt[rec_num_mod40] < Constants.MODEM_CNT_MIN) ||
                (modemCnt[rec_num_mod40] > Constants.MODEM_CNT_MAX)
             ){
                modemCnt[rec_num_mod40] = Constants.MODEM_CNT_FILL;
                hkpg_q[rec_num_mod40] |= Constants.OUT_OF_RANGE;
          CDF_Gen.log.writeln("Modem");
             }
             break;
          default:
             break;
       }
          
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
 
          if(
             (lc1_raw[rec_num_20Hz] < Constants.FSPC_RAW_MIN) ||
             (lc1_raw[rec_num_20Hz] > Constants.FSPC_RAW_MAX)
          ){
             lc1_raw[rec_num_20Hz] = Constants.FSPC_RAW_FILL;
             fspc_q[rec_num_20Hz] |= Constants.OUT_OF_RANGE;
          }
          if(
             (lc2_raw[rec_num_20Hz] < Constants.FSPC_RAW_MIN) ||
             (lc2_raw[rec_num_20Hz] > Constants.FSPC_RAW_MAX)
          ){
             lc2_raw[rec_num_20Hz] = Constants.FSPC_RAW_FILL;
             fspc_q[rec_num_20Hz] |= Constants.OUT_OF_RANGE;
          }
          if(
             (lc3_raw[rec_num_20Hz] < Constants.FSPC_RAW_MIN) ||
             (lc3_raw[rec_num_20Hz] > Constants.FSPC_RAW_MAX)
          ){
             lc3_raw[rec_num_20Hz] = Constants.FSPC_RAW_FILL;
             fspc_q[rec_num_20Hz] |= Constants.OUT_OF_RANGE;
          }
          if(
             (lc4_raw[rec_num_20Hz] < Constants.FSPC_RAW_MIN) ||
             (lc4_raw[rec_num_20Hz] > Constants.FSPC_RAW_MAX)
          ){
             lc4_raw[rec_num_20Hz] = Constants.FSPC_RAW_FILL;
             fspc_q[rec_num_20Hz] |= Constants.OUT_OF_RANGE;
          }
       }
        
       //medium spectra: 12 channels per frame, 16 bits/channels
       for(int mspc_i = 0, chan_i = 0; mspc_i < 12; mspc_i++){
          chan_i = (mod4 * 12) + mspc_i;
          mspc_raw[rec_num_mod4][chan_i] =
                frame.shiftRight(336 - (16 * mspc_i))
                   .and(BigInteger.valueOf(65535)).intValue();
          if(
             (mspc_raw[rec_num_mod4][chan_i] < Constants.MSPC_RAW_MIN) ||
             (mspc_raw[rec_num_mod4][chan_i] > Constants.MSPC_RAW_MAX)
          ){
             mspc_raw[rec_num_mod4][chan_i] = Constants.MSPC_RAW_FILL;
             mspc_q[rec_num_mod4] |= Constants.OUT_OF_RANGE;
          }
       }
 
       //slow spectra: 8 channels per frame, 16 bits/channels
       for(int sspc_i = 0, chan_i = 0; sspc_i < 8; sspc_i++){
          chan_i = (mod32 * 8) + sspc_i;
          sspc_raw[rec_num_mod32][chan_i] =
                frame.shiftRight(144 - (16 * sspc_i))
                   .and(BigInteger.valueOf(65535)).intValue();
          if(
             (sspc_raw[rec_num_mod32][chan_i] < Constants.SSPC_RAW_MIN) ||
             (sspc_raw[rec_num_mod32][chan_i] > Constants.SSPC_RAW_MAX)
          ){
             sspc_raw[rec_num_mod32][chan_i] = Constants.SSPC_RAW_FILL;
             sspc_q[rec_num_mod32] |= Constants.OUT_OF_RANGE;
          }
       }
       
       //rate counter: mod4 data, 16bits
       rcnt_raw[mod4][rec_num_mod4] = 
          frame.shiftRight(16).and(BigInteger.valueOf(65535)).longValue();
       if(
          (rcnt_raw[mod4][rec_num_mod4] < Constants.RCNT_MIN) ||
          (rcnt_raw[mod4][rec_num_mod4] > Constants.RCNT_MAX)
       ){
          rcnt_raw[mod4][rec_num_mod4] = Constants.RCNT_FILL;
          rcnt_q[rec_num_mod4] |= Constants.OUT_OF_RANGE;
       }
    }
 }
