 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package weka.clusterers;
 
 import weka.classifiers.rules.DecisionTableHashKey;
 import weka.core.Attribute;
 import weka.core.Capabilities;
 import weka.core.CapabilitiesHandler;
 import weka.core.Capabilities.Capability;
 import weka.core.DistanceFunction;
 import weka.core.EuclideanDistance;
 import weka.core.ManhattanDistance;
 import weka.core.Instance;
 import weka.core.Instances;
 
 import weka.core.Option;
 import weka.core.OptionHandler;
 import weka.core.RevisionUtils;
 import weka.core.Utils;
 import weka.core.WeightedInstancesHandler;
 
 import weka.filters.Filter;
 import weka.filters.unsupervised.attribute.ReplaceMissingValues;
 
 import java.util.Vector;
 import java.util.Enumeration;
 import java.util.Random;
 import java.util.HashMap;
 
 public class kMeans
 extends RandomizableClusterer
   implements NumberOfClustersRequestable, WeightedInstancesHandler, OptionHandler, CapabilitiesHandler {
     //Private Members
     private int m_numClusters;
     private int m_currentIteration;
     private int m_maxIterations;
     private DistanceFunction m_distanceFunction;
     private Instances m_clusterCenters;
     private Instances[] m_clusterDistribution;
     private int[] m_previousAssignment;
     private boolean m_dontReplaceMissing = false;
     private ReplaceMissingValues m_ReplaceMissingFilter;
     private double[] m_clusterErrors;
     //end of Private Members
 
     //Constructors
     public kMeans() {
         m_numClusters = 2;
         m_maxIterations = 100;
         m_distanceFunction = new EuclideanDistance();
     }
     //end of Constructors
 
     //Randomizable Clusterer
     public void buildClusterer(Instances data) throws Exception {
         //is data consistent with cluster capabilities
         getCapabilities().testWithFail(data);
 
         Random rand = new Random(getSeed());
 
         m_clusterCenters = new Instances(data, m_numClusters);
 
         m_ReplaceMissingFilter = new ReplaceMissingValues();
         Instances inst = new Instances(data);
         inst.setClassIndex(-1);
         if (!m_dontReplaceMissing) {
           m_ReplaceMissingFilter.setInputFormat(inst);
           inst = Filter.useFilter(inst, m_ReplaceMissingFilter);
         }
         m_distanceFunction.setInstances(inst);
         int count = inst.numInstances()-1;
 
         int clusterIndex;
         HashMap initC = new HashMap();
         DecisionTableHashKey hk = null;
 
         for (int j = count; j >= 0; j--) {
             clusterIndex = rand.nextInt(j+1);
             hk = new DecisionTableHashKey(inst.instance(clusterIndex),
                                     inst.numAttributes(), true);
             if (!initC.containsKey(hk)) {
                 m_clusterCenters.add(inst.instance(clusterIndex));
                 initC.put(hk, null);
             }
             inst.swap(j, clusterIndex);
 
             if (m_clusterCenters.numInstances() == m_numClusters) {
                 break;
             }
         }
 
         boolean finished = false;
         m_currentIteration = 0;
         m_clusterDistribution = new Instances[m_numClusters];
         m_previousAssignment = new int[count+1];
         int emptyClustCount = 0;
         m_clusterErrors = new double[m_numClusters];
 
         while(!finished) {
             finished = true;
             m_currentIteration++;
 
             for(int i = 0; i < inst.numInstances(); i++) {
                 Instance next = inst.instance(i);
                 int newClust = clusterFilteredInstance(next, true);
                 if(newClust != m_previousAssignment[i]) {
                     m_previousAssignment[i] = newClust;
                     finished = false;
                 }
             }
 
             if(!finished) {
                 m_clusterCenters = new Instances(inst, m_numClusters);
                 for (int i = 0; i < m_numClusters; i++) {
                     m_clusterDistribution[i] = new Instances(inst, 0);
                 }
                 //update Clusters logic
                 for(int i = 0; i < inst.numInstances(); i++) {
                     m_clusterDistribution[m_previousAssignment[i]].add(inst.instance(i));
                 }
                 for (int i = 0; i < m_numClusters; i++) {
                     if (m_clusterDistribution[i].numInstances() == 0) {
                         emptyClustCount++;
                     } else {
                         recalcCenters(m_clusterDistribution[i]);
                     }
                 }
             }
 
             if (emptyClustCount > 0) {
                 m_numClusters -= emptyClustCount;
                 if (finished) {
                     Instances[] newClusterDistribution = new Instances[m_numClusters];
                     int index = 0;
 
                     for (int k = 0; k < m_clusterDistribution.length; k++) {
                         if (m_clusterDistribution[k].numInstances() > 0) {
                             newClusterDistribution[index++] = m_clusterDistribution[k];
                         }
                     }
 
                     m_clusterDistribution = newClusterDistribution;
                 } else {
                     m_clusterDistribution = new Instances[m_numClusters];
                 }
             }
 
             if(!finished) {
                 m_clusterErrors = new double[m_numClusters];
             }
 
             if(m_currentIteration == m_maxIterations)
                 finished = true;
         }
 
         // Substitue the sum of the squared errors with the average squared error.
         for(int f =0; f < m_clusterErrors.length; f++) {
             m_clusterErrors[f] /= m_clusterDistribution[f].numInstances();
         }
     }
     
     public int clusterInstance(Instance instance) throws Exception {
         Instance toCluster = null;
         if (!m_dontReplaceMissing) {
             m_ReplaceMissingFilter.input(instance);
             m_ReplaceMissingFilter.batchFinished();
             toCluster = m_ReplaceMissingFilter.output();
         } else {
             toCluster = instance;
         }
 
         return clusterFilteredInstance(toCluster, false);
     }
 
     private int clusterFilteredInstance(Instance instance, boolean updateErrors) throws Exception {
         double minDist = m_distanceFunction.distance(instance, m_clusterCenters.instance(0));
         int retValue = 0;
 
         for (int i = 1; i < m_numClusters; i++) {
             double dist = m_distanceFunction.distance(instance, m_clusterCenters.instance(i));
             if (dist < minDist) {
                 minDist = dist;
                 retValue = i;
             }
         }
 
         if (updateErrors) {
             if(m_distanceFunction instanceof EuclideanDistance){
                 //Euclidean distance to Squared Euclidean distance
                 minDist *= minDist;
             }
             m_clusterErrors[retValue] += minDist;
         }
 
         return retValue;
     }
 
     public int numberOfClusters() {
         return m_numClusters;
     }
     //end of Randomizable Clusterer
 
     //NumberOfClustersRequestable
     public void setNumClusters(int numClusters) throws Exception {
         if(numClusters <= 0) {
             throw new Exception("Number of clusters must be > 0");
         }
 
         m_numClusters = numClusters;
     }
     //end of NumberOfClustersRequestable
 
     //OptionHandler
     public Enumeration listOptions() {
         Vector result = new Vector();
 
         result.addElement(new Option(
                                  "\tReplace missing values with mean/mode.\n",
                                  "M", 0, "-M"));
 
         result.addElement(new Option(
                                  "\tNumber of clusters.\n"
                                  + "\t(default 2).",
                                  "N", 1, "-N <num>"));
         result.add(new Option(
                           "\tDistance function to use.\n"
                           + "\t(default: weka.core.EuclideanDistance)",
                           "A", 1,"-A <classname and options>"));
 
         result.add(new Option(
                             "\tMaximum number of iterations.\n",
                             "I",1,"-I <num>"));
 
         Enumeration en = super.listOptions();
         while (en.hasMoreElements())
             result.addElement(en.nextElement());
 
         return  result.elements();
     }
 
     public void setOptions(String[] options) throws Exception {
         m_dontReplaceMissing = Utils.getFlag("M", options);
 
         String optionString = Utils.getOption('N', options);
 
         if (optionString.length() != 0) {
             setNumClusters(Integer.parseInt(optionString));
         } else {
             setNumClusters(2);
         }
 
         optionString = Utils.getOption("I", options);
         if (optionString.length() != 0) {
         setMaxIterations(Integer.parseInt(optionString));
         } else {
               setMaxIterations(50);
         }
 
         String distFunctionClass = Utils.getOption('A', options);
         if(distFunctionClass.length() != 0) {
           String distFunctionClassSpec[] = Utils.splitOptions(distFunctionClass);
           if(distFunctionClassSpec.length == 0) {
             throw new Exception("Invalid DistanceFunction specification string.");
           }
           String className = distFunctionClassSpec[0];
           distFunctionClassSpec[0] = "";
 
           setDistanceFunction( (DistanceFunction)
                                Utils.forName( DistanceFunction.class,
                                               className, distFunctionClassSpec) );
         }
         else {
           setDistanceFunction(new EuclideanDistance());
         }
 
         super.setOptions(options);
     }
 
     public String[] getOptions() {
         int i;
         Vector result = new Vector();
         String[] options;
 
         if (m_dontReplaceMissing) {
             result.add("-M");
         }
 
         result.add("-N");
         result.add(""+ numberOfClusters());
 
         result.add("-A");
         result.add((m_distanceFunction.getClass().getName() + " " +
                 Utils.joinOptions(m_distanceFunction.getOptions())).trim());
 
         result.add("-I");
         result.add(""+ getMaxIterations());
 
         options = super.getOptions();
         for (i = 0; i < options.length; i++)
             result.add(options[i]);
 
         return (String[]) result.toArray(new String[result.size()]);
     }
     //end of OptionHandler
 
     //CapabilitiesHandler
     public Capabilities getCapabilities() {
         Capabilities result = super.getCapabilities();
         result.disableAll();
 
         //class
         result.enable(Capability.NO_CLASS);
        //result.enable(Capability.NUMERIC_CLASS);
 
         // attributes
         result.enable(Capability.NUMERIC_ATTRIBUTES);
         result.enable(Capability.MISSING_VALUES);
 
         return result;
     }
     //end of CapabilitiesHandler
 
     //HelperFunctions
     public void setMaxIterations(int n) throws Exception {
         if (n <= 0) {
             throw new Exception("Maximum number of iterations must be > 0");
         }
         m_maxIterations = n;
     }
     public int getMaxIterations() {
         return m_maxIterations;
     }
     public int getNumClusters() {
         return m_numClusters;
     }
     private double[] recalcCenters(Instances members) throws Exception {
         double [] vals = new double[members.numAttributes()];
 
         //used only for Manhattan Distance
         Instances sortedMembers = null;
         int middle = 0;
         boolean dataIsEven = false;
 
         if(m_distanceFunction instanceof ManhattanDistance){
             middle = (members.numInstances()-1)/2;
             dataIsEven = ((members.numInstances()%2)==0);
             sortedMembers = new Instances(members);
 
         }
 
         for (int j = 0; j < members.numAttributes(); j++) {
             if(m_distanceFunction instanceof EuclideanDistance) {
                 vals[j] = members.meanOrMode(j);
             } else if(m_distanceFunction instanceof ManhattanDistance) {
                 if(members.numInstances() == 1) {
                     vals[j] = members.instance(0).value(j);
                 } else {
                     sortedMembers.kthSmallestValue(j, middle+1);
                     vals[j] = sortedMembers.instance(middle).value(j);
                     if( dataIsEven ) {
                         sortedMembers.kthSmallestValue(j, middle+2);
                         vals[j] = (vals[j]+sortedMembers.instance(middle+1).value(j))/2;
                     }
                 }
             }
 
             if(members.attributeStats(j).missingCount == members.numInstances())
                 vals[j] = Instance.missingValue();
         }
 
         m_clusterCenters.add(new Instance(1.0,vals));
         return vals;
     }
 
     public void setDistanceFunction(DistanceFunction df) throws Exception {
         if(!(df instanceof EuclideanDistance) &&
            !(df instanceof ManhattanDistance))
         {
             throw new Exception("SimpleKMeans currently only supports the Euclidean and Manhattan distances.");
         }
         m_distanceFunction = df;
     }
 
     public DistanceFunction getDistanceFunction() {
         return m_distanceFunction;
     }
 
     public void setDontReplaceMissingValues(boolean value) {
         m_dontReplaceMissing = value;
     }
 
     public boolean getDontReplaceMissingValues() {
         return m_dontReplaceMissing;
     }
 
     //Interface for BisectingKMeans class
     public double[] getClusterErrors() {
         return m_clusterErrors;
     }
 
     public Instances getClusterCentroids() {
         return m_clusterCenters;
     }
 
     public double getSquaredError() {
         return Utils.sum(m_clusterErrors);
     }
     //end Interface for BisectingKMeans class
     //end HelperFunctions
 
     //GUI
     public String toString() {
           String resultString = new String();
         resultString = resultString.concat("Number of clusters: ");
         resultString = resultString.concat(m_numClusters + "\n");
         resultString = resultString.concat("\n Cluster centroids:\n");
         for (int i = 0; i < m_numClusters; ++i){
             resultString = resultString.concat("Cluster " + i + " centroid: ");
             resultString = resultString.concat(m_clusterCenters.instance(i).toString() + "\n");
         }
 
         resultString = resultString.concat("\nCluster average squared errors:\n");
         for (int r = 0; r < m_numClusters; ++r){
             resultString = resultString.concat("Cluster " + r + " average squared error: ");
             resultString = resultString.concat(m_clusterErrors[r] + "\n");
         }
 
         resultString = resultString.concat("\nSum of the clusters average squared errors: " + Utils.sum(m_clusterErrors) + "\n");
 
 //        resultString = resultStringd.concat("\n");
 //        for (int i = 0; i < m_numClusters; i++){
 //            resultString = resultString.concat("Cluster " + i + " contains the following instances: \n");
 //            for( int j = 0; j< m_clusterDistribution[i].numInstances(); j++){
 //                resultString = resultString.concat(m_clusterDistribution[i].instance(j).toString());
 //                resultString = resultString.concat("\n");
 //            }
 //            resultString = resultString.concat("=======================================\n");
 //        }
 
         resultString = resultString.concat("\n");
         return resultString;
     }
 
     //GUI Info
     public String globalInfo() {
         return "Basic kMeans algorithm for clustering data, containing numeric only values.";
     }
 
     public String numClustersTipText() {
         return "Set the number of clusters required.";
     }
 
     public String maxIterationsTipText() {
         return "Set maximum number of algorithm cycle iterations.";
     }
 
     public String dontReplaceMissingValuesTipText() {
         return "Replace missing values globally with mean/mode.";
     }
     //end GUI Info
     //end GUI
 
     public static void main (String[] argv) {
         runClusterer(new kMeans(), argv);
     }
 }
