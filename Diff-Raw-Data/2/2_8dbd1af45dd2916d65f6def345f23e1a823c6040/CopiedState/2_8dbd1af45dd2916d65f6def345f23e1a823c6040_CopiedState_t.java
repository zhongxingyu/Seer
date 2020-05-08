 
 import java.util.HashSet;
 import java.util.Set;
 
 public class CopiedState extends State {
 
     int simulatedHeight; // Height after which the game is considered to be lost.
 
     public double maximumAltitude = 0,
             altitudeDelta = 0,
             minimumAltitude = 0,
             filledSpotCount = 0,
             highestHole = 0,
             connectedHoleCount = 0,
             holeCount = 0,
             weightedHoleCount = 0,
             blocksAboveHighestHoleCount = 0,
             blockadeCount = 0,
             invertedWeightedHoleCount = 0,
             maxWellDepth = 0,
             TotalWellDepth = 0,
             wellCount = 0,
             surfaceAreaRoughness = 0,
             weightedLinesCleared ,
             maxContactArea;
 
     public double rowTransitions;
     public double columnTransitions;
 
     //total no. of heuristic: 16, please let us know if there is any one with
     //not updated, might have missed it
 
 
     CopiedState(State s) {
         this(s, ROWS);
     }
 
     CopiedState(State s, int perceivedHeight) {
         simulatedHeight = perceivedHeight;
 
         int[][] curField = getField();
         int[][] stateField = s.getField();
         for (int x = 0; x < curField.length; x++) {
             for (int y = 0; y < curField[x].length; y++) {
                 curField[x][y] = stateField[x][y];
             }
         }
 
         int[] curTop = getTop();
         for (int x = 0; x < curTop.length; x++) {
             curTop[x] = s.getTop()[x];
         }
 
         nextPiece = s.getNextPiece();
     }
 
     private void computeFeatureScores() {
         computeHoleFeatures();
         computeAltitudeFeatures();
         computeConnectedHoleFeatures();
         computeBlockades();
         computeWellFeatures();
         computeWellCount();
         computeSurfaceAreaRoughness();
     }
 
     @Override
     public boolean makeMove(int orient, int slot) {
         //This 2 heuristics depends on the prev state.
         EvaluationFunctions eval = new EvaluationFunctions(this, orient, slot);
         //this.maxContactArea = eval.maximumContactArea();
         //this.weightedLinesCleared = eval.weightedLinesClearScore();
 
         boolean result = super.makeMove(orient, slot);
         computeFeatureScores();
         if (maximumAltitude > simulatedHeight) {
             result = false; // lost
             lost = true;
         }
         return result;
     }
 
 
     //get hightest,weighted and hole count
     public void computeHoleFeatures() {
         int Holecount = 0;
         int Highesthole = 0;
         int weightedHole = 0;
         int BlocksAboveHighestHole = 0;
         int BlockCount = 0;
         int InvertedWeightedHoleCount = 0;
 
         for (int x = 0; x < getTop().length; x++) {
             boolean firsthole = true;
             int colblockcount = 0;
             for (int y = getTop()[x] - 2; y >= 0; y--) {
                 int cell = getField()[y][x];
 
                 if (cell == 0) { //is hole
                     Holecount++;
                     //y=height need +1
                    weightedHole += (y + 1);
                     InvertedWeightedHoleCount = InvertedWeightedHoleCount + (getTop()[x] - y - 1);
 
                     if (firsthole == true) {
                         Highesthole = Math.max(y + 1, Highesthole);
                         firsthole = false;
                         BlocksAboveHighestHole = getTop()[x] - Highesthole;
                     }
                 } else {
                     colblockcount++;
                 }
             }
             if (getTop()[x] >= 1) {
                 colblockcount++;
             }
             BlockCount += colblockcount;
         }
         highestHole = Highesthole;
         holeCount = Holecount;
         weightedHoleCount = weightedHole;
         blocksAboveHighestHoleCount = BlocksAboveHighestHole;
         filledSpotCount = BlockCount;
         invertedWeightedHoleCount = InvertedWeightedHoleCount;
     }
 
     // Get highest, lowest and altitude deltas
     private void computeAltitudeFeatures() {
         int highest = -1, lowest = ROWS + 1;
         for (int i : getTop()) {
             highest = Math.max(i, highest);
             lowest = Math.min(i, lowest);
         }
         maximumAltitude = highest;
         minimumAltitude = lowest;
         altitudeDelta = highest - lowest;
     }
 
     private void computeConnectedHoleFeatures() {
 
         Set<String> holes = new HashSet<String>();
         int count = 0;
         for (int x = 0; x < this.getField()[0].length; x++) {
             for (int y = getTop()[x] - 2; y >= 0; y--) {
                 int cell = this.getField()[y][x];
                 if (cell == 0) {//is empty  //is hole
                     String cur = x + ":" + y;
                     holes.add(cur);
                     String top = x + ":" + (y + 1);
                     String btm = x + ":" + (y - 1);
                     String left = (x - 1) + ":" + (y);
                     String right = (x + 1) + ":" + (y);
 
                     if (!holes.contains(top) && !holes.contains(btm) && !holes.contains(left) && !holes.contains(right)) {
                         count++;
                         int tempx = (x + 1);
                         while (tempx < getTop().length && this.getField()[y][tempx] == 0) {
                             int tempy = y;
                             while (getTop()[tempx] > tempy && this.getField()[tempy][tempx] == 0) {
                                 holes.add(tempx + ":" + tempy);
                                 tempy++;
                             }
                             tempx++;
                         }
                     }
                 }
             }
         }
         connectedHoleCount = count;
     }
 
     private void computeBlockades() {
         blockadeCount = 0;
         int[][] field = getField();
         for (int x = 0; x < COLS; x++) {
             boolean holeAlreadyFound = false;
             for (int y = getTop()[x] - 2; y >= 0; y--) {
                 if (field[y][x] == 0) { //is hole
                     if (holeAlreadyFound) {
                         blockadeCount++;
                     }
                     holeAlreadyFound = true;
                 }
             }
         }
     }
 
     //maximum depth of a well after executing a move
     private void computeWellFeatures() {
         int maxWellDep = 0;
         int totalWellDep = 0;
         int col = State.COLS - 1;
 
         for (int x = 0; x < State.COLS; x++) {
             //if check if the side of the column to see if it is enclosed and does not have
             if ((x == 0 || this.getTop()[x - 1] > this.getTop()[x]) && (x == col || this.getTop()[x + 1] > this.getTop()[x])) {
                 int wellDepth = 0;
                 if (x == 0)//first column
                 {
                     wellDepth = this.getTop()[x + 1] - this.getTop()[x];
                 } else if (x == col)//last column
                 {
                     wellDepth = this.getTop()[x - 1] - this.getTop()[x];
                 } else {
                     wellDepth = Math.min(this.getTop()[x - 1], this.getTop()[x + 1]) - this.getTop()[x];
                 }
                 maxWellDep = Math.max(wellDepth, maxWellDep);
                 totalWellDep += wellDepth;
             }
         }
 
         maxWellDepth = maxWellDep;
         this.TotalWellDepth = totalWellDep;
     }
 
     //no. of wells after executing a move
     private void computeWellCount() {
         int count = 0;
 
         for (int x = 0; x < State.COLS; x++) {
 
             int startCount = 0;
             int depth = 0;
             int wellSpotted = 0;
 
             for (int y = this.getTop().length - 1; y >= 0; y--) {
                 int cell = getField()[y][x];
                 if (cell != 0) { //if the top hole is not filled start to count
                     startCount = 1;
                 }
 
                 if (startCount == 1) {
                     depth++;
                 }
                 if (depth > 3) {
                     if (cell != 0) {
                         wellSpotted = 1;
                     }
                     if (wellSpotted == 1) {
                         if (cell == 0) {
                             wellSpotted = 0;
                             count++;
                         }
                     }
                 }
             }
         }
         wellCount = count;
     }
 
     //Surface area roughness after executing a move
     private void computeSurfaceAreaRoughness() {
         int roughness = 0;
         int maxTop = 0;
 
         for (int c = 0; c < this.getTop().length; c++)//by column
         {
             if (this.getTop()[c] > maxTop) {
                 maxTop = this.getTop()[c];
             }
         }
 
         for (int c = 0; c < this.getTop().length; c++)//by column
         {
             roughness += Math.abs(maxTop - this.getTop()[c]);
         }
         surfaceAreaRoughness = roughness;
         //return roughness;
     }
 }
