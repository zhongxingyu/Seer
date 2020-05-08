 package aima.core.learning.learners;
 
 import aima.core.learning.framework.DataSet;
 import aima.core.learning.framework.Example;
 import aima.core.learning.framework.Learner;
 import aima.core.util.Util;
 import java.util.Arrays;
 
 /**
  * Implements ADABOOST algorithm from page 751, AIMAv3.
  *
  * @author Ravi Mohan
  * @author Andrew Brown
  */
 public class AdaBoostLearner implements Learner {
 
     private Class<? extends Learner> learnerType;
     private int learnerCount;
     private WeightedMajorityLearner ensemble;
 
     /**
      * Constructor; uses page 750 defaults
      */
     public AdaBoostLearner() {
         this.learnerType = StumpLearner.class;
         this.learnerCount = 5;
     }
 
     /**
      * Constructor
      *
      * @param learnerType
      * @param learnerCount
      */
     public AdaBoostLearner(Class<? extends Learner> learnerType, int learnerCount) {
         this.learnerType = learnerType;
         this.learnerCount = learnerCount;
     }
 
     /**
      * Train the learners according to page 751, AIMAv3.
      * <pre><code>
      * function ADABOOST(examples, L, K) returns a weighted-majority hypothesis
      *  inputs: examples, a set of N labeled examples (x_1, y_1), ..., (x_N, y_N)
      *      L, a learning algorithm
      *      K, the number of hypothesis in the ensemble
      *  local variables: w, a vector of N example weights, initially 1/N
      *      h, a vector of K hypotheses
      *      z, a vector of K hypothesis weights
      *
      *  for k = 1 to K do
      *      h[k] &lt;- L(examples, w)
      *      error &lt;- 0
      *      for j = 1 to N do
      *          if h[k](x_j) != y_j then error &lt;- error + w[j]
      *      for j = 1 to N do
      *          if h[k](x_j) = y_j then w[j] &lt;- w[j] * error/(1 - error)
      *      w &lt;- NORMALIZE(w)
      *      z[k] &lt;- log (1-error)/error
      *  return WEIGHTED-MAJORITY(h, z)
      * </code></pre>
      *
      * @param examples
      */
     public WeightedMajorityLearner adaBoost(DataSet examples, Class<? extends Learner> L, int K) {
         // initialize local variables
         int N = examples.size();
         double[] w = new double[N];
        Arrays.fill(w, 1 / N);
         Learner[] h = new Learner[K];
         try {
            Arrays.fill(h, L.getConstructor().newInstance());
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
         double[] z = new double[K];
         // for each K
         for (int k = 0; k < K; k++) {
            h[k].train(examples); // @todo h[k] <- L(examples, w); w not really implemented here; each Learner will be identical
             double error = 0;
             for (int j = 0; j < N; j++) {
                 Object x_j = h[k].predict(examples.getExample(j));
                 Object y_j = examples.getExample(j).getOutput();
                 if (!x_j.equals(y_j)) {
                     error = error + w[j];
                 }
             }
             for (int j = 0; j < N; j++) {
                 Object x_j = h[k].predict(examples.getExample(j));
                 Object y_j = examples.getExample(j).getOutput();
                 if (x_j.equals(y_j)) {
                     w[j] = w[j] * (error / (1 - error));
                 }
             }
             w = Util.normalize(w);
             z[k] = Math.log((1 - error) / error);
         }
         // return
         return new WeightedMajorityLearner(h, z);
     }
 
     /**
      * Train this learning ensemble
      *
      * @param examples
      */
     @Override
     public void train(DataSet examples) {
         this.ensemble = this.adaBoost(examples, this.learnerType, this.learnerCount);
     }
 
     /**
      * Predict a result based on the trained ensemble
      *
      * @param e
      * @return
      */
     @Override
     public <T> T predict(Example e) {
         if (this.ensemble == null) {
             throw new RuntimeException("AdaBoostLearner has not yet been trained.");
         }
         return (T) this.ensemble.predict(e);
     }
 
     /**
      * Return the accuracy of the decision tree on the specified set of examples
      *
      * @param examples
      * @return
      */
     @Override
     public int[] test(DataSet examples) {
         int[] results = new int[]{0, 0};
         for (Example e : examples) {
             if (e.getOutput().equals(this.predict(e))) {
                 results[0]++;
             } else {
                 results[1]++;
             }
         }
         return results;
     }
 }
