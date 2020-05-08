 package eu.trentorise.smartcampus.mediation.model;
 
 import java.io.Serializable;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import eu.trentorise.smartcampus.network.JsonUtils;
 
 public class MessageToMediationService implements Serializable {
 
 	private boolean parseApproved;
 	private boolean mediationApproved;
 	private long timestamp;
 	private String webappname;
 	private int entityId;
 	private String entityTesto;
 	private String note;
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	
 
 	public MessageToMediationService(String webappname,int entityId,String entityTesto) {
 		this.setWebappname(webappname);
 		this.timestamp = System.currentTimeMillis();
 		this.mediationApproved = true;
 		this.parseApproved = true;
 		this.setEntityId(entityId);
 		this.setEntityTesto(entityTesto);
 	}
 
 	public boolean isParseApproved() {
 		return parseApproved;
 	}
 
 	public void setParseApproved(boolean parseApproved) {
 		this.parseApproved = parseApproved;
 	}
 
 	public String getWebappname() {
 		return webappname;
 	}
 
 	public void setWebappname(String webappname) {
 		this.webappname = webappname;
 	}
 
 	public boolean isMediationApproved() {
 		return mediationApproved;
 	}
 
 	public void setMediationApproved(boolean mediationApproved) {
 		this.mediationApproved = mediationApproved;
 	}
 
 	public long getTimestamp() {
 		return timestamp;
 	}
 
 	public void setTimestamp(long timestamp) {
 		this.timestamp = timestamp;
 	}
 
 	public int getEntityId() {
 		return entityId;
 	}
 
 	public void setEntityId(int entityId) {
 		this.entityId = entityId;
 	}
 
 	public String getEntityTesto() {
 		return entityTesto;
 	}
 
 	public void setEntityTesto(String entityTesto) {
 		this.entityTesto = entityTesto;
 	}
 
 	public String ToJson() {
 		String object=new String();
 		object="{\"parseApproved\":"+parseApproved+",\"mediationApproved\":"+mediationApproved+",\"timestamp\":"+timestamp+",\"webappname\":\""+webappname+"\",\"entityId\":"+entityId+",\"entityTesto\":\""+entityTesto+"\",\"note\":\""+note+"\"}";
 		
 		return object;
 	}
 
 	
 	public static MessageToMediationService valueOf(String json) {
 		try {
 			JSONObject o = new JSONObject(json);
 			String webapp =o.getString("webappname");
 			String entityTesto =o.getString("entityTesto");
 			int entityId =o.getInt("entityId");
 			
 			MessageToMediationService messageToMediationService = new MessageToMediationService(webapp,entityId,entityTesto);
 			messageToMediationService.setMediationApproved(o.getBoolean("mediationApproved"));
 			messageToMediationService.setParseApproved(o.getBoolean("parseApproved"));
 			messageToMediationService.setTimestamp(o.getLong("timestamp"));
			if(o.has("note"))
 			messageToMediationService.setNote(o.getString("note"));
 			
 		
 			return messageToMediationService;
 		} catch (JSONException e) {
 			return null;
 		}
 	}
 
 	public String getNote() {
 		return note;
 	}
 
 	public void setNote(String note) {
 		this.note = note;
 	}
 }
