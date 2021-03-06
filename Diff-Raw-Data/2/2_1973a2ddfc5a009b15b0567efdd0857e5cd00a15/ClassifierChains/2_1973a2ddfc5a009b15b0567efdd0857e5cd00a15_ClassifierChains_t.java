 /*
  *    This program is free software; you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation; either version 2 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program; if not, write to the Free Software
  *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 
 /*
  *    ClassifierChains.java
  *    Copyright (C) 2009-2010 Aristotle University of Thessaloniki, Thessaloniki, Greece
  */
 package mulan.classifier.transformation;
 
 import mulan.classifier.MultiLabelOutput;
 import mulan.data.DataUtils;
 import mulan.data.MultiLabelInstances;
 import weka.classifiers.AbstractClassifier;
 import weka.classifiers.Classifier;
 import weka.classifiers.meta.FilteredClassifier;
 import weka.core.Attribute;
 import weka.core.Instance;
 import weka.core.Instances;
 import weka.core.TechnicalInformation;
 import weka.core.TechnicalInformation.Field;
 import weka.core.TechnicalInformation.Type;
 import weka.filters.unsupervised.attribute.Remove;
 
 /**
  * 
  * <!-- globalinfo-start -->
  * <!-- globalinfo-end -->
  * 
  * <!-- technical-bibtex-start -->
  * <!-- technical-bibtex-end -->
  *
  * @author Eleftherios Spyromitros-Xioufis ( espyromi@csd.auth.gr )
  * @author Konstantinos Sechidis (sechidis@csd.auth.gr)
  */
 public class ClassifierChains extends TransformationBasedMultiLabelLearner {
 
     /**
      * The new chain ordering of the label indices
      */
     protected int[] newLabelIndices;
     /**
      * A boolean variable that determines whether we are taking the label indices
      * as they are or not.
      */
     boolean random;
 
     /**
      * Returns a string describing classifier.
      * @return a description suitable for
      * displaying in the explorer/experimenter gui
      */
     public String globalInfo() {
 
         return "Class implementing the Classifier Chains for Multi-label Classification algorithm." + "\n\n" + "For more information, see\n\n" + getTechnicalInformation().toString();
     }
 
     /**
      * Returns an instance of a TechnicalInformation object, containing detailed
      * information about the technical background of this class, e.g., paper
      * reference or book this class is based on.
      *
      * @return the technical information about this class
      */
     @Override
     public TechnicalInformation getTechnicalInformation() {
         TechnicalInformation result;
 
         result = new TechnicalInformation(Type.INPROCEEDINGS);
         result.setValue(Field.AUTHOR, "Read, Jesse and Pfahringer, Bernhard and Holmes, Geoff and Frank, Eibe");
         result.setValue(Field.TITLE, "Classifier Chains for Multi-label Classification");
         result.setValue(Field.VOLUME, "Proceedings of ECML/PKDD 2009");
         result.setValue(Field.YEAR, "2009");
         result.setValue(Field.PAGES, "254--269");
         result.setValue(Field.ADDRESS, "Bled, Slovenia");
         return result;
     }
 
     /**
      * Taking the new random chaim ordering of the label indices
      * @param newLabelIndices
      */
     public void setNewLabelIndices(int[] newLabelIndices) {
         this.newLabelIndices = newLabelIndices;
     }
     /**
      * The ensemble of binary relevance models. These are Weka
      * FilteredClassifier objects, where the filter corresponds to removing all
      * label apart from the one that serves as a target for the corresponding
      * model.
      */
     protected FilteredClassifier[] ensemble;
 
     /**
      * Determine whether we have a new label indices ordering or not
      * @param random
      */
     public void setRandom(boolean random) {
         this.random = random;
     }
 
     /**
      * Creates a new instance
      *
      * @param classifier  the base-level classification algorithm that will be
      * used for training each of the binary models
      */
     public ClassifierChains(Classifier classifier) {
         super(classifier);
         random = false;
     }
 
     protected void buildInternal(MultiLabelInstances train) throws Exception {
         Instances trainDataset;
         numLabels = train.getNumLabels();
         ensemble = new FilteredClassifier[numLabels];
         trainDataset = train.getDataSet();
         if (random == false) {//take label indices as they are
             newLabelIndices = this.labelIndices;
         }
 
         for (int i = 0; i < numLabels; i++) {
             ensemble[i] = new FilteredClassifier();
             ensemble[i].setClassifier(AbstractClassifier.makeCopy(baseClassifier));
 
             // Indices of attributes to remove first removes numLabels attributes
             // the numLabels - 1 attributes and so on.
             // The loop starts from the last attribute.
             int[] indicesToRemove = new int[numLabels - 1 - i];
             int counter2 = 0;
             for (int counter1 = 0; counter1 < numLabels - i - 1; counter1++) {
                 indicesToRemove[counter1] = newLabelIndices[numLabels - 1 - counter2];
                 counter2++;
             }
 
             Remove remove = new Remove();
             remove.setAttributeIndicesArray(indicesToRemove);
             remove.setInputFormat(trainDataset);
             remove.setInvertSelection(false);
             ensemble[i].setFilter(remove);
 
             trainDataset.setClassIndex(newLabelIndices[i]);
             debug("Bulding model " + (i + 1) + "/" + numLabels);
             ensemble[i].buildClassifier(trainDataset);
         }
     }
 
     protected MultiLabelOutput makePredictionInternal(Instance instance) throws Exception {
         boolean[] bipartition = new boolean[numLabels];
         double[] confidences = new double[numLabels];
 
         Instance tempInstance = DataUtils.createInstance(instance, instance.weight(), instance.toDoubleArray());
         for (int counter = 0; counter < numLabels; counter++) {
             double distribution[] = new double[2];
             try {
                distribution = ensemble[counter].distributionForInstance(tempInstance);
             } catch (Exception e) {
                 System.out.println(e);
                 return null;
             }
             int maxIndex = (distribution[0] > distribution[1]) ? 0 : 1;
 
             // Ensure correct predictions both for class values {0,1} and {1,0}
             Attribute classAttribute = ensemble[counter].getFilter().getOutputFormat().classAttribute();
             bipartition[counter] = (classAttribute.value(maxIndex).equals("1")) ? true : false;
 
             // The confidence of the label being equal to 1
             confidences[counter] = distribution[classAttribute.indexOfValue("1")];
 
             tempInstance.setValue(newLabelIndices[counter], maxIndex);
 
         }
 
         MultiLabelOutput mlo = new MultiLabelOutput(bipartition, confidences);
         return mlo;
     }
 }
