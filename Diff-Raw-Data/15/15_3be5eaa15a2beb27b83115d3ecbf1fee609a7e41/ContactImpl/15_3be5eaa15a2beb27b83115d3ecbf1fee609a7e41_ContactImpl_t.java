 
 public class ContactImpl implements Contact, Comparable<ContactImpl>{
 	
 	private int id;
 	private String name;
 	private String notes;
 	
 	public ContactImpl(int id, String name) {
 		this.id = id;
 		this.name = new String(name);
 		this.notes = "";		
 	}
 
 	@Override
 	public int getId() {
 		return id;
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public String getNotes() {
 		return notes;
 	}
 
 	@Override
 	public void addNotes(String note) {
 		if(notes.length() == 0){
 			notes += note;
 		} else {
 			notes += "\n" + note;
 		}
 	}
 
 	@Override
 	public int compareTo(ContactImpl contact2) {
 		if(this.getId() == contact2.getId()) {
			return 0;  								// Check that this is right - i.e. that contacts are uniquely 
 		}											// defined by Id, and that 1 means equal, 0 not
		if(this.getId() < contact2.getId()) {
			return -1;
 		}
		return 1;
 	}
 }

