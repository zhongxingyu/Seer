 package net.gautampulla.shoppinglist;
 
 import net.gautampulla.shoppinglist.data.Item;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 
 public class ItemDetailActivity extends Activity {
 	public static final String ActivityModeProperty = "Type";
 	public static final int ActivityMode_NewItem = 0;
 	public static final int ActivityMode_ExistingItem = 1;
 	
 	public static final String NameProperty = "Name";
 	
 	private EditText _name;
 	private EditText _notes;
 	private String _nameText;
 	private Boolean _ischecked;
 	private int _mode;
 	
 	@TargetApi(11)
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.detail_item);
 		
 		this._name = (EditText)findViewById(R.id.detail_item_Name_EditText);
 		this._notes = (EditText)findViewById(R.id.detail_item_Notes_EditText);
 
 		// Check in what mode the detail view was opened in
 		// if an existing item then load it from the database
 		// TODO: refactor this as a separate method
 		Bundle extras = getIntent().getExtras();
 		_mode = extras.getInt(ItemDetailActivity.ActivityModeProperty);
 		if (_mode == ItemDetailActivity.ActivityMode_ExistingItem) {
 			_nameText = extras.getString(ItemDetailActivity.NameProperty);
 			Item item = null;
 			try {
 				item = MainActivity.Instance.ItemsTable.getItem(_nameText);
 				_ischecked = item.get_ischecked();
 			} catch (Exception e) {
 				// TODO: unexpected error: the item should be there
 				// Handle more cleanly
 				e.printStackTrace();
 				finish();
 			}
 			
 			this._name.setText(item.get_name());
 			this._notes.setText(item.get_notes());
 		}
 
 		ImageButton saveButton = (ImageButton)findViewById(R.id.detail_item_SaveButton);
 		saveButton.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View v) {
 				String originalName = _nameText;
 				String currentName = _name.getText().toString();
 				String notes = _notes.getText().toString();
 				try {
 					if (_mode == ItemDetailActivity.ActivityMode_NewItem) {
 						MainActivity.Instance.ItemsTable.addItem(currentName, notes);
 					} else if (_mode == ItemDetailActivity.ActivityMode_ExistingItem) {
 						Item updatedItem = new Item(currentName, notes, _ischecked);
 						MainActivity.Instance.ItemsTable.updateItem(originalName, updatedItem);
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				
 				setResult(MainActivity.ItemDetailActivityCode);
 				finish();
 			}
 		});
 		
 		ImageButton deleteButton = (ImageButton)findViewById(R.id.detail_item_DeleteButton);
 		deleteButton.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View v) {
 				try {
 					MainActivity.Instance.ItemsTable.deleteItem(_nameText);
 				} catch (Exception e) {
 					System.err.println("Error deleting item");
 				}
 				
 				setResult(MainActivity.ItemDetailActivityCode);
 				finish();
 			}
 		});
 		
 		ImageButton cancelButton = (ImageButton)findViewById(R.id.detail_item_CancelButton);
 		cancelButton.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View v) {
 				// TODO: popup warning if the item is dirty and changes are
 				// unsaved
 				finish();
 			}
 		});
 	}
 }
