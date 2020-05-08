 package fedora.server.storage;
 
 import java.awt.Dimension;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.List;
 
 import org.apache.commons.httpclient.Header;
 import org.apache.commons.httpclient.HeaderElement;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.HttpState;
 import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
 import org.apache.commons.httpclient.UsernamePasswordCredentials;
 import org.apache.commons.httpclient.methods.GetMethod;
 
 import fedora.client.Administrator;
 import fedora.client.SwingWorker;
 import fedora.server.Context;
 import fedora.server.Module;
 import fedora.server.Server;
 import fedora.server.errors.GeneralException;
 import fedora.server.errors.ModuleInitializationException;
 import fedora.server.storage.types.MIMETypedStream;
 import fedora.server.errors.HttpServiceNotFoundException;
 import fedora.server.errors.StreamIOException;
 import fedora.server.storage.types.Property;
 import fedora.server.utilities.StreamUtility;
 import fedora.server.security.Authorization;
 import fedora.common.Constants;
 
 /**
  *
  * <p><b>Title:</b> DefaultExternalContentManager.java</p>
  * <p><b>Description:</b> Provides a mechanism to obtain external HTTP-accessible
  * content.</p>
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
  * @author rlw@virginia.edu
  * @version $Id$
  */
 public class DefaultExternalContentManager extends Module
     implements ExternalContentManager
 {
 
   private String m_userAgent;
   private String fedoraServerHost;
   private String fedoraServerPort;
 
   /**
    * <p> Creates a new DefaultExternalContentManager.</p>
    *
    * @param moduleParameters The name/value pair map of module parameters.
    * @param server The server instance.
    * @param role The module role name.
    * @throws ModuleInitializationException If initialization values are
    *         invalid or initialization fails for some other reason.
    */
   public DefaultExternalContentManager(Map moduleParameters,
                                        Server server, String role)
       throws ModuleInitializationException
   {
     super(moduleParameters, server, role);
   }
 
   /**
    * Initializes the Module based on configuration parameters. The
    * implementation of this method is dependent on the schema used to define
    * the parameter names for the role of
    * <code>fedora.server.storage.DefaultExternalContentManager</code>.
    *
    * @throws ModuleInitializationException If initialization values are
    *         invalid or initialization fails for some other reason.
    */
   public void initModule() throws ModuleInitializationException
   {
     try
     {
       Server s_server = this.getServer();
       s_server.logInfo("DefaultExternalContentManager initialized");
       m_userAgent=getParameter("userAgent");
       if (m_userAgent==null) {
         m_userAgent="Fedora";
       }
 
       fedoraServerPort = s_server.getParameter("fedoraServerPort");
       fedoraServerHost = s_server.getParameter("fedoraServerHost");
 
 
     } catch (Throwable th)
     {
       throw new ModuleInitializationException("[DefaultExternalContentManager] "
           + "An external content manager "
           + "could not be instantiated. The underlying error was a "
           + th.getClass().getName() + "The message was \""
           + th.getMessage() + "\".", getRole());
     }
   }
 
   /**
    * A method that reads the contents of the specified URL and returns the
    * result as a MIMETypedStream
    *
   * @param url The URL of the external content.
    * @return A MIME-typed stream.
    * @throws HttpServiceNotFoundException If the URL connection could not
    *         be established.
    */
   public MIMETypedStream getExternalContent(String url, Context context)
       throws GeneralException, HttpServiceNotFoundException {
   	log("in getExternalContent(), url=" + url);
   	MIMETypedStream httpContent = null;
   	try {
   		HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager()); 
   		client.setConnectionTimeout(20000); // wait 20 seconds max
   		log("password=" + context.getPassword());
   		client.getState().setCredentials(null, null, new UsernamePasswordCredentials(context.getSubjectValue(Authorization.SUBJECT_ID_URI_STRING),context.getPassword()));
   		client.getState().setAuthenticationPreemptive(true);
   		log("in getExternalContent(), after setup");
   	  	GetMethod get = null;
   		int resultCode = -1;
   		for (int loops = 0; (url != null) && (loops < 25); loops++) {
   			log("in getExternalContent(), new loop, url=" + url);
   			get = new GetMethod(url);
   			url = null;
   			log("in getExternalContent(), got GetMethod object=" + get);
   			get.addRequestHeader("User-Agent", m_userAgent);
   			get.setDoAuthentication(true);
   			get.setFollowRedirects(true);
   			resultCode=client.executeMethod(get);
   			if (300 <= resultCode && resultCode <= 399) {
   				url=get.getResponseHeader("Location").getValue();
   				log("in getExternalContent(), got redirect, new url=" + url);
   			}
   		}
   		if (resultCode!=HttpURLConnection.HTTP_OK) {
   			log("in getExternalContent(), got bad code=" + resultCode);
   			throw new StreamIOException(
                 "Server returned a non-200 response code ("
                 + resultCode + ") from GET request of URL: "
                 + url);
   		}          
   		log("in getExternalContent(), got 200");
   		//connection.setInstanceFollowRedirects(true);
   		Header[] headers = get.getResponseHeaders();
   		Property[] headerArray = new Property[headers.length];
   		for (int i = 0; i < headers.length; i++) {
   			headerArray[i] = new Property();
   			headerArray[i].name = headers[i].getName();
   			headerArray[i].value = headers[i].getValue();
   			log("in getExternalContent(), (after loop) " + headerArray[i].name + "=" + headerArray[i].value);
   		}
   		String contentType = "text/plain";
   		if (get.getResponseHeader("Content-Type") != null) {
   			contentType = get.getResponseHeader("Content-Type").getValue();
   		}
   		log("in getExternalContent(), contentType=" + contentType);
   		for (int ha=0; ha<headerArray.length; ha++) {
   			log("in getExternalContent(), header=" + headerArray[ha].name + "=" + headerArray[ha].value);
   		}
   		httpContent = new MIMETypedStream(contentType, get.getResponseBodyAsStream(), headerArray);
   		//get.releaseConnection() before stream is read would give java.io.IOException: Attempted read on closed stream. 
   		log("in getExternalContent(), httpContent=" + httpContent);
   	} catch (Throwable th) {
   		th.printStackTrace();
   		throw new HttpServiceNotFoundException("[DefaultExternalContentManager] "
   			+ "returned an error.  The underlying error was a "
 			+ th.getClass().getName() + "  The message "
 			+ "was  \"" + th.getMessage() + "\"  .  ");
   	} finally {
   		log("in getExternalContent(), in finally");
  	
   	}    	
 	return(httpContent);
   }
   
   private boolean log = false;
   
   private final void log(String msg) {
   	if (log) {
 	  	System.err.println(msg);	  		
   	}
   }
   
 }
