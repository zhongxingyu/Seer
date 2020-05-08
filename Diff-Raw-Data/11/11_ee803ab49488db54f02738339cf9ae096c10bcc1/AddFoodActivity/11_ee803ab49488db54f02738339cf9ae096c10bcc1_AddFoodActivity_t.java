 package com.chips;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.Toast;
 
 import com.chips.dataclient.DataPushClient;
 import com.chips.dataclient.FoodClient;
 import com.chips.dataclientactions.PushClientToastOnFailureAction;
 import com.chips.dataclientobservers.UpdateActionDataClientObserver;
 import com.chips.datarecord.FoodRecord;
 import com.chips.homebar.HomeBar;
 import com.chips.homebar.HomeBarAction;
 import com.chips.user.PersistentUser;
 import com.google.zxing.integration.android.IntentIntegrator;
 import com.google.zxing.integration.android.IntentResult;
 
 public class AddFoodActivity extends DataClientActivity
         implements HomeBar {
     private static final String LAST_DIALOG_FOOD_NAMES = "lastDialogFoodNames";
     private static final String LAST_DIALOG_FOOD_RECORDS = "lastDialogFoodRecords";
     private static final int SEARCH_REQUEST_CODE = 0;
     private static final String BASE_URL 
         = "http://cs110chips.phpfogapp.com/index.php/mobile/";
     private static final String ASSIGN_BARCODE_TO_FOOD_URL
         = BASE_URL + "assign_barcode_to_food/";
     private static final String GET_FOOD_WITH_BARCODE_URL
         = BASE_URL + "get_food_with_barcode/";
     private static final String ADD_NEW_FOOD_TO_DATABASE_URL
         = BASE_URL + "add_new_food_to_database";
     public static final String BUNDLE_ADD_KEY = "addURL";
     public static final String BUNDLE_ARGUMENTS_KEY = "fixedArguments";
     
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         HomeBarAction.inflateHomeBarView(this, R.layout.add_food_to_inventory);
         
         setupAddURL();
         setupWebsiteCommunication();
         
         searchFoodIntent = new Intent(this, SearchFoodActivity.class);
         handleLinkBarcode = false;
         setupEditTexts();
         
         restoreDialog(savedInstanceState);
     }
     
     @SuppressWarnings("unchecked")
     private void restoreDialog(Bundle savedInstanceState) {
         if (savedInstanceState != null) {
             lastDialogFoodNames = savedInstanceState.getStringArray(
                     LAST_DIALOG_FOOD_NAMES);
             lastDialogFoodRecords = (ArrayList<FoodRecord>) 
                     savedInstanceState.getSerializable(LAST_DIALOG_FOOD_RECORDS);
             if (lastDialogFoodNames != null && lastDialogFoodRecords != null) {
                 makeSelectFoodDialog(lastDialogFoodNames, lastDialogFoodRecords);
             }
         }
     }
     
     private void setupAddURL() {
         Bundle b = getIntent().getExtras();
         
         addURL = b.getString(BUNDLE_ADD_KEY);
         fixedArguments = b.getString(BUNDLE_ARGUMENTS_KEY);
     }
     
     private void setupWebsiteCommunication() {
         pushClient = new DataPushClient();
         foodClient = new FoodClient();
         UpdateActionDataClientObserver updateActionObserver 
             = new UpdateActionDataClientObserver(this, pushClient);
         Toast failureToast = Toast.makeText(this, 
                 "Website Communication Failed", Toast.LENGTH_LONG);
         PushClientToastOnFailureAction action 
             = new PushClientToastOnFailureAction(failureToast);
         updateActionObserver.addAction(action);
         
         addClientObserverPair(pushClient, updateActionObserver);
     }
     
     private void setupEditTexts() {
         nameField = (EditText) findViewById(R.id.newFoodNameEditText);
         caloriesField = (EditText) findViewById(R.id.caloriesEditText);
         carbohydratesField 
             = (EditText) findViewById(R.id.carbohydratesEditText);
         proteinField = (EditText) findViewById(R.id.proteinEditText);
         fatField = (EditText) findViewById(R.id.fatEditText);
         quantityField = (EditText) findViewById(R.id.quantityEditText);
         barcodeFormatField 
             = (EditText) findViewById(R.id.barcodeFormatEditText);
         barcodeField 
             = (EditText) findViewById(R.id.barcodeEditText);
     }
     
     /*
      * Callback function for when 'Scan Barcode' is clicked
      */
     public void scanBarcodeClicked(View view) {
         startBarcodeScanner();
         handleLinkBarcode = false;
     }
     
     public void linkBarcodeClicked(View view) {
         startBarcodeScanner();
         handleLinkBarcode = true;
     }
     
     private void startBarcodeScanner() {
         IntentIntegrator integrator = new IntentIntegrator(this);
         integrator.initiateScan();
     }
     
     // Callback for barcode scanner activity
     public void onActivityResult(int requestCode, int resultCode, Intent intent) {
         switch (requestCode) {
         case SEARCH_REQUEST_CODE:
             handleSearchResult(requestCode, resultCode, intent);
             break;
         default:
             handleScanResult(requestCode, resultCode, intent);
         }
     }
     
     private void handleSearchResult(int requestCode, int resultCode, 
             Intent intent) {
         if (resultCode == RESULT_OK) {
             Bundle extras = intent.getExtras();
             FoodRecord selectedFood = (FoodRecord) extras.get("selectedFood");
             
             foodToAdd = selectedFood;
             populateFields(selectedFood);
         }
     }
     
     private void populateFields(FoodRecord food) {
         if (food != null) {
             nameField.setText(food.getName());
             caloriesField.setText(Double.toString(food.getCalories()));
             carbohydratesField.setText(Double.toString(food.getCarbohydrates()));
             proteinField.setText(Double.toString(food.getProtein()));
             fatField.setText(Double.toString(food.getFat()));
             
             setNutritionFieldsEnabled(false);
         }
     }
     
     private void setNutritionFieldsEnabled(boolean enabled) {
         nameField.setEnabled(enabled);
         nameField.setFocusable(enabled);
         caloriesField.setEnabled(enabled);
         caloriesField.setFocusable(enabled);
         carbohydratesField.setEnabled(enabled);
         carbohydratesField.setFocusable(enabled);
         proteinField.setEnabled(enabled);
         proteinField.setFocusable(enabled);
         fatField.setEnabled(enabled);
         fatField.setFocusable(enabled);
     }
     
     private void handleScanResult(int requestCode, int resultCode, 
             Intent intent) {
         IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
 //        ArrayList<String> assignBarcodeArguments = new ArrayList<String>();
         
         if (scanResult == null || scanResult.getContents() == null) return;
         
         if (!handleLinkBarcode)  {
             // handle barcode lookup
             List<FoodRecord> foods = synchronousLookupFoodsWithBarcode(scanResult.getContents(), 
                     scanResult.getFormatName());
             if (foods.size() > 0) {
                 getUserFoodChoice(foods);
             } else {
                 Toast.makeText(this, "No foods with that barcode found." 
                         + "Please enter nutritional information manually", 
                         Toast.LENGTH_LONG);
             }
         }
 
         barcodeField.setText(scanResult.getContents());
         barcodeFormatField.setText(scanResult.getFormatName());
     }
     
     private List<FoodRecord> synchronousLookupFoodsWithBarcode(String barcode,
             String format) {
         
         ArrayList<String> assignBarcodeArguments = new ArrayList<String>();
         assignBarcodeArguments.add(PersistentUser.getSessionID());
         assignBarcodeArguments.add(barcode);
         assignBarcodeArguments.add(format);
         
         foodClient.setURL(GET_FOOD_WITH_BARCODE_URL, 
                   assignBarcodeArguments);
         foodClient.logURL();
         foodClient.synchronousLoadClientData();
         
         return foodClient.getFoodRecords();
     }
     
     private void getUserFoodChoice(List<FoodRecord> foods) {
         String[] foodNames = new String[foods.size()];
         
         for (int i = 0; i < foods.size(); i++) {
             foodNames[i] = foods.get(i).toString();
         }
 
         if (foods.size() > 1) {
             makeSelectFoodDialog(foodNames, foods);
 //            AlertDialog.Builder builder = new AlertDialog.Builder(this);
 //            builder.setTitle("Multiple foods with given barcode found:");
 //            builder.setItems(foodNames, 
 //                    new SelectFoodWithBarcodeListener(foods));
 //            lastAlertDialog = builder.create();
 //            lastAlertDialog.show();
         } else {
             foodToAdd = foods.get(0);
             populateFields(foodToAdd);
         }
     }
     
     private void makeSelectFoodDialog(String[] foodNames, 
             List<FoodRecord> foods) {
         lastDialogFoodNames = foodNames;
         lastDialogFoodRecords = new ArrayList<FoodRecord>(foods);
         
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle("Multiple foods with given barcode found:");
         builder.setItems(foodNames, 
                 new SelectFoodWithBarcodeListener(foods));
         AlertDialog alertDialog = builder.create();
         alertDialog.show();
     }
     
     public void addFoodToInventoryClicked(View view) {
         ImageButton addFoodToInventoryButton 
             = (ImageButton) findViewById(R.id.addButton);
         addFoodToInventoryButton.requestFocus();
         
         if (missingFoodFieldValuesExist()) {
             Toast.makeText(this, 
                     "Please fill name and all nutritional information for "
                     + "new food", 
                     Toast.LENGTH_LONG).show();
         }
         
         if (foodToAdd == null) {
             boolean success = createFoodFromFields();
             if (!success) return;
         }
         
         if (pushFoodToAdd()) finish();
     }
     
     private boolean createFoodFromFields() {
         ArrayList<String> createFoodArguments = new ArrayList<String>();
         createFoodArguments.add(PersistentUser.getSessionID());
         createFoodArguments.add(nameField.getText().toString());
         createFoodArguments.add(caloriesField.getText().toString());
         createFoodArguments.add(carbohydratesField.getText().toString());
         createFoodArguments.add(fatField.getText().toString());
         createFoodArguments.add(proteinField.getText().toString());
         
         foodClient.setURL(ADD_NEW_FOOD_TO_DATABASE_URL, createFoodArguments);
         foodClient.logURL();
         foodClient.synchronousLoadClientData();
 
         // make foodToAdd the returned food
         List<FoodRecord> createdFoodList = foodClient.getFoodRecords();
         boolean success = (createdFoodList.size() > 0);
         
         if (success) {
             foodToAdd = createdFoodList.get(0);
         } else {
             Toast.makeText(this, "Communication Error", 
                     Toast.LENGTH_LONG).show();
         }
         
         return success;
     }
     
     private boolean pushFoodToAdd() {
         ArrayList<String> addFoodArguments = new ArrayList<String>();
         addFoodArguments.add(PersistentUser.getSessionID());
         if (fixedArguments != null) addFoodArguments.add(fixedArguments);
         addFoodArguments.add(foodToAdd.getId() + "");
         addFoodArguments.add(quantityField.getText().toString());
         
         String barcode = barcodeField.getText().toString();
         String barcodeFormat = barcodeField.getText().toString();
         if (!barcode.equals("") && !barcodeFormat.equals("")) {
             assignBarcodeToFood(barcodeField.getText().toString(), 
                     barcodeFormatField.getText().toString(), 
                     foodToAdd.getId() + "");
         }
         
         pushClient.setURL(addURL, addFoodArguments);
         pushClient.logURL();
         pushClient.synchronousLoadClientData();
         
         boolean success = pushClient.lastCompletedPushSuccessful();
         if (!success) {
             Toast.makeText(this, "Communication Error", 
                     Toast.LENGTH_LONG).show();
         }
         
         return success;
     }
     
     private void assignBarcodeToFood(String barcode, String barcodeFormat, 
             String foodID) {
         ArrayList<String> assignBarcodeArguments = new ArrayList<String>();
         assignBarcodeArguments.add(PersistentUser.getSessionID());
         assignBarcodeArguments.add(barcode);
         assignBarcodeArguments.add(barcodeFormat);
         assignBarcodeArguments.add(foodID);
           
         pushClient.setURL(ASSIGN_BARCODE_TO_FOOD_URL, 
                   assignBarcodeArguments);
         pushClient.logURL();
         pushClient.synchronousLoadClientData();
     }
     
     private boolean missingFoodFieldValuesExist() {
         return (nameField.getText().toString().equals("")
                     || caloriesField.getText().toString().equals("")
                     || carbohydratesField.getText().toString().equals("")
                     || proteinField.getText().toString().equals("")
                     || fatField.getText().toString().equals("")
                     || quantityField.getText().toString().equals(""));
     }
     
     public void goHomeClicked(View view) {
         HomeBarAction.goHomeClicked(this, view);
     }
     
     public void addFavoriteClicked(View view) {
         HomeBarAction.addFavoriteClicked(this, view);
     }
     
     public void searchFoodClicked(View view) {
         startActivityForResult(searchFoodIntent, SEARCH_REQUEST_CODE);
     }
     
     private class SelectFoodWithBarcodeListener 
             implements DialogInterface.OnClickListener {
         public SelectFoodWithBarcodeListener(List<FoodRecord> selectableFoods) {
             this.selectableFoods = selectableFoods;
         }
         
         @Override
         public void onClick(DialogInterface dialog, int which) {
             foodToAdd = selectableFoods.get(which);
             populateFields(foodToAdd);
            
            lastDialogFoodNames = null;
            lastDialogFoodRecords = null;
         }
         
         private List<FoodRecord> selectableFoods;
         
     }
     
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         
         outState.putSerializable(LAST_DIALOG_FOOD_RECORDS, 
                 lastDialogFoodRecords);
         outState.putStringArray(LAST_DIALOG_FOOD_NAMES, lastDialogFoodNames);
     }
     
     private Intent searchFoodIntent;
     private EditText nameField;
     private EditText caloriesField;
     private EditText carbohydratesField;
     private EditText proteinField;
     private EditText fatField;
     private EditText quantityField;
     private EditText barcodeField;
     private EditText barcodeFormatField;
     private DataPushClient pushClient;
     private FoodClient foodClient;
     private FoodRecord foodToAdd;
     private String addURL;
     private String fixedArguments;
     private boolean handleLinkBarcode;
     private String[] lastDialogFoodNames;
     private ArrayList<FoodRecord> lastDialogFoodRecords;
 }
