 package fedora.server.management;
 
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
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Properties;
 
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.transform.Templates;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 
 import com.icl.saxon.expr.StringValue;
 
 import fedora.server.Context;
 import fedora.server.Logging;
 import fedora.server.ReadOnlyContext;
 import fedora.server.Server;
 import fedora.server.errors.InitializationException;
 import fedora.server.errors.GeneralException;
 import fedora.server.errors.ServerException;
 import fedora.server.errors.StreamIOException;
 
 /**
  * <p><b>Title: </b>DescribeRepositoryServlet.java</p>
  * <p><b>Description: </b>Implements the "getNextPID" functionality
  * of the Fedora Management LITE (API-M-LITE) interface using a
  * java servlet front end. The syntax defined by API-M-LITE for getting
  * a list of the next available PIDs has the following binding:
  * <ol>
  * <li>getNextPID URL syntax:
  * http://hostname:port/fedora/management/getNextPID[?numPIDs=NUMPIDS&namespace=NAMESPACE&xml=BOOLEAN]
  * This syntax requests a list of next available PIDS. The parameter numPIDs
  * determines the number of requested PIDS to generate. If omitted, numPIDs
  * defaults to 1. The namespace parameter determines the namespace to be used in
  * generating the PIDs. If omitted, namespace defaults to the namespace defined
  * in the fedora.fcfg configuration file for the parameter pidNamespace.
  * The xml parameter determines the type of output returned.
  * If the parameter is omitted or has a value of "false", a MIME-typed stream
  * consisting of an html table is returned providing a browser-savvy means
  * of viewing the object profile. If the value specified is "true", then
  * a MIME-typed stream consisting of XML is returned.</li>
  * <ul>
  * <li>hostname - required hostname of the Fedora server.</li>
  * <li>port - required port number on which the Fedora server is running.</li>
  * <li>fedora - required name of the Fedora access service.</li>
  * <li>describe - required verb of the Fedora service.</li>
  * <li>numPIDs - an optional parameter indicating the number of PIDs to be
  *               generated. If omitted, it defaults to 1.</li>
  * <li>namespace - an optional parameter indicating the namesapce to be used
  *                 in generating the PIDs. If omitted, it defaults to the
  *                 namespace defined in the <code>fedora.fcfg</code>
  *                 configuration file for the parameter pidNamespace.</li>
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
 public class GetNextPIDServlet extends HttpServlet implements Logging
 {
   /** Content type for html. */
   private static final String CONTENT_TYPE_HTML = "text/html; charset=UTF-8";
 
   /** Content type for xml. */
   private static final String CONTENT_TYPE_XML  = "text/xml; charset=UTF-8";
 
   /** Instance of the Fedora server. */
   private static Server s_server = null;
 
   /** Instance of the Management subsystem. */
   private static Management s_management = null;
 
   /** Instance of URLDecoder */
   private URLDecoder decoder = new URLDecoder();
 
   private static String s_serverHost = null;
   private static String s_serverPort = null;
 
   /**
    * <p>Process the Fedora API-M-LITE request to generate a list of next
    * available PIDs. Parse and validate the servlet input parameters and then
    * execute the specified request.</p>
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
     boolean xml = false;
     int numPIDs = 1;
     String namespace = null;
 
     HashMap h=new HashMap();
     h.put("application", "apim");
     h.put("useCachedObject", "false");
     h.put("userId", "fedoraAdmin");
     h.put("host", request.getRemoteAddr());
     ReadOnlyContext context = new ReadOnlyContext(h);
 
     // Get optional supplied parameters.
     for ( Enumeration e = request.getParameterNames(); e.hasMoreElements();)
     {
       String name = decoder.decode((String)e.nextElement(), "UTF-8");
       if (name.equalsIgnoreCase("xml"))
       {
         xml = new Boolean(request.getParameter(name)).booleanValue();
       }
       if (name.equalsIgnoreCase("numPIDs"))
       {
         numPIDs = new Integer(decoder.decode(request.getParameter(name), "UTF-8")).intValue();
       }
       if (name.equalsIgnoreCase("namespace"))
       {
         namespace = decoder.decode(request.getParameter(name), "UTF-8");
       }
     }
 
     try
     {
       getNextPID(context, numPIDs, namespace, xml, response);
     } catch (Throwable th)
       {
         String message = "[GetNextPIDServlet] An error has occured in "
             + "accessing the Fedora Management Subsystem. The error was \" "
             + th.getClass().getName()
             + " \". Reason: "  + th.getMessage()
             + "  Input Request was: \"" + request.getRequestURL().toString();
         logWarning(message);
         th.printStackTrace();
     }
   }
 
   /**
    * <p> Get the requested list of next Available PIDs by invoking the
    * approriate method from the Management subsystem.</p>
    *
    * @param context The context of this request.
    * @param numPIDs The number of PIDs requested.
    * @param namespace The namespace of the requested PIDs.
    * @param xml Boolean that determines format of response; true indicates
    *            response format is xml; false indicates response format
    *            is html.
    * @param response The servlet response.
    * @throws ServerException If an error occurred while accessing the Fedora
    *                         Management subsystem.
    */
   public void getNextPID(Context context, int numPIDs, String namespace,
         boolean xml, HttpServletResponse response) throws ServerException
   {
 
     OutputStreamWriter out = null;
     PipedWriter pw = null;
     PipedReader pr = null;
 
     try
     {
       pw = new PipedWriter();
       pr = new PipedReader(pw);
       String[] pidList = s_management.getNextPID(context, numPIDs, namespace);
       if (pidList.length > 0)
       {
         // Repository info obtained.
         // Serialize the RepositoryInfo object into XML
         new GetNextPIDSerializerThread(pidList, pw).start();
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
           File xslFile = new File(s_server.getHomeDir(), "management/getNextPIDInfo.xslt");
           TransformerFactory factory = TransformerFactory.newInstance();
           Templates template = factory.newTemplates(new StreamSource(xslFile));
           Transformer transformer = template.newTransformer();
           Properties details = template.getOutputProperties();
           transformer.transform(new StreamSource(pr), new StreamResult(out));
         }
         out.flush();
 
       } else
       {
         // GetNextPID request returned no PIDs.
         String message = "[GetNextPIDServlet] No PIDs returned.";
         logInfo(message);
       }
     } catch (Throwable th)
     {
       String message = "[GetNextPIDServlet] An error has occured. "
                      + " The error was a \" "
                      + th.getClass().getName()
                      + " \". Reason: "  + th.getMessage();
       logWarning(message);
       th.printStackTrace();
       throw new GeneralException(message);
     } finally
     {
       try
       {
         if (pr != null) pr.close();
         if (out != null) out.close();
       } catch (Throwable th)
       {
         String message = "[GetNextPIDServlet] An error has occured. "
                        + " The error was a \" "
                        + th.getClass().getName()
                      + " \". Reason: "  + th.getMessage();
         throw new StreamIOException(message);
       }
     }
   }
 
   /**
    * <p> A Thread to serialize an array of PIDs into XML.</p>
    *
    */
   public class GetNextPIDSerializerThread extends Thread
   {
     private PipedWriter pw = null;
     private String[] pidList = null;
 
     /**
      * <p> Constructor for GetNextPIDSerializerThread.</p>
      *
      * @param pidList An array of the requested next available PIDs.
      * @param pw A PipedWriter to which the serialization info is written.
      */
     public GetNextPIDSerializerThread(String[] pidList, PipedWriter pw)
     {
       this.pw = pw;
       this.pidList = pidList;
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
           pw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
           pw.write("<pidList "
               + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
               + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
               + " xsi:schemaLocation=\"http://www.fedora.info/definitions/1/0/management/"
               + " http://" + s_serverHost + ":" + s_serverPort
              + "/ getNextPIDInfo.xsd\">\n");
 
           // PID array serialization
           for (int i=0; i<pidList.length; i++) {
           pw.write("  <pid>" + pidList[i] + "</pid>\n");
           }
           pw.write("</pidList>\n");
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
       s_management = (Management) s_server.getModule("fedora.server.management.Management");
       s_serverHost = s_server.getParameter("fedoraServerHost");
       s_serverPort = s_server.getParameter("fedoraServerPort");
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
    * <p>Get an instance of the Fedora server.</p>
    *
    * @return An instance of the Fedora server.
    */
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
