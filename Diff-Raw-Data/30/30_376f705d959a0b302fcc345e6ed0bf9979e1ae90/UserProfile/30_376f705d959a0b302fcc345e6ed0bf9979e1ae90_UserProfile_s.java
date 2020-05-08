 package au.edu.qut.inn372.greenhat.bean;
 
 import java.io.Serializable;
 
 import org.ksoap2.serialization.SoapObject;
 
 /**
  * Bean that represents a UserProfile
  * 
  * @author Charleston Telles -- Modified by Martins
  * @version 1.0
  */
 
 public class UserProfile extends AndroidAbstractBean implements Serializable {
 	/**
 	 * Unique Identifier
 	 */
 	private static final long serialVersionUID = 4599421625243305561L;
 	private String key;
 	private String name;
 	private String email="";
 	private String password;
 	private int type; //1=customer, 2=seller, 3=admin
 	
 	public UserProfile(){
 		
 	}
 	
 	public UserProfile(SoapObject userSoap, int soapOperation) {
 		if (userSoap != null){
 			SoapObject user = (SoapObject)userSoap.getProperty(0);
 			switch (soapOperation) {
 			case AndroidAbstractBean.OPERATION_VALIDATE_CREDENTIALS:
				this.name = new String(userSoap.getProperty("name").toString());
 				
				this.email = new String(userSoap.getProperty("email").toString());
				this.password = new String(userSoap.getProperty("password").toString());
				this.type = new Integer(userSoap.getProperty("type").toString());
 				break;
 			case AndroidAbstractBean.OPERATION_SAVE_USER_PROFILE:
				this.name = new String(user.getProperty("name").toString());
				this.name = new String( ( (userSoap.getProperty("name")) ).toString() );
				this.email = new String(userSoap.getProperty("email").toString());
				this.password = new String(userSoap.getProperty("password").toString());
				this.type = new Integer(userSoap.getProperty("type").toString());
 				break;
 			default:
 				break;
 			}		
 		}
 	}
 	
 	/**
 	 * Gets user profile key
 	 * @return key
 	 */
 	public String getKey() {
 		return key;
 	}
 	/**
 	 * Sets user profile key
 	 * @param key
 	 */
 	public void setKey(String key) {
 		this.key = key;
 	}
 	/**
 	 * Gets User profile name
 	 * @return name
 	 */
 	public String getName() {
 		return name;
 	}
 	/**
 	 * Sets User profile name
 	 * @param name
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 	/**
 	 * Gets user profile email
 	 * @return
 	 */
 	public String getEmail() {
 		return email;
 	}
 	/**
 	 * Sets user profile email
 	 * @param email
 	 */
 	public void setEmail(String email) {
 		this.email = email;
 	}
 	/**
 	 * Sets user profile Password
 	 * @return password
 	 */
 	public String getPassword() {
 		return password;
 	}
 	/**
 	 * Sets User profile Password
 	 * @param password
 	 */
 	public void setPassword(String password) {
 		this.password = password;
 	}
 	/**
 	 * Sets user profile Type.
 	 * 1=customer
 	 * 2=seller
 	 * 3=admin
 	 * @return type
 	 */
 	public int getType() {
 		return type;
 	}
 	/**
 	 * Gets user profile type
 	 * @param type
 	 */
 	public void setType(int type) {
 		this.type = type;
 	}
 	
 	//Added by Martins
 	/**
 	 * ATTENTION: ALL VALUES MUST BE CONVERTED TO STRING
 	 */
 	@Override
 	protected void setSoapObject(int soapOperation) {
 		switch (soapOperation) {
 		case AndroidAbstractBean.OPERATION_CALC_ENERGY_PRODUCTION:
 			this.soapObject = new SoapObject(AndroidAbstractBean.NAMESPACE,AndroidAbstractBean.OPERATION_CALC_ENERGY_PRODUCTION_NAME);
 			this.soapObject = setDefaultSoapObject(this.soapObject);
 			break;
 		case AndroidAbstractBean.OPERATION_SAVE_CALCULATION:
 			this.soapObject = new SoapObject(AndroidAbstractBean.NAMESPACE,AndroidAbstractBean.OPERATION_SAVE_CALCULATION_NAME);
 			this.soapObject = setDefaultSoapObject(this.soapObject);
 			break;
 		case AndroidAbstractBean.OPERATION_VALIDATE_CREDENTIALS:
 			this.soapObject = new SoapObject(AndroidAbstractBean.NAMESPACE,AndroidAbstractBean.OPERATION_VALIDATE_CREDENTIALS_NAME);
 			this.soapObject = setDefaultSoapObject(this.soapObject);
 			break;
 		case AndroidAbstractBean.OPERATION_SAVE_USER_PROFILE:
 			this.soapObject = new SoapObject(AndroidAbstractBean.NAMESPACE,AndroidAbstractBean.OPERATION_SAVE_USER_PROFILE_NAME);
 			SoapObject profileElement = new SoapObject("", "userProfile");
 			profileElement = setDefaultSoapObject(profileElement);
 			this.soapObject.addSoapObject(profileElement);
 			break;
 		default:
 			this.soapObject = new SoapObject("", "userProfile");
 			this.soapObject = setDefaultSoapObject(this.soapObject);
 			break;
 		}
 	}
 	
 	/**
 	 * Adds all object properties to the given soap object and returns it
 	 * @param currentSoapObject Soap object that fields are added to
 	 * @return Amended soap object with fields
 	 */
 	private SoapObject setDefaultSoapObject(SoapObject currentSoapObject) {
 		//currentSoapObject.addProperty("key", ""+this.key);
 		currentSoapObject.addProperty("name", ""+this.name);
 		currentSoapObject.addProperty("email", ""+this.email);
 		currentSoapObject.addProperty("password", ""+this.password);
 		currentSoapObject.addProperty("type", ""+this.type);
 		return currentSoapObject;
 	}
 	
 }
