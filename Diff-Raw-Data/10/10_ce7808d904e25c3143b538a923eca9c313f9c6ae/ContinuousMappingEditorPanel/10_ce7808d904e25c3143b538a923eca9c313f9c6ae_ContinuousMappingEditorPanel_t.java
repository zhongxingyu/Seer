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
 import cytoscape.logger.CyLogger;
 
 import cytoscape.data.CyAttributes;
 
 import cytoscape.data.attr.CountedIterator;
 import cytoscape.data.attr.MultiHashMap;
 
 import cytoscape.visual.VisualPropertyType;
 
 import cytoscape.visual.calculators.Calculator;
 
 import cytoscape.visual.mappings.BoundaryRangeValues;
 import cytoscape.visual.mappings.ContinuousMapping;
 import cytoscape.visual.mappings.continuous.ContinuousMappingPoint;
 
 import org.jdesktop.swingx.JXMultiThumbSlider;
 import org.jdesktop.swingx.multislider.Thumb;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.BufferedImage;
 
 import java.beans.PropertyChangeListener;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 
 /**
  * Abstract class for all Continuous Mapping Editors.
  *
  * @version 0.5
  * @since Cytoscape 2.5
  * @author kono
   */
 public abstract class ContinuousMappingEditorPanel extends JDialog implements PropertyChangeListener {
 	
 	private static final long serialVersionUID = -2558647616344119220L;
 
 	// Tell vizMapper main which editor is disabled/enabled.
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
 	protected ContinuousMapping savedMapping;
 	protected List<ContinuousMappingPoint> allPoints;
 	protected Object below;
 	protected Object above;
 	
 	private CyAttributes attrs;
 	
 	protected static ContinuousMappingEditorPanel editor;
 
 	protected static boolean updatePending = false;
 	protected static boolean updating = false;
 	
 	protected double lastSpinnerNumber = 0;
 
 	/** Creates new form ContinuousMapperEditorPanel */
 	public ContinuousMappingEditorPanel(final VisualPropertyType type) {
 		super(Cytoscape.getDesktop());
 		this.type = type;
 		initComponents();
 
 		initRangeValues();
 		setSpinners();
 		this.addWindowListener(new WindowAdapter() {
 				public void windowOpened(WindowEvent e) {
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
 
 	protected void setSpinners() {
 		final Class dataType = type.getDataType();
 		setSpinner();
 		if (dataType == Color.class) {
 		} else if (dataType == Number.class) {
 			setPropertySpinner();
 		} else {
 		}
 	}
 
 	protected void setSpinner() {
 		SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0.0d, Float.NEGATIVE_INFINITY,
 		                                                         Float.POSITIVE_INFINITY, 0.01d);
 		spinnerModel.addChangeListener(new SpinnerChangeListener(spinnerModel));
 		valueSpinner.setModel(spinnerModel);
 	}
 
 	protected void setPropertySpinner() {
 		SpinnerNumberModel propertySpinnerModel = new SpinnerNumberModel(0.0d, Float.NEGATIVE_INFINITY,
 		                                                                 Float.POSITIVE_INFINITY, 0.01d);
 		propertySpinnerModel.addChangeListener(new PropertySpinnerChangeListener(propertySpinnerModel));
 		propertySpinner.setModel(propertySpinnerModel);
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
 		addButton = new javax.swing.JButton();
 		deleteButton = new javax.swing.JButton();
 
 		// New in 2.6
 		minMaxButton = new javax.swing.JButton();
 
 		okButton = new javax.swing.JButton();
 		cancelButton = new javax.swing.JButton();
 
 		slider = new JXMultiThumbSlider();
 		iconPanel = new YValueLegendPanel(type);
 
 		valueSpinner = new JSpinner();
 		valueSpinner.setEnabled(false);
 		valueLabel = new JLabel("Attribute Value");
 		valueLabel.setLabelFor(valueSpinner);
 
 		Class dataType = this.type.getDataType();
 		if (dataType == Number.class) {
 			propertySpinner = new JSpinner();
 			propertySpinner.setEnabled(false);
 			propertyComponent = propertySpinner;
 			propertyLabel = new JLabel(type.getName());
 			propertyLabel.setLabelFor(propertyComponent);
 		} else if (dataType == Color.class) {
 			// We use the colorButton for both discrete and color
 			colorButton = new javax.swing.JButton("Change");
 			colorButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
 			colorButton.setEnabled(false);
 			propertyComponent = colorButton;
 			propertyLabel = new JLabel(type.getName());
 			propertyLabel.setLabelFor(propertyComponent);
 		} else {
 			propertyComponent = new JLabel();
 			propertyLabel = new JLabel("Double-click on icon to change "+type.getName());
 		}
 
 		rotaryEncoder = new JXMultiThumbSlider();
 
 		iconPanel.setPreferredSize(new Dimension(25, 1));
 
 		mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5,5,5,5));
 
 		rangeSettingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
 		                                                                         "Handle Settings",
 		                                                                         javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
 		                                                                         javax.swing.border.TitledBorder.DEFAULT_POSITION,
 		                                                                         new java.awt.Font("SansSerif", 1, 10),
 		                                                                         new java.awt.Color(0, 0, 0)));
 		addButton.setText("Add");
 		addButton.setPreferredSize(new Dimension(100, 10));
 		addButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
 		addButton.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					addButtonActionPerformed(evt);
 				}
 			});
 
 		deleteButton.setText("Delete");
 		deleteButton.setPreferredSize(new Dimension(100, 10));
 		deleteButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
 		deleteButton.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					deleteButtonActionPerformed(evt);
 				}
 			});
 
 		// New in 2.6
 		minMaxButton.setText("Min/Max");
 		minMaxButton.setPreferredSize(new Dimension(100, 10));
 		minMaxButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
 		minMaxButton.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					minMaxButtonActionPerformed(evt);
 				}
 			});
 
 		cancelButton.setText("Cancel");
 		cancelButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
 		cancelButton.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					calculator.getMappings().set(0,savedMapping);
 					Cytoscape.getVisualMappingManager().applyAppearances();
 					Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
 					firePropertyChange(EDITOR_WINDOW_CLOSED, this, type);
 					setVisible(false);
 				}
 			});
 
 		okButton.setText("OK");
 		// okButton.setPreferredSize(new Dimension(50, 30));
 		okButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
 		okButton.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					firePropertyChange(EDITOR_WINDOW_CLOSED, this, type);
 					setVisible(false);
 				}
 			});
 
 		slider.setMaximumValue(100.0F);
 		rotaryEncoder.setMaximumValue(100.0F);
 
 		org.jdesktop.layout.GroupLayout sliderLayout = new org.jdesktop.layout.GroupLayout(slider);
 		slider.setLayout(sliderLayout);
 		sliderLayout.setHorizontalGroup(sliderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                            .add(0, 486, Short.MAX_VALUE));
 		sliderLayout.setVerticalGroup(sliderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                          .add(0, 116, Short.MAX_VALUE));
 
 		org.jdesktop.layout.GroupLayout jXMultiThumbSlider1Layout = new org.jdesktop.layout.GroupLayout(rotaryEncoder);
 		rotaryEncoder.setLayout(jXMultiThumbSlider1Layout);
 		jXMultiThumbSlider1Layout.setHorizontalGroup(jXMultiThumbSlider1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                                      .add(0, 84,
 		                                                                           Short.MAX_VALUE));
 		jXMultiThumbSlider1Layout.setVerticalGroup(jXMultiThumbSlider1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                                    .add(0, 65,
 		                                                                         Short.MAX_VALUE));
 
 		org.jdesktop.layout.GroupLayout rangeSettingPanelLayout = new org.jdesktop.layout.GroupLayout(rangeSettingPanel);
 		rangeSettingPanel.setLayout(rangeSettingPanelLayout);
 		rangeSettingPanelLayout.setHorizontalGroup(rangeSettingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                                  .add(rangeSettingPanelLayout.createSequentialGroup()
 		                                                                                              .addContainerGap()
 		                                                                                              .add(valueLabel,
 		                                                                                                   org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                                   org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                                   org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                                                                              .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                              .add(valueSpinner,
 		                                                                                                   org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                                   100,
 		                                                                                                   org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                                                                              .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED,
 		                                                                                                               118,
 		                                                                                                               Short.MAX_VALUE)
 		                                                                                              .add(minMaxButton,
 		                                                                                                   org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                                   100,
 		                                                                                                   org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                                                                              .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                              .add(addButton,
 		                                                                                                   org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                                   100,
 		                                                                                                   org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                                                                              .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                              .add(deleteButton)
 		                                                                                              .add(10, 10, 10))
 		                                                                  .add(rangeSettingPanelLayout.createSequentialGroup()
 		                                                                                              .addContainerGap()
 		                                                                                              .add(propertyLabel,
 		                                                                                                   org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                                   org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                                   org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                                                                              .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                              .add(propertyComponent,
 		                                                                                                   org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                                   100,
 		                                                                                                   org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));
 
 		rangeSettingPanelLayout.setVerticalGroup(rangeSettingPanelLayout.createSequentialGroup()
 		                                                                .add(rangeSettingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 		                                                                                            .add(valueLabel)
 		                                                                                            .add(valueSpinner,
 		                                                                                                 org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                                 org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                 org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                                                                            .add(minMaxButton)
 		                                                                                            .add(deleteButton)
 		                                                                                            .add(addButton))
 		                                                                .add(rangeSettingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 		                                                                                            .add(propertyLabel)
 		                                                                                            .add(propertyComponent,
 		                                                                                                 org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                                 org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                 org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));
 
 		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(mainPanel);
 		mainPanel.setLayout(layout);
 
 		layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                .add(layout.createSequentialGroup()
 		                                     .addContainerGap()
 		                                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED,
 		                                                      118,
 		                                                      Short.MAX_VALUE)
 		                                     .add(cancelButton,
 		                                          org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                               100,
 		                                          org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                     .add(okButton,
 		                                          org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                               100,
 		                                          org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                          .add(10, 10, 10))
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
 		                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                     .add(cancelButton,
 		                                                          org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                          org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                          org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                          .add(okButton,
 		                                                          org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                          org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                          org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))));
 
 		// add the main panel to the dialog.
 		this.getContentPane().add(mainPanel);
 		this.pack();
 	} // </editor-fold>               
 
 	protected void minMaxButtonActionPerformed(ActionEvent evt) {
 		final Double[] newVal = MinMaxDialog.getMinMax(EditorValueRangeTracer.getTracer().getMin(type),
 		                                         EditorValueRangeTracer.getTracer().getMax(type), attrs, mapping.getControllingAttributeName());
 
 		if (newVal == null)
 			return;
 
 		EditorValueRangeTracer.getTracer().setMin(type, newVal[0]);
 		EditorValueRangeTracer.getTracer().setMax(type, newVal[1]);
 		updateMap();
 		this.repaint();
 		updateCytoscape();
 	}
 
 	abstract protected void deleteButtonActionPerformed(java.awt.event.ActionEvent evt);
 
 	abstract protected void addButtonActionPerformed(java.awt.event.ActionEvent evt);
 
 	private void initRangeValues() {
 
 		if (type.isNodeProp()) {
 			attrs = Cytoscape.getNodeAttributes();
 			calculator = Cytoscape.getVisualMappingManager().getVisualStyle()
 			                      .getNodeAppearanceCalculator().getCalculator(type);
 		} else {
 			attrs = Cytoscape.getEdgeAttributes();
 			calculator = Cytoscape.getVisualMappingManager().getVisualStyle()
 			                      .getEdgeAppearanceCalculator().getCalculator(type);
 		}
 
 		if (calculator == null)
 			return;
 
 		// Assume this calc only returns cont. mapping.
 		if (calculator.getMapping(0).getClass() == ContinuousMapping.class) {
 			mapping = (ContinuousMapping)calculator.getMapping(0);
 
 			savedMapping = (ContinuousMapping)(mapping.clone());
 
 			final String controllingAttrName = mapping.getControllingAttributeName();
 
 			final MultiHashMap mhm = attrs.getMultiHashMap();
 
 			List<String> attrNames = new ArrayList<String>();
 			Collections.addAll(attrNames, attrs.getAttributeNames());
 
 			if (attrNames.contains(controllingAttrName) == false)
 				return;
 
 			// Set range values
 			if (EditorValueRangeTracer.getTracer().getRange(type) == 0) {
 				final CountedIterator it = mhm.getObjectKeys(controllingAttrName);
 				double maxValue = Double.NEGATIVE_INFINITY;
 				double minValue = Double.POSITIVE_INFINITY;
 
 				while (it.hasNext()) {
 					final String id = (String)it.next();
 					final Object attrValue = attrs.getAttribute(id, controllingAttrName);
					if(attrValue == null)
						continue;
					
 					final double val;
					
 					if (attrValue.getClass() == Double.class)
 						val = (Double)attrValue;
 					else if (attrValue.getClass() == Integer.class)
 						val = (Integer)attrValue;
 					else
 						val = Double.parseDouble(attrValue.toString());
 
 					if (val > maxValue)
 						maxValue = val;
 
 					if (val < minValue)
 						minValue = val;
 				}
 				EditorValueRangeTracer.getTracer().setMax(type, maxValue);
 				EditorValueRangeTracer.getTracer().setMin(type, minValue);
 			}
 
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
 	protected javax.swing.JButton colorButton;
 	protected javax.swing.JButton deleteButton;
 	protected javax.swing.JButton okButton;
 	protected javax.swing.JButton cancelButton;
 	protected javax.swing.JPanel iconPanel;
 	private javax.swing.JPanel rangeSettingPanel;
 	protected JXMultiThumbSlider slider;
 	protected JSpinner valueSpinner;
 	private JLabel valueLabel;
 	private JLabel propertyLabel;
 	protected JSpinner propertySpinner = null;
 	protected JComponent propertyComponent;
 	protected JXMultiThumbSlider rotaryEncoder;
 	protected JButton minMaxButton;
 
 	/*
 	 * For Gradient panel only.
 	 */
 	protected BelowAndAbovePanel abovePanel;
 	protected BelowAndAbovePanel belowPanel;
 
 	protected int getSelectedPoint(int selectedIndex) {
 		final List<Thumb<?>> thumbs = slider.getModel().getSortedThumbs();
 		Thumb<?> selected = slider.getModel().getThumbAt(selectedIndex);
 		for (int i = 0; i < thumbs.size(); i++) {
 			if (thumbs.get(i) == selected) {
 				return i;
 			}
 		}
 
 		return -1;
 	}
 
 	protected void selectThumbAtPosition(float position) {
 		int selectedIndex = getThumbIndexAtPosition(position);
 		if (selectedIndex != -1) {
 			TriangleThumbRenderer renderer = (TriangleThumbRenderer)slider.getThumbRenderer();
 			renderer.setSelectedIndex(selectedIndex);
 			enableSpinner(selectedIndex);
 		}
 	}
 
 	protected int getThumbIndexAtPosition(float position) {
 		final List<Thumb<?>> thumbs = slider.getModel().getSortedThumbs();
 		for (int i = 0; i < thumbs.size(); i++) {
 			if (slider.getModel().getThumbAt(i).getPosition() == position) {
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	protected void setButtonColor(Color newColor) {
 		final int iconWidth = 10;
 		final int iconHeight = 10;
 		final BufferedImage bi = new BufferedImage(iconWidth, iconHeight, BufferedImage.TYPE_INT_RGB);
 		final Graphics2D g2 = bi.createGraphics();
 
 		/*
 		 * Fill background
 		 */
 		g2.setColor(newColor);
 		g2.fillRect(0, 0, iconWidth, iconHeight);
 
 		Icon colorIcon = new ImageIcon(bi);
 		colorButton.setIcon(colorIcon);
 		colorButton.setIconTextGap(6);
 	
 	}
 
 	protected void updateCytoscape() {
 		// TODO: We only want to do this for relatively small graphs....
 		// Start a thread???
 		updatePending = true;
 		if (!updating) {
 			UpdaterThread u = new UpdaterThread();
 			new Thread(u).start();
 		}
 	}
 
 	protected void updateMap() {
 		final List<Thumb> thumbs = slider.getModel().getSortedThumbs();
 
 		final double min = EditorValueRangeTracer.getTracer().getMin(type);
 		final double range = EditorValueRangeTracer.getTracer().getRange(type);
 		
 		Thumb t;
 		Double newVal;
 
 		if (thumbs.size() == 1) {
 			// Special case: only one handle.
 			mapping.getPoint(0).getRange().equalValue = thumbs.get(0).getObject();
 			mapping.getPoint(0).getRange().lesserValue = below;
 			mapping.getPoint(0).getRange().greaterValue = above;
 
 			newVal = ((thumbs.get(0).getPosition() / 100) * range) + min;
 			mapping.getPoint(0).setValue(newVal);
 
 			return;
 		}
 
 		BoundaryRangeValues rg;
 		int size = thumbs.size();
 		for (int i = 0; i < size; i++) {
 			t = thumbs.get(i);
 			rg = mapping.getPoint(i).getRange();
 			if (i == 0) {
 				rg.lesserValue = below;
 				rg.greaterValue = t.getObject();
 			} else if (i == (thumbs.size() - 1)) {
 				rg.greaterValue = above;
 				rg.lesserValue = t.getObject();
 			} else {
 				rg.lesserValue = t.getObject();
 				rg.greaterValue = t.getObject();
 			}
 
 			newVal = ((t.getPosition() / 100) * range) + min;
 			mapping.getPoint(i).setValue(newVal);
 			rg.equalValue = t.getObject();
 		}
 	}
 
 	protected void enableSpinner(int selectedIndex) {
 		final Class<?> dataType = type.getDataType();
 		valueSpinner.setEnabled(true);
 		
 		final int count = slider.getModel().getThumbCount();
 //		for(int i=0; i<count; i++) {
 //			final Thumb<?> thumb = slider.getModel().getThumbAt(i);
 //			System.out.println(i + ": " + thumb.getPosition() + " = " + thumb.getObject());
 //		}
 		
 		final Thumb<?> selectedThumb = slider.getModel().getThumbAt(selectedIndex);
 		final Double newVal = ((selectedThumb.getPosition() / 100) * EditorValueRangeTracer.getTracer().getRange(type))
 				            + EditorValueRangeTracer.getTracer().getMin(type);
 		
 		valueSpinner.setValue(newVal);
 		updateMap();
 
 		if (Number.class.isAssignableFrom(dataType)) {
 			propertySpinner.setEnabled(true);
 			final Number value = (Number) selectedThumb.getObject();
 			propertySpinner.setValue(value);
 		} else if (dataType == Color.class) {
 			colorButton.setEnabled(true);
 			setButtonColor((Color)selectedThumb.getObject());
 		}
 	}
 
 	protected void disableSpinner() {
 		Class dataType = type.getClass();
 		valueSpinner.setEnabled(false);
 		valueSpinner.setValue(0);
 		if (dataType == Number.class) {
 			propertySpinner.setEnabled(false);
 			propertySpinner.setValue(0);
 		}
 	}
 
 	// End of variables declaration
 	protected class ThumbMouseListener extends MouseAdapter {
 		public void mouseReleased(MouseEvent e) {
 			int selectedIndex = ((TriangleThumbRenderer)slider.getThumbRenderer()).getSelectedIndex();
 
 			if ((0 <= selectedIndex) && (slider.getModel().getThumbCount() > 0)) {
 				enableSpinner(selectedIndex);
 				slider.repaint();
 				repaint();
 				updateCytoscape();
 			} else {
 				disableSpinner();
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
 		SpinnerNumberModel spinnerModel;
 		public SpinnerChangeListener(SpinnerNumberModel model) {
 			this.spinnerModel = model;
 		}
 
 		public void stateChanged(ChangeEvent e) {
 			
 			final Number newVal = spinnerModel.getNumber();
 			final int selectedIndex = ((TriangleThumbRenderer)slider.getThumbRenderer()).getSelectedIndex();
 
 			if ((0 <= selectedIndex) && (slider.getModel().getThumbCount() >= 1)) {
 				
 				if ((newVal.doubleValue() < EditorValueRangeTracer.getTracer().getMin(type))
 				    || (newVal.doubleValue() > EditorValueRangeTracer.getTracer().getMax(type))) {
 										
 					if ((lastSpinnerNumber > EditorValueRangeTracer.getTracer().getMin(type))
 					    && (lastSpinnerNumber < EditorValueRangeTracer.getTracer().getMax(type))) {
 						spinnerModel.setValue(lastSpinnerNumber);
 					} else {
 						spinnerModel.setValue(0);
 					}
 
 					return;
 				}
 
 				final Double newPosition = ((newVal.floatValue()
 				                      - EditorValueRangeTracer.getTracer().getMin(type)) / EditorValueRangeTracer.getTracer()
 				                                                                                                 .getRange(type));
 				
 				JComponent selectedThumb = ((TriangleThumbRenderer)slider.getThumbRenderer()).getSelectedThumb();
 				slider.getModel().getThumbAt(selectedIndex)
 				      .setPosition(newPosition.floatValue() * 100);
 
 				selectedThumb.setLocation((int) ((slider.getSize().width - 12) * newPosition), 0);
 
 				updateMap();
 
 				selectedThumb.repaint();
 				slider.getParent().repaint();
 				slider.repaint();
 
 				updateCytoscape();
 
 				lastSpinnerNumber = newVal.doubleValue();
 			}
 		}
 	}
 
 	class PropertySpinnerChangeListener implements ChangeListener {
 		SpinnerNumberModel spinnerModel;
 		public PropertySpinnerChangeListener(SpinnerNumberModel model) {
 			this.spinnerModel = model;
 		}
 
 		public void stateChanged(ChangeEvent e) {
 			final Float newVal = new Float(spinnerModel.getNumber().doubleValue());
 			final int selectedIndex = ((TriangleThumbRenderer)slider.getThumbRenderer()).getSelectedIndex();
 			ContinuousTrackRenderer trackRenderer = (ContinuousTrackRenderer)slider.getTrackRenderer();
 			trackRenderer.setSelectedIndex(selectedIndex);
 
 			trackRenderer.setSelectedValue(newVal);
 
 			slider.repaint();
 			repaint();
 			updateCytoscape();
 
 		}
 	}
 
 	class UpdaterThread implements Runnable {
 		public UpdaterThread() {
 		}
 
 		public void run() {
 			updating = true;
 			while (updatePending) {
 				updatePending = false;
 				Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
 			}
 			updating = false;
 		}
 	}
 }
