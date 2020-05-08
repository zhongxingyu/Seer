 package org.game.advwars;
 
 import android.app.Activity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnClickListener;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Toast;
 
public class SelectPlayerName extends Activity implements OnItemSelectedListener, android.view.View.OnClickListener
 {
 	private GUIGameValues ggv = new GUIGameValues();
 	
 	protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.select_player_name);
         
         Spinner spinner = (Spinner) findViewById(R.id.player_name_selection);
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                 this, R.array.player_names, android.R.layout.simple_spinner_item);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinner.setAdapter(adapter);
         
         spinner.setOnItemSelectedListener(new SelectPlayerName());
         
        Button selectPlayerName = (Button) findViewById(R.id.confirm_player_name);
        selectPlayerName.setOnClickListener(this);
     }
 
 	@Override
 	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
 	{
 	    Toast.makeText(parent.getContext(), "You selected " +
 	    parent.getItemAtPosition(pos).toString(), Toast.LENGTH_LONG).show();
 	    
 	    this.ggv.setPlayerName(parent.getItemAtPosition(pos).toString());
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> arg0)
 	{
 		// Do nothing	
 	}

	@Override
	public void onClick(View v)
 	{
 		if (!this.ggv.getPlayerName().equals(""))
 		{
 			Intent i = new Intent(this, MainMenu.class);
 			startActivity(i);
 		}
 		else
 		{
 			Intent i2 = new Intent(this, InvalidPlayerNameSelected.class);
 			startActivity(i2);
 		}
 	}
 }
