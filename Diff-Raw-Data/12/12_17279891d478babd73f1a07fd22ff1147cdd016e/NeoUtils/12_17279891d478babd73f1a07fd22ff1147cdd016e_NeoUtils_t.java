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
 package org.amanzi.neo.core.utils;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Set;
 
 import net.refractions.udig.catalog.CatalogPlugin;
 import net.refractions.udig.catalog.ICatalog;
 import net.refractions.udig.catalog.IService;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.enums.DriveTypes;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.GisTypes;
 import org.amanzi.neo.core.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.enums.OssType;
 import org.amanzi.neo.core.enums.ProbeCallRelationshipType;
 import org.amanzi.neo.core.enums.CallProperties.CallType;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.index.MultiPropertyIndex;
 import org.amanzi.neo.index.PropertyIndex;
 import org.amanzi.neo.index.MultiPropertyIndex.MultiDoubleConverter;
 import org.amanzi.neo.index.MultiPropertyIndex.MultiTimeIndexConverter;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.PropertyContainer;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.ReturnableEvaluator;
 import org.neo4j.api.core.StopEvaluator;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.api.core.TraversalPosition;
 import org.neo4j.api.core.Traverser;
 import org.neo4j.api.core.Traverser.Order;
 import org.neo4j.neoclipse.preference.NeoDecoratorPreferences;
 
 /**
  * <p>
  * Utility class that provides common methods for work with neo nodes
  * </p>
  * 
  * @author Cinkel_A
  * @since 1.0.0
  */
 public class NeoUtils {
     private static final String TIMESTAMP_INDEX_NAME = "Index-timestamp-";
     private static final String LOCATION_INDEX_NAME = "Index-location-";
 
     private NeoUtils() {
 
     }
 
     /**
      * delete all incoming reference
      * 
      * @param node
      */
     public static void deleteIncomingRelations(Node node) {
         for (Relationship relation : node.getRelationships(Direction.INCOMING)) {
             relation.delete();
         }
     }
 
     /**
      * gets node name
      * 
      * @param node node
      * @param defValue default value
      * @return node name or defValue
      */
     public static String getNodeType(Node node, String... defValue) {
         String def = defValue == null || defValue.length < 1 ? null : defValue[0];
         return (String)node.getProperty(INeoConstants.PROPERTY_TYPE_NAME, def);
     }
 
     /**
      * Gets node name
      * 
      * @param node node
      * @return node name or empty string
      */
     public static String getNodeName(Node node) {
         // String type = node.getProperty(INeoConstants.PROPERTY_TYPE_NAME, "").toString();
         // if (type.equals(INeoConstants.MP_TYPE_NAME)) {
         // return node.getProperty(INeoConstants.PROPERTY_TIME_NAME, "").toString();
         //        
         // } else if (type.equals(INeoConstants.HEADER_M)) {
         // return node.getProperty(INeoConstants.PROPERTY_CODE_NAME, "").toString();
         //        
         // }
         return getSimpleNodeName(node, "");
     }
 
     /**
      * Gets node name
      * 
      * @param node node
      * @return node name or empty string
      */
     public static String getNodeName(Node node, NeoService service) {
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
      * Gets node name
      * 
      * @param node node
      * @param defValue default value
      * @return node name or empty string
      */
     public static String getSimpleNodeName(PropertyContainer node, String defValue) {
         Transaction tx = beginTransaction();
         try {
             return node.getProperty(INeoConstants.PROPERTY_NAME_NAME, defValue).toString();
         } finally {
             tx.success();
             tx.finish();
         }
     }
 
     /**
      * Gets node name
      * 
      * @param node node
      * @param defValue default value
      * @return node name or empty string
      */
     public static String getSimpleNodeName(PropertyContainer node, String defValue, NeoService service) {
         Transaction tx = beginTx(service);
         try {
             return node.getProperty(INeoConstants.PROPERTY_NAME_NAME, defValue).toString();
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * check node by type
      * 
      * @param node node
      * @return true if node is file node
      */
     public static boolean isFileNode(Node node) {
         return node != null && NodeTypes.FILE.getId().equals(getNodeType(node, ""));
     }
 
     /**
      * check node by type
      * 
      * @param node node
      * @return true if node is file node
      */
     public static boolean isDrivePointNode(Node node) {
         return node != null && NodeTypes.MP.getId().equals(getNodeType(node, ""));
     }
 
     /**
      * check node by type
      * 
      * @param node node
      * @return true if node is file node
      */
     public static boolean isDatasetNode(Node node) {
         return node != null && NodeTypes.DATASET.getId().equals(getNodeType(node, ""));
     }
 
     /**
      * check node by type
      * 
      * @param node node
      * @return true if node is file node
      */
     public static boolean isDriveMNode(Node node) {
         return node != null && NodeTypes.M.getId().equals(getNodeType(node, ""));
     }
 
     /**
      * Is this node a Probe node
      * 
      * @param node node to check
      * @return is this node a Probe node
      */
     public static boolean isProbeNode(Node node) {
         return node != null && NodeTypes.PROBE.getId().equals(getNodeType(node, ""));
     }
 
     /**
      * Is this node a Probe Calls node
      * 
      * @param node node to check
      * @return is this node a Probe Calls node
      */
     public static boolean isProbeCallsNode(Node node) {
         return node != null && NodeTypes.CALLS.getId().equals(getNodeType(node, ""));
     }
 
     /**
      * Is this node a Call node
      * 
      * @param node node to check
      * @return is this node a Call node
      */
     public static boolean isCallNode(Node node) {
         return node != null && NodeTypes.CALL.getId().equals(getNodeType(node, ""));
     }
 
     /**
      * gets stop evaluator with necessary depth
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
      * finds gis node by name
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
      * finds gis node by name
      * 
      * @param gisName name of gis node
      * @return gis node or null
      */
     public static Node findGisNode(final String gisName, final NeoService service) {
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
      * finds root (child of reference node)node by name
      * 
      * @param nodeName name of gis node
      * @return gis node or null
      */
     public static Node findRootNode(final NodeTypes type, final String nodeName, final NeoService service) {
         if (nodeName == null || nodeName.isEmpty() || type == null) {
             return null;
         }
         Transaction tx = beginTx(service);
         try {
             Node root = service.getReferenceNode();
             Iterator<Node> gisIterator = root.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     Node node = currentPos.currentNode();
                     return type.checkNode(node) && getNodeName(node, service).equals(nodeName);
                 }
             }, NetworkRelationshipTypes.CHILD, Direction.OUTGOING).iterator();
             return gisIterator.hasNext() ? gisIterator.next() : null;
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * check node by type
      * 
      * @param node node
      * @return true if node is gis node
      */
     public static boolean isGisNode(Node node) {
         return node != null && NodeTypes.GIS.getId().equals(getNodeType(node, ""));
     }
 
     /**
      * check node by type
      * 
      * @param node node
      * @return true if node is gis node
      */
     public static boolean isNeighbourNode(Node node) {
         return node != null && NodeTypes.NEIGHBOUR.getId().equals(getNodeType(node, ""));
     }
 
     /**
      * check node by type
      * 
      * @param node node
      * @return true if node is gis node
      */
     public static boolean isTransmission(Node node) {
         return node != null && NodeTypes.TRANSMISSION.getId().equals(getNodeType(node, ""));
     }
 
     /**
      *Delete gis node if it do not have outcoming relations
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
                 NeoCorePlugin.getDefault().getProjectService().deleteNode(gisNode);
             }
             tx.success();
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Finds gis node by child
      * 
      * @param childNode child
      * @return gis node or null
      */
     public static Node findGisNodeByChild(Node childNode) {
         Transaction tx = NeoServiceProvider.getProvider().getService().beginTx();
         try {
             Iterator<Node> gisIterator = childNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     Node node = currentPos.currentNode();
                     return isGisNode(node);
                 }
             }, NetworkRelationshipTypes.CHILD, Direction.INCOMING, GeoNeoRelationshipTypes.NEXT, Direction.INCOMING).iterator();
             tx.success();
             return gisIterator.hasNext() ? gisIterator.next() : null;
         } finally {
             tx.finish();
         }
     }
     
     /**
      * Finds gis node by child
      * 
      * @param childNode child
      * @param service NeoService
      * @return gis node or null
      */
     public static Node findGisNodeByChild(Node childNode, NeoService service) {
         Transaction tx = service.beginTx();
         try {
             Iterator<Node> gisIterator = childNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     Node node = currentPos.currentNode();
                     return isGisNode(node);
                 }
             }, NetworkRelationshipTypes.CHILD, Direction.INCOMING, GeoNeoRelationshipTypes.NEXT, Direction.INCOMING).iterator();
             tx.success();
             return gisIterator.hasNext() ? gisIterator.next() : null;
         } finally {
             tx.finish();
         }
     }
 
     /**
      * @throws MalformedURLException
      */
     public static void resetGeoNeoService() {
         try {
             // TODO gets service URL from static field
             String databaseLocation = NeoServiceProvider.getProvider().getDefaultDatabaseLocation();
             ICatalog catalog = CatalogPlugin.getDefault().getLocalCatalog();
             List<IService> services = CatalogPlugin.getDefault().getServiceFactory().createService(new URL("file://" + databaseLocation));
             for (IService service : services) {
                 System.out.println("Found catalog service: " + service);
                 if (catalog.getById(IService.class, service.getIdentifier(), new NullProgressMonitor()) != null) {
                     catalog.replace(service.getIdentifier(), service);
                 } else {
                     catalog.add(service);
                 }
             }
 
         } catch (MalformedURLException e) {
             // TODO Handle MalformedURLException
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
     }
 
     /**
      * begins new neo4j transaction
      * 
      * @return transaction
      */
     public static Transaction beginTransaction() {
         return NeoServiceProvider.getProvider().getService().beginTx();
 
     }
 
     /**
      *Finds Neighbour of network
      * 
      * @param network - network GIS node
      * @param name - Neighbour name
      * @return Neighbour node or null;
      */
     public static Node findNeighbour(Node network, final String name) {
         return findNeighbour(network, name, NeoServiceProvider.getProvider().getService());
     }
 
     /**
      *Finds Neighbour of network
      * 
      * @param network - network GIS node
      * @param name - Neighbour name
      * @return Neighbour node or null;
      */
     public static Node findNeighbour(Node network, final String name, NeoService neo) {
         if (network == null || name == null) {
             return null;
         }
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
      * Gets array of Numeric Fields
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
             return null;
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Gets Neighbour name of relation
      * 
      * @param relation relation
      * @param defValue default value
      * @return name or default value if property do not exist
      */
     public static String getNeighbourName(Relationship relation, String defValue) {
         return relation.getProperty(INeoConstants.NEIGHBOUR_NAME, defValue).toString();
     }
 
     /**
      * Return neighbour relations of selected neighbour list
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
             if (neighbourName.equals(getNeighbourName(relation, null))) {
                 result.add(relation);
             }
         }
         return result;
     }
 
     /**
      * Return neighbour relations of selected neighbour list
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
             if (neighbourName.equals(getNeighbourName(relation, null))) {
                 result.add(relation);
             }
         }
         return result;
     }
 
     /**
      * gets neighbour property name
      * 
      * @param aNeighbourName - name of neighbour
      * @return property name
      */
     public static String getNeighbourPropertyName(String aNeighbourName) {
         return String.format("# '%s' neighbours", aNeighbourName);
     }
 
     /**
      * get Transmission property name
      * 
      * @param aTransmissionName name of transmission
      * @return property name
      */
     public static String getTransmissionPropertyName(String aTransmissionName) {
         return String.format("# '%s' transmissions", aTransmissionName);
     }
 
     /**
      * Finds node by child
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
      * Get all fields property
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
      * delete node and all relation from/to it
      * 
      * @param node - node to delete
      */
     public static void deleteSingleNode(Node node) {
         Transaction tx = beginTransaction();
         try {
             for (Relationship relation : node.getRelationships()) {
                 relation.delete();
             }
             node.delete();
             tx.success();
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Gets chart node of current node
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
      * get timestamp of node
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
         String time = (String)node.getProperty("time", null);
         if (time == null) {
             return null;
         }
         SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
         Date timeD;
         try {
             timeD = df.parse(time);
         } catch (ParseException e) {
             NeoCorePlugin.error(e.getLocalizedMessage(), e);
             return null;
         }
         return timeD.getTime();
     }
 
     /**
      * get Traverser of all file node
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
      * gets node by id
      * 
      * @param nodeId node id
      * @return node
      */
     public static Node getNodeById(Long nodeId) {
         return nodeId == null ? null : NeoServiceProvider.getProvider().getService().getNodeById(nodeId);
     }
 
     /**
      *Finds Transmission of network
      * 
      * @param network - network GIS node
      * @param name - Transmission name
      * @param neo - NeoService
      * @return Transmission node or null;
      */
     public static Node findTransmission(Node network, final String name, NeoService neo) {
         if (network == null || name == null) {
             return null;
         }
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
      * Gets name of ms childs
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
                 return getNodeType(curNode, "").equals(NodeTypes.M.getId()) && curNode.hasProperty(msName);
             }
         }, NetworkRelationshipTypes.CHILD, Direction.OUTGOING);
         for (Node nodeMs : traverse) {
 
             result.append(nodeMs.getProperty(msName).toString()).append(delim);
         }
         if (result.length() > delim.length()) {
             result.setLength(result.length() - delim.length());
         }
         return result.toString();
     }
 
     /**
      * finds or create if necessary SectorDriveRoot node
      * 
      * @param root - root node of drive network
      * @param neo - NeoService
      * @return SectorDriveRoot node
      */
     public static Node findOrCreateSectorDriveRoot(Node root, NeoService service, boolean isNewTransaction) {
 
         NeoService neo = isNewTransaction ? service : null;
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
      * find or create sector-drive node
      * 
      * @param mpNode - mp node
      * @param neo - neo service if null then transaction do not created
      * @return sector-drive node
      */
     public static Node findOrCreateSectorDrive(String aDriveName, Node sectorDriveRoot, Node mpNode, NeoService service, boolean isNewTransaction) {
         NeoService neo = isNewTransaction ? service : null;
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
      * @param parentNode parent node
      * @return Pair<is node was created?,child node>
      */
     public static Pair<Boolean, Node> findOrCreateChildNode(NeoService service, Node parentNode, final String nodeName) {
         Transaction tx = beginTx(service);
         try {
             Traverser fileNodeTraverser = NeoUtils.getChildTraverser(parentNode);
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
      * @param parentNode parent node
      * @return Pair<is node was created?,child node>
      */
     public static Pair<Boolean, Node> findOrCreateFileNode(NeoService service, Node parentNode, final String nodeName, final String fileName) {
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
      * get all network gis nodes which linked with drive nodes
      * 
      * @param service NeoService
      * @return Traverser
      */
     public static Traverser getLinkedNetworkTraverser(NeoService service) {
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
                     return node.hasRelationship(NetworkRelationshipTypes.LINKED_NETWORK_DRIVE, Direction.OUTGOING);
                 }
             }, NetworkRelationshipTypes.CHILD, Direction.OUTGOING);
         } finally {
             tx.finish();
         }
     }
 
     /**
      * finish transaction if it not null
      * 
      * @param tx -Transaction or null
      */
     public static void finishTx(Transaction tx) {
         if (tx != null) {
             tx.finish();
         }
     }
 
     /**
      * mark success transaction if it not null
      * 
      * @param tx -Transaction or null
      */
     public static void successTx(Transaction tx) {
         if (tx != null) {
             tx.success();
         }
     }
 
     /**
      * begin transaction
      * 
      * @param service
      * @return Transaction if service present else null
      */
     public static Transaction beginTx(NeoService service) {
         return service == null ? null : service.beginTx();
     }
 
     /**
      * link SectorDrive node with sector node
      * 
      * @param networkGis network gis node
      * @param sectorDrive - SectorDrive node
      * @param service - neo service if null then transaction do not created
      * @return sector node or null if sector not found
      */
     public static Node linkWithSector(Node networkGis, Node sectorDrive, NeoService service) {
         Transaction tx = beginTx(service);
         try {
             // TODO use index?
             String networkName = NeoUtils.getSimpleNodeName(networkGis, "");
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
      * get list of all event of mp node
      * 
      * @param mpNode mp node
      * @param service - neo service if null then transaction do not created
      * @return list of all event of mp node
      */
     public static Set<String> getEventsList(Node mpNode, NeoService service) {
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
 
     public static void addTransactionLog(Transaction transaction, Thread thread, String description) {
         PrintStream out;
         final String simpleName = transaction == null ? "" : transaction.getClass().getSimpleName();
         if (simpleName.isEmpty() || simpleName.equals("TransactionImpl")) {
             out = System.out;
         } else {
             out = System.err;
         }
         out.println(new StringBuilder("Transaction:\t").append(thread.getId()).append("\t").append(thread.getName()).append("\t").append(simpleName).append("\t").append(
                 description));
     }
 
     /**
      * gets name of timestamp index
      * 
      * @param datasetName - dataset name
      * @return index name
      */
     public static String getTimeIndexName(String datasetName) {
         return TIMESTAMP_INDEX_NAME + datasetName;
     }
 
     /**
      * gets name of location index
      * 
      * @param datasetName - dataset name
      * @return index name
      */
     public static String getLocationIndexName(String datasetName) {
         return LOCATION_INDEX_NAME + datasetName;
     }
 
     /**
      * Returns pair of min and max timestamps for this dataset
      * 
      * @param driveGisNode drive gis node
      * @param service neoservice if null then transaction do not created
      * @return pair of min and max timestamps
      */
     public static Pair<Long, Long> getMinMaxTimeOfDataset(Node driveGisNode, NeoService service) {
         Transaction tx = beginTx(service);
         try {
             Pair<Long, Long> pair = new Pair<Long, Long>((Long)driveGisNode.getProperty(INeoConstants.MIN_TIMESTAMP, null), (Long)driveGisNode.getProperty(INeoConstants.MAX_TIMESTAMP,
                     null));
             return pair;
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * Gets timestamp index property
      * 
      * @param name - dataset name
      * @return timestamp index property
      * @throws IOException
      */
     public static MultiPropertyIndex<Long> getTimeIndexProperty(String name) throws IOException {
         return new MultiPropertyIndex<Long>(NeoUtils.getTimeIndexName(name), new String[] {INeoConstants.PROPERTY_TIMESTAMP_NAME}, new MultiTimeIndexConverter(), 10);
     }
 
     /**
      * Get location index
      * 
      * @param name - dataset name
      * @return location index
      * @throws IOException
      */
     public static MultiPropertyIndex<Double> getLocationIndexProperty(String name) throws IOException {
         return new MultiPropertyIndex<Double>(NeoUtils.getLocationIndexName(name), new String[] {INeoConstants.PROPERTY_LAT_NAME, INeoConstants.PROPERTY_LON_NAME},
                 new MultiDoubleConverter(0.001), 10);
     }
 
     /**
      * Searches for the Probe node by it's name
      * 
      * @param networkNode Network that should contain Probe
      * @param probeName name of Probe
      * @return Probe node
      */
     public static Node findOrCreateProbeNode(Node networkNode, final String probeName, final NeoService service) {
         Transaction tx = beginTx(service);
         Node result = null;
         try {
             Iterator<Node> probeIterator = networkNode.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     return NeoUtils.isProbeNode(currentPos.currentNode()) && probeName.equals(NeoUtils.getNodeName(currentPos.currentNode(),service));
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
             NeoCorePlugin.error(null, e);
             tx.failure();
         } finally {
             tx.finish();
         }
 
         return result;
     }
 
     /**
      * Returns a Probe Calls node for current dataset and network
      * 
      * @param datasetName name of dataset
      * @param probesName name of probe
      * @param probesNode node for probe
      * @param service Neo Serivce
      * @return a Probe Calls node for current dataset
      */
     public static Node getCallsNode(Node datasetNode, String probesName, Node probesNode, NeoService service) {
         Transaction tx = beginTx(service);
         Node callsNode = null;
         try {
             final String datasetName = NeoUtils.getNodeName(datasetNode,service);
             Iterator<Node> callsIterator = probesNode.traverse(Order.BREADTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     return datasetName.equals(currentPos.currentNode().getProperty(INeoConstants.DATASET_TYPE_NAME, ""));
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
             NeoCorePlugin.error(null, e);
             tx.failure();
         } finally {
             tx.finish();
         }
 
         return callsNode;
     }
 
     /**
      * Returns last Call from Probe Calls queue
      * 
      * @param probeCallsNode Probe Calls node
      * @param service Neo service
      * @return last Call node
      */
     public static Node getLastCallFromProbeCalls(Node probeCallsNode, NeoService service) {
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
      *Return type of dataset
      * 
      * @param datasetNode dataset node
      * @param service neoservice if null then transaction do not created
      * @return DriveTypes or null
      */
     public static DriveTypes getDatasetType(Node datasetNode, NeoService service) {
         Transaction tx = beginTx(service);
         try {
             String typeId = (String)datasetNode.getProperty(INeoConstants.DRIVE_TYPE, null);
             return DriveTypes.findById(typeId);
         } finally {
             finishTx(tx);
         }
     }
 
     public static boolean isVirtualDataset(Node datasetNode) {
         return getNodeType(datasetNode, "").equals(NodeTypes.DATASET.getId()) && datasetNode.hasRelationship(GeoNeoRelationshipTypes.VIRTUAL_DATASET, Direction.INCOMING);
     }
 
     /**
      * Gets all dataset nodes
      * 
      * @param service neoservice can not be null
      * @return Map<node name,dataset node>
      */
     public static LinkedHashMap<String, Node> getAllDatasetNodes(NeoService service) {
         Transaction tx = beginTx(service);
         LinkedHashMap<String, Node> result = new LinkedHashMap<String, Node>();
         try {
             Traverser traverser = service.getReferenceNode().traverse(Order.DEPTH_FIRST, getStopEvaluator(3), new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     Node node = currentPos.currentNode();
                     return isDatasetNode(node);
                 }
             }, NetworkRelationshipTypes.CHILD, Direction.OUTGOING, GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING, GeoNeoRelationshipTypes.VIRTUAL_DATASET,
                     Direction.OUTGOING);
             for (Node node : traverser) {
                 result.put(getNodeName(node,service), node);
             }
             return result;
         } finally {
             finishTx(tx);
         }
     }
 
     public static Node findOrCreateVirtualDatasetNode(Node realDatasetNode, DriveTypes driveType, final NeoService neo) {
         Node virtualDataset = null;
         String realDatasetName = NeoUtils.getNodeName(realDatasetNode, neo);
         final String virtualDatasetName = driveType.getFullDatasetName(realDatasetName);
         Transaction tx = neo.beginTx();
         try {
             Iterator<Node> virtualDatasetsIterator = realDatasetNode.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     return virtualDatasetName.equals(NeoUtils.getNodeName(currentPos.currentNode(), neo));
                 }
             }, GeoNeoRelationshipTypes.VIRTUAL_DATASET, Direction.OUTGOING).iterator();
 
             if (virtualDatasetsIterator.hasNext()) {
                 virtualDataset = virtualDatasetsIterator.next();
             } else {
                 virtualDataset = neo.createNode();
                 virtualDataset.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.DATASET.getId());
                 virtualDataset.setProperty(INeoConstants.PROPERTY_NAME_NAME, virtualDatasetName);
                 virtualDataset.setProperty(INeoConstants.DRIVE_TYPE, driveType.getId());
 
                 realDatasetNode.createRelationshipTo(virtualDataset, GeoNeoRelationshipTypes.VIRTUAL_DATASET);
             }
             tx.success();
         } catch (Exception e) {
             tx.failure();
             NeoCorePlugin.error(null, e);
         } finally {
             tx.finish();
         }
 
         return virtualDataset;
     }
 
     public static Node createGISNode(Node parent, String gisName, String gisType, NeoService neo) {
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
             NeoCorePlugin.error(null, e);
         } finally {
             transaction.finish();
         }
 
         return gisNode;
     }
 
     /**
      * @param amsCalls
      * @param service
      * @return
      */
     public static LinkedHashMap<String, Node> getAllDatasetNodesByType(final DriveTypes datasetType, NeoService service) {
         Transaction tx = beginTx(service);
         LinkedHashMap<String, Node> result = new LinkedHashMap<String, Node>();
         try {
             Traverser traverser = service.getReferenceNode().traverse(Order.DEPTH_FIRST, getStopEvaluator(3), new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     Node node = currentPos.currentNode();
                     return isDatasetNode(node) && getDatasetType(node, null) == datasetType;
                 }
             }, NetworkRelationshipTypes.CHILD, Direction.OUTGOING, GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING, GeoNeoRelationshipTypes.VIRTUAL_DATASET,
                     Direction.OUTGOING);
             for (Node node : traverser) {
                 result.put(getNodeName(node), node);
             }
             return result;
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * Get traverser by child of node (one child )
      * 
      * @param rootNode - root node;
      * @return Traverser
      */
     public static Traverser getChildTraverser(Node rootNode) {
         return getChildTraverser(rootNode, StopEvaluator.END_OF_GRAPH, ReturnableEvaluator.ALL);
     }
 
     public static Traverser getChildTraverser(Node rootNode, final StopEvaluator stopEvaluator) {
         return getChildTraverser(rootNode, stopEvaluator, ReturnableEvaluator.ALL);
     }
 
     public static Traverser getChildTraverser(Node rootNode, final ReturnableEvaluator returnableEvaluator) {
         return getChildTraverser(rootNode, StopEvaluator.END_OF_GRAPH, returnableEvaluator);
     }
 
     /**
      * Get traverser by child of node (one child )
      * 
      * @param rootNode - root node;
      * @return Traverser
      */
     public static Traverser getChildTraverser(Node rootNode, final StopEvaluator stopEvaluator, final ReturnableEvaluator returnableEvaluator) {
         Iterator<Relationship> relations = rootNode.getRelationships(GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING).iterator();
         // TODO add check on single relations?
         if (relations.hasNext()) {
             return relations.next().getOtherNode(rootNode).traverse(Order.DEPTH_FIRST, stopEvaluator, returnableEvaluator, GeoNeoRelationshipTypes.NEXT,
                     Direction.OUTGOING);
         } else {
             return emptyTraverser(rootNode);
         }
     }
 
     /**
      * return empty traverser
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
      * get parent of current node
      * 
      * @param service NeoService if null then new transaction created
      * @param childNode child node
      * @return parent node or null
      */
     public static Node getParent(NeoService service, Node childNode) {
         Transaction tx = beginTx(service);
         try {
             Iterator<Node> parentIterator = childNode.traverse(Order.DEPTH_FIRST, new StopEvaluator() {
 
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
      *Return type of gis
      * 
      * @param gisNode GIS node
      * @param service neoservice if null then transaction do not created
      * @return GisTypes or null
      */
     public static GisTypes getGisType(Node gisNode, NeoService service) {
         Transaction tx = beginTx(service);
         try {
             String typeId = (String)gisNode.getProperty(INeoConstants.PROPERTY_GIS_TYPE_NAME, "");
             return GisTypes.findGisTypeByHeader(typeId);
         } finally {
             finishTx(tx);
         }
     }
 
     /**
      * Return location node of current drive node
      * 
      * @param node - drive node
      * @param service neoservice if null then transaction do not created
      * @return location node or null
      */
     public static Node getLocationNode(Node node, NeoService service) {
         Transaction tx = beginTx(service);
         try {
             return node.hasRelationship(GeoNeoRelationshipTypes.LOCATION, Direction.OUTGOING) ? node.getSingleRelationship(GeoNeoRelationshipTypes.LOCATION,
                     Direction.OUTGOING).getOtherNode(node) : null;
         } finally {
             finishTx(tx);
         }
     }
 
     public static boolean hasCallsOfType(Node datasetNode, CallType type, final String probeName) {
         String propertyName = null;
         switch (type) {
         case INDIVIDUAL:
             propertyName = "has_individual_calls";
             break;
         case GROUP:
             propertyName = "has_group_calls";
             break;
         }
         final String finalName = propertyName;
 
         Iterator<Node> probeCalls = datasetNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
 
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 return (Boolean)currentPos.currentNode().getProperty(finalName, false)
                         && probeName.equals(getNodeName(currentPos.currentNode().getSingleRelationship(ProbeCallRelationshipType.CALLS, Direction.INCOMING)
                                 .getStartNode()));
             }
         }, ProbeCallRelationshipType.PROBE_DATASET, Direction.OUTGOING).iterator();
 
         return probeCalls.hasNext();
     }
 
     /**
      * Returns all Probe Nodes related to Dataset
      * 
      * @param datasetNode Dataset Node
      * @return all Probe Nodes
      */
     public static Collection<Node> getAllProbesOfDataset(Node datasetNode, CallType type) {
         String propertyName = null;
         switch (type) {
         case INDIVIDUAL:
             propertyName = "has_individual_calls";
             break;
         case GROUP:
             propertyName = "has_group_calls";
             break;
         }
         final String finalName = propertyName;
 
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
      *Get location pair
      * 
      * @param locationNode - location node
      * @param service neoservice if null then transaction do not created
      * @return Pair<LATITUDE,LONGITUDE>
      */
     public static Pair<Double, Double> getLocationPair(Node locationNode, NeoService service) {
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
 
     public static void checkTransactionOnThread(NeoService service, String description) {
         service = service == null ? NeoServiceProvider.getProvider().getService() : service;
         Transaction tx = beginTx(service);
         addTransactionLog(tx, Thread.currentThread(), description);
         tx.finish();
     }
 
     /**
      * get format date string
      * 
      * @param startTime - begin timestamp
      * @param endTime end timestamp
      * @return
      */
     public static String getFormatDateString(Long startTime, Long endTime, String dayFormat) {
         if (startTime == null || endTime == null) {
             return "No time";
         }
         StringBuilder sb = new StringBuilder();
         String pattern = "yyyy-MM-dd " + dayFormat;
         if (endTime - startTime <= 24 * 60 * 60 * 1000 && new Date(startTime).getDay() == new Date(endTime).getDay()) {
             SimpleDateFormat sf = new SimpleDateFormat(pattern);
             SimpleDateFormat sf2 = new SimpleDateFormat(dayFormat);
             sb.append(sf.format(new Date(startTime)));
             sb.append("-").append(sf2.format(endTime));
         } else {
             SimpleDateFormat sfMulDay1 = new SimpleDateFormat(pattern);
             SimpleDateFormat sfMulDay2 = new SimpleDateFormat(pattern);
             sb.append(sfMulDay1.format(new Date(startTime)));
             sb.append(" to ").append(sfMulDay2.format(endTime));
         }
         return sb.toString();
     }
 
     /**
      * gets formated node name
      * 
      * @param node node
      * @param defValue default value
      * @return node name or defValue
      */
     public static String getFormatedNodeName(Node node, String defName) {
         Transaction tx = beginTransaction();
         try {
             String prefStore = org.neo4j.neoclipse.Activator.getDefault().getPreferenceStore().getString(NeoDecoratorPreferences.NODE_PROPERTY_NAMES);
             StringBuilder values = new StringBuilder();
             for (String name : prefStore.split(",")) {
                 name = name.trim();
                 if ("".equals(name)) {
                     continue;
                 }
                 Object propertyValue = node.getProperty(name, null);
                 if (propertyValue == null) {
                     continue;
                 }
                 values.append(", ").append(propertyValue.toString());
             }
             return values.length() == 0 ? defName : values.substring(2);
         } finally {
             tx.finish();
         }
     }
 
     public static Node findMultiPropertyIndex(final String indexName,final NeoService neoService) {
         Iterator<Node> indexNodes = neoService.getReferenceNode().traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 return indexName.equals(NeoUtils.getNodeName(currentPos.currentNode(),neoService));
             }
         }, PropertyIndex.NeoIndexRelationshipTypes.INDEX, Direction.OUTGOING).iterator();
 
         if (indexNodes.hasNext()) {
             return indexNodes.next();
         } else {
             return null;
         }
     }
 
     /**
      * gets name of index
      * 
      * @param basename - gis name
      * @param propertyName - property name
      * @param type - node type
      * @return luciene key
      */
     public static String getLuceneIndexKeyByProperty(String basename, String propertyName, NodeTypes type) {
         return new StringBuilder(basename).append("@").append(type.getId()).append("@").append(propertyName).toString();
     }
 
     /**
      *Gets all OSS nodes
      * 
      * @param service-neoservice
      */
     public static Collection<Node> getAllOss(NeoService service) {
         Transaction tx = beginTx(service);
         try {
             return service.getReferenceNode().traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     return NodeTypes.OSS.checkNode(currentPos.currentNode());
                 }
             }, GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING).getAllNodes();
         } finally {
             finishTx(tx);
         }
     }
     /**
      *Gets all OSS nodes
      * 
      * @param service-neoservice
      */
     public static Collection<Node> getAllGpeh(NeoService service) {
         Transaction tx = beginTx(service);
         try {
             return getAllReferenceChild(service, new ReturnableEvaluator() {
                 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     Node node=currentPos.currentNode();
                     return NodeTypes.OSS.checkNode(node)&&OssType.GPEH.checkNode(node);
                 }
             }).getAllNodes();
         } finally {
             finishTx(tx);
         }
     }   
     /**
      *Gets all childs of reference node
      * 
      * @param service-neoservice
      */
     public static Traverser getAllReferenceChild(NeoService service,final ReturnableEvaluator evaluator) {
         Transaction tx = beginTx(service);
         try {
             return service.getReferenceNode().traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE,evaluator, GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING);
         } finally {
             finishTx(tx);
         }
     }
     /**
      * add child to node
      * 
      * @param mainNode root node
      * @param subNode child node
      * @param lastChild - last child node (in not known or mainNode do not have childs lastChild
      *        should be= null)
      * @param neo - neoservice
      */
     public static void addChild(Node mainNode, Node subNode, Node lastChild, NeoService neo) {
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
         } finally {
             finishTx(tx);
         }
 
     }
 
     /**
      * return last child of node
      * 
      * @param mainNode root node
      * @param neo service
      * @return
      */
     public static Node findLastChild(Node mainNode, NeoService neo) {
         Transaction tx = beginTx(neo);
         try {
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
      * @param gis gis node
      */
     public static Node findOrCreateNetworkNode(Node gisNode, String basename, String filename, NeoService neo) {
         Node network;
         Transaction tx = neo.beginTx();
         try {
             Relationship relation = gisNode.getSingleRelationship(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING);
             if (relation != null) {
                 return relation.getOtherNode(gisNode);
             }
             network = neo.createNode();
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
 }
