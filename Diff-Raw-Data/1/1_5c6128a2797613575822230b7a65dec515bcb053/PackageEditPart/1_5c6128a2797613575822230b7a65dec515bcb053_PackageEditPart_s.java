 package org.eclipse.uml2.diagram.sequence.edit.parts;
 
 import org.eclipse.gef.EditPolicy;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.uml2.diagram.common.editpolicies.U2TGraphicalNodeEditPolicy;
 import org.eclipse.uml2.diagram.common.editpolicies.XYLayoutEditPolicyWithMovableLabels;
 import org.eclipse.uml2.diagram.sequence.edit.create.SDCreationEditPolicy;
 import org.eclipse.uml2.diagram.sequence.edit.policies.PackageCanonicalEditPolicy;
 import org.eclipse.uml2.diagram.sequence.edit.policies.PackageItemSemanticEditPolicy;
 import org.eclipse.uml2.diagram.sequence.part.UMLDiagramUpdateCommand;
 import org.eclipse.uml2.diagram.sequence.part.UMLVisualIDRegistry;
 
 /**
  * @generated
  */
 
 public class PackageEditPart extends DiagramEditPart {
 
 	/**
 	 * @generated
 	 */
 	public final static String MODEL_ID = "UMLSequence"; //$NON-NLS-1$
 
 	/**
 	 * @generated
 	 */
 	public static final int VISUAL_ID = 1000;
 
 	/**
 	 * @generated
 	 */
 	public PackageEditPart(View view) {
 		super(view);
 	}
 
 	/**
 	 * @generated
 	 */
 	protected void createDefaultEditPolicies() {
 		super.createDefaultEditPolicies();
 		installEditPolicy(EditPolicyRoles.SEMANTIC_ROLE, new PackageItemSemanticEditPolicy());
 		installEditPolicy(EditPolicyRoles.CANONICAL_ROLE, new PackageCanonicalEditPolicy());
 		// removeEditPolicy(org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles.POPUPBAR_ROLE);
 
 		installEditPolicy(EditPolicyRoles.CREATION_ROLE, new SDCreationEditPolicy(UMLVisualIDRegistry.TYPED_ADAPTER));
 		installEditPolicy(EditPolicy.LAYOUT_ROLE, new XYLayoutEditPolicyWithMovableLabels()); //replace with U2T specific version
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new U2TGraphicalNodeEditPolicy());
 	}
 
 	/**
 	 * @generated
 	 */
 	public void refreshDiagram() {
 		UMLDiagramUpdateCommand.performCanonicalUpdate(getDiagramView().getElement());
 	}
 
 }
