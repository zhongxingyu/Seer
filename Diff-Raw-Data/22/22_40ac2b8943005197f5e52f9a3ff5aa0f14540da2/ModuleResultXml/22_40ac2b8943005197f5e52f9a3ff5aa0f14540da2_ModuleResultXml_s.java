 package org.kalibro.service.entities;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.kalibro.core.model.CompoundMetric;
 import org.kalibro.core.model.MetricResult;
 import org.kalibro.core.model.ModuleResult;
 import org.kalibro.core.util.DataTransferObject;
 
 @XmlRootElement(name = "ModuleResult")
 @XmlAccessorType(XmlAccessType.FIELD)
 public class ModuleResultXml implements DataTransferObject<ModuleResult> {
 
 	private ModuleXml module;
 	private Date date;
 	private Double grade;
 
 	@XmlElement(name = "metricResult")
 	private Collection<MetricResultXml> metricResults;
 
 	@XmlElement(name = "compoundMetricWithError")
 	private Collection<CompoundMetricWithErrorXml> compoundMetricsWithError;
 
 	public ModuleResultXml() {
 		super();
 	}
 
 	public ModuleResultXml(ModuleResult moduleResult) {
 		module = new ModuleXml(moduleResult.getModule());
 		date = moduleResult.getDate();
 		grade = moduleResult.getGrade();
 		initializeMetricResults(moduleResult);
 		initializeCompoundMetricsWithError(moduleResult);
 	}
 
 	private void initializeMetricResults(ModuleResult moduleResult) {
 		metricResults = new ArrayList<MetricResultXml>(moduleResult.getMetricResults().size());
 		for (MetricResult metricResult : moduleResult.getMetricResults())
 			metricResults.add(new MetricResultXml(metricResult));
 	}
 
 	private void initializeCompoundMetricsWithError(ModuleResult moduleResult) {
 		compoundMetricsWithError = new ArrayList<CompoundMetricWithErrorXml>();
 		for (CompoundMetric metric : moduleResult.getCompoundMetricsWithError())
 			compoundMetricsWithError.add(new CompoundMetricWithErrorXml(metric, moduleResult.getErrorFor(metric)));
 	}
 
 	@Override
 	public ModuleResult convert() {
 		ModuleResult moduleResult = new ModuleResult(module.convert(), date);
 		moduleResult.setGrade(grade);
 		convertMetricResults(moduleResult);
 		convertCompoundMetricsWithError(moduleResult);
 		return moduleResult;
 	}
 
 	private void convertMetricResults(ModuleResult moduleResult) {
 		for (MetricResultXml metricResult : metricResults)
 			moduleResult.addMetricResult(metricResult.convert());
 	}
 
 	private void convertCompoundMetricsWithError(ModuleResult moduleResult) {
		for (CompoundMetricWithErrorXml metricWithError : compoundMetricsWithError)
			moduleResult.addCompoundMetricWithError(metricWithError.getMetric(), metricWithError.getError());
 	}
 }
