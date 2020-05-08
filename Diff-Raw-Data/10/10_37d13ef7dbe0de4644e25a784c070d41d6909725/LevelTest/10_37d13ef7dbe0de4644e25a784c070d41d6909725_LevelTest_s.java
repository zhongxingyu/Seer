 package cz.sparko.Bugmaze.Level;
 
 import cz.sparko.Bugmaze.Activity.Game;
 import cz.sparko.Bugmaze.Block.*;
 import cz.sparko.Bugmaze.Helper.Coordinate;
 
 public class LevelTest extends Level {
     public LevelTest(Game game) {
         super(game);
     }
 
     @Override
     public Block createRandomBlock(Coordinate coordinate) {
        Class[] blocks = {Corner.class, LineOneWay.class, Line.class};
         float[] probabilities = {0.6f, 0.1f, 0.3f};
         int[] walkThroughs = {1, 1, 1};
         try {
             return Block.createRandomBlock(blocks, probabilities, walkThroughs, game, coordinate, true);
         } catch (Exception e) {
             e.printStackTrace();
         }
         return null;
     }
 
     @Override
     public Block[] getLevelBlocks() {
         return new Block[0];
     }
 
     @Override
     public float getSpeed() {
         return 1;
     }
 
     @Override
     public void cleanLevelForStart() {
 
     }
 }
