 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.debug.ui;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.core.model.ISourceLocator;
 import org.eclipse.debug.ui.IDebugUIConstants;
 import org.eclipse.debug.ui.ILaunchConfigurationTab;
 import org.eclipse.dltk.debug.core.DLTKDebugPlugin;
 import org.eclipse.dltk.debug.core.model.IScriptStackFrame;
 import org.eclipse.dltk.debug.core.model.IScriptVariable;
 import org.eclipse.dltk.debug.core.model.ISourceOffsetLookup;
 import org.eclipse.dltk.internal.debug.core.model.HotCodeReplaceManager;
 import org.eclipse.dltk.internal.debug.ui.ScriptDebugOptionsManager;
 import org.eclipse.dltk.internal.debug.ui.ScriptHotCodeReplaceListener;
 import org.eclipse.dltk.internal.debug.ui.log.ScriptDebugLogManager;
 import org.eclipse.dltk.internal.launching.DLTKLaunchingPlugin;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.preference.IPreferenceNode;
 import org.eclipse.jface.preference.IPreferencePage;
 import org.eclipse.jface.preference.PreferenceDialog;
 import org.eclipse.jface.preference.PreferenceManager;
 import org.eclipse.jface.preference.PreferenceNode;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.custom.BusyIndicator;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.console.ConsolePlugin;
 import org.eclipse.ui.console.IConsoleListener;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.eclipse.ui.texteditor.IDocumentProvider;
 import org.osgi.framework.BundleContext;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class DLTKDebugUIPlugin extends AbstractUIPlugin {
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "org.eclipse.dltk.debug.ui"; //$NON-NLS-1$
 
 	private ImageDescriptorRegistry fImageDescriptorRegistry;
 
 	// The shared instance
 	private static DLTKDebugUIPlugin plugin;
 
 	// Map of InterpreterInstallTypeIDs to IConfigurationElements
 	protected Map fInterpreterInstallTypePageMap;
 
 	/**
 	 * Whether this plugin is in the process of shutting down.
 	 */
 	private boolean fShuttingDown = false;
 
 	private ScriptHotCodeReplaceListener fHCRListener;
 
 	private HashMap fPresentations = new HashMap();
 
 	// private Object fUtilPresentation;
 
 	/**
 	 * Returns the image descriptor registry used for this plugin.
 	 */
 	public static ImageDescriptorRegistry getImageDescriptorRegistry() {
 		if (getDefault().fImageDescriptorRegistry == null) {
 			getDefault().fImageDescriptorRegistry = new ImageDescriptorRegistry();
 		}
 		return getDefault().fImageDescriptorRegistry;
 	}
 
 	public DLTKDebugUIPlugin() {
 		plugin = this;
 	}
 
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 
 		ILaunchManager launchManager = DebugPlugin.getDefault()
 				.getLaunchManager();
 		launchManager.addLaunchListener(DebugConsoleManager.getInstance());
 		launchManager.addLaunchListener(ScriptDebugLogManager.getInstance());
 
 		Platform.getAdapterManager().registerAdapters(
 				ScriptDebugElementAdapterFactory.getInstance(),
 				IScriptVariable.class);
 
 		ScriptDebugOptionsManager.getDefault().startup();
 
 		// Special listener that prints command line on the console
 		// TODO: add user preferences
 		ConsolePlugin.getDefault().getConsoleManager().addConsoleListener(
 				new IConsoleListener() {
 					public void consolesAdded(
 							org.eclipse.ui.console.IConsole[] consoles) {
 						for (int i = 0; i < consoles.length; ++i) {
 							if (consoles[i] instanceof org.eclipse.debug.ui.console.IConsole) {
 								org.eclipse.debug.ui.console.IConsole console = (org.eclipse.debug.ui.console.IConsole) consoles[i];
 								org.eclipse.ui.console.IOConsoleOutputStream stream = console
 										.getStream(IDebugUIConstants.ID_STANDARD_OUTPUT_STREAM);
 								if (stream != null) {
 									String cmdLine = console
 											.getProcess()
 											.getLaunch()
 											.getAttribute(
 													DLTKLaunchingPlugin.LAUNCH_COMMAND_LINE);
 
 									if (cmdLine != null) {
 
 										// try { stream.write(cmdLine.trim());
 										// stream.write("\n"); stream.flush(); }
 										// catch (IOException e) {
 										// DLTKDebugUIPlugin.log(e); }
 
 									}
 								}
 							}
 						}
 					}
 
 					public void consolesRemoved(
 							org.eclipse.ui.console.IConsole[] consoles) {
 					}
 				});
 
 		fHCRListener = new ScriptHotCodeReplaceListener();
 		HotCodeReplaceManager.getDefault().addHotCodeReplaceListener(
 				fHCRListener);
 		DLTKDebugPlugin.setSourceOffsetRetriever(new ISourceOffsetLookup() {
 
 			public int calculateOffset(IScriptStackFrame frame, int lineNumber,
 					int column) {
 				final ILaunch launch = frame.getLaunch();
 				final ISourceLocator sourceLocator = launch.getSourceLocator();
 				final Object object = sourceLocator.getSourceElement(frame);
 				if (object instanceof IFile) {
 					final IDocumentProvider provider = DLTKUIPlugin
 							.getDocumentProvider();
 					final IDocument document = provider
 							.getDocument(new FileEditorInput((IFile) object));
 					if (document != null) {
 						try {
							if (column >= 0) {
								return document.getLineOffset(lineNumber - 1)
										+ column;
							} else {
								return document.getLineOffset(lineNumber) - 2; // -2
																				// skips
																				// also
																				// the
																				// \n
							}
 						} catch (BadLocationException e) {
 							// ignore
 						}
 					}
 				}
 				return -1;
 			}
 
 		});
 	}
 
 	public void stop(BundleContext context) throws Exception {
 		try {
 			DLTKDebugPlugin.setSourceOffsetRetriever(null);
 			HotCodeReplaceManager.getDefault().removeHotCodeReplaceListener(
 					fHCRListener);
 
 			setShuttingDown(true);
 
 			ScriptDebugOptionsManager.getDefault().shutdown();
 			ScriptDebugElementAdapterFactory.getInstance().dispose();
 
 			ILaunchManager launchManager = DebugPlugin.getDefault()
 					.getLaunchManager();
 			launchManager.removeLaunchListener(DebugConsoleManager
 					.getInstance());
 			launchManager.removeLaunchListener(ScriptDebugLogManager
 					.getInstance());
 		} finally {
 			super.stop(context);
 		}
 	}
 
 	/**
 	 * Returns whether this plug-in is in the process of being shutdown.
 	 * 
 	 * @return whether this plug-in is in the process of being shutdown
 	 */
 	public boolean isShuttingDown() {
 		return fShuttingDown;
 	}
 
 	private void setShuttingDown(boolean value) {
 		this.fShuttingDown = value;
 	}
 
 	/**
 	 * Returns the shared instance
 	 * 
 	 * @return the shared instance
 	 */
 	public static DLTKDebugUIPlugin getDefault() {
 		return plugin;
 	}
 
 	// UI
 	/**
 	 * Returns the active workbench window
 	 * 
 	 * @return the active workbench window
 	 */
 	public static IWorkbenchWindow getActiveWorkbenchWindow() {
 		return getDefault().getWorkbench().getActiveWorkbenchWindow();
 	}
 
 	/**
 	 * Returns the active workbench shell or <code>null</code> if none
 	 * 
 	 * @return the active workbench shell or <code>null</code> if none
 	 */
 	public static Shell getActiveWorkbenchShell() {
 		IWorkbenchWindow window = getActiveWorkbenchWindow();
 		if (window != null) {
 			return window.getShell();
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the standard display to be used. The method first checks, if the
 	 * thread calling this method has an associated display. If so, this display
 	 * is returned. Otherwise the method returns the default display.
 	 */
 	public static Display getStandardDisplay() {
 		return DLTKUIPlugin.getStandardDisplay();
 	}
 
 	/**
 	 * Convenience method which returns the unique identifier of this plugin.
 	 */
 	public static String getUniqueIdentifier() {
 		return PLUGIN_ID;
 	}
 
 	/**
 	 * Displays the given preference page.
 	 * 
 	 * @param id
 	 *            pref page id
 	 * @param page
 	 *            pref page
 	 */
 	public static void showPreferencePage(String id, IPreferencePage page) {
 		final IPreferenceNode targetNode = new PreferenceNode(id, page);
 
 		PreferenceManager manager = new PreferenceManager();
 		manager.addToRoot(targetNode);
 		final PreferenceDialog dialog = new PreferenceDialog(DLTKDebugUIPlugin
 				.getActiveWorkbenchShell(), manager);
 		final boolean[] result = new boolean[] { false };
 		BusyIndicator.showWhile(DLTKDebugUIPlugin.getStandardDisplay(),
 				new Runnable() {
 					public void run() {
 						dialog.create();
 						dialog.setMessage(targetNode.getLabelText());
 						result[0] = (dialog.open() == Window.OK);
 					}
 				});
 	}
 
 	/**
 	 * Logs the specified status with this plug-in's log.
 	 * 
 	 * @param status
 	 *            status to log
 	 */
 	public static void log(IStatus status) {
 		getDefault().getLog().log(status);
 	}
 
 	/**
 	 * Logs an internal error with the specified message.
 	 * 
 	 * @param message
 	 *            the error message to log
 	 */
 	public static void logErrorMessage(String message) {
 		log(new Status(IStatus.ERROR, getUniqueIdentifier(),
 				IDLTKDebugUIConstants.INTERNAL_ERROR, message, null));
 	}
 
 	/**
 	 * Logs an internal error with the specified throwable
 	 * 
 	 * @param e
 	 *            the exception to be logged
 	 */
 	public static void log(Throwable e) {
 		log(new Status(IStatus.ERROR, getUniqueIdentifier(),
 				IDLTKDebugUIConstants.INTERNAL_ERROR,
 				Messages.DLTKDebugUIPlugin_internalError, e));
 	}
 
 	public static void errorDialog(String message, IStatus status) {
 		log(status);
 		Shell shell = getActiveWorkbenchShell();
 		if (shell != null) {
 			ErrorDialog.openError(shell,
 					"DebugUIM0essages.JDIDebugUIPlugin_Error_1", //$NON-NLS-1$
 					message, status);
 		}
 	}
 
 	/**
 	 * Utility method with conventions
 	 */
 	public static void errorDialog(String message, Throwable t) {
 		log(t);
 		Shell shell = getActiveWorkbenchShell();
 		if (shell != null) {
 			IStatus status = new Status(IStatus.ERROR, getUniqueIdentifier(),
 					IDLTKDebugUIConstants.INTERNAL_ERROR,
 					"Error logged from DLTK Debug UI: ", t); //$NON-NLS-1$	
 			ErrorDialog.openError(shell,
 					"DebugUIMessages.JDIDebugUIPlugin_Error_1", //$NON-NLS-1$
 					message, status);
 		}
 	}
 
 	/**
 	 * Return an object that implements <code>ILaunchConfigurationTab</code> for
 	 * the specified Interpreter install type ID.
 	 */
 	public ILaunchConfigurationTab getInterpreterInstallTypePage(
 			String InterpreterInstallTypeID) {
 		if (fInterpreterInstallTypePageMap == null) {
 			initializeInterpreterInstallTypePageMap();
 		}
 		IConfigurationElement configElement = (IConfigurationElement) fInterpreterInstallTypePageMap
 				.get(InterpreterInstallTypeID);
 		ILaunchConfigurationTab tab = null;
 		if (configElement != null) {
 			try {
 				tab = (ILaunchConfigurationTab) configElement
 						.createExecutableExtension("class"); //$NON-NLS-1$
 			} catch (CoreException ce) {
 				log(new Status(
 						IStatus.ERROR,
 						getUniqueIdentifier(),
 						IDLTKDebugUIConstants.INTERNAL_ERROR,
 						"DebugUIMessages.JDIDebugUIPlugin_An_error_occurred_retrieving_a_InterpreterInstallType_page_1", //$NON-NLS-1$
 						ce));
 			}
 		}
 		return tab;
 	}
 
 	protected void initializeInterpreterInstallTypePageMap() {
 		fInterpreterInstallTypePageMap = new HashMap(10);
 
 		IExtensionPoint extensionPoint = Platform
 				.getExtensionRegistry()
 				.getExtensionPoint(
 						getUniqueIdentifier(),
 						IDLTKDebugUIConstants.EXTENSION_POINT_INTERPRETER_INSTALL_TYPE_PAGE);
 		IConfigurationElement[] infos = extensionPoint
 				.getConfigurationElements();
 		for (int i = 0; i < infos.length; i++) {
 			String id = infos[i].getAttribute("interpreterInstallTypeID"); //$NON-NLS-1$
 			fInterpreterInstallTypePageMap.put(id, infos[i]);
 		}
 	}
 
 	public static IWorkbenchPage getActivePage() {
 		IWorkbenchWindow w = getActiveWorkbenchWindow();
 		if (w != null) {
 			return w.getActivePage();
 		}
 		return null;
 	}
 
 	private static ScriptDebugModelPresentation loadDebugModelPresentation(
 			String modelId) {
 		IExtensionPoint point = Platform.getExtensionRegistry()
 				.getExtensionPoint(IDebugUIConstants.PLUGIN_ID,
 						IDebugUIConstants.ID_DEBUG_MODEL_PRESENTATION);
 		if (point != null) {
 			IExtension[] extensions = point.getExtensions();
 			for (int i = 0; i < extensions.length; i++) {
 				IExtension extension = extensions[i];
 				IConfigurationElement[] configElements = extension
 						.getConfigurationElements();
 				for (int j = 0; j < configElements.length; j++) {
 					IConfigurationElement elt = configElements[j];
 					String id = elt.getAttribute("id"); //$NON-NLS-1$
 					if (id != null && id.equals(modelId)) {
 						try {
 							return (ScriptDebugModelPresentation) elt
 									.createExecutableExtension("class"); //$NON-NLS-1$
 						} catch (CoreException e) {
 							DLTKDebugUIPlugin.log(e);
 						}
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	public synchronized ScriptDebugModelPresentation getModelPresentation(
 			String modelId) {
 		if (!fPresentations.containsKey(modelId)) {
 			fPresentations.put(modelId, loadDebugModelPresentation(modelId));
 		}
 		return (ScriptDebugModelPresentation) fPresentations.get(modelId);
 	}
 }
