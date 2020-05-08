 package com.globalresolvewws;
 
 /**
  * This service is based off of an example found on stackoverflow
  * which pointed at the google weather API. It has been modified to point at
  * the previmeteo API and hard coded to point at Accra in Ghana
  * 
  * @author Caleb, (?)
  * 
  */
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 public class WeatherService {
 
 	protected static final String URL_SOURCE =
 			"http://api.previmeteo.com/e240a3071ce76b3930492e637bd3f326/ig/api?weather=";
 	
	public ArrayList<Weather> Weather() 
 	{
 		/*** Create the request ***/
 		// Let's pick a location:
 		String location = "accra";
 		// Create the URL:
 		String query = URL_SOURCE + location;
 		// Replace blanks with HTML-Equivalent:
 		query = query.replace(" ","%20");
 
 		/*** Make the request (This needs to be in a try-catch
 		     block because things can go wrong)***/
 		try {
 			// Turn the string into a URL object
 			URL urlObject = new URL(query);
 			// Open the stream (which returns an InputStream):
 			InputStream in = urlObject.openStream();
 
 		/** Now parse the data (the stream) that we received back ***/
 
 			// Create an XML reader
 			XMLReader xr = XMLReaderFactory.createXMLReader();
 
 			// Tell that XML reader to use our special Handler
 			ServiceHandler ourSpecialHandler = new ServiceHandler();
 			xr.setContentHandler(ourSpecialHandler);
 
 			// We have an InputStream, but let's just wrap it in
 			//  an InputSource (the SAX parser likes it that way)
 			InputSource inSource = new InputSource(in);
 			
 			//System.out.print(inSource);
 			// And parse it!
 			//xr.parse(inSource);
 			return ourSpecialHandler.forecast;
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		} catch (SAXException se) {
 			se.printStackTrace();
 		}
 		return null;
 	}
 }
