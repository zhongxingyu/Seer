 /** 
 * A contact is a person we are making business with or may do in the future. 
 * 
 * Contacts have an ID (unique), a name (probably unique, but maybe 
 * not), and notes that the user may want to save about them. 
 */
 public class ContactImpl implements Contact { 
 	private int id;
 	private String name;
 	private String notes = "";
 	private static int idAssigner = 0; //to facilitate assignment of an id
 	
 	public ContactImpl(String name) {
 		this.name = name;
 		id = idAssigner + 1; //first contact will have id 1, second 2 etc
 		incrementIdAssigner();
 	}
 	
 	private static void incrementIdAssigner() {
 		idAssigner++;
 	}
 	
	public int getId() {
 		return id;
 	}
 
	public String getName() {
 		return name;
 	}
 
 /** 
 * Returns our notes about the contact, if any. 
 * 
 * If we have not written anything about the contact, the empty 
 * string is returned. 
 * 
 * @return a string with notes about the contact, maybe empty. 
 */
	public String getNotes() {
 		return notes;
 	}
 
	public void addNotes(String note) {
 		notes = notes + note + " "; //so that notes String is not overwritten if addNotes is called more than once
 	}
 
 }
