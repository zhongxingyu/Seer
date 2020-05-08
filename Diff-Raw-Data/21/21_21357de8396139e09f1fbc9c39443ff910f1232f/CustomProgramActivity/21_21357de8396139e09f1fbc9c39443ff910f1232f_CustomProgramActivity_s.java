 package com.freescale.iastate.washer;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.FloatMath;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.LinearLayout;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.Switch;
 import android.widget.TextView;
import android.widget.Toast;
 import android.widget.ToggleButton;
 
 import com.freescale.iastate.washer.data.ProgramDataSource;
 import com.freescale.iastate.washer.util.CustomRadioGroup;
 import com.freescale.iastate.washer.util.Cycle;
 import com.freescale.iastate.washer.util.Cycle.Level;
 import com.freescale.iastate.washer.util.Cycle.Size;
 import com.freescale.iastate.washer.util.Cycle.Temperature;
 import com.freescale.iastate.washer.util.MenuInterface;
 import com.freescale.iastate.washer.util.Program;
 
 public class CustomProgramActivity extends Activity implements MenuInterface {
 	
 	String selection;
 	private ProgramDataSource datasource;
 	private Program program;
 	
 	Switch presoakSwitch;
 	Switch steamSwitch;
 	
 	// Agitation Level 
 	TextView agLevelText;
 	float agLevelStart = 0;
 	float agLevelEnd = 4;
 	int agLevel = 2;
 	
 	// Spin Speed 
 	TextView spinText;
 	float spinStart = 0;
 	float spinEnd = 4;
 	int spin = 2;
 	
 	// Spin Speed 
 	TextView loadSizeText;
 	float loadSizeStart = 0;
 	float loadSizeEnd = 4;
 	int loadSize = 2;
 		
 	// Soil Level 
 	TextView soilLevelText;
 	float soilLevelStart = 0;
 	float soilLevelEnd = 4;
 	int soilLevel = 2;
 		
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.custom);
		
         //unpack intent
         Intent intent = getIntent();
         Bundle bundle = intent.getExtras();
 		
         program = Program.getDefaultProgram();
         
         if(bundle != null) {
 			if(bundle.containsKey("selection")){
 				selection = bundle.getString("selection");
 				datasource = new ProgramDataSource(this);
 				datasource.open();
 				program = datasource.nameToProgram(selection);
 				datasource.close();
 			}
 			
 			if(bundle.containsKey("soil_level")) {
 				program.setSoilLevel(bundle.getInt("soil_level"));
 			}
 			
 			if(bundle.containsKey("load_size") ){
 				program.setLoadSize(bundle.getInt("load_size"));	
 			}
 			
 			if(bundle.containsKey("steam")) {
 				program.setSteam(bundle.getBoolean("steam"));
 			}
         }
 		
		
		
        final ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
         
         rootIntent.setHelpText("Customize Wash Program", getText(R.string.customize_help));
      
         createLayout();
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.actionbar, menu);
 		return true;
 	}
 
 	private void createLayout() {
 		// Add components to left pane	
 		
 		// Add presoak switch
 		presoakSwitch = (Switch) findViewById(R.id.presoakSwitch);
 		presoakSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView,
 					boolean isChecked) {
 				program.getWashCycle().setPresoak(isChecked);
 			}
 		});
 		presoakSwitch.setChecked(program.getWashCycle().getPresoak());
 		
 		
 		// Add steam switch
 		steamSwitch = (Switch) findViewById(R.id.steamSwitch);
 		steamSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView,
 					boolean isChecked) {
 				program.setSteam(isChecked);
 			}
 		});
 		steamSwitch.setChecked(program.getSteam());
 		
 		// Create Wash Temperature Toggle Buttons
 		LinearLayout washTempPanel = (LinearLayout) findViewById(R.id.washTempGrp);
         Temperature[] water_temps = Temperature.values();
         CustomRadioGroup washTempGrp = new CustomRadioGroup();
         OnClickListener washTempListener = new OnClickListener() {
             public void onClick(View v) {
                 selectWashTemperature(v);
             }
         };
         
         Temperature washTemp = program.getWashCycle().getTemp();
         ToggleButton temperature_rb[] = new ToggleButton[water_temps.length];
         for(int i = 0; i < water_temps.length; i++){
         	temperature_rb[i] = new ToggleButton(this);
         	temperature_rb[i].setText(water_temps[i].getLabel());
 	     	temperature_rb[i].setTextOn(water_temps[i].getLabel());
 	     	temperature_rb[i].setTextOff(water_temps[i].getLabel());
         	temperature_rb[i].setOnClickListener(washTempListener);
         	washTempGrp.addRadioButton(temperature_rb[i]);
         	washTempPanel.addView(temperature_rb[i]);
         	
         	if(washTemp.getLabel().equals(temperature_rb[i].getText())) {
         		temperature_rb[i].setChecked(true);
         	}
         }
         
         // Create Rinse Temperature Toggle Buttons
 		 LinearLayout rinseTempPanel = (LinearLayout) findViewById(R.id.rinseTempGrp);
 	     CustomRadioGroup temperature_grp = new CustomRadioGroup();
 	     OnClickListener rinseTempListener = new OnClickListener() {
 	         public void onClick(View v) {
 	             selectRinseTemperature(v);
 	         }
 	     };
 	     
 	     Temperature rinseTemp = program.getRinseCycle().getTemp();
 	     temperature_rb = new ToggleButton[water_temps.length];
 	     for(int i = 0; i < water_temps.length; i++){
 	     	temperature_rb[i] = new ToggleButton(this);
 	     	temperature_rb[i].setText(water_temps[i].getLabel());
 	     	temperature_rb[i].setTextOn(water_temps[i].getLabel());
 	     	temperature_rb[i].setTextOff(water_temps[i].getLabel());
 	     	temperature_rb[i].setOnClickListener(rinseTempListener);
 	     	temperature_grp.addRadioButton(temperature_rb[i]);
 	     	rinseTempPanel.addView(temperature_rb[i]);
 	     	
 	     	if(rinseTemp.getLabel().equals(temperature_rb[i].getText())) {
         		temperature_rb[i].setChecked(true);
         	}
 	     }
 	    
 	    
 	     //Add Wash Agitation Seek bar
 		agLevelText = (TextView) findViewById(R.id.agitationText);
 		
 		SeekBar agLevelSeek = (SeekBar) findViewById(R.id.agLevelSeek);
 		agLevel = program.getAgitation().getID();
 		agLevelSeek.setProgress((int)(100/(agLevelEnd-agLevelStart)*agLevel));
 		agLevelText.setText("Wash Agitation Level: "+program.getAgitation().getLabel());
 		
 		
 		agLevelSeek.setOnSeekBarChangeListener( new OnSeekBarChangeListener() {
 		
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				float temp = progress;
 				float dis = agLevelEnd-agLevelStart;
 				agLevel = (int) FloatMath.ceil((agLevelStart + ((temp/100)*dis)));
 				
 				Level agitationLevel = Cycle.Level.values()[agLevel];
 				String level = (String) agitationLevel.getLabel();
 				agLevelText.setText("Wash Agitation Level: " + level);
 				program.setAgitation(agitationLevel);
 				
 			}
 		
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {	}
 		
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {}
 		});
 		
 		
 		// Add Spin Speed seek bar
 		spinText = (TextView) findViewById(R.id.spinSpeedText);
 		
 		SeekBar spinSeek = (SeekBar) findViewById(R.id.spinSpeedSeek);
 		spin = program.getSpinCycle().getSpinSpeed().getID();
 		spinSeek.setProgress((int)(100/(spinEnd-spinStart)*spin));
 		spinText.setText("Spin Speed: "+program.getSpinCycle().getSpinSpeed().getLabel());
 		spinSeek.setOnSeekBarChangeListener( new OnSeekBarChangeListener() {
 		
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				float temp = progress;
 				float dis = spinEnd-spinStart;
 				spin = (int) FloatMath.ceil((spinStart + ((temp/100)*dis)));
 				Level spinSpeed = Cycle.Level.values()[spin];
 				String level = (String) spinSpeed.getLabel();
 				spinText.setText("Spin Speed: " + level);
 				program.getSpinCycle().setSpinSpeed(spinSpeed);
 			}
 		
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {	}
 		
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {}
 		});
 	    
 		// Add load size seek bar
 	    loadSizeText = (TextView) findViewById(R.id.loadSizeText);
         
         SeekBar loadSizeSeek = (SeekBar) findViewById(R.id.loadSizeSeek);
         loadSize = program.getLoadSize().getID();
 		loadSizeSeek.setProgress((int)(100/(spinEnd-spinStart)*loadSize));
 		loadSizeText.setText("Load Size: "+program.getLoadSize().getLabel());
         loadSizeSeek.setOnSeekBarChangeListener( new OnSeekBarChangeListener() {
 
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				float temp = progress;
 				float dis = loadSizeEnd-loadSizeStart;
 				loadSize = (int) FloatMath.ceil((loadSizeStart + ((temp/100)*dis)));
 				
 				Size load_size = Cycle.Size.values()[loadSize];
 				program.setLoadSize(load_size);
 				
 				String level = (String) load_size.getLabel();
 				loadSizeText.setText("Load Size: " + level);
 				
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {	}
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {}
         });
         
 		// Add soil level seek bar
 	    soilLevelText = (TextView) findViewById(R.id.soilLevelText);
         
         SeekBar soilLevelSeek = (SeekBar) findViewById(R.id.soilLevelSeek);
         soilLevel = program.getSoilLevel().getID();
 		soilLevelSeek.setProgress((int)(100/(soilLevelEnd-soilLevelStart)*soilLevel));
 		soilLevelText.setText("Soil Level: "+program.getSoilLevel().getLabel());
 		soilLevelSeek.setOnSeekBarChangeListener( new OnSeekBarChangeListener() {
 
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				float temp = progress;
 				float dis = soilLevelEnd-soilLevelStart;
 				soilLevel = (int) FloatMath.ceil((soilLevelStart + ((temp/100)*dis)));
 				
 				Level soil_level = Cycle.Level.values()[soilLevel];
 				program.setSoilLevel(soil_level);
 				
 				String level = (String) soil_level.getLabel();
 				soilLevelText.setText("Soil Level: " + level);
 				
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {	}
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {}
         });
 	        
 	        
 	}
 	
 	private void selectWashTemperature(View v){
 		ToggleButton button = (ToggleButton) v;
 	    String text = (String) button.getText();
 	    
 	    Temperature temperatures[] = Temperature.values();
 	    for(int i = 0; i < temperatures.length; i++){
 	    	if(text.compareTo(temperatures[i].getLabel()) == 0){
 	    		Temperature temperature = temperatures[i];
 	    		program.getWashCycle().setTemp(temperature);
 	    	}
 	    }
 	    
 	}
 	
 	private void selectRinseTemperature(View v){
 		ToggleButton button = (ToggleButton) v;
 	    String text = (String) button.getText();
 	    
 	    Temperature temperatures[] = Temperature.values();
 	    for(int i = 0; i < temperatures.length; i++){
 	    	if(text.compareTo(temperatures[i].getLabel()) == 0){
 	    		Temperature temperature = temperatures[i];
 	    		program.getRinseCycle().setTemp(temperature);
 	    	}
 	    }
 	    
 	}
 	
 	public void startWash(View v) {
 
 		Intent intent = new Intent(this, ProgressActivity.class);
 		intent.putExtra("program", program);
 
 		startActivity(intent);
 	}
 	
 	
 }
