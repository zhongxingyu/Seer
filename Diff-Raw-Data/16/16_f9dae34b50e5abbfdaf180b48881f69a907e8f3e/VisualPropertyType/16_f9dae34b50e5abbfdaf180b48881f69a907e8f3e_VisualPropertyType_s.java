 /*
  Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)
 
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
 package cytoscape.visual;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 import cytoscape.visual.calculators.Calculator;
 import cytoscape.visual.properties.EdgeColorProp;
 import cytoscape.visual.properties.EdgeFontFaceProp;
 import cytoscape.visual.properties.EdgeFontSizeProp;
 import cytoscape.visual.properties.EdgeLabelColorProp;
 import cytoscape.visual.properties.EdgeLabelOpacityProp;
 import cytoscape.visual.properties.EdgeLabelPositionProp;
 import cytoscape.visual.properties.EdgeLabelProp;
 import cytoscape.visual.properties.EdgeLineStyleProp;
 import cytoscape.visual.properties.EdgeLineTypeProp;
 import cytoscape.visual.properties.EdgeLineWidthProp;
 import cytoscape.visual.properties.EdgeOpacityProp;
 import cytoscape.visual.properties.EdgeSourceArrowColorProp;
 import cytoscape.visual.properties.EdgeSourceArrowOpacityProp;
 import cytoscape.visual.properties.EdgeSourceArrowProp;
 import cytoscape.visual.properties.EdgeSourceArrowShapeProp;
 import cytoscape.visual.properties.EdgeTargetArrowColorProp;
 import cytoscape.visual.properties.EdgeTargetArrowOpacityProp;
 import cytoscape.visual.properties.EdgeTargetArrowProp;
 import cytoscape.visual.properties.EdgeTargetArrowShapeProp;
 import cytoscape.visual.properties.EdgeToolTipProp;
 import cytoscape.visual.properties.NodeBorderColorProp;
 import cytoscape.visual.properties.NodeBorderOpacityProp;
 import cytoscape.visual.properties.NodeFillColorProp;
 import cytoscape.visual.properties.NodeFontFaceProp;
 import cytoscape.visual.properties.NodeFontSizeProp;
 import cytoscape.visual.properties.NodeHeightProp;
 import cytoscape.visual.properties.NodeLabelColorProp;
 import cytoscape.visual.properties.NodeLabelOpacityProp;
 import cytoscape.visual.properties.NodeLabelPositionProp;
 import cytoscape.visual.properties.NodeLabelProp;
 import cytoscape.visual.properties.NodeLineStyleProp;
 import cytoscape.visual.properties.NodeLineTypeProp;
 import cytoscape.visual.properties.NodeLineWidthProp;
 import cytoscape.visual.properties.NodeOpacityProp;
 import cytoscape.visual.properties.NodeShapeProp;
 import cytoscape.visual.properties.NodeSizeProp;
 import cytoscape.visual.properties.NodeToolTipProp;
 import cytoscape.visual.properties.NodeWidthProp;
 import cytoscape.visual.parsers.*;
 import cytoscape.visual.ui.EditorDisplayer;
 import cytoscape.visual.ui.EditorDisplayer.EditorType;
 import cytoscape.visual.ui.editors.continuous.ContinuousMappingEditorPanel;
 
 /**
  * Enum for calculator types.<br>
  *
  * This will replace public constants defined in VizMapperUI class.<br>
  * This Enum defines visual attributes used in Cytoscape.
  *
  * @version 0.5
  * @since Cytoscape 2.5
  * @author kono
  *
  */
 public enum VisualPropertyType {
 
 	NODE_FILL_COLOR("Node Color", "nodeFillColorCalculator", "node.fillColor",
 	                "defaultNodeFillColor", Color.class,
 	                new NodeFillColorProp(), new ColorParser(), true, true),
 	NODE_BORDER_COLOR("Node Border Color", "nodeBorderColorCalculator", "node.borderColor",
 	                  "defaultNodeBorderColor", 
 	                  Color.class, new NodeBorderColorProp(), new ColorParser(), true, true ), 
 	NODE_LINETYPE("Node Line Type", "nodeLineTypeCalculator", "node.lineType",
 	              "defaultNodeLineType", LineType.class,
 	              new NodeLineTypeProp(), new LineTypeParser(), true, false ), 
 	NODE_SHAPE("Node Shape", "nodeShapeCalculator", "node.shape", "defaultNodeShape",
 	           NodeShape.class, new NodeShapeProp(),
 			   new NodeShapeParser(), true, true), 
 	NODE_SIZE("Node Size", "nodeUniformSizeCalculator", "node.size", "defaultNodeSize",
 	          Number.class, new NodeSizeProp(),
 			  new DoubleParser(), true, true), 
 	NODE_WIDTH("Node Width", "nodeWidthCalculator", "node.width", "defaultNodeWidth",
 	           Number.class, new NodeWidthProp(),
 			   new DoubleParser(), true, true), 
 	NODE_HEIGHT("Node Height", "nodeHeightCalculator", "node.height", "defaultNodeHight",
 	            Number.class, new NodeHeightProp(),
 				new DoubleParser(), true, true), 
 	NODE_LABEL("Node Label", "nodeLabelCalculator", "node.label", "defaultNodeLabel",
 	           String.class, new NodeLabelProp(),
 			   new StringParser(), true, true), 
 	NODE_FONT_FACE("Node Font Face", "nodeFontFaceCalculator", "node.font", "defaultNodeFont",
 	               Font.class, new NodeFontFaceProp(),
 				   new FontParser(), true, true), 
 	NODE_FONT_SIZE("Node Font Size", "nodeFontSizeCalculator", "node.fontSize",
 	               "defaultNodeFontSize", Number.class,
 	               new NodeFontSizeProp(), new DoubleParser(), true, true), 
 	NODE_LABEL_COLOR("Node Label Color", "nodeLabelColor", "node.labelColor",
 	                 "defaultNodeLabelColor", Color.class,
 	                 new NodeLabelColorProp(), new ColorParser(), true, true), 
 	NODE_TOOLTIP("Node Tooltip", "nodeTooltipCalculator", "node.toolTip", "defaultNodeToolTip",
 	             String.class, new NodeToolTipProp(),
 				 new StringParser(), true, true), 
 	NODE_LABEL_POSITION("Node Label Position", "nodeLabelPositionCalculator", "node.labelPosition",
 	                    "defaultNodeLabelPosition", 
 	                    LabelPosition.class, new NodeLabelPositionProp(), new LabelPositionParser(), true, true), 
 	EDGE_COLOR("Edge Color", "edgeColorCalculator", "edge.color", "defaultEdgeColor",
 	           Color.class, new EdgeColorProp(),
 			   new ColorParser(), false, true), 
 	EDGE_LINETYPE("Edge Line Type", "edgeLineTypeCalculator", "edge.lineType",
 	              "defaultEdgeLineType", LineType.class,
 	              new EdgeLineTypeProp(), new LineTypeParser(), false, false), 
 	EDGE_SRCARROW("Edge Source Arrow", "edgeSourceArrowCalculator", "edge.sourceArrow",
 	              "defaultEdgeSourceArrow", Arrow.class,
 	              new EdgeSourceArrowProp(), new ArrowParser(), false, false), 
 	EDGE_TGTARROW("Edge Target Arrow", "edgeTargetArrowCalculator", "edge.targetArrow",
 	              "defaultEdgeTargetArrow", Arrow.class,
 	              new EdgeTargetArrowProp(), new ArrowParser(), false, false), 
 	EDGE_LABEL("Edge Label", "edgeLabelCalculator", "edge.label", "defaultEdgeLabel",
 	           String.class, new EdgeLabelProp(),
 			   new StringParser(), false, true), 
 	EDGE_FONT_FACE("Edge Font Face", "edgeFontFaceCalculator", "edge.font", "defaultEdgeFont",
 	               Font.class, new EdgeFontFaceProp(),
 				   new FontParser(), false, true), 
 	EDGE_FONT_SIZE("Edge Font Size", "edgeFontSizeCalculator", "edge.fontSize",
 	               "defaultEdgeFontSize", Number.class,
 	               new EdgeFontSizeProp(), new DoubleParser(), false, true), 
 	EDGE_LABEL_COLOR("Edge Label Color", "edgeLabelColorCalculator", "edge.labelColor",
 	                 "defaultEdgeLabelColor", Color.class,
 	                 new EdgeLabelColorProp(), new ColorParser(), false, true), 
 	EDGE_TOOLTIP("Edge Tooltip", "edgeTooltipCalculator", "edge.toolTip", "defaultEdgeToolTip",
 	             String.class, new EdgeToolTipProp(),
 				 new StringParser(), false, true), 
 
 	// New from 2.5: line can have arbitrary width.
 	NODE_LINE_WIDTH("Node Line Width", "nodeLineWidthCalculator", "node.lineWidth",
 	                "defaultNodeLineWidth", Number.class,
 	                new NodeLineWidthProp(), new FloatParser(), true, true), 
 	EDGE_LINE_WIDTH("Edge Line Width", "edgeLineWidthCalculator", "edge.lineWidth",
 	                "defaultEdgeLineWidth", Number.class,
 	                new EdgeLineWidthProp(), new FloatParser(), false, true), 
 	NODE_LINE_STYLE("Node Line Style", "nodeLineStyleCalculator", "node.lineStyle",
 	                "defaultNodeLineStyle", LineStyle.class,
 	                new NodeLineStyleProp(), new LineStyleParser(), true, true), 
 	EDGE_LINE_STYLE("Edge Line Style", "edgeLineStyleCalculator", "edge.lineStyle",
 	                "defaultEdgeLineStyle", LineStyle.class,
 	                new EdgeLineStyleProp(), new LineStyleParser(), false, true), 
 
 	// New from 2.5: arrows have its own color, shape, and size.
 	EDGE_SRCARROW_SHAPE("Edge Source Arrow Shape", "edgeSourceArrowShapeCalculator",
 	                    "edge.sourceArrowShape", "defaultEdgeSourceArrowShape",
 	                    ArrowShape.class,
 	                    new EdgeSourceArrowShapeProp(), new ArrowShapeParser(), false, true), 
 	EDGE_TGTARROW_SHAPE("Edge Target Arrow Shape", "edgeTargetArrowShapeCalculator",
 	                    "edge.targetArrowShape", "defaultEdgeTargetArrowShape",
 	                    ArrowShape.class,
 	                    new EdgeTargetArrowShapeProp(), new ArrowShapeParser(), false, true), 
 	EDGE_SRCARROW_COLOR("Edge Source Arrow Color", "edgeSourceArrowColorCalculator",
 	                    "edge.sourceArrowColor", "defaultEdgeSourceArrowColor",
 	                    Color.class,
 	                    new EdgeSourceArrowColorProp(), new ColorParser(), false, true), 
 	EDGE_TGTARROW_COLOR("Edge Target Arrow Color", "edgeTargetArrowColorCalculator",
 	                    "edge.targetArrowColor", "defaultEdgeTargetArrowColor",
 	                    Color.class,
 	                    new EdgeTargetArrowColorProp(), new ColorParser(), false, true),
 	/*
 	 * New in 2.5: Opacity support
 	 */
 	NODE_OPACITY("Node Opacity", "nodeOpacityCalculator", "node.opacity", "defaultNodeOpacity",
 	             Number.class, new NodeOpacityProp(),
 				 new FloatParser(), true, true), 
 	EDGE_OPACITY("Edge Opacity", "edgeOpacityCalculator", "edge.opacity", "defaultEdgeOpacity",
 	    	     Number.class, new EdgeOpacityProp(),
 				 new FloatParser(), false, true), 
 	NODE_LABEL_OPACITY("Node Label Opacity", "nodeLabelOpacityCalculator", "node.LabelOpacity", "defaultNodeLabelOpacity",
 	    	             Number.class, new NodeLabelOpacityProp(),
 						 new FloatParser(), true, true), 
 	EDGE_LABEL_OPACITY("Edge Label Opacity", "edgeLabelOpacityCalculator", "edge.labelOpacity", "defaultEdgeLabelOpacity",
 	    	    	     Number.class, new EdgeLabelOpacityProp(),
 						 new FloatParser(), false, true), 
     NODE_BORDER_OPACITY("Node Border Opacity", "nodeBorderOpacityCalculator", "node.borderOpacity", "defaultNodeBorderOpacity",
 	    	    	             Number.class, new NodeBorderOpacityProp(),
 								 new FloatParser(), true, true), 
 	EDGE_SRCARROW_OPACITY("Edge Source Arrow Opacity", "edgeSourceArrowOpacityCalculator", "edge.sourceArrowOpacity", "defaultEdgeSourceArrowOpacity", Number.class, new EdgeSourceArrowOpacityProp(),
 						 new FloatParser(), false, true), 
 	EDGE_TGTARROW_OPACITY("Edge Target Arrow Opacity", "edgeTargetArrowOpacityCalculator", "edge.targetArrowOpacity", "defaultEdgeTargetArrowOpacity", Number.class, new EdgeTargetArrowOpacityProp(),
 						 new FloatParser(), false, true), 
 	// Not yet implemented in version 2.5
 	EDGE_LABEL_POSITION("Edge Label Position", "edgeLabelPositionCalculator", "edge.labelPosition",
 	                    "defaultEdgeLabelPosition", null, new EdgeLabelPositionProp(),
 						new LabelPositionParser(), false, false),
 						
 	;
 	/*
 	 * String returned by toString() method.
 	 */
 	private final String calcName;
 
 	/*
 	 * Property label in prop file.
 	 */
 	private String propertyLabel;
 
 	/*
 	 * Attribute name for vizmap bypass function (right-click bypass)
 	 */
 	private String bypassAttrName;
 	private String defaultPropertyLabel;
 
 	// Data type for the actual visual property.
 	private Class dataType;
 	private VisualProperty vizProp;
 	private ValueParser valueParser;
 
 	// indicates whether or not property is for a node or edge
 	private boolean isNodeProp;
 
 	// Indicates whether or not we should be using this property for 
 	// normal operations. If this is false, that means the property
 	// either hasn't been implemented or has been deprecated.  This
 	// is primarily used to build lists of properties that we can
 	// display to users.
 	private boolean isAllowed;
 
 	/*
 	 * private constructor to put name into this enum.
 	 */
 	private VisualPropertyType(final String calcName, final String propertyLabel,
 	                           final String bypassAttrName, final String defaultPropertyLabel,
 	                           final Class dataType, final VisualProperty vizProp, 
 							   final ValueParser valueParser, final boolean isNodeProp,
 							   final boolean isAllowed) {
 		this.calcName = calcName;
 		this.propertyLabel = propertyLabel;
 		this.bypassAttrName = bypassAttrName;
 		this.defaultPropertyLabel = defaultPropertyLabel;
 		this.dataType = dataType;
 		this.vizProp = vizProp;
 		this.valueParser = valueParser;
 		this.isNodeProp = isNodeProp;
 		this.isAllowed = isAllowed;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public String getName() {
 		return calcName;
 	}
 
 	/**
 	 * Returns string used as property label in VS prop file.
 	 *
 	 * @return
 	 */
 	public String getPropertyLabel() {
 		return propertyLabel;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public String getBypassAttrName() {
 		return bypassAttrName;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public String getDefaultPropertyLabel() {
 		return defaultPropertyLabel;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @param baseKey
 	 *            DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public String getDefaultPropertyKey(final String baseKey) {
 		return baseKey + "." + defaultPropertyLabel;
 	}
 
 	/**
 	 * Return position in thie enum as byte.<br>
 	 * Will be used as type.
 	 *
 	 * DO NOT USE THIS. This is only for backward compatibility.<br>
 	 * Replace your "byte" with is emum!
 	 *
 	 * @return byte type
 	 * @deprecated Will be removed 5/2008
 	 */
 	@Deprecated
 	public byte getType() {
 		return (byte) ordinal();
 	}
 
 	/**
 	 * DO NOT USE THIS. This is only for backward compatibility.
 	 *
 	 * @param type
 	 * @return
 	 * @deprecated Will be removed 5/2008.
 	 */
 	@Deprecated
 	public static VisualPropertyType getVisualPorpertyType(byte type) {
 		/*
 		 * Type is always equal to ordinal.
 		 */
 		return values()[type];
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public Class getDataType() {
 		return dataType;
 	}
 
 	/**
 	 * Check this visual property is for node or not.
 	 *
 	 * @return true if vp is for node.
 	 */
 	public boolean isNodeProp() {
 		return isNodeProp;
 	}
 
 	/** 
 	 * Indicates whether or not we should be using this property for 
 	 * normal operations. If this is false, that means the property
 	 * either hasn't been implemented or has been deprecated.  This
 	 * is primarily used to build lists of properties that we can
 	 * display to users.
 	 */
 	public boolean isAllowed() {
 		return isAllowed;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public static List<VisualPropertyType> getNodeVisualPropertyList() {
 		List<VisualPropertyType> list = new ArrayList<VisualPropertyType>();
 
 		for (VisualPropertyType type : values()) {
 			if ( type.isNodeProp() && type.isAllowed() ) {
 				list.add(type);
 			}
 		}
 
 		return list;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public static List<VisualPropertyType> getEdgeVisualPropertyList() {
 		List<VisualPropertyType> list = new ArrayList<VisualPropertyType>();
 
 		for (VisualPropertyType type : values()) {
 			if ( !type.isNodeProp() && type.isAllowed() ) {
 				list.add(type);
 			}
 		}
 
 		return list;
 	}
 
 	private Object showEditor(EditorDisplayer action)
 	    throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
 	               SecurityException, NoSuchMethodException {
 		Method method = action.getActionClass()
 		                      .getMethod(action.getCommand(), action.getParamTypes());
 		
 		
 		Object ret = method.invoke(null, action.getParameters());
 
 		// This is an editor.
		if ((ret != null) && ret instanceof ContinuousMappingEditorPanel)
 			return ret;
		
		else if ((ret != null) && this == EDGE_LINE_WIDTH) {
 			try {
 				ret = Float.valueOf(((String)ret));
 			} catch (NumberFormatException e){
 				ret = 1f;
 			}
		} else if ((ret != null) && (action.getCompatibleClass() != ret.getClass())) {
 			try {
 				ret = Double.parseDouble(ret.toString());
 			} catch (NumberFormatException e){
 				ret = 1d;
 			}
			
			
 		}
 		
 		// If size, it should be greater than 0.  Otherwise, 1 will be set.
 		if((this.name()).toUpperCase().endsWith("WIDTH") || (this.name()).toUpperCase().endsWith("SIZE")) {
 			if(((Number)ret).doubleValue() < 0) {
 				ret = 1f;
 			}
 		}
 		
 		if((this.name()).toUpperCase().endsWith("OPACITY")) {
 			if (((Number)ret).doubleValue() > 255) {
 				ret = 255d;
 			} else if(((Number)ret).doubleValue() < 0) {
 				ret = 0d;
 			}
 		}
 
 		return ret;
 	}
 
 	/**
 	 * Display discrete value editor for this visual property.
 	 *
 	 * @return DOCUMENT ME!
 	 *
 	 * @throws Exception DOCUMENT ME!
 	 */
 	public Object showDiscreteEditor() throws Exception {
 		return showEditor(EditorDisplayer.getEditor(this, EditorType.DISCRETE));
 	}
 
 	/**
 	 * Display continuous value editor.
 	 *
 	 * <p>
 	 *         Continuous editor always update mapping automatically, so there is no return value.
 	 * </p>
 	 * @throws Exception DOCUMENT ME!
 	 */
 	public Object showContinuousEditor() throws Exception {
 		final EditorDisplayer editor = EditorDisplayer.getEditor(this, EditorType.CONTINUOUS);
 
 		if (editor == EditorDisplayer.CONTINUOUS_COLOR)
 			editor.setParameters(new Object[] { 450, 180, "Gradient Editor for " + this.calcName, this });
 		else if (editor == EditorDisplayer.CONTINUOUS_CONTINUOUS)
 			editor.setParameters(new Object[] {
 			                         450, 350, "Continuous Editor for " + this.calcName, this
 			                     });
 		else
 			editor.setParameters(new Object[] {
 			                         450, 300, "Continuous Editor for " + this.calcName, this
 			                     });
 
 		return showEditor(editor);
 	}
 
 	/**
 	 * Returns the VisualProperty object associated with this enum.
 	 */
 	public VisualProperty getVisualProperty() {
 		return vizProp;
 	}
 
 	/**
 	 * Gets the current default value for this type in the specified
 	     * visual style. Returns null if the style is null.
 	     * @param style The visual style we want to get the default for.
 	     * @return the default object for this type and the specified style.
 	 */
 	public Object getDefault(VisualStyle style) {
 		if (style == null)
 			return null;
 
 		Appearance a = null;
 
 		if (isNodeProp())
 			a = style.getNodeAppearanceCalculator().getDefaultAppearance();
 		else
 			a = style.getEdgeAppearanceCalculator().getDefaultAppearance();
 
 		return a.get(this);
 	}
 
 	/**
 	 * Sets the default value for the visual attribute for this type
 	 * in the specified visual style. No-op if either arg is null.
 	     * @param style The visual style to be set.
 	     * @param c The new default value.
 	 */
 	public void setDefault(VisualStyle style, Object c) {
 		if ((style == null) || (c == null))
 			return;
 
 		if (isNodeProp()) {
 			NodeAppearanceCalculator nodeCalc = style.getNodeAppearanceCalculator();
 			NodeAppearance na = nodeCalc.getDefaultAppearance();
 			na.set(this, c);
 			nodeCalc.setDefaultAppearance(na);
 		} else {
 			EdgeAppearanceCalculator edgeCalc = style.getEdgeAppearanceCalculator();
 			EdgeAppearance ea = edgeCalc.getDefaultAppearance();
 			ea.set(this, c);
 			edgeCalc.setDefaultAppearance(ea);
 		}
 	}
 
 	/**
 	 * Gets the current calculator for the visual attribute for this type
 	     * and the specified visual style.  This may be null if no calculator
 	     * is currently specified. Returns null if the style is null.
 	     * @param style The style we're getting the calculator for.
 	     * @return the current calculator for this style and type
 	 */
 	public Calculator getCurrentCalculator(VisualStyle style) {
 		if (style == null)
 			return null;
 
 		if (isNodeProp())
 			return style.getNodeAppearanceCalculator().getCalculator(this);
 		else
 
 			return style.getEdgeAppearanceCalculator().getCalculator(this);
 	}
 
 	/**
 	 * Sets the current calculator for the visual attribute for this type
 	 * and the specified visual style. If the new calculator is null, then
 	     * the calculator for this type will be removed. This method does
 	 * nothing if the first argument specifying the visual style is null.
 	     * @param style The style to set the calculator for.
 	     * @param c The calculator to set.
 	 */
 	public void setCurrentCalculator(VisualStyle style, Calculator c) {
 		if (style == null)
 			return;
 
 		if (isNodeProp()) {
 			if (c == null)
 				style.getNodeAppearanceCalculator().removeCalculator(this);
 			else
 				style.getNodeAppearanceCalculator().setCalculator(c);
 		} else {
 			if (c == null)
 				style.getEdgeAppearanceCalculator().removeCalculator(this);
 			else
 				style.getEdgeAppearanceCalculator().setCalculator(c);
 		}
 	}
 
 	public ValueParser getValueParser() {
 		return valueParser;
 	}
 }
