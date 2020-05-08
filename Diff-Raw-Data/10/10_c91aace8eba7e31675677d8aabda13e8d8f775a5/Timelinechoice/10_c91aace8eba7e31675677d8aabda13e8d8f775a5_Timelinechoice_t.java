 package com.maptime.maptime;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.OverlayItem;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.annotation.SuppressLint;
 import android.app.Activity;
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
 	private final static String APIURL = "http://jakob-aungiers.com/misc/example.xml";
 	private ArrayList<Timeline> timelines = new ArrayList<Timeline>();
 	private int timelineChoice = -1;
 	
     @SuppressLint({ "NewApi", "NewApi" }) //so it doesn't error on getActionBar()
 	@Override
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
 		} catch (Exception e) {}		
 	}
     
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
 		
 		public TimelineRetrieverTask(Context ct) {
 			context = ct;
 		}
 		
 		@Override
 		protected Void doInBackground(Void... params) {
 			// TODO Auto-generated method stub
 			retrieveTimelines();
 			return null;
 		}
 		
 		@Override
 		protected void onPostExecute(Void result) {
 			// TODO Auto-generated method stub
 			String[] tlNames = new String[timelines.size()];
 	        for (int i = 0; i < timelines.size(); i++) {
 	        	tlNames[i] = timelines.get(i).getLineName();
 	        } 
 	        ArrayAdapter ad=new ArrayAdapter(context,android.R.layout.simple_list_item_1,tlNames);
 	        final ListView list=(ListView)findViewById(R.id.tlcList);
 	        list.setAdapter(ad);
 	        list.setOnItemClickListener(new OnItemClickListener() {
 
 	        	public void onItemClick(AdapterView arg0, View arg1, int arg2, long arg3) {
 	        		// TODO Auto-generated method stub
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
