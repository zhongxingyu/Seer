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
 package cytoscape.visual.ui.editors.continuous;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Polygon;
 import java.awt.RenderingHints;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 
 import org.jdesktop.swingx.JXMultiThumbSlider;
 import org.jdesktop.swingx.multislider.Thumb;
 
 import cytoscape.Cytoscape;
 import cytoscape.visual.VisualPropertyType;
 import cytoscape.visual.mappings.BoundaryRangeValues;
 import cytoscape.visual.mappings.ContinuousMapping;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author $author$
   */
 public class ContinuousTrackRenderer extends JComponent implements VizMapperTrackRenderer {
 	/*
 	 * Constants for diagram.
 	 */
 	private final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 12);
 	private static final Font ICON_FONT = new Font("SansSerif", Font.BOLD, 8);
 	private static final int THUMB_WIDTH = 12;
 	private final Font smallFont = new Font("SansSerif", Font.BOLD, 10);
 	private final Font defFont = new Font("SansSerif", Font.BOLD, 12);
 	private static final Color VALUE_AREA_COLOR = new Color(0, 180, 255, 100);
 	private Map<Integer, Double> valueMap;
 	private static final int LEFT_SPACE = 50;
 
 	/*
 	 * Define Colors used in this diagram.
 	 */
 	private int trackHeight = 120;
 	private int arrowBarPosition = trackHeight + 50;
 	private static final Color BORDER_COLOR = Color.black;
 	private double valueRange;
 
 	/*
 	 * Min and Max for X-Axis.
 	 */
 	private double minValue;
 	private double maxValue;
 
 	/*
 	 * Min and Max for the Y-Axis.
 	 */
 	private float min = 0;
 	private float max = 0;
 	private boolean clickFlag = false;
 	private boolean dragFlag = false;
 	private Point curPoint;
 	private JXMultiThumbSlider slider;
 	private CMouseListener listener = null;
 	private Map<Integer, Point> verticesList;
 	private int selectedIdx;
 	private Point dragOrigin;
 	private VisualPropertyType type;
 	private ContinuousMapping cMapping;
 	private String title;
 	private Number below;
 	private Number above;
 	private List<Float> values = new ArrayList<Float>();
 	private Polygon valueArea = new Polygon();
 	private Point belowSquare;
 	private Point aboveSquare;
 
 	/**
 	 * Creates a new ContinuousTrackRenderer object.
 	 *
 	 * @param minValue DOCUMENT ME!
 	 * @param maxValue DOCUMENT ME!
 	 */
 	public ContinuousTrackRenderer(VisualPropertyType type, double minValue, double maxValue,
 	                               Number below, Number above) {
 		this.minValue = minValue;
 		this.maxValue = maxValue;
 		this.below = below;
 		this.above = above;
 
 		this.type = type;
 
 		if (type.isNodeProp())
 			cMapping = (ContinuousMapping) Cytoscape.getVisualMappingManager().getVisualStyle()
 			                                        .getNodeAppearanceCalculator()
 			                                        .getCalculator(type).getMapping(0);
 		else
 			cMapping = (ContinuousMapping) Cytoscape.getVisualMappingManager().getVisualStyle()
 			                                        .getEdgeAppearanceCalculator()
 			                                        .getCalculator(type).getMapping(0);
 
 		title = cMapping.getControllingAttributeName();
 		valueRange = Math.abs(maxValue - minValue);
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @param g DOCUMENT ME!
 	 */
 	public void paint(Graphics g) {
 		super.paint(g);
 		paintComponent(g);
 	}
 
 	/**
 	 * Remove square
 	 *
 	 * @param index
 	 */
 	protected void removeSquare(Integer index) {
 		System.out.println("\n\nTrying to remove " + index);
 
 		for (Object key : verticesList.keySet()) {
 			System.out.println("Key = " + key + ", " + verticesList.get(key));
 		}
 
 		verticesList.remove(index);
 
 		for (Object key : verticesList.keySet()) {
 			System.out.println("Key After = " + key + ", " + verticesList.get(key));
 		}
 	}
 
 	/*
 	 * Drawing actual track.<br>
 	 *
 	 * (non-Javadoc)
 	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
 	 */
 	protected void paintComponent(Graphics gfx) {
 		trackHeight = slider.getHeight() - 100;
 		arrowBarPosition = trackHeight + 50;
 
 		// AA on
 		Graphics2D g = (Graphics2D) gfx;
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
 		int thumb_width = 12;
 		int track_width = slider.getWidth() - thumb_width;
 		g.translate(thumb_width / 2, 12);
 
 		// get the list of tumbs
 		List<Thumb> stops = slider.getModel().getSortedThumbs();
 
 		int numPoints = stops.size();
 
 		// set up the data for the gradient
 		float[] fractions = new float[numPoints];
 		Float[] floatProperty = new Float[numPoints];
 		int i = 0;
 
 		values.clear();
 		values.add(below.floatValue());
 		values.add(above.floatValue());
 
 		for (Thumb thumb : stops) {
 			floatProperty[i] = (Float) thumb.getObject();
 			fractions[i] = thumb.getPosition();
 			values.add((Float) thumb.getObject());
 			i++;
 		}
 
 		for (Float val : values) {
 			if (min >= val)
 				min = val;
 
 			if (max <= val)
 				max = val;
 		}
 
 		// Draw arrow bar
 		g.setStroke(new BasicStroke(1.0f));
 		g.setColor(Color.black);
 		g.drawLine(0, arrowBarPosition, track_width, arrowBarPosition);
 
 		Polygon arrow = new Polygon();
 		arrow.addPoint(track_width, arrowBarPosition);
 		arrow.addPoint(track_width - 20, arrowBarPosition - 8);
 		arrow.addPoint(track_width - 20, arrowBarPosition);
 		g.fill(arrow);
 
 		g.setColor(Color.gray);
 		g.drawLine(0, arrowBarPosition, 15, arrowBarPosition - 30);
 		g.drawLine(15, arrowBarPosition - 30, 25, arrowBarPosition - 30);
 
 		g.setFont(smallFont);
 		g.drawString("Min=" + minValue, 28, arrowBarPosition - 25);
 
 		g.drawLine(track_width, arrowBarPosition, track_width - 15, arrowBarPosition + 30);
 		g.drawLine(track_width - 15, arrowBarPosition + 30, track_width - 25, arrowBarPosition + 30);
 
 		final String maxStr = "Max=" + maxValue;
 		int strWidth = SwingUtilities.computeStringWidth(g.getFontMetrics(), maxStr);
 		g.drawString(maxStr, track_width - strWidth - 26, arrowBarPosition + 35);
 
 		g.setFont(defFont);
 		g.setColor(Color.black);
 		strWidth = SwingUtilities.computeStringWidth(g.getFontMetrics(), title);
 		g.drawString(title, (track_width / 2) - (strWidth / 2), arrowBarPosition + 35);
 
 		/*
 		 * If no points, just draw empty box.
 		 */
 		if (numPoints == 0) {
 			g.setColor(BORDER_COLOR);
 			g.setStroke(new BasicStroke(1.5f));
 			g.drawRect(0, 5, track_width, trackHeight);
 
 			return;
 		}
 
 		g.setStroke(new BasicStroke(1.0f));
 
 		/*
 		 * Fill background
 		 */
 		g.setColor(Color.white);
 		g.fillRect(0, 5, track_width, trackHeight);
 
 		int newX = 0;
 		int lastY = 0;
 
 		Point2D p1 = new Point2D.Float(0, 5);
 		Point2D p2 = new Point2D.Float(0, 5);
 
 		for (i = 0; i < floatProperty.length; i++) {
 			newX = (int) (track_width * (fractions[i] / 100));
 
 			p2.setLocation(newX, 5);
 
 			int newY = (5 + trackHeight) - (int) ((floatProperty[i] / max) * trackHeight);
 
 			valueArea.reset();
 
 			g.setColor(VALUE_AREA_COLOR);
 
 			if (i == 0) {
 				int h = (5 + trackHeight) - (int) ((below.floatValue() / max) * trackHeight);
 				g.fillRect(0, h, newX, (int) ((below.floatValue() / max) * trackHeight));
 				g.setColor(Color.red);
 				g.fillRect(-5, h - 5, 10, 10);
 				belowSquare = new Point(0, h);
 			} else {
 				valueArea.addPoint((int) p1.getX(), lastY);
 				valueArea.addPoint(newX, newY);
 				valueArea.addPoint(newX, trackHeight + 5);
 				valueArea.addPoint((int) p1.getX(), trackHeight + 5);
 				g.fill(valueArea);
 			}
 
 			for (int j = 0; j < stops.size(); j++) {
 				if (slider.getModel().getThumbAt(j).getObject() == floatProperty[i]) {
 					Point newPoint = new Point(newX, newY);
 
 					if (verticesList.containsValue(newPoint) == false)
 						verticesList.put(j, new Point(newX, newY));
 
 					break;
 				}
 			}
 
 			lastY = newY;
 
 			g.setColor(Color.black);
 			g.setStroke(new BasicStroke(1.5f));
 			g.setFont(smallFont);
 
 			int numberWidth = SwingUtilities.computeStringWidth(g.getFontMetrics(),
 			                                                    floatProperty[i].toString());
 
 			g.setColor(Color.DARK_GRAY);
 
 			if (fractions[i] < 10) {
 				g.drawLine(newX, newY, newX + 15, newY - 35);
 				g.drawString(floatProperty[i].toString(), newX + numberWidth, newY - 48);
 			} else {
 				g.drawLine(newX, newY, newX - 15, newY + 35);
 				g.drawString(floatProperty[i].toString(), newX - (numberWidth + 5), newY + 48);
 			}
 
 			g.setColor(Color.DARK_GRAY);
 			g.setFont(new Font("SansSerif", Font.BOLD, 10));
 
 			Float curPositionValue = ((Double) (((fractions[i] / 100) * valueRange)
			                         + minValue)).floatValue();
 			String valueString = String.format("%.4f", curPositionValue);
 
 			int flipLimit = 90;
 			int borderVal = track_width - newX;
 
 			if (((i % 2) == 0) && (flipLimit < borderVal)) {
 				g.drawLine(newX, arrowBarPosition, newX + 20, arrowBarPosition - 15);
 				g.drawLine(newX + 20, arrowBarPosition - 15, newX + 30, arrowBarPosition - 15);
 				g.setColor(Color.black);
 				g.drawString(valueString, newX + 33, arrowBarPosition - 11);
 			} else if (((i % 2) == 1) && (flipLimit < borderVal)) {
 				g.drawLine(newX, arrowBarPosition, newX + 20, arrowBarPosition + 15);
 				g.drawLine(newX + 20, arrowBarPosition + 15, newX + 30, arrowBarPosition + 15);
 				g.setColor(Color.black);
 				g.drawString(valueString, newX + 33, arrowBarPosition + 19);
 			} else if (((i % 2) == 0) && (flipLimit >= borderVal)) {
 				g.drawLine(newX, arrowBarPosition, newX - 20, arrowBarPosition - 15);
 				g.drawLine(newX - 20, arrowBarPosition - 15, newX - 30, arrowBarPosition - 15);
 				g.setColor(Color.black);
 				g.drawString(valueString, newX - 90, arrowBarPosition - 11);
 			} else {
 				g.drawLine(newX, arrowBarPosition, newX - 20, arrowBarPosition + 15);
 				g.drawLine(newX - 20, arrowBarPosition + 15, newX - 30, arrowBarPosition + 15);
 				g.setColor(Color.black);
 				g.drawString(valueString, newX - 90, arrowBarPosition + 19);
 			}
 
 			g.setColor(Color.black);
 			g.fillOval(newX - 3, arrowBarPosition - 3, 6, 6);
 
 			p1.setLocation(p2);
 		}
 
 		p2.setLocation(track_width, 5);
 
 		g.setColor(VALUE_AREA_COLOR);
 
 		int h = (5 + trackHeight) - (int) ((above.floatValue() / max) * trackHeight);
 		g.fillRect((int) p1.getX(), h, track_width - (int) p1.getX(),
 		           (int) ((above.floatValue() / max) * trackHeight));
 		g.setColor(Color.red);
 		g.fillRect(track_width - 5, h - 5, 10, 10);
 		aboveSquare = new Point(track_width, h);
 
 		/*
 		 * Finally, draw border line (rectangle)
 		 */
 		g.setColor(BORDER_COLOR);
 		g.setStroke(new BasicStroke(1.5f));
 		g.drawRect(0, 5, track_width, trackHeight);
 
 		g.setColor(Color.red);
 		g.setStroke(new BasicStroke(1.5f));
 
 		for (Integer key : verticesList.keySet()) {
 			Point p = verticesList.get(key);
 
 			if (clickFlag) {
 				int diffX = Math.abs(p.x - (curPoint.x - 6));
 				int diffY = Math.abs(p.y - (curPoint.y - 12));
 
 				if (((diffX < 6) && (diffY < 6)) || (key == selectedIdx)) {
 					g.setColor(Color.green);
 					g.setStroke(new BasicStroke(2.5f));
 				} else {
 					g.setColor(Color.red);
 					g.setStroke(new BasicStroke(1.5f));
 				}
 			}
 
 			g.drawRect(p.x - 5, p.y - 5, 10, 10);
 		}
 
 		/*
 		 * Draw below & above
 		 */
 		g.translate(-THUMB_WIDTH / 2, -12);
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public Double getSelectedThumbValue() {
 		final float position = slider.getModel().getThumbAt(slider.getSelectedIndex()).getPosition();
 		final double thumbVal = (((position / 100) * valueRange) - Math.abs(minValue));
 
 		return thumbVal;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @param slider DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public JComponent getRendererComponent(JXMultiThumbSlider slider) {
 		this.slider = slider;
 
 		if (listener == null) {
 			listener = new CMouseListener();
 			this.slider.addMouseListener(listener);
 			this.slider.addMouseMotionListener(new CMouseMotionListener());
 		}
 
 		if (verticesList == null)
 			verticesList = new HashMap<Integer, Point>();
 
 		if (valueMap == null)
 			valueMap = new HashMap<Integer, Double>();
 
 		return this;
 	}
 
 	class CMouseMotionListener implements MouseMotionListener {
 		public void mouseDragged(MouseEvent e) {
 			/*
 			 * If user is moving thumbs, update is not necessary!
 			 */
 			if ((e.getY() < THUMB_WIDTH) && (dragFlag == false)) {
 				return;
 			}
 
 			dragFlag = true;
 
 			curPoint = e.getPoint();
 
 			/*
 			 * If beyond the bottom lin
 			 */
 			if (clickFlag == true) {
 				Thumb selectedThumb = slider.getModel().getThumbAt(selectedIdx);
 
 				if (curPoint.getY() >= (trackHeight + 5)) {
 					selectedThumb.setObject(0f);
 
 					return;
 				}
 
 				double curY = curPoint.getY();
 
 				float newY = (float) ((((trackHeight + 5) - curY) * max) / (trackHeight + 5));
 
 				selectedThumb.setObject(newY);
 
 				//updateMax();
 				Object newVal = newY;
 				cMapping.getPoint(selectedIdx).getRange().equalValue = newVal;
 
 				final BoundaryRangeValues brv = new BoundaryRangeValues(cMapping.getPoint(selectedIdx)
 				                                                                .getRange().lesserValue,
 				                                                        newVal,
 				                                                        cMapping.getPoint(selectedIdx)
 				                                                                .getRange().greaterValue);
 
 				cMapping.getPoint(selectedIdx).setRange(brv);
 
 				int numPoints = cMapping.getAllPoints().size();
 
 				// Update Values which are not accessible from
 				// UI
 				if (numPoints > 1) {
 					if (selectedIdx == 0)
 						brv.greaterValue = newVal;
 					else if (selectedIdx == (numPoints - 1))
 						brv.lesserValue = newVal;
 					else {
 						brv.lesserValue = newVal;
 						brv.greaterValue = newVal;
 					}
 				}
 			}
 
 			dragOrigin = e.getPoint();
 			slider.repaint();
 		}
 
 		public void mouseMoved(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 		}
 	}
 
 	class CMouseListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent e) {
 			/*
 			 * Show popup dialog to enter new numerical value.
 			 */
 			if (isPointerInSquare(e) && (e.getClickCount() == 2)) {
 				final String val = JOptionPane.showInputDialog(slider,
 				                                               "Please type new value for this pivot.");
 
 				if (val == null)
 					return;
 
 				Float newVal = 0f;
 
 				try {
 					newVal = Float.valueOf(val);
 				} catch (Exception ne) {
 					// Number format error.
 					return;
 				}
 
 				slider.getModel().getThumbAt(selectedIdx).setObject(newVal);
 
 				updateMax();
 
 				cMapping.getPoint(selectedIdx).getRange().equalValue = newVal;
 
 				final BoundaryRangeValues brv = new BoundaryRangeValues(cMapping.getPoint(selectedIdx)
 				                                                                .getRange().lesserValue,
 				                                                        newVal,
 				                                                        cMapping.getPoint(selectedIdx)
 				                                                                .getRange().greaterValue);
 
 				cMapping.getPoint(selectedIdx).setRange(brv);
 
 				int numPoints = cMapping.getAllPoints().size();
 
 				// Update Values which are not accessible from
 				// UI
 				if (numPoints > 1) {
 					if (selectedIdx == 0)
 						brv.greaterValue = newVal;
 					else if (selectedIdx == (numPoints - 1))
 						brv.lesserValue = newVal;
 					else {
 						brv.lesserValue = newVal;
 						brv.greaterValue = newVal;
 					}
 
 					cMapping.fireStateChanged();
 
 					Cytoscape.getVisualMappingManager().getNetworkView().redrawGraph(false, true);
 					slider.repaint();
 				}
 
 				repaint();
 				slider.repaint();
 				repaint();
 			} else if ((e.getClickCount() == 2) && (isBelow(e.getPoint()))) {
 				final String val = JOptionPane.showInputDialog(slider,
 				                                               "Please type new value for BELOW:");
 
 				if (val == null) {
 					return;
 				}
 
 				try {
 					below = Float.valueOf(val);
 				} catch (Exception ne) {
 					// Number format error.
 					return;
 				}
 
 				Object newValue = below;
 
 				BoundaryRangeValues brv;
 				BoundaryRangeValues original;
 
 				original = cMapping.getPoint(0).getRange();
 				brv = new BoundaryRangeValues(newValue, original.equalValue, original.greaterValue);
 				cMapping.getPoint(0).setRange(brv);
 
 				cMapping.fireStateChanged();
 
 				// Update view.
 				Cytoscape.getVisualMappingManager().getNetworkView().redrawGraph(false, true);
 
 				slider.repaint();
 				repaint();
 
 				firePropertyChange(ContinuousMappingEditorPanel.BELOW_VALUE_CHANGED, null, below);
 			} else if ((e.getClickCount() == 2) && (isAbove(e.getPoint()))) {
 				final String val = JOptionPane.showInputDialog(slider,
 				                                               "Please type new value for ABOVE:");
 
 				if (val == null) {
 					return;
 				}
 
 				try {
 					above = Float.valueOf(val);
 				} catch (Exception ne) {
 					// Number format error.
 					return;
 				}
 
 				BoundaryRangeValues brv;
 				BoundaryRangeValues original;
 
 				original = cMapping.getPoint(cMapping.getPointCount() - 1).getRange();
 				brv = new BoundaryRangeValues(original.lesserValue, original.equalValue, above);
 				cMapping.getPoint(cMapping.getPointCount() - 1).setRange(brv);
 
 				cMapping.fireStateChanged();
 
 				// Update view.
 				Cytoscape.getVisualMappingManager().getNetworkView().redrawGraph(false, true);
 
 				slider.repaint();
 				repaint();
 
 				firePropertyChange(ContinuousMappingEditorPanel.ABOVE_VALUE_CHANGED, null, above);
 			}
 		}
 
 		private boolean isBelow(final Point p) {
 			
 			if(belowSquare == null) {
 				return false;
 			}
 			
 			int diffY = Math.abs(p.y - 12 - belowSquare.y);
 			int diffX = Math.abs(p.x - 6 - belowSquare.x);
 
 			if ((diffX < 6) && (diffY < 6)) {
 				return true;
 			}
 
 			return false;
 		}
 
 		private boolean isAbove(final Point p) {
 			
 			if(aboveSquare == null) {
 				return false;
 			}
 			
 			int diffY = Math.abs(p.y - 12 - aboveSquare.y);
 			int diffX = Math.abs(p.x - 6 - aboveSquare.x);
 
 			if ((diffX < 6) && (diffY < 6)) {
 				return true;
 			}
 
 			return false;
 		}
 
 		public void mousePressed(MouseEvent e) {
 			curPoint = e.getPoint();
 			dragOrigin = e.getPoint();
 
 			for (Integer key : verticesList.keySet()) {
 				Point p = verticesList.get(key);
 				int diffY = Math.abs((p.y + 12) - curPoint.y);
 				int diffX = Math.abs((p.x + (THUMB_WIDTH / 2)) - curPoint.x);
 
 				if ((diffX < 6) && (diffY < 6)) {
 					selectedIdx = key;
 					clickFlag = true;
 				}
 			}
 		}
 
 		public void mouseReleased(MouseEvent arg0) {
 			clickFlag = false;
 			updateMax();
 
 			if (slider.getSelectedThumb() == null)
 				slider.repaint();
 
 			repaint();
 
 			if (dragFlag == true) {
 				dragFlag = false;
 				cMapping.fireStateChanged();
 				Cytoscape.getVisualMappingManager().getNetworkView().redrawGraph(false, true);
 			}
 		}
 
 		private boolean isPointerInSquare(MouseEvent e) {
 			curPoint = e.getPoint();
 			dragOrigin = e.getPoint();
 
 			for (Integer key : verticesList.keySet()) {
 				Point p = verticesList.get(key);
 				int diffY = Math.abs((p.y + 12) - curPoint.y);
 				int diffX = Math.abs((p.x + (THUMB_WIDTH / 2)) - curPoint.x);
 
 				if ((diffX < 6) && (diffY < 6)) {
 					selectedIdx = key;
 
 					return true;
 				}
 			}
 
 			return false;
 		}
 
 		private void updateMax() {
 			Float val;
 			Float curMax = 0f;
 
 			for (Object thumb : slider.getModel().getSortedThumbs()) {
 				val = (Float) ((Thumb) thumb).getObject();
 
 				if (val > curMax)
 					curMax = val;
 			}
 
 			max = curMax;
 		}
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param iconWidth DOCUMENT ME!
 	 * @param iconHeight DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public ImageIcon getTrackGraphicIcon(int iconWidth, int iconHeight) {
 		return drawIcon(iconWidth, iconHeight, false);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param iconWidth DOCUMENT ME!
 	 * @param iconHeight DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public ImageIcon getLegend(int iconWidth, int iconHeight) {
 		return drawIcon(iconWidth, iconHeight, true);
 	}
 
 	private ImageIcon drawIcon(int iconWidth, int iconHeight, boolean detail) {
 		if (slider == null) {
 			return null;
 		}
 
 		final BufferedImage bi = new BufferedImage(iconWidth, iconHeight, BufferedImage.TYPE_INT_RGB);
 		final Graphics2D g = bi.createGraphics();
 
 		// Turn Anti-alias on
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
 		/*
 		 * Fill background
 		 */
 		g.setColor(Color.white);
 		g.fillRect(0, 0, iconWidth, iconHeight);
 
 		int leftSpace = 10;
 		int trackHeight = iconHeight - 9;
 		int trackWidth = iconWidth - leftSpace;
 
 		/*
 		 * Compute fractions from mapping
 		 */
 		List<Thumb> stops = slider.getModel().getSortedThumbs();
 
 		int numPoints = stops.size();
 
 		float[] fractions = new float[numPoints];
 		Float[] floatProperty = new Float[numPoints];
 		int i = 0;
 
 		values.clear();
 		values.add(below.floatValue());
 		values.add(above.floatValue());
 
 		for (Thumb thumb : stops) {
 			floatProperty[i] = (Float) thumb.getObject();
 			fractions[i] = thumb.getPosition();
 			values.add((Float) thumb.getObject());
 			i++;
 		}
 
 		for (Float val : values) {
 			if (min >= val)
 				min = val;
 
 			if (max <= val)
 				max = val;
 		}
 
 		// Draw min/max
 		g.setColor(Color.DARK_GRAY);
 		g.setFont(ICON_FONT);
 
 		int minWidth = SwingUtilities.computeStringWidth(g.getFontMetrics(),
 		                                                 String.format("%.1f", min));
 		int maxWidth = SwingUtilities.computeStringWidth(g.getFontMetrics(),
 		                                                 String.format("%.1f", max));
 
 		if (detail) {
 			leftSpace = LEFT_SPACE;
 			trackHeight = iconHeight - 30;
 		} else {
 			leftSpace = Math.max(minWidth, maxWidth) + 1;
 		}
 
 		trackWidth = iconWidth - leftSpace;
 
 		g.drawString(String.format("%.1f", min), 0, trackHeight);
 		g.drawString(String.format("%.1f", max), 0, 8);
 
 		/*
 		 * If no points, just return empty rectangle.
 		 */
 		if (numPoints == 0) {
 			g.setStroke(new BasicStroke(1.0f));
 			g.setColor(Color.DARK_GRAY);
 			g.drawRect(leftSpace, 0, trackWidth - 3, trackHeight);
 
 			return new ImageIcon(bi);
 		}
 
 		g.translate(leftSpace, 0);
 		g.setStroke(new BasicStroke(1.0f));
 
 		int newX = 0;
 		int lastY = 0;
 
 		Point2D p1 = new Point2D.Float(0, 0);
 		Point2D p2 = new Point2D.Float(0, 0);
 
 		for (i = 0; i < floatProperty.length; i++) {
 			newX = (int) (trackWidth * (fractions[i] / 100))-3;
 			if(newX<0) {
 				newX = 0;
 			}
 
 			p2.setLocation(newX, 0);
 
 			int newY = trackHeight - (int) ((floatProperty[i] / max) * trackHeight);
 
 			valueArea.reset();
 
 			g.setColor(VALUE_AREA_COLOR);
 
 			if (i == 0) {
 				int h = trackHeight - (int) ((below.floatValue() / max) * trackHeight);
 				g.fillRect(0, h, newX, (int) ((below.floatValue() / max) * trackHeight));
 			} else {
 				valueArea.addPoint((int) p1.getX(), lastY);
 				valueArea.addPoint(newX, newY);
 				valueArea.addPoint(newX, trackHeight);
 				valueArea.addPoint((int) p1.getX(), trackHeight);
 				g.fill(valueArea);
 			}
 
 			for (int j = 0; j < stops.size(); j++) {
 				if (slider.getModel().getThumbAt(j).getObject() == floatProperty[i]) {
 					Point newPoint = new Point(newX, newY);
 
 					if (verticesList.containsValue(newPoint) == false)
 						verticesList.put(j, new Point(newX, newY));
 
 					break;
 				}
 			}
 
 			lastY = newY;
 			p1.setLocation(p2);
 		}
 
 		p2.setLocation(trackWidth, 0);
 
 		g.setColor(VALUE_AREA_COLOR);
 
 		int h = trackHeight - (int) ((above.floatValue() / max) * trackHeight);
 		g.fillRect((int) p1.getX(), h, trackWidth - (int) p1.getX()-3,
 		           (int) ((above.floatValue() / max) * trackHeight));
 
 		g.translate(-leftSpace, 0);
 
 		/*
 		 * Draw border line (rectangle)
 		 */
 		g.setColor(BORDER_COLOR);
 		g.setStroke(new BasicStroke(1.0f));
 		g.drawRect(leftSpace, 0, trackWidth - 3, trackHeight);
 
 		/*
 		 * Draw numbers and arrows
 		 */
 		g.setFont(new Font("SansSerif", Font.BOLD, 9));
 
 		final String minStr = String.format("%.2f", minValue);
 		final String maxStr = String.format("%.2f", maxValue);
 		int strWidth;
 		g.setColor(Color.black);
 
 		if (detail) {
 			String fNum = null;
 
 			for (int j = 0; j < fractions.length; j++) {
 				fNum = String.format("%.2f",
 				                     ((fractions[j] / 100) * valueRange) - Math.abs(minValue));
 				strWidth = SwingUtilities.computeStringWidth(g.getFontMetrics(), fNum);
 				g.drawString(fNum,
 				             ((fractions[j] / 100) * trackWidth) - (strWidth / 2) + leftSpace,
 				             iconHeight - 20);
 			}
 
 			g.drawString(minStr, leftSpace, iconHeight);
 			strWidth = SwingUtilities.computeStringWidth(g.getFontMetrics(), maxStr);
 			g.drawString(maxStr, iconWidth - strWidth - 2, iconHeight);
 
 			g.setFont(TITLE_FONT);
 
 			final int titleWidth = SwingUtilities.computeStringWidth(g.getFontMetrics(), title);
 			g.setColor(Color.black);
 			g.drawString(title, (iconWidth / 2) - (titleWidth / 2), iconHeight - 5);
 
 			Polygon p = new Polygon();
 			p.addPoint(iconWidth, iconHeight - 9);
 			p.addPoint(iconWidth - 15, iconHeight - 15);
 			p.addPoint(iconWidth - 15, iconHeight - 9);
 			g.fillPolygon(p);
 			g.drawLine(leftSpace, iconHeight - 9,
 			           ((iconWidth - leftSpace) / 2) - (titleWidth / 2) - 3, iconHeight - 9);
 			g.drawLine((iconWidth / 2) + (titleWidth / 2) + 3, iconHeight - 9, iconWidth,
 			           iconHeight - 9);
 
 			/*
 			 * Draw vertical arrow
 			 */
 			int panelHeight = iconHeight - 30;
 
 			final Polygon poly = new Polygon();
 			int top = 0;
 
 			g.setStroke(new BasicStroke(1.0f));
 
 			int center = (leftSpace / 2) + 6;
 
 			poly.addPoint(center, top);
 			poly.addPoint(center - 6, top + 15);
 			poly.addPoint(center, top + 15);
 			g.fillPolygon(poly);
 
 			g.drawLine(center, top, center, panelHeight);
 			g.setColor(Color.DARK_GRAY);
 			g.setFont(new Font("SansSerif", Font.BOLD, 10));
 
 			final String label = type.getName();
 			final int width = SwingUtilities.computeStringWidth(g.getFontMetrics(), label);
 			AffineTransform af = new AffineTransform();
 			af.rotate(Math.PI + (Math.PI / 2));
 			g.setTransform(af);
 
 			g.setColor(Color.black);
 			g.drawString(type.getName(), (-panelHeight / 2) - (width / 2), (leftSpace / 2) + 5);
 		} else {
 			g.drawString(minStr, 0, iconHeight);
 			strWidth = SwingUtilities.computeStringWidth(g.getFontMetrics(), maxStr);
 			g.drawString(maxStr, iconWidth - strWidth - 2, iconHeight);
 		}
 
 		return new ImageIcon(bi);
 	}
 }
