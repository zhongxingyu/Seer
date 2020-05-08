 /*
  * $Id: LoginForm.java,v 1.4 2008-05-27 14:35:32 pandyas Exp $
  *
  * $Log: not supported by cvs2svn $
  * Revision 1.3  2006/04/17 19:09:19  pandyas
  * caMod 2.1 OM changes
  *
  */
 package gov.nih.nci.camod.webapp.form;
 
 import gov.nih.nci.camod.util.SafeHTMLUtil;
 
 import java.io.Serializable;
 import javax.servlet.http.HttpServletRequest;
 import org.apache.struts.action.ActionMapping;
 
 public class LoginForm extends BaseForm implements Serializable {
     
     private static final long serialVersionUID = 3257165453799404851L;
     
 	/**
 	 * Default empty constructor
 	 * @author nschroedl
 	 */
 	public LoginForm() {}
 	
 	protected String username;
 	protected String password;
 	
 	/**
 	 * @return Returns the username.
 	 */
 	public String getUsername() {
 		return username;
 	}
 	/**
 	 * @param username The username to set.
 	 * Do not clean variable for SQL injection here
 	 * camodcurator%27 cleans to camodcurator and 
 	 * then incorrectly continues through the code
 	 * so a hacker can get lucky
 	 */
 	public void setUsername(String username) {
 		this.username = username;
 	}
 	/**
 	 * @param password The password to set.
 	 */
 	public void setPassword(String password) {
 		this.password = password;
 	}
 	/**
 	 * @return Returns the password.
 	 */
 	public String getPassword() {
 		return password;
 	}
 
 	public void reset(ActionMapping mapping, HttpServletRequest request) {
             // reset any boolean data types to false
         }
 }
