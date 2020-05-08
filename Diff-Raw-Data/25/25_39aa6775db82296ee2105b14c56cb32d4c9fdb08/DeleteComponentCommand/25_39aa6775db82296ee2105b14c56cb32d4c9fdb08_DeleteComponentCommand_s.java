 /*
  * Copyright 2005-7 Pi4 Technologies Ltd
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
  * Feb 20, 2007 : Initial version created by gary
  */
 package org.savara.tools.scenario.designer.commands;
 
 import org.savara.scenario.model.*;
 import org.savara.tools.scenario.designer.model.*;
 import org.eclipse.gef.commands.Command;
 
 /**
  * This class implements the activity deletion command.
  */
 public class DeleteComponentCommand
 			extends org.eclipse.gef.commands.Command {
 	
 	public DeleteComponentCommand() {
 	}
 	
 	public boolean canExecute() {
 		boolean ret=false;
 		
 		if (m_parent != null && m_child != null) {
 			ret = true;
 		}
 		
 		return(ret);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.gef.commands.Command#execute()
 	 */
 	public void execute() {
 		
 		if (m_child instanceof MessageEvent) {
 			
			java.util.List list=ModelSupport.getSourceConnections(m_scenario, m_child);
 			
 			for (int i=list.size()-1; i >= 0; i--) {
 				Link link=(Link)list.get(i);
 				
 				link.setSource(null);
 				link.setTarget(null);
 				
 				m_scenario.getLink().remove(link);
 			}
 			
 			list=ModelSupport.getTargetConnections(m_scenario, m_child);
 			
 			for (int i=list.size()-1; i >= 0; i--) {
 				Link link=(Link)list.get(i);
 				
 				link.setSource(null);
 				link.setTarget(null);
 				
 				m_scenario.getLink().remove(link);
 			}
 
 		} else if (m_child instanceof Role) {
 			// Construct deletion commands for each nessage event
 			// related to the participant
 			
			java.util.List<Event> results=new java.util.Vector();
 			
 			ModelSupport.getEventsForRole((Role)m_child, m_scenario.getEvent(), results);
 			
			for (Event event : results) {
 				DeleteComponentCommand command=
 					new DeleteComponentCommand();
 				
 				command.setScenario(m_scenario);
 				command.setChild(event);
 				
 				Object parent=ModelSupport.getParent(m_scenario, event);
 				
 				command.setParent(parent);
 				
 				command.setIndex(ModelSupport.getChildIndex(
 						parent, event));
 				
 				m_propagatedCommands.add(command);
 			}			
 		}
 		
 		for (int i=0; i < m_propagatedCommands.size(); i++) {
 			Command command=(Command)m_propagatedCommands.get(i);
 			
 			command.execute();
 		}
 
 		ModelSupport.removeChild(m_parent, m_child);
 		
 		if (m_child instanceof Group) {
 
 			// Scan list of message links to see if any no longer have
 			// a message event that is attached to the scenario - and
 			// then save these in case of an undo
 			for (int i=m_scenario.getLink().size()-1;
 						i >= 0; i--) {
 				Link link=(Link)m_scenario.getLink().get(i);
 				
 				if ((link.getSource() != null &&
 						ModelSupport.getParent(m_scenario, link.getSource()) == null) ||
 					(link.getTarget() != null &&
 							ModelSupport.getParent(m_scenario, link.getTarget()) == null)) {
 					
 					// Remove link
 					m_scenario.getLink().remove(link);
 					
 					m_removedMessageLinks.add(0, link);
 				}
 			}
 		}
 	}
 	
 	public Object getParent() {
 		return(m_parent);
 	}
 	
 	public void redo() {
 		execute();
 	}
 	
 	public void setScenario(Scenario scenario) {
 		m_scenario = scenario;
 	}
 	
 	public void setChild(Object newNode) {
 		m_child = newNode;
 		
 		// Determine connected children
 		if (newNode instanceof MessageEvent) {
 			
 			java.util.List<Link> list=ModelSupport.getSourceConnections(m_scenario, newNode);
 			
 			for (int i=0; i < list.size(); i++) {
 				Link link=(Link)list.get(i);
 				
 				m_targetConnectedEvents.add((MessageEvent)link.getTarget());
 			}
 			
 			list=ModelSupport.getTargetConnections(m_scenario, newNode);
 			
 			for (int i=0; i < list.size(); i++) {
 				Link link=(Link)list.get(i);
 				
 				m_sourceConnectedEvents.add((MessageEvent)link.getSource());
 			}
 		}
 	}
 	
 	public void setIndex(int index) {
 		m_index = index;
 	}
 	
 	public void setParent(Object newParent) {
 		m_parent = newParent;
 	}
 	
 	public void undo() {
 		
 		ModelSupport.addChild(m_parent, m_child, m_index);
 		
 		for (int i=m_propagatedCommands.size()-1; i >= 0; i--) {
 			Command command=(Command)m_propagatedCommands.get(i);
 			
 			command.undo();
 		}
 		
 		m_propagatedCommands.clear();
 		
 		if (m_child instanceof MessageEvent) {
 			/* TODO: GPB: need scenario */
 			for (int i=0; i < m_sourceConnectedEvents.size(); i++) {
 				Link link=new Link();
 				
 				link.setSource((MessageEvent)m_sourceConnectedEvents.get(i));
 				link.setTarget((MessageEvent)m_child);
 				
 				m_scenario.getLink().add(link);
 			}
 			
 			for (int i=0; i < m_targetConnectedEvents.size(); i++) {
 				Link link=new Link();
 				
 				link.setSource((MessageEvent)m_child);
 				link.setTarget((MessageEvent)m_targetConnectedEvents.get(i));
 				
 				m_scenario.getLink().add(link);
 			}
 			 /**/
 		} else if (m_child instanceof Group) {
 			
 			for (int i=0; i < m_removedMessageLinks.size(); i++) {
 				m_scenario.getLink().add(
 						m_removedMessageLinks.get(i));
 			}
 			
 			m_removedMessageLinks.clear();
 		}
 	}
 
 	private Scenario m_scenario=null;
 	private Object m_child=null;
 	private Object m_parent=null;
 	private int m_index = -1;
 	private java.util.Vector<MessageEvent> m_sourceConnectedEvents=new java.util.Vector<MessageEvent>();
 	private java.util.Vector<MessageEvent> m_targetConnectedEvents=new java.util.Vector<MessageEvent>();
 	private java.util.Vector<Link> m_removedMessageLinks=new java.util.Vector<Link>();
 	private java.util.Vector<Command> m_propagatedCommands=new java.util.Vector<Command>();
 }
