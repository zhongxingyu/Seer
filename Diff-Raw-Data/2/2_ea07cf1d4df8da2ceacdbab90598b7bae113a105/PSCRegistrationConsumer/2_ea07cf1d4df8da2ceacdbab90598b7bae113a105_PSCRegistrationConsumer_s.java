 /**
  *
  */
 package edu.northwestern.bioinformatics.studycalendar.grid;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
 import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
 import edu.northwestern.bioinformatics.studycalendar.domain.Gender;
 import edu.northwestern.bioinformatics.studycalendar.domain.Population;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
 import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;
 import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
 import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
 import gov.nih.nci.cabig.ccts.domain.IdentifierType;
 import gov.nih.nci.cabig.ccts.domain.OrganizationAssignedIdentifierType;
 import gov.nih.nci.cabig.ccts.domain.ParticipantType;
 import gov.nih.nci.cabig.ccts.domain.Registration;
 import gov.nih.nci.cabig.ccts.domain.ScheduledTreatmentEpochType;
 import gov.nih.nci.cabig.ctms.audit.dao.AuditHistoryRepository;
 import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
 import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
 import gov.nih.nci.ccts.grid.common.RegistrationConsumerI;
 import gov.nih.nci.ccts.grid.stubs.types.InvalidRegistrationException;
 import gov.nih.nci.ccts.grid.stubs.types.RegistrationConsumptionException;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.oasis.wsrf.properties.GetMultipleResourcePropertiesResponse;
 import org.oasis.wsrf.properties.GetMultipleResourceProperties_Element;
 import org.oasis.wsrf.properties.GetResourcePropertyResponse;
 import org.oasis.wsrf.properties.QueryResourcePropertiesResponse;
 import org.oasis.wsrf.properties.QueryResourceProperties_Element;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.xml.namespace.QName;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author <a href="mailto:joshua.phillips@semanticbits.com>Joshua Phillips</a>
  */
 @Transactional(readOnly = false)
 public class PSCRegistrationConsumer implements RegistrationConsumerI {
 
     private static final Log logger = LogFactory.getLog(PSCRegistrationConsumer.class);
 
     public static final String SERVICE_BEAN_NAME = "scheduledCalendarService";
 
     private static final String MRN_IDENTIFIER_TYPE = "MRN";
 
 
     private static final String COORDINATING_CENTER_IDENTIFIER_TYPE = "Coordinating Center Identifier";
 
     // private StudyDao studyDao;
 
     private StudyService studyService;
 
     private SubjectDao subjectDao;
 
     private SubjectService subjectService;
 
     private AuditHistoryRepository auditHistoryRepository;
 
     private String registrationConsumerGridServiceUrl;
 
     private String rollbackTimeOut;
 
     private PscUserDetailsService pscUserDetailsService;
 
     private RegistrationGridServiceAuthorizationHelper gridServicesAuthorizationHelper;
     /**
      * This method authorize the caller for REGISTRAR Role
      * @return boolean
      */
     private SuiteRoleMembership getUserSuiteRoleMembership(){
     	String userName = getGridServicesAuthorizationHelper().getCurrentUsername();
     	SuiteRoleMembership suiteRoleMembership;
     	if (userName != null){
     		PscUser loadedUser = pscUserDetailsService.loadUserByUsername(userName);
     		Map<SuiteRole, SuiteRoleMembership> memberships = loadedUser.getMemberships();
     		suiteRoleMembership = memberships.get(SuiteRole.REGISTRAR);
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
 
     /**
      * Does nothing as we are already committing Registration message by default.
      *
      * @param registration
      * @throws RemoteException
      * @throws InvalidRegistrationException
      */
     public void commit(final Registration registration) throws RemoteException, InvalidRegistrationException {
     	
     }
 
     public void rollback(final Registration registration) throws RemoteException, InvalidRegistrationException {
     	//Get the study
     	String ccIdentifier = findCoordinatingCenterIdentifier(registration);
     	Study study = fetchStudy(ccIdentifier);
     	if (study == null) {
     		String message = "Study identified by Coordinating Center Identifier '" + ccIdentifier + "' doesn't exist";
     		throw getInvalidRegistrationException(message);
     	}
     	//Get the studySite
     	String siteNCICode = registration.getStudySite().getHealthcareSite(0).getNciInstituteCode();
     	StudySite studySite = findStudySite(study, siteNCICode);
     	if (studySite == null) {
     		siteNCICode = registration.getStudySite().getHealthcareSite(0).getGridId();
     		if((siteNCICode != null) && !(siteNCICode.equals(""))){
     			studySite = findStudySite(study, siteNCICode);
     		}
     		if (studySite == null){
     			String message = "The study '" + study.getLongTitle() + "', identified by Coordinating Center Identifier '" + ccIdentifier
     			+ "' is not associated to a site identified by NCI code :'" + siteNCICode + "'";
     			throw getInvalidRegistrationException(message);
     		}
     	}
     	//Get the Subject
     	String mrn = findMedicalRecordNumber(registration.getParticipant());
     	Subject subject = fetchCommitedSubject(mrn);
     	if (subject == null) {
     		String message = "Exception while rollback subject..no subject found with given identifier: " + mrn;
     		throw getInvalidRegistrationException(message);
     	}
     	try {
     		//check if subject was created by the grid service or not
     		boolean checkIfSubjectWasCreatedByGridService = auditHistoryRepository.checkIfEntityWasCreatedByUrl(subject.getClass(), 
     				subject.getId(), registrationConsumerGridServiceUrl);
 
     		//check if this subject was created one minute before or not
     		Calendar calendar = Calendar.getInstance();
     		Integer rollbackTime = 1;
     		try {
     			rollbackTime = Integer.parseInt(rollbackTimeOut);
     		} catch (NumberFormatException e) {
     			logger.error(String.format("error parsing value of rollback time out. Value of rollback time out %s must be integer.", rollbackTimeOut));
     		}
 
     		boolean checkIfSubjectWasCreatedOneMinuteBeforeCurrentTime = auditHistoryRepository.
     		checkIfEntityWasCreatedMinutesBeforeSpecificDate(subject.getClass(), subject.getId(), calendar, rollbackTime);
     		
     		//this Subject got created by the previous registration message. so delete it.
     		if (checkIfSubjectWasCreatedByGridService && checkIfSubjectWasCreatedOneMinuteBeforeCurrentTime) {
     			logger.info("Subject (id:" + subject.getId() + ") was created by the grid service url:" + registrationConsumerGridServiceUrl);
     			logger.info(String.format("Subject was created %s minute before the current time:%s", rollbackTime, calendar.getTime().toString()));
     			logger.info("So deleting the subject: " + subject.getId());
     			subjectDao.delete(subject);         
     		}else{
     			removeStudyAssignments(subject, studySite, rollbackTime);
     		}
     	} catch (Exception exception) {
     		String message = "Error while rollback, " + exception.getMessage();
     		throw getRegistrationConsumerException(message);
 
     	}
     }
     
     /**
      * Removes subject assignments, created one minute before current time by grid 
      * @param subject
      * @param studySite
      * @param rollbackTime
      */
     private void removeStudyAssignments(Subject subject, StudySite studySite, Integer rollbackTime){
     	
     	Calendar calendar = Calendar.getInstance();
     	List<StudySubjectAssignment> assignmentList = subject.getAssignments();
     	
     	//Check each subject assignment, Delete SubjectAssignment created by previous registration message(based on Subject and StudySite)
 		//if the SujectAssignment was created one minute before
 		List<StudySubjectAssignment> tempStudySubjectAssignmentList = new ArrayList<StudySubjectAssignment>();
 		for (StudySubjectAssignment studySubjectAssignment: assignmentList){
 			if(studySubjectAssignment.getStudySite().getId().equals(studySite.getId())){
 				boolean checkIfAssignmentWasCreatedOneMinuteBeforeCurrentTime = auditHistoryRepository.
 						checkIfEntityWasCreatedMinutesBeforeSpecificDate(studySubjectAssignment.getClass(), 
 						studySubjectAssignment.getId(), calendar, rollbackTime);
 				boolean checkIfAssignmentWasCreatedByGridService = auditHistoryRepository.checkIfEntityWasCreatedByUrl(studySubjectAssignment.getClass(), studySubjectAssignment.getId(), registrationConsumerGridServiceUrl);
 
 				if (checkIfAssignmentWasCreatedByGridService && checkIfAssignmentWasCreatedOneMinuteBeforeCurrentTime){
 					tempStudySubjectAssignmentList.add(studySubjectAssignment);
 				}
 			}
 		}
 		if(tempStudySubjectAssignmentList.size() > 0){
 			logger.info(String.format("SubjectAssignment was created %s minute before the current time:%s", rollbackTime, calendar.getTime().toString()));
 			logger.info(String.format("So deleting subjectAssignment for Subject: " + subject.getId()
 					+ "and StudySite: " + studySite.getId()));
 			for(StudySubjectAssignment studySubjectAssignmentObj: tempStudySubjectAssignmentList){
 				assignmentList.remove(studySubjectAssignmentObj);
 			}
 			subjectDao.save(subject);
 		}else{
 			logger.info(String.format("Subject/ SubjectAssignment was not created %s minute " +
 					"before the current time:%s so can not rollback this registration:%s",
 					rollbackTime, calendar.getTime().toString(), subject.getId())); 
 		}
     }
 
     /*
       * (non-Javadoc)
       * @see gov.nih.nci.cabig.ctms.common.RegistrationConsumer#createRegistration(gov.nih.nci.cabig.ctms.grid.RegistrationType)
       */
     public Registration register(final Registration registration) throws RemoteException, InvalidRegistrationException,
             RegistrationConsumptionException {
     	
 
     		// Check for Role
     		// 1. If Role is Registrar, then process, otherwise Access Denied.
     		SuiteRoleMembership suiteRoleMembership = getUserSuiteRoleMembership();
     		if(suiteRoleMembership == null){
     			String message = "Access Denied: user does not have REGISTRAR role";
     			throw getInvalidRegistrationException(message);
     		}
 
     		String ccIdentifier = findCoordinatingCenterIdentifier(registration);
     		// Authorization for study
     		if(!authorizedStudyIdentifier(ccIdentifier, suiteRoleMembership)){
     			String message = "Access Denied: Registrar is not authorized for the Study:" + ccIdentifier;
     			throw getInvalidRegistrationException(message);
     		}
 
     		Study study = fetchStudy(ccIdentifier);
 
     		if (study == null) {
     			String message = "Study identified by Coordinating Center Identifier '" + ccIdentifier + "' doesn't exist";
     			throw getInvalidRegistrationException(message);
     		}
 
     		String siteNCICode = registration.getStudySite().getHealthcareSite(0).getNciInstituteCode();
     		StudySite studySite = findStudySite(study, siteNCICode);
     		if (studySite == null) {
     			siteNCICode = registration.getStudySite().getHealthcareSite(0).getGridId();
     			if((siteNCICode != null) && !(siteNCICode.equals(""))){
     				studySite = findStudySite(study, siteNCICode);
     			}
     			if (studySite == null){
     				String message = "The study '" + study.getLongTitle() + "', identified by Coordinating Center Identifier '" + ccIdentifier
     				+ "' is not associated to a site identified by NCI code :'" + siteNCICode + "'";
     				throw getInvalidRegistrationException(message);
     			}
 
     		}
     		// Authorization for site
     		if(!authorizedSiteIdentifier(siteNCICode, suiteRoleMembership)){
     			String message = "Access Denied: Registrar is not authorized for the associated StudySite:" + siteNCICode;
     			throw getInvalidRegistrationException(message);
     		}
     		
     		String mrn = findMedicalRecordNumber(registration.getParticipant());
 
     		Subject subject = fetchCommitedSubject(mrn);
     		if (subject == null) {
     			subject = createSubject(registration.getParticipant(), mrn);
     			subjectDao.save(subject);
     		} else {
 
     			StudySubjectAssignment assignment = subjectDao.getAssignment(subject, study, studySite.getSite());
     			if (assignment != null) {
     				String message = "Subject already assigned to this study. Use scheduleNextArm to change to the next arm.";
     				throw getInvalidRegistrationException(message);
     			}
 
     		}
     		// // retrieve Arm
     		StudySegment studySegment = null;
     		StudySegment loadedStudySegment = null;
     		if (registration.getScheduledEpoch() != null
     				&& registration.getScheduledEpoch() instanceof ScheduledTreatmentEpochType
     				&& ((ScheduledTreatmentEpochType) registration.getScheduledEpoch()).getScheduledArm() != null
     				&& ((ScheduledTreatmentEpochType) registration.getScheduledEpoch()).getScheduledArm().getArm() != null) {
     			studySegment = new StudySegment();
     			studySegment.setName(((ScheduledTreatmentEpochType) registration.getScheduledEpoch()).getScheduledArm()
     					.getArm().getName());
     			studySegment.setGridId(((ScheduledTreatmentEpochType) registration.getScheduledEpoch()).getScheduledArm()
     					.getArm().getGridId());
     			loadedStudySegment = loadAndValidateStudySegmentInStudy(study, studySegment);
     		} else {
     			try {
     				loadedStudySegment = study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);
     			} catch (Exception e) {
     				String message = "The study '" + study.getLongTitle() + "', identified by Coordinating Center Identifier '" + ccIdentifier
     				+ "' does not have any arm'";
     				throw getInvalidRegistrationException(message);
 
     			}
     		}
 
     		String registrationGridId = registration.getGridId();
     		// Using the informed consent date as the calendar start date
     		Date startDate = registration.getInformedConsentFormSignedDate();
     		if (startDate == null) {
     			startDate = new Date();
     		}
 
     		StudySubjectAssignment newAssignment = null;
     		try {
     			newAssignment = subjectService.assignSubject(studySite,
                     new edu.northwestern.bioinformatics.studycalendar.service.presenter.Registration.Builder().
                         subject(subject).firstStudySegment(loadedStudySegment).date(startDate).
                         desiredAssignmentId(registrationGridId).studySubjectId(registrationGridId).
                         populations(Collections.<Population>emptySet()).
                         toRegistration());
     		} catch (StudyCalendarSystemException exp) {
     			throw getRegistrationConsumerException(exp.getMessage());
 
     		}
 
     		ScheduledCalendar scheduledCalendar = newAssignment.getScheduledCalendar();
     		logger.info("Created assignment " + scheduledCalendar.getId());
     		return registration;
     	
     }
 
     private Subject fetchCommitedSubject(String mrn) {
         return subjectService.findSubjectByPersonId(mrn);
 
     }
 
     private StudySite findStudySite(final Study study, final String siteNCICode) {
         for (StudySite studySite : study.getStudySites()) {
             if (StringUtils.equals(studySite.getSite().getAssignedIdentifier(), siteNCICode)) {
                 return studySite;
             }
         }
         return null;
     }
 
     /*
       * Finds the coordinating center identifier for the sutdy
       */
     private String findCoordinatingCenterIdentifier(final Registration registration)
             throws InvalidRegistrationException {
         String ccIdentifier = findIdentifierOfType(registration.getStudyRef().getIdentifier(),
                 COORDINATING_CENTER_IDENTIFIER_TYPE);
 
         if (ccIdentifier == null) {
             String message = "In StudyRef-Identifiers, Coordinating Center Identifier is not available";
             throw getInvalidRegistrationException(message);
         }
         return ccIdentifier;
 
     }
 
     private InvalidRegistrationException getInvalidRegistrationException(String message) {
         InvalidRegistrationException invalidRegistrationException = new InvalidRegistrationException();
 
         invalidRegistrationException.setFaultReason(message);
         invalidRegistrationException.setFaultString(message);
         logger.error(message);
         return invalidRegistrationException;
     }
 
     private String findIdentifierOfType(final IdentifierType[] idTypes, final String ofType) {
         if (idTypes == null) {
             return null;
         }
         for (IdentifierType identifierType : idTypes) {
             if (identifierType instanceof OrganizationAssignedIdentifierType && StringUtils.equals(identifierType.getType(), ofType)) {
                 return identifierType.getValue();
             }
         }
         return null;
     }
 
     private Study fetchStudy(final String ccIdentifier) {
         Study study = studyService.getStudyByAssignedIdentifier(ccIdentifier);
 
         return study;
     }
 
     private String findMedicalRecordNumber(final ParticipantType participantType) throws InvalidRegistrationException {
         String subjectIdentifier = findIdentifierOfType(participantType.getIdentifier(), MRN_IDENTIFIER_TYPE);
 
         if (subjectIdentifier == null) {
 
             String message = "There is no identifier associated to this subject, Medical Record Number(MRN) is needed to register this subject ";
             throw getInvalidRegistrationException(message);
         }
         return subjectIdentifier;
     }
 
 
     private StudySegment loadAndValidateStudySegmentInStudy(final Study study, final StudySegment requiredStudySegment) throws InvalidRegistrationException {
         for (Epoch epoch : study.getPlannedCalendar().getEpochs()) {
             List<StudySegment> studySegments = epoch.getStudySegments();
             for (StudySegment studySegment : studySegments) {
                 if (studySegment.getName().equals(requiredStudySegment.getName())) {
                     return studySegment;
                 }
             }
 
         }
         String message = "Arm " + requiredStudySegment.getName() + " not part of template for study "
                + study.getGridId();
         throw getInvalidRegistrationException(message);
     }
 
     private Subject createSubject(final ParticipantType participantType, final String mrn) {
         Subject subject = new Subject();
         subject.setGridId(participantType.getGridId());
         if (Gender.getByCode(participantType.getAdministrativeGenderCode()) != null) {
             subject.setGender(Gender.getByCode(participantType.getAdministrativeGenderCode()));
         } else {
             subject.setGender(Gender.MALE);
         }
         subject.setDateOfBirth(participantType.getBirthDate());
         subject.setFirstName(participantType.getFirstName());
         subject.setLastName(participantType.getLastName());
 
         subject.setPersonId(mrn);
         return subject;
     }
 
     private RegistrationConsumptionException getRegistrationConsumerException(String message) {
         RegistrationConsumptionException registrationConsumptionException = new RegistrationConsumptionException();
         registrationConsumptionException.setFaultReason(message);
         registrationConsumptionException.setFaultString(message);
         logger.error(message);
         return registrationConsumptionException;
     }
 
     @Required
     public void setStudyService(StudyService studyService) {
         this.studyService = studyService;
     }
 
     @Required
     public void setSubjectDao(SubjectDao subjectDao) {
         this.subjectDao = subjectDao;
     }
 
     @Required
     public void setSubjectService(SubjectService subjectService) {
         this.subjectService = subjectService;
     }
 
     @Required
     public void setAuditHistoryRepository(AuditHistoryRepository auditHistoryRepository) {
         this.auditHistoryRepository = auditHistoryRepository;
     }
 
     @Required
     public void setRegistrationConsumerGridServiceUrl(String registrationConsumerGridServiceUrl) {
         this.registrationConsumerGridServiceUrl = registrationConsumerGridServiceUrl;
     }
 
     @Required
     public void setRollbackTimeOut(String rollbackTimeOut) {
         this.rollbackTimeOut = rollbackTimeOut;
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
 	
 	public RegistrationGridServiceAuthorizationHelper getGridServicesAuthorizationHelper() {
 		if(gridServicesAuthorizationHelper==null){
 			gridServicesAuthorizationHelper = new RegistrationGridServiceAuthorizationHelper();
 		}
 		return gridServicesAuthorizationHelper;
 	}
 	public void setGridServicesAuthorizationHelper(
 			RegistrationGridServiceAuthorizationHelper gridServicesAuthorizationHelper) {
 		this.gridServicesAuthorizationHelper = gridServicesAuthorizationHelper;
 	}
 }
