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
 
 package org.amanzi.neo.services.correlation;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.amanzi.neo.db.manager.NeoServiceProvider;
 import org.amanzi.neo.services.AbstractService;
 import org.amanzi.neo.services.INeoConstants;
 import org.amanzi.neo.services.enums.CorrelationRelationshipTypes;
 import org.amanzi.neo.services.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.services.enums.NodeTypes;
 import org.amanzi.neo.services.enums.SectorIdentificationType;
 import org.amanzi.neo.services.utils.Utils;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Path;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.RelationshipType;
 import org.neo4j.graphdb.ReturnableEvaluator;
 import org.neo4j.graphdb.StopEvaluator;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.TraversalPosition;
 import org.neo4j.graphdb.Traverser.Order;
 import org.neo4j.graphdb.traversal.TraversalDescription;
 import org.neo4j.helpers.Predicate;
 import org.neo4j.index.lucene.LuceneIndexService;
 import org.neo4j.kernel.Traversal;
 
 /**
  * <p>
  * Service for correlation
  * </p>
  * .
  * 
  * @author Saelenchits_N
  * @since 1.0.0
  */
 public class CorrelationService extends AbstractService {
 
     /** The lucene service. */
     private final LuceneIndexService luceneService;
 
     /** The model. */
     private CorrelationModel model;
 
     /**
      * Instantiates a new correlation service.
      */
     public CorrelationService() {
         super();
         luceneService = NeoServiceProvider.getProvider().getIndexService();
     }
 
     /**
      * Instantiates a new correlation service.
      * 
      * @param databaseService the database service
      */
     public CorrelationService(GraphDatabaseService databaseService) {
         super(databaseService);
         luceneService = NeoServiceProvider.getProvider().getIndexService();
     }
 
     /**
      * Removes the correlation.
      * 
      * @param dataNodes the data nodes
      * @param rootCorrelationNode the root correlation node
      */
     private void removeCorrelation(Set<Node> dataNodes, Node rootCorrelationNode) {
         if (dataNodes.isEmpty()) {
             return;
         }
 
         ArrayList<String> datasetNames = new ArrayList<String>();
         for (Node datasetNode : dataNodes) {
             datasetNames.add(Utils.getNodeName(datasetNode, databaseService));
         }
         
       RelationshipType[] types = new RelationshipType[] {CorrelationRelationshipTypes.CORRELATED/*, NetworkRelationshipTypes.DRIVE*/};
 
         // clear correlation between sectors and M nodes
         for (Node correlationNode : rootCorrelationNode.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE,
                 GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING)) {
             for (RelationshipType typeToCheck : types) {
                 for (Relationship correlationRel : correlationNode.getRelationships(typeToCheck, Direction.OUTGOING)) {
                     String datasetName = (String)correlationRel.getProperty(INeoConstants.NETWORK_GIS_NAME);
                     if (datasetNames.contains(datasetName)) {
                         correlationRel.delete();
                     }
                 }
             }
 
             boolean delete = true;
             for (RelationshipType typeToCheck : types) {
                 delete = delete && !correlationNode.getRelationships(typeToCheck, Direction.OUTGOING).iterator().hasNext();
             }
 
             if (delete) {
                 correlationNode.getSingleRelationship(CorrelationRelationshipTypes.CORRELATION, Direction.OUTGOING).delete();
                 correlationNode.getSingleRelationship(GeoNeoRelationshipTypes.CHILD, Direction.INCOMING).delete();
                 correlationNode.delete();
             }
         }
         
 
         // clear correlation between Dataset and Network
         for (Relationship datasetLink : rootCorrelationNode.getRelationships(CorrelationRelationshipTypes.CORRELATED, Direction.INCOMING)) {
             if (dataNodes.contains(datasetLink.getStartNode())) {
                 datasetLink.delete();
             }
         }
 
         // if there are no correlation for this Network we should clear Root Correlation Node
         if (!rootCorrelationNode.getRelationships(CorrelationRelationshipTypes.CORRELATED, Direction.INCOMING).iterator().hasNext()) {
             rootCorrelationNode.getSingleRelationship(CorrelationRelationshipTypes.CORRELATION, Direction.INCOMING).delete();
             rootCorrelationNode.delete();
         }
     }
 
     /**
      * Run correlation.
      * 
      * @param correlationModel the correlation model
      */
     public void runCorrelation(CorrelationModel correlationModel) {
         this.model = correlationModel;
 
         Node networkNode = model.getNetwork();
         Node rootCorrelationNode = getRootCorrelationNode(networkNode, !model.getDatasets().isEmpty());
         if (model.getDatasets().isEmpty() && rootCorrelationNode == null)
             return;
 
         Set<Node> datasetsToClear = new HashSet<Node>();
 
         Transaction tx = databaseService.beginTx();
         try {
             Iterable<Relationship> links = rootCorrelationNode.getRelationships(CorrelationRelationshipTypes.CORRELATED, Direction.INCOMING);
             for (Relationship link : links) {
                 Node correlatedNode = link.getStartNode();
                 if (model.getDatasets().contains(correlatedNode)) {
                     model.getDatasets().remove(correlatedNode);
                     continue;
                 } else {
                     datasetsToClear.add(correlatedNode);
                 }
             }
 
             HashMap<Node, Relationship> statistic = new HashMap<Node, Relationship>();
             for (Node dataNode : model.getDatasets()) {
                 Relationship rel = dataNode.createRelationshipTo(rootCorrelationNode, CorrelationRelationshipTypes.CORRELATED);
                 statistic.put(dataNode, rel);
             }
             int counter = 0;
             String networkName = Utils.getNodeName(networkNode, databaseService);
             if(!model.getDatasets().isEmpty())
             for (Node sector : getNetworkIterator(networkNode)) {
                 Node correlationNode = null;
 
                 HashMap<SectorIdentificationType, String> searchValues = new HashMap<SectorIdentificationType, String>();
 
                 for (Node driveNode : model.getDatasets()) {
                     // int driveCounter = 0;
                     // driveCounter++;
                     SectorIdentificationType searchType = SectorIdentificationType.valueOf((String)driveNode.getProperty(INeoConstants.SECTOR_ID_TYPE));
                     String sectorId = searchValues.get(searchType);
                     if (sectorId == null) {
                         String searchProp = searchType.getProperty();
                         Object property = sector.getProperty(searchProp, null);
                         if (property == null) {
                             System.out.println("sector [" + sector.getId() + "," + sector.getProperty("name", null) + "] does not have the property " + searchProp);
                             continue;
                         }
                         sectorId = property.toString();
                         searchValues.put(searchType, sectorId);
                     }
                     String searchIndex = Utils.getLuceneIndexKeyByProperty(driveNode, INeoConstants.SECTOR_ID_PROPERTIES, NodeTypes.M);
                     Iterator<Node> nodes = luceneService.getNodes(searchIndex, sectorId).iterator();
 
                     if (nodes.hasNext() && (correlationNode == null)) {
                         correlationNode = getCorrelationNode(networkNode, rootCorrelationNode, sectorId);
                        counter++;
                     }
 
                     while (nodes.hasNext()) {
                         Node mNode = nodes.next();
                         correlateNodes(sector, correlationNode, mNode, Utils.getNodeName(driveNode, databaseService), networkName);
                        counter++;
                         Relationship rel = statistic.get(driveNode);
                         rel.setProperty(INeoConstants.PROPERTY_COUNT_NAME, (Integer)rel.getProperty(INeoConstants.PROPERTY_COUNT_NAME, 0) + 1);
 
                         Object timestamp = mNode.getProperty(INeoConstants.PROPERTY_TIMESTAMP_NAME, null);
                         if (timestamp != null)
                             updateTimestampMinMax(rel, (Long)timestamp);
                        
                        if (counter > 1000) {
                            tx.success();
                            tx.finish();

                            tx = databaseService.beginTx();
                            counter = 0;
                        }
                     }
                     // correlationNode.setProperty(INeoConstants.COUNT_TYPE_NAME, driveCounter);
                    
                 }
             }
 
             removeCorrelation(datasetsToClear, rootCorrelationNode);
 
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             tx.success();
             tx.finish();
         }
 
     }
    
    
	private void updateObjectCounter() {
	}
 
     /**
      * Update timestamp min max.
      * 
      * @param rel the rel
      * @param timestamp the timestamp
      */
     protected void updateTimestampMinMax(Relationship rel, long timestamp) {
         Long minValue = (Long)rel.getProperty(INeoConstants.PROPERTY_NAME_MIN_VALUE, null);
         Long maxValue = (Long)rel.getProperty(INeoConstants.PROPERTY_NAME_MAX_VALUE, null);
         Long minTimeStamp = minValue == null ? timestamp : Math.min(minValue, timestamp);
         Long maxTimeStamp = maxValue == null ? timestamp : Math.max(maxValue, timestamp);
         rel.setProperty(INeoConstants.PROPERTY_NAME_MIN_VALUE, minTimeStamp);
         rel.setProperty(INeoConstants.PROPERTY_NAME_MAX_VALUE, maxTimeStamp);
     }
 
     /**
      * Adds the single correlation.
      * 
      * @param networkNode the network node
      * @param datasetNode the dataset node
      */
     public void addSingleCorrelation(Node networkNode, Node datasetNode) {
         CorrelationModel correlationModel = getCorrelationModel(networkNode);
         if (correlationModel.getDatasets().contains(datasetNode))
             return;
         correlationModel.getDatasets().add(datasetNode);
         runCorrelation(correlationModel);
     }
 
     /**
      * Removes the single correlation.
      * 
      * @param networkNode the network node
      * @param datasetNode the dataset node
      */
     public void removeSingleCorrelation(Node networkNode, Node datasetNode) {
         CorrelationModel correlationModel = getCorrelationModel(networkNode);
         if (!correlationModel.getDatasets().contains(datasetNode))
             return;
         correlationModel.getDatasets().remove(datasetNode);
         runCorrelation(correlationModel);
     }
 
     /**
      * Gets the correlation node.
      * 
      * @param networkNode the network node
      * @param rootCorrelationNode the root correlation node
      * @param sectorId the sector id
      * @return the correlation node
      */
     private Node getCorrelationNode(Node networkNode, Node rootCorrelationNode, String sectorId) {
         String networkName = Utils.getNodeName(networkNode, databaseService);
         String luceneIndexKey = networkName + "@Correlation";
         Node node = luceneService.getSingleNode(luceneIndexKey, sectorId);
 
         if (node == null) {
             node = databaseService.createNode();
 
             node.setProperty(INeoConstants.SECTOR_ID_PROPERTIES, sectorId);
             node.setProperty(INeoConstants.PROPERTY_NAME_NAME, networkName);
             luceneService.index(node, luceneIndexKey, sectorId);
             rootCorrelationNode.createRelationshipTo(node, GeoNeoRelationshipTypes.CHILD);
         }
 
         return node;
     }
 
     /**
      * Correlate nodes.
      * 
      * @param sectorNode the sector node
      * @param correlationNode the correlation node
      * @param correlatedNode the correlated node
      * @param correlationType the correlation type
      * @param networkName the network name
      */
     private void correlateNodes(Node sectorNode, Node correlationNode, Node correlatedNode, String correlationType, String networkName) {
         boolean create = !correlationNode.hasRelationship(CorrelationRelationshipTypes.CORRELATION, Direction.OUTGOING);
 
         Relationship link;
         if (create) {
             link = correlationNode.createRelationshipTo(sectorNode, CorrelationRelationshipTypes.CORRELATION);
             link.setProperty(INeoConstants.NETWORK_GIS_NAME, networkName);
         }
 
 //        Relationship locationLink = correlatedNode.getSingleRelationship(GeoNeoRelationshipTypes.LOCATION, Direction.OUTGOING);
 //        if (locationLink != null) {
 //            Node locationNode = locationLink.getEndNode();
 //
 //            // link = correlationNode.createRelationshipTo(locationNode,
 //            // CorrelationRelationshipTypes.CORRELATED_LOCATION);
 //            // link.setProperty(INeoConstants.NETWORK_GIS_NAME, correlationType);
 //
 ////            link = correlationNode.createRelationshipTo(locationNode, NetworkRelationshipTypes.DRIVE);
 ////            link.setProperty(INeoConstants.NETWORK_GIS_NAME, correlationType);
 //        }
 
         link = correlationNode.createRelationshipTo(correlatedNode, CorrelationRelationshipTypes.CORRELATED);
         link.setProperty(INeoConstants.NETWORK_GIS_NAME, correlationType);
     }
 
     /**
      * Gets the network iterator.
      * 
      * @param networkNode the network node
      * @return the network iterator
      */
     private Iterable<Node> getNetworkIterator(Node networkNode) {
         return networkNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
 
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 return Utils.getNodeType(currentPos.currentNode()).equals(NodeTypes.SECTOR.getId());
             }
         }, GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING, GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING);
     }
 
     /**
      * Gets the root correlation node.
      * 
      * @param networkNode the network node
      * @param toCreate the to create
      * @return the root correlation node
      */
     private Node getRootCorrelationNode(Node networkNode, boolean toCreate) {
         Transaction tx = databaseService.beginTx();
 
         try {
             Relationship link = networkNode.getSingleRelationship(CorrelationRelationshipTypes.CORRELATION, Direction.OUTGOING);
 
             if (link != null) {
                 return link.getEndNode();
             }
 
             Node result = null;
 
             if (toCreate) {
                 result = databaseService.createNode();
                 result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.ROOT_SECTOR_DRIVE.getId());
 
                 networkNode.createRelationshipTo(result, CorrelationRelationshipTypes.CORRELATION);
             }
 
             return result;
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         } finally {
             tx.success();
             tx.finish();
         }
     }
 
     /**
      * Gets the correlation model.
      * 
      * @param networkNode the network node
      * @return the correlation model
      */
     public CorrelationModel getCorrelationModel(Node networkNode) {
 
         TraversalDescription td = Traversal.description().depthFirst().filter(new Predicate<Path>() {
 
             @Override
             public boolean accept(Path item) {
                 return (item.length() > 0) && item.lastRelationship().isType(CorrelationRelationshipTypes.CORRELATED);
             }
         }).prune(Traversal.pruneAfterDepth(2)).relationships(CorrelationRelationshipTypes.CORRELATION, Direction.OUTGOING).relationships(
                 CorrelationRelationshipTypes.CORRELATED, Direction.INCOMING);
         HashMap<Node, Relationship> relationshipsMap = new HashMap<Node, Relationship>();
         for (Path path : td.traverse(networkNode)) {
             Relationship lastRel = path.lastRelationship();
             Node dataset = lastRel.getStartNode();
             relationshipsMap.put(dataset, lastRel);
         }
 
         return new CorrelationModel(networkNode, relationshipsMap);
 
     }
 }
