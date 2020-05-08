 /*
 ExactTiming.java v13.01.04
 
 Description:
    Uses a block of gps time info to create a more exact time variable.
    Ported from MPM's C code.
 
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
    v13.01.04
       -Changed the way epoch is calculated and fixed the "12h bug"
       
    v12.12.31
       -Removed redundant calculation of rec_i when 
       checking if BarrelTimes array is full
       
       
    v12.11.28
       -Fixed null pointer error caused by trying to 
       process data without a linear model
 
    v12.11.27
       -Grabs DataHolder object from CDF_Gen as a member 
       rather than having it passed through all function
       -Rewroked a number of routines to add ability to fill missing time 
       
    v12.11.26
       -Fixed lots and lots of bug.
       -Changed offset to adjust the GPS start time to J2000.
       -Uses Calendar date object to calculate time relative to GPS start time
       -Writes values to DataHolder object now.
       
    v12.11.20
       -Changed references to Level_Generator to CDF_Gen
       
    v12.10.15
       -First Version
 
 */
 
 package edu.ucsc.barrel.cdf_gen;
 
 import gsfc.nssdc.cdf.CDFException;
 import gsfc.nssdc.cdf.util.CDFTT2000;
 
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.TimeZone;
 
 public class ExtractTiming {
    //Set some constant values
    private static final int MAX_RECS = 2000;// max number of frames into model
    private static final double NOM_RATE = 999.89;// nominal ms per frame
    private static final double SPERWEEK = 604800.0;// #seconds in a week
    private static final byte FILLED = 1;//quality bit---ms time filled
    private static final byte NEW_WEEK = 2;// quality bit---replaced week
    private static final byte NEW_MSOFWEEK = 4;// quality bit---replaced msofweek
    private static final short BADFC = 256;// quality bit---bad fc value 
    private static final short BADWK = 512;// quality bit---bad week value
    private static final short BADMS = 1024;// quality bit---bad msofweek
    private static final short BADPPS = 2048;// quality bit---bad PPS
    private static final short NOINFO = 4096;// quality bit---not enough info
    private static final short PPSFILL = -32768;// fill value for ms_of_week
    private static final int MSFILL = -2147483648;// fill value for ms_of_week
    private static final int FCFILL = -2147483648;// fill value for ms_of_week
    private static final short WKFILL = -32768;// fill value for week
    private static final short MINWEEK = 1200;
    private static final byte MINPPS = 0;
    private static final byte MINMS = 1;
    private static final byte MINFC = 0;
    private static final short MAXWEEK = 1880;
    private static final short MAXPPS = 1000;
    private static final int MAXMS = 604800000;
    private static final int MAXFC = 2097152;
    private static final long LEAPSEC = 16;
    
    //date offset info
    //Offset in ms from system epoch to gps start time (00:00:00 1980-01-60 UTC) 
    private static long GPS_START_TIME; 
    
    //ms from system epoch to J2000 (11:58:55.816 2000-01-01 UTC)
    private static long J2000; 
    
    //ms from GPS_START_TIME to J2000
    private static long J2000_OFFSET;
 
    //model parameters for a linear fit
    //Example: ms = rate * (fc + offset);
    public class Model{
       private double rate; 
       private double offset;
       
       public Model(double r, double o){
          rate = r;
          offset = o;
       }
       
       public void setRate(double r){rate = r;}
       public void setOffset(double o){offset = o;}
       
       public double getRate(){return rate;}
       public double getOffset(){return offset;}
    }
    
    private class TimePair{
       private long ms;//frame time; ms since J2000
       private long fc;//frame counter
 
       public void setMS(long t){ms = t;}
       public void setFrame(long f){fc = f;}
       
       public long getMS(){return ms;}
       public long getFrame(){return fc;}
    }
    
    private class BarrelTime{
       private long ms;//frame time; ms since J2000
       private long fc = FCFILL;//frame counter
       private long ms_of_week = MSFILL;// ms since 00:00 on Sunday
       private short week = WKFILL;//weeks since 6-Jan-1980
       private short pps = PPSFILL;//ms into frame when GPS pps comes
       private short quality = 0;//quality of recovered time
       private long flag = 0;//unused for now
      
       public void setMS(long t){ms = t;}
       public void setFrame(long f){
          if((f > MINFC) && (f < MAXFC)){
             fc = f;
          }
          else{
             fc = FCFILL;
             setQuality(BADFC);
          }
       }
       public void setMS_of_week(long msw){
          if((msw > MINMS) && (msw < MAXMS)){
             ms_of_week = msw;
          }
          else{
             ms_of_week = MSFILL;
             setQuality(BADMS);
          }
       }
       public void setWeek(short w){
          if((w > MINWEEK) && (w < MAXWEEK)){
             week = w;
          }
          else{
             week = WKFILL;
             setQuality(BADWK);
          }
       }
       public void setPPS(short p){
          if((p > MINPPS) && (p < MAXPPS)){
             pps = p;
          }
          else{
             pps = PPSFILL;
             setQuality(BADPPS);
          }
       }
       public void setQuality(short q){quality |= q;}
       public void setFlag(long f){flag = f;}
       
       public long getMS(){return ms;}
       public long getFrame(){return fc;}
       public long getMS_of_week(){return ms_of_week;}
       public short getWeek(){return week;}
       public short getPPS(){return pps;}
       public short getQuality(){return quality;}
       public long getFlag(){return flag;}
       
       public boolean testQuality(short test_pattern){
          if((getQuality() & test_pattern) == test_pattern){
             return true;
          }else{return false;}
       }
    }
    
    //declare initial time model
    private Model time_model;
    //declare an array of time pairs
    private TimePair[] time_pairs;
    
    //holder for BarrelTime objects
    public BarrelTime[] timeRecs;
    
    private DataHolder data;
    
    public ExtractTiming(){
       //get DataHolder storage object
       data = CDF_Gen.getDataSet();
       
       time_model = null;
       
       //calculate GPS_START_TIME, J2000, and the offset between them
       Calendar gps_start_cal = 
          Calendar.getInstance(TimeZone.getTimeZone("UTC"));
       gps_start_cal.set(
          1980, 00, 06, 00, 00, 00);
       GPS_START_TIME = gps_start_cal.getTimeInMillis();
       Calendar j2000_cal = 
          Calendar.getInstance(TimeZone.getTimeZone("UTC"));
       j2000_cal.set(
          2000, 00, 01, 11, 58, 55);
       j2000_cal.add(Calendar.MILLISECOND, 816);
       J2000 = j2000_cal.getTimeInMillis();
       J2000_OFFSET = J2000 - GPS_START_TIME;
 
       int temp, day, fc, week, ms, pps, cnt, mod4, mod40;
       
       //data set index reference
       int FC = 0, DAY = 1, WEEK = 2, MS = 3, PPS = 4;
       int rec_i = 0, frame_i = 0;
       
       timeRecs = new BarrelTime[MAX_RECS];
 
       //loop through all of the frames and generate time models
       for(frame_i = 0; frame_i < data.getSize("1Hz"); frame_i++){
          //Figure out which record in the set of MAX_RECS this is 
          rec_i = frame_i % MAX_RECS;
 
          //check if the BarrelTimes array is full
          if(rec_i == 0 && frame_i > 1){
             //generate a model and fill BarrelTime array
             fillTime(frame_i, MAX_RECS);
            
             timeRecs = new BarrelTime[MAX_RECS];
          }
          
          //figure out the mod4 and mod40 values
          mod4 = data.frame_1Hz[frame_i] % 4;
          mod40 = data.frame_1Hz[frame_i] % 40;
 
          //initialize the BarrelTime object
          timeRecs[rec_i] = new BarrelTime();
          
          //fill a BarrelTime object with data values
          timeRecs[rec_i].setFrame(data.frame_1Hz[frame_i]);
          if(mod40 == DataHolder.WEEK){
             timeRecs[rec_i].setWeek((short)data.weeks[frame_i / 40]);
          }
          
          //set the ms_of_week to a fill value if mod4!=1 or the saved value is 0
          if(mod4 == DataHolder.TIME){ 
             timeRecs[rec_i].setMS_of_week(data.ms_of_week[frame_i / 4]);
          }
          timeRecs[rec_i].setPPS((short)data.pps[frame_i]);
       }
       
       //process any remaining records
       fillTime(frame_i, (rec_i + 1));
       
       backFillModels();
       
       //calculate epoch data for the DataHolder object
       fillEpoch();
             
       System.out.println("Time corrected.\n");
    }
    
    public void fillTime(int current_data_i, int num_of_recs){
       double temp;
       Model q;
       int pair_cnt, good_cnt, goodfit;
 
       //verify this set of records does not contain bad frame counters
       for(int rec_i = 0; rec_i < num_of_recs; rec_i++) {
          if(timeRecs[rec_i].testQuality(BADFC)){return;}
       }
    
       time_pairs = new TimePair[MAX_RECS];
       pair_cnt = makePairs(num_of_recs);
       
       if(pair_cnt > 2){
          //remove any outliers from the time_pair array
          good_cnt = selectPairs(pair_cnt);
          
          //Try to create a new model
          q = genModel(good_cnt);
          if (q != null) {
             time_model = new Model(q.getRate(), q.getOffset());
          }
       }
       
       //Make sure we have a model
       if(time_model != null){
          updateTimes(current_data_i, num_of_recs);
       }else{//or just set quality bits to noinfo
          for (int rec_i = 0; rec_i < num_of_recs; rec_i++){
             timeRecs[rec_i].setQuality(NOINFO);
             //data.time_q[current_data_i - num_of_recs + rec_i] |= NOINFO; 
          }
       }
    }
    
    public int makePairs(int cnt){
       int goodcnt = 0;
       short week, pps;
       long ms;
       Calendar date = Calendar.getInstance();
 
       //Make sure there are a good number of records
       if (cnt <= 0 || cnt > MAX_RECS){return 0;}
 
       for(int rec_i = 0; rec_i < cnt; rec_i++) {
          week = timeRecs[rec_i].getWeek();
          pps = timeRecs[rec_i].getPPS();
          ms = timeRecs[rec_i].getMS_of_week();
 
          //check for all the needed components
          if(ms == MSFILL || week == WKFILL || pps == PPSFILL) {continue;}
 
          time_pairs[goodcnt] = new TimePair();
         
          //get ms since Jan 6, 1980
         ms += ((((3 + 7 * week) * 86400) - LEAPSEC) * 1000);
          
          //convert to ms since J2000
          ms -= J2000_OFFSET;
 
          //correct for pps
          if (pps < 241) {
             ms -= pps;
          } else {
             ms += 1000 - pps;
          }
 
          //save the ms since system epoch
          time_pairs[goodcnt].setMS(ms);
          time_pairs[goodcnt].setFrame(timeRecs[rec_i].getFrame());
          goodcnt++;
       }
       return goodcnt;
    }
    
    public int selectPairs(int m){
       double[] offsets = new double[m];
       double med;
       int last_good_pair_i = 0;
 
       //not enough pairs to continue
       if(m < 2){return m;}
       
       //get offsets from set of time pairs
       for(int pair_i = 0; pair_i < m; pair_i++){
          offsets[pair_i] = 
             time_pairs[pair_i].getMS() - 
             (NOM_RATE * time_pairs[pair_i].getFrame());
       }
       
       med = median(offsets);
       
       //reject any points more than 200ms off the median offset value
       for (int pair_i = 0; pair_i < m; pair_i++) {
          if(Math.abs(offsets[pair_i] - med) > 200){
             time_pairs[pair_i] = null;
          }else{
             //move the accepted pair up in the array
             time_pairs[last_good_pair_i] = time_pairs[pair_i];
             
             //remove the pair from its old location in the array
             if(last_good_pair_i != pair_i){
                time_pairs[pair_i] = null;
             }
 
             last_good_pair_i++;
          }
       }
       
       return last_good_pair_i;
    }
    
    public double median(double[] list){
       double[] sortedList = new double[list.length];
       
       //copy the input list and sort it
       System.arraycopy(list, 0, sortedList, 0, list.length);
       
       Arrays.sort(sortedList);
       
       if(list.length > 2){
          return sortedList[(int) (sortedList.length / 2) + 1];
       }else{
          return sortedList[0];
       }
    }
    
    public Model genModel(int n){
       Model linfit = new Model(0.0,0.0);
       double  mux = 0., muy = 0., xy = 0., xx = 0.;
       double m, x, y;
 
       //not enough points to generate model
       if (n < 3) {return null;}
 
       //get mean x and y values
       for (int pnt_i = 0; pnt_i < n; pnt_i++) {
          mux += time_pairs[pnt_i].getFrame();
          muy += time_pairs[pnt_i].getMS();
       }
       mux /= n;
       muy /= n;
       
       //calculate the least square regression
       for (int pnt_i = 0; pnt_i < n; pnt_i++) {
          x = time_pairs[pnt_i].getFrame() - mux;
          y = time_pairs[pnt_i].getMS() - muy;
          xx += x * x;
          xy += x * y;
       }
       m = xy / xx;
       
       //save the model
       linfit.setRate(m);
       linfit.setOffset(muy / m - mux);
       
       System.out.println(linfit.getRate() + " " + linfit.getOffset());
       
       return linfit;
    }
 
    public boolean evaluateModel(Model test_model, int cnt){
       if(test_model == null || cnt < 2){
          return false;
       }
       else if (cnt == 2) {
          double ms1 = 
             test_model.getRate() * 
                (time_pairs[0].getFrame() + test_model.getOffset());
          double ms2 = 
             time_model.getRate() * 
                (time_pairs[1].getFrame() + time_model.getOffset());
       
          //test to see if model gives less than 50ms change
          if(Math.abs(ms1 - ms2) < 0.5 && 
             Math.abs(ms2 - time_pairs[0].getMS()) < 0.5
          ){
             return true;
          }else{
             return false;
          }
       }
       else{
           //FIX THIS
           return true;
        }
    }
    
    public void updateTimes(int current_data_i, int num_of_recs){
       for(
          int rec_i = 0, data_i = current_data_i - num_of_recs;
          rec_i < num_of_recs; 
          rec_i++, data_i++
       ){
          timeRecs[rec_i].setMS( 
             (long)(time_model.getRate() * 
             (timeRecs[rec_i].getFrame() + time_model.getOffset()))
          );
          timeRecs[rec_i].setQuality(FILLED);
          
          data.time_model_offset[data_i] = time_model.getOffset();
          data.time_model_rate[data_i] = time_model.getRate();
          data.ms_since_j2000[data_i] = timeRecs[rec_i].getMS();
          data.time_q[data_i] |= FILLED;
       }
    }
    
    public void backFillModels(){
       double last_offset = -999, last_rate = -999;
       
       for(int data_i = data.getSize("1Hz") - 1; data_i >= 0 ; data_i--){
          if((data.time_q[data_i] & FILLED) == FILLED){
             last_offset = data.time_model_offset[data_i];
             last_rate = data.time_model_rate[data_i];
          }
          else if((last_offset != -999) && (last_rate != -999)){
             data.time_model_offset[data_i] = last_offset;
             data.time_model_rate[data_i] = last_rate;
             
             data.ms_since_j2000[data_i] = 
                (long)(last_rate * (data.frame_1Hz[data_i] + last_offset));
          }
       }
    }
    
    public void fillEpoch(){
       Calendar date = Calendar.getInstance();
       int 
          fc_mod4 = 0, fc_mod32 = 0, fc_mod40 = 0, 
          last_fc_mod4 = -1, last_fc_mod32 = -1, last_fc_mod40 = -1,
          rec_num_mod4 = -1, rec_num_mod32 = -1, rec_num_mod40 = -1;
 
       for(int data_i = 0; data_i < data.getSize("1Hz"); data_i++){
          fc_mod4 = 
             data.frame_1Hz[data_i] - (data.frame_1Hz[data_i] % 4);
          fc_mod32 = 
             data.frame_1Hz[data_i] - (data.frame_1Hz[data_i] % 32);
          fc_mod40 = 
             data.frame_1Hz[data_i] - (data.frame_1Hz[data_i] % 40);
          
          //increment the record number for the <1Hz cadence data types
          if(fc_mod4 != last_fc_mod4){rec_num_mod4++;}
          if(fc_mod32 != last_fc_mod32){rec_num_mod32++;}
          if(fc_mod40 != last_fc_mod40){rec_num_mod40++;}
 
          //convert from "ms since system epoch" to "ns since J2000"
          data.epoch_1Hz[data_i] =
             (long)(data.ms_since_j2000[data_i] * 1000000);
          //save epoch to the various time scales
          //fill the >1Hz times 
          for(int fill_i = 0; fill_i < 4; fill_i++){
             data.epoch_4Hz[(data_i * 4) + fill_i] = 
                data.epoch_1Hz[data_i] + (fill_i * 250000000);
          }
          for(int fill_i = 0; fill_i < 20; fill_i++){
             data.epoch_20Hz[(data_i * 20) + fill_i] = 
                data.epoch_1Hz[data_i] + (fill_i * 50000000);
          }
          //fill the <1Hz times. 
          //These time stamps are for the begining of the accumulation period
          data.epoch_mod4[rec_num_mod4] = 
             (long)(data.epoch_1Hz[data_i] - 
                ((data.frame_1Hz[data_i] % 4) - 4) * NOM_RATE * 1000000);
          data.epoch_mod32[rec_num_mod32] =
             (long)(data.epoch_1Hz[data_i] - 
                ((data.frame_1Hz[data_i] % 32) - 32) * NOM_RATE * 1000000);
          data.epoch_mod40[rec_num_mod40] = 
             (long)(data.epoch_1Hz[data_i] - 
                ((data.frame_1Hz[data_i] % 40) - 40) * NOM_RATE * 1000000);
 
          last_fc_mod4 = fc_mod4;
          last_fc_mod32 = fc_mod32;
          last_fc_mod40 = fc_mod40;
       }
    }
 }
