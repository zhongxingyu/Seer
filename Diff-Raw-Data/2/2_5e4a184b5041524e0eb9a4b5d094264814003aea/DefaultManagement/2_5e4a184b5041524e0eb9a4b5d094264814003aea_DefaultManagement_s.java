 package fedora.server.management;
 
 import java.io.*;
 import java.sql.*;
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
 
 import fedora.server.*;
 import fedora.server.errors.*;
 import fedora.server.security.*;
 import fedora.server.storage.*;
 import fedora.server.storage.types.*;
 import fedora.server.utilities.*;
 
 /**
  * Implements API-M without regard to the transport/messaging protocol.
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
  * <p>The entire file consists of original code.  Copyright &copy; 2002-2004 by The
  * Rector and Visitors of the University of Virginia and Cornell University.
  * All rights reserved.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * @author cwilper@cs.cornell.edu
  * @version $Id$
  */
 public class DefaultManagement
         extends Module implements Management {
 
     private DOManager m_manager;
     private IPRestriction m_ipRestriction;
     private String m_fedoraServerHost;
     private String m_fedoraServerPort;
     private int m_uploadStorageMinutes;
     private int m_lastId;
     private File m_tempDir;
     private Hashtable m_uploadStartTime;
     private ExternalContentManager m_contentManager;
 
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
         String allowHosts=getParameter("allowHosts");
         String denyHosts=getParameter("denyHosts");
         try {
             m_ipRestriction=new IPRestriction(allowHosts, denyHosts);
         } catch (ServerException se) {
             throw new ModuleInitializationException("Error setting IP restriction "
                     + "for Access subsystem: " + se.getClass().getName() + ": "
                     + se.getMessage(), getRole());
         }
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
 			m_uploadStartTime=new Hashtable();
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
         m_fedoraServerHost=getServer().getParameter("fedoraServerHost");
         m_fedoraServerPort=getServer().getParameter("fedoraServerPort");
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
             getServer().logFinest("Entered DefaultManagement.ingestObject");
             m_ipRestriction.enforce(context);
             w=m_manager.getIngestWriter(context, serialization, format, encoding, newPid);
             String pid=w.GetObjectPID();
             w.commit(logMessage);
             return pid;
         } finally {
             if (w != null) {
                 m_manager.releaseWriter(w);
             }
             Runtime r=Runtime.getRuntime();
             getServer().logFinest("Memory: " + r.freeMemory() + " bytes free of " + r.totalMemory() + " available.");
             getServer().logFinest("Exiting DefaultManagement.ingestObject");
         }
     }
 
     public Date modifyObject(Context context, 
                              String pid, 
                              String state,
                              String label, 
                              String logMessage)
             throws ServerException {
         DOWriter w = null;
         try {
             logFinest("Entered DefaultManagement.modifyObject");
             m_ipRestriction.enforce(context);
             w=m_manager.getWriter(context, pid);
             if (state!=null && !state.equals("")) {
                 if (!state.equals("A") && !state.equals("D") && !state.equals("I")) {
                   throw new InvalidStateException("The object state of \"" + state
                           + "\" is invalid. The allowed values for state are: "
                           + " A (active), D (deleted), and I (inactive).");
                 }                
                 w.setState(state);
             }
             if (label!=null && !label.equals(""))
                 w.setLabel(label);
             w.commit(logMessage);
             return w.getLastModDate();
         } finally {
             if (w != null) {
                 m_manager.releaseWriter(w);
             }
             Runtime r=Runtime.getRuntime();
             getServer().logFinest("Memory: " + r.freeMemory() + " bytes free of " + r.totalMemory() + " available.");
             getServer().logFinest("Exiting DefaultManagement.modifyObject");
         }
     }
 
 	public Property[] getObjectProperties(Context context, String pid)
 		throws ServerException {		
 		try {
 			logFinest("Entered DefaultManagement.getObjectProperties");
 			m_ipRestriction.enforce(context);			
 			ArrayList props = new ArrayList();
 			DOReader reader=m_manager.getReader(context, pid);
 			
 			props.add(new Property(
 						"info:fedora/fedora-system:def/fType",
 						reader.getFedoraObjectType()));
 			
 
 			props.add(new Property(
 						"info:fedora/fedora-system:def/cModel",
 						reader.getContentModelId()));
 						
 			props.add(new Property(
 						"info:fedora/fedora-system:def/label",
 						reader.GetObjectLabel()));
 						
 			props.add(new Property(
 						"info:fedora/fedora-system:def/state",
 						reader.GetObjectState()));
 						
 			props.add(new Property(
 						"info:fedora/fedora-system:def/owner",
 						reader.getOwnerId()));
 						
 			props.add(new Property(
 						"info:fedora/fedora-system:def/cDate",
 						DateUtility.convertDateToString(reader.getCreateDate())));
 						
 			props.add(new Property(
 						"info:fedora/fedora-system:def/mDate",
 						DateUtility.convertDateToString(reader.getLastModDate())));
 			
 			//Property[] extProps=reader.getExtProperties();
 			
 			return (Property[])props.toArray(new Property[0]);
 		} finally {
 			logFinest("Exiting DefaultManagement.getObjectProperties");
 		}
 	}
 
     public InputStream getObjectXML(Context context, 
                                     String pid, 
                                     String encoding) 
     		throws ServerException {
         try {
             logFinest("Entered DefaultManagement.getObjectXML");
             m_ipRestriction.enforce(context);
             DOReader reader=m_manager.getReader(context, pid);
             InputStream instream=reader.GetObjectXML();
             return instream;
         } finally {
             logFinest("Exiting DefaultManagement.getObjectXML");
         }
     }
 
     public InputStream exportObject(Context context, 
                                     String pid, 
                                     String format,
                                     String exportContext, 
                                     String encoding) 
     		throws ServerException {
         try {
             logFinest("Entered DefaultManagement.exportObject");
             m_ipRestriction.enforce(context);
             DOReader reader=m_manager.getReader(context, pid);
             InputStream instream=reader.ExportObject(format,exportContext);
             return instream;
         } finally {
             logFinest("Exiting DefaultManagement.exportObject");
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
             logFinest("Entered DefaultManagement.purgeObject");
             m_ipRestriction.enforce(context);
             w=m_manager.getWriter(context, pid);
             w.remove();
             w.commit(logMessage);
             return DateUtility.convertLocalDateToUTCDate(new Date());
         } finally {
             if (w != null) m_manager.releaseWriter(w);
             logFinest("Exiting DefaultManagement.purgeObject");
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
                                 String logMessage) throws ServerException {
                                    	
        if (dsID.equals("AUDIT") || dsID.equals("FEDORA-AUDITTRAIL")) {
 			throw new GeneralException("Creation of a datastream with an"
 				+ " identifier of 'AUDIT' or 'FEDORA-AUDITTRAIL' is not permitted.");
         }
         DOWriter w=null;
         try {
             getServer().logFinest("Entered DefaultManagement.addDatastream");
             m_ipRestriction.enforce(context);
             w=m_manager.getWriter(context, pid);
             Datastream ds;
             if (controlGroup.equals("X")) {
                 ds=new DatastreamXMLMetadata();
                 ds.DSInfoType="";  // field is now deprecated
                 try {
                     InputStream in;
                     if (dsLocation.startsWith("uploaded://")) {
                         in=getTempStream(dsLocation);
                     } else {
                         in=m_contentManager.getExternalContent(dsLocation).getStream();
                     }
                     // parse with xerces... then re-serialize, removing
                     // processing instructions and ensuring the encoding gets to UTF-8
                     ByteArrayOutputStream out=new ByteArrayOutputStream();
                     // use xerces to pretty print the xml, assuming it's well formed
                     OutputFormat fmt=new OutputFormat("XML", "UTF-8", true);
                     fmt.setIndent(2);
                     fmt.setLineWidth(120);
                     fmt.setPreserveSpace(false);
                     fmt.setOmitXMLDeclaration(true);
                     XMLSerializer ser=new XMLSerializer(out, fmt);
                     DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
                     factory.setNamespaceAware(true);
                     DocumentBuilder builder=factory.newDocumentBuilder();
                     Document doc=builder.parse(in);
                     ser.serialize(doc);
                     // now put it in the byte array
                     ((DatastreamXMLMetadata) ds).xmlContent=out.toByteArray();
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
             ds.DSLabel=dsLabel;
             ds.DSLocation=dsLocation;
             ds.DSFormatURI=formatURI;
             if (versionable) {
                 ds.DSVersionable = "YES";
             } else {
                 ds.DSVersionable = "NO";
             }
             ds.DSMIME=MIMEType;
             if (!dsState.equals("A") && !dsState.equals("D") && !dsState.equals("I")) {
                 throw new InvalidStateException("The datastream state of \"" + dsState
                         + "\" is invalid. The allowed values for state are: "
                         + " A (active), D (deleted), and I (inactive).");
             }            
             ds.DSState= dsState;
             Date nowUTC=DateUtility.convertLocalDateToUTCDate(new Date());
             ds.DSCreateDT=nowUTC;
             if (dsID==null || dsID.length()==0) {
                 ds.DatastreamID=w.newDatastreamID();
             } else {
                 if (dsID.indexOf(" ")!=-1) {
                     throw new GeneralException("Datastream ids cannot contain spaces.");
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
             ds.DSVersionID=ds.DatastreamID + ".0";
             ds.DatastreamAltIDs = altIDs;
             AuditRecord audit=new fedora.server.storage.types.AuditRecord();
             audit.id=w.newAuditRecordID();
             audit.processType="Fedora API-M";
             audit.action="addDatastream";
             audit.componentID=ds.DatastreamID;
             audit.responsibility=context.get("userId");
             audit.date=nowUTC;
             audit.justification=logMessage;
             w.getAuditRecords().add(audit);
             w.addDatastream(ds);
             w.commit("Added a new datastream");
             return ds.DatastreamID;
         } finally {
             if (w!=null) {
                 m_manager.releaseWriter(w);
             }
             getServer().logFinest("Exiting DefaultManagement.addDatastream");
         }
     }
 
 	public String addDisseminator(Context context,
 									String pid,
 									String bDefPid,
 									String bMechPid,
 									String dissLabel,
 									String bDefLabel,
 									String bMechLabel,
 									DSBindingMap bindingMap,
 									String dissState,
 									String logMessage) throws ServerException {
 
 			DOWriter w=null;
 			try {
                 getServer().logFinest("Entered DefaultManagement.addDisseminator");
     			m_ipRestriction.enforce(context);
 				w=m_manager.getWriter(context, pid);
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
 				diss.bDefLabel = bDefLabel;
 				diss.bMechLabel = bMechLabel;
 				Date nowUTC=DateUtility.convertLocalDateToUTCDate(new Date());
 				diss.dissCreateDT=nowUTC;
 				diss.dissID = w.newDisseminatorID();
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
 				audit.responsibility=context.get("userId");
 				audit.date=nowUTC;
 				audit.justification=logMessage;
 				w.getAuditRecords().add(audit);
 				w.addDisseminator(diss);
 				w.commit("Added a new disseminator");
 				return diss.dissID;
 			} finally {
 				if (w!=null) {
 					m_manager.releaseWriter(w);
 				}
                 getServer().logFinest("Exiting DefaultManagement.addDisseminator");
 			}
 		}
 
     public Date modifyDatastreamByReference(Context context, 
                                             String pid,
                                             String datastreamId, 
                                             String[] altIDs,
                                             String dsLabel, 
                                             boolean versionable,
                                             String mimeType,
                                             String formatURI,
                                             String dsLocation, 
                                             String dsState,
                                             String logMessage,
                                             boolean force)
             throws ServerException {
 		if (datastreamId.equals("AUDIT") || datastreamId.equals("FEDORA-AUDITTRAIL")) {
 			throw new GeneralException("Modification of the system-controlled AUDIT"
 				+ " datastream is not permitted.");
 		}
 
         DOWriter w = null;
         try {
             getServer().logFinest("Entered DefaultManagement.modifyDatastreamByReference");
             m_ipRestriction.enforce(context);
             w=m_manager.getWriter(context, pid);
             fedora.server.storage.types.Datastream orig=w.GetDatastream(datastreamId, null);
 
             // If force is false and the mime type changed, validate the
             // original datastream with respect to any disseminators it is
             // involved in, and keep a record of that information for later
             // (so we can determine whether the mime type change would cause
             // data contract invalidation)
             Map oldValidationReports = null;
             if (mimeType == null) mimeType = orig.DSMIME;
             if ( !mimeType.equals(orig.DSMIME) && !force) {
                 oldValidationReports = getAllBindingMapValidationReports(
                                            context, w, datastreamId);
             }
 
             if (orig.DSState.equals("D")) {
                 throw new GeneralException("Can only change state on deleted datastreams.");
             }
             Date nowUTC;  // datastream modified date
             if (orig.DSControlGrp.equals("M")) {
                     // copy the original datastream, replacing its DSLocation with
                     // the new location (or the old datastream's default dissemination location, if empty or null),
                     // triggering to doCommit that it needs to
                     // be loaded from a new remote location
                     DatastreamManagedContent newds=new DatastreamManagedContent();
                     newds.DatastreamID=orig.DatastreamID;
                     // make sure it has a different id
                     newds.DSVersionID=w.newDatastreamID(datastreamId);
                     newds.DatastreamAltIDs = altIDs;
                     newds.DSLabel=dsLabel;
                     if (versionable) {
                         newds.DSVersionable = "YES";
                     } else {
                         newds.DSVersionable = "NO";
                     }
                     newds.DSMIME = mimeType;
                     newds.DSFormatURI=formatURI;
                     nowUTC=DateUtility.convertLocalDateToUTCDate(new Date());
                     newds.DSCreateDT=nowUTC;
                     //newds.DSSize will be computed later
                     newds.DSControlGrp="M";
                     newds.DSInfoType=orig.DSInfoType;
                     if(dsState==null || dsState.equals("")) {
                       // If reference unspecified leave state unchanged
                       newds.DSState = orig.DSState;
                     } else {
                       // Check that supplied value for state is one of the allowable values
                       if (!dsState.equals("A") && !dsState.equals("D") && !dsState.equals("I")) {
                           throw new InvalidStateException("The datastream state of \"" + dsState
                                   + "\" is invalid. The allowed values for state are: "
                                   + " A (active), D (deleted), and I (inactive).");
                       }                           
                       newds.DSState = dsState;
                     }                     
                     if (dsLocation==null || dsLocation.equals("")) {
                         // if location unspecified, cause a copy of the
                         // prior content to be made at commit-time
                         newds.DSLocation="copy://" + orig.DSLocation;
                     } else {
                         newds.DSLocation=dsLocation;
                     }
                     // just add the datastream
                     w.addDatastream(newds);
                     // if state was changed, set new state
                     if (!orig.DSState.equals(newds.DSState)) {
                         w.setDatastreamState(datastreamId, newds.DSState); }
                     // add the audit record
                     fedora.server.storage.types.AuditRecord audit=new fedora.server.storage.types.AuditRecord();
                     audit.id=w.newAuditRecordID();
                     audit.processType="Fedora API-M";
                     audit.action="modifyDatastreamByReference";
                     audit.componentID=newds.DatastreamID;
                     audit.responsibility=context.get("userId");
                     audit.date=nowUTC;
                     audit.justification=logMessage;
                     w.getAuditRecords().add(audit);
             } else {
                 // Deal with other kinds, except xml (that must be passed in by value).
                 if (orig.DSControlGrp.equals("X")) {
                     throw new GeneralException("Inline XML datastreams must be modified by value, not by reference.");
                 }
                 DatastreamReferencedContent newds=new DatastreamReferencedContent();
                 newds.DatastreamID=orig.DatastreamID;
                 // make sure it has a different id
                 newds.DSVersionID=w.newDatastreamID(datastreamId);
                 newds.DSLabel=dsLabel;
                 newds.DatastreamAltIDs = altIDs;
                 if (versionable) {
                     newds.DSVersionable = "YES";
                 } else {
                     newds.DSVersionable = "NO";
                 }
                 newds.DSMIME = mimeType;
                 newds.DSFormatURI=formatURI;
                 nowUTC=DateUtility.convertLocalDateToUTCDate(new Date());
                 newds.DSCreateDT=nowUTC;
                 newds.DSControlGrp=orig.DSControlGrp;
                 newds.DSInfoType=orig.DSInfoType;
                 if(dsState==null || dsState.equals("")) {
                   // If reference unspecified leave state unchanged
                   newds.DSState = orig.DSState;
                 } else {
                   // Check that supplied value for state is one of the allowable values
                   if (!dsState.equals("A") && !dsState.equals("D") && !dsState.equals("I")) {
                       throw new InvalidStateException("The datastream state of \"" + dsState
                               + "\" is invalid. The allowed values for state are: "
                               + " A (active), D (deleted), and I (inactive).");
                   }                           
                   newds.DSState = dsState;
                 }                
                 if (dsLocation==null || dsLocation.equals("")) {
                     // if location unspecified for referenced or external,
                     // just use the old location
                     newds.DSLocation=orig.DSLocation;
                 } else {
                     newds.DSLocation=dsLocation;
                 }
                 // just add the datastream
                 w.addDatastream(newds);
                 // if state was changed, set new state
                 if (!orig.DSState.equals(newds.DSState)) {
                         w.setDatastreamState(datastreamId, newds.DSState); }
                 // add the audit record
                 fedora.server.storage.types.AuditRecord audit=new fedora.server.storage.types.AuditRecord();
                 audit.id=w.newAuditRecordID();
                 audit.processType="Fedora API-M";
                 audit.action="modifyDatastreamByReference";
                 audit.componentID=newds.DatastreamID;
                 audit.responsibility=context.get("userId");
                 audit.date=nowUTC;
                 audit.justification=logMessage;
                 w.getAuditRecords().add(audit);
             }
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
             if (w!=null) {
                 m_manager.releaseWriter(w);
             }
             getServer().logFinest("Exiting DefaultManagement.modifyDatastreamByReference");
         }
     }
 
     public Date modifyDatastreamByValue(Context context, 
                                         String pid,
                                         String datastreamId, 
                                         String[] altIDs,
                                         String dsLabel, 
                                         boolean versionable,
                                         String mimeType,
                                         String formatURI,
                                         InputStream dsContent,
                                         String dsState,
                                         String logMessage,
                                         boolean force)
             throws ServerException {
 		if (datastreamId.equals("AUDIT") || datastreamId.equals("FEDORA-AUDITTRAIL")) {
 			throw new GeneralException("Modification of the system-controlled AUDIT"
 				+ " datastream is not permitted.");
 		}
         DOWriter w=null;
         boolean mimeChanged = false;
         try {
             getServer().logFinest("Entered DefaultManagement.modifyDatastreamByValue");
             m_ipRestriction.enforce(context);
             w=m_manager.getWriter(context, pid);
             fedora.server.storage.types.Datastream orig=w.GetDatastream(datastreamId, null);
 
             // If force is false and the mime type changed, validate the
             // original datastream with respect to any disseminators it is
             // involved in, and keep a record of that information for later
             // (so we can determine whether the mime type change would cause
             // data contract invalidation)
             Map oldValidationReports = null;
             if (mimeType == null) mimeType = orig.DSMIME;
             if ( !mimeType.equals(orig.DSMIME) && !force) {
                 oldValidationReports = getAllBindingMapValidationReports(
                                            context, w, datastreamId);
             }
 
             if (orig.DSState.equals("D")) {
                 throw new GeneralException("Can only change state on deleted datastreams.");
             }
             if (!orig.DSControlGrp.equals("X")) {
                 throw new GeneralException("Only inline XML datastreams may be modified by value.");
             }
             if (orig.DatastreamID.equals("METHODMAP")
                     || orig.DatastreamID.equals("DSINPUTSPEC")
                     || orig.DatastreamID.equals("WSDL")) {
                 throw new GeneralException("METHODMAP, DSINPUTSPEC, and WSDL datastreams cannot be modified.");
             }
             DatastreamXMLMetadata newds=new DatastreamXMLMetadata();
             newds.DSMDClass=((DatastreamXMLMetadata) orig).DSMDClass;
             if (dsContent==null) {
                 // If the passed-in dsContent is null, that means "dont change
                 // the content".  Accordingly, here we just make a copy of
                 // the old content.
                 newds.xmlContent=((DatastreamXMLMetadata) orig).xmlContent;
             } else {
                 // If it's not null, use it
                 ByteArrayOutputStream bytes=new ByteArrayOutputStream();
                 try {
                     StreamUtility.pipeStream(dsContent, bytes, 1024);
                 } catch (Exception ex) {
                 }
                 newds.xmlContent=bytes.toByteArray();
             }
             newds.DatastreamID=orig.DatastreamID;
             // make sure it has a different id
             newds.DSVersionID=w.newDatastreamID(datastreamId);
             newds.DSLabel=dsLabel;
             newds.DatastreamAltIDs = altIDs;
             if (versionable) {
                 newds.DSVersionable = "YES";
             } else {
                 newds.DSVersionable = "NO";
             }
             newds.DSMIME = mimeType;
             newds.DSFormatURI = formatURI;
             Date nowUTC=DateUtility.convertLocalDateToUTCDate(new Date());
             newds.DSCreateDT=nowUTC;
             newds.DSControlGrp=orig.DSControlGrp;
             newds.DSInfoType=orig.DSInfoType;
             if(dsState==null || dsState.equals("")) {
               // If reference unspecified leave state unchanged
               newds.DSState = orig.DSState;
             } else {
               // Check that supplied value for state is one of the allowable values
               if (!dsState.equals("A") && !dsState.equals("D") && !dsState.equals("I")) {
                   throw new InvalidStateException("The datastream state of \"" + dsState
                           + "\" is invalid. The allowed values for state are: "
                           + " A (active), D (deleted), and I (inactive).");
               }                           
               newds.DSState = dsState;
             }
             // just add the datastream
             w.addDatastream(newds);
             // if state was changed, set new state
             if (!orig.DSState.equals(newds.DSState)) {
                         w.setDatastreamState(datastreamId, newds.DSState); }
             // add the audit record
             fedora.server.storage.types.AuditRecord audit=new fedora.server.storage.types.AuditRecord();
             audit.id=w.newAuditRecordID();
             audit.processType="Fedora API-M";
             audit.action="modifyDatastreamByValue";
             audit.componentID=newds.DatastreamID;
             audit.responsibility=context.get("userId");
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
             if (w!=null) {
                 m_manager.releaseWriter(w);
             }
             getServer().logFinest("Exiting DefaultManagement.modifyDatastreamByValue");
         }
     }
 
     public Date modifyDisseminator(Context context, 
                                    String pid,
                                    String disseminatorId, 
                                    String bMechPid, 
                                    String dissLabel,
                                    String bDefLabel, 
                                    String bMechLabel, 
                                    DSBindingMap dsBindingMap,
                                    String dissState,
                                    String logMessage,
                                    boolean force)
             throws ServerException {
         DOWriter w=null;
         DOReader r=null;
         try {
             getServer().logFinest("Entered DefaultManagement.modifyDisseminator");
             m_ipRestriction.enforce(context);
             w = m_manager.getWriter(context, pid);
             fedora.server.storage.types.Disseminator orig=w.GetDisseminator(disseminatorId, null);
             String oldValidationReport = null;
             if (!force) {
                 oldValidationReport = getBindingMapValidationReport(context,
                                                                     w,
                                                                     orig.bMechID);
             }
             r=m_manager.getReader(context,pid);  // FIXME: Unnecessary?  Is 
                                                  // there a reason "w" isn't 
                                                  // used for the call below?
             Date[] d=r.getDisseminatorVersions(disseminatorId);
             // copy the original disseminator, replacing any modified fiELDS
             Disseminator newdiss=new Disseminator();
             newdiss.dissID=orig.dissID;
             // make sure disseminator has a different id
             newdiss.dissVersionID=w.newDisseminatorID(disseminatorId);
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
             Date nowUTC=DateUtility.convertLocalDateToUTCDate(new Date());
             newdiss.dissCreateDT=nowUTC;
             // changing bDefID and ParentPid not permitted; use original values
             newdiss.bDefID=orig.bDefID;
             newdiss.parentPID=orig.parentPID;
             // set any fields that were specified; null/empty indicates
             // leave original value unchanged
             if (dissLabel==null || dissLabel.equals("")) {
               newdiss.dissLabel=orig.dissLabel;
             } else {
               newdiss.dissLabel=dissLabel;
             }
             if (bDefLabel==null || bDefLabel.equals("")) {
               newdiss.bDefLabel=orig.bDefLabel;
             } else {
               newdiss.bDefLabel=bDefLabel;
             }
             if (bMechPid==null || bMechPid.equals("")) {
               newdiss.bMechID=orig.bMechID;
             } else {
               newdiss.bMechID=bMechPid;
             }
             if (bMechLabel==null || bMechLabel.equals("")) {
               newdiss.bMechLabel=orig.bMechLabel;
             } else {
               newdiss.bMechLabel=bMechLabel;
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
             audit.responsibility=context.get("userId");
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
             if (w!=null) {
                 m_manager.releaseWriter(w);
             }
             getServer().logFinest("Exiting DefaultManagement.modifyDisseminator");
         }
     }
 
 
     public Date[] purgeDatastream(Context context, 
                                   String pid,
                                   String datastreamID, 
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
             getServer().logFinest("Entered DefaultManagement.purgeDatastream");
             m_ipRestriction.enforce(context);
             w=m_manager.getWriter(context, pid);
             Date start=null;
             Date[] deletedDates=w.removeDatastream(datastreamID, start, endDT);
             // check if there's at least one version with this id...
             if (w.GetDatastream(datastreamID, null)==null) {
                 // if deleting would result in no versions remaining,
                 // only continue if there are no disseminators that use
                 // this datastream.
                 // to do this, we must look through all versions of every
                 // disseminator, regardless of state
                 SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                 ArrayList usedList=new ArrayList();
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
                                              start, 
                                              endDT, 
                                              deletedDates);
             Date nowUTC=DateUtility.convertLocalDateToUTCDate(new Date());
             fedora.server.storage.types.AuditRecord audit=new fedora.server.storage.types.AuditRecord();
             audit.id=w.newAuditRecordID();
             audit.processType="Fedora API-M";
             audit.action="purgeDatastream";
             audit.componentID=datastreamID;
             audit.responsibility=context.get("userId");
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
             if (w!=null) {
                 m_manager.releaseWriter(w);
             }
             getServer().logFinest("Exiting DefaultManagement.purgeDatastream");
         }
     }
 
     private String getPurgeLogMessage(String kindaThing, 
                                       String id, 
                                       Date start,
                                       Date end, 
                                       Date[] deletedDates) {
         SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
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
             getServer().logFinest("Entered DefaultManagement.getDatastream");
             m_ipRestriction.enforce(context);
             DOReader r=m_manager.getReader(context, pid);
     		return r.GetDatastream(datastreamID, asOfDateTime);
         } finally {
             getServer().logFinest("Exiting DefaultManagement.getDatastream");
         }
     }
 
     public Datastream[] getDatastreams(Context context, 
                                        String pid,
                                        Date asOfDateTime, 
                                        String state)
             throws ServerException {
         try {
             getServer().logFinest("Entered DefaultManagement.getDatastreams");
             m_ipRestriction.enforce(context);
             DOReader r=m_manager.getReader(context, pid);
     		return r.GetDatastreams(asOfDateTime, state);
         } finally {
             getServer().logFinest("Exiting DefaultManagement.getDatastream");
         }
     }
 
     public Datastream[] getDatastreamHistory(Context context, 
                                              String pid, 
                                              String datastreamID)
             throws ServerException {
         try {
             getServer().logFinest("Entered DefaultManagement.getDatastreamHistory");
             m_ipRestriction.enforce(context);
             DOReader r=m_manager.getReader(context, pid);
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
             getServer().logFinest("Exiting DefaultManagement.getDatastreamHistory");
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
             getServer().logFinest("Entered DefaultManagement.purgeDisseminator");
             m_ipRestriction.enforce(context);
             w=m_manager.getWriter(context, pid);
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
             Date nowUTC=DateUtility.convertLocalDateToUTCDate(new Date());
             fedora.server.storage.types.AuditRecord audit=new fedora.server.storage.types.AuditRecord();
             audit.id=w.newAuditRecordID();
             audit.processType="Fedora API-M";
             audit.action="purgeDisseminator";
             audit.componentID=disseminatorID;
             audit.responsibility=context.get("userId");
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
             if (w!=null) {
                 m_manager.releaseWriter(w);
             }
             getServer().logFinest("Exiting DefaultManagement.purgeDisseminator");
         }
     }
 
     public Disseminator getDisseminator(Context context, 
                                         String pid,
                                         String disseminatorId, 
                                         Date asOfDateTime)
             throws ServerException {
         try {
             getServer().logFinest("Entered DefaultManagement.getDisseminator");
             m_ipRestriction.enforce(context);
             DOReader r=m_manager.getReader(context, pid);
             return r.GetDisseminator(disseminatorId, asOfDateTime);
         } finally {
             getServer().logFinest("Exiting DefaultManagement.getDisseminator");
         }
     }
 
     public Disseminator[] getDisseminators(Context context, 
                                            String pid,
                                            Date asOfDateTime, 
                                            String dissState)
             throws ServerException {
         try {
             getServer().logFinest("Entered DefaultManagement.getDisseminators");
             m_ipRestriction.enforce(context);
             DOReader r=m_manager.getReader(context, pid);
             return r.GetDisseminators(asOfDateTime, dissState);
         } finally {
             getServer().logFinest("Exiting DefaultManagement.getDisseminators");
         }
     }
 
     public Disseminator[] getDisseminatorHistory(Context context, 
                                                  String pid, 
                                                  String disseminatorID)
             throws ServerException {
         try {
             getServer().logFinest("Entered DefaultManagement.getDisseminatorHistory");
             m_ipRestriction.enforce(context);
             DOReader r=m_manager.getReader(context, pid);
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
             getServer().logFinest("Exiting DefaultManagement.getDisseminatorHistory");
         }
     }
 
     public String[] getNextPID(Context context, 
                                int numPIDs,
                                String namespace)
             throws ServerException {
         try {
             getServer().logFinest("Entered DefaultManagement.getNextPID");
             m_ipRestriction.enforce(context);
             return m_manager.getNextPID(numPIDs, namespace);
         } finally {
             getServer().logFinest("Exiting DefaultManagement.getNextPID");
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
 
     public String putTempStream(InputStream in)
     	    throws StreamWriteException {
 		// first clean up after old stuff
 		long minStartTime=System.currentTimeMillis()-(60*1000*m_uploadStorageMinutes);
                 ArrayList removeList=new ArrayList();
 		Iterator iter=m_uploadStartTime.keySet().iterator();
         while (iter.hasNext()) {
 		    String id=(String) iter.next();
 		    Long startTime=(Long) m_uploadStartTime.get(id);
 			if (startTime.longValue()<minStartTime) {
 			    // remove from filesystem and hash
 				File f=new File(m_tempDir, id);
 				if (f.delete()) {
 				    logInfo("Removed uploaded file '" + id + "' because it expired.");
 				} else {
 				    logWarning("Could not remove expired uploaded file '" + id
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
 		int id=getNextTempId();
 		// and attempt to save the stream
 	    try {
 		    StreamUtility.pipeStream(in, new FileOutputStream(new File(m_tempDir, "" + id)), 8192);
 		} catch (Exception e) {
 		    throw new StreamWriteException(e.getMessage());
 		}
 		// if we got this far w/o an exception, add to hash with current time
 		// and return the identifier-that-looks-like-a-url
 		long now=System.currentTimeMillis();
 	    m_uploadStartTime.put("" + id, new Long(now));
 		return "uploaded://" + id;
 	}
 
     private synchronized int getNextTempId() {
 	    m_lastId++;
 		return m_lastId;
 	}
 
     public InputStream getTempStream(String id)
     	    throws StreamReadException {
 		// it should come in starting with "uploaded://"
 		if (id.startsWith("uploaded://") || id.length()<12) {
 		    String internalId=id.substring(11);
 			if (m_uploadStartTime.get(internalId)!=null) {
 			    // found... remove from hash and return inputstream
 		        m_uploadStartTime.remove(internalId);
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
           getServer().logFinest("Entered DefaultManagement.setDatastreamState");
           m_ipRestriction.enforce(context);
           w=m_manager.getWriter(context, pid);
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
           audit.responsibility=context.get("userId");
           Date nowUTC=DateUtility.convertLocalDateToUTCDate(new Date());
           audit.date=nowUTC;
           audit.justification=logMessage;
           w.getAuditRecords().add(audit);
 
           // if all went ok, commit
           w.commit(logMessage);
           return nowUTC;
       } finally {
           if (w!=null) {
               m_manager.releaseWriter(w);
           }
           getServer().logFinest("Exiting DefaultManagement.setDatastreamState");
         }
     }
 
     public Date setDisseminatorState(Context context, 
                                      String pid, 
                                      String disseminatorID, 
                                      String dissState, 
                                      String logMessage)
             throws ServerException {
       DOWriter w=null;
       try {
           getServer().logFinest("Entered DefaultManagement.setDisseminatorState");
           m_ipRestriction.enforce(context);
           w=m_manager.getWriter(context, pid);
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
           audit.responsibility=context.get("userId");
           Date nowUTC=DateUtility.convertLocalDateToUTCDate(new Date());
           audit.date=nowUTC;
           audit.justification=logMessage;
           w.getAuditRecords().add(audit);
 
           // if all went ok, commit
           w.commit(logMessage);
           return nowUTC;
       } finally {
           if (w!=null) {
               m_manager.releaseWriter(w);
           }
           getServer().logFinest("Exiting DefaultManagement.setDisseminatorState");
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
         BMechReader mReader = m_manager.getBMechReader(context, bMechPID);
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
         HashMap map = new HashMap();
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
         HashMap map = new HashMap();
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
 }
