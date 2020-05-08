 package org.blanco.lacuenta;
 
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import org.blanco.lacuenta.listeners.CalculateClickListener;
 import org.blanco.lacuenta.receivers.DialogResultReceiver;
 import org.blanco.lacuenta.receivers.ResultReceiver;
 import org.blanco.lacuenta.receivers.SpeechResultReceiver;
 import org.blanco.lacuenta.receivers.TextViewResultReceiver;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.method.DigitsKeyListener;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 /***
  * @author Alexandro Blanco <ti3r.bubblenet@gmail.com>
  * Initial Activity of the Application.
  */
 public class MainActivity extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.main);
         initComponents();
     }
     
        
 
 	private void initComponents(){
     	edtTotal = (EditText) findViewById(R.id.main_activity_edt_bill_total);
     	edtTotal.setKeyListener(new DigitsKeyListener(false, true));
     	
     	txtResult = (TextView) findViewById(R.id.main_activity_txt_result);
     	spnTip = (Spinner) findViewById(R.id.main_activity_spn_tip);
     	spnPeople = (Spinner) findViewById(R.id.main_activity_spn_people);
     	btnCalculate = (Button) findViewById(R.id.main_activity_btn_calculate);
     	clickListener = new CalculateClickListener(edtTotal, spnTip, spnPeople, getResultReceiver());
     	btnCalculate.setOnClickListener(clickListener);
     	numPad = (NumPad) findViewById(R.id.main_activity_num_pad);
     	
     	if (numPad != null) //Landscape layout will not have numPad
    		numPad.setText(edtTotal);
     }
       
     /***
      * This method will return the result Receiver that will be used when displaying 
      * the calculus results. It will return an instance of an object that implements the
      * ResultReceiver interface depending on established application settings.
      * @return an Object that implements ResultReceiver Interface.
      */
     private List<ResultReceiver> getResultReceiver(){
     	boolean showResOnDialog = 
     	getSharedPreferences(SettingsActivity.SHARED_PREFS_NAME, MODE_PRIVATE).getBoolean(SettingsActivity.SHOW_RES_DIALOG_SETTING_NAME, false);
     	List<ResultReceiver> result = new ArrayList<ResultReceiver>(2);
     	if (showResOnDialog){
     		//deactivate the Result Label
     		this.txtResult.setVisibility(View.GONE);
     		result.add(new DialogResultReceiver(this));
     	}
     	else{
     		this.txtResult.setVisibility(View.VISIBLE);
     		result.add(new TextViewResultReceiver(this,txtResult));
     	}
     	boolean textToSpeech = getSharedPreferences(SettingsActivity.SHARED_PREFS_NAME, MODE_PRIVATE).getBoolean(SettingsActivity.SAY_RES_OUT_LOUD, false);
     	if (textToSpeech)
     		result.add(new SpeechResultReceiver(this, Locale.getDefault()));
     	
     	return result;
     }
     
     @Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
     	new MenuInflater(this).inflate(R.menu.main_activity_main_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.main_activity_main_menu_exit_item:
 				setResult(0);
 				finish();
 			break;
 		case R.id.main_activity_main_menu_settings_item:
 				startConfiguration();
 			break;
 		default:
 			return super.onMenuItemSelected(featureId, item);
 		}
 		return true;
 	}
 
 	@Override
 	protected void onPause() {
 		boolean savePrefs = 
 			getSharedPreferences("settings",MODE_WORLD_WRITEABLE).getBoolean("save_prefs", false);
 		if (savePrefs)
 			saveControlPreferences();
 		super.onPause();
 	}
 	
 	@Override
 	protected void onStart() {
 		boolean savePrefs = 
 			getSharedPreferences("settings",MODE_WORLD_WRITEABLE).getBoolean("save_prefs", false);
 		if (savePrefs)
 			loadControlPreferences();
 		super.onStart();
 	}
 
 	/***
 	 * Starts the configuration Activity
 	 */
 	private void startConfiguration() {
 		Intent settingsIntent = new Intent(this, SettingsActivity.class);
 		startActivityForResult(settingsIntent, 0);
 	}
 
 	/***
 	 * Save the control values in a preferences file in order to be
 	 * restored on the next application execution.
 	 */
 	private void saveControlPreferences(){
 		getSharedPreferences("control_preferences", MODE_PRIVATE).edit().putString("edtTotal", edtTotal.getText().toString()).commit();
 		getSharedPreferences("control_preferences", MODE_PRIVATE).edit().putInt("spnTip", spnTip.getSelectedItemPosition()).commit();
 		getSharedPreferences("control_preferences", MODE_PRIVATE).edit().putInt("spnPeople", spnPeople.getSelectedItemPosition()).commit();
 	}
 	/***
 	 * Load the control values from a preferences file in order to 
 	 * present the user the same interface that when it left
 	 */
 	private void loadControlPreferences(){
 		edtTotal.setText(getSharedPreferences("control_preferences", MODE_PRIVATE).getString("edtTotal", "0"));
 		spnTip.setSelection(getSharedPreferences("control_preferences", MODE_PRIVATE).getInt("spnTip", 0));
 		spnPeople.setSelection(getSharedPreferences("control_preferences", MODE_PRIVATE).getInt("spnPeople", 0));
 	}
 	
 
 	
 	@Override
 	protected void onDestroy() {
 		//finalize the result receivers, it will free the text to speech service in case it is activated
 		if (clickListener != null)
 			clickListener.Destroy();
 		super.onDestroy();
 	}
 
 
 
 
 	EditText edtTotal = null;
     Button btnCalculate = null;
     Spinner spnTip = null;
     Spinner spnPeople = null;
     TextView txtResult = null;
     NumPad numPad= null;
     CalculateClickListener clickListener = null;
 	
 }
