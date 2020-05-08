 
 /*
  Copyright (c) 2007, The Cytoscape Consortium (www.cytoscape.org)
 
  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies
 
  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.
 
  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
 
 package cytoscape.data.readers;
 
 import cytoscape.Cytoscape;
 import cytoscape.data.CyAttributes;
 import cytoscape.visual.*;
 import cytoscape.visual.calculators.*;
 import cytoscape.visual.mappings.*;
 
 import java.util.*;
 import java.lang.Math;
 import java.awt.Color;
 
 /**
  * Based on the graph/node/edge view information, build new Visual Style.
  *
  * This class accepts style properties and adds hidden Cytoscape attributes that
  * will be used to actually create the style.
  */
 public class VisualStyleBuilder {
 
 	Map<VisualPropertyType,Map<Object,Object>> valueMaps;
 	Map<VisualPropertyType,Map<Object,Integer>> counts;
 	String name;
 	private boolean nodeSizeLocked = true;
 
 	private int nodeMax;
 	private int edgeMax;
 
 	/**
 	 * Build a new VisualStyleBuilder object whose output style will be called "name".
 	 * 
 	 * @param name the name of the visual style that will be created.
 	 */
 	public VisualStyleBuilder(String name) {
		this.name = name;
 		valueMaps = new EnumMap<VisualPropertyType,Map<Object,Object>>(VisualPropertyType.class);
 		counts = new EnumMap<VisualPropertyType,Map<Object,Integer>>(VisualPropertyType.class);
 	}
 
 	/**
 	 * Build a new VisualStyleBuilder object whose output style will be called "name" based
 	 * on JAXB Graphics objects.  This constructor is no longer used and is not supported.
 	 * 
 	 * @param newName the name of the visual style that will be created.
 	 * @param nodeGraphics the map of node to JAXB Graphics object
 	 * @param edgeGraphics the map of edge to JAXB Graphics object
 	 * @param globalGraphics the map of network to JAXB Graphics object
 	 * @deprecated this should no longer be used and is not functional.  Use VisualStyleBuilder(String)
 	 * instead and then call addProperty for each value
 	 */
 	public VisualStyleBuilder(String newName, Map nodeGraphics, Map edgeGraphics, Map globalGraphics) {
 		this(newName);
 	}
 
 	/**
 	 * Build a new VisualStyleBuilder object whose output style will be called "name".
 	 * 
 	 * @param name the name of the visual style that will be created.
 	 * @param addOvAttr not used. 
 	 */
 	public VisualStyleBuilder(String name, boolean addOvAttr) {
 		this(name);
 	}
 
 	/**
 	 * Actually build the style using the provided properties
 	 */
 	public void buildStyle() {
 		// First, get our current style information. 
 		VisualMappingManager vm = Cytoscape.getVisualMappingManager();
 		VisualStyle currentStyle = vm.getVisualStyle();
 		NodeAppearanceCalculator nac = new NodeAppearanceCalculator(currentStyle.getNodeAppearanceCalculator());
 		EdgeAppearanceCalculator eac = new EdgeAppearanceCalculator(currentStyle.getEdgeAppearanceCalculator());
 		GlobalAppearanceCalculator gac = new GlobalAppearanceCalculator(currentStyle.getGlobalAppearanceCalculator());
 
 		nac.setNodeSizeLocked(nodeSizeLocked);
 
 		processCounts();
 
 		for ( VisualPropertyType type : valueMaps.keySet() ) {
 		
 			Map<Object,Object> valMap = valueMaps.get(type);
 
 			// If there is more than one value specified for a given
 			// visual property, or if only a subset of nodes/edges
 			// have a property then create a mapping and calculator.
 			if ( createMapping(type) ) {
 
 				DiscreteMapping dm = new DiscreteMapping( type.getVisualProperty().getDefaultAppearanceObject(), 
 			   	                                       getAttrName(type), 
 			   	                                       type.isNodeProp() ? 
 														   ObjectMapping.NODE_MAPPING : 
 														   ObjectMapping.EDGE_MAPPING );
 
 				System.out.println( "ValueMaps size: " + valueMaps.get(type).size() );
 				dm.putAll( valMap );
 
				Calculator calc = new BasicCalculator("homer " + getAttrName(type), dm, type);
 	
 				if ( type.isNodeProp() )
 					nac.setCalculator( calc );
 				else
 					eac.setCalculator( calc );
 
 			// Otherwise, set the default appearance value for the visual style
 			// and then remove the attribute that was created.
 			} else {
 				if ( type.isNodeProp() ) {
 					for ( Object key : valMap.keySet() ) 
 						nac.getDefaultAppearance().set( type, valMap.get(key) );
 					Cytoscape.getNodeAttributes().deleteAttribute(getAttrName(type));
 				} else {
 					for ( Object key : valMap.keySet() ) 
 						eac.getDefaultAppearance().set( type, valMap.get(key) );
 					Cytoscape.getEdgeAttributes().deleteAttribute(getAttrName(type));
 				}
 			}
 		}
 
 		VisualMappingManager vizmapper = Cytoscape.getVisualMappingManager();
 		CalculatorCatalog catalog = vizmapper.getCalculatorCatalog();
 
		String styleName = name+" style";
 		VisualStyle graphStyle = new VisualStyle(styleName, nac, eac, gac);
 
 		// Remove this in case we've already loaded this network once
 		catalog.removeVisualStyle(styleName);
 
 		// Now, attempt to add it
 		catalog.addVisualStyle(graphStyle);
 		vizmapper.setVisualStyle(graphStyle);
 	}
 
 	private String getAttrName(VisualPropertyType type) {
 		return "vizmap:"+name + " " + type.toString();
 	}
 
 	/**
 	 * This method actually adds a property to be considered for inclusion into
 	 * the resulting style.
 	 *
 	 * @param id the id of the node or edge
 	 * @param type the type of the property
 	 * @param desc the property value
 	 */
 	public void addProperty(String id, VisualPropertyType type, String desc) {
 		CyAttributes attrs;
 		Object value = type.getValueParser().parseStringValue(desc);
 		if (value == null)
 			return;
 		if ( type.isNodeProp() )
 			attrs = Cytoscape.getNodeAttributes();
 		else
 			attrs = Cytoscape.getEdgeAttributes();
 
 		attrs.setAttribute(id, getAttrName(type), value.toString());
 
 		String vString = value.toString();
 
 		// store the value
 		if ( !valueMaps.containsKey(type) )
 			valueMaps.put( type, new HashMap<Object,Object>() );
 		valueMaps.get(type).put( vString, value );
 
 		// store the count
 		if ( !counts.containsKey(type) ) 
 			counts.put( type, new HashMap<Object,Integer>() );
 
 		if ( !counts.get(type).containsKey(vString) )
 			counts.get(type).put( vString, 0 );
 
 		counts.get(type).put( vString, counts.get(type).get(vString) + 1 );
 	}
 	
 	/**
 	 * This method lock/unlock the size object (Node Width, Node Height) in NodeAppearanceCalculator
 	 * If unlocked, we can modify both width and height of node
 	 * 
 	 * @param pLock
 	 */
 	
 	public void setNodeSizeLocked(boolean pLock){
 		nodeSizeLocked = pLock;
 	}
 	
 	/**
 	 * Processes the counts for the various visual properties and establishes
 	 * how many nodes and edges there are.
 	 */
 	private void processCounts() {
 		Map<VisualPropertyType,Integer> cm = new EnumMap<VisualPropertyType,Integer>(VisualPropertyType.class);
 		for ( VisualPropertyType vpt : counts.keySet() ) {
 			int total = 0;
 			for ( Object o : counts.get(vpt).keySet() ) {
 				total += counts.get(vpt).get(o);
 			}
 			cm.put(vpt,total);
 			System.out.println(vpt + "  " + total);
 		}
 		
 		nodeMax = 0;
 		edgeMax = 0;
 		for ( VisualPropertyType vpt : counts.keySet() ) {
 			if ( counts.get(vpt).size() == 1 ) {
 				for ( Object o : counts.get(vpt).keySet() ) {
 					if ( vpt.isNodeProp() ) 
 							nodeMax = Math.max( counts.get(vpt).get(o), nodeMax );
 					else 
 							edgeMax = Math.max( counts.get(vpt).get(o), edgeMax );
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method determines whether or not to create a mapping for
 	 * this visual property type.  There are two times when you want
 	 * to create a mapping: 1) when there is more than one key mapped
 	 * to a value for type and 2) when only one key is mapped to a value,
 	 * but only a subset of nodes or edges have that mapping (which is
 	 * to say the property doesn't hold for all nodes or all edges).
 	 */
 	private boolean createMapping(VisualPropertyType vpt) {
 		// if there is more than one mapping
 		if ( counts.get(vpt).size() > 1 )
 			return true;
 
 		// check the number of times the value is mapped
 		// relative to the number of nodes or edges
 		for ( Object o : counts.get(vpt).keySet() ) {
 			int ct = counts.get(vpt).get(o).intValue();
 			if ( vpt.isNodeProp() ) {
 				if ( ct < nodeMax )
 					return true;
 				else 
 					return false;
 			} else {
 				if ( ct < edgeMax )
 					return true;
 				else 
 					return false;
 			}
 		}
 
 		return false;
 	}
 }
