 package ch9k.chat;
 
 import ch9k.chat.events.ContactOfflineEvent;
 import ch9k.chat.events.ContactOnlineEvent;
 import ch9k.chat.events.ContactRequestEvent;
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
 import javax.swing.JOptionPane;
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
         EventPool.getAppPool().addListener(new ContactRequestListener(),
                 new EventFilter(ContactRequestEvent.class));
     }
 
     /**
      * Get a list of all the contacts from the current user
      * @return contacts
      */
     public SortedSet<Contact> getContacts() {
         return contacts;
     }
     
     private class ContactOnlineListener implements EventListener {
         @Override
         public void handleEvent(Event event) {
             ContactOnlineEvent onlineEvent = (ContactOnlineEvent)event;
             Contact contact = onlineEvent.getContact();
             /* all this has to be true, otherwise we just have to ignore */
             if(onlineEvent.isExternal() &&
                     contact != null &&
                     !contact.isIgnored() &&
                     !contact.isBlocked()) {
                 EventPool.getAppPool().raiseEvent(new ContactOnlineEvent(contact));
                 /* keep in mind here that this will set the state to online
                  * so in essence this will also handle responses 
                  * from friendrequests 
                  */
                 contact.setOnline(true);
                 onlineHash.put(onlineEvent.getSource(), contact);
 
                 fireListChanged();    
             }
         }
     }
     
     private class ContactOfflineListener implements EventListener {
         @Override
         public void handleEvent(Event event) {
             ContactOfflineEvent offlineEvent = (ContactOfflineEvent)event;
             Contact contact = offlineEvent.getContact();
             /* all this has to be true, otherwise we just have to ignore */
             if(offlineEvent.isExternal() && contact != null ) {
                 contact.setOnline(false);
                 onlineHash.remove(offlineEvent.getSource());
             }
         }
     }
     
     private class ContactRequestListener implements EventListener {
         @Override
         public void handleEvent(Event ev) {
             ContactRequestEvent event = (ContactRequestEvent)ev;
            if(event.isExternal()) {
                String text = "would you like to add " + event.getUsername()
                         + " as your friend?";
                 int confirmation = JOptionPane.showConfirmDialog(
                         null, text, "Friend Request", JOptionPane.YES_NO_OPTION);
 
                Contact contact = new Contact(event.getUsername(),event.getSource());
                 if (confirmation != JOptionPane.YES_OPTION) {
                     contact.setIgnored();
                 }
                 addContact(contact);
             }
         }
     }
     
     private void pingContact(Contact contact) {
         if (contact.isRequested()) {
             EventPool.getAppPool().raiseEvent(new ContactRequestEvent(
                     contact.getIp(), contact.getUsername()));
         } else {
             EventPool.getAppPool().raiseEvent(new ContactOnlineEvent(contact));            
         }
 
     }
     
     /**
      * Add a contact to the ContactList.
      * Note: no contact-request will be performed,
      * this should already have happened!
      * @param contact
      */
     public void addContact(Contact contact, boolean sendRequest) {
         boolean success = contacts.add(contact);
         if(sendRequest) {
             contact.setRequested();
         }
         if(success) {
             contact.addChangeListener(this);
             fireListChanged();
             pingContact(contact);
         }
     }
     
     public void addContact(Contact contact) {
         addContact(contact, false);
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
         if(contact != null && 
                 contact.getIp().equals(ip) &&
                 contact.getUsername().equals(username)){
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
