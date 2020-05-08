 /*
  * Copyright 2013 Red Hat, Inc.
  * Author: Dennis Crissman
  *
  * Licensed under the GNU Lesser General Public License, version 3 or
  * any later version.
  *
  * In addition to the conditions of LGPLv3, you must preserve author
  * attributions in source code distributions and must accompany binary
  * distributions with such author attributions.
  */
 
 package cacher.aop;
 
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.replay;
 import static org.easymock.EasyMock.reset;
 import static org.easymock.EasyMock.verify;
 import static org.junit.Assert.assertEquals;
 
 import org.aopalliance.intercept.MethodInvocation;
 import org.easymock.EasyMock;
 import org.junit.Test;
 
 import cacher.fetcher.FetchManager;
 import cacher.impl.ExceptionThrowingCache;
 
 public class TestCacheInterceptor{
 
 	@Test
 	public void testNullFetcher() throws Throwable{
 		Object rtn = new Object();
 
 		MethodInvocation invocation = EasyMock.createMock(MethodInvocation.class);
 		reset(invocation);
 		expect(invocation.getArguments()).andReturn(new Object[0]).once();
 		expect(invocation.getMethod()).andReturn(getClass().getMethod("testNullFetcher")).once();
		expect(invocation.getArguments()).andReturn(new Object[0]).once();
 		expect(invocation.proceed()).andReturn(rtn).once();
 		replay(invocation);
 
 		Object obj = new CacheInterceptor().invoke(invocation);
 
 		assertEquals(rtn, obj);
 
 		verify(invocation);
 	}
 
 	@Test
 	public void testMissingAnnotation() throws Throwable{
 		Object rtn = new Object();
 
 		MethodInvocation invocation = EasyMock.createMock(MethodInvocation.class);
 		reset(invocation);
 		expect(invocation.getArguments()).andReturn(new Object[0]).once();
 		expect(invocation.getMethod()).andReturn(getClass().getMethod("testMissingAnnotation")).times(2);
		expect(invocation.getArguments()).andReturn(new Object[0]).once();
 		expect(invocation.proceed()).andReturn(rtn).once();
 		replay(invocation);
 
 		Object obj = new CacheInterceptor(new FetchManager(new ExceptionThrowingCache())).invoke(invocation);
 
 		assertEquals(rtn, obj);
 
 		verify(invocation);
 	}
 
 }
