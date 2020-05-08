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
 package de.tuilmenau.ics.fog.ui;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 
 
 public class MarkerContainer
 {
 	private MarkerContainer()
 	{
 		
 	}
 	
 	public static final MarkerContainer getInstance()
 	{
 		if(sElements == null) {
 			sElements = new MarkerContainer();
 		}
 		
 		return sElements;
 	}
 	
 	public void addObserver(IMarkerContainerObserver pObs)
 	{
 		if(pObs != null) {
 			// lazy creation
 			if(observers == null) observers = new LinkedList<IMarkerContainerObserver>();
 			
 			observers.add(pObs);
 		}
 	}
 	
 	private void notifyObservers(Object pChangedObject)
 	{
 		if(observers != null) {
 			for(IMarkerContainerObserver obs : observers) {
 				try {
 					obs.notify(this, pChangedObject);
 				}
 				catch(Exception exc) {
 					// if an observer throws an error, report and ignore it 
 					Logging.getInstance().err(this, "Error in observer " +obs +" for changes object " +pChangedObject, exc);
 				}
 			}
 		}
 	}
 	
 	public void deleteObserver(IMarkerContainerObserver obs)
 	{
 		if(observers != null) observers.remove(obs);
 	}
 	
 
 	
	public void add(Object pObject, Marker pMarker)
 	{
 		if(mMarkedElements == null) {
 			mMarkedElements = new HashMap<Object, LinkedList<Marker>>();
 		}
 		
 		LinkedList<Marker> tMarkers = mMarkedElements.get(pObject);
 		if(tMarkers == null) {
 			tMarkers = new LinkedList<Marker>();
 			mMarkedElements.put(pObject, tMarkers);
 		}
 		
 		tMarkers.add(pMarker);
 		
 		notifyObservers(pObject);
 	}
 	
	public boolean remove(Object pObject, Marker pMarker)
 	{
 		if(mMarkedElements != null) {
 			LinkedList<Marker> tMarkers = mMarkedElements.get(pObject);
 			
 			if(tMarkers != null) {
 				if(tMarkers.remove(pMarker)) {
 					// no markers for the object any more?
 					if(tMarkers.isEmpty()) {
 						tMarkers.remove(pObject);
 					}
 					
 					notifyObservers(pObject);
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * @param pObject Reference object
 	 * @return All markers listed for the reference object
 	 */
	public Marker[] get(Object pObject)
 	{
 		if(mMarkedElements != null) {
 			LinkedList<Marker> tMarkers = mMarkedElements.get(pObject);
 			
 			if(tMarkers != null) {
 				return tMarkers.toArray(sDummyResult);
 			}
 		}
 		
 		return sDummyResult;
 	}
 	
 	/**
 	 * @return List of all markers (!= null)
 	 */
	public Marker[] getMarkers()
 	{
 		if(mMarkedElements != null) {
 			LinkedList<Marker> tMarkers = new LinkedList<Marker>();
 			
 			// iterate all lists and search for markers
 			for(LinkedList<Marker> tMarkerList : mMarkedElements.values()) {
 				for(Marker tMarker : tMarkerList) {
 					if(!tMarkers.contains(tMarker)) {
 						tMarkers.add(tMarker);
 					}
 				}
 			}
 			
 			return tMarkers.toArray(sDummyResult);
 		} else {
 			return sDummyResult;
 		}
 	}
 	
 	/**
 	 * Removes all appearances of a marker (uses equals comparison).
 	 * 
 	 * @param pMarker Marker to remove
 	 */
	public void removeMarker(Marker pMarker)
 	{
 		if(pMarker != null) {
 			LinkedList<Marker> tDelMarkerList = new LinkedList<Marker>();
 			
 			// iterate all lists and search for appearance of marker
 			for(LinkedList<Marker> tMarkerList : mMarkedElements.values()) {
 				// 1. search for markers to delete
 				for(Marker tMarker : tMarkerList) {
 					if(pMarker.equals(tMarker)) {
 						tDelMarkerList.add(tMarker);
 					}
 				}
 				
 				// 2. delete them
 				if(!tDelMarkerList.isEmpty()) {
 					for(Marker tDelMarker : tDelMarkerList) {
 						tMarkerList.remove(tDelMarker);
 					}
 					
 					tDelMarkerList.clear();
 				}
 			}
 			
 			notifyObservers(null);
 		}
 	}
 	
 	// Lazy evaluated observer list
 	private LinkedList<IMarkerContainerObserver> observers = null;
 	
 	// dummy result
 	private static Marker[] sDummyResult = new Marker[0];
 	
 	// Singleton instance for all graphs residing in this process.
 	private static MarkerContainer sElements = null;
 	
 	private HashMap<Object, LinkedList<Marker>> mMarkedElements = null;
 }
