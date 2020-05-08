 package com.chalmers.frapp;
 
import android.content.res.Resources;
 import android.util.Log;
 import android.content.res.AssetManager;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 public class LocationXMLParser extends DefaultHandler {
 
 	public LocationXMLParser(AssetManager m) {
 		// TODO Auto-generated constructor stub
 		try {
 			// getting SAXParserFactory instance
 			SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
 
 			// Getting SAXParser object from AXParserFactory instance
 			SAXParser saxParser = saxParserFactory.newSAXParser();
 
 			// Parsing XML Document by calling parse method of SAXParser class
 			saxParser.parse(m.open("chalmers.xml"), this);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
     public void startElement(String uri, String localName, String qName,
             Attributes atts) throws SAXException {
     	Log.i("[XML]", "<<");
     	Log.i("[XML]", uri);
     	Log.i("[XML]", localName);
     	Log.i("[XML]", qName);
     	for(int i = 0; i < atts.getLength(); i++)
     		Log.i("[XML]", atts.getQName(i) + ":" + atts.getValue(i));
     }
 
     public void characters(char[] ch, int start, int length)
     		throws SAXException {
     	Log.i("[XML]", new String(ch, start, length));
     }
     
     public void endElement(String uri, String localName, String qName) 
             throws SAXException {
     	Log.i("[XML]", ">>");
     }
  
     public void endDocument() throws SAXException {
         // you can do something here for example send
         // the Channel object somewhere or whatever.
     }
 
 }
