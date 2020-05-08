 package com.britesnow.samplesocial.service;
 
 import com.britesnow.samplesocial.entity.SocialIdEntity;
 import com.britesnow.samplesocial.entity.User;
 import com.google.gdata.client.contacts.ContactQuery;
 import com.google.gdata.client.contacts.ContactsService;
 import com.google.gdata.data.PlainTextConstruct;
 import com.google.gdata.data.contacts.ContactEntry;
 import com.google.gdata.data.contacts.ContactFeed;
 import com.google.gdata.data.contacts.ContactGroupEntry;
 import com.google.gdata.data.contacts.ContactGroupFeed;
 import com.google.gdata.util.ServiceException;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.List;
 
 @Singleton
 public class GContactService {
     @Inject
     GoogleAuthService authService;
 
     private final String BASE_CONTACTS_URL = "https://www.google.com/m8/feeds/contacts/default/full";
     private final String BASE_GROUP_URL = "https://www.google.com/m8/feeds/groups/default/full";
 
     public ContactGroupEntry createContactGroupEntry(User user, String name) throws Exception {
         ContactGroupEntry group = new ContactGroupEntry();
         group.setTitle(new PlainTextConstruct(name));
         URL postUrl = new URL(BASE_GROUP_URL);
         return getContactsService(user).insert(postUrl, group);
     }
 
     public ContactGroupEntry getContactGroupEntry(User user,String groupId) throws IOException, ServiceException {
         URL url = new URL(BASE_GROUP_URL + "/" + groupId);
         return getContactsService(user).getEntry(url, ContactGroupEntry.class);
     }
     public ContactEntry getContactEntry(User user,String contactId) throws IOException, ServiceException {
         URL url = new URL(BASE_CONTACTS_URL + "/" + contactId);
         return getContactsService(user).getEntry(url, ContactEntry.class);
     }
 
 
    public List<ContactEntry> getGroupContactResults(User user,String groupId, int startIndex, int count) throws ServiceException, IOException {
         URL feedUrl = new URL(BASE_CONTACTS_URL);
         ContactQuery myQuery = new ContactQuery(feedUrl);
        myQuery.setStringCustomParameter("group", groupId);
         myQuery.setStartIndex(startIndex);
         myQuery.setMaxResults(count);
 //       myQuery.setGroup(String.format(BASE_GROUP_URL + "/" + groupId).replace("full","base"));
 //        myQuery.setGroup("https://www.google.com/m8/feeds/groups/woofgl%40gmail.com/base/6");
         ContactFeed resultFeed = getContactsService(user).query(myQuery, ContactFeed.class);
         return resultFeed.getEntries();
     }
 
    public List<ContactEntry> getContactResults(User user) throws IOException, ServiceException {
        URL feedUrl = new URL(BASE_CONTACTS_URL);
        ContactFeed resultFeed = getContactsService(user).getFeed(feedUrl, ContactFeed.class);
        return resultFeed.getEntries();
    }
 
     public List<ContactGroupEntry> getGroupResults(User user) throws IOException, ServiceException {
         URL feedurUrl = new URL(BASE_GROUP_URL);
         ContactGroupFeed contactGroupFeed = getContactsService(user).getFeed(feedurUrl, ContactGroupFeed.class);
         return contactGroupFeed.getEntries();
     }
 
 
 
     public ContactEntry createContact(User user,ContactInfo contact)
             throws ServiceException, IOException {
         ContactEntry contactEntry = contact.to();
         //Add process
         URL postUrl = new URL(BASE_CONTACTS_URL);
         return getContactsService(user).insert(postUrl, contactEntry);
 
     }
 
 
     public void deleteGroup(User user,String groupId, String etag) throws IOException, ServiceException {
         String url = String.format("%s/%s", BASE_GROUP_URL, groupId);
         getContactsService(user).delete(new URL(url), etag);
     }
     public void deleteContact(User user,String contactId, String etag) throws IOException, ServiceException {
         String url = String.format("%s/%s", BASE_CONTACTS_URL, contactId);
         getContactsService(user).delete(new URL(url), etag);
     }
 
     public void updateContactGroupEntry(User user,String groupId, String etag, String groupName) throws IOException, ServiceException {
         String url = String.format("%s/%s", BASE_GROUP_URL, groupId);
         ContactGroupEntry group = new ContactGroupEntry();
         group.setTitle(new PlainTextConstruct(groupName));
         getContactsService(user).update(new URL(url), group, etag);
     }
 
     public void updateContactEntry(User user,ContactInfo contact) throws IOException, ServiceException {
         String url = String.format("%s/%s", BASE_CONTACTS_URL, contact.getId());
         getContactsService(user).update(new URL(url), contact.to());
     }
 
     private ContactsService getContactsService(User user) {
         SocialIdEntity social = authService.getSocialIdEntity(user.getId());
         if (social != null && social.isValid()) {
             ContactsService service = new ContactsService("Contacts Sample");
             service.setHeader("Authorization", "Bearer " + social.getToken());
             return service;
         }
         return null;
     }
 }
