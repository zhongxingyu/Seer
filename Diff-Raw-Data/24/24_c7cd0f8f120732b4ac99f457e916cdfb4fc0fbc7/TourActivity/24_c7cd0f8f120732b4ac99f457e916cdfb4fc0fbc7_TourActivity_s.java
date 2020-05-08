 package edu.fiu.cs.seniorproject;
 
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 
 import edu.fiu.cs.seniorproject.utils.Logger;
 import edu.fiu.cs.seniorproject.utils.XMLParser;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.XmlResourceParser;
 
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 
 import android.widget.ListView;
 
 import android.widget.SimpleAdapter;
 
 import android.widget.AdapterView.OnItemClickListener;
 
 public class TourActivity extends Activity{
 	
 	private List<String> tours = null;	
 	private List<Hashtable<String, String>> mTourList = null;	
 	private XMLParser parser = null;
 	
 	XmlResourceParser stringXmlContent = null;
    
 	 private final OnItemClickListener mClickListener = new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				if ( mTourList != null && mTourList.size() > position ) {
 					Hashtable<String, String> map = mTourList.get(position);
 					
 					if ( map != null ) {
 						Intent intent = new Intent(TourActivity.this, TourActivityDetails.class);
 						intent.putExtra("tour", map.get("name"));
 						TourActivity.this.startActivity(intent);
 					}
 				}
 			}
 		};
 		
 	
 	 @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_tour);
         
         tours = new ArrayList<String>();  
         parser = new XMLParser();
    		stringXmlContent = parser.getXMLFromSRC(this,R.xml.tours);
    		tours = parser.getTourName(stringXmlContent);
    		this.showEventList(tours);       
         
     }// end onCreate
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_tour, menu);
         return true;
     }
 	
 	private List<Hashtable<String, String>> buildEventMap(List<String> tourList ) {
     	List<Hashtable<String, String>> fillMaps = new ArrayList<Hashtable<String, String>>(tourList.size());
 		
 		for(int i = 0; i<tourList.size(); i++)
 		{
 			Hashtable<String, String> entry = new Hashtable<String, String>();
 			
 			String tourname = tourList.get(i);
 			
 			if ( tourname != null ) {
 				entry.put("name", tourname);					
 			} else {
 				Logger.Warning("Tour name is null!!!!");
 			}
 			fillMaps.add(entry);
 		}
 		return fillMaps;
     }// buildEventMap
     
     private void showEventList( List<String> tourList ) {
     		if (this.mTourList == null ) {
 	    		
 	    		if (  tourList != null && tourList.size() > 0) {
 	    			
 		    		ListView lv = (ListView)findViewById(android.R.id.list);
 		    		if ( lv != null ) {
 		    			
 		    			// create the grid item mapping
 		    			String[] from = new String[] {"name"};
 						int[] to = new int[] { R.id.tour_name };
 	
 						this.mTourList = this.buildEventMap(tourList);
 						
 						SimpleAdapter adapter = new SimpleAdapter(this, this.mTourList, R.layout.tour_row, from, to);
 						lv.setAdapter(adapter);
 		    			lv.setVisibility(View.VISIBLE);
 		    			lv.setOnItemClickListener(mClickListener);
 		    		}
 		    		
 		    		// Hide progress bar
 			    	findViewById(android.R.id.progress).setVisibility(View.GONE);
 	    		}
 	    	} else {
 	    		ListView lv = (ListView)findViewById(android.R.id.list);
 	    		if ( lv != null && lv.getAdapter() != null ) {
 	    			List<Hashtable<String, String>> eventMap = this.buildEventMap(tourList);
 	        		if ( eventMap != null ) {
 	        			this.mTourList.addAll(eventMap);
 	        			((SimpleAdapter)lv.getAdapter()).notifyDataSetChanged();
 	        		}
 	    		}    		
 	    	}
     	
     }// end showEventList
 	
 }// end TourActivity
