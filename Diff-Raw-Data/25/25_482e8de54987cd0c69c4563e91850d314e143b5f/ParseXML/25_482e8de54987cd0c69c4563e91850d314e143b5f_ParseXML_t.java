 package com.turfgame.xml;
 
 //Guide http://www.anddev.org/novice-tutorials-f8/parsing-xml-from-the-net-using-the-saxparser-t353.html
 
import java.io.FileNotFoundException;
 import java.net.ConnectException;
 import java.net.URL;
 import java.net.UnknownHostException;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.InputSource;
 import org.xml.sax.XMLReader;
 
 import android.util.Log;
 
 import com.turfgame.widget.CharStats;
 import com.turfgame.widget.TurfWidget;
 
 public class ParseXML
 {
 	private static final String URL_ADRESS = "http://api.turfgame.com/v1/user/";
 	private static final String DEBUG_TAG = "ParseXML";
 
 	public static CharStats parseXML(String email)
 	{
 		if (TurfWidget.DEBUG) {
 			Log.d(TurfWidget.DEBUG_STRING, "parseXML");
 			// Simulating somewhat slow network.
 			try {
 				Log.d(TurfWidget.DEBUG_STRING, "sleeping..");
 				Thread.sleep(3000);
 				Log.d(TurfWidget.DEBUG_STRING, "..done");
 			}
 			catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		try {
 			URL url = new URL(URL_ADRESS + email);
 
 			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
 			SAXParser saxParser = parserFactory.newSAXParser();
 
 			XMLReader reader = saxParser.getXMLReader();
 
 			ParseHandler parseHandler = new ParseHandler();
 			reader.setContentHandler(parseHandler);
 
 			InputSource is = new InputSource(url.openStream());
 			
 			// Hack to workaround faulty encoding from server.
 			// When the server side is fixed to actually deliver utf-8 this will
 			// result in broken characters, but it will at lease decode without
 			// exceptions, and those strings are not yet used anyway.
 			is.setEncoding("ISO-8859-1");
 
 			reader.parse(is);
 			if (TurfWidget.DEBUG) {
 				Log.d(DEBUG_TAG, "Success");
 			}
 
 			CharStats charStats = parseHandler.getCharStats();
 
 			TurfWidget.setError(null);
 			return charStats;
 		}
 		catch (UnknownHostException e) {
 			TurfWidget.setError("internet");
 //			Log.e(DEBUG_TAG, "XMLQueryErrror?", e);
 		}
 		catch (ConnectException e) {
 			TurfWidget.setError("server");
 //			Log.e(DEBUG_TAG, "XMLQueryErrror?", e);
 		}
        catch (FileNotFoundException e) {
            TurfWidget.setError("file");
        }
 		catch (Exception e) {
 			Log.e(DEBUG_TAG, "XMLQueryErrror?", e);
 		}
 
 		return new CharStats();
 	}
 }
