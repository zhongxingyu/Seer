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
 package cytoscape.visual.ui;
 
 import com.l2fprod.common.propertysheet.DefaultProperty;
 import com.l2fprod.common.propertysheet.Property;
 import com.l2fprod.common.propertysheet.PropertyEditorRegistry;
 import com.l2fprod.common.propertysheet.PropertyRendererRegistry;
 import com.l2fprod.common.propertysheet.PropertySheetPanel;
 import com.l2fprod.common.propertysheet.PropertySheetTable;
 import com.l2fprod.common.propertysheet.PropertySheetTableModel.Item;
 import com.l2fprod.common.swing.plaf.blue.BlueishButtonUI;
 
 import cytoscape.CyEdge;
 import cytoscape.CyNetwork;
 import cytoscape.CyNetworkEvent;
 import cytoscape.CyNetworkListener;
 import cytoscape.CyNode;
 import cytoscape.Cytoscape;
 
 import cytoscape.data.CyAttributes;
 import cytoscape.data.CyAttributesUtils;
 import cytoscape.data.Semantics;
 
 import cytoscape.util.SwingWorker;
 
 import cytoscape.util.swing.DropDownMenuButton;
 
 import cytoscape.view.CyNetworkView;
 import cytoscape.view.CytoscapeDesktop;
 import cytoscape.view.NetworkPanel;
 
 import cytoscape.visual.ArrowShape;
 import cytoscape.visual.CalculatorCatalog;
 import cytoscape.visual.EdgeAppearanceCalculator;
 import cytoscape.visual.LineTypeDef;
 import cytoscape.visual.NodeAppearanceCalculator;
 import cytoscape.visual.NodeShape;
 import cytoscape.visual.VisualMappingManager;
 import cytoscape.visual.VisualPropertyType;
 import cytoscape.visual.VisualStyle;
 
 import cytoscape.visual.calculators.Calculator;
 import cytoscape.visual.calculators.CalculatorFactory;
 
 import cytoscape.visual.mappings.ContinuousMapping;
 import cytoscape.visual.mappings.DiscreteMapping;
 import cytoscape.visual.mappings.ObjectMapping;
 import cytoscape.visual.mappings.PassThroughMapping;
 
 import cytoscape.visual.ui.editors.continuous.ContinuousMappingEditorPanel;
 import cytoscape.visual.ui.editors.continuous.ContinuousTrackRenderer;
 import cytoscape.visual.ui.editors.continuous.CyGradientTrackRenderer;
 import cytoscape.visual.ui.editors.continuous.DiscreteTrackRenderer;
 import cytoscape.visual.ui.editors.continuous.GradientEditorPanel;
 import cytoscape.visual.ui.editors.discrete.CyColorCellRenderer;
 import cytoscape.visual.ui.editors.discrete.CyColorPropertyEditor;
 import cytoscape.visual.ui.editors.discrete.CyComboBoxPropertyEditor;
 import cytoscape.visual.ui.editors.discrete.CyDoublePropertyEditor;
 import cytoscape.visual.ui.editors.discrete.CyFontPropertyEditor;
 import cytoscape.visual.ui.editors.discrete.CyStringPropertyEditor;
 import cytoscape.visual.ui.editors.discrete.FontCellRenderer;
 import cytoscape.visual.ui.editors.discrete.ShapeCellRenderer;
 import cytoscape.visual.ui.icon.NodeIcon;
 import cytoscape.visual.ui.icon.VisualPropertyIcon;
 
 import ding.view.DGraphView;
 import ding.view.InnerCanvas;
 
 import giny.model.GraphObject;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.awt.image.VolatileImage;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyEditor;
 
 import java.lang.reflect.Constructor;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.swing.AbstractAction;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JToggleButton;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.TableColumnModelEvent;
 import javax.swing.event.TableColumnModelListener;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableCellRenderer;
 
 
 /**
  * New VizMapper UI main panel.
  *
  * This panel consists of 3 panels:
  * <ul>
  * <li>Global Control Panel
  * <li>Default editor panel
  * <li>Visual Mapping Browser
  * </ul>
  *
  * @version 0.5
  * @since Cytoscape 2.5
  * @author kono
  * @param <syncronized>
  */
 public class VizMapperMainPanel extends JPanel implements PropertyChangeListener, CyNetworkListener {
 	public enum DefaultEditor {
 		NODE,
 		EDGE,
 		GLOBAL;
 	}
 
 	private static JPopupMenu menu;
 	private static JMenuItem add;
 	private static JMenuItem delete;
 	private static JMenuItem randomize;
 	private static JMenuItem editAll;
 	private static JPopupMenu optionMenu;
 	private static JMenuItem newVS;
 	private static JMenuItem renameVS;
 	private static JMenuItem deleteVS;
 	private static JMenuItem duplicateVS;
 	private static JMenuItem createLegend;
 
 	/*
 	 * Icons used in this panel.
 	 */
 	private static final ImageIcon optionIcon = new ImageIcon(Cytoscape.class.getResource("/cytoscape/images/ximian/stock_form-properties.png"));
 	private static final ImageIcon delIcon = new ImageIcon(Cytoscape.class.getResource("/cytoscape/images/ximian/stock_delete-16.png"));
 	private static final ImageIcon addIcon = new ImageIcon(Cytoscape.class.getResource("/cytoscape/images/ximian/stock_data-new-table-16.png"));
 	private static final ImageIcon rndIcon = new ImageIcon(Cytoscape.class.getResource("/cytoscape/images/ximian/stock_filters-16.png"));
 	private static final ImageIcon renameIcon = new ImageIcon(Cytoscape.class.getResource("/cytoscape/images/ximian/stock_redo-16.png"));
 	private static final ImageIcon duplicateIcon = new ImageIcon(Cytoscape.class.getResource("/cytoscape/images/ximian/stock_slide-duplicate.png"));
 	private static final ImageIcon legendIcon = new ImageIcon(Cytoscape.class.getResource("/cytoscape/images/ximian/stock_graphic-styles-16.png"));
 	private static final ImageIcon editIcon = new ImageIcon(Cytoscape.class.getResource("/cytoscape/images/ximian/stock_edit-16.png"));
 	private static final String DEFAULT_VS_NAME = "default";
 
 	/*
 	 * This is a singleton.
 	 */
 	private static VizMapperMainPanel panel;
 
 	static {
 		/*
 		 * Make dummy network nodes & edges
 		 */
 		final CyNode source = Cytoscape.getCyNode("Source", true);
 		final CyNode target = Cytoscape.getCyNode("Target", true);
 		final CyEdge edge = Cytoscape.getCyEdge(source, target, Semantics.INTERACTION,
 		                                        "interaction", true, true);
 	}
 
 	/*
 	 * Visual mapping manager. All parameters should be taken from here.
 	 */
 	private VisualMappingManager vmm;
 
 	/*
 	 * Keeps 0properties in the browser.
 	 */
 	private Map<String, List<Property>> propertyMap;
 	private String lastVSName = null;
 	private JScrollPane noMapListScrollPane;
 	private List<VisualPropertyType> noMapping;
 	private JPanel buttonPanel;
 	private JButton addButton;
 	private JButton deleteButton;
 	private JPanel bottomPanel;
 	private Map<VisualPropertyType, JDialog> editorWindowManager = new HashMap<VisualPropertyType, JDialog>();
 	private Map<String, Image> defaultImageManager = new HashMap<String, Image>();
 
 	/** Creates new form AttributeOrientedPanel */
 	private VizMapperMainPanel() {
 		vmm = Cytoscape.getVisualMappingManager();
 
 		propertyMap = new HashMap<String, List<Property>>();
 		setMenu();
 
 		Cytoscape.getSwingPropertyChangeSupport().addPropertyChangeListener(this);
 
 		initComponents();
 		registerCellEditorListeners();
 	}
 
 	/*
 	 * Register listeners for editors.
 	 */
 	private void registerCellEditorListeners() {
 		nodeAttrEditor.addPropertyChangeListener(this);
 		edgeAttrEditor.addPropertyChangeListener(this);
 
 		colorCellEditor.addPropertyChangeListener(this);
 		mappingTypeEditor.addPropertyChangeListener(this);
 		fontCellEditor.addPropertyChangeListener(this);
 		numberCellEditor.addPropertyChangeListener(this);
 		shapeCellEditor.addPropertyChangeListener(this);
 		stringCellEditor.addPropertyChangeListener(this);
 	}
 
 	/**
 	 * Get an instance of VizMapper UI panel. This is a singleton.
 	 *
 	 * @return
 	 */
 	public static VizMapperMainPanel getVizMapperUI() {
 		if (panel == null)
 			panel = new VizMapperMainPanel();
 
 		return panel;
 	}
 
 	/**
 	 * Setup menu items.
 	 *
 	 */
 	private void setMenu() {
 		/*
 		 * Option Menu
 		 */
 		newVS = new JMenuItem("Create new Visual Style...");
 		newVS.setIcon(addIcon);
 		newVS.addActionListener(new NewStyleListener());
 
 		deleteVS = new JMenuItem("Delete Visual Style...");
 		deleteVS.setIcon(delIcon);
 		deleteVS.addActionListener(new RemoveStyleListener());
 
 		renameVS = new JMenuItem("Rename Visual Style...");
 		renameVS.setIcon(renameIcon);
 		renameVS.addActionListener(new RenameStyleListener());
 
 		duplicateVS = new JMenuItem("Copy existing Visual Style...");
 		duplicateVS.setIcon(duplicateIcon);
 		duplicateVS.addActionListener(new CopyStyleListener());
 
 		createLegend = new JMenuItem("Create legend from current Visual Style");
 		createLegend.setIcon(legendIcon);
 		createLegend.addActionListener(new CreateLegendListener());
 		optionMenu = new JPopupMenu();
 		optionMenu.add(newVS);
 		optionMenu.add(deleteVS);
 		optionMenu.add(renameVS);
 		optionMenu.add(duplicateVS);
 		optionMenu.add(createLegend);
 
 		/*
 		 * Build right-click menu
 		 */
 		add = new JMenuItem("Add new mapping");
 		delete = new JMenuItem("Delete mapping");
 		randomize = new JMenuItem("Set randomized values (Discrete Only)");
 		editAll = new JMenuItem("Edit selected values at once...");
 
 		add.setIcon(addIcon);
 		delete.setIcon(delIcon);
 		randomize.setIcon(rndIcon);
 		editAll.setIcon(editIcon);
 
 		delete.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					removeMapping();
 				}
 			});
 		editAll.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 					editSelectedCells();
 				}
 			});
 		// add.addActionListener(l)
 		// select.setIcon(vmIcon);
 		menu = new JPopupMenu();
 		menu.add(add);
 		menu.add(delete);
 		menu.add(new JSeparator());
 		menu.add(randomize);
 		menu.add(editAll);
 	}
 
 	public static void apply(Object newValue, VisualPropertyType type) {
 		if (newValue != null)
 			type.setDefault(Cytoscape.getVisualMappingManager().getVisualStyle(), newValue);
 	}
 
 	public static Object showValueSelectDialog(VisualPropertyType type, Component caller)
 	    throws Exception {
 		return type.showDiscreteEditor();
 	}
 
 	/**
 	 * This method is called from within the constructor to initialize the form.
 	 * WARNING: Do NOT modify this code. The content of this method is always
 	 * regenerated by the Form Editor.
 	 */
 
 	// <editor-fold defaultstate="collapsed" desc=" Generated Code ">
 	private void initComponents() {
 		mainSplitPane = new javax.swing.JSplitPane();
 		listSplitPane = new javax.swing.JSplitPane();
 
 		bottomPanel = new javax.swing.JPanel();
 
 		defaultAppearencePanel = new javax.swing.JPanel();
 		visualPropertySheetPanel = new PropertySheetPanel();
 		visualPropertySheetPanel.setTable(new PropertySheetTable());
 
 		vsSelectPanel = new javax.swing.JPanel();
 		vsNameComboBox = new javax.swing.JComboBox();
 
 		// optionButton = new javax.swing.JButton();
 		buttonPanel = new javax.swing.JPanel();
 
 		GridBagLayout gridbag = new GridBagLayout();
 		GridBagConstraints constraints = new GridBagConstraints();
 		buttonPanel.setLayout(gridbag);
 		constraints.gridx = 0;
 		constraints.gridy = 0;
 		constraints.gridwidth = 1;
 		constraints.gridheight = GridBagConstraints.REMAINDER;
 
 		addButton = new javax.swing.JButton();
 		deleteButton = new javax.swing.JButton();
 
 		addButton.setIcon(new javax.swing.ImageIcon("/cellar/users/kono/docs/cytoscape25Mock/images/ximian/stock_up-16.png"));
 		addButton.setText("Add");
 		addButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
 
 		addButton.setPreferredSize(new java.awt.Dimension(70, 20));
 
 		deleteButton.setIcon(new javax.swing.ImageIcon("/cellar/users/kono/docs/cytoscape25Mock/images/ximian/stock_down-16.png"));
 		deleteButton.setText("Delete");
 		deleteButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
 		deleteButton.setPreferredSize(new java.awt.Dimension(70, 20));
 
 		addButton.setUI(new BlueishButtonUI());
 		deleteButton.setUI(new BlueishButtonUI());
 
 		gridbag.setConstraints(addButton, constraints);
 		buttonPanel.add(addButton);
 
 		constraints.gridx = 2;
 		constraints.gridy = 0;
 		gridbag.setConstraints(deleteButton, constraints);
 		buttonPanel.add(deleteButton);
 
 		defaultAppearencePanel.setMinimumSize(new Dimension(100, 100));
 		defaultAppearencePanel.setPreferredSize(new Dimension(mainSplitPane.getWidth(),
 		                                                      this.mainSplitPane.getDividerLocation()));
 		defaultAppearencePanel.setSize(defaultAppearencePanel.getPreferredSize());
 		//defaultAppearencePanel.addMouseListener(new DefaultMouseListener());
 		mainSplitPane.setDividerLocation(120);
 		mainSplitPane.setDividerSize(4);
 		listSplitPane.setDividerLocation(400);
 		listSplitPane.setDividerSize(5);
 		listSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
 
 		noMapListScrollPane = new javax.swing.JScrollPane();
 		noMapListScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
 		                                                                           "Unused Visual Properties",
 		                                                                           javax.swing.border.TitledBorder.CENTER,
 		                                                                           javax.swing.border.TitledBorder.DEFAULT_POSITION,
 		                                                                           new java.awt.Font("SansSerif",
 		                                                                                             1,
 		                                                                                             12)));
 		noMapListScrollPane.setToolTipText("To Create New Mapping, Drag & Drop List Item to Browser.");
 
 		org.jdesktop.layout.GroupLayout bottomPanelLayout = new org.jdesktop.layout.GroupLayout(bottomPanel);
 		bottomPanel.setLayout(bottomPanelLayout);
 		bottomPanelLayout.setHorizontalGroup(bottomPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                      .add(noMapListScrollPane,
 		                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                           272, Short.MAX_VALUE)
 		                                                      .add(buttonPanel,
 		                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                           Short.MAX_VALUE));
 		bottomPanelLayout.setVerticalGroup(bottomPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                    .add(bottomPanelLayout.createSequentialGroup()
 		                                                                          .add(buttonPanel,
 		                                                                               org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                               25,
 		                                                                               org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                                                          .add(noMapListScrollPane,
 		                                                                               org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                               135,
 		                                                                               Short.MAX_VALUE)));
 
 		listSplitPane.setLeftComponent(mainSplitPane);
 		listSplitPane.setRightComponent(bottomPanel);
 
 		mainSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
 		defaultAppearencePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
 		                                                                              "Defaults",
 		                                                                              javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
 		                                                                              javax.swing.border.TitledBorder.DEFAULT_POSITION,
 		                                                                              new java.awt.Font("SansSerif",
 		                                                                                                1,
 		                                                                                                12),
 		                                                                              java.awt.Color.darkGray));
 		// defaultTabbedPane
 		// .setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
 		// defaultTabbedPane.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
 		mainSplitPane.setLeftComponent(defaultAppearencePanel);
 
 		visualPropertySheetPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
 		                                                                                "Visual Mapping Browser",
 		                                                                                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
 		                                                                                javax.swing.border.TitledBorder.DEFAULT_POSITION,
 		                                                                                new java.awt.Font("SansSerif",
 		                                                                                                  1,
 		                                                                                                  12),
 		                                                                                java.awt.Color.darkGray));
 
 		mainSplitPane.setRightComponent(visualPropertySheetPanel);
 
 		vsSelectPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
 		                                                                     "Current Visual Style",
 		                                                                     javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
 		                                                                     javax.swing.border.TitledBorder.DEFAULT_POSITION,
 		                                                                     new java.awt.Font("SansSerif",
 		                                                                                       1, 12),
 		                                                                     java.awt.Color.darkGray));
 
 		vsNameComboBox.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					vsNameComboBoxActionPerformed(evt);
 				}
 			});
 
 		optionButton = new DropDownMenuButton(new AbstractAction() {
 				public void actionPerformed(ActionEvent ae) {
 					DropDownMenuButton b = (DropDownMenuButton) ae.getSource();
 					optionMenu.show(b, 0, b.getHeight());
 				}
 			});
 
 		optionButton.setToolTipText("Options...");
 		optionButton.setIcon(optionIcon);
 		optionButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
 		optionButton.setComponentPopupMenu(optionMenu);
 
 		org.jdesktop.layout.GroupLayout vsSelectPanelLayout = new org.jdesktop.layout.GroupLayout(vsSelectPanel);
 		vsSelectPanel.setLayout(vsSelectPanelLayout);
 		vsSelectPanelLayout.setHorizontalGroup(vsSelectPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                          .add(vsSelectPanelLayout.createSequentialGroup()
 		                                                                                  .addContainerGap()
 		                                                                                  .add(vsNameComboBox,
 		                                                                                       0,
 		                                                                                       146,
 		                                                                                       Short.MAX_VALUE)
 		                                                                                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                  .add(optionButton,
 		                                                                                       org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                       64,
 		                                                                                       org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                                                                  .addContainerGap()));
 		vsSelectPanelLayout.setVerticalGroup(vsSelectPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                        .add(vsSelectPanelLayout.createSequentialGroup()
 		                                                                                .add(vsSelectPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 		                                                                                                        .add(vsNameComboBox,
 		                                                                                                             org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                                             org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                             org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                                                                                        .add(optionButton)) // .addContainerGap(
 		                                                                                                                            // org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                                            // Short.MAX_VALUE)
 		));
 
 		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
 		this.setLayout(layout);
 		layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                .add(vsSelectPanel,
 		                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                     Short.MAX_VALUE)
 		                                .add(mainSplitPane,
 		                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 280,
 		                                     Short.MAX_VALUE));
 		layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                              .add(layout.createSequentialGroup()
 		                                         .add(vsSelectPanel,
 		                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                         .add(mainSplitPane,
 		                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                              510, Short.MAX_VALUE)));
 	} // </editor-fold>
 
 	// Variables declaration - do not modify
 	private JPanel defaultAppearencePanel;
 	private javax.swing.JSplitPane mainSplitPane;
 	private javax.swing.JSplitPane listSplitPane;
 	private DropDownMenuButton optionButton;
 	private PropertySheetPanel visualPropertySheetPanel;
 	private javax.swing.JComboBox vsNameComboBox;
 	private javax.swing.JPanel vsSelectPanel;
 
 	/*
 	 * Renderer and Editors for the cells
 	 */
 
 	// For general values (string & number)
 	private DefaultTableCellRenderer defCellRenderer = new DefaultTableCellRenderer();
 
 	// For String values
 	private CyStringPropertyEditor stringCellEditor = new CyStringPropertyEditor();
 
 	// For colors
 	private CyColorCellRenderer collorCellRenderer = new CyColorCellRenderer();
 	private CyColorPropertyEditor colorCellEditor = new CyColorPropertyEditor();
 
 	// For shapes
 	private ShapeCellRenderer shapeCellRenderer = new ShapeCellRenderer(VisualPropertyType.NODE_SHAPE);
 	private CyComboBoxPropertyEditor shapeCellEditor = new CyComboBoxPropertyEditor();
 
 	// For Lines
 	private ShapeCellRenderer lineCellRenderer = new ShapeCellRenderer(VisualPropertyType.EDGE_LINETYPE);
 	private CyComboBoxPropertyEditor lineCellEditor = new CyComboBoxPropertyEditor();
 
 	// For sizes
 	private CyDoublePropertyEditor numberCellEditor = new CyDoublePropertyEditor();
 
 	// For font faces
 	private CyFontPropertyEditor fontCellEditor = new CyFontPropertyEditor();
 	private FontCellRenderer fontCellRenderer = new FontCellRenderer();
 	private DefaultTableCellRenderer emptyBoxRenderer = new DefaultTableCellRenderer();
 	private DefaultTableCellRenderer filledBoxRenderer = new DefaultTableCellRenderer();
 	private DefaultTableCellRenderer gradientRenderer = new DefaultTableCellRenderer();
 	private DefaultTableCellRenderer continuousRenderer = new DefaultTableCellRenderer();
 	private DefaultTableCellRenderer discreteRenderer = new DefaultTableCellRenderer();
 	private CyComboBoxPropertyEditor nodeAttrEditor = new CyComboBoxPropertyEditor();
 	private CyComboBoxPropertyEditor edgeAttrEditor = new CyComboBoxPropertyEditor();
 	private CyComboBoxPropertyEditor mappingTypeEditor = new CyComboBoxPropertyEditor();
 	private static final Map<Object, Icon> nodeShapeIcons = NodeShape.getIconSet();
 	private static final Map<Object, Icon> arrowShapeIcons = ArrowShape.getIconSet();
 	private static final Map<Object, Icon> lineTypeIcons = LineTypeDef.getIconSet();
 	private PropertyRendererRegistry pr = new PropertyRendererRegistry();
 	private PropertyEditorRegistry regr = new PropertyEditorRegistry();
 
 	// End of variables declaration
 	private void vsNameComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
 		final String vsName = (String) vsNameComboBox.getSelectedItem();
 
 		if (vsName != null) {
 			switchVS(vsName);
 		}
 	}
 
 	private void switchVS(String vsName) {
 		// If new VS name is the same, ignore.
 		if (lastVSName == vsName) {
 			return;
 		}
 
 		vmm.setVisualStyle(vsName);
 
 		if (propertyMap.containsKey(vsName) && (vsName.equals(lastVSName) == false)) {
 			List<Property> props = propertyMap.get(vsName);
 
 			for (Property item : visualPropertySheetPanel.getProperties())
 				visualPropertySheetPanel.removeProperty(item);
 
 			for (Property prop : props)
 				visualPropertySheetPanel.addProperty(prop);
 		} else {
 			setPropertyTable();
 		}
 
 		lastVSName = vsName;
 		vmm.getNetworkView().setVisualStyle(vsName);
 		vmm.getNetworkView().redrawGraph(false, true);
 
 		/*
 		 * Draw default view
 		 */
 		setDefaultPanel(defaultImageManager.get(vsName));
 		Cytoscape.getDesktop().repaint();
 	}
 
 	private static final String CATEGORY_NODE = "Node Attributes";
 	private static final String CATEGORY_EDGE = "Edge Attributes";
 	private static final String CATEGORY_UNUSED = "Unused Properties";
 	private static final String GRAPHICAL_MAP_VIEW = "Graphical View";
 	private static final String NODE_VISUAL_MAPPING = "Node Visual Mapping";
 	private static final String EDGE_VISUAL_MAPPING = "Edge Visual Mapping";
 	private static final int VS_ORIENTED = 1;
 	private static final int ATTR_ORIENTED = 2;
 	private JButton addMappingButton;
 
 	/*
 	 * Set Visual Style selector combo box.
 	 */
 	private void setVSSelector() {
 		Set<String> vsNames = vmm.getCalculatorCatalog().getVisualStyleNames();
 
 		vsNameComboBox.removeAllItems();
 
 		JPanel defPanel;
 
 		final Dimension panelSize = defaultAppearencePanel.getSize();
 		DGraphView view;
 
 		CyNetworkView oldView = vmm.getNetworkView();
 		for (String name : vsNames) {
 			vsNameComboBox.addItem(name);
 
 			defPanel = DefaultAppearenceBuilder.getDefaultView(name);
 			view = (DGraphView) ((DefaultViewPanel) defPanel).getView();
 
 			if (view != null) {
 				System.out.println("Creating Default Image for " + name);
 				createDefaultImage(name, view, panelSize);
 			}
 		}
 
 		vmm.setNetworkView(oldView);
 
 //		Cytoscape.destroyNetwork(dummyNet);
 	}
 
 	/**
 	 * Create image of a default dummy network and save in a Map object.
 	 *
 	 * @param vsName
 	 * @param view
 	 * @param size
 	 */
 	private void createDefaultImage(String vsName, DGraphView view, Dimension size) {
 		/*
 		 * Adjust image size
 		 */
 		view.getCanvas().setSize((int) size.getWidth() - 10, (int) size.getHeight() - 10);
 		view.fitContent();
 		view.setZoom(view.getZoom() * 0.9);
 		
 		defaultAppearencePanel.setLayout(new BorderLayout());
 		final InnerCanvas canvas = view.getCanvas();
 		canvas.setLocation(5, 5);
 		
 		final Dimension imageSize = canvas.getSize();
 		final Image image = new BufferedImage(imageSize.width, imageSize.height,
 		                                              BufferedImage.TYPE_INT_RGB);
 		
 		final Graphics2D g = (Graphics2D)image.getGraphics();
 		g.setColor((Color)view.getBackgroundPaint());
 		g.fillRect(0, 0, imageSize.width, imageSize.height);
 		canvas.paint( g );
 		defaultImageManager.put(vsName, image);
 	}
 
 	private void initializePropertySheetPanel() {
 		Component[] comps = visualPropertySheetPanel.getComponents();
 
 		for (Component comp : comps) {
 			if (comp.getClass() == JPanel.class) {
 				addMappingButton = new JButton();
 				addMappingButton.setToolTipText("Create New Mapping");
 				addMappingButton.setIcon(addIcon);
 				addMappingButton.addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent arg0) {
 							// final NewMappingDialog newDialog = new
 							// NewMappingDialog(
 							// Cytoscape.getDesktop(), true);
 							// newDialog.setVisible(true);
 						}
 					});
 				// addMappingButton.setOpaque(false);
 				addMappingButton.setUI(new BlueishButtonUI());
 				((JPanel) comp).add(addMappingButton);
 
 				/*
 				 * Some experimental buttons
 				 */
 				JButton deleteMapping = new JButton();
 				deleteMapping.setToolTipText("Delete Selected Mapping");
 				deleteMapping.setIcon(new ImageIcon(Cytoscape.class.getResource("/cytoscape/images/ximian/stock_delete-16.png")));
 				deleteMapping.setUI(new BlueishButtonUI());
 				((JPanel) comp).add(deleteMapping);
 
 				JButton randomize = new JButton();
 				randomize.setToolTipText("Randomize Values (Discrete Mapping Only)");
 				randomize.setIcon(new ImageIcon(Cytoscape.class.getResource("/cytoscape/images/ximian/stock_filters-16.png")));
 				randomize.setUI(new BlueishButtonUI());
 				((JPanel) comp).add(randomize);
 
 				JToggleButton newButton = new JToggleButton();
 				newButton.setToolTipText("Switch View (Attribute-Oriented <--> Visual Property Oriented)");
 				newButton.setIcon(new ImageIcon(Cytoscape.class.getResource("images/ximian/stock_refresh-16.png")));
 				// newButton.setOpaque(false);
 				newButton.setUI(new BlueishButtonUI());
 				((JPanel) comp).add(newButton);
 
 				comp.repaint();
 				repaint();
 			}
 		}
 
 		visualPropertySheetPanel.setToolBarVisible(true);
 		visualPropertySheetPanel.setSorting(true);
 	}
 
 	private void setPropertySheetAppearence() {
 		/*
 		 * Set popup menu
 		 */
 
 		/*
 		 * Set Tooltiptext for the table.
 		 */
 		visualPropertySheetPanel.setTable(new PropertySheetTable() {
 				public String getToolTipText(MouseEvent me) {
 					final Point pt = me.getPoint();
 					final int row = rowAtPoint(pt);
 
 					if (row < 0)
 						return null;
 					else {
 						final Property prop = ((Item) getValueAt(row, 0)).getProperty();
 
 						final Color fontColor;
 
 						if ((prop != null) && (prop.getValue() != null)
 						    && (prop.getValue().getClass() == Color.class))
 							fontColor = (Color) prop.getValue();
 						else
 							fontColor = Color.DARK_GRAY;
 
 						final String colorString = Integer.toHexString(fontColor.getRGB());
 
 						/*
 						 * Edit
 						 */
 						if (prop == null)
 							return null;
 
 						if (prop.getDisplayName().equals(GRAPHICAL_MAP_VIEW))
 							return "Click to edit this mapping...";
 
 						if ((prop.getDisplayName() == "Controlling Attribute")
 						    || (prop.getDisplayName() == "Mapping Type"))
 							return "<html><Body BgColor=\"white\"><font Size=\"4\" Color=\"#"
 							       + colorString.substring(2, 8) + "\"><strong>"
 							       + prop.getDisplayName() + " = " + prop.getValue()
 							       + "</font></strong></body></html>";
 						else if ((prop.getSubProperties() == null)
 						         || (prop.getSubProperties().length == 0))
 							return "<html><Body BgColor=\"white\"><font Size=\"4\" Color=\"#"
 							       + colorString.substring(2, 8) + "\"><strong>"
 							       + prop.getDisplayName() + "</font></strong></body></html>";
 
 						return null;
 					}
 				}
 			});
 
 		visualPropertySheetPanel.getTable().getColumnModel().addColumnModelListener(new TableColumnModelListener() {
 				public void columnAdded(TableColumnModelEvent arg0) {
 					// TODO Auto-generated method stub
 				}
 
 				public void columnMarginChanged(ChangeEvent e) {
 					final PropertySheetTable table = visualPropertySheetPanel.getTable();
 					Property shownProp = null;
 					int rowCount = table.getRowCount();
 
 					for (int i = 0; i < rowCount; i++) {
 						shownProp = ((Item) visualPropertySheetPanel.getTable().getValueAt(i, 0))
 						                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         .getProperty();
 
 						if ((shownProp != null)
 						    && shownProp.getDisplayName().equals(GRAPHICAL_MAP_VIEW)) {
 							final Property parent = shownProp.getParentProperty();
 
 							final Object type = ((VizMapperProperty) parent).getHiddenObject();
 
 							if (type instanceof VisualPropertyType) {
 								ObjectMapping mapping;
 
 								if (((VisualPropertyType) type).isNodeProp())
 									mapping = vmm.getVisualStyle().getNodeAppearanceCalculator()
 									             .getCalculator(((VisualPropertyType) type))
 									             .getMapping(0);
 								else
 									mapping = vmm.getVisualStyle().getEdgeAppearanceCalculator()
 									             .getCalculator(((VisualPropertyType) type))
 									             .getMapping(0);
 
 								if (mapping instanceof ContinuousMapping) {
 									table.setRowHeight(i, 80);
 
 									int wi = table.getCellRect(0, 1, true).width;
 
 									switch ((VisualPropertyType) type) {
 										case NODE_FILL_COLOR:
 										case NODE_BORDER_COLOR:
 
 											final GradientEditorPanel gre = new GradientEditorPanel(((VisualPropertyType) type));
 											gradientRenderer.setIcon(CyGradientTrackRenderer
 											                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    .getTrackGraphicIcon(wi,
 											                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         70,
 											                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         (ContinuousMapping) mapping));
 											pr.registerRenderer(shownProp, gradientRenderer);
 
 											break;
 
 										case NODE_SIZE:
 											continuousRenderer.setIcon(ContinuousTrackRenderer
 											                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             .getTrackGraphicIcon(wi,
 											                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  70,
 											                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  (ContinuousMapping) mapping,
 											                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  (VisualPropertyType) type));
 											pr.registerRenderer(shownProp, continuousRenderer);
 
 											break;
 
 										default:
 											break;
 									}
 								}
 							}
 						}
 					}
 
 					repaint();
 					visualPropertySheetPanel.repaint();
 				}
 
 				public void columnMoved(TableColumnModelEvent e) {
 					// TODO Auto-generated method stub
 				}
 
 				public void columnRemoved(TableColumnModelEvent e) {
 					// TODO Auto-generated method stub
 				}
 
 				public void columnSelectionChanged(ListSelectionEvent e) {
 					// TODO Auto-generated method stub
 				}
 			});
 
 		/*
 		 * By default, show category.
 		 */
 		visualPropertySheetPanel.setMode(PropertySheetPanel.VIEW_AS_CATEGORIES);
 
 		visualPropertySheetPanel.getTable().setComponentPopupMenu(menu);
 
 		visualPropertySheetPanel.getTable().addMouseListener(new MouseAdapter() {
 				public void mouseClicked(MouseEvent e) {
 					processMouseClick(e);
 				}
 			});
 
 		PropertySheetTable table = visualPropertySheetPanel.getTable();
 
 		//
 		// table.setDefaultRenderer(Object.class, mainCat);
 		table.setRowHeight(20);
 		// table.getRendererFactory().createTableCellRenderer(String.class);
 		// com.l2fprod.common.swing.renderer.DefaultCellRenderer rend3 = new
 		// DefaultCellRenderer();
 		//		
 		// rend3.setFont(new Font("SansSerif", Font.BOLD, 32));
 		//		
 		// TableCellRenderer rend =
 		// ((PropertyRendererRegistry)table.getRendererFactory()).getRenderer(Object.class);
 		// System.out.print("!!!!!!!!!! Renderer = " + rend);
 		//		
 		//		
 		// pr.registerRenderer(Object.class, rend3);
 		//		
 		//		
 		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 		table.setCategoryBackground(new Color(10, 10, 50, 20));
 		table.setCategoryForeground(Color.black);
 		table.setSelectionBackground(Color.white);
 		table.setSelectionForeground(Color.blue);
 		// table.setForeground(Color.black);
 
 		/*
 		 * Set editors
 		 */
 		collorCellRenderer.setForeground(Color.DARK_GRAY);
 		collorCellRenderer.setOddBackgroundColor(new Color(150, 150, 150, 20));
 		collorCellRenderer.setEvenBackgroundColor(Color.white);
 
 		// rend2.setIcon(vmIcon);
 		gradientRenderer.setHorizontalTextPosition(SwingConstants.CENTER);
 		gradientRenderer.setVerticalAlignment(SwingConstants.CENTER);
 
 		emptyBoxRenderer.setHorizontalTextPosition(SwingConstants.CENTER);
 		emptyBoxRenderer.setHorizontalAlignment(SwingConstants.CENTER);
 		emptyBoxRenderer.setBackground(new Color(0, 200, 255, 20));
 		emptyBoxRenderer.setForeground(Color.red);
 		emptyBoxRenderer.setFont(new Font("SansSerif", Font.BOLD, 12));
 
 		filledBoxRenderer.setBackground(Color.white);
 		filledBoxRenderer.setForeground(Color.blue);
 
 		// emptyBoxRenderer.setFont(new Font("SansSerif", Font.BOLD, 12));
 
 		// rend2.setFont(new Font("SansSerif", Font.BOLD, 38));
 		// rend2.setBackground(Color.white);
 		// rend2.setForeground(Color.red);
 		final Object[] nodeAttrNames = Cytoscape.getNodeAttributes().getAttributeNames();
 		Arrays.sort(nodeAttrNames);
 
 		final Object[] edgeAttrNames = Cytoscape.getEdgeAttributes().getAttributeNames();
 		Arrays.sort(edgeAttrNames);
 		nodeAttrEditor.setAvailableValues(nodeAttrNames);
 		edgeAttrEditor.setAvailableValues(edgeAttrNames);
 
 		final Set mappingTypes = Cytoscape.getVisualMappingManager().getCalculatorCatalog()
 		                                  .getMappingNames();
 
 		mappingTypeEditor.setAvailableValues(mappingTypes.toArray());
 
 		VisualPropertyIcon newIcon;
 
 		List<Icon> iconList = new ArrayList();
 		iconList.addAll(nodeShapeIcons.values());
 
 		Icon[] iconArray = new Icon[iconList.size()];
 		String[] shapeNames = new String[iconList.size()];
 		Set nodeShapes = nodeShapeIcons.keySet();
 
 		for (int i = 0; i < iconArray.length; i++) {
 			newIcon = ((NodeIcon) iconList.get(i)).clone();
 			newIcon.setIconHeight(16);
 			newIcon.setIconWidth(16);
 			iconArray[i] = newIcon;
 			shapeNames[i] = newIcon.getName();
 		}
 
 		shapeCellEditor.setAvailableValues(nodeShapes.toArray());
 		shapeCellEditor.setAvailableIcons(iconArray);
 
 		iconList = new ArrayList();
 		iconList.addAll(lineTypeIcons.values());
 		iconArray = new Icon[iconList.size()];
 		shapeNames = new String[iconList.size()];
 
 		Set lineTypes = lineTypeIcons.keySet();
 
 		for (int i = 0; i < iconArray.length; i++) {
 			newIcon = (VisualPropertyIcon) (iconList.get(i));
 			newIcon.setIconHeight(16);
 			newIcon.setIconWidth(16);
 			iconArray[i] = newIcon;
 			shapeNames[i] = newIcon.getName();
 		}
 
 		lineCellEditor.setAvailableValues(lineTypes.toArray());
 		lineCellEditor.setAvailableIcons(iconArray);
 	}
 
 	private void processMouseClick(MouseEvent e) {
 		final PropertySheetTable table = visualPropertySheetPanel.getTable();
 
 		int selected = visualPropertySheetPanel.getTable().getSelectedRow();
 
 		Property shownProp = null;
 
 		/*
 		 * Adjust height if it's an legend icon.
 		 */
 		for (int i = 0; i < table.getModel().getRowCount(); i++) {
 			shownProp = ((Item) visualPropertySheetPanel.getTable().getValueAt(i, 0)).getProperty();
 
 			if ((shownProp != null) && shownProp.getDisplayName().equals(GRAPHICAL_MAP_VIEW))
 				table.setRowHeight(i, 80);
 		}
 
 		visualPropertySheetPanel.repaint();
 
 		if (SwingUtilities.isRightMouseButton(e)) {
 			/*
 			 * Popup menu
 			 */
 			final int col = visualPropertySheetPanel.getTable().columnAtPoint(e.getPoint());
 			final int row = visualPropertySheetPanel.getTable().rowAtPoint(e.getPoint());
 
 			if (row >= 0) {
 				final Property prop = ((Item) visualPropertySheetPanel.getTable()
 				                                                      .getValueAt(selected, 0))
 				                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           .getProperty();
 				String controllerName = (String) prop.getValue();
 				CyAttributes selectedAttr = Cytoscape.getNodeAttributes();
 			}
 		} else {
 			/*
 			 * Left click.
 			 */
 			if (0 <= selected) {
 				final Item item = (Item) visualPropertySheetPanel.getTable().getValueAt(selected, 0);
 
 				final Property curProp = item.getProperty();
 
 				if (curProp == null)
 					return;
 
 				/*
 				 * Create new mapping if double-click on unused
 				 * val.
 				 */
 				String category = curProp.getCategory();
 
 				if ((e.getClickCount() == 2) && (category != null)
 				    && category.equalsIgnoreCase("Unused Properties")) {
 					((VizMapperProperty) curProp).setEditable(true);
 
 					VisualPropertyType type = (VisualPropertyType) ((VizMapperProperty) curProp)
 					                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  .getHiddenObject();
 					createNewMapping(type);
 					visualPropertySheetPanel.removeProperty(curProp);
 
 					return;
 				} else if ((e.getClickCount() == 1) && (category == null)) {
 					/*
 					 * Single left-click
 					 */
 					VisualPropertyType type = null;
 
 					if ((curProp.getParentProperty() == null)
 					    && ((VizMapperProperty) curProp).getHiddenObject() instanceof VisualPropertyType)
 						type = (VisualPropertyType) ((VizMapperProperty) curProp).getHiddenObject();
 					else if (curProp.getParentProperty() != null)
 						type = (VisualPropertyType) ((VizMapperProperty) curProp.getParentProperty())
 						       .getHiddenObject();
 					else
 
 						return;
 
 					final ObjectMapping selectedMapping;
 
 					if (type.isNodeProp())
 						selectedMapping = vmm.getVisualStyle().getNodeAppearanceCalculator()
 						                     .getCalculator(type).getMapping(0);
 					else
 						selectedMapping = vmm.getVisualStyle().getEdgeAppearanceCalculator()
 						                     .getCalculator(type).getMapping(0);
 
 					if (selectedMapping instanceof ContinuousMapping) {
 						/*
 						     * Need to check other windows.
 						     */
 						if (editorWindowManager.containsKey(type)) {
 							// This means editor is already on display.
 							editorWindowManager.get(type).requestFocus();
 
 							return;
 						} else {
 							try {
 								((JDialog) type.showContinuousEditor()).addPropertyChangeListener(this);
 							} catch (Exception e1) {
 								// TODO Auto-generated catch block
 								e1.printStackTrace();
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/*
 	 * Set property sheet panel.
 	 */
 	private void setPropertyTable() {
 		setPropertySheetAppearence();
 
 		System.out.println("VS name = "
 		                   + Cytoscape.getVisualMappingManager().getVisualStyle().getName());
 
 		/*
 		 * Clean up sheet
 		 */
 		for (Property item : visualPropertySheetPanel.getProperties())
 			visualPropertySheetPanel.removeProperty(item);
 
 		final NodeAppearanceCalculator nac = Cytoscape.getVisualMappingManager().getVisualStyle()
 		                                              .getNodeAppearanceCalculator();
 
 		final EdgeAppearanceCalculator eac = Cytoscape.getVisualMappingManager().getVisualStyle()
 		                                              .getEdgeAppearanceCalculator();
 
 		final List<Calculator> nacList = nac.getCalculators();
 		final List<Calculator> eacList = eac.getCalculators();
 
 		regr.registerDefaults();
 
 		/*
 		 * Add properties to the browser.
 		 */
 		List<Property> propRecord = new ArrayList<Property>();
 		setPropertyFromCalculator(nacList, NODE_VISUAL_MAPPING, propRecord);
 		setPropertyFromCalculator(eacList, EDGE_VISUAL_MAPPING, propRecord);
 		propertyMap.put(vmm.getVisualStyle().getName(), propRecord);
 		/*
 		 * Finally, build undef list
 		 */
 
 		// noMapList = new DSourceList(mappingExist.toArray());
 		//
 		// noMapList
 		// .setToolTipText("To Create New Mapping, Drag & Drop List Item to
 		// Browser.");
 		// noMapListScrollPane.setViewportView(noMapList);
 
 		/*
 		 * Set Unused
 		 */
 		setUnused(propRecord);
 	}
 
 	/*
 	 * Add unused visual properties to the property sheet
 	 *
 	 */
 	private void setUnused(List<Property> propList) {
 		buildList();
 		Collections.sort(noMapping);
 
 		for (VisualPropertyType type : noMapping) {
 			VizMapperProperty prop = new VizMapperProperty();
 			prop.setCategory(CATEGORY_UNUSED);
 			prop.setDisplayName(type.getName());
 			prop.setHiddenObject(type);
 			prop.setValue("Double-Click to create...");
 			prop.setEditable(false);
 			// regr.registerEditor(prop, newMappingTypeEditor);
 			visualPropertySheetPanel.addProperty(prop);
 			propList.add(prop);
 		}
 	}
 
 	/*
 	 * Set value, title, and renderer for each property in the category.
 	 */
 	private void setDiscreteProps(VisualPropertyType type, Map discMapping, Set<Object> attrKeys,
 	                              PropertyEditor editor, TableCellRenderer rend,
 	                              DefaultProperty parent) {
 		if (attrKeys == null)
 			return;
 
 		Object val = null;
 		VizMapperProperty valProp;
 		String strVal;
 
 		for (Object key : attrKeys) {
 			valProp = new VizMapperProperty();
 			strVal = key.toString();
 			valProp.setDisplayName(strVal);
 			valProp.setName(strVal);
 			valProp.setParentProperty(parent);
 
 			try {
 				val = discMapping.get(key);
 			} catch (Exception e) {
 				System.out.println("------- Map = " + discMapping.getClass() + ", class = "
 				                   + key.getClass() + ", err = " + e.getMessage());
 				System.out.println("------- Key = " + key + ", val = " + val + ", disp = " + strVal);
 			}
 
 			if (val != null)
 				valProp.setType(val.getClass());
 
 			parent.addSubProperty(valProp);
 			pr.registerRenderer(valProp, rend);
 			regr.registerEditor(valProp, editor);
 
 			valProp.setValue(val);
 		}
 	}
 
 	/*
 	 * Build one property for one visual property.
 	 */
 	private void buildProperty(Calculator calc, VizMapperProperty calculatorTypeProp,
 	                           String rootCategory) {
 		final VisualPropertyType type = calc.getVisualPropertyType();
 		/*
 		 * Set one calculator
 		 */
 		calculatorTypeProp.setCategory(rootCategory);
 		//calculatorTypeProp.setType(String.class);
 		calculatorTypeProp.setDisplayName(type.getName());
 		calculatorTypeProp.setHiddenObject(type);
 
 		/*
 		 * Mapping 0 is always currently used mapping.
 		 */
 		final ObjectMapping firstMap = calc.getMapping(0);
 
 		String attrName;
 
 		if (firstMap != null) {
 			final VizMapperProperty mappingHeader = new VizMapperProperty();
 
 			attrName = firstMap.getControllingAttributeName();
 
 			if (attrName == null) {
 				calculatorTypeProp.setValue("Please select a value!");
 				pr.registerRenderer(calculatorTypeProp, emptyBoxRenderer);
 			} else {
 				calculatorTypeProp.setValue(attrName);
 
 				final CyAttributes attr;
 
 				if (type.isNodeProp()) {
 					attr = Cytoscape.getNodeAttributes();
 				} else {
 					attr = Cytoscape.getEdgeAttributes();
 				}
 
 				calculatorTypeProp.setShortDescription("Attribute Data Type = "
 				                                       + CyAttributesUtils.getClass(attrName, attr)
 				                                                          .toString());
 				pr.registerRenderer(calculatorTypeProp, filledBoxRenderer);
 			}
 
 			mappingHeader.setDisplayName("Mapping Type");
 			mappingHeader.setHiddenObject(firstMap.getClass());
 
 			if (firstMap.getClass() == DiscreteMapping.class)
 				mappingHeader.setValue("Discrete Mapping");
 			else if (firstMap.getClass() == ContinuousMapping.class)
 				mappingHeader.setValue("Continuous Mapping");
 			else
 				mappingHeader.setValue("Passthrough Mapping");
 
 			mappingHeader.setHiddenObject(firstMap);
 
 			mappingHeader.setParentProperty(calculatorTypeProp);
 			calculatorTypeProp.addSubProperty(mappingHeader);
 			regr.registerEditor(mappingHeader, mappingTypeEditor);
 
 			final CyAttributes attr;
 			final Iterator it;
 
 			if (calc.getVisualPropertyType().isNodeProp()) {
 				attr = Cytoscape.getNodeAttributes();
 				it = Cytoscape.getCurrentNetwork().nodesIterator();
 				regr.registerEditor(calculatorTypeProp, nodeAttrEditor);
 			} else {
 				attr = Cytoscape.getEdgeAttributes();
 				it = Cytoscape.getCurrentNetwork().edgesIterator();
 				regr.registerEditor(calculatorTypeProp, edgeAttrEditor);
 			}
 
 			/*
 			 * Discrete Mapping
 			 */
 			if ((firstMap.getClass() == DiscreteMapping.class) && (attrName != null)) {
 				final Map discMapping = ((DiscreteMapping) firstMap).getAll();
 				final Set<Object> attrSet = loadKeys(attrName, attr, firstMap);
 
 				switch (type) {
 					/*
 					 * Color calculators
 					 */
 					case NODE_FILL_COLOR:
 					case NODE_BORDER_COLOR:
 					case EDGE_COLOR:
 					case EDGE_SRCARROW_COLOR:
 					case EDGE_TGTARROW_COLOR:
 					case NODE_LABEL_COLOR:
 					case EDGE_LABEL_COLOR:
 						setDiscreteProps(type, discMapping, attrSet, colorCellEditor,
 						                 collorCellRenderer, calculatorTypeProp);
 
 						break;
 
 					case NODE_LINETYPE:
 					case EDGE_LINETYPE:
 						setDiscreteProps(type, discMapping, attrSet, lineCellEditor,
 						                 lineCellRenderer, calculatorTypeProp);
 
 						break;
 
 					/*
 					 * Shape property
 					 */
 					case NODE_SHAPE:
 						setDiscreteProps(type, discMapping, attrSet, shapeCellEditor,
 						                 defCellRenderer, calculatorTypeProp);
 
 						break;
 
 					/*
 					 * Arrow Head Shapes
 					 */
 					case EDGE_SRCARROW_SHAPE:
 					case EDGE_TGTARROW_SHAPE:
 						break;
 
 					case NODE_LABEL:
 					case EDGE_LABEL:
 					case NODE_TOOLTIP:
 					case EDGE_TOOLTIP:
 						setDiscreteProps(type, discMapping, attrSet, stringCellEditor,
 						                 defCellRenderer, calculatorTypeProp);
 
 						break;
 
 					/*
 					 * Font props
 					 */
 					case NODE_FONT_FACE:
 					case EDGE_FONT_FACE:
 						setDiscreteProps(type, discMapping, attrSet, fontCellEditor,
 						                 fontCellRenderer, calculatorTypeProp);
 
 						break;
 
 					/*
 					 * Size-related props
 					 */
 					case NODE_FONT_SIZE:
 					case EDGE_FONT_SIZE:
 					case NODE_SIZE:
 					case NODE_WIDTH:
 					case NODE_HEIGHT:
 					case NODE_LINE_WIDTH:
 					case EDGE_LINE_WIDTH:
 						setDiscreteProps(type, discMapping, attrSet, numberCellEditor,
 						                 defCellRenderer, calculatorTypeProp);
 
 						break;
 
 					/*
 					 * Node Label Position. Needs special editor
 					 */
 					case NODE_LABEL_POSITION:
 						setDiscreteProps(type, discMapping, attrSet, stringCellEditor,
 						                 defCellRenderer, calculatorTypeProp);
 
 						break;
 
 					default:
 						break;
 				}
 			} else if ((firstMap.getClass() == ContinuousMapping.class) && (attrName != null)) {
 				gradientRenderer.setForeground(Color.white);
 				gradientRenderer.setBackground(Color.white);
 
 				int wi = this.visualPropertySheetPanel.getTable().getCellRect(0, 1, true).width;
 
 				// int h = this.visualPropertySheetPanel.getTable()
 				// .getCellRect(0, 1, true).height;
 				int h = 80;
 
 				VizMapperProperty graphicalView = new VizMapperProperty();
 				graphicalView.setDisplayName(GRAPHICAL_MAP_VIEW);
 				graphicalView.setParentProperty(calculatorTypeProp);
 				calculatorTypeProp.addSubProperty(graphicalView);
 
 				switch (type) {
 					/*
 					 * Color-related calcs.
 					 */
 					case NODE_FILL_COLOR:
 					case NODE_BORDER_COLOR:
 					case EDGE_COLOR:
 					case EDGE_SRCARROW_COLOR:
 					case EDGE_TGTARROW_COLOR:
 						graphicalView.setName("Color Mapping");
 						gradientRenderer.setIcon(CyGradientTrackRenderer.getTrackGraphicIcon(wi,
 						                                                                     70,
 						                                                                     (ContinuousMapping) firstMap));
 						pr.registerRenderer(graphicalView, gradientRenderer);
 
 						// for (VizMapperProperty curProp : propList) {
 						// pr.registerRenderer(curProp, cr);
 						// regr.registerEditor(curProp, ce);
 						// }
 						break;
 
 					/*
 					 * Size/Width related calcs.
 					 */
 					case NODE_LINE_WIDTH:
 					case NODE_SIZE:
 					case NODE_WIDTH:
 					case NODE_FONT_SIZE:
 					case NODE_HEIGHT:
 					case EDGE_LINE_WIDTH:
 					case EDGE_FONT_SIZE:
 						graphicalView.setName("CC Mapping");
 						continuousRenderer.setIcon(ContinuousTrackRenderer.getTrackGraphicIcon(wi,
 						                                                                       70,
 						                                                                       (ContinuousMapping) firstMap,
 						                                                                       type));
 						pr.registerRenderer(graphicalView, continuousRenderer);
 
 						break;
 
 					/*
 					 * Fixed value calcs
 					 */
 					case NODE_FONT_FACE:
 					case EDGE_FONT_FACE:
 
 						// for (VizMapperProperty curProp : propList)
 						// curProp.setType(Font.class);
 						break;
 
 					case NODE_SHAPE:
 					case NODE_LINETYPE:
 					case NODE_LABEL:
 					case NODE_LABEL_POSITION:
 					case EDGE_LINETYPE:
 					case EDGE_SRCARROW_SHAPE:
 					case EDGE_TGTARROW_SHAPE:
 					case EDGE_LABEL:
 						discreteRenderer.setIcon(DiscreteTrackRenderer.getTrackGraphicIcon(wi, 70,
 						                                                                   (ContinuousMapping) firstMap));
 						pr.registerRenderer(graphicalView, discreteRenderer);
 
 						break;
 
 					default:
 						break;
 				}
 			} else if ((firstMap.getClass() == PassThroughMapping.class) && (attrName != null)) {
 				/*
 				 * Passthrough
 				 */
 				String id;
 				String value;
 				VizMapperProperty oneProperty;
 
 				/*
 				 * Accept String only.
 				 */
 				if (attr.getType(attrName) == CyAttributes.TYPE_STRING) {
 					while (it.hasNext()) {
 						id = ((GraphObject) it.next()).getIdentifier();
 
 						value = attr.getStringAttribute(id, attrName);
 						oneProperty = new VizMapperProperty();
 
 						if (attrName.equals("ID"))
 							oneProperty.setValue(id);
 						else
 							oneProperty.setValue(value);
 
 						// This prop. should not be editable!
 						oneProperty.setEditable(false);
 
 						oneProperty.setParentProperty(calculatorTypeProp);
 						oneProperty.setDisplayName(id);
 						oneProperty.setType(String.class);
 
 						calculatorTypeProp.addSubProperty(oneProperty);
 					}
 				}
 			}
 		}
 
 		visualPropertySheetPanel.addProperty(calculatorTypeProp);
 		visualPropertySheetPanel.setRendererFactory(pr);
 		visualPropertySheetPanel.setEditorFactory(regr);
 	}
 
 	private void setPropertyFromCalculator(List<Calculator> calcList, String rootCategory,
 	                                       List<Property> propRecord) {
 		VisualPropertyType type;
 
 		for (Calculator calc : calcList) {
 			final VizMapperProperty calculatorTypeProp = new VizMapperProperty();
 			type = calc.getVisualPropertyType();
 
 			/*
 			 * Set one calculator
 			 */
 			calculatorTypeProp.setCategory(rootCategory);
 			calculatorTypeProp.setName(type.getName());
 			calculatorTypeProp.setType(String.class);
 			calculatorTypeProp.setDisplayName(type.getName());
 			calculatorTypeProp.setHiddenObject(type);
 
 			// calculatorTypeProp.setEditable(false);
 			// calculatorTypeProp.setValue(calcType);
 
 			/*
 			 * Mapping 0 is always currently used mapping.
 			 */
 			final ObjectMapping firstMap = calc.getMapping(0);
 
 			String attrName;
 
 			if (firstMap != null) {
 				final VizMapperProperty mappingHeader = new VizMapperProperty();
 
 				attrName = firstMap.getControllingAttributeName();
 
 				if (attrName == null) {
 					calculatorTypeProp.setValue("Please select a value!");
 					pr.registerRenderer(calculatorTypeProp, emptyBoxRenderer);
 				} else {
 					calculatorTypeProp.setValue(attrName);
 					pr.registerRenderer(calculatorTypeProp, filledBoxRenderer);
 				}
 
 				mappingHeader.setDisplayName("Mapping Type");
 				mappingHeader.setHiddenObject(firstMap.getClass());
 
 				if (firstMap.getClass() == DiscreteMapping.class)
 					mappingHeader.setValue("Discrete Mapping");
 				else if (firstMap.getClass() == ContinuousMapping.class)
 					mappingHeader.setValue("Continuous Mapping");
 				else
 					mappingHeader.setValue("Passthrough Mapping");
 
 				mappingHeader.setHiddenObject(firstMap);
 
 				mappingHeader.setParentProperty(calculatorTypeProp);
 				calculatorTypeProp.addSubProperty(mappingHeader);
 				regr.registerEditor(mappingHeader, mappingTypeEditor);
 
 				final CyAttributes attr;
 				final Iterator it;
 
 				if (calc.getVisualPropertyType().isNodeProp()) {
 					attr = Cytoscape.getNodeAttributes();
 					it = Cytoscape.getCurrentNetwork().nodesIterator();
 					regr.registerEditor(calculatorTypeProp, nodeAttrEditor);
 				} else {
 					attr = Cytoscape.getEdgeAttributes();
 					it = Cytoscape.getCurrentNetwork().edgesIterator();
 					regr.registerEditor(calculatorTypeProp, edgeAttrEditor);
 				}
 
 				/*
 				 * Discrete Mapping
 				 */
 				if ((firstMap.getClass() == DiscreteMapping.class) && (attrName != null)) {
 					final Map discMapping = ((DiscreteMapping) firstMap).getAll();
 					final Set<String> keyset = discMapping.keySet();
 
 					Set<Object> attrSet = loadKeys(attrName, attr, firstMap);
 
 					switch (type) {
 						/*
 						 * Color calculators
 						 */
 						case NODE_FILL_COLOR:
 						case NODE_BORDER_COLOR:
 						case EDGE_COLOR:
 						case EDGE_SRCARROW_COLOR:
 						case EDGE_TGTARROW_COLOR:
 						case NODE_LABEL_COLOR:
 						case EDGE_LABEL_COLOR:
 							setDiscreteProps(type, discMapping, attrSet, colorCellEditor,
 							                 collorCellRenderer, calculatorTypeProp);
 
 							break;
 
 						case NODE_LINETYPE:
 						case EDGE_LINETYPE:
 							setDiscreteProps(type, discMapping, attrSet, lineCellEditor,
 							                 lineCellRenderer, calculatorTypeProp);
 
 							break;
 
 						/*
 						 * Shape property
 						 */
 						case NODE_SHAPE:
 							setDiscreteProps(type, discMapping, attrSet, shapeCellEditor,
 							                 defCellRenderer, calculatorTypeProp);
 
 							break;
 
 						/*
 						 * Arrow Head Shapes
 						 */
 						case EDGE_SRCARROW_SHAPE:
 						case EDGE_TGTARROW_SHAPE:
 							break;
 
 						case NODE_LABEL:
 						case EDGE_LABEL:
 						case NODE_TOOLTIP:
 						case EDGE_TOOLTIP:
 							setDiscreteProps(type, discMapping, attrSet, stringCellEditor,
 							                 defCellRenderer, calculatorTypeProp);
 
 							// for (String key : keyset) {
 							// final VizMapperProperty valProp = new
 							// VizMapperProperty();
 							// valProp.setDisplayName(key);
 							// valProp.setValue(discMapping.get(key));
 							// valProp.setParentProperty(calculatorTypeProp);
 							// calculatorTypeProp.addSubProperty(valProp);
 							// regr.registerEditor(valProp, stringCellEditor);
 							// }
 							break;
 
 						/*
 						 * Font props
 						 */
 						case NODE_FONT_FACE:
 						case EDGE_FONT_FACE:
 							setDiscreteProps(type, discMapping, attrSet, fontCellEditor,
 							                 fontCellRenderer, calculatorTypeProp);
 
 							break;
 
 						/*
 						 * Size-related props
 						 */
 						case NODE_FONT_SIZE:
 						case EDGE_FONT_SIZE:
 						case NODE_SIZE:
 						case NODE_WIDTH:
 						case NODE_HEIGHT:
 						case NODE_LINE_WIDTH:
 						case EDGE_LINE_WIDTH:
 							setDiscreteProps(type, discMapping, attrSet, numberCellEditor,
 							                 defCellRenderer, calculatorTypeProp);
 
 							break;
 
 						/*
 						 * Node Label Position. Needs special editor
 						 */
 						case NODE_LABEL_POSITION:
 							setDiscreteProps(type, discMapping, attrSet, stringCellEditor,
 							                 defCellRenderer, calculatorTypeProp);
 
 							break;
 
 						default:
 							break;
 					}
 				} else if ((firstMap.getClass() == ContinuousMapping.class) && (attrName != null)) {
 					gradientRenderer.setForeground(Color.white);
 					gradientRenderer.setBackground(Color.white);
 
 					int wi = this.visualPropertySheetPanel.getTable().getCellRect(0, 1, true).width;
 
 					// int h = this.visualPropertySheetPanel.getTable()
 					// .getCellRect(0, 1, true).height;
 					int h = 80;
 
 					VizMapperProperty graphicalView = new VizMapperProperty();
 					graphicalView.setDisplayName(GRAPHICAL_MAP_VIEW);
 					graphicalView.setParentProperty(calculatorTypeProp);
 					calculatorTypeProp.addSubProperty(graphicalView);
 
 					switch (type) {
 						/*
 						 * Color-related calcs.
 						 */
 						case NODE_FILL_COLOR:
 						case NODE_BORDER_COLOR:
 						case EDGE_COLOR:
 						case EDGE_SRCARROW_COLOR:
 						case EDGE_TGTARROW_COLOR:
 							graphicalView.setName("Color Mapping");
 							gradientRenderer.setIcon(CyGradientTrackRenderer.getTrackGraphicIcon(wi,
 							                                                                     70,
 							                                                                     (ContinuousMapping) firstMap));
 							pr.registerRenderer(graphicalView, gradientRenderer);
 
 							// for (VizMapperProperty curProp : propList) {
 							// pr.registerRenderer(curProp, cr);
 							// regr.registerEditor(curProp, ce);
 							// }
 							break;
 
 						/*
 						 * Size/Width related calcs.
 						 */
 						case NODE_LINE_WIDTH:
 						case NODE_SIZE:
 						case NODE_WIDTH:
 						case NODE_FONT_SIZE:
 						case NODE_HEIGHT:
 						case EDGE_LINE_WIDTH:
 						case EDGE_FONT_SIZE:
 							graphicalView.setName("CC Mapping");
 							continuousRenderer.setIcon(ContinuousTrackRenderer.getTrackGraphicIcon(wi,
 							                                                                       70,
 							                                                                       (ContinuousMapping) firstMap,
 							                                                                       type));
 							pr.registerRenderer(graphicalView, continuousRenderer);
 
 							break;
 
 						/*
 						 * Fixed value calcs
 						 */
 						case NODE_FONT_FACE:
 						case EDGE_FONT_FACE:
 
 							// for (VizMapperProperty curProp : propList)
 							// curProp.setType(Font.class);
 							break;
 
 						case NODE_SHAPE:
 						case NODE_LINETYPE:
 						case NODE_LABEL:
 						case NODE_LABEL_POSITION:
 						case EDGE_LINETYPE:
 						case EDGE_SRCARROW_SHAPE:
 						case EDGE_TGTARROW_SHAPE:
 						case EDGE_LABEL:
 							discreteRenderer.setIcon(DiscreteTrackRenderer.getTrackGraphicIcon(wi,
 							                                                                   70,
 							                                                                   (ContinuousMapping) firstMap));
 							pr.registerRenderer(graphicalView, discreteRenderer);
 
 							break;
 
 						default:
 							break;
 					}
 				} else if ((firstMap.getClass() == PassThroughMapping.class) && (attrName != null)) {
 					/*
 					 * Passthrough
 					 */
 					String id;
 					String value;
 					VizMapperProperty oneProperty;
 
 					while (it.hasNext()) {
 						id = ((GraphObject) it.next()).getIdentifier();
 						value = attr.getStringAttribute(id, attrName);
 
 						oneProperty = new VizMapperProperty();
 
 						if (attrName.equals("ID"))
 							oneProperty.setValue(id);
 						else
 							oneProperty.setValue(value);
 
 						// This prop. should not be editable!
 						oneProperty.setEditable(false);
 
 						oneProperty.setParentProperty(calculatorTypeProp);
 						oneProperty.setDisplayName(id);
 						oneProperty.setType(String.class);
 
 						calculatorTypeProp.addSubProperty(oneProperty);
 					}
 				}
 			}
 
 			visualPropertySheetPanel.addProperty(calculatorTypeProp);
 			visualPropertySheetPanel.setRendererFactory(pr);
 			visualPropertySheetPanel.setEditorFactory(regr);
 			propRecord.add(calculatorTypeProp);
 		}
 	}
 
 	private Set<Object> loadKeys(final String attrName, final CyAttributes attrs,
 	                             final ObjectMapping mapping) {
 		Map mapAttrs;
 		mapAttrs = CyAttributesUtils.getAttribute(attrName, attrs);
 
 		if ((mapAttrs == null) || (mapAttrs.size() == 0))
 			return null;
 
 		List acceptedClasses = Arrays.asList(mapping.getAcceptedDataClasses());
 		Class mapAttrClass = CyAttributesUtils.getClass(attrName, attrs);
 
 		if ((mapAttrClass == null) || !(acceptedClasses.contains(mapAttrClass)))
 			return null;
 
 		return loadKeySet(mapAttrs);
 	}
 
 	/**
 	 * Loads the Key Set.
 	 */
 	private Set<Object> loadKeySet(final Map mapAttrs) {
 		final Set<Object> mappedKeys = new TreeSet<Object>();
 
 		final Iterator keyIter = mapAttrs.values().iterator();
 
 		Object o = null;
 
 		while (keyIter.hasNext()) {
 			o = keyIter.next();
 
 			if (o instanceof List) {
 				List list = (List) o;
 
 				for (int i = 0; i < list.size(); i++) {
 					Object vo = list.get(i);
 
 					if (!mappedKeys.contains(vo))
 						mappedKeys.add(vo);
 				}
 			} else {
 				if (!mappedKeys.contains(o))
 					mappedKeys.add(o);
 			}
 		}
 
 		return mappedKeys;
 	}
 
 	private void setDefaultPanel(final Image defImage) {
 		if (defImage == null) {
 			return;
 		}
 
 		final Color bgColor = vmm.getVisualStyle().getGlobalAppearanceCalculator()
 		                         .getDefaultBackgroundColor();
 
 		defaultAppearencePanel.removeAll();
 
 		final JButton defaultImageButton = new JButton();
 		defaultImageButton.setUI(new BlueishButtonUI());
 		
 		defaultImageButton.setIcon(new ImageIcon(defImage));
 		//defaultImageButton.setBackground(bgColor);
 		defaultAppearencePanel.add(defaultImageButton, BorderLayout.CENTER);
 		defaultImageButton.addMouseListener(new DefaultMouseListener());
 	}
 
 	class DefaultMouseListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent e) {
 			if (javax.swing.SwingUtilities.isLeftMouseButton(e)) {
 				final String targetName = vmm.getVisualStyle().getName();
 				final CyNetworkView oldView = vmm.getNetworkView();
 				final String focus = vmm.getNetwork().getIdentifier();
 
 				//				System.out.println("\n\n=========Before=============: " + targetName);
 				//				Map<String, CyNetworkView> views = Cytoscape.getNetworkViewMap();
 				//				for(String key: views.keySet()) {
 				//					System.out.println("Network Name: " + Cytoscape.getNetwork(key).getTitle() + ", VS name = " + views.get(key).getVisualStyle().getName());
 				//				}
 				//				for(String key: defaultImageManager.keySet()) {
 				//					System.out.println("Key Name: " + key );
 				//				}
 				final DefaultViewPanel panel = (DefaultViewPanel) DefaultAppearenceBuilder
 				                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                .showDialog(Cytoscape
 				                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            .getDesktop());
 				createDefaultImage(targetName, (DGraphView) panel.getView(),
 				                   defaultAppearencePanel.getSize());
 				setDefaultPanel(defaultImageManager.get(targetName));
 				vmm.setNetworkView(oldView);
 				vmm.setVisualStyle(targetName);
 				Cytoscape.getDesktop().setFocus(focus);
				Cytoscape.getDesktop().repaint();
 
 				//				System.out.println("=========After============= " + targetName + ", CurVS = " + vmm.getVisualStyle().getName());
 				//				views = Cytoscape.getNetworkViewMap();
 				//				for(String key: views.keySet()) {
 				//					System.out.println("Network Name: " + Cytoscape.getNetwork(key).getTitle() + ", VS name = " + views.get(key).getVisualStyle().getName());
 				//				}
 				//				for(String key: defaultImageManager.keySet()) {
 				//					System.out.println("Key Name: " + key );
 				//				}
 			}
 		}
 	}
 
 	/**
 	 * Handle propeaty change events.
 	 *
 	 * @param e
 	 *            DOCUMENT ME!
 	 */
 	public void propertyChange(PropertyChangeEvent e) {
 		/*
 		* Managing editor windows.
 		*/
 		if (e.getPropertyName() == ContinuousMappingEditorPanel.EDITOR_WINDOW_OPENED) {
 			this.editorWindowManager.put((VisualPropertyType) e.getNewValue(),
 			                             (JDialog) e.getSource());
 
 			return;
 		} else if (e.getPropertyName() == ContinuousMappingEditorPanel.EDITOR_WINDOW_CLOSED) {
 			this.editorWindowManager.remove((VisualPropertyType) e.getNewValue());
 
 			return;
 		}
 
 		/*
 		 * Get global siginal
 		 */
 //		System.out.println("Event = " + e.getPropertyName() + ", Source = " + e.getSource()
 //		                   + ", New val = " + e.getNewValue());
 
 		if (e.getPropertyName().equals(Cytoscape.CYTOSCAPE_INITIALIZED)) {
 			String vmName = vmm.getVisualStyle().getName();
 
 			if (vsNameComboBox.getItemCount() == 0) {
 				setVSSelector();
 			}
 
 			setDefaultPanel(defaultImageManager.get(vmName));
 
 			vsNameComboBox.setSelectedItem(vmName);
 			vmm.setVisualStyle(vmName);
 
 			return;
 		} else if (e.getPropertyName().equals(Cytoscape.SESSION_LOADED)) {
 			String vmName = vmm.getVisualStyle().getName();
 			setVSSelector();
 			vsNameComboBox.setSelectedItem(vmName);
 			vmm.setVisualStyle(vmName);
 
 			//System.out.println("Visual Style Switched: " + vmm.getVisualStyle().getName());
 			return;
 		} else if (e.getPropertyName().equals(CytoscapeDesktop.NETWORK_VIEW_FOCUS)
 		           && (e.getSource().getClass() == NetworkPanel.class)) {
 			//			System.out.println("Focus Sig Name: " + e.getPropertyName() + ", New = "
 			//			                   + e.getNewValue() + ", OLD = " + e.getOldValue());
 			final VisualStyle vs = vmm.getNetworkView().getVisualStyle();
 
 			if (vs != null) {
 				switchVS(vs.getName());
 				vsNameComboBox.setSelectedItem(vs.getName());
 				setDefaultPanel(this.defaultImageManager.get(vs.getName()));
 			}
 
 			return;
 		} else if (e.getPropertyName().equals(Cytoscape.ATTRIBUTES_CHANGED)) {
 			
 			System.out.println("Updating attr: Event = " + e.getPropertyName() + ", Source = " + e.getSource());
 			
 			final Object[] nodeAttrNames = Cytoscape.getNodeAttributes().getAttributeNames();
 			Arrays.sort(nodeAttrNames);
 
 			final Object[] edgeAttrNames = Cytoscape.getEdgeAttributes().getAttributeNames();
 			Arrays.sort(edgeAttrNames);
 			nodeAttrEditor.setAvailableValues(nodeAttrNames);
 			edgeAttrEditor.setAvailableValues(edgeAttrNames);
 			
 		}
 
 		/*******************************************************************
 		 * Below this line, accept only cell editor events.
 		 ******************************************************************/
 		if (e.getPropertyName().equalsIgnoreCase("value") == false)
 			return;
 
 		if (e.getNewValue() == e.getOldValue())
 			return;
 
 		final PropertySheetTable table = visualPropertySheetPanel.getTable();
 		final int selected = table.getSelectedRow();
 
 		/*
 		 * Do nothing if not selected.
 		 */
 		if (selected < 0)
 			return;
 
 		Item selectedItem = (Item) visualPropertySheetPanel.getTable().getValueAt(selected, 0);
 		VizMapperProperty prop = (VizMapperProperty) selectedItem.getProperty();
 
 		final VisualPropertyType type;
 		String ctrAttrName = null;
 
 		VizMapperProperty typeRootProp;
 
 		if ((prop.getParentProperty() == null) && e.getNewValue() instanceof String) {
 			/*
 			 * This is a controlling attr name.
 			 */
 			typeRootProp = (VizMapperProperty) prop;
 			type = (VisualPropertyType) ((VizMapperProperty) prop).getHiddenObject();
 			ctrAttrName = (String) e.getNewValue();
 		} else if ((prop.getParentProperty() == null) && (e.getNewValue() == null)) {
 			/*
 			 * Empty cell celected.  no need to change anything.
 			 */
 			return;
 		} else {
 			typeRootProp = (VizMapperProperty) prop.getParentProperty();
 			type = (VisualPropertyType) ((VizMapperProperty) prop.getParentProperty())
 			                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  .getHiddenObject();
 		}
 
 		/*
 		 * Mapping type changed
 		 */
 		if (prop.getHiddenObject() instanceof ObjectMapping) {
 			System.out.println("Mapping type changed: " + prop.getHiddenObject().toString());
 
 			if (e.getNewValue() == null)
 				return;
 
 			switchMapping(prop, e.getNewValue().toString());
 
 			/*
 			 * restore expanded props.
 			 */
 			expandLastSelectedItem(type.getName());
 
 			return;
 		}
 
 		/*
 		 * Extract calculator
 		 */
 		final ObjectMapping mapping;
 
 		System.out.println("New calc = "
 		                   + vmm.getVisualStyle().getNodeAppearanceCalculator().getCalculator(type));
 
 		if (type.isNodeProp())
 			mapping = vmm.getVisualStyle().getNodeAppearanceCalculator().getCalculator(type)
 			             .getMapping(0);
 		else
 			mapping = vmm.getVisualStyle().getEdgeAppearanceCalculator().getCalculator(type)
 			             .getMapping(0);
 
 		/*
 		 * Controlling Attribute has been changed.
 		 */
 		if (ctrAttrName != null) {
 			mapping.setControllingAttributeName(ctrAttrName, vmm.getNetwork(), false);
 			vmm.getNetworkView().redrawGraph(false, true);
 
 			/*
 			 * NEED to replace this.
 			 */
 			System.out.println("This is discrete. update prop: " + typeRootProp.getDisplayName());
 
 			visualPropertySheetPanel.removeProperty(typeRootProp);
 
 			final VizMapperProperty newRootProp = new VizMapperProperty();
 
 			if (type.isNodeProp())
 				buildProperty(vmm.getVisualStyle().getNodeAppearanceCalculator().getCalculator(type),
 				              newRootProp, NODE_VISUAL_MAPPING);
 			else
 				buildProperty(vmm.getVisualStyle().getEdgeAppearanceCalculator().getCalculator(type),
 				              newRootProp, EDGE_VISUAL_MAPPING);
 
 			removeProperty(typeRootProp);
 			propertyMap.get(vmm.getVisualStyle().getName()).add(newRootProp);
 
 			typeRootProp = null;
 
 			expandLastSelectedItem(type.getName());
 
 			return;
 		}
 
 		if (mapping instanceof ContinuousMapping || mapping instanceof PassThroughMapping)
 			return;
 
 		Object key = ((Item) visualPropertySheetPanel.getTable().getValueAt(selected, 0)).getProperty()
 		              .getDisplayName();
 
 		/*
 		 * Need to convert this string to proper data types.
 		 */
 		final CyAttributes attr;
 		ctrAttrName = mapping.getControllingAttributeName();
 
 		if (type.isNodeProp()) {
 			attr = Cytoscape.getNodeAttributes();
 		} else {
 			attr = Cytoscape.getEdgeAttributes();
 		}
 
 		Byte attrType = attr.getType(ctrAttrName);
 
 		if (attrType != CyAttributes.TYPE_STRING) {
 			switch (attrType) {
 				case CyAttributes.TYPE_BOOLEAN:
 					key = Boolean.valueOf((String) key);
 
 					break;
 
 				case CyAttributes.TYPE_INTEGER:
 					key = Integer.valueOf((String) key);
 
 					break;
 
 				case CyAttributes.TYPE_FLOATING:
 					key = Double.valueOf((String) key);
 
 					break;
 
 				default:
 					break;
 			}
 		}
 
 		System.out.println("Data Types: key = " + key.getClass() + ", val = " + e.getNewValue());
 		((DiscreteMapping) mapping).putMapValue(key, e.getNewValue());
 
 		/*
 		 * Update table and current network view.
 		 */
 		table.repaint();
 		vmm.getNetworkView().redrawGraph(false, true);
 	}
 
 	/**
 	 * Switching between mapppings. Each calcs has 3 mappings. The first one
 	 * (getMapping(0)) is the current mapping used by calculator.
 	 *
 	 */
 	private void switchMapping(VizMapperProperty prop, String newMapName) {
 		System.out.println("==========Switching map================= " + newMapName);
 
 		final VisualPropertyType type = (VisualPropertyType) ((VizMapperProperty) prop
 		                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         .getParentProperty())
 		                                .getHiddenObject();
 		final String newCalcName = vmm.getVisualStyle().getName() + "-" + type.getName() + "-"
 		                           + newMapName;
 
 		// Extract target calculator
 		Calculator newCalc = vmm.getCalculatorCatalog().getCalculator(type, newCalcName);
 
 		Calculator oldCalc = null;
 
 		if (type.isNodeProp())
 			oldCalc = vmm.getVisualStyle().getNodeAppearanceCalculator().getCalculator(type);
 		else
 			oldCalc = vmm.getVisualStyle().getEdgeAppearanceCalculator().getCalculator(type);
 
 		/*
 		 * If not exist, create new one.
 		 */
 		if (newCalc == null) {
 			System.out.println("====== Need to create: " + newCalcName);
 			vmm.getCalculatorCatalog().addCalculator(getNewCalculator(type, newMapName, newCalcName));
 		}
 
 		newCalc = vmm.getCalculatorCatalog().getCalculator(type, newCalcName);
 
 		if (type.isNodeProp())
 			vmm.getVisualStyle().getNodeAppearanceCalculator().setCalculator(newCalc);
 		else
 			vmm.getVisualStyle().getEdgeAppearanceCalculator().setCalculator(newCalc);
 
 		/*
 		 * If old calc is not standard name, rename it.
 		 */
 		if (oldCalc != null) {
 			final String oldMappingTypeName;
 
 			if (oldCalc.getMapping(0) instanceof DiscreteMapping)
 				oldMappingTypeName = "Discrete Mapper";
 			else if (oldCalc.getMapping(0) instanceof ContinuousMapping)
 				oldMappingTypeName = "Continuous Mapper";
 			else if (oldCalc.getMapping(0) instanceof PassThroughMapping)
 				oldMappingTypeName = "Passthrough Mapper";
 			else
 				oldMappingTypeName = null;
 
 			final String oldCalcName = type.getName() + "-" + oldMappingTypeName;
 
 			if (vmm.getCalculatorCatalog().getCalculator(type, oldCalcName) == null)
 				vmm.getCalculatorCatalog()
 				   .addCalculator(getNewCalculator(type, oldMappingTypeName, oldCalcName));
 		}
 
 		Property parent = prop.getParentProperty();
 		visualPropertySheetPanel.removeProperty(parent);
 
 		final VizMapperProperty newRootProp = new VizMapperProperty();
 
 		if (type.isNodeProp())
 			buildProperty(vmm.getVisualStyle().getNodeAppearanceCalculator().getCalculator(type),
 			              newRootProp, NODE_VISUAL_MAPPING);
 		else
 			buildProperty(vmm.getVisualStyle().getEdgeAppearanceCalculator().getCalculator(type),
 			              newRootProp, EDGE_VISUAL_MAPPING);
 
 		expandLastSelectedItem(type.getName());
 
 		removeProperty(parent);
 
 		propertyMap.get(vmm.getVisualStyle().getName()).add(newRootProp);
 
 		vmm.getNetworkView().redrawGraph(false, true);
 		parent = null;
 	}
 
 	private void expandLastSelectedItem(String name) {
 		final PropertySheetTable table = visualPropertySheetPanel.getTable();
 		Item item = null;
 
 		for (int i = 0; i < table.getRowCount(); i++) {
 			item = (Item) table.getValueAt(i, 0);
 
 			Property curProp = item.getProperty();
 
 			if ((curProp != null) && (curProp.getDisplayName() == name)) {
 				visualPropertySheetPanel.getTable().setRowSelectionInterval(i, i);
 
 				break;
 			}
 		}
 
 		visualPropertySheetPanel.getTable().getActionMap().get("toggle").actionPerformed(null);
 	}
 
 	private Calculator getNewCalculator(final VisualPropertyType type, final String newMappingName,
 	                                    final String newCalcName) {
 		System.out.println("Mapper = " + newMappingName);
 
 		final CalculatorCatalog catalog = vmm.getCalculatorCatalog();
 
 		Class mapperClass = catalog.getMapping(newMappingName);
 
 		// create the selected mapper
 		Class[] conTypes = { Object.class, byte.class };
 		Constructor mapperCon;
 
 		try {
 			mapperCon = mapperClass.getConstructor(conTypes);
 		} catch (NoSuchMethodException exc) {
 			// Should not happen...
 			System.err.println("Invalid mapper " + mapperClass.getName());
 
 			return null;
 		}
 
 		// create the mapper
 		final byte mapType; // node or edge calculator
 
 		if (type.isNodeProp())
 			mapType = ObjectMapping.NODE_MAPPING;
 		else
 			mapType = ObjectMapping.EDGE_MAPPING;
 
 		final Object defaultObj = type.getDefault(vmm.getVisualStyle());
 		final Object[] invokeArgs = { defaultObj, new Byte(mapType) };
 		ObjectMapping mapper = null;
 
 		try {
 			mapper = (ObjectMapping) mapperCon.newInstance(invokeArgs);
 		} catch (Exception exc) {
 			System.err.println("Error creating mapping");
 
 			return null;
 		}
 
 		return CalculatorFactory.newDefaultCalculator(type, newCalcName, mapper);
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @param vsName
 	 *            DOCUMENT ME!
 	 */
 	public void setCurrentVS(String vsName) {
 		vsNameComboBox.setSelectedItem(vsName);
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @param event
 	 *            DOCUMENT ME!
 	 */
 	public void onCyNetworkEvent(CyNetworkEvent event) {
 		System.out.println("||||||||||||||| CNEVENT  !");
 	}
 
 	private void buildList() {
 		noMapping = new ArrayList<VisualPropertyType>();
 
 		final VisualStyle vs = vmm.getVisualStyle();
 		final NodeAppearanceCalculator nac = vs.getNodeAppearanceCalculator();
 		final EdgeAppearanceCalculator eac = vs.getEdgeAppearanceCalculator();
 
 		ObjectMapping mapping = null;
 
 		for (VisualPropertyType type : VisualPropertyType.values()) {
 			Calculator calc = nac.getCalculator(type);
 
 			if (calc == null) {
 				calc = eac.getCalculator(type);
 
 				if (calc != null)
 					mapping = calc.getMapping(0);
 			} else
 				mapping = calc.getMapping(0);
 
 			if (mapping == null)
 				noMapping.add(type);
 
 			mapping = null;
 		}
 
 		System.out.println("Undef = " + noMapping.size());
 	}
 
 	/*
 	 * Actions for option menu
 	 */
 	protected class CreateLegendListener extends AbstractAction {
 		public void actionPerformed(ActionEvent e) {
 			final SwingWorker worker = new SwingWorker() {
 				public Object construct() {
 					LegendDialog ld = new LegendDialog(Cytoscape.getDesktop(), vmm.getVisualStyle());
 					ld.setVisible(true);
 
 					return null;
 				}
 			};
 
 			worker.start();
 		}
 	}
 
 	/**
 	 * Create a new Visual Style.
 	 *
 	 * @author kono
 	 *
 	 */
 	private class NewStyleListener extends AbstractAction {
 		public void actionPerformed(ActionEvent e) {
 			final String name = getStyleName(null);
 
 			if (name == null)
 				return;
 
 			// create the new style
 			final VisualStyle newStyle = new VisualStyle(name);
 
 			// add it to the catalog
 			vmm.getCalculatorCatalog().addVisualStyle(newStyle);
 			// Apply the new style
 			vmm.setVisualStyle(newStyle);
 			vmm.getNetworkView().setVisualStyle(newStyle.getName());
 
 			// this applies the new style to the graph
 			vmm.getNetworkView().redrawGraph(false, true);
 
 			/*
 			 * Rebuild the visual mapping browser.
 			 */
 			vsNameComboBox.addItem(name);
 			switchVS(name);
 
 			final JPanel defPanel = DefaultAppearenceBuilder.getDefaultView(name);
 			final DGraphView view = (DGraphView) ((DefaultViewPanel) defPanel).getView();
 			final Dimension panelSize = defaultAppearencePanel.getSize();
 
 			if (view != null) {
 				System.out.println("Creating Default Image for new visual style " + name);
 				createDefaultImage(name, view, panelSize);
 				setDefaultPanel(defaultImageManager.get(name));
 			}
 		}
 	}
 
 	/**
 	 * Get a new Visual Style name
 	 *
 	 * @param s
 	 *            DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	private String getStyleName(VisualStyle s) {
 		String suggestedName = null;
 
 		if (s != null)
 			suggestedName = vmm.getCalculatorCatalog().checkVisualStyleName(s.getName());
 
 		// keep prompting for input until user cancels or we get a valid
 		// name
 		while (true) {
 			String ret = (String) JOptionPane.showInputDialog(Cytoscape.getDesktop(),
 			                                                  "Please enter new name for the visual style.",
 			                                                  "Enter Visual Style Name",
 			                                                  JOptionPane.QUESTION_MESSAGE, null,
 			                                                  null, suggestedName);
 
 			if (ret == null)
 				return null;
 
 			String newName = vmm.getCalculatorCatalog().checkVisualStyleName(ret);
 
 			if (newName.equals(ret))
 				return ret;
 
 			int alt = JOptionPane.showConfirmDialog(Cytoscape.getDesktop(),
 			                                        "Visual style with name " + ret
 			                                        + " already exists,\nrename to " + newName
 			                                        + " okay?", "Duplicate visual style name",
 			                                        JOptionPane.YES_NO_OPTION,
 			                                        JOptionPane.WARNING_MESSAGE, null);
 
 			if (alt == JOptionPane.YES_OPTION)
 				return newName;
 		}
 	}
 
 	/**
 	* Rename a Visual Style<br>
 	*
 	*/
 	private class RenameStyleListener extends AbstractAction {
 		public void actionPerformed(ActionEvent e) {
 			final VisualStyle currentStyle = vmm.getVisualStyle();
 			final String oldName = currentStyle.getName();
 			final String name = getStyleName(currentStyle);
 
 			if (name == null) {
 				return;
 			}
 
 			lastVSName = name;
 
 			final Image img = defaultImageManager.get(oldName);
 			defaultImageManager.put(name, img);
 			defaultImageManager.remove(oldName);
 
 			/*
 			 * Update name
 			 */
 			currentStyle.setName(name);
 
 			vmm.getCalculatorCatalog().removeVisualStyle(oldName);
 			vmm.getCalculatorCatalog().addVisualStyle(currentStyle);
 
 			vmm.setVisualStyle(currentStyle);
 			vmm.getNetworkView().setVisualStyle(name);
 
 			/*
 			 * Update combo box and
 			 */
 			vsNameComboBox.addItem(name);
 			vsNameComboBox.setSelectedItem(name);
 			vsNameComboBox.removeItem(oldName);
 
 			final List<Property> props = propertyMap.get(oldName);
 			propertyMap.put(name, props);
 			propertyMap.remove(oldName);
 		}
 	}
 
 	/**
 	 * Remove selected visual style.
 	 */
 	private class RemoveStyleListener extends AbstractAction {
 		public void actionPerformed(ActionEvent e) {
 			if (vmm.getVisualStyle().getName().equals(DEFAULT_VS_NAME)) {
 				JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
 				                              "You cannot delete default style.",
 				                              "Cannot remove style!", JOptionPane.ERROR_MESSAGE);
 
 				return;
 			}
 
 			// make sure the user really wants to do this
 			final String styleName = vmm.getVisualStyle().getName();
 			final String checkString = "Are you sure you want to permanently delete"
 			                           + " the visual style '" + styleName + "'?";
 			int ich = JOptionPane.showConfirmDialog(Cytoscape.getDesktop(), checkString,
 			                                        "Confirm Delete Style",
 			                                        JOptionPane.YES_NO_OPTION);
 
 			if (ich == JOptionPane.YES_OPTION) {
 				final CalculatorCatalog catalog = vmm.getCalculatorCatalog();
 				catalog.removeVisualStyle(styleName);
 
 				// try to switch to the default style
 				VisualStyle currentStyle = catalog.getVisualStyle(DEFAULT_VS_NAME);
 
 				/*
 				 * Update Visual Mapping Browser.
 				 */
 				vsNameComboBox.removeItem(styleName);
 				vsNameComboBox.setSelectedItem(currentStyle.getName());
 				switchVS(currentStyle.getName());
 				defaultImageManager.remove(styleName);
 				propertyMap.remove(styleName);
 
 				vmm.setVisualStyle(currentStyle);
 				vmm.getNetworkView().setVisualStyle(currentStyle.getName());
 				vmm.getNetworkView().redrawGraph(false, true);
 			}
 		}
 	}
 
 	protected class CopyStyleListener extends AbstractAction {
 		public void actionPerformed(ActionEvent e) {
 			VisualStyle currentStyle = vmm.getVisualStyle();
 			VisualStyle clone = null;
 
 			try {
 				clone = (VisualStyle) currentStyle.clone();
 			} catch (CloneNotSupportedException exc) {
 				System.err.println("Clone not supported exception!");
 			}
 
 			// get new name for clone
 			final String newName = getStyleName(clone);
 
 			if (newName == null) {
 				return;
 			}
 
 			clone.setName(newName);
 			// add new style to the catalog
 			vmm.getCalculatorCatalog().addVisualStyle(clone);
 			currentStyle = clone;
 
 			vmm.setVisualStyle(currentStyle);
 			// this applies the new style to the graph
 			vmm.getNetworkView().redrawGraph(false, true);
 
 			vsNameComboBox.addItem(newName);
 			vsNameComboBox.setSelectedItem(newName);
 			switchVS(newName);
 
 			final JPanel defPanel = DefaultAppearenceBuilder.getDefaultView(newName);
 			final DGraphView view = (DGraphView) ((DefaultViewPanel) defPanel).getView();
 			final Dimension panelSize = defaultAppearencePanel.getSize();
 
 			if (view != null) {
 				System.out.println("Creating Default Image for new visual style " + newName);
 				createDefaultImage(newName, view, panelSize);
 				setDefaultPanel(defaultImageManager.get(newName));
 			}
 		}
 	}
 
 	/*
 	 * Create new calculator (mapping)
 	 */
 	private void createNewMapping(VisualPropertyType type) {
 		// get available mappings
 		final CalculatorCatalog catalog = vmm.getCalculatorCatalog();
 		final Set mapperNames = catalog.getMappingNames();
 
 		// convert to array for JOptionPane
 		// Object[] mapperArray = mapperNames.toArray();
 
 		// get a name for the new calculator
 		final String defaultMapperName = "Discrete Mapper";
 		String calcName = vmm.getVisualStyle().getName() + "-" + type.getName() + "-"
 		                  + defaultMapperName;
 
 		// create the new calculator
 		// get the selected mapper
 		Class mapperClass = catalog.getMapping(defaultMapperName);
 
 		// create the selected mapper
 		Class[] conTypes = { Object.class, byte.class };
 		Constructor mapperCon;
 
 		try {
 			mapperCon = mapperClass.getConstructor(conTypes);
 		} catch (NoSuchMethodException exc) {
 			// Should not happen...
 			System.err.println("Invalid mapper " + mapperClass.getName());
 
 			return;
 		}
 
 		// create the mapper
 		final byte mapType; // node or edge calculator
 
 		if (type.isNodeProp())
 			mapType = ObjectMapping.NODE_MAPPING;
 		else
 			mapType = ObjectMapping.EDGE_MAPPING;
 
 		final Object defaultObj = type.getDefault(vmm.getVisualStyle());
 		final Object[] invokeArgs = { defaultObj, new Byte(mapType) };
 		ObjectMapping mapper = null;
 
 		try {
 			mapper = (ObjectMapping) mapperCon.newInstance(invokeArgs);
 		} catch (Exception exc) {
 			System.err.println("Error creating mapping");
 
 			return;
 		}
 
 		Calculator calc = vmm.getCalculatorCatalog().getCalculator(type, calcName);
 
 		if (calc == null) {
 			calc = CalculatorFactory.newDefaultCalculator(type, calcName, mapper);
 
 			vmm.getCalculatorCatalog().addCalculator(calc);
 		}
 
 		if (type.isNodeProp())
 			vmm.getVisualStyle().getNodeAppearanceCalculator().setCalculator(calc);
 		else
 			vmm.getVisualStyle().getEdgeAppearanceCalculator().setCalculator(calc);
 
 		/*
 		 * Move the property in the list.
 		 */
 		Property prop = null;
 
 		for (int i = 0; i < visualPropertySheetPanel.getTable().getModel().getRowCount(); i++) {
 			Item item = (Item) visualPropertySheetPanel.getTable().getModel().getValueAt(i, 0);
 			prop = item.getProperty();
 
 			if ((prop != null) && prop.getDisplayName().equals(type.getName()))
 				visualPropertySheetPanel.removeProperty(prop);
 		}
 
 		final VizMapperProperty newRootProp = new VizMapperProperty();
 
 		if (type.isNodeProp())
 			buildProperty(calc, newRootProp, NODE_VISUAL_MAPPING);
 		else
 			buildProperty(calc, newRootProp, EDGE_VISUAL_MAPPING);
 
 		/*
 		 * Update memory
 		 */
 
 		// need to remove 2.
 		removeProperty(prop);
 
 		propertyMap.get(vmm.getVisualStyle().getName()).add(newRootProp);
 
 		noMapping.remove(newRootProp.getHiddenObject());
 		prop = null;
 
 		expandLastSelectedItem(type.getName());
 
 		vmm.getNetworkView().redrawGraph(false, true);
 	}
 
 	/**
 	 * Remove a mapping from current visual style.
 	 *
 	 */
 	private void removeMapping() {
 		final int selected = visualPropertySheetPanel.getTable().getSelectedRow();
 
 		if (0 <= selected) {
 			Item item = (Item) visualPropertySheetPanel.getTable().getValueAt(selected, 0);
 
 			Property curProp = item.getProperty();
 
 			if (curProp instanceof VizMapperProperty) {
 				VisualPropertyType type = (VisualPropertyType) ((VizMapperProperty) curProp)
 				                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           .getHiddenObject();
 
 				String[] message = {
 				                       "The Mapping for " + type.getName() + " will be removed.",
 				                       "Proceed�ｼ�"
 				                   };
 				int value = JOptionPane.showConfirmDialog(Cytoscape.getDesktop(), message,
 				                                          "Remove Mapping",
 				                                          JOptionPane.YES_NO_OPTION);
 
 				if (value == JOptionPane.YES_OPTION) {
 					/*
 					 * First, remove from property sheet.
 					 */
 
 					// visualPropertySheetPanel.removeProperty(curProp);
 					/*
 					 * Then, remove from calculator & redraw
 					 */
 					if (type.isNodeProp())
 						vmm.getVisualStyle().getNodeAppearanceCalculator().removeCalculator(type);
 					else
 						vmm.getVisualStyle().getEdgeAppearanceCalculator().removeCalculator(type);
 
 					vmm.getNetworkView().redrawGraph(false, true);
 
 					/*
 					 * Finally, move the visual property to "unused list"
 					 */
 					noMapping.add(type);
 
 					VizMapperProperty prop = new VizMapperProperty();
 					prop.setCategory(CATEGORY_UNUSED);
 					prop.setDisplayName(type.getName());
 					prop.setHiddenObject(type);
 					prop.setValue("Double-Click to create...");
 					prop.setEditable(false);
 					// regr.registerEditor(prop, newMappingTypeEditor);
 					visualPropertySheetPanel.addProperty(prop);
 
 					visualPropertySheetPanel.removeProperty(curProp);
 
 					removeProperty(curProp);
 
 					propertyMap.get(vmm.getVisualStyle().getName()).add(prop);
 					visualPropertySheetPanel.repaint();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Edit all selected cells at once.
 	 *
 	 * This is for Discrete Mapping only.
 	 *
 	 */
 	private void editSelectedCells() {
 		final PropertySheetTable table = visualPropertySheetPanel.getTable();
 		final int[] selected = table.getSelectedRows();
 
 		Item item = null;
 
 		/*
 		 * Test with the first selected item
 		 */
 		item = (Item) visualPropertySheetPanel.getTable().getValueAt(selected[0], 0);
 
 		VizMapperProperty prop = (VizMapperProperty) item.getProperty();
 		final VisualPropertyType type = (VisualPropertyType) ((VizMapperProperty) prop
 		                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         .getParentProperty())
 		                                .getHiddenObject();
 
 		/*
 		 * Extract calculator
 		 */
 		final ObjectMapping mapping;
 		final CyAttributes attr;
 
 		if (type.isNodeProp()) {
 			mapping = vmm.getVisualStyle().getNodeAppearanceCalculator().getCalculator(type)
 			             .getMapping(0);
 			attr = Cytoscape.getNodeAttributes();
 		} else {
 			mapping = vmm.getVisualStyle().getEdgeAppearanceCalculator().getCalculator(type)
 			             .getMapping(0);
 			attr = Cytoscape.getEdgeAttributes();
 		}
 
 		if (mapping instanceof ContinuousMapping || mapping instanceof PassThroughMapping)
 			return;
 
 		Object newValue = null;
 
 		try {
 			newValue = type.showDiscreteEditor();
 		} catch (Exception e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 		Object key = null;
 		final Class keyClass = CyAttributesUtils.getClass(mapping.getControllingAttributeName(),
 		                                                  attr);
 
 		for (int i = 0; i < selected.length; i++) {
 			/*
 			 * First, update property sheet
 			 */
 			((Item) visualPropertySheetPanel.getTable().getValueAt(selected[i], 0)).getProperty()
 			 .setValue(newValue);
 			/*
 			 * Then update backend.
 			 */
 			key = ((Item) visualPropertySheetPanel.getTable().getValueAt(selected[i], 0)).getProperty()
 			       .getDisplayName();
 
 			if (keyClass == Integer.class) {
 				key = Integer.valueOf((String) key);
 			} else if (keyClass == Double.class) {
 				key = Double.valueOf((String) key);
 			} else if (keyClass == Boolean.class) {
 				key = Boolean.valueOf((String) key);
 			}
 
 			((DiscreteMapping) mapping).putMapValue(key, newValue);
 		}
 
 		/*
 		 * Update table and current network view.
 		 */
 		table.repaint();
 		vmm.getNetworkView().redrawGraph(false, true);
 	}
 
 	/*
 	 * Remove an entry in the browser.
 	 */
 	private void removeProperty(final Property prop) {
 		List<Property> targets = new ArrayList<Property>();
 
 		if (propertyMap.get(vmm.getVisualStyle().getName()) == null) {
 			return;
 		}
 
 		for (Property p : propertyMap.get(vmm.getVisualStyle().getName())) {
 			if (p.getDisplayName().equals(prop.getDisplayName())) {
 				targets.add(p);
 			}
 		}
 
 		for (Property p : targets) {
 			System.out.println("Removed: " + p.getDisplayName());
 			propertyMap.get(vmm.getVisualStyle().getName()).remove(p);
 		}
 	}
 }
