 package proai.service;
 
 import java.io.*;
 import java.util.*;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 
 import proai.error.BadArgumentException;
 import proai.error.BadVerbException;
 import proai.error.ProtocolException;
 import proai.error.ServerException;
 import proai.util.StreamUtil;
 
 public class ProviderServlet extends HttpServlet {
 
     private static final Logger logger =
             Logger.getLogger(ProviderServlet.class.getName());
 
     /** Every response starts with this string. */
     private static final String _XMLSTART = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                                          + "<OAI-PMH xmlns=\"http://www.openarchives.org/OAI/2.0/\"\n"
                                          + "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                                          + "         xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/\n"
                                          + "                             http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd\">\n"
                                          + "  <responseDate>";
 
     /**
      * Entry point for handling OAI requests.
      */
     public void doGet(HttpServletRequest request, 
                       HttpServletResponse response)  {
 
         if (logger.isDebugEnabled()) {
             StringBuffer buf = new StringBuffer();
             buf.append("Started servicing request ( ");
             Map map = request.getParameterMap();
             Iterator nameIter = map.keySet().iterator();
             while (nameIter.hasNext()) {
                 String parmName = (String) nameIter.next();
                 String[] parmVals = (String[]) map.get(parmName);
                 buf.append(parmName + "=" + parmVals[0] + " ");
             }
             buf.append(") from " + request.getRemoteAddr());
             logger.debug(buf.toString());
         }
 
         String url = null;
         String verb = null;
         String identifier = null;
         String from = null;
         String until = null;
         String metadataPrefix = null;
         String set = null;
         String resumptionToken = null;
         try {
             url = request.getRequestURL().toString();
             verb = request.getParameter("verb");
            if (verb == null) throw new BadArgumentException("must specify a verb");
             identifier = request.getParameter("identifier");
             from = request.getParameter("from");
             until = request.getParameter("until");
             metadataPrefix = request.getParameter("metadataPrefix");
             set = request.getParameter("set");
             resumptionToken = request.getParameter("resumptionToken");
 
             // die if any other parameters are given, too
             // this is a bit draconian, but required by the spec nonetheless
             Set argKeys = request.getParameterMap().keySet();
             int argCount = argKeys.size() - 1;
             Iterator names = argKeys.iterator();
             while (names.hasNext()) {
                 String n = (String) names.next();
                 if (!n.equals("verb") 
                         && !n.equals("identifier")
                         && !n.equals("from")
                         && !n.equals("until")
                         && !n.equals("metadataPrefix")
                         && !n.equals("set")
                         && !n.equals("resumptionToken")) {
                     throw new BadArgumentException("unknown argument: " + n);
                 }
             }
 
             ResponseData data = null;
             try {
                 if (verb.equals("GetRecord")) {
                     if (argCount != 2) throw new BadArgumentException("two arguments needed, got " + argCount);
                     data = m_responder.getRecord(identifier, metadataPrefix);
                 } else if (verb.equals("Identify")) {
                     if (argCount != 0) throw new BadArgumentException("zero arguments needed, got " + argCount);
                     data = m_responder.identify();
                 } else if (verb.equals("ListIdentifiers")) {
                     if (identifier != null) throw new BadArgumentException("identifier argument is not valid for this verb");
                     data = m_responder.listIdentifiers(from, until, metadataPrefix, set, resumptionToken);
                 } else if (verb.equals("ListMetadataFormats")) {
                     if (argCount > 1) throw new BadArgumentException("one or zero arguments needed, got " + argCount);
                     data = m_responder.listMetadataFormats(identifier);
                 } else if (verb.equals("ListRecords")) {
                     if (identifier != null) throw new BadArgumentException("identifier argument is not valid for this verb");
                     data = m_responder.listRecords(from, until, metadataPrefix, set, resumptionToken);
                 } else if (verb.equals("ListSets")) {
                     if (argCount > 1) throw new BadArgumentException("one or zero arguments needed, got " + argCount);
                     data = m_responder.listSets(resumptionToken);
                 } else {
                    throw new BadArgumentException("bad verb: " + verb);
                 }
                 response.setStatus(HttpServletResponse.SC_OK);
                 response.setContentType("text/xml; charset=UTF-8");
                 PrintWriter writer = response.getWriter();
                 writer.print(getResponseStart(url, verb, identifier, from, until, metadataPrefix, set, resumptionToken, null));
                 data.write(response.getWriter());
                 writer.println("</OAI-PMH>");
                 writer.flush();
                 writer.close();
             } finally {
                 if (data != null) {
                     try {
                         data.release();
                     } catch (ServerException e) {
                         logger.warn("Could not release response data", e);
                     }
                 }
             }
         } catch (ProtocolException e) {
             sendProtocolException(getResponseStart(url,
                                                    verb,
                                                    identifier,
                                                    from,
                                                    until,
                                                    metadataPrefix,
                                                    set,
                                                    resumptionToken,
                                                    e),
                                    e, response);
         } catch (ServerException e) {
             try {
                 logger.warn("OAI Service Error", e);
                 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "OAI Service Error");
             } catch (IOException ioe) {
                 logger.warn("Could not send error to client", ioe);
             }
         } catch (Throwable th) {
             try {
                 logger.warn("Unexpected Error", th);
                 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error");
             } catch (IOException ioe) {
                 logger.warn("Could not send error to client", ioe);
             }
         } finally {
             if (logger.isDebugEnabled()) {
                 logger.debug("Finished servicing request from " + request.getRemoteAddr());
             }
         }
     }
 
     private String getResponseStart(String url,
                                     String verb,
                                     String identifier,
                                     String from,
                                     String until,
                                     String metadataPrefix,
                                     String set,
                                     String resumptionToken,
                                     ProtocolException e) { // normally null
         boolean doParams = true;
         if (verb == null) doParams = false;
         if (e != null 
             && ( e instanceof BadVerbException 
               || e instanceof BadArgumentException)) doParams = false;
 
         StringBuffer buf = new StringBuffer();
         buf.append(_XMLSTART);
         buf.append(StreamUtil.nowUTCString());
         buf.append("</responseDate>\n");
         buf.append("  <request");
         if (doParams) {
             appendAttribute("verb", verb, buf);
             appendAttribute("identifier", identifier, buf);
             appendAttribute("from", from, buf);
             appendAttribute("until", until, buf);
             appendAttribute("metadataPrefix", metadataPrefix, buf);
             appendAttribute("set", set, buf);
             appendAttribute("resumptionToken", resumptionToken, buf);
         }
         buf.append(">" + url + "</request>\n");
         return buf.toString();
     }
 
     private static void appendAttribute(String name, String value, StringBuffer buf) {
         if (value != null) {
             buf.append(" " + name + "=\"");
             buf.append(StreamUtil.xmlEncode(value));
             buf.append("\"");
         }
     }
 
     private void sendProtocolException(String responseStart,
                                        ProtocolException e,
                                        HttpServletResponse response) {
         try {
             response.setStatus(HttpServletResponse.SC_OK);
             response.setContentType("text/xml; charset=UTF-8");
             PrintWriter writer = response.getWriter();
             writer.print(responseStart);
             writer.print("  <error code=\"" + e.getCode() + "\">");
             if (e.getMessage() != null) writer.print(StreamUtil.xmlEncode(e.getMessage()));
             writer.println("</error>");
             writer.println("</OAI-PMH>");
             writer.flush();
             writer.close();
         } catch (Throwable th) {
             logger.warn("Error while sending a protocol exception (" + e.getClass().getName() + ") response", th);
         }
     }
 
     private void sendBusy(HttpServletResponse response) {
         try {
             response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
             response.setHeader("Retry-After", "" + m_retrySeconds);
             response.setContentType("text/plain; charset=UTF-8");
             PrintWriter writer = response.getWriter();
             writer.println("The OAI provider is too busy.  Try again in " + m_retrySeconds + " seconds.");
             writer.flush();
             writer.close();
         } catch (Throwable th) {
             logger.warn("Error while sending SERVICE_UNAVAILABLE (503) response", th);
         }
     }
 
     public void doPost(HttpServletRequest request, 
                        HttpServletResponse response)  {
         doGet(request, response);
     }
 
     private int m_retrySeconds;
 
     private Responder m_responder;
 
     public void init() throws ServletException {
         try {
             InputStream propStream = this.getClass().getResourceAsStream("/proai.properties");
             if (propStream == null) {
                 throw new IOException("Error loading configuration: /proai.properties not found in classpath");
             }
             Properties props = new Properties();
             props.load(propStream);
             init(props);
         } catch (Exception e) {
             throw new ServletException("Unable to initialize ProviderServlet", e);
         }
     }
 
     public void init(Properties props) throws ServerException {
         m_responder = new Responder(props);
         m_retrySeconds = 60;
     }
 
     /**
      * Close the Responder at shutdown-time.
      *
      * This makes a best-effort attempt to properly close any resources
      * (db connections, threads, etc) that are being held.
      */
     public void destroy() {
         try {
             m_responder.close();
         } catch (Exception e) {
             logger.warn("Error trying to close Responder", e);
         }
     }
 
}
