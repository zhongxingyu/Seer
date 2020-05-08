 // LineTypePopupButton.java
 //---------------------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //---------------------------------------------------------------------------------------
 package cytoscape.dialogs;
 //---------------------------------------------------------------------------------------
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.event.*;
 import java.text.*;
 import java.util.*;
 import y.view.*;
 
 //---------------------------------------------------------------------------------------
 
 public class LineTypePopupButton extends JPanel implements ActionListener {
 
     String title;                    // title of the button
     String currentLineTypeName;             // internal storage of node shape type
     LineType   currentLineType;         // and here as a shape byte
     JButton shapeButton;             // the button on the left
     JLabel shapeSelectionForPanel;   // the shape type displayed on the right
     JFrame mainFrame;
     JDialog mainDialog;
     JPanel mainPanel;
     HashMap shape2byte;       // The hash of shape type strings to Realizer bytes
     HashMap byte2shape;       // ... going the opposite direction
     JList lineTypeList;          // ... and the equivalent list for display
     int numberOfLineTypeItems;
     String rootPath;           // location of cytoscape class files
     JDialog parentDialog;
     boolean alreadyConstructed;
 
     public LineTypePopupButton (String title, String startLineType, JDialog parentDialog){
 	alreadyConstructed = false;
 	this.parentDialog = parentDialog;
 	setupLineTypeMap();
	if(rootPath==null) return;
 	this.title = title;
 	this.setLineTypeName(startLineType);
 	setupWindow();
     }
     public LineTypePopupButton (String title, LineType startLineType, JDialog parentDialog){
 	alreadyConstructed = false;
 	this.parentDialog = parentDialog;
 	setupLineTypeMap();
	if(rootPath==null) return;
 	this.title = title;
 	this.setLineType(startLineType);
 	setupWindow();
     }
 
     private void setupWindow(){
 	// find the right icon
 	ListModel theModel = lineTypeList.getModel();
 	ImageIcon icon = (ImageIcon)theModel.getElementAt(0); // default icon
 	int modelSize = theModel.getSize();
 	for (int modelIndex = 0; modelIndex < modelSize; modelIndex++) {
 	    ImageIcon indexedIcon = (ImageIcon)theModel.getElementAt(modelIndex);
 	    if(currentLineTypeName == indexedIcon.getDescription()) {
 		icon = indexedIcon;
 		lineTypeList.setSelectedValue(icon,true);
 	    }
 	}
 	shapeButton = new JButton(title);	
 	shapeButton.addActionListener(this);
 	mainPanel = new JPanel(new GridLayout(0,1));
 	shapeSelectionForPanel = new JLabel(icon);
 	add(shapeButton);
 	add(shapeSelectionForPanel);
     }
 
     // builds shape2byte and byte2shape map, lineTypeList, and rootPath to images
     private void setupLineTypeMap(){
 
 	shape2byte = new HashMap();  // hash
 	shape2byte.put("DASHED_1", LineType.DASHED_1);
 	shape2byte.put("DASHED_2", LineType.DASHED_2);
 	shape2byte.put("DASHED_3", LineType.DASHED_3);
 	shape2byte.put("DASHED_4", LineType.DASHED_4);
 	shape2byte.put("DASHED_5", LineType.DASHED_5);
 	shape2byte.put("LINE_1", LineType.LINE_1);
 	shape2byte.put("LINE_2", LineType.LINE_2);
 	shape2byte.put("LINE_3", LineType.LINE_3);
 	shape2byte.put("LINE_4", LineType.LINE_4);
 	shape2byte.put("LINE_5", LineType.LINE_5);
 	shape2byte.put("LINE_6", LineType.LINE_6);
 	shape2byte.put("LINE_7", LineType.LINE_7);
 
 	byte2shape = new HashMap();  // reverse hash
 	byte2shape.put(LineType.DASHED_1, "DASHED_1");
 	byte2shape.put(LineType.DASHED_2, "DASHED_2");
 	byte2shape.put(LineType.DASHED_3, "DASHED_3");
 	byte2shape.put(LineType.DASHED_4, "DASHED_4");
 	byte2shape.put(LineType.DASHED_5, "DASHED_5");
 	byte2shape.put(LineType.LINE_1, "LINE_1");
 	byte2shape.put(LineType.LINE_2, "LINE_2");
 	byte2shape.put(LineType.LINE_3, "LINE_3");
 	byte2shape.put(LineType.LINE_4, "LINE_4");
 	byte2shape.put(LineType.LINE_5, "LINE_5");
 	byte2shape.put(LineType.LINE_6, "LINE_6");
 	byte2shape.put(LineType.LINE_7, "LINE_7");
 
 	rootPath = System.getProperty ("CYTOSCAPE_HOME");
	if(rootPath == null) {
	    JLabel errorLabel = new JLabel("ERROR: No CYTOSCAPE_HOME specified on java command line.");
	    add(errorLabel);
	    return;
	}
 	if (rootPath.endsWith("/")) rootPath = rootPath.substring(0,rootPath.length()-1);
 	rootPath = rootPath + "/cytoscape/dialogs/images/";
 	//rootPath = rootPath.substring(rootPath.lastIndexOf(":")+1);
 	String p = rootPath;
 	ImageIcon [] shapeIcons = new ImageIcon [12];  // Array of icons for the list
 	//System.out.println(p+"/dialogs/images/rectangle.jpg");
 	shapeIcons[0] = new ImageIcon(p+"line_1.jpg", "LINE_1");
 	shapeIcons[1] = new ImageIcon(p+"line_2.jpg", "LINE_2");
 	shapeIcons[2] = new ImageIcon(p+"line_3.jpg", "LINE_3");
 	shapeIcons[3] = new ImageIcon(p+"line_4.jpg", "LINE_4");
 	shapeIcons[4] = new ImageIcon(p+"line_5.jpg", "LINE_5");
 	shapeIcons[5] = new ImageIcon(p+"line_6.jpg", "LINE_6");
 	shapeIcons[6] = new ImageIcon(p+"line_7.jpg", "LINE_7");
 	shapeIcons[7] = new ImageIcon(p+"dashed_1.jpg", "DASHED_1");
 	shapeIcons[8] = new ImageIcon(p+"dashed_2.jpg", "DASHED_2");
 	shapeIcons[9] = new ImageIcon(p+"dashed_3.jpg", "DASHED_3");
 	shapeIcons[10] = new ImageIcon(p+"dashed_4.jpg", "DASHED_4");
 	shapeIcons[11] = new ImageIcon(p+"dashed_5.jpg", "DASHED_5");
 
 	numberOfLineTypeItems = shape2byte.keySet().size();
 	String shapeStrings [];
 	shapeStrings = new String[numberOfLineTypeItems];
 	shapeStrings = (String[]) shape2byte.keySet().toArray(shapeStrings);
 	//lineTypeList = new JList(shapeStrings);
 	lineTypeList = new JList (shapeIcons);
     }
 
     // if button is pressed, launch window with list of choices
     public void actionPerformed(ActionEvent e){
 	if(!alreadyConstructed) {
 	mainDialog = new JDialog(parentDialog, this.title);
 
 	// create buttons
         final JButton setButton = new JButton("Apply");
         JButton cancelButton = new JButton("Dismiss");
 	setButton.addActionListener    (new ApplyLineTypeAction());
 	cancelButton.addActionListener (new DismissLineTypeAction()); 
 
 	// create list
 	lineTypeList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
 	lineTypeList.setVisibleRowCount(1);
 	//lineTypeList.setFixedCellHeight(35);
 	//lineTypeList.setFixedCellWidth(35);
 	lineTypeList.setBackground(Color.WHITE);
 	lineTypeList.setSelectionBackground(Color.RED);
 	lineTypeList.setSelectionForeground(Color.RED);
 	lineTypeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         lineTypeList.addMouseListener( new MouseAdapter() {
 		public void mouseClicked(MouseEvent e) {
 		    if (e.getClickCount() == 2) setButton.doClick();
 		}
 	    });
         JScrollPane listScroller = new JScrollPane(lineTypeList) ;
         listScroller.setPreferredSize(new Dimension(150, 50));
         listScroller.setMinimumSize(new Dimension(150,50));
 	listScroller.setAlignmentX(LEFT_ALIGNMENT);
 	listScroller.setAlignmentY(BOTTOM_ALIGNMENT);
 	lineTypeList.ensureIndexIsVisible(lineTypeList.getSelectedIndex());
 
 	// Create a container so that we can add a title around the scroll pane
         JPanel listPane = new JPanel();
         listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));
         JLabel label = new JLabel("Set shape:");
         label.setLabelFor(lineTypeList);
         listPane.add(label);
         listPane.add(Box.createRigidArea(new Dimension(0,5)));
         listPane.add(listScroller);
         listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
 
 	// Lay out the buttons from left to right.
         JPanel buttonPane = new JPanel();
         buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
         buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
         buttonPane.add(Box.createHorizontalGlue());
         buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
         buttonPane.add(setButton);
         buttonPane.add(cancelButton);
 
 	// add everything
 	mainPanel.add(listPane, BorderLayout.CENTER);
 	mainPanel.add(buttonPane, BorderLayout.SOUTH);
 	mainDialog.setContentPane (mainPanel);
 	alreadyConstructed = true;
 	}
 	
 	mainDialog.pack ();
 	mainDialog.setLocationRelativeTo (parentDialog);
   	mainDialog.setVisible(true);
     }
 
     public class ApplyLineTypeAction extends AbstractAction{   
 	public void actionPerformed(ActionEvent e){
             // System.out.println ("LineTypePopupButton.actionPerformed: " + e);
 	    //setLineType((String) lineTypeList.getSelectedValue());
 	    ImageIcon icon = (ImageIcon) lineTypeList.getSelectedValue();
 	    setLineTypeName(icon.getDescription());
 	    shapeSelectionForPanel.setIcon(icon);
 	    mainDialog.dispose();
 	}
     }
     public class DismissLineTypeAction extends AbstractAction{
 	DismissLineTypeAction(){super ("");}
 	public void actionPerformed (ActionEvent e){
 	    mainDialog.dispose();
 	}
     }
     public String getLineTypeName() {
 	return currentLineTypeName;
     }
     public LineType getLineType() {
 	return currentLineType;
     }
     public void setLineTypeName(String linetype) {
 	currentLineTypeName = linetype;
 	currentLineType = (LineType)shape2byte.get( (String) linetype);
     }
     public void setLineType(LineType lineType) {
 	currentLineType = lineType;
 	currentLineTypeName = (String) byte2shape.get( (LineType) lineType);
     }
 
 }//class LineTypePopupButton
