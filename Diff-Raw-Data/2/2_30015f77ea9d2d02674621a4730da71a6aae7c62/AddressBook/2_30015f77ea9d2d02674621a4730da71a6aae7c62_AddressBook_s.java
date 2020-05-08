 package Model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * This class contains a list of contact details 
  * @author Kelvin
  */
 public class AddressBook {
 	
 	private ArrayList<Contact> contacts;
 
 	/**
 	 * Default constructor for AddressBook Class
 	 */
 	public AddressBook() {
 		contacts = new ArrayList<Contact>();
 	}
 
 	/**
 	 * @return An ArrayList of all contacts
 	 */
 	public ArrayList<Contact> getContacts() {
 		return contacts;
 	}
 
 	/**
 	 * Adds a new contact to the Address Book
 	 * @param newContact The contact to be added
 	 * @return result of adding contact
 	 * @return true if contact added successfully, false if duplicated contact found
 	 */
 	public boolean addContact(Contact newContact) {
 		for(Contact c : contacts){
 			if(c.getFirstName().equals(newContact.getFirstName())
 					&& c.getLastName().equals(newContact.getLastName())
 					&& c.getEmail().equals(newContact.getEmail())){
 				return false;
 			}
 		}
 		contacts.add(newContact);
 		return true;
 	}
 
 	/**
 	 * @param contacts Contacts to add
 	 * @return true if successful
 	 */
 	public boolean addContacts(List<Contact> contacts) {
 		for(Contact c : contacts){
 			if (!this.addContact(c)) return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * @param firstName The first name of the contact to search for
 	 * @return All contacts with the given first name
 	 */
 	public ArrayList<Contact> searchContactsByFirstName(String firstName) {
 		return getContactsByName(firstName, true);
 	}
 
 	/**
 	 * @param lastName The last name of the contact to search for
 	 * @return All contacts with the given last name
 	 */
 	public ArrayList<Contact> searchContactsByLastName(String LastName) {
 		return getContactsByName(LastName, false);
 	}
 
 	/**
 	 * @param name Name to search for
 	 * @param first If true search by first name, else search by last name
 	 * @return
 	 */
 	private ArrayList<Contact> getContactsByName(String name, boolean first) {
 		ArrayList<Contact> result = new ArrayList<Contact>();
 
 		for (Contact c : contacts) {
 			if(c.getFirstName().equals(name) && first) {
 				result.add(c);
 			}
 			else if(c.getLastName().equals(name) && !first){
 				result.add(c);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Removes a contact from the Address book
 	 * @param name Name of contact to remove
 	 * @return Result of removal : true for successful false for failure
 	 */
 	public boolean removeContactByName(String name) {
 		for(Contact c : contacts){
 			if(c.getFullName().equals(name)){
 				contacts.remove(c);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/** Removes a contact from the Address book
 	 * @param email Email Address of contact to remove
 	 * @return Result of removal : true for successful false for failure
 	 */
 	public boolean removeContactByEmail(String email) {
 		for(Contact c : contacts){
 			if(c.getEmail().equals(email)){
 				contacts.remove(c);
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Saves the addressBook to an xml file on disk
 	 */
 	public void saveToXML(){
 		try {
 			@SuppressWarnings("unused")
			SaveToXMLFile xmlFile = new SaveToXMLFile(this.getContacts());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 	
 }
