 package org.eclipse.dltk.examples.internal.python.ui;
 
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.examples.internal.python.core.ExamplePythonLanguageToolkit;
 import org.eclipse.dltk.examples.internal.python.core.PythonCorePlugin;
 import org.eclipse.dltk.ui.AbstractDLTKUILanguageToolkit;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 
 public class ExamplePythonUILanguageToolkit extends AbstractDLTKUILanguageToolkit {
 	protected AbstractUIPlugin getUIPLugin() {
 		return PythonCorePlugin.getDefault();
 	}
 	public IDLTKLanguageToolkit getCoreToolkit() {
 		return ExamplePythonLanguageToolkit.getDefault();
 	} 
 }
