 // ShapePopupButton.java
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
 
 public class ShapePopupButton extends JPanel implements ActionListener {
 
     String title;                    // title of the button
     String currentShape;             // internal storage of node shape type
     Byte   currentShapeByte;         // and here as a shape byte
     JButton shapeButton;             // the button on the left
     JLabel shapeSelectionForPanel;   // the shape type displayed on the right
     JFrame mainFrame;
     JDialog mainDialog;
     JPanel mainPanel;
     HashMap shape2byte;       // The hash of shape type strings to Realizer bytes
     HashMap byte2shape;       // ... going the opposite direction
     JList shapeList;          // ... and the equivalent list for display
     int numberOfShapeItems;
     String rootPath;           // location of cytoscape class files
     JDialog parentDialog;
     boolean alreadyConstructed;
 
     public ShapePopupButton (String title, String startShape, JDialog parentDialog){
 	alreadyConstructed = false;
 	this.parentDialog = parentDialog;
 	setupShapeMap();
 	this.title = title;
 	this.setShape(startShape);
 	setupWindow();
     }
     public ShapePopupButton (String title, byte startShapeByte, JDialog parentDialog){
 	alreadyConstructed = false;
 	this.parentDialog = parentDialog;
 	setupShapeMap();
 	this.title = title;
 	this.setShapeByte(new Byte(startShapeByte));
 	setupWindow();
     }
     private void setupWindow(){
 	// find the right icon
 	ListModel theModel = shapeList.getModel();
 	ImageIcon icon = (ImageIcon)theModel.getElementAt(0); // default icon
 	int modelSize = theModel.getSize();
 	for (int modelIndex = 0; modelIndex < modelSize; modelIndex++) {
 	    ImageIcon indexedIcon = (ImageIcon)theModel.getElementAt(modelIndex);
 	    if(currentShape == indexedIcon.getDescription()) {
 		icon = indexedIcon;
 		shapeList.setSelectedValue(icon,true);
 	    }
 	}
 
 	shapeButton = new JButton(title);	
 	shapeButton.addActionListener(this);
 	mainPanel = new JPanel(new GridLayout(0,1));
 	shapeSelectionForPanel = new JLabel(icon);
 	add(shapeButton);
 	add(shapeSelectionForPanel);
     }
 
     // builds shape2byte and byte2shape map, shapeList, and rootPath to images
     private void setupShapeMap(){
 
 	shape2byte = new HashMap();  // hash
 	shape2byte.put("RECTANGLE", new Byte (ShapeNodeRealizer.RECT));
 	shape2byte.put("DIAMOND", new Byte (ShapeNodeRealizer.DIAMOND));
 	shape2byte.put("ELLIPSE", new Byte (ShapeNodeRealizer.ELLIPSE));
 	shape2byte.put("HEXAGON", new Byte (ShapeNodeRealizer.HEXAGON));
 	shape2byte.put("TRAPEZOID", new Byte (ShapeNodeRealizer.TRAPEZOID));
 	shape2byte.put("TRIANGLE", new Byte (ShapeNodeRealizer.TRIANGLE));
 
 	byte2shape = new HashMap();  // reverse hash
 	byte2shape.put(new Byte (ShapeNodeRealizer.RECT), "RECTANGLE");
 	byte2shape.put(new Byte (ShapeNodeRealizer.DIAMOND), "DIAMOND");
 	byte2shape.put(new Byte (ShapeNodeRealizer.ELLIPSE), "ELLIPSE");
 	byte2shape.put(new Byte (ShapeNodeRealizer.HEXAGON), "HEXAGON");
 	byte2shape.put(new Byte (ShapeNodeRealizer.TRAPEZOID), "TRAPEZOID");
 	byte2shape.put(new Byte (ShapeNodeRealizer.TRIANGLE), "TRIANGLE");
 	
 	rootPath = System.getProperty ("CYTOSCAPE_HOME"); // get class directory root
 	if (rootPath.endsWith("/")) rootPath = rootPath.substring(0,rootPath.length()-1);
 	rootPath = rootPath + "/cytoscape/dialogs/images/";
 	rootPath = rootPath.substring(rootPath.lastIndexOf(":")+1);
 	String p = rootPath;
 	ImageIcon [] shapeIcons = new ImageIcon [6];  // Array of icons for the list
 	//System.out.println(p+"/dialogs/images/rectangle.jpg");
 	shapeIcons[0] = new ImageIcon(p+"rectangle.jpg", "RECTANGLE");
 	shapeIcons[1] = new ImageIcon(p+"diamond.jpg", "DIAMOND");
 	shapeIcons[2] = new ImageIcon(p+"ellipse.jpg", "ELLIPSE");
 	shapeIcons[3] = new ImageIcon(p+"hexagon.jpg", "HEXAGON");
 	shapeIcons[4] = new ImageIcon(p+"trapezoid.jpg", "TRAPEZOID");
 	shapeIcons[5] = new ImageIcon(p+"triangle.jpg", "TRIANGLE");
 
 	numberOfShapeItems = shape2byte.keySet().size();
 	String shapeStrings [];
 	shapeStrings = new String[numberOfShapeItems];
 	shapeStrings = (String[]) shape2byte.keySet().toArray(shapeStrings);
 	//shapeList = new JList(shapeStrings);
 	shapeList = new JList (shapeIcons);
     }
 
     // if button is pressed, launch window with list of choices
     public void actionPerformed(ActionEvent e){
 	if(!alreadyConstructed) {
 	mainDialog = new JDialog(parentDialog, this.title);
 
 	// create buttons
         final JButton setButton = new JButton("Apply");
         JButton cancelButton = new JButton("Dismiss");
 	setButton.addActionListener    (new ApplyShapeAction());
 	cancelButton.addActionListener (new DismissShapeAction()); 
 
 	// create list
 	shapeList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
 	shapeList.setVisibleRowCount(1);
 	//shapeList.setFixedCellHeight(35);
 	//shapeList.setFixedCellWidth(35);
 	shapeList.setBackground(Color.WHITE);
 	shapeList.setSelectionBackground(Color.RED);
 	shapeList.setSelectionForeground(Color.RED);
 	shapeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         shapeList.addMouseListener( new MouseAdapter() {
 		public void mouseClicked(MouseEvent e) {
 		    if (e.getClickCount() == 2) setButton.doClick();
 		}
 	    });
         JScrollPane listScroller = new JScrollPane(shapeList) ;
         listScroller.setPreferredSize(new Dimension(150, 50));
         listScroller.setMinimumSize(new Dimension(150,50));
 	listScroller.setAlignmentX(LEFT_ALIGNMENT);
 	listScroller.setAlignmentY(BOTTOM_ALIGNMENT);
 	shapeList.ensureIndexIsVisible(shapeList.getSelectedIndex());
 
 	// Create a container so that we can add a title around the scroll pane
         JPanel listPane = new JPanel();
         listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));
         JLabel label = new JLabel("Set shape:");
         label.setLabelFor(shapeList);
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
 
     public class ApplyShapeAction extends AbstractAction{   
 	public void actionPerformed(ActionEvent e){
             // System.out.println ("ShapePopupButton.actionPerformed: " + e);
 	    //setShape((String) shapeList.getSelectedValue());
 	    ImageIcon icon = (ImageIcon) shapeList.getSelectedValue();
 	    setShape(icon.getDescription());
 	    shapeSelectionForPanel.setIcon(icon);
 	    mainDialog.dispose();
 	}
     }
     public class DismissShapeAction extends AbstractAction{
 	DismissShapeAction(){super ("");}
 	public void actionPerformed (ActionEvent e){
 	    mainDialog.dispose();
 	}
     }
     public String getShape() {
 	return currentShape;
     }
     public Byte getShapeByte() {
 	return currentShapeByte;
     }
     public void setShape(String shape) {
 	currentShape = shape;
 	currentShapeByte = (Byte) shape2byte.get( (String) shape);
     }
     public void setShapeByte(Byte shapeByte) {
 	currentShapeByte = shapeByte;
 	currentShape = (String) byte2shape.get( (Byte) shapeByte);
     }
 
 }//class ShapePopupButton
