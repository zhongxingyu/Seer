 package com.ticot.simuimmo;
 
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.util.Locale;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 import com.ticot.simuimmo.calculs.Temp;
 import com.ticot.simuimmo.model.Inputs;
 import com.ticot.simuimmo.model.acquisition.Acquisition;
 import com.ticot.simuimmo.model.bien.Bien;
 import com.ticot.simuimmo.model.gestion.Gestion;
 
 public class MainActivity extends Activity {
 
 	//Declaration of variables
 	public boolean AcquisitionCollpased = true; 
 	public Bien bien = Temp.test();						//Creation of the class Bien through the temporary class
 	
 	/**Called when the activity is first created*/
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 	}
 
 	//TODO onCreateOptionsMenu to do
 /*	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}*/
 	
 	//To do list in the MainActivity
 	//TODO Take into account the mandatory fields
 	//TODO Create the methods to collapse less important fields
 	//TODO Take into account the empty fields and if necessary assign them an appropriate value (0 for instance)
 	
 	
 	//Calculation button
 	//==============================================================================
 	public void onClick (View v){
 		//Methods to get user's inputs, launch calculation and fill back the result in the UI 
 		
 		//Get the user's input values
 		//Inputs.prixFAI = getDoubleValue(R.id.valuePrixFAI);			//Test to check value before assign it to the variable
 		
 		Inputs.reelNetvendeur = ((CheckBox)findViewById(R.id.ReelNetVendeur)).isChecked();
 		Inputs.reelFraisAgence = ((CheckBox)findViewById(R.id.ReelFraisAgence)).isChecked();
 		Inputs.reelFraisNotaire = ((CheckBox)findViewById(R.id.ReelFraisNotaire)).isChecked();
 		Inputs.reelHonoraireConseil = ((CheckBox)findViewById(R.id.ReelHonoraireConseil)).isChecked();
 		Inputs.reelCapitalEmrpunte = ((CheckBox)findViewById(R.id.ReelCapitalEmprunte)).isChecked();
 		Inputs.reelTauxCredit = ((CheckBox)findViewById(R.id.ReelTauxCredit)).isChecked();
 		Inputs.reelTauxAssurance = ((CheckBox)findViewById(R.id.ReelTauxAssurance)).isChecked();
 		//====================================
 		Inputs.netVendeur = Double.valueOf(
 				((EditText)findViewById(R.id.valueReelNetVendeur)).getText().toString());
 		Inputs.fraisAgence = Double.valueOf(
 				((EditText)findViewById(R.id.valueReelFraisAgence)).getText().toString());
 		Inputs.fraisNotaire = Double.valueOf(
 				((EditText)findViewById(R.id.valueReelFraisNotaire)).getText().toString());
 		Inputs.honoraireConseil = Double.valueOf(
 				((EditText)findViewById(R.id.valueReelHonoraireConseil)).getText().toString());
 		Inputs.capitalEmprunte = Double.valueOf(
 				((EditText)findViewById(R.id.valueReelCapitalEmprunte)).getText().toString());
 		Inputs.tauxCredit = Double.valueOf(
 				((EditText)findViewById(R.id.valueReelTauxCredit)).getText().toString());
 		Inputs.tauxAssuranceCredit = Double.valueOf(
 				((EditText)findViewById(R.id.valueReelTauxAssurance)).getText().toString());
 		//====================================
 		Inputs.prixFAI = Double.valueOf(
 				((EditText)findViewById(R.id.valueReelPrixFAI)).getText().toString());
 		Inputs.agence = ((CheckBox)findViewById(R.id.valueAgence)).isChecked();
 		Inputs.travaux = Double.valueOf(
 				((EditText)findViewById(R.id.valueTravaux)).getText().toString());
 		Inputs.dureeCredit = Integer.valueOf(
 				((EditText)findViewById(R.id.valueDureeCredit)).getText().toString());
 		Inputs.amenagement = Double.valueOf(
 				((EditText)findViewById(R.id.valueAmenagement)).getText().toString());
 		Inputs.conseil = ((CheckBox)findViewById(R.id.valueConseil)).isChecked();
 		Inputs.apport = Double.valueOf(
 				((EditText)findViewById(R.id.valueApport)).getText().toString()); 
 		Inputs.autresFrais = Double.valueOf(
 				((EditText)findViewById(R.id.valueAutresFrais)).getText().toString()); 
 		//TODO Check user's input values (mandatory, empty, formated, ...)
 		//TODO Directly instanciate the fields' values in the object Bien instead of the Inputs intermediate classe
 		
 		//Popultate the created instance of Bien (and run the different calculs)
 		bien.setAcquisition(Temp.TestAcquisition());
 		bien.setGestion(Temp.TestGestion());
 		
 		//Launch the update of the UI to display the computed values
 		fillComputedValues(bien.getAcquisition(), bien.getGestion());
 		
 	}
 	
 	private void fillComputedValues(Acquisition a, Gestion g){
 		//Methods to update the UI fields with all the computed values;
 		
 		//Define the format for the values
 		DecimalFormat formatEur = new DecimalFormat("###,### €");
 		formatEur.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.FRANCE));
 		formatEur.setMaximumFractionDigits(2);
 		formatEur.setMinimumFractionDigits(2);
 		formatEur.setMinimumIntegerDigits(1);
 		DecimalFormat formatPer = new DecimalFormat("## %");
 		formatPer.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.FRANCE));
 		formatPer.setMaximumFractionDigits(2);
 		formatPer.setMinimumFractionDigits(2);
 		formatPer.setMinimumIntegerDigits(1);
 		
 		//Format user's inputs for FraisAcquisition
 		//((EditText) findViewById(R.id.valuePrixFAI)).setText(
 		//		String.valueOf(formatEur.format((a.getFraisAcquisition()).getPrixFAI())));
 		
 		//Fill computed values for FraisAcquisition
 		((TextView) findViewById(R.id.valueNetVendeur)).setText(
 				String.valueOf(formatEur.format((a.getFraisAcquisition()).getNetVendeur())));
 		((TextView) findViewById(R.id.valueFraisAgence)).setText(
 				String.valueOf(formatEur.format((a.getFraisAcquisition()).getFraisAgence())));
 		((TextView) findViewById(R.id.valueFraisNotaire)).setText(
 				String.valueOf(formatEur.format((a.getFraisAcquisition()).getFraisNotaire())));
 		((TextView) findViewById(R.id.valueHonoraireConseil)).setText(
 				String.valueOf(formatEur.format((a.getFraisAcquisition()).getHonoraireConseil())));
 		((TextView) findViewById(R.id.valueCoutTotal)).setText(
 				String.valueOf(formatEur.format((a.getFraisAcquisition()).getCoutTotal())));
 		((TextView) findViewById(R.id.valueSequestre)).setText(
 				String.valueOf(formatEur.format((a.getFraisAcquisition()).getSequestre())));
 		
 		//Fill computed values for Emprunt
 		((TextView) findViewById(R.id.valueCapitalEmprunte)).setText(
 				String.valueOf(formatEur.format((a.getEmprunt()).getCapitalEmprunte())));
 		((TextView) findViewById(R.id.valueNbMensualite)).setText(
				String.valueOf((a.getEmprunt()).getNbMensualiteCredit()));
 		((TextView) findViewById(R.id.valueTauxCredit)).setText(
 				String.valueOf(formatPer.format((a.getEmprunt()).getTauxCredit())));
 		((TextView) findViewById(R.id.valueTauxAssurance)).setText(
 				String.valueOf(formatPer.format((a.getEmprunt()).getTauxAssuranceCredit())));
 		((TextView) findViewById(R.id.valueMensualite)).setText(
 				String.valueOf(formatEur.format((a.getEmprunt()).getMensualiteCredit())));
 		((TextView) findViewById(R.id.valueTauxEndettement)).setText(
 				String.valueOf(formatPer.format((a.getEmprunt()).getTauxEndettement())));
 	}
 	
 	public void collapseUI (View view){
 		//Methods to collapse or expand the UI to focus on most important fields or display all fields
 		
 		//Get the Form layout aggregating all fields
 		LinearLayout ll = (LinearLayout)findViewById(R.id.layoutForm);
 
 		//Get all childs of the Form layout and check for each one if their tag is "Collapsable"
 		for (int i=0; i < ll.getChildCount(); i++){
 			View v = ll.getChildAt(i);
 			if (v.getTag().toString().equals("Collapsable")){			//Condition doesn't work for view without tag, need to check if tag is empty
 				//If the item is "Collapsable"
 				//Checking if the UI is already collapsed or not
 				if (AcquisitionCollpased)
 					//If already collapsed, so set the visibility to visible (0) 
 					v.setVisibility(0);
 				else
 					//If not already collapsed, so set the visibility to gone (8)
 					v.setVisibility(8);
 			}
 		}
 		//FInally update the global variable 
 		if (AcquisitionCollpased)
 		{
 			AcquisitionCollpased = false;
 			((Button)view).setText("Afficher moins");
 		}		
 		else
 		{
 			AcquisitionCollpased = true;
 			((Button)view).setText("Afficher plus");
 		}
 	}
 	
 	public void switchRealField(View view){
 		//Methods to switch between the TextView (for the computed values) to the EditText (allowing user to fill its own real value)
 		
 		switch (view.getId()){		//Get the ID of the item requesting to switch TextView/EditText
 		//Several fields are able to switch between TextView and EditText
 		case R.id.ReelNetVendeur:
 			if (((CheckBox)view).isChecked()){
 				findViewById(R.id.valueNetVendeur).setVisibility(8);		//If checked turn TextView visibility to GONE
 				findViewById(R.id.valueReelNetVendeur).setVisibility(0);	//If checked turn EditText visibility to VISIBLE
 				findViewById(R.id.valueReelNetVendeur).requestFocus();		//If checked set focus to the EditText
 				if (((CheckBox)findViewById(R.id.ReelFraisAgence)).isChecked()){
 					findViewById(R.id.valueReelPrixFAI).setVisibility(8);
 					findViewById(R.id.valuePrixFAI).setVisibility(0);
 				}
 			} else{
 				findViewById(R.id.valueNetVendeur).setVisibility(0);		//If unchecked turn TextView visibility to VISIBLE
 				findViewById(R.id.valueReelNetVendeur).setVisibility(8);	//If unchecked turn EditText visibility to GONE
 				findViewById(R.id.valueReelPrixFAI).setVisibility(0);
 				findViewById(R.id.valuePrixFAI).setVisibility(8);
 			}
 			break;
 		case R.id.ReelFraisAgence:
 			if (((CheckBox)view).isChecked()){
 				findViewById(R.id.valueFraisAgence).setVisibility(8);
 				findViewById(R.id.valueReelFraisAgence).setVisibility(0);
 				findViewById(R.id.valueReelFraisAgence).requestFocus();
 				if (((CheckBox)findViewById(R.id.ReelNetVendeur)).isChecked()){
 					findViewById(R.id.valueReelPrixFAI).setVisibility(8);
 					findViewById(R.id.valuePrixFAI).setVisibility(0);
 				}
 			} else{
 				findViewById(R.id.valueFraisAgence).setVisibility(0);
 				findViewById(R.id.valueReelFraisAgence).setVisibility(8);
 				findViewById(R.id.valueReelPrixFAI).setVisibility(0);
 				findViewById(R.id.valuePrixFAI).setVisibility(8);
 			}
 			break;
 		case R.id.ReelFraisNotaire:
 			if (((CheckBox)view).isChecked()){
 				findViewById(R.id.valueFraisNotaire).setVisibility(8);
 				findViewById(R.id.valueReelFraisNotaire).setVisibility(0);
 				findViewById(R.id.valueReelFraisNotaire).requestFocus();
 			} else{
 				findViewById(R.id.valueFraisNotaire).setVisibility(0);
 				findViewById(R.id.valueReelFraisNotaire).setVisibility(8);
 			}
 			break;
 		case R.id.ReelHonoraireConseil:
 			if (((CheckBox)view).isChecked()){
 				findViewById(R.id.valueHonoraireConseil).setVisibility(8);
 				findViewById(R.id.valueReelHonoraireConseil).setVisibility(0);
 				findViewById(R.id.valueReelHonoraireConseil).requestFocus();
 			} else{
 				findViewById(R.id.valueHonoraireConseil).setVisibility(0);
 				findViewById(R.id.valueReelHonoraireConseil).setVisibility(8);
 			}
 			break;
 		case R.id.ReelCapitalEmprunte:
 			if (((CheckBox)view).isChecked()){
 				findViewById(R.id.valueCapitalEmprunte).setVisibility(8);
 				findViewById(R.id.valueReelCapitalEmprunte).setVisibility(0);
 				findViewById(R.id.valueReelCapitalEmprunte).requestFocus();
 			} else{
 				findViewById(R.id.valueCapitalEmprunte).setVisibility(0);
 				findViewById(R.id.valueReelCapitalEmprunte).setVisibility(8);
 			}
 			break;
 		case R.id.ReelTauxCredit:
 			if (((CheckBox)view).isChecked()){
 				findViewById(R.id.valueTauxCredit).setVisibility(8);
 				findViewById(R.id.valueReelTauxCredit).setVisibility(0);
 				findViewById(R.id.valueReelTauxCredit).requestFocus();
 			} else{
 				findViewById(R.id.valueTauxCredit).setVisibility(0);
 				findViewById(R.id.valueReelTauxCredit).setVisibility(8);
 			}
 			break;
 		case R.id.ReelTauxAssurance:
 			if (((CheckBox)view).isChecked()){
 				findViewById(R.id.valueTauxAssurance).setVisibility(8);
 				findViewById(R.id.valueReelTauxAssurance).setVisibility(0);
 				findViewById(R.id.valueReelTauxAssurance).requestFocus();
 			} else{
 				findViewById(R.id.valueTauxAssurance).setVisibility(0);
 				findViewById(R.id.valueReelTauxAssurance).setVisibility(8);
 			}
 			break;
 		}
 	}
 	
 	/*
 	private double getDoubleValue(int id){
 		return Double.valueOf(((EditText)findViewById(id)).getText().toString());
 	}
 	*/
 }
