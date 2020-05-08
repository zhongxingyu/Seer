 package org.yaoha;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.provider.MediaStore;
 
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
 import android.widget.MultiAutoCompleteTextView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class YaohaActivity extends Activity implements OnClickListener {
 	Button mapButton;
 	Button startButton;
 	ImageButton button_favorite_1, button_favorite_2, button_favorite_3, button_favorite_4;
 	ImageButton actualButton;
 	final static String EDIT_FAV_STRING = "edit favorite";
 	final static String EDIT_FAV_PIC = "edit picture";
 	final static String REMOVE_FAV = "remove favorite";
 	TextView text_fav_1;
 	TextView text_fav_2;
 	TextView text_fav_3;
 	TextView text_fav_4;
 	final static int SELECT_PICTURE = 1;
 	Uri selectedImageUri;
 	
 	private static final String[] SHOP_TYPES = new String[] {
         "groceries", "computer", "sport", "clothes", "gas station"
     };
 	
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         text_fav_1 = (TextView) findViewById(R.id.textView_fav_1);
     	text_fav_2 = (TextView) findViewById(R.id.textView_fav_2);
     	text_fav_3 = (TextView) findViewById(R.id.textView_fav_3);
     	text_fav_4 = (TextView) findViewById(R.id.textView_fav_4);
     	
         startButton = (Button) findViewById(R.id.button_start);
         startButton.setOnClickListener(this);
         button_favorite_1 = (ImageButton) findViewById(R.id.button_fav_1);
         button_favorite_1.setOnClickListener(this);
//        button_favorite_1.setScaleType(F)
         button_favorite_2 = (ImageButton) findViewById(R.id.button_fav_2);
         button_favorite_2.setOnClickListener(this);
         button_favorite_3 = (ImageButton) findViewById(R.id.button_fav_3);
         button_favorite_3.setOnClickListener(this);
         button_favorite_4 = (ImageButton) findViewById(R.id.button_fav_4);
         button_favorite_4.setOnClickListener(this);
         
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, SHOP_TYPES);
         MultiAutoCompleteTextView textView = (MultiAutoCompleteTextView) findViewById(R.id.searchTextfield);
         textView.setAdapter(adapter);
         textView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
         
         registerForContextMenu(button_favorite_1);
         registerForContextMenu(button_favorite_2);
         registerForContextMenu(button_favorite_3);
         registerForContextMenu(button_favorite_4);
         
         SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(getBaseContext()); 
         if (prefs.getBoolean("start_with_map",false) == true) {
             startButton.performClick();
         }
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.settings:
                 Intent i = new Intent(this, SettingsActivity.class);
                 startActivity(i);
                 return true;
             case R.id.quit_app:
                 this.finish();
                 return true;
             case R.id.buy_pro:
                 Toast.makeText(this, "You just payed 49,99. Enjoy this Pro-Version!", Toast.LENGTH_LONG).show();
                 return true;
             default:
                 return false;
         }
     }
     
     @Override
     public void onClick(View v) {
         if(v.getId() == R.id.button_start) {
             MultiAutoCompleteTextView textView = (MultiAutoCompleteTextView) findViewById(R.id.searchTextfield);
             searchMapWithKey(textView.getText());
         }
         if(v.getId() == R.id.button_fav_1) {
             if (text_fav_1.getText().equals(getText(R.string.add_favorite))) {
                 openFavMenu(button_favorite_1, text_fav_1);
             } else {
                 searchMapWithKey(text_fav_1.getText());
             }
         }
         if(v.getId() == R.id.button_fav_2) {
             if (text_fav_2.getText().equals(getText(R.string.add_favorite))) {
             	openFavMenu(button_favorite_2, text_fav_2);
             } else {
                 searchMapWithKey(text_fav_2.getText());
             }
         }
         if(v.getId() == R.id.button_fav_3) {
             if (text_fav_3.getText().equals(getText(R.string.add_favorite))) {
             	openFavMenu(button_favorite_3, text_fav_3);
             } else {
                 searchMapWithKey(text_fav_3.getText());
             }
         }
         if(v.getId() == R.id.button_fav_4) {
             if (text_fav_4.getText().equals(getText(R.string.add_favorite))) {
             	openFavMenu(button_favorite_4, text_fav_4);
             } else {
                 searchMapWithKey(text_fav_4.getText());
             }
         }
     }
     
     public void searchMapWithKey(CharSequence keyword){
         Intent intent = new Intent(this, YaohaMapActivity.class);
         intent.putExtra("org.yaoha.YaohaMapActivity.SearchText", keyword);
         startActivity(intent); 
     }
     
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v,
             ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         menu.setHeaderTitle("Action");  
         menu.add(0, v.getId(), 0, EDIT_FAV_STRING);
         menu.add(0, v.getId(), 0, EDIT_FAV_PIC);
         menu.add(0, v.getId(), 0, REMOVE_FAV);
     }
     
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         if (item.getItemId() == button_favorite_1.getId()){
             editFavs(item, button_favorite_1, text_fav_1);
         }else if (item.getItemId() == button_favorite_2.getId()){
             editFavs(item, button_favorite_2, text_fav_2);
         }else if (item.getItemId() == button_favorite_3.getId()){
             editFavs(item, button_favorite_3, text_fav_3);
         }else if (item.getItemId() == button_favorite_4.getId()){
             editFavs(item, button_favorite_4, text_fav_4);
         }
     return super.onContextItemSelected(item);
     }
     
     public boolean editFavs(MenuItem item, final ImageButton btn, final TextView tv){
         if(item.getTitle()==EDIT_FAV_STRING){
         	openFavMenu(btn, tv);
         } else if (item.getTitle()==EDIT_FAV_PIC){
         	Intent intent = new Intent();
             intent.setType("image/*");
             intent.setAction(Intent.ACTION_GET_CONTENT);
             actualButton = btn;  //workaround, there must be a better way
             startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
         } else if (item.getTitle()==REMOVE_FAV){
            tv.setText(getText(R.string.add_favorite));
            btn.setImageResource(R.drawable.plus_sign_small);
         } else {
             Toast.makeText(this, "Placeholder - You schould never see this.", Toast.LENGTH_SHORT).show();
             return false;
         }
         return super.onContextItemSelected(item);
     }
 
 	private void openFavMenu(final ImageButton btn, final TextView tv) {
         AlertDialog.Builder alert = new AlertDialog.Builder(this); 
         final EditText input = new EditText(this);
         alert.setTitle("Adding favorite"); 
         alert.setMessage("Enter your favorite search"); 
         alert.setView(input); 
 
         alert.setPositiveButton("Set", new DialogInterface.OnClickListener() { 
             public void onClick(DialogInterface dialog, int whichButton) {
                 tv.setText(input.getText());
                 btn.setImageResource(R.drawable.placeholder_logo);
                 //TODO add method to catch store-icons
             } 
             }); 
 
             alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() { 
               public void onClick(DialogInterface dialog, int whichButton) { 
                 // Canceled. 
               } 
             }); 
         alert.show();
 	}
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		 if (resultCode == RESULT_OK) {
 		        if (requestCode == SELECT_PICTURE) {
 		            selectedImageUri = data.getData();
 		            actualButton.setImageURI(selectedImageUri);
 		        }
 		    }
 	}
 	
 	public String getPath(Uri uri) {
 	    String[] projection = { MediaStore.Images.Media.DATA };
 	    Cursor cursor = managedQuery(uri, projection, null, null, null);
 	    int column_index = cursor
 	            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
 	    cursor.moveToFirst();
 	    return cursor.getString(column_index);
 	}
 }
