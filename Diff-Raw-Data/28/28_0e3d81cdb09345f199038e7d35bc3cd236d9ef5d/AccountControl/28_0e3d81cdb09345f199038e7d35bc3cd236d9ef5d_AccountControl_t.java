 /**
  * 
  */
 package ApplicationLogic;
 
 import java.io.UnsupportedEncodingException;
 import java.nio.charset.Charset;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.security.spec.InvalidKeySpecException;
 import java.sql.SQLException;
 import java.util.EnumSet;
 
 import javax.crypto.SecretKey;
 import javax.crypto.SecretKeyFactory;
 import javax.crypto.spec.PBEKeySpec;
 import javax.servlet.http.HttpSession;
 import javax.xml.bind.DatatypeConverter;
 
 import DatabaseFrontend.Permission;
 import DatabaseFrontend.User;
 
 public class AccountControl {
 	private final HttpSession s;
 	final Control p;
 
 	public AccountControl(HttpSession s, Control c) {
 		this.s = s;
 		this.p = c;
 	}
 	
 	private static final EnumSet<Permission> stdPermissions = EnumSet.noneOf(Permission.class);
 	public static final EnumSet<Permission> stdUserPermissions = EnumSet.allOf(Permission.class);
 
 	private UserWrapper loggedUser;
 	private static SecureRandom r = new SecureRandom();
 
 	public RegStatus tryRegisterUser(String email, String userName, String contactInfo,
 			String password, Permission... permissions) {
 		UserWrapper temp = p.userControl.getUserFromEmail(email);
 		if (temp != null)
 			return RegStatus.EMAIL_TAKEN;
 		temp = p.userControl.getUserFromName(userName);
 		if (temp != null)
 			return RegStatus.NAME_TAKEN;
 		if (password.length() < 8)
 			return RegStatus.PASS_TOO_SHORT;
 		String salt = makeSalt();
 		try {
 			loggedUser = p.userControl.wrap(User.makeUser(email, userName, contactInfo, hash(password, salt), stdUserPermissions, salt));
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return RegStatus.ERROR;
 		}
 		return RegStatus.OK;
 	}
 
 	public boolean tryLogin(String name, String password) {
 		UserWrapper toRet = p.userControl.getUserFromEmailOrName(name);
 		if (toRet == null)
 			return false;
 		if (!toRet.checkPass(password))
 			toRet = null;
		 s.setAttribute("userId", toRet.getUser().getID());
 		loggedUser = toRet;
 		return loggedUser != null;
 	}
 
 	public void logOut() {
 		 s.removeAttribute("userId");
 		loggedUser = null;
 		s.invalidate();
 	}
 	
 	public boolean isLoggedIn() {
 		return getLoggedInUser() != null;
 	}
 	
 	public boolean isLoggedInUserAllowed(Permission p) {
 		UserWrapper w = getLoggedInUser();
 		if (w != null)
 			return w.isAllowed(p);
 		return stdPermissions.contains(p);
 	}
 	
 	
 	public UserWrapper getLoggedInUser() {
 		if (loggedUser != null)
 			return loggedUser;
 		Object o = s.getAttribute("userId");
 		if (o == null)
 			return null;
 		else if (!(o instanceof Long)) {
 			System.err.println("Uh-oh. Invalid object type: " + o);
 			s.removeAttribute("userId");
 		} else {
 			loggedUser = p.userControl.getUserFromId((Long) o);
 		}
 		return loggedUser;
 	}
 
 	public static String hash(String password, String salt) {
 		if (password == null || password.length() == 0)
 			throw new IllegalArgumentException(
 					"Empty passwords are not supported.");
 		SecretKey key = null;
 		try {
 			SecretKeyFactory f = SecretKeyFactory
 					.getInstance("PBKDF2WithHmacSHA1");
 			key = f.generateSecret(new PBEKeySpec(password.toCharArray(), salt
 					.getBytes("UTF-8"), 10 * 1024, 512));
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		} catch (InvalidKeySpecException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 		return DatatypeConverter.printBase64Binary(key.getEncoded());
 	}
 
 	public static String makeSalt() {
 		final byte[] saltBytes = new byte[512 / 8];
 		r.nextBytes(saltBytes);
 		return DatatypeConverter.printBase64Binary(saltBytes);
 	}
 
 	public boolean isLoggedInUserAllowed(UserWrapper userWrapper,
 			Permission p) {
 		return true;
 	}
 
 	public boolean isLoggedInUserAllowed(UserWrapper userWrapper,
 			Permission editownuser, Permission editotherusers) {
 		return true;
 	}
 }
