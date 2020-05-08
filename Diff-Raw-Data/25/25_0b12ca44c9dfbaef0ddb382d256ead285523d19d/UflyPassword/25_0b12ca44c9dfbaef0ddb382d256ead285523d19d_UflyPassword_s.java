 package ufly.entities;
 
 import java.io.Serializable;
 
 /**
  * 
  * @author joel, felix
  *
  * This class will be used to store passwords, it has two fields,
  * the stored password, and the security version,
  * the stored password will be different based on the security version
  * that way if we upgrade the security method, we can still find out
  * how the old passwords were stored, and upgrade them on login.
  * It is impossible to convert because we don't necessarily know the password
  * that we have stored. We will probably only know the hashed value
  * 
  * This class is a serializable class and not a JDO data class. This class will not be indexed and
  * cannot be used in query filters or sort orders
  */
 public class UflyPassword implements Serializable
 {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 4981756966545899133L;
 	/**
 	 * Create a new password object
 	 * @param password
 	 */
 	public UflyPassword(String password)
 	{
 		this.securityVersion=0;
 		this.storedPassword = password;
 	}
 	
 	public String getStoredPassword() {
 		return storedPassword;
 	}
 	
 	/**
 	 * Check if password check is a matching password
 	 * @param check :possible matching password
 	 * @return true if password matches, else return false
 	 */
 	public boolean Matches(String check)
 	{
 		switch(this.getSecurityVersion())
 		{
 		case 0://PlainText
			if (this.storedPassword == check)
 				return true;
 			return false;
 		//add cases for future security models
 		default:
 			return false;
 		}
 	}
 	public int getSecurityVersion() {
 		return securityVersion;
 	}
 
 	private String storedPassword;
 	private int securityVersion;
 }
