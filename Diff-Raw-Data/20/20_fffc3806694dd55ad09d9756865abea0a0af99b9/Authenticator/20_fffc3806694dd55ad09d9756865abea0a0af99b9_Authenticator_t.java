 package com.grademaster.auth;
 
 import java.util.Iterator;
 
 import com.eakjb.EakjbData.DataStructureQuery;
 import com.eakjb.EakjbData.IDataObject;
 import com.eakjb.EakjbData.IDataStructure;
 import com.eakjb.EakjbData.IQuery;
 import com.grademaster.Globals;
 /**
  * Authenticator objects are used to compare passwords and lookup
  * user data
  * @author jake
  *
  */
 public class Authenticator {
 	/**
 	 * The UserDataBase in which the Authenticator looks up user data
 	 */
 	private IDataStructure users;
 	/**
 	 * Constructs an Authenticator object with a given UserDataBase
 	 * @param users a UserDataBase object in which user data will be searched for
 	 */
 	public Authenticator(IDataStructure users) {
 		this.users=users;
 	}
 	/**
 	 * Constructs an Authenticator object with a default UserDataBase taken from com.grademaster.globals.getUsers()
 	 * @throws Exception 
 	 */
 	public Authenticator() throws Exception {
 		this(Globals.loadXMLFile(Globals.getUserURL()));
 	}
 	/**
 	 * Looks up a user object based on given data returns null if no user found
 	 * @param uname The username of the user being looked up
 	 * @param pword The password (raw String) of the user being looked up.  Remember, this will be hashed with SHA-256 before comparison
 	 * @param type The type of account currently (as of 12-17-2012) "student" or "teacher"
 	 * @return either a User object with given properties or null if none was found
 	 */
 	public IDataStructure authUser(String uname, String pword, String type) {
 		Hasher h = new Hasher(pword);
 		IDataStructure user = null;
 		String hpword=h.getHashed();
 		
 		IQuery q = new DataStructureQuery(users,"user");
 		IDataStructure us=(IDataStructure) q.execute();
 		
 		Iterator<IDataObject> usi = us.iterator();
 		
 		while (usi.hasNext()) {
 			IDataStructure u = (IDataStructure) usi.next();
 			if (u.get("uname").getTextValue().equals(uname)&&u.get("pword").getTextValue().equals(hpword)&&u.get("type").getTextValue().equals(type)) {
 				user=u;
 				break;
 			}
 		}
 		
 		
 		return user;
 	}
 	/**
 	 * Gets the type of user (as of 12-17-2012 "student" or "teacher") or returns null if not found
 	 * @param uname The username of the user being looked up
 	 * @return the user type of the user with a given username (as of 12-17-2012 "student or "teacher") or null if no user was found
 	 */
 	public String typeUser(String uname) {
 		IQuery q = new DataStructureQuery(users,"user");
 		IDataStructure us=(IDataStructure) q.execute();
 		
 		Iterator<IDataObject> usi = us.iterator();
 		
 		while (usi.hasNext()) {
 			IDataStructure u = (IDataStructure) usi.next();
 			if (u.get("uname").getTextValue().equals(uname)) {
 				return u.get("type").getTextValue();
 			}
 		}
 		return null;
 	}
 }
