 /*
  Copyright 2012 Joseph B. Ottinger
 
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
 
 package com.enigmastation.ml.bayes.impl;
 

 import org.testng.annotations.Test;
 
 import static org.testng.Assert.assertEquals;
 
 public class ClassifierTest {
     @Test
     public void testInternalsOfTrain() {
         SimpleClassifierImpl c = new SimpleClassifierImpl();
         c.train("the quick brown fox jumps over the lazy dog's tail", "good");
         c.train("make quick money in the online casino", "bad");
         assertEquals(c.featureCount("quick", "good"), 1);
         assertEquals(c.featureCount("quick", "bad"), 1);
     }
 
     @Test
     public void testfprob() {
         SimpleClassifierImpl cl = getTrainedClassifier();
         assertEquals(cl.featureProb("quick", "good"), 0.6666, 0.0001);
         assertEquals(cl.weightedProb("monei", "good"), 0.25, 0.001);
         train(cl);
         assertEquals(cl.weightedProb("monei", "good"), 0.166, 0.001);
     }
 
     @Test
     public void testBayes() {
         SimpleClassifierImpl cl = getTrainedClassifier();
         assertEquals(cl.prob("quick rabbit", "good"), 0.15624, 0.0001);
         assertEquals(cl.prob("quick rabbit", "bad"), 0.05, 0.001);
     }
 
     @Test
     public void testThreshold() {
         SimpleClassifierImpl cl = getTrainedClassifier();
         assertEquals(cl.getThreshold("bad"), 1.0, 0.01);
     }
 
     @Test
     public void testClassification() {
         SimpleClassifierImpl cl = getTrainedClassifier();
         assertEquals(cl.classify("quick rabbit", "unknown"), "good");
         assertEquals(cl.classify("quick money", "unknown"), "bad");
         cl.setThreshold("bad", 3.0);
         assertEquals(cl.classify("quick money", "unknown"), "unknown");
         for (int i = 0; i < 10; i++) {
             train(cl);
         }
         assertEquals(cl.classify("quick money", "unknown"), "bad");
     }
 
     private SimpleClassifierImpl getTrainedClassifier() {
         SimpleClassifierImpl cl = new SimpleClassifierImpl();
 
         train(cl);
         return cl;
     }
 
     private void train(SimpleClassifierImpl cl) {
         cl.train("Nobody owns the water", "good");
         cl.train("The quick rabbit jumps fences", "good");
         cl.train("Buy pharmaceuticals now", "bad");
         cl.train("make quick money in the online casino", "bad");
         cl.train("the quick brown fox jumps", "good");
     }
 
     private FisherClassifierImpl getTrainedFisher() {
         FisherClassifierImpl fc = new FisherClassifierImpl();
         train(fc);
         return fc;
     }
 
     @Test
     public void testFisherCProb() {
         FisherClassifierImpl cl = getTrainedFisher();
 
         assertEquals(cl.featureProb("quick", "good"), 0.571, 0.001);
         assertEquals(cl.featureProb("monei", "bad"), 1.0, 0.001);
         assertEquals(cl.fisherProbability("quick rabbit", "good"), 0.78, 0.001);
         assertEquals(cl.fisherProbability("quick rabbit", "bad"), 0.356, 0.001);
         assertEquals(cl.classify("quick rabbit"), "good");
         assertEquals(cl.classify("quick money"), "bad");
         cl.setMinimum("bad", 0.8);
         assertEquals(cl.classify("quick money"), "good");
         cl.setMinimum("bad", 0.4);
         assertEquals(cl.classify("quick money"), "bad");
     }
 }
