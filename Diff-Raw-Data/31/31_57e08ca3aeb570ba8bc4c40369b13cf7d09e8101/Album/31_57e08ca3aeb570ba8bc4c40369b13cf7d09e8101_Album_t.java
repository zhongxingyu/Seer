 package edu.asu.krypton.model.persist.db;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 
import org.springframework.data.annotation.Id;
 import org.springframework.data.mongodb.core.mapping.DBRef;
 import org.springframework.data.mongodb.core.mapping.Document;
 /**
  * 
  * @author Nadeem
  *
  */
 @Document
 public class Album {
 	@Id
 	private String id;
 	
 	private String title;
 	
 	@DBRef
 	private Collection<Photo> photos = new ArrayList<Photo>();
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public Collection<Photo> getPhotos() {
 		return photos;
 	}
 
 	public void setPhotos(Collection<Photo> photos) {
 		this.photos = photos;
 	}
 }
