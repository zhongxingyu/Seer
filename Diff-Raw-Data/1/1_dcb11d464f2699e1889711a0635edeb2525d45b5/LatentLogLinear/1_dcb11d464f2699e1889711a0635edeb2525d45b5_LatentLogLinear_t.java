 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.mahout.classifier.sgd;
 
 import org.apache.mahout.classifier.OnlineLearner;
 import org.apache.mahout.common.RandomUtils;
 import org.apache.mahout.math.DenseMatrix;
 import org.apache.mahout.math.DenseVector;
 import org.apache.mahout.math.Matrix;
 import org.apache.mahout.math.Vector;
 import org.apache.mahout.math.function.UnaryFunction;
 import org.apache.mahout.math.list.IntArrayList;
 
 import java.util.Random;
 
 /**
  * Implements a Latent Factor Log-linear model as described in Dyadic Prediction Using a Latent
  * Feature Log-Linear Model by Aditya K Menon and Charles Elkan. See http://arxiv.org/abs/1006.2156
  * <p/>
  * The way that this works is that the latent factors for left-items is kept in one matrix and
  * latent factors for right-items is kept in another.  Training proceeds by considering each row of
  * left and right weights as if they were weights for a logistic regression or a feature vector.  We
  * then use the right weights as a feature to learn the left weights and vice versa.
  * <p/>
  * Regularization is done using an L1 or L2 scheme to decrease weights on each training step.
  * Nothing fancy is done in terms of per term regularization or learning rate annealing because all
  * updates are dense.
  */
 public class LatentLogLinear implements OnlineLearner {
   private final LogLinearModel left, right;
   private double mu0;
 
   public LatentLogLinear(int factors) {
     left = new LogLinearModel(factors);
     right = new LogLinearModel(factors);
   }
 
   public void train(int leftId, int rightId, int actual) {
     // chase intercept term to the left weights
 //    left.adjustBias(leftId, right.getBias(rightId));
 //    right.setBias(rightId, 1);
 
     left.train(leftId, actual, right.weights(rightId));
     right.train(rightId, actual, left.weights(leftId));
  }
   /**
    * Updates the model using a particular target variable value and a feature vector that
    * contains just a row and column id.
    *
    * @param actual   The value of the target variable.  This value should be in the half-open
    *                 interval [0..n) where n is the number of target categories.
    * @param instance The feature vector containing the row and column id's in elements 0 and 1.
    */
   @Override
   public void train(int actual, Vector instance) {
     double rowId = instance.get(0);
     double columnId = instance.get(1);
 
     if (Math.floor(rowId) != rowId) {
       throw new IllegalArgumentException("Feature 0 must be row id.  Got a float with non-zero fractional part");
     }
     if (Math.floor(columnId) != columnId) {
       throw new IllegalArgumentException("Feature 1 must be column id.  Got a float with non-zero fractional part");
     }
 
     train((int) rowId, (int) columnId, actual);
   }
 
   /**
    * Convenience method, delegates to train(int, Vector).
    *
    * @param trackingKey The tracking key for this training example.
    * @param groupKey    An optional value that allows examples to be grouped in the computation of
    *                    the update to the model.
    * @param actual      The value of the target variable.  This value should be in the half-open
    *                    interval [0..n) where n is the number of target categories.
    * @param instance    The feature vector for this example.
    */
   @Override
   public void train(long trackingKey, String groupKey, int actual, Vector instance) {
     train(actual, instance);
   }
 
   /**
    * Convenience method, delegates to train(int, Vector).
    *
    * @param trackingKey The tracking key for this training example.
    * @param actual      The value of the target variable.  This value should be in the half-open
    *                    interval [0..n) where n is the number of target categories.
    * @param instance    The feature vector for this example.
    */
   @Override
   public void train(long trackingKey, int actual, Vector instance) {
     train(actual, instance);
   }
 
   /**
    * Prepares the classifier for classification and deallocates any temporary data structures.
    * <p/>
    * An online classifier should be able to accept more training after being closed, but closing the
    * classifier may make classification more efficient.
    */
   @Override
   public void close() {
     left.close();
     right.close();
   }
 
   public LatentLogLinear learningRate(double mu0) {
     left.learningRate(mu0);
     right.learningRate(mu0);
     return this;
   }
 
   public LatentLogLinear lambda(double lambda) {
     left.lambda(lambda);
     right.lambda(lambda);
     return this;
   }
 
   public double getLambda() {
     return left.getLambda();
   }
 
   public double classifyScalar(int leftId, int rightId) {
     left.extend(leftId);
     right.extend(rightId);
     if (leftId >= left.weights.rowSize() || rightId >= left.weights.rowSize()) {
       return Double.NaN;
     } else {
       return logit(left.weights(leftId).dot(right.weights(rightId)));
     }
   }
 
   private double logit(double v) {
     return 1 / (1 + Math.exp(-v));
   }
 
   private static class LogLinearModel extends AbstractOnlineLogisticRegression {
     private final Random rand = RandomUtils.getRandom();
 
     private Matrix weights;
     private IntArrayList updates;
     private double mu0 = 1;
     private int updateCount;
 
     private LogLinearModel(int factors) {
       this.numCategories = 2;
       this.prior = new L1();
 
       int numFeatures = factors;
       updateSteps = new DenseVector(numFeatures);
       updateCounts = new DenseVector(numFeatures);
       beta = new DenseMatrix(numCategories - 1, numFeatures);
 
       weights = new BlockSparseMatrix(numFeatures);
       updates = new IntArrayList();
     }
 
     @Override
     public double perTermLearningRate(int j) {
       return 1;
     }
 
     @Override
     public double currentLearningRate() {
       return mu0 / Math.sqrt(updateCount);
     }
 
     public void train(int id, int actual, Vector features) {
       extend(id);
      
       updateCount = updates.getQuick(id) + 1;
       updates.setQuick(id, updateCount);
 
       if (id >= weights.rowSize()) {
         weights.setQuick(id, 0, 0);
       }
 
       beta = weights.viewPart(id, 1, 0, weights.columnSize());
       train(actual, features);
     }
 
     @Override
     public void regularize(Vector instance) {
       beta.assign(new UnaryFunction() {
         @Override
         public double apply(double arg1) {
           double newValue = arg1 - getLambda() * currentLearningRate() * Math.signum(arg1);
           if (newValue * arg1 < 0) {
             return 0;
           } else {
             return newValue;
           }
         }
       });
     }
 
     public Vector weights(int id) {
       extend(id);
       return weights.getRow(id);
     }
 
     private void initializeWeights(int id) {
       if (updates.get(id) < 0) {
         weights.getRow(id).assign(new UnaryFunction() {
           @Override
           public double apply(double arg1) {
             return rand.nextGaussian();
           }
         });
         updates.set(id, 0);
       }
     }
 
     private void extendUpdateCounts(int id) {
       if (id >= updates.size()) {
         while (id >= updates.size()) {
           // this signals that the corresponding row hasn't been initialized
           updates.add(-1);
         }
       }
     }
 
     public void setBias(int id, double value) {
       extend(id);
       weights.setQuick(id, 0, value);
     }
 
     public double getBias(int id) {
       extend(id);
       return weights.getQuick(id, 0);
     }
 
     public void adjustBias(int id, double adjustment) {
       double newValue = getBias(id) * adjustment;
       if (Double.isNaN(newValue) || Double.isInfinite(newValue)) {
         System.out.printf("bad bias\n");
       }
       setBias(id, newValue);
     }
 
     public void learningRate(double mu0) {
       this.mu0 = mu0;
     }
 
     public void extend(int id) {
       extendUpdateCounts(id);
       initializeWeights(id);
     }
   }
 }
