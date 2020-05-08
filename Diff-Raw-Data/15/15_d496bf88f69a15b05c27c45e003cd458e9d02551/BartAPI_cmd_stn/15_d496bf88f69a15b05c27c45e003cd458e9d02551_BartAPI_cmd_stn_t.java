 package com.trugertech.quickbart;
 
 import java.net.URL;
 import java.util.ArrayList;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.DefaultHandler;
 
 import android.util.Log;
 
 public class BartAPI_cmd_stn extends DefaultHandler {
 	
     private String currentNode = null;
 
     ArrayList<String> nameList;
     ArrayList<String> abbrList;
     
 
     BartAPI_cmd_stn() {
     }
 
     /**
      * Gets the BART station name listing as a 2 dimensional string array.
      * The first array is the long names of the stations.
      * The second array is the abbreviated names of the stations.
      * @return
      */
     String[][] getResults()
     {
     	
    	String[][] ret = { 	nameList.toArray(new String[nameList.size()]), 
    						abbrList.toArray(new String[abbrList.size()]) };
     	return ret;
     }
     
     @Override
     public void startDocument() throws SAXException {
     	// initialize "list"
     	nameList = new ArrayList<String>();
     	abbrList = new ArrayList<String>();
     }
 
     @Override
     public void endDocument() throws SAXException {
 
     }
 
     @Override
     public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
 
         try {
 
         	// Check and save state for parent nodes
         	if (localName.equals("station")) {
 	        	this.currentNode = localName;
 	        }
         	else if (localName.equals("name")) {
 	        	this.currentNode = localName;
 	        }
         	else if (localName.equals("abbr")) {
 	        	this.currentNode = localName;
 	        }
         	else {
         		this.currentNode = null;
         	}
         	
         } catch (Exception ee) {
             Log.d("error in startElement", ee.getStackTrace().toString());
         }
     }
 
     @Override
     public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
 
     }
 
 
     @Override
     public void characters(char ch[], int start, int length) {
 
     	String info = new String(ch, start, length);
    
     	if (this.currentNode != null){
     		if (this.currentNode.equals("name")) {
     			this.nameList.add(info);
             }
     		else if (this.currentNode.equals("abbr")) {
         		this.abbrList.add(info);
             }
     		this.currentNode = null;// reset since value saved
     	}
     	
     }
     
     /**
      * Grabs all the available BART stations from the BART API.
      * Stores both the Long and Short station names
      * @return Two dimensional string array wit the long an short
      * station names
      * 
      * @throws Exception
      *  
      */
     public static String[][] getStations() throws Exception {
     	
     	String[][] stations = null;
         	
 		// set up XML source
 		URL bartURL = new URL(BartAPI_URIGenerator.getCmd_Stn());
 		InputSource is = new InputSource(bartURL.openStream());
 		
 		//create XML factory
 		SAXParserFactory factory = SAXParserFactory.newInstance();
 		
 		//crate XML parser
 		SAXParser parser = factory.newSAXParser();
 		
 		//create XML reader
 		XMLReader xmlReader = parser.getXMLReader();
 		
 		//instantiate handler	        	
 		BartAPI_cmd_stn bfh = new BartAPI_cmd_stn();
 		
 		//assign handler
 		xmlReader.setContentHandler(bfh);
 		
 		//parse
 		xmlReader.parse(is);
 		
 		//get some data!
 		stations = bfh.getResults();	
         	
     	return stations;
     }
     
     /**
      * Takes in a station listing array, finds the passed short name
      * and returns the full name of the station.
      * If the matching long name is not found the short name is returned.
      */
     public static String getStationLongName(String[][] stations, String shortName){
 
     	int index = 0;
     	
     	for(String tmpName : stations[1]){
     		if(tmpName.equals(shortName)){
     			return stations[0][index]; 
     		}
     		index++;
     	}
     	return shortName;
     }
 
 }
