 /*
  File: NodeAppearance.java
 
  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
 
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
 
 //----------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //----------------------------------------------------------------------------
 package cytoscape.visual;
 
 import static cytoscape.visual.VisualPropertyType.NODE_BORDER_COLOR;
 import static cytoscape.visual.VisualPropertyType.NODE_FILL_COLOR;
 import static cytoscape.visual.VisualPropertyType.NODE_FONT_FACE;
 import static cytoscape.visual.VisualPropertyType.NODE_FONT_SIZE;
 import static cytoscape.visual.VisualPropertyType.NODE_HEIGHT;
 import static cytoscape.visual.VisualPropertyType.NODE_LABEL;
 import static cytoscape.visual.VisualPropertyType.NODE_LABEL_COLOR;
 import static cytoscape.visual.VisualPropertyType.NODE_LABEL_POSITION;
 import static cytoscape.visual.VisualPropertyType.NODE_LINETYPE;
 import static cytoscape.visual.VisualPropertyType.NODE_OPACITY;
 import static cytoscape.visual.VisualPropertyType.NODE_SHAPE;
 import static cytoscape.visual.VisualPropertyType.NODE_SIZE;
 import static cytoscape.visual.VisualPropertyType.NODE_TOOLTIP;
 import static cytoscape.visual.VisualPropertyType.NODE_WIDTH;
 import giny.view.NodeView;
 
 import java.awt.Color;
 import java.awt.Font;
 
 
 /**
  * Objects of this class hold data describing the appearance of a Node.
  */
 public class NodeAppearance extends Appearance {
 
 	/**
 	 * Constructor.
 	 */
 	public NodeAppearance() {
 		super();
 	}
 
 	/**
 	 * Clone.
 	 */
     public Object clone() {
 		NodeAppearance ga = new NodeAppearance();
 		ga.copy(this);
 		return ga;
 	}
 
     /**
      * @deprecated Use Appearance.get(VisualPropertyType) instead. Will be removed 5/2008.
      */
     public Color getFillColor() {
         return (Color)(super.get(NODE_FILL_COLOR));
     }
 
     /**
      * @deprecated Use Appearance.set(VisualPropertyType,Object) instead. Will be removed 5/2008.
      */
     public void setFillColor(Color c) {
 		set(NODE_FILL_COLOR,c);
     }
     
     /**
      * @deprecated Use Appearance.get(VisualPropertyType) instead. Will be removed 5/2008.
      */
     public Color getBorderColor() {
         return (Color)(get(NODE_BORDER_COLOR));
     }
 
     /**
      * @deprecated Use Appearance.set(VisualPropertyType,Object) instead. Will be removed 5/2008.
      */
     public void setBorderColor(Color c) {
 		set(NODE_BORDER_COLOR,c);
     }
 
     /**
      * @deprecated Use Appearance.get(VisualPropertyType) instead. Will be removed 5/2008.
      */
     public LineType getBorderLineType() {
        	return (LineType)(get(NODE_LINETYPE));
     }
 
     /**
      * @deprecated Use Appearance.set(VisualPropertyType,Object) instead. Will be removed 5/2008.
      */
     public void setBorderLineType(LineType lt) {
 		set(NODE_LINETYPE,lt);
     }
 
     /**
      * @deprecated Use Appearance.get(VisualPropertyType) instead. Will be removed 5/2008.
      */
     public byte getShape() {
 		return (byte)(((NodeShape)(get(NODE_SHAPE))).ordinal());
     }
 
     /**
      * @deprecated Use Appearance.get(VisualPropertyType) instead. Will be removed 5/2008.
      */
     public NodeShape getNodeShape() {
        	return (NodeShape)(get(NODE_SHAPE));
     }
 
     /**
      * @deprecated Use Appearance.set(VisualPropertyType,Object) instead. Will be removed 5/2008.
      */
     public void setShape(byte s) {
		set(NODE_SHAPE,VisualPropertyType.getVisualPorpertyType(s));
     }
 
     /**
      * @deprecated Use Appearance.set(VisualPropertyType,Object) instead. Will be removed 5/2008.
      */
     public void setNodeShape(NodeShape s) {
 		set(NODE_SHAPE,s);
     }
 
     /**
      * @deprecated Use Appearance.get(VisualPropertyType) instead. Will be removed 5/2008.
      */
     public double getWidth() {
         if (nodeSizeLocked)
             return ((Double)(get(NODE_SIZE))).doubleValue();
         else
             return ((Double)(get(NODE_WIDTH))).doubleValue();
     }
 
     /**
      * @deprecated Use Appearance.set(VisualPropertyType,Object) instead. Will be removed 5/2008.
      * Sets only the height variable.
      */
     public void setJustWidth(double d) {
 		set(NODE_WIDTH,new Double(d));
     }
 
     /**
      * Sets the width variable, but also the size variable if the node size is
      * locked. This is to support deprecated code that used setting width/height
      * for setting uniform size as well.
      * @deprecated Use Appearance.set(VisualPropertyType,Object) instead. Will be removed 5/2008.
      */
     public void setWidth(double d) {
 		set(NODE_WIDTH,new Double(d));
 
         if (nodeSizeLocked)
 			set(NODE_SIZE,new Double(d));
     }
 
     /**
      * @deprecated Use Appearance.get(VisualPropertyType) instead. Will be removed 5/2008.
      */
     public double getHeight() {
         if (nodeSizeLocked)
             return ((Double)(get(NODE_SIZE))).doubleValue();
         else
             return ((Double)(get(NODE_HEIGHT))).doubleValue();
     }
 
     /**
      * Sets only the height variable.
      * @deprecated Use Appearance.set(VisualPropertyType,Object) instead. Will be removed 5/2008.
      */
     public void setJustHeight(double d) {
 		set(NODE_HEIGHT,new Double(d));
     }
 
     /**
      * Sets the height variable, but also the size variable if the node size is
      * locked. This is to support deprecated code that used setting width/height
      * for setting uniform size as well.
      * @deprecated Use Appearance.set(VisualPropertyType,Object) instead. Will be removed 5/2008.
      */
     public void setHeight(double d) {
 		set(NODE_HEIGHT,new Double(d));
 
         if (nodeSizeLocked)
 			set(NODE_SIZE,new Double(d));
     }
 
     /**
      * @deprecated Use Appearance.get(VisualPropertyType) instead. Will be removed 5/2008.
      */
     public double getSize() {
         return ((Double)(get(NODE_SIZE))).doubleValue();
     }
 
     /**
      * @deprecated Use Appearance.set(VisualPropertyType,Object) instead. Will be removed 5/2008.
      */
     public void setSize(double s) {
 		set(NODE_SIZE,new Double(s));
     }
 
     /**
      * @deprecated Use Appearance.get(VisualPropertyType) instead. Will be removed 5/2008.
      */
     public String getLabel() {
         return (String)(get(NODE_LABEL));
     }
 
     /**
      * @deprecated Use Appearance.set(VisualPropertyType,Object) instead. Will be removed 5/2008.
      */
     public void setLabel(String s) {
 		set(NODE_LABEL,s);
     }
 
     /**
      * @deprecated Use Appearance.get(VisualPropertyType) instead. Will be removed 5/2008.
      */
     public String getToolTip() {
         return (String)(get(NODE_TOOLTIP));
     }
 
     /**
      * @deprecated Use Appearance.set(VisualPropertyType,Object) instead. Will be removed 5/2008.
      */
     public void setToolTip(String s) {
 		set(NODE_TOOLTIP,s);
     }
 
     /**
      * @deprecated Use Appearance.get(VisualPropertyType) instead. Will be removed 5/2008.
      */
     public Font getFont() {
         return (Font)(get(NODE_FONT_FACE));
     }
 
     /**
      * @deprecated Use Appearance.set(VisualPropertyType,Object) instead. Will be removed 5/2008.
      */
     public void setFont(Font f) {
 		set(NODE_FONT_FACE,f);
     }
 
     /**
      * @deprecated Use Appearance.get(VisualPropertyType) instead. Will be removed 5/2008.
      */
     public float getFontSize() {
         return ((Number)(get(NODE_FONT_SIZE))).floatValue();
     }
 
     /**
      * @deprecated Use Appearance.set(VisualPropertyType,Object) instead. Will be removed 5/2008.
      */
     public void setFontSize(float f) {
 		set(NODE_FONT_SIZE,new Float(f));
     }
 
     /**
      * @deprecated Use Appearance.get(VisualPropertyType) instead. Will be removed 5/2008.
      */
     public Color getLabelColor() {
         return (Color)(get(NODE_LABEL_COLOR));
     }
 
     /**
      * @deprecated Use Appearance.set(VisualPropertyType,Object) instead. Will be removed 5/2008.
      */
     public void setLabelColor(Color c) {
 		set(NODE_LABEL_COLOR,c);
     }
 
     /**
      * @deprecated Use Appearance.get(VisualPropertyType) instead. Will be removed 5/2008.
      */
     public LabelPosition getLabelPosition() {
         return (LabelPosition)(get(NODE_LABEL_POSITION));
     }
 
     /**
      * @deprecated Use Appearance.set(VisualPropertyType,Object) instead. Will be removed 5/2008.
      */
     public void setLabelPosition(LabelPosition c) {
 		set(NODE_LABEL_POSITION,c);
     }
 
     /**
      * @deprecated Use Appearance.get(VisualPropertyType) instead. Will be removed 5/2008.
      */
     public Object get(byte b) {
 	        return get(VisualPropertyType.getVisualPorpertyType(b));
     }
 
     /**
      * @deprecated Use Appearance.set(VisualPropertyType,Object) instead. Will be removed 5/2008.
      */
 	public void set(byte b, Object o) {
 		set(VisualPropertyType.getVisualPorpertyType(b),o);
 	}
 }
