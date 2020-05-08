 package info.bpace.caffeine;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.database.Cursor;
 import android.widget.EditText;
 import android.widget.Button;
 
 import android.view.View;
 import android.view.View.OnClickListener;
 
 /**
  * Activity for adding and editting items
  * in the database
  */
 public class EditItemActivity extends Activity implements OnClickListener
 {
 	private EditText mTitle;
 	private EditText mIngredients;
 	private EditText mBody;
 	private Long mId;
 	
 	private DBAdapter mDBAdapter;
 
 // -----------------------------------------------------------------------------
 // Public methods
 // -----------------------------------------------------------------------------
 
 	/**
 	 * Starts rendering main view, opens database connection
 	 * and sets mouse callback for submit button
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.edit_item);
 		
 		mDBAdapter = new DBAdapter(this);
 		mDBAdapter.open();
 		
 		mTitle = (EditText) findViewById(R.id.item_title);
 		mIngredients = (EditText) findViewById(R.id.item_ingredients);
 		mBody = (EditText) findViewById(R.id.item_body);
 		Button submit = (Button) findViewById(R.id.submit);
 		
 		// check if activity is resuming, and pull id from saved state
 		mId = savedInstanceState != null ? savedInstanceState.getLong(DBAdapter.KEY_ROWID)
 		                                 : null;
 		
 		if(mId == null)
 		{
 			// activity isn't resuming, try to pull id from intent
 			Bundle extras = getIntent().getExtras();
 			mId = extras != null ? extras.getLong(DBAdapter.KEY_ROWID)
 			                     : null;
 		}
 		
		populateFields();
 		
 		submit.setOnClickListener(this);
 	}
 	
 	/**
 	 * Submits form information and quits activity
 	 */
 	@Override
 	public void onClick(View view)
 	{
 		setResult(RESULT_OK);
 		saveState();
 		finish();
 	}
 
 // -----------------------------------------------------------------------------
 // Private methods
 // -----------------------------------------------------------------------------
 
 	/**
 	 */
	private void populateFields()
 	{
		if(mId != null)
 		{
 			Cursor c = mDBAdapter.readEntry(mId);
 			startManagingCursor(c);
 			
 			int title_index = c.getColumnIndexOrThrow(DBAdapter.KEY_TITLE);
 			int ingredients_index = c.getColumnIndexOrThrow(DBAdapter.KEY_INGREDIENTS);
 			int body_index = c.getColumnIndexOrThrow(DBAdapter.KEY_BODY);
 
 			mTitle.setText( c.getString(title_index) );
 			mIngredients.setText( c.getString(ingredients_index) );
 			mBody.setText( c.getString(body_index) );
 		}
 	}
 
 	/**
 	 * Write EditText fields out to database
 	 */
 	private void saveState()
 	{
 		String title = mTitle.getText().toString();
 		String ingredients = mIngredients.getText().toString();
 		String body = mBody.getText().toString();
 		
 		if(mId == null)
 		{
 			mDBAdapter.createEntry(title, ingredients, body);
 		}
 		else
 		{
 			mDBAdapter.updateEntry(mId, title, ingredients, body);
 		}
 	}
 }
