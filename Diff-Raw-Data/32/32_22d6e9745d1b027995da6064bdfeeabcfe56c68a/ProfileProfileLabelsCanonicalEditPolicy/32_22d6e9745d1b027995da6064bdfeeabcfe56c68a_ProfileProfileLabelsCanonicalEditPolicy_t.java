 package org.eclipse.uml2.diagram.profile.edit.policies;
 
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.CanonicalEditPolicy;
 import org.eclipse.gmf.runtime.notation.View;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.emf.ecore.EObject;
 
 import org.eclipse.uml2.diagram.profile.edit.parts.ElementImport2EditPart;
 
 import org.eclipse.uml2.diagram.profile.part.UMLVisualIDRegistry;
 
 import org.eclipse.uml2.uml.Namespace;
 
 /**
  * @generated
  */
 public class ProfileProfileLabelsCanonicalEditPolicy extends CanonicalEditPolicy {
 
 	/**
	 *  
 	 * @generated
 	 */
 	protected List getSemanticChildrenList() {
 		List result = new LinkedList();
 		EObject modelObject = ((View) getHost().getModel()).getElement();
 		View viewObject = (View) getHost().getModel();
 		EObject nextValue;
 		int nodeVID;
 		for (Iterator values = ((Namespace) modelObject).getElementImports().iterator(); values.hasNext();) {
 			nextValue = (EObject) values.next();
 			nodeVID = UMLVisualIDRegistry.getNodeVisualID(viewObject, nextValue);
 			if (ElementImport2EditPart.VISUAL_ID == nodeVID) {
 				result.add(nextValue);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected boolean shouldDeleteView(View view) {
 		return view.isSetElement() && view.getElement() != null && view.getElement().eIsProxy();
 	}
 
 	/**
 	 * @generated
 	 */
 	protected String getDefaultFactoryHint() {
 		return null;
 	}
 
 }
