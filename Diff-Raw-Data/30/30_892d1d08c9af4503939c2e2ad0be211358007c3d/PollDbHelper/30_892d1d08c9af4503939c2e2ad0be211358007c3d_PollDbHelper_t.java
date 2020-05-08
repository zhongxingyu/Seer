 package ch.bfh.evoting.votinglib.db;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ch.bfh.evoting.votinglib.entities.DatabaseException;
 import ch.bfh.evoting.votinglib.entities.Option;
 import ch.bfh.evoting.votinglib.entities.Poll;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 /**
  * Helper class to access to the application database
  * @author Philémon von Bergen
  *
  */
 public class PollDbHelper extends SQLiteOpenHelper {
 	
 	private static final String TAG = PollDbHelper.class.getSimpleName();
 
 	// Basic DB parameters
 	private static final String DATABASE_NAME = "poll_options.db";
 	private static final int DATABASE_VERSION = 2;
 
 	// Table names
 	private static final String TABLE_NAME_POLLS = "poll";
 	private static final String TABLE_NAME_OPTIONS = "option";
 
 	// Attributes of the poll table
 	private static final String POLL_ID = "id";
 	private static final String POLL_QUESTION = "question";
 	private static final String POLL_START_TIME = "start_time";
 	private static final String POLL_IS_TERMINATED = "is_terminated";
 	private static final String POLL_NUMBER_PARTICIPANTS = "number_participants";
 
 	// Attributes of the results table
 	private static final String OPTION_ID = "id";
 	private static final String OPTION_POLL_ID = "poll_id";
 	private static final String OPTION_TEXT = "text";
 	private static final String OPTION_NUMBER_OF_VOTES = "nbr_votes";
 	private static final String OPTION_PERCENTAGE = "percentage";
 	
 	private static PollDbHelper instance;
 
 	/**
 	 * Private constructor of this helper
 	 * @param context
 	 */
 	private PollDbHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		
 	}
 
 	/**
 	 * Return the instance of the DB Helper
 	 * @param ctx android context
 	 * @return a DB hepler object
 	 */
 	public static PollDbHelper getInstance(Context ctx) {
 
 		if (instance == null) {
 			instance = new PollDbHelper(ctx);
 		}
 		return instance;
 	}
 	
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		
 		// Create the schema if it is not there
 		String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_POLLS
 				+ " (" + POLL_ID + " INTEGER PRIMARY KEY, "
 				+ POLL_QUESTION + " TEXT, "
 				+ POLL_START_TIME + " INTEGER, "
 				+ POLL_IS_TERMINATED + " INTEGER, "
 				+ POLL_NUMBER_PARTICIPANTS + " INTEGER);";
 
 		db.execSQL(sql);
 
 		sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_OPTIONS + " ("
 				+ OPTION_ID + " INTEGER PRIMARY KEY, "
 				+ OPTION_POLL_ID + " INTEGER, "
 				+ OPTION_TEXT + " TEXT, "
 				+ OPTION_NUMBER_OF_VOTES + " INTEGER, "
 				+ OPTION_PERCENTAGE + " REAL, "
 				+ "FOREIGN KEY(" + OPTION_POLL_ID
 				+ ") REFERENCES " + TABLE_NAME_POLLS + "("
 				+ POLL_ID + "));";
 
 		db.execSQL(sql);
 
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// drop the schema if there is a new version
 		Log.w(this.getClass().getSimpleName(), "DB Upgrade from Version " + oldVersion + " to version "
 				+ newVersion);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_POLLS);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_OPTIONS);
 		onCreate(db);
 	}
 	
 	/**
 	 * Get all non-terminated poll
 	 * @return a list of non terminated polls
 	 */
 	public List<Poll> getAllOpenPolls(){
 		SQLiteDatabase db = getReadableDatabase();
 
 		String sql1 = "SELECT * FROM " + TABLE_NAME_POLLS + " WHERE "+ POLL_IS_TERMINATED +"=0 ORDER BY ID ASC;";
 		Cursor c1 = db.rawQuery(sql1, null);
 
 		List<Poll> polls = new ArrayList<Poll>();
 
 		c1.moveToFirst();
 		while(!c1.isAfterLast()){
 			Poll poll = new Poll();
 			poll.setId(c1.getInt(c1.getColumnIndex(POLL_ID)));
 			poll.setQuestion(c1.getString(c1.getColumnIndex(POLL_QUESTION)));
 			poll.setStartTime(c1.getLong(c1.getColumnIndex(POLL_START_TIME)));
 			poll.setTerminated(c1.getInt(c1.getColumnIndex(POLL_IS_TERMINATED)) == 1);
			poll.setNumberOfParticipants(c1.getInt(c1.getColumnIndex(POLL_NUMBER_PARTICIPANTS)));
 			String sql2 = "SELECT * FROM " + TABLE_NAME_OPTIONS + " WHERE " + OPTION_POLL_ID + "=" + c1.getInt(c1.getColumnIndex(POLL_ID)) + " ORDER BY ID ASC;";
 			Cursor c2 = db.rawQuery(sql2, null);
 
 			List<Option> options = new ArrayList<Option>();
 
 			c2.moveToFirst();
 			while(!c2.isAfterLast()){
 				Option option = new Option();
 				option.setId(c2.getInt(c2.getColumnIndex(OPTION_ID)));
 				option.setText(c2.getString(c2.getColumnIndex(OPTION_TEXT)));
 				option.setPollId(c2.getInt(c2.getColumnIndex(OPTION_POLL_ID)));
 				option.setVotes(c2.getInt(c2.getColumnIndex(OPTION_NUMBER_OF_VOTES)));
 				option.setPercentage(c2.getDouble(c2.getColumnIndex(OPTION_PERCENTAGE)));
 				options.add(option);
 				c2.moveToNext();
 			}
 			c2.close();
 
 			poll.setOptions(options);
 
 			polls.add(poll);
 			c1.moveToNext();
 		}
 
 		c1.close();
 		db.close();
 		return polls;
 	}
 	
 	/**
 	 * Get all terminated polls
 	 * @return a list of terminated polls
 	 */
 	public List<Poll> getAllTerminatedPolls(){
 		SQLiteDatabase db = getReadableDatabase();
 
 		String sql1 = "SELECT * FROM " + TABLE_NAME_POLLS + " WHERE "+ POLL_IS_TERMINATED +"=1 ORDER BY ID ASC;";
 		Cursor c1 = db.rawQuery(sql1, null);
 
 		List<Poll> polls = new ArrayList<Poll>();
 
 		c1.moveToFirst();
 		while(!c1.isAfterLast()){
 			Poll poll = new Poll();
 			poll.setId(c1.getInt(c1.getColumnIndex(POLL_ID)));
 			poll.setQuestion(c1.getString(c1.getColumnIndex(POLL_QUESTION)));
 			poll.setStartTime(c1.getLong(c1.getColumnIndex(POLL_START_TIME)));
 			poll.setTerminated(c1.getInt(c1.getColumnIndex(POLL_IS_TERMINATED)) == 1);
			poll.setNumberOfParticipants(c1.getInt(c1.getColumnIndex(POLL_NUMBER_PARTICIPANTS)));
 
 			String sql2 = "SELECT * FROM " + TABLE_NAME_OPTIONS + " WHERE " + OPTION_POLL_ID + "=" + c1.getInt(c1.getColumnIndex(POLL_ID)) + " ORDER BY ID ASC;";
 			Cursor c2 = db.rawQuery(sql2, null);
 
 			List<Option> options = new ArrayList<Option>();
 
 			c2.moveToFirst();
 			while(!c2.isAfterLast()){
 				Option option = new Option();
 				option.setId(c2.getInt(c2.getColumnIndex(OPTION_ID)));
 				option.setText(c2.getString(c2.getColumnIndex(OPTION_TEXT)));
 				option.setPollId(c2.getInt(c2.getColumnIndex(OPTION_POLL_ID)));
 				option.setVotes(c2.getInt(c2.getColumnIndex(OPTION_NUMBER_OF_VOTES)));
 				option.setPercentage(c2.getDouble(c2.getColumnIndex(OPTION_PERCENTAGE)));
 				options.add(option);
 				c2.moveToNext();
 			}
 			c2.close();
 
 			poll.setOptions(options);
 
 			polls.add(poll);
 			c1.moveToNext();
 		}
 
 		c1.close();
 		db.close();
 		return polls;
 	}
 
 	/**
 	 * Get all terminated and non-terminated polls
 	 * @return list of all polls
 	 */
 	public List<Poll> getAllPolls(){
 		SQLiteDatabase db = getReadableDatabase();
 
 		String sql1 = "SELECT * FROM " + TABLE_NAME_POLLS + " ORDER BY ID ASC;";
 		Cursor c1 = db.rawQuery(sql1, null);
 
 		List<Poll> polls = new ArrayList<Poll>();
 
 		c1.moveToFirst();
 		while(!c1.isAfterLast()){
 			Poll poll = new Poll();
 			poll.setId(c1.getInt(c1.getColumnIndex(POLL_ID)));
 			poll.setQuestion(c1.getString(c1.getColumnIndex(POLL_QUESTION)));
 			poll.setStartTime(c1.getLong(c1.getColumnIndex(POLL_START_TIME)));
 			poll.setTerminated(c1.getInt(c1.getColumnIndex(POLL_IS_TERMINATED)) == 1);
			poll.setNumberOfParticipants(c1.getInt(c1.getColumnIndex(POLL_NUMBER_PARTICIPANTS)));
 
 			String sql2 = "SELECT * FROM " + TABLE_NAME_OPTIONS + " WHERE " + OPTION_POLL_ID + "=" + c1.getInt(c1.getColumnIndex(POLL_ID)) + " ORDER BY ID ASC;";
 			Cursor c2 = db.rawQuery(sql2, null);
 
 			List<Option> options = new ArrayList<Option>();
 
 			c2.moveToFirst();
 			while(!c2.isAfterLast()){
 				Option option = new Option();
 				option.setId(c2.getInt(c2.getColumnIndex(OPTION_ID)));
 				option.setText(c2.getString(c2.getColumnIndex(OPTION_TEXT)));
 				option.setPollId(c2.getInt(c2.getColumnIndex(OPTION_POLL_ID)));
 				option.setVotes(c2.getInt(c2.getColumnIndex(OPTION_NUMBER_OF_VOTES)));
 				option.setPercentage(c2.getDouble(c2.getColumnIndex(OPTION_PERCENTAGE)));
 				options.add(option);
 				c2.moveToNext();
 			}
 			c2.close();
 
 			poll.setOptions(options);
 
 			polls.add(poll);
 			c1.moveToNext();
 		}
 
 		c1.close();
 		db.close();
 		return polls;
 	}
 
 	/**
 	 * Get the poll with the given id
 	 * @param pollId id of the poll wanted
 	 * @return the poll object
 	 */
 	public Poll getPoll(int pollId){
 		SQLiteDatabase db = getReadableDatabase();
 
 		String sql1 = "SELECT * FROM " + TABLE_NAME_POLLS + " WHERE " + POLL_ID + "=" + pollId + " ORDER BY ID ASC;";
 		Cursor c1 = db.rawQuery(sql1, null);
 		
 		Poll poll = new Poll();
 
 		c1.moveToFirst();
 		while(!c1.isAfterLast()){
 			poll.setId(c1.getInt(c1.getColumnIndex(POLL_ID)));
 			poll.setQuestion(c1.getString(c1.getColumnIndex(POLL_QUESTION)));
 			poll.setStartTime(c1.getLong(c1.getColumnIndex(POLL_START_TIME)));
 			poll.setTerminated(c1.getInt(c1.getColumnIndex(POLL_IS_TERMINATED)) == 1);
			poll.setNumberOfParticipants(c1.getInt(c1.getColumnIndex(POLL_NUMBER_PARTICIPANTS)));
 
 			String sql2 = "SELECT * FROM " + TABLE_NAME_OPTIONS + " WHERE " + OPTION_POLL_ID + "=" + c1.getInt(c1.getColumnIndex(POLL_ID)) + " ORDER BY ID ASC;";
 			Cursor c2 = db.rawQuery(sql2, null);
 
 			List<Option> options = new ArrayList<Option>();
 
 			c2.moveToFirst();
 			while(!c2.isAfterLast()){
 				Option option = new Option();
 				option.setId(c2.getInt(c2.getColumnIndex(OPTION_ID)));
 				option.setText(c2.getString(c2.getColumnIndex(OPTION_TEXT)));
 				option.setPollId(c2.getInt(c2.getColumnIndex(OPTION_POLL_ID)));
 				option.setVotes(c2.getInt(c2.getColumnIndex(OPTION_NUMBER_OF_VOTES)));
 				option.setPercentage(c2.getDouble(c2.getColumnIndex(OPTION_PERCENTAGE)));
 				options.add(option);
 				c2.moveToNext();
 			}
 			c2.close();
 
 			poll.setOptions(options);
 			
 			c1.moveToNext();
 		}
 
 		c1.close();
 		db.close();
 		return poll;
 	}
 	
 	/**
 	 * Save a poll into the database
 	 * @param poll the poll to save
 	 * @return the id of the row where the poll has been inserted
 	 * @throws DatabaseException thrown when an error occurred inserting the record in the db
 	 */
 	public long savePoll(Poll poll) throws DatabaseException{
 		SQLiteDatabase db = getWritableDatabase();
 		ContentValues valuesPoll = new ContentValues();
 		valuesPoll.put(POLL_QUESTION, poll.getQuestion());
 		valuesPoll.put(POLL_START_TIME, poll.getStartTime());
 		valuesPoll.put(POLL_IS_TERMINATED, poll.isTerminated());
 		valuesPoll.put(POLL_NUMBER_PARTICIPANTS, poll.getNumberOfParticipants());
 		
 		long rowId = db.insert(TABLE_NAME_POLLS, null, valuesPoll);
 		poll.setId((int)(rowId));
 
 		long rowId2 = 0;
 		for(Option option : poll.getOptions()){
 			ContentValues valuesOption = new ContentValues();
 			valuesOption.put(OPTION_POLL_ID, rowId);
 			valuesOption.put(OPTION_TEXT, option.getText());
 			valuesOption.put(OPTION_NUMBER_OF_VOTES, option.getVotes());
 			valuesOption.put(OPTION_PERCENTAGE, option.getPercentage());
 
 			rowId2 = db.insert(TABLE_NAME_OPTIONS, null, valuesOption);
 		}
 		db.close();
 
 		if(rowId==-1 || rowId2 == -1){
 			throw new DatabaseException("Error while saving terminated poll!");
 		}
 		return rowId;
 	}
 	
 	/**
 	 * Update an already existing poll in the DB
 	 * @param pollId id of the poll to update
 	 * @param poll the poll object containing the data to update
 	 * @throws DatabaseException thrown when an error occurred inserting the record in the db
 	 */
 	public void updatePoll(int pollId, Poll poll) throws DatabaseException{
 		SQLiteDatabase db = getWritableDatabase();
 		String strFilter = POLL_ID + "=" + pollId;
 		ContentValues valuesPoll = new ContentValues();
 		valuesPoll.put(POLL_QUESTION, poll.getQuestion());
 		valuesPoll.put(POLL_START_TIME, poll.getStartTime());
 		valuesPoll.put(POLL_IS_TERMINATED, poll.isTerminated());
 		valuesPoll.put(POLL_NUMBER_PARTICIPANTS, poll.getNumberOfParticipants());
 		db.update(TABLE_NAME_POLLS, valuesPoll, strFilter, null);
 		
 		Log.d(TAG, "I have " + poll.getOptions().size() + " Options in the update.");
		Log.d(TAG, "I have " + poll.getNumberOfParticipants() + " Participants in the update.");
 		
 		//Delete actual options and put the new options
 		db.delete(TABLE_NAME_OPTIONS, OPTION_POLL_ID + "=" + pollId, null);
 		
 		long rowId2 = -1;
 		for(Option option : poll.getOptions()){
 			
 			ContentValues valuesOption = new ContentValues();
 			valuesOption.put(OPTION_POLL_ID, pollId);
 			valuesOption.put(OPTION_TEXT, option.getText());
 			valuesOption.put(OPTION_NUMBER_OF_VOTES, option.getVotes());
 			valuesOption.put(OPTION_PERCENTAGE, option.getPercentage());
 
 			rowId2 = db.insert(TABLE_NAME_OPTIONS, null, valuesOption);
 		}
 		
 		if(rowId2 == -1 && poll.getOptions().size()!=0){
 			throw new DatabaseException("Error while saving terminated poll!");
 		}
 		
 		db.close();
 	}
 
 	/**
 	 * Get the number of open polls in the database
 	 * @return the number of open polls in the database
 	 */
 	public int getNumberOfOpenPolls(){
 		String countQuery = "SELECT  * FROM " + TABLE_NAME_POLLS + " WHERE "+ POLL_IS_TERMINATED +"=0 ORDER BY ID ASC;";
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(countQuery, null);
 		int count = cursor.getCount();
 		cursor.close();
 		db.close();
 
 		return count;
 	}
 	
 	/**
 	 * Get the number of terminated poll in the database
 	 * @return the number of terminated poll in the database
 	 */
 	public int getNumberOfTerminatedPolls(){
 		String countQuery = "SELECT  * FROM " + TABLE_NAME_POLLS + " WHERE "+ POLL_IS_TERMINATED +"=1 ORDER BY ID ASC;";
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(countQuery, null);
 		int count = cursor.getCount();
 		cursor.close();
 		db.close();
 
 		return count;
 	}
 	
 	/**
 	 * Get the total number of poll
 	 * @return the total number of poll
 	 */
 	public int getNumberOfPolls(){
 		String countQuery = "SELECT  * FROM " + TABLE_NAME_POLLS + " ORDER BY ID ASC;";
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(countQuery, null);
 		int count = cursor.getCount();
 		cursor.close();
 		db.close();
 
 		return count;
 	}
 
 	/**
 	 * Get the number of option for the given poll
 	 * @param pollId id of the poll
 	 * @return number of option of this poll
 	 */
 	public int getNumberOfOptionsForPoll(int pollId){
 		String countQuery = "SELECT  * FROM " + TABLE_NAME_OPTIONS + " WHERE " + OPTION_POLL_ID + "=" + pollId + " ORDER BY ID ASC;";
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(countQuery, null);
 		int count = cursor.getCount();
 		cursor.close();
 		db.close();
 
 		return count;
 	}
 	
 	/**
 	 * Delete a poll from the db
 	 * @param pollId id of the poll to delete
 	 */
 	public void deletePoll(int pollId){
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		db.delete(TABLE_NAME_OPTIONS, OPTION_POLL_ID + "=" + pollId, null);
 		db.delete(TABLE_NAME_POLLS, POLL_ID + "=" + pollId, null);
 
 		db.close();
 	}
 }
