 package draw;
 
 /* Draw.java
  * drawing GUI design
  * @Author - Steven Horowitz
  */
 
 import javax.swing.*;
 
 import draw.primitives.*;
 import draw.primitives.Rectangle.CornerStyle;
 
 import java.awt.*;
 import java.awt.Rectangle;
 import java.awt.event.*;
 import java.awt.geom.*;
 import java.util.*;
 import draw.operations.*;
 
 public class UserInterface extends JFrame implements ActionListener, Observer{
     
 	Create rect_create;
 	Create line_create;
 	Delete delete;
 	Select select;
 	Move move;
 	Copy copy;
     JMenu file;
     JMenu edit; 
     JMenu create;
     JButton moveButton;
     JButton selectButton;
     JButton createLine;
     JButton createRectangle;
     DrawingPanel panel;
     public Canvas myCanvas;
     Color defaultBackground;
     JFileChooser fc;
     
     /*
      * public class draw
      * @param name - The name of the object to be displayed on the title bar of the frame.
      */
     public UserInterface(String name){
         super(name);
         fc = new JFileChooser("SCRAW");
         setSize(400,480);
         setResizable(false);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         // Creates a generic JPanel
         panel = new DrawingPanel();
         
         
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
         JMenuItem groupMenu = new JMenuItem("Group");
         JMenuItem open = new JMenuItem("Open");
         
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
         groupMenu.addActionListener(this);
         open.addActionListener(this);
         
         //we need to add all the created items to the GUI here.
         file.add(open);
         file.add(save);
         file.add(quit);
         edit.add(properties);
         edit.add(groupMenu);
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
         panel.paintComponent(myCanvas);
     }
     
     /*
      * public attachCanvas
      * attaches a canvas for the class to observe.
      */
     @SuppressWarnings("unchecked")
 	public void attachCanvas(Canvas canvas){
     	myCanvas = canvas;
         rect_create = new Create(panel,myCanvas,new draw.primitives.Rectangle(new Point(),new Point()).getClass());
     	line_create = new Create(panel,myCanvas,new Line(null,null).getClass());
     	delete = new Delete(panel,myCanvas);
     	move = new Move(panel,myCanvas);
     	select = new Select(panel, myCanvas);
     	copy = new Copy(panel, myCanvas);
         panel.addKeyListener(delete);
         panel.addKeyListener(copy);
     }
     
     // test main object
     public static void main (String[]args){
     	Canvas myCanvas = new Canvas();
         UserInterface draw = new UserInterface("Draw");
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
         resetButtons();
         removeListeners();
         if (command == "Open"){
         	Save save = new Save(this);
         	ArrayList<DrawingPrimitive> myPrimitives = save.load();
         	if(myPrimitives.size()!=0)
         		for(int i = 0; i<myPrimitives.size();i++)
         			myCanvas.add(myPrimitives.get(i));
         }
         if (command == "Copy"){
         	copy.copySelected();
         }
         if (command == "Save"){
         	Save mySave = new Save(this);
         	mySave.saveFile();
         }
         if (command == "Quit")
             System.exit(0);
         if (command == "Line" || command == "Create Line"){
         	swapColor(createLine);
         	
         	panel.addMouseListener(line_create);
         	panel.addMouseMotionListener(line_create);
             update(myCanvas,null);
         }
         
         if (command == "Rectangle" || command == "Create Rectangle") {
         	swapColor(createRectangle);
     
         	panel.addMouseListener(rect_create);
         	panel.addMouseMotionListener(rect_create);
             update(myCanvas,null);
         }
         	
         if (command == "Properties"){
         	ArrayList<DrawingPrimitive> selection = myCanvas.getSelected();
         	if(!selection.isEmpty())
         	{
 	        	DrawingPrimitive d = selection.get(0);
 	        	String className = d.getClass().toString();
 	        	if(className.equals("class draw.primitives.Line"))
 	        	{
 		            editLine edit = new editLine(d, panel, myCanvas, "Edit Line");
 		            edit.setVisible(true);
 	        	}
 	        	else if(className.equals("class draw.primitives.Rectangle"))
 	        	{
 		            editRectangle edit = new editRectangle(d, panel, myCanvas, "Edit Rectangle");
 		            edit.setVisible(true);
 	        	}
         	}
         	else
         	{
         		//TODO: warning
         	}
         }
         if (command == "Delete"){
             delete.deleteSelected();
             
         }
         if (command == "Move"){
         	swapColor(moveButton);
         	panel.addMouseListener(move);
         	panel.addMouseMotionListener(move);
         }
         else if (command == "Select"){
         	swapColor(selectButton);
         	panel.addMouseListener(select);
         	panel.addKeyListener(select);
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
     
     public void removeListeners(){
     	panel.removeMouseListener(line_create);
     	panel.removeMouseMotionListener(line_create);
     	panel.removeMouseListener(rect_create);
     	panel.removeMouseMotionListener(rect_create);
     	panel.removeMouseMotionListener(move);
     	panel.removeMouseListener(select);
     	panel.removeMouseListener(move);
     	panel.removeKeyListener(select);
     }
     
     public void swapColor(JButton button){
     	if(button.getBackground()==defaultBackground)
     		button.setBackground(Color.gray);
     	else
     		button.setBackground(defaultBackground);		
     }
     
     public void paint(Graphics g){
     	super.paint(this.getGraphics());
     	panel.paintComponent(myCanvas);
     }
 }
 
 /*
  * editLine.java
  * @author Steven Horowitz
  * This is a sub-class for a properties J-Frame, it is the basic edit panel for a line segment.
  */
 class editLine extends JFrame implements ActionListener{
     protected ButtonGroup group;
     protected JButton done = new JButton("Done");
     protected DrawingPrimitive primitive;
     protected DrawingPanel panel;
     protected Canvas canvas;
     
     /*
      * public editLine
      * @param name - the name of the panel that is displayed on the title bar.
      * This panel allows us to modify the color of strings with a radio button.
      */
     public editLine(DrawingPrimitive primitive, DrawingPanel panel, Canvas canvas, String name){
         super(name);
         this.panel = panel;
         this.primitive = primitive;
         this.canvas = canvas;
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
     public void actionPerformed(ActionEvent arg0)
     {
     	String col = group.getSelection().getActionCommand();
     	Color newColor = Color.BLACK;
     	System.out.println(col);
     	if(col.equals("black"))
     		newColor = Color.BLACK;
     	else if(col.equals("red"))
     		newColor = Color.RED;
     	else if(col.equals("blue"))
     		newColor = Color.BLUE;
     	else if(col.equals("green"))
     		newColor = Color.GREEN;
     	this.primitive.setColor(newColor);
         this.setVisible(false);
         panel.paintComponent(canvas);
     }
 }
 
 /*
  * editRectangle.java
  * @author Steven Horowitz
  * a sub-class of the editLine pane; has a few more features making it slightly more sophisticated.
  */
 class editRectangle extends editLine implements ActionListener{
     ButtonGroup edges = new ButtonGroup();
     DrawingPrimitive primitive;
     
     /*
      * public editRectangle
      * @param name- the name of the window to be displayed in the title bar of the frame.
      * This allows us to edit simple rectangles.
      */
     public editRectangle(DrawingPrimitive primitive, DrawingPanel panel, Canvas canvas, String name){
         super(primitive, panel, canvas, name);
         this.primitive = primitive;
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
     	draw.primitives.Rectangle editRect = (draw.primitives.Rectangle)this.primitive;
         String edges = this.edges.getSelection().getActionCommand();
         if(edges.equals("hard"))
         	editRect.setCornerStyle(CornerStyle.Sharp);
         else if(edges.equals("rounded"))
         	editRect.setCornerStyle(CornerStyle.Rounded);
     	super.actionPerformed(e);
     }
 }
