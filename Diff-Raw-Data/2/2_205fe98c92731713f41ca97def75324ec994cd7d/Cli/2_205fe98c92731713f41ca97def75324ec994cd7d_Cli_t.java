 package app;
 
 import asg.cliche.Command;
 import asg.cliche.ShellFactory;
 import asg.cliche.InputConverter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Random;
 
 public class Cli {
 
 	private HashMap<String, Session> session_table = new HashMap<String, Session>();
 
 	private class Result {
 		boolean success = false;
 		Session session = null;
 		String payload = "";
 
 		public Result( boolean ans, Session s ) {
 			success = ans;
 			session = s;
 		}
 
 		public Result( boolean ans, String reason ) {
 			success = ans;
 			payload = reason;
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
 	public Result signup( String email ) {
 		return null;
 	}
 
 	@Command
 	public Result register( String ct, String name, String pass, String passVer ) {
 		return null;
 	}
 
 	@Command
 	public RecoveryToken recoverPassword( String email ) {
 		return null;
 	}
 
 	@Command
 	public Result registerEmail( String session_id, String address) {
 		Result res = validateSession( session_id );
 		if( !res.success )
 			return res;
 
 		Email email = new Email(address);
 
 		return new Result(true, "Email Claim Token - " + email.getClaimToken());
 	}
 
 	@Command
 	public Result addEmail( String session_id, String address, String ct) {
 		Result res = validateSession( session_id );
 		if( !res.success )
 			return res;
 
 		Email email = new Email(address);
 
 		if( !(email.getClaimToken().equals(ct)) )
 			return new Result(false, "Invalid Email Claim Token");
 
 		Session session = res.session;
 		session.user.addEmail(email);
 
 		return new Result(true, "Email Added");
 	}
 
 	@Command
 	public Result deleteEmail( String session_id, String email ) {
 		Result res = validateSession( session_id );
 		if( !res.success )
 			return res;
 
 		Session session = res.session;
		session.user.removeEmail(new Email(email));
 
 		return new Result(true, "Email Removed");
 	}
 
 	@Command
 	public Result addToPorfolio( String session_id, String cit ) {
 		Result res = validateSession( session_id );
 		if( !res.success )
 			return res;
 
 		return null;
 	}
 
 	@Command
 	public Result removeFromPorfolio( String session_id, String cit ) {
 		Result res = validateSession( session_id );
 		if( !res.success )
 			return res;
 
 		return null;
 	}
 
 	@Command
 	public Result viewPortfolio( String email ) {
 		return null;
 	}
 
 	@Command
 	public Result trust( String session_id, String subject, String... citations ) {
 		Result res = validateSession( session_id );
 		if( !res.success )
 			return res;
 
 		return null;
 	}
 
 	@Command
 	public Result untrust( String session_id, String trustEdge ) {
 		Result res = validateSession( session_id );
 		if( !res.success )
 			return res;
 
 		return null;
 	}
 
 	@Command
 	public Result viewSubjectiveNetwork( String session_id, String subject, int threshold ) {
 		Result res = validateSession( session_id );
 		if( !res.success )
 			return res;
 
 		return null;
 	}
 
 	@Command
 	public Result viewTrustNetwork( String email ) {
 		return null;
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
