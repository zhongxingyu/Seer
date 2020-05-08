 package test.cli.cloudify;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.cloudifysource.dsl.internal.CloudifyConstants;
 import org.cloudifysource.dsl.utils.ServiceUtils;
 import org.openspaces.admin.alert.Alert;
 import org.openspaces.admin.alert.AlertManager;
 import org.openspaces.admin.alert.AlertStatus;
 import org.openspaces.admin.alert.alerts.ElasticAutoScalingAlert;
 import org.openspaces.admin.alert.config.ElasticAutoScalingAlertConfiguration;
 import org.openspaces.admin.alert.events.AlertTriggeredEventListener;
 import org.openspaces.admin.internal.pu.InternalProcessingUnit;
 import org.openspaces.admin.pu.ProcessingUnitInstance;
 import org.openspaces.admin.pu.statistics.AverageInstancesStatisticsConfig;
 import org.openspaces.admin.pu.statistics.AverageTimeWindowStatisticsConfigurer;
 import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;
 import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsIdConfigurer;
import org.openspaces.admin.zone.config.AnyZonesConfig;
 import org.testng.Assert;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import framework.tools.SGTestHelper;
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 import framework.utils.GridServiceContainersCounter;
 import framework.utils.LogUtils;
 
 public class ScalingRulesRecipeTest extends AbstractLocalCloudTest {
 
 	private static final String SERVICE_NAME = "customServiceMonitor";
 	private static final String SERVICE_RELATIVE_PATH = "/apps/cloudify/recipes/" + SERVICE_NAME;
 	
 	private static final String COUNTER_METRIC = "counter";
 	private static final String ABSOLUTE_SERVICE_NAME = ServiceUtils.getAbsolutePUName(DEFAULT_APPLICATION_NAME,SERVICE_NAME);
 	private final List<Alert> adminAlerts = new ArrayList<Alert>();
 	
 	@BeforeMethod
 	public void before() throws IOException, InterruptedException {
 		
 		String serviceDir = SGTestHelper.getSGTestRootDir() + SERVICE_RELATIVE_PATH;
 		runCommand("connect " + restUrl + ";install-service --verbose " + serviceDir);
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT)
 	public void customServiceMonitorsAutoScalingTest() throws InterruptedException, IOException {
 		
 		final InternalProcessingUnit pu = (InternalProcessingUnit) admin.getProcessingUnits().waitFor(ABSOLUTE_SERVICE_NAME,OPERATION_TIMEOUT,TimeUnit.MILLISECONDS);
 		initAlerts();
 		final GridServiceContainersCounter gscCounter = new GridServiceContainersCounter(pu);
 		final AtomicInteger numberOfRaisedAlerts = new AtomicInteger(0);
         final AtomicInteger numberOfResolvedAlerts = new AtomicInteger(0);
 	    final CountDownLatch raisedLatch = new CountDownLatch(1);
 	    final CountDownLatch resolvedLatch = new CountDownLatch(1);
 	    
 	    AlertTriggeredEventListener listener = new AlertTriggeredEventListener() {
 			
 			@Override
 			public void alertTriggered(Alert alert) {
 				if (alert instanceof ElasticAutoScalingAlert) {
 					if (alert.getStatus().equals(AlertStatus.RAISED)) {
 						numberOfRaisedAlerts.incrementAndGet();
 						raisedLatch.countDown();
 					}
 					else if(alert.getStatus().equals(AlertStatus.RESOLVED)) {
 						numberOfResolvedAlerts.incrementAndGet();
 						resolvedLatch.countDown();
 					}
 					adminAlerts.add(alert);
 				}
 			}
 		};
 		
 		admin.getAlertManager().getAlertTriggered().add(listener, false);
 		try {
 			repetitiveAssertNumberOfInstances(pu, 2);
 			ProcessingUnitInstance instance = pu.getInstances()[0];
 			assertNotNull(instance);
 			final ProcessingUnitStatisticsId averageStatisticsId = 
 					new ProcessingUnitStatisticsIdConfigurer()
 					.monitor(CloudifyConstants.USM_MONITORS_SERVICE_ID)
 					.metric(COUNTER_METRIC)
 					.instancesStatistics(new AverageInstancesStatisticsConfig())
 					.timeWindowStatistics(new AverageTimeWindowStatisticsConfigurer().timeWindow(5, TimeUnit.SECONDS).create())
					.agentZones(new AnyZonesConfig())
 					.create();
 			pu.addStatisticsCalculation(averageStatisticsId);
 			pu.setStatisticsInterval(1, TimeUnit.SECONDS);
 			pu.startStatisticsMonitor();
 			try {
 				repetitiveAssertStatistics(pu, averageStatisticsId, 0);
 				Assert.assertEquals(numberOfRaisedAlerts.get(),0);
 				Assert.assertEquals(numberOfResolvedAlerts.get(),0);
 				
 				//automatic scale out test
 				//set metric value of 2 instances to 100
 				setStatistics(pu, 2, 100);
 
 				// the high threshold is 90, and the average value is set to 100.0 - which means auto scale out (from 2 instances to 3 instances)
 				// then the average value would be (100+100+0)/3 = 66 (two instance are 100 and the third one is still 0) which is within thresholds and should stop the scale out process
 				// the maximum #instances is 4, so we do not expect 4 GSCs added at any point.
 				repetitiveAssertNumberOfInstances(pu, 3);
 				repetitiveAssertStatistics(pu, averageStatisticsId, 100.0*2/3);
 				gscCounter.repetitiveAssertNumberOfGridServiceContainersAdded(3, OPERATION_TIMEOUT);
 				gscCounter.repetitiveAssertNumberOfGridServiceContainersRemoved(0, OPERATION_TIMEOUT);
 				Assert.assertEquals(numberOfRaisedAlerts.get(),0);
 				Assert.assertEquals(numberOfResolvedAlerts.get(),0);
 				
 				// set metric value of 3 instances to 1000
 				setStatistics(pu, 3, 1000);
 				
 				// the high threshold is 90, and the average value is set to 100.0 - which means auto scale out (from 3 instances to 4 instances)
 				// then the average value would be (1000+1000+1000+0)/4 = 660 (three instance are 1000 and the fourth one is still 0)
 				// the maximum #instances is 4, so we expect at most 4 GSCs added at any point.
 				repetitiveAssertNumberOfInstances(pu, 4);
 				repetitiveAssertStatistics(pu, averageStatisticsId, 1000.0*3/4);
 				gscCounter.repetitiveAssertNumberOfGridServiceContainersAdded(4, OPERATION_TIMEOUT);
 				gscCounter.repetitiveAssertNumberOfGridServiceContainersRemoved(0, OPERATION_TIMEOUT);
 				Assert.assertTrue(raisedLatch.await(OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
 				Assert.assertEquals(numberOfRaisedAlerts.get(),1);
 				Assert.assertEquals(numberOfResolvedAlerts.get(),0);
 				
 				// the low threshold is 30, and the average value is set to 0.0 - which means auto scale in (from 3 instances to 2 instances)
 				// the minimum #instances is 2, so we do not expect less than 2 GSCs added at any point.
 				setStatistics(pu, 4, 0);
 				repetitiveAssertStatistics(pu, averageStatisticsId, 0);
 				gscCounter.repetitiveAssertNumberOfGridServiceContainersAdded(4, OPERATION_TIMEOUT);
 				gscCounter.repetitiveAssertNumberOfGridServiceContainersRemoved(2, OPERATION_TIMEOUT);
 				Assert.assertTrue(resolvedLatch.await(OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
 				Assert.assertEquals(numberOfRaisedAlerts.get(),1);
 				Assert.assertEquals(numberOfResolvedAlerts.get(),1);
 			}
 			finally {
 				uninstallService(SERVICE_NAME);
 				pu.stopStatisticsMonitor();
 			}
 		}
 		finally {
 			if (gscCounter != null) {
 				gscCounter.close();
 			}
 			if (listener != null) {
 				admin.getAlertManager().getAlertTriggered().remove(listener);
 			}
 		}
 	}
 
 	private void initAlerts() {
 		final AlertManager alertManager = admin.getAlertManager();
 		alertManager.setConfig(new ElasticAutoScalingAlertConfiguration());
 		admin.getAlertManager().enableAlert(ElasticAutoScalingAlertConfiguration.class);
 	}
 
 	private void repetitiveAssertStatistics(final InternalProcessingUnit pu,
 			final ProcessingUnitStatisticsId statisticsId,
 			final Number expectedResult) {
 		repetitiveAssertTrue("Failed waiting for counter to be "+ expectedResult, new RepetitiveConditionProvider() {
 			
 			@Override
 			public boolean getCondition() {
 				
 				final Object counter = pu.getStatistics().getStatistics().get(statisticsId);
 				if (counter == null) {
 					LogUtils.log("Cannot get statistics " + statisticsId);
 					return false;
 				}
 				
 				if (!(counter instanceof Number)) {
 					LogUtils.log("Cannot get Number from statistics " + statisticsId);
 					return false;
 				}
 				
 				boolean equals = ((Number)counter).doubleValue() == expectedResult.doubleValue();
 				if (!equals) {
 					LogUtils.log("Waiting for value of average(counter), to be " + expectedResult + " current value is " + counter);
 				}
 				return equals;
 			
 				
 			}
 		}, OPERATION_TIMEOUT);
 	}
 	
 	public void setStatistics(final InternalProcessingUnit pu, final int expectedNumberOfInstances, long value) throws IOException, InterruptedException {
 		
 		ProcessingUnitInstance[] instances = repetitiveAssertNumberOfInstances(pu, expectedNumberOfInstances);
 		
 		for (ProcessingUnitInstance instance : instances) {
 			String command = "connect localhost;invoke -instanceid " + instance.getInstanceId() + " --verbose " + SERVICE_NAME + " set " + value;
 			String output = CommandTestUtils.runCommandAndWait(command);
 			LogUtils.log(output);
 		}
 	}
 }
