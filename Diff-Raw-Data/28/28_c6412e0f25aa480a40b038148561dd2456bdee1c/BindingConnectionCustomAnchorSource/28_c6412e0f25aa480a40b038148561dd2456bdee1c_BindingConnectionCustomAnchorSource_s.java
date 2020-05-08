 package org.ow2.mindEd.adl.editor.graphic.ui.custom.layouts;
 
 import org.eclipse.draw2d.AbstractConnectionAnchor;
 import org.eclipse.draw2d.ConnectionAnchor;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.gef.ConnectionEditPart;
 import org.eclipse.gef.GraphicalEditPart;
 import org.eclipse.gmf.runtime.notation.View;

 import org.ow2.mindEd.adl.InterfaceDefinition;
 import org.ow2.mindEd.adl.Role;
 
 
 /**
  * @author Yann Davin
  *
  */
 public class BindingConnectionCustomAnchorSource extends AbstractConnectionAnchor implements
 		ConnectionAnchor {
 	ConnectionEditPart bindingEditPart;
 	
 	public BindingConnectionCustomAnchorSource(ConnectionEditPart bindingEditPart) {
 		super(((GraphicalEditPart)bindingEditPart.getSource()).getFigure());
 		this.bindingEditPart = bindingEditPart;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.draw2d.ConnectionAnchor#getLocation(org.eclipse.draw2d.geometry.Point)
 	 */
 	public Point getLocation(Point reference) {
 		return getReferencePoint();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.draw2d.AbstractConnectionAnchor#getReferencePoint()
 	 */
 	public Point getReferencePoint() {
 		
 		if(getOwner() == null || bindingEditPart == null){
 			return null;
 		}
 		
 		GraphicalEditPart sourceEditPart =(GraphicalEditPart)bindingEditPart.getSource();
 		if (sourceEditPart == null) return null;
 		
 		IFigure source =  sourceEditPart.getFigure();
 		if(source == null || source != getOwner()){
 			return null;
 		}
 		
 		Point ref = new Point();
 		InterfaceDefinition sourceItf = ((InterfaceDefinition)((View)sourceEditPart.getModel()).getElement());
 		Role sourceRole = 	sourceItf.getRole();
 				
 //		// A binding between two interfaces of the same component
 //		if(sourceItf.eContainer() == targetItf.eContainer()){
 //			if(sourceRole == Role.REQUIRES){
 //				// TODO remove the constant, calculate it from the figure
 //				ref.x = source.getBounds().x;
 //			}else{
 //				ref.x = source.getBounds().x + source.getBounds().width - 15;
 //			}
 //		}else if(targetItf.eContainer()!= null && sourceItf.eContainer() ==targetItf.eContainer().eContainer()){
 //			ref.x = source.getBounds().x;
 //		}else{
 //			ref.x = source.getBounds().x;
 //		}
 		
 		if(sourceRole == Role.PROVIDES){
 			// TODO remove the constant, calculate it from the figure
 			ref = source.getBounds().getTopRight();
 			ref.translate(-16, 5);
 		}else{
 			ref = source.getBounds().getTopLeft();
 			ref.translate(15, 5);
 		}
 		
 		source.translateToAbsolute(ref);
 		return ref;
 	}
 
 }
