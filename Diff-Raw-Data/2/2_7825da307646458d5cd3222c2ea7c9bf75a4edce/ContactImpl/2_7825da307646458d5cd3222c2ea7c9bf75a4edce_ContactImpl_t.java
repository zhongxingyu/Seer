 public class ContactImpl implements Contact {
 	private int id;
 	private String name;
 	private String notes;
 	
 	public ContactImpl(int id, String name, String notes) {
 		this.id = id;
 		this.name = name;
 		this.notes = notes + "\n";
 	}
 	public ContactImpl(int id, String name) {
 		this(id, name, "");
 	}
 	public ContactImpl(Contact contact) {
 		this(contact.getId(), contact.getName(), contact.getNotes());
 	}
 	public int getId() {
 		return id;
 	}
 	public String getName() {
 		return name;
 	}
 	public String getNotes() {
 		return notes;
 	}
 	public void addNotes(String note) {
 		notes += note + "\n";
 	}
 	public boolean equals(Contact otherContact) {
 		boolean equal = false;
 		equal = getId() == otherContact.getId();
 		equal = equal && getName().equals(otherContact.getName());
 		equal = equal && getNotes().equals(otherContact.getNotes());
		return equal;
 	}		
 }
