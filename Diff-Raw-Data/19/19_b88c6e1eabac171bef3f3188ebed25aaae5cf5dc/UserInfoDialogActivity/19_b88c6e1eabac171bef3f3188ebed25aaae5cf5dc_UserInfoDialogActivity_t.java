 package ru.romanov.schedule.src;
 
 import ru.romanov.schedule.R;
 import ru.romanov.schedule.utils.StringConstants;
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.provider.ContactsContract.CommonDataKinds.Email;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class UserInfoDialogActivity extends Activity implements OnClickListener{
 
 	
 	private Button exitBT;
 	private TextView nameTV;
 	private TextView phoneTV;
 	private TextView emailTV;
 	
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.info_dialog_layout);
 		this.exitBT = (Button) findViewById(R.id.info_exit_but);
 		this.phoneTV = (TextView) findViewById(R.id.info_phone);
 		this.nameTV = (TextView) findViewById(R.id.info_name);
 		this.emailTV = (TextView) findViewById(R.id.info_email);
 	}
 	@Override
 	protected void onStart() {
 		SharedPreferences sp = getSharedPreferences(StringConstants.SCHEDULE_SHARED_PREFERENCES, MODE_PRIVATE);
 		this.emailTV.setText(sp.getString(StringConstants.SHARED_EMAIL, "-"));
 		this.nameTV.setText(sp.getString(StringConstants.SHARED_NAME, "-"));
		this.phoneTV.setText(sp.getString(StringConstants.SHARED_PHONE, "-"));
 		this.exitBT.setOnClickListener(this);
 		super.onStart();
 	}
 	@Override
 	public void onClick(View arg0) {
 		finish();
 		
 	}
 }
