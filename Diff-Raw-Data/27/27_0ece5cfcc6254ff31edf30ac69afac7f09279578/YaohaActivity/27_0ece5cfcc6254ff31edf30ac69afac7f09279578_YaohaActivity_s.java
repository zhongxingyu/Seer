 package org.yaoha;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import oauth.signpost.OAuth;
 import oauth.signpost.OAuthConsumer;
 import oauth.signpost.OAuthProvider;
 import oauth.signpost.basic.DefaultOAuthConsumer;
 import oauth.signpost.basic.DefaultOAuthProvider;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.provider.MediaStore;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.MultiAutoCompleteTextView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class YaohaActivity extends Activity implements OnClickListener {
     Button mapButton;
     Button startButton;
     String OSM_TOKEN = "", OSM_SECRET_TOKEN = "", PASST = "";
     String Test1 = "NULL";
     ImageButton button_favorite_1, button_favorite_2, button_favorite_3, button_favorite_4, button_favorite_5, button_favorite_6;
     ImageButton actualButton;
     final static String EDIT_FAV_STRING = "edit favorite";
     final static String EDIT_FAV_PIC = "edit picture";
     final static String REMOVE_FAV = "remove favorite";
     TextView text_fav_1, text_fav_2, text_fav_3, text_fav_4, text_fav_5, text_fav_6; 
     OAuthConsumer OSMconsumer = new DefaultOAuthConsumer("LXhdgmfvvoGRmVCc0EPZajUS8458AXYZ2615f9hs", "ZTfY5iYZ8Lszgy6DtRh0b258qciz4aYm1XnMciDi");
 //  String scope = "http://www.yaoha.org";
     OAuthProvider OSMprovider = new DefaultOAuthProvider(
           "http://www.openstreetmap.org/oauth/request_token"/*+ URLEncoder.encode(scope, "utf-8")*/,
           "http://www.openstreetmap.org/oauth/access_token",
           "http://www.openstreetmap.org/oauth/authorize");
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
         //TODO Evil Set of workarounds, change this to make favorites persistent
         text_fav_1 = new TextView(this);
         text_fav_1.setText(R.string.add_favorite);
         text_fav_2 = new TextView(this);
         text_fav_2.setText(R.string.add_favorite);
         text_fav_3 = new TextView(this);
         text_fav_3.setText(R.string.add_favorite);
         text_fav_4 = new TextView(this);
         text_fav_4.setText(R.string.add_favorite);
         text_fav_5 = new TextView(this);
         text_fav_5.setText(R.string.add_favorite);
         text_fav_6 = new TextView(this);
         text_fav_6.setText(R.string.add_favorite);
 
 
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
         button_favorite_5 = (ImageButton) findViewById(R.id.button_fav_5);
         button_favorite_5.setOnClickListener(this);
         button_favorite_6 = (ImageButton) findViewById(R.id.button_fav_6);
         button_favorite_6.setOnClickListener(this);
         
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, SHOP_TYPES);
         MultiAutoCompleteTextView textView = (MultiAutoCompleteTextView) findViewById(R.id.searchTextfield);
         textView.setAdapter(adapter);
         textView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
         
         registerForContextMenu(button_favorite_1);
         registerForContextMenu(button_favorite_2);
         registerForContextMenu(button_favorite_3);
         registerForContextMenu(button_favorite_4);
         registerForContextMenu(button_favorite_5);
         registerForContextMenu(button_favorite_6);
         
         SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(getBaseContext()); 
         if (prefs.getBoolean("start_with_map",false) == true) {
             startButton.performClick();
         }
         
         SharedPreferences settings = getSharedPreferences("MyPrefsFile", 0);
         OSM_TOKEN = settings.getString("osm_token", "NULL");
         OSM_SECRET_TOKEN = settings.getString("osm_secret", "NULL");
 //        if (!OSM_TOKEN.equals("NULL")){
 //            Toast.makeText(this, "OSM-Tokens found", Toast.LENGTH_LONG).show();
 //        }
         
         OsmNodeDbHelper.create(getApplicationContext());
     }
     
     @Override
     protected void onStop() {
         SharedPreferences settings = getSharedPreferences("MyPrefsFile", 0);
         SharedPreferences.Editor editor = settings.edit();
         editor.putString("osm_token", OSM_TOKEN);
         editor.putString("osm_secret", OSM_SECRET_TOKEN);
         
         editor.commit();
         super.onStop();
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
             case R.id.setOSM:
                 registerOSM();
                 return true;
 //            case R.id.connectToOSM:
 //                connectToOSM(OSMconsumer);
 //                return true;
             default:
                 return false;
         }
     }
     
     private void registerOSM() {
       Toast.makeText(this, "Fetching request token...", Toast.LENGTH_SHORT).show();
       String authUrl = "";
       String errorOSMToken = "";
       try {
           authUrl = OSMprovider.retrieveRequestToken(OSMconsumer, OAuth.OUT_OF_BAND);
     } catch (Exception e) {
         errorOSMToken = e.getMessage();
     }
 
       this.OSM_TOKEN = OSMconsumer.getToken();
       this.OSM_SECRET_TOKEN = OSMconsumer.getTokenSecret();
 
       Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(authUrl));
       startActivity(viewIntent);
         
     }
 
     private void connectToOSM(OAuthConsumer consumer){
         URL url = null;
         HttpURLConnection request = null;
         try {
             url = new URL("http://openstreetmap.de/karte.html");
 
         
             request = (HttpURLConnection) url.openConnection();
     
             consumer.sign(request);
     
             //System.out.println("Sending request...");
             request.connect();
     
             
             Toast.makeText(this, "Response: " + request.getResponseCode() + " " + request.getResponseMessage(), Toast.LENGTH_SHORT).show();
         } catch (Exception e) {
             String test4 = e.getMessage();
             String bla = test4;
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
         if(v.getId() == R.id.button_fav_5) {
             if (text_fav_5.getText().equals(getText(R.string.add_favorite))) {
                 openFavMenu(button_favorite_5, text_fav_5);
             } else {
                 searchMapWithKey(text_fav_5.getText());
             }
         }
         if(v.getId() == R.id.button_fav_6) {
             if (text_fav_6.getText().equals(getText(R.string.add_favorite))) {
                 openFavMenu(button_favorite_6, text_fav_6);
             } else {
                 searchMapWithKey(text_fav_6.getText());
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
         }else if (item.getItemId() == button_favorite_5.getId()){
             editFavs(item, button_favorite_5, text_fav_5);
         }else if (item.getItemId() == button_favorite_6.getId()){
             editFavs(item, button_favorite_6, text_fav_6);
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
                     Bitmap tmpbitmap = null;
                     try {
                         tmpbitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                     } catch (FileNotFoundException e) {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
                     } catch (IOException e) {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
                     }
                     int tmp_width = tmpbitmap.getWidth();
                     int tmp_height = tmpbitmap.getHeight();
                     Bitmap fav_bitmap = null;
                     if (tmp_width < tmp_height) {
                         fav_bitmap = Bitmap.createBitmap(tmpbitmap, 0, (tmp_height-tmp_width)/2, tmp_width, tmp_width);
                     } else if (tmp_height < tmp_width) {
                         fav_bitmap = Bitmap.createBitmap(tmpbitmap, (tmp_width-tmp_height)/2, 0, tmp_height, tmp_height);
                     } else {
                         fav_bitmap = tmpbitmap;
                     }
                     
                     actualButton.setImageBitmap(Bitmap.createScaledBitmap(fav_bitmap, actualButton.getWidth()-20, actualButton.getHeight()-20, false));
                 }
             }
     }
 }
