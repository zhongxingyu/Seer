 /*   
    Copyright 2011-2012 The Cassandra Consortium (cassandra-fp7.eu)
 
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
 
 package eu.cassandra.training.behaviour;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Scanner;
 
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 
 import eu.cassandra.training.response.Incentive;
 import eu.cassandra.training.response.IncentiveVector;
 import eu.cassandra.training.response.PeakFinder;
 import eu.cassandra.training.response.PricingVector;
 import eu.cassandra.training.utils.Constants;
 import eu.cassandra.training.utils.RNG;
 import eu.cassandra.training.utils.Utils;
 
 /**
  * @author Antonios Chrysopoulos
  * @version prelim
  * @since 2012-26-06
  */
 public class GaussianMixtureModels implements ProbabilityDistribution
 {
 
   private String name = "";
   private String type = "";
   private String distributionID = "";
   protected double[] pi;
   protected Gaussian[] gaussians;
 
   // For precomputation
   protected boolean precomputed;
   protected int numberOfBins;
   protected double precomputeFrom;
   protected double precomputeTo;
   protected double[] histogram;
 
   /**
    * Constructor. Sets the parameters of the standard normal distribution,
    * with mean 0 and standard deviation 1.
    */
   public GaussianMixtureModels (int n)
   {
     name = "Generic Mixture";
     type = "Gaussian Mixture Models";
     pi = new double[n];
     for (int i = 0; i < n; i++) {
       pi[i] = (1.0 / n);
       gaussians[i] = new Gaussian();
     }
     precomputed = false;
   }
 
   /**
    * @param mu
    *          Mean value of the Gaussian distribution.
    * @param s
    *          Standard deviation of the Gaussian distribution.
    */
   public GaussianMixtureModels (int n, double[] pi, double[] mu, double[] s)
   {
     name = "Generic Mixture";
     type = "Gaussian Mixture Models";
     gaussians = new Gaussian[n];
     for (int i = 0; i < n; i++) {
       this.pi = pi;
       gaussians[i] = new Gaussian(mu[i], s[i]);
     }
     precomputed = false;
   }
 
   public GaussianMixtureModels (String filename) throws FileNotFoundException
   {
     name = filename;
     type = "Gaussian Mixture Models";
     File file = new File(filename);
     Scanner input = new Scanner(file);
     String nextLine = input.nextLine();
 
     String[] temp = nextLine.split(":");
     int maxValue = Integer.parseInt(temp[1]);
 
     // Read the number of mixtures
     nextLine = input.nextLine();
     int n = Integer.parseInt(nextLine);
     String[] line = new String[n];
 
     // Read the percentage of presence
     nextLine = input.nextLine();
     line = nextLine.split("-");
     pi = new double[n];
     for (int i = 0; i < n; i++) {
 
       pi[i] = Double.parseDouble(line[i].replace(",", "."));
 
     }
 
     // Read the means
     nextLine = input.nextLine();
     line = nextLine.split("-");
     double[] mu = new double[n];
     for (int i = 0; i < n; i++) {
 
       mu[i] = Double.parseDouble(line[i].replace(",", "."));
 
     }
 
     // Read the sigmas
     nextLine = input.nextLine();
     line = nextLine.split("-");
     double[] s = new double[n];
     for (int i = 0; i < n; i++) {
 
       s[i] = Double.parseDouble(line[i].replace(",", "."));
 
     }
 
     gaussians = new Gaussian[n];
     for (int i = 0; i < n; i++) {
       gaussians[i] = new Gaussian(mu[i], s[i]);
     }
 
     precompute(0, maxValue, maxValue);
 
     // status();
 
     input.close();
 
   }
 
   public String getName ()
   {
     return name;
   }
 
   public String getType ()
   {
     return type;
   }
 
   public String getDistributionID ()
   {
     return distributionID;
   }
 
   public void setDistributionID (String id)
   {
     distributionID = id;
   }
 
   public String getDescription ()
   {
     String description = "Gaussian Mixture Models probability density function";
     return description;
   }
 
   public int getNumberOfParameters ()
   {
     return 3 * pi.length;
   }
 
   public double[] getParameters (int index)
   {
     double[] temp = new double[3];
 
     temp[0] = gaussians[index].getParameter(0);
     temp[1] = gaussians[index].getParameter(1);
     temp[2] = pi[index];
 
     return temp;
   }
 
   public void setParameters (int index, double[] values)
   {
 
     gaussians[index].setParameter(0, values[0]);
     gaussians[index].setParameter(1, values[1]);
     pi[index] = values[2];
 
   }
 
   public void precompute (int startValue, int endValue, int nBins)
   {
     if (startValue >= endValue) {
       System.out.println("The end point is before the start point.");
       return;
     }
     precomputeFrom = startValue;
     precomputeTo = endValue;
     numberOfBins = nBins;
     histogram = new double[nBins];
 
     for (int i = 0; i < gaussians.length; i++) {
       gaussians[i].precompute(startValue, endValue, nBins);
     }
 
     for (int i = 0; i < nBins; i++) {
 
       for (int j = 0; j < gaussians.length; j++) {
         histogram[i] += pi[j] * gaussians[j].getPrecomputedProbability(i);
       }
 
     }
 
     precomputed = true;
   }
 
   public double getProbability (int x)
   {
     double sum = 0;
     for (int j = 0; j < pi.length; j++) {
       sum += pi[j] * gaussians[j].getProbability(x);
     }
     return sum;
   }
 
   public double getCumulativeProbability (int z)
   {
     double sum = 0;
     for (int j = 0; j < pi.length; j++) {
       sum +=
         pi[j]
                 * Gaussian.bigPhi(z, gaussians[j].getParameter(0),
                                   gaussians[j].getParameter(1));
     }
     return sum;
   }
 
   public double getParameter (int index)
   {
     return 0;
   }
 
   public void setParameter (int index, double value)
   {
   }
 
   public double getPrecomputedProbability (int x)
   {
     if (!precomputed) {
       return -1;
     }
     double div = (precomputeTo - precomputeFrom) / (double) numberOfBins;
     int bin = (int) Math.floor((x - precomputeFrom) / div);
     if (bin == numberOfBins) {
       bin--;
     }
     return histogram[bin];
   }
 
   public int getPrecomputedBin ()
   {
     if (!precomputed) {
       return -1;
     }
     // double div = (precomputeTo - precomputeFrom) / (double) numberOfBins;
     double dice = RNG.nextDouble();
     double sum = 0;
     for (int i = 0; i < numberOfBins; i++) {
       sum += histogram[i];
       // if(dice < sum) return (int)(precomputeFrom + i * div);
       if (dice < sum)
         return i;
     }
     return -1;
   }
 
   public double[] getHistogram ()
   {
 
     return histogram;
 
   }
 
   public void status ()
   {
 
     System.out.print("Gaussian Mixture with");
     System.out.println(" Number of Mixtures:" + pi.length);
     for (int i = 0; i < pi.length; i++) {
       System.out.print("Mixture " + i);
       System.out.print(" Mean: " + gaussians[i].getParameter(0));
       System.out.print(" Sigma: " + gaussians[i].getParameter(1));
       System.out.print(" Weight: " + pi[i]);
       System.out.println();
     }
     System.out.println("Precomputed: " + precomputed);
     if (precomputed) {
       System.out.print("Number of Beans: " + numberOfBins);
       System.out.print(" Starting Point: " + precomputeFrom);
       System.out.println(" Ending Point: " + precomputeTo);
     }
     System.out.println(Arrays.toString(histogram));
   }
 
   public static void main (String[] args) throws IOException
   {
     System.out.println("Testing Mixture Creation.");
 
     GaussianMixtureModels g;
     RNG.init();
     /*
      * double[] pi = { 0.3, 0.5, 0.2 };
      * 
      * double[] mean = { 100, 200, 300 };
      * 
      * double[] std = { 10, 20, 30 };
      * 
      * g = new GaussianMixtureModels(3, pi, mean, std);
      */
     g = new GaussianMixtureModels("Files/newGMM.csv");
 
     double[] temp = g.getHistogram();
     double sum = 0;
 
     for (int i = 0; i < Constants.MINUTES_PER_DAY; i++)
       sum += temp[i];
 
     System.out.println("Summary:" + sum);
     PeakFinder pf = new PeakFinder(temp);
 
     pf.findLocalIntervalMaxima();
     pf.showListInterval();
     System.out.println(pf.findGlobalIntervalMaximum().toString());
     System.out.println();
 
     // int peakIndex = pf.findGlobalIntervalMaximum().getIndexMinute();
 
     // g.movePeak(peakIndex * Constants.MINUTES_PER_BIN, 30);
 
     g.precompute(0, 1440, 1440);
 
     double[] temp2 = g.getHistogram();
 
     System.out.println(temp.length + " " + temp2.length);
 
   }
 
   public double getProbabilityGreaterEqual (int x)
   {
     double prob = 0;
 
     int start = (int) x;
 
     for (int i = start; i < histogram.length; i++)
       prob += histogram[i];
 
     return prob;
   }
 
   public double getProbabilityLess (int x)
   {
     return 1 - getProbabilityGreaterEqual(x);
   }
 
   public double[] movingAverage (int index, int window)
   {
     int side = 0;
     if (window % 2 == 1)
       side = (int) (window / 2);
     else {
       window++;
       side = (int) (window / 2) + 1;
     }
 
     double[] histogram = Arrays.copyOf(this.histogram, this.histogram.length);
 
     int startIndex = Math.max(index - side, 0);
     int endIndex = Math.min(index + side, Constants.MINUTES_PER_DAY);
 
     for (int i = startIndex; i < endIndex; i++) {
       double temp = 0;
       // System.out.print("Index:" + i + " Old Value: " + values[i]);
 
       for (int j = -side; j < side; j++)
         temp += histogram[i + j];
 
       histogram[i] = temp / window;
       // System.out.println("New Value: " + values[i]);
     }
 
     double sum = 0;
 
     for (int i = 0; i < histogram.length; i++)
       sum += histogram[i];
 
     double diff = 1 - sum;
     double diffPortion = diff / window;
 
     System.out.println("Summary: " + sum + " Difference: " + diff
                        + " Portion: " + diffPortion);
 
     for (int i = 0; i < window; i++)
       histogram[endIndex + i] += diffPortion;
 
     for (int i = 0; i < window; i++)
       histogram[startIndex - i] += diffPortion;
 
     return histogram;
   }
 
   @Override
   public void shifting (int shiftingCase, double[] basicScheme,
                         double[] newScheme)
   {
 
     if (shiftingCase == 0) {
 
       histogram = shiftingBest(newScheme);
     }
     else if (shiftingCase == 1) {
 
       histogram = shiftingNormal(basicScheme, newScheme);
     }
     else if (shiftingCase == 2) {
 
       histogram = shiftingWorst(basicScheme, newScheme);
     }
     else {
       System.out.println("ERROR in shifting function");
     }
 
   }
 
   @Override
   public double[] shiftingPreview (int shiftingCase, double[] basicScheme,
                                    double[] newScheme)
   {
 
     double[] result = new double[Constants.MINUTES_PER_DAY];
 
     if (shiftingCase == 0) {
 
       result = shiftingBest(newScheme);
     }
     else if (shiftingCase == 1) {
 
       result = shiftingNormal(basicScheme, newScheme);
     }
     else if (shiftingCase == 2) {
 
       result = shiftingWorst(basicScheme, newScheme);
     }
     else {
       System.out.println("ERROR in shifting function");
     }
 
     result = Utils.aggregateStartTimeDistribution(result);
 
     return result;
 
   }
 
   @Override
   public double[] shiftingBest (double[] newScheme)
   {
     double[] result = new double[Constants.MINUTES_PER_DAY];
 
     double sum = 0;
 
     for (int i = 0; i < newScheme.length; i++) {
       result[i] = histogram[i] / newScheme[i];
       sum += result[i];
     }
 
     for (int i = 0; i < result.length; i++)
       result[i] /= sum;
 
     return result;
 
   }
 
   @Override
   public double[] shiftingNormal (double[] basicScheme, double[] newScheme)
   {
     double[] result = Arrays.copyOf(histogram, histogram.length);
 
     IncentiveVector inc = new IncentiveVector(basicScheme, newScheme);
 
     for (Incentive incentive: inc.getIncentives()) {
       result = movingAverage(result, incentive);
     }
 
     return result;
 
   }
 
   @Override
   public double[] shiftingWorst (double[] basicScheme, double[] newScheme)
   {
     double[] result = Arrays.copyOf(histogram, histogram.length);
 
     PricingVector pricingVector = new PricingVector(basicScheme, newScheme);
 
     result = worstAverage(result, pricingVector);
 
     return result;
   }
 
   private double[] movingAverage (double[] values, Incentive incentive)
   {
     int side = -1;
     int startIndex = incentive.getStartMinute();
     int endIndex = incentive.getEndMinute();
     double overDiff = 0;
     double temp = 0;
     String type = "";
     double sum = 0;
 
     if (incentive.isPenalty()) {
 
       if (incentive.getBeforeDifference() > 0
           && incentive.getAfterDifference() < 0)
         type = "Both";
 
       if (incentive.getBeforeDifference() > 0
           && incentive.getAfterDifference() >= 0)
         type = "Left";
 
       if (incentive.getBeforeDifference() <= 0
           && incentive.getAfterDifference() < 0)
         type = "Right";
 
       if (incentive.getBeforeDifference() < 0
           && incentive.getAfterDifference() > 0)
         type = "None";
     }
     else {
 
       if (incentive.getBeforeDifference() < 0
           && incentive.getAfterDifference() > 0)
         type = "Both";
 
       if (incentive.getBeforeDifference() < 0
           && incentive.getAfterDifference() <= 0)
         type = "Left";
 
       if (incentive.getBeforeDifference() >= 0
           && incentive.getAfterDifference() > 0)
         type = "Right";
 
       if (incentive.getBeforeDifference() > 0
           && incentive.getAfterDifference() < 0)
         type = "None";
 
     }
 
     System.out.println("Penalty: " + incentive.isPenalty() + " Type: " + type);
 
     if (!type.equalsIgnoreCase("None")) {
 
       if (incentive.isPenalty()) {
 
         for (int i = startIndex; i < endIndex; i++) {
           temp = incentive.getBase() * values[i] / incentive.getPrice();
           overDiff += values[i] - temp;
           values[i] = temp;
 
         }
         // System.out.println("Over Difference = " + overDiff);
         double additive = overDiff / Constants.SHIFTING_WINDOW_IN_MINUTES;
 
         switch (type) {
 
         case "Both":
 
           side = Constants.SHIFTING_WINDOW_IN_MINUTES / 2;
 
           for (int i = 0; i < side; i++) {
 
             int before = startIndex - i;
             if (before < 0)
               before += Constants.MINUTES_PER_DAY;
             int after = endIndex + i;
             if (after > Constants.MINUTES_PER_DAY - 1)
               after %= Constants.MINUTES_PER_DAY;
 
             values[before] += additive;
             values[after] += additive;
 
           }
           break;
 
         case "Left":
 
           side = Constants.SHIFTING_WINDOW_IN_MINUTES;
 
           for (int i = 0; i < side; i++) {
 
             int before = startIndex - i;
             if (before < 0)
               before += Constants.MINUTES_PER_DAY;
             values[before] += additive;
 
           }
           break;
 
         case "Right":
 
           side = Constants.SHIFTING_WINDOW_IN_MINUTES;
 
           for (int i = 0; i < side; i++) {
 
             int after = endIndex + i;
             if (after > Constants.MINUTES_PER_DAY - 1)
               after %= Constants.MINUTES_PER_DAY;
 
             values[after] += additive;
           }
         }
       }
       else {
         side = Constants.SHIFTING_WINDOW_IN_MINUTES * 2;
         switch (type) {
 
         case "Both":
 
           for (int i = startIndex - side; i < startIndex; i++) {
 
             int index = i;
 
             if (index < 0)
               index += Constants.MINUTES_PER_DAY;
 
             temp = incentive.getPrice() * values[index] / incentive.getBase();
             overDiff += values[index] - temp;
             values[index] = temp;
           }
 
           for (int i = endIndex; i < endIndex + side; i++) {
 
             int index = i;
 
             if (index > Constants.MINUTES_PER_DAY - 1)
               index %= Constants.MINUTES_PER_DAY;
 
             temp = incentive.getPrice() * values[index] / incentive.getBase();
             overDiff += values[index] - temp;
             values[index] = temp;
           }
           break;
 
         case "Left":
 
           for (int i = startIndex - 2 * side; i < startIndex; i++) {
 
             int index = i;
 
             if (index < 0)
               index += Constants.MINUTES_PER_DAY;
 
             temp = incentive.getPrice() * values[index] / incentive.getBase();
             overDiff += values[index] - temp;
             values[index] = temp;
           }
           break;
 
         case "Right":
 
           for (int i = endIndex; i < endIndex + 2 * side; i++) {
 
             int index = i;
 
             if (index > Constants.MINUTES_PER_DAY - 1)
               index %= Constants.MINUTES_PER_DAY;
 
             temp = incentive.getPrice() * values[index] / incentive.getBase();
             overDiff += values[index] - temp;
             values[index] = temp;
           }
 
         }
         // System.out.println("Over Difference = " + overDiff);
 
         double additive = overDiff / (endIndex - startIndex);
 
         for (int i = startIndex; i < endIndex; i++)
           values[i] += additive;
 
       }
 
     }
     for (int i = 0; i < values.length; i++)
       sum += values[i];
     System.out.println("Summary: " + sum);
 
     return values;
   }
 
   private double[] worstAverage (double[] values, PricingVector pricing)
   {
     double temp = 0;
     double sum = 0;
     double overDiff = 0;
     int start, end;
     double newPrice;
     int cheapest = pricing.getCheapest();
     int startCheapest =
       pricing.getPrices(pricing.getCheapest()).getStartMinute();
     int endCheapest = pricing.getPrices(pricing.getCheapest()).getEndMinute();
     int durationCheapest = endCheapest - startCheapest;
     double cheapestPrice = pricing.getPrices(pricing.getCheapest()).getPrice();
 
     for (int i = 0; i < pricing.getPrices().size(); i++) {
 
       if (i != cheapest) {
         sum = 0;
         overDiff = 0;
         start = pricing.getPrices(i).getStartMinute();
         end = pricing.getPrices(i).getEndMinute();
         newPrice = pricing.getPrices(i).getPrice();
 
         for (int j = start; j <= end; j++) {
           temp = cheapestPrice * values[j] / newPrice;
           overDiff += values[j] - temp;
           values[j] = temp;
         }
 
         double additive = overDiff / durationCheapest;
 
         for (int j = startCheapest; j <= endCheapest; j++)
           values[j] += additive;
 
         for (int j = 0; j < values.length; j++)
           sum += values[j];
         System.out.println("Summary after index " + i + ": " + sum);
 
       }
 
     }
 
     return values;
   }
 
   public DBObject toJSON (String activityModelID)
   {
    double[] values = new double[1];
     DBObject temp = new BasicDBObject();
     BasicDBList param = new BasicDBList();
 
     for (int i = 0; i < gaussians.length; i++) {
       DBObject paramItem = new BasicDBObject();
 
       double[] means = new double[gaussians.length];
       double[] sigmas = new double[gaussians.length];
 
       means[i] = gaussians[i].mean;
       sigmas[i] = gaussians[i].sigma;
 
       paramItem.put("w", pi[i]);
       paramItem.put("mean", means[i]);
       paramItem.put("std", sigmas[i]);
 
       param.add(paramItem);
     }
 
     temp.put("name", name);
     temp.put("type", type);
     temp.put("description", name + " " + type);
     temp.put("distrType", type);
     temp.put("actmod_id", activityModelID);
     temp.put("parameters", param);
    temp.put("values", values);
     return temp;
 
   }
 }
