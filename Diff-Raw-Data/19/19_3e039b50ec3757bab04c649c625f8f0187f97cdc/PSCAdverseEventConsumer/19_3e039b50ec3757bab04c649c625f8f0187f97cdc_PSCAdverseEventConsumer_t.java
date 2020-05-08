 /**
  *
  */
 package edu.northwestern.bioinformatics.studycalendar.grid;
 
 import edu.northwestern.bioinformatics.studycalendar.api.ScheduledCalendarService;
 import edu.northwestern.bioinformatics.studycalendar.domain.AdverseEvent;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
 import gov.nih.nci.cabig.ctms.grid.ae.common.AdverseEventConsumerI;
 import gov.nih.nci.cabig.ctms.grid.ae.stubs.types.InvalidRegistration;
 import gov.nih.nci.cabig.ctms.grid.ae.stubs.types.RegistrationFailed;
 import gov.nih.nci.cabig.ccts.ae.domain.AENotificationType;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.globus.wsrf.security.SecurityManager;
 import org.oasis.wsrf.properties.*;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.transaction.annotation.Transactional;
 
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;
 import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
 import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
 
 import javax.xml.namespace.QName;
 import java.rmi.RemoteException;
 import java.util.Date;
 import java.util.Map;
 
 /**
  * @author <a href="mailto:joshua.phillips@semanticbits.com>Joshua Phillips</a>
  */
 
 @Transactional(readOnly = false)
 public class PSCAdverseEventConsumer implements AdverseEventConsumerI {
 
     private static final Log logger = LogFactory.getLog(PSCAdverseEventConsumer.class);
 
     private ScheduledCalendarService scheduledCalendarService;
     
     private PscUserDetailsService pscUserDetailsService;
     
     private StudySubjectAssignmentDao studySubjectAssignmentDao;
     
     private AdverseEventGridServiceAuthorizationHelper gridServicesAuthorizationHelper;
 
     private SuiteRoleMembership getUserSuiteRoleMembership(){
     	String userName = getGridServicesAuthorizationHelper().getCurrentUsername();
     	SuiteRoleMembership suiteRoleMembership;
     	if (userName != null){
     		PscUser loadedUser = pscUserDetailsService.loadUserByUsername(userName);
     		Map<SuiteRole, SuiteRoleMembership> memberships = loadedUser.getMemberships();
     		suiteRoleMembership = memberships.get(SuiteRole.AE_REPORTER);
     		return suiteRoleMembership;
     	}
     	return null;
     }
 
     public boolean authorizedStudyIdentifier(String studyIdentifier,SuiteRoleMembership suiteRoleMembership ){
     	if(suiteRoleMembership.isAllStudies()){
     		return true;
     	}else {
     		return suiteRoleMembership.getStudyIdentifiers().contains(studyIdentifier);
     	}
     }
     
     public boolean authorizedSiteIdentifier(String siteidentifier,SuiteRoleMembership suiteRoleMembership){
     	if(suiteRoleMembership.isAllSites()){
     		return true;
     	}else {
     		return suiteRoleMembership.getSiteIdentifiers().contains(siteidentifier);
     	}
     }
 
     public void register(final AENotificationType aeNotification) throws java.rmi.RemoteException, InvalidRegistration, RegistrationFailed {
    	
     		// Check for Role
     		// 1. If Assigned Role is AE_REPORTER, then process, otherwise Access Denied.
     		SuiteRoleMembership suiteRoleMembership = getUserSuiteRoleMembership();
     		if(suiteRoleMembership == null){
    			logger.error("Access Denied: user does not have AE_REPORTER role");
    			throw new RemoteException("Access Denied: user does not have AE_REPORTER role");
     		}
 
     		// TODO: Change fault implementation to accept a reason message.
     		String gridId = aeNotification.getRegistrationGridId();
     		if (gridId == null) {
     			logger.error("No registrationGridId provided");
     			throw new InvalidRegistration();
     		}
     		// get the StudySubjectAssignment
     		StudySubjectAssignment studySubjectAssignment = studySubjectAssignmentDao.getByGridId(gridId);
     		if(studySubjectAssignment == null){
     			logger.error("Invalid Registration GridId provided");
     			throw new InvalidRegistration();
     		}
     		StudySite studySite = studySubjectAssignment.getStudySite();
     		Study study = studySite.getStudy();
     		String studyAssignedIdentifier = study.getAssignedIdentifier();
     		// Authorization for study
     		if(!authorizedStudyIdentifier(studyAssignedIdentifier, suiteRoleMembership)){
     			String message = "Access Denied: AE_REPORTER is not authorized for this Study Identifier : " + studyAssignedIdentifier;
     			logger.error(message);
    			throw new RemoteException(message);
     		}
     		
     		Site site = studySite.getSite();
     		
     		String siteAssignedIdentifier = site.getAssignedIdentifier();
     		// Authorization for site
     		if(!authorizedSiteIdentifier(siteAssignedIdentifier, suiteRoleMembership)){
     			String message = "Access Denied: AE_REPORTER is not authorized for this Site Identifier : " + siteAssignedIdentifier;
     			logger.error(message);
    			throw new RemoteException(message);
     		}
     		
     		String description = aeNotification.getDescription();
     		if (description == null) {
     			logger.error("No description provided");
     			throw new InvalidRegistration();
     		}
     		Date detectionDate = aeNotification.getDetectionDate();
     		if (detectionDate == null) {
     			logger.error("No detectionDate provided");
     			throw new InvalidRegistration();
     		}
 
     		StudySubjectAssignment assignment = new StudySubjectAssignment();
     		assignment.setGridId(gridId);
 
     		AdverseEvent event = new AdverseEvent();
     		event.setDescription(description);
     		event.setDetectionDate(detectionDate);
 
     		try {
     			scheduledCalendarService.registerSevereAdverseEvent(assignment, event);
     		}
     		catch (Exception ex) {
     			logger.error("Error registering adverse event: " + ex.getMessage(), ex);
     			throw new RegistrationFailed();
     		}
    	
     }
 
     @Required
     public void setScheduledCalendarService(final ScheduledCalendarService scheduledCalendarService) {
         this.scheduledCalendarService = scheduledCalendarService;
     }
 
     public GetMultipleResourcePropertiesResponse getMultipleResourceProperties(final GetMultipleResourceProperties_Element getMultipleResourceProperties_element) throws RemoteException {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public GetResourcePropertyResponse getResourceProperty(final QName qName) throws RemoteException {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public QueryResourcePropertiesResponse queryResourceProperties(final QueryResourceProperties_Element queryResourceProperties_element) throws RemoteException {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
     
     public PscUserDetailsService getPscUserDetailsService() {
 		return pscUserDetailsService;
 	}
 	@Required
 	public void setPscUserDetailsService(PscUserDetailsService pscUserDetailsService) {
 		this.pscUserDetailsService = pscUserDetailsService;
 	}
 
 	public StudySubjectAssignmentDao getStudySubjectAssignmentDao() {
 		return studySubjectAssignmentDao;
 	}
 	@Required
 	public void setStudySubjectAssignmentDao(
 			StudySubjectAssignmentDao studySubjectAssignmentDao) {
 		this.studySubjectAssignmentDao = studySubjectAssignmentDao;
 	}
 	
 	public AdverseEventGridServiceAuthorizationHelper getGridServicesAuthorizationHelper() {
 		if(gridServicesAuthorizationHelper==null){
 			gridServicesAuthorizationHelper = new AdverseEventGridServiceAuthorizationHelper();
 		}
 		return gridServicesAuthorizationHelper;
 	}
 	public void setGridServicesAuthorizationHelper(
 			AdverseEventGridServiceAuthorizationHelper gridServicesAuthorizationHelper) {
 		this.gridServicesAuthorizationHelper = gridServicesAuthorizationHelper;
 	}
 }
