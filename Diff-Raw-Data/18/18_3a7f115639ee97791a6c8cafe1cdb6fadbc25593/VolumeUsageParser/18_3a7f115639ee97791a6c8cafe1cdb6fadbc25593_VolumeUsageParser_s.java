 package au.id.teda.broadband.usage.parser;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.util.Log;
 import android.util.Xml;
 import au.id.teda.broadband.usage.ui.MainActivity;
 import au.id.teda.broadband.usage.util.DailyVolumeUsage;
 
 public class VolumeUsageParser {
 	
 	private static final String DEBUG_TAG = MainActivity.DEBUG_TAG;
 	
 	private static final String ns = null; // We don't use namespaces
 	private static final String FEED_TAG = "ii_feed";
 	private static final String VOLUME_USAGE_TAG = "volume_usage";
 	private static final String DAY_HOUR_TAG = "day_hour";
 	private static final String USAGE_TAG = "usage";
 	private static final String PERIOD_ATT	= "period";
 	private static final String TYPE_ATT = "type";
 	private static final String PEAK = "peak";
 	private static final String OFFPEAK = "offpeak";
 	private static final String UPLOADS = "uploads";
 	private static final String FREEZONE = "freezone";
 	
 	// Flag to make sure we set the month only once during parsing
 	private boolean monthSetFlag = false;
 	private String mDataMonth = null;
 	
 	public List<DailyVolumeUsage> parse (InputStream inputStream) throws XmlPullParserException, IOException {
 		try {
 			XmlPullParser parser = Xml.newPullParser();
 	        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
 	        parser.setInput(inputStream, null);
 	        parser.nextTag();
 	        return readFeed(parser);
 	    } finally {
 	    	inputStream.close();
 	    }
 	}
 	
 	private List<DailyVolumeUsage> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
 		
 		List<DailyVolumeUsage> usage = new ArrayList<DailyVolumeUsage>();
 		
 	    parser.require(XmlPullParser.START_TAG, ns, FEED_TAG);
 	    while (parser.next() != XmlPullParser.END_TAG) {
 	    	if (parser.getEventType() != XmlPullParser.START_TAG) {
 	    		continue;
 	    	}
 	    		
 	    	String tagName = parser.getName();
 	    	if (tagName.equals(VOLUME_USAGE_TAG)) {
 	    		usage = readVolumeUsage(parser);
 	    	} else {
 	    		skip(parser);
 	    	}
 	    }
 	    return usage;
 	}
 	
     public List<DailyVolumeUsage> readVolumeUsage(XmlPullParser parser) throws XmlPullParserException, IOException {
     	
     	List<DailyVolumeUsage> usage = new ArrayList<DailyVolumeUsage>();
     	
 	    parser.require(XmlPullParser.START_TAG, ns, VOLUME_USAGE_TAG);
 	    while (parser.next() != XmlPullParser.END_TAG) {
 	    	if (parser.getEventType() != XmlPullParser.START_TAG) {
 	    		continue;
 	    	}
 	    	
 	    	String tagName = parser.getName();
 	    	if (tagName.equals(VOLUME_USAGE_TAG)){
 	    		usage = readVolumeUsage2(parser);
 	    	} else {
                 skip(parser);
             }
 	    }
 	    return usage;   
     }
     
     public List<DailyVolumeUsage> readVolumeUsage2(XmlPullParser parser) throws XmlPullParserException, IOException {
     	
     	List<DailyVolumeUsage> usage = new ArrayList<DailyVolumeUsage>();
     	
 	    parser.require(XmlPullParser.START_TAG, ns, VOLUME_USAGE_TAG);
         
 	    while (parser.next() != XmlPullParser.END_TAG) {
 	    	if (parser.getEventType() != XmlPullParser.START_TAG) {
 	    		continue;
 	    	}
 	    	
 	    	String tagName = parser.getName();
 	    	if (tagName.equals(DAY_HOUR_TAG)){
 	    		usage.add(readDayHour(parser));
 	    	} else {
                 skip(parser);
             }
 	    }
 	    return usage;
     }
     
     public DailyVolumeUsage readDayHour(XmlPullParser parser) throws XmlPullParserException, IOException {
     	Calendar day = getDay(parser.getAttributeValue(null, PERIOD_ATT));
    	Log.d(DEBUG_TAG, "Date:" + day.getTime());
     	
     	parser.require(XmlPullParser.START_TAG, ns, DAY_HOUR_TAG);
     	
     	if (!monthSetFlag){
     		mDataMonth = getMonthString(day);
     		monthSetFlag = true;
     	}
     	
     	Long mDay = day.getTimeInMillis();
     	Long mPeak = null;
     	Long mOffPeak = null;
     	Long mUploads  = null;
     	Long mFreezone = null;
     	
     	while (parser.next() != XmlPullParser.END_TAG) {
             if (parser.getEventType() != XmlPullParser.START_TAG) {
                 continue;
             }
             
             String tagName = parser.getName();
             String tagAtt = parser.getAttributeValue(null, TYPE_ATT);
             if (tagName.equals(USAGE_TAG)){
             	if (tagAtt.equals(PEAK)){
             		mPeak = readUsage(parser);
 	    		} else if (tagAtt.equals(OFFPEAK)){
 	    			mOffPeak = readUsage(parser);
 	    		} else if (tagAtt.equals(UPLOADS)){
 	    			mUploads = readUsage(parser);
 	    		} else if (tagAtt.equals(FREEZONE)){
 	    			mFreezone = readUsage(parser);
 	    		}
 	    	} else {
                 skip(parser);
             }
     	}
     	
     	return new DailyVolumeUsage(mDay, mDataMonth, mPeak, mOffPeak, mUploads, mFreezone );
     }
 	
     private Long readUsage(XmlPullParser parser) throws IOException, XmlPullParserException {
         parser.require(XmlPullParser.START_TAG, ns, USAGE_TAG);
         String usage = readText(parser);
         parser.require(XmlPullParser.END_TAG, ns, USAGE_TAG);
         return stringToLong(usage);
     }
     
     private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
         String text = null;
         if (parser.next() == XmlPullParser.TEXT) {
         	text = parser.getText();
             parser.nextTag();
         }
         return text;
     }
     
     private Calendar getDay(String period){
     	String FORMAT_YYYY_MM_DD  = "yyyy-MM-dd";
     	SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_YYYY_MM_DD);
     	Calendar day = Calendar.getInstance();
 		try {
 			day.setTime(sdf.parse(period));
 			day.set(Calendar.HOUR_OF_DAY, 0);
 			day.set(Calendar.MINUTE, 1);
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
		
     	return day;
     }
     
     private String getMonthString(Calendar period){
     	
     	String FORMAT_YYYYMM = "yyyyMM";
     	period.add(Calendar.DATE, 27 );
     	
     	SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_YYYYMM);
     	String month = sdf.format(period.getTime());
     	
     	return month;
     }
 	
     private Long stringToLong(String s){
     	Long l = Long.parseLong(s);
     	return l;
     }
 	
     // Skips tags the parser isn't interested in. Uses depth to handle nested tags.
     private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
     	
         if (parser.getEventType() != XmlPullParser.START_TAG) {
             throw new IllegalStateException();
         }
         int depth = 1;
         while (depth != 0) {
         	switch (parser.next()) {
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
