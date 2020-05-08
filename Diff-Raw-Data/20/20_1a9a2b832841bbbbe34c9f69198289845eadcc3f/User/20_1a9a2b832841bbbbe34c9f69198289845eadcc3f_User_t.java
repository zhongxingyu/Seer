 package app;
 
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.traversal.*;
 import org.neo4j.kernel.Traversal;
 
 public class User extends Entity {
	public static final String USER_INDEX = "User";
 	public static final String USERNAME = "Username";
 	public static final String PASSWORD = "Password";
 
	private static UniqueNodeFactory factory = new UniqueNodeFactory( USERNAME, USER_INDEX );

 	public User(Node internalNode) {
 		this.internalNode = internalNode;
 	}
 
 	public User(String name) {
 		GraphDatabaseService graphDb = GraphDatabase.get();
 		try( Transaction tx = graphDb.beginTx() ) {
			this.internalNode = factory.getOrCreate(name);
 			this.internalNode.addLabel(LabelDef.USER);
 			tx.success();
 		}
 	}
 
 	public void setPassword(String str) {
 		GraphDatabaseService graphDb = GraphDatabase.get();
 		try( Transaction tx = graphDb.beginTx() ) {
 			getInternalNode().setProperty( PASSWORD, str );
 			tx.success();
 		}
 	}
 
 	public String getPassword() {
 		GraphDatabaseService graphDb = GraphDatabase.get();
 		try( Transaction tx = graphDb.beginTx() ) {
			String password = (String) getInternalNode().getProperty( USERNAME, null );
 			tx.success();
 			return password;
 		}
 	}
 
 	public void addToPortfolio(Citation citation) {
 		GraphDatabaseService graphDb = GraphDatabase.get();
 		try( Transaction tx = graphDb.beginTx() ) {
 			this.createRelationshipTo(citation, RelType.PORTFOLIO);
 			tx.success();
 		}
 	}
 
 	public void removeFromPortfolio(Citation citation) {
 		GraphDatabaseService graphDb = GraphDatabase.get();
 		try( Transaction tx = graphDb.beginTx() ) {
 			Relationship rel = this.createRelationshipTo(citation, RelType.PORTFOLIO);
 			rel.delete();
 			tx.success();
 		}
 	}
 
 	public Iterable<Citation> viewPortfolio() {
 		GraphDatabaseService graphDb = GraphDatabase.get();
 		try( Transaction tx = graphDb.beginTx() ) {
 			TraversalDescription traversal = Traversal.description()
 				.depthFirst()
 				.evaluator(Evaluators.toDepth(1))
 				.relationships( RelType.PORTFOLIO );
 
 			Traverser paths = traversal.traverse();
 			tx.success();
 			return new Citation.PathIterableWrapper(paths);
 		}
 	}
 
 	public void addEmail(Email email) {
 		GraphDatabaseService graphDb = GraphDatabase.get();
 		try( Transaction tx = graphDb.beginTx() ) {
 			this.createRelationshipTo(email, RelType.OWNS);
 			tx.success();
 		}
 	}
 
 	public void removeEmail(Email email) {
 		GraphDatabaseService graphDb = GraphDatabase.get();
 		try( Transaction tx = graphDb.beginTx() ) {
 			Relationship rel = this.createRelationshipTo(email, RelType.OWNS);
 			rel.delete();
 			tx.success();
 		}
 	}
 
 	public Iterable<Email> viewEmails() {
 		GraphDatabaseService graphDb = GraphDatabase.get();
 		try( Transaction tx = graphDb.beginTx() ) {
 			TraversalDescription traversal = Traversal.description()
 				.depthFirst()
 				.evaluator(Evaluators.toDepth(1))
 				.relationships( RelType.OWNS );
 
 			Traverser paths = traversal.traverse();
 			tx.success();
 			return new Email.PathIterableWrapper(paths);
 		}
 	}
 
 	public static boolean isValidUsername(String username) {
 		if( username == null ) return false;
 		return true;
 	}
 
 	public static boolean isValidPassword(String password) {
 		if( password == null ) return false;
 		return true;
 	}
 }
