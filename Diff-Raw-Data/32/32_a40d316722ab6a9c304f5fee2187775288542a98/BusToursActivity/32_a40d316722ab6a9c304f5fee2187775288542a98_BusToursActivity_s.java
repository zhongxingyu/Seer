 /* vim: set ts=4 sw=4 et: */
 
 package org.gitorious.scrapfilbleu.android;
 
 import java.io.IOException;
 import java.net.SocketTimeoutException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.app.Dialog;
 
 import android.content.Context;
 import android.content.DialogInterface;
 
 import android.util.Log;
 
 import android.os.Bundle;
 import android.os.AsyncTask;
 
 import android.view.View;
 
 import android.widget.Toast;
 import android.widget.Button;
 import android.widget.AutoCompleteTextView;
 import android.widget.Spinner;
 import android.widget.ArrayAdapter;
 import android.widget.SimpleAdapter;
 import android.widget.DatePicker;
 import android.widget.TimePicker;
 import android.widget.TextView;
 import android.widget.ProgressBar;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 
 public class BusToursActivity extends Activity
 {
 	private static Context context;
     private DatePicker date;
     private TimePicker time;
     private AutoCompleteTextView txtCityDeparture;
     private AutoCompleteTextView txtStopDeparture;
     private Spinner sens;
     private AutoCompleteTextView txtCityArrival;
     private AutoCompleteTextView txtStopArrival;
     private Spinner listCriteria;
     private Button btnGetJourney;
 
     // Showing Async progress
 	private Dialog dialog;
 	private TextView statusProgressHttp;
 	private ProgressBar progressHttp;
 
     private String[] journeyCriteriaValues;
     private String[] sensValues;
 
     private URLs urls;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         this.context = this;
         this.date               = (DatePicker)findViewById(R.id.date);
         this.time               = (TimePicker)findViewById(R.id.time);
         this.txtCityDeparture   = (AutoCompleteTextView)findViewById(R.id.txtCityDeparture);
         this.txtStopDeparture   = (AutoCompleteTextView)findViewById(R.id.txtStopDeparture);
         this.sens               = (Spinner)findViewById(R.id.Sens);
         this.txtCityArrival     = (AutoCompleteTextView)findViewById(R.id.txtCityArrival);
         this.txtStopArrival     = (AutoCompleteTextView)findViewById(R.id.txtStopArrival);
         this.listCriteria       = (Spinner)findViewById(R.id.listCriteria);
         this.btnGetJourney      = (Button)findViewById(R.id.btnGetJourney);
 
         this.journeyCriteriaValues  = getResources().getStringArray(R.array.journeyCriteriaValues);
         this.sensValues             = getResources().getStringArray(R.array.sensValues);
 
         this.fill();
         this.bindWidgets();
     }
     
     public void fill()
     {
         // fill journey criteria
         ArrayAdapter<CharSequence> criteriaAdapter = ArrayAdapter.createFromResource(this, R.array.journeyCriteria, android.R.layout.simple_spinner_item);
         criteriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         this.listCriteria.setAdapter(criteriaAdapter);
 
         // fill sens
         ArrayAdapter<CharSequence> sensAdapter = ArrayAdapter.createFromResource(this, R.array.sens, android.R.layout.simple_spinner_item);
         sensAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         this.sens.setAdapter(sensAdapter);
 
         BusCities cities = new BusCities();
         BusStops stops = new BusStops();
 
         // fill city autocomplete
         ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, cities.getCities());
         this.txtCityDeparture.setAdapter(cityAdapter);
         this.txtCityArrival.setAdapter(cityAdapter);
 
         // fill stop autocomplete
         ArrayAdapter<String> stopAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, stops.getStops());
         this.txtStopDeparture.setAdapter(stopAdapter);
         this.txtStopArrival.setAdapter(stopAdapter);
     }
 
     public void bindWidgets()
     {
         this.txtCityDeparture.setThreshold(1);
         this.txtCityArrival.setThreshold(1);
         this.txtStopDeparture.setThreshold(1);
         this.txtStopArrival.setThreshold(1);
         this.time.setIs24HourView(true);
         this.btnGetJourney.setOnClickListener(new View.OnClickListener() { public void onClick(View arg0) { onClick_btnGetJourney(); } });
     }
 
     public int getJourneyCriteriaValue()
     {
         return Integer.parseInt(this.journeyCriteriaValues[this.listCriteria.getSelectedItemPosition()]);
     }
 
     public int getSensValue()
     {
         return Integer.parseInt(this.sensValues[this.sens.getSelectedItemPosition()]);
     }
 
     public void onClick_btnGetJourney()
     {
         BusJourney j = new BusJourney();
         j.setCityDep(this.txtCityDeparture.getEditableText().toString());
         j.setCityArr(this.txtCityArrival.getEditableText().toString());
         j.setStopDep(this.txtStopDeparture.getEditableText().toString());
         j.setStopArr(this.txtStopArrival.getEditableText().toString());
         j.setDate(new String()
             + String.valueOf(this.date.getDayOfMonth())
             + "/"
             + String.valueOf(this.date.getMonth())
             + "/"
             + String.valueOf(this.date.getYear())
         );
         j.setHour(String.valueOf(this.time.getCurrentHour()));
         j.setMinute(String.valueOf(this.time.getCurrentMinute()));
         j.setSens(String.valueOf(this.getSensValue()));
         j.setCriteria(String.valueOf(this.getJourneyCriteriaValue()));
         new ProcessScrapping().execute(j);
     }
 
     public static void messageBox(String text) {
 		Toast.makeText(context,text, Toast.LENGTH_SHORT).show();
 	}
 
 	public AlertDialog alertBox(String title, String text) {
 		AlertDialog.Builder dialog = new AlertDialog.Builder(this.context);
 		dialog.setTitle(title);
 		dialog.setMessage(text);
 		dialog.setCancelable(false);
 		dialog.setPositiveButton(
 			getString(R.string.okay),
 			new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					dialog.cancel();
 				}
 			}
 		);
 		return dialog.create();
 	}
 
 	public void alertInfoBox(String title, String text) {
 		AlertDialog d = alertBox("[" + getString(R.string.msgInfoTitle) + "]: " + title, text);
 		d.show();
 	}
 
 	public void alertErrorBox(String title, String text) {
 		AlertDialog d = alertBox("[" + getString(R.string.msgErrorTitle) + "]: " + title, text);
 		d.show();
 	}
 
     private class ProcessScrapping extends AsyncTask<BusJourney, Integer, Boolean> {
         private Exception exc;
 
         protected void onPreExecute() {
             dialog = new Dialog(context);
             dialog.setContentView(R.layout.progress);
             dialog.setTitle(getString(R.string.scrapping));
 
             statusProgressHttp = (TextView) dialog.findViewById(R.id.statusProgressHttp);
             progressHttp = (ProgressBar) dialog.findViewById(R.id.progressHttp);
             progressHttp.setMax(100);
 
             dialog.show();
         }
 
         protected Boolean doInBackground(BusJourney ... journey) {
             publishProgress(0, R.string.startHttpScrapping);
             try {
                 publishProgress(10, R.string.jsoupConnect);
                 journey[0].getBusJourneys();
                 publishProgress(100, R.string.jsoupDocReady);
                 return true;
             } catch (Exception e) {
                 this.exc = e;
                 e.printStackTrace();
                 return false;
             }
         }
 
         protected void onProgressUpdate(Integer ... progress) {
             progressHttp.setProgress(progress[0]);
             statusProgressHttp.setText(getString(progress[1]));
         }
 
         protected void onPostExecute(Boolean result) {
             if (!result) {
                 String excName = this.exc.getClass().getSimpleName();
                if (excName == "SocketTimeoutException") {
                    messageBox(getString(R.string.networkError));
                 }
 
                if (excName == "IOException") {
                 }
 
                if (excName == "ScrappingException") {
                     ScrappingException e = (ScrappingException)(this.exc);
                    messageBox(getString(R.string.scrappError) + ": " + e.getError());
                 }
            }
 
            dialog.dismiss();
         }
     }
 }
