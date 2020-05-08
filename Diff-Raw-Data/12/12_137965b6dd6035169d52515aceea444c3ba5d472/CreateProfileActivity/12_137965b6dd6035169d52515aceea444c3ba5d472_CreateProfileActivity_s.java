 package org.touchirc.activity;
 
 import org.touchirc.R;
 import org.touchirc.TouchIrc;
 import org.touchirc.model.Profile;
 import org.touchirc.utils.Regex;
 
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.text.Spannable;
 import android.text.SpannableStringBuilder;
 import android.text.style.ForegroundColorSpan;
 import android.text.style.StyleSpan;
 import android.view.KeyEvent;
 import android.view.WindowManager;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 public class CreateProfileActivity extends SherlockActivity{
 
 	private TextView profileName_TV;
 	private EditText profileName_ET;
 
 	private TextView firstNickname_TV;
 	private EditText firstNickname_ET;
 
 	private TextView secondNickname_TV;
 	private EditText secondNickname_ET;
 
 	private TextView thirdNickname_TV;
 	private EditText thirdNickname_ET;
 
 	private TextView userName_TV;
 	private EditText userName_ET;
 
 	private TextView realName_TV;
 	private EditText realName_ET;
 
 	private Profile prof;
 	private Bundle bundleEdit = null;
 	private Bundle bundleAddFromMenu = null;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Allows to the actionBar's icon to do "Previous"
 		ActionBar actionBar = getSupportActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		actionBar.setTitle("Create a Profile");
 
 		// Prevents the keyboard to be displayed when you come on this activity
 		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
 
 		setContentView(R.layout.create_profile_layout);
 
 		Intent i = getIntent();
 
 		// Collect the Bundle object if the activity is started by ExistingProfilesActivity : Add or Edit
 		bundleEdit = i.getBundleExtra("ProfileIdentification");
 		bundleAddFromMenu = i.getBundleExtra("AddingFromExistingProfilesActivity");
 
 		// TextViews : black ones (default color)s are compulsories, gray ones are optionals
 		// Meanwhile, we can set the "default" value of the textView
 
 		this.profileName_TV = (TextView) findViewById(R.id.textView_profile_name);
 
 		this.firstNickname_TV = (TextView) findViewById(R.id.textView_first_nick);
 
 		this.secondNickname_TV = (TextView) findViewById(R.id.textView_second_nick);
 		this.secondNickname_TV.setTextColor(Color.GRAY);
 
 		this.thirdNickname_TV = (TextView) findViewById(R.id.textView_third_nick);
 		this.thirdNickname_TV.setTextColor(Color.GRAY);
 
 		this.userName_TV = (TextView) findViewById(R.id.textView_username);
 
 		this.realName_TV = (TextView) findViewById(R.id.textView_realname);
 
 		// EditTexts		
 
 		this.profileName_ET = (EditText) findViewById(R.id.editText_profile_name);
 		this.firstNickname_ET = (EditText) findViewById(R.id.editText_first_nick);
 		this.secondNickname_ET = (EditText) findViewById(R.id.editText_second_nick);
 		this.thirdNickname_ET = (EditText) findViewById(R.id.editText_third_nick);
 		this.userName_ET = (EditText) findViewById(R.id.editText_username);
 		this.realName_ET = (EditText) findViewById(R.id.editText_realname);
 
 		// if started by ExistingProfilesActivity, changing the EditTexts'value
 		if(bundleEdit != null && bundleEdit.containsKey("ProfileId")){
 			// We collect the profile from available profiles list
			Profile profileToEdit = TouchIrc.getInstance().getAvailableProfiles().valueAt(bundleEdit.getInt("ProfileId"));
 
 			// And put values in corresponding editText
 			this.profileName_ET.setText(profileToEdit.getProfile_name());
 			this.firstNickname_ET.setText(profileToEdit.getFirstNick());
 			this.secondNickname_ET.setText(profileToEdit.getSecondNick());
 			this.thirdNickname_ET.setText(profileToEdit.getThirdNick());
 			this.userName_ET.setText(profileToEdit.getUsername());
 			this.realName_ET.setText(profileToEdit.getRealname());
 		}
 	}
 
 	/**
 	 * Define the display of the actionBar
 	 * 
 	 * @param menu
 	 * @return true
 	 */
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu){
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.create_object_activity, menu);
 		return true;
 	}
 
 	/**
 	 * 
 	 * This method allows you to configure the behavior of the icon 
 	 * in the action bar: When this button is clicked, the current activity 
 	 * is removed and the previous activity becomes the current activity
 	 * 
 	 */
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// app icon in action bar clicked; go home
 			Intent intent = new Intent(this, MenuActivity.class);
 			// According to the origin of the triggering of the activity
 			if(bundleEdit != null || bundleAddFromMenu != null){
 				intent.setClass(this, ExistingProfilesActivity.class);
 			}
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			return true;
 
 		case R.id.save :
 
 			// If the user push again (after some errors)
 			puttDefaultTextViews();
 
 			if(addProfile()){				
 				finish(); // close the activity
 			}
 			return true;
 
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void puttDefaultTextViews(){
 
 		this.profileName_TV.setText(R.string.profileName);
 		this.firstNickname_TV.setText(R.string.firstNick);
 		this.secondNickname_TV.setText(R.string.secondNick);
 		this.secondNickname_TV.setTextColor(Color.GRAY);
 		this.thirdNickname_TV.setText(R.string.thirdNick);
 		this.thirdNickname_TV.setTextColor(Color.GRAY);
 		this.userName_TV.setText(R.string.userName);
 		this.realName_TV.setText(R.string.realName);
 
 	}
 
 	private void missingInformation() {
 
 		if(realName_ET.getText().length() == 0){
 
 			SpannableStringBuilder sb = new SpannableStringBuilder(getResources().getText(R.string.realNameNotCompleted));
 			ForegroundColorSpan fcs = new ForegroundColorSpan(Color.RED);
 			StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); 
 			sb.setSpan(fcs, 8, 22, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
 			sb.setSpan(bss, 8, 22, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
 			this.realName_TV.setText(sb);
 
 			realName_ET.requestFocus();
 
 		}
 		if(userName_ET.getText().length() == 0){
 
 			SpannableStringBuilder sb = new SpannableStringBuilder(getResources().getText(R.string.userNameNotCompleted));
 			ForegroundColorSpan fcs = new ForegroundColorSpan(Color.RED);
 			StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); 
 			sb.setSpan(fcs, 8, 22, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
 			sb.setSpan(bss, 8, 22, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
 			this.userName_TV.setText(sb);
 
 			userName_ET.requestFocus();
 
 		}
 
 		if(firstNickname_ET.getText().length() == 0){
 
 			SpannableStringBuilder sb = new SpannableStringBuilder(getResources().getText(R.string.firstNickNameNotCompleted));
 			ForegroundColorSpan fcs = new ForegroundColorSpan(Color.RED);
 			StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); 
 			sb.setSpan(fcs, 14, 28, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
 			sb.setSpan(bss, 14, 28, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
 			this.firstNickname_TV.setText(sb);
 
 			firstNickname_ET.requestFocus();
 
 		}
 
 		if(profileName_ET.getText().length() == 0){
 
 			SpannableStringBuilder sb = new SpannableStringBuilder(getResources().getText(R.string.profileNameNotCompleted));
 			ForegroundColorSpan fcs = new ForegroundColorSpan(Color.RED);
 			StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); 
 			sb.setSpan(fcs, 14, 28, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
 			sb.setSpan(bss, 14, 28, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
 			this.profileName_TV.setText(sb);
 
 			profileName_ET.requestFocus();
 
 		}
 	}
 
 	private boolean addProfile() {
 
 		String fnn = firstNickname_ET.getText().toString();
 		String snn = secondNickname_ET.getText().toString();
 		String tnn = thirdNickname_ET.getText().toString();
 		
 		// We check if important values are not specified
 		missingInformation();
 		// Control the validity of the nicknames
 		boolean validFirstNickname = Regex.isAValidNickName(firstNickname_ET.getText().toString());
 		boolean validSecondNickname = Regex.isAValidNickName(secondNickname_ET.getText().toString());
 		boolean validThirdNickname = Regex.isAValidNickName(thirdNickname_ET.getText().toString());
 
 		if(validFirstNickname || validSecondNickname || validThirdNickname){
 
 			/** ------------------------------------------------------------------------- **/
 
 			// Check if the nicknames are the same
 
 			if(!snn.equals("") || !tnn.equals("")){
 				boolean sameNickNames = false;
 
 				if(snn.equals(fnn)){
 					SpannableStringBuilder sb = new SpannableStringBuilder(getResources().getText(R.string.secondNickNameAlreadyInUse));
 					ForegroundColorSpan fcs = new ForegroundColorSpan(Color.RED);
 					StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); 
 					sb.setSpan(fcs, 16, 30, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
 					sb.setSpan(bss, 16, 30, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
 					this.secondNickname_TV.setText(sb);
 
 					sameNickNames = true;
 				}
 
 				if(tnn.equals(snn) || tnn.equals(fnn)){
 					SpannableStringBuilder sb = new SpannableStringBuilder(getResources().getText(R.string.thirdNickNameAlreadyInUse));
 					ForegroundColorSpan fcs = new ForegroundColorSpan(Color.RED);
 					StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); 
 					sb.setSpan(fcs, 15, 29, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
 					sb.setSpan(bss, 15, 29, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
 					this.thirdNickname_TV.setText(sb);
 
 					sameNickNames = true;
 				}
 
 				if(sameNickNames == true){
 					return false;
 				}	
 			}
 			// Add a suffix on 2nd and 3rd nicknames if the user don't input them
 			else{
 				snn = fnn + "_";
 				tnn = fnn + "__";
 			}
 
 			/** ------------------------------------------------------------------------- **/
 
 			// the Profile is created with the datas given by the user
 
 			prof = new Profile(profileName_ET.getText().toString(),
 					fnn,
 					snn,
 					tnn,
 					userName_ET.getText().toString(),
 					realName_ET.getText().toString());
 
 			/** ------------------------------------------------------------------------- **/
 
 			// ADD or UPDATE the profile
 
 			String s, p = getResources().getString(R.string.theProfile);
 			Intent i = new Intent(CreateProfileActivity.this, ExistingProfilesActivity.class);
 			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 
 			if(bundleEdit != null){
 				// Update the Profile in the database (+1 since the ID in Database begins at 0)
 				s = getResources().getString(R.string.hasBeenModified);
				TouchIrc.getInstance().updateProfile(bundleEdit.getInt("ProfileId")+1, prof, getApplicationContext());
 				Toast.makeText(getApplicationContext(), p + " " + prof.getProfile_name() + " " 
 				+ s, Toast.LENGTH_SHORT).show();
 
 				startActivity(i);
 
 			}
 			else{
 
 				if(!TouchIrc.getInstance().addProfile(prof, this)){
 
 					SpannableStringBuilder sb = new SpannableStringBuilder(getResources().getText(R.string.profileNameAlreadyInUse));
 					ForegroundColorSpan fcs = new ForegroundColorSpan(Color.RED);
 					StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); 
 					sb.setSpan(fcs, 15, 29, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
 					sb.setSpan(bss, 15, 29, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
 					this.profileName_TV.setText(sb);
 
 					return false;
 				}
 				else{
 
 					// Add the Profile just created into the database
 					s = getResources().getString(R.string.hasBeenAdded);
 					System.out.println(s);
 					Toast.makeText(getApplicationContext(), p + " " + prof.getProfile_name() + 
 							" " + s, Toast.LENGTH_SHORT).show();
 					if(bundleAddFromMenu == null){
 						i.setClass(getApplicationContext(), MenuActivity.class);
 					}
 					startActivity(i);
 				}
 
 			}
 
 			/** ------------------------------------------------------------ **/
 
 			// No problems concerning values
 			return true;
 
 		}
 
 		/** ------------------------------------------------------------ **/
 
 		// If some nicknames are invalids
 
 		else{
 			if(!validFirstNickname && !fnn.equals("")){
 
 				SpannableStringBuilder sb = new SpannableStringBuilder(getResources().getText(R.string.firstNickNameInvalid));
 				ForegroundColorSpan fcs = new ForegroundColorSpan(Color.RED);
 				StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); 
 				sb.setSpan(fcs, 14, 22, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
 				sb.setSpan(bss, 14, 22, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
 				this.firstNickname_TV.setText(sb);
 
 			}
 			if(!validSecondNickname && !snn.equals("")){
 
 				SpannableStringBuilder sb = new SpannableStringBuilder(getResources().getText(R.string.secondNickNameInvalid));
 				ForegroundColorSpan fcs = new ForegroundColorSpan(Color.RED);
 				StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); 
 				sb.setSpan(fcs, 15, 23, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
 				sb.setSpan(bss, 15, 23, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
 				this.secondNickname_TV.setText(sb);
 
 			}
 			if(!validThirdNickname && !tnn.equals("")){
 
 				SpannableStringBuilder sb = new SpannableStringBuilder(getResources().getText(R.string.thirdNickNameInvalid));
 				ForegroundColorSpan fcs = new ForegroundColorSpan(Color.RED);
 				StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); 
 				sb.setSpan(fcs, 15, 22, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
 				sb.setSpan(bss, 15, 22, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
 				this.thirdNickname_TV.setText(sb);
 
 			}
 
 			// Some values are missing/invalids
 			return false;
 		}
 
 		/** ------------------------------------------------------------ **/
 
 	}
 
 	/**
 	 * 
 	 * This method allows you to configure the behavior of the physical button 
 	 * "Back" (on device) : When this button is clicked, the current activity 
 	 * is removed and the previous activity becomes the current activity
 	 * 
 	 */
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event)  {
 		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
 
 			Intent intent = new Intent(this, MenuActivity.class);
 			// According to the origin of the triggering of the activity
 			if(bundleEdit != null || bundleAddFromMenu != null){
 				intent.setClass(this, ExistingProfilesActivity.class);
 			}
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			return true;
 		}
 
 		return super.onKeyDown(keyCode, event);
 	}
 }
