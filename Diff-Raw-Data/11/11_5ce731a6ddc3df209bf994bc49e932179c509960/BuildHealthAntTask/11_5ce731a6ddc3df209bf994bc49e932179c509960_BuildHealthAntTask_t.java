 package org.pescuma.buildhealth.ant;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.TaskContainer;
 import org.pescuma.buildhealth.analyser.coverage.CoverageAnalyser;
 import org.pescuma.buildhealth.analyser.diskusage.DiskUsageAnalyser;
 import org.pescuma.buildhealth.analyser.loc.LOCAnalyser;
 import org.pescuma.buildhealth.analyser.staticanalysis.StaticAnalysisAnalyser;
 import org.pescuma.buildhealth.analyser.unittest.UnitTestAnalyser;
 import org.pescuma.buildhealth.core.BuildHealth;
 import org.pescuma.buildhealth.core.BuildStatus;
 import org.pescuma.buildhealth.core.Report;
 import org.pescuma.buildhealth.core.ReportFormater;
 
 public class BuildHealthAntTask extends Task implements TaskContainer {
 	
 	private final List<Task> tasks = new ArrayList<Task>();
 	private File home;
 	private boolean failOnError = true;
 	
 	public File getHome() {
 		return home;
 	}
 	
 	public void setHome(File home) {
 		this.home = home;
 	}
 	
 	public boolean isFailOnError() {
 		return failOnError;
 	}
 	
 	public void setFailOnError(boolean failOnError) {
 		this.failOnError = failOnError;
 	}
 	
 	@Override
 	public void addTask(Task task) {
 		tasks.add(task);
 	}
 	
 	public static BuildHealth buildHealth;
 	
 	@Override
 	public void execute() throws BuildException {
 		buildHealth = new BuildHealth(BuildHealth.findHome(home, true));
 		
 		// TODO
 		buildHealth.addAnalyser(new UnitTestAnalyser());
 		buildHealth.addAnalyser(new CoverageAnalyser());
 		buildHealth.addAnalyser(new LOCAnalyser());
 		buildHealth.addAnalyser(new StaticAnalysisAnalyser());
		buildHealth.addAnalyser(new DiskUsageAnalyser());
 		
 		buildHealth.startNewBuild();
 		
 		for (Task task : tasks)
 			task.perform();
 		
 		Report report = buildHealth.generateReportSummary();
 		log(new ReportFormater().format(report).trim());
 		
 		buildHealth.shutdown();
 		buildHealth = null;
 		
 		if (failOnError && (report == null || report.getStatus() != BuildStatus.Good))
 			throw new BuildException(new ReportFormater().createSummaryLine(report), getLocation());
 	}
 	
 }
