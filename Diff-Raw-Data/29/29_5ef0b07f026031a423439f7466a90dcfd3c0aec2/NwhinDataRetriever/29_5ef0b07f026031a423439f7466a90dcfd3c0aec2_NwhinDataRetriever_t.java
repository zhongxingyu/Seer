 package org.osehra.das.repo.bluebutton;
 
 import java.sql.Timestamp;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.jms.Message;
 import javax.jms.MessageListener;
 import javax.jms.TextMessage;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.osehra.das.BeanUtils;
 import org.osehra.das.FormatTS;
 import org.osehra.das.wrapper.nwhin.C32DocumentEntity;
 import org.osehra.das.wrapper.nwhin.C32DocumentEntityFactory;
 import org.osehra.das.wrapper.nwhin.WrapperResource;
 
 /**
  * Message-driven bean (MDB) to call a <code>WrapperResource</code> to retrieve a C32.  
  * Uses a <code>C32DocumentEntityFactory</code> to create a 
  * <code>C32DocumentEntity</code> from the retrieved C32 document, and then uses a 
  * <code>C32DocumentDao</code> to save the <code>C32DocumentEntity</code> result.
  * @author Dept of VA
  *
  */
 public class NwhinDataRetriever extends AbstractC32DaoAware implements MessageListener {
 	protected WrapperResource _nwhinResource;
 	protected FormatTS asyncMessageFormat;
 	protected static Log logger = LogFactory.getLog(NwhinDataRetriever.class);
 	protected C32DocumentEntityFactory documentFactory;
 	
 	/**
 	 * 
 	 * @return Formatter for an AsyncRetrieveMessage
 	 */
 	public FormatTS getAsyncMessageFormat() {
 		return asyncMessageFormat;
 	}
 
 	/**
 	 * 
 	 * @param asyncMessageFormat Formatter for an AsyncRetrieveMessage
 	 */
 	public void setAsyncMessageFormat(FormatTS asyncMessageFormat) {
 		this.asyncMessageFormat = asyncMessageFormat;
 	}
 
 	/**
 	 * 
 	 * @return Factory for a C32DocumentEntity
 	 */
 	public C32DocumentEntityFactory getDocumentFactory() {
 		return documentFactory;
 	}
 
 	/**
 	 * 
 	 * @param documentFactory Factory for a C32DocumentEntity
 	 */
 	public void setDocumentFactory(C32DocumentEntityFactory documentFactory) {
 		this.documentFactory = documentFactory;
 	}
 
 	/**
 	 * 
 	 * @param resource C32 retriever interface
 	 */
 	public void setWrapperResource(WrapperResource resource) {
 		_nwhinResource = resource;
 	}
 
 	/**
 	 * Retrieves the text from the JMS message, parses it into an AsyncRetrieveMessage, calls
 	 * a WrapperResource for a C32 document, creates a C32DocumentEntity from a C32DocumentEntityFactory,
 	 * and saves the C32DocumentEntity using the C32DocumentDao.
 	 */
 	@Override
 	public void onMessage(Message msg) {
 		TextMessage tMsg = (TextMessage)msg;
 		AsyncRetrieveMessage aMsg;
 		boolean debugging = logger.isDebugEnabled();
 		try {
 			aMsg = (AsyncRetrieveMessage)getAsyncMessageFormat().parse(tMsg.getText());
 			if (debugging) {
 				logger.debug("calling getDomainXml for " + aMsg);
 			}
 			String xml = (String)_nwhinResource.getDomainXml(aMsg.getPatientId(), aMsg.getPatientName());
 			if (debugging) {
 				logger.debug("Attempting to Persist Domain XML for: " + aMsg.getPatientId());
 			}
 			updateDocumentWithNewDocument(aMsg.getPatientId(), getDocumentFactory().createDocument(aMsg.getPatientId(), xml));
 		} catch (Exception e) {
 			logger.error("general exception for " + msg, e);
 		}
 	}
 	
 	protected void updateDocumentWithNewDocument(String patientId, C32DocumentEntity newDoc) {
 		boolean debugging = logger.isDebugEnabled();
 		C32DocumentEntity oldDocument = getOldDocument(newDoc);
 		if (oldDocument==null) {
 			if (debugging) {
 				logger.info("no old document, so inserting new:" + newDoc);
 			}
 			getC32DocumentDao().insert(newDoc);
 			return;
 		}
 		if (newDoc.getDocument() != null && !newDoc.getDocument().isEmpty()) {
 			if (stringsEqualNullSafe(newDoc.getDocumentPatientId(), patientId)) {
 				oldDocument.setCreateDate(newDoc.getCreateDate());
 				oldDocument.setDocument(newDoc.getDocument());
 				oldDocument.setDocumentPatientId(newDoc.getDocumentPatientId());
 				if (debugging) {
 					logger.debug("merging (updating) document:" + oldDocument);
 				}
 				getC32DocumentDao().update(oldDocument);
 			}
 			else {
 				logger.warn("Patient ID's don't match: requested ID:" + patientId + " document ID:" + newDoc.getDocumentPatientId());
				oldDocument.setDocument(BlueButtonConstants.ERROR_STATUS_STRING);
				getC32DocumentDao().update(oldDocument);	
 			}
 		} else {
 			logger.warn("No valid record available for patient ID: " + patientId);
			oldDocument.setDocument(BlueButtonConstants.UNAVAILABLE_STATUS_STRING);
			getC32DocumentDao().update(oldDocument);
 		}
 	}
 
 	protected C32DocumentEntity getOldDocument(C32DocumentEntity newDoc) {
     	List<C32DocumentEntity> results = getC32DocumentDao().getAllDocuments(newDoc.getIcn());
     	return getOldDocFromList(results, newDoc);
     }
 
 	protected C32DocumentEntity getOldDocFromList(List<C32DocumentEntity> results, C32DocumentEntity newDoc) {
 		if (results!=null && results.size()>0) {
 			GregorianCalendar cal = new GregorianCalendar();
 			if (results.size()>1) {
 				Collections.sort(results);
 			}
 			for (int i=results.size()-1;i>-1;i--) {
 				if (newDoc.getIcn().equals(results.get(i).getIcn()) &&
 						BeanUtils.equalsNullSafe(results.get(i).getDocument(), BlueButtonConstants.INCOMPLETE_STATUS_STRING) &&
 						datesEqual(cal, results.get(i).getCreateDate(), newDoc.getCreateDate())) {
 					return results.get(i);
				}		
 			}
 		}
 		return null;
 	}
 
 	protected boolean datesEqual(Calendar cal, Timestamp date1, Timestamp date2) {
 		if (date1!=null && date2!=null) {
 			cal.setTime(date1);
 			int year1 = cal.get(Calendar.YEAR);
 			int month1 = cal.get(Calendar.MONTH);
 			int day1 = cal.get(Calendar.DATE);
 
 			cal.setTime(date2);
 			int year2 = cal.get(Calendar.YEAR);
 			int month2 = cal.get(Calendar.MONTH);
 			int day2 = cal.get(Calendar.DATE);
 			return year1==year2 && month1==month2 && day1==day2;
 		}
 		return date1==null && date2==null;
 	}
     
 	protected static boolean stringsEqualNullSafe(String s1, String s2) {
 		if (s1==null && s2==null) {
 			return true;
 		}
 		if (s1!=null && s2!=null) {
 			String temp1 = s1.trim().toLowerCase();
 			String temp2 = s2.trim().toLowerCase();
 			return temp1.equals(temp2);
 		}
 		return false;
 	}
 
 }
