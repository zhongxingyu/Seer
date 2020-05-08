 package com.silentcorp.autotracker.activities;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.InputType;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.silentcorp.autotracker.R;
 import com.silentcorp.autotracker.beans.VehicleBean;
 import com.silentcorp.autotracker.controls.NumberView;
 import com.silentcorp.autotracker.controls.spinneradapter.FuelSpinnerAdapter;
 import com.silentcorp.autotracker.db.Utils;
 import com.silentcorp.autotracker.db.VehicleDB;
 
 /**
  * Add/Edit vehicle activity. Used to add or edit a vehicle to DB.
  * 
  * @author neandertal
  * 
  */
 public class VehicleActivity extends SherlockActivity
 {
     private VehicleBean vehicle;
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_vehicle);
         // back button in action bar
         getSupportActionBar().setHomeButtonEnabled(true);
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 
         initViews();
 
         // initialize any spinners and auto complete text views
         initChoices();
 
         // add listeners
         initListeners();
     }
 
     private void initViews()
     {
         String currSuff = Utils.getLocalizedCurrencySuffix(this);
         String distSuff = Utils.getLocalizedDistanceSuffix(this);
 
         // initialize purchase price view
         NumberView purchasePriceView = (NumberView) findViewById(R.id.purchase_price_number_view);
         purchasePriceView.setDialogTitle(getString(R.string.text_price));
         purchasePriceView.setStep(1.0);
         purchasePriceView.setValueDecimal(true);
         purchasePriceView.setSuffix(currSuff);
 
         // initialize purchase odometer view
         NumberView purchaseOdomView = (NumberView) findViewById(R.id.purchase_odometer_number_view);
         purchaseOdomView.setDialogTitle(getString(R.string.text_odometer));
         purchaseOdomView.setRange(0.0, Double.MAX_VALUE);
         purchaseOdomView.setStep(1000.0);
         purchaseOdomView.setValueDecimal(false);
         purchaseOdomView.setSuffix(distSuff);
 
         // initialize sell price view
         NumberView sellPriceView = (NumberView) findViewById(R.id.sell_price_number_view);
         sellPriceView.setDialogTitle(getString(R.string.text_price));
         sellPriceView.setStep(1.0);
         sellPriceView.setValueDecimal(true);
         sellPriceView.setSuffix(currSuff);
 
         // initialize sell odometer view
         NumberView sellOdomView = (NumberView) findViewById(R.id.sell_odometer_number_view);
         sellOdomView.setDialogTitle(getString(R.string.text_odometer));
         sellOdomView.setRange(0.0, Double.MAX_VALUE);
         sellOdomView.setStep(1000.0);
         sellOdomView.setValueDecimal(false);
         sellOdomView.setSuffix(distSuff);
     }
 
     /**
      * Add listeners
      */
     private void initListeners()
     {
         TextView nameView = (TextView) findViewById(R.id.name_edit_text);
         nameView.addTextChangedListener(new TextWatcher()
         {
             @Override
             public void onTextChanged(CharSequence s, int start, int before, int count)
             {}
 
             @Override
             public void beforeTextChanged(CharSequence s, int start, int count, int after)
             {}
 
             @Override
             public void afterTextChanged(Editable s)
             {
                 Button saveBtn = (Button) findViewById(R.id.save_button);
                 saveBtn.setEnabled(s.length() > 0);
             }
         });
 
         // On change in primary fuel spinner, disable the selected
         // choice in secondary fuel spinner
         Spinner pfSpinner = (Spinner) findViewById(R.id.primary_fuel_type_spinner);
         pfSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
         {
             private boolean skipFirst = true;
 
             @Override
             public void onItemSelected(AdapterView<?> parent, View selectedItemView, int position, long id)
             {
                 Spinner sfSpinner = (Spinner) findViewById(R.id.secondary_fuel_type_spinner);
                 FuelSpinnerAdapter sfsa = (FuelSpinnerAdapter) sfSpinner.getAdapter();
                 // on view create
                 if (skipFirst)
                 {
                     skipFirst = false;
                     // select for second fuel spinner the actual value in the
                     // object
                     sfSpinner.setSelection(sfsa.getPosition(vehicle.getSecondaryFuel()));
                 }
                 else
                 {
                     FuelSpinnerAdapter pfsa = (FuelSpinnerAdapter) parent.getAdapter();
                     String selValue = pfsa.getItem(position).getValue();
                     // set primary fuel value as disabled in the
                     sfsa.setDisabledValue(selValue);
 
                     // If primary fuel set as secondary, then change secondary
                     // to NULL
                     int selPos = sfSpinner.getSelectedItemPosition();
                     if (selPos == sfsa.getPosition(selValue))
                     {
                         sfSpinner.setSelection(sfsa.getPosition(null));
                     }
                 }
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> arg0)
             {}
         });
 
         // change text of sold check box
         CheckBox soldChk = (CheckBox) findViewById(R.id.sold_checkbox);
         soldChk.setOnCheckedChangeListener(new OnCheckedChangeListener()
         {
             @Override
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
             {
                 buttonView.setText((isChecked) ? R.string.text_yes : R.string.text_no);
             }
         });
     }
 
     @Override
     protected void onStart()
     {
         super.onStart();
 
         Log.d(VehicleActivity.class.getName(), "onStart()");
 
         Intent intent = getIntent();
         long vehicleID = intent.getLongExtra(Utils.KEY_SELECTED_ID, -1l);
 
         if (vehicleID < 0)
         {
             // change title
             setTitle(R.string.text_add_vehicle);
 
             // called to create new vehicle
             vehicle = new VehicleBean();
             Log.d(VehicleActivity.class.getName(), "Created new vehicle.");
         }
         else
         {
             // change title
             setTitle(R.string.text_edit_vehicle);
 
             vehicle = VehicleDB.readVehicle(this, vehicleID, vehicle);
             Log.d(VehicleActivity.class.getName(), "Read vehicle from DB:" + vehicleID);
         }
        
        // called to edit existing vehicle
        loadVehicleToForm();
     }
 
     /**
      * Called by "Cancel" button
      * 
      * @param view
      */
     public void onCancel(View view)
     {
         Log.d(VehicleActivity.class.getName(), "onCancel()");
         setResult(RESULT_CANCELED);
         finish();
     }
 
     /**
      * Called by "Save" button
      * 
      * @param view
      */
     public void onSave(View view)
     {
         Log.d(VehicleActivity.class.getName(), "onSave()");
         saveFormToVehicle();
 
         // Update DB
         if (vehicle.getId() < 0)
         {
             VehicleDB.createVehicle(this, vehicle);
         }
         else
         {
             VehicleDB.updateVehicle(this, vehicle);
         }
 
         // set result to calling activity
         Intent resultIntent = new Intent();
         resultIntent.putExtra(Utils.KEY_SELECTED_ID, vehicle.getId());
         setResult(RESULT_OK, resultIntent);
         // finish activity
         finish();
     }
 
     /**
      * Back key pressed - cancel the operation.
      */
     @Override
     public void onBackPressed()
     {
         onCancel(null);
         return;
     }
 
     /**
      * Loads the vehicle to the form views
      */
    private void loadVehicleToForm()
     {
         Log.d(VehicleActivity.class.getName(), "loadVehicleToForm()");
 
         Utils.setViewText(this, R.id.name_edit_text, vehicle.getName());
 
         // TODO set color
 
         // group details
         Utils.setViewText(this, R.id.make_autocomplete, vehicle.getMake());
         Utils.setViewText(this, R.id.model_edit_text, vehicle.getModel());
         Utils.setViewText(this, R.id.year_edit_text, vehicle.getYear());
         Utils.setViewText(this, R.id.licence_plate_edit_text, vehicle.getLicensePlate());
 
         // group engine
         Utils.setSpinnerSelection(this, R.id.primary_fuel_type_spinner, vehicle.getPrimaryFuel());
         Utils.setSpinnerSelection(this, R.id.secondary_fuel_type_spinner, vehicle.getSecondaryFuel());
 
         // group purchase
         Utils.setDateViewValue(this, R.id.purchase_date_edit_text, vehicle.getPurchaseDate());
         Utils.setNumberViewValue(this, R.id.purchase_price_number_view, vehicle.getPurchasePrice());
         Utils.setNumberViewValue(this, R.id.purchase_odometer_number_view, vehicle.getPurchaseOdometer());
         Utils.setViewText(this, R.id.purchase_note_edit_text, vehicle.getPurchaseNote());
 
         // group sell
         Utils.setCheckboxSelection(this, R.id.sold_checkbox, vehicle.getIsSold());
         Utils.setDateViewValue(this, R.id.sell_date_edit_text, vehicle.getSellDate());
         Utils.setNumberViewValue(this, R.id.sell_price_number_view, vehicle.getSellPrice());
         Utils.setNumberViewValue(this, R.id.sell_odometer_number_view, vehicle.getSellOdometer());
         Utils.setViewText(this, R.id.sell_note_edit_text, vehicle.getSellNote());
     }
 
     /**
      * Saves form values to the current vehicle object
      */
     private void saveFormToVehicle()
     {
         Log.d(VehicleActivity.class.getName(), "saveFormToVehicle()");
 
         vehicle.setName(Utils.getViewText(this, R.id.name_edit_text));
 
         // TODO set color
         vehicle.setColor(1);
 
         // group details
         vehicle.setMake(Utils.getViewText(this, R.id.make_autocomplete));
         vehicle.setModel(Utils.getViewText(this, R.id.model_edit_text));
         vehicle.setYear(Utils.getViewTextAsInt(this, R.id.year_edit_text));
         vehicle.setLicensePlate(Utils.getViewText(this, R.id.licence_plate_edit_text));
 
         // group engine
         vehicle.setPrimaryFuel(Utils.getSpinnerValueAsStr(this, R.id.primary_fuel_type_spinner));
         vehicle.setSecondaryFuel(Utils.getSpinnerValueAsStr(this, R.id.secondary_fuel_type_spinner));
 
         // group purchase
         vehicle.setPurchaseDate(Utils.getDateViewValue(this, R.id.purchase_date_edit_text));
         vehicle.setPurchasePrice(Utils.getNumberViewValue(this, R.id.purchase_price_number_view));
         vehicle.setPurchaseOdometer(Utils.getNumberViewValueAsInt(this, R.id.purchase_odometer_number_view));
         vehicle.setPurchaseNote(Utils.getViewText(this, R.id.purchase_note_edit_text));
 
         // group sell
         vehicle.setIsSold(Utils.getCheckboxValue(this, R.id.sold_checkbox));
         vehicle.setSellDate(Utils.getDateViewValue(this, R.id.sell_date_edit_text));
         vehicle.setSellPrice(Utils.getNumberViewValue(this, R.id.sell_price_number_view));
         vehicle.setSellOdometer(Utils.getNumberViewValueAsInt(this, R.id.sell_odometer_number_view));
         vehicle.setSellNote(Utils.getViewText(this, R.id.sell_note_edit_text));
     }
 
     /**
      * initialize any spinners and auto complete text views
      */
     private void initChoices()
     {
         Log.d(VehicleActivity.class.getName(), "initChoiceViews()");
 
         // fill auto complete text view with predefined list of make's
         AutoCompleteTextView makeATV = (AutoCompleteTextView) findViewById(R.id.make_autocomplete);
         String[] makesArr = getResources().getStringArray(R.array.make_array);
         ArrayAdapter<String> arrAdr = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, makesArr);
         makeATV.setAdapter(arrAdr);
         //TODO disable dictionary suggestions - does not work in emulator
         makeATV.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
         
         // fill primary fuel spinner with fuel types without empty choice
         Spinner pfSpinner = (Spinner) findViewById(R.id.primary_fuel_type_spinner);
         FuelSpinnerAdapter pfsa = new FuelSpinnerAdapter(this, false);
         pfSpinner.setAdapter(pfsa);
 
         // fill secondary fuel spinner with all fuel types
         Spinner sfSpinner = (Spinner) findViewById(R.id.secondary_fuel_type_spinner);
         FuelSpinnerAdapter sfsa = new FuelSpinnerAdapter(this, true);
         sfSpinner.setAdapter(sfsa);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         // Inflate the menu; this adds items to the action bar if it is present.
         getSupportMenuInflater().inflate(R.menu.vehicle, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         // back button in action bar
         switch (item.getItemId())
         {
             case android.R.id.home:
             {
                 onCancel(null);
                 break;
             }
             default:
             {
                 return super.onOptionsItemSelected(item);
             }
         }
         return false;
     }
 
 }
