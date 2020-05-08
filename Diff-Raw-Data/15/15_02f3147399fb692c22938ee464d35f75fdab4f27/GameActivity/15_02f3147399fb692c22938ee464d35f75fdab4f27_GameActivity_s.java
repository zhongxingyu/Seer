 package de.mad.seminarapp.activity;
 
 import de.mad.seminarapp.R;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class GameActivity extends Activity {
 	
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE); 
         setContentView(R.layout.activity_game);
         
         final Button btnBack = (Button) findViewById(R.id.gameViewButton1);
         final TextView password = (TextView) findViewById(R.id.gameViewTextView2);
         
         Intent i = getIntent();
         
         String pw = i.getStringExtra("password");
         Log.e("GameView: ", pw + ".");
         
         password.setText(pw);
         
         btnBack.setOnClickListener(new OnClickListener(){
 
         	  public void onClick(View v){
        	    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        	    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);   
        	    startActivity(intent);
         	  }
         	});
 
 
     }
     
     public void gotToMainView(View view) {
     	Intent nextScreen = new Intent(getApplicationContext(), MainActivity.class);
     	nextScreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
     	startActivity(nextScreen);
     	finish();
     }
 }
