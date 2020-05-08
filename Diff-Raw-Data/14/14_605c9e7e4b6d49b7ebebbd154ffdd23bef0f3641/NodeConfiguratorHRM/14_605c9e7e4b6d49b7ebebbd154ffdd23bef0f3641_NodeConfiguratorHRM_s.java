 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.ui.eclipse.commands.hierarchical;
 
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.HierarchicalRoutingService;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.ElectionEvent;
 import de.tuilmenau.ics.fog.scenario.NodeConfigurator;
 import de.tuilmenau.ics.fog.topology.AutonomousSystem;
 import de.tuilmenau.ics.fog.topology.Node;
 
 /**
  * This class is used to configure nodes that are newly created.
  *
  */
 public class NodeConfiguratorHRM implements NodeConfigurator
 {
 
 //	public static final String NAME = "hierarchical";
 	
 	public NodeConfiguratorHRM()
 	{
 		
 	}
 
 	@Override
 	public void configure(String pName, AutonomousSystem pAS, Node pNode)
 	{
 		// create a new HRM instance for this node
 		HierarchicalRoutingService tHRS = new HierarchicalRoutingService(pNode);
 		
 		// register HRM instance as routing service for the current node
 		pNode.getHost().registerRoutingService(tHRS);
 
		// call the delayed INIT function
		tHRS.createHRMControllerApp();
 
 		// start coordinator election for the created HRM instance if desired
 		if(HRMConfig.Hierarchy.BUILD_STARTS_AFTER_NODE_CONFIGURATION) {
 			if(pAS.getSimulation().getPendingEvents() == null) {
 				pAS.getSimulation().addEvent(new ElectionEvent());
 			} else if (pAS.getSimulation().getPendingEvents().contains(new ElectionEvent())) {
 				/*
 				 * hashCode of ElectionEvent always returns 0 because it is only needed once
 				 * So do nothing now!
 				 */
 			}
 		}
 
 		// set the Bully priority 
 		BullyPriority.configureNode(pNode);
 		
 	}
 }
