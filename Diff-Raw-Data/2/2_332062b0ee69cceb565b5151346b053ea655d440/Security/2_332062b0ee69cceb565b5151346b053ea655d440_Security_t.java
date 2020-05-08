 package controllers;
 
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.persistence.NoResultException;
 import models.*;
 
 /**
  * Extended class for authorization and authentication
  * @author Daniel Robenek <danrob@seznam.cz>
  */
 public class Security extends Secure.Security {
 
 	/**
 	 * Is this user allowed to log in?
 	 * @param login
 	 * @param password
 	 * @return 
 	 */
 	static boolean authenticate(String login, String password) {
 		User user = null;
 		try {
 			user = User.findActiveByLogin(login);
 		} catch (NoResultException e) {
 			return false;
 		}
 		if (!user.password.equals(hashPassword(password))) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Processed after authentication
 	 */
 	static void onAuthenticated() {
 		Role role = null;
 		try {
 			role = User.getLoggedUser().role;
 		} catch (NoResultException e) {
 			try {
 				Secure.login();
 			} catch (Throwable ex) {
 				Logger.getLogger(Security.class.getName()).log(Level.SEVERE, null, ex);
 			}
 		}
 		if (role.is(Role.Check.ADMIN)) {
			redirect("admin.Competitions.list");
 		}
 		if (role.is(Role.Check.CONTESTANT)) {
 			controllers.contestant.Competitions.index();
 		}
 		throw new IllegalStateException("Invalid role " + role.key);
 	}
 
 	/**
 	 * Has user any role specified in profile? (separated by comma)
 	 * @param profile
 	 * @return 
 	 */
 	static boolean check(String profile) {
 		String[] roles = profile.split(",");
 		String userRole;
 		try {
 			userRole = User.findActiveByLogin(connected()).role.key;
 		} catch (NoResultException e) {
 			return false;
 		}
 		for (String role : roles) {
 			if (userRole.equals(role)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * When user is not allowed to process action
 	 * @param profile 
 	 */
 	public static void onCheckFailed(String profile) {
 		forbidden();
 	}
 
 	/**
 	 * When user is not allowed to process action
 	 * @param profile 
 	 */
 	public static void onCheckFailed() {
 		onCheckFailed(null);
 	}
 
 	/**
 	 * Hash password by SHA-256
 	 * @param password
 	 * @return 
 	 */
 	public static String hashPassword(String password) {
 		MessageDigest md;
 		try {
 			md = MessageDigest.getInstance("SHA-256");
 		} catch (NoSuchAlgorithmException ex) {
 			throw new RuntimeException(ex);
 		}
 		String text = "rg$#%dargvbnsf!fw5364dsadfga8w4effs" + password;
 		try {
 			md.update(text.getBytes("UTF-8"));
 		} catch (UnsupportedEncodingException ex) {
 			throw new RuntimeException(ex);
 		}
 		byte[] digest = md.digest();
 		StringBuilder sb = new StringBuilder();
 		for (byte b : digest) {
 			if (b > 0 && b < 16) {
 				sb.append("0");
 			}
 			sb.append(Integer.toHexString(b & 0xff));
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * Return user name
 	 * @return 
 	 */
 	public static String getUsername() {
 		return isConnected() ? connected() : null;
 	}
 }
