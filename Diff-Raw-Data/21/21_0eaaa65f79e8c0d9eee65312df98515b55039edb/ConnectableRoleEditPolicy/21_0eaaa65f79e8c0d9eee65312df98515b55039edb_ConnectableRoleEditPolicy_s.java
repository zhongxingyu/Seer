 /*
  * Copyright 2005 Pi4 Technologies Ltd
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  *
  * Change History:
  * Jul 14, 2005 : Initial version created by gary
  */
 package org.savara.tools.scenario.designer.policies;
 
 import org.eclipse.draw2d.ConnectionAnchor;
 import org.eclipse.draw2d.FigureCanvas;
 import org.eclipse.draw2d.Viewport;
 
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gef.requests.CreateConnectionRequest;
 import org.eclipse.gef.requests.ReconnectRequest;
 import org.savara.scenario.model.*;
 import org.savara.tools.scenario.designer.commands.*;
 import org.savara.tools.scenario.designer.model.*;
 import org.savara.tools.scenario.designer.parts.*;
 import org.savara.tools.scenario.designer.view.ViewSupport;
 
 /**
  * This class provides the edit policy for the connectable
  * types container.
  */
 public class ConnectableRoleEditPolicy 
 		extends org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy {
 
     /**
      * 
      */
     public ConnectableRoleEditPolicy() {
         super();
         // TODO Auto-generated constructor stub
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getConnectionCompleteCommand(org.eclipse.gef.requests.CreateConnectionRequest)
      */
     protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
         Command command =null;
         Command requestCommand=request.getStartCommand();
         
         if (requestCommand instanceof ConnectionCommand) {
         	RoleConnectionCommand newCommand=new RoleConnectionCommand();
         	
         	newCommand.setSource(((ConnectionCommand)requestCommand).getSource());
         	newCommand.setLink(((ConnectionCommand)requestCommand).getLink());
         	
         	requestCommand = newCommand;
         }
 
         if (requestCommand instanceof RoleConnectionCommand) {
             command = requestCommand;
             
         	((RoleConnectionCommand)command).setTarget((Role)getRole());
 
         	ScenarioBaseEditPart target=null;
 			FigureCanvas canvas=(FigureCanvas)
			getHost().getViewer().getControl();
 
 			Viewport port = canvas.getViewport();
 			org.eclipse.draw2d.geometry.Point point=
 				request.getLocation().getTranslated(port.getClientArea().getTopLeft());
 			
         	if (request.getTargetEditPart() instanceof ScenarioBaseEditPart) {
         		target = ((ScenarioBaseEditPart)request.getTargetEditPart()).getScenarioDiagram().findEditPartAtLocation(point,
 						Event.class);
         		if (target == null) {
         			target = ((ScenarioBaseEditPart)request.getTargetEditPart()).getScenarioDiagram().findEditPartAtLocation(point,
         					Scenario.class);
         		}
         	} else if (request.getSourceEditPart() instanceof ScenarioBaseEditPart) {
         		target = ((ScenarioBaseEditPart)request.getSourceEditPart()).getScenarioDiagram().findEditPartAtLocation(point,
         				Event.class);
         		if (target == null) {
         			target = ((ScenarioBaseEditPart)request.getSourceEditPart()).getScenarioDiagram().findEditPartAtLocation(point,
         					Scenario.class);
         		}
         	}
         	
         	if (target != null) {
         		((RoleConnectionCommand)command).setScenario(target.getScenarioDiagram().getScenario());
         	}
         	
         	/* GPB: WAS 
         	if (request.getTargetEditPart() instanceof ScenarioBaseEditPart) {
         		target = ((ScenarioBaseEditPart)request.getTargetEditPart()).getScenarioDiagram().findEditPartAtLocation(point,
 						ScenarioObject.class);
         		if (target == null) {
         			target = ((ScenarioBaseEditPart)request.getTargetEditPart()).getScenarioDiagram().findEditPartAtLocation(point,
         					Scenario.class);
         		}
         	} else if (request.getSourceEditPart() instanceof ScenarioBaseEditPart) {
         		target = ((ScenarioBaseEditPart)request.getSourceEditPart()).getScenarioDiagram().findEditPartAtLocation(point,
 						ScenarioObject.class);
         		if (target == null) {
         			target = ((ScenarioBaseEditPart)request.getSourceEditPart()).getScenarioDiagram().findEditPartAtLocation(point,
         					Scenario.class);
         		}
         	}
         	*/
        	
         	// GPB: TODO Mislocating target editpart - put
         	// into to ensure does not try to drop on message
         	// event part
         	// Need to check how an edit part is found based
         	// on bounds and locations
         	
            	if (target instanceof MessageEventEditPart) {
             	if (request.getTargetEditPart() instanceof ScenarioBaseEditPart) {
             		target = ((ScenarioBaseEditPart)request.getTargetEditPart()).getScenarioDiagram().findEditPartAtLocation(point,
             								Event.class);
             	} else if (request.getSourceEditPart() instanceof ScenarioBaseEditPart) {
             		target = ((ScenarioBaseEditPart)request.getSourceEditPart()).getScenarioDiagram().findEditPartAtLocation(point,
             								Event.class);
             	}
         		return(null);
         	}
         	
            	/* GPB: WAS
            	if (target instanceof MessageEventEditPart) {
             	if (request.getTargetEditPart() instanceof ScenarioBaseEditPart) {
             		target = ((ScenarioBaseEditPart)request.getTargetEditPart()).getScenarioDiagram().findEditPartAtLocation(point,
             								ScenarioObject.class);
             	} else if (request.getSourceEditPart() instanceof ScenarioBaseEditPart) {
             		target = ((ScenarioBaseEditPart)request.getSourceEditPart()).getScenarioDiagram().findEditPartAtLocation(point,
             								ScenarioObject.class);
             	}
         		return(null);
         	}
         	*/
         	Scenario scenario=null;
         	
         	if (target != null) {
         		scenario = target.getScenarioDiagram().getScenario();
         	}
         	
 			// Calculate the index position
 			int y=port.getClientArea().y + request.getLocation().y
 						- ViewSupport.getHeaderPadding(scenario, target.getModel()); /* -
 						ViewSupport.INITIAL_YPADDING
 							- ViewSupport.YPADDING;*/
 			y -= target.getFigure().getBounds().y;
 		
 			int index=0;
 			java.util.List list=ModelSupport.getChildren(target.getModel());
 			
 			for (int i=0; y > 0 && i < list.size(); i++) {
 				index++;
 				
 				y -= ViewSupport.getHeight(list.get(i),
 							target.getScenarioDiagram());
 				
 				y -= ViewSupport.getPadding(list, i);
 			}
 			
 	        ((RoleConnectionCommand)command).setTargetParent(target.getModel());
 
 	        if (((RoleConnectionCommand)command).getSourceParent() == target.getModel()) {
 	        	index++;
 	        }
 	        
 	        ((RoleConnectionCommand)command).setTargetIndex(index);
         }
         
         return command;
     }
 
     /**
      * Feedback should be added to the scaled feedback layer.
      * @see org.eclipse.gef.editpolicies.GraphicalEditPolicy#getFeedbackLayer()
      */
     protected org.eclipse.draw2d.IFigure getFeedbackLayer() {
     	return getLayer(org.eclipse.gef.LayerConstants.SCALED_FEEDBACK_LAYER);
     }
 
     protected RoleEditPart getConnectableEditPart() {
         return((RoleEditPart)getHost());
     }
 
     protected Role getRole() {
         return((Role)getHost().getModel());
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getConnectionCreateCommand(org.eclipse.gef.requests.CreateConnectionRequest)
      */
     protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
     	RoleConnectionCommand command=null;
         
     	ScenarioBaseEditPart source=null;
     	
 		FigureCanvas canvas=(FigureCanvas)
 		getHost().getViewer().getControl();
 
 		Viewport port = canvas.getViewport();
 	
 		org.eclipse.draw2d.geometry.Point point=
 			request.getLocation().getTranslated(port.getClientArea().getTopLeft());
 		
     	if (request.getSourceEditPart() instanceof ScenarioBaseEditPart) {
     		source = ((ScenarioBaseEditPart)request.getSourceEditPart()).getScenarioDiagram().findEditPartAtLocation(point,
     				Group.class);
     		if (source == null) {
         		source = ((ScenarioBaseEditPart)request.getSourceEditPart()).getScenarioDiagram().findEditPartAtLocation(point,
 						Scenario.class);
     		}
     	/*
     	} else if (request.getSourceEditPart() instanceof RoleEditPart) {
            	source = ((RoleEditPart)request.getSourceEditPart()).getScenarioDiagram().findEditPartAtLocation(point,
     						Scenario.class);
     	*/
     	} else if (request.getTargetEditPart() instanceof ScenarioBaseEditPart) {
     		source = ((ScenarioBaseEditPart)request.getTargetEditPart()).getScenarioDiagram().findEditPartAtLocation(point,
     				Group.class);
     		if (source == null) {
     			source = ((ScenarioBaseEditPart)request.getTargetEditPart()).getScenarioDiagram().findEditPartAtLocation(point,
     					Scenario.class);
     		}
     	/*
     	} else if (request.getTargetEditPart() instanceof RoleEditPart) {
    			source = ((RoleEditPart)request.getTargetEditPart()).getScenarioDiagram().findEditPartAtLocation(point,
 					Group.class);
    			source = ((RoleEditPart)request.getTargetEditPart()).getScenarioDiagram().findEditPartAtLocation(point,
 					Scenario.class);
 		*/
     	}
     	
     	/* GPB: WAS
     	if (request.getSourceEditPart() instanceof ScenarioBaseEditPart) {
     		source = ((ScenarioBaseEditPart)request.getSourceEditPart()).getScenarioDiagram().findEditPartAtLocation(point,
     								ScenarioObject.class);
     		if (source == null) {
         		source = ((ScenarioBaseEditPart)request.getSourceEditPart()).getScenarioDiagram().findEditPartAtLocation(point,
 						Scenario.class);
     		}
     	} else if (request.getTargetEditPart() instanceof ScenarioBaseEditPart) {
     		source = ((ScenarioBaseEditPart)request.getTargetEditPart()).getScenarioDiagram().findEditPartAtLocation(point,
 					ScenarioObject.class);
     		if (source == null) {
     			source = ((ScenarioBaseEditPart)request.getTargetEditPart()).getScenarioDiagram().findEditPartAtLocation(point,
     					Scenario.class);
     		}
     	}
 
     	 */
         //if (getParticipant() != null &&
         //		getParticipant().getDirection() ==
         //			MessageDirection.SEND) {
     	if (source instanceof GroupEditPart ||
     			source instanceof ScenarioEditPart) {
 
           	command = new RoleConnectionCommand();
         	
 	        command.setLink((Link) request.getNewObject());
 	
 	        command.setSource((Role)getRole());
 	        request.setStartCommand(command);
 	        
 			// Calculate the index position
 			int y=port.getClientArea().y + request.getLocation().y
 						- ViewSupport.getHeaderPadding(source.getScenarioDiagram().getScenario(),
 											source.getModel()); /* -
 						ViewSupport.INITIAL_YPADDING
 							- ViewSupport.YPADDING;*/
 			y -= source.getFigure().getBounds().y;
 		
 			int index=0;
 			java.util.List list=ModelSupport.getChildren(source.getModel());
 			
 			for (int i=0; y > 0 && i < list.size(); i++) {
 				index++;
 				
 				y -= ViewSupport.getHeight(list.get(i), source.getScenarioDiagram());
 				
 				y -= ViewSupport.getPadding(list, i);
 			}
 	
 	        command.setSourceIndex(index);
 	        command.setSourceParent(source.getModel());
 
 	        if (source != null) {
 	       		((RoleConnectionCommand)command).setScenario(source.getScenarioDiagram().getScenario());
 	    	}
         }
 
         return(command);
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getReconnectTargetCommand(org.eclipse.gef.requests.ReconnectRequest)
      */
     protected Command getReconnectTargetCommand(ReconnectRequest request) {
         RoleConnectionCommand command = null; //new ParticipantConnectionCommand();
         
         /*
         MessageLink rel = (MessageLink)((MessageLinkEditPart) request.getConnectionEditPart()).getModel();
         command.setMessageLink(rel);
         command.setOldTarget(rel.getTarget());
         command.setTarget((MessageEvent)getParticipant());
 */
         return command;
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getReconnectSourceCommand(org.eclipse.gef.requests.ReconnectRequest)
      */
     protected Command getReconnectSourceCommand(ReconnectRequest request) {
     	RoleConnectionCommand command = null; //new ParticipantConnectionCommand();
     	/*
         MessageLink rel = (MessageLink)((MessageLinkEditPart) request.getConnectionEditPart()).getModel();
         command.setMessageLink(rel);
         command.setOldSource(rel.getSource());
         command.setSource((MessageEvent)getParticipant());
 */
         return command;
     }
 }
