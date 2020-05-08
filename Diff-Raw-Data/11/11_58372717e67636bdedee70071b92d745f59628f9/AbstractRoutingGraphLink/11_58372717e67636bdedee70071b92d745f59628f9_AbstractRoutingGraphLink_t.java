 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.hierarchical.management;
 
 import java.io.Serializable;
 
 import de.tuilmenau.ics.fog.routing.Route;
import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * Clusters are built up at bus level at the first hierarchical level. Clusters that are not physically connected to the node
  * are attached neighbor clusters. This class is used in order to determine the distance to a cluster.
  *  
  */
 public class AbstractRoutingGraphLink implements Serializable
 {
 	private static final long serialVersionUID = 3333293111147481060L;
 	public enum LinkType {OBJECT_REF /* node internal object reference */, ROUTE /* network based link */, LOCAL_CONNECTION /* a connection to a local node */, REMOTE_CONNECTION /* a connection to a remote node */};
 
 	/**
 	 * Stores the type of the link
 	 */
 	private LinkType mLinkType;
 
 	/**
 	 * Stores the route to the remote side
 	 */
 	private Route mRoute = null;
 	
 	/**
 	 * Constructor of a node (cluster) connection
 	 * 
 	 * @param pType the type of the link between the two ARG entries
 	 */
 	public AbstractRoutingGraphLink(LinkType pLinkType)
 	{
 		mLinkType = pLinkType;
 	}
 	
 	/**
 	 * Constructor of a node (cluster) connection
 	 * 
 	 * @param pRoute the route to the remote side
 	 */
 	public AbstractRoutingGraphLink(Route pRoute)
 	{
 		mLinkType = LinkType.ROUTE;
 		mRoute = pRoute;
 	}
 
 	/**
 	 * 
 	 * @return Return the type of the connection here.
 	 */
 	public LinkType getLinkType()
 	{
 		return mLinkType;
 	}
 	
 	/**
 	 * Returns the route to the remote side
 	 * 
 	 * @return the route to the remote side
 	 */
 	public Route getRoute()
 	{
 		return mRoute;
 	}
 	
 	/**
 	 * Updates the stored route to the remote side
 	 * 
 	 * @param pNewRoute the new route to the remote side
 	 */
 	public void setRoute(Route pNewRoute)
 	{
		if(HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
			Logging.log(this, ">>> Old route: " + mRoute);
		}
 		mRoute = pNewRoute;
		if(HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
			Logging.log(this, ">>> New route: " + mRoute);
		}
 	}
 	
 	/**
 	 * Returns a descriptive string about the object
 	 * 
 	 * @return the descriptive string
 	 */
 	public String toString()
 	{
 		if(mRoute == null){
 			return getLinkType().toString();
 		}else{
 			return getLinkType().toString() +": " + mRoute.toString();				
 		}
 	}
 }
