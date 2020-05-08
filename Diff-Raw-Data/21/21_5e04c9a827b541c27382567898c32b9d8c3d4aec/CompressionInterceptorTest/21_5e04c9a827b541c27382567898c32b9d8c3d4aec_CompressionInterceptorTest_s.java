 /*******************************************************************************
  * Copyright (c) 2008, 2009 SOPERA GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     SOPERA GmbH - initial API and implementation
  *******************************************************************************/
 package org.eclipse.swordfish.plugins.compression.tests;
 
 import static org.easymock.EasyMock.createMock;
 import static org.easymock.EasyMock.createStrictMock;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.replay;
 
 import javax.jbi.messaging.MessageExchange;
 import javax.jbi.messaging.NormalizedMessage;
 import javax.xml.transform.Source;
 
 import org.eclipse.swordfish.core.SwordfishException;
 import org.eclipse.swordfish.plugins.compression.CompressionInterceptor;
 import org.eclipse.swordfish.plugins.compression.Compressor;
 import org.junit.Before;
 import org.junit.Test;
 
 public class CompressionInterceptorTest {
 	private static final String CORRELATION_ID = "CorrelationID";
 	private CompressionInterceptor interceptor;
 	
 	
 	@Before
 	public void setUp() throws Exception {
 		interceptor = new CompressionInterceptor();
 	}
 
 	
 	@Test
 	public void testProcessEmptyConsumerRequestDoesNoCompression() throws Exception {
 		Source src = createStrictMock(Source.class);
 		Compressor cpr = createStrictMock(Compressor.class);
 		NormalizedMessage nMsg = createStrictMock(NormalizedMessage.class);
 		MessageExchange mx = createRequestExchange(nMsg, null);
 	
 		expect(cpr.isSourceEmpty(src)).andReturn(true); // Request message empty
 		expect(nMsg.getContent()).andReturn(src);
 		
 		replay(cpr, nMsg, src);
 		interceptor.setCompressor(cpr);
 		interceptor.process(mx);
 	}
 
 	
 	@Test(expected=SwordfishException.class)
 	public void testProcessConsumerRequestFailsWithCompressionProblems() throws Exception {
 		Source src = createStrictMock(Source.class);
 		Compressor cpr = createStrictMock(Compressor.class);
 		NormalizedMessage nMsg = createStrictMock(NormalizedMessage.class);
 		MessageExchange mx = createRequestExchange(nMsg, null);
 
 		expect(cpr.isSourceEmpty(src)).andReturn(false);
 		expect(nMsg.getContent()).andReturn(src);
 		expect(cpr.asCompressedSource(src)).andThrow(new SwordfishException()); // Failure here!
 		
 		replay(cpr, nMsg, src);
 		interceptor.setCompressor(cpr);
 		interceptor.process(mx);
 	}
 
 	
 	@Test
 	public void testProcessConsumerRequest() throws Exception {
 		Source src = createStrictMock(Source.class);
 		Compressor cpr = createStrictMock(Compressor.class);
 		NormalizedMessage nMsg = createStrictMock(NormalizedMessage.class);
 		MessageExchange mx = createRequestExchange(nMsg, null);
 
 		expect(cpr.isSourceEmpty(src)).andReturn(false);
 		expect(nMsg.getContent()).andReturn(src);
 		expect(cpr.asCompressedSource(src)).andReturn(src);
 		nMsg.setContent(src);
 		
 		replay(cpr, nMsg, src);
 		interceptor.setCompressor(cpr);
 		interceptor.process(mx);
 	}
 
 	
 	@Test
 	public void testProcessEmptyProviderRequestDoesNoUncompression() throws Exception {
 		Source src = createStrictMock(Source.class);
 		Compressor cpr = createStrictMock(Compressor.class);
 		NormalizedMessage nMsg = createStrictMock(NormalizedMessage.class);
 		MessageExchange mx = createRequestExchange(nMsg, CORRELATION_ID);
 
 		expect(cpr.isSourceEmpty(src)).andReturn(true); // Request message empty
 		expect(nMsg.getContent()).andReturn(src);
		expect(cpr.asUncompressedSource(src)).andReturn(src);
 		
 		replay(cpr, nMsg, src);
 		interceptor.setCompressor(cpr);
 		interceptor.process(mx);
 	}
 
 	
 	@Test(expected=SwordfishException.class)
 	public void testProcessProviderRequestFailsWithUncompressionProblems() throws Exception {
 		Source src = createStrictMock(Source.class);
 		Compressor cpr = createStrictMock(Compressor.class);
 		NormalizedMessage nMsg = createStrictMock(NormalizedMessage.class);
 		MessageExchange mx = createRequestExchange(nMsg, CORRELATION_ID);
 
 		expect(cpr.isSourceEmpty(src)).andReturn(false);
 		expect(nMsg.getContent()).andReturn(src);
 		expect(cpr.asUncompressedSource(src)).andThrow(new SwordfishException()); // Failure here!
 		
 		replay(cpr, nMsg, src);
 		interceptor.setCompressor(cpr);
 		interceptor.process(mx);
 	}
 
 	
 	@Test
 	public void testProcessProviderRequest() throws Exception {
 		Source src = createStrictMock(Source.class);
 		Compressor cpr = createStrictMock(Compressor.class);
 		NormalizedMessage nMsg = createStrictMock(NormalizedMessage.class);
 		MessageExchange mx = createRequestExchange(nMsg, CORRELATION_ID);
 
 		expect(cpr.isSourceEmpty(src)).andReturn(false);
 		expect(nMsg.getContent()).andReturn(src);
 		expect(cpr.asUncompressedSource(src)).andReturn(src);
 		nMsg.setContent(src);
 		
 		replay(cpr, nMsg, src);
 		interceptor.setCompressor(cpr);
 		interceptor.process(mx);
 	}
 
 	
 	@Test
 	public void testProcessProviderResponse() throws Exception {
 		Source src = createStrictMock(Source.class);
 		Compressor cpr = createStrictMock(Compressor.class);
 		NormalizedMessage nMsg = createStrictMock(NormalizedMessage.class);
 		MessageExchange mx = createResponseExchange(nMsg, CORRELATION_ID);
 
 		expect(cpr.isSourceEmpty(src)).andReturn(false);
 		expect(nMsg.getContent()).andReturn(src);
 		expect(cpr.asCompressedSource(src)).andReturn(src);
 		nMsg.setContent(src);
 			
 		replay(cpr, nMsg, src);
 		interceptor.setCompressor(cpr);
 		interceptor.process(mx);
 	}
 
 	
 	@Test
 	public void testProcessConsumerResponse() throws Exception {
 		Source src = createStrictMock(Source.class);
 		Compressor cpr = createStrictMock(Compressor.class);
 		NormalizedMessage nMsg = createStrictMock(NormalizedMessage.class);
 		MessageExchange mx = createResponseExchange(nMsg, null);
 
 		expect(cpr.isSourceEmpty(src)).andReturn(false);
 		expect(nMsg.getContent()).andReturn(src);
 		expect(cpr.asUncompressedSource(src)).andReturn(src);
 		nMsg.setContent(src);
 		
 		replay(cpr, nMsg, src);
 		interceptor.setCompressor(cpr);
 		interceptor.process(mx);
 	}
 	
 	
 	private MessageExchange createRequestExchange(NormalizedMessage msg, String correlationId) {
 		MessageExchange mx = createMock(MessageExchange.class);
 		expect(mx.getProperty("org.apache.servicemix.correlationId")).andReturn(correlationId);
 		expect(mx.getMessage("in")).andReturn(msg).times(2);
 		expect(mx.getMessage("out")).andReturn(null);
 		replay(mx);
 		return mx;
 	}
 	
 	
 	private MessageExchange createResponseExchange(NormalizedMessage msg, String correlationId) {
 		MessageExchange mx = createMock(MessageExchange.class);
 		expect(mx.getProperty("org.apache.servicemix.correlationId")).andReturn(correlationId);
 		expect(mx.getMessage("in")).andReturn(msg).times(2);
 		expect(mx.getMessage("out")).andReturn(msg).times(3);
 		replay(mx);
 		return mx;
 	}
 }
