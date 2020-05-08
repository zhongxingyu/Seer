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
 
 package org.amanzi.neo.services;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.amanzi.neo.services.enums.DatasetRelationshipTypes;
 import org.amanzi.neo.services.enums.DriveTypes;
 import org.amanzi.neo.services.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.services.enums.GisTypes;
 import org.amanzi.neo.services.enums.INodeType;
 import org.amanzi.neo.services.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.services.enums.NodeTypes;
 import org.amanzi.neo.services.enums.SplashRelationshipTypes;
 import org.amanzi.neo.services.indexes.MultiPropertyIndex;
 import org.amanzi.neo.services.internal.DynamicNodeType;
 import org.amanzi.neo.services.utils.Utils;
 import org.amanzi.neo.services.utils.Utils.FilterAND;
 import org.apache.commons.lang.StringUtils;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Path;
 import org.neo4j.graphdb.PropertyContainer;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.traversal.PruneEvaluator;
 import org.neo4j.graphdb.traversal.TraversalDescription;
 import org.neo4j.graphdb.traversal.Traverser;
 import org.neo4j.graphdb.traversal.Uniqueness;
 import org.neo4j.helpers.Predicate;
 import org.neo4j.index.IndexHits;
 import org.neo4j.index.IndexService;
 import org.neo4j.kernel.Traversal;
 
 import com.vividsolutions.jts.util.Assert;
 
 /**
  * <p>
  * Service provide common operations with base structure in database: network, dataset,index,
  * statistics ect.
  * </p>
  * 
  * @author Lagutko_N
  * @since 1.0.0
  */
 public class DatasetService extends AbstractService {
     private Map<String,INodeType>registeredTypes=Collections.synchronizedMap(new HashMap<String,INodeType>());
     /**
      * should be used NeoServiceFactory for getting instance of DatasetService
      */
     DatasetService() {
         super();
     }
 
     // Constants for property names
     /* dynamic node type */
     private static final String DYNAMIC_TYPES = "dynamic_types";
 
     // Others constants
     /* proxy name separator */
     private static final String PROXY_NAME_SEPARATOR = "/";
 
     /**
      * Gets the root node
      * 
      * @param projectName the project name
      * @param datasetName the dataset name
      * @param rootType the root type
      * @return the root node
      */
     public Node getRootNode(String projectName, String datasetName, INodeType rootType) {
         return getRootNode(projectName, datasetName, rootType.getId());
     }
 
     /**
      * Gets the root node. Root it is the child of project node: dataset, network,oss...
      * 
      * @param projectName the project name
      * @param datasetName the root name
      * @param rootTypeId the root type id
      * @return the root node or null if node not found.
      */
     protected Node getRootNode(String projectName, String datasetName, String rootTypeId) {
         Node datasetNode = findRoot(projectName, datasetName);
         if (datasetNode != null) {
             if (!rootTypeId.equals(getTypeId(datasetNode))) {
                 throw new IllegalArgumentException(String.format("Wrong types of found node. Expected type: %s, real type: %s", rootTypeId, datasetNode));
             }
         }
         if (datasetNode == null) {
             datasetNode = addSimpleChild(findOrCreateAweProject(projectName), rootTypeId, datasetName);
         }
         return datasetNode;
     }
 
     /**
      * Adds the simple child. Create node and linked with parent used relation CHILD
      * 
      * @param parent the parent node
      * @param type the node type
      * @param name the node name
      * @return the created node
      */
     public Node addSimpleChild(Node parent, INodeType type, String name) {
         return addSimpleChild(parent, type.getId(), name);
     }
 
     /**
      * Adds the simple child. Create node and linked with parent used relation CHILD
      * 
      * @param parent the parent node
      * @param type the node type id
      * @param name the node name
      * @return the created node
      */
     protected Node addSimpleChild(Node parent, String typeId, String name) {
         Transaction tx = databaseService.beginTx();
         try {
             Node child = databaseService.createNode();
             child.setProperty(INeoConstants.PROPERTY_TYPE_NAME, typeId);
             child.setProperty(INeoConstants.PROPERTY_NAME_NAME, name);
             parent.createRelationshipTo(child, NetworkRelationshipTypes.CHILD);
             tx.success();
             return child;
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Gets the node type.
      * 
      * @param node the node
      * @return the node type
      */
     public INodeType getNodeType(PropertyContainer node) {
         String typeId = getTypeId(node);
         return getNodeType(typeId);
     }
 
     /**
      * Gets the node name.
      * 
      * @param node the node
      * @return the node name
      */
     public String getNodeName(Node node) {
         return (String)node.getProperty(INeoConstants.PROPERTY_NAME_NAME, null);
     }
     
     /**
      * Register type.
      *
      * @param newNodeType the new node type
      * @return true, if successful
      */
     public boolean registerNodeType(INodeType newNodeType){
         if (getNodeType(newNodeType.getId())!=null){
             return false;
         }
         registeredTypes.put(newNodeType.getId(),newNodeType);
         return true;
     }
     
     /**
      * Gets the node type by type id
      * 
      * @param typeId the type id
      * @return the node type
      */
     public INodeType getNodeType(String typeId) {
         if (typeId == null) {
             return null;
         }
         INodeType result = NodeTypes.getEnumById(typeId);
         if (result == null) {
             result = getDynamicNodeType(typeId);
         }
         if (result == null) {
             result=registeredTypes.get(typeId);
         }
         return result;
     }
 
     /**
      * Gets the dynamic node type.
      * 
      * @param type the type
      * @return the dynamic node type
      */
     private INodeType getDynamicNodeType(String type) {
         String[] types = (String[])getGlobalConfigNode().getProperty(DYNAMIC_TYPES, new String[0]);
         for (int i = 0; i < types.length; i++) {
             if (types[i].equals(type))
                 return new DynamicNodeType(type);
         }
         return null;
     }
 
     /**
      * Gets the type id of node
      * 
      * @param node the node
      * @return the type id
      */
     public String getTypeId(PropertyContainer node) {
         return (String)node.getProperty("type", null);
     }
 
     /**
      * Sets the node type
      * 
      * @param node the node
      * @param type the node type
      */
     public void setNodeType(Node node, INodeType type) {
         Transaction tx = databaseService.beginTx();
         try {
             setType(node, type.getId());
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Sets the type id in node
      * 
      * @param node the node
      * @param typeId the type id
      */
     protected void setType(Node node, String typeId) {
         node.setProperty("type", typeId);
     }
 
     /**
      * Gets the file node - if node not found, the necessary node was created
      * 
      * @param rootNode the root node
      * @param fileName the file name
      * @return the file node
      */
     public Node getFileNode(Node rootNode, String fileName) {
         Node fileNode = findFileNode(rootNode, fileName);
         if (fileNode == null) {
             fileNode = createFileNode(fileName);
             addChild(rootNode, fileNode, null);
         }
         return fileNode;
     }
 
     /**
      * Find file node
      * 
      * @param datasetNode the root node
      * @param fileName the file name
      * @return the node or null if node not found
      */
     public Node findFileNode(Node datasetNode, String fileName) {
         return findChildByName(datasetNode, fileName);
     }
 
     /**
      * Gets the gpeh statistics - if node not found, the necessary node was created
      * 
      * @param datasetNode the dataset node
      * @return the gpeh statistics
      */
     public GpehStatisticModel getGpehStatistics(Node datasetNode) {
         if (datasetNode.hasRelationship(DatasetRelationshipTypes.GPEH_STATISTICS, Direction.OUTGOING)) {
             Node statNode = datasetNode.getSingleRelationship(DatasetRelationshipTypes.GPEH_STATISTICS, Direction.OUTGOING).getEndNode();
             return new GpehStatisticModel(datasetNode, statNode, databaseService);
         } else {
             Node statNode = databaseService.createNode();
             datasetNode.createRelationshipTo(statNode, DatasetRelationshipTypes.GPEH_STATISTICS);
             return new GpehStatisticModel(datasetNode, statNode, databaseService);
         }
     }
 
     /**
      * Find root node by name - root node it is child of project node (network,dataset,oss)
      * 
      * @param projectName the project name
      * @param rootname the name of root node
      * @return the node or null if node not found
      */
     public Node findRoot(final String projectName, final String rootname) {
         TraversalDescription td = Utils.getTDRootNodes(new Predicate<Path>() {
 
             @Override
             public boolean accept(Path paramT) {
                 return rootname.equals(paramT.endNode().getProperty(INeoConstants.PROPERTY_NAME_NAME, ""))
                         && projectName.equals(paramT.lastRelationship().getStartNode().getProperty(INeoConstants.PROPERTY_NAME_NAME, ""));
             }
         });
         Iterator<Node> it = td.traverse(databaseService.getReferenceNode()).nodes().iterator();
         return it.hasNext() ? it.next() : null;
     }
 
     /**
      * Adds the data node to project- if root node always have incoming child relation, do nothing.
      * 
      * @param aweProjectName the awe project name
      * @param rootNode the root node
      */
     public void addDataNodeToProject(String aweProjectName, Node rootNode) {
 
         for (Relationship rel : rootNode.getRelationships(GeoNeoRelationshipTypes.CHILD, Direction.INCOMING)) {
             if (NodeTypes.AWE_PROJECT.checkNode(rel.getOtherNode(rootNode))) {
                 return;
             }
         }
         Transaction transacation = databaseService.beginTx();
         try {
             Node project = findOrCreateAweProject(aweProjectName);
             project.createRelationshipTo(rootNode, GeoNeoRelationshipTypes.CHILD);
             transacation.success();
         } finally {
             transacation.finish();
         }
     }
 
     /**
      * Find or create awe project node
      * 
      * @param aweProjectName the awe project name
      * @return the node
      */
     public Node findOrCreateAweProject(String aweProjectName) {
         Node result = null;
         result = findAweProject(aweProjectName);
         if (result == null) {
             result = createEmptyAweProject(aweProjectName);
         }
         return result;
     }
 
     /**
      * Creates the empty awe project.
      * 
      * @param projectName the project name
      * @return the node
      */
     private Node createEmptyAweProject(String projectName) {
         Transaction transaction = databaseService.beginTx();
         try {
             Node result = databaseService.createNode();
             result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.AWE_PROJECT.getId());
             result.setProperty(INeoConstants.PROPERTY_NAME_NAME, projectName);
             databaseService.getReferenceNode().createRelationshipTo(result, SplashRelationshipTypes.AWE_PROJECT);
             transaction.success();
             return result;
         } finally {
             transaction.finish();
         }
 
     }
 
     /**
      * Find awe project node
      * 
      * @param aweProjectName the awe project name
      * @return the node
      */
     public Node findAweProject(final String aweProjectName) {
         Iterator<Node> it = Utils.getTDProjectNodes(new Predicate<Path>() {
 
             @Override
             public boolean accept(Path paramT) {
                 return aweProjectName.equals(paramT.endNode().getProperty(INeoConstants.PROPERTY_NAME_NAME, ""));
             }
         }).traverse(databaseService.getReferenceNode()).nodes().iterator();
         return it.hasNext() ? it.next() : null;
     }
 
     /**
      * Creates the root node and link to project
      * 
      * @param projectName the project name
      * @param rootname the root name
      * @param rootNodeType the root node type id
      * @return the root node
      */
     public Node createRootNode(String projectName, String rootname, String rootNodeType) {
         Transaction tx = databaseService.beginTx();
         try {
             Node result = databaseService.createNode();
             setName(result, rootname);
             setType(result, rootNodeType);
             addDataNodeToProject(projectName, result);
             tx.success();
             return result;
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Index by property. Indexes node by value of necessary properties. Used Lucene index service.
      * 
      * @param rootId the root id - additional identification (id of root node)
      * @param node the node for index
      * @param propertyName the property name
      */
     public void indexByProperty(long rootId, Node node, String propertyName) {
         Assert.isTrue(node.hasProperty(propertyName));
         Transaction tx = databaseService.beginTx();
         try {
             String type = getTypeId(node);
             String indexName = new StringBuilder("Id").append(rootId).append("@").append(type).append("@").append(propertyName).toString();
             getIndexService().index(node, indexName, node.getProperty(propertyName));
             tx.success();
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Gets the location index property.
      * 
      * @param rootname the name of root node
      * @return the MultiPropertyIndex which provide work with 'lat','lon' properties
      * @throws IOException Signals that an I/O exception has occurred.
      */
     public MultiPropertyIndex< ? > getLocationIndexProperty(String rootname) throws IOException {
         return new MultiPropertyIndex<Double>(Utils.getLocationIndexName(rootname), new String[] {INeoConstants.PROPERTY_LAT_NAME, INeoConstants.PROPERTY_LON_NAME},
                 new org.amanzi.neo.services.indexes.MultiPropertyIndex.MultiDoubleConverter(0.001), 10);
     }
 
     /**
      * Gets the time index property
      * 
      * @param name the root name
      * @return the MultiPropertyIndex which provide work with 'timestamp' property
      * @throws IOException Signals that an I/O exception has occurred.
      */
     public MultiPropertyIndex<Long> getTimeIndexProperty(String name) throws IOException {
         return new MultiPropertyIndex<Long>(Utils.getTimeIndexName(name), new String[] {INeoConstants.PROPERTY_TIMESTAMP_NAME},
                 new org.amanzi.neo.services.indexes.MultiPropertyIndex.MultiTimeIndexConverter(), 10);
     }
 
     /**
      * Find child by name - find child node with necessary name by CHILD-NEXT structure
      * 
      * @param parent the parent node
      * @param name the name name of necessary node
      * @return the node or nuu if node not found
      */
     public Node findChildByName(Node parent, final String name) {
         TraversalDescription td = getChildTraversal(new Predicate<Path>() {
 
             @Override
             public boolean accept(Path item) {
                 return name.equals(getName(item.endNode()));
             }
 
         });
         Iterator<Node> it = td.traverse(parent).nodes().iterator();
         return it.hasNext() ? it.next() : null;
     };
 
     /**
      * Gets the child traversal - returns TraversalDescription for CHILD-NEXT structure
      * 
      * @param additionalFilter the additional filter - additional filter, can be null
      * @return the child traversal
      */
     public TraversalDescription getChildTraversal(Predicate<Path> additionalFilter) {
         FilterAND filter = new FilterAND();
         filter.addFilter(new Predicate<Path>() {
 
             @Override
             public boolean accept(Path paramT) {
                 int length = paramT.length();
                 if (length == 0) {
                     return false;
                 }
                 if (length == 1) {
                     return paramT.lastRelationship().isType(GeoNeoRelationshipTypes.CHILD);
                 } else {
                     return paramT.lastRelationship().isType(GeoNeoRelationshipTypes.NEXT);
                 }
             }
         });
         filter.addFilter(additionalFilter);
        return Traversal.description().depthFirst().uniqueness(Uniqueness.NONE).filter(filter)
                 .relationships(GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING).relationships(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING).prune(new PruneEvaluator() {
 
                     @Override
                     public boolean pruneAfter(Path position) {
                         if (position.lastRelationship() == null) {
                             return false;
                         }
                         if (position.length() == 1) {
                             return position.lastRelationship().isType(GeoNeoRelationshipTypes.NEXT);
                         } else {
                             return position.lastRelationship().isType(GeoNeoRelationshipTypes.CHILD);
                         }
                     }
                 });
     }
 
     /**
      * Adds the child in CHILD-NEXT structure
      * 
      * @param mainNode the main node
      * @param subNode the child node for adding to main node
      * @param lastChild the last child - if null, then last child will be find
      */
     public void addChild(Node mainNode, Node subNode, Node lastChild) {
         if (lastChild == null) {
             lastChild = findLastChild(mainNode);
         }
         Transaction tx = databaseService.beginTx();
         try {
             if (lastChild == null) {
                 mainNode.createRelationshipTo(subNode, GeoNeoRelationshipTypes.CHILD);
             } else {
                 lastChild.createRelationshipTo(subNode, GeoNeoRelationshipTypes.NEXT);
             }
             // save last child like property in main node
             mainNode.setProperty(INeoConstants.LAST_CHILD_ID, subNode.getId());
             tx.success();
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Find last child in CHILD-NEXT structure
      * 
      * @param mainNode - root node
      * @return last child node or null if root node do not have childs
      */
     public Node findLastChild(Node mainNode) {
         Long lastChild = (Long)mainNode.getProperty(INeoConstants.LAST_CHILD_ID, null);
         if (lastChild != null) {
             Node result = databaseService.getNodeById(lastChild);
             assert !result.hasRelationship(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING);
             return result;
         }
         TraversalDescription td = getChildTraversal(new Predicate<Path>() {
 
             @Override
             public boolean accept(Path item) {
                 return !item.endNode().hasRelationship(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING);
             }
         });
 
         Iterator<Node> iterator = td.traverse(mainNode).nodes().iterator();
         return iterator.hasNext() ? iterator.next() : null;
     }
 
     /**
      * Creates the file node with necessary name
      * 
      * @param fileName the file name
      * @return the node
      */
     public Node createFileNode(String fileName) {
         return createNode(NodeTypes.FILE, fileName);
 
     }
 
     /**
      * Creates the new node
      * 
      * @param type the node type
      * @param name the node name
      * @return the created node
      */
     public Node createNode(INodeType type, String name) {
         return createNode(type.getId(), INeoConstants.PROPERTY_NAME_NAME, name);
     }
 
     /**
      * Creates the node
      * 
      * @param typeId the type id
      * @param additionalProperties the additional properties - pair [property name, property
      *        value]...
      * @return the node
      */
     private Node createNode(String typeId, Object... additionalProperties) {
         Transaction tx = databaseService.beginTx();
         try {
             Node node = databaseService.createNode();
             setType(node, typeId);
             if (additionalProperties != null) {
                 for (int i = 0; i < additionalProperties.length - 1; i += 2) {
                     node.setProperty(String.valueOf(additionalProperties[i]), additionalProperties[i + 1]);
                 }
             }
             tx.success();
             return node;
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Sets the node name
      * 
      * @param node the node
      * @param name the name
      */
     private void setName(Node node, String name) {
         node.setProperty(INeoConstants.PROPERTY_NAME_NAME, name);
     }
 
     /**
      * Gets the name of node
      * 
      * @param node the node
      * @return the node name or null if node do not have necessary properties
      */
     private String getName(Node node) {
         return (String)node.getProperty(INeoConstants.PROPERTY_NAME_NAME, null);
     }
 
     /**
      * Creates the m node - created M node and add like child in CHILD-NEXT structure
      * 
      * @param parent the parent node
      * @param lastMNode the last m node, can be null
      * @return the created node with type 'm'
      */
     public Node createMNode(Node parent, Node lastMNode) {
         return createChild(parent, lastMNode, NodeTypes.M.getId());
     }
 
     /**
      * Gets the virtual dataset. if node not found, the necessary node was created
      * 
      * @param rootNode the root node
      * @param type the virtual dataset type
      * @return the virtual dataset node
      */
     public Node getVirtualDataset(Node rootNode, DriveTypes type) {
         Node result = findVirtualDataset(rootNode, type);
         if (result == null) {
             result = createNode(NodeTypes.DATASET.getId(), INeoConstants.PROPERTY_NAME_NAME, type.getFullDatasetName(getName(rootNode)), INeoConstants.DRIVE_TYPE, type.getId());
             Transaction tx = databaseService.beginTx();
             try {
                 rootNode.createRelationshipTo(result, GeoNeoRelationshipTypes.VIRTUAL_DATASET);
                 tx.success();
             } finally {
                 tx.finish();
             }
         }
         return result;
     }
 
     /**
      * Find virtual dataset.
      * 
      * @param rootNode the root node
      * @param type the virtual dataset type
      * @return the node or null if node do not found
      */
     public Node findVirtualDataset(Node rootNode, final DriveTypes type) {
         TraversalDescription td = Traversal.description().depthFirst().uniqueness(Uniqueness.NONE).prune(Traversal.pruneAfterDepth(1))
                 .relationships(GeoNeoRelationshipTypes.VIRTUAL_DATASET, Direction.OUTGOING).filter(new Predicate<Path>() {
 
                     @Override
                     public boolean accept(Path item) {
                         return item.length() == 1 && type == DriveTypes.getNodeType(item.endNode());
                     }
                 });
         Iterator<Node> it = td.traverse(rootNode).nodes().iterator();
         return it.hasNext() ? it.next() : null;
     }
 
     /**
      * Creates the ms node - created 'ms' node and add like child in CHILD-NEXT structure
      * 
      * @param parent the parent node
      * @param lastMsNode the last ms node, can be null
      * @return the created node
      */
     public Node createMsNode(Node parent, Node lastMsNode) {
         return createChild(parent, lastMsNode, NodeTypes.HEADER_MS.getId());
 
     }
 
     /**
      * Creates the mm node - created 'mm' node and add like child in CHILD-NEXT structure
      * 
      * @param parent the parent node
      * @param lastMsNode the last mm node, can be null
      * @return the created node
      */
     public Node createMMNode(Node parent, Node lastMsNode) {
         return createChild(parent, lastMsNode, NodeTypes.MM.getId());
     }
 
     /**
      * Creates the child for CHILD-NEXT structure
      * 
      * @param parent the parent parent node
      * @param lastNode the last node - last node, can be null
      * @param typeId the typeid of created node
      * @return the created node
      */
     private Node createChild(Node parent, Node lastNode, String typeId) {
         Node node = createNode(typeId);
         addChild(parent, node, lastNode);
         return node;
     }
 
     /**
      * Save dynamic node type in database
      * 
      * @param nodeTypeId the node type id
      */
     public void saveDynamicNodeType(String nodeTypeId) {
         nodeTypeId = nodeTypeId.toLowerCase().trim();
 
         Node node = getGlobalConfigNode();
         String[] types = (String[])node.getProperty(DYNAMIC_TYPES, new String[0]);
         String[] newTypes = new String[types.length + 1];
         for (int i = 0; i < types.length; i++) {
             if (types[i].equals(nodeTypeId))
                 return;
             newTypes[i] = types[i];
         }
         newTypes[newTypes.length - 1] = nodeTypeId;
         Transaction tx = databaseService.beginTx();
         try {
             node.setProperty(DYNAMIC_TYPES, newTypes);
             tx.success();
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Gets the global config node. IF node not exist in database, the global config node will be
      * created
      * 
      * @return the global config node
      */
     private Node getGlobalConfigNode() {
         Node refNode = databaseService.getReferenceNode();
         Relationship rel = refNode.getSingleRelationship(DatasetRelationshipTypes.GLOBAL_PROPERTIES, Direction.OUTGOING);
         if (rel == null) {
             Transaction tx = databaseService.beginTx();
             try {
                 Node globalPropertiesNode = createNode(NodeTypes.GLOBAL_PROPERTIES.getId(), INeoConstants.PROPERTY_NAME_NAME, "Global properties", DYNAMIC_TYPES, new String[0]);
                 refNode.createRelationshipTo(globalPropertiesNode, DatasetRelationshipTypes.GLOBAL_PROPERTIES);
                 tx.success();
             } finally {
                 tx.finish();
             }
             rel = refNode.getSingleRelationship(DatasetRelationshipTypes.GLOBAL_PROPERTIES, Direction.OUTGOING);
         }
         return rel.getEndNode();
     }
 
     /**
      * Gets the user defined node types.
      * 
      * @return the list of user defined node types, which stored in database
      */
     public List<INodeType> getUserDefinedNodeTypes() {
         List<INodeType> result = new ArrayList<INodeType>();
         Node globalConfigNode = getGlobalConfigNode();
         String[] types = (String[])globalConfigNode.getProperty(DYNAMIC_TYPES, new String[0]);
         for (int i = 0; i < types.length; i++) {
             result.add(new DynamicNodeType(types[i]));
         }
         return result;
     }
 
     /**
      * Gets the traverser by all child of necessary project
      * 
      * @param projectName the project name
      * @return the traverser
      */
     public org.neo4j.graphdb.traversal.Traverser getRoots(final String projectName) {
         TraversalDescription td = Utils.getTDRootNodes(new Predicate<Path>() {
 
             @Override
             public boolean accept(Path paramT) {
                 return projectName.equals(paramT.lastRelationship().getStartNode().getProperty(INeoConstants.PROPERTY_NAME_NAME, ""));
             }
         });
         return td.traverse(databaseService.getReferenceNode());
     }
 
     /**
      * Gets the node type.
      * 
      * @param type the node type id
      * @param createFake - is it necessary create type wrapper if type do not exist?
      * @return the node type or null if createFake==false&&type do not exist in project
      */
     public INodeType getNodeType(String type, boolean createFake) {
         INodeType result = getNodeType(type);
         if (result != null || !createFake) {
             return result;
         }
         return new DynamicNodeType(type);
     }
 
     /**
      * Sets the structure of root node
      * 
      * @param root the child of project node (network.dataset,"")
      * @param structure the structure - collection of INodeType for save
      */
     public void setStructure(Node root, Collection<INodeType> structure) {
         String[] structureProperty = new String[structure.size()];
         int i = 0;
         for (INodeType element : structure) {
             structureProperty[i++] = element.getId();
         }
         setStructure(root, structureProperty);
     }
 
     /**
      * Sets the structure of root node
      * 
      * @param root the child of project node (network.dataset,"")
      * @param structureProperty the structure - array of type id
      */
     public void setStructure(Node root, String[] structureProperty) {
         Transaction tx = databaseService.beginTx();
         try {
             root.setProperty(INeoConstants.PROPERTY_STRUCTURE_NAME, structureProperty);
             tx.success();
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Gets the list of the node type ids that keeped in network node.
      * 
      * @param sourceNode the source node
      * @return the sructure types
      */
     public List<INodeType> getSructureTypes(Node sourceNode) {
         String[] stTypes=getSructureTypesId(sourceNode);
         List<INodeType> result = new ArrayList<INodeType>(stTypes.length);
 
         for (int i = 0; i < stTypes.length; i++) {
             NodeTypes nodeType = NodeTypes.getEnumById(stTypes[i]);
             if (nodeType != null) {
                 result.add(nodeType);
             } else {
                 result.add(getNodeType(stTypes[i]));
             }
         }
         return result;
     }
     public String[] getSructureTypesId(Node sourceNode) {
         Node networkNode = Utils.getParentNode(sourceNode, NodeTypes.NETWORK.getId());
         return (String[])networkNode.getProperty(INeoConstants.PROPERTY_STRUCTURE_NAME, new String[0]);
     }
     /**
      * Find root node by child
      * 
      * @param node the child node
      * @return the root node or null if root node not found
      */
     public Node findRootByChild(Node node) {
         Traverser traverser = findProjectByChild(node);
         Iterator<Path> rel = traverser.iterator();
         if (rel.hasNext()) {
             Path next = rel.next();
             return next.lastRelationship().getEndNode();
         } else {
             return null;
         }
     }
 
     /**
      * Find project by child node
      * 
      * @param node the child node
      * @return the path traverser
      */
     public Traverser findProjectByChild(Node node) {
         TraversalDescription trd = Traversal.description().uniqueness(Uniqueness.NONE).depthFirst().relationships(GeoNeoRelationshipTypes.NEXT, Direction.INCOMING)
                 .relationships(GeoNeoRelationshipTypes.CHILD, Direction.INCOMING).relationships(GeoNeoRelationshipTypes.VIRTUAL_DATASET, Direction.INCOMING)
                 .filter(new Predicate<Path>() {
 
                     @Override
                     public boolean accept(Path item) {
                         return NodeTypes.AWE_PROJECT.checkNode(item.endNode());
                     }
                 });
         if (NodeTypes.GIS.checkNode(node)) {
             return trd.traverse(node.getSingleRelationship(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING).getEndNode());
         }
 
         return trd.traverse(node);
     }
     
     /**
      * Gets the gis node by dataset.
      * 
      * @param dataset the dataset
      * @return the gis node by dataset
      */
     public Node getGisNodeByDataset(Node dataset) {
         return dataset.getSingleRelationship(GeoNeoRelationshipTypes.NEXT, Direction.INCOMING).getStartNode();
     }
 
     /**
      * Get the GIS node from root node. If GIS node not exist, it will be create.
      * 
      * @param root the root node
      * @return the GIS node
      */
     public GisProperties getGisNode(Node root) {
 
         Node gis = findGisNode(root);
         if (gis == null) {
             Transaction tx = databaseService.beginTx();
             try {
                 gis = databaseService.createNode();
                 setName(gis, getName(root));
                 setType(gis, NodeTypes.GIS.getId());
                 INodeType type = getNodeType(root);
                 GisTypes gisType = GisTypes.getGisTypeFromRootType(type.getId());
                 gis.setProperty(INeoConstants.PROPERTY_GIS_TYPE_NAME, gisType.getHeader());
                 databaseService.getReferenceNode().createRelationshipTo(gis, GeoNeoRelationshipTypes.CHILD);
                 gis.createRelationshipTo(root, GeoNeoRelationshipTypes.NEXT);
                 tx.success();
             } finally {
                 tx.finish();
             }
         }
         return new GisProperties(gis);
     }
 
     /**
      * Find GIS node of root node.
      * 
      * @param root the root node
      * @return the GIS node or null if node not found
      */
     public Node findGisNode(Node root) {
         Assert.isTrue(Utils.isRoootNode(root));
         Node gis = null;
         Relationship rel = root.getSingleRelationship(GeoNeoRelationshipTypes.NEXT, Direction.INCOMING);
         if (rel != null) {
             gis = rel.getOtherNode(root);
         }
         return gis;
     }
 
     /**
      * Save GisProperties in GIS node
      * 
      * @param gis the GisProperties
      */
     public void saveGis(GisProperties gis) {
         Transaction tx = databaseService.beginTx();
         try {
             gis.save();
             tx.success();
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Gets the index manager for root node.
      * 
      * @param root the root node
      * @return the index manager
      */
     public IndexManager getIndexManager(Node root) {
         return new IndexManager(root);
     }
 
     /**
      * Find sector node Sector node find by many parameters: if ci==null&&returnFirsElement==true -
      * We return the first matching by name sector node or we use lucene index for finding nodes
      * with necessary ci and fing by others defined parameters.
      * 
      * @param rootNode the root node
      * @param ci the ci property
      * @param lac the lac property
      * @param name the sector name
      * @param returnFirsElement the return firs element
      * @return the sector node or null
      */
     public Node findSector(Node rootNode, Integer ci, Integer lac, String name, boolean returnFirsElement) {
         Node baseNode = getGlobalConfigNode();
         IndexService index = getIndexService();
         if (baseNode == null || (ci == null && name == null)) {
             return null;
         }
         assert baseNode != null && (ci != null || name != null) && index != null;
         if (ci == null) {
             String indexName = Utils.getLuceneIndexKeyByProperty(baseNode, INeoConstants.PROPERTY_NAME_NAME, NodeTypes.SECTOR);
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
         String indexName = Utils.getLuceneIndexKeyByProperty(baseNode, INeoConstants.PROPERTY_SECTOR_CI, NodeTypes.SECTOR);
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
     }
 
     /**
      * Gets the neighbour root node If neighbour not found, the necessary nodes was created
      * 
      * @param rootNode the root node (child of project node)
      * @param neighbourName the neighbour name
      * @return the neighbour node
      */
     public Node getNeighbour(Node rootNode, String neighbourName) {
         Node result = findNeighbour(rootNode, neighbourName);
         if (result == null) {
             Transaction tx = databaseService.beginTx();
             try {
                 result = createNode(NodeTypes.NEIGHBOUR, neighbourName);
                 rootNode.createRelationshipTo(result, NetworkRelationshipTypes.NEIGHBOUR_DATA);
                 tx.success();
             } finally {
                 tx.finish();
             }
         }
         return result;
     }
 
     /**
      * Gets the transmission root node If transmission not found, the necessary nodes was created
      * 
      * @param rootNode the root node (child of project node)
      * @param transmissionName the transmission name
      * @return the transmission node
      */
     public Node getTransmission(Node rootNode, String transmissionName) {
         Node result = findTransmission(rootNode, transmissionName);
         if (result == null) {
             Transaction tx = databaseService.beginTx();
             try {
                 result = createNode(NodeTypes.TRANSMISSION, transmissionName);
                 rootNode.createRelationshipTo(result, NetworkRelationshipTypes.TRANSMISSION_DATA);
                 tx.success();
             } finally {
                 tx.finish();
             }
         }
         return result;
     }
 
     /**
      * Find neighbour node
      * 
      * @param rootNode the root node
      * @param neighbourName the neighbour name
      * @return the neighbour node or null if node not found
      */
     public Node findNeighbour(final Node rootNode, final String neighbourName) {
         if (rootNode == null || StringUtils.isEmpty(neighbourName)) {
             return null;
         }
         TraversalDescription td = Traversal.description().uniqueness(Uniqueness.NONE).depthFirst().prune(Traversal.pruneAfterDepth(1))
                 .relationships(NetworkRelationshipTypes.NEIGHBOUR_DATA, Direction.OUTGOING).filter(new Predicate<Path>() {
 
                     @Override
                     public boolean accept(Path item) {
                         if (item.length() == 1 && NodeTypes.NEIGHBOUR.checkNode(item.endNode())) {
                             return neighbourName.equals(getName(item.endNode()));
                         }
                         return false;
                     }
                 });
         Iterator<Node> it = td.traverse(rootNode).nodes().iterator();
         return it.hasNext() ? it.next() : null;
     }
 
     /**
      * Find transmission node
      * 
      * @param rootNode the root node
      * @param transmissionName the transmission name
      * @return the transmission node or null if node not found
      */
     public Node findTransmission(final Node rootNode, final String transmissionName) {
         if (rootNode == null || StringUtils.isEmpty(transmissionName)) {
             return null;
         }
         TraversalDescription td = Traversal.description().uniqueness(Uniqueness.NONE).depthFirst().prune(Traversal.pruneAfterDepth(1))
                 .relationships(NetworkRelationshipTypes.TRANSMISSION_DATA, Direction.OUTGOING).filter(new Predicate<Path>() {
 
                     @Override
                     public boolean accept(Path item) {
                         if (item.length() == 1 && NodeTypes.TRANSMISSION.checkNode(item.endNode())) {
                             return transmissionName.equals(getName(item.endNode()));
                         }
                         return false;
                     }
                 });
         Iterator<Node> it = td.traverse(rootNode).nodes().iterator();
         return it.hasNext() ? it.next() : null;
     }
 
     /**
      * Gets the neighbour proxy.
      * 
      * @param neighbourRoot the neighbour root
      * @param sector the sector
      * @return the neighbour proxy
      */
     public NodeResult getNeighbourProxy(Node neighbourRoot, Node sector) {
         String proxySectorName = getName(neighbourRoot) + PROXY_NAME_SEPARATOR + getName(sector);
         String luceneIndexKeyByProperty = Utils.getLuceneIndexKeyByProperty(neighbourRoot, INeoConstants.PROPERTY_NAME_NAME, NodeTypes.SECTOR_SECTOR_RELATIONS);
         Node proxySector = null;
         for (Node node : getIndexService().getNodes(luceneIndexKeyByProperty, proxySectorName)) {
             if (node.getSingleRelationship(NetworkRelationshipTypes.NEIGHBOURS, Direction.INCOMING).getOtherNode(node).equals(sector)) {
                 proxySector = node;
                 break;
             }
         }
         boolean isCreated = false;
         if (proxySector == null) {
             Transaction tx = databaseService.beginTx();
             try {
                 proxySector = createNode(NodeTypes.SECTOR_SECTOR_RELATIONS, proxySectorName);
                 getIndexService().index(proxySector, luceneIndexKeyByProperty, proxySectorName);
                 isCreated = true;
                 // TODO check documentation
                 addChild(neighbourRoot, proxySector, null);
                 tx.success();
             } finally {
                 tx.finish();
             }
         }
         return new NodeResultImpl(proxySector, isCreated);
     }
 
     /**
      * The Interface NodeResult. - wrapper of Node which contains result of operation: Node was
      * found, or node was created
      */
     public interface NodeResult extends Node {
 
         /**
          * Checks if is created.
          * 
          * @return true, if is created
          */
         boolean isCreated();
     }
 
     /**
      * <p>
      * Implementation of NodeResult
      * </p>
      * 
      * @author tsinkel_a
      * @since 1.0.0
      */
     private static class NodeResultImpl extends NodeWrapper implements NodeResult {
 
         /** The is created. */
         private final boolean isCreated;
 
         /**
          * Instantiates a new node result impl.
          * 
          * @param node the node
          * @param isCreated the is created
          */
         public NodeResultImpl(Node node, boolean isCreated) {
             super(node);
             this.isCreated = isCreated;
         }
 
         /**
          * Checks if is created.
          * 
          * @return true, if is created
          */
         @Override
         public boolean isCreated() {
             return isCreated;
         }
 
     }
 
     /**
      * Find site.
      * 
      * @param rootNode the root node
      * @param name the site name
      * @param site_no the site nomer
      * @return the site node or null if node not found
      */
     public Node findSite(Node rootNode, String name, String site_no) {
         if (StringUtils.isNotEmpty(name)) {
             return getIndexService().getSingleNode(Utils.getLuceneIndexKeyByProperty(rootNode, INeoConstants.PROPERTY_NAME_NAME, NodeTypes.SITE), name);
         }
         if (StringUtils.isNotEmpty(site_no)) {
             return getIndexService().getSingleNode(Utils.getLuceneIndexKeyByProperty(rootNode, INeoConstants.PROPERTY_SITE_NO, NodeTypes.SITE), site_no);
         }
         return null;
     }
 
     /**
      * Gets the transmission proxy.
      * 
      * @param neighbourRoot the neighbour root
      * @param sector the sector
      * @return the neighbour proxy
      */
     public NodeResult getTransmissionProxy(Node transmissionRoot, Node site) {
         String proxySiteName = getName(transmissionRoot) + PROXY_NAME_SEPARATOR + getName(site);
         String luceneIndexKeyByProperty = Utils.getLuceneIndexKeyByProperty(transmissionRoot, INeoConstants.PROPERTY_NAME_NAME, NodeTypes.SITE_SITE_RELATIONS);
         Node proxySite = null;
         for (Node node : getIndexService().getNodes(luceneIndexKeyByProperty, proxySiteName)) {
             if (node.getSingleRelationship(NetworkRelationshipTypes.TRANSMISSIONS, Direction.INCOMING).getOtherNode(node).equals(site)) {
                 proxySite = node;
                 break;
             }
         }
         boolean isCreated = false;
         if (proxySite == null) {
             Transaction tx = databaseService.beginTx();
             try {
                 proxySite = createNode(NodeTypes.SITE_SITE_RELATIONS, proxySiteName);
                 getIndexService().index(proxySite, luceneIndexKeyByProperty, proxySiteName);
                 isCreated = true;
                 // TODO check documentation
                 addChild(transmissionRoot, proxySite, null);
                 tx.success();
             } finally {
                 tx.finish();
             }
         }
         return new NodeResultImpl(proxySite, isCreated);
 
     }
 
     /**
      * get Probe node
      * 
      * @param rootNode root node
      * @param probeName - probe name
      * @return the probe node
      */
     public NodeResult getProbe(Node rootNode, String probeName) {
         String indName = Utils.getLuceneIndexKeyByProperty(rootNode, INeoConstants.PROPERTY_NAME_NAME, NodeTypes.PROBE);
         boolean isCreated = false;
         Node result = getIndexService().getSingleNode(indName, probeName);
         if (result == null) {
             Transaction tx = databaseService.beginTx();
             try {
                 isCreated = true;
                 result = createNode(NodeTypes.PROBE, probeName);
                 getIndexService().index(result, indName, probeName);
                 isCreated = true;
                 rootNode.createRelationshipTo(result, GeoNeoRelationshipTypes.CHILD);
                 tx.success();
             } finally {
                 tx.finish();
             }
         }
         return new NodeResultImpl(result, isCreated);
     }
 
     public Traverser getSectorsOfSite(Node site) {
         //check node on Sector type
         if (!getNodeType(site).equals(NodeTypes.SITE)) {
             //TODO: better to throw an exception, but for now only return null
             return null;
         }
         
         return Traversal.description().breadthFirst().
                          prune(Traversal.pruneAfterDepth(1)).
                          relationships(NetworkRelationshipTypes.CHILD, Direction.OUTGOING).
                          filter(Traversal.returnAllButStartNode()).
                          traverse(site);
     }
     
     /**
      * Gets all dataset nodes.
      * 
      * @param service neoservice can not be null
      * @return Map
      */
     public Traverser getAllDatasetNodes() {
         return Traversal.description().depthFirst().prune(Traversal.pruneAfterDepth(2)).filter(new Predicate<Path>() {
 
             @Override
             public boolean accept(Path item) {
                 return item.endNode().hasProperty(INeoConstants.PROPERTY_TYPE_NAME) &&
                        (item.endNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME).equals(NodeTypes.DATASET.getId()) ||
                         item.endNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME).equals(NodeTypes.NETWORK.getId()));
             }
         }).traverse(databaseService.getReferenceNode());        
     }
     
     /**
      * Gets the dataset node by gis.
      * 
      * @param gis the gis
      * @return the dataset node by gis
      */
     public Node getDatasetNodeByGis(Node gis) {
         return gis.getSingleRelationship(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING).getEndNode();
     }
 
 
     /**
      * Checks if is reference node.
      *
      * @param node the node
      * @return true, if is reference node
      */
     public boolean isReferenceNode(Node node) {
         return node!=null&&databaseService.getReferenceNode().getId()==node.getId();
     }
 }
