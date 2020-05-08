 package de.deepamehta.mehtagraph.impl;
 
 import de.deepamehta.mehtagraph.MehtaObject;
 
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Path;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.RelationshipType;
 import org.neo4j.graphdb.traversal.Evaluation;
 import org.neo4j.graphdb.traversal.Evaluator;
 import org.neo4j.graphdb.traversal.TraversalDescription;
 // FIXME: new index API of Neo4j 1.3 and 1.4 doesn't work with OSGi
 // import org.neo4j.graphdb.index.Index;
 // Using old index API instead
 import org.neo4j.index.IndexService;
 import org.neo4j.index.lucene.LuceneFulltextQueryIndexService;
 //
 import org.neo4j.kernel.Traversal;
 import org.neo4j.kernel.Uniqueness;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 
 
 class Neo4jBase {
 
     // ------------------------------------------------------------------------------------------------------- Constants
 
     protected static final String KEY_IS_MEHTA_EDGE = "_is_mehta_edge";
     protected static final String KEY_FULLTEXT = "_fulltext";
 
     // ---------------------------------------------------------------------------------------------- Instance Variables
 
     private final Logger logger = Logger.getLogger(getClass().getName());
 
     protected final GraphDatabaseService neo4j;
     protected Neo4jRelationtypeCache relTypeCache;
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
 
     protected Neo4jBase(Neo4jBase base) {
         this.neo4j = base.neo4j;
         this.relTypeCache = base.relTypeCache;
         this.exactIndex = base.exactIndex;
         this.fulltextIndex = base.fulltextIndex;
     }
 
     // ----------------------------------------------------------------------------------------------- Protected Methods
 
     protected final Neo4jMehtaNode buildMehtaNode(Node node) {
         if (node == null) {
             throw new IllegalArgumentException("Tried to build a MehtaNode from a null Node");
         }
         if (isAuxiliaryNode(node)) {
             throw new IllegalArgumentException("ID " + node.getId() + " refers not to a MehtaNode but to a MehtaEdge");
         }
         return new Neo4jMehtaNode(node, this);
     }
 
     protected final Neo4jMehtaEdge buildMehtaEdge(Node auxiliaryNode) {
         if (auxiliaryNode == null) {
             throw new IllegalArgumentException("Tried to build a MehtaEdge from a null auxiliary Node");
         }
         if (!isAuxiliaryNode(auxiliaryNode)) {
             throw new IllegalArgumentException("ID " + auxiliaryNode.getId() + " refers not to a MehtaEdge but to " +
                 "a MehtaNode");
         }
         return new Neo4jMehtaEdge(auxiliaryNode, this);
     }
 
     protected final MehtaObject buildMehtaObject(Node node) {
         return isAuxiliaryNode(node) ? buildMehtaEdge(node) : buildMehtaNode(node);
     }
 
     // ---
 
     protected final RelationshipType getRelationshipType(String typeName) {
         return relTypeCache.get(typeName);
     }
 
     protected final boolean isAuxiliaryNode(Node node) {
         return (Boolean) node.getProperty(KEY_IS_MEHTA_EDGE, false);
     }
 
 
 
     // === Traversal ===
 
     /**
      * The created traversal description allows to find mehta nodes that
      * are connected to the start node/edge via the given role types.
      * <p>
      * Called from {@link Neo4jMehtaObject#getConnectedMehtaNodes}.
      *
      * @param   myRoleType      Pass <code>null</code> to switch role type filter off.
      * @param   othersRoleType  Pass <code>null</code> to switch role type filter off.
      */
     protected final TraversalDescription traverseToMehtaNodes(String myRoleType, String othersRoleType) {
         return createTraversalDescription(new RoleTypeEvaluator(myRoleType, othersRoleType),
                                           new AuxiliaryEvaluator(false));
     }
 
     /**
      * The created traversal description allows to find mehta edges that
      * are connected to the start node/edge via the given role types.
      * <p>
      * Called from {@link Neo4jMehtaObject#getConnectedMehtaEdges}.
      *
      * @param   myRoleType      Pass <code>null</code> to switch role type filter off.
      * @param   othersRoleType  Pass <code>null</code> to switch role type filter off.
      */
     protected final TraversalDescription traverseToMehtaEdges(String myRoleType, String othersRoleType) {
         return createTraversalDescription(new RoleTypeEvaluator(myRoleType, othersRoleType),
                                           new AuxiliaryEvaluator(true));
     }
 
     /**
      * The created traversal description allows to find all mehta edges between
      * the the start node/edge and the given node.
      * <p>
      * Called from {@link Neo4jMehtaGraph#getMehtaEdges}
      */
     protected final TraversalDescription traverseToMehtaNode(long nodeId) {
         return createTraversalDescription(new ConnectedNodeEvaluator(nodeId),
                                           new AuxiliaryEvaluator(false));
     }
 
     // ---
 
     protected abstract class TraveralResultBuilder {
 
         private Set result = new HashSet();
 
         protected TraveralResultBuilder(Node node, TraversalDescription desc) {
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
 
     // ------------------------------------------------------------------------------------------------- Private Methods
 
 
 
     // === Traversal ===
 
     /**
      * Describes one mehta edge hop.
      */
     private TraversalDescription createTraversalDescription(Evaluator evaluator1, Evaluator evaluator2) {
         return Traversal.description()
             .evaluator(new DirectionEvaluator())
             .evaluator(evaluator1)
             .evaluator(evaluator2)
            .uniqueness(Uniqueness.RELATIONSHIP_GLOBAL);
        // Note: we need to traverse a node more than once. Consider this case: mehta node A
         // is connected with mehta node B via mehta edge C and A is connected to C as well.
         // (default uniqueness is not RELATIONSHIP_GLOBAL, but probably NODE_GLOBAL).
     }
 
     // ---
 
     private class DirectionEvaluator implements Evaluator {
 
         @Override
         public Evaluation evaluate(Path path) {
             boolean includes = false;
             boolean continues = false;
             //
             Relationship rel = path.lastRelationship();
             Node node = path.endNode();
             if (path.length() == 1) {
                 continues = rel.getStartNode().getId() == node.getId();
             } else if (path.length() == 2) {
                 includes = rel.getEndNode().getId() == node.getId();
             }
             //
             return Evaluation.of(includes, continues);
         }
     }
 
     private class RoleTypeEvaluator implements Evaluator {
 
         private RelationshipType myRoleType;
         private RelationshipType othersRoleType;
 
         private RoleTypeEvaluator(String myRoleType, String othersRoleType) {
             if (myRoleType != null) {
                 this.myRoleType = getRelationshipType(myRoleType);
             }
             if (othersRoleType != null) {
                 this.othersRoleType = getRelationshipType(othersRoleType);
             }
         }
 
         @Override
         public Evaluation evaluate(Path path) {
             boolean includes = true;
             boolean continues = true;
             //
             Relationship rel = path.lastRelationship();
             Node node = path.endNode();
             if (path.length() == 1) {
                 continues = myRoleType == null || rel.isType(myRoleType);
             } else if (path.length() == 2) {
                 includes = othersRoleType == null || rel.isType(othersRoleType);
             }
             //
             return Evaluation.of(includes, continues);
         }
     }
 
     private class ConnectedNodeEvaluator implements Evaluator {
 
         private long connectedNodeId;
 
         private ConnectedNodeEvaluator(long connectedNodeId) {
             this.connectedNodeId = connectedNodeId;
         }
 
         @Override
         public Evaluation evaluate(Path path) {
             boolean includes = path.length() == 2 && path.endNode().getId() == connectedNodeId;
             boolean continues = true;
             return Evaluation.of(includes, continues);
         }
     }
 
     private class AuxiliaryEvaluator implements Evaluator {
 
         private boolean isAuxiliary;
 
         private AuxiliaryEvaluator(boolean isAuxiliary) {
             this.isAuxiliary = isAuxiliary;
         }
 
         @Override
         public Evaluation evaluate(Path path) {
             boolean includes = path.length() == 2 && isAuxiliaryNode(path.endNode()) == isAuxiliary;
             boolean continues = true;
             return Evaluation.of(includes, continues);
         }
     }
 }
