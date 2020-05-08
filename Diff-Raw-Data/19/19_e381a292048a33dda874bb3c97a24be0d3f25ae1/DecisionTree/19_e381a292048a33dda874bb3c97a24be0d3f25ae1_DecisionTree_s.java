 package classification;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Set;
 
 public class DecisionTree {
     private String trainingFile;
     private String testFile;
     private ArrayList<DataInstance> trainingInstances;
     private ArrayList<DataInstance> testInstances;
     private HashMap<Integer, ArrayList<DataInstance>> selectedInstances;
     private SelectionMethod selectionMethod;
     private Node root;
 
     public DecisionTree(SelectionMethod selectionMethod) {
         this.selectionMethod = selectionMethod;
     }
 
     public void setTestInstances(ArrayList<DataInstance> instances) {
         testInstances = instances;
     }
 
     public void setTrainingInstances(ArrayList<DataInstance> instances) {
         trainingInstances = instances;
     }
 
     public Node getRoot() {
         return root;
     }
 
     public void classifyFromFiles(String trainingFile, String testFile) {
         loadData(trainingFile, testFile);
         trainClassifier();
         classifyTestData();
     }
 
     private void loadData(String trainingFile, String testFile) {
         //System.out.println("Training File: " + trainingFile);
         //System.out.println("Test File: " + testFile);
         this.trainingFile = trainingFile;
         this.testFile     = testFile;
         trainingInstances = new ArrayList<DataInstance>();
         testInstances     = new ArrayList<DataInstance>();
         trainingInstances = readFile(trainingFile, trainingInstances);
         testInstances     = readFile(testFile, testInstances);
 
         // Debuging print outs
         //printInstances(trainingInstances);
         //printInstances(testInstances);
     }
 
     public void trainClassifier(ArrayList<Integer> attributeList) {
         root = generateDecisionTree(trainingInstances, attributeList);
     }
 
     public void trainClassifier(){
         root = generateDecisionTree(trainingInstances
                                   , trainingInstances.get(0).getAttributeList());
         
         /*
         System.out.println("Root label: " + root.getLabel());
         System.out.println("Root parent: " + root.getParent());
         System.out.println("Number of root children: " + root.getChildren().size());
         for (Node branch : root.getChildren()) {
             System.out.println("\t" + branch.getLabel() + " # Branches: " + branch.getChildren().size());
             for (Node child : branch.getChildren()) {
                 System.out.println("\t\t" + child.getLabel());
                 System.out.println("\t\t" + child.getChildren().size());
             }
         }
         */
     }
 
     public void classifyTestData() {
 
         HashMap<String, Integer> results = classifyTestData(root, testInstances);
         //System.out.println("Results:\n" + results);
        for (Integer r : new ArrayList<Integer>(results.values())){
            System.out.println(r);
        }
         //Utilities.evaluateModel(results);
     }
 
     private HashMap<String, Integer> classifyTestData(Node classifier
                                                     , ArrayList<DataInstance> instances){
         String result;
         String label;
         String outcome;
         HashMap<String, Integer> results = new HashMap<String, Integer>();
         results.put("true positive", 0);
         results.put("false negative", 0);
         results.put("false positive", 0);
         results.put("true negative", 0);
         for (DataInstance di : instances) {
             result = classify(classifier, di);
             label  = di.getLabel();
             //System.out.println(label + " = " + result);
             di.setClassifierResult(result);
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
 
     public String classify(Node classifier, DataInstance tuple) {
         Integer value = tuple.getAttributes().get(Integer.parseInt(classifier.getLabel()));
         for (Node branch : classifier.getChildren()){
             if (Integer.parseInt(branch.getLabel()) == value) {
                 Node child = branch.getChildren().get(0);
                 if (child.isLeaf()) {
                     return child.getLabel();
                 } else {
                     return classify(child, tuple);
                 }
             }
         }
         return "Error";
     }
 
     public ArrayList<DataInstance> readFile(String fileName, ArrayList<DataInstance> instances) {
         try {
             FileInputStream fstream = new FileInputStream(fileName);
             DataInputStream in      = new DataInputStream(fstream);
             BufferedReader br       = new BufferedReader(new InputStreamReader(in));
             String strLine;
             String[] splitLine;
             while ((strLine = br.readLine()) != null) {
                 splitLine = strLine.split("\t");
                 ArrayList<Integer> attributes = new ArrayList<Integer>();
                 for (int i = 1; i < splitLine.length; i++) {
                     attributes.add(Integer.parseInt(splitLine[i]));
                 }
                 instances.add(new DataInstance(splitLine[0], attributes));
             }
             in.close();
         } catch (Exception e) {
             System.err.println("Error: " + e.getMessage());
         }
         return instances;
     }
 
     private Node generateDecisionTree(ArrayList<DataInstance> instances
                                     , ArrayList<Integer> attributeList) {
         //System.out.println(attributeList);
         Node n = new Node();
         n.setInstances(instances);
         if (n.instancesOfSameLabel()) {
             //System.out.println("same instances");
             return n;
         }
         if (attributeList.isEmpty()) {
             //System.out.println("attribute list empty");
             n.setMajorityLabel();
             return n;
         }
 
         HashMap<String, Object> splitCriterion = 
           selectionMethod.selectAttribute(instances, attributeList);
         selectedInstances = selectionMethod.getSelectedInstances();
 
         n.setLabel(splitCriterion.get("attribute").toString());
         if (splitCriterion.get("discrete").toString() == "true" && 
             splitCriterion.get("multiway").toString() == "true" ){
             attributeList.remove((Integer) splitCriterion.get("attribute"));
         }
         Set<Integer> labels = selectedInstances.keySet();
         ArrayList<DataInstance> splitInstances;
         Node leaf;
         Node branch;
         for (Integer l : labels) {
             splitInstances = selectedInstances.get(l);
             if (splitInstances == null) {
                 splitInstances = new ArrayList<DataInstance>();
             }
             branch = new Node();
             branch.setLabel(l.toString());
             if (splitInstances.isEmpty()) {
                 leaf = new Node();
                 leaf.setInstances(instances);
                 leaf.setMajorityLabel();
                 branch.addChild(leaf);
             } else {
                 branch.addChild(generateDecisionTree(splitInstances, attributeList));
             }
             n.addChild(branch);
         }
         return n;
     }
 
     public void printInstances(ArrayList<DataInstance> instances) {
         for (DataInstance di : instances) {
             System.out.println(
                 "label: " + di.getLabel() + " attributes: " + di.getAttributes());
         }
     }
 
     public static void main(String[] args) {
         DecisionTree dt = new DecisionTree(new InformationGain());
         dt.classifyFromFiles(args[0], args[1]);
     }
 }
