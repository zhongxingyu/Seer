 package app;
 
 import org.neo4j.graphdb.*;
 import org.neo4j.graphdb.GraphDatabaseService;
 import asg.cliche.Command;
 import asg.cliche.ShellFactory;
 import asg.cliche.InputConverter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Random;
 import java.util.Iterator;
 import java.util.Queue;
 import java.util.LinkedList;
 import org.neo4j.kernel.Traversal;
 import org.neo4j.graphdb.traversal.*;
 import org.neo4j.graphdb.*;
 
 public class Cli {
 
 	private HashMap<String, Session> session_table = new HashMap<String, Session>();
 
 	private class Result {
 		boolean success = false;
 		Session session = null;
 		Email email = null;
 		String payload = "";
 
 		public Result( boolean ans, String reason ) {
 			success = ans;
 			payload = reason;
 		}
 
 		public Result( boolean ans, Session s ) {
 			success = ans;
 			session = s;
 		}
 
 		public Result( boolean ans, Email e ) {
 			success = ans;
 			email = e;
 		}
 
 		public String toString() {
 			if( success )
 				return "success:" + payload;
 			else
 				return "failure:" + payload;
 		}
 	}
 
 	private Result validateSession( String session_id ) {
 		if( !session_table.containsKey( session_id ) )
 			return new Result(false, "Invalid Session");
 
 		Session s = session_table.get(session_id);
 		if( !s.isValid() ) {
 			session_table.remove(session_id);
 			return new Result(false, "Expired Session");
 		}
 
 		return new Result(true, s);
 	}
 
 	private Result validateEmail( String address, boolean mustExist ) {
 		Email email = null;
 
 		if( !Email.isValidAddress(address) ) {
 			return new Result(false, "Invalid email");
 		}
 
 		try(Transaction tx = GraphDatabase.get().beginTx()) {
 			if( mustExist ) {
 				//check email is valid
 				Node n = Entity.findExistingNode(LabelDef.EMAIL, Email.EMAIL_KEY, address);
 
 				if( n == null ) {
 					tx.failure();
 					return new Result(false, "Invalid email");
 				}
 				email = new Email( n );
 			} else {
 				try {
 					email = new Email(address);
 				} catch(IllegalArgumentException e) {
 					tx.failure();
 					return new Result(false, "Invalid email");
 				}
 			}
 
 			tx.success();
 			return new Result(true, email);
 		}
 	}
 
 	@Command
 	public Result login( String email, String pass ) {
 		Session session = Session.createFromLogin(email, pass);
 		if( session == null )
 			return new Result(false, "Invalid Login");
 
 		boolean did = false;
 		String session_id = "";
 		for( int i=0; i < 20; i++ ) {
 			session_id = randomString(4);
 			if( session_table.containsKey( session_id ) ) {
 				continue;
 			}
 
 			session_table.put( session_id, session );
 			did = true;
 		}
 
 		if( !did )
 			return new Result(false, "Couldn't generate unique Session.id");
 
 		return new Result(true, "Session:"+session_id);
 	}
 
 	@Command
 	public Result logout( String session_id ) {
 		Result res = validateSession( session_id );
 		if( !res.success )
 			return res;
 
 		session_table.remove(session_id);
 
 		return res;
 	}
 
 	@Command
 	public Result signup( String address ) {
 		Result res = validateEmail(address, false);
 		if( !res.success ) {
 			return res;
 		}
 
 		//check if email has been registered
 		Email email = res.email;
 		if(email.getClaimToken() != null) {
 			return (new Result(true, "ClaimToken:" + email.getClaimToken().toString()));
 		}
 
 		return (new Result(false, "Email claimed"));
 	}
 
 	@Command
 	public Result register( String ct, String pass, String passVer ) {
 		try(Transaction tx = GraphDatabase.get().beginTx()) {
 			//check if passwords match
 			if(!pass.equals(passVer))
 				return (new Result(false, "passwords do not match"));
 
 			Email e = null;
 			try {
 				//find email by claimtoken
 				e = new Email(Entity.findExistingNode(LabelDef.EMAIL, Email.CLAIM_TOKEN, ct));
 			}catch(IllegalStateException ise) {
 				//no email found, bad claimtoken
 				return (new Result(false, "bad claimtoken"));
 			}
 
 			//create or find user and set password and email
 			User u = new User();
 			u.setPassword(pass);
 			u.addEmail(e);
 
 			//clear claimToken(should do this on its own!!)
 			e.clearClaimToken();
 
 			tx.success();
 			return (new Result(true, "registration complete"));
 		}
 	}
 
 	@Command
 	public Result addEmail( String session_id, String address, String ct) {
 		Result res = validateSession( session_id );
 		if( !res.success )
 			return res;
 		Session session = res.session;
 
 		res = validateEmail(address, false);
 		if( !res.success ) {
 			return new Result(false, "Invalid email, no ClaimToken associated");
 		}
 		Email email = res.email;
 
 		if( !(email.getClaimToken().toString().equals(ct)) ) {
 			return new Result(false, "Invalid email Claim Token");
 		}
 
 		session.user.addEmail(email);
 
 		return new Result(true, "Email Added");
 	}
 
 	@Command
 	public Result deleteEmail( String session_id, String address ) {
 		Result res = validateSession( session_id );
 		if( !res.success ) {
 			return res;
 		}
 		Session session = res.session;
 
 		//email must exist as claimtoken should have been added
 		res = validateEmail(address, true);
 		if( !res.success ) {
 			return res;
 		}
 		Email email = res.email;
 
 		try {
 			session.user.removeEmail(email);
 		} catch(IllegalStateException e) {
 			return new Result(false, "Invalid email to remove");
 		}
 
 		return new Result(true, "Removed email");
 	}
 
 	@Command
 	public Result addToPortfolio( String session_id, String description, String resource ) {
 		Result res = validateSession( session_id );
 		if( !res.success )
 			return res;
 		Session session = res.session;
 
 		Citation c = new Citation(description, resource);
 
 		session.user.addToPortfolio(c);
 
 		return new Result(true, "Citation Added");
 	}
 
 	@Command
 	public Result removeFromPortfolio( String session_id, String cit ) {
 		try(Transaction tx = GraphDatabase.get().beginTx()) {
 			Result res = validateSession( session_id );
 			if( !res.success ){
 				return res;
 			}
 
 			try {
 				Citation c = new Citation(new Token(cit));
 				if (c == null) {
 					return new Result(false, "Invalid session");
 				}
 				c.delete();
 				tx.success();
 				return new Result(true, "Citation removed");
 			}
 
 			catch(IllegalArgumentException e ) {
 				return new Result(false, "Bad Citation ID");
 			}
 		}
 	}
 
 	@Command
 	public Result viewPortfolio( String address ) {
 		//find Email for Email. call email.getUser() to get User.
 		//then call user.viewPortfolio which returns an iterator
 		//over the citations, all of which i want to print
 
 		//email must exist as claimtoken should have been added
 		try(Transaction tx = GraphDatabase.get().beginTx()) {
 			Result res = validateEmail(address, true);
 			if( !res.success ) {
 				return res;
 			}
 
 			Email email = res.email;
 
 			if (email.getUser() == null) {
 				//not found
 				return new Result(false, "Invalid email, no profile associated");
 			}
 			User user = email.getUser();
 			Iterator<Citation> iterator = user.viewPortfolio().iterator();
 			StringBuilder output = new StringBuilder();
 			while (iterator.hasNext()) {
 				Citation c = iterator.next();
 				output.append(c.toString());
 			}
 
 
 			tx.success();
 			return new Result(true , output.toString());
 		}
 	}
 
 	@Command					//shouldn't this have an entity arg?
 	public Result trust( String session_id, String address, String subject, String citation_desc, String citation_resource ) {
 		Result res = validateSession( session_id );
 		if( !res.success ){
 			return res;
 		}
 		Session session = res.session;
 
 		res = validateEmail( address, false );
 		if( !res.success ){
 			return res;
 		}
 
 		try(Transaction tx=GraphDatabase.get().beginTx()){
 			Email email = res.email;
			User u = email.getUser();
 			Citation c;
 			Subject s;
			TrustEdge te, te2user = null;
 
 			try {
 				c = new Citation(citation_desc, citation_resource);
 			} catch( IllegalArgumentException e ) {
 				return new Result(false, "Invalid citation strings");
 			}
 
 			try {
 				s = new Subject(subject);
 			} catch( IllegalArgumentException e ) {
 				return new Result(false, "Invalid subject");
 			}
 
 			try {
 				te = new TrustEdge(session.user, email, s );
				if( u != null ) {
					te2user = new TrustEdge(session.user, u, s );
				}
 			} catch( IllegalArgumentException e ) {
 				return new Result(false, "Internal error");
 			}
 
 			te.addCitation( c );
			if( te2user != null )
				te2user.addCitation( c );
 
 			tx.success();
 			return new Result(true, "" + te );
 		}
 	}
 
 	@Command
 	public Result untrust( String session_id, String trustEdge ) {
 		Result res = validateSession( session_id );
 		if( !res.success ){
 			return res;
 		}
 
 		try(Transaction tx=GraphDatabase.get().beginTx()){
 			//something involved with removing a TrustEdge
 
 			TrustEdge te;
 			try {
 				te = new TrustEdge( Entity.nodeByID( new Token( trustEdge ) ) );
 			} catch( Exception e ) {
 				return new Result(false, "Invalid TrustEdge");
 			}
 
 			te.delete();
 			tx.success();
 		}
 
 		return new Result(true,"");
 	}
 
 	/*
 	 * prints all users who are trusted by the logged in user
 	 * in the subject passed to the function
 	 */
 
 	@Command
 	public Result viewSubjectiveNetwork( String session_id, String subject, int threshold ) {
 		Result res = validateSession( session_id );
 		if( !res.success ){
 			return res;
 		}
 		try(Transaction tx=GraphDatabase.get().beginTx()){
 			Session s=session_table.get(session_id);
 			User me=s.user;
 			Node start=me.getInternalNode();
 			LinkedList q=new LinkedList();
 			LinkedList mark=new LinkedList();
 			int depth=0;
 
 			//BFS
 			q.addFirst(start);
 			for(Email e:new User(start).viewEmails()){
 				mark.add(e.getAddress());
 				break;
 			}
 			while(!q.isEmpty()){
 				Node temp;
 				temp=(Node)q.removeLast();
 				if(depth==threshold){
 					return new Result(true,"");
 				}
 				depth++;
 				// r is relationship from User to TE
 				for(Relationship r: temp.getRelationships(RelType.FROM)){
 					//r2 is relationship from TE to next User
 					for(Relationship r2:r.getEndNode().getRelationships(RelType.TO)){
 						//accessing Email for identification to print
 						for(Email e:new User(r2.getEndNode()).viewEmails()){
 							//if email has hasnt been added and the subject is correct
 							if(!mark.contains(e.getAddress()) && r2.getStartNode().getProperty("subject").equals(subject)){
 								mark.add(e.getAddress());
 								q.addFirst(r2.getEndNode());
 								for(Email e1:new User(r2.getEndNode()).viewEmails()){
 									System.out.println(e1.getAddress());
 									break;
 								}
 							}
 						}
 						break;
 					}
 				}
 			}
 		}
 		return new Result(true,"");
 	}
 
 	/*
 	 * prints all user's primary emails that
 	 * the user associated with the argument email
 	 * has trusted on any subject
 	 */
 
 	@Command
 	public Result viewTrustNetwork( String address ) {
 		try(Transaction tx=GraphDatabase.get().beginTx()){
 			Email e2=new Email(address);
 			User me=e2.getUser();
 			if(me==null){
 				return new Result(false,"Email is not registered");
 			}
 			Node start=me.getInternalNode();
 			LinkedList q=new LinkedList();
 			LinkedList mark=new LinkedList();
 
 			//BFS
 			q.addFirst(start);
 			for(Email e3:new User(start).viewEmails()){
 				mark.add(e3.getAddress());
 				break;
 			}
 			while(!q.isEmpty()){
 				Node temp;
 				temp=(Node)q.removeLast();
 
 				// r is relationship from User to TE
 				for(Relationship r: temp.getRelationships(RelType.FROM)){
 					for(Relationship r2:r.getEndNode().getRelationships(RelType.TO)){
 						if(r2.getStartNode().hasProperty("Guid")){System.out.println("TrustEdge:"+r2.getStartNode().getProperty("Guid"));}
 						System.out.println("subject:" + r2.getStartNode().getProperty("subject"));
 					}
 					//r2 is relationship from TE to next User
 					for(Relationship r2:r.getEndNode().getRelationships(RelType.TO)){
 						//accessing Email for identification to print
 						for(Email e:new User(r2.getEndNode()).viewEmails()){
 							//if email has hasnt been added
 							if(e==null){continue;}
 							if(!mark.contains(e.getAddress())){
 								mark.add(e.getAddress());
 								q.addFirst(r2.getEndNode());
 								for(Email e1:new User(r2.getEndNode()).viewEmails()){
 									if(e1.getAddress()!=null){System.out.println(e1.getAddress());}
 									break;
 								}
 							}
 						}
 						break;
 					}
 				}
 			}
 		}
 		return new Result(true,"");
 	}
 
 	public static void main(String[] args) throws IOException {
 		ShellFactory.createConsoleShell("app-sh", "", new Cli()).commandLoop();
 	}
 
 	static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
 	static Random rnd = new Random();
 
 	private String randomString( int len ) {
 		StringBuilder sb = new StringBuilder( len );
 		for( int i = 0; i < len; i++ )
 			sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
 		return sb.toString();
 
 	}
 }
