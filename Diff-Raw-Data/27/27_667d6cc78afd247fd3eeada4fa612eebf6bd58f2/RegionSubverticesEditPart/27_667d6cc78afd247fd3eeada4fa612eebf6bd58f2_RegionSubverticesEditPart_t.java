 package org.eclipse.uml2.diagram.statemachine.edit.parts;
 
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeCompartmentEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.CreationEditPolicy;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.DragDropEditPolicy;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
 import org.eclipse.gmf.runtime.diagram.ui.figures.ResizableCompartmentFigure;
 import org.eclipse.gmf.runtime.draw2d.ui.figures.ConstrainedToolbarLayout;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.uml2.diagram.statemachine.edit.policies.RegionSubverticesCanonicalEditPolicy;
 import org.eclipse.uml2.diagram.statemachine.edit.policies.RegionSubverticesItemSemanticEditPolicy;
 
 /**
  * @generated
  */
 public class RegionSubverticesEditPart extends ShapeCompartmentEditPart {
 
 	/**
 	 * @generated
 	 */
 	public static final int VISUAL_ID = 7001;
 
 	/**
 	 * @generated
 	 */
 	public RegionSubverticesEditPart(View view) {
 		super(view);
 	}
 
 	/**
 	 * @generated
 	 */
 	public String getCompartmentName() {
 		return "subvertices";
 	}
 
 	/**
 	 * @generated
 	 */
 	public IFigure createFigure() {
 		ResizableCompartmentFigure result = (ResizableCompartmentFigure) super.createFigure();
 		result.setTitleVisibility(false);
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected void createDefaultEditPolicies() {
 		super.createDefaultEditPolicies();
 		installEditPolicy(EditPolicyRoles.SEMANTIC_ROLE, new RegionSubverticesItemSemanticEditPolicy());
 		installEditPolicy(EditPolicyRoles.CREATION_ROLE, new CreationEditPolicy());
 		installEditPolicy(EditPolicyRoles.DRAG_DROP_ROLE, new DragDropEditPolicy());
 		installEditPolicy(EditPolicyRoles.CANONICAL_ROLE, new RegionSubverticesCanonicalEditPolicy());
 	}
 
 	/**
 	 * @generated
 	 */
 	protected void setRatio(Double ratio) {
 		if (getFigure().getParent().getLayoutManager() instanceof ConstrainedToolbarLayout) {
 			super.setRatio(ratio);
 		}
 	}
 }
