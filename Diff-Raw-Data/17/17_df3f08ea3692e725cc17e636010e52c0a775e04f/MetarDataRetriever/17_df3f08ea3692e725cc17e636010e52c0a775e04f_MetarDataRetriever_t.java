 /******************************************************************************
  * Copyright 2011 Keith Mendoza
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package info.homepluspower.nearbymetars;
 
 import info.homepluspower.nearbymetars.MetarItem.SkyConds;
 
 import java.io.IOException;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.location.Location;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.view.View;
import android.widget.Toast;
 
 /**
  * @author mendozak
  *
  */
 public class MetarDataRetriever extends AsyncTask<Object, Void, Void> implements DialogInterface.OnCancelListener {
 	
 	private class MetarParser extends DefaultHandler {
 		private String text;
 		private MetarList list;
 		
 		private MetarItem.SkyConds skyCond;
 		private double latitude, longitude;
 		private String rawMetar, location;
 		
 		public MetarParser(MetarList list) {
 			text = new String();
 			this.list = list;
 		}
 		
 		@Override
 		public void startDocument() {
 			text="";
 		}
 		
 		@Override
 		public void startElement(String URI, String localName, String qName, Attributes attributes) throws SAXException {
 			if(isCancelled()) {
 				Log.d("NearbyMetars", "Parsing cancel detected at startElement");
 				throw new SAXException("parsing cancelled");
 			}
 			
 			Log.v("NearbyMetars", "Start element: " + localName);
 			if(localName.equals("sky_condition")) {
 				String cover = attributes.getValue("sky_cover");
 				SkyConds tmpSkyCond = SkyConds.valueOf(cover);
 				if(skyCond == null || tmpSkyCond.ordinal() > skyCond.ordinal())
 					skyCond = tmpSkyCond;
 			}
 		}
 		
 		@Override
 		public void endElement(String URI, String localName, String qName) throws SAXException {
 			if(isCancelled()) {
 				Log.d("NearbyMetars", "Parsing cancel detected at endElement");
 				throw new SAXException("parsing cancelled");
 			}
 			
 			Log.v("NearbyMetars", "End element: " + localName);
 			
 			if(localName.equals("METAR")) {
 				if(skyCond == null) {
 					Log.e("NearbyMetars", "Sky condition for " + location +" not known. Skipping");
 					return;
 				}
 				
 				Log.d("NearbyMetars", "Inserting MetarItem\nLocation: " + location + "\nSky condition: " + skyCond.toString());
 				MetarItem metarItem = new MetarItem(MetarItem.coordsToGeoPoint(latitude, longitude), location, rawMetar, skyCond);
 				list.addOverlay(metarItem);
 				skyCond = null;
 			}
 			else if(localName.equals("raw_text"))
 				rawMetar = text;
 			else if(localName.equals("station_id"))
 				location = text;
 			else if(localName.equals("latitude"))
 				latitude = Double.valueOf(text);
 			else if(localName.equals("longitude"))
 				longitude = Double.valueOf(text);
 			
 			text="";
 		}
 		
 		@Override
 		public void characters(char[] ch, int start, int length) {
 			String tmp = new String(ch);
 			Log.v("NearbyMetars", "characters: >>>" + tmp + "<<< start: " + Integer.toString(start) + " length: " + Integer.toString(length));
 			Log.v("NearbyMetars", "Substring to append: " + tmp.substring(start, start+length));
 			text = text + tmp.substring(start, start+length).trim();
 		}
 	}
 
 	private Context callerContext;
 	private ProgressDialog dialog;
 	private View view;
 	
 	public MetarDataRetriever(Context callerContext, View view) {
 		this.callerContext = callerContext;
 		this.view = view;
 	}
 	
 	@Override
 	protected void onPreExecute() {
 		Log.d("NearbyMetars", "Show progress dialog");
 		dialog = ProgressDialog.show(callerContext, "", "Getting METAR data, standby", true, true, this);
 	}
 	
	private void showToastMsg(String msg) {
		Toast toast = Toast.makeText(callerContext, msg, Toast.LENGTH_SHORT);
		toast.show();
	}
	
 	@Override
 	protected Void doInBackground(Object... params) {
 		MetarList list = (MetarList)params[0];
 		Location location = (Location)params[1];
 		
 		String url = "http://weather.aero/dataserver_current/httpparam?dataSource=metars&requestType=retrieve&format=xml&hoursBeforeNow=1&mostRecentForEachStation=true&radialDistance=50;" + Double.toString(location.getLongitude()) + "," + Double.toString(location.getLatitude());
 		list.reset();
 		Log.d("NearbyMetars", "URL to use: " + url);
 		try {
 			SAXParserFactory factory = SAXParserFactory.newInstance();
 			SAXParser parser = factory.newSAXParser();
 			
 			parser.parse(url, new MetarParser(list));
 		} catch(SAXException e) {
 			if(isCancelled())
 				Log.d("NearbyMetars", "Cancelling parsing");
			else {
 				Log.e("NearbyMetars", "Parsing exception: " + e.getMessage());
				showToastMsg("Parsing exception: " + e.getMessage());
			}	
 		} catch(IOException e) {
 			Log.e("NearbyMetars", "Failed to retrieve data");
			showToastMsg("Failed to retrieve METAR data, try again later");
 		} catch(ParserConfigurationException e) {
 			Log.e("NearbyMetars", "No matching SAX parser");
			showToastMsg("No matching SAX parser");
 		}
 		return null;
 	}
 	
 	@Override
 	protected void onPostExecute (Void result) {
 		Log.d("NearbyMetars", "Close progress dialog");
 		dialog.dismiss();
 		view.postInvalidate();
 	}
 	
 	@Override
 	protected void onCancelled() {
 		dialog.dismiss();
 	}
 
 	public void onCancel(DialogInterface dialog) {
 		this.cancel(true);		
 	}
 }
