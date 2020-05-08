 package com.lossboys.customerapp.dashboard;
 
 import com.lossboys.customerapp.R;
 import android.app.Activity;
 import android.os.Bundle;
 import java.util.ArrayList;
 import java.util.List;
 
import android.app.Activity;
 import android.content.Intent;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 public class CheckoutActivity extends Activity implements OnItemSelectedListener {
 	/** Called when the activity is first created. */
 	EditText inputCardNumber;
 	EditText inputSecurityCode;
 	TextView checkoutErrorMsg;
 	Button btn_next;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.checkout_layout);
 
 		inputCardNumber = (EditText) findViewById(R.id.cardNumber);
 		inputSecurityCode = (EditText) findViewById(R.id.securityCode);
 		checkoutErrorMsg = (TextView) findViewById(R.id.checkout_error);
 
 		// Button for Next
 		btn_next = (Button) findViewById(R.id.btnNext);
 
 		// Spinner element
 		Spinner cardType_spinner = (Spinner) findViewById(R.id.cardtype_spinner);
 		// Spinner element
 		Spinner cardExpMM_spinner = (Spinner) findViewById(R.id.cardexpMM_spinner);
 		// Spinner element
 		Spinner cardExpYYYY_spinner = (Spinner) findViewById(R.id.cardexpYYYY_spinner);
 
 		// Spinner click listener
 		cardType_spinner.setOnItemSelectedListener(this);
 		// Spinner click listener
 		cardExpMM_spinner.setOnItemSelectedListener(this);
 		// Spinner click listener
 		cardExpYYYY_spinner.setOnItemSelectedListener(this);
 
 		// Spinner Drop down elements
 		List<String> cardTypes = new ArrayList<String>();
 		cardTypes.add("Visa");
 		cardTypes.add("Master Card");
 		cardTypes.add("Discover");
 		cardTypes.add("American Express");
 		// Spinner Drop down elements
 		List<String> cardExpMM = new ArrayList<String>();
 		cardExpMM.add("01");
 		cardExpMM.add("02");
 		cardExpMM.add("03");
 		cardExpMM.add("04");
 		cardExpMM.add("05");
 		cardExpMM.add("06");
 		cardExpMM.add("07");
 		cardExpMM.add("08");
 		cardExpMM.add("09");
 		cardExpMM.add("10");
 		cardExpMM.add("11");
 		cardExpMM.add("12");
 		// Spinner Drop down elements
 		List<String> cardExpYYYY = new ArrayList<String>();
 		cardExpYYYY.add("2013");
 		cardExpYYYY.add("2014");
 		cardExpYYYY.add("2015");
 		cardExpYYYY.add("2016");
 		cardExpYYYY.add("2017");
 		cardExpYYYY.add("2018");
 		cardExpYYYY.add("2019");
 		cardExpYYYY.add("2020");
 		cardExpYYYY.add("2021");
 		cardExpYYYY.add("2022");
 		cardExpYYYY.add("2023");
 		cardExpYYYY.add("2024");
 		cardExpYYYY.add("2025");
 		cardExpYYYY.add("2026");
 		cardExpYYYY.add("2027");
 		cardExpYYYY.add("2028");
 		cardExpYYYY.add("2029");
 		cardExpYYYY.add("2030");
 		cardExpYYYY.add("2031");
 		cardExpYYYY.add("2032");
 		cardExpYYYY.add("2033");
 
 		// Creating adapter for spinner
 		ArrayAdapter<String> cardtypeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, cardTypes);
 		// Creating adapter for spinner
 		ArrayAdapter<String> cardExpMMAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, cardExpMM);
 		// Creating adapter for spinner
 		ArrayAdapter<String> cardExpYYYYAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, cardExpYYYY);
 
 		// Drop down layout style - list view with radio button
 		cardtypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		// Drop down layout style - list view with radio button
 		cardExpMMAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		// Drop down layout style - list view with radio button
 		cardExpYYYYAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
 		// attaching data adapter to spinner
 		cardType_spinner.setAdapter(cardtypeAdapter);
 		// attaching data adapter to spinner
 		cardExpMM_spinner.setAdapter(cardExpMMAdapter);
 		// attaching data adapter to spinner
 		cardExpYYYY_spinner.setAdapter(cardExpYYYYAdapter);
 
 		btn_next.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View view) {
 
 				String CardNumber = inputCardNumber.getText().toString();
 				String SecurityCode = inputSecurityCode.getText().toString();
 
 				Intent i = new Intent(getApplicationContext(), CheckoutActivity2.class);
 				startActivity(i);
 			}
 		});
 
 	}
 
 	@Override
 	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
 		// On selecting a spinner item
 		String item = parent.getItemAtPosition(position).toString();
 
 		// Showing selected spinner item
 		Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
 
 	}
 
 	public void onNothingSelected(AdapterView<?> arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
