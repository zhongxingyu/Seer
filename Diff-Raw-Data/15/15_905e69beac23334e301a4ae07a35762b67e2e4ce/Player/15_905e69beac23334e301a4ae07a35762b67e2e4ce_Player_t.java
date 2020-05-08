 package fruit.g4;
 
 import java.util.*;
 
 public class Player extends fruit.sim.Player
 {
   private float[] prefs;
   private float[] platter;
   private float numFruits = 0;
   private float bowlsRemaining;
   private float totalNumBowls;
   private int numPlayers;
   private Stats scoreStats;
 
   private MLE mle;
 
 
   public void init(int nplayers, int[] pref) {
     numPlayers = nplayers;
     prefs = Vectors.castToFloatArray(pref);
     platter = new float[pref.length];
     bowlsRemaining = (float)(nplayers - getIndex());
     totalNumBowls = bowlsRemaining;
     scoreStats = new Stats();
     System.out.println(getIndex());
   }
 
   public boolean pass(int[] bowl, int bowlId, int round, boolean canPick, boolean mustTake) {
     // SETUP
     float[] currentBowl = Vectors.castToFloatArray(bowl);
     numFruits = Vectors.sum(currentBowl);
     if (!canPick){
       return false;
     }
 
     System.out.println("Number of bowls that will pass: " + totalNumBowls);
     System.out.println("Number of bowls remaining: " + bowlsRemaining);
 
     // Initialize the histogram now that we know how many fruit come in a bowl
     if (mle == null){
       mle = new MLE((int) numFruits, numPlayers);
     }
     mle.addObservation(currentBowl);
 
     // calculate score for the bowl the we get
     float score = score(currentBowl);
     scoreStats.addData(score);
 
     // get MLE and score it
     float[] uniformBowl = new float[currentBowl.length];
     for (int i = 0 ; i < bowl.length; i++){
       uniformBowl[i] = numFruits / bowl.length;
     }
     float uniformScore = score(uniformBowl);
 
     System.out.println("Uniform Score: " + uniformScore);
     System.out.println("MLE Score: " + score(mle.bowl(round == 0)));
     System.out.println("Score: " + score);
     bowlsRemaining--;
     float[] mleBowl = mle.bowl(round == 0);
     float[] mlePlatter = mle.platter();
     float maxScore = maxScore(mlePlatter);
     return shouldTakeBasedOnScore(score, score(mleBowl), maxScore);
   }
 
   private boolean shouldTakeBasedOnScore(float currentScore, float mle, float maxScore){
     // based on number of bowls remaining to pass you, decide if you should take
     if (currentScore < mle) return false;
 
     float diff = maxScore - mle;
     return currentScore > (0.3f * diff * (numPlayers / 9.0f * (totalNumBowls - 1) / bowlsRemaining)) + mle;
   }
 
   private float maxScore(float[] mlePlatter) {
     float fruitsTaken = 0;
     float maxScore = 0;
    float currentPref = prefs.length;
     while (fruitsTaken < numFruits && currentPref > 0) {
      int currentFruit = indexOf(prefs, currentPref);
       if (numFruits - fruitsTaken < mlePlatter[currentFruit]) {
         maxScore += (numFruits - fruitsTaken) * currentPref;
 	fruitsTaken = numFruits;
       } else {
 	maxScore += mlePlatter[currentFruit] * currentPref;
 	fruitsTaken += mlePlatter[currentFruit];
       }
       currentPref--;
     }
     return maxScore;
   }
 
  private int indexOf(float[] a, float x) {
    for (int i = 0; i < a.length; i++) {
      if (a[i] == x) {
        return i;
      }
    }
    return -1;
  }

   private float score(float[] bowl){
     return Vectors.dot(bowl, prefs);
   }
 
   private Random random = new Random();
 }
