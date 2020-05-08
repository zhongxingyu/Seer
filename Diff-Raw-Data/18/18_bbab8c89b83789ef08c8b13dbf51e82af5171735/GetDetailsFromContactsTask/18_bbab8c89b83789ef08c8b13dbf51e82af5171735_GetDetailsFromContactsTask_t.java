 package hu.velorum.ConCopy.backend.tasks;
 
 import android.database.Cursor;
 import android.provider.BaseColumns;
 import hu.velorum.ConCopy.backend.entity.ContactItem;
 import hu.velorum.ConCopy.backend.entity.PhoneItem;
 import hu.velorum.ConCopy.backend.entity.QueryParams;
 import hu.velorum.ConCopy.backend.statics.DBDataManager;
 import hu.velorum.ConCopy.backend.statics.StaticData;
 import hu.velorum.ConCopy.ui.ContactItemGetFace;
 import hu.velorum.ConCopy.ui.UploadFace;
 
 import java.util.List;
 
 import static android.provider.ContactsContract.CommonDataKinds.*;
 import static android.provider.ContactsContract.Data;
 
 
 /*
 iterate all contacts stored either in phone memory or on the SIM card.
 Read first name, last name, 1 or 2 phone numbers per contact
 + type of phone number (mobile, home, work or other + which is the default phone number to be called).
 Create a text file from these contacts: one line = one contact.
 
 One line = tab separated values:
 first name,
 last name,
 first phone number, type of first phone number(one char code, details to be discussed),
 second phone number (if the contact has a second phone number),type of second phone number (if applicable).
 
 Send text file to server: HTTP POST request, text file is
 the body of the POST request.  Server responds with a 6 character code ("access code").
 
 Display the access code to user on the second screen with a success message.
 
 Content-Type needs to be "text/plain; charset=utf-8"
 Content-Length needs to be the length of the text file (in bytes, obvisouly)
 the text file needs to be UTF-8 encoded
 the server will respond with a text/plain file
 which contain 6 characters
 ASCII encoding
 
 which is the "access code" we need to display to the user
 
 */
 public class GetDetailsFromContactsTask extends QueryForCursorTask {
 
 	/*
 	If first name or last name is empty, then please write an empty string instead of "null"
 	We haven't discussed yet, but here are the codes for phone number types:
 
 	*/
 
 	public static final String AND_ARG_ = " = ?";
 	private static final String TAG = "GetEmailsTask";
 	private List<ContactItem> contacts;
 	private ContactItemGetFace<ContactItem, Cursor> contactsUpdateFace;
 	private static final String[] arguments1 = new String[1];
 
 	public GetDetailsFromContactsTask(ContactItemGetFace<ContactItem, Cursor> taskFace, QueryParams params,
 									  List<ContactItem> contacts) {
 		super(taskFace, params);
 		this.contacts = contacts;
 		contactsUpdateFace = taskFace;
 	}
 
 	@Override
 	protected int doAdditionToCursor(Cursor cursor) {
 		int totalCnt = cursor.getCount();
 //		float num = 0;
 		int num = 0;
 
 		while (cursor.moveToNext()) {
 
 //			int progress = (int) ((num++ / (float) totalCnt) * 100);
 			int progress = num++;
 
 			String id = DBDataManager.getString(cursor, BaseColumns._ID);
 //			String id = DBDataManager.getString(cursor, ContactsContract.RawContacts.CONTACT_ID);
 //			String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
 
 
 			ContactItem contactItem = new ContactItem();
 
 			arguments1[0] = id;
 			Cursor phoneCursor = contentResolver.query(Phone.CONTENT_URI, PHONE_PROJECTION,
 					PHONE_SELECTION, arguments1 , null);
 
 			Cursor emailsCursor = contentResolver.query(Email.CONTENT_URI, EMAIL_PROJECTION,
 					EMAIL_SELECTION, arguments1, null);
 
 //			String phoneLabel = null;
 			int phoneCnt = 0;
 			if (phoneCursor != null && phoneCursor.moveToFirst()) {
 				do {
 
 					String phoneNumber = DBDataManager.getString(phoneCursor, Phone.NUMBER);
 					String phoneType = DBDataManager.getString(phoneCursor, Phone.TYPE);
 					int preferred = DBDataManager.getInt(phoneCursor, Phone.IS_PRIMARY);
 
 					final PhoneItem phoneItem = new PhoneItem();
 					phoneItem.setPhone(phoneNumber);
 					phoneItem.setType(Integer.parseInt(phoneType), preferred > 0);
 //					phoneLabel = phoneCursor.getString(phoneCursor.getColumnIndex(Phone.LABEL));
 
 					if (phoneCnt == 0) {
 						contactItem.setPhoneItemOne(phoneItem);
 					} else {
 						contactItem.setPhoneItemTwo(phoneItem);
 					}
 
 					if (++phoneCnt == 2) {   // we need only 2 phones
 						break;
 					}
 				} while (phoneCursor.moveToNext());

				phoneCursor.close();
 			} else {
 				continue;
 			}
 
 
 			Cursor dataCursor = contentResolver.query(Data.CONTENT_URI,
 					DATA_PROJECTION, DATA_SELECTION, arguments1, null);
 
 			if (dataCursor != null && dataCursor.moveToFirst()) {
 //				do {
 				dataCursor.moveToNext(); // first position return empty data
 					String firstName = DBDataManager.getString(dataCursor, StructuredName.GIVEN_NAME);
 					contactItem.setFirstName(firstName);
 	//					Log.d("Data", " first name = " + firstName);
 
 					String lastName = DBDataManager.getString(dataCursor, StructuredName.FAMILY_NAME);
 					contactItem.setLastName(lastName);
 	//					Log.d("Data", " lastName = " + lastName);
 //				}while (dataCursor.moveToNext());
				dataCursor.close();
 			}
 
 			if (emailsCursor != null && emailsCursor.moveToFirst()) {
 				contactItem.setEmail(emailsCursor.getString(emailsCursor.getColumnIndex(Email.DATA)));

				emailsCursor.close();
 			}
 
 
 			contacts.add(contactItem);
 
 			((UploadFace) taskFace).onProgressUpdated(progress, totalCnt);
 		}
 		if (contacts.size() > 0) {
 			result = StaticData.RESULT_OK;
 		} else {
 			result = StaticData.VALUE_DOESNT_EXIST;
 		}
 
 		cursor.close();
 
 		return result;
 	}
 
 	@Override
 	protected void onPostExecute(Integer result) {
 		taskFace.showProgress(false);
 		if (isCancelled())
 			return;
 
 		if (result == StaticData.RESULT_OK) {
 			contactsUpdateFace.updateContacts(contacts);
 		} else {
 			taskFace.errorHandle(result);
 		}
 	}
 	private static final String PHONE_SELECTION = Phone.CONTACT_ID + AND_ARG_;
 	private static final String EMAIL_SELECTION = Email.CONTACT_ID + AND_ARG_;
 	private static final String DATA_SELECTION = Data.CONTACT_ID + AND_ARG_ /*+ " AND "
 			+ StructuredName.GIVEN_NAME + " != NULL"*/;
 
 
 	private static final String[] PHONE_PROJECTION = new String[]{
 			Phone._ID,
 			Phone.TYPE,
 			Phone.NUMBER,
 			Phone.IS_PRIMARY
 	};
 
 	private static final String[] EMAIL_PROJECTION = new String[]{
 			Email._ID,
 			Email.ADDRESS
 	};
 	private static final String[] DATA_PROJECTION = new String[]{
 			Data._ID,
 			StructuredName.GIVEN_NAME,
 			StructuredName.FAMILY_NAME
 
 	};
 }
