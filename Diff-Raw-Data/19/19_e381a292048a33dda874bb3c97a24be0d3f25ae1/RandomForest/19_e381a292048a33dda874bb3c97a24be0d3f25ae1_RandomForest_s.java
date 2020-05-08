 package classification;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 import java.lang.Math;
 
 public class RandomForest {
 
     private ArrayList<DecisionTree> forest;
 
     public RandomForest(String trainingFile, String testFile){
         forest = new ArrayList<DecisionTree>();
         for (int i = 0; i < 2000; i++) {
             forest.add(new DecisionTree(new RandomSelection()));
         }
 
         ArrayList<DataInstance> trainingInstances = 
              forest.get(0).readFile(trainingFile, new ArrayList<DataInstance>());
 
         ArrayList<DataInstance> testInstances = 
              forest.get(0).readFile(testFile, new ArrayList<DataInstance>());
 
         for (DecisionTree dt : forest) {
             dt.setTrainingInstances(sample(trainingInstances));
             dt.trainClassifier(sampleAttrs(trainingInstances.get(0).getAttributeList()));
         }
 
         HashMap<String, Integer> results = classifyTestData(testInstances);
         //System.out.println("Results:\n" + results);
        for (Integer r : new ArrayList<Integer>(results.values())){
            System.out.println(r);
        }
 
         //Utilities.evaluateModel(results);
     }
     
     private HashMap<String, Integer> classifyTestData(ArrayList<DataInstance> testInstances) {
         String result;
         String label;
         String outcome;
         HashMap<String, Integer> results = new HashMap<String, Integer>();
         results.put("true positive", 0);
         results.put("false negative", 0);
         results.put("false positive", 0);
         results.put("true negative", 0);
 
         for (DataInstance di : testInstances) {
             result = classify(di);
             di.setClassifierResult(result);
             label  = di.getLabel();
             if (label.equals("+1")) {
                 if (result.equals("+1")) {
                     outcome = "true positive";
                 } else {
                     outcome = "false negative";
                 }
             } else {
                 if (result.equals("-1")) {
                     outcome = "true negative";
                 } else {
                     outcome = "false positive";
                 }
             }
             results.put(outcome, results.get(outcome) + 1);
         }
         return results;
     }
 
     private String classify(DataInstance di) {
         String result;
         int countPositive = 0;
         int countNegative = 0;
         for (DecisionTree dt : forest) {
             result = dt.classify(dt.getRoot(), di);
             if (result.equals("+1")) {
                 countPositive += 1;
             } else {
                 countNegative += 1;
             }
         }
 
         int max = Math.max(countPositive, countNegative);
         if (max == countPositive) {
             return "+1";
         } else {
             return "-1";
         }
     }
 
     public ArrayList<DataInstance> sample(ArrayList<DataInstance> instances){
         ArrayList<DataInstance> sampleInstances = new ArrayList<DataInstance>();
         Random r = new Random();
         for (int i = 0; i < instances.size(); i++) {
             sampleInstances.add(instances.get(r.nextInt(instances.size())));
         }
         return sampleInstances;
     }
 
     public ArrayList<Integer> sampleAttrs(ArrayList<Integer> attributeList){
         ArrayList<Integer> sampleAttrs= new ArrayList<Integer>();
         Random r = new Random();
         int pick;
         int numOfAttrs = (int) (attributeList.size() * .3);
         for (int i = 0; i < numOfAttrs; i++) {
             pick = r.nextInt(numOfAttrs);
             sampleAttrs.add(attributeList.get(pick));
             attributeList.remove(pick);
         }
         return sampleAttrs;
     }
 
     public static void main(String[] args) {
         RandomForest rf = new RandomForest(args[0], args[1]);
     }
 }
