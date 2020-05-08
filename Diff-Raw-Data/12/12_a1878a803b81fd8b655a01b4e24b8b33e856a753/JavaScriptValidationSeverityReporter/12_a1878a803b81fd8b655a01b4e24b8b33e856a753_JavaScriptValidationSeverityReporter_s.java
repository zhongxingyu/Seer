 package org.eclipse.dltk.internal.javascript.validation;
 
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.dltk.compiler.problem.ProblemSeverities;
 import org.eclipse.dltk.javascript.core.JavaScriptPlugin;
 import org.eclipse.dltk.javascript.parser.ISeverityReporter;
 import org.eclipse.dltk.javascript.parser.Reporter.Severity;
 
 public class JavaScriptValidationSeverityReporter implements ISeverityReporter {
 
	public Severity getSeverity(int problemId) {
 
 		int severity = new InstanceScope().getNode(JavaScriptPlugin.PLUGIN_ID)
 				.getInt("JavaScriptProblems_" + problemId,
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
