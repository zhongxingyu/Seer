 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.runtime.tests.common.sif.localbinding;
 
 import static org.junit.Assert.assertTrue;
 
 import javax.xml.ws.Dispatch;
 import javax.xml.ws.Response;
 
import org.junit.Ignore;
 import org.junit.Test;
 
 import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceInvocationException;
 import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceInvocationRuntimeException;
 import org.ebayopensource.turmeric.runtime.errorlibrary.ErrorDataCollection;
 import org.ebayopensource.turmeric.runtime.sif.impl.internal.pipeline.LocalBindingThreadPool;
 import org.ebayopensource.turmeric.runtime.sif.impl.internal.pipeline.LocalBindingThreadPool.ThreadPoolStats;
 import org.ebayopensource.turmeric.runtime.sif.service.Service;
 import org.ebayopensource.turmeric.runtime.tests.common.sif.Test1Driver;
 import org.ebayopensource.turmeric.runtime.tests.common.sif.Test1Driver.TestMode;
 import org.ebayopensource.turmeric.runtime.tests.service1.sample.types1.MyMessage;
 
 
 public class DetachedLocalBindingTest extends BaseLocalBindingTestCase {
 
 	public DetachedLocalBindingTest() throws Exception {
 		super();
 	}
 
 	@Test
 	public void testThreadPoolSize() throws Exception {
 		Test1Driver driver = createDriver();
 		driver.setDetachedLocalBinding(true);
 		driver.setRequestTimeout(1000);
 		driver.doCall();
 		ThreadPoolStats stats = LocalBindingThreadPool.getInstance().getStatistics();
 		assertTrue("pool size should be > 0", stats.getPoolSize() > 0);
 	}
 
 	@Test
 	public void callsWithOverrides() throws Exception {
 		Test1Driver driver = createDriver();
 		driver.setDetachedLocalBinding(false);
 		driver.doCall();
 		ThreadPoolStats stats = LocalBindingThreadPool.getInstance().getStatistics();
 		assertTrue("pool size should be > 0", stats.getPoolSize() > 0);
 		assertTrue("idle thread count should be > 0", stats.getIdleThreadCount() > 0);
 	}
 
 
 	@Test
	@Ignore // inconsistent testcases
 	public void callsWithTimeoutOverride() throws Exception {
 		System.setProperty("test.log.out", "true");
 		try{
 			Test1Driver driver = 
 				new Test1Driver(Test1Driver.TEST1_ADMIN_NAME, "detached", CONFIG_ROOT, LOCAL_TRANSPORT, "XML", "XML");
 	        driver.setDetachedLocalBinding(true);
 			driver.setNoPayloadData(true);
 			driver.skipAyncTest(true);
 			driver.setVerifier(new Verifier());
 			driver.setRequestTimeout(0);
 			driver.setExpectedError(
 					ErrorDataCollection.svc_transport_local_binding_timeout.getErrorId(),
 					ServiceInvocationException.class,
 					ServiceInvocationRuntimeException.class,
 					"Request timed out after " + 0
 							+ " ms in local transport for service - ");
 			driver.doCall();
 		}finally{
 			System.setProperty("test.log.out", ""); //remove is a multistep process for target jvm
 			// see some other tear downs. This is temporary to try and track whats going on with this 
 			// test failure on ci server
 		}
 	}
 
 	protected Test1Driver createDriver() throws Exception {
 		Test1Driver driver = new Test1Driver(Test1Driver.TEST1_ADMIN_NAME, "detached", CONFIG_ROOT, LOCAL_TRANSPORT);
 		driver.setDetachedLocalBinding(true);
 		driver.setVerifier(new Verifier());
 		return driver;
 	}
 
 	protected class Verifier implements Test1Driver.SuccessVerifier {
 		public void checkSuccess(Service service, String opName,
 				MyMessage request, MyMessage response, byte[] payloadData)
 				throws Exception {
 			/**
 			 * Uncomment the following to see the stats at the end of each test
 			 * System.out.println("After running the
 			 * InlineLocalBindingTest..."); showThreadPoolStats();
 			 */
 		}
 
 		@SuppressWarnings("rawtypes")
 		public void checkSuccess(Service service, Dispatch dispatch,
 				Response futureResponse, MyMessage request, MyMessage response,
 				byte[] payloadData, TestMode mode) throws Exception {
 			/**
 			 * Uncomment the following to see the stats at the end of each test
 			 * System.out.println("After running the
 			 * InlineLocalBindingTest..."); showThreadPoolStats();
 			 */
 		}
 	}
 	
 	@Test
 	public void testNormalCalls() throws Exception {
 		
 		// Assume passes, let the test continue.
 		println("Creating Driver");
 		Test1Driver driver = 
 				new Test1Driver(Test1Driver.TEST1_ADMIN_NAME, "detached", CONFIG_ROOT, LOCAL_TRANSPORT);
 		println("Calling Driver");
 		driver.doCall();
 	}
 }
