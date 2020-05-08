 // NewModifierWizard.java
 package org.eclipse.stem.ui.wizards;
 
 /*******************************************************************************
  * Copyright (c) 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.IHandler;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.provider.DelegatingWrapperItemProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.stem.core.STEMURI;
 import org.eclipse.stem.core.common.Identifiable;
 import org.eclipse.stem.core.graph.Edge;
 import org.eclipse.stem.core.modifier.Modifier;
 import org.eclipse.stem.core.modifier.ModifierFactory;
 import org.eclipse.stem.core.modifier.ModifierPackage;
 import org.eclipse.stem.ui.Activator;
 import org.eclipse.stem.ui.adapters.newmodifierpage.NewModifierPageAdapter;
 import org.eclipse.stem.ui.adapters.newmodifierpage.NewModifierPageAdapterFactory;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 /**
  * This class is a {@link NewIdentifiableWizard} for that creates new STEM
  * {@link Modifier}s.
  */
 public class NewModifierWizard extends NewIdentifiableWizard {
 
 	/**
 	 * This is the {@link Identifiable} for which the wizard is creating a
 	 * {@link Modifier}.
 	 */
 	private EObject target;
 	private NewModifierPage newModifierPage;
 
 	/**
 	 * @see org.eclipse.stem.ui.wizards.NewIdentifiableWizard#getWizardTitle()
 	 */
 	@Override
 	protected String getWizardTitle() {
 		return Messages.getString("NModifierWiz.wizard_title"); //$NON-NLS-1$
 	}
 
 	/**
 	 * @see org.eclipse.stem.ui.wizards.NewIdentifiableWizard#createNewIdentifiablePage()
 	 */
 	@Override
 	protected NewIdentifiablePage createNewIdentifiablePage() {
 
 		// We use an adapter factory to adapt the Identifiable into a
 		// NewModifierPage. We do this because the universe of Identifiables is
 		// unlimited. The number and types of fields can vary widely. The
 		// adapter factory is Composable which means that it maintains an
 		// extensible collection of other factories that can be used to perform
 		// the adaption. For instance, a new Decorator (a type of Identifiable)
 		// that implements a disease model computation can be adapted by the
 		// addition of a factory that recognizes it.
 		final NewModifierPageAdapter adapter = (NewModifierPageAdapter) NewModifierPageAdapterFactory.INSTANCE
 				.adapt(target, NewModifierPageAdapter.class);
 		// Were we successful in adapting?
 		if (adapter != null) {
 			// Yes
 			adapter.setTarget(target);
 			newModifierPage = adapter.createNewModifierPage();
 		} // if
 		else {
 			// No
 			Activator.logError(
 					"Internal Error: could not create NewModifierPage for \""
 							+ target.getClass().getName() + "\"", null);
 			newModifierPage = new NewModifierPage(target) {
 				@Override
 				protected Composite createSpecificComposite(
 						@SuppressWarnings("unused") final Composite parent) {
 					return null;
 				}
 			};
 		} // else
 		
 		this.setHelpContextId("org.eclipse.stem.doc.newmodifier_contextid"); //HERE
 
 		return newModifierPage;
 	} // createNewIdentifiablePage
 
 	/**
 	 * @see org.eclipse.stem.ui.wizards.NewIdentifiableWizard#createDublinCorePage()
 	 */
 	@Override
 	protected DublinCorePage createDublinCorePage() {
 		return new DublinCorePage() {
 			@Override
 			protected void initializeDCAttributes() {
 				super.initializeDCAttributes();
 				format.setText(ModifierPackage.eNS_URI);
 				format.setEnabled(false);
 				type.setText(STEMURI.MODIFIER_TYPE_URI.toString());
 				type.setEnabled(false);
 			}
 		};
 	} // createDublinCorePage
 
 	/**
 	 * @see org.eclipse.stem.ui.wizards.NewIdentifiableWizard#createIdentifiable()
 	 */
 	@Override
 	protected Identifiable createIdentifiable() {
 		final Modifier retValue = ModifierFactory.eINSTANCE.createModifier();
 		retValue.setDublinCore(newDublinCorePage.createDublinCore());
 		// We allow Edges to be considered to be "modifiable", but we really
 		// mean that their labels can be modified. This is a convenience for the
 		// user so that they can right-click on an Edge and create a modifier
 		// for their label. The problem is that we've got to get the right
 		// target URI, if we just get the URI of the Identifiable (which could
 		// be an EdgeLabel) then we'd get the URI of the Edge and not its Label.
 
 		// Is this an Edge?
 		if (target instanceof Edge) {
 			// Yes
 			final Edge edge = (Edge) target;
 			retValue.setTargetURI(edge.getLabel().getURI());
 		} else if(target instanceof Identifiable){
 			retValue.setTargetURI(((Identifiable)target).getURI());
 		} else retValue.setTargetURI(null); // String value or double value
 		retValue.getFeatureModifiers().addAll(
 				newModifierPage.getFeatureModifiers());
 		return retValue;
 	} // createSpecificIdentifiable
 
 	/**
 	 * @see org.eclipse.stem.ui.wizards.NewIdentifiableWizard#getSerializationFolderName()
 	 */
 	@Override
 	protected String getSerializationFolderName() {
 		return NewSTEMProjectWizard.MODIFIERS_FOLDER_NAME;
 	} // getSerializationFolderName
 
 	/**
 	 * @see org.eclipse.stem.ui.wizards.NewIdentifiableWizard#getSerializationFileNameExtension()
 	 */
 	@Override
 	protected String getSerializationFileNameExtension() {
 		return ModifierPackage.eNAME;
 	} // getIdentifiableSerializationFileExtension
 
 	/**
 	 * @param target
 	 *            the target that the will
 	 *            modify.
 	 */
 	final public void setTarget(final EObject target) {
 		this.target = target;
 	}
 
 	/**
 	 * This class is a {@link IHandler} for the command that creates a new
 	 * ModifierWizard.
 	 */
 	public static class NewModifierWizardCommandHandler extends AbstractHandler
 			implements IHandler {
 
 		/**
 		 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
 		 */
 		public Object execute(final ExecutionEvent executionEvent)
 				throws ExecutionException {
 
 			final ISelection selection = HandlerUtil
 					.getCurrentSelectionChecked(executionEvent);
 
 			// Structured Selection?
 			if (selection instanceof StructuredSelection) {
 				// Yes
 				try {
 					final Object obj = ((StructuredSelection) selection).toArray()[0];
					EObject target = null;
					if(obj instanceof DelegatingWrapperItemProvider) 
						target = (EObject)((DelegatingWrapperItemProvider)obj).getValue();
					else if (obj instanceof EObject) 
						target = (EObject)obj;
					
					if(target != null) {
 						final IWorkbenchWindow window = HandlerUtil
 								.getActiveWorkbenchWindowChecked(executionEvent);
 						final NewModifierWizard wizard = new NewModifierWizard();
 						wizard.setTarget(target);
 						wizard.init(window.getWorkbench(),
 								StructuredSelection.EMPTY);
 						final WizardDialog wizardDialog = new STEMWizardDialog(window
 								.getShell(), wizard);
 						wizardDialog.open();
					} else {
 						// No
 						Activator.logError(
 								"Internal error: attempting to create Modifier Wizard for \""
 										+ obj.getClass().getName() + "\"", null);
 					} // else
 				} catch (ArrayIndexOutOfBoundsException ioobe) {
 					// Ignore, it just means there wasn't anything really selected.
 				}
 			} // if StructuredSelection
 
 			return null;
 		} // execute
 	} // NewModifierWizardCommandHandler
 } // NewModifierWizard
