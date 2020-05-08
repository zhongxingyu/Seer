 package fedora.server.test;
 
 /**
  * <p>Title: FedoraAccessSoapServlet.java</p>
  * <p>Description: Provides a test interface for the Fedora Access SOAP service.
  * This servlet is used for testing the Fedora Access SOAP service using a
  * java servlet. This servlet mirrors the functionality of
  * <code>FedoraAccessServlet</code> with the major difference being that
  * this servlet provides an interface to the Fedora Access SOAP service
  * while <code>FedoraAccessServlet</code> provides an interface to the Fedora
  * Access HTTP service. Note that this servlet is provided as an example of
  * how one could write a simple web-based interface to the Fedora SOAP service.
  * Applications that can readily handle SOAP requests and responses
  * would most likely communicate directly with the Fedora SOAP service rather
  * than use a java servlet as an intermediary.
  *
  * Input parameters for the servlet include:
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
 import fedora.server.errors.MethodNotFoundException;
 import fedora.server.errors.ObjectNotFoundException;
 import fedora.server.storage.DefinitiveDOReader;
 import fedora.server.storage.FastDOReader;
 import fedora.server.storage.types.DisseminationBindingInfo;
 import fedora.server.storage.types.MethodDef;
 import fedora.server.storage.types.MethodParmDef;
 import fedora.server.storage.types.MIMETypedStream;
 import fedora.server.storage.types.ObjectMethodsDef;
 import fedora.server.storage.types.Property;
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
 import java.util.Hashtable;
 
 // Axis SOAP server imports
 import javax.xml.namespace.QName;
 import org.apache.axis.client.Service;
 import org.apache.axis.client.Call;
 
 public class FedoraAccessSoapServlet extends HttpServlet
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
           MIMETypedStream dissemination = null;
           dissemination = getDisseminationFromCache(action, PID, bDefPID,
               methodName, userParms, asOfDate, clearCache, response);
           if (dissemination == null)
           {
             // Dissemination request failed
             // FIXME!! need to decide on exception handling
             showURLParms(action, PID, bDefPID, methodName, asOfDate, userParms,
                         clearCache, response);
             System.out.println("Dissemination Result: NULL");
               this.getServletContext().log("Dissemination Result: NULL");
           } else
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
         } catch (Exception e)
         {
           // FIXME!! Decide on error handling
           System.out.println(e.getMessage());
           System.out.println("GetDissemination: NO RESULT");
           showURLParms(action, PID, bDefPID, methodName, asOfDate, userParms,
                        clearCache, response);
           this.getServletContext().log(e.getMessage(), e.getCause());
         }
       } else if (action.equals(GET_BEHAVIOR_DEFINITIONS))
       {
         String[] behaviorDefs = null;
         try
         {
           // Call Fedora Access SOAP service to request Behavior Definiitons
           behaviorDefs = getBehaviorDefinitions(PID, asOfDate);
           if (behaviorDefs == null)
           {
             // Dissemination request failed
             // FIXME!! need to decide on exception handling
             showURLParms(action, PID, bDefPID, methodName, asOfDate, userParms,
                         clearCache, response);
             System.out.println("GetBehaviorDefinition Result: NULL");
             this.getServletContext().log("GetBehaviorDefinitions Result: NULL");
           }
           response.setContentType(CONTENT_TYPE_HTML);
           // Return HTML table containing results; include links to digital
           // object PID to further explore object.
           out.println("<br><center><table border='1' cellspacing='1' "+
                       "cellpadding='1'>");
           out.println("<tr>");
           out.println("<td><b><font size='+2'><b>PID</font></td></b>");
           out.println("<td><b><font size='+2'>Version Date</font></b></td>");
           out.println("<td><b><font size='+2'>Behavior Definitions</font>"+
                       "</b></td");
           out.println("</tr>");
           // Format table such that repeating fields display only once
           int rows = behaviorDefs.length - 1;
           for (int i=0; i<behaviorDefs.length; i++)
           {
             out.println("<tr>");
             if (i == 0)
             {
               out.println("<td><font color='blue'><a href='" + requestURL +
                           "action_=GetObjectMethods&PID_=" + PID+ "'>" + PID +
                           "</a></font></td>");
               out.println("<td><font color='blue'>" +
                           DateUtility.convertDateToString(versDateTime) +
                           "</font></td>");
               out.println("<td><font color='red'>" + behaviorDefs[i] +
                           "</font></td>");
               out.println("</tr>");
             } else if (i == 1)
             {
               out.println("<td colspan='2' rowspan='" + rows +
                           "'></td><td><font color='red'>" + behaviorDefs[i] +
                           "</font></td>");
               out.println("</tr>");
             } else
             {
               out.println("<td><font color='red'>" + behaviorDefs[i] +
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
           showURLParms(action, PID, bDefPID, methodName, asOfDate, userParms,
                        clearCache, response);
           this.getServletContext().log(e.getMessage(), e.getCause());
         }
       } else if (action.equals(GET_BEHAVIOR_METHODS))
       {
         fedora.server.types.gen.MethodDef[] methodDefs = null;
         try
         {
           // Call Fedora Access SOAP service to request Method Definitions
           methodDefs = getBehaviorMethods(PID, bDefPID, asOfDate);
           if (methodDefs == null)
           {
             // No method definitions found; echo back request parameters
             showURLParms(action, PID, bDefPID, methodName, asOfDate, userParms,
                          clearCache, response);
            System.out.println("GetBehaviorMethods: NO METHODS FOUND");
           } else
           {
              response.setContentType(CONTENT_TYPE_HTML);
              out.println("<center><table border='1' cellspacing='1' "+
                          "cellpadding='2'>");
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
              int rows = methodDefs.length - 1;
              for (int i=0; i<methodDefs.length; i++)
              {
                fedora.server.types.gen.MethodDef results = methodDefs[i];
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
                            results.getMethodName() + "\"> " +
                            results.getMethodName() +
                            " </a> </td>");
                out.println("</tr>");
                } else if (i == 1)
                {
                  out.println("<td colspan='3' rowspan='"+rows+"'></td>"+
                              "<td><font color=\"red\"> " + "<a href=\""+
                            requestURL + "action_=GetDissemination&PID_=" +
                            PID + "&bDefPID_=" + bDefPID + "&methodName_=" +
                            results.getMethodName() + "\"> " +
                            results.getMethodName() +
                            " </a> </td>");
                  out.println("</tr>");
                } else
                {
                  out.println("<td><font color=\"red\"> " + "<a href=\""+
                            requestURL + "action_=GetDissemination&PID_=" +
                            PID + "&bDefPID_=" + bDefPID + "&methodName_=" +
                            results.getMethodName() + "\"> " +
                            results.getMethodName() +
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
           System.out.println("GetBehaviorMethods: NO RESULTS");
           showURLParms(action, PID, bDefPID, methodName, asOfDate, userParms,
                        clearCache, response);
           this.getServletContext().log(e.getMessage(), e.getCause());
         }
         } else if (action.equalsIgnoreCase(GET_BEHAVIOR_METHODS_AS_WSDL))
         {
           fedora.server.types.gen.MIMETypedStream methodDefs = null;
           try
           {
             // Call Fedora Access SOAP service to request Method Definitions
             // in WSDL form
             methodDefs = getBehaviorMethodsAsWSDL(PID, bDefPID, asOfDate);
             if (methodDefs == null)
             {
               // No method WSDL found; echo back request parameters
               showURLParms(action, PID, bDefPID, methodName, asOfDate,
                            userParms, clearCache, response);
              System.out.println("GetBehaviorMethodsAsWSDL: NO METHODS FOUND");
             } else
             {
               ByteArrayInputStream methodResults =
                   new ByteArrayInputStream(methodDefs.getStream());
               response.setContentType(methodDefs.getMIMEType());
               if (debug) System.out.println("MIMEType: "+
                   methodDefs.getMIMEType());
               // WSDL is actually just an XML fragment so add appropriate
               // XML namespace and XML declaration to make a valid XML
               // output stream
               // FIXME!! Should these be added automatically in
               // the class DefinitiveBMechReader
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
           } catch (Exception e)
           {
             System.out.println("GetBehaviorMethodsAsWSDL: NO METHODS FOUND");
             System.out.println(e.getMessage());
             showURLParms(action, PID, bDefPID, methodName, asOfDate, userParms,
                          clearCache, response);
             this.getServletContext().log(e.getMessage(), e.getCause());
         }
       } else if (action.equals(GET_OBJECT_METHODS))
       {
         //fedora.server.types.gen.ObjectMethodsDef objMethDef = null;
         fedora.server.types.gen.ObjectMethodsDef[] objMethDefArray = null;
         try
         {
           // Call Fedora Access SOAP service to request Object Methods
           objMethDefArray = getObjectMethods(PID, asOfDate);
           if (objMethDefArray == null)
           {
             // No object methods were found; echo back request parameters
             showURLParms(action, PID, bDefPID, methodName, asOfDate, userParms,
                          clearCache, response);
            System.out.println("GetObjectMethods: NO METHODS FOUND");
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
                             objMethDefArray[i].getPID() + "</font></td>");
                 out.println("<td><font color=\"green\"> " +
                             objMethDefArray[i].getBDefPID() + " </font></td>");
                 out.println("<td><font color=\"green\"> " +
                             versDateTime + " </font></td>");
                 out.println("<td><font color=\"red\"> " +
                             "<a href=\""+requestURL+
                             "action_=GetDissemination&PID_=" +
                             objMethDefArray[i].getPID() + "&bDefPID_=" +
                             objMethDefArray[i].getBDefPID() + "&methodName_=" +
                             objMethDefArray[i].getMethodName() + "\"> " +
                             objMethDefArray[i].getMethodName() + " </a> </td>");
                 out.println("</tr>");
               } else if (i == 1)
               {
                 out.println("<td colspan='3' rowspan='" + rows +
                             "'></td><td><font color=\"red\"> " +
                             "<a href=\""+requestURL+
                             "action_=GetDissemination&PID_=" +
                             objMethDefArray[i].getPID() + "&bDefPID_=" +
                             objMethDefArray[i].getBDefPID() + "&methodName_=" +
                             objMethDefArray[i].getMethodName() + "\"> " +
                             objMethDefArray[i].getMethodName() + " </a> </td>");
                 out.println("</tr>");
               } else
               {
                 out.println("<td><font color=\"red\"> " +
                             "<a href=\""+requestURL+
                             "action_=GetDissemination&PID_=" +
                             objMethDefArray[i].getPID() + "&bDefPID_=" +
                             objMethDefArray[i].getBDefPID() + "&methodName_=" +
                             objMethDefArray[i].getMethodName() + "\"> " +
                             objMethDefArray[i].getMethodName() + " </a> </td>");
               }
             }
           }
         } catch (Exception e)
         {
           // FIXME!! Need to decide on Exception handling
           System.out.println(e.getMessage());
           this.getServletContext().log(e.getMessage(), e.getCause());
           showURLParms(action, PID, bDefPID, methodName, asOfDate, userParms,
                        clearCache, response);
           System.out.println("GetObjectMethods: NO METHODS FOUND");
         }
       }
     } else
     {
       // URL parameters failed validation check
       // Output from showURLParms method should provide enough information
       // to discern the cause of the failure.
       System.out.println("URLParametersInvalid");
       showURLParms(action, PID, bDefPID, methodName,asOfDate, userParms,
                    clearCache, response);
     }
   }
 
   /**
    * <p>Gets a list of Behavior Definition object PIDs for the specified
    * digital object by making the appropriate call to the Fedora Access
    * SOAP service.</p>
    *
    * @param PID persistent identifier of the digital object
    * @param asOfDate versioning datetime stamp
    * @return String[] containing Behavior Definitions
    */
   public String[] getBehaviorDefinitions(String PID, Calendar asOfDate)
   {
     String[] behaviorDefs = null;
     try
     {
        Date versDateTime = DateUtility.convertCalendarToDate(asOfDate);
        String qName1 = "http://www.fedora.info/definitions/1/0/api/";
        String endpoint = "http://localhost:8080/fedora/access/soap";
        Service service = new Service();
        Call call = (Call) service.createCall();
        call.setTargetEndpointAddress( new java.net.URL(endpoint) );
        call.setOperationName(new javax.xml.namespace.QName(qName1,
            "GetBehaviorDefinitions") );
        behaviorDefs = (String[])call.invoke(new Object[] { PID, versDateTime });
     } catch (Exception e)
     {
       // FIXME!! - need to decide on exception handling
       System.out.println("GetBehaviorMethods: No Method Defs FOUND");
     }
     return behaviorDefs;
   }
 
   /**
    * <p>Gets a list of Behavior Methods associated with the specified
    * Behavior Mechanism object by making the appropriate call to the
    * Fedora Access SOAP service.</p>
    *
    * @param PID persistent identifier of Digital Object
    * @param bDefPID persistent identifier of Behavior Definition object
    * @param asOfDate versioning datetime stamp
    * @return MethodDef[] containing method definitions
    */
   public fedora.server.types.gen.MethodDef[] getBehaviorMethods(String PID,
       String bDefPID, Calendar asOfDate)
   {
     fedora.server.types.gen.MethodDef[] methodDefs = null;
     try
     {
       String qName1 = "http://www.fedora.info/definitions/1/0/api/";
       String endpoint = "http://localhost:8080/fedora/access/soap";
       Service service = new Service();
       Call call = (Call) service.createCall();
       call.setOperationName(new javax.xml.namespace.QName(qName1,
           "GetBehaviorMethods") );
       QName qn = new QName("http://www.fedora.info/definitions/1/0/types/",
                             "MethodDef");
       QName qn2 = new QName("http://www.fedora.info/definitions/1/0/types/",
                             "MethodParmDef");
       call.setTargetEndpointAddress( new java.net.URL(endpoint) );
       call.registerTypeMapping(fedora.server.types.gen.MethodDef.class,
           qn,
           new org.apache.axis.encoding.ser.BeanSerializerFactory(
           fedora.server.types.gen.MethodDef.class, qn),
           new org.apache.axis.encoding.ser.BeanDeserializerFactory(
           fedora.server.types.gen.MethodDef.class, qn));
       call.registerTypeMapping(fedora.server.types.gen.MethodParmDef.class,
           qn2,
           new org.apache.axis.encoding.ser.BeanSerializerFactory(
           fedora.server.types.gen.MethodParmDef.class, qn2),
           new org.apache.axis.encoding.ser.BeanDeserializerFactory(
           fedora.server.types.gen.MethodParmDef.class, qn2));
       methodDefs = (fedora.server.types.gen.MethodDef[])
           call.invoke( new Object[] { PID, bDefPID, asOfDate} );
     } catch (Exception e)
     {
       System.out.println(e.getMessage());
     }
     return methodDefs;
   }
 
   /**
    * <p>Gets a bytestream containing the WSDL that defines the Behavior Methods
    * of the associated Behavior Mechanism object by making the appropriate
    * call to the Fedora Access SOAP service.</p>
    *
    * @param PID persistent identifier of Digital Object
    * @param bDefPID persistent identifier of Behavior Definition object
    * @param asOfDate versioning datetime stamp
    * @return MIMETypedStream containing WSDL method definitions
    */
   public fedora.server.types.gen.MIMETypedStream getBehaviorMethodsAsWSDL(
       String PID, String bDefPID, Calendar asOfDate)
   {
     fedora.server.types.gen.MIMETypedStream methodDefs = null;
     try
     {
       String qName1 = "http://www.fedora.info/definitions/1/0/api/";
       String endpoint = "http://localhost:8080/fedora/access/soap";
       Service service = new Service();
       Call call = (Call) service.createCall();
       call.setOperationName(new javax.xml.namespace.QName(qName1,
           "GetBehaviorMethodsAsWSDL") );
       QName qn = new QName("http://www.fedora.info/definitions/1/0/types/",
                             "MIMETypedStream");
       call.setTargetEndpointAddress( new java.net.URL(endpoint) );
       call.registerTypeMapping(fedora.server.types.gen.MIMETypedStream.class,
           qn,
           new org.apache.axis.encoding.ser.BeanSerializerFactory(
           fedora.server.types.gen.MIMETypedStream.class, qn),
           new org.apache.axis.encoding.ser.BeanDeserializerFactory(
           fedora.server.types.gen.MIMETypedStream.class, qn));
       methodDefs = new fedora.server.types.gen.MIMETypedStream();
       methodDefs = (fedora.server.types.gen.MIMETypedStream)
                    call.invoke( new Object[] { PID, bDefPID, asOfDate} );
     } catch (Exception e)
     {
       System.out.println(e.getMessage());
     }
     return methodDefs;
   }
 
   /**
    * <p>Gets a MIME-typed bytestream containing the result of a dissemination
    * by making the appropriate call to the Fedora Access SOAP service.</p>
    *
    * @param PID persistent identifier of the Digital Object
    * @param bDefPID persistent identifier of the Behavior Definition object
    * @param methodName name of the method
    * @param asOfDate version datetime stamp of the digital object
    * @param userParms array of user-supplied method parameters and values
    * @return MIMETypedStream containing the dissemination result
    */
   public MIMETypedStream getDissemination(String PID, String bDefPID,
        String methodName, Property[] userParms, Calendar asOfDate)
    {
     MIMETypedStream dissemination = null;
     try
     {
       // See if dissemination request is in local cache
       // Generate a call to the Fedora SOAP service requesting the
       // GetDissemination method
       String qName1 = "http://www.fedora.info/definitions/1/0/api/";
       String endpoint = "http://localhost:8080/fedora/access/soap";
       Service service = new Service();
       Call call = (Call) service.createCall();
       call.setTargetEndpointAddress( new java.net.URL(endpoint) );
       call.setOperationName(new javax.xml.namespace.QName(qName1,
           "GetDissemination") );
       QName qn =
           new QName("http://www.fedora.info/definitions/1/0/types/",
           "MIMETypedStream");
       call.registerTypeMapping(
           fedora.server.types.gen.MIMETypedStream.class,
           qn,
           new org.apache.axis.encoding.ser.BeanSerializerFactory(
           fedora.server.types.gen.MIMETypedStream.class, qn),
           new org.apache.axis.encoding.ser.BeanDeserializerFactory(
           fedora.server.types.gen.MIMETypedStream.class, qn));
       fedora.server.types.gen.MIMETypedStream dissem =
           (fedora.server.types.gen.MIMETypedStream)
           call.invoke( new Object[] { PID, bDefPID, methodName,
           asOfDate} );
       // FIXME!! Decide on exception handling
       if (dissem != null)
       {
         dissemination = new MIMETypedStream(dissem.getMIMEType(),
             dissem.getStream());
       }
     } catch (Exception e)
     {
       System.out.println(e.getMessage());
       this.getServletContext().log(e.getMessage(), e.getCause());
     }
     return dissemination;
    }
 
    /**
     * <p>Gets a list of all method definitions for the specified object by
     * making the appropriate call to the Fedora Access SOAP service.</p>
     *
     * @param PID persistent identifier for the digital object
     * @param asOfDate versioning datetime stamp
     * @return ObjectMethodsDef array of object method definitions
     */
   public fedora.server.types.gen.ObjectMethodsDef[] getObjectMethods(String PID,
       Calendar asOfDate)
   {
     Date versDateTime = DateUtility.convertCalendarToDate(asOfDate);
     fedora.server.types.gen.ObjectMethodsDef[] objMethDefArray = null;
 
     try
     {
       String qName1 = "http://www.fedora.info/definitions/1/0/api/";
       String endpoint = "http://localhost:8080/fedora/access/soap";
       Service service = new Service();
       Call call = (Call) service.createCall();
       call.setOperationName(new javax.xml.namespace.QName(qName1,
           "GetObjectMethods") );
       QName qn = new QName("http://www.fedora.info/definitions/1/0/types/",
                             "ObjectMethodsDef");
       call.setTargetEndpointAddress( new java.net.URL(endpoint) );
       call.registerTypeMapping(fedora.server.types.gen.ObjectMethodsDef.class,
           qn,
           new org.apache.axis.encoding.ser.BeanSerializerFactory(
           fedora.server.types.gen.ObjectMethodsDef.class, qn),
           new org.apache.axis.encoding.ser.BeanDeserializerFactory(
           fedora.server.types.gen.ObjectMethodsDef.class, qn));
       objMethDefArray = (fedora.server.types.gen.ObjectMethodsDef[])
           call.invoke( new Object[] { PID, asOfDate} );
 
     } catch (Exception e)
     {
       System.out.println(e.getMessage());
     }
     return objMethDefArray;
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
                           "*****REQUIRED PARAMETER NOT FOUND: "+
                           methodParm.parmName + "</font></b>");
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
         out.println("</table></center></font>");
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
        (clearCache == null || clearCache.equalsIgnoreCase(YES)))
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
       disseminationResult = getDissemination(PID, bDefPID, methodName,
           userParms, asOfDate);
       if (disseminationResult != null)
       {
         // Dissemination request succeeded, so add to local cache
         disseminationCache.put(requestURI, disseminationResult);
          if (debug) System.out.println("ADDED to CACHE: "+requestURI);
       }
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
