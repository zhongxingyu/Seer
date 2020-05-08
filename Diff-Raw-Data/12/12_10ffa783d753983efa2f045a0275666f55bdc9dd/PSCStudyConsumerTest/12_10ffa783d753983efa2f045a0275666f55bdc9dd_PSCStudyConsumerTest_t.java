 package edu.northwestern.bioinformatics.studycalendar.grid;
 
 import java.io.FileReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.Date;
 import java.util.Collections;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.test.AbstractTransactionalSpringContextTests;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
 import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
 import org.easymock.classextension.EasyMock;
 import static org.easymock.EasyMock.expect;
 
 import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
 import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;
 
 import java.rmi.RemoteException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
import gov.nih.nci.ccts.grid.studyconsumer.stubs.types.InvalidStudyException;
import gov.nih.nci.ccts.grid.studyconsumer.stubs.types.StudyCreationException;
 
 
 public class PSCStudyConsumerTest extends AbstractTransactionalSpringContextTests {
 
 	public static final Log logger = LogFactory.getLog(PSCStudyConsumerTest.class);
 	private String regFile;
 	private PSCStudyConsumer studyConsumer;	
 	private StudyService studyService;
 	private String ccIdentifier = "TEST_STUDY";
 	private StudyGridServiceAuthorizationHelper gridServicesAuthorizationHelper;
 	private PscUserDetailsService pscUserDetailsService;
 	private PscUser user;
 	private PscUser userWithoutStudyCreatorRole;
 	private PscUser userWithIncorrectStudySite;
 	private PscUser userWithStudyCreatorAllSites;
 	
 	
 	protected void onSetUpInTransaction() throws Exception {
 		DataAuditInfo.setLocal(new DataAuditInfo("test", "localhost", new Date(), "/wsrf-psc/services/cagrid/StudyConsumer"));
 		regFile = System.getProperty("psc.test.sampleStudyFile");
 	
 		gridServicesAuthorizationHelper=EasyMock.createMock(StudyGridServiceAuthorizationHelper.class);
 		pscUserDetailsService=EasyMock.createMock(PscUserDetailsService.class);
 
 		SuiteRoleMembership suiteRoleMembership = new SuiteRoleMembership(SuiteRole.STUDY_CREATOR, null, null);
 		suiteRoleMembership.addSite("TEST_SITE");
 		Map<SuiteRole,SuiteRoleMembership> expectedMemberships = Collections.singletonMap(SuiteRole.STUDY_CREATOR,suiteRoleMembership);
 		user = new PscUser(null, expectedMemberships);
 		
 		
 		// user without STUDY_CTEATOR Role
 		SuiteRoleMembership suiteRoleMembership1 = new SuiteRoleMembership(SuiteRole.REGISTRAR, null, null);
 		suiteRoleMembership1.addSite("TEST_SITE");
 		Map<SuiteRole,SuiteRoleMembership> expectedMemberships1 = Collections.singletonMap(SuiteRole.REGISTRAR,suiteRoleMembership1);
 		userWithoutStudyCreatorRole = new PscUser(null, expectedMemberships1);
 		
 		
 		// user with STUDY_CREATOR Role and incorrect study Site associated
 		SuiteRoleMembership suiteRoleMembership2 = new SuiteRoleMembership(SuiteRole.STUDY_CREATOR, null, null);
 		Map<SuiteRole,SuiteRoleMembership> expectedMemberships2 = Collections.singletonMap(SuiteRole.STUDY_CREATOR,suiteRoleMembership2);
 		userWithIncorrectStudySite = new PscUser(null, expectedMemberships2);
 		
 		// user with STUDY_CREATOR Role and AllSites true
 		SuiteRoleMembership suiteRoleMembership3 = new SuiteRoleMembership(SuiteRole.STUDY_CREATOR, null, null);
 		suiteRoleMembership3.forAllSites();
 		Map<SuiteRole,SuiteRoleMembership> expectedMemberships3 = Collections.singletonMap(SuiteRole.STUDY_CREATOR,suiteRoleMembership3);
 		userWithStudyCreatorAllSites = new PscUser(null, expectedMemberships3);
 
 	}
 	
 	// test the user with no STUDY_CREATOR role
 	public void testAuthorizationForNoStudyCreatorRole() throws Exception{
 		logger.info("### Running Authorization test with User not having STUDY_CREATOR role");
 		gov.nih.nci.cabig.ccts.domain.Study study = populateStudyDTO();
 		
 		studyConsumer.setGridServicesAuthorizationHelper(gridServicesAuthorizationHelper);
 		studyConsumer.setPscUserDetailsService(pscUserDetailsService);
 
 		expect(gridServicesAuthorizationHelper.getCurrentUsername()).andReturn("John");
 		expect(pscUserDetailsService.loadUserByUsername("John")).andReturn(userWithoutStudyCreatorRole);
 
 		EasyMock.replay(gridServicesAuthorizationHelper);
 		EasyMock.replay(pscUserDetailsService);
 
 		try{
 			studyConsumer.createStudy(study);
 			fail("createStudy() should've thrown an exception!");
		}catch(InvalidStudyException e){
			assertEquals("Test failed:","Access Denied: user does not have STUDY_CREATOR role", e.getFaultReason());
 		}
 		EasyMock.verify(gridServicesAuthorizationHelper);
 		EasyMock.verify(pscUserDetailsService);
 		Study pscStudy = studyService.getStudyByAssignedIdentifier(ccIdentifier);
 		assertNull("Authorization failed; Study should not get created ", pscStudy);
 	}
 	
 	// test the user with STUDY_CREATOR role and with wrong StudySite 
 	public void testAuthorizationForStudyCreatorRoleAndNoStudySite() throws Exception{
 		logger.info("### Running authorization test with User having STUDY_CREATOR role and no matching studysite");
 		gov.nih.nci.cabig.ccts.domain.Study study = populateStudyDTO();
 		
 		studyConsumer.setGridServicesAuthorizationHelper(gridServicesAuthorizationHelper);
 		studyConsumer.setPscUserDetailsService(pscUserDetailsService);
 
 		expect(gridServicesAuthorizationHelper.getCurrentUsername()).andReturn("John");
 		expect(pscUserDetailsService.loadUserByUsername("John")).andReturn(userWithIncorrectStudySite);
 
 		EasyMock.replay(gridServicesAuthorizationHelper);
 		EasyMock.replay(pscUserDetailsService);
 
 		try{
 			studyConsumer.createStudy(study);
 			fail("createStudy().fetchSite() should've thrown an exception!");
		}catch(StudyCreationException e){
			assertEquals("Test failed:","Access Denied: Study_Creator is not authorized for the Site Identifier : TEST_SITE", e.getFaultReason());
 		}
 		EasyMock.verify(gridServicesAuthorizationHelper);
 		EasyMock.verify(pscUserDetailsService);
 		Study pscStudy = studyService.getStudyByAssignedIdentifier(ccIdentifier);
 		assertNull("Authorization failed; Study should not get created ", pscStudy);
 	}
 	
 	// test authorization with STUDY_CREATOR role and allSites true
 	public void testAuthorizationWithStudyCreatorRoleAndAllSites() throws Exception{
 		logger.info("### Running authorization test with STUDY_CREATOR role and AllSites true");
 		gov.nih.nci.cabig.ccts.domain.Study study = populateStudyDTO();
 		
 		studyConsumer.setGridServicesAuthorizationHelper(gridServicesAuthorizationHelper);
 		studyConsumer.setPscUserDetailsService(pscUserDetailsService);
 
 		expect(gridServicesAuthorizationHelper.getCurrentUsername()).andReturn("John");
 		expect(pscUserDetailsService.loadUserByUsername("John")).andReturn(userWithStudyCreatorAllSites);
 
 		EasyMock.replay(gridServicesAuthorizationHelper);
 		EasyMock.replay(pscUserDetailsService);
 
 		try{
 			studyConsumer.createStudy(study);
 		}catch(Exception exp){
 			fail("Test Failed: Exception shouldn't have come");
 		}
 		
 		EasyMock.verify(gridServicesAuthorizationHelper);
 		EasyMock.verify(pscUserDetailsService);	
 
 		Study pscStudy = studyService.getStudyByAssignedIdentifier(ccIdentifier);
 		assertNotNull("must create study", pscStudy);
 		assertNotNull("must create study", pscStudy.getId());
 	}
 	
 	
 	// test the create study with user(STUDY_CREATOR role and proper StudySite associated)
 	public void testCreateStudyLocal() throws Exception {
 		logger.info("### Running test create study local method");
 		gov.nih.nci.cabig.ccts.domain.Study study = populateStudyDTO();
 		
 		studyConsumer.setGridServicesAuthorizationHelper(gridServicesAuthorizationHelper);
 		studyConsumer.setPscUserDetailsService(pscUserDetailsService);
 
 		expect(gridServicesAuthorizationHelper.getCurrentUsername()).andReturn("John");
 		expect(pscUserDetailsService.loadUserByUsername("John")).andReturn(user);
 
 		EasyMock.replay(gridServicesAuthorizationHelper);
 		EasyMock.replay(pscUserDetailsService);
 
 		studyConsumer.createStudy(study);
 		
 		EasyMock.verify(gridServicesAuthorizationHelper);
 		EasyMock.verify(pscUserDetailsService);
 		
 		Study pscStudy = studyService.getStudyByAssignedIdentifier(ccIdentifier);
 		assertNotNull("must create study", pscStudy);
 		assertNotNull("must create study", pscStudy.getId());
 	}
 	
 	public void testRollbackStudyLocal() throws Exception {
 		logger.info("### Running test rollback study local method");
 		gov.nih.nci.cabig.ccts.domain.Study study = populateStudyDTO();
 		logger.info("Creating local study");
 		
 		studyConsumer.setGridServicesAuthorizationHelper(gridServicesAuthorizationHelper);
 		studyConsumer.setPscUserDetailsService(pscUserDetailsService);
 
 		expect(gridServicesAuthorizationHelper.getCurrentUsername()).andReturn("John");
 		expect(pscUserDetailsService.loadUserByUsername("John")).andReturn(user);
 
 		EasyMock.replay(gridServicesAuthorizationHelper);
 		EasyMock.replay(pscUserDetailsService);
 		
 		studyConsumer.createStudy(study);
 		
 		EasyMock.verify(gridServicesAuthorizationHelper);
 		EasyMock.verify(pscUserDetailsService);
 		
 		Study pscStudy = studyService.getStudyByAssignedIdentifier(ccIdentifier);
 		assertNotNull("must create study", pscStudy);
 		assertNotNull("must create study", pscStudy.getId());
 		
 		logger.info("Calling Rollback study method");
 		
 		studyConsumer.rollback(study);
 		pscStudy = studyService.getStudyByAssignedIdentifier(ccIdentifier);
 		assertNull("Study rollback error : ", pscStudy);
 	}
 
 
 	private gov.nih.nci.cabig.ccts.domain.Study populateStudyDTO() throws Exception {
 		gov.nih.nci.cabig.ccts.domain.Study studyDTO = null;
 		try {
 			InputStream config = Thread.currentThread().getContextClassLoader().getResourceAsStream(
 					"gov/nih/nci/ccts/grid/studyconsumer/client/client-config.wsdd");
 			Reader reader = null;
 			if (regFile != null){
 				reader = new FileReader(regFile);
 			}else{
 				reader = new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("SampleStudyMessage.xml"));
 			}
 			studyDTO = (gov.nih.nci.cabig.ccts.domain.Study) gov.nih.nci.cagrid.common.Utils.deserializeObject(reader, gov.nih.nci.cabig.ccts.domain.Study.class, config);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			throw e;
 		}
 		return studyDTO;
 	}
 
 
 	protected String[] getConfigLocations() {
 
 		String[] configs = {"classpath:applicationContext-studyConsumer-grid.xml"};
 		return configs;
 	}
 
 	protected void onTearDownAfterTransaction() throws Exception {
 		DataAuditInfo.setLocal(null);
 		
 	}
 
 	@Required
 	public void setStudyConsumer(PSCStudyConsumer studyConsumer) {
 		this.studyConsumer = studyConsumer;
 	}
 
 	@Required
 	public void setStudyService(StudyService studyService) {
 		this.studyService = studyService;
 	}
 
 }
