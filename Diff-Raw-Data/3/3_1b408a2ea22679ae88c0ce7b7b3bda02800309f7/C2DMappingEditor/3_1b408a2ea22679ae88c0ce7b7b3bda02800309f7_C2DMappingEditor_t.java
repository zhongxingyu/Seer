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
 
 import cytoscape.Cytoscape;
 
 import cytoscape.visual.VisualPropertyType;
 
 import cytoscape.visual.mappings.BoundaryRangeValues;
 import cytoscape.visual.mappings.continuous.ContinuousMappingPoint;
 
 import org.jdesktop.swingx.multislider.Thumb;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import java.beans.PropertyChangeEvent;
 
 import javax.swing.ImageIcon;
 
 
 /**
  * Continuous Mapping editor for discrete values,
  * such as Font, Shape, Label Position, etc.
  *
  * @author kono
   */
 public class C2DMappingEditor extends ContinuousMappingEditorPanel {
 	/**
 	 * Creates a new C2DMappingEditor object.
 	 *
 	 * @param type DOCUMENT ME!
 	 */
 	public C2DMappingEditor(VisualPropertyType type) {
 		super(type);
 		this.iconPanel.setVisible(false);
 		this.belowPanel.setVisible(false);
 		this.abovePanel.setVisible(false);
 		setSlider();
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param width DOCUMENT ME!
 	 * @param height DOCUMENT ME!
 	 * @param title DOCUMENT ME!
 	 * @param type DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public static Object showDialog(final int width, final int height, final String title,
 	                                VisualPropertyType type) {
 		editor = new C2DMappingEditor(type);
 		editor.setSize(new Dimension(width, height));
 		editor.setTitle(title);
 		editor.setAlwaysOnTop(true);
 		editor.setLocationRelativeTo(Cytoscape.getDesktop());
 		editor.setVisible(true);
 
 		return editor;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public static ImageIcon getIcon(final int iconWidth, final int iconHeight,
 	                                VisualPropertyType type) {
 		editor = new C2DMappingEditor(type);
 
 		if(editor.slider.getTrackRenderer() instanceof DiscreteTrackRenderer == false) {
 			return null;
 		} 
 		DiscreteTrackRenderer rend = (DiscreteTrackRenderer) editor.slider.getTrackRenderer();
 		rend.getRendererComponent(editor.slider);
 
 		return rend.getTrackGraphicIcon(iconWidth, iconHeight);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param width DOCUMENT ME!
 	 * @param height DOCUMENT ME!
 	 * @param type DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public static ImageIcon getLegend(final int width, final int height,
 	                                  final VisualPropertyType type) {
 		editor = new C2DMappingEditor(type);
 
		if(editor.slider.getTrackRenderer() instanceof DiscreteTrackRenderer == false) {
			return null;
		}
 		DiscreteTrackRenderer rend = (DiscreteTrackRenderer) editor.slider.getTrackRenderer();
 		rend.getRendererComponent(editor.slider);
 
 		return rend.getLegend(width, height);
 	}
 
 	@Override
 	protected void addButtonActionPerformed(ActionEvent evt) {
 		BoundaryRangeValues newRange;
 		Object defValue = Cytoscape.getVisualMappingManager().getVisualStyle()
 		                           .getNodeAppearanceCalculator().getDefaultAppearance().get(type);
 
 		if (mapping.getPointCount() == 0) {
 			slider.getModel().addThumb(50f, defValue);
 
 			newRange = new BoundaryRangeValues(below, defValue, above);
 			mapping.addPoint(maxValue / 2, newRange);
 			Cytoscape.getVisualMappingManager().getNetworkView().redrawGraph(false, true);
 
 			slider.repaint();
 			repaint();
 
 			return;
 		}
 
 		// Add a new thumb with default value
 		slider.getModel().addThumb(100f, defValue);
 
 		// Update continuous mapping
 		final Double newVal = maxValue;
 
 		// Pick Up first point.
 		final ContinuousMappingPoint previousPoint = mapping.getPoint(mapping.getPointCount() - 1);
 
 		final BoundaryRangeValues previousRange = previousPoint.getRange();
 		newRange = new BoundaryRangeValues(previousRange);
 
 		newRange.lesserValue = slider.getModel().getSortedThumbs()
 		                             .get(slider.getModel().getThumbCount() - 1);
 		System.out.println("EQ Val = " + newRange.lesserValue);
 		newRange.equalValue = defValue;
 		newRange.greaterValue = previousRange.greaterValue;
 		mapping.addPoint(maxValue, newRange);
 
 		updateMap();
 
 		Cytoscape.getVisualMappingManager().getNetworkView().redrawGraph(false, true);
 
 		slider.repaint();
 		repaint();
 	}
 
 	@Override
 	protected void deleteButtonActionPerformed(ActionEvent evt) {
 		final int selectedIndex = slider.getSelectedIndex();
 
 		if (0 <= selectedIndex) {
 			slider.getModel().removeThumb(selectedIndex);
 			mapping.removePoint(selectedIndex);
 			updateMap();
 			mapping.fireStateChanged();
 
 			Cytoscape.getVisualMappingManager().getNetworkView().redrawGraph(false, true);
 			repaint();
 		}
 	}
 
 	private void setSlider() {
 		Dimension dim = new Dimension(600, 100);
 		setPreferredSize(dim);
 		setSize(dim);
 		setMinimumSize(new Dimension(300, 80));
 		slider.updateUI();
 
 		slider.addMouseListener(new MouseAdapter() {
 				public void mouseClicked(MouseEvent e) {
 					int range = ((DiscreteTrackRenderer) slider.getTrackRenderer()).getRangeID(e
 					                                                                                                                                                                                       .getX(),
 					                                                                           e
 					                                                                                                                                                                                         .getY());
 
 					Object newValue = null;
 
 					if (e.getClickCount() == 2) {
 						try {
 							setAlwaysOnTop(false);
 							newValue = type.showDiscreteEditor();
 						} catch (Exception e1) {
 							e1.printStackTrace();
 						} finally {
 							setAlwaysOnTop(true);
 						}
 
 						if (newValue == null)
 							return;
 
 						if (range == 0) {
 							below = newValue;
 						} else if (range == slider.getModel().getThumbCount()) {
 							above = newValue;
 						} else {
 							((Thumb) slider.getModel().getSortedThumbs().get(range)).setObject(newValue);
 						}
 
 						updateMap();
 
 						slider.setTrackRenderer(new DiscreteTrackRenderer(type, minValue, maxValue,
 						                                                  below, above));
 						slider.repaint();
 
 						Cytoscape.getVisualMappingManager().getNetworkView().redrawGraph(false, true);
 					}
 				}
 			});
 
 		double actualRange = Math.abs(minValue - maxValue);
 
 		BoundaryRangeValues bound;
 		Float fraction;
 
 		/*
 		 * NPE?
 		 */
 		if(allPoints == null) {
 			return;
 		}
 		for (ContinuousMappingPoint point : allPoints) {
 			bound = point.getRange();
 
 			fraction = ((Number) ((point.getValue() - minValue) / actualRange)).floatValue() * 100;
 			slider.getModel().addThumb(fraction, bound.equalValue);
 		}
 
 		if (allPoints.size() != 0) {
 			below = allPoints.get(0).getRange().lesserValue;
 			above = allPoints.get(allPoints.size() - 1).getRange().greaterValue;
 		} else {
 			Object defaultVal = Cytoscape.getVisualMappingManager().getVisualStyle()
 			                             .getNodeAppearanceCalculator().getDefaultAppearance()
 			                             .get(type);
 			below = defaultVal;
 			above = defaultVal;
 		}
 
 		/*
 		 * get min and max for the value object
 		 */
 		TriangleThumbRenderer thumbRend = new TriangleThumbRenderer(slider);
 		DiscreteTrackRenderer dRend = new DiscreteTrackRenderer(type, minValue, maxValue, below,
 		                                                        above);
 
 		slider.setThumbRenderer(thumbRend);
 		slider.setTrackRenderer(dRend);
 		slider.addMouseListener(new ThumbMouseListener());
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param evt DOCUMENT ME!
 	 */
 	public void propertyChange(PropertyChangeEvent evt) {
 		// TODO Auto-generated method stub
 	}
 }
