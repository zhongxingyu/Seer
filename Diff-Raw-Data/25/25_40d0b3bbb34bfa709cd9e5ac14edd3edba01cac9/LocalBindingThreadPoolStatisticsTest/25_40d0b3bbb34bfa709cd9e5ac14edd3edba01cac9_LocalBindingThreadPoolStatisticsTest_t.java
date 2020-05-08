 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.runtime.tests.common.sif.localbinding;
 
 import static org.junit.Assert.*;
 
 import java.util.concurrent.Callable;
 import java.util.concurrent.Future;
 
 import org.ebayopensource.turmeric.runtime.common.pipeline.MessageContext;
 import org.ebayopensource.turmeric.runtime.common.utils.ThreadPoolConfig;
 import org.ebayopensource.turmeric.runtime.sif.impl.internal.pipeline.LocalBindingThreadPool;
 import org.ebayopensource.turmeric.runtime.sif.impl.internal.pipeline.LocalBindingThreadPool.ThreadPoolStats;
 import org.junit.Test;
 
 
 public class LocalBindingThreadPoolStatisticsTest extends BaseLocalBindingTestCase implements Callable<MessageContext> {
 
 	private int m_oldActiveCount;
 	private int m_oldPoolSize;
 	private int m_oldIdleThreadCount;
 
 	// Implementation of Callable.call() method
 	public MessageContext call(){
		
 		ThreadPoolStats tpStats = LocalBindingThreadPool.getInstance().getStatistics();
 		
 		int newActiveCount = tpStats.getActiveCount();
 		int newPoolSize = tpStats.getPoolSize();
 		m_oldIdleThreadCount = tpStats.getIdleThreadCount();
 		
 		assertEquals(newActiveCount, m_oldActiveCount+1);
		assertTrue(newPoolSize >= m_oldPoolSize);
		
 		return null;
 	}
 	
 	public LocalBindingThreadPoolStatisticsTest() throws Exception{
 		super();
 	}
 	
 	@Test
 	public void localBindingThreadPoolStatistics() throws Exception {
 		ThreadPoolStats tpStats = LocalBindingThreadPool.getInstance().getStatistics();
 		m_oldActiveCount = tpStats.getActiveCount();
 		m_oldPoolSize = tpStats.getPoolSize();
		
 		Future<MessageContext> future = LocalBindingThreadPool.getInstance().execute(this);
 		future.get();
 		
 		int newIdleThreadCount = LocalBindingThreadPool.getInstance().getStatistics().getIdleThreadCount();
 		assertEquals(newIdleThreadCount, m_oldIdleThreadCount+1);
 	}
 
 	@Test
 	public void localBindingThreadPoolName()  throws Exception {
 		ThreadPoolStats tpStats = LocalBindingThreadPool.getInstance().getStatistics();
 		String tpName = tpStats.getThreadPoolname();
 		assertTrue(tpName.length()>0);
 	}
 	
 	@Test
 	public void localBindingThreadPoolKeepAlive()  throws Exception {

 		ThreadPoolConfig tpConf = LocalBindingThreadPool.getInstance().getConfiguration();
 		ThreadPoolStats tpStats = LocalBindingThreadPool.getInstance().getStatistics();

 		Long keepAliveFromConf = tpConf.getKeepAliveTimeInSec();
 		Long keepAliveFromStats = tpStats.getKeepAliveTimeInSecs();

 		assertEquals(keepAliveFromConf, keepAliveFromStats);
 	}
 }
