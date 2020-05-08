 package org.eclipse.uml2.diagram.statemachine.edit.parts;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.draw2d.BorderLayout;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.Label;
 import org.eclipse.draw2d.MarginBorder;
 import org.eclipse.draw2d.RectangleFigure;
 import org.eclipse.draw2d.RoundedRectangle;
 import org.eclipse.draw2d.Shape;
 import org.eclipse.draw2d.StackLayout;
 import org.eclipse.draw2d.ToolbarLayout;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.transaction.RunnableWithResult;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.EditPolicy;
 import org.eclipse.gef.Request;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gef.editpolicies.LayoutEditPolicy;
 import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
 import org.eclipse.gef.requests.CreateRequest;
 import org.eclipse.gef.requests.DirectEditRequest;
 import org.eclipse.gmf.runtime.diagram.core.edithelpers.CreateElementRequestAdapter;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeNodeEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.DragDropEditPolicy;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
 import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest;
 import org.eclipse.gmf.runtime.diagram.ui.requests.RequestConstants;
 import org.eclipse.gmf.runtime.draw2d.ui.figures.ConstrainedToolbarLayout;
 import org.eclipse.gmf.runtime.draw2d.ui.figures.WrappingLabel;
 import org.eclipse.gmf.runtime.emf.type.core.IElementType;
 import org.eclipse.gmf.runtime.gef.ui.figures.DefaultSizeNodeFigure;
 import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.uml2.diagram.common.draw2d.LaneLayout;
 import org.eclipse.uml2.diagram.common.draw2d.NameAndStereotypeBlock;
 import org.eclipse.uml2.diagram.common.draw2d.StereotypeLabel2;
 import org.eclipse.uml2.diagram.common.editparts.PrimaryShapeEditPart;
 import org.eclipse.uml2.diagram.common.editpolicies.CreationEditPolicyWithCustomReparent;
 import org.eclipse.uml2.diagram.common.editpolicies.LaneLayoutEditPolicy;
 import org.eclipse.uml2.diagram.common.editpolicies.UpdateDescriptionEditPolicy;
 import org.eclipse.uml2.diagram.statemachine.edit.policies.CompositeStateItemSemanticEditPolicy;
 import org.eclipse.uml2.diagram.statemachine.edit.policies.State2CanonicalEditPolicy;
 import org.eclipse.uml2.diagram.statemachine.part.UMLDiagramUpdater;
 import org.eclipse.uml2.diagram.statemachine.part.UMLVisualIDRegistry;
 import org.eclipse.uml2.diagram.statemachine.providers.UMLElementTypes;
 
 /**
  * @generated
  */
 
 public class CompositeStateEditPart extends ShapeNodeEditPart implements PrimaryShapeEditPart {
 
 	/**
 	 * @generated
 	 */
 	public static final int VISUAL_ID = 3012;
 
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
 	public CompositeStateEditPart(View view) {
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
 		installEditPolicy(EditPolicyRoles.SEMANTIC_ROLE, new CompositeStateItemSemanticEditPolicy());
 		installEditPolicy(EditPolicyRoles.DRAG_DROP_ROLE, new DragDropEditPolicy());
 		installEditPolicy(EditPolicyRoles.CANONICAL_ROLE, new State2CanonicalEditPolicy());
 		installEditPolicy(EditPolicy.LAYOUT_ROLE, createLayoutEditPolicy());
 		installEditPolicy("LayoutEditPolicy", new LaneLayoutEditPolicy()); //$NON-NLS-1$
 		// XXX need an SCR to runtime to have another abstract superclass that would let children add reasonable editpolicies
 		// removeEditPolicy(org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles.CONNECTION_HANDLES_ROLE);
 
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
 		if (childEditPart instanceof CompositeStateNameEditPart) {
 			((CompositeStateNameEditPart) childEditPart).setLabel(getPrimaryShape().getCompositeStateFigure_name());
 			return true;
 		}
 		if (childEditPart instanceof CompositeStateStereotypeEditPart) {
 			((CompositeStateStereotypeEditPart) childEditPart).setLabel(getPrimaryShape().getCompositeStateFigure_stereo());
 			return true;
 		}
 		if (childEditPart instanceof CompositeState_InternalActivitiesEditPart) {
 			IFigure pane = getPrimaryShape().getFigureCompositeStateFigure_InternalActivitiesCompartment();
 			setupContentPane(pane); // FIXME each comparment should handle his content pane in his own way 
 			pane.add(((CompositeState_InternalActivitiesEditPart) childEditPart).getFigure());
 			return true;
 		}
 		if (childEditPart instanceof CompositeState_InternalTransitionsEditPart) {
 			IFigure pane = getPrimaryShape().getFigureCompositeStateFigure_InternalTransitionsCompartment();
 			setupContentPane(pane); // FIXME each comparment should handle his content pane in his own way 
 			pane.add(((CompositeState_InternalTransitionsEditPart) childEditPart).getFigure());
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected boolean removeFixedChild(EditPart childEditPart) {
 
 		if (childEditPart instanceof CompositeState_InternalActivitiesEditPart) {
 			IFigure pane = getPrimaryShape().getFigureCompositeStateFigure_InternalActivitiesCompartment();
 			pane.remove(((CompositeState_InternalActivitiesEditPart) childEditPart).getFigure());
 			return true;
 		}
 		if (childEditPart instanceof CompositeState_InternalTransitionsEditPart) {
 			IFigure pane = getPrimaryShape().getFigureCompositeStateFigure_InternalTransitionsCompartment();
 			pane.remove(((CompositeState_InternalTransitionsEditPart) childEditPart).getFigure());
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
 	protected IFigure getContentPaneFor(IGraphicalEditPart editPart) {
 		if (editPart instanceof CompositeState_InternalActivitiesEditPart) {
 			return getPrimaryShape().getFigureCompositeStateFigure_InternalActivitiesCompartment();
 		}
 		if (editPart instanceof CompositeState_InternalTransitionsEditPart) {
 			return getPrimaryShape().getFigureCompositeStateFigure_InternalTransitionsCompartment();
 		}
 		return getContentPane();
 	}
 
 	/**
 	 * @generated
 	 */
 	protected NodeFigure createNodePlate() {
 		DefaultSizeNodeFigure result = new DefaultSizeNodeFigure(getMapMode().DPtoLP(40), getMapMode().DPtoLP(40));
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
 	protected NodeFigure createNodeFigure() {
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
 		return getChildBySemanticHint(UMLVisualIDRegistry.getType(CompositeStateNameEditPart.VISUAL_ID));
 	}
 
 	/**
 	 * @generated
 	 */
 	public List/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/getMARelTypesOnSource() {
 		List/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/types = new ArrayList/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/();
 		types.add(UMLElementTypes.Transition_4001);
 		return types;
 	}
 
 	/**
 	 * @generated
 	 */
 	public List/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/getMARelTypesOnSourceAndTarget(IGraphicalEditPart targetEditPart) {
 		List/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/types = new ArrayList/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/();
 		if (targetEditPart instanceof SimpleStateEditPart) {
 			types.add(UMLElementTypes.Transition_4001);
 		}
 		if (targetEditPart instanceof org.eclipse.uml2.diagram.statemachine.edit.parts.CompositeStateEditPart) {
 			types.add(UMLElementTypes.Transition_4001);
 		}
 		if (targetEditPart instanceof SubmachineStateEditPart) {
 			types.add(UMLElementTypes.Transition_4001);
 		}
 		if (targetEditPart instanceof EntryConnectionPointReferenceEditPart) {
 			types.add(UMLElementTypes.Transition_4001);
 		}
 		if (targetEditPart instanceof ExitConnectionPointReferenceEditPart) {
 			types.add(UMLElementTypes.Transition_4001);
 		}
 		if (targetEditPart instanceof FinalStateEditPart) {
 			types.add(UMLElementTypes.Transition_4001);
 		}
 		if (targetEditPart instanceof InitialPseudostateEditPart) {
 			types.add(UMLElementTypes.Transition_4001);
 		}
 		if (targetEditPart instanceof ShallowHistoryPseudostateEditPart) {
 			types.add(UMLElementTypes.Transition_4001);
 		}
 		if (targetEditPart instanceof DeepHistoryPseudostateEditPart) {
 			types.add(UMLElementTypes.Transition_4001);
 		}
 		if (targetEditPart instanceof ForkPseudostateEditPart) {
 			types.add(UMLElementTypes.Transition_4001);
 		}
 		if (targetEditPart instanceof JoinPseudostateEditPart) {
 			types.add(UMLElementTypes.Transition_4001);
 		}
 		if (targetEditPart instanceof JunctionPseudostateEditPart) {
 			types.add(UMLElementTypes.Transition_4001);
 		}
 		if (targetEditPart instanceof ChoicePseudostateEditPart) {
 			types.add(UMLElementTypes.Transition_4001);
 		}
 		if (targetEditPart instanceof TerminatePseudostateEditPart) {
 			types.add(UMLElementTypes.Transition_4001);
 		}
 		if (targetEditPart instanceof EntryPointPseudostateEditPart) {
 			types.add(UMLElementTypes.Transition_4001);
 		}
 		if (targetEditPart instanceof ExitPointPseudostateEditPart) {
 			types.add(UMLElementTypes.Transition_4001);
 		}
 		return types;
 	}
 
 	/**
 	 * @generated
 	 */
 	public List/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/getMATypesForTarget(IElementType relationshipType) {
 		List/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/types = new ArrayList/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/();
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.State_3001);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.State_3012);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.State_3016);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.ConnectionPointReference_3017);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.ConnectionPointReference_3018);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.FinalState_3003);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.Pseudostate_3004);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.Pseudostate_3005);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.Pseudostate_3006);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.Pseudostate_3007);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.Pseudostate_3008);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.Pseudostate_3009);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.Pseudostate_3010);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.Pseudostate_3011);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.Pseudostate_3014);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.Pseudostate_3015);
 		}
 		return types;
 	}
 
 	/**
 	 * @generated
 	 */
 	public List/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/getMARelTypesOnTarget() {
 		List/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/types = new ArrayList/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/();
 		types.add(UMLElementTypes.Transition_4001);
 		return types;
 	}
 
 	/**
 	 * @generated
 	 */
 	public List/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/getMATypesForSource(IElementType relationshipType) {
 		List/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/types = new ArrayList/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/();
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.State_3001);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.State_3012);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.State_3016);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.ConnectionPointReference_3017);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.ConnectionPointReference_3018);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.FinalState_3003);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.Pseudostate_3004);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.Pseudostate_3005);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.Pseudostate_3006);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.Pseudostate_3007);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.Pseudostate_3008);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.Pseudostate_3009);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.Pseudostate_3010);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.Pseudostate_3011);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.Pseudostate_3014);
 		}
 		if (relationshipType == UMLElementTypes.Transition_4001) {
 			types.add(UMLElementTypes.Pseudostate_3015);
 		}
 		return types;
 	}
 
 	/**
 	 * @generated
 	 */
 	public EditPart getTargetEditPart(Request request) {
 		if (request instanceof CreateViewAndElementRequest) {
 			CreateElementRequestAdapter adapter = ((CreateViewAndElementRequest) request).getViewAndElementDescriptor().getCreateElementRequestAdapter();
 			IElementType type = (IElementType) adapter.getAdapter(IElementType.class);
 			if (type == UMLElementTypes.Behavior_3019) {
 				return getChildBySemanticHint(UMLVisualIDRegistry.getType(CompositeState_InternalActivitiesEditPart.VISUAL_ID));
 			}
 			if (type == UMLElementTypes.Behavior_3020) {
 				return getChildBySemanticHint(UMLVisualIDRegistry.getType(CompositeState_InternalActivitiesEditPart.VISUAL_ID));
 			}
 			if (type == UMLElementTypes.Behavior_3021) {
 				return getChildBySemanticHint(UMLVisualIDRegistry.getType(CompositeState_InternalActivitiesEditPart.VISUAL_ID));
 			}
 			if (type == UMLElementTypes.Transition_3022) {
 				return getChildBySemanticHint(UMLVisualIDRegistry.getType(CompositeState_InternalTransitionsEditPart.VISUAL_ID));
 			}
 		}
 		return super.getTargetEditPart(request);
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
 	protected void performDirectEditRequest(final Request request) {
 		EditPart editPart = this;
 		if (request instanceof DirectEditRequest) {
 			Point p = new Point(((DirectEditRequest) request).getLocation());
 			getFigure().translateToRelative(p);
 			IFigure fig = getFigure().findFigureAt(p);
 			editPart = (EditPart) getViewer().getVisualPartMap().get(fig);
 		}
 		if (editPart == this) {
 			try {
 				editPart = (EditPart) getEditingDomain().runExclusive(new RunnableWithResult.Impl() {
 
 					public void run() {
 						setResult(chooseLabelEditPartForDirectEditRequest(request));
 					}
 				});
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			if (editPart != null && editPart != this) {
 				editPart.performRequest(request);
 			}
 		}
 	}
 
 	/**
 	 * @generated
 	 */
 	protected EditPart chooseLabelEditPartForDirectEditRequest(Request request) {
 		if (request.getExtendedData().containsKey(RequestConstants.REQ_DIRECTEDIT_EXTENDEDDATA_INITIAL_CHAR)) {
 			Character initialChar = (Character) request.getExtendedData().get(RequestConstants.REQ_DIRECTEDIT_EXTENDEDDATA_INITIAL_CHAR);
 			// '<' has special meaning, because we have both name- and stereo- inplaces for single node edit part
 			// we want to activate stereotype inplace if user presses '<' (for "<< stereotype >>" 
 			// notation, also we don't include '<' and '>' into actual inplace text).
 			// If user presses any other alfanum key, we will activate name-inplace, as for all other figures
 
 			if (initialChar.charValue() == '<') {
 				EditPart result = getChildBySemanticHint(UMLVisualIDRegistry.getType(CompositeStateStereotypeEditPart.VISUAL_ID));
 				if (result != null) {
 					return result;
 				}
 			}
 		}
 		return getPrimaryChildEditPart();
 	}
 
 }
