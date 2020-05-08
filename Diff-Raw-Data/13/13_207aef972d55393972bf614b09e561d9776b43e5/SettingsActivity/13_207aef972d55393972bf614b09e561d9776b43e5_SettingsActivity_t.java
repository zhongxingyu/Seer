 package com.example.git;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 
 public class SettingsActivity extends Activity {
 
 	private final String TAG = getClass().getName();
 	private String sshPrivateKeyPath = "";
 	private String sshPublicKeyPath = "";
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_settings);
 		
 		Log.d(TAG, SettingsActivity.this.getResources().getString(R.string.APPSETTINGS));
 		SharedPreferences settings = getSharedPreferences(SettingsActivity.this.getResources().getString(R.string.APPSETTINGS), 0);
 		sshPrivateKeyPath = settings.getString(SettingsActivity.this.getResources().getString(R.string.SSHPRIVATEKEYPATHSETTING), "");
 		sshPublicKeyPath = settings.getString(SettingsActivity.this.getResources().getString(R.string.SSHPUBLICKEYPATHSETTING), "");
 		
  	 	EditText sshPrivateKeyPathEditText = (EditText) findViewById(R.id.ssh_private_key_path);
 	 	//sshPrivateKeyPath = sshPrivateKeyPathEditText.getText().toString();
 	 	if (sshPrivateKeyPath != "") {
 	 		sshPrivateKeyPathEditText.setText(sshPrivateKeyPath);
 	 	}
  	 	sshPrivateKeyPathEditText.setEnabled(false);
 		
  	 	Button sshPrivateKeyPathButton = (Button) findViewById(R.id.button_select_folder_ssh_private_key_path);
  	 	sshPrivateKeyPathButton.setOnClickListener(new View.OnClickListener() {
  	 		public void onClick(View v) {
  				Intent intent = new Intent(SettingsActivity.this, BrowserActivity.class);
  				intent.putExtra("originOfRequestforResult", "sshPrivateKeyPathButton");
  				startActivityForResult(intent, 1);
  	 		}
  	 	});
  	 	
  	 	EditText sshPublicKeyPathEditText = (EditText) findViewById(R.id.ssh_public_key_path);
 	 	//sshPublicKeyPath = sshPublicKeyPathEditText.getText().toString();
 	 	if (sshPublicKeyPath != "") {
 	 		sshPublicKeyPathEditText.setText(sshPublicKeyPath);
 	 	}
  	 	sshPublicKeyPathEditText.setEnabled(false);
  	
   	Button sshPublicKeyPathButton = (Button) findViewById(R.id.button_select_folder_ssh_public_key_path);
   	sshPublicKeyPathButton.setOnClickListener(new View.OnClickListener() {
   			public void onClick(View v) {
    				Intent intent = new Intent(SettingsActivity.this, BrowserActivity.class);
    				intent.putExtra("originOfRequestforResult", "sshPublicKeyPathButton");
    				startActivityForResult(intent, 1);
   			}
   	});
   	
 		Button saveButton = (Button) findViewById(R.id.button_save);
 		saveButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				SharedPreferences settings = getSharedPreferences(SettingsActivity.this.getResources().getString(R.string.APPSETTINGS), 0);
 				SharedPreferences.Editor editor = settings.edit();
 				editor.putString(SettingsActivity.this.getResources().getString(R.string.SSHPRIVATEKEYPATHSETTING), sshPrivateKeyPath);
 				editor.putString(SettingsActivity.this.getResources().getString(R.string.SSHPUBLICKEYPATHSETTING), sshPublicKeyPath);
 				editor.commit();
 				finish();
 			}
 		});
 	}
 	
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == 1) {
 			if (resultCode == RESULT_OK) {
 				if (data.getStringExtra("originOfRequestforResult").equals("sshPrivateKeyPathButton")) {
 					sshPrivateKeyPath = data.getStringExtra("currentPath");
 		    }
 		    if (data.getStringExtra("originOfRequestforResult").equals("sshPublicKeyPathButton")) {
 		    	sshPublicKeyPath = data.getStringExtra("currentPath");
 		    }
 		    EditText sshPrivateKeyPathEditText = (EditText) findViewById(R.id.ssh_private_key_path);
 		    sshPrivateKeyPathEditText.setText(sshPrivateKeyPath);
 		    sshPrivateKeyPathEditText.setEnabled(false);
 		    			     	 	
 		    EditText sshPublicKeyPathEditText = (EditText) findViewById(R.id.ssh_public_key_path);
 		    sshPublicKeyPathEditText.setText(sshPublicKeyPath);
 		    sshPublicKeyPathEditText.setEnabled(false);					      
 		  }
 		  if (resultCode == RESULT_CANCELED) {
 		  	ToastNotification.makeToast("No valid choice!", Toast.LENGTH_LONG, SettingsActivity.this);
 		  }
 		}
 	}
 }
