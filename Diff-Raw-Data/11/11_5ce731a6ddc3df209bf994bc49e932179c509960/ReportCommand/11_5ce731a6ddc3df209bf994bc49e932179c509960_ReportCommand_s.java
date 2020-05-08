 package org.pescuma.buildhealth.cli.commands;
 
 import io.airlift.command.Command;
 
 import org.pescuma.buildhealth.analyser.coverage.CoverageAnalyser;
 import org.pescuma.buildhealth.analyser.diskusage.DiskUsageAnalyser;
 import org.pescuma.buildhealth.analyser.loc.LOCAnalyser;
 import org.pescuma.buildhealth.analyser.staticanalysis.StaticAnalysisAnalyser;
 import org.pescuma.buildhealth.analyser.unittest.UnitTestAnalyser;
 import org.pescuma.buildhealth.cli.BuildHealthCliCommand;
 import org.pescuma.buildhealth.core.Report;
 import org.pescuma.buildhealth.core.ReportFormater;
 
 @Command(name = "report", description = "Report the status of the current build")
 public class ReportCommand extends BuildHealthCliCommand {
 	
 	@Override
 	public void execute() {
 		// TODO
 		buildHealth.addAnalyser(new UnitTestAnalyser());
 		buildHealth.addAnalyser(new CoverageAnalyser());
 		buildHealth.addAnalyser(new LOCAnalyser());
		buildHealth.addAnalyser(new DiskUsageAnalyser());
 		buildHealth.addAnalyser(new StaticAnalysisAnalyser());
 		
 		Report report = buildHealth.generateReportSummary();
 		System.out.println(new ReportFormater().format(report));
 	}
 	
 }
