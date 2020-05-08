 package org.eclipse.dltk.examples.internal.python.core;
 
import java.util.regex.Pattern;

 import org.eclipse.dltk.core.ScriptContentDescriber;
 
 public class ExamplePythonContentDescriber extends ScriptContentDescriber {
 
 	public ExamplePythonContentDescriber() {
 	}
 
	protected static Pattern[] header_patterns = { Pattern.compile(
			"^#!.*python.*", Pattern.MULTILINE) }; //$NON-NLS-1$

	protected Pattern[] getHeaderPatterns() {
		return header_patterns;
 	}
 }
