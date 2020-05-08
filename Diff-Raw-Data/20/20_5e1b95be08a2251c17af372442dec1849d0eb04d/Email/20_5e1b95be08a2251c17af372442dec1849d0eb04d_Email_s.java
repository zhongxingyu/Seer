 package app;
 
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.helpers.collection.IterableWrapper;
 import org.neo4j.graphdb.Path;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Email extends Entity {
 	public static final String EMAIL_INDEX = "email";
 	public static final String EMAIL_KEY = "address";
 	public static final String CLAIM_TOKEN = "claimtoken";
 
 	private static UniqueNodeFactory factory = new UniqueNodeFactory( EMAIL_KEY, EMAIL_INDEX );
 
 	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
 	    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
 
 	private static Pattern pattern = Pattern.compile(EMAIL_PATTERN);
 
 	public Email( final String address ) {
 		if( !Email.isValidAddress( address ) ) {
 			throw new IllegalArgumentException();
 		}
 		try(Transaction tx = graphDb().beginTx()) {
 			initialize( factory.getOrCreate( address ) );
 			// New email, not associated with a User yet so add Claim Token
 			if( !internalNode.hasLabel(LabelDef.EMAIL) ) {
 				internalNode.addLabel(LabelDef.EMAIL);
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
 		if( getUser() != null ) return null;
 		try( Transaction tx = graphDb().beginTx() ) {
 			String signature;
 
 			if( !internalNode.hasProperty(CLAIM_TOKEN) ) {
 				internalNode.setProperty(CLAIM_TOKEN,
 						new ClaimToken().toString());
 			}
 			signature = (String) internalNode.getProperty( CLAIM_TOKEN );
 			tx.success();
 
 			return new ClaimToken( signature );
 		}
 	}
 
 	public void clearClaimToken() {
 		try( Transaction tx = graphDb().beginTx() ) {
 			internalNode.removeProperty( CLAIM_TOKEN );
 			tx.success();
 		}
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
 		// If empty Address or only an '@', not valid
 		if( address == null || address.length() <= 1 ) return false;
 
 
 		// Match against email regex to determine validity
 		Matcher matcher = pattern.matcher(address);
		
 		return matcher.matches();
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
