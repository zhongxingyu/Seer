 package com.pekall.pctool.model.contact;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.content.ContentProviderOperation;
 import android.content.ContentProviderOperation.Builder;
 import android.content.ContentProviderResult;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.OperationApplicationException;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.RemoteException;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.CommonDataKinds.Email;
 import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
 import android.provider.ContactsContract.CommonDataKinds.Im;
 import android.provider.ContactsContract.CommonDataKinds.Nickname;
 import android.provider.ContactsContract.CommonDataKinds.Organization;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.provider.ContactsContract.CommonDataKinds.Photo;
 import android.provider.ContactsContract.CommonDataKinds.StructuredName;
 import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
 import android.provider.ContactsContract.Data;
 import android.provider.ContactsContract.Groups;
 import android.provider.ContactsContract.RawContacts;
 import android.provider.ContactsContract.RawContactsEntity;
 import android.telephony.PhoneNumberUtils;
 import android.text.TextUtils;
 
 import com.pekall.pctool.Slog;
 import com.pekall.pctool.model.account.AccountInfo;
 import com.pekall.pctool.model.contact.Contact.AddressInfo;
 import com.pekall.pctool.model.contact.Contact.ContactVersion;
 import com.pekall.pctool.model.contact.Contact.EmailInfo;
 import com.pekall.pctool.model.contact.Contact.ImInfo;
 import com.pekall.pctool.model.contact.Contact.ModifyTag;
 import com.pekall.pctool.model.contact.Contact.OrgInfo;
 import com.pekall.pctool.model.contact.Contact.PhoneInfo;
 
 public class ContactUtil {
 
     private static final boolean DUMP_PARAMS = true;
 
     //
     // Accounts
     //
     public static final String DEFAULT_ACCOUNT_NAME = "contacts.account.name.local";
     public static final String DEFAULT_ACCOUNT_TYPE = "contacts.account.type.local";
     public static final String SIM1_ACCOUNT_NAME = "contacts.account.name.sim1";
     public static final String SIM1_ACCOUNT_TYPE = "contacts.account.type.sim";
     public static final String SIM2_ACCOUNT_NAME = "contacts.account.name.sim2";
     public static final String SIM2_ACCOUNT_TYPE = "contacts.account.type.sim";
 
     
     //
     //  
     //
     private static final int RAW_CONTACT_DELETE_FLAG = 1;
 
     /**
      * This utility class cannot be instantiated
      */
     private ContactUtil() {
     }
 
     /**
      * Get the {@link Contact} id by phone number
      * 
      * @param context
      * @param number
      * @return {@link Contact} id or zero if cannot find the {@link Contact}
      */
     public static long getRawContactId(Context context, String number) {
         Cursor c = null;
         try {
             c = context.getContentResolver().query(Phone.CONTENT_URI,
                     new String[] {
                             Phone.RAW_CONTACT_ID, Phone.NUMBER
                     }, null, null, null);
             if (c != null && c.moveToFirst()) {
                 while (!c.isAfterLast()) {
                     if (PhoneNumberUtils.compare(number, c.getString(/*
                                                                       * Phone.NUMBER
                                                                       */1))) {
                         return c.getLong(/* Phone.RAW_CONTACT_ID */0);
                     }
                     c.moveToNext();
                 }
             }
         } catch (Exception e) {
             Slog.e("getContactId error:", e);
         } finally {
             if (c != null) {
                 c.close();
             }
         }
         return 0;
     }
 
     /**
      * Get the specified contact version
      * 
      * @param context
      * @param id
      * @return
      */
     public static int getContactVersion(Context context, long id) {
         Cursor cursor = context.getContentResolver().query(RawContacts.CONTENT_URI, new String[] {
                 RawContacts.VERSION
         }, RawContacts.DELETED + "!=? AND " + RawContacts._ID + "=?", new String[] {
                 String.valueOf(RAW_CONTACT_DELETE_FLAG), String.valueOf(id)
         }, null);
 
         int version = -1;
 
         if (cursor != null) {
             if (cursor.moveToFirst()) {
                 version = cursor.getInt(cursor.getColumnIndex(RawContacts.VERSION));
             }
             cursor.close();
         }
         return version;
     }
 
     /**
      * get all version of contacts
      * 
      * @param context
      * @return
      */
     public static List<ContactVersion> getAllContactVersions(Context context) {
         List<ContactVersion> contactVersions = new ArrayList<ContactVersion>();
 
         Cursor cursor = context.getContentResolver().query(RawContacts.CONTENT_URI, new String[] {
                 RawContacts._ID, RawContacts.VERSION
         }, RawContacts.DELETED + "!=" + RAW_CONTACT_DELETE_FLAG, null, null);
 
         if (cursor != null) {
             if (cursor.moveToFirst()) {
                 final int IndexOfId = cursor.getColumnIndex(RawContacts._ID);
                 final int indexOfVersion = cursor.getColumnIndex(RawContacts.VERSION);
 
                 do {
                     ContactVersion contactVersion = new ContactVersion();
 
                     contactVersion.id = cursor.getLong(IndexOfId);
                     contactVersion.version = cursor.getInt(indexOfVersion);
 
                     contactVersions.add(contactVersion);
                 } while (cursor.moveToNext());
             }
             cursor.close();
         }
         return contactVersions;
     }
 
     /**
      * get AllGroupInfos
      * 
      * @param context
      * @return
      */
     public static List<GroupInfo> getAllGroups(Context context) {
         List<GroupInfo> li = new ArrayList<GroupInfo>();
         Cursor cursorOfGroup = context.getContentResolver().query(Groups.CONTENT_URI, null, null, null, null);
         if (cursorOfGroup.moveToFirst()) {
             final int GROUP_ID = cursorOfGroup.getColumnIndex(Groups._ID);
             final int GROUP_TITLE = cursorOfGroup.getColumnIndex(Groups.TITLE);
             final int GROUP_NOTES = cursorOfGroup.getColumnIndex(Groups.NOTES);
             final int GROUP_ACCOUNT_TYPE = cursorOfGroup.getColumnIndex(Groups.ACCOUNT_TYPE);
             final int GROUP_ACCOUNT_NAME = cursorOfGroup.getColumnIndex(Groups.ACCOUNT_NAME);
             do {
                 GroupInfo gi = new GroupInfo();
 
                 gi.grId = cursorOfGroup.getLong(GROUP_ID);
                 gi.name = cursorOfGroup.getString(GROUP_TITLE);
                 gi.note = cursorOfGroup.getString(GROUP_NOTES);
                 gi.accountInfo.accountType = cursorOfGroup.getString(GROUP_ACCOUNT_TYPE);
                 gi.accountInfo.accountName = cursorOfGroup.getString(GROUP_ACCOUNT_NAME);
 
                 li.add(gi);
             } while (cursorOfGroup.moveToNext());
         }
         cursorOfGroup.close();
         return li;
     }
 
  
     /**
      * get all accounts of phone we need to show the account.name
      * 
      * @param context
      * @return
      */
     public static List<AccountInfo> getAllAccounts(Context context) {
         AccountManager accountManager = AccountManager.get(context);
         Account[] accounts = accountManager.getAccounts();
         List<AccountInfo> ls = new ArrayList<AccountInfo>();
         for (int i = 0; i < accounts.length; i++) {
             AccountInfo ai = new AccountInfo();
             ai.accountName = accounts[i].name;
             ai.accountType = accounts[i].type;
             ls.add(ai);
         }
         AccountInfo ai = new AccountInfo();
         ai.accountName = DEFAULT_ACCOUNT_NAME;
         ai.accountType = DEFAULT_ACCOUNT_TYPE;
         ls.add(ai);
         if (getSimCardState(context) == 1) {
             ai = new AccountInfo();
             ai.accountName = SIM1_ACCOUNT_NAME;
             ai.accountType = SIM1_ACCOUNT_TYPE;
             ls.add(ai);
         } else if (getSimCardState(context) == 2) {
             ai = new AccountInfo();
             ai.accountName = SIM2_ACCOUNT_NAME;
             ai.accountType = SIM2_ACCOUNT_TYPE;
             ls.add(ai);
         } else if (getSimCardState(context) == 3) {
             ai = new AccountInfo();
             ai.accountName = SIM1_ACCOUNT_NAME;
             ai.accountType = SIM1_ACCOUNT_TYPE;
             ls.add(ai);
             ai = new AccountInfo();
             ai.accountName = SIM2_ACCOUNT_NAME;
             ai.accountType = SIM2_ACCOUNT_TYPE;
             ls.add(ai);
         }
         return ls;
     }
 
     /**
      * get the sim card state return 1 means simcard 1 return 2 means simcard 2
      * return 3 means simcard1 and simcard2 return 0 means no simcard only test
      * for coolpal 7728
      * 
      * @param context
      * @return
      */
     public static int getSimCardState(Context context) {
         int count = 0;
         String where = RawContacts.ACCOUNT_NAME + "=?" + " and " + RawContacts.ACCOUNT_TYPE + "=?";
         String whereargs1[] = new String[] {
                 SIM1_ACCOUNT_NAME, SIM1_ACCOUNT_TYPE
         };
         String whereargs2[] = new String[] {
                 SIM2_ACCOUNT_NAME, SIM2_ACCOUNT_TYPE
         };
         Cursor cursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, where,
                 whereargs1, null);
         if (cursor.getCount() > 0)
             count++;
         cursor.close();
         cursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, where, whereargs2,
                 null);
         if (cursor.getCount() > 0)
             count += 2;
         cursor.close();
         return count;
     }
 
     /**
      * delete a contact by rawId
      * 
      * @param context
      * @param rawId
      */
     public static boolean deleteContactById(Context context, long rawId) {
         ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
         ops.add(ContentProviderOperation.newDelete(ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawId)).build());
         try {
             context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
             return true;
         } catch (Exception e) {
             Slog.e("Error deleteContactById", e);
             return false;
         }
     }
 
     /**
      * delete some contact by rawId[]
      * 
      * @param context rawId[]
      */
     public static boolean deleteContactByIds(Context context, List<Long> rawIds) {
         ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
         for (long rawId : rawIds) {
             ops.add(ContentProviderOperation.newDelete(ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawId))
                     .build());
         }
         try {
             context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
             return true;
         } catch (RemoteException e) {
             Slog.e("Error deleteContactByIds", e);
             return false;
         } catch (OperationApplicationException e) {
             Slog.e("Error deleteContactByIds", e);
             return false;
         }
     }
 
     public static int deleteContactAll(Context context) {
         return context.getContentResolver().delete(RawContacts.CONTENT_URI, null, null);
     }
 
     /**
      * Update Contact when there is no ModifyTag
      * 
      * @param context
      * @param contact
      * @return
      */
     public static boolean updateContactForce(Context context, Contact contact) {
         Slog.d("updateContactForce E");
 
         if (DUMP_PARAMS) {
             Slog.d(">>>>> DUMP CONTACT >>>>>");
             Slog.d(contact.toString());
             Slog.d("<<<<< DUMP CONTACT <<<<<");
         }
 
         ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
 
         // update Name
         if (hasField(context, StructuredName.CONTENT_ITEM_TYPE, contact.id)) {
             Slog.d("has name");
 
             final Builder builder = ContentProviderOperation
                     .newUpdate(Data.CONTENT_URI)
                     .withSelection(Data.RAW_CONTACT_ID + "=?" + " AND " + Data.MIMETYPE + " = ?",
                             new String[] {
                                     String.valueOf(contact.id), StructuredName.CONTENT_ITEM_TYPE
                             }).withValue(StructuredName.DISPLAY_NAME, contact.name);
             if (TextUtils.isEmpty(contact.name)) {
                 builder.withValue(StructuredName.FAMILY_NAME, "")
                         .withValue(StructuredName.GIVEN_NAME, "")
                         .withValue(StructuredName.MIDDLE_NAME, "");
             }
             ops.add(builder.build());
 
         } else {
             Slog.d("does not has name");
             ops.add(ContentProviderOperation
                     .newInsert(Data.CONTENT_URI)
                     .withValue(Data.RAW_CONTACT_ID, contact.id)
                     .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                     .withValue(StructuredName.DISPLAY_NAME, contact.name)
                     .withValue(StructuredName.FAMILY_NAME, "")
                     .withValue(StructuredName.GIVEN_NAME, "")
                     .withValue(StructuredName.MIDDLE_NAME, "").build());
         }
 
         // update nickname
         if (hasField(context, Nickname.CONTENT_ITEM_TYPE, contact.id)) {
             ops.add(ContentProviderOperation
                     .newUpdate(Data.CONTENT_URI)
                     .withSelection(Data.RAW_CONTACT_ID + "=?" + " AND " + Data.MIMETYPE + " = ?",
                             new String[] {
                                     String.valueOf(contact.id), Nickname.CONTENT_ITEM_TYPE
                             }).withValue(Nickname.NAME, contact.nickname).build());
         } else {
             ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                     .withValue(Data.RAW_CONTACT_ID, contact.id)
                     .withValue(Data.MIMETYPE, Nickname.CONTENT_ITEM_TYPE)
                     .withValue(Nickname.NAME, contact.nickname).build());
         }
 
         // update photo
         if (contact.shouldUpdatePhoto) {
             if (hasField(context, Photo.CONTENT_ITEM_TYPE, contact.id)) {
                 if (contact.photo != null) {
                     ops.add(ContentProviderOperation
                             .newUpdate(Data.CONTENT_URI)
                             .withSelection(
                                     Data.RAW_CONTACT_ID + "=?" + " AND " + Data.MIMETYPE + " = ?",
                                     new String[] {
                                             String.valueOf(contact.id), Photo.CONTENT_ITEM_TYPE
                                     }).withValue(Photo.PHOTO, contact.photo)
                             .withValue(Data.IS_SUPER_PRIMARY, 1).build());
                 } else {
                     long dataPhotoId = -1;
 
                     final String[] projection = new String[] {
                             Data._ID
                     };
                     final String selection = Data.CONTENT_TYPE + "=? and" + Data.RAW_CONTACT_ID + "=?";
                     final String[] selectionArgs = new String[] {
                             Photo.CONTENT_ITEM_TYPE, String.valueOf(contact.id)
                     };
 
                     Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI, projection, selection,
                             selectionArgs, null);
                     if (cursor.moveToFirst()) {
                         dataPhotoId = cursor.getLong(cursor.getColumnIndex(Data._ID));
                     }
                     cursor.close();
                     if (-1 != dataPhotoId) {
                         ops.add(ContentProviderOperation.newDelete(
                                 ContentUris.withAppendedId(Data.CONTENT_URI, dataPhotoId)).build());
                     }
                 }
             } else {
                 if (contact.photo != null) {
                     ops.add(ContentProviderOperation
                             .newInsert(Data.CONTENT_URI)
                             .withValue(Data.RAW_CONTACT_ID, contact.id)
                             .withValue(Data.MIMETYPE, Photo.CONTENT_ITEM_TYPE)
                             .withValue(Photo.PHOTO, contact.photo).build());
                 }
             }
         }
 
         // update Group
         if (contact.groupInfos != null) {
 
             // first delete all groups
             ops.add(ContentProviderOperation
                     .newDelete(Data.CONTENT_URI)
                     .withSelection(
                             Data.RAW_CONTACT_ID + "=? AND " + Data.MIMETYPE + "=?",
                             new String[] {
                                     String.valueOf(contact.id),
                                     GroupMembership.CONTENT_ITEM_TYPE
                             })
                     .build());
 
             for (GroupInfo gi : contact.groupInfos) {
                 ops.add(ContentProviderOperation
                         .newInsert(Data.CONTENT_URI)
                         .withValue(Data.RAW_CONTACT_ID, contact.id)
                         .withValue(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE)
                         .withValue(GroupMembership.GROUP_ROW_ID, gi.grId).build());
             }
         }
 
         // update Phone
         if (contact.phoneInfos != null) {
 
             // first delete all phones
             ops.add(ContentProviderOperation
                     .newDelete(Data.CONTENT_URI)
                     .withSelection(Data.RAW_CONTACT_ID + "=? AND " + Data.MIMETYPE + "=?", new String[] {
                             String.valueOf(contact.id), Phone.CONTENT_ITEM_TYPE
                     })
                     .build());
 
             for (PhoneInfo pr : contact.phoneInfos) {
                 ops.add(ContentProviderOperation
                         .newInsert(Data.CONTENT_URI)
                         .withValue(Data.RAW_CONTACT_ID, contact.id)
                         .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                         .withValue(Phone.NUMBER, pr.number)
                         .withValue(Phone.TYPE, pr.type)
                         .withValue(Phone.LABEL, pr.customName).build());
             }
         }
 
         // update Email
         if (contact.emailInfos != null) {
 
             // first delete all email
             ops.add(ContentProviderOperation
                     .newDelete(Data.CONTENT_URI)
                     .withSelection(Data.RAW_CONTACT_ID + "=? AND " + Data.MIMETYPE + "=?", new String[] {
                             String.valueOf(contact.id), Email.CONTENT_ITEM_TYPE
                     })
                     .build());
 
             for (EmailInfo er : contact.emailInfos) {
                 ops.add(ContentProviderOperation
                         .newInsert(Data.CONTENT_URI)
                         .withValue(Data.RAW_CONTACT_ID, contact.id)
                         .withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE)
                         .withValue(Email.ADDRESS, er.address)
                         .withValue(Email.TYPE, er.type)
                         .withValue(Email.LABEL, er.customName).build());
             }
         }
 
         // update Organization
         if (contact.orgInfos != null) {
 
             // first delete all organizations
             ops.add(ContentProviderOperation
                     .newDelete(Data.CONTENT_URI)
                     .withSelection(Data.RAW_CONTACT_ID + "=? AND " + Data.MIMETYPE + "=?", new String[] {
                             String.valueOf(contact.id), Organization.CONTENT_ITEM_TYPE
                     })
                     .build());
 
             for (OrgInfo or : contact.orgInfos) {
                 ops.add(ContentProviderOperation
                         .newInsert(Data.CONTENT_URI)
                         .withValue(Data.RAW_CONTACT_ID, contact.id)
                         .withValue(Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE)
                         .withValue(Organization.COMPANY, or.company)
                         .withValue(Organization.TYPE, or.type)
                         .withValue(Organization.LABEL, or.customName).build());
             }
         }
 
         // update Address
         if (contact.addressInfos != null) {
             // first delete all addresses
             ops.add(ContentProviderOperation
                     .newDelete(Data.CONTENT_URI)
                     .withSelection(Data.RAW_CONTACT_ID + "=? AND " + Data.MIMETYPE + "=?", new String[] {
                             String.valueOf(contact.id), StructuredPostal.CONTENT_ITEM_TYPE
                     })
                     .build());
 
             for (AddressInfo ar : contact.addressInfos) {
                 ops.add(ContentProviderOperation
                         .newInsert(Data.CONTENT_URI)
                         .withValue(Data.RAW_CONTACT_ID, contact.id)
                         .withValue(Data.MIMETYPE, StructuredPostal.CONTENT_ITEM_TYPE)
                         .withValue(StructuredPostal.LABEL, ar.customName)
                         .withValue(StructuredPostal.COUNTRY, ar.country)
                         .withValue(StructuredPostal.CITY, ar.city)
                         .withValue(StructuredPostal.STREET, ar.street)
                         .withValue(StructuredPostal.POSTCODE, ar.postcode)
                         .withValue(StructuredPostal.REGION, ar.region)
                         .withValue(StructuredPostal.FORMATTED_ADDRESS, ar.address)
                         .withValue(StructuredPostal.TYPE, ar.type).build());
             }
         }
 
         // update IM
         if (contact.imInfos != null) {
             // first delete all im
             ops.add(ContentProviderOperation
                     .newDelete(Data.CONTENT_URI)
                     .withSelection(Data.RAW_CONTACT_ID + "=? AND " + Data.MIMETYPE + "=?", new String[] {
                             String.valueOf(contact.id), Im.CONTENT_ITEM_TYPE
                     })
                     .build());
 
             for (ImInfo ir : contact.imInfos) {
                 ops.add(ContentProviderOperation
                         .newInsert(Data.CONTENT_URI)
                         .withValue(Data.RAW_CONTACT_ID, contact.id)
                         .withValue(Data.MIMETYPE, Im.CONTENT_ITEM_TYPE)
                         .withValue(Im.DATA, ir.account)
                         .withValue(Im.PROTOCOL, ir.protocol)
                         .withValue(Im.CUSTOM_PROTOCOL, ir.customProtocol).build());
             }
         }
 
         try {
 
             if (DUMP_PARAMS) {
                 Slog.d(ops.toString());
             }
 
             context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
 
             Slog.d("updateContactForce X");
 
             return true;
         } catch (RemoteException e) {
             Slog.e("Error updateContactForce", e);
             Slog.d("updateContactForce X");
             return false;
         } catch (OperationApplicationException e) {
             Slog.e("Error updateContactForce", e);
             Slog.d("updateContactForce X");
             return false;
         }
     }
 
     /**
      * update contact
      * 
      * @param context
      * @param contact
      * @return
      */
     public static Queue<Long> updateContact(Context context, Contact contact) {
         Slog.d("updateContact E");
 
         if (DUMP_PARAMS) {
             Slog.d(">>>>> DUMP CONTACT >>>>>");
             Slog.d(contact.toString());
             Slog.d("<<<<< DUMP CONTACT <<<<<");
         }
 
         int newDataIdSkipCount = 0;
         Queue<Long> newDataIds = new LinkedList<Long>();
         ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
         
         // update account
         ops.add(ContentProviderOperation
                 .newUpdate(RawContacts.CONTENT_URI)
                 .withSelection(RawContacts._ID + "=?", new String[] {String.valueOf(contact.id)})
                 .withValue(RawContacts.ACCOUNT_TYPE, contact.accountInfo.accountType)
                 .withValue(RawContacts.ACCOUNT_NAME, contact.accountInfo.accountName).build());
         
 
         // update Name
         if (hasField(context, StructuredName.CONTENT_ITEM_TYPE, contact.id)) {
             ops.add(ContentProviderOperation
                     .newUpdate(Data.CONTENT_URI)
                     .withSelection(Data.RAW_CONTACT_ID + "=?" + " AND " + Data.MIMETYPE + "=?",
                             new String[] { String.valueOf(contact.id), StructuredName.CONTENT_ITEM_TYPE })
                     .withValue(StructuredName.DISPLAY_NAME, contact.name)
                     .withValue(StructuredName.FAMILY_NAME, "")
                     .withValue(StructuredName.GIVEN_NAME, "")
                     .withValue(StructuredName.MIDDLE_NAME, "").build());
         } else {
             ops.add(ContentProviderOperation
                     .newInsert(Data.CONTENT_URI)
                     .withValue(Data.RAW_CONTACT_ID, contact.id)
                     .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                     .withValue(StructuredName.DISPLAY_NAME, contact.name)
                     .withValue(StructuredName.FAMILY_NAME, "")
                     .withValue(StructuredName.GIVEN_NAME, "")
                     .withValue(StructuredName.MIDDLE_NAME, "").build());
 
             newDataIdSkipCount++;
         }
 
         // update nickname
         if (hasField(context, Nickname.CONTENT_ITEM_TYPE, contact.id)) {
             ops.add(ContentProviderOperation
                     .newUpdate(Data.CONTENT_URI)
                     .withSelection(Data.RAW_CONTACT_ID + "=?" + " AND " + Data.MIMETYPE + " = ?",
                             new String[] {
                                     String.valueOf(contact.id), Nickname.CONTENT_ITEM_TYPE
                             }).withValue(Nickname.NAME, contact.nickname).build());
         } else {
             ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                     .withValue(Data.RAW_CONTACT_ID, contact.id)
                     .withValue(Data.MIMETYPE, Nickname.CONTENT_ITEM_TYPE)
                     .withValue(Nickname.NAME, contact.nickname).build());
 
             newDataIdSkipCount++;
         }
 
         // update photo
         if (contact.shouldUpdatePhoto) {
             if (hasField(context, Photo.CONTENT_ITEM_TYPE, contact.id)) {
                 if (contact.photo != null) {
                     // update phone
                     ops.add(ContentProviderOperation
                             .newUpdate(Data.CONTENT_URI)
                             .withSelection(
                                    Data.RAW_CONTACT_ID + "=? AND " + Data.MIMETYPE + " = ?",
                                     new String[] {
                                             String.valueOf(contact.id), Photo.CONTENT_ITEM_TYPE
                                     }).withValue(Data.DATA15, contact.photo)
                             .withValue(Data.IS_SUPER_PRIMARY, 1).build());
                 } else {
                     // delete photo
                     long dataPhotoId = -1;
                     Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI, new String[] {
                             Data._ID
                     }, Data.CONTENT_TYPE + "=? and " + Data.RAW_CONTACT_ID + "=?", new String[] {
                             Photo.CONTENT_ITEM_TYPE, String.valueOf(contact.id)
                     }, null);
                     if (cursor.moveToNext()) {
                         dataPhotoId = cursor.getLong(cursor.getColumnIndex(Data._ID));
                     }
                     cursor.close();
                     if (-1 != dataPhotoId) {
                         ops.add(ContentProviderOperation.newDelete(
                                 ContentUris.withAppendedId(Data.CONTENT_URI, dataPhotoId)).build());
                     }
                 }
             } else {
                 if (contact.photo != null) {
                     ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                             .withValue(Data.RAW_CONTACT_ID, contact.id)
                             .withValue(Data.MIMETYPE, Photo.CONTENT_ITEM_TYPE)
                             .withValue(Photo.PHOTO, contact.photo).build());
 
                     newDataIdSkipCount++;
                 }
             }
         }
 
         // update group
         for (GroupInfo gi : contact.groupInfos) {
             if (gi.modifyFlag == ModifyTag.add) {
                 ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                         .withValue(Data.RAW_CONTACT_ID, contact.id)
                         .withValue(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE)
                         .withValue(GroupMembership.GROUP_ROW_ID, gi.grId).build());
             } else if (gi.modifyFlag == ModifyTag.del) {
                ops.add(ContentProviderOperation.newDelete(ContentUris.withAppendedId(Data.CONTENT_URI, gi.dataId))
                        .build());
             } else if (gi.modifyFlag == ModifyTag.edit) {
                 ops.add(ContentProviderOperation.newUpdate(Data.CONTENT_URI)
                         .withSelection(Data._ID + "=?", new String[] {
                                 String.valueOf(gi.dataId)
                         })
                         .withValue(GroupMembership.GROUP_ROW_ID, gi.grId).build());
             }
         }
 
         // update Phone
         for (PhoneInfo pr : contact.phoneInfos) {
             if (pr.modifyFlag == ModifyTag.add) {
                 ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                         .withValue(Data.RAW_CONTACT_ID, contact.id)
                         .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                         .withValue(Phone.NUMBER, pr.number)
                         .withValue(Phone.TYPE, pr.type)
                         .withValue(Phone.LABEL, pr.customName).build());
             } else if (pr.modifyFlag == ModifyTag.del) {
                 ops.add(ContentProviderOperation.newDelete(ContentUris.withAppendedId(Data.CONTENT_URI, pr.id)).build());
             } else if (pr.modifyFlag == ModifyTag.edit) {
                 ops.add(ContentProviderOperation.newUpdate(Data.CONTENT_URI)
                         .withSelection(Data._ID + "=?", new String[] {
                                 String.valueOf(pr.id)
                         })
                         .withValue(Phone.NUMBER, pr.number)
                         .withValue(Phone.TYPE, pr.type)
                         .withValue(Phone.LABEL, pr.customName).build());
             }
         }
 
         // update Email
         for (EmailInfo er : contact.emailInfos) {
             if (er.modifyFlag == ModifyTag.add) {
                 ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                         .withValue(Data.RAW_CONTACT_ID, contact.id)
                         .withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE)
                         .withValue(Email.ADDRESS, er.address)
                         .withValue(Email.TYPE, er.type)
                         .withValue(Email.LABEL, er.customName).build());
             } else if (er.modifyFlag == ModifyTag.del) {
                 ops.add(ContentProviderOperation.newDelete(ContentUris.withAppendedId(Data.CONTENT_URI, er.id)).build());
             } else if (er.modifyFlag == ModifyTag.edit) {
                 ops.add(ContentProviderOperation.newUpdate(Data.CONTENT_URI)
                         .withSelection(Data._ID + "=?", new String[] {
                                 String.valueOf(er.id)
                         })
                         .withValue(Email.ADDRESS, er.address)
                         .withValue(Email.TYPE, er.type)
                         .withValue(Email.LABEL, er.customName).build());
             }
         }
 
         // update Organization
         for (OrgInfo or : contact.orgInfos) {
             if (or.modifyFlag == ModifyTag.add) {
                 ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                         .withValue(Data.RAW_CONTACT_ID, contact.id)
                         .withValue(Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE)
                         .withValue(Organization.COMPANY, or.company)
                         .withValue(Organization.TYPE, or.type)
                         .withValue(Organization.LABEL, or.customName).build());
             } else if (or.modifyFlag == ModifyTag.del) {
                 ops.add(ContentProviderOperation.newDelete(ContentUris.withAppendedId(Data.CONTENT_URI, or.id)).build());
             } else if (or.modifyFlag == ModifyTag.edit) {
                 ops.add(ContentProviderOperation.newUpdate(Data.CONTENT_URI)
                         .withSelection(Data._ID + "=?", new String[] {
                                 String.valueOf(or.id)
                         })
                         .withValue(Organization.COMPANY, or.company)
                         .withValue(Organization.TYPE, or.type)
                         .withValue(Organization.LABEL, or.customName).build());
             }
         }
 
         // update Address
         for (AddressInfo ar : contact.addressInfos) {
             if (ar.modifyFlag == ModifyTag.add) {
                 ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                         .withValue(Data.RAW_CONTACT_ID, contact.id)
                         .withValue(Data.MIMETYPE, StructuredPostal.CONTENT_ITEM_TYPE)
                         .withValue(StructuredPostal.COUNTRY, ar.country)
                         .withValue(StructuredPostal.CITY, ar.city)
                         .withValue(StructuredPostal.STREET, ar.street)
                         .withValue(StructuredPostal.POSTCODE, ar.postcode)
                         .withValue(StructuredPostal.REGION, ar.region)
                         .withValue(StructuredPostal.FORMATTED_ADDRESS, ar.address)
                         .withValue(StructuredPostal.LABEL, ar.customName)
                         .withValue(StructuredPostal.TYPE, ar.type).build());
 
             } else if (ar.modifyFlag == ModifyTag.del) {
                 ops.add(ContentProviderOperation.newDelete(ContentUris.withAppendedId(Data.CONTENT_URI, ar.id)).build());
             } else if (ar.modifyFlag == ModifyTag.edit) {
                 if (TextUtils.isEmpty(ar.address)) {
                     ar.country = "";
                     ar.street = "";
                     ar.city = "";
                     ar.postcode = "";
                     ar.region = "";
                 }
                 ops.add(ContentProviderOperation.newUpdate(Data.CONTENT_URI)
                         .withSelection(Data._ID + "=?", new String[] {
                                 String.valueOf(ar.id)
                         })
                         .withValue(StructuredPostal.FORMATTED_ADDRESS, ar.address)
                         .withValue(StructuredPostal.COUNTRY, ar.country)
                         .withValue(StructuredPostal.CITY, ar.city)
                         .withValue(StructuredPostal.STREET, ar.street)
                         .withValue(StructuredPostal.POSTCODE, ar.postcode)
                         .withValue(StructuredPostal.LABEL, ar.customName)
                         .withValue(StructuredPostal.REGION, ar.region)
                         .withValue(StructuredPostal.TYPE, ar.type).build());
             }
         }
 
         // update IM
         for (ImInfo ir : contact.imInfos) {
             if (ir.modifyFlag == ModifyTag.add) {
                 ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                         .withValue(Data.RAW_CONTACT_ID, contact.id)
                         .withValue(Data.MIMETYPE, Im.CONTENT_ITEM_TYPE)
                         .withValue(Im.DATA, ir.account)
                         .withValue(Im.PROTOCOL, ir.protocol)
                         .withValue(Im.CUSTOM_PROTOCOL, ir.customProtocol).build());
             } else if (ir.modifyFlag == ModifyTag.del) {
                 ops.add(ContentProviderOperation.newDelete(ContentUris.withAppendedId(Data.CONTENT_URI, ir.id)).build());
             } else if (ir.modifyFlag == ModifyTag.edit) {
                 ops.add(ContentProviderOperation.newUpdate(Data.CONTENT_URI)
                         .withSelection(Data._ID + "=?", new String[] {
                                 String.valueOf(ir.id)
                         })
                         .withValue(Im.DATA, ir.account)
                         .withValue(Im.PROTOCOL, ir.protocol)
                         .withValue(Im.CUSTOM_PROTOCOL, ir.customProtocol).build());
             }
         }
 
         try {
             // Slog.d(ops.toString());
             ContentProviderResult[] results = context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
 
             for (ContentProviderResult result : results) {
                 if (result.uri != null) {
                     if (newDataIdSkipCount > 0) {
                         newDataIdSkipCount--;
                     } else {
                         long newDataId = ContentUris.parseId(result.uri);
                         newDataIds.add(newDataId);
                     }
                 }
             }
             return newDataIds;
         } catch (RemoteException e) {
             Slog.e("Error updateContact", e);
             return null;
         } catch (OperationApplicationException e) {
             Slog.e("Error updateContact", e);
             return null;
         }
 
     }
 
     /**
      * add a contact
      * 
      * @param context
      * @param contact
      * @param AccountInfo
      * @return the id of the new created contact, or -1 if failed
      */
     public static long addContact(Context context, Contact contact) {
         if (DUMP_PARAMS) {
             Slog.d("+++++ DUMP CONTACT +++++");
 
             Slog.d(contact.toString());
 
             Slog.d("----- DUMP CONTACT -----");
         }
 
         ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
 
         // if the contact's account info is empty, we add the contact to default
         // account
         if (TextUtils.isEmpty(contact.accountInfo.accountName)) {
             contact.accountInfo.accountName = DEFAULT_ACCOUNT_NAME;
             contact.accountInfo.accountType = DEFAULT_ACCOUNT_TYPE;
         }
 
         // rawcontacts'account
         // don't give c._Id value because it is automatically increased
         ops.add(ContentProviderOperation
                 .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                 .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, contact.accountInfo.accountType)
                 .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, contact.accountInfo.accountName)
                 .withValue(ContactsContract.RawContacts.AGGREGATION_MODE,
                         ContactsContract.RawContacts.AGGREGATION_MODE_DISABLED).build());
 
         // add group
         if (contact.groupInfos != null) {
             for (int i = 0; i < contact.groupInfos.size(); i++) {
                 GroupInfo gr = contact.groupInfos.get(i);
                 ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                         .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                         .withValue(ContactsContract.Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE)
                         .withValue(Data.DATA1, gr.grId).build());
             }
         }
 
         if (contact.photo != null) {
             ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                     .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                     .withValue(ContactsContract.Data.IS_SUPER_PRIMARY, 1)
                     .withValue(ContactsContract.Data.MIMETYPE, Photo.CONTENT_ITEM_TYPE)
                     .withValue(Photo.PHOTO, contact.photo)
                     .build());
         }
         // name
         if (!contact.name.equals("")) {
             ops.add(ContentProviderOperation
                     .newInsert(ContactsContract.Data.CONTENT_URI)
                     .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                     .withValue(ContactsContract.Data.MIMETYPE,
                             ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                     .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.name).build());
         }
         // Organization
         if (contact.orgInfos.size() > 0) {
             for (Iterator<OrgInfo> iter = contact.orgInfos.iterator(); iter.hasNext();) {
                 OrgInfo or = iter.next();
                 ops.add(ContentProviderOperation
                         .newInsert(ContactsContract.Data.CONTENT_URI)
                         .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                         .withValue(ContactsContract.Data.MIMETYPE,
                                 ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                         .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, or.company)
                         .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, or.type)
                         .withValue(ContactsContract.CommonDataKinds.Organization.DATA3, or.customName).build());
             }
         }
         // phone number
         if (contact.phoneInfos.size() > 0) {
             for (Iterator<PhoneInfo> iter = contact.phoneInfos.iterator(); iter.hasNext();) {
                 PhoneInfo pr = iter.next();
                 ops.add(ContentProviderOperation
                         .newInsert(ContactsContract.Data.CONTENT_URI)
                         .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                         .withValue(ContactsContract.Data.MIMETYPE,
                                 ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                         .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, pr.number)
                         .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, pr.type)
                         .withValue(ContactsContract.CommonDataKinds.Phone.DATA3, pr.customName).build());
             }
         }
         // email
         if (contact.emailInfos.size() > 0) {
             for (Iterator<EmailInfo> iter = contact.emailInfos.iterator(); iter.hasNext();) {
                 EmailInfo er = iter.next();
                 ops.add(ContentProviderOperation
                         .newInsert(ContactsContract.Data.CONTENT_URI)
                         .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                         .withValue(ContactsContract.Data.MIMETYPE,
                                 ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                         .withValue(ContactsContract.CommonDataKinds.Email.DATA, er.address)
                         .withValue(ContactsContract.CommonDataKinds.Email.TYPE, er.type)
                         .withValue(ContactsContract.CommonDataKinds.Email.DATA3, er.customName).build());
             }
         }
         // address
         if (contact.addressInfos.size() > 0) {
             for (Iterator<AddressInfo> iter = contact.addressInfos.iterator(); iter.hasNext();) {
                 AddressInfo ar = iter.next();
                 ops.add(ContentProviderOperation
                         .newInsert(ContactsContract.Data.CONTENT_URI)
                         .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                         .withValue(ContactsContract.Data.MIMETYPE,
                                 ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                         .withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, ar.type)
                         .withValue(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY, ar.country)
                         .withValue(ContactsContract.CommonDataKinds.StructuredPostal.CITY, ar.city)
                         .withValue(ContactsContract.CommonDataKinds.StructuredPostal.STREET, ar.street)
                         .withValue(ContactsContract.CommonDataKinds.StructuredPostal.REGION, ar.region)
                         .withValue(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE, ar.postcode)
                         .withValue(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, ar.address)
                         .withValue(ContactsContract.CommonDataKinds.StructuredPostal.DATA3, ar.customName).build());
             }
         }
         // IM
         if (contact.imInfos.size() > 0) {
             for (Iterator<ImInfo> iter = contact.imInfos.iterator(); iter.hasNext();) {
                 ImInfo ir = iter.next();
                 ops.add(ContentProviderOperation
                         .newInsert(ContactsContract.Data.CONTENT_URI)
                         .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                         .withValue(ContactsContract.Data.MIMETYPE,
                                 ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE)
                         .withValue(ContactsContract.CommonDataKinds.Im.DATA1, ir.account)
                         .withValue(ContactsContract.CommonDataKinds.Im.PROTOCOL, ir.protocol)
                         .withValue(ContactsContract.CommonDataKinds.Im.DATA3, ir.customProtocol).build());
             }
         }
 
         // nick name
         if (contact.nickname != null && !"".equals(contact.nickname)) {
             ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                     .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                     .withValue(ContactsContract.Data.MIMETYPE, Nickname.CONTENT_ITEM_TYPE)
                     .withValue(ContactsContract.CommonDataKinds.Nickname.NAME, contact.nickname).build());
         }
 
         ContentProviderResult[] results;
         try {
             results = context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
             // insert RawContact result
             ContentProviderResult result = results[0];
             Slog.d("count = " + result.count + ", uri = " + result.uri);
 
             return ContentUris.parseId(result.uri);
         } catch (RemoteException e) {
             Slog.e("Error when addContact", e);
             return -1;
         } catch (OperationApplicationException e) {
             Slog.e("Error when addContact", e);
             return -1;
         }
     }
 
     /**
      * get all group's detail information to one account
      * 
      * @param context
      */
     public static List<GroupInfo> getGroupByAccount(Context context, AccountInfo ai) {
         if (ai == null) {
             ai = new AccountInfo();
             ai.accountName = DEFAULT_ACCOUNT_NAME;
             ai.accountType = DEFAULT_ACCOUNT_TYPE;
         }
         String where = ContactsContract.Groups.ACCOUNT_NAME + "=?" + " and " + ContactsContract.Groups.ACCOUNT_TYPE
                 + "=?";
         String whereArgs[] = {
                 ai.accountName, ai.accountType
         };
         Cursor cursor = context.getContentResolver().query(Groups.CONTENT_URI, null, where, whereArgs, null);
         List<GroupInfo> ls = new ArrayList<GroupInfo>();
         try {
             if (cursor.moveToFirst()) {
                 final int GROUP_ID = cursor.getColumnIndex(Groups._ID);
                 final int GROUP_TITLE = cursor.getColumnIndex(Groups.TITLE);
                 final int GROUP_ACCOUNT_NAME = cursor.getColumnIndex(Groups.ACCOUNT_NAME);
                 final int GROUP_ACCOUNT_TYPE = cursor.getColumnIndex(Groups.ACCOUNT_TYPE);
                 do {
                     GroupInfo gi = new GroupInfo();
                     gi.grId = cursor.getLong(GROUP_ID);
                     gi.name = cursor.getString(GROUP_TITLE);
                     gi.accountInfo.accountType = cursor.getString(GROUP_ACCOUNT_NAME);
                     gi.accountInfo.accountName = cursor.getString(GROUP_ACCOUNT_TYPE);
                     ls.add(gi);
                 } while (cursor.moveToNext());
             }
         } finally {
             cursor.close();
         }
         return ls;
     }
 
 
 
     /**
      * Check whether the contact belongs to some group
      * 
      * @param context
      * @param rawContactId
      * @return true if the contact belongs to some group, otherwise false
      */
     public static boolean isContactInGroup(Context context, long rawContactId) {
         String projection[] = {
                 Data.DATA1
         };
         String where = Data.RAW_CONTACT_ID + "=?" + " and " + Data.MIMETYPE + " = ?";
         String whereArgs[] = {
                 String.valueOf(rawContactId), GroupMembership.CONTENT_ITEM_TYPE
         };
         Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI, projection, where, whereArgs, null);
         int count = 0;
         try {
             count = cursor.getCount();
         } finally {
             cursor.close();
         }
         return count > 0;
     }
 
     /**
      * add a group
      */
     public static boolean addGroup(Context context, GroupInfo gi) {
         if (TextUtils.isEmpty(gi.accountInfo.accountName)) {
             gi.accountInfo.accountName = DEFAULT_ACCOUNT_NAME;
             gi.accountInfo.accountType = DEFAULT_ACCOUNT_TYPE;
         }
         
         ContentValues cv = new ContentValues();
         cv.put(Groups.ACCOUNT_NAME, gi.accountInfo.accountName);
         cv.put(Groups.ACCOUNT_TYPE, gi.accountInfo.accountType);
         cv.put(Groups.TITLE, gi.name);
         cv.put(Groups.NOTES, gi.note);
         
         Uri newUri = context.getContentResolver().insert(Groups.CONTENT_URI, cv);
         
         return ContentUris.parseId(newUri) > 0;
     }
 
     /**
      * delete a group
      */
     public static boolean deleteGroup(Context context, long groupId) {
         Uri groupUri = Uri.parse(ContactsContract.Groups.CONTENT_URI.toString() + "?"
                 + ContactsContract.CALLER_IS_SYNCADAPTER + "=true");
         System.out.println(groupUri.toString());
         // remeber if we let callerIsSyncAdapter=true we delete the group and
         // contact's data
         // if we don't attach importance to explain callerIsSyncAdapter
         // we only make group's data dirty not really deleted and unless sync
         // contact
         int rows = context.getContentResolver().delete(groupUri, Groups._ID + "=" + groupId, null);
         if (rows > 0) {
             return true;
         }
         return false;
     }
 
     /**
      * 
      * 
      * @param context
      * @param groupInfo
      * @return true if success, otherwise false
      */
     public static boolean updateGroup(Context context, GroupInfo groupInfo) {
         if (TextUtils.isEmpty(groupInfo.accountInfo.accountName)) {
             groupInfo.accountInfo.accountName = DEFAULT_ACCOUNT_NAME;
             groupInfo.accountInfo.accountType = DEFAULT_ACCOUNT_TYPE;
         }
         ContentValues cv = new ContentValues();
         cv.put(Groups.ACCOUNT_NAME, groupInfo.accountInfo.accountName);
         cv.put(Groups.ACCOUNT_TYPE, groupInfo.accountInfo.accountType);
         cv.put(Groups.TITLE, groupInfo.name);
         cv.put(Groups.NOTES, groupInfo.note);
         int rows = context.getContentResolver().update(Groups.CONTENT_URI, cv, Groups._ID + "=?", new String[] {
                 String.valueOf(groupInfo.grId)
         });
         return rows > 0;
     }
 
     /**
      * Check whether the mimeType specified column has been set in {@link android.provider.ContactsContract.RawContacts.Data}
      * 
      * @param context
      * @param mimeType
      * @param rawContactId
      * @return
      */
     public static boolean hasField(Context context, String mimeType, long rawContactId) {
         String[] projection = {
                 Data._ID
         };
         String where = Data.MIMETYPE + "=? and " + Data.RAW_CONTACT_ID + "=?";
         String[] whereArgs = {
                 mimeType, String.valueOf(rawContactId)
         };
         
         Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI, projection, where, whereArgs, null);
         boolean hasField = false;
         try {
             hasField = (cursor.getCount() > 0);
         } finally {
             cursor.close();
         }
         return hasField;
     }
     
     public static Contact getContactById(Context context, long rawContactId) {
         String selection = RawContacts.DELETED + "!=" + RAW_CONTACT_DELETE_FLAG + " and " + RawContacts._ID + "=?";
         String[] selectionArgs = {String.valueOf(rawContactId)};
         
         Collection<Contact> contacts = getContactsFast(context, selection, selectionArgs);
         
         for (Contact contact : contacts) {
             return contact;
         }
         return null;
     }
     
     public static Collection<Contact> getContactsAll(Context context) {
         String selection = RawContacts.DELETED + "!=" + RAW_CONTACT_DELETE_FLAG;
         String[] selectionArgs = null;
         
         return getContactsFast(context, selection, selectionArgs);
     }
 
     private static Collection<Contact> getContactsFast(Context context, String selection, String[] selectionArgs) {
         Map<Long, Contact> contactsMap = new HashMap<Long, Contact>();
         
         {
             String[] projection = {
                     RawContacts._ID, 
                     RawContacts.ACCOUNT_NAME, 
                     RawContacts.ACCOUNT_TYPE, 
                     RawContacts.VERSION
             };
 
             Cursor cursor = context.getContentResolver().query(RawContacts.CONTENT_URI, projection, 
                     selection, selectionArgs, null);
 
             if (cursor != null) {
                 try {
                     if (cursor.moveToFirst()) {
                         final int idxForId = cursor.getColumnIndex(RawContacts._ID);
                         final int idxForAccountName = cursor.getColumnIndex(RawContacts.ACCOUNT_NAME);
                         final int idxForAccountType = cursor.getColumnIndex(RawContacts.ACCOUNT_TYPE);
                         final int idxForVersion = cursor.getColumnIndex(RawContacts.VERSION);
 
                         do {
                             Contact contact = new Contact();
                             contact.id = cursor.getLong(idxForId);
                             contact.accountInfo.accountName = cursor.getString(idxForAccountName);
                             contact.accountInfo.accountType = cursor.getString(idxForAccountType);
                             contact.version = cursor.getInt(idxForVersion);
                             
                             contactsMap.put(contact.id, contact);
                         } while (cursor.moveToNext());
                     }
                 } finally {
                     cursor.close();
                 }
             }
         }
 
         {
             String[] projection = {
                    RawContactsEntity._ID,
                    RawContactsEntity.DATA_ID,
                    RawContactsEntity.MIMETYPE,
                    RawContactsEntity.DATA1,
                    RawContactsEntity.DATA2,
                    RawContactsEntity.DATA3,
                    RawContactsEntity.DATA4,
                    RawContactsEntity.DATA5,
                    RawContactsEntity.DATA6,
                    RawContactsEntity.DATA7,
                    RawContactsEntity.DATA8,
                    RawContactsEntity.DATA9,
                    RawContactsEntity.DATA10,
                    RawContactsEntity.DATA11,
                    RawContactsEntity.DATA12,
                    RawContactsEntity.DATA13,
                    RawContactsEntity.DATA14,
                    RawContactsEntity.DATA15,
             };
             Cursor cursor = context.getContentResolver().query(RawContactsEntity.CONTENT_URI, projection,
                     selection, selectionArgs, RawContactsEntity._ID);
             if (cursor != null) {
                 try {
                     if (cursor.moveToFirst()) {
                         final int idxForRawContactId = cursor.getColumnIndex(RawContactsEntity._ID);
                         final int idxForDataId = cursor.getColumnIndex(RawContactsEntity.DATA_ID);
                         final int idxForMimeType = cursor.getColumnIndex(RawContactsEntity.MIMETYPE);
                         final int idxForData1 = cursor.getColumnIndex(RawContactsEntity.DATA1);
                         final int idxForData2 = cursor.getColumnIndex(RawContactsEntity.DATA2);
                         final int idxForData3 = cursor.getColumnIndex(RawContactsEntity.DATA3);
                         final int idxForData4 = cursor.getColumnIndex(RawContactsEntity.DATA4);
                         final int idxForData5 = cursor.getColumnIndex(RawContactsEntity.DATA5);
                         final int idxForData6 = cursor.getColumnIndex(RawContactsEntity.DATA6);
                         final int idxForData7 = cursor.getColumnIndex(RawContactsEntity.DATA7);
                         final int idxForData8 = cursor.getColumnIndex(RawContactsEntity.DATA8);
                         final int idxForData9 = cursor.getColumnIndex(RawContactsEntity.DATA9);
                         final int idxForData10 = cursor.getColumnIndex(RawContactsEntity.DATA10);
                         final int idxForData11 = cursor.getColumnIndex(RawContactsEntity.DATA11);
                         final int idxForData12 = cursor.getColumnIndex(RawContactsEntity.DATA12);
                         final int idxForData13 = cursor.getColumnIndex(RawContactsEntity.DATA13);
                         final int idxForData14 = cursor.getColumnIndex(RawContactsEntity.DATA14);
                         final int idxForData15 = cursor.getColumnIndex(RawContactsEntity.DATA15);
                         
                         do {
                             final long rawContactId = cursor.getLong(idxForRawContactId);
                             Contact contact = contactsMap.get(rawContactId);
                             if (contact != null) {
                                 String mimeType = cursor.getString(idxForMimeType);
                                 
                                 if (TextUtils.isEmpty(mimeType)) {
                                     continue;
                                 }
                                 
                                 if (mimeType.equals(StructuredName.CONTENT_ITEM_TYPE)) {
                                     contact.name = cursor.getString(idxForData1);
                                 } else if (mimeType.equals(Nickname.CONTENT_ITEM_TYPE)) {
                                     contact.nickname = cursor.getString(idxForData1);
                                 } else if (mimeType.equals(Photo.CONTENT_ITEM_TYPE)) {
                                     contact.photo = cursor.getBlob(idxForData15);
                                 } else if (mimeType.equals(GroupMembership.CONTENT_ITEM_TYPE)) {
                                     GroupInfo groupInfo = new GroupInfo();
                                     
                                     groupInfo.grId = cursor.getLong(idxForData1);
                                     groupInfo.dataId = cursor.getLong(idxForDataId);
                                     
                                     contact.addGroupInfo(groupInfo);
                                 } else if (mimeType.equals(Phone.CONTENT_ITEM_TYPE)) {
                                     PhoneInfo phoneInfo = new PhoneInfo();
                                     
                                     phoneInfo.id = cursor.getLong(idxForDataId);
                                     phoneInfo.number = cursor.getString(idxForData1);   // number
                                     phoneInfo.type = cursor.getInt(idxForData2);        // type
                                     if (phoneInfo.type == Phone.TYPE_CUSTOM) {
                                         phoneInfo.customName = cursor.getString(idxForData3);   // label
                                     }
                                     
                                     contact.addPhoneInfo(phoneInfo);
                                 } else if (mimeType.equals(Email.CONTENT_ITEM_TYPE)) {
                                     EmailInfo emailInfo = new EmailInfo();
                                     
                                     emailInfo.id = cursor.getLong(idxForDataId);
                                     emailInfo.address = cursor.getString(idxForData1);    // address
                                     emailInfo.type = cursor.getInt(idxForData2);        // type
                                     if (emailInfo.type == Email.TYPE_CUSTOM) {
                                         emailInfo.customName = cursor.getString(idxForData3); // label
                                     }
                                     
                                     contact.addEmailInfo(emailInfo);
                                 } else if (mimeType.equals(Im.CONTENT_ITEM_TYPE)) {
                                     ImInfo imInfo = new ImInfo();
                                     
                                     imInfo.id = cursor.getLong(idxForDataId);
                                     imInfo.account = cursor.getString(idxForData1); // data
                                     imInfo.protocol = cursor.getInt(idxForData5);   // protocol
                                     if (imInfo.protocol == Im.PROTOCOL_CUSTOM) {
                                         imInfo.customProtocol = cursor.getString(idxForData6); // custom protocol
                                     }
                                     
                                     contact.addImInfo(imInfo);
                                 } else if (mimeType.equals(Organization.CONTENT_ITEM_TYPE)) {
                                     OrgInfo orgInfo = new OrgInfo();
                                     
                                     orgInfo.id = cursor.getLong(idxForDataId);
                                     orgInfo.company = cursor.getString(idxForData1);    // company
                                     if (cursor.isNull(idxForData2)) {                   // type
                                         orgInfo.type = Organization.TYPE_WORK;
                                     } else {
                                         orgInfo.type = cursor.getInt(idxForData2);
                                     }
                                     if (orgInfo.type == Organization.TYPE_CUSTOM) {
                                         orgInfo.customName = cursor.getString(idxForData3); // label
                                     }
                                     
                                     contact.addOrgInfo(orgInfo);
                                 } else if (mimeType.equals(StructuredPostal.CONTENT_ITEM_TYPE)) {
                                     AddressInfo addressInfo = new AddressInfo();
                                     
                                     addressInfo.id = cursor.getLong(idxForDataId);
                                     addressInfo.address = cursor.getString(idxForData1);    // formated address
                                     addressInfo.type = cursor.getInt(idxForData2); // type
                                     addressInfo.street = cursor.getString(idxForData4); // street
                                     addressInfo.city = cursor.getString(idxForData7); // city
                                     addressInfo.region = cursor.getString(idxForData8); // region
                                     addressInfo.postcode = cursor.getString(idxForData9); // postcode
                                     addressInfo.country = cursor.getString(idxForData10);// country
 
                                     if (addressInfo.type == StructuredPostal.TYPE_CUSTOM) {
                                         addressInfo.customName = cursor.getString(idxForData3); // label
                                     }
                                     
                                     contact.addAddressInfo(addressInfo);
                                 } 
                             }
                             
                         } while (cursor.moveToNext());
                     }
                 } finally {
                     cursor.close();
                 }
             }
         }
         return contactsMap.values();
     }
 }
