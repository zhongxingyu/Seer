 package com.orangeleap.tangerine.service.impl;
 
 import javax.annotation.Resource;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.orangeleap.tangerine.dao.CommunicationHistoryDao;
 import com.orangeleap.tangerine.domain.CommunicationHistory;
 import com.orangeleap.tangerine.domain.Person;
 import com.orangeleap.tangerine.service.AuditService;
 import com.orangeleap.tangerine.service.CommunicationHistoryService;
 import com.orangeleap.tangerine.util.TangerineUserHelper;
 import com.orangeleap.tangerine.web.common.PaginatedResult;
 import com.orangeleap.tangerine.web.common.SortInfo;
 
 @Service("communicationHistoryService")
 @Transactional(propagation = Propagation.REQUIRED)
 public class CommunicationHistoryServiceImpl extends AbstractTangerineService implements CommunicationHistoryService {
 
 	/** Logger for this class and subclasses */
 	protected final Log logger = LogFactory.getLog(getClass());
 
 	@Resource(name = "communicationHistoryDAO")
 	private CommunicationHistoryDao communicationHistoryDao;
 	
 	@Resource(name = "tangerineUserHelper")
 	private TangerineUserHelper tangerineUserHelper;
 	
     @Resource(name = "auditService")
     protected AuditService auditService;
 
 	@Override
 	public CommunicationHistory maintainCommunicationHistory(CommunicationHistory communicationHistory) {
         if (logger.isTraceEnabled()) {
             logger.trace("maintainCommunicationHistory: communicationHistory = " + communicationHistory);
         }
 		if (communicationHistory.getPerson() == null) {
             return null;
         }
		Long lookupUserId = tangerineUserHelper.lookupUserId();
		if (lookupUserId != null) {
		    communicationHistory.setCustomFieldValue("recordedBy", "" + tangerineUserHelper.lookupUserId());
		}
 		CommunicationHistory savedHistory = communicationHistoryDao.maintainCommunicationHistory(communicationHistory);
 		auditService.auditObject(savedHistory, communicationHistory.getPerson());
 		return savedHistory;
 	}
 
 	@Override
 	public CommunicationHistory readCommunicationHistoryById(Long communicationHistoryId) {
         if (logger.isTraceEnabled()) {
             logger.trace("readCommunicationHistoryById: communicationHistoryId = " + communicationHistoryId);
         }
 		return communicationHistoryDao.readCommunicationHistoryById(communicationHistoryId);
 	}
 	
 	@Override
 	public PaginatedResult readCommunicationHistoryByConstituent(Long constituentId, SortInfo sortInfo) {
         if (logger.isTraceEnabled()) {
             logger.trace("readCommunicationHistoryByConstituent: constituentId = " + constituentId);
         }
 		return communicationHistoryDao.readCommunicationHistoryByConstituentId(constituentId, sortInfo);
 	}
 
 	@Override
 	public CommunicationHistory readCommunicationHistoryByIdCreateIfNull(String communicationHistoryId, Person constituent) {
 		if (logger.isTraceEnabled()) {
 			logger.trace("readCommunicationHistoryByIdCreateIfNull: communicationHistoryId = " + communicationHistoryId + " constituentId = " + (constituent == null ? null : constituent.getId()));
 		}
 		CommunicationHistory communicationHistory = null;
 		if (communicationHistoryId == null) {
 			communicationHistory = this.createCommunicationHistory(constituent);
 			communicationHistory.setRecordDate(new java.util.Date()); // default to today
 		}
 		else {
 			communicationHistory = this.readCommunicationHistoryById(Long.valueOf(communicationHistoryId));
 		}
 		return communicationHistory;
 	}
 
 	private CommunicationHistory createCommunicationHistory(Person constituent) {
 		CommunicationHistory communicationHistory = new CommunicationHistory();
 		communicationHistory.setPerson(constituent);
 		return communicationHistory;
 	}
 }
