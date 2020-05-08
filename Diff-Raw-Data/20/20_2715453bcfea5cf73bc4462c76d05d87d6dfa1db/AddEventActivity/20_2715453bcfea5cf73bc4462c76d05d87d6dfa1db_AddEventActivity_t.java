 package org.cgiar.ilri.mistro.farmer;
 
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.DatePicker;
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
 import org.cgiar.ilri.mistro.farmer.carrier.Dam;
 import org.cgiar.ilri.mistro.farmer.carrier.Farmer;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 public class AddEventActivity extends SherlockActivity implements View.OnClickListener,View.OnFocusChangeListener,DatePickerDialog.OnDateSetListener, Spinner.OnItemSelectedListener
 {
     public static final String TAG="AddEventActivity";
     private final String dateFormat="dd/MM/yyyy";
 
     private TextView cowIdentifierTV;
     private Spinner cowIdentifierS;
     private TextView dateTV;
     private EditText dateET;
     private TextView eventTypeTV;
     private Spinner eventTypeS;
     private TextView eventSubtypeTV;
     private Spinner eventSubtypeS;
     private TextView remarksTV;
     private EditText remarksET;
     private Button okayB;
     private DatePickerDialog datePickerDialog;
     private TextView strawNumberTV;
     private EditText strawNumberET;
     private TextView vetUsedTV;
     private EditText vetUsedET;
     private TextView bullNameTV;
     private AutoCompleteTextView bullNameACTV;
     private TextView bullETNTV;
     private AutoCompleteTextView bullETNACTV;
     private TextView noOfServicingDaysTV;
     private EditText noOfServicingDaysET;
     private TextView servicingTV;
     private Spinner servicingS;
     private TextView causeOfDeathTV;
     private Spinner causeOfDeathS;
 
     private String[] cowNameArray;
     private String[] cowEarTagNumberArray;
     private String[] cowSexArray;
     private String enterDate;
     private String dateInFuture;
     private String eventRecorded;
     private String sendUnsuccessfulWarning;
     private String loadingPleaseWait;
     private List<Integer> servicingIDs;
    private List<String> servicingTypes;
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_add_event);
 
         cowIdentifierTV=(TextView)findViewById(R.id.cow_identifier_tv);
         cowIdentifierS=(Spinner)findViewById(R.id.cow_identifier_s);
         dateTV=(TextView)findViewById(R.id.date_tv);
         dateET=(EditText)findViewById(R.id.date_et);
         dateET.setOnFocusChangeListener(this);
         dateET.setOnClickListener(this);
         eventTypeTV=(TextView)findViewById(R.id.event_type_tv);
         eventTypeS=(Spinner)findViewById(R.id.event_type_s);
         eventTypeS.setOnItemSelectedListener(this);
         eventSubtypeTV=(TextView)findViewById(R.id.event_subtype_tv);
         eventSubtypeS=(Spinner)findViewById(R.id.event_subtype_s);
         servicingTV = (TextView)findViewById(R.id.servicing_tv);
         servicingS = (Spinner)findViewById(R.id.servicing_s);
         strawNumberTV = (TextView)findViewById(R.id.straw_number_tv);
         strawNumberET = (EditText)findViewById(R.id.straw_number_et);
         vetUsedTV = (TextView)findViewById(R.id.vet_used_tv);
         vetUsedET = (EditText)findViewById(R.id.vet_used_et);
         causeOfDeathTV = (TextView)findViewById(R.id.cause_of_death_tv);
         causeOfDeathS = (Spinner)findViewById(R.id.cause_of_death_s);
         bullNameTV = (TextView)findViewById(R.id.bull_name_tv);
         bullNameACTV = (AutoCompleteTextView)findViewById(R.id.bull_name_actv);
         bullETNTV = (TextView)findViewById(R.id.bull_etn_tv);
         bullETNACTV = (AutoCompleteTextView)findViewById(R.id.bull_etn_actv);
         noOfServicingDaysTV = (TextView)findViewById(R.id.no_of_servicing_days_tv);
         noOfServicingDaysET = (EditText)findViewById(R.id.no_of_servicing_days_et);
         remarksTV=(TextView)findViewById(R.id.remarks_tv);
         remarksET=(EditText)findViewById(R.id.remarks_et);
         okayB=(Button)findViewById(R.id.okay_b);
         okayB.setOnClickListener(this);
 
         initTextInViews();
         fetchCowIdentifiers();
         fetchServicingEvents();
     }
 
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.add_event, menu);
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
 
     private void initTextInViews()
     {
         setTitle(Locale.getStringInLocale("add_an_event",this));
         cowIdentifierTV.setText(Locale.getStringInLocale("cow",this));
         dateTV.setText(Locale.getStringInLocale("date",this));
         eventTypeTV.setText(Locale.getStringInLocale("event",this));
 
         int eventTypeArrayID = Locale.getArrayIDInLocale("cow_event_types",this);
         if(eventTypeArrayID !=0) {
             ArrayAdapter<CharSequence> eventTypeArrayAdapter=ArrayAdapter.createFromResource(this, eventTypeArrayID, android.R.layout.simple_spinner_item);
             eventTypeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
             eventTypeS.setAdapter(eventTypeArrayAdapter);
         }
         strawNumberTV.setText(Locale.getStringInLocale("straw_number",this));
         vetUsedTV.setText(Locale.getStringInLocale("vet_used",this));
         causeOfDeathTV.setText(Locale.getStringInLocale("cause_of_death",this));
         int causesOfDeathID = Locale.getArrayIDInLocale("causes_of_death",this);
         if(causesOfDeathID != 0) {
             ArrayAdapter<CharSequence> causesOfDeathArrayAdapter=ArrayAdapter.createFromResource(this, causesOfDeathID, android.R.layout.simple_spinner_item);
             causesOfDeathArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
             causeOfDeathS.setAdapter(causesOfDeathArrayAdapter);
         }
         bullNameTV.setText(Locale.getStringInLocale("servicing_bull_name",this));
         bullETNTV.setText(Locale.getStringInLocale("servicing_bull_ear_tag_number",this));
         noOfServicingDaysTV.setText(Locale.getStringInLocale("no_of_days_in_servicing",this));
         remarksTV.setText(Locale.getStringInLocale("remarks",this));
         okayB.setText(Locale.getStringInLocale("okay",this));
         enterDate=Locale.getStringInLocale("enter_date",this);
         dateInFuture=Locale.getStringInLocale("date_in_future",this);
         eventRecorded=Locale.getStringInLocale("event_successfully_recorded",this);
         sendUnsuccessfulWarning=Locale.getStringInLocale("something_went_wrong_try_again",this);
         loadingPleaseWait = Locale.getStringInLocale("loading_please_wait",this);
         servicingTV.setText(Locale.getStringInLocale("associated_servicing",this));
     }
 
     private void fetchCowIdentifiers()
     {
         CowIdentifierThread cowIdentifierThread=new CowIdentifierThread();
         TelephonyManager telephonyManager=(TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
         cowIdentifierThread.execute(telephonyManager.getSimSerialNumber());
     }
 
     private void setCowIdentifiers(String[] cowIdentifiers)
     {
         if(cowIdentifierS!=null)
         {
             ArrayAdapter<String> cowsArrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,cowIdentifiers);
             cowsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
             cowIdentifierS.setAdapter(cowsArrayAdapter);
         }
     }
 
     @Override
     public void onClick(View view)
     {
         if(view==okayB)
         {
             sendEvent();
         }
         else if(view==dateET)
         {
             dateETClicked();
         }
     }
 
     @Override
     public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
         if(parent == eventTypeS) {
             okayB.setText(Locale.getStringInLocale("okay",this));
             dateTV.setText(Locale.getStringInLocale("date",this));
             strawNumberTV.setVisibility(TextView.GONE);
             strawNumberET.setVisibility(EditText.GONE);
             vetUsedTV.setVisibility(TextView.GONE);
             vetUsedET.setVisibility(EditText.GONE);
             bullNameTV.setVisibility(TextView.GONE);
             bullNameACTV.setVisibility(AutoCompleteTextView.GONE);
             bullETNTV.setVisibility(TextView.GONE);
             bullETNACTV.setVisibility(AutoCompleteTextView.GONE);
             remarksTV.setVisibility(TextView.GONE);
             remarksET.setVisibility(EditText.GONE);
             cowIdentifierS.setVisibility(Spinner.GONE);
             cowIdentifierTV.setVisibility(TextView.GONE);
             noOfServicingDaysTV.setVisibility(TextView.GONE);
             noOfServicingDaysET.setVisibility(EditText.GONE);
             servicingTV.setVisibility(TextView.GONE);
             servicingS.setVisibility(Spinner.GONE);
             causeOfDeathTV.setVisibility(TextView.GONE);
             causeOfDeathS.setVisibility(Spinner.GONE);
             String[] eventTypesEN = Locale.getArrayInLocale("cow_event_types", this, Locale.LOCALE_ENGLISH);
             if(eventTypesEN[eventTypeS.getSelectedItemPosition()].equals("Birth")) {
                 birthEventSelected();
                 eventSubtypeTV.setVisibility(TextView.VISIBLE);
                 eventSubtypeS.setVisibility(Spinner.VISIBLE);
                 servicingTV.setVisibility(TextView.VISIBLE);
                 servicingS.setVisibility(Spinner.VISIBLE);
                cowIdentifierS.setVisibility(Spinner.VISIBLE);
                cowIdentifierTV.setVisibility(TextView.VISIBLE);
                 okayB.setText(Locale.getStringInLocale("next",this));
             }
             else if(eventTypesEN[eventTypeS.getSelectedItemPosition()].equals("Abortion")) {
                 servicingTV.setVisibility(TextView.VISIBLE);
                 servicingS.setVisibility(Spinner.VISIBLE);
                 remarksTV.setVisibility(TextView.VISIBLE);
                 remarksET.setVisibility(EditText.VISIBLE);
                 cowIdentifierS.setVisibility(Spinner.VISIBLE);
                 cowIdentifierTV.setVisibility(TextView.VISIBLE);
             }
             else if(eventTypesEN[eventTypeS.getSelectedItemPosition()].equals("Acquisition")) {
                 okayB.setText(Locale.getStringInLocale("next",this));
             }
             else if(eventTypesEN[eventTypeS.getSelectedItemPosition()].equals("Artificial Insemination")) {
                 strawNumberTV.setVisibility(TextView.VISIBLE);
                 strawNumberET.setVisibility(EditText.VISIBLE);
                 vetUsedTV.setVisibility(TextView.VISIBLE);
                 vetUsedET.setVisibility(EditText.VISIBLE);
                 remarksTV.setVisibility(TextView.VISIBLE);
                 remarksET.setVisibility(EditText.VISIBLE);
                 cowIdentifierS.setVisibility(Spinner.VISIBLE);
                 cowIdentifierTV.setVisibility(TextView.VISIBLE);
             }
             else if(eventTypesEN[eventTypeS.getSelectedItemPosition()].equals("Bull Servicing")) {
                 bullNameTV.setVisibility(TextView.VISIBLE);
                 bullNameACTV.setVisibility(AutoCompleteTextView.VISIBLE);
                 bullETNTV.setVisibility(TextView.VISIBLE);
                 bullETNACTV.setVisibility(AutoCompleteTextView.VISIBLE);
                 remarksTV.setVisibility(TextView.VISIBLE);
                 remarksET.setVisibility(EditText.VISIBLE);
                 cowIdentifierS.setVisibility(Spinner.VISIBLE);
                 cowIdentifierTV.setVisibility(TextView.VISIBLE);
                 noOfServicingDaysTV.setVisibility(TextView.VISIBLE);
                 noOfServicingDaysET.setVisibility(EditText.VISIBLE);
                dateTV.setText(Locale.getStringInLocale("start_date", this));
             }
             else if(eventTypesEN[eventTypeS.getSelectedItemPosition()].equals("Death")) {
                 causeOfDeathTV.setVisibility(TextView.VISIBLE);
                 causeOfDeathS.setVisibility(Spinner.VISIBLE);
                 remarksTV.setVisibility(TextView.VISIBLE);
                 remarksET.setVisibility(EditText.VISIBLE);
                 cowIdentifierS.setVisibility(Spinner.VISIBLE);
                 cowIdentifierTV.setVisibility(TextView.VISIBLE);
             }
             else {
                 remarksTV.setVisibility(TextView.VISIBLE);
                 remarksET.setVisibility(EditText.VISIBLE);
                 cowIdentifierS.setVisibility(Spinner.VISIBLE);
                 cowIdentifierTV.setVisibility(TextView.VISIBLE);
             }
         }
     }
 
     private void birthEventSelected() {
         eventSubtypeTV.setText(Locale.getStringInLocale("type_of_birth",this));
         int eventSubtypeArrayID = Locale.getArrayIDInLocale("birth_types",this);
         if(eventSubtypeArrayID != 0) {
             ArrayAdapter<CharSequence> eventSubtypeArrayAdapter=ArrayAdapter.createFromResource(this, eventSubtypeArrayID, android.R.layout.simple_spinner_item);
             eventSubtypeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
             eventSubtypeS.setAdapter(eventSubtypeArrayAdapter);
         }
     }
 
     @Override
     public void onNothingSelected(AdapterView<?> parent) {
 
     }
 
     private boolean validateInput()
     {
         if(dateET.getText().toString()==null||dateET.getText().toString().length()==0)
         {
             Toast.makeText(this,enterDate,Toast.LENGTH_LONG).show();
             return false;
         }
         else
         {
             if(!validateDate()){
                 return false;
             }
         }
         return true;
     }
 
     private boolean validateDate() {
         try
         {
             Date dateEntered=new SimpleDateFormat(dateFormat, java.util.Locale.ENGLISH).parse(dateET.getText().toString());
             Date today=new Date();
             long milisecondDifference = today.getTime() - dateEntered.getTime();
             long days = milisecondDifference / 86400000;
             if((today.getTime()-dateEntered.getTime())<0)
             {
                 Toast.makeText(this,dateInFuture,Toast.LENGTH_LONG).show();
                 return false;
             }
             else if(days > 30) {//more than one month
                 Toast.makeText(this,Locale.getStringInLocale("event_too_old",this),Toast.LENGTH_LONG).show();
                 return false;
             }
             else {
                 return true;
             }
         }
         catch (ParseException e)
         {
             e.printStackTrace();
             return false;
         }
     }
 
     private void sendEvent()
     {
         if(validateInput() && DataHandler.checkNetworkConnection(this,null))
         {
             String[] eventTypes = Locale.getArrayInLocale("cow_event_types",this,Locale.LOCALE_ENGLISH);
             String selectedEvent = eventTypes[eventTypeS.getSelectedItemPosition()];
             if(selectedEvent.equals("Birth")) {
                 String[] eventSubtypes = Locale.getArrayInLocale("birth_types",this,Locale.LOCALE_ENGLISH);
                 if(eventSubtypes[eventSubtypeS.getSelectedItemPosition()].equals("Normal")) {
                     AlertDialog cowRegistrationAlertDialog = constructCalfRegistrationDialog();
                     cowRegistrationAlertDialog.show();
                 }
             }
             else if(selectedEvent.equals("Acquisition")) {
                 AlertDialog cowRegistrationAlertDialog = constructCowRegistrationDialog();
                 cowRegistrationAlertDialog.show();
             }
             else {
                 String selectedCowETN = cowEarTagNumberArray[cowIdentifierS.getSelectedItemPosition()];
                 String selectedCowName = cowNameArray[cowIdentifierS.getSelectedItemPosition()];
                 TelephonyManager telephonyManager=(TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
                 String serialNumber = telephonyManager.getSimSerialNumber();
                 JSONObject jsonObject = new JSONObject();
                 try
                 {
                     String[] causesOfDeathInEN = Locale.getArrayInLocale("causes_of_death",AddEventActivity.this,Locale.LOCALE_ENGLISH);
                     jsonObject.put("simCardSN", serialNumber);
                     jsonObject.put("cowEarTagNumber", selectedCowETN);
                     jsonObject.put("cowName", selectedCowName);
                     jsonObject.put("date", dateET.getText().toString());
                     jsonObject.put("eventType", selectedEvent);
                     jsonObject.put("remarks", remarksET.getText().toString());
                     jsonObject.put("strawNumber", strawNumberET.getText().toString());
                     jsonObject.put("vetUsed", vetUsedET.getText().toString());
                     jsonObject.put("bullName", bullNameACTV.getText().toString());
                     jsonObject.put("bullEarTagNo", bullETNACTV.getText().toString());
                     jsonObject.put("noOfServicingDays", noOfServicingDaysET.getText().toString());
                     jsonObject.put("parentEvent", servicingIDs.get(servicingS.getSelectedItemPosition()));
                     jsonObject.put("causeOfDeath", causesOfDeathInEN[causeOfDeathS.getSelectedItemPosition()]);
                     CowEventAdditionThread cowEventAdditionThread=new CowEventAdditionThread();
                     cowEventAdditionThread.execute(jsonObject);
                 }
                 catch (JSONException e)
                 {
                     e.printStackTrace();
                 }
             }
 
         }
     }
 
     private AlertDialog constructCalfRegistrationDialog() {
         AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(this);
         alertDialogBuilder.setTitle(Locale.getStringInLocale("calf_registration", this));
         alertDialogBuilder
                 .setMessage(Locale.getStringInLocale("next_screen_is_calf_registration",AddEventActivity.this))
                 .setCancelable(false)
                 .setPositiveButton(Locale.getStringInLocale("next", AddEventActivity.this), new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         int numberOfCows = 1;
                         Farmer farmer = new Farmer();
                         farmer.setCowNumber(numberOfCows);
                         farmer.setMode(Farmer.MODE_NEW_COW_REGISTRATION);
                         TelephonyManager telephonyManager = (TelephonyManager) AddEventActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
                         farmer.setSimCardSN(telephonyManager.getSimSerialNumber());
                         Cow thisCalf = new Cow(true);
                         Dam calfDam = new Dam();
                         calfDam.setEarTagNumber(cowEarTagNumberArray[cowIdentifierS.getSelectedItemPosition()]);
                         calfDam.setName(cowNameArray[cowIdentifierS.getSelectedItemPosition()]);
                         thisCalf.setDam(calfDam);
                         JSONObject jsonObject = new JSONObject();
                         String[] birthTypesInEN = Locale.getArrayInLocale("birth_types", AddEventActivity.this, Locale.LOCALE_ENGLISH);
                         try {
                             jsonObject.put("birthType", birthTypesInEN[eventSubtypeS.getSelectedItemPosition()]);
                             jsonObject.put("parentEvent", servicingIDs.get(servicingS.getSelectedItemPosition()));
                             thisCalf.setPiggyBack(jsonObject.toString());
                         } catch (JSONException e) {
                             e.printStackTrace();
                         }
                        if(servicingTypes.get(servicingS.getSelectedItemPosition()).equals("Artificial Insemination")) {
                            thisCalf.setServiceType(Cow.SERVICE_TYPE_AI);
                        }
                        else if(servicingTypes.get(servicingS.getSelectedItemPosition()).equals("Bull Servicing")) {
                            thisCalf.setServiceType(Cow.SERVICE_TYPE_BULL);
                        }
                         thisCalf.setDateOfBirth(dateET.getText().toString());
                         thisCalf.setMode(Cow.MODE_BORN_CALF_REGISTRATION);

                         farmer.setCow(thisCalf, 0);
 
                         Intent intent = new Intent(AddEventActivity.this, CowRegistrationActivity.class);
                         //intent.putExtra(CowRegistrationActivity.KEY_MODE,CowRegistrationActivity.MODE_COW);
                         intent.putExtra(CowRegistrationActivity.KEY_INDEX, 0);
                         intent.putExtra(CowRegistrationActivity.KEY_NUMBER_OF_COWS, numberOfCows);
                         Bundle bundle = new Bundle();
                         bundle.putParcelable(Farmer.PARCELABLE_KEY, farmer);
                         intent.putExtras(bundle);
                         startActivity(intent);
                     }
                 })
                 .setNegativeButton(Locale.getStringInLocale("cancel", AddEventActivity.this), new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         dialog.cancel();
                     }
                 });
         AlertDialog alertDialog=alertDialogBuilder.create();
         return alertDialog;
     }
 
     private AlertDialog constructCowRegistrationDialog() {
         AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(this);
         alertDialogBuilder.setTitle(Locale.getStringInLocale("cow_registration_proper", this));
         alertDialogBuilder
                 .setMessage(Locale.getStringInLocale("next_screen_is_cow_registration",AddEventActivity.this))
                 .setCancelable(false)
                 .setPositiveButton(Locale.getStringInLocale("next", AddEventActivity.this), new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         int numberOfCows = 1;
                         Farmer farmer = new Farmer();
                         farmer.setCowNumber(numberOfCows);
                         farmer.setMode(Farmer.MODE_NEW_COW_REGISTRATION);
                         TelephonyManager telephonyManager = (TelephonyManager) AddEventActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
                         farmer.setSimCardSN(telephonyManager.getSimSerialNumber());
                         Cow thisCow = new Cow(true);
                         JSONObject jsonObject = new JSONObject();
                         try {
                             jsonObject.put("remarks", remarksET.getText().toString());
                             thisCow.setPiggyBack(jsonObject.toString());
                         } catch (JSONException e) {
                             e.printStackTrace();
                         }
                         thisCow.setMode(Cow.MODE_ADULT_COW_REGISTRATION);
                         farmer.setCow(thisCow, 0);
 
                         Intent intent = new Intent(AddEventActivity.this, CowRegistrationActivity.class);
                         //intent.putExtra(CowRegistrationActivity.KEY_MODE,CowRegistrationActivity.MODE_COW);
                         intent.putExtra(CowRegistrationActivity.KEY_INDEX, 0);
                         intent.putExtra(CowRegistrationActivity.KEY_NUMBER_OF_COWS, numberOfCows);
                         Bundle bundle = new Bundle();
                         bundle.putParcelable(Farmer.PARCELABLE_KEY, farmer);
                         intent.putExtras(bundle);
                         startActivity(intent);
                     }
                 })
                 .setNegativeButton(Locale.getStringInLocale("cancel", AddEventActivity.this), new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         dialog.cancel();
                     }
                 });
         AlertDialog alertDialog=alertDialogBuilder.create();
         return alertDialog;
     }
 
     private class CowEventAdditionThread extends AsyncTask<JSONObject, Integer, String>
     {
         private ProgressDialog progressDialog;
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             progressDialog = ProgressDialog.show(AddEventActivity.this, "",loadingPleaseWait, true);
         }
 
         @Override
         protected String doInBackground(JSONObject... params)
         {
             String result = DataHandler.sendDataToServer(params[0].toString(), DataHandler.FARMER_ADD_COW_EVENT_URL);
             return result;
         }
 
         @Override
         protected void onPostExecute(String result)
         {
             super.onPostExecute(result);
             progressDialog.dismiss();
             if(result.equals(DataHandler.ACKNOWLEDGE_OK))
             {
                 Toast.makeText(AddEventActivity.this, eventRecorded, Toast.LENGTH_LONG).show();
                 Intent intent = new Intent(AddEventActivity.this, EventsActivity.class);
                 intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                 startActivity(intent);
             }
             else
             {
                 Toast.makeText(AddEventActivity.this, sendUnsuccessfulWarning, Toast.LENGTH_LONG).show();
             }
         }
     }
 
     @Override
     public void onFocusChange(View view, boolean hasFocus)
     {
         if(view==dateET && hasFocus)
         {
             dateETClicked();
         }
     }
 
     private void dateETClicked()
     {
         Date date=null;
 
         if(dateET.getText().toString().length()>0)
         {
             try
             {
                 date=new SimpleDateFormat(dateFormat, java.util.Locale.ENGLISH).parse(dateET.getText().toString());
             }
             catch (ParseException e)
             {
                 e.printStackTrace();
             }
         }
         if(date==null)
         {
             date=new Date();
         }
 
         Calendar calendar=new GregorianCalendar();
         calendar.setTime(date);
         datePickerDialog=new DatePickerDialog(this,this,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
         //datePickerDialog=createDialogWithoutDateField(this,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
         datePickerDialog.show();
     }
 
     @Override
     public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
     {
         String dateString=String.valueOf(dayOfMonth)+"/"+String.valueOf(monthOfYear+1)+"/"+String.valueOf(year);
         dateET.setText(dateString);
         if(!validateDate()){
             dateET.setText("");
         }
     }
 
     private class CowIdentifierThread extends AsyncTask<String,Integer,String>
     {
 
         private ProgressDialog progressDialog;
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             progressDialog = ProgressDialog.show(AddEventActivity.this, "",loadingPleaseWait, true);
         }
 
         @Override
         protected String doInBackground(String... params)
         {
             JSONObject jsonObject=new JSONObject();
             try
             {
                 jsonObject.put("simCardSN",params[0]);
                 String result= DataHandler.sendDataToServer(jsonObject.toString(), DataHandler.FARMER_FETCH_COW_IDENTIFIERS_URL);
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
                 JSONArray sexTextArray = jsonObject.getJSONArray("sex");
                 String[] cowArray=new String[cowNamesArray.length()];
                 String[] earTagArray=new String[cowNamesArray.length()];
                 String[] sexArray=new String[cowNamesArray.length()];
                 for(int i=0;i<cowNamesArray.length();i++)
                 {
                     cowArray[i]=cowNamesArray.get(i).toString();
                     earTagArray[i]=earTagNumbersArray.get(i).toString();
                     sexArray[i]=sexTextArray.get(i).toString();
                 }
                 //TODO: warn user if no cows
                 if(cowArray.length==0)
                 {
                     Toast.makeText(AddEventActivity.this, "no cows fetched", Toast.LENGTH_LONG).show();
                 }
                 AddEventActivity.this.cowNameArray =cowArray;
                 AddEventActivity.this.cowEarTagNumberArray=earTagArray;
                 AddEventActivity.this.cowSexArray = sexArray;
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
 
     private void fetchServicingEvents() {
         TelephonyManager telephonyManager=(TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
         CowEventHistoryThread cowEventHistoryThread =new CowEventHistoryThread();
         cowEventHistoryThread.execute(telephonyManager.getSimSerialNumber());
     }
 
     private class CowEventHistoryThread extends AsyncTask<String, Integer, String> {
 
         ProgressDialog progressDialog;
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             progressDialog = ProgressDialog.show(AddEventActivity.this, "",Locale.getStringInLocale("loading_please_wait",AddEventActivity.this), true);
         }
 
         @Override
         protected String doInBackground(String... params) {
             String result = null;
             JSONObject jsonObject = new JSONObject();
             try {
                 jsonObject.put("simCardSN",params[0]);
                 result = DataHandler.sendDataToServer(jsonObject.toString(),DataHandler.FARMER_FETCH_COW_SERVICING_EVENTS_URL);
             }
             catch (JSONException e) {
                 e.printStackTrace();
             }
 
             return result;
         }
 
         @Override
         protected void onPostExecute(String result) {
             super.onPostExecute(result);
             progressDialog.dismiss();
             if(result == null) {
                 Toast.makeText(AddEventActivity.this, Locale.getStringInLocale("problem_connecting_to_server",AddEventActivity.this), Toast.LENGTH_LONG).show();
             }
             else if(result.equals(DataHandler.NO_DATA)) {
                 Toast.makeText(AddEventActivity.this, Locale.getStringInLocale("no_data_received",AddEventActivity.this), Toast.LENGTH_LONG).show();
             }
             else {
                 try {
                     JSONObject jsonObject = new JSONObject(result);
                     JSONArray historyArray = jsonObject.getJSONArray("history");
                     List<String> servicingNames = new ArrayList<String>();
                     servicingIDs = new ArrayList<Integer>();
                    servicingTypes = new ArrayList<String>();
                     for(int i = 0; i < historyArray.length(); i++) {
                         JSONObject currentServicing = historyArray.getJSONObject(i);
                         servicingIDs.add(currentServicing.getInt("id"));
                         servicingNames.add(currentServicing.getString("event_date")+" ("+currentServicing.getString("ear_tag_number")+")");
                        servicingTypes.add(currentServicing.getString("event_name"));
                     }
                     ArrayAdapter<String> servicingsArrayAdapter=new ArrayAdapter<String>(AddEventActivity.this,android.R.layout.simple_spinner_dropdown_item,servicingNames);
                     servicingsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                     servicingS.setAdapter(servicingsArrayAdapter);
 
                 }
                 catch (JSONException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
     
 }
