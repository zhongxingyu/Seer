 package cornell.eickleapp;
 
 import cornell.eickleapp.R;
 import cornell.eickleapp.R.id;
 import cornell.eickleapp.R.layout;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 
 
 public class DrinkAssessment extends Activity implements OnClickListener { 
 
 	private DatabaseHandler db;
 	private EditText drink_num;
 	private SeekBar excess_drinks;
 	private int progress_changed;
 	private Button finish;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		db = new DatabaseHandler(this);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.drink_assessment);
 		finish = (Button) findViewById(R.id.drink_assess_finish);
 		finish.setOnClickListener(this);
 		
 		drink_num = (EditText)findViewById(R.id.number_drinks);
 		
 		progress_changed = 0;
 		excess_drinks = (SeekBar)findViewById(R.id.excess_drinks);		
 		
 		excess_drinks.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 			public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser){
 				progress_changed = progress;
 			}
 			
 			public void onStartTrackingTouch(SeekBar seekbar){
 				
 			}
 			public void onStopTrackingTouch(SeekBar seekbar){
 				
 			}
 		});
 		excess_drinks.setProgress(0); 
 		excess_drinks.setMax(100);
 		
 		
 	}
 	
 	private void saveToDB(){
 		//Add the users guess for num drinks to db
 		if(drink_num.getText() != null){
 			db.addValue("drink_guess", Integer.parseInt(drink_num.getText().toString()));
 		}
 		//Add the value for the excess slider to db
 		db.addValue("drink_excess",progress_changed);
 		db.addValue("drink_assess", "True");
 	}
 	
 	@Override
 	public void onClick(View view) {
 		switch (view.getId()) {
 		case R.id.drink_assess_finish:
 			saveToDB();
 			Intent intent = new Intent();
			setResult(3, intent);
 			finish();
 			break;
 		}
 	}
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 		Intent intent = new Intent();
 		setResult(0, intent);
 		finish();
 	}
 
 }
 
