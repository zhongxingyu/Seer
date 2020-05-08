 package cs252.lab6.atoms;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 
 public class Settings extends Activity implements OnClickListener {
 	int content = R.layout.settings;
 	
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		
 		setContentView(content);
 		
 		EditText player1 = (EditText) findViewById(R.id.player1);
 		player1.setOnClickListener(this);
 		
 		EditText player2 = (EditText) findViewById(R.id.player2);
 		player2.setOnClickListener(this);
 		
 		EditText player3 = (EditText) findViewById(R.id.player3);
 		player3.setOnClickListener(this);
 		
 		EditText player4 = (EditText) findViewById(R.id.player4);
 		player4.setOnClickListener(this);
 	}
 
 	@Override
	public void onClick(View arg0) {
 		// TODO Auto-generated method stub
		
 	}
 }
