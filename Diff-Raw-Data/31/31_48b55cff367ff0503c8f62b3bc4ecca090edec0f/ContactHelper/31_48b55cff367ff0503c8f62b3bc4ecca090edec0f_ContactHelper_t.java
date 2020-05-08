 package com.tuvistavie.meetup.contact.util;
 
 import android.content.ContentResolver;
 import android.content.Context;
 import android.database.Cursor;
 import android.provider.ContactsContract;
 
 import com.tuvistavie.meetup.contact.model.Contact;
 
 import java.util.ArrayList;
import java.util.Collections;
 import java.util.List;
 
 /**
  * Created by daniel on 9/3/13.
  */
 public final class ContactHelper {
     public static List<Contact> getContactList(Context context) {
         List<Contact> contactList = new ArrayList<Contact>();
         ContentResolver contentResolver = context.getContentResolver();
         Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
         while(cursor.moveToNext()) {
             if(Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                 String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                 String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                 List<String> emails = getUserEmails(context, id);
                 List<String> numbers = getUserNumbers(context, id);
                 Contact contact = new Contact(displayName, emails, numbers);
                 contactList.add(contact);
             }
         }
         cursor.close();
 
        Collections.reverse(contactList);
         return contactList;
     }
 
     private static List<String> getUserEmails(Context context, String userId) {
         Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                 ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{userId}, null);
         List<String> emails = new ArrayList<String>();
         while(cursor.moveToNext()) {
             String email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
             emails.add(email);
         }
         cursor.close();
         return emails;
     }
 
     private static List<String> getUserNumbers(Context context, String userId) {
         Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                 ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{userId}, null);
         List<String> phoneNumbers = new ArrayList<String>();
         while(cursor.moveToNext()) {
             String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
             phoneNumbers.add(phoneNumber);
         }
         cursor.close();
         return phoneNumbers;
     }
 }
