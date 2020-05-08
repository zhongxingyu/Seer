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
 package eu.cassandra.training.utils;
 
 import jMEF.MixtureModel;
 import jMEF.PVector;
 import jMEF.UnivariateGaussian;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.util.Vector;
 
 /**
  * This class is used to create the Normal and Gaussian Mixture Models
  * Distributions out of Histograms using Expectation Maximization
  * methods.
  * 
  * @author Antonios Chrysopoulos
  * @version 0.9, Date: 29.07.2013
  */
 
 public class MixtureCreator
 {
   /** The number of mixtures for the GMM distribution. */
   int n;
 
   /**
    * This array contains the values from the sample (times per day, duration,
    * start minute of the day).
    */
   Integer[] temp;
 
   /**
    * This variable stated the maximum value that appears in the array of values.
    */
   int max;
 
   /**
    * Simple constructor of the MixtureCreator
    */
   public MixtureCreator ()
   {
     n = 0;
   }
 
   /**
    * This function is used for the creation of an GMM Distribution given the
    * variable and the input.The result is exported to a file.
    * 
    * @param input
    *          The input file with the value array.
    * @param output
    *          The output file name.
    * @param variable
    *          The random variable for which the GMM is created.
    * @throws IOException
    */
   public void createGMM (String input, String output, String variable)
     throws IOException
   {
 
     temp = readFile(input);
 
     if (variable.equals("Duration") || variable.equals("DailyTimes")) {
       max = findMax();
       n = 10;
     }
     else if (variable.equals("StartTime")) {
       max = Constants.MINUTES_PER_DAY;
      n = 10;
     }
     else if (variable.equals("StartTimeBinned")) {
       max = Constants.MINUTES_PER_DAY / Constants.TEN_MINUTES;
      n = 10;
     }
 
     int interval = (int) (max / n);
     int median = (int) (interval / 2);
 
     // Initial mixture model
     MixtureModel mm = new MixtureModel(n);
     MixtureModel best = null;
     mm.EF = new UnivariateGaussian();
     for (int i = 0; i < n; i++) {
       PVector param = new PVector(2);
       param.array[0] = interval * i + median;
       param.array[1] = param.array[0] / 10;
       mm.param[i] = param;
       mm.weight[i] = 1;
     }
     mm.normalizeWeights();
     // System.out.println("Initial mixture model \n" + mm + "\n");
 
     double[] whatever = new double[1];
 
     // PVector[] pointsInit = mm.drawRandomPoints(n);
     PVector[] points = mm.drawRandomPoints(temp.length);
 
     for (int i = 0; i < temp.length; i++) {
 
       whatever[0] = temp[i];
       points[i].setArray(whatever.clone());
 
     }
 
     // Vector<PVector>[] clusters = new Vector[n];
     //
     // for (int i = 0; i < n; i++) {
     //
     // whatever[0] = (1440.0 / n) * i;
     // pointsInit[i].setArray(whatever.clone());
     // Vector<PVector> tempVector = new Vector<PVector>();
     // tempVector.add(pointsInit[i]);
     //
     // clusters[i] = tempVector;
     // }
 
     double logBest = Double.NEGATIVE_INFINITY, logNew = 0;
     MixtureModel mmc = null;
 
     for (int i = 0; i < 100; i++) {
 
       Vector<PVector>[] clusters = KMeans.run(points, n);
 
       // Classical MixtureCreator
 
       mmc = ExpectationMaximization1D.initialize(clusters);
       // System.out.println("Mixture model initial state \n" + mmc + "\n");
       mmc = ExpectationMaximization1D.run(points, mmc);
 
       logNew = Math.abs(ExpectationMaximization1D.logLikelihood(points, mmc));
 
       // System.out.println(logNew);
 
       if (logBest < logNew) {
         logBest = logNew;
         best = mmc;
       }
     }
 
     // System.out
     // .println("Mixture model estimated using classical MixtureCreator \n"
    // + best + "\n");
 
     GMM2File(best, output);
 
   }
 
   /**
    * This function is used for the creation of an Gaussian distribution given
    * the variable and the input.The result is exported to a file.
    * 
    * @param input
    *          The input file with the value array.
    * @param output
    *          The output file name.
    * @param variable
    *          The random variable for which the GMM is created.
    * @throws IOException
    */
   public void createNormal (String input, String output, String variable)
     throws IOException
   {
 
     temp = readFile(input);
     n = 1;
 
     if (variable.equals("Duration") || variable.equals("DailyTimes"))
       max = findMax();
     else if (variable.equals("StartTime"))
       max = Constants.MINUTES_PER_DAY;
     else if (variable.equals("StartTimeBinned"))
       max = Constants.MINUTES_PER_DAY / Constants.TEN_MINUTES;
 
     int interval = (int) (max / n);
 
     int median = (int) (interval / 2);
 
     // Initial mixture model
     MixtureModel mm = new MixtureModel(n);
     mm.EF = new UnivariateGaussian();
     for (int i = 0; i < n; i++) {
       PVector param = new PVector(2);
       param.array[0] = interval * i + median;
       param.array[1] = param.array[0] / 10;
       mm.param[i] = param;
       mm.weight[i] = i + 1;
     }
     mm.normalizeWeights();
     // System.out.println("Initial mixture model \n" + mm + "\n");
 
     double[] whatever = new double[1];
 
     PVector[] points = mm.drawRandomPoints(temp.length);
 
     for (int i = 0; i < temp.length; i++) {
 
       whatever[0] = temp[i];
       points[i].setArray(whatever.clone());
 
     }
 
     Vector<PVector>[] clusters = KMeans.run(points, n);
 
     // Classical MixtureCreator
     MixtureModel mmc;
     mmc = ExpectationMaximization1D.initialize(clusters);
 
     // System.out
     // .println("Mixture model estimated using classical MixtureCreator \n"
     // + mmc + "\n");
 
     Gaussian2File(mmc, output);
 
   }
 
   /**
    * This function is used for the parsing of the file containing the attribute
    * values to add them in the array.
    * 
    * @param filename
    *          The input file with the value array.
    * @return an array with the values.
    * @throws FileNotFoundException
    */
   private Integer[] readFile (String filename) throws FileNotFoundException
   {
     ArrayList<Integer> temp = new ArrayList<Integer>();
 
     File file = new File(filename);
     Scanner input = new Scanner(file);
     String nextLine;
 
     while (input.hasNext()) {
       nextLine = input.nextLine();
       temp.add(Integer.parseInt(nextLine));
     }
 
     input.close();
 
     Integer[] result = new Integer[temp.size()];
 
     result = temp.toArray(result);
 
     return result;
   }
 
   /**
    * This function is used for exporting the newly created GMM distribution to a
    * file that will be parsed later on from the Training Module to create the
    * activity model in demand.
    * 
    * @param mm
    *          The Mixture Model created.
    * @param filename
    *          The name of the export file.
    * @throws IOException
    */
   private void GMM2File (MixtureModel mm, String filename) throws IOException
   {
 
     String line;
 
     int n = mm.size;
     double[] weights = mm.weight;
     Double[] means = new Double[n];
     Double[] sigmas = new Double[n];
 
     for (int i = 0; i < n; i++) {
 
       line = mm.param[i].toString().substring(3, 15);
       line = line.replace(" ", "");
       means[i] = Double.parseDouble(line);
 
       line =
         mm.param[i].toString().substring(16,
                                          mm.param[i].toString().length() - 1);
       line = line.replace(" ", "");
 
       sigmas[i] = Double.parseDouble(line);
 
     }
 
     DecimalFormat df = new DecimalFormat("#.##########");
     PrintStream realSystemOut = System.out;
 
     OutputStream output = new FileOutputStream(filename);
     PrintStream printOut = new PrintStream(output);
     System.setOut(printOut);
 
     System.out.println("Max Value:" + max);
 
     System.out.println(n);
 
     for (int i = 0; i < n; i++) {
 
       System.out.print(df.format(weights[i]));
       if (i != n - 1)
         System.out.print("-");
       else
         System.out.println();
     }
 
     for (int i = 0; i < n; i++) {
 
       System.out.print(df.format(means[i]));
       if (i != n - 1)
         System.out.print("-");
       else
         System.out.println();
 
     }
 
     for (int i = 0; i < n; i++) {
 
       if (sigmas[i] < 0.00000001) {
         sigmas[i] = 0.00000001;
       }
 
       System.out.print(df.format(sigmas[i]));
       if (i != n - 1)
         System.out.print("-");
       else
         System.out.println();
 
     }
 
     System.setOut(realSystemOut);
 
     output.close();
 
   }
 
   /**
    * This function is used for exporting the newly created Normal distribution
    * to a
    * file that will be parsed later on from the Training Module to create the
    * activity model in demand.
    * 
    * @param mm
    *          The Mixture Model created which has one distribution only.
    * @param filename
    *          The name of the export file.
    * @throws IOException
    */
   private void Gaussian2File (MixtureModel mm, String filename)
     throws IOException
   {
 
     String line;
 
     int n = mm.size;
     Double[] means = new Double[n];
     Double[] sigmas = new Double[n];
 
     for (int i = 0; i < n; i++) {
 
       line = mm.param[i].toString().substring(3, 15);
       line = line.replace(" ", "");
       means[i] = Double.parseDouble(line);
 
       line =
         mm.param[i].toString().substring(16,
                                          mm.param[i].toString().length() - 1);
       line = line.replace(" ", "");
 
       sigmas[i] = Double.parseDouble(line);
 
     }
 
     DecimalFormat df = new DecimalFormat("#.##########");
     PrintStream realSystemOut = System.out;
 
     OutputStream output = new FileOutputStream(filename);
     PrintStream printOut = new PrintStream(output);
     System.setOut(printOut);
 
     System.out.println("Max Value:" + max);
 
     for (int i = 0; i < n; i++) {
 
       System.out.print(df.format(means[i]));
       if (i != n - 1)
         System.out.print("-");
       else
         System.out.println();
 
     }
 
     for (int i = 0; i < n; i++) {
 
       System.out.print(df.format(sigmas[i]));
       if (i != n - 1)
         System.out.print("-");
       else
         System.out.println();
 
     }
 
     System.setOut(realSystemOut);
 
     output.close();
 
   }
 
   /**
    * This function is responsible for finding the greatest value that appears in
    * the array of values.
    * 
    * @return the largest value of the array.
    */
   public int findMax ()
   {
 
     int maxValue = -1;
 
     for (int i = 0; i < temp.length; i++) {
       maxValue = Math.max(temp[i], maxValue);
     }
 
     return maxValue;
   }
 
 }
