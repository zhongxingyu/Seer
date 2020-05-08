 package com.tuvistavie.meetup.contact.model;
 
 import com.tuvistavie.meetup.App;
 import com.tuvistavie.meetup.contact.util.ContactHelper;
 import com.tuvistavie.meetup.model.AbstractCollection;
 import com.tuvistavie.meetup.util.Routes;
 
 /**
  * Created by daniel on 9/4/13.
  */
 public class ContactList extends AbstractCollection<Contact> {
     private static ContactList phoneBookContent;
 
     @Override
     protected Class<?> getEntityClass() {
        return Contact.class;
     }
 
     @Override
     protected String getURI() {
         return Routes.CONTACTS.getRoute();
     }
 
     public void loadFromPhoneBook() {
         if(phoneBookContent == null) {
             phoneBookContent = new ContactList();
             phoneBookContent.setEntities(ContactHelper.getContactList(App.getContext()));
         }
         this.setEntities(phoneBookContent.getEntities());
         for(Contact contact: getEntities()) {
             contact.setSelected(false);
         }
     }
 
 }
