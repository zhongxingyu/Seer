 package com.abhidsm.whoisnext;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 import android.provider.ContactsContract;
 
 public class ContactList {
 	private List<Contact> _contacts=new ArrayList<Contact>();
 	private Context context;
 	
 	public List<Contact> getContacts()
 	{
 		this.sortContacts();
 		return _contacts;
 	}
 
 	public ContactList(Context context) {
 		super();
 		this.context = context;
 	}
 
 	public void addContact(Contact contact){ 
 		this._contacts.add(contact);
 		this.createFile();
 	}
 	
 	public void removeContact(int position){
 		this._contacts.remove(position);
 		this.createFile();
 	}
 
 	private void createFile(){
 		try {
 			FileOutputStream fos = this.context.openFileOutput(WhoIsNextApplication.fileName, Context.MODE_PRIVATE);
 			for(Iterator<Contact> i = this._contacts.iterator(); i.hasNext();){
 				Contact contact = (Contact) i.next();
 				String data = contact.getId()+","; 
 				fos.write(data.getBytes());
 			}
 			fos.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private String[] getContactIDsFromFile(){
 		String data = "";
 		try {
 			FileInputStream fis = this.context.openFileInput(WhoIsNextApplication.fileName);
 			
 			byte[] input = new byte[fis.available()];
 			while(fis.read(input) != -1){
 				data += new String(input);
 			}
 			fis.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		String[] contactIDs = data.split(",");
 		return contactIDs;
 	}
 	
 	public void addContactsFromFile(){
 		String[] contactIDs = this.getContactIDsFromFile();
 		for(int i=0; i<contactIDs.length; i++){
 			Contact contact = getContactFromID(contactIDs[i]);
			this._contacts.add(contact);
 		}
 	}
 	
 	private Contact getContactFromID(String contactId){
 		Contact contact = new Contact();
 		String where = ContactsContract.Data._ID + " = ? "; 
 		String[] whereParameters = new String[]{contactId}; 
         Uri uri=ContactsContract.Contacts.CONTENT_URI;
         ContentResolver cr = this.context.getContentResolver();
         String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
         Cursor cur=cr.query(uri, null, where, whereParameters, sortOrder);
         if(cur.getCount()>0)
         {
             cur.moveToNext();
             String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
             String name=cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
             String lastTimeContacted =cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LAST_TIME_CONTACTED));
             contact.setId(id);
             contact.setDisplayName(name);
             contact.setLastTimeContacted(lastTimeContacted);
         }
         cur.close();
 		return contact;
 	}
 	
 	private void sortContacts(){
 		int length = this._contacts.size();
 		for(int i=1; i<=length; i++){
 			for(int j=0; j<length-i; j++){
 				if(this._contacts.get(j).getContactedTimeInLong() > this._contacts.get(j+1).getContactedTimeInLong()){
 					Contact tempContact = this._contacts.get(j);
 					this._contacts.set(j, this._contacts.get(j+1));
 					this._contacts.set(j+1, tempContact);
 				}
 			}
 		}
 	}
 }
