 package ch9k.chat;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 /**
  * List of al the contacts of the current user.
  * @author Jens Panneel
  */
 public class ContactList {
     private List<Contact> contacts;
 
     /**
      * Constructor
      * @param contacts The set of all the by configuration known contacts
      *
      * contacts is a Set because you dont want to save the same contact two times, and in saving/loading there is no need for an ordening.
      * In the ContactList there is need for an ordening. This could be done here.
      */
     public ContactList(Set<Contact> contacts) {
         // TODO some sort of sorting/ordening
         this.contacts = new ArrayList<Contact>(contacts);
     }
 
     /**
      * Get a list of all the contacts from the current user
      * @return contacts
      */
     public List<Contact> getContacts() {
         return contacts;
     }
 
    //throws contact allready in list exception or returns a boolean
     /**
      * Add a contact to the ContactList.
      * @param contact
      * @throws Exception
      *
      */
     public void addContact(Contact contact) throws Exception {
         if (!contacts.contains(contact)){
             contacts.add(contact);
         } else {
             // TODO throw the right exception
            throw new Exception("Contact allready in List");
         }
     }
 
     //throws contact not in list exception or returns a boolean or just dont check. Will only be called on excisting contacts...
     /**
      * Remove the given contact from the ContactList
      * @param contact
      */
     public void removeContact(Contact contact) {
         contacts.remove(contact);
     }
 
 }
