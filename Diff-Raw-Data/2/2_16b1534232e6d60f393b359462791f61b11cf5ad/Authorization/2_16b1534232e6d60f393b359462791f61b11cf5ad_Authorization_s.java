 package fedora.server.security;
 
 import java.util.Date;
 import fedora.server.Context;
 import fedora.server.errors.authorization.AuthzException;
 
 public interface Authorization {
 	
 
 	//subject
 	public static final String SUBJECT_CATEGORY = "urn:oasis:names:tc:xacml:1.0:subject";
 	public static final String SUBJECT_CATEGORY_ACCESS = "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject";
                                      
 	//action	
 	public static final String ACTION_CATEGORY = "urn:oasis:names:tc:xacml:1.0:action";
 	public static final String ACTION_CATEGORY_ACCESS = "urn:oasis:names:tc:xacml:1.0:action-category:access-action";
 
 	//resource
 	public static final String RESOURCE_CATEGORY = "urn:oasis:names:tc:xacml:1.0:resource";
 	public static final String RESOURCE_CATEGORY_ACCESS = "urn:oasis:names:tc:xacml:1.0:resource-category:access-resource";
 	
 	//environment
 	public static final String ENVIRONMENT_CATEGORY = "urn:oasis:names:tc:xacml:1.0:environment";
 	public static final String ENVIRONMENT_CATEGORY_ACCESS = "urn:oasis:names:tc:xacml:1.0:environment-category:access-environment";
 
 	public void reloadPolicies(Context context)	throws Exception;
 	
 	public void enforceAddDatastream(Context context, String pid, String dsId, String[] altIDs, 
 			String MIMEType, String formatURI, String dsLocation, String controlGroup, String dsState) 
 	throws AuthzException;
 	
 	public void enforceAddDisseminator(Context context, String pid, String bDefPid, String bMechPid, String dissState) 
 	throws AuthzException;
 	
 	public void enforceExportObject(Context context, String pid, String format, String exportContext, String exportEncoding) 
 	throws AuthzException;
 
 	public void enforceGetDatastream(Context context, String pid, String datastreamId, Date asOfDateTime) //x 
 	throws AuthzException; 
 
 	public void enforceGetDatastreamHistory(Context context, String pid, String datastreamId) 
 	throws AuthzException;
 
 	public void enforceGetDatastreams(Context context, String pid, Date asOfDate, String state) 
 	throws AuthzException;
 	
 	public void enforceGetDisseminator(Context context, String pid, String disseminatorId, Date asOfDateTime) 
 	throws AuthzException;
 	
 	public void enforceGetDisseminators(Context context, String pid, Date asOfDate, String disseminatorState) 
 	throws AuthzException;
 
 	public void enforceGetDisseminatorHistory(Context context, String pid, String disseminatorPid) 
 	throws AuthzException;
 	
 	public void enforceGetNextPid(Context context, String namespace, int nNewPids) 
 	throws AuthzException; 
 	
 	public void enforceGetObjectProperties(Context context, String pid) 
 	throws AuthzException;
 	
 	public void enforceGetObjectXML(Context context, String pid, String objectXmlEncoding) 
 	throws AuthzException;
 
 	public void enforceIngestObject(Context context, String pid, String format, String ingestEncoding) 
 	throws AuthzException;
 	
 	public void enforceListObjectInFieldSearchResults(Context context, String pid) 
 	throws AuthzException;
 	
 	public void enforceListObjectInResourceIndexResults(Context context, String pid) 
 	throws AuthzException;
 
 	public void enforceModifyDatastreamByReference(Context context, String pid, String datastreamId, String[] altIDs, 
 			String mimeType, String formatURI, String datastreamNewLocation, String datastreamNewState)
 	throws AuthzException;
 
 	public void enforceModifyDatastreamByValue(Context context, String pid, String datastreamId, String[] altIDs, 
 			String mimeType, String formatURI, String newDatastreamState)
 	throws AuthzException;
 	
 	public void enforceModifyDisseminator(Context context, String pid, String disseminatorId, String mechanismPid, String disseminatorState) 
 	throws AuthzException;
 	
 	public void enforceModifyObject(Context context, String pid, String objectState) 
 	throws AuthzException;
 	
 	public void enforcePurgeDatastream(Context context, String pid, String datastreamId, Date endDT) 
 	throws AuthzException;
 
 	public void enforcePurgeDisseminator(Context context, String pid, String disseminatorId, Date endDT) //x
 	throws AuthzException;
 
 	public void enforcePurgeObject(Context context, String pid) 
 	throws AuthzException;
 
 	public void enforceSetDatastreamState(Context context, String pid, String datastreamId, String datastreamNewState) 
 	throws AuthzException;
 
 	public void enforceSetDisseminatorState(Context context, String pid, String disseminatorId, String disseminatorNewState) 
 	throws AuthzException;
 
 	//APIA
 
 	public void enforceDescribeRepository(Context context) 
 	throws AuthzException;
 
 	public void enforceFindObjects(Context context) 
 	throws AuthzException;
 	
 	public void enforceRIFindObjects(Context context) 
 	throws AuthzException;
 
 	public void enforceGetDatastreamDissemination(Context context, String pid, String datastreamId, Date asOfDate) 
 	throws AuthzException;
 	
 	public void enforceGetDissemination(Context context, String pid, String bDefPID, String methodName, Date asOfDate,
 			String authzAux_objState, String authzAux_bdefState, String authzAux_bmechPID, String authzAux_bmechState, String authzAux_dissState) 
 	throws AuthzException;
 
 	public void enforceGetObjectHistory(Context context, String pid) 
 	throws AuthzException;
 
 	public void enforceGetObjectProfile(Context context, String pid, Date asOfDate) 
 	throws AuthzException;
 
 	public void enforceListDatastreams(Context context, String pid, Date asOfDate) 
 	throws AuthzException;
 
 	public void enforceListMethods(Context context, String pid, Date ofAsDate) 
 	throws AuthzException;
 	
 	public void enforceAdminPing(Context context) 
 	throws AuthzException;
 	
 	public void enforceServerShutdown(Context context)
 	throws AuthzException;
 	
 	public void enforceServerStatus(Context context)
 	throws AuthzException;
 
 	public void enforceOAIRespond(Context context)
 	throws AuthzException;	
 
 	public void enforceUpload(Context context)
 	throws AuthzException;		
 
 	public void enforce_Internal_DSState(Context context, String PID, String state)
 	throws AuthzException;
 
 	public void enforceResolveDatastream(Context context, Date ticketDateTime)
 	throws AuthzException;
 	
 	public void enforceReloadPolicies(Context context)
 	throws AuthzException;
 	
 }
 
 
 
