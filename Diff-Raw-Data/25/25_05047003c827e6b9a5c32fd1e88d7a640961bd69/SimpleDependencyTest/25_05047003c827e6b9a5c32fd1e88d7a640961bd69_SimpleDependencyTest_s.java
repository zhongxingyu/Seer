 package org.cloudifysource.quality.iTests.test.cli.cloudify.recipes;
 
 import java.io.IOException;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.openspaces.admin.pu.ProcessingUnitInstance;
 import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventListener;
 import org.testng.annotations.Test;
 
import org.cloudifysource.quality.iTests.test.cli.cloudify.AbstractLocalCloudTest;
import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils;
import org.cloudifysource.quality.iTests.framework.utils.AssertUtils.RepetitiveConditionProvider;

 /**
  * This test checks the application dependency mechanism.
  * It asserts "second" service starts after "first" service first instance.
 * 
  *  The first service takes 30 seconds to install and the second takes 10 seconds to install,
  *  so it makes it more likely to find a bug in dependency order.
 *   
  * @author itaif
  *
  */
 public class SimpleDependencyTest extends AbstractLocalCloudTest{
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void dependencyTest() throws IOException, InterruptedException{
 
 		String ApplicationName = "simpleDependency";
 		String path = CommandTestUtils.getPath("src/main/resources/apps/cloudify/recipes/" + ApplicationName);
 		final AtomicInteger firstStarted = new AtomicInteger();
 		final AtomicInteger secondStarted = new AtomicInteger();
 		final AtomicBoolean testFailed = new AtomicBoolean();
 		ProcessingUnitInstanceAddedEventListener eventListener = new ProcessingUnitInstanceAddedEventListener() {
 
 			@Override
 			public void processingUnitInstanceAdded(
 					ProcessingUnitInstance processingUnitInstance) {
 				String name = processingUnitInstance.getProcessingUnit().getName();
 				if (name.equals("simpleDependency.first")) {
 					firstStarted.incrementAndGet();
 				}
 				else if (name.equals("simpleDependency.second")) {
 					// This is where we check dependency order
 					if (firstStarted.get() == 0) {
 						testFailed.set(true);
 					}
 					secondStarted.incrementAndGet();
 				}
 			}
 		};
 		admin.getProcessingUnits().getProcessingUnitInstanceAdded().add(eventListener);
 		try {
 			// install application and wait for both services to be discovered
 			CommandTestUtils.runCommandExpectedFail("connect " + this.restUrl + ";install-application " + path + ";exit");
 			RepetitiveConditionProvider condition = new RepetitiveConditionProvider() {
				
 				@Override
 				public boolean getCondition() {
 					return firstStarted.get() > 0 && secondStarted.get() > 0;
 				}
 			};
 			repetitiveAssertTrue("Waiting for both first and second service to install", condition, OPERATION_TIMEOUT);
 		}
 		finally {
 			admin.getProcessingUnits().getProcessingUnitInstanceAdded().remove(eventListener);
 		}
		
 		assertTrue("second service cannot be started before first service since they are dependant", !testFailed.get());
 	}
 }
