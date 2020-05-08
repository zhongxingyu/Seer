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
 package cytoscape.visual.properties;
 
 import cytoscape.Cytoscape;
 
 import cytoscape.visual.ArrowShape;
 import cytoscape.visual.VisualPropertyType;
 
 import cytoscape.visual.ui.icon.LineTypeIcon;
 
 import cytoscape.visual.VisualPropertyDependency;
 import static cytoscape.visual.VisualPropertyDependency.Definition.*;
 
 import giny.view.EdgeView;
 
 import java.awt.Color;
 import java.awt.Paint;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.util.Properties;
 
 
 import java.util.Properties;
 
 import javax.swing.Icon;
 
 
 /**
  *
  */
 public class EdgeSourceArrowOpacityProp extends AbstractVisualProperty {
 
 	public EdgeSourceArrowOpacityProp() {
 		validator = new OpacityValidator();
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public VisualPropertyType getType() {
 		return VisualPropertyType.EDGE_SRCARROW_OPACITY;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public Icon getIcon(final Object value) {
         final LineTypeIcon icon = new LineTypeIcon() {
             public void paintIcon(Component c, Graphics g, int x, int y) {
                 super.setColor(new Color(10, 10, 10, 0));
                 super.paintIcon(c, g, x, y);
 
                 g2d.setFont(new Font("SansSerif", Font.BOLD, 24));
                 g2d.setColor(Color.DARK_GRAY);
                 g2d.drawString(value.toString(),
                                c.getX() + ((LineTypeIcon.DEFAULT_ICON_SIZE * 3) / 2),
                                (int) ((c.getHeight() / 2) + 7));
 
                 g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
             }
         };
 
         return icon;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param ev DOCUMENT ME!
 	 * @param o DOCUMENT ME!
 	 */
 	public void applyToEdgeView(EdgeView ev, Object o, VisualPropertyDependency dep) {
 		if ((o == null) || (ev == null))
 			return;
 
 		if ( dep != null && dep.check(ARROW_COLOR_MATCHES_EDGE) ) {
             if (ev.getUnselectedPaint() != ev.getSourceEdgeEndPaint())
                 ev.setSourceEdgeEndPaint(ev.getUnselectedPaint());
		} else {
 			final Color oldPaint = (Color) ev.getSourceEdgeEndPaint();
 
 			Integer tp = oldPaint.getAlpha();
 			Integer newTp = ((Number) o).intValue();
 
 			if (tp != newTp) 
 				ev.setSourceEdgeEndPaint(new Color(oldPaint.getRed(), oldPaint.getGreen(), 
 				                                   oldPaint.getBlue(), newTp));
 		}
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public Object getDefaultAppearanceObject() {
 		return new Integer(255); 
 	}
 
 	public boolean constrained(VisualPropertyDependency dep) {
 		return ( dep != null && dep.check(ARROW_COLOR_MATCHES_EDGE) );
 	}
 }
