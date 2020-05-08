 package org.jasig.portlet.blackboardvcportlet.dao.ws.impl;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.activation.DataHandler;
 import javax.mail.util.ByteArrayDataSource;
 
 import org.jasig.portlet.blackboardvcportlet.dao.ws.PresentationWSDao;
 import org.jasig.portlet.blackboardvcportlet.dao.ws.SessionWSDao;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.elluminate.sas.BlackboardPresentationResponse;
 import com.elluminate.sas.BlackboardSessionResponse;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = "classpath:/test-applicationContext.xml")
 public class PresentationWSDaoImplIT extends AbstractWSIT {
 	
 	@Autowired
 	PresentationWSDao dao;
 	
 	@Autowired
 	SessionWSDao sessionDao;
 	
 	private Map<Long, BlackboardPresentationResponse> presentations = new HashMap <Long, BlackboardPresentationResponse>();
 	
 	@Before
 	public void before() {
 		form = buildSession();
 		user = buildUser();
 		session = sessionDao.createSession(user, form);
 	}
 	
 	@After
 	public void after() {
 		List<BlackboardSessionResponse> sessions = sessionDao.getSessions(null, null, null, user.getUniqueId(), null, null, null);
 		for(BlackboardSessionResponse session : sessions ) {
 			List<BlackboardPresentationResponse> sessionPresentations = dao.getSessionPresentations(session.getSessionId());
 			for(BlackboardPresentationResponse presenation : sessionPresentations) {
 				dao.deleteSessionPresenation(session.getSessionId(), presenation.getPresentationId());
 			}
 			sessionDao.deleteSession(session.getSessionId());			
 		}
 		
 		for(Map.Entry<Long, BlackboardPresentationResponse> presentation : presentations.entrySet()) {
 			dao.deletePresentation(presentation.getKey());
 		}
 		
 	}
 
 	@Test
 	public void uploadPresentationTest() throws Exception {
 		BlackboardPresentationResponse createRepoPresentation = createRepoPresentation();
 		assertNotNull(createRepoPresentation);
 	}
 
 	@Test
 	public void linkPresentationToSessionTest() throws Exception {
 		BlackboardPresentationResponse createRepoPresentation = createRepoPresentation();
 		assertNotNull(createRepoPresentation);
 		assertTrue(dao.linkPresentationToSession(session.getSessionId(), createRepoPresentation.getPresentationId()));
 		
 	}
 
 	@Test
 	public void getSessionPresentations() throws Exception {
 		linkPresentationToSessionTest();
 		List<BlackboardPresentationResponse> sessionPresentations = dao.getSessionPresentations(session.getSessionId());
 		assertEquals(sessionPresentations.size(),1);
 	}
 
 	@Test
 	public void getRepositoryPresentationsTest() throws Exception {
 		BlackboardPresentationResponse repoPresentation = createRepoPresentation();
 		List<BlackboardPresentationResponse> repositoryPresentations = dao.getRepositoryPresentations(user.getUniqueId(), repoPresentation.getPresentationId(), null);
 		assertNotNull(repositoryPresentations);
 		assertEquals(repositoryPresentations.size(),1);
 		assertEquals(repoPresentation,repositoryPresentations.get(0));
 	}
 
 	@Test
 	public void deletePresentation() throws Exception {
 		BlackboardPresentationResponse repoPresentation = createRepoPresentation();
 		boolean deletePresentation = dao.deletePresentation(repoPresentation.getPresentationId());
 		presentations.remove(repoPresentation.getPresentationId());
 		assertTrue(deletePresentation);
 	}
 
 	@Test
 	public void deleteSessionPresenation() throws Exception {
 		BlackboardPresentationResponse createRepoPresentation = createRepoPresentation();
 		assertNotNull(createRepoPresentation);
 		assertTrue(dao.linkPresentationToSession(session.getSessionId(), createRepoPresentation.getPresentationId()));
 		dao.deleteSessionPresenation(session.getSessionId(), createRepoPresentation.getPresentationId());
 	}
 	
 	private BlackboardPresentationResponse createRepoPresentation() throws Exception {
 		InputStream is = new ByteArrayInputStream("fdsdfsfsdadsfasfda".getBytes());
         ByteArrayDataSource rawData = new ByteArrayDataSource(is,"video/mpeg");
 		DataHandler dataHandler = new DataHandler(rawData);
 
 		BlackboardPresentationResponse presenetation = dao.uploadPresentation(user.getUniqueId(), "test.elp", "aliens",dataHandler);
         presentations.put(presenetation.getPresentationId(), presenetation);
         return presenetation;
 	}
 
 }
