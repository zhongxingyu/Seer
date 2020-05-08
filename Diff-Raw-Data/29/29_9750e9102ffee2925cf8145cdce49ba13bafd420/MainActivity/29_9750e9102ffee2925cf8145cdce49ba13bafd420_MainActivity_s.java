 package info.ohgita.bincalc_android;
 
 import com.actionbarsherlock.R;
 import com.actionbarsherlock.app.SherlockActivity;
 
 import android.os.Bundle;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 public class MainActivity extends SherlockActivity {
	
 	static int ID_BASETYPE_BIN =	100;
 	static int ID_BASETYPE_DEC =	200;
 	static int ID_BASETYPE_HEX =	300;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		/* Event handler for Basetype toggles */
 		final ToggleButton tb_bin = (ToggleButton) findViewById(R.id.toggle_basetype_bin);
 		final ToggleButton tb_dec = (ToggleButton) findViewById(R.id.toggle_basetype_dec);
 		final ToggleButton tb_hex = (ToggleButton) findViewById(R.id.toggle_basetype_hex);
 		tb_bin.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				switchBasetype(ID_BASETYPE_BIN);
 			}
 		});
 		tb_dec.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				switchBasetype(ID_BASETYPE_DEC);
 			}
 		});
 		tb_hex.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				switchBasetype(ID_BASETYPE_HEX);
 			}
 		});

 	}
 	
 	/**
 	 * switch base-type
 	 * @param basetypeId	Base-type ID number
 	 */
 	public void switchBasetype(int basetypeId){
		Toast.makeText(MainActivity.this, "ID:"+basetypeId, Toast.LENGTH_SHORT).show();
 		ToggleButton tb_bin = (ToggleButton) findViewById(R.id.toggle_basetype_bin);
 		ToggleButton tb_dec = (ToggleButton) findViewById(R.id.toggle_basetype_dec);
 		ToggleButton tb_hex = (ToggleButton) findViewById(R.id.toggle_basetype_hex);
 		tb_bin.setChecked(false);
 		tb_dec.setChecked(false);
 		tb_hex.setChecked(false);
 		if(basetypeId == ID_BASETYPE_BIN){
 			tb_bin.setChecked(true);
 		}else if(basetypeId == ID_BASETYPE_DEC){
 			tb_dec.setChecked(true);
 		}else{
 			tb_hex.setChecked(true);
 		}
 	}
 }
