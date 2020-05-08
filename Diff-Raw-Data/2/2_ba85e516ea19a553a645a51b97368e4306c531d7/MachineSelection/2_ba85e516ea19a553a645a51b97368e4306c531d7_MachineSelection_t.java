 package ufit.profilecreation;
 
 import ufit.global.MyApp;
 import ufit.namespace.R;
 import ufit.profile.Profile;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.CheckBox;
 
 public class MachineSelection extends Activity implements OnClickListener {
 	private Profile profile;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.machineselection);
 		profile = ( (MyApp)getApplication()).getProfile();
 		
 		loadInformation(); //this loads the stored information from profile into the display
 		setOnClickListenerForViews(); //this sets up listeners for the necessary buttons.
 		
 		
 	}
 
 	private void setOnClickListenerForViews() {
 		View v = findViewById(R.id.machineselect_button_finish);
 		v.setOnClickListener(this);
 		/*CheckBox box; //we don't need to listen to these buttons.
 		box = (CheckBox) findViewById(R.id.machineselect_checkbox_dumbbells);
 		box.setOnClickListener(this);
 		box = (CheckBox) findViewById(R.id.machineselect_checkbox_barbells);
 		box.setOnClickListener(this);
 		box = (CheckBox) findViewById(R.id.machineselect_checkbox_benchpress);
 		box.setOnClickListener(this);
 		box = (CheckBox) findViewById(R.id.machineselect_checkbox_chestfly);
 		box.setOnClickListener(this);
 		box = (CheckBox) findViewById(R.id.machineselect_checkbox_shoulderpress);
 		box.setOnClickListener(this);
 		box = (CheckBox) findViewById(R.id.machineselect_checkbox_legpress);
 		box.setOnClickListener(this);
 		box = (CheckBox) findViewById(R.id.machineselect_checkbox_hamstringcurl);
 		box.setOnClickListener(this);
 		box = (CheckBox) findViewById(R.id.machineselect_checkbox_pulley);
 		box.setOnClickListener(this);*/
 	}
 
 	private void loadInformation() {
 		CheckBox box;
 		//this sucks, since I need to assume things are spelled in the same way, but whatever
 		box = (CheckBox) findViewById(R.id.machineselect_checkbox_dumbbells);
 		box.setChecked( profile.getEquipment( getString(R.string.dumbbells) ));
 		
 		box = (CheckBox) findViewById(R.id.machineselect_checkbox_barbells);
 		box.setChecked( profile.getEquipment( getString(R.string.barbells) ));
 		
 		box = (CheckBox) findViewById(R.id.machineselect_checkbox_benchpress);
 		box.setChecked( profile.getEquipment( getString(R.string.benchpress) ));
 		
 		box = (CheckBox) findViewById(R.id.machineselect_checkbox_chestfly);
 		box.setChecked( profile.getEquipment( getString(R.string.chestfly) ));
 		
 		box = (CheckBox) findViewById(R.id.machineselect_checkbox_shoulderpress);
 		box.setChecked( profile.getEquipment( getString(R.string.shoulderpress) ));
 		
 		box = (CheckBox) findViewById(R.id.machineselect_checkbox_legpress);
 		box.setChecked( profile.getEquipment( getString(R.string.legpress) ));
 		
 		box = (CheckBox) findViewById(R.id.machineselect_checkbox_hamstringcurl);
 		box.setChecked( profile.getEquipment( getString(R.string.hamstringcurl) ));
 		
 		box = (CheckBox) findViewById(R.id.machineselect_checkbox_pulley);
 		box.setChecked( profile.getEquipment( getString(R.string.pulley) ));
 		
 	}
 
 	public void onClick(View arg0) {
		if(arg0.getId() == R.id.machineselect_button_finish){
 			Intent intent = new Intent(this,Selection.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP).setAction("Go to Home");
 			this.startActivity(intent);
 		}
 		
 	}
 
 	private void saveCheckedInformation() {
 		CheckBox box; 
 		box = (CheckBox) findViewById(R.id.machineselect_checkbox_dumbbells);
 		profile.setEquipment( getString(R.string.dumbbells) , box.isChecked());
 		
 		box = (CheckBox) findViewById(R.id.machineselect_checkbox_barbells);
 		profile.setEquipment( getString(R.string.barbells) , box.isChecked());
 		
 		box = (CheckBox) findViewById(R.id.machineselect_checkbox_benchpress);
 		profile.setEquipment( getString(R.string.benchpress) , box.isChecked());
 		
 		box = (CheckBox) findViewById(R.id.machineselect_checkbox_chestfly);
 		profile.setEquipment( getString(R.string.chestfly) , box.isChecked());
 		
 		box = (CheckBox) findViewById(R.id.machineselect_checkbox_shoulderpress);
 		profile.setEquipment( getString(R.string.shoulderpress) , box.isChecked());
 		
 		box = (CheckBox) findViewById(R.id.machineselect_checkbox_legpress);
 		profile.setEquipment( getString(R.string.legpress) , box.isChecked());
 		
 		box = (CheckBox) findViewById(R.id.machineselect_checkbox_hamstringcurl);
 		profile.setEquipment( getString(R.string.hamstringcurl) , box.isChecked());
 		
 		box = (CheckBox) findViewById(R.id.machineselect_checkbox_pulley);
 		profile.setEquipment( getString(R.string.pulley) , box.isChecked());
 		
 	}
 	
 	protected void onPause() {
 		super.onPause();
 		saveCheckedInformation();
 	}
 
 }
