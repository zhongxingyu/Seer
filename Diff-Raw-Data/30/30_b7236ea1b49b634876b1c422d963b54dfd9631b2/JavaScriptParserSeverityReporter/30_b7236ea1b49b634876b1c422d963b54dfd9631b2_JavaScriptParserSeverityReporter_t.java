 package org.eclipse.dltk.javascript.parser;
 
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.dltk.compiler.problem.ProblemSeverities;
 import org.eclipse.dltk.javascript.parser.Reporter.Severity;
 
 public class JavaScriptParserSeverityReporter implements ISeverityReporter {
 
	public Severity getSeverity(int problemId, Severity defaultSeverity) {
		if (defaultSeverity == null)
			defaultSeverity = Severity.WARNING;
		int defaultProblemSeverity = defaultSeverity == Severity.WARNING ? ProblemSeverities.Warning
				: ProblemSeverities.Error;

 		int severity = new InstanceScope().getNode(
 				JavaScriptParserPlugin.PLUGIN_ID).getInt(
 				"JavaScriptParserProblems_" + problemId,
				defaultProblemSeverity);
 		switch (severity) {
 		case ProblemSeverities.Error:
 			return Severity.ERROR;
 		case ProblemSeverities.Ignore:
 			return null;
 		}
		return defaultSeverity;
 
 	}
 
 }
