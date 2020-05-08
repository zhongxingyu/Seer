 package fedora.server.access;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.PipedReader;
 import java.io.PipedWriter;
 import java.net.URLDecoder;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Properties;
 
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.transform.Templates;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 
 import fedora.server.Context;
 import fedora.server.ReadOnlyContext;
 import fedora.server.Server;
 import fedora.server.errors.InitializationException;
 import fedora.server.errors.GeneralException;
 import fedora.server.errors.ServerException;
 import fedora.server.errors.StreamIOException;
 import fedora.server.utilities.Logger;
 
 /**
  * <p><b>Title: </b>GetObjectHistoryServlet.java</p>
  * <p><b>Description: </b>Implements the "getObjectHistory" functionality
  * of the Fedora Access LITE (API-A-LITE) interface using a
  * java servlet front end. The syntax defined by API-A-LITE has for getting
  * a description of the repository has the following binding:
  * <ol>
  * <li>getObjectHistory URL syntax:
  * http://hostname:port/fedora/getObjectHistory/pid[?xml=BOOLEAN]
  * This syntax requests information about the repository.
  * The xml parameter determines the type of output returned.
  * If the parameter is omitted or has a value of "false", a MIME-typed stream
  * consisting of an html table is returned providing a browser-savvy means
  * of viewing the object profile. If the value specified is "true", then
  * a MIME-typed stream consisting of XML is returned.</li>
  * <ul>
  * <li>hostname - required hostname of the Fedora server.</li>
  * <li>port - required port number on which the Fedora server is running.</li>
  * <li>fedora - required name of the Fedora access service.</li>
  * <li>getObjectHistory - required verb of the Fedora service.</li>
  * <li>pid - the persistent identifier of the digital object.
  * <li>xml - an optional parameter indicating the requested output format.
  *           A value of "true" indicates a return type of text/xml; the
  *           absence of the xml parameter or a value of "false"
  *           indicates format is to be text/html.</li>
  * </ul>
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
 public class GetObjectHistoryServlet extends HttpServlet
 {
   /** Content type for html. */
   private static final String CONTENT_TYPE_HTML = "text/html; charset=UTF-8";
 
   /** Content type for xml. */
   private static final String CONTENT_TYPE_XML  = "text/xml; charset=UTF-8";
 
   /** Instance of the Fedora server. */
   private static Server s_server = null;
 
   /** Instance of the access subsystem. */
   private static Access s_access = null;
 
   /** Instance of URLDecoder */
   private URLDecoder decoder = new URLDecoder();
 
   /** Host name of the Fedora server **/
   private static String fedoraServerHost = null;
 
   /** Port number on which the Fedora server is running. **/
   private static String fedoraServerPort = null;
 
   /** Instance of Logger to log servlet events in Fedora server log */
   private static Logger logger = null;
 
   /**
    * <p>Process Fedora Access Request. Parse and validate the servlet input
    * parameters and then execute the specified request.</p>
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
     String PID = null;
     boolean xml = false;
 
     HashMap h=new HashMap();
     h.put("application", "apia");
     h.put("useCachedObject", "true");
     h.put("userId", "fedoraAdmin");
     h.put("host", request.getRemoteAddr());
     ReadOnlyContext context = new ReadOnlyContext(h);
 
     // Parse servlet URL.
     String[] URIArray = request.getRequestURL().toString().split("/");
     if (URIArray.length != 6 || !URIArray[4].equals("getObjectHistory"))
     {
       // Bad syntax; redirect to syntax documentation page.
       response.sendRedirect("/userdocs/client/browser/apialite/index.html");
       return;
     }
 
     PID = URIArray[5];
     logger.logFinest("[GetObjectHistoryServlet] getObjectHistory Syntax "
         + "Encountered: "+ request.getRequestURL().toString() + "?"
         + request.getQueryString());
 
     // Check for xml encoding parameter; ignore any other parameters
     Hashtable h_userParms = new Hashtable();
     for ( Enumeration e = request.getParameterNames(); e.hasMoreElements();)
     {
         String name = decoder.decode((String)e.nextElement(), "UTF-8");
         String value = decoder.decode(request.getParameter(name), "UTF-8");
         if (name.equalsIgnoreCase("xml"))
         {
             xml = new Boolean(request.getParameter(name)).booleanValue();
         }
         h_userParms.put(name,value);
     }
 
     try
     {
         getObjectHistory(context, PID, xml, response);
     } catch (Throwable th)
       {
         String message = "[GetObjectHistoryServlet] An error has occured in "
             + "accessing the Fedora Access Subsystem. The error was \" "
             + th.getClass().getName()
             + " \". Reason: "  + th.getMessage()
             + "  Input Request was: \"" + request.getRequestURL().toString();
         displayURLParms(PID, h_userParms, response, message);
         logger.logWarning(message);
     }
   }
 
   public void getObjectHistory(Context context, String PID, boolean xml,
     HttpServletResponse response) throws ServerException
   {
 
     OutputStreamWriter out = null;
     String[] objectHistory = new String[0];
     PipedWriter pw = null;
     PipedReader pr = null;
 
     try
     {
       pw = new PipedWriter();
       pr = new PipedReader(pw);
       objectHistory = s_access.getObjectHistory(context, PID);
       if (objectHistory.length > 0)
       {
         // Object history.
         // Serialize the ObjectHistory object into XML
         new ObjectHistorySerializerThread(objectHistory, PID, pw).start();
         if (xml)
         {
           // Return results as raw XML
           response.setContentType(CONTENT_TYPE_XML);
 
           // Insures stream read from PipedReader correctly translates utf-8
           // encoded characters to OutputStreamWriter.
           out = new OutputStreamWriter(response.getOutputStream(),"UTF-8");
           int bufSize = 4096;
           char[] buf=new char[bufSize];
           int len=0;
           while ( (len = pr.read(buf, 0, bufSize)) != -1) {
               out.write(buf, 0, len);
           }
           out.flush();
         } else
         {
           // Transform results into an html table
           response.setContentType(CONTENT_TYPE_HTML);
           out = new OutputStreamWriter(response.getOutputStream(),"UTF-8");
           File xslFile = new File(s_server.getHomeDir(), "access/viewObjectHistory.xslt");
           TransformerFactory factory = TransformerFactory.newInstance();
           Templates template = factory.newTemplates(new StreamSource(xslFile));
           Transformer transformer = template.newTransformer();
           Properties details = template.getOutputProperties();
           transformer.transform(new StreamSource(pr), new StreamResult(out));
         }
         out.flush();
 
       } else
       {
         // getObjectHistory request returned nothing.
         String message = "[GetObjectHistoryServlet] No object history returned.";
         logger.logInfo(message);
       }
     } catch (Throwable th)
     {
       String message = "[GetObjectHistoryServlet] An error has occured. "
                      + " The error was a \" "
                      + th.getClass().getName()
                      + " \". Reason: "  + th.getMessage();
       logger.logWarning(message);
       throw new GeneralException(message);
     } finally
     {
       try
       {
         if (pr != null) pr.close();
         if (out != null) out.close();
       } catch (Throwable th)
       {
         String message = "[GetObjectHistoryServlet] An error has occured. "
                        + " The error was a \" "
                        + th.getClass().getName()
                      + " \". Reason: "  + th.getMessage();
         throw new StreamIOException(message);
       }
     }
   }
 
   /**
    * <p> A Thread to serialize an ObjectProfile object into XML.</p>
    *
    */
   public class ObjectHistorySerializerThread extends Thread
   {
     private PipedWriter pw = null;
     private String[] objectHistory = new String[0];
     private String PID = null;
 
     /**
      * <p> Constructor for ObjectHistorySerializerThread.</p>
      *
      * @param objectHistory An object history data structure.
      * @param pw A PipedWriter to which the serialization info is written.
      */
     public ObjectHistorySerializerThread(String[] objectHistory, String PID, PipedWriter pw)
     {
       this.pw = pw;
       this.objectHistory = objectHistory;
       this.PID = PID;
     }
 
     /**
      * <p> This method executes the thread.</p>
      */
     public void run()
     {
       if (pw != null)
       {
         try
         {
           pw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
           pw.write("<fedoraObjectHistory "
               + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
               + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
               + " xsi:schemaLocation=\"http://www.fedora.info/definitions/1/0/access/"
               + " http://" + fedoraServerHost + ":" + fedoraServerPort
               + "/fedoraObjectHistory.xsd\" pid=\"" + PID + "\" >");
 
           // Object History Serialization
           for (int i=0; i<objectHistory.length; i++)
           {
               pw.write("<objectChangeDate>" + objectHistory[i] + "</objectChangeDate>");
           }
           pw.write("</fedoraObjectHistory>");
           pw.flush();
           pw.close();
         } catch (IOException ioe) {
           System.err.println("WriteThread IOException: " + ioe.getMessage());
         } finally
         {
           try
           {
             if (pw != null) pw.close();
           } catch (IOException ioe)
           {
             System.err.println("WriteThread IOException: " + ioe.getMessage());
           }
         }
       }
     }
   }
 
   /**
    * <p>For now, treat a HTTP POST request just like a GET request.</p>
    *
    * @param request The servet request.
    * @param response The servlet response.
    * @throws ServletException If thrown by <code>doGet</code>.
    * @throws IOException If thrown by <code>doGet</code>.
    */
   public void doPost(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException
   {
     doGet(request, response);
   }
 
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
       fedoraServerHost = s_server.getParameter("fedoraServerHost");
       fedoraServerPort = s_server.getParameter("fedoraServerPort");
       s_access = (Access) s_server.getModule("fedora.server.access.Access");
      logger = new Logger();
     } catch (InitializationException ie)
     {
       throw new ServletException("Unable to get Fedora Server instance."
           + ie.getMessage());
     }
   }
 
   /**
    * <p>Cleans up servlet resources.</p>
    */
   public void destroy()
   {}
 
   /**
    * <p>Displays a list of the servlet input parameters. This method is
    * generally called when a service request returns no data. Usually
    * this is a result of an incorrect spelling of either a required
    * URL parameter or in one of the user-supplied parameters. The output
    * from this method can be used to help verify the URL parameters
    * sent to the servlet and hopefully fix the problem.</p>
    *
    * @param PID The persistent identifier of the digital object.
    * @param bDefPID The persistent identifier of the Behavior Definition object.
    * @param methodName the name of the method.
    * @param asOfDateTime The version datetime stamp of the digital object.
    * @param userParms An array of user-supplied method parameters and values.
    * @param response The servlet response.
    * @param message The message text to include at the top of the output page.
    * @throws IOException If an error occurrs with an input or output operation.
    */
   private void displayURLParms(String PID, Hashtable h_userParms,
                            HttpServletResponse response,
                            String message)
       throws IOException
   {
     response.setContentType(CONTENT_TYPE_HTML);
     ServletOutputStream out = response.getOutputStream();
 
     // Display servlet input parameters
     StringBuffer html = new StringBuffer();
     html.append("<html>");
     html.append("<head>");
     html.append("<title>GetObjectHistoryServlet</title>");
     html.append("</head>");
     html.append("<body>");
     html.append("<br></br><font size='+2'>" + message + "</font>");
     html.append("<br></br><font color='red'>Request Parameters</font>");
     html.append("<br></br>");
     html.append("<table cellpadding='5'>");
     html.append("<tr>");
     html.append("<td><font color='red'>PID</td>");
     html.append("<td> = <td>" + PID + "</td>");
     html.append("</tr>");
     html.append("<tr>");
     html.append("</tr>");
     html.append("<tr>");
     html.append("<td colspan='5'><font size='+1' color='blue'>"+
                 "Other Parameters Found:</font></td>");
     html.append("</tr>");
     html.append("<tr>");
     html.append("</tr>");
 
     // List parameters if any
     if (h_userParms.size() != 0)
     {
         for (Enumeration e = h_userParms.keys(); e.hasMoreElements();)
         {
             String parmName = (String) e.nextElement();
             String parmValue = (String) h_userParms.get(parmName);
             html.append("<tr>");
             html.append("<td><font color='red'>" + parmName
                         + "</font></td>");
             html.append("<td> = </td>");
             html.append("<td>" + parmValue + "</td>");
             html.append("</tr>");
             logger.logFinest("parmName: " + parmName
                 + " parmValue: " + parmValue);
         }
     }
     html.append("</table></center></font>");
     html.append("</body></html>");
     out.println(html.toString());
     html = null;
   }
 
   private Server getServer() {
       return s_server;
   }
 
 }
