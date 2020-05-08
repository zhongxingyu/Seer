 package fedora.server.storage;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import fedora.server.Context;
 import fedora.server.Logging;
 import fedora.server.errors.ServerException;
 import fedora.server.errors.StreamIOException;
 import fedora.server.errors.StreamReadException;
 import fedora.server.errors.StreamWriteException;
 import fedora.server.errors.StorageDeviceException;
 import fedora.server.errors.ObjectExistsException;
 import fedora.server.errors.ObjectIntegrityException;
 import fedora.server.errors.ObjectNotFoundException;
 import fedora.server.errors.ValidationException;
 import fedora.server.storage.DefaultDOManager;
 import fedora.server.storage.translation.DOTranslator;
 import fedora.server.storage.types.AuditRecord;
 import fedora.server.storage.types.Datastream;
 import fedora.server.storage.types.DigitalObject;
 import fedora.server.storage.types.Disseminator;
 
 /**
  *
  * <p><b>Title:</b> SimpleDOWriter.java</p>
  * <p><b>Description:</b> A <code>DOWriter</code> that uses a DigitalObject
  * under the hood.</p>
  *
  * <p>This interface supports transaction behavior with the commit(String) and
  * rollBack() methods.  When a DOWriter is instantiated, there is an implicit
  * transaction.  Write methods may be called, but they won't affect the
  * the underlying data store until commit(String) is invoked.  This also has
  * the effect of creating another implicit transaction.  If temporary
  * changes are no longer wanted, rollBack() may be called to return the object
  * to it's original form.  rollBack() is only valid for the current transaction.</p>
  *
  * <p>The read methods of DOWriter reflect on the composition of the object in
  * the context of the current transaction.</p>
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
 public class SimpleDOWriter
         extends SimpleDOReader
         implements DOWriter {
 
     private static ObjectIntegrityException ERROR_PENDING_REMOVAL =
             new ObjectIntegrityException("That can't be done because you said "
                     + "I should remove the object and i assume that's what you "
                     + "want unless you call rollback()");
 
     private static ObjectIntegrityException ERROR_INVALIDATED =
             new ObjectIntegrityException("The handle is no longer valid "
                     + "... this object has already been committed or explicitly"
                     + " invalidated.");
 
     private DigitalObject m_obj;
     private Context m_context;
     private DefaultDOManager m_mgr;
 
     private boolean m_pendingRemoval=false;
     private boolean m_invalidated=false;
 
     public SimpleDOWriter(Context context, DefaultDOManager mgr,
             DOTranslator translator, String shortExportFormat,
             String longExportFormat, String encoding, DigitalObject obj,
             Logging logTarget) {
         super(context, mgr, translator, shortExportFormat, longExportFormat,
                 encoding, obj, logTarget);
         m_context=context;
         m_obj=obj;
         m_mgr=mgr;
     }
 
     public void setState(String state)
             throws ObjectIntegrityException {
         assertNotInvalidated();
         assertNotPendingRemoval();
         m_obj.setState(state);
     }
 
     public void setLabel(String label)
             throws ObjectIntegrityException {
         assertNotInvalidated();
         assertNotPendingRemoval();
         m_obj.setLabel(label);
     }
 
     /**
      * Removes the entire digital object.
      *
      * @throws ServerException If any type of error occurred fulfilling the
      *         request.
      */
     public void remove()
             throws ObjectIntegrityException {
         assertNotInvalidated();
         assertNotPendingRemoval();
         m_pendingRemoval=true;
     }
 
     /**
      * Adds a datastream to the object.
      *
      * @param datastream The datastream.
      * @throws ServerException If any type of error occurred fulfilling the
      *         request.
      */
     public void addDatastream(Datastream datastream)
             throws ServerException {
         assertNotInvalidated();
         assertNotPendingRemoval();
         m_obj.datastreams(datastream.DatastreamID).add(datastream);
     }
 
     /**
      * Adds a disseminator to the object.
      *
      * @param disseminator The disseminator.
      * @throws ServerException If any type of error occurred fulfilling the
      *         request.
      */
     public void addDisseminator(Disseminator disseminator)
             throws ServerException {
         assertNotInvalidated();
         assertNotPendingRemoval();
         m_obj.disseminators(disseminator.dissID).add(disseminator);
     }
 
     /**
      * Removes a datastream from the object.
      *
      * @param id The id of the datastream.
      * @param start The start date (inclusive) of versions to remove.  If
      *        <code>null</code>, this is taken to be the smallest possible
      *        value.
      * @param end The end date (inclusive) of versions to remove.  If
      *        <code>null</code>, this is taken to be the greatest possible
      *        value.
      * @throws ServerException If any type of error occurred fulfilling the
      *         request.
      */
     public Date[] removeDatastream(String id, Date start, Date end)
             throws ServerException {
         assertNotInvalidated();
         assertNotPendingRemoval();
         List versions=m_obj.datastreams(id);
         ArrayList removeList=new ArrayList();
         for (int i=0; i<versions.size(); i++) {
             Datastream ds=(Datastream) versions.get(i);
             boolean doRemove=false;
             if (start!=null) {
                 if (end!=null) {
                     if ( (ds.DSCreateDT.compareTo(start)>=0)
                             && (ds.DSCreateDT.compareTo(end)<=0) ) {
                         doRemove=true;
                     }
                 } else {
                     if (ds.DSCreateDT.compareTo(start)>=0) {
                         doRemove=true;
                     }
                 }
             } else {
                 if (end!=null) {
                     if (ds.DSCreateDT.compareTo(end)<=0) {
                         doRemove=true;
                     }
                 } else {
                     doRemove=true;
                 }
             }
             if (doRemove) {
                 // remove associated audit records...
                 removeObjectAuditRecords(ds.auditRecordIdList());
                 // ...then add this datastream to the datastream to-be-removed list.
                 removeList.add(ds);
             }
         }
         versions.removeAll(removeList);
         // finally, return the dates of each deleted item
         Date[] deletedDates=new Date[removeList.size()];
         for (int i=0; i<removeList.size(); i++) {
             deletedDates[i]=((Datastream) removeList.get(i)).DSCreateDT;
         }
         return deletedDates;
     }
 
     /**
      * Removes a disseminator from the object.
      *
      * @param id The id of the datastream.
      * @param start The start date (inclusive) of versions to remove.  If
      *        <code>null</code>, this is taken to be the smallest possible
      *        value.
      * @param end The end date (inclusive) of versions to remove.  If
      *        <code>null</code>, this is taken to be the greatest possible
      *        value.
      * @throws ServerException If any type of error occurred fulfilling the
      *         request.
      */
     public Date[] removeDisseminator(String id, Date start, Date end)
             throws ServerException {
         assertNotInvalidated();
         assertNotPendingRemoval();
         List versions=m_obj.disseminators(id);
         ArrayList removeList=new ArrayList();
         for (int i=0; i<versions.size(); i++) {
             Disseminator diss=(Disseminator) versions.get(i);
             boolean doRemove=false;
             if (start!=null) {
                 if (end!=null) {
                     if ( (diss.dissCreateDT.compareTo(start)>=0)
                             && (diss.dissCreateDT.compareTo(end)<=0) ) {
                         doRemove=true;
                     }
                 } else {
                     if (diss.dissCreateDT.compareTo(start)>=0) {
                         doRemove=true;
                     }
                 }
             } else {
                 if (end!=null) {
                     if (diss.dissCreateDT.compareTo(end)<=0) {
                         doRemove=true;
                     }
                 } else {
                     doRemove=true;
                 }
             }
             if (doRemove) {
                 // remove associated audit records...
                 //
                 // FIXME: Currently, disseminators have no audit records,
                 // so this step is skipped.  Versioning code is not yet
                 // implemented for disseminators anyway, but when it is,
                 // we need to add an auditRecordIdList to the disseminator
                 // class.  (ADMID is supported for interfaceDefs in METS1.3)
                 //
                 // removeObjectAuditRecords(diss.auditRecordIdList());
                 //
                 // ...then add this disseminator to the disseminator to-be-removed list.
                 removeList.add(diss);
             }
         }
         versions.removeAll(removeList);
         // finally, return the dates of each deleted item
         Date[] deletedDates=new Date[removeList.size()];
         for (int i=0; i<removeList.size(); i++) {
             deletedDates[i]=((Disseminator) removeList.get(i)).dissCreateDT;
         }
         return deletedDates;
     }
 
     /**
      * Remove the audit records with ids given in the list.
      *
      * This is a helper method for removeDatastream and removeDisseminator,
      * which need to clean up associated audit records.
      *
      * @param auditIds a List of audit record ids.
      */
     private void removeObjectAuditRecords(List auditIds) {
         StringBuffer removeAuditIds=new StringBuffer();
         for (int j=0; j<auditIds.size(); j++) {
             String auditId=(String) auditIds.get(j);
             removeAuditIds.append("#" + auditId + "#");
         }
         List objectAuditRecords=m_obj.getAuditRecords();
         ArrayList removeAuditList=new ArrayList();
         for (int j=0; j<objectAuditRecords.size(); j++) {
             AuditRecord objectAuditRecord=(AuditRecord) objectAuditRecords.get(j);
             if (removeAuditIds.indexOf("#" + objectAuditRecord.id + "#")!=-1) {
                 removeAuditList.add(objectAuditRecord);
             }
         }
         objectAuditRecords.removeAll(removeAuditList);
     }
 
     /**
      * Saves the changes thus far to the permanent copy of the digital object.
      *
      * @param logMessage An explanation of the change(s).
      * @throws ServerException If any type of error occurred fulfilling the
      *         request.
      */
     public void commit(String logMessage)
             throws ServerException {
         assertNotInvalidated();
         m_mgr.doCommit(m_context, m_obj, logMessage, m_pendingRemoval);
         invalidate();
     }
 
     public void save() {
     }
 
     public void cancel() {
         // cleanup temp, release lock, and invalidate
     }
 
     public void invalidate() {
         m_invalidated=true;
     }
 
     private void assertNotPendingRemoval()
             throws ObjectIntegrityException {
         if (m_pendingRemoval)
             throw ERROR_PENDING_REMOVAL;
     }
 
     private void assertNotInvalidated()
             throws ObjectIntegrityException {
         if (m_invalidated)
             throw ERROR_INVALIDATED;
     }
 }
