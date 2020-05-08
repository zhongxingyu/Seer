 
 
 /**
  * Unweighted mean of the last N data points
  *
  * @author Team Gredona
  */
 public class SimpleMovingAverage extends Strategy {
 
     /**
      * for use by Triangular Moving Averages
      */
     protected CircularFIFOBuffer<Float> fastSMABuffer = new CircularFIFOBuffer<Float>(FAST_PERIOD);
     protected CircularFIFOBuffer<Float> slowSMABuffer = new CircularFIFOBuffer<Float>(SLOW_PERIOD);
 
     public SimpleMovingAverage(GraphData g) {
         super(g);
         type = "Simple Moving Average";
         acronym = "SMA";
         typeInt = 0;
     }
 
     /**
      * @return
      */
     @Override
     protected float computeSlowMovingAverage() {
         int t = slowDataBuffer.size();
         
         //for the first N = SLOW_PERIOD = 20 data points, we simply take the average
         if (SLOW_PERIOD > t) {
             float sum = 0;
             for (float datapoint : slowDataBuffer) {
                 sum += datapoint;
             }
            float v = sum/t;
            slowSMABuffer.add(v);
            myGraphData.pushSlowSMA(v); 
            return v;
         }
 
         //When calculating successive values, a new value comes into the sum and and old one drops out, meaning full summation each time isn't necessary
         float SMA = currentSlowMovingAverage - oldestSlowDatapoint / SLOW_PERIOD + slowDataBuffer.peekLast() / SLOW_PERIOD;
         slowSMABuffer.add(SMA);
         myGraphData.pushSlowSMA(SMA);
         return SMA;
     }
 
     @Override
     protected float computeFastMovingAverage() {
         int t = fastDataBuffer.size();
         
         //for the first N = FAST_PERIOD = 5 data points, we simply take the average
         if (FAST_PERIOD > t) {
             float sum = 0;
             for (float datapoint : fastDataBuffer) {
                 sum += datapoint;
             }
            float v = sum/t;
            fastSMABuffer.add(v);
            fastSMABuffer.add(v); 
            return v;
         }
         
         //When calculating successive values, a new value comes into the sum and and old one drops out, meaning full summation each time isn't necessary
         float FMA = currentFastMovingAverage - oldestFastDatapoint / FAST_PERIOD + fastDataBuffer.peekLast() / FAST_PERIOD;
         myGraphData.pushFastSMA(FMA);
         fastSMABuffer.add(FMA);
         return FMA;
     }
 
     /**
      *
      * @param index of the element
      * @return the float at the index of slowSMABuffer
      */
     public float getSlowBufferValue(int index) {
         return slowSMABuffer.get(index);
     }
 
     /**
      * @param index of the element
      * @return the float at the index of fastSMABuffer
      */
     public float getFastBufferValue(int index) {
         return fastSMABuffer.get(index);
     }
 }
