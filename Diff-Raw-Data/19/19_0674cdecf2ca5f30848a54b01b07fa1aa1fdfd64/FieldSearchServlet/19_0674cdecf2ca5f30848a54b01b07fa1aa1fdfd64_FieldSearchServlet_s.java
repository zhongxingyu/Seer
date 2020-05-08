 package fedora.server.access;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletException;
 
 import fedora.server.Logging;
 import fedora.server.ReadOnlyContext;
 import fedora.server.Server;
 import fedora.server.errors.InitializationException;
 import fedora.server.errors.ServerException;
 import fedora.server.search.Condition;
 import fedora.server.search.ObjectFields;
 
 public class FieldSearchServlet 
         extends HttpServlet 
         implements Logging {
         
     /** Instance of the Server */
     private static Server s_server=null;
 
     /** Instance of the access subsystem */
     private static Access s_access=null;
 
     public void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         try {
             HashMap h=new HashMap();
             h.put("application", "apia");
             h.put("useCachedObject", "true");
             h.put("userId", "fedoraAdmin");
             h.put("host", request.getRemoteAddr());
             ReadOnlyContext context = new ReadOnlyContext(h);
 
             String[] fieldsArray=request.getParameterValues("fields");
             String terms=request.getParameter("terms");
             String query=request.getParameter("query");
             String xmlOutput=request.getParameter("xml");
             boolean xml=false;
             if ( (xmlOutput!=null) 
                     && (xmlOutput.toLowerCase().startsWith("t")
                     || xmlOutput.toLowerCase().startsWith("y")) ) {
                 xml=true;
             }
             StringBuffer xmlBuf=new StringBuffer(); 
             StringBuffer html=new StringBuffer();
             if (!xml) {
                 html.append("<form method=\"get\" action=\"/fedora/search\">");
                 html.append("<center><table border=0 cellpadding=8>\n");
                 html.append("<tr><td width=18% valign=top>");
                 html.append("<input type=\"checkbox\" name=\"fields\" value=\"pid\"> Pid<br>");
                 html.append("<input type=\"checkbox\" name=\"fields\" value=\"label\"> Label<br>");
                 html.append("<input type=\"checkbox\" name=\"fields\" value=\"fType\"> Fedora Object Type<br>");
                 html.append("<input type=\"checkbox\" name=\"fields\" value=\"cModel\"> Content Model<br>");
                 html.append("<input type=\"checkbox\" name=\"fields\" value=\"state\"> State<br>");
                 html.append("<input type=\"checkbox\" name=\"fields\" value=\"locker\"> Locking User<br>");
                 html.append("<input type=\"checkbox\" name=\"fields\" value=\"cDate\"> Creation Date<br>");
                 html.append("<input type=\"checkbox\" name=\"fields\" value=\"mDate\"> Last Modified Date<br>");
                 html.append("</td><td width=18% valign=top>");
                 html.append("<input type=\"checkbox\" name=\"fields\" value=\"title\"> Title<br>");
                 html.append("<input type=\"checkbox\" name=\"fields\" value=\"creator\"> Creator<br>");
                 html.append("<input type=\"checkbox\" name=\"fields\" value=\"subject\"> Subject<br>");
                 html.append("<input type=\"checkbox\" name=\"fields\" value=\"description\"> Description<br>");
                 html.append("<input type=\"checkbox\" name=\"fields\" value=\"publisher\"> Publisher<br>");
                 html.append("<input type=\"checkbox\" name=\"fields\" value=\"contributor\"> Contributor<br>");
                 html.append("<input type=\"checkbox\" name=\"fields\" value=\"date\"> Date<br>");
                 html.append("<input type=\"checkbox\" name=\"fields\" value=\"type\"> Type<br>");
                 html.append("<input type=\"checkbox\" name=\"fields\" value=\"format\"> Format<br>");
                 html.append("<input type=\"checkbox\" name=\"fields\" value=\"identifier\"> Identifier<br>");
                 html.append("<input type=\"checkbox\" name=\"fields\" value=\"source\"> Source<br>");
                 html.append("<input type=\"checkbox\" name=\"fields\" value=\"language\"> Language<br>");
                 html.append("<input type=\"checkbox\" name=\"fields\" value=\"relation\"> Relation<br>");
                 html.append("<input type=\"checkbox\" name=\"fields\" value=\"coverage\"> Coverage<br>");
                 html.append("<input type=\"checkbox\" name=\"fields\" value=\"rights\"> Rights<br>");
                 //html.append("Fields: <input type=\"text\" name=\"fields\" size=\"15\"> ");
                 html.append("</td><td width=64% valign=top>");
                 html.append("Simple Query: <input type=\"text\" name=\"terms\" size=\"15\"><p> ");
                 html.append("Advanced Query: <input type=\"text\" name=\"query\" size=\"15\"><p>&nbsp;<p> ");
                 html.append("<input type=\"submit\" value=\"Search\"><p> ");
                 html.append("<font size=-1>Choose the fields you want returned by checking the appropriate boxes.");
                 html.append(" For simple queries, fill in the Simple Query box and press Search.  ");
                 html.append("A simple query consists of one or more keywords, where keywords can contain the * and ? wildcards.");
                 html.append("For advanced queries, fill in the Advanced Query box and press Search.");
                 html.append(" An advanced query is a space-delimited set of conditions.  A condition is of the form <i>propertyOPERATORvalue</i>.");
                 html.append("Valid operators are =, ~ (meaning 'contains' -- this can take wildcards), <, <=, >, or >=.  Conditions may not contain spaces, ");
                 html.append("unless the value is enclosed in single quotes.  For date-based fields, you may use any operator, but if you use the ");
                 html.append("<, <=, >, or >= operators, you must format your date like: <i>yyyy-DD-MMTHH:MM:SS</i>.  Note that the ");
                 html.append("letter T acts as a delimiter between the date and time parts of the string.</font>");
                 html.append("</td></tr></table></center>");
                 html.append("</form>");
                 html.append("<hr size=\"1\">");
             }
             if (fieldsArray!=null && fieldsArray.length>0) {
                 List searchResults;
                 if ((terms!=null) && (terms.length()!=0)) {
                     searchResults=s_access.search(context, fieldsArray, terms);
                 } else {
                     searchResults=s_access.search(context, fieldsArray, Condition.getConditions(query));
                 }
                 if (!xml) {
                     html.append("<center><table width=\"90%\" border=\"0\" cellpadding=\"4\">\n");
                     html.append("<tr>");
                     for (int i=0; i<fieldsArray.length; i++) {
                         html.append("<td bgcolor=\"#333399\" valign=\"top\"><font color=\"#ffffff\"><b>");
                         html.append(fieldsArray[i]);
                         html.append("</b></font></td>");
                     }
                     html.append("</tr>");
                 }
                 SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
                 for (int i=0; i<searchResults.size(); i++) {
                     ObjectFields f=(ObjectFields) searchResults.get(i);
                     if (xml) {
 		        xmlBuf.append("  <result>\n");
                         for (int j=0; j<fieldsArray.length; j++) {
                             String l=fieldsArray[j];
                             if (l.equalsIgnoreCase("pid")) {
                                 appendXML(l, f.getPid(), xmlBuf);
                             } else if (l.equalsIgnoreCase("label")) {
                                 appendXML(l, f.getLabel(), xmlBuf);
                             } else if (l.equalsIgnoreCase("fType")) {
 			        appendXML(l, f.getFType(), xmlBuf);
                             } else if (l.equalsIgnoreCase("cModel")) {
 			        appendXML(l, f.getCModel(), xmlBuf);
                             } else if (l.equalsIgnoreCase("state")) {
 			        appendXML(l, f.getState(), xmlBuf);
                             } else if (l.equalsIgnoreCase("locker")) {
 			        appendXML(l, f.getLocker(), xmlBuf);
                             } else if (l.equalsIgnoreCase("cDate")) {
 			        appendXML(l, f.getCDate(), formatter, xmlBuf);
                             } else if (l.equalsIgnoreCase("mDate")) {
 			        appendXML(l, f.getMDate(), formatter, xmlBuf);
                             } else if (l.equalsIgnoreCase("title")) {
 			        appendXML(l, f.titles(), xmlBuf);
                             } else if (l.equalsIgnoreCase("creator")) {
 			        appendXML(l, f.creators(), xmlBuf);
                             } else if (l.equalsIgnoreCase("subject")) {
 			        appendXML(l, f.subjects(), xmlBuf);
                             } else if (l.equalsIgnoreCase("description")) {
 			        appendXML(l, f.descriptions(), xmlBuf);
                             } else if (l.equalsIgnoreCase("publisher")) {
 			        appendXML(l, f.publishers(), xmlBuf);
                             } else if (l.equalsIgnoreCase("contributor")) {
 			        appendXML(l, f.contributors(), xmlBuf); 
 			    } else if (l.equalsIgnoreCase("date")) {
 			        appendXML(l, f.dates(), xmlBuf); 
                             } else if (l.equalsIgnoreCase("type")) {
 			        appendXML(l, f.types(), xmlBuf);
                             } else if (l.equalsIgnoreCase("format")) {
 			        appendXML(l, f.formats(), xmlBuf);
                             } else if (l.equalsIgnoreCase("identifier")) {
 			        appendXML(l, f.identifiers(), xmlBuf);
                             } else if (l.equalsIgnoreCase("source")) {
 			        appendXML(l, f.sources(), xmlBuf);
                             } else if (l.equalsIgnoreCase("language")) {
 			        appendXML(l, f.languages(), xmlBuf);
                             } else if (l.equalsIgnoreCase("relation")) {
 			        appendXML(l, f.relations(), xmlBuf);
                             } else if (l.equalsIgnoreCase("coverage")) {
 			        appendXML(l, f.coverages(), xmlBuf);
                             } else if (l.equalsIgnoreCase("rights")) {
 			        appendXML(l, f.rights(), xmlBuf);
                             }                        
 			 }
 		         xmlBuf.append("  </result>\n");
 		    } else {
                         html.append("<tr>");
                         for (int j=0; j<fieldsArray.length; j++) {
                             String l=fieldsArray[j];
                             html.append("<td valign=\"top\">");
                             if (l.equalsIgnoreCase("pid")) {
                                 html.append("<a href=\"/fedora/get/");
                                 html.append(f.getPid());
                                 html.append("\">");
                                 html.append(f.getPid());
                                 html.append("</a>");
                             } else if (l.equalsIgnoreCase("label")) {
                                html.append(f.getLabel());
                             } else if (l.equalsIgnoreCase("fType")) {
                                 html.append(f.getFType());
                             } else if (l.equalsIgnoreCase("cModel")) {
                                html.append(f.getCModel());
                             } else if (l.equalsIgnoreCase("state")) {
                                 html.append(f.getState());
                             } else if (l.equalsIgnoreCase("locker")) {
                                html.append(f.getLocker());
                             } else if (l.equalsIgnoreCase("cDate")) {
                                 html.append(formatter.format(f.getCDate()));
                             } else if (l.equalsIgnoreCase("mDate")) {
                                 html.append(formatter.format(f.getMDate()));
                             } else if (l.equalsIgnoreCase("title")) {
                                 html.append(getList(f.titles()));
                             } else if (l.equalsIgnoreCase("creator")) {
                                 html.append(getList(f.creators()));
                             } else if (l.equalsIgnoreCase("subject")) {
                                 html.append(getList(f.subjects()));
                             } else if (l.equalsIgnoreCase("description")) {
                                 html.append(getList(f.descriptions()));
                             } else if (l.equalsIgnoreCase("publisher")) {
                                 html.append(getList(f.publishers()));
                             } else if (l.equalsIgnoreCase("contributor")) {
                                 html.append(getList(f.contributors()));
                             } else if (l.equalsIgnoreCase("date")) {
                                 html.append(getList(f.dates()));
                             } else if (l.equalsIgnoreCase("type")) {
                                 html.append(getList(f.types()));
                             } else if (l.equalsIgnoreCase("format")) {
                                 html.append(getList(f.formats()));
                             } else if (l.equalsIgnoreCase("identifier")) {
                                 html.append(getList(f.identifiers()));
                             } else if (l.equalsIgnoreCase("source")) {
                                 html.append(getList(f.sources()));
                             } else if (l.equalsIgnoreCase("language")) {
                                 html.append(getList(f.languages()));
                             } else if (l.equalsIgnoreCase("relation")) {
                                 html.append(getList(f.relations()));
                             } else if (l.equalsIgnoreCase("coverage")) {
                                 html.append(getList(f.coverages()));
                             } else if (l.equalsIgnoreCase("rights")) {
                                 html.append(getList(f.rights()));
                             }                        
                             html.append("</td>");
                         }
                         html.append("</tr>");
                         html.append("<tr><td colspan=\"");
                         html.append(fieldsArray.length);
                         html.append("\"><hr size=\"1\"></td></tr>");
                     }
                 }
                 if (!xml) {
                     html.append("</table></center>\n");
                 }
             }
             if (!xml) {
                 response.setContentType("text/html");
                 PrintWriter out=response.getWriter();
                 out.print("<html><head><title>Search Repository</title></head>");
                 out.print("<body><h2>Search Repository</h2>");
                 out.print(html.toString());
                 out.print("</body>");
             } else {
                 response.setContentType("text/xml");
                 PrintWriter out=response.getWriter();
 		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
 		out.println("<results>");
 		out.println(xmlBuf.toString());
 		out.println("</results>");
             }
         } catch (ServerException se) {
             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
             response.setContentType("text/html");
             PrintWriter out=response.getWriter();
             out.print("<html><head><title>Fedora Error</title></head>");
             out.print("<body><h2>Fedora Error</h2>");
             out.print("<i>");
             out.print(se.getClass().getName());
             out.print("</i>: ");
             out.print(se.getMessage());
             out.print("</body>");
         }
     }
 
     private void appendXML(String name, String value, StringBuffer out) {
        out.append("      <" + name + ">" + value + "</" + name + ">\n");
     }
     
     private void appendXML(String name, List values, StringBuffer out) {
         for (int i=0; i<values.size(); i++) {
 	    appendXML(name, (String) values.get(i), out);
 	}
     }
 
     private void appendXML(String name, Date dt, SimpleDateFormat formatter, 
             StringBuffer out) {
         appendXML(name, formatter.format(dt), out);
     }
     
     private String getList(List l) {
         StringBuffer ret=new StringBuffer();
         for (int i=0; i<l.size(); i++) {
             if (i>0) {
                 ret.append(", ");
             }
             ret.append((String) l.get(i));
         }
         return ret.toString();
     }
 
     /** Exactly the same behavior as doGet. */
     public void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         doGet(request, response);
     }
 
     /** Gets the Fedora Server instance. */
     public void init() throws ServletException {
         try {
             s_server=Server.getInstance(new File(System.getProperty("fedora.home")));
             s_access=(Access) s_server.getModule("fedora.server.access.Access");
         } catch (InitializationException ie) {
             throw new ServletException("Error getting Fedora Server instance: "
                     + ie.getMessage());
         }
     }
     
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
