 package no.ntnu.idi.socialhitchhiking;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.concurrent.ExecutionException;
 
 import no.ntnu.idi.freerider.model.TripPreferences;
 import no.ntnu.idi.freerider.model.User;
 import no.ntnu.idi.freerider.model.Visibility;
 import no.ntnu.idi.freerider.protocol.PreferenceRequest;
 import no.ntnu.idi.freerider.protocol.PreferenceResponse;
 import no.ntnu.idi.freerider.protocol.Request;
 import no.ntnu.idi.freerider.protocol.RequestType;
 import no.ntnu.idi.freerider.protocol.UserRequest;
 import no.ntnu.idi.freerider.protocol.UserResponse;
 import no.ntnu.idi.socialhitchhiking.client.RequestTask;
 import no.ntnu.idi.socialhitchhiking.journey.TripOption;
 import no.ntnu.idi.socialhitchhiking.utility.SocialHitchhikingActivity;
 
 import org.apache.http.client.ClientProtocolException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.Html;
 import android.util.Log;
 import android.util.SparseBooleanArray;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 /**
  * Class that contains functionality for the "Preferences" tab in "My Account".
  * @author Made Ziius
 * @author Kristoffer Aulie
  */
 public class MyAccountPreferences extends SocialHitchhikingActivity {
 
 	private ArrayAdapter<String> prefAdap;
 	private ListView listPreferences;
 	private SparseBooleanArray 	checked;
 	private ArrayList<String>  selectedItems;
 	private TripPreferences pref2;
 	private Integer selectedPrivacy = null;
 	private Visibility privacyPreference;
 	private String facebookPrivacy = "";
 	private Button btnFacebook;
     /** Called when the activity is first created. */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// Setting the loading layout
 		setContentView(R.layout.main_loading);
 		// Starting AsyncTask loading preferences from database
 		new PreferenceLoader(this).execute();
 	}
 	public void initPreferences(PreferenceResponse res){
 		setContentView(R.layout.my_account_preferences);
 		
 		// Get default privacy value from user preferences
         privacyPreference=getApp().getSettings().getFacebookPrivacy();
         if(privacyPreference==Visibility.FRIENDS){
         	selectedPrivacy=0;
         	facebookPrivacy = "Friends";
         }
         if(privacyPreference==Visibility.FRIENDS_OF_FRIENDS){
     		selectedPrivacy=1;
     		facebookPrivacy = "Friends of friends";
     	}
         if(privacyPreference==Visibility.PUBLIC){
     		selectedPrivacy=2;
     		facebookPrivacy = "Public";
     	}
         
 		listPreferences = (ListView) findViewById(R.id.listPreferences);
		btnFacebook = (Button)findViewById(R.id.preferencesFacebookButton);
 
 		/**Preferences fetched from the strigs.xml*/
 		String[] preferences = getResources().getStringArray(R.array.preferences_array);
 		prefAdap = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, preferences);
 		listPreferences.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 		listPreferences.setAdapter(prefAdap);
 	    prefAdap.notifyDataSetChanged();
 	   
 	    selectedItems = new ArrayList<String>();
 	    checked = listPreferences.getCheckedItemPositions();
 	    pref2 =res.getPreferences();
 		Log.e("PREFERENCE", pref2.getMusic().toString());
 	    
 	    /**Fetch preferences from the database and display them*/
 	    for (int i = 0; i < 5; i++){
 	            switch (i) {
 	                case 0: if(pref2.getMusic()){
 	               listPreferences.setItemChecked(i, true);
 	                		}
 	                         break;
 	                case 1: if( pref2.getAnimals()){
 	                	listPreferences.setItemChecked(i, true);
                 		}
 	                	break;
 	                case 2: if( pref2.getBreaks()){
 	                	listPreferences.setItemChecked(i, true);
                 		}
 	                	break;
 	                case 3:  if( pref2.getTalking()){
 	                	listPreferences.setItemChecked(i, true);
                 		}
 	                	break;
 	                case 4:  if( pref2.getSmoking()){
 	                	listPreferences.setItemChecked(i, true);
                 		}
 	                	break;
 	            }
 	    }
 	    prefAdap.notifyDataSetChanged();
	    
	    listPreferences.addFooterView(btnFacebook);
	    // Setting the Facebook privacy button
        btnFacebook.setText(Html.fromHtml("<b>" + "Set Facebook privacy" + "</b>" +  "<br />" + 
                "<small>" + facebookPrivacy + "</small>" + "<br />"));
 	}
 	/**
 	 * Called when the button "Set Facebook privacy" is clicked. Opens an AlertDialog where the user selects a privacy setting.
 	 * @param view
 	 */
 	public void btnFacebookClicked(View view){
 		
     	AlertDialog.Builder builder = new AlertDialog.Builder(this);
     	builder.setTitle("Set Privacy");
     	builder.setSingleChoiceItems(R.array.privacy_setting, selectedPrivacy, new DialogInterface.OnClickListener() {
     	    public void onClick(DialogInterface dialog, int item) {
     	        selectedPrivacy = item;
     	    }
     	});
     	builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
             	switch (selectedPrivacy) {
 				case 0:
 					privacyPreference=Visibility.FRIENDS;
 					facebookPrivacy = "Friends";
 					break;
 				case 1:
 					privacyPreference=Visibility.FRIENDS_OF_FRIENDS;
 					facebookPrivacy = "Friends of friends";
 					break;
 				case 2:
 					privacyPreference=Visibility.PUBLIC;
 					facebookPrivacy = "Public";
 					break;
 				default:
 					break;
 				}
             	// Setting button text
             	btnFacebook.setText(Html.fromHtml("<b align = left><big>" + "Set Facebook privacy" + "</big></b>" +  
             			"<br />" + facebookPrivacy  + "<br />"));
             	 
                 // Writing data to SharedPreferences
             	getApp().getSettings().setFacebookPrivacy(privacyPreference);
             }
         });
         builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                  dialog.cancel();
             }
         });
     	AlertDialog alert = builder.create();
     	alert.show();
     	
     }
 	/**invoke getCheckedItemPositions() on list view object which returns the set of checked items (as SparseBooleanArray) in the list*/
 	public void onClick(View v) {
 	        for (int i = 0; i < checked.size(); i++) {
 	            // Item position in adapter
 	            int position = checked.keyAt(i);
 	            // Add preference if it is checked i.e.) == TRUE!
 	            if (checked.valueAt(i))
 	                selectedItems.add(prefAdap.getItem(position));
 	        }
 	 } 
 	/** Save the preferences on the database*/
 	@Override
 	public void onStop() {
 
         for (int i = 0; i < checked.size(); i++) {
             // Item position in adapter
             int position = checked.keyAt(i);
             // Add preference if it is checked i.e.) == TRUE and remove it if it is not checked
 	            switch (position) {
 	                case 0: pref2.setMusic(checked.valueAt(i));
 	                         break;
 	                case 1:  pref2.setAnimals(checked.valueAt(i));
 	                         break;
 	                case 2: pref2.setBreaks(checked.valueAt(i));
 	                		break;
 	                case 3: pref2.setTalking(checked.valueAt(i));
 	                		break;
 	                case 4: pref2.setSmoking(checked.valueAt(i));
 	                		break;
 	            }
         }
         Request req3 = new PreferenceRequest(RequestType.UPDATE_PREFERENCE,getApp().getUser(),pref2);
         try {
 			PreferenceResponse res2 = (PreferenceResponse) RequestTask.sendRequest(req3,getApp());
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		// TODO Auto-generated method stub
 		super.onStop();
 	}
 }
 /**
  * Class that loads the preferences from the server in the background.
  * @author Kristoffer Aulie
  *
  */
 class PreferenceLoader extends AsyncTask<Void, TripPreferences, PreferenceResponse>{
 	MyAccountPreferences activity;
 	
 	public PreferenceLoader(Activity activity){
 		this.activity = (MyAccountPreferences) activity;
 	}
 	/**
 	 * Getting the preference information from the database.
 	 * @param params
 	 * @return
 	 */
 	protected PreferenceResponse doInBackground(Void... params) {
 		TripPreferences pref = new TripPreferences(777, true, true, true, true, true);
 		pref.setPrefId(1);
 		PreferenceResponse res = null;
 		try {
 			Request req = new PreferenceRequest(RequestType.GET_PREFERENCE, activity.getApp().getUser(), pref);
 			res = (PreferenceResponse) RequestTask.sendRequest(req,activity.getApp());
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return res;
 	}
 	@Override
 	protected void onPostExecute(PreferenceResponse result) {
 		activity.initPreferences(result);
 	}
 }
 
 
