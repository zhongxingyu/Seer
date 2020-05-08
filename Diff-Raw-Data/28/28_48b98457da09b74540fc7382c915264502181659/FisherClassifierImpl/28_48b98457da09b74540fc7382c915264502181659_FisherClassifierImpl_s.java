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
 
 import com.enigmastation.ml.bayes.ClassifierDataFactory;
 import com.enigmastation.ml.bayes.FisherClassifier;
 import com.enigmastation.ml.bayes.annotations.BayesClassifier;
 import com.enigmastation.ml.bayes.annotations.FisherBayesClassifier;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 @BayesClassifier
 @FisherBayesClassifier
 public class FisherClassifierImpl extends SimpleClassifierImpl implements FisherClassifier {
     Map<Object, Double> minimums = new HashMap<>();
 
     /**
      * This accesses the current minimum strength for the category. If the probability of a
      * classification operation is less than this strength for this category, the result is discarded.
      *
      * @param category The category for which to acquire the minimum strength
      * @return the strength
      */
     @Override
     public double getMinimum(Object category) {
         if (minimums.containsKey(category)) {
             return minimums.get(category);
         } else {
             return 0.0;
         }
     }
 
     /**
      * This sets the minimum strength required for the supplied category to be considered as a
      * classification result. If the classification operation yields a weaker probability than
      * this strength, the result is ignored.
      * <p/>
      * The result of this is that you are able to say "classify as category 'x' but only if 'x'
      * is very likely," for example.
      *
      * @param category the category for which the strength is mutated
      * @param strength the minimum probability to accept for this category
      */
     @Override
     public void setMinimum(Object category, double strength) {
         minimums.put(category, strength);
     }
 
     public FisherClassifierImpl() {
         super();
     }
 
     public FisherClassifierImpl(ClassifierDataFactory factory) {
         super(factory);
     }
 
     @Override
     protected double featureProb(Object feature, Object category) {
         double clf = super.featureProb(feature, category);
         if (clf == 0.0) {
             return 0.0;
         }
         double frequencySum = 0.0;
         for (Object c : categories()) {
             frequencySum += super.featureProb(feature, c);
         }
         return clf / frequencySum;
     }
 
    private double fisherProbability(Object source, Object category) {
         double p = 1.0;
         List<Object> features = getFeatures(source);
         for (Object f : features) {
             p *= weightedProb(f, category);
         }
         double fisherScore = -2.0 * Math.log(p);
         return invChi(fisherScore, features.size() * 2);
     }
 
     protected double invChi(double chi, double df) {
         double m = chi / 2.0;
         double sum = Math.exp(-m);
         double term = sum;
         for (int i = 1; i < df / 2; i++) {
             term *= (m / i);
             sum += term;
         }
         return Math.min(sum, 1.0);
     }
 
     /**
      * This returns the best-match classification from the bayesian engine if
      * and only if the classification is more probable than the default thresholds
      * for classification.
      *
      * @param source the source corpus for the classification operation
      * @return the best-match classification
      */
     @Override
     public Object classify(Object source, Object defaultClassification) {
         Object best = defaultClassification;
         double max = 0.0;
         for (Object c : categories()) {
             double p = fisherProbability(source, c);
             if (p > getMinimum(c) && p > max) {
                 best = c;
                 max = p;
             }
         }
         return best;
     }
 }
