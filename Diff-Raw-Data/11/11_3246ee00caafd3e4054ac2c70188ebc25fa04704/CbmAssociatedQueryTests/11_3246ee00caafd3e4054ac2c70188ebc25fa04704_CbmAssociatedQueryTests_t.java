 package org.cagrid.CBM.test;
 
 import gov.nih.nci.cagrid.cqlquery.CQLQuery;
 import gov.nih.nci.cagrid.cqlresultset.CQLQueryResults;
 import gov.nih.nci.cagrid.data.faults.MalformedQueryExceptionType;
 import gov.nih.nci.cagrid.data.faults.QueryProcessingExceptionType;
 import gov.nih.nci.cagrid.data.utilities.CQLQueryResultsIterator;
 import gov.nih.nci.cbm.domain.LogicalModel.CollectionProtocol;
 import gov.nih.nci.cbm.domain.LogicalModel.Institution;
 import gov.nih.nci.cbm.domain.LogicalModel.ParticipantCollectionSummary;
 import gov.nih.nci.cbm.domain.LogicalModel.SpecimenCollectionSummary;
 
 import java.io.InputStream;
 import java.rmi.RemoteException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import org.cagrid.CBM.test.query.RetrieveAllAttributesQueryBuilder;
 import org.cagrid.CBM.test.query.RetrieveAssociationsQueryBuilder;
 import org.junit.Test;
 
 /**
  * @author powersb
  */
 public class CbmAssociatedQueryTests extends CbmTest {
 
 
 	/*
 	 * Test of retrievals with a parent collection protocol object
 	 */
 	@Test
 	public void testRetrieveParticipantStudyFromCollectionProtocol() throws Exception {
 		CbmObject associatedObject = CbmObject.COLLECTION_PROTOCOL;
 		CbmObject targetObject = CbmObject.PARTICIPANT_COLLECTION_SUMMARY;
 		String associatedObjectName = associatedObject.getSimpleName();
 		String associationIdAttr = "id";
 		String associationName = "isEnrolledIn";
 		
 		checkAssociations(associatedObject, targetObject, associatedObjectName,
 				associationIdAttr, associationName);
 
 	}
 	
 	@Test
 	public void testRetrieveInstitutionFromCollectionProtocol() throws Exception {
 		CbmObject associatedObject = CbmObject.INSTITUTION;
 		CbmObject targetObject = CbmObject.COLLECTION_PROTOCOL;
 		String associatedObjectName = associatedObject.getSimpleName();
 		String associationIdAttr = "id";
 		String associationName = "residesAt";
 		
 		checkAssociations(associatedObject, targetObject, associatedObjectName,
 				associationIdAttr, associationName);
 
 	}
 	
 	@Test
 	public void testRetrieveContactFromCollectionProtocol() throws Exception {
 		CbmObject associatedObject = CbmObject.COLLECTION_PROTOCOL;
 		CbmObject targetObject = CbmObject.SPECIMEN_COLLECTION_CONTACT;
 		String associatedObjectName = associatedObject.getSimpleName();
 		String associationIdAttr = "id";
 		String associationName = "isResponsibleFor";
 		
 		checkAssociations(associatedObject, targetObject, associatedObjectName,
 				associationIdAttr, associationName);
 
 	}
 	
 	@Test
 	public void testRetrieveAnnotationsFromCollectionProtocol() throws Exception {
 		CbmObject associatedObject = CbmObject.COLLECTION_PROTOCOL;
 		CbmObject targetObject = CbmObject.ANNOTATION_AVAILABILITY_PROFILE;
 		String associatedObjectName = associatedObject.getSimpleName();
 		String associationIdAttr = "id";
 		String associationName = "providesInformationTo";
 		
 		checkAssociations(associatedObject, targetObject, associatedObjectName,
 				associationIdAttr, associationName);
 
 	}
 	
 	@Test
 	public void testRetrieveAvailabilityFromCollectionProtocol() throws Exception {
 		CbmObject associatedObject = CbmObject.COLLECTION_PROTOCOL;
 		CbmObject targetObject = CbmObject.SPECIMEN_AVAILABILITY_SUMMARY_PROFILE;
 		String associatedObjectName = associatedObject.getSimpleName();
 		String associationIdAttr = "id";
 		String associationName = "restrictsAccessTo";
 		
 		checkAssociations(associatedObject, targetObject, associatedObjectName,
 				associationIdAttr, associationName);
 
 	}
 	
 	
 	/*
 	 * Test of retrievals with a parent participant collection summary object
 	 */
 	
 	@Test
 	public void testRetrieveSpecimenCollectionsFromParticipantSummary() throws Exception {
 		CbmObject associatedObject = CbmObject.PARTICIPANT_COLLECTION_SUMMARY;
 		CbmObject targetObject = CbmObject.SPECIMEN_COLLECTION_SUMMARY;
 		String associatedObjectName = associatedObject.getSimpleName();
 		String associationIdAttr = "id";
 		String associationName = "isCollectedFrom";
 		
 		checkAssociations(associatedObject, targetObject, associatedObjectName,
 				associationIdAttr, associationName);
 
 	}
 	
 	@Test
 	public void testRetrieveRaceFromParticipantSummary() throws Exception {
 		CbmObject associatedObject = CbmObject.PARTICIPANT_COLLECTION_SUMMARY;
 		CbmObject targetObject = CbmObject.RACE;
 		String associatedObjectName = associatedObject.getSimpleName();
 		String associationIdAttr = "id";
 		String associationName = "classifies";
 		
 		checkAssociations(associatedObject, targetObject, associatedObjectName,
 				associationIdAttr, associationName);
 
 	}
 	
/*	@Test
 	public void testRetrieveDiagnosisFromParticipantSummary() throws Exception {
 		CbmObject associatedObject = CbmObject.PARTICIPANT_COLLECTION_SUMMARY;
 		CbmObject targetObject = CbmObject.DIAGNOSIS;
 		String associatedObjectName = associatedObject.getSimpleName();
 		String associationIdAttr = "id";
 		String associationName = "isProvidedTo";
 		
 		checkAssociations(associatedObject, targetObject, associatedObjectName,
 				associationIdAttr, associationName);
 
 	}
 */
 	
 	/*
 	 * Test of retrievals with a parent specimen collection summary object
 	 */
 	@Test
 	public void testRetrievePreservationFromSpecimenSummary() throws Exception {
 		CbmObject associatedObject = CbmObject.SPECIMEN_COLLECTION_SUMMARY;
 		CbmObject targetObject = CbmObject.PRESERVATION;
 		String associatedObjectName = associatedObject.getSimpleName();
 		String associationIdAttr = "id";
 		String associationName = "isAppliedTo";
 		
 		checkAssociations(associatedObject, targetObject, associatedObjectName,
 				associationIdAttr, associationName);
 
 	}

	/*
 	@Test
 	public void testRetrievePatientAgeFromSpecimenSummary() throws Exception {
 		CbmObject associatedObject = CbmObject.SPECIMEN_COLLECTION_SUMMARY;
 		CbmObject targetObject = CbmObject.PATIENT_AGE_GROUP_AT_COLLECTION;
 		String associatedObjectName = associatedObject.getSimpleName();
 		String associationIdAttr = "id";
 		String associationName = "describesPatientAge";
 		
 		checkAssociations(associatedObject, targetObject, associatedObjectName,
 				associationIdAttr, associationName);
 
 	}
	*/
 	
 
 	private void checkAssociations(CbmObject associatedObject,
 			CbmObject targetObject, String associatedObjectName,
 			String associationIdAttr, String associationName) throws Exception,
 			RemoteException, MalformedQueryExceptionType,
 			QueryProcessingExceptionType {
 		
 		List<Object> sampleRecordSet = retrieveAllRecords(associatedObject);
 		int maxRetryCount = 3; // It's possible that not all retrieved records have associated records
 		boolean testPassed = false;
 
 		// Start with the first summary record
 		for(int i = 0; i < maxRetryCount; i++){
 			String id = null;
 			try{
 				switch(associatedObject)
 				{
 				case PARTICIPANT_COLLECTION_SUMMARY:	
 					ParticipantCollectionSummary pcsRecord = (ParticipantCollectionSummary)sampleRecordSet.get(i);
 					id = pcsRecord.getId().toString();
 					break;
 
 				case SPECIMEN_COLLECTION_SUMMARY:	
 					SpecimenCollectionSummary scsRecord = (SpecimenCollectionSummary)sampleRecordSet.get(i);
 					id = scsRecord.getId().toString();
 					break;
 
 				case COLLECTION_PROTOCOL:	
 					CollectionProtocol cpRecord = (CollectionProtocol)sampleRecordSet.get(i);
 					id = cpRecord.getId().toString();
 					break;
 
 				case INSTITUTION:	
 					Institution iRecord = (Institution)sampleRecordSet.get(i);
 					id = iRecord.getId().toString();
 					break;
 
 				default:
 					throw new CbmException("Unsupported associated object reference: " + associatedObject.getSimpleName());
 				}
 
 			}catch(ArrayIndexOutOfBoundsException ae){
 				fail("Cannot properly test.  No " + targetObject.getSimpleName() + " records returned for "  + associatedObject.getSimpleName() + 
 				".  This may mean that references have not been populated properly.");
 			}
 			RetrieveAssociationsQueryBuilder raqBuilder = new RetrieveAssociationsQueryBuilder();
 			
 			CQLQuery query = raqBuilder.getQuery(targetObject, associatedObjectName, associationName, associationIdAttr,
 					id);
 			CQLQueryResults results = serviceClient.query(query);
 			List<Object> values = processResults(results);
 
 			if (values.size() > 0) {
 				// Values found, test passed
 				testPassed = true;
 				break;
 			}
 
 		}
 
 		if (!testPassed) {
 			String errorMsg = "Unable to retrieve a set of " + targetObject.getSimpleName()
 					+ " objects related to a "
 					+ associatedObject.getSimpleName() + " object."
 					+ "\nPlease confirm that the latest version of the CBM service is being used.  "
 					+ "Please visit https://wiki.nci.nih.gov/display/TBPT/Common+Biorepository+Model+%28CBM%29 for more information";
 			fail(errorMsg);
 		}
 	}
 
 	/**
 	 * Retrieve all records for the given object. This tests that records from
 	 * each object can be retrieved. In reality, this method really just
 	 * retrieves the first 1000 records for each object due to the build in page
 	 * size of caCORE
 	 * 
 	 * @param theObject
 	 * @throws Exception
 	 * @throws RemoteException
 	 * @throws MalformedQueryExceptionType
 	 * @throws QueryProcessingExceptionType
 	 */
 	private List<Object> retrieveAllRecords(CbmObject theObject)
 			throws Exception {
 		RetrieveAllAttributesQueryBuilder builder = new RetrieveAllAttributesQueryBuilder();
 		CQLQuery query = builder.getQuery(theObject);
 		CQLQueryResults results = serviceClient.query(query);
 		List<Object> values = processResults(results);
 		// TODO: Do we want an error here?
 		// if(values.size() < 1){
 		// throw new Exception("No records found for object " +
 		// theObject.getSimpleName());
 		// }
 		return values;
 	}
 
 	private List<Object> processResults(CQLQueryResults results)
 			throws Exception {
 
 		InputStream resourceAsStream = CbmCodeListTests.class
 				.getResourceAsStream("client-config.wsdd");
 		Iterator<?> iter = new CQLQueryResultsIterator(results,
 				resourceAsStream);
 
 		List<Object> remoteValues = new Vector<Object>();
 
 		// Check that all retrieved values are supported by the reference code
 		// list while
 		while (iter.hasNext()) {
 			Object rawValue = iter.next();
 
 			remoteValues.add(rawValue);
 
 		}
 		return remoteValues;
 	}
 }
