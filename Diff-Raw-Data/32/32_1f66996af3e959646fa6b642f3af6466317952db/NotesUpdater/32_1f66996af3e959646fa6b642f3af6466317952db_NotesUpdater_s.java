 package edu.illinois.messaging;
 
 import java.util.List;
 
 import edu.illinois.communication.Communicator;
 import edu.illinois.database.NoteContentProvider;
 import edu.illinois.database.SQLiteHelperMessage;
 import edu.illinois.digitalstickynotes.MainActivity;
 import android.content.ContentValues;
 import android.os.Handler;
 
 /**
  * @author tianyiw
  */
 public class NotesUpdater implements Runnable {
 
 	private MainActivity mainActivity;
 	private Communicator communicator;
 
 	private int updateInterval = 60000;			// 60 seconds by default.
 	private Handler updateHandler;
 
 	/**
 	 * Insert the newly received note into the database.
 	 * ListView should automatically refresh.
 	 * 
 	 * @param note the {@link Note} to be inserted into the database.
 	 */
 	private void insertNote(Note note) {
 		String title = note.getTitle();
 		String message = note.getMessage();
 		String sender = note.getSender().getUserName();
 		String availableTime = note.getAvailableDateString();
 		String receivedTime = note.getReceivedDateString();
 		String expireTime = note.getExpireDateString();
 
 		ContentValues values = new ContentValues();
 		values.put(SQLiteHelperMessage.COLUMN_TITLE, title);
 		values.put(SQLiteHelperMessage.COLUMN_MESSAGE, message);
 		values.put(SQLiteHelperMessage.COLUMN_SENDER, sender);
 		values.put(SQLiteHelperMessage.COLUMN_AVAILABLE_TIME, availableTime);
 		values.put(SQLiteHelperMessage.COLUMN_RECEIVED_TIME, receivedTime);
 		values.put(SQLiteHelperMessage.COLUMN_EXPIRE_TIME, expireTime);
 
 		mainActivity.getContentResolver().insert(NoteContentProvider.CONTENT_URI, values);
 	}
 
 	private void updateMessages() {
 	
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
 		updateMessages();
 		
 		updateHandler.postDelayed(this, updateInterval);
 	}
 	
 	public void start() {
 		updateHandler.postDelayed(this, 0);
 	}
 	
 	public void terminate() {
 		updateHandler.removeCallbacks(this);
 	}
 	
 	public NotesUpdater(MainActivity mainActivity, Communicator communicator) {
 		this.mainActivity = mainActivity;
 		this.communicator = communicator;
 
 		this.updateHandler = new Handler();
 	}
 }
