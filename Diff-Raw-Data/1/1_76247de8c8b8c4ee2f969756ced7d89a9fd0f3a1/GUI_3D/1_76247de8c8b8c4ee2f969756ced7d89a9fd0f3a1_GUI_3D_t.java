 /*	3D Geometric Object Rendering Application
     Copyright (C) 2011  Jennifer Hill, Ryan Kane, Sean Weber, Donald Shaner, Dorothy Kirlew
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>. */
 
 import java.io.*;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.util.Calendar;
 import java.util.Hashtable;
 import java.util.Scanner;
 
 import javax.imageio.ImageIO;
 import javax.media.j3d.Canvas3D;
 import javax.media.j3d.Transform3D;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
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
 
 public class GUI_3D extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
 
 	// COMMANDS:
 	// Move, Rotate, Scale, Resize, Zoom
 	private final String create = "cre", move = "mov", rotate = "rot",
 			scale = "scl", resize = "rsz", zoom = "zom";
 
 	// SHAPES (Solids):
 	// Prisms: Rectangular, Triangular, & Hexagonal
 	// Square Pyramid
 	// Cylinder
 	// Sphere
 	private final String rectangle = "rec", triangle = "tri", hexagon = "hex",
 			pyramid = "pyr", cylinder = "cyl", sphere = "sph";
 
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
 	
 	private AestheticsPanel aestheticsPanel;
 	private CurrentShapesPanel currentShapesPanel;
 
 	// Shapes Toolbar
 	private JToolBar shapesToolbar;
 	private JButton btn_triPri, btn_recPri, btn_hexPri, btn_pyramid,
 			btn_cylinder, btn_sphere;
 	private Image img_triPri, img_recPri, img_hexPri, img_pyramid,
 			img_cylinder, img_sphere;
 
 	private JTextArea logText;
 	private JScrollPane logScroll;
 	private JLabel statusBar;
 	static Logger sessionLog = new Logger();
 	static int a = 1;
 
 	public GUI_3D() {
 		swingTest = new SwingTest();
 		c3d = swingTest.getC3d();
 		c3d.addMouseMotionListener(this);
 		c3d.addMouseListener(this);
 		sessionLog.writeOut(sessionLog.getFilename(), sessionLog.getLog());
 		init();
 	}
 
 	public final void init() {
 
 		frame = new JFrame("3D GUI");
 		frame.setSize(900, 700);
 		frame.setLocationRelativeTo(null);
 		frame.setResizable(false);
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
 		menubar.add(Box.createHorizontalGlue()); // adheres Help menu to right
 													// side
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
 
 		shapesToolbar.setLayout(new GridLayout(7, 1, 0, 8));
 		shapesToolbar.setBorder(LineBorder.createGrayLineBorder());
 
 		// creates buttons for each shape
 
 		btn_triPri = new JButton();
 		btn_recPri = new JButton();
 		btn_hexPri = new JButton();
 		btn_pyramid = new JButton();
 		btn_cylinder = new JButton();
 		btn_sphere = new JButton();
 
 		// change background color of buttons
 		btn_triPri.setBackground(Color.WHITE);
 		btn_recPri.setBackground(Color.WHITE);
 		btn_hexPri.setBackground(Color.WHITE);
 		btn_pyramid.setBackground(Color.WHITE);
 		btn_cylinder.setBackground(Color.WHITE);
 		btn_sphere.setBackground(Color.WHITE);
 
 		// adds images to the buttons
 		try {
 			img_triPri = ImageIO.read(getClass().getResource(
 					"resources/triangular_prism.png"));
 			btn_triPri.setIcon(new ImageIcon(img_triPri));
 		} catch (IOException ex) {
 			btn_triPri.setText("Tri Pri");
 		}
 		try {
 			img_recPri = ImageIO.read(getClass().getResource(
 					"resources/square_prism.png"));
 			btn_recPri.setIcon(new ImageIcon(img_recPri));
 		} catch (IOException ex) {
 			btn_recPri.setText("Sqr Pri");
 		}
 		try {
 			img_hexPri = ImageIO.read(getClass().getResource(
 					"resources/hexagonal_prism.png"));
 			btn_hexPri.setIcon(new ImageIcon(img_hexPri));
 		} catch (IOException ex) {
 			btn_hexPri.setText("Hex Pri");
 		}
 		try {
 			img_pyramid = ImageIO.read(getClass().getResource(
 					"resources/square_pyramid.png"));
 			btn_pyramid.setIcon(new ImageIcon(img_pyramid));
 		} catch (IOException ex) {
 			btn_pyramid.setText("Tri Pyr");
 		}
 		try {
 			img_cylinder = ImageIO.read(getClass().getResource(
 					"resources/cylinder.png"));
 			btn_cylinder.setIcon(new ImageIcon(img_cylinder));
 		} catch (IOException ex) {
 			btn_cylinder.setText("Cylinder");
 		}
 		try {
 			img_sphere = ImageIO.read(getClass().getResource(
 					"resources/sphere.png"));
 			btn_sphere.setIcon(new ImageIcon(img_sphere));
 		} catch (IOException ex) {
 			btn_sphere.setText("Sphere");
 		}
 
 		btn_recPri.addActionListener(new CreateRectangularPrism());
 		btn_triPri.addActionListener(new CreateTriangularPrism());
 		btn_pyramid.addActionListener(new CreatePyramid());
 		btn_cylinder.addActionListener(new CreateCylinder());
 		btn_sphere.addActionListener(new CreateSphere());
 		btn_hexPri.addActionListener(new CreateHexagonalPrism());
 
 		// adds buttons to left-hand toolbar
 		shapesToolbar.add(btn_recPri);
 		shapesToolbar.add(btn_triPri);
 		shapesToolbar.add(btn_pyramid);
 		shapesToolbar.add(btn_cylinder);
 		shapesToolbar.add(btn_sphere);
 		shapesToolbar.add(btn_hexPri);
 
 		// creates right-hand toolbar
 		rightToolbar = new JPanel();
 		rightToolbar.setPreferredSize(new Dimension(150, 0));
 		// rightToolbar.setLayout(new GridLayout(4,1,0,15));
 		rightToolbar.setLayout(new BoxLayout(rightToolbar, BoxLayout.Y_AXIS));
 		rightToolbar.setBorder(LineBorder.createGrayLineBorder());
 
 		mainPanel.add(rightToolbar, BorderLayout.LINE_END);
 
 		// current shapes panel
 		JPanel currentShapes = new JPanel();
 		currentShapes.setPreferredSize(new Dimension(150, 100));
 		rightToolbar.add(currentShapes);
 		rightToolbar.add(Box.createVerticalGlue());
 		currentShapes.setBorder(LineBorder.createGrayLineBorder());
 
 		currentShapesPanel = new CurrentShapesPanel(currentShapes);
 
 		// rotation panel
 		JPanel rotatePane = new JPanel();
 		rotatePane.setMaximumSize(new Dimension(150, 190));
 		rotatePane.setPreferredSize(new Dimension(150, 190));
 		rightToolbar.add(rotatePane);
 		rotatePane.setBorder(new EmptyBorder(0, 0, 0, 0));
 		rotatePane.setBorder(LineBorder.createGrayLineBorder());
 		rightToolbar.add(Box.createVerticalGlue());
 
 		new RotatePanel(rotatePane);
 
 		// resize panel
 		JPanel resizePane = new JPanel();
 		resizePane.setPreferredSize(new Dimension(100, 100));
 		rightToolbar.add(resizePane);
 		resizePane.setBorder(LineBorder.createGrayLineBorder());
 
 		new ResizePanel(resizePane);
 
 		// aesthetics panel
 		JPanel aestheticsPane = new JPanel();
 		aestheticsPane.setMaximumSize(new Dimension(150, 110));
 		aestheticsPane.setPreferredSize(new Dimension(150, 110));
 
 		rightToolbar.add(aestheticsPane);
 		aestheticsPane.setBorder(LineBorder.createGrayLineBorder());
 
 		aestheticsPanel = new AestheticsPanel(aestheticsPane);
 
 		// creates center panel
 		centerPanel = new JPanel();
 		centerPanel.setLayout(new BorderLayout());
 
 		mainPanel.add(centerPanel, BorderLayout.CENTER);
 
 		centerPanel.add(c3d, BorderLayout.CENTER);
 		frame.setVisible(true);
 
 		JPanel bottomCenter = new JPanel();
 		bottomCenter.setLayout(new BorderLayout());
 
 		logPart("current");
 
 		JSlider zoom = new JSlider(JSlider.HORIZONTAL, 5, 200, 100);
 
 		zoom.setSnapToTicks(true);
 		zoom.setMajorTickSpacing(25);
 		zoom.setMinorTickSpacing(5);
 		zoom.setPaintTicks(true);
 		// zoom.setPaintLabels(true);
 
 		zoom.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				// System.out.println("Changing.");
 				JSlider source = (JSlider) e.getSource();
 				// double previousValue = source.getValue();
 
 				// System.out.println("Zoom amount: " + zoomAmount);
 
 				Transform3D t3d = new Transform3D();
 
 				if (((source.getValue()) % 5 == 0)) {
 
 					System.out.println(zoomAmount);
 
 					if (zoomAmount >= -10.0)
 						zoomAmount = -10 + (0.4)
 								* ((source.getValue() - 100) / 5);
 					else if (zoomAmount < -10.0)
 						zoomAmount = -10 - (4)
 								* ((100 - source.getValue()) / 5);
 
 					t3d.setTranslation(new Vector3d(0.0, 0.0, zoomAmount));
 					t3d.invert(); // moving the viewer, not the scene
 					GUI_3D.getSwingTest().getTgArray().setTransform(t3d);
 				}
 			}
 		});
 
 		bottomCenter.add(logScroll, BorderLayout.PAGE_END);
 		JLabel lbl_log = new JLabel(" L O G G E R:");
 		lbl_log.setFont(new Font("sansserif", Font.BOLD, 18));
 		bottomCenter.add(lbl_log, BorderLayout.LINE_START);
 		bottomCenter.add(zoom, BorderLayout.LINE_END);
 		centerPanel.add(bottomCenter, BorderLayout.PAGE_END);
 
 		statusBar = new JLabel();
 		statusBar
 				.setText(" Cursor Position:  |  Selected: x  |  Total Shapes: x");
 		statusBar.setPreferredSize(new Dimension(-1, 22));
 		statusBar.setBorder(LineBorder.createGrayLineBorder());
 		mainPanel.add(statusBar, BorderLayout.PAGE_END);
 
 		frame.setVisible(true);
 	}
 
 	void logPart(String filePath) {
 		String currentLog;
 
 		if (filePath.equalsIgnoreCase("current")) {
 			currentLog = sessionLog.getFilename();
 		} else {
 			currentLog = filePath;
 		}
 
 		/*
 		 * File directory = new File(new File(".").getAbsolutePath()); //Yo
 		 * dawg, I heard you like new Files String file[] = directory.list();
 		 * for (int i = 0; i < file.length; i++) { current = file[i]; String[]
 		 * splitName = current.split("_"); if(splitName[0].equals("2011")){
 		 * currentLog = current; //Finds the latest log by looking at the end of
 		 * the directory. Could be more robust } else{} }
 		 */
 
 		logText = new JTextArea("Logging... \n"); // **LOGGER PANEL**
 		logText.setLineWrap(true);
 		logText.setBorder(LineBorder.createGrayLineBorder());
 
 		// This loop reads every line in the file and adds it to the top of the
 		// logger window
 		try {
 			BufferedReader input = new BufferedReader(
 					new FileReader(currentLog));
 			try {
 				String line = null;
 				while ((line = input.readLine()) != null) {
 					logText.append(line + "\n");
 				}
 			} finally {
 				input.close();
 			}
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		}
 
 		// logText.insert(sessionLog.getLog(),0);
 
 		logScroll = new JScrollPane(logText);
 		logText.setEditable(false);
 		logScroll
 				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		logScroll.setPreferredSize(new Dimension(0, 150));
 	}
 
 	public void parseLog() {
 		String capture = "";
 		Scanner scan = null;
 
 		try {
 			File file = new File(sessionLog.getFilename());
 			scan = new Scanner(file);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			System.out.println("File not found!");
 		}
 
 		while (scan.hasNextLine()) {
 			String line = scan.nextLine();
 			String[] seg = line.split(";");
 
 			// For all lines
 			String cmd = seg[0]; // Command
 
 			if (cmd.equals(zoom)) {
 				double percent = Double.parseDouble(seg[1]); // Percentage
 				capture = String.format("You zoomed in %.2f%%.\n", percent);
 			} else {
 				String shp = seg[1]; // Action
 				if (cmd.equals(create)) {
 					String id = seg[2];
 					capture = String.format("You created a %s called %s.\n", shp, id);
 					if (shp.equalsIgnoreCase(pyramid)) {
 						swingTest.getSceneBranchGroup().addChild(swingTest.createPyramid());
 					} else if (shp.equalsIgnoreCase(sphere)) {
 						swingTest.getSceneBranchGroup().addChild(swingTest.createSphere());
 					} else if (shp.equalsIgnoreCase(cylinder)) {
 						swingTest.getSceneBranchGroup().addChild(swingTest.createCylinder());
 					} else if (shp.equalsIgnoreCase(rectangle)) {
 						swingTest.getSceneBranchGroup().addChild(swingTest.createRectPrism());
 					} else if (shp.equalsIgnoreCase(triangle)) {
 						swingTest.getSceneBranchGroup().addChild(swingTest.createTriPrism());
 					} else if (shp.equalsIgnoreCase(hexagon)) {
 						swingTest.getSceneBranchGroup().addChild(swingTest.createHexPrism());
 					} else {
 						System.out.println("Error: Problem identifying shape!");
 					}
 					currentShapesPanel.getListModel().addElement(swingTest.getShapeClicked().getUserData());
 					currentShapesPanel.getList().setSelectedValue(swingTest.getShapeClicked().getUserData(), true);
 				} else if (cmd.equals(move)) {
 					double x = Double.parseDouble(seg[2]); // X-Axis Translation
 					double y = Double.parseDouble(seg[3]); // Y-Axis Translation
 					double z = Double.parseDouble(seg[4]); // Z-Axis Translation
 					capture = String.format("You moved a %s (%.2f, %.2f, %.2f).\n", shp, x, y, z);
 				} else if (cmd.equals(rotate)) {
 					String axis = seg[2]; // Which axis?
 					int numRot = Integer.parseInt(seg[3]);
 					capture = String.format(
 							"You rotated a %s %d times around the %s-axis.\n",
 							shp, numRot, axis);
 				} else if (cmd.equals(scale)) {
 					double percent = Double.parseDouble(seg[2]); // Percentage
 					capture = String.format("You scaled a %s %.2f%%.\n", shp,
 							percent);
 				} else if (cmd.equals(resize)) {
 					double height = Double.parseDouble(seg[2]); // X-Axis
 																// Translation
 					double width = Double.parseDouble(seg[3]); // Y-Axis
 																// Translation
 					double depth = Double.parseDouble(seg[4]); // Z-Axis
 																// Translation
 					capture = String.format(
 							"You resized a %s to %.2f&x%.2f&x%.2f&.\n", shp,
 							height, width, depth);
 				}
 			}
 			logText.setText(logText.getText() + capture);
 			System.out.print(capture);
 		}
 	}
 	
 	
 	public void updateAestheticsPanel() {
 		int numFaces = 0;
 
 		if (swingTest.getShapeClicked().getClass().getName().equals("aSphere")
 			|| swingTest.getShapeClicked().getClass().getName().equals("aCylinder"))
 			numFaces = 1;
 		else if (swingTest.getShapeClicked().getClass().getName().equals("Pyramid") 
 				|| swingTest.getShapeClicked().getClass().getName().equals("TriangularPrism"))
 			numFaces = 5;
 		else if (swingTest.getShapeClicked().getClass().getName().equals("RectangularPrism"))
 			numFaces = 6;
 		else if (swingTest.getShapeClicked().getClass().getName().equals("HexagonalPrism"))
 			numFaces = 8;
 		
 		
 		if (swingTest.getShapeClicked().getClass().getName().equals("aCylinder") 
 				|| swingTest.getShapeClicked().getClass().getName().equals("aSphere")) {
 			aestheticsPanel.getFaceSelection().setEnabled(false);
 			aestheticsPanel.getEdgeColors().setEnabled(false);
 			aestheticsPanel.getEdgeWeight().setEnabled(false);
 		}
 		else {
 			aestheticsPanel.getFaceSelection().setEnabled(true);
 			aestheticsPanel.getEdgeColors().setEnabled(true);
 			aestheticsPanel.getEdgeWeight().setEnabled(true);
 		}
 		
 		
 		String[] f = new String[numFaces];
 		
 		for (int i = 0; i < numFaces; i++) {
 			f[i] = "Face ".concat(Integer.toString(i+1));
 		}
 		
 		
 		aestheticsPanel.getFaceSelection().removeAllItems();
 		
 		for (int i = 0; i < f.length; i++)
 			aestheticsPanel.getFaceSelection().insertItemAt(f[i], i);
 		
 		aestheticsPanel.getFaceSelection().setSelectedIndex(0);
 	}
 	
 
 	public void actionPerformed(ActionEvent e) {
 	}
 
 	public static void main(String[] args) {
 		GUI_3D ex = new GUI_3D();
 		ex.setVisible(true);
 	}
 
 	protected int triangularPrismCount = 0;
 	class CreateTriangularPrism implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			// System.out.println("Created: Triangular Prism");
 			swingTest.getSceneBranchGroup()
 					.addChild(swingTest.createTriPrism());
 			statusBar.setText(" Cursor Position: " + swingTest.getCurPos()
 					+ "  |  Selected: "
 					+ swingTest.getShapeClicked().getUserData()
 					+ "  |  Total Shapes: " + swingTest.getTotalShapes());
 			sessionLog.add(create + ";" + triangle + ";" + triangularPrismCount++);
 			currentShapesPanel.getListModel().addElement(swingTest.getShapeClicked().getUserData());
 			currentShapesPanel.getList().setSelectedValue(swingTest.getShapeClicked().getUserData(), true);
 		}
 	}
 
 	protected int rectangularPrismCount = 0;
 	class CreateRectangularPrism implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			// System.out.println("Created: RectPrism");
 			swingTest.getSceneBranchGroup().addChild(
 					swingTest.createRectPrism());
 			statusBar.setText(" Cursor Position: " + swingTest.getCurPos()
 					+ "  |  Selected: "
 					+ swingTest.getShapeClicked().getUserData()
 					+ "  |  Total Shapes: " + swingTest.getTotalShapes());
 			sessionLog.add(create + ";" + rectangle + ";" + rectangularPrismCount++);
 			currentShapesPanel.getListModel().addElement(swingTest.getShapeClicked().getUserData());
 			currentShapesPanel.getList().setSelectedValue(swingTest.getShapeClicked().getUserData(), true);
 		}
 	}
 
 	protected int pyramidCount = 0;
 	class CreatePyramid implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			// System.out.println("Created: Pyramid");
 			swingTest.getSceneBranchGroup().addChild(swingTest.createPyramid());
 			statusBar.setText(" Cursor Position: " + swingTest.getCurPos()
 					+ "  |  Selected: "
 					+ swingTest.getShapeClicked().getUserData()
 					+ "  |  Total Shapes: " + swingTest.getTotalShapes());
 			sessionLog.add(create + ";" + pyramid + ";" + pyramidCount++);
 			currentShapesPanel.getListModel().addElement(swingTest.getShapeClicked().getUserData());
 			currentShapesPanel.getList().setSelectedValue(swingTest.getShapeClicked().getUserData(), true);
 		}
 	}
 
 	protected int cylinderCount = 0;
 	class CreateCylinder implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			// System.out.println("Created: Cylinder");
 			swingTest.getSceneBranchGroup().addChild(swingTest.createCylinder());
 			statusBar.setText(" Cursor Position: " + swingTest.getCurPos()
 					+ "  |  Selected: "
 					+ swingTest.getShapeClicked().getUserData()
 					+ "  |  Total Shapes: " + swingTest.getTotalShapes());
 			sessionLog.add(create + ";" + cylinder + ";" + swingTest.getCylinderCount());
 			currentShapesPanel.getListModel().addElement(swingTest.getShapeClicked().getUserData());
 			currentShapesPanel.getList().setSelectedValue(swingTest.getShapeClicked().getUserData(), true);
 			sessionLog.add(create + ";" + cylinder + ";" + cylinderCount++);
 		}
 	}
 
 	protected int sphereCount = 0;
 	class CreateSphere implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			// System.out.println("Created: Sphere");
 			swingTest.getSceneBranchGroup().addChild(swingTest.createSphere());
 			statusBar.setText(" Cursor Position: " + swingTest.getCurPos()
 					+ "  |  Selected: "
 					+ swingTest.getShapeClicked().getUserData()
 					+ "  |  Total Shapes: " + swingTest.getTotalShapes());
 			sessionLog.add(create + ";" + sphere + ";" + sphereCount++);
 			currentShapesPanel.getListModel().addElement(swingTest.getShapeClicked().getUserData());
 			currentShapesPanel.getList().setSelectedValue(swingTest.getShapeClicked().getUserData(), true);
 		}
 	}
 
 	class CreateHexagonalPrism implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			// System.out.println("Created: Hexagonal Prism");
 			swingTest.getSceneBranchGroup()
 					.addChild(swingTest.createHexPrism());
 			statusBar.setText(" Cursor Position: " + swingTest.getCurPos()
 					+ "  |  Selected: "
 					+ swingTest.getShapeClicked().getUserData()
 					+ "  |  Total Shapes: " + swingTest.getTotalShapes());
 			sessionLog.add(create + ";" + hexagon + ";" + swingTest.getHexPrismCount());		
 			currentShapesPanel.getListModel().addElement(swingTest.getShapeClicked().getUserData());
 			currentShapesPanel.getList().setSelectedValue(swingTest.getShapeClicked().getUserData(), true);
 		}
 	}
 
 	class SaveAction implements ActionListener {// Action For Save goes here
 		public void actionPerformed(ActionEvent e) {
 			sessionLog.writeOut(sessionLog.getFilename(), sessionLog.getLog());
 			JFrame saveFrame = new JFrame("Save");
 			JLabel label = new JLabel(String.format("You saved %s\n",
 					sessionLog.getFilename()));
 			JPanel panel = new JPanel();
 			saveFrame.add(panel);
 			panel.add(label);
 			saveFrame.setLocationRelativeTo(null);
 			saveFrame.setVisible(true);
 			saveFrame.setSize(300, 75);
 		}
 	}
 
 	class LoadAction implements ActionListener {// Action For Save goes here
 		public void actionPerformed(ActionEvent e) {
 			JFileChooser chooser = new JFileChooser(
 					new File(".").getAbsolutePath());
 			// chooser.addChoosableFileFilter(filter);
 			// CustomFileFilter filter = new CustomFileFilter();
 			// filter.addExtension("log"); // Only choose text files
 			// filter.setDescription("Log Files");
 			// chooser.setFileFilter(filter);
 
 			int returnVal = chooser.showOpenDialog(null);
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				System.out.println("You chose to open this file: "
 						+ chooser.getSelectedFile().getName());
 				logPart(chooser.getSelectedFile().getName());
 			}
 
 			String fileName = chooser.getSelectedFile().getName();
 			int ext = fileName.indexOf('.');
 			fileName = fileName.substring(0, ext); // Get rid of extension
 
 			sessionLog.setFilename(fileName);
 			sessionLog.readFile(); // Read File
 			parseLog(); // Parse the log.
 		}
 	}
 
 	class ExitAction implements ActionListener {// Action For Exit
 		public void actionPerformed(ActionEvent e) {
 			sessionLog.writeOut(sessionLog.getFilename(), sessionLog.getLog());
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
 							+ "\n\nJennifer Hill\nRyan Kane\nDorothy Kirlew\n"
 							+ "Donald Shaner\nand Sean Weber"
 							+ "\n \n \n"
 							+ "The 3D Manipulator is released under the GNU GPLv3 license, "
 							+ " freely distributable for educational use only.");
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
 		statusBar.setText(" Cursor Position: " + swingTest.getCurPos()
 				+ "  |  Selected: " + swingTest.getShapeClicked().getUserData()
 				+ "  |  Total Shapes: " + swingTest.getTotalShapes());
 	}
 
 	public void mouseMoved(MouseEvent e) {
 		if (swingTest.getShapeClicked() == null)
 			statusBar.setText(" Cursor Position: " + swingTest.getCurPos()
 					+ "  |  Selected: (none)  |  Total Shapes: "
 					+ swingTest.getTotalShapes());
 		else
 			statusBar.setText(" Cursor Position: " + swingTest.getCurPos()
 					+ "  |  Selected: "
 					+ swingTest.getShapeClicked().getUserData()
 					+ "  |  Total Shapes: " + swingTest.getTotalShapes());
 
 	}
 
 	public void mouseClicked(MouseEvent arg0) {
 		statusBar.setText(" Cursor Position: " + swingTest.getCurPos()
 				+ "  |  Selected: " + swingTest.getShapeClicked().getUserData()
 				+ "  |  Total Shapes: " + swingTest.getTotalShapes());
 	}
 
 	
 	public void mouseEntered(MouseEvent arg0) { }
 	public void mouseExited(MouseEvent arg0) { }
 
 	public void mousePressed(MouseEvent arg0) {
 		updateAestheticsPanel();
 		
 		currentShapesPanel.getList().setSelectedValue(swingTest.getShapeClicked().getUserData(), true);
 	}
 
 	public void mouseReleased(MouseEvent e) {
 		sessionLog.add(swingTest.getShapeClicked().getName() + ";" + swingTest.getTranslationX() + ";" + swingTest.getTranslateY());
 	}
 
 	
 	
 	public void keyPressed(KeyEvent e) {
 		if (e.getKeyCode() == KeyEvent.VK_DELETE) {
 			swingTest.removeShape((String)swingTest.getShapeClicked().getUserData());
 			currentShapesPanel.getListModel().removeElement(swingTest.getShapeClicked().getUserData());
 		}
 	}
 
 	public void keyTyped(KeyEvent e) { }
 	public void keyReleased(KeyEvent e) { }
 }
