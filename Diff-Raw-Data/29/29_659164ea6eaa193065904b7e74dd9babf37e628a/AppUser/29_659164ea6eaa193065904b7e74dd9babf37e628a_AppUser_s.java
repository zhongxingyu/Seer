 package edu.gatech.oad.rocket.findmythings.server.model;
 
 import java.util.Set;
 
 import com.googlecode.objectify.annotation.EntitySubclass;
 
 /**
  * CS 2340 - FindMyStuff Android App
  * This class creates a new AppUser object
  *
  * @author TeamRocket
  * */
 @EntitySubclass
 public class AppUser extends AppMember {
 
     /**
 	 *
 	 */
 	private static final long serialVersionUID = 2321636178121300257L;
 
 	private boolean isLocked;
 
 	/** Constructors **/
 
     public AppUser(String email, Set<String> roles, Set<String> permissions) {
 		super(email, roles, permissions);
 	}
 
 	public AppUser(String email, String password, Set<String> roles,
 			Set<String> permissions, boolean isRegistered) {
 		super(email, password, roles, permissions, isRegistered);
 	}
 
 	public AppUser(String email, String password, Set<String> roles,
 			Set<String> permissions) {
 		super(email, password, roles, permissions);
 	}
 
 	public AppUser(String email, String password) {
 		super(email, password);
 	}
 
 	public AppUser(String email) {
 		super(email);
 	}
 
 	/** Accessors **/
 
 	@Override
 	public boolean isLocked() {
         return isLocked;
     }
 
     public void setLocked(boolean locked) {
 	isLocked = locked;
     }
 
 	@Override
 	public boolean isAdmin() {
 		return false;
 	}
 
 }
