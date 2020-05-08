 package fedora.server.access;
 
 /**
  * <p>Title: FedoraAccessServlet.java</p>
  * <p>Description: Implements Fedora Access interface via a java Servlet
  * front end. Input parameters for the servlet include:
  * <ul>
  * <li>action_ name of Fedora service which must be one of the following:
  * <ol>
  * <li>GetBehaviorDefinitions - Gets list of Behavior Defintions</li>
  * <li>GetBehaviorMethods - Gets list of Behavior Methods</li>
  * <li>GetBehaviorMethodsAsWSDL - Gets Behavior Methods as XML</li>
  * <li>GetDissemination - Gets a dissemination result</li>
  * </ol>
  * <li>PID_ - persistent identifier of the digital object</li>
  * <li>bDefPID_ - persistent identifier of the Behavior Definiiton object</li>
  * <li>methodName_ - name of the method</li>
  * <li>asOfDate_ - versioning datetime stamp</li>
  * <li>clearCache_ - signal to flush the dissemination cache; value of "yes"
  * will clear the cache.
  * <li>methodParms - some methods require or provide optional parameters that
  * may be provided by the user; these parameters are entered as name/value
  * pairs like the other serlvet parameters. (optional)
  * </ul>
  * </p>
  * <p>Copyright: Copyright (c) 2002</p>
  * <p>Company: </p>
  * @author Ross Wayland
  * @version 1.0
  */
 
 // Fedora imports
 import fedora.server.access.localservices.HttpService;
 import fedora.server.errors.HttpServiceNotFoundException;
 import fedora.server.errors.ObjectNotFoundException;
 import fedora.server.errors.MethodNotFoundException;
 import fedora.server.errors.MethodParmNotFoundException;
 import fedora.server.storage.DefinitiveBMechReader;
 import fedora.server.storage.DefinitiveDOReader;
 import fedora.server.storage.FastDOReader;
 import fedora.server.storage.FastDOReader;
 import fedora.server.storage.types.DisseminationBindingInfo;
 import fedora.server.storage.types.MIMETypedStream;
 import fedora.server.storage.types.Property;
 import fedora.server.storage.types.MethodParmDef;
 import fedora.server.storage.types.MethodDef;
 import fedora.server.storage.types.ObjectMethodsDef;
 import fedora.server.utilities.DateUtility;
 
 // Java imports
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.servlet.ServletException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.Vector;
 
 public class FedoraAccessServlet extends HttpServlet implements FedoraAccess
 {
 
   private Hashtable disseminationCache = new Hashtable();
   private HttpSession session = null;
   private static final String CONTENT_TYPE_HTML = "text/html";
   private static final String CONTENT_TYPE_XML = "text/xml";
   private static final String GET_BEHAVIOR_DEFINITIONS =
       "GetBehaviorDefinitions";
   private static final String GET_BEHAVIOR_METHODS =
       "GetBehaviorMethods";
   private static final String GET_BEHAVIOR_METHODS_AS_WSDL =
       "GetBehaviorMethodsAsWSDL";
   private static final String GET_DISSEMINATION =
       "GetDissemination";
   private static final String GET_OBJECT_METHODS =
       "GetObjectMethods";
   private static final String LOCAL_ADDRESS_LOCATION = "LOCAL";
   private static final String YES = "yes";
   private static final int DISS_CACHE_SIZE = 100;
   private Hashtable h_userParms = new Hashtable();
   private String requestURL = null;
   private String requestURI = null;
 
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
    * <p>For now, treat a HTTP POST request just like a GET request.</p>
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
    * <p>Process Fedora Access Request.</p>
    *
    * @param request  servlet request
    * @param response servlet response
    * @throws ServletException
    * @throws IOException
    */
   public void doGet(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException
   {
     String action = null;
     String PID = null;
     String bDefPID = null;
     String methodName = null;
     Calendar asOfDate = null;
     Date versDateTime = null;
     String clearCache = null;
     Property[] userParms = null;
     // FIXME!! getRequestURL() not available in all releases of servlet API
     //requestURL = request.getRequestURL().toString()+"?";
     requestURL = "http://"+request.getServerName()+":"+request.getServerPort()+
                  request.getRequestURI()+"?";
     requestURI = requestURL+request.getQueryString();
     session = request.getSession(true);
     PrintWriter out = response.getWriter();
     if (debug) System.out.println("RequestURL: "+requestURL+
                                   "RequestURI: "+requestURI+
                                   "Session: "+session);
     // Get servlet input parameters
     Enumeration URLParms = request.getParameterNames();
     while ( URLParms.hasMoreElements())
     {
       String parm = (String) URLParms.nextElement();
       if (parm.equals("action_"))
       {
         action = request.getParameter(parm);
       } else if (parm.equals("PID_"))
       {
         PID = request.getParameter(parm);
       } else if (parm.equals("bDefPID_"))
       {
         bDefPID = request.getParameter(parm);
       } else if (parm.equals("methodName_"))
       {
         methodName = request.getParameter(parm);
       } else if (parm.equals("asOfDate_"))
       {
         asOfDate = DateUtility.
                    convertStringToCalendar(request.getParameter(parm));
       } else if (parm.equals("clearCache_"))
       {
         clearCache = request.getParameter(parm);
       } else
       {
         // Any remaining parameters are assumed to be user-supplied parameters.
         // Place user-supplied parameters in hashtable for easy access.
         h_userParms.put(parm, request.getParameter(parm));
       }
     }
     // API-A interface requires user-supplied paramters to be of type
     // Property[] so create Property[] from hashtable of user parameters.
     int userParmCounter = 0;
     userParms = new Property[h_userParms.size()];
     for ( Enumeration e = h_userParms.keys(); e.hasMoreElements();)
     {
       Property userParm = new Property();
       userParm.Name = (String)e.nextElement();
       userParm.Value = (String)h_userParms.get(userParm.Name);
       userParms[userParmCounter] = userParm;
       userParmCounter++;
     }
 
     // Validate servlet URL parameters to verify that all parameters required
     // by the servlet implementation of API-A are present and to verify
     // that any other user-supplied parameters are valid for the request.
     if (isValidURLParms(action, PID, bDefPID, methodName, versDateTime,
                       h_userParms, clearCache, response))
     {
       // FIXME!! May need to deal with session management in the future
 
       // Have valid request.
       if (action.equals(GET_DISSEMINATION))
       {
         try
         {
           // See if dissemination request is in local cache
           MIMETypedStream dissemination = null;
           dissemination = getDisseminationFromCache(action, PID, bDefPID, methodName,
                                      userParms, asOfDate, clearCache, response);
           if (dissemination != null)
           {
             // Dissemination was successful;
             // Return MIMETypedStream back to browser client
             response.setContentType(dissemination.MIMEType);
             int byteStream = 0;
             ByteArrayInputStream dissemResult =
                 new ByteArrayInputStream(dissemination.stream);
             while ((byteStream = dissemResult.read()) >= 0)
             {
               out.write(byteStream);
             }
         } else
         {
           // Dissemination request failed
           // FIXME!! need to decide on exception handling
           showURLParms(action, PID, bDefPID, methodName, asOfDate, userParms,
                       clearCache, response);
           System.out.println("Dissemination Result: NULL");
           this.getServletContext().log("Dissemination Result: NULL");
         }
         // FIXME!! Decide on exception handling
         } catch (Exception e)
         {
           System.out.println(e.getMessage());
           this.getServletContext().log(e.getMessage(), e.getCause());
         }
       } else if (action.equals(GET_BEHAVIOR_DEFINITIONS))
       {
         try
         {
           response.setContentType(CONTENT_TYPE_HTML);
           String[] bDefs = GetBehaviorDefinitions(PID, asOfDate);
           // Return HTML table containing results; include links to digital
           // object PID to further explore object.
           out.println("<br><center><table border='1' cellspacing='1' cellpadding='1'>");
           out.println("<tr>");
           out.println("<td><b><font size='+2'><b>PID</font></td></b>");
           out.println("<td><b><font size='+2'>Version Date</font></b></td>");
           out.println("<td><b><font size='+2'>Behavior Definitions</font>"+
                       "</b></td");
           out.println("</tr>");
           // Format table such that repeating fields display only once
           int rows = bDefs.length - 1;
           for (int i=0; i<bDefs.length; i++)
           {
             out.println("<tr>");
             if (i == 0)
             {
               out.println("<td><font color='blue'><a href='" + requestURL +
                           "action_=GetObjectMethods&PID_=" + PID+ "'>" + PID +
                           "</a></font></td>");
               //out.flush();
               out.println("<td><font color='blue'>" +
                           DateUtility.convertDateToString(versDateTime) +
                           "</font></td>");
               out.println("<td><font color='red'>" + bDefs[i] +
                           "</font></td>");
               out.println("</tr>");
             } else if (i == 1)
             {
               out.println("<td colspan='2' rowspan='" + rows +
                           "'></td><td><font color='red'>" + bDefs[i] +
                           "</font></td>");
               out.println("</tr>");
             } else
             {
               out.println("<td><font color='red'>" + bDefs[i] +
                           "</font></td>");
               out.println("</tr>");
             }
           }
           out.println("</table></center><br>");
           out.println("</body></html>");
         // FIXME!! Decide on Exception handling
         } catch (Exception e)
         {
           System.out.println(e.getMessage());
           this.getServletContext().log(e.getMessage(), e.getCause());
         }
       } else if (action.equalsIgnoreCase(GET_BEHAVIOR_METHODS_AS_WSDL))
       {
         MIMETypedStream methodWSDL = null;
         methodWSDL = GetBehaviorMethodsAsWSDL(PID, bDefPID, asOfDate);
         if (methodWSDL != null)
         {
           // Method WSDL found; output WSDL as xml
           response.setContentType(methodWSDL.MIMEType);
           ByteArrayInputStream bais = new ByteArrayInputStream(methodWSDL.stream);
           int byteStream = 0;
           while ((byteStream = bais.read()) >= 0)
           {
             out.write(byteStream);
         }
         } else
         {
           // No method WSDL found; echo back request parameters
           showURLParms(action, PID, bDefPID, methodName, asOfDate, userParms,
                        clearCache, response);
           System.out.println("GetBehaviorMethodsAsWSDL: NO METHODS FOUND");
         }
       } else if (action.equals(GET_BEHAVIOR_METHODS))
       {
         try
         {
           MethodDef[] bDefMethods = GetBehaviorMethods(PID, bDefPID, asOfDate);
           if (bDefMethods == null)
           {
             // No Behavior Definitions were found; echo back request parameters
             showURLParms(action, PID, bDefPID, methodName, asOfDate, userParms,
                         clearCache, response);
           } else
           {
             // Behavior Definitions found; output HTML table of results
             // with links to each method enabling dissemination of the
             // method and a link to the object PID enabling further
             // discovery about the object.
             response.setContentType(CONTENT_TYPE_HTML);
             out.println("<center><table border='1' cellspacing='1' cellpadding='2'>");
             out.println("<tr>");
             out.println("<td><font color=\"blue\"> Object PID " +
                         " </font></td>");
             out.println("<td><font color=\"green\"> BDEF PID" +
                         " </font></td>");
             out.println("<td><font color=\"green\"> Version Date" +
                         " </font></td>");
             out.println("<td><font color=\"red\"> Method Name" +
                         " </font></td>");
             out.println("</tr>");
             // Format table such that repeating fields display only once
             int rows = bDefMethods.length - 1;
             for (int i=0; i<bDefMethods.length; i++)
             {
               MethodDef results = bDefMethods[i];
               out.println("<tr>");
               if (i == 0)
               {
               out.println("<tr>");
               out.println("<td><font color=\"blue\"> " + "<a href=\""+
                           requestURL + "action_=GetObjectMethods&PID_=" +
                           PID + "\"> " + PID + " </a></font></td>");
               out.println("<td><font color=\"green\"> " + bDefPID +
                           " </font></td>"+
                           "<td><font color=\"green\"> " +
                           DateUtility.convertDateToString(versDateTime) +
                           "</font></td>");
               out.println("<td><font color=\"red\"> " + "<a href=\""+
                           requestURL + "action_=GetDissemination&PID_=" +
                           PID + "&bDefPID_=" + bDefPID + "&methodName_=" +
                           results.methodName + "\"> " + results.methodName +
                           " </a> </td>");
               out.println("</tr>");
               //out.flush();
               } else if (i == 1)
               {
                 out.println("<td colspan='3' rowspan='"+rows+"'></td>"+
                             "<td><font color=\"red\"> " + "<a href=\""+
                           requestURL + "action_=GetDissemination&PID_=" +
                           PID + "&bDefPID_=" + bDefPID + "&methodName_=" +
                           results.methodName + "\"> " + results.methodName +
                           " </a> </td>");
                 out.println("</tr>");
               } else
               {
                 out.println("<td><font color=\"red\"> " + "<a href=\""+
                           requestURL + "action_=GetDissemination&PID_=" +
                           PID + "&bDefPID_=" + bDefPID + "&methodName_=" +
                           results.methodName + "\"> " + results.methodName +
                           " </a> </td>");
                 out.println("</tr>");
               }
             }
             out.println("</table></center>");
           }
         // FIXME!! Need to decide on Exception handling
         } catch (Exception e)
         {
           System.out.println(e.getMessage());
           this.getServletContext().log(e.getMessage(), e.getCause());
         }
       } else if (action.equals(GET_OBJECT_METHODS))
       {
         ObjectMethodsDef objMethDef = null;
         ObjectMethodsDef[] objMethDefArray = null;
         try
         {
           objMethDefArray = GetObjectMethods(PID, asOfDate);
           if (objMethDefArray == null)
           {
             // No object methods were found; echo back request parameters
             showURLParms(action, PID, bDefPID, methodName, asOfDate, userParms,
                         clearCache, response);
           } else
           {
             // Object methods found; output HTML table containing all object
             // methods with links on each method enabling dissemination of
             // that particular method.
             response.setContentType(CONTENT_TYPE_HTML);
             out.println("<br><br><center><b><font color=\"purple\">" +
                         "DIGITAL OBJECT:</font><font color=\"blue\"> " +
                         PID + " </font></b></center>\n");
             out.println("<br><center><table border>\n");
             out.println("<tr>");
             out.println("<td><font color=\"blue\"> Object PID "+
                                     " </font></td>");
             out.println("<td><font color=\"green\"> BDEF PID"+
                                     " </font></td>");
             out.println("<td><font color=\"green\"> Version Date"+
                                     " </font></td>");
             out.println("<td><font color=\"red\"> Method Name"+
                                     " </font></td>");
             out.println("</tr>");
             // Format table such that repeating fields only display once
             int rows = objMethDefArray.length-1;
             for (int i=0; i<objMethDefArray.length; i++)
             {
               out.println("<tr>");
               if (i == 0)
               {
                 out.println("<td><font color=\"blue\"> " +
                             objMethDefArray[i].PID + "</font></td>");
                 out.flush();
                 out.println("<td><font color=\"green\"> " +
                             objMethDefArray[i].bDefPID + " </font></td>");
                 out.println("<td><font color=\"green\"> " +
                             versDateTime + " </font></td>");
                 out.println("<td><font color=\"red\"> " +
                             "<a href=\""+requestURL+
                             "action_=GetDissemination&PID_=" +
                             objMethDefArray[i].PID + "&bDefPID_=" +
                             objMethDefArray[i].bDefPID + "&methodName_=" +
                             objMethDefArray[i].methodName + "\"> " +
                             objMethDefArray[i].methodName + " </a> </td>");
                 out.flush();
                 out.println("</tr>");
               } else if (i == 1)
               {
                 out.println("<td colspan='3' rowspan='" + rows +
                             "'></td><td><font color=\"red\"> " +
                             "<a href=\""+requestURL+
                             "action_=GetDissemination&PID_=" +
                             objMethDefArray[i].PID + "&bDefPID_=" +
                             objMethDefArray[i].bDefPID + "&methodName_=" +
                             objMethDefArray[i].methodName + "\"> " +
                             objMethDefArray[i].methodName + " </a> </td>");
                 out.println("</tr>");
               } else
               {
                 out.println("<td><font color=\"red\"> " +
                             "<a href=\""+requestURL+
                             "action_=GetDissemination&PID_=" +
                             objMethDefArray[i].PID + "&bDefPID_=" +
                             objMethDefArray[i].bDefPID + "&methodName_=" +
                             objMethDefArray[i].methodName + "\"> " +
                             objMethDefArray[i].methodName + " </a> </td>");
               }
           }
           }
         } catch (Exception e)
         {
           // FIXME!! Need to decide on Exception handling
           System.out.println(e.getMessage());
           this.getServletContext().log(e.getMessage(), e.getCause());
     }
       }
     } else
     {
       // URL parameters failed validation check
       // Output from showURLParms method should provide enough information
       // to discern the cause of the failure.
       System.out.println("URLParametersInvalid");
       showURLParms(action, PID, bDefPID, methodName,asOfDate, userParms, clearCache, response);
     }
   }
 
   /**
    * <p>Implements GetBehaviorDefinitions in the FedoraAccess interface. Gets a
    * list of Behavior Definition object PIDs for the specified digital object.
    * </p>
    *
    * @param PID persistent identifier of the digital object
    * @param asOfDate versioning datetime stamp
    * @return String[] containing Behavior Definitions
    */
   public String[] GetBehaviorDefinitions(String PID, Calendar asOfDate)
   {
     try
     {
       FastDOReader fastReader = new FastDOReader(PID);
       Date versDateTime = DateUtility.convertCalendarToDate(asOfDate);
       return fastReader.GetBehaviorDefs(versDateTime);
     } catch (ObjectNotFoundException onfe)
     {
       // FIXME!! - need to decide on exception handling
       return null;
     }
   }
 
   /**
    * <p>Implements GetBehaviorMethods in the FedoraAccess interface.
    * Gets a list of Behavior Methods associated with the specified
    * Behavior Mechanism object.</p>
    *
    * @param PID persistent identifier of Digital Object
    * @param bDefPID persistent identifier of Behavior Definition object
    * @param asOfDate versioning datetime stamp
    * @return MethodDef[] containing method definitions
    */
   public MethodDef[] GetBehaviorMethods(String PID, String bDefPID,
       Calendar asOfDate)
   {
     Date versDateTime = DateUtility.convertCalendarToDate(asOfDate);
     ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
     MIMETypedStream bDefMethods = null;
     MethodDef[] methodDefs = null;
     try
     {
       FastDOReader fastReader = new FastDOReader(PID);
       methodDefs = fastReader.GetBMechMethods(bDefPID, versDateTime);
     } catch (ObjectNotFoundException onfe)
     {
       // FIXME!! - need to decide on exception handling
       return null;
     }
     return methodDefs;
   }
 
   /**
    * <p>Implements GetBehaviorMethodsAsWSDL in the FedoraAccess interface.
    * Gets a bytestream containing the WSDL that defines the Behavior Methods
    * of the associated Behavior Mechanism object.
    *
    * @param PID persistent identifier of Digital Object
    * @param bDefPID persistent identifier of Behavior Definition object
    * @param asOfDate versioning datetime stamp
    * @return MIMETypedStream containing WSDL method definitions
    */
   public MIMETypedStream GetBehaviorMethodsAsWSDL(String PID, String bDefPID,
       Calendar asOfDate)
   {
     Date versDateTime = DateUtility.convertCalendarToDate(asOfDate);
     ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
     MIMETypedStream methodDefs = null;
     try
     {
       DefinitiveDOReader doReader = new DefinitiveDOReader(PID);
       InputStream methodResults = doReader.GetBMechMethodsWSDL(bDefPID,
           versDateTime);
       int byteStream = 0;
       while ((byteStream = methodResults.read()) >= 0)
       {
         baos.write(byteStream);
       }
     } catch (IOException ioe)
     {
       System.out.println(ioe);
       this.getServletContext().log(ioe.getMessage(), ioe.getCause());
     } catch (Exception e)
     {
       // FIXME!! - need to decide on exception handling
       return null;
     }
     methodDefs = new MIMETypedStream(CONTENT_TYPE_XML,baos.toByteArray());
     return methodDefs;
   }
 
   /**
    * <p>Implements GetDissemination in the Fedora Access interface.
    * Gets a MIME-typed bytestream containing the result of a dissemination.
    *
    * @param PID persistent identifier of the Digital Object
    * @param bDefPID persistent identifier of the Behavior Definition object
    * @param methodName name of the method
    * @param asOfDate version datetime stamp of the digital object
    * @param userParms array of user-supplied method parameters and values
    * @return MIMETypedStream containing the dissemination result
    */
   public MIMETypedStream GetDissemination(String PID, String bDefPID,
        String methodName, Property[] userParms, Calendar asOfDate)
    {
      String protocolType = null;
      DisseminationBindingInfo[] dissResults = null;
      DisseminationBindingInfo dissResult = null;
      String dissURL = null;
      String operationLocation = null;
      MIMETypedStream dissemination = null;
      Date versDateTime = DateUtility.convertCalendarToDate(asOfDate);
      FastDOReader fastReader = null;
      try
      {
        fastReader = new FastDOReader(PID);
        dissResults = fastReader.getDissemination(PID, bDefPID, methodName,
            versDateTime);
        String replaceString = null;
        //DissResultSet results = new DissResultSet();
 
        int numElements = dissResults.length;
        // Get row(s) of WSDL results and perform string substitution
        // on DSBindingKey and method parameter values in WSDL
        // Note: In case where more than one datastream matches the
        // DSBindingKey or there are multiple DSBindingKeys for the
        // method, multiple rows will be returned; otherwise
        // a single row is returned.
        for (int i=0; i<dissResults.length; i++)
        {
          dissResult = dissResults[i];
          // If AddressLocation has a value of "LOCAL", this is a flag to
          // indicate the associated OperationLocation requires no
          // AddressLocation. i.e., the OperationLocation contains all
          // information necessary to perform the dissemination request.
          if (dissResult.AddressLocation.equalsIgnoreCase(LOCAL_ADDRESS_LOCATION))
          {
            dissResult.AddressLocation = "";
          }
          // Match DSBindingKey pattern in WSDL
          String bindingKeyPattern = "\\("+dissResult.DSBindKey+"\\)";
          if (i == 0)
          {
            operationLocation = dissResult.OperationLocation;
            dissURL = dissResult.AddressLocation+dissResult.OperationLocation;
            protocolType = dissResult.ProtocolType;
          }
          if (debug) System.out.println("counter: "+i+" numelem: "+numElements);
          String currentKey = dissResult.DSBindKey;
          String nextKey = "";
          if (i != numElements-1)
          {
            // Except for last row, get the value of the next binding key
            // to compare with the value of the current binding key.
            if (debug) System.out.println("currentKey: '"+currentKey+"'");
            nextKey = dissResults[i+1].DSBindKey;
            if (debug) System.out.println("' nextKey: '"+nextKey+"'");
          }
          // In most cases, there is only a single datastream that matches a given
          // DSBindingKey so the substitution process is to just replace the
          // occurence of (BINDING_KEY) with the value of the datastream location.
          // However, when multiple datastreams match the same DSBindingKey, the
          // occurrence of (BINDING_KEY) is replaced with the value of the
          // datastream location and the value +(BINDING_KEY) is appended so that
          // subsequent datastreams matching the binding key will be substituted.
          // The end result is that the binding key will be replaced by a string
          // datastream locations separated by a plus(+) sign. e.g.,
          //
          // file=(PHOTO) becomes
          // file=dslocation1+dslocation2+dslocation3
          //
          // It is the responsibility of the Behavior Mechanism to know how to
          // handle an input parameter with multiple datastreams.
          //
          // In the case of a method containing multiple binding keys,
          // substitutions are performed on each binding key. e.g.,
          //
          // image=(PHOTO)&watermark=(WATERMARK) becomes
          // image=dslocation1&watermark=dslocation2
          //
          // In the case with mutliple binding keys and multiple datastreams,
          // the substitution might appear like the following:
          //
          // image=(PHOTO)&watermark=(WATERMARK) becomes
          // image=dslocation1+dslocation2&watermark=dslocation3
          if (nextKey.equalsIgnoreCase(currentKey) & i != numElements)
          {
            replaceString = dissResult.DSLocation+"+("+dissResult.DSBindKey+")";
          } else
          {
            replaceString = dissResult.DSLocation;
          }
          if (debug) System.out.println("replaceString: "+replaceString);
          dissURL = substituteString(dissURL, bindingKeyPattern, replaceString);
          if (debug) System.out.println("replaced dissURL = "+
                                       dissURL.toString()+
                                       " counter = "+i);
        }
 
        // User-supplied parameters have already been validated.
        // Substitute user-supplied parameter values in dissemination URL
        Enumeration e = h_userParms.keys();
        while (e.hasMoreElements())
        {
          String name = (String)e.nextElement();
          String value = (String)h_userParms.get(name);
          String pattern = "\\("+name+"\\)";
          dissURL = substituteString(dissURL, pattern, value);
          if (debug) System.out.println("UserParmSubstitution dissURL: "+dissURL);
        }
 
        // Resolve content referenced by dissemination result
        if (debug) System.out.println("ProtocolType = "+protocolType);
        if (protocolType.equalsIgnoreCase("http"))
        {
          // FIXME!! need to implement Access Policy control.
          // If access is based on restrictions to content,
          // this is the last chance to apply those restrictions
          // before returnign dissemination result to client.
          HttpService httpService = new HttpService(dissURL);
          try
          {
            dissemination = httpService.getHttpContent(dissURL);
          } catch (HttpServiceNotFoundException onfe)
          {
            // FIXME!! -- Decide on Exception handling
            System.out.println(onfe.getMessage());
          }
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
      } catch (ObjectNotFoundException onfe)
      {
        // FIXME!! Decide on Exception handling
        // Object was not found in SQL database or in XML storage area
        System.out.println("getdissem: ObjectNotFound");
        this.getServletContext().log(onfe.getMessage(), onfe.getCause());
      }
      return dissemination;
    }
 
    /**
     * <p>Implements GetObjectMethods in the Fedora Access Interface.
     * Gets a list of all method definitions for the specified object.</p>
     *
     * @param PID persistent identifier for the digital object
     * @param asOfDate versioning datetime stamp
     * @return ObjectMethodsDef array of object method definitions
     */
   public ObjectMethodsDef[] GetObjectMethods(String PID, Calendar asOfDate)
   {
     Date versDateTime = DateUtility.convertCalendarToDate(asOfDate);
     FastDOReader fastReader = null;
     ObjectMethodsDef[] objMethDefArray = null;
     try
     {
       fastReader = new FastDOReader(PID);
       objMethDefArray = fastReader.getObjectMethods(PID, versDateTime);
     } catch (ObjectNotFoundException onfe)
     {
       // FIXME!! Decide on Exception handling
       System.out.println(onfe.getMessage());
       this.getServletContext().log(onfe.getMessage(), onfe.getCause());
     }
     return objMethDefArray;
   }
 
   /**
    * <p>Performs simple string replacement using regular expressions.
    * All matching occurrences of the pattern string will be replaced in the
    * input string by the replacement string.
    *
    * @param inputString source string
    * @param patternString regular expression pattern
    * @param replaceString replacement string
    * @return String source string with substitutions
    */
   private String substituteString(String inputString, String patternString,
                                  String replaceString)
   {
     Pattern pattern = Pattern.compile(patternString);
     Matcher m = pattern.matcher(inputString);
     return m.replaceAll(replaceString);
   }
 
   /**
    * <p>Validates user-supplied method parameters against values
    * in the corresponding Behavior Definition object. The method will validate
    * for:</p>
    * <ol>
    * <li> Valid name - each name must match a valid method parameter name</li>
    * <li> DefaultValue - any specified parameters with valid default values
    * will have the default value substituted if the user-supplied value is null
    * </li>
    * <li> Required name - each required method parameter name must be present
    * </ol>
    *
    * @param PID persistent identifier of the Digital Object
    * @param bDefPID persistent identifier of the Behavior Definition object
    * @param methodName name of the method
    * @param h_userParms hashtable of user-supplied method parameter name/value
    *                      pairs
    * @return boolean true if method parameters are valid; false otherwise
    *
    */
   private boolean isValidUserParms(String PID, String bDefPID,
                                     String methodName, Hashtable h_userParms,
                                     Date versDateTime,
                                     HttpServletResponse response)
       throws IOException
   {
     boolean valid = true;
     FastDOReader fdor = null;
     MethodParmDef[] methodParms = null;
     MethodParmDef methodParm = null;
 
     try
     {
       fdor = new FastDOReader(PID);
       methodParms = fdor.GetBMechMethodParm(bDefPID, methodName, versDateTime);
       // FIXME!! Decide on Exception handling
     } catch(MethodNotFoundException mpnfe)
     {
       System.out.println(mpnfe.getMessage());
       this.getServletContext().log(mpnfe.getMessage(), mpnfe.getCause());
     } catch (ObjectNotFoundException onfe)
     {
       System.out.println(onfe.getMessage());
       this.getServletContext().log(onfe.getMessage(), onfe.getCause());
     }
     // Put valid method parameters and their attributes into hashtable
     Hashtable v_validParms = new Hashtable();
     if (methodParms != null)
     {
       for (int i=0; i<methodParms.length; i++)
       {
         methodParm = methodParms[i];
         v_validParms.put(methodParm.parmName,methodParm);
       }
     }
     // check if no user supplied parameters
     if (!h_userParms.isEmpty())
     {
       // Iterate over each user supplied parameter name
       Enumeration parmNames = h_userParms.keys();
       while (parmNames.hasMoreElements())
       {
         String name = (String)parmNames.nextElement();
         methodParm = (MethodParmDef)v_validParms.get(name);
         if (methodParm != null && methodParm.parmName != null)
         {
           // Method has at least one parameter
           if (methodParm.parmRequired)
           {
             // Method parm is required
             if (h_userParms.get(methodParm.parmName) == null)
             {
               // Error: required method parameter not in user-supplied list
               System.out.println("REQUIRED PARAMETER:" + methodParm.parmName +
                                  " NOT FOUND");
               response.setContentType(CONTENT_TYPE_HTML);
               PrintWriter out = response.getWriter();
               out.println("<br></br><b><font size=\"+1\" color=\"green\">"+
                           "*****REQUIRED PARAMETER NOT FOUND: "+methodParm.parmName+
                           "</font></b>");
               valid = false;
             } else
             {
               // Required parameter found
               if (debug) System.out.println("Required parameter FOUND: " +
                   methodParm.parmName);
             }
           }
           // Method parameter is not required
           // Check for default value if user-supplied value is null or empty
           String value = (String)h_userParms.get(methodParm.parmName);
           if (value == null && value.equalsIgnoreCase(""))
           {
             // Value of user-supplied parameter is  null or empty
             if(methodParm.parmDefaultValue != null)
             {
               // Default value is specified for this parameter.
               // Substitute default value.
               h_userParms.put(methodParm.parmName, methodParm.parmDefaultValue);
               if (debug) System.out.println("SET DEFAULT VALUE: "+
                   methodParm.parmDefaultValue);
             }
           } else
           {
             // Value of user-supplied parameter is NOT null or empty
             if (debug) System.out.println("NO DEFAULT VALUE APPLIED");
           }
           if (!h_userParms.isEmpty() &&
               (h_userParms.get(methodParm.parmName) == null) )
           {
             // User-supplied parameter name does not match any valid parameter
             // names for this method.
             System.out.println("USER SUPPLIED PARAMETER NOT VALID FOR THIS " +
                               "METHOD: "+methodParm.parmName);
             response.setContentType(CONTENT_TYPE_HTML);
             PrintWriter out = response.getWriter();
             out.println("<br><b><font size=\"+1\" color=\"green\">"+
                         "*****INVALID METHOD PARAMETER: "+methodParm.parmName+
                           "</font></b>");
             valid = false;
           }
         } else
         {
           if (debug) System.out.println("NAME NOT FOUND: "+name);
         }
     }
     } else
     {
       // There were no user supplied parameters.
       // Check if this method has any required parameters.
       if (methodParms != null)
       {
         for (int i=0; i<methodParms.length; i++)
         {
           methodParm = methodParms[i];
           if (methodParm.parmRequired)
           {
             // A required method parameter was not found
             if (debug) System.out.println("emptyREQUIRED PARAM NAME NOT FOUND: "
                 + methodParm.parmName);
             response.setContentType(CONTENT_TYPE_HTML);
             PrintWriter out = response.getWriter();
             out.println("<br></br><b><font size=\"+1\" color=\"green\">"+
                         "REQUIRED METHOD PARAMETER NOT FOUND: "+methodParm.parmName+
                           "</font></b>");
             valid = false;
           } else
           {
             //if (debug) System.out.println("emptyNON-REQUIRED PARAM FOUND: " +
             //    methodParm.parmName);
           }
         }
       }
     }
     return valid;
   }
 
   /**
    * <p>Displays a list of the servlet input parameters. This method is
    * generally called when a service request returns no data. Usually
    * this is a result of an incorrect spelling of either a required
    * URL parameter or in one of the user-supplied parameters. The output
    * from this method can be used to help verify the URL parameters
    * sent to the servlet</p>
    *
    * @param action Fedora service requested
    * @param PID persistent identifier of the Digital Object
    * @param bDefPID persistent identifier of the Behavior Definition object
    * @param methodName name of the method
    * @param asOfDate version datetime stamp of the digital object
    * @param userParms array of user-supplied method parameters and values
    * @param clearCache dissemination cache flag
    * @param response servlet response
    * @throws IOException if unable to create <code>PrintWriter</code>
    */
   private void showURLParms(String action, String PID, String bDefPID,
                            String methodName, Calendar asOfDate,
                            Property[] userParms, String clearCache,
                            HttpServletResponse response)
       throws IOException
   {
 
     String versDate = DateUtility.convertCalendarToString(asOfDate);
     if (debug) System.out.println("versdate: "+versDate);
     PrintWriter out = response.getWriter();
     response.setContentType(CONTENT_TYPE_HTML);
     // Display servlet input parameters
     out.println("<html>");
     out.println("<head><title>FedoraServlet</title></head>");
     out.println("<body>");
     out.println("<br></br><h3>REQUEST Returned NO Data</h3>");
     out.println("<br></br><font color='red'>Request Parameters</font>"+
                 "<br></br>");
     out.println("<center><table>"+
                 "<tr><td><font color='red'>action_</td><td> = </td><td>"+
                 action+"</td></tr>"+
                 "<tr><td><font color='red'>PID_</td><td> = <td>"+PID+
                 "</td></tr></font><tr><td><font color='red'>bDefPID_</td>"+
                 "<td> = </td><td>"+bDefPID+"</td></tr></font>"+
                 "<tr><td><font color='red'>methodName_</td><td> = </td>"+
                 "<td>"+methodName+"</td></tr></font><tr><td><font color='red'>"+
                 "AsOfDate_</td><td> = </td><td>"+versDate+"</td></tr></font>"+
                 "<tr><td><font color='red'>clearCache_</td><td> = </td>"+
                 "<td>"+clearCache+"</td></tr>");
     out.println("<tr></tr><tr><td colspan='5'><font color='blue'>"+
                 "Other-Parameters:</font></td></tr><tr></tr>");
     if (userParms != null)
     {
       // List user-supplied parameters if any
       for (int i=0; i<userParms.length; i++)
       {
         out.println("<tr><td><font color='red'>"+userParms[i].Name+" </td>"+
         "<td>= </td><td>"+userParms[i].Value+"</td></tr></font>");
       }
     }
     out.println("</table></center></font>");
     out.println("</body></html>");
 
     if (debug)
     {
       System.out.println("PID: "+PID+"bDEF: "+bDefPID+"methodName: " +
                          methodName);
       if (userParms != null)
       {
         for (int i=0; i<userParms.length; i++)
         {
           System.out.println("<p>userParm: "+userParms[i].Name+
           " userValue: "+userParms[i].Value);
         }
       }
     }
     System.out.println("REQUEST Returned NO Data");
   }
 
   /**
    * <p>Validates required servlet URL parameters. Different parameters
    * are required based on the requested action.</p>
    *
    * @param action servlet action to be executed
    * @param PID persistent identifier of the Digital Object
    * @param bDefPID persistent identifier of the Behavior Definition object
    * @param methodName method name
    * @param versDate version datetime stamp of the digital object
    * @param userParms user-supplied method parameters
    * @param clearCache boolean to clear dissemination cache
    * @param response Servlet http response
    * @return boolean true if required parameters are valid; false otherwise
    * @throws IOException
    */
   private boolean isValidURLParms(String action, String PID, String bDefPID,
                           String methodName, Date versDateTime,
                           Hashtable h_userParms, String clearCache,
                           HttpServletResponse response)
       throws IOException
   {
     // check for missing parameters required by the interface definition
     boolean checkOK = true;
     PrintWriter out = response.getWriter();
     String versDate = DateUtility.convertDateToString(versDateTime);
     if (action != null && action.equals(GET_DISSEMINATION))
     {
       if (PID == null || bDefPID == null || methodName == null)
       {
         // Dissemination requires PID, bDefPID, and methodName
         // asOfDate is optional
         response.setContentType(CONTENT_TYPE_HTML);
         out.println("<html>");
         out.println("<head><title>FedoraServlet</title></head>");
         out.println("<body>");
         out.println("<br></br><font color='red'>Required parameter missing "+
                     "in Dissemination Request:</font>");
         out.println("<center><table>"+
                     "<tr><td><font color='red'>action_</td><td> = </td><td>"+
                     action+"</td><td><font color='blue'>(REQUIRED)</font></td>"+
                     "</tr>"+
                     "<tr><td><font color='red'>PID_</td><td> = </td><td>"+PID+
                     "</td><td><font color='blue'>(REQUIRED)</font></td></tr>"+
                     "</font><tr><td><font color='red'>bDefPID_</td>"+
                     "<td> = </td><td>"+bDefPID+"</td><td><font color='blue'>"+
                     "(REQUIRED)</font></td></tr></font><tr><td>"+
                     "<font color='red'>methodName_</td><td> = </td><td>"+
                     methodName+
                     "</td><td><font color='blue'>(REQUIRED)</font></td></tr>"+
                     "</font><tr><td><font color='red'>AsOfDate_</td>"+
                     "<td> = </td><td>"+versDate+"</td><td>"+
                     "<font color='green'>(OPTIONAL)</font></td></tr></font>"+
                     "<tr><td><font color='red'>clearCache_</td><td> = </td>"+
                     "<td>"+clearCache+"</td><td><font color='green'>"+
                     "(OPTIONAL)</font></td></tr></font>");
         out.println("<tr></tr><tr><td colspan='5'>Other-Parameters:</td>"+
                     "</tr><tr></tr>");
         for (Enumeration e = h_userParms.keys() ; e.hasMoreElements(); )
         {
           String name = (String)e.nextElement();
           out.println("<tr><td><font color='red'>"+name+
           " </td><td>= </td><td>"+h_userParms.get(name)+"</td></tr>");
         }
         out.println("</table></center></font>");
         out.println("</body></html>");
         checkOK = false;
       }
       // Check user supplied method parms:
       //   1) For all parameters required by the method
       //   2) For any parameters with null values for which the method
       //      defines default values
       //   3) For any parameters not definined by the method
       if(!isValidUserParms(PID, bDefPID, methodName, h_userParms,
                             versDateTime, response))
       {
         checkOK = false;
       }
 
     } else if (action != null &&
                (action.equals(GET_BEHAVIOR_DEFINITIONS) ||
                action.equals(GET_OBJECT_METHODS)))
     {
       if (PID == null)
       {
         // GetBehaviorDefinitions and GetObjectMethods require PID
         // asOfDate is optional
         response.setContentType(CONTENT_TYPE_HTML);
         out.println("<html>");
         out.println("<head><title>FedoraServlet</title></head>");
         out.println("<body>");
         out.println("<tr><td colspan='5'><font color='red'>Required "+
                     "parameter missing in Behavior Definition Request:"+
                     "</td></tr></font>");
         out.println("<center><table>"+
                     "<tr><td><font color='red'>action_</td><td> = </td><td>"+
                     action+"</td><td><font color='blue'>(REQUIRED)</font></td>"+
                     "</tr>"+
                     "<tr><td><font color='red'>PID_</td><td> = <td>"+PID+
                     "</td><td><font color='blue'>(REQUIRED)"+
                     "</font></td></tr></font><tr><td><font color='red'>"+
                     "bDefPID_</td><td> = </td><td>"+bDefPID+"</td>"+
                     "<td><font color='green'>(OPTIONAL)</font></td>"+
                     "</tr></font><tr><td><font color='red'>methodName_</td>"+
                     "<td> = </td><td>"+methodName+
                     "</td><td><font color='green'>"+
                     "(OPTIONAL)</font></td></tr></font><tr><td>"+
                     "<font color='red'>AsOfDate_</td><td> = </td><td>"+
                     versDate+"</td><td><font color='green'>(OPTIONAL)"+
                     "</font></td></tr></font><tr><td><font color='red'>"+
                     "clearCache_</td><td> = </td><td>"+clearCache+
                     "</td><td><font color='green'>(OPTIONAL)"+
                     "</font></td></tr></font>");
         out.println("<tr></tr><tr><td colspan='5'>Other-Parameters:</td>"+
                     "</tr><tr></tr>");
         for (Enumeration e = h_userParms.keys() ; e.hasMoreElements(); )
         {
           String name = (String)e.nextElement();
           out.println("<tr><td><font color='red'>"+name+
           " </td><td>= </td><td>"+h_userParms.get(name)+"</td></tr></font>");
         }
         out.println("</table><center></font>");
         out.println("</body></html>");
         checkOK = false;
       }
     } else if (action != null &&
                (action.equalsIgnoreCase(GET_BEHAVIOR_METHODS) ||
                action.equalsIgnoreCase(GET_BEHAVIOR_METHODS_AS_WSDL)))
     {
       if (PID == null || bDefPID == null)
       {
         // GetBehaviorMethods and GetBehaviorMethodsAsWSDL require PID, bDefPID
         // asOfDate is optional
         response.setContentType(CONTENT_TYPE_HTML);
         out.println("<html>");
         out.println("<head><title>FedoraServlet</title></head>");
         out.println("<body>");
         out.println("<br></br><font color='red'>Required parameter "+
                     "missing in Behavior Methods Request:</font>");
         out.println("<center><table>"+
                     "<tr><td><font color='red'>action_</td><td> = </td><td>"+
                     action+"</td><td><font color='blue'>(REQUIRED)</font></td>"+
                     "</tr>"+
                     "<tr><td><font color='red'>PID_</td><td> = </td><td>"+PID+
                     "</td><td><font color='blue'>(REQUIRED)</font></td></tr>"+
                     "</font><tr><td><font color='red'>bDefPID_</td>"+
                     "<td> = </td><td>"+bDefPID+"</td><td><font color='blue'>"+
                     "(REQUIRED)</font></td></tr></font><tr><td>"+
                     "<font color='red'>methodName_</td><td> = </td><td>"+
                     methodName+
                     "</td><td><font color='green'>(OPTIONAL)</font></td>"+
                     "</tr></font><tr><td><font color='red'>AsOfDate_</td>"+
                     "<td> = </td><td>"+versDate+"</td><td>"+
                     "<font color='green'>(OPTIONAL)</font></td></tr></font>"+
                     "<tr><td><font color='red'>clearCache_</td><td> = </td>"+
                     "<td>"+clearCache+"</td><td><font color='green'>"+
                     "(OPTIONAL)</font></td></tr></font>");
         out.println("<tr></tr><tr><td colspan='5'>Other-Parameters:</td></tr>"+
                     "<tr></tr>");
         for (Enumeration e = h_userParms.keys() ; e.hasMoreElements(); )
         {
           String name = (String)e.nextElement();
           out.println("<tr><td><font color='red'>"+name+
           " <td>= <td>"+h_userParms.get(name)+"<td><font color='green'>"+
           "(OPTIONAL)</font></tr></font>");
         }
         out.println("</table></center></font>");
         out.println("</body></html>");
         checkOK = false;
       }
     } else
     {
       // Unknown Fedora service has been requested
       response.setContentType(CONTENT_TYPE_HTML);
       out.println("<html>");
       out.println("<head><title>FedoraServlet</title></head>");
       out.println("<body>");
       out.println("<br><font color='red'>Invalid action parameter"+
                  " specified in Servlet Request:action= "+action+"<br></br>");
       out.println("<br></br><font color='blue'>Reserved parameters "+
           "in Request:</td></tr></font>");
       out.println("<center><table>"+
                   "<tr><td><font color='red'>action_</td><td> = </td><td>"+
                    action+"</td></tr>"+
                     "<tr><td><font color='red'>PID_</td><td> = <td>"+PID+
                     "</td></tr></font><tr><td><font color='red'>bDefPID_</td>"+
                     "<td> = </td><td>"+bDefPID+"</td></tr></font>"+
                     "<tr><td><font color='red'>methodName_</td><td> = </td>"+
                     "<td>"+methodName+
                     "</td></tr></font><tr><td><font color='red'>"+
                     "AsOfDate_</td><td> = </td><td>"+versDate+"</td></tr>"+
                     "</font><tr><td><font color='red'>clearCache_</td>"+
                     "<td> = </td><td>"+clearCache+"</td></tr></font>");
       out.println("<tr></tr><tr><td colspan='5'><font color='blue'>"+
                   "Other-Parameters:</font></td></tr><tr></tr>");
 
       for ( Enumeration e = h_userParms.keys(); e.hasMoreElements(); )
       {
         String name = (String)e.nextElement();
         out.println("<tr><td><font color='red'>"+name+" </td>"+
         "<td>= </td><td>"+h_userParms.get(name)+"</td></tr></font>");
       }
       out.println("</table></center></font>");
       out.println("</body></html>");
       checkOK = false;
     }
 
     if (debug)
     {
       System.out.println("PID: "+PID+"bDEF: "+bDefPID+"methodName: "+methodName+
                          "action: "+action);
 
       for ( Enumeration e = h_userParms.keys(); e.hasMoreElements(); )
       {
         String name = (String)e.nextElement();
         System.out.println("<p>userParm: "+name+
         " userValue: "+h_userParms.get(name));
       }
     }
     return checkOK;
   }
 
   /**
    * <p>Gets dissemination from cache. This method attempts to retrieve
    * a dissemination from the cache. If found, the dissemination is
    * returned. If not found, this method calls <code>GetDissemination</code>
    * to get the dissemination. If the retrieval is successful, the
    * dissemination is added to the cache. The cache may be cleared by
    * setting the URL servlet parameter <code>clearCache</code> to a value
    * of "yes". The cache is also flushed when it reaches the limit
    * specified by <code>DISS_CACHE_SIZE</code>.</p>
    *
    * @param dissRequestID originating URI request used as hash key
    * @param PID persistent identifier of the Digital Object
    * @param bDefPID persistent identifier of the Behavior Definition object
    * @param methodName method name
    * @param userParms user-supplied method parameters
    * @param asOfDate version datetime stamp of the digital object
    * @return MIMETypedStream containing dissemination result
    */
   private synchronized MIMETypedStream getDisseminationFromCache(String action,
       String PID, String bDefPID, String methodName,
       Property[] userParms, Calendar asOfDate, String clearCache,
       HttpServletResponse response) throws IOException
   {
     // Clear cache if size gets larger than DISS_CACHE_SIZE
     // FIXME!! This needs to part of the Fedora server config parameters
     if (disseminationCache.size() > DISS_CACHE_SIZE ||
        (clearCache != null && clearCache.equalsIgnoreCase(YES)))
     {
       clearDisseminationCache();
     }
     MIMETypedStream disseminationResult = null;
     // See if dissemination request is in local cache
     disseminationResult =
         (MIMETypedStream)disseminationCache.get(requestURI);
     if (disseminationResult == null)
     {
       // Dissemination request NOT in local cache.
       // Try reading from relational database
       disseminationResult = GetDissemination(PID, bDefPID, methodName,
           userParms, asOfDate);
       if (disseminationResult != null)
       {
         // Dissemination request succeeded, so add to local cache
         disseminationCache.put(requestURI, disseminationResult);
          if (debug) System.out.println("ADDED to CACHE: "+requestURI);
       } /* else
       {
         // Dissemination request failed
         // FIXME!! need to decide on exception handling
         showURLParms(action, PID, bDefPID, methodName, asOfDate, userParms,
                     clearCache, response);
         System.out.println("Dissemination Result: NULL");
         this.getServletContext().log("Dissemination Result: NULL");
       } */
       if (debug) System.out.println("CACHE SIZE: "+disseminationCache.size());
     }
     return disseminationResult;
   }
 
   /**
    * <p>Instantiates a new dissemination cache.</p>
    */
   private synchronized void clearDisseminationCache() {
     disseminationCache = new Hashtable();
   }
 
   /**
    * <p>Cleans up servlet resources.</p>
    */
   public void destroy()
   {
   }
 }
