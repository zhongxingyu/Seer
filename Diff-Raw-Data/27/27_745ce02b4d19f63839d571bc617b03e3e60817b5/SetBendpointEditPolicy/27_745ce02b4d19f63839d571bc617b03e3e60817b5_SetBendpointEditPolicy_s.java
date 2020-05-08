 /**
 * Copyright (C) 2013  Luc Hermans
  * 
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU General Public License as published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with this program.  If
  * not, see <http://www.gnu.org/licenses/>.
  * 
  * Contact information: kozzeluc@gmail.com.
  */
 package org.lh.dmlj.schema.editor.policy;
 
 import org.eclipse.draw2d.Connection;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.PrecisionPoint;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.gef.GraphicalEditPart;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gef.editpolicies.BendpointEditPolicy;
 import org.eclipse.gef.requests.BendpointRequest;
 import org.lh.dmlj.schema.ConnectionPart;
 import org.lh.dmlj.schema.DiagramLocation;
 import org.lh.dmlj.schema.DiagramNode;
 import org.lh.dmlj.schema.editor.command.CreateBendpointCommand;
 import org.lh.dmlj.schema.editor.command.DeleteBendpointCommand;
 import org.lh.dmlj.schema.editor.command.LockEndpointsCommand;
 import org.lh.dmlj.schema.editor.command.MoveBendpointCommand;
 import org.lh.dmlj.schema.editor.figure.RecordFigure;
 import org.lh.dmlj.schema.editor.part.SetEditPart;
 
 public class SetBendpointEditPolicy extends BendpointEditPolicy {
 
 	private ConnectionPart connectionPart;
 	private SetEditPart    editPart;
 	
 	public SetBendpointEditPolicy(SetEditPart editPart) {
 		super();
 		this.editPart = editPart;
 		this.connectionPart = editPart.getModel();
 	}
 	
 	@Override
 	protected Command getCreateBendpointCommand(BendpointRequest request) {						
 		
 		// we will deliver a composite command, consisting of a command
 		// that locks the current owner and member endpoints and a 
 		// command that actually creates the bendpoint...
 		
 		// get the current zoomLevel...
 		double zoomLevel = connectionPart.getMemberRole()
 									     .getSet()
 									     .getSchema()
 									     .getDiagramData()
 									     .getZoomLevel();
 		
 		// get the owner endpoint location as an (unscaled) offset pair 
 		// within the owner figure; if this is a system owned indexed 
 		// set, forget about the owner endpoint location because it is 
 		// always anchored at the same location...
 		PrecisionPoint ownerEndpoint = null;
 		if (connectionPart.getMemberRole().getSet().getSystemOwner() == null) {
 			// user owned set
 			GraphicalEditPart ownerEditPart = 
 				(GraphicalEditPart) editPart.getViewer()
 											.getEditPartRegistry()
 									  		.get(connectionPart.getMemberRole()
 									  						   .getSet()
 									  						   .getOwner()
 									  						   .getRecord());
 			Rectangle ownerBounds = 
 				ownerEditPart.getFigure().getBounds().getCopy();
 			ownerEditPart.getFigure().translateToAbsolute(ownerBounds);
 			Connection connection;
 			if (connectionPart.getMemberRole().getConnectionParts().size() == 1) {
 				// in a one piece set line, the connection directly orriginates 
 				// from the owner record
 				connection = getConnection();
 			} else {
 				// we're in a two piece set line, go get the first connection
 				ConnectionPart connectionPart1 =
 					connectionPart.getMemberRole().getConnectionParts().get(0);
 				SetEditPart setEditPart = 
 					(SetEditPart) editPart.getViewer()
 										  .getEditPartRegistry()
 										  .get(connectionPart1);
 				connection = setEditPart.getConnectionFigure();
 			}
 			Point firstPoint = 
 				connection.getPoints().getFirstPoint().getCopy();
 			ownerEditPart.getFigure().translateToAbsolute(firstPoint);
 			ownerEndpoint = 
 				new PrecisionPoint(firstPoint.x - ownerBounds.x,
 							   	   firstPoint.y - ownerBounds.y);
 			RecordFigure.unscale(ownerEndpoint, 
 								 (RecordFigure)ownerEditPart.getFigure(), zoomLevel);
 		}
 		
 		// get the member endpoint location as an (unscaled) offset pair 
 		// within the member figure...
 		GraphicalEditPart memberEditPart = 
 			(GraphicalEditPart) editPart.getViewer()
 										.getEditPartRegistry()
 								  		.get(connectionPart.getMemberRole()
 								  						   .getRecord());
 		Rectangle memberBounds = 
 			memberEditPart.getFigure().getBounds().getCopy();
 		memberEditPart.getFigure().translateToAbsolute(memberBounds);
 		Connection connection;
 		if (connectionPart.getMemberRole().getConnectionParts().size() == 1) {
 			// in a one piece set line, the connection directly points to the
 			// member record
 			connection = getConnection();
 		} else {
 			// we're in a two piece set line, go get the second connection
 			ConnectionPart connectionPart2 =
 				connectionPart.getMemberRole().getConnectionParts().get(1);
 			SetEditPart setEditPart = 
 				(SetEditPart) editPart.getViewer()
 									  .getEditPartRegistry()
 									  .get(connectionPart2);
 			connection = setEditPart.getConnectionFigure();
 		}
 		Point lastPoint = 
 			connection.getPoints().getLastPoint().getCopy();
 		memberEditPart.getFigure().translateToAbsolute(lastPoint);
 		PrecisionPoint memberEndpoint = 
 			new PrecisionPoint(lastPoint.x - memberBounds.x,
 							   lastPoint.y - memberBounds.y);
 		RecordFigure.unscale(memberEndpoint, 
 				 			 (RecordFigure)memberEditPart.getFigure(), zoomLevel);
 		
         // create the lock endpoints command; note that the endpoints 
 		// may already be locked, but the command takes care of that...
 		LockEndpointsCommand lockEndpointsCommand =
 			new LockEndpointsCommand(connectionPart.getMemberRole(), 
 									 ownerEndpoint, memberEndpoint);
         
 		// calculate the bendpoint location as an unscaled offset to the
 		// owner figure...
         PrecisionPoint p = new PrecisionPoint(request.getLocation().x,
         									  request.getLocation().y); 				
         getConnection().translateToRelative(p);
         PrecisionPoint ownerLocation = getOwnerFigureLocation();
         p.setPreciseX(p.preciseX() - ownerLocation.preciseX());                
         p.setPreciseY(p.preciseY() - ownerLocation.preciseY());                
         
         // create the create bendpoint command...
         CreateBendpointCommand createBendpointCommand = 
			new CreateBendpointCommand(connectionPart, request.getIndex(), p.x, 
									   p.y);
         
        // chain both commands together, forming the final command...
        return lockEndpointsCommand.chain(createBendpointCommand);
 	}
 
 	@Override
 	protected Command getDeleteBendpointCommand(BendpointRequest request) {
 		DeleteBendpointCommand command = 
 			new DeleteBendpointCommand(connectionPart, request.getIndex());
 		return command;
 	}
 
 	@Override
 	protected Command getMoveBendpointCommand(BendpointRequest request) {
 		
 		// calculate the bendpoint location as an unscaled offset to the
 		// owner figure...
         PrecisionPoint p = new PrecisionPoint(request.getLocation().x,
         									  request.getLocation().y); 				
         getConnection().translateToRelative(p);
         PrecisionPoint ownerLocation = getOwnerFigureLocation();
         p.setPreciseX(p.preciseX() - ownerLocation.preciseX());                
         p.setPreciseY(p.preciseY() - ownerLocation.preciseY());
 		
         // create the move bendpoint command...
         MoveBendpointCommand command = 
 			new MoveBendpointCommand(connectionPart, request.getIndex(), p.x, 
 									 p.y);
 		
         return command;
 	}	
 	
 	/**
 	 * @return the location, in absolute coordinates and as stored in the model
 	 *         (i.e. unscaled), for the RecordFigure or IndexFigure that acts as 
 	 *         the owner of this set
 	 */
 	private PrecisionPoint getOwnerFigureLocation() {		
 		DiagramNode owner;
 		if (connectionPart.getMemberRole().getSet().getSystemOwner() != null) {
 			// system owned set
 			owner = connectionPart.getMemberRole().getSet().getSystemOwner();			 
 		} else {
 			// user owned set
 			owner = connectionPart.getMemberRole().getSet().getOwner().getRecord();
 		}
 		DiagramLocation location = owner.getDiagramLocation();
 		return new PrecisionPoint(location.getX(), location.getY());
 	}
 	
 }
