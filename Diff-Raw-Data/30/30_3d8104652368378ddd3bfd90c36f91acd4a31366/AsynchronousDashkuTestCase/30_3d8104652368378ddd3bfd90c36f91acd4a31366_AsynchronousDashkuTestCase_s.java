 /**
  * Helios, OpenSource Monitoring
  * Brought to you by the Helios Development Group
  *
  * Copyright 2007, Helios Development Group and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org. 
  *
  */
 package test.org.helios.dashkuj;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicReference;
 
 import junit.framework.Assert;
 
 import org.helios.dashkuj.api.AsynchDashku;
 import org.helios.dashkuj.api.DashkuHandler;
 import org.helios.dashkuj.api.SynchDashku;
 import org.helios.dashkuj.core.apiimpl.AsynchDashkuImpl;
 import org.helios.dashkuj.domain.Dashboard;
 import org.helios.dashkuj.domain.DomainRepository;
 import org.helios.dashkuj.domain.ScreenWidth;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  * <p>Title: AsynchronousDashkuTestCase</p>
  * <p>Description: Test cases for {@link AsynchDashkuImpl}</p> 
  * <p>Company: Helios Development Group LLC</p>
  * @author Whitehead (nwhitehead AT heliosdev DOT org)
  * <p><code>test.org.helios.dashkuj.AsynchronousDashkuTestCase</code></p>
  */
 
 public class AsynchronousDashkuTestCase extends BaseTest {
 	/** The asynchronous dashkus keyed by apiKey */
 	protected static final Map<String, AsynchDashku> asds = new HashMap<String, AsynchDashku>();
 	/** The asynch waiter for this test */
 	protected final AtomicReference<AsynchWaiter> asynchWaiter = new AtomicReference<AsynchWaiter>(null);
 	/** A list of objects collected in the asynch callback */
 	final List<Object> testSet = new ArrayList<Object>();
 	/** The dashku repository */
 	protected static DomainRepository repo = null;
 
 	/** The default asynch test op timeout */
 	public static final long DEFAULT_TIMEOUT;
 	
 	static {
 		long tmp = -1;
 		try {
 			tmp = Long.parseLong(System.getProperty("org.helios.dashkuj.asynch.timeout"));
 			log("Using system configured asynch timeout:" + tmp + " ms.");
 		} catch (Exception ex) {
 			tmp = 3000;
 			log("Using default asynch timeout:" + tmp + " ms.");
 		}
 		DEFAULT_TIMEOUT = tmp;
 	}
 	
 	/**
 	 * Closes the asynch dashkus
 	 */
 	@AfterClass
 	public static void tearDownAfterClass()  {
 		for(AsynchDashku asd: asds.values()) {
 			asd.dispose();
 		}
 		asds.clear();
 	}
 	
 	
 	/**
 	 * Acquires the synchronous dashku and initializes a new asynch waiter
 	 */
 	@BeforeClass
 	public static void getDashku()  {
 		if(asds.isEmpty()) {
 			for(String apiKey: apiKey2userId.keySet()) {
 				AsynchDashku asd = DASH.getAsynchDashku(apiKey, defaultDashkuHost, defaultDashkuPort);
 				asds.put(asd.getApiKey(), asd);
 			}
 			repo = DomainRepository.getInstance(defaultDashkuHost, defaultDashkuPort);			
 		}
 	}
 	
 	/**
 	 * Flushes the repo and resets the asynchs
 	 */
 	@Before
 	public void getAsynchs()  {
 		repo.flush();
 		asynchWaiter.set(new AsynchWaiter());
 		testSet.clear();		
 	}
 	
 	
 	/**
 	 * Cleans up the latch
 	 */
 	@After
 	public void cleanup()  {
 		asynchWaiter.getAndSet(null).countDown();
 	}
 	
 	
 	
 	/**
 	 * Tests getDashboards
 	 * @throws Exception thrown on any error
 	 */
 	@Test
 	public void getDashboards() throws Exception {
 		for(Map.Entry<String, AsynchDashku> entry: asds.entrySet()) {			
 			AsynchDashku asd = entry.getValue();			
 			asd.getDashboards(new DashkuHandler<Collection<Dashboard>>(){
 				@Override
 				public void handle(Collection<Dashboard> event) {
 					testSet.addAll(event);
 					asynchWaiter.get().countDown();
 				}
 			});
 			asynchWaiter.get().waitForLatch();
 			log("Wait Complete");
 			
 			for(Object t: testSet) {
 				Dashboard d = (Dashboard)t;
 				Assert.assertNotNull("The API Dash was null", d);
 				log("Fetching DB Dash [" + d.getId() + "]");
 				Dashboard dbD = getDbDashboard(d.getId());			
 				Assert.assertNotNull("The DB Dash was null for id [" + d.getId() + "]" , dbD);
 				compareDashboards(d, dbD);
 			}		
 			Collection<Dashboard> repoDashboards = repo.getDashboards();
 			Assert.assertEquals("The number of dashboards is not the same from the API as in the repo", testSet.size(), repoDashboards.size());
 			for(Object t: testSet) {
 				Dashboard d = (Dashboard)t;
 				compareDashboards(d, repo.getDashboard(d.getId()));
 			}
 		}
 	}
 	
 	/**
 	 * Tests getDashboard driving by api key
 	 * @throws Exception thrown on any error
 	 */
 	@Test
 	public void getDashboardUsingApiKey() throws Exception {
 		for(Map.Entry<String, AsynchDashku> entry: asds.entrySet()) {			
 			AsynchDashku asd = entry.getValue();
 			String apiKey = asd.getApiKey();
 			final Dashboard d1 = getDbDashboardsByApiKey(apiKey).iterator().next();		
 			asd.getDashboard(d1.getId(), new DashkuHandler<Dashboard>(){
 				@Override
 				public void handle(Dashboard d2) {				
 					testSet.add(d2);
 					asynchWaiter.get().countDown();
 				}
 			});
 			asynchWaiter.get().waitForLatch();
 			compareDashboards(d1, (Dashboard)testSet.iterator().next());
 			
 		}		
 	}
 	
 	/**
 	 * Tests getDashboard driving by user name
 	 * @throws Exception thrown on any error
 	 */
 	@Test
 	public void getDashboardUsingUserName() throws Exception {
 		for(Map.Entry<String, AsynchDashku> entry: asds.entrySet()) {			
 			AsynchDashku asd = entry.getValue();
 			String apiKey = asd.getApiKey();
 			String userId = apiKey2userId.get(apiKey);
 			String userName = userId2userName.get(userId);
 			final Dashboard d1 = getDbDashboardsByUser(userName).iterator().next();		
 			asd.getDashboard(d1.getId(), new DashkuHandler<Dashboard>(){
 				@Override
 				public void handle(Dashboard d2) {				
 					testSet.add(d2);
 					asynchWaiter.get().countDown();
 				}
 			});
 			asynchWaiter.get().waitForLatch();
 			compareDashboards(d1, (Dashboard)testSet.iterator().next());			
 		}		
 	}
 	
 	/**
 	 * Creates a new dashboard, then verifies it can be retrieved as expected.
 	 * Incidentally tests deletion in that if the delete fails, the test will fail.
 	 * @throws Exception thrown on any errors
 	 */
 	@Test
 	public void createDashboard() throws Exception {
 		for(Map.Entry<String, AsynchDashku> entry: asds.entrySet()) {			
 			AsynchDashku asd = entry.getValue();
 			Dashboard newDash = new Dashboard();
 			final UUID uid = UUID.randomUUID();
 			final String css = "<!-- " + uid + " -->" ; 
 			newDash.setCss(css);
 			newDash.setName(uid.toString());
 			newDash.setScreenWidth(ScreenWidth.fluid);
 			asd.createDashboard(newDash, new DashkuHandler<Dashboard>(){
 				@Override
 				public void handle(Dashboard d2) {				
 					testSet.add(d2);
 					asynchWaiter.get().countDown();
 				}
 			});
 			asynchWaiter.get().waitForLatch();
 			Dashboard d1 = (Dashboard)testSet.iterator().next();
 			final String dashboardId = d1.getId();
 			try {
				// ====================================================
				// Logged defect with dashku. These don't work
				// FIXME: https://github.com/Anephenix/dashku/issues/16
				// ====================================================
				/*
 				Assert.assertEquals("CSS was not the expected [" + css + "]", css, d1.getCss());
				Assert.assertEquals("Screen width was not the expected [" + ScreenWidth.fluid + "]", ScreenWidth.fluid.name(), d1.getScreenWidth());				
				*/
				// ====================================================
				Assert.assertEquals("Dashboard name was not the expected [" + uid.toString() + "]", uid.toString(), d1.getName());
 				Dashboard d2 = getDbDashboard(d1.getId());
 				compareDashboards(d1, d2);
 			} finally {
 				asd.getSynchDashku().deleteDashboard(dashboardId);				
 			}
 		}
 		
 	}
 	
 	/**
 	 * Issues an update on the dashboard, adding neutral text to test for
 	 * @throws Exception thrown on any error 
 	 */
 	@Test
 	public void updateDashboard() throws Exception {
 		for(Map.Entry<String, AsynchDashku> entry: asds.entrySet()) {			
 			AsynchDashku asd = entry.getValue();
 			Dashboard newDash = new Dashboard();
 			final UUID uid = UUID.randomUUID();
 			final String css = "<!-- " + uid + " -->" ;
 			final String name = uid.toString();
 			final String updatedName = new StringBuilder(name).reverse().toString();
 			// unlikely but.....
 			Assert.assertNotSame("The reverse of a UID name was same as UID !!", name, updatedName);
 			newDash.setName(name);
 			asd.createDashboard(newDash, new DashkuHandler<Dashboard>(){
 				@Override
 				public void handle(Dashboard d2) {				
 					testSet.add(d2);
 					asynchWaiter.get().countDown();
 				}
 			});
 			asynchWaiter.get().waitForLatch();
 			Dashboard d1 = (Dashboard)testSet.iterator().next();
 			final String dashboardId = d1.getId();
 			try {				
 				Assert.assertNotSame("The css should not be the post-update value", d1.getCss(), css);
 				Assert.assertNotSame("The name should not be the post-update value", d1.getName(), name);
 				Assert.assertNotSame("The screenwidth should not be the post-update value", d1.getScreenWidth(), ScreenWidth.fluid.name());
 				Dashboard d2 = getDbDashboard(dashboardId);
 				compareDashboards(d1, d2);				
 				d1.setName(updatedName);
 				d1.setCss(css);
 				d1.setScreenWidth(ScreenWidth.fluid);
 				testSet.clear();
 				asynchWaiter.set(new AsynchWaiter());
 				asd.updateDashboard(d1, new DashkuHandler<Dashboard>(){
 					@Override
 					public void handle(Dashboard d2) {				
 						testSet.add(d2);
 						asynchWaiter.get().countDown();
 					}
 				});
 				asynchWaiter.get().waitForLatch();
 				Assert.assertEquals("The css was not the post-update value", css, d1.getCss());
 				Assert.assertEquals("The screen width was not the post-update value", ScreenWidth.fluid.name(), d1.getScreenWidth());
 				Assert.assertEquals("The name was not the post-update value", updatedName, d1.getName());
 				
 				d2 = getDbDashboard(dashboardId);
 				compareDashboards(d1, d2);				
 			} finally {
 				asd.getSynchDashku().deleteDashboard(dashboardId);
 			}
 		}
 		
 	}
 	
 	
 	
 	
 	/**
 	 * <p>Title: AsynchWaiter</p>
 	 * <p>Description: A wrapped latch for testing asynch calls</p> 
 	 * <p>Company: Helios Development Group LLC</p>
 	 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 	 * <p><code>test.org.helios.dashkuj.AsynchronousDashkuTestCase.AsynchWaiter</code></p>
 	 */
 	protected class AsynchWaiter {
 		final CountDownLatch latch = new CountDownLatch(1);
 		final long timeout;
 		
 		/**
 		 * Creates a new AsynchWaiter
 		 * @param timeout the specified timeout in ms
 		 */
 		public AsynchWaiter(long timeout) {
 			this.timeout = timeout;
 		}
 		/**
 		 * Creates a new AsynchWaiter using the default timeout
 		 */
 		public AsynchWaiter() {
 			this(DEFAULT_TIMEOUT);
 		}
 		
 		/**
 		 * Counts down the latch
 		 */
 		public void countDown() {
 			latch.countDown();
 		}
 		
 		/**
 		 * Waits the timeout period for the latch to be dropped
 		 * @throws Exception thrown on timeout or thread interrupt
 		 */
 		public void waitForLatch() throws Exception {
 			if(!latch.await(timeout, TimeUnit.MILLISECONDS)) {
 				throw new Exception("Request timed out", new Throwable());
 			}			
 		}
 	}
 	
 	
 }
