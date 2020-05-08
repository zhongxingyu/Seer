 
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
 import java.awt.Color;
 
 /**
  * Based on the graph/node/edge view information, build new Visual Style.
  *
  * This class accepts style properties and adds hidden Cytoscape attributes that
  * will be used to actually create the style.
  */
 public class VisualStyleBuilder {
 
 	Map<VisualPropertyType,Map<Object,Object>> valueMaps;
 	Map<VisualPropertyType,Map<String,Object>> idMaps;
 	String name;
 	boolean addOverride = false;
 
 	/**
 	 * Build a new VisualStyleBuilder object whose output style will be called "name".
 	 * 
 	 * @param name the name of the visual style that will be created.
 	 */
 	public VisualStyleBuilder(String name) {
 		this.name = name;
 		valueMaps = new EnumMap<VisualPropertyType,Map<Object,Object>>(VisualPropertyType.class);
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
 		this.name = newName;
 	}
 
 	/**
 	 * Build a new VisualStyleBuilder object whose output style will be called "name".
 	 * 
 	 * @param name the name of the visual style that will be created.
 	 * @param addOvAttr adds override attributes for each style set
 	 */
 	public VisualStyleBuilder(String name, boolean addOvAttr) {
 		this.name = name;
 		valueMaps = new EnumMap<VisualPropertyType,Map<Object,Object>>(VisualPropertyType.class);
 		this.addOverride = addOvAttr;
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
 
 		for ( VisualPropertyType type : valueMaps.keySet() ) {
 			DiscreteMapping dm = new DiscreteMapping( type.getVisualProperty().getDefaultAppearanceObject(), 
 			                                          getAttrName(type), 
 			                                          type.isNodeProp() ? 
 													   ObjectMapping.NODE_MAPPING : 
 													   ObjectMapping.EDGE_MAPPING );
 
 			dm.putAll( valueMaps.get(type) );
 
 			Calculator calc = new BasicCalculator("homer " + getAttrName(type), dm, type);
 
 			if ( type.isNodeProp() )
 				nac.setCalculator( calc );
 			else
 				eac.setCalculator( calc );
 		}
 
 		VisualMappingManager vizmapper = Cytoscape.getVisualMappingManager();
 		CalculatorCatalog catalog = vizmapper.getCalculatorCatalog();
 
		VisualStyle graphStyle = new VisualStyle(name + "style", nac, eac, gac);
 
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
 
 		attrs.setAttribute(id, getAttrName(type), value.hashCode());
 		attrs.setUserVisible(getAttrName(type), false);
 		if (addOverride) {
 			String strValue = value.toString();
 			if (type.getDataType() == Color.class) {
 				strValue = ((Color)value).getRed()+","+((Color)value).getGreen()+","+((Color)value).getBlue();
 			}
 			attrs.setAttribute(id, type.getBypassAttrName(), strValue);
 		}
 
 		if ( !valueMaps.containsKey(type) )
 			valueMaps.put( type, new HashMap<Object,Object>() );
 		valueMaps.get(type).put( new Integer(value.hashCode()), value );
 	
 	}
 }
