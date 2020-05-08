 package net.java.dev.cejug.classifieds.test.integration;
 
 import java.util.List;
 
 import net.java.dev.cejug.classifieds.test.integration.admin.AdminTestSuite;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.notification.Failure;
 import org.junit.runners.Suite;
 import org.junit.runners.model.InitializationError;
 
 public class ConcurrentTestSuite {
 	private DefaultJUnitNotifier notifier = new DefaultJUnitNotifier();
 	private int concurrencyFactor = 3;
 
 	public static void main(String[] args) {
 		new ConcurrentTestSuite().test();
 	}
 
 	@Test
 	public void test() {
 		try {
 			Suite.SuiteClasses annos = AdminTestSuite.class
 					.getAnnotation(Suite.SuiteClasses.class);
 			Class<?>[] testSuite = annos.value();
 			Thread[] processes = new Thread[testSuite.length
 					* concurrencyFactor];
 
 			ThreadGroup adminTestSuiteGroup = new ThreadGroup("AdminTestSuite");
 			for (int i = 0; i < testSuite.length; i++) {
 				for (int j = 0; j < concurrencyFactor; j++) {
 					processes[i * concurrencyFactor + j] = new Thread(
 							adminTestSuiteGroup, new JUnitRunnable(
 									testSuite[i], notifier));
 					notifier.incrementThreadsCounter();
 				}
 			}
 
 			for (Thread t : processes) {
 				t.start();
 			}
 
 			// TODO: include a decent timer here instead of this blatant
 			// counter.
 			int i = 0;
 			while (notifier.getThreadCounter() > 0) {
 				Thread.yield();
 			}
 
 			List<Failure> failures = notifier.getFailures();
 			if (failures.size() > 0) {
 				Assert.fail(failures.get(0).getTrace());
 			}
 		} catch (InitializationError e) {
 			e.printStackTrace();
 		}
 	}
 }
