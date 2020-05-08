 package fedora.server.resourceIndex;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.jrdf.graph.Triple;
 import org.trippi.TripleIterator;
 import org.trippi.TripleMaker;
 import org.trippi.TrippiException;
 
 import fedora.common.Constants;
 import fedora.server.errors.ResourceIndexException;
 import fedora.server.utilities.DateUtility;
 /**
  * Simple queuing for ResourceIndexImpl
  * @author Edwin Shin
  */
 public class RIQueue implements Constants {
     private List m_triples;
     private Map m_lastModified;
     private Map m_volatile;
     
     public RIQueue() {
         m_triples = new ArrayList();
         m_lastModified = new HashMap();
         m_volatile = new HashMap();
     }
     
     public void clear() {
         m_triples.clear();
         m_lastModified.clear();
         m_volatile.clear();
     }
 
     public List listTriples() {
         return m_triples;
     }
     
     public TripleIterator getTripleIterator() throws ResourceIndexException {
         return new IteratorBasedTripleIterator(listTriples().iterator());
     }
     
     /**
      * Note: Silently drops the Triple if cModel is null or empty.
      * @param queue The list to which the Triple will be added
      * @param digitalObjectURI
      * @param cModel
      * @throws ResourceIndexException
      */
     protected void queueContentModel(String digitalObjectURI, String cModel) throws ResourceIndexException {
         if (cModel == null || cModel.equals("")) {
             return;
         }
         addPlainTriple(digitalObjectURI, 
                        MODEL.CONTENT_MODEL.uri, 
                        cModel);
     }
     
     protected void queueCreatedDate(String subject, Date date) throws ResourceIndexException {
         String dateTime = DateUtility.convertDateToString(date);
         if (dateTime == null || dateTime.equals("")) {
             return;
         }
         addTypedTriple(subject, 
                        MODEL.CREATED_DATE.uri, 
                        dateTime, 
                        XSD.DATE_TIME.uri);
     }
    
     protected void queueDC(String digitalObjectURI, String property, String value) throws ResourceIndexException {
        addPlainTriple(digitalObjectURI, 
                       property, 
                       value);
     }
     
     protected void queueDefinesMethod(String bDefURI, String method) throws ResourceIndexException {
         addPlainTriple(bDefURI, 
                        MODEL.DEFINES_METHOD.uri, 
                        method);
     }
     
     protected void queueDependsOn(String rep1, String rep2) throws ResourceIndexException {
         addTriple(rep1, 
                   MODEL.DEPENDS_ON.uri, 
                   rep2);
     }
     
     protected void queueDissemination(String digitalObjectURI, String dissemination) throws ResourceIndexException {
         addTriple(digitalObjectURI, 
                   VIEW.DISSEMINATES.uri,
                   dissemination);
     }
     
     protected void queueDisseminationType(String dissemination, String dType) throws ResourceIndexException {
         addTriple(dissemination, 
                   VIEW.DISSEMINATION_TYPE.uri, 
                   dType);
     }
     
     protected void queueExternalProperty(String digitalObjectURI, String property, String value) throws ResourceIndexException {
         addPlainTriple(digitalObjectURI, 
                        property, 
                        value);
     }
     
     protected void queueHasDatastream(String digitalObjectURI, String datastreamURI) throws ResourceIndexException {
     	addTriple(digitalObjectURI,
     			  VIEW.HAS_DATASTREAM.uri,
 				  datastreamURI);
     }
 
     protected void queueImplements(String bMechURI, String bDefURI) throws ResourceIndexException {
         addTriple(bMechURI, 
                   MODEL.IMPLEMENTS_BDEF.uri, 
                   bDefURI);
     }
     
     protected void queueIsVolatile(String subject, boolean isVolatile) throws ResourceIndexException {
         Boolean v = new Boolean(isVolatile);
         addPlainTriple(subject, 
                        VIEW.IS_VOLATILE.uri, 
                        v.toString());
         m_volatile.put(subject, v);
     }
     
     /**
      * Note: Silently drops the Triple if label is null or empty.
      * @param queue 
      * @param subject
      * @param label
      * @throws ResourceIndexException
      */
     protected void queueLabel(String subject, String label) throws ResourceIndexException {
         if (label == null || label.equals("")) {
             return;
         }
         addPlainTriple(subject, 
                        MODEL.LABEL.uri, 
                        label);
     }
     
     protected void queueLastModifiedDate(String subject, Date date) throws ResourceIndexException {
         String dateTime = DateUtility.convertDateToString(date);
         if (dateTime == null || dateTime.equals("")) {
             return;
         }
         addTypedTriple(subject, 
                        VIEW.LAST_MODIFIED_DATE.uri, 
                        dateTime, 
                        XSD.DATE_TIME.uri);
         m_lastModified.put(subject, date);
     }
     
     protected void queueMimeType(String dissemination, String mimeType) throws ResourceIndexException {
         addPlainTriple(dissemination, 
                        VIEW.MIME_TYPE.uri, 
                        mimeType);
     }
     
     protected void queueOwner(String subject, String owner) throws ResourceIndexException {
         addPlainTriple(subject, 
                        MODEL.OWNER.uri, 
                        owner);
     }
     
     protected void queueRDFType(String digitalObjectURI, String rdfType) throws ResourceIndexException {
         addTriple(digitalObjectURI, 
                   RDF.TYPE.uri, 
                   rdfType);
     }
     
     protected void queueState(String subject, String state) throws ResourceIndexException {
         addTriple(subject, 
                   MODEL.STATE.uri, 
                   getStateURI(state));
     }
 
     protected void queueUsesBMech(String dataObjectURI, String bMechURI) throws ResourceIndexException {
         addTriple(dataObjectURI, 
                   MODEL.USES_BMECH.uri, 
                   bMechURI);
     }
     
     /**
      * Updates the lastModifiedDate of a dissemination if the dependency's
      * lastModifiedDate is more recent.
      * @param dissemination
      * @param dependency
      * @throws ResourceIndexException
      */
     protected void updateLastModified(String dissemination, String dependency) throws ResourceIndexException {
         Date dissDate = getLastModified(dissemination);
         Date depDate = getLastModified(dependency);
         
         if (depDate == null) {
             return;
         } else if (dissDate == null || depDate.after(dissDate)) {
             String datetime = DateUtility.convertDateToString(dissDate);
             try {
                 Triple t = TripleMaker.createTyped(dissemination, 
                                                     VIEW.LAST_MODIFIED_DATE.uri, 
                                                     datetime, 
                                                     XSD.DATE_TIME.uri);
                 m_triples.remove(t);
                 m_lastModified.remove(dissemination);
                 queueLastModifiedDate(dissemination, depDate);;
             } catch (TrippiException e) {
                 throw new ResourceIndexException(e.getMessage(), e);
             }
         }
     }
     
     protected void updateIsVolatile(String dissemination, String dependency) throws ResourceIndexException {
         // for a dissemination, need to get all dependencies,
         // iterate through volatility?, favoring true?
         Boolean dissV = getIsVolatile(dissemination);
         Boolean depV = getIsVolatile(dependency);
         
         if (depV == null) {
             return;
         } 
         if (dissV == null) {
             queueIsVolatile(dissemination, depV.booleanValue());
         } else if (dissV != depV && depV.booleanValue()) {
             try {
                 Triple t = TripleMaker.createPlain(dissemination, 
                                                    VIEW.IS_VOLATILE.uri, 
                                                    dissV.toString());
                 m_triples.remove(t);
                 m_volatile.remove(dissemination);
                 queueIsVolatile(dissemination, depV.booleanValue());
             } catch (TrippiException e) {
                 throw new ResourceIndexException(e.getMessage(), e);
             }
         }
     }
     
     protected void addTriple(Triple triple) {
         m_triples.add(triple);
     }
     
     private void addTriple(String subject, String predicate, String object) throws ResourceIndexException {
         try {
             m_triples.add(TripleMaker.create(subject, predicate, object));
         } catch (TrippiException e) {
             throw new ResourceIndexException(e.getMessage(), e);
         }
     }
     
     private void addPlainTriple(String subject, String predicate, String object) throws ResourceIndexException {
         try {
             m_triples.add(TripleMaker.createPlain(subject, predicate, object));
         } catch (TrippiException e) {
             throw new ResourceIndexException(e.getMessage(), e);
         }
     }
     
     private void addTypedTriple(String subject, String predicate, String object, String datatype) throws ResourceIndexException {
         try {
             m_triples.add(TripleMaker.createTyped(subject, predicate, object, datatype));
         } catch (TrippiException e) {
             throw new ResourceIndexException(e.getMessage(), e);
         }
     }
     
     private Date getLastModified(String subject) {
         return (Date)m_lastModified.get(subject);
     }
     
     private Boolean getIsVolatile(String subject) {
         return (Boolean)m_volatile.get(subject);
     }
     
     private static String getStateURI(String state) throws ResourceIndexException {
         if (state == null) {
             throw new ResourceIndexException("State cannot be null");
         } else if (state.equalsIgnoreCase("A")) {
             return MODEL.ACTIVE.uri;
         } else if (state.equalsIgnoreCase("I")) {
             return MODEL.INACTIVE.uri;
         } else if (state.equalsIgnoreCase("D")) {
             return MODEL.DELETED.uri;
         } else {
             throw new ResourceIndexException("Unknown state: " + state);
         }
     }
 
     public class IteratorBasedTripleIterator extends TripleIterator {
 
         private Iterator m_iter;
 
         public IteratorBasedTripleIterator(Iterator iter) {
             m_iter = iter;
         }
 
         public boolean hasNext() {
             return m_iter.hasNext();
         }
 
         public Triple next() {
             return (Triple) m_iter.next();
         }
 
         public void close() { }
 
     }
 }
