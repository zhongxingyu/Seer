 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator
  * Copyright (C) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * This program and the accompanying materials are dual-licensed under either
  * the terms of the Eclipse Public License v1.0 as published by the Eclipse
  * Foundation
  *  
  *   or (per the licensee's choosing)
  *  
  * under the terms of the GNU General Public License version 2 as published
  * by the Free Software Foundation.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing;
 
 import java.io.Serializable;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.facade.Description;
import de.tuilmenau.ics.fog.facade.Name;
import de.tuilmenau.ics.fog.routing.simulated.RoutingServiceAddress;
 import de.tuilmenau.ics.fog.transfer.gates.GateID;
 import de.tuilmenau.ics.fog.util.SimpleName;
 
 
 /**
  * A route consists of segments, which can be of different types.
  * The first segment of route is the 'active' one. It will be used by
  * the forwarding nodes to forward the packet to the next gate.
  */
 public class Route extends LinkedList<RouteSegment> implements Serializable
 {
 	private static final long serialVersionUID = -5723249136563400383L;
 	
 
 	public Route()
 	{
 		super();
 	}
 	
 	public Route(RouteSegment pRouteSegment)
 	{
 		super();
 		
 		add(pRouteSegment);
 		++mRouteLength;
 	}
 	
 	public Route(GateID pShortRoute)
 	{
 		super();
 		
 		add(new RouteSegmentPath(pShortRoute));
 		++mRouteLength;
 	}
 
 	public Route(Route pRoute)
 	{
 		super();
 		
 		if(pRoute != null) {
 			for(int i=0; i<pRoute.size(); i++) {
 				RouteSegment seg = pRoute.get(i);
 				
 				if(seg != null)
 				{
 					addLast(seg.clone());
 					++mRouteLength;
 				}
 			}
 		}
 	}
 
 	public void add(Route pRoute)
 	{
 		for(RouteSegment tRouteSegment: pRoute){
 			add(tRouteSegment);
 		}
 	}
 	
 	public void addFirst(GateID gateID)
 	{
 		if(size() > 0) {
 			++mRouteLength;
 			if(get(0) instanceof RouteSegmentPath) {
 				// already a segment with gate ID at the beginning 
 				RouteSegmentPath pathSeg = (RouteSegmentPath) get(0);
 				pathSeg.addFirst(gateID);
 			} else {
 				// other segment type at the beginning
 				addFirst(new RouteSegmentPath(gateID));
 			}
 		} else {
 			// no segment at all in
 			addFirst(new RouteSegmentPath(gateID));
 			++mRouteLength;
 		}
 	}
 
 	public void addLast(GateID gateID)
 	{
 		if(size() > 0) {
 			++mRouteLength;
 			RouteSegment segment = getLast();
 			if(segment instanceof RouteSegmentPath) {
 				// already a segment with gate ID at the beginning 
 				RouteSegmentPath pathSeg = (RouteSegmentPath) segment;
 				pathSeg.addLast(gateID);
 			} else {
 				// other segment type at the beginning
 				addLast(new RouteSegmentPath(gateID));
 			}
 		} else {
 			// no segment at all in
 			addLast(new RouteSegmentPath(gateID));
 			++mRouteLength;
 		}
 	}
 	
 	public void addFirst(Route route)
 	{
 		for(int i=route.size() -1; i>=0; i--) {
 			addFirst(route.get(i));
 			++mRouteLength;
 		}
 	}
 	
 	public void addLast(Route route)
 	{
 		for(int i=0; i<route.size(); i++) {
 			addLast(route.get(i));
 			++mRouteLength;
 		}
 	}
 	
 	/**
 	 * Determines the first gate number from the route. That is the number,
 	 * which will be processed next by a forwarding node on the route. 
 	 * 
 	 * @param removeIt true=first gate number will be removed from route; false=gate number will remain in route and just returned
 	 * @return Gate number or null, if no gate number is available (route empty, wrong route segment)
 	 */
 	public GateID getFirst(boolean removeIt)
 	{
 		// check to avoid exception in getFirst
 		if(size() > 0) {
 			RouteSegment segment = getFirst();
 			
 			if(segment instanceof RouteSegmentPath) {
 				RouteSegmentPath path = (RouteSegmentPath) segment;
 				
 				if(path.size() > 0) {
 					if(removeIt) {
 						return path.removeFirst();
 					} else {
 						return path.getFirst();
 					}
 				} else {
 					// first segment is an empty path segment
 					// -> remove it and try next segment
 					removeFirst();
 					return getFirst(removeIt);
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Determines the first description (route segment) from the route. 
 	 * 
 	 * @param removeIt true=first description will be removed from route; false=description will remain in route and just returned
 	 * @return Description or null, if no description is the head of the route or no segment is available (route empty, wrong route segment)
 	 */
 	public RouteSegmentDescription getFirstDescription(boolean removeIt)
 	{
 		// check to avoid exception in getFirstRouteSegment
 		if(size() > 0) {
 			RouteSegment tSegment = getFirst();
 			if (tSegment instanceof RouteSegmentDescription){
 				
 				if (removeIt) 
 					return (RouteSegmentDescription) removeFirst();
 				else
 					return (RouteSegmentDescription) getFirst();
 			}
 		}
 		
 		return null;
 	}
 	
 	public boolean isExplicit()
 	{
 		for(RouteSegment tSeg : this) {
 			if(!(tSeg instanceof RouteSegmentPath)) {
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 	public boolean isShorter(Route pOtherRoute)
 	{
 		// simple comparison between the sizes of the stored linked lists
 		return (size() < pOtherRoute.size());
 		
 		//TODO: introduce a scoring which assigns each entry a score depending on the route effort (gate numbers -> 1, full addresses -> 1000?)
 	}
 	
 	public boolean equals(Route pRoute)
 	{
		if(pRoute == null){
			return false;
		}
		
 		Iterator<RouteSegment> thisRouteIterator = this.iterator();
 		Iterator<RouteSegment> tRouteIterator = pRoute.iterator();
 		if(this.size() == pRoute.size()) {
 			// this.iterator() and pRoute.iterator() have the same amount of elements because of Comparison above!
 			// --> doesn't matter which Iterator we test of havin next element!
 			while(thisRouteIterator.hasNext()) {
 				//pick the next RouteSegment from both Routes
 				RouteSegment thisRouteSegment = thisRouteIterator.next();
 				RouteSegment tRouteSegment = tRouteIterator.next();
 				if(thisRouteSegment instanceof RouteSegmentPath && tRouteSegment instanceof RouteSegmentPath) {
 					RouteSegmentPath thisRouteSegmentPath = (RouteSegmentPath) thisRouteSegment;
 					RouteSegmentPath tRouteSegmentPath = (RouteSegmentPath) tRouteSegment;
 					
 					if(!thisRouteSegmentPath.equals(tRouteSegmentPath)) {
 						return false;
 					}
 				} else if(thisRouteSegment instanceof RouteSegmentAddress && tRouteSegment instanceof RouteSegmentAddress) { 
					Name thisRouteSegmentGenAddress = (RoutingServiceAddress) ((RouteSegmentAddress)thisRouteSegment).getAddress();
					if(thisRouteSegmentGenAddress instanceof SimpleName){
						SimpleName thisRouteSegmentAddresss = (SimpleName) ((RouteSegmentAddress)thisRouteSegment).getAddress();
						SimpleName tRouteSegmentAddress = (SimpleName) ((RouteSegmentAddress)tRouteSegment).getAddress();
						
						if(!thisRouteSegmentAddresss.equals(tRouteSegmentAddress)) {
							return false;
						}
					}else if (thisRouteSegmentGenAddress instanceof RoutingServiceAddress){
						RoutingServiceAddress thisRouteSegmentAddresss = (RoutingServiceAddress) ((RouteSegmentAddress)thisRouteSegment).getAddress();
						RoutingServiceAddress tRouteSegmentAddress = (RoutingServiceAddress) ((RouteSegmentAddress)tRouteSegment).getAddress();
 					
						if(!thisRouteSegmentAddresss.equals(tRouteSegmentAddress)) {
							return false;
						}
 					}
 				} else if(thisRouteSegment instanceof RouteSegmentDescription && tRouteSegment instanceof RouteSegmentDescription) {
 					Description thisRouteSegmentDescription = ((RouteSegmentDescription)thisRouteSegment).getDescription();
 					Description tRouteSegmentDescription = ((RouteSegmentDescription)tRouteSegment).getDescription();
 					
 					if(!thisRouteSegmentDescription.equals(tRouteSegmentDescription)) {
 						return false;
 					}
 				}
 			}
 			return true;
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public Route clone()
 	{
 		Route tNewRoute=new Route();
 		for(int i=this.size();i>0;i--){
 			tNewRoute.addFirst(this.get(i-1).clone());
 		}
 		return tNewRoute;
 	}
 
 	public String toString()
 	{
 		StringBuffer resultStr = new StringBuffer(128);
 
 		resultStr.append("[");
 		if(size() > 0) {
 			for(int i = 0; i < size(); i++) {
 				resultStr.append(get(i));
 				if (i < size() - 1) {
 					resultStr.append(",");
 				}
 			}
 		}
 		resultStr.append("]");
 
 		return resultStr.toString();
 	}
 	
 	/**
 	 * @deprecated Is this method used?
 	 * TODO And, is it really implemented correctly?
 	 * @return ?
 	 */
 	public int getGateAmount()
 	{
 		return mRouteLength;
 	}
 	
 	/**
 	 * @return Number of explicit gate numbers in route
 	 */
 	public int sizeNumberGates()
 	{
 		int tGateNumber = 0;
 
 		for(RouteSegment tSegment : this) {
 			if(tSegment instanceof RouteSegmentPath) {
 				tGateNumber += ((RouteSegmentPath) tSegment).size();
 			}
 		}
 		
 		return tGateNumber;
 	}
 	
 	public int getSerialisedSize()
 	{
 		int tSize = 0;
 		
 		for(RouteSegment tSegment : this) {
 			tSize += tSegment.getSerialisedSize();
 		}
 		
 		return tSize;
 	}
 	
 	private int mRouteLength = 0;
 
 
 }
