 /*
 Copyright 2011-2013 The Cassandra Consortium (cassandra-fp7.eu)
 
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
 
 package eu.cassandra.utils;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.paukov.combinatorics.Factory;
 import org.paukov.combinatorics.Generator;
 import org.paukov.combinatorics.ICombinatoricsVector;
 
 import weka.clusterers.SimpleKMeans;
 import weka.core.Attribute;
 import weka.core.DenseInstance;
 import weka.core.Instance;
 import weka.core.Instances;
 import weka.filters.Filter;
 import weka.filters.unsupervised.attribute.AddCluster;
 
 import com.google.ortools.constraintsolver.DecisionBuilder;
 import com.google.ortools.constraintsolver.IntVar;
 import com.google.ortools.constraintsolver.OptimizeVar;
 import com.google.ortools.constraintsolver.Solver;
 
 import eu.cassandra.appliance.Appliance;
 
 /**
  * This class contains static functions that are used for general purposes
  * throughout the Disaggregation Module.
  * 
  * @author Antonios Chrysopoulos
  * @version 0.9, Date: 29.07.2013
  */
 
 public class Utils
 {
 
   /** Loading a library for integer programming. */
   static {
     System.loadLibrary("jniconstraintsolver");
   }
 
   /**
    * This function is estimating the absolute euclidean distance of the active
    * and reactive power vector distance of two points of interest in the form of
    * arrays.
    * 
    * @param a1
    *          The first array of values
    * @param a2
    *          The second array of values
    * 
    * @return the estimated absolute euclidean distance.
    */
   public static double absoluteEuclideanDistance (double[] a1, double[] a2)
   {
     return Math.sqrt(Math.pow(a1[0] - a2[0], 2) + Math.pow(a1[1] - a2[1], 2));
   }
 
   /**
    * This function is estimating the percentage euclidean distance of the active
    * and reactive power vector distance of two points of interest in the form of
    * arrays.
    * 
    * @param a1
    *          The first array of values
    * 
    * @param a2
    *          The second array of values
    * 
    * @return the estimated percentage euclidean distance.
    */
   public static double percentageEuclideanDistance (double[] a1, double[] a2)
   {
     return 100
            * Math.sqrt(Math.pow(a1[0] - a2[0], 2) + Math.pow(a1[1] - a2[1], 2))
            / norm(a1);
   }
 
   /**
    * This function is estimating the euclidean length (or norm) of an array of
    * two values
    * 
    * @param poi
    *          The point of interest's array of values
    * @return the euclidean length of the array.
    */
   public static double norm (double[] poi)
   {
     return Math.sqrt(Math.pow(poi[0], 2) + Math.pow(poi[1], 2));
   }
 
   /**
    * This function is used in order to check if a certain appliance is within
    * the permitted limits
    * 
    * @param trueValue
    *          The value under examination
    * @param limit
    *          The limit value that is used as threshold
    * @return true if it is within limits, false otherwise
    */
   public static boolean checkLimit (double trueValue, double limit)
   {
 
     double lowerLimit = (1 - Constants.ERROR_FRINGE) * limit;
 
     return (trueValue > lowerLimit);
   }
 
   /**
    * This function is used in order to check if a certain appliance is within
    * the permitted limits
    * 
    * @param trueValue
    *          The value under examination
    * @param limit
    *          The limit value that is used as threshold
    * @return true if it is within limits, false otherwise
    */
   public static boolean checkLimitFridge (double trueValue, double limit)
   {
 
     double lowerLimit = (1 - Constants.ERROR_FRIDGE) * limit;
     double upperLimit = (1 + Constants.ERROR_FRIDGE) * limit;
 
     return (trueValue < upperLimit && trueValue > lowerLimit);
   }
 
   /**
    * This function is used in order to check if a certain appliance is within
    * the permitted limits
    * 
    * @param trueValue
    *          The value under examination
    * @param limit
    *          The limit value that is used as threshold
    * @return true if it is within limits, false otherwise
    */
   public static double pairingLimit (double trueValue)
   {
 
     double upperLimit = (1 + Constants.PAIR_ERROR_FRINGE) * trueValue;
 
     return upperLimit;
   }
 
   /**
    * This function is used for the detection of reduction points following a
    * rising point, so that there is a possibility they can be connected as a
    * pair
    * 
    * @param index
    *          The index of the rising point of interest.
    * @param pois
    *          The list of points of interest under examination.
    * @return an array of indices that contain possible combinatorial reduction
    *         points of interest.
    */
   public static Integer[] findRedPoints (int index,
                                          ArrayList<PointOfInterest> pois)
   {
 
     ArrayList<Integer> temp = new ArrayList<Integer>();
     double limit = pairingLimit(pois.get(index).getPDiff());
     for (int i = index + 1; i < pois.size(); i++)
       if (pois.get(i).getRising() == false && limit > -pois.get(i).getPDiff())
         temp.add(i);
 
     Integer[] result = new Integer[temp.size()];
 
     for (int i = 0; i < temp.size(); i++)
       result[i] = temp.get(i);
 
     return result;
 
   }
 
   /**
    * This function is used for the detection of rising points preceding a
    * reduction point, so that there is a possibility they can be connected as a
    * pair
    * 
    * @param index
    *          The index of the reduction point of interest.
    * @param pois
    *          The list of points of interest under examination.
    * @return an array of indices that contain possible combinatorial rising
    *         points of interest.
    */
   public static Integer[] findRisPoints (int index,
                                          ArrayList<PointOfInterest> pois)
   {
 
     ArrayList<Integer> temp = new ArrayList<Integer>();
     double limit = -pairingLimit(pois.get(index).getPDiff());
     for (int i = 0; i < index; i++)
       if (pois.get(i).getRising() && limit > pois.get(i).getPDiff())
         temp.add(i);
 
     Integer[] result = new Integer[temp.size()];
 
     for (int i = 0; i < temp.size(); i++)
       result[i] = temp.get(i);
 
     return result;
 
   }
 
   /**
    * This is an auxiliary function used to estimate the mean values of a pair of
    * points of interest.
    * 
    * @param pois
    *          The pair of points of interest under examination.
    * @return an array of the mean values of active and reactive power.
    */
   public static double[] meanValues (PointOfInterest[] pois)
   {
 
     double[] result =
       { (pois[0].getPDiff() - pois[1].getPDiff()) / 2,
        (pois[0].getQDiff() - pois[1].getQDiff()) / 2 };
 
     return result;
 
   }
 
   /**
    * This auxiliary function checks if two pairs of points of interest are cross
    * covered
    * or not into time.
    * 
    * @param poi1
    *          The first pair of the points of interest.
    * @param poi2
    *          The second pair of the points of interest.
    * @return true if they are not cross covered, false otherwise.
    */
   public static boolean independentPair (PointOfInterest[] poi1,
                                          PointOfInterest[] poi2)
   {
     return (poi2[0].getMinute() > poi1[1].getMinute());
   }
 
   public static int sumArray (int[] array)
   {
     int sum = 0;
 
     for (int i = 0; i < array.length; i++)
       sum += array[i];
 
     return sum;
   }
 
   /**
    * This is an integer programming solver.
    * 
    * @param input
    *          The input array of alternatives.
    * @param cost
    *          The cost array of the alternatives.
    * @return a list of all the solutions.
    */
   public static ArrayList<ArrayList<Integer>> solve (int[][] input,
                                                      double[] cost)
   {
     ArrayList<ArrayList<Integer>> solutions =
       new ArrayList<ArrayList<Integer>>();
     Solver solver = new Solver("Integer Programming");
 
     int num_alternatives = cost.length;
     int num_objects = input[0].length;
 
     int[] costNew = new int[cost.length];
     int lambda = 1000;
     for (int i = 0; i < costNew.length; i++) {
       costNew[i] = (int) (100 * cost[i]);
       costNew[i] *= lambda;
     }
     //
     // variables
     //
     IntVar[] x = solver.makeIntVarArray(num_alternatives, 0, 1, "x");
 
     // number of assigned senators, to be minimize
     IntVar z = solver.makeScalProd(x, costNew).var();
 
     //
     // constraints
     //
 
     for (int j = 0; j < num_objects; j++) {
       IntVar[] b = new IntVar[num_alternatives];
       for (int i = 0; i < num_alternatives; i++) {
         b[i] = solver.makeProd(x[i], input[i][j]).var();
       }
 
       solver.addConstraint(solver.makeSumLessOrEqual(b, 1));
 
     }
 
     //
     // objective
     //
     OptimizeVar objective = solver.makeMaximize(z, 1);
 
     //
     // search
     //
     DecisionBuilder db =
       solver.makePhase(x, solver.INT_VAR_DEFAULT, solver.INT_VALUE_DEFAULT);
     solver.newSearch(db, objective);
 
     //
     // output
     //
 
     // ArrayList<Integer> temp = ArrayList<Integer>()
     ArrayList<Integer> temp = null;
     while (solver.nextSolution()) {
       temp = new ArrayList<Integer>();
       System.out.println("z: " + z.value());
       System.out.print("Selected alternatives: ");
       for (int i = 0; i < num_alternatives; i++) {
         if (x[i].value() == 1) {
           System.out.print((1 + i) + " ");
           temp.add(i);
         }
       }
       solutions.add(temp);
       // System.out.println("\n");
 
     }
     solver.endSearch();
 
     // Statistics
     System.out.println();
     System.out.println("Solutions: " + solver.solutions());
     System.out.println("Failures: " + solver.failures());
     System.out.println("Branches: " + solver.branches());
     System.out.println("Wall time: " + solver.wallTime() + "ms");
 
     return solutions;
 
   }
 
   /**
    * This is an integer programming solver.
    * 
    * @param input
    *          The input array of alternatives.
    * @param cost
    *          The cost array of the alternatives.
    * @return a list of the indexes of the solution alternatives.
    */
   public static ArrayList<Integer> solve2 (int[][] input, double[] cost)
   {
 
     Solver solver = new Solver("Integer Programming");
 
     int num_alternatives = cost.length;
     int num_objects = input[0].length;
 
     int solutionThreshold = 0;
 
     if (input[0].length < 10)
       solutionThreshold = Constants.SOLUTION_THRESHOLD_UNDER_10;
     else if (input[0].length < 15)
       solutionThreshold = Constants.SOLUTION_THRESHOLD_UNDER_15;
     else if (input[0].length < 20)
       solutionThreshold = Constants.SOLUTION_THRESHOLD_UNDER_20;
     else
       solutionThreshold = Constants.SOLUTION_THRESHOLD_UNDER_25;
 
     System.out.println("Objects: " + num_objects + " Threshold: "
                        + solutionThreshold);
 
     int[] costNew = new int[cost.length];
 
     for (int i = 0; i < costNew.length; i++)
       costNew[i] = (int) (10000 * cost[i]);
 
     //
     // variables
     //
     IntVar[] x = solver.makeIntVarArray(num_alternatives, 0, 1, "x");
 
     // number of assigned senators, to be minimize
     IntVar z = solver.makeScalProd(x, costNew).var();
 
     //
     // constraints
     //
 
     for (int j = 0; j < num_objects; j++) {
       IntVar[] b = new IntVar[num_alternatives];
       for (int i = 0; i < num_alternatives; i++) {
         b[i] = solver.makeProd(x[i], input[i][j]).var();
       }
 
       solver.addConstraint(solver.makeSumLessOrEqual(b, 1));
 
     }
 
     //
     // objective
     //
     OptimizeVar objective = solver.makeMaximize(z, 1);
 
     //
     // search
     //
     DecisionBuilder db =
       solver.makePhase(x, solver.INT_VAR_DEFAULT, solver.INT_VALUE_DEFAULT);
     solver.newSearch(db, objective);
 
     //
     // output
     //
     ArrayList<Integer> temp = new ArrayList<Integer>();
     while (solver.nextSolution()) {
       temp.clear();
       System.out.println("z: " + z.value());
       System.out.print("Selected alternatives: ");
       for (int i = 0; i < num_alternatives; i++) {
         if (x[i].value() == 1) {
           System.out.print((1 + i) + " ");
           temp.add(i);
         }
       }
       if (z.value() > solutionThreshold)
         break;
       // System.out.println("\n");
 
     }
     solver.endSearch();
 
     // Statistics
     System.out.println();
     System.out.println("Solutions: " + solver.solutions());
     System.out.println("Failures: " + solver.failures());
     System.out.println("Branches: " + solver.branches());
     System.out.println("Wall time: " + solver.wallTime() + "ms");
 
     return temp;
 
   }
 
   /**
    * Testing another type of solution.
    * 
    * @param input
    *          The input array of alternatives.
    * @param cost
    *          The cost array of the alternatives.
    * @return a list of the indexes of the solution alternatives.
    */
   public static ArrayList<Integer> solve3 (int[][] input, double[] cost)
   {
 
     Solver solver = new Solver("Integer Programming");
 
     int num_alternatives = cost.length;
     int num_objects = input[0].length;
 
     int[] costNew = new int[cost.length];
 
     for (int i = 0; i < costNew.length; i++)
       costNew[i] = (int) (10000 * cost[i]);
 
     //
     // variables
     //
     IntVar[] x = solver.makeIntVarArray(num_alternatives, 0, 1, "x");
 
     // number of assigned senators, to be minimize
     IntVar z = solver.makeScalProd(x, costNew).var();
 
     //
     // constraints
     //
 
     for (int j = 0; j < num_objects; j++) {
       IntVar[] b = new IntVar[num_alternatives];
       for (int i = 0; i < num_alternatives; i++) {
         b[i] = solver.makeProd(x[i], input[i][j]).var();
       }
 
       solver.addConstraint(solver.makeSumEquality(b, 1));
 
     }
 
     //
     // objective
     //
     OptimizeVar objective = solver.makeMaximize(z, 1);
 
     //
     // search
     //
     DecisionBuilder db =
       solver.makePhase(x, solver.INT_VAR_DEFAULT, solver.INT_VALUE_DEFAULT);
     solver.newSearch(db, objective);
 
     //
     // output
     //
     ArrayList<Integer> temp = new ArrayList<Integer>();
     while (solver.nextSolution()) {
       temp.clear();
       System.out.println("z: " + z.value());
       System.out.print("Selected alternatives: ");
       for (int i = 0; i < num_alternatives; i++) {
         if (x[i].value() == 1) {
           System.out.print((1 + i) + " ");
           temp.add(i);
         }
       }
       if (z.value() > Constants.OTHER_SOLUTION_THRESHOLD)
         break;
       // System.out.println("\n");
 
     }
     solver.endSearch();
 
     // Statistics
     System.out.println();
     System.out.println("Solutions: " + solver.solutions());
     System.out.println("Failures: " + solver.failures());
     System.out.println("Branches: " + solver.branches());
     System.out.println("Wall time: " + solver.wallTime() + "ms");
 
     return temp;
 
   }
 
   /**
    * This function is used for the creation of final matching pairs of points of
    * interest from the solutions that the integer programming solver has
    * provided.
    * 
    * @param pois
    *          The list of points of interest under examination.
    * @param array
    *          An array of 0-1 that shows which points of interest are included
    *          in the solution.
    * @return a list of pairs of points of interest.
    */
   public static ArrayList<PointOfInterest[]>
     createFinalPairs (ArrayList<PointOfInterest> pois, int[] array)
   {
     // Initializing the auxiliary variables.
     ArrayList<PointOfInterest[]> result = new ArrayList<PointOfInterest[]>();
     ArrayList<PointOfInterest> rising = new ArrayList<PointOfInterest>();
     ArrayList<PointOfInterest> reduction = new ArrayList<PointOfInterest>();
 
     // For all the points if the are 1 are included in the solution
     for (int i = 0; i < array.length; i++) {
 
       if (array[i] == 1) {
         if (pois.get(i).getRising())
           rising.add(pois.get(i));
         else
           reduction.add(pois.get(i));
       }
 
     }
 
     // If there are one of each point types.
     if (rising.size() == 1 && reduction.size() == 1) {
 
       PointOfInterest[] temp = { rising.get(0), reduction.get(0) };
       result.add(temp);
     }
     // If there is only one rising
     else if (rising.size() == 1) {
 
       for (PointOfInterest red: reduction) {
 
         PointOfInterest start =
           new PointOfInterest(rising.get(0).getMinute(), true, -red.getPDiff(),
                               -red.getQDiff());
 
         PointOfInterest[] temp = { start, red };
         result.add(temp);
       }
 
     }
     // If there is only one reduction
     else {
       for (PointOfInterest rise: rising) {
 
         PointOfInterest end =
           new PointOfInterest(reduction.get(0).getMinute(), false,
                               -rise.getPDiff(), -rise.getQDiff());
 
         PointOfInterest[] temp = { rise, end };
         result.add(temp);
       }
     }
 
     return result;
   }
 
   /**
    * This function is used to extract the file name from a path of a file,
    * excluding the file extension.
    * 
    * @param filename
    *          The full name and path of the file of interest.
    * @return The name of the file without the file extension.
    */
   public static String getFileName (String filename)
   {
     return filename.substring(0, filename.length() - 4);
   }
 
   /**
    * This function is used in order to create clusters of points of interest
    * based on the active power difference they have.
    * 
    * @param pois
    *          The list of points of interest that will be clustered.
    * @return The newly created clusters with the points that are comprising
    *         them.
    * @throws Exception
    */
   public static ArrayList<ArrayList<PointOfInterest>>
     clusterPoints (ArrayList<PointOfInterest> pois) throws Exception
   {
     // Initialize the auxiliary variables
     ArrayList<ArrayList<PointOfInterest>> result =
       new ArrayList<ArrayList<PointOfInterest>>();
 
     // Estimating the number of clusters that will be created
     int numberOfClusters =
       (int) (Math.ceil((double) pois.size()
                        / (double) Constants.MAX_POINTS_OF_INTEREST));
 
     System.out.println("Clusters: " + pois.size() + " / "
                        + Constants.MAX_POINTS_OF_INTEREST + " = "
                        + numberOfClusters);
 
     // Create a new empty list of points for each cluster
     for (int i = 0; i < numberOfClusters; i++)
       result.add(new ArrayList<PointOfInterest>());
 
     // Initializing auxiliary variables namely the attributes of the data set
     Attribute id = new Attribute("id");
     Attribute pDiffRise = new Attribute("pDiff");
 
     ArrayList<Attribute> attr = new ArrayList<Attribute>();
     attr.add(id);
     attr.add(pDiffRise);
 
     Instances instances = new Instances("Points of Interest", attr, 0);
 
     // Each event is translated to an instance with the above attributes
     for (int i = 0; i < pois.size(); i++) {
 
       Instance inst = new DenseInstance(2);
       inst.setValue(id, i);
       inst.setValue(pDiffRise, Math.abs(pois.get(i).getPDiff()));
 
       instances.add(inst);
 
     }
 
     // System.out.println(instances.toString());
 
     Instances newInst = null;
 
     System.out.println("Instances: " + instances.toSummaryString());
 
     // Create the addcluster filter of Weka and the set up the hierarchical
     // clusterer.
     AddCluster addcluster = new AddCluster();
 
     SimpleKMeans kmeans = new SimpleKMeans();
 
     kmeans.setSeed(numberOfClusters);
 
     // This is the important parameter to set
     kmeans.setPreserveInstancesOrder(true);
     kmeans.setNumClusters(numberOfClusters);
     kmeans.buildClusterer(instances);
 
     addcluster.setClusterer(kmeans);
     addcluster.setInputFormat(instances);
     addcluster.setIgnoredAttributeIndices("1");
 
     // Cluster data set
     newInst = Filter.useFilter(instances, addcluster);
 
     // System.out.println(newInst.toString());
 
     // Parse through the dataset to see where each point is placed in the
     // clusters.
     for (int i = 0; i < newInst.size(); i++) {
 
       String cluster = newInst.get(i).stringValue(newInst.attribute(2));
 
       cluster = cluster.replace("cluster", "");
 
       System.out.println("Point of Interest: " + i + " Cluster: " + cluster);
 
       result.get(Integer.parseInt(cluster) - 1).add(pois.get(i));
     }
 
     // Sorting the each cluster points by their minutes.
    for (int i = 0; i < result.size(); i++)
      Collections.sort(result.get(i), Constants.comp);
 
     // Sorting the all clusters by their active power.
     Collections.sort(result, Constants.comp5);
 
     return result;
   }
 
   /**
    * This function is utilized for the extraction of the points that are not
    * combined with other ones in order to create the final pairs of operation.
    * 
    * @param pois
    *          The list of all the points of interest.
    * @param solution
    *          This is the list that contains the solution vectors for the points
    *          of interest.
    * @param solutionArray
    *          This array contains the indices of the points of interest
    *          participating in each solution.
    * @return
    */
   public static ArrayList<PointOfInterest>
     extractRemainingPoints (ArrayList<PointOfInterest> pois,
                             ArrayList<Integer> solution, int[][] solutionArray)
   {
 
     ArrayList<PointOfInterest> result = new ArrayList<PointOfInterest>();
     int[] tempArray = new int[solutionArray[0].length];
 
     for (Integer index: solution)
       for (int i = 0; i < solutionArray[index].length; i++)
         if (solutionArray[index][i] == 1)
           tempArray[i] = 1;
 
     // System.out.println("TempArray:" + Arrays.toString(tempArray));
 
     for (int i = 0; i < tempArray.length; i++)
       if (tempArray[i] == 0)
         result.add(pois.get(i));
 
     if (result.size() == 0)
       result = null;
 
     return result;
   }
 
   /**
    * This function is used to remove the smallest points of interest from a list
    * in order to make its size viable to estimate the pairs.
    * 
    * @param pois
    *          The list of points of interest.
    * @return The list of points of interest with a percentage of the points
    *         removed.
    */
   public static ArrayList<PointOfInterest>
     removePoints (ArrayList<PointOfInterest> pois)
   {
 
     ArrayList<PointOfInterest> result = new ArrayList<PointOfInterest>(pois);
 
     int number = result.size() * Constants.REMOVAL_PERCENTAGE / 100;
 
     System.out.println("Initial Size: " + result.size() + " Removing: "
                        + number);
 
     Collections.sort(pois, Constants.comp4);
 
     System.out.println("Initial POIS: " + pois.toString());
 
     Collections.sort(result, Constants.comp4);
 
     for (int i = 0; i < number; i++)
       result.remove(result.size() - 1);
 
     System.out.println("Final POIS: " + result.toString());
 
     return result;
 
   }
 
   /**
    * This function is responsible for creating the possible combinations of the
    * rising and reduction points of interest in the event in order to create the
    * final pairs that can be matched.
    * 
    * @param temp
    *          The list of points of interest in the procedure.
    * @param complex
    *          The flag that show that this is a complex procedure due to the
    *          large number of points of interest involved.
    * @return A hashmap of the matched points with the distance that they have.
    */
   public static Map<int[], Double>
     findCombinations (ArrayList<PointOfInterest> temp, boolean complex)
   {
 
     // Initializing the auxiliary variables
     Map<int[], Double> input = new HashMap<int[], Double>();
     Integer[] points = null;
     int[] pointsArray = null;
     List<Integer> subset = new ArrayList<Integer>();
     double distance = 0;
 
     int distanceThreshold = 0;
 
     if (complex)
       distanceThreshold = Constants.SECOND_DISTANCE_THRESHOLD;
     else
       distanceThreshold = Constants.DISTANCE_THRESHOLD;
 
     // For each point
     for (int i = 0; i < temp.size(); i++) {
       // If rising point then we find the reduction points after that point,
       // create all the possible subsets from them and estimate the distance
       // of the active and reactive power measurements.If the distance is
       // under a certain threshold, the combination is accepted.
 
       double previousMaxDistance = Double.NEGATIVE_INFINITY, currentMaxDistance =
         Double.NEGATIVE_INFINITY;
 
       if (temp.get(i).getRising()) {
 
         // Returns the reduction points that can be associated with this rising
         // point
         points = Utils.findRedPoints(i, temp);
 
         // An initial vector of all the reduction points
         ICombinatoricsVector<Integer> initialSet = Factory.createVector(points);
         System.out.println("Initial Set for point " + temp.get(i).toString()
                            + ": " + initialSet.toString());
         System.out.println("Reduction Points for rising point " + i + ": "
                            + initialSet.toString());
 
         // Set the max combination of reduction point for each rising point
         int upperThres =
           Math.min(Constants.MAX_POINTS_LIMIT, initialSet.getSize());
         // System.out.println("Upper Threshold:" + upperThres);
 
         // For a number of reduction points for the rising point
         for (int pairing = 1; pairing <= upperThres; pairing++) {
 
           Generator<Integer> gen =
             Factory.createSimpleCombinationGenerator(initialSet, pairing);
 
           for (ICombinatoricsVector<Integer> subSet: gen) {
             // System.out.println(subSet.toString());
             if (subSet.getSize() > 0) {
               double sumP = 0, sumQ = 0;
 
               subset = subSet.getVector();
 
               for (Integer index: subset) {
                 sumP += temp.get(index).getPDiff();
                 sumQ += temp.get(index).getQDiff();
               }
 
               double[] tempValues = { -sumP, -sumQ };
 
               distance =
                 1 / (temp.get(i).percentageEuclideanDistance(tempValues) + Constants.NEAR_ZERO);
 
               // If accepted then an array is created with 1 in the index of the
               // points included, 0 otherwise
               if ((1 / distance) < distanceThreshold) {
 
                 System.out.println("Subset: " + subSet.toString()
                                    + " Distance: " + 1 / distance + " Z: "
                                    + distance);
 
                 pointsArray = new int[temp.size()];
                 pointsArray[i] = 1;
                 for (Integer index: subset)
                   pointsArray[index] = 1;
 
                 if (distance > currentMaxDistance) {
                   // System.out.println("Distance: " + distance
                   // + " Current Max Distance: "
                   // + currentMaxDistance);
                   currentMaxDistance = distance;
                 }
 
                 // Check if the array is already included in the alternatives.If
                 // not, it is added to the alternatives.
                 boolean flag = true;
                 for (int[] content: input.keySet()) {
                   if (Arrays.equals(content, pointsArray)) {
                     flag = false;
                     break;
                   }
                 }
 
                 if (flag) {
                   input.put(pointsArray, distance);
                   // System.out.println("Input:" + input);
                 }
               }
             }
           }
           // System.out.println("Input size for pairing of " + pairing + ":"
           // + input.size());
           // System.out.println("Current Max Distance: " + currentMaxDistance
           // + " Previous Max Distance:" + previousMaxDistance);
 
           // Checking if the max distance is reduced and continue for larger
           // combination else stop the procedure
           if (previousMaxDistance < currentMaxDistance
               || currentMaxDistance == Double.NEGATIVE_INFINITY)
             previousMaxDistance = currentMaxDistance;
           else {
             // System.out.println("Breaking for pairing = " + pairing);
             break;
           }
 
         }
       }
       // If reduction point then we find the rising points before that point,
       // create all the possible subsets from them and estimate the distance
       // of the active and reactive power measurements.If the distance is
       // under a certain threshold, the combination is accepted.
       else {
         points = Utils.findRisPoints(i, temp);
 
         ICombinatoricsVector<Integer> initialSet = Factory.createVector(points);
 
         System.out.println("Initial Set for point " + temp.get(i).toString()
                            + ": " + initialSet.toString());
         System.out.println("Rising Points for reduction point " + i + ": "
                            + initialSet.toString());
 
         int upperThres =
           Math.min(Constants.MAX_POINTS_LIMIT, initialSet.getSize());
         // System.out.println("Upper Threshold:" + upperThres);
 
         for (int pairing = 1; pairing <= upperThres; pairing++) {
 
           Generator<Integer> gen =
             Factory.createSimpleCombinationGenerator(initialSet, pairing);
 
           for (ICombinatoricsVector<Integer> subSet: gen) {
 
             if (subSet.getSize() > 0) {
               double sumP = 0, sumQ = 0;
 
               subset = subSet.getVector();
 
               for (Integer index: subset) {
                 sumP += temp.get(index).getPDiff();
                 sumQ += temp.get(index).getQDiff();
               }
 
               double[] sum = { sumP, sumQ };
               double[] tempValues =
                 { -temp.get(i).getPDiff(), -temp.get(i).getQDiff() };
 
               distance =
                 1 / (Utils.percentageEuclideanDistance(sum, tempValues) + Constants.NEAR_ZERO);
 
               // If accepted then an array is created with 1 in the index of the
               // points included, 0 otherwise
               if ((1 / distance) < distanceThreshold) {
 
                 System.out.println("Subset: " + subSet.toString()
                                    + " Distance: " + 1 / distance + " Z: "
                                    + distance);
 
                 pointsArray = new int[temp.size()];
                 pointsArray[i] = 1;
                 for (Integer index: subset)
                   pointsArray[index] = 1;
 
                 if (distance > currentMaxDistance) {
                   // System.out.println("Distance: " + distance
                   // + " Current Max Distance: "
                   // + currentMaxDistance);
                   currentMaxDistance = distance;
                 }
 
                 // Check if the array is already included in the alternatives.If
                 // not, it is added to the alternatives.
                 boolean flag = true;
                 for (int[] content: input.keySet()) {
                   if (Arrays.equals(content, pointsArray)) {
                     flag = false;
                     break;
                   }
                 }
 
                 if (flag) {
                   input.put(pointsArray, distance);
                   // System.out.println("Input:" + input);
                 }
               }
             }
           }
 
           // System.out.println("Input size for pairing of " + pairing + ":"
           // + input.size());
           // System.out.println("Current Max Distance: " + currentMaxDistance
           // + " Previous Max Distance:" + previousMaxDistance);
 
           if (previousMaxDistance < currentMaxDistance
               || currentMaxDistance == Double.NEGATIVE_INFINITY)
             previousMaxDistance = currentMaxDistance;
           else {
             // System.out.println("Breaking for pairing = " + pairing);
             break;
           }
         }
       }
     }
     return input;
   }
 
   /**
    * This function is used in order to find the maximum value from an array.
    * 
    * @param matrix
    * @return
    */
   public static double findMax (double[] matrix)
   {
 
     double result = Double.NEGATIVE_INFINITY;
 
     for (int i = 0; i < matrix.length; i++)
       if (result < matrix[i])
         result = matrix[i];
 
     return result;
   }
 
   /**
    * This function is used when the user has already tracked the electrical
    * appliances installed in the installation. He can used them as a base case
    * and extend it with any additional ones that may be found during the later
    * stages of analysis of the consumption.
    * 
    * @param filename
    *          The filename of the file containing the appliances.
    * @return
    *         A list of appliances
    * @throws FileNotFoundException
    */
   public static ArrayList<Appliance> appliancesFromFile (String filename)
     throws FileNotFoundException
   {
     // Read appliance file and start appliance parsing
     File file = new File(filename);
     Scanner input = new Scanner(file);
 
     ArrayList<Appliance> appliances = new ArrayList<Appliance>();
 
     String nextLine;
     String[] line;
 
     while (input.hasNext()) {
       nextLine = input.nextLine();
       line = nextLine.split(",");
       String name = line[0];
       String activity = line[1];
       double p = Double.parseDouble(line[3]);
       double q = Double.parseDouble(line[4]);
 
       // For each appliance found in the file, an temporary Appliance
       // Entity is created.
       appliances.add(new Appliance(name, activity, p, q));
 
     }
 
     System.out.println("Appliances:" + appliances.size());
 
     input.close();
 
     return appliances;
   }
 
   /**
    * This is an auxiliary function used in case of the refrigerator in order to
    * estimate some metrics useful for the successful detection of its end-use in
    * more complex events.
    */
   public static double[]
     calculateMetrics (Map<Integer, ArrayList<PointOfInterest>> risingPoints,
                       Map<Integer, ArrayList<PointOfInterest>> reductionPoints)
   {
     double[] result = new double[3];
     // Initializing auxiliary variables.
     int counter = 0;
 
     // Create a collection of the events in the rising and reduction
     // maps' keysets.
     Set<Integer> keys = new TreeSet<Integer>();
     keys.addAll(risingPoints.keySet());
     keys.addAll(reductionPoints.keySet());
 
     // For each event present, a search for a clean one to one identification of
     // rising and reduction points is at hand.
     for (Integer key: keys) {
 
       if (risingPoints.containsKey(key) && reductionPoints.containsKey(key)) {
 
         result[0] +=
           risingPoints.get(key).get(0).getPDiff()
                   - reductionPoints.get(key).get(0).getPDiff();
         result[1] +=
           risingPoints.get(key).get(0).getQDiff()
                   - reductionPoints.get(key).get(0).getQDiff();
         result[2] +=
           reductionPoints.get(key).get(0).getMinute()
                   - risingPoints.get(key).get(0).getMinute();
         PointOfInterest[] temp =
           { risingPoints.get(key).get(0), reductionPoints.get(key).get(0) };
 
         ArrayList<PointOfInterest[]> tempArray =
           new ArrayList<PointOfInterest[]>();
         tempArray.add(temp);
         // matchingPoints.put(key, tempArray);
         // numberOfMatchingPoints += 2;
         counter++;
         risingPoints.remove(key);
         reductionPoints.remove(key);
 
       }
     }
     // From those the mean duration is calculated.
     result[0] /= (counter * 2);
     result[1] /= (counter * 2);
     result[2] /= counter;
 
     keys.clear();
 
     risingPoints.clear();
     reductionPoints.clear();
 
     return result;
   }
 
   /**
    * This is an auxiliary function used to check if the distance in time and
    * space of a pair is close to this appliance, meaning that it belongs to this
    * appliance.
    * 
    * @param mean
    *          The mean active and reactive power measurements.
    * @param duration
    *          The duration of the end-use.
    * @param metrics
    *          The metrics that are the base level
    * @return true if it is close, false otherwise.
    */
   public static boolean isCloseRef (double[] mean, int duration,
                                     double[] metrics)
   {
 
     double[] meanValues = { metrics[0], metrics[1] };
 
     return ((Utils.percentageEuclideanDistance(mean, meanValues) < Constants.REF_THRESHOLD) && (Utils
             .checkLimitFridge(duration, metrics[2])));
 
   }
 }
