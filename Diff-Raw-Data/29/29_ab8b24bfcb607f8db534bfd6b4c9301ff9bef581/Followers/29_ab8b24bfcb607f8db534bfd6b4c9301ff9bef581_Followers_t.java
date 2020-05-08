 package com.example.ucrinstagram;
 
 import com.example.ucrinstagram.Models.User;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class Followers extends Activity implements OnClickListener {
 
 	String Logineduser = Login.username.toLowerCase().replaceAll("\\s", "");
 	User[] followers;
 	Button[] btn;
 	User user1;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_followers);
 
 		user1 = new User(getIntent().getExtras().getString("username"));
 		followers = user1.getFriendedBy();
 		if (followers.length >= 1) {
 			btn = new Button[followers.length];
 			for (int i = 0; i < followers.length; i++) {
 				System.out.println(followers[i].username);
 				TextView f = new TextView(this);
 				f.setText(followers[i].username);
		        f.setTextSize(18);
 				LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayoutWithLotofContent);
 
 				linearLayout.addView(f);
				if (Logineduser.equals(user1.username)){
 					btn[i] = new Button(this);
 					btn[i].setText("Follow");
 					btn[i].setOnClickListener(this);
 					linearLayout.addView(btn[i]);
 				}
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_followers, menu);
 		return true;
 	}
 
 	@Override
 	public void onClick(View view) {
 		for (int i = 0; i < followers.length; i++) {
 			if (view == btn[i]) {
 				user1.addFriend(followers[i]);
 			}
 		}
 
 		finish();
 		startActivity(getIntent());
 	}
 
 }
