 package com.ideoma.black_thorn;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 
 public class MainActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);		
 		
 		LatLng monterey = new LatLng(36.654244, -121.799272);
 		LatLng[] montereyArrayCoords;
 		
 		GoogleMap map;
 		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
 		map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
 		map.addMarker(new MarkerOptions()
         .position(monterey)
         .title("Hello world"));
 		map.setMyLocationEnabled(true);
 		map.animateCamera(CameraUpdateFactory.newLatLng(monterey));
 		/*
 		LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		LocationListener mlocListener = new MyLocationListener();
 		mlocManager.*/
 		
 		montereyArrayCoords = GetGpsCoordsFromResource(R.xml.csumb_gps_coordinates);
 		for(int i = 0; i < montereyArrayCoords.length; i++)
 		{
 			map.addMarker(new MarkerOptions().position(montereyArrayCoords[i]).title(i+" pos"));
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	public class MarqueeChangeTask extends AsyncTask {
 		long startTime = 0;
 		long nextTime = 2*(60*1000);
 		Calendar c;
 		@Override
 		protected Object doInBackground(Object... arg0) {
 			if(c.getTimeInMillis()-startTime >= nextTime)
 			{
 				RestartTime(c);
 				//Change text
 			}
 			return null;
 		}
 		
 		protected void onPreExecute()
 		{
 			c = Calendar.getInstance();
 			RestartTime(c);
 		}
 		
 		private void RestartTime(Calendar cal)
 		{
 			startTime = cal.getTimeInMillis();
 		}
 	}
 	
 	LatLng[] GetGpsCoordsFromResource(int res)
 	{
 		try {
 			ArrayList<LatLng> coords = new ArrayList<LatLng>();
 			InputStream in = getResources().openRawResource(res);
 			XMLParser parser = new XMLParser();
 		
 			Document doc = parser.ParseXMLToDoc(in);
 			NodeList nList = parser.ParseDocByTagName(doc, "lat_long_pts");
 			for(int i = 0; i < nList.getLength(); i++)
 			{
 				long lat = 0, lng = 0;
 				Element ele = (Element) nList.item(i);
 				lat = Long.parseLong(parser.GetTextValueByTagName(ele, "lat"));
				lng = Long.parseLong(parser.GetTextValueByTagName(ele, "lat"));
 				coords.add(new LatLng(lat,lng));
 			}
 			LatLng[] latlngarray = new LatLng[coords.size()];
 			coords.toArray(latlngarray);
 			return latlngarray;
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		}
 		return null;
 	} 
 	
 }
