 package forests;
 
 import java.util.*;
 
 public class DecisionForest {
     private DecisionTree[] trees;
     public DecisionForest(Dataset data, int numTrees, int subsetSize) {
         this.trees = new DecisionTree[numTrees];
         for (int i = 0; i < numTrees; i++) {
             Dataset bootstrapped = data.bootstrap(data.size);
             this.trees[i] = new DecisionTree(bootstrapped, subsetSize);
         }
     }
 
     public String classify(double[] data) {
         HashMap<String, Integer> votes = new HashMap<String, Integer>();
         for (DecisionTree tree : trees) {
             String prediction = tree.classify(data);
             if (votes.containsKey(prediction)) {
                 int numVotes = votes.get(prediction);
                 votes.put(prediction, numVotes + 1);
             } else {
                 votes.put(prediction, 1);
             }
         }
 
         int max = -1;
         String plurality = null;
         for (Map.Entry<String, Integer> entry : votes.entrySet()) {
             if (entry.getValue() > max || plurality == null) {
                 max = entry.getValue();
                 plurality = entry.getKey();
             }
         }
 
         return plurality;
     }
 }
