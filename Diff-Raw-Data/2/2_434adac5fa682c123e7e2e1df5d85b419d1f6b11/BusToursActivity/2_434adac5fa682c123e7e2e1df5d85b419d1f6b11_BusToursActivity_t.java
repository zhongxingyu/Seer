 /* vim: set ts=4 sw=4 et: */
 
 package org.gitorious.scrapfilbleu.android;
 
 import java.io.IOException;
 import java.net.SocketTimeoutException;
 import java.util.Map;
 import java.util.List;
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.text.DecimalFormat;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.app.Dialog;
 
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 
 import android.util.Log;
 
 import android.os.Bundle;
 import android.os.AsyncTask;
 
 import android.net.Uri;
 
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
 import android.widget.ExpandableListView;
 import android.widget.ExpandableListAdapter;
 import android.widget.SimpleExpandableListAdapter;
 import android.widget.ExpandableListView.OnChildClickListener;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.RadioButton;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 
 public class BusToursActivity extends Activity
 {
 	private static Context context;
     private DatePicker date;
     private TimePicker time;
     private AutoCompleteTextView txtStopDeparture;
     private Spinner sens;
     private AutoCompleteTextView txtStopArrival;
     private Spinner listCriteria;
     private ImageButton btnGetClosestStopDeparture;
     private ImageButton btnGetClosestStopArrival;
     private Button btnGetJourney;
     private RadioButton targetDeparture;
     private RadioButton targetArrival;
 
     private Dialog journeyList;
     private Dialog journeyDetails;
     private Dialog closestStops;
 
     private String[] journeyCriteriaValues;
     private String[] sensValues;
 
     private ArrayList<Journey> journeys;
     private int journeyDetailsProcessing;
 
     private URLs urls;
     private BusStops stops;
     private List<BusStops.BusStop> nearests;
     private LocationManager mLocManager;
     private String mLocProvider;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         this.context = this;
         this.date               = (DatePicker)findViewById(R.id.date);
         this.time               = (TimePicker)findViewById(R.id.time);
         this.txtStopDeparture   = (AutoCompleteTextView)findViewById(R.id.txtStopDeparture);
         // this.sens               = (Spinner)findViewById(R.id.Sens);
         this.targetDeparture    = (RadioButton)findViewById(R.id.radioButtonDeparture);
         this.targetArrival    = (RadioButton)findViewById(R.id.radioButtonArrival);
         this.txtStopArrival     = (AutoCompleteTextView)findViewById(R.id.txtStopArrival);
         this.listCriteria       = (Spinner)findViewById(R.id.listCriteria);
         this.btnGetClosestStopDeparture = (ImageButton)findViewById(R.id.btnGetClosestStopDeparture);
         this.btnGetClosestStopArrival = (ImageButton)findViewById(R.id.btnGetClosestStopArrival);
         this.btnGetJourney      = (Button)findViewById(R.id.btnGetJourney);
 
         this.journeyCriteriaValues  = getResources().getStringArray(R.array.journeyCriteriaValues);
         this.sensValues             = getResources().getStringArray(R.array.sensValues);
 
         this.stops = new BusStops();
 
         this.mLocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
         Criteria crit = new Criteria();
         crit.setAccuracy(Criteria.ACCURACY_COARSE);
         this.mLocProvider = this.mLocManager.getBestProvider(crit, true);
 
         this.fill();
         this.bindWidgets();
     }
     
     public void fill()
     {
         // fill journey criteria
         ArrayAdapter<CharSequence> criteriaAdapter = ArrayAdapter.createFromResource(this, R.array.journeyCriteria, android.R.layout.simple_spinner_item);
         criteriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         this.listCriteria.setAdapter(criteriaAdapter);
 
         /*
         // fill sens
         ArrayAdapter<CharSequence> sensAdapter = ArrayAdapter.createFromResource(this, R.array.sens, android.R.layout.simple_spinner_item);
         sensAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         this.sens.setAdapter(sensAdapter);
         */
 
         // fill stop autocomplete
         ArrayAdapter<String> stopAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, this.stops.getStops());
         this.txtStopDeparture.setAdapter(stopAdapter);
         this.txtStopArrival.setAdapter(stopAdapter);
     }
 
     public void bindWidgets()
     {
         this.txtStopDeparture.setThreshold(2);
         this.txtStopArrival.setThreshold(2);
         this.time.setIs24HourView(true);
         this.btnGetClosestStopDeparture.setOnClickListener(new View.OnClickListener() { public void onClick(View arg0) { onClick_btnGetClosestStopDeparture(); } });
         this.btnGetClosestStopArrival.setOnClickListener(new View.OnClickListener() { public void onClick(View arg0) { onClick_btnGetClosestStopArrival(); } });
         this.btnGetJourney.setOnClickListener(new View.OnClickListener() { public void onClick(View arg0) { onClick_btnGetJourney(); } });
     }
 
     public int getJourneyCriteriaValue()
     {
         return Integer.parseInt(this.journeyCriteriaValues[this.listCriteria.getSelectedItemPosition()]);
     }
 
     public int getSensValue()
     {
         // return Integer.parseInt(this.sensValues[this.sens.getSelectedItemPosition()]);
         int retval = 0;
 
         if (this.targetDeparture.isChecked()) retval = 1;
         if (this.targetArrival.isChecked()) retval = -1;
 
         return retval;
     }
 
     public void onClick_btnGetJourney()
     {
         String dep = this.txtStopDeparture.getEditableText().toString();
         String arr = this.txtStopArrival.getEditableText().toString();
 
         if (dep.length() < 1 || arr.length() < 1) {
             this.alertErrorBox(getString(R.string.missingValues), getString(R.string.descMissingValues));
             return;
         }
 
         String[] cityStopDep = this.stops.getStopCity(dep);
         String[] cityStopArr = this.stops.getStopCity(arr);
 
         BusJourney j = new BusJourney();
         j.setCityDep(cityStopDep[1]);
         j.setCityArr(cityStopArr[1]);
         j.setStopDep(cityStopDep[0]);
         j.setStopArr(cityStopArr[0]);
         j.setDate(new String()
             + String.valueOf(this.date.getDayOfMonth())
             + "/"
            + String.valueOf(this.date.getMonth() + 1)
             + "/"
             + String.valueOf(this.date.getYear())
         );
         j.setHour(String.valueOf(this.time.getCurrentHour()));
         j.setMinute(String.valueOf(this.time.getCurrentMinute()));
         j.setSens(String.valueOf(this.getSensValue()));
         j.setCriteria(String.valueOf(this.getJourneyCriteriaValue()));
         new ProcessScrapping().execute(j);
     }
 
     public void onClick_btnGetClosestStopDeparture() {
         this.buildClosestStopsUi("dep");
     }
 
     public void onClick_btnGetClosestStopArrival() {
         this.buildClosestStopsUi("arr");
     }
 
     public void setDepartureStopName(String name) {
         this.txtStopDeparture.setText(name);
         closestStops.dismiss();
     }
 
     public void setArrivalStopName(String name) {
         this.txtStopArrival.setText(name);
         closestStops.dismiss();
     }
 
     public List<BusStops.BusStop> getNearests() {
         return this.nearests;
     }
 
     public BusStops.BusStop getNearest(int pos) {
         return this.nearests.get(pos);
     }
 
     public Location getLastLocation() {
         return this.mLocManager.getLastKnownLocation(this.mLocProvider);
     }
 
     public void buildClosestStopsUi(String type) {
         closestStops = new Dialog(context);
         closestStops.setContentView(R.layout.closest);
         closestStops.setTitle(getString(R.string.closest_stops));
         Button btnWhereAmI = (Button)closestStops.findViewById(R.id.btnWhereAmI);
 
         btnWhereAmI.setOnClickListener(new View.OnClickListener() {
             public void onClick(View arg0) {
                 Location lastLoc = getLastLocation();
                 Intent intent = new Intent(Intent.ACTION_VIEW,
                     Uri.parse(
                         "geo:"
                         + lastLoc.getLatitude()
                         + ","
                         + lastLoc.getLongitude()
                         + "?q="
                         + lastLoc.getLatitude()
                         + ","
                         + lastLoc.getLongitude()
                         + "(" + getString(R.string.youAreHere) + ")"
                         )
                     );
                 startActivity(intent);
             }
         });
 
         ListView list = (ListView)closestStops.findViewById(R.id.listClosestStops);
         if (type.equals("dep")) {
             list.setOnItemClickListener(new OnItemClickListener() {
                 public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                     BusStops.BusStop bs = getNearest(position);
                     if (bs != null) {
                         setDepartureStopName(bs.name);
                     }
                 }
             });
         }
         if (type.equals("arr")) {
             list.setOnItemClickListener(new OnItemClickListener() {
                 public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                     BusStops.BusStop bs = getNearest(position);
                     if (bs != null) {
                         setArrivalStopName(bs.name);
                     }
                 }
             });
         }
         list.setAdapter(this.buildClosestStopsAdapter());
         list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 
         closestStops.show();
     }
 
     public ArrayAdapter<String> buildClosestStopsAdapter() {
         this.getClosestStops();
         DecimalFormat df = new DecimalFormat("###");
         List<String> listClosest = new ArrayList<String>();
 
         if (this.nearests != null) {
             Iterator itMinDist = this.nearests.iterator();
             while(itMinDist.hasNext()) {
                 BusStops.BusStop bs = (BusStops.BusStop)itMinDist.next();
                 listClosest.add(new String(bs.name + " (" + df.format(bs.dist) + "m)"));
             }
         }
 
         return new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listClosest);
     }
 
     public void getClosestStops() {
         List<BusStops.BusStop> nearests = null;
         Log.e("BusTours", "Using provider: " + this.mLocProvider);
         Location lastLoc = this.getLastLocation();
         if (lastLoc == null) {
             Log.e("BusTours", "No last known location");
         } else {
             Log.e("BusTours", "Current lastKnown (lat;lon)=(" + String.valueOf(lastLoc.getLatitude()) + ";" + String.valueOf(lastLoc.getLongitude()) + ")");
             this.nearests = this.stops.getNearestStop(lastLoc.getLatitude(), lastLoc.getLongitude());
             Iterator itMinDist = this.nearests.iterator();
             while(itMinDist.hasNext()) {
                 BusStops.BusStop bs = (BusStops.BusStop)itMinDist.next();
                 Log.e("BusTours", "Closest bus stop at (lat;lon)=(" + String.valueOf(bs.lat) + ";" + String.valueOf(bs.lon) + ") :: " + bs.name + " is " + bs.dist + "m");
             }
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
 
     public void setJourneys(ArrayList<Journey> js) {
         this.journeys = js;
     }
 
     public ArrayList<Journey> getJourneys() {
         return this.journeys;
     }
 
     public void setJourneyDetailsProcessing(int v) {
         this.journeyDetailsProcessing = v;
     }
 
     public void onAsyncTaskScrapJourneyDetailsComplete() {
         Log.e("BusTours", "Got details for " + this.journeyDetailsProcessing);
         Journey targetJourney = this.journeys.get(this.journeyDetailsProcessing);
         JourneyDetails details = targetJourney.getJourneyDetails();
 
         if (details == null) {
             Log.e("BusTours", "No details available for " + this.journeyDetailsProcessing);
             this.alertInfoBox(getString(R.string.noDetails), getString(R.string.noDetailsTxt));
             return;
         }
 
         ExpandableListView list;
         journeyDetails = new Dialog(this);
         journeyDetails.setContentView(R.layout.details);
         journeyDetails.setTitle(getString(R.string.journey_details));
 
         String[] fromGroup = new String[] { "head" };
         int[] toGroup = new int[] { android.R.id.text1 };
         String[] fromChild = new String[] { "head", "more" };
         int[] toChild = new int[] { android.R.id.text1, android.R.id.text2 };
 
         List<HashMap<String, String>> jList = new ArrayList<HashMap<String, String>>();
         List<List<HashMap<String, String>>> jListChild = new ArrayList<List<HashMap<String, String>>>();
 
         Iterator<JourneyDetails.JourneyPart> jit = details.getParts().iterator();
         while (jit.hasNext()) {
             JourneyDetails.JourneyPart jp = (JourneyDetails.JourneyPart)jit.next();
             JourneyDetails.Indication indic = jp.getIndic();
             HashMap<String, String> map = new HashMap<String, String>();
             List<HashMap<String, String>> children = new ArrayList<HashMap<String, String>>();
             HashMap<String, String> curChildMap = new HashMap<String, String>();
 
             if (jp.getType().equals("indication")) {
                 if (indic.getType().equals("mount")) {
                     map.put("head", jp.getTime() + ": " + getString(R.string.stopIndic) + " '" + indic.getStop() + "'");
                     curChildMap.put("head", getString(R.string.detailLine) + " " + indic.getLine());
                     curChildMap.put("more", getString(R.string.detailDirection) + " " + indic.getDirection());
                 }
 
                 if (indic.getType().equals("umount")) {
                     map.put("head", jp.getTime() + ": " + getString(R.string.stopIndic) + " '" + indic.getStop() + "'");
                     curChildMap.put("head", getString(R.string.detailUmount));
                     curChildMap.put("more", "");
                 }
 
                 if (indic.getType().equals("walk")) {
                     map.put("head", getString(R.string.walkIndic) + " '" + indic.getStop() + "'");
                     curChildMap.put("head", getString(R.string.detailWalkTo) + " " + indic.getDirection());
                     curChildMap.put("more", getString(R.string.detailWalkFrom) + " " + indic.getStop());
                 }
             }
 
             if (jp.getType().equals("connection")) {
                 map.put("head", getString(R.string.connectionInfo));
                 curChildMap.put("head", getString(R.string.connectionDuration) + " " + jp.getDuration());
                 curChildMap.put("more", "");
             }
 
             children.add(curChildMap);
             jListChild.add(children);
             jList.add(map);
         }
 
         list = (ExpandableListView)journeyDetails.findViewById(R.id.listJourneyDetails);
         ExpandableListAdapter journeyDetailsAdapter = new SimpleExpandableListAdapter(
             this,
             jList,
             android.R.layout.simple_expandable_list_item_2,
             fromGroup, toGroup,
             jListChild,
             android.R.layout.simple_expandable_list_item_2,
             fromChild, toChild
         );
 
         list.setAdapter(journeyDetailsAdapter);
 
         journeyDetails.show();
     }
 
     public void onAsyncTaskScrapJourneyListComplete() {
         if (this.journeys == null) {
             Log.e("BusTours", "No journey to display");
             return;
         }
 
         ExpandableListView list;
         journeyList = new Dialog(this);
         journeyList.setContentView(R.layout.journey);
         journeyList.setTitle(getString(R.string.journey_list));
 
         String[] fromGroup = new String[] { "head" };
         int[] toGroup = new int[] { android.R.id.text1 };
         String[] fromChild = new String[] { "head", "more" };
         int[] toChild = new int[] { android.R.id.text1, android.R.id.text2 };
 
         List<HashMap<String, String>> jList = new ArrayList<HashMap<String, String>>();
         List<List<HashMap<String, String>>> jListChild = new ArrayList<List<HashMap<String, String>>>();
 
         Iterator<Journey> jit = this.journeys.iterator();
         while (jit.hasNext()) {
             Journey j = (Journey)jit.next();
 
             HashMap<String, String> map = new HashMap<String, String>();
             map.put("head", j.getDepartureTime() + " - " + j.getArrivalTime() + " (" + j.getDuration() + ")");
 
             List<HashMap<String, String>> children = new ArrayList<HashMap<String, String>>();
 
             HashMap<String, String> curChildMap = new HashMap<String, String>();
             curChildMap.put("head", getString(R.string.duration) + " " + j.getDuration());
             curChildMap.put("more", getString(R.string.connections) + " " + j.getConnections());
             children.add(curChildMap);
 
             jListChild.add(children);
 
             jList.add(map);
         }
 
         list = (ExpandableListView)journeyList.findViewById(R.id.listJourneys);
         list.setOnChildClickListener(new OnChildClickListener() {
             public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                 try {
                     Journey journey = getJourneys().get(groupPosition);
                     setJourneyDetailsProcessing(groupPosition);
                     Log.e("BusTours", "groupPosition:" + String.valueOf(groupPosition));
                     Log.e("BusTours", "journey:" + journey);
                     if (journey.getJourneyDetails() == null) {
                         Log.e("BusTours", "No details for this journey, starting scrapping ...");
                         new ProcessScrapping().execute(journey);
                     } else {
                         Log.e("BusTours", "Details already available.");
                         onAsyncTaskScrapJourneyDetailsComplete();
                     }
                 } catch(Exception e) {
                     e.printStackTrace();
                 }
                 return false;
             }
         });
 
         ExpandableListAdapter journeyAdapter = new SimpleExpandableListAdapter(
             this,
             jList,
             android.R.layout.simple_expandable_list_item_2,
             fromGroup, toGroup,
             jListChild,
             android.R.layout.simple_expandable_list_item_2,
             fromChild, toChild
         );
 
         list.setAdapter(journeyAdapter);
 
         journeyList.show();
     }
 
     public class ProcessScrapping extends AsyncTask<Object, Integer, Boolean> {
         private Exception exc;
         private String processing;
 
         // Showing Async progress
         private Dialog dialog;
         private TextView statusProgressHttp;
         private ProgressBar progressHttp;
 
         public void progress(Integer ... progress) {
             this.publishProgress(progress);
         }
 
         protected void onPreExecute() {
             dialog = new Dialog(context);
             dialog.setContentView(R.layout.progress);
             dialog.setTitle(getString(R.string.scrapping));
 
             statusProgressHttp = (TextView) dialog.findViewById(R.id.statusProgressHttp);
             progressHttp = (ProgressBar) dialog.findViewById(R.id.progressHttp);
             progressHttp.setMax(100);
 
             dialog.show();
         }
 
         protected Boolean doInBackground(Object ... journey) {
             String className = journey[0].getClass().getSimpleName();
             publishProgress(0, R.string.startHttpScrapping);
 
             Log.e("BusTours", "Processing " + className);
             this.processing = className;
 
             try {
                 publishProgress(10, R.string.jsoupConnect);
                 if (className.equals("BusJourney")) {
                     BusJourney j = (BusJourney)journey[0];
                     setJourneys(j.getBusJourneys(this));
                 }
                 if (className.equals("Journey")) {
                     Journey j = (Journey)journey[0];
                     j.getDetails(this);
                 }
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
             dialog.dismiss();
 
             if (!result) {
                 String excName = this.exc.getClass().getSimpleName();
                 String msg = "";
 
                 Log.e("BusTours", "Got exception: " + excName);
 
                 if (excName.equals("SocketTimeoutException")) {
                     Log.e("BusTours", "Got SocketTimeoutException");
                     msg = getString(R.string.networkError);
                 }
 
                 if (excName.equals("IOException")) {
                     Log.e("BusTours", "Got IOException");
                 }
 
                 if (excName.equals("ScrappingException")) {
                     Log.e("BusTours", "Got ScrappingException");
                     ScrappingException e = (ScrappingException)(this.exc);
                     msg = getString(R.string.scrappError) + ": " + e.getError();
                 }
 
                 if (msg.length() != 0) {
                     Log.e("BusTours", "msg=" + msg);
                     alertErrorBox(excName, msg);
                 }
             }
 
             if (this.processing.equals("BusJourney")) {
                 onAsyncTaskScrapJourneyListComplete();
             }
 
             if (this.processing.equals("Journey")) {
                 onAsyncTaskScrapJourneyDetailsComplete();
             }
         }
     }
 }
