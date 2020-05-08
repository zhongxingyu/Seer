 package test.cli.cloudify;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import junit.framework.Assert;
 
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.cloudifysource.dsl.internal.CloudifyConstants;
 import org.cloudifysource.dsl.utils.ServiceUtils;
 import org.openspaces.admin.internal.pu.InternalProcessingUnit;
 import org.openspaces.admin.pu.statistics.AverageInstancesStatisticsConfig;
 import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;
 import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsIdConfigurer;
 import org.openspaces.admin.pu.statistics.ThroughputTimeWindowStatisticsConfigurer;
 import org.openspaces.admin.zone.config.AnyZonesConfig;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import framework.tools.SGTestHelper;
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 import framework.utils.LogUtils;
 
 public class ScalingRulesTomcatTotalRequestsTest extends AbstractLocalCloudTest {
 
 	private static final String COUNTER_METRIC = "Total Requests Count";
 	private static final String APPLICATION_NAME = "petclinic";
 	private static final String APPLICATION_FOLDER_NAME = "petclinic-simple";
 	private static final String SERVICE_NAME = "tomcat";
 	private static final String ABSOLUTE_SERVICE_NAME = ServiceUtils.getAbsolutePUName(APPLICATION_NAME,SERVICE_NAME);
 	private static final int NUMBER_OF_HTTP_GET_THREADS = 10;
 	private static final int THROUGHPUT_PER_THREAD = 1;
 	private static final int TOTAL_THROUGHPUT = NUMBER_OF_HTTP_GET_THREADS * THROUGHPUT_PER_THREAD;
 	private ScheduledExecutorService executor;
 	private String applicationUrl;
 	private AtomicInteger requestsMade = new AtomicInteger(0);
 
 	@BeforeMethod
 	public void before() throws IOException, InterruptedException {
 		String applicationDir = SGTestHelper.getSGTestRootDir() + "/recipes/apps/" + APPLICATION_FOLDER_NAME;
		runCommand("connect " + restUrl + ";install-application --verbose " + applicationDir);
 		this.executor= Executors.newScheduledThreadPool(NUMBER_OF_HTTP_GET_THREADS);
 	}
 
 	@AfterMethod(alwaysRun = true)
 	public void shutdownExecutor() {
 		if (this.executor != null) {
 			this.executor.shutdownNow();
 			this.executor = null;
 		}
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , enabled = true)
 	public void tomcatAutoScalingTest() throws Exception {
 
 		try {
 			final InternalProcessingUnit pu = (InternalProcessingUnit) admin.getProcessingUnits().waitFor(ABSOLUTE_SERVICE_NAME,OPERATION_TIMEOUT,TimeUnit.MILLISECONDS);
 			final ProcessingUnitStatisticsId statisticsId = 
 					new ProcessingUnitStatisticsIdConfigurer()
 			.monitor(CloudifyConstants.USM_MONITORS_SERVICE_ID)
 			.metric(COUNTER_METRIC)
 			.instancesStatistics(new AverageInstancesStatisticsConfig())
 			.timeWindowStatistics(new ThroughputTimeWindowStatisticsConfigurer().timeWindow(20, TimeUnit.SECONDS).create())
 			.agentZones(new AnyZonesConfig())
 			.create();
 
 			pu.addStatisticsCalculation(statisticsId);
 			pu.setStatisticsInterval(1, TimeUnit.SECONDS);
 			pu.startStatisticsMonitor();
 
 			repetitiveAssertNumberOfInstances(pu, 1);
 			for(int i = 0 ; i < NUMBER_OF_HTTP_GET_THREADS ; i++){
 				executor.scheduleWithFixedDelay(new HttpRequest(new URL(applicationUrl)), 0, THROUGHPUT_PER_THREAD, TimeUnit.SECONDS);
 			}
 			repetitiveAssertStatistics(pu, statisticsId, (double)TOTAL_THROUGHPUT);
 			repetitiveAssertNumberOfInstances(pu, 2);
 			executor.shutdownNow();
 			Assert.assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));
 
 			repetitiveAssertNumberOfInstances(pu, 1);
 		} finally {
 			uninstallApplication(APPLICATION_NAME);
 		}
 	}
 
 	public class HttpRequest implements Runnable{
 
 		private URL url;
 
 		public HttpRequest(URL url){
 			this.url = url;
 		}
 		@Override
 		public void run() {
 			HttpClient client = new DefaultHttpClient();
 			try {
 				HttpGet get = new HttpGet(url.toURI());				
 				client.execute(get);
 				requestsMade.incrementAndGet();
 			} catch (Throwable t) {
 				if (!(t instanceof InterruptedException)) {
 					LogUtils.log("an HttpRequest thread failed", t);
 				}
 				throw new RuntimeException(t); // this thread will never be scheduled again
 			}finally{
 				client.getConnectionManager().shutdown();
 			}
 
 		}
 
 	}
 
 	private void repetitiveAssertStatistics(final InternalProcessingUnit pu,
 			final ProcessingUnitStatisticsId statisticsId,
 			final Double expectedResult) {
 		repetitiveAssertTrue("Failed waiting for counter to be "+ expectedResult, new RepetitiveConditionProvider() {
 
 			@Override
 			public boolean getCondition() {
 
 				final Object counter = pu.getStatistics().getStatistics().get(statisticsId);
 				if (counter == null) {
 					LogUtils.log("Cannot get statistics " + statisticsId);
 				}
 				else if (!(counter instanceof Double)) {
 					LogUtils.log("Cannot get Double from statistics " + statisticsId);
 				} else {
 
 					if (((Double)counter) != expectedResult) {
 						LogUtils.log("Waiting for value of average(counter), to be " + expectedResult + " current value is " + counter);
 					}
 				}
 				return expectedResult.equals(counter);
 			}
 		}, OPERATION_TIMEOUT);
 	}
 }
