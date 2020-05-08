 package org.ow2.mindEd.adl.editor.graphic.ui.custom.edit.parts.proxy;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.LayoutManager;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.gef.DragTracker;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.Request;
 import org.eclipse.gef.editpolicies.LayoutEditPolicy;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
 import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
 import org.eclipse.swt.graphics.Color;
 import org.ow2.mindEd.adl.editor.graphic.ui.custom.figures.IFractalShape;
 import org.ow2.mindEd.adl.editor.graphic.ui.custom.helpers.ComponentHelper;
 
 public class MindProxy extends AbstractMindProxy {
 
 	public MindProxy(GraphicalEditPart realEditPart, int vID) {
		if (realEditPart == null) throw new NullPointerException();
 		editPart = realEditPart;
 		visualID = vID;
 	}
 	
 	public MindProxy(GraphicalEditPart realEditPart, int vID, int type) {
		if (realEditPart == null) throw new NullPointerException();
 		editPart = realEditPart;
 		visualID = vID;
 		mindType = type;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public GraphicalEditPart getCompartment() {
 		List<EditPart> children = editPart.getChildren();
 		
 		for (EditPart child : children) {
 			if (getMindProxyFor(child) instanceof MindCompartmentProxy)
 				return (GraphicalEditPart)child;
 		}
 		return null;
 	}
 	
 	
 	public void activate() {
 		if (ComponentHelper.isMerged(editPart)) 
 			// If the component is merged handle custom behaviour
 			ComponentHelper.handleMergedElement(editPart);
 	}
 	
 	
 	public LayoutManager getLayoutManager() {
 		return null;
 	}
 	
 	/**
 	 * Calls the layout() method of the layout for all the sub figures
 	 * @param fig
 	 */
 	@SuppressWarnings("rawtypes")
 	public void layoutAllChildren(IFigure fig) {
 		if (fig == null) return;
 		LayoutManager manager = fig.getLayoutManager();
 		if (manager != null) {
 			manager.layout(fig);
 		}
 		List children = fig.getChildren();
 		for (Iterator iterator = children.iterator(); iterator.hasNext();) {
 			IFigure child = (IFigure) iterator.next();
 			if (child != null) {
 				layoutAllChildren(child);
 			}
 		}
 	}
 	
 	public NodeFigure createNodePlate() {
 		return null;
 	}
 	
 	public void refreshParent() {
 		editPart.getParent().refresh();
 	}
 	
 	public void refreshAllParents() {
 		// not implemented yet
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void refreshAllChildren() {
 		List<EditPart> children = editPart.getChildren();
 		for (EditPart child : children) {
 			child.refresh();
 			getMindProxyFor(child).refreshAllChildren();
 		}
 	}
 	
 	/**
 	 * Default layout is OK but we don't want the spacing
 	 */
 	public IFigure setupContentPane(IFigure nodeShape) {
 		return null;
 	}
 
 
 	public boolean addChildVisual(EditPart childEditPart, int index) {
 		return false;
 	}
 
 
 	public boolean addFixedChild(EditPart childEditPart) {
 		return false;
 	}
 
 	public boolean removeFixedChild(EditPart childEditPart) {
 		return false;
 	}
 
 	public DragTracker getDragTracker(Request request) {
 		return null;
 	}
 
 	public boolean refreshBounds() {
 		return false;
 	}
 	
 	public void setLayoutManager(IFigure figure) {}
 
 
 	public void refresh() {}
 
 	public LayoutEditPolicy createLayoutEditPolicy() {
 		return null;
 	}
 
 	public Color getMindBorderColor() {
 		return IFractalShape.GRAY;
 	}
 	
 	public Color getMindBackgroundColor() {
 		return IFractalShape.GRAY;
 	}
 	
 	public Dimension getMindPreferredSize() {
 		return new Dimension(500,500);
 	}
 
 	public boolean setLayoutConstraint(EditPart child, IFigure childFigure,
 			Object constraint) {
 		return false;
 	}
 
 	public void handleNotificationEvent(Notification notification) {}
 	
 }
