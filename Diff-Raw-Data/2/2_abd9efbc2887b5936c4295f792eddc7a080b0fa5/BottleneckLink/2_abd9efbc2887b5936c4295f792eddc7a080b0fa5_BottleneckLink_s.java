 package dgu.bufsizing;
 
 import dgu.bufsizing.control.RouterController;
 import dgu.bufsizing.control.RouterController.RouterCmd;
 import dgu.util.IllegalArgValException;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.io.*;
 import java.util.HashMap;
 import java.util.Vector;
 import org.jfree.data.xy.XYSeries;
 
 /**
  * Information about a bottleneck link.
  * @author David Underhill
  */
 public class BottleneckLink extends Link<Router> {
     private static final int QUEUE_WIDTH  = 100;
     private static final int QUEUE_HEIGHT = 24;
     public static final BasicStroke STROKE_OCC = new BasicStroke( QUEUE_HEIGHT*0.75f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL ); 
     public static final BasicStroke STROKE_BOTTLENECK = new BasicStroke( 5.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER ); 
     public static final BasicStroke STROKE_BOTTLENECK_OUTLINE = new BasicStroke( 7.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER ); 
     public static final BasicStroke STROKE_BOTTLENECK_SEL_OUTLINE = new BasicStroke( 9.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER ); 
     public static final Color COLOR_QUEUE_FILL = new Color( 0xFF, 0xAA, 0x11 );
     
     // don't have JFreeChart worry about sorting data or looking for duplicates (performance!)
     private static final boolean AUTOSORT_SETTING = false;
     private static final boolean ALLOW_DUPS_SETTING = true;
     
     private static final String SAVED_RESULTS_FN = "measured.txt";
     
     // user-defined variables on this bottleneck link
     private BufferSizeRule bufSizeRule = BufferSizeRule.RULE_OF_THUMB;
     private int numFlows;
     private int rtt_ms;
     private int rateLimit_kbps;
     private int customBufSize_bytes;
     private int lastBufSize_bytes = -1;
     private boolean selected;
     
     // the traffic generator responsible for handling flows over this link
     private TrafficGenerator tgen;
     
     // recently collected data
     private long time_offset_ns8 = 0;
     private long prev_time_offset_end_ns8 = 0;
     private long bytes_sent_since_last_update = 0;
     private int  queueOcc_bytes = -1;
     private static final int SEC_DIV_8NS  = 125000000;
     private static final int MSEC_DIV_8NS = 125000;
     
     /** set packet sizes for sizing the buffer (assumes packets are MTU-sized 
      * packets which is reasonably for the type of traffic we generate) */
     public static final int BYTES_PER_PACKET = 1500;
     
     // empirical data collected from the router
     private final XYSeries dataThroughput    = new XYSeries("Throughput",AUTOSORT_SETTING,ALLOW_DUPS_SETTING);
     private final XYSeries dataThroughputPer = new XYSeries("Link Utilization",AUTOSORT_SETTING,ALLOW_DUPS_SETTING);
     private final XYSeries dataQueueOcc      = new XYSeries("Queue Occupancy",AUTOSORT_SETTING,ALLOW_DUPS_SETTING);
     private final XYSeries dataQueueOccPer   = new XYSeries("Queue Utilization",AUTOSORT_SETTING,ALLOW_DUPS_SETTING);
     
     // last throughput data point (replicated to avoid critical section / locking) (float => one word ~=> atomic)
     private float instantaneousUtilization = 0.0f;
     private float instantaneousQueueOcc    = 0.0f;
     
     // settings for buffer size and rate limit as set by the user
     private final XYSeries dataBufSize = new XYSeries("Buffer Size",AUTOSORT_SETTING,ALLOW_DUPS_SETTING);
     private final XYSeries dataRateLimit = new XYSeries("Max Link Rate",AUTOSORT_SETTING,ALLOW_DUPS_SETTING);
     private boolean forceSet;
     
     private final XYSeries dataRTheROT = new XYSeries("Rule Of Thumb (RTT*C)",AUTOSORT_SETTING,ALLOW_DUPS_SETTING);
     private final XYSeries dataRTheGuido = new XYSeries("RTT * C / sqrt(N)", AUTOSORT_SETTING, ALLOW_DUPS_SETTING);
     private final XYSeries dataRMea = new XYSeries("Measured", AUTOSORT_SETTING, ALLOW_DUPS_SETTING);
     private final XYSeries dataRToday = new XYSeries("Today", AUTOSORT_SETTING, ALLOW_DUPS_SETTING);
     private final XYSeries dataRCur = new XYSeries("Now", AUTOSORT_SETTING, ALLOW_DUPS_SETTING);
     
     /** Returns the current time in units of 8ns (with millisecond resolution) */
     public static final long currentTime8ns() {
         return System.currentTimeMillis() * MSEC_DIV_8NS;
     }
     
     /** Converts router time to local time. */
     public final long routerTimeToLocalTime8ns( long rtr_time_ns8 ) {
         return rtr_time_ns8 + time_offset_ns8;
     }
     
     /** Puts xys into manual notification mode and sets a limit on the number of data points it may track. */
     public static void prepareXYSeries( XYSeries xys, int maxDataPoints ) {
         xys.setNotify( false );
         xys.setMaximumItemCount( maxDataPoints );
     }
     
     
     private static final int bitsToRateRangeUnits( int num_bits ) {
         return num_bits / 1000;
     }
     
     private static final int bytesToSizeRangeUnits( int num_bytes ) {
         return num_bytes / 1024;
     }
     
     private void addDataPointToRateData( long time_ns8 ) {
         dataRateLimit.add( time_ns8, bitsToRateRangeUnits(this.rateLimit_kbps*1000), false );
     }
     
     private void addDataPointToBufferSizeData( long time_ns8, int curBufSize_bytes ) {
         dataBufSize.add( time_ns8, bytesToSizeRangeUnits(curBufSize_bytes), false );
     }
     
     private void addDataPointToRateAndBufferSizeData( long time_ns8, int curBufSize_bytes ) {
         addDataPointToRateData(time_ns8);
         addDataPointToBufferSizeData(time_ns8, curBufSize_bytes);
     }
     
     private void addDataPointToQueueOccData( long time_ns8 ) {
         if( dgu.bufsizing.control.EventProcessor.USE_PACKETS ) {
             dataQueueOcc.add( time_ns8, queueOcc_bytes );
             
             if( this.getQueueOcc_bytes() == 0 )
                 dataQueueOccPer.add( time_ns8, 0 );
             else
                 dataQueueOccPer.add( time_ns8, queueOcc_bytes / this.getQueueOcc_bytes() );
         }
         else {
             dataQueueOcc.add( time_ns8, bytesToSizeRangeUnits(queueOcc_bytes) );
             if( this.getActualBufSize() == 0 )
                 dataQueueOccPer.add( time_ns8, 0 );
             else
                 dataQueueOccPer.add( time_ns8, bytesToSizeRangeUnits(queueOcc_bytes / this.getActualBufSize()) );
         }
     }
     
     private void addDataPointToXputData( long time_ns8, int xput_bps ) {
         dataThroughput.add( time_ns8, bitsToRateRangeUnits(xput_bps), false );
         dataThroughputPer.add( time_ns8, xput_bps / (double)(this.getRateLimit_kbps() * 1000), false );
     }
     
     public synchronized void clearInProgressPoint() {
         dataRCur.clear();
     }
     
     /**
      * Constructs a new uni-directional bottleneck link between src and dst.
      * @param src               The source of data on this link.
      * @param dst               The endpoint of this link.
      * @param queueID           The interface index for the queue on src for this link.
      * @param rtt               Initial rtt for tgens across this link
      * @param rateLimit_kbps    Initial rate limit in kilobits per second
      * @param dataPointsToKeep  Maximum number of data points to keep around
      * @throws IllegalArgValException  thrown if too many links already exist from src
      */
     public BottleneckLink( Router src, Node dst, int queueID,
                            int rtt, int rateLimit_kbps, int dataPointsToKeep  ) 
                            throws IllegalArgValException {
         super( src, dst, queueID );
         
         prepareXYSeries( dataThroughput,    dataPointsToKeep );
         prepareXYSeries( dataThroughputPer, dataPointsToKeep );
         prepareXYSeries( dataQueueOcc,      dataPointsToKeep );
         prepareXYSeries( dataQueueOccPer,   dataPointsToKeep );
         
         prepareXYSeries( dataBufSize,   dataPointsToKeep );
         prepareXYSeries( dataRateLimit, dataPointsToKeep );
         
         prepareXYSeries( dataRTheROT,   dataPointsToKeep );
         prepareXYSeries( dataRTheGuido, dataPointsToKeep );
         prepareXYSeries( dataRMea,      dataPointsToKeep );
         prepareXYSeries( dataRToday,    dataPointsToKeep );
         prepareXYSeries( dataRCur,      dataPointsToKeep );
         
         // set the initial values
         this.rtt_ms = 0;
         this.rateLimit_kbps = 0;
         this.numFlows = 0;
         this.selected = false;
         this.tgen = null;
         setTGen( Iperf.class );
         
         // update the plots appropriately
         forceSet = true;
         setNumFlows(0);
         setRTT_ms( rtt  );
         setRateLimit_kbps( rateLimit_kbps );
         forceSet = false;
         
         initMeasuredResults();
     }
     
     /**
      * Constructs the current gradient color.
      * @param goodness  1.0 => green, 0.0 => red, between the two => constant hue mix
      * @return Color based on the current goodness
      */
     public static Color getCurrentGradientColor( float goodness ) {
         float extra = 2.0f * (0.5f - Math.abs( goodness - 0.5f ));
         return new Color( 1.0f - goodness, goodness, extra );
     }
     
     public void draw( Graphics2D gfx ) {
         // get a local copy of the current utilization
         float saturation = instantaneousUtilization;
         float queue_usage = instantaneousQueueOcc;
         
         // draw the outline of the bottleneck link
         if( selected ) {
             gfx.setColor( Drawable.COLOR_SELECTED );
             gfx.setStroke( STROKE_BOTTLENECK_SEL_OUTLINE );
         }
         else
             gfx.setStroke( STROKE_BOTTLENECK_OUTLINE );
         gfx.drawLine( src.getX(), src.getY(), dst.getX(), dst.getY() );
         
         // draw the inner part of the bottleneck link (redder => less saturated/lower utilization)
         gfx.setColor( getCurrentGradientColor(saturation) );
         gfx.setStroke( STROKE_BOTTLENECK );
         gfx.drawLine( src.getX(), src.getY(), dst.getX(), dst.getY() );
 
         // draw the queue
         gfx.setColor( Drawable.COLOR_DEFAULT );
         gfx.setStroke( Drawable.STROKE_THICK );
         int x = src.getX() - QUEUE_WIDTH / 2;
         int y = src.getQueueY( gfx ) + QUEUE_HEIGHT * queueID;
         gfx.drawLine( x, y, x + QUEUE_WIDTH, y );
         gfx.drawLine( x, y + QUEUE_HEIGHT, x + QUEUE_WIDTH, y + QUEUE_HEIGHT );
         gfx.drawLine( x + QUEUE_WIDTH, y, x + QUEUE_WIDTH, y + QUEUE_HEIGHT );
         
         // fill the queue based on current occupancy
         gfx.setColor( COLOR_QUEUE_FILL );
         gfx.setStroke( STROKE_OCC );
         int width = (int)(QUEUE_WIDTH * queue_usage);
         int fillX = x + (QUEUE_WIDTH - width) - 1;
         int fillY = y + QUEUE_HEIGHT / 2;
         gfx.drawLine( fillX, fillY, fillX + width, fillY );
         
         // draw marker near bottom of queue to finish the queue symbol
         gfx.setColor( Color.RED );
         gfx.setStroke( Drawable.STROKE_THICK );
         x += QUEUE_WIDTH * 4 / 5;
         gfx.drawLine( x, y + QUEUE_HEIGHT / 5, x, y + QUEUE_HEIGHT * 4 / 5 );
         
         // restore defaultsAndBufferSize
         gfx.setPaint( Drawable.PAINT_DEFAULT );
         gfx.setStroke( Drawable.STROKE_DEFAULT );
         
         // for now, do NOT paint the name of the interface
         // GUIHelper.drawCeneteredString( dst.getName(), gfx, x + QUEUE_WIDTH / 2, fillY + 4 );
     }
     
     /**
      * Synchronizes the approximate offset between our time domain and the 
      * router's timestamps.  It uses current time and doesn't do anything smart 
      * about incorporating round trip times as small differences here will not
      * matter in the long run.  We just need something "reasonably close."  This
      * method will only have an effect the first time it is called.
      * 
      * @param rtr_time_ns8  the router time in units of 8ns
      * @return true if the update is not older than the most recent update
      */
     public synchronized boolean prepareForUpdate( long rtr_time_ns8 ) {
         // initialize the offset if not already done
         if( time_offset_ns8 == 0 ) {
             // convert millis to units of 8ns
             time_offset_ns8 = currentTime8ns() - rtr_time_ns8;
         }
         
         // return true if the update is newer
         return prev_time_offset_end_ns8 <= rtr_time_ns8;
     }
     
     public synchronized void setOccupancy( long rtr_time_ns8, int num_bytes ) {
         // add the old data point
         addDataPointToQueueOccData( routerTimeToLocalTime8ns(rtr_time_ns8) );
         
         //add the new data point
         queueOcc_bytes = num_bytes;
         addDataPointToQueueOccData( routerTimeToLocalTime8ns(rtr_time_ns8) );
     }
     
     public synchronized void arrival( long rtr_time_ns8, int num_bytes ) {
         setOccupancy( rtr_time_ns8, queueOcc_bytes + num_bytes );
     }
     
     public synchronized void departure( long rtr_time_ns8, int num_bytes ) {
         setOccupancy( rtr_time_ns8, queueOcc_bytes - num_bytes );
         bytes_sent_since_last_update += num_bytes;
         //c_bytes += num_bytes;
     }
     
     public synchronized void dropped( long rtr_time_ns8, int num_bytes ) {
         setOccupancy( rtr_time_ns8, queueOcc_bytes - num_bytes );
     }
     
     public int getQueueOcc_bytes() {
         return queueOcc_bytes;
     }
     
     //public static long c_pkts = 0, c_bytes = 0;
     private float cont_throughput_bps = 0;
     
     private boolean noteCurrentXputValue = true;
     private int avgThroughput_bps = 0;
     private int numReadingsForAvgXput = 0;
     public synchronized void resetXputMovingAverage() {
         avgThroughput_bps = 0;
         numReadingsForAvgXput = 0;
     }
     public synchronized int getXputMovingAverage() {
         return avgThroughput_bps;
     }
     
     public synchronized void refreshInstantaneousValues( long rtr_time_ns8 ) {
         // don't compute instantaneous values until we have received > 1 packet
         if( prev_time_offset_end_ns8 == 0 ) {
             prev_time_offset_end_ns8 = rtr_time_ns8;
             bytes_sent_since_last_update = 0;
             return;
         }
         
         //c_pkts += 1;
         //System.err.println( "==> " + c_pkts + " / " + c_bytes );
         
         // compute xput b/w end of last update and now (+1 to avoid div by 0 ... 8ns won't matter)
         float time_passed_ns8 = rtr_time_ns8 - prev_time_offset_end_ns8 + 1;
         float throughput_bps = (8 * bytes_sent_since_last_update * SEC_DIV_8NS) / time_passed_ns8;
         int actualThroughput_bps = (int)throughput_bps;
         
         throughput_bps = cont_throughput_bps = cont_throughput_bps*0.5f + throughput_bps*0.5f;
         bytes_sent_since_last_update = 0;
         prev_time_offset_end_ns8 = rtr_time_ns8;
         
         // set new instantaneous utilizatoin value
         int rateLimit_bps = rateLimit_kbps * 1000;
         if( throughput_bps < rateLimit_bps )
             instantaneousUtilization = throughput_bps / (float)rateLimit_bps;
         else
             instantaneousUtilization = 1.0f;
         
         // compute the new instantaneous queue occupancy
         int bufSize_bytes = getActualBufSize();
         if( queueOcc_bytes < bufSize_bytes )
             instantaneousQueueOcc = queueOcc_bytes / (float)bufSize_bytes;
         else
             instantaneousQueueOcc = 1.0f;
         
         // plot the new throughput value
         long t = routerTimeToLocalTime8ns(rtr_time_ns8);
         this.addDataPointToXputData( t,  (int)throughput_bps);
         extendUserDataPoints( t );
         
         // update the throughput moving average
         avgThroughput_bps = (actualThroughput_bps + avgThroughput_bps*numReadingsForAvgXput) / (numReadingsForAvgXput+1);
         numReadingsForAvgXput += 1;
         
         // plot the current value on the util plot too
         if( noteCurrentXputValue )
             noteCurrentMeasuredResult(getActualBufSize(), 0);
     }
 
     public XYSeries getDataRTheROT() {
         return dataRTheROT;
     }
 
     public XYSeries getDataRTheGuido() {
         return dataRTheGuido;
     }
 
     public XYSeries getDataRMea() {
         return dataRMea;
     }
 
     public XYSeries getDataRToday() {
         return dataRToday;
     }
     
     public XYSeries getDataRCur() {
         return dataRCur;
     }
     
     public class Result {
         public static final int BASE_RTT = 250;
         
         public final int b_B;
         public final int c_kbps;
         public final int numDataPoints;
         public Result(int b, int c, int r, int num) { 
             c_kbps = c;
             numDataPoints = num;
             
             // scale to base r so our data is rtt-independent
             double scale = r / (double)BASE_RTT;
             b_B = (int)(b * scale);
         }
         
         public int getScaledBufSize_B(int rtt_ms) {
             double scale = rtt_ms / (double)BASE_RTT;
            return (int)(b_B * scale);
         }
     }
     
     HashMap<Integer, Result> resultsMea = new HashMap<Integer, Result>();
     public static final int[] interestingN = new int[]{1,5,10,25,50,75,100,150,200};
     
     /** read measured results from measured.txt */
     private void initMeasuredResults() {
         try {
             File file = new File(SAVED_RESULTS_FN);
             FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr);
 
             while( true ) {
                 String line = br.readLine();
                 if( line == null )
                     break;
                         
                 // ignore comments
                 if( line.charAt(0) == '#' )
                     continue;
                 
                 String[] vals = line.split(" ");
                 if( vals.length != 4 ) {
                     System.err.println("Error: invalid line format in " + SAVED_RESULTS_FN + ": " + line);
                     System.exit(1);
                 }
                 
                 int n = Integer.valueOf(vals[0]);
                 int b = Integer.valueOf(vals[1]);
                 int c = Integer.valueOf(vals[2]);
                 int num = Integer.valueOf(vals[3]);
                     
                 resultsMea.put(n, new Result(b,c,Result.BASE_RTT,num));
             }
 
             br.close();
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         
         populateMeasuredResults();
     }
     
     /** refresh measured results data */
     private void populateMeasuredResults() {
         dataRMea.clear();
         
         int rtt = rtt_ms;
         Vector<Integer> nVals = new Vector<Integer>(resultsMea.keySet());
         java.util.Collections.sort(nVals);
         for( Integer n : nVals ) {
             Result r = resultsMea.get(n);
             if( r.c_kbps == this.getRateLimit_kbps() )
                 dataRMea.add(n.intValue(), r.getScaledBufSize_B(rtt) / 1024);
         }
     }
     
     /** 
      * specifies the measured buffer size 'b' for the current capacity (rate limit)
      * @param bufSizeForMaxUtil_kB  buffer size for 100% utilization in kilobytes
      */
     public synchronized void addMeasuredResult(int bufSizeForMaxUtil_B) {
         int n = this.getNumFlows();
         Result r = new Result( bufSizeForMaxUtil_B, this.getRateLimit_kbps(), this.getRTT_ms(), 1 );
         dataRToday.add(n, bufSizeForMaxUtil_B / 1024);
         System.out.println( n + " " + r.b_B + " " + r.c_kbps + " " + r.numDataPoints );
     }
     
     /** 
      * specifies the in progress measured buffer size 'b' for the current capacity (rate limit)
      * @param bufSizeForMaxUtil_B  buffer size for 100% utilization in bytes
      * @param confidence  confidence (0 => none, 1=> very)
      */
     public synchronized void noteCurrentMeasuredResult(int bufSizeForMaxUtil_B, double confidence) {
         dataRCur.clear();
         dataRCur.add(this.getNumFlows(), bufSizeForMaxUtil_B / 1024);
       
         // update the points color based on the confidence
         int r = (confidence <  0.5) ? 128 + (int)(128*(0.5 - confidence)) : 0;
         int g = (confidence >= 0.5) ? 128 + (int)(128*(confidence - 0.5)) : 0;
         DemoGUI.me.resultsRenderer.setSeriesPaint(4, new java.awt.Color(r,g,0));
         
         theta += (5 * Math.PI / 180.0);
         for( int i=0; i<starXRot.length; i++ ) {
             starXRot[i] = (int)(starX[i]*Math.cos(theta) - starY[i]*Math.sin(theta));
             starYRot[i] = (int)(starX[i]*Math.sin(theta) + starY[i]*Math.cos(theta));
         }
         DemoGUI.me.resultsRenderer.setSeriesShape(4, new java.awt.Polygon(starXRot, starYRot, starXRot.length), false);
     }
     private double theta = 0;
     private static final int starX[] = { -7, -2, 2, 7, 3, 3, 7, 2, -2, -7, -3, -3, -7 };
     private static final int starY[] = { -7, -3, -3, -7, -2, 2, 7, 3, 3, 7, 2, -2, -7 };
     private static final int starXRot[] = new int[starX.length];
     private static final int starYRot[] = new int[starY.length];
     
     public synchronized void populateTheoreticalResults() {
         // recompute the theoretical
         dataRTheROT.clear();
         dataRTheGuido.clear();
         for( int n=1; n<=interestingN[interestingN.length-1]; n++ ) {
             dataRTheROT.add(n, computeBufSize( BufferSizeRule.RULE_OF_THUMB, 
                                                this.getRTT_ms(), 
                                                this.getRateLimit_kbps(), 
                                                n ) / 1024);
              
             dataRTheGuido.add(n, computeBufSize( BufferSizeRule.FLOW_SENSITIVE, 
                                                  this.getRTT_ms(), 
                                                  this.getRateLimit_kbps(), 
                                                  n ) / 1024);
         }
     }
     
     public synchronized void clearData() {
         dataThroughput.clear( false );
         dataThroughputPer.clear( false );
         dataQueueOcc.clear(   false );
         dataQueueOccPer.clear(   false );
         dataBufSize.clear(    false );
         dataRateLimit.clear(  false );
         
         // re-start the graphs with the thresholds
         long time_ns8 = currentTime8ns();
         int curBufSize = getActualBufSize();
         addDataPointToRateAndBufferSizeData(time_ns8, curBufSize);
         addDataPointToRateAndBufferSizeData(time_ns8, curBufSize);
     }
     
     public synchronized void extendUserDataPoints( long time_ns ) {
         // remove the old temporary endpoints of user-controlled values
         if( dataBufSize.getItemCount() > 0 ) {
             dataBufSize.remove( dataBufSize.getItemCount() - 1, false );
             dataRateLimit.remove( dataRateLimit.getItemCount() - 1, false );
         }
         
         // add the new updated endpoints of user-controlled values and refresh the plot
         addDataPointToRateData(time_ns);
         addDataPointToBufferSizeData(time_ns, lastBufSize_bytes);
     }
     
     public BufferSizeRule getBufSizeRule() {
         return bufSizeRule;
     }
 
     public synchronized void setBufSizeRule( BufferSizeRule bufSizeRule ) {
         if( this.bufSizeRule == bufSizeRule )
             return;
         
         this.bufSizeRule = bufSizeRule;
         updateActualBufSize();
     }
     
     public int getNumFlows() {
         return numFlows;
     }
     
     public synchronized void setNumFlows(int n) {
         this.numFlows = n;
         updateActualBufSize();
         if( DemoGUI.me != null )
             DemoGUI.me.setNumFlowsText(this);
         
         tgen.setNumFlows(n);
     }
 
     public synchronized void adjustNumFlows( int adjust ) {
         setNumFlows( this.numFlows + adjust );
     }
     
     /**
      * Returns the current buffer size in bytes.
      */
     public synchronized int getActualBufSize() {
         return getActualBufSize(bufSizeRule);
     }
     
     /**
      * Returns the buffer size which would be specified by bufSizeRule with the 
      * current parameters.  The units on the return value is bytes.
      */
     public synchronized int getActualBufSize( BufferSizeRule bufSizeRule ) {
         if( bufSizeRule == BufferSizeRule.CUSTOM )
             return customBufSize_bytes;
         
         return computeBufSize( bufSizeRule, this.getRTT_ms(), this.getRateLimit_kbps(), this.getNumFlows() );
     }
     
     /**
      * Returns the buffer size which would be dictated by the specified rule and 
      * link parameters.  The units on the return value is bytes.
      */
     public static int computeBufSize( BufferSizeRule bufSizeRule, int rtt_ms, int capacity_kbps, int n ) {
         // prevent div by zero
         if( n == 0 )
             n = 1;
         
         switch( bufSizeRule ) {
             case RULE_OF_THUMB:  return rtt_ms * capacity_kbps / 8;
             case FLOW_SENSITIVE: return (int)(rtt_ms * capacity_kbps / (8 * Math.sqrt(n)));
             
             default:             throw( new Error("Error: unknown rule: " + bufSizeRule) );
         }
     }
     
     private synchronized void updateActualBufSize() {
         // refresh the GUI no matter what
         if( DemoGUI.me != null ) DemoGUI.me.setBufferSizeText( this );
         
         long t = currentTime8ns();
         int curBufSize_bytes = getActualBufSize();
         if( lastBufSize_bytes == curBufSize_bytes )
             return; /* no change */
         
         // add endpoint if last has a meaningful value
         if( lastBufSize_bytes >= 0 )
             addDataPointToBufferSizeData(t, lastBufSize_bytes);
         
         // and start point for new buffer size
         addDataPointToBufferSizeData(t, curBufSize_bytes);
         addDataPointToBufferSizeData(t, curBufSize_bytes);
         lastBufSize_bytes = curBufSize_bytes;
         
         // tell the router about the new buffer size in terms of packets
         int numPackets = curBufSize_bytes / BYTES_PER_PACKET;
         if( curBufSize_bytes > 0 && numPackets == 0 ) numPackets = 1;
         this.src.getController().command( RouterCmd.CMD_SET_BUF_SZ, queueID, numPackets );
     }
 
     public synchronized int getCustomBufSize() {
         return customBufSize_bytes;
     }
     
     public synchronized void setCustomBufSize(int numBytes) {
         if( customBufSize_bytes != numBytes ) {
             customBufSize_bytes = numBytes;
             if( bufSizeRule == BufferSizeRule.CUSTOM )
                 updateActualBufSize();
             else
                 if( DemoGUI.me != null ) DemoGUI.me.setBufferSizeText( this );
         }
     }
     
     public int getRTT_ms() {
         return rtt_ms;
     }
     
     public synchronized void setRTT_ms( int rtt_ms  ) {
         if( this.rtt_ms == rtt_ms && !forceSet )
             return;
         
         // set the new buffer size
         this.rtt_ms = rtt_ms;
         DemoGUI.me.mnuSetRTT.setText( "Set RTT (" + rtt_ms + "ms)" );
         
         // tell the router about the new buffer size in terms of packets
         updateActualBufSize();
         
         // refresh the measured results data being displayed (displays for the specified capacity)
         populateTheoreticalResults();
         populateMeasuredResults();
     }
 
     public int getRateLimit_kbps() {
         return rateLimit_kbps;
     }
 
     public int getRateLimit_regValue() {
         return getRateLimit_regValue(rateLimit_kbps*1000);
     }
     
     /** returns the register value to achieve the requested rate in bps */
     public int getRateLimit_regValue(long rate_bps) {
         // translate the requested to rate to the register value to get the closest rate
         byte regValue = 0;
         while( rate_bps < DemoGUI.RATE_LIM_MAX_RATE ) {
             rate_bps *= 2;
             regValue += 1;
             
             // stop at the max value
             if( regValue == DemoGUI.RATE_LIM_VALUE_COUNT - 1 )
                 break;
         }
         
         return regValue + 1; // + 1 to compensate for the hw off by one ...
     }
     
     public synchronized void setRateLimit_kbps(int rate_kbps) {
         setRateLimitReg( getRateLimit_regValue(rate_kbps*1000) );
     }
         
     public synchronized void setRateLimitReg(int regValue) {
         if( regValue < DemoGUI.RATE_LIM_MIN_REG_VAL ) {
             System.err.println( "setRateLimitReg Error: " + regValue + " is too small of a register value (e.g. too high of a rate)" );
             return;
         }
         else if( regValue >= DemoGUI.RATE_LIM_VALUE_COUNT ) {
             System.err.println( "setRateLimitReg Error: " + regValue + " is too big of a register value (e.g. too low of a rate)" );
             return;
         }
         
         // translate the requested rate into the closest attainable rate
         int new_rateLimit_kbps = RouterController.translateRateLimitRegToBitsPerSec( regValue ) / 1000;
         
         // do nothing if the requested rate hasn't changed since the last request
         if( rateLimit_kbps == new_rateLimit_kbps && !forceSet )
             return;
         
         // tell our traffic generator what to do if needs to know
         tgen.setXput_bps(new_rateLimit_kbps * 1000);
         
         // add the end point of the old rate
         long t = currentTime8ns();
         addDataPointToRateData(t);
         
         // set the new buffer size
         rateLimit_kbps = new_rateLimit_kbps;
         if( DemoGUI.me != null ) DemoGUI.me.setRateLimitText( this );
         updateActualBufSize();
         
         // tell the router about the new rate limit
         src.getController().command( RouterCmd.CMD_SET_RATE, queueID, regValue );
         
         // add the start point of the new rate and buffer size
         addDataPointToRateData(t);
         addDataPointToRateData(t);
         
         // refresh the measured results data being displayed (displays for the specified capacity)
         populateTheoreticalResults();
         populateMeasuredResults();
     }
 
     public Class getTGen() {
         return tgen.getClass();
     }
     
     public void setTGen( Class cls ) {
         if( tgen != null )
             tgen.destroy();
             
         if( cls == Iperf.class ) 
             tgen = new Iperf(Demo.DEFAULT_DST_IP);
         else if( cls == Tomahawk.class )
             tgen = new Tomahawk(Demo.DEFAULT_DST_IP);
         else
             throw( new Error("BottleneckLink::setTGen does not yet support " + tgen.getClass().getName()) );
     }
     
     public XYSeries getDataThroughput() {
         return dataThroughput;
     }
     
     public XYSeries getDataThroughputPer() {
         return dataThroughputPer;
     }
 
     public XYSeries getDataQueueOcc() {
         return dataQueueOcc;
     }
 
     public XYSeries getDataQueueOccPer() {
         return dataQueueOccPer;
     }
     
     public XYSeries getDataBufSize() {
         return dataBufSize;
     }
 
     public XYSeries getDataRateLimit() {
         return dataRateLimit;
     }
     
     public boolean isSelected() {
         return selected;
     }
 
     public void setSelected(boolean selected) {
         this.selected = selected;
     }
     
     public String toString() {
         return src.toString() + " ---> " + dst.toString();
     }
 }
