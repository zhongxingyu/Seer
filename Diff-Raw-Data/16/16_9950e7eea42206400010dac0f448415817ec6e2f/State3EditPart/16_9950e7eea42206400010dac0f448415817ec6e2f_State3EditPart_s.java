 package org.eclipse.uml2.diagram.statemachine.edit.parts;
 
 import org.eclipse.draw2d.GridData;
 import org.eclipse.draw2d.GridLayout;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.Label;
 import org.eclipse.draw2d.MarginBorder;
 import org.eclipse.draw2d.RectangleFigure;
 import org.eclipse.draw2d.RoundedRectangle;
 import org.eclipse.draw2d.StackLayout;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.EditPolicy;
 import org.eclipse.gef.Request;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gef.editpolicies.LayoutEditPolicy;
 import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
 import org.eclipse.gef.requests.CreateRequest;
 import org.eclipse.gmf.runtime.diagram.core.edithelpers.CreateElementRequestAdapter;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.AbstractBorderedShapeEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.IBorderItemEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.BorderItemSelectionEditPolicy;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.CreationEditPolicy;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.DragDropEditPolicy;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
 import org.eclipse.gmf.runtime.diagram.ui.figures.IBorderItemLocator;
 import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest;
 import org.eclipse.gmf.runtime.draw2d.ui.figures.ConstrainedToolbarLayout;
 import org.eclipse.gmf.runtime.draw2d.ui.figures.WrapLabel;
 import org.eclipse.gmf.runtime.emf.type.core.IElementType;
 import org.eclipse.gmf.runtime.gef.ui.figures.DefaultSizeNodeFigure;
 import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.uml2.diagram.common.draw2d.BisectionBorderItemLocator;
 import org.eclipse.uml2.diagram.common.draw2d.LaneLayout;
 import org.eclipse.uml2.diagram.common.editpolicies.LaneLayoutEditPolicy;
 import org.eclipse.uml2.diagram.statemachine.edit.policies.OpenDiagramEditPolicy;
 import org.eclipse.uml2.diagram.statemachine.edit.policies.State3CanonicalEditPolicy;
 import org.eclipse.uml2.diagram.statemachine.edit.policies.State3ItemSemanticEditPolicy;
 import org.eclipse.uml2.diagram.statemachine.part.UMLVisualIDRegistry;
 import org.eclipse.uml2.diagram.statemachine.providers.UMLElementTypes;
 
 /**
  * @generated
  */
 public class State3EditPart extends AbstractBorderedShapeEditPart {
 
 	/**
 	 * @generated
 	 */
 	public static final int VISUAL_ID = 3016;
 
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
 	public State3EditPart(View view) {
 		super(view);
 	}
 
 	/**
 	 * @generated
 	 */
 	protected void createDefaultEditPolicies() {
 		installEditPolicy(EditPolicyRoles.CREATION_ROLE, new CreationEditPolicy() {
 
 			public Command getCommand(Request request) {
 				if (understandsRequest(request)) {
 					if (request instanceof CreateViewAndElementRequest) {
 						CreateElementRequestAdapter adapter = ((CreateViewAndElementRequest) request).getViewAndElementDescriptor().getCreateElementRequestAdapter();
 						IElementType type = (IElementType) adapter.getAdapter(IElementType.class);
 						if (type == UMLElementTypes.Behavior_3019) {
 							EditPart compartmentEditPart = getChildBySemanticHint(UMLVisualIDRegistry.getType(StateCompositeState_InternalActivities2EditPart.VISUAL_ID));
 							return compartmentEditPart == null ? null : compartmentEditPart.getCommand(request);
 						}
 						if (type == UMLElementTypes.Behavior_3020) {
 							EditPart compartmentEditPart = getChildBySemanticHint(UMLVisualIDRegistry.getType(StateCompositeState_InternalActivities2EditPart.VISUAL_ID));
 							return compartmentEditPart == null ? null : compartmentEditPart.getCommand(request);
 						}
 						if (type == UMLElementTypes.Behavior_3021) {
 							EditPart compartmentEditPart = getChildBySemanticHint(UMLVisualIDRegistry.getType(StateCompositeState_InternalActivities2EditPart.VISUAL_ID));
 							return compartmentEditPart == null ? null : compartmentEditPart.getCommand(request);
 						}
 					}
 					return super.getCommand(request);
 				}
 				return null;
 			}
 		});
 		super.createDefaultEditPolicies();
 		installEditPolicy(EditPolicyRoles.SEMANTIC_ROLE, new State3ItemSemanticEditPolicy());
 		installEditPolicy(EditPolicyRoles.DRAG_DROP_ROLE, new DragDropEditPolicy());
 		installEditPolicy(EditPolicyRoles.CANONICAL_ROLE, new State3CanonicalEditPolicy());
 		installEditPolicy(EditPolicy.LAYOUT_ROLE, createLayoutEditPolicy());
 		installEditPolicy(EditPolicyRoles.OPEN_ROLE, new OpenDiagramEditPolicy());
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
 				if (child instanceof IBorderItemEditPart) {
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
 		if (childEditPart instanceof StateName2EditPart) {
 			((StateName2EditPart) childEditPart).setLabel(getPrimaryShape().getFigureCompositeStateFigure_name());
 			return true;
 		}
 		if (childEditPart instanceof StateCompositeState_InternalActivities2EditPart) {
 			IFigure pane = getPrimaryShape().getFigureCompositeStateFigure_InternalActivitiesCompartment();
 			setupContentPane(pane); // FIXME each comparment should handle his content pane in his own way 
 			pane.add(((StateCompositeState_InternalActivities2EditPart) childEditPart).getFigure());
 			return true;
 		}
 		if (childEditPart instanceof ConnectionPointReferenceEditPart) {
 			org.eclipse.gmf.runtime.draw2d.ui.figures.IBorderItemLocator locator = new BisectionBorderItemLocator(getMainFigure());
 			getBorderedFigure().getBorderItemContainer().add(((ConnectionPointReferenceEditPart) childEditPart).getFigure(), locator);
 			return true;
 		}
 		if (childEditPart instanceof ConnectionPointReference2EditPart) {
 			org.eclipse.gmf.runtime.draw2d.ui.figures.IBorderItemLocator locator = new BisectionBorderItemLocator(getMainFigure());
 			getBorderedFigure().getBorderItemContainer().add(((ConnectionPointReference2EditPart) childEditPart).getFigure(), locator);
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected boolean removeFixedChild(EditPart childEditPart) {
 
 		if (childEditPart instanceof StateCompositeState_InternalActivities2EditPart) {
 			IFigure pane = getPrimaryShape().getFigureCompositeStateFigure_InternalActivitiesCompartment();
 			pane.remove(((StateCompositeState_InternalActivities2EditPart) childEditPart).getFigure());
 			return true;
 		}
 		if (childEditPart instanceof ConnectionPointReferenceEditPart) {
 			getBorderedFigure().getBorderItemContainer().remove(((ConnectionPointReferenceEditPart) childEditPart).getFigure());
 			return true;
 		}
 		if (childEditPart instanceof ConnectionPointReference2EditPart) {
 			getBorderedFigure().getBorderItemContainer().remove(((ConnectionPointReference2EditPart) childEditPart).getFigure());
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
 
 		if (editPart instanceof StateCompositeState_InternalActivities2EditPart) {
 			return getPrimaryShape().getFigureCompositeStateFigure_InternalActivitiesCompartment();
 		}
 		if (editPart instanceof ConnectionPointReferenceEditPart) {
 			return getBorderedFigure().getBorderItemContainer();
 		}
 		if (editPart instanceof ConnectionPointReference2EditPart) {
 			return getBorderedFigure().getBorderItemContainer();
 		}
 		return super.getContentPaneFor(editPart);
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
 	 * XXX: Support content pane in xPand custom templates
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
 	public EditPart getPrimaryChildEditPart() {
 		return getChildBySemanticHint(UMLVisualIDRegistry.getType(StateName2EditPart.VISUAL_ID));
 	}
 
 	/**
 	 * @generated
 	 */
 	public class CompositeStateFigure extends RoundedRectangle {
 
 		/**
 		 * @generated
 		 */
 		private Label fFigureCompositeStateFigure_name;
 
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
 		public CompositeStateFigure() {
 
 			GridLayout layoutThis = new GridLayout();
 			layoutThis.numColumns = 1;
 			layoutThis.makeColumnsEqualWidth = true;
 			layoutThis.horizontalSpacing = 0;
 			layoutThis.verticalSpacing = 0;
 			layoutThis.marginWidth = 0;
 			layoutThis.marginHeight = 0;
 			this.setLayoutManager(layoutThis);
 
 			this.setCornerDimensions(new Dimension(getMapMode().DPtoLP(18), getMapMode().DPtoLP(18)));
 			this.setBorder(new MarginBorder(getMapMode().DPtoLP(4), getMapMode().DPtoLP(4), getMapMode().DPtoLP(4), getMapMode().DPtoLP(4)));
 			createContents();
 		}
 
 		/**
 		 * @generated
 		 */
 		private void createContents() {
 
 			RectangleFigure compositeStateFigure_NameContainer0 = new RectangleFigure();
 			compositeStateFigure_NameContainer0.setFill(false);
 			compositeStateFigure_NameContainer0.setOutline(false);
 
 			GridData constraintCompositeStateFigure_NameContainer0 = new GridData();
 			constraintCompositeStateFigure_NameContainer0.verticalAlignment = GridData.FILL;
 			constraintCompositeStateFigure_NameContainer0.horizontalAlignment = GridData.FILL;
 			constraintCompositeStateFigure_NameContainer0.horizontalIndent = 0;
 			constraintCompositeStateFigure_NameContainer0.horizontalSpan = 1;
 			constraintCompositeStateFigure_NameContainer0.verticalSpan = 1;
 			constraintCompositeStateFigure_NameContainer0.grabExcessHorizontalSpace = true;
 			constraintCompositeStateFigure_NameContainer0.grabExcessVerticalSpace = false;
 			this.add(compositeStateFigure_NameContainer0, constraintCompositeStateFigure_NameContainer0);
 
 			GridLayout layoutCompositeStateFigure_NameContainer0 = new GridLayout();
 			layoutCompositeStateFigure_NameContainer0.numColumns = 1;
 			layoutCompositeStateFigure_NameContainer0.makeColumnsEqualWidth = true;
 			layoutCompositeStateFigure_NameContainer0.horizontalSpacing = 0;
 			layoutCompositeStateFigure_NameContainer0.verticalSpacing = 0;
 			layoutCompositeStateFigure_NameContainer0.marginWidth = 3;
 			layoutCompositeStateFigure_NameContainer0.marginHeight = 3;
 			compositeStateFigure_NameContainer0.setLayoutManager(layoutCompositeStateFigure_NameContainer0);
 
 			fFigureCompositeStateFigure_name = new Label();
 			fFigureCompositeStateFigure_name.setText("");
 
 			GridData constraintFFigureCompositeStateFigure_name = new GridData();
 			constraintFFigureCompositeStateFigure_name.verticalAlignment = GridData.CENTER;
 			constraintFFigureCompositeStateFigure_name.horizontalAlignment = GridData.CENTER;
 			constraintFFigureCompositeStateFigure_name.horizontalIndent = 0;
 			constraintFFigureCompositeStateFigure_name.horizontalSpan = 1;
 			constraintFFigureCompositeStateFigure_name.verticalSpan = 1;
 			constraintFFigureCompositeStateFigure_name.grabExcessHorizontalSpace = true;
 			constraintFFigureCompositeStateFigure_name.grabExcessVerticalSpace = false;
 			compositeStateFigure_NameContainer0.add(fFigureCompositeStateFigure_name, constraintFFigureCompositeStateFigure_name);
 
 			fFigureCompositeStateFigure_InternalActivitiesCompartment = new RectangleFigure();
 			fFigureCompositeStateFigure_InternalActivitiesCompartment.setOutline(false);
 
 			GridData constraintFFigureCompositeStateFigure_InternalActivitiesCompartment = new GridData();
 			constraintFFigureCompositeStateFigure_InternalActivitiesCompartment.verticalAlignment = GridData.FILL;
 			constraintFFigureCompositeStateFigure_InternalActivitiesCompartment.horizontalAlignment = GridData.FILL;
 			constraintFFigureCompositeStateFigure_InternalActivitiesCompartment.horizontalIndent = 0;
 			constraintFFigureCompositeStateFigure_InternalActivitiesCompartment.horizontalSpan = 1;
 			constraintFFigureCompositeStateFigure_InternalActivitiesCompartment.verticalSpan = 1;
 			constraintFFigureCompositeStateFigure_InternalActivitiesCompartment.grabExcessHorizontalSpace = true;
 			constraintFFigureCompositeStateFigure_InternalActivitiesCompartment.grabExcessVerticalSpace = false;
 			this.add(fFigureCompositeStateFigure_InternalActivitiesCompartment, constraintFFigureCompositeStateFigure_InternalActivitiesCompartment);
 
 			fFigureCompositeStateFigure_InternalActivitiesCompartment.setLayoutManager(new StackLayout());
 
 			fFigureCompositeStateFigure_Body = new RectangleFigure();
 			fFigureCompositeStateFigure_Body.setOutline(false);
 
 			GridData constraintFFigureCompositeStateFigure_Body = new GridData();
 			constraintFFigureCompositeStateFigure_Body.verticalAlignment = GridData.FILL;
 			constraintFFigureCompositeStateFigure_Body.horizontalAlignment = GridData.FILL;
 			constraintFFigureCompositeStateFigure_Body.horizontalIndent = 0;
 			constraintFFigureCompositeStateFigure_Body.horizontalSpan = 1;
 			constraintFFigureCompositeStateFigure_Body.verticalSpan = 1;
 			constraintFFigureCompositeStateFigure_Body.grabExcessHorizontalSpace = true;
 			constraintFFigureCompositeStateFigure_Body.grabExcessVerticalSpace = true;
 			this.add(fFigureCompositeStateFigure_Body, constraintFFigureCompositeStateFigure_Body);
 
 			LaneLayout layoutFFigureCompositeStateFigure_Body = new LaneLayout();
 
 			fFigureCompositeStateFigure_Body.setLayoutManager(layoutFFigureCompositeStateFigure_Body);
 
 		}
 
 		/**
 		 * @generated
 		 */
 		public Label getFigureCompositeStateFigure_name() {
 			return fFigureCompositeStateFigure_name;
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
