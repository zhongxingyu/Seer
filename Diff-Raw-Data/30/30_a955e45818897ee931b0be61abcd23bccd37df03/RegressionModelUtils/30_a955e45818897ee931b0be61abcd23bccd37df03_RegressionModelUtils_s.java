 package com.github.tteofili.nlputils;
 
 /**
 * Add javadoc here
  */
 public class RegressionModelUtils {
 
     public static double ordinaryLeastSquares(TrainingSet trainingSet, Hypothesis hypothesis) {
         double output = 0;
         for (TrainigExample trainigExample : trainingSet) {
             output += Math.pow(hypothesis.calculateOutput(trainigExample.getInputs()) - trainigExample.getOutput(), 2);
         }
         return output / 2;
     }
 
 }
