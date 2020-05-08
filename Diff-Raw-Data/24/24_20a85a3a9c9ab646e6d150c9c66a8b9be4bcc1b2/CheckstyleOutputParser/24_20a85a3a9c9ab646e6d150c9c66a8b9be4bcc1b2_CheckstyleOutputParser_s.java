 package org.checkstyle;
 
 import com.puppycrawl.tools.checkstyle.api.AuditEvent;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import org.kalibro.core.model.NativeMetric;
 import org.kalibro.core.model.NativeModuleResult;
 
 public class CheckstyleOutputParser extends AuditAdapter {
 
 	private Set<NativeMetric> wantedMetrics;
 	private Map<String, PreModuleResult> resultsMap;
 
 	public CheckstyleOutputParser(Set<NativeMetric> wantedMetrics) {
 		this.wantedMetrics = wantedMetrics;
 		resultsMap = new HashMap<String, PreModuleResult>();
 	}
 
 	@Override
 	public void auditStarted(AuditEvent aEvt) {
 		resultsMap = new HashMap<String, PreModuleResult>();
 	}
 
 	@Override
 	public void addError(AuditEvent event) {
 		String messageKey = event.getLocalizedMessage().getKey();
 		String className = fileNameToClass(event.getFileName());
		Double value = Double.parseDouble(event.getMessage());
 		addMetricResult(className, messageKey, value);
 	}
 
 	public Set<NativeModuleResult> getResults() {
 		Set<NativeModuleResult> results = new HashSet<NativeModuleResult>();
 		for (PreModuleResult result : resultsMap.values())
 			results.add(result.getModuleResult());
 		return results;
 	}
 
 	private String fileNameToClass(String group) {
 		String[] parts = group.split("\\.|" + Pattern.quote(File.separator));
 		return parts[parts.length - 2];
 	}
 
 	private void addMetricResult(String className, String messageKey, Double value) {
 		getPreResult(className).addMetricResult(messageKey, value);
 	}
 
 	private PreModuleResult getPreResult(String className) {
 		if (!resultsMap.containsKey(className))
 			resultsMap.put(className, new PreModuleResult(className, wantedMetrics));
 		return resultsMap.get(className);
 	}
 }
