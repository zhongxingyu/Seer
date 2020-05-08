 /*******************************************************************************
  * Copyright (c) 2006, 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.diagram.papyrus;
 
 import org.eclipse.emf.compare.diagram.diff.util.DiffUtil;
 import org.eclipse.emf.compare.diagram.provider.AbstractLabelProvider;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.ITextAwareEditPart;
 import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.IMaskManagedLabelEditPolicy;
 import org.eclipse.papyrus.uml.tools.utils.CollaborationUseUtil;
 import org.eclipse.papyrus.uml.tools.utils.ICustomAppearence;
 import org.eclipse.papyrus.uml.tools.utils.InstanceSpecificationUtil;
 import org.eclipse.papyrus.uml.tools.utils.OperationUtil;
 import org.eclipse.papyrus.uml.tools.utils.ParameterUtil;
 import org.eclipse.papyrus.uml.tools.utils.PropertyUtil;
 import org.eclipse.uml2.uml.CollaborationUse;
 import org.eclipse.uml2.uml.InstanceSpecification;
 import org.eclipse.uml2.uml.Operation;
 import org.eclipse.uml2.uml.Parameter;
 import org.eclipse.uml2.uml.Property;
 import org.eclipse.uml2.uml.util.UMLSwitch;
 
 /**
  * View label provider for UMLView.
  * 
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 public abstract class AbstractUMLViewLabelProvider extends AbstractLabelProvider {
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.diagram.provider.AbstractLabelProvider#elementLabel(org.eclipse.gmf.runtime.notation.View)
 	 */
 	@Override
 	public String elementLabel(View view) {
 		if (view == null) {
 			throw new IllegalArgumentException("view"); //$NON-NLS-1$
 		}
 		final ITextAwareEditPart editPart = DiffUtil.getTextEditPart(view);
 		final EObject semanticElement = getSemanticElement(editPart);
 
 		final IMaskManagedLabelEditPolicy policy = (IMaskManagedLabelEditPolicy)editPart
 				.getEditPolicy(IMaskManagedLabelEditPolicy.MASK_MANAGED_LABEL_EDIT_POLICY);
 		final String label = getLabelSwitch(policy).doSwitch(semanticElement);
 
 		if (label != null && label.length() > 0) {
 			return label;
 		}
 
 		// fall-through super implementation
 		return super.elementLabel(view);
 	}
 
 	/**
 	 * Get the label switch in relation to the label edit policy.
 	 * 
 	 * @param labelEditPolicy
 	 *            The policy.
 	 * @return The switch.
 	 */
 	protected LabelSwitch getLabelSwitch(IMaskManagedLabelEditPolicy labelEditPolicy) {
 		return new LabelSwitch(labelEditPolicy);
 	}
 
 	/**
 	 * Switch to return the label in relation to the kind of model object.
 	 * 
 	 * @author Mickael Barbero <a href="mailto:mickael.barbero@obeo.fr">mickael.barbero@obeo.fr</a>
 	 */
 	static class LabelSwitch extends UMLSwitch<String> {
 
 		/**
 		 * The label edit policy.
 		 */
 		private final IMaskManagedLabelEditPolicy labelEditPolicy;
 
 		/**
 		 * Constructor.
 		 * 
 		 * @param pLabelEditPolicy
 		 *            The edit policy.
 		 */
 		LabelSwitch(IMaskManagedLabelEditPolicy pLabelEditPolicy) {
 			this.labelEditPolicy = pLabelEditPolicy;
 		}
 
 		/**
 		 * Returns the label edit policy.
 		 * 
 		 * @return the labelEditPolicy The policy.
 		 */
 		public IMaskManagedLabelEditPolicy getLabelEditPolicy() {
 			return labelEditPolicy;
 		}
 
 		@Override
 		public String caseProperty(Property object) {
 			int displayValue = ICustomAppearence.DEFAULT_UML_PROPERTY;
 			if (getLabelEditPolicy() != null) {
 				displayValue = getLabelEditPolicy().getCurrentDisplayValue();
 			}
 			return PropertyUtil.getCustomLabel(object, displayValue);
 		}
 
 		@Override
 		public String caseOperation(Operation object) {
 			int displayValue = ICustomAppearence.DEFAULT_UML_OPERATION;
 			if (getLabelEditPolicy() != null) {
 				displayValue = getLabelEditPolicy().getCurrentDisplayValue();
 			}
 			return OperationUtil.getCustomLabel(object, displayValue);
 		}
 
 		@Override
 		public String caseInstanceSpecification(InstanceSpecification object) {
 			int displayValue = ICustomAppearence.DEFAULT_UML_INSTANCESPECIFICATION;
 			if (getLabelEditPolicy() != null) {
 				displayValue = getLabelEditPolicy().getCurrentDisplayValue();
 			}
 			return InstanceSpecificationUtil.getCustomLabel(object, displayValue);
 		}
 
 		@Override
 		public String caseCollaborationUse(CollaborationUse object) {
 			int displayValue = ICustomAppearence.DEFAULT_UML_PROPERTY;
 			if (getLabelEditPolicy() != null) {
 				displayValue = getLabelEditPolicy().getCurrentDisplayValue();
 			}
 			return CollaborationUseUtil.getCustomLabel(object, displayValue);
 		}
 
 		@Override
 		public String caseParameter(Parameter object) {
 			int displayValue = ICustomAppearence.DEFAULT_UML_PARAMETER;
 			if (getLabelEditPolicy() != null) {
 				displayValue = getLabelEditPolicy().getCurrentDisplayValue();
 			}
 			return ParameterUtil.getCustomLabel(object, displayValue);
 		}
 	}
 }
