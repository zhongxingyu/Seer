 package org.eclipse.dltk.javascript.parser;
 
 import org.eclipse.dltk.javascript.parser.Reporter.Severity;
 
 public interface ISeverityReporter {
	public Severity getSeverity(int problemId);
 }
