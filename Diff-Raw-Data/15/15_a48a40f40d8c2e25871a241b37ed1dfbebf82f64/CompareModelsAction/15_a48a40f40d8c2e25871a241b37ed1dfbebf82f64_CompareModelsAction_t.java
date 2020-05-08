 /**
  * <copyright>
  * 
  * Copyright (c) 2010-2012 Thales Global Services S.A.S.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Thales Global Services S.A.S. - initial API and implementation
  * 
  * </copyright>
  */
 package org.eclipse.emf.diffmerge.ui.actions;
 
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.compare.CompareUI;
 import org.eclipse.emf.diffmerge.ui.ComparisonContextManager;
 import org.eclipse.emf.diffmerge.ui.EMFDiffMergeUIPlugin;
 import org.eclipse.emf.diffmerge.ui.Messages;
 import org.eclipse.emf.diffmerge.ui.specification.IComparisonSpecification;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IObjectActionDelegate;
 import org.eclipse.ui.IWorkbenchPart;
 
 
 /**
  * The action for calling the EMF Diff/Merge UI.
  * @author Olivier Constant
  */
 public class CompareModelsAction implements IObjectActionDelegate {
   // Eclipse Compare used to terminate a comparison process immediately if the selected resources
   // were identical, even though they were only similar entry points for fragmented models
   // containing differences in other resources.
   
   /** The user selection (initially null) */
   private IStructuredSelection _selection;
   
   /** The current shell (initially null) */
   private Shell _shell;
   
   
   /**
    * Constructor
    */
   public CompareModelsAction() {
     _selection = null;
     _shell = null;
   }
   
   /**
    * Return the current selection
    * @return a non-null, potentially empty, unmodifiable list
    */
   @SuppressWarnings("unchecked")
   protected List<Object> getSelection() {
     List<Object> result;
     if (_selection == null)
       result = Collections.emptyList();
     else
       result = Collections.unmodifiableList(_selection.toList());
     return result;
   }
   
   /**
    * Return the current shell
    * @return a potentially null shell
    */
   protected Shell getShell() {
     return _shell;
   }
   
   /**
    * Return whether this action is applicable given the current selection
    */
   protected boolean isApplicable() {
     List<Object> allSelected = getSelection();
     int size = allSelected.size();
     if (size != 2 && size != 3)
       return false;
     ComparisonContextManager manager = EMFDiffMergeUIPlugin.getDefault().getContextManager();
     for (Object selected : allSelected) {
       if (!manager.isValidEntrypoint(selected))
         return false;
     }
     return true;
   }
   
   /**
    * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
    */
   public void run(IAction action_p) {
     List<Object> allSelected = getSelection();
     int size = allSelected.size();
     if (size == 2 || size == 3) {
       ComparisonContextManager manager = EMFDiffMergeUIPlugin.getDefault().getContextManager();
      ComparisonSetup setup = manager.createComparisonSetup(
           allSelected.get(0), allSelected.get(1), size == 3? allSelected.get(2): null);
       if (setup != null) {
         ComparisonSetupWizard wizard = new ComparisonSetupWizard(setup);
         WizardDialog dialog = new WizardDialog(getShell(), wizard);
         dialog.setHelpAvailable(false);
         if (Window.OK == dialog.open()) {
           IComparisonSpecification specification = setup.getComparisonSpecification();
           if (specification != null) {
             EMFDiffMergeEditorInput input = new EMFDiffMergeEditorInput(specification);
             CompareUI.openCompareEditor(input);
           }
         }
       } else {
         MessageDialog.openError(getShell(), EMFDiffMergeUIPlugin.LABEL,
             Messages.CompareModelsAction_ModelsOnly);
       }
     }
   }
   
   /**
    * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
    */
   public void selectionChanged(IAction action_p, ISelection selection_p) {
     if (selection_p instanceof IStructuredSelection)
       _selection = (IStructuredSelection)selection_p;
     else
       _selection = null;
     if (action_p != null)
       action_p.setEnabled(isApplicable());
   }
   
   /**
    * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
    */
   public void setActivePart(IAction action_p, IWorkbenchPart targetPart_p) {
     if (targetPart_p != null && targetPart_p.getSite() != null)
       _shell = targetPart_p.getSite().getShell();
   }
   
 }
