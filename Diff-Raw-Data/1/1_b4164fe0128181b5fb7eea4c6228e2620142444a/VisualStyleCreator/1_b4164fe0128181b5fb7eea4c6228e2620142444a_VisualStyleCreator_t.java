 // CyTargetLinker,
 // a Cytoscape plugin to extend biological networks with regulatory interaction
 //
 // Copyright 2011-2013 Department of Bioinformatics - BiGCaT, Maastricht University
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //       http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 //
 package org.cytargetlinker.app.internal.gui;
 
 import java.awt.Color;
 
 import org.cytargetlinker.app.internal.ExtensionManager;
 import org.cytargetlinker.app.internal.Plugin;
 import org.cytargetlinker.app.internal.data.DataSource;
 import org.cytargetlinker.app.internal.data.NodeType;
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
 import org.cytoscape.view.presentation.property.BasicVisualLexicon;
 import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
 import org.cytoscape.view.presentation.property.values.ArrowShape;
 import org.cytoscape.view.presentation.property.values.NodeShape;
 import org.cytoscape.view.vizmap.VisualStyle;
 import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
 import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
 
 /**
  * 
  * @author martina kutmon
  * creates the cytargetlinker visual style
  *
  */
 public class VisualStyleCreator {
 
 	private Plugin plugin;
 	private VisualStyle vs;
 	
 	public VisualStyleCreator(Plugin plugin) {
 		this.plugin = plugin;
 		for(VisualStyle style : plugin.getVisualMappingManager().getAllVisualStyles()) {
 			if(style.getTitle().equals("CyTargetLinker")) {
 				vs = style;
 			}
 		}
 		if(vs == null) {
 			vs = plugin.getVisualStyleFactory().createVisualStyle("CyTargetLinker");
			plugin.getVisualMappingManager().addVisualStyle(vs);
 		}
 	}
 	
 	private CyNetwork network;
 	
 	public VisualStyle getVisualStyle(CyNetwork network) {
 		this.network = network;
 				
 		DiscreteMapping<String, NodeShape> shapeMapping = getNodeShapeStyle();
 		DiscreteMapping<String, Color> nodeColorMapping = getNodeColor();
 		
 		vs.addVisualMappingFunction(shapeMapping);
 		vs.addVisualMappingFunction(nodeColorMapping);
 		vs.addVisualMappingFunction(getArrowShape());
 		vs.addVisualMappingFunction(getEdgeColor());
 		vs.addVisualMappingFunction(getNodeLabelMapping());
 		return vs;
 	
 	}
 	
 	@SuppressWarnings("rawtypes")
 	private PassthroughMapping getNodeLabelMapping() {
 		PassthroughMapping mapping = (PassthroughMapping) plugin.getVisualMappingFunctionFactoryPassthrough().createVisualMappingFunction("name", String.class, BasicVisualLexicon.NODE_LABEL);
 		return mapping;
 	}
 	
 	// TODO: change mapping based on interaction and make sure interaction is not empty in the RINs!
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private DiscreteMapping<String, Color> getEdgeColor() {
 		Class<String> dataType = String.class;
 		DiscreteMapping<String, Color> edgeColorMapper = (DiscreteMapping) plugin.getVisualMappingFunctionFactoryDiscrete().createVisualMappingFunction("datasource", dataType, BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);		
 		ExtensionManager mgr = plugin.getExtensionManager(network);
 		for(DataSource ds : mgr.getDatasources()) {
 			edgeColorMapper.putMapValue(ds.getName(), ds.getColor());
 		}
 	        
 	    return edgeColorMapper;
 	}
 
 	// TODO: change mapping based on interaction and make sure interaction is not empty in the RINs!
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private DiscreteMapping<String, ArrowShape> getArrowShape() {
 		Class<String> dataType = String.class;
 		DiscreteMapping<String, ArrowShape> arrowShapeMapper = (DiscreteMapping) plugin.getVisualMappingFunctionFactoryDiscrete().createVisualMappingFunction("datasource", dataType, BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE);
 		
 		ExtensionManager mgr = plugin.getExtensionManager(network);
 		for(DataSource ds : mgr.getDatasources()) {
 			arrowShapeMapper.putMapValue(ds.getName(), ArrowShapeVisualProperty.ARROW);
 		}
 		
         arrowShapeMapper.putMapValue("pp", ArrowShapeVisualProperty.CIRCLE);
         arrowShapeMapper.putMapValue("interaction", ArrowShapeVisualProperty.ARROW);
         arrowShapeMapper.putMapValue("Line, Arrow", ArrowShapeVisualProperty.ARROW);
         arrowShapeMapper.putMapValue("Line, TBar", ArrowShapeVisualProperty.T);
         arrowShapeMapper.putMapValue("group-connection", ArrowShapeVisualProperty.NONE);
         
         return arrowShapeMapper;
 	}
 	
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private DiscreteMapping<String, Color> getNodeColor() {
 		String ctrAttr = "biologicalType";
 		Class<String> dataType = String.class; 
 		
 		DiscreteMapping<String, Color> dMapping = (DiscreteMapping) plugin.getVisualMappingFunctionFactoryDiscrete().createVisualMappingFunction(ctrAttr, dataType, BasicVisualLexicon.NODE_FILL_COLOR);
 		
 		String tf  = "transcriptionFactor";
 		dMapping.putMapValue(tf, new Color(204, 255, 204));
 		String gene  = "gene";
 		dMapping.putMapValue(gene, new Color(255,204,204));
 		String target  = "target";
 		dMapping.putMapValue(target, new Color(255,204,204));
 		String miRNA  = "microRNA";
 		dMapping.putMapValue(miRNA, new Color(255, 255, 204));
 		String drug  = "drug";
 		dMapping.putMapValue(drug, new Color(204, 204, 255));
 		
 		return dMapping;
 	}
 	
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private DiscreteMapping<String, NodeShape> getNodeShapeStyle() {
 		String ctrAttr = "ctl.nodeType";
 		Class<String> dataType = String.class; 
 		
 		DiscreteMapping<String, NodeShape> dMapping = (DiscreteMapping) plugin.getVisualMappingFunctionFactoryDiscrete().createVisualMappingFunction(ctrAttr, dataType, BasicVisualLexicon.NODE_SHAPE);
 
 		String reg  = NodeType.REGULATOR.toString();
 		dMapping.putMapValue(reg, NodeShapeVisualProperty.ROUND_RECTANGLE);
 		String tar  = NodeType.TARGET.toString();
 		dMapping.putMapValue(tar, NodeShapeVisualProperty.HEXAGON);
 		String both  = NodeType.BOTH.toString();
 		dMapping.putMapValue(both, NodeShapeVisualProperty.DIAMOND);
 		String init  = NodeType.INITIAL.toString();
 		dMapping.putMapValue(init, NodeShapeVisualProperty.ELLIPSE);
 		
 		return dMapping;
 	}
 }
