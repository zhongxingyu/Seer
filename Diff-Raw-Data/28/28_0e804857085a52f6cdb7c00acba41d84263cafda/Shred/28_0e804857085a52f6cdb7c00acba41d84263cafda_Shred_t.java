 package com.mikey.shredhub.domain;
 
 import java.util.Date;
 import java.util.List;
 
 import org.bson.types.ObjectId;
 import org.springframework.data.annotation.Id;
 import org.springframework.data.mongodb.core.mapping.DBRef;
 import org.springframework.data.mongodb.core.mapping.Document;
 
 @Document
 public class Shred {
 	
 	@Id
 	private String id;
 	
 	private String description;
 	
 	@DBRef
 	private Shredder owner;
 	
 	private Date timeCreated;
 	
 	private String videoPath;
 	
 	private String videoThumbnail;
 	
 	private List <ShredComment> shredComments;
 	
 	private List <String> tags;
 	
 	private ShredRating shredRating;
 
 	private String shredType;
 	
 	public String getShredType() {
 		return shredType;
 	}
 	
 	public List<String> getTags() {
 		return tags;
 	}
 
 	public void setTags(List<String> tags) {
 		this.tags = tags;
 	}
	
 	public void setShredType(String shredType) {
 		this.shredType = shredType;
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}	
 
 	public String getVideoThumbnail() {
 		return videoThumbnail;
 	}
 
 	public void setVideoThumbnail(String videoThumbnail) {
 		this.videoThumbnail = videoThumbnail;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public Shredder getOwner() {
 		return owner;
 	}
 
 	public void setOwner(Shredder owner) {
 		this.owner = owner;
 	}
 
 	public Date getTimeCreated() {
 		return timeCreated;
 	}
 
 	public void setTimeCreated(Date timeCreated) {
 		this.timeCreated = timeCreated;
 	}
 
 	public String getVideoPath() {
 		return videoPath;
 	}
 
 	public void setVideoPath(String videoPath) {
 		this.videoPath = videoPath;
 	}
 	
 	public ShredRating getShredRating() {
 		return shredRating;
 	}
 	
 	public void setShredRating(ShredRating rating) {
 		this.shredRating = rating;		
 	}
 	
 	
 	
 
 	@Override
 	public String toString() {
 		return "Shred [id=" + id + ", description=" + description + ", owner="
 				+ owner + ", timeCreated=" + timeCreated + ", videoPath="
 				+ videoPath + ", videoThumbnail=" + videoThumbnail
 				+ ", shredComments=" + shredComments + ", tags=" + tags
 				+ ", shredRating=" + shredRating + ", shredType=" + shredType
 				+ "]";
 	}
 
 	public List<ShredComment> getShredComments() {
 		return shredComments;
 	}
 
 	public void setShredComments(List<ShredComment> shredComments) {
 		this.shredComments = shredComments;
 	}
 
 	
 	
 /*
 	public List<Tag> getTags() {
 		return tags;
 	}
 
 	public void setTags(List<Tag> tags) {
 		this.tags = tags;
 	}
 
 	@Override
 	public String toString() {
 		return "Shred [id=" + id + ", description=" + description + ", owner="
 				+ owner + ", timeCreated=" + timeCreated + ", videoPath="
 				+ videoPath + ", tags=" + tags + ", shredComments="
 				+ shredComments + ", rating=" + rating.getRating() + ", shredType="
 				+ shredType + "]";
 	}
 	*/
 	
 	
 
 }
