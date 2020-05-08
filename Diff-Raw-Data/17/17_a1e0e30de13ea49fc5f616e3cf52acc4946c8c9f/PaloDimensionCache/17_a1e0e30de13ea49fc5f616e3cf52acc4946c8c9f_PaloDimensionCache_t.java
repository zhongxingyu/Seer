 /*
  *   This file is part of PaloKettlePlugin.
  *
  *   PaloKettlePlugin is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU Lesser General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   PaloKettlePlugin is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with PaloKettlePlugin.  If not, see <http://www.gnu.org/licenses/>.
  *   
  *   Copyright 2011 De Bortoli Wines Pty Limited (Australia)
  */
 
 /**
  * This Class manages dimension cache to minimize palo calls to the server. 
  * 
  * @author Pieter van der Merwe
  * @since 05-08-2011
  */
 package org.pentaho.di.palo.core;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Hashtable;
 
 import com.jedox.palojlib.interfaces.IDatabase;
 import com.jedox.palojlib.interfaces.IDimension;
 import com.jedox.palojlib.interfaces.IElement;
 import com.jedox.palojlib.interfaces.IElement.ElementType;
 
 public class PaloDimensionCache {
 	private Hashtable<String, IElement> elementCache = new Hashtable<String, IElement>();
 	private String dimensionName = "";
 	private IDimension dimension;
 	private boolean enableCache = false;
 	private boolean allLoaded = false;
 	private ArrayList<String> elementsAdded = new ArrayList<String>();
 	
 	public PaloDimensionCache(IDatabase paloDatabase, IDimension dimension, boolean enableCache){
 		this.dimension = dimension;
 		this.dimensionName = this.dimension.getName();
 		this.enableCache = enableCache;
 	}
 	
 	public PaloDimensionCache(IDatabase paloDatabase, String dimensionName, boolean enableCache) throws Exception{
 		this.dimensionName = dimensionName;
 		this.enableCache = enableCache;
 		
 		this.dimension = paloDatabase.getDimensionByName(dimensionName);
 		
 		if(this.dimension == null)
             throw new Exception("The dimension "+dimensionName+" does not exist.");
 	}
 	
 	public void loadDimensionCache() throws Exception {
 		if (!enableCache)
 			throw new Exception("Cache is not enabled for dimension " + dimensionName);
 
 		IElement [] elements = dimension.getElements(false);
 
 		for (IElement elem : elements){
 			elementCache.put(elem.getName(), elem);
 		}
 		
 		allLoaded = true;
 		elementsAdded.clear();
 	}
 	
 	public IElement getElement(final String elementName){
 		IElement element = null;
 		
 		if (enableCache)
 			element = elementCache.get(elementName);
 		
 		if (element == null && allLoaded == false)
 		{
 			element = dimension.getElementByName(elementName, false);
 			if (enableCache && element != null)
 				elementCache.put(elementName,element);
 		}
 		
 		return element;
 	}
 	
 	public IElement createElement(String elementName, IElement.ElementType elementType, boolean errorIfExists) throws Exception{
 		IElement elem = getElement(elementName);
 		if (errorIfExists == true && elem != null)
 			throw new Exception("Element with name " + elementName + " already exists");
 		
 		/* New item */
 		if (elem == null){
 			elem = dimension.addBaseElement(elementName, elementType);
 			if (enableCache)
 				elementCache.put(elementName,elem);
 		}
 		return elem;
 	}
 	
 	public void createElements(ArrayList<String> elementNames, IElement.ElementType elementType, boolean errorIfExists) throws Exception{
 		// Get a unique list
 		elementNames = new ArrayList<String>(new HashSet<String>(elementNames));
 		
		ArrayList<String> uniqueElementNames = new ArrayList<String>();
		
 		// Only non existing elements can be added
 		for (String elementName : elementNames){
 			if (elementsAdded.contains(elementName)
 				|| this.getElement(elementName) != null){
 				if (errorIfExists){
 					throw new Exception("Element with name " + elementName + " already exists");
 				}
 			}
			else uniqueElementNames.add(elementName);
 		}
 		
 		// Setup the types for the elements
		ElementType[] elementTypes = new ElementType[uniqueElementNames.size()];
		for (int i = 0; i < uniqueElementNames.size(); i++){
 			elementTypes[i] = elementType;
 		}
 
		dimension.addElements(uniqueElementNames.toArray(new String[0]), elementTypes);
 		
		for (String elementName : uniqueElementNames){
 			elementsAdded.add(elementName);
 		}
 	}
 	
 	public String getDimensionName(){
 		return dimensionName;
 	}
 	
 	public IDimension getDimension(){
 		return dimension;
 	}
 }
