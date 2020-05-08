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
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 
import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 
 import eu.cassandra.training.response.Incentive;
 import eu.cassandra.training.response.IncentiveVector;
 import eu.cassandra.training.response.PricingVector;
 import eu.cassandra.training.utils.Constants;
 import eu.cassandra.training.utils.RNG;
 import eu.cassandra.training.utils.Utils;
 
 public class Histogram implements ProbabilityDistribution
 {
 
   private String name = "";
   private String type = "";
   private String distributionID = "";
 
   protected int numberOfBins;
   protected double[] values;
 
   public Histogram (String name, double[] values)
   {
     this.name = name;
     type = "Histogram";
     numberOfBins = values.length;
     this.values = values;
   }
 
   public Histogram (String filename) throws FileNotFoundException
   {
     name = filename;
     type = "Histogram";
     Map<Integer, Double> histogram = new HashMap<Integer, Double>();
     File file = new File(filename);
     Scanner input = new Scanner(file);
     String nextLine = input.nextLine();
     String[] line = new String[2];
 
     // System.out.println(nextLine);
 
     while (input.hasNext()) {
       nextLine = input.nextLine();
       line = nextLine.split("-");
       // System.out.println(line[0] + " " + line[1]);
 
       line[1] = line[1].replace(",", ".");
 
       histogram.put(Integer.parseInt(line[0]), Double.parseDouble(line[1]));
 
     }
 
     numberOfBins = (Integer.parseInt(line[0])) + 1;
     values = new double[numberOfBins];
 
     for (int i = 0; i < numberOfBins; i++) {
 
       if (histogram.containsKey(i))
         values[i] = histogram.get(i);
       else
         values[i] = 0;
     }
 
     /*
      * double sum = 0;
      * 
      * for (int i = 0; i < numberOfBins; i++) sum += values[i];
      * 
      * System.out.println(sum);
      */
     input.close();
     file.deleteOnExit();
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
 
   @Override
   public int getNumberOfParameters ()
   {
     return 1;
   }
 
   @Override
   public double getParameter (int index)
   {
 
     return numberOfBins;
   }
 
   @Override
   public void setParameter (int index, double value)
   {
   }
 
   @Override
   public void precompute (int startValue, int endValue, int nBins)
   {
   }
 
   public String getDescription ()
   {
     String description = "Histogram probability Frequency function";
     return description;
   }
 
   public double getProbability (int x)
   {
     if (x < 0)
       return 0;
     else
       return values[x];
   }
 
   public double getPrecomputedProbability (int x)
   {
     if (x < 0)
       return 0;
     else
       return values[x];
   }
 
   public int getPrecomputedBin ()
   {
 
     double dice = RNG.nextDouble();
     double sum = 0;
     for (int i = 0; i < numberOfBins; i++) {
       sum += values[i];
 
       if (dice < sum)
         return i;
     }
     return -1;
   }
 
   public void status ()
   {
     System.out.println("Histogram Distribution");
     System.out.println("Number of Beans: " + numberOfBins);
     System.out.println("Values:");
 
     for (int i = 0; i < values.length; i++) {
       System.out.println("Index: " + i + " Value: " + values[i]);
     }
 
   }
 
   public double[] getHistogram ()
   {
 
     return values;
 
   }
 
   public double getProbabilityGreaterEqual (int x)
   {
     double prob = 0;
 
     int start = (int) x;
 
     for (int i = start; i < values.length; i++)
       prob += values[i];
 
     return prob;
   }
 
   public double getProbabilityLess (int x)
   {
     return 1 - getProbabilityGreaterEqual(x);
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
 
   @Override
   public void shifting (int shiftingCase, double[] basicScheme,
                         double[] newScheme)
   {
 
     if (shiftingCase == 0) {
 
       values = shiftingBest(newScheme);
     }
     else if (shiftingCase == 1) {
 
       values = shiftingNormal(basicScheme, newScheme);
     }
     else if (shiftingCase == 2) {
 
       values = shiftingWorst(basicScheme, newScheme);
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
       result[i] = values[i] / newScheme[i];
       sum += result[i];
     }
 
     for (int i = 0; i < result.length; i++)
       result[i] /= sum;
 
     return result;
 
   }
 
   @Override
   public double[] shiftingNormal (double[] basicScheme, double[] newScheme)
   {
     double[] result = Arrays.copyOf(this.values, this.values.length);
 
     IncentiveVector inc = new IncentiveVector(basicScheme, newScheme);
 
     for (Incentive incentive: inc.getIncentives()) {
       result = movingAverage(result, incentive);
     }
 
     return result;
 
   }
 
   @Override
   public double[] shiftingWorst (double[] basicScheme, double[] newScheme)
   {
     double[] result = Arrays.copyOf(this.values, this.values.length);
 
     PricingVector pricingVector = new PricingVector(basicScheme, newScheme);
 
     result = worstAverage(result, pricingVector);
 
     return result;
   }
 
   public DBObject toJSON (String activityModelID)
   {
    BasicDBList param = new BasicDBList();
     DBObject temp = new BasicDBObject();
 
     temp.put("name", name);
     temp.put("type", type);
     temp.put("description", name + " " + type);
     temp.put("distrType", type);
     temp.put("actmod_id", activityModelID);
     temp.put("values", values);
    temp.put("parameters", param);
     return temp;
 
   }
 }
