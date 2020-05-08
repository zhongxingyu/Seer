 package fedora.server.management;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import fedora.server.Context;
 import fedora.server.Module;
 import fedora.server.Server;
 import fedora.server.errors.GeneralException;
 import fedora.server.errors.ModuleInitializationException;
 import fedora.server.errors.ModuleShutdownException;
 import fedora.server.errors.ServerException;
 import fedora.server.errors.StreamReadException;
 import fedora.server.errors.StreamWriteException;
 import fedora.server.security.IPRestriction;
 import fedora.server.storage.DOReader;
 import fedora.server.storage.DOManager;
 import fedora.server.storage.DOWriter;
 import fedora.server.storage.types.DSBindingMap;
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
     private HashMap m_uploadStartTime;
 
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
 			m_uploadStartTime=new HashMap();
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
         // FIXME: this logic should go in clients,
         // but it happens that it's convenient to put here for now.
         // the below does a purgeObject if commit fails... kind of an auto-cleanup
         // in the future this "initial state" stuff will be reconsidered anyway,
         // applying the ideas of workflow, etc..
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
         // FIXME: This should get a writer and call remove, then commit instead...but this works for now
         // fedora.server.storage.types.BasicDigitalObject obj=new fedora.server.storage.types.BasicDigitalObject();
         // obj.setPid(pid);
         // ((fedora.server.storage.DefaultDOManager) m_manager).doCommit(context, obj, logMessage, true);
         DOWriter w=m_manager.getWriter(context, pid);
         w.remove();
         w.commit(logMessage);
         m_manager.releaseWriter(w);
         logFinest("Exiting DefaultManagement.purgeObject");
     }
 
 /*
     public AuditRecord[] getObjectAuditTrail(Context context, String pid) { return null; }
 
     public String addDatastreamExternal(Context context, String pid, String dsLabel, String dsLocation) { return null; }
 
     public String addDatastreamManagedContent(Context context, String pid, String dsLabel, String MimeType, InputStream dsContent) { return null; }
 
     public String addDatastreamXMLMetadata(Context context, String pid, String dsLabel, String MdType, InputStream dsInlineMetadata) { return null; }
 */
 
     private String getNextID(String id) {
         // naive impl... just add "1" to the string
         return id + "1";
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
             if (orig.DSControlGrp.equals("M")) {
                     // copy the original datastream, replacing its DSLocation with
                     // the new location (or the old datastream's default dissemination location, if empty or null),
                     // triggering to doCommit that it needs to
                     // be loaded from a new remote location
                     DatastreamManagedContent newds=new DatastreamManagedContent();
                     newds.metadataIdList().addAll(((DatastreamContent) orig).metadataIdList());
                     newds.DatastreamID=orig.DatastreamID;
                     // make sure it has a different id
                     newds.DSVersionID=getNextID(orig.DSVersionID);
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
                         // if location unspecified, use the location of
                         // the datastream on the system, thus making a copy
                         newds.DSLocation="http://" + m_fedoraServerHost + ":"
                                 + m_fedoraServerPort
                                 + "/fedora/get/" + pid + "/fedora-system:3/getItem?itemID="
                                 + datastreamId;
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
                     audit.id="AUDIT" + w.getAuditRecords().size() + 1;
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
                 newds.DSVersionID=getNextID(orig.DSVersionID);
                 newds.DSLabel=dsLabel;
                 newds.DSMIME=orig.DSMIME;
                 Date nowUTC=DateUtility.convertLocalDateToUTCDate(new Date());
                 newds.DSCreateDT=nowUTC;
                 newds.DSControlGrp=orig.DSControlGrp;
                 newds.DSInfoType=orig.DSInfoType;
                 newds.DSState=dsState;
                 //newds.DSState=orig.DSState;
                 if (dsLocation==null || dsLocation.equals("")) {
                     // if location unspecified, use the location of
                     // the datastream on the system, thus making a copy
                     newds.DSLocation="http://" + m_fedoraServerHost + ":"
                             + m_fedoraServerPort
                             + "/fedora/get/" + pid + "/fedora-system:3/getItem?itemID="
                             + datastreamId;
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
                 audit.id="AUDIT" + w.getAuditRecords().size() + 1;
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
             ByteArrayOutputStream bytes=new ByteArrayOutputStream();
             try {
                 StreamUtility.pipeStream(dsContent, bytes, 1024);
             } catch (Exception ex) {
             }
             newds.xmlContent=bytes.toByteArray();
             newds.DatastreamID=orig.DatastreamID;
             // make sure it has a different id
             newds.DSVersionID=getNextID(orig.DSVersionID);
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
             audit.id="AUDIT" + w.getAuditRecords().size() + 1;
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
             DSBindingMap bindingMap, String logMessage, String dissState) throws ServerException {
         m_ipRestriction.enforce(context);
         DOWriter w=null;
         try {
             w=m_manager.getWriter(context, pid);
             fedora.server.storage.types.Disseminator orig=w.GetDisseminator(disseminatorId, null);
                     // copy the original datastream, replacing its DSLocation with
                     // the new location (or the old datastream's default dissemination location, if empty or null),
                     // triggering to doCommit that it needs to
                     // be loaded from a new remote location
                     Disseminator newdiss=new Disseminator();
                     //newdiss.metadataIdList().addAll(((DatastreamContent) orig).metadataIdList());
                     newdiss.dissID=orig.dissID;
                     // make sure it has a different id
                     newdiss.dissVersionID=getNextID(orig.dissVersionID);
                     newdiss.dissLabel=dissLabel;
                     newdiss.dsBindMapID=orig.dsBindMapID;
                     newdiss.dsBindMap=orig.dsBindMap;
                     Date nowUTC=DateUtility.convertLocalDateToUTCDate(new Date());
                     newdiss.dissCreateDT=nowUTC;
                     //newds.DSSize will be computed later
                     newdiss.bDefID=orig.bDefID;
                     newdiss.bDefLabel=orig.bDefLabel;
                     newdiss.bMechID=orig.bMechID;
                     newdiss.bMechLabel=orig.bMechLabel;
                     newdiss.dissState=dissState;
                     newdiss.parentPID=orig.parentPID;
                     newdiss.auditRecordIdList().addAll(orig.auditRecordIdList());
                     // just add the datastream
                     w.addDisseminator(newdiss);
                     // add the audit record
                     fedora.server.storage.types.AuditRecord audit=new fedora.server.storage.types.AuditRecord();
                     audit.id="AUDIT" + w.getAuditRecords().size() + 1;
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
                 // Deleting all versions of a datastream is currently unsupported
                 // FIXME: In the future, this exception should be replaced with an
                 // integrity check.  If the datastream binding info for any version
 				// of a disseminator that uses this would leave a dangling REQUIRED
 				// reference, don't allow it.  If it would leave a dangling UNREQUIRED
 				// reference (in the case of a bucket binding map where the # of datastreams
 				// would still be ok), [[DO WHAT? Undecided]]
                 throw new GeneralException("Purge was aborted because it would"
                         + " result in the permanent deletion of ALL versions "
                         + "of the datastream.");
             }
             // make a log messsage explaining what happened
             String logMessage=getPurgeLogMessage("datastream", datastreamID,
                     start, end, deletedDates);
             Date nowUTC=DateUtility.convertLocalDateToUTCDate(new Date());
             fedora.server.storage.types.AuditRecord audit=new fedora.server.storage.types.AuditRecord();
             audit.id="AUDIT" + w.getAuditRecords().size() + 1;
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
         SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
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
 
     public Calendar[] getDatastreamHistory(Context context, String pid, String datastreamID)
             throws ServerException {
         m_ipRestriction.enforce(context);
         DOReader r=m_manager.getReader(context, pid);
         return dateArrayToCalendarArray(r.getDatastreamVersions(datastreamID));
     }
 
 /*
     public String addDisseminator(Context context, String pid, String bMechPid, String dissLabel, DatastreamBindingMap bindingMap) { return null; }
 
     public void modifyDisseminator(Context context, String pid, String disseminatorId, String bMechPid, String dissLabel, DatastreamBindingMap bindingMap) { }
 
     public Calendar[] purgeDisseminator(Context context, String pid, String disseminatorId, Calendar startDateTime, Calendar endDateTime) { return null; }
 
     public Disseminator getDisseminator(Context context, String pid, String disseminatorId, Calendar asOfDateTime) { return null; }
 
     public Disseminator[] getDisseminators(Context context, String pid, Calendar asOfDateTime) { return null; }
 
     public ComponentInfo[] getDisseminatorHistory(Context context, String pid, String disseminatorId) { return null; }
  */
 
     public String putTempStream(InputStream in)
     	    throws StreamWriteException {
 		// first clean up after old stuff
 		long minStartTime=System.currentTimeMillis()-(60*1000*m_uploadStorageMinutes);
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
 				m_uploadStartTime.remove(id);
 			}
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
           audit.id="AUDIT" + w.getAuditRecords().size() + 1;
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
           audit.id="AUDIT" + w.getAuditRecords().size() + 1;
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
