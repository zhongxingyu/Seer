 package org.calculator.controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 
import org.apache.log4j.Logger;
 import org.calculator.business.IScenarioManager;
 import org.calculator.business.reporting.IReportingManager;
 import org.calculator.models.Scenario;
 import org.calculator.reporting.models.Report;
 import org.calculator.reporting.models.ReportResult;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  * Reporting controller
  * 
  * @author khalil.amdouni
  *
  */
 @Controller
 public class ReportingController {
 	
 	private IReportingManager reportingManager;
 	private IScenarioManager scenarioManager;
 	
	private final static Logger logger = Logger.getLogger(ReportingController.class);
	
 	@RequestMapping(value = "/saveReport/{scenarioId}/{resultsAsString}", method = RequestMethod.POST)
 	public String saveReport(@PathVariable("scenarioId") long scenarioId,
 			@PathVariable("resultsAsString") String resultsAsString) {
 		String[] results = resultsAsString.split("-");
 		List<ReportResult> reportResults = new ArrayList<ReportResult>();
 		Scenario scenario = scenarioManager.get(scenarioId);
 		String reportName = "Report for scenario " + scenario.getName();
 		Report report = new Report();
 		report.setTitle(reportName);
 		report.setScenario(scenario);
		logger.info("================ >>" + resultsAsString);
 		for (int i = 0; i < results.length; i++) {
 			ReportResult reportResult = new ReportResult();
 			reportResult.setReport(report);
			reportResult.setX(Double.valueOf(results[i].split("_")[0]));
			reportResult.setY(Double.valueOf(results[i].split("_")[1]));
 			reportResults.add(reportResult);
 		}
 		report.setResults(reportResults);
 		reportingManager.save(report);
 		return "OK";
 	}
 
 	public IReportingManager getReportingManager() {
 		return reportingManager;
 	}
 
 	@Autowired
 	@Qualifier(value = "reportingManager")
 	public void setReportingManager(IReportingManager reportingManager) {
 		this.reportingManager = reportingManager;
 	}
 
 	public IScenarioManager getScenarioManager() {
 		return scenarioManager;
 	}
 
 	@Autowired
 	@Qualifier(value = "scenarioManager")
 	public void setScenarioManager(IScenarioManager scenarioManager) {
 		this.scenarioManager = scenarioManager;
 	}
 	
 }
