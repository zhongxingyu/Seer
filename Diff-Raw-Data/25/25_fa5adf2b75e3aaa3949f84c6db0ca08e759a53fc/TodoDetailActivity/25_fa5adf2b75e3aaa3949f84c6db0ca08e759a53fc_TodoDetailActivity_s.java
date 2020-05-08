 package com.manning.gia.todo.activity;
 
 import android.app.Activity;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.manning.gia.todo.R;
 import com.manning.gia.todo.data.ItemContentProvider;
 import com.manning.gia.todo.data.ItemRepository;
 import com.manning.gia.todo.model.Item;
 
 /**
  * Detail todo activity view
  * @author jkeam
  *
  */
 public class TodoDetailActivity extends Activity {
 	private Uri uri;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.todo_edit);
		Uri uri = getUri(savedInstanceState);
 		Item item = getItem(uri);
 
 		Button confirmButton = (Button)findViewById(R.id.todoEditConfirmButton);
 		EditText editText = (EditText)findViewById(R.id.editText);
 		editText.setText(item.getName());
 
		attachOnClickHandler(confirmButton, editText.getText().toString());
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
		saveState();
 		outState.putParcelable(ItemContentProvider.CONTENT_ITEM_TYPE, uri);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
		saveState();
 	}
 	
	protected void saveState() {
 		EditText editText = (EditText)findViewById(R.id.editText);
 		String text = editText.getText().toString();
 		if (uri == null) {
 			ItemRepository.save(getContentResolver(), text);
 		}
 		else {
 			ItemRepository.update(getContentResolver(), uri, text);
 		}
 	}
 
	protected void attachOnClickHandler(Button button, final String text) {
 		button.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
				if (TextUtils.isEmpty(text)) {
 					showValidation();
 				} else {
 					setResult(RESULT_OK);
 					finish();
 				}
 			}
 
 		});
 	}
 
 	private void showValidation() {
		Toast.makeText(TodoDetailActivity.this, "Please enter text for the todo item", Toast.LENGTH_LONG).show();
 	}
 
 	protected Item getItem(Uri uri) {
 		return ItemRepository.getItemFromContentItemUri(getContentResolver(), uri);
 	}
 
 	protected Uri getUri(Bundle savedInstanceState) {
 		Uri uri = null;
 
 		Bundle bundleFromIntent = getIntent().getExtras();
 		if (bundleFromIntent != null) {
 			uri = bundleFromIntent.getParcelable(ItemContentProvider.CONTENT_ITEM_TYPE);
 		}
 		else if (savedInstanceState != null) {
 			uri = savedInstanceState.getParcelable(ItemContentProvider.CONTENT_ITEM_TYPE);
 		}
 
 		return uri;
 	}
 }
