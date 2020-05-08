 package org.eclipse.dltk.javascript.parser;
 
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.dltk.compiler.problem.ProblemSeverities;
 import org.eclipse.dltk.javascript.parser.Reporter.Severity;
 
 public class JavaScriptParserSeverityReporter implements ISeverityReporter {
 
	public Severity getSeverity(int problemId) {
 		int severity = new InstanceScope().getNode(
 				JavaScriptParserPlugin.PLUGIN_ID).getInt(
 				"JavaScriptParserProblems_" + problemId,
				ProblemSeverities.Warning);
 		switch (severity) {
 		case ProblemSeverities.Error:
 			return Severity.ERROR;
 		case ProblemSeverities.Ignore:
 			return null;
 		}
		return Severity.WARNING;
 
 	}
 
 }
