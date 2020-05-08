 package com.github.tteofili.nlputils;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 
 /**
 * Add javadoc here
  */
 public class TrainingSet implements Iterable<TrainigExample> {
 
     private Collection<TrainigExample> trainigExamples = new HashSet<TrainigExample>();
 
     @Override
     public Iterator<TrainigExample> iterator() {
         return trainigExamples.iterator();
     }
 
     public void add(TrainigExample trainigExample) {
         trainigExamples.add(trainigExample);
     }
 }
