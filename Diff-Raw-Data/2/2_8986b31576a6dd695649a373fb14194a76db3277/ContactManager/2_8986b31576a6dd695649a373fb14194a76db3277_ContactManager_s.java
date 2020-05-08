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
 
 package com.geertvanderploeg.kiekeboek.platform;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.geertvanderploeg.kiekeboek.Constants;
 import com.geertvanderploeg.kiekeboek.client.User;
 
 import android.content.ContentProviderOperation;
 import android.content.ContentProviderResult;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.Context;
 import android.content.OperationApplicationException;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.RemoteException;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.CommonDataKinds.Email;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.provider.ContactsContract.CommonDataKinds.StructuredName;
 import android.provider.ContactsContract.Data;
 import android.provider.ContactsContract.RawContacts;
 import android.util.Log;
 
 /**
  * Class for managing contacts sync related mOperations
  */
 public class ContactManager {
 
   private static final String TAG = "ContactManager";
 
   private static Calendar childThreshold;
   private static Calendar teenagerThreshold;
   private static long childGroupId;
   private static long teenagerGroupId;
   private static long adultGroupId;
 
   static {
     childThreshold = new GregorianCalendar();
     childThreshold.add(Calendar.YEAR, -12);
     teenagerThreshold = new GregorianCalendar();
     teenagerThreshold.add(Calendar.YEAR, -20);
   }
 
   /**
    * Synchronize raw contacts
    *
    * @param context The context of Authenticator Activity
    * @param account The username for the account
    * @param users   The list of users
    */
   public static synchronized void syncContacts(Context context,
                                                String account, List<User> users) {
     long userId;
     long rawContactId = 0;
     final ContentResolver resolver = context.getContentResolver();
     final BatchOperation batchOperation =
         new BatchOperation(context, resolver);
     Log.d(TAG, "In SyncContacts");
 
     prepareGroups(resolver);
     for (final User user : users) {
       userId = user.getUserId();
       // Check to see if the contact needs to be inserted or updated
       rawContactId = lookupRawContact(resolver, userId);
       if (rawContactId != 0) {
         if (!user.isDeleted()) {
           // update contact
           Log.i(TAG, "Updating user: " + user);
           updateContact(context, resolver, account, user,
               rawContactId, batchOperation);
         } else {
           Log.i(TAG, "Deleting user: " + user);
           // delete contact
           deleteContact(context, rawContactId, batchOperation);
         }
       } else {
         // add new contact
         Log.d(TAG, "In addContact");
         if (!user.isDeleted()) {
           Log.d(TAG, "Adding user: " + user);
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
 
   private static void prepareGroups(ContentResolver resolver) {
     try {
       // Search for one of the groups to see whether they exist already.
       Cursor c = resolver.query(
           ContactsContract.Groups.CONTENT_URI,
           new String[]{ContactsContract.Groups._ID, ContactsContract.Groups.TITLE},
           "TITLE = ?", new String[]{"Kinderen"}, null);
       if (c == null || !c.moveToFirst()) {
         Log.i(TAG, "Creating groups because they're not found or cursor failed: " + c);
         createGroups(resolver);
       }
     } catch (RemoteException e) {
       e.printStackTrace();
     }
     queryGroupIds(resolver);
   }
 
   private static void createGroups(ContentResolver resolver) throws RemoteException {
     ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
     ops.add(
         ContentProviderOperation
             .newInsert(ContactsContract.Groups.CONTENT_URI)
             .withValue(ContactsContract.Groups.TITLE, "Kinderen")
             .build());
     ops.add(
         ContentProviderOperation
             .newInsert(ContactsContract.Groups.CONTENT_URI)
             .withValue(ContactsContract.Groups.TITLE, "Jongeren")
             .build());
     ops.add(
         ContentProviderOperation
             .newInsert(ContactsContract.Groups.CONTENT_URI)
             .withValue(ContactsContract.Groups.TITLE, "Volwassenen")
             .build());
     try {
       ContentProviderResult[] result = resolver.applyBatch(ContactsContract.AUTHORITY, ops);
       Log.i(TAG, "Created groups, nr of results: " + result.length);
     } catch (OperationApplicationException e) {
       throw new RuntimeException(e);
     }
   }
 
 
   /**
    * Adds a single contact to the platform contacts provider.
    *
    * @param context     the Authenticator Activity context
    * @param accountName the account the contact belongs to
    * @param user        the sample SyncAdapter User object
    */
   private static void addContact(Context context, String accountName, User user, BatchOperation batchOperation) {
 
     /*
      * Do not add children to the address book as they probably will not be called anyway and will not call
      * themselves.
      */
     if (isChild(user.getBirthdate())) {
       return;
     }
 
     // Put the data in the contacts provider
     ContactOperations
         .createNewContact(context, user.getUserId(), accountName, batchOperation)
         .addName(user.getFirstName(), user.getMiddleName(), user.getLastName(), user.getDisplayName())
         .addEmail(user.getEmail())
         .addAddress(user.getStreet(), user.getPostcode(), user.getCity())
         .addProfileAction(user.getUserId())
         .addGroupMembership(getGroupByBirthdate(user.getBirthdate()))
         .addPicture(user.getPhotoData())
         .addPhone(user.getHomePhone(), Phone.TYPE_HOME)
         .addPhone(user.getCellPhone(), Phone.TYPE_MOBILE);
   }
 
   private static boolean isChild(Date birthdate) {
    return birthdate.before(childThreshold.getTime());
   }
 
   private static long getGroupByBirthdate(Date birthdate) {
 
     if (birthdate.after(childThreshold.getTime())) {
       return childGroupId;
     } else if (birthdate.after(teenagerThreshold.getTime())) {
       return teenagerGroupId;
     } else {
       return adultGroupId;
     }
   }
 
   /**
    * Populate the static fields with the current groupids.
    *
    * @param resolver the CR
    */
   private static void queryGroupIds(ContentResolver resolver) {
     final Cursor cursor = resolver.query(ContactsContract.Groups.CONTENT_URI,
         new String[]{ContactsContract.Groups._ID, ContactsContract.Groups.TITLE},
         "title in (?, ?, ?)",
         new String[]{"Kinderen", "Jongeren", "Volwassenen"},
         "");
     if (cursor != null && cursor.moveToFirst()) {
       int titleCi = cursor.getColumnIndex(ContactsContract.Groups.TITLE);
       int idCi = cursor.getColumnIndex(ContactsContract.Groups._ID);
       Map<String, Long> nameIdMapping = new HashMap<String, Long>();
       do {
         long id = cursor.getLong(idCi);
         String title = cursor.getString(titleCi);
 
         if (title.equals("Kinderen")) {
           childGroupId = id;
         } else if (title.equals("Jongeren")) {
           teenagerGroupId = id;
         } else if (title.equals("Volwassenen")) {
           adultGroupId = id;
         }
       }
       while (cursor.moveToNext());
 
     }
     Log.i(TAG, String.format("Result of queryGroupIds: children id: %d, teenagers id:%d, adults id: %d",
         childGroupId, teenagerGroupId, adultGroupId));
   }
 
   /**
    * Updates a single contact to the platform contacts provider.
    *
    * @param context      the Authenticator Activity context
    * @param resolver     the ContentResolver to use
    * @param accountName  the account the contact belongs to
    * @param user         the sample SyncAdapter contact object.
    * @param rawContactId the unique Id for this rawContact in contacts
    *                     provider
    */
   private static void updateContact(Context context,
                                     ContentResolver resolver, String accountName, User user,
                                     long rawContactId, BatchOperation batchOperation) {
     Uri uri;
     String cellPhone = null;
     String homePhone = null;
     String email = null;
 
     final Cursor c = resolver.query(Data.CONTENT_URI, DataQuery.PROJECTION,
             DataQuery.SELECTION, new String[]{String.valueOf(rawContactId)}, null);
     final ContactOperations contactOp = ContactOperations.updateExistingContact(context, rawContactId, batchOperation);
 
     try {
       while (c.moveToNext()) {
         final long id = c.getLong(DataQuery.COLUMN_ID);
         final String mimeType = c.getString(DataQuery.COLUMN_MIMETYPE);
         uri = ContentUris.withAppendedId(Data.CONTENT_URI, id);
 
         if (mimeType.equals(StructuredName.CONTENT_ITEM_TYPE)) {
           final String lastName = c.getString(DataQuery.COLUMN_FAMILY_NAME);
           final String firstName = c.getString(DataQuery.COLUMN_GIVEN_NAME);
           contactOp.updateName(uri, firstName, lastName, user.getFirstName(), user.getLastName());
         } else if (mimeType.equals(Phone.CONTENT_ITEM_TYPE)) {
           final int type = c.getInt(DataQuery.COLUMN_PHONE_TYPE);
           if (type == Phone.TYPE_MOBILE) {
             cellPhone = c.getString(DataQuery.COLUMN_PHONE_NUMBER);
             contactOp.updatePhone(cellPhone, user.getCellPhone(), uri);
           } else if (type == Phone.TYPE_HOME) {
             homePhone = c.getString(DataQuery.COLUMN_PHONE_NUMBER);
             contactOp.updatePhone(homePhone, user.getHomePhone(), uri);
           }
         } else if (Data.MIMETYPE.equals(Email.CONTENT_ITEM_TYPE)) {
           email = c.getString(DataQuery.COLUMN_EMAIL_ADDRESS);
           contactOp.updateEmail(user.getEmail(), email, uri);
         }
       } // while
     } finally {
       c.close();
     }
 
     // Add the cell phone, if present and not updated above
     if (cellPhone == null) {
       contactOp.addPhone(user.getCellPhone(), Phone.TYPE_MOBILE);
     }
 
     // Add the other phone, if present and not updated above
     if (homePhone == null) {
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
    * @param context      the Authenticator Activity context
    * @param rawContactId the unique Id for this rawContact in contacts
    *                     provider
    */
   private static void deleteContact(Context context, long rawContactId, BatchOperation batchOperation) {
     batchOperation.add(ContactOperations.newDeleteCpo(
         ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId), true).build());
   }
 
   /**
    * Returns the RawContact id for a sample SyncAdapter contact, or 0 if the
    * user isn't found.
    *
    * @param resolver the content resolver
    * @param userId   the user ID to lookup
    * @return the RawContact id, or 0 if not found
    */
   private static long lookupRawContact(ContentResolver resolver, long userId) {
     long authorId = 0;
     final Cursor c = resolver.query(RawContacts.CONTENT_URI, UserIdQuery.PROJECTION,
             UserIdQuery.SELECTION, new String[]{String.valueOf(userId)}, null);
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
    * Constants for a query to find a contact given a sample SyncAdapter user
    * ID.
    */
   private interface UserIdQuery {
     public final static String[] PROJECTION =
         new String[]{RawContacts._ID};
 
     public final static int COLUMN_ID = 0;
 
     public static final String SELECTION =
         RawContacts.ACCOUNT_TYPE + "='" + Constants.ACCOUNT_TYPE + "' AND "
             + RawContacts.SOURCE_ID + "=?";
   }
 
   /**
    * Constants for a query to get contact data for a given rawContactId
    */
   private interface DataQuery {
     public static final String[] PROJECTION =
         new String[]{Data._ID, Data.MIMETYPE, Data.DATA1, Data.DATA2,
             Data.DATA3,};
 
     public static final int COLUMN_ID = 0;
     public static final int COLUMN_MIMETYPE = 1;
     public static final int COLUMN_DATA1 = 2;
     public static final int COLUMN_DATA2 = 3;
     public static final int COLUMN_DATA3 = 4;
     public static final int COLUMN_PHONE_NUMBER = COLUMN_DATA1;
     public static final int COLUMN_PHONE_TYPE = COLUMN_DATA2;
     public static final int COLUMN_EMAIL_ADDRESS = COLUMN_DATA1;
     public static final int COLUMN_GIVEN_NAME = COLUMN_DATA2;
     public static final int COLUMN_FAMILY_NAME = COLUMN_DATA3;
 
     public static final String SELECTION = Data.RAW_CONTACT_ID + "=?";
   }
 }
