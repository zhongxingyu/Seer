 package fedora.server.storage;
 
 import java.io.ByteArrayOutputStream;
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 
 import fedora.server.errors.ObjectExistsException;
 import fedora.server.errors.ObjectIntegrityException;
 import fedora.server.errors.ObjectNotFoundException;
 import fedora.server.errors.StorageDeviceException;
 import fedora.server.errors.StreamIOException;
 import fedora.server.errors.StreamReadException;
 import fedora.server.errors.StreamWriteException;
 import fedora.server.errors.ValidationException;
 import fedora.server.storage.types.AuditRecord;
 import fedora.server.storage.types.Datastream;
 import fedora.server.storage.types.Disseminator;
 import fedora.server.storage.types.BasicDigitalObject;
 import fedora.server.storage.types.*;
 
 /**
  * A <code>DOWriter</code> for working with the definitive copy of a
  * digital object in the reference implementation.
  * <p></p>
  * This implementation stores the entire digital object in a 
  * <code>TestStreamStorage</code>, while allowing the periodic saving of 
  * non-committed changes to an additional <code>TestStreamStorage</code>.
  * <p></p>
  * The serialization and deserialization formats used by instances of this 
  * class are configured by means of the constructor's exportSerializer,
  * importDeserializer, and storageSerializer and storageDeserializer.
  * <pre>
  * method        serializer         deserializer   
  * ---------     -----------        ------------- 
  * set(...)      storageSerializer  importDeserializer 
  * commit(...)   storageSerializer  N/A
  * get(...)      exportSerializer   storageDeserializer
  * </pre>
  * In the case where storageSerializer is the same as exportSerializer
  * (compared via Object.equals()), no translation is done during get(...)
  * because it's not needed.
  * <p></p>
  * @author cwilper@cs.cornell.edu
  */
 public class DefinitiveDOWriter
         implements DOWriter {
 
     private BasicDigitalObject m_obj;
     private boolean m_pendingRemoval;
     private boolean m_pendingSave;
     private boolean m_pendingCommit;
     private boolean m_removed;
     
     private static ObjectIntegrityException ERROR_PENDING_REMOVAL =
             new ObjectIntegrityException("that can't be done because you said "
                     + "i should remove the object and i assume that's what you "
                     + "want unless you call rollback()");
                     
     private static ObjectIntegrityException ERROR_REMOVED =
             new ObjectIntegrityException("the handle is no longer valid "
                     + "because the object has been removed and the change "
                     + "has been committed.");
     
     private TestStreamStorage m_storage;
     private TestStreamStorage m_tempStorage;
     private StreamValidator m_validator;
     private DODeserializer m_importDeserializer;
     private DOSerializer m_storageSerializer;
     private DODeserializer m_storageDeserializer;
     private DOSerializer m_exportSerializer;
 
     /**
      * Constructs a DOWriter as a handle on an existing digital object.
      * If workingCopy==true, the working copy area is examined for
      * a copy of the object pending commit or removal, and if it doesn't
      * find one, it works from the definitive copy (acts as if workingCopy was
      * false)
      */
     public DefinitiveDOWriter(String pid, TestStreamStorage storage, 
             TestStreamStorage tempStorage, StreamValidator validator,
             DODeserializer importDeserializer, DOSerializer storageSerializer,
             DODeserializer storageDeserializer, DOSerializer exportSerializer,
             boolean workingCopy) 
             throws StorageDeviceException, ObjectNotFoundException,
             ObjectIntegrityException, StreamIOException, StreamReadException {
         m_obj=new BasicDigitalObject();
         m_storage=storage;
         m_tempStorage=tempStorage;
         m_validator=validator;
         m_importDeserializer=importDeserializer;
         m_storageSerializer=storageSerializer;
         m_storageDeserializer=storageDeserializer;
         m_exportSerializer=exportSerializer;
         boolean initialized=false;
         if (workingCopy) {
             try {
                 m_storageDeserializer.deserialize(m_tempStorage.retrieve(pid + "-pendingCommit"), m_obj);
                 // it was found, and it's pending commit...init it as such
                 m_removed=false;
                 m_pendingRemoval=false;
                 makeDirty();
                 initialized=true;
             } catch (ObjectNotFoundException onfe) {
                 try {
                     InputStream in=m_tempStorage.retrieve(pid + "-pendingRemoval");
                     // it was found, and it's pending removal...init it as such
                     try {
                     in.close();
                     } catch (IOException ioe) { }
                     m_removed=false;
                     m_pendingRemoval=true;
                     makeDirty();
                     initialized=true;
                 } catch (ObjectNotFoundException onfe2) {
                     // it wasnt found... so we should load from permanent
                     // source (this will happen after exit from this block
                     // because initialized is false
                 }
             }
         }
         if (!initialized) {
             m_storageDeserializer.deserialize(m_storage.retrieve(pid), m_obj);
             m_pendingCommit=false;
             m_pendingSave=false;
             m_pendingRemoval=false;
             m_removed=false;
         }
         if (!m_pendingRemoval) {
             if (!pid.equals(m_obj.getPid())) {
                 throw new ObjectIntegrityException("While getting a DOWriter for the "
                         + "pre-existing object '" + pid + "', it was found but "
                         + "after deserializing it, it has a different PID, '"
                         + m_obj.getPid() + "'.");
             }
         }
     }
     
     /**
      * Constructs a DOWriter as a handle on a new digital object.
      */
     public DefinitiveDOWriter(String pid, TestStreamStorage storage, 
             TestStreamStorage tempStorage, StreamValidator validator,
             DODeserializer importDeserializer, DOSerializer storageSerializer,
             DODeserializer storageDeserializer, DOSerializer exportSerializer,
             InputStream initialContent, boolean useContentPid) 
             throws ObjectIntegrityException, 
             StreamIOException, StreamReadException {
         m_obj=new BasicDigitalObject();
         m_storage=storage;
         m_tempStorage=tempStorage;
         m_validator=validator;
         m_importDeserializer=importDeserializer;
         m_storageSerializer=storageSerializer;
         m_storageDeserializer=storageDeserializer;
         m_exportSerializer=exportSerializer;
         m_pendingRemoval=false;
         m_removed=false;
         set(initialContent);
         if (!useContentPid) {
             m_obj.setPid(pid);
         }
     }
 
     /**
      * Sets the content of the entire digital object.
      *
      * @param content A stream of encoded content of the digital object.
      */
     public void set(InputStream content) 
             throws ObjectIntegrityException, StreamIOException, 
             StreamReadException {
         assertNotRemoved();
         assertNotPendingRemoval();
         m_importDeserializer.deserialize(content, m_obj);
         makeDirty();
     }
 
     /**
      * Sets the state of the entire digital object.
      *
      * @param state The state.
      */
     public void setState(String state) 
             throws ObjectIntegrityException {
         assertNotRemoved();
         assertNotPendingRemoval();
         m_obj.setState(state);
         makeDirty();
     }
 
     /**
      * Sets the label of the digital object.
      *
      * @param label The label.
      */
     public void setLabel(String label) 
             throws ObjectIntegrityException {
         assertNotRemoved();
         assertNotPendingRemoval();
         m_obj.setLabel(label);
         makeDirty();
     }
 
     /**
      * Removes the entire digital object.
      *
      */    
     public void remove() 
             throws ObjectIntegrityException {
         assertNotRemoved();
         assertNotPendingRemoval();
         m_pendingRemoval=true;
         makeDirty();
     }
 
     /**
      * Adds a datastream to the object.
      *
      * @param datastream The datastream.
      * @return An internally-unique datastream id.
      */
     public String addDatastream(Datastream datastream) 
             throws ObjectIntegrityException {
         assertNotRemoved();
         assertNotPendingRemoval();
         makeDirty();
         return null;
     }
 
     /**
      * Adds a disseminator to the object.
      *
      * @param disseminator The disseminator.
      * @return An internally-unique disseminator id.
      */
     public String addDisseminator(Disseminator disseminator) 
             throws ObjectIntegrityException {
         assertNotRemoved();
         assertNotPendingRemoval();
         makeDirty();
         return null;
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
      */
     public void removeDatastream(String id, Date start, Date end) 
             throws ObjectIntegrityException {
         assertNotRemoved();
         assertNotPendingRemoval();
         makeDirty();
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
      */
     public void removeDisseminator(String id, Date start, Date end)
             throws ObjectIntegrityException {
         assertNotRemoved();
         assertNotPendingRemoval();
         makeDirty();
     }
 
     /**
      * Saves the changes thus far to the permanent copy of the digital object.
      *
      * @param logMessage An explanation of the change(s).
      */
     public void commit(String logMessage)
             throws ObjectIntegrityException {
         assertNotRemoved();
         if (save()) {
             AuditRecord a=new AuditRecord();
             a.id="REC1024";
             a.processType="API-M"; 
             a.action="Don't know"; 
             a.responsibility="You"; 
             a.date=new Date();
            a.justification=logMessage; 
             m_obj.getAuditRecords().add(a);
             
             // replicate
             
             // reflect changes from temp copy to perm copy
             m_pendingCommit=false;
         }
     }
 
     /**
      * Clears the temporary storage area of changes to this object.
      * <p></p>
      * Subsequent calls will behave as if the changes made thus far never 
      * happened.
      *
      */
     public void rollBack()
             throws ObjectIntegrityException {
         assertNotRemoved();
         m_pendingCommit=false;
         m_pendingSave=false; // i think
     }
     
     public InputStream GetObjectXML()
             throws ObjectIntegrityException, StreamIOException {
         assertNotRemoved();
         assertNotPendingRemoval();
         ByteArrayOutputStream bytes=new ByteArrayOutputStream();
         m_exportSerializer.serialize(m_obj, bytes);
         return new ByteArrayInputStream(bytes.toByteArray());
     }
 
     public String GetObjectState()
             throws ObjectIntegrityException {
         assertNotRemoved();
         assertNotPendingRemoval();
         return m_obj.getState();
     }
 
     public InputStream ExportObject()
             throws ObjectIntegrityException, StreamIOException {
         assertNotRemoved();
         assertNotPendingRemoval();
         return GetObjectXML();
     }
 
     public String GetObjectPID()
             throws ObjectIntegrityException {
         assertNotRemoved(); // be a little forgiving of pending removal
         return m_obj.getPid();
     }
 
     public String GetObjectLabel()
             throws ObjectIntegrityException {
         assertNotRemoved();
         assertNotPendingRemoval();
         return m_obj.getLabel();
     }
 
     public String[] ListDatastreamIDs(String state)
             throws ObjectIntegrityException {
         assertNotRemoved();
         assertNotPendingRemoval();
         Iterator iter=m_obj.datastreamIdIterator();
         ArrayList al=new ArrayList();
         while (iter.hasNext()) {
             al.add((String) iter.next());
         }
         iter=al.iterator();
         String[] out=new String[al.size()];
         int i=0;
         while (iter.hasNext()) {
             out[i]=(String) iter.next();
             i++;
         }
         return out;
     }
 
     public Datastream[] GetDatastreams(Date versDateTime)
             throws ObjectIntegrityException {
         assertNotRemoved();
         assertNotPendingRemoval();
         return new Datastream[0]; 
     }
 
     public Datastream GetDatastream(String datastreamID, Date versDateTime)
             throws ObjectIntegrityException {
         assertNotRemoved();
         assertNotPendingRemoval();
         return new Datastream();
     }
 
     public Disseminator[] GetDisseminators(Date versDateTime)
             throws ObjectIntegrityException {
         assertNotRemoved();
         assertNotPendingRemoval();
         return new Disseminator[0];
     }
 
     public String[] ListDisseminatorIDs(String state)
             throws ObjectIntegrityException {
         assertNotRemoved();
         assertNotPendingRemoval();
         
         assertNotRemoved();
         assertNotPendingRemoval();
         Iterator iter=m_obj.disseminatorIdIterator();
         ArrayList al=new ArrayList();
         while (iter.hasNext()) {
             al.add((String) iter.next());
         }
         iter=al.iterator();
         String[] out=new String[al.size()];
         int i=0;
         while (iter.hasNext()) {
             out[i]=(String) iter.next();
             i++;
         }
         return out;
     }
 
     public Disseminator GetDisseminator(String disseminatorID, Date versDateTime)
             throws ObjectIntegrityException {
         assertNotRemoved();
         assertNotPendingRemoval();
         return new Disseminator();
     }
 
     // Returns PIDs of Behavior Definitions to which object subscribes
     public String[] GetBehaviorDefs(Date versDateTime)
             throws ObjectIntegrityException {
         assertNotRemoved();
         assertNotPendingRemoval();
         return new String[0];
     }
 
     // Returns list of methods that Behavior Mechanism implements for a BDef
     public MethodDef[] GetBMechMethods(String bDefPID, Date versDateTime)
             throws ObjectIntegrityException {
         assertNotRemoved();
         assertNotPendingRemoval();
         return new MethodDef[0];
     }
 
     // Overloaded method: returns InputStream as alternative
     public InputStream GetBMechMethodsWSDL(String bDefPID, Date versDateTime)
             throws ObjectIntegrityException {
         assertNotRemoved();
         assertNotPendingRemoval();
         return new ByteArrayInputStream(new byte[0]);
     }
 
     public DSBindingMapAugmented[] GetDSBindingMaps(Date versDateTime)
             throws ObjectIntegrityException { 
         assertNotRemoved();
         assertNotPendingRemoval();
         return new DSBindingMapAugmented[0];
     }
 
     /** this should go in DOReader, methinks */
     public void validate(String validationType)
             throws ObjectIntegrityException {
         assertNotRemoved();
         assertNotPendingRemoval();
     }
 
     // i don't think this needs to be public as long as save() is,
     // but making it public might be nice
     public boolean pendingCommit()
             throws ObjectIntegrityException {
         assertNotRemoved();
         return m_pendingCommit;
     }
     
     private void makeDirty() {
         m_pendingCommit=true;
         m_pendingSave=true;
     }
    
     // saves if it hasn't been saved in this state yet
     public boolean save()
             throws ObjectIntegrityException {
         assertNotRemoved();
         if (m_pendingSave) {
            if (m_pendingRemoval) {
                // flag that removal is needed by removing the temp copy
                // and creating a 0000-pendingRemoval item
            } else {
                
                // serialize to temp copy as 0000-pendingCommit
            }
            m_pendingSave=false;
            return true;
         }
         return false;
     }
     
     private void assertNotPendingRemoval()
             throws ObjectIntegrityException {
         if (m_pendingRemoval)
             throw ERROR_PENDING_REMOVAL;
     }
     
     private void assertNotRemoved() 
             throws ObjectIntegrityException {
         if (m_removed)
             throw ERROR_REMOVED;
     }
     
     public void finalize() throws ObjectIntegrityException {
         save();
     }
 }
