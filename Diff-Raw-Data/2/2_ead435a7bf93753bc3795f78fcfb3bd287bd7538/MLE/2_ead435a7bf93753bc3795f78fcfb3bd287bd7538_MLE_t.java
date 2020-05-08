 package fruit.g4;
 import java.util.*;
 
 class MLE {
   private int[][] occuranceHist;
   private int numFruitsPerBowl;
   private int NUM_FRUIT_TYPES = 12;
   private int numPlayers;
   private Random random;
   private float[] prefs;
   private Stats bowlScoreStats;
 
   public MLE(int numFruitsPer, int nplayers, float[] preferences){
     numPlayers = nplayers;
     numFruitsPerBowl = numFruitsPer;
     occuranceHist = new int[NUM_FRUIT_TYPES][numFruitsPerBowl + 1];
     random = new Random();
     prefs = preferences;
   }
 
   public void addObservation(float[] bowl){
     for (int i = 0; i < NUM_FRUIT_TYPES; i++){
       occuranceHist[i][(int) bowl[i]] += 1;
     }
   }
 
   // Discrete gaussian roughly sigma = 1
   private float gaussian(int sample, int mean){
     int diff = Math.abs(mean - sample);
 
     // .46 + 2(.22) + 2(.04) + 2(0.01) = 1.0
     if (diff == 0) return 1.0f;
     //else if (diff == 1) return 0.22f;
     //else if (diff == 2) return 0.04f;
     //else if (diff == 3) return 0.01f;
     return 0f;
   }
 
   public float[] distribution(){
     float[] bowl = new float[NUM_FRUIT_TYPES];
     for (int i = 0; i < NUM_FRUIT_TYPES; i++){ // each fruit
       bowl[i] = fruitOccuranceMLE(i);
     }
     return Vectors.normalize(bowl);
   }
 
   public float fruitOccuranceMLE(int fruit) {
     float[] gaussianArr = new float[numFruitsPerBowl + 1];
     for (int i = 0; i < occuranceHist[0].length; i++){
       for (int j = 0; j < occuranceHist[0].length; j++){ //TODO: just loop across 3 on each side
         gaussianArr[i] += gaussian(i, j) * occuranceHist[fruit][j];
       }
     }
     return Vectors.maxIndex(gaussianArr);
   }
 
   // Get MLE for a platter by inferring from bowls that youve seen
   public float[] platter(){
     return Vectors.scale(
       distribution(),
       (float) (numPlayers * numFruitsPerBowl)
     );
   }
 
   private float numOccurances(int fruit){
     float num = 0f;
     for (int i = 0; i < occuranceHist[fruit].length; i++){
       num += occuranceHist[fruit][i] * i;
     }
     return num;
   }
 
   public float[] bowl(boolean firstRound){
     float[] averageBowl = new float[NUM_FRUIT_TYPES];
     float[] platter = platter();
     System.out.println(Arrays.toString(platter));
    bowlScoreStats = new Stats();
     for (int i = 0; i < 1000; i++) {
       float[] tempPlatter = platter.clone();
       float[] tempBowl = simulateBowl(tempPlatter);
       for (int j = 0; j < NUM_FRUIT_TYPES; j++) {
         averageBowl[j] += tempBowl[j];
       }
       bowlScoreStats.addData(Vectors.dot(tempBowl, prefs));
     }
     for (int i = 0; i < NUM_FRUIT_TYPES; i++) {
       averageBowl[i] = averageBowl[i] / 1000;
     }
     return averageBowl;
   }
 
   // simulates the picking of a bowl taking into account clustering and returns the score of the bowl
   private float[] simulateBowl(float[] currentFruits) {
     float[] bowl = new float[NUM_FRUIT_TYPES];
     int sz = 0;
     while (sz < numFruitsPerBowl) {
       // pick a fruit according to current fruit distribution
       int fruit = pickFruit(currentFruits); 
       float c = 1 + random.nextInt(3);
       c = Math.min(c, numFruitsPerBowl - sz);
       c = Math.min(c, currentFruits[fruit]);
 
       bowl[fruit] += c;
       sz += c;
       currentFruits[fruit] -= c;
     }
     return bowl;
   }
 
   private int pickFruit(float[] currentFruits) {
     // generate a prefix sum
     float[] prefixsum = new float[NUM_FRUIT_TYPES];
     prefixsum[0] = currentFruits[0];
     for (int i = 1; i != NUM_FRUIT_TYPES; ++i) {
       prefixsum[i] = prefixsum[i-1] + currentFruits[i];
     }
 
     float currentFruitCount = prefixsum[NUM_FRUIT_TYPES - 1];
     // roll a dice [0, currentFruitCount)
     int rnd = random.nextInt((int) currentFruitCount);
         
     for (int i = 0; i != NUM_FRUIT_TYPES; ++i)
       if (rnd < prefixsum[i])
         return i;
 
     assert false;
 
     return -1;
   }
 
   public int mostCanSee(int fruit){
     return Math.min(
       (int) Math.max(Math.ceil(platter()[fruit]), 0),
       (int) numFruitsPerBowl
     );
   }
 
   public Stats getBowlGenerationStats() {
     return bowlScoreStats;
   }
 }
