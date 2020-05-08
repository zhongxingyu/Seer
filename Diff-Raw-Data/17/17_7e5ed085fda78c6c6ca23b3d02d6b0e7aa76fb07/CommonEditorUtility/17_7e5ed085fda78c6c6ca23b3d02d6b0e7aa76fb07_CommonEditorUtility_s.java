 /***************************************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: IBM Corporation - initial API and implementation
  **************************************************************************************************/
 package org.eclipse.jst.j2ee.internal.plugin;
 
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.internal.EditorManager;
 
 
 
 public class CommonEditorUtility {
 
 	public static final Class IRESOURCE_CLASS = IResource.class;
 
 	private CommonEditorUtility() {
 		super();
 	}
 
 	/**
 	 * Returns an array of all editors that have an unsaved content. If the identical content is
 	 * presented in more than one editor, only one of those editor parts is part of the result.
 	 * 
 	 * @return an array of all dirty editor parts.
 	 */
 	public static IEditorPart[] getDirtyEditors() {
 		return getDirtyEditors(null);
 	}
 
 	/**
 	 * Returns an array of all editors that have an unsaved content, where the content is include in
 	 * one of the projects in the List parameter. If the identical content is presented in more than
 	 * one editor, only one of those editor parts is part of the result.
 	 * 
 	 * @return an array of all dirty editor parts.
 	 */
 	public static IEditorPart[] getDirtyEditors(List projects) {
 		Set inputs = new HashSet();
 		List result = new ArrayList(0);
 		IWorkbench workbench = J2EEUIPlugin.getPluginWorkbench();
 		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
 		for (int i = 0; i < windows.length; i++) {
 			IWorkbenchPage[] pages = windows[i].getPages();
 			for (int x = 0; x < pages.length; x++) {
 				IEditorPart[] editors = pages[x].getDirtyEditors();
 				for (int z = 0; z < editors.length; z++) {
 					IEditorPart ep = editors[z];
 					IEditorInput input = ep.getEditorInput();
 					if (!inputs.contains(input) && (projects == null || inputInProjects(input, projects))) {
 						inputs.add(input);
 						result.add(ep);
 					}
 				}
 			}
 		}
 		return (IEditorPart[]) result.toArray(new IEditorPart[result.size()]);
 	}
 
 	public static boolean inputInProjects(IEditorInput input, List projects) {
 		IResource res = (IResource) input.getAdapter(IRESOURCE_CLASS);
 		IProject project = res == null ? null : res.getProject();
 		if (project == null)
 			return false;
 		for (int i = 0; i < projects.size(); i++) {
 			if (project.equals(projects.get(i)))
 				return true;
 		}
 		return false;
 	}
 
 	public static boolean promptToSaveAllDirtyEditors() {
 		return promptToSaveDirtyEditors(Arrays.asList(getDirtyEditors()));
 	}
 
 	public static boolean promptToSaveDirtyEditors(List dirtyEditors) {
 		if (dirtyEditors.isEmpty())
 			return true;
		return EditorManager.saveAll(dirtyEditors, true, J2EEUIPlugin.getActiveWorkbenchWindow());
 	}
 
 	public static boolean promptToSaveDirtyEditorsInProjects(List projects) {
 		return promptToSaveDirtyEditors(Arrays.asList(getDirtyEditors(projects)));
 	}
 }
