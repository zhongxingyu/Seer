 /*******************************************************************************
  * Copyright (c) 2011 University of Illinois at Urbana-Champaign
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *  
  * Contributors: 
  * University of Illinois at Urbana-Champaign - initial API and implementation
  *******************************************************************************/
 package edu.illinois.parallelism.openmp.refactoring.padscalarvariables;
 
 import org.eclipse.cdt.core.model.ICElement;
 import org.eclipse.cdt.core.model.ICProject;
 import org.eclipse.cdt.internal.ui.refactoring.RefactoringRunner;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.window.IShellProvider;
 import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
 
 /**
  * 
  * @author nchen
  * 
  */
 @SuppressWarnings("restriction")
 public class PadScalarVariablesRunner extends RefactoringRunner {
 
 	public PadScalarVariablesRunner(IFile file, ISelection selection, ICElement element, IShellProvider shellProvider, ICProject cProject) {
 		super(file, selection, element, shellProvider, cProject);
 	}
 
 	@Override
 	public void run() {
 		PadScalarVariablesRefactoring refactoring = new PadScalarVariablesRefactoring(file, selection, celement, project);
 
 		if (!refactoring.isWithinProperOMPRegion()) {
 			MessageDialog.openInformation(shellProvider.getShell(), Messages.PadScalarVariablesRunner_unavailableDialogTitle, Messages.PadScalarVariablesRunner_unavailableDialogInfo);
 			return;
 		}
 
 		PadScalarVariablesWizard wizard = new PadScalarVariablesWizard(refactoring);
 		RefactoringWizardOpenOperation operation = new RefactoringWizardOpenOperation(wizard);
 		try {
 			operation.run(shellProvider.getShell(), refactoring.getName());
 		} catch (InterruptedException e) {
 			// initial condition checking got canceled by the user.
 		}
 
 	}
 
 }
