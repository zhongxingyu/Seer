 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package classifier;
 
 import classifier.dataset.DataSet;
 import classifier.dataset.Instance;
 import util.MathHelper;
 import util.Pair;
 
 /**
  *
  * @author Nicklas
  */
 public abstract class IBuilder {
 
     private boolean redo = true;
 
     public Pair<IClassifier, DataSet> build(DataSet ds) {
         redo = true;
         IClassifier classifier = null;
         DataSet modifiedDataSet = null;
         while (redo) {
             System.out.println("Running generateHypothesis");
             classifier = generateHypothesis(ds);
             modifiedDataSet = update(classifier, ds);
             System.out.println("Weight updated");
             System.out.println("Classififer: "+classifier);
         }
         return new Pair<>(classifier, modifiedDataSet);
     }
 
     protected abstract IClassifier generateHypothesis(DataSet ds);
 
     protected DataSet update(IClassifier classifier, DataSet dataSet) {
         int misses = 0;
         int hits = 0;
        double error = Double.MIN_VALUE;
         Instance currentInstance;
         int corr = 0;
         double beta = 1.5;
         int numberOfClasses = dataSet.getClasses().length;
         for (int i = 0; i < dataSet.length(); i++) {
             currentInstance = dataSet.get(i);
             if (classifier.guessClass(currentInstance) != currentInstance.getCategory()) {
                 System.out.println("I was wrong adding weight: " + currentInstance.getWeight());
                 error += currentInstance.getWeight();
             }
         }
 
         if (error >= (numberOfClasses - 1) / (numberOfClasses * 1.0)) {
             return jiggleWeight(dataSet, beta);
         } else if (error > 0 && error < (numberOfClasses - 1) / (numberOfClasses * 1.0)) {
             for (int i = 0; i < dataSet.length(); i++) {
                 currentInstance = dataSet.get(i);
                 if (classifier.guessClass(currentInstance) == currentInstance.getCategory()) {
                     hits++;
                     currentInstance.setWeight(currentInstance.getWeight() * ((1 - error) / error) * (numberOfClasses - 1));
                     corr++;
                 }
             }
 
             double[] allWeights = dataSet.getInstanceWeights();
             double allWeightsSummed = MathHelper.sum(allWeights);
             for (Instance instance : dataSet.getInstances()) {
                 instance.setWeight(instance.getWeight() / allWeightsSummed);
             }
             System.out.println("Error: " + error + " " + ((1 - error) / error));
             classifier.setWeight(Math.log(((1 - error) / error)) * (numberOfClasses - 1));
             System.out.println("Classifier had " + corr + " out of " + dataSet.length() + " correct. Weight: " + classifier.getWeight());
         }else if (error == 0) {
             jiggleWeight(dataSet, beta);
             classifier.setWeight(10+Math.log(numberOfClasses-1));
         }
         redo = false;
         return dataSet;
     }
 
     private DataSet jiggleWeight(DataSet ds, double beta) {
         for (Instance i : ds.getInstances()) {
             i.setWeight(Math.max(0, i.getWeight() + random(-1 / (Math.pow(ds.length(), beta)), 1 / (Math.pow(ds.length(), beta)))));
         }
         double sum = MathHelper.sum(ds.getInstanceWeights());
         for (Instance i : ds.getInstances()) {
             i.setWeight(i.getWeight() / sum);
         }
         return ds;
     }
 
     private double random(double start, double end) {
         return Math.random() * (end - start) - start;
     }
 }
