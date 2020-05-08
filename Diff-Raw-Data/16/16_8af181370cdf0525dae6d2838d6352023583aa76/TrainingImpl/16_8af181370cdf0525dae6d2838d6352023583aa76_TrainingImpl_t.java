 /*
  * This file is part of ALOE.
  *
  * ALOE is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * ALOE is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with ALOE.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Copyright (c) 2012 SCCL, University of Washington (http://depts.washington.edu/sccl)
  */
 package etc.aloe.cscw2013;
 
 import etc.aloe.data.ExampleSet;
 import etc.aloe.processes.Training;
 import weka.classifiers.Classifier;
 import weka.classifiers.CostMatrix;
 import weka.classifiers.functions.SMO;
 import weka.classifiers.meta.CostSensitiveClassifier;
 import weka.core.Utils;
 
 /**
  * Performs basic training of a linear support vector machine classifier.
  *
  * @author Michael Brooks <mjbrooks@uw.edu>
  */
 public class TrainingImpl implements Training {
 
     private static final String SMO_OPTIONS = "-C 1.0 -L 0.0010 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0\"";
     private boolean buildLogisticModel = false;
     private double falsePositiveCost = 1;
     private double falseNegativeCost = 1;
     private boolean useReweighting = false;
     private boolean useCostTraining = false;
 
     public TrainingImpl() {
     }
 
     public TrainingImpl(double falsePositiveCost, double falseNegativeCost, boolean useReweighting) {
         this.falsePositiveCost = falsePositiveCost;
         this.falseNegativeCost = falseNegativeCost;
         this.useReweighting = useReweighting;
         this.useCostTraining = true;
     }
 
     public boolean isBuildLogisticModel() {
         return buildLogisticModel;
     }
 
     public void setBuildLogisticModel(boolean buildLogisticModel) {
         this.buildLogisticModel = buildLogisticModel;
     }
 
     public double getFalsePositiveCost() {
         return falsePositiveCost;
     }
 
     public void setFalsePositiveCost(double falsePositiveCost) {
         this.falsePositiveCost = falsePositiveCost;
     }
 
     public double getFalseNegativeCost() {
         return falseNegativeCost;
     }
 
     public void setFalseNegativeCost(double falseNegativeCost) {
         this.falseNegativeCost = falseNegativeCost;
     }
 
     public boolean isUseReweighting() {
         return useReweighting;
     }
 
     public void setUseReweighting(boolean useReweighting) {
         this.useReweighting = useReweighting;
     }
 
     public boolean isUseCostTraining() {
         return useCostTraining;
     }
 
     public void setUseCostTraining(boolean useCostTraining) {
         this.useCostTraining = useCostTraining;
     }
 
     @Override
     public WekaModel train(ExampleSet examples) {
         System.out.println("SMO Options: " + SMO_OPTIONS);
         SMO smo = new SMO();
         try {
             smo.setOptions(Utils.splitOptions(SMO_OPTIONS));
         } catch (Exception ex) {
             System.err.println("Unable to configure SMO.");
             System.err.println("\t" + ex.getMessage());
             return null;
         }
 
         //Build logistic models if desired
         smo.setBuildLogisticModels(isBuildLogisticModel());
 
         Classifier classifier = smo;
 
         if (useCostTraining) {
             CostSensitiveClassifier cost = new CostSensitiveClassifier();
             cost.setClassifier(smo);
             CostMatrix matrix = new CostMatrix(2);
             matrix.setElement(0, 0, 0);
             matrix.setElement(0, 1, falsePositiveCost);
             matrix.setElement(1, 0, falseNegativeCost);
             matrix.setElement(1, 1, 0);
             cost.setCostMatrix(matrix);
 
             classifier = cost;
 
             System.out.print("Wrapping SMO in CostSensitiveClassifier " + matrix.toMatlab());
 
             if (useReweighting) {
                 cost.setMinimizeExpectedCost(false);
                 System.out.println(" using re-weighting.");
             } else {
                 cost.setMinimizeExpectedCost(true);
                 System.out.println(" using min-cost criterion.");
             }
         }
 
         try {
             System.out.print("Training SMO on " + examples.size() + " examples... ");
             classifier.buildClassifier(examples.getInstances());
             System.out.println("done.");

            WekaModel model = new WekaModel(classifier);
            return model;
         } catch (Exception ex) {
             System.err.println("Unable to train SMO.");
             System.err.println("\t" + ex.getMessage());
            return null;
         }
     }
 }
