 /*
  * NotepadEditNote.java
  * Saturday, July 16th 2011
  *
  * Created by Jose Miguel Salcido Aguilar (jose152)
  * The brief description of the class is on Javadoc format.
  * 
  */
 // TODO Places and all these things
 
 package local.pruebas;
 
 import local.pruebas.adaptadores.DatabaseAdapter;
 import android.app.Activity;
 import android.content.Context;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.EditText;
import android.widget.Toast;
 
 /**
  * NotepadEditNote:
  * Activity destined to edit notes
  */
 public class NotepadEditNote extends Activity {
 	
 	private Context mContexto;
 	private DatabaseAdapter mDbAdapter;
 	private Long mRowId;
 	private int mBehaviour;
 	
 	// UI COMPONENTS
 	private EditText noteTitleText;
 	private EditText noteBodyText;
 	
 	// MENU OPTIONS EDIT/CREATE
 	private final static int MENU_SAVE = 0;
 	private final static int MENU_CANCEL = 1;
 
 	@Override
 	/**
      * onCreate:
      * Main method for the activity, you know the rest if not check developer.android.com
      */
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.createnote);
 		mContexto = getApplicationContext();
 		mDbAdapter = new DatabaseAdapter(mContexto);
 		mDbAdapter.open(NotepadUtils.OPTION_NOTES_DATABASE);
 		mBehaviour = setBehaviour(savedInstanceState);
 		mRowId = getRowId(savedInstanceState);
 		initUIElements();
 		fillApp();
 	}
 	
 	/**
 	 * initUIElements:
 	 * Initiate all the UI Elements for the activity.
 	 * @param behaviour
 	 */
 	private void initUIElements() {
 		noteTitleText = (EditText) findViewById(R.id.noteTitleText);
 		noteBodyText = (EditText) findViewById(R.id.noteBodyText);
 		// TODO Places and all these things
 	}
 	
 	@Override
 	/**
 	 * onRetainNonConfigurationInstance
 	 */
 	public Object onRetainNonConfigurationInstance() {
 		return mRowId;
 	}
 
 	/**
 	 * fillApp:
 	 * Fills all the activity data: title, body and place of the note with id: mRowId.
 	 */
 	private void fillApp() {
 		if (mRowId != null && mBehaviour == NotepadUtils.EDIT_NOTES) {
 			Cursor note = mDbAdapter.fetchNote(mRowId);
 			startManagingCursor(note);
 			// TODO Places
 			String title = note.getString(note.getColumnIndexOrThrow(NotepadUtils.KEY_TITLE_DATABASE));
 			String body  = note.getString(note.getColumnIndexOrThrow(NotepadUtils.KEY_BODY_DATABASE));
 			noteTitleText.setText(title);
 			noteBodyText.setText(body);
 		} else {
			// Nothing, if the rowId is NotepadUtils.ERROR, then it must be ADD_NOTE or ... just a bug.
 		}
 	}
 	
 	@Override
 	/**
 	 * onDestroy:
 	 */
 	protected void onDestroy() {
 		mDbAdapter.close();
 		super.onDestroy();
 	}
 
 	@Override
 	/**
 	 * onCreateOptionsMenu:
 	 * this method will create the menu for us
 	 */
 	public boolean onCreateOptionsMenu(Menu menu) {
 		int magic_number = 0; // ??, I have used 0 all the time here but I dont know why, so it is a magic number.
 		menu.add(magic_number, MENU_SAVE, MENU_SAVE, R.string.noteMenuSave);
 		menu.add(magic_number, MENU_CANCEL, MENU_CANCEL, R.string.noteMenuCancel);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	/**
 	 * onOptionsItemSelected:
 	 * handles the menu actions
 	 */
 	public boolean onOptionsItemSelected(MenuItem item) {
 		int MENU_OPTION = item.getItemId();
 		switch(MENU_OPTION) {
 		case MENU_SAVE:
 			save();
 			return true;
 		case MENU_CANCEL:
 			finish();
 			return true;
 		default:
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	/**
 	 * getBehaviour:
 	 * Return the behaviour of the note (Edit | View).
 	 * @param savedInstanceState
 	 * @return
 	 */
 	private int setBehaviour(Bundle savedInstanceState) {
 		Integer behaviour;
 		if (savedInstanceState == null) {
 			Bundle extras = getIntent().getExtras();
 			behaviour = extras.getInt(NotepadUtils.KEY_BEHAVIOUR_NOTE);
 		} else {
 			behaviour = (Integer) savedInstanceState.getSerializable(NotepadUtils.KEY_BEHAVIOUR_NOTE);
 		}
 		return (behaviour != null) ? behaviour : NotepadUtils.ERROR_NOTES;
 	}
 	
 	/**
 	 * getRowId(Bundle):
 	 * Return the rowId of the note (to edit or view)
 	 * @param savedInstanceState
 	 * @return
 	 */
 	private long getRowId(Bundle savedInstanceState) {
 		Long rowId;
 		if (savedInstanceState == null) {
 			Bundle extras = getIntent().getExtras();
 			rowId = extras.getLong(NotepadUtils.KEY_ROWID_DATABASE);
 		} else
 			rowId = (Long) savedInstanceState.getSerializable(NotepadUtils.KEY_ROWID_DATABASE);
 		return (rowId != null) ? rowId : NotepadUtils.ERROR;
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		outState.putLong(NotepadUtils.KEY_ROWID_DATABASE, mRowId);
 		outState.putInt(NotepadUtils.KEY_BEHAVIOUR_NOTE, mBehaviour);
 		super.onSaveInstanceState(outState);
 	}
 	
 	/**
 	 * save:
 	 * This method will add or update the note, It is not calling the onSaveStatus so, shit nigga, im gonna hack this.
 	 */
 	private void save() {
 		String body = noteBodyText.getText().toString();
 		String title = noteTitleText.getText().toString();
 		
 		// NO EMPTY NOTES HACK
		if (body.equals("") && title.equals("")) {
			Toast.makeText(mContexto, R.string.emptyNote, Toast.LENGTH_SHORT).show();
			return;
		}
 		
 		// Check if the behaviour is ADD_NOTE
 		if (mBehaviour == NotepadUtils.ADD_NOTE) {
 			long id = mDbAdapter.createNote(title, body);
 			if (id>0) {
 				mRowId = id;
				finish();
 			}
 		} else {
 			// UPDATE Note
 			mDbAdapter.updateNote(mRowId, title, body);
 		} 
 	}
 	
 }
