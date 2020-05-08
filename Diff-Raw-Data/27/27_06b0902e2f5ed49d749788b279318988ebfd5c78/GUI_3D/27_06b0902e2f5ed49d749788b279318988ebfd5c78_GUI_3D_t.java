 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.util.Hashtable;
 
 
 import javax.media.j3d.Canvas3D;
 import javax.media.j3d.Transform3D;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JTextArea;
 import javax.swing.JToolBar;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.LineBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.vecmath.Vector3d;
 
 import com.sun.j3d.utils.picking.PickCanvas;
 
 public class GUI_3D extends JPanel implements MouseListener, MouseMotionListener  {
 	
 	static float rotateSpeed = 0.0f;
 	
 	private static final long serialVersionUID = 1L;
 
 	public static SwingTest getSwingTest() {
 		return swingTest;
 	}
 
 	public static void setSwingTest(SwingTest swingTest) {
 		GUI_3D.swingTest = swingTest;
 	}
 	
 	private static SwingTest swingTest;
 	private static Canvas3D c3d;
 	
 	private JFrame frame;
 	
 	private static double zoomAmount = -10.0;
 	private static double zoomValue;
 	
 	// Menu
 	private JMenuBar menubar;
 	private JMenu file, edit, help;
 	private JMenuItem save, load, exit, blank, about;
 	
 	// Panels
 	private JPanel mainPanel, rightToolbar, currentShapes, rotatePane,
 			resizePane, aestheticsPane, centerPanel;
 	
 	// Shapes Toolbar
 	private JToolBar shapesToolbar;
 	private JButton rectPrism_b, triprism_b, pyramid_b, cylinder_b, sphere_b,
 			hexprism_b, line_b;
 	
 	private JTextArea logText;
 	private JScrollPane logScroll;
 	private JLabel statusBar;
 	
 	public GUI_3D() {
 		swingTest = new SwingTest();
 		c3d = swingTest.getC3d();
 		c3d.addMouseMotionListener(this);
 		c3d.addMouseListener(this);
 
 		init();
 	}
 
 	public final void init() {
 
 		frame = new JFrame("3D GUI");
 		frame.setSize(900, 700);
 		frame.setLocationRelativeTo(null);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		addMouseListener(this);
 		addMouseMotionListener(this);
 
 		menubar = new JMenuBar();
 		frame.setJMenuBar(menubar);
 
 		// File
 		file = new JMenu("File");// Items under File
 		edit = new JMenu("Edit");// Items under Edit
 		help = new JMenu("Help");
 
 		menubar.add(file);
 		menubar.add(edit);
 		menubar.add(Box.createHorizontalGlue()); // adheres Help menu to right side
 		menubar.add(help);
 
 		save = new JMenuItem("Save");
 		load = new JMenuItem("Load");
 		exit = new JMenuItem("Exit");
 
 		file.add(save);
 		file.add(load);
 		file.add(exit);
 
 		
 		// Edit
 		blank = new JMenuItem("Blank Button");
 		edit.add(blank);
 		
 
 		// Help
 		about = new JMenuItem("About");
 		help.add(about);
 		
 
 		// Adding the function of the action to the button
 		exit.addActionListener(new ExitAction());
 		about.addActionListener(new AboutAction());
 		blank.addActionListener(new BlankAction());
 		save.addActionListener(new SaveAction());
 		load.addActionListener(new LoadAction());
 
 		mainPanel = new JPanel();
 		mainPanel.setLayout(new BorderLayout());
 		frame.add(mainPanel);
 
 		
 		// creates left-hand toolbar
 		shapesToolbar = new JToolBar(JToolBar.VERTICAL);
 		shapesToolbar.setFloatable(false);
 		mainPanel.add(shapesToolbar, BorderLayout.LINE_START);
 
 		shapesToolbar.setLayout(new GridLayout(7, 1, 0, 10));
 		shapesToolbar.setBorder(LineBorder.createGrayLineBorder());
 
 		
 		// creates buttons for each shape
 		rectPrism_b = new JButton("[rect prism]");
 		triprism_b = new JButton("[tri prism]");
 		pyramid_b = new JButton("[pyramid]");
 		cylinder_b = new JButton("[cylinder]");
 		sphere_b = new JButton("[sphere]");
 		hexprism_b = new JButton("[hex prism]");
 		line_b = new JButton("[line]");
 		
 		
 		rectPrism_b.addActionListener(new ActionListener() {
 			 
             public void actionPerformed(ActionEvent e) {
                // System.out.println("Created: RectPrism");
                 swingTest.getSceneBranchGroup().addChild( swingTest.createRectPrism() );
             }
         });  
 		
 		triprism_b.addActionListener(new ActionListener() {
 			 
             public void actionPerformed(ActionEvent e) {
                 //System.out.println("Created: Triangular Prism");
                 swingTest.getSceneBranchGroup().addChild( swingTest.createTriPrism() );
             }
         });    
 		
 		pyramid_b.addActionListener(new ActionListener() {
 			 
             public void actionPerformed(ActionEvent e) {
                 //System.out.println("Created: Pyramid");
                 swingTest.getSceneBranchGroup().addChild( swingTest.createPyramid() );
                 //System.out.println("Shape clicked: " + swingTest.getShapeClicked());
                 
             }
         });    
 		
 		cylinder_b.addActionListener(new ActionListener() {
 			 
             public void actionPerformed(ActionEvent e) {
                 //System.out.println("Created: Cylinder");
                 //swingTest.getSceneBranchGroup().addChild( swingTest.createCylinder() );
             }
         });    
 		
 		sphere_b.addActionListener(new ActionListener() {
 			 
             public void actionPerformed(ActionEvent e) {
                 //System.out.println("Created: Sphere");
                 swingTest.getSceneBranchGroup().addChild( swingTest.createSphere() );
             }
         });    
 		
 		hexprism_b.addActionListener(new ActionListener() {
 			 
             public void actionPerformed(ActionEvent e) {
                 //System.out.println("Created: Hexagonal Prism");
                 swingTest.getSceneBranchGroup().addChild( swingTest.createHexPrism() );
             }
         });    
 
 		
 		// adds buttons to left-hand toolbar
 		shapesToolbar.add(rectPrism_b);
 		shapesToolbar.add(triprism_b);
 		shapesToolbar.add(pyramid_b);
 		shapesToolbar.add(cylinder_b);
 		shapesToolbar.add(sphere_b);
 		shapesToolbar.add(hexprism_b);
 		shapesToolbar.add(line_b);
 
 		
 		// creates right-hand toolbar
 		rightToolbar = new JPanel();
 		rightToolbar.setPreferredSize(new Dimension(150, 0));
 		// rightToolbar.setLayout(new GridLayout(4,1,0,15));
 		rightToolbar.setLayout(new BoxLayout(rightToolbar, BoxLayout.Y_AXIS));
 		rightToolbar.setBorder(LineBorder.createGrayLineBorder());
 
 		mainPanel.add(rightToolbar, BorderLayout.LINE_END);
 
 		
         //current shapes panel
         JPanel currentShapes = new JPanel();
         currentShapes.setPreferredSize(new Dimension(150, 100));
         rightToolbar.add(currentShapes);
         rightToolbar.add(Box.createVerticalGlue());
         currentShapes.setBorder(LineBorder.createGrayLineBorder());
         
         new CurrentShapesPanel(currentShapes);
         
         
         //rotation panel
         JPanel rotatePane = new JPanel();
         rotatePane.setMaximumSize(new Dimension(150, 190));
         rotatePane.setPreferredSize(new Dimension(150, 190));
         rightToolbar.add(rotatePane);
         rotatePane.setBorder(new EmptyBorder(0, 0, 0, 0) );
         rotatePane.setBorder(LineBorder.createGrayLineBorder());
         rightToolbar.add(Box.createVerticalGlue());
 	    
 		new RotatePanel(rotatePane);
         
 
         
         //resize panel
         JPanel resizePane = new JPanel();
         resizePane.setPreferredSize(new Dimension(100, 100));
         rightToolbar.add(resizePane);
         resizePane.setBorder(LineBorder.createGrayLineBorder());
         
         new ResizePanel(resizePane);
         
         
         
         //aesthetics panel
         JPanel aestheticsPane = new JPanel();
         aestheticsPane.setMaximumSize(new Dimension(150, 110));
         aestheticsPane.setPreferredSize(new Dimension(150, 110));
         rightToolbar.add(aestheticsPane);
         aestheticsPane.setBorder(LineBorder.createGrayLineBorder());
         
         new AestheticsPanel(aestheticsPane);
         
         
 
 		// creates center panel
 		centerPanel = new JPanel();
 		centerPanel.setLayout(new BorderLayout());
 
 		mainPanel.add(centerPanel, BorderLayout.CENTER);
 
 		centerPanel.add(c3d, BorderLayout.CENTER);
 		frame.setVisible(true);
 		
 
 		
 		
 		JPanel bottomCenter = new JPanel();
 		bottomCenter.setLayout(new BorderLayout());
 		
 		logText = new JTextArea(); // **LOGGER PANEL**
 		logText.setLineWrap(true);
 		logText.setBorder(LineBorder.createGrayLineBorder());
 		logScroll = new JScrollPane(logText);
 		logScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		logScroll.setPreferredSize(new Dimension(0, 150));
 
 		
 		JSlider zoom = new JSlider(JSlider.HORIZONTAL, 5, 200, 100);
 		
 		zoom.setSnapToTicks(true);
 		zoom.setMajorTickSpacing(25);
 		zoom.setMinorTickSpacing(5);
 		zoom.setPaintTicks(true);
 		//zoom.setPaintLabels(true);
 		
 		
 		
 		zoom.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				//System.out.println("Changing.");
 				JSlider source = (JSlider)e.getSource();
 				//double previousValue = source.getValue();
 				
 				//System.out.println("Zoom amount: " + zoomAmount);
 				
 				Transform3D t3d = new Transform3D();
 				
 			    if (((source.getValue())%5 == 0)) {
 			    	
 			    	System.out.println(zoomAmount);
 			    	
 			    	if (zoomAmount >= -10.0)
 			    		zoomAmount = -10 + (0.4)*((source.getValue()-100)/5);
 			    	else if (zoomAmount < -10.0)
 			    		zoomAmount = -10 - (4)*((100-source.getValue())/5);
 			    	
 		    		t3d.setTranslation( new Vector3d( 0.0, 0.0, zoomAmount ) );
 					 t3d.invert(); //moving the viewer, not the scene
 					 GUI_3D.getSwingTest().getTgArray().setTransform( t3d );
 			    }
 			}
 		});
 		
 		
 		
 		bottomCenter.add(logScroll, BorderLayout.PAGE_END);
 		JLabel lbl_log = new JLabel(" L O G G E R:");
 		lbl_log.setFont(new Font("sansserif",Font.BOLD,18));
 		bottomCenter.add(lbl_log, BorderLayout.LINE_START);
 		bottomCenter.add(zoom, BorderLayout.LINE_END);
 		centerPanel.add(bottomCenter, BorderLayout.PAGE_END);
 
 		statusBar = new JLabel();
 		statusBar.setText(" Cursor Position:  |  Selected: x  |  Total Shapes: x");
 		statusBar.setPreferredSize(new Dimension(-1, 22));
 		statusBar.setBorder(LineBorder.createGrayLineBorder());
 		mainPanel.add(statusBar, BorderLayout.PAGE_END);
 
 		frame.setVisible(true);
 	}
 
 	public void actionPerformed(ActionEvent e) { }
 
 	public static void main(String[] args) {
 		GUI_3D ex = new GUI_3D();
 		ex.setVisible(true);
 	}
 
 	class SaveAction implements ActionListener {// Action For Save goes here
 		// NOT COMPLETE
 		public void actionPerformed(ActionEvent e) {
 			JFrame saveFrame = new JFrame("Save");
 			saveFrame.setVisible(true);
 			saveFrame.setSize(200, 200);
 			JLabel label = new JLabel("YOU CLICKED SAVE");
 			JPanel panel = new JPanel();
 			saveFrame.add(panel);
 			panel.add(label);
 		}
 	}
 
 	class LoadAction implements ActionListener {// Action For Save goes here
 		// NOT COMPLETE
 		public void actionPerformed(ActionEvent e) {
 
 			JFileChooser chooser = new JFileChooser();
 			//CustomFileFilter filter = new CustomFileFilter();
 			//filter.addExtension("log"); // Only choose text files
 			//filter.setDescription("Log Files");
 			//chooser.setFileFilter(filter);
 			
 			int returnVal = chooser.showOpenDialog(null);
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				System.out.println("You chose to open this file: "
 						+ chooser.getSelectedFile().getName());
 			}
 		}
 	}
 
 	class ExitAction implements ActionListener {// Action For Exit
 		public void actionPerformed(ActionEvent e) {
 			System.exit(0);
 		}
 	}
 
 	class AboutAction implements ActionListener {// Action For About
 		public void actionPerformed(ActionEvent e) {
 			JFrame aboutFrame = new JFrame("About");
 			aboutFrame.setVisible(true);
 			aboutFrame.setSize(400, 250);
 
 			JTextArea aboutText = new JTextArea(
 					"This application was created by:"
 							+ "\n\nJennifer Hill\nRyan Kane\nDorothy Kirlew\n" +
 							"Donald Shaner\nand Sean Weber");
 			Font JTextFont = new Font("Verdana", Font.BOLD, 12);
 			aboutText.setFont(JTextFont);
 
 			aboutText.setPreferredSize(new Dimension(380, 250));
 			aboutText.setLineWrap(true);
 			aboutText.setWrapStyleWord(true);
 
 			JPanel panel = new JPanel();
 			aboutFrame.add(panel);
 			panel.add(aboutText);
 		}
 	}
 
 	class BlankAction implements ActionListener {// Blank action
 		public void actionPerformed(ActionEvent e) {
 			JFrame blankFrame = new JFrame("Blank");
 			blankFrame.setVisible(true);
 			blankFrame.setSize(200, 200);
 			JLabel label = new JLabel("Blank");
 			JPanel panel = new JPanel();
 			blankFrame.add(panel);
 			panel.add(label);
 		}
 	}
 
 	public JLabel getStatusbar() {
 		return statusBar;
 	}
 
 	public void setStatusbar(JLabel statusbar) {
 		this.statusBar = statusBar;
 	}
 
 	public void mouseDragged(MouseEvent arg0) {
		statusBar.setText(" Cursor Position: " + swingTest.getCurPos() + "  |  Selected: x  |  Total Shapes: x");
 	}
 		
 	public void mouseMoved(MouseEvent e){
		statusBar.setText(" Cursor Position: " + swingTest.getCurPos() + "  |  Selected: x  |  Total Shapes: x");
 		
 	}
 	
 	public void mouseClicked(MouseEvent arg0) { }
 	public void mouseEntered(MouseEvent arg0) { }
 	public void mouseExited(MouseEvent arg0) { }
 	public void mousePressed(MouseEvent arg0) { }
 	public void mouseReleased(MouseEvent e) { }	
 }
