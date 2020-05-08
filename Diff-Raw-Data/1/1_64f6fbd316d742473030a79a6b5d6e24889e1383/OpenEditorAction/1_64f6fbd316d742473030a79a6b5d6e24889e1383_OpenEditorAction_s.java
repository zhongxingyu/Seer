 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.dltk.internal.testing.ui;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IMember;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.internal.testing.MemberResolverManager;
 import org.eclipse.dltk.launching.ScriptLaunchConfigurationConstants;
 import org.eclipse.dltk.testing.IDLTKTestingConstants;
 import org.eclipse.dltk.testing.ITestingElementResolver;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.texteditor.ITextEditor;
 
 /**
  * Abstract Action for opening a Java editor.
  */
 public abstract class OpenEditorAction extends Action {
 	protected String fName;
 	protected TestRunnerViewPart fTestRunner;
 	private final boolean fActivate;
 
 	protected OpenEditorAction(TestRunnerViewPart testRunner,
 			String testClassName) {
 		this(testRunner, testClassName, true);
 	}
 
 	public OpenEditorAction(TestRunnerViewPart testRunner, String className,
 			boolean activate) {
 		super(DLTKTestingMessages.OpenEditorAction_action_label);
 		fName = className;
 		fTestRunner = testRunner;
 		fActivate = activate;
 	}
 
 	/*
 	 * @see IAction#run()
 	 */
 	public void run() {
 		ITextEditor textEditor = null;
 		try {
 			IModelElement element = findMember(getLaunchedProject(), fName);
 			if (element == null) {
 				MessageDialog
 						.openError(
 								getShell(),
 								DLTKTestingMessages.OpenEditorAction_error_cannotopen_title,
 								DLTKTestingMessages.OpenEditorAction_error_cannotopen_message);
 				return;
 			}
 			textEditor = (ITextEditor) DLTKUIPlugin.openInEditor(element,
 					fActivate, false);
 		} catch (CoreException e) {
 			ErrorDialog.openError(getShell(),
 					DLTKTestingMessages.OpenEditorAction_error_dialog_title,
 					DLTKTestingMessages.OpenEditorAction_error_dialog_message,
 					e.getStatus());
 			return;
 		}
 		if (textEditor == null) {
 			fTestRunner
 					.registerInfoMessage(DLTKTestingMessages.OpenEditorAction_message_cannotopen);
 			return;
 		}
 		reveal(textEditor);
 	}
 
 	protected Shell getShell() {
 		return fTestRunner.getSite().getShell();
 	}
 
 	/**
 	 * @return the Java project, or <code>null</code>
 	 */
 	protected IScriptProject getLaunchedProject() {
 		return fTestRunner.getLaunchedProject();
 	}
 
 	protected String getClassName() {
 		return fName;
 	}
 
 	protected IModelElement findMember(IScriptProject project, String name)
 			throws ModelException {
 		IScriptProject launchedProject = fTestRunner.getLaunchedProject();
 		ILaunch launch = fTestRunner.getLaunch();
 		ILaunchConfiguration launchConfiguration = launch
 				.getLaunchConfiguration();
 		IModelElement element = null;
 		String id = null;
 		try {
 			id = launchConfiguration.getAttribute(
 					IDLTKTestingConstants.ENGINE_ID_ATR, "");
 		} catch (CoreException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		}
 		if (id != null) {
 			ITestingElementResolver resolver = MemberResolverManager.getResolver(id);
 			if (resolver == null) {
 				return element;
 			}
 			ISourceModule module = resolveSourceModule(launchedProject,
 					launchConfiguration);
 			element = resolver.resolveElement(launchedProject,
 					launchConfiguration, module, name);
 			if (element == null) {
 				String title = DLTKTestingMessages.OpenTestAction_error_title;
 				String message = "Error";
 				MessageDialog.openInformation(getShell(), title, message);
 				return element;
 			}
 		}
 		return element;
 	}
 
 	protected ISourceModule resolveSourceModule(IScriptProject launchedProject,
 			ILaunchConfiguration launchConfiguration) {
 		String scriptName;
 		try {
 			scriptName = launchConfiguration.getAttribute(
 					ScriptLaunchConfigurationConstants.ATTR_MAIN_SCRIPT_NAME,
 					(String) null);
 		} catch (CoreException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 		IProject prj = launchedProject.getProject();
 		IResource file = prj.findMember(new Path(scriptName));
 		if (file instanceof IFile) {
 			return (ISourceModule) DLTKCore.create(file);
 		}
 		return null;
 	}
 
 	protected abstract void reveal(ITextEditor editor);
 }
