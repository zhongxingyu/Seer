 package com.portaboom.android.icecastStatus;
 /**
  * $Id$
  * 
  * @author elspicyjack at gmail dot com
  * @version $Revision$
  *
  * NOTE: Please do not e-mail the author directly regarding this code.  
  * The proper forum for support is the Streambake Google Groups list at
  * http://groups.google.com/group/streambake or <streambake@groups.google.com>
  * 
 */
 
 /* FIXME
- Handle the exceptions better below; show a better error message
  to the user if there's no network connectivity for example
 - Add callbacks that other objects can use to prompt this object to do
   something
   - server URL and port to connect to?
 - Abstract the URL; the URL could be put together in this object using only
   the base
   - try for simple.xsl first (streambake/PSAS status file)
   - try for status2.xsl (Icecast default file)
- Status object calls that could be made from this object
  - returnFetchedData - returns the fetched data back to the controller
  - raiseError - shows an error message to the user and/or program logs
  about a problem encountered when fetching data
 */
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import android.util.Log;
 
 public class HTTPStatusDownload {
 	static final String LOGTAG = "HTTPStatusDownload";
 	static final String SIMPLESTATUS = "simple.xsl";
 	static final String DEFAULTSTATUS = "status2.xsl";
 	
 	/**
      * Fetch the URL passed in as as @param statURL 
 	 * @param statURL The URL to fetch and parse the output of 
 	 */
 
 	public String fetch(String statURL) {
 		Log.d(LOGTAG, "entering fetch; statURL is " + statURL);
 		// initialize local variables
 		String line = "", returnHTML = "";
 		String simpleURL = statURL + "/" + SIMPLESTATUS;
 		//String defaultURL = statURL + "/" + DEFAULTSTATUS;
 		int linesRead = 0;
 		URL openURL = null;
 		BufferedReader urlReader = null;
 		
 		// start processing the URL
 		Log.d(LOGTAG, "creating URL object as: " + simpleURL);
     	try {
             openURL = new URL(statURL + "/" + SIMPLESTATUS);
     	} catch (MalformedURLException e) {
     		// FIXME pop up some notice to the user
     		// make a call back to the IcecastStatus object with the error text
     		Log.e(LOGTAG, "malformed URL: " + simpleURL );
     	} // try openURL
     	
     	// open the stream using the URL object
     	Log.d(LOGTAG, "opening URL: " + simpleURL );
         try {
             urlReader = new BufferedReader(
             		new InputStreamReader(openURL.openStream()));
         } catch (FileNotFoundException e) {
     		// FIXME pop up some notice to the user
     		// make a call back to the IcecastStatus object with the error text
         	Log.e(LOGTAG, "HTTP 404 File not found: " + simpleURL);
         	return "";
         } catch (IOException e) {
     		// FIXME pop up some notice to the user
     		// make a call back to the IcecastStatus object with the error text
         	Log.e(LOGTAG, "IOException: " + e);
         } // try BufferedReader
         
         // now read in from the open socket
 		try {
             while ((line = urlReader.readLine()) != null) {
                 returnHTML = returnHTML + line;
                 linesRead++;
             }
             urlReader.close();
 		//} catch (Some Exception e) { 
         //    Log.e(LOGTAG, "Load failed: " + e);
         } catch (IOException e) {
     		// FIXME pop up some notice to the user
     		// make a call back to the IcecastStatus object with the error text
             Log.e(LOGTAG, "IOException: " + e);
         } // try
 //        System.out.println("Downloaded status is:");
 //        System.out.println(returnHTML);
         Log.d(LOGTAG, "Read " + linesRead + " from " + statURL);
         return returnHTML;
 	} // public static void main(String[] args)
 } // public class HTTPStatusDownload
