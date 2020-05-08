 package tasktracker.controller;
 
 import tasktracker.model.DatabaseModel;
 import tasktracker.model.elements.Task;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 public class DatabaseAdapter {
 
 	public DatabaseModel databaseModel = new DatabaseModel();
 	public static final String ID = "_id";
 	public static final String DATE = "date";
 	public static final String TASK = "task";
 	public static final String TASK_ID = "task_id";
 	public static final String PHOTO = "photo";
 	public static final String USER = "user";
 	public static final String USER_ID = "user_id";
 	public static final String TEXT = "text";
 	public static final String REQS_PHOTO = "requiresPhoto";
 	public static final String REQS_TEXT = "requiresText";
 	public static final String MEMBERS = "members";
 	public static final String EMAIL = "email";
 	public static final String PASSWORD = "password";
 	public static final String PRIVATE = "private";
 	public static final String COUNT = "count";
 	public static final String DOWNLOADED = "downloaded";
 	//public static final String DESCRIPTION = "description";
 
 	private SQLiteDatabase mDb;
 
 	private static final String TABLE_PHOTOS = "photos";
 	private static final String TABLE_TASKS = "tasks";
 	private static final String TABLE_MEMBERS = "members";
 	private static final String TABLE_USERS = "users";
 	private static final String TABLE_FULFILLMENTS = "fulfillments";
 	private static final String TABLE_NOTIFICATIONS = "notifications";
 	private static final String TABLE_VOTES = "votes";
 
 	/**
 	 * Constructor - takes the context to allow the database to be
 	 * opened/created and gives it to the model
 	 * 
 	 * @param ctx
 	 *            the Context within which to work
 	 */
 	public DatabaseAdapter(Context ctx) {
 		Log.d("DEBUG", "DatabaseAdapter(Context)");
 		databaseModel.setMCtx(ctx);
 	}
 
 	/**
 	 * Open the entries database. If it cannot be opened, try to create a new
 	 * instance of the database. If it cannot be created, throw an exception to
 	 * signal the failure
 	 * 
 	 * @return this (self reference, allowing this to be chained in an
 	 *         initialization call)
 	 * @throws SQLException
 	 *             if the database could be neither opened or created
 	 */
 	public DatabaseAdapter open() throws SQLException {
 		return databaseModel.open(this);
 	}
 
 	/**
 	 * Closes the database
 	 */
 	public void close() {
 		databaseModel.close();
 	}
 
 	/**
 	 * Create a new entry using the information provided. If the entry is
 	 * successfully created return the new rowId for that entry, otherwise
 	 * return a -1 to indicate failure.
 	 * 
 	 * @param date
 	 *            the date (in yyyy-mm-dd format)
 	 * @param folder
 	 *            the folder the photo is in
 	 * @param tag
 	 *            the tag the photo is under
 	 * @param photo
 	 *            the photo in byte array format
 	 * @return rowId or -1 if failed
 	 */
 	public long createPhoto(String date, String taskID, byte[] photo) {
 		ContentValues initialValues = new ContentValues();
 
 		initialValues.put(DATE, date);
 		initialValues.put(PHOTO, photo);
 		initialValues.put(TASK_ID, taskID);
 
 		return mDb.insert(TABLE_PHOTOS, null, initialValues);
 	}
 
 	/**
 	 * Create a new entry using the information provided. If the entry is
 	 * successfully created return the new rowId for that entry, otherwise
 	 * return a -1 to indicate failure.
 	 * 
 	 * @param folder
 	 *            the folder
 	 * @return rowId or -1 if failed
 	 */
 	public long createTask(Task task) {
 		ContentValues initialValues = new ContentValues();
 		
 		initialValues.put(ID, task.getID());
 		initialValues.put(TASK, task.getName());
 		initialValues.put(DATE, task.getDateCreated());
 		initialValues.put(USER, task.getCreator());
 		initialValues.put(TEXT, task.getDescription());
 		initialValues.put(REQS_PHOTO, task.requiresPhoto() ? 1 : 0);
 		initialValues.put(REQS_TEXT, task.requiresText() ? 1 : 0);
 		initialValues.put(PRIVATE, task.isPrivate() ? 1 : 0);
 		initialValues.put(DOWNLOADED, task.getDownloaded());	//Added nov29 -mike
 
 		Log.d("DatabaseAdapter",
 				"PRIVATE = " + Boolean.toString(task.isPrivate()));
 
 		return mDb.insert(TABLE_TASKS, null, initialValues);
 	}
 
 	public long createNotification(String taskID, String recipient, String message) {
 		ContentValues initialValues = new ContentValues();
 
 		initialValues.put(TASK_ID, taskID);
 		initialValues.put(USER, recipient);
 		initialValues.put(TEXT, message);
 
 		return mDb.insert(TABLE_NOTIFICATIONS, null, initialValues);
 	}
 
 	/*
 	 * For creating a new user entry in the local SQL DB at the given ID
 	 */
 	public long createUser(String user, String email, String password, String id) {
 		ContentValues initialValues = new ContentValues();
 
 		initialValues.put(ID, id);
 		initialValues.put(USER, user);
 		initialValues.put(EMAIL, email);
 		initialValues.put(PASSWORD, password);
 		//TODO enter this user under the given ID
 
 		return mDb.insert(TABLE_USERS, null, initialValues);
 	}
 	
 	//TODO: Remove all references to this and delete it
 	public long createUser(String user, String email, String password) {
 		ContentValues initialValues = new ContentValues();
 
 		initialValues.put(USER, user);
 		initialValues.put(EMAIL, email);
 		initialValues.put(PASSWORD, password);
 
 		return mDb.insert(TABLE_USERS, null, initialValues);
 	}
 	
 	//TODO Method should be moved out of the create__() section of this module
 	public long updateUser(String user, String username, String email, String password){
 		//Untested!
 		//TODO Not used; Need to rearrange user system so that each user has a key corresponding to
 		//  the Crowdsourcer ID key given to that user item; currently, it just uses an incrementing
 		//  integer
 		ContentValues newValues = new ContentValues();
 		
 		newValues.put(USER, user);
 		newValues.put(EMAIL, email);
 		newValues.put(PASSWORD, password);
 		
 		return mDb.update(TABLE_USERS, newValues, USER+"='"+user+"'", null);
 	}
 
 	public long createFulfillment(String taskID, String date, String fulfiller,
 			String text) {
 		ContentValues initialValues = new ContentValues();
 
 		initialValues.put(TASK_ID, taskID);
 		initialValues.put(TEXT, text);
 		initialValues.put(USER, fulfiller);
 		initialValues.put(DATE, date);
 
 		return mDb.insert(TABLE_FULFILLMENTS, null, initialValues);
 	}
 
 	public long createMember(String taskID, String user) {
 		ContentValues initialValues = new ContentValues();
 
 		initialValues.put(TASK_ID, taskID);
 		initialValues.put(USER, user);
 
 		return mDb.insert(TABLE_MEMBERS, null, initialValues);
 	}
 
 	public long createVote(String taskID, String user) {
 		ContentValues initialValues = new ContentValues();
 
 		initialValues.put(TASK_ID, taskID);
 		initialValues.put(USER, user);
 
 		return mDb.insert(TABLE_VOTES, null, initialValues);
 	}
 
 	public void deleteVote(String taskID, String user) {
 		Cursor cursor = mDb.rawQuery("DELETE FROM " + TABLE_VOTES + " WHERE "
 				+ TASK_ID + "= " + taskID + " AND " + USER + " = ?",
 				new String[] { user });
 		Log.d("DatabaseAdapter", "Cursor Delete Vote: " + cursor.toString());
 	}
 	/**
 	 * Delete the entry with the given rowId
 	 * 
 	 * @param rowId
 	 *            id of entry to delete
 	 * @return true if deleted, false otherwise
 	 */
 	public boolean deletePhoto(long rowId) {
 		return mDb.delete(TABLE_PHOTOS, ID + "=" + rowId, null) > 0;
 	}
 
 	/**
 	 * Delete the entry with the given rowId
 	 * 
 	 * @param rowId
 	 *            id of entry to delete
 	 * @return true if deleted, false otherwise
 	 */
 	public boolean deleteTask(long rowId) {
 		return mDb.delete(TABLE_TASKS, ID + "=" + rowId, null) > 0;
 	}
 
 	/**
 	 * Delete all of the photos in the given folder Id of folder in folder table
 	 * is given to streamline deletion process.
 	 * 
 	 * @param task
 	 *            name of folder to delete photos from
 	 * @return true if deleted, false otherwise
 	 */
 	public boolean deletePhotosInTask(String taskID) {
 		return mDb.delete(TABLE_PHOTOS, TASK_ID + "=" + taskID, null) > 0;
 	}
 
 	/**
 	 * Return a Cursor over the list of all folders in the table
 	 * 
 	 * @return Cursor over all folders
 	 */
 	public Cursor fetchAllTasks() {
 		return mDb.query(TABLE_TASKS, new String[] { ID, TASK, USER, DATE },
 				null, null, null, null, null);
 	}
 
 	public Cursor fetchTasksAvailableToUser(String user) {
 		return mDb
 				.rawQuery(
 						"SELECT DISTINCT _id, task, user, date, downloaded, text, CASE WHEN count IS NULL THEN 0 ELSE count END as count"
 								+ " FROM (SELECT t._id, t.task, t.user, t.date, t.downloaded, t.text"
 								+ " FROM tasks as t, members as m"
 								+ " WHERE t.private = 0 OR (t.private = 1 AND m.user = ? AND t._id = m.task_id)"
 								+ ") as available LEFT JOIN"
 								+ " (SELECT v.task_id as task_id, COUNT(v.user) as count"
 								+ " FROM tasks as t, votes as v"
 								+ " WHERE t._id = v.task_id GROUP BY v.task_id) as votecount"
 								+ " ON available._id = votecount.task_id",
 						new String[] { user });
 	}
 
 //	public Cursor fetchAllFulfillments() {
 //		return mDb.query(TABLE_FULFILLMENTS, new String[] { ID, TASK_ID, USER,
 //				DATE }, null, null, null, null, null);
 //	}
 
 	public Cursor fetchUserNotifications(String recipient) {
 		return mDb.rawQuery("SELECT * FROM " + TABLE_NOTIFICATIONS + " WHERE "
 				+ USER + " = ? ORDER BY " + ID + " DESC",
 				new String[] { recipient });
 	}
 
 	public Cursor fetchFulfillment(String taskID) {
 
 		return mDb
 				.query(TABLE_FULFILLMENTS, new String[] { ID, TASK_ID, DATE, USER,
 						TEXT }, TASK_ID + "=" + taskID, null, null, null, null,
 						null);
 	}
 
 	public Cursor fetchTask(String rowId) {
 		return mDb.query(TABLE_TASKS, new String[] { ID, TASK, DATE, USER,
				TEXT, REQS_PHOTO, REQS_TEXT, PRIVATE }, ID + "=" + rowId, null,	//TODO think error lies here -Mike
 				null, null, null, null);
 	}
 
 	public Cursor fetchUserViaID(String rowId) {
 		return mDb.query(TABLE_USERS, new String[] { ID, USER, EMAIL }, ID
 				+ "=" + rowId, null, null, null, null, null);
 	}
 
 	/**
 	 * To validate login
 	 * 
 	 * @param user
 	 * @param password
 	 * @return
 	 */
 	public Cursor fetchUser(String user, String password) {
 		return mDb.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + USER
 				+ " = ? AND " + PASSWORD + " = ?", new String[] { user,
 				password });
 	}
 
 	/**
 	 * To check if a username is available
 	 * 
 	 * @param user
 	 * @return
 	 */
 	public Cursor fetchUserViaName(String user) {
 		return mDb.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + USER
 				+ " = ?", new String[] { user });
 	}
 
 
 	/**
 	 * Returns a cursor that points to data with the requested tag
 	 * 
 	 * @param task
 	 *            retrieve photos with given task
 	 * @return Cursor that traverses photos with given task
 	 */
 	public Cursor fetchPhotosUnderTask(String taskID) {
 		Cursor mCursor = mDb.query(true, TABLE_PHOTOS, new String[] { ID, DATE,
 				TASK_ID, PHOTO }, TASK_ID + "=" + taskID, null, null, null, null,
 				null);
 
 		return mCursor;
 	}
 
 	public Cursor fetchTaskMembers(String taskID) {
 		return mDb.rawQuery("SELECT DISTINCT * FROM " + TABLE_MEMBERS
 				+ " WHERE " + TASK_ID + " = " + taskID, new String[0]);
 	}
 
 	public Cursor countAllVotes(String taskID) {
 		return mDb.rawQuery("SELECT COUNT(*) FROM " + TABLE_VOTES + " WHERE "
 				+ TASK_ID + " = " + taskID, new String[0]);
 	}
 
 	public Cursor fetchVote(String taskID, String user) {
 		return mDb.rawQuery("SELECT * FROM " + TABLE_VOTES + " WHERE "
 				+ TASK_ID + " = " + taskID + " AND " + USER + " = ?",
 				new String[] { user });
 	}
 
 	/**
 	 * Sets the database
 	 * 
 	 * @param mDb
 	 *            Takes in the SQL database
 	 */
 	public void setMDb(SQLiteDatabase mDb) {
 		this.mDb = mDb;
 	}
 	
 	public void resetDatabase(){
 
 		for (String table : DatabaseModel.TABLE_NAMES) {
 			mDb.execSQL("DROP TABLE IF EXISTS " + table);
 			Log.d("RESET", table);
 		}
 
 		for (String createTable : DatabaseModel.CREATE_TABLES) {
 			mDb.execSQL(createTable);
 			Log.d("CREATE", createTable);
 		}
 	}
 
 }
