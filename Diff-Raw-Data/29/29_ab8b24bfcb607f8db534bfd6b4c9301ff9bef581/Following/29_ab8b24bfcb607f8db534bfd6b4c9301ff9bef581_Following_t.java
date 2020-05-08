 package com.example.ucrinstagram;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.example.ucrinstagram.Models.User;
 
 public class Following extends Activity implements OnClickListener {
 	String Logineduser = Login.username.toLowerCase().replaceAll("\\s", "");
 	User[] following;
 	Button[] btn;
 	User user1;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_following);
 		user1 = new User(getIntent().getExtras().getString("username"));
 		following = user1.getFriends();
 		if (following.length >= 1) {
 			btn = new Button[following.length];
 			for (int i = 0; i < following.length; i++) {
 				System.out.println(following[i].username);
 				TextView f = new TextView(this);
 		        f.setText(following[i].username);
		        f.setTextSize(18);
 				LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayoutWithLotofContent);
 				linearLayout.addView(f);
				//System.out.println("Logineduser");
				if(Logineduser.equals(getIntent().getExtras().getString("username"))){
 					btn[i] = new Button(this);
 					btn[i].setText("Unfollow");
 					btn[i].setOnClickListener(this);
 					linearLayout.addView(btn[i]);
 				}
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_following, menu);
 		return true;
 	}
 
 	@Override
 	public void onClick(View view) {
 		for (int i = 0; i < following.length; i++) {
 			if (view == btn[i]) {
 				user1.removeFriend(following[i]);
 			}
 		}
 
 		finish();
 		startActivity(getIntent());
 	}
 
 }
