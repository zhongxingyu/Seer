 package noteBlock.hig.noteedit;
 
 import java.text.SimpleDateFormat;
 
 import noteBlock.hig.R;
 import noteBlock.hig.notepad.NotesDbAdapter;
 import android.content.Context;
 import android.database.Cursor;
 import android.util.Log;
 import android.widget.EditText;
 
 	
 /**
  * This is a mainly class for the NoteEdit activity 
  * This calss basicly helps with saving and updating the notes in the database
  *  Creation is also apart of it (the note)
  */
 public class NoteEditSavePopulate {
 	private NoteEdit noteEdit;
 	private NotesDbAdapter mDbHelper;
 	private Long mRowId;
 	private EditText mTitleText, mBodyText;
 
 	private String positionReminder = "false";
 	private String timeReminder = "false";
 	private String latitude = "lat", longitude = "long";
 	private String snippet = "";
 	private long time = 0;
 
 	public NoteEditSavePopulate(Context con, NotesDbAdapter dbHelper, Long rowId) {
 		noteEdit = (NoteEdit) con;
 		mDbHelper = dbHelper;
 		mRowId = rowId;
 		
 		mTitleText = (EditText) noteEdit.findViewById(R.id.title);
 		mBodyText = (EditText) noteEdit.findViewById(R.id.body);
 	}
 
 	public String getTitle(){
 		return mTitleText.getText().toString();
 	}
 	
 	public String getBody(){
 		return mBodyText.getText().toString();
 	}
 	
 	public String getPositionReminder() {
 		return positionReminder;
 	}
 
 	public long getTime() {
 		return time;
 	}
 
 	public String getLatitude() {
 		return latitude;
 	}
 
 	public String getLongitude() {
 		return longitude;
 	}
 
 	public String getSnippet() {
 		return snippet;
 	}
 
 	public String getTimeReminder() {
 		return timeReminder;
 	}
 	
 	public Long getRowId(){
 		return mRowId;
 	}
 
 	public void populateFields() {
 		if (mRowId != null) {
 			Cursor note = mDbHelper.fetchNote(mRowId);
 
 			mTitleText.setText(note.getString(note
 					.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
 			mBodyText.setText(note.getString(note
 					.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
 
 			positionReminder = note
 					.getString(note
 							.getColumnIndexOrThrow(NotesDbAdapter.KEY_POSITION_REMINDER));
 
 			time = note.getLong(note
 					.getColumnIndexOrThrow(NotesDbAdapter.KEY_TIME));
 
 			// Fetch the position values from the database:
 			latitude = note.getString(note
 					.getColumnIndexOrThrow(NotesDbAdapter.KEY_LATI));
 			longitude = note.getString(note
 					.getColumnIndexOrThrow(NotesDbAdapter.KEY_LONG));
 			snippet = note.getString(note
 					.getColumnIndexOrThrow(NotesDbAdapter.KEY_SNIPPET));
 
 			timeReminder = note.getString(note
 					.getColumnIndexOrThrow(NotesDbAdapter.KEY_TIME_REMINDER));
 			
 
 			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
 			Log.i("NoteEditPopulate", "time is: "+ dateFormat.format(time));
 			Log.i("NoteEditPopulate", "timereminder is: "+ timeReminder);
 		}
 	}
 
 	// Saves the values to the database
 	public void saveState() {
 
 		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
 		Log.i("NoteEditSave", "time is: "+ dateFormat.format(time));
 		Log.i("NoteEditSave", "timereminder is: "+ timeReminder);
 		if (mRowId == null) {
 			
 			Log.i("NoteEditSavePopulate", "Savestate. mRowId is null.");
 			
 			long id = mDbHelper.createNote(mTitleText.getText().toString(),
 					mBodyText.getText().toString(), time, longitude, latitude,
 					positionReminder, snippet, timeReminder);
 			 
 			if (id > 0) {
 				mRowId = id;
 			}
 			Log.i("Savepopul", "savestate. id is: " + mRowId);
 		} else {
 			mDbHelper.updateNote(mRowId, mTitleText.getText().toString(),
 					mBodyText.getText().toString(), time, longitude, latitude,
 					positionReminder, snippet, timeReminder);
 		}
 	}
 
 	/**
 	 * Saves the postion 
 	 * @param lati
 	 * @param longi
 	 * @param snipp
 	 * @param posReminder
 	 */
 	public void savePosition(String lati, String longi, String snipp,
 			String posReminder) {
 		System.out.println("savepos");
 		latitude = lati;
 		longitude = longi;
 		snippet = snipp;
 		positionReminder = posReminder;
 		saveState();
 	}
 
 	/**
 	 * Updates the postion
 	 * @param posReminder
 	 */
 	public void updatePositionNotify(String posReminder) {
 		System.out.println("updatepos");
 		positionReminder = posReminder;
 		mDbHelper.updatePositionNotification(mRowId, positionReminder);
 	}
 
 	/**
 	 * Updates the time reminder 
 	 * @param timReminder
 	 */
 	public void updateTimeNotification(String timReminder) {
 		System.out.println("updatetime");
 		timeReminder = timReminder;
 		mDbHelper.updateTimeNotification(mRowId, timReminder);
 	}
 
 	/**
 	 * 
 	 * @param tim Time gotten from user input
 	 * @param timReminder true or false
 	 */
 	public void saveTime(long tim, String timReminder) {
 		System.out.println("savetime");
 		time = tim;
 		timeReminder = timReminder;
 		saveState();
 	}
 
 	public void closeDB(){
 		mDbHelper.close();
 	}
 	
 	public boolean noTitle(){
 		if (getTitle().trim().matches("")
 		&& (!getBody().trim().matches("") || getLatitude() !="lat" || getTime()!=0))
 			return true;
 		return false;
 	}
 	
 	public void setTitle(String title){
 		mTitleText.setText(title);
 	}
 	
 	public boolean emptyNote(){
 		
 		if (getTitle().trim().matches("") && getBody().trim().matches("") && getLatitude().contains("lat") && getTime()==0){
 			Log.i("savepopulate" , "the note is empty");
 			return true;
 		}
 		Log.i("savepopulate" , "the note is not empty");
 		return false;
 	}
 }
