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
 package cytoscape.visual.ui.icon;
 
 import cytoscape.Cytoscape;
 
 import cytoscape.visual.LineType;
 import cytoscape.visual.VisualPropertyType;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.Stroke;
 import java.awt.geom.Line2D;
 
 import javax.swing.SwingUtilities;
 
 /**
  * DOCUMENT ME!
  *
  * @author $author$
   */
 public class LineTypeIcon extends VisualPropertyIcon {
 	private BasicStroke stroke;
 	protected Graphics2D g2d;
 
 	// If not null, this message will be shown over the icon.
 	private String superimposedText = null;
 	private Font textFont = null;
 	private Color textColor = null;
 
 	/**
 	 * Creates a new LineTypeIcon object.
 	 */
 	public LineTypeIcon() {
 		this((BasicStroke) ((LineType) VisualPropertyType.EDGE_LINETYPE.getDefault(Cytoscape.getVisualMappingManager()
 		                                                                                    .getVisualStyle()))
 		     .getStroke(), DEFAULT_ICON_SIZE * 3, DEFAULT_ICON_SIZE,
 		     ((LineType) VisualPropertyType.EDGE_LINETYPE
 		      .getDefault(Cytoscape.getVisualMappingManager().getVisualStyle())).getType().toString(),
 		     (Color) VisualPropertyType.EDGE_COLOR.getDefault(Cytoscape.getVisualMappingManager()
 		                                                               .getVisualStyle()));
 	}
 
 	/**
 	 * Creates a new LineTypeIcon object.
 	 *
 	 * @param lineType  DOCUMENT ME!
 	 */
 	public LineTypeIcon(Object lineType) {
 		this((BasicStroke) ((LineType) lineType).getStroke(), DEFAULT_ICON_SIZE * 3,
 		     DEFAULT_ICON_SIZE,
 		     ((LineType) lineType).getType().toString(),
 		     (Color) VisualPropertyType.EDGE_COLOR.getDefault(Cytoscape.getVisualMappingManager()
 		                                                               .getVisualStyle()));
 	}
 	
 	public LineTypeIcon(cytoscape.visual.LineStyle style) {
 		this(style.getStroke(2f), DEFAULT_ICON_SIZE * 3,
 			     DEFAULT_ICON_SIZE, style.name());
 	}
 
 	/**
 	 * Creates a new LineTypeIcon object.
 	 *
 	 * @param stroke DOCUMENT ME!
 	 * @param width DOCUMENT ME!
 	 * @param height DOCUMENT ME!
 	 * @param name DOCUMENT ME!
 	 */
 	public LineTypeIcon(Stroke stroke, int width, int height, String name) {
 		super(null, width, height, name);
 		this.stroke = (BasicStroke) stroke;
 	}
 
 	/**
 	 * Creates a new LineTypeIcon object.
 	 *
 	 * @param stroke DOCUMENT ME!
 	 * @param width DOCUMENT ME!
 	 * @param height DOCUMENT ME!
 	 * @param name DOCUMENT ME!
 	 * @param color DOCUMENT ME!
 	 */
 	public LineTypeIcon(Stroke stroke, int width, int height, String name, Color color) {
 		super(null, width, height, name, color);
 
 		final float lineWidth = ((Number) VisualPropertyType.EDGE_LINE_WIDTH.getDefault(Cytoscape.getVisualMappingManager()
 		                                                                                         .getVisualStyle()))
 		                        .floatValue();
 
 		final BasicStroke st = (BasicStroke) stroke;
 		/*
 		 * Define a stroke for the line segment icon
 		 */
 		if ((st != null) && (st.getDashArray() != null)) {
			this.stroke = new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER,
 			                              st.getMiterLimit(), st.getDashArray(),
 			                              st.getDashPhase());
		} else if (st != null) {
			// This is a solid line.
			this.stroke = new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
 		} else {
			this.stroke = new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
 		}
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @param c DOCUMENT ME!
 	 * @param g DOCUMENT ME!
 	 * @param x DOCUMENT ME!
 	 * @param y DOCUMENT ME!
 	 */
 	public void paintIcon(Component c, Graphics g, int x, int y) {
 		g2d = (Graphics2D) g;
 		g2d.setColor(color);
 		// AA on
 		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
 		g2d.translate(leftPad, bottomPad);
 
 		float[] dashDef = null;
 
 		//        if (stroke.getDashArray() != null)
 		//            dashDef = stroke.getDashArray();
 		//
 		//        final BasicStroke lineStroke = new BasicStroke(
 		//                stroke.getLineWidth(),
 		//                BasicStroke.CAP_BUTT,
 		//                BasicStroke.JOIN_MITER,
 		//                10.0f,
 		//                dashDef,
 		//                0.0f);
 		g2d.setStroke(stroke);
 		g2d.draw(new Line2D.Double(20, (height + 20) / 2, width, (height + 20) / 2));
 
 		/*
 		 * Superimpose text if text object is not empty.
 		 */
 		if (superimposedText != null) {
 			int strWidth = SwingUtilities.computeStringWidth(g2d.getFontMetrics(), superimposedText);
 
 			if (textColor == null) {
 				g2d.setColor(Color.DARK_GRAY);
 			} else
 				g2d.setColor(textColor);
 
 			if (textFont == null) {
 				g2d.setFont(new Font("SansSerif", Font.BOLD, 24));
 			} else {
 				g2d.setFont(textFont);
 			}
 
 			g2d.drawString(superimposedText, 20, (height + 40) / 2);
 		}
 
 		g2d.translate(-leftPad, -bottomPad);
 		g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param text DOCUMENT ME!
 	 */
 	public void setText(final String text) {
 		this.superimposedText = text;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param font DOCUMENT ME!
 	 */
 	public void setTextFont(final Font font) {
 		this.textFont = font;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param color DOCUMENT ME!
 	 */
 	public void setTextColor(final Color color) {
 		this.textColor = color;
 	}
 }
