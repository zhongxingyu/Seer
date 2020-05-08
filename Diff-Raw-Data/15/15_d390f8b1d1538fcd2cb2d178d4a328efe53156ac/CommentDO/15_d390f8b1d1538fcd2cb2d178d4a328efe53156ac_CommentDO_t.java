 package com.zarcode.data.model;
 
 import java.io.Serializable;
 import java.net.URLDecoder;
 import java.util.Date;
 import java.util.logging.Logger;
 
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.IdentityType;
 import javax.jdo.annotations.NotPersistent;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 import com.google.appengine.api.datastore.Text;
 import com.zarcode.app.AppCommon;
 import com.zarcode.common.ApplicationProps;
 import com.zarcode.data.dao.UserDao;
 import com.zarcode.platform.model.AbstractLoaderDO;
 import com.zarcode.platform.model.AppPropDO;
 import com.zarcode.security.BlockTea;
 
 @XmlRootElement(name = "Comment") 
 @PersistenceCapable(identityType = IdentityType.APPLICATION)
 public class CommentDO extends AbstractLoaderDO implements Serializable, Comparable<CommentDO> {
 	
 	/**
 	 * logger
 	 */
 	private Logger logger = Logger.getLogger(CommentDO.class.getName());
 	
 	@NotPersistent
 	private String timeDisplay = null;
 	
 	@NotPersistent
 	private String response = "END OF LIST";
 	
 	@NotPersistent
 	private String profileUrl = null;
 	
 	@NotPersistent
 	private String username = null;
 
 	@PrimaryKey 
     @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
 	private Long commentId = null; 
 	
 	@Persistent
 	private Long msgId = null; 
 	
 	@Persistent
 	private Long timestamp = null;
 	
 	@Persistent
 	private String llId = null;
 	
 	@Persistent
 	private String idClear = null;
 	
 	@Persistent
 	private Long resourceId = null;
 	
 	@Persistent
 	private Text responseText = null;
 	
 	@Persistent
 	private double lat = 0;
 	
 	@Persistent
 	private double lng = 0;
 	
 	@Persistent
 	private Date createDate = null;
 	
 	public void postCreation() {
 		logger.info("lat=" + lat + " lng=" + lng);
 		createDate = new Date();
 		responseText = new Text(response);
 		
 		/*
 		 * URL decode
 		 */
 		llId = URLDecoder.decode(llId);
 		
 		/*
 		 * decrypt llId
 		 */
 		AppPropDO p1 = ApplicationProps.getInstance().getProp("CLIENT_TO_SERVER_SECRET");
 		BlockTea.BIG_ENDIAN = false;
 		String plainText = BlockTea.decrypt(llId, p1.getStringValue());
 		logger.info("Decrypted llId: " + plainText + " Encrypted llId: " + llId);
 		idClear = plainText;
 	}
 	
	/**
	 * This method should be called after CommentDO is retrieved from GAE.
	 */
 	public void postReturn() {
 		UserDO user = null;
 		UserDao userDao = new UserDao();
 		if (responseText != null) {
 			this.response = responseText.getValue();
 		}
 		timeDisplay = AppCommon.generateTimeOffset(createDate);
		logger.info("postReturn: timeDisplay=" + timeDisplay);
 		user = userDao.getUserByIdClear(this.idClear);
 		if (user != null) {
			this.username = user.getDisplayName();
			this.profileUrl = user.getProfileUrl();
 		}
 		else {
			this.username = AppCommon.UNKNOWN;
 		}
 	}
 	
 	/////////////////////////////////////////////////////////////////////////////////////////////
 	//
 	// Implements Comparable Interface
 	//
 	/////////////////////////////////////////////////////////////////////////////////////////////
 	
 	public int compareTo(CommentDO n) {
 		if (n.timestamp < this.timestamp) {
 			return 1;
 		}
 		else if (n.timestamp == this.timestamp) {
 			return 0;
 		}
 		else {
 			return -1;
 		}
 	}
 	
 	@XmlElement
 	public Long getMsgId() {
 		return msgId;
 	}
 
 	public void setMsgId(Long msgId) {
 		this.msgId = msgId;
 	}
 	
 	@XmlElement
 	public Long getCommentId() {
 		return commentId;
 	}
 
 	public void setCommentId(Long commentId) {
 		this.commentId = commentId;
 	}
 	
 	@XmlElement
 	public double getLat() {
 		return lat;
 	}
 
 	public void setLat(double lat) {
 		this.lat = lat;
 	}
 	
 	@XmlElement
 	public double getLng() {
 		return lng;
 	}
 
 	public void setLng(double lng) {
 		this.lng = lng;
 	}
 	
 	@XmlElement
 	public Long getTimestamp() {
 		return timestamp;
 	}
 
 	public void setTimestamp(Long timestamp) {
 		this.timestamp = timestamp;
 	}
 	
 	@XmlElement
 	public Long getResourceId() {
 		return resourceId;
 	}
 
 	public void setResourceId(Long resourceId) {
 		this.resourceId = resourceId;
 	}
 	
 	@XmlElement
 	public String getTimeDisplay() {
 		return timeDisplay;
 	}
 
 	public void setTimeDisplay(String timeDisplay) {
 		this.timeDisplay = timeDisplay;
 	}
 	
 	@XmlTransient
 	public String getLlId() {
 		return llId;
 	}
 
 	public void setLlId(String llId) {
 		this.llId = llId;
 	}
 	
 	@XmlTransient
 	public String getIdClear() {
 		return idClear;
 	}
 
 	public void setIdClear(String idClear) {
 		this.idClear = idClear;
 	}
 	
 	@XmlElement
 	public String getResponse() {
 		return response;
 	}
 
 	public void setResponse(String response) {
 		this.response = response;
 	}
 	
 	@XmlElement
 	public String getProfileUrl() {
 		return profileUrl;
 	}
 
 	public void setProfileUrl(String url) {
 		this.profileUrl = url;
 	}
 	
 	@XmlElement
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 	
 	
     @XmlElement
 	public Date getCreateDate() {
 		return createDate;
 	}
 
 	public void setCreateDate(Date createDate) {
 		this.createDate = createDate;
 	}
 	
 	public String toString() {
 		return "CommentDO::" + commentId + " (llId=" + llId + " timestamp=" + timestamp + ")";
 	}
 
 }
