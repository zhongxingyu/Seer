 // VisualPropertiesDialog.java
 //---------------------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //---------------------------------------------------------------------------------------
 package cytoscape.dialogs;
 //---------------------------------------------------------------------------------------
 import javax.swing.*;
 import javax.swing.AbstractAction;
 import javax.swing.border.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.event.*;
 import java.text.NumberFormat;
 import java.util.HashMap;
 import java.util.Map;
 
 import y.view.LineType;
 import y.view.Arrow;
 
 import cytoscape.GraphObjAttributes;
 import cytoscape.data.*;
 import cytoscape.vizmap.*;
 import cytoscape.dialogs.NewSlider;
 import cytoscape.util.MutableColor;
 import cytoscape.util.MutableString;
 import cytoscape.util.MutableBool;
 import cytoscape.dialogs.GeneralColorDialogListener;
 import cytoscape.dialogs.MiscGB;
 //--------------------------------------------------------------------------------------
 public class VisualPropertiesDialog extends JDialog {
 
     IconPopupButton shapeDefault;
     IconPopupButton lineTypeDefault;
     IconPopupButton arrowDefault;
     IntegerEntryField sizeDefault;
     AttributeMapper aMapper;
     Frame parentFrame;
     MutableColor nColor;
     MutableColor bColor;
     MutableColor bgColor;
     MutableString localNodeLabelKey;
     MutableString parentNodeLabelKey;
     MutableString localEdgeKey;
     MutableBool applied;
     EdgeTextPanel edgeTextPanel;
 //--------------------------------------------------------------------------------------
 public VisualPropertiesDialog (Frame parentFrame,
 			       String title,
 			       AttributeMapper mapper,
 			       GraphObjAttributes nodeAttribs,
 			       GraphObjAttributes edgeAttribs,
 			       MutableString nodeLabelKey,
 			       MutableBool applied)
 {
   super (parentFrame, true);
   setTitle (title);
   this.parentFrame = parentFrame;
 
   aMapper = mapper;
   nColor = new MutableColor(getBasicColor(VizMapperCategories.NODE_FILL_COLOR));
   bColor = new MutableColor(getBasicColor(VizMapperCategories.NODE_BORDER_COLOR));
   bgColor = new MutableColor(getBasicColor(VizMapperCategories.BG_COLOR));
   localNodeLabelKey = new MutableString(nodeLabelKey.getString());
   parentNodeLabelKey = nodeLabelKey;
   this.applied = applied;
 
   JPanel mainPanel = new JPanel ();
   GridBagLayout gridbag = new GridBagLayout(); 
   GridBagConstraints c = new GridBagConstraints();
   MiscGB.pad(c,5,5);
   mainPanel.setLayout (gridbag);
 
   BorderedPanel defaultBP = new BorderedPanel("Defaults");
   JPanel defaultPanel = defaultBP.getPanel();
   GridBagLayout defaultLayout = defaultBP.getLayout();
   GridBagConstraints defaultC = defaultBP.getConstraints();
   MiscGB.pad(defaultC,5,5);
 
 
   JButton colorButton = new JButton("Node Color");
   JLabel colorLabel = MiscGB.createColorLabel(nColor.getColor());
   colorButton.addActionListener(new
       GeneralColorDialogListener(this,nColor,colorLabel,
				 "Choose a Node Color"));
   MiscGB.set(defaultC,0,0,1,1,GridBagConstraints.HORIZONTAL);
   MiscGB.insert(defaultPanel,colorButton,defaultLayout,defaultC);
   MiscGB.set(defaultC,1,0);
   MiscGB.insert(defaultPanel,colorLabel,defaultLayout,defaultC);
 
   JButton borderColorButton = new JButton("Node Border Color");
   JLabel borderColorLabel = MiscGB.createColorLabel(bColor.getColor());
   borderColorButton.addActionListener(new
       GeneralColorDialogListener(this,bColor,borderColorLabel,
				 "Choose a Node Border Color"));
   MiscGB.set(defaultC,1,2);
   MiscGB.insert(defaultPanel,borderColorLabel,defaultLayout,defaultC);
 
   MiscGB.set(defaultC,0,2,1,1,GridBagConstraints.HORIZONTAL);
   MiscGB.insert(defaultPanel,borderColorButton,defaultLayout,defaultC);
 
   JButton bgColorButton
       = new JButton("Background Color");
   JLabel bgColorLabel = MiscGB.createColorLabel(bgColor.getColor());
   bgColorButton.addActionListener(new
       GeneralColorDialogListener(this,bgColor,bgColorLabel,
				 "Choose a Background Color"));
   MiscGB.set(defaultC,1,3);
   MiscGB.insert(defaultPanel,bgColorLabel,defaultLayout,defaultC);
 
   MiscGB.set(defaultC,0,3,1,1,GridBagConstraints.HORIZONTAL);
   MiscGB.insert(defaultPanel,bgColorButton,defaultLayout,defaultC);
 
   sizeDefault = 
       new IntegerEntryField
 	  ("Node Size",
 	   ((Integer)aMapper.getDefaultValue(VizMapperCategories.NODE_HEIGHT)).intValue(),
 	   500);
   MiscGB.set(defaultC,0,4);
   MiscGB.insert(defaultPanel,sizeDefault.getLabel(),defaultLayout,defaultC);
   MiscGB.set(defaultC,1,4);
   MiscGB.insert(defaultPanel,sizeDefault.getField(),defaultLayout,defaultC);
 
   initializeShapeDefault();
   MiscGB.set(defaultC,0,5,1,1,GridBagConstraints.HORIZONTAL);
   MiscGB.insert(defaultPanel,shapeDefault.getButton(),defaultLayout,defaultC);
   MiscGB.set(defaultC,1,5);
   MiscGB.insert(defaultPanel,shapeDefault.getLabel(),defaultLayout,defaultC);
 
   initializeLineTypeDefault();
   MiscGB.set(defaultC,0,6,1,1,GridBagConstraints.HORIZONTAL);
   MiscGB.insert(defaultPanel,lineTypeDefault.getButton(),defaultLayout,defaultC);
   MiscGB.set(defaultC,1,6);
   MiscGB.insert(defaultPanel,lineTypeDefault.getLabel(),defaultLayout,defaultC);
 
   initializeArrowDefault();
   MiscGB.set(defaultC,0,7,1,1,GridBagConstraints.HORIZONTAL);
   MiscGB.insert(defaultPanel,arrowDefault.getButton(),defaultLayout,defaultC);
   MiscGB.set(defaultC,1,7);
   MiscGB.insert(defaultPanel,arrowDefault.getLabel(),defaultLayout,defaultC);
 
   //////////////////////////////////////////////
   MiscGB.set(c,0,0,2,1,GridBagConstraints.HORIZONTAL);
   MiscGB.insert(mainPanel,defaultPanel,gridbag,c);
 
   //////////////////////////////////////////////
   BorderedPanel labelBP = new BorderedPanel("Node Label Mapping");
   JPanel labelPanel = labelBP.getPanel();
   GridBagLayout labelLayout = labelBP.getLayout();
   GridBagConstraints labelConstraints = labelBP.getConstraints();
 
   JPanel labelTextPanel
       = new LabelTextPanel(nodeAttribs,localNodeLabelKey);
   MiscGB.set(labelConstraints,0,0);
   MiscGB.insert(labelPanel,labelTextPanel,labelLayout,labelConstraints);
   MiscGB.set(c,0,8,2,1,GridBagConstraints.HORIZONTAL);
   MiscGB.insert(mainPanel,labelPanel,gridbag,c);
 
   //////////////////////////////////////////////
   BorderedPanel edgeBP = new BorderedPanel("Edge Color Mapping");
   JPanel edgePanel = edgeBP.getPanel();
   GridBagLayout edgeLayout = edgeBP.getLayout();
   GridBagConstraints edgeConstraints = edgeBP.getConstraints();
   
   if(localEdgeKey==null) localEdgeKey = new MutableString("temp");
   edgeTextPanel
       = new EdgeTextPanel(edgeAttribs,aMapper,parentFrame,localEdgeKey);
   MiscGB.set(edgeConstraints,0,0);
   MiscGB.insert(edgePanel,edgeTextPanel,edgeLayout,edgeConstraints);
   MiscGB.set(c,0,9,2,1,GridBagConstraints.HORIZONTAL);
   MiscGB.insert(mainPanel,edgePanel,gridbag,c);
 
   JButton applyButton = new JButton ("Apply");
   applyButton.addActionListener (new ApplyAction ());
   MiscGB.set(c,0,10);
   MiscGB.insert(mainPanel,applyButton,gridbag,c);
 
   JButton cancelButton = new JButton ("Cancel");
   cancelButton.addActionListener (new CancelAction ());
   MiscGB.set(c,1,10);
   MiscGB.insert(mainPanel,cancelButton,gridbag,c);
 
   setContentPane (mainPanel);
 } // PopupDialog ctor
 
 public class ApplyAction extends AbstractAction {
   ApplyAction () {
       super ("");
   }
 
     public void actionPerformed (ActionEvent e) {
 	
 	Object o;
 	
 	Color node = nColor.getColor();
 	if(node != null)
 	    o = aMapper.setDefaultValue(VizMapperCategories.NODE_FILL_COLOR, node);
 	Color bg = bgColor.getColor();
 	if(bg != null)
 	    o = aMapper.setDefaultValue(VizMapperCategories.BG_COLOR, bg);
 	Byte shape = (Byte)shapeDefault.getIconObject();
 	if(shape != null)
 	    o = aMapper.setDefaultValue(VizMapperCategories.NODE_SHAPE, shape);
 	LineType line = (LineType)lineTypeDefault.getIconObject();
 	if(line != null)
	    o = aMapper.setDefaultValue(VizMapperCategories.EDGE_LINETYPE, null);
 	Integer size = sizeDefault.getInteger();
 	if(size != null) {
 	    o = aMapper.setDefaultValue(VizMapperCategories.NODE_HEIGHT, size);
 	    o = aMapper.setDefaultValue(VizMapperCategories.NODE_WIDTH, size);
 	}
 	Color border = bColor.getColor();
 	if(border != null)
 	    o = aMapper.setDefaultValue(VizMapperCategories.NODE_BORDER_COLOR, border);
 	Arrow arrow = (Arrow)arrowDefault.getIconObject();
 	if(arrow != null)
 	    o = aMapper.setDefaultValue(VizMapperCategories.EDGE_TARGET_DECORATION, arrow);
 	
       /*
       EdgeArrowColor.removeThenAddEdgeColor(aMapper,"pp",ppColor.getColor());
       EdgeArrowColor.removeThenAddEdgeColor(aMapper,"pd",pdColor.getColor());
       */
 
       if(edgeTextPanel.getWhetherToUseTheMap()) {
 	  Map m = edgeTextPanel.getMap();
 	  if(m != null) {
 	      aMapper.setAttributeMapEntry(VizMapperCategories.EDGE_COLOR,
 					   localEdgeKey.getString(),
 					   new DiscreteMapper(m));
 
 	  }
       }
       parentNodeLabelKey.setString(localNodeLabelKey.getString());
       applied.setBool(true);
       VisualPropertiesDialog.this.dispose ();
   }
 
 } // ApplyAction
 
 public class CancelAction extends AbstractAction {
   CancelAction () { super (""); }
   public void actionPerformed (ActionEvent e) {
       applied.setBool(false);
       VisualPropertiesDialog.this.dispose ();
   }
 } // CancelAction
 
 public class BorderedPanel {
     JPanel panel;
     GridBagLayout layout;
     GridBagConstraints constraints;
     BorderedPanel(String title) {
 	panel = new JPanel();
 	layout = new GridBagLayout(); 
 	constraints = new GridBagConstraints();
 	panel.setLayout (layout);
   
 	Border border = BorderFactory.createLineBorder (Color.black);
 	Border titledBorder = 
 	    BorderFactory.createTitledBorder (border,
 					      title,
 					      TitledBorder.CENTER, 
 					      TitledBorder.DEFAULT_POSITION);
 	panel.setBorder (titledBorder);
     }
     public JPanel getPanel() { return panel; }
     public GridBagLayout getLayout() { return layout; }
     public GridBagConstraints getConstraints() { return constraints; }
 }
 
     private Color getBasicColor(Integer category) {
 	Color tempColor;
 	try {
 	    tempColor = 
 		(Color)aMapper.getDefaultValue(category);
 	} catch (NullPointerException ex) {
 	    tempColor = new Color(255,255,255);
 	}
 	if(tempColor != null)
 	    return tempColor;
 	else {
 	    return new Color(255,255,255);
 	}
     }
 
     private Color getDMColor(Integer category, String key) {
 	Color tempColor;
 
 	DiscreteMapper dm =
 	    (DiscreteMapper)
 	    aMapper.getValueMapper(category);
 
 	Map valueMap = dm.getValueMap();
 	tempColor = (Color)valueMap.get(key);
 
 	if(tempColor != null)
 	    return tempColor;
 	else {
 	    return new Color(255,255,255);
 	}
     }
 
     /**
      *  For each icon-list of attributes, there is an IconPopupButton,
      *  initialized through a function called
      *     initialize<Whatever>Default()
      *  (several instances of this sort of function follow this comment).
      *
      *  Construction of an IconPopupButton requires a title for the button,
      *  a name of the attribute in question, two hashes for going from
      *  Object to String and back again, a list of ImageIcon's, a current
      *  Object that is selected, and a reference to this, the parent dialog.
      *
      *  Each different icon-list of attributes also depends on a few
      *  functions from MiscDialog.java.  These functions create the
      *  HashMaps and the list of ImageIcons.
      *
      *  Finally, the value in each of these IconPopupButton's is taken and
      *  used during ApplyAction's actionPerformed(), above.
      *
      */
 
     private void initializeArrowDefault() {
 	int ns = ((Integer)aMapper.getDefaultValue(VizMapperCategories.NODE_HEIGHT)).intValue();
 	Object currentArrow =  aMapper.getDefaultValue(VizMapperCategories.EDGE_TARGET_DECORATION);
 	HashMap arrowToString = MiscDialog.getArrowToStringHashMap(ns);
 	HashMap stringToArrow = MiscDialog.getStringToArrowHashMap(ns);
 	ImageIcon [] icons = MiscDialog.getArrowIcons();
 	arrowDefault =
 	    new IconPopupButton ("Arrow",
 				 "Arrow",
 				 arrowToString,
 				 stringToArrow,
 				 icons,
 				 currentArrow,
 				 this);
     }
 
 
     private void initializeShapeDefault() {
 	Object currentShape =  aMapper.getDefaultValue(VizMapperCategories.NODE_SHAPE);
 	HashMap shapeToString = MiscDialog.getShapeByteToStringHashMap();
 	HashMap stringToShape = MiscDialog.getStringToShapeByteHashMap();
 	ImageIcon [] icons = MiscDialog.getShapeIcons();
 	shapeDefault =
 	    new IconPopupButton ("Node Shape",
 				 "Node Shape",
 				 shapeToString,
 				 stringToShape,
 				 icons,
 				 currentShape,
 				 this);
     }
 
 
     private void initializeLineTypeDefault() {
 	Object currentLineType =  aMapper.getDefaultValue(VizMapperCategories.EDGE_LINETYPE);
 	HashMap lineTypeToString = MiscDialog.getLineTypeToStringHashMap();
 	HashMap stringToLineType = MiscDialog.getStringToLineTypeHashMap();
 	ImageIcon [] icons = MiscDialog.getLineTypeIcons();
 	lineTypeDefault =
 	    new IconPopupButton ("Line Type",
 				 "Line Type",
 				 lineTypeToString,
 				 stringToLineType,
 				 icons,
 				 currentLineType,
 				 this);
     }
 
 
 } // class VisualPropertiesDialog
