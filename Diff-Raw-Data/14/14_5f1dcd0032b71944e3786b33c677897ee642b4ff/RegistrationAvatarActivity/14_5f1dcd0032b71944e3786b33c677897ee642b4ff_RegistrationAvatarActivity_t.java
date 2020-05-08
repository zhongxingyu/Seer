 package com.example.medicinereminder;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
import android.graphics.drawable.Drawable;
 import android.view.Menu;
 import android.view.View;
 
 public class RegistrationAvatarActivity extends Activity {
 	private int avatar;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_registration_avatar);
 		this.avatar= MyGuy.getUser().avatar;
 		if(avatar > 0 && avatar < 9){
 			View images = findViewById(R.id.SeventhPage);
 			View image = images.findViewWithTag(Integer.toString(avatar));
 			image.setBackgroundColor(Color.RED);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.registration_avatar, menu);
 		return true;
 	}
 	
 	public void submitForm(View v){
 		MyGuy.getUser().setAvatar(avatar);
 		//MyGuy.getUser().sendToDatabase(this);
 		AlarmSet alarm = new AlarmSet(this);
 		alarm.setAlarm();
 		Intent intent = new Intent(this, HomePageActivity.class);
 		startActivity(intent);
 		finish();
 	}
 	
 
 	public void toPrevPage(View v){
 		MyGuy.getUser().setAvatar(avatar);
 		Intent intent = new Intent(this, RegistrationRefillsActivity.class);
 		startActivity(intent);
 		finish();
 	}
 	
	public void clearSelection(Drawable back){
		for (int i=1;i<9;i++){
			View images = findViewById(R.id.SeventhPage);
			View image = images.findViewWithTag(Integer.toString(i));
			image.setBackgroundDrawable(back);		
		}
		
	}
	
 	public void selectAvatar(View v){
		Drawable old = v.getBackground();
		clearSelection(old);
 		v.setBackgroundColor(Color.RED);
 		String tag =(String)v.getTag();
 		avatar = Integer.parseInt(tag);
 		/*TODO Store avatar on click. Also, set other avatars background color to grey, so that
 		 *only one is selected at a time.
 		 */
 }
 
 }
