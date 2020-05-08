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
 
 package org.amanzi.neo.services.utils;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.amanzi.neo.db.manager.NeoServiceProvider;
 import org.amanzi.neo.services.GpehReportUtil;
 import org.amanzi.neo.services.INeoConstants;
 import org.amanzi.neo.services.NeoServiceFactory;
 import org.amanzi.neo.services.enums.CallProperties.CallType;
 import org.amanzi.neo.services.enums.CorrelationRelationshipTypes;
 import org.amanzi.neo.services.enums.DriveTypes;
 import org.amanzi.neo.services.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.services.enums.GisTypes;
 import org.amanzi.neo.services.enums.INodeType;
 import org.amanzi.neo.services.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.services.enums.NetworkTypes;
 import org.amanzi.neo.services.enums.NodeTypes;
 import org.amanzi.neo.services.enums.ProbeCallRelationshipType;
 import org.amanzi.neo.services.enums.SplashRelationshipTypes;
 import org.amanzi.neo.services.indexes.MultiPropertyIndex;
 import org.amanzi.neo.services.indexes.MultiPropertyIndex.MultiDoubleConverter;
 import org.amanzi.neo.services.indexes.MultiPropertyIndex.MultiTimeIndexConverter;
 import org.amanzi.neo.services.indexes.PropertyIndex;
 import org.amanzi.neo.services.indexes.PropertyIndex.NeoIndexRelationshipTypes;
 import org.amanzi.neo.services.nodes.AweProjectNode;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.geotools.referencing.CRS;
 import org.hsqldb.lib.StringUtil;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Path;
 import org.neo4j.graphdb.PropertyContainer;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.RelationshipType;
 import org.neo4j.graphdb.ReturnableEvaluator;
 import org.neo4j.graphdb.StopEvaluator;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.TraversalPosition;
 import org.neo4j.graphdb.Traverser;
 import org.neo4j.graphdb.Traverser.Order;
 import org.neo4j.graphdb.traversal.TraversalDescription;
 import org.neo4j.graphdb.traversal.Uniqueness;
 import org.neo4j.helpers.Predicate;
 import org.neo4j.index.IndexHits;
 import org.neo4j.index.IndexService;
 import org.neo4j.index.lucene.LuceneIndexService;
 import org.neo4j.kernel.Traversal;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 
 
 /**
  * <p>
  * Utility class
  * </p>
  * 
  * @author tsinkel_a
  * @since 1.0.0
  */
 public class Utils {
     private static final Logger LOGGER=Logger.getLogger(Utils.class);
     /** The Constant TIMESTAMP_INDEX_NAME. */
     private static final String TIMESTAMP_INDEX_NAME = "Index-timestamp-";
 
     /** The Constant LOCATION_INDEX_NAME. */
     private static final String LOCATION_INDEX_NAME = "Index-location-";
 
     private static final String PROXY_NAME_SEPARATOR = "/";
     protected Utils() {
         // hide constructor
     }
     
     /**
      * gets name of timestamp index.
      * 
      * @param datasetName - dataset name
      * @return index name
      */
     public static String getTimeIndexName(String datasetName) {
         return TIMESTAMP_INDEX_NAME + datasetName;
     }
 
     /**
      * gets name of location index.
      * 
      * @param datasetName - dataset name
      * @return index name
      */
     public static String getLocationIndexName(String datasetName) {
         return LOCATION_INDEX_NAME + datasetName;
     }
     /**
      * Gets the number value.
      * 
      * @param <T> the generic type
      * @param klass the klass
      * @param value the value
      * @return the number value
      * @throws SecurityException the security exception
      * @throws NoSuchMethodException the no such method exception
      * @throws IllegalArgumentException the illegal argument exception
      * @throws IllegalAccessException the illegal access exception
      * @throws InvocationTargetException the invocation target exception
      */
     @SuppressWarnings("unchecked")
     public static <T extends Number> T getNumberValue(Class<T> klass, String value) throws SecurityException, NoSuchMethodException, IllegalArgumentException,
             IllegalAccessException, InvocationTargetException {
         if (StringUtils.isEmpty(value)) {
             return null;
         }
         String methodName = klass == Integer.class ? "parseInt" : "parse" + klass.getSimpleName();
 
         Method metod = klass.getMethod(methodName, String.class);
         return (T)metod.invoke(null, value);
     }
     /**
      * Gets the relations set.
      * 
      * @param from the 'from' node
      * @param to the 'to' node
      * @param relationType the relation type
      * @return the relations
      */
     public static Set<Relationship> getRelations(Node from, Node to, RelationshipType relationType) {
         Set<Relationship> result = new HashSet<Relationship>();
         for (Relationship relation : from.getRelationships(relationType, Direction.OUTGOING)) {
             if (relation.getOtherNode(from).equals(to)) {
                 result.add(relation);
             }
         }
         return result;
     }
     /**
      * Gets the neighbour relation.
      * 
      * @param server the server
      * @param neighbour the neighbour
      * @param neighbourName the neighbour name
      * @return the neighbour relation
      */
     public static Relationship getNeighbourRelation(Node server, Node neighbour, String neighbourName) {
             Set<Relationship> allRelations = getRelations(server, neighbour, NetworkRelationshipTypes.NEIGHBOUR);
             for (Relationship relation : allRelations) {
                 if (relation.getProperty(INeoConstants.NEIGHBOUR_NAME, "").equals(neighbourName)) {
                     return relation;
                 }
             }
             return null;
     }
     /**
      * Gets the root nodes.
      * 
      * @param additionalFilter the additional filter
      * @return the root nodes
      */
     public static TraversalDescription getTDRootNodes(Predicate<Path> additionalFilter) {
         FilterAND filter = new FilterAND();
         filter.addFilter(new Predicate<Path>() {
 
             @Override
             public boolean accept(Path paramT) {
                 if (paramT.length() != 2) {
                     return false;
                 }
                 return NodeTypes.AWE_PROJECT.checkNode(paramT.lastRelationship().getStartNode());
             }
         });
         filter.addFilter(additionalFilter);
         return Traversal.description().depthFirst().uniqueness(Uniqueness.NONE).prune(Traversal.pruneAfterDepth(2)).filter(filter)
                 .relationships(SplashRelationshipTypes.AWE_PROJECT, Direction.OUTGOING).relationships(GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING);
     }
 
     public static TraversalDescription getTDRootNodesOfProject(final String projectName, Predicate<Path> additionalFilter) {
         FilterAND filter = new FilterAND();
         filter.addFilter(
 
         new Predicate<Path>() {
 
             @Override
             public boolean accept(Path item) {
                 return projectName.equals(item.lastRelationship().getStartNode().getProperty(INeoConstants.PROPERTY_NAME_NAME, null));
             }
         });
         filter.addFilter(additionalFilter);
         return getTDRootNodes(filter);
     }
 
     /**
      * Gets the tD project nodes.
      * 
      * @param additionalFilter the additional filter
      * @return the tD project nodes
      */
     public static TraversalDescription getTDProjectNodes(Predicate<Path> additionalFilter) {
         FilterAND filter = new FilterAND();
         filter.addFilter(new Predicate<Path>() {
 
             @Override
             public boolean accept(Path paramT) {
                 return paramT.length() == 1;
             }
         });
         filter.addFilter(additionalFilter);
         return Traversal.description().depthFirst().uniqueness(Uniqueness.NONE).prune(Traversal.pruneAfterDepth(1)).filter(filter)
                 .relationships(SplashRelationshipTypes.AWE_PROJECT, Direction.OUTGOING);
     }
     /**
      * <p>
      * Wrapper for set of filters
      * </p>
      * 
      * @author tsinkel_a
      * @since 1.0.0
      */
     public static class FilterAND implements Predicate<Path> {
         public LinkedHashSet<Predicate<Path>> filters = new LinkedHashSet<Predicate<Path>>();
 
         /**
          * Adds the filter.
          * 
          * @param filter the filter
          */
         public void addFilter(Predicate<Path> filter) {
             if (filter != null) {
                 filters.add(filter);
             }
         }
 
         @Override
         public boolean accept(Path paramT) {
             for (Predicate<Path> filter : filters) {
                 if (!filter.accept(paramT)) {
                     return false;
                 }
             }
             return true;
         }
 
     }
     /**
      * Find a parent node of the specified type, following the NEXT relations back up the chain
      * 
      * @param node subnode
      * @return parent node of specified type or null
      */
     public static Node getParentNode(Node node, final String type) {
         Traverser traverse = node.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
 
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 return currentPos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME, "").equals(type);
             }
         }, NetworkRelationshipTypes.CHILD, Direction.INCOMING);
         return traverse.iterator().hasNext() ? traverse.iterator().next() : null;
     }
     /**
      * Checks if is rooot node.
      * 
      * @param node the node
      * @return true, if is rooot node (OR its VIRTUAL dataset)
      */
     public static boolean isRoootNode(Node node) {
         if (node == null || !node.hasProperty(INeoConstants.PROPERTY_TYPE_NAME) || node.hasRelationship(GeoNeoRelationshipTypes.VIRTUAL_DATASET, Direction.INCOMING)) {
             return false;
         }
         String typeId = (String)node.getProperty(INeoConstants.PROPERTY_TYPE_NAME);
         return NodeTypes.DATASET.getId().equals(typeId) || NodeTypes.NETWORK.getId().equals(typeId) || NodeTypes.OSS.getId().equals(typeId);
     }
     /**
      * gets name of index.
      * 
      * @param basename - gis name
      * @param propertyName - property name
      * @param type - node type
      * @return luciene key
      */
     public static String getLuceneIndexKeyByProperty(Node basename, String propertyName, INodeType type) {
         return new StringBuilder("Id").append(basename.getId()).append("@").append(type.getId()).append("@").append(propertyName).toString();
     }
     /**
      * Get location index.
      * 
      * @param name - dataset name
      * @return location index
      * @throws IOException Signals that an I/O exception has occurred.
      */
     public static MultiPropertyIndex<Double> getLocationIndexProperty(String name) throws IOException {
         return new MultiPropertyIndex<Double>(Utils.getLocationIndexName(name), new String[] {INeoConstants.PROPERTY_LAT_NAME, INeoConstants.PROPERTY_LON_NAME},
                 new MultiDoubleConverter(0.001), 10);
     }
     
     
     
     //from NeoUtils TODO refactoring
     
     
     
     
 
     /**
      * delete all incoming reference.
      * 
      * @param node the node
      */
     public static void deleteIncomingRelations(Node node) {
         for (Relationship relation : node.getRelationships(Direction.INCOMING)) {
             relation.delete();
         }
     }
 
     /**
      * gets node name.
      * 
      * @param node node
      * @param defValue default value
      * @return node name or defValue
      */
     public static String getNodeType(Node node, String defValue) {
         return (String)node.getProperty(INeoConstants.PROPERTY_TYPE_NAME, defValue);
     }
 
     /**
      * gets node name.
      * 
      * @param node node
      * @return node name or defValue
      */
     public static String getNodeType(Node node) {
         return getNodeType(node, null);
     }
 
     /**
      * Gets node name.
      * 
      * @param node node
      * @return node name or empty string
      * @deprecated
      */
     public static String getNodeName(Node node) {
         return getSimpleNodeName(node, "");
     }
 
     /**
      * Gets node name.
      * 
      * @param node node
      * @param service the service
      * @return node name or empty string
      */
     public static String getNodeName(Node node, GraphDatabaseService service) {
         // String type = node.getProperty(INeoConstants.PROPERTY_TYPE_NAME, "").toString();
         // if (type.equals(INeoConstants.MP_TYPE_NAME)) {
         // return node.getProperty(INeoConstants.PROPERTY_TIME_NAME, "").toString();
         //
         // } else if (type.equals(INeoConstants.HEADER_M)) {
         // return node.getProperty(INeoConstants.PROPERTY_CODE_NAME, "").toString();
         //
         // }
         return getSimpleNodeName(node, "", service);
     }
 
     /**
      * Gets node name.
      * 
      * @param node node
      * @param defValue default value
      * @return node name or empty string
      */
     public static String getSimpleNodeName(PropertyContainer node, String defValue) {
         return (String)node.getProperty(INeoConstants.PROPERTY_NAME_NAME, defValue);
     }
 
     /**
      * Gets node name.
      * 
      * @param node node
      * @param defValue default value
      * @param service the service
      * @return node name or empty string
      */
     public static String getSimpleNodeName(PropertyContainer node, String defValue, GraphDatabaseService service) {
         Transaction tx = beginTx(service);
         try {
             return node.getProperty(INeoConstants.PROPERTY_NAME_NAME, defValue).toString();
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * check node by type.
      * 
      * @param node node
      * @return true if node is file node
      */
     public static boolean isFileNode(Node node) {
         return node != null && NodeTypes.FILE.getId().equals(getNodeType(node, ""));
     }
 
     /**
      * check node by type.
      * 
      * @param node node
      * @return true if node is file node
      */
     public static boolean isDrivePointNode(Node node) {
         return node != null && NodeTypes.MP.getId().equals(getNodeType(node, ""));
     }
 
     /**
      * check node by type.
      * 
      * @param node node
      * @return true if node is file node
      */
     public static boolean isDatasetNode(Node node) {
         return node != null && NodeTypes.DATASET.getId().equals(getNodeType(node, ""));
     }
 
     /**
      * check node by type.
      * 
      * @param node node
      * @return true if node is file node
      */
     public static boolean isOssNode(Node node) {
         return node != null && NodeTypes.OSS.getId().equals(getNodeType(node, ""));
     }
 
     /**
      * check node by type.
      * 
      * @param node node
      * @return true if node is file node
      */
     public static boolean isDriveMNode(Node node) {
         return node != null && NodeTypes.M.getId().equals(getNodeType(node, ""));
     }
 
     /**
      * Is this node a Probe node.
      * 
      * @param node node to check
      * @return is this node a Probe node
      */
     public static boolean isProbeNode(Node node) {
         return node != null && NodeTypes.PROBE.getId().equals(getNodeType(node, ""));
     }
 
     /**
      * Is this node a Probe Calls node.
      * 
      * @param node node to check
      * @return is this node a Probe Calls node
      */
     public static boolean isProbeCallsNode(Node node) {
         return node != null && NodeTypes.CALLS.getId().equals(getNodeType(node, ""));
     }
 
     /**
      * Is this node a Call node.
      * 
      * @param node node to check
      * @return is this node a Call node
      */
     public static boolean isCallNode(Node node) {
         return node != null && NodeTypes.CALL.getId().equals(getNodeType(node, ""));
     }
 
     /**
      * Is this node a SCell node.
      * 
      * @param node node to check
      * @return is this node a Call node
      */
     public static boolean isScellNode(Node node) {
         return node != null && NodeTypes.S_CELL.getId().equals(getNodeType(node, ""));
     }
 
     /**
      * Is this node a SRow node.
      * 
      * @param node node to check
      * @return is this node a Call node
      */
     public static boolean isSRowNode(Node node) {
         return node != null && NodeTypes.S_ROW.getId().equals(getNodeType(node, ""));
     }
 
     /**
      * Is this node a Aggregation node.
      * 
      * @param node node to check
      * @return is this node a Call node
      */
     public static boolean isAggregationNode(Node node) {
         return node != null && NodeTypes.AGGREGATION.getId().equals(getNodeType(node, ""));
     }
 
     /**
      * Is this node a Count node.
      * 
      * @param node node to check
      * @return is this node a Call node
      */
     public static boolean isCountNode(Node node) {
         return node != null && NodeTypes.COUNT.getId().equals(getNodeType(node, ""));
     }
 
     /**
      * gets stop evaluator with necessary depth.
      * 
      * @param depth - depth
      * @return stop evaluator
      */
     public static final StopEvaluator getStopEvaluator(final int depth) {
         return new StopEvaluator() {
             @Override
             public boolean isStopNode(TraversalPosition currentPosition) {
                 return currentPosition.depth() >= depth;
             }
         };
     }
 
     /**
      * finds gis node by name.
      * 
      * @param gisName name of gis node
      * @return gis node or null
      */
     public static Node findGisNode(final String gisName) {
         if (gisName == null || gisName.isEmpty()) {
             return null;
         }
         Node root = NeoServiceProvider.getProvider().getService().getReferenceNode();
         Iterator<Node> gisIterator = root.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 Node node = currentPos.currentNode();
                 return isGisNode(node) && getNodeName(node).equals(gisName);
             }
         }, NetworkRelationshipTypes.CHILD, Direction.OUTGOING).iterator();
         return gisIterator.hasNext() ? gisIterator.next() : null;
     }
 
     /**
      * finds gis node by name.
      * 
      * @param gisName name of gis node
      * @param service the service
      * @return gis node or null
      */
     public static Node findGisNode(final String gisName, final GraphDatabaseService service) {
         if (gisName == null || gisName.isEmpty()) {
             return null;
         }
         Node root = service.getReferenceNode();
         Iterator<Node> gisIterator = root.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 Node node = currentPos.currentNode();
                 return isGisNode(node) && getNodeName(node, service).equals(gisName);
             }
         }, NetworkRelationshipTypes.CHILD, Direction.OUTGOING).iterator();
         return gisIterator.hasNext() ? gisIterator.next() : null;
     }
 
     /**
      * finds root.
      * 
      * @param nodeName name of gis node
      * @param service the service
      * @return gis node or null
      */
     public static Node findRootNodeByName(final String nodeName, final GraphDatabaseService service) {
         if (nodeName == null || nodeName.isEmpty()) {
             return null;
         }
         Transaction tx = beginTx(service);
         try {
             Node root = service.getReferenceNode();
             // root.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, ReturnableEvaluator.ALL,
             // , Direction.OUTGOING)
             Iterator<Node> gisIterator = root.traverse(Order.DEPTH_FIRST, getStopEvaluator(2), new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     if (currentPos.depth() != 2) {
                         return false;
                     }
                     Node node = currentPos.currentNode();
                     return getNodeName(node, service).equals(nodeName);
                 }
             }, SplashRelationshipTypes.AWE_PROJECT, Direction.OUTGOING, NetworkRelationshipTypes.CHILD, Direction.OUTGOING).iterator();
             while (gisIterator.hasNext()) {
                 System.out.println("GIS:" + gisIterator.next());
             }
             return gisIterator.hasNext() ? gisIterator.next() : null;
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * Gets the all root traverser.
      * 
      * @param service the service
      * @param additionalReturnableEvaluator the additional returnableEvaluator
      * @return the all root traverser
      */
     public static Traverser getAllRootTraverser(GraphDatabaseService service, final ReturnableEvaluator additionalReturnableEvaluator) {
         return service.getReferenceNode().traverse(Order.DEPTH_FIRST, getStopEvaluator(2), new ReturnableEvaluator() {
 
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 Node node = currentPos.currentNode();
                 // TODO optimize - get all childs of project like root node!
                 if (!(NodeTypes.NETWORK.checkNode(node) || NodeTypes.AFP.checkNode(node) || isDatasetNode(node) || isOssNode(node))) {
                     return false;
                 }
                 return additionalReturnableEvaluator == null || additionalReturnableEvaluator.isReturnableNode(currentPos);
             }
         }, SplashRelationshipTypes.AWE_PROJECT, Direction.OUTGOING, NetworkRelationshipTypes.CHILD, Direction.OUTGOING);
     }
 
     /**
      * check node by type.
      * 
      * @param node node
      * @return true if node is gis node
      */
     public static boolean isGisNode(Node node) {
         return node != null && NodeTypes.GIS.getId().equals(getNodeType(node, ""));
     }
 
     /**
      * check node by type.
      * 
      * @param node node
      * @return true if node is gis node
      */
     public static boolean isNeighbourNode(Node node) {
         return node != null && NodeTypes.NEIGHBOUR.getId().equals(getNodeType(node, ""));
     }
 
     /**
      * check node by type.
      * 
      * @param node node
      * @return true if node is gis node
      */
     public static boolean isTransmission(Node node) {
         return node != null && NodeTypes.TRANSMISSION.getId().equals(getNodeType(node, ""));
     }
 
     /**
      * Delete gis node if it do not have outcoming relations.
      */
     public static void deleteEmptyGisNodes() {
         Transaction tx = NeoServiceProvider.getProvider().getService().beginTx();
         try {
             Node root = NeoServiceProvider.getProvider().getService().getReferenceNode();
             Traverser gisTraverser = root.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     Node node = currentPos.currentNode();
                     return isGisNode(node) && !node.hasRelationship(Direction.OUTGOING);
                 }
             }, NetworkRelationshipTypes.CHILD, Direction.OUTGOING);
             for (Node gisNode : gisTraverser.getAllNodes()) {
                 deleteSingleNode(gisNode, NeoServiceProvider.getProvider().getService());
             }
             tx.success();
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Finds gis node by child.
      * 
      * @param childNode child
      * @return gis node or null
      */
     public static Node findGisNodeByChild(Node childNode) {
         return findGisNodeByChild(childNode, NeoServiceProvider.getProvider().getService());
 
     }
 
     /**
      * Finds gis node by child.
      * 
      * @param childNode child
      * @param service NeoService
      * @return gis node or null
      */
     public static Node findGisNodeByChild(Node childNode, GraphDatabaseService service) {
         if (childNode == null) {
             return null;
         }
         Transaction tx = service.beginTx();
         try {
             Iterator<Node> gisIterator = childNode.traverse(Order.BREADTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     Node node = currentPos.currentNode();
                     return isGisNode(node);
                 }
             }, NetworkRelationshipTypes.CHILD, Direction.INCOMING, GeoNeoRelationshipTypes.NEXT, Direction.INCOMING, GeoNeoRelationshipTypes.VIRTUAL_DATASET, Direction.INCOMING,
                     NetworkRelationshipTypes.NEIGHBOUR_DATA, Direction.INCOMING, NetworkRelationshipTypes.TRANSMISSION_DATA, Direction.INCOMING).iterator();
             tx.success();
             return gisIterator.hasNext() ? gisIterator.next() : null;
         } finally {
             tx.finish();
         }
     }
 
 
 
     /**
      * begins new neo4j transaction.
      * 
      * @return transaction
      */
     public static Transaction beginTransaction() {
        return NeoServiceProvider.getProvider().getService().beginTx();
 
     }
 
     /**
      * Finds Neighbour of network.
      * 
      * @param network - network GIS node
      * @param name - Neighbour name
      * @return Neighbour node or null;
      */
     public static Node findNeighbour(Node network, final String name) {
 
         return findNeighbour(network, name, NeoServiceProvider.getProvider().getService());
     }
 
     /**
      * Finds Neighbour of network.
      * 
      * @param network - network GIS node
      * @param name - Neighbour name
      * @param neo the neo
      * @return Neighbour node or null;
      */
     public static Node findNeighbour(Node network, final String name, GraphDatabaseService neo) {
         if (network == null || name == null) {
             return null;
         }
         assert !isGisNode(network);
         Transaction tx = neo.beginTx();
         try {
             Iterator<Node> iterator = network.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     Node node = currentPos.currentNode();
                     return isNeighbourNode(node) && name.equals(node.getProperty(INeoConstants.PROPERTY_NAME_NAME, ""));
                 }
             }, NetworkRelationshipTypes.NEIGHBOUR_DATA, Direction.OUTGOING).iterator();
             return iterator.hasNext() ? iterator.next() : null;
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Gets array of Numeric Fields.
      * 
      * @param node - node
      * @return array or null if properties do not exist
      */
     public static String[] getNumericFields(Node node) {
         Transaction tx = beginTransaction();
         try {
             if (node.hasProperty(INeoConstants.LIST_NUMERIC_PROPERTIES)) {
                 return (String[])node.getProperty(INeoConstants.LIST_NUMERIC_PROPERTIES);
             }
             // TODO remove this after refactoring tems loader
             if (node.getProperty(INeoConstants.PROPERTY_GIS_TYPE_NAME, "").equals(GisTypes.DRIVE.getHeader())) {
                 List<String> result = new ArrayList<String>();
                 Iterator<Node> iteratorProperties = node.traverse(Order.BREADTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
 
                     @Override
                     public boolean isReturnableNode(TraversalPosition traversalposition) {
                         Node curNode = traversalposition.currentNode();
                         Object type = curNode.getProperty(INeoConstants.PROPERTY_TYPE_NAME, null);
                         return type != null && (NodeTypes.M.getId().equals(type.toString()));
                     }
                 }, NetworkRelationshipTypes.CHILD, Direction.OUTGOING, GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING).iterator();
                 if (iteratorProperties.hasNext()) {
                     Node propNode = iteratorProperties.next();
                     Iterator<String> iteratorProper = propNode.getPropertyKeys().iterator();
                     while (iteratorProper.hasNext()) {
                         String propName = iteratorProper.next();
                         if (propNode.getProperty(propName) instanceof Number) {
                             result.add(propName);
                         }
                     }
                 }
                 return result.toArray(new String[0]);
             }
             return new String[0];
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Gets Neighbour name of the proxy server node.
      * 
      * @param serverNode the proxy server node
      * @return name
      */
     public static String getNeighbourName(Node serveNode) {
         String sectorName = serveNode.getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
         return sectorName.split(PROXY_NAME_SEPARATOR)[0];
     }
 
     /**
      * Gets Neighbour name of relation.
      * 
      * @param relation relation
      * @param defValue default value
      * @return name or default value if property do not exist
      */
     public static String getNeighbourName(Relationship relation, String defValue) {
         return relation.getProperty(INeoConstants.NEIGHBOUR_NAME, defValue).toString();
     }
 
     /**
      * Return neighbour relations of selected neighbour list.
      * 
      * @param node node
      * @param neighbourName neighbour list name (if null then will returns all neighbour relations
      *        of this node)
      * @return neighbour relations of selected neighbour list
      */
     public static Iterable<Relationship> getNeighbourRelations(Node node, String neighbourName) {
         Iterable<Relationship> relationships = node.getRelationships(NetworkRelationshipTypes.NEIGHBOUR, Direction.OUTGOING);
         if (neighbourName == null) {
             return relationships;
         }
         ArrayList<Relationship> result = new ArrayList<Relationship>();
         for (Relationship relation : relationships) {
             // if (neighbourName.equals(getNeighbourName(relation, null))) {
             if (neighbourName.equals(getNeighbourName(relation, "").equals("") ? getNeighbourName(relation.getStartNode()) : getNeighbourName(relation, null))) {
                 result.add(relation);
             }
         }
         return result;
     }
 
     /**
      * Return neighbour relations of selected neighbour list.
      * 
      * @param node node
      * @param neighbourName neighbour list name (if null then will returns all neighbour relations
      *        of this node)
      * @return neighbour relations of selected neighbour list
      */
     public static Iterable<Relationship> getTransmissionRelations(Node node, String neighbourName) {
         Iterable<Relationship> relationships = node.getRelationships(NetworkRelationshipTypes.TRANSMISSION, Direction.OUTGOING);
         if (neighbourName == null) {
             return relationships;
         }
         ArrayList<Relationship> result = new ArrayList<Relationship>();
         for (Relationship relation : relationships) {
             if (neighbourName.equals(getNeighbourName(relation, "").equals("") ? getNeighbourName(relation.getStartNode()) : getNeighbourName(relation, null))) {
                 result.add(relation);
             }
         }
         return result;
     }
 
     /**
      * gets neighbour property name.
      * 
      * @param aNeighbourName - name of neighbour
      * @return property name
      */
     public static String getNeighbourPropertyName(String aNeighbourName) {
         return String.format("# '%s' neighbours", aNeighbourName);
     }
 
     /**
      * get Transmission property name.
      * 
      * @param aTransmissionName name of transmission
      * @return property name
      */
     public static String getTransmissionPropertyName(String aTransmissionName) {
         return String.format("# '%s' transmissions", aTransmissionName);
     }
 
     /**
      * Finds node by child.
      * 
      * @param node - node
      * @param nodeType - type of finding node
      * @return node or null
      */
     public static Node findNodeByChild(Node node, final String nodeType) {
         if (nodeType == null || nodeType.isEmpty()) {
             return null;
         }
         Transaction tx = beginTransaction();
         try {
             Iterator<Node> iterator = node.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     Node node = currentPos.currentNode();
                     return nodeType.equals(getNodeType(node, ""));
                 }
             }, NetworkRelationshipTypes.CHILD, Direction.INCOMING, GeoNeoRelationshipTypes.NEXT, Direction.INCOMING).iterator();
             tx.success();
             return iterator.hasNext() ? iterator.next() : null;
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Get all fields property.
      * 
      * @param node - node
      * @return array of fields or null
      */
     public static String[] getAllFields(Node node) {
         Transaction tx = beginTransaction();
         try {
             String[] result = (String[])node.getProperty(INeoConstants.LIST_ALL_PROPERTIES, null);
             return result == null ? new String[0] : result;
         } finally {
             tx.finish();
         }
 
     }
 
     /**
      * delete node and all relation from/to it.
      * 
      * @param node - node to delete
      * @param service the service
      */
     public static void deleteSingleNode(Node node, GraphDatabaseService service) {
         Transaction tx = beginTx(service);
         try {
             for (Relationship relation : node.getRelationships()) {
                 relation.delete();
             }
             node.delete();
             successTx(tx);
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * Gets chart node of current node.
      * 
      * @param node node
      * @param aggNode aggregation node
      * @return node or null
      */
     public static Node getChartNode(Node node, Node aggNode) {
         if (node == null || aggNode == null) {
             return null;
         }
         Transaction tx = beginTransaction();
         final long nodeId = aggNode.getId();
         try {
             Iterator<Node> iterator = node.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     return nodeId == (Long)currentPos.currentNode().getProperty(INeoConstants.PROPERTY_AGGR_PARENT_ID, nodeId - 1);
                 }
             }, NetworkRelationshipTypes.AGGREGATE, Direction.INCOMING).iterator();
             return iterator.hasNext() ? iterator.next() : null;
         } finally {
             tx.finish();
         }
     }
 
     /**
      * get timestamp of node.
      * 
      * @param node node
      * @return (Long) timestamp
      */
     public static Long getNodeTime(Node node) {
         if (node.hasProperty("timestamp")) {
             Object time = node.getProperty("timestamp");
             if (time instanceof Long) {
                 return (Long)time;
             }
         }
         // TODO: This code only supports Romes data, we need TEMS support also (later)
         Object time = node.getProperty("time", null);
         if (time == null) {
             return null;
         }
         if (time instanceof Long) {
             return (Long)time;
         }
         SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
         Date timeD;
         try {
             timeD = df.parse(time.toString());
         } catch (ParseException e) {
             LOGGER.error(e.getLocalizedMessage(), e);
             return null;
         }
         return timeD.getTime();
     }
 
     /**
      * get Traverser of all file node.
      * 
      * @param gis - gis node
      * @return Traverser
      */
     public static Traverser getAllFileNodes(Node gis) {
         return gis.traverse(Order.DEPTH_FIRST, getStopEvaluator(3), new ReturnableEvaluator() {
 
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 return isFileNode(currentPos.currentNode());
             }
         }, GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING);
     }
 
     /**
      * gets node by id.
      * 
      * @param nodeId node id
      * @return node
      */
     public static Node getNodeById(Long nodeId) {
         return nodeId == null ? null : NeoServiceProvider.getProvider().getService().getNodeById(nodeId);
     }
 
     /**
      * Finds Transmission of network.
      * 
      * @param network - network GIS node
      * @param name - Transmission name
      * @param neo - NeoService
      * @return Transmission node or null;
      */
     public static Node findTransmission(Node network, final String name, GraphDatabaseService neo) {
         if (network == null || name == null) {
             return null;
         }
         assert !isGisNode(network);
         Transaction tx = neo.beginTx();
         try {
             Iterator<Node> iterator = network.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     Node node = currentPos.currentNode();
                     return isTransmission(node) && name.equals(node.getProperty(INeoConstants.PROPERTY_NAME_NAME, ""));
                 }
             }, NetworkRelationshipTypes.TRANSMISSION_DATA, Direction.OUTGOING).iterator();
             return iterator.hasNext() ? iterator.next() : null;
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Gets name of ms childs.
      * 
      * @param node - mp node
      * @param msName - property , that forms the name
      * @return cummulative name
      */
     public static String getMsNames(Node node, final String msName) {
         String delim = ", ";
         StringBuilder result = new StringBuilder("");
         Traverser traverse = node.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 Node curNode = currentPos.currentNode();
                 return !currentPos.isStartNode() && curNode.hasProperty(msName);
             }
         }, GeoNeoRelationshipTypes.LOCATION, Direction.INCOMING);
         for (Node nodeMs : traverse) {
 
             result.append(nodeMs.getProperty(msName).toString()).append(delim);
         }
         if (result.length() > delim.length()) {
             result.setLength(result.length() - delim.length());
         }
         return result.toString();
     }
 
     /**
      * finds or create if necessary SectorDriveRoot node.
      * 
      * @param root - root node of drive network
      * @param service the service
      * @param isNewTransaction the is new transaction
      * @return SectorDriveRoot node
      */
     public static Node findOrCreateSectorDriveRoot(Node root, GraphDatabaseService service, boolean isNewTransaction) {
 
         GraphDatabaseService neo = isNewTransaction ? service : null;
         Transaction tx = beginTx(neo);
         try {
             Relationship relation = root.getSingleRelationship(NetworkRelationshipTypes.SECTOR_DRIVE, Direction.OUTGOING);
             if (relation != null) {
                 return relation.getOtherNode(root);
             }
             Node result = service.createNode();
             result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.ROOT_SECTOR_DRIVE.getId());
             result.setProperty(INeoConstants.PROPERTY_NAME_NAME, INeoConstants.ROOT_SECTOR_DRIVE);
             root.createRelationshipTo(result, NetworkRelationshipTypes.SECTOR_DRIVE);
             successTx(tx);
             return result;
         } finally {
             finishTx(tx);
         }
 
     }
 
     /**
      * find or create sector-drive node.
      * 
      * @param aDriveName the a drive name
      * @param sectorDriveRoot the sector drive root
      * @param mpNode - mp node
      * @param service the service
      * @param isNewTransaction the is new transaction
      * @return sector-drive node
      */
     public static Node findOrCreateSectorDrive(String aDriveName, Node sectorDriveRoot, Node mpNode, GraphDatabaseService service, boolean isNewTransaction) {
         GraphDatabaseService neo = isNewTransaction ? service : null;
         Transaction tx = beginTx(neo);
         try {
             final Object idProperty = mpNode.getProperty(INeoConstants.SECTOR_ID_PROPERTIES, null);
             if (idProperty == null) {
                 return null;
             }
             if (mpNode.hasRelationship(NetworkRelationshipTypes.DRIVE, Direction.INCOMING)) {
                 return mpNode.getSingleRelationship(NetworkRelationshipTypes.DRIVE, Direction.INCOMING).getOtherNode(mpNode);
             }
             Iterator<Node> iterator = sectorDriveRoot.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     Node node = currentPos.currentNode();
                     Object id = node.getProperty(INeoConstants.SECTOR_ID_PROPERTIES, null);
 
                     return id != null && idProperty.equals(id);
                 }
             }, NetworkRelationshipTypes.CHILD, Direction.OUTGOING).iterator();
             Node result;
             if (iterator.hasNext()) {
                 result = iterator.next();
             } else {
                 result = service.createNode();
                 result.setProperty(INeoConstants.SECTOR_ID_PROPERTIES, idProperty);
                 sectorDriveRoot.createRelationshipTo(result, NetworkRelationshipTypes.CHILD);
             }
             Relationship relation = result.createRelationshipTo(mpNode, NetworkRelationshipTypes.DRIVE);
             relation.setProperty(INeoConstants.DRIVE_GIS_NAME, aDriveName);
             successTx(tx);
             return result;
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * Finds or create if not exist child node. Assumes existence of transaction.
      * 
      * @param service the service
      * @param parentNode parent node
      * @param nodeName the node name
      * @return Pair
      */
     public static Pair<Boolean, Node> findOrCreateChildNode(GraphDatabaseService service, Node parentNode, final String nodeName) {
         Transaction tx = beginTx(service);
         try {
             Traverser fileNodeTraverser = getChildTraverser(parentNode);
             Node lastChild = null;
             for (Node node : fileNodeTraverser) {
                 if (getNodeName(node, service).equals(nodeName)) {
                     return new Pair<Boolean, Node>(false, node);
                 }
                 lastChild = node;
             }
 
             Node result = service.createNode();
             result.setProperty(INeoConstants.PROPERTY_NAME_NAME, nodeName);
             if (lastChild == null) {
                 parentNode.createRelationshipTo(result, GeoNeoRelationshipTypes.CHILD);
             } else {
                 lastChild.createRelationshipTo(result, GeoNeoRelationshipTypes.NEXT);
             }
             successTx(tx);
             return new Pair<Boolean, Node>(true, result);
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * Finds or create if not exist child node. Assumes existence of transaction.
      * 
      * @param service the service
      * @param parentNode parent node
      * @param nodeName the node name
      * @param fileName the file name
      * @return Pair
      */
     public static Pair<Boolean, Node> findOrCreateFileNode(GraphDatabaseService service, Node parentNode, final String nodeName, final String fileName) {
         Transaction tx = beginTx(service);
         try {
             Pair<Boolean, Node> result = findOrCreateChildNode(service, parentNode, nodeName);
             if (result.getLeft()) {
                 Node node = result.getRight();
                 node.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.FILE.getId());
                 node.setProperty(INeoConstants.PROPERTY_FILENAME_NAME, fileName);
             }
             successTx(tx);
             return result;
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * get all network gis nodes which linked with drive nodes.
      * 
      * @param service NeoService
      * @return Traverser
      */
     public static Traverser getLinkedNetworkTraverser(GraphDatabaseService service) {
         Transaction tx = service.beginTx();
         try {
             return service.getReferenceNode().traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     Node node = currentPos.currentNode();
                     if (!isGisNode(node)) {
                         return false;
                     }
                     Object type = node.getProperty(INeoConstants.PROPERTY_GIS_TYPE_NAME, "");
                     if (!type.equals(GisTypes.NETWORK.getHeader())) {
                         return false;
                     }
                     return node.hasRelationship(CorrelationRelationshipTypes.LINKED_NETWORK_DRIVE, Direction.OUTGOING);
                 }
             }, NetworkRelationshipTypes.CHILD, Direction.OUTGOING);
         } finally {
             tx.finish();
         }
     }
 
     /**
      * finish transaction if it not null.
      * 
      * @param tx -Transaction or null
      */
     public static void finishTx(Transaction tx) {
         if (tx != null) {
             tx.finish();
         }
     }
 
     /**
      * mark success transaction if it not null.
      * 
      * @param tx -Transaction or null
      */
     public static void successTx(Transaction tx) {
         if (tx != null) {
             tx.success();
         }
     }
 
     /**
      * begin transaction.
      * 
      * @param service the service
      * @return Transaction if service present else null
      */
     public static Transaction beginTx(GraphDatabaseService service) {
         return service == null ? null : service.beginTx();
     }
 
     /**
      * link SectorDrive node with sector node.
      * 
      * @param networkGis network gis node
      * @param sectorDrive - SectorDrive node
      * @param service - neo service if null then transaction do not created
      * @return sector node or null if sector not found
      */
     public static Node linkWithSector(Node networkGis, Node sectorDrive, GraphDatabaseService service) {
         Transaction tx = beginTx(service);
         try {
             // TODO use index?
             String networkName = getSimpleNodeName(networkGis, "");
             for (Relationship relation : sectorDrive.getRelationships(NetworkRelationshipTypes.SECTOR, Direction.OUTGOING)) {
                 if (networkName.equals(relation.getProperty(INeoConstants.NETWORK_GIS_NAME, ""))) {
                     return relation.getOtherNode(sectorDrive);
                 }
             }
             final Object id = sectorDrive.getProperty(INeoConstants.SECTOR_ID_PROPERTIES);
             Iterator<Node> iterator = networkGis.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     Node node = currentPos.currentNode();
                     if (!node.hasProperty("ci")) {
                         return false;
                     }
                     return node.getProperty("ci").equals(id);
                 }
             }, NetworkRelationshipTypes.CHILD, Direction.OUTGOING, GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING).iterator();
             if (!iterator.hasNext()) {
                 return null;
             }
             Node result = iterator.next();
             Relationship relation = sectorDrive.createRelationshipTo(result, NetworkRelationshipTypes.SECTOR);
             relation.setProperty(INeoConstants.NETWORK_GIS_NAME, networkName);
             successTx(tx);
             return result;
         } finally {
             finishTx(tx);
         }
 
     }
 
     /**
      * get list of all event of mp node.
      * 
      * @param mpNode mp node
      * @param service - neo service if null then transaction do not created
      * @return list of all event of mp node
      */
     public static Set<String> getEventsList(Node mpNode, GraphDatabaseService service) {
         // TODO store list of events in mp node
         Transaction tx = beginTx(service);
         try {
             Set<String> result = new HashSet<String>();
             Traverser traverse = mpNode.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     if (currentPos.isStartNode()) {
                         return false;
                     }
                     Node node = currentPos.currentNode();
                     boolean result = node.hasProperty(INeoConstants.PROPERTY_TYPE_EVENT);
                     return result;
                 }
             }, GeoNeoRelationshipTypes.LOCATION, Direction.INCOMING);
             for (Node node : traverse) {
                 result.add(node.getProperty(INeoConstants.PROPERTY_TYPE_EVENT).toString());
             }
             return result;
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * Adds the transaction log.
      * 
      * @param transaction the transaction
      * @param thread the thread
      * @param description the description
      */
     public static void addTransactionLog(Transaction transaction, Thread thread, String description) {
         PrintStream out;
         final String simpleName = transaction == null ? "" : transaction.getClass().getSimpleName();
         if (simpleName.isEmpty() || simpleName.equals("TransactionImpl")) {
             out = System.out;
         } else {
             out = System.err;
         }
         out.println(new StringBuilder("Transaction:\t").append(thread.getId()).append("\t").append(thread.getName()).append("\t").append(simpleName).append("\t")
                 .append(description));
     }
 
 
 
     /**
      * Returns pair of min and max timestamps for this dataset.
      * 
      * @param driveGisNode drive gis node
      * @param service neoservice if null then transaction do not created
      * @return pair of min and max timestamps
      */
     public static Pair<Long, Long> getMinMaxTimeOfDataset(Node driveGisNode, GraphDatabaseService service) {
         Transaction tx = beginTx(service);
 
         try {
             // TODO only for fast fix - remove code after testing
             if (isGisNode(driveGisNode)) {
                 StringBuilder st = new StringBuilder("should be fixed - gets this property from root node instead gis node!\n");
                 for (StackTraceElement elem : Thread.currentThread().getStackTrace()) {
                     st.append("\tat ").append(elem).append("\n");
                 }
                 LOGGER.error(st.toString());
                 return getMinMaxTimeOfDataset(driveGisNode.getSingleRelationship(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING).getOtherNode(driveGisNode), service);
             }
             // --
             Pair<Long, Long> pair = new Pair<Long, Long>((Long)driveGisNode.getProperty(INeoConstants.MIN_TIMESTAMP, null), (Long)driveGisNode.getProperty(
                     INeoConstants.MAX_TIMESTAMP, null));
             return pair;
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * Gets timestamp index property.
      * 
      * @param name - dataset name
      * @return timestamp index property
      * @throws IOException Signals that an I/O exception has occurred.
      */
     public static MultiPropertyIndex<Long> getTimeIndexProperty(String name) throws IOException {
         return new MultiPropertyIndex<Long>(getTimeIndexName(name), new String[] {INeoConstants.PROPERTY_TIMESTAMP_NAME}, new MultiTimeIndexConverter(), 10);
     }
 
 
 
     /**
      * Searches for the Probe node by it's name.
      * 
      * @param networkNode Network that should contain Probe
      * @param probeName name of Probe
      * @param service the service
      * @return Probe node
      */
     public static Node findOrCreateProbeNode(Node networkNode, final String probeName, final GraphDatabaseService service) {
         Transaction tx = beginTx(service);
         Node result = null;
         try {
             Iterator<Node> probeIterator = networkNode.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     return isProbeNode(currentPos.currentNode()) && probeName.equals(getNodeName(currentPos.currentNode(), service));
                 }
             }, GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING).iterator();
 
             if (probeIterator.hasNext()) {
                 result = probeIterator.next();
             } else {
                 result = service.createNode();
                 result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.PROBE.getId());
                 result.setProperty(INeoConstants.PROPERTY_NAME_NAME, probeName);
 
                 networkNode.createRelationshipTo(result, GeoNeoRelationshipTypes.CHILD);
             }
             tx.success();
         } catch (Exception e) {
             LOGGER.error(e.getLocalizedMessage(), e);
             tx.failure();
         } finally {
             tx.finish();
         }
 
         return result;
     }
 
     /**
      * Build probe name like: 'name (La: local area, F: frequency)'.
      * 
      * @param probeName String (real probe name)
      * @param la Integer (local area)
      * @param frequency Float
      * @return String
      */
     public static String buildProbeName(String probeName, Integer la, Float frequency) {
         return probeName + " (LA: " + la + ", F: " + frequency + ")";
     }
 
     /**
      * Returns a Probe Calls node for current dataset and network.
      * 
      * @param datasetNode the dataset node
      * @param probesName name of probe
      * @param probesNode node for probe
      * @param service Neo Serivce
      * @return a Probe Calls node for current dataset
      */
     public static Node getCallsNode(Node datasetNode, String probesName, Node probesNode, GraphDatabaseService service) {
         Transaction tx = beginTx(service);
         Node callsNode = null;
         try {
             final String datasetName = getNodeName(datasetNode, service);
             Iterator<Node> callsIterator = probesNode.traverse(Order.BREADTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     return datasetName.equals(currentPos.currentNode().getProperty(NodeTypes.DATASET.getId(), ""));
                 }
             }, ProbeCallRelationshipType.CALLS, Direction.OUTGOING, GeoNeoRelationshipTypes.VIRTUAL_DATASET, Direction.OUTGOING).iterator();
 
             if (callsIterator.hasNext()) {
                 callsNode = callsIterator.next();
             } else {
                 callsNode = service.createNode();
                 callsNode.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.CALLS.getId());
                 callsNode.setProperty(INeoConstants.PROPERTY_NAME_NAME, probesName + " - " + datasetName);
                 callsNode.setProperty(NodeTypes.DATASET.getId(), datasetName);
 
                 probesNode.createRelationshipTo(callsNode, ProbeCallRelationshipType.CALLS);
                 datasetNode.createRelationshipTo(callsNode, ProbeCallRelationshipType.PROBE_DATASET);
             }
 
             tx.success();
         } catch (Exception e) {
             LOGGER.error(e.getLocalizedMessage(), e);
             tx.failure();
         } finally {
             tx.finish();
         }
 
         return callsNode;
     }
 
     /**
      * Returns last Call from Probe Calls queue.
      * 
      * @param probeCallsNode Probe Calls node
      * @param service Neo service
      * @return last Call node
      */
     public static Node getLastCallFromProbeCalls(Node probeCallsNode, GraphDatabaseService service) {
         Transaction tx = beginTx(service);
         Node callNode = null;
         try {
             Long lastCallId = (Long)probeCallsNode.getProperty(INeoConstants.LAST_CALL_NODE_ID_PROPERTY_NAME, new Long(-1));
 
             if (lastCallId != -1) {
                 callNode = service.getNodeById(lastCallId);
             }
 
             tx.success();
         } finally {
             tx.finish();
         }
 
         return callNode;
     }
 
     /**
      * Return type of dataset.
      * 
      * @param datasetNode dataset node
      * @param service neoservice if null then transaction do not created
      * @return DriveTypes or null
      */
     public static DriveTypes getDatasetType(Node datasetNode, GraphDatabaseService service) {
         Transaction tx = beginTx(service);
         try {
             String typeId = (String)datasetNode.getProperty(INeoConstants.DRIVE_TYPE, null);
             return DriveTypes.findById(typeId);
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * Checks if is virtual dataset.
      * 
      * @param datasetNode the dataset node
      * @return true, if is virtual dataset
      */
     public static boolean isVirtualDataset(Node datasetNode) {
         return getNodeType(datasetNode, "").equals(NodeTypes.DATASET.getId()) && datasetNode.hasRelationship(GeoNeoRelationshipTypes.VIRTUAL_DATASET, Direction.INCOMING);
     }
 
     /**
      * Gets all dataset nodes.
      * 
      * @param service neoservice can not be null
      * @return Map
      */
     public static LinkedHashMap<String, Node> getAllDatasetNodes(GraphDatabaseService service) {
         Transaction tx = beginTx(service);
         LinkedHashMap<String, Node> result = new LinkedHashMap<String, Node>();
         try {
             Traverser traverser = service.getReferenceNode().traverse(Order.DEPTH_FIRST, getStopEvaluator(3), new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     Node node = currentPos.currentNode();
                     return isDatasetNode(node);
                 }
             }, NetworkRelationshipTypes.CHILD, Direction.OUTGOING, GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING, GeoNeoRelationshipTypes.VIRTUAL_DATASET, Direction.OUTGOING);
             for (Node node : traverser) {
                 result.put(getNodeName(node, service), node);
             }
             return result;
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * Find or create virtual dataset node.
      * 
      * @param realDatasetNode the real dataset node
      * @param virtualDatasetName the virtual dataset name
      * @param neo the neo
      * @return the node
      */
     public static Node findOrCreateVirtualDatasetNode(Node realDatasetNode, final String virtualDatasetName, final GraphDatabaseService neo) {
         Node virtualDataset = null;
         Transaction tx = neo.beginTx();
         try {
             Iterator<Node> virtualDatasetsIterator = realDatasetNode.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     return virtualDatasetName.equals(getNodeName(currentPos.currentNode(), neo));
                 }
             }, GeoNeoRelationshipTypes.VIRTUAL_DATASET, Direction.OUTGOING).iterator();
 
             if (virtualDatasetsIterator.hasNext()) {
                 virtualDataset = virtualDatasetsIterator.next();
             } else {
                 virtualDataset = neo.createNode();
                 virtualDataset.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.DATASET.getId());
                 virtualDataset.setProperty(INeoConstants.PROPERTY_NAME_NAME, virtualDatasetName);
 
                 realDatasetNode.createRelationshipTo(virtualDataset, GeoNeoRelationshipTypes.VIRTUAL_DATASET);
             }
             tx.success();
             return virtualDataset;
         } catch (Exception e) {
             tx.failure();
             LOGGER.error(e.getLocalizedMessage(), e);
             return null;
         } finally {
             tx.finish();
         }
 
     }
 
     /**
      * Find or create virtual dataset node.
      * 
      * @param realDatasetNode the real dataset node
      * @param driveType the drive type
      * @param neo the neo
      * @return the node
      */
     public static Node findOrCreateVirtualDatasetNode(Node realDatasetNode, DriveTypes driveType, final GraphDatabaseService neo) {
 
         String realDatasetName = getNodeName(realDatasetNode, neo);
         final String virtualDatasetName = driveType.getFullDatasetName(realDatasetName);
         Node node = findOrCreateVirtualDatasetNode(realDatasetNode, virtualDatasetName, neo);
         DriveTypes nodeType = DriveTypes.getNodeType(node, neo);
         if (nodeType == null) {
             driveType.setTypeToNode(node, neo);
         } else {
             assert nodeType == driveType;
         }
         return node;
     }
 
     /**
      * Creates the gis node.
      * 
      * @param parent the parent
      * @param gisName the gis name
      * @param gisType the gis type
      * @param neo the neo
      * @return the node
      */
     public static Node createGISNode(Node parent, String gisName, String gisType, GraphDatabaseService neo) {
         Node gisNode = null;
 
         Transaction transaction = beginTx(neo);
         try {
             gisNode = neo.createNode();
             gisNode.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.GIS.getId());
             gisNode.setProperty(INeoConstants.PROPERTY_NAME_NAME, gisName);
             gisNode.setProperty(INeoConstants.PROPERTY_GIS_TYPE_NAME, gisType);
             parent.createRelationshipTo(gisNode, NetworkRelationshipTypes.CHILD);
 
             transaction.success();
         } catch (Exception e) {
 
             transaction.failure();
             LOGGER.error(e.getLocalizedMessage(), e);
         } finally {
             transaction.finish();
         }
 
         return gisNode;
     }
 
     /**
      * Gets the all dataset nodes by type.
      * 
      * @param datasetType the dataset type
      * @param service the service
      * @return the all dataset nodes by type
      */
     public static LinkedHashMap<String, Node> getAllDatasetNodesByType(final DriveTypes datasetType, GraphDatabaseService service) {
         Transaction tx = beginTx(service);
         LinkedHashMap<String, Node> result = new LinkedHashMap<String, Node>();
         try {
             Traverser traverser = service.getReferenceNode().traverse(Order.DEPTH_FIRST, getStopEvaluator(3), new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     Node node = currentPos.currentNode();
                     return isDatasetNode(node) && getDatasetType(node, null) == datasetType;
                 }
             }, NetworkRelationshipTypes.CHILD, Direction.OUTGOING, GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING, GeoNeoRelationshipTypes.VIRTUAL_DATASET, Direction.OUTGOING);
             for (Node node : traverser) {
                 result.put(getNodeName(node), node);
             }
             return result;
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * Get traverser by child of node (one child ).
      * 
      * @param rootNode - root node;
      * @return Traverser
      */
     public static Traverser getChildTraverser(Node rootNode) {
         return getChildTraverser(rootNode, StopEvaluator.END_OF_GRAPH, ReturnableEvaluator.ALL);
     }
 
     /**
      * Gets the child traverser.
      * 
      * @param rootNode the root node
      * @param stopEvaluator the stop evaluator
      * @return the child traverser
      */
     public static Traverser getChildTraverser(Node rootNode, final StopEvaluator stopEvaluator) {
         return getChildTraverser(rootNode, stopEvaluator, ReturnableEvaluator.ALL);
     }
 
     /**
      * Gets the child traverser.
      * 
      * @param rootNode the root node
      * @param returnableEvaluator the returnable evaluator
      * @return the child traverser
      */
     public static Traverser getChildTraverser(Node rootNode, final ReturnableEvaluator returnableEvaluator) {
         return getChildTraverser(rootNode, StopEvaluator.END_OF_GRAPH, returnableEvaluator);
     }
 
     /**
      * Get traverser by child of node (one child ).
      * 
      * @param rootNode - root node;
      * @param stopEvaluator the stop evaluator
      * @param returnableEvaluator the returnable evaluator
      * @return Traverser
      */
     public static Traverser getChildTraverser(Node rootNode, final StopEvaluator stopEvaluator, final ReturnableEvaluator returnableEvaluator) {
         Iterator<Relationship> relations = rootNode.getRelationships(GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING).iterator();
         // TODO add check on single relations?
         if (relations.hasNext()) {
             return relations.next().getOtherNode(rootNode).traverse(Order.DEPTH_FIRST, stopEvaluator, returnableEvaluator, GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING);
         } else {
             return emptyTraverser(rootNode);
         }
     }
 
     /**
      * return empty traverser.
      * 
      * @param rootNode - root node
      * @return empty traverser
      */
     public static Traverser emptyTraverser(Node rootNode) {
         return rootNode.traverse(Order.DEPTH_FIRST, new StopEvaluator() {
 
             @Override
             public boolean isStopNode(TraversalPosition currentPos) {
                 return true;
             }
         }, new ReturnableEvaluator() {
 
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 return false;
             }
         }, GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING);
     }
 
     /**
      * get parent of current node.
      * 
      * @param service NeoService if null then new transaction created
      * @param childNode child node
      * @return parent node or null
      */
     public static Node getParent(GraphDatabaseService service, Node childNode) {
         Transaction tx = beginTx(service);
         try {
             Iterator<Node> parentIterator = childNode.traverse(Order.BREADTH_FIRST, new StopEvaluator() {
 
                 @Override
                 public boolean isStopNode(TraversalPosition currentPos) {
                     return currentPos.lastRelationshipTraversed() != null && currentPos.lastRelationshipTraversed().isType(GeoNeoRelationshipTypes.CHILD);
                 }
             }, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     return currentPos.lastRelationshipTraversed() != null && currentPos.lastRelationshipTraversed().isType(GeoNeoRelationshipTypes.CHILD);
                 }
             }, GeoNeoRelationshipTypes.CHILD, Direction.INCOMING, GeoNeoRelationshipTypes.NEXT, Direction.INCOMING).iterator();
             return parentIterator.hasNext() ? parentIterator.next() : null;
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * Return type of gis.
      * 
      * @param gisNode GIS node
      * @param service neoservice if null then transaction do not created
      * @return GisTypes or null
      */
     public static GisTypes getGisType(Node gisNode, GraphDatabaseService service) {
         Transaction tx = beginTx(service);
         try {
             String typeId = (String)gisNode.getProperty(INeoConstants.PROPERTY_GIS_TYPE_NAME, "");
             return GisTypes.findGisTypeByHeader(typeId);
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * Return location node of current drive node.
      * 
      * @param node - drive node
      * @param service neoservice if null then transaction do not created
      * @return location node or null
      */
     public static Node getLocationNode(Node node, GraphDatabaseService service) {
         Transaction tx = beginTx(service);
         try {
             return node.hasRelationship(GeoNeoRelationshipTypes.LOCATION, Direction.OUTGOING) ? node.getSingleRelationship(GeoNeoRelationshipTypes.LOCATION, Direction.OUTGOING)
                     .getOtherNode(node) : null;
         } catch (Exception e) {
             // TODO: FAST FAKE - because some nodes can few location - exception is possible. fix
             // after difinition mobile probe.
             return null;
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * Calculates is Probe has Calls of choosen type in current dataset.
      * 
      * @param datasetNode Dataset to check
      * @param type type of Calls
      * @param probeName name of Probe
      * @return true, if successful
      */
     public static boolean hasCallsOfType(Node datasetNode, CallType type, final String probeName) {
         final String finalName = type.getProperty();
 
         Iterator<Node> probeCalls = datasetNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
 
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 return (Boolean)currentPos.currentNode().getProperty(finalName, false)
                         && probeName.equals(getNodeName(currentPos.currentNode().getSingleRelationship(ProbeCallRelationshipType.CALLS, Direction.INCOMING).getStartNode()));
             }
         }, ProbeCallRelationshipType.PROBE_DATASET, Direction.OUTGOING).iterator();
 
         return probeCalls.hasNext();
     }
 
     /**
      * Returns all Probe Nodes related to Dataset.
      * 
      * @param datasetNode Dataset Node
      * @param type the type
      * @return all Probe Nodes
      */
     public static Collection<Node> getAllProbesOfDataset(Node datasetNode, CallType type) {
         final String finalName = type.getProperty();
 
         ArrayList<Node> nodes = new ArrayList<Node>();
         Iterator<Node> probeCalls = datasetNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
 
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 return (Boolean)currentPos.currentNode().getProperty(finalName, false);
             }
         }, ProbeCallRelationshipType.PROBE_DATASET, Direction.OUTGOING).iterator();
 
         while (probeCalls.hasNext()) {
             nodes.add(probeCalls.next().getSingleRelationship(ProbeCallRelationshipType.CALLS, Direction.INCOMING).getStartNode());
         }
 
         return nodes;
     }
 
     /**
      * Get location pair.
      * 
      * @param locationNode - location node
      * @param service neoservice if null then transaction do not created
      * @return Pair
      */
     public static Pair<Double, Double> getLocationPair(Node locationNode, GraphDatabaseService service) {
         Transaction tx = beginTx(service);
         try {
             Double lat = (Double)locationNode.getProperty(INeoConstants.PROPERTY_LAT_NAME, null);
             Double lon = (Double)locationNode.getProperty(INeoConstants.PROPERTY_LON_NAME, null);
             Pair<Double, Double> result = new Pair<Double, Double>(lat, lon);
             return result;
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * Check transaction on thread.
      * 
      * @param service the service
      * @param description the description
      */
     public static void checkTransactionOnThread(GraphDatabaseService service, String description) {
         service = service == null ? NeoServiceProvider.getProvider().getService() : service;
         Transaction tx = beginTx(service);
         addTransactionLog(tx, Thread.currentThread(), description);
         tx.finish();
     }
 
     /**
      * get format date string.
      * 
      * @param startTime - begin timestamp
      * @param endTime end timestamp
      * @param dayFormat the day format
      * @return the format date string
      */
     public static String getFormatDateString(Long startTime, Long endTime, String dayFormat) {
         if (startTime == null || endTime == null) {
             return "No time";
         }
         Calendar endTimeCal = Calendar.getInstance();
         endTimeCal.setTimeInMillis(endTime);
 
         Calendar startTimeCal = Calendar.getInstance();
         startTimeCal.setTimeInMillis(startTime);
 
         String pattern = "yyyy-MM-dd " + dayFormat;
         SimpleDateFormat sf = new SimpleDateFormat(pattern);
 
         StringBuilder sb = new StringBuilder();
         if (endTime - startTime <= 24 * 60 * 60 * 1000 && startTimeCal.get(Calendar.DAY_OF_WEEK) == endTimeCal.get(Calendar.DAY_OF_WEEK)) {
             SimpleDateFormat sf2 = new SimpleDateFormat(dayFormat);
             sb.append(sf.format(startTimeCal.getTime()));
             sb.append("-").append(sf2.format(endTimeCal.getTime()));
         } else {
             SimpleDateFormat sfMulDay2 = new SimpleDateFormat(pattern);
             sb.append(sf.format(startTimeCal.getTime()));
             sb.append(" to ").append(sfMulDay2.format(endTimeCal.getTime()));
         }
         return sb.toString();
     }
 
     /**
      * get format date string.
      * 
      * @param startTime - begin timestamp
      * @param endTime end timestamp
      * @param dayFormat the day format
      * @param periodId the period id
      * @return the format date string
      */
     public static String getFormatDateStringForSrow(Long startTime, Long endTime, String dayFormat, String periodId) {
         if (startTime == null || endTime == null) {
             return "No time";
         }
         if (periodId.equals("hourly")) {
             return getNameForHourlySRow(startTime, endTime, dayFormat);
         }
         if (periodId.equals("daily")) {
             return getNameForDailySRow(startTime);
         }
         if (periodId.equals("weekly")) {
             return getNameForWeeklySRow(startTime, endTime);
         }
         return getNameForMonthlySRow(startTime, endTime);
     }
 
     /**
      * Gets the name for hourly s row.
      * 
      * @param startTime the start time
      * @param endTime the end time
      * @param dayFormat the day format
      * @return the name for hourly s row
      */
     public static String getNameForHourlySRow(Long startTime, Long endTime, String dayFormat) {
         Calendar endTimeCal = Calendar.getInstance();
         endTimeCal.setTimeInMillis(endTime);
 
         Calendar startTimeCal = Calendar.getInstance();
         startTimeCal.setTimeInMillis(startTime);
 
         String pattern = "yyyy-MM-dd " + dayFormat;
         SimpleDateFormat sf = new SimpleDateFormat(pattern);
 
         StringBuilder sb = new StringBuilder();
         if (startTimeCal.get(Calendar.DAY_OF_WEEK) == endTimeCal.get(Calendar.DAY_OF_WEEK)) {
             SimpleDateFormat sf2 = new SimpleDateFormat(dayFormat);
             sb.append(sf.format(startTimeCal.getTime()));
             sb.append("-").append(sf2.format(endTimeCal.getTime()));
         } else {
             SimpleDateFormat sfMulDay2 = new SimpleDateFormat(pattern);
             sb.append(sf.format(startTimeCal.getTime()));
             sb.append(" to ").append(sfMulDay2.format(endTimeCal.getTime()));
         }
         return sb.toString();
     }
 
     /**
      * Gets the name for daily s row.
      * 
      * @param startTime the start time
      * @return the name for daily s row
      */
     public static String getNameForDailySRow(Long startTime) {
         String pattern = "yyyy-MM-dd";
         SimpleDateFormat sf = new SimpleDateFormat(pattern);
         return sf.format(new Date(startTime));
     }
 
     /**
      * Gets the name for weekly s row.
      * 
      * @param startTime the start time
      * @param endTime the end time
      * @return the name for weekly s row
      */
     public static String getNameForWeeklySRow(Long startTime, Long endTime) {
         Calendar startTimeCal = Calendar.getInstance();
         startTimeCal.setTimeInMillis(startTime);
         String result = "Week " + startTimeCal.get(Calendar.WEEK_OF_YEAR);
         if (isNeedAddYear(startTime, endTime)) {
             result = result + ", " + startTimeCal.get(Calendar.YEAR);
         }
         return result;
     }
 
     /**
      * Gets the name for monthly s row.
      * 
      * @param startTime the start time
      * @param endTime the end time
      * @return the name for monthly s row
      */
     public static String getNameForMonthlySRow(Long startTime, Long endTime) {
         Calendar startTimeCal = Calendar.getInstance();
         startTimeCal.setTimeInMillis(startTime);
         int month = startTimeCal.get(Calendar.MONTH);
         boolean needYear = isNeedAddYear(startTime, endTime);
         int year = startTimeCal.get(Calendar.YEAR);
         switch (month) {
         case Calendar.JANUARY:
             return "January" + (needYear ? (" " + year) : "");
         case Calendar.FEBRUARY:
             return "February" + (needYear ? (" " + year) : "");
         case Calendar.MARCH:
             return "March" + (needYear ? (" " + year) : "");
         case Calendar.APRIL:
             return "April" + (needYear ? (" " + year) : "");
         case Calendar.MAY:
             return "May" + (needYear ? (" " + year) : "");
         case Calendar.JUNE:
             return "June" + (needYear ? (" " + year) : "");
         case Calendar.JULY:
             return "July" + (needYear ? (" " + year) : "");
         case Calendar.AUGUST:
             return "August" + (needYear ? (" " + year) : "");
         case Calendar.SEPTEMBER:
             return "September" + (needYear ? (" " + year) : "");
         case Calendar.OCTOBER:
             return "October" + (needYear ? (" " + year) : "");
         case Calendar.NOVEMBER:
             return "November" + (needYear ? (" " + year) : "");
         case Calendar.DECEMBER:
             return "December" + (needYear ? (" " + year) : "");
         default:
             return "Month " + month + (needYear ? (" " + year) : "");
         }
     }
 
     /**
      * Checks if is need add year.
      * 
      * @param start the start
      * @param end the end
      * @return true, if is need add year
      */
     private static boolean isNeedAddYear(Long start, Long end) {
         Calendar currTimeCal = Calendar.getInstance();
         currTimeCal.setTimeInMillis(System.currentTimeMillis());
         int currYear = currTimeCal.get(Calendar.YEAR);
 
         Calendar startTimeCal = Calendar.getInstance();
         startTimeCal.setTimeInMillis(start);
 
         int startYear = startTimeCal.get(Calendar.YEAR);
         if (startYear != currYear) {
             return true;
         }
 
         Calendar endTimeCal = Calendar.getInstance();
         endTimeCal.setTimeInMillis(end);
         int endYear = endTimeCal.get(Calendar.YEAR);
         return startYear != endYear;
     }
 
 
 
     /**
      * Find multi property index.
      * 
      * @param indexName the index name
      * @param neoService the neo service
      * @return the node
      */
     public static Node findMultiPropertyIndex(final String indexName, final GraphDatabaseService neoService) {
         Iterator<Node> indexNodes = neoService.getReferenceNode().traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 return indexName.equals(getNodeName(currentPos.currentNode(), neoService));
             }
         }, PropertyIndex.NeoIndexRelationshipTypes.INDEX, Direction.OUTGOING).iterator();
 
         if (indexNodes.hasNext()) {
             return indexNodes.next();
         } else {
             return null;
         }
     }
 
 
 
     /**
      * Gets all OSS nodes.
      * 
      * @param service the service
      * @return the all oss
      */
     public static Collection<Node> getAllOss(GraphDatabaseService service) {
         Transaction tx = beginTx(service);
         try {
             return getAllRootTraverser(service, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     return NodeTypes.OSS.checkNode(currentPos.currentNode());
                 }
             }).getAllNodes();
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * Gets all OSS nodes.
      * 
      * @param service the service
      * @return the all gpeh
      */
     public static Collection<Node> getAllGpeh(GraphDatabaseService service) {
         Transaction tx = beginTx(service);
         try {
             return getAllRootTraverser(service, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     return isDatasetNode(currentPos.currentNode()) && DriveTypes.OSS.getId().equals(currentPos.currentNode().getProperty(INeoConstants.DRIVE_TYPE, null));
                 }
             }).getAllNodes();
 
             // return getAllReferenceChild(service, new ReturnableEvaluator() {
             //
             // @Override
             // public boolean isReturnableNode(TraversalPosition currentPos) {
             // Node node = currentPos.currentNode();
             // boolean res1 = NodeTypes.DATASET.checkNode(node);
             // boolean res2 = OssType.GPEH.checkNode(node);
             // boolean result = NodeTypes.DATASET.checkNode(node) && OssType.GPEH.checkNode(node);
             // return res1 && res2;
             // }
             // }).getAllNodes();
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * Gets all childs of reference node.
      * 
      * @param service the service
      * @param evaluator the evaluator
      * @return the all reference child
      */
     public static Traverser getAllReferenceChild(GraphDatabaseService service, final ReturnableEvaluator evaluator) {
         Transaction tx = beginTx(service);
         try {
             return service.getReferenceNode().traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, evaluator, GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING);
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * add child to node.
      * 
      * @param mainNode root node
      * @param subNode child node
      * @param lastChild - last child node (in not known or mainNode do not have childs lastChild
      *        should be= null)
      * @param neo - neoservice
      */
     public static void addChild(Node mainNode, Node subNode, Node lastChild, GraphDatabaseService neo) {
         Transaction tx = beginTx(neo);
         try {
             if (lastChild == null) {
                 lastChild = findLastChild(mainNode, neo);
             }
             if (lastChild == null) {
                 mainNode.createRelationshipTo(subNode, GeoNeoRelationshipTypes.CHILD);
             } else {
                 lastChild.createRelationshipTo(subNode, GeoNeoRelationshipTypes.NEXT);
             }
             // save last child like properti in main node
             mainNode.setProperty(INeoConstants.LAST_CHILD_ID, subNode.getId());
             successTx(tx);
         } finally {
             finishTx(tx);
         }
 
     }
 
     /**
      * return last child of node.
      * 
      * @param mainNode root node
      * @param neo service
      * @return the node
      */
     public static Node findLastChild(Node mainNode, GraphDatabaseService neo) {
         Transaction tx = beginTx(neo);
         try {
             Long lastChild = (Long)mainNode.getProperty(INeoConstants.LAST_CHILD_ID, null);
             if (lastChild != null) {
                 Node result = neo.getNodeById(lastChild);
                 assert !result.hasRelationship(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING);
                 return result;
             }
             Iterator<Node> iterator = getChildTraverser(mainNode, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     return !currentPos.currentNode().hasRelationship(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING);
                 }
             }).iterator();
             return iterator.hasNext() ? iterator.next() : null;
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * This code finds the specified network node in the database, creating its own transaction for
      * that.
      * 
      * @param gisNode the gis node
      * @param basename the basename
      * @param filename the filename
      * @param neo the neo
      * @return the node
      */
     public static Node findOrCreateNetworkNode(Node gisNode, String basename, String filename, GraphDatabaseService neo) {
         Node network;
         Transaction tx = neo.beginTx();
         try {
             Relationship relation = gisNode.getSingleRelationship(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING);
             if (relation != null) {
                 return relation.getOtherNode(gisNode);
             }
             NetworkTypes type = NetworkTypes.getNodeType(gisNode);
             network = neo.createNode();
             if (type != null) {
                 type.setTypeToNode(network, neo);
             }
             network.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.NETWORK.getId());
             network.setProperty(INeoConstants.PROPERTY_NAME_NAME, basename);
             network.setProperty(INeoConstants.PROPERTY_FILENAME_NAME, filename);
             gisNode.createRelationshipTo(network, GeoNeoRelationshipTypes.NEXT);
             tx.success();
         } finally {
             tx.finish();
         }
         return network;
     }
 
     /**
      * find Or Create Filter root node.
      * 
      * @param service NeoService
      * @return filter root node
      */
     public static Node findOrCreateFilterRootNode(GraphDatabaseService service) {
         Transaction tx = beginTx(service);
         try {
             Node referenceNode = service.getReferenceNode();
             Relationship relation = referenceNode.getSingleRelationship(GeoNeoRelationshipTypes.FILTERS, Direction.OUTGOING);
             if (relation != null) {
                 return relation.getOtherNode(referenceNode);
             }
             Node node = service.createNode();
             NodeTypes.FILTER_ROOT.setNodeType(node, service);
             node.setProperty(INeoConstants.PROPERTY_NAME_NAME, "ROOT");
             referenceNode.createRelationshipTo(node, GeoNeoRelationshipTypes.FILTERS);
             successTx(tx);
             return node;
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Sets node name.
      * 
      * @param node node
      * @param name - name
      * @param service NeoService - if null then used current transaction
      */
     public static void setNodeName(Node node, String name, GraphDatabaseService service) {
         Transaction tx = beginTx(service);
         try {
             if (name != null) {
                 node.setProperty(INeoConstants.PROPERTY_NAME_NAME, name);
             } else {
                 node.removeProperty(INeoConstants.PROPERTY_NAME_NAME);
             }
             successTx(tx);
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * Gets relationsip type by type name.
      * 
      * @param relation the relation
      * @return DeletableRelationshipType
      */
     public static RelationshipType getRelationType(Relationship relation) {
         String etalonName = relation.getType().name();
         RelationshipType result = findType(Arrays.asList(GeoNeoRelationshipTypes.values()), etalonName);
         if (result != null) {
             return result;
         }
         result = findType(Arrays.asList(NetworkRelationshipTypes.values()), etalonName);
         if (result != null) {
             return result;
         }
         result = findType(Arrays.asList(ProbeCallRelationshipType.values()), etalonName);
         if (result != null) {
             return result;
         }
         result = findType(Arrays.asList(SplashRelationshipTypes.values()), etalonName);
         if (result != null) {
             return result;
         }
         result = findType(Arrays.asList(NeoIndexRelationshipTypes.values()), etalonName);
         if (result != null) {
             return result;
         }
         throw new IllegalArgumentException("Relationship type <" + etalonName + "> does not instanseof DeletableRelationshipType.");
     }
 
     /**
      * Find relationship type.
      * 
      * @param types List of types
      * @param etalonName String
      * @return DeletableRelationshipType
      */
     private static RelationshipType findType(List< ? extends RelationshipType> types, String etalonName) {
         for (RelationshipType type : types) {
             if (type.name().equals(etalonName)) {
                 return type;
             }
         }
         return null;
     }
 
 
 
 
 
 
 
 
     /**
      * Find sector by next rules: baseName must be defined, ci or name must be defined (lac used
      * only if ci!=null).
      * 
      * @param baseNode the base node
      * @param ci the ci
      * @param lac the lac
      * @param name the name
      * @param returnFirsElement -will be handled only if (ci==null||(ac==null&&name==null)) true: if
      *        result has multiple nodes - return first; false=if result has multiple nodes - return
      *        null
      * @param index the index
      * @param service the service
      * @return the node
      */
     public static Node findSector(Node baseNode, Integer ci, Integer lac, String name, boolean returnFirsElement, IndexService index, GraphDatabaseService service) {
         if (baseNode == null || (ci == null && name == null)) {
             return null;
         }
         assert baseNode != null && (ci != null || name != null) && index != null;
         Transaction tx = beginTx(service);
         try {
             if (ci == null) {
                 String indexName = getLuceneIndexKeyByProperty(baseNode, INeoConstants.PROPERTY_NAME_NAME, NodeTypes.SECTOR);
                 IndexHits<Node> nodesName = index.getNodes(indexName, name);
                 Node sector = nodesName.size() > 0 ? nodesName.next() : null;
                 if (nodesName.size() == 1) {
                     nodesName.close();
                     return sector;
                 } else {
                     nodesName.close();
                     return returnFirsElement ? sector : null;
                 }
             }
             String indexName = getLuceneIndexKeyByProperty(baseNode, INeoConstants.PROPERTY_SECTOR_CI, NodeTypes.SECTOR);
             IndexHits<Node> nodesCi = index.getNodes(indexName, ci);
             if (lac == null && name == null) {
                 Node sector = nodesCi.size() > 0 ? nodesCi.next() : null;
                 if (nodesCi.size() == 1) {
                     nodesCi.close();
                     return sector;
                 } else {
                     nodesCi.close();
                     return returnFirsElement ? sector : null;
                 }
             }
             boolean canCheck = true;
             if (lac != null) {
                 canCheck = false;
                 for (Node sector : nodesCi) {
                     if (sector.hasProperty(INeoConstants.PROPERTY_SECTOR_LAC)) {
                         if (sector.getProperty(INeoConstants.PROPERTY_SECTOR_LAC).equals(lac)) {
                             nodesCi.close();
                             return sector;
                         }
                     } else {
                         canCheck = true;
                     }
                 }
             }
             if (canCheck) {
                 if (lac != null) {
                     nodesCi.close();
                     nodesCi = index.getNodes(indexName, ci);
                 }
                 if (name != null) {
                     for (Node sector : nodesCi) {
                         Object sectorName = sector.getProperty(INeoConstants.PROPERTY_NAME_NAME, null);
                         if (name.equals(sectorName) && (lac == null || !sector.hasProperty(INeoConstants.PROPERTY_SECTOR_LAC))) {
                             return sector;
                         }
                     }
                 }
             }
             return null;
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * Find sector.
      * 
      * @param baseNode the base name
      * @param ci the ci
      * @param rnc the rnc
      * @param luceneService the lucene service
      * @param service the service
      * @return the node
      */
     public static Node findSector(Node baseNode, Integer ci, String rnc, LuceneIndexService luceneService, GraphDatabaseService service) {
         assert baseNode != null && ci != null && rnc != null && luceneService != null;
         Transaction tx = beginTx(service);
         try {
             String indexName = getLuceneIndexKeyByProperty(baseNode, INeoConstants.PROPERTY_SECTOR_CI, NodeTypes.SECTOR);
             IndexHits<Node> nodesCi = luceneService.getNodes(indexName, ci);
             for (Node node : nodesCi) {
                 Object rncId = node.getProperty(GpehReportUtil.RNC_ID, null);
                 if (rnc.equals(rncId)) {
                     return node;
                 }
             }
             return null;
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * Gets the relationship type by name.
      * 
      * @param name the name
      * @return the relationship type by name
      */
     public static RelationshipType getRelationshipTypeByName(final String name) {
         return new RelationshipType() {
 
             @Override
             public String name() {
                 return name;
             }
         };
     }
 
     /**
      * Find root node.
      * 
      * @param node the node - node from datast/network
      * @param service the service
      * @return the node
      */
     public static Node findRoot(Node node, GraphDatabaseService service) {
         Transaction tx = beginTx(service);
         try {
             if (isRoootNode(node)) {
                 return node;
             } else if (isGisNode(node)) {
                 return node.getSingleRelationship(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING).getOtherNode(node);
             } else {
                 Iterator<Node> rootIterator = node.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
 
                     @Override
                     public boolean isReturnableNode(TraversalPosition currentPos) {
                         return isRoootNode(currentPos.currentNode());
                     }
                 }, NetworkRelationshipTypes.CHILD, Direction.INCOMING, GeoNeoRelationshipTypes.NEXT, Direction.INCOMING, GeoNeoRelationshipTypes.LOCATION, Direction.INCOMING,
                         GeoNeoRelationshipTypes.VIRTUAL_DATASET, Direction.INCOMING).iterator();
                 tx.success();
                 return rootIterator.hasNext() ? rootIterator.next() : null;
             }
         } finally {
             finishTx(tx);
         }
     }
 
 
     /**
      * Gets the primary type of dataset node.
      * 
      * @param dataNode the root node
      * @return the primary type or null
      */
     public static String getPrimaryType(Node dataNode) {
         return (String)dataNode.getProperty(INeoConstants.PRIMARY_TYPE_ID, null);
     }
 
     /**
      * Sets the primary type.
      * 
      * @param dataNode the data node
      * @param primaryType the primary type
      * @param service the service
      */
     public static void setPrimaryType(Node dataNode, String primaryType, GraphDatabaseService service) {
         setPropertyToNode(dataNode, INeoConstants.PRIMARY_TYPE_ID, primaryType, service);
     }
 
     /**
      * Sets the property to node.
      * 
      * @param node the node
      * @param propertyName the property name
      * @param value the value
      * @param service the service
      */
     private static void setPropertyToNode(Node node, String propertyName, Object value, GraphDatabaseService service) {
         Transaction tx = beginTx(service);
         try {
             if (value == null) {
                 node.removeProperty(propertyName);
             } else {
                 node.setProperty(INeoConstants.PRIMARY_TYPE_ID, value);
             }
             successTx(tx);
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * Gets the traverser of all datasets by network node.
      * 
      * @param networkNode network node
      * @param service the service
      * @return Traverser
      */
     public static Traverser getAllCorrelatedDatasets(Node networkNode, GraphDatabaseService service) {
         // Transaction tx = beginTx(service);
         try {
             return networkNode.traverse(Order.DEPTH_FIRST, getStopEvaluator(2), new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     return currentPos.notStartNode() && currentPos.lastRelationshipTraversed().isType(CorrelationRelationshipTypes.CORRELATED);
 
                 }
             }, CorrelationRelationshipTypes.CORRELATION, Direction.OUTGOING, CorrelationRelationshipTypes.CORRELATED, Direction.INCOMING);
         } finally {
             // finishTx(tx);
         }
 
     }
 
     /**
      * Sets the crs.
      * 
      * @param mainGisNode the main gis node
      * @param crs the crs
      * @param service the service
      */
     public static void setCRS(Node mainGisNode, CoordinateReferenceSystem crs, GraphDatabaseService service) {
         Transaction tx = beginTx(service);
         try {
             String type = "geographic";
             String epsg = crs.getIdentifiers().iterator().next().toString();
             mainGisNode.setProperty(INeoConstants.PROPERTY_WKT_CRS, crs.toWKT());
             mainGisNode.setProperty(INeoConstants.PROPERTY_CRS_TYPE_NAME, type);
             mainGisNode.setProperty(INeoConstants.PROPERTY_CRS_NAME, epsg);
             successTx(tx);
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * Gets the cRS.
      * 
      * @param gisNode the gis node
      * @param defaultCRS the default crs
      * @return the cRS
      */
     public static CoordinateReferenceSystem getCRS(Node gisNode, CoordinateReferenceSystem defaultCRS) {
         CoordinateReferenceSystem crs = defaultCRS; // default if crs cannot be found below
         try {
             if (gisNode.hasProperty(INeoConstants.PROPERTY_WKT_CRS)) {
                 crs = CRS.parseWKT((String)gisNode.getProperty(INeoConstants.PROPERTY_WKT_CRS));
             } else if (gisNode.hasProperty(INeoConstants.PROPERTY_CRS_NAME)) {
                 // The simple approach is to name the CRS, eg. EPSG:4326 (GeoNeo spec prefers a
                 // new naming standard, but I'm not sure geotools knows it)
                 crs = CRS.decode(gisNode.getProperty(INeoConstants.PROPERTY_CRS_NAME).toString());
             } else if (gisNode.hasProperty(INeoConstants.PROPERTY_CRS_HREF_NAME)) {
                 // TODO: This type is specified in GeoNeo spec, but what the HREF means is not,
                 // so we assume it is a live URL that will feed a CRS specification directly
                 // TODO: Lagutko: gisNode.hasProperty() has 'crs_href' as parameter, but
                 // gisNode.getProperty() has only 'href'. What is right?
                 URL crsURL = new URL(gisNode.getProperty(INeoConstants.PROPERTY_CRS_HREF_NAME).toString());
                 crs = CRS.decode(crsURL.getContent().toString());
             }
         } catch (Exception crs_e) {
             System.err.println("Failed to interpret CRS: " + crs_e.getMessage());
             crs_e.printStackTrace(System.err);
         }
         return crs;
     }
 
     /**
      * Gets the main node from gis.
      * 
      * @param gisNode the gis node
      * @param service the service
      * @return the main node from gis
      */
     public static Node getMainNodeFromGis(Node gisNode, GraphDatabaseService service) {
         Transaction tx = beginTx(service);
         try {
             Relationship rel = gisNode.getSingleRelationship(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING);
             return rel == null ? null : rel.getOtherNode(gisNode);
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Gets the gis node by dataset.
      * 
      * @param dataset the dataset
      * @return the gis node by dataset
      */
     public static Node getGisNodeByDataset(Node dataset) {
         return dataset.getSingleRelationship(GeoNeoRelationshipTypes.NEXT, Direction.INCOMING).getStartNode();
     }
 
     /**
      * Gets the dataset node by gis.
      * 
      * @param gis the gis
      * @return the dataset node by gis
      */
     public static Node getDatasetNodeByGis(Node gis) {
         return gis.getSingleRelationship(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING).getEndNode();
     }
 
     /**
      * Returns list of Networks correlated with Dataset
      * 
      * @param dataset Dataset to search from
      * @param service NeoService
      * @return correlated Networks
      */
     public static List<Node> getCorrelationNetworks(Node dataset, GraphDatabaseService service) {
         Transaction tx = service.beginTx();
         ArrayList<Node> result = new ArrayList<Node>();
 
         try {
             for (Relationship correlationRel : dataset.getRelationships(CorrelationRelationshipTypes.CORRELATED, Direction.OUTGOING)) {
                 Node network = correlationRel.getEndNode().getSingleRelationship(CorrelationRelationshipTypes.CORRELATION, Direction.INCOMING).getStartNode();
 
                 result.add(getGisNodeByDataset(network));
             }
         } catch (Exception e) {
             LOGGER.error(e.getLocalizedMessage(), e);
         } finally {
             tx.success();
             tx.finish();
         }
 
         return result;
     }
 
     /**
      * Gets the primary elem traverser.
      * 
      * @param node the node
      * @param service the service
      * @return the primary elem traverser
      */
     public static Traverser getPrimaryElemTraverser(Node node, GraphDatabaseService service) {
         final String type = getPrimaryType(node);
         ReturnableEvaluator re = new ReturnableEvaluator() {
 
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 return currentPos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME, "").equals(type);
             }
         };
         return node.traverse(Order.BREADTH_FIRST, StopEvaluator.END_OF_GRAPH, re, GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING, GeoNeoRelationshipTypes.CHILD,
                 Direction.OUTGOING);
 
     }
 
     /**
      * Get calls for probe.
      * 
      * @param node Node
      * @param service GraphDatabaseService
      * @return Set<Node>
      */
     public static Set<Node> getCallsForProbeNode(Node node, GraphDatabaseService service) {
         Transaction tx = service == null ? null : service.beginTx();
         try {
             Relationship relationship = node.getSingleRelationship(ProbeCallRelationshipType.CALLS, Direction.OUTGOING);
             if (relationship == null) {
                 return new HashSet<Node>();
             }
             Node probeCalls = relationship.getEndNode();
             Traverser calls = probeCalls.traverse(Order.BREADTH_FIRST, StopEvaluator.END_OF_GRAPH, ReturnableEvaluator.ALL_BUT_START_NODE, ProbeCallRelationshipType.CALLER,
                     Direction.INCOMING);
             return new HashSet<Node>(calls.getAllNodes());
         } finally {
             if (tx != null) {
                 tx.finish();
             }
         }
     }
 
     /**
      * Get calls for row.
      * 
      * @param node Node
      * @param service GraphDatabaseService
      * @return Set<Node>
      */
     public static Set<Node> getCallsForSRowNode(Node node, GraphDatabaseService service) {
         Transaction tx = service == null ? null : service.beginTx();
         try {
             Set<Node> nodes = new HashSet<Node>();
             Traverser cells = getChildTraverser(node);
             for (Node cell : cells) {
                 nodes.addAll(getCallsForSCellNode(cell, service));
             }
             return nodes;
         } finally {
             if (tx != null) {
                 tx.finish();
             }
         }
     }
 
     /**
      * Get calls for cell.
      * 
      * @param node Node
      * @param service GraphDatabaseService
      * @return Set<Node>
      */
     public static Set<Node> getCallsForSCellNode(Node node, GraphDatabaseService service) {
         Transaction tx = service == null ? null : service.beginTx();
         try {
             Traverser calls = node.traverse(Order.BREADTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     return isCallNode(currentPos.currentNode());
                 }
             }, GeoNeoRelationshipTypes.SOURCE, Direction.OUTGOING);
             return new HashSet<Node>(calls.getAllNodes());
         } finally {
             if (tx != null) {
                 tx.finish();
             }
         }
     }
 
     /**
      * Gets the full name.
      * 
      * @param root the root
      * @param service the servise
      * @return the full name
      */
     public static String getFullName(Node root, GraphDatabaseService service) {
         Transaction tx = beginTx(service);
         try {
             Relationship parentNodeRel = root.getSingleRelationship(GeoNeoRelationshipTypes.CHILD, Direction.INCOMING);
             Node parent = parentNodeRel == null ? null : parentNodeRel.getOtherNode(root);
             return getFullName(parent, root, service);
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * Gets the full name.
      * 
      * @param parent the parent
      * @param root the root
      * @param service the service
      * @return the full name
      */
     private static String getFullName(Node parent, Node root, GraphDatabaseService service) {
         Transaction tx = beginTx(service);
         try {
             StringBuilder result = new StringBuilder();
             if (parent != null) {
                 result.append("(").append(getNodeName(parent, service)).append(")");
             }
             if (root != null) {
                 result.append(getNodeName(root, service));
             }
             return result.toString();
         } finally {
             finishTx(tx);
         }
     }
 
   
 
     /**
      * Gets the gpeh cell name.
      * 
      * @param bestCell the best cell
      * @param service the service
      * @return the gpeh cell name
      */
     public static String getGpehCellName(Node bestCell, GraphDatabaseService service) {
         Transaction tx = beginTx(service);
         try {
             String result = (String)bestCell.getProperty("userLabel", "");
             if (StringUtil.isEmpty(result)) {
                 result = getNodeName(bestCell, service);
             }
             return result;
         } finally {
             finishTx(tx);
         }
     }
 
 
 
 
  
 
 
     /**
      * @param sector the sector whose proxy is to be created
      * @param fileName the name of file
      * @param neighbour the root neighbour node for this file
      * @param service the service
      * @return the proxy sector node
      */
     public static Node createProxySector(Node sector, String fileName, Node neighbour, Node lastSector, RelationshipType type, GraphDatabaseService service) {
         Node proxySector = null;
         Transaction tx = service.beginTx();
         try {
             proxySector = service.createNode();
             String sectorName = sector.getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
             String proxySectorName = fileName + PROXY_NAME_SEPARATOR + sectorName;
             proxySector.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.SECTOR_SECTOR_RELATIONS.getId());
             proxySector.setProperty(INeoConstants.PROPERTY_NAME_NAME, proxySectorName);
 
             if (lastSector == null || lastSector.equals(neighbour))
                 neighbour.createRelationshipTo(proxySector, NetworkRelationshipTypes.CHILD);
             else
                 lastSector.createRelationshipTo(proxySector, NetworkRelationshipTypes.NEXT);
 
             sector.createRelationshipTo(proxySector, type);
             tx.success();
         } catch (Exception e) {
             LOGGER.error(e.getLocalizedMessage(), e);
         } finally {
             tx.finish();
         }
 
         return proxySector;
     }
 
     /**
      * @param site the sector whose proxy is to be created
      * @param fileName the name of file
      * @param neighbour the root neighbour node for this file
      * @param service the service
      * @return the proxy site node
      */
     public static Node createProxySite(Node site, String fileName, Node neighbour, Node lastSite, RelationshipType type, GraphDatabaseService service) {
         Node proxySector = null;
         Transaction tx = service.beginTx();
         try {
             proxySector = service.createNode();
             String sectorName = site.getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
             String proxySectorName = fileName + PROXY_NAME_SEPARATOR + sectorName;
             proxySector.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.SITE_SITE_RELATIONS.getId());
             proxySector.setProperty(INeoConstants.PROPERTY_NAME_NAME, proxySectorName);
 
             if (lastSite == null || lastSite.equals(neighbour))
                 neighbour.createRelationshipTo(proxySector, NetworkRelationshipTypes.CHILD);
             else
                 lastSite.createRelationshipTo(proxySector, NetworkRelationshipTypes.NEXT);
 
             site.createRelationshipTo(proxySector, type);
             tx.success();
         } catch (Exception e) {
             LOGGER.error(e.getLocalizedMessage(), e);
         } finally {
             tx.finish();
         }
 
         return proxySector;
     }
 
     /**
      * @param proxyNode the proxy node
      * @param type Relationship type between proxy node and parent
      * @param service the service
      * @return the parent node
      */
     public static Node getNodeFromProxy(Node proxyNode, RelationshipType type, GraphDatabaseService service) {
         Transaction tx = service.beginTx();
         try {
             return proxyNode.getSingleRelationship(type, Direction.INCOMING).getOtherNode(proxyNode);
         } finally {
             tx.finish();
         }
     }
 
     public static Node getProxySector(Node sector, String fileName) {
         String proxySectorName = fileName + PROXY_NAME_SEPARATOR + sector.getProperty(INeoConstants.PROPERTY_NAME_NAME);
         for (Node node : sector.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, NetworkRelationshipTypes.NEIGHBOURS,
                 Direction.OUTGOING)) {
             if (node.getProperty(INeoConstants.PROPERTY_NAME_NAME, "").toString().equals(proxySectorName))
                 return node;
         }
         return null;
     }
     /**
      * @param rootName
      * @param creater
      * @param service
      */
     public static Node findorCreateRootInActiveProject(String projectName, String rootName, RunnableWithResult<Node> creater, GraphDatabaseService service) {
         Node result = findRootNodeByName(projectName, rootName, service);
         if (result == null) {
             Transaction tx = beginTx(service);
             try {
                 creater.run();
                 result = creater.getValue();
                 
                 NeoServiceFactory.getInstance().getProjectService().addDataNodeToProject(projectName, result);
             } finally {
                 tx.finish();
             }
         }
         return result;
     }
 
     /**
      * Find root node by name.
      * 
      * @param projectName the project name
      * @param rootName the root name
      * @param service the service
      * @return the node
      */
     public static Node findRootNodeByName(final String projectName, final String rootName, final GraphDatabaseService service) {
         
         AweProjectNode project = NeoServiceFactory.getInstance().getProjectService().findAweProject(projectName);
         if (project == null) {
             return null;
         }
         Transaction tx = beginTx(service);
         try {
             Iterator<Node> it = project.getUnderlyingNode().traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     if (currentPos.isStartNode()) {
                         return false;
                     }
                     return rootName.equals(getNodeName(currentPos.currentNode(), service));
                 }
             }, GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING).iterator();
             if (it.hasNext()) {
                 return it.next();
             }
             return null;
         } finally {
             tx.finish();
         }
     }
 }
