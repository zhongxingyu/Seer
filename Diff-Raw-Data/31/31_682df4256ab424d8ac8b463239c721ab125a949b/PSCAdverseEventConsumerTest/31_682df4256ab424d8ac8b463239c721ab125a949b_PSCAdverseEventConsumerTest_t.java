 package edu.northwestern.bioinformatics.studycalendar.grid;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
 import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
 import edu.northwestern.bioinformatics.studycalendar.domain.Gender;
 import edu.northwestern.bioinformatics.studycalendar.domain.Notification;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
 import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;
 import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
 import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
 import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
 import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
 import edu.northwestern.bioinformatics.studycalendar.service.TemplateSkeletonCreatorImpl;
 import gov.nih.nci.cabig.ccts.ae.domain.AENotificationType;
 import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
 import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
 import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
 import gov.nih.nci.cagrid.common.Utils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.easymock.classextension.EasyMock;
 import org.junit.Assert;
 import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.test.AbstractTransactionalSpringContextTests;
 
 import java.io.FileReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
import java.rmi.RemoteException;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Map;
 
import static org.easymock.EasyMock.expect;
 
 /**
  * Test class added to validate the clean scripts that were added for CCTS roll-back script requirement
  *
  * @author Saurabh Agrawal
  */
 public class PSCAdverseEventConsumerTest  extends AbstractTransactionalSpringContextTests {
 
 	private final Log logger = LogFactory.getLog(getClass());
 	private String assignmentGridId = "48a14190-04d8-4c50-b594-588bb4fe8a7d";  //John Doe
 	private String assignedIdentifier = "TEST_STUDY";
 	private String nciCode = "SITE_01";
 	private SiteDao siteDao;
 	private String shortTitle = "SMOTE_TEST";
 	private String longTitle = "Test long title";
 	
 	private PSCAdverseEventConsumer adverseEventConsumer;
 	private String aeFile;
 	private StudyService studyService;
 	private StudySubjectAssignmentDao studySubjectAssignmentDao;
 	private SubjectService subjectService;
 	private StudyDao studyDao;
 	private SubjectDao subjectDao;
 	private String subjectGridId = "91dd4580-801b-4874-adeb-a174361bacea";
 	private StudySubjectAssignment studySubjectAssignment;
 	
 	private AdverseEventGridServiceAuthorizationHelper gridServicesAuthorizationHelper;
 	private PscUserDetailsService pscUserDetailsService;
 	private PscUser user;
 	private PscUser userWithoutAeReporterRole;
 	private PscUser userWithIncorrectStudy;
 	private PscUser userWithIncorrectStudySite;
 	private PscUser userWithAllStudiesAllSites;
 	private PscUser userWithStudyAllSites;
 	private PscUser userWithAllStudiesStudySite;
 
 	protected void onSetUpInTransaction() throws Exception {
 
 		DataAuditInfo.setLocal(new DataAuditInfo("test", "localhost", new Date(), "/wsrf-psc/services/cagrid/AdverseEventConsumer"));
 		
 		aeFile = System.getProperty("psc.test.sampleNotificationFile");
 		
 		Study study = studyDao.getByAssignedIdentifier(assignedIdentifier);
 		if (study == null) {
 			logger.error(String.format("no study found for given identifier %s", assignedIdentifier));
 			createStudy(); //create study and re-run the test case..
 		}
 		
 		createAssignmentAndAeNotifications();
 		
 		gridServicesAuthorizationHelper=EasyMock.createMock(AdverseEventGridServiceAuthorizationHelper.class);
 		pscUserDetailsService=EasyMock.createMock(PscUserDetailsService.class);
 
 		SuiteRoleMembership suiteRoleMembership = new SuiteRoleMembership(SuiteRole.AE_REPORTER, null, null);
 		suiteRoleMembership.addSite("SITE_01");
 		suiteRoleMembership.addStudy("TEST_STUDY");
 		Map<SuiteRole,SuiteRoleMembership> expectedMemberships = Collections.singletonMap(SuiteRole.AE_REPORTER, suiteRoleMembership);
 		user = new PscUser(null, expectedMemberships);
 		
 		// User without AE_REPORTER role
 		SuiteRoleMembership suiteRoleMembership1 = new SuiteRoleMembership(SuiteRole.STUDY_CREATOR, null, null);
 		suiteRoleMembership1.addSite("SITE_01");
 		suiteRoleMembership1.addStudy("TEST_STUDY");
 		Map<SuiteRole,SuiteRoleMembership> expectedMemberships1 = Collections.singletonMap(SuiteRole.STUDY_CREATOR, suiteRoleMembership1);
 		userWithoutAeReporterRole = new PscUser(null, expectedMemberships1);
 		
 		// User with AE_REPORTER role, No Study
 		SuiteRoleMembership suiteRoleMembership2 = new SuiteRoleMembership(SuiteRole.AE_REPORTER, null, null);
 		suiteRoleMembership2.addSite("SITE_01");
 		Map<SuiteRole,SuiteRoleMembership> expectedMemberships2 = Collections.singletonMap(SuiteRole.AE_REPORTER, suiteRoleMembership2);
 		userWithIncorrectStudy = new PscUser(null, expectedMemberships2);
 		
 		// User with AE_REPORTER role,Study and No StudySite
 		SuiteRoleMembership suiteRoleMembership3 = new SuiteRoleMembership(SuiteRole.AE_REPORTER, null, null);
 		suiteRoleMembership3.addStudy("TEST_STUDY");
 		Map<SuiteRole,SuiteRoleMembership> expectedMemberships3 = Collections.singletonMap(SuiteRole.AE_REPORTER, suiteRoleMembership3);
 		userWithIncorrectStudySite = new PscUser(null, expectedMemberships3);
 		
 		// User with AE_REPORTER role, AllStudies and AllSites
 		SuiteRoleMembership suiteRoleMembership4 = new SuiteRoleMembership(SuiteRole.AE_REPORTER, null, null);
 		suiteRoleMembership4.forAllSites();
 		suiteRoleMembership4.forAllStudies();
 		Map<SuiteRole,SuiteRoleMembership> expectedMemberships4 = Collections.singletonMap(SuiteRole.AE_REPORTER, suiteRoleMembership4);
 		userWithAllStudiesAllSites = new PscUser(null, expectedMemberships4);
 		
 		// User with AE_REPORTER role, Study and AllSites
 		SuiteRoleMembership suiteRoleMembership5 = new SuiteRoleMembership(SuiteRole.AE_REPORTER, null, null);
 		suiteRoleMembership5.forAllSites();
 		suiteRoleMembership5.addStudy("TEST_STUDY");
 		Map<SuiteRole,SuiteRoleMembership> expectedMemberships5 = Collections.singletonMap(SuiteRole.AE_REPORTER, suiteRoleMembership5);
 		userWithStudyAllSites = new PscUser(null, expectedMemberships5);
 		
 		// User with AE_REPORTER role, AllStudies and StudySite
 		SuiteRoleMembership suiteRoleMembership6 = new SuiteRoleMembership(SuiteRole.AE_REPORTER, null, null);
 		suiteRoleMembership6.addSite("SITE_01");
 		suiteRoleMembership6.forAllStudies();
 		Map<SuiteRole,SuiteRoleMembership> expectedMemberships6 = Collections.singletonMap(SuiteRole.AE_REPORTER, suiteRoleMembership6);
 		userWithAllStudiesStudySite = new PscUser(null, expectedMemberships6);
 	}
 	
 	public void testApplicationContextLoads() throws Exception {
         ApplicationContext loaded = new ClassPathXmlApplicationContext("classpath:applicationContext-grid-ae.xml");
         assertTrue("No beans loaded", loaded.getBeanDefinitionCount() > 0);
     }
 	
 	// test the user with no AE_REPORTER role
 	public void testAuthorizationForUserWithoutAeReporterRole() throws Exception{
 		logger.info("### Running Authorization test with no AE_REPORTER role");
 		AENotificationType ae = getNotification();
 		adverseEventConsumer.setGridServicesAuthorizationHelper(gridServicesAuthorizationHelper);
 		adverseEventConsumer.setPscUserDetailsService(pscUserDetailsService);
 
 		expect(gridServicesAuthorizationHelper.getCurrentUsername()).andReturn("John");
 		expect(pscUserDetailsService.loadUserByUsername("John")).andReturn(userWithoutAeReporterRole);
 
 		EasyMock.replay(gridServicesAuthorizationHelper);
 		EasyMock.replay(pscUserDetailsService);
 		try{
 			adverseEventConsumer.register(ae);
 			fail("Authorization Failed: RegsitrationConsumer should've thrown an exception!");
 		}catch(RemoteException e){
 			assertEquals("Test failed:","Access Denied: user does not have AE_REPORTER role", e.getMessage());
 		}
 		EasyMock.verify(gridServicesAuthorizationHelper);
 		EasyMock.verify(pscUserDetailsService);
 		
 		assertTrue("must not have any notificaitons.", studySubjectAssignment.getNotifications().isEmpty());
 		logger.info("### End Authorization test with no AE_REPORTER role");
 	}
 	
 	// User with AE_REPORTER role,Study and No StudySite
 	public void testAuthorizationForAeReporterRoleWithCorrectStudyNoStudySite() throws Exception{
 		logger.info("### Running Authorization test AE_REPORTER role, associated Study and no studySite");
 		AENotificationType ae = getNotification();
 		adverseEventConsumer.setGridServicesAuthorizationHelper(gridServicesAuthorizationHelper);
 		adverseEventConsumer.setPscUserDetailsService(pscUserDetailsService);
 
 		expect(gridServicesAuthorizationHelper.getCurrentUsername()).andReturn("John");
 		expect(pscUserDetailsService.loadUserByUsername("John")).andReturn(userWithIncorrectStudySite);
 
 		EasyMock.replay(gridServicesAuthorizationHelper);
 		EasyMock.replay(pscUserDetailsService);
 		try{
 			adverseEventConsumer.register(ae);
 			fail("Authorization Failed: RegsitrationConsumer should've thrown an exception!");
 		}catch(RemoteException e){
 			assertEquals("Test failed:","Access Denied: AE_REPORTER is not authorized for this Site Identifier : SITE_01", e.getMessage());
 		}
 		EasyMock.verify(gridServicesAuthorizationHelper);
 		EasyMock.verify(pscUserDetailsService);
 		
 		assertTrue("must not have any notificaitons.", studySubjectAssignment.getNotifications().isEmpty());
 		logger.info("### End Authorization test AE_REPORTER role, associated Study and no studySite");
 	}
 	
 	
 	// User with AE_REPORTER role, No Study
 	public void testAuthorizationForAeReporterRoleWithNoStudy() throws Exception{
 		logger.info("### Running Authorization test AE_REPORTER role and no study");
 		AENotificationType ae = getNotification();
 		adverseEventConsumer.setGridServicesAuthorizationHelper(gridServicesAuthorizationHelper);
 		adverseEventConsumer.setPscUserDetailsService(pscUserDetailsService);
 
 		expect(gridServicesAuthorizationHelper.getCurrentUsername()).andReturn("John");
 		expect(pscUserDetailsService.loadUserByUsername("John")).andReturn(userWithIncorrectStudy);
 
 		EasyMock.replay(gridServicesAuthorizationHelper);
 		EasyMock.replay(pscUserDetailsService);
 		try{
 			adverseEventConsumer.register(ae);
 			fail("Authorization Failed: RegsitrationConsumer should've thrown an exception!");
 		}catch(RemoteException e){
 			assertEquals("Test failed:","Access Denied: AE_REPORTER is not authorized for this Study Identifier : TEST_STUDY", e.getMessage());
 		}
 		EasyMock.verify(gridServicesAuthorizationHelper);
 		EasyMock.verify(pscUserDetailsService);
 		
 		assertTrue("must not have any notificaitons.", studySubjectAssignment.getNotifications().isEmpty());
 		logger.info("### End Authorization test AE_REPORTER role and no study");
 	}
 	
 
 	// User with AE_REPORTER role, AllStudies and AllSites
 	public void testAuthorizationForAeReporterRoleAllStudiesAllSites() throws Exception{
 		logger.info("### Running Authorization test AE_REPORTER role, AllStudies and AllSites");
 		AENotificationType ae = getNotification();
 		adverseEventConsumer.setGridServicesAuthorizationHelper(gridServicesAuthorizationHelper);
 		adverseEventConsumer.setPscUserDetailsService(pscUserDetailsService);
 
 		expect(gridServicesAuthorizationHelper.getCurrentUsername()).andReturn("John");
 		expect(pscUserDetailsService.loadUserByUsername("John")).andReturn(userWithAllStudiesAllSites);
 
 		EasyMock.replay(gridServicesAuthorizationHelper);
 		EasyMock.replay(pscUserDetailsService);
 
 		adverseEventConsumer.register(ae);
 		
 		EasyMock.verify(gridServicesAuthorizationHelper);
 		EasyMock.verify(pscUserDetailsService);
 		
 		Notification notification = studySubjectAssignment.getNotifications().get(0);
 		assertNotNull("AdverseEvent create test failed: ", notification);
 		logger.info("### End Authorization test AE_REPORTER role, AllStudies and AllSites");
 	}
 	
 	// User with AE_REPORTER role, AllStudies and StudySite
 	public void testAuthorizationForAeReporterRoleAllStudiesStudySite() throws Exception{
 		logger.info("### Running Authorization test AE_REPORTER role, AllStudies and site");
 		AENotificationType ae = getNotification();
 		adverseEventConsumer.setGridServicesAuthorizationHelper(gridServicesAuthorizationHelper);
 		adverseEventConsumer.setPscUserDetailsService(pscUserDetailsService);
 
 		expect(gridServicesAuthorizationHelper.getCurrentUsername()).andReturn("John");
 		expect(pscUserDetailsService.loadUserByUsername("John")).andReturn(userWithAllStudiesStudySite);
 
 		EasyMock.replay(gridServicesAuthorizationHelper);
 		EasyMock.replay(pscUserDetailsService);
 
 		adverseEventConsumer.register(ae);
 		
 		EasyMock.verify(gridServicesAuthorizationHelper);
 		EasyMock.verify(pscUserDetailsService);
 		
 		Notification notification = studySubjectAssignment.getNotifications().get(0);
 		assertNotNull("AdverseEvent create test failed: ", notification);
 		logger.info("### End Authorization test AE_REPORTER role, AllStudies and site");
 	}
 	
 	// User with AE_REPORTER role, associated Study and AllSites
 	public void testAuthorizationForAeReporterRoleWithCorrectStudyAllSites() throws Exception{
 		logger.info("### Running Authorization test AE_REPORTER role, associated Study and AllSites");
 		AENotificationType ae = getNotification();
 		adverseEventConsumer.setGridServicesAuthorizationHelper(gridServicesAuthorizationHelper);
 		adverseEventConsumer.setPscUserDetailsService(pscUserDetailsService);
 
 		expect(gridServicesAuthorizationHelper.getCurrentUsername()).andReturn("John");
 		expect(pscUserDetailsService.loadUserByUsername("John")).andReturn(userWithStudyAllSites);
 
 		EasyMock.replay(gridServicesAuthorizationHelper);
 		EasyMock.replay(pscUserDetailsService);
 
 		adverseEventConsumer.register(ae);
 		
 		EasyMock.verify(gridServicesAuthorizationHelper);
 		EasyMock.verify(pscUserDetailsService);
 		
 		Notification notification = studySubjectAssignment.getNotifications().get(0);
 		assertNotNull("AdverseEvent create test failed: ", notification);
 		logger.info("### End Authorization test AE_REPORTER role, associated Study and AllSites");
 	}
 	
 	
 	
 	
 	public void testCreateNotificationLocal() throws Exception {
 		logger.info("### Running AdverseEvent Consumer: create Notification test with AE_REPORTER role and correct study and stuDySite");
 		AENotificationType ae = getNotification();
 		adverseEventConsumer.setGridServicesAuthorizationHelper(gridServicesAuthorizationHelper);
 		adverseEventConsumer.setPscUserDetailsService(pscUserDetailsService);
 
 		expect(gridServicesAuthorizationHelper.getCurrentUsername()).andReturn("John");
 		expect(pscUserDetailsService.loadUserByUsername("John")).andReturn(user);
 
 		EasyMock.replay(gridServicesAuthorizationHelper);
 		EasyMock.replay(pscUserDetailsService);
 
 		adverseEventConsumer.register(ae);
 		
 		EasyMock.verify(gridServicesAuthorizationHelper);
 		EasyMock.verify(pscUserDetailsService);
 		
 		Notification notification = studySubjectAssignment.getNotifications().get(0);
 		assertNotNull("AdverseEvent create test failed: ", notification);
 		logger.info("### End AdverseEvent Consumer: create Notification test with AE_REPORTER role and correct study and stuDySite");
 	}
 
 	private AENotificationType getNotification() throws Exception {
 		AENotificationType ae = null;
 		InputStream config = Thread.currentThread().getContextClassLoader().getResourceAsStream("gov/nih/nci/cabig/ctms/grid/ae/client/client-config.wsdd");
 		Reader reader = null;
 		try{
 			if (aeFile != null){
 				reader = new FileReader(aeFile);
 			}else{
 				reader = new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(
 				"SampleAdverseEventMessage.xml"));
 			}
 			ae = (AENotificationType) Utils.deserializeObject(reader, AENotificationType.class, config);
 		}
 		catch (Exception ex) {
 			ex.printStackTrace();
 			fail("Error deserializing Adverse Event object: " + ex.getMessage());
 		}
 		return ae;
 	}
 
 	public void createSubjectAssigment() {
 		if (studySubjectAssignmentDao.getByGridId(assignmentGridId) == null) {
 			logger.debug("in createSubjectAssigment method");
 
 			Study study = studyDao.getByAssignedIdentifier(assignedIdentifier);
 			if (study == null) {
 				String message = "Study identified by assigned Identifier '" + assignedIdentifier + "' doesn't exist";
 				logger.error(message);
 				return;
 			}
 			StudySite studySite = findStudySite(study, nciCode);
 			if (studySite == null) {
 
 				String message = "The study '" + study.getLongTitle() + "', identified by assigned Identifier '" + assignedIdentifier
 				+ "' is not associated to a site identified by NCI code :'" + nciCode + "'";
 				logger.error(message);
 				return;
 			}
 
 			Subject subject = subjectDao.getByGridId(subjectGridId);
 
 			if (subject == null) {
 				subject = new Subject();
 				subject.setGridId(subjectGridId);
 				subject.setGender(Gender.MALE);
 				subject.setDateOfBirth(new Date());
 				subject.setFirstName("first name");
 				subject.setLastName(" last name");
 				subject.setPersonId("1234");
 
 				subjectDao.save(subject);
 				logger.debug("created subject: " + subject.getId());
 			} else {
 				logger.debug(String.format("subject %s found for given grid id %s", subject.getFullName(), subjectGridId));
 			}
 			StudySegment loadedStudySegment = null;
 			try {
 				loadedStudySegment = study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);
 			} catch (Exception e) {
 				String message = "The study '" + study.getLongTitle() + "', identified by Coordinating Center Identifier '" + assignedIdentifier
 				+ "' does not have any arm'";
 				logger.error(message);
 				return;
 			}
 			StudySubjectAssignment newAssignment = null;
 			try {
 				logger.debug("creating subject assignment");
 
                newAssignment = subjectService.assignSubject(
                    subject, studySite, loadedStudySegment, new Date(),
                    assignmentGridId, assignmentGridId, Collections.<Population>emptySet(), null
                );
 				ScheduledCalendar scheduledCalendar = newAssignment.getScheduledCalendar();
 				logger.debug("Created assignment " + newAssignment.getId());
 			} catch (StudyCalendarSystemException exp) {
 				logger.error("Error crating assignment. " + exp.getMessage());
 			}
 		} else {
 			logger.debug("assignment already exists for given grid id");
 		}
 	}
 
 	private StudySite findStudySite(final Study study, final String siteNCICode) {
 		for (StudySite studySite : study.getStudySites()) {
 			if (StringUtils.equals(studySite.getSite().getAssignedIdentifier(), siteNCICode)) {
 				return studySite;
 			}
 		}
 		return null;
 	}
 
 	public void createStudy() throws Exception {
 		if (studyDao.getByAssignedIdentifier(assignedIdentifier) == null) {
 			logger.debug("creating study for given identifer:" + assignedIdentifier);
 			Study study = TemplateSkeletonCreatorImpl.createBase(shortTitle);
 			study.setAssignedIdentifier(assignedIdentifier);
 			study.setLongTitle(longTitle);
 
 			Site site = siteDao.getByAssignedIdentifier(nciCode);
 			if (site == null) {
 				String message = "No site exists for given assigned identifier" + nciCode;
 				logger.error(message);
 				site = new Site();
 				site.setAssignedIdentifier(nciCode);
 				site.setName(nciCode);
 				siteService.createOrUpdateSite(site);
 			}
 			StudySite studySite = new StudySite();
 			studySite.setSite(site);
 			studySite.setStudy(study);
 			study.addStudySite(studySite);
 
 			TemplateSkeletonCreatorImpl.addEpoch(study, 0, Epoch.create("Treatment"));
 			Epoch epoch = new Epoch();
 			epoch.setName("Treatment");
 			StudySegment child = new StudySegment();
 			child.setName("Arm A");
 			epoch.addChild(child);
 			study.getPlannedCalendar().addEpoch(epoch);
 
 			studyService.save(study);
 
 			amendmentService.amend(study);
 
 			AmendmentApproval approvals = new AmendmentApproval();
 			approvals.setAmendment(study.getAmendment());
 			approvals.setDate(new Date());
 
 			amendmentService.approve(studySite, approvals);
 			logger.info("Created the study :" + study.getId());
 		} else {
 			logger.debug("study already exists for given identifier : " + assignedIdentifier);
 		}
 	}
 
 	private SiteService siteService;
 	private AmendmentService amendmentService;
 
 	public void setStudyService(StudyService studyService) {
 		this.studyService = studyService;
 	}
 
 	public void setStudyDao(StudyDao studyDao) {
 		this.studyDao = studyDao;
 	}
 
 	public void setSiteService(SiteService siteService) {
 		this.siteService = siteService;
 	}
 
 	public void setAmendmentService(AmendmentService amendmentService) {
 		this.amendmentService = amendmentService;
 	}
 
 	public void setSiteDao(SiteDao siteDao) {
 		this.siteDao = siteDao;
 	}
 
 	public void setAdverseEventConsumer(PSCAdverseEventConsumer adverseEventConsumer) {
 		this.adverseEventConsumer = adverseEventConsumer;
 	}
 
 	private void createAssignmentAndAeNotifications() {
 		studySubjectAssignment = studySubjectAssignmentDao.getByGridId(assignmentGridId);
 		if (studySubjectAssignment == null) {
 			createSubjectAssigment();
 			studySubjectAssignment = studySubjectAssignmentDao.getByGridId(assignmentGridId);
 		} else {
 			logger.error(String.format("no assignment found for given grid id %s", assignmentGridId));
 		}
 		logger.debug(String.format("Deleting subject's %s ae notifications", studySubjectAssignment.getSubject().getFullName()));
 		studySubjectAssignment.getNotifications().clear();
 		studySubjectAssignmentDao.save(studySubjectAssignment);
 		studySubjectAssignment = studySubjectAssignmentDao.getByGridId(assignmentGridId);
 		Assert.assertTrue("must not have any notificaitons.", studySubjectAssignment.getNotifications().isEmpty());
 		logger.debug(String.format("Sucessfully deleted subject's %s ae notifications", studySubjectAssignment.getSubject().getFullName()));
 	}
 
 
 	protected String[] getConfigLocations() {
 
 		String[] configs = {"classpath:applicationContext-grid-ae.xml"};
 		return configs;
 	}
 
 	public void setSubjectService(SubjectService subjectService) {
 		this.subjectService = subjectService;
 	}
 
 	public void setSubjectDao(SubjectDao subjectDao) {
 		this.subjectDao = subjectDao;
 	}
 
 	protected void onTearDownAfterTransaction() throws Exception {
 
 		DataAuditInfo.setLocal(null);
 	}
 
 
 	@Required
 	public void setStudySubjectAssignmentDao(StudySubjectAssignmentDao studySubjectAssignmentDao) {
 		this.studySubjectAssignmentDao = studySubjectAssignmentDao;
 	}
 }
