 /*
  * (c) Fachhochschule Potsdam
  */
 package org.fritzing.fritzing.diagram.part;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Preferences;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramGraphicalViewer;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.IWorkbenchWindowActionDelegate;
 import org.fritzing.fritzing.diagram.export.Fritzing2Eagle;
 import org.fritzing.fritzing.diagram.preferences.EaglePreferencePage;
 
 /**
  * @generated NOT
  */
 public class FritzingPCBExportAction implements
 		IWorkbenchWindowActionDelegate {
 
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
 		// STEP 1: create Eagle script file from Fritzing files
 				
 	    // use currently active diagram
 		IDiagramGraphicalViewer viewer = FritzingDiagramEditorUtil.getActiveDiagramEditor()
 			.getDiagramGraphicalViewer();
 		String script = Fritzing2Eagle.createEagleScript(viewer);
 		
 		// TODO: write script to file
 	    URI fritzingDiagramURI = FritzingDiagramEditorUtil.getActiveDiagramURI();
 	    String fritzing2eagleSCR = fritzingDiagramURI.trimFileExtension()
     		.appendFileExtension("scr").toFileString();
 		
 	    try {
 			Writer w = new FileWriter(fritzing2eagleSCR);
 			w.write(script);
 			w.close();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 	    
 	    // STEP 2: start Eagle ULP on the created script file
 	    
 		// EAGLE folder
 		Preferences preferences = FritzingDiagramEditorPlugin.getInstance().getPluginPreferences();
 		String eagleLocation = preferences.getString(
 			EaglePreferencePage.EAGLE_LOCATION) + File.separator;
 		// EAGLE executable
 		String eagleExec = "";
 	    if(Platform.getOS().equals(Platform.OS_WIN32)) {
	    	eagleExec = "bin/eagle.exe"; 
 	    }
 	    else if(Platform.getOS().equals(Platform.OS_MACOSX)) {
	    	eagleExec = "EAGLE.app/Contents/MacOS/eagle"; 
 	    }
 	    else if(Platform.getOS().equals(Platform.OS_LINUX)) {
	    	eagleExec = "bin/eagle"; 
 	    }
 	    // EAGLE PCB ULP
//	    String eagleULP = Platform.getLocation().toString() + "/eagle/ulp/fritzing_master.ulp";
 		String eagleULP = eagleLocation + "ulp/fritzing_master.ulp";
 	    // EAGLE Schematc
 		String eagleSCH = fritzingDiagramURI.trimFileExtension()
     		.appendFileExtension("sch").toFileString();
 	    // EAGLE parameters
 	    String eagleParams = "-C\"RUN " + 
 	    	"'" + eagleULP + "' " + 
 	    	"'" + fritzing2eagleSCR + "'\" " +
	    	"\"" + eagleSCH + "\"" ;	
 	    // Run!
 	    String command = 
	    	"\"" + eagleLocation + eagleExec + "\" " + eagleParams;
 	    try {
 			Runtime.getRuntime().exec(command);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
