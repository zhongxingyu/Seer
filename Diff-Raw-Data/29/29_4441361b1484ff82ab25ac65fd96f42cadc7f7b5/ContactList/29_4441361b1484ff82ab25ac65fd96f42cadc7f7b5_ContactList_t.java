 package com.tuvistavie.meetup.contact.model;
 
 import android.os.AsyncTask;
 
 import com.tuvistavie.meetup.App;
 import com.tuvistavie.meetup.contact.util.ContactHelper;
 import com.tuvistavie.meetup.model.AbstractCollection;
 import com.tuvistavie.meetup.util.Routes;
 
 import org.json.JSONArray;
 
 import java.util.List;
 
 /**
  * Created by daniel on 9/4/13.
  */
 public class ContactList extends AbstractCollection<Contact> {
     private static ContactList phoneBookContent;
 
     protected String uri;
 
     private boolean loaded;
 
     public ContactList() {
         this.uri = Routes.CONTACTS.getRoute();
     }
 
 
     public ContactList(JSONArray jsonArray) {
         super(jsonArray);
     }
 
     @Override
     protected Class<?> getEntityClass() {
         return Contact.class;
     }
 
 
     @Override
     protected String getURI() {
         return uri;
     }
 
     public void setUri(String uri) {
         this.uri = uri;
     }
 
     public void loadFromPhoneBook() {
         if(phoneBookContent == null) {
             phoneBookContent = new ContactList();
             //phoneBookContent.setEntities(ContactHelper.getContactList(App.getContext()));
             new LoadFromPhoneBookTask().execute();
         }
         this.setEntities(phoneBookContent.getEntities());
     }
 
     public boolean isLoaded() {
         return loaded;
     }
 
     private class LoadFromPhoneBookTask extends AsyncTask<Void, Void, List<Contact>> {
         @Override
         protected List<Contact> doInBackground(Void... params) {
             return ContactHelper.getContactList(App.getContext());
         }
 
         @Override
         protected void onPostExecute(List<Contact> contacts) {
             phoneBookContent.setEntities(contacts);
             setEntities(contacts);
            runOnUpdate();
             loaded = true;
         }
     }
 
 }
