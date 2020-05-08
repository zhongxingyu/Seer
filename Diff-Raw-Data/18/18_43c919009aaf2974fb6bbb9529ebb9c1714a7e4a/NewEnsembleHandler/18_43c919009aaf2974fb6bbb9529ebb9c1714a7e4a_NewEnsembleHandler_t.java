 /*
  * License (BSD Style License):
  * Copyright (c) 2012
  * Software Engineering
  * Department of Computer Science
  * Technische Universitiät Darmstadt
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * - Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  * - Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  * - Neither the name of the Software Engineering Group or Technische
  * Universität Darmstadt nor the names of its contributors may be used to
  * endorse or promote products derived from this software without specific
  * prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package de.opalproject.vespucci.ui.handlers;
 
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.command.AddCommand;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 import de.opalproject.vespucci.datamodel.DatamodelFactory;
 import de.opalproject.vespucci.datamodel.DatamodelPackage;
 import de.opalproject.vespucci.datamodel.Ensemble;
 import de.opalproject.vespucci.ui.wizards.NewEnsembleWizard;
 
 /**
  * Used by NewEnsembleWizard to create a new ensemble
  * 
  * @author Lars
  * @author Marco Jacobasch
  * 
  */
 public class NewEnsembleHandler extends AbstractEnsembleCommandHandler {
 
 	@Override
 	public Command getCommand(IStructuredSelection selection,
 			ExecutionEvent event) {
 		NewEnsembleWizard wizard = createAndOpenWizard(event);
		if (wizard.getName() != null) {
			Ensemble ensemble = createEnsemble(wizard);
 
			EObject owner = getOwner(selection);
			Object feature = DatamodelPackage.Literals.TREE_NODE__CHILDREN;
 
			Command add = AddCommand.create(getEditingDomain(), owner, feature,
					ensemble);
 
			return add;
		}
		return null;
 	}
 
 	/**
 	 * Opens a new ensemble wizard
 	 * 
 	 * @param event
 	 * @return
 	 */
 	private NewEnsembleWizard createAndOpenWizard(ExecutionEvent event) {
 		final NewEnsembleWizard wizard = new NewEnsembleWizard();
 
 		WizardDialog dialog = new WizardDialog(
 				HandlerUtil.getActiveShell(event), wizard);
 		dialog.open();
 
 		return wizard;
 	}
 
 	/**
 	 * Creates a new ensemble with data provided by the wizard
 	 * 
 	 * @param wizard
 	 * @return
 	 */
 	private Ensemble createEnsemble(NewEnsembleWizard wizard) {
 		DatamodelFactory factory = DatamodelFactory.eINSTANCE;
 		Ensemble ensemble = factory.createConcreteEnsemble();
 		ensemble.setName(wizard.getName());
 		ensemble.setDescription(wizard.getDescription());
 		ensemble.setDerived(false);
 		ensemble.setQuery(wizard.getQuery());
 
 		return ensemble;
 	}
 
 }
