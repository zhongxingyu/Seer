 package fedora.server.storage.types;
 
 import fedora.server.errors.StreamIOException;
 import java.io.InputStream;
 import fedora.common.http.HttpInputStream;
 import fedora.common.http.WebClient;
 
 /**
  *
  * <p><b>Title:</b> DatastreamReferencedContent.java</p>
  * <p><b>Description:</b> Referenced Content.</p>
  *
  * @author cwilper@cs.cornell.edu
  * @version $Id$
  */
 public class DatastreamReferencedContent
         extends Datastream {
 
 	private static WebClient s_http;
 	
 	static {
 		s_http = new WebClient();
 	}
 	
     public DatastreamReferencedContent() {
     }
 
     public Datastream copy() {
         DatastreamReferencedContent ds = new DatastreamReferencedContent();
         copy(ds);
         return ds;
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
 
       	HttpInputStream contentStream = null;
       	try {
            contentStream = s_http.get(DSLocation, true, null, null);
             DSSize = new Long(contentStream.getResponseHeaderValue("content-length","0")).longValue();
       	} catch (Throwable th) {
       		th.printStackTrace();
       		throw new StreamIOException("[DatastreamReferencedContent] "
       			+ "returned an error.  The underlying error was a "
     			+ th.getClass().getName() + "  The message "
     			+ "was  \"" + th.getMessage() + "\"  .  ");
       	}  	
     	return(contentStream);
     }    
     
 }
