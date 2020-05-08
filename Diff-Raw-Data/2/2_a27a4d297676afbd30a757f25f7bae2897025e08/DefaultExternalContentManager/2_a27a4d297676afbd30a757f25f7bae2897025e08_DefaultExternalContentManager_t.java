 /* The contents of this file are subject to the license and copyright terms
  * detailed in the license directory at the root of the source tree (also 
  * available online at http://www.fedora.info/license/).
  */
 
 package fedora.server.storage;
 
 import java.util.Hashtable;
 import java.util.Map;
 
 import org.apache.commons.httpclient.Header;
 
 import org.apache.log4j.Logger;
 
 import fedora.common.http.HttpInputStream;
 import fedora.common.http.WebClient;
 import fedora.server.Context;
 import fedora.server.Module;
 import fedora.server.Server;
 import fedora.server.errors.GeneralException;
 import fedora.server.errors.HttpServiceNotFoundException;
 import fedora.server.errors.ModuleInitializationException;
 import fedora.server.security.BackendPolicies;
 import fedora.server.security.BackendSecurity;
 import fedora.server.security.BackendSecuritySpec;
 import fedora.server.storage.types.MIMETypedStream;
 import fedora.server.storage.types.Property;
 import fedora.server.utilities.ServerUtility;
 
 /**
  *
  * <p><b>Title:</b> DefaultExternalContentManager.java</p>
  * <p><b>Description:</b> Provides a mechanism to obtain external HTTP-accessible
  * content.</p>
  *
  * @author rlw@virginia.edu
  * @version $Id$
  */
 public class DefaultExternalContentManager extends Module
     implements ExternalContentManager
 {
 
   /** Logger for this class. */
   private static final Logger LOG = Logger.getLogger(
         DefaultExternalContentManager.class.getName());
 
   private String m_userAgent;
   private String fedoraServerHost;
   private String fedoraServerPort;
   private String fedoraServerRedirectPort;
 
   private WebClient m_http;
 
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
       m_userAgent=getParameter("userAgent");
       if (m_userAgent==null) {
         m_userAgent="Fedora";
       }
 
       fedoraServerPort = s_server.getParameter("fedoraServerPort");
       fedoraServerHost = s_server.getParameter("fedoraServerHost");
       fedoraServerRedirectPort = s_server.getParameter("fedoraRedirectPort");
 
       m_http = new WebClient();
       m_http.USER_AGENT = m_userAgent;
 
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
      * Get a MIMETypedStream for the given URL.
      *
      * If user or password are <code>null</code>, basic authentication will 
      * not be attempted.
      */
     private MIMETypedStream get(String url,
                                 String user,
                                 String pass) throws GeneralException {
         LOG.debug("DefaultExternalContentManager.get(" + url + ")");
         try {
             HttpInputStream response = m_http.get(url, true, user, pass);
             String mimeType = response.getResponseHeaderValue("Content-Type",
                                                               "text/plain");
             Property[] headerArray = toPropertyArray(
                                          response.getResponseHeaders());
   		    return new MIMETypedStream(mimeType, response, headerArray);
         } catch (Exception e) {
             throw new GeneralException("Error getting " + url, e);
         }
     }
 
     /**
      * Convert the given HTTP <code>Headers</code> to an array of 
      * <code>Property</code> objects.
      */
     private static Property[] toPropertyArray(Header[] headers) {
 
         Property[] props = new Property[headers.length];
         for (int i = 0; i < headers.length; i++) {
             props[i] = new Property();
             props[i].name = headers[i].getName();
             props[i].value = headers[i].getValue();
         }
         return props;
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
   	LOG.debug("in getExternalContent(), url=" + url);
   	try {
   		String backendUsername = "";
   		String backendPassword = "";
   		boolean backendSSL = false;
   		String modURL = url;
 		if (ServerUtility.isURLFedoraServer(modURL)) {
 		    BackendSecuritySpec m_beSS;
 		    BackendSecurity m_beSecurity = (BackendSecurity) getServer().getModule("fedora.server.security.BackendSecurity");
 		    try {
 		    	m_beSS = m_beSecurity.getBackendSecuritySpec();
 		    } catch (Exception e) {
 		    	throw new ModuleInitializationException("Can't intitialize BackendSecurity module (in default access) from Server.getModule", getRole());
 		    }	            
 		    Hashtable beHash = m_beSS.getSecuritySpec(BackendPolicies.FEDORA_INTERNAL_CALL);
 		    backendUsername = (String) beHash.get("callUsername");
 		    backendPassword = (String) beHash.get("callPassword");	            
		    backendSSL = new Boolean((String) beHash.get("callSSL")).booleanValue();
 		    if (backendSSL) {
 		    	if (modURL.startsWith("http:")) {
 		    		modURL = modURL.replaceFirst("http:", "https:");
 		    	}
 		    	modURL = modURL.replaceFirst(":"+fedoraServerPort+"/", ":"+fedoraServerRedirectPort+"/");
 		    }    
 		}
 		if (LOG.isDebugEnabled()) {
 		   	LOG.debug("************************* backendUsername: "+backendUsername+ "     backendPassword: "+backendPassword+"     backendSSL: "+backendSSL);
 		   	LOG.debug("************************* doAuthnGetURL: "+modURL);
 		}
 
         return get(modURL, backendUsername, backendPassword);
 
   	} catch (GeneralException ge) {
         throw ge;
   	} catch (Throwable th) {
         // catch anything but generalexception
   		th.printStackTrace();
   		throw new HttpServiceNotFoundException("[DefaultExternalContentManager] "
   			+ "returned an error.  The underlying error was a "
 			+ th.getClass().getName() + "  The message "
 			+ "was  \"" + th.getMessage() + "\"  .  ");
   	}
   } 
   
 }
