 package ch9k.chat;
 
 import ch9k.chat.events.ContactOfflineEvent;
 import ch9k.chat.events.ContactOnlineEvent;
 import ch9k.configuration.Persistable;
 import ch9k.configuration.PersistentDataObject;
 import ch9k.eventpool.Event;
 import ch9k.eventpool.EventFilter;
 import ch9k.eventpool.EventListener;
 import ch9k.eventpool.EventPool;
 import java.net.InetAddress;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.SortedSet;
 import java.util.TreeSet;
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
     private SortedSet<Contact> contacts;
     
     /**
      * A hashmap because this will make lookup much easier
      * BEWARE: this hash will only contain online contacts
      */
     private HashMap<InetAddress,Contact> onlineHash;
 
     /**
      * Construct a new ContactList
      */
     public ContactList() {
         contacts = new TreeSet<Contact>();
         onlineHash = new HashMap<InetAddress,Contact>();
         init();
     }
     
     /**
      * Construct a new ContactList, and immediately restores it to a previous state
      * @param data Previously stored state of this object
      */
     public ContactList(PersistentDataObject data) {
         contacts = new TreeSet<Contact>();
         onlineHash = new HashMap<InetAddress,Contact>();
         load(data);
         init();
     }
 
     private void init() {
         EventPool.getAppPool().addListener(new ContactOnlineListener(),
                 new EventFilter(ContactOnlineEvent.class));
         EventPool.getAppPool().addListener(new ContactOfflineListener(),
                 new EventFilter(ContactOfflineEvent.class));
 
         for(Contact contact : contacts) {
             pingContact(contact);
         }
     }
 
     /**
      * Get a list of all the contacts from the current user
      * @return contacts
      */
     public SortedSet<Contact> getContacts() {
         return contacts;
     }
     
     private class ContactOnlineListener implements EventListener {
         public void handleEvent(Event ev) {
             ContactOnlineEvent event = (ContactOnlineEvent)ev;
             Contact contact = event.getContact();
             if(event.isExternal() && contact != null) {
                 /* when the contact wasn't online, we respond by saying we are online */
                 if (!contact.isOnline()) {
                     EventPool.getAppPool().raiseEvent(new ContactOnlineEvent(contact));
                 }
 
                 contact.setOnline(true);
                 onlineHash.put(event.getSource(), contact);
                 
                 fireListChanged();
             }
         }
     }
     
     private class ContactOfflineListener implements EventListener {
         public void handleEvent(Event ev) {
             ContactOfflineEvent event = (ContactOfflineEvent)ev;
             event.getContact().setOnline(false);
             onlineHash.remove(event.getSource());
         }
     }
     
     private void pingContact(Contact contact) {
         EventPool.getAppPool().raiseEvent(new ContactOnlineEvent(contact));
     }
     
     /**
      * Add a contact to the ContactList.
      * Note: no contact-request will be performed,
      * this should already have happened!
      * @param contact
      */
     public void addContact(Contact contact) {
         boolean success = contacts.add(contact);
         if(success) {
             contact.addChangeListener(this);
             fireListChanged();
             pingContact(contact);
         }
     }
 
     /**
      * Remove the given contact from the ContactList
      * @param contact
      */
     public void removeContact(Contact contact) {
         contacts.remove(contact);
        if(contact.isOnline()) {
            onlineHash.remove(contact.getIp());
        }
         fireListChanged();
     }
     
     /**
      * Debug method: remove all contacts
      */
     public void clear() {
         contacts.clear();
         fireListChanged();
     }
 
     /**
      * Get the contact that is know by this info, returns null if no such contact is found
     * will first look in it's online hash, since lookup in there is much easier and faster
     * if not found there, search in all the contacts
      * @param ip
      * @param username
      * @return contact
      */
     public Contact getContact(InetAddress ip, String username) {
         Contact contact = onlineHash.get(ip);
         if(contact != null && contact.getIp().equals(ip) && contact.getUsername().equals(username)){
             return contact;
         }
        
        for (Contact cont : contacts) {
            if(cont.getIp().equals(ip) && cont.getUsername().equals(username)) {
                return cont;
            }
        }
        
         return null;
     }
     
     private void fireListChanged() {
         fireContentsChanged(this, 0, contacts.size());
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
     public Object getElementAt(int j) {
         if(j >= contacts.size()) {
             return null;
         }
         
         Iterator<Contact> it = contacts.iterator();
         Contact contact = it.next();
         int i = 0;
         while(it.hasNext() && i < j) {
             contact = it.next();
             i++;
         }
         return contact;
     }
 
     @Override
     public void stateChanged(ChangeEvent changeEvent) {
         if(changeEvent.getSource() instanceof Contact) {
             Contact contact = (Contact)changeEvent.getSource();
             fireListChanged();
         }
     }
 }
