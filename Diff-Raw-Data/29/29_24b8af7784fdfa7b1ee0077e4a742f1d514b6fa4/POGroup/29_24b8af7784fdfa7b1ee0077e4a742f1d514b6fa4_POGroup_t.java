 package info.berryworks.photoorder.model;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 
 /**
 * Model class which will store the Group Items
  * 
  * @author Darrell Dupas
  * 
  */
 @Entity
 public class POGroup {
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private Long id;
 	private String name;
 	private String notes;
 
 	public POGroup(String name, String notes) {
 		this.name = name;
 		this.notes = notes;
 	
 	}
 	
 	public String toString() {
 		String out  = "<pogroup><id>"+ this.id + "</id>" +
 		"<name>" + this.name + "</name>" + 
 		"<notes>" + this.notes + "</notes></pogroup>";
 		return out;
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
