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
 public class POAlbum {
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private Long id;
 	private String name;
 	private String notes;
 	private String groupid;
 	
 	public POAlbum(String name, String notes, String groupid) {
 		this.name = name;
 		this.notes = notes;
 		this.groupid = groupid;
 	}
 	
 	public String toString(){
 		String out = "<poalbum><id>" + this.id + "</id>" +
 		"<name>" + this.name + "</name>" + 
 		"<notes>" + this.notes + "</notes>" + 
 		"<groupid>" + this.groupid + "</groupid></poalbum>";
 		return out;
 	}
 	
 	public String getGroupid() {
 		return this.groupid;
 	}
 
 	public void setGroupid(String gid) {
 		this.groupid = gid;
 	}
 
 	public Long getId() {
 		return this.id;
 	}
 	public String getName() {
 		return this.name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public String getNotes() {
 		return notes;
 	}
 
 	public void setNotes(String notes){
 		this.notes = notes;
 	}
 	public void addNote(String note){
 		this.notes = this.notes + "<br>" + note;
 	}
 }
