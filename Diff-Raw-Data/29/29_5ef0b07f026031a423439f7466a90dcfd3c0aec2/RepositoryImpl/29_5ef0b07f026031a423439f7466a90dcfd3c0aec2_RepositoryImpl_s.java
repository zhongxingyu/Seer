 package org.osehra.das.repo.bluebutton;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.joda.time.LocalDate;
 import org.osehra.das.BeanUtils;
 import org.osehra.das.C32Document;
 import org.osehra.das.wrapper.nwhin.C32DocumentEntity;
 
 /**
  * Executes the business logic on the services side of the JMS Queue.
  * 
  * @author Steve Monson
  *
  */
 public class RepositoryImpl extends AbstractC32DaoAware implements Repository {
 	protected MessageSendable messageSender;
 	protected static final Log logger = LogFactory.getLog(RepositoryImpl.class);
 	
 	public MessageSendable getMessageSender() {
 		return messageSender;
 	}
 
 	public void setMessageSender(MessageSendable messageSender) {
 		this.messageSender = messageSender;
 	}
 
 	@Override
 	public List<DocStatus> getStatus(String patientId, String patientName) {
 		List<C32DocumentEntity> docList = getAllStoredDocuments(patientId);
 
 		Date today = new Date();
 		
 		if (getDocumentByDate(today, docList)==null) {
 			saveIncompleteDocument(today, patientId);
 			sendMessageToRetrieve(today, patientId, patientName);
 			docList = getAllStoredDocuments(patientId);	
 		}
 		docList = getLimitedStatuses(today, docList);
 		return getDocStatuses(docList);
 	}
 	
 	@Override
 	public C32Document getDocument(Date docDate, String patientId) {
 		List<C32DocumentEntity> docList = getAllStoredDocuments(patientId); 
 		docList = getCompletedDocuments(docList);
 		C32DocumentEntity doc = getDocumentByDate(docDate, docList);
 
 		if (doc==null) {
 			throw new RuntimeException("document for date:" + docDate + " & id:" + patientId + " does not exist");
 		}
 		
 		return getC32Doc(doc);
 	}
 	
 	/**
 	 * Creates a place-holder entry in the database indicating that a record has been requested, with an incomplete status.
 	 * 
 	 * @param docDate		The date of the document to make an entry for.
 	 * @param ptId			The identifier of the patient to make an entry for.
 	 */
 
 	protected void saveIncompleteDocument(Date docDate, String ptId) {
 		getC32DocumentDao().insert(new C32DocumentEntity(ptId, ptId, new java.sql.Timestamp(docDate.getTime()), BlueButtonConstants.INCOMPLETE_STATUS_STRING));
 	}
 	/**
 	 * BASE64 encodes the XML on a document passed to it.
 	 * 
 	 * @param		A C32 Document Entity.
 	 * @return		A C32 Document Entity.
 	 */
 	protected C32Document getC32Doc(C32DocumentEntity doc) {
 		C32Document newDoc = new C32Document();
 		String xmlDoc = doc.getDocument();
 		if (xmlDoc==null) {
 			xmlDoc = "";
 		}
 		newDoc.setDocument(Base64.encodeBase64String(xmlDoc.getBytes()));
 		newDoc.setPatientId(doc.getIcn());
 		return newDoc;
 	}
 	
 	/**
 	 * Returns a list of documents either completed or in progress for the date and list input.
 	 * 
 	 * @param today		The date to compare look up documents for.
 	 * @param docList	The list of documents to be filtered.
 	 * @return			Returns the filtered list of documents.
 	 */
 
 	protected C32DocumentEntity getDocumentByDate(Date today, List<C32DocumentEntity> docList) {
 		if (docList!=null && !docList.isEmpty()) {
 			for (int i=0;i<docList.size();i++) {
 
 				LocalDate fileDate = LocalDate.fromDateFields(docList.get(i).getCreateDate());
 				LocalDate todayDate = LocalDate.fromDateFields(today);
 				
 				if (todayDate.equals(fileDate)) {
 						return docList.get(i);
 				}
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Takes a list of documents and filters them to only returns items where they are not incomplete.
 	 * 
 	 * @param docList	The list of documents to be filtered.
 	 * @return			Returns the filtered list of documents.
 	 */
 	protected List<C32DocumentEntity> getCompletedDocuments(List<C32DocumentEntity> docList) {
 		if (docList!=null && !docList.isEmpty()) {	
 			int arraySize = docList.size();
 			List<C32DocumentEntity> filteredList = new ArrayList<C32DocumentEntity>(arraySize);	
 
 			for (int i=0;i<docList.size(); i++) {
				if (BeanUtils.equalsNullSafe(docList.get(i).getDocument().toString(), BlueButtonConstants.INCOMPLETE_STATUS_STRING) == false) {
 					filteredList.add(docList.get(i));
 				}
 			}
 			return filteredList;	
 		}
 		return null;		
 	}
 	
 	/**
 	 * Retrieves all of the stored documents from the database for the patient.
 	 * 
 	 * @param patientId		Patient identifier of the individual.
 	 * @return				A list of all documents available in the database for the patient.
 	 */
 
 	protected List<C32DocumentEntity> getAllStoredDocuments(String patientId) {
 		return getC32DocumentDao().getAllDocuments(patientId);
 	}
 	
 	/**
 	 * Constructs the final list of statuses from the list of available documents for publishing to service call.
 	 * 
 	 * @param docList
 	 * @return
 	 */
 
 	protected List<DocStatus> buildStatusList(List<C32DocumentEntity> docList) {
 		if (docList==null || docList.isEmpty()) {
 			return null;
 		}
 		List<DocStatus> statusList = new ArrayList<DocStatus>(docList.size());
 		for (int i=0;i<docList.size();i++) {
 			statusList.add(new DocStatus(docList.get(i).getIcn(), docList.get(i).getCreateDate(), BlueButtonConstants.COMPLETE_STATUS_STRING));
 		}
 		return statusList;
 	}
 	
 	/**
 	 * Returns a list of all of the document statues for display through the getStatus web service.
 	 * 
 	 * @param docList		The List of documents to be formatted for output.
 	 * @return				The return list of documents for final display.
 	 */
 
 	protected List<DocStatus> getDocStatuses(List<C32DocumentEntity> docList) {
 		if (docList==null || docList.isEmpty()) {
 			return null;
 		}
 		
 		int arraySize = docList.size();
 		List<DocStatus> statusList = new ArrayList<DocStatus>(arraySize);
 		
 		for (int i=0;i<docList.size();i++) {
 			if (BeanUtils.equalsNullSafe(docList.get(i).getDocument(), BlueButtonConstants.INCOMPLETE_STATUS_STRING)) {
 				statusList.add(new DocStatus(docList.get(i).getIcn(), docList.get(i).getCreateDate(), BlueButtonConstants.INCOMPLETE_STATUS_STRING));
 			} else {
 				statusList.add(new DocStatus(docList.get(i).getIcn(), docList.get(i).getCreateDate(), BlueButtonConstants.COMPLETE_STATUS_STRING));
 			}
 		}		
 		return statusList;		
 	}
 	
 	/**
 	 * Limits the number of documents returned in getStatus to two.
 	 * 
 	 * @param today			Date to be used in the filtering process.
 	 * @param docList		List of C32 Documents to be filtered.
 	 * @return				List of C32 Documents after filtering.
 	 */
 
 	protected List<C32DocumentEntity> getLimitedStatuses(Date today, List<C32DocumentEntity> docList) {
 		if (docList==null || docList.isEmpty()) {
 			return null;
 		}
 		
 		int maxReturn = 2;
 		int arraySize = 0;
 		
 		if (docList.size() <= maxReturn) {
 			arraySize = docList.size();	
 		} else {
 			arraySize = maxReturn;
 		};
 		
 		List<C32DocumentEntity> statusList = new ArrayList<C32DocumentEntity>(arraySize);
 		
 		Collections.sort(docList, new Comparator<C32DocumentEntity>() {
 			public int compare (C32DocumentEntity e1, C32DocumentEntity e2) {
 				return e1.getCreateDate().compareTo(e2.getCreateDate());
 			}
 		});
 		
 		Collections.sort(docList, Collections.reverseOrder());
 		
 		for (int i=0;i<arraySize;i++) {
 			statusList.add(docList.get(i));			
 		}		
 		return statusList;		
 	}
 
 	
 	/**
 	 * Creates an entry in the JMS Queue to request a C32 be retrieved from the NwHIN adapter.
 	 * 
 	 * @param today			The date of the file request being submitted.
 	 * @param patientId		Patient identifier of the individual.
 	 * @param ptName		Last name of the associated patient.
 	 */
 
 	protected void sendMessageToRetrieve(final Date today, final String patientId, final String ptName) {
 		getMessageSender().sendRetrieveMessage(patientId, ptName, today);
 	}
 
 }
