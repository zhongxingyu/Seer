 /*
  * (c) Fachhochschule Potsdam
  */
 package org.fritzing.fritzing.diagram.part;
 
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.IWorkbenchWindowActionDelegate;
 
 /**
  * @generated NOT
  */
 public class FritzingExampleAction implements IWorkbenchWindowActionDelegate {
 
 	/**
 	 * @generated NOT
 	 */
 	private IWorkbenchWindow window;
 
 	/**
 	 * @generated NOT
 	 */
 	public void init(IWorkbenchWindow window) {
 		this.window = window;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	public void dispose() {
 		window = null;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	public void selectionChanged(IAction action, ISelection selection) {
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	private Shell getShell() {
 		return window.getShell();
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	public void run(IAction action) {
 		String exampleLocation = 
			Platform.getInstallLocation().getURL() + "/examples/HelloWorld.fzb";
 		FritzingDiagramEditorUtil.openFritzingFile(
 				URI.createFileURI(exampleLocation));
 	}
 }
