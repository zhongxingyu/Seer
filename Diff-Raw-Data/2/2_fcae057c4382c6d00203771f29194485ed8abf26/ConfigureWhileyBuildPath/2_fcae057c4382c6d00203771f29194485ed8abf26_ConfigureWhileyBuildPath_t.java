 package wyclipse.ui.actions;
 
 import java.util.Arrays;
 import java.util.Collections;
 
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.IHandler;
 import org.eclipse.core.commands.IHandlerListener;
 import org.eclipse.core.resources.ICommand;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jface.preference.PreferenceDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.dialogs.PreferencesUtil;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 import wyclipse.core.Activator;
 import wyclipse.core.WhileyNature;
 
 /**
  * Opens the properties dialog for configuring the Whiley Build Path. When
  * applied to a project which does not include the <code>WhileyNature</code>,
  * then this is automatically added an initialised appropriately. Specifically,
  * this will be configured based on the ".whileypath" file (if one exists), or
  * will fall back to a default setup.
  * 
  * @author David J. Pearce
  * 
  */
 public class ConfigureWhileyBuildPath implements IHandler {
 
 	@Override
 	public void addHandlerListener(IHandlerListener handlerListener) {
 	}
 
 	@Override
 	public void dispose() {}
 
 	@Override
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		System.out.println("*** ConfigureWhileyBuildPath action called");
 
 		// First, determine whether this command was executed on a selected
 		// project or not.
 
 		try {
 			IWorkbenchWindow activeWorkbenchWindow = HandlerUtil
 					.getActiveWorkbenchWindow(event);
 			ISelection selection = activeWorkbenchWindow.getActivePage()
 					.getSelection();
 			
 			// TODO: update the following so that this works on all structured
 			// selections within a project.
 			
 			if (selection != null & selection instanceof IStructuredSelection) {
 				IStructuredSelection iss = (IStructuredSelection) selection;
 				Object firstElement = iss.getFirstElement();
 				
 				if (firstElement instanceof IProject) {
 					IProject project = (IProject) firstElement;
 					
 					if (!hasWhileyNature(project)) {
 						configureWhileyNature(project);
 					}
 					
 					// Project definitely has Whiley nature so jump straight to
 					// the Whiley Build Path Configuration page.
 					openPropertiesDialog(project, activeWorkbenchWindow);
 				}
 			}
 		} catch (CoreException e) {
 			throw new ExecutionException(
 					"A problem occurred configuring the Whiley Build Path", e);
 		}
 
 		return null;
 	}
 
 	@Override
 	public boolean isEnabled() {
 		return true;
 	}
 
 	@Override
 	public boolean isHandled() {
 		return true;
 	}
 
 	@Override
 	public void removeHandlerListener(IHandlerListener handlerListener) { }
 
 	/**
 	 * Determine whether a given project has the Whiley nature associated with
 	 * it or not. This is important because, if it does, then we can go straight
 	 * to the Whiley Build Path property page. Otherwise, we need to use a
 	 * wizard.
 	 * 
 	 * @param project
 	 * @return
 	 * @throws CoreException
 	 */
 	private boolean hasWhileyNature(IProject project) throws CoreException {
 		IProjectDescription description = project.getDescription();
 		for (String natureId : description.getNatureIds()) {
 			if (natureId.equals(wyclipse.core.Activator.WYCLIPSE_NATURE_ID)) {
 				return true;
 
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Force the Whiley Build Path property page to open in the properties
 	 * Dialog.
 	 * 
 	 * @param project
 	 * @param window
 	 */
 	private void openPropertiesDialog(IProject project, IWorkbenchWindow window) {
 		PreferenceDialog dialog = PreferencesUtil
 				.createPropertyDialogOn(
 						window.getShell(),
 						project,
 						wyclipse.ui.Activator.WHILEY_BUILD_PATH_PROPERTY_PAGE_ID,
 						new String[] { wyclipse.ui.Activator.WHILEY_BUILD_PATH_PROPERTY_PAGE_ID },
 						Collections.EMPTY_MAP);
 		dialog.open();
 	}
 	
 	/**
 	 * Add a default whiley nature onto this project, including an appropriate
 	 * ".whileypath" file.
 	 * 
 	 * @param project
 	 */
 	private void configureWhileyNature(IProject project) throws CoreException {
 
 		// Fist, add the WhileyNature to the given project.
 		IProjectDescription desc = project.getDescription();
 		String[] natures = desc.getNatureIds();
 			
 		String[] newNatures = new String[natures.length+1];
 		System.arraycopy(natures, 0, newNatures, 0, natures.length);
 		newNatures[natures.length] = Activator.WYCLIPSE_NATURE_ID;
 		desc.setNatureIds(newNatures);
 		
 		// Second, add Whiley Builder onto list of builders. We put this before
 		// all other builders to ensure that it runs before e.g. the Java
 		// builder.
 		ICommand buildCommand = desc.newCommand();				
 		buildCommand.setBuilderName(Activator.WYCLIPSE_BUILDER_ID);
 
 		ICommand[] oldBuilders = desc.getBuildSpec();
		ICommand[] newBuilders = new ICommand[oldBuilders.length+1];
 		System.arraycopy(natures, 0, newNatures, 1, natures.length);
 		newBuilders[0] = buildCommand;
 		desc.setBuildSpec(newBuilders);		
 		
 		project.setDescription(desc, null);	
 		
 		// At this point, we cannot be guaranteed that the WhileyNature object
 		// is constructed.
 		
 		// TODO: detect existing .whileypath and/or configure new one
 		WhileyNature.initialiseWhileyPath(project, null);
 	}
 }
