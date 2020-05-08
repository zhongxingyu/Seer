 package org.eclipse.dltk.examples.internal.python.interpreter.ui;
 
import org.eclipse.dltk.examples.internal.python.core.PythonCorePlugin;
 import org.eclipse.dltk.internal.debug.ui.interpreters.AbstractInterpreterLibraryBlock;
 import org.eclipse.dltk.internal.debug.ui.interpreters.AddScriptInterpreterDialog;
 import org.eclipse.dltk.internal.debug.ui.interpreters.LibraryLabelProvider;
import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.viewers.IBaseLabelProvider;
 
public class ExamplePythonInterpreterLibraryBlock extends AbstractInterpreterLibraryBlock {
 	public ExamplePythonInterpreterLibraryBlock(AddScriptInterpreterDialog d) {
 		super(d);
 	}
 	protected IBaseLabelProvider getLabelProvider() {
 		return new LibraryLabelProvider();
 	}
	protected IDialogSettings getDialogSettions() {
		return PythonCorePlugin.getDefault().getDialogSettings();
	}
 }
