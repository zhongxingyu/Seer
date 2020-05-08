 package com.github.tteofili.nlputils;
 
 /**
 * Add javadoc here
  */
 public class TrainigExample {
     private double[] inputs;
     private double output;
 
     public TrainigExample(double[] inputs, double output) {
         this.inputs = inputs;
         this.output = output;
     }
 
     public double[] getInputs() {
         return inputs;
     }
 
     public double getOutput() {
         return output;
     }
 }
