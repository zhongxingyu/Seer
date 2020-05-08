 package com.codingspezis.android.metalonly.player;
 
 import java.util.*;
 
 import org.apache.http.*;
 import org.apache.http.client.*;
 import org.apache.http.client.entity.*;
 import org.apache.http.client.methods.*;
 import org.apache.http.impl.client.*;
 import org.apache.http.message.*;
 import org.apache.http.protocol.*;
 
 import android.annotation.*;
 import android.app.*;
 import android.content.*;
 import android.os.*;
 import android.support.v4.app.*;
 import android.view.*;
 import android.view.View.OnClickListener;
 import android.widget.*;
 
 import com.actionbarsherlock.app.*;
 import com.actionbarsherlock.view.MenuItem;
 import com.codingspezis.android.metalonly.player.wish.*;
 
 /**
  * activity that is showing a GUI for entering and sending wishes and/or regards
  * to metal-only.de
  * 
  */
 public class WishActivity extends SherlockActivity implements OnClickListener {
 
 	// URL to wish script on metal-only.de
 	public static final String URL_WISHES = "http://metalonly.de/?action=wunschscript";
 
 	// intent keys
 	public static final String KEY_WISHES_ALLOWED = "MO_WISHES_ALLOWED";
 	public static final String KEY_REGARDS_ALLOWED = "MO_REGARDS_ALLOWED";
 	public static final String KEY_NUMBER_OF_WISHES = "MO_NUMBER_OF_WISHES";
 	public static final String KEY_DEFAULT_INTERPRET = "MO_DEFAULT_INTERPRET";
 	public static final String KEY_DEFAULT_TITLE = "MO_DEFAULT_TITLE";
 
 	// shared preferences keys
 	public static final String KEY_SP_NICK = "moa_nickname";
 
 	boolean wish, regard;
 
 	private SharedPreferences settings;
 
 	// buttons
 	private Button buttonSend;
 	private ImageButton buttonHelp;
 
 	// user input
 	private EditText editNick;
 	private EditText editArtist;
 	private EditText editTitle;
 	private EditText editRegard;
 
 	private String numberOfWishes;
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_wishes);
 
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 		getSupportActionBar().setHomeButtonEnabled(true);
 
 		settings = getSharedPreferences(getString(R.string.app_name),
 				Context.MODE_MULTI_PROCESS);
 
 		// input fields
 		editNick = (EditText) findViewById(R.id.editNick);
 		editNick.setText(settings.getString(KEY_SP_NICK, ""));
 		editArtist = (EditText) findViewById(R.id.editArtist);
 		editTitle = (EditText) findViewById(R.id.editTitle);
 		editRegard = (EditText) findViewById(R.id.editRegard);
 
 		buttonHelp = (ImageButton) findViewById(R.id.btnHelp);
 		buttonSend = (Button) findViewById(R.id.btnSend);
 		buttonHelp.setOnClickListener(this);
 		buttonSend.setOnClickListener(this);
 
 		// get parameters
 		Bundle bundle = getIntent().getExtras();
 		if (bundle != null) {
 			wish = bundle.getBoolean(KEY_WISHES_ALLOWED, false);
 			regard = bundle.getBoolean(KEY_REGARDS_ALLOWED, false);
 			numberOfWishes = bundle.getString(KEY_NUMBER_OF_WISHES);
 			editArtist.setText(bundle.getString(KEY_DEFAULT_INTERPRET));
 			editTitle.setText(bundle.getString(KEY_DEFAULT_TITLE));
 		}
 
		((TextView) findViewById(R.id.txtWishcount)).setText(numberOfWishes);
 		if (!wish) {
 			editArtist.setText(R.string.no_wishes_short);
 			editArtist.setEnabled(false);
 			editArtist.setVisibility(View.GONE);
 			editTitle.setText(R.string.no_wishes_short);
 			editTitle.setEnabled(false);
 			editTitle.setVisibility(View.GONE);
 		}
 		if (!regard) {
 			editRegard.setText(R.string.no_regards);
 			editRegard.setEnabled(false);
 			editRegard.setVisibility(View.GONE);
 		}
 	}
 
 	@Override
 	public void onDestroy() {
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putString(KEY_SP_NICK, editNick.getText().toString());
 		editor.commit();
 		super.onDestroy();
 	}
 
 	/**
 	 * checks if it is possible to wish something
 	 * 
 	 * @param allowedActions
 	 *            class for representing what is allowed
 	 * @return true if you can wish - false otherwise
 	 */
 	public static boolean canWishOrDisplayNot(Activity activity,
 			AllowedActions allowedActions) {
 		if (!allowedActions.moderated) {
 			MainActivity.alertMessage(activity,
 					activity.getString(R.string.no_moderator));
 			return false;
 		} else if (!allowedActions.wishes) {
 			MainActivity.alertMessage(activity,
 					activity.getString(R.string.no_wishes));
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * checks edit text objects for valid data
 	 * 
 	 * @return true if input is valid - false otherwise
 	 */
 	private boolean haveValidData() {
 		boolean haveNick = editNick.getText().length() != 0;
 		boolean haveWish = editArtist.getText().length() != 0
 				&& editTitle.getText().length() != 0 && editArtist.isEnabled()
 				&& editTitle.isEnabled();
 		boolean haveRegard = editRegard.getText().length() != 0
 				&& editRegard.isEnabled();
 		return haveNick && (haveWish || haveRegard);
 	}
 
 	@Override
 	public void onClick(View v) {
 		// send
 		if (v == buttonSend) {
 			if (!haveValidData()) {
 				runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						Toast.makeText(getApplicationContext(),
 								R.string.invalid_input, Toast.LENGTH_SHORT)
 								.show();
 					}
 				});
 			} else {
 				(new GetSender()).start();
 			}
 		} else if (v == buttonHelp) {
 			showInfo();
 		}
 	}
 
 	/**
 	 * shows info
 	 */
 	private void showInfo() {
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		alert.setTitle(R.string.notification);
 		final View v = getLayoutInflater().inflate(R.layout.dialog_text, null);
 		TextView tv = (TextView) v.findViewById(R.id.text);
 		tv.setText(R.string.wish_explanation);
 		alert.setView(v);
 		alert.setPositiveButton(R.string.ok, null);
 		alert.show();
 	}
 
 	/**
 	 * GetSender
 	 */
 	private class GetSender extends Thread {
 		@Override
 		public void run() {
 			// generate url
 			HttpClient client = new DefaultHttpClient();
 			HttpPost post = new HttpPost(
 					"http://www.metal-only.de/?action=wunschscript&do=save");
 			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
 			// add post values
 			if (!editNick.getText().toString().equals("")) {
 				pairs.add(new BasicNameValuePair("nick", editNick.getText()
 						.toString()));
 			}
 			if (!editArtist.getText().toString().equals("")
 					&& editArtist.isEnabled()) {
 				pairs.add(new BasicNameValuePair("artist", editArtist.getText()
 						.toString()));
 			}
 			if (!editTitle.getText().toString().equals("")
 					&& editTitle.isEnabled()) {
 				pairs.add(new BasicNameValuePair("song", editTitle.getText()
 						.toString()));
 			}
 			if (!editRegard.getText().toString().equals("")) {
 				pairs.add(new BasicNameValuePair("greet", editRegard.getText()
 						.toString()));
 			}
 
 			try {
 				// generate entity
 				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairs,
 						"UTF-8");
 				entity.setContentEncoding(HTTP.UTF_8);
 				// send post
 				post.setEntity(entity);
 				HttpResponse response = client.execute(post);
 				if (!response.getStatusLine().toString()
 						.equals("HTTP/1.1 200 OK")) {
 					Exception e = new Exception(
 							getString(R.string.sending_error));
 					throw e;
 				}
 				// success message
 				runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						Toast.makeText(getApplicationContext(), R.string.sent,
 								Toast.LENGTH_SHORT).show();
 						finish();
 					}
 				});
 			} catch (final Exception e) {
 				// display http error message
 				runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						Toast.makeText(getApplicationContext(), e.toString(),
 								Toast.LENGTH_LONG).show();
 					}
 				});
 			}
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == android.R.id.home) {
 			Intent intent = new Intent(this, MainActivity.class);
 			NavUtils.navigateUpTo(this, intent);
 			return true;
 		}
 		return false;
 	}
 }
