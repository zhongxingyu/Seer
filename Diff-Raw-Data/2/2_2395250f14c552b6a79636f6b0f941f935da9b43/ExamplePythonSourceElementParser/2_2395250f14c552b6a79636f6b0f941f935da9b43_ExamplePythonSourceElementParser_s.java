 package org.eclipse.dltk.examples.internal.python.core.parser;
 
 import org.eclipse.dltk.compiler.ISourceElementRequestor;
import org.eclipse.dltk.compiler.ISourceElementRequestor.TypeInfo;
 import org.eclipse.dltk.core.AbstractSourceElementParser;
 import org.eclipse.dltk.core.ISourceModuleInfoCache.ISourceModuleInfo;
 import org.eclipse.dltk.examples.internal.python.core.ExamplePythonNature;
 
 public class ExamplePythonSourceElementParser extends
 		AbstractSourceElementParser {
 	public void parseSourceModule(
 			org.eclipse.dltk.compiler.env.ISourceModule module,
 			ISourceModuleInfo astCache) {
 		ISourceElementRequestor requestor = getRequestor();
 
 		requestor.enterModule();
 		TypeInfo info = new TypeInfo();
 		info.name = "Example type"; //$NON-NLS-1$
 		requestor.enterType(info);
 		requestor.exitType(0);
 		requestor.exitModule(0);
 	}
 
 	protected String getNatureId() {
 		return ExamplePythonNature.PYTHON_NATURE;
 	}
 }
