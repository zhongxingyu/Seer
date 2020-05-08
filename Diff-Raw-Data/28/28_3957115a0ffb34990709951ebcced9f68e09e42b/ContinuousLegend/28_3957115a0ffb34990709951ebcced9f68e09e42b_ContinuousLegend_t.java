 /*
   File: ContinuousUI.java
 
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
 package cytoscape.visual.mappings.continuous;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GradientPaint;
 import java.awt.Graphics2D;
 import java.awt.GridLayout;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.image.BufferedImage;
 import java.util.List;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 import javax.swing.border.Border;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.MatteBorder;
 
 import cytoscape.visual.VisualPropertyType;
 import cytoscape.visual.mappings.LegendTable;
 import cytoscape.visual.ui.editors.continuous.C2CMappingEditor;
 import cytoscape.visual.ui.editors.continuous.C2DMappingEditor;
 import cytoscape.visual.ui.editors.continuous.GradientEditorPanel;
 
 
 /**
  *
  */
 public class ContinuousLegend extends JPanel {
 	private static final Font TITLE_FONT2 = new Font("SansSerif", Font.BOLD, 18);
 	private static final Color TITLE_COLOR = new Color(10, 200, 255);
 	private static final Border BORDER = new MatteBorder(0, 6, 3, 0, Color.DARK_GRAY);
 	private List points;
 	private VisualPropertyType type;
 	private JLabel legend = null;
 
 	/**
 	 * Creates a new ContinuousLegend object.
 	 *
 	 * @param visualAttr  DOCUMENT ME!
 	 * @param dataAttr  DOCUMENT ME!
 	 * @param points  DOCUMENT ME!
 	 * @param obj  DOCUMENT ME!
 	 * @param b  DOCUMENT ME!
 	 * @deprecated Use constructor with VisualPropertyType instead. Gone 5/2008.
 	 */
 	@Deprecated
 	public ContinuousLegend(String visualAttr, String dataAttr, List points, Object obj, byte b) {
 		this(dataAttr, points, obj, VisualPropertyType.getVisualPorpertyType(b));
 	}
 
 	/**
 	 * Creates a new ContinuousLegend object.
 	 *
 	 * @param dataAttr  DOCUMENT ME!
 	 * @param points  DOCUMENT ME!
 	 * @param obj  DOCUMENT ME!
 	 * @param vpt  DOCUMENT ME!
 	 */
 	public ContinuousLegend(String dataAttr, List points, Object obj, VisualPropertyType vpt) {
 		super();
 		this.points = points;
 		this.type = vpt;
		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				setLegend(e);
			}
		});
 
 		setLayout(new BorderLayout());
 		setBackground(Color.white);
 		setBorder(BORDER);
 
 		final JLabel title = new JLabel(" " + vpt.getName() + " Mapping");
 		title.setFont(TITLE_FONT2);
 		title.setForeground(TITLE_COLOR);
 		title.setBorder(new MatteBorder(0, 10, 1, 0, TITLE_COLOR));
 
 		title.setHorizontalTextPosition(SwingConstants.LEADING);
 		title.setPreferredSize(new Dimension(1, 50));
 		add(title, BorderLayout.NORTH);
 
 		setLegend(null);
 	}
 
 	private void setLegend(ComponentEvent e) {
 		if (legend != null) {
 			remove(legend);
 		}
 
 		Integer trackW = null;
 
 		if (getParent() == null) {
 			trackW = 600;
 		} else {
			trackW = ((Number)(this.getParent().getParent().getParent().getWidth()*0.82)).intValue();
			if(trackW < 200) {
				trackW = 200;
			}
 		}
 
 		if (type.getDataType() == Color.class) {
 			legend = new JLabel(GradientEditorPanel.getLegend(trackW, 100, type));
 		} else if (type.getDataType() == Number.class) {
 			legend = new JLabel(C2CMappingEditor.getLegend(trackW, 150, type));
 		} else {
 			legend = new JLabel(C2DMappingEditor.getLegend(trackW, 150, type));
 		}
 
 		legend.setBorder(new EmptyBorder(10, 10, 10, 10));
 		add(legend, BorderLayout.CENTER);
		repaint();
 	}
 
 	private JPanel getGradientPanel() {
 		JPanel holder = new JPanel();
 		holder.setLayout(new GridLayout(1, 2));
 		holder.setAlignmentX(0);
 		holder.setBackground(Color.white);
 
 		JLabel grad = new JLabel(getColorGradientIcon());
 		grad.setAlignmentX(0);
 		holder.add(grad);
 
 		JLabel num = new JLabel(getNumberGradientIcon());
 		num.setAlignmentX(0);
 		holder.add(num);
 
 		return holder;
 	}
 
 	int width = 40;
 	int height = 40;
 	int yoff = height;
 
 	private ImageIcon getNumberGradientIcon() {
 		int imageHeight = (points.size() + 1) * height;
 		BufferedImage bi = new BufferedImage(width, imageHeight, BufferedImage.TYPE_INT_RGB);
 		Graphics2D g2 = bi.createGraphics();
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		g2.setPaint(Color.white);
 		g2.fillRect(0, 0, width, imageHeight);
 		g2.setPaint(Color.black);
 
 		int yoff = (int) (((float) g2.getFontMetrics().getMaxAscent()) / 2);
 
 		ContinuousMappingPoint curr = null;
 
 		for (int i = 0; i < points.size(); i++) {
 			curr = (ContinuousMappingPoint) points.get(i);
 
 			g2.drawString(Double.toString(curr.getValue()), 0, ((i + 1) * height) + yoff);
 		}
 
 		return new ImageIcon(bi);
 	}
 
 	private ImageIcon getColorGradientIcon() {
 		int imageHeight = (points.size() + 1) * height;
 		BufferedImage bi = new BufferedImage(width, imageHeight, BufferedImage.TYPE_INT_RGB);
 		Graphics2D g2 = bi.createGraphics();
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		g2.setPaint(Color.white);
 		g2.fillRect(0, 0, width, imageHeight);
 
 		ContinuousMappingPoint curr = null;
 		ContinuousMappingPoint next = null;
 
 		Rectangle rect = new Rectangle(0, 0, width, height);
 
 		for (int i = 0; i < points.size(); i++) {
 			curr = (ContinuousMappingPoint) points.get(i);
 
 			if ((i + 1) < points.size())
 				next = (ContinuousMappingPoint) points.get(i + 1);
 			else
 				next = null;
 
 			if (i == 0) {
 				g2.setPaint((Color) (curr.getRange().lesserValue));
 				rect.setBounds(0, 0, width, height);
 				g2.fill(rect);
 			}
 
 			if (next != null) {
 				GradientPaint gp = new GradientPaint(0, ((i + 1) * height),
 				                                     (Color) curr.getRange().equalValue, 0,
 				                                     ((i + 2) * height),
 				                                     (Color) next.getRange().equalValue);
 				g2.setPaint(gp);
 				rect.setBounds(0, ((i + 1) * height), width, height);
 				g2.fill(rect);
 			} else {
 				g2.setPaint((Color) (curr.getRange().greaterValue));
 				rect.setBounds(0, ((i + 1) * height), width, height);
 				g2.fill(rect);
 			}
 		}
 
 		return new ImageIcon(bi);
 	}
 
 	private JPanel getObjectPanel(VisualPropertyType vpt) {
 		Object[][] data = new Object[points.size() + 2][2];
 
 		ContinuousMappingPoint curr = null;
 
 		for (int i = 0; i < points.size(); i++) {
 			curr = (ContinuousMappingPoint) points.get(i);
 
 			if (i == 0) {
 				data[i][0] = curr.getRange().lesserValue;
 				data[i][1] = "< " + Double.toString(curr.getValue());
 			}
 
 			data[i + 1][0] = curr.getRange().equalValue;
 			data[i + 1][1] = "= " + Double.toString(curr.getValue());
 
 			if (i == (points.size() - 1)) {
 				data[i + 2][0] = curr.getRange().greaterValue;
 				data[i + 2][1] = "> " + Double.toString(curr.getValue());
 			}
 		}
 
 		LegendTable lt = new LegendTable(data, vpt);
 
 		return lt;
 	}
 }
