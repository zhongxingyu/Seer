 /**
  * <copyright>
  *
  * Copyright (c) 2010 Springsite BV (The Netherlands) and others
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Martin Taal - Initial API and implementation
  *
  * </copyright>
  *
  * $Id: BaseGenerateAction.java,v 1.6 2011/08/25 12:36:19 mtaal Exp $
  */
 package org.eclipse.emf.texo.eclipse.popup.actions;
 
 import java.lang.reflect.InvocationTargetException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.texo.eclipse.Messages;
 import org.eclipse.emf.texo.eclipse.TexoEclipsePlugin;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IActionDelegate;
 import org.eclipse.ui.IObjectActionDelegate;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.WorkspaceModifyOperation;
 
 /**
  * Base class which contains generic methods for reading selected model files and translate them to epackages.
  * 
  * @author mtaal
  */
 public abstract class BaseGenerateAction implements IObjectActionDelegate {
 
   private List<IFile> modelFiles = new ArrayList<IFile>();
 
   /**
    * Constructor for Action1.
    */
   public BaseGenerateAction() {
     super();
   }
 
   /**
    * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
    */
   public void setActivePart(IAction action, IWorkbenchPart targetPart) {
   }
 
   /**
    * @see IActionDelegate#run(IAction)
    */
   public void run(IAction action) {
     WorkspaceModifyOperation runnable = new WorkspaceModifyOperation() {
       @Override
       public void execute(IProgressMonitor progressMonitor) {
         generate(progressMonitor);
       }
     };
     try {
       PlatformUI.getWorkbench().getProgressService().run(true, true, runnable);
     } catch (Exception e) {
       if (e instanceof InvocationTargetException) {
         showError(((InvocationTargetException) e).getTargetException());
       } else {
         showError(e);
       }
       throw new IllegalStateException(e);
     }
   }
 
   public void generate(IProgressMonitor monitor) {
     // sort the modelfiles by project and run by project
     final Map<IProject, List<IFile>> filesByProject = new HashMap<IProject, List<IFile>>();
     for (IFile modelFile : modelFiles) {
       final IProject project = modelFile.getProject();
       if (filesByProject.get(project) == null) {
         filesByProject.put(project, new ArrayList<IFile>());
       }
       filesByProject.get(project).add(modelFile);
     }
 
     try {
       monitor.setTaskName(Messages.getString("generate.Artifacts")); //$NON-NLS-1$
       //    monitor.beginTask(Messages.getString("generate.Artifacts"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
       for (IProject project : filesByProject.keySet()) {
         project.refreshLocal(IResource.DEPTH_INFINITE, new org.eclipse.core.runtime.NullProgressMonitor());
 
         generate(monitor, project, filesByProject.get(project));
         monitor.subTask(Messages.getString("generate.Refresh")); //$NON-NLS-1$
 
         project.refreshLocal(IResource.DEPTH_INFINITE, new org.eclipse.core.runtime.NullProgressMonitor());
 
         if (monitor.isCanceled()) {
           return;
         }
       }
     } catch (CoreException e) {
       throw new IllegalStateException(e);
     }
   }
 
   protected void generate(IProgressMonitor monitor, IProject project, List<IFile> projectModelFiles) {
     monitor.subTask(Messages.getString("generate.Reading")); //$NON-NLS-1$
     final List<URI> uris = new ArrayList<URI>();
     for (final IFile modelFile : modelFiles) {
      uris.add(modelFile.getLocationURI());
     }
     generateFromUris(monitor, project, uris);
   }
 
   protected abstract void generateFromUris(IProgressMonitor monitor, IProject project, List<URI> uris);
 
   public static void showError(Throwable e) {
 
     // not the nicest logging...
     e.printStackTrace(System.err);
 
     IStatus status = new Status(IStatus.ERROR, TexoEclipsePlugin.PLUGIN_ID, 0,
         Messages.getString("error.click.details"), e); //$NON-NLS-1$
     ErrorDialog.openError(Display.getDefault().getActiveShell(),
         "", Messages.getString("error.during.artifact.generation"), status); //$NON-NLS-1$ //$NON-NLS-2$
   }
 
   /**
    * @see IActionDelegate#selectionChanged(IAction, ISelection)
    */
   public void selectionChanged(IAction action, ISelection selection) {
     if (!(selection instanceof IStructuredSelection)) {
       return;
     }
 
     final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
     final Iterator<?> it = structuredSelection.iterator();
     modelFiles.clear();
     while (it.hasNext()) {
       final Object obj = it.next();
       if (obj instanceof IFile) {
         modelFiles.add((IFile) obj);
       }
     }
   }
 
   public List<IFile> getModelFiles() {
     return modelFiles;
   }
 
   public void setModelFiles(List<IFile> modelFiles) {
     this.modelFiles = modelFiles;
   }
 
 }
