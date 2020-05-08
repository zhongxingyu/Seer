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
  *    Id3.java
  *    Copyright (C) 1999 University of Waikato, Hamilton, New Zealand
  *
  */
 package weka.classifiers.trees;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.Vector;
 
 import weka.classifiers.AbstractClassifier;
 import weka.core.AdditionalMeasureProducer;
 import weka.core.Attribute;
 import weka.core.Capabilities;
 import weka.core.Instance;
 import weka.core.Instances;
 import weka.core.NoSupportForMissingValuesException;
 import weka.core.Option;
 import weka.core.OptionHandler;
 import weka.core.RevisionUtils;
 import weka.core.SelectedTag;
 import weka.core.Tag;
 import weka.core.TechnicalInformation;
 import weka.core.TechnicalInformationHandler;
 import weka.core.Utils;
 import weka.core.Capabilities.Capability;
 import weka.core.TechnicalInformation.Field;
 import weka.core.TechnicalInformation.Type;
 
 /**
 <!-- globalinfo-start -->
  * Laborationsmall för att skapa ett träd som både kan använda GainRatio och GiniIndex 
  * utifrån parameterval. Det skall även gå att välja mellan att ha binära eller multipla 
  * splits. Vidare skall algoritmen även ha stöd för att använda numeriska variabler. 
  * <br/>
  * Följande är krav på labben:
  * Implementering av GainRatio och GiniIndex
  * Binära och multipla splits
  * Numeriska variabler
  * En korrekt utskrift av trädet
  * Möjlighet att specificera minsta antalet instanser som tillåts i lövnoderna 
  * Debugläge, som skriver ut info om alla viktiga händelser som sker
  * <br/>
  * Extra funktionalitet som kan implementeras:
  * Möjlighet att pruna trädet
  * En översättning av trädet till en java-klass (implementering av interfacet Sourcable)
  * <p/>
 <!-- globalinfo-end -->
  *
  * <p/>
 <!-- technical-bibtex-end -->
  *
 <!-- options-start -->
  * Valid options are: <p/>
  * 
  * <pre> -M &lt;minimum number of instances&gt;
  *  Set minimum number of instances per leaf.
  *  (default 2)</pre>
  * 
  * <pre> -B
  *  Use binary splits only.</pre>
  * 
  * <pre> -Q &lt;seed&gt;
  *  Seed for random data shuffling (default 1).</pre>
  *  
  * <pre> -D
  *  If set, classifier is run in debug mode and
  *  may output additional info to the console</pre>
  * 
 <!-- options-end -->
  *
  * @author Johan, Daniel, Fredrik, Andreas
  * @version $Revision:  $
  */
 public class Grupp3Labb1
         extends AbstractClassifier
         implements TechnicalInformationHandler, OptionHandler, AdditionalMeasureProducer {//, Sourcable {
 
     /** for serialization */
     @SuppressWarnings("UnusedDeclaration")
     static final long serialVersionUID = -2693678647096322561L;
     /** The node's successors. */
     private Grupp3Labb1[] m_Successors;
     /** Attribute used for splitting. */
     private Attribute m_Attribute;
     /** Instance **/
     private Instances m_Data;
     /** Class value if node is leaf. */
     private double m_ClassValue;
     /** Class distribution if node is leaf. */
     private double[] m_Distribution;
     /** Class attribute of dataset. */
     private Attribute m_ClassAttribute;
     /** Minimum number of instances in leafs*/
     private int m_minNumObj = 2;
     /** The minimum leaf size */
     private double m_MinimumLeafSize;
     /** The majority class */
     private static int m_MajorityClass;
     /** The spliting method */
     public static Tag[] TAGS_SplitMethod = {
         new Tag(0, "GainRatio"),
         new Tag(1, "GiniIndex")
     };
     public int m_SplitMethod = 0;
     /** Binary splits on nominal attributes? */
     private boolean m_UseBinarySplits;
 
     private double[] previousGinis = new double[10];
     private double[] splitIndex;
 
     /**
      * Returns a string describing the classifier.
      * @return a description suitable for the GUI.
      */
     public String globalInfo() {
         return "Class for constructing an unpruned decision tree based on the ID3 "
                 + "algorithm. Can only deal with nominal attributes. No missing values "
                 + "allowed. Empty leaves may result in unclassified instances. For more "
                 + "information see: \n\n"
                 + getTechnicalInformation().toString();
     }
 
     /**
      * Returns an instance of a TechnicalInformation object, containing 
      * detailed information about the technical background of this class,
      * e.g., paper reference or book this class is based on.
      * 
      * @return the technical information about this class
      */
     public TechnicalInformation getTechnicalInformation() {
         TechnicalInformation result;
 
         result = new TechnicalInformation(Type.UNPUBLISHED);
         result.setValue(Field.AUTHOR, "A. Westberg");
         result.setValue(Field.AUTHOR, "D. Jansson");
         result.setValue(Field.AUTHOR, "F. Törnvall");
         result.setValue(Field.AUTHOR, "J. Dahlberg");
         result.setValue(Field.YEAR, "2013");
         result.setValue(Field.TYPE, "Laboration");
         result.setValue(Field.SCHOOL, "University of Borås");
         result.setValue(Field.NOTE, "Egna kommentarer");
         result.setValue(Field.NUMBER, "3");
 
         return result;
     }
 
     /**
      * Returns default capabilities of the classifier.
      *
      * @return      the capabilities of this classifier
      */
     public Capabilities getCapabilities() {
         Capabilities result = super.getCapabilities();
         result.disableAll();
 
         // attributes
         result.enable(Capability.NOMINAL_ATTRIBUTES);
         result.enable(Capability.NUMERIC_ATTRIBUTES);
         result.enable(Capability.MISSING_VALUES);
 
         // class
         result.enable(Capability.NOMINAL_CLASS);
         result.enable(Capability.MISSING_CLASS_VALUES);
 
         // instances
         result.setMinimumNumberInstances(0);
 
         return result;
     }
 
     /**
      * Builds Id3 decision tree classifier.
      *
      * @param data the training data
      * @exception Exception if classifier can't be built successfully
      */
     public void buildClassifier(Instances data) throws Exception {
         // can classifier handle the data?
         getCapabilities().testWithFail(data);
 
         // remove instances with missing class
         data = new Instances(data);
         data.deleteWithMissingClass();
 
         m_MajorityClass = getMajorityClass(data);
 
         makeTree(data);
     }
 
     private int getMajorityClass(Instances data) {
         int[] classCounts = new int[data.numClasses()];
         for (int i = 0; i < data.numInstances(); i++) {
             classCounts[(int) data.instance(i).classValue()]++;
         }
         return Utils.maxIndex(classCounts);
     }
 
     /**
      * Method for building an Id3 tree.
      *
      * @param data the training data
      * @exception Exception if decision tree can't be built successfully
      */
     private void makeTree(Instances data) throws Exception {
        splitIndex = new double[data.numAttributes()-1];

         // Check if no instances have reached this node.
         if (data.numInstances() == 0) {
             m_Attribute = null;
             m_ClassValue = Utils.missingValue();
             m_Distribution = new double[data.numClasses()];
             return;
         }
 
         printDebugMessage("\n--------------NEW NODE--------------");
         //printDebugMessage("\nNumber of instances in: " + data.numInstances());
 
         // Compute attribute with maximum information gain.
         double[] bestAttr = new double[data.numAttributes()];
         Enumeration attEnum = data.enumerateAttributes();
 
         if (m_SplitMethod == 0) {
             printDebugMessage("\nGainRatio for Atributes:");
         } else {
             printDebugMessage("\nGiniIndex for Atributes:");
         }
 
         while (attEnum.hasMoreElements()) {
             Attribute att = (Attribute) attEnum.nextElement();
             bestAttr[att.index()] = computeAttributeValue(data, att);
             printDebugMessage("\n" + att.index() + ": " + bestAttr[att.index()]);
         }
         m_Attribute = data.attribute(Utils.maxIndex(bestAttr));
 
 
         if (m_SplitMethod == 0) {
             m_Attribute = data.attribute(Utils.maxIndex(bestAttr));
         } else {
             m_Attribute = data.attribute(Utils.minIndex(bestAttr));
         }
         printDebugMessage("\nChoosing: " + m_Attribute.index() + " (" + m_Attribute.name() + ")\n");
 
         // Make leaf if information gain is zero. 
         // Otherwise create successors.
         if (Utils.eq(bestAttr[m_Attribute.index()], 0)) {
             makeLeaf(data);
         } else {
             Instances[] splitData = getSplitData(data, m_Attribute);
             m_Successors = new Grupp3Labb1[m_Attribute.numValues()];
             for (int j = 0; j < m_Attribute.numValues(); j++) {
                 m_Successors[j] = new Grupp3Labb1();
                 m_Successors[j].makeTree(splitData[j]);
                 m_Successors[j].setMinimumLeafSize(m_MinimumLeafSize);
 
             }
         }
     }
 
     /**
      * Create leaf
      * - Refactored from makeTree
      *
      * @param data the leaf instance
      */
     private void makeLeaf(Instances data) {
         m_Attribute = null;
         m_Data = data;
         m_Distribution = new double[data.numClasses()];
 
         Enumeration instEnum = data.enumerateInstances();
         while (instEnum.hasMoreElements()) {
             Instance inst = (Instance) instEnum.nextElement();
             m_Distribution[(int) inst.classValue()]++;
         }
 
         Utils.normalize(m_Distribution);
         m_ClassValue = Utils.maxIndex(m_Distribution);
         m_ClassAttribute = data.classAttribute();
     }
 
     /**
      * Classifies a given test instance using the decision tree.
      *
      * @param instance the instance to be classified
      * @return the classification
      * @throws NoSupportForMissingValuesException if instance has missing values
      */
     public double classifyInstance(Instance instance)
             throws NoSupportForMissingValuesException {
 
         if (instance.hasMissingValue()) {
             return handleMissingValue(instance);
         }
 
         if (m_Attribute == null) {
             return m_ClassValue;
         } else {
             return m_Successors[(int) instance.value(m_Attribute)].classifyInstance(instance);
         }
     }
 
     /**
      * Computes class distribution for instance using decision tree.
      *
      * @param instance the instance for which distribution is to be computed
      * @return the class distribution for the given instance
      * @throws NoSupportForMissingValuesException if instance has missing values
      */
     public double[] distributionForInstance(Instance instance)
             throws NoSupportForMissingValuesException {
         if (instance.hasMissingValue()) {
             instance.setClassValue(handleMissingValue(instance));
         }
         if (m_Attribute == null) {
             return m_Distribution;
         } else {
             return m_Successors[(int) instance.value(m_Attribute)].distributionForInstance(instance);
         }
     }
 
     /**
      * Handle missing value
      * @param instance current instance
      * @return the value to fill the missing
      */
     private double handleMissingValue(Instance instance) {
         //return m_MajorityClass; // Could be used
         return getMostCommonValue(instance);
     }
 
     /**
      * @param instance current instance
      * @return the most common value
      */
     private double getMostCommonValue(Instance instance) {
         double mostCommonValue = 0;
         int count = 0;
 
         for(int i = 0; i < instance.numAttributes(); i++) {
             int currentCount = 0;
             double currentValue = instance.value(i);
             double comparedValue;
 
             for(int j = 0; j < instance.numAttributes(); j++){
                 comparedValue = instance.value(j);
                 if(currentValue == comparedValue)
                     currentCount++;
             }
             if(currentCount > count){
                 count = currentCount;
                 mostCommonValue =  currentValue;
             }
         }
 
         /*for(int a  = 0; a < instance.numAttributes(); a++){
             printDebugMessage(instance.value(a) + "");
         }
         printDebugMessage("m_MajorityClass:");
         printDebugMessage(m_MajorityClass + "");
         printDebugMessage("mostCommonValue:");
         printDebugMessage(mostCommonValue + "");*/
 
         return mostCommonValue;
     }
 
     /**
      * TODO: Work in progress
      * @param data an instance array
      * @return true if most of the values are the same
      */
     boolean isMostOfTheValuesSame(Instances[] data){
         ArrayList<String> instances = new ArrayList<String>();
 
         for (Instances aData : data) {
             for (int j = 0; j < aData.numAttributes(); j++) {
                 for (int k = 0; k < aData.attribute(j).numValues(); k++) {
                     instances.add(aData.attribute(k).value(k));
                 }
             }
         }
         return false;
     }
 
     /**
      * TODO: Comment
      *
      * @param data the data for which info gain is to be computed
      * @param att the attribute
      * @return TODO: comment
      * @throws Exception if computation fails
      */
     private double computeAttributeValue(Instances data, Attribute att) throws Exception {
         switch (m_SplitMethod) {
             case 0: //GainRatio
                 double infoGain = computeInfoGain(data, att);
                 double splitInfo = computeSplitInfo(data, att);
                 printDebugMessage("InfoGain: " + infoGain);
                 printDebugMessage("SplitInfo: " + splitInfo);
                 printDebugMessage("GainRatio: " + infoGain/splitInfo);
                 return infoGain / splitInfo;
             case 1: //GiniIndex
                 double giniIndex = computeGiniIndex(data, att);
                 addToPreviousGinis(giniIndex);
                 printDebugMessage("GiniIndex: " + giniIndex);
                 return giniIndex;
         }
         throw new Exception("ComputeAttributeValue: Unreachable code");
     }
 
     private void addToPreviousGinis(double gini){
         int len = previousGinis.length;
         if(previousGinis[0] == 0.0){
 
         }
 
     }
 
     /**
      * TODO: Comment
      *
      * @param data the data for which gini index is to be computed
      * @param att the attribute
      * @return TODO: double gini index value
      */
     private double computeGiniIndex(Instances data, Attribute att){
     	Instances[] splitData = getSplitData(data, att);
     	double gini = 0;
 
         for (Instances aSplitData : splitData) {
             //for each node..
             double nodeResult = 1.0;
             double[] classCount = new double[data.numClasses()];
             for (int init = 0; init < data.numClasses(); init++) {
                 classCount[init] = 0;
             }
 
             //count instances in classes.
             for (int i = 0; i < data.numInstances(); i++) {
                 //cumpute how frequent a class is.
                 classCount[(int) data.instance(i).classValue()]++;
             }
 
             for (double aClassCount : classCount) {
                 double p = aClassCount / data.numInstances();
                 //for each class result - P(C1)^2.. loop and do P(C2)^2.. and so on
                 nodeResult = nodeResult - (p * p);
             }
 
             gini += ((double) splitData.length / (double) data.size()) * nodeResult;
         }
     	return gini;
     }
 
     /**
      * TODO: Comment
      *
      * @param data the data for which info gain is to be computed
      * @param att the attribute
      * @return TODO: splitInfo value
      */
     private double computeSplitInfo(Instances data, Attribute att){
     	Instances[] splitData = getSplitData(data, att);
     	double splitInfo = 0.0;
         for (Instances aSplitData : splitData) {
             splitInfo -= (aSplitData.numInstances() / data.numInstances()) * Math.log(aSplitData.numInstances() / data.numInstances());
         }
     	return splitInfo;
     }
 
     /**
      * Computes information gain for an attribute.
      *
      * @param data the data for which info gain is to be computed
      * @param att the attribute
      * @return the information gain for the given attribute and data
      * @throws Exception if computation fails
      */
     private double computeInfoGain(Instances data, Attribute att)
             throws Exception {
         double infoGain = computeEntropy(data);
         Instances[] splitData = getSplitData(data, att);
 
         if(splitData != null) {
             for (int j = 0; j < att.numValues(); j++) {
                 if (splitData[j].numInstances() > 0) {
                     infoGain -= ((double) splitData[j].numInstances()
                             / (double) data.numInstances())
                             * computeEntropy(splitData[j]);
                 }
             }
         }
         return infoGain;
     }
 
     /**
      * Computes the entropy of a dataset.
      * 
      * @param data the data for which entropy is to be computed
      * @return the entropy of the data's class distribution
      * @throws Exception if computation fails
      */
     private double computeEntropy(Instances data) throws Exception {
         double[] classCounts = new double[data.numClasses()];
         Enumeration instEnum = data.enumerateInstances();
         while (instEnum.hasMoreElements()) {
             Instance inst = (Instance) instEnum.nextElement();
             classCounts[(int) inst.classValue()]++;
         }
         double entropy = 0;
         for (int j = 0; j < data.numClasses(); j++) {
             if (classCounts[j] > 0) {
                 entropy -= classCounts[j] * Utils.log2(classCounts[j]);
             }
         }
         entropy /= (double) data.numInstances();
         return entropy + Utils.log2(data.numInstances());
     }
 
     /**
      * @param data instances
      * @return splitData according to the current setting and type of attribute
      */
     private Instances[] getSplitData(Instances data) {
         return getSplitData(data, m_Attribute);
     }
 
     /**
      * @param data instances
      * @param att attribute
      * @return splitData according to the current setting and type of attribute
      */
     private Instances[] getSplitData(Instances data, Attribute att) {
         if(m_UseBinarySplits)
             return binarySplitData(data, att);
         else
             return splitData(data, att);
     }
 
     /**
      * Splits a dataset binarily, according to the values of a nominal attribute.
      * TODO: Work in progress - Johan
      *
      * @param data the data that is to be split
      * @param att the attribute to be used for splitting
      * @return Best split produced
      */
     private Instances[] binarySplitData(Instances data, Attribute att) {
         // Don't try to split less than 2 instances
         if(data.numInstances() < 2) {
             printDebugMessage("You can't try to split less than 2 instances (facepalm)");
             return null;
         }
 
         if(att.isNominal())
             return binarySplitDataNominal(data, att);
         else if(att.isNumeric())
             return binarySplitDataNumeric(data, att);
         else
             return null;
     }
 
     /**
      * @param data the data that is to be split
      * @param att the attribute to be used for splitting
      * @return Best split produced
      */
     private Instances[] binarySplitDataNominal(Instances data, Attribute att) {
         throw new UnsupportedOperationException("Not yet implemented");
     }
 
     /**
      * @param data the data that is to be split
      * @param att the attribute to be used for splitting
      * @return Best split produced
      */
     private Instances[] binarySplitDataNumeric(Instances data, Attribute att) {
         double maxValue = Double.NEGATIVE_INFINITY, minValue = Double.POSITIVE_INFINITY;
         Instance inst;
 
         Instances[] splitData = new Instances[2];
         splitData[0] = data.stringFreeStructure();
         splitData[1] = data.stringFreeStructure();
 
         Enumeration instEnum = data.enumerateInstances();
         while (instEnum.hasMoreElements()) {
             inst = (Instance) instEnum.nextElement();
             double value = inst.value(att);
 
             if(maxValue < value)
                 maxValue = value;
             if(minValue > value)
                 minValue = value;
         }
 
         double diff = maxValue - minValue;
         double splitValue = diff/2;
 
         double value = minValue+splitValue;
         for (int i = 0; i < 2; i++) {
             splitIndex[i] = value;
             value += splitValue;
         }
 
         instEnum = data.enumerateInstances();
         while (instEnum.hasMoreElements()) {
             inst = (Instance) instEnum.nextElement();
 
             if(!inst.hasMissingValue()) {
                 double bound = minValue + splitValue;
 
                 if(bound < inst.value(att)) {
                     splitData[1].add(inst);
                 } else {
                     splitData[0].add(inst);
                 }
             }
         }
 
         for (Instances aSplitData : splitData) {
             aSplitData.compactify();
         }
         return splitData;
     }
 
     /**
      * Splits a dataset according to the values of a nominal attribute.
      *
      * @param data the data which is to be split
      * @param att the attribute to be used for splitting
      * @return the sets of instances produced by the split
      */
     private Instances[] splitData(Instances data, Attribute att) {
         if(att.isNominal())
             return splitDataNominal(data, att);
         else if(att.isNumeric())
             return splitDataNumeric(data, att);
         else
             return null;
     }
 
     /**
      * @param data the data which is to be split
      * @param att the attribute to be used for splitting
      * @return the sets of instances produced by the split
      */
     private Instances[] splitDataNominal(Instances data, Attribute att) {
         Instances[] splitData = new Instances[att.numValues()];
         for (int j = 0; j < att.numValues(); j++) {
             splitData[j] = new Instances(data, data.numInstances());
         }
 
         Enumeration instEnum = data.enumerateInstances();
         while (instEnum.hasMoreElements()) {
             Instance inst = (Instance) instEnum.nextElement();
             splitData[(int) inst.value(att)].add(inst);
         }
 
         for (Instances aSplitData : splitData) {
             aSplitData.compactify();
         }
         return splitData;
     }
 
     /**
      * @param data the data which is to be split
      * @param att the attribute to be used for splitting
      * @return the sets of instances produced by the split
      */
     private Instances[] splitDataNumeric(Instances data, Attribute att) {
         throw new UnsupportedOperationException("Not yet implemented");
     }
 
     /**
      * Returns an enumeration describing the available options.
      * <pre> -M &lt;minimum number of instances&gt;
      *  Set minimum number of instances per leaf.
      *  (default 2)</pre>
      * 
      * <pre> -B
      *  Use binary splits only.</pre>
      *
      * @return an enumeration of all the available options.
      */
     @SuppressWarnings("unchecked")
     public Enumeration listOptions() {
         Vector<Option> newVector = new Vector<Option>();
 
         newVector.addElement(new Option(
                 MinimumLeafSizeTipText(),
                 "M", 1, "-M < default 2 >"));
         newVector.addElement(new Option(
                 UseBinarySplitsTipText(),
                 "B", 0, "-B"));
         newVector.addElement(new Option(
                 SplitMethodTipText(),
                 "S", 1,
                 "-S < spliting method, [GainRatio, GiniIndex] >"));
         Enumeration<Option> enu = super.listOptions();
         while (enu.hasMoreElements()) {
             newVector.addElement(enu.nextElement());
         }
 
         return newVector.elements();
     }
 
     /**
      * Parses a given list of options. Valid options are:<p>
      *
      * -D  <br>
      * If set, classifier is run in debug mode and
      * may output additional info to the console.<p>
      *
      * @param options the list of options as an array of strings
      * @exception Exception if an option is not supported
      */
     public void setOptions(String[] options) throws Exception {
         String tmp = Utils.getOption("M", options);
         if (tmp.length() != 0) {
             setMinimumLeafSize(Integer.parseInt(tmp));
         }
 
         setUseBinarySplits(Utils.getFlag("B", options));
 
         tmp = Utils.getOption("S", options);
         if (tmp.length() != 0) {
             setSplitMethod(
                     new SelectedTag(Integer.parseInt(tmp), TAGS_SplitMethod));
         } else {
             setSplitMethod(
                     new SelectedTag(m_SplitMethod, TAGS_SplitMethod));
         }
     }
 
     /**
      * Gets the current settings of the Classifier.
      *
      * @return an array of strings suitable for passing to setOptions
      */
     public String[] getOptions() {
         Vector<String> result = new Vector<String>();
         String[] options;
 
         result.add("-M");
         result.add("" + getMinimumLeafSize());
         if (getUseBinarySplits()) {
             result.add("-B");
         }
         result.add("-S");
         result.add("" + getSplitMethod());
         options = super.getOptions();
         Collections.addAll(result, options);
 
         String[] array = new String[result.size()];
         for (int i = 0; i < result.size(); i++) {
             array[i] = result.elementAt(i);
         }
 
         return array; // (String[]) result.toArray(new String[result.size()]);
     }
 
     /**
      * Returns the tip text for this property
      * @return tip text for this property suitable for
      * displaying in the explorer/experimenter gui
      */
     public String MinimumLeafSizeTipText() {
         return "Set minimum number of instances per leaf.";
     }
 
     /**
      * Get the value of this property.
      *
      * @return Value of this property.
      */
     public double getMinimumLeafSize() {
         return m_MinimumLeafSize;
     }
 
     /**
      * Set the value of this property.
      *
      * @param value Value to assign to this property.
      */
     public void setMinimumLeafSize(double value) {
         m_MinimumLeafSize = value;
     }
 
     /**
      * Returns the tip text for this property
      * @return tip text for this property suitable for
      * displaying in the explorer/experimenter gui
      */
     public String UseBinarySplitsTipText() {
         return "Use binary or multiple splits";
     }
 
     /**
      * Get the value of this property.
      *
      * @return Value of this property.
      */
     public boolean getUseBinarySplits() {
         return m_UseBinarySplits;
     }
 
     /**
      * Set the value of this property.
      *
      * @param value Value to assign to this property.
      */
     public void setUseBinarySplits(boolean value) {
         m_UseBinarySplits = value;
     }
 
     /**
      * Returns the tip text for this property
      * @return tip text for this property suitable for
      * displaying in the explorer/experimenter gui
      */
     public String SplitMethodTipText() {
         return "Select the method used to determine best split (GainRatio / GiniIndex)";
     }
 
     /**
      * Get the value of this property.
      *
      * @return Value of this property.
      */
     public SelectedTag getSplitMethod() {
         return new SelectedTag(m_SplitMethod, TAGS_SplitMethod);
     }
 
     /**
      * Set the value of this property.
      *
      * @param value Value to assign to this property.
      */
     public void setSplitMethod(SelectedTag value) {
         if (value.getTags() == TAGS_SplitMethod) {
             m_SplitMethod = value.getSelectedTag().getID();
         }
     }
 
     /**
      * Prints the decision tree using the private toString method from below.
      *
      * @return a textual description of the classifier
      */
     public String toString() {
         if ((m_Distribution == null) && (m_Successors == null)) {
             return "Grupp3Labb1: No model built yet.";
         }
 
         return "Grupp3Labb1\n------------------\n" + toString(0)
                 + "\n\nSize of the tree: " + (int)measureTreeSize() + "\n\n"
                 + "Number of leaves: " + (int)measureNumLeaves();
     }
 
     /**
      * Outputs a tree at a certain level.
      *
      * @param level the level at which the tree is to be printed
      * @return the tree as string at the given level
      */
     private String toString(int level) {
         StringBuilder text = new StringBuilder();
 
         if (m_Attribute == null) { // isLeaf
             if (Utils.isMissingValue(m_ClassValue)) {
                 text.append(": null");
             } else {
                 text.append(": ").append(m_ClassAttribute.value((int) m_ClassValue)).append(leafInfo());
             }
         } else {
             for (int j = 0; j < m_Attribute.numValues(); j++) {
                 text.append("\n");
                 for (int i = 0; i < level; i++) {
                     text.append("|  ");
                 }
 
                 text.append(m_Attribute.name()).append(" = ").append(m_Attribute.value(j));
 
                 if(m_Successors[j] != null)
                     text.append(m_Successors[j].toString(level + 1));
             }
         }
         return text.toString();
     }
 
     /**
      * Print info about the leaf
      *
      * @return leafInfo
      */
     public String leafInfo() {
         Enumeration instances = m_Data.enumerateInstances();
         double sum = 0, error = 0;
 
         while(instances.hasMoreElements()) {
             Instance inst = (Instance) instances.nextElement();
             sum++;
             //printDebugMessage("\nInstances ca: "+ inst.value(inst.classAttribute()));
             //printDebugMessage("Correct ca: "+ m_ClassValue);
 
             if(inst.value(inst.classAttribute()) != m_ClassValue)
                 error++;
         }
 
         String info;
         if(error == 0)
             info = " (" + sum + ")";
         else
             info = " (" + sum + "/" + error + ")";
 
         return info;
     }
 
     /**
      * Adds this tree recursively to the buffer.
      * 
      * @param id          the unqiue id for the method
      * @param buffer      the buffer to add the source code to
      * @return            the last ID being used
      * @throws Exception  if something goes wrong
      */
     protected int toSource(int id, StringBuffer buffer) throws Exception {
         int result;
         int i;
         int newID;
         StringBuffer[] subBuffers;
 
         buffer.append("\n");
         buffer.append("  protected static double node").append(id).append("(Object[] i) {\n");
 
         // leaf?
         if (m_Attribute == null) {
             result = id;
             if (Double.isNaN(m_ClassValue)) {
                 buffer.append("    return Double.NaN;");
             } else {
                 buffer.append("    return ").append(m_ClassValue).append(";");
             }
             if (m_ClassAttribute != null) {
                 buffer.append(" // ").append(m_ClassAttribute.value((int) m_ClassValue));
             }
             buffer.append("\n");
             buffer.append("  }\n");
         } else {
             buffer.append("    // ").append(m_Attribute.name()).append("\n");
 
             // subtree calls
             subBuffers = new StringBuffer[m_Attribute.numValues()];
             newID = id;
             for (i = 0; i < m_Attribute.numValues(); i++) {
                 newID++;
 
                 buffer.append("    ");
                 if (i > 0) {
                     buffer.append("else ");
                 }
                 buffer.append("if (((String) i[").append(m_Attribute.index()).append("]).equals(\"").append(m_Attribute.value(i)).append("\"))\n");
                 buffer.append("      return node").append(newID).append("(i);\n");
 
                 subBuffers[i] = new StringBuffer();
                 newID = m_Successors[i].toSource(newID, subBuffers[i]);
             }
             buffer.append("    else\n");
             buffer.append("      throw new IllegalArgumentException(\"Value '\" + i[").append(m_Attribute.index()).append("] + \"' is not allowed!\");\n");
             buffer.append("  }\n");
 
             // output subtree code
             for (i = 0; i < m_Attribute.numValues(); i++) {
                 buffer.append(subBuffers[i].toString());
             }
             //subBuffers = null;
 
             result = newID;
         }
 
         return result;
     }
 
     /**
      * Returns a string that describes the classifier as source. The
      * classifier will be contained in a class with the given name (there may
      * be auxiliary classes),
      * and will contain a method with the signature:
      * <pre><code>
      * public static double classify(Object[] i);
      * </code></pre>
      * where the array <code>i</code> contains elements that are either
      * Double, String, with missing values represented as null. The generated
      * code is public domain and comes with no warranty. <br/>
      * Note: works only if class attribute is the last attribute in the dataset.
      *
      * @param className the name that should be given to the source class.
      * @return the object source described by a string
      * @throws Exception if the source can't be computed
      */
     public String toSource(String className) throws Exception {
         StringBuffer result;
         int id;
 
         result = new StringBuffer();
 
         result.append("class ").append(className).append(" {\n");
         result.append("  public static double classify(Object[] i) {\n");
         id = 0;
         result.append("    return node").append(id).append("(i);\n");
         result.append("  }\n");
         toSource(id, result);
         result.append("}\n");
 
         return result.toString();
     }
 
     /**
      * Print message if debug
      * @param msg the message to be printed
      */
     private void printDebugMessage(String msg) {
         if (m_Debug) {
             System.out.println("\n" + msg);
         }
     }
 
     /**
      * Returns the revision string.
      * 
      * @return		the revision
      */
     public String getRevision() {
         return RevisionUtils.extract("$Revision: 1 $");
     }
 
     /**
      * Main method.
      *
      * @param args the options for the classifier
      */
     public static void main(String[] args) {
         runClassifier(new Grupp3Labb1(), args);
     }
 
     /**
      * Returns an enumeration of the additional measure names
      * @return an enumeration of the measure names
      */
     public Enumeration enumerateMeasures() {
         Vector<String> newVector = new Vector<String>(3);
         newVector.addElement("measureTreeSize");
         newVector.addElement("measureNumLeaves");
         newVector.addElement("measureNumRules");
         return newVector.elements();
     }
 
     /**
      * Returns the value of the named measure
      * @param additionalMeasureName the name of the measure to query for its value
      * @return the value of the named measure
      * @throws IllegalArgumentException if the named measure is not supported
      */
     public double getMeasure(String additionalMeasureName) {
         if (additionalMeasureName.compareToIgnoreCase("measureNumRules") == 0) {
             return measureNumRules();
         } else if (additionalMeasureName.compareToIgnoreCase("measureTreeSize") == 0) {
             return measureTreeSize();
         } else if (additionalMeasureName.compareToIgnoreCase("measureNumLeaves") == 0) {
             return measureNumLeaves();
         } else {
             throw new IllegalArgumentException(additionalMeasureName
                     + " not supported (ID3)");
         }
     }
 
     /**
      * @return the number of leaves
      */
     private double measureNumLeaves() {
         double num = 0;
 
         if(isLeaf())
             return 1;
         else {
             for(Grupp3Labb1 suc : m_Successors) {
                 if(suc == null)
                     num++;
                 else
                     num += suc.measureNumLeaves();
             }
         }
 
         return num;
     }
 
     /**
      * @return the number of rules
      */
     private double measureNumRules() {
         double num = 1;
 
         if(isLeaf())
             return 0;
         else {
             for(Grupp3Labb1 suc : m_Successors) {
                 if(suc == null)
                     num++;
                 else
                     num += suc.measureNumRules();
             }
         }
 
         return num;
     }
 
     /**
      * @return the size of the tree
      */
     private double measureTreeSize() {
         double num = 1;
 
         if(isLeaf())
             return 1;
         else {
             for(Grupp3Labb1 suc : m_Successors) {
                 if(suc == null)
                     num++;
                 else
                     num += suc.measureTreeSize();
             }
         }
 
         return num;
     }
 
     /**
      * @return return true if node is a leaf
      */
     private boolean isLeaf() {
         return(m_Successors == null || m_Successors.length == 0);
     }
 }
