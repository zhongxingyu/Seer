 /*
  * Copyright 2002-2005 the original author or authors.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.jcf.graphicMessage;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.util.Assert;
 
 /**
  * Represents a graphical Object with a list of points
  * stored as instances of Location
  * @author FaKod
  *
  */
 abstract class GraphicObjectImpl implements Serializable, GraphicObject {
 
 	/**
 	 * unique id of graphical object
 	 */
 	private Id id;
 	
 	/**
 	 * list of locations (ordered)
 	 */
 	private List<Location> locations;
 	
 	/**
 	 * custom GraphicObjectProperty
 	 */
 	GraphicObjectProperty graphicObjectProperty;
 	
 	/**
 	 * ctor to use with unique id
 	 * @param id
 	 */
 	protected GraphicObjectImpl(Id id) {
 		Assert.notNull(id);
 		this.id = id;
 	}
 	
 	/**
 	 * dont use this
 	 */
 	@SuppressWarnings("unused")
 	private GraphicObjectImpl() {
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jcf.graphicMessage.GraphicObject#getId()
 	 */
 	public Id getId() {
 		return id;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jcf.graphicMessage.GraphicObject#getLocations()
 	 */
 	public List<Location> getLocations() {
 		return locations;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jcf.graphicMessage.GraphicObject#setLocations(java.util.List)
 	 */
 	public void setLocations(List<Location> locations) {
 		Assert.notNull(locations);
 		this.locations = locations;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.jcf.graphicMessage.GraphicObject#addLocation(org.jcf.graphicMessage.Location)
 	 */
 	public void addLocation(Location loc) {
 		Assert.notNull(loc);
 		if(locations==null)
 			locations = new ArrayList<Location>();
 		locations.add(loc);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.jcf.graphicMessage.GraphicObject#addAllLocation(java.util.List)
 	 */
 	public void addAllLocation(List<Location> locations) {
 		Assert.notNull(locations);
		if(locations==null)
 			setLocations(locations);
 		else
 			locations.addAll(locations);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jcf.graphicMessage.GraphicObject#getGraphicObjectProperty()
 	 */
 	public GraphicObjectProperty getGraphicObjectProperty() {
 		return graphicObjectProperty;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jcf.graphicMessage.GraphicObject#setGraphicObjectProperty(org.jcf.graphicMessage.GraphicObjectProperty)
 	 */
 	public void setGraphicObjectProperty(GraphicObjectProperty graphicObjectProperty) {
 		this.graphicObjectProperty = graphicObjectProperty;
 	}
 }
