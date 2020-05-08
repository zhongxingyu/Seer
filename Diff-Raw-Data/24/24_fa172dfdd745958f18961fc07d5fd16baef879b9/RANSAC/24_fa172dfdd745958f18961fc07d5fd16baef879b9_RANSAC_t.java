 package rgbdslam;
 
 import java.util.*;
 import april.jmat.*;
 import april.jmat.geom.*;
 import rgbdslam.DescriptorMatcher.Match;
 
 public class RANSAC {
 
     final static double MIN_DIST = 5;  // In meters, min distance for two features to be compared
     final static int DOF = 3;           // Degrees of freedom
     final static int NUM_ITER = 1000;
     final static double MAX_SQ_CONSENSUS_DISTANCE = 0.0001;
     final static double MIN_BREAK_EARLY_INLIERS_PERCENTAGE = 0.8;
     private static Random rand = new Random(83247983);
 
     /**
      * Computes the best RBT between the ImageFeatures in the list matches, trying to identify
      * and exclude outliers. The RBT returned is the one that fits all inliers best.
      * @param matches the ImageFeature matches that are to be aligned
      * @param inliers this list, which must not be null, will be filled with the inliers
      * @return the RBT that best aligns all of the inlier matches
      */
     public static double[][] RANSAC(List<Match> matches, List<Match> inliers) {
         // RANSAC works by choosing the minimum number of points needed to fit a model, and then
         // computing the model that generates the highest consensus among all points.
         // In our case, the model needs DOF points.
 
         List<Match> bestInliers = new ArrayList<Match>();
         double bestConsensus = 0;
 
         // The loop stops either at NUM_ITER iterations, or when MIN_BREAK_EARLY_INLIERS_PERCENTAGE of
         // the whole data set is considered to be an inlier.
         for (int iter = 0; iter < NUM_ITER; iter++) {
 
             // gets DOF indices out of the whole list of matches
             int[] idx = getRandomIndices(matches.size());
             List<Match> currentInliers = new ArrayList<Match>(matches.size());
 
             // Find corresponding locations and 3D transform for the points forming the model
             ArrayList<double[]> cora = new ArrayList<double[]>();
             ArrayList<double[]> corb = new ArrayList<double[]>();
 
             for (int i = 0; i < DOF; i++) {
                 Match match = matches.get(idx[i]);
                 cora.add(match.feature1.xyz());
                 corb.add(match.feature2.xyz());
             }
 
             // compute the RBT from feature1 to feature2
             double[][] transform = AlignPoints3D.align(cora, corb);
 
             // we now compute the score for this model. one match votes "yes" if the current RBT makes its
             // feature on the first frame align with the one on the second frame.
             int score = 0;
             for (Match match : matches) {
                 double[] predictedFeature2 = LinAlg.transform(transform, match.feature1.xyz());
                 double sqdDistance = LinAlg.squaredDistance(predictedFeature2, match.feature2.xyz());
                 if (sqdDistance  < MAX_SQ_CONSENSUS_DISTANCE) {
                     //System.out.println("D = " + sqdDistance);
                     score++;
                    match.xyzDistance = Math.sqrt(sqdDistance);
                     currentInliers.add(match);
                 }
             }
 
             if (score > bestConsensus) {
                 bestConsensus = score;
                 bestInliers = currentInliers;
             }
             if (inliers.size() >= MIN_BREAK_EARLY_INLIERS_PERCENTAGE * matches.size()) {
                 break;
             }
         }
 
         inliers.clear();
         inliers.addAll(bestInliers);
         return getAlignment(inliers);
     }
 
     /**
      * Computes the best alignment between a list of matches (used to compute the final RBT between all
      * inliers).
      * @param matches
      * @return
      */
     private static double[][] getAlignment(List<Match> matches) {
         ArrayList<double[]> cora = new ArrayList<double[]>();
         ArrayList<double[]> corb = new ArrayList<double[]>();
 
         for (Match match : matches) {
             cora.add(match.feature1.xyz());
             corb.add(match.feature2.xyz());
         }
 
         if (cora.size() < 1 || corb.size() < 1)
             return null;
 
         return AlignPoints3D.align(cora, corb);
     }
 
     /**
      * Returns a list of DOF indices between 0 and n.
      * @param n
      * @return
      */
     protected static int[] getRandomIndices(int n) {
         // Get DOF number of different features
         int[] idx = new int[DOF];
         for (int i = 0; i < DOF; i++) {
             boolean copy;
             do {
                 idx[i] = rand.nextInt(n);
 
                 copy = false;
                 for (int j = 0; j < i; j++) {
                     if (idx[i] == idx[j]) {
                         copy = true;
                     }
                 }
             } while (copy == true);
         }
         return idx;
     }
 
 }
