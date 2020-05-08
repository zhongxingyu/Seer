 package com.af.mbtwhere;
 
 import java.io.IOException;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.widget.RadioGroup;
 
 import java.util.HashMap;
 import com.af.mbtwhere.LineLayout;
 import com.af.mbtwhere.Route.Builder;
 
 public class TrainFind extends Activity {
 	private static final String TAG = "MBTWhere";
 	private final HashMap<String, String> feeds = new HashMap<String, String>();
 	private final HashMap<String, Line> lines = new HashMap<String, Line>();
 	private RadioGroup lineGroup;
 	private HorizontalPager pager;
 	
     @Override
     public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
         setContentView(R.layout.main);
         
         feeds.put("red", "http://developer.mbta.com/Data/red.json");
         feeds.put("orange", "http://developer.mbta.com/Data/orange.json");
         feeds.put("blue", "http://developer.mbta.com/Data/blue.json");
         
         XmlPullParser xpp = getResources().getXml(R.xml.stations);
         try {
         	parseLines(xpp);
         } catch(XmlPullParserException e) {
         	Log.v(TAG, "parsing exception" + e);
         } catch(IOException e) {
         	Log.v(TAG, "io exception" + e);
         }
         lineGroup = (RadioGroup) findViewById(R.id.line_tabs);
         lineGroup.setOnCheckedChangeListener(onCheckedChangedListener);
         pager = (HorizontalPager)findViewById(R.id.lines);
         pager.setOnScreenSwitchListener(onScreenSwitchListener);
         LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         int i = 0;
        String[] lineOrder = {"red","orange","blue"};
        for(String sline : lineOrder) {
        	Line line = lines.get(sline);
        	if(line != null) {
	        	LineLayout linePanel = new LineLayout(this, feeds.get(line.name), line);
	        	inflater.inflate(R.layout.picker, linePanel);
	        	pager.addView(linePanel);
	        	linePanel.setup();
	        	i++;
        	}
         }
     }
     
     private final HorizontalPager.OnScreenSwitchListener onScreenSwitchListener =
         new HorizontalPager.OnScreenSwitchListener() {
             public void onScreenSwitched(final int screen) {
             	switch (screen) {
                 case 0:
                     lineGroup.check(R.id.radio_red);
                     break;
                 case 1:
                     lineGroup.check(R.id.radio_orange);
                     break;
                 case 2:
                     lineGroup.check(R.id.radio_blue);
                     break;
                 default:
                     break;
             	}
             }
     };
         
     private final RadioGroup.OnCheckedChangeListener onCheckedChangedListener =
     	new RadioGroup.OnCheckedChangeListener() {
     		public void onCheckedChanged(final RadioGroup group, final int checkedId) {
     			// Slide to the appropriate screen when the user checks a button.
     			switch (checkedId) {
     				case R.id.radio_red:
     					pager.setCurrentScreen(0, true);
     					break;
                     case R.id.radio_orange:
                     	pager.setCurrentScreen(1, true);
                     	break;
                     case R.id.radio_blue:
                     	pager.setCurrentScreen(2, true);
                     	break;
                     default:
                     	break;
     			}
     		}
     };
     
     private void parseLines(XmlPullParser parser) throws XmlPullParserException, IOException {
     	int eventType = parser.getEventType();
     	Line line = null;
     	while (eventType != XmlPullParser.END_DOCUMENT) {
     		String tag = "";
     		if(eventType == XmlPullParser.START_TAG) {
     			tag = parser.getName();
     			if("line".equals(tag)) {
     				line = new Line(parser.getAttributeValue(0));
     				lines.put(line.name, line);
     			} else if("station".equals(tag)) {
     				if(line != null ) {
     					line.addStation(parseStation(parser));
     				} else {
     					throw new XmlPullParserException("Data Format Exception: Station tag precedes line tag");
     				}
     			}
             }
     		eventType = parser.next();
     	}
     }
     
     private Station parseStation(XmlPullParser parser) throws XmlPullParserException, IOException {
     	Station station = new Station(parser.getAttributeValue(0), parser.getAttributeValue(1), parser.getAttributeValue(2), parser.getAttributeValue(3));
 		int eventType = parser.next();
 		String tag = "";
     	while(true) { 
     		if(eventType == XmlPullParser.START_TAG) {
     			tag = parser.getName();
     			if("outbound".equals(tag) || "inbound".equals(tag)) {
     				Builder b  = new Route.Builder();
     				b.code(parser.getAttributeValue(0)).prevCode(parser.getAttributeValue(1)).nextCode(parser.getAttributeValue(2)).station(station);
     				try {
     					b.flag(parser.getAttributeValue(3));
     				} catch(Exception e) {
     					b.flag(Line.ANY_BRANCH);
     				}
     				Route r = b.build();
     				if("inbound".equals(tag)) {
     					station.addInbound(r);
     				} else {
     					station.addOutbound(r);
     				}
     			}
     		} else if(eventType == XmlPullParser.END_TAG) {
     			tag = parser.getName();
     			if("station".equals(tag)) {
     				return station;
     			}
     		}
     		eventType = parser.next();
     	}
     }
 }
