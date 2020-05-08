 package edu.utsa.cs.smsmessenger.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.utsa.cs.smsmessenger.model.ContactContainer;
 
 import android.content.ContentResolver;
 import android.database.Cursor;
 import android.net.Uri;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.PhoneLookup;
 import android.util.Log;
 
 /**
  * This class is a utility class to allow the activities to query contact
  * information. Contact Names on a phone, get a phone number based on contact,
  * etc.
  * 
  * @author Emilio Mercado
  * @version 1.0
  * @since 1.0
  * 
  */
 
 public class ContactsUtil {
 	/**
 	 * This method retrieves all literal string contact names from the phone
 	 * 
 	 * @param activity
 	 *            The activity which to use for obtaining contentResolver() for
 	 *            querying.
 	 * @return returns an ArrayList of String Contact names for the current
 	 *         Phone Activity.
 	 */
 	public static List<String> getAllContactNames(
 			ContentResolver contentResolver) {
 		List<String> lContactNamesList = new ArrayList<String>();
 		try {
 			// Get all Contacts
 			Cursor lPeople = contentResolver.query(
 					ContactsContract.Contacts.CONTENT_URI, null, null, null,
 					null);
 			if (lPeople != null) {
 				while (lPeople.moveToNext()) {
 					// Add Contact's Name into the List
 					lContactNamesList
 							.add(lPeople.getString(lPeople
 									.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
 				}
 				lPeople.close();
 			}
 		} catch (NullPointerException e) {
 			Log.e("getAllContactNames()", e.getMessage());
 		}
 
 		return lContactNamesList;
 	}
 
 	/**
 	 * This method takes an ID and queries for a ContactContainer object that
 	 * contains display name, Photo URI and Phone Number.
 	 * 
 	 * @param contentResolver
 	 *            The ContentResolver which to use for querying.
 	 * @param contactId
 	 *            The ContentID query parameter.
 	 * @return returns a new ContactContainer instance specified by Contact ID.
 	 */
 	public static ContactContainer getContactById(
 			ContentResolver contentResolver, long contactId) {
 
 		Log.d("getContactById", "id = " + contactId);
 
 		ContactContainer contact = new ContactContainer();
 		if (contactId == -1)
 			return contact;
 
 		String strId = "" + contactId;
 		contact.setId(contactId);
 
 		Cursor cursor = contentResolver.query(
 				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
 				ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
 				new String[] { strId }, null);
 
 		while (cursor.moveToNext()) {
 			String name = cursor.getString(cursor
 					.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
 			String photoUri = cursor.getString(cursor
 					.getColumnIndex(ContactsContract.Contacts.Photo.PHOTO_URI));
 			String phoneNumber = cursor
 					.getString(cursor
 							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
 			contact.setDisplayName(name);
 			contact.setPhotoUri(photoUri);
 			contact.setPhoneNumber(phoneNumber);
 			cursor.close();
 			return contact;
 		}
 		cursor.close();
 		return contact;
 	}
 
 	/**
 	 * This method takes a String PhoneNumber and queries for a ContactContainer
 	 * object that contains display name, Photo URI and Phone Number.
 	 * 
 	 * @param contentResolver
 	 *            The ContentResolver which to use for querying.
 	 * @param phoneNumber
 	 *            The Phone Number to use for querying.
 	 * @return returns a new ContactContainer instance specified by PhoneNumber.
 	 */
 	public static ContactContainer getContactByPhoneNumber(
 			ContentResolver contentResolver, String phoneNumber) {
 
		phoneNumber = getStrippedPhoneNumber(phoneNumber);
 		ContactContainer contact = new ContactContainer();
 		contact.setPhoneNumber(phoneNumber);
 		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
 				Uri.encode(phoneNumber));
 		Cursor cur;
 		try {
 			cur = contentResolver.query(uri, new String[] { PhoneLookup._ID,
 					PhoneLookup.DISPLAY_NAME, PhoneLookup.PHOTO_URI }, null,
 					null, null);
 			if (cur != null) {
 				if (cur.moveToFirst()) {
 					String name = cur
 							.getString(cur
 									.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
 					long id = cur.getLong(cur
 							.getColumnIndex(ContactsContract.Data._ID));
 					String photoUri = cur.getString(cur
 							.getColumnIndex(ContactsContract.Data.PHOTO_URI));
 
 					// Set contact data
 					contact.setId(id);
 					contact.setDisplayName(name);
 					contact.setPhotoUri(photoUri);
 				}
 				cur.close();
 			}
 
 		} catch (Exception e) {
 			Log.d("ContactsUtil",
 					"getContactByPhoneNumber Error getting contacts");
 
 		}
 		return contact;
 	}
 
 	/**
 	 * This static method takes a String Contact Name and retrieves the Phone
 	 * Number.
 	 * 
 	 * @param contentResolver
 	 *            The ContentResolver to allow querying information from the
 	 *            phone contacts.
 	 * @param contactName
 	 *            the contact name used for this query.
 	 * @return returns the String phone number stored on the phone matching the
 	 *         contact
 	 */
 	public static String getPhoneNumberByContactName(
 			ContentResolver contentResolver, String contactName) {
 		String ret = null;
 		String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
 				+ " like'%" + contactName + "%'";
 		String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER };
 		Cursor c = contentResolver.query(
 				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection,
 				selection, null, null);
 		if (c.moveToFirst()) {
 			ret = c.getString(0);
 		}
 		c.close();
 
 		return ret;
 	}
 
 	/**
 	 * This static method takes a String Contact verifies if it is a phone
 	 * number.
 	 * 
 	 * @param contact
 	 *            the contact will be checked to see if passes phone number
 	 *            validation.
 	 * @return returns true if it is a phone number.
 	 */
 	public static boolean isAPhoneNumber(String contact) {
		if ( removeSpecialPhoneChars(contact)
 				.matches("^\\d{3,15}$")) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * This static method that checks if a phone number or contact name has a
 	 * valid phone number.
 	 * 
 	 * @param contentResolver
 	 *            The ContentResolver to allow querying information from the
 	 *            phone contacts.
 	 * @param contact
 	 *            the contact name or contact phone number used for this query.
 	 * @return returns true if contact has a valid phone number.
 	 * 
 	 */
 	public static boolean isAValidPhoneNumber(ContentResolver contentResolver,
 			String contact) {
 		if (isAPhoneNumber(contact)) {
 			return true;
 		} else if (getPhoneNumberByContactName(contentResolver, contact) != null) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * This static method strips all non integer characters for a phone number.
 	 * 
 	 * @param phoneNumber
	 *            The phone to remove all non integer characters from number.
 	 * 
 	 * @return returns a string representing the passed in phone number with all
 	 *         non integer characters removed.
 	 * 
 	 */
 	public static String getStrippedPhoneNumber(String phoneNumber) {
 		return phoneNumber.replaceAll("[^\\d]", "");
 	}

	/**
	 * This static method strips all special characters from a phone number.
	 * 
	 * @param phoneNumber
	 *            The phone to remove all special characters from number.
	 * 
	 * @return returns a string representing the passed in phone number with all
	 *         non special characters removed.
	 * 
	 */
	public static String removeSpecialPhoneChars(String number)
	{
		return number.replace("(", "")
				.replace(")", "").replace(".", "")
				.replace("+", "").replace("-", "")
				.replace("#", "").replace(" ", "");
	}
 }
