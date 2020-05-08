 package com.ciandt.cestaclt.activity;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.ciandt.cestaclt.R;
 import com.ciandt.cestaclt.Util;
 
 public class TransporteEditActivity extends Activity {
 
 	public static final String PREFS_NAME = "TransportePrefsFile";
 	
 	private float quilometros;
 	private boolean estacionamento;
 	private boolean fretado;
 	private float total;
 	
 	private TextView totalTV;
 	private EditText quilometrosEditText;
 	private CheckBox fretadoCheck;
 	private CheckBox estacionamentoCheck;
 	private Button button;
 
 	private static final float quilometroValue = 1.1f;
 	private static final float estacionamentoValue = 60f;
 	private static final float fretadoValue = 183f;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.transporte_edit);
         bindComponents();
         loadPreferences();
         loadValuesToComponents();
         calculateAndUpdateTotal();
 	}
 
 	private void bindComponents() {
 		totalTV = (TextView) findViewById(R.id.transporte_total);
 		quilometrosEditText = (EditText) findViewById(R.id.quilometros);
 		fretadoCheck = (CheckBox) findViewById(R.id.fretado_check);
 		estacionamentoCheck = (CheckBox) findViewById(R.id.estacionamento_check);
 		button = (Button) findViewById(R.id.transporte_edit_button);
 		
 		quilometrosEditText.addTextChangedListener(new TextWatcher() {
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 			}
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 			}
 			@Override
 			public void afterTextChanged(Editable s) {
 				calculateAndUpdateTotal();
 			}
 		});
 		
 		fretadoCheck.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				calculateAndUpdateTotal();
 			}
 		});
 
 		estacionamentoCheck.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				calculateAndUpdateTotal();
 			}
 		});
 		
 		button.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				calculateAndUpdateTotal();
 				writePreferences();
 				
 				Bundle bundle = new Bundle();
 				bundle.putFloat(Home.TRANSPORTE_RESULT_VALUE, -total);
 
 				Intent intent = new Intent();
 				intent.putExtras(bundle);
 				setResult(RESULT_OK, intent);
 				finish();
 			}
 		});
 	}
 
 	private void loadPreferences() {
 		SharedPreferences settings = getSharedPreferences(TransporteEditActivity.PREFS_NAME, MODE_PRIVATE);
 		
 		quilometros = settings.getFloat("quilometros", 0f);
 		estacionamento = settings.getBoolean("estacionamento", false);
 		fretado = settings.getBoolean("fretado", false);
 	}
 
 	private void loadValuesToComponents() {
 		quilometrosEditText.setText(Float.toString(quilometros));
 		estacionamentoCheck.setChecked(estacionamento);
 		fretadoCheck.setChecked(fretado);
 	}
 
 	private void calculateAndUpdateTotal() {
 		total = 0;
		estacionamento = estacionamentoCheck.isChecked();
		fretado = fretadoCheck.isChecked();
 		
 		String valueText = quilometrosEditText.getText().toString();
 		if(!valueText.trim().equals("")){
 			quilometros = Float.parseFloat(valueText);
 		}
 		
 		total += (quilometros * 44) * quilometroValue;
		if(estacionamento) total += estacionamentoValue;
		if(fretado) total += fretadoValue;
 		
 		totalTV.setText(Util.formatarMoeda(total));
 	}
 
 	private void writePreferences() {
 		SharedPreferences settings = getSharedPreferences(TransporteEditActivity.PREFS_NAME, MODE_PRIVATE);
 		SharedPreferences.Editor editor = settings.edit();
 	   	 
     	editor.putFloat("quilometros", quilometros);
    	editor.putBoolean("estacionamento", estacionamento);
    	editor.putBoolean("fretado", fretado);
     	
     	editor.commit();
 	}
 }
