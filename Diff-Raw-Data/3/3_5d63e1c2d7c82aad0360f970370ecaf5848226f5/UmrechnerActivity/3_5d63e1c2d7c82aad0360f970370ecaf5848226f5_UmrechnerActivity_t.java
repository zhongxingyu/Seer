 package de.gstools.test;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.TextView;
 
 public class UmrechnerActivity extends Activity {
     /** Called when the activity is first created. */
     @Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 	}
 
 	public void umRechnen(View view){
 		TextView text = (TextView) findViewById(R.id.textView1);
 		RadioButton zoll = (RadioButton) findViewById(R.id.radio1);
 		EditText wert = (EditText) findViewById(R.id.editText1);
 		String newString = wert.getText().toString();
 		double d = Double.valueOf(newString.trim()).doubleValue();
 		if (zoll.isChecked()){
 			double zoll_conv = umrechnenZentimeterinZoll(d);
 			text.setText(Double.toString(zoll_conv));
 		} else {
 			double cm_conv = umrechnenZollinZentimeter(d);
 			text.setText(Double.toString(cm_conv));
 		}
 	}
	
 	public void Loeschen(View view){
 		TextView text = (TextView) findViewById(R.id.textView1);
 		text.setText("");
 		//add a comment
 		EditText wert = (EditText) findViewById(R.id.editText1);
 		wert.setText("");
 	}

 	public double umrechnenZentimeterinZoll(double zentimeter){
 		return (zentimeter *0.3937);
 	}
 	
 	public double umrechnenZollinZentimeter(double zoll){
 		return (zoll * 2.54);
 	}
 }
