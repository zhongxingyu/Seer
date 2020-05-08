 package de.fhworms.inf1743.tawk;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.text.Editable;
 import android.text.method.KeyListener;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 public final class UserLogin extends Activity {
 
 	static final String DATA_STORE = "dataStore";
 
 	static final String USER_NAME = "userName";
 	static final String PASSWORD = "password";
 	static final String PROPERTY_1 = "userProperty1";
 	static final String PROPERTY_2 = "userProperty2";
 	static final String USE_TEMP = "useTemporaryVariables";
 
 	// results could be handled from the following Activities
 	final int USER_LOGIN = 0;
 	final int CREATE_MESSAGE = 1;
 	final int VIEW_MESSAGES = 2;
 	final int ACTIVITY_SELECT_IMAGE = 3;
 
 	SharedPreferences dataStore;
 	Editor ed;
 
 	CheckBox savePasswordCheckbox;
 	Button property1;
 	EditText userName, password, property2;
 	ImageView userImage;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.user_login);
 
 		savePasswordCheckbox = (CheckBox) findViewById(R.id.save_password_checkbox);
 		userName = (EditText) findViewById(R.id.username_edittext);
 		password = (EditText) findViewById(R.id.password_edittext);
 		property1 = (Button) findViewById(R.id.user_property1_button);
 		userImage = (ImageView) findViewById(R.id.user_image);
 		property2 = (EditText) findViewById(R.id.user_property2_edittext);
 
 		dataStore = getSharedPreferences(DATA_STORE, MODE_PRIVATE);
 
 		// restore user name
 		if (dataStore.getString(USER_NAME, "") != "") {
 			userName.setText((dataStore.getString(USER_NAME, "")));
 		}
 
 		// restore password
 		if (dataStore.getString(PASSWORD, "") != "") {
 			password.setText((dataStore.getString(PASSWORD, "")));
 		}
 
 		// restore "save it" button by checking negated useTemp
 		if (!dataStore.getBoolean(USE_TEMP, false)) {
 			savePasswordCheckbox.setChecked(!dataStore.getBoolean(USE_TEMP,
 					false));
 		}
 
 		// restore user properties
 		if (dataStore.getString(PROPERTY_1, "") != "") {
 			property1.setText((dataStore.getString(PROPERTY_1, "")));
 		}
 
 
 		if (dataStore.getString(PROPERTY_2, "") != "") {
 			property2.setText((dataStore.getString(PROPERTY_2, "")));
 		}
 		
 		//TODO: attach to userName
 		// watch for illegal characters entered in user name field
 //		KeyListener k = new KeyListener() {
 //			
 //			
 //			public boolean onKeyUp(View view, Editable text, int keyCode, KeyEvent event) {
 //				// TODO Auto-generated method stub
 //				return false;
 //			}
 //			
 //			public boolean onKeyOther(View view, Editable text, KeyEvent event) {
 //				// TODO Auto-generated method stub
 //				return false;
 //			}
 //			
 //			public boolean onKeyDown(View view, Editable text, int keyCode,
 //					KeyEvent event) {
 //				// TODO Auto-generated method stub
 //				return false;
 //			}
 //			
 //			public int getInputType() {
 //				// TODO Auto-generated method stub
 //				return 0;
 //			}
 //			
 //			public void clearMetaKeyState(View view, Editable content, int states) {
 //				// TODO Auto-generated method stub
 //				
 //			}
 //		};
 		
 
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		outState.putString(PROPERTY_1, property1.getText().toString());		
 		super.onSaveInstanceState(outState);
 	}
 	
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		property1.setText(savedInstanceState.getString(PROPERTY_1));
 		super.onRestoreInstanceState(savedInstanceState);
 	}
 
 	@Override
 	protected void onResume() {
 		if (property1.getText().toString() != "") {
 			try {
				userImage.setImageBitmap(BitmapFactory.decodeFile(property1.getText().toString()));
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		super.onResume();
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode,
 			Intent imageReturnedIntent) {
 		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
 
 		switch (requestCode) {
 		case ACTIVITY_SELECT_IMAGE:
 			if (resultCode == RESULT_OK) {
 				Uri selectedImage = imageReturnedIntent.getData();
 				String[] filePathColumn = { MediaStore.Images.Media.DATA };
 
 				Cursor cursor = getContentResolver().query(selectedImage,
 						filePathColumn, null, null, null);
 				cursor.moveToFirst();
 
 				String filePath = cursor.getString(cursor
 						.getColumnIndex(filePathColumn[0]));
 				cursor.close();
 
 				userImage.setImageBitmap(getResizedBitmap(
 						BitmapFactory.decodeFile(filePath), 80, 80));
 				property1.setText(filePath);
 			}
 		default:
 			break;
 		}
 
 	}
 
 	public void selectImage(View view) {
 		Intent i = new Intent(Intent.ACTION_PICK,
 				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
 		startActivityForResult(i, ACTIVITY_SELECT_IMAGE);
 	}
 
 	public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
 
 		int width = bm.getWidth();
 
 		int height = bm.getHeight();
 
 		float scaleWidth = ((float) newWidth) / width;
 
 		float scaleHeight = ((float) newHeight) / height;
 
 		// create a matrix for the manipulation
 
 		Matrix matrix = new Matrix();
 
 		// resize the bit map
 
 		matrix.postScale(scaleWidth, scaleHeight);
 
 		// recreate the new Bitmap
 
 		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
 				matrix, false);
 
 		return resizedBitmap;
 
 	}
 
 	public void close(View view) {
 		// no new activities are allowed here, because multiple
 		// login data sets may then be active in those instances
 		finish();
 	}
 
 	/**
 	 * A user tries to log in. This is called by a button. It stores all data,
 	 * temporary or not, accordingly.
 	 */
 	public void login(View view) {
 
 		// TODO: allow only users whose authentication works
 		// if (userName.getText().toString() == "franz"
 		// && password.getText().toString() == "denker") {
 
 		dataStore = getSharedPreferences(DATA_STORE, MODE_PRIVATE);
 		ed = dataStore.edit();
 
 		// data returns the value of useTemp (= !savePW)
 		Intent data = new Intent();
 
 		if (savePasswordCheckbox.isChecked()) {
 			// save persistent, e.g. store user name and password in a service
 			// and make start's onCreate() check for it
 
 			ed.putString(USER_NAME, userName.getText().toString());
 			ed.putString(PASSWORD, password.getText().toString());
 			// only write these if given, not empty
 			if (property1.getText().toString() != "") {
 				ed.putString(PROPERTY_1, property1.getText().toString());
 			}
 			if (property2.getText().toString() != "") {
 				ed.putString(PROPERTY_2, property2.getText().toString());
 			}
 
 		} else {
 			// delete old login data if temporary login is sent back
 			ed.putString(USER_NAME, "");
 			ed.putString(PASSWORD, "");
 			ed.putString(PROPERTY_1, "");
 			ed.putString(PROPERTY_2, "");
 
 			data.putExtra(USER_NAME, userName.getText().toString());
 			data.putExtra(PASSWORD, password.getText().toString());
 			data.putExtra(PROPERTY_1, property1.getText().toString());
 			data.putExtra(PROPERTY_2, property2.getText().toString());
 
 		}
 
 		ed.putBoolean(USE_TEMP, !savePasswordCheckbox.isChecked());
 
 		data.putExtra(USE_TEMP, (!savePasswordCheckbox.isChecked()));
 
 		setResult(RESULT_OK, data);
 		ed.commit();
 		finish();
 		// } else {
 		// Toast.makeText(getApplicationContext(),
 		// "please check Username and/or password", Toast.LENGTH_LONG).show();
 		// // TODO: use R.string resource for hardcoded texts
 		// }
 	}
 }
