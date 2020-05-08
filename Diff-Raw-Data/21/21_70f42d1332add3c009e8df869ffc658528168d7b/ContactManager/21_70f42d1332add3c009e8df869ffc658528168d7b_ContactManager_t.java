 /*
  * Copyright (C) 2010 The Android Open Source Project
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.nbos.phonebook.sync.platform;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.CommonDataKinds.Email;
 import android.provider.ContactsContract.CommonDataKinds.Im;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.provider.ContactsContract.CommonDataKinds.StructuredName;
 import android.provider.ContactsContract.Data;
 import android.provider.ContactsContract.RawContacts;
 import android.provider.ContactsContract.StatusUpdates;
 import android.util.Log;
 
 import com.nbos.phonebook.DatabaseHelper;
 import com.nbos.phonebook.R;
 import com.nbos.phonebook.database.tables.BookTable;
 import com.nbos.phonebook.sync.Constants;
 import com.nbos.phonebook.sync.client.Contact;
 import com.nbos.phonebook.sync.client.User;
 import com.nbos.phonebook.sync.client.Group;
 
 /**
  * Class for managing contacts sync related mOperations
  */
 public class ContactManager {
     /**
      * Custom IM protocol used when storing status messages.
      */
     public static final String CUSTOM_IM_PROTOCOL = "SampleSyncAdapter";
     private static final String TAG = "ContactManager";
 
     /**
      * Synchronize raw contacts
      * 
      * @param context The context of Authenticator Activity
      * @param account The username for the account
      * @param users The list of users
      * @param contacts 
      * @param dataCursor 
      */
     public static synchronized void syncContacts(Context context,
         String account, List<User> users, List<User> contacts, Cursor dataCursor) {
         long rawContactId = 0;
         final ContentResolver resolver = context.getContentResolver();
         final BatchOperation batchOperation =
             new BatchOperation(context, resolver);
         
         final Cursor rawContactsCursor =
             resolver.query(RawContacts.CONTENT_URI, UserIdQuery.PROJECTION,
                 null, null, null);
         Log.i(TAG, "There are "+rawContactsCursor.getCount()+" raw contacts");
         Log.i(TAG, "There are "+contacts.size()+" contacts");
         // syncSharedBooks(context);
         Log.d(TAG, "In SyncContacts");
         for (final User user : users) {
             // userId = Integer.parseInt(user.getUserId());
             // Check to see if the contact needs to be inserted or updated
             rawContactId = lookupRawContact(resolver, user, rawContactsCursor, contacts);
             Log.d(TAG, "Raw contact id is: "+rawContactId);
             if (rawContactId != 0) {
                 if (!user.isDeleted()) {
                     // update contact
                     updateContact(context, resolver, account, user,
                         rawContactId, batchOperation, dataCursor);
                 } else {
                     // delete contact
                     deleteContact(context, rawContactId, batchOperation);
                 }
             } else {
                 // add new contact
                 Log.d(TAG, "In addContact, user: "+user.getFirstName());
                 if (!user.isDeleted()) {
                     addContact(context, account, user, batchOperation);
                 }
             }
             // A sync adapter should batch operations on multiple contacts,
             // because it will make a dramatic performance difference.
             if (batchOperation.size() >= 50) {
                 batchOperation.execute();
             }
         }
         batchOperation.execute();
     }
 
 	private static long lookupRawContact(ContentResolver resolver, User user, Cursor rawContactsCursor, List<User> contacts) {
 		if(rawContactsCursor.getCount() == 0) return 0;
 		rawContactsCursor.moveToFirst();
 		do
 		{
 			String serverId = rawContactsCursor.getString(rawContactsCursor.getColumnIndex(Constants.CONTACT_SERVER_ID));
 			// String sourceId = rawContactsCursor.getString(rawContactsCursor.getColumnIndex(RawContacts.SOURCE_ID));
 			try {
 				if(serverId.equals(user.getUserId()))
 					return rawContactsCursor.getLong(0);
 			}
 			catch(Exception e){}
 			
 		} while(rawContactsCursor.moveToNext());
 		// could not find the contact, do a phone number search
 		for(User u : contacts) {
 			if(u.getCellPhone().equals(user.getCellPhone())) {// maybe a contact
 				// check if the rest of the information is the same
 				if(u.getFirstName().equals(user.getFirstName()))
 				{
 					Log.i(TAG, "Existing contact; ph: "+u.getCellPhone()+", name: "+u.getFirstName()+", serverId: "+user.getUserId()+", contactId: "+u.getContactId()+", phone serverId: "+u.getUserId());
 					// update the serverId of the contact
 					DatabaseHelper.updateContactServerId(u.getContactId(), user.getUserId(), resolver);
 					return Long.parseLong(u.getContactId());
 				}
 			}
 		}
 		return 0;
 	}
 
 	/**
      * Add a list of status messages to the contacts provider.
      * 
      * @param context the context to use
      * @param accountName the username of the logged in user
      * @param statuses the list of statuses to store
      */
     public static void insertStatuses(Context context, String username,
         List<User.Status> list) {
         final ContentValues values = new ContentValues();
         final ContentResolver resolver = context.getContentResolver();
         final BatchOperation batchOperation =
             new BatchOperation(context, resolver);
         for (final User.Status status : list) {
             // Look up the user's sample SyncAdapter data row
             final long userId = status.getUserId();
             final long profileId = lookupProfile(resolver, userId);
 
             // Insert the activity into the stream
             if (profileId > 0) {
                 values.put(StatusUpdates.DATA_ID, profileId);
                 values.put(StatusUpdates.STATUS, status.getStatus());
                 values.put(StatusUpdates.PROTOCOL, Im.PROTOCOL_CUSTOM);
                 values.put(StatusUpdates.CUSTOM_PROTOCOL, CUSTOM_IM_PROTOCOL);
                 values.put(StatusUpdates.IM_ACCOUNT, username);
                 values.put(StatusUpdates.IM_HANDLE, status.getUserId());
                 values.put(StatusUpdates.STATUS_RES_PACKAGE, context
                     .getPackageName());
                 values.put(StatusUpdates.STATUS_ICON, R.drawable.icon);
                 values.put(StatusUpdates.STATUS_LABEL, R.string.label);
 
                 batchOperation
                     .add(ContactOperations.newInsertCpo(
                         StatusUpdates.CONTENT_URI, true).withValues(values)
                         .build());
                 // A sync adapter should batch operations on multiple contacts,
                 // because it will make a dramatic performance difference.
                 if (batchOperation.size() >= 50) {
                     batchOperation.execute();
                 }
             }
         }
         batchOperation.execute();
     }
 
     /**
      * Adds a single contact to the platform contacts provider.
      * 
      * @param context the Authenticator Activity context
      * @param accountName the account the contact belongs to
      * @param user the sample SyncAdapter User object
      */
     private static void addContact(Context context, String accountName,
         User user, BatchOperation batchOperation) {
         // Put the data in the contacts provider
         final ContactOperations contactOp =
             ContactOperations.createNewContact(context, Integer.parseInt(user.getUserId()),
                 accountName, batchOperation);
         contactOp.addName(user.getFirstName(), user.getLastName()).addEmail(
             user.getEmail()).addPhone(user.getCellPhone(), Phone.TYPE_MOBILE)
             .addPhone(user.getHomePhone(), Phone.TYPE_OTHER).addProfileAction(
                 Integer.parseInt(user.getUserId()));
     }
 
     /**
      * Updates a single contact to the platform contacts provider.
      * 
      * @param context the Authenticator Activity context
      * @param resolver the ContentResolver to use
      * @param accountName the account the contact belongs to
      * @param user the sample SyncAdapter contact object.
      * @param rawContactId the unique Id for this rawContact in contacts
      *        provider
      * @param dataCursor 
      */
     private static void updateContact(Context context,
         ContentResolver resolver, String accountName, User user,
         long rawContactId, BatchOperation batchOperation, Cursor dataCursor) {
     	Log.i(TAG, "Update contact: "+user.getFirstName()+", rawContactId: "+rawContactId);
         Uri uri;
         String cellPhone = null;
         String otherPhone = null;
         String email = null;
 
         /*final Cursor dataCursor =
             resolver.query(Data.CONTENT_URI, DataQuery.PROJECTION,
                 DataQuery.SELECTION,
                 new String[] {String.valueOf(rawContactId)}, null);*/
         final ContactOperations contactOp =
             ContactOperations.updateExistingContact(context, rawContactId,
                 batchOperation);
         dataCursor.moveToFirst();
         try {
             do {
             	long rawContactIdee = dataCursor.getLong(dataCursor.getColumnIndex(Data.RAW_CONTACT_ID));
             	if(rawContactIdee != rawContactId) continue;
                 final long id = dataCursor.getLong(DataQuery.COLUMN_ID);
                 final String mimeType = dataCursor.getString(DataQuery.COLUMN_MIMETYPE);
                 uri = ContentUris.withAppendedId(Data.CONTENT_URI, id);
 
                 if (mimeType.equals(StructuredName.CONTENT_ITEM_TYPE)) {
                     final String lastName =
                         dataCursor.getString(DataQuery.COLUMN_FAMILY_NAME);
                     final String firstName =
                         dataCursor.getString(DataQuery.COLUMN_GIVEN_NAME);
                     contactOp.updateName(uri, firstName, lastName, user
                         .getFirstName(), user.getLastName());
                 }
 
                 else if (mimeType.equals(Phone.CONTENT_ITEM_TYPE)) {
                     final int type = dataCursor.getInt(DataQuery.COLUMN_PHONE_TYPE);
 
                     if (type == Phone.TYPE_MOBILE) {
                         cellPhone = dataCursor.getString(DataQuery.COLUMN_PHONE_NUMBER);
                         contactOp.updatePhone(cellPhone, user.getCellPhone(),
                             uri);
                     } else if (type == Phone.TYPE_OTHER) {
                         otherPhone = dataCursor.getString(DataQuery.COLUMN_PHONE_NUMBER);
                         contactOp.updatePhone(otherPhone, user.getHomePhone(),
                             uri);
                     }
                 }
 
                 else if (Data.MIMETYPE.equals(Email.CONTENT_ITEM_TYPE)) {
                     email = dataCursor.getString(DataQuery.COLUMN_EMAIL_ADDRESS);
                     contactOp.updateEmail(user.getEmail(), email, uri);
 
                 }
             } while (dataCursor.moveToNext());// while
         } finally {
             // c.close();
         }
 
         // Add the cell phone, if present and not updated above
         if (cellPhone == null) {
             contactOp.addPhone(user.getCellPhone(), Phone.TYPE_MOBILE);
         }
 
         // Add the other phone, if present and not updated above
         if (otherPhone == null) {
             contactOp.addPhone(user.getHomePhone(), Phone.TYPE_OTHER);
         }
 
         // Add the email address, if present and not updated above
         if (email == null) {
             contactOp.addEmail(user.getEmail());
         }
 
     }
 
     /**
      * Deletes a contact from the platform contacts provider.
      * 
      * @param context the Authenticator Activity context
      * @param rawContactId the unique Id for this rawContact in contacts
      *        provider
      */
     private static void deleteContact(Context context, long rawContactId,
         BatchOperation batchOperation) {
         batchOperation.add(ContactOperations.newDeleteCpo(
             ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId),
             true).build());
     }
 
     /**
      * Returns the RawContact id for a sample SyncAdapter contact, or 0 if the
      * sample SyncAdapter user isn't found.
      * 
      * @param context the Authenticator Activity context
      * @param userId the sample SyncAdapter user ID to lookup
      * @return the RawContact id, or 0 if not found
      */
     private static long lookupRawContact(ContentResolver resolver, long userId) {
         long authorId = 0;
         final Cursor c =
             resolver.query(RawContacts.CONTENT_URI, UserIdQuery.PROJECTION,
                 UserIdQuery.SELECTION, new String[] {String.valueOf(userId)},
                 null);
         try {
             if (c.moveToFirst()) {
                 authorId = c.getLong(UserIdQuery.COLUMN_ID);
             }
         } finally {
             if (c != null) {
                 c.close();
             }
         }
         return authorId;
     }
 
     /**
      * Returns the Data id for a sample SyncAdapter contact's profile row, or 0
      * if the sample SyncAdapter user isn't found.
      * 
      * @param resolver a content resolver
      * @param userId the sample SyncAdapter user ID to lookup
      * @return the profile Data row id, or 0 if not found
      */
     private static long lookupProfile(ContentResolver resolver, long userId) {
         long profileId = 0;
         final Cursor c =
             resolver.query(Data.CONTENT_URI, ProfileQuery.PROJECTION,
                 ProfileQuery.SELECTION, new String[] {String.valueOf(userId)},
                 null);
         try {
             if (c != null && c.moveToFirst()) {
                 profileId = c.getLong(ProfileQuery.COLUMN_ID);
             }
         } finally {
             if (c != null) {
                 c.close();
             }
         }
         return profileId;
     }
 
     /**
      * Constants for a query to find a contact given a sample SyncAdapter user
      * ID.
      */
     private interface ProfileQuery {
         public final static String[] PROJECTION = new String[] {Data._ID};
 
         public final static int COLUMN_ID = 0;
 
         public static final String SELECTION =
             Data.MIMETYPE + "='" + SampleSyncAdapterColumns.MIME_PROFILE
                 + "' AND " + SampleSyncAdapterColumns.DATA_PID + "=?";
     }
     /**
      * Constants for a query to find a contact given a sample SyncAdapter user
      * ID.
      */
     private interface UserIdQuery {
         public final static String[] PROJECTION =
             // new String[] {RawContacts._ID, RawContacts.SOURCE_ID};
         	new String[] {RawContacts._ID, Constants.CONTACT_SERVER_ID};
         public final static int COLUMN_ID = 0;
 
         public static final String SELECTION =
             // RawContacts.ACCOUNT_TYPE + "='" + Constants.ACCOUNT_TYPE + "' AND " +
             //      RawContacts.SOURCE_ID + "=?";
         	Constants.CONTACT_SERVER_ID + "=?";
     }
 
     /**
      * Constants for a query to get contact data for a given rawContactId
      */
     private interface DataQuery {
         public static final String[] PROJECTION =
             new String[] {Data._ID, Data.MIMETYPE, Data.DATA1, Data.DATA2,
                 Data.DATA3,};
 
         public static final int COLUMN_ID = 0;
         public static final int COLUMN_MIMETYPE = 1;
         public static final int COLUMN_DATA1 = 2;
         public static final int COLUMN_DATA2 = 3;
         public static final int COLUMN_DATA3 = 4;
         public static final int COLUMN_PHONE_NUMBER = COLUMN_DATA1;
         public static final int COLUMN_PHONE_TYPE = COLUMN_DATA2;
         public static final int COLUMN_EMAIL_ADDRESS = COLUMN_DATA1;
         public static final int COLUMN_EMAIL_TYPE = COLUMN_DATA2;
         public static final int COLUMN_GIVEN_NAME = COLUMN_DATA2;
         public static final int COLUMN_FAMILY_NAME = COLUMN_DATA3;
 
         public static final String SELECTION = Data.RAW_CONTACT_ID + "=?";
     }
 
 	public static void resetDirtySharedBooks(Context ctx) {
 		ContentResolver cr = ctx.getContentResolver();
 	    ContentValues values = new ContentValues();
 	    values = new ContentValues();
 	    values.put(BookTable.DIRTY, "0");
 	    int num = cr.update(
 	    		Uri.parse(Constants.SHARE_BOOK_PROVIDER), values,
 	    		null, null);
 	    Log.i(TAG, "Updated "+num+" sharebooks to dirty = 0");
 
 	    
 	}
 
 	public static void resetDirtyGroups(Context ctx) {
 		ContentResolver cr = ctx.getContentResolver();
 	    ContentValues values = new ContentValues();
 	    values.put(ContactsContract.Groups.DIRTY, "0");
 	    int num = cr.update(ContactsContract.Groups.CONTENT_URI, values, null, null);
 	    Log.i(TAG, "Updated "+num+" groups to dirty = 0");
 	}
 	
 
 	public static void resetDirtyContacts(Context mContext) {
 		// TODO: reset individual contact and group from update contact or group
 	    ContentResolver cr = mContext.getContentResolver();
 	    Uri uri = ContactsContract.RawContacts.CONTENT_URI;
 	    ContentValues values = new ContentValues();
 	    values.put(ContactsContract.RawContacts.DIRTY, 0);
 	    int num = cr.update(uri, values, null, null);
 	    Log.i("ContactManager", "Resetting "+num+" dirty on contacts");
 	}
 
 	public static void setDirtyContacts(Context mContext) { // for testing
 	    ContentResolver cr = mContext.getContentResolver();
 	    Uri uri = ContactsContract.RawContacts.CONTENT_URI;
 	    ContentValues values = new ContentValues();
 	    values.put(ContactsContract.RawContacts.DIRTY, 1);
 	    int num = cr.update(uri, values, null, null);
 	    Log.i("ContactManager", "Resetting "+num+" dirty on contacts");
 	}
 
 	public static void updateContact(JSONObject contact, Context context) throws JSONException {
         int sourceId = contact.getInt("sourceId"),
         	contactId = contact.getInt("contactId");
         Log.i(TAG, "updateContact, sourceId: "+sourceId+", contactId: "+contactId);
 	    ContentResolver cr = context.getContentResolver();
 	    Uri uri = ContactsContract.RawContacts.CONTENT_URI;
 	    ContentValues values = new ContentValues();
 	    values.put(Constants.CONTACT_SERVER_ID, sourceId);
	    int rows = cr.update(uri, values, ContactsContract.RawContacts.CONTACT_ID + " = " + contactId, null);
 	    Log.i(TAG, rows + " rows updated");
 	}
 
 	public static void updateBook(JSONObject book, Context context) throws JSONException {
         int sourceId = book.getInt("sourceId"),
     		groupId = book.getInt("groupId");
         Log.i(TAG, "updateBook, sourceId: "+sourceId+", contactId: "+groupId);
         ContentResolver cr = context.getContentResolver();
         Uri uri = ContactsContract.Groups.CONTENT_URI;
         ContentValues values = new ContentValues();
         values.put(ContactsContract.Groups.SOURCE_ID, sourceId);
         int rows = cr.update(uri, values, ContactsContract.Groups._ID + " = " + groupId, null);
         Log.i(TAG, rows + " rows updated");
 	}
 
 	public static void syncSharedBooks(Context mContext, String accountName,
 			List<Group> sharedBooks, List<User> contacts, Cursor dataCursor) {
 		for(Group b : sharedBooks)
 			ContactManager.updateSharedBook(mContext, accountName, b, contacts, dataCursor);
 	}
 
 	private static void updateSharedBook(Context ctx, String accountName, Group sharedBook, List<User> contacts, Cursor dataCursor) {
 	    // Uri uri = Uri.parse(Constants.SHARE_BOOK_PROVIDER);
 	    int id = Integer.parseInt(sharedBook.groupId);
 	    ContentResolver cr = ctx.getContentResolver();
 	    Cursor cursor = cr.query(ContactsContract.Groups.CONTENT_URI, null,  
 	    		ContactsContract.Groups.SOURCE_ID + " = "+id, null, null);
 	    if(cursor.getCount() == 0)
 	    {
 	    	Log.i(TAG, "New share book: "+accountName);
 	    	// create a group with the share book name
 	    	DatabaseHelper.createAGroup(ctx, sharedBook.name, sharedBook.owner, accountName, id);
 	    	cursor.requery();
 	    	Log.i(TAG, "cursor has "+cursor.getCount()+" rows");
 	    }
 	    cursor.moveToFirst();
 	    String groupId = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups._ID));
 	    
     	Log.i(TAG, "Update share book, id: "+groupId);
     	List<User> users = new ArrayList<User>();
     	for(Contact c : sharedBook.contacts)
     		users.add(new User(c.getName(), c.getNumber(), c.getId()));
     	Log.i(TAG, "There are "+users.size()+" users");
     	// syncContacts(ctx, accountName, users, contacts, dataCursor);
     	for(User u : users)
     		updateSharedBookContact(u, groupId, ctx);
 	    
 	}
 
 	private static void updateSharedBookContact(User u, String groupId, Context ctx) {
 		String contactId = DatabaseHelper.getContactIdFromSourceId(ctx.getContentResolver(), u.getUserId());
 		DatabaseHelper.updateToGroup(groupId, contactId, ctx.getContentResolver());
 	}
 
 	public static void updateGroup(JSONObject group, Context context) throws JSONException {
         int sourceId = group.getInt("sourceId"),
 			groupId = group.getInt("groupId");
         Log.i(TAG, "updateGroup, sourceId: "+sourceId+", groupId: "+groupId);
 	    ContentResolver cr = context.getContentResolver();
 	    Uri uri = ContactsContract.Groups.CONTENT_URI;
 	    ContentValues values = new ContentValues();
 	    values.put(ContactsContract.Groups.SOURCE_ID, sourceId);
 	    int rows = cr.update(uri, values, ContactsContract.Groups._ID + " = " + groupId, null);
 	    Log.i(TAG, "updated "+ rows + " rows to sourceId: "+sourceId);
 	}
 
 	public static void syncGroups(Context mContext, String name,
 			List<Group> groups, List<User> contacts, Cursor dataCursor) {
 		for(Group g : groups)
 			ContactManager.updateGroup(mContext, name, g, contacts, dataCursor);
 	}
 
 	private static void updateGroup(Context ctx, String name, Group g, List<User> contacts, Cursor dataCursor) {
 	    String id = g.groupId;
 	    ContentResolver cr = ctx.getContentResolver();
 	    Cursor cursor = cr.query(ContactsContract.Groups.CONTENT_URI, null,  
 	    		ContactsContract.Groups.SOURCE_ID + " = "+id, null, null);
 	    if(cursor.getCount() == 0)
 	    {
 	    	Log.i(TAG, "New group: "+name);
 	    	// create a group with the share book name
 	    	DatabaseHelper.createAGroup(ctx, g.name, null, name, Integer.parseInt(id));
 	    	cursor.requery();
 	    	Log.i(TAG, "cursor has "+cursor.getCount()+" rows");
 	    }
 	    cursor.moveToFirst();
 	    String groupId = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups._ID));
 	    
     	Log.i(TAG, "Update group, id: "+groupId);
     	List<User> users = new ArrayList<User>();
     	for(Contact c : g.contacts)
     		users.add(new User(c.getName(), c.getNumber(), c.getId()));
     	Log.i(TAG, "There are "+users.size()+" users in group "+g.name);
     	syncContacts(ctx, name, users, contacts, dataCursor);
     	for(User u : users)
     		updateGroupContact(u, groupId, ctx);
 	    
 	}
 
 	private static void updateGroupContact(User u, String groupId, Context ctx) {
 		String contactId = DatabaseHelper.getContactIdFromSourceId(ctx.getContentResolver(), u.getUserId());
 		DatabaseHelper.updateToGroup(groupId, contactId, ctx.getContentResolver());
 	}
 }
