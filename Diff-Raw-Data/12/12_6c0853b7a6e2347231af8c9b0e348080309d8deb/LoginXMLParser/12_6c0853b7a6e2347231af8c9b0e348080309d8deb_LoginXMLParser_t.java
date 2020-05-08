 package com.axprint.official.XMLParsers;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import android.util.Log;
 
 public class LoginXMLParser implements GeneralXMLParser {
 	public static final int LOGIN_OK = 0x1;
 	public static final int LOGIN_FAILED = 0x2;
 	public static final int LOGIN_ERROR = 0x3;
 	XmlPullParser xpp;
 
 	public LoginXMLParser() {
 		try {
 			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
 			factory.setNamespaceAware(false);
 			xpp = factory.newPullParser();
 		} catch (XmlPullParserException e) {
 			// XXX do something!
 		}
 	}
 
 	@Override
 	public Integer parse(InputStream response) {
 		Log.d("MYTAP","In parse");
 		try {
 		xpp.setInput(new InputStreamReader(response));
 		int eventType = xpp.getEventType();
 		while(eventType != XmlPullParser.END_DOCUMENT) {
 			switch(eventType) {
 			case XmlPullParser.START_DOCUMENT:
 				break;
 			case XmlPullParser.START_TAG:
 				break;
 			case XmlPullParser.END_TAG:
 				break;
 			case XmlPullParser.TEXT:
 				String text = xpp.getText();
 				Log.d("MYTAP","text is " + text);
				if(text.equals("1")) return LOGIN_OK;
				if(text.equals("0")) return LOGIN_FAILED;
				if(text.equals("-1")) return LOGIN_ERROR;
 			}
 			eventType = xpp.next();
 		}
 		return LOGIN_ERROR;
 		} catch(XmlPullParserException e) {
 			// XXX do something
 			return LOGIN_ERROR;
 		} catch(IOException e) {
 			// XXX do something
 			return LOGIN_ERROR;
 		}
 	}
 
 }
