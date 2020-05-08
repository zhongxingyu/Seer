 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.runtime.tests.common.sif.async;
 
 import static org.hamcrest.Matchers.is;
 
 import java.util.List;
 
 import javax.xml.ws.Response;
 
 import org.ebayopensource.turmeric.runtime.sif.service.Service;
 import org.ebayopensource.turmeric.runtime.sif.service.ServiceFactory;
 import org.ebayopensource.turmeric.runtime.tests.common.jetty.AbstractWithServerTest;
 import org.ebayopensource.turmeric.runtime.tests.common.util.TestUtils;
 import org.ebayopensource.turmeric.runtime.tests.service1.sample.types1.MyMessage;
 import org.junit.Assert;
 import org.junit.Test;
 
 
 public class AsyncPollNonBlockingTest extends AbstractWithServerTest {
 	private final String ECHO_STRING = "BH Test String";
 
 	@Test
 	@SuppressWarnings("unchecked")
     // TODO: this testcase is supposed to be tested using Test1Driver.
 	public void testServicePollNonBlocking() throws Exception {
 		Service service = ServiceFactory.create("test1", "remote", serverUri.toURL());
 		service.createDispatch("echoString").invokeAsync(
 				ECHO_STRING + "service1");
 		List<Response<?>> responseList = service.poll(false, true);
 		while (responseList.size() < 1) {
 			responseList.addAll(service.poll(false, true));
 		}
 
 		debug(responseList);
 
 		Assert.assertThat("ReponseList.size", responseList.size(), is(1));
 	}
 	
 	@Test
 	@SuppressWarnings("unchecked")
 	public void servicePollNonBlocking_timeout() throws Exception {
 		Service service = ServiceFactory.create("test1", "remote", null);
 		service.createDispatch("echoString").invokeAsync(
 				ECHO_STRING + "service1");
 		List<Response<?>> responseList = service.poll(false, true, 0);
 
 		for (Response element : responseList) {
 			System.out.println("element.get()=" + element.get());
 		}
 
 		Assert.assertTrue(responseList.size() == 0);
 	}
 	
 	
 	@Test
 	@SuppressWarnings("unchecked")
 	public void servicePoll_NonBlocking_different_Operations_timeout()
 			throws Exception {
 		MyMessage msg = TestUtils.createTestMessage();
		Service service = ServiceFactory.create("test1", "remote", serverUri.toURL());
 		service.createDispatch("echoString").invokeAsync(ECHO_STRING);
 		msg.setBody(msg.getBody());
 		service.createDispatch("myTestOperation").invokeAsync(msg);
 
 		List<Response<?>> responseList = service.poll(false, true, 0);
 
 		for (Response element : responseList) {
 			System.out.println("element.get()=" + element.get());
 		}
 
 		Assert.assertTrue(responseList.size() < 2);
 	}
 
 	@Test
 	@SuppressWarnings("unchecked")
     // TODO: this testcase is supposed to be tested using Test1Driver.
 	public void testServicePollNonBlockingDifferentOperations()
 			throws Exception {
 		MyMessage msg = TestUtils.createTestMessage();
 		Service service = ServiceFactory.create("test1", "remote", serverUri.toURL());
 		service.createDispatch("echoString").invokeAsync(ECHO_STRING);
 		msg.setBody(msg.getBody());
 		service.createDispatch("myTestOperation").invokeAsync(msg);
 
 		List<Response<?>> responseList = service.poll(false, true);
 
 		while (responseList.size() < 2) {
 			responseList.addAll(service.poll(false, true));
 		}
 
 		debug(responseList);
 
 		Assert.assertThat("ReponseList.size", responseList.size(), is(2));
 	}
 	
 	@Test
 	@SuppressWarnings("unchecked")
 	public void servicePoll_local_NonBlocking_timeout() throws Exception {
 		Service service = ServiceFactory.create("Test1Service", "localAsync", null);
 		service.createDispatch("echoString").invokeAsync(ECHO_STRING);
 
 		List<Response<?>> responseList = service.poll(false, true, 0);
 
 		for (Response element : responseList) {
 			System.out.println("element.get()=" + element.get());
 		}
 
 		Assert.assertTrue(responseList.size() <= 1);
 	}
 	
 
 	@Test
 	@SuppressWarnings("unchecked")
 	public void testServicePollLocalNonBlocking() throws Exception {
 		Service service = ServiceFactory.create("Test1Service", "localAsync", serverUri.toURL());
 		service.createDispatch("echoString").invokeAsync(ECHO_STRING);
 
 		List<Response<?>> responseList = service.poll(false, true);
 
 		while (responseList.size() < 1) {
 			responseList.addAll(service.poll(false, true));
 		}
 
 		debug(responseList);
 
 		Assert.assertThat("ReponseList.size", responseList.size(), is(1));
 	}
 
 	@Test
 	@SuppressWarnings("unchecked")
 	public void testServicePollNonBlockingLocalDifferentOperations()
 			throws Exception {
 		MyMessage msg = TestUtils.createTestMessage();
 
 		Service service = ServiceFactory.create("Test1Service", "localAsync", serverUri.toURL());
 		service.createDispatch("echoString").invokeAsync(ECHO_STRING);
 		msg.setBody(msg.getBody());
 		service.createDispatch("myTestOperation").invokeAsync(msg);
 
 		List<Response<?>> responseList = service.poll(false, true);
 
 		while (responseList.size() < 2) {
 			responseList.addAll(service.poll(false, true));
 		}
 
 		debug(responseList);
 
 		Assert.assertThat("ReponseList.size", responseList.size(), is(2));
 	}
 	
 	@Test
 	@SuppressWarnings("unchecked")
 	public void servicePoll_NonBlocking_local_different_Operations_timeout()
 			throws Exception {
 		MyMessage msg = TestUtils.createTestMessage();
 
 		Service service = ServiceFactory.create("Test1Service", "localAsync", null);
 		service.createDispatch("echoString").invokeAsync(ECHO_STRING);
 		msg.setBody(msg.getBody());
 		service.createDispatch("myTestOperation").invokeAsync(msg);
 
 		List<Response<?>> responseList = service.poll(false, true, 0);
 
 		while (responseList.size() < 2) {
 			responseList.addAll(service.poll(false, true, 0));
 		}
 
 		Assert.assertTrue(responseList.size() <= 2);
 	}
 	
 	private void debug(List<Response<?>> responseList) throws Exception {
 		System.out.printf("ResponseList.size = %d%n", responseList.size());
 		for (Response<?> element : responseList) {
 			System.out.printf("  element.get() = %s%n", element.get());
 		}
 	}
 }
