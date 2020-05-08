 package net.javacrumbs.springws.test.simple;
 
 import static org.easymock.EasyMock.createMock;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.replay;
 import static org.easymock.EasyMock.verify;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.fail;
 import net.javacrumbs.springws.test.RequestProcessor;
 import net.javacrumbs.springws.test.WsTestException;
 import net.javacrumbs.springws.test.validator.AbstractValidatorTest;
 
 import org.junit.Test;
 import org.springframework.ws.WebServiceMessage;
 
 
 public class LimitingRequestProcessorWrapperTest extends AbstractValidatorTest{
 
 	@Test
 	public void testCall() throws Exception
 	{
 		RequestProcessor processor = createMock(RequestProcessor.class);
 		WebServiceMessage request = getValidMessage();
 		WebServiceMessage response = getInvalidMessage();
 		expect(processor.processRequest(TEST_URI, messageFactory, request)).andReturn(response).once();
 		replay(processor);
 		
 		LimitingRequestProcessorWrapper wrapper = new LimitingRequestProcessorWrapper(processor,"Test processor");
 
 		try {
 			wrapper.verify();
 			fail("WsTestException expected");
 		} catch (WsTestException e) {
 			assertEquals("Test processor: Unexpected call, expected from 1 to 1 calls, was 0.",e.getMessage());
 		}
 		
 		//first call
 		assertSame(response, wrapper.processRequest(TEST_URI, messageFactory, request));
 		assertEquals(1, wrapper.getNumberOfProcessedRequests());
 		
 		wrapper.verify();
 		
 		//second call will be ignored
 		assertNull(wrapper.processRequest(TEST_URI, messageFactory, request));
 		
		//no problem
		wrapper.verify();
		
 		verify(processor);
 		
 	}
 	
 	@Test
 	public void testTwoTimesCall() throws Exception
 	{
 		RequestProcessor processor = createMock(RequestProcessor.class);
 		WebServiceMessage request = getValidMessage();
 		WebServiceMessage response = getInvalidMessage();
 		expect(processor.processRequest(TEST_URI, messageFactory, request)).andReturn(response).times(2);
 		replay(processor);
 		
 		LimitingRequestProcessorWrapper wrapper = new LimitingRequestProcessorWrapper(processor, "Test processor2");
 		wrapper.setMinNumberOfProcessedRequests(2);
 		wrapper.setMaxNumberOfProcessedRequests(2);
 		
 		//first call
 		assertSame(response, wrapper.processRequest(TEST_URI, messageFactory, request));
 		assertEquals(1, wrapper.getNumberOfProcessedRequests());
 		try {
 			wrapper.verify();
 			fail("WsTestException expected");
 		} catch (WsTestException e) {
 			assertEquals("Test processor2: Unexpected call, expected from 2 to 2 calls, was 1.",e.getMessage());
 		}
 		
 		//second call
 		assertSame(response, wrapper.processRequest(TEST_URI, messageFactory, request));
 		assertEquals(2, wrapper.getNumberOfProcessedRequests());
 		wrapper.verify();
 		
 		//third call will be ignored
 		assertNull(wrapper.processRequest(TEST_URI, messageFactory, request));
 		
 		verify(processor);
 		
 	}
 }
