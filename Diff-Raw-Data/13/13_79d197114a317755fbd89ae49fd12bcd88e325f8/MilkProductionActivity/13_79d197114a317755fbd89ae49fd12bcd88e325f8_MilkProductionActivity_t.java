 package org.cgiar.ilri.mistro.farmer;
 
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 import org.cgiar.ilri.mistro.farmer.backend.DataHandler;
 import org.cgiar.ilri.mistro.farmer.backend.Locale;
 import org.cgiar.ilri.mistro.farmer.carrier.Cow;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class MilkProductionActivity extends SherlockActivity implements View.OnClickListener
 {
     private static final String TAG="MilkProductionActivity";
     private Button addProductionB;
     private Button productionHistoryB;
     private Dialog addMilkProductionDialog;
     private TextView cowTV;
     private Spinner cowS;
     private TextView timeTV;
     private Spinner timeS;
     private TextView quantityTV;
     private EditText quantityET;
     private Button addMilkProductionAddB;
     private String[] cowNameArray;
     private String[] cowEarTagNumberArray;
     private String quantityETEmptyWarning;
     private String infoSuccessfullySent;
     private String problemInData;
     private String loadingPleaseWait;
     private TextView quantityTypeTV;
     private Spinner quantityTypeS;
     private String[] quantityTypes;
    private TextView dateTV;
    private EditText dateET;
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_milk_production);
 
         addProductionB=(Button)this.findViewById(R.id.add_production_b);
         addProductionB.setOnClickListener(this);
         productionHistoryB =(Button)this.findViewById(R.id.production_history_b);
         productionHistoryB.setOnClickListener(this);
         addMilkProductionDialog=new Dialog(this);
         addMilkProductionDialog.setContentView(R.layout.dialog_add_milk_production);
         cowTV=(TextView)addMilkProductionDialog.findViewById(R.id.cow_tv);
         cowS=(Spinner)addMilkProductionDialog.findViewById(R.id.cow_s);
        dateTV=(TextView)addMilkProductionDialog.findViewById(R.id.date_tv);
        dateET=(EditText)addMilkProductionDialog.findViewById(R.id.date_et);
         timeTV=(TextView)addMilkProductionDialog.findViewById(R.id.time_tv);
         timeS=(Spinner)addMilkProductionDialog.findViewById(R.id.time_s);
         quantityTV=(TextView)addMilkProductionDialog.findViewById(R.id.quantity_tv);
         quantityET=(EditText)addMilkProductionDialog.findViewById(R.id.quantity_et);
         quantityTypeTV=(TextView)addMilkProductionDialog.findViewById(R.id.quantity_type_tv);
         quantityTypeS=(Spinner)addMilkProductionDialog.findViewById(R.id.quantity_type_s);
         addMilkProductionAddB=(Button)addMilkProductionDialog.findViewById(R.id.dialog_add_milk_add_b);
         addMilkProductionAddB.setOnClickListener(this);
 
         initTextInViews();
         fetchCowIdentifiers();
     }
 
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.milk_production, menu);
         return true;
     }
 
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         if(item.getItemId() == R.id.action_english) {
             Locale.switchLocale(Locale.LOCALE_ENGLISH, this);
             initTextInViews();
             return true;
         }
         else if(item.getItemId() == R.id.action_swahili) {
             Locale.switchLocale(Locale.LOCALE_SWAHILI, this);
             initTextInViews();
             Toast.makeText(this, "kazi katika maendeleo", Toast.LENGTH_LONG).show();
             return true;
         }
         return false;
     }
 
     public void initTextInViews()
     {
         this.setTitle(Locale.getStringInLocale("milk_production",this));
         addProductionB.setText(Locale.getStringInLocale("add_production",this));
         productionHistoryB.setText(Locale.getStringInLocale("production_history",this));
         addMilkProductionDialog.setTitle(Locale.getStringInLocale("add_production",this));
         cowTV.setText(Locale.getStringInLocale("cow",this));
        dateTV.setText(Locale.getStringInLocale("date",this));
         timeTV.setText(Locale.getStringInLocale("time",this));
         quantityTV.setText(Locale.getStringInLocale("quantity",this));
         addMilkProductionAddB.setText(Locale.getStringInLocale("add",this));
         int milkingTimesArrayID = Locale.getArrayIDInLocale("milking_times",this);
         if(milkingTimesArrayID != 0) {
             ArrayAdapter<CharSequence> milkingTimesArrayAdapter=ArrayAdapter.createFromResource(this, milkingTimesArrayID, android.R.layout.simple_spinner_item);
             milkingTimesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
             timeS.setAdapter(milkingTimesArrayAdapter);
         }
         quantityETEmptyWarning=Locale.getStringInLocale("enter_quantity_of_milk_produced",this);
         infoSuccessfullySent=Locale.getStringInLocale("information_successfully_sent_to_server",this);
         problemInData=Locale.getStringInLocale("production_for_time_already_exists",this);
         loadingPleaseWait = Locale.getStringInLocale("loading_please_wait",this);
         quantityTypeTV.setText(Locale.getStringInLocale("measurement_type",this));
 
         quantityTypes = Locale.getArrayInLocale("quantity_types",this);
         int defaultQuantityTypeIndex = 0;
         if(quantityTypes == null ) {
             quantityTypes = new String[1];
             quantityTypes[0] = "";
         }
         else {
             defaultQuantityTypeIndex = Integer.parseInt(DataHandler.getSharedPreference(this, DataHandler.SP_KEY_MILK_QUANTITY_TYPE,"0"));
         }
         ArrayAdapter<String> quantityTypesArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,quantityTypes);
         quantityTypesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         quantityTypeS.setAdapter(quantityTypesArrayAdapter);
         quantityTypeS.setSelection(defaultQuantityTypeIndex);
     }
 
     @Override
     public void onClick(View view)
     {
         if(view==addProductionB)
         {
             addMilkProductionDialog.show();
         }
         else if(view==addMilkProductionAddB)
         {
             sendMilkProductionData();
         }
         else if(view==productionHistoryB)
         {
             Intent intent=new Intent(MilkProductionActivity.this,MilkProcutionHistoryActivity.class);
             startActivity(intent);
         }
     }
 
     private void fetchCowIdentifiers()
     {
         if(DataHandler.checkNetworkConnection(this,null))
         {
             CowIdentifierThread cowIdentifierThread=new CowIdentifierThread();
             TelephonyManager telephonyManager=(TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
             cowIdentifierThread.execute(telephonyManager.getSimSerialNumber());
         }
     }
 
     private void setCowIdentifiers(String[] cowArray)
     {
         if(cowS!=null)
         {
             ArrayAdapter<String> cowsArrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,cowArray);
             cowsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
             cowS.setAdapter(cowsArrayAdapter);
         }
     }
     
     private class CowIdentifierThread extends AsyncTask<String,Integer,String>
     {
         private ProgressDialog progressDialog;
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             progressDialog = ProgressDialog.show(MilkProductionActivity.this, "",loadingPleaseWait, true);
         }
 
         @Override
         protected String doInBackground(String... params)
         {
             JSONObject jsonObject=new JSONObject();
             try
             {
                 jsonObject.put("simCardSN",params[0]);
                 jsonObject.put("cowSex", Cow.SEX_FEMALE);
                 String result=DataHandler.sendDataToServer(jsonObject.toString(),DataHandler.FARMER_FETCH_COW_IDENTIFIERS_URL);
                 return result;
             }
             catch (JSONException e)
             {
                 e.printStackTrace();
             }
             return null;
         }
 
         @Override
         protected void onPostExecute(String result)
         {
             super.onPostExecute(result);
             progressDialog.dismiss();
             try
             {
                 JSONObject jsonObject=new JSONObject(result);
                 JSONArray cowNamesArray=jsonObject.getJSONArray("cowNames");
                 JSONArray earTagNumbersArray=jsonObject.getJSONArray("earTagNumbers");
                 String[] cowArray=new String[cowNamesArray.length()];
                 String[] earTagArray=new String[cowNamesArray.length()];
                 for(int i=0;i<cowNamesArray.length();i++)
                 {
                     cowArray[i]=cowNamesArray.get(i).toString();
                     earTagArray[i]=earTagNumbersArray.get(i).toString();
                 }
                 //TODO: warn user if no cows
                 if(cowArray.length==0)
                 {
                     Toast.makeText(MilkProductionActivity.this,"no cows fetched",Toast.LENGTH_LONG).show();
                 }
                 MilkProductionActivity.this.cowNameArray =cowArray;
                 MilkProductionActivity.this.cowEarTagNumberArray=earTagArray;
                 String[] identifierArray=new String[cowArray.length];
                 for (int i=0;i<cowArray.length;i++)
                 {
                     if(cowArray[i]!=null&&!cowArray[i].equals(""))
                     {
                         identifierArray[i]=cowArray[i];
                     }
                     else
                     {
                         identifierArray[i]=earTagArray[i];
                     }
                 }
                 setCowIdentifiers(identifierArray);
             }
             catch (JSONException e)
             {
                 e.printStackTrace();
             }
 
         }
     }
 
     private void sendMilkProductionData()
     {
         if(DataHandler.checkNetworkConnection(this,null))
         {
             if(validateInput())
             {
                 TelephonyManager telephonyManager=(TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
                 MilkProductionDataAdditionThread milkProductionDataAdditionThread=new MilkProductionDataAdditionThread();
                 String[] quantityTypesInEN = Locale.getArrayInLocale("quantity_types",this,Locale.LOCALE_ENGLISH);
                 String quantityType = "";
                 if(quantityTypesInEN.length == quantityTypes.length) {
                     DataHandler.setSharedPreference(MilkProductionActivity.this, DataHandler.SP_KEY_MILK_QUANTITY_TYPE, String.valueOf(quantityTypeS.getSelectedItemPosition()));
                     quantityType = quantityTypesInEN[quantityTypeS.getSelectedItemPosition()];
                 }
                milkProductionDataAdditionThread.execute(telephonyManager.getSimSerialNumber(),cowNameArray[cowS.getSelectedItemPosition()],cowEarTagNumberArray[cowS.getSelectedItemPosition()],String.valueOf(timeS.getSelectedItemPosition()),quantityET.getText().toString(),quantityType,dateET.getText().toString());
             }
         }
 
     }
 
     private boolean validateInput()
     {
         if(quantityET.getText().toString()==null)
         {
             Toast.makeText(this,quantityETEmptyWarning,Toast.LENGTH_LONG).show();
             return false;
         }
         else if(quantityET.getText().toString().length()<=0)
         {
             Toast.makeText(this,quantityETEmptyWarning,Toast.LENGTH_LONG).show();
             return false;
         }
         return true;
     }
 
     private class MilkProductionDataAdditionThread extends AsyncTask<String,Integer,String>
     {
         private ProgressDialog progressDialog;
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             progressDialog = ProgressDialog.show(MilkProductionActivity.this, "",loadingPleaseWait, true);
         }
 
         @Override
         protected String doInBackground(String... params)
         {
             Log.d(TAG,"at milkProductionDataAdditionThread");
             JSONObject jsonObject=new JSONObject();
             try
             {
                 jsonObject.put("simCardSN",params[0]);
                 jsonObject.put("cowName",params[1]);
                 jsonObject.put("cowEarTagNumber",params[2]);
                 jsonObject.put("time",params[3]);
                 jsonObject.put("quantity",params[4]);
                 jsonObject.put("quantityType",params[5]);
                jsonObject.put("date",params[6]);
                 String result=DataHandler.sendDataToServer(jsonObject.toString(),DataHandler.FARMER_ADD_MILK_PRODUCTION_URL);
                 Log.d(TAG,"data sent to server, result = "+result);
                 return result;
             }
             catch (JSONException e)
             {
                 e.printStackTrace();
             }
             return null;
         }
 
         @Override
         protected void onPostExecute(String result)
         {
             super.onPostExecute(result);
             progressDialog.dismiss();
             if(result==null)
             {
                 Toast.makeText(MilkProductionActivity.this,"server error",Toast.LENGTH_LONG).show();
             }
             else if(result.equals(DataHandler.ACKNOWLEDGE_OK))
             {
                 addMilkProductionDialog.dismiss();
                 Toast.makeText(MilkProductionActivity.this,infoSuccessfullySent,Toast.LENGTH_LONG).show();
             }
             else if(result.equals(DataHandler.DATA_ERROR))
             {
                 Toast.makeText(MilkProductionActivity.this,problemInData,Toast.LENGTH_LONG).show();
             }
         }
     }
 }
