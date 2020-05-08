 package com.ahutlesson.android;
 
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.os.Bundle;
import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.MenuItem;
 
 public class BaseActivity extends SherlockActivity {
 
 	protected ActionBar actionBar;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		actionBar = getSupportActionBar();
 		enableHomeButton();
 		super.onCreate(savedInstanceState);
 	}
 
 	public void enableHomeButton() {
 		if(actionBar == null) return;
 		actionBar.setHomeButtonEnabled(true);
 		actionBar.setDisplayHomeAsUpEnabled(true);
 	}
 
 	public void disableHomeButton() {
 		if(actionBar == null) return;
 		actionBar.setHomeButtonEnabled(false);
 		actionBar.setDisplayHomeAsUpEnabled(false);
 	}
 	
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			finish();
 			return (true);
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	public void openActivity(Class<?> cls) {
 		Intent i = new Intent(this, cls);
 		startActivity(i);
 	}
 	
 	public void alert(String message) {
 		if(!this.hasWindowFocus()) return;
 		new AlertDialog.Builder(this)
 			.setTitle(R.string.app_name)
 			.setIcon(R.drawable.ahutlesson)
 			.setMessage(message)
 			.setPositiveButton(R.string.ok, null).show();
 	}
 	
 	public void alert(String title, String message) {
 		if(!this.hasWindowFocus()) return;
 		new AlertDialog.Builder(this)
 			.setTitle(title)
 			.setMessage(message)
 			.setPositiveButton(R.string.ok, null).show();
 	}
 	
	public void makeToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
 }
