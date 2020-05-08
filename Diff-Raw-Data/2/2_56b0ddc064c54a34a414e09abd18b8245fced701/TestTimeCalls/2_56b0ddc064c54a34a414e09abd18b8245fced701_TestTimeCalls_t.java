 package com.insightfullogic.os.interceptors;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.fail;
 import static org.junit.Assert.assertThat;
 import junit.framework.Assert;
 
 import org.apache.log4j.Level;
 import org.hamcrest.Matchers;
 import org.junit.Test;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.insightfullogic.os.interceptors.TimeCallsModule;
 import com.insightfullogic.os.interceptors.TimingResults;
 
 public class TestTimeCalls {
 
 	private final Injector injector = Guice.createInjector(new TimeCallsModule(true));
 	private final InterceptedMock mock = injector.getInstance(InterceptedMock.class);
 	private final TimingResults results = injector.getInstance(TimingResults.class);
 
 	@Test
 	public void methodCallsAreTimedAndCounted() {
 		final String s = "someString";
 
 		mock.setCheck(s);
 		mock.interceptedMethod(s);
 		mock.interceptedMethod(s);
 
 		assertThat(results.totalTime(InterceptedMock.class, "interceptedMethod", String.class), Matchers.greaterThan(200l));
 		assertEquals(results.numberOfCalls(InterceptedMock.class, "interceptedMethod", String.class), 2);
 	}
 
 	@Test
 	public void stillTimedWhenThrowingExceptions() {
 		try {
 			mock.interceptedException();
 			fail();
 		} catch (final RuntimeException e) {
 			Assert.assertEquals("fail", e.getMessage());
 			assertEquals(results.numberOfCalls(InterceptedMock.class, "interceptedException"), 1);
 		}
 	}
 
 	@Test
 	public void logsTimes() {
 		results.reset();
 		try {
 			mock.interceptedException();
 		} catch (final Exception e) {
 
 		}
 		final MockLogger ml = new MockLogger(
				"public void com.insightfullogic.os.interceptors.InterceptedMock.interceptedException() was called 1 times for a total time usage of ");
 		results.logResults(ml, Level.INFO);
 		ml.checkFinished();
 	}
 
 }
