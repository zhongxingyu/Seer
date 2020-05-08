 package me.davidgreene.minerstatus;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.RadioButton;
 import android.widget.ScrollView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 public class OptionsActivity extends AbstractMinerStatusActivity{
 		
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.options);
         int bgColor = themeService.getTheme().getBackgroundColor();
         int color = themeService.getTheme().getTextColor();
         
         ScrollView scrollView = (ScrollView) findViewById(R.id.optionsScrollView);
         scrollView.setBackgroundColor(bgColor);
         
         TextView mtGoxToggleLabel = (TextView) findViewById(R.id.mtGoxButtonLabel);
         mtGoxToggleLabel.setTextColor(color);
         TextView themeSpinnerLabel = (TextView) findViewById(R.id.themeSpinnerLabel);
         themeSpinnerLabel.setTextColor(color);
         final ToggleButton mtGoxToggle = (ToggleButton) findViewById(R.id.mtGoxToggle);
         mtGoxToggle.setChecked(Boolean.valueOf(configService.getConfigValue("show.mtgox")));
         mtGoxToggle.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 		        if (mtGoxToggle.isChecked()) {
 		        	configService.setConfigValue("show.mtgox", "true");
 		        } else {
 		        	configService.setConfigValue("show.mtgox", "false");
 		        }
 		        Toast.makeText(OptionsActivity.this, (mtGoxToggle.isChecked()) ? "Mt. Gox Visible" : "Mt. Gox Hidden", Toast.LENGTH_SHORT).show();
 			}
 		});
         
     	final RadioButton darkTheme = (RadioButton) findViewById(R.id.radio_dark);
     	final RadioButton lightTheme = (RadioButton) findViewById(R.id.radio_light);
 
     	String themeString = configService.getConfigValue("theme");
     	
     	if(themeString.equals("light")){
     		lightTheme.setChecked(Boolean.TRUE);
     	} else if (themeString.equals("dark")){
     		darkTheme.setChecked(Boolean.TRUE);
     	}
     	
     	darkTheme.setOnClickListener(radioListener);
     	darkTheme.setTextColor(color);
     	
     	lightTheme.setOnClickListener(radioListener);
     	lightTheme.setTextColor(color);
     	
    	TextView minerDeleteSpinnerLabel = (TextView) findViewById(R.id.deleteSpinnerLabel);
    	minerDeleteSpinnerLabel.setTextColor(color);
    	
     	final Spinner spinner = (Spinner) findViewById(R.id.miner_delete_spinner);
     	populateSpinner(spinner);
         Button deleteMinerButton = (Button) findViewById(R.id.deleteMinerButtonOptionsMenu);
         deleteMinerButton.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					if (spinner.getSelectedItem() == null){
 						Toast.makeText(getApplicationContext(), "You cannot delete nothing!?  Or can you?",
 								Toast.LENGTH_LONG).show();
 						return;
 					}
 					AlertDialog.Builder alert = new AlertDialog.Builder(OptionsActivity.this);
 					alert.setTitle("Remove " + (CharSequence)spinner.getSelectedItem() + "?");
 					alert.setPositiveButton("Remove", new DialogInterface.OnClickListener() {	
 						public void onClick(DialogInterface dialog, int whichButton) {
 							Toast.makeText(getApplicationContext(), (CharSequence)spinner.getSelectedItem() +" removed.",
 									Toast.LENGTH_LONG).show();
 							minerService.deleteMiner(((CharSequence)spinner.getSelectedItem()).toString());
 							populateSpinner(spinner);
 						}
 					});		
 					alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int whichButton) {
 							dialog.cancel();
 						}
 					});				
 					alert.show();  
 				}
 			});
         
 	}	
 	
 	private void populateSpinner(Spinner spinner){
     	Cursor cur = minerService.getMiners();
     	ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item);
     	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		while(cur.moveToNext()){
 			String miner = cur.getString(0);     
 			CharSequence seq = miner;
 			adapter.add(miner);
 		}
 		spinner.setAdapter(adapter);
 		cur.close();
 	}
 	
 	private OnClickListener radioListener = new OnClickListener() {
 	    public void onClick(View v) {
 	        RadioButton rb = (RadioButton) v;
 	        if(rb.getText().equals("Dark Theme")){
 	        	configService.setConfigValue("theme", "dark");
 	        } else if (rb.getText().equals("Light Theme")){
 	        	configService.setConfigValue("theme", "light");
 	        }
 	    }
 	};
 	
 }
