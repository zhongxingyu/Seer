 package com.meetme.app;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 
 public class MeetMeDbAdapter {
 	public static final String KEY_USERNAME = "username";
 	public static final String KEY_PASSWORD = "password";
 	public static final String KEY_NAME = "name";
 	public static final String KEY_COMPANY = "company";
 	public static final String KEY_POSITION = "position";
 	public static final String KEY_IMAGE = "image";
 	public static final String KEY_TWITTERNAME = "twittername";
 	public static final String KEY_TWITTERPASS = "twitterpass";
 
 
 	public static final String KEY_PHONE = "phonenumber";
 	public static final String KEY_WEB = "webpage";
 	public static final String KEY_MAIL = "mail";
 	
 
 
 	public static final String KEY_ROWID = "_id";
 	
 	private static final String TAG = "MeetMeDbAdapter";
 	private DatabaseHelper mDbHelper;
 	private SQLiteDatabase mDb;
 	
 	
 	private static final String DATABASE_CREATE_USERS = 
		"create table users (username primary key, password text not null)";
 
 	
 	private static final String DATABASE_CREATE_PROFILES = 
 		"create table profiles (username primary key, name text, company text, "
 		+ "position text, image blob, twittername text, twitterpass text)";
 	
 	private static final String DATABASE_CREATE_PHONES =
 		"create table phones (_id integer primary key autoincrement, "
 		+ "username text not null, phonenumber text not null)";
 	
 	private static String DATABASE_CREATE_WEBS = 
 		"create table webs (_id integer primary key autoincrement, "
 		+ "username text not null, webpage text not null)";
 	
 	private static final String DATABASE_CREATE_MAILS = 
 		"create table mails (_id integer primary key autoincrement, "
 		+ "username text not null, mail text not null)";
 	
 	private static final String DATABASE_NAME = "data";
     private static final String DATABASE_TABLE_USERS = "users";
     private static final String DATABASE_TABLE_PROFILES = "profiles";
     private static final String DATABASE_TABLE_PHONES = "phones";
 	private static final String DATABASE_TABLE_WEBS = "webs";
 	private static final String DATABASE_TABLE_MAILS = "mails";
 
     private static final int DATABASE_VERSION = 1;
 	
     private final Context mCtx;
     
     private static class DatabaseHelper extends SQLiteOpenHelper {
     	DatabaseHelper(Context context) {
     		super(context, DATABASE_NAME, null, DATABASE_VERSION);
     	}
     	
     	@Override
     	public void onCreate(SQLiteDatabase db) {
     		db.execSQL(DATABASE_CREATE_USERS);
     		db.execSQL(DATABASE_CREATE_PROFILES);
     		db.execSQL(DATABASE_CREATE_PHONES);
     		db.execSQL(DATABASE_CREATE_WEBS);
     		db.execSQL(DATABASE_CREATE_MAILS);
     	}
     	
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                     + newVersion + ", which will destroy all old data");
             db.execSQL("DROP TABLE IF EXISTS users");
             onCreate(db);			
 		}
     	
     }
     
     public MeetMeDbAdapter(Context ctx) {
     	mCtx = ctx;
     }
     
     public MeetMeDbAdapter open() throws SQLException {
         mDbHelper = new DatabaseHelper(mCtx);
         mDb = mDbHelper.getWritableDatabase();
         return this;
     }
  
     public void close() {
         mDbHelper.close();
     }
     
     
     
     
     
     
     
     
     /**
      * TAULA USERS: USERNAME I PASSWORD
      */
     
     /**
      * Crea un nou usuari.
      * @param username el nom d'usuari nic
      * @param password la contrassenya associada al nom d'usuari
      * @return rowId o -1 si ha fallat
      */
     public long createUser(String username, String password) {
     	ContentValues initialValues = new ContentValues();
     	initialValues.put(KEY_USERNAME, username);
     	initialValues.put(KEY_PASSWORD, password);
     	return mDb.insert(DATABASE_TABLE_USERS, null, initialValues);
     }
     
     public boolean updateUser(String username, String password) {
     	ContentValues args = new ContentValues();
     	args.put(KEY_USERNAME, username);
     	args.put(KEY_PASSWORD, password);
     	return mDb.update(DATABASE_TABLE_USERS, args, KEY_USERNAME + "=" + username, null) > 0;
     }
     
     public boolean deleteUser(String username) {
         return mDb.delete(DATABASE_TABLE_USERS, KEY_USERNAME + "=" + username, null) > 0;
     }
     
     /**
      * Retorna un cursor amb la password de l'usuari username.
      * @param username identificador de l'usuari
      * @return noms retorna la password perqu el username ja el sabem
      * @throws SQLException
      */
     public Cursor fetchUser(String username) throws SQLException{
    	Cursor cursor =
            mDb.query(true, DATABASE_TABLE_USERS, new String[] {KEY_PASSWORD}, KEY_USERNAME + "=" + username, null,
                     null, null, null, null);
         if (cursor != null) {
             cursor.moveToFirst();
         }
         return cursor;
     }
     
     
     
     
     
     
     /**
      * TAULA PROFILES: emmagatzema la informaci de perfil dels usuaris via username
      */
     
     
     public long createProfile(String username, String name, String company,
     		String position, String image, String twittername, String twitterpass) {
     	
     	ContentValues initialValues = new ContentValues();
     	initialValues.put(KEY_USERNAME, username);
     	initialValues.put(KEY_NAME, name);
     	initialValues.put(KEY_COMPANY, company);
     	initialValues.put(KEY_POSITION, position);
     	initialValues.put(KEY_IMAGE, image);
     	initialValues.put(KEY_TWITTERNAME, twittername);
     	initialValues.put(KEY_TWITTERPASS, twitterpass);
     	return mDb.insert(DATABASE_TABLE_PROFILES, null, initialValues);
     }
     
     public boolean updateProfile(String username, String name, String company,
     		String position, String image, String twittername, String twitterpass) {
     	ContentValues args = new ContentValues();
     	args.put(KEY_USERNAME, username);
     	args.put(KEY_NAME, name);
     	args.put(KEY_COMPANY, company);
     	args.put(KEY_POSITION, position);
     	args.put(KEY_IMAGE, image);
     	args.put(KEY_TWITTERNAME, twittername);
     	args.put(KEY_TWITTERPASS, twitterpass);
     	return mDb.update(DATABASE_TABLE_PROFILES, args, KEY_USERNAME + "=" + username, null) > 0;
     }
     
     
     public boolean deleteProfile(String username) {
         return mDb.delete(DATABASE_TABLE_PROFILES, KEY_USERNAME + "=" + username, null) > 0;
     }
     
     public Cursor fetchProfile(String username) throws SQLException {
     	Cursor cursor =
             mDb.query(true, DATABASE_TABLE_PROFILES, new String[] {KEY_COMPANY, KEY_POSITION,
             		KEY_IMAGE, KEY_TWITTERNAME, KEY_TWITTERPASS}, KEY_USERNAME + "=" + username,
             		null, null, null, null, null);
         if (cursor != null) {
             cursor.moveToFirst();
         }
         return cursor;
     }
     
     
     
     
     
     /**
      * TAULA PHONES: username + phone
      */
     
     
     /**
      * Es crea una fila a la taula phones per a representar que
      * l'usuari userId t el telfone phoneNumber.
      * @param userId la rowId de l'usuari
      * @param phoneNumber un dels nmeros de telfon de l'usuari
      * @return rowId o -1 si ha fallat
      */
     public long createPhone(String username, String phoneNumber) {
     	ContentValues initialValues = new ContentValues();
     	initialValues.put(KEY_USERNAME, username);
     	initialValues.put(KEY_PHONE, phoneNumber);
     	return mDb.insert(DATABASE_TABLE_PHONES, null, initialValues);
     	
     }
     
     
     /**
      * Esborra la fila de la taula phones amb id rowId
      * @param rowId id de la fila
      * @return cert si s'ha esborrat correctament
      */
     public boolean deletePhone(long rowId) {
         return mDb.delete(DATABASE_TABLE_PHONES, KEY_ROWID + "=" + rowId, null) > 0;
     }
     
     
     /**
      * Esborra el telfon phoneNumber de l'usuari username.
      * @param phoneNumber nmero de telfon a esborrar
      * @param username identificador de l'usuari associat al mail
      * @return cert si s'ha esborrat la fila correctament
      */
     public boolean deletePhoneOfUser(String phoneNumber, String username) {
     	return mDb.delete(DATABASE_TABLE_PHONES, KEY_USERNAME + "=" + username + 
     			" and " + KEY_PHONE + "=" + phoneNumber, null) > 0;
     }
     
     
     public boolean deletePhonesOfUser(String username) {
     	return mDb.delete(DATABASE_TABLE_PHONES, KEY_USERNAME + "=" + username, null) > 0;
     
     }
     
     /**
      * Retorna un cursor sobre els telfons d'un usuari.
      * @param userId id de la fila de l'usuari a la taula users
      * @return cursor sobre els telfons de l'usuari amb id userId
      * @throws SQLException
      */
     public Cursor fetchPhonesOf(String username) throws SQLException {
     	return mDb.query(true, DATABASE_TABLE_PHONES, new String[] {KEY_ROWID,
     			KEY_PHONE}, KEY_USERNAME + "=" + username, null, null, null, null, null);
     }	
     	
 
     	
     	
     	
     	
     /**
      * TAULA MAILS: username + mail	
      */
     	
 	/**
      * Es crea una fila a la taula mails per a representar que
      * l'usuaru userId t el mail mail.
      * @param userId la rowId de l'usuari
      * @param mail un dels mails de l'usuari
      * @return rowId o -1 si ha fallat
      */
     public long createMail(String username, String mail) {
     	ContentValues initialValues = new ContentValues();
     	initialValues.put(KEY_USERNAME, username);
     	initialValues.put(KEY_MAIL, mail);
     	return mDb.insert(DATABASE_TABLE_MAILS, null, initialValues);  	
     }
     
     /**
      * Esborra la fila de la taula mails amb id rowId
      * @param rowId id de la fila
      * @return cert si s'ha esborrat correctament
      */
     public boolean deleteMail(long rowId) {
         return mDb.delete(DATABASE_TABLE_MAILS, KEY_ROWID + "=" + rowId, null) > 0;
     }
     
     /**
      * Esborra el mail mail de l'usuari username.
      * @param mail mail a esborrar
      * @param username identificador de l'usuari associat al mail
      * @return cert si s'ha esborrat la fila correctament
      */
     public boolean deleteMailOfUser(String mail, String username) {
     	return mDb.delete(DATABASE_TABLE_MAILS, KEY_USERNAME + "=" + username + 
     			" and " + KEY_MAIL + "=" + mail, null) > 0;
     }
     
     
     public boolean deleteMailsOfUSer(String username) {
     	return mDb.delete(DATABASE_TABLE_MAILS, KEY_USERNAME + "=" + username, null) > 0;
     }
     
     
     /**
      * Retorna un cursor sobre els mails d'un usuari.
      * @param userId id de la fila de l'usuari a la taula users
      * @return cursor sobre els mails de l'usuari amb id userId
      * @throws SQLException
      */
     public Cursor fetchMailsOf(String username) throws SQLException {
     	return mDb.query(true, DATABASE_TABLE_MAILS, new String[] {KEY_ROWID,
     			KEY_MAIL}, KEY_USERNAME + "=" + username, null, null, null, null, null);    	
     }
     
     
     
     
     
     
     
     
     
     /**
      * TAULA WEBS: username + web
      */
     
     
     /**
      * Es crea una fila a la taula webs per a representar que
      * l'usuari userId t la web webPage.
      * @param userId la rowId de l'usuari
      * @param webPage una de les webs de l'usuari
      * @return rowId o -1 si ha fallat
      */
     public long createWeb(String username, String webPage) {
     	ContentValues initialValues = new ContentValues();
     	initialValues.put(KEY_USERNAME, username);
     	initialValues.put(KEY_WEB, webPage);
     	return mDb.insert(DATABASE_TABLE_WEBS, null, initialValues);
     	
     }
     
     /**
      * Esborra la fila de la taula webs amb id rowId
      * @param rowId id de la fila
      * @return cert si s'ha esborrat correctament
      */
     public boolean deleteWeb(long rowId) {
         return mDb.delete(DATABASE_TABLE_WEBS, KEY_ROWID + "=" + rowId, null) > 0;
     }
     
     
     /**
      * Esborra la web webPage de l'usuari username.
      * @param webPage web a esborrar
      * @param username identificador de l'usuari associat al mail
      * @return cert si s'ha esborrat la fila correctament
      */
     public boolean deleteWebOfUser(String webPage, String username) {
     	return mDb.delete(DATABASE_TABLE_WEBS, KEY_USERNAME + "=" + username + 
     			" and " + KEY_WEB + "=" + webPage, null) > 0;
     }
     
     
     public boolean deleteWebsOfUser(String username) {
     	return mDb.delete(DATABASE_TABLE_WEBS, KEY_USERNAME + "=" + username, null) > 0;
     }
     
     /**
      * Retorna un cursor sobre les webs d'un usuari.
      * @param userId id de la fila de l'usuari a la taula users
      * @return cursor sobre les webs de l'usuari amb id userId
      * @throws SQLException
      */
     public Cursor fetchWebsOf(String username) throws SQLException {
     	return mDb.query(true, DATABASE_TABLE_WEBS, new String[] {KEY_ROWID,
     			KEY_WEB}, KEY_USERNAME + "=" + username, null, null, null, null, null);    	
     }
     
     
 /*
     
     
     /**
      * Esborra un usuari per complet: la fila de la taula d'usuaris i les files
      * associades al mateix usuari de les taules phones, mails i webs.
      * @param rowId id de l'usuari que volem esborrar
      * @return cert si s'ha esborrat tota la informaci relativa a l'usuari
      */
     /*
     public boolean deleteUser(long rowId) {
         if (mDb.delete(DATABASE_TABLE_MAILS, KEY_USERID + "=" + rowId, null) <= 0) return false;
     	if (mDb.delete(DATABASE_TABLE_PHONES, KEY_USERID + "=" + rowId, null) <= 0) return false;
     	if (mDb.delete(DATABASE_TABLE_WEBS, KEY_USERID + "=" + rowId, null) <= 0) return false;
     	return mDb.delete(DATABASE_TABLE_USERS, KEY_ROWID + "=" + rowId, null) > 0;
     }
 
 */
         
 }
