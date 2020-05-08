 package org.yaoha;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 
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
 import android.widget.MultiAutoCompleteTextView;
 import android.widget.Toast;
 
 public class YaohaActivity extends Activity implements OnClickListener {
 	Button mapButton;
 	Button startButton;
 	Button button_favorite_1, button_favorite_2, button_favorite_3;
 	private static final String[] SHOP_TYPES = new String[] {
         "groceries", "computer", "sport", "clothes", "gas station"
     };
 	
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 //        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(getBaseContext()); 
 //        if (prefs.getBoolean("start_with_map",false) == true) {
 //            //startButton.performClick();
 //            //TODO make this work
 //		}
         startButton = (Button) findViewById(R.id.button_start);
         startButton.setOnClickListener(this);
         button_favorite_1 = (Button) findViewById(R.id.button_fav_1);
         button_favorite_1.setOnClickListener(this);
         button_favorite_2 = (Button) findViewById(R.id.button_fav_2);
         button_favorite_2.setOnClickListener(this);
         //button_favorite_3 = (Button) findViewById(R.id.button_fav_3);
         //button_favorite_3.setOnClickListener(this);
         
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, SHOP_TYPES);
         MultiAutoCompleteTextView textView = (MultiAutoCompleteTextView) findViewById(R.id.searchTextfield);
         textView.setAdapter(adapter);
         textView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
         
         registerForContextMenu(button_favorite_1);
         registerForContextMenu(button_favorite_2);
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
                 Toast.makeText(this, "You just payed 49,99€. Enjoy this Pro-Version!", Toast.LENGTH_LONG).show();
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
            if (button_favorite_1.getText().equals("+")) {
                 Toast.makeText(this, "Hold button to edit.", Toast.LENGTH_SHORT).show();
             } else {
                 searchMapWithKey(button_favorite_1.getText());
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
         menu.setHeaderTitle("Context Menu");  
         menu.add(0, v.getId(), 0, "Add/edit this favorite");  
         menu.add(0, v.getId(), 0, "Remove this favorite");
         
     }
     
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         if (item.getItemId() == button_favorite_1.getId()){
             editFavs(item, button_favorite_1);
         }else if (item.getItemId() == button_favorite_2.getId()){
             editFavs(item, button_favorite_2);
         }
     return super.onContextItemSelected(item);
     }
     
     public boolean editFavs(MenuItem item, final Button btn){
         if(item.getTitle()=="Add/edit this favorite"){
             AlertDialog.Builder alert = new AlertDialog.Builder(this); 
             final EditText  input = new EditText(this);
             alert.setTitle("Adding favorite"); 
             alert.setMessage("Enter your favorite search"); 
             alert.setView(input); 
 
             alert.setPositiveButton("Set", new DialogInterface.OnClickListener() { 
                 public void onClick(DialogInterface dialog, int whichButton) {
                     btn.setText(input.getText());
                 } 
                 }); 
 
                 alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() { 
                   public void onClick(DialogInterface dialog, int whichButton) { 
                     // Canceled. 
                   } 
                 }); 
             alert.show();
         } else if (item.getTitle()=="Remove this favorite"){
            btn.setText("+");
         } else {
             Toast.makeText(this, "Placeholder - You schould never see this.", Toast.LENGTH_SHORT).show();
             return false;
         }
         return super.onContextItemSelected(item);
     }
 }
