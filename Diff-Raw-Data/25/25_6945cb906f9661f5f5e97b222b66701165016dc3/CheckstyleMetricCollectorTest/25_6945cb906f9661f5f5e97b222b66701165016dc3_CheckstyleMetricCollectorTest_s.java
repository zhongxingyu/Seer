 package org.checkstyle;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.util.Set;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.kalibro.KalibroTestCase;
 import org.kalibro.core.model.NativeModuleResult;
 import org.mockito.Mockito;
 import org.powermock.api.mockito.PowerMockito;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 @RunWith(PowerMockRunner.class)
@PrepareForTest({CheckstyleMetricCollector.class})
 public class CheckstyleMetricCollectorTest extends KalibroTestCase {
 
 	private CheckstyleMetricCollector collector;
	
 	private KalibroChecker checker;
 	private CheckstyleOutputParser parser;
 
 	@Before
 	public void setUp() throws Exception {
 		mockParser();
 		mockChecker();
 		collector = new CheckstyleMetricCollector();
 	}
 
 	private void mockParser() throws Exception {
 		parser = PowerMockito.mock(CheckstyleOutputParser.class);
 		PowerMockito.whenNew(CheckstyleOutputParser.class).withNoArguments().thenReturn(parser);
 	}
 
 	private void mockChecker() throws Exception {
 		checker = PowerMockito.mock(KalibroChecker.class);
 		PowerMockito.whenNew(KalibroChecker.class).withArguments(parser).thenReturn(checker);
 	}
	
 	@Test(timeout = UNIT_TIMEOUT)
 	public void checkSupportedMetrics() {
 		assertDeepEquals(CheckstyleMetric.supportedMetrics(), collector.getSupportedMetrics());
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldCollectMetrics() throws Exception {
 		File codeDirectory = PowerMockito.mock(File.class);
 		Set<NativeModuleResult> results = CheckstyleStub.results();
 		PowerMockito.when(parser.getResults()).thenReturn(results);
		
 		assertSame(results, collector.collectMetrics(codeDirectory, CheckstyleMetric.supportedMetrics()));
 		Mockito.verify(checker).process(codeDirectory);
 		Mockito.verify(parser).getResults();
 	}
 }
