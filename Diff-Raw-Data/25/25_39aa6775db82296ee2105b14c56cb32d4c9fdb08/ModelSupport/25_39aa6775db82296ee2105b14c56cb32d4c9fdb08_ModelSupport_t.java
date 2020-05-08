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
  * 16 Feb 2007 : Initial version created by gary
  */
 package org.savara.tools.scenario.designer.model;
 
 import java.util.logging.Logger;
 import org.savara.scenario.model.*;
 
 public class ModelSupport {
 	
 	private static Logger logger = Logger.getLogger(ModelSupport.class.getName());
 
 	public static boolean isSame(Object component1, Object component2) {
 		boolean ret=(component1 == component2);
 		
 		if (!ret) {
 			if (component1 instanceof Event && component2 instanceof Event) {
 				ret = ((Event)component1).getId().equals(((Event)component2).getId());
 			} else if (component1 instanceof Role && component2 instanceof Role) {
 				ret = ((Role)component1).getId().equals(((Role)component2).getId());
 			}
 		}
 		
 		return(ret);
 	}
 	
 	public static java.util.List<Event> getChildren(Object component) {
 		java.util.List<Event> ret=null;
 		
 		if (component instanceof Scenario) {
 			ret = ((Scenario)component).getEvent();
 		} else if (component instanceof Group) {
 			ret = ((Group)component).getEvent();
 		}
 		
 		return(ret);
 	}
 	
 	public static void getEventsForRole(Role role, java.util.List<Event> events,
 							java.util.List<Event> results) {
 		for (Event event : events) {
 			if (event instanceof Group) {
 				getEventsForRole(role, ((Group)event).getEvent(), results);
 			} else if (event instanceof RoleEvent &&
 					((RoleEvent)event).getRole() == role) {
 				results.add(event);
 			}
 		}
 	}
 	
 	public static Object getParent(Scenario scenario, Object component) {
 		Object ret=null;
 		
 		// Need to scan the scenario as the components don't have reference to their parents
 		if (component instanceof Event) {
 			ret = getParentFromEventList(scenario, scenario.getEvent(), component);
 		} else if (component instanceof Link) {
 			if (scenario.getLink().contains(component)) {
 				ret = scenario;
 			}
 		} else if (component instanceof Role) {
 			if (scenario.getRole().contains(component)) {
 				ret = scenario;
 			}
 		}
 		
 		return(ret);
 	}
 	
 	protected static Object getParentFromEventList(Object parent, java.util.List<Event> events,
 								Object component) {
 		Object ret=null;
 		
 		if (events.contains(component)) {
 			ret = parent;
 		} else {
 			for (int i=0; ret == null && i < events.size(); i++) {
 				Event evt=events.get(i);
 				
 				if (evt instanceof Group) {
 					ret = getParentFromEventList(evt, ((Group)evt).getEvent(), component);
 				}
 			}
 		}
 		
 		return(ret);
 	}
 	
 	public static int getChildIndex(Object parent, Object child) {
 		int ret=0;
 		
 		if (child instanceof Role) {
			if (parent instanceof Scenario) {
				ret = ((Scenario)parent).getRole().indexOf(child);
			} else {
				logger.warning("Invalid parent ("+parent+") for child of type 'Role'");
			}
 		} else {
			java.util.List<Event> children=getChildren(parent);
 		
 			ret = children.indexOf(child);
 		}
 		
 		return(ret);
 	}
 	
 	/**
 	 * This method adds a new child to the parent.
 	 * 
 	 * @param parent The parent
 	 * @param child The child
 	 * @param index The index of the new child
 	 */
 	public static void addChild(Object parent, Object child,
 						int index) {
 
 		if (parent != null && child != null) {
 			java.util.List list=null;
 			
 			if (child instanceof Role && parent instanceof Scenario) {
 				list = ((Scenario)parent).getRole();
 			} else {
 				list = getChildren(parent);		
 			}
 	
 			if (list != null) {
 				
 				if (index == -1 || index > list.size()) {
 					list.add(child);
 				} else {
 					list.add(index, child);
 				}
 				
 			} else {
 				logger.severe("DON'T KNOW HOW TO ADD: child class="+child+
 						" to parent="+parent);				
 			}
 		}
 	}
 	
 	/**
 	 * This method removes a child from the parent.
 	 * 
 	 * @param parent The parent
 	 * @param child The child
 	 */
 	public static void removeChild(Object parent, Object child) {
 		
 		if (parent != null && child != null) {
 			java.util.List list=null;
 			
 			if (parent instanceof Scenario &&
 					child instanceof Role) {				
 				list = ((Scenario)parent).getRole();
 			} else {
 				list = getChildren(parent);		
 			}
 			
 			if (list != null) {
 				list.remove(child);
 								
 			} else {
 				logger.severe("DON'T KNOW HOW TO REMOVE: child class="+child+
 						" to parent="+parent);				
 			}
 		}
 	}
 	
 	public static boolean isValidTarget(Object child, Object parent) {
 		boolean ret=false;
 		
 		if ((parent instanceof Scenario  ||
 				parent instanceof Group) &&
 				child instanceof Role) {
 			ret = true;
 		} else if ((parent instanceof Scenario ||
 					parent instanceof Group) &&
 					child instanceof Event) {
 			ret = true;
 		} else if ((parent instanceof Scenario || parent
 					instanceof Group) &&
 					child instanceof Link) {
 			ret = true;
 		}
 		
 		logger.info("Is valid target: parent="+parent+" child="+child+" ret="+ret);
 		
 		return(ret);
 	}
 	
 	public static java.util.List<Link> getSourceConnections(Scenario scenario, Object component) {
 		java.util.List<Link> ret=new java.util.Vector<Link>();
 		
 		if (component instanceof MessageEvent) {
 			java.util.List<Link> links=scenario.getLink();
 			
 			for (int i=0; i < links.size(); i++) {
 				Link link=(Link)links.get(i);
 				
 				if (link.getSource() == component) {
 					ret.add(link);
 				}
 			}
 		}
 		
 		return(ret);
 	}
 	
 	public static java.util.List<Link> getTargetConnections(Scenario scenario, Object component) {
 		java.util.List<Link> ret=new java.util.Vector<Link>();
 		
 		if (component instanceof MessageEvent) {
 			java.util.List<Link> links=scenario.getLink();
 			
 			for (int i=0; i < links.size(); i++) {
 				Link link=(Link)links.get(i);
 				
 				if (link.getTarget() == component) {
 					ret.add(link);
 				}
 			}
 		}
 		
 		return(ret);
 	}
 }
