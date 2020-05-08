package main.java.scalax.automata.gui;
 
 import java.awt.BorderLayout;
 import java.awt.event.KeyEvent;
 
 import java.util.Arrays;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import com.mxgraph.swing.mxGraphComponent;
 import com.mxgraph.view.mxGraph;
 
 
 public class GUI extends JFrame {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6921400848602068156L;
 
 	public mxGraph graph;
 	Object root;
 	
 	public GUI (String name) {
 		super(name);
 	}
 	
 	public void initGUI(JFrame gui) {
 		JMenu menu;
 		JMenuItem menuItem;
 		JMenuBar menuBar = new JMenuBar();
 		JPanel sidePanel, statusBar, subPanel, statesPanel, transitionsPanel, infoPanel, buttonPanel;
 		JButton runButton, quitButton;
 		JTextField alphabetField, testField;
 
 		// menus
 		menu = new JMenu("File");
 		menu.setMnemonic(KeyEvent.VK_F);
 		menuBar.add(menu);
 		// TODO: create image for icon
 		menuItem = new JMenuItem("New", new ImageIcon());
 		menuItem.setMnemonic(KeyEvent.VK_N);
 		menu.add(menuItem);
 		// TODO: create image for icon
 		menuItem = new JMenuItem("Open", new ImageIcon());
 		menuItem.setMnemonic(KeyEvent.VK_O);
 		menu.add(menuItem);
 		// TODO: create image for icon
 		menuItem = new JMenuItem("Save", new ImageIcon());
 		menuItem.setMnemonic(KeyEvent.VK_S);
 		menu.add(menuItem);
 		menu.addSeparator();
 		// TODO: create image for icon
 		menuItem = new JMenuItem("Quit", new ImageIcon());
 		menuItem.setMnemonic(KeyEvent.VK_Q);
 		menu.add(menuItem);
 		
 		menu = new JMenu("Simulation");
 		menu.setMnemonic(KeyEvent.VK_S);
 		menuBar.add(menu);
 		// TODO: create image for icon
 		menuItem = new JMenuItem("Run", new ImageIcon());
 		menuItem.setMnemonic(KeyEvent.VK_R);
 		menu.add(menuItem);
 		
 		menu = new JMenu("Info");
 		menu.setMnemonic(KeyEvent.VK_I);
 		menuBar.add(menu);
 		// TODO: create image for icon
 		menuItem = new JMenuItem("About", new ImageIcon());
 		menuItem.setMnemonic(KeyEvent.VK_A);
 		menu.add(menuItem);
 		
 		statusBar = new JPanel();
 		sidePanel = new JPanel();
 		subPanel = new JPanel();
 		buttonPanel = new JPanel();
 		statesPanel = new JPanel();
 		transitionsPanel = new JPanel();
 		infoPanel = new JPanel();
 		
 		sidePanel.setLayout(new BorderLayout());
 		subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.Y_AXIS));
 		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
 		statesPanel.setLayout(new BorderLayout());
 		statesPanel.setBorder(BorderFactory.createTitledBorder("States:"));
 		transitionsPanel.setLayout(new BorderLayout());
 		transitionsPanel.setBorder(BorderFactory.createTitledBorder("Transitions:"));
 		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
 		
 		alphabetField = new JTextField();
 		alphabetField.setEditable(false);
 		testField = new JTextField();
 		infoPanel.add(new JLabel("Alphabet:"));
 		infoPanel.add(alphabetField);
 		infoPanel.add(new JLabel("Word to test:"));
 		infoPanel.add(testField);
 		
 		runButton = new JButton("Run");
 		quitButton = new JButton("Quit");
 		buttonPanel.add(runButton);
 		buttonPanel.add(quitButton);
 		
 		sidePanel.add(subPanel, BorderLayout.NORTH);
 		sidePanel.add(buttonPanel, BorderLayout.SOUTH);
 		subPanel.add(statesPanel);
 		subPanel.add(transitionsPanel);
 		subPanel.add(infoPanel);
 		
 		// TODO: remove status stub
 		JLabel status = new JLabel("StatusBar");
 		statusBar.add(status);	
 		
 		gui.setJMenuBar(menuBar);
 		gui.setLayout(new BorderLayout());
 		gui.add(statusBar, BorderLayout.SOUTH);
 		gui.add(sidePanel, BorderLayout.EAST);
 		gui.add(initGraph(), BorderLayout.CENTER);
 	}
 	
 	public JComponent initGraph() {
 		graph = new mxGraph();
 		root = graph.getDefaultParent();
 		
 		graph.getModel().beginUpdate();
 		try
 		{
 			Object v1 = graph.insertVertex(root, null, "Hello", 20, 20, 80,
 					30, "shape=ellipse;perimeter=ellipsePerimeter");
 			Object v2 = graph.insertVertex(root, null, "World!", 240, 150,
 					80, 30);
 			graph.insertEdge(root, null, "Edge", v1, v2);
 		}
 		finally
 		{
 			graph.getModel().endUpdate();
 		}
 		
 		return new mxGraphComponent(graph);
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		GUI gui = new GUI("Scalomator - Simulate finite-state machines");
 		gui.initGUI(gui);
 		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		gui.setSize(640, 480);
 		gui.setVisible(true);
 		
 		// some test for vertex and edge grabbing
 		System.out.println(Arrays.toString(gui.graph.getChildCells(gui.root, true, false)));
 		System.out.println(Arrays.toString(gui.graph.getChildCells(gui.root, false, true)));
 	}
 
 }
