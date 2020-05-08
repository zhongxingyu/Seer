 package cz.vutbr.fit.gja.gjaddr.persistancelayer;
 
 import java.io.Serializable;
 
 /**
  * One group from database representation.
  *
  * @author Bc. Radek Gajdušek <xgajdu07@stud.fit.vutbr.cz>
  */
 public class Group  implements Serializable {
 
 	static private final long serialVersionUID = 6L;
 
 	private int id;
 	private String name;
 
 	public int getId() {
 		return id;
 	}
 
 	public String getName() {
 		return this.name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
	}
 
 	public Group (int id, String name) {
 		this.id = id;
 		this.name = name;
 	}
 
 	public boolean equals(Object obj) {
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		final Group other = (Group) obj;
 		if (this.id != other.id) {
 			return false;
 		}
 		if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public String toString() {
		return name;
 	}
 }
