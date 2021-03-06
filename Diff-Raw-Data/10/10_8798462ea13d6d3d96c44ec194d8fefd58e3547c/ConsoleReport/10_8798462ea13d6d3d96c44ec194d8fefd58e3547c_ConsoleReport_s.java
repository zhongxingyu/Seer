 /**
  * Copyright (C) 2009 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.google.caliper;
 
import com.google.common.base.Function;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.LinkedHashMultimap;
 import com.google.common.collect.Multimap;
 import com.google.common.collect.Ordering;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Prints a report containing the tested values and the corresponding
  * measurements. Measurements are grouped by variable using indentation.
  * Alongside numeric values, quick-glance ascii art bar charts are printed.
  * Sample output:
  * <pre>
  *              benchmark                 d     ns logarithmic runtime
  * ConcatenationBenchmark 3.141592653589793   4397 ||||||||||||||||||||||||
  * ConcatenationBenchmark              -0.0    223 |||||||||||||||
  *     FormatterBenchmark 3.141592653589793  33999 ||||||||||||||||||||||||||||||
  *     FormatterBenchmark              -0.0  26399 |||||||||||||||||||||||||||||
  * </pre>
  */
 final class ConsoleReport {
 
   private static final int bargraphWidth = 30;
   private static final String vmKey = "vm";
 
   private final List<Parameter> parameters;
   private final Result result;
   private final List<Run> runs;
 
   private final double maxValue;
   private final double logMaxValue;
   private final int decimalDigits;
   private final double divideBy;
   private final String units;
   private final int measurementColumnLength;
 
   ConsoleReport(Result result) {
     this.result = result;
 
     double min = Double.POSITIVE_INFINITY;
     double max = 0;
 
     Multimap<String, String> nameToValues = LinkedHashMultimap.create();
     List<Parameter> parametersBuilder = new ArrayList<Parameter>();
     for (Map.Entry<Run, Double> entry : result.getMeasurements().entrySet()) {
       Run run = entry.getKey();
       double d = entry.getValue();
 
       min = Math.min(min, d);
       max = Math.max(max, d);
 
       for (Map.Entry<String, String> parameter : run.getParameters().entrySet()) {
         String name = parameter.getKey();
         nameToValues.put(name, parameter.getValue());
       }
 
       nameToValues.put(vmKey, run.getVm());
     }
 
     for (Map.Entry<String, Collection<String>> entry : nameToValues.asMap().entrySet()) {
       Parameter parameter = new Parameter(entry.getKey(), entry.getValue());
       parametersBuilder.add(parameter);
     }
 
     /*
      * Figure out how much influence each parameter has on the measured value.
      * We sum the measurements taken with each value of each parameter. For
      * parameters that have influence on the measurement, the sums will differ
      * by value. If the parameter has little influence, the sums will be similar
      * to one another and close to the overall average. We take the standard
      * deviation across each parameters collection of sums. Higher standard
      * deviation implies higher influence on the measured result.
      */
     double sumOfAllMeasurements = 0;
     for (double measurement : result.getMeasurements().values()) {
       sumOfAllMeasurements += measurement;
     }
     for (Parameter parameter : parametersBuilder) {
       int numValues = parameter.values.size();
       double[] sumForValue = new double[numValues];
       for (Map.Entry<Run, Double> entry : result.getMeasurements().entrySet()) {
         Run run = entry.getKey();
         sumForValue[parameter.index(run)] += entry.getValue();
       }
       double mean = sumOfAllMeasurements / sumForValue.length;
       double stdDeviationSquared = 0;
       for (double value : sumForValue) {
         double distance = value - mean;
         stdDeviationSquared += distance * distance;
       }
       parameter.stdDeviation = Math.sqrt(stdDeviationSquared / numValues);
     }
 
     this.parameters = new StandardDeviationOrdering().reverse().sortedCopy(parametersBuilder);
     this.runs = new ByParametersOrdering().sortedCopy(result.getMeasurements().keySet());
     this.maxValue = max;
     this.logMaxValue = Math.log(max);
 
     int numDigitsInMin = ceil(Math.log10(min));
     if (numDigitsInMin > 9) {
       divideBy = 1000000000;
       decimalDigits = Math.max(0, 9 + 3 - numDigitsInMin);
       units = "s";
     } else if (numDigitsInMin > 6) {
       divideBy = 1000000;
       decimalDigits = Math.max(0, 6 + 3 - numDigitsInMin);
       units = "ms";
     } else if (numDigitsInMin > 3) {
       divideBy = 1000;
       decimalDigits = Math.max(0, 3 + 3 - numDigitsInMin);
       units = "us";
     } else {
       divideBy = 1;
       decimalDigits = 0;
       units = "ns";
     }
     measurementColumnLength = max > 0
         ? ceil(Math.log10(max / divideBy)) + decimalDigits + 1
         : 1;
   }
 
   /**
    * A parameter plus all of its values.
    */
   private static class Parameter {
     final String name;
     final ImmutableList<String> values;
     final int maxLength;
     double stdDeviation;
 
    // TODO: this should be in Functions.java
    private static final Function<String,Integer> STRING_LENGTH_FUNCTION =
        new Function<String, Integer>() {
          public Integer apply(String s) {
            return s.length();
          }
        };

     Parameter(String name, Collection<String> values) {
       this.name = name;
       this.values = ImmutableList.copyOf(values);
 
       int maxLen = name.length();
       for (String value : values) {
         maxLen = Math.max(maxLen, value.length());
       }
       this.maxLength = maxLen;
     }
 
     String get(Run run) {
       return vmKey.equals(name) ? run.getVm() : run.getParameters().get(name);
     }
 
     int index(Run run) {
       return values.indexOf(get(run));
     }
 
     boolean isInteresting() {
       return values.size() > 1;
     }
   }
 
   /**
    * Orders the different parameters by their standard deviation. This results
    * in an appropriate grouping of output values.
    */
   private static class StandardDeviationOrdering extends Ordering<Parameter> {
     public int compare(Parameter a, Parameter b) {
       return Double.compare(a.stdDeviation, b.stdDeviation);
     }
   }
 
   /**
    * Orders runs by the parameters.
    */
   private class ByParametersOrdering extends Ordering<Run> {
     public int compare(Run a, Run b) {
       for (Parameter parameter : parameters) {
         int aValue = parameter.values.indexOf(parameter.get(a));
         int bValue = parameter.values.indexOf(parameter.get(b));
         int diff = aValue - bValue;
         if (diff != 0) {
           return diff;
         }
       }
       return 0;
     }
   }
 
   void displayResults() {
     printValues();
     System.out.println();
     printUninterestingParameters();
   }
 
   /**
    * Prints a table of values.
    */
   private void printValues() {
     // header
     for (Parameter parameter : parameters) {
       if (parameter.isInteresting()) {
         System.out.printf("%" + parameter.maxLength + "s ", parameter.name);
       }
     }
     System.out.printf("%" + measurementColumnLength + "s logarithmic runtime%n", units);
 
     // rows
     String numbersFormat = "%" + measurementColumnLength + "." + decimalDigits + "f %s%n";
     for (Run run : runs) {
       for (Parameter parameter : parameters) {
         if (parameter.isInteresting()) {
           System.out.printf("%" + parameter.maxLength + "s ", parameter.get(run));
         }
       }
       double measurement = result.getMeasurements().get(run);
       System.out.printf(numbersFormat, measurement / divideBy, bargraph(measurement));
     }
   }
 
   /**
    * Prints parameters with only one unique value.
    */
   private void printUninterestingParameters() {
     for (Parameter parameter : parameters) {
       if (!parameter.isInteresting()) {
         System.out.println(parameter.name + ": " + Iterables.getOnlyElement(parameter.values));
       }
     }
   }
 
   /**
    * Returns a string containing a bar of proportional width to the specified
    * value.
    */
   private String bargraph(double value) {
     int numLinearChars = floor(value / maxValue * bargraphWidth);
     double logValue = Math.log(value);
     int numChars = floor(logValue / logMaxValue * bargraphWidth);
     StringBuilder sb = new StringBuilder(numChars);
     for (int i = 0; i < numLinearChars; i++) {
       sb.append("X");
     }
 
     for (int i = numLinearChars; i < numChars; i++) {
       sb.append("|");
     }
     return sb.toString();
   }
 
   @SuppressWarnings("NumericCastThatLosesPrecision")
   private static int floor(double d) {
     return (int) d;
   }
 
   @SuppressWarnings("NumericCastThatLosesPrecision")
   private static int ceil(double d) {
     return (int) Math.ceil(d);
   }
 }
