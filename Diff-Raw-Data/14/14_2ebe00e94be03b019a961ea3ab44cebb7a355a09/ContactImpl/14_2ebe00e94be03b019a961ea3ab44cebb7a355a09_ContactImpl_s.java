 public class ContactImpl implements Contact, Comparable<Contact> {
 	
 	private int id;
 	private String name;
 	private String note;
 	
 	public ContactImpl(int id, String name){
 		this.id = id;
 		this.name = name;
 	}
 	
 	/**
 	* Returns the ID of the contact.
 	*
 	* @return the ID of the contact.
 	*/
 	public int getId(){
 		return this.id;
 	}
 	
 	/**
 	* Returns the name of the contact.
 	*
 	* @return the name of the contact.
 	*/
 	public String getName(){
 		return this.name;
 	}
 	
 	/**
 	* Returns our notes about the contact, if any.
 	*
 	* If we have not written anything about the contact, the empty
 	* string is returned.
 	*
 	* @return a string with notes about the contact, maybe empty.
 	*/
 	public String getNotes(){
 		if(note == null){
 			note = "";
 		}
 		return note;
 	}
 	/**
 	* Add notes about the contact.
 	*
 	* @param note the notes to be added
 	*/
 	public void addNotes(String note){
 		this.note = note;
 	}
 	
	public int compareTo(<Contact> cn){
 		if(cn.getId() == this.getId()){
			return 0;
 		}else if(this.getId()< cn.getId()){
			return -1;
 		}else if(this.getId() > cn.getId()){
			return 1;
 		}
 	}
 			
 }
