 package org.eclipse.uml2.diagram.clazz.edit.parts;
 
 import java.util.Iterator;
 
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.Connection;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.RotatableDecoration;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionNodeEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
 import org.eclipse.gmf.runtime.draw2d.ui.figures.PolylineConnectionEx;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.uml2.diagram.clazz.conventions.AssociationEndConvention;
 import org.eclipse.uml2.diagram.clazz.edit.policies.AssociationItemSemanticEditPolicy;
 import org.eclipse.uml2.diagram.common.draw2d.decoration.AssociationDecoration;
 import org.eclipse.uml2.uml.Association;
 import org.eclipse.uml2.uml.Property;
 
 /**
  * @generated
  */
 public class AssociationEditPart extends ConnectionNodeEditPart {
 
 	/**
 	 * @generated
 	 */
 	public static final int VISUAL_ID = 4005;
 
 	/**
 	 * @generated
 	 */
 	public AssociationEditPart(View view) {
 		super(view);
 	}
 
 	/**
 	 * @generated
 	 */
 	protected void createDefaultEditPolicies() {
 		super.createDefaultEditPolicies();
 		installEditPolicy(EditPolicyRoles.SEMANTIC_ROLE, new AssociationItemSemanticEditPolicy());
 	}
 
 	/**
 	 * Creates figure for this edit part.
 	 * 
 	 * Body of this method does not depend on settings in generation model
 	 * so you may safely remove <i>generated</i> tag and modify it.
 	 * 
 	 * @generated
 	 */
 	protected Connection createConnectionFigureGen() {
 
 		return new AssociationLinkFigure();
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	protected Connection createConnectionFigure() {
 		Connection result = createConnectionFigureGen();
 		refreshDecorations((AssociationLinkFigure) result);
 		return result;
 	}
 
 	/**
 	 * @NOT-GENERATED
 	 */
 	protected void addSemanticListeners() {
 		super.addSemanticListeners();
 		Association association = (Association) resolveSemanticElement();
 		for (Iterator ends = association.getMemberEnds().iterator(); ends.hasNext();) {
 			Property next = (Property) ends.next();
 			addListenerFilter("SemanticModel", this, next);
 		}
 	}
 
 	/**
 	 * @NOT-GENERATED
 	 */
 	protected void handleNotificationEvent(Notification notification) {
 		super.handleNotificationEvent(notification);
 		if (notification.getNotifier() instanceof Property) {
 			refreshDecorations((AssociationLinkFigure) getConnectionFigure());
 		}
 	}
 
 	/**
 	 * @NOT-GENERATED
 	 */
 	private void refreshDecorations(AssociationLinkFigure linkFigure) {
 		Association association = (Association) resolveSemanticElement();
		if (association == null) {
 			return;
 		}
 		Property sourceEnd = AssociationEndConvention.getSourceEnd(association);
 		Property targetEnd = AssociationEndConvention.getTargetEnd(association);
 
 		linkFigure.getSourceDecorationImpl().update(association, sourceEnd, targetEnd);
 		linkFigure.getTargetDecorationImpl().update(association, targetEnd, sourceEnd);
 	}
 
 	/**
 	 * @generated
 	 */
 	public class AssociationLinkFigure extends PolylineConnectionEx {
 
 		/**
 		 * @generated
 		 */
 		public AssociationLinkFigure() {
 			this.setFill(true);
 			this.setFillXOR(false);
 			this.setOutline(true);
 			this.setOutlineXOR(false);
 			this.setLineWidth(1);
 			this.setLineStyle(Graphics.LINE_SOLID);
 			this.setForegroundColor(ColorConstants.blue);
 
 			setSourceDecoration(createSourceDecoration());
 			setTargetDecoration(createTargetDecoration());
 		}
 
 		/**
 		 * @generated
 		 */
 		private RotatableDecoration createSourceDecoration() {
 			AssociationDecoration df = new AssociationDecoration();
 
 			return df;
 		}
 
 		/**
 		 * @generated
 		 */
 		private RotatableDecoration createTargetDecoration() {
 			AssociationDecoration df = new AssociationDecoration();
 
 			return df;
 		}
 
 		/**
 		 * @generated NOT
 		 */
 		private AssociationDecoration getTargetDecorationImpl() {
 			return (AssociationDecoration) getTargetDecoration();
 		}
 
 		/**
 		 * @generated NOT
 		 */
 		private AssociationDecoration getSourceDecorationImpl() {
 			return (AssociationDecoration) getSourceDecoration();
 		}
 
 	}
 
 }
