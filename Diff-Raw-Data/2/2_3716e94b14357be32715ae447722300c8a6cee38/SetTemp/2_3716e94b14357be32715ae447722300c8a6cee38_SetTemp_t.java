 package edu.calpoly.ai.skynest;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 public class SetTemp extends Activity implements OnSeekBarChangeListener {
 	
 	private double temperature;
 	private TempManager tm;
 	private SharedPreferences sp;
 	private TextView tempField;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_set_temp);
 		sp = this.getSharedPreferences(MainActivity.PREF_FILE, MODE_PRIVATE);
 		tm = new TempManager(sp);
 		temperature = tm.getPreferedTemp();
 		
 		initLayout();
 	}
 	
 	public void initLayout(){
 		final Button home_button = (Button) findViewById(R.id.button_home);
 		home_button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
         		Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
         		myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(myIntent);
             }
         });
 		
 		tempField = (TextView)findViewById(R.id.editText2);
		tempField.setText(Double.toString(temperature) + " F");
 		
 		SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar1);
 		seekBar.setProgress((int) temperature);
 		seekBar.setOnSeekBarChangeListener(this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.set_temp, menu);
 		return true;
 	}
 
 	@Override
 	public void onProgressChanged(SeekBar seekBar, int progress,
 			boolean fromUser) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void onStartTrackingTouch(SeekBar seekBar) {
 		temperature = seekBar.getProgress();
 		tempField.setText(Double.toString(temperature) + " F");
 	}
 
 	@Override
 	public void onStopTrackingTouch(SeekBar seekBar) {
 		temperature = seekBar.getProgress();
 		tm.setPreferedTemp(temperature);
 		tempField.setText(Double.toString(temperature) + " F");
 	}
 
 }
