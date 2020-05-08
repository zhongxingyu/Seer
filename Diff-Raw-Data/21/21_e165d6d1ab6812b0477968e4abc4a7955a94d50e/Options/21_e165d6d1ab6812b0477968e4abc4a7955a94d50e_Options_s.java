 import java.util.Random;
 
 public class Options
 {
     public int shapesQueueSize = Constants.DEFAULT_SHAPES_QUEUE_SIZE;
     public Random random;
     public int maxMoves = Integer.MAX_VALUE;
     public boolean exactSearch = false;
     public double gameScoreWeight = 10;
     public double boardHeightWeight = -5;
     public double boardOverhangsWeight = -10;
 
     public Options()
     {
         if(Constants.seed == 0)
         {
             random = new Random();
         }
         else
         {
             random = new Random(Constants.seed);
         }
     }
 }
