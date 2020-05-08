 package app;
 
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.helpers.collection.IterableWrapper;
 import org.neo4j.graphdb.Path;
 
 public class Email extends Entity {
 	public static final String EMAIL_INDEX = "email";
 	public static final String EMAIL_KEY = "address";
 	public static final String CLAIM_TOKEN = "claimtoken";
 
 	private static UniqueNodeFactory factory = new UniqueNodeFactory( EMAIL_KEY, EMAIL_INDEX );
 
 	public Email( final String address ) {
 		if( !Email.isValidAddress( address ) ) {
 			throw new IllegalArgumentException();
 		}
 		try(Transaction tx = graphDb().beginTx()) {
 			initialize( factory.getOrCreate( address ) );
 			// New email, not associated with a User yet so add Claim Token
 			if( !internalNode.hasLabel(LabelDef.EMAIL) ) {
 				internalNode.addLabel(LabelDef.EMAIL);
 				internalNode.setProperty(CLAIM_TOKEN, new ClaimToken().toString());
 			}
 			tx.success();
 		}
 	}
 
 	public Email( final Node internalNode ) {
 		super.initialize(internalNode);
 	}
 
 	public String getAddress() {
 		try( Transaction tx = graphDb().beginTx() ) {
 			String address = (String) internalNode.getProperty( EMAIL_KEY );
 			tx.success();
 			return address;
 		}
 	}
 
 	public ClaimToken getClaimToken() {
 		String signature;
 		try( Transaction tx = graphDb().beginTx() ) {
 			signature = (String) internalNode.getProperty( CLAIM_TOKEN, null );
 			tx.success();
 		}
 		if( signature == null ) return null;
 		return new ClaimToken( signature );
 	}
 
 	public User getUser() {
 		try( Transaction tx = graphDb().beginTx() ) {
 			Relationship rel = internalNode.getSingleRelationship(RelType.OWNS, Direction.BOTH);
			if( rel == null )
				return null;
 			Node node = rel.getOtherNode( internalNode );
 			tx.success();
 			return new User(node);
 		}
 	}
 	public static boolean isValidAddress(String address) {
 		//TODO: more complicated regex
 		// If empty Address or only an '@', not valid
 		if( address == null || address.length() <= 1 ) return false;
 
 
 		// If doesn't contain '@' sign or the '@' is at the front or at the end, not valid.
 		if( address.indexOf('@') <= 0 || address.lastIndexOf('@') == (address.length() - 1) ) return false;
 		return true;
 	}
 
 	public static class PathIterableWrapper extends IterableWrapper<Email, Path> {
 		public PathIterableWrapper(Iterable<Path> iterable) {
 			super(iterable);
 		}
 
 		@Override
 		protected Email underlyingObjectToObject( Path path ) {
 			return new Email( path.endNode() );
 		}
 	}
 }
