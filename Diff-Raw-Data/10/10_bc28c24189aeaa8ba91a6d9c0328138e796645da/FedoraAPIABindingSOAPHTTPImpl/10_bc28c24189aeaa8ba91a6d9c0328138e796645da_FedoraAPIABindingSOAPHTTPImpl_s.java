 /**
  * FedoraAPIABindingSOAPHTTPImpl.java
  *
  * This file was auto-generated from WSDL
  * by the Apache Axis WSDL2Java emitter.
  */
 
 package fedora.server.access;
 
 import fedora.server.storage.DefinitiveDOReader;
 import fedora.server.storage.DefinitiveBMechReader;
 import fedora.server.storage.types.MIMETypedStream;
 import fedora.server.storage.FastDOReader;
 import fedora.server.access.localservices.HttpService;
 import fedora.server.errors.ObjectNotFoundException;
 import fedora.server.errors.MethodParmNotFoundException;
 import fedora.server.errors.HttpServiceNotFoundException;
 import fedora.server.utilities.DateUtility;
 import java.util.Enumeration;
 import java.util.Vector;
 import java.util.Date;
 import java.util.Calendar;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.PrintWriter;
 import java.util.Hashtable;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class FedoraAPIABindingSOAPHTTPImpl implements fedora.server.access.FedoraAPIA
 {
   private static final String CONTENT_TYPE_XML = "text/xml";
   private static final String LOCAL_ADDRESS_LOCATION = "LOCAL";
   private static final boolean debug = true;
 
     public java.lang.String[] getBehaviorDefinitions(java.lang.String PID, java.util.Calendar asOfDateTime) throws java.rmi.RemoteException {
       DefinitiveDOReader doReader = new DefinitiveDOReader(PID);
       // FIXME!! versioning based on datetime not yet implemented
     return doReader.GetBehaviorDefs(null);
     }
 
     public fedora.server.types.gen.MIMETypedStream getBehaviorMethods(java.lang.String PID, java.lang.String bDefPID, java.util.Calendar asOfDateTime) throws java.rmi.RemoteException {
       ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
       fedora.server.types.gen.MIMETypedStream bDefMethods = null;
       try
       {
         DefinitiveBMechReader doReader = new DefinitiveBMechReader(bDefPID);
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
       //bDefMethods = new MIMETypedStream(CONTENT_TYPE_XML,baos.toByteArray());
       bDefMethods = new fedora.server.types.gen.MIMETypedStream();
       bDefMethods.setMIMEType(CONTENT_TYPE_XML);
       bDefMethods.setStream(baos.toByteArray());
 
       return(bDefMethods);
 
     }
 
     public fedora.server.types.gen.MIMETypedStream getDissemination(java.lang.String PID, java.lang.String bDefPID, java.lang.String methodName, fedora.server.types.gen.Property[] parameters, java.util.Calendar asOfDateTime) throws java.rmi.RemoteException {
 
       String protocolType = null;
       Vector dissResult = null;
       String dissURL = null;
       String operationLocation = null;
       fedora.server.types.gen.MIMETypedStream dissemination = null;
       Date versDateTime = null;
       if (!(asOfDateTime == null))
       {
         versDateTime = DateUtility.convertCalendarToDate(asOfDateTime);
       }
       FastDOReader fastReader = null;
       try
       {
        fastReader = new FastDOReader(PID, bDefPID, methodName,
            versDateTime);
        dissResult = fastReader.getDissemination(PID, bDefPID, methodName,
            versDateTime);
         String replaceString = null;
         DissResultSet results = new DissResultSet();
         // Build a hashtable of the dissemination result sets to be used
         // as a way of indexing the different result sets.
         Enumeration e = dissResult.elements();
         Hashtable h = new Hashtable();
         int index = 1;
         Integer key = new Integer(index);
         while (e.hasMoreElements())
         {
           results = new DissResultSet((String[])e.nextElement());
           if (debug) System.out.println("KEY: "+key+" VALUE: "+results.dsBindingKey);
           h.put(key,results);
           index++;
           key = new Integer(index);
         }
         int counter = 1;
         int numElements = dissResult.size();
         e = dissResult.elements();
         // Get row(s) of WSDL results and perform string substitution
         // on DSBindingKey and method parameter values in WSDL
         // Note: In case where more than one datastream matches the
         // DSBindingKey or there are multiple DSBindingKeys for the
         // method, multiple rows will be returned; otherwise
         // a single row is returned.
         while (e.hasMoreElements())
         {
           results = new DissResultSet((String[])e.nextElement());
           // If AddressLocation is LOCAL, this is a flag to indicate
           // the associated OperationLocation requires no AddressLocation.
           // i.e., the OperationLocation contains all information necessary
           // to perform the dissemination request.
           if (results.addressLocation.equals(LOCAL_ADDRESS_LOCATION))
           {
             results.addressLocation = "";
           }
           // Match DSBindingKey pattern in WSDL
           String bindingKeyPattern = "\\("+results.dsBindingKey+"\\)";
           if (counter == 1)
           {
             operationLocation = results.operationLocation;
             dissURL = results.addressLocation+operationLocation;
             protocolType = results.protocolType;
           }
           if (debug) System.out.println("counter: "+counter+" numelem: "+numElements);
           String currentKey = results.dsBindingKey;
           Integer hashKey = null;
           String nextKey = "";
           if (counter != numElements)
           {
             // Except for last row, get the value of the next binding key
             // out of the hashtable to compare with the value of the current
             // binding key.
             hashKey = new Integer(counter+1);
             if (debug) System.out.println("hashKey: '"+hashKey+"' currentKey: '"+currentKey+"'");
             DissResultSet result2 = (DissResultSet)h.get(hashKey);
             nextKey = result2.dsBindingKey;
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
           if (nextKey.equalsIgnoreCase(currentKey) & counter != numElements)
           {
             replaceString = results.dsLocation+"+("+results.dsBindingKey+")";
           } else
           {
             replaceString = results.dsLocation;
           }
           if (debug) System.out.println("replaceString: "+replaceString);
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
           HttpService httpService = new HttpService(dissURL);
           MIMETypedStream diss = null;
           try
           {
             diss = httpService.getHttpContent(dissURL);
             dissemination = new fedora.server.types.gen.MIMETypedStream();
             dissemination.setMIMEType(diss.MIMEType);
             dissemination.setStream(diss.stream);
           } catch (HttpServiceNotFoundException onfe)
           {
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
         // Object was not found in SQL database or in XML storage area
         System.out.println("getdissem: ObjectNotFound");
       }
      return dissemination;
     }
 
     /**
     * Gets a list of Disseminator IDs, Behavior Definition PIDs, 			BehaviorMechanism
     * PIDs, and methodNames supported by a digital object. 			This is a
     * set of methodNames, one for each behavior mechanism type associated
     * with each of the object's disseminator(s).
     */
     public fedora.server.types.gen.Vector viewObject(java.lang.String PID, java.util.Calendar asOfDateTime) throws java.rmi.RemoteException
     {
       Date versDateTime = DateUtility.convertCalendarToDate(asOfDateTime);
       FastDOReader fastReader = null;
       Vector queryResults = null;
       MIMETypedStream mts = null;
       ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
       PrintWriter out = new PrintWriter(baos);
       try
       {
        fastReader = new FastDOReader(PID, null, null, versDateTime);
         queryResults = fastReader.getObject(PID, versDateTime);
       } catch (ObjectNotFoundException onfe)
       {
         System.out.println(onfe.getMessage());
         //this.getServletContext().log(onfe.getMessage(), onfe.getCause());
       }
       Enumeration e = queryResults.elements();
       fedora.server.types.gen.Vector results = new fedora.server.types.gen.Vector();
       Object[] o = new Object[queryResults.size()];
       int count = 0;
       while (e.hasMoreElements())
       {
         o[count] = e.nextElement();
         count++;
       }
       results.setItem(o);
       return(results);
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
 
 }
