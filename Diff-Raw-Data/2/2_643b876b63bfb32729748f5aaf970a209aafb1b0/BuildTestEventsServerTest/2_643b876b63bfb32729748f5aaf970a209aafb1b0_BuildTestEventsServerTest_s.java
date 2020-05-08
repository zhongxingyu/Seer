 package org.jenkinsci.testinprogress.server;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import org.jenkinsci.testinprogress.runner.ProgressSuite;
 import org.jenkinsci.testinprogress.server.events.build.BuildTestEvent;
 import org.jenkinsci.testinprogress.server.events.build.IBuildTestEventListener;
 import org.jenkinsci.testinprogress.server.events.run.IRunTestEvent;
 import org.jenkinsci.testinprogress.server.events.run.RunEndEvent;
 import org.jenkinsci.testinprogress.server.events.run.RunStartEvent;
 import org.jenkinsci.testinprogress.server.filters.StackTraceFilter;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.JUnitCore;
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite.SuiteClasses;
 
 import tests.CalcTest;
 import tests.CalcTest2;
 import tests.IgnoredTest;
 
 import com.google.common.collect.Lists;
 
 public class BuildTestEventsServerTest {
 	private BuildTestEventsServer server;
 	private BuildTestEventListener listener;
 
 	@Before
 	public void setUp() throws IOException {
 		int port = findFreePort();
 		listener = new BuildTestEventListener();
 		server = new BuildTestEventsServer(port, new StackTraceFilter(),
 				new IBuildTestEventListener[] { listener });
 		server.start();
 		System.setProperty("TEST_IN_PROGRESS_PORT", Integer.toString(port));
 	}
 
 	@After
 	public void tearDown() throws IOException {
 		server.stop();
 		System.setProperty("TEST_IN_PROGRESS_PORT", "");
 	}
 
 	@Test
 	public void testBuildTestEventsServer() throws Exception {
 		// Given
 		ProgressSuite1.class.getAnnotations();
 		
 		// When
 		runProgressSuites(ProgressSuite1.class);
 		runProgressSuites(ProgressSuite2.class);
 
 		// Then
 		waitForBuildTestEvent(ProgressSuite1.class.getName(), RunStartEvent.class);
 		waitForBuildTestEvent(ProgressSuite1.class.getName(), RunEndEvent.class);
 		waitForBuildTestEvent(ProgressSuite2.class.getName(), RunStartEvent.class);
 		waitForBuildTestEvent(ProgressSuite2.class.getName(), RunEndEvent.class);
 	}
 
 	private void runProgressSuites(Class<?>... classes) throws IOException {
 		JUnitCore jUnitCore = new JUnitCore();
 		jUnitCore.run(classes);
 	}
 
 	private int findFreePort() {
 		ServerSocket socket = null;
 		try {
 			socket = new ServerSocket(0);
 			return socket.getLocalPort();
 		} catch (IOException e) {
 		} finally {
 			if (socket != null) {
 				try {
 					socket.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 		return -1;
 	}
 
 	@SuppressWarnings("unchecked")
 	private <B extends IRunTestEvent> B waitForBuildTestEvent(String runId, Class<B> type) throws TimeoutException {
 		long t1 = System.nanoTime();
		while (TimeUnit.MILLISECONDS.convert(System.nanoTime() - t1 , TimeUnit.NANOSECONDS) < 100) {
 			for (BuildTestEvent event : listener.getBuildTestEvents()) {
 				if (runId.equals(event.getRunId()) && type.isInstance(event.getRunTestEvent())) {
 					return (B)event.getRunTestEvent();
 				}
 			}
 		}
 		throw new TimeoutException();
 	}
 	
 	private static class BuildTestEventListener implements
 			IBuildTestEventListener {
 		private final List<BuildTestEvent> buildTestEvents = Lists
 				.newCopyOnWriteArrayList();
 
 		public void event(BuildTestEvent buildTestEvent) {
 			buildTestEvents.add(buildTestEvent);
 		}
 
 		public List<BuildTestEvent> getBuildTestEvents() {
 			return buildTestEvents;
 		}
 
 	}
 	
 	@RunWith(ProgressSuite.class)
 	@SuiteClasses({ CalcTest.class,  CalcTest2.class })
 	private static class ProgressSuite1 {
 
 	}
 
 	@RunWith(ProgressSuite.class)
 	@SuiteClasses({ IgnoredTest.class })
 	private static class ProgressSuite2 {
 
 	}
 
 }
