 package org.eclipse.dltk.validators.internal.ui.popup.actions;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IParent;
 import org.eclipse.dltk.validators.core.ValidatorRuntime;
 import org.eclipse.dltk.validators.internal.core.ValidatorUtils;
 import org.eclipse.dltk.validators.internal.ui.ValidatorConsoleTrackerManager;
 import org.eclipse.dltk.validators.internal.ui.ValidatorUpdater;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.IActionDelegate;
 import org.eclipse.ui.IObjectActionDelegate;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.console.ConsolePlugin;
 import org.eclipse.ui.console.IConsole;
 import org.eclipse.ui.console.IConsoleManager;
 import org.eclipse.ui.console.IOConsole;
 import org.eclipse.ui.console.IOConsoleOutputStream;
 import org.eclipse.ui.console.IPatternMatchListener;
 
 public class ValidatorSelectionWithConsoleAction implements
 		IObjectActionDelegate {
 	ISelection selection;
 
 	/**
 	 * Constructor for Action1.
 	 */
 	public ValidatorSelectionWithConsoleAction() {
 		super();
 	}
 
 	/**
 	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
 	 */
 	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
 	}
 
 	/**
 	 * @see IActionDelegate#run(IAction)
 	 */
 	public void run(IAction action) {
 		IConsoleManager consoleManager = ConsolePlugin.getDefault()
 				.getConsoleManager();
 		IOConsole ioConsole = new IOConsole("DLTK Validators output", null);
 		IPatternMatchListener[] listeners = ValidatorConsoleTrackerManager.getListeners();
 		for (int i = 0; i < listeners.length; i++) {
 			ioConsole.addPatternMatchListener(listeners[i]);
 		}
 		consoleManager.addConsoles(new IConsole[] { ioConsole });
 		consoleManager.showConsoleView(ioConsole);
 		IOConsoleOutputStream newOutputStream = ioConsole.newOutputStream();
 		if( this.selection == null ) {
 			return;
 		}
 		processSelectionToElements(newOutputStream, selection);
 		try {
 			newOutputStream.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void processSelectionToElements(OutputStream out, ISelection selection ) {
 		List elements = new ArrayList();
 		List resources = new ArrayList();
 		if (this.selection != null
 				&& this.selection instanceof IStructuredSelection) {
 			IStructuredSelection sel = (IStructuredSelection) this.selection;
 			Iterator iterator = sel.iterator();
 			for (; iterator.hasNext();) {
 				Object o = iterator.next();
 				ValidatorUtils.processResourcesToElements(o, elements, resources);
 			}
 		}
		ValidatorRuntime.executeAllValidatorsWithConsole(out, elements, resources);
 	}
 
 	/**
 	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
 	 */
 	public void selectionChanged(IAction action, ISelection selection) {
 		this.selection = selection;
 	}
 
 }
