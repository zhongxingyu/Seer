 /*
  * Copyright (c) 2007 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *    Dmitry Stadnik (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.examples.taipan.port.diagram.edit.parts;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.MarginBorder;
 import org.eclipse.draw2d.StackLayout;
 import org.eclipse.draw2d.ToolbarLayout;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.EditPolicy;
 import org.eclipse.gef.Request;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gef.editpolicies.LayoutEditPolicy;
 import org.eclipse.gmf.examples.taipan.figures.BuildingShape;
 import org.eclipse.gmf.examples.taipan.port.diagram.edit.policies.BuildingItemSemanticEditPolicy;
 import org.eclipse.gmf.examples.taipan.port.diagram.edit.policies.PortTextSelectionEditPolicy;
 import org.eclipse.gmf.examples.taipan.port.diagram.part.PortVisualIDRegistry;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.ITextAwareEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeNodeEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.ConstrainedToolbarLayoutEditPolicy;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
 import org.eclipse.gmf.runtime.diagram.ui.requests.ArrangeRequest;
 import org.eclipse.gmf.runtime.diagram.ui.requests.RequestConstants;
 import org.eclipse.gmf.runtime.draw2d.ui.figures.ConstrainedToolbarLayout;
 import org.eclipse.gmf.runtime.gef.ui.figures.DefaultSizeNodeFigure;
 import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
 import org.eclipse.gmf.runtime.notation.View;
 
 /**
  * @generated
  */
 public class BuildingEditPart extends ShapeNodeEditPart {
 
 	/**
 	 * @generated
 	 */
 	public static final int VISUAL_ID = 2001;
 
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
 	public BuildingEditPart(View view) {
 		super(view);
 	}
 
 	/**
 	 * @generated
 	 */
 	protected void createDefaultEditPolicies() {
 
 		super.createDefaultEditPolicies();
 		installEditPolicy(EditPolicyRoles.SEMANTIC_ROLE, new BuildingItemSemanticEditPolicy());
 		installEditPolicy(EditPolicy.LAYOUT_ROLE, createLayoutEditPolicy());
 	}
 
 	/**
 	 * @generated
 	 */
 	protected LayoutEditPolicy createLayoutEditPolicy() {
 
 		ConstrainedToolbarLayoutEditPolicy lep = new ConstrainedToolbarLayoutEditPolicy() {
 
 			protected EditPolicy createChildEditPolicy(EditPart child) {
 				if (child.getEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE) == null) {
 					if (child instanceof ITextAwareEditPart) {
 						return new PortTextSelectionEditPolicy();
 					}
 				}
 				return super.createChildEditPolicy(child);
 			}
 		};
 		return lep;
 	}
 
 	public Command getCommand(Request request) {
 		Command command = super.getCommand(request);
 		if (request.getType().equals(REQ_DELETE)) {
 			ArrangeRequest layoutRequest = new ArrangeRequest(RequestConstants.REQ_ARRANGE_DEFERRED);
 			List editParts = new ArrayList(getParent().getChildren());
 			editParts.remove(this);
 			layoutRequest.setViewAdaptersToArrange(editParts);
 			Command layoutCommand = getParent().getCommand(layoutRequest);
 			if (layoutCommand != null) {
 				command = command.chain(layoutCommand);
 			}
 		}
 		return command;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected IFigure createNodeShapeGen() {
 		return primaryShape = new BuildingShape();
 	}
 
 	protected IFigure createNodeShape() {
 		IFigure shape = createNodeShapeGen();
 		ToolbarLayout layout = new ToolbarLayout();
 		layout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
 		layout.setStretchMinorAxis(false);
 		layout.setSpacing(getMapMode().DPtoLP(5));
 		shape.setLayoutManager(layout);
 		shape.setBorder(new MarginBorder(getMapMode().DPtoLP(5)));
 		return shape;
 	}
 
 	/**
 	 * @generated
 	 */
 	public BuildingShape getPrimaryShape() {
 		return (BuildingShape) primaryShape;
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
 		return getChildBySemanticHint(PortVisualIDRegistry.getType(BuildingInfoEditPart.VISUAL_ID));
 	}
 
 }
