 package org.ow2.fractal.mind.diagram.custom.edit.parts.generic;
 
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.LayoutManager;
 import org.eclipse.gef.EditPolicy;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
 import org.ow2.fractal.mind.diagram.custom.edit.policies.FixedChildrenLayoutEditPolicy;
 import org.ow2.fractal.mind.diagram.custom.edit.policies.MindSuperCreationEditPolicy;
 import org.ow2.fractal.mind.diagram.custom.layouts.ConstrainedFlowLayout;
 import org.ow2.fractal.mind.diagram.custom.layouts.CustomFreeFormLayoutEx;
 
 public class MindListCompartmentEditPart extends MindCompartmentEditPart {
 
 	public MindListCompartmentEditPart(GraphicalEditPart editPart, int vID) {
 		super(editPart, vID, TYPE_COMPARTMENT_LIST);
 	}
 
 	public MindListCompartmentEditPart(GraphicalEditPart editPart, int vID,
 			int mindType) {
 		super(editPart, vID, mindType);
 	}
 	
 	@Override
 	public void createDefaultEditPolicies(){
 		super.createDefaultEditPolicies();
 		// Extended layout features
 		realEditPart.installEditPolicy(EditPolicy.LAYOUT_ROLE,
 				new FixedChildrenLayoutEditPolicy());
 		// Extended creation features
 		realEditPart.installEditPolicy(EditPolicyRoles.CREATION_ROLE,
 				new MindSuperCreationEditPolicy());
 		// No drag and drop
 		realEditPart.removeEditPolicy(EditPolicyRoles.DRAG_DROP_ROLE);
 	}
 	
 	@Override
 	public void setLayoutManager(IFigure figure) {
 		figure.setLayoutManager(getLayoutManager());
 	}
 	
 	
 	/**
 	 * Implements a ConstrainedFlowLayout
 	 * @return
 	 */
 	@Override
 	public LayoutManager getLayoutManager() {
 		if (layoutManager == null) {
 			layoutManager = new ConstrainedFlowLayout(false);
 			((ConstrainedFlowLayout)layoutManager).setMinorSpacing(0);
 			((ConstrainedFlowLayout)layoutManager).setMajorSpacing(0);
 		}
 		return layoutManager;
 	}
 
 }
