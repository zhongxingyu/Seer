 package org.eclipse.uml2.diagram.profile.edit.parts;
 
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.MarginBorder;
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
 import org.eclipse.uml2.diagram.profile.edit.policies.Profile2ItemSemanticEditPolicy;
 import org.eclipse.uml2.diagram.profile.part.UMLVisualIDRegistry;
 import org.eclipse.uml2.diagram.profile.providers.UMLElementTypes;
 
 /**
  * @generated
  */
 public class Profile2EditPart extends ShapeNodeEditPart {
 
 	/**
 	 * @generated
 	 */
 	public static final int VISUAL_ID = 2002;
 
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
 	public Profile2EditPart(View view) {
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
 						if (type == UMLElementTypes.Stereotype_3003) {
 							EditPart compartmentEditPart = getChildBySemanticHint(UMLVisualIDRegistry.getType(ProfileContentsEditPart.VISUAL_ID));
 							return compartmentEditPart == null ? null : compartmentEditPart.getCommand(request);
 						}
 					}
 					return super.getCommand(request);
 				}
 				return null;
 			}
 		});
 		super.createDefaultEditPolicies();
 
 		installEditPolicy(EditPolicyRoles.SEMANTIC_ROLE, new Profile2ItemSemanticEditPolicy());
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
 		ProfileFigure figure = new ProfileFigure();
 		return primaryShape = figure;
 	}
 
 	/**
 	 * @generated
 	 */
 	public ProfileFigure getPrimaryShape() {
 		return (ProfileFigure) primaryShape;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected boolean addFixedChild(EditPart childEditPart) {
 		if (childEditPart instanceof ProfileNameEditPart) {
 			((ProfileNameEditPart) childEditPart).setLabel(getPrimaryShape().getFigureProfileFigure_NameLabel());
 			return true;
 		}
 		if (childEditPart instanceof ProfileContentsEditPart) {
 			IFigure pane = getPrimaryShape().getFigureProfileFigure_ContentsCompartment();
 			setupContentPane(pane); // FIXME each comparment should handle his content pane in his own way 
 			pane.add(((ProfileContentsEditPart) childEditPart).getFigure());
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected boolean removeFixedChild(EditPart childEditPart) {
 
 		if (childEditPart instanceof ProfileContentsEditPart) {
 			IFigure pane = getPrimaryShape().getFigureProfileFigure_ContentsCompartment();
			setupContentPane(pane); // FIXME each comparment should handle his content pane in his own way 
 			pane.remove(((ProfileContentsEditPart) childEditPart).getFigure());
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
 
 		if (editPart instanceof ProfileContentsEditPart) {
 			return getPrimaryShape().getFigureProfileFigure_ContentsCompartment();
 		}
 		return super.getContentPaneFor(editPart);
 	}
 
 	/**
 	 * @generated
 	 */
 	protected NodeFigure createNodePlate() {
 		DefaultSizeNodeFigure result = new DefaultSizeNodeFigure(getMapMode().DPtoLP(200), getMapMode().DPtoLP(150));
 
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
 		return getChildBySemanticHint(UMLVisualIDRegistry.getType(ProfileNameEditPart.VISUAL_ID));
 	}
 
 	/**
 	 * @generated
 	 */
 	public class ProfileFigure extends RectangleFigure {
 
 		/**
 		 * @generated
 		 */
 		public ProfileFigure() {
 
 			GridLayout layoutThis = new GridLayout();
 			layoutThis.numColumns = 2;
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
 			this.setForegroundColor(ColorConstants.lightGray);
 			createContents();
 		}
 
 		/**
 		 * @generated
 		 */
 		private void createContents() {
 
 			RectangleFigure profile_leftTab0 = new RectangleFigure();
 			profile_leftTab0.setFill(true);
 			profile_leftTab0.setFillXOR(false);
 			profile_leftTab0.setOutline(true);
 			profile_leftTab0.setOutlineXOR(false);
 			profile_leftTab0.setLineWidth(1);
 			profile_leftTab0.setLineStyle(Graphics.LINE_SOLID);
 			profile_leftTab0.setPreferredSize(new Dimension(getMapMode().DPtoLP(0), getMapMode().DPtoLP(25)));
 			profile_leftTab0.setMinimumSize(new Dimension(getMapMode().DPtoLP(0), getMapMode().DPtoLP(25)));
 
 			GridLayoutData constraintProfile_leftTab0 = new GridLayoutData();
 			constraintProfile_leftTab0.verticalAlignment = GridLayoutData.FILL;
 			constraintProfile_leftTab0.horizontalAlignment = GridLayoutData.FILL;
 			constraintProfile_leftTab0.horizontalIndent = 0;
 			constraintProfile_leftTab0.horizontalSpan = 1;
 			constraintProfile_leftTab0.verticalSpan = 1;
 			constraintProfile_leftTab0.grabExcessHorizontalSpace = true;
 			constraintProfile_leftTab0.grabExcessVerticalSpace = false;
 			this.add(profile_leftTab0, constraintProfile_leftTab0);
 
 			RectangleFigure profile_rightTab0 = new RectangleFigure();
 			profile_rightTab0.setFill(false);
 			profile_rightTab0.setFillXOR(false);
 			profile_rightTab0.setOutline(false);
 			profile_rightTab0.setOutlineXOR(false);
 			profile_rightTab0.setLineWidth(1);
 			profile_rightTab0.setLineStyle(Graphics.LINE_SOLID);
 			profile_rightTab0.setPreferredSize(new Dimension(getMapMode().DPtoLP(0), getMapMode().DPtoLP(25)));
 			profile_rightTab0.setMinimumSize(new Dimension(getMapMode().DPtoLP(0), getMapMode().DPtoLP(25)));
 
 			GridLayoutData constraintProfile_rightTab0 = new GridLayoutData();
 			constraintProfile_rightTab0.verticalAlignment = GridLayoutData.FILL;
 			constraintProfile_rightTab0.horizontalAlignment = GridLayoutData.FILL;
 			constraintProfile_rightTab0.horizontalIndent = 0;
 			constraintProfile_rightTab0.horizontalSpan = 1;
 			constraintProfile_rightTab0.verticalSpan = 1;
 			constraintProfile_rightTab0.grabExcessHorizontalSpace = true;
 			constraintProfile_rightTab0.grabExcessVerticalSpace = false;
 			this.add(profile_rightTab0, constraintProfile_rightTab0);
 
 			RectangleFigure profileFigure_body0 = new RectangleFigure();
 			profileFigure_body0.setFill(true);
 			profileFigure_body0.setFillXOR(false);
 			profileFigure_body0.setOutline(true);
 			profileFigure_body0.setOutlineXOR(false);
 			profileFigure_body0.setLineWidth(1);
 			profileFigure_body0.setLineStyle(Graphics.LINE_SOLID);
 			profileFigure_body0.setBorder(new MarginBorder(getMapMode().DPtoLP(1), getMapMode().DPtoLP(1), getMapMode().DPtoLP(1), getMapMode().DPtoLP(1)));
 
 			GridLayoutData constraintProfileFigure_body0 = new GridLayoutData();
 			constraintProfileFigure_body0.verticalAlignment = GridLayoutData.FILL;
 			constraintProfileFigure_body0.horizontalAlignment = GridLayoutData.FILL;
 			constraintProfileFigure_body0.horizontalIndent = 0;
 			constraintProfileFigure_body0.horizontalSpan = 2;
 			constraintProfileFigure_body0.verticalSpan = 1;
 			constraintProfileFigure_body0.grabExcessHorizontalSpace = true;
 			constraintProfileFigure_body0.grabExcessVerticalSpace = true;
 			this.add(profileFigure_body0, constraintProfileFigure_body0);
 
 			ToolbarLayout layoutProfileFigure_body0 = new ToolbarLayout();
 			layoutProfileFigure_body0.setStretchMinorAxis(true);
 			layoutProfileFigure_body0.setMinorAlignment(ToolbarLayout.ALIGN_CENTER
 
 			);
 
 			layoutProfileFigure_body0.setSpacing(0);
 			layoutProfileFigure_body0.setVertical(true);
 
 			profileFigure_body0.setLayoutManager(layoutProfileFigure_body0);
 
 			RectangleFigure profile_NameContainer1 = new RectangleFigure();
 			profile_NameContainer1.setFill(true);
 			profile_NameContainer1.setFillXOR(false);
 			profile_NameContainer1.setOutline(false);
 			profile_NameContainer1.setOutlineXOR(false);
 			profile_NameContainer1.setLineWidth(1);
 			profile_NameContainer1.setLineStyle(Graphics.LINE_SOLID);
 			profile_NameContainer1.setMinimumSize(new Dimension(getMapMode().DPtoLP(0), getMapMode().DPtoLP(25)));
 
 			profileFigure_body0.add(profile_NameContainer1);
 
 			CenterLayout layoutProfile_NameContainer1 = new CenterLayout();
 
 			profile_NameContainer1.setLayoutManager(layoutProfile_NameContainer1);
 
 			WrapLabel profileFigure_NameLabel2 = new WrapLabel();
 			profileFigure_NameLabel2.setText("");
 
 			profile_NameContainer1.add(profileFigure_NameLabel2);
 			setFigureProfileFigure_NameLabel(profileFigure_NameLabel2);
 
 			RectangleFigure profileFigure_ProfileContainer1 = new RectangleFigure();
 			profileFigure_ProfileContainer1.setFill(true);
 			profileFigure_ProfileContainer1.setFillXOR(false);
 			profileFigure_ProfileContainer1.setOutline(false);
 			profileFigure_ProfileContainer1.setOutlineXOR(false);
 			profileFigure_ProfileContainer1.setLineWidth(1);
 			profileFigure_ProfileContainer1.setLineStyle(Graphics.LINE_SOLID);
 			profileFigure_ProfileContainer1.setMinimumSize(new Dimension(getMapMode().DPtoLP(0), getMapMode().DPtoLP(25)));
 
 			profileFigure_body0.add(profileFigure_ProfileContainer1);
 
 			CenterLayout layoutProfileFigure_ProfileContainer1 = new CenterLayout();
 
 			profileFigure_ProfileContainer1.setLayoutManager(layoutProfileFigure_ProfileContainer1);
 
 			WrapLabel profileFigure_ProfileLabel2 = new WrapLabel();
 			profileFigure_ProfileLabel2.setText("\u00ABprofile\u00BB");
 
 			GridLayoutData constraintProfileFigure_ProfileLabel2 = new GridLayoutData();
 			constraintProfileFigure_ProfileLabel2.verticalAlignment = GridLayoutData.CENTER;
 			constraintProfileFigure_ProfileLabel2.horizontalAlignment = GridLayoutData.CENTER;
 			constraintProfileFigure_ProfileLabel2.horizontalIndent = 0;
 			constraintProfileFigure_ProfileLabel2.horizontalSpan = 1;
 			constraintProfileFigure_ProfileLabel2.verticalSpan = 1;
 			constraintProfileFigure_ProfileLabel2.grabExcessHorizontalSpace = false;
 			constraintProfileFigure_ProfileLabel2.grabExcessVerticalSpace = false;
 			profileFigure_ProfileContainer1.add(profileFigure_ProfileLabel2, constraintProfileFigure_ProfileLabel2);
 
 			RectangleFigure profileFigure_ContentsCompartment1 = new RectangleFigure();
 			profileFigure_ContentsCompartment1.setFill(true);
 			profileFigure_ContentsCompartment1.setFillXOR(false);
 			profileFigure_ContentsCompartment1.setOutline(false);
 			profileFigure_ContentsCompartment1.setOutlineXOR(false);
 			profileFigure_ContentsCompartment1.setLineWidth(1);
 			profileFigure_ContentsCompartment1.setLineStyle(Graphics.LINE_SOLID);
 
 			profileFigure_body0.add(profileFigure_ContentsCompartment1);
 			setFigureProfileFigure_ContentsCompartment(profileFigure_ContentsCompartment1);
 
 		}
 
 		/**
 		 * @generated
 		 */
 		private WrapLabel fProfileFigure_NameLabel;
 
 		/**
 		 * @generated
 		 */
 		public WrapLabel getFigureProfileFigure_NameLabel() {
 			return fProfileFigure_NameLabel;
 		}
 
 		/**
 		 * @generated
 		 */
 		private void setFigureProfileFigure_NameLabel(WrapLabel fig) {
 			fProfileFigure_NameLabel = fig;
 		}
 
 		/**
 		 * @generated
 		 */
 		private RectangleFigure fProfileFigure_ContentsCompartment;
 
 		/**
 		 * @generated
 		 */
 		public RectangleFigure getFigureProfileFigure_ContentsCompartment() {
 			return fProfileFigure_ContentsCompartment;
 		}
 
 		/**
 		 * @generated
 		 */
 		private void setFigureProfileFigure_ContentsCompartment(RectangleFigure fig) {
 			fProfileFigure_ContentsCompartment = fig;
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
