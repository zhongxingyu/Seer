 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.runtime.tests.common.sif.localbinding;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import org.ebayopensource.turmeric.runtime.common.utils.ThreadPoolConfig;
 import org.ebayopensource.turmeric.runtime.sif.impl.internal.pipeline.LocalBindingThreadPool;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import com.ebay.kernel.configuration.ConfigurationAttribute;
 import com.ebay.kernel.configuration.ConfigurationException;
 import com.ebay.kernel.configuration.ConfigurationManager;
 
 public class LocalBindingThreadPoolConfigTest extends BaseLocalBindingTestCase {
 	
 	public LocalBindingThreadPoolConfigTest() throws Exception{
 		super();
 	}
 
 	@Test(expected=ConfigurationException.class)
 	public void illegalKeepAliveValue() throws Exception {
 		//System.out.println("<><> Starting LocalBindingThreadPoolConfigTest.illegalKeepAliveValue <><>");
 		LocalBindingThreadPool tp = LocalBindingThreadPool.getInstance();
 		ThreadPoolConfig tpConf = tp.getConfiguration();
 		String configId = tpConf.getConfigCategoryId();
 		assertTrue("Configuration Category Id should be valid. It is:"+configId,configId != null && !configId.trim().isEmpty());
 		ConfigurationManager cm = ConfigurationManager.getInstance();
 		String propName  = ThreadPoolConfig.KEEP_ALIVE_TIME_IN_SEC.getName();
 		ConfigurationAttribute attribute = new ConfigurationAttribute(propName, Long.valueOf(0));
 		cm.setAttributeValue(tpConf.getConfigCategoryId(), attribute);
 	}
 	
 	@Test
 	public void changeKeepAliveValue() throws Exception {
 		LocalBindingThreadPool tp = LocalBindingThreadPool.getInstance();
 		ThreadPoolConfig tpConf = tp.getConfiguration();
 		assertTrue("Configuration Id should be valid. It is:"+tpConf.getConfigCategoryId(),tpConf.getConfigCategoryId() != null && !tpConf.getConfigCategoryId().trim().isEmpty());
 		long oldKeepAliveValue = tpConf.getKeepAliveTimeInSec(); 
 		
 		ConfigurationManager cm = ConfigurationManager.getInstance();
 		String propName  = ThreadPoolConfig.KEEP_ALIVE_TIME_IN_SEC.getName();
 
 		ConfigurationAttribute attribute = new ConfigurationAttribute(propName, oldKeepAliveValue+1);
 		cm.setAttributeValue(tpConf.getConfigCategoryId(), attribute);
 		long newKeepAliveValue = tpConf.getKeepAliveTimeInSec(); 
 
 		assertEquals(newKeepAliveValue, oldKeepAliveValue+1);
 	}
 }
