 package dk.bigherman.android.hello;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.ArrayList;
 
 import com.google.android.maps.*;
 
 import android.database.SQLException;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
import android.view.MotionEvent;

 import org.xmlpull.v1.*;
 
 public class MyMap extends MapActivity 
 {
 	private MapView mapView;
     public MyMap()
     {
     	
     	//this.sc.setGeoHelper(new GeoHelper());
     }
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         mapView = (MapView) findViewById(R.id.mapView);
         mapView.setBuiltInZoomControls(true); 
         mapView.displayZoomControls(true);
         
         try {
         	initMap();
         } catch (IOException ex) {
         	System.out.println("Map Init Error");
         } catch (XmlPullParserException ex) {
         	System.out.println("XML Parser Error");
         }
         
         System.out.println("All done");
     }
 
 	/**
 	 * Initialise the map and adds the zoomcontrols to the LinearLayout.
 	 */
 	private void initMap() throws XmlPullParserException, IOException
 	{
 		DataBaseHelper myDbHelper = new DataBaseHelper(this);
 		Log.i("airfields", "Start db load");
         try {
         	myDbHelper.createDataBase();
         	myDbHelper.openDataBase();
 	 	} catch (IOException ioe) {
 	 		throw new Error("Unable to create database");
 	 	} catch(SQLException sqle) {
 	 		throw sqle;
 	 	}
 	 
 	 	ArrayList<Airfield> airfields = myDbHelper.airfieldsInArea(140, 150, 175, 192);
         myDbHelper.close();
         
         Log.i("airfields", "End db load");
         
         List<Overlay> mapOverlays = mapView.getOverlays();
         Drawable drawable = this.getResources().getDrawable(R.drawable.icn_empty);
         HelloItemizedOverlay itemizedoverlay = new HelloItemizedOverlay(drawable, this);
 		
 		mapView = (MapView) findViewById(R.id.mapView);
 	    mapView.displayZoomControls(true);
 	    GeoPoint aarhus = new GeoPoint(56500000, 10300000);
 	    mapView.getController().setCenter(aarhus);
 	    mapView.getController().setZoom(8);
 	    
 	//    new PopulateMap(this, airfields).execute(itemizedoverlay);
 	    mapOverlays.add(itemizedoverlay);
 	    
         mapView.invalidate();
         Log.i("airfields","Map Invalidated");
 	    Log.i("airfields",String.valueOf(airfields.size()));
 	}
 
     @Override
     protected boolean isRouteDisplayed()
     {
     	return false;
     }
     
 
 }
