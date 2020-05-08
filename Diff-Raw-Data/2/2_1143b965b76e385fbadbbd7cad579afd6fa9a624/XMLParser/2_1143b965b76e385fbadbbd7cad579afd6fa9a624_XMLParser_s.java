 package no.ntnu.idi.socialhitchhiking.map;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.util.Log;
 import android.util.Xml;
 
 import no.ntnu.idi.freerider.model.MapLocation;
 import no.ntnu.idi.socialhitchhiking.map.MapRoute;
 
 public class XMLParser 
 {
 	private static final String ns = null;
 	public XMLParser(){
 		
 	}
 	public MapRoute parse(InputStream is) throws XmlPullParserException, IOException{
 		try
 		{
 			XmlPullParser parser = Xml.newPullParser();
 			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
 			parser.setInput(is,null);
 			parser.nextTag();
 			Log.e("Reached","Parse");
 			return readFeed(parser);
 		}
 		finally
 		{
 			is.close();
 		}
 	}
 	private MapRoute readFeed(XmlPullParser parser) throws XmlPullParserException, IOException
 	{
 		Log.e("Reached","Feed");
 		MapRoute route = new MapRoute();
 		parser.require(XmlPullParser.START_TAG, ns, "DirectionsResponse");
 		while(parser.next() != XmlPullParser.END_TAG) {
 			if(parser.getEventType() != XmlPullParser.START_TAG) {
 				continue;
 			}
 			String name = parser.getName();
 			if(name.equals("route")) {
 				route = readRoute(parser);
 			}
 			else {
 				skip(parser);
 			}
 		}
 		Log.e("Reached","End Feed");
 		return route;
 	}
 	private MapRoute readRoute(XmlPullParser parser) throws XmlPullParserException, IOException {
 		MapRoute route = new MapRoute();
 		Log.e("Reached","Route");
 		parser.require(XmlPullParser.START_TAG, ns, "route");
 		while(parser.next() != XmlPullParser.END_TAG) {
 			if(parser.getEventType() != XmlPullParser.START_TAG) {
 				continue;
 			}
 			String name = parser.getName();
 			if(name.equals("leg")) {
 				route = readLeg(parser);
 			} else {
 				skip(parser);
 			}
 		}
 		Log.e("Reached","End Route");
 		return route;
 	}
 	private MapRoute readLeg(XmlPullParser parser) throws XmlPullParserException, IOException
 	{
 		Log.e("Reached","Leg");
 		MapRoute route = new MapRoute();
 		//ArrayList<MapLocation> mapPoints = new ArrayList<MapLocation>();
 		double duration = 0;
 		parser.require(XmlPullParser.START_TAG, ns, "leg");
 		//int i = 0;
 		while(parser.next() != XmlPullParser.END_TAG) {
 			if(parser.getEventType() != XmlPullParser.START_TAG) {
 				continue;
 			}
 			String name = parser.getName();
 			if(name.equals("step")) {
 				Log.e("Reached","Step0");
 				Step step = readStep(parser);
 				try
 				{
 					duration = duration + Double.parseDouble(step.minutesDuration.replace(" mins", "").replace(" min", ""));
 					Log.e("Duration", Double.toString(Double.parseDouble(step.minutesDuration.replace(" mins", "").replace(" min", ""))));
					//Log.e("Total duration",)
 				}
 				catch(Exception e)
 				{
 					Log.e("DurationException", e.getMessage());
 				}
 				//MapRoute tempRoute = new MapRoute();
 				route.addCoordinate(new MapLocation(Double.parseDouble(step.getStartLatitude()), Double.parseDouble(step.getStartLongitude())));
 				route.addCoordinate(new MapLocation(Double.parseDouble(step.getEndLatitude()), Double.parseDouble(step.getEndLongitude())));
 				route.addToDrivingThroughList(new MapLocation(Double.parseDouble(step.getEndLatitude()), Double.parseDouble(step.getEndLongitude())));
 				//route.setDistanceDescription(step.getDescription());
 				//route.setDistanceInKilometers(Double.parseDouble(step.getKmDistance()));
 				route.setDistanceInMinutes(duration);
 				//route.setName("Test"); //Use <summary>?
 				//route.addRouteAsPartOfThis(tempRoute, (i==0));
 				
 				
 				//i++;
 			} else {
 				skip(parser);
 			}
 		}
 		Log.e("Reached","End Leg");
 		return route;
 	}
 	private Step readStep(XmlPullParser parser) throws XmlPullParserException, IOException {
 		//Log.e("Reached","Step1");
 		Step step = new Step();
 		parser.require(XmlPullParser.START_TAG, ns, "step");
 		
 		while(parser.next() != XmlPullParser.END_TAG) {
 			//Log.e("Reached",parser.getName());
 			if(parser.getEventType() != XmlPullParser.START_TAG){
 				continue;
 			}
 			String name = parser.getName();
 			//Log.e("Type", name);
 			if(name.equals("start_location")) {
 				LowLocation ll = readStartLocation(parser);
 				step.setStartLatitude(ll.getLatitude());
 				step.setStartLongitude(ll.getLongitude());
 				
 			} else if(name.equals("end_location")) {
 				LowLocation ll = readEndLocation(parser);
 				step.setEndLatitude(ll.getLatitude());
 				step.setEndLongitude(ll.getLongitude());
 				
 			} else if(name.equals("polyline")) {
 				step.setMapPoints(readPolyline(parser));
 				
 			} else if(name.equals("duration")) {
 				step.setMinutesDuration(readDuration(parser));
 				
 			} else if(name.equals("html_instructions")) {
 				step.setDescription(readString(parser,"html_instructions"));
 			
 			} else if(name.equals("distance")) {
 				step.setKmDistance(readDistance(parser));
 			
 			} else {
 				skip(parser);
 			
 			}
 				
 		}
 		//Log.e("Step",step.getDescription());
 		Log.e("Reached","Step");
 		return step;
 	}
 	private LowLocation readEndLocation(XmlPullParser parser) throws XmlPullParserException, IOException {
 		LowLocation ll = new LowLocation();
 		parser.require(XmlPullParser.START_TAG, ns, "end_location");
 		while(parser.next() != XmlPullParser.END_TAG) {
 			if(parser.getEventType() != XmlPullParser.START_TAG) {
 				continue;
 			}
 			String name = parser.getName();
 			if(name.equals("lat")) {
 				ll.setLatitude(readString(parser,"lat"));
 			
 			} else if(name.equals("lng")) {
 				ll.setLongitude(readString(parser,"lng"));
 			
 			} else {
 				skip(parser);
 			}
 		}
 		return ll;
 	}
 	private LowLocation readStartLocation(XmlPullParser parser) throws XmlPullParserException, IOException {
 		LowLocation ll = new LowLocation();
 		parser.require(XmlPullParser.START_TAG, ns, "start_location");
 		while(parser.next() != XmlPullParser.END_TAG) {
 			if(parser.getEventType() != XmlPullParser.START_TAG) {
 				continue;
 			}
 			String name = parser.getName();
 			if(name.equals("lat")) {
 				ll.setLatitude(readString(parser,"lat"));
 			
 			} else if(name.equals("lng")) {
 				ll.setLongitude(readString(parser,"lng"));
 			
 			} else {
 				skip(parser);
 			
 			}
 		}
 		return ll;
 	}
 	private String readDistance(XmlPullParser parser) throws XmlPullParserException, IOException {
 		String ret = "";
 		parser.require(XmlPullParser.START_TAG, ns, "distance");
 		while(parser.next() != XmlPullParser.END_TAG) {
 			if(parser.getEventType() != XmlPullParser.START_TAG) {
 				continue;
 			}
 			String name = parser.getName();
 			if(name.equals("text")) {
 				ret = readString(parser,"text");
 			
 			} else {
 				skip(parser);
 			
 			}
 		}
 		return ret;
 	}
 	
 	private String readDuration(XmlPullParser parser) throws XmlPullParserException, IOException {
 		String ret = "";
 		parser.require(XmlPullParser.START_TAG, ns, "duration");
 		while(parser.next() != XmlPullParser.END_TAG) {
 			if(parser.getEventType() != XmlPullParser.START_TAG) {
 				continue;
 			}
 			String name = parser.getName();
 			if(name.equals("text")) {
 				ret = readString(parser,"text");
 			
 			} else {
 				skip(parser);
 			
 			}
 		}
 		return ret;
 	}
 	private String readPolyline(XmlPullParser parser) throws XmlPullParserException, IOException {
 		String ret = "";
 		parser.require(XmlPullParser.START_TAG, ns, "polyline");
 		while(parser.next() != XmlPullParser.END_TAG) {
 			if(parser.getEventType() != XmlPullParser.START_TAG) {
 				continue;
 			}
 			String name = parser.getName();
 			if(name.equals("points")) {
 				ret = readString(parser,"points");
 			
 			} else {
 				skip(parser);
 			
 			}
 		}
 		return ret;
 	}
 	private String readString(XmlPullParser parser, String name) throws XmlPullParserException, IOException {
 		
 		parser.require(XmlPullParser.START_TAG, ns, name);
 		String result = "";
 		if(parser.next() == XmlPullParser.TEXT) {
 			result = parser.getText();
 			parser.nextTag();
 		}
 		parser.require(XmlPullParser.END_TAG, ns, name);
 		//Log.e("Result",result);
 		return result;
 	}
 	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
     	if(parser.getEventType() != XmlPullParser.START_TAG) {
     		throw new IllegalStateException();
     	}
     	int depth = 1;
     	while(depth != 0) {
     		switch(parser.next()) {
     		case XmlPullParser.END_TAG:
     			depth--;
     			break;
     		case XmlPullParser.START_TAG:
     			depth++;
     			break;
     		}
     	}
     }
 
 }
