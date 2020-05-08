 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.debug.ui.launcher;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationType;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.ui.DebugUITools;
 import org.eclipse.debug.ui.IDebugModelPresentation;
 import org.eclipse.debug.ui.ILaunchShortcut;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IParent;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.debug.ui.DLTKDebugUIPlugin;
 import org.eclipse.dltk.debug.ui.messages.DLTKLaunchMessages;
 import org.eclipse.dltk.internal.launching.DLTKLaunchingPlugin;
 import org.eclipse.dltk.launching.IDLTKLaunchConfigurationConstants;
 import org.eclipse.dltk.launching.LaunchingMessages;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.operation.IRunnableContext;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.ElementListSelectionDialog;
 import org.eclipse.ui.model.WorkbenchLabelProvider;
 
 
 
 public abstract class ScriptLaunchShortcut implements ILaunchShortcut {
 	public void launch(ISelection selection, String mode) {
 		if (selection instanceof IStructuredSelection) {
 			searchAndLaunch(((IStructuredSelection)selection).toArray(), mode, getScriptSelectionTitle(), getSelectionEmptyMessage());
 		}
 	}
 
 	/**
 	 * @param search the elements to search for a main script
 	 * @param mode the mode to launch in
 	 */
 	public void searchAndLaunch(Object[] search, String mode, String selectMessage, String emptyMessage) {
 		IResource[] scripts = null;
 		try {
 			scripts = findScripts(search, PlatformUI.getWorkbench().getProgressService());
 		} catch (InterruptedException e) {
 			return;
 		} catch (CoreException e) {
 			MessageDialog.openError(getShell(), LaunchingMessages.ScriptLaunchShortcut_0, e.getMessage());
 			return;
 		}
 		IResource script = null;
 		if (scripts.length == 0) {
 			MessageDialog.openError(getShell(), LaunchingMessages.ScriptLaunchShortcut_1, emptyMessage);
 		} else if (scripts.length > 1) {
 			try {
 				script = chooseScript(scripts, selectMessage);
 			} catch (ModelException e) {
 				reportErorr(e);
 				return;
 			}
 		} else {
 			script = scripts[0];
 		}
 		if (script != null) {
 			launch(script, mode);
 		}
 	}
 
 	/**
 	 * Prompts the user to select a type from the given types.
 	 *
 	 * @param types the types to choose from
 	 * @param title the selection dialog title
 	 *
 	 * @return the selected type or <code>null</code> if none.
 	 */
 	protected IResource chooseScript(IResource[] scripts, String title) throws ModelException {
 		ElementListSelectionDialog dialog = new ElementListSelectionDialog (getShell(), new WorkbenchLabelProvider() );
 		dialog.setElements(scripts);
 		dialog.setMessage(LaunchingMessages.ScriptLaunchShortcut_Choose_a_main_script_to_launch);
 		dialog.setTitle(title);
 		if (dialog.open() == Window.OK) {
 			return (IResource)dialog.getResult()[0];
 		}
 		return null;
 	}
 
 	/**
 	 * Opens an error dialog on the given excpetion.
 	 *
 	 * @param exception
 	 */
 	protected void reportErorr(CoreException exception) {
 		MessageDialog.openError(getShell(), LaunchingMessages.ScriptLaunchShortcut_3, exception.getStatus().getMessage());
 	}
 
 	public void launch(IEditorPart editor, String mode) {
 		IEditorInput editorInput = editor.getEditorInput();
 		if (editorInput == null)
 			return;
 		IResource script = ((IFileEditorInput) editorInput).getFile();
 		if (script != null)
 			launch(script, mode);
 	}
 
 	protected void launch (IResource script, String mode) {
 		ILaunchConfiguration config = findLaunchConfiguration(script, getConfigurationType());
 		if (config != null) {
 			DebugUITools.launch(config, mode);
 		}
 	}
 
 	protected ILaunchManager getLaunchManager() {
 		return DebugPlugin.getDefault().getLaunchManager();
 	}
 
 	/**
 	 * Returns the type of configuration this shortcut is applicable to.
 	 *
 	 * @return the type of configuration this shortcut is applicable to
 	 */
 	protected abstract ILaunchConfigurationType getConfigurationType();
 
 	/**
 	 * Locate a configuration to relaunch for the given type.  If one cannot be found, create one.
 	 *
 	 * @return a re-useable config or <code>null</code> if none
 	 */
 	protected ILaunchConfiguration findLaunchConfiguration(IResource script, ILaunchConfigurationType configType) {
 		List candidateConfigs = Collections.EMPTY_LIST;
 		try {
 			ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(configType);
 			candidateConfigs = new ArrayList(configs.length);
 			for (int i = 0; i < configs.length; i++) {
 				ILaunchConfiguration config = configs[i];
				if (config.getAttribute(IDLTKLaunchConfigurationConstants.ATTR_MAIN_SCRIPT_NAME, "").equals(script.getProjectRelativePath().toString())) { //$NON-NLS-1$
						candidateConfigs.add(config);
 				}
 			}
 		} catch (CoreException e) {
 			DLTKLaunchingPlugin.log(e);
 		}
 
 		// If there are no existing configs associated with the script, create one.
 		// If there is exactly one config associated with the script, return it.
 		// Otherwise, if there is more than one config associated with the script, prompt the
 		// user to choose one.
 		int candidateCount = candidateConfigs.size();
 		if (candidateCount < 1) {
 			return createConfiguration(script);
 		} else if (candidateCount == 1) {
 			return (ILaunchConfiguration) candidateConfigs.get(0);
 		} else {
 			// Prompt the user to choose a config.  A null result means the user
 			// cancelled the dialog, in which case this method returns null,
 			// since cancelling the dialog should also cancel launching anything.
 			ILaunchConfiguration config = chooseConfiguration(candidateConfigs);
 			if (config != null) {
 				return config;
 			}
 		}
 
 		return null;
 	}
 
     protected abstract String getNature();
 
     protected ILaunchConfiguration createConfiguration(IResource script) {
         ILaunchConfiguration config = null;
         ILaunchConfigurationWorkingCopy wc = null;
         try {
             ILaunchConfigurationType configType = getConfigurationType();
             wc = configType.newInstance(null, getLaunchManager().generateUniqueLaunchConfigurationNameFrom(script.getName()));
             wc.setAttribute(IDLTKLaunchConfigurationConstants.ATTR_NATURE, getNature());
             wc.setAttribute(IDLTKLaunchConfigurationConstants.ATTR_PROJECT_NAME, script.getProject().getName());
             wc.setAttribute(IDLTKLaunchConfigurationConstants.ATTR_MAIN_SCRIPT_NAME, script.getProjectRelativePath().toPortableString()/*script.getFullPath().toPortableString()*/);
             wc.setMappedResources(new IResource[] {script.getProject()});
             config = wc.doSave();
         } catch (CoreException exception) {
             exception.printStackTrace();
         }
         return config;
     }
 
 
 	/**
 	 * Convenience method to get the window that owns this action's Shell.
 	 */
 	protected Shell getShell() {
 		return DLTKDebugUIPlugin.getActiveWorkbenchShell();
 	}
 
 
 	/**
 	 * Show a selection dialog that allows the user to choose one of the specified
 	 * launch configurations.  Return the chosen config, or <code>null</code> if the
 	 * user cancelled the dialog.
 	 */
 	protected ILaunchConfiguration chooseConfiguration(List configList) {
 		IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
 		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
 		dialog.setElements(configList.toArray());
 		dialog.setTitle(DLTKLaunchMessages.scriptLaunchShortcut2_title);
 		dialog.setMessage(DLTKLaunchMessages.scriptLaunchShortcut2);
 		dialog.setMultipleSelection(false);
 		int result = dialog.open();
 		labelProvider.dispose();
 		if (result == Window.OK) {
 			return (ILaunchConfiguration) dialog.getFirstResult();
 		}
 		return null;
 	}
 
 
 	/**
 	 * Returns the model elements corresponding to the given objects.
 	 *
 	 * @param objects selected objects
 	 * @return corresponding Script elements
 	 */
 	private IResource[] getScriptResources(Object[] objects, IProgressMonitor pm) {
 		List list= new ArrayList(objects.length);
 		for (int i = 0; i < objects.length; i++) {
 			Object object = objects[i];
 			try {
 				if (object instanceof IFile) {
 					IFile f = (IFile)object;
 					if (!f.getName().startsWith("."))
 						list.add(object);
 				} else
 				if (object instanceof IContainer) {
 					IContainer f = (IContainer)object;
 					IResource mem[] = f.members();
 					IResource res[] = getScriptResources(mem, pm);
 					for (int j = 0; j < res.length; j++) {
 						list.add(res[j]);
 					}
 				} else
 				if (object instanceof IModelElement) {
 					IModelElement elem = (IModelElement) object;
 					if (elem instanceof ISourceModule) {
 						IResource res = ((ISourceModule)elem).getCorrespondingResource();
 						if (res != null)
 							list.add(res);
 					}
 					else if (elem instanceof IParent) {
 						IParent proj = (IParent)elem;
 						IResource res[] = getScriptResources(proj.getChildren(), pm);
 						for (int j = 0; j < res.length; j++) {
 							list.add(res[j]);
 						}
 					}
 	 			}
 			} catch (CoreException e) {
 			}
 		}
 		return (IResource[]) list.toArray(new IResource[list.size()]);
 	}
 
 	/**
 	 * Finds and returns the launchable scripts in the given selection of elements.
 	 *
 	 * @param elements scope to search for launchable types
 	 * @param context progess reporting context
 	 * @return launchable types, possibly empty
 	 * @exception InterruptedException if the search is cancelled
 	 * @exception org.eclipse.core.runtime.CoreException if the search fails
 	 */
 	protected IResource[] findScripts(final Object[] elements, IRunnableContext context) throws InterruptedException, CoreException {
 		try {
 			final IResource[][] res= new IResource[1][];
 
 			IRunnableWithProgress runnable= new IRunnableWithProgress() {
 				public void run(IProgressMonitor pm) throws InvocationTargetException {
 					pm.beginTask(LaunchingMessages.LaunchShortcut_searchingForScripts, 1);
 					res[0] = getScriptResources(elements, pm);
 					pm.done();
 				}
 			};
 			context.run(true, true, runnable);
 
 			return res[0];
 		} catch (InvocationTargetException e) {
 			throw (CoreException)e.getTargetException();
 		}
 	}
 
 	/**
 	 * Returns the title for type selection dialog for this launch shortcut.
 	 *
 	 * @return type selection dialog title
 	 */
 	protected String getScriptSelectionTitle() {
 		return LaunchingMessages.LaunchShortcut_selectScriptToLaunch;
 	}
 
 	/**
 	 * Returns an error message to use when the selection does not contain a launchable type.
 	 *
 	 * @return error message
 	 */
 	protected String getSelectionEmptyMessage() {
 		return LaunchingMessages.LaunchShortcut_selectionContainsNoScript;
 	}
 
 
 }
