 //
 // typica - A client library for Amazon Web Services
 // Copyright (C) 2007 Xerox Corporation
 // 
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 // 
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 // 
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 //
 package com.xerox.amazonws.common;
 
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.text.Collator;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TimeZone;
 import java.util.TreeMap;
 
 import com.xerox.amazonws.tools.LoggingConfigurator;
 
 /**
  * This class provides an interface with the Amazon SQS service. It provides high level
  * methods for listing and creating message queues.
  *
  * @author D. Kavanagh
  * @author developer@dotech.com
  */
 public class AWSQueryConnection extends AWSConnection {
 
     /**
 	 * Initializes the queue service with your AWS login information.
 	 *
      * @param awsAccessId The your user key into AWS
      * @param awsSecretKey The secret string used to generate signatures for authentication.
      * @param isSecure True if the data should be encrypted on the wire on the way to or from SQS.
      * @param server Which host to connect to.  Usually, this will be s3.amazonaws.com
      * @param port Which port to use.
      */
     public AWSQueryConnection(String awsAccessKeyId, String awsSecretAccessKey, boolean isSecure,
                              String server, int port)
     {
 		super(awsAccessKeyId, awsSecretAccessKey, isSecure, server, port);
     }
 
     /**
      * Make a new HttpURLConnection.
      * @param method The HTTP method to use (GET, PUT, DELETE)
      * @param resource The resource name (bucketName + "/" + key).
      * @param headers A Map of String to List of Strings representing the http
      * headers to pass (can be null).
      */
     protected HttpURLConnection makeRequest(String method, String action, Map<String, String> params)
         throws MalformedURLException, IOException
     {
 		// add auth params, and protocol specific headers
 		Map<String, String> qParams = new HashMap<String, String>(params);
 		qParams.put("Action", action);
 		qParams.put("AWSAccessKeyId", getAwsAccessKeyId());
 		qParams.put("SignatureVersion", "1");
 		qParams.put("Timestamp", httpDate());
         if (headers != null) {
             for (Iterator<String> i = headers.keySet().iterator(); i.hasNext(); ) {
                 String key = i.next();
                 for (Iterator<String> j = headers.get(key).iterator(); j.hasNext(); ) {
 					qParams.put(key, j.next());
                 }
             }
         }
 		// sort params by key
 		ArrayList<String> keys = new ArrayList<String>(qParams.keySet());
 		Collator stringCollator = Collator.getInstance();
 		stringCollator.setStrength(Collator.PRIMARY);
 		Collections.sort(keys, stringCollator);
 
 		// build param string
 		StringBuilder resource = new StringBuilder();
 		for (String key : keys) {
			resource.append("&");
 			resource.append(key);
			resource.append("=");
 			resource.append(qParams.get(key));
 		}
		resource.setCharAt(0, '?');	// set first param delimeter
 
 		// calculate signature
		String canonical = resource.toString().replaceAll("&", "").replaceAll("=", "").replaceAll("\\?", "");
        String encoded = urlencode(encode(getSecretAccessKey(), canonical, false));
 
 		// build param string, encoding values and adding request signature
 		resource = new StringBuilder();
 		for (String key : keys) {
 			resource.append("&");
 			resource.append(key);
 			resource.append("=");
 			resource.append(urlencode(qParams.get(key)));
 		}
 		resource.setCharAt(0, '?');	// set first param delimeter
 		resource.append("&Signature=");
 		resource.append(encoded);
 
 		// finally, build request object
         URL url = makeURL(resource.toString());
         HttpURLConnection connection = (HttpURLConnection)url.openConnection();
         connection.setRequestMethod(method);
 
         return connection;
     }
 
     /**
      * Generate an rfc822 date for use in the Date HTTP header.
      */
     private static String httpDate() {
         final String DateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
         SimpleDateFormat format = new SimpleDateFormat( DateFormat, Locale.US );
         format.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
         return format.format( new Date() );
     }
 }
