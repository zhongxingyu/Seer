 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.openmrs.module.shr.contenthandler.api;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 import org.junit.Test;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.shr.contenthandler.UnstructuredDataHandler;
 import org.openmrs.test.BaseModuleContextSensitiveTest;
 
 /**
  * Tests {@link ${ContentHandlerService}}.
  */
 public class  ContentHandlerServiceTest extends BaseModuleContextSensitiveTest {
 	
 	@Test
 	public void shouldSetupContext() {
 		assertNotNull(Context.getService(ContentHandlerService.class));
 	}
 	
 	private ContentHandlerService getService() {
 		ContentHandlerService chs = Context.getService(ContentHandlerService.class);
 		chs.deregisterContentHandler("text/plain");
 		return chs;
 	}
 
 	/**
 	 * @see ContentHandlerService#deregisterContentHandler(String)
 	 * @verifies Deregister the handler assigned for to specified contentType
 	 */
 	@Test
 	public void deregisterContentHandler_shouldDeregisterTheHandlerAssignedForToSpecifiedContentType()
 			throws Exception {
 		ContentHandlerService chs = getService();
 		
 		ContentHandler mockHandler = mock(ContentHandler.class);
 		when(mockHandler.cloneHandler()).thenReturn(mockHandler);
 		
 		chs.registerContentHandler("text/plain", mockHandler);
 		assertTrue(chs.getContentHandler("text/plain") == mockHandler);
 		
 		chs.deregisterContentHandler("text/plain");
 		assertTrue(chs.getContentHandler("text/plain") != mockHandler);
 	}
 
 	/**
 	 * @see ContentHandlerService#deregisterContentHandler(String)
 	 * @verifies Do nothing if there is no handler assigned for a specified contentType
 	 */
 	@Test
 	public void deregisterContentHandler_shouldDoNothingIfThereIsNoHandlerAssignedForASpecifiedContentType()
 			throws Exception {
 		ContentHandlerService chs = getService();
 		
 		chs.deregisterContentHandler("application/nothing-here");
 	}
 
 	/**
 	 * @see ContentHandlerService#getContentHandler(String)
 	 * @verifies Get an appropriate content handler for a specified content type
 	 */
 	@Test
 	public void getContentHandler_shouldGetAnAppropriateContentHandlerForASpecifiedContentType()
 			throws Exception {
 		ContentHandlerService chs = getService();
 		
 		ContentHandler mockHandler = mock(ContentHandler.class);
 		when(mockHandler.cloneHandler()).thenReturn(mockHandler);
 		
 		chs.registerContentHandler("text/plain", mockHandler);
 		assertTrue(chs.getContentHandler("text/plain") == mockHandler);
 	}
 
 	/**
 	 * @see ContentHandlerService#getContentHandler(String)
 	 * @verifies Return the default handler (UnstructuredDataHandler) for an unknown content type
 	 */
 	@Test
 	public void getContentHandler_shouldReturnTheDefaultHandlerUnstructuredDataHandlerForAnUnknownContentType()
 			throws Exception {
 		ContentHandlerService chs = getService();
 		
 		assertTrue(chs.getContentHandler("application/nothing-here") instanceof UnstructuredDataHandler);
 	}
 
 	/**
 	 * @see ContentHandlerService#getContentHandler(String)
 	 * @verifies Never return null
 	 */
 	@Test
 	public void getContentHandler_shouldNeverReturnNull() throws Exception {
 		ContentHandlerService chs = getService();
 		
 		assertNotNull(chs.getContentHandler("text/plain"));
 		
 		ContentHandler mockHandler = mock(ContentHandler.class);
 		chs.registerContentHandler("text/plain", mockHandler);
 		when(mockHandler.cloneHandler()).thenReturn(mockHandler);
 		assertNotNull(chs.getContentHandler("text/plain"));
 		
 		assertNotNull(chs.getContentHandler("application/xml"));
 		assertNotNull(chs.getContentHandler("application/nothing-here"));
 	}
 
 	/**
 	 * @see ContentHandlerService#registerContentHandler(String,ContentHandler)
 	 * @verifies Register the specified handler for the specified content type
 	 */
 	@Test
 	public void registerContentHandler_shouldRegisterTheSpecifiedHandlerForTheSpecifiedContentType()
 			throws Exception {
 		ContentHandlerService chs = getService();
 		
 		ContentHandler mockHandler = mock(ContentHandler.class);
 		when(mockHandler.cloneHandler()).thenReturn(mockHandler);
 		
 		assertTrue(chs.getContentHandler("text/plain") != mockHandler);
 		chs.registerContentHandler("text/plain", mockHandler);
 		assertTrue(chs.getContentHandler("text/plain") == mockHandler);
 	}
 
 	/**
 	 * @see ContentHandlerService#registerContentHandler(String,ContentHandler)
 	 * @verifies Throw an AlreadyRegisteredException if a handler is already registered for a specified content type
 	 */
 	@Test
 	public void registerContentHandler_shouldThrowAnAlreadyRegisteredExceptionIfAHandlerIsAlreadyRegisteredForASpecifiedContentType()
 			throws Exception {
 		ContentHandlerService chs = getService();
 		
 		ContentHandler mockHandler = mock(ContentHandler.class);
 		when(mockHandler.cloneHandler()).thenReturn(mockHandler);
 		ContentHandler mockHandler2 = mock(ContentHandler.class);
 		
 		chs.registerContentHandler("text/plain", mockHandler);
 		try {
 			chs.registerContentHandler("text/plain", mockHandler2);
			fail();
 		} catch (AlreadyRegisteredException ex) {
 			//expected
 		}
 	}
 
 	/**
 	 * @see ContentHandlerService#deregisterContentHandler(String)
 	 * @verifies Do nothing if there is an invalid content type specified
 	 */
 	@Test
 	public void deregisterContentHandler_shouldDoNothingIfThereIsAnInvalidContentTypeSpecified()
 			throws Exception {
 		ContentHandlerService chs = getService();
 		
 		chs.deregisterContentHandler(null);
 		chs.deregisterContentHandler("");
 		chs.deregisterContentHandler("this isn't valid");
 	}
 
 	/**
 	 * @see ContentHandlerService#registerContentHandler(String,ContentHandler)
 	 * @verifies Throw an InvalidContentTypeException if an invalid content type is specified
 	 */
 	@Test
 	public void registerContentHandler_shouldThrowAnInvalidContentTypeExceptionIfAnInvalidContentTypeIsSpecified()
 			throws Exception {
 		ContentHandlerService chs = getService();
 		
 		ContentHandler mockHandler = mock(ContentHandler.class);
 		try {
 			chs.registerContentHandler(null, mockHandler);
			fail();
 		} catch (InvalidContentTypeException ex) {
 			//expected
 		}
 		try {
 			chs.registerContentHandler("", mockHandler);
			fail();
 		} catch (InvalidContentTypeException ex) {
 			//expected
 		}
 		try {
 			chs.registerContentHandler("This isn't valid", mockHandler);
			fail();
 		} catch (InvalidContentTypeException ex) {
 			//expected
 		}
 	}
 
 	/**
 	 * @see ContentHandlerService#getContentHandler(String)
 	 * @verifies Return a clone of the requested handler using the handler's cloneHandler method
 	 */
 	@Test
 	public void getContentHandler_shouldReturnACloneOfTheRequestedHandlerUsingTheHandlersCloneHandlerMethod()
 			throws Exception {
 		ContentHandlerService chs = getService();
 		
 		ContentHandler mockHandler = mock(ContentHandler.class);
 		when(mockHandler.cloneHandler()).thenReturn(mockHandler);
 		
 		chs.registerContentHandler("text/plain", mockHandler);
 		chs.getContentHandler("text/plain");
 		verify(mockHandler).cloneHandler();
 	}
 
 	/**
 	 * @see ContentHandlerService#registerContentHandler(String,ContentHandler)
 	 * @verifies Throw a NullPointerException if prototype is null
 	 */
 	@Test
 	public void registerContentHandler_shouldThrowANullPointerExceptionIfPrototypeIsNull()
 			throws Exception {
 		ContentHandlerService chs = getService();
 		
 		try {
 			chs.registerContentHandler("text/plain", null);
			fail();
 		} catch (NullPointerException ex) {
 			//expected
 		}
 	}
 }
