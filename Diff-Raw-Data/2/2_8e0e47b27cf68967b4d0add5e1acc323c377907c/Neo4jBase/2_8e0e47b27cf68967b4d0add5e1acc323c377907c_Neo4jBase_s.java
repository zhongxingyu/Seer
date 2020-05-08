 package de.deepamehta.hypergraph.impl;
 
 import de.deepamehta.hypergraph.ConnectedHyperEdge;
 import de.deepamehta.hypergraph.ConnectedHyperNode;
 import de.deepamehta.hypergraph.HyperNode;
 import de.deepamehta.hypergraph.HyperEdge;
 
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.DynamicRelationshipType;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Path;
 import org.neo4j.graphdb.PropertyContainer;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.RelationshipType;
 import org.neo4j.graphdb.traversal.Evaluation;
 import org.neo4j.graphdb.traversal.Evaluator;
 import org.neo4j.graphdb.traversal.TraversalDescription;
 // FIXME: new index API doesn't work with OSGi
 // import org.neo4j.graphdb.index.Index;
 //
 // Using old index API instead
 import org.neo4j.index.IndexHits;
 import org.neo4j.index.IndexService;
 import org.neo4j.index.lucene.LuceneIndexService;
 import org.neo4j.index.lucene.LuceneFulltextQueryIndexService;
 import org.neo4j.kernel.Traversal;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 
 
 class Neo4jBase {
 
     // ------------------------------------------------------------------------------------------------------- Constants
 
     protected static final String KEY_IS_HYPER_EDGE = "_is_hyper_edge";
 
     protected static final String KEY_FULLTEXT = "_fulltext";
 
     // ---------------------------------------------------------------------------------------------- Instance Variables
 
     private final Logger logger = Logger.getLogger(getClass().getName());
 
     protected GraphDatabaseService neo4j;
     // FIXME: new index API doesn't work with OSGi
     // protected Index<Node> exactIndex;
     // protected Index<Node> fulltextIndex;
     //
     // Using old index API instead
     protected IndexService exactIndex;
     protected LuceneFulltextQueryIndexService fulltextIndex;
 
     // ---------------------------------------------------------------------------------------------------- Constructors
 
     protected Neo4jBase(GraphDatabaseService neo4j) {
         this.neo4j = neo4j;
     }
 
     protected Neo4jBase(GraphDatabaseService neo4j, IndexService exactIndex,
                         LuceneFulltextQueryIndexService fulltextIndex
                         /* FIXME: Index exactIndex, Index fulltextIndex */) {
         this.neo4j = neo4j;
         this.exactIndex = exactIndex;
         this.fulltextIndex = fulltextIndex;
     }
 
     // ----------------------------------------------------------------------------------------------- Protected Methods
 
     protected final HyperNode buildHyperNode(Node node) {
         if (node == null) {
             throw new IllegalArgumentException("Tried to build a HyperNode from a null Node");
         }
         if (isAuxiliaryNode(node)) {
             throw new IllegalArgumentException("ID " + node.getId() + " refers not to a HyperNode but to a HyperEdge");
         }
         return new Neo4jHyperNode(node, neo4j, exactIndex, fulltextIndex);
     }
 
     protected final HyperEdge buildHyperEdge(Node auxiliaryNode) {
         if (auxiliaryNode == null) {
             throw new IllegalArgumentException("Tried to build a HyperEdge from a null auxiliary Node");
         }
         if (!isAuxiliaryNode(auxiliaryNode)) {
             throw new IllegalArgumentException("ID " + auxiliaryNode.getId() + " refers not to a HyperEdge but to " +
                 "a HyperNode");
         }
         return new Neo4jHyperEdge(auxiliaryNode, neo4j, exactIndex, fulltextIndex);
     }
 
     // ---
 
     protected final boolean isAuxiliaryNode(Node node) {
         return (Boolean) node.getProperty(KEY_IS_HYPER_EDGE, false);
     }
 
     // ---
 
     protected final String getAttributesString(PropertyContainer container) {
         Map<String, Object> properties = getProperties(container);
         //
         StringBuilder builder = new StringBuilder("{");
         for (String key : properties.keySet()) {
             if (builder.length() > 1) {
                 builder.append(", ");
             }
             builder.append(key + "=" + properties.get(key));
         }
         builder.append("}");
         return builder.toString();
     }
 
     protected final Map<String, Object> getProperties(PropertyContainer container) {
         Map properties = new HashMap();
         for (String key : container.getPropertyKeys()) {
             properties.put(key, container.getProperty(key));
         }
         return properties;
     }
 
     protected final RelationshipType getRelationshipType(String typeName) {
         // search through dynamic types
         for (RelationshipType relType : neo4j.getRelationshipTypes()) {
             if (relType.name().equals(typeName)) {
                 return relType;
             }
         }
         // fallback: create new type
         logger.info("### Relation type \"" + typeName + "\" does not exist -- Creating it dynamically");
         return DynamicRelationshipType.withName(typeName);
     }
 
     // === Traversal ===
 
     protected final ConnectedHyperNode getConnectedHyperNode(Node node, String myRoleType, String othersRoleType) {
         Set<ConnectedHyperNode> nodes = getConnectedHyperNodes(node, myRoleType, othersRoleType);
         switch (nodes.size()) {
         case 0:
             return null;
         case 1:
             return nodes.iterator().next();
         default:
             throw new RuntimeException("Ambiguity: there are " + nodes.size() + " connected nodes (" + node +
                 ", myRoleType=\"" + myRoleType + "\", othersRoleType=\"" + othersRoleType + "\")");
         }
     }
 
     protected final Set<ConnectedHyperNode> getConnectedHyperNodes(Node node, String myRoleType,
                                                                               String othersRoleType) {
         return new TraveralResultBuilder(node, myRoleType, othersRoleType) {
             @Override
             Object buildResult(Node connectedNode, Node auxiliaryNode) {
                 return new ConnectedHyperNode(buildHyperNode(connectedNode), buildHyperEdge(auxiliaryNode));
             }
         }.getResult();
     }
 
     // ---
 
     protected final ConnectedHyperEdge getConnectedHyperEdge(Node node, String myRoleType, String othersRoleType) {
         Set<ConnectedHyperEdge> edges = getConnectedHyperEdges(node, myRoleType, othersRoleType);
         switch (edges.size()) {
         case 0:
             return null;
         case 1:
             return edges.iterator().next();
         default:
             throw new RuntimeException("Ambiguity: there are " + edges.size() + " connected edges (" + node +
                 ", myRoleType=\"" + myRoleType + "\", othersRoleType=\"" + othersRoleType + "\")");
         }
     }
 
     protected final Set<ConnectedHyperEdge> getConnectedHyperEdges(Node node, String myRoleType,
                                                                               String othersRoleType) {
         return new TraveralResultBuilder(node, myRoleType, othersRoleType) {
             @Override
             Object buildResult(Node connectedNode, Node auxiliaryNode) {
                 return new ConnectedHyperEdge(buildHyperEdge(connectedNode), buildHyperEdge(auxiliaryNode));
             }
         }.getResult();
     }
 
     // ---
 
     protected abstract class TraveralResultBuilder {
 
         private Set result = new HashSet();
 
         protected TraveralResultBuilder(Node node, String myRoleType, String othersRoleType) {
             TraversalDescription desc = Traversal.description();
             if (myRoleType != null && othersRoleType != null) {
                 desc = desc.evaluator(new RoleTypeEvaluator(myRoleType, othersRoleType));
                 desc = desc.relationships(getRelationshipType(myRoleType), Direction.INCOMING);
                 desc = desc.relationships(getRelationshipType(othersRoleType), Direction.OUTGOING);
             } else if (myRoleType == null && othersRoleType == null) {
                 desc = desc.evaluator(new AuxiliaryEvaluator());
             } else {
                 throw new IllegalArgumentException("Both or none role types must be set");
             }
             //
             for (Path path : desc.traverse(node)) {
                 // sanity check
                 if (path.length() != 2) {
                     throw new RuntimeException("jri doesn't understand Neo4j traversal");
                 }
                 //
                 Node connectedNode = path.endNode();
                 Node auxiliaryNode = path.lastRelationship().getStartNode();
                 result.add(buildResult(connectedNode, auxiliaryNode));
             }
         }
 
         abstract Object buildResult(Node connectedNode, Node auxiliaryNode);
 
         Set getResult() {
             return result;
         }
     }
 
     private class RoleTypeEvaluator implements Evaluator {
 
         private RelationshipType myRoleType;
         private RelationshipType othersRoleType;
 
         private RoleTypeEvaluator(String myRoleType, String othersRoleType) {
             this.myRoleType = getRelationshipType(myRoleType);
             this.othersRoleType = getRelationshipType(othersRoleType);
         }
 
         @Override
         public Evaluation evaluate(Path path) {
             boolean includes = true;
             boolean continues = true;
             Relationship rel = path.lastRelationship();
             Node node = path.endNode();
             if (path.length() == 1) {
                 if (!rel.isType(myRoleType) || rel.getStartNode().getId() != node.getId()) {
                     continues = false;
                 }
             } else if (path.length() == 2) {
                 if (!rel.isType(othersRoleType) || rel.getEndNode().getId() != node.getId()) {
                     includes = false;
                 }
             }
             //
             includes = includes && path.length() == 2;
             continues = continues && path.length() < 2;
             return Evaluation.of(includes, continues);
         }
     }
 
     private class AuxiliaryEvaluator implements Evaluator {
 
         @Override
         public Evaluation evaluate(Path path) {
             Node node = path.endNode();
             // sanity check
             if (path.length() == 1) {
                 if (!isAuxiliaryNode(node)) {
                     throw new RuntimeException("jri doesn't understand Neo4j traversal or your graph is corrupted");
                 }
             }
             //
            boolean includes = path.length() == 2 && isAuxiliaryNode(node);
             boolean continues = path.length() < 2;
             return Evaluation.of(includes, continues);
         }
     }
 }
