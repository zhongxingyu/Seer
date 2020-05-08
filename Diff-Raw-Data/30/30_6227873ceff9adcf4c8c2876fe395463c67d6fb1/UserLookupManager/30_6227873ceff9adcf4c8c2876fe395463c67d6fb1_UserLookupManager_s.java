 package uk.ac.cam.signups.util;
 
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBufferByte;
 import java.awt.image.WritableRaster;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 
 import org.apache.commons.codec.binary.Base64;
 import org.eclipse.jetty.util.log.Log;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.cam.signups.helpers.LDAPQueryHelper;
 
 import com.google.common.collect.ImmutableMap;
 
 /**
 * @author      Holly Priest <hp343@cam.ac.uk>
  * @version     1                
  * A class to contain all data that may need to be looked up for a user.
  * Created once per session for a single crsid.
  * Stores the data after the first lookup to avoid looking up the same data
  * through LDAP multiple times.
  * Uses LDAPQueryHelper class to make queries
  */
 public class UserLookupManager {
 	private static UserLookupManager u;
 	
 	private static Logger log = LoggerFactory.getLogger(UserLookupManager.class);
 	
 	/**
 	 * Fields to cache user data once looked up
 	 */
 	private String crsid;
 	private String displayName;
 	private String surname;
 	private String email;
 	private String status;
 	private String photo;
 	private String institution;
 	private ImmutableMap<String, ?> userData;
 	
 	private UserLookupManager(String crsid){
 		this.crsid = crsid;
 	}
 	
 	/**
 	 * Get users display name
 	 * @return String displayName
 	 */
 	public String getDisplayName(){
 		if(displayName==null){
 			displayName = LDAPQueryHelper.getRegisteredName(crsid);
 		} 
 		return displayName;
 	}
 	
 	/**
 	 * Get users surname
 	 * @return String surname
 	 */
 	public String getSurname(){
 		if(surname==null){
 			surname = LDAPQueryHelper.getSurname(crsid);	
 		} 
 		return surname;
 	}
 	
 
 	/**
 	 * Get users email
 	 * @return String email
 	 */
 	public String getEmail(){
 		if(email==null){
 			email = LDAPQueryHelper.getEmail(crsid);	
 		} 
 		return email;
 	}
 
 	
 	/**
 	 * Gets a list of misAffiliations associated with user
 	 * If 'staff' misAffiliations user is present sets status as staff, otherwise student
 	 * @return String status
 	 */
 	public String getStatus(){
 		if(status==null){
 			status = LDAPQueryHelper.getStatus(crsid);
 		} 
 		return status;
 	}
 	
 	/**
 	 * Gets photo as an encoded base 64 jpeg
 	 * To display in soy template, use  <img src="data:image/jpeg;base64,{$user.photo}" /> or similar
 	 * @return String photo
 	 */
 	public String getPhoto(){
 		if(photo==null){
 			photo = LDAPQueryHelper.getPhoto(crsid);
 		} 
 		return photo;
 	}
 	
 	/**
 	 * Gets a list of institutions associated with user
 	 * @return String status
 	 */
 	public String getInstitution(){
 		if(institution==null){
 			institution = LDAPQueryHelper.getInstitution(crsid);
 		} 
 		return institution;
 	}
 	
 	/**
 	 * Builds a soy-ready immutable map of all data related to user
 	 * @return ImmutableMap userData
 	 */
 	public ImmutableMap<String, ?> getAll(){
 		if(userData==null){
 			getDisplayName();
 			getSurname();
 			getEmail();
 			getStatus();
 			getPhoto();
 			getInstitution();
 			userData = LDAPQueryHelper.getAll(crsid);
 		}
 		return userData;
 	}
 	
 	/**
 	 * Creates a UserLookupManager object if it does not already exist and
 	 * returns it, otherwise just returns the existing singleton instance
 	 * @param crsid
 	 * @returns UserLookupManager u
 	 *
 	 */
 	// Create instance of UserLookupManager if it doesn't exist
 	public static UserLookupManager getUserLookupManager(String crsid){
 		log.info("UserLookupManager created for " + crsid);
 		if(u==null){
 			u = new UserLookupManager(crsid);
 		} 
 		return u;
 	}
 }
