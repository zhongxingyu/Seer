 package moses.client.abstraction.apks;
 
 import java.util.regex.Pattern;
 
 /**
  * Reference to an application on the server, referenced by it's MoSeS id
  * 
  * @author Simon L
  * 
  */
 public class ExternalApplication {
 	private static final String TAG_DESCRIPTION = "[description]";
 
 	private static final String TAG_NAME = "[name]";
 
 	private static final String SEPARATOR = "#EA#";
 	
 	private String ID;
 
 	// lazy loading variables for non-defining attributes
 	private String name;
 	private String description;
 
 	public String getID() {
 		return ID;
 	}
 
 	public ExternalApplication(String ID) {
 		this.ID = ID;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	/**
 	 * Returns the name of this external application. If the name was not set,
 	 * or retrieved yet, this will be done now. In this case, this method should
 	 * not be called from the UI thread (method blocks).
 	 * 
 	 * @return
 	 */
 	public String getName() {
 		if (name != null) {
 			return name;
 		} else {
			return getGenericName();
 		}
 	}
 
 	/**
 	 * retrieves the name of this application from the server in case it was not
 	 * set yet.
 	 * 
 	 * @return the name of the application
 	 */
	private String getGenericName() {
 		// TODO: implement, placeholder
		return "loading Name...  (ID: " + getID() + ")";
 	}
 
 	/**
 	 * Returns the description of this external application. If the description
 	 * was not set, or retrieved yet, this will be done now. In this case, this
 	 * method should not be called from the UI thread (method blocks).
 	 * 
 	 * @return
 	 */
 	public String getDescription() {
 		if (description != null) {
 			return description;
 		} else {
			return retrieveDescription();
 		}
 	}
 
 	/**
 	 * retrieves the description of this application from the server in case it
 	 * was not set yet.
 	 * 
 	 * @return the description of the application
 	 */
 	private String retrieveDescription() {
		return "loading Description...";
 	}
 
 	/**
 	 * @return whether the description of the application was already
 	 *         retrieved/set
 	 */
 	public boolean isDescriptionSet() {
 		return description != null;
 	}
 
 	/**
 	 * @return whether the name of the application was already retrieved/set
 	 */
 	public boolean isNameSet() {
 		return name != null;
 	}
 	
 	public String asOnelineString() { //ID-{name}-{description}
 		String result = this.ID;
 		if(isNameSet()) {
 			result += SEPARATOR + TAG_NAME + getName();
 		}
 		if(isDescriptionSet()) {
 			result += SEPARATOR + TAG_DESCRIPTION + getName();
 		}
 		return result;
 	}
 	
 	public static ExternalApplication fromOnelineString(String s) {
 		String[] split = s.split(Pattern.quote(SEPARATOR));
 		String ID = null;
 		String name = null;
 		String description = null;
 		for(int i=0; i<split.length; i++) {
 			if(i==0) {
 				ID = split[i];
 			} else {
 				if(split[i].startsWith(TAG_DESCRIPTION)) {
 					description = split[i].substring(TAG_DESCRIPTION.length());
 				}
 				if(split[i].startsWith(TAG_NAME)) {
 					name = split[i].substring(TAG_NAME.length());
 				}
 			}
 		}
 		
 		ExternalApplication externalApplication = new ExternalApplication(ID);
 		externalApplication.setName(name);
 		externalApplication.setDescription(description);
 		return externalApplication;
 	}
 
 }
