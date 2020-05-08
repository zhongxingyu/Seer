 package cz.krajcovic.playersengine.base;
 
 import java.io.Serializable;
 
 public class Player implements Serializable {
 	/**
 	 * 
 	 */
	private static final long serialVersionUID = 356568155973140358L;

 	private int id;
 	private String secondName;
 	private String firstName;
 	private String description;
 
 	public void update(Player newPlayer) {
 
 		this.setId(newPlayer.getId());
 		this.setSecondName(newPlayer.getSecondName());
 		this.setFirstName(newPlayer.getFirstName());
 		this.setDescription(newPlayer.getDescription());
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public String getSecondName() {
 		return secondName;
 	}
 
 	public void setSecondName(String secondName) {
 		this.secondName = secondName;
 	}
 
 	public String getFirstName() {
 		return firstName;
 	}
 
 	public void setFirstName(String firstName) {
 		this.firstName = firstName;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	/**
 	 * Validate player values.
 	 * 
 	 * @return TRUE for OK.
 	 */
 	public Boolean validate() {
 		if (!secondName.matches("^[0-9A-Za-z\\.]{1,10}$")) {
 			return Boolean.FALSE;
 		}
 
 		if (!firstName.matches("^[0-9A-Za-z\\.]{1,10}$")) {
 			return Boolean.FALSE;
 		}
 
 		if (!description.matches("^[0-9A-Za-z\\.]{1,10}$")) {
 			return Boolean.FALSE;
 		}
 
 		return Boolean.TRUE;
 	}
 
 	@Override
 	public String toString() {
 		return secondName + " " + firstName + " " + description;
 	}
 
 }
