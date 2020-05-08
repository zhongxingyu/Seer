 package de.unisiegen.informatik.bs.alvis.commands;
 
 import java.io.File;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
 import org.eclipse.ui.model.BaseWorkbenchContentProvider;
 import org.eclipse.ui.model.WorkbenchLabelProvider;
 import org.eclipse.ui.part.FileEditorInput;
 import de.unisiegen.informatik.bs.alvis.Activator;
 import de.unisiegen.informatik.bs.alvis.Run;
 import de.unisiegen.informatik.bs.alvis.compiler.CompilerAccess;
 import de.unisiegen.informatik.bs.alvis.tools.IO;
 
 public class RunCompile extends AbstractHandler {
 
 	Run seri;
 	ExecutionEvent myEvent;
 
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		// NOTE: event is null when executing from run editor.
 		myEvent = event;
 		// Save all Editors
 		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
 				.saveAllEditors(true);
 
 		new CloseRunPerspective().execute(event);
 
 		// Instantiate IEditorInput
 		IEditorInput input = null;
 
 		/*
 		 * Register datatypes and packagenames to the compiler This is important
 		 * for compiling
 		 */
 		CompilerAccess.getDefault().setDatatypes(
 				Activator.getDefault().getAllDatatypesInPlugIns());
 		CompilerAccess.getDefault().setDatatypePackages(
 				Activator.getDefault().getAllDatatypesPackagesInPlugIns());
 
 		// System.out.println(Platform.getInstanceLocation().getURL().getPath());
 		// CompilerAccess.getDefault().testDatatypes();
 
 		try {
 			// What to run? get the input (filepath)
 			input = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
 					.getActivePage().getActiveEditor().getEditorInput();
 		} catch (NullPointerException e) {
 			e.printStackTrace();
 		}
 
 		// Instantiate a new Run object
 		seri = null;
 
 		/*
 		 * GET THE RUN OBJECT
 		 */
 		// Check if the input is a FileEditorInput
 		if (input != null && input instanceof FileEditorInput) {
 			// cast to FileEditorInput
 			FileEditorInput fileInput = (FileEditorInput) input;
 			// If the user has choosen a graph to run...
 			if (fileInput.getFile().getFileExtension().equals("run")) { //$NON-NLS-1$
 				// get the path in system
 				String systemPath = fileInput.getPath().toString();
 				// and deserialize the saved run to seri
 				seri = (Run) IO.deserialize(systemPath);
 			} else {
 				// ask for run settings
 				seri = getPreferencesByDialog();
 			}
 		} else {
 			// ask for run settings
 			seri = getPreferencesByDialog();
 		}
 
 		// END OF GET THE RUN OBJECT
 
 		if (seri != null) {
 
 			// GET THE ALGORITHM AS STRING
 			try {
 				// Translate the PseudoCode and get the translated file
 				File javaCode = null;
 				
 				// if the algorithm is of type ".algo" it is pseudo code and it must be compiled
 				// if not, it's passed on to the virtual machine
 				if(seri.getAlgorithmFile().endsWith(".algo")){
 					// try to compile with compiler
 					javaCode = CompilerAccess.getDefault().compile(
 							seri.getAlgorithmFile());
 	
 					// if fails
 					if (null == javaCode) // compile with dummy
						throw new Exception();// TODO throw a meaningful exception
 				}
 				else{
 					javaCode = new File(Platform.getInstanceLocation().getURL().getPath() + seri.getAlgorithmFile());
 				}
 				// Kill the extension
 				String fileNameOfTheAlgorithm = javaCode.getCanonicalPath()
 						.replaceAll("\\.java$", ""); //$NON-NLS-1$
 
 				// Get the path where the translated files are saved to.
 				String pathToTheAlgorithm = javaCode.getParentFile()
 						.getCanonicalPath();
 
 				// Register Algorithm to VM
 
 				// TODO Warning, if we change the name of the translated file
 				// this here will crash
 				fileNameOfTheAlgorithm = "Algorithm";
 
 				// setJavaAlgorithmToVM has 2 parameter 1. is the path 2. is the
 				// filename
 				// if /usr/alvis/src/Algorithm.java then
 				// 1.: /usr/alvis/src
 				// 2.: Algorithm
 				Activator.getDefault().setJavaAlgorithmToVM(pathToTheAlgorithm,
 						fileNameOfTheAlgorithm,
 						Activator.getDefault().getAllDatatypesInPlugIns());
 
 				Activator.getDefault().setActiveRun(seri);
 				// Then activate command SwitchToRunPerspective
 				new SwitchToRunPerspective().execute(event);
 			} catch (Exception e) {
 				// Create the required Status object
 				Status status = new Status(IStatus.ERROR, "My Plug-in ID", 0,
 						e.getMessage(), null);
 
 				// Display the dialog
 				ErrorDialog
 						.openError(
 								Display.getCurrent().getActiveShell(),
 								"Error starting the Run",
 								"An Error has occurred. The run could not start. Read the message shown below to solve the problem.",
 								status);
 				e.printStackTrace();
 			} finally {
 
 			}
 
 		} else {
 			return null;
 		}
 
 		// IResource.refreshLocal();
 
 		new RefreshWorkspace().execute(event);
 
 		return null;
 	}
 
 	private Run getPreferencesByDialog() {
 		
 		String extensions = Activator.getDefault().getFileExtensionsAsCommaSeparatedList();
 		
 		Run seri = new Run();
 		while (seri.getAlgorithmFile().equals("") | seri.getExampleFile().equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
 			ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(
 					PlatformUI.getWorkbench().getActiveWorkbenchWindow()
 							.getShell(), new WorkbenchLabelProvider(),
 					new BaseWorkbenchContentProvider());
 			dialog.setTitle(Messages.RunCompile_7);
 			dialog.setMessage(NLS.bind(Messages.RunCompile_8, "(" + extensions + ")"));
 			dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
 			dialog.open();
 
 			if (dialog.getResult() != null) {
 				String result = ""; //$NON-NLS-1$
 				for (Object o : dialog.getResult()) {
 					result = o.toString();
 					for(String fileExtension : Activator.getDefault().getFileExtensions()){
 						if (result.startsWith("L") && result.endsWith(fileExtension)) { //$NON-NLS-1$ //$NON-NLS-2$
 							result = result.substring(2); // cut the first two chars
 							seri.setExampleFile(result);
 						}
 					}
 					if (result.startsWith("L") && (result.endsWith("algo")|| result.endsWith(".java"))) { //$NON-NLS-1$ //$NON-NLS-2$
 						result = result.substring(2); // cut the first two chars
 						seri.setAlgorithmFile(result);
 					}
 				}
 			}
 			if (dialog.getReturnCode() == 1) // the user clicked cancel
 				return null;
 		}
 
 		return seri;
 	}
 
 }
