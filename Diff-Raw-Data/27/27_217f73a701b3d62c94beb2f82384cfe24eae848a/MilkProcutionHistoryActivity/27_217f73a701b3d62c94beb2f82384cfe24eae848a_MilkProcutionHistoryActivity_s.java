 package org.cgiar.ilri.mistro.farmer;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.telephony.TelephonyManager;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 import org.cgiar.ilri.mistro.farmer.backend.DataHandler;
 import org.cgiar.ilri.mistro.farmer.backend.Locale;
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
 
 public class MilkProcutionHistoryActivity extends SherlockActivity
 {
     private static final String TAG="MIlkProductionHistoryActivity";
     private TextView dateTV;
     private TextView cowNameTV;
     private TextView timeTV;
     private  TextView quantityTV;
     private List<String> productionHistoryIDs;
     private String noDataWarning;
     private DisplayMetrics metrics;
     private TableLayout productionHistoryTL;
     private String[] times;
     private String todayText;
     private String yesterdayText;
     private String loadingPleaseWait;
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_milk_production_history);
 
         productionHistoryIDs=new ArrayList<String>();
         metrics=new DisplayMetrics();
 
         dateTV=(TextView)findViewById(R.id.date_tv);
         cowNameTV=(TextView)findViewById(R.id.cow_name_tv);
         timeTV=(TextView)findViewById(R.id.time_tv);
         quantityTV=(TextView)findViewById(R.id.quantity_tv);
         productionHistoryTL=(TableLayout)findViewById(R.id.production_history_tl);
 
         initTextInViews();
         fetchProductionHistory();
     }
 
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.milk_procution_history, menu);
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
         dateTV.setText(Locale.getStringInLocale("date",this));
         cowNameTV.setText(Locale.getStringInLocale("cow",this));
         timeTV.setText(Locale.getStringInLocale("time",this));
         quantityTV.setText(Locale.getStringInLocale("quantity",this));
         noDataWarning=Locale.getStringInLocale("no_data_received",this);
         times=Locale.getArrayInLocale("milking_times",this);
         if(times == null) {
             times = new String[1];
             times[0] = "";
         }
         todayText=Locale.getStringInLocale("today",this);
         yesterdayText=Locale.getStringInLocale("yesterday",this);
         loadingPleaseWait=Locale.getStringInLocale("loading_please_wait",this);
     }
 
     private void fetchProductionHistory()
     {
         if(DataHandler.checkNetworkConnection(this,null))
         {
             TelephonyManager telephonyManager=(TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
             ProductionHistoryThread productionHistoryThread=new ProductionHistoryThread();
             if(productionHistoryIDs.size()==0)
             {
                 productionHistoryThread.execute(telephonyManager.getSimSerialNumber(),"-1");
             }
             else
             {
                 productionHistoryThread.execute(telephonyManager.getSimSerialNumber(),productionHistoryIDs.get(productionHistoryIDs.size()-1));
             }
         }
     }
 
     private class ProductionHistoryThread extends AsyncTask<String,Integer,String>
     {
         private ProgressDialog progressDialog;
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             progressDialog = ProgressDialog.show(MilkProcutionHistoryActivity.this, "",loadingPleaseWait, true);
         }
 
         @Override
         protected String doInBackground(String... params)
         {
             JSONObject jsonObject=new JSONObject();
             try
             {
                 jsonObject.put("simCardSN",params[0]);
                 jsonObject.put("fromID",params[1]);
                 String result=DataHandler.sendDataToServer(jsonObject.toString(),DataHandler.FARMER_FETCH_MILK_PRODUCTION_HISTORY_URL);
                 Log.d(TAG,"result gotten from server = "+result);
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
                 Toast.makeText(MilkProcutionHistoryActivity.this,"server error",Toast.LENGTH_LONG).show();
             }
             else if(result.equals(DataHandler.NO_DATA))
             {
                 Toast.makeText(MilkProcutionHistoryActivity.this,noDataWarning,Toast.LENGTH_LONG).show();
             }
             else
             {
                 try
                 {
                     JSONObject jsonObject=new JSONObject(result);
                     JSONArray historyArray=jsonObject.getJSONArray("history");
                     addHistoryTableRows(historyArray);
                 }
                 catch (JSONException e)
                 {
                     e.printStackTrace();
                 }
             }
         }
     }
 
     private TextView generateTextView(String text, int id,int tableRowHeight,int tableTextSize)
     {
         TextView textView=new TextView(this);
         TableRow.LayoutParams dateLP=new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,tableRowHeight);
         textView.setId(id);
         textView.setText(text);
         textView.setTextSize(tableTextSize);
         textView.setTextColor(getResources().getColor(R.color.text_input_color));
         textView.setGravity(Gravity.CENTER);
         textView.setLayoutParams(dateLP);
         return textView;
     }
 
     private View generateRowSeparator(int id,int tableRowHeight)
     {
         final View rowSeperator=new View(this);
         rowSeperator.setId(id);
         rowSeperator.setLayoutParams(new TableRow.LayoutParams(1,tableRowHeight));
         rowSeperator.setBackgroundColor(getResources().getColor(R.color.pressed_mistro));
         return rowSeperator;
     }
 
     private void addHistoryTableRows(JSONArray history)
     {
         for (int i=0;i<history.length();i++)
         {
             try
             {
                 JSONObject jsonObject=history.getJSONObject(i);
                 productionHistoryIDs.add(jsonObject.getString("id"));
                 final TableRow tableRow=new TableRow(this);
                 tableRow.setId(3424+Integer.parseInt(jsonObject.getString("id")));
                 int tableRowHeight=0;
                 int tableTextSideMargin=0;//4dp
                 int tableTextSize=0;//14dp
                 if(metrics.densityDpi==DisplayMetrics.DENSITY_XHIGH)
                 {
                     tableRowHeight=58;//initially 30
                     tableTextSideMargin=14;//initially 6
                     tableTextSize=16;//initially 21
                 }
                 else if(metrics.densityDpi==DisplayMetrics.DENSITY_HIGH)
                 {
                     tableRowHeight=44;//initially 30
                     tableTextSideMargin=14;//initially 6
                     tableTextSize=16;//initially 21
                 }
                 else if(metrics.densityDpi==DisplayMetrics.DENSITY_MEDIUM)
                 {
                     tableRowHeight=27;//initially 20
                     tableTextSideMargin=10;//initially 4
                     tableTextSize=15;//initially 14
                 }
                 else if(metrics.densityDpi==DisplayMetrics.DENSITY_LOW)
                 {
                     tableRowHeight=16;//initially 15
                     tableTextSideMargin=6;//initially 3
                     tableTextSize=15;//initially 11
                 }
                 else
                 {
                     tableRowHeight=58;//initially 30
                     tableTextSideMargin=14;//initially 6
                     tableTextSize=16;//initially 21
                 }
                 tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,tableRowHeight));
 
                 SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
                 Date milkingDate=dateFormat.parse(jsonObject.getString("date"));
                 Date today=new Date();
                 long dateDifference=(today.getTime()-milkingDate.getTime())/86400000;
 
                 Calendar calendar=new GregorianCalendar();
                 calendar.setTime(milkingDate);
                String dateText=String.valueOf(calendar.get(Calendar.DAY_OF_MONTH))+"/"+String.valueOf(calendar.get(Calendar.MONTH))+"/"+calendar.get(Calendar.YEAR);
                 if(dateDifference<1)
                 {
                     dateText=todayText;
                 }
                 else if(dateDifference>=1&&dateDifference<2)
                 {
                     dateText=yesterdayText;
                 }
 
                 final TextView date=generateTextView(dateText,3424+jsonObject.getInt("id")+332,tableRowHeight,tableTextSize);
                 tableRow.addView(date);
 
                 final View rowSeparator1=generateRowSeparator(3424+jsonObject.getInt("id")+3322,tableRowHeight);
                 tableRow.addView(rowSeparator1);
 
                 String[] milkingTimesInEN = Locale.getArrayInLocale("milking_times",this,Locale.LOCALE_ENGLISH);
                 String milkingTime = "";
                 for(int j = 0; j < milkingTimesInEN.length; j++) {
                     if(jsonObject.getString("time").equals(milkingTimesInEN[j])) {
                         if(milkingTimesInEN.length == times.length){
                             milkingTime = times[j];
                         }
                         break;
                     }
                 }
                 final  TextView time=generateTextView(milkingTime,3424+jsonObject.getInt("id")+554,tableRowHeight,tableTextSize);
                 tableRow.addView(time);
 
                 final View rowSeparator2=generateRowSeparator(3424+jsonObject.getInt("id")+3432,tableRowHeight);
                 tableRow.addView(rowSeparator2);
 
                 //TODO: check if name is null and use ear tag number
                 String nameText=jsonObject.getString("name");
                 if(nameText==null||nameText.equals(""))
                 {
                     nameText=jsonObject.getString("ear_tag_number");
                 }
                 final TextView name=generateTextView(nameText,3424+jsonObject.getInt("id")+432,tableRowHeight,tableTextSize);
                 tableRow.addView(name);
 
                 final View rowSeparator3=generateRowSeparator(3424+jsonObject.getInt("id")+3532,tableRowHeight);
                 tableRow.addView(rowSeparator3);
 
                 String quantityType = jsonObject.getString("quantity_type");
                 String quantityString = jsonObject.getString("quantity");
                 String[] quantityTypesInEN = Locale.getArrayInLocale("quantity_types",this, Locale.LOCALE_ENGLISH);
                 String[] quantityTypes = Locale.getArrayInLocale("quantity_types", this);
                 if(quantityTypes != null && quantityTypesInEN.length == quantityTypes.length) {
                     for(int j = 0; j < quantityTypesInEN.length; j++) {
                         if(quantityType.equals(quantityTypesInEN[j])) {
                             quantityString = quantityString +" ("+quantityTypes[j]+")";
                             break;
                         }
                     }
                 }
                 final TextView quantity=generateTextView(quantityString,3424+jsonObject.getInt("id")+564,tableRowHeight,tableTextSize);
                 tableRow.addView(quantity);
 
                 productionHistoryTL.addView(tableRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,tableRowHeight));
 
             }
             catch (JSONException e)
             {
                 e.printStackTrace();
             }
             catch (ParseException e)
             {
                 e.printStackTrace();
             }
         }
     }
 }
