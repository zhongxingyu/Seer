 package com.anonymous.solar.android;
 
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Adapter;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.FrameLayout;
 import android.widget.Spinner;
 
 import com.anonymous.solar.shared.SolarInverter;
 import com.anonymous.solar.shared.SolarSetup;
 
 /**
  * The Customer Usage Wizard Pane.
  * 
  * @author 07627505 Darran Kartaschew
  * @version 1.0
  */
 public class WizardElectrical extends WizardViews {
 
 	// Our default layout.
 	private static int layout = R.layout.wizard_inverter;
 
 	// Reference to the parent view.
 	private MainActivity parent;
 
 	// Layout widgets.
 	private EditText inverterDetails;
 	private EditText wiringLength;
 	private EditText wiringEfficiency;
 	private Button editInverterButton;
 	private OnClickListener editBttonListener;
 	private Spinner definedInverters;
 
 	private List<SolarInverter> inverters;
 	private SolarInverter localInverter;
 
 	/**
 	 * Default Constructor
 	 * 
 	 * @param parent
 	 *            The parent Activity which is hosting the wizard view controls.
 	 */
 	public WizardElectrical(MainActivity parent) {
 		this.parent = parent;
 
 		// Set the layout.
 		setView(parent, layout);
 
 		// Set the layout widgets.
 		inverterDetails = (EditText) parent.findViewById(R.id.editTextInverterDetails);
 		wiringLength = (EditText) parent.findViewById(R.id.editTextWiringLength);
 		wiringEfficiency = (EditText) parent.findViewById(R.id.editTextWiringEff);
 		definedInverters = (Spinner) parent.findViewById(R.id.spinnerInverters);
 		editInverterButton = (Button) parent.findViewById(R.id.buttonInverterEdit);
 
 		// Setup Edit Button.
 		setupEditButton();
 
 		// Setup predefined locations
 		setupSpinner();
 	}
 
 	/**
 	 * Create all button actions for wizard navigation
 	 */
 	private void setupEditButton() {
 
 		// Close button.
 		editBttonListener = new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				buttonEditEvent();
 			}
 		};
 		editInverterButton.setOnClickListener(editBttonListener);
 	}
 
 	/**
 	 * Setup the edit button for the inverter information.
 	 */
 	private void buttonEditEvent() {
 
 		AlertDialog.Builder alert = new AlertDialog.Builder(parent);
 
 		alert.setTitle("Edit Inverter Information");
 		// alert.setMessage("Modify Inverter Information");
 
 		LayoutInflater inflater = parent.getLayoutInflater();
 		FrameLayout f1 = (FrameLayout) parent.findViewById(android.R.id.custom);
 		// f1.addView(inflater.inflate(R.layout.inverter_edit, f1, false));
 		View view = inflater.inflate(R.layout.inverter_edit, f1, false);
 		alert.setView(view);
 
 		// Get our dialog elements
 		final EditText cost = (EditText) view.findViewById(R.id.editTextInverterEditCost);
 		final EditText rrp = (EditText) view.findViewById(R.id.editTextInverterEditRRP);
 		final EditText wattage = (EditText) view.findViewById(R.id.editTextInverterEditWattage);
 		final EditText life = (EditText) view.findViewById(R.id.editTextInverterEditLife);
 		final EditText eff = (EditText) view.findViewById(R.id.editTextInverterEditEfficiency);
 		final EditText effLoss = (EditText) view.findViewById(R.id.editTextInverterEditEfficicencyLoss);
 
 		// Set our information.
 		cost.setText(localInverter.getInverterCost().toString());
 		rrp.setText(localInverter.getInverterRRP().toString());
 		wattage.setText(localInverter.getInverterWattage().toString());
 		life.setText(localInverter.getInverterLifeYears().toString());
 		eff.setText(localInverter.getInverterEfficiency().toString());
 		effLoss.setText(localInverter.getInverterLossYear().toString());
 
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				// record the values
				try{
 					localInverter.setInverterCost(Double.parseDouble(cost.getText().toString()));
 					localInverter.setInverterRRP(Double.parseDouble(rrp.getText().toString()));
 					localInverter.setInverterWattage(Double.parseDouble(wattage.getText().toString()));
 					localInverter.setInverterEfficiency(Double.parseDouble(eff.getText().toString()));
 					localInverter.setInverterLossYear(Double.parseDouble(effLoss.getText().toString()));
 					localInverter.setInverterLifeYears(Integer.parseInt(life.getText().toString()));
				} catch (Exception e){
 					// kill the exception.
 				}
 				inverterDetails.setText(localInverter.toDetailsString());
 			}
 		});
 
 		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				// Canceled.
 			}
 		});
 
 		alert.show();
 	}
 
 	/**
 	 * Setup the spinner used for the predefined locations.
 	 */
 	private void setupSpinner() {
 		// Clear the defined locations, and reload from service.
 		definedInverters.setSelection(Adapter.NO_SELECTION);
 
 		// Launch background thread to get our data.
 		new WizardElectricalSetupInverters(this.parent, this).execute();
 
 		// Add our handler for selecting items.
 		OnItemSelectedListener spinnerListener = new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 
 				SolarInverter inverter = (SolarInverter) arg0.getItemAtPosition(arg2);
 				// Populate the text fields.
 				inverterDetails.setText(inverter.toDetailsString());
 				localInverter = inverter;
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 				// TODO Auto-generated method stub
 
 			}
 		};
 		definedInverters.setOnItemSelectedListener(spinnerListener);
 	}
 
 	@Override
 	public boolean callbackStart() {
 		SolarSetup global = parent.getSolarSetup();
 		localInverter = global.getInverter();
 		if (localInverter != null && inverters != null) {
			definedInverters.setSelection(inverters.indexOf(localInverter));
 		}
 		Double wireLength = global.getWireLength();
 		if (wireLength != null) {
 			wiringLength.setText(wireLength.toString());
 		} else {
 			// Add some default data.
 			wiringLength.setText("10");
 		}
 		Double wireEficiency = global.getWireEfficiency();
 		if (wireEficiency != null) {
 			wiringEfficiency.setText(wireEficiency.toString());
 		} else {
 			// Add some default data.
 			wiringEfficiency.setText("99");
 		}
 		return true;
 	}
 
 	@Override
 	public boolean callbackDispose(boolean validateInput) {
 		SolarSetup global = parent.getSolarSetup();
 		double wireLength;
 		double wireEff;
 		// Get our values.
 		try {
 			wireLength = Double.parseDouble(wiringLength.getText().toString());
 		} catch (Exception e) {
 			wireLength = 0.0;
 			if (validateInput) {
 				new SolarAlertDialog().displayAlert(parent, "Missing Input", "Please enter a wire length");
 				return false;
 			}
 		}
 		try {
 			wireEff = Double.parseDouble(wiringEfficiency.getText().toString());
 			if ((wireEff < 0 || wireEff > 100.00) && validateInput) {
				new SolarAlertDialog().displayAlert(parent, "Missing Input", "Wire Efficiency must be between 0.00% and 100.00%");
 				return false;
 			}
 		} catch (Exception e) {
 			wireEff = 0.00;
 			if (validateInput) {
				new SolarAlertDialog().displayAlert(parent, "Missing Input", "Please enter a wire efficiency between 0.00% and 100%");
 				return false;
 			}
 		}
 
 		try {
 			global.setInverter(localInverter);
 			global.setWireLength(wireLength);
 			global.setWireEfficiency(wireEff);
 		} catch (Exception e) {
 			if (validateInput) {
 				new SolarAlertDialog().displayAlert(parent, "Missing Input",
 						"Invalid parameters entered, please ensure values entered are correct");
 
 				e.printStackTrace();
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Set the local copy of all inverters as stored by GAE.
 	 * 
 	 * @param inverters
 	 */
 	public synchronized void setInverters(List<SolarInverter> inverters) {
 		this.inverters = inverters;
 	}
 
 }
