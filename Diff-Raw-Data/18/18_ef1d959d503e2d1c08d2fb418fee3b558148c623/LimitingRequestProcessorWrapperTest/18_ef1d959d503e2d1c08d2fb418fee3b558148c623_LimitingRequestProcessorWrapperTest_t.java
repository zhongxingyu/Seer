 /**
  * Copyright 2009-2010 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
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
			assertEquals("Test processor: has not been called enough times, expected at least 1 calls, has been 0.",e.getMessage());
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
			assertEquals("Test processor2: has not been called enough times, expected at least 2 calls, has been 1.",e.getMessage());
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
