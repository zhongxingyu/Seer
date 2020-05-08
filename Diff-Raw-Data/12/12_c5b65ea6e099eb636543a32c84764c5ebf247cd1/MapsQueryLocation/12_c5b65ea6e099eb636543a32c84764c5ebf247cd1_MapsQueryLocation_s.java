 package il.ac.tau.team3.addressQuery;
 
 import java.io.Serializable;
 
 import org.codehaus.jackson.annotate.JsonIgnoreProperties;
 
 
 @JsonIgnoreProperties(ignoreUnknown = true)
 
 public class MapsQueryLocation implements Serializable {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 533627910597277058L;
 	
 	
 	private String display_name;
 
 
 	public void setDisplay_name(String display_name) {
 		this.display_name = display_name;
 	}
 
 
 	public String getDisplay_name() {
		return display_name.replace("Al Wa'rat, ", "").replace("West Bank, ", "");
 	}
 	
 	
 	
 	
 };
