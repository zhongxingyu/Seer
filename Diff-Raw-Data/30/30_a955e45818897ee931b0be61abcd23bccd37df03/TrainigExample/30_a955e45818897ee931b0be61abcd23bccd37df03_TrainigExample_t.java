 package com.github.tteofili.nlputils;
 
 /**
 * A {@link TrainigExample} holds some inputs and a corresponding output
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
