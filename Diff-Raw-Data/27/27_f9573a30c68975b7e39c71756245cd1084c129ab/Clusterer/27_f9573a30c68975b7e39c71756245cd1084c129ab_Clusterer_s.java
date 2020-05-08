 package pl.poznan.put.cs.bioserver.clustering;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Random;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A utility class to allow clustering of data (either hierarchical or
  * partitional).
  * 
  * @author tzok
  */
 public final class Clusterer {
     protected class Result {
         private Set<Integer> medoids;
         private double[][] matrix;
         private double score;
 
         public Result(double score, Set<Integer> medoids, double[][] matrix) {
             this.score = score;
             this.medoids = medoids;
             this.matrix = matrix.clone();
         }
 
         public int[] clusterAssignment() {
             Map<Integer, Set<Integer>> clustering = Clusterer.getClustering(
                     medoids, matrix);
             int[] result = new int[matrix.length];
 
             int i = 0;
             for (Entry<Integer, Set<Integer>> entry : clustering.entrySet()) {
                 for (int j : entry.getValue()) {
                     result[j] = i;
                 }
                 i++;
             }
             return result;
         }
     }
 
     private interface ScoringFunction {
         double score(Map<Integer, Set<Integer>> clustering, double[][] matrix);
     }
 
     /** Available hierarchical clustering types. */
     enum Type {
         SINGLE, COMPLETE, AVERAGE;
     }
 
     private final static Logger LOGGER = LoggerFactory
             .getLogger(Clusterer.class);
 
     private static ScoringFunction scoringPAMSIL = new ScoringFunction() {
         @Override
         public double score(Map<Integer, Set<Integer>> clustering,
                 double[][] matrix) {
             return Clusterer.scoreBySilhouette(clustering, matrix);
         }
     };
 
     private static ScoringFunction scoringPAM = new ScoringFunction() {
         @Override
         public double score(Map<Integer, Set<Integer>> clustering,
                 double[][] matrix) {
             return Clusterer.scoreByDistance(clustering, matrix);
         }
     };
 
     /** Information about the results of clustering. */
     private static List<List<Integer>> clusters;
     private static Clusterer INSTANCE = new Clusterer();
 
     private static final Random RANDOM = new Random();
 
     static Result clusterPAM(double[][] matrix) {
         return Clusterer.kMedoids(matrix, Clusterer.scoringPAM);
     }
 
     static Result clusterPAM(double[][] matrix, int k) {
         return Clusterer.kMedoids(matrix, Clusterer.scoringPAM, k);
     }
 
     public static Result clusterPAMSIL(double[][] matrix) {
         return Clusterer.kMedoids(matrix, Clusterer.scoringPAMSIL);
     }
 
     static Result clusterPAMSIL(double[][] matrix, int k) {
         return Clusterer.kMedoids(matrix, Clusterer.scoringPAMSIL, k);
     }
 
     private static Map<Integer, Set<Integer>> getClustering(
             Set<Integer> medoids, double[][] matrix) {
         Map<Integer, Set<Integer>> clustering = new HashMap<>();
         for (int i : medoids) {
             clustering.put(i, new HashSet<Integer>());
         }
         for (int i = 0; i < matrix.length; i++) {
             double minimum = Double.POSITIVE_INFINITY;
             int minimizer = -1;
             for (int j : medoids) {
                 if (matrix[i][j] < minimum) {
                     minimum = matrix[i][j];
                     minimizer = j;
                 }
             }
             Set<Integer> set = clustering.get(minimizer);
             set.add(i);
         }
         return clustering;
     }
 
     static List<List<Integer>> getClusters() {
         return Clusterer.clusters;
     }
 
     /**
      * Perform agglomerative, hierarchical clustering using SINGLE, COMPLETE or
      * AVERAGE linkage.
      * 
      * @param matrix
      *            Distance matrix.
      * @param linkage
      *            SINGLE, COMPLETE or AVERAGE.
      * @return An array of triplets in form (A, B, d(A, B)), where A and B are
      *         cluster IDs and d(A, B) is a scaled distance between them..
      */
     static int[][] hierarchicalClustering(double[][] matrix,
             Clusterer.Type linkage) {
         /*
          * initialise clusters as single elements
          */
         Clusterer.clusters = new ArrayList<>();
         for (int i = 0; i < matrix.length; ++i) {
             List<Integer> c = new ArrayList<>();
             c.add(i);
             Clusterer.clusters.add(c);
         }
 
         List<int[]> result = new ArrayList<>();
         while (Clusterer.clusters.size() > 1) {
             /*
              * get two clusters to be merged
              */
             int[] toMerge = new int[2];
             double leastDiff = Double.POSITIVE_INFINITY;
             for (int i = 0; i < Clusterer.clusters.size(); ++i) {
                 for (int j = i + 1; j < Clusterer.clusters.size(); ++j) {
                     List<Integer> c1 = Clusterer.clusters.get(i);
                     List<Integer> c2 = Clusterer.clusters.get(j);
                     double delta = 0;
 
                     switch (linkage) {
                     case SINGLE:
                         delta = Double.POSITIVE_INFINITY;
                         for (int m : c1) {
                             for (int n : c2) {
                                 if (matrix[m][n] < delta) {
                                     delta = matrix[m][n];
                                 }
                             }
                         }
                         break;
                     case COMPLETE:
                         delta = Double.NEGATIVE_INFINITY;
                         for (int m : c1) {
                             for (int n : c2) {
                                 if (matrix[m][n] > delta) {
                                     delta = matrix[m][n];
                                 }
                             }
                         }
                         break;
                     case AVERAGE:
                         int count = 0;
                         for (int m : c1) {
                             for (int n : c2) {
                                 delta += matrix[m][n];
                                 count++;
                             }
                         }
                         delta /= count;
                         break;
                     default:
                         // TODO: fill the default case
                         break;
                     }
 
                     if (delta < leastDiff) {
                         toMerge[0] = i;
                         toMerge[1] = j;
                         leastDiff = delta;
                     }
                 }
             }
 
             /*
              * merge clusters
              */
             List<Integer> c1 = Clusterer.clusters.get(toMerge[0]);
             List<Integer> c2 = Clusterer.clusters.get(toMerge[1]);
             c1.addAll(c2);
             Clusterer.clusters.remove(toMerge[1]);
 
             result.add(new int[] { toMerge[0], toMerge[1],
                     (int) (1000.0 * leastDiff) });
         }
         return result.toArray(new int[result.size()][]);
     }
 
     private static Result kMedoids(double[][] matrix, ScoringFunction sf) {
         Result overallBest = Clusterer.INSTANCE.new Result(
                 Double.NEGATIVE_INFINITY, null, matrix);
         for (int k = 2; k <= matrix.length; k++) {
             Result result = Clusterer.kMedoids(matrix, sf, k);
             double score = Clusterer.scoreCluster(result.medoids, matrix,
                     Clusterer.scoringPAMSIL);
             if (score > overallBest.score) {
                 overallBest.score = score;
                 overallBest.medoids = result.medoids;
             }
         }
         Clusterer.LOGGER.debug("Final score for clustering: "
                 + overallBest.score);
         return overallBest;
     }
 
     private static Result kMedoids(double[][] matrix, ScoringFunction sf, int k) {
         double overallBestScore = Double.NEGATIVE_INFINITY;
         Set<Integer> overallBestMedoids = null;
         for (int trial = 0; trial < 10; trial++) {
             int[] options = new int[matrix.length];
             for (int i = 0; i < matrix.length; i++) {
                 options[i] = i;
             }
             for (int i = 0; i < matrix.length; i++) {
                 int j = Clusterer.RANDOM.nextInt(matrix.length);
                 int tmp = options[i];
                 options[i] = options[j];
                 options[j] = tmp;
             }
             Set<Integer> medoids = new HashSet<>();
             for (int i = 0; i < k; i++) {
                 medoids.add(options[i]);
             }
 
             double score = Clusterer.scoreCluster(medoids, matrix, sf);
             while (true) {
                 Set<Integer> nonmedoids = new HashSet<>();
                 for (int i = 0; i < matrix.length; i++) {
                     if (!medoids.contains(i)) {
                         nonmedoids.add(i);
                     }
                 }
 
                 double bestScore = score;
                 Set<Integer> bestMedoids = medoids;
                 for (int i : medoids) {
                     for (int j : nonmedoids) {
                         Set<Integer> swap = new HashSet<>();
                         for (int m : medoids) {
                             swap.add(m == i ? j : m);
                         }
                         double newScore = Clusterer.scoreCluster(swap, matrix,
                                 sf);
                         if (newScore > bestScore) {
                             bestScore = newScore;
                             bestMedoids = swap;
                         }
                     }
                 }
 
                 if (bestScore > score) {
                     score = bestScore;
                     medoids = bestMedoids;
 
                 } else {
                     break;
                 }
             }
 
             if (score > overallBestScore) {
                 overallBestScore = score;
                 overallBestMedoids = medoids;
             }
         }
 
         Clusterer.LOGGER.debug("Final score for clustering (k=" + k + "): "
                 + overallBestScore);
         return Clusterer.INSTANCE.new Result(overallBestScore,
                 overallBestMedoids, matrix);
     }
 
     private static double scoreByDistance(
             Map<Integer, Set<Integer>> clustering, double[][] matrix) {
         double result = 0;
         for (Entry<Integer, Set<Integer>> entry : clustering.entrySet()) {
             int j = entry.getKey();
             for (int i : entry.getValue()) {
                 result += matrix[j][i];
             }
         }
         return -result;
     }
 
     private static double scoreBySilhouette(
             Map<Integer, Set<Integer>> clustering, double[][] matrix) {
         double result = 0;
         for (Entry<Integer, Set<Integer>> e1 : clustering.entrySet()) {
             if (e1.getValue().size() == 1) {
                 continue;
             }
             for (int j : e1.getValue()) {
                 double aj = 0;
                 for (int i : e1.getValue()) {
                     aj += matrix[j][i];
                 }
                 aj /= e1.getValue().size();
 
                 double bj = Double.POSITIVE_INFINITY;
                 for (Entry<Integer, Set<Integer>> e2 : clustering.entrySet()) {
                    if (e1.getKey() == e2.getKey()) {
                         continue;
                     }
 
                     double bjk = 0;
                     for (int k : e2.getValue()) {
                         bjk += matrix[j][k];
                     }
                     bjk /= e2.getValue().size();
 
                     if (bjk < bj) {
                         bj = bjk;
                     }
                 }
                 result += (bj - aj) / Math.max(aj, bj);
             }
         }
         return result;
     }
 
     private static double scoreCluster(Set<Integer> medoids, double[][] matrix,
             ScoringFunction sf) {
         Map<Integer, Set<Integer>> clustering = Clusterer.getClustering(
                 medoids, matrix);
         return sf.score(clustering, matrix);
     }
 
     private Clusterer() {
     }
 }
