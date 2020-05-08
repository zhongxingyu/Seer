 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 package org.amanzi.neo.loader;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.enums.DriveTypes;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.GisTypes;
 import org.amanzi.neo.core.enums.MeasurementRelationshipTypes;
 import org.amanzi.neo.core.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.eclipse.swt.widgets.Display;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.ReturnableEvaluator;
 import org.neo4j.api.core.StopEvaluator;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.api.core.TraversalPosition;
 import org.neo4j.api.core.Traverser;
 import org.neo4j.api.core.Traverser.Order;
 import org.neo4j.util.index.LuceneIndexService;
 
 public abstract class DriveLoader extends AbstractLoader {
 	
     protected DriveTypes driveType;
     protected String dataset = null;
     protected Node file = null;
     protected Node virtualFile = null;
     protected Node datasetNode = null;
     private static int[] times = new int[2];
     private HashMap<Integer, int[]> stats = new HashMap<Integer, int[]>();
     private int countValidMessage = 0;
     private int countValidLocation = 0;
     private int countValidChanged = 0;
     protected Integer hours = null;
     protected Calendar _workDate = null;
     protected Long minTimeStamp = null;
     protected Long maxTimeStamp = null;
     private boolean needParceHeader = true;
     
     /** How many units of work for the progress monitor for each file */
     public static final int WORKED_PER_FILE = 100;
     
     //TODO: Lagutko, 17.12.2009, maybe create this indexes on rendering but not on importing? 
     protected static LuceneIndexService index;
     
     private HashMap<String, Node> virtualDatasets = new HashMap<String, Node>();
 
     /**
      * Initialize Loader with a specified set of parameters 
      * 
      * @param type defaults to 'Drive' if empty
      * @param neoService defaults to looking up from Neoclipse if null
      * @param fileName name of file to load
      * @param display Display to use for scheduling plugin lookups and message boxes, or null
      * @param datasetName to add data to, or null to add to same as filename
      */
     protected final void initialize(String typeString, NeoService neoService, String filenameString, Display display, String datasetName) {
         super.initialize(typeString, neoService, filenameString, display);
         if (datasetName == null || datasetName.trim().isEmpty()) {
             this.dataset = null;
         } else {
             this.dataset = datasetName.trim();
         }
     }
 
     public void clearCaches() {
         super.clearCaches();
         this.stats.clear();
     }
     
     protected void initializeLuceneIndex() {
     	index = NeoServiceProvider.getProvider().getIndexService();
     }
     
     protected final void addStats(int pn_code, int ec_io) {
         if(!stats.containsKey(pn_code)) this.stats.put(pn_code,new int[2]);
         stats.get(pn_code)[0]+=1;
         stats.get(pn_code)[1]+=ec_io;
     }
 
     /**
      * Finds or create necessary file node, including finding or creating related dataset and gis nodes.
      * 
      * @param measurement point to add as first point to file node if created
      */
     protected void findOrCreateVirtualFileNode(Node mp) {
         if (virtualFile == null) {
             Transaction tx = neo.beginTx();
             try {
                 Node virtualDatasetNode = getVirtualDataset(DriveTypes.MS);
                 Node reference = neo.getReferenceNode();
                 virtualFile = findOrCreateFileNode(reference, virtualDatasetNode);
 
                 virtualFile.createRelationshipTo(mp, GeoNeoRelationshipTypes.NEXT);
 
                 Object time = null;
                 if (mp.hasProperty(INeoConstants.PROPERTY_TIME_NAME)) {
                     time = mp.getProperty(INeoConstants.PROPERTY_TIME_NAME);
                 } else if (mp.hasProperty(INeoConstants.PROPERTY_TIMESTAMP_NAME)) {
                     time = mp.getProperty(INeoConstants.PROPERTY_TIMESTAMP_NAME);
                 }
                 debug("Added '" + time + "' as first measurement of '" + file.getProperty(INeoConstants.PROPERTY_FILENAME_NAME));
                 tx.success();
             } finally {
                 tx.finish();
             }
         }
     }
 
     /**
      * Finds or create necessary file node, including finding or creating related dataset and gis
      * nodes.
      * 
      * @param measurement point to add as first point to file node if created
      */
     protected void findOrCreateFileNode(Node mp) {
         if (file == null) {
             Transaction tx = neo.beginTx();
             try {
                 Node reference = neo.getReferenceNode();
                 file = findOrCreateFileNode(reference, datasetNode);
 
                 Node mainFileNode = datasetNode == null ? file : datasetNode;
                 file.createRelationshipTo(mp, GeoNeoRelationshipTypes.NEXT);
                 findOrCreateGISNode(mainFileNode, GisTypes.DRIVE.getHeader());
 
                 Object time = null;
                 if (mp.hasProperty(INeoConstants.PROPERTY_TIME_NAME)) {
                 	time = mp.getProperty(INeoConstants.PROPERTY_TIME_NAME);
                 }
                 else if (mp.hasProperty(INeoConstants.PROPERTY_TIMESTAMP_NAME)) {
                 	time = mp.getProperty(INeoConstants.PROPERTY_TIMESTAMP_NAME);
                 }
                 debug("Added '" + time + "' as first measurement of '"
                         + file.getProperty(INeoConstants.PROPERTY_FILENAME_NAME));
                 tx.success();
             } finally {
                 tx.finish();
             }
         }
     }
 
     /**
      * Finds or create if not exist necessary file node. Assumes existence of transaction.
      * 
      * @param root root node
      * @param datasetNode dataset node
      * @return file node created
      */
     private Node findOrCreateFileNode(Node root, Node datasetNode) {
         ReturnableEvaluator fileReturnableEvaluator=new ReturnableEvaluator() {
             
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 final Node currentNode = currentPos.currentNode();
                 return NeoUtils.isFileNode(currentNode) && basename.equals(NeoUtils.getNodeName(currentNode));
             }
         };
         Traverser fileNodeTraverser;
         if (datasetNode!=null){
             fileNodeTraverser=datasetNode.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, fileReturnableEvaluator, GeoNeoRelationshipTypes.NEXT,Direction.OUTGOING);
         }else{
             fileNodeTraverser = root.traverse(Order.DEPTH_FIRST, NeoUtils.getStopEvaluator(2), fileReturnableEvaluator,
                     GeoNeoRelationshipTypes.NEXT,
                     Direction.OUTGOING, NetworkRelationshipTypes.CHILD, Direction.OUTGOING);
         }
         final Iterator<Node> iterator = fileNodeTraverser.iterator();
         if (iterator.hasNext()) {
             return iterator.next();
         }
         Node result = neo.createNode();
         result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, INeoConstants.FILE_TYPE_NAME);
         result.setProperty(INeoConstants.PROPERTY_NAME_NAME, basename);
         result.setProperty(INeoConstants.PROPERTY_FILENAME_NAME, filename);
         if (datasetNode != null) {
             datasetNode.createRelationshipTo(result, GeoNeoRelationshipTypes.NEXT);
         }
         return result;
     }
 
     /**
      * Finds or create if not exist necessary dataset node. Assumes existance of transaction.
      * 
      * @param root root node
      * @param datasetName name of dataset node
      * @return dataset node
      */
     protected final Node findOrCreateDatasetNode(Node root, final String datasetName) {
     	
         if (datasetName == null || datasetName.isEmpty()) {
             return null;
         }
         Traverser traverse = NeoCorePlugin.getDefault().getProjectService().getAllDatasetTraverser(root);
         for (Node node : traverse) {
             if (node.getProperty(INeoConstants.PROPERTY_NAME_NAME, "").equals(datasetName)) {
                 return node;
             }
         }
         Node result = neo.createNode();
         result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, INeoConstants.DATASET_TYPE_NAME);
         result.setProperty(INeoConstants.PROPERTY_NAME_NAME, datasetName);
         result.setProperty(INeoConstants.DRIVE_TYPE, driveType.getId());
         return result;
     }
 
     protected final static String propertiesString(Node node){
         StringBuffer properties = new StringBuffer();
         for(String property:node.getPropertyKeys()) {
             if(properties.length()>0) properties.append(", ");
             properties.append(property).append(" => ").append(node.getProperty(property));
             if(properties.length()>80) {
                 properties.append("...");
                 break;
             }
         }
         return properties.toString();
     }
 
     public void printStats(boolean verbose) {
         addTimes(timeTaken());
         super.printStats(verbose);
         notify("Read " + (lineNumber - 1) + " data lines and then filtered down to:");
         notify("\t" + countValidMessage + " with valid messages");
         notify("\t" + countValidLocation + " with known locations");
         notify("\t" + countValidChanged + " with changed data");
         notify("Read " + stats.keySet().size() + " unique PN codes:");
         for (int pn_code : stats.keySet()) {
             int[] pn_counts = stats.get(pn_code);
             notify("\t" + pn_code + " measured " + pn_counts[0] + " times (average Ec/Io = " + pn_counts[1] / pn_counts[0] + ")");
         }
         if (file != null) {
         	if (verbose) {
         		printMeasurements(file);
         	}
         } else {
             error("No measurement file node found");
         }
     }
 
     private void printMeasurement(Node measurement){
         info("Found measurement: "+propertiesString(measurement)+" --- "+childrenString(measurement));
     }
 
     private String childrenString(Node node){
         StringBuffer sb = new StringBuffer();
         for(Relationship relationship:node.getRelationships(MeasurementRelationshipTypes.CHILD,Direction.OUTGOING)){
             if(sb.length()>0) sb.append(", ");
             Node ms = relationship.getEndNode();
             if(ms.hasProperty(INeoConstants.PRPOPERTY_CHANNEL_NAME)){
                 sb.append(ms.getProperty(INeoConstants.PRPOPERTY_CHANNEL_NAME)).append(":");
                 sb.append(ms.getProperty(INeoConstants.PROPERTY_CODE_NAME)).append("=");
                 sb.append((ms.getProperty(INeoConstants.PROPERTY_DBM_NAME).toString()+"000000").substring(0,6));
             }else{
                 sb.append(propertiesString(ms));
             }
         }
         return sb.toString();       
     }
     private void printMeasurements(Node file){
         if (file == null)
             return;
         Transaction transaction = neo.beginTx();
         try {
             int count = 0;
             MEASUREMENTS: for (Relationship relationship : file.getRelationships(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING)) {
                 Node measurement = relationship.getEndNode();
                 printMeasurement(measurement);
                 Iterator<Relationship> relationships = measurement.getRelationships(GeoNeoRelationshipTypes.NEXT,
                         Direction.OUTGOING).iterator();
                 while (relationships.hasNext()) {
                     relationship = relationships.next();
                     measurement = relationship.getEndNode();
                     printMeasurement(measurement);
                     relationships = measurement.getRelationships(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING)
                             .iterator();
                     count++;
                     if(count>20) {
                         notify("Exiting statistics after 100 measurement points");
                         break MEASUREMENTS;
                     }
                 }
             }
             transaction.success();
         } finally {
             transaction.finish();
         }
     }
 
     /**
      * Initialize the accumulating times stats. Do this at the beginning of a block of loading many
      * files and then at the end call the method printTimesStats to see the results.
      */
     public static void initTimes() {
         times = new int[2];
     }
 
     private static int addTimes(long taken) {
         times[0] += 1;
         times[1] += taken;
         return times[0];
     }
 
     public static void printTimesStats() {
         System.err.println("Finished " + times[0] + " loads in " + times[1] / 60000.0 + " minutes (average "
                 + (times[1] / times[0]) / 1000.0 + " seconds per load)");
     }
 
     protected int incValidMessage() {
         return ++countValidMessage;
     }
 
     protected int incValidLocation() {
         return ++countValidLocation;
     }
 
     protected int incValidChanged() {
         return ++countValidChanged;
     }
 
     /**
      * After all lines have been parsed, this method is called, allowing the implementing class the
      * opportunity to save any cached information, or write any final statistics. It is not abstract
      * because it is possible, or even probable, to write an importer that does not need it.
      */
     protected void finishUp() {
         if (minTimeStamp != null) {
             gis.setProperty(INeoConstants.MIN_TIMESTAMP, minTimeStamp);
         }
         if (maxTimeStamp != null) {
             gis.setProperty(INeoConstants.MAX_TIMESTAMP, maxTimeStamp);
         }
         super.cleanupGisNode();//(datasetNode == null ? file : datasetNode);
     }
 
     /**
      * gets root node of drive network;
      * 
      * @return
      */
     protected Node getRootNode() {
         return datasetNode != null ? datasetNode : file;
     }
 
     /**
      * get Timestamp of nodeDate
      * 
      * @param nodeDate date of node
      * @return long (0 if nodeDate==null)
      */
     protected long getTimeStamp(Date nodeDate) {
         if (nodeDate == null || _workDate == null) {
             return 0L;
         }
         final int nodeHours = nodeDate.getHours();
         if (hours != null && hours > nodeHours) {
             // next day
             _workDate.add(Calendar.DAY_OF_MONTH, 1);
 
         }
         hours = nodeHours;
         _workDate.set(Calendar.HOUR_OF_DAY, nodeHours);
         _workDate.set(Calendar.MINUTE, nodeDate.getMinutes());
         _workDate.set(Calendar.SECOND, nodeDate.getSeconds());
         final long timestamp = _workDate.getTimeInMillis();
         updateTimestampMinMax(timestamp);
         return timestamp;
     }
 
 	/**
 	 * Updates Min and Max timestamp values for this gis
 	 *
 	 * @param timestamp
 	 */
 	protected void updateTimestampMinMax(final long timestamp) {
 		minTimeStamp = minTimeStamp == null ? timestamp : Math.min(minTimeStamp, timestamp);
         maxTimeStamp = maxTimeStamp == null ? timestamp : Math.max(maxTimeStamp, timestamp);
 	}
 	
 	public Node getDatasetNode() {
     	return datasetNode == null ? file : datasetNode;
     }
 	
 	/**
 	 * Returns a Virtual Dataset by it's name
 	 *
 	 * @param name name of Dataset
 	 * @param datasetType type of Dataset
 	 * @return dataset node
 	 */
     protected Node getVirtualDataset(DriveTypes datasetType) {
         final String name = datasetType.getFullDatasetName(dataset);
 		Node virtualDataset = virtualDatasets.get(name);
 		if (virtualDataset != null) {
 			return virtualDataset;
 		}
 		Transaction tx = neo.beginTx();
 		try {
 			Iterator<Node> virtualDatasetsIterator = datasetNode.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 			
 				@Override
 				public boolean isReturnableNode(TraversalPosition currentPos) {
 					return name.equals(NeoUtils.getNodeName(currentPos.currentNode()));
 				}
 			}, GeoNeoRelationshipTypes.VIRTUAL_DATASET, Direction.OUTGOING).iterator();
 		
 			if (virtualDatasetsIterator.hasNext()) {
 				virtualDataset = virtualDatasetsIterator.next();
 			}
 			else {
 				virtualDataset = neo.createNode();
 				virtualDataset.setProperty(INeoConstants.PROPERTY_TYPE_NAME, INeoConstants.DATASET_TYPE_NAME);
 				virtualDataset.setProperty(INeoConstants.PROPERTY_NAME_NAME, name);
 				virtualDataset.setProperty(INeoConstants.DRIVE_TYPE, datasetType.getId());				
 				
 				datasetNode.createRelationshipTo(virtualDataset, GeoNeoRelationshipTypes.VIRTUAL_DATASET);
 				
 				virtualDatasets.put(name, virtualDataset);
 			}
 			tx.success();
 		}
 		catch (Exception e) {
 			tx.failure();
 		}
 		finally {
 			tx.finish();
 		}
 		
 		return virtualDataset;
 	}
 
     @Override
     protected boolean needParceHeaders() {
         if (needParceHeader) {
             needParceHeader = false;
             return true;
         }
         return false;
     }
 }
