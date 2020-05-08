 package org.checkstyle;
 
 import java.io.File;
 import java.util.Set;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.kalibro.KalibroTestCase;
 import org.kalibro.core.model.NativeMetric;
 
 public class CheckstyleTest extends KalibroTestCase {
 
 	private CheckstyleMetricCollector checkstyle;
 
 	@Before
 	public void setUp() {
 		checkstyle = new CheckstyleMetricCollector();
 	}
 
 	@Test(timeout = INTEGRATION_TIMEOUT)
 	public void shouldCollectMetrics() throws Exception {
 		File samplesDirectory = new File(SAMPLES_DIRECTORY, "checkstyle");
		File codeDirectory = new File(samplesDirectory, "Fibonacci");
 		Set<NativeMetric> metrics = checkstyle.getSupportedMetrics();
 		assertDeepEquals(CheckstyleStub.results(), checkstyle.collectMetrics(codeDirectory, metrics));
 	}
 }
