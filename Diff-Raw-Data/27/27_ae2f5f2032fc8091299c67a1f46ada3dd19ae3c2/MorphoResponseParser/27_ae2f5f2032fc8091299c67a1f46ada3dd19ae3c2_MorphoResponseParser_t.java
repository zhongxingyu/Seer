 package com.example.simpletalk;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
import android.content.Context;
 import android.util.Xml;
 
 public class MorphoResponseParser {
    private static String ADJECTIVE;
    private static String AUXILIARY_VERB;
    private static String POSTPOSITION;
 
     private int mCount = 0;
     private List<String> mReadings = new ArrayList<String>();
     private List<String> mPoses = new ArrayList<String>();
 
    public MorphoResponseParser(Context context) {
    	ADJECTIVE = context.getResources().getString(R.string.adjective);
    	AUXILIARY_VERB = context.getResources().getString(R.string.auxiliary_verb);
    	POSTPOSITION = context.getResources().getString(R.string.postposition);
    }

     public void parse(String xml) {
         XmlPullParser parser = Xml.newPullParser();
         try {
             parser.setInput(new StringReader(xml));
 
             int eventType = parser.getEventType();
             String reading = null;
             String pos = null;
             while (eventType != XmlPullParser.END_DOCUMENT) {
                 if (eventType == XmlPullParser.START_DOCUMENT) {
                     // start parse
                 } else if (eventType == XmlPullParser.START_TAG) {
                     if (parser.getName().equals("ma_result")) {
                         // do nothing
                     } else if (parser.getName().equals("total_count")) {
                         mCount = Integer.parseInt(parser.nextText());
                     } else if (parser.getName().equals("reading")) {
                         reading = parser.nextText();
                     } else if (parser.getName().equals("pos")) {
                         pos = parser.nextText();
                     }
                 } else if (eventType == XmlPullParser.END_TAG) {
                     if (parser.getName().equals("word")) {
                         if (reading != null & pos != null) {
                             mReadings.add(reading);
                             reading = null;
                             mPoses.add(pos);
                             pos = null;
                         }
                     }
                 }
                 eventType = parser.next();
             }
         } catch (XmlPullParserException e) {
             e.printStackTrace();
         } catch (NumberFormatException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public String parrot() {
         if (mCount < 2 || mPoses.size() < 1) {
             return null;
         }
         int lastIndex = mPoses.size() - 1;
         StringBuilder result = new StringBuilder();
         int index = mPoses.lastIndexOf(ADJECTIVE);
         result.append(mReadings.get(index));
         // next word
         if (mPoses.get(++index).equals(AUXILIARY_VERB)) {
             result.append(mReadings.get(index));
             // next word
             if (mPoses.get(++index).equals(POSTPOSITION) && lastIndex == index) {
                 result.append(mReadings.get(index));
                 return result.toString();
             }
         } else if (mPoses.get(++index).equals(POSTPOSITION) && lastIndex == index) {
             result.append(mReadings.get(index));
             return result.toString();
         }
         return null;
     }
 }
