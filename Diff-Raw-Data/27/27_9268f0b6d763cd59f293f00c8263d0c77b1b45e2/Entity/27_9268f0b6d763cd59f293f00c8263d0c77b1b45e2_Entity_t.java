 package app;
 
 import org.neo4j.graphdb.*;
 import org.neo4j.graphdb.index.*;
 
 public abstract class Entity {
 	protected Node internalNode;
 
 	private static final String GUID = "Guid";
 
 	protected void initialize(Node internalNode) {
 		try(Transaction tx = graphDb().beginTx()) {
 			this.internalNode = internalNode;
			if( !this.internalNode.hasLabel(LabelDef.ENTITY) ) {
				this.internalNode.addLabel(LabelDef.ENTITY);
				this.internalNode.setProperty(GUID, Token.randomToken().toString());
			}
 			tx.success();
 		}
 	}
 
 	public Relationship createRelationshipTo(Entity other, RelType type) {
 		return RelationshipFactory.get().getOrCreateRelationship(this.internalNode, other.internalNode, type);
 	}
 
 	public static GraphDatabaseService graphDb() {
 		return GraphDatabase.get();
 	}
 
 	public static <T> T getSingle(ResourceIterable<T> iterable) {
 		try (ResourceIterator<T> iterator = iterable.iterator()) {
 			if( !iterator.hasNext() ) throw new IllegalStateException();
 			return iterator.next();
 		}
 	}
 
 	public Node getInternalNode() {
 		return internalNode;
 	}
 
 	public Token getId() {
 		try(Transaction tx = graphDb().beginTx()) {
 			String guid = (String)internalNode.getProperty(GUID);
 			tx.success();
 			return new Token(guid);
 		}
 	}
 
 	public static Node findExistingNode(Label label, String key, Object value) {
 		try(Transaction tx = graphDb().beginTx()) {
 			ResourceIterable<Node> nodes = graphDb().findNodesByLabelAndProperty( label, key, value );
 			Node node = getSingle( nodes );
 			tx.success();
 			return node;
 		}
 	}
 
 	protected static Node nodeByID(Token token) {
 		return findExistingNode(LabelDef.ENTITY, GUID, token.toString());
 	}
 }
