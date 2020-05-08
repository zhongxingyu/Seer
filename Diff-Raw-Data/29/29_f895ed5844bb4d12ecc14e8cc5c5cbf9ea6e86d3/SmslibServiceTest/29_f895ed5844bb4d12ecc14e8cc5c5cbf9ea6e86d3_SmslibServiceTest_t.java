 package net.frontlinesms.camel.smslib;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 import org.smslib.CIncomingMessage;
 import org.smslib.COutgoingMessage;
 import org.smslib.CService;
import org.smslib.service.MessageClass;
 
 import serial.mock.MockSerial;
 import serial.NoSuchPortException;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 /** Unit test for {@link SmslibService} */
 public class SmslibServiceTest {
 	CService cServiceMock;
 	CServiceFactory cServiceFactory;
 
 	Map<String, Object> parameters;
 	String remaining;
 	String uri;
 	
 	SmslibService service;
 	
 	@Before
 	public void setUp() {
 		MockSerial.init();
 
 		uri = "asdf";
 		remaining = "hjkl";
 		parameters = new HashMap<String, Object>();
 		
 		cServiceMock = mock(CService.class);
 		
 		cServiceFactory = mock(CServiceFactory.class);
 		when(cServiceFactory.create(uri, remaining, parameters)).thenReturn(cServiceMock);
 		
 		service = new SmslibService(cServiceFactory, uri, remaining, parameters);
 	}
 	
 	@After
 	public void tearDown() {
 		cServiceMock = null;
 		cServiceFactory = null;
 		
 		parameters = null;
 		remaining = null;
 		uri = null;
 		
 		service = null;
 	}
 	
 	@Test
 	public void testCServiceCreation() throws NoSuchPortException {
 		verify(cServiceFactory).create(uri, remaining, parameters);
 	}
 	
 	@Test
 	public void testCServiceStart() throws Exception {
 		// given
 		SmslibProducer mockProducer = mock(SmslibProducer.class);
 		service.setProducer(mockProducer);
 		
 		// when
 		service.startForProducer();
 		
 		// then
 		verify(cServiceMock).connect();
 	}
 	
 	@Test
 	public void testCServiceNoStartIfAlreadyStarted() throws Exception {
 		// given
 		SmslibProducer mockProducer = mock(SmslibProducer.class);
 		service.setProducer(mockProducer);
 		when(cServiceMock.isConnected()).thenReturn(true); // TODO this assumes that CService.isConnected() returns true while TRYING to conenct...
 		
 		// when
 		service.startForProducer();
 		
 		// then
 		verify(cServiceMock, never()).connect();
 	}
 	
 	@Test
 	public void testCServiceStop() throws Exception {
 		// given
 		service.setProducer(mock(SmslibProducer.class));
 		when(cServiceMock.isConnected()).thenReturn(true);
 		
 		// when
 		service.stopForProducer();
 		
 		// verify
 		verify(cServiceMock).disconnect();
 	}
 	
 	@Test
 	public void testCServiceNoStopIfNotStarted() throws Exception {
 		// given
 		service.setProducer(mock(SmslibProducer.class));
 		
 		// when
 		service.stopForProducer();
 		
 		// verify
 		verify(cServiceMock, never()).disconnect();
 	}
 	
 	@Test
 	public void testCServiceNoStopIfOtherUsers() throws Exception {
 		// given
 		service.setProducer(mock(SmslibProducer.class));
 		service.startForProducer();
 		service.setConsumer(mock(SmslibConsumer.class));
 		service.startForConsumer();
 		when(cServiceMock.isConnected()).thenReturn(true);
 		
 		// when
 		service.stopForProducer();
 		
 		// verify
 		verify(cServiceMock, never()).disconnect();
 	}
 
 	@Test
 	public void testCServiceStopIfNoMoreUsers() throws Exception {
 		// given		
 		SmslibProducer mockProducer = mock(SmslibProducer.class);
 		service.setProducer(mockProducer);
 		service.startForProducer();
 		SmslibConsumer mockConsumer = mock(SmslibConsumer.class);
 		service.setConsumer(mockConsumer);
 		service.startForConsumer();
 		when(cServiceMock.isConnected()).thenReturn(true);
 		
 		// when
 		service.stopForProducer();
 		verify(cServiceMock, never()).disconnect();
 		
 		// and
 		service.stopForConsumer();
 		
 		// verify
 		verify(cServiceMock).disconnect();
 	}
 	
 	@Test
 	public void testMaxOneProducer() throws Exception {
 		// given
 		service.setProducer(mock(SmslibProducer.class));
 		
 		// when then
 		try {
 			service.setProducer(mock(SmslibProducer.class));
 			fail("Should not be able to start for more than one producer.");
 		} catch(SmslibServiceException ex) {
 			// expected
 		}
 	}
 	
 	@Test
 	public void testMaxOneConsumer() throws Exception {
 		// given
 		service.setConsumer(mock(SmslibConsumer.class));
 		
 		// when then
 		try {
 			service.setConsumer(mock(SmslibConsumer.class));
 			fail("Should not be able to start for more than one producer.");
 		} catch(SmslibServiceException ex) {
 			// expected
 		}
 	}
 	
 	@Test
 	public void testSend() throws Exception {
 		// given
 		COutgoingMessage cOutgoingMessageMock = mock(COutgoingMessage.class);
 		OutgoingSmslibCamelMessage camelMessage = new OutgoingSmslibCamelMessage(cOutgoingMessageMock);
 		
 		// when
 		service.send(camelMessage);
 		
 		// then
 		verify(cServiceMock).sendMessage(cOutgoingMessageMock);
 	}
 	
 	@Test
 	public void testSendFailureHandlingCmsError() throws Exception {
 		// given
 		COutgoingMessage smslibMessage = mock(COutgoingMessage.class);
 		when(smslibMessage.getRefNo()).thenReturn(-1);
 		OutgoingSmslibCamelMessage camelMessage = new OutgoingSmslibCamelMessage(smslibMessage);
 		
 		try {
 			service.send(camelMessage);
 			fail();
 		} catch(MessageRejectedException expected) {}
 	}
 	
 	@Test
 	public void testSendFailureHandlingForCserviceStopped() throws Exception {
 		// given
 		COutgoingMessage smslibMessage = mock(COutgoingMessage.class);
 		OutgoingSmslibCamelMessage camelMessage = new OutgoingSmslibCamelMessage(smslibMessage);
 		doThrow(new ConnectionFailedException()).when(cServiceMock).sendMessage(smslibMessage);
 		
 		// when then
 		try {
 			service.send(camelMessage);
 		} catch(ConnectionFailedException e) {
 			// expected
 		}
 	}
 	
 	@Test
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public void testReceive() throws Exception {
 		// given
 		SmslibConsumer consumerMock = mock(SmslibConsumer.class);
 		service.setConsumer(consumerMock);
 		doAnswer(new Answer() {
 			public Object answer(InvocationOnMock inv) {
 				LinkedList<CIncomingMessage> messageList =
 						(LinkedList<CIncomingMessage>) inv.getArguments()[0];
 				for(int i=0; i<3; ++i) messageList.add(mock(CIncomingMessage.class));
 				return null;
 			}
 		}).when(cServiceMock).readMessages(any(LinkedList.class), eq(MessageClass.ALL));
 		
 		// when
 		service.doReceive();
 		// then
 		verify(consumerMock, times(3)).accept(any(IncomingSmslibCamelMessage.class));
 		
 		// when
 		reset(consumerMock);
 		service.setReceiveMessageClass(MessageClass.UNREAD);
 		service.doReceive();
 		// then
 		verify(consumerMock, never()).accept(any(IncomingSmslibCamelMessage.class));
 		
 		// when
 		reset(consumerMock);
 		service.setReceiveMessageClass(MessageClass.ALL);
 		service.doReceive();
 		// then
 		verify(consumerMock, times(3)).accept(any(IncomingSmslibCamelMessage.class));
 	}
 
 	@Test
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public void testReceiveUnread() throws Exception {
 		// given
 		parameters.put("receiveMessageClass", MessageClass.UNREAD);
 		service = new SmslibService(cServiceFactory, uri, remaining, parameters);
 		
 		// given
 		SmslibConsumer consumerMock = mock(SmslibConsumer.class);
 		service.setConsumer(consumerMock);
 		doAnswer(new Answer() {
 			public Object answer(InvocationOnMock inv) {
 				LinkedList<CIncomingMessage> messageList =
 						(LinkedList<CIncomingMessage>) inv.getArguments()[0];
 				for(int i=0; i<3; ++i) messageList.add(mock(CIncomingMessage.class));
 				return null;
 			}
 		}).when(cServiceMock).readMessages(any(LinkedList.class), eq(MessageClass.UNREAD));
 		
 		// when
 		service.doReceive();
 		// then
 		verify(consumerMock, times(3)).accept(any(IncomingSmslibCamelMessage.class));
 	}
 	
 	@Test
 	public void ifStartForProducerFailsThenProducerShouldBeNull() throws Exception {
 		// given
 		SmslibProducer prod = mock(SmslibProducer.class);
 		service.setProducer(prod);
 		doThrow(new NoSuchPortException(new RuntimeException())).when(cServiceMock).connect();
 		
 		// when
 		try {
 			service.startForProducer();
 			fail(".startForProducer() should have thrown serial Exception");
 		} catch(NoSuchPortException expected) {}
 		
 		// then
 		assertNull(service.getProducer());
 		assertFalse(service.isProducerRunning());
 	}
 }
