 package com.dhbw.dvst.activities;
 
 import android.app.Activity;
 import android.app.AlertDialog;
import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 
 import com.dhbw.dvst.R;
 import com.dhbw.dvst.model.Control;
 import com.dhbw.dvst.model.Spiel;
 import com.dhbw.dvst.model.Spielfigur;
 
 public class SpielerActivity extends Activity {
 	private Spiel spiel = Control.getInstance();
 	private EditText et_name;
 	private Spinner spin_farbe;
 	private Spinner spin_form;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.spieler);
 		
 		et_name = (EditText) findViewById(R.id.et_name);
 		spin_farbe = (Spinner) findViewById(R.id.spin_farbe);
 		spin_form = (Spinner) findViewById(R.id.spin_figur);
 		ArrayAdapter<CharSequence> adapt_farbe = ArrayAdapter.createFromResource(this, R.array.farben, android.R.layout.simple_spinner_item);
 		spin_farbe.setAdapter(adapt_farbe);
 		ArrayAdapter<CharSequence> adapt_figur = ArrayAdapter.createFromResource(this, R.array.figuren, android.R.layout.simple_spinner_item);
 		spin_form.setAdapter(adapt_figur);
 		final Button btn_neu = (Button) findViewById(R.id.btn_erstellen);
         btn_neu.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {            	
             	if(SpielerActivity.this.et_name.getText()==null ||
             			SpielerActivity.this.et_name.getText().toString().equals("")){
             		SpielerActivity.this.et_name.setError(getString(R.string.err_eingabe_leer));
             	}
             	else if(SpielerActivity.this.spin_farbe.getSelectedItem()==null ||
             			SpielerActivity.this.spin_form.getSelectedItem()==null){
            		AlertDialog.Builder builder = new AlertDialog.Builder(SpielerActivity.this);
             		builder.setMessage(getString(R.string.err_nichts_selektiert))
             			.setCancelable(true);
             		AlertDialog alert = builder.create();
             		alert.show();
             	}
             	else{
             		for (Spielfigur figur : spiel.getAlleSpielfiguren()) {            			
 						if(figur.getFarbe().equals(spin_farbe.getSelectedItem().toString()) &
 								figur.getForm().equals(spin_form.getSelectedItem().toString())){
 							if(figur.isVergeben()){
								AlertDialog.Builder builder = new AlertDialog.Builder(SpielerActivity.this);
			            		builder.setMessage(getString(R.string.err_vergeben))
			            			.setNegativeButton("Close", new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											dialog.cancel();
											
										}
									});
 			            		AlertDialog alert = builder.create();
 			            		alert.show();
 							}
 							else{
 								spiel.spielerHinzufuegen(et_name.getText().toString(), figur);
 								Intent intent_uebersicht = new Intent(SpielerActivity.this, SpielerUebersichtActivity.class);
 			            		SpielerActivity.this.startActivity(intent_uebersicht);
 							}
 						}
 					}            		
             	}           	
             }
         });
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 	  // ignore orientation/keyboard change
 	  super.onConfigurationChanged(newConfig);
 	}
 
 }
