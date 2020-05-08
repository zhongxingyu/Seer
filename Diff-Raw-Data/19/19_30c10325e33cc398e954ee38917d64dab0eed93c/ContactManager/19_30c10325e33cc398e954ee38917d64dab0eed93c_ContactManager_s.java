 package com.ae.apps.common.managers;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.provider.BaseColumns;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.Contacts;
 import android.provider.ContactsContract.RawContacts;
 import android.util.Log;
 
 import com.ae.apps.common.utils.CommonUtils;
 import com.ae.apps.common.vo.ContactVo;
 import com.ae.apps.common.vo.MessageVo;
 import com.ae.apps.common.vo.PhoneNumberVo;
 
 /**
  * The ContactManager class manages access to the Android's Contacts and related tables to retrieve information which
  * are exposed via public methods.
  * 
  * Need following permissions in manifest android.permission.READ_CONTACTS android.permission.CALL_PHONE
  * android.permission.READ_SMS
  */
 public class ContactManager {
 
 	private static final String		SMS_PERSON		= "person";
 	private static final String		SMS_URI_INBOX	= "content://sms/inbox";
 	private static final String		DATE_FORMAT		= "MMM dd, yyyy hh:mm a";
 	private static final String		TAG				= "ContactManager";
 
 	private ContentResolver			contentResolver;
 	private Resources				res;
 	protected ArrayList<ContactVo>	contactsList;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param contentResolver
 	 */
 	public ContactManager(ContentResolver contentResolver) {
 		this.contentResolver = contentResolver;
 
 		// Read all the contacts from the contacts database
 		contactsList = fetchAllContacts();
 	}
 
 	/**
 	 * Constructor
 	 * 
 	 * @param contentResolver
 	 * @param res
 	 *            resources instance
 	 */
 	public ContactManager(ContentResolver contentResolver, Resources res) {
 		this(contentResolver);
 		this.res = res;
 	}
 
 	/**
 	 * Returns the total number of contacts fetched from the phone's database
 	 * 
 	 * @return
 	 */
 	public int getTotalContactCount() {
 		return contactsList.size();
 	}
 
 	/**
 	 * Returns a random contactVo from the list if the list is non empty, null otherwise
 	 * 
 	 * @return a ContactVo object
 	 */
 	public ContactVo getRandomContact() {
 		ContactVo contactVo = null;
 		// generate a random number less than contactsList.size();
 		int numContacts = getTotalContactCount();
 
 		if (numContacts > 0) {
 			Random random = new Random();
 			int contactIndex = random.nextInt(numContacts);
 			contactVo = getContactPhoneDetails(contactsList.get(contactIndex));
 
 			// Update the contactslist with the updated contact item
 			contactsList.set(contactIndex, contactVo);
 		}
 
 		return contactVo;
 	}
 
 	/**
 	 * Returns a contact with phone details
 	 * 
 	 * @param id
 	 * @return
 	 */
 	public ContactVo getContactWithPhoneDetails(String id) {
 		ContactVo contactVo = null;
 
 		if (null != id && getTotalContactCount() > 0) {
 			contactVo = getContactPhoneDetails(getContactInfo(id));
 		}
 
 		return contactVo;
 	}
 
 	/**
 	 * Returns a Bitmap object for the contact's photo
 	 * 
 	 * @param contactId
 	 * @return
 	 */
 	public Bitmap getContactPhoto(String contactId) {
 		long contactIdLong = Long.parseLong(contactId);
 		Uri contactPhotoUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactIdLong);
 
 		InputStream photoDataStream = ContactsContract.Contacts.openContactPhotoInputStream(contentResolver,
 				contactPhotoUri);
 		Bitmap photo = BitmapFactory.decodeStream(photoDataStream);
 
 		return photo;
 	}
 
 	/**
 	 * 
 	 * @param contactId
 	 * @return
 	 */
 	public ContactVo getContactInfo(String contactId) {
 		ContactVo contactVo = null;
 		String tmp;
 		for (ContactVo vo : contactsList) {
 			tmp = vo.getId();
 			if (tmp.equals(contactId)) {
 				contactVo = vo;
 				break;
 			}
 		}
 		return contactVo;
 	}
 
 	/**
 	 * Fetches all the Phone numbers for this contacts.
 	 * 
 	 * @param contactVo
 	 * @return
 	 */
 	protected ContactVo getContactPhoneDetails(ContactVo contactVo) {
 		// Make sure we update the contact only if we didnt populate the
 		// phonenumbers list already
 		// and the contact has phone numbers
 		if (contactVo.getPhoneNumbersList() == null && contactVo.getHasPhoneNumber()) {
 			ArrayList<PhoneNumberVo> phoneNumbersList = new ArrayList<PhoneNumberVo>();
 			Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
 					ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { contactVo.getId() },
 					null);
 
 			int phoneType;
 			String phoneLabel = null;
 			String phoneNumber = null;
 			String customLabel = null;
 
 			// Prefetch some column indexes from the cursor
 			int phoneNumberIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
 			int phoneTypeIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
 			int phoneLabelIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL);
 
 			while (phoneCursor.moveToNext()) {
 				// Retrieve values from the cursor
 				phoneType = phoneCursor.getInt(phoneTypeIndex);
 				phoneNumber = phoneCursor.getString(phoneNumberIndex);
 				if (null != res) {
 					customLabel = phoneCursor.getString(phoneLabelIndex);
 					phoneLabel = (String) ContactsContract.CommonDataKinds.Phone.getTypeLabel(res, phoneType,
 							customLabel);
 				}
 
 				// Lets save this phone number as a vo
 				PhoneNumberVo phoneNumberVo = new PhoneNumberVo();
 				phoneNumberVo.setPhoneNumber(phoneNumber);
 				phoneNumberVo.setPhoneType(phoneLabel);
 
 				phoneNumbersList.add(phoneNumberVo);
 			}
 			phoneCursor.close();
 			contactVo.setPhoneNumbersList(phoneNumbersList);
 		}
 		return contactVo;
 	}
 
 	/**
 	 * 
 	 * @param contactId
 	 * @return
 	 */
 	public InputStream openPhoto(long contactId) {
 		Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
 		Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
 		Cursor cursor = contentResolver.query(photoUri, new String[] { Contacts.Photo.DATA15 }, null, null, null);
 
 		if (cursor == null) {
 			return null;
 		}
 		try {
 			if (cursor.moveToFirst()) {
 				byte[] data = cursor.getBlob(0);
 				if (data != null) {
 					return new ByteArrayInputStream(data);
 				}
 			}
 		} finally {
 			cursor.close();
 		}
 		return null;
 	}
 
 	/**
 	 * Returns a MessageVo for the contactId
 	 * 
 	 * @param contactVo
 	 * @return
 	 */
 	public MessageVo getLatestMessage(String contactId) {
 		MessageVo messageVo = null;
 		// TODO : Complete this method
 		/*
 		 * Cursor inboxCursor = contentResolver.query(Uri.parse(SMS_INBOX_URI), null, SMS_PERSON + " = ?", new String[]
 		 * { contactId }, null);
 		 * 
 		 * if (inboxCursor != null) { if (inboxCursor.moveToNext()) { int bodyColumn =
 		 * inboxCursor.getColumnIndex(COLUMN_BODY); if (bodyColumn > 0) { String messageBody =
 		 * inboxCursor.getString(bodyColumn); messageVo = new MessageVo(); messageVo.setBody(messageBody); } } }
 		 */
 		List<MessageVo> listOfMessages = getContactMessages(contactId);
 		if (listOfMessages != null && listOfMessages.size() > 0) {
 			messageVo = listOfMessages.get(0);
 		}
 		return messageVo;
 	}
 
 	/**
 	 * Returns the list of messages for this contact with id
 	 * 
 	 * @param contactId
 	 * @return
 	 */
 	public List<MessageVo> getContactMessages(String contactId) {
 		MessageVo messageVo = null;
 		List<MessageVo> messagesList = new ArrayList<MessageVo>();
 		String[] projection = new String[] { "_id", "address", SMS_PERSON, "body" };
 		Cursor inboxCursor = contentResolver.query(Uri.parse(SMS_URI_INBOX), projection, SMS_PERSON + " = ?",
 				new String[] { contactId }, null);
 
 		if (inboxCursor.moveToLast()) {
 			int bodyIndex = inboxCursor.getColumnIndex("body");
 			do {
 				String messageBody = inboxCursor.getString(bodyIndex);
 				messageVo = new MessageVo();
 				messageVo.setBody(messageBody);
 				messagesList.add(messageVo);
 			} while (inboxCursor.moveToNext());
 			inboxCursor.close();
 		}
 		return messagesList;
 	}
 
 	/**
 	 * Returns the contactId based on the rawContactId
 	 * 
 	 * @param rawContactId
 	 * @return
 	 */
 	public long getContactIdFromRawContactId(String rawContactId) {
 		long contactId = 0;
 		String[] projection = new String[] { RawContacts._ID, RawContacts.CONTACT_ID };
 		Cursor cursor = contentResolver.query(RawContacts.CONTENT_URI, projection, RawContacts._ID + " = ?",
 				new String[] { rawContactId }, null);
 
 		if (cursor.moveToFirst()) {
 			int contactIdIndex = cursor.getColumnIndex(RawContacts.CONTACT_ID);
 			contactId = cursor.getLong(contactIdIndex);
 		}
 		cursor.close();
 
 		return contactId;
 	}
 
 	/**
 	 * Does a lookup on the contacts by phone number lookup
 	 * 
 	 * @param address
 	 * @return
 	 */
 	public String getContactIdFromAddress(String address) {
 		String contactId = null;
		if (address != null) {
			String[] projection = new String[] { "_id" };
			Uri personUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, address);
			Cursor cursor = contentResolver.query(personUri, projection, null, null, null);
			if (cursor.moveToFirst()) {
				contactId = cursor.getString(cursor.getColumnIndex("_id"));
 			}
			cursor.close();
 		}
 
 		return contactId;
 	}
 
 	// ---------------------------------
 	// Private Methods
 	// ---------------------------------
 
 	/**
 	 * Fetches all the contacts from the contacts data table
 	 * 
 	 * @return
 	 */
 	private ArrayList<ContactVo> fetchAllContacts() {
 		ArrayList<ContactVo> contactsList = new ArrayList<ContactVo>();
 
 		Log.d(TAG, "Read from the contacts table");
 		Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
 		if (cursor.getCount() > 0) {
 			String id = null;
 			String name = null;
 			String timesContacted = null;
 			String hasPhoneNumberText = null;
 			String lastContactedTime = null;
 			String lastContactedTimeString = null;
 			boolean hasPhoneNumber = false;
 
 			while (cursor.moveToNext()) {
 				// Read data from the Contacts data table
 				id = cursor.getString(cursor.getColumnIndex(BaseColumns._ID));
 				name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
 				timesContacted = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.TIMES_CONTACTED));
 				hasPhoneNumberText = cursor
 						.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
 				lastContactedTime = cursor.getString(cursor
 						.getColumnIndex(ContactsContract.Contacts.LAST_TIME_CONTACTED));
 
 				// Add this contact only if there is a phone number
 				hasPhoneNumber = Integer.parseInt(hasPhoneNumberText) > 0;
 				if (hasPhoneNumber) {
 					lastContactedTimeString = CommonUtils.formatTimeStamp(lastContactedTime, DATE_FORMAT);
 
 					// Save that data to a VO
 					ContactVo contactVo = new ContactVo();
 					contactVo.setId(id);
 					contactVo.setName(name);
 
 					contactVo.setHasPhoneNumber(hasPhoneNumber);
 					contactVo.setTimesContacted(timesContacted);
 					contactVo.setLastContactedTime(lastContactedTimeString);
 
 					// Add the to an arraylist
 					contactsList.add(contactVo);
 				}
 			}
 		}
 		cursor.close();
 		Log.d(TAG, "Found " + contactsList.size() + " contacts");
 		return contactsList;
 	}
 }
