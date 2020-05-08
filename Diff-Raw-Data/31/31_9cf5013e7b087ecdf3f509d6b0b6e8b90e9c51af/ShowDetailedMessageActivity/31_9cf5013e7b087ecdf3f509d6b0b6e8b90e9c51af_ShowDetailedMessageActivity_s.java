 package edu.illinois.userinterfaces;
 
 import edu.illinois.database.NoteContentProvider;
 import edu.illinois.database.SQLiteHelperMessage;
 import edu.illinois.digitalstickynotes.R;
 import android.net.Uri;
 import android.os.Bundle;
 import android.app.Activity;
 import android.database.Cursor;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TextView;
 
 /**
  * Activity to show a single note.
  * 
  * @author tianyiw
  */
 public class ShowDetailedMessageActivity extends Activity {
 
 	// UI references.
 	private TextView messageTextView;
 	private TextView availableTextView;
 	private TextView receivedTextView;
 	private TextView expireTextView;
 	private TextView locationTextView;
 	private TextView senderTextView;
 
 	private Uri noteUri;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_show_detailed_message);
 
 		// Show the Up button in the action bar.
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 		messageTextView = (TextView) findViewById(R.id.message_content);
 		availableTextView = (TextView) findViewById(R.id.available_time);
 		receivedTextView = (TextView) findViewById(R.id.received_time);
 		expireTextView = (TextView) findViewById(R.id.expire_time);
 		locationTextView = (TextView) findViewById(R.id.received_loc);
 		senderTextView = (TextView) findViewById(R.id.sender);
 
 		Bundle extras = getIntent().getExtras();
 
 		if (extras != null) {
 			noteUri = extras.getParcelable(NoteContentProvider.CONTENT_ITEM_TYPE);
 			fillData(noteUri);
 		}
 	}
 
 	/**
 	 * Populate the current note.
 	 * 
 	 * @param uri the URI indicating the note.
 	 */
 	private void fillData(Uri uri) {
 
 		String[] projection = {SQLiteHelperMessage.COLUMN_TITLE, SQLiteHelperMessage.COLUMN_MESSAGE,
 				SQLiteHelperMessage.COLUMN_SENDER, SQLiteHelperMessage.COLUMN_RECEIVED_TIME,
 				SQLiteHelperMessage.COLUMN_AVAILABLE_TIME, SQLiteHelperMessage.COLUMN_EXPIRE_TIME,
 				SQLiteHelperMessage.COLUMN_LOCATION};
 
 		Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
 
 		if (cursor != null) {
 			cursor.moveToFirst();
 
 			setTitle(cursor.getString(cursor.
 					getColumnIndexOrThrow(SQLiteHelperMessage.COLUMN_TITLE)));
			messageTextView.setText("Note: " + cursor.
					getColumnIndexOrThrow(SQLiteHelperMessage.COLUMN_MESSAGE));
			availableTextView.setText("Available Time: " + cursor.
					getColumnIndexOrThrow(SQLiteHelperMessage.COLUMN_AVAILABLE_TIME));
			receivedTextView.setText("Received Time: " + cursor.
					getColumnIndexOrThrow(SQLiteHelperMessage.COLUMN_RECEIVED_TIME));
			expireTextView.setText("Expire Time: " + cursor.
					getColumnIndexOrThrow(SQLiteHelperMessage.COLUMN_EXPIRE_TIME));
			locationTextView.setText("Received location: " + cursor.
					getColumnIndexOrThrow(SQLiteHelperMessage.COLUMN_LOCATION));
			senderTextView.setText("Sender: " + cursor.
					getColumnIndexOrThrow(SQLiteHelperMessage.COLUMN_SENDER));
 
 			cursor.close();
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_show_detailed_message, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 }
