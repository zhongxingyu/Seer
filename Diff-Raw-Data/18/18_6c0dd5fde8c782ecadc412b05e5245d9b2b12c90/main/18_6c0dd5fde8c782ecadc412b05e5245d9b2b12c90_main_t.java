 package com.danesh.intercepturl;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class main extends Activity {
	Dialog dialog;
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		Intent getIntent=getIntent();
 		String path = "",filename = "";
 		if (getIntent!=null){
 			if (getIntent.getData()!=null){
 				path = getIntent.getData().toString();
 				filename = getIntent.getData().getLastPathSegment();
 			}			
 		}
 		if (path==null || filename==null || path=="" || filename==""){
 			final Dialog dialog = new Dialog(main.this);
 			dialog.setContentView(R.layout.dialog);
 			dialog.setTitle("            InterceptUrl");
			dialog.setCancelable(false);
 			Button button = (Button) dialog.findViewById(R.id.Accept);
 			Button button2 = (Button) dialog.findViewById(R.id.Cancel);
 			final CheckBox check = (CheckBox) dialog.findViewById(R.id.auto);
 			final EditText edit = (EditText) dialog.findViewById(R.id.text);
 			SharedPreferences settings = getSharedPreferences("prefs", 0);
 			if (settings.getString("download", "not")=="not"){
 				edit.setText("/sdcard/download");
 			}else{
 				edit.setText(settings.getString("download", "not"));
 			}
 			if (!settings.contains("auto")){
 				check.setChecked(true);
 			}else{
 				if (settings.getBoolean("auto", false)){
 					check.setChecked(true);
 				}else{
 					check.setChecked(false);
 				}
 			}
 			final ShellCommand cmd = new ShellCommand();
 			check.setOnCheckedChangeListener(new OnCheckedChangeListener()
 			{
 			    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
 			    {
 			        if ( isChecked )
 			        {
 			           cmd.canSU();
 			        }
 			    }
 			});
 			button.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					SharedPreferences settings = getSharedPreferences("prefs", 0);
 					SharedPreferences.Editor editor = settings.edit();
 					editor.putString("download",edit.getText().toString());
 					editor.putBoolean("auto", check.isChecked());
 					editor.commit();
 					if (check.isChecked())
 						cmd.canSU();
 					showToast("Preference Saved");
					dialog.dismiss();
					finish();
 				}
 			});
 			button2.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					showToast("Preferences Not Saved"); 
					dialog.dismiss();
 					finish();
 				}
 			}); 
 			dialog.show();
 		}else{
 			Intent mIntent = new Intent(this, LocalService.class);
 			Bundle mBundle = new Bundle();
 			mBundle.putString("path", path);
 			mBundle.putString("filename", filename);
 			mIntent.putExtras(mBundle);
 			startService(mIntent);
 			finish();
 		}
 	}

 	public void showToast(String msg) {
 		Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
 		error.show();
 	}
 }
