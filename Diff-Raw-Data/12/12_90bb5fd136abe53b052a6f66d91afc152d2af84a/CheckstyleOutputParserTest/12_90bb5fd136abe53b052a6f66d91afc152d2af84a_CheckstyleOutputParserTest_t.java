 package org.checkstyle;
 
 import static org.checkstyle.CheckstyleMetric.*;
 
 import com.puppycrawl.tools.checkstyle.api.AuditEvent;
 import com.puppycrawl.tools.checkstyle.api.LocalizedMessage;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.kalibro.KalibroTestCase;
 import org.kalibro.core.model.NativeMetric;
 import org.kalibro.core.model.NativeModuleResult;
 import org.powermock.api.mockito.PowerMockito;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest({AuditEvent.class, LocalizedMessage.class})
 public class CheckstyleOutputParserTest extends KalibroTestCase {
 
 	private CheckstyleOutputParser parser;
 	private HashSet<NativeMetric> wantedMetricsSet;
 
 	@Before
 	public void setUp() {
 		NativeMetric[] wantedMetrics = {
 			AVERAGE_ANONYMOUS_CLASSES_LENGTH.getNativeMetric(),
 			AVERAGE_METHOD_LENGTH.getNativeMetric(),
 			EXECUTABLE_STATEMENTS.getNativeMetric(),
 			FILE_LENGTH.getNativeMetric(),
 			NUMBER_OF_METHODS.getNativeMetric(),
 			NUMBER_OF_OUTER_TYPES.getNativeMetric(),
 		};
 
 		wantedMetricsSet = new HashSet<NativeMetric>(Arrays.asList(wantedMetrics));
 		parser = new CheckstyleOutputParser(wantedMetricsSet);
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldParseMetricResults() {
 		parser.auditStarted(null);
 		simulateCheckstyle();

 		Set<NativeModuleResult> expected = filterStubResultWithWantedMetrics();
 		assertDeepEquals(expected, parser.getResults());
 	}
 
 	private Set<NativeModuleResult> filterStubResultWithWantedMetrics() {
 		NativeModuleResult stubResult = CheckstyleStub.result();
 		NativeModuleResult expectedModuleResult = new NativeModuleResult(stubResult.getModule());

		for (NativeMetric wantedMetric : wantedMetricsSet)
 			if (stubResult.hasResultFor(wantedMetric))
 				expectedModuleResult.addMetricResult(stubResult.getResultFor(wantedMetric));

 		return new HashSet<NativeModuleResult>(Arrays.asList(expectedModuleResult));
 	}
 
 	public void simulateCheckstyle() {
 		parser.addError(mockEvent("maxLen.anonInner", "6"));
 		parser.addError(mockEvent("maxLen.method", "" + 56.0 / 7.0));
 		parser.addError(mockEvent("executableStatementCount", "19"));
 		parser.addError(mockEvent("maxLen.file", "58"));
 		parser.addError(mockEvent("too.many.methods", "6"));
 		parser.addError(mockEvent("maxOuterTypes", "2"));
 		parser.auditFinished(null);
 	}
 
 	private AuditEvent mockEvent(String messageKey, String message) {
 		LocalizedMessage localizedMessage = PowerMockito.mock(LocalizedMessage.class);
 		PowerMockito.when(localizedMessage.getKey()).thenReturn(messageKey);
 
 		AuditEvent event = PowerMockito.mock(AuditEvent.class);
 		PowerMockito.when(event.getLocalizedMessage()).thenReturn(localizedMessage);
 		PowerMockito.when(event.getFileName()).thenReturn("Fibonacci.java");
 		PowerMockito.when(event.getMessage()).thenReturn(message);
 		return event;
 	}
 }
