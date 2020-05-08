 package org.mdissjava.mdisscore.model.pojo;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.bson.types.ObjectId;
 
 import com.google.code.morphia.annotations.Embedded;
 import com.google.code.morphia.annotations.Entity;
 import com.google.code.morphia.annotations.Id;
 import com.google.code.morphia.annotations.Reference;
 
 /**
  * Represents a photo took by a user.
  * 
  * @author mdiss_java
  */
 @Entity
 public class Photo {
 
 	/** The unique id. */
 	@Id private ObjectId id;
 
 	/** The photo id (normally the same as data ID) */
 	private String photoId;
 	
 	/** The title of the photo. */
 	private String title;
 
 	/** the reference to the album */
 	@Reference(lazy = true) private Album album;
 	
 	/** Represents if the photo is public, can be seen by anyone, or private. */
 	private Boolean publicPhoto;
 
 	/** The list of all voted the photo has received. */
 	@Embedded private List<Vote> votes;
 
 	/** The date when the photo was uploaded. */
 	private Date uploadDate;
 
 	/**
 	 * The next photo in the sequence, used to short the photos in the users
 	 * gallery.
 	 */
 	@Reference private Photo nextPhoto;
 
 	/**
 	 * The previous photo in the sequence, used to short the photos in the users
 	 * gallery.
 	 */
 	@Reference private Photo backwardPhoto;
 
 	/** The metadata. */
 	@Embedded private Metadata metadata;
 
 	/** The list of tags associated to the photo . */
 	private List<String> tags;
 
 	/** The id of the photo's data in the database. */
 	private String dataId;
 
 	/** Represents if the photo is only for adults or not. */
 	private Boolean plus18;
 
 	public Photo() {
 		tags = new ArrayList<String>();
 		votes = new ArrayList<Vote>();
 	}
 
 	/**
 	 * Gets the id.
 	 * 
 	 * @return the id
 	 */
 	public ObjectId getId() {
 		return this.id;
 	}
 
 	/**
 	 * Sets the id.
 	 * 
 	 * @param id
 	 *            the new id
 	 */
 	public void setId(ObjectId id) {
 		this.id = id;
 	}
 	
 	/**
 	 * Gets the photo ID
 	 * 
 	 * @return
 	 */
 	public String getPhotoId() {
 		return photoId;
 	}
 
 	/**
 	 * Sets the photoId
 	 * 
 	 * @param photoId
 	 */
 	public void setPhotoId(String photoId) {
 		this.photoId = photoId;
 	}
 
 	/**
 	 * Gets the title.
 	 * 
 	 * @return the title
 	 */
 	public String getTitle() {
 		return this.title;
 	}
 
 	/**
 	 * Sets the title.
 	 * 
 	 * @param title
 	 *            the new title
 	 */
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	
 	/**
 	 * gets the album (we use lazyness so we don't have until we call some of this methods)
 	 * @return
 	 */
 	public Album getAlbum() {
 		return album;
 	}
 
 	/**
 	 * Sets the album reference
 	 * 
 	 * @param album
 	 */
 	public void setAlbum(Album album) {
 		this.album = album;
 	}
 
 	/**
 	 * Gets the public photo.
 	 * 
 	 * @return the public photo
 	 */
 	public Boolean getPublicPhoto() {
 		return this.publicPhoto;
 	}
 
 	/**
 	 * Sets the public photo.
 	 * 
 	 * @param publicPhoto
 	 *            the new public photo
 	 */
 	public void setPublicPhoto(Boolean publicPhoto) {
 		this.publicPhoto = publicPhoto;
 	}
 
 	/**
 	 * Gets the vote.
 	 * 
 	 * @return the vote
 	 */
 	public List<Vote> getVotes() {
 		return this.votes;
 	}
 
 	/**
 	 * Sets the vote.
 	 * 
 	 * @param votes
 	 *            the new vote
 	 */
 	public void setVote(List<Vote> votes) {
 		this.votes = votes;
 	}
 
 	/**
 	 * Gets the upload date.
 	 * 
 	 * @return the upload date
 	 */
 	public Date getUploadDate() {
 		return this.uploadDate;
 	}
 
 	/**
 	 * Sets the upload date.
 	 * 
 	 * @param uploadDate
 	 *            the new upload date
 	 */
 	public void setUploadDate(Date uploadDate) {
 		this.uploadDate = uploadDate;
 	}
 
 	/**
 	 * Gets the next photo.
 	 * 
 	 * @return the next photo
 	 */
 	public Photo getNextPhoto() {
 		return this.nextPhoto;
 	}
 
 	/**
 	 * Sets the next photo.
 	 * 
 	 * @param nextPhoto
 	 *            the new next photo
 	 */
 	public void setNextPhoto(Photo nextPhoto) {
 		this.nextPhoto = nextPhoto;
 	}
 
 	/**
 	 * Gets the backward photo.
 	 * 
 	 * @return the backward photo
 	 */
 	public Photo getBackwardPhoto() {
 		return this.backwardPhoto;
 	}
 
 	/**
 	 * Sets the backward photo.
 	 * 
 	 * @param backwardPhoto
 	 *            the new backward photo
 	 */
 	public void setBackwardPhoto(Photo backwardPhoto) {
 		this.backwardPhoto = backwardPhoto;
 	}
 
 	/**
 	 * Gets the metadata.
 	 * 
 	 * @return the metadata
 	 */
 	public Metadata getMetadata() {
 		return this.metadata;
 	}
 
 	/**
 	 * Sets the metadata.
 	 * 
 	 * @param metadata
 	 *            the new metadata
 	 */
 	public void setMetadata(Metadata metadata) {
 		this.metadata = metadata;
 	}
 
 	/**
 	 * Gets the tags.
 	 * 
 	 * @return the tags
 	 */
 	public List<String> getTags() {
 		return this.tags;
 	}
 
 	/**
 	 * Sets the tags.
 	 * 
 	 * @param tags
 	 *            the new tags
 	 */
 	public void setTags(List<String> tags) {
 		this.tags = tags;
 	}
 
 	/**
 	 * Gets the data id.
 	 * 
 	 * @return the data id
 	 */
 	public String getDataId() {
 		return this.dataId;
 	}
 
 	/**
 	 * Sets the data id.
 	 * 
 	 * @param dataId
 	 *            the new data id
 	 */
 	public void setDataId(String dataId) {
 		this.dataId = dataId;
 	}
 
 	/**
 	 * Gets the plus18.
 	 * 
 	 * @return the plus18
 	 */
 	public Boolean getPlus18() {
 		return this.plus18;
 	}
 
 	/**
 	 * Sets the plus18.
 	 * 
 	 * @param plus18
 	 *            the new plus18
 	 */
 	public void setPlus18(Boolean plus18) {
 		this.plus18 = plus18;
 	}
 
 }
