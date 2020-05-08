 /**
  *  author:crazyfarmer.cn@gmail.com 
  * 
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.github.anyview.popup.actions;
 
 import java.io.File;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
 import org.eclipse.jdt.internal.core.PackageFragment;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IActionDelegate;
 import org.eclipse.ui.IObjectActionDelegate;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.IWorkbenchWindowActionDelegate;
 
 import com.github.anyview.Activator;
 import com.github.anyview.dialogs.ExceptionMessageDialog;
 import com.github.anyview.utils.AnyViewHelper;
 
 public abstract class BaseViewAction implements IObjectActionDelegate,
 		IWorkbenchWindowActionDelegate {
 
 	private static final String ANY_VIEW = "AnyView";
 
 	private final String UNKNOWN_SELECTED = "unknown";
 
 	private Object selectedObject = UNKNOWN_SELECTED;
 
 	private Class selectedClass = null;
 
 	protected String commandAction;
 
 	/**
 	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
 	 */
 	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
 	}
 
 	@Override
 	public void run(IAction action) {
 		initCommandAction();
 		executeCommandAction();
 	}
 
 	public abstract void initCommandAction();
 
 	public void executeCommandAction() {
 		if (!isActionAvailable()) {
 			return;
 		}
 		createCommandAction();
 		runCommandAction();
 
 	}
 
 	private void runCommandAction() {
 		try {
 			Runtime.getRuntime().exec(commandAction);
 		} catch (Throwable t) {
 			String msg = "Unable to execute " + commandAction;
 			ExceptionMessageDialog.openError(new Shell(), ANY_VIEW, msg, t);
 			Activator.log(t);
 		}
 	}
 
 	private void createCommandAction() {
 		File directory = null;
 		if (selectedObject instanceof IResource) {
 			directory = new File(((IResource) selectedObject).getLocation()
 					.toOSString());
 		} else if (selectedObject instanceof File) {
 			directory = (File) selectedObject;
 		}
 		if (selectedObject instanceof IFile) {
 			directory = directory.getParentFile();
 		}
 		if (selectedObject instanceof File) {
 			directory = directory.getParentFile();
 		}
		commandAction = commandAction.trim() + directory.toString();
 	}
 
 	private boolean isActionAvailable() {
 		boolean isAvailable = true;
 		if (!AnyViewHelper.isSupported()) {
 			String msg = "This platform (" + System.getProperty("os.name")
 					+ ") is currently unsupported.\n";
 			ExceptionMessageDialog
 					.openConfirm(new Shell(), ANY_VIEW, msg, null);
 			isAvailable = false;
 		} else if (UNKNOWN_SELECTED.equals(selectedObject)) {
 			String msg = "Unable to run command " + selectedClass.getName();
 			ExceptionMessageDialog
 					.openConfirm(new Shell(), ANY_VIEW, msg, null);
 			Activator.log(msg);
 			isAvailable = false;
 		}
 		return isAvailable;
 
 	}
 
 	/**
 	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
 	 */
 	public void selectionChanged(IAction action, ISelection selection) {
 		try {
 			IAdaptable adaptable = null;
 			if (selection instanceof IStructuredSelection) {
 				adaptable = (IAdaptable) ((IStructuredSelection) selection)
 						.getFirstElement();
 				this.selectedClass = adaptable.getClass();
 				if (adaptable instanceof IResource) {
 					this.selectedObject = (IResource) adaptable;
 				} else if (adaptable instanceof PackageFragment
 						&& ((PackageFragment) adaptable)
 								.getPackageFragmentRoot() instanceof JarPackageFragmentRoot) {
 					this.selectedObject = getJarFile(((PackageFragment) adaptable)
 							.getPackageFragmentRoot());
 				} else if (adaptable instanceof JarPackageFragmentRoot) {
 					this.selectedObject = getJarFile(adaptable);
 				} else {
 					this.selectedObject = (IResource) adaptable
 							.getAdapter(IResource.class);
 				}
 			}
 		} catch (Throwable e) {
 			String msg = "error occur while select change happen";
 			ExceptionMessageDialog.openError(new Shell(), ANY_VIEW, msg, e);
 			Activator.log(e);
 		}
 	}
 
 	protected File getJarFile(IAdaptable adaptable) {
 		JarPackageFragmentRoot jpfr = (JarPackageFragmentRoot) adaptable;
 		File selected = (File) jpfr.getPath().makeAbsolute().toFile();
 		if (!((File) selected).exists()) {
 			File projectFile = new File(jpfr.getJavaProject().getProject()
 					.getLocation().toOSString());
 			selected = new File(projectFile.getParent() + selected.toString());
 		}
 		return selected;
 	}
 
 	public void dispose() {
 
 	}
 
 	public void init(IWorkbenchWindow workbenchWindow) {
 
 	}
 
 }
