 /*
  * (c) Fachhochschule Potsdam
  */
 package org.fritzing.fritzing.diagram.part;
 
 import java.io.File;
import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
import java.io.OutputStreamWriter;
 import java.io.Writer;
 
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Preferences;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramWorkbenchPart;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.IWorkbenchWindowActionDelegate;
 import org.fritzing.fritzing.diagram.export.Fritzing2Eagle;
 import org.fritzing.fritzing.diagram.preferences.EaglePreferencePage;
 
 /**
  * @generated NOT
  */
 public class FritzingPCBExportAction implements IWorkbenchWindowActionDelegate {
 
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
 		
 		IDiagramWorkbenchPart editor = null;
 		URI diagramUri = null;
 		
 		try {
 			// use currently active diagram
 			editor = FritzingDiagramEditorUtil.getActiveDiagramPart();
 			diagramUri = FritzingDiagramEditorUtil.getActiveDiagramURI();
 		} catch (NullPointerException npe) {
 			ErrorDialog.openError(getShell(), "PCB Export Error",
 					Messages.FritzingPCBExportAction_NoOpenSketchError,
 					new Status(Status.ERROR, FritzingDiagramEditorPlugin.ID,
 							"No sketch currently opened.", npe));
 			return;
 		}
 		// validate
 		ValidateAction.runValidation(editor.getDiagramEditPart(), editor.getDiagram());
 		// TODO: warn if validation shows errors
 		
 		// transform into EAGLE script
 		String script = Fritzing2Eagle.createEagleScript(editor.getDiagramGraphicalViewer());
 
 		String fritzing2eagleSCR = diagramUri.trimFileExtension()
 				.appendFileExtension("scr").toFileString();
 		try {
			FileOutputStream fos = new FileOutputStream(fritzing2eagleSCR);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			osw.write(script);
			osw.flush();
			fos.flush();
			fos.getFD().sync();
			// NOTE: FileWriter would be nice here, but it doesn't sync immediately
 		} catch (IOException ioe) {	
 			ErrorDialog.openError(getShell(), "PCB Export Error",
 				"Could not create file " + fritzing2eagleSCR + " for writing the EAGLE script.\n"+
 				"Please check if Fritzing has write access.",
 				new Status(Status.ERROR, FritzingDiagramEditorPlugin.ID,
 						"File could not be written.", ioe));
 			return;
 		}
 
 		// STEP 2: start Eagle ULP on the created script file
 
 		// EAGLE folder
 		Preferences preferences = FritzingDiagramEditorPlugin.getInstance()
 				.getPluginPreferences();
 		String eagleLocation = preferences
 				.getString(EaglePreferencePage.EAGLE_LOCATION) + File.separator;
 		// EAGLE executable
 		String eagleExec = "";
 		if (Platform.getOS().equals(Platform.OS_WIN32)) {
 			eagleExec = eagleLocation + "bin\\eagle.exe";
 		} else if (Platform.getOS().equals(Platform.OS_MACOSX)) {
 			eagleExec = eagleLocation + "EAGLE.app/Contents/MacOS/eagle";
 		} else if (Platform.getOS().equals(Platform.OS_LINUX)) {
 			eagleExec = eagleLocation + "bin/eagle";
 		}
 		if (! new File(eagleExec).exists()) {
 			ErrorDialog.openError(getShell(), "PCB Export Error",
 				"Could not find EAGLE installation at " + eagleLocation + ".\n"+
 				"Please check if you correctly set the EAGLE location in the preferences.",
 				new Status(Status.ERROR, FritzingDiagramEditorPlugin.ID,
 						"No EAGLE executable at "+eagleExec, null));
 			return;
 		}
 		// EAGLE PCB ULP
 		String eagleULP = "";
 		if (Platform.getOS().equals(Platform.OS_WIN32)) {
 			eagleULP = eagleLocation + "ulp\\fritzing_master.ulp";
 		} else if (Platform.getOS().equals(Platform.OS_MACOSX)) {
 			eagleULP = eagleLocation + "ulp/fritzing_master.ulp";		
 		} else if (Platform.getOS().equals(Platform.OS_LINUX)) {
 			eagleULP = eagleLocation + "ulp/fritzing_master.ulp";
 		}
 		
 		if (! new File(eagleULP).exists()) {
 			ErrorDialog.openError(getShell(), "PCB Export Error",
 				"Could not find Fritzing ULP at " + eagleULP + ".\n"+
 				"Please check if you copied the Fritzing EAGLE files to the EAGLE subfolders.",
 				new Status(Status.ERROR, FritzingDiagramEditorPlugin.ID,
 						"Fritzing ULP not found.", null));
 			return;
 		} 
 		// EAGLE .sch and .brd files
 		String eagleSCH = diagramUri.trimFileExtension()
 				.appendFileExtension("sch").toFileString();
 		String eagleBRD = diagramUri.trimFileExtension()
 				.appendFileExtension("brd").toFileString();
 		// Delete existing ones so that EAGLE will create new ones
 		// TODO: warn user before deleting files
 		File eagleSchFile = new File(eagleSCH);
 		if (eagleSchFile.exists()) {
 			eagleSchFile.delete(); 
 		}
 		File eagleBrdFile = new File(eagleBRD);
 		if (eagleBrdFile.exists()) {
 			eagleBrdFile.delete();
 		}
 		// EAGLE parameters
 		String eagleParams = "-C\"RUN " + "'" + eagleULP + "' " + "'"
 				+ fritzing2eagleSCR + "'\" " + "\"" + eagleSCH + "\"";
 		// Run!
 		String command = eagleExec + " " + eagleParams;
 		// special case for OSX:
 		if (Platform.getOS().equals(Platform.OS_MACOSX)) {
 			// write command to file
 			String macExec = diagramUri.trimFileExtension()
 					.appendFileExtension("sh").toFileString();
 			try {
				FileOutputStream fos = new FileOutputStream(macExec);
				OutputStreamWriter osw = new OutputStreamWriter(fos);
				osw.write(command);
				osw.flush();
				fos.flush();
				fos.getFD().sync();
				// NOTE: FileWriter would be nice here, but it doesn't sync immediately
 			} catch (IOException ioe) {	
 				System.out.println(ioe.getMessage());
 			}
 			// make it executable
 			try {
 				Runtime.getRuntime().exec("chmod a+x " + macExec);
 			} catch (IOException e) {
 				System.out.println(e.getMessage());
 			}
 			command = macExec;
 		}
 		try {
 			Runtime.getRuntime().exec(command);
 		} catch (IOException ioe1) {
 			ErrorDialog.openError(getShell(), "PCB Export Error",
 					"Could not launch EAGLE PCB export.\n"+
 					"Please check that all of the files in the following command exist:\n\n"+
 					command,
 					new Status(Status.ERROR, FritzingDiagramEditorPlugin.ID,
 							"EAGLE PCB export failed.", ioe1));
 				return;
 		}
 	}
 }
