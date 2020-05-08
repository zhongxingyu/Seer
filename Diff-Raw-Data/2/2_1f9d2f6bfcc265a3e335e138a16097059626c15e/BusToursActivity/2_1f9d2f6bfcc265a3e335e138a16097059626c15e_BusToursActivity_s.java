 /* vim: set ts=4 sw=4 et: */
 
 package org.gitorious.scrapfilbleu.android;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.app.Dialog;
 
 import android.content.Context;
 import android.content.DialogInterface;
 
 import android.util.Log;
 
 import android.os.Bundle;
 
 import android.view.View;
 
 import android.widget.Toast;
 import android.widget.Button;
 import android.widget.AutoCompleteTextView;
 import android.widget.Spinner;
 import android.widget.ArrayAdapter;
 import android.widget.SimpleAdapter;
 import android.widget.DatePicker;
 import android.widget.TimePicker;
 
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
     }
 
     public void bindWidgets()
     {
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
         try {
             Document doc = Jsoup.connect(this.urls.urlBase + "?id=1-1&raz").get();
             Log.e("BusTours", doc.title());
         } catch (IOException e) {
             e.printStackTrace();
         }
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
 }
