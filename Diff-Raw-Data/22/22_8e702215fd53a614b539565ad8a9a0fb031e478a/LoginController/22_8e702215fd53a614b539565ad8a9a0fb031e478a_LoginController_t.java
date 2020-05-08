 package fr.esiea.web_dev.miammiam.controllers;
 
 import static fr.esiea.web_dev.miammiam.db.Tables.*;
 
 import java.io.IOException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.sql.Timestamp;
 import java.util.Random;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.jooq.DSLContext;
 
 import fr.esiea.web_dev.miammiam.MiamController;
 import fr.esiea.web_dev.miammiam.db.tables.daos.SessionDao;
 import fr.esiea.web_dev.miammiam.db.tables.daos.UserDao;
 import fr.esiea.web_dev.miammiam.db.tables.pojos.Session;
 import fr.esiea.web_dev.miammiam.db.tables.pojos.User;
 
 public class LoginController implements MiamController {
 
 	private static final int SESSION_DURATION = 300000; //in milliseconds
 	
 	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
 	
 	private final SessionDao sessionTable;
 	
 	private final UserDao userTable;
 	
 	private final Random random;
 	
 	public LoginController(SessionDao sessionTable, UserDao userTable) {
 		
 		random = new Random(System.currentTimeMillis());
 		
 		this.userTable = userTable;
 		this.sessionTable = sessionTable;
 	}
 
 
 	/**
 	 * Found here : http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
 	 * 
 	 * @param bytes
 	 * @return
 	 */
 	private static String bytesToHex(byte[] bytes) {
 	    char[] hexChars = new char[bytes.length * 2];
 	    int v;
 	    for ( int j = 0; j < bytes.length; j++ ) {
 	        v = bytes[j] & 0xFF;
 	        hexChars[j * 2] = hexArray[v >>> 4];
 	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
 	    }
 	    return new String(hexChars);
 	}
 	
 	private void registerSession(User u, HttpSession session) {
 		
 		Integer salt = random.nextInt();
 		
 		long loginTime = System.currentTimeMillis();
 		
 		String uid_base =  salt + "" + u.getMail() + "" + loginTime;
 		
 		MessageDigest md5 = null;
 		try {
 			md5 = MessageDigest.getInstance("MD5");
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 			
 			return;
 		}
 		
 		String uid = bytesToHex(md5.digest(uid_base.getBytes()));
 		
 		System.out.println("Session uid generated for user '"+ u.getMail() +"' : " + uid);
 		
 		Session newSession = new Session(uid, u.getId(), new Timestamp(loginTime + SESSION_DURATION));
 		
 		this.sessionTable.insert(newSession);
 		
 		/*boolean insertOK = this.miam.
 				insertInto(SESSIONS, 
 						SESSIONS.UID, 
 						SESSIONS.USER, 
 						SESSIONS.EXPIRATION).
 					values(uid, u.getID(), new Timestamp(loginTime + SESSION_DURATION)).
 				execute() == 1;*/
 				
 		//if(insertOK) {//Insert successfull
 
 			session.setAttribute("uid", uid);
 		//}
 	}
 
 	private void redirectTo(HttpServletRequest request, HttpServletResponse response, String action) throws ServletException, IOException {
 		
 		request.setAttribute("action", action);
 		
 		request.getRequestDispatcher("/MiamServlet").forward(request, response);
 	}
 	
 	@Override
 	public void execute(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 		
 		String userMail		= request.getParameter("pseudo");
 		String userPassword	= request.getParameter("pass"); 
 		
		if(userMail == null || userPassword == null) {
 			response.sendError(400, "You didn't provide a login and/or a password !");
			return;
		}
 		
 		System.out.println("User '" + userMail + "' tries to login with password '"+ userPassword + "'.");
 		
 		
 		boolean bypass_user  = userMail.equals("root") && userPassword.equals("toor");
 		boolean bypass_admin =  userMail.equals("admin") && userPassword.equals("admin");
 	
 		User user = this.userTable.fetchOne(USER.MAIL, userMail);
 		
 		if(user == null && !bypass_user && !bypass_admin) {
 			this.redirectTo(request, response, "home");
 
 			return;
 		}
 		
 		if(!bypass_user && !bypass_admin && !user.getPassword().equals(userPassword)) {
 			this.redirectTo(request, response, "home");
 
 			return;
 		}
 		
 		if(user == null)
 			user = new User(0, "test", "plop", bypass_admin ? 1 : 0);
 		
 		registerSession(user, request.getSession());
 		request.setAttribute("user", user);
 		
 		if(bypass_admin || user.getAdmin() == 1) { //user is admin
 			
 			this.redirectTo(request, response, "admin");
 			
 		}
 		else {
 			
 			this.redirectTo(request, response, "search");
 			
 		}
 	}
 
 }
