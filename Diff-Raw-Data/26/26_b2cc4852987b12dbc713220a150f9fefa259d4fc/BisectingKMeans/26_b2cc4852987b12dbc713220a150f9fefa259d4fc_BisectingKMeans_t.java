 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package weka.clusterers;
 
 import weka.core.Attribute;
 import weka.core.Capabilities;
 import weka.core.DistanceFunction;
 import weka.core.EuclideanDistance;
 import weka.core.Instance;
 import weka.core.Instances;
 import weka.core.ManhattanDistance;
 import weka.core.KPrototypes_DistanceFunction;
 import weka.core.Option;
 import weka.core.RevisionUtils;
 import weka.core.Utils;
 import weka.core.WeightedInstancesHandler;
 import weka.core.Capabilities.Capability;
 import weka.filters.Filter;
 import weka.filters.unsupervised.attribute.ReplaceMissingValues;
 
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Random;
 import java.util.Vector;
 
 /**
  *
  * @author MetalGearRex
  */
 public class BisectingKMeans
   extends RandomizableClusterer 
   implements NumberOfClustersRequestable, WeightedInstancesHandler {
 
   /** for serialization */
   static final long serialVersionUID = -3235809600124455376L;
 
   /**
    * replace missing values in training instances
    */
   private ReplaceMissingValues m_ReplaceMissingFilter;
 
   /**
    * number of clusters to generate
    */
   private int m_NumClusters = 2;
 
   /**
    * Replace missing values globally?
    */
   private boolean m_dontReplaceMissing = false;
 
   /**
    * The number of instances in each cluster
    */
   private int [] m_ClusterSizes;
 
   /**
    * Maximum number of iterations to be executed by the K-means subalgorithm
    */
   private int m_MaxIterations = 500;
 
   /**
    * Holds the errors for all clusters
    */
   private double [] m_ClusterErrors;
 
   /** the distance function used. */
   protected DistanceFunction m_DistanceFunction = new EuclideanDistance();
 
   /**
    * Assignments obtained
    */
   protected int[] m_Assignments = null;
 
   /**
    * Number of executions of the K-means subalgorithm at each splitting
    */
   private int m_NumExecutions = 2;
 
   /**
    * The resulting clusters
    */
   private Vector<Instances> m_Clusters;
 
   /**
    * The cluster centroids
    */
   private Instance[] m_ClusterCentroids;
 
   /**
    * A hash map that holds to which cluster an instance belongs
    */
   private HashMap<String, Integer> m_ClusterIndices;
 
   /**
    * The possible ways to choose the cluster to split
    */
   private int m_wayToChooseClusterToSplit = 3;
 
   /**
    * the default constructor
    */
   public BisectingKMeans() {
     super();
 
     m_SeedDefault = 10;
     setSeed(m_SeedDefault);
   }
 
   /**
    * Returns a string describing this clusterer
    * @return a description of the evaluator suitable for
    * displaying in the explorer/experimenter gui
    */
   public String globalInfo() {
     return "Cluster data using the bisecting k means algorithm; Can use either "
       + "the Euclidean distance (default) or the Manhattan distance;"
       + " If the Manhattan distance is used, then centroids are computed "
       + "as the component-wise median rather than mean.";
   }
 
   /**
    * Returns default capabilities of the clusterer.
    *
    * @return      the capabilities of this clusterer
    */
   public Capabilities getCapabilities() {
     Capabilities result = super.getCapabilities();
     result.disableAll();
     result.enable(Capability.NO_CLASS);
 
     // attributes
     result.enable(Capability.NOMINAL_ATTRIBUTES);
     result.enable(Capability.NUMERIC_ATTRIBUTES);
     result.enable(Capability.MISSING_VALUES);
 
     return result;
   }
 
   /**
    * Chooses a cluster to split into two
    *
    * @param clusters vector of the clusters to choose from
    * @param seed seed for the random number generator
    *
    * @return the index in the vector of the chosen cluster
    */
 
   private int chooseClusterToSplit(int seed) throws Exception {
     int clusterIndex = 0;
     switch (m_wayToChooseClusterToSplit){
       case 1:   // Random
         Random RandomO = new Random(getSeed());
         clusterIndex = RandomO.nextInt(m_Clusters.size());
         break;
       case 2:   // With highest count of instances
         int maxInstances = 0;
         for (int i = 0; i < m_Clusters.size(); ++i){
           if (maxInstances < m_Clusters.get(i).numInstances()){
             clusterIndex = i;
             maxInstances = m_Clusters.get(i).numInstances();
           }
         }
         break;
       case 3:   // With highest error
         double highest_error = -1;
         for (int i = 0; i < m_Clusters.size(); ++i){
           if (highest_error < m_ClusterErrors[i]){
             highest_error = m_ClusterErrors[i];
             clusterIndex = i;
           }
         }
         break;
       default:
         throw new Exception("BisectingKMeans currently only supports 3 ways to choose a cluster to split. Check the tooltip for description");
     }
 
     return clusterIndex;
   }
 
   public void buildClusterer(Instances data) throws Exception {
     getCapabilities().testWithFail(data);
 
     m_ReplaceMissingFilter = new ReplaceMissingValues();
     Instances instances = new Instances(data);
 
     instances.setClassIndex(-1);
     if (!m_dontReplaceMissing) {
       m_ReplaceMissingFilter.setInputFormat(instances);
       instances = Filter.useFilter(instances, m_ReplaceMissingFilter);
     }
 
     // all the instances are assigned to cluster 0
     m_Assignments = new int [instances.numInstances()];
     m_ClusterCentroids = new Instance[m_NumClusters];
     m_ClusterErrors = new double[m_NumClusters];
 
     Random RandomO = new Random(getSeed());
     m_ClusterSizes = new int[m_NumClusters];
 
     m_Clusters = new Vector<Instances>();
     m_Clusters.add(instances);
     m_ClusterIndices = new HashMap<String, Integer>();
     for (int i = 0; i < instances.numInstances(); ++i){
         m_ClusterIndices.put(instances.instance(i).toString(), 0);
     }
 
     while (m_Clusters.size() < m_NumClusters){
       int clusterIndex = chooseClusterToSplit(RandomO.nextInt());
       Instances clusterToSplit = m_Clusters.get(clusterIndex);
       double minimumError = 1.79769313486231570e+308d;  // largest Java number
       Instances first = new Instances(data, 0), second = new Instances(data, 0);
       Instance firstCentroid = null, secondCentroid = null;
       double firstError = 0, secondError = 0;
       for (int l = 0; l < m_NumExecutions; l++){
         // create and configure the K-Means subalgorithm
        weka.clusterers.kMeans kMeans = new weka.clusterers.kMeans();
        //kMeans.setDisplayStdDevs(false);
         kMeans.setDistanceFunction(m_DistanceFunction);
         kMeans.setDontReplaceMissingValues(m_dontReplaceMissing);
         kMeans.setMaxIterations(m_MaxIterations);
         kMeans.setNumClusters(2);   // always split into two subclusters
        //kMeans.setPreserveInstancesOrder(false);    // no need for that
         kMeans.setSeed(RandomO.nextInt());
         kMeans.buildClusterer(clusterToSplit);
 
         // FIXME: think about supporting other types of error calculating
         double currentError = kMeans.getSquaredError();
         if (currentError < minimumError){
           // update the set of clusters with the new clusters
           first.delete();
           second.delete();
           for (int i = 0; i < clusterToSplit.numInstances(); ++i){
             Instance nextInstance = clusterToSplit.instance(i);
             if (kMeans.clusterInstance(nextInstance) == 0){
               first.add(nextInstance);
             }
             else {
               second.add(nextInstance);
             }
           }
           // FIXME: There should be a better way to get the two centroids.
           Instances centroids = kMeans.getClusterCentroids();
           firstCentroid = centroids.instance(0);
           centroids.delete(0);
           secondCentroid = centroids.instance(0);
 
           firstError = kMeans.getClusterErrors()[0];
           secondError = kMeans.getClusterErrors()[1];
           minimumError = currentError;
         }
       }
       // update the set of clusters with the new clusters
       m_Clusters.set(clusterIndex, first);
       m_Clusters.add(second);
       // mark the instances of the split cluster as belonging to one of the two new clusters
       for (int l = 0; l < first.numInstances(); ++l){
           m_ClusterIndices.put(first.instance(l).toString(), clusterIndex);
       }
       for (int l = 0; l < second.numInstances(); ++l){
           m_ClusterIndices.put(second.instance(l).toString(), m_Clusters.size() - 1);
       }
       // update the centroids and errors of the new clusters
       m_ClusterCentroids[clusterIndex] = firstCentroid;
       m_ClusterCentroids[m_Clusters.size() - 1] = secondCentroid;
       m_ClusterErrors[clusterIndex] = firstError;
       m_ClusterErrors[m_Clusters.size() - 1] = secondError;
     }
 
     // set the indices of respective clusters for each instance
     for (int i = 0; i < instances.numInstances(); ++i){
         m_Assignments[i] = m_ClusterIndices.get(instances.instance(i).toString());
     }
 
     // set the sizes of each cluster
     for (int i = 0; i < m_NumClusters; ++i){
         m_ClusterSizes[i] = m_Clusters.get(i).numInstances();
     }
   }
 
   /**
    * clusters an instance that has been through the filters
    *
    * @param instance the instance to assign a cluster to
    * @param updateErrors if true, update the within clusters sum of errors
    * @return a cluster number
    */
   private int clusterProcessedInstance(Instance instance, boolean updateErrors) {
     double minDist = Integer.MAX_VALUE;
     int bestCluster = 0;
     for (int i = 0; i < m_NumClusters; i++) {
       double dist = m_DistanceFunction.distance(instance, m_ClusterCentroids[i]);
       if (dist < minDist) {
 	minDist = dist;
 	bestCluster = i;
       }
     }
     if (updateErrors) {
       if(m_DistanceFunction instanceof EuclideanDistance){
         //Euclidean distance to Squared Euclidean distance
         minDist *= minDist;
       }
       m_ClusterErrors[bestCluster] += minDist;
     }
     return bestCluster;
   }
 
   /**
    * Classifies a given instance.
    *
    * @param instance the instance to be assigned to a cluster
    * @return the number of the assigned cluster as an interger
    * if the class is enumerated, otherwise the predicted value
    * @throws Exception if instance could not be classified
    * successfully
    */
   @Override
   public int clusterInstance(Instance instance) throws Exception {
     Instance inst = null;
     if (!m_dontReplaceMissing) {
       m_ReplaceMissingFilter.input(instance);
       m_ReplaceMissingFilter.batchFinished();
       inst = m_ReplaceMissingFilter.output();
     } else {
       inst = instance;
     }
 
     return clusterProcessedInstance(inst, false);
   }
 
   /**
    * Returns the tip text for this property
    * @return tip text for this property suitable for
    * displaying in the explorer/experimenter gui
    */
   public String numClustersTipText() {
     return "set number of clusters";
   }
 
   /**
    * set the number of clusters to generate
    *
    * @param n the number of clusters to generate
    * @throws Exception if number of clusters is negative
    */
   public void setNumClusters(int n) throws Exception {
     if (n <= 0) {
       throw new Exception("Number of clusters must be > 0");
     }
     m_NumClusters = n;
   }
 
   /**
    * gets the number of clusters to generate
    *
    * @return the number of clusters to generate
    */
   public int getNumClusters() {
     return m_NumClusters;
   }
 
   /**
    * Returns the tip text for this property
    * @return tip text for this property suitable for
    * displaying in the explorer/experimenter gui
    */
   public String maxIterationsTipText() {
     return "set maximum number of iterations";
   }
 
   /**
    * set the maximum number of iterations to be executed
    *
    * @param n the maximum number of iterations
    * @throws Exception if maximum number of iteration is smaller than 1
    */
   public void setMaxIterations(int n) throws Exception {
     if (n <= 0) {
       throw new Exception("Maximum number of iterations must be > 0");
     }
     m_MaxIterations = n;
   }
 
   /**
    * gets the number of maximum iterations to be executed
    *
    * @return the number of clusters to generate
    */
   public int getMaxIterations() {
     return m_MaxIterations;
   }
 
   /**
    * Returns the tip text for this property
    * @return tip text for this property suitable for
    * displaying in the explorer/experimenter gui
    */
   public String numExecutionsTipText() {
     return "set number of executions of the K-means subalgorithm";
   }
 
   /**
    * set the maximum number of iterations to be executed
    *
    * @param n the maximum number of iterations
    * @throws Exception if maximum number of iteration is smaller than 1
    */
   public void setNumExecutions(int n) throws Exception {
     if (n <= 0) {
       throw new Exception("Number of executions must be > 0");
     }
     m_MaxIterations = n;
   }
 
   /**
    * gets the number of maximum iterations to be executed
    *
    * @return the number of clusters to generate
    */
   public int getNumExecutions() {
     return m_NumExecutions;
   }
 
   /**
    * Returns the tip text for this property
    * @return tip text for this property suitable for
    * displaying in the explorer/experimenter gui
    */
   public String dontReplaceMissingValuesTipText() {
     return "Replace missing values globally with mean/mode.";
   }
 
   /**
    * Sets whether missing values are to be replaced
    *
    * @param r true if missing values are to be
    * replaced
    */
   public void setDontReplaceMissingValues(boolean r) {
     m_dontReplaceMissing = r;
   }
 
   /**
    * Gets whether missing values are to be replaced
    *
    * @return true if missing values are to be
    * replaced
    */
   public boolean getDontReplaceMissingValues() {
     return m_dontReplaceMissing;
   }
 
   /**
    * Returns the tip text for this property.
    *
    * @return 		tip text for this property suitable for
    *         		displaying in the explorer/experimenter gui
    */
   public String distanceFunctionTipText() {
     return "The distance function to use for instances comparison " +
       "(default: weka.core.EuclideanDistance). ";
   }
 
   /**
    * returns the distance function currently in use.
    *
    * @return the distance function
    */
   public DistanceFunction getDistanceFunction() {
     return m_DistanceFunction;
   }
 
   /**
    * sets the distance function to use for instance comparison.
    *
    * @param df the new distance function to use
    * @throws Exception if instances cannot be processed
    */
   public void setDistanceFunction(DistanceFunction df) throws Exception {
     if(!(df instanceof EuclideanDistance) &&
        !(df instanceof ManhattanDistance) &&
        !(df instanceof KPrototypes_DistanceFunction))
       {
         throw new Exception("BisectingKMeans currently only supports the Euclidean, Manhattan and KPrototypes distances.");
       }
     m_DistanceFunction = df;
   }
 
   /**
    * Returns the tip text for this property
    * @return tip text for this property suitable for
    * displaying in the explorer/experimenter gui
    */
   public String wayToChooseClusterToSplitTipText() {
     return "Way to choose the cluster to split:" +
            " 1 = Random;" +
            " 2 = With highest count of instances;" +
            " 3 = With highest error";
   }
 
   /**
    * Sets the chosen way to choose the cluster to split
    *
    * @param w the way to choose the cluster to split
    * look at the tooltip for description
    */
   public void setWayToChooseClusterToSplit(int w) throws Exception {
     if ((w <= 0) || (w > 3))
     {
       throw new Exception("BisectingKMeans currently only supports 3 ways to choose a cluster to split. Check the tooltip for description");
     }
     m_wayToChooseClusterToSplit = w;
   }
 
   /**
    * Gets the chosen way to choose the cluster to split
    *
    * @return w the way to choose the cluster to split
    * look at the tooltip for description
    */
   public int getWayToChooseClusterToSplit() {
     return m_wayToChooseClusterToSplit;
   }
 
   /**
    * Returns the number of clusters.
    *
    * @return the number of clusters generated for a training dataset.
    * @throws Exception if number of clusters could not be returned
    * successfully
    */
   public int numberOfClusters() throws Exception {
     return m_NumClusters;
   }
 
   public Enumeration listOptions () {
     Vector result = new Vector();
 
     result.addElement(new Option(
                                  "\tnumber of clusters.\n"
                                  + "\t(default 2).",
                                  "N", 1, "-N <num>"));
     result.addElement(new Option(
                                  "\tReplace missing values with mean/mode.\n",
                                  "M", 0, "-M"));
 
     result.add(new Option(
                           "\tDistance function to use.\n"
                           + "\t(default: weka.core.EuclideanDistance)",
                           "A", 1,"-A <classname and options>"));
 
     result.add(new Option(
                           "\tMaximum number of iterations.\n",
                           "I",1,"-I <num>"));
 
     result.addElement(new Option(
                                  "\tNumber of executions of the K-means subalgorithm.\n",
                                  "X", 1, "-X"));
 
     result.addElement(new Option(
                                  "\tWay to choose the cluster to split.\n",
                                  "W", 1, "-W"));
 
     Enumeration en = super.listOptions();
     while (en.hasMoreElements())
       result.addElement(en.nextElement());
 
     return  result.elements();
   }
   /**
    * Parses a given list of options. <p/>
    *
    <!-- options-start -->
    * Valid options are: <p/>
    *
    * <pre> -N &lt;num&gt;
    *  number of clusters.
    *  (default 2).
    * </pre>
    *
    * <pre> -V
    *  Display std. deviations for centroids.
    * </pre>
    *
    * <pre> -M
    *  Replace missing values with mean/mode.
    * </pre>
    *
    * <pre> -S &lt;num&gt;
    *  Random number seed.
    *  (default 10)
    * </pre>
    *
    * <pre> -A &lt;classname and options&gt;
    *  Distance function to be used for instance comparison
    *  (default weka.core.EuclidianDistance)
    * </pre>
    *
    * <pre> -I &lt;num&gt;
    *  Maximum number of iterations of the K-means subalgorithm.
    * </pre>
    *
    * <pre> -O
    *  Preserve order of instances.
    * </pre>
    *
    * <pre> -X
    *  Number of executions of the K-means subalgorithm at each splitting.
    * </pre>
    *
    * <pre> -W
    *  Way to choose the cluster to split.
    * </pre>
    *
    <!-- options-end -->
    *
    * @param options the list of options as an array of strings
    * @throws Exception if an option is not supported
    */
   public void setOptions (String[] options)
     throws Exception {
 
     m_dontReplaceMissing = Utils.getFlag("M", options);
 
     String optionString = Utils.getOption('N', options);
 
     if (optionString.length() != 0) {
       setNumClusters(Integer.parseInt(optionString));
     }
 
     optionString = Utils.getOption("I", options);
     if (optionString.length() != 0) {
       setMaxIterations(Integer.parseInt(optionString));
     }
 
     optionString = Utils.getOption("X", options);
     if (optionString.length() != 0) {
       setNumExecutions(Integer.parseInt(optionString));
     }
 
     optionString = Utils.getOption("W", options);
     if (optionString.length() != 0) {
       setWayToChooseClusterToSplit(Integer.parseInt(optionString));
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
 
   /**
    * Gets the current settings of BisectingKMeans
    *
    * @return an array of strings suitable for passing to setOptions()
    */
   public String[] getOptions () {
     int       	i;
     Vector    	result;
     String[]  	options;
 
     result = new Vector();
 
     if (m_dontReplaceMissing) {
       result.add("-M");
     }
 
     result.add("-N");
     result.add("" + getNumClusters());
 
     result.add("-A");
     result.add((m_DistanceFunction.getClass().getName() + " " +
                 Utils.joinOptions(m_DistanceFunction.getOptions())).trim());
 
     result.add("-I");
     result.add(""+ getMaxIterations());
 
     result.add("-X");
     result.add(""+ getNumExecutions());
 
     result.add("-В");
     result.add(""+ getWayToChooseClusterToSplit());
 
     options = super.getOptions();
     for (i = 0; i < options.length; i++)
       result.add(options[i]);
 
     return (String[]) result.toArray(new String[result.size()]);
   }
 
   /**
    * return a string describing this clusterer
    *
    * @return a description of the clusterer as a string
    */
   public String toString()
   {
         String resultString = new String();
         resultString = resultString.concat("Number of clusters: ");
         resultString = resultString.concat(m_NumClusters + "\n");
         resultString = resultString.concat("Number of executions of the subalgorithm: ");
         resultString = resultString.concat(m_NumExecutions + "\n");
         resultString = resultString.concat("Max iterations of the subalgorithm: ");
         resultString = resultString.concat(m_MaxIterations + "\n");
         resultString = resultString.concat("\n Cluster centroids:\n");
         for (int i = 0; i < m_NumClusters; ++i){
             resultString = resultString.concat("Cluster " + i + " centroid: ");
             resultString = resultString.concat(m_ClusterCentroids[i].toString() + "\n");
         }
         resultString = resultString.concat("\nCluster errors:\n");
         for (int i = 0; i < m_NumClusters; ++i){
             resultString = resultString.concat("Cluster " + i + " error: ");
             resultString = resultString.concat(m_ClusterErrors[i] + "\n");
         }
         resultString = resultString.concat("\n");
         for (int i = 0; i < m_NumClusters; i++){
             resultString = resultString.concat("Cluster " + i + " contains the following instances: \n");
             for( int j = 0; j< m_Clusters.get(i).numInstances(); j++){
                 resultString = resultString.concat(m_Clusters.get(i).instance(j).toString());
                 resultString = resultString.concat("\n");
             }
             resultString = resultString.concat("=======================================\n");
         }
         resultString = resultString.concat("\n");
         return resultString;
   }
 
   /**
    * Gets the  cluster centroids
    *
    * @return		the cluster centroids
    */
   public Instance[] getClusterCentroids() {
     return m_ClusterCentroids;
   }
 
   /**
    * Gets the cluster errors
    *
    * @return		the cluster errors
    */
   public double[] getClusterErrors() {
     return m_ClusterErrors;
   }
 
   /**
    * Gets the error for all clusters
    *
    * @return		the error
    */
   public double getErrors() {
     return Utils.sum(m_ClusterErrors);
   }
 
   /**
    * Gets the number of instances in each cluster
    *
    * @return		The number of instances in each cluster
    */
   public int [] getClusterSizes() {
     return m_ClusterSizes;
   }
 
   /**
    * Gets the assignments for each instance
    * @return Array of indexes of the centroid assigned to each instance
    * @throws Exception if order of instances wasn't preserved or no assignments were made
    */
   public int [] getAssignments() throws Exception{
     if(m_Assignments == null){
       throw new Exception("No assignments made.");
     }
     return m_Assignments;
   }
 
   /**
    * Returns the revision string.
    *
    * @return		the revision
    */
   public String getRevision() {
     return RevisionUtils.extract("$Revision: 1000 $");
   }
 
   public static void main (String[] argv) {
     runClusterer(new BisectingKMeans(), argv);
   }
 
 }
