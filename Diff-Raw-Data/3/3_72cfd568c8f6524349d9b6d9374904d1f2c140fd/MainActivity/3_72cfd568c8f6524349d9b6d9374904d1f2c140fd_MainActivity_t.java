 package com.speakaboos.android.testIntent;
 
 import java.util.List;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	
 	public final static String EXTRA_MESSAGE = "com.speakaboos.android.firstapp.MESSAGE";
 	private static final int REQUEST_CODE = 10;
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	public static boolean isIntentAvailable(Context ctx, Intent intent) {
 	   final PackageManager mgr = ctx.getPackageManager();
 	   List<ResolveInfo> list =
 	      mgr.queryIntentActivities(intent, 
 	         PackageManager.MATCH_DEFAULT_ONLY);
 	   return list.size() > 0;
 	} 
 	
 	public void sendMessage(View view){
 		Intent intent = new Intent(this, DisplayMessageActivity.class);
 		EditText editText = (EditText) findViewById(R.id.edit_message);
 		String message = editText.getText().toString();
 		intent.putExtra(EXTRA_MESSAGE, message);
 		startActivity(intent);
 	}
 	
 	public void sendIntent(View view){
 		log("sendIntent");
 		
 		Intent sendIntent = new Intent();
 		sendIntent.setAction(Intent.ACTION_SEND);
 		sendIntent.putExtra(Intent.EXTRA_TEXT, "This is extra text to send.");
 		sendIntent.addCategory("android.intent.category.DEFAULT");
 		sendIntent.setType("text/plain");
 		
 		log("intentAvailable: "+ isIntentAvailable(this, sendIntent));
 		
 		startActivity(sendIntent);
 		//startActivity(Intent.createChooser(sendIntent, "Testing Intent Chooser."));
 	}
 	
 	public void sendExplicitIntent(View view){
 		Intent sendIntent = new Intent("com.speakaboos.android.intent.SHARE");
 		sendIntent.putExtra("extra1", "This is extra text to send -1.");
 		sendIntent.putExtra("extra2", "This is extra text to send -2.");
 		//sendIntent.addCategory("android.intent.category.DEFAULT");
 		sendIntent.setType("text/plain");
 		
 		log("intentAvailable: "+ isIntentAvailable(this, sendIntent));
 		
		//startActivityForResult(sendIntent, REQUEST_CODE);
		startActivity(Intent.createChooser(sendIntent, "Testing Intent Chooser."));
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		log("onActivityResult");
 		if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
 			if (data.hasExtra("returnKey1")) {
 			Toast.makeText(this, data.getExtras().getString("returnKey1"),
 			Toast.LENGTH_SHORT).show();
 			}
 		}
 	}
 	
 	public void log(String msg){
 		System.out.println(msg);
 	}
 
 }
