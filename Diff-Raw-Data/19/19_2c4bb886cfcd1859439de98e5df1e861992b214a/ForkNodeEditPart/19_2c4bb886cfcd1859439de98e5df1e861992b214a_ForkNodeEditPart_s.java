 package org.eclipse.uml2.diagram.activity.edit.parts;
 
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.RectangleFigure;
 import org.eclipse.draw2d.StackLayout;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.EditPolicy;
 import org.eclipse.gef.Request;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gef.editpolicies.LayoutEditPolicy;
 import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
 import org.eclipse.gef.requests.CreateRequest;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeNodeEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
 import org.eclipse.gmf.runtime.draw2d.ui.figures.ConstrainedToolbarLayout;
 import org.eclipse.gmf.runtime.gef.ui.figures.DefaultSizeNodeFigure;
 import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.uml2.diagram.activity.edit.policies.ForkNodeItemSemanticEditPolicy;
 import org.eclipse.uml2.diagram.common.editpolicies.DynamicResizeDirectionsEditPolicy;
 
 /**
  * @generated
  */
 public class ForkNodeEditPart extends ShapeNodeEditPart {
 
 	/**
 	 * @generated
 	 */
 	public static final int VISUAL_ID = 2012;
 
 	/**
 	 * @generated
 	 */
 	protected IFigure contentPane;
 
 	/**
 	 * @generated
 	 */
 	protected IFigure primaryShape;
 
 	/**
 	 * @generated
 	 */
 	public ForkNodeEditPart(View view) {
 		super(view);
 	}
 
 	/**
 	 * @generated
 	 */
 	protected void createDefaultEditPolicies() {
 		super.createDefaultEditPolicies();
 
 		installEditPolicy(EditPolicyRoles.SEMANTIC_ROLE, new ForkNodeItemSemanticEditPolicy());
 		installEditPolicy(EditPolicy.LAYOUT_ROLE, createLayoutEditPolicy());
 	}
 
 	/**
 	 * @generated
 	 */
 	protected LayoutEditPolicy createLayoutEditPolicy() {
 		LayoutEditPolicy lep = new LayoutEditPolicy() {
 
 			protected EditPolicy createChildEditPolicy(EditPart child) {
 				EditPolicy result = child.getEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE);
 				if (result == null) {
 					result = new NonResizableEditPolicy();
 				}
 				return result;
 			}
 
 			protected Command getMoveChildrenCommand(Request request) {
 				return null;
 			}
 
 			protected Command getCreateCommand(CreateRequest request) {
 				return null;
 			}
 		};
 		return lep;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected IFigure createNodeShape() {
 		ForkJoinFigure figure = new ForkJoinFigure();
 		return primaryShape = figure;
 	}
 
 	/**
 	 * @generated
 	 */
 	public ForkJoinFigure getPrimaryShape() {
 		return (ForkJoinFigure) primaryShape;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected NodeFigure createNodePlate() {
 		DefaultSizeNodeFigure result = new DefaultSizeNodeFigure(getMapMode().DPtoLP(4), getMapMode().DPtoLP(50));
 
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public EditPolicy getPrimaryDragEditPolicy() {
 		return new DynamicResizeDirectionsEditPolicy();
 	}
 
 	/**
 	 * Creates figure for this edit part.
 	 * 
 	 * Body of this method does not depend on settings in generation model
 	 * so you may safely remove <i>generated</i> tag and modify it.
 	 * 
 	 * @generated
 	 */
 	protected NodeFigure createNodeFigure() {
 		NodeFigure figure = createNodePlate();
 		figure.setLayoutManager(new StackLayout());
 		IFigure shape = createNodeShape();
 		figure.add(shape);
 		contentPane = setupContentPane(shape);
 		return figure;
 	}
 
 	/**
 	 * Default implementation treats passed figure as content pane.
 	 * Respects layout one may have set for generated figure.
 	 * @param nodeShape instance of generated figure class
 	 * @generated
 	 */
 	protected IFigure setupContentPane(IFigure nodeShape) {
 		if (nodeShape.getLayoutManager() == null) {
 			ConstrainedToolbarLayout layout = new ConstrainedToolbarLayout();
 			layout.setSpacing(getMapMode().DPtoLP(5));
 			nodeShape.setLayoutManager(layout);
 		}
 		return nodeShape; // use nodeShape itself as contentPane
 	}
 
 	/**
 	 * @generated
 	 */
 	public IFigure getContentPane() {
 		if (contentPane != null) {
 			return contentPane;
 		}
 		return super.getContentPane();
 	}
 
 	/**
 	 * @generated
 	 */
 	public class ForkJoinFigure extends RectangleFigure {
 
 		/**
 		 * @generated
 		 */
 		public ForkJoinFigure() {
 			this.setFill(true);
 			this.setFillXOR(false);
 			this.setOutline(true);
 			this.setOutlineXOR(false);
 			this.setLineWidth(1);
 			this.setLineStyle(Graphics.LINE_SOLID);
 			this.setBackgroundColor(ColorConstants.black);
 			this.setPreferredSize(new Dimension(getMapMode().DPtoLP(4), getMapMode().DPtoLP(50)));
			this.setMinimumSize(new Dimension(getMapMode().DPtoLP(4), getMapMode().DPtoLP(50)));
 		}
 
 		/**
 		 * @generated
 		 */
 		private boolean myUseLocalCoordinates = false;
 
 		/**
 		 * @generated
 		 */
 		protected boolean useLocalCoordinates() {
 			return myUseLocalCoordinates;
 		}
 
 		/**
 		 * @generated
 		 */
 		protected void setUseLocalCoordinates(boolean useLocalCoordinates) {
 			myUseLocalCoordinates = useLocalCoordinates;
 		}
 
 	}
 
 }
