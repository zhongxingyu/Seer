 package app;
 
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.helpers.collection.IterableWrapper;
 import org.neo4j.graphdb.traversal.*;
 import org.neo4j.kernel.Traversal;
 
 public class TrustEdge extends Entity {
 	private static final String SUBJECT = "subject";
 
 	public TrustEdge( final Node internalNode ) {
 		super.initialize(internalNode);
 	}
 
 	public TrustEdge( final User from, final User to, final Subject subject ) {
 		this(from, (Entity)to, subject);
 	}
 
 	public TrustEdge( final User from, final Email to, final Subject subject ) {
 		this(from, (Entity)to, subject);
 	}
 
 	public void addCitation(Citation reason) {
 		this.createRelationshipTo( reason, RelType.REASON );
 	}
 
 	public Iterable<Citation> reasons() {
 		GraphDatabaseService graphDb = GraphDatabase.get();
 		try( Transaction tx = graphDb.beginTx() ) {
 			TraversalDescription traversal = Traversal.description()
 				.depthFirst()
 				.evaluator(Evaluators.fromDepth(1))
 				.evaluator(Evaluators.toDepth(1))
 				.relationships( RelType.REASON );
 
 			Traverser paths = traversal.traverse(internalNode);
 			tx.success();
 			return new Citation.PathIterableWrapper(paths);
 		}
 	}
 
 	public User from() {
 		GraphDatabaseService graphDb = GraphDatabase.get();
 		try( Transaction tx = graphDb.beginTx() ) {
 			Relationship rel = internalNode.getSingleRelationship(RelType.FROM, Direction.BOTH);
 			Node userNode = rel.getOtherNode( internalNode );
 			tx.success();
 			return new User( userNode );
 		}
 	}
 
 	public Entity to() {
 		GraphDatabaseService graphDb = GraphDatabase.get();
 		try( Transaction tx = graphDb.beginTx() ) {
 			Relationship rel = internalNode.getSingleRelationship(RelType.TO, Direction.BOTH);
 			Node node = rel.getOtherNode( internalNode );
 			tx.success();
 
 			if( node.hasLabel( LabelDef.USER ) ) {
 				return new User( node );
 			}
 			else {
 				return new Email( node );
 			}
 		}
 	}
 
 	public Subject subject() {
 		GraphDatabaseService graphDb = GraphDatabase.get();
 		try( Transaction tx = graphDb.beginTx() ) {
 			String subj = (String) internalNode.getProperty(SUBJECT);
 			tx.success();
 			return new Subject(subj);
 		}
 	}
 
 	private TrustEdge( final Entity from, final Entity to, final Subject subject ) {
 		if( from == null
 				|| to == null
 				&& from.equals(to) )
 		{
 			throw new IllegalArgumentException();
 		}
 
 		GraphDatabaseService graphDb = graphDb();
 		try( Transaction tx = graphDb.beginTx() ) {
 
 			internalNode = graphDb.createNode( LabelDef.CITATION );
 
 			internalNode.setProperty(SUBJECT, subject.getName());
 
 			from.createRelationshipTo( this, RelType.FROM );
 			this.createRelationshipTo( to, RelType.TO );
 
 			tx.success();
 		}
 	}
 }
