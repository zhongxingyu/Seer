 package com.web.minimalistsweather;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import org.w3c.dom.*;
 
 import android.location.*;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Color;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.LinearLayout;
 
 public class MainWeather extends Activity implements LocationListener{
 	LocationManager lm;
 	java.lang.Double longitude;
 	java.lang.Double latitude;
 	
 	String sFileName = "lastForcast.txt";
 	
 	android.widget.ScrollView s;
 	android.widget.TextView t;
 	
 	FileOutputStream fo = null;
 	FileInputStream fi = null;
 	
 	static boolean LocationAvailable = false;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 		
 		this.s = new android.widget.ScrollView(this);
 		this.s.setVerticalScrollBarEnabled(true);
 		this.s.setHorizontalScrollBarEnabled(false);
 		this.s.setBackgroundColor(Color.BLACK);
 		this.s.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
 		this.t = new android.widget.TextView(this.getBaseContext());
 		this.t.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
 		this.t.setBackgroundColor(Color.BLACK);
 		this.t.setTextColor(Color.WHITE);
 		
 		this.t.setText("Getting location and weather info.");
 		
 		this.s.addView(this.t);
 		setContentView(this.s);
 		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000*60*30, 10000, this);		
 	}
 	
 	private boolean isFileExists(){
 		File f = this.getFileStreamPath(sFileName);		
 		if(f.exists()) return true;
 		return false;
 	}
 	
 	private void readFile() throws Exception{
 		fi = this.openFileInput(sFileName);
 		byte[] fileData = new byte[4096*2];
 		int length = fi.read(fileData);
 		if(length > 0){
 			String sForcast = new String(fileData).substring(0, length);
 			this.t.setText(sForcast);
 		}
 		fi.close();
 		fi = null;
 	}
 	
 	private void writeFile(java.lang.StringBuilder sb) throws Exception{
 		fo = this.openFileOutput(sFileName, 0);
 		fo.write(sb.toString().getBytes());
 		fo.close();
 		fo = null;
 	}
 	
 	private void deleteFile(){
 		File f = this.getFileStreamPath(sFileName);
 		f.delete();
 	}
 	
 	private void getWeather(){
 		try{
 			javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
 			javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
 			if(this.longitude != null && this.latitude != null)
 			{
 				Document xmlDoc = builder.parse("http://forecast.weather.gov/MapClick.php?lat="+latitude.toString()+"&lon="+longitude.toString()+"&unit=0&lg=english&FcstType=dwml");
 				//Document xmlDoc = builder.parse("http://forecast.weather.gov/MapClick.php?lat=40.66716&lon=-79.13227440000003&unit=0&lg=english&FcstType=dwml");
 				java.lang.StringBuilder sb = new java.lang.StringBuilder();
 				xmlDoc.getDocumentElement().normalize();
 				
 				NodeList createdDate = xmlDoc.getElementsByTagName("creation-date");
 				String[] sDate = createdDate.item(0).getTextContent().split("T");
 				String[] sTime = sDate[1].split("-");
 				sb.append("Forcast Date: " + sDate[0] + " Time: " + sTime[0] + "\n");
 				NodeList cities = xmlDoc.getElementsByTagName("city");
 				sb.append("City Name:   " + cities.item(0).getTextContent() + "\n\n");
 				
 				NodeList temperatures = xmlDoc.getElementsByTagName("temperature");
 				for(int i=0; i<temperatures.getLength(); i++){
 					if(((Element)temperatures.item(i)).getAttribute("type").compareTo("apparent") == 0){
 						sb.append("Current Temperature: " + ((Element)temperatures.item(i)).getTextContent() + "\n\n");
 					}
 				}
 				
 				Element forcast = (Element)(xmlDoc.getElementsByTagName("wordedForecast").item(0));
 				java.lang.String type = forcast.getAttribute("time-layout");
 				
 				NodeList forcasts = forcast.getElementsByTagName("text");
 				NodeList TimeLayouts = xmlDoc.getElementsByTagName("time-layout");
 				NodeList TimeLayoutNames = null;
 				
 				for(int i=0; i<TimeLayouts.getLength(); i++){
 					Element e = (Element) (((Element) (TimeLayouts.item(i))).getElementsByTagName("layout-key").item(0));
 					if(e.getTextContent().compareToIgnoreCase(type) == 0){
 						TimeLayoutNames = ((Element)TimeLayouts.item(i)).getElementsByTagName("start-valid-time");
 					}
 				}
 				
 				for(int i=0; i<forcasts.getLength(); i++){
 					sb.append(((Element)(TimeLayoutNames.item(i))).getAttribute("period-name") + ":   " + forcasts.item(i).getTextContent() + "\n\n");
 				}
 				
 				if(this.isFileExists())this.deleteFile();
 				this.writeFile(sb);
 				this.readFile();
 				
 			}else{
 				this.t.setText("No Latitude and Longitude information");
 			}
 		} catch(Exception ex){
 			this.t.setText(ex.getMessage());
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		menu.add("Refresh");
 		menu.add("Exit");
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if(item.getTitle().toString().compareTo("Exit") == 0){
 			this.finish();			
 		}
 		if(item.getTitle().toString().compareTo("Refresh") == 0){
 			try{
 				this.getWeather();
 			}catch(Exception ex){
 				this.t.setText(ex.toString());
 			}
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		this.latitude = location.getLatitude();
 		this.longitude = location.getLongitude();
 		if(!LocationAvailable) this.getWeather();
		else{
			LocationAvailable = true;
			try{
				if(this.isFileExists()){
					this.readFile();
				}else{
					this.getWeather();
				}
			}catch(Exception ex){
				this.t.setText(ex.getMessage());
			}
		}
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		
 	}
 }
