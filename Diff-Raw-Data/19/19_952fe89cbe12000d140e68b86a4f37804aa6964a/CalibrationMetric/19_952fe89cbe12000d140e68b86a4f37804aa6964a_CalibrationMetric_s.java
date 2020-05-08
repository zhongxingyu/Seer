 /*
  * Copyright 2012, United States Geological Survey or
  * third-party contributors as indicated by the @author tags.
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
  * along with this program.  If not, see <http://www.gnu.org/licenses/  >.
  *
  */
 package asl.seedscan.metrics;
 
 import java.util.logging.Logger;
 import java.util.List;
 import java.util.Calendar;
 import java.util.Enumeration;
 import java.util.GregorianCalendar;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.TreeSet;
 
 import java.nio.ByteBuffer;
 import asl.util.Hex;
 
 import asl.metadata.Channel;
 import asl.metadata.EpochData;
 import asl.metadata.meta_new.ChannelMeta;
 import asl.metadata.meta_new.ResponseStage;
 import asl.seedsplitter.DataSet;
 import asl.util.PlotMaker;
 
 import seed.Blockette320;
 
 import freq.Cmplx;
 import timeutils.PSD;
 import timeutils.Timeseries;
 
 
 public class CalibrationMetric
 extends Metric
 {
     private static final Logger logger = Logger.getLogger("asl.seedscan.metrics.CalibrationMetric");
 
     @Override public long getVersion()
     {
         return 1;
     }
 
     @Override public String getName()
     {
         return "CalibrationMetric";
     }
 
 
     public void process()
     {
         System.out.format("\n              [ == Metric %s == ]    [== Station %s ==]    [== Day %s ==]\n", 
                           getName(), getStation(), getDay() );
 
 
         if (metricData.hasCalibrationData() == false) {
             System.out.format("== No Calibrations loaded for this station for this day --> return\n");
             return;
         }
 
     // Get all BH? channels for this stationMeta:
         List<Channel> channels = stationMeta.getChannelArray("BH");
 
         for (Channel channel : channels){
 
             ByteBuffer digest = metricData.valueDigestChanged(channel, createIdentifier(channel));
 
         // At this point we KNOW we have metadata so we WILL compute a digest.  If the digest is null
         //  then nothing has changed and we don't need to recompute the metric
             if (digest == null) { 
                 System.out.format("%s INFO: Data and metadata have NOT changed for this channel:%s --> Skipping\n"
                                 ,getName(), channel);
                 continue;
             }
 
             //double result = computeMetric(channel);
         // We're computing 2 results (amp + phase diff) but we don't actually have a way yet to load
         // 2 responses for a single metric (= single channel + powerband, etc.) into the database
             double[] results = computeMetric(channel);
 
             //if (result == NO_RESULT) {
             if (results == null) {
                 // Do nothing --> skip to next channel
             }
             else {
                 //metricResult.addResult(channel, result, digest);
                 metricResult.addResult(channel, results[0], digest);
                 //for (int i=0; i<results.length; i++) {
                     //metricResult.addResult(channel, results[i], digest);
                 //}
             }
 
         }// end foreach channel
     } // end process()
 
     //private double computeMetric(Channel channel) {
     private double[] computeMetric(Channel channel) {
 
         double[] result = new double[2];
 
         if (!metricData.hasChannelData(channel)) {
             return null;
             //return NO_RESULT;
         }
 
         List<Blockette320> calBlocks = metricData.getChannelCalData(channel);
 
         if (calBlocks == null) {
             System.out.format("== %s: No cal blocks found for channel=[%s]\n", getName(), channel);
             return null;
             //return NO_RESULT;
         }
 
         if (calBlocks.size() > 1) {
             System.out.format("== %s: Found n=%d Random Calibration Blockettes! --> What to do ?\n", getName(), calBlocks.size() );
         }
 
         Blockette320 blockette320 = calBlocks.get(0);
         blockette320.print();
         long calStartEpoch      = blockette320.getCalibrationEpoch();   // Epoch millisecs
         long calDuration        = blockette320.getCalibrationDuration();// Duration in millisecs
         long calEndEpoch        = calStartEpoch + calDuration;
         String channelExtension = blockette320.getCalInputChannel();  // e.g., "BC0" or "BC1"
 
         List<DataSet> data = metricData.getChannelData(channel);
         long dataStartEpoch     = data.get(0).getStartTime() / 1000;  // Convert microsecs --> millisecs
         long dataEndEpoch       = data.get(0).getEndTime()   / 1000;  // ...
         double srate            = data.get(0).getSampleRate();
 
         System.out.format("== %s: channel=[%s] calStartTime=[%s] calDuration=[%d]\n", getName(), channel, 
                            EpochData.epochToDateString(blockette320.getCalibrationCalendar()), calDuration/1000);
 
         if ( blockette320.getCalibrationCalendar().get(Calendar.HOUR) == 0 ){
             // This appears to be the 2nd half of a cal that began on the previous day --> Skip
             System.out.format("== %s: cal appears to be the 2nd half of a cal from previous day --> Skip\n", getName());
             //return NO_RESULT;
             return null;
         }
 
         if ( calEndEpoch > dataEndEpoch ) {
             // Look for cal to span into next day
 
             System.out.format("== %s: channel=[%s] calEndEpoch > dataEndEpoch --> Cal appears to span day\n", getName(), channel);
 
             //calBlocks = nextMetricData.getChannelCalData(channel);
             calBlocks = metricData.getNextMetricData().getChannelCalData(channel);
 
             if (calBlocks == null) {
                 System.out.format("== %s: No DAY 2 cal blocks found for channel=[%s]\n", getName(), channel);
             }
             else {
                 System.out.format("== %s: channel=[%s] Found matching blockette320 on 2nd day:\n", getName(), channel);
                 blockette320 = calBlocks.get(0);
                 blockette320.print();
                 long nextCalStartEpoch      = blockette320.getCalibrationEpoch();
                 long nextCalDuration        = blockette320.getCalibrationDuration();
                 String nextChannelExtension = blockette320.getCalInputChannel();  // e.g., "BC0"
             // Compare millisecs between end of previous cal and start of this cal
                 if ( Math.abs(nextCalStartEpoch - calEndEpoch) < 1800000 ) { // They are within 1800 (?) secs of each other
                     boolean calSpansNextDay = true;
                     calDuration += nextCalDuration; 
                 }
             }
     
         }
 
     // We have the cal startTime and duration --> window both the input (BC?) and output (=channel data) and 
     //    compute the PSD of each
     //    Calibration input channel seed files (e.g., BC0.512.seed) do not have location code so it defaults to "--":
 
         double[] outData = metricData.getWindowedData(channel, calStartEpoch, calStartEpoch + calDuration);
         double[] inData  = metricData.getWindowedData(new Channel("--",channelExtension), calStartEpoch, calStartEpoch + calDuration);
         //Timeseries.writeSacFile(outData, srate, "dataOut", getStation(), channel.getChannel());  
         //Timeseries.writeSacFile(inData,  srate, "dataIn",  getStation(), channelExtension);  
 
      // Compute/Get the 1-sided psd[f] using Peterson's algorithm (24 hrs, 13 segments, etc.)
 
         double dt = 1.0/srate;
         PSD psdX         = new PSD(inData, inData, dt);
         Cmplx[] Gx       = psdX.getSpectrum();
         double df        = psdX.getDeltaF();
         double[] freq    = psdX.getFreq();
         int nf           = freq.length;
 
         ChannelMeta chanMeta = stationMeta.getChanMeta(channel);
         ResponseStage stage  = chanMeta.getStage(1);
         double s=0;
         if (stage.getStageType() == 'A') {
             s = 2. * Math.PI;
         }
         else if (stage.getStageType() == 'B') {
             s = 1.;
         }
         else {
             throw new RuntimeException("Error: Unrecognized stage1 type != {'A' || 'B'} --> can't compute!");
         }
 
         PSD psdXY        = new PSD(inData, outData, dt);
         Cmplx[] Gxy      = psdXY.getSpectrum();
         Cmplx[] Hf       = new Cmplx[Gxy.length];
         double[] calAmp  = new double[Gxy.length];
         double[] calPhs  = new double[Gxy.length];
         Cmplx ic         = new Cmplx(0.0 , 1.0);
         for (int k=0; k<Gxy.length; k++) {
           // Cal coils generate an ACCERLATION but we want the intrument response to VELOCITY:
           // Note that for metadata stage 1 = 'A' [Laplace rad/s] so that    s=i*2pi*f
           //   most II stations have stage1 = 'B' [Analog Hz] and should use s=i*f
             Cmplx iw  = Cmplx.mul(ic , s*freq[k]);
             Hf[k]     = Cmplx.div( Gxy[k], Gx[k] );
             Hf[k]     = Cmplx.mul( Hf[k], iw );
             //calAmp[k] = Hf[k].mag();
             calAmp[k] = 20. * Math.log10( Hf[k].mag() );
             calPhs[k] = Hf[k].phs() * 180./Math.PI;
         }
 
         Cmplx[] instResponse = chanMeta.getPoleZeroResponse(freq);
         double[] ampResponse = new double[nf];
         double[] phsResponse = new double[nf];
         for (int k=0; k<nf; k++) {
             //ampResponse[k] = instResponse[k].mag();
             ampResponse[k] = 20. * Math.log10( instResponse[k].mag() );
             phsResponse[k] = instResponse[k].phs() * 180./Math.PI;
         }
 
         final double MIDBAND_PERIOD = 20.0; // Period at which to normalize the H(f) magnitudes
 
         double midFreq = 1.0/MIDBAND_PERIOD;
         int index = (int)(midFreq/df);
         double midAmp   = ampResponse[index];
         double magDiff  = calAmp[index] - midAmp;
 
         // Scale calAmp to = ampResponse at the mid-band frequency
         for (int k=0; k<nf; k++) {
             calAmp[k] -= magDiff;  // subtract offset from the decibel spectrum
         }
 
         // Get cornerFreq = Freq where ampResponse falls by -3dB below midAmp
         double cornerFreq = 0.;
         for (int k=index; k>=0; k--) {
             if (Math.abs(midAmp - ampResponse[k]) >= 3) {
                 cornerFreq = freq[k];
                 break;
             }
         }
 
         if (cornerFreq <= 0.) {
             throw new RuntimeException("CalibrationMetric: Error - cornerFreq == 0!");
         }
         // Get an octave window of periods with cornerPer in the Geometric Mean: Tc = sqrt(Ts*Tl)
         double cornerPer = 1./cornerFreq;
         double Ts = cornerPer / Math.sqrt(2);
         double Tl = 2.*Ts;
 
         double diff = 0.;
         int nPer = 0;
         for (int k=0; k<freq.length; k++) {
             double per = 1./freq[k];
             if ( (per >= Ts) && (per <= Tl) ){
                 diff += Math.abs( ampResponse[k] - calAmp[k] );
                 nPer++;
                //System.out.format("== Count Per[%d]=%.2f ampResp[k]-calAmp[k]=%.6f\n", nPer, per, Math.abs(ampResponse[k]-calAmp[k]) );
             }
         }
 
         if (nPer <= 0) {}
         diff /= (double)nPer;
         System.out.format("== diff=%.6f\n", diff);
 
         // Compute phase difference between Tl and Tmin (= 1/1.0 Hz)
         double Tmin = 1.;
         double phaseDiff = 0.;
         nPer = 0;
         for (int k=0; k<freq.length; k++) {
             double per = 1./freq[k];
             if ( (per >= Tmin) && (per <= Tl) ){
                 phaseDiff += Math.abs( phsResponse[k] - calPhs[k] );
                 nPer++;
                //System.out.format("== Count Per[%d]=%.2f phsResp[k]-calPhs[k]=%.6f\n", nPer, per, Math.abs(phsResponse[k]-calPhs[k]) );
             }
         }
         if (nPer <= 0) {}
         phaseDiff /= (double)nPer;
         System.out.format("== phaseDiff=%.6f\n", phaseDiff);
 
         if (getMakePlots()){
             PlotMaker plotMaker = new PlotMaker(metricResult.getStation(), channel, metricResult.getDate());
             plotMaker.plotSpecAmp2(freq, ampResponse, phsResponse, calAmp, calPhs, "calib");
         }
 
     // diff = average absolute diff (in dB) between calAmp and ampResponse in (per) octave containing Tc: Ts < Tc < Tl
     // phaseDiff = average absolute diff (in deg) between calPhs and phsResponse over all periods from Nyq to Tl
         result[0]=diff;
         result[1]=phaseDiff;
 
         return result;
 
     } // end computeMetric()
 
 }
