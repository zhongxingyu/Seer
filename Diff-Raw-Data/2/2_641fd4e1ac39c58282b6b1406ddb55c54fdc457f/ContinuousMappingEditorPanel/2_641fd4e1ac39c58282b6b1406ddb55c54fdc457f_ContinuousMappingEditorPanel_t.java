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
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.ImageIcon;
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.jdesktop.swingx.JXMultiThumbSlider;
 import org.jdesktop.swingx.multislider.Thumb;
 
 import cytoscape.Cytoscape;
 import cytoscape.data.CyAttributes;
 import cytoscape.data.attr.CountedIterator;
 import cytoscape.data.attr.MultiHashMap;
 import cytoscape.visual.VisualPropertyType;
 import cytoscape.visual.calculators.Calculator;
 import cytoscape.visual.mappings.ContinuousMapping;
 import cytoscape.visual.mappings.continuous.ContinuousMappingPoint;
 
 
 /**
  * Abstract class for all Continuous Mapping Editors.
  *
  * @version 0.5
  * @since Cytoscape 2.5
  * @author kono
   */
 public abstract class ContinuousMappingEditorPanel extends JDialog implements PropertyChangeListener {
 	// Tell vizMapper main whic editor is disabled/enabled.
 	/**
 	 * DOCUMENT ME!
 	 */
 	public static final String EDITOR_WINDOW_CLOSED = "EDITOR_WINDOW_CLOSED";
 
 	/**
 	 * DOCUMENT ME!
 	 */
 	public static final String EDITOR_WINDOW_OPENED = "EDITOR_WINDOW_OPENED";
 
 	/*
 	 * Used by trackrenderers.
 	 */
 	protected static final String BELOW_VALUE_CHANGED = "BELOW_VALUE_CHANGED";
 	protected static final String ABOVE_VALUE_CHANGED = "ABOVE_VALUE_CHANGED";
 	protected VisualPropertyType type;
 	protected Calculator calculator;
 	protected ContinuousMapping mapping;
 	protected double maxValue;
 	protected double minValue;
 	protected double valRange;
 	protected ArrayList<ContinuousMappingPoint> allPoints;
 	private SpinnerNumberModel spinnerModel;
 	protected Object below;
 	protected Object above;
 	protected static ContinuousMappingEditorPanel editor;
 
 	/** Creates new form ContinuousMapperEditorPanel */
 	public ContinuousMappingEditorPanel(final VisualPropertyType type) {
 		this.type = type;
 		initComponents();
 		setVisualPropLabel();
 
 		setAttrComboBox();
 		setSpinner();
 		this.addWindowListener(new WindowAdapter() {
 				public void windowOpened(WindowEvent e) {
 					System.out.println("windowOpened");
 					firePropertyChange(EDITOR_WINDOW_OPENED, null, type);
 				}
 
 				public void windowClosing(WindowEvent e) {
 					firePropertyChange(EDITOR_WINDOW_CLOSED, this, type);
 				}
 			});
 	}
 
 	/**
 	 *  Dynamically generate small icons from continuous mappers.
 	 *
 	 * @param width DOCUMENT ME!
 	 * @param height DOCUMENT ME!
 	 * @param type DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public static ImageIcon getIcon(final int width, final int height, VisualPropertyType type) {
 		final Class dataType = type.getDataType();
 
 		if (dataType == Color.class) {
 			return GradientEditorPanel.getIcon(width, height, type);
 		} else if (dataType == Number.class) {
 			return C2CMappingEditor.getIcon(width, height, type);
 		} else {
 			return C2DMappingEditor.getIcon(width, height, type);
 		}
 	}
 
 	protected void setSpinner() {
 		spinnerModel = new SpinnerNumberModel(0.0d, Float.NEGATIVE_INFINITY,
 		                                      Float.POSITIVE_INFINITY, 0.01d);
 		spinnerModel.addChangeListener(new SpinnerChangeListener());
 		valueSpinner.setModel(spinnerModel);
 	}
 
 	protected void setVisualPropLabel() {
 		this.visualPropertyLabel.setText("Visual Property: " + type.getName());
 	}
 
 	/**
 	 * This method is called from within the constructor to initialize the form.
 	 * WARNING: Do NOT modify this code. The content of this method is always
 	 * regenerated by the Form Editor.
 	 */
 
 	// <editor-fold defaultstate="collapsed" desc=" Generated Code ">
 	private void initComponents() {
 		JPanel mainPanel = new JPanel();
 
 		abovePanel = new BelowAndAbovePanel(type, Color.yellow, false);
 		abovePanel.setName("abovePanel");
 		belowPanel = new BelowAndAbovePanel(type, Color.white, true);
 		belowPanel.setName("belowPanel");
 
 		abovePanel.setPreferredSize(new Dimension(16, 1));
 		belowPanel.setPreferredSize(new Dimension(16, 1));
 
 		rangeSettingPanel = new javax.swing.JPanel();
 		pivotLabel = new javax.swing.JLabel();
 		addButton = new javax.swing.JButton();
 		deleteButton = new javax.swing.JButton();
 
 		colorButton = new javax.swing.JButton();
 		rangeEditorPanel = new javax.swing.JPanel();
 		slider = new org.jdesktop.swingx.JXMultiThumbSlider();
 		attrNameLabel = new javax.swing.JLabel();
 		iconPanel = new YValueLegendPanel(type);
 		visualPropertyLabel = new javax.swing.JLabel();
 
 		valueSpinner = new JSpinner();
 
 		valueSpinner.setEnabled(false);
 
 		rotaryEncoder = new JXMultiThumbSlider();
 
 		iconPanel.setPreferredSize(new Dimension(25, 1));
 
 		mainPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
 		                                                                 "Continuous Mapping for "
 		                                                                 + type.getName(),
 		                                                                 javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
 		                                                                 javax.swing.border.TitledBorder.DEFAULT_POSITION,
 		                                                                 new java.awt.Font("SansSerif",
 		                                                                                   Font.BOLD,
 		                                                                                   12),
 		                                                                 new java.awt.Color(0, 0, 0)));
 
 		rangeSettingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
 		                                                                         "Range Setting",
 		                                                                         javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
 		                                                                         javax.swing.border.TitledBorder.DEFAULT_POSITION,
 		                                                                         new java.awt.Font("SansSerif",
 		                                                                                           1,
 		                                                                                           10),
 		                                                                         new java.awt.Color(0,
 		                                                                                            0,
 		                                                                                            0)));
 		pivotLabel.setFont(new java.awt.Font("SansSerif", 1, 12));
 		pivotLabel.setForeground(java.awt.Color.darkGray);
 		pivotLabel.setText("Pivot:");
 
 		addButton.setText("Add");
 		addButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
 		addButton.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					addButtonActionPerformed(evt);
 				}
 			});
 
 		deleteButton.setText("Delete");
 		deleteButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
 		deleteButton.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					deleteButtonActionPerformed(evt);
 				}
 			});
 
 		rangeEditorPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
 		                                                                        "Range Editor",
 		                                                                        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
 		                                                                        javax.swing.border.TitledBorder.DEFAULT_POSITION,
 		                                                                        new java.awt.Font("SansSerif",
 		                                                                                          1,
 		                                                                                          10),
 		                                                                        new java.awt.Color(0,
 		                                                                                           0,
 		                                                                                           0)));
 		slider.setMaximumValue(100.0F);
 		rotaryEncoder.setMaximumValue(100.0F);
 
 		org.jdesktop.layout.GroupLayout sliderLayout = new org.jdesktop.layout.GroupLayout(slider);
 		slider.setLayout(sliderLayout);
 		sliderLayout.setHorizontalGroup(sliderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                            .add(0, 486, Short.MAX_VALUE));
 		sliderLayout.setVerticalGroup(sliderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                          .add(0, 116, Short.MAX_VALUE));
 
 		attrNameLabel.setFont(new java.awt.Font("SansSerif", 1, 14));
 		attrNameLabel.setForeground(java.awt.Color.darkGray);
 		attrNameLabel.setText("Attribute Name");
 
 		//        org.jdesktop.layout.GroupLayout iconPanelLayout = new org.jdesktop.layout.GroupLayout(iconPanel);
 		//        iconPanel.setLayout(iconPanelLayout);
 		//        iconPanelLayout.setHorizontalGroup(
 		//            iconPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		//            .add(org.jdesktop.layout.GroupLayout.TRAILING, rotaryEncoder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
 		//        );
 		//        iconPanelLayout.setVerticalGroup(
 		//            iconPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		//            .add(org.jdesktop.layout.GroupLayout.TRAILING, rotaryEncoder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
 		//        );
 		org.jdesktop.layout.GroupLayout jXMultiThumbSlider1Layout = new org.jdesktop.layout.GroupLayout(rotaryEncoder);
 		rotaryEncoder.setLayout(jXMultiThumbSlider1Layout);
 		jXMultiThumbSlider1Layout.setHorizontalGroup(jXMultiThumbSlider1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                                      .add(0, 84,
 		                                                                           Short.MAX_VALUE));
 		jXMultiThumbSlider1Layout.setVerticalGroup(jXMultiThumbSlider1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                                    .add(0, 65,
 		                                                                         Short.MAX_VALUE));
 
 		visualPropertyLabel.setFont(new java.awt.Font("SansSerif", 1, 14));
 		visualPropertyLabel.setForeground(java.awt.Color.darkGray);
 
 		org.jdesktop.layout.GroupLayout rangeSettingPanelLayout = new org.jdesktop.layout.GroupLayout(rangeSettingPanel);
 		rangeSettingPanel.setLayout(rangeSettingPanelLayout);
 		rangeSettingPanelLayout.setHorizontalGroup(rangeSettingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                                  .add(rangeSettingPanelLayout.createSequentialGroup()
 		                                                                                              .addContainerGap()
 		                                                                                              .add(valueSpinner,
 		                                                                                                   org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                                   67,
 		                                                                                                   org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                                                                              .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED,
 		                                                                                                               118,
 		                                                                                                               Short.MAX_VALUE)
 		                                                                                              .add(addButton,
 		                                                                                                   org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                                   55,
 		                                                                                                   org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                                                                              .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                              .add(deleteButton)
 		                                                                                              .add(10,
 		                                                                                                   10,
 		                                                                                                   10)));
 		rangeSettingPanelLayout.setVerticalGroup(rangeSettingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                                .add(rangeSettingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 		                                                                                            .add(valueSpinner,
 		                                                                                                 org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                                 org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                 org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                                                                            .add(deleteButton)
 		                                                                                            .add(addButton,
 		                                                                                                 org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                                 org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                 org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));
 
 		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(mainPanel);
 		mainPanel.setLayout(layout);
 
 		layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                .add(rangeSettingPanel,
 		                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                     Short.MAX_VALUE)
 		                                .add(layout.createSequentialGroup()
 		                                           .add(iconPanel,
 		                                                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                           .add(belowPanel,
 		                                                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                           .add(slider,
 		                                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                243, Short.MAX_VALUE)
 		                                           .add(abovePanel,
 		                                                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));
 		layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                              .add(layout.createSequentialGroup()
 		                                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                    .add(org.jdesktop.layout.GroupLayout.TRAILING,
 		                                                         slider,
 		                                                         org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                         145, Short.MAX_VALUE)
 		                                                    .add(org.jdesktop.layout.GroupLayout.TRAILING,
 		                                                         iconPanel,
 		                                                         org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                         org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                         Short.MAX_VALUE)
 		                                                    .add(org.jdesktop.layout.GroupLayout.TRAILING,
 		                                                         belowPanel,
 		                                                         org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                         org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                         Short.MAX_VALUE)
 		                                                    .add(org.jdesktop.layout.GroupLayout.TRAILING,
 		                                                         abovePanel,
 		                                                         org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                         org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                         Short.MAX_VALUE))
 		                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                         .add(rangeSettingPanel,
 		                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));
 
 		// add the main panel to the dialog.
 		this.getContentPane().add(mainPanel);
 		this.pack();
 	} // </editor-fold>               
 
 	abstract protected void deleteButtonActionPerformed(java.awt.event.ActionEvent evt);
 
 	abstract protected void addButtonActionPerformed(java.awt.event.ActionEvent evt);
 
 	private void setAttrComboBox() {
 		final CyAttributes attr;
 
 		if (type.isNodeProp()) {
 			attr = Cytoscape.getNodeAttributes();
 			calculator = Cytoscape.getVisualMappingManager().getVisualStyle()
 			                      .getNodeAppearanceCalculator().getCalculator(type);
 		} else {
 			attr = Cytoscape.getEdgeAttributes();
 			calculator = Cytoscape.getVisualMappingManager().getVisualStyle()
 			                      .getEdgeAppearanceCalculator().getCalculator(type);
 		}
 
 		if (calculator == null)
 			return;
 
 		// Assume this calc only returns cont. mapping.
 		if (calculator.getMapping(0).getClass() == ContinuousMapping.class) {
 			mapping = (ContinuousMapping) calculator.getMapping(0);
 
 			final String controllingAttrName = mapping.getControllingAttributeName();
 
 			final MultiHashMap mhm = attr.getMultiHashMap();
 
 			List<String> attrNames = new ArrayList<String>();
 			Collections.addAll(attrNames, attr.getAttributeNames());
 
 			if (attrNames.contains(controllingAttrName) == false) {
 				return;
 			}
 
 			final CountedIterator it = mhm.getObjectKeys(controllingAttrName);
 			Object key;
 			maxValue = Double.NEGATIVE_INFINITY;
 			minValue = Double.POSITIVE_INFINITY;
 
 			while (it.hasNext()) {
 				key = it.next();
 
 				Double val = Double.parseDouble(mhm.getAttributeValue((String) key,
 				                                                      controllingAttrName, null)
 				                                   .toString());
 
 				if (val > maxValue)
 					maxValue = val;
 
 				if (val < minValue)
 					minValue = val;
 			}
 
 			valRange = Math.abs(minValue - maxValue);
 			allPoints = mapping.getAllPoints();
 		}
 	}
 
 	protected void setSidePanelIconColor(Color below, Color above) {
 		this.abovePanel.setColor(above);
 		this.belowPanel.setColor(below);
 		repaint();
 	}
 
 	// Variables declaration - do not modify
 	protected javax.swing.JButton addButton;
 	private javax.swing.JLabel attrNameLabel;
 
 	//    private javax.swing.JComboBox attributeComboBox;
 	protected javax.swing.JButton colorButton;
 	protected javax.swing.JButton deleteButton;
 	protected javax.swing.JPanel iconPanel;
 	private javax.swing.JLabel pivotLabel;
 	private javax.swing.JPanel rangeEditorPanel;
 	private javax.swing.JPanel rangeSettingPanel;
	protected JXMultiThumbSlider slider;
 	protected javax.swing.JSpinner valueSpinner;
 	private javax.swing.JLabel visualPropertyLabel;
 	protected JXMultiThumbSlider rotaryEncoder;
 
 	/*
 	 * For Gradient panel only.
 	 */
 	protected BelowAndAbovePanel abovePanel;
 	protected BelowAndAbovePanel belowPanel;
 
 	protected int getSelectedPoint(int selectedIndex) {
 		final List<Thumb> thumbs = slider.getModel().getSortedThumbs();
 		Thumb selected = slider.getModel().getThumbAt(selectedIndex);
 		int i;
 
 		for (i = 0; i < thumbs.size(); i++) {
 			if (thumbs.get(i) == selected) {
 				return i;
 			}
 		}
 
 		return -1;
 	}
 
 	protected void updateMap() {
 		List<Thumb> thumbs = slider.getModel().getSortedThumbs();
 
 		//List<ContinuousMappingPoint> points = mapping.getAllPoints();
 		Thumb t;
 		Double newVal;
 
 		if (thumbs.size() == 1) {
 			// Special case: only one handle.
 			mapping.getPoint(0).getRange().equalValue = thumbs.get(0).getObject();
 			mapping.getPoint(0).getRange().lesserValue = below;
 			mapping.getPoint(0).getRange().greaterValue = above;
 			
 			newVal = ((thumbs.get(0).getPosition() / 100) * valRange) + minValue;
 			mapping.getPoint(0).setValue(newVal);
 			return;
 		}
 
 		for (int i = 0; i < thumbs.size(); i++) {
 			t = thumbs.get(i);
 
 			if (i == 0) {
 				mapping.getPoint(i).getRange().lesserValue = below;
 				mapping.getPoint(i).getRange().greaterValue = t.getObject();
 			} else if (i == (thumbs.size() - 1)) {
 				mapping.getPoint(i).getRange().greaterValue = above;
 				mapping.getPoint(i).getRange().lesserValue = t.getObject();
 			} else {
 				mapping.getPoint(i).getRange().lesserValue = t.getObject();
 				mapping.getPoint(i).getRange().greaterValue = t.getObject();
 			}
 
 			newVal = ((t.getPosition() / 100) * valRange) + minValue;
 			mapping.getPoint(i).setValue(newVal);
 
 			mapping.getPoint(i).getRange().equalValue = t.getObject();
 		}
 	}
 
 	// End of variables declaration
 	protected class ThumbMouseListener extends MouseAdapter {
 		public void mouseReleased(MouseEvent e) {
 			int selectedIndex = slider.getSelectedIndex();
 
 			if ((0 <= selectedIndex) && (slider.getModel().getThumbCount() > 0)) {
 				valueSpinner.setEnabled(true);
 
 				Double newVal = ((slider.getModel().getThumbAt(selectedIndex).getPosition() / 100) * valRange)
 				                + minValue;
 				valueSpinner.setValue(newVal);
 
 				updateMap();
 				
 				slider.repaint();
 				repaint();
 
 				Cytoscape.getVisualMappingManager().getNetworkView().redrawGraph(false, true);
 			} else {
 				valueSpinner.setEnabled(false);
 				valueSpinner.setValue(0);
 			}
 		}
 	}
 
 	/**
 	 * Watching spinner
 	 *
 	 * @author kono
 	 *
 	 */
 	class SpinnerChangeListener implements ChangeListener {
 		public void stateChanged(ChangeEvent e) {
 			Number newVal = spinnerModel.getNumber();
 			int selectedIndex = slider.getSelectedIndex();
 
 			if ((0 <= selectedIndex) && (slider.getModel().getThumbCount() > 1)) {
 				Double newPosition = ((newVal.floatValue() - minValue) / valRange);
 
 				slider.getModel().getThumbAt(selectedIndex)
 				      .setPosition(newPosition.floatValue() * 100);
 				slider.getSelectedThumb()
 				      .setLocation((int) ((slider.getSize().width - 12) * newPosition), 0);
 
 				updateMap();
 				Cytoscape.getVisualMappingManager().getNetworkView().redrawGraph(false, true);
 
 				slider.getSelectedThumb().repaint();
 				slider.getParent().repaint();
 				slider.repaint();
 
 				/*
 				 * Set continuous mapper value
 				 */
 			}
 		}
 	}
 }
