 package k11.encrunner;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.view.View;
 import android.view.View.OnClickListener;
import android.widget.TextView;
 
 public class MainMenu extends Activity implements OnClickListener {
     private SharedPreferences mPrefs;
    private TextView encIDTextView;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
        TextView encIDTextView = (TextView) findViewById(R.id.encID_text);
        SharedPreferences mPrefs = getSharedPreferences("prefs",0);
         if (mPrefs.getInt("encID", 0) != 0)
         	encIDTextView.setText(String.valueOf(mPrefs.getInt("encID", 0)));
         
         //Click listeners
         View partyButton = findViewById(R.id.party_button);
         partyButton.setOnClickListener(this);
         View characterButton = findViewById(R.id.character_button);
         characterButton.setOnClickListener(this);
         View tableButton = findViewById(R.id.table_button);
         tableButton.setOnClickListener(this);
         View scoreboardButton = findViewById(R.id.scoreboard_button);
         scoreboardButton.setOnClickListener(this);
         View optionsButton = findViewById(R.id.options_button);
         optionsButton.setOnClickListener(this);
         View aboutButton = findViewById(R.id.about_button);
         aboutButton.setOnClickListener(this);
        
        SharedPreferences.Editor ed = mPrefs.edit(); //Test
     }
     
     public void onPause()
     {
     	super.onPause();
     	
    	//SharedPreferences test = mPrefs;
        //SharedPreferences.Editor ed = mPrefs.edit();
        //ed.putInt("encID", Integer.getInteger((String) encIDTextView.getText()));
        //ed.commit();
 
     }
     
     public void onClick(View v)
     {
     	switch (v.getId())
     	{
     	case R.id.about_button:
     		Intent i = new Intent(this, About.class);
     		startActivity(i);
     		break;
     	case R.id.character_button:
     		Intent i2 = new Intent(this, Character.class);
     		startActivity(i2);
     		break;
     	}
     }
 }
