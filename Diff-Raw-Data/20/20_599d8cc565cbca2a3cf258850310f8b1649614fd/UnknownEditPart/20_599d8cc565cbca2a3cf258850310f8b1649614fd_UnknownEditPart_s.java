 /**
  * Copyright (c) 2010 Ugo Sangiorgi and others.
  *  All rights reserved. This program and the accompanying materials
  *  are made available under the terms of the Eclipse Public License v1.0
  *  which accompanies this distribution, and is available at
  *  http://www.eclipse.org/legal/epl-v10.html
  *  
  *  Contributors:
  *  Ugo Sangiorgi <ugo.sangiorgi@gmail.com> - Initial contribution
  *  
  *
  * $Id$
  */ 
  package org.eclipse.sketch.examples.shapes.diagram.edit.parts;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.Label;
 import org.eclipse.draw2d.PositionConstants;
 import org.eclipse.draw2d.Shape;
 import org.eclipse.draw2d.StackLayout;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.EditPolicy;
 import org.eclipse.gef.Request;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gef.editpolicies.LayoutEditPolicy;
 import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
 import org.eclipse.gef.requests.CreateRequest;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeNodeEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.ResizableShapeLabelEditPolicy;
 import org.eclipse.gmf.runtime.draw2d.ui.figures.ConstrainedToolbarLayout;
 import org.eclipse.gmf.runtime.draw2d.ui.figures.WrappingLabel;
 import org.eclipse.gmf.runtime.draw2d.ui.render.RenderedImage;
 import org.eclipse.gmf.runtime.draw2d.ui.render.factory.RenderedImageFactory;
 import org.eclipse.gmf.runtime.draw2d.ui.render.figures.ScalableImageFigure;
 import org.eclipse.gmf.runtime.emf.type.core.IElementType;
 import org.eclipse.gmf.runtime.gef.ui.figures.DefaultSizeNodeFigure;
 import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.gmf.runtime.notation.impl.NodeImpl;
 import org.eclipse.gmf.runtime.notation.impl.ShapeImpl;
 import org.eclipse.sketch.examples.shapes.Unknown;
 import org.eclipse.sketch.examples.shapes.diagram.edit.policies.UnknownItemSemanticEditPolicy;
 import org.eclipse.sketch.examples.shapes.diagram.part.ShapesVisualIDRegistry;
 import org.eclipse.sketch.examples.shapes.diagram.providers.ShapesElementTypes;
 import org.eclipse.sketch.examples.shapes.diagram.util.Base64;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.RGB;
 import org.osgi.framework.Bundle;
 
 /**
  * @generated
  */
 public class UnknownEditPart extends ShapeNodeEditPart {
 
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
 	public UnknownEditPart(View view) {
 		super(view);
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	protected void createDefaultEditPolicies() {
 		super.createDefaultEditPolicies();
 		installEditPolicy(EditPolicyRoles.SEMANTIC_ROLE,
 				new UnknownItemSemanticEditPolicy());
 		installEditPolicy(EditPolicy.LAYOUT_ROLE, createLayoutEditPolicy());
 		// XXX need an SCR to runtime to have another abstract superclass that would let children add reasonable editpolicies
 		removeEditPolicy(org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles.CONNECTION_HANDLES_ROLE);
		installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE,
				new ResizableShapeLabelEditPolicy());
 	}
 
 	/**
 	 * @generated
 	 */
 	protected LayoutEditPolicy createLayoutEditPolicy() {
 		LayoutEditPolicy lep = new LayoutEditPolicy() {
 
 			protected EditPolicy createChildEditPolicy(EditPart child) {
 				EditPolicy result = child
 						.getEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE);
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
 	 * @generated NOT
 	 */
 	protected IFigure createNodeShape() {
 
 		Bundle bundle = Platform.getBundle("org.eclipse.sketch.examples.shapes");
 		try {
 
 			NodeImpl n = (NodeImpl) getModel();
 			Unknown s = (Unknown) n.basicGetElement();
 
			if (s.getSvg() != null) {
 
				IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace()
						.getRoot();
 				File imgfile = myWorkspaceRoot.getLocation().append(
 						"s" + System.currentTimeMillis() + ".svg").toFile();
 
 				Base64.decodeToFile(s.getSvg(), imgfile.getPath());
 
 				RenderedImage img = RenderedImageFactory.getInstance(imgfile
 						.getPath());
 				img.getRenderInfo().setValues(0, 0, true, true,
 						new RGB(255, 0, 0), new RGB(255, 0, 0));
 
 				primaryShape = new SVGSketchFigure(img);
 
 			} else {
 				primaryShape = new SVGSketchFigure(RenderedImageFactory
 						.getInstance(bundle.getEntry("/icons/square.svg")));
 			}
 
 		} catch (IOException e) {
 
 			e.printStackTrace();
 			//primaryShape = new SketchFigure(RenderedImageFactory
 			//		.getInstance(bundle.getEntry("/icons/imgmiss.gif")));
 		}
 
 		return primaryShape;
 
 	}
 
 	/**
 	 * @generated
 	 */
 	public SVGSketchFigure getPrimaryShape() {
 		return (SVGSketchFigure) primaryShape;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected boolean addFixedChild(EditPart childEditPart) {
 		if (childEditPart instanceof UnknownLabelEditPart) {
 			((UnknownLabelEditPart) childEditPart).setLabel(getPrimaryShape()
 					.getFigureSketchLabelFigure());
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected boolean removeFixedChild(EditPart childEditPart) {
 		if (childEditPart instanceof UnknownLabelEditPart) {
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
 		return getContentPane();
 	}
 
 	/**
 	 * @generated
 	 */
 	protected NodeFigure createNodePlate() {
 		DefaultSizeNodeFigure result = new DefaultSizeNodeFigure(40, 40);
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
 			layout.setSpacing(5);
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
 			((Shape) primaryShape).setLineWidth(width);
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
 		return getChildBySemanticHint(ShapesVisualIDRegistry
 				.getType(UnknownLabelEditPart.VISUAL_ID));
 	}
 
 	/**
 	 * @generated
 	 */
 	public List/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/getMARelTypesOnSource() {
 		List/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/types = new ArrayList/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/();
 		types.add(ShapesElementTypes.Connection_4001);
 		types.add(ShapesElementTypes.DashedConnection_4002);
 		return types;
 	}
 
 	/**
 	 * @generated
 	 */
 	public List/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/getMARelTypesOnSourceAndTarget(
 			IGraphicalEditPart targetEditPart) {
 		List/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/types = new ArrayList/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/();
 		if (targetEditPart instanceof SquareEditPart) {
 			types.add(ShapesElementTypes.Connection_4001);
 		}
 		if (targetEditPart instanceof TriangleEditPart) {
 			types.add(ShapesElementTypes.Connection_4001);
 		}
 		if (targetEditPart instanceof org.eclipse.sketch.examples.shapes.diagram.edit.parts.UnknownEditPart) {
 			types.add(ShapesElementTypes.Connection_4001);
 		}
 		if (targetEditPart instanceof SquareEditPart) {
 			types.add(ShapesElementTypes.DashedConnection_4002);
 		}
 		if (targetEditPart instanceof TriangleEditPart) {
 			types.add(ShapesElementTypes.DashedConnection_4002);
 		}
 		if (targetEditPart instanceof org.eclipse.sketch.examples.shapes.diagram.edit.parts.UnknownEditPart) {
 			types.add(ShapesElementTypes.DashedConnection_4002);
 		}
 		return types;
 	}
 
 	/**
 	 * @generated
 	 */
 	public List/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/getMATypesForTarget(
 			IElementType relationshipType) {
 		List/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/types = new ArrayList/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/();
 		if (relationshipType == ShapesElementTypes.Connection_4001) {
 			types.add(ShapesElementTypes.Square_2001);
 		}
 		if (relationshipType == ShapesElementTypes.Connection_4001) {
 			types.add(ShapesElementTypes.Triangle_2002);
 		}
 		if (relationshipType == ShapesElementTypes.Connection_4001) {
 			types.add(ShapesElementTypes.Unknown_2003);
 		}
 		if (relationshipType == ShapesElementTypes.DashedConnection_4002) {
 			types.add(ShapesElementTypes.Square_2001);
 		}
 		if (relationshipType == ShapesElementTypes.DashedConnection_4002) {
 			types.add(ShapesElementTypes.Triangle_2002);
 		}
 		if (relationshipType == ShapesElementTypes.DashedConnection_4002) {
 			types.add(ShapesElementTypes.Unknown_2003);
 		}
 		return types;
 	}
 
 	/**
 	 * @generated
 	 */
 	public List/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/getMARelTypesOnTarget() {
 		List/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/types = new ArrayList/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/();
 		types.add(ShapesElementTypes.Connection_4001);
 		types.add(ShapesElementTypes.DashedConnection_4002);
 		return types;
 	}
 
 	/**
 	 * @generated
 	 */
 	public List/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/getMATypesForSource(
 			IElementType relationshipType) {
 		List/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/types = new ArrayList/*<org.eclipse.gmf.runtime.emf.type.core.IElementType>*/();
 		if (relationshipType == ShapesElementTypes.Connection_4001) {
 			types.add(ShapesElementTypes.Square_2001);
 		}
 		if (relationshipType == ShapesElementTypes.Connection_4001) {
 			types.add(ShapesElementTypes.Triangle_2002);
 		}
 		if (relationshipType == ShapesElementTypes.Connection_4001) {
 			types.add(ShapesElementTypes.Unknown_2003);
 		}
 		if (relationshipType == ShapesElementTypes.DashedConnection_4002) {
 			types.add(ShapesElementTypes.Square_2001);
 		}
 		if (relationshipType == ShapesElementTypes.DashedConnection_4002) {
 			types.add(ShapesElementTypes.Triangle_2002);
 		}
 		if (relationshipType == ShapesElementTypes.DashedConnection_4002) {
 			types.add(ShapesElementTypes.Unknown_2003);
 		}
 		return types;
 	}
 
 	/**
 	 * @generated
 	 */
 	public class SVGSketchFigure extends ScalableImageFigure {
 
 		/**
 		 * @generated
 		 */
 		private WrappingLabel fFigureSketchLabelFigure;
 
 		/**
 		 * @generated NOT
 		 */
 		private RenderedImage image;
 
 		/**
 		 * @generated NOT
 		 */
 		public SVGSketchFigure() {
 			super(null, true, true, true);
 			createContents();
 		}
 
 		/**
 		 * @generated NOT
 		 */
 		public SVGSketchFigure(RenderedImage i) {
 			super(i, true, true, true);
 
 			Unknown model = (Unknown) ((ShapeImpl) getModel())
 					.basicGetElement();
 			setToolTip(new Label(model.getElement()));

 			this.image = i;
 
 			this.setMaintainAspectRatio(true);
 
 			this.setRenderedImage(image);
 
 			createContents();
 
 			//center the children
 			this.setLayoutManager(new StackLayout() {
 				public void layout(IFigure figure) {
 					Rectangle r = figure.getClientArea();
 					List children = figure.getChildren();
 					IFigure child;
 					Dimension d;
 					for (int i = 0; i < children.size(); i++) {
 						child = (IFigure) children.get(i);
 						d = child.getPreferredSize(r.width, r.height);
 						d.width = Math.min(d.width, r.width);
 						d.height = Math.min(d.height, r.height);
 						Rectangle childRect = new Rectangle(r.x
 								+ (r.width - d.width) / 2, r.y
 								+ (r.height - d.height) / 2, d.width, d.height);
 						child.setBounds(childRect);
 					}
 				}
 			});
 		}
 
 		/**
 		 * @generated NOT
 		 */
 		private void createContents() {
 
 			fFigureSketchLabelFigure = new WrappingLabel();
 			fFigureSketchLabelFigure.setAlignment(PositionConstants.CENTER);
 			fFigureSketchLabelFigure.setText("");
 			fFigureSketchLabelFigure.setBackgroundColor(ColorConstants.white);
 
 			this.add(fFigureSketchLabelFigure);
 
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
 		public WrappingLabel getFigureSketchLabelFigure() {
 			return fFigureSketchLabelFigure;
 		}
 
 	}
 
}
