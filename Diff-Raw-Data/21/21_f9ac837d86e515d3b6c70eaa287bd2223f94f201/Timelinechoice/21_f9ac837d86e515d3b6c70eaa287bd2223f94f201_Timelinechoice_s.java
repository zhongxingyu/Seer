 package com.maptime.maptime;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.support.v4.app.NavUtils;
 
 public class Timelinechoice extends Activity {
 
 	public final static String GEOPOINTS = "com.maptime.maptime.GEOPOINTS";
 	private final static String APIURL = "http://kanga-na8g09c.ecs.soton.ac.uk/api/fetchAll.php";
 	private ArrayList<Timeline> timelines = new ArrayList<Timeline>();
 	private int timelineChoice = -1;
 
     @SuppressLint({ "NewApi", "NewApi" }) //so it doesn't error on getActionBar()
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_timelinechoice);
         if (android.os.Build.VERSION.SDK_INT >= 11) { //11 = API 11 i.e. honeycomb
         	getActionBar().setDisplayHomeAsUpEnabled(true);
         }
         //TODO: get timeline list, populate list with timelines
         //somehow return a list of geopoints to the main activity when something is selected here
 		//TODO: the intent returning needs to be done somehow before onPause() so 
         //prob at after user selects a timeline from the list. Every time user selects timeline from list
         
         //new Thread(new TimelineRetriever(this)).start();
         		
         new TimelineRetrieverTask(this).execute();
     }
 
     
     private void retrieveTimelines() {
     	try {
 			readXML();
 		} catch (Exception e) {
 			handler .sendEmptyMessage(0);
 		}
 	}
     
     /*
      * Handles the caught error for fetching XML (can't put dialogs inside the actual catch(){} method without app breaking)
      */
     private Handler handler = new Handler() {
         public void handleMessage(Message message) {
         	String errorTitle = getResources().getString(R.string.error_title);
 			String errorMessage = getResources().getString(R.string.error_readXML);
 			
 			AlertDialog errDialog = new AlertDialog.Builder(Timelinechoice.this).create();
 			errDialog.setTitle(errorTitle);
 			errDialog.setMessage(errorMessage);
 			errDialog.show();
         }
     };
     
     /*
 	 * Read the XML file in a nice way
 	 */
 	private void readXML() throws ParserConfigurationException, SAXException, IOException {
 		SAXParserFactory factory = SAXParserFactory.newInstance();
 		SAXParser saxParser = factory.newSAXParser();
 		
 		DefaultHandler handler = new DefaultHandler() {
 			int noOfTimelines = 0;
 			boolean btime = false;
 			boolean bname = false;
 			boolean bdesc = false;
 			boolean bmonth = false;
 			boolean bday = false;
 			
 			String name;
 			Double time;
 			String desc;
 			int month;
 			int day;			
 			int timepointID;
 		 
 			public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
 				if (qName.equalsIgnoreCase("name")) { bname = true; }	 
 				if (qName.equalsIgnoreCase("description")) { bdesc = true; }	 
 				if (qName.equalsIgnoreCase("month")) { bmonth = true; }	 
 				if (qName.equalsIgnoreCase("day")) { bday = true; }
 				if (qName.equalsIgnoreCase("yearInBC")) { btime = true; }
 				
 				for (int i = 0; i < attributes.getLength(); i++) {
 					if (attributes.getQName(i).equalsIgnoreCase("timelineName")) {
 						timelines.add(noOfTimelines, new Timeline(attributes.getValue(i), noOfTimelines));
 						noOfTimelines++;
 					}
 					if (attributes.getQName(i).equalsIgnoreCase("timepointID")) {
 						timepointID = Integer.parseInt(attributes.getValue(i));
 					}
 				}
 			}
 			
 			public void endElement(String uri, String localName, String qName) throws SAXException {
 					if(qName.equalsIgnoreCase("timepoint")) {
 						timelines.get(noOfTimelines - 1).addTimePoint(time, timepointID, name, desc, month, day);
 					}
 			}
 		 
 			public void characters(char ch[], int start, int length) throws SAXException {
 				if (bname) {
 					name = new String(ch, start, length);
 					bname = false;
 				}
 				if (bdesc) {
 					desc = new String(ch, start, length);
 					bdesc = false;
 				}
 				if (bmonth) {
 					month = Integer.parseInt(new String(ch, start, length));
 					bmonth = false;
 				}
 				if (bday) {
 					day = Integer.parseInt(new String(ch, start, length));
 					bday = false;
 				}
 				if (btime) {
 					time = Double.parseDouble(new String(ch, start, length));
 					btime = false;
 				}
 		 
 			}
 			
 	    };
 		saxParser.parse(APIURL, handler);
 	}
     
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_timelinechoice, menu);
         return true;
     }
 
     
 	@Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
         	case R.id.menu_refresh:
         		new TimelineRetrieverTask(this).execute();
         		return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
 	private class TimelineRetrieverTask extends AsyncTask<Void, Void, Void> {
 
 		private Context context;
 		private ProgressDialog progressDialog;
 		
 		public TimelineRetrieverTask(Context ct) {
 			context = ct;
 		}
 		
 		protected void onPreExecute() {
 			String progressTitle = getString(R.string.progress_loading);
 			String progressMessage = getString(R.string.progress_fetchingTimlines);
 			progressDialog = ProgressDialog.show(context, progressTitle , progressMessage);
 		}
 		
 		protected Void doInBackground(Void... params) {
 			retrieveTimelines();
 			return null;
 		}
 		
 		protected void onPostExecute(Void result) {
 			progressDialog.dismiss();
 			String[] tlNames = new String[timelines.size()];
 	        for (int i = 0; i < timelines.size(); i++) {
 	        	tlNames[i] = timelines.get(i).getLineName();
	        } 
	        ArrayAdapter ad=new ArrayAdapter(context,android.R.layout.simple_list_item_1,tlNames);
	        final ListView list=(ListView)findViewById(R.id.tlcList);
 	        list.setAdapter(ad);
 	        list.setOnItemClickListener(new OnItemClickListener() {
 
 	        	public void onItemClick(AdapterView arg0, View arg1, int arg2, long arg3) {
	        		TextView txt=(TextView)findViewById(R.id.tlcTXT);
	        		txt.setText(list.getItemAtPosition(arg2).toString());
 	        		timelineChoice = arg2;
 	        		//the following is quick/dirty purely for making-work purposes
 	        		Intent result = new Intent();
 	        		result.putExtra("selectedTimeline", timelines.get(arg2));
 	        		setResult(RESULT_OK, result);
 	        		finish();
 	        	}
 	        });
 		}
 		
 	}
 	
 	
 	
 }
