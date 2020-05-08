 package org.mumps.meditor;
 
 import gov.va.med.iss.meditor.utils.MEditorMessageConsole;
 import gov.va.med.iss.meditor.utils.MEditorUtilities;
 import gov.va.med.iss.meditor.utils.RoutineChangedDialog;
 import gov.va.med.iss.meditor.utils.RoutineChangedDialogData;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.PlatformUI;
 
 public class SaveRoutine {
 	
 	private MEditorRPC rpc;
 
 	public SaveRoutine(MEditorRPC rpc) {
 		this.rpc = rpc;
 	}
 	
 	public void save(String routineName, String projectName, String fileCode) throws ServerSaveFailure {
 		//Load routine from server
 		String serverCode = null;
 		boolean isNewRoutine = false;
 		try { //attempt Load routine into a string
 			serverCode = rpc.getRoutineFromServer(routineName);
 		} catch (RoutineNotFoundException e) {
 			//save the routine as a new file.
 			isNewRoutine = true;
 		}
 
 		String backupCode = null;
 		try {
 			if (!isNewRoutine)
 				backupCode = MEditorUtils.getBackupFileContents(projectName, routineName);
 		} catch (FileNotFoundException e1) {
 			//inform backup file cannot be found and ask whether or not to proceed with a dialog
 //			MessageDialog dialog = new MessageDialog(null, "MEditor", null,
 //					"This routine exists on the server, but no backup file exists in this project. Therefore it is not known if the editor and the server are in sync.", MessageDialog.QUESTION, 
 //					new String[] { "Yes", "No" }, 
 //							1); // No is the default
 //	   int result = dialog.open();
 			
 			if (!MEditorUtils.compareRoutines(fileCode, serverCode)) {
 			
 				RoutineChangedDialog dialog = new RoutineChangedDialog(Display.getDefault().getActiveShell());
 				RoutineChangedDialogData userInput = dialog.open(routineName, serverCode, fileCode, false, false,
 						"This routine exists on the server, but no backup file exists"); //TODO: fix this dialog so it display wrapped text messages. "in this project. Therefore it is not known if the editor and the server are in sync.");
 				if (!userInput.getReturnValue()) {
 					throw new ServerSaveFailure("User cancelled, no previous backup file was found.");
 //					super.doSave(monitor);
 //					return; 
 				}
 			}
 			
 		} catch (CoreException | IOException e1) {
 			e1.printStackTrace();
 			String message = "Failed to save routine onto the server. Error occured while loading the backup file: " +e1.getMessage();
 			MessageDialog.openError(PlatformUI.getWorkbench()
 					.getActiveWorkbenchWindow().getShell(), "MEditor", message
 					);
 			throw new ServerSaveFailure(message, e1);
 //			super.doSave(monitor);
 //			return;
 		}
 
 		
 		//First compare contents of editor to contents on server to see if there is even a proposed change
 		boolean isCopy = false;
 		if (!isNewRoutine && MEditorUtils.compareRoutines(fileCode, serverCode)) {
 			//show prompt asking about whether to cancel because they are same, or to continue thereby updating the routine header on server and client	
 			
 //			MessageDialog.openError(PlatformUI.getWorkbench()
 //					.getActiveWorkbenchWindow().getShell(), "MEditor",
 //					"Rotine on server is identical to the local routine. Nothing new to deploy to server.");
 			
 			MessageDialog dialog = new MessageDialog(null, "MEditor", null,
 			"Routines are the same on the client and in this project. Would " +
 			"you like to continue by updating the date in the routine header" +
 			" on both the client and server?",
 			MessageDialog.QUESTION, 
 			new String[] { "OK", "Cancel" },
 					1); // Cancel is the default
 			if (dialog.open() != 0) {
 				throw new ServerSaveFailure("User cancelled, files are the same on client and server.");
 //				super.doSave(monitor);
 //				return;
 			} else			
 				isCopy = true;
 		}
 		
 		//Next compare contents of server to contents of backup to see if MEditor was the last to touch the server
		if (!isNewRoutine && backupCode != null && !MEditorUtils.compareRoutines(backupCode, serverCode)) {
 			RoutineChangedDialog dialog = new RoutineChangedDialog(Display.getDefault().getActiveShell());
 			RoutineChangedDialogData userInput = dialog.open(routineName, serverCode, backupCode, true, false);
 			if (!userInput.getReturnValue()) {
 				throw new ServerSaveFailure("User cancelled, backup file differs from what is on server.");
 //				super.doSave(monitor);
 //				return;
 			}
 		}
 		
 		//Save to server and display XINDEX results
 		String saveResults = "";
 		try {
 			saveResults = rpc.saveRoutineToServer(routineName, fileCode, isCopy);
 		} catch (Throwable t) {
 			saveResults = "Unable to save routine " +routineName+ " to server";
 //			return;
 			throw new ServerSaveFailure("RPC failed.", t);
 		} finally {
 			try {
 				MEditorMessageConsole.writeToConsole(saveResults);
 			} catch (Exception e) {
 				MessageDialog.openError(
 						MEditorUtilities.getIWorkbenchWindow().getShell(),
 						"Meditor Plug-in Routine Save",
 						saveResults);
 			}
 		}
 		
 		//Sync the latest on server to the backup
 		try {
 			MEditorUtils.syncBackup(projectName, routineName, fileCode);
 		} catch (CoreException e) {
 			// show warning only
 			e.printStackTrace();
 			MessageDialog.openWarning(PlatformUI.getWorkbench()
 					.getActiveWorkbenchWindow().getShell(), "MEditor",
 					"Routine Saved on server, but error occured while syncing the local backup file: " +e.getMessage());			
 		}
 	}
 	
 }
