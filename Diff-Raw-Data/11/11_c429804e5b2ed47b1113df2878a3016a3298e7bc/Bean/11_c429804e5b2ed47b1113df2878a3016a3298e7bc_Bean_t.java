 package valtech.dbdiff.model;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 
 @Entity
 public class Bean {
 
 	@Id
 	private int id;
 
	private String value;

 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
 }
