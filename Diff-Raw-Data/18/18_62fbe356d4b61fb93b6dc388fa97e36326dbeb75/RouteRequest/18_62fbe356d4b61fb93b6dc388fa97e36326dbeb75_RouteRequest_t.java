 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.packets.hierarchical;
 
 import java.io.Serializable;
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingServiceLinkVector;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.ClusterDummy;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMName;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * 
  * Use this object to request routes from coordinators - those will ask their local HierarchicalRoutingService instance
  */
 public class RouteRequest implements Serializable
 {
 	public enum ResultType {SUCCESS, UNFEASIBLE, ALTERNATIVE };
 	
 	private static final long serialVersionUID = 8346063693634901158L;
 	private Description mDescription;
 	private HRMName mSource;
 	private HRMName mTarget;
 	private boolean mAnswer = false;
 	private long mSession;
 	private LinkedList<RoutingServiceLinkVector> mRouteToTarget = null;
 	private boolean mInterASRequest;
 	private boolean mRouteAccumulation = false;
 	private Route mRoute = null;
 	private ResultType mResult = null;
 	private LinkedList<ClusterDummy> mRequiredClusters;
 	
 	/**
 	 * 
 	 * @param pSource is the node that wishes to know a route to the target
 	 * @param pTarget is the target to which a route should be found
 	 * @param pDescription contains requirements that are supposed to be provided by the route answer
 	 * @param pSession identifies the session to which a route request is associated to
 	 */
 	public RouteRequest(HRMName pSource, HRMName pTarget, Description pDescription, long pSession)
 	{
 		mDescription = pDescription;
 		mTarget = pTarget;
 		mSource = pSource;
 		mSession = pSession;
 	}
 	
 	/**
 	 * 
 	 * @param pSource manipulate the source of this route request
 	 */
 	public void setSource(HRMID pSource)
 	{
 		mSource = pSource;
 	}
 	
 	/**
 	 * 
 	 * @return source that wishes to know the route to a target
 	 */
 	public HRMName getSource()
 	{
 		return mSource;
 	}
 	
 	/**
 	 * 
 	 * @param pRoute is the route to the target
 	 */
 	public void setRoute(Route pRoute)
 	{
 		mRoute = pRoute;
 	}
 	
 	/**
 	 * 
 	 * @return route to the target
 	 */
 	public Route getRoute()
 	{
 		return mRoute;
 	}
 	
 	/**
 	 * Use this method to allow the accumulation of a route
 	 */
 	public void setRouteAccumulation()
 	{
 		mRouteAccumulation = true;
 	}
 
 	/**
 	 * 
 	 * @param pDummy is a cluster that is required to lie in the route to the target
 	 */
 	public void addRequiredCluster(ClusterDummy pDummy)
 	{
 		if(mRequiredClusters == null) {
 			mRequiredClusters = new LinkedList<ClusterDummy>();
 		}
 		if(!mRequiredClusters.contains(pDummy)) {
 			mRequiredClusters.add(pDummy);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param pRequiredClusters set clusters that are required to lie along the route to the target
 	 */
 	public void setRequiredClusters(LinkedList<ClusterDummy> pRequiredClusters)
 	{
 		mRequiredClusters = (LinkedList<ClusterDummy>) pRequiredClusters.clone();
 	}
 	
 	/**
 	 * 
 	 * @return get to know clusters that are required to lie along the route 
 	 */
 	public LinkedList<ClusterDummy> getRequiredClusters()
 	{
 		return mRequiredClusters;
 	}
 	
 	/**
 	 * 
 	 * @return true if route accumulation is used
 	 */
 	public boolean isRouteAccumulation()
 	{
 		return mRouteAccumulation;
 	}
 	
 	/**
 	 * 
 	 * activate the flag that this object carries an answer
 	 */
 	public void setAnswer()
 	{
 		mAnswer = true;
 	}
 	
 	/**
 	 * 
 	 * @return true if this packet is an answer, false otherwise
 	 */
 	public boolean isAnswer()
 	{
 		return mAnswer;
 	}
 	
 	/**
 	 * 
 	 * @return route request session in order to find out to which session this request belongs
 	 */
 	public long getSession()
 	{
 		return mSession;
 	}
 	
 	/**
 	 * 
 	 * @return routing vectors necessary to reach the target
 	 */
 	public LinkedList<RoutingServiceLinkVector> getRoutingVectors()
 	{
 		return mRouteToTarget;
 	}
 	
 	/**
 	 * 
 	 * @return target to which a route should be provided
 	 */
 	public HRMName getTarget()
 	{
 		return mTarget;
 	}
 	
 	/**
 	 * 
 	 * @return requirements for the route
 	 */
 	public Description getDescription()
 	{
 		return mDescription;
 	}
 	
 //	/**
 //	 * state the fact that the result would traverse multiple autonomous systems
 //	 */
 //	public void setInterASResult()
 //	{
 //		mInterASRequest = true;
 //	}
 	
 	/**
 	 * 
 	 * @return true if the result would cause the packet to traverse mutliple autonomous systems
 	 */
 	public boolean isInterASResult()
 	{
 		return mInterASRequest;
 	}
 	
 	/**
 	 * 
 	 * @param pLink to add a routing vector that is necessary to reach the target
 	 */
 	public void addRoutingVector(RoutingServiceLinkVector pLink)
 	{
 		if(mRouteToTarget == null) {
 			mRouteToTarget = new LinkedList<RoutingServiceLinkVector>();
 		}
 		if(!mRouteToTarget.contains(pLink)) {
 			Logging.log(this, "Adding " + pLink + " to\n" + mRouteToTarget);
 			mRouteToTarget.add(pLink);
 		}
 	}
 
 	public String toString()
 	{
 		return getClass().getSimpleName() + "->" + mTarget + (mResult != null ? "(Result:" + mResult  + ")" : "") + "\n" + (mRoute != null ? mRoute.toString() + "\n" : "") + (mRouteToTarget != null && !mRouteToTarget.isEmpty() ? mRouteToTarget.toString(): "");
 	}
 
 	public RouteRequest clone()
 	{
 		RouteRequest tRequest = new RouteRequest(mSource, mTarget, mDescription, mSession);
 		tRequest.mAnswer = mAnswer;
 		tRequest.mRoute = mRoute;
 		tRequest.mRouteToTarget = (LinkedList<RoutingServiceLinkVector>) mRouteToTarget.clone();
 		tRequest.mInterASRequest = mInterASRequest;
 		tRequest.mDescription = mDescription.clone();
 		tRequest.mRouteAccumulation = mRouteAccumulation;
 		tRequest.mResult = mResult;
 		return tRequest;
 	}
 	
 	/**
 	 * 
 	 * @return find out whether the result was one of {SUCCESS, UNFEASIBLE, ALTERNATIVE }
 	 */
 	public ResultType getResult()
 	{
 		return mResult;
 	}
 
 	/**
 	 * 
 	 * @param pResult to set the result of the route request
 	 */
 	public void setResult(ResultType pResult)
 	{
 		mResult = pResult;
 	}
 }
