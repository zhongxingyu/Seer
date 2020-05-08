 package edu.ucsb.cs290.touch.to.chat.crypto;
 
 import java.io.File;
 import java.security.PublicKey;
 import java.security.SignedObject;
 
 import net.sqlcipher.database.SQLiteDatabase;
 import net.sqlcipher.database.SQLiteOpenHelper;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.util.Base64;
 
 /**
  * 
  * Instantiate and provide access to the DB, which contains Messages, Contacts,
  * and secure private key storage.
  */
 public class DatabaseHelper extends SQLiteOpenHelper {
 
 	// DB Strings
 	public static final String MESSAGES_TABLE = "Messages";
 	public static final String MESSAGES_ID = "messages_id";
 	public static final String THREAD_ID = "threadId";
 	public static final String CONTACTS_TABLE = "Contacts";
 	public static final String CONTACTS_ID = "_id";
 	public static final String LOCAL_STORAGE = "LocalStorage";
 
 	private static final String ID = "_id";
 	private static final String NICKNAME = "nickname";
 	private static final String CONTACT_ID = "contactId";
 	private static final String DATE_TIME = "dateTime";
 	private static final String SUBJECT = "subject";
 	private static final String MESSAGE_BODY = "messageBody";
 	private static final String HASH_MATCHES = "hashVerifed";
 	private static final String SIGNATURE_MATCHES = "signatureVerifed";
 	private static final String ATTACHMENT = "attachmentBlob";
 	private static final String READ = "read"; // 1 if read, 0 for unread
 
 	// NICKNAME
 	private static final String TOKEN = "token";
 	private static final String CONTACT_NOTE = "note";
 	private static final String VERIFIED_BY = "verifiers";
 	private static final String PRIVATE_KEY = "privateKey";
 	private static final String PUBLIC_KEY = "publicKey";
 	private static final String KEYPAIR_NAME = "keyName";
 
 	// Messages: _id, thread_id, nickname, CONTACT_ID, timestamp, hash_matches,
 	// read, signature_matches, subject, body, attachment
 	private static final String CREATE_MESSAGES_COMMAND = "CREATE TABLE "
 			+ MESSAGES_TABLE + " (" + MESSAGES_ID
 			+ " integer PRIMARY KEY autoincrement, " + THREAD_ID + " INTEGER, "
 			+ NICKNAME + " TEXT, " + CONTACT_ID + " INTEGER, " + DATE_TIME
 			+ " INTEGER, " + HASH_MATCHES + " INTEGER DEFAULT 0, " + READ
 			+ " INTEGER DEFAULT 0, " + SIGNATURE_MATCHES
 			+ " INTEGER DEFAULT 0, " + SUBJECT + " TEXT, " + MESSAGE_BODY
 			+ " TEXT, " + ATTACHMENT + " BLOB);";
 
 	// Contacts: _id, name, CONTACT_ID, timestamp (added), verified_by (_ids),
 	// note
 	private static final String CREATE_CONTACTS_COMMAND = "CREATE TABLE "
 			+ CONTACTS_TABLE + " (" + CONTACTS_ID
 			+ " integer PRIMARY KEY autoincrement, " + NICKNAME + " TEXT, "
 			+ PUBLIC_KEY + " BLOB, " + DATE_TIME + " INTEGER, " + VERIFIED_BY
 			+ " TEXT, " + TOKEN + " BLOB, " + CONTACT_NOTE + " TEXT);";
 
 	// LocalStorage: _id, private key, public key, timestamp (added), name
 	private static final String CREATE_LOCAL_STORAGE_COMMAND = "CREATE TABLE "
 			+ LOCAL_STORAGE + " (" + ID + " integer PRIMARY KEY, "
 			+ PRIVATE_KEY + " TEXT, " + PUBLIC_KEY + " TEXT, " + DATE_TIME
 			+ " INTEGER, " + KEYPAIR_NAME + " TEXT);";
 
 	private static final String DATABASE_NAME = "touchToText.db";
 	private static final int DATABASE_VERSION = 1;
 
 	// Databases and Context
 	private File dbFile = null;
 	private SQLiteDatabase db;
 	private MasterPassword passwordInstance = null;
 	private Context context;
 	private SealablePublicKey publicKey;
 	// The singleton instance
 	private static DatabaseHelper dbHelperInstance = null;
 
 	DatabaseHelper(Context ctx) {
 		// calls the super constructor, requesting the default cursor factory.
 		super(ctx.getApplicationContext(), DATABASE_NAME, null,
 				DATABASE_VERSION);
 		context = ctx;
 	}
 
 	public static DatabaseHelper getInstance(Context ctx) {
 		if (dbHelperInstance == null) {
 			// Use global context for the app
 			dbHelperInstance = new DatabaseHelper(ctx);
 		}
 		return dbHelperInstance;
 	}
 
 	/**
 	 * Create the tables and set a password
 	 * 
 	 * @param password
 	 */
 	public void initalizeInstance(String password) {
 		if (dbHelperInstance != null
 				&& dbHelperInstance.passwordInstance == null) {
 			dbHelperInstance.setPassword(password);
 			SQLiteDatabase.loadLibs(context);
 			db = this.getWritableDatabase(password);
 		}
 	}
 
 	void insertKeypair(byte[] privateKeyRing, byte[] publicKeyRing, String name) {
 		ContentValues cv = new ContentValues();
 		if (privateKeyRing == null) {
 			cv.put(PUBLIC_KEY, publicKeyRing);
 			cv.put(DATE_TIME, System.currentTimeMillis());
 			cv.put(NICKNAME, name);
 			db.insert(CONTACTS_TABLE, null, cv);
 		} else {
 			cv.put(PRIVATE_KEY, privateKeyRing);
 			cv.put(PUBLIC_KEY, publicKeyRing);
 			cv.put(DATE_TIME, System.currentTimeMillis());
 			cv.put(KEYPAIR_NAME, name);
 			db.insert(LOCAL_STORAGE, null, cv);
 		}
 	}
 
 	// Insert a message you just wrote into the database.
 	void addSentMessage(int threadID, int contactID, String nickname,
 			String body) {
 		ContentValues cv = new ContentValues();
 		cv.put(THREAD_ID, threadID);
 		cv.put(NICKNAME, nickname);
 		cv.put(CONTACT_ID, contactID);
 		cv.put(MESSAGE_BODY, body);
 		cv.put(DATE_TIME, System.currentTimeMillis());
 		db.insert(MESSAGES_TABLE, null, cv);
 	}
 
 	public void setPassword(String password) {
 		if (passwordInstance != null) {
 			passwordInstance.forgetPassword();
 		}
 		passwordInstance = MasterPassword.getInstance(password);
 	}
 
 	/**
 	 * Erase the entire database file.
 	 * 
 	 * @return true if DB was deleted, false otherwise.
 	 */
 	public boolean wipeDB() {
 		if (db != null) {
 			db.close();
 			db = null;
 		}
 		return dbFile.delete();
 	}
 
 	private void createTables(SQLiteDatabase db) {
 		db.execSQL(CREATE_MESSAGES_COMMAND);
 		db.execSQL(CREATE_CONTACTS_COMMAND);
 		db.execSQL(CREATE_LOCAL_STORAGE_COMMAND);
 	}
 
 	private boolean tableExists(String table_name) {
 
 		Cursor cursor = getReadableDatabase("password").rawQuery(
 				"select DISTINCT tbl_name from sqlite_master where tbl_name = '"
 						+ table_name + "'", null);
 		if (cursor != null) {
 			if (cursor.getCount() > 0) {
 				cursor.close();
 				return true;
 			}
 			cursor.close();
 		}
 		return false;
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		createTables(db);
 		SecurePreferences encryptedPublicKey = new SecurePreferences(context,
				"touchToTextPreferences.xml", passwordInstance.getPassword()
 						.toString(), true);
		String publicKeyString = Base64.encodeToString(Helpers.serialize(new KeyPairsProvider()), Base64.DEFAULT);
 		encryptedPublicKey.put(PUBLIC_KEY, publicKeyString);
 	}
 
 	// Don't do anything on upgrade! But must implement to work with schema
 	// changes.
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
 	}
 
 	public SealablePublicKey getPGPPublicKey() {
 		SecurePreferences encryptedPublicKey = new SecurePreferences(context,
				"touchToTextPreferences.xml", passwordInstance.getPassword()
 						.toString(), true);
 		
 		String publicKeyString = encryptedPublicKey.getString(PUBLIC_KEY);
		
		publicKey = ((KeyPairsProvider) Helpers.deserialize(Base64.decode(
				publicKeyString, Base64.DEFAULT))).getExternalKey();
 		
 		return publicKey;
 	}
 
 	public void addPublicKey(SealablePublicKey key) {
 		// insertKeypair(null, key.publicKey, key.identity);
 	}
 
 	public void addContact(CryptoContacts.Contact newContact) {
 		AddContactsToDBTask task = new AddContactsToDBTask();
 		task.execute(new CryptoContacts.Contact[] { newContact });
 	}
 
 	// Messages: _id, thread_id, nickname, CONTACT_ID, timestamp, hash_matches,
 	// read, signature_matches, subject, body, attachment
 	public void addOutgoingMessage(final String messageToSend, long timeSent,
 			CryptoContacts.Contact contact) {
 		AddMessageToDBTask task = new AddMessageToDBTask();
 		ContentValues newMessage = new ContentValues();
 		newMessage.put(MESSAGE_BODY, messageToSend);
 		newMessage.put(DATE_TIME, timeSent);
 		newMessage.put(NICKNAME, contact.toString());
 		// Need to get CONTACT_ID from contact and add that, nickname is not
 		// guaranteed unique.
 		task.execute(new ContentValues[] { newMessage });
 	}
 
 	// Messages: _id, thread_id, nickname, CONTACT_ID, timestamp, hash_matches,
 	// read, signature_matches, subject, body, attachment
 
 	private class AddMessageToDBTask extends
 			AsyncTask<ContentValues, Void, Uri> {
 		@Override
 		protected Uri doInBackground(ContentValues... toAdd) {
 			Uri mNewUri = null;
 			for (ContentValues val : toAdd) {
 				mNewUri = context.getContentResolver().insert(
 						MessagesProvider.CONTENT_URI, val);
 			}
 			return mNewUri;
 		}
 
 		@Override
 		protected void onPostExecute(Uri result) {
 			// result is Uri of newly added row
 		}
 	}
 
 	public void getAllContacts() {
 		GetContactsFromDBTask task = new GetContactsFromDBTask();
 		task.execute(new String[] { null });
 	}
 
 	private class AddContactsToDBTask extends
 			AsyncTask<CryptoContacts.Contact, Void, Uri> {
 		@Override
 		protected Uri doInBackground(CryptoContacts.Contact... toAdd) {
 			Uri mNewUri = null;
 			for (CryptoContacts.Contact newContact : toAdd) {
 				ContentValues newUser = new ContentValues();
 				newUser.put(NICKNAME, newContact.toString());
 				newUser.put(PUBLIC_KEY, Helpers.serialize(newContact.getSigningKey()));
 				newUser.put(DATE_TIME, System.currentTimeMillis());
 				newUser.put(TOKEN, Helpers.serialize(newContact.getToken()));
 				mNewUri = context.getContentResolver().insert(
 						MessagesProvider.CONTENT_URI, newUser);
 			}
 			return mNewUri;
 		}
 
 		@Override
 		protected void onPostExecute(Uri result) {
 			// result is Uri of newly added row
 		}
 	}
 
 	private class GetContactsFromDBTask extends AsyncTask<String, Void, Cursor> {
 		@Override
 		protected Cursor doInBackground(String... names) {
 
 			// A "projection" defines the columns that will be returned for each
 			// row
 			String[] mProjection = { ID, TOKEN, PUBLIC_KEY, NICKNAME };
 
 			// Defines a string to contain the selection clause
 			String mSelectionClause = null;
 			// Initializes an array to contain selection arguments
 			String[] mSelectionArgs = { "" };
 			String sortOrder = DATE_TIME + " DESC";
 			Cursor contactsCursor = null;
 			contactsCursor = context.getContentResolver().query(
 					ContactsProvider.CONTENT_URI, mProjection,
 					mSelectionClause, mSelectionArgs, sortOrder);
 
 			return contactsCursor;
 		}
 
 		@Override
 		protected void onPostExecute(Cursor result) {
 			// {ID, TOKEN, PUBLIC_KEY, NICKNAME };
 			while (!result.isAfterLast()) {
 				SignedObject token = (SignedObject)Helpers.deserialize(result.getBlob(1));
 				PublicKey key = (PublicKey) Helpers.deserialize(result.getBlob(2));
 				String nickname = result.getString(3);
 				CryptoContacts.Contact newContact = new CryptoContacts.Contact(
 						nickname, key, key, token);
 				CryptoContacts.addContact(newContact);
 			}
 		}
 	}
 }
