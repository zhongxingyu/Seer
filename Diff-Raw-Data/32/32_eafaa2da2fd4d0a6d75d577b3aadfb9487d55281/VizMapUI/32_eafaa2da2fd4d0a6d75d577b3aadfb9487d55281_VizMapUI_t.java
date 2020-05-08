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
     private VisualMappingManager VMM;
     /** Node apperance calculator reference */
     private NodeAppearanceCalculator nodeCalc;
     /** Edge appearance calculator reference */
     private EdgeAppearanceCalculator edgeCalc;
     /** The content pane for the dialog */
     private Container mainPane;
     private JPanel actionButtonsPanel, attrSelectorPanel;
     /** The content pane for the JTabbedPanes */
     private JPanel tabPaneContainer;
     /** Keeps track of contained tabs */
     private VizMapTab[] tabs;
 
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
 	this.mainPane = new JPanel(new BorderLayout(), false);
 	this.tabs = new VizMapTab[EDGE_LABEL_FONT + 1];
 	this.tabPaneContainer = new JPanel(false);
 	
 	// get appearance calculator references
 	this.nodeCalc = VMM.getNodeAppearanceCalculator();
 	this.edgeCalc = VMM.getEdgeAppearanceCalculator();
 
 	JTabbedPane nodePane = new JTabbedPane();
 	JTabbedPane edgePane = new JTabbedPane();
 	
 	// add panes to tabbed panes
 	for (byte i = NODE_COLOR; i <= NODE_LABEL_FONT; i++) {
 	    VizMapTab tab;
 	    if (i == NODE_SIZE)
 		tab = new VizMapSizeTab(this, VMM, i);
 	    else if (i == NODE_LABEL_FONT)
 		tab = new VizMapFontTab(this, VMM, i);
 	    else
 		tab = new VizMapAttrTab(this, VMM, i);
 	    nodePane.add(tab);
 	    tabs[i] = tab;
 	}
 	for (byte i = EDGE_COLOR; i <= EDGE_LABEL_FONT; i++) {
 	    VizMapTab tab;
 	    if (i == EDGE_LABEL_FONT)
 		tab = new VizMapFontTab(this, VMM, i);
 	    else
 		    tab = new VizMapAttrTab(this, VMM, i);
 	    edgePane.add(tab);
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
 	
 	this.attrSelectorPanel = new JPanel(false);
 	attrSelectorPanel.add(nodeSelect);
 	attrSelectorPanel.add(edgeSelect);
 	attrSelectorPanel.add(defSelect);
 	attrSelectorPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 	mainPane.add(attrSelectorPanel, BorderLayout.NORTH);
 	//tabPaneContainer.add(nodePane);
 	
 	// add tab pane container
 	mainPane.add(tabPaneContainer, BorderLayout.CENTER);
 	
 	// add apply & cancel button
 	this.actionButtonsPanel = new JPanel();
 	JButton applyButton = new JButton("Apply");
 	applyButton.addActionListener(new ApplyAction());
 	JButton closeButton = new JButton("Close");
 	closeButton.addActionListener(new CloseAction());
 	actionButtonsPanel.add(applyButton);
 	actionButtonsPanel.add(closeButton);
 	
 	mainPane.add(actionButtonsPanel, BorderLayout.SOUTH);
 	
 	setContentPane(mainPane);
 	pack();
 	//this.show();
 	nodeSelect.doClick();
 	initialized = true;
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
 	    //attrSelectorPanel.setBackground(bgColor);
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
 }
