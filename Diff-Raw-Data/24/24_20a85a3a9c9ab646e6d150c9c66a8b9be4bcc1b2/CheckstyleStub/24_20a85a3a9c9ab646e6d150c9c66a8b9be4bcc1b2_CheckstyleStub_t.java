 package org.checkstyle;
 
 import static org.checkstyle.CheckstyleMetric.*;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.kalibro.core.MetricCollector;
 import org.kalibro.core.model.Module;
 import org.kalibro.core.model.NativeMetric;
 import org.kalibro.core.model.NativeMetricResult;
 import org.kalibro.core.model.NativeModuleResult;
 import org.kalibro.core.model.enums.Granularity;
 import org.kalibro.core.model.enums.Language;
 
 public class CheckstyleStub implements MetricCollector {
 
 	private static Set<NativeMetric> nativeMetrics;
 	private static NativeModuleResult result = initializeResult();
 
 	private static NativeModuleResult initializeResult() {
 		nativeMetrics = new HashSet<NativeMetric>();
 
 		Module module = new Module(Granularity.CLASS, "HelloWorld");
 		result = new NativeModuleResult(module);
		addMetricResult(AVERAGE_ANONYMOUS_CLASSES_LENGTH, 6.0);
 		addMetricResult(AVERAGE_FOR_DEPTH, 0.0);
 		addMetricResult(AVERAGE_IF_DEPTH, 0.0);
 		addMetricResult(AVERAGE_TRY_DEPTH, 0.0);
		addMetricResult(AVERAGE_METHOD_LENGTH, 54.0 / 7.0);
		addMetricResult(AVERAGE_RETURN_COUNT, 5.0 / 7.0);
		addMetricResult(AVERAGE_THROWS_COUNT, 4.0 / 7.0);
 		addMetricResult(EXECUTABLE_STATEMENTS, 1.0);
 		addMetricResult(FILE_LENGTH, 6.0);
 		addMetricResult(MAGIC_NUMBER_COUNT, 0.0);
 		addMetricResult(NUMBER_OF_EMPTY_STATEMENTS, 0.0);
 		addMetricResult(NUMBER_OF_INLINE_CONDITIONALS, 0.0);
 		addMetricResult(NUMBER_OF_METHODS, 1.0);
 		addMetricResult(NUMBER_OF_OUTER_TYPES, 1.0);
 		addMetricResult(NUMBER_OF_TODO_COMMENTS, 0.0);
 		addMetricResult(NUMBER_OF_TRAILING_COMMENTS, 0.0);
 		addMetricResult(SIMPLIFIABLE_BOOLEAN_RETURNS, 0.0);
 		addMetricResult(SIMPLIFIABLE_BOOLEAN_EXPRESSIONS, 0.0);
 		return result;
 	}
 
 	private static void addMetricResult(CheckstyleMetric metric, Double value) {
 		NativeMetric nativeMetric = new NativeMetric(metric.toString(), Granularity.CLASS, Language.JAVA);
 		nativeMetrics.add(nativeMetric);
 		result.addMetricResult(new NativeMetricResult(nativeMetric, value));
 	}
 
 	public static Set<NativeMetric> nativeMetrics() {
 		return nativeMetrics;
 	}
 
 	public static NativeModuleResult result() {
 		return result;
 	}
 
 	public static Set<NativeModuleResult> results() {
 		return new HashSet<NativeModuleResult>(Arrays.asList(result));
 	}
 
 	@Override
 	public Set<NativeMetric> getSupportedMetrics() {
 		return nativeMetrics();
 	}
 
 	@Override
 	public Set<NativeModuleResult> collectMetrics(File codeDirectory, Set<NativeMetric> metrics) {
 		return results();
 	}
 }
