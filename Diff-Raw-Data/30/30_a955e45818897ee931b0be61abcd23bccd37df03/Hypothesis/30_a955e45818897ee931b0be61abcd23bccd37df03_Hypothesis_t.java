 package com.github.tteofili.nlputils;
 
 /**
 * An {@link Hypothesis} maps a series of inputs in an output
  */
 public interface Hypothesis {
     double calculateOutput(double... inputs);
 }
