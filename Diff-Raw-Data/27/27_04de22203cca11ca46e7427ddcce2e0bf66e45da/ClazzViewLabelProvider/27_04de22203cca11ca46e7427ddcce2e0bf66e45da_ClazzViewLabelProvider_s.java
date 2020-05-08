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
 
 import org.eclipse.gmf.runtime.diagram.ui.editparts.ITextAwareEditPart;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.papyrus.uml.diagram.clazz.edit.parts.UMLEditPartFactory;
import org.eclipse.papyrus.uml.diagram.common.editpolicies.IMaskManagedLabelEditPolicy;
 import org.eclipse.papyrus.uml.tools.utils.ICustomAppearence;
 import org.eclipse.papyrus.uml.tools.utils.PropertyUtil;
 import org.eclipse.uml2.uml.Property;
 
 /**
  * View label provider for Class.
  * @author Mickael Barbero <a href="mailto:mickael.barbero@obeo.fr">mickael.barbero@obeo.fr</a>
  */
 public class ClazzViewLabelProvider extends AbstractUMLViewLabelProvider {
 
 	/**
 	 * The edit part factory.
 	 */
 	private static final UMLEditPartFactory EDIT_PART_FACTORY = new UMLEditPartFactory();
 	
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @see org.eclipse.emf.compare.diagram.provider.IViewLabelProvider#isManaged(org.eclipse.gmf.runtime.notation.View)
 	 */
 	public boolean isManaged(View view) {
 		return EDIT_PART_FACTORY.createEditPart(null, view) instanceof ITextAwareEditPart;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @see org.eclipse.emf.compare.diagram.papyrus.AbstractUMLViewLabelProvider#getLabelSwitch(org.eclipse.papyrus.diagram.common.editpolicies.IMaskManagedLabelEditPolicy)
 	 */
 	@Override
 	protected LabelSwitch getLabelSwitch(IMaskManagedLabelEditPolicy labelEditPolicy) {
 		return new ClassLabelSwitch(labelEditPolicy);
 	}
 	
 	/**
 	 * Switch to return the label in relation to the kind of model object.
 	 * @author Mickael Barbero <a href="mailto:mickael.barbero@obeo.fr">mickael.barbero@obeo.fr</a>
 	 */
 	class ClassLabelSwitch extends LabelSwitch {
 
 		/**
 		 * Constructor.
 		 * @param labelEditPolicy The label edit policy..
 		 */
 		ClassLabelSwitch(IMaskManagedLabelEditPolicy labelEditPolicy) {
 			super(labelEditPolicy);
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 *
 		 * @see org.eclipse.emf.compare.diagram.papyrus.AbstractUMLViewLabelProvider.LabelSwitch#caseProperty(org.eclipse.uml2.uml.Property)
 		 */
 		@Override
 		public String caseProperty(Property object) {
 			if (object.getOwningAssociation() != null) {
 				int displayValue = ICustomAppearence.DEFAULT_UML_RELATIONEND_PROPERTY;
 				if (getLabelEditPolicy() != null) {
 					displayValue = getLabelEditPolicy().getCurrentDisplayValue();
 				}
 				return PropertyUtil.getCustomLabel(object, displayValue);
 			}
 			return super.caseProperty(object);
 		}
 	}
 	
 }
