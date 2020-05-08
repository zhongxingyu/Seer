 package fedora.server.management;
 
 import java.io.*;
 import java.text.*;
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 import javax.xml.parsers.*;
 import org.w3c.dom.*;
 
 import org.apache.xml.serialize.*;
 import org.apache.commons.betwixt.XMLUtils;
 
 import org.apache.log4j.Logger;
 
 import fedora.server.*;
 import fedora.server.errors.*;
 import fedora.server.errors.authorization.AuthzException;
 import fedora.server.storage.*;
 import fedora.server.storage.types.*;
 import fedora.server.utilities.*;
 import fedora.server.validation.*;
 import fedora.server.security.Authorization;
 import fedora.common.Constants;
 
 /**
  * Implements API-M without regard to the transport/messaging protocol.
  *
  * @author cwilper@cs.cornell.edu
  * @version $Id$
  */
 public class DefaultManagement
         extends Module implements Management, ManagementDelegate {
 
     /** Logger for this class. */
     private static Logger LOG = 
             Logger.getLogger(DefaultManagement.class.getName());
 
     private DOManager m_manager;
     private int m_uploadStorageMinutes;
     private int m_lastId;
     private File m_tempDir;
     private Hashtable<String, Long> m_uploadStartTime;
     private ExternalContentManager m_contentManager;
     private Authorization m_fedoraXACMLModule;
 
     /**
      * Creates and initializes the Management Module.
      * <p></p>
      * When the server is starting up, this is invoked as part of the
      * initialization process.
      *
      * @param moduleParameters A pre-loaded Map of name-value pairs comprising
      *        the intended configuration of this Module.
      * @param server The <code>Server</code> instance.
      * @param role The role this module fulfills, a java class name.
      * @throws ModuleInitializationException If initilization values are
      *         invalid or initialization fails for some other reason.
      */
     public DefaultManagement(Map moduleParameters, 
                              Server server, 
                              String role) 
                 throws ModuleInitializationException {
         super(moduleParameters, server, role);
     }
 
     public void initModule() 
             throws ModuleInitializationException {
 
         // how many minutes should we hold on to uploaded files? default=5
         String min=getParameter("uploadStorageMinutes");
         if (min==null) min="5";
         try {
             m_uploadStorageMinutes=Integer.parseInt(min);
             if (m_uploadStorageMinutes<1) {
                 throw new ModuleInitializationException("uploadStorageMinutes "
                         + "must be 1 or more, if specified.", getRole());
             }
         } catch (NumberFormatException nfe) {
             throw new ModuleInitializationException("uploadStorageMinutes must "
                     + "be an integer, if specified.", getRole());
         }
         // initialize storage area by 1) ensuring the directory is there
         // and 2) reading in the existing files, if any, and setting their
         // startTime to the current time.
         try {
             m_tempDir=new File(getServer().getHomeDir(), "management/upload");
             if (!m_tempDir.isDirectory()) {
                 m_tempDir.mkdirs();
             }
             // put leftovers in hash, while saving highest id as m_lastId
             m_uploadStartTime=new Hashtable<String, Long>();
             String[] fNames=m_tempDir.list();
             Long leftoverStartTime=new Long(System.currentTimeMillis());
             m_lastId=0;
             for (int i=0; i<fNames.length; i++) {
                 try {
                     int id=Integer.parseInt(fNames[i]);
                     if (id>m_lastId) m_lastId=id;
                     m_uploadStartTime.put(fNames[i], leftoverStartTime);
                 } catch (NumberFormatException nfe) {
                     // skip files that aren't named numerically
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
             throw new ModuleInitializationException("Error while initializing "
                     + "temporary storage area: " + e.getClass().getName() + ": "
                     + e.getMessage(), getRole());
         }
         
         // initialize variables pertaining to checksumming datastreams.
         if (Datastream.defaultChecksumType == null)
         {
             Datastream.defaultChecksumType = "DISABLED";
             try 
             {
                 String auto =getParameter("autoChecksum");
                 LOG.debug("Got Parameter: autoChecksum = " + auto);
                 if (auto.equalsIgnoreCase("true"))
                 {
                     Datastream.autoChecksum = true;
                     Datastream.defaultChecksumType = getParameter("checksumAlgorithm");
                 }
                 else
                 {
                     Datastream.autoChecksum = false;
                     Datastream.defaultChecksumType = "DISABLED";
                 }
                 LOG.info("autoChecksum is "+ auto);
                 LOG.info("defaultChecksumType is "+ Datastream.defaultChecksumType);
             }
             catch (Exception e)
             {
                 LOG.warn("Exception in getting default checksum type", e);
                 // IGNORE  
             }
         }
 
     }
 
     public void postInitModule()
             throws ModuleInitializationException {
         m_manager=(DOManager) getServer().getModule(
                 "fedora.server.storage.DOManager");
         if (m_manager==null) {
             throw new ModuleInitializationException("Can't get a DOManager "
                     + "from Server.getModule", getRole());
         }
         m_contentManager=(ExternalContentManager) getServer().getModule(
                 "fedora.server.storage.ExternalContentManager");
         if (m_contentManager==null) {
             throw new ModuleInitializationException("Can't get an ExternalContentManager "
                     + "from Server.getModule", getRole());
         }
         
         m_fedoraXACMLModule = (Authorization) getServer().getModule("fedora.server.security.Authorization");
         if (m_fedoraXACMLModule == null) {
             throw new ModuleInitializationException("Can't get Authorization module (in default management) from Server.getModule", getRole());
         }
 
     }
 
     public String ingestObject(Context context, 
                                InputStream serialization,
                                String logMessage, 
                                String format, 
                                String encoding, 
                                boolean newPid)
             throws ServerException {
         DOWriter w = null;
         try {
             LOG.info("Entered ingestObject");
             w=m_manager.getIngestWriter(Server.USE_DEFINITIVE_STORE, context, serialization, format, encoding, newPid);
             String pid=w.GetObjectPID();
             
             m_fedoraXACMLModule.enforceIngestObject(context, pid, format, encoding);
 
             w.commit(logMessage);
             return pid;
         } finally {
             finishModification(w, "ingestObject");
         }
     }
 
     private void finishModification(DOWriter w, String method)
             throws ServerException {
         LOG.info("Exiting " + method);
         if (w != null) {
             m_manager.releaseWriter(w);
         }
         if (LOG.isDebugEnabled()) {
             Runtime r=Runtime.getRuntime();
             LOG.debug("Memory: " + r.freeMemory() + " bytes free of " 
                     + r.totalMemory() + " available.");
         }
     }
 
     public Date modifyObject(Context context, 
                              String pid, 
                              String state,
                              String label, 
                              String ownerId,
                              String logMessage)
             throws ServerException {
         DOWriter w = null;
         try {
             LOG.info("Entered modifyObject");
 
             m_fedoraXACMLModule.enforceModifyObject(context, pid, state, ownerId); 
 
             checkObjectLabel(label);
 
             w=m_manager.getWriter(Server.USE_DEFINITIVE_STORE, context, pid);
             if (state!=null && !state.equals("")) {
                 if (!state.equals("A") && !state.equals("D") && !state.equals("I")) {
                   throw new InvalidStateException("The object state of \"" + state
                           + "\" is invalid. The allowed values for state are: "
                           + " A (active), D (deleted), and I (inactive).");
                 }                
                 w.setState(state);
             }
             //if (label!=null && !label.equals(""))
             if (label!=null) {
                 w.setLabel(label);
             }
            if (ownerId!=null) {
                w.setOwnerId(ownerId);
            }
             w.commit(logMessage);
             return w.getLastModDate();
         } finally {
             finishModification(w, "modifyObject");
         }
     }
 
     public Property[] getObjectProperties(Context context, String pid)
         throws ServerException {        
         try {
             LOG.info("Entered getObjectProperties");
 
             m_fedoraXACMLModule.enforceGetObjectProperties(context, pid);
             
             ArrayList<Property> props = new ArrayList<Property>();
             DOReader reader=m_manager.getReader(Server.USE_CACHE, context, pid);
             
             props.add(new Property(
                         "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
                         reader.getFedoraObjectType()));
             
 
             props.add(new Property(
             			"info:fedora/fedora-system:def/model#contentModel",
                         reader.getContentModelId()));
                         
             props.add(new Property(
                         "info:fedora/fedora-system:def/model#label",
                         reader.GetObjectLabel()));
                         
             props.add(new Property(
                         "info:fedora/fedora-system:def/model#state",
                         reader.GetObjectState()));
                         
             props.add(new Property(
                         "info:fedora/fedora-system:def/model#ownerId",
                         reader.getOwnerId()));
                         
             props.add(new Property(
                         "info:fedora/fedora-system:def/model#createdDate",
                         DateUtility.convertDateToString(reader.getCreateDate())));
                         
             props.add(new Property(
                         "info:fedora/fedora-system:def/view#lastModifiedDate",
                         DateUtility.convertDateToString(reader.getLastModDate())));
             
             //Property[] extProps=reader.getExtProperties();
             
             return (Property[])props.toArray(new Property[0]);
         } finally {
             LOG.info("Exiting getObjectProperties");
         }
     }
 
     public InputStream getObjectXML(Context context, 
                                     String pid, 
                                     String encoding) 
             throws ServerException {
         try {
             LOG.info("Entered getObjectXML");
             
             m_fedoraXACMLModule.enforceGetObjectXML(context, pid, encoding);
 
             DOReader reader=m_manager.getReader(Server.USE_CACHE, context, pid);
             InputStream instream=reader.GetObjectXML();
             return instream;
         } finally {
             LOG.info("Exiting getObjectXML");
         }
     }
 
     public InputStream exportObject(Context context, 
                                     String pid, 
                                     String format,
                                     String exportContext, 
                                     String encoding) 
             throws ServerException {
         try {
             LOG.info("Entered exportObject");
             
             m_fedoraXACMLModule.enforceExportObject(context, pid, format, exportContext, encoding);
                         
             DOReader reader=m_manager.getReader(Server.USE_DEFINITIVE_STORE, context, pid);
             InputStream instream=reader.ExportObject(format,exportContext);
             return instream;
         } finally {
             LOG.info("Exiting exportObject");
         }
     }
 
     public Date purgeObject(Context context, 
                             String pid, 
                             String logMessage,
                             boolean force)
             throws ServerException {
         if (force) {
             throw new GeneralException("Forced object removal is not "
                     + "yet supported.");
         }
         DOWriter w = null;
         try {
             LOG.info("Entered purgeObject");
 
             m_fedoraXACMLModule.enforcePurgeObject(context, pid);
             
             w=m_manager.getWriter(Server.USE_DEFINITIVE_STORE, context, pid);
             w.remove();
             w.commit(logMessage);
             return Server.getCurrentDate(context);
         } finally {
             finishModification(w, "purgeObject");
         }
     }
 
     public String addDatastream(Context context,
                                 String pid,
                                 String dsID,
                                 String[] altIDs,
                                 String dsLabel,
                                 boolean versionable,
                                 String MIMEType,
                                 String formatURI,
                                 String dsLocation,
                                 String controlGroup,
                                 String dsState,
                                 String checksumType,
                                 String checksum,
                                 String logMessage) throws ServerException {
 
         LOG.info("Entered addDatastream");
 
         // empty MIME types are allowed.  assume they meant "" if they provide it as null.
         if (MIMEType == null) MIMEType = "";
 
         // If the datastream ID is not specified directly, see
         // if we can get it from the RecoveryContext
         if (dsID == null && context instanceof RecoveryContext) {
             RecoveryContext rContext = (RecoveryContext) context;
             dsID = rContext.getRecoveryValue(Constants.RECOVERY.DATASTREAM_ID.uri);
             if (dsID != null) {
                 LOG.debug("Using new dsID from recovery context");
             }
         }
 
         // check for valid xml name for datastream ID
         if (dsID != null) {
             if (!XMLUtils.isWellFormedXMLName(dsID)) {
                 throw new InvalidXMLNameException("Invalid syntax for datastream ID. "
                         + "The datastream ID of \""+dsID+"\" is" 
                         + "not a valid XML Name");
             }
         }
                                        
         if ( dsID!=null && (dsID.equals("AUDIT") || dsID.equals("FEDORA-AUDITTRAIL"))) {
             throw new GeneralException("Creation of a datastream with an"
                 + " identifier of 'AUDIT' or 'FEDORA-AUDITTRAIL' is not permitted.");
         }
         DOWriter w=null;
         try {
             m_fedoraXACMLModule.enforceAddDatastream(context, pid, dsID,  altIDs, MIMEType,
                     formatURI, dsLocation, controlGroup, dsState, checksumType, checksum);
 
             checkDatastreamID(dsID);
             checkDatastreamLabel(dsLabel);
 
             w=m_manager.getWriter(Server.USE_DEFINITIVE_STORE, context, pid);
             Datastream ds;
             if (controlGroup.equals("X")) {
                 ds=new DatastreamXMLMetadata();
                 ds.DSInfoType="";  // field is now deprecated
                 try {
                     InputStream in;
                     if (dsLocation.startsWith("uploaded://")) {
                         in=getTempStream(dsLocation);
                     } else {
                         in=m_contentManager.getExternalContent(dsLocation, context).getStream();
                     }
                     ((DatastreamXMLMetadata) ds).xmlContent = getEmbeddableXML(in);
                     // If it's a RELS-EXT datastream, do validation
                     if (dsID!=null && dsID.equals("RELS-EXT")){
                         validateRelsExt(pid, new ByteArrayInputStream(
                             ((DatastreamXMLMetadata) ds).xmlContent));
                     }
                 } catch (Exception e) {
                     String extraInfo;
                     if (e.getMessage()==null)
                         extraInfo="";
                     else
                         extraInfo=" : " + e.getMessage();
                     throw new GeneralException("Error with " + dsLocation + extraInfo);
                 }
             } else if (controlGroup.equals("M")) {
                 ds=new DatastreamManagedContent();
                 ds.DSInfoType="DATA";
             } else if (controlGroup.equals("R") || controlGroup.equals("E")) {
                 ds=new DatastreamReferencedContent();
                 ds.DSInfoType="DATA";
             } else {
                 throw new GeneralException("Invalid control group: " + controlGroup);
             }
             ds.isNew=true;
             ds.DSControlGrp=controlGroup;
             ds.DSVersionable=versionable;
             if (!dsState.equals("A") && !dsState.equals("D") && !dsState.equals("I")) {
                 throw new InvalidStateException("The datastream state of \"" + dsState
                         + "\" is invalid. The allowed values for state are: "
                         + " A (active), D (deleted), and I (inactive).");
             }            
             ds.DSState= dsState;
             // set new datastream id if not provided...
             if (dsID==null || dsID.length()==0) {
                 ds.DatastreamID=w.newDatastreamID();
             } else {
                 if (dsID.indexOf(" ")!=-1) {
                     throw new GeneralException("Datastream ids cannot contain spaces.");
                 }
                 if (dsID.indexOf("+")!=-1) {
                     throw new GeneralException("Datastream ids cannot contain plusses.");
                 }
                 if (dsID.indexOf(":")!=-1) {
                     throw new GeneralException("Datastream ids cannot contain colons.");
                 }
                 if (w.GetDatastream(dsID, null)!=null) {
                     throw new GeneralException("A datastream already exists with ID: " + dsID);
                 } else {
                     ds.DatastreamID=dsID;
                 }
             }
             // add version level attributes and
             // create new ds version id ...
             ds.DSVersionID=ds.DatastreamID + ".0";
             ds.DSLabel=dsLabel;
             ds.DSLocation=dsLocation;
             if (dsLocation != null) {
                 ValidationUtility.validateURL(dsLocation, false);
             }
             ds.DSFormatURI=formatURI;
             ds.DatastreamAltIDs = altIDs;
             ds.DSMIME=MIMEType;
             ds.DSChecksumType = checksumType;
             if (checksumType == null)
             {
                 ds.DSChecksumType = Datastream.getDefaultChecksumType();
             }
             if (checksum != null && checksumType != null)
             {
                 String check = ds.getChecksum();
                 if (!checksum.equals(check))
                 {
                     throw new ValidationException("Checksum Mismatch: " + check);
                 }
             }
             Date nowUTC = Server.getCurrentDate(context);
             ds.DSCreateDT=nowUTC;
             AuditRecord audit=new fedora.server.storage.types.AuditRecord();
             audit.id=w.newAuditRecordID();
             audit.processType="Fedora API-M";
             audit.action="addDatastream";
             audit.componentID=ds.DatastreamID;
             audit.responsibility=context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri);
             audit.date=nowUTC;
             audit.justification=logMessage;
             w.getAuditRecords().add(audit);
             w.addDatastream(ds, true);
             w.commit("Added a new datastream");
             return ds.DatastreamID;
         } finally {
             finishModification(w, "addDatastream");
         }
     }
 
     public String addDisseminator(Context context,
                                     String pid, String bDefPid, String bMechPid,
                                     String dissLabel,
                                     DSBindingMap bindingMap,
                                     String dissState,
                                     String logMessage) throws ServerException {
 
         DOWriter w=null;
         try {
             LOG.info("Entered addDisseminator");
                 
             m_fedoraXACMLModule.enforceAddDisseminator(context, pid, 
                     bDefPid, bMechPid, dissState);
 
             checkDisseminatorLabel(dissLabel);
 
             w=m_manager.getWriter(Server.USE_DEFINITIVE_STORE, context, pid);
             Disseminator diss = new Disseminator();
             diss.isNew=true;
             diss.parentPID = pid;
 
             if (!dissState.equals("A") && !dissState.equals("D") && !dissState.equals("I")) {
                 throw new InvalidStateException("The disseminator state of \"" + dissState
                         + "\" is invalid. The allowed values for state are: "
                         + " A (active), D (deleted), and I (inactive).");
             }
             diss.dissState= dissState;
             diss.dissLabel = dissLabel;
             diss.bMechID = bMechPid;
             diss.bDefID = bDefPid;
             Date nowUTC = Server.getCurrentDate(context);
             diss.dissCreateDT = nowUTC;
 
             // See if we can get the new disseminator ID from the RecoveryContext
             String dissID = null;
             if (context instanceof RecoveryContext) {
                 RecoveryContext rContext = (RecoveryContext) context;
                 dissID = rContext.getRecoveryValue(Constants.RECOVERY.DISSEMINATOR_ID.uri);
             }
             if (dissID == null) {
                 diss.dissID = w.newDisseminatorID();
             } else {
                 diss.dissID = dissID;
                 LOG.debug("Using new dissID from recovery context");
             }
 
             diss.dissVersionID = diss.dissID + ".0";
             // Generate the binding map ID here - ignore the value passed in
             // and set the field on both the disseminator and the binding map,
             // then set the disseminator's binding map to the one passed in.
             diss.dsBindMapID=w.newDatastreamBindingMapID();
                             bindingMap.dsBindMapID=diss.dsBindMapID;
             diss.dsBindMap=bindingMap;
             AuditRecord audit=new fedora.server.storage.types.AuditRecord();
             audit.id=w.newAuditRecordID();
             audit.processType="Fedora API-M";
             audit.action="addDisseminator";
             audit.componentID=diss.dissID;
             audit.responsibility=context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri);
             audit.date=nowUTC;
             audit.justification=logMessage;
             w.getAuditRecords().add(audit);
             w.addDisseminator(diss);
             w.commit("Added a new disseminator");
             return diss.dissID;
         } finally {
             finishModification(w, "addDisseminator");
         }
     }
 
     public Date modifyDatastreamByReference(Context context, 
                                             String pid,
                                             String datastreamId, 
                                             String[] altIDs,
                                             String dsLabel, 
                                             String mimeType,
                                             String formatURI,
                                             String dsLocation, 
                                             String checksumType,
                                             String checksum, 
                                             String logMessage,
                                             boolean force)
             throws ServerException {
 
          // check for valid xml name for datastream ID   
          if ( datastreamId!=null) {      
              if(!XMLUtils.isWellFormedXMLName(datastreamId)) {   
                  throw new InvalidXMLNameException("Invalid syntax for "
                          + "datastream ID. The datastream ID of \""
                          +datastreamId+"\" is not a valid XML Name");      
              }   
          }
 
         if (datastreamId.equals("AUDIT") || datastreamId.equals("FEDORA-AUDITTRAIL")) {
             throw new GeneralException("Modification of the system-controlled AUDIT"
                 + " datastream is not permitted.");
         }
 
         DOWriter w = null;
         try {
             LOG.info("Entered modifyDatastreamByReference");
 
             // FIXME: enforceModifyDatastreamByReference expects a parameter 
             //        of dsState, we no longer have. I'm passing in a value 
             //        of "" but this could cause a problem.
             m_fedoraXACMLModule.enforceModifyDatastreamByReference(context, pid, datastreamId, altIDs, mimeType, formatURI, dsLocation, checksumType, checksum);
 
             checkDatastreamLabel(dsLabel);
             w=m_manager.getWriter(Server.USE_DEFINITIVE_STORE, context, pid);
             fedora.server.storage.types.Datastream orig=w.GetDatastream(datastreamId, null);
             Date nowUTC; // variable for ds modified date
 
             // some forbidden scenarios...
             if (orig.DSControlGrp.equals("X")) {
                 throw new GeneralException("Inline XML datastreams must be modified by value, not by reference.");
             }
             if (orig.DSState.equals("D")) {
                 throw new GeneralException("Changing attributes on deleted datastreams is forbidden.");
             }
             
             // A NULL INPUT PARM MEANS NO CHANGE TO DS ATTRIBUTE...
             // if input parms are null, the ds attribute should not be changed,
             // so set the parm values to the existing values in the datastream.
             if (dsLabel == null) dsLabel = orig.DSLabel;
             if (mimeType == null) mimeType = orig.DSMIME;
             if (formatURI == null) formatURI = orig.DSFormatURI;
             if (altIDs == null) altIDs = orig.DatastreamAltIDs;
             
             // In cases where an empty attribute value is not allowed, then
             // NULL or EMPTY PARM means no change to ds attribute...
             if (dsLocation==null || dsLocation.equals("")) {
                 if (orig.DSControlGrp.equals("M")) {
                     // if managed content location is unspecified, 
                     // cause a copy of the prior content to be made at commit-time
                     dsLocation="copy://" + orig.DSLocation;
                 } else {
                     dsLocation=orig.DSLocation;
                 }
             } else {
                 ValidationUtility.validateURL(dsLocation, false);
             }
             
             // if "force" is false and the mime type changed, validate the
             // original datastream with respect to any disseminators it is
             // involved in, and keep a record of that information for later
             // (so we can determine whether the mime type change would cause
             // data contract invalidation)
             Map oldValidationReports = null;
             if ( !mimeType.equals(orig.DSMIME) && !force) {
                 oldValidationReports = getAllBindingMapValidationReports(
                                            context, w, datastreamId);
             }
 
             // instantiate the right class of datastream
             // (inline xml "X" datastreams have already been rejected)
             Datastream newds;
             if (orig.DSControlGrp.equals("M")) {
                 newds=new DatastreamManagedContent();
             } else {
                 newds=new DatastreamReferencedContent();
             }
             // update ds attributes that are common to all versions...
             // first, those that cannot be changed by client...
             newds.DatastreamID=orig.DatastreamID;
             newds.DSControlGrp=orig.DSControlGrp;
             newds.DSInfoType=orig.DSInfoType;
             // next, those that can be changed by client...
             newds.DSState = orig.DSState;
             newds.DSVersionable=orig.DSVersionable;
                      
             // update ds version-level attributes, and
             // make sure ds gets a new version id
             newds.DSVersionID=w.newDatastreamID(datastreamId);
             newds.DSLabel=dsLabel;
             newds.DSMIME = mimeType;
             newds.DSFormatURI=formatURI;
             newds.DatastreamAltIDs = altIDs;
             nowUTC = Server.getCurrentDate(context);
             newds.DSCreateDT=nowUTC;
             //newds.DSSize will be computed later
             if (dsLocation != null) {
                 ValidationUtility.validateURL(dsLocation, false);
             }
             newds.DSLocation=dsLocation;
             newds.DSChecksumType = checksumType;
             if (checksumType == null)
             {
                 newds.DSChecksumType = Datastream.getDefaultChecksumType();
             }
             if (checksum != null && checksumType != null)
             {
                 String check = newds.getChecksum();
                 if (!checksum.equals(check))
                 {
                     throw new ValidationException("Checksum Mismatch: " + check);
                 }
             }
             
             // next, add the datastream via the object writer
             w.addDatastream(newds, orig.DSVersionable);
 
             // add the audit record
             fedora.server.storage.types.AuditRecord audit=new fedora.server.storage.types.AuditRecord();
             audit.id=w.newAuditRecordID();
             audit.processType="Fedora API-M";
             audit.action="modifyDatastreamByReference";
             audit.componentID=newds.DatastreamID;
             audit.responsibility=context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri);
             audit.date=nowUTC;
             audit.justification=logMessage;
             w.getAuditRecords().add(audit);                        
             
             // if all went ok, check if we need to validate, then commit.
             if (oldValidationReports != null) { // mime changed and force=false
                 rejectMimeChangeIfCausedInvalidation(
                         oldValidationReports,
                         getAllBindingMapValidationReports(context, 
                                                           w, 
                                                           datastreamId));
             }
             w.commit(logMessage);
             return nowUTC;
         } finally {
             finishModification(w, "modifyDatastreamByReference");
         }
     }
 
     public Date modifyDatastreamByValue(Context context, 
                                         String pid,
                                         String datastreamId, 
                                         String[] altIDs,
                                         String dsLabel, 
                                         String mimeType,
                                         String formatURI,
                                         InputStream dsContent,
                                         String checksumType,
                                         String checksum, 
                                         String logMessage,
                                         boolean force)
             throws ServerException {
 
          // check for valid xml name for datastream ID   
          if ( datastreamId!=null) {     
              if(!XMLUtils.isWellFormedXMLName(datastreamId)) {    
                  throw new InvalidXMLNameException("Invalid syntax for "
                          + "datastream ID. The datastream ID of \""
                          +datastreamId+"\" is not a valid XML Name");    
              }      
          }
 
         if (datastreamId.equals("AUDIT") || datastreamId.equals("FEDORA-AUDITTRAIL")) {
             throw new GeneralException("Modification of the system-controlled AUDIT"
                 + " datastream is not permitted.");
         }
         DOWriter w=null;
         boolean mimeChanged = false;
         try {
             LOG.info("Entered modifyDatastreamByValue");
             
             // FIXME: enforceModifyDatastreamByReference expects a parameter 
             //        of dsState, we no longer have. I'm passing in a value 
             //        of "" but this could cause a problem.
             m_fedoraXACMLModule.enforceModifyDatastreamByValue(context, pid, datastreamId, altIDs, mimeType, formatURI, checksumType, checksum);
 
             checkDatastreamLabel(dsLabel);
             w=m_manager.getWriter(Server.USE_DEFINITIVE_STORE, context, pid);
             fedora.server.storage.types.Datastream orig=w.GetDatastream(datastreamId, null);
             
             // some forbidden scenarios...
             if (orig.DSState.equals("D")) {
                 throw new GeneralException("Changing attributes on deleted datastreams is forbidden.");
             }
             if (!orig.DSControlGrp.equals("X")) {
                 throw new GeneralException("Only content of inline XML datastreams may"
                     + " be modified by value.\n"
                     + "Use modifyDatastreamByReference instead.");
             }
             if (orig.DatastreamID.equals("METHODMAP")
                     || orig.DatastreamID.equals("DSINPUTSPEC")
                     || orig.DatastreamID.equals("WSDL")) {
                 throw new GeneralException("METHODMAP, DSINPUTSPEC, and WSDL datastreams cannot be modified.");
             }
 
             // A NULL INPUT PARM MEANS NO CHANGE TO DS ATTRIBUTE...
             // if input parms are null, the ds attribute should not be changed,
             // so set the parm values to the existing values in the datastream.
             if (dsLabel == null) dsLabel = orig.DSLabel;
             if (mimeType == null) mimeType = orig.DSMIME;
             if (formatURI == null) formatURI = orig.DSFormatURI;
             if (altIDs == null) altIDs = orig.DatastreamAltIDs;
             
             // If "force" is false and the mime type changed, validate the
             // original datastream with respect to any disseminators it is
             // involved in, and keep a record of that information for later
             // (so we can determine whether the mime type change would cause
             // data contract invalidation)
             Map oldValidationReports = null;
             if ( !mimeType.equals(orig.DSMIME) && !force) {
                 oldValidationReports = getAllBindingMapValidationReports(
                                            context, w, datastreamId);
             }
 
             DatastreamXMLMetadata newds=new DatastreamXMLMetadata();
             newds.DSMDClass=((DatastreamXMLMetadata) orig).DSMDClass;
             if (dsContent==null) {
                 // If the dsContent input stream parm is null, 
                 // that means "do not change the content".  
                 // Accordingly, here we just make a copy of the old content.
                 newds.xmlContent = ((DatastreamXMLMetadata) orig).xmlContent;
             } else {
                 // If it's not null, use it
                 newds.xmlContent = getEmbeddableXML(dsContent);
                 // If it's a RELS-EXT datastream, do validation
                 if (orig.DatastreamID.equals("RELS-EXT")){
                     validateRelsExt(pid, new ByteArrayInputStream(
                         ((DatastreamXMLMetadata) newds).xmlContent));
                 }
             }
 
             // update ds attributes that are common to all versions...
             // first, those that cannot be changed by client...
             newds.DatastreamID=orig.DatastreamID;
             newds.DSControlGrp=orig.DSControlGrp;
             newds.DSInfoType=orig.DSInfoType;
             // next, those that can be changed by client...
             newds.DSState = orig.DSState;
             newds.DSVersionable = orig.DSVersionable;
             
             // update ds version level attributes, and
             // make sure ds gets a new version id
             newds.DSVersionID=w.newDatastreamID(datastreamId);
             newds.DSLabel=dsLabel;
             newds.DatastreamAltIDs=altIDs;
             newds.DSMIME=mimeType;
             newds.DSFormatURI=formatURI;
             Date nowUTC = Server.getCurrentDate(context);
             newds.DSCreateDT=nowUTC;
             
             newds.DSChecksumType = checksumType;
             if (checksumType == null)
             {
                 newds.DSChecksumType = Datastream.getDefaultChecksumType();
             }
             if (checksum != null && checksumType != null)
             {
                 String check = newds.getChecksum();
                 if (!checksum.equals(check))
                 {
                     throw new ValidationException("Checksum Mismatch: " + check);
                 }
             }
             // next, add the datastream via the object writer
             w.addDatastream(newds, orig.DSVersionable);
                         
             // add the audit record
             fedora.server.storage.types.AuditRecord audit=new fedora.server.storage.types.AuditRecord();
             audit.id=w.newAuditRecordID();
             audit.processType="Fedora API-M";
             audit.action="modifyDatastreamByValue";
             audit.componentID=newds.DatastreamID;
             audit.responsibility=context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri);
             audit.date=nowUTC;
             audit.justification=logMessage;
             w.getAuditRecords().add(audit);
             
             // if all went ok, check if we need to validate, then commit.
             if (oldValidationReports != null) { // mime changed and force=false
                 rejectMimeChangeIfCausedInvalidation(
                         oldValidationReports,
                         getAllBindingMapValidationReports(context, 
                                                           w, 
                                                           datastreamId));
             }
             w.commit(logMessage);
             return nowUTC;
         } finally {
             finishModification(w, "modifyDatastreamByValue");
         }
     }
 
     public Date modifyDisseminator(Context context, 
                                    String pid,
                                    String disseminatorId, 
                                    String bMechPid, 
                                    String dissLabel,
                                    DSBindingMap dsBindingMap,
                                    String dissState,
                                    String logMessage,
                                    boolean force)
             throws ServerException {
         DOWriter w=null;
         DOReader r=null;
         try {
             LOG.info("Entered modifyDisseminator");
             m_fedoraXACMLModule.enforceModifyDisseminator(context, pid, disseminatorId, bMechPid, dissState);
 
             checkDisseminatorLabel(dissLabel);
             w=m_manager.getWriter(Server.USE_DEFINITIVE_STORE, context, pid);
             fedora.server.storage.types.Disseminator orig=w.GetDisseminator(disseminatorId, null);
             String oldValidationReport = null;
             if (!force) {
                 oldValidationReport = getBindingMapValidationReport(context,
                                                                     w,
                                                                     orig.bMechID);
             }
             r=m_manager.getReader(Server.USE_DEFINITIVE_STORE, context,pid);  // FIXME: Unnecessary?  Is 
                                                  // there a reason "w" isn't 
                                                  // used for the call below?
             Date[] d=r.getDisseminatorVersions(disseminatorId);
 
             Disseminator newdiss=new Disseminator();
             // use original diss values for attributes that can't be changed by client
             newdiss.dissID=orig.dissID;
             newdiss.bDefID=orig.bDefID;
             newdiss.parentPID=orig.parentPID;
             
             // make sure disseminator has a new version id
             newdiss.dissVersionID=w.newDisseminatorID(disseminatorId);
             // make sure disseminator has a new version date
             Date nowUTC = Server.getCurrentDate(context);
             newdiss.dissCreateDT=nowUTC;
            
             // for testing; null indicates a new (uninitialized) instance
             // of dsBindingMap was passed in which is what you get if
             // you pass null in for dsBindingMap using MangementConsole
             if (dsBindingMap.dsBindMapID!=null) {
               newdiss.dsBindMap=dsBindingMap;
             } else {
               newdiss.dsBindMap=orig.dsBindMap;
             }
             // make sure dsBindMapID has a different id
             newdiss.dsBindMapID=w.newDatastreamBindingMapID();
             newdiss.dsBindMap.dsBindMapID=w.newDatastreamBindingMapID();
 
             
             // NULL INPUT PARMS MEANS NO CHANGE in these cases:
             // set any diss attributes whose input parms value
             // is NULL to the original attribute value on the disseminator
             if (dissLabel==null) {
             //if (dissLabel==null || dissLabel.equals("")) {
               newdiss.dissLabel=orig.dissLabel;
             } else {
               newdiss.dissLabel=dissLabel;
             }
             
             // NULL OR "" INPUT PARM MEANS NO CHANGE:
             // for diss attributes whose values MUST NOT be empty,
             // either NULL or "" on the input parm indicates no change 
             // (keep original value)
             if (bMechPid==null || bMechPid.equals("")) {
               newdiss.bMechID=orig.bMechID;
             } else {
               newdiss.bMechID=bMechPid;
             }
             if (dissState==null || dissState.equals("")) {
               // If reference unspecified leave state unchanged
               newdiss.dissState=orig.dissState;
             } else {
               // Check that supplied value for state is one of the allowable values
               if (!dissState.equals("A") && !dissState.equals("D") && !dissState.equals("I")) {
                   throw new InvalidStateException("The disseminator state of \"" + dissState
                           + "\" is invalid. The allowed values for state are: "
                           + " A (active), D (deleted), and I (inactive).");
               }                        
               newdiss.dissState=dissState;
             }
             
             // just add the disseminator
             w.addDisseminator(newdiss);
             if (!orig.dissState.equals(newdiss.dissState)) {
                 w.setDisseminatorState(disseminatorId, newdiss.dissState); }            
             // add the audit record
             fedora.server.storage.types.AuditRecord audit=new fedora.server.storage.types.AuditRecord();
             audit.id=w.newAuditRecordID();
             audit.processType="Fedora API-M";
             audit.action="modifyDisseminator";
             audit.componentID=newdiss.dissID;
             audit.responsibility=context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri);
             audit.date=nowUTC;
             audit.justification=logMessage;
             w.getAuditRecords().add(audit);
             // if all went ok, check if we need to validate, then commit.
             if (!force && oldValidationReport == null) {
                 String cause = getBindingMapValidationReport(context,
                                                              w,
                                                              newdiss.bMechID);
                 if (cause != null) {
                     throw new GeneralException("That change would invalidate "
                             + "the disseminator: " + cause);
                 }
             }
             w.commit(logMessage);
             return nowUTC;
         } finally {
             finishModification(w, "modifyDisseminator");
         }
     }
 
 
     public Date[] purgeDatastream(Context context, 
                                   String pid,
                                   String datastreamID, 
                                   Date startDT,
                                   Date endDT,
                                   String logMessage,
                                   boolean force)
             throws ServerException {
         if (force) {
             throw new GeneralException("Forced datastream removal is not "
                     + "yet supported.");
         }
         DOWriter w=null;
         try {
             LOG.info("Entered purgeDatastream");
             
             m_fedoraXACMLModule.enforcePurgeDatastream(context, pid, datastreamID, endDT);
 
             w=m_manager.getWriter(Server.USE_DEFINITIVE_STORE, context, pid);
             Date[] deletedDates=w.removeDatastream(datastreamID, startDT, endDT);
             // check if there's at least one version with this id...
             if (w.GetDatastream(datastreamID, null)==null) {
                 // if deleting would result in no versions remaining,
                 // only continue if there are no disseminators that use
                 // this datastream.
                 // to do this, we must look through all versions of every
                 // disseminator, regardless of state
                 SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                 ArrayList<String> usedList=new ArrayList<String>();
                 if (datastreamID.equals("DC")) {
                     usedList.add("The default disseminator");
                 }
                 // ...for each disseminator
                 Disseminator[] disses=w.GetDisseminators(null, null);
                 for (int i=0; i<disses.length; i++) {
                     Date[] dates=w.getDisseminatorVersions(disses[i].dissID);
                     // ...for each of its versions
                     for (int j=0; j<dates.length; j++) {
                         Disseminator diss=w.GetDisseminator(disses[i].dissID, dates[j]);
                         DSBinding[] dsBindings=diss.dsBindMap.dsBindings;
                         // ...for each of its datastream bindings
                         for (int k=0; k<dsBindings.length; k++) {
                             // ...is the datastream id referenced?
                             if (dsBindings[k].datastreamID.equals(datastreamID)) {
                                 usedList.add(diss.dissID + " ("
                                         + formatter.format(diss.dissCreateDT)
                                         + ")");
                             }
                         }
                     }
                 }
                 if (usedList.size()>0) {
                     StringBuffer msg=new StringBuffer();
                     msg.append("Cannot purge entire datastream because it\n");
                     msg.append("is used by the following disseminators:");
                     for (int i=0; i<usedList.size(); i++) {
                         msg.append("\n - " + (String) usedList.get(i));
                     }
                     throw new GeneralException(msg.toString());
                 }
             }
             // add an explanation of what happened to the user-supplied message.
             if (logMessage == null) {
                 logMessage = "";
             } else {
                 logMessage += " . . . ";
             }
             logMessage += getPurgeLogMessage("datastream", 
                                              datastreamID,
                                              startDT, 
                                              endDT, 
                                              deletedDates);
             Date nowUTC = Server.getCurrentDate(context);
             fedora.server.storage.types.AuditRecord audit=new fedora.server.storage.types.AuditRecord();
             audit.id=w.newAuditRecordID();
             audit.processType="Fedora API-M";
             audit.action="purgeDatastream";
             audit.componentID=datastreamID;
             audit.responsibility=context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri);
             audit.date=nowUTC;
             audit.justification=logMessage;
             // Normally we associate an audit record with a specific version
             // of a datastream, but in this case we are talking about a range
             // of versions.  So we'll just add it to the object, but not associate
             // it with anything.
             w.getAuditRecords().add(audit);
             // It looks like all went ok, so commit
             w.commit(logMessage);
             // ... then give the response
             return deletedDates;
         } finally {
             finishModification(w, "purgeDatastream");
         }
     }
 
     private String getPurgeLogMessage(String kindaThing, 
                                       String id, 
                                       Date start,
                                       Date end, 
                                       Date[] deletedDates) {
         SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
         StringBuffer buf=new StringBuffer();
         buf.append("Purged ");
         buf.append(kindaThing);
         buf.append(" (ID=");
         buf.append(id);
         buf.append("), versions ranging from ");
         if (start==null) {
             buf.append("the beginning of time");
         } else {
             buf.append(formatter.format(start));
         }
         buf.append(" to ");
         if (end==null) {
             buf.append("the end of time");
         } else {
             buf.append(formatter.format(end));
         }
         buf.append(".  This resulted in the permanent removal of ");
         buf.append(deletedDates.length + " ");
         buf.append(kindaThing);
         buf.append(" version(s) (");
         for (int i=0; i<deletedDates.length; i++) {
             if (i>0) {
                 buf.append(", ");
             }
             buf.append(formatter.format(deletedDates[i]));
         }
         buf.append(") and all associated audit records.");
         return buf.toString();
     }
 
     public Datastream getDatastream(Context context, 
                                     String pid,
                                     String datastreamID, 
                                     Date asOfDateTime)
             throws ServerException {
         try {
             LOG.info("Entered getDatastream");
             
             m_fedoraXACMLModule.enforceGetDatastream(context, pid, datastreamID, asOfDateTime);
             
             DOReader r=m_manager.getReader(Server.GLOBAL_CHOICE, context, pid);
             return r.GetDatastream(datastreamID, asOfDateTime);
         } finally {
             LOG.info("Exiting getDatastream");
         }
     }
 
     public Datastream[] getDatastreams(Context context, 
                                        String pid,
                                        Date asOfDateTime, 
                                        String state)
             throws ServerException {
         try {
             LOG.info("Entered getDatastreams");
 
             m_fedoraXACMLModule.enforceGetDatastreams(context, pid, asOfDateTime, state);
            
             DOReader r=m_manager.getReader(Server.GLOBAL_CHOICE, context, pid);
             return r.GetDatastreams(asOfDateTime, state);
         } finally {
             LOG.info("Exiting getDatastreams");
         }
     }
 
     public Datastream[] getDatastreamHistory(Context context, 
                                              String pid, 
                                              String datastreamID)
             throws ServerException {
         try {
             LOG.info("Entered getDatastreamHistory");
             
             m_fedoraXACMLModule.enforceGetDatastreamHistory(context, pid, datastreamID);
 
             DOReader r=m_manager.getReader(Server.GLOBAL_CHOICE, context, pid);
             Date[] versionDates=r.getDatastreamVersions(datastreamID);
             Datastream[] versions=new Datastream[versionDates.length];
             for (int i=0; i<versionDates.length; i++) {
                 versions[i]=r.GetDatastream(datastreamID, versionDates[i]);
             }
             // sort, ascending
             Arrays.sort(versions, new DatastreamDateComparator());
             // reverse it (make it descend, so most recent date is element 0)
             Datastream[] out=new Datastream[versions.length];
             for (int i=0; i<versions.length; i++) {
                 out[i]=versions[versions.length-1-i];
             }
             return out;
         } finally {
             LOG.info("Exiting getDatastreamHistory");
         }
     }
 
     public class DatastreamDateComparator
             implements Comparator {
 
         public int compare(Object o1, Object o2) {
             long ms1=((Datastream) o1).DSCreateDT.getTime();
             long ms2=((Datastream) o1).DSCreateDT.getTime();
             if (ms1<ms2) return -1;
             if (ms1>ms2) return 1;
             return 0;
         }
     }
 
     public Date[] purgeDisseminator(Context context, 
                                     String pid,
                                     String disseminatorID, 
                                     Date endDT,
                                     String logMessage)
             throws ServerException {
         DOWriter w=null;
         try {
             LOG.info("Entered purgeDisseminator");
             
             m_fedoraXACMLModule.enforcePurgeDisseminator(context, pid, disseminatorID, endDT);
             
             w=m_manager.getWriter(Server.GLOBAL_CHOICE, context, pid);
             Date start=null;
             Date[] deletedDates=w.removeDisseminator(disseminatorID, start, endDT);
             // add an explanation of what happened to the user-supplied message.
             if (logMessage == null) {
                 logMessage = "";
             } else {
                 logMessage += " . . . ";
             }
             logMessage += getPurgeLogMessage("disseminator", 
                                              disseminatorID,
                                              start, 
                                              endDT, 
                                              deletedDates);
             Date nowUTC = Server.getCurrentDate(context);
             fedora.server.storage.types.AuditRecord audit=new fedora.server.storage.types.AuditRecord();
             audit.id=w.newAuditRecordID();
             audit.processType="Fedora API-M";
             audit.action="purgeDisseminator";
             audit.componentID=disseminatorID;
             audit.responsibility=context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri);
             audit.date=nowUTC;
             audit.justification=logMessage;
             // Normally we associate an audit record with a specific version
             // of a disseminator, but in this case we are talking about a range
             // of versions.  So we'll just add it to the object, but not associate
             // it with anything.
             w.getAuditRecords().add(audit);
             // It looks like all went ok, so commit
             // ... then give the response
             w.commit(logMessage);
             return deletedDates;
         } finally {
             finishModification(w, "purgeDisseminator");
         }
     }
 
     public Disseminator getDisseminator(Context context, 
                                         String pid,
                                         String disseminatorId, 
                                         Date asOfDateTime)
             throws ServerException {
         try {
             LOG.info("Entered getDisseminator");
             
             m_fedoraXACMLModule.enforceGetDisseminator(context, pid, disseminatorId, asOfDateTime);
             
             DOReader r=m_manager.getReader(Server.GLOBAL_CHOICE, context, pid);
             return r.GetDisseminator(disseminatorId, asOfDateTime);
         } finally {
             LOG.info("Exiting getDisseminator");
         }
     }
 
     public Disseminator[] getDisseminators(Context context, 
                                            String pid,
                                            Date asOfDateTime, 
                                            String dissState)
             throws ServerException {
         try {
             LOG.info("Entered getDisseminators");
             m_fedoraXACMLModule.enforceGetDisseminators(context, pid, asOfDateTime, dissState);
             DOReader r=m_manager.getReader(Server.GLOBAL_CHOICE, context, pid);
             return r.GetDisseminators(asOfDateTime, dissState);
         } finally {
             LOG.info("Exiting getDisseminators");
         }
     }
 
     public Disseminator[] getDisseminatorHistory(Context context, 
                                                  String pid, 
                                                  String disseminatorID)
             throws ServerException {
         try {
             LOG.info("Entered getDisseminatorHistory");
 
             m_fedoraXACMLModule.enforceGetDisseminatorHistory(context, 
                     pid, disseminatorID); 
 
             DOReader r=m_manager.getReader(Server.USE_DEFINITIVE_STORE, context, pid);
             Date[] versionDates=r.getDisseminatorVersions(disseminatorID);
             Disseminator[] versions=new Disseminator[versionDates.length];
             for (int i=0; i<versionDates.length; i++) {
                 versions[i]=r.GetDisseminator(disseminatorID, versionDates[i]);
             }
             // sort, ascending
             Arrays.sort(versions, new DisseminatorDateComparator());
             // reverse it (make it descend, so most recent date is element 0)
             Disseminator[] out=new Disseminator[versions.length];
             for (int i=0; i<versions.length; i++) {
                 out[i]=versions[versions.length-1-i];
             }
             return out;
         } finally {
             LOG.info("Exiting getDisseminatorHistory");
         }
     }
 
     public String[] getNextPID(Context context, 
                                int numPIDs,
                                String namespace)
             throws ServerException {
         try {
             LOG.info("Entered getNextPID");           
             m_fedoraXACMLModule.enforceGetNextPid(context, namespace, numPIDs);
 
             String[] pidList = null;
 
             // If the pidList is in the RecoveryContext, just reserve them
             // rather than generating new ones.
             if (context instanceof RecoveryContext) {
                 RecoveryContext rContext = (RecoveryContext) context;
                 pidList = rContext.getRecoveryValues(Constants.RECOVERY.PID_LIST.uri);
                 if (pidList != null && pidList.length > 0) {
                     LOG.debug("Reserving and returning PID_LIST "
                             + "from recovery context");
                     m_manager.reservePIDs(pidList);
                 }
             }
 
             if (pidList == null || pidList.length == 0) {
                 pidList = m_manager.getNextPID(numPIDs, namespace);
             }
 
             return pidList;
 
         } finally {
             LOG.info("Exiting getNextPID");
         }
     }
 
 
     public class DisseminatorDateComparator
             implements Comparator {
 
         public int compare(Object o1, Object o2) {
             long ms1=((Disseminator) o1).dissCreateDT.getTime();
             long ms2=((Disseminator) o2).dissCreateDT.getTime();
             if (ms1<ms2) return -1;
             if (ms1>ms2) return 1;
             return 0;
         }
     }
 
     public String putTempStream(Context context, InputStream in)
             throws StreamWriteException, AuthzException {
         m_fedoraXACMLModule.enforceUpload(context);
         // first clean up after old stuff
         long minStartTime=System.currentTimeMillis()-(60*1000*m_uploadStorageMinutes);
                 ArrayList<String> removeList=new ArrayList<String>();
         Iterator iter=m_uploadStartTime.keySet().iterator();
         while (iter.hasNext()) {
             String id=(String) iter.next();
             Long startTime=(Long) m_uploadStartTime.get(id);
             if (startTime.longValue()<minStartTime) {
                 // remove from filesystem and hash
                 File f=new File(m_tempDir, id);
                 if (f.delete()) {
                     LOG.info("Removed uploaded file '" + id + "' because it expired.");
                 } else {
                     LOG.warn("Could not remove expired uploaded file '" + id
                             + "'.  Check existence/permissions in management/upload/ directory.");
                 }
                                 removeList.add(id);
             }
         }
                 for (int i=0; i<removeList.size(); i++) {
                     String id=(String) removeList.get(i);
                     m_uploadStartTime.remove(id);
                 }
         // then generate an id
         int id=getNextTempId(context);
         // and attempt to save the stream
         File outFile = new File(m_tempDir, "" + id);
         FileOutputStream out = null;
         try {
             out = new FileOutputStream(outFile);
             StreamUtility.pipeStream(in, out, 32768);
         } catch (Exception e) {
             if (out != null) {
                 try { out.close(); } catch (Exception ex) { }
                 outFile.delete();
             }
             throw new StreamWriteException(e.getMessage());
         }
         // if we got this far w/o an exception, add to hash with current time
         // and return the identifier-that-looks-like-a-url
         long now=System.currentTimeMillis();
         m_uploadStartTime.put("" + id, new Long(now));
         return "uploaded://" + id;
     }
 
     private synchronized int getNextTempId(Context context) {
 
         int recoveryId = -1;
 
         // If the RecoveryContext has an uploaded://n url, use n.
         if (context instanceof RecoveryContext) {
             RecoveryContext rContext = (RecoveryContext) context;
             String uploadURL = rContext.getRecoveryValue(
                     Constants.RECOVERY.UPLOAD_ID.uri);
             if (uploadURL != null) {
                 try {
                     String n = uploadURL.substring(11);
                     recoveryId = Integer.parseInt(n);
                 } catch (Exception e) {
                     throw new IllegalArgumentException("Unable to parse UPLOAD_ID "
                             + "from recovery context: '" + uploadURL + "'");
                 }
             }
         }
         if (recoveryId == -1) {
             m_lastId++;
         } else {
             m_lastId = recoveryId;
         }
         return m_lastId;
     }
 
     public InputStream getTempStream(String id)
             throws StreamReadException {
         // it should come in starting with "uploaded://"
         if (id.startsWith("uploaded://") || id.length()<12) {
             String internalId=id.substring(11);
             if (m_uploadStartTime.get(internalId)!=null) {
                 // found... return inputstream
                 try {
                     return new FileInputStream(new File(m_tempDir, internalId));
                 } catch (Exception e) {
                     throw new StreamReadException(e.getMessage());
                 }
             } else {
                 throw new StreamReadException("Id specified, '" + id + "', does not match an existing file.");
             }
         } else {
             throw new StreamReadException("Invalid id syntax '" + id + "'.");
         }
     }
 
     public Date setDatastreamState(Context context, 
                                    String pid, 
                                    String datastreamID, 
                                    String dsState, 
                                    String logMessage)
             throws ServerException {
       DOWriter w=null;
       try {
           LOG.info("Entered setDatastreamState");
           
           m_fedoraXACMLModule.enforceSetDatastreamState(context, pid, datastreamID, dsState); 
           
           w=m_manager.getWriter(Server.USE_DEFINITIVE_STORE, context, pid);
           if (!dsState.equals("A") && !dsState.equals("D") && !dsState.equals("I")) {
               throw new InvalidStateException("The datastream state of \"" + dsState
                       + "\" is invalid. The allowed values for state are: "
                       + " A (active), D (deleted), and I (inactive).");
           }          
           fedora.server.storage.types.Datastream ds=w.GetDatastream(datastreamID, null);
           w.setDatastreamState(datastreamID, dsState);
 
           // add the audit record
           fedora.server.storage.types.AuditRecord audit=new fedora.server.storage.types.AuditRecord();
           audit.id=w.newAuditRecordID();
           audit.processType="Fedora API-M";
           audit.action="setDatastreamState";
           audit.componentID=datastreamID;
           audit.responsibility=context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri);
           Date nowUTC = Server.getCurrentDate(context);
           audit.date=nowUTC;
           audit.justification=logMessage;
           w.getAuditRecords().add(audit);
 
           // if all went ok, commit
           w.commit(logMessage);
           return nowUTC;
       } finally {
           finishModification(w, "setDatastreamState");
         }
     }
 
     public Date setDatastreamVersionable(Context context, String pid,     
             String datastreamID, boolean versionable, String logMessage)      
             throws ServerException {
        DOWriter w=null;    
        try {   
            LOG.info("Entered setDatastreamVersionable");      
    
            m_fedoraXACMLModule.enforceSetDatastreamVersionable(context, pid, datastreamID, versionable);   
    
            w=m_manager.getWriter(Server.USE_DEFINITIVE_STORE, context, pid);   
            fedora.server.storage.types.Datastream ds=w.GetDatastream(datastreamID, null);      
            w.setDatastreamVersionable(datastreamID, versionable);      
    
            // add the audit record     
            fedora.server.storage.types.AuditRecord audit=new fedora.server.storage.types.AuditRecord();    
            audit.id=w.newAuditRecordID();      
            audit.processType="Fedora API-M";   
            audit.action="setDatastreamVersionable";    
            audit.componentID=datastreamID;     
            audit.responsibility=context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri);   
            Date nowUTC = Server.getCurrentDate(context);
            audit.date=nowUTC;      
            audit.justification=logMessage;     
            w.getAuditRecords().add(audit);     
    
            // if all went ok, commit   
            w.commit(logMessage);   
            return nowUTC;      
        } finally {   
            finishModification(w, "setDatastreamVersionable"); 
        }   
     }
         
     public String compareDatastreamChecksum(Context context, String pid,     
             String datastreamID, Date versionDate)      
             throws ServerException {   
         DOReader r = null;    
         try {   
             LOG.info("Entered compareDatastreamChecksum");      
    
             m_fedoraXACMLModule.enforceCompareDatastreamChecksum(context, pid, datastreamID, versionDate);   
    
             LOG.debug("Getting Reader");
             r=m_manager.getReader(Server.USE_DEFINITIVE_STORE, context, pid);   
             LOG.debug("Getting datastream:" + datastreamID + "date: "+ versionDate);
             Datastream ds = r.GetDatastream(datastreamID, versionDate);      
             LOG.debug("Got Datastream, comparing checksum");
             boolean check = ds.compareChecksum();      
             LOG.debug("compared checksum = " + check);
        
             return check ? ds.getChecksum() : "Checksum validation error";      
         } finally {   
             LOG.info("Exiting compareDatastreamChecksum");      
         }   
     }
 
     public Date setDisseminatorState(Context context, String pid, 
             String disseminatorID, String dissState, String logMessage)
             throws ServerException {
       DOWriter w=null;
       try {
           LOG.info("Entered setDisseminatorState");
           m_fedoraXACMLModule.enforceSetDisseminatorState(context, pid, disseminatorID, dissState);  
 
           w=m_manager.getWriter(Server.USE_DEFINITIVE_STORE, context, pid);
           if (!dissState.equals("A") && !dissState.equals("D") && !dissState.equals("I")) {
               throw new InvalidStateException("The disseminator state of \"" + dissState
                       + "\" is invalid. The allowed values for state are: "
                       + " A (active), D (deleted), and I (inactive).");
           }          
           fedora.server.storage.types.Disseminator diss=w.GetDisseminator(disseminatorID, null);
           w.setDisseminatorState(disseminatorID, dissState);
 
           // add the audit record
           fedora.server.storage.types.AuditRecord audit=new fedora.server.storage.types.AuditRecord();
           audit.id=w.newAuditRecordID();
           audit.processType="Fedora API-M";
           audit.action="setDisseminatorState";
           audit.componentID=disseminatorID;
           audit.responsibility=context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri);
           Date nowUTC = Server.getCurrentDate(context);
           audit.date=nowUTC;
           audit.justification=logMessage;
           w.getAuditRecords().add(audit);
 
           // if all went ok, commit
           w.commit(logMessage);
           return nowUTC;
       } finally {
           finishModification(w, "setDisseminatorState");
       }
    }
 
     /**
      * Get a byte array containing an xml chunk that is safe to embed in 
      * another UTF-8 xml document.
      * <p>
      * This will ensure that the xml is:
      * <ul>
      *   <li> well-formed. If not, an exception will be raised.</li>
      *   <li> encoded in UTF-8. It will be converted otherwise.</li>
      *   <li> devoid of processing instructions. These will be stripped if present.</li>
      *   <li> devoid of DOCTYPE declarations. These will be stripped if present.</li>
      *   <li> devoid of internal entity references.  These will be expanded if present.</li>
      * </ul>
      * </p>
      */
     private byte[] getEmbeddableXML(InputStream in) throws GeneralException {
         // parse with xerces and re-serialize the fixed xml to a byte array
         try {
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputFormat fmt = new OutputFormat("XML", "UTF-8", true);
             fmt.setIndent(2);
             fmt.setLineWidth(120);
             fmt.setPreserveSpace(false);
             fmt.setOmitXMLDeclaration(true);
             fmt.setOmitDocumentType(true);
             XMLSerializer ser = new XMLSerializer(out, fmt);
             DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             factory.setNamespaceAware(true);
             DocumentBuilder builder = factory.newDocumentBuilder();
             Document doc = builder.parse(in);
             ser.serialize(doc);
             return out.toByteArray();
         } catch (Exception e) {
             String message = e.getMessage();
             if (message == null) message = "";
             throw new GeneralException("XML was not well-formed. " + message, e);
         }
     }
 
     /**
      * Get a string indicating whether the associated binding map (or an empty
      * binding map, if none is found) is valid or invalid according to the 
      * data contract defined by the indicated behavior mechanism.
      *
      * Returns null if valid, otherwise returns a String explaining why not.
      *
      * This assumes the indicated bMech actually exists, and the binding
      * map, if it exists and specifies any datastreams, refers to existing 
      * datastreams within the object.  If these conditions are not met, an 
      * exception is thrown.
      */
     private String getBindingMapValidationReport(Context context,
                                                  DOReader doReader,
                                                  String bMechPID)
             throws ServerException {
 
         // find the associated datastream binding map, else use an empty one.
         DSBindingMapAugmented augMap = new DSBindingMapAugmented();
         DSBindingMapAugmented[] augMaps = doReader.GetDSBindingMaps(null);
         for (int i = 0; i < augMaps.length; i++) {
             if (augMaps[i].dsBindMechanismPID.equals(bMechPID)) {
                 augMap = augMaps[i];
             }
         }
 
         // load the bmech, then validate the bindings
         BMechReader mReader = m_manager.getBMechReader(Server.USE_CACHE, context, bMechPID);
         BMechDSBindSpec spec = mReader.getServiceDSInputSpec(null);
         return spec.validate(augMap.dsBindingsAugmented);
     }
 
     /**
      * Get a combined report indicating failure or success of data contract 
      * validation for every disseminator in the given object that the indicated 
      * datastream is bound to.
      *
      * The returned map's keys will be Disseminator objects.
      * The values will be null in the case of successful validation,
      * or Strings (explaining why) in the case of failure.
      *
      * This assumes that all bMechs specified in the binding maps of the
      * disseminators that use the indicated datastream actually exist, and 
      * the binding map, if it exists and specifies any datastreams, refers to 
      * existing datastreams within the object.  If these conditions are not 
      * met, an exception is thrown.
      */
     private Map getAllBindingMapValidationReports(Context context,
                                                   DOReader doReader,
                                                   String dsID)
             throws ServerException {
         HashMap<Disseminator, String> map = new HashMap<Disseminator, String>();
         // for all disseminators in the object,
         Disseminator[] disses = doReader.GetDisseminators(null, null);
         for (int i = 0; i < disses.length; i++) {
             DSBinding[] bindings = disses[i].dsBindMap.dsBindings; 
             boolean isUsed = false;
             // check each binding to see if it's the indicated datastream
             for (int j = 0; j < bindings.length && !isUsed; j++) {
                 if (bindings[j].datastreamID.equals(dsID)) isUsed = true;
             }
             if (isUsed) {
                 // if it's used, add it's validation information to the map.
                 map.put(disses[i], 
                         getBindingMapValidationReport(context,
                                                       doReader,
                                                       disses[i].bMechID));
             }
         }
         return map;
     }
 
     private Map getNewFailedValidationReports(Map oldReport,
                                               Map newReport) {
         HashMap<Disseminator, String> map = new HashMap<Disseminator, String>();
         Iterator newIter = newReport.keySet().iterator();
         // For each disseminator in the new report:
         while (newIter.hasNext()) {
             Disseminator diss = (Disseminator) newIter.next();
             String failedMessage = (String) newReport.get(diss);
             // Did it fail in the new report . . .
             if (failedMessage != null) {
                 // . . . but not in the old one?
                 if (oldReport.get(diss) == null) {
                     map.put(diss, failedMessage);
                 }
             }
         }
         return map;
     }
 
     private void rejectMimeChangeIfCausedInvalidation(Map oldReports,
                                                       Map newReports)
             throws ServerException {
         Map causedFailures = getNewFailedValidationReports(oldReports, 
                                                            newReports);
         int numFailures = causedFailures.keySet().size();
         if (numFailures > 0) {
             StringBuffer buf = new StringBuffer();
             buf.append("This mime type change would invalidate " 
                     + numFailures + " disseminator(s):");
             Iterator iter = causedFailures.keySet().iterator();
             while (iter.hasNext()) {
                 Disseminator diss = (Disseminator) iter.next();
                 String reason = (String) causedFailures.get(diss);
                 buf.append("\n" + diss.dissID + ": " + reason);
             }
             throw new GeneralException(buf.toString());
         }
     }
     
     private void validateRelsExt(String pid, InputStream relsext)
         throws ServerException {            
         // RELATIONSHIP METADATA VALIDATION:
         try {
             RelsExtValidator deser=new RelsExtValidator("UTF-8", false);
             if (relsext!=null) {
                 LOG.debug("API-M: Validating RELS-EXT datastream...");
                 deser.deserialize(relsext, "info:fedora/" + pid);
                 LOG.debug("API-M: RELS-EXT datastream passed validation.");
             }
         } catch (Exception e) {
 
             String message = e.getMessage();
             if (message == null) message = e.getClass().getName();
             throw new GeneralException("RELS-EXT validation failed: " + message);
         }
     }
 
     private void checkDatastreamID(String id) throws ValidationException {
         checkString(id, "Datastream id", 
                     ValidationConstants.DATASTREAM_ID_MAXLEN,
                     ValidationConstants.DATASTREAM_ID_BADCHARS);
     }
     
     private void checkDatastreamLabel(String label) throws ValidationException {
         checkString(label, "Datastream label", 
                     ValidationConstants.DATASTREAM_LABEL_MAXLEN, null);
     }
     
     private void checkDisseminatorID(String id) throws ValidationException {
         checkString(id, "Disseminator id", 
                     ValidationConstants.DISSEMINATOR_ID_MAXLEN,
                     ValidationConstants.DISSEMINATOR_ID_BADCHARS);
     }
     
     private void checkDisseminatorLabel(String label) throws ValidationException {
         checkString(label, "Disseminator label", 
                     ValidationConstants.DISSEMINATOR_LABEL_MAXLEN, null);
     }
     
     private void checkObjectLabel(String label) throws ValidationException {
         checkString(label, "Object label", 
                     ValidationConstants.OBJECT_LABEL_MAXLEN, null);
     }
 
     private void checkString(String string,
                              String kind,
                              int maxLen,
                              char[] badChars) throws ValidationException {
         if (string != null) {
             if (string.length() > maxLen) {
                 throw new ValidationException(kind + " is too long. Maximum "
                         + "length is " + maxLen + " characters.");
             } else if (badChars != null) {
                 for (int i = 0; i < badChars.length; i++) {
                     char c = badChars[i];
                     if (string.indexOf(c) != -1) {
                         throw new ValidationException(kind + " contains a "
                                 + "'" + c + "', but that character is not "
                                 + "allowed.");
                     }
                 }
             }
         }
     }
 
     public boolean adminPing(Context context)
         throws ServerException {    
         m_fedoraXACMLModule.enforceAdminPing(context);  
         return true;
     }
 
 
 }
