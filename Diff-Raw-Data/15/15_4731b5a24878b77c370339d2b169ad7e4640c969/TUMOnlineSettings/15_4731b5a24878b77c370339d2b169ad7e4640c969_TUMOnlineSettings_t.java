 package de.tum.in.newtumcampus;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 import de.tum.in.newtumcampus.common.Utils;
 import de.tum.in.newtumcampus.tumonline.TUMOnlineRequest;
 
 /**
  * This activity should handle all preferences and interaction to set up the app
  * working with TUMOnline i.e. get access token, set permissions etc.
  * 
  * @author Vincenz Doelle, Daniel G. Mayr
  * @review Daniel G. Mayr
  */
 public class TUMOnlineSettings extends PreferenceActivity implements
 		OnClickListener, android.content.DialogInterface.OnClickListener {
 
 	/**
 	 * UI button to generate access token
 	 */
 	private Button btnGetAccessToken;
 	private Preference lrzIdPreference;
 	private String lrzId;
 
 	/**
 	 * just overwitten to set layout, view and the listeners
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.tumonline);
 		addPreferencesFromResource(R.xml.tumonline_settings);
 		// new Button to get access token
 		btnGetAccessToken = (Button) findViewById(R.id.btnGetAccessToken);
 		btnGetAccessToken.setOnClickListener(this);
 
//		PreferenceManager.getDefaultSharedPreferences(getBaseContext()).get)
//		getPreferenceScreen().removePreference(this.findPreference(Const.ACCESS_TOKEN));
//		getPreferenceScreen().removePreference(this.findPreference(Const.LRZ_ID));
		
		
 		lrzIdPreference = (Preference) this.findPreference(Const.LRZ_ID);
 		lrzIdPreference
 				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
 
 					@Override
 					public boolean onPreferenceChange(Preference preference,
 							Object newValue) {
 						setLrzId((String)newValue);
 						if (!((String) newValue).equals(Utils.getSetting(
 								getBaseContext(), Const.LRZ_ID))
 								&& !((String) newValue).equals("")) {
 							generateToken(getLrzId());
 						}
 						return true;
 					}
 				});
 	}
 
 	/**
 	 * get a new access token for TUMOnline by passing the lrz ID due to the
 	 * simplicity of the given xml file we only need to parse the <token>
 	 * element using an xml-parser is simply to much... just extract the pattern
 	 * via regex
 	 * 
 	 * @param lrz_id
 	 *            lrz user id
 	 * @return the access token
 	 */
 	private static String getAccessToken(String lrz_id) {
 		// we don't have an access token yet, though we take the constructor
 		// with only one parameter to set the method
 		TUMOnlineRequest request = new TUMOnlineRequest("requestToken");
 		// add lrz_id to parameters
 		request.setParameter("pUsername", lrz_id);
 		// add readable name for TUMOnline
 		request.setParameter("pTokenName", "TUMCampusApp");
 
 		// fetch the xml response of requestToken
 		String strTokenXml = request.fetch();
 		Log.d("RAWOUTPUT", strTokenXml);
 		// it is only one tag in that xml, let's do a regex pattern
 		return strTokenXml.substring(
 				strTokenXml.indexOf("<token>") + "<token>".length(),
 				strTokenXml.indexOf("</token>"));
 	}
 
 	/**
 	 * this function does the handle of the OnClickListener of the
 	 * getAccessToken Button
 	 */
 	@Override
 	public void onClick(View v) {
 		if (v.getId() == btnGetAccessToken.getId()) {
 			// btnAccessToken was pressed			
 			generateToken(getLrzId());
 			}
 			
 	}
 	
 	private void generateToken(String localLRZID){
 		// check if lrz could be valid?
 		if (localLRZID.length() == 7) {
 			// is access token already set?
 			String oldaccesstoken = PreferenceManager.getDefaultSharedPreferences(this).getString(
 					Const.ACCESS_TOKEN, "");
 			if (oldaccesstoken.length() > 2) {
 				// show Dialog first
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				builder.setMessage(getString(R.string.dialog_new_token))
 				.setPositiveButton(getString(R.string.yes), this)
 				.setNegativeButton(getString(R.string.no), this).show();
 			} else {
 				setAccessToken(localLRZID);
 			}
 		} else {
 			Toast.makeText(this, getString(R.string.error_lrz_wrong), Toast.LENGTH_LONG).show();
 		}
 	}
 
 	public void setAccessToken() {
 		setAccessToken(getLrzId());
 	}
 
 	/**
 	 * Internal method for setting a new token. WARNING: Doesn't use shared
 	 * preferences, but rather a parameter. Needed for the onPreferenceChanged
 	 * callback, so as to use the new LRZ_ID value for the token generation
 	 * before it is set (which happens right after the callback).
 	 * 
 	 * @param stringLRZID
 	 */
 	private void setAccessToken(String stringLRZID) {
 		try {
 			if (!Utils.isConnected(getBaseContext())) {
 				Toast.makeText(getBaseContext(),
 						R.string.no_internet_connection, Toast.LENGTH_LONG);
 				return;
 			}
 			// ok, do the request now
 			String strAccessToken = getAccessToken(stringLRZID);
 			Log.d("AcquiredAccessToken", strAccessToken);
 
 			// save access token to preferences
 			Utils.setSetting(getBaseContext(), Const.ACCESS_TOKEN,
 					strAccessToken);
 			Toast.makeText(this, getString(R.string.access_token_generated),
 					Toast.LENGTH_LONG).show();
 
 		} catch (Exception ex) {
 			// set access token to null
 			Utils.setSetting(this, Const.ACCESS_TOKEN, null);
 			Toast.makeText(this,
 					getString(R.string.access_token_wasnt_generated),
 					Toast.LENGTH_LONG).show();
 		}
 	}
 
 	@Override
 	public void onClick(DialogInterface dialog, int which) {
 		if (which == DialogInterface.BUTTON_POSITIVE) {
 			// Ja geklickt
 			setAccessToken(getLrzId());
 		}
 
 	}
 
 	private String getLrzId() {
 		if (lrzId == null || lrzId == "") {
 			lrzId = Utils.getSetting(getBaseContext(), Const.LRZ_ID);
 		}
 		return lrzId;
 	}
 
 	private void setLrzId(String lrzId) {
 		this.lrzId = lrzId;
 	}
 }
