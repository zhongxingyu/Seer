 package com.MusicalSketches;
 
 import java.util.Date;
 import java.util.Calendar;
 
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.*;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.DialogFragment;
 import android.app.ListActivity;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ImageView;
 import android.widget.Button;
 import android.view.*;
 import com.MusicalSketches.datarep.*;
 
 public class SongSelect extends Activity {
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.new_song_setting);
 
 	    Spinner spinner = (Spinner) findViewById(R.id.spinner);
 	    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
 	            this, R.array.planets_array, android.R.layout.simple_spinner_item);
 	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    spinner.setAdapter(adapter);
 	    spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
 	    
 	    Spinner button1 = (Spinner) findViewById(R.id.button1);
 	    ArrayAdapter<CharSequence> adapterb = ArrayAdapter.createFromResource(
 	            this, R.array.clef_array, android.R.layout.simple_spinner_item);
 	    adapterb.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    button1.setAdapter(adapterb);
 	    button1.setOnItemSelectedListener(new MyOnItemSelectedListener());
 	    
 	    Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
 	    ArrayAdapter<CharSequence> adapterc = ArrayAdapter.createFromResource(
 	            this, R.array.meter_array, android.R.layout.simple_spinner_item);
 	    adapterc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    spinner1.setAdapter(adapterc);
 	    spinner1.setOnItemSelectedListener(new MyOnItemSelectedListener());
 	    
 	    //Should go to Edit mode once that is done
 	    Button setButton = (Button) findViewById(R.id.button2);
 	    setButton.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
             	
             	Date d = new Date();
             	Song song = new Song(d);
             	
             	//Set Clef
             	Spinner button1 = (Spinner) findViewById(R.id.button1);
             	if (button1.getSelectedItem().toString() == "Bass"){
             		//Set to bass clef.  Bass is 0
             		song.setClef(0);
            	}
            	else {
             		//treble is 1.
             		song.setClef(1);
             	}
             	
             	//Set Tempo
             	EditText tempo = (EditText) findViewById(R.id.editText2);
             	song.setTempo(Integer.valueOf(tempo.getText().toString()));
             	
             	//Set Title
             	EditText title = (EditText) findViewById(R.id.editText1);
             	song.setTitle(title.getText().toString());
             	
             	//Set Meter
             	Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
             	if(spinner1.getPrompt().toString().length() == 4){
             		song.setMeter(12,8);
             	}
             	else {
             		String str = spinner1.getSelectedItem().toString();
            		song.setMeter(str.charAt(0), str.charAt(2));
             	}
             	
             	
             	//Toast for testing attribute setting
             	Toast.makeText(getApplicationContext(), tempo.getText().toString(), 2).show();
             	Intent next = new Intent(SongSelect.this,EditMode.class);
             	next.putExtra("song object", song);
 				startActivityForResult(next, 0);
             }
         });
 	    
 	    //Should go back to library
 	    Button cancelButton = (Button) findViewById(R.id.button3);
 	    cancelButton.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
             	//Intent next = new Intent(SongSelect.this,MusicalLibrary.class);
 				//startActivity(next);
 				finish();
             }
         });
 	        
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		setResult(resultCode, data);
 		finish();
 	}
 	
 	
 	public class MyOnItemSelectedListener implements OnItemSelectedListener {
 
 	    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
 	    }
 
 	    public void onNothingSelected(AdapterView parent) {
 	      // Do nothing.
 	    }
 	}
 }
