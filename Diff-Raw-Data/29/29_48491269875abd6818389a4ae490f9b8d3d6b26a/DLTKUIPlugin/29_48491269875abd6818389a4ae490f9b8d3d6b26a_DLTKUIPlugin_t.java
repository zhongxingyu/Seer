 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
 
  *******************************************************************************/
 package org.eclipse.dltk.ui;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.filesystem.URIUtil;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SafeRunner;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IBuffer;
 import org.eclipse.dltk.core.IExternalSourceModule;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelElementVisitor;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.IScriptFolder;
 import org.eclipse.dltk.core.IScriptModel;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.IShutdownListener;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ISourceReference;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.WorkingCopyOwner;
 import org.eclipse.dltk.core.environment.EnvironmentManager;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.internal.core.BufferManager;
 import org.eclipse.dltk.internal.core.BuiltinSourceModule;
 import org.eclipse.dltk.internal.core.ExternalProjectFragment;
 import org.eclipse.dltk.internal.launching.DLTKLaunchingPlugin;
 import org.eclipse.dltk.internal.ui.DLTKUI;
 import org.eclipse.dltk.internal.ui.DLTKUIMessages;
 import org.eclipse.dltk.internal.ui.IDLTKStatusConstants;
 import org.eclipse.dltk.internal.ui.InitializeJob;
 import org.eclipse.dltk.internal.ui.editor.DocumentAdapter;
 import org.eclipse.dltk.internal.ui.editor.EditorUtility;
import org.eclipse.dltk.internal.ui.editor.IScriptEditor;
 import org.eclipse.dltk.internal.ui.editor.ISourceModuleDocumentProvider;
 import org.eclipse.dltk.internal.ui.editor.SourceModuleDocumentProvider;
 import org.eclipse.dltk.internal.ui.editor.WorkingCopyManager;
 import org.eclipse.dltk.internal.ui.text.hover.EditorTextHoverDescriptor;
 import org.eclipse.dltk.internal.ui.viewsupport.ContextActivator;
 import org.eclipse.dltk.internal.ui.wizards.buildpath.BuildpathAttributeConfigurationDescriptors;
 import org.eclipse.dltk.launching.sourcelookup.DBGPSourceModule;
 import org.eclipse.dltk.ui.editor.saveparticipant.SaveParticipantRegistry;
 import org.eclipse.dltk.ui.text.completion.ContentAssistHistory;
 import org.eclipse.dltk.ui.viewsupport.ImageDescriptorRegistry;
 import org.eclipse.dltk.ui.viewsupport.ImagesOnFileSystemRegistry;
 import org.eclipse.dltk.ui.viewsupport.ProblemMarkerManager;
 import org.eclipse.dltk.utils.ExecutionContexts;
 import org.eclipse.dltk.utils.IExecutableOperation;
 import org.eclipse.dltk.utils.IExecutionContextManager;
 import org.eclipse.jface.action.GroupMarker;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchListener;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.editors.text.EditorsUI;
 import org.eclipse.ui.ide.FileStoreEditorInput;
 import org.eclipse.ui.navigator.ICommonMenuConstants;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
 import org.eclipse.ui.texteditor.ConfigurationElementSorter;
 import org.osgi.framework.BundleContext;
 
 /**
  * The main plugin class to be used in the desktop.
  */
 public class DLTKUIPlugin extends AbstractUIPlugin {
 
 	public static final String PLUGIN_ID = "org.eclipse.dltk.ui"; //$NON-NLS-1$
 	/**
 	 * @since 2.0
 	 */
 	public static final String ID_SCRIPT_EXPLORER = "org.eclipse.dltk.ui.ScriptExplorer"; //$NON-NLS-1$
 	@Deprecated
 	public static final String ID_SCRIPTEXPLORER = ID_SCRIPT_EXPLORER;
 	public static final String ID_TYPE_HIERARCHY = "org.eclipse.dltk.ui.TypeHierarchy"; //$NON-NLS-1$
 	/**
 	 * The preference page id of the build path variables preference page (value
 	 * 
 	 * <code>"org.eclipse.dltk.ui.preferences.BuildpathVariablesPreferencePage"</code>
 	 * ).
 	 * 
 	 */
 	public static final String ID_BUILDPATH_VARIABLES_PREFERENCE_PAGE = "org.eclipse.dltk.ui.preferences.BuildpathVariablesPreferencePage"; //$NON-NLS-1$
 	public static final String CONTEXT_VIEWS = "org.eclipse.dltk.ui.context.views"; //$NON-NLS-1$
 	// The shared instance.
 	private static DLTKUIPlugin plugin;
 
 	private MembersOrderPreferenceCache fMembersOrderPreferenceCache;
 
 	// private static ISharedImages fgSharedImages = null;
 
 	/**
 	 * Content assist history.
 	 * 
 	 * 
 	 */
 	private ContentAssistHistory fContentAssistHistory;
 
 	private BuildpathAttributeConfigurationDescriptors fBuildpathAttributeConfigurationDescriptors;
 
 	/**
 	 * The constructor.
 	 */
 	public DLTKUIPlugin() {
 		DLTKUIPlugin.plugin = this;
 	}
 
 	/**
 	 * This method is called upon plug-in activation
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 
 		WorkingCopyOwner.setPrimaryBufferProvider(new WorkingCopyOwner() {
 			public IBuffer createBuffer(ISourceModule workingCopy) {
 				ISourceModule original = workingCopy.getPrimary();
 				IResource resource = original.getResource();
 				if (resource != null) {
 					if (resource instanceof IFile) {
 						return new DocumentAdapter(workingCopy,
 								(IFile) resource);
 					}
 				} else if (original instanceof IExternalSourceModule) {
 					IProjectFragment fragment = (IProjectFragment) original
 							.getAncestor(IModelElement.PROJECT_FRAGMENT);
 					if (!fragment.isArchive()) {
 						// IPath path = original.getPath();
 						// return new DocumentAdapter(workingCopy, path);
 						return BufferManager.createBuffer(original);
 					}
 					return BufferManager.createBuffer(original);
 				}
 
 				if (original instanceof DBGPSourceModule) {
 					return BufferManager.createBuffer(original);
 				}
 
 				if (original instanceof BuiltinSourceModule) {
 					// IPath path = original.getPath();
 					// return new DocumentAdapter(workingCopy, path);
 					return BufferManager.createBuffer(original);
 				}
 				return DocumentAdapter.NULL;
 			}
 		});
 
 		// must add here to guarantee that it is the first in the listener list
 
 		IPreferenceStore store = getPreferenceStore();
 		fMembersOrderPreferenceCache = new MembersOrderPreferenceCache();
 		fMembersOrderPreferenceCache.install(store);
 
 		// to initialize launching
 		DLTKLaunchingPlugin.getDefault();
 
 		// Close all open editors which has remote environment files open
 		try {
 			PlatformUI.getWorkbench().addWorkbenchListener(
 					new ShutdownCloseRemoteEditorsListener());
 			ContextActivator.getInstance().install();
 		} catch (IllegalStateException e) {
 			// IGNORE: workbench has not been created yet.
 		}
 
 		ExecutionContexts.setManager(new UIExecutionContextManager());
 		new InitializeJob().schedule();
 	}
 
	static class ShutdownCloseRemoteEditorsListener implements
 			IWorkbenchListener {
 		public void postShutdown(IWorkbench workbench) {
 			// empty
 		}
 
		public boolean preShutdown(final IWorkbench workbench, boolean forced) {
			SafeRunner.run(new ISafeRunnable() {
				public void run() throws Exception {
					preShutdownInternal(workbench);
				}

				public void handleException(Throwable exception) {
					// Logged by SafeRunner
				}
			});
			return true;
		}

		void preShutdownInternal(final IWorkbench workbench) {
 			IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
 			if (window != null) {
 				IWorkbenchPage page = window.getActivePage();
 				if (page != null) {
 					IEditorReference[] references = page.getEditorReferences();
 					for (int i = 0; i < references.length; i++) {
 						IEditorPart editor = references[i].getEditor(false);
						if (editor != null && editor instanceof IScriptEditor) {
 							closeEditor(page, editor);
 						}
 					}
 				}
 			}
 		}
 
 		private void closeEditor(IWorkbenchPage page, IEditorPart editor) {
 			IModelElement modelElement = EditorUtility
 					.getEditorInputModelElement(editor, false);
 			if (modelElement != null) {
 				IEnvironment environment = EnvironmentManager
 						.getEnvironment(modelElement);
 				if (environment != null && !environment.isLocal()) {
 					page.closeEditor(editor, false);
 				}
 			}
 		}
 	}
 
 	private static class UIExecutionContextManager implements
 			IExecutionContextManager {
 
 		private boolean active = false;
 
 		public void executeInBackground(final IExecutableOperation operation) {
 			if (!isRunningInUIThread()) {
 				operation.execute(new NullProgressMonitor());
 			} else if (DLTKUI.isStarted()) {
 				if (active) {
 					return;
 				}
 				final ProgressMonitorDialog dialog = new ProgressMonitorDialog(
 						null) {
 					@Override
 					protected void configureShell(Shell shell) {
 						super.configureShell(shell);
 						shell.setText(operation.getOperationName());
 					}
 				};
 				active = true;
 				try {
 					dialog.run(true, false, new IRunnableWithProgress() {
 						public void run(IProgressMonitor monitor) {
 							if (!isRunningInUIThread()) {
 								operation.execute(monitor);
 							}
 						}
 					});
 				} catch (InvocationTargetException e) {
 					DLTKCore.error(e.getMessage(), e);
 				} catch (InterruptedException e) {
 					DLTKCore.error(e.getMessage(), e);
 				} finally {
 					active = false;
 				}
 			}
 		}
 
 		public boolean isRunningInUIThread() {
 			return Display.getCurrent() != null;
 		}
 
 	}
 
 	private final ListenerList shutdownListeners = new ListenerList();
 
 	public void addShutdownListener(IShutdownListener listener) {
 		shutdownListeners.add(listener);
 	}
 
 	/**
 	 * This method is called when the plug-in is stopped
 	 */
 	@Override
 	public void stop(BundleContext context) throws Exception {
 
 		ExecutionContexts.setManager(null);
 		if (fMembersOrderPreferenceCache != null) {
 			fMembersOrderPreferenceCache.dispose();
 			fMembersOrderPreferenceCache = null;
 		}
 		Object[] listeners = shutdownListeners.getListeners();
 		for (int i = 0; i < listeners.length; ++i) {
 			((IShutdownListener) listeners[i]).shutdown();
 		}
 		shutdownListeners.clear();
 		super.stop(context);
 		DLTKUIPlugin.plugin = null;
 	}
 
 	private IWorkbenchPage internalGetActivePage() {
 		final IWorkbench workbench;
 		try {
 			workbench = getWorkbench();
 		} catch (IllegalStateException e) {
 			// Workbench has not been created yet - can happen in tests
 			return null;
 		}
 		final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
 		if (window == null) {
 			return null;
 		}
 		return workbench.getActiveWorkbenchWindow().getActivePage();
 	}
 
 	/**
 	 * Returns the shared instance.
 	 */
 	public static DLTKUIPlugin getDefault() {
 		return DLTKUIPlugin.plugin;
 	}
 
 	public IDialogSettings getDialogSettingsSection(String name) {
 		IDialogSettings dialogSettings = getDialogSettings();
 		IDialogSettings section = dialogSettings.getSection(name);
 		if (section == null) {
 			section = dialogSettings.addNewSection(name);
 		}
 		return section;
 	}
 
 	public static IWorkbenchPage getActivePage() {
 		return DLTKUIPlugin.getDefault().internalGetActivePage();
 	}
 
 	public static IWorkbenchWindow getActiveWorkbenchWindow() {
 		return DLTKUIPlugin.getDefault().getWorkbench()
 				.getActiveWorkbenchWindow();
 	}
 
 	/**
 	 * Returns an image descriptor for the image file at the given plug-in
 	 * relative path.
 	 * 
 	 * @param path
 	 *            the path
 	 * @return the image descriptor
 	 */
 	public static ImageDescriptor getImageDescriptor(String path) {
 		return AbstractUIPlugin.imageDescriptorFromPlugin(
 				"org.eclipse.dltk.ui", path); //$NON-NLS-1$
 	}
 
 	private IWorkingCopyManager fWorkingCopyManager;
 	private ISourceModuleDocumentProvider fSourceModuleDocumentProvider;
 	private ProblemMarkerManager fProblemMarkerManager;
 	private ImageDescriptorRegistry fImageDescriptorRegistry;
 
 	public synchronized IWorkingCopyManager getWorkingCopyManager() {
 		if (fWorkingCopyManager == null) {
 			ISourceModuleDocumentProvider provider = getSourceModuleDocumentProvider();
 			fWorkingCopyManager = new WorkingCopyManager(provider);
 		}
 		return fWorkingCopyManager;
 	}
 
 	public synchronized ISourceModuleDocumentProvider getSourceModuleDocumentProvider() {
 
 		if (fSourceModuleDocumentProvider == null) {
 			fSourceModuleDocumentProvider = new SourceModuleDocumentProvider();
 		}
 		return fSourceModuleDocumentProvider;
 	}
 
 	public static ISourceModuleDocumentProvider getDocumentProvider() {
 		return DLTKUIPlugin.getDefault().getSourceModuleDocumentProvider();
 	}
 
 	public static ImageDescriptorRegistry getImageDescriptorRegistry() {
 		return DLTKUIPlugin.getDefault().internalGetImageDescriptorRegistry();
 	}
 
 	private ImageDescriptorRegistry internalGetImageDescriptorRegistry() {
 		if (fImageDescriptorRegistry == null) {
 			fImageDescriptorRegistry = new ImageDescriptorRegistry();
 		}
 		return fImageDescriptorRegistry;
 	}
 
 	/**
 	 * Returns the model element wrapped by the given editor input.
 	 * 
 	 * @param editorInput
 	 *            the editor input
 	 * @return the model element wrapped by <code>editorInput</code> or
 	 *         <code>null</code> if none
 	 */
 	public static ISourceModule getEditorInputModelElement(
 			IEditorInput editorInput) {
 		// Performance: check working copy manager first: this is faster
 		ISourceModule je = DLTKUIPlugin.getDefault().getWorkingCopyManager()
 				.getWorkingCopy(editorInput, false);
 		if (je != null) {
 			return je;
 		}
 
 		if (editorInput instanceof FileStoreEditorInput) {
 			ISourceModule module = resolveSourceModule((FileStoreEditorInput) editorInput);
 			if (module != null) {
 				return module;
 			}
 		}
 		IModelElement me = (IModelElement) editorInput
 				.getAdapter(IModelElement.class);
 		if (me instanceof ISourceModule) {
 			return (ISourceModule) me;
 		}
 		return null;
 	}
 
 	public static IWorkspace getWorkspace() {
 		return ResourcesPlugin.getWorkspace();
 	}
 
 	public static void log(IStatus status) {
 		DLTKUIPlugin.getDefault().getLog().log(status);
 	}
 
 	public static void log(Throwable e) {
 		DLTKUIPlugin.log(new Status(IStatus.ERROR, DLTKUIPlugin.PLUGIN_ID,
 				IDLTKStatusConstants.INTERNAL_ERROR,
 				DLTKUIMessages.ScriptPlugin_internal_error, e));
 	}
 
 	public static void logErrorMessage(String message) {
 		logErrorMessage(message, null);
 	}
 
 	public static void warn(String message) {
 		warn(message, null);
 	}
 
 	public static void warn(String message, Throwable throwable) {
 		log(new Status(IStatus.WARNING, PLUGIN_ID,
 				IDLTKStatusConstants.INTERNAL_ERROR, message, throwable));
 	}
 
 	public static void logErrorMessage(String message, Throwable throwable) {
 		DLTKUIPlugin.log(new Status(IStatus.ERROR, DLTKUIPlugin.PLUGIN_ID,
 				IDLTKStatusConstants.INTERNAL_ERROR, message, throwable));
 	}
 
 	public static void logErrorStatus(String message, IStatus status) {
 		if (status == null) {
 			DLTKUIPlugin.logErrorMessage(message);
 			return;
 		}
 		MultiStatus multi = new MultiStatus(DLTKUIPlugin.PLUGIN_ID,
 				IDLTKStatusConstants.INTERNAL_ERROR, message, null);
 		multi.add(status);
 		DLTKUIPlugin.log(multi);
 	}
 
 	public static Shell getActiveWorkbenchShell() {
 		IWorkbenchWindow window = DLTKUIPlugin.getActiveWorkbenchWindow();
 		if (window != null) {
 			return window.getShell();
 		}
 		return null;
 	}
 
 	/**
 	 * Creates the DLTK plug-in's standard groups for view context menus.
 	 * 
 	 * @param menu
 	 *            the menu manager to be populated
 	 */
 	public static void createStandardGroups(IMenuManager menu) {
 		if (!menu.isEmpty()) {
 			return;
 		}
 
 		menu.add(new Separator(IContextMenuConstants.GROUP_NEW));
 		menu.add(new GroupMarker(IContextMenuConstants.GROUP_GOTO));
 		menu.add(new Separator(IContextMenuConstants.GROUP_OPEN));
 		menu.add(new GroupMarker(IContextMenuConstants.GROUP_SHOW));
 		menu.add(new Separator(ICommonMenuConstants.GROUP_EDIT));
 		menu.add(new Separator(IContextMenuConstants.GROUP_REORGANIZE));
 		menu.add(new Separator(IContextMenuConstants.GROUP_GENERATE));
 		menu.add(new Separator(IContextMenuConstants.GROUP_SEARCH));
 		menu.add(new Separator(IContextMenuConstants.GROUP_BUILD));
 		menu.add(new Separator(IContextMenuConstants.GROUP_ADDITIONS));
 		menu.add(new Separator(IContextMenuConstants.GROUP_VIEWER_SETUP));
 		menu.add(new Separator(IContextMenuConstants.GROUP_PROPERTIES));
 	}
 
 	public synchronized MembersOrderPreferenceCache getMemberOrderPreferenceCache() {
 		// initialized on startup
 		return fMembersOrderPreferenceCache;
 	}
 
 	public static String getPluginId() {
 		return DLTKUIPlugin.PLUGIN_ID;
 	}
 
 	/**
 	 * Returns the Script content assist history.
 	 * 
 	 * @return the Script content assist history
 	 * 
 	 */
 	public ContentAssistHistory getContentAssistHistory() {
 		if (fContentAssistHistory == null) {
 			try {
 				fContentAssistHistory = ContentAssistHistory.load(
 						getPluginPreferences(),
 						PreferenceConstants.CODEASSIST_LRU_HISTORY);
 			} catch (CoreException x) {
 				DLTKUIPlugin.log(x);
 			}
 			if (fContentAssistHistory == null) {
 				fContentAssistHistory = new ContentAssistHistory();
 			}
 		}
 
 		return fContentAssistHistory;
 	}
 
 	private final Map editorTextHoverDescriptorsByNature = new HashMap();
 
 	/**
 	 * Resets editor text hovers contributed to the workbench.
 	 * <p>
 	 * This will force a rebuild of the descriptors the next time a client asks
 	 * for them.
 	 * </p>
 	 * 
 	 * @deprecated
 	 */
 	public void resetEditorTextHoverDescriptors() {
 		synchronized (editorTextHoverDescriptorsByNature) {
 			editorTextHoverDescriptorsByNature.clear();
 		}
 	}
 
 	/**
 	 * Resets editor text hovers contributed to the workbench.
 	 * <p>
 	 * This will force a rebuild of the descriptors the next time a client asks
 	 * for them.
 	 * </p>
 	 */
 	public void resetEditorTextHoverDescriptors(String natureId) {
 		synchronized (editorTextHoverDescriptorsByNature) {
 			editorTextHoverDescriptorsByNature.remove(natureId);
 		}
 	}
 
 	/**
 	 * Returns all editor text hovers contributed to the workbench without
 	 * specified nature.
 	 * 
 	 * @param store
 	 * @return
 	 * @deprecated
 	 */
 	public EditorTextHoverDescriptor[] getEditorTextHoverDescriptors(
 			IPreferenceStore store) {
 		return initializeEditorTextHoverDescriprtors(store, null);
 	}
 
 	/**
 	 * Returns all editor text hovers contributed to the workbench for the
 	 * specified nature.
 	 * 
 	 * @param store
 	 *            preference store to initialize settings from
 	 * @param natureId
 	 *            the nature to filter text hovers
 	 * @return an array of EditorTextHoverDescriptor
 	 */
 	public EditorTextHoverDescriptor[] getEditorTextHoverDescriptors(
 			IPreferenceStore store, String natureId) {
 		EditorTextHoverDescriptor[] descriptors;
 		synchronized (editorTextHoverDescriptorsByNature) {
 			descriptors = (EditorTextHoverDescriptor[]) editorTextHoverDescriptorsByNature
 					.get(natureId);
 		}
 		if (descriptors == null) {
 			descriptors = initializeEditorTextHoverDescriprtors(store, natureId);
 			if (descriptors != null && natureId != null) {
 				synchronized (editorTextHoverDescriptorsByNature) {
 					editorTextHoverDescriptorsByNature.put(natureId,
 							descriptors);
 				}
 			}
 		}
 		return descriptors;
 	}
 
 	private EditorTextHoverDescriptor[] initializeEditorTextHoverDescriprtors(
 			IPreferenceStore store, String natureId) {
 		EditorTextHoverDescriptor[] descriptors = EditorTextHoverDescriptor
 				.getContributedHovers(natureId, store);
 		ConfigurationElementSorter sorter = new ConfigurationElementSorter() {
 			/*
 			 * @see org.eclipse.ui.texteditor.ConfigurationElementSorter#
 			 * getConfigurationElement(java.lang.Object)
 			 */
 			public IConfigurationElement getConfigurationElement(Object object) {
 				return ((EditorTextHoverDescriptor) object)
 						.getConfigurationElement();
 			}
 		};
 		sorter.sort(descriptors);
 		// Move Best Match hover to front
 		for (int i = 0; i < descriptors.length - 1; i++) {
 			if (PreferenceConstants.ID_BESTMATCH_HOVER.equals(descriptors[i]
 					.getId())) {
 				final EditorTextHoverDescriptor hoverDescriptor = descriptors[i];
 				for (int j = i; j > 0; j--)
 					descriptors[j] = descriptors[j - 1];
 				descriptors[0] = hoverDescriptor;
 				break;
 			}
 		}
 		return descriptors;
 	}
 
 	/**
 	 * Opens an editor on the given Java element in the active page. Valid
 	 * elements are all Java elements that are {@link ISourceReference}. For
 	 * elements inside a compilation unit or class file, the parent is opened in
 	 * the editor is opened and the element revealed. If there already is an
 	 * open Java editor for the given element, it is returned.
 	 * 
 	 * @param element
 	 *            the input element; either a compilation unit (
 	 *            <code>ICompilationUnit</code>) or a class file (
 	 *            <code>IClassFile</code>) or source references inside.
 	 * @return returns the editor part of the opened editor or <code>null</code>
 	 *         if the element is not a {@link ISourceReference} or the file was
 	 *         opened in an external editor.
 	 * @exception PartInitException
 	 *                if the editor could not be initialized or no workbench
 	 *                page is active
 	 * @exception JavaModelException
 	 *                if this element does not exist or if an exception occurs
 	 *                while accessing its underlying resource
 	 */
 	public static IEditorPart openInEditor(IModelElement element)
 			throws ModelException, PartInitException {
 		return DLTKUIPlugin.openInEditor(element, true, true);
 	}
 
 	/**
 	 * Opens an editor on the given Java element in the active page. Valid
 	 * elements are all Java elements that are {@link ISourceReference}. For
 	 * elements inside a compilation unit or class file, the parent is opened in
 	 * the editor is opened. If there already is an open Java editor for the
 	 * given element, it is returned.
 	 * 
 	 * @param element
 	 *            the input element; either a compilation unit (
 	 *            <code>ICompilationUnit</code>) or a class file (
 	 *            <code>IClassFile</code>) or source references inside.
 	 * @param activate
 	 *            if set, the editor will be activated.
 	 * @param reveal
 	 *            if set, the element will be revealed.
 	 * @return returns the editor part of the opened editor or <code>null</code>
 	 *         if the element is not a {@link ISourceReference} or the file was
 	 *         opened in an external editor.
 	 * @exception PartInitException
 	 *                if the editor could not be initialized or no workbench
 	 *                page is active
 	 * @exception JavaModelException
 	 *                if this element does not exist or if an exception occurs
 	 *                while accessing its underlying resource
 	 * @since 3.3
 	 */
 	public static IEditorPart openInEditor(IModelElement element,
 			boolean activate, boolean reveal) throws ModelException,
 			PartInitException {
 		if (!(element instanceof ISourceReference)) {
 			return null;
 		}
 		IEditorPart part = EditorUtility.openInEditor(element, activate);
 		if (reveal && part != null) {
 			EditorUtility.revealInEditor(part, element);
 		}
 		return part;
 	}
 
 	public synchronized ProblemMarkerManager getProblemMarkerManager() {
 		if (fProblemMarkerManager == null) {
 			fProblemMarkerManager = new ProblemMarkerManager();
 		}
 		return fProblemMarkerManager;
 	}
 
 	public static boolean isDebug() {
 		return DLTKCore.DEBUG;
 	}
 
 	public BuildpathAttributeConfigurationDescriptors getClasspathAttributeConfigurationDescriptors() {
 		if (fBuildpathAttributeConfigurationDescriptors == null) {
 			fBuildpathAttributeConfigurationDescriptors = new BuildpathAttributeConfigurationDescriptors();
 		}
 		return fBuildpathAttributeConfigurationDescriptors;
 	}
 
 	public static ISourceModule resolveSourceModule(FileStoreEditorInput input) {
 		final ISourceModule[] modules = new ISourceModule[1];
 		final IPath filePath = URIUtil.toPath(input.getURI());
 		IScriptModel scriptModel = DLTKCore.create(ResourcesPlugin
 				.getWorkspace().getRoot());
 		try {
 			scriptModel.accept(new IModelElementVisitor() {
 
 				public boolean visit(IModelElement element) {
 					boolean shouldDescend = (modules[0] == null);
 
 					if (shouldDescend == true) {
 						if (element instanceof ExternalProjectFragment) {
 							ExternalProjectFragment fragment = (ExternalProjectFragment) element;
 
 							try {
 								if (filePath
 										.removeLastSegments(1)
 										.toFile()
 										.getCanonicalPath()
 										.startsWith(
 												fragment.getPath().toFile()
 														.getCanonicalPath()) == true) {
 									IPath folderPath = new Path(filePath
 											.removeLastSegments(1).toFile()
 											.getCanonicalPath());
 									folderPath = folderPath
 											.removeFirstSegments(new Path(
 													fragment.getPath().toFile()
 															.getCanonicalPath())
 													.segmentCount());
 									IScriptFolder folder = fragment
 											.getScriptFolder(folderPath);
 									if ((folder != null)
 											&& (folder.exists() == true)) {
 										ISourceModule module = folder
 												.getSourceModule(filePath
 														.lastSegment());
 										if (module != null) {
 											modules[0] = module;
 										}
 									}
 								}
 							} catch (IOException ixcn) {
 								ixcn.printStackTrace();
 							}
 
 							shouldDescend = false;
 						} else {
 							shouldDescend = ((element instanceof IScriptProject) || (element instanceof IScriptModel));
 						}
 					}
 
 					return shouldDescend;
 				}
 
 			});
 		} catch (ModelException mxcn) {
 			mxcn.printStackTrace();
 		}
 
 		return modules[0];
 	}
 
 	/**
 	 * Returns the standard display to be used. The method first checks, if the
 	 * thread calling this method has an associated display. If so, this display
 	 * is returned. Otherwise the method returns the default display.
 	 */
 	public static Display getStandardDisplay() {
 		Display display;
 		display = Display.getCurrent();
 		if (display == null)
 			display = Display.getDefault();
 		return display;
 	}
 
 	public static String getAdditionalInfoAffordanceString() {
 		if (!EditorsUI
 				.getPreferenceStore()
 				.getBoolean(
 						AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE))
 			return null;
 		return Messages.DLTKUIPlugin_additionalInfo_affordance;
 	}
 
 	/**
 	 * The save participant registry.
 	 * 
 	 * @since 3.0
 	 */
 	private SaveParticipantRegistry fSaveParticipantRegistry;
 
 	/**
 	 * Returns the save participant registry.
 	 * 
 	 * @return the save participant registry, not null
 	 * @since 3.0
 	 */
 	public synchronized SaveParticipantRegistry getSaveParticipantRegistry() {
 		if (fSaveParticipantRegistry == null)
 			fSaveParticipantRegistry = new SaveParticipantRegistry();
 		return fSaveParticipantRegistry;
 	}
 
 	private ImagesOnFileSystemRegistry fImagesOnFSRegistry;
 
 	/**
 	 * Returns the image registry that keeps its images on the local file
 	 * system.
 	 * 
 	 * @return the image registry
 	 * @since 4.0
 	 */
 	public ImagesOnFileSystemRegistry getImagesOnFSRegistry() {
 		if (fImagesOnFSRegistry == null) {
 			fImagesOnFSRegistry = new ImagesOnFileSystemRegistry();
 		}
 		return fImagesOnFSRegistry;
 	}
 }
