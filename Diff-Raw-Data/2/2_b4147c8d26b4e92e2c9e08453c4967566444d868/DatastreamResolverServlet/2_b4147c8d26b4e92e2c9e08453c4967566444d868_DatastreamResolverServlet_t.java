 package fedora.server.access.dissemination;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.File;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 
 import fedora.server.Logging;
 import fedora.server.Server;
 import fedora.server.errors.InitializationException;
 import fedora.server.Context;
 import fedora.server.ReadOnlyContext;
 import fedora.server.storage.DOManager;
 import fedora.server.storage.DOReader;
 import fedora.server.storage.ExternalContentManager;
 import fedora.server.storage.types.MIMETypedStream;
 import fedora.server.storage.types.Datastream;
 import fedora.server.storage.types.DatastreamMediation;
 
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
  * datastreamMediationLimit and is specified in milliseconds. If this parameter
  * is not supplied it defaults to 5000 miliseconds.</p>
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
  * <p>The entire file consists of original code.  Copyright &copy; 2002-2004 by The
  * Rector and Visitors of the University of Virginia and Cornell University.
  * All rights reserved.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * @author rlw@virginia.edu
  * @version $Id$
  */
 public class DatastreamResolverServlet extends HttpServlet implements Logging
 {
 
   private static Server s_server;
   private static DOManager m_manager;
   private static Context m_context;
   private static Hashtable dsRegistry;
   private static int datastreamMediationLimit;
   private static final String HTML_CONTENT_TYPE = "text/html";
 
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
       m_manager = (DOManager) getServer().getModule("fedora.server.storage.DOManager");
       String expireLimit = getServer().getParameter("datastreamMediationLimit");
       if (expireLimit == null || expireLimit.equalsIgnoreCase(""))
       {
         logWarning("DatastreamResolverServlet was unable to "
             + "resolve the datastream expiration limit from the configuration "
             + "file.  The expiration limit has been set to 5000 milliseconds.  ");
         datastreamMediationLimit = 5000;
       } else
       {
         datastreamMediationLimit = new Integer(expireLimit).intValue();
         logFinest("datastreamMediationLimit: "
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
       logWarning(message);
     }
   }
 
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
 
     // Initialize context.
     HashMap h = new HashMap();
     h.put("application", "apia");
     h.put("useCachedObject", "false");
     h.put("userId", "fedoraAdmin");
     m_context = new ReadOnlyContext(h);
 
     id = request.getParameter("id").replaceAll("T"," ");
     logFinest("[DatastreamResolverServlet] datastream tempID: " + id);
 
     try
     {
       // Check for required id parameter.
       if (id == null || id.equalsIgnoreCase(""))
       {
         String message = "[DatastreamResolverServlet] No datastream ID "
             + "specified in servlet request:  " + request.getRequestURI()
             + "  .  ";
         logWarning(message);
         response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         response.sendError(response.SC_INTERNAL_SERVER_ERROR, message);
         return;
       }
      id=id.replaceAll("T", " ").replaceAll("/", "").trim();
 
       // Get in-memory hashtable of mappings from Fedora server.
       ds = new DisseminationService();
       dsRegistry = ds.dsRegistry;
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
       keyTimestamp = keyTimestamp.valueOf(ds.extractTimestamp(id));
       currentTimestamp = new Timestamp(new Date().getTime());
       logFinest("[DatastreamResolverServlet] dsPhysicalLocation: "
           + dsPhysicalLocation);
       logFinest("[DatastreamResolverServlet] dsControlGroupType: "
           + dsControlGroupType);
 
       // Deny mechanism requests that fall outside the specified time interval.
       // The expiration limit can be adjusted using the Fedora config parameter
       // named "datastreamMediationLimit" which is in milliseconds.
       logFiner("[DatastreamResolverServlet] TimeStamp differential "
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
         logWarning("[DatastreamResolverServlet] Error: "
             + "Mechanism has failed to respond "
             + "to the DatastreamResolverServlet within the specified "
             + "time limit of  \"" + datastreamMediationLimit + "\""
             + "  milliseconds. Datastream access denied.");
         out.close();
         return;
       }
 
       if (dsControlGroupType.equalsIgnoreCase("E"))
       {
         // Datastream is ReferencedExternalContent so dsLocation is a URL string
         ExternalContentManager externalContentManager =
             (ExternalContentManager)getServer().getModule(
             "fedora.server.storage.ExternalContentManager");
         mimeTypedStream =
             externalContentManager.getExternalContent(dsPhysicalLocation);
         outStream = response.getOutputStream();
         response.setContentType(mimeTypedStream.MIMEType);
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
           logWarning(message);
           throw new ServletException(message);
         }
         PID = s[0];
         dsID = s[1];
         dsVersionID = s[2];
         logFinest("[DatastreamResolverServlet] PID: " + PID
             + " -- dsID: " + dsID + " -- dsVersionID: " + dsVersionID);
         DOReader doReader =  m_manager.getReader(m_context, PID);
         Datastream d =
             (Datastream) doReader.getDatastream(dsID, dsVersionID);
         logFinest("[DatastreamResolverServlet] Got datastream: "
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
         logWarning("[DatastreamResolverServlet] Unknown "
             + "dsControlGroupType: " + dsControlGroupType);
       }
     } catch (Throwable th)
     {
       String message = "[DatastreamResolverServlet] returned an error. The "
           + "underlying error was a  \"" + th.getClass().getName()
           + "  The message was  \"" + th.getMessage() + "\".  ";
       th.printStackTrace();
       logWarning(message);
       throw new ServletException(message);
     } finally
     {
       if (out != null) out.close();
       if (outStream != null) outStream.close();
     }
   }
 
   //Clean up resources
   public void destroy()
   {}
 
   private Server getServer() {
       return s_server;
   }
 
   /**
    * Logs a SEVERE message, indicating that the server is inoperable or
    * unable to start.
    *
    * @param message The message.
    */
   public final void logSevere(String message) {
       StringBuffer m=new StringBuffer();
       m.append(getClass().getName());
       m.append(": ");
       m.append(message);
       getServer().logSevere(m.toString());
   }
 
   public final boolean loggingSevere() {
       return getServer().loggingSevere();
   }
 
   /**
    * Logs a WARNING message, indicating that an undesired (but non-fatal)
    * condition occured.
    *
    * @param message The message.
    */
   public final void logWarning(String message) {
       StringBuffer m=new StringBuffer();
       m.append(getClass().getName());
       m.append(": ");
       m.append(message);
       getServer().logWarning(m.toString());
   }
 
   public final boolean loggingWarning() {
       return getServer().loggingWarning();
   }
 
   /**
    * Logs an INFO message, indicating that something relatively uncommon and
    * interesting happened, like server or module startup or shutdown, or
    * a periodic job.
    *
    * @param message The message.
    */
   public final void logInfo(String message) {
       StringBuffer m=new StringBuffer();
       m.append(getClass().getName());
       m.append(": ");
       m.append(message);
       getServer().logInfo(m.toString());
   }
 
   public final boolean loggingInfo() {
       return getServer().loggingInfo();
   }
 
   /**
    * Logs a CONFIG message, indicating what occurred during the server's
    * (or a module's) configuration phase.
    *
    * @param message The message.
    */
   public final void logConfig(String message) {
       StringBuffer m=new StringBuffer();
       m.append(getClass().getName());
       m.append(": ");
       m.append(message);
       getServer().logConfig(m.toString());
   }
 
   public final boolean loggingConfig() {
       return getServer().loggingConfig();
   }
 
   /**
    * Logs a FINE message, indicating basic information about a request to
    * the server (like hostname, operation name, and success or failure).
    *
    * @param message The message.
    */
   public final void logFine(String message) {
       StringBuffer m=new StringBuffer();
       m.append(getClass().getName());
       m.append(": ");
       m.append(message);
       getServer().logFine(m.toString());
   }
 
   public final boolean loggingFine() {
       return getServer().loggingFine();
   }
 
   /**
    * Logs a FINER message, indicating detailed information about a request
    * to the server (like the full request, full response, and timing
    * information).
    *
    * @param message The message.
    */
   public final void logFiner(String message) {
       StringBuffer m=new StringBuffer();
       m.append(getClass().getName());
       m.append(": ");
       m.append(message);
       getServer().logFiner(m.toString());
   }
 
   public final boolean loggingFiner() {
       return getServer().loggingFiner();
   }
 
   /**
    * Logs a FINEST message, indicating method entry/exit or extremely
    * verbose information intended to aid in debugging.
    *
    * @param message The message.
    */
   public final void logFinest(String message) {
       StringBuffer m=new StringBuffer();
       m.append(getClass().getName());
       m.append(": ");
       m.append(message);
       getServer().logFinest(m.toString());
   }
 
   public final boolean loggingFinest() {
       return getServer().loggingFinest();
   }
 
 }
