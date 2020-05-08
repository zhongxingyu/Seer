 package fedora.server.access;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import fedora.server.Context;
 import fedora.server.Module;
 import fedora.server.Server;
 import fedora.server.access.dissemination.DisseminationService;
 import fedora.server.errors.DatastreamNotFoundException;
 import fedora.server.errors.DisseminatorNotFoundException;
 import fedora.server.errors.InvalidUserParmException;
 import fedora.server.errors.ModuleInitializationException;
 import fedora.server.errors.GeneralException;
 import fedora.server.errors.ServerException;
 import fedora.server.search.FieldSearchQuery;
 import fedora.server.search.FieldSearchResult;
 import fedora.server.security.IPRestriction;
 import fedora.server.storage.DOReader;
 import fedora.server.storage.BDefReader;
 import fedora.server.storage.BMechReader;
 import fedora.server.storage.DOManager;
 import fedora.server.storage.ExternalContentManager;
 import fedora.server.storage.types.Datastream;
 import fedora.server.storage.types.DatastreamDef;
 import fedora.server.storage.types.DatastreamReferencedContent;
 import fedora.server.storage.types.DatastreamManagedContent;
 import fedora.server.storage.types.DatastreamXMLMetadata;
 import fedora.server.storage.types.DisseminationBindingInfo;
 import fedora.server.storage.types.Disseminator;
 import fedora.server.storage.types.MethodParmDef;
 import fedora.server.storage.types.MIMETypedStream;
 import fedora.server.storage.types.ObjectMethodsDef;
 import fedora.server.storage.types.Property;
 import fedora.server.utilities.DateUtility;
 
 /**
  *
  * <p><b>Title: </b>DefaultAccess.java</p>
  *
  * <p><b>Description: </b>The Access Module, providing support for the Fedora
  * Access subsystem.</p>
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
 public class DefaultAccess extends Module implements Access
 {
 
   /** Current DOManager of the Fedora server. */
   private DOManager m_manager;
 
   /** OAI Provider domain name, for the describe request's identifier info. */
   private String m_repositoryDomainName;
 
   /** IP Restriction for the Access subsystem. */
   private IPRestriction m_ipRestriction;
 
   /** Dynamic Access Module */
   // FIXIT!! is this the right way to associate the dynamic access module???
   private DynamicAccessModule m_dynamicAccess;
 
   private ExternalContentManager m_externalContentManager;
   
   private String fedoraServerHost = null;
 
   private String fedoraServerPort = null;
 
   /**
    * <p>Creates and initializes the Access Module. When the server is starting
    * up, this is invoked as part of the initialization process.</p>
    *
    * @param moduleParameters A pre-loaded Map of name-value pairs comprising
    *        the intended configuration of this Module.
    * @param server The <code>Server</code> instance.
    * @param role The role this module fulfills, a java class name.
    * @throws ModuleInitializationException If initilization values are
    *         invalid or initialization fails for some other reason.
    */
   public DefaultAccess(Map moduleParameters, Server server, String role)
           throws ModuleInitializationException
   {
     super(moduleParameters, server, role);
   }
 
   /**
    * <p>Initializes the module.</p>
    *
    * @throws ModuleInitializationException If the module cannot be initialized.
    */
   public void initModule() throws ModuleInitializationException
   {
 
     String dsMediation = getParameter("doMediateDatastreams");
     if (dsMediation==null)
     {
         throw new ModuleInitializationException(
             "doMediateDatastreams parameter must be specified.", getRole());
     }
     String allowHosts=getParameter("allowHosts");
     String denyHosts=getParameter("denyHosts");
     try {
         m_ipRestriction=new IPRestriction(allowHosts, denyHosts);
     } catch (ServerException se) {
         throw new ModuleInitializationException("Error setting IP restriction "
                 + "for Access subsystem: " + se.getClass().getName() + ": "
                 + se.getMessage(), getRole());
     }
   }
 
   public void postInitModule()
       throws ModuleInitializationException
   {
     // get ref to DOManager
     m_manager=(DOManager) getServer().getModule(
         "fedora.server.storage.DOManager");
     if (m_manager == null)
     {
       throw new ModuleInitializationException("Can't get a DOManager "
           + "from Server.getModule", getRole());
     }
       // get ref to DynamicAccess module
       m_dynamicAccess = (DynamicAccessModule) getServer().
               getModule("fedora.server.access.DynamicAccess");
       
       // get ref to ExternalContentManager
       m_externalContentManager = (ExternalContentManager) getServer().
       getModule("fedora.server.storage.ExternalContentManager");      
       
     // get ref to OAIProvider, for repositoryDomainName param for oai info
     Module oaiProvider=(Module) getServer().getModule("fedora.oai.OAIProvider");
     if (oaiProvider==null) {
       throw new ModuleInitializationException("DefaultAccess module requires that the server "
           + "has an OAIProvider module configured so that it can get the repositoryDomainName parameter.", getRole());
     }
     m_repositoryDomainName=oaiProvider.getParameter("repositoryDomainName");
     if (m_repositoryDomainName==null) {
       throw new ModuleInitializationException("DefaultAccess module requires that the OAIProvider "
           + "module has the repositoryDomainName parameter specified.", getRole());
     }
 
   }
 
   public void checkState(Context context, String objectType, String state, String PID)
       throws ServerException
   {
     PID = Server.getPID(PID).toString();
     // Check Object State
     if ( state.equalsIgnoreCase("D")  &&
          ( context.get("canUseDeletedObject")==null
            || (!context.get("canUseDeletedObject").equals("true")) )
       )
     {
       throw new GeneralException("The requested "+objectType+" object \""+PID+"\" is no "
           + "longer available for dissemination. It has been flagged for DELETION "
           + "by the repository administrator. ");
 
     } else if ( state.equalsIgnoreCase("I")  &&
                 ( context.get("canUseInactiveObject")==null
                   || (!context.get("canUseInactiveObject").equals("true")) )
               )
     {
       throw new GeneralException("The requested "+objectType+" object \""+PID+"\" is no "
           + "longer available for dissemination. It has been flagged as INACTIVE "
           + "by the repository administrator. ");
     }
   }
 
   /**
    * <p>Disseminates the content produced by executing the specified method
    * of the associated Behavior Mechanism object of the specified digital
    * object.</p>
    *
    * @param context The context of this request.
    * @param PID The persistent identifier of the digital object.
    * @param bDefPID The persistent identifier of the Behavior Definition object.
    * @param methodName The name of the method to be executed.
    * @param userParms An array of user-supplied method parameters consisting
    *        of name/value pairs.
    * @param asOfDateTime The versioning datetime stamp.
    * @return A MIME-typed stream containing the result of the dissemination.
    * @throws ServerException If any type of error occurred fulfilling the
    *         request.
    */
   public MIMETypedStream getDissemination(Context context, String PID,
       String bDefPID, String methodName, Property[] userParms,
       Date asOfDateTime) throws ServerException
   {
     PID = Server.getPID(PID).toString();
     m_ipRestriction.enforce(context);
     bDefPID = Server.getPID(bDefPID).toString();
     long initStartTime = new Date().getTime();
     long startTime = new Date().getTime();
     DOReader reader = m_manager.getReader(context, PID);
 
 
     // DYNAMIC!! If the behavior definition (bDefPID) is defined as dynamic, then
     // perform the dissemination via the DynamicAccess module.
     if (m_dynamicAccess.isDynamicBehaviorDefinition(context, PID, bDefPID))
     {
       return
         m_dynamicAccess.getDissemination(context, PID, bDefPID, methodName,
           userParms, asOfDateTime);
     }
     long stopTime = new Date().getTime();
     long interval = stopTime - startTime;
     logFiner("[DefaultAccess] Roundtrip DynamicDisseminator: "
         + interval + " milliseconds.");
 
     // Check data object state
     checkState(context, "Data", reader.GetObjectState(), PID);
 
     // Check associated bdef object state
     BDefReader bDefReader = m_manager.getBDefReader(context, bDefPID);
     checkState(context, "Behavior Definition", bDefReader.GetObjectState(), bDefPID);
 
     // SDP: get a bmech reader to get information that is specific to
     // a mechanism.
     Date versDateTime = asOfDateTime;
     BMechReader bmechreader = null;
     Disseminator[] dissSet = reader.GetDisseminators(versDateTime, null);
     startTime = new Date().getTime();
     for (int i=0; i<dissSet.length; i++)
     {
       if (dissSet[i].bDefID.equalsIgnoreCase(bDefPID))
       {
         checkState(context, "disseminator (\""+dissSet[i].dissID+"\") for the data ", dissSet[i].dissState, PID);
         bmechreader = m_manager.getBMechReader(context, dissSet[i].bMechID);
         break;
       }
     }
 
     // if bmechreader is null, it means that no disseminators matched the specified bDef PID
     // This can occur if a date/time stamp value is specified that is earlier than the creation
     // date of all disseminators or if the specified bDef PID does not match the bDef of
     // any disseminators in the object.
     if(bmechreader == null) {
         String message = "[DefaultAccess] Either there are no disseminators found in "
             + "the object \"" + PID + "\" that match the specified date/time stamp "
             + "of \"" + DateUtility.convertDateToString(asOfDateTime) + "\"  OR "
             + "the specified bDef PID of \"" + bDefPID + "\" does not match the "
             + "bDef PID of any disseminators for this digital object.";
         throw new DisseminatorNotFoundException(message);
     }
     stopTime = new Date().getTime();
     interval = stopTime - startTime;
     logFiner("[DefaultAccess] Roundtrip Looping Diss: "
               + interval + " milliseconds.");
 
     // Check bmech object state
     checkState(context, "behavior mechanism", bmechreader.GetObjectState(), bmechreader.GetObjectPID());
 
     // Get method parms
     Hashtable h_userParms = new Hashtable();
     MIMETypedStream dissemination = null;
     MethodParmDef[] defaultMethodParms = null;
 
     startTime = new Date().getTime();
     // Put any user-supplied method parameters into hash table
     if (userParms != null)
     {
       for (int i=0; i<userParms.length; i++)
       {
         h_userParms.put(userParms[i].name, userParms[i].value);
       }
     }
 
     // Validate user-supplied parameters
     validateUserParms(context, PID, bDefPID, methodName,
                       h_userParms, versDateTime);
 
     stopTime = new Date().getTime();
     interval = stopTime - startTime;
     logFiner("[DefaultAccess] Roundtrip Get/Validate User Parms: "
         + interval + " milliseconds.");
 
     startTime = new Date().getTime();
     // SDP: GET INFO FROM BMECH READER:
     // Add any default method parameters to validated user parm list
     //defaultMethodParms = reader.GetBMechDefaultMethodParms(bDefPID,
     defaultMethodParms = bmechreader.getServiceMethodParms(methodName, versDateTime);
     for (int i=0; i<defaultMethodParms.length; i++)
     {
       if (!defaultMethodParms[i].parmType.equals(MethodParmDef.DATASTREAM_INPUT)) {
           if (!h_userParms.containsKey(defaultMethodParms[i].parmName)) {
             this.getServer().logFinest("addedDefaultName: "+defaultMethodParms[i].parmName);
             String pdv=defaultMethodParms[i].parmDefaultValue;
             if (pdv.equalsIgnoreCase("$pid")) {
                 pdv=PID;
             } else if (pdv.equalsIgnoreCase("$objuri")) {
                 pdv="info:fedora/" + PID;
             }
             this.getServer().logFinest("addedDefaultValue: "+pdv);
             h_userParms.put(defaultMethodParms[i].parmName, pdv);
           }
       }
     }
 
     stopTime = new Date().getTime();
     interval = stopTime - startTime;
     logFiner("[DefaultAccess] Roundtrip Get BMech Parms: "
         + interval + " milliseconds.");
 
     startTime = new Date().getTime();
     // Get dissemination binding info.
     DisseminationBindingInfo[] dissBindInfo =
         reader.getDisseminationBindingInfo(bDefPID, methodName, versDateTime);
 
     // Assemble and execute the dissemination request from the binding info.
     DisseminationService dissService = new DisseminationService();
     dissemination =
         dissService.assembleDissemination(context, PID, h_userParms, dissBindInfo);
 
     stopTime = new Date().getTime();
     interval = stopTime - startTime;
     logFiner("[DefaultAccess] Roundtrip Assemble Dissemination: "
         + interval + " milliseconds.");
 
     stopTime = new Date().getTime();
     interval = stopTime - initStartTime;
     logFiner("[DefaultAccess] Roundtrip GetDissemination: "
               + interval + " milliseconds.");
     return dissemination;
   }
 
   public ObjectMethodsDef[] listMethods(Context context, String PID,
       Date asOfDateTime) throws ServerException
   {
     long startTime = new Date().getTime();
     PID = Server.getPID(PID).toString();
     m_ipRestriction.enforce(context);
     DOReader reader =
         m_manager.getReader(context, PID);
 
     // Check data object state
     checkState(context, "Data", reader.GetObjectState(), PID);
 
     ObjectMethodsDef[] methodDefs =
         reader.listMethods(asOfDateTime);
     long stopTime = new Date().getTime();
     long interval = stopTime - startTime;
     logFiner("[DefaultAccess] Roundtrip listMethods: "
               + interval + " milliseconds.");
 
     // DYNAMIC!! Grab any dynamic method definitions and merge them with
     // the statically bound method definitions
     ObjectMethodsDef[] dynamicMethodDefs =
         //m_dynamicAccess.getObjectMethods(context, PID, asOfDateTime);
         m_dynamicAccess.listMethods(context, PID, asOfDateTime);
     ArrayList methodList = new ArrayList();
     for (int i=0; i < methodDefs.length; i++)
     {
       methodList.add(methodDefs[i]);
     }
     for (int j=0; j < dynamicMethodDefs.length; j++)
     {
       methodList.add(dynamicMethodDefs[j]);
     }
     return (ObjectMethodsDef[])methodList.toArray(new ObjectMethodsDef[0]);
   }
 
   public DatastreamDef[] listDatastreams(Context context, String PID,
       Date asOfDateTime) throws ServerException
   {
     long startTime = new Date().getTime();
     m_ipRestriction.enforce(context);
     PID = Server.getPID(PID).toString();
     DOReader reader =
         m_manager.getReader(context, PID);
 
     // Check data object state
     checkState(context, "Data", reader.GetObjectState(), PID);
 
     Datastream[] datastreams = reader.GetDatastreams(asOfDateTime, null);
     DatastreamDef[] dsDefs = new DatastreamDef[datastreams.length];
     for (int i=0; i<datastreams.length; i++) {
         DatastreamDef dsDef = new DatastreamDef();
         dsDef.dsID = datastreams[i].DatastreamID;
         dsDef.dsLabel = datastreams[i].DSLabel;
         dsDef.dsMIME = datastreams[i].DSMIME;
         dsDefs[i] = dsDef;
     }
     
     long stopTime = new Date().getTime();
     long interval = stopTime - startTime;
     logFiner("[DefaultAccess] Roundtrip listDatastreams: "
             + interval + " milliseconds.");
     return dsDefs;
   }
 
   public ObjectProfile getObjectProfile(Context context, String PID,
     Date asOfDateTime) throws ServerException
   {
     PID = Server.getPID(PID).toString();
     DOReader reader = m_manager.getReader(context, PID);
 
     // Check data object state
     checkState(context, "Data", reader.GetObjectState(), PID);
 
     Date versDateTime = asOfDateTime;
     ObjectProfile profile = new ObjectProfile();
     profile.PID = reader.GetObjectPID();
     profile.objectLabel = reader.GetObjectLabel();
     profile.objectContentModel = reader.getContentModelId();
     profile.objectCreateDate = reader.getCreateDate();
     profile.objectLastModDate = reader.getLastModDate();
     profile.objectType = reader.getFedoraObjectType();
     profile.dissIndexViewURL = getDissIndexViewURL(getReposBaseURL(),
         reader.GetObjectPID(), versDateTime);
     profile.itemIndexViewURL = getItemIndexViewURL(getReposBaseURL(),
         reader.GetObjectPID(), versDateTime);
       return profile;
   }
 
   /**
    * <p>Lists the specified fields of each object matching the given
    * criteria.</p>
    *
    * @param context the context of this request
    * @param resultFields the names of the fields to return
    * @param maxResults the maximum number of results to return at a time
    * @param query the query
    * @return the results of te field search
    * @throws ServerException If any type of error occurred fulfilling the
    *         request.
    */
   public FieldSearchResult findObjects(Context context,
           String[] resultFields, int maxResults, FieldSearchQuery query)
           throws ServerException {
       m_ipRestriction.enforce(context);
       return m_manager.findObjects(context, resultFields, maxResults, query);
   }
 
   /**
    * <p>Resumes an in-progress listing of object fields.</p>
    *
    * @param context the context of this request
    * @param sessionToken the token of the session in which the remaining
    *        results can be obtained
    * @return the next set of results from the initial field search
    * @throws ServerException If any type of error occurred fulfilling the
    *         request.
    */
   public FieldSearchResult resumeFindObjects(Context context,
           String sessionToken) throws ServerException {
       m_ipRestriction.enforce(context);
       return m_manager.resumeFindObjects(context, sessionToken);
   }
 
   /**
    * <p>Gets information that describes the repository.</p>
    *
    * @param context the context of this request
    * @return information that describes the repository.
    * @throws ServerException If any type of error occurred fulfilling the
    *         request.
    */
   public RepositoryInfo describeRepository(Context context) throws ServerException
   {
     RepositoryInfo repositoryInfo = new RepositoryInfo();
     repositoryInfo.repositoryName = getServer().getParameter("repositoryName");
     repositoryInfo.repositoryBaseURL = getReposBaseURL() + "/fedora";
     repositoryInfo.repositoryVersion =
       Server.VERSION_MAJOR + "." + Server.VERSION_MINOR;
     Module domgr = getServer().getModule("fedora.server.storage.DOManager");
     repositoryInfo.repositoryPIDNamespace = domgr.getParameter("pidNamespace");
 	repositoryInfo.defaultExportFormat = domgr.getParameter("defaultExportFormat");
     repositoryInfo.OAINamespace = m_repositoryDomainName;
     repositoryInfo.adminEmailList = getAdminEmails();
     repositoryInfo.samplePID = repositoryInfo.repositoryPIDNamespace + ":100";
     repositoryInfo.sampleOAIIdentifer = "oai:" + repositoryInfo.OAINamespace
       + ":" + repositoryInfo.samplePID;
     repositoryInfo.sampleSearchURL = repositoryInfo.repositoryBaseURL
       + "/search";
     repositoryInfo.sampleAccessURL = repositoryInfo.repositoryBaseURL
       + "/get/" + "demo:5";
     repositoryInfo.sampleOAIURL = repositoryInfo.repositoryBaseURL
       + "/oai?verb=Identify";
     repositoryInfo.retainPIDs = getRetainPIDs();
     return repositoryInfo;
   }
 
   /**
    * <p>Gets the change history of an object by returning a list of timestamps
    * that correspond to modification dates of components. This currently includes
    * changes to datastreams and disseminators.</p>
    *
    * @param context The context of this request.
    * @param PID The persistent identifier of the digitla object.
    * @return An Array containing the list of timestamps indicating when changes
    *         were made to the object.
    * @throws ServerException If any type of error occurred fulfilling the
    *         request.
    */
   public String[] getObjectHistory(Context context, String PID) throws ServerException
   {
     PID = Server.getPID(PID).toString();
     m_ipRestriction.enforce(context);
     DOReader reader = m_manager.getReader(context, PID);
 
     // Check data object state
     checkState(context, "Data", reader.GetObjectState(), PID);
 
     return reader.getObjectHistory(PID);
   }
 
   private String[] getAdminEmails()
   {
     String emailsCSV = convertToCSV(getServer().getParameter("adminEmailList"));
     Vector emails = new Vector();
     StringTokenizer st = new StringTokenizer(emailsCSV, ",");
     while (st.hasMoreElements())
     {
       emails.add(st.nextElement());
     }
     return (String[])emails.toArray(new String[0]);
   }
 
   private String[] getRetainPIDs()
   {
     String retainPIDsCSV = convertToCSV(getServer().getModule("fedora.server.storage.DOManager").getParameter("retainPIDs"));
     Vector retainPIDs = new Vector();
     StringTokenizer st = new StringTokenizer(retainPIDsCSV, ",");
     while (st.hasMoreElements())
     {
       retainPIDs.add(st.nextElement());
     }
     return (String[])retainPIDs.toArray(new String[0]);
   }
   private String convertToCSV(String list)
   {
     // make sure values in the list are comma delimited
     String original = list.trim();
     Pattern spaces = Pattern.compile(" ++");
     Matcher m = spaces.matcher(original);
     String interim = m.replaceAll(",");
     Pattern multcommas = Pattern.compile(",++");
     Matcher m2 = multcommas.matcher(interim);
     String csv = m2.replaceAll(",");
     return csv;
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
    * @param context The context of this request.
    * @param PID The persistent identifier of the digital object.
    * @param bDefPID The persistent identifier of the Behavior Definition object.
    * @param methodName The name of the method.
    * @param h_userParms A hashtable of user-supplied method parameter
    *        name/value pairs.
    * @param versDateTime The version datetime stamp of the digital object.
    * @throws ServerException If any type of error occurred fulfilling the
    *         request.
    *
    */
   private void validateUserParms(Context context, String PID, String bDefPID,
       String methodName, Hashtable h_userParms, Date versDateTime)
       throws ServerException
   {
     PID = Server.getPID(PID).toString();
     bDefPID = Server.getPID(bDefPID).toString();
     m_ipRestriction.enforce(context);
     MethodParmDef[] methodParms = null;
     MethodParmDef methodParm = null;
     StringBuffer sb = new StringBuffer();
     Hashtable h_validParms = new Hashtable();
     boolean isValid = true;
 
     DOReader reader =
       m_manager.getReader(context, PID);
     methodParms = reader.getObjectMethodParms(bDefPID,
         methodName, versDateTime);
 
     // Put valid method parameters and their attributes into hashtable
     if (methodParms != null)
     {
       for (int i=0; i<methodParms.length; i++)
       {
         methodParm = methodParms[i];
         h_validParms.put(methodParm.parmName,methodParm);
         this.getServer().logFinest("methodParms[" + i + "]: "
             + methodParms[i].parmName
             + "\nlabel: " + methodParms[i].parmLabel
             + "\ndefault: " + methodParms[i].parmDefaultValue
             + "\nrequired: " + methodParms[i].parmRequired
             + "\ntype: " + methodParms[i].parmType);
         for (int j=0; j<methodParms[i].parmDomainValues.length; j++)
         {
           this.getServer().logFinest("domainValues: "
               + methodParms[i].parmDomainValues[j]);
         }
       }
     }
 
     if (!h_validParms.isEmpty())
     {
       // Iterate over valid parmameters to check for any missing required parms.
       Enumeration e = h_validParms.keys();
       while (e.hasMoreElements())
       {
         String validName = (String)e.nextElement();
         MethodParmDef mp = (MethodParmDef)h_validParms.get(validName);
         if(mp.parmRequired && h_userParms.get(validName) == null)
         {
           // This is a fatal error. A required method parameter does not
           // appear in the list of user supplied parameters.
           sb.append("The required parameter \""
               + validName + "\" was not found in the "
               + "user-supplied parameter list.");
           throw new InvalidUserParmException("[Invalid User Parameters] "
               + sb.toString());
         }
       }
 
       // Iterate over each user supplied parameter name
       Enumeration parmNames = h_userParms.keys();
       while (parmNames.hasMoreElements())
       {
         String parmName = (String)parmNames.nextElement();
         methodParm = (MethodParmDef)h_validParms.get(parmName);
         if (methodParm != null && methodParm.parmName != null)
         {
           // Method has one or more parameters defined
           // Check for default value if user-supplied value is null or empty
           String value = (String)h_userParms.get(methodParm.parmName);
           if (value == null || value.equalsIgnoreCase(""))
           {
             // Value of user-supplied parameter is  null or empty
             if(methodParm.parmDefaultValue != null)
             {
               // Default value is specified for this parameter.
               // Substitute default value.
               h_userParms.put(methodParm.parmName, methodParm.parmDefaultValue);
             } else
             {
               // This is a non-fatal error. There is no default specified
               // for this parameter and the user has supplied no value for
               // the parameter. The value of the empty string will be used
               // as the value of the parameter.
               this.getServer().logWarning("The method parameter \""
                   + methodParm.parmName
                   + "\" has no default value and no "
                   + "value was specified by the user.  "
                   + "The value of the empty string has "
                   + "been assigned to this parameter.");
             }
           } else
           {
             // Value of user-supplied parameter contains a value.
             // Validate the supplied value against the parmDomainValues list.
             String[] parmDomainValues = methodParm.parmDomainValues;
             if (parmDomainValues.length > 0)
             {
               if (!parmDomainValues[0].equalsIgnoreCase("null"))
               {
                 boolean isValidValue = false;
                 String userValue = (String)h_userParms.get(methodParm.parmName);
                 for (int i=0; i<parmDomainValues.length; i++)
                 {
                   if (userValue.equalsIgnoreCase(parmDomainValues[i]) ||
                       parmDomainValues[i].equalsIgnoreCase("null"))
                   {
                     isValidValue = true;
                   }
                 }
                 if (!isValidValue)
                 {
                   for (int i=0; i<parmDomainValues.length; i++)
                   {
                     if (i == parmDomainValues.length-1)
                     {
                       sb.append(parmDomainValues[i]);
                     } else
                     {
                       sb.append(parmDomainValues[i]+", ");
                     }
                   }
                   sb.append("The method parameter \""
                             + methodParm.parmName
                             + "\" with a value of \""
                             + (String)h_userParms.get(methodParm.parmName)
                             + "\" is not allowed for the method \""
                             + methodName + "\". Allowed values for this "
                             + "method include \"" + sb.toString() + "\".");
                   isValid = false;
                 }
               }
             }
           }
         } else
         {
           // This is a fatal error. A user-supplied parameter name does
           // not match any valid parameter names for this method.
           sb.append("The method parameter \"" + parmName
                     + "\" is not valid for the method \""
                     + methodName + "\".");
           isValid = false;
         }
       }
     } else
     {
       // There are no method parameters define for this method.
       if (!h_userParms.isEmpty())
       {
         // This is an error. There are no method parameters defined for
         // this method and user parameters are specified in the
         // dissemination request.
         Enumeration e = h_userParms.keys();
         while (e.hasMoreElements())
         {
           sb.append("The method parameter \"" + (String)e.nextElement()
                     + "\" is not valid for the method \""
                     + methodName + "\"."
                     + "The method \"" + methodName
                     + "\" defines no method parameters.");
         }
         throw new InvalidUserParmException("[Invalid User Parameters] "
             + sb.toString());
       }
     }
     if (!isValid)
     {
       throw new InvalidUserParmException("[Invalid User Parameter] "
           + sb.toString());
     }
     return;
   }
 
   private String getDissIndexViewURL(String reposBaseURL, String PID, Date versDateTime)
   {
       String dissIndexURL = null;
 
       if (versDateTime == null)
       {
         dissIndexURL = reposBaseURL + "/fedora/get/" + PID +
                       "/fedora-system:3/viewMethodIndex";
       }
       else
       {
           dissIndexURL = reposBaseURL + "/fedora/get/"
             + PID + "/fedora-system:3/viewMethodIndex/"
             + DateUtility.convertDateToString(versDateTime);
       }
       return dissIndexURL;
   }
 
   // FIXIT!! Consider implications of hard-coding the default dissemination
   // aspects of the URL (e.g. fedora-system3 as the PID and viewItemIndex.
   private String getItemIndexViewURL(String reposBaseURL, String PID, Date versDateTime)
   {
       String itemIndexURL = null;
 
       if (versDateTime == null)
       {
         itemIndexURL = reposBaseURL + "/fedora/get/" + PID +
                        "/fedora-system:3/viewItemIndex";
       }
       else
       {
           itemIndexURL = reposBaseURL + "/fedora/get/"
             + PID + "/fedora-system:3/viewItemIndex/"
             + DateUtility.convertDateToString(versDateTime);
       }
       return itemIndexURL;
   }
 
   private String getReposBaseURL()
   {
     String reposBaseURL = null;
     InetAddress hostIP = null;
     try
     {
       hostIP = InetAddress.getLocalHost();
     } catch (UnknownHostException uhe)
     {
       System.err.println("[DefaultAccess] was unable to "
           + "resolve the IP address of the Fedora Server: "
           + " The underlying error was a "
           + uhe.getClass().getName() + "The message "
           + "was \"" + uhe.getMessage() + "\"");
     }
     String fedoraServerPort = getServer().getParameter("fedoraServerPort");
     String fedoraServerHost = getServer().getParameter("fedoraServerHost");
     if (fedoraServerHost==null || fedoraServerHost.equals("")) {
         fedoraServerHost=hostIP.getHostName();
     }
     reposBaseURL = "http://" + fedoraServerHost + ":" + fedoraServerPort;
     return reposBaseURL;
   }
 
   public MIMETypedStream getDatastreamDissemination(Context context, String PID,
           String dsID, Date asOfDateTime) throws ServerException {
       PID = Server.getPID(PID).toString();
       m_ipRestriction.enforce(context);
       MIMETypedStream mimeTypedStream = null;
       long startTime = new Date().getTime();
       DOReader reader = m_manager.getReader(context, PID);
 
       // Check data object state
       checkState(context, "Data", reader.GetObjectState(), PID);
       Datastream ds = (Datastream) reader.GetDatastream(dsID, asOfDateTime);
       if (ds == null) {
           String message = "[DefaulAccess] No datastream could be returned. "
               + "Either there is no datastream for the digital "
               + "object \"" + PID + "\" with datastream ID of \"" + dsID
               + " \"  OR  there are no datastreams that match the specified "
               + "date/time value of \"" + DateUtility.convertDateToString(asOfDateTime)
               + " \"  .";
           throw new DatastreamNotFoundException(message);
       }
       
       if (ds.DSControlGrp.equalsIgnoreCase("E")) {
           DatastreamReferencedContent drc = (DatastreamReferencedContent) reader.GetDatastream(dsID, asOfDateTime);
           mimeTypedStream = m_externalContentManager.getExternalContent(drc.DSLocation);
       } else if(ds.DSControlGrp.equalsIgnoreCase("M")) {
           DatastreamManagedContent dmc = (DatastreamManagedContent) reader.GetDatastream(dsID, asOfDateTime);
           mimeTypedStream = new MIMETypedStream(ds.DSMIME, dmc.getContentStream(), null);
       } else if(ds.DSControlGrp.equalsIgnoreCase("X")) {
           DatastreamXMLMetadata dxm =  (DatastreamXMLMetadata) reader.GetDatastream(dsID, asOfDateTime);
           mimeTypedStream = new MIMETypedStream(ds.DSMIME, dxm.getContentStream(), null);
       } else if(ds.DSControlGrp.equalsIgnoreCase("R")){
           DatastreamReferencedContent drc = (DatastreamReferencedContent) reader.GetDatastream(dsID, asOfDateTime);
           // The dsControlGroupType of Redirect("R") is a special control type
           // used primarily for streaming media. Datastreams of this type are
           // not mediated (proxied by Fedora) and their physical dsLocation is
           // simply redirected back to the client. Therefore, the contents
           // of the MIMETypedStream returned for dissemination requests will
           // contain the raw URL of the dsLocation and will be assigned a
           // special fedora-specific MIME type to identify the stream as
           // a MIMETypedStream whose contents contain a URL to which the client
           // should be redirected.
           try
           {
             InputStream inStream = new ByteArrayInputStream(drc.DSLocation.getBytes("UTF-8"));
             mimeTypedStream = new MIMETypedStream("application/fedora-redirect", inStream, null);
           } catch (UnsupportedEncodingException uee)
           {
             String message = "[DefaultAccess] An error has occurred. "
                 + "The error was a \"" + uee.getClass().getName() + "\"  . The "
                 + "Reason was \"" + uee.getMessage() + "\"  . String value: "
                 + drc.DSLocation + "  . ";
             logFinest(message);
             throw new GeneralException(message);
           }          
       }
       long stopTime = new Date().getTime();
       long interval = stopTime - startTime;
       logFiner("[DefaultAccess] Roundtrip getDatastreamDissemination: "
               + interval + " milliseconds.");      
       return mimeTypedStream;
   }
 }
