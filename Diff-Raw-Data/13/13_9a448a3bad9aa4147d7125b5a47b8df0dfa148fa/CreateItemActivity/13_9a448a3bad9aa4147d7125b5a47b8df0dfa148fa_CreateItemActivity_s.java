 package ch.unibe.ese.shoppinglist;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class CreateItemActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_create_item);
 		// Show the Up button in the action bar.
 		getActionBar().hide();
 		
 		// get item name
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			// Get list
 			String name = extras.getString("Item");
 			EditText textName = (EditText) findViewById(R.id.editTextName);
 			textName.setText(name);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.create_item, menu);
 		return true;
 	}
 	
 	/** Called when the user touches the abort button */
 	public void goBack(View view) {
 		// TODO: make sure that the back button takes the user to the right list
		NavUtils.navigateUpFromSameTask(this);
 	}
 
 	/** Called when the user touches the save button */
 	public void saveItem(View view) {
 		// TODO: save item
 	}
 
 }
