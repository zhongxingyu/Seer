 //------------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //------------------------------------------------------------------------------
 package cytoscape.visual.ui;
 //------------------------------------------------------------------------------
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 import cytoscape.visual.*;
 import cytoscape.visual.calculators.*;
 import cytoscape.dialogs.GridBagGroup;
 import cytoscape.dialogs.MiscGB;
 //------------------------------------------------------------------------------
 
 /**
  * Primary UI class for the Set Visual Properties dialog box.
  */
 public class VizMapUI extends JDialog {
     // constants for attribute types, one for each tab
     public static final byte NODE_COLOR = 0;
     public static final byte NODE_BORDER_COLOR = 1;
     public static final byte NODE_LINETYPE = 2;
     public static final byte NODE_SHAPE = 3;
     public static final byte NODE_SIZE = 4;
     public static final byte NODE_LABEL = 5;
     public static final byte NODE_LABEL_FONT = 6;
     public static final byte EDGE_COLOR = 7;
     public static final byte EDGE_LINETYPE = 8;
     public static final byte EDGE_SRCARROW = 9;
     public static final byte EDGE_TGTARROW = 10;
     public static final byte EDGE_LABEL = 11;
     public static final byte EDGE_LABEL_FONT = 12;
     public static final byte NODE_TOOLTIP = 13;
     public static final byte EDGE_TOOLTIP = 14;
     
     // for creating VizMapTabs with font face/size on one page
     public static final byte NODE_FONT_FACE = 122;
     public static final byte NODE_FONT_SIZE = 123;
     public static final byte EDGE_FONT_FACE = 124;
     public static final byte EDGE_FONT_SIZE = 125;
 
     // for creating VizMapTabs with locked node height/width
     public static final byte NODE_HEIGHT = 126;
     public static final byte NODE_WIDTH = 127;
 
     // VisualMappingManager for the graph.
     protected VisualMappingManager VMM;
     /** The content pane for the dialog */
     private JPanel mainPane;
     private GridBagGroup mainGBG;
     private JPanel actionButtonsPanel, attrSelectorPanel;
     /** The content pane for the JTabbedPanes */
     private JPanel tabPaneContainer;
     /** Keeps track of contained tabs */
     private VizMapTab[] tabs;
     /**
      *  All known VisualStyles
      */
     protected Collection styles;
     /**
      * StyleSelector sub-dialog
      */
     protected StyleSelector styleSelector;
     // kludge!
     private boolean initialized = false;
 
     /**
      *	Make and display the Set Visual Properties UI.
      *
      *	@param	VMM	VisualMappingManager for the graph
      */
     public VizMapUI(VisualMappingManager VMM) {
 	super(VMM.getCytoscapeWindow().getMainFrame(), "Set Visual Properties");
 	
 	this.VMM = VMM;
 	this.mainGBG = new GridBagGroup();
 	this.mainPane = mainGBG.panel;
 	//MiscGB.pad(mainGBG.constraints, 2, 2);
 	//MiscGB.inset(mainGBG.constraints, 3);
 	this.tabs = new VizMapTab[EDGE_LABEL_FONT + 1];
 	this.tabPaneContainer = new JPanel(false);
 
 	JTabbedPane nodePane = new JTabbedPane();
 	JTabbedPane edgePane = new JTabbedPane();
 	
 	// add panes to tabbed panes
 	for (byte i = NODE_COLOR; i <= NODE_LABEL_FONT; i++) {
 	    VizMapTab tab;
 	    if (i == NODE_SIZE)
 		tab = new VizMapSizeTab(this, nodePane, i, VMM, i);
 	    else if (i == NODE_LABEL_FONT)
 		tab = new VizMapFontTab(this, nodePane, i, VMM, i);
 	    else
 		tab = new VizMapAttrTab(this, nodePane, i, VMM, i);
 	    nodePane.add(tab, i);
 	    tabs[i] = tab;
 	}
 	for (byte i = EDGE_COLOR; i <= EDGE_LABEL_FONT; i++) {
 	    VizMapTab tab;
 	    if (i == EDGE_LABEL_FONT)
 		tab = new VizMapFontTab(this, edgePane, i-EDGE_COLOR, VMM, i);
 	    else
 		tab = new VizMapAttrTab(this, edgePane, i-EDGE_COLOR, VMM, i);
 	    edgePane.add(tab, i-EDGE_COLOR);
 	    tabs[i] = tab;
 	}
 	
 	// global default pane
 	JPanel defaultPane = new DefaultPanel(this, VMM);
 	
 	// node/edge/default selector
 	ButtonGroup grp = new ButtonGroup();
 	JToggleButton nodeSelect = new JToggleButton("Node Attributes", true);
 	JToggleButton edgeSelect = new JToggleButton("Edge Attributes", false);
 	JToggleButton defSelect = new JToggleButton("Global Defaults", false);
 	grp.add(defSelect);
 	grp.add(nodeSelect);
 	grp.add(edgeSelect);
 	Color nodeColor = new Color(130, 150, 129);
 	Color edgeColor = new Color(124, 134, 173);
 	Color defColor = new Color(201,201,15);
 	nodeSelect.addActionListener(new AttrSelector(nodePane,nodeColor));
 	edgeSelect.addActionListener(new AttrSelector(edgePane,edgeColor));
 	defSelect.addActionListener(new AttrSelector(defaultPane,defColor));
 	
 	this.attrSelectorPanel = new JPanel(new FlowLayout(), false);
 	attrSelectorPanel.add(nodeSelect);
 	attrSelectorPanel.add(edgeSelect);
 	attrSelectorPanel.add(defSelect);
  	//attrSelectorPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 
 	this.styleSelector = new StyleSelector(this);
 
 	//MiscGB.insert(mainGBG, vizStylePanel, 0, 0, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL);
 	MiscGB.insert(mainGBG, attrSelectorPanel, 0, 0, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL);
 	MiscGB.insert(mainGBG, tabPaneContainer, 0, 1, 1, 1, 1, 1, GridBagConstraints.BOTH);
 	
 	// add apply & cancel button
 	this.actionButtonsPanel = new JPanel();
 	//JButton applyButton = new JButton("Apply");
 	//applyButton.addActionListener(new ApplyAction());
 	JButton closeButton = new JButton("Close");
 	closeButton.addActionListener(new CloseAction());
 	//actionButtonsPanel.add(applyButton);
 	actionButtonsPanel.add(closeButton);
 	
 	MiscGB.insert(mainGBG, actionButtonsPanel, 0, 3, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL);
 	
 	setContentPane(mainPane);
 	pack();
 	nodeSelect.doClick();
 	initialized = true;
     }
     
     public StyleSelector getStyleSelector() {
 	return this.styleSelector;
     }
 
     /**
      * StyleSelector implements the style selection control. It has been separated
      * visually from the dialog shown by VizMapUI, but is still very much a part
      * of the parent VizMapUI. Keeping StyleSelector as an internal class of
      * VizMapUI makes the program simpler since many variables must be kept
      * synchronized between the two classes.
      */
     public class StyleSelector extends JDialog {
 	/**
 	 *  Reference to catalog
 	 */
 	protected CalculatorCatalog catalog;
 
 	/**
 	 *  Model for combo boxes
 	 */
 	protected DefaultComboBoxModel styleComboModel = new DefaultComboBoxModel();
 
 	/**
 	 *  Combo box for style selection
 	 */
 	protected JComboBox styleComboBox = new JComboBox(styleComboModel);
 
 	/**
 	 *  Duplicate combo box for style selection - used in toolbar
 	 */
 	protected JComboBox styleComboBoxDupe = new JComboBox(styleComboModel);
 
 	/**
 	 *  GridBagGroup for layout
 	 */
 	protected GridBagGroup styleGBG;
 
 	/**
 	 *  Currently selected style
 	 */
 	protected VisualStyle currentStyle;
 
 	/**
 	 *  Reference to style definition UI
 	 */
 	protected VizMapUI styleDefUI;
 
 	/**
 	 *  Reference back to self for action listeners
 	 */
 	protected StyleSelector myself;
 
 	/**
 	 *  Lazily create visual style parameter UI.
 	 */
 	protected boolean styleDefNeedsUpdate = false;
 	
 	protected StyleSelector(VizMapUI styleDef) {
 	    super(VMM.getCytoscapeWindow().getMainFrame(), "Visual Styles");
 	    this.styleDefUI = styleDef;
 	    this.catalog = VMM.getCalculatorCatalog();
 	    styles = catalog.getVisualStyles();
 	    this.styleGBG = new GridBagGroup("Visual Styles");
 	    this.myself = this;
 	    // attach listener
 	    StyleSelectionListener listen = new StyleSelectionListener();
 	    this.styleComboBox.addItemListener(listen);
 	    //the duplicate styleComboBox doesn't need a listener because
 	    //JComboBox fires ItemEvents when the underlying model changes.
 	    //this.styleComboBoxDupe.addItemListener(listen);
 	    String comboBoxHelp = "Change the current visual style";
 	    this.styleComboBox.setToolTipText(comboBoxHelp);
 	    this.styleComboBoxDupe.setToolTipText(comboBoxHelp);
 	    MiscGB.pad(styleGBG.constraints, 2, 2);
 	    MiscGB.inset(styleGBG.constraints, 3);
 	    
 	    resetStyles();
 
 	    // new style button
 	    JButton newStyle = new JButton("New");
 	    newStyle.addActionListener(new NewStyleListener());
 	    MiscGB.insert(styleGBG, newStyle, 0, 1, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL);
 	    newStyle.setToolTipText("Create a new style");
 
 	    // duplicate style button
 	    JButton dupeStyle = new JButton("Duplicate");
 	    dupeStyle.addActionListener(new DupeStyleListener());
 	    MiscGB.insert(styleGBG, dupeStyle, 1, 1, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL);
 	    dupeStyle.setToolTipText("Duplicate the current style");
 
 	    // rename style button
 	    JButton renStyle = new JButton("Rename");
 	    renStyle.addActionListener(new RenStyleListener());
 	    MiscGB.insert(styleGBG, renStyle, 2, 1, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL);
 	    renStyle.setToolTipText("Rename the current style");
 
 	    // remove style button
 	    JButton rmStyle = new JButton("Delete");
 	    rmStyle.addActionListener(new RmStyleListener());
 	    MiscGB.insert(styleGBG, rmStyle, 3, 1, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL);
 	    rmStyle.setToolTipText("Delete the current style");
 
 	    // define style button
 	    JButton defStyle = new JButton("Define");
 	    defStyle.addActionListener(new DefStyleListener());
 	    MiscGB.insert(styleGBG, defStyle, 4, 0, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL);
 	    defStyle.setToolTipText("Change the current style's settings");
 
 	    // close button
 	    JButton closeBut = new JButton("Close");
 	    closeBut.addActionListener(
 	        new AbstractAction() {
 		    public void actionPerformed(ActionEvent e) {
 			dispose();
 		    }
 		}
 		);
 	    MiscGB.insert(styleGBG, closeBut, 4, 1, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL);
 	    closeBut.setToolTipText("Close this dialog");
 	    
 	    MiscGB.insert(this.styleGBG, this.styleComboBox, 0, 0, 4, 1, 1, 0, GridBagConstraints.HORIZONTAL);
 	    setContentPane(styleGBG.panel);
 	    styleGBG.panel.setToolTipText("Visual styles are a collection of attribute mappings.");
 	    pack();
 	}
 	
 	public String getStyleName(VisualStyle s) {
 	    String suggestedName = null;
 	    if (s != null)
 		suggestedName = this.catalog.checkVisualStyleName(s.getName());
 	    // keep prompting for input until user cancels or we get a valid name
 	    while(true) {
 		String ret = (String) JOptionPane.showInputDialog(myself,
 								  "Name for new visual style",
 								  "Visual Style Name Input",
 								  JOptionPane.QUESTION_MESSAGE,
 								  null, null,
 								  suggestedName);
 		if (ret == null) {
 		    return null;
 		}
 		String newName = catalog.checkVisualStyleName(ret);
 		if (newName.equals(ret))
 		    return ret;
 		int alt = JOptionPane.showConfirmDialog(myself,
 							"Visual style with name " + ret + " already exists,\nrename to " + newName + " okay?",
 							"Duplicate visual style name",
 							JOptionPane.YES_NO_OPTION,
 							JOptionPane.WARNING_MESSAGE,
 							null);
 		if (alt == JOptionPane.YES_OPTION)
 		    return newName;
 	    }
 	}
 
 	protected class DefStyleListener extends AbstractAction {
 	    public void actionPerformed(ActionEvent e) {
 		if (styleDefNeedsUpdate)
 		    styleDefUI.visualStyleChanged();
 		styleDefUI.show();
 	    }
 	}
 
 	protected class NewStyleListener extends AbstractAction {
 	    public void actionPerformed(ActionEvent e) {
 		// just create a new style with all mappers set to none
 		// get a name for the new calculator
 		String name = getStyleName(null);
 		if (name == null)
 		    return;
 		currentStyle = new VisualStyle(name);
 		catalog.addVisualStyle(currentStyle);
 		VMM.setVisualStyle(currentStyle);
 		resetStyles();
 	    }
 	}
 
 	protected class RenStyleListener extends AbstractAction {
 	    public void actionPerformed(ActionEvent e) {
 		String name = getStyleName(currentStyle);
 		if (name == null)
 		    return;
 		currentStyle.setName(name);
 		resetStyles();
 	    }
 	}
 
 	protected class RmStyleListener extends AbstractAction {
 	    public void actionPerformed(ActionEvent e) {
 		if (styles.size() == 1) {
 		    JOptionPane.showMessageDialog(myself,
 						  "There must be at least one visual style",
 						  "Cannot remove style",
 						  JOptionPane.ERROR_MESSAGE);
 		    return;
 		}
                 //make sure the user really wants to do this
                 String styleName = currentStyle.getName();
                 String checkString = "Are you sure you want to permanently delete"
                      + " the visual style named '" + styleName + "'?";
                 int ich = JOptionPane.showConfirmDialog(myself,
                                                         checkString,
                                                         "Confirm Delete Style",
                                                         JOptionPane.YES_NO_OPTION);
                 if (ich == JOptionPane.YES_OPTION) {
                     catalog.removeVisualStyle(currentStyle.getName());
                     currentStyle = (VisualStyle) styles.iterator().next();
                     VMM.setVisualStyle(currentStyle);
                     resetStyles();
                 }
 	    }
 	}
 
 	protected class DupeStyleListener extends AbstractAction {
 	    public void actionPerformed(ActionEvent e) {
 		VisualStyle clone = null;
 		try {
 		    clone = (VisualStyle) currentStyle.clone();
 		}
 		catch (CloneNotSupportedException exc) {
 		    System.err.println("Clone not supported exception!");
 		}
 		// get new name for clone
 		String newName = getStyleName(clone);
 		if (newName == null)
 		    return;
 		clone.setName(newName);
 		catalog.addVisualStyle(clone);
 		currentStyle = clone;
 		VMM.setVisualStyle(currentStyle);
 		resetStyles();
 	    }
 	}
 	
 	protected class StyleSelectionListener implements ItemListener {
 	    public void itemStateChanged(ItemEvent e) {
 		if (e.getStateChange() == ItemEvent.SELECTED) {
 		    VisualStyle newStyle = (VisualStyle) ((JComboBox) e.getSource()).getSelectedItem();
 		    if (newStyle != currentStyle && newStyle != null) {
 			currentStyle = newStyle;
 			VMM.setVisualStyle(currentStyle);
 			VMM.applyAppearances();
 			if (styleDefUI.isShowing())
 			    visualStyleChanged();
 			else
 			    styleDefNeedsUpdate = true;
 		    }
 		}
 	    }
 	}
 
 	/**
 	 *  Populates the styles combo box
 	 */
 	/*
 	protected void setupStyleComboBox() {
 	    Object styleArray[] = new Object[styles.size()];
 	    Iterator styleIter = styles.iterator();
 	    for (int i = 0; styleIter.hasNext(); i++) {
 		styleArray[i] = (VisualStyle)styleIter.next();
 	    }
 	    this.styleComboBox = new JComboBox(styleArray);
 	    this.styleComboBoxDupe = new JComboBox(styleArray);
 	    this.styleComboBox.setSelectedItem(null);
 	    this.styleComboBoxDupe.setSelectedItem(null);
 	    // attach listener
 	    StyleSelectionListener listen = new StyleSelectionListener();
 	    this.styleComboBox.addItemListener(listen);
 	    this.styleComboBoxDupe.addItemListener(listen);
 	    
 	    if (this.currentStyle == null)
 		this.currentStyle = (VisualStyle) styleArray[0];
 	    this.styleComboBox.setSelectedItem(this.currentStyle);
 	    this.styleComboBoxDupe.setSelectedItem(this.currentStyle);
 	}
 	 */
 
 	/**
 	 *  Retrieve copy of style selection combo box for toolbar
 	 */
 	public JComboBox getToolbarComboBox() {
 	    return this.styleComboBoxDupe;
 	}
 
 	/**
 	 *  Update the style combo box model
 	 */
 	protected void refreshStyleComboBox() {
 	    Iterator styleIter = styles.iterator();
 	    
 	    this.styleComboModel.removeAllElements();
 	    for (int i = 0; styleIter.hasNext(); i++) {
 		this.styleComboModel.addElement(styleIter.next());
 	    }
 	    this.styleComboModel.setSelectedItem(null);
	    if (this.currentStyle == null)
		this.currentStyle = (VisualStyle) this.styleComboModel.getElementAt(0);
	    this.styleComboModel.setSelectedItem(this.currentStyle);
 	}
 	    
 
 	/**
 	 *  Reset the style selection controls.
 	 */
 	public void resetStyles() {
 	    // reset local style collection
 	    styles = catalog.getVisualStyles();
 	    refreshStyleComboBox();
 	    /*	    if (this.styleComboBox != null)
 		this.styleGBG.panel.remove(this.styleComboBox);
 	    refreshStyleComboBox();
 	    MiscGB.insert(this.styleGBG, this.styleComboBox, 0, 0, 4, 1, 1, 0, GridBagConstraints.HORIZONTAL);
 	    validate();
 	    repaint(); */
 	}
     }
     
     private class AttrSelector implements ActionListener {
 	private JComponent myTab;
 	private Color bgColor;
 	private AttrSelector(JComponent myTab, Color bg) {
 	    this.myTab = myTab;
 	    this.bgColor = bg;
 	}
 	public void actionPerformed(ActionEvent e) {
 	    tabPaneContainer.removeAll();
 	    tabPaneContainer.add(myTab);
 	    tabPaneContainer.setBackground(bgColor);
 	    actionButtonsPanel.setBackground(bgColor);
 	    attrSelectorPanel.setBackground(bgColor);
 	    pack();
 	    repaint();
 	}
     }
     
     // apply button action listener
     private class ApplyAction extends AbstractAction {
 	public void actionPerformed(ActionEvent e) {
 	    VMM.applyAppearances();
 	}
     }
 
     // close button action listener
     private class CloseAction extends AbstractAction {
 	public void actionPerformed(ActionEvent e) {
 	    dispose();
 	}
     }
     
     /**
      * When the data structures (eg. NodeAttributes, EdgeAttributes) change,
      * refresh the UI.
      */
     public void refreshUI() {
 	for (int i = 0; i < tabs.length; i++) {
 	    tabs[i].refreshUI();
 	}
 	validate();
 	repaint();
     }
 
     /**
      * When the currently selected visual styles changed, a new set of calculators
      * with their corresponding interfaces must be switched into the UI.
      */
     public void visualStyleChanged() {
 	for (int i = 0; i < tabs.length; i++) {
 	    tabs[i].visualStyleChanged();
 	}
 	validate();
 	pack();
 	repaint();
     }
 
     /**
      * Due to a Java AWT design choice, {@link Component}s may belong to only one
      * {@link Container}. This causes problems in the VizMapper when there are
      * attributes that share calculators, such as Node Color and Node Border Color.
      * Each VizMapTab calls this method when switching calculators to ensure that
      * the newly selected calculator is not already selected elsewhere.
      * @param	selectedCalc	Calculator that the calling VizMapTab is trying to
      *				switch to.
      * @return	true if calculator already selected elsewhere, false otherwise
      */
     VizMapTab checkCalcSelected(Calculator selectedCalc) {
 	if (!initialized)
 	    return null;
 	VizMapTab selected = null;
 	for (int i = 0; i < tabs.length && (selected == null); i++) {
 	    VizMapTab t = tabs[i];
 	    selected = t.checkCalcSelected(selectedCalc);
 	}
 	return selected;
     }
 
     /**
      * Ensure that the calculator to be removed isn't used in other visual styles.
      * If it is, return the names of visual styles that are currently using it.
      * 
      * @param	c	calculator to check usage for
      * @return	names of visual styles using the calculator
      */
     public Vector checkCalculatorUsage(Calculator c) {
 	Vector conflicts = new Vector();
 	for (Iterator iter = styles.iterator(); iter.hasNext();) {
 	    VisualStyle vs = (VisualStyle) iter.next();
 	    Vector styleName = vs.checkConflictingCalculator(c);
 	    if (styleName.size() != 1)
 		conflicts.add(styleName);
 	}
 	return conflicts;
     }
 }
