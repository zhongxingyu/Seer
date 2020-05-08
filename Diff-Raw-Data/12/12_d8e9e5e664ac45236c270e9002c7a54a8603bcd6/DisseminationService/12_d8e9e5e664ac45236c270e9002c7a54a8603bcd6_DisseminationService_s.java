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
 import java.util.Hashtable;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
import java.text.SimpleDateFormat;
 
 import fedora.server.Server;
 import fedora.server.errors.DisseminationException;
 import fedora.server.errors.DisseminationBindingInfoNotFoundException;
 import fedora.server.errors.GeneralException;
 import fedora.server.errors.InitializationException;
 import fedora.server.errors.ServerException;
 import fedora.server.errors.ServerInitializationException;
 import fedora.server.storage.ExternalContentManager;
 import fedora.server.storage.types.DatastreamMediation;
 import fedora.server.storage.types.DisseminationBindingInfo;
 import fedora.server.storage.types.MIMETypedStream;
 
 /**
  * <p><b>Title: </b>DisseminationService.java</p>
  * <p><b>Description: </b>A service for executing a dissemination given its
  * binding information.</p>
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
  * <p>The entire file consists of original code.  Copyright &copy; 2002, 2003 by The
  * Rector and Visitors of the University of Virginia and Cornell University.
  * All rights reserved.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * @author rlw@virginia.edu
  * @version $Id$
  */
 public class DisseminationService
 {
 
   /** The Fedora Server instance */
   private static Server s_server;
 
   /** Signifies the special type of address location known as LOCAL.
    *  An address location of LOCAL implies that no remote host name is
    *  required for the address location and that the contents of the
    *  operation location are sufficient to execute the associated mechanism.
    */
   private static final String LOCAL_ADDRESS_LOCATION = "LOCAL";
 
   /** Port number on which the Fedora server is running; determined from
    * fedora.fcfg config file.
    */
   private static String fedoraServerPort = null;
 
   /** Hostname of the Fedora server determined from
    * fedora.fcfg config file, or (fallback) by hostIP.getHostName()
    */
   private static String fedoraServerHost = null;
 
   /** URL of the DatastreamResolverServlet; built dynamically.*/
   private static String datastreamResolverServletURL = null;
 
   /** The expiration limit in minutes for removing entries from the database. */
   private static int datastreamExpirationLimit = 0;
 
   /** An incremental counter used to insure uniqueness of tempIDs used for
    * datastream mediation.
    */
   private static int counter = 0;
 
   /** The IP address of the local host; determined dynamically. */
   private static InetAddress hostIP = null;
 
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
         hostIP = null;
         fedoraServerPort = s_server.getParameter("fedoraServerPort");
         s_server.logFinest("fedoraServerPort: " + fedoraServerPort);
         hostIP = InetAddress.getLocalHost();
         fedoraServerHost = s_server.getParameter("fedoraServerHost");
         if (fedoraServerHost==null || fedoraServerHost.equals("")) {
             fedoraServerHost=hostIP.getHostName();
         }
         s_server.logFinest("[DisseminationService] "
             + "datastreamResolverServletURL: " + datastreamResolverServletURL);
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
 
     } catch (UnknownHostException uhe)
     {
       System.err.println("[DisseminationService] was unable to "
           + "resolve the IP address of the Fedora Server: "
           + datastreamResolverServletURL + "."
           + " The underlying error was a "
           + uhe.getClass().getName() + "The message "
           + "was \"" + uhe.getMessage() + "\"");
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
    *
    * @throws ServerException If the port number or the IP address of the Fedora
    *         server cannot be obtained.
    */
   public DisseminationService() throws ServerException
   {
 
     if (this.fedoraServerPort == null || fedoraServerPort.equalsIgnoreCase(""))
     {
       throw new DisseminationException("[DisseminationService] was unable to "
           + "resolve the port number of the Fedora Server: "
           + datastreamResolverServletURL + "from the configuration file. This "
           + "information is required by the Dissemination Service.");
     }
     if (this.hostIP == null)
     {
       throw new DisseminationException("[DisseminationService] was unable to "
           + "resolve the IP address of the Fedora Server: "
           + datastreamResolverServletURL + " .");
       }
   }
 
   /**
    * <p>Assembles a dissemination given an instance of <code>
    * DisseminationBindingInfo</code> which has the dissemination-related
    * information from the digital object and its associated Behavior
    * Mechanism object.</p>
    *
    * @param PID The persistent identifier of the digital object.
    * @param h_userParms A hashtable of user-supplied method parameters.
    * @param dissBindInfoArray The associated dissemination binding information.
    * @return A MIME-typed stream containing the result of the dissemination.
    * @throws ServerException If unable to assemble the dissemination for any
    *         reason.
    */
   public MIMETypedStream assembleDissemination(String PID,
      Hashtable h_userParms, DisseminationBindingInfo[] dissBindInfoArray,
      Date asOfDateTime)
       throws ServerException
   {
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
         dissBindInfo = dissBindInfoArray[i];
 
         // Before doing anything, check whether we can replace any
         // placeholders in the datastream url with parameter values from
         // the request.  This supports the special case where a
         // datastream's URL is dependent on user parameters, such
         // as when the datastream is actually a dissemination that
         // takes parameters.
         if (dissBindInfo.dsLocation!=null
                 && dissBindInfo.dsLocation.startsWith("http://")) {
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
           datastreamResolverServletURL = "http://" + fedoraServerHost
               + ":" + fedoraServerPort + "/fedora/getDS?id=";
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
           if ((doDatastreamMediation ||
               dissBindInfo.dsControlGroupType.equalsIgnoreCase("M") ||
               dissBindInfo.dsControlGroupType.equalsIgnoreCase("X")) &&
               !dissBindInfo.dsControlGroupType.equalsIgnoreCase("R"))
           {
             // Use Datastream Mediation.
             replaceString = datastreamResolverServletURL
                 + registerDatastreamLocation(dissBindInfo.dsLocation,
                                            dissBindInfo.dsControlGroupType)
                 + "+(" + dissBindInfo.DSBindKey + ")";
           } else
           {
             // Bypass Datastream Mediation.
             replaceString = dissBindInfo.dsLocation
                 + "+(" + dissBindInfo.DSBindKey + ")";
             if (dissBindInfo.dsControlGroupType.equalsIgnoreCase("R") &&
                 dissBindInfo.AddressLocation.equals(LOCAL_ADDRESS_LOCATION))
                     isRedirect = true;
           }
         } else
         {
           // Case where there are one or more binding keys.
           if ((doDatastreamMediation ||
               dissBindInfo.dsControlGroupType.equalsIgnoreCase("M") ||
               dissBindInfo.dsControlGroupType.equalsIgnoreCase("X")) &&
               !dissBindInfo.dsControlGroupType.equalsIgnoreCase("R"))
           {
             // Use Datastream Mediation.
             replaceString = datastreamResolverServletURL
                 + registerDatastreamLocation(dissBindInfo.dsLocation,
                                              dissBindInfo.dsControlGroupType);
           } else
           {
             // Bypass Datastream Mediation.
             replaceString = dissBindInfo.dsLocation;
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
         long startTime = new Date().getTime();
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
 
           // RLW: change required by conversion fom byte[] to InputStream
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
           //dissemination = new MIMETypedStream("application/fedora-redirect",dissURL.getBytes());
           dissemination = new MIMETypedStream("application/fedora-redirect",is);
           // RLW: change required by conversion fom byte[] to InputStream
         } else
         {
           // For all non-redirected disseminations, Fedora captures and returns
           // the MIMETypedStream resulting from the dissemination request.
           ExternalContentManager externalContentManager = (ExternalContentManager)
               s_server.getModule("fedora.server.storage.ExternalContentManager");
           dissemination = externalContentManager.getExternalContent(dissURL);
         }
         long stopTime = new Date().getTime();
         long interval = stopTime - startTime;
 
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
    * <p>YYYY-MM-DD hh:mm:ss.mmm:dddddd where</p>
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
         timeStamp = timeStamp.valueOf(extractTimestamp(key));
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
 }
