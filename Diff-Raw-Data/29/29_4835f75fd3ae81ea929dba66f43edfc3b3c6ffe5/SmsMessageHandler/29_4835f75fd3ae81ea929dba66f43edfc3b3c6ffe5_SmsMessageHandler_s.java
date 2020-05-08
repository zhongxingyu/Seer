 package edu.utsa.cs.smsmessenger.util;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 import edu.utsa.cs.smsmessenger.model.ConversationPreview;
 import edu.utsa.cs.smsmessenger.model.MessageContainer;
 
 /**
  * This class contains methods to interfaces with the system's database
  * 
  * @author Michael Madrigal
  * @version 1.0
  * @since 1.0
  * 
  */
 public class SmsMessageHandler extends SQLiteOpenHelper {
 
 	public static final String UPDATE_MSG_INTENT = "edu.utsa.cs.smsmessenger.UPDATE_MSG_INTENT";
 
 	// Message Characteristics
 	public static final int SMS_MESSAGE_LENGTH = 160;
 	public static final int MAX_CONVERSATIONS = 256;
 	public static final int MAX_MSGS_IN_CONVERSATION = 256;
 
 	// Message status
 	public static final String SMS_NEW_DRAFT = "SMS_NEW_DRAFT";
 	public static final String SMS_DRAFT = "SMS_DRAFT";
 	public static final String SMS_SENT = "SMS_SENT";
 	public static final String SMS_DELIVERED = "SMS_DELIVERED";
 	public static final String SMS_FAILED = "SMS_FAILED";
 	public static final String SMS_RECEIVED = "SMS_RECEIVED";
 
 	public static final String DB_NAME = "cs5103messenger.db";
 	public static final int DB_VERSION = 1;
 
 	// Message Types, also corresponds to table names in database
 	public static final String MSG_TYPE_OUT = "outgoing";
 	public static final String MSG_TYPE_IN = "incoming";
 	public static final String MSG_TYPE_DRAFT = "draft";
 
 	// Table columns for both tables
 	public static final String COL_NAME_ID = "id";
 	public static final String COL_NAME_PHONE_NUMBER = "phoneNumber";
 	public static final String COL_NAME_CONTACT_ID = "contactId";
 	public static final String COL_NAME_DATE = "date";
 	public static final String COL_NAME_SUBJECT = "subject";
 	public static final String COL_NAME_BODY = "body";
 	public static final String COL_NAME_READ = "read";
 	public static final String COL_NAME_STATUS = "status";
 
 	public static final String CREATE_SMS_MSG_TABLE = "CREATE TABLE %s ("
 			+ COL_NAME_ID + " INTEGER PRIMARY KEY, " + COL_NAME_PHONE_NUMBER
 			+ " TEXT, " + COL_NAME_CONTACT_ID + " INTEGER, " + COL_NAME_DATE
 			+ " INTEGER, " + COL_NAME_SUBJECT + " TEXT, " + COL_NAME_BODY
 			+ " TEXT, " + COL_NAME_READ + " INTEGER, " + COL_NAME_STATUS
 			+ " TEXT)";
 
 	public static final String DELETE_TABLE = "DROP TABLE IF EXISTS %s";
 
 	private Context context;
 
 	public SmsMessageHandler(Context context) {
 		super(context, DB_NAME, null, DB_VERSION);
 		this.context = context;
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL(String.format(CREATE_SMS_MSG_TABLE, MSG_TYPE_OUT));
 		db.execSQL(String.format(CREATE_SMS_MSG_TABLE, MSG_TYPE_IN));
 		db.execSQL(String.format(CREATE_SMS_MSG_TABLE, MSG_TYPE_DRAFT));
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
 		// TODO - this will delete old table on upgrade
 		db.execSQL(String.format(DELETE_TABLE, MSG_TYPE_OUT));
 		db.execSQL(String.format(DELETE_TABLE, MSG_TYPE_IN));
 		db.execSQL(String.format(DELETE_TABLE, MSG_TYPE_DRAFT));
 		db.execSQL(String.format(CREATE_SMS_MSG_TABLE, MSG_TYPE_OUT));
 		db.execSQL(String.format(CREATE_SMS_MSG_TABLE, MSG_TYPE_IN));
 		db.execSQL(String.format(CREATE_SMS_MSG_TABLE, MSG_TYPE_DRAFT));
 	}
 
 	/**
 	 * This method saves a MessageContainer object to the OS database table
 	 * specified by the MessageContainer's type.
 	 * 
 	 * @param message
 	 *            is the MessageContainer object to save to the database table
 	 *            specified in the MessageContainer's type.
 	 * @return returns a long that represents the database table id of the saved
 	 *         message.
 	 */
 	public long saveSmsToDB(MessageContainer message) {
 		SQLiteDatabase db = this.getWritableDatabase();
 
 		// Create a new map of values, where column names are the keys
 		ContentValues values = new ContentValues();
 		values.put(COL_NAME_PHONE_NUMBER, message.getPhoneNumber());
 		values.put(COL_NAME_CONTACT_ID, message.getContactId());
 		values.put(COL_NAME_DATE, message.getDate());
 		values.put(COL_NAME_SUBJECT, message.getSubject());
 		values.put(COL_NAME_BODY, message.getBody());
 		values.put(COL_NAME_READ, message.isRead() ? 1 : 0);
 		values.put(COL_NAME_STATUS, message.getStatus());
 
 		// Insert the new row, returning the primary key value of the new row
 		long newRowId;
 		newRowId = db.insert(message.getType(), null, values);
 
 		message.setId(newRowId);
 		message.setSaved(true);
 
 		return newRowId;
 	}
 
 	/**
 	 * This method updates the table row specified by the MessageContainer's id
 	 * with new field values stored in the MessageContainer object.
 	 * 
 	 * @param message
 	 *            is the MessageContainer object to update in the database table
 	 *            specified in the MessageContainer's type.
 	 * @return returns an int that represents the number of rows that were
 	 *         updated.
 	 */
 	public int updateSmsMessage(MessageContainer message) {
 		SQLiteDatabase db = this.getReadableDatabase();
 
 		// New value for one column
 		ContentValues values = new ContentValues();
 		values.put(COL_NAME_PHONE_NUMBER, message.getPhoneNumber());
 		values.put(COL_NAME_CONTACT_ID, message.getContactId());
 		values.put(COL_NAME_DATE, message.getDate());
 		values.put(COL_NAME_SUBJECT, message.getSubject());
 		values.put(COL_NAME_BODY, message.getBody());
 		values.put(COL_NAME_READ, message.isRead() ? 1 : 0);
 		values.put(COL_NAME_STATUS, message.getStatus());
 
 		// Which row to update, based on the ID
 		String selection = COL_NAME_ID + " = ?";
 		String[] selectionArgs = { Long.toString(message.getId()) };
 		message.setSaved(true);
 		int count = db.update(message.getType(), values, selection,
 				selectionArgs);
 
 		return count;
 	}
 
 	/**
 	 * Queries the specified OS database table using the passed in parameters
 	 * for selection, and returns and ArrayList of MessageContainer objects
 	 * representing the database entries.
 	 * 
 	 * @param selection
 	 *            a database selection string with one or more '?' as a
 	 *            placeholder for arguments.
 	 * @param selectionArgs
 	 *            an array of arguments expected in the selection string.
 	 * @param sortOrder
 	 *            the desired order to return the entries from the database.
 	 * @param table
 	 *            the database table to retrieve the requested entries from.
 	 * @return an ArrayList of MessageContainer objects that meet the criteria
 	 *         of the passed parameters.
 	 */
 	public ArrayList<MessageContainer> getSmsMessages(String selection,
 			String[] selectionArgs, String sortOrder, String table) {
 		Log.d("SmsMessageHandler", "getSmsMessages() - table: " + table);
 
 		SQLiteDatabase db = this.getReadableDatabase();
 
 		// Define a projection that specifies which columns from the database
 		// you will actually use after this query.
 		String[] projection = { COL_NAME_ID, COL_NAME_PHONE_NUMBER,
 				COL_NAME_CONTACT_ID, COL_NAME_DATE, COL_NAME_SUBJECT,
 				COL_NAME_BODY, COL_NAME_READ, COL_NAME_STATUS };
 
 		// How you want the results sorted in the resulting Cursor
 		// String sortOrder = COL_NAME_DATE + " DESC";
 
 		Cursor c = db.query(table, projection, selection, selectionArgs, null,
 				null, sortOrder);
 
 		ArrayList<MessageContainer> msgList = new ArrayList<MessageContainer>();
 		c.moveToFirst();
 		if (c.getCount() > 0) {
 			do {
 				// Store in List
 				MessageContainer msg = new MessageContainer();
 				msg.setId(c.getLong(c.getColumnIndex(COL_NAME_ID)));
 				msg.setPhoneNumber(c.getString(c
 						.getColumnIndex(COL_NAME_PHONE_NUMBER)));
 				msg.setContactId(c.getInt(c.getColumnIndex(COL_NAME_CONTACT_ID)));
 				msg.setDate(c.getLong(c.getColumnIndex(COL_NAME_DATE)));
 				msg.setSubject(c.getString(c.getColumnIndex(COL_NAME_SUBJECT)));
 				msg.setBody(c.getString(c.getColumnIndex(COL_NAME_BODY)));
 				msg.setRead(c.getInt(c.getColumnIndex(COL_NAME_READ)) == 0 ? false
 						: true);
 				msg.setStatus(c.getString(c.getColumnIndex(COL_NAME_STATUS)));
 				msg.setType(table);
 
 				msgList.add(msg);
 			} while (c.moveToNext());
 		}
 		return msgList;
 	}
 
 	/**
 	 * This method retrieves messages for the user and the specified contact,
 	 * and merges the messages into an ArrayList of MessageContainer objects
 	 * ordered by date in ascending order.
 	 * 
 	 * @param phoneNumber
 	 *            the phone number of the user to retrieve messages from.
 	 * @return returns an ArrayList of MessageContainer objects representing
 	 *         messages from the user and the contact specified by the phone
 	 *         number.
 	 */
 	public ArrayList<MessageContainer> getConversationWithUser(
 			String phoneNumber) {
 		String selectString = COL_NAME_PHONE_NUMBER + " = ?";
 		String[] selectArgs = { phoneNumber };
 		String sortOrder = COL_NAME_DATE + " ASC";
 
 		ArrayList<MessageContainer> fromMsgList = getSmsMessages(selectString,
 				selectArgs, sortOrder, MSG_TYPE_IN);
 		Log.d("SmsMessageHandler", "getConversationWithUser() fromMsgList: "
 				+ fromMsgList.size());
 		ArrayList<MessageContainer> toMsgList = getSmsMessages(selectString,
 				selectArgs, sortOrder, MSG_TYPE_OUT);
 		Log.d("SmsMessageHandler", "getConversationWithUser() toMsgList: "
 				+ toMsgList.size());
 
 		fromMsgList.addAll(toMsgList);
 		ArrayList<MessageContainer> msgList = fromMsgList; 
 		Collections.sort(msgList);
 		
 		if(msgList.size()>MAX_MSGS_IN_CONVERSATION)
 		{
 			for(int count = 0; count < msgList.size() - MAX_MSGS_IN_CONVERSATION; count++)
 			{
 				deleteMessage(msgList.get(0));
 				msgList.remove(0);
 			}
 		}
 		Log.d("SmsMessageHandler",
 				"getConversationWithUser() fromMsgList merged: "
 						+ fromMsgList.size());
 
 		return msgList;
 	}
 
 	/**
 	 * This method retrieves messages for that meet the query criteria
 	 * 
 	 * @param query
 	 *            the query string to search for in all message bodies.
 	 * @return returns an ArrayList of MessageContainer objects representing
 	 *         messages that meet the query criteria.
 	 */
 	public ArrayList<MessageContainer> queryMessages(String query) {
 		String selectString = COL_NAME_BODY + " LIKE ?";
 		String[] selectArgs = { "%" + query + "%" };
 		String sortOrder = COL_NAME_DATE + " ASC";
 
 		ArrayList<MessageContainer> fromMsgList = getSmsMessages(selectString,
 				selectArgs, sortOrder, MSG_TYPE_IN);
 		Log.d("SmsMessageHandler", "getConversationWithUser() fromMsgList: "
 				+ fromMsgList.size());
 		ArrayList<MessageContainer> toMsgList = getSmsMessages(selectString,
 				selectArgs, sortOrder, MSG_TYPE_OUT);
 		Log.d("SmsMessageHandler", "getConversationWithUser() toMsgList: "
 				+ toMsgList.size());
 
 		fromMsgList.addAll(toMsgList);
 		Collections.sort(fromMsgList);
 		Log.d("SmsMessageHandler",
 				"getConversationWithUser() fromMsgList merged: "
 						+ fromMsgList.size());
 
 		return fromMsgList;
 	}
 
 	/**
 	 * This methods searches the database for all conversations with the user's
 	 * contacts and generates a preview list of each conversation in a HashMap
 	 * data type.
 	 * 
 	 * @return returns a HashMap with a contact's phone number as the key and a
 	 *         ConversationPreview object representing the latest message
 	 *         between the user and contact.
 	 */
 	public HashMap<String, ConversationPreview> getConversationPreviewItmes(
 			Activity activity) {
 
 		// Get message list for database
 		ArrayList<MessageContainer> inMsgList = getSmsMessages(null, null,
 				COL_NAME_DATE + " DESC", MSG_TYPE_IN);
 		ArrayList<MessageContainer> outMsgList = getSmsMessages(null, null,
 				COL_NAME_DATE + " DESC", MSG_TYPE_OUT);
 
 		ArrayList<MessageContainer> msgList = inMsgList;
 		// merge lists
 		msgList.addAll(outMsgList);
 		// sort merged lists
 		Collections.sort(msgList, Collections.reverseOrder());
 
 		HashMap<String, ConversationPreview> convPrevList = new HashMap<String, ConversationPreview>();
 		
 		ArrayList<String> conversationsToDelete = new ArrayList<String>();
 		for (MessageContainer msg : msgList) {
 			Log.d("SmsMessageHandler", "convPrevList.size():" +convPrevList.size() );
 			// Since they are in order, no need to check if next is more recent
 			if (!convPrevList.containsKey(msg.getPhoneNumber())) {
 				if (convPrevList.size() < MAX_CONVERSATIONS) {
 					ConversationPreview preview = new ConversationPreview(
							msg.getBody(), msg.isRead()
									|| msg.getType() == MSG_TYPE_IN ? 0 : 1,
 							msg.getDate(), msg.getPhoneNumber(),
 							msg.getContactId());
 					convPrevList.put(msg.getPhoneNumber(), preview);
 				}
 				else
 				{
 					Log.d("SmsMessageHandler", "convPrevList.size(): " +convPrevList.size() + ", MAX: " + MAX_CONVERSATIONS);
 					if(!conversationsToDelete.contains(msg.getPhoneNumber()))
 						conversationsToDelete.add(msg.getPhoneNumber());
 				}
 			} else {
 				ConversationPreview existing = convPrevList.get(msg
 						.getPhoneNumber());
 				existing.incremtNotReadCount(!msg.isRead());
 			}
 		}
 		//Delete all conversations that exceed the limit
 		if(conversationsToDelete.size()>0)
 		{
 			for(String phoneNumber : conversationsToDelete)
 				deleteConversation(phoneNumber);
 		}
 		return convPrevList;
 	}
 
 	/**
 	 * This method uses the passed in phone number to lookup and delete a
 	 * conversation, which is all message from the user to a contact and from a
 	 * contact to the user.
 	 * 
 	 * @param phoneNumber
 	 *            the phone number of the contact in the conversation with the
 	 *            user that will be deleted.
 	 * @return returns true if delete affected more that one row in any table,
 	 *         returns false if no rows were deleted.
 	 */
 	public boolean deleteConversation(String phoneNumber) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		boolean bDeletedIncoming = db.delete(MSG_TYPE_IN, COL_NAME_PHONE_NUMBER
 				+ "=" + phoneNumber, null) > 0;
 		boolean bDeletedOutgoing = db.delete(MSG_TYPE_OUT,
 				COL_NAME_PHONE_NUMBER + "=" + phoneNumber, null) > 0;
 		return bDeletedIncoming || bDeletedOutgoing;
 	}
 
 	/**
 	 * This method uses the passed MessageContainer object to lookup and delete
 	 * a message from the specified database table.
 	 * 
 	 * @param message
 	 *            the MessageContainer object containing the id and type (table
 	 *            type) to specify which table row to delete.
 	 * @return returns true if delete affected more that one row in any table,
 	 *         returns false if no rows were deleted.
 	 */
 	public boolean deleteMessage(MessageContainer message) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		return db.delete(message.getType(),
 				COL_NAME_ID + "=" + message.getId(), null) > 0;
 	}
 
 	/**
 	 * This method uses deletes all outgoing message with a NEW_DRAFT status
 	 * 
 	 * @return returns true if delete affected more that one row in any table,
 	 *         returns false if no rows were deleted.
 	 */
 	public boolean deleteNewDraftMessage() {
 		SQLiteDatabase db = this.getWritableDatabase();
 
 		String selectString = COL_NAME_STATUS + " = ?";
 		String[] selectArgs = { SMS_NEW_DRAFT };
 		String sortOrder = COL_NAME_DATE + " DESC";
 
 		String[] projection = { COL_NAME_ID };
 
 		// How you want the results sorted in the resulting Cursor
 		// String sortOrder = COL_NAME_DATE + " DESC";
 
 		Cursor c = db.query(MSG_TYPE_DRAFT, projection, selectString,
 				selectArgs, null, null, sortOrder);
 
 		c.moveToFirst();
 		if (c.getCount() > 0) {
 			do {
 				Log.d("SmsMessageHandler",
 						"deleting " + c.getLong(c.getColumnIndex(COL_NAME_ID)));
 
 				db.delete(
 						MSG_TYPE_DRAFT,
 						COL_NAME_ID + "="
 								+ c.getLong(c.getColumnIndex(COL_NAME_ID)),
 						null);
 			} while (c.moveToNext());
 		} else
 			return false;
 
 		return true;
 	}
 
 	/**
 	 * This method uses deletes all outgoing message with a DRAFT status for a phone number
 	 * 
 	 * @return returns true if delete affected more that one row in any table,
 	 *         returns false if no rows were deleted.
 	 */
 	public boolean deleteDraftMessage(String selectString, String[] selectArgs) {
 		SQLiteDatabase db = this.getWritableDatabase();
 
 		String sortOrder = COL_NAME_DATE + " DESC";
 
 		String[] projection = { COL_NAME_ID };
 
 		// How you want the results sorted in the resulting Cursor
 		// String sortOrder = COL_NAME_DATE + " DESC";
 
 		Cursor c = db.query(MSG_TYPE_DRAFT, projection, selectString,
 				selectArgs, null, null, sortOrder);
 
 		c.moveToFirst();
 		if (c.getCount() > 0) {
 			do {
 				Log.d("SmsMessageHandler",
 						"deleting " + c.getLong(c.getColumnIndex(COL_NAME_ID)));
 
 				db.delete(
 						MSG_TYPE_DRAFT,
 						COL_NAME_ID + "="
 								+ c.getLong(c.getColumnIndex(COL_NAME_ID)),
 						null);
 			} while (c.moveToNext());
 		} else
 			return false;
 
 		return true;
 	}
 }
