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
 import com.enigmastation.ml.bayes.Feature;
 import com.enigmastation.ml.bayes.SimpleClassifier;
 import com.enigmastation.ml.bayes.annotations.BayesClassifier;
 import com.enigmastation.ml.bayes.annotations.NaiveBayesClassifier;
 import com.enigmastation.ml.tokenizer.Tokenizer;
 import com.enigmastation.ml.tokenizer.impl.PorterTokenizer;
 import org.infinispan.Cache;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * This is a simple (naive) bayesian classifier.
  */
 @BayesClassifier
 @NaiveBayesClassifier
 public class SimpleClassifierImpl implements SimpleClassifier {
     protected Cache<Object, Feature> features;
     protected Cache<Object, Integer> categories;
     protected Tokenizer tokenizer = new PorterTokenizer();
     protected Map<Object, Double> thresholds = new HashMap<>();
     private static final ThreadLocal<Object> lastData = new ThreadLocal<>();
     private static final ThreadLocal<List<Object>> lastFeatures = new ThreadLocal<>();
 
     /**
      * This constructor uses the supplied ClassifierDataFactory as a backing store.
      *
      * @param factory The ClassifierDataFactory to use
      */
     SimpleClassifierImpl(ClassifierDataFactory factory) {
         features = factory.buildFeatures();
         categories = factory.buildCategories();
     }
 
     /**
      * This constructor uses the default classifier data factory (oddly enough,
      * the class name is "DefaultClassifierDataFactory".)
      */
     public SimpleClassifierImpl() {
         this(new DefaultClassifierDataFactory());
     }
 
     /**
      * This returns the best-match classification from the bayesian engine.
      * The default classification is "none", which will be returned if
      * no other classification matches.
      *
      * @param source the source corpus for the classification operation
      * @return the best-match classification
      */
     @Override
     public Object classify(Object source) {
         return classify(source, "none");
     }
 
     /**
      * This returns the best-match classification from the bayesian engine if
      * and only if the classification is more probable than the default threshold
      * for classification.
      * <p/>
      * The default threshold is normally 0.0, which means the default classification
      * will be used only if no match at all is found with this method.
      * <p/>
      * As a result, it probably shouldn't be used. You should prefer
      * classify(Object, Object, double) instead.
      *
      * @param source the source corpus for the classification operation
      * @return the best-match classification
      */
     @Override
     public Object classify(Object source, Object defaultClassification) {
         return classify(source, defaultClassification, 0.0);
     }
 
     /**
      * This returns the best-match classification from the bayesian engine if
      * and only if the classification is more probable than the strength threshold.
      * <p/>
      * If the best-match classification is less probable than the threshold,
      * the default classification is returned.
      *
      * @param source                the source corpus for the classification operation
      * @param defaultClassification the default classification if the best-match
      *                              is less strong than strength
      * @param strength              the strength threshold for the best-match
      * @return the best-match or default classification
      */
     @Override
     public Object classify(Object source, Object defaultClassification, double strength) {
         Map<Object, Double> probabilities = getClassificationProbabilities(source);
         double max = 0.0;
         Object category = null;
         for (Map.Entry<Object, Double> entry : probabilities.entrySet()) {
             if (entry.getValue() > max) {
                 max = entry.getValue();
                 category = entry.getKey();
             }
         }
         for (Map.Entry<Object, Double> entry : probabilities.entrySet()) {
             if (entry.getKey().equals(category)) {
                 continue;
             }
 
             if ((entry.getValue() * getThreshold(category)) > probabilities.get(category)) {
                 return defaultClassification;
             }
         }
         return category;
     }
 
     /**
      * This method returns the raw classification data, as a map. The map contains
      * the classification as a key, which maps to the classification's strength.
      *
      * @param source the source corpus for the classification operation
      * @return A Map where the key is the classification category and the value
      *         is the strength of that category
      */
     @Override
     public Map<Object, Double> getClassificationProbabilities(Object source) {
         Map<Object, Double> probabilities = new HashMap<>();
         for (Object category : categories()) {
             probabilities.put(category, documentProbability(source, category));
         }
         return probabilities;
     }
 
     /**
      * This method trains the classifier.
      *
      * @param source         The source text for the training operation
      * @param classification The classification for which to train
      */
     @Override
     public void train(Object source, Object classification) {
         List<Object> features = getFeatures(source);
         for (Object feature : features) {
             incrementFeature(feature, classification);
         }
         incrementCategory(classification);
     }
 
     /**
      * This method returns the tokenized features for a given source object.
      * It caches the data in a ThreadLocal, which may yield performance enhancements.
      *
      * @param source The source to tokenize
      * @return The tokenized source
      */
     protected List<Object> getFeatures(Object source) {
         List<Object> features;
         if (source.equals(lastData.get())) {
             features = lastFeatures.get();
         } else {
             features = tokenizer.tokenize(source);
             lastFeatures.set(features);
         }
         lastData.set(source);
         return features;
     }
 
     private void incrementFeature(Object feature, Object category) {
         Feature f = features.get(feature);
         if (f == null) {
             f = new Feature();
             f.setFeature(feature);
             f.setCategories(new HashMap<Object, Integer>());
         }
         features.put(feature, f);
         f.incrementCategoryCount(category);
     }
 
     private void incrementCategory(Object category) {
         Integer oldCount = categories.get(category);
         if (oldCount == null) {
             oldCount = 0;
         }
         categories.put(category, oldCount + 1);
     }
 
     // the number of times a feature has occurred in a category
    protected int featureCount(Object feature, Object category) {
         Feature f = features.get(feature);
         if (f == null) {
             return 0;
         }
         return f.getCountForCategory(category);
     }
 
     private int categoryCount(Object category) {
         if (categories.containsKey(category)) {
             return categories.get(category);
         }
         return 0;
     }
 
     private int totalCount() {
         int sum = 0;
         for (Integer i : categories.values()) {
             sum += i;
         }
         return sum;
     }
 
     protected Set<Object> categories() {
         return categories.keySet();
     }
 
     protected double featureProb(Object feature, Object category) {
         if (categoryCount(category) == 0) {
             return 0.0;
         }
         return (1.0 * featureCount(feature, category)) / categoryCount(category);
     }
 
     private double weightedProb(Object feature, Object category, double weight, double assumedProbability) {
         double basicProbability = featureProb(feature, category);
 
         double totals = 0;
         for (Object cat : categories()) {
             totals += featureCount(feature, cat);
         }
         return ((weight * assumedProbability) + (totals * basicProbability)) / (weight + totals);
     }
 
     private double weightedProb(Object feature, Object category, double weight) {
         return weightedProb(feature, category, weight, 0.5);
     }
 
     protected double weightedProb(Object feature, Object category) {
         return weightedProb(feature, category, 1.0);
     }
 
     /* naive bayes, very naive - and not what we usually need. */
     private double documentProbability(Object source, Object category) {
         List<Object> features = getFeatures(source);
         double p = 1.0;
         for (Object f : features) {
             p *= weightedProb(f, category);
         }
         return p;
     }
 
    protected double prob(Object corpus, Object category) {
         double categoryProbability = (1.0 * categoryCount(category)) / totalCount();
         double documentProbability = documentProbability(corpus, category);
         return documentProbability * categoryProbability;
     }
 
     /**
      * This sets the minimum threshold for a given category. If the classification operation
      * yields less probability than this threshold, the result is discarded.
      *
      * @param category  The category for which to set the threshold
      * @param threshold the minimum threshold
      */
     @Override
     public void setThreshold(Object category, Double threshold) {
         thresholds.put(category, threshold);
     }
 
     /**
      * This method returns the minimum threshold considered for this category.
      *
      * @param category The category for which to set the minimum strength
      * @return the strength associated with the category
      */
     @Override
     public double getThreshold(Object category) {
         if (thresholds.containsKey(category)) {
             return thresholds.get(category);
         }
         return 1.0;
     }
 }
