 package org.jasig.portlet.blackboardvcportlet.dao.ws.impl;
 
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.when;
 
import java.io.IOException;

 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import org.jasig.portlet.blackboardvcportlet.dao.ws.SessionWSDao;
 import org.jasig.portlet.blackboardvcportlet.data.ConferenceUser;
 import org.jasig.portlet.blackboardvcportlet.service.SessionForm;
 import org.jasig.portlet.blackboardvcportlet.service.util.SASWebServiceOperations;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Matchers;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.elluminate.sas.BlackboardPresentationResponseCollection;
 import com.elluminate.sas.BlackboardSessionResponse;
 import com.elluminate.sas.BlackboardSuccessResponse;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 public class PresentationWSDaoImplTest extends PresentationWSDaoImplIT {
 	@Mock
 	private SASWebServiceOperations sasWebServiceOperations;
 	@Mock
 	private BlackboardSessionResponse session;
 	@Mock
 	private SessionWSDao sessionDao;
 	
 	@Before
 	public void thebefore() throws JAXBException {
 		MockitoAnnotations.initMocks(this);
 		when(sasWebServiceOperations.marshalSendAndReceiveToSAS(Matchers.contains("http://sas.elluminate.com/ListSessionPresentation"), any(Object.class))).thenReturn(getSinglePresentation());
 		when(sessionDao.createSession(any(ConferenceUser.class), any(SessionForm.class))).thenReturn(session);
 		super.sessionDao = this.sessionDao;
 		dao.setSasWebServiceOperations(sasWebServiceOperations);
 	}
 	
 	@Test
 	public void uploadPresentationTest() throws Exception {
 		when(sasWebServiceOperations.marshalSendAndReceiveToSAS(Matchers.contains("http://sas.elluminate.com/UploadRepositoryPresentation"), any(Object.class))).thenReturn(getSinglePresentation());
 		super.uploadPresentationTest();
 	}
 
 	@Test
 	public void linkPresentationToSessionTest() throws Exception {
 		when(sasWebServiceOperations.marshalSendAndReceiveToSAS(Matchers.contains("http://sas.elluminate.com/UploadRepositoryPresentation"), any(Object.class))).thenReturn(getSinglePresentation());
 		when(sasWebServiceOperations.marshalSendAndReceiveToSAS(Matchers.contains("http://sas.elluminate.com/SetSessionPresentation"), any(Object.class))).thenReturn(getSuccessfulResponse());
 		super.linkPresentationToSessionTest();
 	}
 
 	@Test
 	public void getSessionPresentations() throws Exception {
 		when(sasWebServiceOperations.marshalSendAndReceiveToSAS(Matchers.contains("http://sas.elluminate.com/UploadRepositoryPresentation"), any(Object.class))).thenReturn(getSinglePresentation());
 		when(sasWebServiceOperations.marshalSendAndReceiveToSAS(Matchers.contains("http://sas.elluminate.com/SetSessionPresentation"), any(Object.class))).thenReturn(getSuccessfulResponse());
 		when(sasWebServiceOperations.marshalSendAndReceiveToSAS(Matchers.contains("http://sas.elluminate.com/ListSessionPresentation"), any(Object.class))).thenReturn(getSinglePresentation());
 		super.getSessionPresentations();
 	}
 
 	@Test
 	public void getRepositoryPresentationsTest() throws Exception {
 		when(sasWebServiceOperations.marshalSendAndReceiveToSAS(Matchers.contains("http://sas.elluminate.com/UploadRepositoryPresentation"), any(Object.class))).thenReturn(getSinglePresentation());
 		when(sasWebServiceOperations.marshalSendAndReceiveToSAS(Matchers.contains("http://sas.elluminate.com/ListRepositoryPresentation"), any(Object.class))).thenReturn(getSinglePresentation());
 		
 		super.getRepositoryPresentationsTest();
 	}
 
 	@Test
 	public void deletePresentation() throws Exception {
 		when(sasWebServiceOperations.marshalSendAndReceiveToSAS(Matchers.contains("http://sas.elluminate.com/UploadRepositoryPresentation"), any(Object.class))).thenReturn(getSinglePresentation());
 		when(sasWebServiceOperations.marshalSendAndReceiveToSAS(Matchers.contains("http://sas.elluminate.com/RemoveRepositoryPresentation"), any(Object.class))).thenReturn(getSuccessfulResponse());
 		super.deletePresentation();
 	}
 
 	@Test
 	public void deleteSessionPresenation() throws Exception {
 		when(sasWebServiceOperations.marshalSendAndReceiveToSAS(Matchers.contains("http://sas.elluminate.com/UploadRepositoryPresentation"), any(Object.class))).thenReturn(getSinglePresentation());
 		when(sasWebServiceOperations.marshalSendAndReceiveToSAS(Matchers.contains("http://sas.elluminate.com/SetSessionPresentation"), any(Object.class))).thenReturn(getSuccessfulResponse());
 		when(sasWebServiceOperations.marshalSendAndReceiveToSAS(Matchers.contains("http://sas.elluminate.com/RemoveSessionPresentation"), any(Object.class))).thenReturn(getSuccessfulResponse());
 		super.deleteSessionPresenation();
 	}
 	
 	@SuppressWarnings("unchecked")
 	private JAXBElement<BlackboardPresentationResponseCollection> getSinglePresentation() throws JAXBException {
 		final JAXBContext context = JAXBContext.newInstance("com.elluminate.sas");
         final Unmarshaller unmarshaller = context.createUnmarshaller();
         JAXBElement<BlackboardPresentationResponseCollection> response = (JAXBElement<BlackboardPresentationResponseCollection>)unmarshaller.unmarshal(this.getClass().getResourceAsStream("/data/singleListRepositoryPresentationResponseCollection.xml"));
         
         return response;
 	}
 	
 	private BlackboardSuccessResponse getSuccessfulResponse() {
 		BlackboardSuccessResponse response = new BlackboardSuccessResponse();
 		response.setSuccess(true);
 		return response;
 	}
 }
