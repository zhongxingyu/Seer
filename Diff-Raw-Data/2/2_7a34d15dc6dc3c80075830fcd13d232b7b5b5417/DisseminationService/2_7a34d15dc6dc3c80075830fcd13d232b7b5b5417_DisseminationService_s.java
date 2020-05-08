 package fedora.server.access.dissemination;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.net.URLEncoder;
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import fedora.common.Constants;
 import fedora.server.Context;
 import fedora.server.ReadOnlyContext;
 import fedora.server.Server;
 import fedora.server.errors.DisseminationException;
 import fedora.server.errors.DisseminationBindingInfoNotFoundException;
 import fedora.server.errors.GeneralException;
 import fedora.server.errors.InitializationException;
 import fedora.server.errors.ServerException;
 import fedora.server.errors.ServerInitializationException;
 import fedora.server.security.Authorization;
 import fedora.server.storage.DOManager;
 import fedora.server.storage.DOReader;
 import fedora.server.storage.ExternalContentManager;
 import fedora.server.storage.types.Datastream;
 import fedora.server.storage.types.DatastreamMediation;
 import fedora.server.storage.types.DisseminationBindingInfo;
 import fedora.server.storage.types.MIMETypedStream;
 import fedora.server.storage.types.MethodParmDef;
 import fedora.server.utilities.DateUtility;
 
 /**
  * <p><b>Title: </b>DisseminationService.java</p>
  * <p><b>Description: </b>A service for executing a dissemination given its
  * binding information.</p>
  *
  * @author rlw@virginia.edu
  * @version $Id$
  */
 public class DisseminationService
 {
 
   /** The Fedora Server instance */
   private static Server s_server;
 
   /** An instance of DO manager */
   private static DOManager m_manager;
 
   /** Signifies the special type of address location known as LOCAL.
    *  An address location of LOCAL implies that no remote host name is
    *  required for the address location and that the contents of the
    *  operation location are sufficient to execute the associated mechanism.
    */
   private static final String LOCAL_ADDRESS_LOCATION = "LOCAL";
 
   /** The expiration limit in minutes for removing entries from the database. */
   private static int datastreamExpirationLimit = 0;
 
   /** An incremental counter used to insure uniqueness of tempIDs used for
    * datastream mediation.
    */
   private static int counter = 0;
 
   /** Datastream Mediation control flag. */
   private static boolean doDatastreamMediation;
 
   /** Make sure we have a server instance for error logging purposes. */
   static
   {
     try
     {
       String fedoraHome = System.getProperty("fedora.home");
       if (fedoraHome == null)
       {
           throw new ServerInitializationException(
               "[DisseminationService] Server failed to initialize: The "
               + "'fedora.home' system property was not set.");
       } else
       {
         s_server = Server.getInstance(new File(fedoraHome));
         m_manager = (DOManager) s_server.getModule("fedora.server.storage.DOManager");
         String expireLimit = s_server.getParameter("datastreamExpirationLimit");
         if (expireLimit == null || expireLimit.equalsIgnoreCase(""))
         {
           s_server.logWarning("[DisseminationService] Unable to resolve "
               + "the datastream expiration limit from the configuration"
               + "file. The expiration limit has been set to 300 seconds.");
           datastreamExpirationLimit = 300;
         } else
         {
           datastreamExpirationLimit = new Integer(expireLimit).intValue();
           s_server.logFinest("[DisseminationService] datastreamExpirationLimit: "
               + datastreamExpirationLimit);
         }
         String dsMediation =
             s_server.getModule("fedora.server.access.Access").getParameter("doMediateDatastreams");
         if (dsMediation == null || dsMediation.equalsIgnoreCase(""))
         {
           s_server.logWarning("[DisseminationService] Unable to resolve "
               + "doDatastreamMediation parameter from the configuration "
               + "file. ");
         } else
         {
           doDatastreamMediation = new Boolean(dsMediation).booleanValue();
         }
       }
     } catch (InitializationException ie)
     {
         System.err.println(ie.getMessage());
     }
   }
 
   /** The hashtable containing information required for datastream mediation. */
   protected static Hashtable dsRegistry = new Hashtable(1000);
 
   /**
    * <p>Constructs an instance of DisseminationService. Initializes two class
    * variables that contain the IP address and port number of the Fedora server.
    * The port number is obtained from the Fedora server config file and the IP
    * address of the server is obtained dynamically. These variables are needed
    * to perform the datastream proxy service for datastream requests.</p>
    */
   public DisseminationService()
   {  }
 /*
   public void checkState(Context context, String state, String dsID, String PID)
       throws ServerException
   {
     // Check Object State
     if ( state.equalsIgnoreCase("D")  &&
          ( context.get("canUseDeletedObject")==null
            || (!context.get("canUseDeletedObject").equals("true")) )
       )
     {
       throw new GeneralException("The requested dissemination for data object \""+PID+"\" is no "
           + "longer available. One of its datastreams (dsID=\""+dsID+"\") has been flagged for DELETION "
           + "by the repository administrator. ");
 
     } else if ( state.equalsIgnoreCase("I")  &&
                 ( context.get("canUseInactiveObject")==null
                   || (!context.get("canUseInactiveObject").equals("true")) )
               )
     {
       throw new GeneralException("The requested dissemination for data object \""+PID+"\" is no "
           + "longer available. One of its datastreams (dsID=\""+dsID+"\") has been flagged as INACTIVE "
           + "by the repository administrator. ");
     }
   }
   */
 
   /**
    * <p>Assembles a dissemination given an instance of <code>
    * DisseminationBindingInfo</code> which has the dissemination-related
    * information from the digital object and its associated Behavior
    * Mechanism object.</p>
    *
    * @param context The current context. 
    * @param PID The persistent identifier of the digital object.
    * @param h_userParms A hashtable of user-supplied method parameters.
    * @param dissBindInfoArray The associated dissemination binding information.
    * @return A MIME-typed stream containing the result of the dissemination.
    * @throws ServerException If unable to assemble the dissemination for any
    *         reason.
    */
   public MIMETypedStream assembleDissemination(Context context, String PID,
       Hashtable h_userParms, DisseminationBindingInfo[] dissBindInfoArray, String reposBaseURL)
       throws ServerException
   {
 
     if (reposBaseURL == null || reposBaseURL.equals(""))
     {
       throw new DisseminationException("[DisseminationService] was unable to "
           + "resolve the base URL of the Fedora Server. The URL specified was: \""
           + reposBaseURL + "\". This information is required by the Dissemination Service.");
     }
     
     String datastreamResolverServletURL = reposBaseURL + "/fedora/getDS?id=";
     if (fedora.server.Debug.DEBUG) {
         printBindingInfo(dissBindInfoArray);
     }
     long initStartTime = new Date().getTime();
     long startTime = new Date().getTime();
     String protocolType = null;
     DisseminationBindingInfo dissBindInfo = null;
     String dissURL = null;
     MIMETypedStream dissemination = null;
     boolean isRedirect = false;
 
     if (dissBindInfoArray != null && dissBindInfoArray.length > 0)
     {
       String replaceString = null;
       int numElements = dissBindInfoArray.length;
 
       // Get row(s) of binding info and perform string substitution
       // on DSBindingKey and method parameter values in WSDL
       // Note: In case where more than one datastream matches the
       // DSBindingKey or there are multiple DSBindingKeys for the
       // method, multiple rows will be present; otherwise there is only
       // a single row.
       for (int i=0; i<dissBindInfoArray.length; i++)
       {
        ((Authorization)s_server.getModule("Authorization")).enforce_Internal_DSState(
         		context, dissBindInfoArray[i].dsID, dissBindInfoArray[i].dsState);
         dissBindInfo = dissBindInfoArray[i];
 
         // Before doing anything, check whether we can replace any
         // placeholders in the datastream url with parameter values from
         // the request.  This supports the special case where a
         // datastream's URL is dependent on user parameters, such
         // as when the datastream is actually a dissemination that
         // takes parameters.
         if (dissBindInfo.dsLocation!=null && 
                 ( dissBindInfo.dsLocation.startsWith("http://") 
                   || dissBindInfo.dsLocation.startsWith("https://") )  ) {
             String[] parts=dissBindInfo.dsLocation.split("=\\("); // regex for =(
             if (parts.length>1) {
                 StringBuffer replaced=new StringBuffer();
                 replaced.append(parts[0]);
                 for (int x=1; x<parts.length; x++) {
                     replaced.append('=');
                     int rightParenPos=parts[x].indexOf(")");
                     if (rightParenPos!=-1 && rightParenPos>0) {
                         String key=parts[x].substring(0, rightParenPos);
                         String val=(String) h_userParms.get(key);
                         if (val!=null) {
                             // We have a match... so insert the urlencoded value.
                             try {
                                 replaced.append(URLEncoder.encode(val, "UTF-8"));
                             } catch (UnsupportedEncodingException uee) {
                                 // won't happen: java always supports UTF-8
                             }
                             if (rightParenPos<parts[x].length()) {
                                 replaced.append(parts[x].substring(rightParenPos+1));
                             }
                         } else {
                             replaced.append('(');
                             replaced.append(parts[x]);
                         }
                     } else {
                         replaced.append('(');
                         replaced.append(parts[x]);
                     }
                 }
                 dissBindInfo.dsLocation=replaced.toString();
             }
         }
 
         // Match DSBindingKey pattern in WSDL which is a string of the form:
         // (DSBindingKey). Rows in DisseminationBindingInfo are sorted
         // alphabetically on binding key.
         String bindingKeyPattern = "\\(" + dissBindInfo.DSBindKey + "\\)";
         if (i == 0)
         {
           // If addressLocation has a value of "LOCAL", this indicates
           // the associated operationLocation requires no addressLocation.
           // i.e., the operationLocation contains all information necessary
           // to perform the dissemination request. This is a special case
           // used when the web services are generally mechanisms like cgi-scripts,
           // java servlets, and simple HTTP GETs. Using the value of LOCAL
           // in the address location also enables one to have different methods
           // serviced by different hosts. In true web services like SOAP, the
           // addressLocation specifies the host name of the service and all
           // methods are served from that single host location.
           if (dissBindInfo.AddressLocation.equalsIgnoreCase(LOCAL_ADDRESS_LOCATION))
           {
             dissURL = dissBindInfo.OperationLocation;
           } else
           {
             dissURL = dissBindInfo.AddressLocation+dissBindInfo.OperationLocation;
           }
           protocolType = dissBindInfo.ProtocolType;
         }
         String currentKey = dissBindInfo.DSBindKey;
         String nextKey = "";
         if (i != numElements-1)
         {
           // Except for last row, get the value of the next binding key
           // to compare with the value of the current binding key.
           nextKey = dissBindInfoArray[i+1].DSBindKey;
         }
         s_server.logFinest("[DisseminationService] currentKey: '"
             + currentKey + "'");
         s_server.logFinest("[DisseminationService] nextKey: '"
             + nextKey + "'");
         // In most cases, there is only a single datastream that matches a
         // given DSBindingKey so the substitution process is to just replace
         // the occurence of (BINDING_KEY) with the value of the datastream
         // location. However, when multiple datastreams match the same
         // DSBindingKey, the occurrence of (BINDING_KEY) is replaced with the
         // value of the datastream location and the value +(BINDING_KEY) is
         // appended so that subsequent datastreams matching the binding key
         // will be substituted. The end result is that the binding key will
         // be replaced by a series of datastream locations separated by a
         // plus(+) sign. For example, in the case where 3 datastreams match
         // the binding key for PHOTO:
         //
         // file=(PHOTO) becomes
         // file=dslocation1+dslocation2+dslocation3
         //
         // It is the responsibility of the Behavior Mechanism to know how to
         // handle an input parameter with multiple datastream locations.
         //
         // In the case of a method containing multiple binding keys,
         // substitutions are performed on each binding key. For example, in
         // the case where there are 2 binding keys named PHOTO and WATERMARK
         // where each matches a single datastream:
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
           // Case where binding keys are equal which means that multiple
           // datastreams matched the same binding key.
           if (doDatastreamMediation  &&
               !dissBindInfo.dsControlGroupType.equalsIgnoreCase("R"))
           {
             // Use Datastream Mediation (except for redirected datastreams).
             replaceString = datastreamResolverServletURL
                 + registerDatastreamLocation(dissBindInfo.dsLocation,
                                            dissBindInfo.dsControlGroupType)
                 + "+(" + dissBindInfo.DSBindKey + ")";
           } else
           {
             // Bypass Datastream Mediation.
             if ( dissBindInfo.dsControlGroupType.equalsIgnoreCase("M") ||
                  dissBindInfo.dsControlGroupType.equalsIgnoreCase("X"))
             {
                 // Use the Default Disseminator syntax to resolve the internal
                 // datastream location for Managed and XML datastreams.
                 replaceString =
                     resolveInternalDSLocation(context, dissBindInfo.dsLocation, PID, reposBaseURL)
                         + "+(" + dissBindInfo.DSBindKey + ")";;
             } else {
                 replaceString =
                         dissBindInfo.dsLocation + "+(" + dissBindInfo.DSBindKey + ")";
             }
             if (dissBindInfo.dsControlGroupType.equalsIgnoreCase("R") &&
                 dissBindInfo.AddressLocation.equals(LOCAL_ADDRESS_LOCATION))
                 isRedirect = true;
           }
         } else
         {
           // Case where there are one or more binding keys.
           if (doDatastreamMediation &&
               !dissBindInfo.dsControlGroupType.equalsIgnoreCase("R"))
           {
             // Use Datastream Mediation (except for Redirected datastreams)
             replaceString = datastreamResolverServletURL
                 + registerDatastreamLocation(dissBindInfo.dsLocation,
                       dissBindInfo.dsControlGroupType);          
           } else
           {
             // Bypass Datastream Mediation.
             if ( dissBindInfo.dsControlGroupType.equalsIgnoreCase("M") ||
                  dissBindInfo.dsControlGroupType.equalsIgnoreCase("X"))
             {
                 // Use the Default Disseminator syntax to resolve the internal
                 // datastream location for Managed and XML datastreams.
                 replaceString =
                     resolveInternalDSLocation(context, dissBindInfo.dsLocation, PID, reposBaseURL);
             } else
             {
                 replaceString = dissBindInfo.dsLocation;
             }
             if (dissBindInfo.dsControlGroupType.equalsIgnoreCase("R") &&
                 dissBindInfo.AddressLocation.equals(LOCAL_ADDRESS_LOCATION))
                     isRedirect = true;
           }
         }
         try
         {
           // If the operationLocation contains datastreamInputParms
           // URLEncode each parameter before substitution. Otherwise, the
           // operationLocation has no parameters (i.e., it is a simple URL )
           // so bypass URLencoding.
           if (dissURL.indexOf("=(") != -1 )
           {
             dissURL = substituteString(dissURL, bindingKeyPattern, URLEncoder.encode(replaceString, "UTF-8"));
           } else
           {
             dissURL = substituteString(dissURL, bindingKeyPattern, replaceString);
           }
         } catch (UnsupportedEncodingException uee)
         {
           String message = "[DisseminationService] An error occured. The error "
               + "was \"" + uee.getClass().getName() + "\"  . The Reason was \""
               + uee.getMessage() + "\"  . String value: " + replaceString + "  . ";
           s_server.logFinest(message);
           throw new GeneralException(message);
         }
         s_server.logFinest("[DisseminationService] replaced dissURL: "
                            + dissURL.toString()
                            + " DissBindingInfo index: " + i);
       }
 
       // Substitute method parameter values in dissemination URL
       Enumeration e = h_userParms.keys();
       while (e.hasMoreElements())
       {
         String name = null;
         String value = null;
         try
         {
           name = URLEncoder.encode((String)e.nextElement(), "UTF-8");
           value = URLEncoder.encode((String)h_userParms.get(name), "UTF-8");
         } catch (UnsupportedEncodingException uee)
         {
           String message = "[DisseminationService] An error occured. The error "
               + "was \"" + uee.getClass().getName() + "\"  . The Reason was \""
               + uee.getMessage() + "\"  . Parameter name: " + name + "  . "
               + "Parameter value: " + value + "  .";
           s_server.logFinest(message);
           throw new GeneralException(message);
         }
         String pattern = "\\(" + name + "\\)";
         dissURL = substituteString(dissURL, pattern, value);
         s_server.logFinest("[DisseminationService] User parm substituted in "
             + "URL: " + dissURL);
       }
 
       // FIXME Need a more elegant means of handling optional userInputParm
       // method parameters that are not supplied by the invoking client;
       // for now, any optional parms that were not supplied are removed from
       // the outgoing URL. This works because parms are validated in
       // DefaultAccess to insure all required parms are present and all parm
       // names match parm names defined for the specific method. The only
       // unsubstituted parms left in the operationLocation string at this point
       // are those for optional parameters that the client omitted in the
       // initial request so they can safely be removed from the outgoing
       // dissemination URL. This step is only needed when optional parameters
       // are not supplied by the client.
       if (dissURL.indexOf("(") != -1)
       {
           dissURL = stripParms(dissURL);
           s_server.logFinest("[DisseminationService] Non-supplied optional "
               + "userInputParm values removed from URL: " + dissURL);
       }
 
       // Resolve content referenced by dissemination result.
       s_server.logFinest("[DisseminationService] ProtocolType: "+protocolType);
       if (protocolType.equalsIgnoreCase("http"))
       {
 
         if (isRedirect)
         {
           // The dsControlGroupType of Redirect("R") is a special control type
           // used primarily for streaming media. Datastreams of this type are
           // not mediated (proxied by Fedora) and their physical dsLocation is
           // simply redirected back to the client. Therefore, the contents
           // of the MIMETypedStream returned for dissemination requests will
           // contain the raw URL of the dsLocation and will be assigned a
           // special fedora-specific MIME type to identify the stream as
           // a MIMETypedStream whose contents contain a URL to which the client
           // should be redirected.
 
           InputStream is = null;
           try
           {
             is = new ByteArrayInputStream(dissURL.getBytes("UTF-8"));
           } catch (UnsupportedEncodingException uee)
           {
             String message = "[DisseminationService] An error has occurred. "
                 + "The error was a \"" + uee.getClass().getName() + "\"  . The "
                 + "Reason was \"" + uee.getMessage() + "\"  . String value: "
                 + dissURL + "  . ";
             s_server.logFinest(message);
             throw new GeneralException(message);
           }
           long stopTime = new Date().getTime();
           long interval = stopTime - startTime;
           s_server.logFiner("[DisseminationService] Roundtrip assembleDissemination: "
               + interval + " milliseconds.");
           dissemination = new MIMETypedStream("application/fedora-redirect",is, null);
         } else
         {
           // For all non-redirected disseminations, Fedora captures and returns
           // the MIMETypedStream resulting from the dissemination request.
           ExternalContentManager externalContentManager = (ExternalContentManager)
               s_server.getModule("fedora.server.storage.ExternalContentManager");
           long stopTime = new Date().getTime();
           long interval = stopTime - startTime;
           s_server.logFiner("[DisseminationService] Roundtrip assembleDissemination: "
               + interval + " milliseconds.");
           if (fedora.server.Debug.DEBUG) System.out.println("URL: "+dissURL);
           dissemination = externalContentManager.getExternalContent(dissURL, context);
         }
 
       } else if (protocolType.equalsIgnoreCase("soap"))
       {
         // FIXME!! future handling of soap bindings.
         String message = "[DisseminationService] Protocol type: "
             + protocolType + "NOT yet implemented";
         s_server.logWarning(message);
         throw new DisseminationException(message);
 
       } else
       {
         String message = "[DisseminationService] Protocol type: "
             + protocolType + "NOT supported.";
         s_server.logWarning(message);
         throw new DisseminationException(message);
       }
 
     } else
     {
       // DisseminationBindingInfo was empty so there was no information
       // provided to construct a dissemination.
       String message = "[DisseminationService] Dissemination Binding "+
                          "Info contained no data";
       s_server.logWarning(message);
       throw new DisseminationBindingInfoNotFoundException(message);
     }
     return dissemination;
   }
 
 
   /**
    * <p>Datastream locations are considered privileged information by the
    * Fedora repository. To prevent disclosing physical datastream locations
    * to external mechanism services, a proxy is used to disguise the datastream
    * locations. This method generates a temporary ID that maps to the
    * physical datastream location and registers this information in a
    * memory resident hashtable for subsequent resolution of the physical
    * datastream location. The servlet <code>DatastreamResolverServlet</code>
    * provides the proxy resolution service for datastreams.</p>
    * <p></p>
    * <p>The format of the tempID is derived from <code>java.sql.Timestamp</code>
    * with an arbitrary counter appended to the end to insure uniqueness. The
    * syntax is of the form:
    * <ul>
    * <p>YYYY-MM-DD HH:mm:ss.mmm:dddddd where</p>
    * <ul>
    * <li>YYYY - year (1900-8099)</li>
    * <li>MM - month (01-12)</li>
    * <li>DD - day (01-31)</li>
    * <li>hh - hours (0-23)</li>
    * <li>mm - minutes (0-59)</li>
    * <li>ss - seconds (0-59)</li>
    * <li>mmm - milliseconds (0-999)</li>
    * <li>dddddd - incremental counter (0-999999)</li>
    * </ul>
    * </ul>
    *
    * @param dsLocation The physical location of the datastream.
    * @param dsControlGroupType The type of the datastream.
    * @return A temporary ID used to reference the physical location of the
    *         specified datastream
    * @throws ServerException If an error occurs in registering a datastream
    *         location.
    */
   public String registerDatastreamLocation(String dsLocation,
       String dsControlGroupType) throws ServerException
   {
 
     String tempID = null;
     Timestamp timeStamp = null;
     if (counter > 999999) counter = 0;
     long currentTime = new Timestamp(new Date().getTime()).getTime();
     long expireLimit = currentTime -
                        (long)datastreamExpirationLimit*1000;
     try
     {
 
       // Remove any datastream registrations that have expired.
       // The expiration limit can be adjusted using the Fedora config parameter
       // named "datastreamExpirationLimit" which is in seconds.
       for ( Enumeration e = dsRegistry.keys(); e.hasMoreElements(); )
       {
         String key = (String)e.nextElement();
         timeStamp = Timestamp.valueOf(extractTimestamp(key));
         if (expireLimit > timeStamp.getTime())
         {
           dsRegistry.remove(key);
           s_server.logFinest("[DisseminationService] DatastreamMediationKey "
               + "removed from Hash: " + key);
         }
       }
 
       // Register datastream.
       if (tempID == null)
       {
         timeStamp = new Timestamp(new Date().getTime());
         tempID = timeStamp.toString()+":"+counter++;
         DatastreamMediation dm = new DatastreamMediation();
         dm.mediatedDatastreamID = tempID;
         dm.dsLocation = dsLocation;
         dm.dsControlGroupType = dsControlGroupType; 
         dsRegistry.put(tempID, dm);
         s_server.logFinest("[DisseminationService] DatastreammediationKey "
             + "added to Hash: " + tempID);
       }
 
     } catch(Throwable th)
     {
       throw new DisseminationException("[DisseminationService] register"
           + "DatastreamLocation: "
           + "returned an error. The underlying error was a "
           + th.getClass().getName() + " The message "
           + "was \"" + th.getMessage() + "\" .");
     }
 
     // Replace the blank between date and time with the character "T".
     return tempID.replaceAll(" ","T");
   }
 
   /**
    * <p>The tempID that is used for datastream mediation consists of a <code>
    * Timestamp</code> plus a counter appended to the end to insure uniqueness.
    * This method is a utility method used to extract the Timestamp portion
    * from the tempID by stripping off the arbitrary counter at the end of
    * the string.</p>
    *
    * @param tempID The tempID to be extracted.
    * @return The extracted Timestamp value as a string.
    */
   public String extractTimestamp(String tempID)
   {
     StringBuffer sb = new StringBuffer();
     sb.append(tempID);
     sb.replace(tempID.lastIndexOf(":"),tempID.length(),"");
     return sb.toString();
   }
 
   /**
    * <p>Performs simple string replacement using regular expressions.
    * All matching occurrences of the pattern string will be replaced in the
    * input string by the replacement string.
    *
    * @param inputString The source string.
    * @param patternString The regular expression pattern.
    * @param replaceString The replacement string.
    * @return The source string with substitutions.
    */
   private String substituteString(String inputString, String patternString,
                                  String replaceString)
   {
     Pattern pattern = Pattern.compile(patternString);
     Matcher m = pattern.matcher(inputString);
     return m.replaceAll(replaceString);
   }
 
   /**
    * <p> Removes any optional userInputParms which remain in the dissemination
    * URL. This occurs when a method has optional parameters and the user does
    * not supply a value for one or more of the optional parameters. The result
    * is a syntax similar to "parm=(PARM_BIND_KEY)". This method removes these
    * non-supplied optional parameters from the string.</p>
    *
    * @param dissURL String to be processed.
    * @return An edited string with parameters removed where no value was
    *         specified for any optional parameters.
    */
   private String stripParms(String dissURL)
   {
     String requestURI = dissURL.substring(0,dissURL.indexOf("?")+1);
     String parmString = dissURL.substring(dissURL.indexOf("?")+1,dissURL.length());
     String[] parms = parmString.split("&");
     StringBuffer sb = new StringBuffer();
     for (int i=0; i<parms.length; i++)
     {
       int len = parms[i].length() - 1;
       if (parms[i].lastIndexOf(")") != len)
       {
         sb.append(parms[i]+"&");
       }
     }
     int index = sb.lastIndexOf("&");
     if ( index != -1 && index+1 == sb.length())
       sb.replace(index,sb.length(),"");
     return requestURI+sb.toString();
   }
 
   /**
    * <p>Converts the internal dsLocation used by managed and XML type datastreams
    * to the corresponding Default Dissemination request that will return the
    * datastream contents.</p>
    *
    * @param internalDSLocation - dsLocation of the Managed or XML type datastream.
    * @param PID - the persistent identifier of the digital object.
    * @return - A URL corresponding to the Default Dissemination request for the
    *           specified datastream.
    * @throws ServerException - If anything goes wrong during the conversion attempt.
    */
   private String resolveInternalDSLocation(Context context, String internalDSLocation,
       String PID, String reposBaseURL) throws ServerException
   {
       
       if (reposBaseURL == null || reposBaseURL.equals(""))
       {
         throw new DisseminationException("[DisseminationService] was unable to "
             + "resolve the base URL of the Fedora Server. The URL specified was: \""
             + reposBaseURL + "\". This information is required by the Dissemination Service.");
       }  
       
       String[] s = internalDSLocation.split("\\+");
       String dsLocation = null;
       if (s.length == 3)
       {
           DOReader doReader =  m_manager.getReader(Server.GLOBAL_CHOICE, context, PID);
           Datastream d = (Datastream) doReader.getDatastream(s[1], s[2]);
           if (fedora.server.Debug.DEBUG) System.out.println("DSDate: "+DateUtility.convertDateToString(d.DSCreateDT));
           dsLocation = reposBaseURL
               +"/fedora/get/"+s[0]+"/"+s[1]+"/"
               +DateUtility.convertDateToString(d.DSCreateDT);
       } else
       {
         String message = "[DisseminationService] An error has occurred. "
             + "The internal dsLocation: \"" + internalDSLocation + "\" is "
             + "not in the required format of: "
             + "\"doPID+DSID+DSVERSIONID\" .";
         s_server.logFinest(message);
             throw new GeneralException(message);
       }
       return dsLocation;
   }
 
   public static void printBindingInfo(DisseminationBindingInfo[] info) {
     for (int i = 0; i < info.length; i++) {
       System.out.println("DisseminationBindingInfo[" + i + "]:");
       System.out.println("  DSBindKey          : " + info[i].DSBindKey);
       System.out.println("  dsLocation         : " + info[i].dsLocation);
       System.out.println("  dsControlGroupType : " + info[i].dsControlGroupType);
       System.out.println("  dsID               : " + info[i].dsID);
       System.out.println("  dsVersionID        : " + info[i].dsVersionID);
       System.out.println("  AddressLocation    : " + info[i].AddressLocation);
       System.out.println("  OperationLocation  : " + info[i].OperationLocation);
       System.out.println("  ProtocolType       : " + info[i].ProtocolType);
       System.out.println("  dsState            : " + info[i].dsState);
       for (int j = 0; j < info[i].methodParms.length; j++) {
         MethodParmDef def = info[i].methodParms[j];
         System.out.println("  MethodParamDef[" + j + "]:");
         System.out.println("    parmName         : " + def.parmName);
         System.out.println("    parmDefaultValue : " + def.parmDefaultValue);
         System.out.println("    parmRequired     : " + def.parmRequired);
         System.out.println("    parmLabel        : " + def.parmLabel);
         System.out.println("    parmPassBy       : " + def.parmPassBy);
         for (int k = 0; k < def.parmDomainValues.length; k++) {
           System.out.println("    parmDomainValue  : " + def.parmDomainValues[k]);
         }
       }
     }
   }
 }
