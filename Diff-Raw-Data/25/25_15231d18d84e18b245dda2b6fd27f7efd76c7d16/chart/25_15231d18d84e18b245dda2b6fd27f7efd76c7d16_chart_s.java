 /*Copyright 2011 Grant Bowman
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
 
 package com.activehabits.android;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Stack;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.ActivityNotFoundException;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.chart.PointStyle;
 import org.achartengine.model.TimeSeries;
 import org.achartengine.model.XYMultipleSeriesDataset;
 import org.achartengine.renderer.XYMultipleSeriesRenderer;
 import org.achartengine.renderer.XYSeriesRenderer;
 
 public class chart extends Activity {
     private static final String TAG = "AH.chart";
     //private static final Integer MAXEVENTS = 50; // fixed max # events String[] of 50 for now // removed since arrays changed to ArrayList<string>
     private Integer DATASETS = 1;
 
     /* Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.chart);
 
         // not sure how to change some values from .../src/achart/doc/org/achartengine/chart/TimeChart.html
         // we created an Intent so the actual class is instantiated inside the other Activity
         Intent draw = ChartFactory.getTimeChartIntent(
                           getApplicationContext(),
                           getDataset(),
                           getRenderer(),
                           "EEE, MMM d");
         try { startActivity(draw); }
         catch (ActivityNotFoundException e) { e.printStackTrace(); }
         catch (NullPointerException e) { e.printStackTrace(); }
         Log.i(TAG, "chart drawn");
 
         // TODO: no options menu in a separate org.achartengine activity, currently uses a separate activity
 
         finish(); // finishes chart activity, leaves the new org.achartengine* activity
     }
 
     private XYMultipleSeriesDataset getDataset() {
         XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
         Integer l = 0; // lines of data, zero based
         ArrayList<String> eventName = new ArrayList<String>();
         ArrayList<String> eventSec = new ArrayList<String>();
         ArrayList<String> eventInput = new ArrayList<String>();
         ArrayList<String> eventHour = new ArrayList<String>();
         ArrayList<String> eventLoc = new ArrayList<String>();
         // assume data on SDcard exists and is good
         // assume no blank lines within data, x.length() > 0
         // assume no actions with line breaks in them
         // read data from SDcard
         try {
             final File root = Environment.getExternalStorageDirectory();
             final Resources res = getResources();
             final String sFileName = res.getString(R.string.log_event_filename);
             final File gpxfile = new File(root,sFileName);
             BufferedReader buf = new BufferedReader(new FileReader(gpxfile)); // appends
             String[] temp = new String[10];
             String x = buf.readLine();
             while ( x.length() > 0 ) {
                 Log.i(TAG, "buf.readline() " + x); // USEFUL
                 if ( ! x.startsWith("#") ) { // if not a comment
                     //Log.i(TAG, "chart read: " + x);
                     temp = x.split("[\t]", 5); // Max 5 strings split on tabs, perfect, sample line follows
                     // wake up	1294599870	input	11.06	Jan 9, 2011 11:04:30 AM	Location[mProvider=network,mTime=1294577632909,mLatitude=37.855569833333334,mLongitude=-122.1274694,mHasAltitude=false,mAltitude=0.0,mHasSpeed=false,mSpeed=0.0,mHasBearing=false,mBearing=0.0,mHasAccuracy=true,mAccuracy=130.0,mExtras=Bundle[mParcelledData.dataSize=148]]
                     //Log.i(TAG, "eventName " + temp[0]);
                     eventName.add(temp[0]);
                     //if (eventSec.get(-1) < temp[1])
                     //    Log.i(TAG, "good  eventSec " + temp[1]);
                     //else
                     // Log.i(TAG, "*BAD* eventSec " + temp[1]);
                     eventSec.add(temp[1]);
                     //Log.i(TAG, "eventHour " + temp[2]);
                    eventHour.add(temp[2]);
                     //Log.i(TAG, "eventInput " + temp[3]);
                    eventInput.add(temp[3]);
                     //Log.i(TAG, "eventMore " + temp[4]);
                     eventLoc.add(temp[4]);
                     l += 1; // only count lines of data
                 }
                 x = buf.readLine(); // next line
             }
         }
         catch (NullPointerException e) { // for arrays - normal end of the loop!
             //e.printStackTrace();
         } catch (FileNotFoundException e) { // for New File...
             e.printStackTrace();
         } catch (IOException e) { // for problems with reading buf
             e.printStackTrace();
         }
 
         // Got Data!
         // currently assumes data is in chronological order
         // X chart values are seconds (not milliseconds) from (int) eventSec[0] to eventSec[l]
         // X range varies with start and end times - TODO: add X range buffers
         // Y chart values are hours from eventHour[0] to eventHour[l]
         // Y range is 0 - 24 hours in the day TODO: chart from TOP to BOTTOM like a Calendar.
         Stack<String> eventList = new Stack<String>(); // eventList are unique values in eventName
         for (int k = 0; k < l; k++) {
             if ( ! eventList.contains(eventName.get(k)) ) {
                 eventList.add(eventName.get(k));
                 Log.i(TAG, "eventList add " + eventName.get(k));
             }
         }
 
         // long minXvalue = eventSec[0] * 1000; // Seconds to Milliseconds
         // Random r = new Random();
         //for (int i = 0; i < 1; i++)  // only one series until eventNames change
         DATASETS = eventList.size();
         for (int i = 0; i < DATASETS; i++) {
             String doit = (String) eventList.pop();
             TimeSeries series = new TimeSeries(doit); // eventName
                for (int k = 0; k < l; k++) {
                    if (doit.equals(eventName.get(k))) {
                        try {
                            series.add(new Date(Long.parseLong(eventSec.get(k))*1000), - Double.parseDouble(eventHour.get(k)));
                        }
                        catch (NumberFormatException e) {
                        Log.i(TAG, "exception " + e);
                    }
                     //Log.i(TAG, "plot point in " + doit);
                 }
             }
             //Log.i(TAG, "series" + series.toString());
             dataset.addSeries(series);
         }
         //Log.i(TAG, "ready to ship dataset");
         return dataset;
     }
 
     private XYMultipleSeriesRenderer getRenderer() {
         XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
         // prepare XYSeriesRenderer
         // ASSUMES no more than 7 DATASETS / sets of actions
         switch (DATASETS) { // must only setup and add a renderer *if* a data set exists
         case 7:
             XYSeriesRenderer w = new XYSeriesRenderer();
             w.setColor(Color.WHITE);
             w.setPointStyle(PointStyle.DIAMOND);
             w.setFillPoints(true);
             renderer.addSeriesRenderer(w);
         case 6:
             XYSeriesRenderer m = new XYSeriesRenderer();
             m.setColor(Color.MAGENTA);
             m.setPointStyle(PointStyle.DIAMOND);
             m.setFillPoints(true);
             renderer.addSeriesRenderer(m);
         case 5:
             XYSeriesRenderer c = new XYSeriesRenderer();
             c.setColor(Color.CYAN);
             c.setPointStyle(PointStyle.DIAMOND);
             c.setFillPoints(true);
             renderer.addSeriesRenderer(c);
         case 4:
             XYSeriesRenderer y = new XYSeriesRenderer();
             y.setColor(Color.YELLOW);
             y.setPointStyle(PointStyle.DIAMOND);
             y.setFillPoints(true);
             renderer.addSeriesRenderer(y);
         case 3:
             XYSeriesRenderer b = new XYSeriesRenderer();
             b.setColor(Color.BLUE);
             b.setPointStyle(PointStyle.DIAMOND);
             b.setFillPoints(true);
             renderer.addSeriesRenderer(b);
         case 2:
             XYSeriesRenderer r = new XYSeriesRenderer();
             r.setColor(Color.RED);
             r.setPointStyle(PointStyle.SQUARE);
             r.setFillPoints(true);
             renderer.addSeriesRenderer(r);
         case 1:
             XYSeriesRenderer g = new XYSeriesRenderer();
             g.setColor(Color.GREEN);
             g.setPointStyle(PointStyle.TRIANGLE);
             g.setLineWidth((float) 0.0); // tried to get rid of the lines
             g.setFillPoints(true);
             renderer.addSeriesRenderer(g);
         }
         // prepare renderer
         renderer.setLabelsColor(Color.LTGRAY);
         renderer.setLabelsTextSize(10);
         renderer.setAxesColor(Color.DKGRAY);
         renderer.setXLabels(3); // TODO: setXLabels suck, need to addTextLabels at specific points manually
         renderer.setYLabels(3);
         renderer.setXAxisMin(renderer.getXAxisMin() - 86400000);
         renderer.setXAxisMax(renderer.getXAxisMax() + 86400000);
         renderer.setYAxisMax(0.0);
         renderer.setYAxisMin(-24.0);
         //renderer.setDateFormat(); //available on class TimeChart, see the TimeChart Intent we use
         renderer.setLegendTextSize(14);
         renderer.setDisplayChartValues(false); // text on plotted values
         renderer.setShowGrid(true);
         //Log.i(TAG, "label text name " + renderer.getTextTypefaceName());
         //Log.i(TAG, "label text style " + renderer.getTextTypefaceStyle());
         renderer.setChartValuesTextSize(18);
         //Log.i(TAG, "renderer " + renderer.toString());
         return renderer;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {  // function not used, currently uses a separate activity
         MenuInflater inflater = getMenuInflater();
         menu.removeItem(R.id.optionchart); // we are in chart so disable chart menu item
         inflater.inflate(R.menu.habit_menu, menu);
         return true;
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {  // function not used, currently uses a separate activity
         super.onPrepareOptionsMenu(menu);
         menu.removeItem(R.id.optionchart);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {  // function not used, currently uses a separate activity
         // Handle item selection
         switch (item.getItemId()) {
         case R.id.optionmark:
             Intent myChartIntent = new Intent(this, mark.class);
             startActivity(myChartIntent);
             return true;
 //      case R.id.chart: // item removed
 //          return true;
 //      case R.id.social:
 //          return true;
 //      case R.id.prefs:
 //          Intent myPrefIntent = new Intent(this,prefs.class);
 //          startActivity(myPrefIntent);
 //          return true;
         case R.id.optionabout:
             Intent myAboutIntent = new Intent(this,about.class);
             startActivity(myAboutIntent);
             return true;
 //        case R.id.quit: {
 //            finish();
 //            return true;
 //        }
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
 //  @Override
 //  public void onResume() {  // function not used, currently uses a separate activity
 //      super.onResume();
 //      //use setPreferences(int)?
 //      //showDialog(R.id.dialog_choose_chart);
 //  }
 
     @Override
     protected Dialog onCreateDialog(int id) {  // function not used, currently uses a separate activity
         switch(id) {
             case R.id.dialog_choose_chart:
                 // Respond to anything from this dialog by drawing right now.
                 CharSequence[] items = new CharSequence[] {"all"}; // now predifined
                     // will populate with habit names
                 return new AlertDialog.Builder(chart.this)
                     .setTitle( R.string.chart )
                     .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int whichButton) {
                             finish();
                         }
                     }
                     ).setItems(items, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int whichButton) {
                             Intent draw = ChartFactory.getTimeChartIntent(
                                           getApplicationContext(),
                                           getDataset(),
                                           getRenderer(),
                                           null);
                             try {
                                 startActivity(draw);
                             }
                             catch (ActivityNotFoundException e) {
                                 e.printStackTrace();
                             }
                             Log.i(TAG, "drawn chart");
                         }
                     }
                 ).create();
             default:
                 return null;
         }
     }
 };
