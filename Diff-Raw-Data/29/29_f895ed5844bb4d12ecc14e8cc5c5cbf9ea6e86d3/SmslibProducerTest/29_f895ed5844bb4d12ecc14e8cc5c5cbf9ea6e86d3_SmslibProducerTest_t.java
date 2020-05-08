 package net.frontlinesms.camel.smslib;
 
 import static org.mockito.Mockito.*;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /** JUnit tests for {@link SmslibProducer} */
 public class SmslibProducerTest {
 	SmslibEndpoint mockEndpoint;
 	SmslibService mockSmslibService;
 	SmslibProducer p;
 	
 	@Before
 	public void setUp() {
 		mockEndpoint = mock(SmslibEndpoint.class);
 		mockSmslibService = mock(SmslibService.class);
 		p = new SmslibProducer(mockEndpoint, mockSmslibService);
 	}
 	
 	@After
 	public void tearDown() {
 		mockEndpoint = null;
 		mockSmslibService = null;
 		p = null;
 	}
 	
 	@Test
 	public void testDoStartShouldStartService() throws Exception {
 		p.doStart();
 		verify(mockSmslibService).startForProducer();
 	}
 	
 	@Test
 	public void testDoStopShouldStopService() throws Exception {
 		p.stop();
 		verify(mockSmslibService).stopForProducer();
 	}
 	
 //	@Test
 //	public void testProcessing() throws Exception {
 //		Exchange sendMessageExchange = mock(Exchange.class);
 //		OutgoingSmslibCamelMessage message = mock(OutgoingSmslibCamelMessage.class);
 //		when(sendMessageExchange.getIn()).thenReturn(message);
 //
 //		p.process(sendMessageExchange);
 //		verify(mockSmslibService).send(message);
 //	}
 }
