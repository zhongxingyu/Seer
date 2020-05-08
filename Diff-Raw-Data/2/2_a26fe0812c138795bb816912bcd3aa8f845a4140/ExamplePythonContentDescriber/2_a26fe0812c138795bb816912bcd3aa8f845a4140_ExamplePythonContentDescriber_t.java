 package org.eclipse.dltk.examples.internal.python.core;
 import java.io.IOException;
 import java.io.Reader;
 
 import org.eclipse.core.runtime.content.IContentDescription;
 import org.eclipse.dltk.core.ScriptContentDescriber;
 
 public class ExamplePythonContentDescriber extends ScriptContentDescriber {
 
 	public ExamplePythonContentDescriber() {
 	}
 
 	/**
	 * This method could be extended to use pattern matching for files without
 	 * extension.
 	 */
 	public int describe(Reader contents, IContentDescription description)
 			throws IOException {
 		return ScriptContentDescriber.INDETERMINATE;
 	}
 }
