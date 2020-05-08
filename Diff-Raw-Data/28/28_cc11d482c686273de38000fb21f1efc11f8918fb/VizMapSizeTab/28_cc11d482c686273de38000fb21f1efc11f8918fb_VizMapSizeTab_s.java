 // VizMapSizeTab.java
 //--------------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //--------------------------------------------------------------------------------
 package cytoscape.visual.ui;
 
 import javax.swing.*;
 import javax.swing.border.*;
 import javax.swing.event.*;
 import java.awt.*;
 import java.awt.event.*;
 import cytoscape.visual.*;
 import cytoscape.visual.calculators.*;
 import y.view.LineType;
 import y.view.Arrow;
 import java.util.*;
 /**
  * Create a tab for the Set Visual Properties dialog for Node Size. Because
  * node height and node width are mapped separately, some special behavior
  * needs to be implemented for node size in case the user would like to lock
  * both to the same calculator. VizMapSizeTab encapsulates three VizMapAttrTabs,
  * one for node height, one for node width, and one for locked node height/width.
  */
 public class VizMapSizeTab extends VizMapTab {
     private JPanel hwPanel, lockPanel, mainPanel;
 
     private VizMapAttrTab height, width, size;
 
     private NodeAppearanceCalculator nodeCalc;
     private VizMapUI mainUIDialog;
 
     private boolean locked;
 
     /**
      *	create a new tab representing the node size. Retrieve current
      *	calculator and default settings from the VMM. Start with node height/width
      *  locked to the same calculator.
      *
      *	@param	VMM	VisualMappingManager for the window
      *  @param	tabContainer	The containing JTabbedPane
      *  @param	tabIndex	index of this tab in tabContainer
      *	@param	n	Underlying network
      *	@param	type	Must be {@link VizMapUI#NODE_SIZE}
      *
      *  @throws IllegalArgumentException if type is not {@link VizMapUI#NODE_SIZE}
      */
     public VizMapSizeTab (VizMapUI mainUI, JTabbedPane tabContainer, int tabIndex, VisualMappingManager VMM, byte type) throws IllegalArgumentException {
	this(mainUI, tabContainer, tabIndex, VMM, type, true);
     }
 
     /**
      *	create a new tab representing the node size. Retrieve current
      *	calculator and default settings from the VMM.
      *
      *	@param	VMM	VisualMappingManager for the window
      *  @param	tabContainer	The containing JTabbedPane
      *  @param	tabIndex	index of this tab in tabContainer
      *	@param	n	Underlying network
      *	@param	type	Must be {@link VizMapUI#NODE_SIZE}
      *  @param	locked	true to lock node height/width to the same calculator,
      *			false otherwise.
      *
      *  @throws IllegalArgumentException if type is not {@link VizMapUI#NODE_SIZE}
      */
 
     public VizMapSizeTab (VizMapUI mainUI, JTabbedPane tabContainer, int tabIndex, VisualMappingManager VMM, byte type, boolean locked) throws IllegalArgumentException {
 	super(new BorderLayout(), false);
 
 	if (type != VizMapUI.NODE_SIZE)
 	    throw new IllegalArgumentException("You can only create a VizMapSizeTab for the Node Size attribute");
 	
 	// set the name of this component appropriately
 	setName("Node Size");
 
 	this.nodeCalc = VMM.getVisualStyle().getNodeAppearanceCalculator();
 	this.mainUIDialog = mainUI;
 	this.locked = locked;
 
 	JPanel lockBoxPanel = new JPanel(false);
	JCheckBox lockBox = new JCheckBox("Lock height/width calculators");
	lockBox.setSelected(true);
 	lockBox.addItemListener(new LockCalcListener());
 	lockBoxPanel.add(lockBox);
 
 	this.hwPanel = new JPanel(false);
 	this.hwPanel.setLayout(new BoxLayout(hwPanel, BoxLayout.Y_AXIS));
 	this.lockPanel = new JPanel(false);
 	// generate panel for height and width
 	height = new VizMapAttrTab(mainUI, tabContainer, tabIndex, VMM, VizMapUI.NODE_HEIGHT);
 	width = new VizMapAttrTab(mainUI, tabContainer, tabIndex, VMM, VizMapUI.NODE_WIDTH);
 
 	// Lock at the nodeAppearanceCalculator level, since can't have duplicate
 	// calculators. We have to be careful to keep size and height in sync.
 	size = new VizMapAttrTab(mainUI, tabContainer, tabIndex, VMM, VizMapUI.NODE_HEIGHT);
 
 	width.setBorder(BorderFactory.createTitledBorder(width.getName()));
 	height.setBorder(BorderFactory.createTitledBorder(height.getName()));
 
 	hwPanel.add(height);
 	hwPanel.add(width);
 
 	lockPanel.add(size);
 
 	mainPanel = new JPanel(false);
 	if (this.locked) {
 	    // initially set to locked width/height display
 	    mainPanel.add(lockPanel);
 	}
 	else {
 	    // initially set to separate width/height calculators
 	    mainPanel.add(hwPanel);
 	}
 
 	nodeCalc.setNodeSizeLocked(this.locked);
 
 	add(lockBoxPanel, BorderLayout.NORTH);
 	add(mainPanel, BorderLayout.CENTER);
     }
 
     /**
      * LockCalcListener toggles locking on the calculators
      */
     private class LockCalcListener implements ItemListener {
 	// initialize to initial state as defined in cytoscape.props
 	private NodeSizeCalculator widthCalc = nodeCalc.getNodeWidthCalculator();
 	private NodeSizeCalculator heightCalc = nodeCalc.getNodeHeightCalculator();
 
 	// maintain separate memoization for the locked calculator
 	private NodeSizeCalculator lockCalc = nodeCalc.getNodeHeightCalculator();
 
 	public void itemStateChanged(ItemEvent e) {
 	    if (e.getStateChange() == ItemEvent.SELECTED) {
 		mainPanel.removeAll();
 		mainPanel.add(lockPanel);
 		locked = true;
 
 		// set nodeSizeLocked in the NodeAppearanceCalculator
 		nodeCalc.setNodeSizeLocked(true);
 
 		// memoize currently selected calculators
 		// have to clear the current calculators to prevent
 		// conflicts in panels - calculators can only be selected in one
 		// VizMapAttrTab
 		this.widthCalc = nodeCalc.getNodeWidthCalculator();
 		width.setComboBox(null);
 
 		this.heightCalc = nodeCalc.getNodeHeightCalculator();
 		height.setComboBox(null);
 
 		// set the locked calculator as memoized
 		size.setComboBox(this.lockCalc);
 	    }
 	    else {
 		mainPanel.removeAll();
 		mainPanel.add(hwPanel);
 		locked = false;
 		
 		// set nodeSizeLocked in the NodeAppearanceCalculator
 		nodeCalc.setNodeSizeLocked(false);
 
 		// memoize currently selected calculator
 		this.lockCalc = nodeCalc.getNodeHeightCalculator();
 		size.setComboBox(null);
 
 		// reset back to old calculators
 		height.setComboBox(this.heightCalc);
 		width.setComboBox(this.widthCalc);
 	    }
 	    
 	    // redraw the panel
 	    validate();
 	    //repaint();
 	    //mainUIDialog.pack();
 	    //mainUIDialog.repaint();
 	    
 	}
     }
 
     public void refreshUI() {
 	this.height.refreshUI();
 	this.width.refreshUI();
 	this.size.refreshUI();
     }
 
     public void visualStyleChanged() {
 	this.height.visualStyleChanged();
 	this.width.visualStyleChanged();
 	this.size.visualStyleChanged();
     }
 
     VizMapTab checkCalcSelected(Calculator c) {
 	if (this.locked) { // pass on to size
 	    return size.checkCalcSelected(c);
 	}
 	else { // pass on to height and width
 	    /* Either height or width has the calculator. Return the first non-null
 	       response.
 	    */
 	    VizMapTab ret = height.checkCalcSelected(c);
 	    if (ret == null)
 		return width.checkCalcSelected(c);
 	    else
 		return ret;
 	}
     }
 }
