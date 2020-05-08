 package fedora.server.management;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 
 import org.apache.xml.serialize.OutputFormat;
 import org.apache.xml.serialize.XMLSerializer;
 
 import fedora.server.Context;
 import fedora.server.Module;
 import fedora.server.Server;
 import fedora.server.errors.GeneralException;
 import fedora.server.errors.ObjectValidityException;
 import fedora.server.errors.ModuleInitializationException;
 import fedora.server.errors.ModuleShutdownException;
 import fedora.server.errors.ServerException;
 import fedora.server.errors.StreamReadException;
 import fedora.server.errors.StreamWriteException;
 import fedora.server.security.IPRestriction;
 import fedora.server.storage.DOReader;
 import fedora.server.storage.DOManager;
 import fedora.server.storage.DOWriter;
 import fedora.server.storage.ExternalContentManager;
 import fedora.server.storage.types.AuditRecord;
 import fedora.server.storage.types.DSBindingMap;
 import fedora.server.storage.types.DSBinding;
 import fedora.server.storage.types.DatastreamContent;
 import fedora.server.storage.types.DatastreamManagedContent;
 import fedora.server.storage.types.DatastreamReferencedContent;
 import fedora.server.storage.types.DatastreamXMLMetadata;
 import fedora.server.storage.types.Datastream;
 import fedora.server.storage.types.Disseminator;
 import fedora.server.utilities.DateUtility;
 import fedora.server.utilities.StreamUtility;
 
 /**
  *
  * <p><b>Title:</b> DefaultManagement.java</p>
  * <p><b>Description:</b> The Management Module, providing support for API-M.</p>
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
     public DefaultManagement(Map moduleParameters, Server server, String role)
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
 
 /*
     public String createObject(Context context)
             throws ServerException {
         getServer().logFinest("Entered DefaultManagement.createObject");
         m_ipRestriction.enforce(context);
         DOWriter w=m_manager.newWriter(context);
         String pid=w.GetObjectPID();
         m_manager.releaseWriter(w);
         getServer().logFinest("Exiting DefaultManagement.createObject");
         return pid;
     }
 */
 
     public String ingestObject(Context context, InputStream serialization, String logMessage, String format, String encoding, boolean newPid)
             throws ServerException {
         getServer().logFinest("Entered DefaultManagement.ingestObject");
         m_ipRestriction.enforce(context);
         DOWriter w=m_manager.newWriter(context, serialization, format, encoding, newPid);
         String pid=w.GetObjectPID();
         try {
             w.commit(logMessage);
             return pid;
         } finally {
             m_manager.releaseWriter(w);
             Runtime r=Runtime.getRuntime();
             getServer().logFinest("Memory: " + r.freeMemory() + " bytes free of " + r.totalMemory() + " available.");
             getServer().logFinest("Exiting DefaultManagement.ingestObject");
         }
     }
 
     public void modifyObject(Context context, String pid, String state,
             String label, String logMessage)
             throws ServerException {
         logFinest("Entered DefaultManagement.modifyObject");
         m_ipRestriction.enforce(context);
         DOWriter w=m_manager.getWriter(context, pid);
         try {
             if (state!=null)
                 w.setState(state);
             if (label!=null)
                 w.setLabel(label);
             w.commit(logMessage);
         } finally {
             m_manager.releaseWriter(w);
             Runtime r=Runtime.getRuntime();
             getServer().logFinest("Memory: " + r.freeMemory() + " bytes free of " + r.totalMemory() + " available.");
             getServer().logFinest("Exiting DefaultManagement.ingestObject");
         }
     }
 
     public InputStream getObjectXML(Context context, String pid, String format, String encoding) throws ServerException {
         logFinest("Entered DefaultManagement.getObjectXML");
         m_ipRestriction.enforce(context);
         DOReader reader=m_manager.getReader(context, pid);
         InputStream instream=reader.GetObjectXML();
         logFinest("Exiting DefaultManagement.getObjectXML");
         return instream;
     }
 
     public InputStream exportObject(Context context, String pid, String format,
             String encoding) throws ServerException {
         logFinest("Entered DefaultManagement.exportObject");
         m_ipRestriction.enforce(context);
         DOReader reader=m_manager.getReader(context, pid);
         InputStream instream=reader.ExportObject();
         logFinest("Exiting DefaultManagement.exportObject");
         return instream;
     }
 
     public void purgeObject(Context context, String pid, String logMessage)
             throws ServerException {
         logFinest("Entered DefaultManagement.purgeObject");
         m_ipRestriction.enforce(context);
         DOWriter w=m_manager.getWriter(context, pid);
         w.remove();
         w.commit(logMessage);
         m_manager.releaseWriter(w);
         logFinest("Exiting DefaultManagement.purgeObject");
     }
 
 /*
     public AuditRecord[] getObjectAuditTrail(Context context, String pid) { return null; }
 
 */
 
     // initial state is always I
     public String addDatastream(Context context,
                                 String pid,
                                 String dsLabel,
                                 String mimeType,
                                 String dsLocation,
                                 String controlGroup,
                                 String mdClass,
                                 String mdType,
                                 String dsState) throws ServerException {
         m_ipRestriction.enforce(context);
         DOWriter w=null;
         try {
             w=m_manager.getWriter(context, pid);
             Datastream ds;
             if (controlGroup.equals("X")) {
                 ds=new DatastreamXMLMetadata();
                 ds.DSInfoType=mdType;
                 if (mdClass.equals("descriptive")) {
                     ((DatastreamXMLMetadata) ds).DSMDClass=DatastreamXMLMetadata.DESCRIPTIVE;
                 } else if (mdClass.equals("digital provenance")) {
                     ((DatastreamXMLMetadata) ds).DSMDClass=DatastreamXMLMetadata.DIGIPROV;
                 } else if (mdClass.equals("source")) {
                     ((DatastreamXMLMetadata) ds).DSMDClass=DatastreamXMLMetadata.SOURCE;
                 } else if (mdClass.equals("rights")) {
                     ((DatastreamXMLMetadata) ds).DSMDClass=DatastreamXMLMetadata.RIGHTS;
                 } else if (mdClass.equals("technical")) {
                     ((DatastreamXMLMetadata) ds).DSMDClass=DatastreamXMLMetadata.TECHNICAL;
                 } else {
                     throw new GeneralException("mdClass must be one of the following:\n"
                             + " - descriptive\n"
                             + " - digital provenance\n"
                             + " - source\n"
                             + " - rights\n"
                             + " - technical");
                 }
                 // retrieve the content and set the xmlContent field appropriately
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
             ds.DSMIME=mimeType;
             ds.DSState= dsState;
             Date nowUTC=DateUtility.convertLocalDateToUTCDate(new Date());
             ds.DSCreateDT=nowUTC;
             ds.DatastreamID=w.newDatastreamID();
             ds.DSVersionID=ds.DatastreamID + ".0";
             AuditRecord audit=new fedora.server.storage.types.AuditRecord();
             audit.id=w.newAuditRecordID();
             audit.processType="Fedora API-M";
             audit.action="addDatastream";
             audit.responsibility=context.get("userId");
             audit.date=nowUTC;
             audit.justification="Added a new datastream";
             w.getAuditRecords().add(audit);
             ds.auditRecordIdList().add(audit.id);
             w.addDatastream(ds);
             w.commit("Added a new datastream");
             return ds.DatastreamID;
         } finally {
             if (w!=null) {
                 m_manager.releaseWriter(w);
             }
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
 									String dissState) throws ServerException {
 
 			m_ipRestriction.enforce(context);
 			DOWriter w=null;
 			try {
 				w=m_manager.getWriter(context, pid);
 				Disseminator diss = new Disseminator();
 				diss.isNew=true;
 				diss.parentPID = pid;
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
 				audit.responsibility=context.get("userId");
 				audit.date=nowUTC;
 				audit.justification="Added a new disseminator";
 				w.getAuditRecords().add(audit);
 				diss.auditRecordIdList().add(audit.id);
 				w.addDisseminator(diss);
 				w.commit("Added a new disseminator");
 				return diss.dissID;
 			} finally {
 				if (w!=null) {
 					m_manager.releaseWriter(w);
 				}
 			}
 		}
 
     public void modifyDatastreamByReference(Context context, String pid,
             String datastreamId, String dsLabel, String logMessage,
             String dsLocation, String dsState)
             throws ServerException {
         m_ipRestriction.enforce(context);
         DOWriter w=null;
         try {
             w=m_manager.getWriter(context, pid);
             fedora.server.storage.types.Datastream orig=w.GetDatastream(datastreamId, null);
             if (orig.DSState.equals("D")) {
                 throw new GeneralException("Can only change state on deleted datastreams.");
             }
             if (orig.DSControlGrp.equals("M")) {
                     // copy the original datastream, replacing its DSLocation with
                     // the new location (or the old datastream's default dissemination location, if empty or null),
                     // triggering to doCommit that it needs to
                     // be loaded from a new remote location
                     DatastreamManagedContent newds=new DatastreamManagedContent();
                     newds.metadataIdList().addAll(((DatastreamContent) orig).metadataIdList());
                     newds.DatastreamID=orig.DatastreamID;
                     // make sure it has a different id
                     newds.DSVersionID=w.newDatastreamID(datastreamId);
                     newds.DSLabel=dsLabel;
                     newds.DSMIME=orig.DSMIME;
                     Date nowUTC=DateUtility.convertLocalDateToUTCDate(new Date());
                     newds.DSCreateDT=nowUTC;
                     //newds.DSSize will be computed later
                     newds.DSControlGrp="M";
                     newds.DSInfoType=orig.DSInfoType;
                     newds.DSState=dsState;
                     //newds.DSState=orig.DSState;
                     if (dsLocation==null || dsLocation.equals("")) {
                         // if location unspecified, cause a copy of the
                         // prior content to be made at commit-time
                         newds.DSLocation="copy://" + orig.DSLocation;
                     } else {
                         newds.DSLocation=dsLocation;
                     }
                     newds.auditRecordIdList().addAll(orig.auditRecordIdList());
                     // just add the datastream
                     w.addDatastream(newds);
                     // if state was changed, set new state
                     if (!orig.DSState.equals(dsState)) {
                         w.setDatastreamState(datastreamId, dsState); }
                     // add the audit record
                     fedora.server.storage.types.AuditRecord audit=new fedora.server.storage.types.AuditRecord();
                     audit.id=w.newAuditRecordID();
                     audit.processType="Fedora API-M";
                     audit.action="modifyDatastreamByReference";
                     audit.responsibility=context.get("userId");
                     audit.date=nowUTC;
                     audit.justification=logMessage;
                     w.getAuditRecords().add(audit);
                     newds.auditRecordIdList().add(audit.id);
             } else {
                 // Deal with other kinds, except xml (that must be passed in by value).
                 if (orig.DSControlGrp.equals("X")) {
                     throw new GeneralException("Inline XML datastreams must be modified by value, not by reference.");
                 }
                 DatastreamReferencedContent newds=new DatastreamReferencedContent();
                 newds.metadataIdList().addAll(((DatastreamContent) orig).metadataIdList());
                 newds.DatastreamID=orig.DatastreamID;
                 // make sure it has a different id
                 newds.DSVersionID=w.newDatastreamID(datastreamId);
                 newds.DSLabel=dsLabel;
                 newds.DSMIME=orig.DSMIME;
                 Date nowUTC=DateUtility.convertLocalDateToUTCDate(new Date());
                 newds.DSCreateDT=nowUTC;
                 newds.DSControlGrp=orig.DSControlGrp;
                 newds.DSInfoType=orig.DSInfoType;
                 newds.DSState=dsState;
                 //newds.DSState=orig.DSState;
                 if (dsLocation==null || dsLocation.equals("")) {
                     // if location unspecified for referenced or external,
                     // just use the old location
                     newds.DSLocation=orig.DSLocation;
                 } else {
                     newds.DSLocation=dsLocation;
                 }
                 newds.auditRecordIdList().addAll(orig.auditRecordIdList());
                 // just add the datastream
                 w.addDatastream(newds);
                 // if state was changed, set new state
                 if (!orig.DSState.equals(dsState)) {
                         w.setDatastreamState(datastreamId, dsState); }
                 // add the audit record
                 fedora.server.storage.types.AuditRecord audit=new fedora.server.storage.types.AuditRecord();
                 audit.id=w.newAuditRecordID();
                 audit.processType="Fedora API-M";
                 audit.action="modifyDatastreamByReference";
                 audit.responsibility=context.get("userId");
                 audit.date=nowUTC;
                 audit.justification=logMessage;
                 w.getAuditRecords().add(audit);
                 newds.auditRecordIdList().add(audit.id);
             }
             // if all went ok, commit
             w.commit(logMessage);
         } finally {
             if (w!=null) {
                 m_manager.releaseWriter(w);
             }
         }
     }
 
     public void modifyDatastreamByValue(Context context, String pid,
             String datastreamId, String dsLabel, String logMessage,
             InputStream dsContent, String dsState) throws ServerException {
         m_ipRestriction.enforce(context);
         DOWriter w=null;
         try {
             w=m_manager.getWriter(context, pid);
             fedora.server.storage.types.Datastream orig=w.GetDatastream(datastreamId, null);
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
             newds.DSMIME=orig.DSMIME;
             Date nowUTC=DateUtility.convertLocalDateToUTCDate(new Date());
             newds.DSCreateDT=nowUTC;
             newds.DSControlGrp=orig.DSControlGrp;
             newds.DSInfoType=orig.DSInfoType;
             newds.DSState=dsState;
             newds.auditRecordIdList().addAll(orig.auditRecordIdList());
             // just add the datastream
             w.addDatastream(newds);
             // if state was changed, set new state
             if (!orig.DSState.equals(dsState)) {
                         w.setDatastreamState(datastreamId, dsState); }
             // add the audit record
             fedora.server.storage.types.AuditRecord audit=new fedora.server.storage.types.AuditRecord();
             audit.id=w.newAuditRecordID();
             audit.processType="Fedora API-M";
            audit.action="modifyDatastreamByReference";
             audit.responsibility=context.get("userId");
             audit.date=nowUTC;
             audit.justification=logMessage;
             w.getAuditRecords().add(audit);
             newds.auditRecordIdList().add(audit.id);
             // if all went ok, commit
             w.commit(logMessage);
         } finally {
             if (w!=null) {
                 m_manager.releaseWriter(w);
             }
         }
     }
 
 
     public void modifyDisseminator(Context context, String pid,
             String disseminatorId, String bMechPid, String dissLabel,
             String bDefLabel, String bMechLabel, DSBindingMap dsBindingMap,
             String logMessage, String dissState)
             throws ServerException {
         m_ipRestriction.enforce(context);
         DOWriter w=null;
         DOReader r=null;
         try {
             w=m_manager.getWriter(context, pid);
             fedora.server.storage.types.Disseminator orig=w.GetDisseminator(disseminatorId, null);
             r=m_manager.getReader(context,pid);
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
                       newdiss.dissState=orig.dissState;
                     } else {
                       newdiss.dissState=dissState;
                     }
                     newdiss.auditRecordIdList().addAll(orig.auditRecordIdList());
                     // just add the disseminator
                     w.addDisseminator(newdiss);
                     // add the audit record
                     fedora.server.storage.types.AuditRecord audit=new fedora.server.storage.types.AuditRecord();
                     audit.id=w.newAuditRecordID();
                     audit.processType="Fedora API-M";
                     audit.action="modifyDisseminator";
                     audit.responsibility=context.get("userId");
                     audit.date=nowUTC;
                     audit.justification=logMessage;
                     w.getAuditRecords().add(audit);
                     newdiss.auditRecordIdList().add(audit.id);
             // if all went ok, commit
             w.commit(logMessage);
         } finally {
             if (w!=null) {
                 m_manager.releaseWriter(w);
             }
         }
     }
 
 
     public Calendar[] purgeDatastream(Context context, String pid,
             String datastreamID, Calendar endDT)
             throws ServerException {
         m_ipRestriction.enforce(context);
         DOWriter w=null;
         try {
             w=m_manager.getWriter(context, pid);
             Date start=null;
             Date end=null;
             if (endDT!=null) {
                 end=endDT.getTime();
             }
             Date[] deletedDates=w.removeDatastream(datastreamID, start, end);
             // check if there's at least one version with this id...
             if (w.GetDatastream(datastreamID, null)==null) {
                 // if deleting would result in no versions remaining,
                 // only continue if there are no disseminators that use
                 // this datastream.
                 // to do this, we must look through all versions of every
                 // disseminator, regardless of state
                 SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
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
             // make a log messsage explaining what happened
             String logMessage=getPurgeLogMessage("datastream", datastreamID,
                     start, end, deletedDates);
             Date nowUTC=DateUtility.convertLocalDateToUTCDate(new Date());
             fedora.server.storage.types.AuditRecord audit=new fedora.server.storage.types.AuditRecord();
             audit.id=w.newAuditRecordID();
             audit.processType="Fedora API-M";
             audit.action="purgeDatastream";
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
             return dateArrayToCalendarArray(deletedDates);
         } finally {
             if (w!=null) {
                 m_manager.releaseWriter(w);
             }
         }
     }
 
     private Calendar[] dateArrayToCalendarArray(Date[] dates) {
         Calendar response[]=new Calendar[dates.length];
         for (int i=0; i<dates.length; i++) {
             response[i]=new GregorianCalendar();
             response[i].setTime(dates[i]);
         }
         return response;
     }
 
     private String getPurgeLogMessage(String kindaThing, String id, Date start,
             Date end, Date[] deletedDates) {
         SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
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
 
     public Datastream getDatastream(Context context, String pid,
             String datastreamID, Calendar asOfDateTime)
             throws ServerException {
         m_ipRestriction.enforce(context);
         DOReader r=m_manager.getReader(context, pid);
         Date d=null;
         if (asOfDateTime!=null) {
             d=asOfDateTime.getTime();
         }
 		return r.GetDatastream(datastreamID, d);
     }
 
     public Datastream[] getDatastreams(Context context, String pid,
             Calendar asOfDateTime, String state)
             throws ServerException {
         DOReader r=m_manager.getReader(context, pid);
         Date d=null;
         if (asOfDateTime!=null) {
             d=asOfDateTime.getTime();
         }
 		return r.GetDatastreams(d, state);
     }
 
     public Datastream[] getDatastreamHistory(Context context, String pid, String datastreamID)
             throws ServerException {
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
 
     public Calendar[] purgeDisseminator(Context context, String pid,
             String disseminatorID, Calendar endDT)
             throws ServerException {
         m_ipRestriction.enforce(context);
         DOWriter w=null;
         try {
             w=m_manager.getWriter(context, pid);
             Date start=null;
             Date end=null;
             if (endDT!=null) {
                 end=endDT.getTime();
             }
             Date[] deletedDates=w.removeDisseminator(disseminatorID, start, end);
             // make a log messsage explaining what happened
             String logMessage=getPurgeLogMessage("disseminator", disseminatorID,
                     start, end, deletedDates);
             Date nowUTC=DateUtility.convertLocalDateToUTCDate(new Date());
             fedora.server.storage.types.AuditRecord audit=new fedora.server.storage.types.AuditRecord();
             audit.id=w.newAuditRecordID();
             audit.processType="Fedora API-M";
             audit.action="purgeDisseminator";
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
             return dateArrayToCalendarArray(deletedDates);
         } finally {
             if (w!=null) {
                 m_manager.releaseWriter(w);
             }
         }
     }
 
     public Disseminator getDisseminator(Context context, String pid,
             String disseminatorId, Calendar asOfDateTime)
             throws ServerException {
         m_ipRestriction.enforce(context);
         DOReader r=m_manager.getReader(context, pid);
         Date d=null;
         if (asOfDateTime!=null) {
             d=asOfDateTime.getTime();
         }
         return r.GetDisseminator(disseminatorId, d);
     }
 
     public Disseminator[] getDisseminators(Context context, String pid,
         Calendar asOfDateTime, String dissState)
             throws ServerException {
         DOReader r=m_manager.getReader(context, pid);
         Date d=null;
         if (asOfDateTime!=null) {
             d=asOfDateTime.getTime();
         }
         return r.GetDisseminators(d, dissState);
     }
 
     public Disseminator[] getDisseminatorHistory(Context context, String pid, String disseminatorID)
             throws ServerException {
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
     }
 
     public String[] getNextPID(Context context, int numPIDs,
             String namespace)
             throws ServerException {
         m_ipRestriction.enforce(context);
         return m_manager.getNextPID(numPIDs, namespace);
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
 
     public void setDatastreamState(Context context, String pid, String datastreamID, String dsState, String logMessage)
             throws ServerException {
       m_ipRestriction.enforce(context);
       DOWriter w=null;
       try {
           w=m_manager.getWriter(context, pid);
           fedora.server.storage.types.Datastream ds=w.GetDatastream(datastreamID, null);
           w.setDatastreamState(datastreamID, dsState);
 
           // add the audit record
           fedora.server.storage.types.AuditRecord audit=new fedora.server.storage.types.AuditRecord();
           audit.id=w.newAuditRecordID();
           audit.processType="Fedora API-M";
           audit.action="setDatastreamState";
           audit.responsibility=context.get("userId");
           Date nowUTC=DateUtility.convertLocalDateToUTCDate(new Date());
           audit.date=nowUTC;
           audit.justification=logMessage;
           w.getAuditRecords().add(audit);
           ds.auditRecordIdList().add(audit.id);
 
           // if all went ok, commit
           w.commit(logMessage);
       } finally {
           if (w!=null) {
               m_manager.releaseWriter(w);
           }
         }
     }
 
     public void setDisseminatorState(Context context, String pid, String disseminatorID, String dissState, String logMessage)
             throws ServerException {
       m_ipRestriction.enforce(context);
       DOWriter w=null;
       try {
           w=m_manager.getWriter(context, pid);
           fedora.server.storage.types.Disseminator diss=w.GetDisseminator(disseminatorID, null);
           w.setDisseminatorState(disseminatorID, dissState);
 
           // add the audit record
           fedora.server.storage.types.AuditRecord audit=new fedora.server.storage.types.AuditRecord();
           audit.id=w.newAuditRecordID();
           audit.processType="Fedora API-M";
           audit.action="setDisseminatorState";
           audit.responsibility=context.get("userId");
           Date nowUTC=DateUtility.convertLocalDateToUTCDate(new Date());
           audit.date=nowUTC;
           audit.justification=logMessage;
           w.getAuditRecords().add(audit);
           diss.auditRecordIdList().add(audit.id);
 
           // if all went ok, commit
           w.commit(logMessage);
       } finally {
           if (w!=null) {
               m_manager.releaseWriter(w);
           }
         }
     }
 }
