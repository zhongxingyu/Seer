 package fedora.server.security;
 
 
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.Map;
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.catalina.realm.JAASRealm;
 
 import fedora.common.Constants;
 import fedora.server.Context;
 import fedora.server.Module;
 import fedora.server.MultiValueMap;
 import fedora.server.Server;
 import fedora.server.errors.AuthzOperationalException;
 import fedora.server.errors.ModuleInitializationException;
 import fedora.server.errors.NotAuthorizedException;
 import fedora.server.storage.DOManager;
 
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
 
 	private String surrogatePoliciesDirectory = "";
 	private String repositoryPoliciesDirectory = "";
 	private String objectPoliciesDirectory = "";
 	private String combiningAlgorithm = ""; //"com.sun.xacml.combine.OrderedDenyOverridesPolicyAlg";
 	private String enforceMode = "";
 
 	private final String SURROGATE_POLICIES_DIRECTORY = "SURROGATE-POLICIES-DIRECTORY";
 	private final String REPOSITORY_POLICIES_DIRECTORY = "REPOSITORY-POLICIES-DIRECTORY";
 	private final String OBJECT_POLICIES_DIRECTORY = "OBJECT-POLICIES-DIRECTORY";
 	private final String COMBINING_ALGORITHM = "XACML-COMBINING-ALGORITHM";
 	private final String ENFORCE_MODE = "ENFORCE-MODE";
 
 	
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
 
     if (moduleParameters.containsKey(SURROGATE_POLICIES_DIRECTORY)) {
     	surrogatePoliciesDirectory = 
     		((String) moduleParameters.get(SURROGATE_POLICIES_DIRECTORY)).startsWith(File.separator) ? "" : serverHome 
 		+ (String) moduleParameters.get(SURROGATE_POLICIES_DIRECTORY);
     	System.err.println("surrogatePoliciesDirectory=" + surrogatePoliciesDirectory);
     }
     if (moduleParameters.containsKey(REPOSITORY_POLICIES_DIRECTORY)) {
     	repositoryPoliciesDirectory = 
     		((String) moduleParameters.get(REPOSITORY_POLICIES_DIRECTORY)).startsWith(File.separator) ? "" : serverHome 
 		+ (String) moduleParameters.get(REPOSITORY_POLICIES_DIRECTORY);
     	System.err.println("repositoryPoliciesDirectory=" + repositoryPoliciesDirectory);
     }
     if (moduleParameters.containsKey(OBJECT_POLICIES_DIRECTORY)) {
     	objectPoliciesDirectory =
     		((String) moduleParameters.get(OBJECT_POLICIES_DIRECTORY)).startsWith(File.separator) ? "" : serverHome 
     	+ (String) moduleParameters.get(OBJECT_POLICIES_DIRECTORY);
     	System.err.println("objectPoliciesDirectory=" + objectPoliciesDirectory);
     }
     if (moduleParameters.containsKey(COMBINING_ALGORITHM)) {
     	combiningAlgorithm = (String) moduleParameters.get(COMBINING_ALGORITHM);
     }
     if (moduleParameters.containsKey(ENFORCE_MODE)) {
     	enforceMode = (String) moduleParameters.get(ENFORCE_MODE);
     }
 	System.err.println("DefaultAuthorization constructor end");
   }
 
   
   /**
    * <p>Initializes the module.</p>
    *
    * @throws ModuleInitializationException If the module cannot be initialized.
    */
   public void initModule() throws ModuleInitializationException {
 	System.err.println("DefaultAuthorization.initModule()");
   }
 
   public void postInitModule() throws ModuleInitializationException {
   	System.err.println("in DefaultAuthorization.postInitModule() 1");
     DOManager m_manager = (DOManager) getServer().getModule("fedora.server.storage.DOManager");
   	System.err.println("in DefaultAuthorization.postInitModule() 2");
     if (m_manager == null) {
     	System.err.println("in DefaultAuthorization.postInitModule() 3");
       throw new ModuleInitializationException("Can't get a DOManager from Server.getModule", getRole());
     }
   	System.err.println("in DefaultAuthorization.postInitModule() 4");
     try {
       	System.err.println("in DefaultAuthorization.postInitModule() 5");
         xacmlPep = PolicyEnforcementPoint.getInstance();
       	System.err.println("in DefaultAuthorization.postInitModule() 6");
         xacmlPep.initPep(enforceMode, combiningAlgorithm, repositoryPoliciesDirectory, objectPoliciesDirectory, m_manager);
       	System.err.println("in DefaultAuthorization.postInitModule() 7");
     } catch (Throwable e1) {
       	System.err.println("in DefaultAuthorization.postInitModule() 8");
     	ModuleInitializationException e2 = new ModuleInitializationException(e1.getMessage(), getRole());
     	throw e2;
     }
     File surrogatePolicyDirectoryFile = new File(surrogatePoliciesDirectory);    
     if (surrogatePolicyDirectoryFile.isDirectory() && surrogatePolicyDirectoryFile.canRead()) {
         Transom.getInstance().setAllowSurrogate(true);
         Transom.getInstance().setSurrogatePolicyDirectory(surrogatePolicyDirectoryFile);
     }
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
 	throws NotAuthorizedException {
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
 	}
 	
 	public final void enforceAddDisseminator(Context context, String pid, String bDefPid, String bMechPid, String dissState)
 	throws NotAuthorizedException { 
 		String target = Constants.ACTION.ADD_DISSEMINATOR.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.DISSEMINATOR.BDEF_PID.uri, bDefPid);
 			name = resourceAttributes.setReturn(Constants.DISSEMINATOR.BMECH_PID.uri, bMechPid);			
 			name = resourceAttributes.setReturn(Constants.DISSEMINATOR.STATE.uri, dissState);
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	}
 		
 	public final void enforceExportObject(Context context, String pid, String format, String exportContext, String exportEncoding)
 	throws NotAuthorizedException { 
 		String target = Constants.ACTION.EXPORT_OBJECT.uri;
 		log("enforcing " + target);
 		MultiValueMap actionAttributes = new MultiValueMap();
 		String name = "";
 		try { 
 			name = actionAttributes.setReturn(Constants.ACTION.FORMAT_URI.uri, format);
 			name = actionAttributes.setReturn(Constants.ACTION.CONTEXT.uri, exportContext);			
 			name = actionAttributes.setReturn(Constants.ACTION.ENCODING.uri, exportEncoding);
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setActionAttributes(actionAttributes);
 		context.setResourceAttributes(null);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	}			
 	
 	
 	public final void enforceGetDisseminatorHistory(Context context, String pid, String disseminatorId) 
 	throws NotAuthorizedException {
 		String target = Constants.ACTION.GET_DISSEMINATOR_HISTORY.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.DISSEMINATOR.PID.uri, disseminatorId);	
 			name = resourceAttributes.setReturn(Constants.DISSEMINATOR.NAMESPACE.uri, extractNamespace(disseminatorId));	
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	}	
 
 	public final void enforceGetNextPid(Context context, String namespace, int nNewPids) 
 	throws NotAuthorizedException {
 		String target = Constants.ACTION.GET_NEXT_PID.uri;	
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			String nNewPidsAsString = Integer.toString(nNewPids);
 			name = resourceAttributes.setReturn(Constants.ACTION.N_PIDS.uri, nNewPidsAsString);
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, "", namespace, context);
 	}	
 
 	public final void enforceGetDatastream(Context context, String pid, String datastreamId, Date asOfDateTime) 
 	throws NotAuthorizedException { 
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
 	}
 	
 	public final void enforceGetDatastreamHistory(Context context, String pid, String datastreamId) 
 	throws NotAuthorizedException {
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
 	throws NotAuthorizedException { 
 		String target = Constants.ACTION.GET_DATASTREAMS.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
         System.err.println("in enforceGetDatastreams");
 		String name = ""; 
 		try {
 	        System.err.println("in enforceGetDatastreams, before setting datastreamState=" + datastreamState);
 	        name = resourceAttributes.setReturn(Constants.MODEL.DATASTREAM_STATE.uri, datastreamState);	
 	        System.err.println("in enforceGetDatastreams, before setting asOfDateAsString");
 	        name = resourceAttributes.setReturn(Constants.RESOURCE.AS_OF_DATE.uri, ensureDate(asOfDate, context));
 		    System.err.println("in enforceGetDatastreams, after setting asOfDateAsString");
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
         System.err.println("in enforceGetDatastreams, before setting resourceAttributes");
 		context.setResourceAttributes(resourceAttributes);
         System.err.println("in enforceGetDatastreams, after setting resourceAttributes");
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
         System.err.println("in enforceGetDatastreams, after calling global enforce");
 	}	
 
 	public final void enforceGetDisseminator(Context context, String pid, String disseminatorPid, Date asOfDate) 
 	throws NotAuthorizedException { 
 		String target = Constants.ACTION.GET_DISSEMINATOR.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.DISSEMINATOR.PID.uri, disseminatorPid);	
 			name = resourceAttributes.setReturn(Constants.DISSEMINATOR.NAMESPACE.uri, extractNamespace(disseminatorPid));
 			name = resourceAttributes.setReturn(Constants.RESOURCE.AS_OF_DATE.uri, ensureDate(asOfDate, context));	
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);		
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	}	
 	
 	public final void enforceGetDisseminators(Context context, String pid, Date asOfDate, String disseminatorState) 
 	throws NotAuthorizedException { 
 		String target = Constants.ACTION.GET_DISSEMINATORS.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.DISSEMINATOR.STATE.uri, disseminatorState);	
 			name = resourceAttributes.setReturn(Constants.RESOURCE.AS_OF_DATE.uri, ensureDate(asOfDate, context));	
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);	
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	}
 	
 	public final void enforceGetObjectProperties(Context context, String pid) 
 	throws NotAuthorizedException {
 		String target = Constants.ACTION.GET_OBJECT_PROPERTIES.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		context.setResourceAttributes(null);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	}	
 	
 	public final void enforceGetObjectXML(Context context, String pid, String objectXmlEncoding) 
 	throws NotAuthorizedException {
 		String target = Constants.ACTION.GET_OBJECT_XML.uri;
 		log("enforcing " + target);
 		context.setResourceAttributes(null);
 		MultiValueMap actionAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = actionAttributes.setReturn(Constants.ACTION.ENCODING.uri, objectXmlEncoding);	
 		} catch (Exception e) {
 			context.setActionAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setActionAttributes(actionAttributes);		
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	}		
 	
 	public final void enforceIngestObject(Context context, String pid, String format, String ingestEncoding)
 	throws NotAuthorizedException {
 		String target = Constants.ACTION.INGEST_OBJECT.uri;
 		log("enforcing " + target);
 		MultiValueMap actionAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = actionAttributes.setReturn(Constants.ACTION.FORMAT_URI.uri, format);	
 			name = actionAttributes.setReturn(Constants.ACTION.ENCODING.uri, ingestEncoding);			
 		} catch (Exception e) {
 			context.setActionAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setActionAttributes(actionAttributes);
 		context.setResourceAttributes(null);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	}
 	
 	public final void enforceListObjectInFieldSearchResults(Context context, String pid) 
 	throws NotAuthorizedException {
 		String target = Constants.ACTION.LIST_OBJECT_IN_FIELD_SEARCH_RESULTS.uri;
 		log("enforcing " + target);
 		if (enforceListObjectInFieldSearchResults) {
 			context.setActionAttributes(null);
 			context.setResourceAttributes(null);
 			xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIA.uri, pid, extractNamespace(pid), context);
 		}
 	}	
 	
 	public final void enforceListObjectInResourceIndexResults(Context context, String pid) 
 	throws NotAuthorizedException {
 		String target = Constants.ACTION.LIST_OBJECT_IN_RESOURCE_INDEX_RESULTS.uri;
 		log("enforcing " + target);
 		if (enforceListObjectInResourceIndexResults) {
 			context.setActionAttributes(null);
 			context.setResourceAttributes(null);
 			xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIA.uri, pid, extractNamespace(pid), context);
 		}
 	}
 
 	public final void enforceModifyDatastreamByReference(Context context, String pid, String datastreamId, 
 			String[] altIDs, // how to handle? 
 			String datastreamNewMimeType, String datastreamNewFormatURI, String datastreamNewLocation, String datastreamNewState) //x
 	throws NotAuthorizedException {
 		String target = Constants.ACTION.MODIFY_DATASTREAM_BY_REFERENCE.uri;
 		log("enforcing " + target);
 		MultiValueMap actionAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = actionAttributes.setReturn(Constants.ACTION.DATASTREAM_MIME_TYPE.uri, datastreamNewMimeType);
 			name = actionAttributes.setReturn(Constants.ACTION.DATASTREAM_FORMAT_URI.uri, datastreamNewFormatURI);			
 			name = actionAttributes.setReturn(Constants.ACTION.DATASTREAM_LOCATION.uri, datastreamNewLocation);			
 			name = actionAttributes.setReturn(Constants.ACTION.DATASTREAM_STATE.uri, datastreamNewState);	
 		} catch (Exception e) {
 			context.setActionAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setActionAttributes(actionAttributes);
 		
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.DATASTREAM.ID.uri, datastreamId);
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);	
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	}
 	
 	public final void enforceModifyDatastreamByValue(Context context, String pid, String datastreamId, 
 			String[] altIDs, // how to handle?
 			String newDatastreamMimeType, String newDatastreamFormatURI, String newDatastreamState)
 	throws NotAuthorizedException { 
 		String target = Constants.ACTION.MODIFY_DATASTREAM_BY_VALUE.uri;
 		log("enforcing " + target);
 		MultiValueMap actionAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = actionAttributes.setReturn(Constants.ACTION.DATASTREAM_MIME_TYPE.uri, newDatastreamMimeType);
 			name = actionAttributes.setReturn(Constants.ACTION.DATASTREAM_FORMAT_URI.uri, newDatastreamFormatURI);
 			name = actionAttributes.setReturn(Constants.ACTION.DATASTREAM_STATE.uri, newDatastreamState);
 		} catch (Exception e) {
 			context.setActionAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setActionAttributes(actionAttributes);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.DATASTREAM.ID.uri, datastreamId);
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);	
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	}	
 
 	public final void enforceModifyDisseminator(Context context, String pid, String disseminatorPid, String bmechNewPid, String disseminatorNewState) 
 	throws NotAuthorizedException {
 		String target = Constants.ACTION.MODIFY_DISSEMINATOR.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.DISSEMINATOR.PID.uri, disseminatorPid);
 			name = resourceAttributes.setReturn(Constants.DISSEMINATOR.NAMESPACE.uri, extractNamespace(disseminatorPid));
 			name = resourceAttributes.setReturn(Constants.ACTION.BMECH_PID.uri, bmechNewPid);	
 			name = resourceAttributes.setReturn(Constants.ACTION.BMECH_NAMESPACE.uri, extractNamespace(bmechNewPid));		
 			name = resourceAttributes.setReturn(Constants.ACTION.DISSEMINATOR_STATE.uri, extractNamespace(disseminatorNewState));		
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);	
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	}	
 	
 	public final void enforceModifyObject(Context context, String pid, String objectNewState) 
 	throws NotAuthorizedException {
 		String target = Constants.ACTION.MODIFY_OBJECT.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.ACTION.OBJECT_STATE.uri, objectNewState);	
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);	
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	}
 
 	public final void enforcePurgeDatastream(Context context, String pid, String datastreamId, Date endDT) 
 	throws NotAuthorizedException {
 		String target = Constants.ACTION.PURGE_DATASTREAM.uri;
 		log("enforcing " + target);
 		MultiValueMap actionAttributes = new MultiValueMap();
 		String name = "";
 		try {
			System.err.println("actionAttributes="+actionAttributes);
			System.err.println("Constants.DATASTREAM.AS_OF_DATETIME.uri="+Constants.DATASTREAM.AS_OF_DATETIME.uri);
			System.err.println("endDT="+endDT);
			name = actionAttributes.setReturn(Constants.DATASTREAM.AS_OF_DATETIME.uri, ensureDate (endDT, context));	
 		} catch (Exception e) {
 			context.setActionAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setActionAttributes(actionAttributes);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.DATASTREAM.ID.uri, datastreamId);	
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);	
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	}
 
 	public final void enforcePurgeDisseminator(Context context, String pid, String disseminatorId, Date endDT)
 	throws NotAuthorizedException {
 		String target = Constants.ACTION.PURGE_DISSEMINATOR.uri;
 		log("enforcing " + target);
 		MultiValueMap actionAttributes = new MultiValueMap();
 		String name = "";
 		try {
			name = actionAttributes.setReturn(Constants.DISSEMINATOR.AS_OF_DATETIME.uri, ensureDate (endDT, context));	
 		} catch (Exception e) {
 			context.setActionAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setActionAttributes(actionAttributes);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.DISSEMINATOR.ID.uri, disseminatorId);	
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);	
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	}	
 	
 	public final void enforcePurgeObject(Context context, String pid) 
 	throws NotAuthorizedException {
 		String target = Constants.ACTION.PURGE_OBJECT.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		context.setResourceAttributes(null);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	}	
 
 	public final void enforceSetDatastreamState(Context context, String pid, String datastreamId, String datastreamNewState) 
 	throws NotAuthorizedException {
 		String target = Constants.ACTION.SET_DATASTREAM_STATE.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.DATASTREAM.ID.uri, datastreamId);	
 			name = resourceAttributes.setReturn(Constants.ACTION.DATASTREAM_STATE.uri, datastreamNewState);	
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);	
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	}
 	
 	public final void enforceSetDisseminatorState(Context context, String pid, String disseminatorId, String disseminatorNewState) 
 	throws NotAuthorizedException {
 		String target = Constants.ACTION.SET_DISSEMINATOR_STATE.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.DISSEMINATOR.PID.uri, disseminatorId);	
 			name = resourceAttributes.setReturn(Constants.DISSEMINATOR.NAMESPACE.uri, extractNamespace(disseminatorId));			
 			name = resourceAttributes.setReturn(Constants.ACTION.DISSEMINATOR_STATE.uri, disseminatorNewState);	
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);	
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, pid, extractNamespace(pid), context);
 	}
 	
 	public void enforceDescribeRepository(Context context) 
 	throws NotAuthorizedException {
 		String target = Constants.ACTION.DESCRIBE_REPOSITORY.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		context.setResourceAttributes(null);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIA.uri, "", "", context);
 	}
 
 	public void enforceFindObjects(Context context) 
 	throws NotAuthorizedException {
 		String target = Constants.ACTION.FIND_OBJECTS.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		context.setResourceAttributes(null);
 		System.err.println("enforceFindObjects, subject (from context)=" + context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri));
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIA.uri, "", "", context);
 	}
 	
 	public void enforceRIFindObjects(Context context) 
 	throws NotAuthorizedException {
 		String target = Constants.ACTION.RI_FIND_OBJECTS.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		context.setResourceAttributes(null);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIA.uri, "", "", context);
 	}
 
 	public void enforceGetDatastreamDissemination(Context context, String pid, String datastreamId, Date asOfDate) 
 	throws NotAuthorizedException {
 		String target = Constants.ACTION.GET_DATASTREAM_DISSEMINATION.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.DATASTREAM.ID.uri, datastreamId);	
 			name = resourceAttributes.setReturn(Constants.RESOURCE.AS_OF_DATE.uri, ensureDate(asOfDate, context));	
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);			
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIA.uri, pid, extractNamespace(pid), context);
 	}
 	
 	public void enforceGetDissemination(Context context, String pid, String bDefPid, String methodName, Date asOfDate) 
 	throws NotAuthorizedException {
 		String target = Constants.ACTION.GET_DISSEMINATION.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.ACTION.BDEF_PID.uri, bDefPid);	
 			name = resourceAttributes.setReturn(Constants.ACTION.BDEF_NAMESPACE.uri, extractNamespace(bDefPid));	
 			name = resourceAttributes.setReturn(Constants.ACTION.DISSEMINATOR_METHOD.uri, methodName);
 			name = resourceAttributes.setReturn(Constants.RESOURCE.AS_OF_DATE.uri, ensureDate(asOfDate, context));	
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);			
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIA.uri, pid, extractNamespace(pid), context);
 	}
 
 	public void enforceGetObjectHistory(Context context, String pid) 
 	throws NotAuthorizedException {
 		String target = Constants.ACTION.GET_OBJECT_HISTORY.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		context.setResourceAttributes(null);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIA.uri, pid, extractNamespace(pid), context);
 	}
 
 	public void enforceGetObjectProfile(Context context, String pid, Date asOfDate) 
 	throws NotAuthorizedException {
 		String target = Constants.ACTION.GET_OBJECT_PROFILE.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.RESOURCE.AS_OF_DATE.uri, ensureDate(asOfDate, context));
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIA.uri, pid, extractNamespace(pid), context);
 	}
 
 	public void enforceListDatastreams(Context context, String pid, Date asOfDate) 
 	throws NotAuthorizedException {
 		String target = Constants.ACTION.LIST_DATASTREAMS.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.RESOURCE.AS_OF_DATE.uri, ensureDate(asOfDate, context));				
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIA.uri, pid, extractNamespace(pid), context);
 	}
 
 	public void enforceListMethods(Context context, String pid, Date asOfDate) 
 	throws NotAuthorizedException {
 		String target = Constants.ACTION.LIST_METHODS.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		MultiValueMap resourceAttributes = new MultiValueMap();
 		String name = "";
 		try {
 			name = resourceAttributes.setReturn(Constants.RESOURCE.AS_OF_DATE.uri, ensureDate(asOfDate, context));				
 		} catch (Exception e) {
 			context.setResourceAttributes(null);		
 			throw new AuthzOperationalException(target + " couldn't set " + name, e);	
 		}
 		context.setResourceAttributes(resourceAttributes);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIA.uri, pid, extractNamespace(pid), context);
 	}
 	
 	public void enforceAdminPing(Context context)
 	throws NotAuthorizedException {
 		String target = Constants.ACTION.ADMIN_PING.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		context.setResourceAttributes(null);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, Constants.ACTION.APIM.uri, "", "", context);
 	}
 	
 	public void enforceServerShutdown(Context context)
 	throws NotAuthorizedException {
 		String target = Constants.ACTION.SERVER_SHUTDOWN.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		context.setResourceAttributes(null);
		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, "", "", "", context);
 	}
 	
 	public void enforceServerStatus(Context context)
 	throws NotAuthorizedException {
 		String target = Constants.ACTION.SERVER_STATUS.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		context.setResourceAttributes(null);
		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, "", "", "", context);
 	}	
 
 	public void enforceOAIRespond(Context context)
 	throws NotAuthorizedException {
 		String target = Constants.ACTION.OAI.uri;
 		log("enforcing " + target);
 		context.setActionAttributes(null);
 		context.setResourceAttributes(null);
 		xacmlPep.enforce(context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri), target, "", "", "", context);
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
 	  		System.err.println("exception in dateAsString " + temp.toString());
 	  		System.err.println(e + " " + e.getMessage());
 	  		throw e;
 	  	}
 	  	return temp.toString();
 	  }
 	  
 	  private static final void putAsOfDate (Hashtable resourceAttributes, Date asOfDate) throws Exception {
 	  	resourceAttributes.put("asOfDate", dateAsString(asOfDate));
 	  }
 	  
 	  private boolean log = true;
 	  
 	  private final void log(String msg) {
 	  	if (log) {
 		  	System.err.println(msg);	  		
 	  	}
 	  }
 
 	  private final String logged(String msg) {
 	  	log(msg);
 	  	return msg;
 	  }
 
 	
 }
