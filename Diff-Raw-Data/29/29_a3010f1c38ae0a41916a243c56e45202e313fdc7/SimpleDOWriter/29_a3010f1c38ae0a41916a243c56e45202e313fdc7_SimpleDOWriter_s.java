 package fedora.server.storage;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import fedora.server.Context;
 import fedora.server.Server;
 import fedora.server.errors.ServerException;
 import fedora.server.errors.ObjectIntegrityException;
 import fedora.server.storage.DefaultDOManager;
 import fedora.server.storage.translation.DOTranslator;
 import fedora.server.storage.types.AuditRecord;
 import fedora.server.storage.types.Datastream;
 import fedora.server.storage.types.DigitalObject;
 import fedora.server.storage.types.Disseminator;
 
 /**
  * A DigitalObject-backed DOWriter.
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
     private boolean m_committed=false;
 
     public SimpleDOWriter(Context context, DefaultDOManager mgr,
             DOTranslator translator,
             String exportFormat, 
             String encoding, DigitalObject obj) {
         super(context, mgr, translator, exportFormat, encoding, obj);
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
 
     public void setDatastreamState(String datastreamID, String dsState)
             throws ServerException {
       assertNotInvalidated();
       assertNotPendingRemoval();
       List allVersions = m_obj.datastreams(datastreamID);
       Iterator dsIter = allVersions.iterator();
 
       // Set all versions of this datastreamID to the specified state
       while (dsIter.hasNext()) {
           Datastream ds = (Datastream) dsIter.next();
           ds.DSState=dsState;
         }
     }
 
     public void setDatastreamVersionable(String datastreamID, boolean versionable)
     throws ServerException {
         assertNotInvalidated();
         assertNotPendingRemoval();
         List allVersions = m_obj.datastreams(datastreamID);
         Iterator dsIter = allVersions.iterator();
 
         // Set all versions of this datastreamID to the specified versionable status
         while (dsIter.hasNext()) {
             Datastream ds = (Datastream) dsIter.next();
             ds.DSVersionable = versionable;
         }
     }      
     
     public void setDisseminatorState(String disseminatorID, String dissState)
            throws ServerException {
       assertNotInvalidated();
       assertNotPendingRemoval();
       List allVersions = m_obj.disseminators(disseminatorID);
       Iterator dissIter = allVersions.iterator();
 
       // Set all versions of this disseminatorID to the specified state
       while (dissIter.hasNext()) {
           Disseminator diss = (Disseminator) dissIter.next();
           diss.dissState=dissState;
       }
 
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
     public void addDatastream(Datastream datastream, boolean addNewVersion)
             throws ServerException {
         assertNotInvalidated();
         assertNotPendingRemoval();
         // use this call to handle versionable
         m_obj.addDatastreamVersion(datastream, addNewVersion);
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
         for (int i=0; i<versions.size(); i++) 
         {
             Datastream ds=(Datastream) versions.get(i);
             boolean doRemove=false;
             if (start!=null) 
             {
                 if (end!=null) 
                 {
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
             if (doRemove) 
             {
                 // Note: We don't remove old audit records by design.
 
                 // add this datastream to the datastream to-be-removed list.
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
                 // Note: We don't remove old audit records by design.
 
                 // add this disseminator to the disseminator to-be-removed list.
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
         m_mgr.doCommit(Server.USE_DEFINITIVE_STORE, m_context, m_obj, logMessage, m_pendingRemoval);
         m_committed=true;
         invalidate();
     }
 
     public void invalidate() {
         m_invalidated=true;
     }
 
     /**
      * Generate a unique id for a datastream.
      */
     public String newDatastreamID() {
         return m_obj.newDatastreamID();
     }
 
     /**
      * Generate a unique id for a datastream version.
      */
     public String newDatastreamID(String dsID) {
         return m_obj.newDatastreamID(dsID);
     }
 
     /**
      * Generate a unique id for a disseminator.
      */
     public String newDisseminatorID() {
         return m_obj.newDisseminatorID();
     }
 
     /**
      * Generate a unique id for a disseminator version.
      */
     public String newDisseminatorID(String dissID) {
         return m_obj.newDisseminatorID(dissID);
     }
 
     /**
      * Generate a unique id for a datastreamBindingMap.
      */
     public String newDatastreamBindingMapID() {
         return m_obj.newDatastreamBindingMapID();
     }
 
     /**
      * Generate a unique id for an audit record.
      */
     public String newAuditRecordID() {
         return m_obj.newAuditRecordID();
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
     
     public boolean isCommitted() {
         return m_committed;
     }
     
     public boolean isNew() {
         return m_obj.isNew();
     }
 }
