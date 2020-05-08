 package app;
 
 import org.neo4j.graphdb.Transaction;
 import java.util.Date;
 import java.util.Calendar;
 
 public class Session {
 	// Reference account
 	public final User user;
 	public final Date validUntil;
 
 	public static Session createFromLogin( String email, String password ) {
		if( email == null || password == null || email.length() == 0 || password.length() == 0 )
			return null;

 		Calendar cal = Calendar.getInstance();
 		cal.setTime(new Date());
 		cal.add(Calendar.HOUR_OF_DAY, 1);
 
 		Session s;
 
 		try(Transaction tx = GraphDatabase.get().beginTx()) {
 			Email e = new Email(email);
 			User u = e.getUser();
 			if( u == null )
 				return null;
 
 			if( !password.equals(u.getPassword())) {
 				return null;
 			}
 
 			s = new Session( u, cal.getTime());
 			tx.success();
 		}
 
 		return s;
 	}
 
 	public Session(User user, Date validUntil) {
 		if(user == null || validUntil == null) {
 			throw new IllegalArgumentException();
 		}
 		this.user = user;
 		this.validUntil = validUntil;
 	}
 
 	public boolean isValid() {
 		Date now = new Date();
 
 		//test if validUntil is equal to now or now is < 0 (before validUntil)
 		return (now.compareTo(validUntil) <= 0);
 	}
 }
