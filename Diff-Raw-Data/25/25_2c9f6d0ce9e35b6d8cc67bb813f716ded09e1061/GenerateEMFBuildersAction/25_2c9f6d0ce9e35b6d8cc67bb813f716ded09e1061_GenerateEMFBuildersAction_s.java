 /*******************************************************************************
  * Copyright (c) 2010 - 2010 Azmun project.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Azmun project committers - initial API and implementation
  * Web: http://www.azmun.de
  * Contact : siamak AT haschemi.org
  *******************************************************************************/
 package org.haschemi.utils.emfbuildergen.ui;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
 import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.mwe.core.WorkflowContext;
 import org.eclipse.emf.mwe.core.WorkflowRunner;
 import org.eclipse.emf.mwe.core.issues.Issues;
 import org.eclipse.emf.mwe.core.issues.IssuesImpl;
 import org.eclipse.emf.mwe.core.monitor.ProgressMonitor;
 import org.eclipse.emf.mwe.core.monitor.ProgressMonitorAdapter;
 import org.eclipse.emf.mwe.core.resources.ResourceLoaderFactory;
 import org.eclipse.emf.mwe.core.resources.ResourceLoaderImpl;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IObjectActionDelegate;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.part.FileEditorInput;
 import org.haschemi.utils.emfbuildergen.EMFBuilderGenerator;
 
 public class GenerateEMFBuildersAction implements IObjectActionDelegate {
   private IWorkbenchPart m_activePart;
   private ISelection m_selection;
 
   @Override
   public void run(IAction p_action) {
     Job job = new Job("Generate Builder Code") {
      
       @Override
       protected IStatus run(IProgressMonitor p_monitor) {
         if (!(m_selection instanceof IStructuredSelection)) {
           return Status.CANCEL_STATUS;
         }
         IStructuredSelection selection = (IStructuredSelection) m_selection;
         if (selection.getFirstElement() == null) {
           return Status.CANCEL_STATUS;
         }
         Object firstElement = selection.getFirstElement();
         if (!(firstElement instanceof GenModel)) {
           return Status.CANCEL_STATUS;
         }
         GenModel genModel = (GenModel) firstElement;
 
         if (!(m_activePart instanceof IEditorPart)) {
           return Status.CANCEL_STATUS;
         }
         IEditorPart editorPart = (IEditorPart) m_activePart;
 
         IEditorInput editorInput = editorPart.getEditorInput();
         if (!(editorInput instanceof FileEditorInput)) {
           return Status.CANCEL_STATUS;
         }
         FileEditorInput fileEditorInput = (FileEditorInput) editorInput;
        
         WorkflowRunner runner = new WorkflowRunner();
         WorkflowContext context = runner.getContext();
         Issues issues = new IssuesImpl();
         ProgressMonitor progressMonitor = new ProgressMonitorAdapter(p_monitor);
         EMFBuilderGenerator emfBuilderGenerator = new EMFBuilderGenerator();
 
         String ecoreFile = genModel.getForeignModel().get(0);
         IFile file = fileEditorInput.getFile();
         emfBuilderGenerator.setProjectPath(file.getProject().getLocation().toString());
 
         IResource ecoreFileResource = file.getParent().findMember(ecoreFile);
         emfBuilderGenerator.setEcoreURI(ecoreFileResource.getFullPath().toString());
 
         String targetDir = new Path(genModel.getModelDirectory()).makeRelativeTo(file.getProject().getFullPath()).toString();
         emfBuilderGenerator.setTargetDir(targetDir);
 
         GenPackage genPackage = genModel.getGenPackages().get(0);
         emfBuilderGenerator.setMetaModelPackageInstance(genPackage.getEcorePackage());
        
        String targetPackage = genPackage.getBasePackage() + "." + genPackage.getEcorePackage().getName();
         emfBuilderGenerator.setTargetPackage(targetPackage + ".util.builder");
         emfBuilderGenerator.setMetaModelFactory(targetPackage + "." + genPackage.getPrefix() + "Factory");
 
         emfBuilderGenerator.checkConfiguration(issues);
 
         if (issues.getErrors().length > 0) {
           StringBuilder sb = new StringBuilder();
           for (Diagnostic diagnostic : issues.getErrors()) {
             sb.append(diagnostic.getMessage()).append("\n");
           }
           throw new RuntimeException(sb.toString());
         }
                
         emfBuilderGenerator.invoke(context, progressMonitor, issues);
         try {
           file.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
         } catch (CoreException e) {
           throw new RuntimeException(e);
        }    
         return Status.OK_STATUS;
       }
    };    
     job.setUser(true);
     job.schedule();
   }
 
   @Override
   public void selectionChanged(IAction p_action, ISelection p_selection) {
     m_selection = p_selection;
   }
 
   @Override
   public void setActivePart(IAction p_action, IWorkbenchPart p_targetPart) {
     m_activePart = p_targetPart;
   }
 }
