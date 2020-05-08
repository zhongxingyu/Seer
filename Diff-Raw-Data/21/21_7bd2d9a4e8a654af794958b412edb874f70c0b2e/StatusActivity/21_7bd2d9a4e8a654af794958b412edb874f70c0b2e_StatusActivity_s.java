 package pt.isel.pdm.yamba;
 
 import pt.isel.pdm.yamba.TwitterAsync.TwitterAsync;
 import pt.isel.pdm.yamba.TwitterAsync.listeners.StatusPublishedListener;
 import winterwell.jtwitter.Twitter.Status;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 
 public class StatusActivity extends MenuActivity implements OnClickListener, TextWatcher, OnSharedPreferenceChangeListener, StatusPublishedListener {
 	private static final String TAG = "PDM";
 	private Button submit;
 	private EditText text;
 	private TextView charsCount;
 	
 	private int maxChar = -1;
 	private SharedPreferences _preferences;
 	
 	private TwitterAsync twitter;
 	
 	private boolean _sending;
 	
 	private static final String SENDING_KEY = "SENDING";
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	Log.d(TAG,"onCreate");
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         submit = (Button) findViewById(R.id.buttonUpdate);
         submit.setOnClickListener(this);
         text = (EditText) findViewById(R.id.editText);
         text.addTextChangedListener(this);
         charsCount = (TextView) findViewById(R.id.charLeft);
 
 		_preferences = PreferenceManager.getDefaultSharedPreferences(this);
 		_preferences.registerOnSharedPreferenceChangeListener(this);
 		
 		twitter = TwitterAsync.connect();
 		twitter.setStatusPublishedListener(this);
     }   
     
     @Override
 	protected void onResume() {
 		super.onResume();
 
 		twitter.setStatusPublishedListener(this);
 		updatePreferences(null);   
 		
 		if(_sending)
 			disableSubmit();
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		
 		_sending = savedInstanceState.getBoolean(SENDING_KEY);
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		
 		outState.putBoolean(SENDING_KEY, _sending);
 	}
 
 	@Override
     protected void doModifyMenu(Menu menu) {
     	
     	enableMenuItem(menu, R.id.show_timeline);
     }
     
 	public void onClick(View v) {
     	Log.d(TAG,"onClick");
     	disableSubmit();
     	updateStatus();
 	}
 
 	private void updateStatus() {		
 		long startTm = System.currentTimeMillis();
 		
 		twitter.updateStatusAsync(this, text.getText().toString());
 		
 		long elapsedTm = System.currentTimeMillis()-startTm;
     	Log.d(TAG,"Submited. Elapsed time="+elapsedTm+", text="+text);
 	}
 	
 	/** Updates the values that are dependent of preferences, and all that depends on those values.
 	 * 
 	 * @param name - The name of the preference to update, or null if all.
 	 * @return true, if any preferences were changed.
 	 */
 	private boolean updatePreferences(String name) {
 		boolean changedPreference = false;
 				
 		if(name == null || name.equalsIgnoreCase("maxCharStatus")) {
 			maxChar = Integer.parseInt(_preferences.getString("maxCharStatus", null));
 			changedPreference = true;	
 					
 			//Update dependents
 			updateCharCount(text.getText());
 		}
 		
 		return changedPreference;
 	}
 
 	private void enableSubmit() {
 		submit.setEnabled(true);
     	submit.setText(R.string.buttonUpdate);
     	
 		_sending = false;
 	}
 
 	private void disableSubmit() {
 		submit.setEnabled(false);
     	submit.setText(R.string.buttonBusy);
 		
 		_sending = true;
 	}
 
 	public void afterTextChanged(Editable arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
 			int arg3) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
 		updateCharCount(arg0);
 	}
 	
 	private void updateCharCount(CharSequence text) {
 		int charsLeft = maxChar - text.length();
 		charsCount.setText(String.format("%d", charsLeft));
 		
 		setCharsLeftColor(charsLeft);
 	}	
 	
 	private void setCharsLeftColor(int charsLeft) {
 		if(charsLeft < 0)
 		{
 			charsCount.setTextColor(getResources().getColor(R.color.chars_left_error));
 		}
 		else if(charsLeft < 20)
 		{
 			charsCount.setTextColor(getResources().getColor(R.color.chars_left_danger));
 		}
 		else
 		{
 			charsCount.setTextColor(getResources().getColor(R.color.chars_left_normal));
 		}
 	}
 
 	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
 		
 		updatePreferences(arg1);
 		
 	}
 
 	@Override
 	public void onStatusPublished(Status status) {
 		
 		enableSubmit();
 		
 	}
 }
