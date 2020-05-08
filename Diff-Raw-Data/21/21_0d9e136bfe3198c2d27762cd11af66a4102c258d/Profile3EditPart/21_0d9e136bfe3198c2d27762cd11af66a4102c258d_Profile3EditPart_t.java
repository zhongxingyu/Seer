 package org.eclipse.uml2.diagram.profile.edit.parts;
 
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.RectangleFigure;
 import org.eclipse.draw2d.Shape;
 import org.eclipse.draw2d.StackLayout;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.PointList;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.EditPolicy;
 import org.eclipse.gef.Request;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gef.editpolicies.LayoutEditPolicy;
 import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
 import org.eclipse.gef.requests.CreateRequest;
 import org.eclipse.gmf.internal.codegen.draw2d.GridLayout;
 import org.eclipse.gmf.internal.codegen.draw2d.GridLayoutData;
 import org.eclipse.gmf.runtime.diagram.core.edithelpers.CreateElementRequestAdapter;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeNodeEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.CreationEditPolicy;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
 import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest;
 import org.eclipse.gmf.runtime.draw2d.ui.figures.ConstrainedToolbarLayout;
 import org.eclipse.gmf.runtime.draw2d.ui.figures.WrapLabel;
 import org.eclipse.gmf.runtime.emf.type.core.IElementType;
 import org.eclipse.gmf.runtime.gef.ui.figures.DefaultSizeNodeFigure;
 import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.uml2.diagram.common.editpolicies.DoNothingEditPolicy;
 import org.eclipse.uml2.diagram.common.editpolicies.UnmovableUnselectableShapeEditPolicy;
 import org.eclipse.uml2.diagram.profile.edit.policies.Profile3ItemSemanticEditPolicy;
 import org.eclipse.uml2.diagram.profile.part.UMLVisualIDRegistry;
 import org.eclipse.uml2.diagram.profile.providers.UMLElementTypes;
 
 /**
  * @generated
  */
 public class Profile3EditPart extends ShapeNodeEditPart {
 
 	/**
 	 * @generated
 	 */
 	public static final int VISUAL_ID = 2007;
 
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
 	public Profile3EditPart(View view) {
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
 						if (type == UMLElementTypes.ElementImport_3009) {
 							EditPart compartmentEditPart = getChildBySemanticHint(UMLVisualIDRegistry.getType(ProfileProfileLabelsEditPart.VISUAL_ID));
 							return compartmentEditPart == null ? null : compartmentEditPart.getCommand(request);
 						}
 					}
 					return super.getCommand(request);
 				}
 				return null;
 			}
 		});
 		super.createDefaultEditPolicies();
 
 		installEditPolicy(EditPolicyRoles.SEMANTIC_ROLE, new Profile3ItemSemanticEditPolicy());
 		installEditPolicy(EditPolicy.LAYOUT_ROLE, createLayoutEditPolicy());
 		installEditPolicy("ConnectionHandlesPolicy", new DoNothingEditPolicy()); //$NON-NLS-1$
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
 		ProfilelabelsFigure figure = new ProfilelabelsFigure();
 		return primaryShape = figure;
 	}
 
 	/**
 	 * @generated
 	 */
 	public ProfilelabelsFigure getPrimaryShape() {
 		return (ProfilelabelsFigure) primaryShape;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected boolean addFixedChild(EditPart childEditPart) {
 		if (childEditPart instanceof ProfileName2EditPart) {
 			((ProfileName2EditPart) childEditPart).setLabel(getPrimaryShape().getFigureProfileLabelsFigure_NameFigure());
 			return true;
 		}
 		if (childEditPart instanceof ProfileProfileLabelsEditPart) {
 			IFigure pane = getPrimaryShape().getFigureProfileLabelCompartmentFigure();
 			setupContentPane(pane); // FIXME each comparment should handle his content pane in his own way 
 			pane.add(((ProfileProfileLabelsEditPart) childEditPart).getFigure());
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected boolean removeFixedChild(EditPart childEditPart) {
 
 		if (childEditPart instanceof ProfileProfileLabelsEditPart) {
 			IFigure pane = getPrimaryShape().getFigureProfileLabelCompartmentFigure();
 			pane.remove(((ProfileProfileLabelsEditPart) childEditPart).getFigure());
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
 
 		if (editPart instanceof ProfileProfileLabelsEditPart) {
 			return getPrimaryShape().getFigureProfileLabelCompartmentFigure();
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
 	 * @generated
 	 */
 	public EditPolicy getPrimaryDragEditPolicy() {
 		return new UnmovableUnselectableShapeEditPolicy();
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
 	public EditPart getPrimaryChildEditPart() {
 		return getChildBySemanticHint(UMLVisualIDRegistry.getType(ProfileName2EditPart.VISUAL_ID));
 	}
 
 	/**
 	 * @generated
 	 */
 	public class ProfilelabelsFigure extends Shape {
 
 		/**
 		 * @generated
 		 */
 		public ProfilelabelsFigure() {
 
 			GridLayout layoutThis = new GridLayout();
 			layoutThis.numColumns = 1;
 			layoutThis.makeColumnsEqualWidth = true;
 			this.setLayoutManager(layoutThis);
 
 			this.addPoint(new Point(getMapMode().DPtoLP(0), getMapMode().DPtoLP(0)));
 			this.addPoint(new Point(getMapMode().DPtoLP(0), getMapMode().DPtoLP(40)));
 			this.addPoint(new Point(getMapMode().DPtoLP(35), getMapMode().DPtoLP(40)));
 			this.addPoint(new Point(getMapMode().DPtoLP(40), getMapMode().DPtoLP(35)));
 			this.addPoint(new Point(getMapMode().DPtoLP(40), getMapMode().DPtoLP(0)));
 			this.setFill(true);
 			this.setFillXOR(false);
 			this.setOutline(true);
 			this.setOutlineXOR(false);
 			this.setLineWidth(1);
 			this.setLineStyle(Graphics.LINE_SOLID);
 			createContents();
 		}
 
 		/**
 		 * @generated
 		 */
 		private void createContents() {
 
 			WrapLabel profileLabels_profileFigure0 = new WrapLabel();
 			profileLabels_profileFigure0.setText("\u00ABprofile\u00BB");
 
 			GridLayoutData constraintProfileLabels_profileFigure0 = new GridLayoutData();
 			constraintProfileLabels_profileFigure0.verticalAlignment = GridLayoutData.CENTER;
 			constraintProfileLabels_profileFigure0.horizontalAlignment = GridLayoutData.CENTER;
 			constraintProfileLabels_profileFigure0.horizontalIndent = 0;
 			constraintProfileLabels_profileFigure0.horizontalSpan = 1;
 			constraintProfileLabels_profileFigure0.verticalSpan = 1;
 			constraintProfileLabels_profileFigure0.grabExcessHorizontalSpace = false;
 			constraintProfileLabels_profileFigure0.grabExcessVerticalSpace = false;
 			this.add(profileLabels_profileFigure0, constraintProfileLabels_profileFigure0);
 
 			WrapLabel profileLabelsFigure_NameFigure0 = new WrapLabel();
 			profileLabelsFigure_NameFigure0.setText("");
 
 			GridLayoutData constraintProfileLabelsFigure_NameFigure0 = new GridLayoutData();
 			constraintProfileLabelsFigure_NameFigure0.verticalAlignment = GridLayoutData.CENTER;
 			constraintProfileLabelsFigure_NameFigure0.horizontalAlignment = GridLayoutData.CENTER;
 			constraintProfileLabelsFigure_NameFigure0.horizontalIndent = 0;
 			constraintProfileLabelsFigure_NameFigure0.horizontalSpan = 1;
 			constraintProfileLabelsFigure_NameFigure0.verticalSpan = 1;
 			constraintProfileLabelsFigure_NameFigure0.grabExcessHorizontalSpace = false;
 			constraintProfileLabelsFigure_NameFigure0.grabExcessVerticalSpace = false;
 			this.add(profileLabelsFigure_NameFigure0, constraintProfileLabelsFigure_NameFigure0);
 
 			setFigureProfileLabelsFigure_NameFigure(profileLabelsFigure_NameFigure0);
 
 			RectangleFigure profileLabelCompartmentFigure0 = new RectangleFigure();
 			profileLabelCompartmentFigure0.setFill(false);
 			profileLabelCompartmentFigure0.setFillXOR(false);
 			profileLabelCompartmentFigure0.setOutline(false);
 			profileLabelCompartmentFigure0.setOutlineXOR(false);
 			profileLabelCompartmentFigure0.setLineWidth(1);
 			profileLabelCompartmentFigure0.setLineStyle(Graphics.LINE_SOLID);
 
 			GridLayoutData constraintProfileLabelCompartmentFigure0 = new GridLayoutData();
 			constraintProfileLabelCompartmentFigure0.verticalAlignment = GridLayoutData.CENTER;
 			constraintProfileLabelCompartmentFigure0.horizontalAlignment = GridLayoutData.CENTER;
 			constraintProfileLabelCompartmentFigure0.horizontalIndent = 0;
 			constraintProfileLabelCompartmentFigure0.horizontalSpan = 1;
 			constraintProfileLabelCompartmentFigure0.verticalSpan = 1;
 			constraintProfileLabelCompartmentFigure0.grabExcessHorizontalSpace = false;
 			constraintProfileLabelCompartmentFigure0.grabExcessVerticalSpace = false;
 			this.add(profileLabelCompartmentFigure0, constraintProfileLabelCompartmentFigure0);
 
 			setFigureProfileLabelCompartmentFigure(profileLabelCompartmentFigure0);
 
 		}
 
 		/**
 		 * @generated
 		 */
 		private WrapLabel fProfileLabelsFigure_NameFigure;
 
 		/**
 		 * @generated
 		 */
 		public WrapLabel getFigureProfileLabelsFigure_NameFigure() {
 			return fProfileLabelsFigure_NameFigure;
 		}
 
 		/**
 		 * @generated
 		 */
 		private void setFigureProfileLabelsFigure_NameFigure(WrapLabel fig) {
 			fProfileLabelsFigure_NameFigure = fig;
 		}
 
 		/**
 		 * @generated
 		 */
 		private RectangleFigure fProfileLabelCompartmentFigure;
 
 		/**
 		 * @generated
 		 */
 		public RectangleFigure getFigureProfileLabelCompartmentFigure() {
 			return fProfileLabelCompartmentFigure;
 		}
 
 		/**
 		 * @generated
 		 */
 		private void setFigureProfileLabelCompartmentFigure(RectangleFigure fig) {
 			fProfileLabelCompartmentFigure = fig;
 		}
 
 		/**
 		 * @generated
 		 */
 		private final PointList myTemplate = new PointList();
 
 		/**
 		 * @generated
 		 */
 		private Rectangle myTemplateBounds;
 
 		/**
 		 * @generated
 		 */
 		public void addPoint(Point point) {
 			myTemplate.addPoint(point);
 			myTemplateBounds = null;
 		}
 
 		/**
 		 * @generated
 		 */
 		protected void fillShape(Graphics graphics) {
 			Rectangle bounds = getBounds();
 			graphics.pushState();
 			graphics.translate(bounds.x, bounds.y);
 			graphics.fillPolygon(scalePointList());
 			graphics.popState();
 		}
 
 		/**
 		 * @generated
 		 */
 		protected void outlineShape(Graphics graphics) {
 			Rectangle bounds = getBounds();
 			graphics.pushState();
 			graphics.translate(bounds.x, bounds.y);
 			graphics.drawPolygon(scalePointList());
 			graphics.popState();
 		}
 
 		/**
 		 * @generated
 		 */
 		private Rectangle getTemplateBounds() {
 			if (myTemplateBounds == null) {
 				myTemplateBounds = myTemplate.getBounds().getCopy().union(0, 0);
 				//just safety -- we are going to use this as divider 
 				if (myTemplateBounds.width < 1) {
 					myTemplateBounds.width = 1;
 				}
 				if (myTemplateBounds.height < 1) {
 					myTemplateBounds.height = 1;
 				}
 			}
 			return myTemplateBounds;
 		}
 
 		/**
 		 * @generated
 		 */
 		private int[] scalePointList() {
 			Rectangle pointsBounds = getTemplateBounds();
 			Rectangle actualBounds = getBounds();
 
 			float xScale = ((float) actualBounds.width) / pointsBounds.width;
 			float yScale = ((float) actualBounds.height) / pointsBounds.height;
 
 			if (xScale == 1 && yScale == 1) {
 				return myTemplate.toIntArray();
 			}
 			int[] scaled = (int[]) myTemplate.toIntArray().clone();
 			for (int i = 0; i < scaled.length; i += 2) {
 				scaled[i] = (int) Math.floor(scaled[i] * xScale);
 				scaled[i + 1] = (int) Math.floor(scaled[i + 1] * yScale);
 			}
 			return scaled;
 		}
 
 	}
 
 }
