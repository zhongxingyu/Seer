 package trading;
 
 /**
  * Class for Triangular Moving Strategy (TMA)
  * @author dbhage
  */
 public class TMAStrategy extends AStrategy implements Runnable
 {
     /** Float Arrays for SMA Values */
     private float[] slowSMAValues = new float[Prices.MAX_SECONDS];
     private float[] fastSMAValues = new float[Prices.MAX_SECONDS];
 
     /** Values of N */
     private final int N_FAST = 5;
     private final int N_SLOW = 20;
 
     /** the tick we are at */
     private int currentTick = 0;
 
     /** Float Arrays for TMA Values */
     private float[] slowTMAValues = new float[Prices.MAX_SECONDS];
     private float[] fastTMAValues = new float[Prices.MAX_SECONDS];
 
     /** latest TMA Value - used when updating */
     private float latestTMAValueSlow = 0;
     private float latestTMAValueFast = 0;
     
     /** checking whether the faster TMA has a greater value than the slower TMA */
     private boolean fasterThenSlower;
   
     /** Prices object to get current price */
     private Prices prices = null;
     
     /**
      * Constructor
      */
     public TMAStrategy(Prices priceList)
     {
        super();
        prices = priceList;
        for (int i=0;i<Prices.MAX_SECONDS;i++)
        {
            slowSMAValues[i] = 0;
            fastSMAValues[i] = 0;
            slowTMAValues[i] = 0;
            fastTMAValues[i] = 0;
        }
     }
     
     /**
      * Run
      */
     public void run()
     {
         while(currentTick != Prices.MAX_SECONDS)
         {
             runStrategy();
            ++currentTick;
         }
     }
     
     /**
      * Run the TMA strategy
      */
     public void runStrategy()
     {
         computeSlowSMA();
         computeFastSMA();
         
         computeSlowTMA(currentTick, N_SLOW);
         computeFastTMA(currentTick, N_FAST);
 
         detectCross();
         
     }
     
     /**
      * update TMA(20)
      * @param t
      */
     public void computeSlowTMA(int t, int n)
     {
         if (t<n)
         {
             // calculation similar to LWMA
             if (t==0)
             {
                 slowTMAValues[t] = slowSMAValues[t];
             }
             else
             {
                 for (int k=0; k<=t; k++)
                 {
                     slowTMAValues[t] += slowSMAValues[t];
                 }
             }
             slowTMAValues[t] /= t+1;
             latestTMAValueSlow = slowTMAValues[t];
         }
         else
         {
             // TMA's formula
             latestTMAValueSlow = slowTMAValues[t-1];
             latestTMAValueSlow = latestTMAValueSlow - (slowSMAValues[t-n]/n) + (slowSMAValues[t]/n);
             slowTMAValues[t] = latestTMAValueSlow;
         }
     }
     
     /**
      * update TMA(5)
      * @param t - the tick
      * @param n - N
      */
     public void computeFastTMA(int t, int n)
     {
         if (t<n)
         {
             if (t==0)
             {
                 fastTMAValues[t] = fastSMAValues[t];
             }
             else
             {
                 for (int k=0; k<=t; k++)
                 {
                     fastTMAValues[t] += fastSMAValues[k];
                 }
                 fastTMAValues[t] /= t+1;
             }
             latestTMAValueFast = fastTMAValues[t];
         }
         else
         {
             // TMA's formula
             latestTMAValueFast = fastTMAValues[t-1];
             latestTMAValueFast = latestTMAValueFast - (fastSMAValues[t-n]/n) + (fastSMAValues[t]/n);
             fastTMAValues[t] = latestTMAValueFast;
         }
     }
     
     /**
      * Compute Slow SMA
      */
     public void computeSlowSMA()
     {
         if (currentTick < N_SLOW)
         {
             for (int i = 0; i <= currentTick; i++)
             {
                 slowSMAValues[currentTick] += prices.GetPrice(i);
             }
             if (currentTick > 0)
             {
                 slowSMAValues[currentTick] /= currentTick + 1;
             }
         }
         else
         {
             slowSMAValues[currentTick] = slowSMAValues[currentTick - 1] + (prices.GetPrice(currentTick) - prices.GetPrice(currentTick - N_SLOW)) / N_SLOW;
         }
     }
     
     /**
      * Compute Fast SMA
      */
     public void computeFastSMA()
     {
         if (currentTick < N_FAST)
         {
             for (int i = 0; i <= currentTick; i++)
             {
                 fastSMAValues[currentTick] += prices.GetPrice(i);
             }
             if (currentTick != 0)
             {
                 fastSMAValues[currentTick] /= currentTick + 1;
             }
         }
         else
         {
             fastSMAValues[currentTick] = fastSMAValues[currentTick - 1]  + (prices.GetPrice(currentTick) - prices.GetPrice(currentTick - N_FAST)) / N_FAST;
         }
     }
     
     /**
      * Get the current tick
      * @return the current tick
      */
     public int getTick()
     {
         return currentTick;
     }
 
     /**
      * Check for crossover
      * @return 'B' for buy, 'S' for sell, 'D' for do nothing
      */
     public void crossover(boolean fastGreaterThanSlow)
     {
         //if(prices.getStop()) return;
         
         if(fastGreaterThanSlow)
         {
             // buy
             write(currentTick, 'B', Trader.getTrader().trade('B'));            
         }
         else
         {
             // sell
             write(currentTick, 'S', Trader.getTrader().trade('S')); 
         }
     }  
     
     private void detectCross(){
 
         if(fastTMAValues[currentTick] > slowTMAValues[currentTick] && slowTMAValues[currentTick-1] > fastTMAValues[currentTick-1]){
             // upward trend - report buy
             write(currentTick, 'B', Trader.getTrader().trade('B'));
 //            System.out.println("Time: "+currentTick+" --  Buy");
         }else if(fastTMAValues[currentTick] < slowTMAValues[currentTick] && slowTMAValues[currentTick-1] < fastTMAValues[currentTick-1]){
             // downward trend - report sell
             write(currentTick, 'S', Trader.getTrader().trade('S'));
 //            System.out.println("Time: "+currentTick+" --  Sell");
         }else{
              // do nothing
             write(currentTick,'D',prices.GetPrice(currentTick));
 //            System.out.println("Time: "+currentTick+" --  Nothing");
         }
     }
     
     public float getTMAFastValue(int t)
     {
         return fastTMAValues[t];
     }
     
     public float getTMASlowValue(int t)
     {
         return slowTMAValues[t];
     }
     
 
 /*	public void test() {
 		double[] ps = { 61.590, 61.440, 61.320, 61.670, 61.920, 62.610, 62.880,
 				63.060, 63.290, 63.320, 63.260, 63.120, 62.240, 62.190, 62.890 };
 		this.currentTick = 0;
 		for (double d : ps) {
 			prices.SetPrice(this.currentTick, (float) d);
 			runStrategy();
 			System.out.println(fastTMAValues[currentTick]+" --- "+slowTMAValues[currentTick]);
 			++currentTick;
 		}
 		for (int i = 0; i < ps.length; i++) {
 			System.out.print(fastTMAValues[i]+" - ");
 		}
 		System.out.println();
 		for (int i = 0; i < ps.length; i++) {
 			System.out.print(slowTMAValues[i]+" - ");
 		}
 	}
 
 	public static void main(String[] args) {
 		Prices p = Prices.GetPrices();
 		(new TMAStrategy(p)).test();
 	}*/
 	
 	
 }
