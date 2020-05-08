 package dk.bigherman.android.hello;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.database.SQLException;
 import android.os.AsyncTask;
 import android.util.Log;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.OverlayItem;
 
 public class PopulateMapArea extends AsyncTask<ArrayList<OverlayItem>, Void, ArrayList<OverlayItem>>
 {
 	private ProgressDialog dialog;
 	private Context myMap;
 	private MapArea mapArea; 
 	
 	public PopulateMapArea(Context myMap, MapArea mapArea)
 	{
 		this.myMap = myMap;
 		this.mapArea = mapArea;
 	}
 	
     @Override
     protected void onPreExecute()
     {
         dialog = new ProgressDialog(this.myMap);
         dialog.setTitle("Loading...");
         dialog.setMessage("Please wait...");
         dialog.setIndeterminate(true);
         dialog.show();
     }
 	
 	@Override
 	protected ArrayList<OverlayItem> doInBackground(ArrayList<OverlayItem>... args)
 	{
 		DataBaseHelper myDbHelper = new DataBaseHelper(myMap);
 		Log.i("airfields", "Start db load");
         try {
         	myDbHelper.createDataBase();
         	myDbHelper.openDataBase();
 	 	} catch (IOException ioe) {
 	 		throw new Error("Unable to create database");
 	 	} catch(SQLException sqle) {
 	 		throw sqle;
 	 	}
 	 
 	 	ArrayList<Airfield> airfields = myDbHelper.airfieldsInArea(
 	 			mapArea.getMinRow(),
 	 			mapArea.getMaxRow(),
 	 			mapArea.getMinCol(),
 	 			mapArea.getMaxCol()
 	 	);
         myDbHelper.close();
 		// db shit here
         
 		for (int i=0; i<airfields.size();i++) {
 			Log.i("airfields", "Next airfield call, ICAO=" + airfields.get(i).getIcaoCode());
 
 			int lat = (int)Math.round(airfields.get(i).getLat());
 			int lng = (int)Math.round(airfields.get(i).getLng());
 			String metar = "No metar available";
 			String stationName = airfields.get(i).getName();
 
			//System.out.println(airfields.get(i).getName());
			//System.out.println(airfields.get(i).getIcaoCode());

 			GeoPoint point = new GeoPoint(lat,lng);
 			OverlayItem overlayitem = new OverlayItem(point, stationName, metar);
 			//args[0].addOverlay(overlayitem);
 			args[0].add(overlayitem);
 		}
 
 		Log.i("PopulateMapArea", "Returning data");
 		return args[0];
 	}
 	
 	protected void onPostExecute(ArrayList<OverlayItem> result)
 	{
 		Log.i("PopulateMapArea", "onPostExecute, result size=" + result.size());
         this.dialog.dismiss();
     }
 }
