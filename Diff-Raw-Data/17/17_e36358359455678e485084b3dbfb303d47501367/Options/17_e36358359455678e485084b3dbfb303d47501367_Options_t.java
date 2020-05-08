 import org.jgap.IChromosome;
 
 import java.util.Random;
 
 public class Options
 {
     public int shapesQueueSize = Constants.DEFAULT_SHAPES_QUEUE_SIZE;
     public Random random;
     public int maxMoves = Integer.MAX_VALUE;
     public boolean exactSearch = false;
     public double gameScoreWeight = Constants.DEFAULT_GAME_SCORE_WEIGHT;
     public double boardHeightWeight = Constants.DEFAULT_BOARD_HEIGHT_WEIGHT;
     public double boardOverhangsWeight = Constants.DEFAULT_BOARD_OVERHANG_WEIGHT;
 
     public Options()
     {
         this(null);
     }
 
     public Options(IChromosome chromosome)
     {
         if(Constants.seed == 0)
         {
             random = new Random();
         }
         else
         {
             random = new Random(Constants.seed);
         }
 
         if(chromosome != null)
         {
            gameScoreWeight = (Double) chromosome.getGene(0).getAllele();
            boardHeightWeight = (Double) chromosome.getGene(1).getAllele();
            boardOverhangsWeight = (Double) chromosome.getGene(2).getAllele();
         }
     }
 
     @Override
     public Options clone()
     {
         Options clonedOptions = new Options();
         clonedOptions.shapesQueueSize = this.shapesQueueSize;
         clonedOptions.maxMoves = this.maxMoves;
         clonedOptions.exactSearch = this.exactSearch;
         clonedOptions.gameScoreWeight = this.gameScoreWeight;
         clonedOptions.boardHeightWeight = this.boardHeightWeight;
         clonedOptions.boardOverhangsWeight = this.boardOverhangsWeight;
         
         return clonedOptions;
     }
 
     public void print()
     {
         System.out.println("Game score weight: " + gameScoreWeight);
         System.out.println("Board height weight: " + boardHeightWeight);
         System.out.println("Board overhang weight: " + boardOverhangsWeight);
     }
 }
