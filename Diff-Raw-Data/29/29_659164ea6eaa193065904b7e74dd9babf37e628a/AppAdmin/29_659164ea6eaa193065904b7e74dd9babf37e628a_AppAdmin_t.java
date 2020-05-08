 package edu.gatech.oad.rocket.findmythings.server.model;
 
 import java.util.Set;
 
 import com.googlecode.objectify.annotation.EntitySubclass;
 
 /**
  * CS 2340 - FindMyStuff Android App
  * This class creates a new AppAdmin object
  *
  * @author TeamRocket
  * */
 @EntitySubclass
 public class AppAdmin extends AppMember {
 
 	/**
 	 *
 	 */
 	private static final long serialVersionUID = 1381561058937653330L;
 
 	/** Constructors **/
 
     public AppAdmin(String email, Set<String> roles, Set<String> permissions) {
 		super(email, roles, permissions);
 	}
 
 	public AppAdmin(String email, String password, Set<String> roles,
 			Set<String> permissions, boolean isRegistered) {
 		super(email, password, roles, permissions, isRegistered);
 	}
 
 	public AppAdmin(String email, String password, Set<String> roles,
 			Set<String> permissions) {
 		super(email, password, roles, permissions);
 	}
 
 	public AppAdmin(String email, String password) {
 		super(email, password);
 	}
 
 	public AppAdmin(String email) {
 		super(email);
 	}
 
	protected AppAdmin() {
		super();
	}

 	/** Accessors **/
 
 	@Override
 	public final boolean isLocked() {
 		return false;
 	}
 
 	/**
 	 * checks if a certain member is an AppAdmin
 	 * @return True
 	 */
 	@Override
 	public final boolean isAdmin() {
 		return true;
 	}
 
 }
