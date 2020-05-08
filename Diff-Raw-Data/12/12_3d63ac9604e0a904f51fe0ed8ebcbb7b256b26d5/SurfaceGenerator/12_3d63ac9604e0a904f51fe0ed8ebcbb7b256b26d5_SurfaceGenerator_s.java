 package kniemkiewicz.jqblocks.ingame.level;
 
 import kniemkiewicz.jqblocks.Configuration;
 import kniemkiewicz.jqblocks.ingame.Sizes;
 import kniemkiewicz.jqblocks.ingame.SolidBlocks;
 import kniemkiewicz.jqblocks.ingame.object.DirtBlock;
 import kniemkiewicz.jqblocks.util.Assert;
 import kniemkiewicz.jqblocks.util.TimeLog;
 import org.newdawn.slick.geom.Rectangle;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 /**
  * User: krzysiek
  * Date: 15.07.12
  */
 @Component
 public class SurfaceGenerator {
 
   @Autowired
   private Configuration configuration;
 
   @Autowired
   private ObjectGenerator objectGenerator;
 
   @Autowired
   private SolidBlocks blocks;
 
   Random random;
 
   int[] heights = new int[(Sizes.MAX_X - Sizes.MIN_X) / Sizes.BLOCK];
 
   void generate(Random random) {
     this.random = random;
     TimeLog t = new TimeLog();
     generateFlat();
     t.logTimeAndRestart("generate flat");
     generateHills();
     t.logTimeAndRestart("generate hills");
     runSlidingWindow(heights);
     t.logTimeAndRestart("sliding window");
     prepareBlocks(blocks);
     t.logTimeAndRestart("prepare blocks");
     objectGenerator.generateTrees(random, heights);
     t.logTimeAndRestart("generate trees");
   }
 
   // This method translates heights[x] into actual blocks, trying to use as few blocks as possible and making sure
   // that we use mostly horizontal blocks.
   private void prepareBlocks(SolidBlocks blocks) {
     //TODO: Use something else than Rectangle, it is too expensive here.
     //TODO: Consider using Stack
     List<Rectangle> proposals = new ArrayList<Rectangle>();
     int h = heights[0];
     // width is zero as it is unknown yet.
     proposals.add(new Rectangle(Sizes.MIN_X, Sizes.MAX_Y - heights[0], 0, heights[0]));
     for (int i = 1; i < heights.length; i++) {
       if (heights[i] > h) {
         // we have to add some more blocks, above the existing proposals.
         proposals.add(new Rectangle(Sizes.MIN_X + i * Sizes.BLOCK, Sizes.MAX_Y - heights[i], 0, heights[i] - h));
       }
       if (heights[i] < h) {
         // Some blocks may end completely.
         int new_y = Sizes.MAX_Y - heights[i];
         while(new_y >= proposals.get(proposals.size() - 1).getMaxY()) {
           Rectangle r = proposals.get(proposals.size() - 1);
           proposals.remove(proposals.size() - 1);
           float width = Sizes.MIN_X + i * Sizes.BLOCK - r.getMinX();
           Assert.executeAndAssert(blocks.add(new DirtBlock(r.getMinX(), r.getMinY(), width, r.getHeight())));
         }
         // We should never reach bottom of the level so there is always at least the last block that we can cut into
         // smaller one if new height is lowest ever seen.
         if (new_y > proposals.get(proposals.size() - 1).getMinY()) {
           Rectangle r = proposals.get(proposals.size() - 1);
           int diff = (int)(new_y - r.getMinY());
           float width = Sizes.MIN_X + i * Sizes.BLOCK - r.getMinX();
           Assert.executeAndAssert(blocks.add(new DirtBlock(r.getMinX(), r.getMinY(), width, diff)));
           r.setY(r.getY() + diff);
           r.setHeight(r.getHeight() - diff);
         }
       }
       h = heights[i];
     }
     for (Rectangle r : proposals) {
       Assert.executeAndAssert(blocks.add(new DirtBlock(r.getMinX(), r.getMinY(), Sizes.MAX_X - r.getMinX(), r.getHeight())));
     }
   }
 
   private void generateFlat() {
    int y = 4 * (Sizes.MAX_Y - Sizes.MIN_Y) / 5;
     int i = 0;
     while (i < heights.length) {
       int dx = random.nextInt(10);
       int dy = random.nextInt(3) - 1;
       y += dy * Sizes.BLOCK;
       for (int x = i; x < i + dx && x < heights.length; x++) {
         heights[x] = y;
       }
       i += dx;
     }
   }
 
   private void generateHills() {
     float HILL_DENSITY = configuration.getFloat("SurfaceGenerator.HILL_DENSITY", 0.5f);
     int MAX_HILL_SIZE = configuration.getInt("SurfaceGenerator.MAX_HILL_SIZE", 60);
     int MIN_HILL_SIZE = configuration.getInt("SurfaceGenerator.MIN_HILL_SIZE", 20);
     int MIN_HILL_HEIGHT = configuration.getInt("SurfaceGenerator.MIN_HILL_HEIGHT", 4);
     int MAX_HILL_HEIGHT = configuration.getInt("SurfaceGenerator.MAX_HILL_HEIGHT", 16);
     int HILL_RND_HEIGHT = configuration.getInt("SurfaceGenerator.HILL_RND_HEIGHT", 3);
     int numberOfHills = (int)(2 * (Sizes.MAX_X - Sizes.MIN_X) / (MIN_HILL_SIZE + MAX_HILL_SIZE) * HILL_DENSITY);
     for (int i = 0; i < numberOfHills; i++) {
       int width = random.nextInt(MAX_HILL_SIZE - MIN_HILL_SIZE) + MIN_HILL_SIZE;
       int x = random.nextInt(heights.length - width - 1);
       int height = random.nextInt(MAX_HILL_HEIGHT - MIN_HILL_HEIGHT) + MIN_HILL_HEIGHT;
       assert width > 4;
       assert x + width < heights.length;
       int topPosition = random.nextInt(width - 4) + 2;
       for (int j = 0; j < topPosition; j++) {
         int rnd = HILL_RND_HEIGHT > 0 ? random.nextInt(HILL_RND_HEIGHT * 2) - HILL_RND_HEIGHT : 0;
         heights[x + j] += Sizes.BLOCK * (height * (j + 1) / topPosition + rnd);
       }
       int remainingWidth = width - topPosition;
       for (int j = topPosition; j < width; j++) {
         int rnd = HILL_RND_HEIGHT > 0 ? random.nextInt(HILL_RND_HEIGHT * 2) - HILL_RND_HEIGHT : 0;
         heights[x + j] += Sizes.BLOCK * ((height * (width - j) / remainingWidth + rnd));
       }
     }
   }
 
   void runSlidingWindow(int[] heightsArg) {
     int SLIDING_WINDOW_SIZE = configuration.getInt("SurfaceGenerator.SLIDING_WINDOW_SIZE", 1);
     int partialSums[] = new int[heightsArg.length];
     int sum = 0;
     for (int i = 0; i < heightsArg.length; i++) {
       sum += heightsArg[i];
       partialSums[i] = sum;
     }
     int length = heightsArg.length;
     for (int i = 0; i < SLIDING_WINDOW_SIZE; i++) {
       heightsArg[i] = Sizes.roundToBlockSize(partialSums[i + SLIDING_WINDOW_SIZE] / (i + 1 + SLIDING_WINDOW_SIZE));
       heightsArg[length - i - 1] = Sizes.roundToBlockSize((partialSums[length - 1] - partialSums[length - 2 - SLIDING_WINDOW_SIZE - i]) / (i + 1 + SLIDING_WINDOW_SIZE));
     }
     for (int i = SLIDING_WINDOW_SIZE; i < length - 1 - SLIDING_WINDOW_SIZE; i++) {
       heightsArg[i] = Sizes.roundToBlockSize((partialSums[i + SLIDING_WINDOW_SIZE] - partialSums[i - SLIDING_WINDOW_SIZE]) / (2 * SLIDING_WINDOW_SIZE));
     }
   }
 }
