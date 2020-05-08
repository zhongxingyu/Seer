 package org.kalibro.core.processing;
 
 import org.kalibro.*;
 
 /**
  * Uses a {@link Configuration} to add compound metrics and calculate the grade of a {@link ModuleResult}.
  * 
  * @author Carlos Morais
  */
 final class ModuleResultConfigurer {
 
 	static void configure(ModuleResult moduleResult, Configuration configuration) {
 		new ModuleResultConfigurer(moduleResult, configuration).addCompoundMetrics();
 		double gradeSum = 0.0;
 		double weightSum = 0.0;
 		for (MetricResult metricResult : moduleResult.getMetricResults())
 			if (metricResult.hasGrade()) {
 				Double weight = metricResult.getWeight();
 				gradeSum += metricResult.getGrade() * weight;
 				weightSum += weight;
 			}
 		moduleResult.setGrade(gradeSum / weightSum);
 	}
 
 	private ModuleResult moduleResult;
 	private Configuration configuration;
 	private JavascriptEvaluator scriptEvaluator;
 
 	private ModuleResultConfigurer(ModuleResult moduleResult, Configuration configuration) {
 		this.moduleResult = moduleResult;
 		this.configuration = configuration;
 		this.scriptEvaluator = new JavascriptEvaluator();
 	}
 
 	private void addCompoundMetrics() {
 		for (MetricConfiguration metricConfiguration : configuration.getMetricConfigurations())
 			includeScriptFor(metricConfiguration);
 		for (CompoundMetric compoundMetric : configuration.getCompoundMetrics())
 			if (isScopeCompatible(compoundMetric))
 				computeCompoundMetric(configuration.getConfigurationFor(compoundMetric));
 	}
 
 	private void includeScriptFor(MetricConfiguration metricConfiguration) {
 		String code = metricConfiguration.getCode();
 		Metric metric = metricConfiguration.getMetric();
		if (!isScopeCompatible(metric))
			return;
 		if (metric.isCompound())
 			scriptEvaluator.addFunction(code, ((CompoundMetric) metric).getScript());
		else if (moduleResult.hasResultFor(metric))
 			scriptEvaluator.addVariable(code, moduleResult.getResultFor(metric).getValue());
 	}
 
 	private boolean isScopeCompatible(Metric metric) {
 		return metric.getScope().ordinal() >= moduleResult.getModule().getGranularity().ordinal();
 	}
 
 	private void computeCompoundMetric(MetricConfiguration metricConfiguration) {
 		try {
 			doComputeCompoundMetric(metricConfiguration);
 		} catch (Exception exception) {
 			moduleResult.addMetricResult(new MetricResult(metricConfiguration, exception));
 		}
 	}
 
 	private void doComputeCompoundMetric(MetricConfiguration metricConfiguration) {
 		Double calculatedResult = scriptEvaluator.evaluate(metricConfiguration.getCode());
 		moduleResult.addMetricResult(new MetricResult(metricConfiguration, calculatedResult));
 	}
 }
