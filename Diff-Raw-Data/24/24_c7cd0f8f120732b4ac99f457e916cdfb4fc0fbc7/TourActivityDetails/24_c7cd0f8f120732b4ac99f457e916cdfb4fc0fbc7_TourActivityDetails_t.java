 package edu.fiu.cs.seniorproject;
 import java.util.ArrayList;
 import java.util.List;
 import com.google.android.maps.MapActivity;
 import edu.fiu.cs.seniorproject.data.Place;
 import edu.fiu.cs.seniorproject.manager.AppLocationManager;
 import edu.fiu.cs.seniorproject.utils.Logger;
 import edu.fiu.cs.seniorproject.utils.XMLParser;
 import android.os.Bundle;
 import android.content.Intent;
 import android.content.res.XmlResourceParser;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 
 public class TourActivityDetails extends MapActivity {
 	
 	private List<Place> tours = null;		
 	private XMLParser parser = null;	
	private XmlResourceParser stringXmlContent = null;	
 	private int cont = 1;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_tour_activity_details);
         
         
         AppLocationManager.init(this);
         
         Intent intent = getIntent();
         
         if ( intent != null && intent.hasExtra("tour")) {
         	String tourName = intent.getStringExtra("tour");         	
         	tours = new ArrayList<Place>();  
             parser = new XMLParser();
        		stringXmlContent = parser.getXMLFromSRC(this,R.xml.tours);
        		tours = parser.getTourByName(stringXmlContent, tourName);   
        		
        		//show the first
        		this.showPlace(this.tours.get(0));
        		
        		Logger.Debug("list size" + tours.size());
         	
         } 
         
     }// end onCreate
     
     public void showPlace(Place place) {
 		
 		if ( place != null ) {			
 			//create place name.
 			TextView name = (TextView)findViewById(R.id.tour_name);
 			if ( name != null ) {
 				name.setText(place.getName());
 			}
 			
 			TextView telephone = (TextView)findViewById(R.id.tour_telephone);
 			if ( telephone != null ) {
 				telephone.setText(place.getTelephone());
 			}
 			
 			//create place name.
 			TextView category = (TextView)findViewById(R.id.tour_category);
 			if ( category != null ) {
 				category.setText(place.getCategory());
 			}
 			
 			//create PLACE description.
 			TextView address = (TextView)findViewById(R.id.tour_address);
 			if ( address != null ) {
 				address.setText(place.getLocation().getAddress());
 			}			
 			
 		}
 		
 		
 	}// end showPlace
     
     public void  onPreviusButtonClick(View view) {
     	
     	if (this.cont >= 0)
     	{
     		showPlace(this.tours.get(this.cont));
     		this.cont --;
     	}
     	else
     	{
     		this.cont ++;
     		this.showPlace(this.tours.get(this.cont));
     	}
     }// onPreviusButtonClick
     
     public void  onNextButtonClick(View view) {
     	
     	if (this.cont < this.tours.size())
     	{
     		showPlace(this.tours.get(this.cont));
     		this.cont ++;
     	}
     	else
     	{
     		this.cont --;
     		this.showPlace(this.tours.get(this.cont));
     	}
     	
     }// end onNextButtonClick
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_tour_activity_details, menu);
         return true;
     }
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		 tours = null;		
		 parser = null;
		 stringXmlContent = null;
	}
 }
