 package cornell.eickleapp;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.RadioGroup;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 
 public class AfterDrinkSurvey extends Activity implements OnClickListener{
 
 	private CheckBox beer_chk, wine_chk, liquor_chk; 
 	private SeekBar recall_bar, regret_bar;
 	private RadioGroup vomit_group;
 	private CheckBox sym_fatigue, sym_nausea, sym_headache, sym_vomit;
 	private Button save_btn;
 	
 	private String regret_result, recall_result, vomit_result;
 	
 	private DatabaseHandler db;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.after_drinking_survey);
 		
 		db = new DatabaseHandler(this);
 		
 		save_btn = (Button)findViewById(R.id.save_survey);
 		
 		recall_bar = (SeekBar)findViewById(R.id.recall_bar);
 		regret_bar = (SeekBar)findViewById(R.id.regret_bar);
 		
 		beer_chk = (CheckBox) findViewById(R.id.type_beer);
 		wine_chk = (CheckBox) findViewById(R.id.type_wine);
 		liquor_chk = (CheckBox) findViewById(R.id.type_liquor);
 		
 		vomit_group = (RadioGroup) findViewById(R.id.vomit_group);
 		
 		sym_fatigue = (CheckBox) findViewById(R.id.symptom_fatigue);
 		sym_headache = (CheckBox) findViewById(R.id.symptom_headache);
 		sym_nausea = (CheckBox) findViewById(R.id.symptom_nausea);
 		sym_vomit = (CheckBox) findViewById(R.id.symptom_vomit);
 		
 		regret_result = "";
 		recall_result = "";
 		
 		save_btn.setOnClickListener(this);
 	
 		
 		initializeRecallSeekBar();
 		initializeRegretSeekBar();
 		initializeVomitGroup();
 	}
 
 	private void initializeVomitGroup(){
 		vomit_group.setOnCheckedChangeListener(
 				new RadioGroup.OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(RadioGroup group, int checkedId) {
 				switch (checkedId) {
 					case R.id.yes_vomit:
 							vomit_result= "yes";
 							break;
 					case R.id.no_vomit:
 							vomit_result = "no";
 							break;
 					default:
 						throw new RuntimeException(
 									"Unknown Button ID For Location Question.");
 					}
 				}
 
 			});
 	}
 	
 	
 	private void initializeRecallSeekBar() {
 		recall_bar.setProgress(50);
 		recall_bar.setMax(100);
 		recall_bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				recall_result = String.valueOf(progress);
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {
 			}
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {
 			}
 		});
 	}
 	
 	private void initializeRegretSeekBar() {
 		regret_bar.setProgress(50);
 		regret_bar.setMax(100);
 		regret_bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				regret_result = String.valueOf(progress);
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {
 			}
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {
 			}
 		});
 	}
 	
 	private void saveToDB(){
 		if(!recall_result.equals("")){
 			db.updateOrAdd("recall", recall_result);
 		}
 		if(!regret_result.equals("")){
 			db.updateOrAdd("regret", regret_result);
 		}
 		if(!vomit_result.equals("")){
 			db.updateOrAdd("vomit", vomit_result);
 		}
 		saveCheckBoxes();
 		
 	}
 	
 	private void saveCheckBoxes(){
 		db.updateOrAdd("type_beer", getCheckValue(beer_chk));
 		db.updateOrAdd("type_wine", getCheckValue(wine_chk));
 		db.updateOrAdd("type_liquor", getCheckValue(liquor_chk));
 		db.updateOrAdd("symptom_fatigue", getCheckValue(sym_fatigue));
 		db.updateOrAdd("symptom_headache", getCheckValue(sym_headache));
 		db.updateOrAdd("symptom_nausea", getCheckValue(sym_nausea));
 		db.updateOrAdd("symptom_vomit", getCheckValue(sym_vomit));
 	}
 	
 	public int getCheckValue(CheckBox chk){
 		if(chk.isChecked()){
 			return 1;
 		}else{
 			return 0;
 		}
 	}
 	
 	@Override
 	public void onClick(View view) {
 		switch(view.getId()){
 		case R.id.save_survey:
 			saveToDB();
 			db.close();
 			finish();
 			break;
 		}
 		
 	}
 	
 	protected void onPause() {
 		super.onPause();
 		finish();
 	}
 
 }
