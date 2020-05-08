 package com.fauxwerd.model;
 
 import javax.validation.constraints.Pattern;
 
 import org.codehaus.jackson.annotate.JsonProperty;
 
 public class EmailSubscription {
 
 //	listSubscribe(string apikey, string id, string email_address, array merge_vars, string email_type, bool double_optin, 
 //	bool update_existing, bool replace_interests, bool send_welcome)
 
 	private String apiKey = null;
 	private String email = null;
 	private String listId = null;
 	private boolean doubleOptIn = false;
 	private String code = null;
 
 	@JsonProperty(value="apikey")
 	public String getApiKey() {
 		return apiKey;
 	}
 	public void setApiKey(String apiKey) {
 		this.apiKey = apiKey;
 	}
 
	@Pattern(regexp="^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}$", flags={Pattern.Flag.CASE_INSENSITIVE})	
 	@JsonProperty(value="email_address")
 	public String getEmail() {
 		return email;
 	}
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	@JsonProperty(value="id")
 	public String getListId() {
 		return listId;
 	}
 	public void setListId(String listId) {
 		this.listId = listId;
 	}
 
 	@JsonProperty(value="double_optin")
 	public boolean isDoubleOptIn() {
 		return doubleOptIn;
 	}
 	public void setDoubleOptIn(boolean doubleOptIn) {
 		this.doubleOptIn = doubleOptIn;
 	}
 	
 	@JsonProperty(value="code")
 	public String getCode() {
 		return code;
 	}
 	public void setCode(String code) {
 		this.code = code;
 	}
 }
