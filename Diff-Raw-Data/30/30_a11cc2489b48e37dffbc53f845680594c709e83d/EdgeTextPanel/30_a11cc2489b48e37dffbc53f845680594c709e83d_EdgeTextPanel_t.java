 // EdgeTextPanel.java
 //---------------------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //---------------------------------------------------------------------------------------
 package cytoscape.dialogs;
 //---------------------------------------------------------------------------------------
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.event.*;
 import javax.swing.ScrollPaneConstants;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.TreeMap;
 
 import cytoscape.GraphObjAttributes;
 import cytoscape.util.MutableString;
 import cytoscape.util.MutableColor;
 import cytoscape.vizmap.*;
 import cytoscape.dialogs.MiscGB;
import cytoscape.dialogs.GridBagGroup;
 //----------------------------------------------------------------------------
 public class EdgeTextPanel extends JPanel {
 
     String [] attributeNames;
     MutableString edgeKey;
     MutableString attribKey;
     JComboBox theBox;
     JButton theButton;
     AttributeMapper aMapper;
     Frame parentFrame;
     Map theMap;
     String theMapKey;
     boolean useThisMap;
     boolean stateChanged;
     boolean useMappingGenerally;
     boolean mapSetup;
 
     public EdgeTextPanel (GraphObjAttributes edgeAttribs,
 			  AttributeMapper aMapper,
 			  Frame parentFrame,
 			  MutableString writeHere,
 			  Map backupMap,
 			  String backupKey,
 			  boolean useMapping)
     {
 	super ();
 	this.aMapper = aMapper;
 	this.parentFrame = parentFrame;
 	attributeNames = edgeAttribs.getAttributeNames ();
 	edgeKey = writeHere;
 	attribKey = new MutableString("");
 	theMap = null;
 	useThisMap=false;
 	stateChanged=false;
 	useMappingGenerally=useMapping;
 
 	this.setLayout (new GridLayout(1,3,10,10));
 	JCheckBox useMappingCheck = new JCheckBox("Use Mapping?",useMappingGenerally);
 	useMappingCheck.addItemListener(new UseMappingListener());
 	this.add(useMappingCheck);
 	
 	DefaultComboBoxModel boxModel = new DefaultComboBoxModel();
 	for (int i=0; i < attributeNames.length; i++)
 	    boxModel.addElement(new String(attributeNames [i]));
 	theBox = new JComboBox(boxModel);
 	mapSetup = prepTheBox(boxModel,backupMap,backupKey);
 	this.add(theBox);
 	
 	theButton = new JButton ("Define Mapping");
 	theButton.addActionListener (new ColorToDiscreteListener());
 	this.add(theButton);
 
 	whetherMenuIsEnabled();	
     }
 
     /** sets up the combo box and map */
     private boolean prepTheBox(DefaultComboBoxModel boxModel, Map backupMap, String backupKey) {
 	boolean retval = false;
 	theBox.addActionListener(new BoxAction(edgeKey));
 	if(boxModel.getSize()==1) {
 	    theBox.setSelectedIndex(0);
 	    edgeKey.setString((String)boxModel.getElementAt(0));
 	    retval = setupTheMap(backupMap,backupKey);
 	}
 	else {
 	    if(edgeKey.getString()!=null) {
 		theBox.setSelectedItem(edgeKey.getString());
 		retval = setupTheMap(backupMap,backupKey);
 	    }
 	    else {
 		if(boxModel.getSize()>=1) {
 		    theBox.setSelectedIndex(0);
 		    edgeKey.setString((String)boxModel.getElementAt(0));
 		    retval = setupTheMap(backupMap,backupKey);
 		}
 	    }
 	}
 	return retval;
     }
 
     /** returns the map. */
     public Map getMap() {
 	return theMap;
     }
     /** records state changes */
     public boolean didStateChange() {
 	return stateChanged;
     }
     /** returns the mapping bit */
     public boolean useMappingGenerally() {
 	return useMappingGenerally;
     }
 
     /** updates the colors on scalable arrows using EdgeArrowColor. */
     public void updateMapperScalableArrows() {
 	/** first loop through the map, making a copy. */
 	TreeMap tempSM = new TreeMap();
 	Set allKeys = theMap.keySet();
 	Iterator keyIter = allKeys.iterator();
 	for(;keyIter.hasNext();) {
 	    String keyString = (String)keyIter.next();
 	    Color c = (Color)theMap.get(keyString);
 	    tempSM.put(keyString,c);
 	}
 	/** then loop through the copy, using the EdgeArrowColor method. */
 	allKeys = tempSM.keySet();
 	keyIter = allKeys.iterator();
 	for(;keyIter.hasNext();) {
 	    String keyString = (String)keyIter.next();
 	    Color c = (Color)theMap.get(keyString);
 	    EdgeArrowColor.removeThenAddEdgeColor(aMapper,keyString,c);
 	}
     }
     public boolean getWhetherToUseTheMap() {
 	return useThisMap;
     }
     public void setWhetherToUseTheMap(boolean b) {
 	useThisMap = b;
     }
     public class BoxAction extends AbstractAction {
 	MutableString ms;
 	public BoxAction(MutableString ms) {
 	    super();
 	    this.ms = ms;
 	}
 	public void actionPerformed(ActionEvent e) {
 	    JComboBox jcb = (JComboBox)e.getSource();
 	    ms.setString((String)jcb.getSelectedItem());
 	    //System.out.println(ms.getString());
 	}
     }
     
     public class ColorToDiscreteListener extends AbstractAction {
 	public void actionPerformed (ActionEvent e) {
 	    //System.out.println("define " + edgeKey.getString());
 	    ColorToDiscreteDialog ctdDialog =
 		new ColorToDiscreteDialog();
 	}
     }
 
     public class UseMappingListener implements ItemListener {
 	public void itemStateChanged (ItemEvent e) {
 	    JCheckBox jcb = (JCheckBox)e.getItem();
 	    useMappingGenerally = jcb.isSelected();
 	    whetherMenuIsEnabled();
 	    stateChanged = true;
 	}
     }
 
     public void whetherMenuIsEnabled() {
 	theBox.setEnabled(useMappingGenerally);
 	theButton.setEnabled(useMappingGenerally);
     }
 
     public class MapStringListener extends AbstractAction {
 	JLabel label;
 	Map theMap;
 	Component component;
 	String key;
 	String title;
 	public MapStringListener(Component component,
 				 Map theMap, String key,
 				 JLabel label, String title) {
 	    this.label = label;
 	    this.theMap = theMap;
 	    this.key = key;
 	    this.component = component;
 	    this.title = title;
 	}
 	public void actionPerformed (ActionEvent e) {
 	    MutableColor mc = new MutableColor((Color)theMap.get(key));
 	    GeneralColorDialogListener gcdl =
 		new GeneralColorDialogListener(component,
 					       mc,
 					       label,
 					       title);
 	    gcdl.popup();
 	    theMap.put(key,mc.getColor());
 	}
     }
     
     public class ColorToDiscreteDialog extends JDialog {
 	JPanel extScrollPanel;
 	JPanel intScrollPanel;
 	public ColorToDiscreteDialog() {
 	    super(parentFrame, true);
 	    setTitle("Colors for " + edgeKey.getString() + " types");
 	    
	    GridBagGroup ctddGBG = new GridBagGroup();
	    intScrollPanel = ctddGBG.panel;
	    //intScrollPanel = new JPanel(new GridLayout(0,2));

 	    /** setupTheMap does some heavy lifting. */
 	    //if(EdgeTextPanel.this.setupTheMap()) {
 	    if(mapSetup) {
 		Set allKeys = theMap.keySet();
 		Iterator keyIter = allKeys.iterator();
		int yPos = 0;
 		for(;keyIter.hasNext();) {
 		    Object keyObject = keyIter.next();
 		    String keyString = (String)keyObject;
 		    //System.out.println(" key: " + keyString);
 		    JButton tempButton = new JButton(keyString);
		    JLabel tempLabel = new JLabel("       ");
 		    tempLabel.setOpaque(true);
 		    tempLabel.setBackground((Color)theMap.get(keyString));
 		    String tempTitle = edgeKey.getString() + " / " + keyString;
 		    tempButton.addActionListener(new
 			MapStringListener(ColorToDiscreteDialog.this,
 					  theMap,
 					  keyString,
 					  tempLabel,
 					  tempTitle));
		    MiscGB.insert(ctddGBG,tempButton,0,yPos,1,1,GridBagConstraints.BOTH);
		    MiscGB.insert(ctddGBG,tempLabel,1,yPos,1,1,GridBagConstraints.VERTICAL);
		    yPos++;
		    //intScrollPanel.add(tempButton);
		    //intScrollPanel.add(tempLabel);
 		}
 	    }
 	    else {
 		// this is so that we don't have to create mappings where none existed.
 		return;
 	    }
 	    /** arrangeInterface does some heavy lifting. */
 	    arrangeInterface(intScrollPanel, extScrollPanel);
 	    setLocationRelativeTo (EdgeTextPanel.this);
 	    setVisible (true);
 	    
 	}
 	private void arrangeInterface(JPanel internalScroll, JPanel externalScroll) {
 	    GridBagGroup popupGBG = new GridBagGroup();
 	    JScrollPane listScrollPane =
 		new JScrollPane(internalScroll,
 				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
 				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    //listScrollPane.setMinimumSize(new Dimension(10,150));
	    //listScrollPane.setMaximumSize(new Dimension(500,150));
	    //listScrollPane.setPreferredSize(new Dimension(150,150));
 	    externalScroll = new JPanel(new GridLayout(1,1));
 	    externalScroll.add(listScrollPane);
 	    MiscGB.insert(popupGBG,externalScroll,0,0,2,1);
 	    
 	    JButton cancelButton = new JButton ("Cancel");
 	    cancelButton.addActionListener (new CancelAction ());
 	    MiscGB.insert(popupGBG,cancelButton,0,3);
 	    JButton applyButton = new JButton ("Apply");
 	    applyButton.addActionListener (new ApplyAction ());
 	    MiscGB.insert(popupGBG,applyButton,1,3);
 
 	    setContentPane(popupGBG.panel);
	    Dimension d = listScrollPane.getPreferredSize();
	    int prefHeight = (int)d.getHeight();
	    if(prefHeight>150) prefHeight=150;
	    listScrollPane.setPreferredSize(new Dimension((int)d.getWidth(),prefHeight));
 	    pack ();
 	}
 	public class EditAction extends AbstractAction {
 	    EditAction () { super (""); }
 	    public void actionPerformed (ActionEvent e) {
 		//
 		MutableColor mc =
 		    new MutableColor((Color)theMap.get(attribKey.getString()));
 		GeneralColorDialogListener gcdListener =
 		    new GeneralColorDialogListener (ColorToDiscreteDialog.this,
 						    mc,
 						    edgeKey.getString() +
 						    "/" +
 						    attribKey.getString());
 		gcdListener.popup();
 		theMap.remove(attribKey.getString());
 		theMap.put(attribKey.getString(),mc.getColor());
 	    }
 	} // EditAction
 	
 	public class CancelAction extends AbstractAction {
 	    CancelAction () { super (""); }
 	    public void actionPerformed (ActionEvent e) {
 		ColorToDiscreteDialog.this.dispose ();
 	    }
 	} // CancelAction
 	
 	public class ApplyAction extends AbstractAction {
 	    ApplyAction () { super (""); }
 	    public void actionPerformed (ActionEvent e) {
 		useThisMap=true;
 		stateChanged = true;
 		ColorToDiscreteDialog.this.dispose ();
 	    }
 	} // CancelAction
 	
     } // class ColorToDiscreteDialog
 
     private boolean setupTheMap(Map backupMap, String backupKey) {
 	boolean retval = false;
 	String controllingAttribName =
 	    aMapper.getControllingDomainAttributeName(VizMapperCategories.EDGE_COLOR);
 	//System.out.println(">" + edgeKey.getString() + "<  vs  >"
 	//		   + controllingAttribName + "<");
 	if(controllingAttribName == null) {
 	    theMap = backupMap;
 	    theMapKey = backupKey;
 	    retval = true;
 	}
 	else if(controllingAttribName.equals(edgeKey.getString())) {
 	    retval = true;
 	    DiscreteMapper dmColor =
 		(DiscreteMapper)
 		aMapper.getValueMapper(VizMapperCategories.EDGE_COLOR);
 	    if(theMap==null || !theMapKey.equals(controllingAttribName)) {
 		System.out.println("In setupTheMap, creating");
 		theMap = new HashMap(dmColor.getValueMap());
 		theMapKey = controllingAttribName;
 	    }
 	}
 	return retval;
     }
 } // class EdgeTextPanel
