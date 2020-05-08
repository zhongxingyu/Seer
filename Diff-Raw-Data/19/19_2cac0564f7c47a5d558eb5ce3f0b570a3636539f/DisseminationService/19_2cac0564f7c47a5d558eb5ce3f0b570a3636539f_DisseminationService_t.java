 package fedora.server.access;
 
 import java.io.File;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import fedora.server.Server;
 import fedora.server.access.localservices.HttpService;
 import fedora.server.errors.DisseminationBindingInfoNotFoundException;
 import fedora.server.errors.HttpServiceNotFoundException;
 import fedora.server.errors.InitializationException;
 import fedora.server.errors.ServerException;
 import fedora.server.errors.ServerInitializationException;
 import fedora.server.storage.types.DisseminationBindingInfo;
 import fedora.server.storage.types.MIMETypedStream;
 import fedora.server.storage.types.Property;
 
 /**
  * <p>Title: DisseminationService.java</p>
  * <p>Description: Provides a mechanism for constructing a dissemination
  * given its binding information.</p>
  *
  * <p>Copyright: Copyright (c) 2002</p>
  * <p>Company: </p>
  * @author Ross Wayland
  * @version 1.0
  */
 public class DisseminationService
 {
   /** The Fedora Server instance */
   private static Server s_server;
 
   /** The exception indicating that initialization failed. */
   private static InitializationException s_initException;
   private static final String CONTENT_TYPE_XML = "text/xml";
   private static final String LOCAL_ADDRESS_LOCATION = "LOCAL";
  private static boolean debug = true;
   private Hashtable h_userParms = new Hashtable();
 
   static
   {
     try
     {
       String fedoraHome=System.getProperty("fedora.home");
       if (fedoraHome == null) {
           throw new ServerInitializationException(
               "Server failed to initialize: The 'fedora.home' "
               + "system property was not set.");
       } else {
           s_server=Server.getInstance(new File(fedoraHome));
           Boolean B1 = new Boolean(s_server.getParameter("debug"));
           debug = B1.booleanValue();
       }
       s_server.logFinest("successful instance of server");
     } catch (InitializationException ie) {
         System.err.println(ie.getMessage());
     }
   }
 
   /**
    * <p>Assembles a dissemination given an instance of <code>
    * DisseminationBindingInfo</code> which has the dissemination-related
    * information from the digital object and its associated Behavior
    * Mechanism object.</p>
    *
    * @param userParms An array of user-supplied method parameters.
    * @param asOfDate The versioning datetime stamp.
    * @param dissBindInfoArray The associated dissemination binding information.
    * @return A MIME-typed stream containing the result of the dissemination.
    * @throws ServerException If unable to assemble the dissemination for any
    * reason.
    */
   public MIMETypedStream assembleDissemination(Property[] userParms,
       DisseminationBindingInfo[] dissBindInfoArray)
       throws ServerException
   {
     String protocolType = null;
     DisseminationBindingInfo dissBindInfo = null;
     String dissURL = null;
     String operationLocation = null;
     MIMETypedStream dissemination = null;
    if (userParms != null && userParms.length > 0)
     {
      s_server.logFinest("assemble" + userParms.length+ "parms: "+userParms);
      for (int i=0; i<userParms.length; i++)
      {
        h_userParms.put(userParms[i].name, userParms[i].value);
      }
     }
      if (dissBindInfoArray != null)
      {
        String replaceString = null;
       int numElements = dissBindInfoArray.length;
 
       // Get row(s) of WSDL results and perform string substitution
       // on DSBindingKey and method parameter values in WSDL
       // Note: In case where more than one datastream matches the
       // DSBindingKey or there are multiple DSBindingKeys for the
       // method, multiple rows will be returned; otherwise
       // a single row is returned.
       for (int i=0; i<dissBindInfoArray.length; i++)
       {
         dissBindInfo = dissBindInfoArray[i];
 
         // If AddressLocation has a value of "LOCAL", this is a flag to
         // indicate the associated OperationLocation requires no
         // AddressLocation. i.e., the OperationLocation contains all
         // information necessary to perform the dissemination request.
         if (dissBindInfo.AddressLocation.equalsIgnoreCase(LOCAL_ADDRESS_LOCATION))
         {
           dissBindInfo.AddressLocation = "";
         }
 
         // Match DSBindingKey pattern in WSDL
         String bindingKeyPattern = "\\("+dissBindInfo.DSBindKey+"\\)";
         if (i == 0)
         {
           operationLocation = dissBindInfo.OperationLocation;
           dissURL = dissBindInfo.AddressLocation+dissBindInfo.OperationLocation;
           protocolType = dissBindInfo.ProtocolType;
         }
         if (debug) s_server.logFinest("counter: "+i+" numelem: "+numElements);
         String currentKey = dissBindInfo.DSBindKey;
         String nextKey = "";
         if (i != numElements-1)
         {
           // Except for last row, get the value of the next binding key
           // to compare with the value of the current binding key.
           if (debug) s_server.logFinest("currentKey: '"+currentKey+"'");
           nextKey = dissBindInfoArray[i+1].DSBindKey;
           if (debug) s_server.logFinest("' nextKey: '"+nextKey+"'");
         }
 
         // In most cases, there is only a single datastream that matches a
         // given DSBindingKey so the substitution process is to just replace
         // the occurence of (BINDING_KEY) with the value of the datastream
         // location. However, when multiple datastreams match the same
         // DSBindingKey, the occurrence of (BINDING_KEY) is replaced with the
         // value of the datastream location and the value +(BINDING_KEY) is
         // appended so that subsequent datastreams matching the binding key
         // will be substituted. The end result is that the binding key will
         // be replaced by a string datastream locations separated by a plus(+)
         // sign. e.g.,
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
           replaceString = dissBindInfo.DSLocation+"+("+dissBindInfo.DSBindKey+")";
         } else
         {
           replaceString = dissBindInfo.DSLocation;
         }
         if (debug) s_server.logFinest("replaceString: "+replaceString);
         dissURL = substituteString(dissURL, bindingKeyPattern, replaceString);
         if (debug) s_server.logFinest("replaced dissURL = "+
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
         if (debug) s_server.logFinest("UserParmSubstitution dissURL: "+
                                       dissURL);
       }
 
       // Resolve content referenced by dissemination result
       if (debug) s_server.logFinest("ProtocolType = "+protocolType);
       if (protocolType.equalsIgnoreCase("http"))
       {
         // FIXME!! need to implement Access Policy control.
         // If access is based on restrictions to content,
         // this is the last chance to apply those restrictions
         // before returnign dissemination result to client.
         HttpService httpService = new HttpService(dissURL);
         try
         {
           if (debug) s_server.logFinest("initiating httpservice call");
           dissemination = httpService.getHttpContent(dissURL);
           if (debug) s_server.logFinest("dissMIME: "+dissemination.MIMEType);
         } catch (HttpServiceNotFoundException hsnfe)
         {
           s_server.logWarning(hsnfe.getMessage());
           throw hsnfe;
         }
       } else if (protocolType.equalsIgnoreCase("soap"))
       {
         // FIXME!! future handling of soap bindings
         s_server.logWarning("Protocol type specified: "+protocolType);
         dissemination = null;
       } else
       {
         s_server.logWarning("Unknown protocol type: "+protocolType);
         dissemination = null;
       }
     } else
     {
       // DisseminationBindingInfo was empty so there was no information
       // provided to construct a dissemination
       s_server.logWarning("DisseminationService: Dissemination Binding "+
                          "Info Not Found");
       throw new DisseminationBindingInfoNotFoundException("Dissemination "+
          "Binding Info Not Found");
     }
      return dissemination;
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
 }
