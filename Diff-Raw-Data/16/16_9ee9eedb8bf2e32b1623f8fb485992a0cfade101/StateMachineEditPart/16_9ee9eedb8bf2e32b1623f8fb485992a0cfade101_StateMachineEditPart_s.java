 package org.eclipse.uml2.diagram.statemachine.edit.parts;
 
 import org.eclipse.draw2d.BorderLayout;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.Label;
 import org.eclipse.draw2d.LayoutManager;
 import org.eclipse.draw2d.MarginBorder;
 import org.eclipse.draw2d.RectangleFigure;
 import org.eclipse.draw2d.RoundedRectangle;
 import org.eclipse.draw2d.Shape;
 import org.eclipse.draw2d.StackLayout;
 import org.eclipse.draw2d.ToolbarLayout;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.EditPolicy;
 import org.eclipse.gef.GraphicalEditPart;
 import org.eclipse.gef.Request;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gef.editpolicies.LayoutEditPolicy;
 import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
 import org.eclipse.gef.requests.CreateRequest;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.AbstractBorderedShapeEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.IBorderItemEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.BorderItemSelectionEditPolicy;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.DragDropEditPolicy;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
 import org.eclipse.gmf.runtime.draw2d.ui.figures.ConstrainedToolbarLayout;
 import org.eclipse.gmf.runtime.draw2d.ui.figures.IBorderItemLocator;
 import org.eclipse.gmf.runtime.draw2d.ui.figures.WrappingLabel;
 import org.eclipse.gmf.runtime.gef.ui.figures.DefaultSizeNodeFigure;
 import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.uml2.diagram.common.draw2d.BisectionBorderItemLocator;
 import org.eclipse.uml2.diagram.common.draw2d.LaneLayout;
 import org.eclipse.uml2.diagram.common.draw2d.NameAndStereotypeBlock;
 import org.eclipse.uml2.diagram.common.draw2d.StereotypeLabel2;
 import org.eclipse.uml2.diagram.common.editparts.PrimaryShapeEditPart;
 import org.eclipse.uml2.diagram.common.editpolicies.CreationEditPolicyWithCustomReparent;
 import org.eclipse.uml2.diagram.common.editpolicies.UpdateDescriptionEditPolicy;
 import org.eclipse.uml2.diagram.statemachine.edit.policies.StateMachineCanonicalEditPolicy;
 import org.eclipse.uml2.diagram.statemachine.edit.policies.StateMachineItemSemanticEditPolicy;
 import org.eclipse.uml2.diagram.statemachine.part.UMLDiagramUpdater;
 import org.eclipse.uml2.diagram.statemachine.part.UMLVisualIDRegistry;
 
 /**
  * @generated
  */
 
 public class StateMachineEditPart extends AbstractBorderedShapeEditPart implements PrimaryShapeEditPart {
 
 	/**
 	 * @generated
 	 */
 	public static final int VISUAL_ID = 2005;
 
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
 	public StateMachineEditPart(View view) {
 		super(view);
 	}
 
 	/**
 	 * @generated
 	 */
 	protected void createDefaultEditPolicies() {
 		if (UMLVisualIDRegistry.isShortcutDescendant(getNotationView())) {
 			installEditPolicy(UpdateDescriptionEditPolicy.ROLE, new UpdateDescriptionEditPolicy(UMLDiagramUpdater.TYPED_ADAPTER, true));
 		}
 		installEditPolicy(EditPolicyRoles.CREATION_ROLE, new CreationEditPolicyWithCustomReparent(UMLVisualIDRegistry.TYPED_ADAPTER));
 		super.createDefaultEditPolicies();
 		installEditPolicy(EditPolicyRoles.SEMANTIC_ROLE, new StateMachineItemSemanticEditPolicy());
 		installEditPolicy(EditPolicyRoles.DRAG_DROP_ROLE, new DragDropEditPolicy());
 		installEditPolicy(EditPolicyRoles.CANONICAL_ROLE, new StateMachineCanonicalEditPolicy());
 		installEditPolicy(EditPolicy.LAYOUT_ROLE, createLayoutEditPolicy());
 		// XXX need an SCR to runtime to have another abstract superclass that would let children add reasonable editpolicies
 		// removeEditPolicy(org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles.CONNECTION_HANDLES_ROLE);
 
 	}
 
 	/**
 	 * @generated
 	 */
 	protected LayoutEditPolicy createLayoutEditPolicy() {
 		LayoutEditPolicy lep = new LayoutEditPolicy() {
 
 			protected EditPolicy createChildEditPolicy(EditPart child) {
 				View childView = (View) child.getModel();
 				switch (UMLVisualIDRegistry.getVisualID(childView)) {
 				case EntryPointPseudostateEditPart.VISUAL_ID:
 				case ExitPointPseudostateEditPart.VISUAL_ID:
 					return new BorderItemSelectionEditPolicy();
 				}
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
 		CompositeStateFigure figure = new CompositeStateFigure();
 		return primaryShape = figure;
 	}
 
 	/**
 	 * @generated
 	 */
 	public CompositeStateFigure getPrimaryShape() {
 		return (CompositeStateFigure) primaryShape;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected boolean addFixedChild(EditPart childEditPart) {
 		if (childEditPart instanceof StateMachineNameEditPart) {
 			((StateMachineNameEditPart) childEditPart).setLabel(getPrimaryShape().getCompositeStateFigure_name());
 			return true;
 		}
 		if (childEditPart instanceof StateMachineStereotypeEditPart) {
 			((StateMachineStereotypeEditPart) childEditPart).setLabel(getPrimaryShape().getCompositeStateFigure_stereo());
 			return true;
 		}
 		if (childEditPart instanceof EntryPointPseudostateEditPart) {
 			IBorderItemLocator locator = new BisectionBorderItemLocator(getMainFigure());
 			getBorderedFigure().getBorderItemContainer().add(((EntryPointPseudostateEditPart) childEditPart).getFigure(), locator);
 			return true;
 		}
 		if (childEditPart instanceof ExitPointPseudostateEditPart) {
 			IBorderItemLocator locator = new BisectionBorderItemLocator(getMainFigure());
 			getBorderedFigure().getBorderItemContainer().add(((ExitPointPseudostateEditPart) childEditPart).getFigure(), locator);
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected boolean removeFixedChild(EditPart childEditPart) {
 
 		if (childEditPart instanceof EntryPointPseudostateEditPart) {
 			getBorderedFigure().getBorderItemContainer().remove(((EntryPointPseudostateEditPart) childEditPart).getFigure());
 			return true;
 		}
 		if (childEditPart instanceof ExitPointPseudostateEditPart) {
 			getBorderedFigure().getBorderItemContainer().remove(((ExitPointPseudostateEditPart) childEditPart).getFigure());
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected void addChildVisual(EditPart childEditPart, int index) {
 		if (addFixedChild(childEditPart)) {
 			return;
 		}
 		super.addChildVisual(childEditPart, -1);
 	}
 
 	/**
 	 * @generated
 	 */
 	protected void removeChildVisual(EditPart childEditPart) {
 		if (removeFixedChild(childEditPart)) {
 			return;
 		}
 		super.removeChildVisual(childEditPart);
 	}
 
 	/**
 	 * @generated
 	 */
 	protected IFigure getContentPaneForGen(IGraphicalEditPart editPart) {
 		if (editPart instanceof IBorderItemEditPart) {
 			return getBorderedFigure().getBorderItemContainer();
 		}
 		return getContentPane();
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	protected IFigure getContentPaneFor(IGraphicalEditPart editPart) {
 		if (editPart instanceof EntryPointPseudostateEditPart || editPart instanceof ExitPointPseudostateEditPart) {
 			return getContentPaneForGen(editPart);
 		}
 		return contentPane;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected NodeFigure createNodePlate() {
 		DefaultSizeNodeFigure result = new DefaultSizeNodeFigure(getMapMode().DPtoLP(450), getMapMode().DPtoLP(300));
 		return result;
 	}
 
 	/**
 	 * Creates figure for this edit part.
 	 * 
 	 * Body of this method does not depend on settings in generation model
 	 * so you may safely remove <i>generated</i> tag and modify it.
 	 * 
 	 * @generated
 	 */
 	protected NodeFigure createMainFigure() {
 		NodeFigure figure = createNodePlate();
 		figure.setLayoutManager(new StackLayout());
 		IFigure shape = createNodeShape();
 		figure.add(shape);
 		contentPane = setupContentPane(shape);
 		return figure;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	protected IFigure setupContentPane(IFigure nodeShape) {
 		return getPrimaryShape().getFigureCompositeStateFigure_Body();
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
 	protected void setForegroundColor(Color color) {
 		if (primaryShape != null) {
 			primaryShape.setForegroundColor(color);
 		}
 	}
 
 	/**
 	 * @generated
 	 */
 	protected void setBackgroundColor(Color color) {
 		if (primaryShape != null) {
 			primaryShape.setBackgroundColor(color);
 		}
 	}
 
 	/**
 	 * @generated
 	 */
 	protected void setLineWidth(int width) {
 		if (primaryShape instanceof Shape) {
 			((Shape) primaryShape).setLineWidth(getMapMode().DPtoLP(width));
 		}
 	}
 
 	/**
 	 * @generated
 	 */
 	protected void setLineType(int style) {
 		if (primaryShape instanceof Shape) {
 			((Shape) primaryShape).setLineStyle(style);
 		}
 	}
 
 	/**
 	 * @generated
 	 */
 	public EditPart getPrimaryChildEditPart() {
 		return getChildBySemanticHint(UMLVisualIDRegistry.getType(StateMachineNameEditPart.VISUAL_ID));
 	}
 
 	/**
 	 * @generated
 	 */
 	protected void handleNotificationEvent(Notification event) {
 		super.handleNotificationEvent(event);
 	}
 
 	/**
 	 * @generated
 	 */
 	public class CompositeStateFigure extends RoundedRectangle {
 
 		/**
 		 * @generated
 		 */
 		private RectangleFigure fFigureCompositeStateFigure_Body;
 
 		/**
 		 * @generated
 		 */
 		private RectangleFigure fFigureCompositeStateFigure_InternalActivitiesCompartment;
 
 		/**
 		 * @generated
 		 */
 		private RectangleFigure fFigureCompositeStateFigure_InternalTransitionsCompartment;
 
 		/**
 		 * @generated
 		 */
 		private NameAndStereotypeBlock fNameAndStereotypeBlock;
 
 		/**
 		 * @generated
 		 */
 		public CompositeStateFigure() {
 
 			BorderLayout layoutThis = new BorderLayout();
 			this.setLayoutManager(layoutThis);
 
 			this.setCornerDimensions(new Dimension(getMapMode().DPtoLP(18), getMapMode().DPtoLP(18)));
 			this.setLineWidth(1);
 			this.setBorder(new MarginBorder(getMapMode().DPtoLP(4), getMapMode().DPtoLP(4), getMapMode().DPtoLP(4), getMapMode().DPtoLP(4)));
 			createContents();
 		}
 
 		/**
 		 * @generated
 		 */
 		private void createContents() {
 
 			RectangleFigure compositeStateFigure_UpperContainer_old0 = new RectangleFigure();
 			compositeStateFigure_UpperContainer_old0.setOutline(false);
 			compositeStateFigure_UpperContainer_old0.setLineWidth(1);
 
 			this.add(compositeStateFigure_UpperContainer_old0, BorderLayout.TOP);
 
 			ToolbarLayout layoutCompositeStateFigure_UpperContainer_old0 = new ToolbarLayout();
 			layoutCompositeStateFigure_UpperContainer_old0.setStretchMinorAxis(true);
 			layoutCompositeStateFigure_UpperContainer_old0.setMinorAlignment(ToolbarLayout.ALIGN_TOPLEFT);
 
 			layoutCompositeStateFigure_UpperContainer_old0.setSpacing(0);
 			layoutCompositeStateFigure_UpperContainer_old0.setVertical(true);
 
 			compositeStateFigure_UpperContainer_old0.setLayoutManager(layoutCompositeStateFigure_UpperContainer_old0);
 
 			fNameAndStereotypeBlock = new NameAndStereotypeBlock();
 
 			fNameAndStereotypeBlock.setBorder(new MarginBorder(getMapMode().DPtoLP(8), getMapMode().DPtoLP(5), getMapMode().DPtoLP(6), getMapMode().DPtoLP(5)));
 
 			compositeStateFigure_UpperContainer_old0.add(fNameAndStereotypeBlock);
 
 			fFigureCompositeStateFigure_InternalActivitiesCompartment = new RectangleFigure();
 			fFigureCompositeStateFigure_InternalActivitiesCompartment.setOutline(false);
 			fFigureCompositeStateFigure_InternalActivitiesCompartment.setLineWidth(1);
 
 			compositeStateFigure_UpperContainer_old0.add(fFigureCompositeStateFigure_InternalActivitiesCompartment);
 			fFigureCompositeStateFigure_InternalActivitiesCompartment.setLayoutManager(new StackLayout());
 
 			fFigureCompositeStateFigure_InternalTransitionsCompartment = new RectangleFigure();
 			fFigureCompositeStateFigure_InternalTransitionsCompartment.setOutline(false);
 			fFigureCompositeStateFigure_InternalTransitionsCompartment.setLineWidth(1);
 
 			compositeStateFigure_UpperContainer_old0.add(fFigureCompositeStateFigure_InternalTransitionsCompartment);
 			fFigureCompositeStateFigure_InternalTransitionsCompartment.setLayoutManager(new StackLayout());
 
 			fFigureCompositeStateFigure_Body = new RectangleFigure();
 			fFigureCompositeStateFigure_Body.setOutline(false);
 			fFigureCompositeStateFigure_Body.setLineWidth(1);
 
 			this.add(fFigureCompositeStateFigure_Body, BorderLayout.CENTER);
 
 			LaneLayout layoutFFigureCompositeStateFigure_Body = new LaneLayout();
 
 			fFigureCompositeStateFigure_Body.setLayoutManager(layoutFFigureCompositeStateFigure_Body);
 
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
 
 		/**
 		 * @generated
 		 */
 		public RectangleFigure getFigureCompositeStateFigure_Body() {
 			return fFigureCompositeStateFigure_Body;
 		}
 
 		/**
 		 * @generated
 		 */
 		public RectangleFigure getFigureCompositeStateFigure_InternalActivitiesCompartment() {
 			return fFigureCompositeStateFigure_InternalActivitiesCompartment;
 		}
 
 		/**
 		 * @generated
 		 */
 		public RectangleFigure getFigureCompositeStateFigure_InternalTransitionsCompartment() {
 			return fFigureCompositeStateFigure_InternalTransitionsCompartment;
 		}
 
 		/**
 		 * @generated
 		 */
 		public NameAndStereotypeBlock getNameAndStereotypeBlock() {
 			return fNameAndStereotypeBlock;
 		}
 
 		/**
 		 * @generated
 		 */
 		public StereotypeLabel2 getCompositeStateFigure_stereo() {
 			return getNameAndStereotypeBlock().getStereotypeLabel();
 		}
 
 		/**
 		 * @generated
 		 */
 		public WrappingLabel getCompositeStateFigure_name() {
			return getNameAndStereotypeBlock().getStereotypeLabel();
 		}
 
 	}
 
 	/**
 	 * @generated
 	 */
 	protected void reorderChild(EditPart child, int index) {
 		// Save the constraint of the child so that it does not
 		// get lost during the remove and re-add.
 		IFigure childFigure = ((GraphicalEditPart) child).getFigure();
 		LayoutManager layout = getContentPaneFor((IGraphicalEditPart) child).getLayoutManager();
 		Object constraint = null;
 		if (layout != null) {
 			constraint = layout.getConstraint(childFigure);
 		}
 		super.reorderChild(child, index);
 		setLayoutConstraint(child, childFigure, constraint);
 	}
 
 }
