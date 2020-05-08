 package org.bitbucket.tsergey.vlt.utils;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.lang.StringUtils;
 import org.bitbucket.tsergey.vlt.Activator;
 import org.bitbucket.tsergey.vlt.exception.VaultException;
 import org.bitbucket.tsergey.vlt.exception.VaultException.Type;
 import org.bitbucket.tsergey.vlt.messages.Messages;
 import org.bitbucket.tsergey.vlt.preferences.GeneralPreferencesPage;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
 import org.eclipse.ui.ide.ResourceUtil;
 
 public class ResourceUtils {
 
 	public static String getResourcePath(Object o) {
 		String path = StringUtils.EMPTY;
 		if(o != null) {
 			IFile file = adaptResource(o, IFile.class);
 			if(file == null) {
 				IFolder folder = adaptResource(o, IFolder.class);
 				if(folder != null) {
 					try {
 						path = folder.getLocation().toFile().getCanonicalPath();
 					} catch (IOException e) {
 						path = StringUtils.EMPTY;
 					}
 				}
 			} else {
 				try {
 					path = file.getLocation().toFile().getCanonicalPath();
 				} catch (IOException e) {
 					path = StringUtils.EMPTY;
 				}
 			}
 		}
 		return path;
 	}
 
	public static <M> M adaptResource(Object resource, Class<M> type) {
 		M result = (M) Platform.getAdapterManager().getAdapter(resource, type);
 		if(result == null) {
 			if(resource instanceof IAdaptable) {
 				result = (M) ((IAdaptable)resource).getAdapter(type);
 			}
 		}
 		return result;
 	}
 
 	public static String getResourcePathFromEditor() {
 		String path = null;
 		IWorkbenchWindow window = getActiveWindow();
 		if(window != null) {
 			IWorkbenchPage page = window.getActivePage();
 			if(page != null) {
 				IEditorPart editorPart = page.getActiveEditor();
 				if(editorPart != null) {
 					IEditorInput editorInput = editorPart.getEditorInput();
 					IFile file = ResourceUtil.getFile(editorInput);
 					if(file != null) {
 						try {
 							path = file.getLocation().toFile().getCanonicalPath();
 						} catch (IOException e) {
 							path = StringUtils.EMPTY;
 						}
 					}
 				}
 			}
 		}
 		return path;
 	}
 
 	public static IWorkbenchWindow getActiveWindow() {
 		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 		if (window == null) {
 			window = PlatformUI.getWorkbench().getWorkbenchWindows()[0];
 		}
 		return window;
 	}
 
 	public static String initJCRRoot() {
 		String jcrRoot = Activator.getDefault().getPreferenceStore().getString(GeneralPreferencesPage.JCR_ROOT_PATH);
 		File jcrRootFile = new File(jcrRoot);
 		try {
 			jcrRoot = jcrRootFile.getCanonicalPath();
 		} catch (IOException e) {
 			throw new VaultException(Type.JCR_ROOT_CONFIG_ERROR, Messages.get(Messages.ERRORS_JCR_ROOT_CONFIG), e);
 		}
 		if(StringUtils.isBlank(jcrRoot)) {
 			throw new VaultException(Type.JCR_ROOT_CONFIG_ERROR, Messages.get(Messages.ERRORS_JCR_ROOT_CONFIG));
 		}
 		System.setProperty("user.dir", jcrRoot);
 		return jcrRoot;
 	}
 	
 	public static String retrieveSelectedPath(ExecutionEvent event, String jcrRoot) {
 		String path = StringUtils.EMPTY;
		ISelection selectedObject = HandlerUtil.getCurrentSelection(event);
 		if(selectedObject != null) {
 			if(selectedObject instanceof IStructuredSelection) {
				Object firstSelectedObject = ((IStructuredSelection)selectedObject).getFirstElement();
				path = ResourceUtils.getResourcePath(firstSelectedObject);
 			} else {
				path = ResourceUtils.getResourcePath(selectedObject);
 			}
 		}
		if(path == null) {
 			path = ResourceUtils.getResourcePathFromEditor();
 		}
 		
 		if(StringUtils.isBlank(path)) {
 			throw new VaultException(Type.NO_FILE_SELECTION_ERROR, Messages.get(Messages.ERRORS_FILE_NOT_SELECTED));
 		}
 		
 		if(StringUtils.contains(path, jcrRoot)) {
 			path = StringUtils.substring(path, StringUtils.length(jcrRoot) + 1);
 		}
 		return path;
 	}
 
 }
