 package com.guster.nissan.db.dao;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Environment;
 import android.util.Log;
 
 import com.guster.nissan.db.ContactSchema;
 import com.guster.nissan.db.domain.Contact;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.nio.channels.FileChannel;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by Gusterwoei on 11/4/13.
  */
 public class ContactDatasource {
     private static boolean DEBUG_MODE_ON = true;
     private Context context;
     private SQLiteDatabase db;
     private ContactSchema schema;
 
     public ContactDatasource(Context context) {
         this.context = context;
         schema = new ContactSchema(context);
     }
 
     public void open() {
         db = schema.getWritableDatabase();
     }
 
     public void close() {
         schema.close();
     }
 
     public boolean save(Contact contact) {
         ContentValues values = new ContentValues();
         values.put(ContactSchema.COL_NAME, contact.getName());
         values.put(ContactSchema.COL_TEL, contact.getTel());
 
         if(get(contact.get_id()) != null) {
             // update existing contact
             int rows = 0;
             rows = db.update(ContactSchema.TABLE_NAME, values, ContactSchema.COL_ID + " = " + contact.get_id(), null);
             saveToSdCard();
             return (rows > 0);
         } else {
             // insert into db
             long id = -1;
             id = db.insert(ContactSchema.TABLE_NAME, null, values);
             saveToSdCard();
             return (id != -1);
         }
     }
 
     public Contact get(int id) {
         String query = "" +
                 "SELECT * " +
                 "FROM " + ContactSchema.TABLE_NAME + " " +
                 "WHERE " + ContactSchema.COL_ID + " = " + id;
         Cursor cursor = db.rawQuery(query, null);
 
         if(cursor.getCount() == 0) return null;
 
         cursor.moveToFirst();
         Contact contact = new Contact();
         contact.set_id(cursor.getInt(0));
         contact.setName(cursor.getString(1));
         contact.setTel(cursor.getString(2));
         return contact;
     }
 
     public List<Contact> getAll() {
         List<Contact> contacts = new ArrayList<Contact>();
         String query = "" +
                 "SELECT * " +
                 "FROM " + ContactSchema.TABLE_NAME;
         Cursor cursor = db.rawQuery(query, null);
         if(cursor.getCount() <= 0) return contacts;
 
         cursor.moveToFirst();
         do {
             Contact contact = new Contact();
             contact.set_id(cursor.getInt(0));
             contact.setName(cursor.getString(1));
             contact.setTel((cursor.getString(2)));
             contacts.add(contact);
         } while(cursor.moveToNext());
 
         return contacts;
     }
 
     public boolean delete(Contact contact) {
         int rows = db.delete(ContactSchema.TABLE_NAME, ContactSchema.COL_ID + " = " + contact.get_id(), null);
         return (rows > 0);
     }
 
     public void deleteAll() {
         for(Contact c : getAll()) {
             delete(c);
         }
     }
 
     public int count() {
         return getAll().size();
     }
 
     public void saveToSdCard() {
         if(!DEBUG_MODE_ON) {
             return;
         }
 
         try {
             File sd = Environment.getExternalStorageDirectory();
             File data = Environment.getDataDirectory();
 
             if (sd.canWrite()) {
                 String currentDBPath = "/data/data/" + context.getPackageName() + "/databases/" + ContactSchema.DATABASE_NAME;
                String backupDBPath = "Contacts.txt";
                 File currentDB = new File(currentDBPath);
                 File backupDB = new File(sd, backupDBPath);
 
                 if (currentDB.exists()) {
                     FileChannel src = new FileInputStream(currentDB).getChannel();
                     FileChannel dst = new FileOutputStream(backupDB).getChannel();
                     dst.transferFrom(src, 0, src.size());
                     src.close();
                     dst.close();
                 }
             }
         } catch (Exception e) {
             Log.d("TESTAPP", e.getMessage());
         }
     }
 }
