 package org.kelemenattila.rectlife;
 
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBufferInt;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.EnumMap;
 import java.util.EnumSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import java.util.concurrent.ForkJoinPool;
 import org.jtrim.utils.ExceptionHelper;
 import org.kelemenattila.rectlife.concurrent.ForkJoinUtils;
 import org.kelemenattila.rectlife.concurrent.IntRangeTask;
 
 /**
  *
  * @author Kelemen Attila
  */
 public final class EntityWorld {
     private static final double DEFAULT_MUTATE_RATE = 0.001;
     private static final int NEURON_COUNT = 10;
     private static final double DEFAULT_DEFENDER_CHANCE_MULTIPLIER = 0.5;
     private static final Random RND = new Random();
 
     private final ForkJoinPool algPool;
     private final int width;
     private final int height;
     private final Entity<EntityAction>[] board;
     private final BoardPos[][] fighters;
     private volatile DnsCombiner geneCombiner;
     private volatile double accidentRate;
    private volatile double defenderChangeMultiplier;
 
     public EntityWorld(ForkJoinPool algPool, int width, int height) {
         ExceptionHelper.checkNotNullArgument(algPool, "algPool");
         ExceptionHelper.checkArgumentInRange(width, 1, Integer.MAX_VALUE, "width");
         ExceptionHelper.checkArgumentInRange(height, 1, Integer.MAX_VALUE, "height");
 
         this.algPool = algPool;
         this.width = width;
         this.height = height;
         this.board = createBoard(width, height);
         this.fighters = new BoardPos[width * height][8];
         this.geneCombiner = new StandardGeneticCombiner(DEFAULT_MUTATE_RATE);
         this.accidentRate = 0.001;
        this.defenderChangeMultiplier = DEFAULT_DEFENDER_CHANCE_MULTIPLIER;
 
         fillBoard();
     }
 
    public void setDefenderChangeMultiplier(double defenderChangeMultiplier) {
        this.defenderChangeMultiplier = defenderChangeMultiplier;
     }
 
     public void setAccidentRate(double accidentRate) {
         this.accidentRate = accidentRate;
     }
 
     private void setEntity(int x, int y, Entity<EntityAction> entity) {
         board[y * width + x] = entity;
     }
 
     private Entity<EntityAction> getEntity(int x, int y) {
         int actualY = (y + height) % height;
         int actualX = (x + width) % width;
         return board[actualY * width + actualX];
     }
 
     private void fillBoard() {
         EntityAction[] actions = EntityAction.values();
 
         for (int y = 0; y < height; y++) {
             for (int x = 0; x < width; x++) {
                 setEntity(x, y, new Entity<>(8, NEURON_COUNT, actions));
             }
         }
     }
 
     @SuppressWarnings("unchecked")
     private static Entity<EntityAction>[] createBoard(int width, int height) {
         return (Entity<EntityAction>[])new Entity<?>[width * height];
     }
 
     public void setMutateRate(double newRate) {
         geneCombiner = new StandardGeneticCombiner(newRate);
     }
 
     private void getNeighbourAppearances(int x, int y, double baseAppearance, double[] neighbours) {
         int index = 0;
         for (int dx = -1; dx <= 1; dx++) {
             for (int dy = -1; dy <= 1; dy++) {
                 if (dx != 0 || dy != 0) {
                     Entity<EntityAction> neighbour = getEntity(x + dx, y + dy);
 
                     neighbours[index] = neighbour != null
                             ? neighbour.getAppearance() - baseAppearance
                             : 0.0;
                 }
             }
         }
     }
 
     private void putFighter(int fighterX, int fighterY, EntityAction.AttackPosition attackPos) {
         int destX = fighterX + attackPos.getDx();
         int destY = fighterY + attackPos.getDy();
         if (destX < 0 || destY < 0 || destX >= width || destY >= height) {
             return;
         }
 
         BoardPos fighterPos = new BoardPos(fighterX, fighterY);
         BoardPos[] posContainer = fighters[destY * width + destX];
         for (int i = 0; i < posContainer.length; i++) {
             if (posContainer[i] == null) {
                 posContainer[i] = fighterPos;
                 break;
             }
         }
     }
 
     private void chooseActions() {
         double[] neighbours = new double[8];
         for (int y = 0; y < height; y++) {
             for (int x = 0; x < width; x++) {
                 Entity<EntityAction> entity = getEntity(x, y);
                 if (entity != null) {
                     getNeighbourAppearances(x, y, entity.getAppearance(), neighbours);
                     EntityAction action = entity.think(neighbours);
                     EntityAction.AttackPosition attackPos = action.getAction();
                     if (attackPos != null) {
                         putFighter(x, y, attackPos);
                     }
                 }
             }
         }
     }
 
     private void resolveFight() {
        double currentDefChanceMul = defenderChangeMultiplier;
 
         for (int y = 0; y < height; y++) {
             for (int x = 0; x < width; x++) {
                 BoardPos[] positions = fighters[y * width + x];
                 int attackerCount = 0;
                 for (BoardPos pos: positions) {
                     if (pos == null) {
                         break;
                     }
                     attackerCount++;
                 }
 
                 if (attackerCount > 0) {
                     double count = attackerCount + 1;
                     double defenderChance = (currentDefChanceMul / count) * (count - 1.0);
                     if (Math.random() < defenderChance) {
                         setEntity(0, 0, null);
                     }
                     else {
                         BoardPos toKill = positions[RND.nextInt(attackerCount)];
                         setEntity(toKill.x, toKill.y, null);
                     }
                 }
             }
         }
         for (int i = 0; i < fighters.length; i++) {
             BoardPos[] positions = fighters[i];
             int count = 0;
             for (BoardPos pos: positions) {
                 if (pos == null) {
                     break;
                 }
                 count++;
             }
 
             if (count > 0) {
                 BoardPos toKill = positions[RND.nextInt(count)];
                 setEntity(toKill.x, toKill.y, null);
             }
         }
     }
 
     private void getNeighbours(int x, int y, List<BoardPos> neighbours) {
         for (int dx = -1; dx <= 1; dx++) {
             for (int dy = -1; dy <= 1; dy++) {
                 if (dx != 0 || dy != 0) {
                     int entityX = x + dx;
                     int entityY = y + dy;
                     Entity<EntityAction> entity = getEntity(entityX, entityY);
                     if (entity != null && entity.getAge() > 0) {
                         neighbours.add(new BoardPos(entityX, entityY));
                     }
                 }
             }
         }
     }
 
     private boolean breedPopulationSingleStep() {
         List<BoardPos> neighbours = new ArrayList<>();
         List<BoardPos> otherNeighbours = new ArrayList<>();
 
         DnsCombiner currentCombiner = geneCombiner;
         boolean didSomething = false;
         for (int y = 0; y < height; y++) {
             for (int x = 0; x < width; x++) {
                 Entity<EntityAction> entity = getEntity(x, y);
                 if (entity != null) {
                     continue;
                 }
 
                 neighbours.clear();
                 getNeighbours(x, y, neighbours);
 
                 while (!neighbours.isEmpty()) {
                     int choseIndex1 = RND.nextInt(neighbours.size());
                     BoardPos entity1Pos = neighbours.get(choseIndex1);
 
                     otherNeighbours.clear();
                     getNeighbours(entity1Pos.x, entity1Pos.y, otherNeighbours);
 
                     if (!otherNeighbours.isEmpty()) {
                         didSomething = true;
                         int choseIndex2 = RND.nextInt(otherNeighbours.size());
                         BoardPos entity2Pos = otherNeighbours.get(choseIndex2);
 
                         Entity<EntityAction> entity1 = getEntity(entity1Pos.x, entity1Pos.y);
                         Entity<EntityAction> entity2 = getEntity(entity2Pos.x, entity2Pos.y);
                         Entity<EntityAction> newEntity = entity1.breed(entity2, currentCombiner);
                         setEntity(x, y, newEntity);
                         break;
                     }
                     else {
                         neighbours.remove(choseIndex1);
                     }
                 }
             }
         }
         return didSomething;
     }
 
     private void breedPopulation() {
         while (breedPopulationSingleStep()) {
             // Do nothing but loop until there is nothing to change.
         }
     }
 
     private void resolveAccidents() {
         double currentAccidentRate = accidentRate;
         for (int i = 0; i < board.length; i++) {
             if (board[i] != null && Math.random() < currentAccidentRate) {
                 board[i] = null;
             }
         }
     }
 
     public void stepWorld() {
         for (int i = 0; i < fighters.length; i++) {
             BoardPos[] positions = fighters[i];
             for (int j = 0; j < positions.length; j++) {
                 positions[j] = null;
             }
         }
 
         chooseActions();
         resolveFight();
         resolveAccidents();
         breedPopulation();
     }
 
     public WorldView createAppearanceView() {
         // Use TYPE_INT_ARGB instead of TYPE_BYTE_GRAY because Java sometimes
         // tries to be too clever when displaying grayscale images not using
         // the comlete gray scale ([0, 255] in this case).
         BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
         DataBufferInt dataBuffer = (DataBufferInt)image.getRaster().getDataBuffer();
         @SuppressWarnings("MismatchedReadAndWriteOfArray")
         int[] pixels = dataBuffer.getData();
 
         for (int y = 0; y < height; y++) {
             for (int x = 0; x < width; x++) {
                 Entity<EntityAction> entity = getEntity(x, y);
                 int color;
 
                 if (entity != null) {
                     int grayLevel = (int)(entity.getAppearance(0.0, 1.0) * 256.0);
                     grayLevel = Math.max(0, Math.min(grayLevel, 0xFF));
                     color = grayLevel | (grayLevel << 8) | (grayLevel << 16) | 0xFF00_0000;
                 }
                 else {
                     color = 0xFFFF0000; // red
                 }
                 pixels[width * y + x] = color;
             }
         }
 
         return new WorldView("Appearance", image);
     }
 
     public WorldView createAgeView() {
         // Use TYPE_INT_ARGB instead of TYPE_BYTE_GRAY because Java sometimes
         // tries to be too clever when displaying grayscale images not using
         // the comlete gray scale ([0, 255] in this case).
         BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
         DataBufferInt dataBuffer = (DataBufferInt)image.getRaster().getDataBuffer();
         @SuppressWarnings("MismatchedReadAndWriteOfArray")
         int[] pixels = dataBuffer.getData();
 
         long minAge = Long.MAX_VALUE;
         long maxAge = Long.MIN_VALUE;
         double avgAge = 0.0;
         int count = 0;
         for (Entity<?> entity: board) {
             if (entity != null) {
                 long age = entity.getAge();
                 if (age < minAge) minAge = age;
                 if (age > maxAge) maxAge = age;
 
                 count++;
                 double w0 = (double)(count - 1) / (double)count;
                 double w1 = 1.0 / (double)count;
                 avgAge = w0 * avgAge + w1 * (double)age;
             }
         }
 
         double ageRadius = Math.min(maxAge - avgAge, avgAge - minAge);
         double lowAge = avgAge - ageRadius;
         double highAge = avgAge + ageRadius;
         double ageScale = 255.0 / (highAge - lowAge);
 
         for (int y = 0; y < height; y++) {
             for (int x = 0; x < width; x++) {
                 Entity<EntityAction> entity = getEntity(x, y);
                 int color;
 
                 if (entity != null) {
                     double age = entity.getAge();
                     double unboundedGrayLevel = ageScale * (age - lowAge);
                     int grayLevel = (int)Math.round(Math.max(0.0, Math.min(unboundedGrayLevel, 255.0)));
                     color = grayLevel | (grayLevel << 8) | (grayLevel << 16) | 0xFF00_0000;
                 }
                 else {
                     color = 0xFFFF0000; // red
                 }
                 pixels[width * y + x] = color;
             }
         }
 
         return new WorldView("Age", image);
     }
 
     public WorldView[] viewWorld() {
         return new WorldView[]{
             createAppearanceView(),
             createAgeView()
         };
     }
 
     private static void updateRunAvg(int index, double[] avgs, int[] counts, double toAdd) {
         int prevCount = counts[index];
         int nextCount = prevCount + 1;
         counts[index] = nextCount;
 
         double prevAvg = avgs[index];
 
         double w0 = (double)prevCount / (double)nextCount;
         double w1 = 1.0 / (double)nextCount;
         avgs[index] = w0 * prevAvg + w1 * toAdd;
     }
 
     public void getGraphs(final double[] racismGraph, final double[] doNothingGraph) {
         if (racismGraph.length != doNothingGraph.length) {
             throw new IllegalArgumentException("Arguments must have the same length.");
         }
 
         Arrays.fill(racismGraph, 0.0);
         Arrays.fill(doNothingGraph, 0.0);
 
         final Map<EntityAction, Integer> neighbourIndex = new EnumMap<>(EntityAction.class);
         neighbourIndex.put(EntityAction.TOP_LEFT, 0);
         neighbourIndex.put(EntityAction.LEFT, 1);
         neighbourIndex.put(EntityAction.BOTTOM_LEFT, 2);
         neighbourIndex.put(EntityAction.TOP, 3);
         neighbourIndex.put(EntityAction.BOTTOM, 4);
         neighbourIndex.put(EntityAction.TOP_RIGHT, 5);
         neighbourIndex.put(EntityAction.RIGHT, 6);
         neighbourIndex.put(EntityAction.BOTTOM_RIGHT, 7);
 
         final double lowRelAppearance = Entity.MIN_APPEARANCE - Entity.MAX_APPEARANCE;
         final double highRelAppearance = Entity.MAX_APPEARANCE - Entity.MIN_APPEARANCE;
 
         final double testValueMultiplier =
                 (highRelAppearance - lowRelAppearance) / ((double)racismGraph.length - 1.0);
 
         final int[] racismCounts = new int[racismGraph.length];
         final int[] doNothingGraphCounts = new int[doNothingGraph.length];
         // racismCounts, doNothingGraphCounts are initialized with zeros
 
         Set<EntityAction> testedActionsSet = EnumSet.allOf(EntityAction.class);
         testedActionsSet.retainAll(neighbourIndex.keySet());
         final EntityAction[] testedActions
                 = testedActionsSet.toArray(new EntityAction[testedActionsSet.size()]);
 
         ForkJoinUtils.forAll(algPool, 0, racismGraph.length, 1, new IntRangeTask() {
             @Override
             public void doWork(int startInclusive, int endExclusive) {
                 final double[] neighbours = new double[8];
 
                 for (int outputIndex = startInclusive; outputIndex < endExclusive; outputIndex++) {
                     double testedValue = testValueMultiplier * outputIndex + lowRelAppearance;
 
                     // gcd(boardStep, board.length) == 1, so the loop below
                     // loops over all elements.
                     //
                     // We use this tricky loop, so that contention on the
                     // synchronized block below will be unlikely.
                     int boardStep = board.length < Integer.MAX_VALUE
                             ? board.length + 1
                             : board.length - 1;
                     int boardIndex = RND.nextInt(board.length);
                     for (int loopCount = 0; loopCount < board.length; loopCount++) {
                         boardIndex = (boardIndex + boardStep) % board.length;
                         Entity<EntityAction> entity = board[boardIndex];
                         if (entity != null) {
                             double appearance = entity.getAppearance();
                             double minAllowed = Entity.MIN_APPEARANCE - appearance;
                             double maxAllowed = Entity.MAX_APPEARANCE - appearance;
                             if (testedValue < minAllowed || testedValue > maxAllowed) {
                                 continue;
                             }
 
                             for (EntityAction testedAction: testedActions) {
                                 int testedIndex = neighbourIndex.get(testedAction);
 
                                 Arrays.fill(neighbours, 0.0);
                                 neighbours[testedIndex] = testedValue;
 
                                 EntityAction action;
                                 synchronized (entity) {
                                     action = entity.thinkWithoutAging(neighbours);
                                 }
                                 EntityAction.AttackPosition attack = action.getAction();
 
                                 double racist;
                                 double idle;
                                 if (attack != null) {
                                     idle = 0.0;
                                     Integer actionValue = neighbourIndex.get(action);
                                     if (actionValue != null && actionValue.intValue() == testedIndex) {
                                         racist = 1.0;
                                     }
                                     else {
                                         racist = 0.0;
                                     }
                                 }
                                 else {
                                     racist = 0.0;
                                     idle = 1.0;
                                 }
 
                                 updateRunAvg(outputIndex, racismGraph, racismCounts, racist);
                                 updateRunAvg(outputIndex, doNothingGraph, doNothingGraphCounts, idle);
                             }
                         }
                     }
                 }
             }
         });
 
         for (int i = 0; i < racismGraph.length; i++) {
             if (racismCounts[i] == 0) racismGraph[i] = Double.NaN;
             if (doNothingGraphCounts[i] == 0) doNothingGraph[i] = Double.NaN;
         }
     }
 
     public static final class WorldView {
         private final String caption;
         private final BufferedImage image;
 
         public WorldView(String caption, BufferedImage image) {
             ExceptionHelper.checkNotNullArgument(caption, "caption");
             ExceptionHelper.checkNotNullArgument(image, "image");
 
             this.caption = caption;
             this.image = image;
         }
 
         public String getCaption() {
             return caption;
         }
 
         public BufferedImage getImage() {
             return image;
         }
     }
 
     private static final class BoardPos {
         public final int x;
         public final int y;
 
         public BoardPos(int x, int y) {
             this.x = x;
             this.y = y;
         }
     }
 }
