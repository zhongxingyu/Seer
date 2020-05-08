 package models;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import views.formdata.ContactFormData;
 
 /**
  * Provides in-memory repository for the Contact data.
  * 
  * @author Jonathan Ortal
  * 
  */
 
 public class ContactDB {
 
   private static Map<String, Map<Long, Contact>> contacts = new HashMap<String, Map<Long, Contact>>();
 
   /**
    * Updates the "database" if id = 0, else updates an old entry.
    * @param user The user
    * @param formData The contact data
    * @return The created contact
    */
   public static Contact addContact(String user, ContactFormData formData) {
     if (!isUser(user)) {
       contacts.put(user, new HashMap<Long, Contact>());
     }
    long idVal = (formData.id == 0) ? contacts.get(user).size() + 1 : formData.id;
    Contact contact =
        new Contact(idVal, formData.firstName, formData.lastName, formData.telephone, formData.telephoneType);          
     contacts.get(user).put(idVal, contact);
     return contact;
   }
 
   public static boolean isUser(String user) {
     return contacts.containsKey(user);
   }
   
   /**
    * Returns the list of contacts.
    * @param user The user
    * @return A list of contacts.
    */
   public static List<Contact> getContacts(String user) {
     return new ArrayList<>(contacts.get(user).values());
   }
 
   /**
    * Returns Contact with associated ID.
    * 
    * @param id The ID.
    * @return The retrieved ID.
    */
   public static Contact getContact(String user, long id) {
     Contact contact = contacts.get(user).get(id);
     if (!isUser(user)) {
       throw new RuntimeException("Invalid user: " + user);
     }
     if (contact == null) {
       throw new RuntimeException("Invalid ID: " + id);
     }
     return contact;
   }
 }
