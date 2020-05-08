 /*
  * (c) Fachhochschule Potsdam
  */
 package org.fritzing.fritzing.diagram.edit.parts;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.draw2d.AbsoluteBendpoint;
 import org.eclipse.draw2d.Connection;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.RelativeBendpoint;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.PointList;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.impl.EAttributeImpl;
 import org.eclipse.gef.ConnectionEditPart;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.EditPolicy;
 import org.eclipse.gef.Request;
 import org.eclipse.gef.RootEditPart;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gef.commands.UnexecutableCommand;
 import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
 import org.eclipse.gef.handles.ConnectionEndHandle;
 import org.eclipse.gef.handles.ConnectionStartHandle;
 import org.eclipse.gef.requests.BendpointRequest;
 import org.eclipse.gef.requests.GroupRequest;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionNodeEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.ConnectionBendpointEditPolicy;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.ConnectionHandleEditPolicy;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
 import org.eclipse.gmf.runtime.diagram.ui.internal.editpolicies.ConnectionEditPolicy;
 import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramCommandStack;
 import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramGraphicalViewer;
 import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramGraphicalViewer;
 import org.eclipse.gmf.runtime.diagram.ui.requests.RequestConstants;
 import org.eclipse.gmf.runtime.diagram.ui.requests.ZOrderRequest;
 import org.eclipse.gmf.runtime.draw2d.ui.figures.PolylineConnectionEx;
 import org.eclipse.gmf.runtime.draw2d.ui.figures.WrapLabel;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.widgets.Display;
 import org.fritzing.fritzing.diagram.edit.policies.LegItemSemanticEditPolicy;
 import org.fritzing.fritzing.diagram.edit.policies.NonDeleteComponentEditPolicy;
 import org.fritzing.fritzing.diagram.part.FritzingDiagramEditor;
 import org.fritzing.fritzing.diagram.part.FritzingDiagramEditorUtil;
 import org.eclipse.swt.graphics.Color;
 
 /**
  * @generated
  */
 public class LegEditPart extends ConnectionFritzingEditPart {
 
 	/**
 	 * @generated
 	 */
 	public static final int VISUAL_ID = 4003;
 
 	/**
 	 * @generated
 	 */
 	public LegEditPart(View view) {
 		super(view);
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	protected void createDefaultEditPolicies() {
 		super.createDefaultEditPolicies();
 		installEditPolicy(EditPolicyRoles.SEMANTIC_ROLE,
 				new LegItemSemanticEditPolicy());
 		installEditPolicy(EditPolicy.CONNECTION_ROLE,
 				new LegItemConnectionEditPolicy());
 		installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE,
 				new LegItemConnectionEndpointEditPolicy());
 		// don't want delete
 		installEditPolicy(EditPolicy.COMPONENT_ROLE,
 				new NonDeleteComponentEditPolicy());
 	}
 
 	protected void initBend(LegFigure figure, Point p) {
 		//			BendpointRequest request = new BendpointRequest();
 		//			request.setType(RequestConstants.REQ_CREATE_BENDPOINT);
 		//			request.setIndex(0);
 		//			request.setSource(this);
 		//			request.setLocation(p);
 		//			Command cmd = super.getCommand(request);
 		//			if (cmd != null && cmd.canExecute()) {
 		//				DiagramCommandStack commandStack = this.getDiagramEditDomain()
 		//				.getDiagramCommandStack();
 		//				commandStack.execute(cmd);		
 		//			}
 
 	}
 
 	protected boolean connectedToSketch() {
 		return (this.getTarget() instanceof SketchEditPart);
 	}
 
 	public Command getCommand(Request request) {
 		if (request.getType() == RequestConstants.REQ_CREATE_BENDPOINT) {
 			if (this.getTarget() instanceof SketchEditPart) {
 				return UnexecutableCommand.INSTANCE;
 			}
 		}
 
 		return super.getCommand(request);
 
 	}
 
 	/**
 	 * Creates figure for this edit part.
 	 * 
 	 * Body of this method does not depend on settings in generation model
 	 * so you may safely remove <i>generated</i> tag and modify it.
 	 * 
 	 * @generated NOT
 	 */
 
 	protected Connection createConnectionFigure() {
 		return new LegFigure(this);
 	}
 
 	/**
 	 * @generated
 	 */
 	public LegFigure getPrimaryShape() {
 		return (LegFigure) getFigure();
 	}
 
 	/**
 	 * @generated
 	 */
 	public class LegFigure extends ConnectionFritzingFigure {
 
 		LegEditPart leg;
 		
 		/**
 		 * @generated NOT
 		 */
 		public LegFigure() {
 			// XXX: do not use this!
 		}
 
 		/**
 		 * @generated NOT
 		 */
 		public LegFigure(LegEditPart leg) {
 			setLineWidth(connectionWidth = 3);
 			setForegroundColor(LEG_FIGURE_COLOR);
 			this.leg = leg;
 			createContents();
 		}
 		
 		public void paintFigure(Graphics g) {
 			this.setSmoothness(0);
 			super.paintFigure(g);
 		}
 
 
 		/**
 		 * @generated NOT
 		 */
 		private void createContents() {
 
 		}
 
 		public Object getRoutingConstraint() {
 			// this isn't a clean fix, but it will do for now
 			// basically, returning a null at this point stops any new bendpoint being created
 			// the root problem is that when a leg is connected to the sketch,
 			// adding a bendpoint to the leg results in the leg getting badly screwed up or disappearing completely
 			// this problem would probably fix itself if we were connecting to a terminal on the sketch and not the sketch itself
 
 			if (leg.connectedToSketch()) {
 				return null;
 			}
 
 			return super.getRoutingConstraint();
 		}
 	}
 
 	public class LegItemConnectionEditPolicy extends ConnectionEditPolicy {
 		protected Command createDeleteViewCommand(GroupRequest deleteRequest) {
 			// disable delete for legs
 			return UnexecutableCommand.INSTANCE;
 		}
 
 		protected Command createDeleteSemanticCommand(GroupRequest deleteRequest) {
 			// disable delete for legs
 			return UnexecutableCommand.INSTANCE;
 		}
 	}
 
 	public class LegItemConnectionEndpointEditPolicy extends
 			ConnectionEndpointEditPolicy {
 		protected List createSelectionHandles() {
 			// only show target handle, not source handle
 			List list = new ArrayList();
 			list.add(new ConnectionEndHandle((ConnectionEditPart) getHost()));
 			return list;
 		}
 
 	}
 
	public static final Color LEG_FIGURE_COLOR = new Color(null, 178, 179, 180);
 
 }
