 package org.huamuzhen.oa.biz;
 
 import java.math.BigDecimal;
 import java.sql.Timestamp;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.huamuzhen.oa.biz.util.DeadlineCounter;
 import org.huamuzhen.oa.domain.dao.ReportFormDAO;
 import org.huamuzhen.oa.domain.dao.ReportFormTypeDAO;
 import org.huamuzhen.oa.domain.dao.UserDAO;
 import org.huamuzhen.oa.domain.entity.OrgUnit;
 import org.huamuzhen.oa.domain.entity.ReportForm;
 import org.huamuzhen.oa.domain.enumeration.ReportFormStatus;
 import org.huamuzhen.oa.domain.enumeration.SquareMeasure;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service
 public class ReportFormManager extends BaseManager<ReportForm, String> {
 	
 	@Resource
 	private ReportFormDAO reportFormDAO;
 	
 	@Resource
 	private ReportFormTypeDAO reportFormTypeDAO;
 	
 	@Resource
 	private UserDAO userDAO;
 	
 	
 	@Resource
 	private MessageManager message;
 
 	@Resource
 	public void setDao(ReportFormDAO dao) {
 		super.setDao(dao);
 	}
 
 	@Transactional
 	public ReportForm updateExisting(String id, String reportFormTypeId, String title,
 			String formId, String landUser, String originalLandUser,
 			String landLocation, BigDecimal landArea, String landAreaMeasure, String landUse,
 			String originalLandUse, String matter, String matterDetail,
 			String policyBasis, String comment, String responsiblePerson,
 			String auditor, String tabulator) {
 		
 		ReportForm reportForm = reportFormDAO.findOne(id);
 		reportForm = this.setBasicDataForReportForm(reportForm,
 				reportFormTypeId, title, landUser, originalLandUser,
 				landLocation, landArea, landAreaMeasure, landUse,
 				originalLandUse, matter, matterDetail, policyBasis, comment,
 				responsiblePerson, auditor, tabulator);
 		reportForm.setFormId(formId);
 		return reportFormDAO.save(reportForm);
 	}
 
 	@Transactional
 	public ReportForm createNew(String title, String reportFormTypeId,
 			String landUser, String originalLandUser, String landLocation,
 			BigDecimal landArea, String landAreaMeasure, String landUse, String originalLandUse,
 			String matter, String matterDetail, String policyBasis,
 			String comment, String responsiblePerson, String auditor, String tabulator, String creatorId) {
 		
 		ReportForm newReportForm = new ReportForm();
 		newReportForm = this.setBasicDataForReportForm(newReportForm,
 				reportFormTypeId, title, landUser, originalLandUser,
 				landLocation, landArea, landAreaMeasure, landUse,
 				originalLandUse, matter, matterDetail, policyBasis, comment,
 				responsiblePerson, auditor, tabulator);
 		newReportForm.setFormId(this.generateFormId());
 		newReportForm.setCreatorId(creatorId);
 		newReportForm.setReferredReportFormId(null);
 		newReportForm.setStatus(ReportFormStatus.NOT_SEND);
 		newReportForm.setCreatedAt(new Timestamp(System.currentTimeMillis()));
 		return reportFormDAO.save(newReportForm);
 	}
 	
 	@Transactional
 	public ReportForm reCreateReportForm(String oldId, String reportFormTypeId,
 			String title, String oldFormId, String landUser,
 			String originalLandUser, String landLocation, BigDecimal landArea, 
 			String landAreaMeasure, String landUse, String originalLandUse, String matter,
 			String matterDetail, String policyBasis, String comment,
 			String responsiblePerson, String auditor, String tabulator,
 			String creatorId) {
 		
 		ReportForm oldReportForm = this.findOne(oldId);
 		oldReportForm.setStatus(ReportFormStatus.DEAD);
 		ReportForm newReportForm = new ReportForm();
 		newReportForm = this.setBasicDataForReportForm(newReportForm,
 				reportFormTypeId, title, landUser, originalLandUser,
 				landLocation, landArea, landAreaMeasure, landUse,
 				originalLandUse, matter, matterDetail, policyBasis, comment,
 				responsiblePerson, auditor, tabulator);
 		newReportForm.setFormId(this.generateFormId(oldFormId));
 		newReportForm.setCreatorId(creatorId);
 		newReportForm.setReferredReportFormId(oldReportForm.getId());
 		newReportForm.setStatus(ReportFormStatus.NOT_SEND);
 		newReportForm.setCreatedAt(new Timestamp(System.currentTimeMillis()));
 		reportFormDAO.save(oldReportForm);
 		return reportFormDAO.save(newReportForm);	
 	}
 	
 	private ReportForm setBasicDataForReportForm(final ReportForm reportForm,String reportFormTypeId,
 			String title, String landUser,
 			String originalLandUser, String landLocation, BigDecimal landArea, 
 			String landAreaMeasure, String landUse, String originalLandUse, String matter,
 			String matterDetail, String policyBasis, String comment,
 			String responsiblePerson, String auditor, String tabulator){
 		reportForm.setTitle(title);
 		reportForm.setReportFormType(reportFormTypeDAO.findOne(reportFormTypeId));
 		reportForm.setLandUser(landUser);
 		reportForm.setOriginalLandUser(originalLandUser);
 		reportForm.setLandLocation(landLocation);
 		reportForm.setLandArea(landArea);
 		reportForm.setLandAreaMeasure(SquareMeasure.valueOf(landAreaMeasure));
 		reportForm.setLandUse(landUse);
 		reportForm.setOriginalLandUse(originalLandUse);
 		reportForm.setMatter(matter);
 		reportForm.setMatterDetail(matterDetail);
 		reportForm.setPolicyBasis(policyBasis);
 		reportForm.setComment(comment);
 		reportForm.setResponsiblePerson(responsiblePerson);
 		reportForm.setAuditor(auditor);
 		reportForm.setTabulator(tabulator);
 		reportForm.setModifiedAt(new Timestamp(System.currentTimeMillis()));
 		return reportForm;
 	}
 
 	
     public String generateFormId(String previousFormId){
 		
 		String currentSuffix= previousFormId.substring(13);
 		Integer nextSuffix = Integer.parseInt(currentSuffix) + 1;
 		if(nextSuffix < 10){
 			return previousFormId.substring(0, 13)+ "0" + nextSuffix;
 		}else{
 			return previousFormId.substring(0, 13)+ nextSuffix.toString();
 		}
 	}
 	
     @Transactional
 	public String generateFormId(){
 		
 		String dateOfToday = new SimpleDateFormat("yyyyMMdd")
 				.format(new Date());
 		String fuzzyQueryCondition = dateOfToday + "%-00";
 		List<String> newCreatedFormIdListOfToday = this.reportFormDAO
 				.findNewCreatedFormIdListOfToday(fuzzyQueryCondition);
 		if (newCreatedFormIdListOfToday.size() == 0) {
 			return new StringBuilder().append(dateOfToday).append("0000-00").toString();
 		}
 		String latestFormIdOfToday = newCreatedFormIdListOfToday.get(0);
 
 		Integer count = Integer.parseInt(latestFormIdOfToday.substring(8, 12)) + 1;
 		if (count < 10) {
 			return new StringBuilder().append(dateOfToday).append("000").append(count).append("-00").toString();
 		} else if (count < 100) {
 			return new StringBuilder().append(dateOfToday).append("00").append(count).append("-00").toString();
 		} else if (count < 1000) {
 			return new StringBuilder().append(dateOfToday).append("0").append(count).append("-00").toString();
 		} else {
 			return new StringBuilder().append(dateOfToday).append(count).append("-00").toString();
 		}
 
 	}
     
     @Transactional
 	public List<ReportForm> findReportFormByStatusAndCurrentSenderId(String reportFormStatusLink, String currentSenderId) {
 			
 		return reportFormDAO.findReportFormByStatusAndCurrentSenderIdOrderByCreatedAtDesc(ReportFormStatusLinkToReportForm(reportFormStatusLink),currentSenderId);
 	}
 
     @Transactional
 	public List<ReportForm> findReportFormByStatusAndCurrentReceiverId(String reportFormStatusLink, String currentReceiverId) {
 			
 		return reportFormDAO.findReportFormByStatusAndCurrentReceiverIdOrderByCreatedAtDesc(ReportFormStatusLinkToReportForm(reportFormStatusLink),currentReceiverId);
 	}
 	
     @Transactional
 	public List<ReportForm> findReportFormByStatusAndCreatorId(String reportFormStatusLink, String creatorId){
 		return reportFormDAO.findReportFormByStatusAndCreatorIdOrderByCreatedAtDesc(ReportFormStatusLinkToReportForm(reportFormStatusLink), creatorId);
 	}
 	
     @Transactional
 	public List<ReportForm> findReportFormByStatus(String reportFormStatusLink) {
 
 		return reportFormDAO.findReportFormByStatusOrderByCreatedAtDesc(ReportFormStatusLinkToReportForm(reportFormStatusLink));
 	}
 	
     @Transactional
 	public ReportForm sendToOrgUnits(String id, String currentSenderId, int deadlineDuration){
     	ReportForm reportForm = reportFormDAO.findOne(id);
 		reportForm.setCurrentSenderId(currentSenderId);
 		reportForm.setStatus(ReportFormStatus.SENT_TO_ORG_UNITS);
 		reportForm.setSendTime(new Timestamp(System.currentTimeMillis()));
 		reportForm.setDeadlineTime(DeadlineCounter.getDeadline(deadlineDuration));
 		message.sendMsg(currentSenderId, "将报审表: "+ reportForm.getFormId() + " 发送至各审批部门");
 		return reportFormDAO.save(reportForm);
 	}
 
     @Transactional
 	public ReportForm sendToLeader1(String id, String leader1Id, String currentSenderId) {
 		ReportForm reportForm = reportFormDAO.findOne(id);
 		reportForm.setStatus(ReportFormStatus.SENT_TO_LEADER1);
 		reportForm.setCurrentSenderId(currentSenderId);
 		reportForm.setCurrentReceiverId(leader1Id);
 		reportForm.setDeadlineTime(DeadlineCounter.getDeadline(1000));
 		message.sendMsg(currentSenderId, "将报审表: "+ reportForm.getFormId() + " 发送至分管领导: " + userDAO.findOne(leader1Id).getUsername());
 		message.sendMsg(leader1Id, "收到报审表: "+ reportForm.getFormId() + " 来自: " + userDAO.findOne(currentSenderId).getUsername());
 		return reportFormDAO.save(reportForm);
 	}
     
     @Transactional
     public ReportForm sendBackToReporter(String id, String currentSenderId) {
     	ReportForm reportForm = reportFormDAO.findOne(id);
     	reportForm.setStatus(ReportFormStatus.REJECTED_BY_LEADER1);
     	reportForm.setCurrentSenderId(currentSenderId);
     	reportForm.setCurrentReceiverId(reportForm.getCreatorId());
     	reportForm.setDeadlineTime(DeadlineCounter.getDeadline(1000));
     	message.sendMsg(currentSenderId, "将报审表: "+ reportForm.getFormId() + " 发送至报审单位: " + userDAO.findOne(reportForm.getCreatorId()).getUsername());
     	message.sendMsg(reportForm.getCreatorId(),  " 被分管领导: " + userDAO.findOne(reportForm.getCreatorId()).getUsername()+ "打回报审表: "+ reportForm.getFormId());
     	return reportFormDAO.save(reportForm);
 	}
 	
 	private ReportFormStatus ReportFormStatusLinkToReportForm(String reportFormStatusLink){
 		ReportFormStatus status = null;
 		if(reportFormStatusLink.equals("notSendReportForm")){
 			status = ReportFormStatus.NOT_SEND;
 		}else if(reportFormStatusLink.equals("sentToOrgUnitsReportForm")){
 			status = ReportFormStatus.SENT_TO_ORG_UNITS;
 		}else if(reportFormStatusLink.equals("gotReplyFromUnitsReportForm")){
 			status = ReportFormStatus.GOT_REPLY_FROM_UNITS;
 		}else if(reportFormStatusLink.equals("sentToLeader1ReportForm")){
 			status = ReportFormStatus.SENT_TO_LEADER1;
 		}else if(reportFormStatusLink.equals("rejectedByLeader1ReportForm")){
 			status = ReportFormStatus.REJECTED_BY_LEADER1;
 		}else if(reportFormStatusLink.equals("sentToLeader2ReportForm")){
 			status = ReportFormStatus.SENT_TO_LEADER2;
 		}else if(reportFormStatusLink.equals("rejectedByLeader2ReportForm")){
 			status = ReportFormStatus.REJECTED_BY_LEADER2;
 		}else if(reportFormStatusLink.equals("sentToOfficeReportForm")){
 			status = ReportFormStatus.SENT_TO_OFFICE;
 		}else if(reportFormStatusLink.equals("passedReportForm")){
 			status = ReportFormStatus.PASSED;
 		}
 		return status;
 	}
 
 	@Transactional
 	public void setReportFormAsDeleted(String id) {
 		ReportForm reportForm = reportFormDAO.findOne(id);
 		reportForm.setStatus(ReportFormStatus.DELETED);
 		this.save(reportForm);
 	}
 }
