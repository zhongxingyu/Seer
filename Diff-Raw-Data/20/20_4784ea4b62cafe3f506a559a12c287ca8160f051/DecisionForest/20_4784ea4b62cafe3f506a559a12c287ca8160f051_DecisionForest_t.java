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

    public double evaluate(Dataset dataset) {
        int correct = 0;
        for (int i = 0; i < dataset.size; i++) {
            String output = this.classify(dataset.data[i]);
            if (output.equals(dataset.labels[i])) {
                correct++;
            }
        }

        return ((double) correct) / dataset.size;
    }
 }
