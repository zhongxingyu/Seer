 package org.eclipse.uml2.diagram.profile.edit.parts;
 
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.RectangleFigure;
 import org.eclipse.draw2d.StackLayout;
 import org.eclipse.draw2d.ToolbarLayout;
 import org.eclipse.draw2d.geometry.Dimension;
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
 import org.eclipse.uml2.diagram.common.draw2d.CenterLayout;
 import org.eclipse.uml2.diagram.profile.edit.policies.EnumerationItemSemanticEditPolicy;
 import org.eclipse.uml2.diagram.profile.part.UMLVisualIDRegistry;
 import org.eclipse.uml2.diagram.profile.providers.UMLElementTypes;
 
 /**
  * @generated
  */
 public class EnumerationEditPart extends ShapeNodeEditPart {
 
 	/**
 	 * @generated
 	 */
 	public static final int VISUAL_ID = 2003;
 
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
 	public EnumerationEditPart(View view) {
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
 						if (type == UMLElementTypes.EnumerationLiteral_3005) {
 							EditPart compartmentEditPart = getChildBySemanticHint(UMLVisualIDRegistry.getType(EnumerationLiteralsEditPart.VISUAL_ID));
 							return compartmentEditPart == null ? null : compartmentEditPart.getCommand(request);
 						}
 					}
 					return super.getCommand(request);
 				}
 				return null;
 			}
 		});
 		super.createDefaultEditPolicies();
 
 		installEditPolicy(EditPolicyRoles.SEMANTIC_ROLE, new EnumerationItemSemanticEditPolicy());
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
 		ClassFigure figure = new ClassFigure();
 		return primaryShape = figure;
 	}
 
 	/**
 	 * @generated
 	 */
 	public ClassFigure getPrimaryShape() {
 		return (ClassFigure) primaryShape;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected boolean addFixedChild(EditPart childEditPart) {
 		if (childEditPart instanceof EnumerationNameEditPart) {
 			((EnumerationNameEditPart) childEditPart).setLabel(getPrimaryShape().getFigureClassFigure_name());
 			return true;
 		}
 		if (childEditPart instanceof EnumerationLiteralsEditPart) {
 			IFigure pane = getPrimaryShape().getFigureClassFigure_LiteralsCompartment();
 			setupContentPane(pane); // FIXME each comparment should handle his content pane in his own way 
 			pane.add(((EnumerationLiteralsEditPart) childEditPart).getFigure());
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected boolean removeFixedChild(EditPart childEditPart) {
 
 		if (childEditPart instanceof EnumerationLiteralsEditPart) {
 			IFigure pane = getPrimaryShape().getFigureClassFigure_LiteralsCompartment();
 			pane.remove(((EnumerationLiteralsEditPart) childEditPart).getFigure());
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
 
 		if (editPart instanceof EnumerationLiteralsEditPart) {
 			return getPrimaryShape().getFigureClassFigure_LiteralsCompartment();
 		}
 		return super.getContentPaneFor(editPart);
 	}
 
 	/**
 	 * @generated
 	 */
 	protected NodeFigure createNodePlate() {
 		DefaultSizeNodeFigure result = new DefaultSizeNodeFigure(getMapMode().DPtoLP(100), getMapMode().DPtoLP(60));
 
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
 		return getChildBySemanticHint(UMLVisualIDRegistry.getType(EnumerationNameEditPart.VISUAL_ID));
 	}
 
 	/**
 	 * @generated
 	 */
 	public class ClassFigure extends RectangleFigure {
 
 		/**
 		 * @generated
 		 */
 		public ClassFigure() {
 
 			GridLayout layoutThis = new GridLayout();
 			layoutThis.numColumns = 1;
 			layoutThis.makeColumnsEqualWidth = true;
 			layoutThis.horizontalSpacing = 0;
 			layoutThis.verticalSpacing = 0;
 			layoutThis.marginWidth = 0;
 			layoutThis.marginHeight = 0;
 			this.setLayoutManager(layoutThis);
 
 			this.setFill(false);
 			this.setFillXOR(false);
 			this.setOutline(false);
 			this.setOutlineXOR(false);
 			this.setLineWidth(1);
 			this.setLineStyle(Graphics.LINE_SOLID);
 			createContents();
 		}
 
 		/**
 		 * @generated
 		 */
 		private void createContents() {
 
 			RectangleFigure classFigure_NameContainer0 = new RectangleFigure();
 			classFigure_NameContainer0.setFill(true);
 			classFigure_NameContainer0.setFillXOR(false);
 			classFigure_NameContainer0.setOutline(true);
 			classFigure_NameContainer0.setOutlineXOR(false);
 			classFigure_NameContainer0.setLineWidth(1);
 			classFigure_NameContainer0.setLineStyle(Graphics.LINE_SOLID);
 			classFigure_NameContainer0.setMinimumSize(new Dimension(getMapMode().DPtoLP(0), getMapMode().DPtoLP(25)));
 
 			GridLayoutData constraintClassFigure_NameContainer0 = new GridLayoutData();
 			constraintClassFigure_NameContainer0.verticalAlignment = GridLayoutData.FILL;
 			constraintClassFigure_NameContainer0.horizontalAlignment = GridLayoutData.FILL;
 			constraintClassFigure_NameContainer0.horizontalIndent = 0;
 			constraintClassFigure_NameContainer0.horizontalSpan = 2;
 			constraintClassFigure_NameContainer0.verticalSpan = 1;
 			constraintClassFigure_NameContainer0.grabExcessHorizontalSpace = true;
 			constraintClassFigure_NameContainer0.grabExcessVerticalSpace = false;
 			this.add(classFigure_NameContainer0, constraintClassFigure_NameContainer0);
 
 			CenterLayout layoutClassFigure_NameContainer0 = new CenterLayout();
 
 			classFigure_NameContainer0.setLayoutManager(layoutClassFigure_NameContainer0);
 
 			WrapLabel classFigure_name1 = new WrapLabel();
 			classFigure_name1.setText("");
 
 			classFigure_NameContainer0.add(classFigure_name1);
 			setFigureClassFigure_name(classFigure_name1);
 
 			RectangleFigure classFigure_Body0 = new RectangleFigure();
 			classFigure_Body0.setFill(true);
 			classFigure_Body0.setFillXOR(false);
 			classFigure_Body0.setOutline(true);
 			classFigure_Body0.setOutlineXOR(false);
 			classFigure_Body0.setLineWidth(1);
 			classFigure_Body0.setLineStyle(Graphics.LINE_SOLID);
 
 			GridLayoutData constraintClassFigure_Body0 = new GridLayoutData();
 			constraintClassFigure_Body0.verticalAlignment = GridLayoutData.FILL;
 			constraintClassFigure_Body0.horizontalAlignment = GridLayoutData.FILL;
 			constraintClassFigure_Body0.horizontalIndent = 0;
 			constraintClassFigure_Body0.horizontalSpan = 2;
 			constraintClassFigure_Body0.verticalSpan = 1;
 			constraintClassFigure_Body0.grabExcessHorizontalSpace = true;
 			constraintClassFigure_Body0.grabExcessVerticalSpace = true;
 			this.add(classFigure_Body0, constraintClassFigure_Body0);
 
 			ToolbarLayout layoutClassFigure_Body0 = new ToolbarLayout();
 			layoutClassFigure_Body0.setStretchMinorAxis(true);
 			layoutClassFigure_Body0.setMinorAlignment(ToolbarLayout.ALIGN_CENTER
 
 			);
 
 			layoutClassFigure_Body0.setSpacing(0);
 			layoutClassFigure_Body0.setVertical(true);
 
 			classFigure_Body0.setLayoutManager(layoutClassFigure_Body0);
 
 			RectangleFigure classFigure_PropertiesCompartment1 = new RectangleFigure();
 			classFigure_PropertiesCompartment1.setFill(true);
 			classFigure_PropertiesCompartment1.setFillXOR(false);
 			classFigure_PropertiesCompartment1.setOutline(true);
 			classFigure_PropertiesCompartment1.setOutlineXOR(false);
 			classFigure_PropertiesCompartment1.setLineWidth(1);
 			classFigure_PropertiesCompartment1.setLineStyle(Graphics.LINE_SOLID);
 
 			classFigure_Body0.add(classFigure_PropertiesCompartment1);
 			setFigureClassFigure_PropertiesCompartment(classFigure_PropertiesCompartment1);
 
 			RectangleFigure classFigure_OperationsCompartment1 = new RectangleFigure();
 			classFigure_OperationsCompartment1.setFill(true);
 			classFigure_OperationsCompartment1.setFillXOR(false);
 			classFigure_OperationsCompartment1.setOutline(true);
 			classFigure_OperationsCompartment1.setOutlineXOR(false);
 			classFigure_OperationsCompartment1.setLineWidth(1);
 			classFigure_OperationsCompartment1.setLineStyle(Graphics.LINE_SOLID);
 
 			classFigure_Body0.add(classFigure_OperationsCompartment1);
 			setFigureClassFigure_OperationsCompartment(classFigure_OperationsCompartment1);
 
 			RectangleFigure classFigure_ClassesCompartment1 = new RectangleFigure();
 			classFigure_ClassesCompartment1.setFill(true);
 			classFigure_ClassesCompartment1.setFillXOR(false);
 			classFigure_ClassesCompartment1.setOutline(true);
 			classFigure_ClassesCompartment1.setOutlineXOR(false);
 			classFigure_ClassesCompartment1.setLineWidth(1);
 			classFigure_ClassesCompartment1.setLineStyle(Graphics.LINE_SOLID);
 
 			classFigure_Body0.add(classFigure_ClassesCompartment1);
 			setFigureClassFigure_ClassesCompartment(classFigure_ClassesCompartment1);
 
 			RectangleFigure classFigure_LiteralsCompartment1 = new RectangleFigure();
 			classFigure_LiteralsCompartment1.setFill(true);
 			classFigure_LiteralsCompartment1.setFillXOR(false);
 			classFigure_LiteralsCompartment1.setOutline(true);
 			classFigure_LiteralsCompartment1.setOutlineXOR(false);
 			classFigure_LiteralsCompartment1.setLineWidth(1);
 			classFigure_LiteralsCompartment1.setLineStyle(Graphics.LINE_SOLID);
 
 			classFigure_Body0.add(classFigure_LiteralsCompartment1);
 			setFigureClassFigure_LiteralsCompartment(classFigure_LiteralsCompartment1);
 
 			RectangleFigure classFigure_OthersCompartment1 = new RectangleFigure();
 			classFigure_OthersCompartment1.setFill(true);
 			classFigure_OthersCompartment1.setFillXOR(false);
 			classFigure_OthersCompartment1.setOutline(true);
 			classFigure_OthersCompartment1.setOutlineXOR(false);
 			classFigure_OthersCompartment1.setLineWidth(1);
 			classFigure_OthersCompartment1.setLineStyle(Graphics.LINE_SOLID);
 
 			classFigure_Body0.add(classFigure_OthersCompartment1);
 			setFigureClassFigure_OthersCompartment(classFigure_OthersCompartment1);
 
 		}
 
 		/**
 		 * @generated
 		 */
 		private WrapLabel fClassFigure_name;
 
 		/**
 		 * @generated
 		 */
 		public WrapLabel getFigureClassFigure_name() {
 			return fClassFigure_name;
 		}
 
 		/**
 		 * @generated
 		 */
 		private void setFigureClassFigure_name(WrapLabel fig) {
 			fClassFigure_name = fig;
 		}
 
 		/**
 		 * @generated
 		 */
 		private RectangleFigure fClassFigure_PropertiesCompartment;
 
 		/**
 		 * @generated
 		 */
 		public RectangleFigure getFigureClassFigure_PropertiesCompartment() {
 			return fClassFigure_PropertiesCompartment;
 		}
 
 		/**
 		 * @generated
 		 */
 		private void setFigureClassFigure_PropertiesCompartment(RectangleFigure fig) {
 			fClassFigure_PropertiesCompartment = fig;
 		}
 
 		/**
 		 * @generated
 		 */
 		private RectangleFigure fClassFigure_OperationsCompartment;
 
 		/**
 		 * @generated
 		 */
 		public RectangleFigure getFigureClassFigure_OperationsCompartment() {
 			return fClassFigure_OperationsCompartment;
 		}
 
 		/**
 		 * @generated
 		 */
 		private void setFigureClassFigure_OperationsCompartment(RectangleFigure fig) {
 			fClassFigure_OperationsCompartment = fig;
 		}
 
 		/**
 		 * @generated
 		 */
 		private RectangleFigure fClassFigure_ClassesCompartment;
 
 		/**
 		 * @generated
 		 */
 		public RectangleFigure getFigureClassFigure_ClassesCompartment() {
 			return fClassFigure_ClassesCompartment;
 		}
 
 		/**
 		 * @generated
 		 */
 		private void setFigureClassFigure_ClassesCompartment(RectangleFigure fig) {
 			fClassFigure_ClassesCompartment = fig;
 		}
 
 		/**
 		 * @generated
 		 */
 		private RectangleFigure fClassFigure_LiteralsCompartment;
 
 		/**
 		 * @generated
 		 */
 		public RectangleFigure getFigureClassFigure_LiteralsCompartment() {
 			return fClassFigure_LiteralsCompartment;
 		}
 
 		/**
 		 * @generated
 		 */
 		private void setFigureClassFigure_LiteralsCompartment(RectangleFigure fig) {
 			fClassFigure_LiteralsCompartment = fig;
 		}
 
 		/**
 		 * @generated
 		 */
 		private RectangleFigure fClassFigure_OthersCompartment;
 
 		/**
 		 * @generated
 		 */
 		public RectangleFigure getFigureClassFigure_OthersCompartment() {
 			return fClassFigure_OthersCompartment;
 		}
 
 		/**
 		 * @generated
 		 */
 		private void setFigureClassFigure_OthersCompartment(RectangleFigure fig) {
 			fClassFigure_OthersCompartment = fig;
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
