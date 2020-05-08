 package org.eclipse.dltk.examples.internal.python.ui;
 
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.examples.internal.python.core.ExamplePythonLanguageToolkit;
 import org.eclipse.dltk.examples.internal.python.core.PythonCorePlugin;
 import org.eclipse.dltk.ui.AbstractDLTKUILanguageToolkit;
 import org.eclipse.dltk.ui.ScriptElementLabels;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 
 public class ExamplePythonUILanguageToolkit extends AbstractDLTKUILanguageToolkit {
 	private static class PythonScriptElementLabels extends ScriptElementLabels {
 		public void getElementLabel(IModelElement element, long flags,
 				StringBuffer buf) {
 			StringBuffer buffer = new StringBuffer(60);
 			super.getElementLabel(element, flags, buffer);
 			String s = buffer.toString();
 			if (s != null && !s.startsWith(element.getElementName())) {
 				if (s.indexOf('$') != -1) {
 					s = s.replaceAll("\\$", ".");
 				}
 			}
 			buf.append(s);
 		}
 
 		protected char getTypeDelimiter() {
 			return '$';
 		}
 	};
 	
 	public ScriptElementLabels getScriptElementLabels() {
 		return new PythonScriptElementLabels();
 	}
 	protected AbstractUIPlugin getUIPLugin() {
 		return PythonCorePlugin.getDefault();
 	}
 	public IDLTKLanguageToolkit getCoreToolkit() {
 		return ExamplePythonLanguageToolkit.getDefault();
	}
 }
