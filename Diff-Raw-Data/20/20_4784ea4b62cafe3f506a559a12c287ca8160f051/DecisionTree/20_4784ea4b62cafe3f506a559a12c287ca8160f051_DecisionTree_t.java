 package forests;
 
 import java.util.*;
 
 
 class DecisionTree {
     private int attributeSubsetSize;
     private DecisionNode root;
 
     public DecisionTree(Dataset data) {
         this(data, 10);
     }
 
     public DecisionTree(Dataset data, int subsetSize) {
         this.attributeSubsetSize = subsetSize;
         int[] allExamples = new int[data.size];
         for (int i = 0; i < allExamples.length; i++) {
             allExamples[i] = i;
         }
         this.root = learn(data, new ArrayList<Integer>(), allExamples);
     }
 
     public String classify(double[] data) {
         return root.classify(data);
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
 
     private DecisionNode learn(Dataset data, ArrayList<Integer> usedAttributes, int[] examples) {
         if (examples.length < data.size / 25 || usedAttributes.size() == data.numAttributes) {
             return new DecisionLeaf(plurality(data, examples));
         } else {
             // Choose a random subset of the attributes.
             ArrayList<Integer> attrIndices = new ArrayList<Integer>();
             for (int i = 0; i < data.numAttributes; i++) {
                 attrIndices.add(i);
             }
             Collections.shuffle(attrIndices);
 
             ArrayList<Integer> attributes = new ArrayList<Integer>();
             int attrIndex = 0;
             for (int i = 0; i < attrIndices.size(); i++) {
                 if (!usedAttributes.contains(attrIndices.get(i))) {
                     attributes.add(attrIndices.get(i));
                 }
 
                 if (attributes.size() == this.attributeSubsetSize) {
                     break;
                 }
             }
 
             // Iterate over attributes. For each one, compute the information
             // gain and the threshold for that attribute, choosing the best
             // threshold for that attribute. Then select the best attribute via
             // information gain and use that to make a decision fork.
             AttributeInfo best = null;
             for (int attribute : attributes) {
                 AttributeInfo info = computeInformationGain(data, attribute, examples);
                 if (best == null || info.informationGain > best.informationGain) {
                     best = info;
                 }
             }
 
             ArrayList<Integer> newUsed = new ArrayList<Integer>();
             newUsed.add(best.attribute);
             newUsed.addAll(usedAttributes);
 
             DecisionNode left = learn(data, newUsed, best.ltExamples);
             DecisionNode right = learn(data, newUsed, best.gtExamples);
             return new DecisionFork(best.attribute, best.threshold, left, right);
         }
     }
 
     private AttributeInfo computeInformationGain(Dataset data, int attribute, int[] examples) {
         // Compute the threshold as the mean.
         double sum = 0;
         for (int example : examples) {
             sum += data.data[example][attribute];
         }
         double threshold = sum / examples.length;
 
         // Compute the information gain for this threshold.
         ArrayList<Integer> lessThan = new ArrayList<Integer>();
         ArrayList<Integer> greaterThan = new ArrayList<Integer>();
         for (int example : examples) {
             if (data.data[example][attribute] < threshold) {
                 lessThan.add(example);
             } else {
                 greaterThan.add(example);
             }
         }
 
         double originalInformation = information(data, examples);
 
         int[] ltExamples = new int[lessThan.size()];
         for (int i = 0; i < ltExamples.length; i++) {
             ltExamples[i] = lessThan.get(i);
         }
         double lessThanInformation = information(data, ltExamples);
 
         int[] gtExamples = new int[greaterThan.size()];
         for (int i = 0; i < gtExamples.length; i++) {
             gtExamples[i] = greaterThan.get(i);
         }
         double greaterThanInformation = information(data, gtExamples);
 
         double gain = originalInformation - lessThanInformation * ltExamples.length / examples.length
                                           - greaterThanInformation * gtExamples.length / examples.length;
 
         AttributeInfo info = new AttributeInfo(attribute);
         info.informationGain = gain;
         info.threshold = threshold;
         info.ltExamples = ltExamples;
         info.gtExamples = gtExamples;
         return info;
     }
 
     private double information(Dataset data, int[] examples) {
         int[] counts = new int[data.classes.length];
         for (int example : examples) {
             String cls = data.labels[example];
 
             // Put it in the right place.
             for (int i = 0; i < data.classes.length; i++) {
                 if (data.classes[i].equals(cls)) {
                     counts[i]++;
                     break;
                 }
             }
         }
 
         double[] probabilities = new double[data.classes.length];
         for (int i = 0; i < counts.length; i++) {
             probabilities[i] = ((double) counts[i]) / examples.length;
         }
 
         double entropy = 0;
         for (double probability : probabilities) {
             entropy += probability * Math.log(probability);
         }
         return -entropy;
     }
 
     private String plurality(Dataset data, int[] examples) {
         HashMap<String, Integer> map = new HashMap<String, Integer>();
         for (int example : examples ) {
             String label = data.labels[example];
             if (map.containsKey(label)) {
                 map.put(label, map.get(label)+1);
             } else {
                 map.put(label, 1);
             }
         }
         String plurality = null;
         int maxCount = -1;
         for (String label : map.keySet()) {
             if (map.get(label) > maxCount) {
                 maxCount = map.get(label);
                 plurality = label;
             }
         }
         return plurality;
     }
 
     private abstract class DecisionNode {
         public abstract String classify(double[] datas);
     }
 
     private class DecisionFork extends DecisionNode {
         public int attribute;
         public double threshold;
         public DecisionNode left, right;
 
         public DecisionFork(int attribute, double threshold, DecisionNode left, DecisionNode right) {
             this.attribute = attribute;
             this.threshold = threshold;
             this.left = left;
             this.right = right;
         }
 
         public String classify(double[] attributeValues) {
             if (attributeValues[this.attribute] < this.threshold) {
                 return this.left.classify(attributeValues);
             } else {
                 return this.right.classify(attributeValues);
             }
         }
     }
 
     private class DecisionLeaf extends DecisionNode {
         public String label;
 
         public DecisionLeaf(String label) {
             this.label = label;
         }
 
         public String classify(double[] attributeValues) {
             return this.label;
         }
     }
 
     private class AttributeInfo {
         public int attribute;
         public double threshold;
         public double informationGain;
         public int[] ltExamples, gtExamples;
 
         public AttributeInfo(int attr) {
             this.attribute = attr;
         }
     }
 }
