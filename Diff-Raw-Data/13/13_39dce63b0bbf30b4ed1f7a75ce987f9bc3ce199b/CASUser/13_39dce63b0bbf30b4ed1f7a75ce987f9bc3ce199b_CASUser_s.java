 /*
  * Created on Jul 27, 2004
  *
  * To change the template for this generated file go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 package org.alt60m.security.CAS;
 
 import java.util.Map;
 import java.io.Serializable;
 
 /**
  * @author justin.sabelko
  *
  * To change the template for this generated type comment go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 public class CASUser implements Serializable {
 
 	private String username;
 	private String pgtIou;
 	private Map attributes;
 	private static final String ATTR_GUID = "ssoGuid";
 	private static final String ATTR_ACCT_NO = "emplid";
 	private static final String ATTR_FIRSTNAME = "firstName";
 	private static final String ATTR_LASTNAME = "lastName";
 
 	/**
 	 * @param user
 	 * @param attributes
 	 */
 	public CASUser(String user, String pgtiou, Map attr) {
 		username = user;
 		pgtIou = pgtiou;
 		attributes = attr;
 		
 		// FOR TESTING PURPOSES ONLY
 /*		if (username.equals("s0544639")) {
 			username = "s0544639.mouse@uscm.org";
 		}*/
 	}
 	
 	/**
 	 * @return Returns the attributes.
 	 */
 	public Map getAttributes() {
 		return attributes;
 	}
 	/**
 	 * @return Returns the username.
 	 */
 	public String getUsername() {
 		return username;
 	}
 
 	public String getPgtIou() {
 		return pgtIou;
 	}	
 	
 	
 	//convenience methods
 	
 	/**
 	 * @return
 	 */
 	public String getGUID() {
 		return (String)attributes.get(ATTR_GUID);
 	}
 
 	/**
 	 * @return
 	 */
 	public String getAcctNo() {
 		return (String)attributes.get(ATTR_ACCT_NO);
 	}
 	
 	public String getFirstName()
 	{
 		return (String) attributes.get(ATTR_FIRSTNAME);
 	}
 	
 	public String getLastName()
 	{
 		return (String) attributes.get(ATTR_LASTNAME);
 	}
 }
