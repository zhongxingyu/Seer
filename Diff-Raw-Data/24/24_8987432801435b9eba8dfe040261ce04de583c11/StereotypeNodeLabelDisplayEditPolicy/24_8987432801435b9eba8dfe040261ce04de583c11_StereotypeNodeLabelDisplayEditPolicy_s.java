 /*****************************************************************************
  * Copyright (c) 2012 CEA LIST.
  *
  *    
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the CeCILL-C Free Software License v1.0
  * which accompanies this distribution, and is available at
  * http://www.cecill.info/licences/Licence_CeCILL-C_V1-en.html
  *
  * Contributors:
  *  Saadia DHOUIB (CEA LIST) - Initial API and implementation
  *
  *****************************************************************************/
 package org.eclipse.papyrus.robotml.diagram.architecture.edit.policy;
 
import java.util.List;
 
 import org.eclipse.papyrus.uml.diagram.common.Activator;
 import org.eclipse.papyrus.uml.diagram.common.editpolicies.AppliedStereotypeNodeLabelDisplayEditPolicy;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.uml2.uml.Element;
 import org.eclipse.uml2.uml.Stereotype;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class StereotypeNodeLabelDisplayEditPolicy.
  */
 public class StereotypeNodeLabelDisplayEditPolicy extends AppliedStereotypeNodeLabelDisplayEditPolicy {
 
 	// Always show the stereotype as icon
 	/* (non-Javadoc)
 	 * @see org.eclipse.papyrus.uml.diagram.common.editpolicies.AppliedStereotypeLabelDisplayEditPolicy#stereotypeIconToDisplay()
 	 */
 	@Override
 	public Image stereotypeIconToDisplay() {
 
 		// retrieve the first stereotype in the list of displayed stereotype
 		if(getUMLElement() instanceof Element) {
			Element umlElement = getUMLElement();
			List<Stereotype> appliedSt = umlElement.getAppliedStereotypes();
			if(!appliedSt.isEmpty()) {
				Image icon = Activator.getIconElement(getUMLElement(), appliedSt.get(0), false);
				//return Activator.getIconElement(getUMLElement(), appliedSt.get(0), false);
				return icon;
 			}
 		}
 		return null;
 
 
 	}
 }
