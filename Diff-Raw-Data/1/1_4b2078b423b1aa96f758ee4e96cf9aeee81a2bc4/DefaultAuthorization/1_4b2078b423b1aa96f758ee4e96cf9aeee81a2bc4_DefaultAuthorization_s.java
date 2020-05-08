 package fedora.server.security;
 
 
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import fedora.common.Constants;
 import fedora.server.Context;
 import fedora.server.Module;
 import fedora.server.MultiValueMap;
 import fedora.server.Server;
 import fedora.server.errors.ModuleInitializationException;
 import fedora.server.errors.authorization.AuthzException;
 import fedora.server.errors.authorization.AuthzOperationalException;
 import fedora.server.storage.DOManager;
 import fedora.server.utilities.DateUtility;
 
 /**
  *
  * <p><b>Title: </b>DefaultAccess.java</p>
  *
  * <p><b>Description: </b>The Access Module, providing support for the Fedora
  * Access subsystem.</p>
  *
  * @author rlw@virginia.edu
  * @version $Id$
  */
 public class DefaultAuthorization extends Module implements Authorization {
 
   private PolicyEnforcementPoint xacmlPep; // = XACMLPep.getInstance();
   
 	boolean enforceListObjectInFieldSearchResults = true;
 	boolean enforceListObjectInResourceIndexResults = true;
 
 	private String repositoryPoliciesActiveDirectory = "";
 	private String objectPoliciesActiveDirectory = "";	
 	private String repositoryPolicyGuitoolDirectory = "";	
 	private String surrogatePoliciesDirectory = "";
 
 	private String combiningAlgorithm = ""; //"com.sun.xacml.combine.OrderedDenyOverridesPolicyAlg";
 	private String enforceMode = "";
 
 	private final String SURROGATE_POLICIES_DIRECTORY_KEY = "SURROGATE-POLICIES-DIRECTORY";
 	private final String REPOSITORY_POLICIES_DIRECTORY_KEY = "REPOSITORY-POLICIES-DIRECTORY";
 	private final String REPOSITORY_POLICY_GUITOOL_DIRECTORY_KEY = "REPOSITORY-POLICY-GUITOOL-POLICIES-DIRECTORY";	
 	private final String OBJECT_POLICIES_DIRECTORY_KEY = "OBJECT-POLICIES-DIRECTORY";
 	private final String COMBINING_ALGORITHM_KEY = "XACML-COMBINING-ALGORITHM";
 	private final String ENFORCE_MODE_KEY = "ENFORCE-MODE";
 	private final String POLICY_SCHEMA_PATH_KEY = "POLICY-SCHEMA-PATH";
 	private final String VALIDATE_REPOSITORY_POLICIES_KEY = "VALIDATE-REPOSITORY-POLICIES";
 	private final String VALIDATE_OBJECT_POLICIES_FROM_FILE_KEY = "VALIDATE-OBJECT-POLICIES-FROM-FILE";
 	private final String VALIDATE_OBJECT_POLICIES_FROM_DATASTREAM_KEY = "VALIDATE-OBJECT-POLICIES-FROM-DATASTREAM";
 	private final String VALIDATE_SURROGATE_POLICIES_KEY = "VALIDATE-SURROGATE-POLICIES";
 	private final String ALLOW_SURROGATE_POLICIES_KEY = "ALLOW-SURROGATE-POLICIES";
 	
 	private static final String XACML_DIST_BASE = "config/xacml-policies";	
 	private static final String DEFAULT_REPOSITORY_POLICIES_DIRECTORY = XACML_DIST_BASE + "/default/default-repository-policies-approximating-2.0"; 
 	private static final String DEFAULT_OBJECT_POLICIES_DIRECTORY = XACML_DIST_BASE + "/default/default-object-policies"; 
 	private static final String BE_SECURITY_PROPERTIES_LOCATION = "config/beSecurity.properties"; 
 	private static final String BACKEND_POLICIES_ACTIVE_DIRECTORY = XACML_DIST_BASE + "/active/generated/backend-service-policies"; 
 	private static final String BACKEND_POLICIES_XSL_LOCATION = XACML_DIST_BASE + "/active/generated/build-backend-policy.xsl";
  
 
 	
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
   public DefaultAuthorization(Map moduleParameters, Server server, String role)
           throws ModuleInitializationException
   {
     super(moduleParameters, server, role);
     System.err.println("log4j.configuration=" + System.getProperty("log4j.configuration"));
 	String serverHome = null;
     try {
 		serverHome = server.getHomeDir().getCanonicalPath() + File.separator;
 	} catch (IOException e1) {
 		throw new ModuleInitializationException("couldn't get server home", role, e1);
 	}
 
     if (moduleParameters.containsKey(SURROGATE_POLICIES_DIRECTORY_KEY)) {
     	surrogatePoliciesDirectory = 
     		//((String) moduleParameters.get(SURROGATE_POLICIES_DIRECTORY_KEY)).startsWith(File.separator) ? "" : serverHome + 
 			(String) moduleParameters.get(SURROGATE_POLICIES_DIRECTORY_KEY);
     	log("surrogatePoliciesDirectory=" + surrogatePoliciesDirectory);
     }
     if (moduleParameters.containsKey(REPOSITORY_POLICIES_DIRECTORY_KEY)) {
     	repositoryPoliciesActiveDirectory = 
     		//((String) moduleParameters.get(REPOSITORY_POLICIES_DIRECTORY_KEY)).startsWith(File.separator) ? "" : serverHome + 
 			(String) moduleParameters.get(REPOSITORY_POLICIES_DIRECTORY_KEY);
     	log("repositoryPoliciesDirectory=" + repositoryPoliciesActiveDirectory);
     }
     if (moduleParameters.containsKey(OBJECT_POLICIES_DIRECTORY_KEY)) {
     	objectPoliciesActiveDirectory =
     		//((String) moduleParameters.get(OBJECT_POLICIES_DIRECTORY_KEY)).startsWith(File.separator) ? "" : serverHome + 
 			(String) moduleParameters.get(OBJECT_POLICIES_DIRECTORY_KEY);
     	log("objectPoliciesDirectory=" + objectPoliciesActiveDirectory);
     }
     if (moduleParameters.containsKey(REPOSITORY_POLICY_GUITOOL_DIRECTORY_KEY)) {
     	repositoryPolicyGuitoolDirectory = 
     		//((String) moduleParameters.get(REPOSITORY_POLICY_GUITOOL_DIRECTORY_KEY)).startsWith(File.separator) ? "" : serverHome + 
 			(String) moduleParameters.get(REPOSITORY_POLICY_GUITOOL_DIRECTORY_KEY);
     	log("repositoryPolicyGuitoolDirectory=" + repositoryPolicyGuitoolDirectory);
     }        
     if (moduleParameters.containsKey(COMBINING_ALGORITHM_KEY)) {
     	combiningAlgorithm = (String) moduleParameters.get(COMBINING_ALGORITHM_KEY);
     }
     if (moduleParameters.containsKey(ENFORCE_MODE_KEY)) {
     	enforceMode = (String) moduleParameters.get(ENFORCE_MODE_KEY);
     }
     log("looking for POLICY_SCHEMA_PATH");
     if (moduleParameters.containsKey(POLICY_SCHEMA_PATH_KEY)) {
         log("found POLICY_SCHEMA_PATH");
     	policySchemaPath = 
     		(((String) moduleParameters.get(POLICY_SCHEMA_PATH_KEY)).startsWith(File.separator) ? "" : serverHome) 
 			+ (String) moduleParameters.get(POLICY_SCHEMA_PATH_KEY);
         log("set it = " + policySchemaPath);
     }
     log("looking for VALIDATE_REPOSITORY_POLICIES");
     if (moduleParameters.containsKey(VALIDATE_REPOSITORY_POLICIES_KEY)) {
         log("found VALIDATE_REPOSITORY_POLICIES");    
         String temp = (String) moduleParameters.get(VALIDATE_REPOSITORY_POLICIES_KEY);
         log("string vers = " + temp);
     	validateRepositoryPolicies = (new Boolean( (String) moduleParameters.get(VALIDATE_REPOSITORY_POLICIES_KEY))).booleanValue();
         log("set it = " + validateRepositoryPolicies);
     }
     if (moduleParameters.containsKey(VALIDATE_OBJECT_POLICIES_FROM_FILE_KEY)) {
     	validateObjectPoliciesFromFile = Boolean.getBoolean((String) moduleParameters.get(VALIDATE_OBJECT_POLICIES_FROM_FILE_KEY));
     }
     if (moduleParameters.containsKey(VALIDATE_OBJECT_POLICIES_FROM_DATASTREAM_KEY)) {
     	validateObjectPoliciesFromDatastream = Boolean.getBoolean((String) moduleParameters.get(VALIDATE_OBJECT_POLICIES_FROM_DATASTREAM_KEY));
     }
     if (moduleParameters.containsKey(VALIDATE_SURROGATE_POLICIES_KEY)) {
     	validateSurrogatePolicies = Boolean.getBoolean((String) moduleParameters.get(VALIDATE_SURROGATE_POLICIES_KEY));
     }
     if (moduleParameters.containsKey(ALLOW_SURROGATE_POLICIES_KEY)) {
     	allowSurrogatePolicies = Boolean.getBoolean((String) moduleParameters.get(ALLOW_SURROGATE_POLICIES_KEY));
     }
 	log("DefaultAuthorization constructor end");
   }
 
   
   /**
    * <p>Initializes the module.</p>
    *
    * @throws ModuleInitializationException If the module cannot be initialized.
    */
   public void initModule() throws ModuleInitializationException {
 	log("DefaultAuthorization.initModule()");
   }
   
 	private String policySchemaPath = "";
   	private boolean validateRepositoryPolicies = false;
   	private boolean validateObjectPoliciesFromFile = false;
   	private boolean validateObjectPoliciesFromDatastream = false;
 	private boolean validateSurrogatePolicies = false;
 	private boolean allowSurrogatePolicies = false;
 	
 	private static boolean mkdir(String dirPath) {
 		boolean createdOnThisCall = false;
 		File directory = new File(dirPath);
 		if (! directory.exists()) {
 			directory.mkdirs();
 			createdOnThisCall = true;
 		}		
 		return createdOnThisCall;
 	}
 	
 	private static final int BUFFERSIZE = 4096;
 	
 	private static void filecopy(String srcPath, String destPath) throws Exception {
 		File srcFile = new File(srcPath);
 		FileInputStream fis = new FileInputStream(srcFile);
 		File destFile = new File(destPath);
 		slog("before creating new file " + destFile.getAbsolutePath());
 		try {
 			destFile.createNewFile();
 		} catch (Exception e) {
 		}
 		slog("after creating new file " + destFile.getAbsolutePath());
 		FileOutputStream fos = new FileOutputStream(destFile);
 		slog("after creating new fos " + fos);			
 		byte[] buffer = new byte[BUFFERSIZE]; 
 		boolean reading = true;
 		while (reading) {
 			slog("loop 1 ");
 			int bytesRead = fis.read(buffer);
 			slog("loop 2 " + bytesRead);
 			if (bytesRead > 0) {
 				slog("loop 2a " + bytesRead);									
 				fos.write(buffer, 0, bytesRead);
 				slog("loop 2b " + bytesRead);				
 			}
 			reading = (bytesRead > -1);
 			slog("loop 3 " + bytesRead);				
 		}
 		slog("after loop 1 ");
 		fis.close();
 		slog("after loop 2");
 		fos.close();
 		slog("after loop 3");
 		
 	}
 	
 	private static void dircopy(String srcPath, String destPath) throws Exception {
 		slog("copying from " + srcPath + " to " + destPath);
 		File srcDir = new File(srcPath);
 		slog("srcDir = " + srcDir);
 		slog("exists?=" + srcDir.exists());
 		slog("canRead?=" + srcDir.canRead());
 		String[] paths = srcDir.list();
 		slog("paths = " + paths);		
 		slog("copying " + paths.length + " files");
 		try {
 		for (int i=0; i<paths.length; i++) {
 			slog("up = " + paths[i]);			
 			String absSrcPath = srcPath + File.separator + paths[i];
 			String absDestPath = destPath + File.separator + paths[i];
 			filecopy(absSrcPath, absDestPath);
 		}
 		} catch (IOException e) {
 			slog("caught IOException: " + e.getMessage());
 			throw e;
 		} catch (Exception x) {
 			slog("caught Exception: " + x.getClass().getName() + " " + x.getMessage());
 			throw x;			
 		}
 	}
 	
 	private static void deldirfiles(String path) throws Exception {
 		slog("deleting from " + path);
 		File srcDir = new File(path);
 		slog("srcDir = " + srcDir);
 		slog("exists?=" + srcDir.exists());
 		slog("canRead?=" + srcDir.canRead());
 		String[] paths = srcDir.list();
 		slog("paths = " + paths);		
 		slog("copying " + paths.length + " files");
 		try {
 		for (int i=0; i<paths.length; i++) {
 			slog("up = " + paths[i]);			
 			String absPath = path + File.separator + paths[i];
 			File f = new File(absPath);
 			f.delete();
 		}
 		} catch (Exception x) {
 			slog("caught Exception: " + x.getClass().getName() + " " + x.getMessage());
 			throw x;			
 		}
 	}
 
 	private final void generateBackendPolicies() throws Exception{
 		String fedoraHome = ((Module)this).getServer().getHomeDir().getAbsolutePath();
 		log("fedorahome=" + fedoraHome);			
 		deldirfiles(fedoraHome + File.separator + BACKEND_POLICIES_ACTIVE_DIRECTORY);
 		BackendPolicies backendPolicies = new BackendPolicies(fedoraHome + File.separator + BE_SECURITY_PROPERTIES_LOCATION);
 		log("fedoraHome + File.separator + BE_SECURITY_PROPERTIES_LOCATION=" + fedoraHome + File.separator + BE_SECURITY_PROPERTIES_LOCATION);	
 		Hashtable tempfiles = backendPolicies.generateBackendPolicies();
 		log("tempfiles=" + tempfiles);					
 		log("tempfiles.length=" + tempfiles.size());							
 		TransformerFactory tfactory = TransformerFactory.newInstance();
 		Iterator iterator = tempfiles.keySet().iterator();
 		while (iterator.hasNext()) {
 			log("fedoraHome + File.separator + BACKEND_POLICIES_XSL_LOCATION=" + fedoraHome + File.separator + BACKEND_POLICIES_XSL_LOCATION);
 			File f = new File(fedoraHome + File.separator + BACKEND_POLICIES_XSL_LOCATION); //<<stylesheet location
 			StreamSource ss = new StreamSource(f);
 		    Transformer transformer = tfactory.newTransformer(ss); //xformPath
 		    String key = (String) iterator.next();
 			log("key=" + key);
 		    File infile = new File((String)tempfiles.get(key));
 			FileInputStream fis = new FileInputStream(infile);
 			log("fedoraHome + File.separator + BACKEND_POLICIES_ACTIVE_DIRECTORY + File.separator + infile.getName()=" + fedoraHome + File.separator + BACKEND_POLICIES_ACTIVE_DIRECTORY + File.separator + infile.getName());			
 			FileOutputStream fos = new FileOutputStream(fedoraHome + File.separator + BACKEND_POLICIES_ACTIVE_DIRECTORY + File.separator + key);
 			transformer.transform(new StreamSource(fis), new StreamResult(fos));
 		}
 	}
 	
 	private void setupActivePolicyDirectories() throws Exception {
 		String fedoraHome = ((Module)this).getServer().getHomeDir().getAbsolutePath();
 		log("fedorahome=" + fedoraHome);				
 		mkdir(surrogatePoliciesDirectory);
 		mkdir(repositoryPolicyGuitoolDirectory);
 		filecopy(fedoraHome + File.separator + XACML_DIST_BASE + File.separator + "readme-policyguitool-generated-policies.txt", 
 				repositoryPolicyGuitoolDirectory + File.separator + "readme-policyguitool-generated-policies.txt");
 		if (mkdir(repositoryPoliciesActiveDirectory)) {
 			dircopy(fedoraHome + File.separator + DEFAULT_REPOSITORY_POLICIES_DIRECTORY, repositoryPoliciesActiveDirectory);
 		}
 		if (mkdir(objectPoliciesActiveDirectory)) {
 			dircopy(fedoraHome + File.separator + DEFAULT_OBJECT_POLICIES_DIRECTORY, objectPoliciesActiveDirectory);
 		}		
 		generateBackendPolicies();
 	}
 	
   public void postInitModule() throws ModuleInitializationException {
   	log("in DefaultAuthorization.postInitModule() 1");
     DOManager m_manager = (DOManager) getServer().getModule("fedora.server.storage.DOManager");
   	log("in DefaultAuthorization.postInitModule() 2");
     if (m_manager == null) {
     	log("in DefaultAuthorization.postInitModule() 3");
       throw new ModuleInitializationException("Can't get a DOManager from Server.getModule", getRole());
     }
   	log("in DefaultAuthorization.postInitModule() 4");
     try {
       	log("in DefaultAuthorization.postInitModule() 5");
       	setupActivePolicyDirectories();
         xacmlPep = PolicyEnforcementPoint.getInstance();
       	log("in DefaultAuthorization.postInitModule() 6, policySchemaPath=" + policySchemaPath +
       			" validateRepositoryPolicies=" + validateRepositoryPolicies);
 		String fedoraHome = ((Module)this).getServer().getHomeDir().getAbsolutePath();      	
         xacmlPep.initPep(enforceMode, combiningAlgorithm, repositoryPoliciesActiveDirectory, fedoraHome + File.separator + BACKEND_POLICIES_ACTIVE_DIRECTORY, repositoryPolicyGuitoolDirectory, objectPoliciesActiveDirectory, m_manager, 
        		validateRepositoryPolicies, validateObjectPoliciesFromFile, validateObjectPoliciesFromDatastream, policySchemaPath);
       	log("in DefaultAuthorization.postInitModule() 7");
         Transom.getInstance().setAllowSurrogate(allowSurrogatePolicies);
         Transom.getInstance().setSurrogatePolicyDirectory(surrogatePoliciesDirectory);
         Transom.getInstance().setValidateSurrogatePolicies(validateSurrogatePolicies);
         Transom.getInstance().setPolicySchemaPath(policySchemaPath);      	
     } catch (Throwable e1) {
       	log("in DefaultAuthorization.postInitModule() 8");
     	ModuleInitializationException e2 = new ModuleInitializationException(e1.getMessage(), getRole());
     	throw e2;
     }
   }
   
   public void reloadPolicies(Context context) throws Exception {
   	enforceReloadPolicies(context);
   	xacmlPep.newPdp();
   }
   
 	private final String extractNamespace(String pid) {
 		String namespace = "";
 		int colonPosition = pid.indexOf(':');
 		if (-1 < colonPosition) {
 			namespace = pid.substring( 0, colonPosition);
 		}
 		return namespace;
 	}
 	
 	public final void enforceAddDatastream(Context context, String pid, String dsId, 
 			String[] altIDs, //how to handle altIDs?
 			String MIMEType, String formatURI, String dsLocation, String controlGroup, String dsState)
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered DefaultAuthorization.enforceAddDatastream");
 		String target = Constants.ACTION.ADD_DATASTREAM.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.DATASTREAM.MIME_TYPE.uri, MIMEType);
 			name = resourceAttributes.setReturn(Constants.DATASTREAM.FORMAT_URI.uri, formatURI);			
 			name = resourceAttributes.setReturn(Constants.DATASTREAM.STATE.uri, dsState);
 			name = resourceAttributes.setReturn(Constants.DATASTREAM.ID.uri, dsId);
 			name = resourceAttributes.setReturn(Constants.DATASTREAM.LOCATION.uri, dsLocation);
 			name = resourceAttributes.setReturn(Constants.DATASTREAM.CONTROL_GROUP.uri, controlGroup);
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes); 
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	} finally {
         getServer().logFinest("Exiting enforceAddDatastream");
 	}
 	}
 	
 	public final void enforceAddDisseminator(Context context, String pid, String bDefPid, String bMechPid, String dissState)
 	throws AuthzException { 
 	try {
         getServer().logFinest("Entered DefaultAuthorization.enforceAddDisseminator");		
 		String target = Constants.ACTION.ADD_DISSEMINATOR.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.BDEF.PID.uri, bDefPid);
 			name = resourceAttributes.setReturn(Constants.BMECH.PID.uri, bMechPid);			
 			name = resourceAttributes.setReturn(Constants.DISSEMINATOR.STATE.uri, dissState);
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	} finally {
         getServer().logFinest("Exiting enforceAddDisseminator");
 	}
 	}
 		
 	public final void enforceExportObject(Context context, String pid, String format, String exportContext, String exportEncoding)
 	throws AuthzException { 
 	try {
         getServer().logFinest("Entered enforceExportObject");		
 		String target = Constants.ACTION.EXPORT_OBJECT.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);		
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try { 
 			name = resourceAttributes.setReturn(Constants.OBJECT.FORMAT_URI.uri, format);
 			name = resourceAttributes.setReturn(Constants.OBJECT.CONTEXT.uri, exportContext);			
 			name = resourceAttributes.setReturn(Constants.OBJECT.ENCODING.uri, exportEncoding);
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	} finally {
         getServer().logFinest("Exiting enforceExportObject");
 	}
 	}
 	
 	
 	public final void enforceGetDisseminatorHistory(Context context, String pid, String disseminatorId) 
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforceGetDisseminatorHistory");		
 		String target = Constants.ACTION.GET_DISSEMINATOR_HISTORY.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.DISSEMINATOR.ID.uri, disseminatorId);	
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	} finally {
         getServer().logFinest("Exiting enforceGetDisseminatorHistory");
 	}
 	}
 
 	public final void enforceGetNextPid(Context context, String namespace, int nNewPids) 
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforceGetNextPid");		
 		String target = Constants.ACTION.GET_NEXT_PID.uri;	
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			String nNewPidsAsString = Integer.toString(nNewPids);
 			name = resourceAttributes.setReturn(Constants.OBJECT.N_PIDS.uri, nNewPidsAsString);
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, "", namespace, context);
 	} finally {
         getServer().logFinest("Exiting enforceGetNextPid");
 	}
 	}
 
 	public final void enforceGetDatastream(Context context, String pid, String datastreamId, Date asOfDateTime) 
 	throws AuthzException { 
 	try {
         getServer().logFinest("Entered enforceGetDatastream");		
 		String target = Constants.ACTION.GET_DATASTREAM.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.DATASTREAM.ID.uri, datastreamId);
 			name = resourceAttributes.setReturn(Constants.DATASTREAM.AS_OF_DATETIME.uri, ensureDate (asOfDateTime, context));			
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	} finally {
         getServer().logFinest("Exiting enforceGetDatastream");
 	}
 	}
 	
 	public final void enforceGetDatastreamHistory(Context context, String pid, String datastreamId) 
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforceGetDatastreamHistory");		
 		String target = Constants.ACTION.GET_DATASTREAM_HISTORY.uri;		
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.DATASTREAM.ID.uri, datastreamId);
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	} finally {
         getServer().logFinest("Exiting enforceGetDatastreamHistory");
 	}
 	}
 	
 	private final String ensureDate (Date date, Context context) throws AuthzOperationalException {
 		if (date == null) {
 			date = context.now();
 		}
 		String dateAsString;
 		try {
 			dateAsString = dateAsString(date);
 		} catch (Throwable t) {
 			throw new AuthzOperationalException("couldn't make date a string", t);
 		}
 		return dateAsString;
 	}
 	
 	public final void enforceGetDatastreams(Context context, String pid, Date asOfDate, String datastreamState) 
 	throws AuthzException { 
 	try {
         getServer().logFinest("Entered enforceGetDatastreams");		
 		String target = Constants.ACTION.GET_DATASTREAMS.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
         log("in enforceGetDatastreams");
 		String name = ""; 
 		try {
 	        log("in enforceGetDatastreams, before setting datastreamState=" + datastreamState);
 	        name = resourceAttributes.setReturn(Constants.DATASTREAM.STATE.uri, datastreamState);	
 	        log("in enforceGetDatastreams, before setting asOfDateAsString");
 	        name = resourceAttributes.setReturn(Constants.RESOURCE.AS_OF_DATETIME.uri, ensureDate(asOfDate, context));
 		    log("in enforceGetDatastreams, after setting asOfDateAsString");
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
         log("in enforceGetDatastreams, before setting resourceAttributes");
 		context.setResourceAttributes(resourceAttributes);
         log("in enforceGetDatastreams, after setting resourceAttributes");
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
         log("in enforceGetDatastreams, after calling global enforce");
 	} finally {
         getServer().logFinest("Exiting enforceGetDatastreams");
 	}
 	}
 
 	public final void enforceGetDisseminator(Context context, String pid, String disseminatorId, Date asOfDate) 
 	throws AuthzException { 
 	try {
         getServer().logFinest("Entered enforceGetDisseminator");		
 		String target = Constants.ACTION.GET_DISSEMINATOR.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.DISSEMINATOR.ID.uri, disseminatorId);	
 			name = resourceAttributes.setReturn(Constants.RESOURCE.AS_OF_DATETIME.uri, ensureDate(asOfDate, context));	
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);		
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	} finally {
         getServer().logFinest("Exiting enforceGetDisseminator");
 	}
 	}
 	
 	public final void enforceGetDisseminators(Context context, String pid, Date asOfDate, String disseminatorState) 
 	throws AuthzException { 
 	try {
         getServer().logFinest("Entered enforceGetDisseminators");		
 		String target = Constants.ACTION.GET_DISSEMINATORS.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.DISSEMINATOR.STATE.uri, disseminatorState);	
 			name = resourceAttributes.setReturn(Constants.RESOURCE.AS_OF_DATETIME.uri, ensureDate(asOfDate, context));	
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);	
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	} finally {
         getServer().logFinest("Exiting enforceGetDisseminators");
 	}
 	}
 	
 	public final void enforceGetObjectProperties(Context context, String pid) 
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforceGetObjectProperties");		
 		String target = Constants.ACTION.GET_OBJECT_PROPERTIES.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		context.setResourceAttributes(null);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	} finally {
         getServer().logFinest("Exiting enforceGetObjectProperties");
 	}
 	}
 	
 	public final void enforceGetObjectXML(Context context, String pid, String objectXmlEncoding) 
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforceGetObjectXML");		
 		String target = Constants.ACTION.GET_OBJECT_XML.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.OBJECT.ENCODING.uri, objectXmlEncoding);	
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);		
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	} finally {
         getServer().logFinest("Exiting enforceGetObjectXML");
 	}
 	}
 	
 	public final void enforceIngestObject(Context context, String pid, String format, String ingestEncoding)
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforceIngestObject");		
 		String target = Constants.ACTION.INGEST_OBJECT.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.OBJECT.FORMAT_URI.uri, format);	
 			name = resourceAttributes.setReturn(Constants.OBJECT.ENCODING.uri, ingestEncoding);			
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	} finally {
         getServer().logFinest("Exiting enforceIngestObject");
 	}
 	}
 	
 	public final void enforceListObjectInFieldSearchResults(Context context, String pid) 
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforceListObjectInFieldSearchResults");		
 		String target = Constants.ACTION.LIST_OBJECT_IN_FIELD_SEARCH_RESULTS.uri;
 		log("enforcing " + target);
 		if (enforceListObjectInFieldSearchResults) {
 			context.setActionAttributes(null);
 			context.setResourceAttributes(null);
 			xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIA.uri, pid, extractNamespace(pid), context);
 		}
 	} finally {
         getServer().logFinest("Exiting enforceListObjectInFieldSearchResults");
 	}
 	}
 	
 	public final void enforceListObjectInResourceIndexResults(Context context, String pid) 
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforceListObjectInResourceIndexResults");		
 		String target = Constants.ACTION.LIST_OBJECT_IN_RESOURCE_INDEX_RESULTS.uri;
 		log("enforcing " + target);
 		if (enforceListObjectInResourceIndexResults) {
 			context.setActionAttributes(null);
 			context.setResourceAttributes(null);
 			xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIA.uri, pid, extractNamespace(pid), context);
 		}
 	} finally {
         getServer().logFinest("Exiting enforceListObjectInResourceIndexResults");
 	}
 	}
 
 	public final void enforceModifyDatastreamByReference(Context context, String pid, String datastreamId, 
 			String[] altIDs, // how to handle? 
 			String datastreamNewMimeType, String datastreamNewFormatURI, String datastreamNewLocation, String datastreamNewState) //x
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforceModifyDatastreamByReference");		
 		String target = Constants.ACTION.MODIFY_DATASTREAM_BY_REFERENCE.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);		
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.DATASTREAM.ID.uri, datastreamId);
 			name = resourceAttributes.setReturn(Constants.DATASTREAM.NEW_MIME_TYPE.uri, datastreamNewMimeType);
 			name = resourceAttributes.setReturn(Constants.DATASTREAM.NEW_FORMAT_URI.uri, datastreamNewFormatURI);			
 			name = resourceAttributes.setReturn(Constants.DATASTREAM.NEW_LOCATION.uri, datastreamNewLocation);			
 			name = resourceAttributes.setReturn(Constants.DATASTREAM.NEW_STATE.uri, datastreamNewState);	
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);	
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	} finally {
         getServer().logFinest("Exiting enforceModifyDatastreamByReference");
 	}
 	}
 	
 	public final void enforceModifyDatastreamByValue(Context context, String pid, String datastreamId, 
 			String[] altIDs, // how to handle?
 			String newDatastreamMimeType, String newDatastreamFormatURI, String newDatastreamState)
 	throws AuthzException { 
 	try {
         getServer().logFinest("Entered enforceModifyDatastreamByValue");		
 		String target = Constants.ACTION.MODIFY_DATASTREAM_BY_VALUE.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);		
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.DATASTREAM.ID.uri, datastreamId);
 			name = resourceAttributes.setReturn(Constants.DATASTREAM.NEW_MIME_TYPE.uri, newDatastreamMimeType);
 			name = resourceAttributes.setReturn(Constants.DATASTREAM.NEW_FORMAT_URI.uri, newDatastreamFormatURI);
 			name = resourceAttributes.setReturn(Constants.DATASTREAM.NEW_STATE.uri, newDatastreamState);			
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);	
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	} finally {
         getServer().logFinest("Exiting enforceModifyDatastreamByValue");
 	}
 	}
 
 	public final void enforceModifyDisseminator(Context context, String pid, String disseminatorId, String bmechNewPid, String disseminatorNewState) 
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforceModifyDisseminator");		
 		String target = Constants.ACTION.MODIFY_DISSEMINATOR.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);		
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.DISSEMINATOR.ID.uri, disseminatorId);
 			name = resourceAttributes.setReturn(Constants.BMECH.NEW_PID.uri, bmechNewPid);	
 			name = resourceAttributes.setReturn(Constants.BMECH.NEW_NAMESPACE.uri, extractNamespace(bmechNewPid));		
 			name = resourceAttributes.setReturn(Constants.DISSEMINATOR.NEW_STATE.uri, extractNamespace(disseminatorNewState));		
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);	
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	} finally {
         getServer().logFinest("Exiting enforceModifyDisseminator");
 	}
 	}
 	
 	public final void enforceModifyObject(Context context, String pid, String objectNewState) 
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforceModifyObject");		
 		String target = Constants.ACTION.MODIFY_OBJECT.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.OBJECT.NEW_STATE.uri, objectNewState);	
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);	
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	} finally {
         getServer().logFinest("Exiting enforceModifyObject");
 	}
 	}
 
 	public final void enforcePurgeDatastream(Context context, String pid, String datastreamId, Date endDT) 
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforcePurgeDatastream");		
 		String target = Constants.ACTION.PURGE_DATASTREAM.uri;
 		log("enforcing " + target);
 		String name = "";
 		context.setActionAttributes(null);		
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.DATASTREAM.ID.uri, datastreamId);	
 			name = resourceAttributes.setReturn(Constants.RESOURCE.AS_OF_DATETIME.uri, ensureDate (endDT, context));	
 	} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);	
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	} finally {
         getServer().logFinest("Exiting enforcePurgeDatastream");
 	}
 	}
 
 	public final void enforcePurgeDisseminator(Context context, String pid, String disseminatorId, Date endDT)
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforcePurgeDisseminator");		
 		String target = Constants.ACTION.PURGE_DISSEMINATOR.uri;
 		log("enforcing " + target);
 		String name = "";
 		context.setActionAttributes(null);		
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		try {
 			name = resourceAttributes.setReturn(Constants.DISSEMINATOR.ID.uri, disseminatorId);
 			name = resourceAttributes.setReturn(Constants.RESOURCE.AS_OF_DATETIME.uri, ensureDate (endDT, context));	
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);	
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	} finally {
         getServer().logFinest("Exiting enforcePurgeDisseminator");
 	}
 	}
 	
 	public final void enforcePurgeObject(Context context, String pid) 
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforcePurgeObject");		
 		String target = Constants.ACTION.PURGE_OBJECT.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		context.setResourceAttributes(null);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	} finally {
         getServer().logFinest("Exiting enforcePurgeObject");
 	}
 	}
 
 	public final void enforceSetDatastreamState(Context context, String pid, String datastreamId, String datastreamNewState) 
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforceSetDatastreamState");		
 		String target = Constants.ACTION.SET_DATASTREAM_STATE.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.DATASTREAM.ID.uri, datastreamId);	
 			name = resourceAttributes.setReturn(Constants.DATASTREAM.NEW_STATE.uri, datastreamNewState);	
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);	
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	} finally {
         getServer().logFinest("Exiting enforceSetDatastreamState");
 	}
 	}
 	
 	public final void enforceSetDisseminatorState(Context context, String pid, String disseminatorId, String disseminatorNewState) 
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforceSetDisseminatorState");		
 		String target = Constants.ACTION.SET_DISSEMINATOR_STATE.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.DISSEMINATOR.ID.uri, disseminatorId);	
 			name = resourceAttributes.setReturn(Constants.DISSEMINATOR.NEW_STATE.uri, disseminatorNewState);	
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);	
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	} finally {
         getServer().logFinest("Exiting enforceSetDisseminatorState");
 	}
 	}
 	
 	public void enforceDescribeRepository(Context context) 
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforceDescribeRepository");		
 		String target = Constants.ACTION.DESCRIBE_REPOSITORY.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		context.setResourceAttributes(null);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIA.uri, "", "", context);
 	} finally {
         getServer().logFinest("Exiting enforceDescribeRepository");
 	}
 	}
 
 	public void enforceFindObjects(Context context) 
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforceFindObjects");		
 		String target = Constants.ACTION.FIND_OBJECTS.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		context.setResourceAttributes(null);
 		log("enforceFindObjects, subject (from context)=" + context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri));
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIA.uri, "", "", context);
 	} finally {
         getServer().logFinest("Exiting enforceFindObjects");
 	}
 	}
 	
 	public void enforceRIFindObjects(Context context) 
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforceRIFindObjects");		
 		String target = Constants.ACTION.RI_FIND_OBJECTS.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		context.setResourceAttributes(null);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIA.uri, "", "", context);
 	} finally {
         getServer().logFinest("Exiting enforceRIFindObjects");
 	}
 	}
 
 	public void enforceGetDatastreamDissemination(Context context, String pid, String datastreamId, Date asOfDate) 
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforceGetDatastreamDissemination");		
 		String target = Constants.ACTION.GET_DATASTREAM_DISSEMINATION.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.DATASTREAM.ID.uri, datastreamId);	
 			name = resourceAttributes.setReturn(Constants.RESOURCE.AS_OF_DATETIME.uri, ensureDate(asOfDate, context));	
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);			
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIA.uri, pid, extractNamespace(pid), context);
 	} finally {
         getServer().logFinest("Exiting enforceGetDatastreamDissemination");
 	}
 	}
 	
 	public void enforceGetDissemination(Context context, String pid, String bdefPid, String methodName, Date asOfDate,
 			String objectState, String bdefState, String bmechPid, String bmechState, String dissState) 
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforceGetDissemination");		
 		String target = Constants.ACTION.GET_DISSEMINATION.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);	
 		MultiValueMap resourceAttributes = new MultiValueMap();		
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.BDEF.PID.uri, bdefPid);	
 			name = resourceAttributes.setReturn(Constants.BDEF.NAMESPACE.uri, extractNamespace(bdefPid));	
 			name = resourceAttributes.setReturn(Constants.DISSEMINATOR.METHOD.uri, methodName);
 			name = resourceAttributes.setReturn(Constants.BMECH.PID.uri, bmechPid);	
 			name = resourceAttributes.setReturn(Constants.BMECH.NAMESPACE.uri, extractNamespace(bmechPid));	
 			name = resourceAttributes.setReturn(Constants.OBJECT.STATE.uri, objectState);
 			name = resourceAttributes.setReturn(Constants.DISSEMINATOR.STATE.uri, dissState);	
 			name = resourceAttributes.setReturn(Constants.BDEF.STATE.uri, bdefState);	
 			name = resourceAttributes.setReturn(Constants.BMECH.STATE.uri, bmechState);
 			name = resourceAttributes.setReturn(Constants.RESOURCE.AS_OF_DATETIME.uri, ensureDate(asOfDate, context));
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}		
 		context.setResourceAttributes(resourceAttributes);			
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIA.uri, pid, extractNamespace(pid), context);
 	} finally {
         getServer().logFinest("Exiting enforceGetDissemination");
 	}
 	}
 
 	public void enforceGetObjectHistory(Context context, String pid) 
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforceGetObjectHistory");		
 		String target = Constants.ACTION.GET_OBJECT_HISTORY.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		context.setResourceAttributes(null);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIA.uri, pid, extractNamespace(pid), context);
 	} finally {
         getServer().logFinest("Exiting enforceGetObjectHistory");
 	}
 	}
 
 	public void enforceGetObjectProfile(Context context, String pid, Date asOfDate) 
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforceGetObjectProfile");		
 		String target = Constants.ACTION.GET_OBJECT_PROFILE.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.RESOURCE.AS_OF_DATETIME.uri, ensureDate(asOfDate, context));
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIA.uri, pid, extractNamespace(pid), context);
 	} finally {
         getServer().logFinest("Exiting enforceGetObjectProfile");
 	}
 	}
 
 	public void enforceListDatastreams(Context context, String pid, Date asOfDate) 
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforceListDatastreams");		
 		String target = Constants.ACTION.LIST_DATASTREAMS.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.RESOURCE.AS_OF_DATETIME.uri, ensureDate(asOfDate, context));				
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIA.uri, pid, extractNamespace(pid), context);
 	} finally {
         getServer().logFinest("Exiting enforceListDatastreams");
 	}
 	}
 
 	public void enforceListMethods(Context context, String pid, Date asOfDate) 
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforceListMethods");		
 		String target = Constants.ACTION.LIST_METHODS.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.RESOURCE.AS_OF_DATETIME.uri, ensureDate(asOfDate, context));				
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIA.uri, pid, extractNamespace(pid), context);
 	} finally {
         getServer().logFinest("Exiting enforceListMethods");
 	}
 	}
 	
 	public void enforceAdminPing(Context context)
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforceAdminPing");		
 		String target = Constants.ACTION.ADMIN_PING.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		context.setResourceAttributes(null);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, "", "", "", context);
 	} finally {
         getServer().logFinest("Exiting enforceAdminPing");
 	}
 	}
 	
 	public void enforceServerShutdown(Context context)
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforceServerShutdown");		
 		String target = Constants.ACTION.SERVER_SHUTDOWN.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		context.setResourceAttributes(null);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, "", "", "", context);
 	} finally {
         getServer().logFinest("Exiting enforceServerShutdown");
 	}
 	}
 	
 	public void enforceServerStatus(Context context)
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforceServerStatus");		
 		String target = Constants.ACTION.SERVER_STATUS.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		context.setResourceAttributes(null);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, "", "", "", context);
 	} finally {
         getServer().logFinest("Exiting enforceServerStatus");
 	}
 	}
 
 	public void enforceOAIRespond(Context context)
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforceOAIRespond");		
 		String target = Constants.ACTION.OAI.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		context.setResourceAttributes(null);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, "", "", "", context);
 	} finally {
         getServer().logFinest("Exiting enforceOAIRespond");
 	}
 	}
 	
 	public void enforceUpload(Context context)
 	throws AuthzException {
 	try {
         getServer().logFinest("Entered enforceUpload");		
 		String target = Constants.ACTION.UPLOAD.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		context.setResourceAttributes(null);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, "", "", "", context);
 	} finally {
         getServer().logFinest("Exiting enforceUpload");
 	}
 	}
 	
 	public void enforce_Internal_DSState(Context context, String id, String state)
 	throws AuthzException {
 		try {
 	        getServer().logFinest("Entered enforce_Internal_DSState");		
 			String target = Constants.ACTION.INTERNAL_DSSTATE.uri;
 			log("enforcing " + target);
 			context.setActionAttributes(null);
 			MultiValueMap resourceAttributes = new MultiValueMap();
 			String name = "";
 			try {
 				name = resourceAttributes.setReturn(Constants.DATASTREAM.ID.uri, id);
 				name = resourceAttributes.setReturn(Constants.DATASTREAM.STATE.uri, state);
 			} catch (Exception e) {
 				context.setResourceAttributes(null);		
 				throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 			}
 			context.setResourceAttributes(resourceAttributes);
 			xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIA.uri, "", "", context);
 		} finally {
 	        getServer().logFinest("Exiting enforce_Internal_DSState");
 		}
 	}
 	
 
 	public void enforceResolveDatastream(Context context, Date ticketIssuedDateTime)
 	throws AuthzException {
 		try {
 	        getServer().logFinest("Entered enforceResolveDatastream");		
 			String target = Constants.ACTION.RESOLVE_DATASTREAM.uri;
 			log("enforcing " + target);
 			context.setResourceAttributes(null);
 			MultiValueMap actionAttributes = new MultiValueMap();
 			String name = "";
 			try {
 				String ticketIssuedDateTimeString = DateUtility.convertDateToString(ticketIssuedDateTime);
 				name = actionAttributes.setReturn(Constants.RESOURCE.TICKET_ISSUED_DATETIME.uri, ticketIssuedDateTimeString);
 			} catch (Exception e) {
 				context.setActionAttributes(null);		
 				throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 			}
 			context.setActionAttributes(actionAttributes);
 			xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, "", "", "", context);
 		} finally {
 	        getServer().logFinest("Exiting enforceResolveDatastream");
 		}
 	}
 	
 	public void enforceReloadPolicies(Context context)
 	throws AuthzException {
 		try {
 	        getServer().logFinest("Entered enforceReloadPolicies");		
 			String target = Constants.ACTION.RELOAD_POLICIES.uri;
 			log("enforcing " + target);
 			context.setResourceAttributes(null);
 			context.setActionAttributes(null);
 			xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, "", "", "", context);
 		} finally {
 	        getServer().logFinest("Exiting enforceReloadPolicies");
 		}
 	}
 
 	  private static final String pad(int n, int length) throws Exception {
 	  	String asString = Integer.toString(n);
 	  	if (asString.length() > length) {
 	  		throw new Exception("value as string is too long");
 	  	}
 	  	StringBuffer padding = new StringBuffer();
 	  	for (int i=0; i<(length - asString.length()); i++) {
 	  		padding.append('0');
 	  	}
 	  	return padding + asString; 
 	  }
 	  
 	  public static final String dateAsString (Date date) throws Exception {
 	  	 //2003-12-13T18:30:02Z
 	  	StringBuffer temp = new StringBuffer();
 	  	try {
 	  	temp.append(pad(1900 + date.getYear(),4));
 	  	temp.append('-');
 	  	temp.append(pad(1 + date.getMonth(),2));
 	  	temp.append('-');
 	  	temp.append(pad(date.getDate(),2));
 	  	temp.append('T');
 	  	temp.append(pad(date.getHours(),2));
 	  	temp.append(':');
 	  	temp.append(pad(date.getMinutes(),2));
 	  	temp.append(':');
 	  	temp.append(pad(date.getSeconds(),2));
 	  	temp.append('Z'); //<<<<<<<<<<<<XXXXXXXXXXX!!!!!!!!!!!!!!!!!
 	  	} catch (Exception e) {
 	  		slog("exception in dateAsString " + temp.toString());
 	  		slog(e + " " + e.getMessage());
 	  		throw e;
 	  	}
 	  	return temp.toString();
 	  }
 	  
 	  private static final void putAsOfDate (Hashtable resourceAttributes, Date asOfDate) throws Exception {
 	  	resourceAttributes.put("asOfDate", dateAsString(asOfDate));
 	  }
 	  
 	  private static boolean log = false;
 	  
 	  private final void log(String msg) {
 	  	if (log) {
 		  	System.err.println(msg);	  		
 	  	}
 	  }
 	  
 	  private static boolean slog = false;
 	  
 	  private static final void slog(String msg) {
 	  	if (slog) {
 		  	System.err.println(msg);	  		
 	  	}
 	  }	  
 
 	  private final String logged(String msg) {
 	  	log(msg);
 	  	return msg;
 	  }
 
 	
 }
