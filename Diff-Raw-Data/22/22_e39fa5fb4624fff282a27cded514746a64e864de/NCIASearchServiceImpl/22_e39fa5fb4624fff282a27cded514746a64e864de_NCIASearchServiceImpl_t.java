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
     private NBIAVersionEnum nbiaVersion;
 
     /**
      * Constructor given a gridServiceURL.
      * @param conn - ServerConnectionProfile for NCIA grid service
      * @param nbiaVersion - version of NBIA.
      * @throws MalformedURIException - exception.
      * @throws RemoteException - exception.
      */
     public NCIASearchServiceImpl(ServerConnectionProfile conn, NBIAVersionEnum nbiaVersion) 
         throws MalformedURIException, RemoteException {
         this(conn, nbiaVersion, null);
     }
 
     /**
      * Constructor given a gridServiceURL.
      * @param conn - ServerConnectionProfile for NCIA grid service
      * @param nbiaVersion - version of NBIA.
      * @param proxy - proxy...
      * @throws MalformedURIException - exception.
      * @throws RemoteException - exception.
      */
     public NCIASearchServiceImpl(ServerConnectionProfile conn, NBIAVersionEnum nbiaVersion,
             GlobusCredential proxy) throws MalformedURIException, RemoteException {
         super(conn.getUrl(), proxy);
         serverConnection = conn;
         this.nbiaVersion = nbiaVersion;
     }
     
     /**
      * {@inheritDoc}
      */
     public List<String> retrieveAllCollectionNameProjects() throws ConnectionException {
         List<String> collectionNameProjectsCollection = new ArrayList<String>();
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
                 collectionNameProjectsCollection.add(obj[0].getValue());
             }
         } 
         return collectionNameProjectsCollection;
     }
     
     /**
      * {@inheritDoc}
      */
     public List<Patient> retrievePatientCollectionFromCollectionNameProject(String collectionNameProject) 
     throws ConnectionException {
         CQLQuery fcqlq = retrieveQueryForPatients(collectionNameProject);
         CQLQueryResults result = connectAndExecuteQuery(fcqlq);
         return iterateAndRetrieveResults(result, Patient.class);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<String> retrievePatientCollectionIdsFromCollectionNameProject(String collectionNameProject) 
     throws ConnectionException {
         CQLQuery fcqlq = retrieveQueryForPatients(collectionNameProject);
         fcqlq.setQueryModifier(retrieveQueryModifierForProperty("patientId"));
         CQLQueryResults result = connectAndExecuteQuery(fcqlq);
         return iterateAndRetrieveResults(result, String.class);
     }
 
     
     private CQLQuery retrieveQueryForPatients(String collectionNameProject) {
         Attribute att = retrieveAttribute("project", Predicate.EQUAL_TO, collectionNameProject);
         Association assoc = retrieveAssociation("gov.nih.nci.ncia.domain.TrialDataProvenance", "dataProvenance", att);
         return retrieveQuery("gov.nih.nci.ncia.domain.Patient", assoc);
     }
     
     /**
      * {@inheritDoc}
      */
     public List<Study> retrieveStudyCollectionFromPatient(String patientId) 
     throws ConnectionException {
         CQLQuery fcqlq = retrieveQueryForStudies(patientId);
         CQLQueryResults result = connectAndExecuteQuery(fcqlq);
         return iterateAndRetrieveResults(result, Study.class);
     }
     
     /**
      * {@inheritDoc}
      */
     public List<String> retrieveStudyCollectionIdsFromPatient(String patientId) 
     throws ConnectionException {
         CQLQuery fcqlq = retrieveQueryForStudies(patientId);
         fcqlq.setQueryModifier(retrieveQueryModifierForProperty("studyInstanceUID"));
         CQLQueryResults result = connectAndExecuteQuery(fcqlq);
         return iterateAndRetrieveResults(result, String.class);
     }
 
     private CQLQuery retrieveQueryForStudies(String patientId) {
         Attribute att = retrieveAttribute("patientId", Predicate.EQUAL_TO, patientId);
         Association assoc = retrieveAssociation("gov.nih.nci.ncia.domain.Patient", "patient", att);
         return retrieveQuery("gov.nih.nci.ncia.domain.Study", assoc);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Series> retrieveImageSeriesCollectionFromStudy(String studyInstanceUID) 
     throws ConnectionException {
         CQLQuery fcqlq = retrieveQueryForImageSeries(studyInstanceUID);
         CQLQueryResults result = connectAndExecuteQuery(fcqlq);
         return iterateAndRetrieveResults(result, Series.class);
     }
     
     /**
      * {@inheritDoc}
      */
     public List<String> retrieveImageSeriesCollectionIdsFromStudy(String studyInstanceUID) 
     throws ConnectionException {
         CQLQuery fcqlq = retrieveQueryForImageSeries(studyInstanceUID);
         fcqlq.setQueryModifier(retrieveQueryModifierForProperty(nbiaVersion.getSeriesIdAtt()));
         CQLQueryResults result = connectAndExecuteQuery(fcqlq);
         return iterateAndRetrieveResults(result, String.class);
     }
 
 
     private CQLQuery retrieveQueryForImageSeries(String studyInstanceUID) {
         Attribute att = retrieveAttribute("studyInstanceUID", Predicate.EQUAL_TO, studyInstanceUID);
         Association assoc = retrieveAssociation("gov.nih.nci.ncia.domain.Study", "study", att);
         return retrieveQuery("gov.nih.nci.ncia.domain.Series", assoc);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Image> retrieveImageCollectionFromSeries(String seriesInstanceUID) 
     throws ConnectionException {
         CQLQuery fcqlq = retrieveQueryForImages(seriesInstanceUID);
         CQLQueryResults result = connectAndExecuteQuery(fcqlq);
         return iterateAndRetrieveResults(result, Image.class);
     }
     
     /**
      * {@inheritDoc}
      */
     public List<String> retrieveImageCollectionIdsFromSeries(String seriesInstanceUID) 
     throws ConnectionException {
         CQLQuery fcqlq = retrieveQueryForImages(seriesInstanceUID);
         fcqlq.setQueryModifier(retrieveQueryModifierForProperty("sopInstanceUID"));
         CQLQueryResults result = connectAndExecuteQuery(fcqlq);
         return iterateAndRetrieveResults(result, String.class);
     }
 
     private CQLQuery retrieveQueryForImages(String seriesInstanceUID) {
         Attribute att = retrieveAttribute(nbiaVersion.getSeriesIdAtt(), Predicate.EQUAL_TO, seriesInstanceUID);
         Association assoc = retrieveAssociation("gov.nih.nci.ncia.domain.Series", "series", att);
         return retrieveQuery("gov.nih.nci.ncia.domain.Image", assoc);
     }
 
     /**
      * {@inheritDoc}
      */
     public Image retrieveRepresentativeImageBySeries(String seriesInstanceUID)
     throws ConnectionException {
         try {
             return getClient().getRepresentativeImageBySeries(seriesInstanceUID);
         } catch (RemoteException e) {
             // TODO - 5/27/09 Ngoc, temporary ignore this exception because this method is only available on Dev
             return null;
             //throw new ConnectionException("Remote Connection Failed.", e);
         }
     }
 
     private NCIACoreServiceClient getClient() throws ConnectionException {
         try {
             return new NCIACoreServiceClient(serverConnection.getUrl());
         } catch (RemoteException e) {
             throw new ConnectionException("Remote Connection Failed.", e);
         } catch (MalformedURIException e) {
           throw new ConnectionException("Malformed URI.", e);
         }
     }
        
     /**
      * {@inheritDoc}
      */
     
     public boolean validate(String seriesInstanceUID) throws ConnectionException {
         CQLQuery query = new CQLQuery();
         Object target = new Object();
         target.setName("gov.nih.nci.ncia.domain.Series");
         Attribute symbolAttribute = new Attribute(nbiaVersion.getSeriesIdAtt(), Predicate.EQUAL_TO, seriesInstanceUID);
         target.setAttribute(symbolAttribute);
         query.setTarget(target);
         CQLQueryResults result = connectAndExecuteQuery(query);
         return !iterateAndRetrieveResults(result, Series.class).isEmpty();
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
             return getClient().query(cqlQuery);
         } catch (QueryProcessingExceptionType e) {
             throw new IllegalStateException("Error Processing Query.", e);
         } catch (MalformedQueryExceptionType e) {
             throw new IllegalStateException("Malformed Query.", e);
         } catch (RemoteException e) {
             throw new ConnectionException("Remote Connection Failed.", e);
         }
     }
 
     @SuppressWarnings({"unchecked", "PMD.UnusedFormalParameter" }) // Generic type.
     private <T> List<T> iterateAndRetrieveResults(CQLQueryResults result, Class<T> clazz) {
         // Iterate Results
         List<T> resultsCollection = new ArrayList<T>();
         
         if (result != null) {
             CQLQueryResultsIterator iter2 = new CQLQueryResultsIterator(result);
             
             while (iter2.hasNext()) {
                 T obj = (T) iter2.next();
                 if (String.class.equals(clazz)) {
                     TargetAttribute[] targetAttributeArray = (TargetAttribute[]) obj;
                     obj = (T) targetAttributeArray[0].getValue();
                 }
                 resultsCollection.add(obj);
             }
         } 
         return resultsCollection;
     }
     
     private QueryModifier retrieveQueryModifierForProperty(String property) {
         QueryModifier queryModifier = new QueryModifier();
         queryModifier.setAttributeNames(new String[]{property});
         return queryModifier;
     }
 
    @Override
    public String[] getSOPInstanceUIDsFromSeriesInstanceUIDNew(String string) throws ConnectionException {
        try {
            return getClient().getSOPInstanceUIDsFromSeriesInstanceUID(string);
        } catch (RemoteException e) {
            throw new ConnectionException("Remote Connection Failed.", e);
        }
    }

 }
