 /*******************************************************************************
  * Copyright (c) 2007-2008 The Bioclipse Project and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * <http://www.eclipse.org/legal/epl-v10.html>
  * 
  * Contributors:
  *     Jonathan Alvarsson
  *     
  ******************************************************************************/
 package net.bioclipse.brunn.ui.explorer;
 
 import net.bioclipse.brunn.ui.explorer.model.ITreeObject;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 
 
 public abstract class BrunnAction extends Action {
 
     private TreeViewer treeViewer;
     protected ITreeObject selectedDomainObject;
    IStructuredSelection selection;
     
     public BrunnAction( String name, TreeViewer treeViewer ) {
         super(name);
         this.treeViewer = treeViewer;
     }
     
     @Override
     public final void run() {
         
        if ( selection == null ) {
            if ( treeViewer.getSelection().isEmpty() ) {
                throw new IllegalStateException( 
                    "No folder was selected in the treeviewer");
            }
            selection = (IStructuredSelection) treeViewer.getSelection();
         }
        
         selectedDomainObject = (ITreeObject) selection.getFirstElement();
        
         if ( popUpDialog(selection) == Dialog.OK ) {
             Job job = new Job( getText() ) {
                 @Override
                 protected IStatus run( IProgressMonitor monitor ) {
                     performWork( monitor );
                     return Status.OK_STATUS;
                 }
             };
             job.schedule();
         }
     }
     
     public abstract void performWork( IProgressMonitor monitor );
     
     public abstract int popUpDialog(IStructuredSelection selection);
 }
