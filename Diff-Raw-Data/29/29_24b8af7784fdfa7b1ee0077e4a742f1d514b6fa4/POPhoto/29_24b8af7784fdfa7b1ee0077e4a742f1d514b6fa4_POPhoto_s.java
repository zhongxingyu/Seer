 package info.berryworks.photoorder.model;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 
 /**
 * Model class which will store the Order Items
  * 
  * @author Darrell Dupas
  * 
  */
 @Entity
 public class POPhoto {
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private Long id;
 	private String photourl;
 	private String notes;
 	private String albumid;
 	
 	
 	public POPhoto(String photourl, String notes, String albumid) {
 		this.photourl = photourl;
 		this.notes = notes;
 		this.albumid = albumid;
 	}
 	
 	public String toString() {
 		String out = "<pophoto><id>" + this.id + "</id>" +
 		"<photourl>" + this.photourl + "</photourl>" +
 		"<notes>" + this.notes + "</notes>" +
 		"<albumid>" + this.albumid + "</albumid></pophoto>";
 		return out;
 	}
 	
 	public Long getId() {
 		return this.id;
 	}
 	public String getPhotourl() {
 		return this.photourl;
 	}
 
 	public void setNotes(String notes) {
 		this.notes = notes;
 	}
 	public void addNote(String note){
 		this.notes = this.notes + "<br>" + note;
 	}
 	
 	public String getNotes() {
 		return notes;
 	}
 	public String getAlbumid() {
 		return this.albumid;
 	}
 	public void setAblbumid(String aid) {
 		this.albumid = aid;
 	}
 	
 	
 }
