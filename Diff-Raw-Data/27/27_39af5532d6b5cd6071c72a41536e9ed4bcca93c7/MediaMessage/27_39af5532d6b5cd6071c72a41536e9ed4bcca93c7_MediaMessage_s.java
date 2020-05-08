 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icemobile.samples.spring.mediacast;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 @XmlRootElement
 public class MediaMessage implements Serializable{
 
 	private static final Logger logger =
 			Logger.getLogger(MediaMessage.class.toString());
 
 	private String email;
 	private String title;
 	private String id;
 	private String description;
 	private List<String> tags = new ArrayList<String>(); 
 	private double latitude = 0.0;
 	private double longitude = 0.0;
 	private double altitude = 0.0;
 	private double direction = 0.0; //0-359 degrees
 	private String uploadMsg;
 	private Media photo = null;
 	private Media video = null;
 	private Media audio = null;
 	private Media smallPhoto = null;
     private Media largePhoto = null;
     private long created;
     private long lastVote;
 
 	public long getLastVote() {
 		return lastVote;
 	}
 
 	public void setLastVote(long lastVote) {
 		this.lastVote = lastVote;
 	}
 
 	private List<String> votes = new ArrayList<String>();
 
 	public static final String MEDIA_TYPE_PHOTO = "Photo";
 	public static final String MEDIA_TYPE_VIDEO = "Video";
 	public static final String MEDIA_TYPE_AUDIO = "Audio";
 
 	public void addPhoto(Media photo) {
 		this.photo = photo;
 	}
 
 	public void clear(){
 		audio = null;
 		description = null;
 		direction = 0;
 		email = null;
 		id = null;
 		latitude = 0;
 		longitude = 0;
 		photo = null;
 		tags = new ArrayList<String>();
 		title = null;
 		video = null;
 		lastVote = 0;
 		created = 0;
 		smallPhoto = null;
 		largePhoto = null;
 	}
 
 	public MediaMessage clone(){
 		MediaMessage cloned = new MediaMessage();
 		cloned.setAudio(audio);
 		cloned.setDescription(description);
 		cloned.setDirection(direction);
 		cloned.setEmail(email);
 		cloned.setId(id);
 		cloned.setLatitude(latitude);
 		cloned.setLongitude(longitude);
 		cloned.setPhoto(photo);
 		cloned.setSmallPhoto(smallPhoto);
 		cloned.setLargePhoto(largePhoto);
 		cloned.setTags(tags);
 		cloned.setTitle(title);
 		cloned.setVideo(video);
 		cloned.setCreated(created);
 		cloned.getVotes().addAll(votes);
 		cloned.setLastVote(lastVote);
 		cloned.setLargePhoto(largePhoto);
 		cloned.setSmallPhoto(smallPhoto);
 		return cloned;
 	}
 
 	/**
 	 * Clean up file resources.
 	 */
 	 public void dispose(){
 		if( photo != null ){
 				photo.dispose();
 		}
 		if( smallPhoto != null ){
 			smallPhoto.dispose();
 		}
 		if( largePhoto != null ){
 			largePhoto.dispose();
 		}
 			
 	 }
 
 	 public double getAltitude(){
 		 return altitude;
 	 }
 
 	 public void setAltitude(double alt){
 		 this.altitude = alt;
 	 }
 
 	 public Media getAudio()  {
 		 return audio;
 	 }
 
 	 public void setAudio(Media media){
 		 this.audio = media;
 	 }
 
 	 public String getDescription() {
 		 return description;
 	 }
 
 	 public double getDirection() {
 		 return direction;
 	 }
 
 	 @XmlElement
 	 public String getId() {
 		 return id;
 	 }
 
 	 public double getLatitude() {
 		 return latitude;
 	 }
 
 	 public double getLongitude() {
 		 return longitude;
 	 }
 
 	 public Media getPhoto() {
 		 return photo;
 	 }
 
 	 public boolean getShowAudio()  {
 		 return audio != null;
 	 }
 
 	 public boolean getShowPhoto()  {
 		 return photo != null;
 	 }
 
 	 public boolean getShowVideo()  {
 		 return video != null;
 	 }
 
 	 public List<String> getTags() {
 		 return tags;
 	 }
 
 	 public String getTitle() {
 		 return title;
 	 }
 
 	 public Media getVideo()  {
 		 return video;
 	 }
 
 	 public void setVideo(Media media){
 		 this.video = media;
 	 }
 
 	 public boolean isHasMedia(){
 		 return this.audio != null || this.photo != null || this.video != null;
 	 }
 
 	 public void setDescription(String description) {
 		 this.description = description;
 	 }
 
 	 public void setDirection(double direction) {
 		 this.direction = direction;
 	 }
 
 	 public void setId(String id) {
 		 this.id = id;
 	 }
 
 	 public void setLatitude(double latitude) {
 		 this.latitude = latitude;
 	 }
 
 	 public void setLongitude(double longitude) {
 		 this.longitude = longitude;
 	 }
 
 	 public void setPhoto(Media photo) {
 		 this.photo = photo;
 	 }
 
 	 public void setTags(List<String> tags){
 		 this.tags = tags;
 	 }
 
 	 public void setTitle(String title) {
 		 this.title = title;
 	 }
 
 	 public String getUploadMsg() {
 		 return uploadMsg;
 	 }
 
 	 public void setUploadMsg(String uploadMsg) {
 		 this.uploadMsg = uploadMsg;
 	 }
 
 	 @Override
 	public String toString() {
 		return "MediaMessage [email=" + email + ", title=" + title + ", id="
 				+ id + ", description=" + description + ", tags=" + tags
 				+ ", latitude=" + latitude + ", longitude=" + longitude
 				+ ", altitude=" + altitude + ", direction=" + direction
 				+ ", uploadMsg=" + uploadMsg + ", photo=" + photo + ", video="
 				+ video + ", audio=" + audio + ", smallPhoto=" + smallPhoto
 				+ ", largePhoto=" + largePhoto + ", created=" + created
 				+ ", lastVote=" + lastVote + ", votes=" + votes + "]";
 	}
 
 	@XmlElement
 	 public String getEmail() {
 		 return email;
 	 }
 
 	 public void setEmail(String email) {
 		 this.email = email;
 	 }
 
 	 @XmlElement
 	 public List<String> getVotes(){
 		 return votes;
 	 }
 
 	 public long getNumberOfVotes(){
 		 return votes.size();
 	 }
 
 	 public long getCreated() {
 		 return created;
 	 }
 
 	 public void setCreated(long created) {
 		 this.created = created;
 	 }
 
 	public Media getSmallPhoto() {
 		return smallPhoto;
 	}
 
 	public void setSmallPhoto(Media smallPhoto) {
 		this.smallPhoto = smallPhoto;
 	}
 
 	public Media getLargePhoto() {
 		return largePhoto;
 	}
 
 	public void setLargePhoto(Media largePhoto) {
 		this.largePhoto = largePhoto;
 	}
 
 	public String getVotesAsString(){
 		StringBuilder str = new StringBuilder();
 		for( String vote : votes ){
 			str.append(vote);
 			str.append(" ");
 		}
 		return str.toString();
 	}
 
 
 
 }
