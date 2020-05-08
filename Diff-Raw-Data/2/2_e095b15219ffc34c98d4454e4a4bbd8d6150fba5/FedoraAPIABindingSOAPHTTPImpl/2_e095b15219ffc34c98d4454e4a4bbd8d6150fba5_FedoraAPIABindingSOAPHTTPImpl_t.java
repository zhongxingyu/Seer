 package fedora.server.access;
 
 import java.io.File;
 import java.rmi.RemoteException;
 import javax.servlet.http.HttpServletRequest;
 import org.apache.axis.MessageContext;
 import org.apache.axis.transport.http.HTTPConstants;
 import org.apache.axis.types.NonNegativeInteger;
 
 import fedora.server.Context;
 import fedora.server.Server;
 import fedora.server.ReadOnlyContext;
 import fedora.server.errors.InitializationException;
 import fedora.server.errors.ServerException;
 import fedora.server.errors.ServerInitializationException;
 import fedora.server.security.Authorization;
 import fedora.server.types.gen.FieldSearchQuery;
 import fedora.server.types.gen.FieldSearchResult;
 import fedora.server.utilities.AxisUtility;
 import fedora.server.utilities.DateUtility;
 import fedora.server.utilities.TypeUtility;
 
 /**
  * <p><b>Title: </b>FedoraAPIABindingSOAPHTTPImpl.java</p>
  * <p><b>Description: </b>Implements the Fedora Access SOAP service.</p>
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
  * <p>The entire file consists of original code.  Copyright &copy; 2002-2005 by The
  * Rector and Visitors of the University of Virginia and Cornell University.
  * All rights reserved.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * @author rlw@virginia.edu
  * @version $Id$
  */
 public class FedoraAPIABindingSOAPHTTPImpl implements
     fedora.server.access.FedoraAPIA
 {
   /** The Fedora Server instance. */
   private static Server s_server;
 
   /** Whether the service has initialized... true if initialized. */
   private static boolean s_initialized;
 
   /** The exception indicating that initialization failed. */
   private static InitializationException s_initException;
 
   /** Instance of the access subsystem */
   private static Access s_access;
 
   /** Context for cached objects. */
   private static ReadOnlyContext context;
 
   /** Debug toggle for testing. */
   private static boolean debug = false;
 
   /** Before fulfilling any requests, make sure we have a server instance. */
   static
   {
     try
     {
       String fedoraHome=System.getProperty("fedora.home");
       if (fedoraHome == null) {
           s_initialized = false;
           s_initException = new ServerInitializationException(
               "Server failed to initialize: The 'fedora.home' "
               + "system property was not set.");
       } else {
           s_server=Server.getInstance(new File(fedoraHome));
           s_initialized = true;
           s_access =
               (Access) s_server.getModule("fedora.server.access.Access");
           Boolean B1 = new Boolean(s_server.getParameter("debug"));
           debug = B1.booleanValue();
           s_server.logFinest("got server instance: " +
                              "s_init: "+s_initialized);
       }
     } catch (InitializationException ie) {
         System.err.println(ie.getMessage());
         s_initialized = false;
         s_initException = ie;
     }
   }
 
   private Context getCachedContext() {
       HttpServletRequest req=(HttpServletRequest) MessageContext.
               getCurrentContext().getProperty(
               HTTPConstants.MC_HTTP_SERVLETREQUEST);
     return ReadOnlyContext.getContext(Authorization.ENVIRONMENT_REQUEST_SOAP_OR_REST_SOAP, req, true);
   }
 
   private Context getUncachedContext() {
       HttpServletRequest req=(HttpServletRequest) MessageContext.
               getCurrentContext().getProperty(
               HTTPConstants.MC_HTTP_SERVLETREQUEST);
       return ReadOnlyContext.getContext(Authorization.ENVIRONMENT_REQUEST_SOAP_OR_REST_SOAP, req, false);
   }
 
   public java.lang.String[] getObjectHistory(java.lang.String PID)
           throws java.rmi.RemoteException
   {
     Context context=getCachedContext();
     assertInitialized();
     try
     {
       String[] bDefs =
           s_access.getObjectHistory(context, PID);
       if (bDefs != null && debug)
       {
         for (int i=0; i<bDefs.length; i++)
         {
           s_server.logFinest("bDef["+i+"] = "+bDefs[i]);
         }
       }
       return bDefs;
     } catch (ServerException se)
     {
       s_server.logFinest("ServerException: " + se.getMessage());
       logStackTrace(se);
       AxisUtility.throwFault(se);
     } catch (Exception e) {
       s_server.logFinest("Exception: " + e.getMessage());
       logStackTrace(e);
       AxisUtility.throwFault(
           new ServerInitializationException(e.getClass().getName() + ": "
           + e.getMessage()));
     }
     return null;
   }
 
   /**
    * <p>Gets a MIME-typed bytestream containing the result of a dissemination.
    * </p>
    *
    * @param PID The persistent identifier of the Digital Object.
    * @param bDefPID The persistent identifier of the Behavior Definition object.
    * @param methodName The name of the method.
    * @param asOfDateTime The version datetime stamp of the digital object.
    * @param userParms An array of user-supplied method parameters and values.
    * @return A MIME-typed stream containing the dissemination result.
    * @throws java.rmi.RemoteException
    */
   public fedora.server.types.gen.MIMETypedStream getDissemination(
                                   String PID,
                                   String bDefPID,
                                   String methodName,
                                   fedora.server.types.gen.Property[] userParms,
                                   String asOfDateTime)
       throws java.rmi.RemoteException
   {
     Context context=getCachedContext();
     try
     {
       fedora.server.storage.types.Property[] properties =
           TypeUtility.convertGenPropertyArrayToPropertyArray(userParms);
       fedora.server.storage.types.MIMETypedStream mimeTypedStream =
           s_access.getDissemination(context,
                                     PID,
                                     bDefPID,
                                     methodName,
                                     properties,
                                     DateUtility.convertStringToDate(asOfDateTime));
       fedora.server.types.gen.MIMETypedStream genMIMETypedStream =
           TypeUtility.convertMIMETypedStreamToGenMIMETypedStream(
           mimeTypedStream);
       return genMIMETypedStream;
     } catch (ServerException se)
     {
       logStackTrace(se);
       AxisUtility.throwFault(se);
     } catch (Exception e) {
       logStackTrace(e);
       AxisUtility.throwFault(
           new ServerInitializationException(e.getClass().getName() + ": "
           + e.getMessage()));
     }
     return null;
   }
 
   public fedora.server.types.gen.MIMETypedStream getDatastreamDissemination(
                                   String PID,
                                   String dsID,
                                   String asOfDateTime)
       throws java.rmi.RemoteException
   {
     Context context=getUncachedContext();
     try
     {
 
       fedora.server.storage.types.MIMETypedStream mimeTypedStream =
           s_access.getDatastreamDissemination(context,
                                     PID,
                                     dsID,
                                     DateUtility.convertStringToDate(asOfDateTime));
       fedora.server.types.gen.MIMETypedStream genMIMETypedStream =
           TypeUtility.convertMIMETypedStreamToGenMIMETypedStream(
           mimeTypedStream);
       return genMIMETypedStream;
     } catch (ServerException se)
     {
       logStackTrace(se);
       AxisUtility.throwFault(se);
     } catch (Exception e) {
       logStackTrace(e);
       AxisUtility.throwFault(
           new ServerInitializationException(e.getClass().getName() + ": "
           + e.getMessage()));
     }
     return null;
   }
 
   public FieldSearchResult findObjects(String[] resultFields,
           NonNegativeInteger maxResults, FieldSearchQuery query)
           throws RemoteException {
       Context context=getCachedContext();
       assertInitialized();
       try {
           fedora.server.search.FieldSearchResult result=s_access.
                   findObjects(context, resultFields, maxResults.intValue(),
                   TypeUtility.convertGenFieldSearchQueryToFieldSearchQuery(
                   query));
           return TypeUtility.convertFieldSearchResultToGenFieldSearchResult(result);
       } catch (ServerException se) {
           logStackTrace(se);
           throw AxisUtility.getFault(se);
       } catch (Exception e) {
           logStackTrace(e);
           throw AxisUtility.getFault(e);
       }
   }
 
 /*
   public ObjectFields[] advancedFieldSearch(
           String[] resultFields, Condition[] conditions)
           List searchConditionList=TypeUtility.
                   convertGenConditionArrayToSearchConditionList(conditions);
           List objectFields=s_access.search(context, resultFields, searchConditionList);
           return TypeUtility.convertSearchObjectFieldsListToGenObjectFieldsArray(
                   objectFields);
 
   public ObjectFields[] simpleFieldSearch(
           String[] resultFields, String terms)
           List objectFields=s_access.search(context, resultFields, terms);
           return TypeUtility.convertSearchObjectFieldsListToGenObjectFieldsArray(
                   objectFields);
 */
 
   public FieldSearchResult resumeFindObjects(String sessionToken)
           throws java.rmi.RemoteException {
       Context context=getCachedContext();
       assertInitialized();
       try {
           fedora.server.search.FieldSearchResult result=s_access.
                   resumeFindObjects(context, sessionToken);
           return TypeUtility.convertFieldSearchResultToGenFieldSearchResult(result);
       } catch (ServerException se) {
           logStackTrace(se);
           throw AxisUtility.getFault(se);
       } catch (Exception e) {
           logStackTrace(e);
           throw AxisUtility.getFault(e);
       }
   }
 
   /**
    * <p>Gets a list of all method definitions for the specified object.</p>
    *
    * @param PID The persistent identifier for the digital object.
    * @param asOfDateTime The versioning datetime stamp.
    * @return An array of object method definitions.
    * @throws java.rmi.RemoteException
    */
   /*
   public fedora.server.types.gen.ObjectMethodsDef[] getObjectMethods(String PID,
                                                                      String asOfDateTime)
       throws java.rmi.RemoteException
   {
     Context context=getCachedContext();
     try
     {
       fedora.server.storage.types.ObjectMethodsDef[] objectMethodDefs =
           s_access.getObjectMethods(context,
                                     PID,
                                     DateUtility.convertStringToDate(asOfDateTime));
       fedora.server.types.gen.ObjectMethodsDef[] genObjectMethodDefs =
           TypeUtility.convertObjectMethodsDefArrayToGenObjectMethodsDefArray(
           objectMethodDefs);
       return genObjectMethodDefs;
     } catch (ServerException se)
     {
       logStackTrace(se);
       AxisUtility.throwFault(se);
     } catch (Exception e) {
       logStackTrace(e);
       AxisUtility.throwFault(
           new ServerInitializationException(e.getClass().getName() + ": "
           + e.getMessage()));
     }
     return null;
   }
   */
 
 
   public fedora.server.types.gen.ObjectMethodsDef[] listMethods(String PID,
                                                                      String asOfDateTime)
       throws java.rmi.RemoteException
   {
     Context context=getCachedContext();
     try
     {
       fedora.server.storage.types.ObjectMethodsDef[] objectMethodDefs =
           s_access.listMethods(context,
                                     PID,
                                     DateUtility.convertStringToDate(asOfDateTime));
       fedora.server.types.gen.ObjectMethodsDef[] genObjectMethodDefs =
           TypeUtility.convertObjectMethodsDefArrayToGenObjectMethodsDefArray(
           objectMethodDefs);
       return genObjectMethodDefs;
     } catch (ServerException se)
     {
       logStackTrace(se);
       AxisUtility.throwFault(se);
     } catch (Exception e) {
       logStackTrace(e);
       AxisUtility.throwFault(
           new ServerInitializationException(e.getClass().getName() + ": "
           + e.getMessage()));
     }
     return null;
   }
 
   public fedora.server.types.gen.DatastreamDef[] listDatastreams(String PID, String asOfDateTime)
       throws java.rmi.RemoteException
   {
    Context context=getUncachedContext();
     try
     {
       fedora.server.storage.types.DatastreamDef[] datastreamDefs =
           s_access.listDatastreams(context,
                                     PID,
                                     DateUtility.convertStringToDate(asOfDateTime));
       fedora.server.types.gen.DatastreamDef[] genDatastreamDefs =
           TypeUtility.convertDatastreamDefArrayToGenDatastreamDefArray(
           datastreamDefs);
       return genDatastreamDefs;
     } catch (ServerException se)
     {
       logStackTrace(se);
       AxisUtility.throwFault(se);
     } catch (Exception e) {
       logStackTrace(e);
       AxisUtility.throwFault(
           new ServerInitializationException(e.getClass().getName() + ": "
           + e.getMessage()));
     }
     return null;
   }
 
   /**
    * <p>Gets the object profile which included key metadata about the object
    * and URLs for the Dissemination Index and Item Index of the object.</p>
    *
    * @param PID The persistent identifier for the digital object.
    * @param asOfDateTime The versioning datetime stamp.
    * @return The object profile data structure.
    * @throws java.rmi.RemoteException
    */
   public fedora.server.types.gen.ObjectProfile getObjectProfile(String PID,
                                                                 String asOfDateTime)
       throws java.rmi.RemoteException
   {
     Context context=getCachedContext();
     try
     {
       fedora.server.access.ObjectProfile objectProfile =
           s_access.getObjectProfile(context,
                                     PID,
                                     DateUtility.convertStringToDate(asOfDateTime));
       fedora.server.types.gen.ObjectProfile genObjectProfile =
           TypeUtility.convertObjectProfileToGenObjectProfile(
           objectProfile);
       return genObjectProfile;
     } catch (ServerException se)
     {
       logStackTrace(se);
       AxisUtility.throwFault(se);
     } catch (Exception e) {
       logStackTrace(e);
       AxisUtility.throwFault(
           new ServerInitializationException(e.getClass().getName() + ": "
           + e.getMessage()));
     }
     return null;
   }
 
   /**
    * <p>Gets key information about the repository.</p>
    *
    * @return The repository info data structure.
    * @throws java.rmi.RemoteException
    */
   public fedora.server.types.gen.RepositoryInfo
       describeRepository() throws java.rmi.RemoteException
   {
     Context context=getCachedContext();
     try
     {
       fedora.server.access.RepositoryInfo repositoryInfo =
           s_access.describeRepository(context);
       fedora.server.types.gen.RepositoryInfo genRepositoryInfo =
           TypeUtility.convertReposInfoToGenReposInfo(repositoryInfo);
       return genRepositoryInfo;
     } catch (ServerException se)
     {
       logStackTrace(se);
       AxisUtility.throwFault(se);
     } catch (Exception e) {
       logStackTrace(e);
       AxisUtility.throwFault(
           new ServerInitializationException(e.getClass().getName() + ": "
           + e.getMessage()));
     }
     return null;
   }
 
   private void logStackTrace(Exception e)
   {
     StackTraceElement[] ste = e.getStackTrace();
     StringBuffer lines = new StringBuffer();
     boolean skip = false;
     for (int i = 0; i < ste.length; i++)
     {
       if (ste[i].toString().indexOf("FedoraAPIABindingSOAPHTTPSkeleton") != -1)
       {
         skip=true;
       }
       if (!skip)
       {
         lines.append(ste[i].toString());
         lines.append("\n");
       }
     }
     s_server.logFiner("Error carried up to API-A level: "
                       + e.getClass().getName() + "\n" + lines.toString());
   }
 
   private void assertInitialized() throws java.rmi.RemoteException
   {
     if (!s_initialized)
     {
       AxisUtility.throwFault(s_initException);
     }
   }
 }
