 package chaw.App.First;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class IntroActivity extends Activity {
 	
 	public void onCreate(Bundle savedInstancestate) {
 		super.onCreate(savedInstancestate);
 		setContentView(R.layout.intro);
 		
 		Button facebook_btn = (Button) findViewById(R.id.facebook_button);
 		facebook_btn.setText("facebook login!!");
 		
 		facebook_btn.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Intent intent = new Intent(IntroActivity.this, FacebookLogIn.class);
 				startActivityForResult(intent, 1);
 			}
 		});
 		
 		Button signIn_btn = (Button) findViewById(R.id.signIn_button);
 		signIn_btn.setText("ȸ!");
 		
 		signIn_btn.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Intent intent = new Intent(IntroActivity.this, SignIn.class);
				startActivityForResult(intent, 1);
 			}
 		});
 	}
 	
 	protected void onActivityResult(int requestCode, int resultCode, Intent data){
 		super.onActivityResult(requestCode, resultCode, data);
 		TextView name_view = (TextView)findViewById(R.id.name_view);
 		TextView email_view = (TextView)findViewById(R.id.email_view);
 		name_view.setText(data.getStringExtra("name"));
 		email_view.setText(data.getStringExtra("email"));
 		android.util.Log.d("user_info", data.getStringExtra("email"));
 	}
 }
