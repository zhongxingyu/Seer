 /*
  * (c) Fachhochschule Potsdam
  */
 package org.fritzing.fritzing.diagram.edit.parts;
 
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.eclipse.draw2d.ConnectionAnchor;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.PositionConstants;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.gef.ConnectionEditPart;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.EditPartListener;
 import org.eclipse.gef.EditPolicy;
 import org.eclipse.gef.Request;
 import org.eclipse.gef.RequestConstants;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gef.commands.UnexecutableCommand;
 import org.eclipse.gef.editparts.AbstractEditPart;
 import org.eclipse.gef.editpolicies.LayoutEditPolicy;
 import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
 import org.eclipse.gef.requests.BendpointRequest;
 import org.eclipse.gef.requests.CreateRequest;
 import org.eclipse.gef.requests.ReconnectRequest;
 import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
 import org.eclipse.gmf.runtime.diagram.ui.actions.ActionIds;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.AbstractBorderedShapeEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.IBorderItemEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.IRotatableEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.BorderItemSelectionEditPolicy;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.PopupBarEditPolicy;
 import org.eclipse.gmf.runtime.diagram.ui.figures.BorderItemLocator;
 import org.eclipse.gmf.runtime.diagram.ui.figures.IBorderItemLocator;
 import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramCommandStack;
 import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewRequest;
 import org.eclipse.gmf.runtime.draw2d.ui.mapmode.IMapMode;
 import org.eclipse.gmf.runtime.gef.ui.figures.DefaultSizeNodeFigure;
 import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.gmf.runtime.notation.impl.DiagramImpl;
 import org.eclipse.gmf.runtime.notation.impl.NodeImpl;
 import org.fritzing.fritzing.Part;
 import org.fritzing.fritzing.Sketch;
 import org.fritzing.fritzing.Terminal;
 import org.fritzing.fritzing.diagram.edit.PartLoader;
 import org.fritzing.fritzing.diagram.edit.PartLoaderRegistry;
 import org.fritzing.fritzing.diagram.edit.policies.RotatableNonresizableShapeEditPolicy;
 import org.fritzing.fritzing.diagram.part.FritzingLinkDescriptor;
 import org.fritzing.fritzing.diagram.providers.FritzingElementTypes;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.impl.EAttributeImpl;
 
 /**
  * @generated NOT
  */
 class PartEditPart extends AbstractBorderedShapeEditPart implements IRotatableEditPart
 {
 
 	/**
 	 * @generated NOT
 	 */
 	PartLoader partLoader;
 	
 	/**
 	 * @generated NOT
 	 */
 	protected Point gridOffset;
 	
 	/**
 	 * @generated NOT
 	 */
 	protected boolean legsInitialized;
 
 	
 	/**
 	 * @generated NOT
 	 */
 	public PartEditPart(View view) {
 		super(view);
 		legsInitialized = false;
 		EObject element = view.getElement();
 		if (element instanceof Part) {
 			String genus = ((Part) element).getGenus();
 			String species = ((Part) element).getSpecies();
 			if (genus != null && species != null) {
 				partLoader = PartLoaderRegistry.getInstance().get(
 						genus + species);
 			}
 		}
 	}
 		
 	public int DPtoLP(int deviceUnit) {
 		return getMapMode().DPtoLP(deviceUnit);		
 	}
 	
 	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
 		EditPart part = connection.getSource();
 		if (part instanceof Terminal2EditPart) {
 			// if it's null, create one here
 			return ((Terminal2EditPart) part).getLegConnectionAnchor();
 		}
 		
 		return null;
 	}
 	
 	public Point getLegTargetPosition(Terminal2EditPart terminalPart) {
 		try {
 			Terminal terminal = (Terminal) ((NodeImpl) terminalPart.getModel()).getElement();
 			return partLoader.getTerminalLegTargetPosition(terminal.getId());
 		}
 		catch (Exception ex) {
 			
 		}			
 		
 		return null;
 	}
 
 	
 	/**
 	 * @generated NOT
 	 */
 	public boolean isRotatable() {
 		return true;
 	}
 	
 	/**
 	 * @generated NOT
 	 */
 	protected void createDefaultEditPolicies() {
 		super.createDefaultEditPolicies();
 
 		// place to add or remove policies
 		// POPUP_BAR and CONNECTOR_HANDLES are disabled by default in preferences
 	}
 	
 	public EditPolicy getPrimaryDragEditPolicy() {
 		/*
 		EditPolicy result = super.getPrimaryDragEditPolicy();
 		if (result instanceof ResizableEditPolicy) {
 			ResizableEditPolicy ep = (ResizableEditPolicy) result;
 			ep.setResizeDirections(PositionConstants.NONE);
 		}
 		return result;
 		*/
 		
 		return new RotatableNonresizableShapeEditPolicy();
 	}
 	
 	
 	
 	protected NodeFigure createMainFigure() {
 		return null;
 	}
 	
 	/**
 	 * @generated NOT
 	 */
 	protected NodeFigure createNodePlate() {
 		Dimension size = partLoader.getSize();
 //		DefaultSizeNodeFigure result = new DefaultSizeNodeFigure(getMapMode()
 //				.DPtoLP((int) (size.width / multiplier)), getMapMode().DPtoLP(
 //				(int) (size.height / multiplier)));
 		DefaultSizeNodeFigure result = new DefaultSizeNodeFigure(size.width, size.height);
 		return result;
 	}
 
    
 	protected void handleNotificationEvent(Notification notification) {
 		
 		Object feature = notification.getFeature();
 //		System.out.println("got a notification " + 
 //				notification.getEventType() + " " + 
 //				notification.getNotifier().getClass().getName()  + " " +
 //				feature
 //				);
 		        
 		if (feature instanceof EAttributeImpl) {
 			// initializeLegs fails if we trigger it too soon after part creation
 			// it seems to work at this point
 			
 			// there is probably a better place to call initializeLegs:
 			// the model already has a leg, and initializeLegs creates a view
 			// the problem is that creating the view doesn't seem to work
 			// if we call it too soon after the EditPart is created.
 			// waiting for the first feature notification is a hack
 			// though it seems to work...
 			// what about a signal when the PartFigure is created?
 			if (!legsInitialized) {
 				legsInitialized = true;
  				initializeLegs();
 			}
 		}
 		
 		super.handleNotificationEvent(notification);
 	}
 	
 	void initializeLegs() {
 		legsInitialized = true;
 				
 		try {
 			List<AbstractEditPart> chldrn = this.getChildren();
 			Sketch sketch = (Sketch) ((DiagramImpl) this.getParent().getModel()).getElement();
 									
 			for (int i = 0; i < chldrn.size(); i++) {				
 				if (chldrn.get(i) instanceof Terminal2EditPart) {
 					Terminal2EditPart child = (Terminal2EditPart) chldrn.get(i);
 					Terminal terminal = (Terminal) ((NodeImpl) child.getModel()).getElement();
 					String s = partLoader.getTerminalType(terminal.getId());
 					
 					if (s.equalsIgnoreCase("female")) {
 						child.setFemale(true);
 						continue;
 					}
 
 					
 					if (!s.equalsIgnoreCase("leg")) continue;
 					
 					if (child.getLegConnectionAnchor() != null) {
 						// already has a leg
 						continue;
 					}
 					
						
					
 					child.getFigure().setVisible(false);
 					FritzingLinkDescriptor fld = new FritzingLinkDescriptor(terminal, 
 							sketch,
							terminal.getLeg(), FritzingElementTypes.Leg_4003, 4003);
					
 					
 					CreateConnectionViewRequest.ConnectionViewDescriptor descriptor = new CreateConnectionViewRequest.ConnectionViewDescriptor(
 							fld.getSemanticAdapter(), "4003",
 							ViewUtil.APPEND, true, ((IGraphicalEditPart) this.getParent())
 									.getDiagramPreferencesHint());
 					CreateConnectionViewRequest ccr = new CreateConnectionViewRequest(
 							descriptor);
 					ccr.setType(RequestConstants.REQ_CONNECTION_START);
 					ccr.setSourceEditPart(child);
 					Command cmd1 = child.getCommand(ccr);
 									
 					ccr.setType(RequestConstants.REQ_CONNECTION_END);
 					ccr.setTargetEditPart(this.getParent());				
 					Command cmd2 = this.getParent().getCommand(ccr);
 							
 					if (cmd2 != null && cmd2.canExecute()) {
 						DiagramCommandStack commandStack = this.getDiagramEditDomain()
 						.getDiagramCommandStack();
						commandStack.execute(cmd2);	
 						
 												
 
 					}
 					else {
 						// alert the user?
 					}
 					
 					
 					
 
 				}
 			}
 		}
 		catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	protected boolean addEastWestFixedChild(EditPart childEditPart) {
 		if (childEditPart instanceof Terminal2EditPart) {
 			int terminalPosition = PositionConstants.NONE;
 			Point p = null;
 			Object model = childEditPart.getModel();		
 			if (model instanceof NodeImpl) {
 				EObject eobject = ((NodeImpl) model).getElement();
 				if (eobject instanceof Terminal) {
 					String name = ((Terminal) eobject).getId();
 					p = findTerminal(childEditPart);
 					if (p == null) {
 						terminalPosition = EastWestBorderItemLocator.parseTerminalName(name);
 					}
 				}
 			}
 			
 			IBorderItemLocator locator = null;			
 			if ((p != null) || (terminalPosition != PositionConstants.NONE)) {
 				locator = new EastWestBorderItemLocator(getMainFigure(), this, terminalPosition, p);
 			}
 			else {
 				locator =  new BorderItemLocator( getMainFigure(), terminalPosition);
 			}
 			getBorderedFigure().getBorderItemContainer().add(
 				((Terminal2EditPart) childEditPart).getFigure(), locator);
 			return true;
 		}
 				
 		return false;
 	}
 	
 	/**
 	 * @generated NOT
 	 */
 	protected LayoutEditPolicy createLayoutEditPolicy() {
 		LayoutEditPolicy lep = new LayoutEditPolicy() {
 
 			protected EditPolicy createChildEditPolicy(EditPart child) {
 				if ((child instanceof IBorderItemEditPart) && !(child instanceof Terminal2EditPart)) {
 					return new BorderItemSelectionEditPolicy();
 				}
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
 
 	protected Point findTerminal(EditPart childEditPart) {
 		if (!(childEditPart instanceof Terminal2EditPart)) return null;
 		
 		Object model = childEditPart.getModel();
 	
 		if (model instanceof NodeImpl) {
 			EObject eobject = ((NodeImpl) model).getElement();
 			if (eobject instanceof Terminal) {
 				String name = ((Terminal) eobject).getId();
 				if (name != null) {
 					Point q = partLoader.getTerminalPoint(name);
 					if (q != null) {
 						return q;
 					}
 				}
 			}
 		}
 		
 		return null;
 	}
 
 }
