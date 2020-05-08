 package valtech.dbdiff.model;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 
 @Entity
 public class Bean {
 
 	@Id
 	private int id;
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 }
