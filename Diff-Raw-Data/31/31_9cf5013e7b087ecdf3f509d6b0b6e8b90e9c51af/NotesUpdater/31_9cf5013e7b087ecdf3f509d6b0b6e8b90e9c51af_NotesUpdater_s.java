 package edu.illinois.messaging;
 
 import java.text.SimpleDateFormat;
 import java.util.List;
 import java.util.Locale;
 
 import edu.illinois.communication.Communicator;
 import edu.illinois.database.NoteContentProvider;
 import edu.illinois.database.SQLiteHelperMessage;
 import edu.illinois.digitalstickynotes.MainActivity;
 import android.content.ContentValues;
 import android.os.Handler;
 
 /**
  * Periodically check for user's notes in the nearby location.
  * 
  * @author tianyiw
  */
 public class NotesUpdater implements Runnable {
 
 	private MainActivity mainActivity;
 	private Communicator communicator;
 
 	private int updateInterval = 60000;			// 60 seconds by default.
 	private Handler updateHandler;
 
 	/**
 	 * Insert the newly received note into the local SQLite database.
 	 * ListView will automatically refresh.
 	 * 
 	 * @param note the {@link Note} to be inserted into the database.
 	 */
 	private void insertNote(Note note) {
 		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
 
 		String title = note.getTitle();
 		String message = note.getMessage();
 		String sender = note.getSender().getUserName();
 		String availableTime = simpleDateFormat.format(note.getAvailableDate());
 		String receivedTime = simpleDateFormat.format(note.getReceivedDate());
 		String expireTime = simpleDateFormat.format(note.getExpireDate());
 		String receivedLocation = note.getReceivedLocation();
 
 		ContentValues values = new ContentValues();
 		values.put(SQLiteHelperMessage.COLUMN_TITLE, title);
 		values.put(SQLiteHelperMessage.COLUMN_MESSAGE, message);
 		values.put(SQLiteHelperMessage.COLUMN_SENDER, sender);
 		values.put(SQLiteHelperMessage.COLUMN_AVAILABLE_TIME, availableTime);
 		values.put(SQLiteHelperMessage.COLUMN_RECEIVED_TIME, receivedTime);
 		values.put(SQLiteHelperMessage.COLUMN_EXPIRE_TIME, expireTime);
 		values.put(SQLiteHelperMessage.COLUMN_LOCATION, receivedLocation);
 
 		mainActivity.getContentResolver().insert(NoteContentProvider.CONTENT_URI, values);
 	}
 
 	/**
 	 * Update user's notes. Try to see if there's nearby location server.
 	 */
 	private void updateNotes() {
 	
 		if (communicator == null) {
 			return;
 		}
 
 		List<Note> notes = communicator.getNotes(mainActivity.getToken());
		
 		if (notes != null) {
 			for (Note note : notes) {
 				insertNote(note);
 			}
 		}
 	}
 	
 	@Override
 	public void run() {
 		updateNotes();
 		
 		updateHandler.postDelayed(this, updateInterval);
 	}
 	
 	/**
 	 * Start the notes updater.
 	 */
 	public void start() {
 		updateHandler.postDelayed(this, 0);
 	}
 	
 	/**
 	 * Stop the notes updater.
 	 */
 	public void terminate() {
 		updateHandler.removeCallbacks(this);
 	}
 	
 	/**
 	 * Constructor.
 	 * 
 	 * @param mainActivity the {@link MainActivity} object.
 	 * @param communicator the {@link Communicator} object.
 	 */
 	public NotesUpdater(MainActivity mainActivity, Communicator communicator) {
 		this.mainActivity = mainActivity;
 		this.communicator = communicator;
 
 		this.updateHandler = new Handler();
 	}
 }
