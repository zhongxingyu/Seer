 package fedora.server.access.dissemination;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.catalina.realm.GenericPrincipal;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Iterator;
 
 import fedora.common.Constants;
 import fedora.server.Server;
 import fedora.server.errors.InitializationException;
 import fedora.server.errors.authorization.AuthzDeniedException;
 import fedora.server.errors.authorization.AuthzException;
 import fedora.server.errors.authorization.AuthzOperationalException;
 import fedora.server.errors.servletExceptionExtensions.RootException;
 import fedora.server.Context;
 import fedora.server.ReadOnlyContext;
 import fedora.server.security.Authorization;
 import fedora.server.storage.DOManager;
 import fedora.server.storage.DOReader;
 import fedora.server.storage.ExternalContentManager;
 import fedora.server.storage.types.MIMETypedStream;
 import fedora.server.storage.types.Datastream;
 import fedora.server.storage.types.DatastreamMediation;
 import fedora.server.storage.types.Property;
 import fedora.server.utilities.Logger;
 
 /**
  * <p><b>Title: </b>DatastreamResolverServlet.java</p>
  * <p><b>Description: </b>This servlet acts as a proxy to resolve the physical location
  * of datastreams. It requires a single parameter named <code>id</code> that
  * denotes the temporary id of the requested datastresm. This id is in the form
  * of a DateTime stamp. The servlet will perform an in-memory hashtable lookup
  * using the temporary id to obtain the actual physical location of the
  * datastream and then return the contents of the datastream as a MIME-typed
  * stream. This servlet is invoked primarily by external mechanisms needing
  * to retrieve the contents of a datastream.</p>
  * <p>The servlet also requires that an external mechanism request a datastream
  * within a finite time interval of the tempID's creation. This is to lessen
  * the risk of unauthorized access. The time interval within which a mechanism
  * must repond is set by the Fedora configuration parameter named
  * datastreamMediationLimit and is speci207fied in milliseconds. If this parameter
  * is not supplied it defaults to 5000 miliseconds.</p>
  *
  * @author rlw@virginia.edu
  * @version $Id$
  */
 public class DatastreamResolverServlet extends HttpServlet
 {
 
   private static Server s_server;
   private static DOManager m_manager;
   private static Hashtable dsRegistry;
   private static int datastreamMediationLimit;
   private static final String HTML_CONTENT_TYPE = "text/html";
   private static Logger logger;
 
   /**
    * <p>Initialize servlet.</p>
    *
    * @throws ServletException If the servet cannot be initialized.
    */
   public void init() throws ServletException
   {
     try
     {
       s_server=Server.getInstance(new File(System.getProperty("fedora.home")));
       logger = new Logger();
       m_manager = (DOManager) s_server.getModule("fedora.server.storage.DOManager");
       String expireLimit = s_server.getParameter("datastreamMediationLimit");
       if (expireLimit == null || expireLimit.equalsIgnoreCase(""))
       {
         logger.logWarning("DatastreamResolverServlet was unable to "
             + "resolve the datastream expiration limit from the configuration "
             + "file.  The expiration limit has been set to 5000 milliseconds.  ");
         datastreamMediationLimit = 5000;
       } else
       {
         datastreamMediationLimit = new Integer(expireLimit).intValue();
         logger.logFinest("datastreamMediationLimit: "
             + datastreamMediationLimit);
       }
     } catch (InitializationException ie) {
       throw new ServletException("Unable to get an instance of Fedora server "
           + "-- " + ie.getMessage());
     } catch (Throwable th)
     {
       String message = "Unable to init DatastreamResolverServlet.  The "
           + "underlying error was a " + th.getClass().getName()
           + "  The message " + "was \"" + th.getMessage() + "\"  ";
       th.printStackTrace();
       logger.logWarning(message);
     }
   }
   
   private static final boolean contains(String[] array, String item) {
   	boolean contains = false;
   	for (int i = 0; i < array.length; i++) {
   		if (array[i].equals(item)) {
   			contains = true;
   			break;
   		}
   	}
   	return contains;
   }
   
   public static final String ACTION_LABEL = "Resolve Datastream";
 
   /**
    * <p>Processes the servlet request and resolves the physical location of
    * the specified datastream.</p>
    *
    * @param request  The servlet request.
    * @param response servlet The servlet response.
    * @throws ServletException If an error occurs that effects the servlet's
    *         basic operation.
    * @throws IOException If an error occurrs with an input or output operation.
    */
   public void doGet(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException
   {
     String id = null;
     String dsPhysicalLocation = null;
     String dsControlGroupType = null;
     MIMETypedStream mimeTypedStream = null;
     DisseminationService ds = null;
     Timestamp keyTimestamp = null;
     Timestamp currentTimestamp = null;
     PrintWriter out = null;
     ServletOutputStream outStream = null;
 
     id = request.getParameter("id").replaceAll("T"," ");
     logger.logFinest("[DatastreamResolverServlet] datastream tempID: " + id);
 
     try
     {
       // Check for required id parameter.
       if (id == null || id.equalsIgnoreCase(""))
       {
         String message = "[DatastreamResolverServlet] No datastream ID "
             + "specified in servlet request:  " + request.getRequestURI()
             + "  .  ";
         logger.logWarning(message);
         response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
         return;
       }
       id=id.replaceAll("T", " ").replaceAll("/", "").trim();
 
       // Get in-memory hashtable of mappings from Fedora server.
       ds = new DisseminationService();
       dsRegistry = DisseminationService.dsRegistry;
       DatastreamMediation dm = (DatastreamMediation)dsRegistry.get(id);
       if (dm==null) {
         StringBuffer entries=new StringBuffer();
         Iterator eIter=dsRegistry.keySet().iterator();
         while (eIter.hasNext()) {
             entries.append("'" + (String) eIter.next() + "' ");
         }
         throw new IOException("Cannot find datastream in temp registry by key: " + id + "\n"
                 + "Reg entries: " + entries.toString());
       }
       dsPhysicalLocation = dm.dsLocation;
       dsControlGroupType = dm.dsControlGroupType;
       keyTimestamp = Timestamp.valueOf(ds.extractTimestamp(id));
       currentTimestamp = new Timestamp(new Date().getTime());
       logger.logFinest("[DatastreamResolverServlet] dsPhysicalLocation: "
           + dsPhysicalLocation);
       logger.logFinest("[DatastreamResolverServlet] dsControlGroupType: "
           + dsControlGroupType);
 
       // Deny mechanism requests that fall outside the specified time interval.
       // The expiration limit can be adjusted using the Fedora config parameter
       // named "datastreamMediationLimit" which is in milliseconds.
       logger.logFiner("[DatastreamResolverServlet] TimeStamp differential "
           + "for Mechanism's response: " + ((long)currentTimestamp.getTime() -
           (long)keyTimestamp.getTime()) + " milliseconds");
       if (currentTimestamp.getTime() - keyTimestamp.getTime() >
           (long) datastreamMediationLimit)
       {
         out = response.getWriter();
         response.setContentType(HTML_CONTENT_TYPE);
         out.println("<br><b>[DatastreamResolverServlet] Error:</b>"
             + "<font color=\"red\"> Mechanism has failed to respond "
             + "to the DatastreamResolverServlet within the specified "
             + "time limit of \"" + datastreamMediationLimit + "\""
             + "milliseconds. Datastream access denied.");
         logger.logWarning("[DatastreamResolverServlet] Error: "
             + "Mechanism has failed to respond "
             + "to the DatastreamResolverServlet within the specified "
             + "time limit of  \"" + datastreamMediationLimit + "\""
             + "  milliseconds. Datastream access denied.");
         out.close();
         return;
       }
 
       if (dm.callbackRole == null) {
           throw new AuthzOperationalException("no callbackRole for this ticket");
       } 
      String targetRole = dm.callbackRole; //restrict access to role of this ticket
       String[] targetRoles = {targetRole};
       Context context = ReadOnlyContext.getContext(Constants.HTTP_REQUEST.REST.uri, request, targetRoles);
       
       if (request.getRemoteUser() == null) {
       	//non-authn:  must accept target role of ticket
       } else {
       	//authn:  check user roles for target role of ticket
       	if  (((request.getUserPrincipal() != null) 
           	&&  (request.getUserPrincipal() instanceof GenericPrincipal)
           	&&  (((GenericPrincipal)request.getUserPrincipal()).getRoles() != null)
           	&&  contains(((GenericPrincipal)request.getUserPrincipal()).getRoles(), targetRole))) {			
         //user has target role
       	} else {
       		throw new AuthzDeniedException("wrong user for this ticket");
       	}
       }
 
       if (fedora.server.Debug.DEBUG) {
           System.err.println("debugging backendService role");      
           System.err.println("targetRole=" + targetRole);
           int targetRolesLength = targetRoles.length;
           System.err.println("targetRolesLength=" + targetRolesLength);
           if (targetRolesLength > 0) {
               System.err.println("targetRoles[0]=" + targetRoles[0]);      	
           }
           int nSubjectValues = context.nSubjectValues(targetRole);
           System.err.println("nSubjectValues=" + nSubjectValues);
           if (nSubjectValues > 0) {
           	System.err.println("context.getSubjectValue(targetRole)=" + context.getSubjectValue(targetRole));   
           }
           Iterator it = context.subjectAttributes();
           while (it.hasNext()) {
           	String name = (String) it.next();
           	String value = context.getSubjectValue(name);
             System.err.println("another subject attribute from context " + name + "=" + value);            	
           }          
       }
       Authorization authorization = (Authorization)s_server.getModule("fedora.server.security.Authorization");
       authorization.enforceResolveDatastream(context, keyTimestamp);
 
       if (dsControlGroupType.equalsIgnoreCase("E"))
       {
         // testing to see what's in request header that might be of interest
       	if (fedora.server.Debug.DEBUG) {
       	  for (Enumeration e= request.getHeaderNames(); e.hasMoreElements();) {
       	  	String name = (String)e.nextElement();
       	  	Enumeration headerValues =  request.getHeaders(name);
       	  	StringBuffer sb = new StringBuffer();
       	  	while (headerValues.hasMoreElements()) {
       	  	  sb.append((String) headerValues.nextElement());
       	  	}
       	  	String value = sb.toString();
       	  	System.out.println("DATASTREAMRESOLVERSERVLET REQUEST HEADER CONTAINED: "+name+" : "+value); 		
       	  }
       	}
 
         // Datastream is ReferencedExternalContent so dsLocation is a URL string
         ExternalContentManager externalContentManager =
             (ExternalContentManager)s_server.getModule(
             "fedora.server.storage.ExternalContentManager");
         mimeTypedStream =
             externalContentManager.getExternalContent(dsPhysicalLocation,
             ReadOnlyContext.getContext(Constants.HTTP_REQUEST.REST.uri, request));
         outStream = response.getOutputStream();
         response.setContentType(mimeTypedStream.MIMEType);
         Property[] headerArray = mimeTypedStream.header;
         if(headerArray != null) {
           for(int i=0; i<headerArray.length; i++) {
               if(headerArray[i].name != null && !(headerArray[i].name.equalsIgnoreCase("content-type"))) {
                   response.addHeader(headerArray[i].name, headerArray[i].value);
                   if (fedora.server.Debug.DEBUG) System.out.println("THIS WAS ADDED TO DATASTREAMRESOLVERSERVLET RESPONSE HEADER FROM ORIGINATING PROVIDER "+headerArray[i].name+" : "+headerArray[i].value);
               }
           }
         }
         int byteStream = 0;
         byte[] buffer = new byte[255];
         while ( (byteStream = mimeTypedStream.getStream().read(buffer)) != -1)
         {
           outStream.write(buffer, 0, byteStream);
         }
         buffer = null;
       } else if (dsControlGroupType.equalsIgnoreCase("M") ||
                  dsControlGroupType.equalsIgnoreCase("X"))
       {
         // Datastream is either XMLMetadata or ManagedContent so dsLocation
         // is in the form of an internal Fedora ID using the syntax:
         // PID+DSID+DSVersID; parse the ID and get the datastream content.
         String PID = null;
         String dsVersionID = null;
         String dsID = null;
         String[] s = dsPhysicalLocation.split("\\+");
         if (s.length != 3)
         {
           String message = "[DatastreamResolverServlet]  The "
               + "internal Fedora datastream id:  \"" + dsPhysicalLocation
               + "\"  is invalid.";
           logger.logWarning(message);
           throw new ServletException(message);
         }
         PID = s[0];
         dsID = s[1];
         dsVersionID = s[2];
         logger.logFinest("[DatastreamResolverServlet] PID: " + PID
             + " -- dsID: " + dsID + " -- dsVersionID: " + dsVersionID);
 
         DOReader doReader =  m_manager.getReader(Server.USE_DEFINITIVE_STORE, context, PID);
         Datastream d =
             (Datastream) doReader.getDatastream(dsID, dsVersionID);
         logger.logFinest("[DatastreamResolverServlet] Got datastream: "
             + d.DatastreamID);
         InputStream is = d.getContentStream();
         int bytestream = 0;
         response.setContentType(d.DSMIME);
         outStream = response.getOutputStream();
         byte[] buffer = new byte[255];
         while ((bytestream = is.read(buffer)) != -1)
         {
           outStream.write(buffer, 0, bytestream);
         }
         buffer = null;
         is.close();
       } else
       {
         out = response.getWriter();
         response.setContentType(HTML_CONTENT_TYPE);
         out.println("<br>[DatastreamResolverServlet] Unknown "
             + "dsControlGroupType: " + dsControlGroupType + "</br>");
         logger.logWarning("[DatastreamResolverServlet] Unknown "
             + "dsControlGroupType: " + dsControlGroupType);
       }
 	} catch (AuthzException ae) {            
         throw RootException.getServletException (ae, request, ACTION_LABEL, new String[0]);	     
     } catch (Throwable th)
     {
       String message = "[DatastreamResolverServlet] returned an error. The "
           + "underlying error was a  \"" + th.getClass().getName()
           + "  The message was  \"" + th.getMessage() + "\".  ";
       th.printStackTrace();
       logger.logWarning(message);
       throw new ServletException(message);
     } finally
     {
       if (out != null) out.close();
       if (outStream != null) outStream.close();
 	  dsRegistry.remove(id);
     }
   }
 
   //Clean up resources
   public void destroy()
   {}
 
 }
