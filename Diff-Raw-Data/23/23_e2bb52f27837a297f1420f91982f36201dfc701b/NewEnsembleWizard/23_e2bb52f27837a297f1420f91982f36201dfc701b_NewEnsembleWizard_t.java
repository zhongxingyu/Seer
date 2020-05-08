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
 package de.opalproject.vespucci.ui.wizards;
 
 import java.lang.reflect.InvocationTargetException;
 import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.command.AddCommand;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.ui.actions.WorkspaceModifyOperation;
 
 import de.opalproject.vespucci.datamodel.DatamodelFactory;
 import de.opalproject.vespucci.datamodel.DatamodelPackage;
 import de.opalproject.vespucci.datamodel.Ensemble;
import de.opalproject.vespucci.ui.Activator;
 import de.opalproject.vespucci.ui.utils.EmfService;
 
 /**
  * Wizard for creating new ensembles
  * 
  * @author Lars
  * 
  */
 public class NewEnsembleWizard extends Wizard {
 
 	protected NewEnsembleWizardPage page;
 	protected NewEnsembleWizardQueryPage page2;
 
 	private String name;
 	private String description;
 	private String query;
 	private IStructuredSelection selection;
 
 	public NewEnsembleWizard(IStructuredSelection iStructuredSelection) {
 		super();
 		this.selection = iStructuredSelection;
 		setNeedsProgressMonitor(true);
 	}
 
 	@Override
 	public void addPages() {
 		page = new NewEnsembleWizardPage();
 		addPage(page);
 		page2 = new NewEnsembleWizardQueryPage();
 		addPage(page2);
 	}
 
 	@Override
 	public boolean performFinish() {
 		name = page.getEnsembleName();
 		description = page.getEnsembleDescription();
 		query = page2.getEnsembleQuery();
 
 		WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
 			@Override
 			protected void execute(IProgressMonitor progressMonitor) {
 				try {
 
 					final TransactionalEditingDomain domain = TransactionalEditingDomain.Registry.INSTANCE
 							.getEditingDomain("de.opalproject.vespucci.navigator.domain.DatamodelEditingDomain");
 
 					DatamodelFactory factory = DatamodelFactory.eINSTANCE;
 					Ensemble ensemble = factory.createConcreteEnsemble();
 					ensemble.setName(getName());
 					ensemble.setDescription(getDescription());
 					ensemble.setDerived(false);
 					ensemble.setQuery(getQuery());
 
 					EObject owner = (EObject) selection.getFirstElement();
 					Object feature = DatamodelPackage.Literals.TREE_NODE__CHILDREN;
 
 					Command add = AddCommand.create(domain, owner, feature,
 							ensemble);
 					domain.getCommandStack().execute(add);
 					EmfService.save(domain);
 
 				} catch (Exception exception) {
					Activator
							.getDefault()
							.getLog()
							.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
									exception.getStackTrace().toString()));
 				} finally {
 					progressMonitor.done();
 				}
 			}
 		};
 
 		try {
 			getContainer().run(false, false, operation);
		} catch (InvocationTargetException | InterruptedException exception) {
			Activator
					.getDefault()
					.getLog()
					.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
							exception.getStackTrace().toString()));
 		}
 
 		return true;
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @return the description
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * @return the query
 	 */
 	public String getQuery() {
 		return query;
 	}
 }
