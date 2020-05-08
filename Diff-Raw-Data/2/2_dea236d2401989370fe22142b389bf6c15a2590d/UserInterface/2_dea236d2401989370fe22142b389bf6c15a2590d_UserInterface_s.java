 package draw;
 
 /* Draw.java
  * drawing GUI design
  * @Author - Steven Horowitz
  */
 import javax.swing.*;
 
 import draw.primitives.*;
 import java.awt.*;
 import java.awt.Rectangle;
 import java.awt.event.*;
 import java.awt.geom.*;
 import java.util.*;
 
 public class UserInterface extends JFrame implements ActionListener, Observer{
     
     JMenu file;
     JMenu edit; 
     JMenu create;
     JButton moveButton;
     JButton selectButton;
     JButton createLine;
     JButton createRectangle;
     drawingPanel panel;
     public static boolean selected = false;
     Canvas myCanvas;
     Color defaultBackground;
     
     public static ArrayList<Line2D> lines;
     
     /*
      * public class draw
      * @param name - The name of the object to be displayed on the title bar of the frame.
      */
     public UserInterface(String name){
         super(name);
         lines = new ArrayList<Line2D>();
         setSize(400,480);
         setResizable(false);
         panel = new drawingPanel();
         JMenuBar menuBar = new JMenuBar();
         file = new JMenu("File");
         edit = new JMenu("Edit");
         create = new JMenu("Create");
         
         // the menu items; these are the selectable options in the top menu bar.
         JMenuItem deleteMenu = new JMenuItem("Delete");
         JMenuItem save = new JMenuItem("Save");
         JMenuItem quit = new JMenuItem("Quit");
         JMenuItem line = new JMenuItem("Line");
         JMenuItem rectangle = new JMenuItem("Rectangle");
         JMenuItem properties = new JMenuItem("Properties");
         JMenuItem copyMenu = new JMenuItem("Copy");
         
         //the buttons that allow you to add items without going through the menu.
         createLine = new JButton("Create Line");
         createRectangle = new JButton("Create Rectangle");
         JButton copyButton = new JButton("Copy");
         JButton deleteButton = new JButton("Delete");
         moveButton = new JButton("Move");
         selectButton = new JButton("Select");
         JButton group = new JButton("Group/Ungroup");
         defaultBackground = selectButton.getBackground();
         
         //add actionlisteners from jordan here.
         deleteMenu.addActionListener(this);
         deleteButton.addActionListener(this);
         copyMenu.addActionListener(this);
         copyButton.addActionListener(this);
         save.addActionListener(this);
         quit.addActionListener(this);
         line.addActionListener(this);
         rectangle.addActionListener(this);
         properties.addActionListener(this);
         createLine.addActionListener(this);
         createRectangle.addActionListener(this);
         selectButton.addActionListener(this);
         moveButton.addActionListener(this);
         group.addActionListener(this);
         
         //we need to add all the created items to the GUI here.
         file.add(save);
         file.add(quit);
         edit.add(properties);
         edit.add(deleteMenu);
         edit.add(copyMenu);
         create.add(line);
         create.add(rectangle);
         menuBar.add(file);
         menuBar.add(edit);
         menuBar.add(create);
         
         setLayout(null );
         add(menuBar);
         add(createLine);
         add(createRectangle);
         add(copyButton);
         add(deleteButton);
         add(moveButton);
         add(selectButton);
         add(group);
         add(panel);
         
         // Because we set the layout to null, we must manually set the bounds for the objects.
         createLine.setBounds(new Rectangle(0,50,133,20));
         createRectangle.setBounds(new Rectangle(133,50,133,20));
         group.setBounds(new Rectangle(266,50,134,20));
         deleteButton.setBounds(new Rectangle(200,30,100,20));
         copyButton.setBounds(new Rectangle(300,30,100,20));
         menuBar.setBounds(new Rectangle(0,0,400,30));
         moveButton.setBounds(new Rectangle(0,30,100,20));
         selectButton.setBounds(new Rectangle(100,30,100,20));
         panel.setBounds(new Rectangle(0,70,400,400));
         
     }
     
     /*
      * public update
      * calls the repaint method on the view class, the panel.
      */
     public void update(Observable myCanvas, Object arg1){
         panel.paintComponent(panel.getGraphics(),myCanvas);
     }
     
     /*
      * public attachCanvas
      * attaches a canvas for the class to observe.
      */
     public void attachCanvas(Canvas canvas){
     	myCanvas = canvas;
     }
     
     // test main object
     public static void main (String[]args){
         UserInterface draw = new UserInterface("Draw");
         Canvas myCanvas = new Canvas();
         draw.attachCanvas(myCanvas);
         draw.setVisible(true);
     }
 
     /*
      * public actionPerformed
      * called when a button or menu item is clicked, this will handle those cases. Likely will be replaced
      * with more sophisticated methods by someone handling the action listeners.
      */
     public void actionPerformed(ActionEvent arg0) {
         String command = arg0.getActionCommand();
         System.out.println(command);
         resetButtons();
         if (command == "Quit")
             System.exit(0);
         if (command == "Line" || command == "Create Line"){
             update(myCanvas,null);
         }
         if (command == "Create Line")
         	swapColor(createLine);
         
         if (command == "Create Rectangle")
         	swapColor(createRectangle);
         
         if (command == "Properties"){
             editRectangle edit = new editRectangle("Edit line");
             edit.setVisible(true);
         }
         if (command == "Delete"){
             UserInterface.lines.remove(0);
             update(null, null);
         }
         if (command == "Move"){
         	swapColor(moveButton);
         }
         else if (command == "Select"){
         	swapColor(selectButton);
         }
     }
     
     /*
      * public void resetButtons()
      * resets the buttons to a base state.
      */
     public void resetButtons(){
     	moveButton.setBackground(defaultBackground);
     	selectButton.setBackground(defaultBackground);
     	createLine.setBackground(defaultBackground);
     	createRectangle.setBackground(defaultBackground);
     }
     
     public void swapColor(JButton button){
     	if(button.getBackground()==defaultBackground)
     		button.setBackground(Color.gray);
     	else
     		button.setBackground(defaultBackground);		
     }
 }
 
 /*
  * drawingPanel.java
  * @author Steven Horowitz
  * This is a sub-class that handles the drawing of all objects on the screen.
  */
 class drawingPanel extends JPanel implements MouseListener, MouseMotionListener{
     public drawingPanel(){
         setSize(400,400);
         this.setBackground(Color.white);
         addMouseListener(this);
         addMouseMotionListener(this);
     }
     
     /*
      * paintComponents
      * @param g - the graphics object for the class; handles drawing of all objects.
      * This is the method where all items are drawn in.
      */
     public void paintComponent(Graphics g, Observable myCanvas){
         super.paintComponent(g);
         Canvas newCanvas = (Canvas) myCanvas;
        ArrayList<DrawingPrimitive> myPrimitives = newCanvas.getElements();
         DrawingPrimitive newPrimitive = new Line(new Point(0,0),new Point(100,100));
         myPrimitives.add(newPrimitive);
         for (int i = 0;i<myPrimitives.size(); i++)
         	myPrimitives.get(i).draw((Graphics2D)g);
     }
 
     /*
      * public mouseClicked
      * @param arg0- the click of a mouse
      * This method registers any and all mouse clicks. Will likely be replaced by a more involved action handler.
      */
     public void mouseClicked(MouseEvent arg0) {
         UserInterface.selected = !UserInterface.selected;
     }
 
     // unimplmeneted methods
     public void mouseEntered(MouseEvent arg0) {}
     public void mouseExited(MouseEvent arg0) {}
     public void mousePressed(MouseEvent arg0) {}
     public void mouseReleased(MouseEvent arg0) {}
     public void mouseDragged(MouseEvent arg0) {}
     // end unimplemented methods.
     
     /*
      * public mouseMoved -
      * @param arg0 - the mouse's current position data.
      * This will update every time the mouse shifts position. Useful for moving objects around on the screen. Should call
      * repaint when an ojbect moves, so that the movement can be tracked in the view.
      */
     public void mouseMoved(MouseEvent arg0) {
         if(UserInterface.selected)
             System.out.println(arg0);
     }
 }
 
 /*
  * editLine.java
  * @author Steven Horowitz
  * This is a sub-class for a properties J-Frame, it is the basic edit panel for a line segment.
  */
 class editLine extends JFrame implements ActionListener{
     ButtonGroup group;
     JButton done = new JButton("Done");
     
     /*
      * public editLine
      * @param name - the name of the panel that is displayed on the title bar.
      * This panel allows us to modify the color of strings with a radio button.
      */
     public editLine(String name){
         super(name);
         this.setFocusable(true);
         setSize(200,250);
         setLayout(new GridLayout(0,1));
         JRadioButton black = new JRadioButton("Black");
         black.setSelected(true);
         JRadioButton blue = new JRadioButton("Blue");
         JRadioButton green = new JRadioButton("Green");
         JRadioButton red = new JRadioButton("Red");
         group = new ButtonGroup();
         done.addActionListener(this);
         group.add(red);
         group.add(green);
         group.add(blue);
         group.add(black);
         black.setActionCommand("black");
         red.setActionCommand("red");
         blue.setActionCommand("blue");
         green.setActionCommand("green");
         add(black);
         add(blue);
         add(green);
         add(red);
         add(done);
     }
     
     /*
      * public actionPerformed
      * @param arg0 - the button press that called this method.
      * allows us to close this window, and identify which color the user has chosen for the line segment.
      */
     public void actionPerformed(ActionEvent arg0) {
         System.out.println(group.getSelection().getActionCommand());
         this.setVisible(false);
     }
 }
 
 /*
  * editRectangle.java
  * @author Steven Horowitz
  * a sub-class of the editLine pane; has a few more features making it slightly more sophisticated.
  */
 class editRectangle extends editLine implements ActionListener{
     ButtonGroup edges = new ButtonGroup();
     
     /*
      * public editRectangle
      * @param name- the name of the window to be displayed in the title bar of the frame.
      * This allows us to edit simple rectangles.
      */
     public editRectangle(String name){
         super(name);
         remove(done);
         setSize(200,350);
         JRadioButton rounded = new JRadioButton ("Rounded Edges");
         JRadioButton hard = new JRadioButton ("Hard Edges");
         edges.add(rounded);
         hard.setSelected(true);
         edges.add(hard);
         rounded.setActionCommand("rounded");
         hard.setActionCommand("hard");
         add(hard);
         add(rounded);
         add(done);
     }
 
     /*
      * public actionPerformed
      * @param e - If the done button is pressed, this is called.
      * This method allows us to retrieve information from the user about the rectangle's formatting.
      */
     public void actionPerformed(ActionEvent e) {
         System.out.println(edges.getSelection().getActionCommand());
         super.actionPerformed(e);
     }
 }
