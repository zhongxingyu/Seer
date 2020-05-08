 /*
  * Copyright (c) 2005-2007 Obeo
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Obeo - initial API and implementation
  */
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
 public class BindingConnectionCustomAnchorTarget extends AbstractConnectionAnchor
 		implements ConnectionAnchor {
 
 	ConnectionEditPart bindingEditPart;
 	
 	public BindingConnectionCustomAnchorTarget(ConnectionEditPart bindingEditPart) {
 		super(bindingEditPart.getTarget()!=null?((GraphicalEditPart)bindingEditPart.getTarget()).getFigure():null);
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
 		
 		GraphicalEditPart targetEditPart =(GraphicalEditPart)bindingEditPart.getTarget();
 		if (targetEditPart == null) return null;
 		
 		IFigure target =  targetEditPart.getFigure();
 		if(target == null || target != getOwner()){
 			return null;
 		}
 		
 		Point ref = new Point();
 		InterfaceDefinition targetItf = ((InterfaceDefinition)((View)targetEditPart.getModel()).getElement());
 		Role targetRole = 	targetItf.getRole();
 					
 		if(targetRole == Role.PROVIDES){
 			// TODO remove the constant, calculate it from the figure
 			ref = target.getBounds().getTopRight();
 			ref.translate(-16, 5);
 		}else{
 			ref = target.getBounds().getTopLeft();
 			ref.translate(15, 5);
 		}
 		
 		target.translateToAbsolute(ref);
 		return ref;
 		
 	}
 
 }
