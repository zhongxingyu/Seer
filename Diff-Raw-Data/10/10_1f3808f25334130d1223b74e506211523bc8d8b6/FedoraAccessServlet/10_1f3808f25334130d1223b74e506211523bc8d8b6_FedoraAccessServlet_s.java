 package fedora.server.access;
 
 /**
  * <p>Title: FedoraAccessServlet.java</p>
  * <p>Description: Implements Fedora Access interface</p>
  * <p>Copyright: Copyright (c) 2002</p>
  * <p>Company: </p>
  * @author Ross Wayland
  * @version 1.0
  */
 
 // Fedora imports
 import fedora.server.storage.ConnectionPool;
 import fedora.server.storage.DefinitiveBDefReader;
 import fedora.server.storage.DefinitiveDOReader;
 import fedora.server.storage.FastDOReader;
 
 // Java imports
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.Vector;
 import java.text.SimpleDateFormat;
 import java.text.ParsePosition;
 import java.sql.SQLException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.servlet.ServletException;
 
 public class FedoraAccessServlet extends HttpServlet implements FedoraAccess
 {
 
   private Hashtable disseminationCache = new Hashtable();
   private HttpSession session = null;
   private static final String CONTENT_TYPE_HTML = "text/html";
   private static final String CONTENT_TYPE_XML = "text/xml";
   private static final String GET_BEHAVIOR_DEFINITIONS =
       "GetBehaviorDefinitions";
   private static final String GET_BEHAVIOR_METHODS = "GetBehaviorMethods";
   private static final String GET_DISSEMINATION = "GetDissemination";
   private static final int DISS_CACHE_SIZE = 100;
   private static final SimpleDateFormat formatter =
       new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss");
 
   // For Testing
   private static final boolean debug = true;
 
 
   /**
    * Initialize servlet
    *
    * @throws ServletException
    */
   public void init() throws ServletException
   {}
 
   /**
    * Process Post request.
    *
    * @param request
    * @param response
    * @throws ServletException
    * @throws IOException
    */
   public void doPost(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException
   {
    // treat Post same as a Get
    doGet(request, response);
   }
 
   /**
    * Process Fedora Access Request.
    *
    * @param request - Servlet request
    * @param response - Servlet response
    * @throws ServletException
    * @throws IOException
    */
   public void doGet(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException
   {
     String action = "";
     String PID = null;
     String bDefPID = null;
     String method = null;
     Calendar asOfDate = null;
     String versDate = null;
     String clearCache = "";
     String requestURI = null;
     Vector userParms = new Vector();
 
     Enumeration params = request.getParameterNames();
     requestURI = request.getServerName()+":"+request.getServerPort()+
                  request.getRequestURI()+"?"+request.getQueryString();
     session = request.getSession(true);
     PrintWriter out = response.getWriter();
     if (debug) System.out.println("RequestURI: "+requestURI+
                                   "Session: "+session);
 
     while ( params.hasMoreElements())
     {
       String param = (String) params.nextElement();
       if (param.equals("action_"))
       {
         action = request.getParameter(param);
       } else if (param.equals("PID_"))
       {
         PID = request.getParameter(param);
       } else if (param.equals("bDefPID_"))
       {
         bDefPID = request.getParameter(param);
       } else if (param.equals("method_"))
       {
         method = request.getParameter(param);
       } else if (param.equals("asOfDate_"))
       {
         versDate = request.getParameter(param);
         asOfDate = convertDate(versDate);
       } else if (param.equals("clear-cache_"))
       {
         clearCache = request.getParameter(param);
       } else
       {
         // get any user-supplied parameters
         String parmvalues = request.getParameter(param);
         userParms.addElement(param);
         userParms.addElement(parmvalues);
       }
     }
 
     // Check for required URL parameters
     // Perform requested acton if required parameters are present
     if (checkURLParams(action, PID, bDefPID, method, versDate,
                       userParms, clearCache, response))
     {
       // FIXME!! Session management code needs to go here
       if (action.equals(GET_DISSEMINATION))
       {
         // See if dissemination request is in local cache
         MIMETypedStream dissemination = null;
         dissemination = checkCache(requestURI, action, PID, bDefPID, method,
                                    userParms, asOfDate, clearCache, response);
         if (dissemination != null)
         {
           response.setContentType(dissemination.MIMEType);
           int byteStream = 0;
           ByteArrayInputStream dissemResult =
               new ByteArrayInputStream(dissemination.stream);
           while ((byteStream = dissemResult.read()) >= 0)
           {
             out.write(byteStream);
           }
         }
       } else if (action.equals(GET_BEHAVIOR_DEFINITIONS))
       {
         response.setContentType(CONTENT_TYPE_HTML);
         // FIXME!! versioning based on datetime stamp not yet implemented
         String[] bDefs = GetBehaviorDefinitions(PID, null);
         for (int i=0; i<bDefs.length; i++)
         {
           out.println("<br>BDef["+i+"] = "+bDefs[i]);
         }
       } else if (action.equals(GET_BEHAVIOR_METHODS))
       {
         try
         {
           // FIXME!! versioning based on datetime stamp not yet implemented
           MIMETypedStream bDefMethods = GetBehaviorMethods(PID, bDefPID, null);
           if (bDefMethods == null)
           {
             emptyResult(action, PID, bDefPID, method, asOfDate, userParms,
                         clearCache, response);
           } else
           {
 
             ByteArrayInputStream methodResults =
                 new ByteArrayInputStream(bDefMethods.stream);
             response.setContentType(bDefMethods.MIMEType);
             if (debug) System.out.println("MIMEType: "+bDefMethods.MIMEType);
             // WSDL is actually just an XML fragment so add appropriate
             // XML namespace and XML declaration to make a valid XML
             // output stream
             // FIXME!! Should these be added automatically in
             // the class DefinitiveBMechReader??
             out.println("<?xml version=\"1.0\"?>");
             out.println("<definitions xmlns:xsd=\"http://www.w3.org/2000/10/"+
                         "XMLSchema-instance\" xmlns:wsdl=\"http://schemas."+
                         "xmlsoap.org/wsdl/\">");
             int byteStream = 0;
             while ((byteStream = methodResults.read()) >= 0)
             {
               out.write(byteStream);
             }
             out.println("</definitions>");
           }
         } catch (IOException ioe)
         {
           System.out.println(ioe);
         }
       }
     }
   }
 
   /**
    * Method that implements GetBehaviorDefinitions in the FedoraAccess
    * interface. The method returns a string array containing a list of the
    * Behavior Definitions of the associated digital object.
    *
    * @param PID - persistent identifier of the digital object
    * @param asOfDate - version datetime stamp of the digital object
    * @return - String[] containing Behavior Definitions
    */
   public String[] GetBehaviorDefinitions(String PID, Calendar asOfDate)
   {
     DefinitiveDOReader doReader = new DefinitiveDOReader(PID);
     // FIXME!! versioning based on datetime not yet implemented
     return doReader.GetBehaviorDefs(null);
   }
 
   /**
    * Method that implements GetBehaviorMethods in the FedoraAccess interface.
    * The method returns a bytestream containing the WSDL that defines
    * the Behavior Methods of the associated Behavior Definition object.
    *
    * @param PID - persistent identifier of Digital Object
    * @param bDefPID - persistent identifier of Behavior Definition object
    * @param asOfDate - version datetime stamp of the digital object
    * @return - MIMETypedStream containing WSDL definitions for methods
    */
   public MIMETypedStream GetBehaviorMethods(String PID, String bDefPID,
       Calendar asOfDate)
   {
     ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
     MIMETypedStream bDefMethods = null;
     try
     {
       DefinitiveBDefReader doReader = new DefinitiveBDefReader(bDefPID);
       // FIXME!! versioning based on datetime not yet implemented
       InputStream methodResults = doReader.GetBehaviorMethodsWSDL(null);
       int byteStream = 0;
       while ((byteStream = methodResults.read()) >= 0)
       {
         baos.write(byteStream);
       }
     } catch (IOException ioe)
     {
       System.out.println(ioe);
     }
     bDefMethods = new MIMETypedStream(CONTENT_TYPE_XML,baos.toByteArray());
 
     return(bDefMethods);
 
   }
 
   /**
    * Method that implements Getdissemination in the FedoraAccess interface.
    * the method returns a MIME-typed bytestream containing the result
    * of a dissemination request.
    *
    * @param PID - persistent identifier of the Digital Object
    * @param bDefPID - persistent identifier of the Behavior Definition object
    * @param method - name of the method
    * @param asOfDate - version datetime stamp of the digital object
    * @param userParms - array of user-supplied method parameters and values
    * @return - MIMETypedStream containing the dissemination result
    */
   public MIMETypedStream GetDissemination(String PID, String bDefPID,
       String method, Vector userParms, Calendar asOfDate)
   {
     String protocolType = null;
     Vector dissResult = null;
     String dissURL = null;
     MIMETypedStream dissemination = null;
     FastDOReader fastReader = new FastDOReader(PID, bDefPID, method);
     dissResult = fastReader.getDissemination(PID, bDefPID, method);
     if (dissResult.size() == 0)
     {
       // FIXME!! need to implement code for getting dissemination
       // via XML vs SQL.
       // If null is returned from FastDOReader, this indicates that
       // the desired object is not in SQL database. Should then try to
       // get dissemination from authoratative version of object in
       // XML store using DefinitiveDOReader.
       System.out.println("Dissemination Result: NULL");
 
     } else {
       String replaceString = null;
       DissResultSet results = null;
       int counter = 1;
       int numElements = dissResult.size();
       Enumeration e = dissResult.elements();
       // Get row(s) of WSDL results and perform string substitution
       // on DSBindingKey and method parameter values in WSDL
       // Note: In case where more than one datastream matches the
       // DSBindingKey, multiple rows will be returned; otherwise
       // a single row is returned.
       while (e.hasMoreElements())
       {
         results = new DissResultSet((String[])e.nextElement());
         // Match DSBindingKey pattern in WSDL
         String bindingKeyPattern = "\\("+results.dsBindingKey+"\\)";
         if (counter == 1)
         {
           dissURL = results.addressLocation+results.operationLocation;
           protocolType = results.protocolType;
         }
         // If more than one datastream returned alter replacement string by
         // appending +(DSBindingKey) for substitution in next iteration.
         if (numElements == 1 | counter >= numElements)
         {
           replaceString = results.dsLocation;
         } else {
           replaceString = results.dsLocation+"+("+results.dsBindingKey+")";
         }
         dissURL = substituteString(dissURL, bindingKeyPattern, replaceString);
         counter++;
         if (debug) System.out.println("replaced dissURL = "+
                                      dissURL.toString()+
                                      " counter = "+counter);
       }
 
       // FIXME!! need to implement handling of user-supplied method parameters
       // Would need to validate user-supplied parameters and then substitute
       // values in operationLocation.
 
       // FIXME!! need to implement Access Policy control
 
       // Resolve content referenced by dissemination result
       if (debug) System.out.println("ProtocolType = "+protocolType);
       if (protocolType.equalsIgnoreCase("http"))
       {
         dissemination = fastReader.getHttpContent(dissURL);
       } else if (protocolType.equalsIgnoreCase("soap"))
       {
         // FIXME!! future handling by soap interface
         System.out.println("Protocol type specified: "+protocolType);
         dissemination = null;
       } else
       {
         System.out.println("Unknown protocol type: "+protocolType);
         dissemination = null;
       }
     }
 
     return dissemination;
   }
 
   /**
    * Method to perform simple string replacement using regular expressions.
    * All mathcing occurrences of the pattern string will be replaced in the
    * input string by the replacement string.
    *
    * @param inputString - source string
    * @param patternString - regular expression pattern
    * @param replaceString - replacement string
    * @return - source string with substitutions
    */
   private String substituteString(String inputString, String patternString,
                                  String replaceString)
   {
     Pattern pattern = Pattern.compile(patternString);
     Matcher m = pattern.matcher(inputString);
     return(m.replaceAll(replaceString));
   }
 
   /**
    * Method to validate user-supplied method parameters against values
    * in the corresponding Behavior Definition object.
    *
    * @param PID - persistent identifier of the Digital Object
    * @param bDefPID - persistent identifier of the Behavior Definition object
    * @param method - name of the method
    * @param userParms - array of user-supplied method parameters and values
    * @return - true if method parameters are valid; false otherwise
    *
    * /
   private boolean validateUserParms(String PID, String bDefPID, String method,
                                    Vector userParms)
   {
     FastDOReader fdor = new FastDOReader(PID, bDefPID, method);
     Vector methodParms = new Vector();
     methodParms = fdor.getMethodParms(bDefPID, method);
     // FIXME!! - implement code to handle validation of user paramters
     return(true);
   }
 
   /**
    * Method to convert a string into a Calendar instance.
    *
    * @param dateTime - string representing datetime stamp of the
    * form: yyyy-mm-ddThh:m:ss
    * @return - a Calendar instance
    */
   private Calendar convertDate(String dateTime)
   {
     Calendar calendar = Calendar.getInstance();
     ParsePosition pos = new ParsePosition(0);
     java.util.Date date = formatter.parse(dateTime, pos);
     if (debug) {System.out.println("<p>Converted Date: "+date);}
     calendar.setTime(date);
     return(calendar);
   }
 
   private void emptyResult(String action, String PID, String bDefPID,
                            String method, Calendar asOfDate,
                            Vector userParms, String clearCache, HttpServletResponse response)
       throws IOException
   {
 
     String versDate = "";
     if (asOfDate != null)
     {
       Date date = asOfDate.getTime();
       versDate = formatter.format(date);
       if (debug) System.out.println("versdate: "+versDate);
     }
     PrintWriter out = response.getWriter();
     response.setContentType(CONTENT_TYPE_HTML);
     out.println("<html>");
     out.println("<head><title>FedoraServlet</title></head>");
     out.println("<body>");
     out.println("<br></br><h3>REQUEST Returned NO Data</h3>");
     out.println("<br></br><font color='red'>Request Parameters</font>"+
                 "<br></br>");
     out.println("<table>"+
                 "<tr><td><font color='red'>action_</td><td> = </td><td>"+
                 action+"</td></tr>"+
                 "<tr><td><font color='red'>PID_</td><td> = <td>"+PID+
                 "</td></tr></font><tr><td><font color='red'>bDefPID_</td>"+
                 "<td> = </td><td>"+bDefPID+"</td></tr></font>"+
                 "<tr><td><font color='red'>method_</td><td> = </td>"+
                 "<td>"+method+"</td></tr></font><tr><td><font color='red'>"+
                 "AsOfDate_</td><td> = </td><td>"+versDate+"</td></tr></font>"+
                 "<tr><td><font color='red'>clear-cache_</td><td> = </td>");
     out.println("<tr></tr><tr><td colspan='5'><font color='blue'>"+
                 "Other-Parameters:</font></td></tr><tr></tr>");
     for (Enumeration e = userParms.elements() ; e.hasMoreElements(); )
     {
       out.println("<tr><td><font color='red'>"+e.nextElement()+" </td>"+
       "<td>= </td><td>"+e.nextElement()+"</td></tr></font>");
     }
     out.println("</table></font>");
     out.println("</body></html>");
 
     if (debug)
     {
       System.out.println("PID: "+PID+"bDEF: "+bDefPID+"method: "+method);
       for (Enumeration e = userParms.elements() ; e.hasMoreElements(); )
       {
         System.out.println("<p>userParm: "+e.nextElement()+
         " userValue: "+e.nextElement());
       }
     }
     System.out.println("REQUEST Returned NO Data");
   }
 
   /**
    * Method to validate required servlet URL parameters. Different parameters
    * are required based on the requested action.
    *
    * @param action - servlet action to be executed
    * @param PID - persistent identifier of the Digital Object
    * @param bDefPID - persistent identifier of the Behavior Definition object
    * @param method - method name
    * @param versDate - version datetime stamp of the digital object
    * @param userParms - user-supplied method parameters
    * @param clearCache - boolean to clear dissemination cache
    * @param response - Servlet http response
    * @return - true if required parameters are valid; false otherwise
    * @throws IOException
    */
   private boolean checkURLParams(String action, String PID, String bDefPID,
                           String method, String versDate, Vector userParms,
                           String clearCache, HttpServletResponse response)
       throws IOException
   {
     // check for missing required parameters
     boolean checkOK = true;
     if (action.equals(GET_DISSEMINATION))
     {
       if (PID == null || bDefPID == null || method == null)
       {
         PrintWriter out = response.getWriter();
         response.setContentType(CONTENT_TYPE_HTML);
         out.println("<html>");
         out.println("<head><title>FedoraServlet</title></head>");
         out.println("<body>");
         out.println("<br></br><font color='red'>Required parameter missing "+
                     "in Dissemination Request:</font>");
         out.println("<table>"+
                     "<tr><td><font color='red'>action_</td><td> = </td><td>"+
                     action+"</td><td><font color='blue'>(REQUIRED)</font></td>"+
                     "</tr>"+
                     "<tr><td><font color='red'>PID_</td><td> = </td><td>"+PID+
                     "</td><td><font color='blue'>(REQUIRED)</font></td></tr>"+
                     "</font><tr><td><font color='red'>bDefPID_</td>"+
                     "<td> = </td><td>"+bDefPID+"</td><td><font color='blue'>"+
                     "(REQUIRED)</font></td></tr></font><tr><td>"+
                     "<font color='red'>method_</td><td> = </td><td>"+method+
                     "</td><td><font color='blue'>(REQUIRED)</font></td></tr>"+
                     "</font><tr><td><font color='red'>AsOfDate_</td>"+
                     "<td> = </td><td>"+versDate+"</td><td>"+
                     "<font color='green'>(OPTIONAL)</font></td></tr></font>"+
                     "<tr><td><font color='red'>clear-cache_</td><td> = </td>"+
                     "<td>"+clearCache+"</td><td><font color='green'>"+
                     "(OPTIONAL)</font></td></tr></font>");
         out.println("<tr></tr><tr><td colspan='5'>Other-Parameters:</td>"+
                     "</tr><tr></tr>");
         for (Enumeration e = userParms.elements() ; e.hasMoreElements(); )
         {
           out.println("<tr><td><font color='red'>"+e.nextElement()+
           " </td><td>= </td><td>"+e.nextElement()+"</td></tr>");
         }
         out.println("</table>></font>");
         out.println("</body></html>");
         checkOK = false;
       }
     } else if (action.equals(GET_BEHAVIOR_DEFINITIONS))
     {
       if (PID == null)
       {
         PrintWriter out = response.getWriter();
         response.setContentType(CONTENT_TYPE_HTML);
         out.println("<html>");
         out.println("<head><title>FedoraServlet</title></head>");
         out.println("<body>");
         out.println("<tr><td colspan='5'><font color='red'>Required "+
                     "parameter missing in Behavior Definition Request:"+
                     "</td></tr></font>");
         out.println("<table>"+
                     "<tr><td><font color='red'>action_</td><td> = </td><td>"+
                     action+"</td><td><font color='blue'>(REQUIRED)</font></td>"+
                     "</tr>"+
                     "<tr><td><font color='red'>PID_</td><td> = <td>"+PID+
                     "</td><td><font color='blue'>(REQUIRED)"+
                     "</font></td></tr></font><tr><td><font color='red'>"+
                     "bDefPID_</td><td> = </td><td>"+bDefPID+"</td>"+
                     "<td><font color='green'>(OPTIONAL)</font></td>"+
                     "</tr></font><tr><td><font color='red'>method_</td>"+
                     "<td> = </td><td>"+method+"</td><td><font color='green'>"+
                     "(OPTIONAL)</font></td></tr></font><tr><td>"+
                     "<font color='red'>AsOfDate_</td><td> = </td><td>"+
                     versDate+"</td><td><font color='green'>(OPTIONAL)"+
                     "</font></td></tr></font><tr><td><font color='red'>"+
                     "clear-cache_</td><td> = </td><td>"+clearCache+
                     "</td><td><font color='green'>(OPTIONAL)"+
                     "</font></td></tr></font>");
         out.println("<tr></tr><tr><td colspan='5'>Other-Parameters:</td>"+
                     "</tr><tr></tr>");
         for (Enumeration e = userParms.elements() ; e.hasMoreElements(); )
         {
           out.println("<tr><td><font color='red'>"+e.nextElement()+
           " </td><td>= </td><td>"+e.nextElement()+"</td></tr></font>");
         }
         out.println("</table>></font>");
         out.println("</body></html>");
         checkOK = false;
       }
     } else if (action.equals(GET_BEHAVIOR_METHODS))
     {
       if (PID == null || bDefPID == null)
       {
         PrintWriter out = response.getWriter();
         response.setContentType(CONTENT_TYPE_HTML);
         out.println("<html>");
         out.println("<head><title>FedoraServlet</title></head>");
         out.println("<body>");
         out.println("<br></br><font color='red'>Required parameter "+
                     "missing in Behavior Methods Request:</font>");
         out.println("<table>"+
                     "<tr><td><font color='red'>action_</td><td> = </td><td>"+
                     action+"</td><td><font color='blue'>(REQUIRED)</font></td>"+
                     "</tr>"+
                     "<tr><td><font color='red'>PID_</td><td> = </td><td>"+PID+
                     "</td><td><font color='blue'>(REQUIRED)</font></td></tr>"+
                     "</font><tr><td><font color='red'>bDefPID_</td>"+
                     "<td> = </td><td>"+bDefPID+"</td><td><font color='blue'>"+
                     "(REQUIRED)</font></td></tr></font><tr><td>"+
                     "<font color='red'>method_</td><td> = </td><td>"+method+
                     "</td><td><font color='green'>(OPTIONAL)</font></td>"+
                     "</tr></font><tr><td><font color='red'>AsOfDate_</td>"+
                     "<td> = </td><td>"+versDate+"</td><td>"+
                     "<font color='green'>(OPTIONAL)</font></td></tr></font>"+
                     "<tr><td><font color='red'>clear-cache_</td><td> = </td>"+
                     "<td>"+clearCache+"</td><td><font color='green'>"+
                     "(OPTIONAL)</font></td></tr></font>");
         out.println("<tr></tr><tr><td colspan='5'>Other-Parameters:</td></tr>"+
                     "<tr></tr>");
         for (Enumeration e = userParms.elements() ; e.hasMoreElements(); )
         {
           out.println("<tr><td><font color='red'>"+e.nextElement()+
           " <td>= <td>"+e.nextElement()+"<td><font color='green'>"+
           "(OPTIONAL)</font></tr></font>");
         }
         out.println("</table>></font>");
         out.println("</body></html>");
         checkOK = false;
       }
     } else
     {
       PrintWriter out = response.getWriter();
       response.setContentType(CONTENT_TYPE_HTML);
       out.println("<html>");
       out.println("<head><title>FedoraServlet</title></head>");
       out.println("<body>");
       out.println("<br><font color='red'>Invalid action parameter"+
                  " specified in Servlet Request:action= "+action+"<br></br>");
       out.println("<br></br><font color='blue'>Reserved parameters "+
           "in Request:</td></tr></font>");
       out.println("<table>"+
                   "<tr><td><font color='red'>action_</td><td> = </td><td>"+
                    action+"</td></tr>"+
                     "<tr><td><font color='red'>PID_</td><td> = <td>"+PID+
                     "</td></tr></font><tr><td><font color='red'>bDefPID_</td>"+
                     "<td> = </td><td>"+bDefPID+"</td></tr></font>"+
                     "<tr><td><font color='red'>method_</td><td> = </td>"+
                     "<td>"+method+"</td></tr></font><tr><td><font color='red'>"+
                     "AsOfDate_</td><td> = </td><td>"+versDate+"</td></tr>"+
                     "</font><tr><td><font color='red'>clear-cache_</td>"+
                     "<td> = </td><td>"+clearCache+"</td></tr></font>");
       out.println("<tr></tr><tr><td colspan='5'><font color='blue'>"+
                   "Other-Parameters:</font></td></tr><tr></tr>");
       for (Enumeration e = userParms.elements() ; e.hasMoreElements(); )
       {
         out.println("<tr><td><font color='red'>"+e.nextElement()+" </td>"+
         "<td>= </td><td>"+e.nextElement()+"</td></tr></font>");
       }
       out.println("</table></font>");
       out.println("</body></html>");
       checkOK = false;
     }
 
     if (debug)
     {
       System.out.println("PID: "+PID+"bDEF: "+bDefPID+"method: "+method+
                          "action: "+action);
       for (Enumeration e = userParms.elements() ; e.hasMoreElements(); )
       {
         System.out.println("<p>userParm: "+e.nextElement()+
         " userValue: "+e.nextElement());
       }
     }
 
     return(checkOK);
   }
 
   /**
    * Method to check dissemination cache. The method retrieves the dissemination
    * result from the cache if possible; otherwise, it initiates a new call
    * to GetDissemination.
    *
    * @param dissRequestID - originating URI request used as hash key
    * @param PID - persistent identifier of the Digital Object
    * @param bDefPID - persistent identifier of the Behavior Definition object
    * @param method - method name
    * @param userParms - user-supplied method parameters
    * @param asOfDate - version datetime stamp of the digital object
    * @return - MIMETypedStream containing dissemination result
    */
   private synchronized MIMETypedStream checkCache(String dissRequestID,
       String action, String PID, String bDefPID, String method,
       Vector userParms, Calendar asOfDate, String clearCache,
       HttpServletResponse response) throws IOException
   {
     // Clear cache if size gets larger than DISS_CACHE_SIZE
     if (disseminationCache.size() > DISS_CACHE_SIZE ||
         clearCache.equalsIgnoreCase("yes"))
     {
       clearDisseminationCache();
     }
     MIMETypedStream disseminationResult = null;
     // See if dissemination request is in local cache
     disseminationResult =
         (MIMETypedStream)disseminationCache.get(dissRequestID);
     if (disseminationResult == null)
     {
       // Dissemination request NOT in local cache.
       // Try reading from database
       disseminationResult = GetDissemination(PID, bDefPID, method,
           userParms, asOfDate);
       if (disseminationResult != null)
       {
         // Dissemination request succeeded, so add to local cache
         disseminationCache.put(dissRequestID, disseminationResult);
          if (debug) System.out.println("ADDED to CACHE: "+dissRequestID);
       } else
       {
         // Dissemination request failed
         emptyResult(action, PID, bDefPID, method, asOfDate, userParms,
                     clearCache, response);
         System.out.println("Dissemination Result: NULL");
       }
       if (debug) System.out.println("CACHE SIZE: "+disseminationCache.size());
     }
 
     return disseminationResult;
 
   }
 
   /**
    * Instantiates a new dissemination cache.
    */
   private synchronized void clearDisseminationCache() {
     disseminationCache = new Hashtable();
   }
 
   /**
    * Cleans up servlet resources
    */
   public void destroy()
   {
 
   }
 }
