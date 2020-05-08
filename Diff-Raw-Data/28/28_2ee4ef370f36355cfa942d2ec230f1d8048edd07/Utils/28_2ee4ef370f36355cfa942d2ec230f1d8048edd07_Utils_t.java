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
 
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
 import java.util.ArrayList;
 
 import com.google.ortools.constraintsolver.DecisionBuilder;
 import com.google.ortools.constraintsolver.IntVar;
 import com.google.ortools.constraintsolver.OptimizeVar;
 import com.google.ortools.constraintsolver.Solver;
 
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

    char SEP = File.separatorChar;
    File dir = new File(System.getProperty("java.home") + SEP + "bin");
    File dirSource = new File("extLib");
    File file1 = new File(dir, "jnilinearsolver.dll");
    File file2 = new File(dir, "jniconstraintsolver.dll");
    File file1Source = new File(dirSource, "jnilinearsolver.dll");
    File file2Source = new File(dirSource, "jniconstraintsolver.dll");
    try {
      if (file1.isFile() == false)
        Files.copy(file1Source.toPath(), file1.toPath(), REPLACE_EXISTING);

      if (file2.isFile() == false)
        Files.copy(file2Source.toPath(), file2.toPath(), REPLACE_EXISTING);
    }
    catch (IOException e) {

      e.printStackTrace();
    }

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
 
     double upperLimit = (1 + Constants.ERROR_FRINGE) * limit;
 
     return (trueValue < upperLimit);
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
       if (z.value() > Constants.SOLUTION_THRESHOLD)
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
 
   public static String getFileName (String filename)
   {
     return filename.replace(".", ",").split(",")[0];
   }
 }
