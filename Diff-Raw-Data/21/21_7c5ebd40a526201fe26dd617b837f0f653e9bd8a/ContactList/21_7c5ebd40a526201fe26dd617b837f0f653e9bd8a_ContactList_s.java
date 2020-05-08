 package ch9k.chat;
 
 import ch9k.configuration.Persistable;
 import ch9k.configuration.PersistentDataObject;
 import ch9k.eventpool.EventListener;
 import ch9k.eventpool.Event;
 import ch9k.eventpool.EventFilter;
 import ch9k.eventpool.EventPool;
 import ch9k.chat.events.ContactOnlineEvent;
 import ch9k.chat.events.ContactOfflineEvent;
 import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import javax.swing.AbstractListModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import org.jdom.Element;
 
 /**
  * List of al the contacts of the current user.
  * @author Jens Panneel
  */
 public class ContactList extends AbstractListModel implements Persistable, ChangeListener {
     /**
      * Collection of contacts, a set because you dont want to save
      * the same contact two times.
      */
     private List<Contact> contacts;
 
     /**
      * Constructor
      */
     public ContactList() {
         contacts = new ArrayList<Contact>();
         init();
     }
     
     /**
      * Creates a new object, and immediately restores it to a previous state
      * 
      * @param data Previously stored state of this object
      */
     public ContactList(PersistentDataObject data) {
         contacts = new ArrayList<Contact>();
         load(data);
         init();
     }
 
     private void init() {
         pingContacts();
 
         EventPool.getAppPool().addListener(new ContactOnlineListener(),
                 new EventFilter(ContactOnlineEvent.class));
         EventPool.getAppPool().addListener(new ContactOfflineListener(),
                 new EventFilter(ContactOfflineEvent.class));
     }
 
     /**
      * Get a list of all the contacts from the current user
      * @return contacts
      */
     public List<Contact> getContacts() {
         return contacts;
     }
     
     private void pingContacts() {
         for (int i = 0; i < contacts.size(); i++ ) {
            EventPool.getAppPool().raiseEvent(new ContactOnlineEvent(contacts.get(i)));
         }
     }
     
     private class ContactOnlineListener implements EventListener {
         public void handleEvent(Event ev) {
             ContactOnlineEvent event = (ContactOnlineEvent)ev;
            /* when the contact wasn't online, we respond by saying we are online*/
            if (! event.getContact().isOnline()) {
                EventPool.getAppPool().raiseEvent(new ContactOnlineEvent(event.getContact()));
             }
            event.getContact().setOnline(true);
         }
     }
     
     private class ContactOfflineListener implements EventListener {
         public void handleEvent(Event ev) {
             ContactOfflineEvent event = (ContactOfflineEvent)ev;
             event.getContact().setOnline(false);
         }
     }
     
     /**
      * Add a contact to the ContactList.
      * @param contact
      * @return added
      */
     public boolean addContact(Contact contact) {
         // maybe isert it in a cool order
         if(contacts.contains(contact)){
             return false;
         } else {
             contact.addChangeListener(this);
             contacts.add(contact);
             int i = contacts.size() - 1;
             fireIntervalAdded(this, i, i);
             return true;
         }
     }
 
     /**
      * Remove the given contact from the ContactList
      * @param contact
      * @return removed
      */
     public boolean removeContact(Contact contact) {
         int i = contacts.indexOf(contact);
         if(i < 0) {
             return false;
         } else {
             contacts.remove(i);
             fireIntervalRemoved(this, i, i);
             return true;
         }
     }
 
     /**
      * Debug method: remove all contacts
      */
     public void clear() {
         contacts.clear();
         fireIntervalRemoved(this, 0, 0);
     }
 
     /**
      * Get the contact that is know by this info, returns null if no such contact is found
      * @param ip
      * @param username
      * @return contact
      */
     public Contact getContact(InetAddress ip, String username) {
         Iterator<Contact> it = contacts.iterator();
         Contact contact;
         while(it.hasNext()){
             contact = it.next();
             if(contact.getIp().equals(ip) && contact.getUsername().equals(username)){
                 return contact;
             }
         }
         return null;
     }
 
     @Override
     public PersistentDataObject persist() {
         Element contactlist = new Element("contactlist");
         for(Contact contact : contacts) {
             contactlist.addContent(contact.persist().getElement());
         }
         return new PersistentDataObject(contactlist);
     }
 
     @Override
     public void load(PersistentDataObject object) {
         for (Object obj : object.getElement().getChildren()) {
             Element child = (Element) obj;
             Contact contact = new Contact(new PersistentDataObject(child));
             addContact(contact);
         }
     }
 
     @Override
     public int getSize() {
         return contacts.size();
     }
 
     @Override
     public Object getElementAt(int n) {
         return contacts.get(n);
     }
 
     @Override
     public void stateChanged(ChangeEvent changeEvent) {
         if(changeEvent.getSource() instanceof Contact) {
             Contact contact = (Contact)changeEvent.getSource();
             int index = contacts.indexOf(contact);
             fireContentsChanged(this, index, index);
         }
     }
 }
