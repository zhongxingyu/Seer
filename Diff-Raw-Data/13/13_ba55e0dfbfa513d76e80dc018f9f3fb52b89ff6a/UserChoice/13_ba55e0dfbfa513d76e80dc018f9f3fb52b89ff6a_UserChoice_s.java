 package nl.catchvifyoucan;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import nl.catchvifyoucan.dal.GetListOfGames;
 import nl.catchvifyoucan.dal.RequestsTasks;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences.Editor;
 import android.media.MediaPlayer;
 import android.os.Bundle;
import android.provider.Settings.Secure;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class UserChoice extends Activity {
 
 	private Button investigator, fugitive;
 	private EditText name, email;
	private String myID;
 	private String role;
 	private App app;
 	private Pattern pattern_name = Pattern.compile("[a-zA-Z]*");
	private Pattern pattern_email = Pattern.compile("[a-zA-Z0-9]{2,40}\\@[a-zA-Z0-9]{2,40}\\.[a-zA-Z0-9]{2,40}");
 	private Matcher matcher_name, matcher_email;
 	private MediaPlayer investigator_sound;
 	private MediaPlayer fugitive_sound;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		app = (App) getApplication();
 		setContentView(R.layout.user_choice);
 		init();
 	}
 
 	public void init() {

		myID = Secure.getString(app.getContentResolver(), Secure.ANDROID_ID);

 		fugitive_sound = MediaPlayer.create(app, R.raw.fugitive);
 		investigator_sound = MediaPlayer.create(app, R.raw.investigator);
 
 		investigator = (Button) findViewById(R.id.btn_investigator);
 		fugitive = (Button) findViewById(R.id.btn_fugitive);
 		name = (EditText) findViewById(R.id.user_name);
 		email = (EditText) findViewById(R.id.user_email);
 
 		investigator.setOnClickListener(listener);
 		fugitive.setOnClickListener(listener);
 	}
 
 	public OnClickListener listener = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			matcher_name = pattern_name.matcher(name.getText().toString());
 			matcher_email = pattern_email.matcher(email.getText().toString());
 			startActivity(new Intent(UserChoice.this, MainActivity.class));
 			switch (v.getId()) {
 				case R.id.btn_fugitive:
 					role = Constants.FUGITIVE;
 
 					if (texValidator()) {
 						setUser();
 						fugitive_sound.start();
 						startActivity(new Intent(UserChoice.this, MainActivity.class));
 					}
 					break;
 				case R.id.btn_investigator:
 					role = Constants.INVESTIGATOR;
 
 					if (texValidator()) {
 						setUser();
 						investigator_sound.start();
 						startActivity(new Intent(UserChoice.this, MainActivity.class));
 					}
 					break;
 				default:
 					break;
 			}
 		}
 	};
 
 	public boolean texValidator() {
		if (!matcher_name.matches()) {
 			name.setError(getResources().getString(R.string.error_name));
 			return false;
 		} else if (!matcher_email.matches()) {
 			email.setError(getResources().getString(R.string.error_email));
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	public void setUser() {
 		if (app.prefs != null) {
 			Editor edit = app.prefs.edit();
 			edit.putString(Constants.SP_USER_ROLE_LABEL, role);
 			edit.putString(Constants.SP_USER_NAME_LABEL, name.getText().toString());
 			edit.putString(Constants.SP_USER_EMAIL_LABEL, email.getText().toString());
 			edit.commit();
 		}
 		new RequestsTasks().execute(new GetListOfGames());
 	}
 }
