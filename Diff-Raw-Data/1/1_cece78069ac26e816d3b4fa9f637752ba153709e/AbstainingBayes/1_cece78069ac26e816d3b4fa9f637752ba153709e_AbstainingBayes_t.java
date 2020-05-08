 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Classifier;
 
 import weka.core.*;
 import weka.classifiers.*;
 import weka.filters.*;
 import java.io.*;
 import moa.classifiers.bayes.NaiveBayes;
 /**
  *
  * @author Christopher
  */
 public class AbstainingBayes {
     NaiveBayesUpdateable nb = new NaiveBayesUpdateable();
     nb.buildClassifier(structure);
     Instance current;
    
     while ((current = loader.getNextInstance(structure)) != null)
         nb.updateClassifier(current);
     
 }
