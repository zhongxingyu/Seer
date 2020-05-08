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
 package org.amanzi.neo.services.node2node;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.amanzi.neo.services.AbstractService;
 import org.amanzi.neo.services.DatasetService;
 import org.amanzi.neo.services.NeoServiceFactory;
 import org.amanzi.neo.services.enums.DatasetRelationshipTypes;
 import org.amanzi.neo.services.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.services.enums.NodeTypes;
 import org.amanzi.neo.services.utils.AggregateRules;
 import org.amanzi.neo.services.utils.Utils;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Path;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.RelationshipType;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.traversal.Evaluation;
 import org.neo4j.graphdb.traversal.Evaluator;
 import org.neo4j.graphdb.traversal.TraversalDescription;
 import org.neo4j.graphdb.traversal.Traverser;
 import org.neo4j.kernel.Traversal;
 import org.neo4j.kernel.Uniqueness;
 
 /**
  * <p>
  * Service Node to node relation
  * </p>
  * 
  * @author Kasnitskij_V
  * @since 1.0.0
  */
 public class NodeToNodeRelationService extends AbstractService {
     private static final String N2N_TYPE = "node2node";
     private static final String COUNT_PROXY = "count_proxy";
     private static final String COUNT_RELATION = "count_rel";
 
    public enum NodeToNodeRelationshipTypes implements RelationshipType {
         SET_TO_ROOT, PROXYS;
     }
 
     private DatasetService datasetService;
 
     /**
      * constructor
      */
     public NodeToNodeRelationService() {
         datasetService = NeoServiceFactory.getInstance().getDatasetService();
     }
 
     /**
      * find root
      * 
      * @param rootNode root node
      * @param type type of relation
      * @param nameRelation name of relation
      * @return finded node
      */
     public Node findNodeToNodeRelationsRoot(Node rootNode, INodeToNodeType type, final String nameRelation) {
         final String typename = type.name();
         Traverser traverser = Traversal.description().uniqueness(Uniqueness.NONE).depthFirst()
                 .relationships(NodeToNodeRelationshipTypes.SET_TO_ROOT, Direction.OUTGOING).evaluator(new Evaluator() {
 
                     @Override
                     public Evaluation evaluate(Path arg0) {
                         boolean continues = arg0.length() == 0;
                         boolean includes = !continues && nameRelation.equals(datasetService.getNodeName(arg0.endNode()))&&typename.equals(getNodeToNodeType(arg0.endNode()));
                         return Evaluation.of(includes, continues);
                     }
                 }).traverse(rootNode);
 
         Iterator<Node> nodes = traverser.nodes().iterator();
         return nodes.hasNext() ? nodes.next() : null;
     }
 
     public String getNodeToNodeType(Node node) {
         return (String)node.getProperty(N2N_TYPE, null);
     }
 
     /**
      * method to create root
      * 
      * @param rootNode root node
      * @param type type of relation
      * @param name name of created node
      * @return
      */
     private Node createNodeToNodeRelationsRoot(Node rootNode, INodeToNodeType type, String name) {
         Transaction tx = databaseService.beginTx();
 
         try {
             Node createdNode = datasetService.createNode(NodeTypes.ROOT_PROXY, name);
             createdNode.setProperty(N2N_TYPE, type.name());
             rootNode.createRelationshipTo(createdNode, NodeToNodeRelationshipTypes.SET_TO_ROOT);
             tx.success();
 
             return createdNode;
         } finally {
             tx.finish();
         }
     }
 
     /**
      * find or create node
      * 
      * @param rootNode root node
      * @param type type of relation
      * @param name name of node
      * @return finded or created node
      */
     public Node getNodeToNodeRelationsRoot(Node rootNode, INodeToNodeType type, String name) {
         Node returnedNode = findNodeToNodeRelationsRoot(rootNode, type, name);
 
         if (returnedNode == null) {
             returnedNode = createNodeToNodeRelationsRoot(rootNode, type, name);
         }
 
         return returnedNode;
     }
 
     /**
      * find proxy node
      * 
      * @param indexKey key of index
      * @param indexValue value of index
      * @return finded node
      */
     public Node findProxy(String indexKey, String indexValue) {
         return getIndexService().getSingleNode(indexKey, indexValue);
     }
 
     public Node getProxy(Node rootNode, String proxyIndexKey, Node node) {
         String name = String.valueOf(node.getId());// datasetService.getNodeName(node);
         Node result = findProxy(proxyIndexKey, name);
         if (result == null) {
             result = createProxy(node, proxyIndexKey, name, rootNode);
         }
         return result;
     }
 
     /**
      * create proxy node
      * 
      * @param index index of node
      * @param name name of node
      * @param rootNode root node
      * @param lastChild last child node
      * @return
      */
     public Node createProxy(Node originalNode, String index, String name, Node rootNode) {
         Transaction tx = databaseService.beginTx();
         try {
             Node node = datasetService.createNode(NodeTypes.PROXY, datasetService.getNodeName(originalNode));
             datasetService.addChild(rootNode, node, null);
             rootNode.setProperty(COUNT_PROXY, getCountProxy(rootNode) + 1);
             getIndexService().index(node, index, name);
             originalNode.createRelationshipTo(node, DatasetRelationshipTypes.PROXY);
             tx.success();
             return node;
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Clears Node2Node relations model
      * 
      * @param rootNode root node of structure
      * @param indexKeys used names of indexes
      * @param deleteRootNode is it need to delete root node of model
      */
     public void clearNodeToNodeStructure(Node rootNode, String[] indexKeys, boolean deleteRootNode) {
         Iterator<Node> proxyNodes = datasetService.getChildTraversal(null).traverse(rootNode).nodes().iterator();
         while (proxyNodes.hasNext()) {
             Node proxy = proxyNodes.next();
 
             Iterator<Relationship> relationIterator = proxy.getRelationships().iterator();
             while (relationIterator.hasNext()) {
                 relationIterator.next().delete();
             }
 
             proxy.delete();
         }
 
         for (String singleIndex : indexKeys) {
             getIndexService().removeIndex(singleIndex);
         }
 
         if (deleteRootNode) {
             rootNode.delete();
         }
     }
 
     public String getIndexKeyForRelation(Node rootNode, RelationshipType relationshipType) {
         return new StringBuilder("Id").append(rootNode.getId()).append("@").append(relationshipType.name()).toString();
     }
 
     public long getCountProxy(Node rootNode) {
         return (Long)rootNode.getProperty(COUNT_PROXY, 0l);
     }
 
     public long getCountRelation(Node rootNode) {
         return (Long)rootNode.getProperty(COUNT_RELATION, 0l);
     }
 
     public Relationship getRelation(Node rootNode, String proxyIndexKey, Node servingNode, Node dependentNode) {
         Node proxyServ=getProxy(rootNode, proxyIndexKey, servingNode);
         Node neigh=getProxy(rootNode, proxyIndexKey, dependentNode);
         Set<Relationship> relations = Utils.getRelations(proxyServ, neigh, NodeToNodeRelationshipTypes.PROXYS);
         if (relations.isEmpty()){
             Transaction tx = databaseService.beginTx();
             try{
                 Relationship rel = proxyServ.createRelationshipTo(neigh, NodeToNodeRelationshipTypes.PROXYS);
                 rootNode.setProperty(COUNT_RELATION, getCountRelation(rootNode)+1);
                 tx.success();
                 return rel;
             }finally{
                 tx.finish();
             }
         }else{
             return relations.iterator().next();
         }
     }
 
     public Set<NodeToNodeRelationModel> findAllNode2NodeRoot(Node network) {
         Set<NodeToNodeRelationModel> result=new HashSet<NodeToNodeRelationModel>();
         for (Node root:getAllNode2NodeRoots(network, null).nodes()){
             result.add(new NodeToNodeRelationModel(root)); 
         }
         return result;
     }
     public Traverser getAllNode2NodeRoots(Node network,Evaluator additionalEvaluator){
        TraversalDescription td = Traversal.description().uniqueness(Uniqueness.NONE).depthFirst().relationships(NodeToNodeRelationshipTypes.SET_TO_ROOT,Direction.OUTGOING).evaluator(new Evaluator() {
         
         @Override
         public Evaluation evaluate(Path arg0) {
             boolean continues=arg0.length()==0;
             boolean includes=!continues;
             return Evaluation.of(includes, continues);
         }
     });
        if (additionalEvaluator!=null){
            td.evaluator(additionalEvaluator);
        }
        return td.traverse(network);
     }
 
     /**
      * Gets the neighbhur traverser.
      *
      * @param rootNode the n2n root node
      * @param additionalEvaluator the additional evaluator
      * @return the neigh traverser
      */
     public Traverser getNeighTraverser(Node rootNode, Evaluator additionalEvaluator) {
         TraversalDescription td = Traversal.description().uniqueness(Uniqueness.NONE).depthFirst()
                 .relationships(GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING)
                 .relationships(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING)
                 .relationships(NodeToNodeRelationshipTypes.PROXYS, Direction.OUTGOING).evaluator(new Evaluator() {
 
                     @Override
                     public Evaluation evaluate(Path arg0) {
                         boolean includes = arg0.lastRelationship() != null
                                 && arg0.lastRelationship().isType(NodeToNodeRelationshipTypes.PROXYS);
                         boolean prune = includes
                                 || (arg0.length() == 1 && !arg0.lastRelationship().isType(GeoNeoRelationshipTypes.CHILD))
                                 || (arg0.length() > 1 && !arg0.lastRelationship().isType(GeoNeoRelationshipTypes.NEXT));
                         return Evaluation.of(includes, !prune);
                     }
                 });
         if (additionalEvaluator != null) {
             td.evaluator(additionalEvaluator);
         }
         return td.traverse(rootNode);
     }
 
     /**
      * Gets the serv traverser.
      *
      * @param rootNode the n2n root node
      * @param evaluator the evaluator
      * @return the serv traverser
      */
     public Traverser getServTraverser(Node rootNode, Evaluator additionalEvaluator) {
         TraversalDescription td = Traversal.description().uniqueness(Uniqueness.NONE).depthFirst()
                 .relationships(GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING)
                 .relationships(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING).evaluator(new Evaluator() {
 
                     @Override
                     public Evaluation evaluate(Path arg0) {
                          boolean prune =  (arg0.length() == 1 && !arg0.lastRelationship().isType(GeoNeoRelationshipTypes.CHILD))
                                 || (arg0.length() > 1 && !arg0.lastRelationship().isType(GeoNeoRelationshipTypes.NEXT));
                          boolean includes = !prune&&arg0.length()>0&&arg0.endNode().hasRelationship(NodeToNodeRelationshipTypes.PROXYS,Direction.OUTGOING);
                         return Evaluation.of(includes, !prune);
                     }
                 });
         if (additionalEvaluator != null) {
             td.evaluator(additionalEvaluator);
         }
         return td.traverse(rootNode);
 
     }
 
     /**
      * Gets the serv exist property evaluator.
      *
      * @param propName the prop name
      * @return the serv exist property evaluator
      */
     public static Evaluator getServExistPropertyEvaluator(final String propName) {
         return new Evaluator() {
             
             @Override
             public Evaluation evaluate(Path arg0) {
                 for (Relationship rel:arg0.endNode().getRelationships(NodeToNodeRelationshipTypes.PROXYS,Direction.OUTGOING)){
                     if (rel.hasProperty(propName)){
                         return Evaluation.of(true, true);
                     }
                 }
                 return Evaluation.of(false, true);
             }
         };
     }
 
     /**
      * Gets the serv aggregated values.
      *
      * @param <T> the generic type
      * @param klass the klass
      * @param rule the rule
      * @param serv the serv
      * @param propertyName the property name
      * @return the serv aggregated values
      */
     @SuppressWarnings("unchecked")
     public <T>T getServAggregatedValues(Class<T> klass, AggregateRules rule, Node serv, String propertyName) {
         List<T> values=new ArrayList<T>();
         for (Relationship rel:serv.getRelationships(NodeToNodeRelationshipTypes.PROXYS,Direction.OUTGOING)){
             try {
                 T value=(T)rel.getProperty(propertyName,null);
                 if (value!=null){
                     values.add(value);
                 }
             } catch (ClassCastException e) {
                 //TODO handle exception
                 e.printStackTrace();
                 continue;
             }
         }
         return Utils.getAggregatedValue(klass,rule,values);
     }
 
     public Node getNetworkNode(Node rootNode) {
         Relationship rel = rootNode.getSingleRelationship(NodeToNodeRelationshipTypes.SET_TO_ROOT, Direction.INCOMING);
         return rel==null?null:rel.getOtherNode(rootNode);
     }
 
     public Iterable<Relationship> getOutgoingRelation(Node servNode) {
         return servNode.getRelationships(NodeToNodeRelationshipTypes.PROXYS,Direction.OUTGOING);
     }
 //TODO why do not work with 2 traverser?
     public Iterable<Relationship> getRelationTraverserByServNode(final Iterable<Node> filteredServNodes) {
         return new Iterable<Relationship>() {
             
             @Override
             public Iterator<Relationship> iterator() {
                 return new Iterator<Relationship>() {
                     Iterator<Node> itr1 = null;
                     Iterator<Relationship> itr2 = null;
                     private Relationship next=null;
                     @Override
                     public void remove() {
                         throw new UnsupportedOperationException();
                     }
                     
                     @Override
                     public Relationship next() {
                         Relationship result=null;
                         if (hasNext()){
                             result=next;
                             next=null;
                         }
                         return result;
                     }
                     
                     @Override
                     public boolean hasNext() {
                         if (next!=null){
                             return true;
                         }
                         if (itr1==null){
                             itr1=filteredServNodes.iterator();
                         }
                         while(itr2==null||!itr2.hasNext()){
                             if (itr1.hasNext()){
                                 Node next2 = itr1.next();
                                 itr2=next2.getRelationships(NodeToNodeRelationshipTypes.PROXYS,Direction.OUTGOING).iterator();
                             }else{
                                 break;
                             }
                         }
                         if (itr2==null||!itr2.hasNext()){
                             return false;
                         }
                         next=itr2.next();
                         return true;
                     }
                 };
             }
         };
     }
 
     public Set<NodeToNodeRelationModel> findAllN2nModels(Node rootNode, final NodeToNodeTypes type) {
         TraversalDescription td = Traversal.description().depthFirst().uniqueness(Uniqueness.NONE).relationships(NodeToNodeRelationshipTypes.SET_TO_ROOT,Direction.OUTGOING).evaluator(new Evaluator() {
             
             @Override
             public Evaluation evaluate(Path arg0) {
                 boolean continues=arg0.length()<1;
                 boolean includes=!continues&&type.name().equals(getNodeToNodeType(arg0.endNode()));
                 return Evaluation.of(includes, continues);
             }
         });
         Set<NodeToNodeRelationModel> result=new HashSet<NodeToNodeRelationModel>();
         for (Node node:td.traverse(rootNode).nodes()){
             result.add(new NodeToNodeRelationModel(node));
         }
         return result;
     }
 
     public Node findNodeFromProxy(Node proxyServ) {
         return proxyServ.getSingleRelationship(DatasetRelationshipTypes.PROXY, Direction.INCOMING).getOtherNode(proxyServ);
     }
 
     public Iterable<Relationship> getOutgoingRelations(Node proxyServ) {
         return proxyServ.getRelationships(NodeToNodeRelationshipTypes.PROXYS,Direction.OUTGOING);
     }
 }
