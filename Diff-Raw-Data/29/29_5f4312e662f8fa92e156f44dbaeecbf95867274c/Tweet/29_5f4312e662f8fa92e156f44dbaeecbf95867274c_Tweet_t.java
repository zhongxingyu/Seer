 package com.vaguehope.senkyou.model;
 
import static com.vaguehope.senkyou.util.Dates.fixDate;

 import java.util.Date;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 @XmlRootElement(name = "tweet")
 @XmlAccessorType(XmlAccessType.NONE)
 public class Tweet {
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 	
 	private volatile long id;
 	private volatile long inReplyId;
 	@XmlElement(name = "user") private volatile String user;
 	@XmlElement(name = "name") private volatile String name;
 	@XmlElement(name = "created") private volatile Date createdAt;
 	@XmlElement(name = "body") private volatile String body;
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 	
 	public long getId () {
 		return this.id;
 	}
 	
 	public Date getCreatedAt () {
		return fixDate(this.createdAt);
 	}
 	
 	public long getInReplyId () {
 		return this.inReplyId;
 	}
 	
 	public String getUser () {
 		return this.user;
 	}
 	
 	public String getName () {
 		return this.name;
 	}
 	
 	public String getBody () {
 		return this.body;
 	}
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 	
 	public void setId (long id) {
 		this.id = id;
 	}
 	
 	public void setCreatedAt (Date createdAt) {
		this.createdAt = fixDate(createdAt);
 	}
 	
 	public void setInReplyId (long inReplyId) {
 		this.inReplyId = inReplyId;
 	}
 	
 	public void setUser (String user) {
 		this.user = user;
 	}
 	
 	public void setName (String name) {
 		this.name = name;
 	}
 	
 	public void setBody (String body) {
 		this.body = body;
 	}
	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 }
