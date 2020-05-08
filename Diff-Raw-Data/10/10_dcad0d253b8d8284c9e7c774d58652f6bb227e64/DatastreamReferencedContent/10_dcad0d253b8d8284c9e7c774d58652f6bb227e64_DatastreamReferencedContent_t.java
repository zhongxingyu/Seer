 package fedora.server.storage.types;
 
 import fedora.common.HttpClient;
 import fedora.server.errors.StreamIOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 
import org.apache.commons.httpclient.methods.GetMethod;

 /**
  *
  * <p><b>Title:</b> DatastreamReferencedContent.java</p>
  * <p><b>Description:</b> Referenced Content.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * <p><b>License and Copyright: </b>The contents of this file are subject to the
  * Mozilla Public License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License
  * at <a href="http://www.mozilla.org/MPL">http://www.mozilla.org/MPL/.</a></p>
  *
  * <p>Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  * the specific language governing rights and limitations under the License.</p>
  *
  * <p>The entire file consists of original code.  Copyright &copy; 2002-2005 by The
  * Rector and Visitors of the University of Virginia and Cornell University.
  * All rights reserved.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * @author cwilper@cs.cornell.edu
  * @version $Id$
  */
 public class DatastreamReferencedContent
         extends Datastream {
 
     public DatastreamReferencedContent() {
     }
 
     /**
      * Gets an InputStream to the content of this externally-referenced
      * datastream.
      * <p></p>
      * The DSLocation of this datastream must be non-null before invoking
      * this method.
      * <p></p>
      * If successful, the DSMIME type is automatically set based on the
      * web server's response header.  If the web server doesn't send a
      * valid Content-type: header, as a last resort, the content-type
      * is guessed by using a map of common extensions to mime-types.
      * <p></p>
      * If the content-length header is present in the response, DSSize
      * will be set accordingly.
      */
     public InputStream getContentStream()
             throws StreamIOException {
       	InputStream contentStream = null;
       	try {
       		HttpClient client = new HttpClient(DSLocation);
      		GetMethod getMethod = client.doNoAuthnGet(20000, 25);
       		if (client.getStatusCode() != HttpURLConnection.HTTP_OK) {
       			log("in getContentStream(), got bad code=" + client.getStatusCode());
       			throw new StreamIOException(
                     "Server returned a non-200 response code ("
                     + client.getStatusCode() + ") from GET request of URL: "
                     + DSLocation);
       		}          
       		log("in getContentStream(), got 200");
      		contentStream = getMethod.getResponseBodyAsStream();
       		//get.releaseConnection() before stream is read would give java.io.IOException: Attempted read on closed stream.
       		int contentLength = 0;
       		if (client.getGetMethod().getResponseHeader("Content-Length") != null) {
      			contentLength = Integer.parseInt(getMethod.getResponseHeader("Content-Length").getValue());
       		}      		
             if (contentLength > -1) {
                 DSSize = contentLength;
             }      		
       	} catch (Throwable th) {
       		th.printStackTrace();
       		throw new StreamIOException("[DatastreamReferencedContent] "
       			+ "returned an error.  The underlying error was a "
     			+ th.getClass().getName() + "  The message "
     			+ "was  \"" + th.getMessage() + "\"  .  ");
       	} finally {
       		log("in getContentStream(), in finally");     	
       	}    	
     	return(contentStream);
 
       		
       		
       		
     	/* begin "was"
         try {
             HttpURLConnection conn=(HttpURLConnection)
                     new URL(DSLocation).openConnection();
             if (conn.getResponseCode()!=HttpURLConnection.HTTP_OK) {
                 throw new StreamIOException(
                         "Server returned a non-200 response code ("
                         + conn.getResponseCode() + ") from GET request of URL: "
                         + DSLocation.toString());
             }
             // Ensure the stream is available before setting any fields.
             InputStream ret=conn.getInputStream();
             // If content-length available, set DSSize.
             int reportedLength=conn.getContentLength();
             if (reportedLength>-1) {
                 DSSize=reportedLength;
             }
 
             // SDP: removed because some web servers will reset the mime type
             // to unpredictable things.  We'll keep the mime type originally
             // recorded with the datastream.
             // If Content-type available, set DSMIME.
             //DSMIME=conn.getContentType();
             //if (DSMIME==null) {
             //    DSMIME=HttpURLConnection.guessContentTypeFromName(
             //            DSLocation);
             //}
             
             return ret;
         } catch (IOException ioe) {
             throw new StreamIOException("Can't get InputStream from URL: "
                     + DSLocation.toString());
         } catch (ClassCastException cce) {
             throw new StreamIOException("Non-http URLs not supported.");
         }
         end "was" */
     }
     
     
     private boolean log = false;
     
     private final void log(String msg) {
     	if (log) {
   	  	System.err.println(msg);	  		
     	}
     }    
     
 }
