 package gov.nih.nci.caintegrator2.external.ncia;
 
 import gov.nih.nci.cagrid.cqlquery.Association;
 import gov.nih.nci.cagrid.cqlquery.Attribute;
 import gov.nih.nci.cagrid.cqlquery.CQLQuery;
 import gov.nih.nci.cagrid.cqlquery.Object;
 import gov.nih.nci.cagrid.cqlquery.Predicate;
 import gov.nih.nci.cagrid.cqlquery.QueryModifier;
 import gov.nih.nci.cagrid.cqlresultset.CQLQueryResults;
 import gov.nih.nci.cagrid.cqlresultset.TargetAttribute;
 import gov.nih.nci.cagrid.data.faults.MalformedQueryExceptionType;
 import gov.nih.nci.cagrid.data.faults.QueryProcessingExceptionType;
 import gov.nih.nci.cagrid.data.utilities.CQLQueryResultsIterator;
 import gov.nih.nci.cagrid.introduce.security.client.ServiceSecurityClient;
 import gov.nih.nci.cagrid.ncia.client.NCIACoreServiceClient;
 import gov.nih.nci.caintegrator2.external.ConnectionException;
 import gov.nih.nci.caintegrator2.external.ServerConnectionProfile;
 import gov.nih.nci.ncia.domain.Image;
 import gov.nih.nci.ncia.domain.Patient;
 import gov.nih.nci.ncia.domain.Series;
 import gov.nih.nci.ncia.domain.Study;
 
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.axis.types.URI.MalformedURIException;
 import org.globus.gsi.GlobusCredential;
 
 /**
  * Implementation the NCIASearchService.
  */
 public class NCIASearchServiceImpl extends ServiceSecurityClient implements NCIASearchService {
 
     private ServerConnectionProfile serverConnection;
 
     /**
      * Constructor given a gridServiceURL.
      * @param conn - ServerConnectionProfile for NCIA grid service
      * @throws MalformedURIException - exception.
      * @throws RemoteException - exception.
      */
     public NCIASearchServiceImpl(ServerConnectionProfile conn) throws MalformedURIException, RemoteException {
         this(conn, null);
     }
 
     /**
      * Constructor given a gridServiceURL.
      * @param conn - ServerConnectionProfile for NCIA grid service
      * @param proxy - proxy...
      * @throws MalformedURIException - exception.
      * @throws RemoteException - exception.
      */
     public NCIASearchServiceImpl(ServerConnectionProfile conn, GlobusCredential proxy) 
                                     throws MalformedURIException, RemoteException {
         super(conn.getUrl(), proxy);
         serverConnection = conn;
     }
 
     
     /**
      * {@inheritDoc}
      */
     public List<String> retrieveAllTrialDataProvenanceProjects() throws ConnectionException {
         List<String> trialDataProvenanceProjectsCollection = new ArrayList<String>();
         final CQLQuery query = new CQLQuery();
         Object target = new Object();
         target.setName("gov.nih.nci.ncia.domain.TrialDataProvenance");
         query.setTarget(target);
         QueryModifier distinctProjectModifier = new QueryModifier();
         distinctProjectModifier.setCountOnly(false);
         distinctProjectModifier.setDistinctAttribute("project");
         query.setQueryModifier(distinctProjectModifier);
 
         CQLQueryResults result = connectAndExecuteQuery(query);
         // Iterate Results
         if (result != null) {
             CQLQueryResultsIterator iter2 = new CQLQueryResultsIterator(result);
             while (iter2.hasNext()) {
                 TargetAttribute[] obj = (TargetAttribute[]) iter2.next();
                 //LOGGER.info(obj[0].getValue());
                 trialDataProvenanceProjectsCollection.add(obj[0].getValue());
             }
         } 
         return trialDataProvenanceProjectsCollection;
     }
     
     /**
      * {@inheritDoc}
      */
     public List<Patient> retrievePatientCollectionFromDataProvenanceProject(String provenanceProject) 
     throws ConnectionException {
         List<Patient> patientsCollection = new ArrayList<Patient>();
 
         Attribute att = retrieveAttribute("project", Predicate.EQUAL_TO, provenanceProject);
 
         Association assoc = retrieveAssociation("gov.nih.nci.ncia.domain.TrialDataProvenance", "dataProvenance", att);
 
         CQLQuery fcqlq = retrieveQuery("gov.nih.nci.ncia.domain.Patient", assoc);
 
         CQLQueryResults result = connectAndExecuteQuery(fcqlq);
 
         // Iterate Results
         if (result != null) {
             CQLQueryResultsIterator iter2 = new CQLQueryResultsIterator(result);
             
             while (iter2.hasNext()) {
                 Patient obj = (Patient) iter2.next();
                 patientsCollection.add(obj);
             }
         } 
         return patientsCollection;
     }
     
     /**
      * {@inheritDoc}
      */
     public List<Study> retrieveStudyCollectionFromPatient(String patientId) 
     throws ConnectionException {
         List<Study> studyCollection = new ArrayList<Study>();
 
         Attribute att = retrieveAttribute("patientId", Predicate.EQUAL_TO, patientId);
 
         Association assoc = retrieveAssociation("gov.nih.nci.ncia.domain.Patient", "patient", att);
 
         CQLQuery fcqlq = retrieveQuery("gov.nih.nci.ncia.domain.Study", assoc);
 
         CQLQueryResults result = connectAndExecuteQuery(fcqlq);
 
         // Iterate Results
         if (result != null) {
             CQLQueryResultsIterator iter2 = new CQLQueryResultsIterator(result);
             while (iter2.hasNext()) {
                 Study obj = (Study) iter2.next();
                 studyCollection.add(obj);
             }
         }
 
         return studyCollection;
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Series> retrieveImageSeriesCollectionFromStudy(String studyInstanceUID) 
     throws ConnectionException {
         List<Series> imageSeriesCollection = new ArrayList<Series>();
 
         Attribute att = retrieveAttribute("studyInstanceUID", Predicate.EQUAL_TO, studyInstanceUID);
 
         Association assoc = retrieveAssociation("gov.nih.nci.ncia.domain.Study", "study", att);
 
         CQLQuery fcqlq = retrieveQuery("gov.nih.nci.ncia.domain.Series", assoc);
 
         CQLQueryResults result = connectAndExecuteQuery(fcqlq);
 
         // Iterate Results
         if (result != null) {
             CQLQueryResultsIterator iter2 = new CQLQueryResultsIterator(result);
             while (iter2.hasNext()) {
                 Series obj = (Series) iter2.next();
                 imageSeriesCollection.add(obj);
             }
         } 
 
         return imageSeriesCollection;
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Image> retrieveImageCollectionFromSeries(String seriesInstanceUID) 
     throws ConnectionException {
         List<Image> imageSeriesCollection = new ArrayList<Image>();
 
         Attribute att = retrieveAttribute("seriesInstanceUID", Predicate.EQUAL_TO, seriesInstanceUID);
 
         Association assoc = retrieveAssociation("gov.nih.nci.ncia.domain.Series", "series", att);
 
         CQLQuery fcqlq = retrieveQuery("gov.nih.nci.ncia.domain.Image", assoc);
 
         CQLQueryResults result = connectAndExecuteQuery(fcqlq);
 
         // Iterate Results
         if (result != null) {
             CQLQueryResultsIterator iter2 = new CQLQueryResultsIterator(result);
             while (iter2.hasNext()) {
                 Image obj = (Image) iter2.next();
                 imageSeriesCollection.add(obj);
             }
         } 
 
         return imageSeriesCollection;
     }
     
        
     /**
      * {@inheritDoc}
      */
     
     public boolean validate(String seriesInstanceUID) throws ConnectionException {
         List<Series> imageSeriesCollection = new ArrayList<Series>();
         CQLQuery query = new CQLQuery();
         Object target = new Object();
         target.setName("gov.nih.nci.ncia.domain.Series");
         Attribute symbolAttribute = new Attribute("seriesInstanceUID", Predicate.EQUAL_TO, seriesInstanceUID);
         target.setAttribute(symbolAttribute);
         query.setTarget(target);
         CQLQueryResults result = connectAndExecuteQuery(query);
         if (result != null) {
             CQLQueryResultsIterator iter2 = new CQLQueryResultsIterator(result);
             while (iter2.hasNext()) {
                 Series obj = (Series) iter2.next();
                 imageSeriesCollection.add(obj);
             }
         } 
        return (!imageSeriesCollection.isEmpty());
     }
     
       
     private CQLQuery retrieveQuery(String targetName, Association assoc) {
         final CQLQuery fcqlq = new CQLQuery();
         Object target = new Object();
         target.setName(targetName);
         fcqlq.setTarget(target);
         target.setAssociation(assoc);
         return fcqlq;
     }
     
     private Attribute retrieveAttribute(String name, Predicate predicate, String value) {
         Attribute att = new Attribute();
         att.setName(name);
         att.setPredicate(predicate);
         att.setValue(value);
         return att;
     }
     
     private Association retrieveAssociation(String name, String roleName, Attribute att) {
         Association assoc = new Association();
         assoc.setName(name);
         assoc.setRoleName(roleName);
         assoc.setAttribute(att);
         return assoc;
     }
     
     private CQLQueryResults connectAndExecuteQuery(CQLQuery cqlQuery) throws ConnectionException {
         try {
             return new NCIACoreServiceClient(serverConnection.getUrl()).query(cqlQuery);
         } catch (QueryProcessingExceptionType e) {
             throw new IllegalStateException("Error Processing Query.", e);
         } catch (MalformedQueryExceptionType e) {
             throw new IllegalStateException("Malformed Query.", e);
         } catch (RemoteException e) {
             throw new ConnectionException("Remote Connection Failed.", e);
         } catch (MalformedURIException e) {
           throw new ConnectionException("Malformed URI.", e);
         }
     }
 
 
 }
