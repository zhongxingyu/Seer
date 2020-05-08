 package jgn09.homework2;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Homework2Activity extends Activity {
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.table);
 	}
 
 	public void clearAll(View v) {
 		Map<Integer, Integer> TvEtDict = new HashMap<Integer, Integer>();
 		TvEtDict.put(R.id.fname, R.id.tv_fname);
 		TvEtDict.put(R.id.lname, R.id.tv_lname);
 		TvEtDict.put(R.id.phone, R.id.tv_phone);
 		TvEtDict.put(R.id.email, R.id.tv_email);
 		TvEtDict.put(R.id.email2, R.id.tv_email2);
 		TvEtDict.put(R.id.username, R.id.tv_username);
 		TvEtDict.put(R.id.password, R.id.tv_password);
 		TvEtDict.put(R.id.password2, R.id.tv_password2);
 
 		EditText field;
 		TextView label;
 		for (Map.Entry<Integer, Integer> entry : TvEtDict.entrySet()) {
 			field = (EditText) findViewById(entry.getKey());
 			field.setText("");
 			label = (TextView) findViewById(entry.getValue());
 			label.setTextColor(Color.LTGRAY);
 		}
 
 		RadioGroup gender = (RadioGroup) findViewById(R.id.genderGroup);
 		gender.clearCheck();
 		label = (TextView) findViewById(R.id.tv_gender);
 		label.setTextColor(Color.LTGRAY);
 	}
 
 	public void submit(View v) {
 		if (checkForm()) {
 			Toast.makeText(this, "Informaton saved to the database successfully.", Toast.LENGTH_LONG).show();
 		} else {
 			Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
 		}
 	}
 
 	public Boolean checkEmptyFields()
 	{
 		Boolean isValid = true;
 		Map<Integer, Integer> TvEtDict = new HashMap<Integer, Integer>();
 		TvEtDict.put(R.id.fname, R.id.tv_fname);
 		TvEtDict.put(R.id.lname, R.id.tv_lname);
 		TvEtDict.put(R.id.phone, R.id.tv_phone);
 		TvEtDict.put(R.id.email, R.id.tv_email);
 		TvEtDict.put(R.id.email2, R.id.tv_email2);
 		TvEtDict.put(R.id.username, R.id.tv_username);
 		TvEtDict.put(R.id.password, R.id.tv_password);
 		TvEtDict.put(R.id.password2, R.id.tv_password2);
 
 		EditText field;
 		TextView label;
 		for (Map.Entry<Integer, Integer> entry : TvEtDict.entrySet()) {
 			field = (EditText) findViewById(entry.getKey());
 			label = (TextView) findViewById(entry.getValue());
 			if (field.getText().toString().length() == 0) {
 				isValid = false;
 				label.setTextColor(Color.RED);
 			} else {
 				label.setTextColor(Color.LTGRAY);
 			}
 		}
 
 		RadioGroup gender = (RadioGroup) findViewById(R.id.genderGroup);
 		label = (TextView) findViewById(R.id.tv_gender);
 		if (gender.getCheckedRadioButtonId() == -1) {
 			label.setTextColor(Color.RED);
 		} else {
 			label.setTextColor(Color.LTGRAY);
 		}
 
 		return isValid;
 	}
 	
	public Boolean checkMatchGeneric(int etId1, int etId2, int tvId1, int tvId2) {
 		EditText et1 = (EditText) findViewById(etId1),
 				 et2 = (EditText) findViewById(etId2);
 		TextView tv1 = (TextView) findViewById(tvId1),
 				 tv2 = (TextView) findViewById(tvId2);
 
 		if (et1.getText().toString().equals(et2.getText().toString())) {
 			tv1.setTextColor(Color.LTGRAY);
 			tv2.setTextColor(Color.LTGRAY);
 			return true;
 		} else {
 			tv1.setTextColor(Color.RED);
 			tv2.setTextColor(Color.RED);
 			return false;
 		}
 	}
 
 	public Boolean checkForm() {				
		Boolean goodEmail,
				goodPassword;
		goodEmail = checkMatchGeneric(R.id.email, R.id.email2, R.id.tv_email, R.id.tv_email2);
		goodPassword = checkMatchGeneric(R.id.password, R.id.password2, R.id.tv_password, R.id.tv_password2);
		
		return goodEmail && goodPassword;
 	}
 }
