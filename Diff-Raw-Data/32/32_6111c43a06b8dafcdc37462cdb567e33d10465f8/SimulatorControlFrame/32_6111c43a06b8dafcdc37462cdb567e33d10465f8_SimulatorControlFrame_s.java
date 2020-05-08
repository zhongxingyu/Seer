 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.sourceforge.frcsimulator.gui;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.lang.reflect.Array;
 import java.util.logging.*;
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.tree.*;
 import net.sourceforge.frcsimulator.Client;
 import net.sourceforge.frcsimulator.gui.propertyeditor.*;
 import net.sourceforge.frcsimulator.internals.*;
 import net.sourceforge.frcsimulator.mistware.Simulator;
 
 /**
  *
  * @author wolf
  */
 public class SimulatorControlFrame extends JFrame {
 
 	protected Simulator simulator;
 	protected String midletName;
 	// Note: must be anonymous to use in applet.
 	protected static final Logger logger = Logger.getAnonymousLogger();//Logger.getLogger(SimulatorControlFrame.class.getName());
 	protected static JButton startButton = new JButton("Start simulator");
 	protected JScrollBar scrollBar = null;
 	protected JMenuBar menuBar;
 	protected JMenu fileMenu, fileExamplesMenuItem, helpMenu;
 	protected JMenuItem fileQuitMenuItem, helpAboutMenuItem;
 	protected JCheckBoxMenuItem fileDebugCheckboxMenuItem;
 	protected JTree componentTree;
 	protected JTextArea console;
 	protected PrintStream consoleStream;
 	protected PropertyEditor editor;
 	protected JSplitPane propertyPane;
 	protected JScrollPane outScroll;
 	protected String[][] examples = {{"MIDlet", "net.sourceforge.frcsimulator.test.FRCBotMIDlet"},
 		{"RobotBase", "net.sourceforge.frcsimulator.test.FRCBotRobotBase"},
 		{"SimpleRobot", "edu.wpi.first.wpilibj.SimpleRobot"},
 		{"IterativeRobot", "edu.wpi.first.wpilibj.IterativeRobot"},
 		{"Watchdog Example", "net.sourceforge.frcsimulator.test.FRCBotWatchdogExample"}};
 	public SimulatorControlFrame() { // For applet
 		this("edu.wpi.first.wpilibj.SimpleRobot");
 	}
 	public SimulatorControlFrame(String testCase) {
 		super("Frc Simulator - " + testCase);
 		midletName = testCase;
 		logger.addHandler(new GuiHandler(System.out));
 		//// Property Editors ////
 		PropertyEditor.register(Boolean.class, BooleanPropertyEditor.class);
 		PropertyEditor.register(Character.class, CharacterPropertyEditor.class);
 		PropertyEditor.register(Number.class, NumberPropertyEditor.class);
 		PropertyEditor.register(Byte.class, NumberPropertyEditor.class);
 		PropertyEditor.register(Short.class, NumberPropertyEditor.class);
 		PropertyEditor.register(Integer.class, NumberPropertyEditor.class);
 		PropertyEditor.register(Float.class, NumberPropertyEditor.class);
 		PropertyEditor.register(Long.class, NumberPropertyEditor.class);
 		PropertyEditor.register(Double.class, NumberPropertyEditor.class);
 		//// Initialize the window ////
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setSize(new Dimension(500, 500));
 		setLayout(new BorderLayout());
 		add(startButton, BorderLayout.NORTH);
 		menuBar = new JMenuBar();
 		fileMenu = new JMenu("File");
 		fileExamplesMenuItem = new JMenu("Examples");
 		for (String[] example : examples) {
 			fileExamplesMenuItem.add(new JMenuItem(new ExampleSelectAction(example[0], example[1])));
 		}
 		fileMenu.add(fileExamplesMenuItem);
 		fileDebugCheckboxMenuItem = new JCheckBoxMenuItem("Enable debug messages");
 		fileDebugCheckboxMenuItem.setAccelerator(KeyStroke.getKeyStroke('D', InputEvent.CTRL_DOWN_MASK));
 		fileDebugCheckboxMenuItem.addChangeListener(new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent ce) {
 				CRIO.getInstance().setDebugging(fileDebugCheckboxMenuItem.isSelected());
 			}
 		});
 		//fileDebugCheckboxMenuItem.setSelected(true);
 		fileMenu.add(fileDebugCheckboxMenuItem);
 		JMenuItem fileRefreshMenuItem = new JMenuItem("Refresh Components", 'R');
 		fileRefreshMenuItem.setAccelerator(KeyStroke.getKeyStroke('R', InputEvent.CTRL_DOWN_MASK));
 		fileRefreshMenuItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				refreshProperties();
 			}
 		});
 		fileMenu.add(fileRefreshMenuItem);
 		fileQuitMenuItem = new JMenuItem("Quit", 'Q');
 		fileQuitMenuItem.setAccelerator(KeyStroke.getKeyStroke('Q', InputEvent.CTRL_DOWN_MASK));
 		fileQuitMenuItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				System.exit(Client.E_NONE);
 			}
 		});
 		fileMenu.add(fileQuitMenuItem);
 		menuBar.add(fileMenu);
 		helpMenu = new JMenu("Help");
 		helpAboutMenuItem = new JMenuItem("About", 'A');
 		helpAboutMenuItem.setAccelerator(KeyStroke.getKeyStroke('I', InputEvent.CTRL_DOWN_MASK));
 		final JFrame frame=this;
 		helpAboutMenuItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				//TODO finish the about dialog
 				JDialog aboutBox = new JDialog(frame);
 				aboutBox.setTitle("About FRC Simulator");
 				aboutBox.setLayout(new GridLayout(0,1));
 				JLabel titleLabel=new JLabel("FRC Simulator",JLabel.CENTER);
 				titleLabel.setFont(new Font(Font.SERIF,Font.BOLD,24));
 				JLabel versionLabel=new JLabel("Subversion trunk",JLabel.CENTER);
 				versionLabel.setFont(new Font(Font.DIALOG,Font.ITALIC,12));
 				aboutBox.add(titleLabel);
 				aboutBox.add(versionLabel);
 				aboutBox.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 				aboutBox.setSize(500,500);
 				aboutBox.setVisible(true);
 			}
 		});
 		helpMenu.add(helpAboutMenuItem);
 		//menuBar.add(helpMenu);
 		setJMenuBar(menuBar);
 		console = new JTextArea();
 		console.setEditable(false);
 		console.setMargin(new Insets(3, 3, 3, 3));
 		outScroll = new JScrollPane(console);
 		scrollBar = outScroll.getVerticalScrollBar();
 		componentTree = new JTree(new JTree.DynamicUtilTreeNode("Components", SimulatedBot.getSimComponents()));
 		componentTree.setMinimumSize(new Dimension(componentTree.getMinimumSize().width, getHeight() / 2));
 		addComponentListener(new ComponentAdapter() {
 			@Override
 			public void componentResized(ComponentEvent ce) {
 				if (outScroll.getWidth() < getWidth() / 2) {
 					outScroll.setSize(new Dimension(getWidth() / 2, outScroll.getHeight()));
 				}
 				outScroll.setMinimumSize(new Dimension(getWidth() / 2, outScroll.getMinimumSize().height));
 				if (propertyPane.getRightComponent().getWidth() > getWidth() / 2) {
 					propertyPane.getRightComponent().setSize(new Dimension(getWidth() / 2, propertyPane.getHeight()));
 				}
 				propertyPane.setMaximumSize(new Dimension(getWidth() / 2, propertyPane.getMaximumSize().height));
 			}
 		});
 		componentTree.setRootVisible(false);
 		componentTree.addTreeSelectionListener(new TreeSelectionListener() {
 			@Override
 			public void valueChanged(TreeSelectionEvent tse) {
 				if (tse.isAddedPath()) {
 					try {
 						Object[] path = componentTree.getSelectionPath().getPath();
 						int i = 0;
 						Object edit = null;
 						for (Object pathNode : path) {
 							Object node;
 							if (DefaultMutableTreeNode.class.isAssignableFrom(pathNode.getClass())) {
 								DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)pathNode;
 								node = treeNode.getUserObject();
 							} else {
 								node = pathNode;
 							}
 							i++;
 							if (node == null || edit == null || "Simulator".equals(edit) || FrcBotSimProperty.class.isAssignableFrom(node.getClass())) {
 								edit = node;
 							} else if (FrcBotSimComponent.class.isAssignableFrom(edit.getClass()) && String.class.isAssignableFrom(node.getClass())) {
 								edit = ((FrcBotSimComponent)edit).getSimProperties().get((String)node);
 							} else if (FrcBotSimProperty.class.isAssignableFrom(edit.getClass()) && (FrcBotSimProperties.class.isAssignableFrom(((FrcBotSimProperty)edit).get().getClass())) && String.class.isAssignableFrom(node.getClass())) {
 								edit = ((FrcBotSimProperties)((FrcBotSimProperty)edit).get()).get((String)node);
 							} else if (Integer.class.isAssignableFrom(node.getClass())) {
 								edit = new ArrayWrappingProperty<Object>(FrcBotSimProperty.class.cast(edit),Integer.decode(node.toString()));
 							} else {
 								edit = node;
 							}
 						}
 						if (edit != null && FrcBotSimProperty.class.isAssignableFrom(edit.getClass())) {
 							editor = PropertyEditor.getEditor(componentTree.getSelectionPath().getLastPathComponent().toString(),(FrcBotSimProperty)edit);
 						} else {
 							editor = PropertyEditor.nullPropertyEditor;
 						}
 					} catch (Exception ex) {
 						logger.log(Level.WARNING, "Could not get an editor for the component", ex);
 						editor = PropertyEditor.nullPropertyEditor;
 					}
 				} else {
 					editor = PropertyEditor.nullPropertyEditor;
 				}
 				((JSplitPane) propertyPane.getRightComponent()).setBottomComponent(editor);
 				editor.setSize(editor.getMinimumSize());
 				//componentTree.setSize(propertyPane.getWidth()-editor.getWidth(),propertyPane.getHeight()-editor.getHeight());
 				//propertyPane.getBottomComponent().setSize(propertyPane.getBottomComponent().getMinimumSize());
 				//propertyPane.getTopComponent().setSize(propertyPane.getTopComponent().getMaximumSize());plitPane(JSplitPane.HORIZONTAL_SPLIT,
 			}
 		});
 		editor = PropertyEditor.nullPropertyEditor;
 		editor.initialize(null, null);
 		outScroll.setSize(getWidth() / 2, outScroll.getHeight());
 		outScroll.setPreferredSize(new Dimension(getWidth() / 2, outScroll.getHeight()));
 		propertyPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, outScroll, new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(componentTree), editor));
 		/*
 		 * propertyPane.add(editor); propertyPane.add(new JScrollPane(componentTree));
 		 */
 		//propertyPane.getTopComponent().setPreferredSize(propertyPane.getTopComponent().getMaximumSize());
 		//propertyPane.setPreferredSize(propertyPane.getMaximumSize());
 		add(propertyPane);
 		consoleStream = new PrintStream(new TextAreaStream(console));
 		startButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				initSimulator(midletName);
 				simulator.start();
 				System.setOut(consoleStream);
 				System.setErr(consoleStream);
 				fileExamplesMenuItem.setEnabled(false);
 				startButton.setEnabled(false);
 			}
 		});
 		setVisible(true);
 	}
 
 	private void refreshProperties() {
 		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Simulator");
 		for (FrcBotSimComponent component : SimulatedBot.getSimComponents()) {
 			DefaultMutableTreeNode branch = new DefaultMutableTreeNode(component);
 			recurseNodes(branch, component.getSimProperties());
 			/*
 			 * for (String key:component.getSimProperties().keySet()) {
 			 * DefaultMutableTreeNode leaf = new DefaultMutableTreeNode(key);
 			 * branch.add(leaf); }
 			 */
 			root.add(branch);
 		}
 		componentTree.setModel(new DefaultTreeModel(root));
 	}
 
 	private void recurseNodes(DefaultMutableTreeNode branch, FrcBotSimComponent component) {
 		recurseNodes(branch,component.getSimProperties());
 	}
 	private void recurseNodes(DefaultMutableTreeNode branch, FrcBotSimProperties properties) {
 		for (String key : properties.keySet()) {
 			recurseNode(branch,properties.get(key).get(),key);
 		}
 	}
 	private void recurseNodes(DefaultMutableTreeNode branch, Object[] array) {
 		for (int i = 0; i < array.length; i++) {
 			recurseNode(branch,array[i],i);
 		}
 	}
 
 	private void recurseNode(DefaultMutableTreeNode branch, Object object, Object key) {
 		DefaultMutableTreeNode branchBranch = new DefaultMutableTreeNode(key);
 		branch.add(branchBranch);
		Class[] classes={boolean[].class,byte[].class,char[].class,short[].class,int[].class,long[].class,
 			float[].class, double[].class};
 		try {
 			boolean worked=false;
			for (int c=0; c<classes.length; c++) {
				if (object.getClass().equals(classes[c])) {
					for (int i = 0; i < Array.getLength(classes[c].cast(object)); i++) {
 						branchBranch.add(new DefaultMutableTreeNode(i));
 					}
 					worked=true;
 				}
 			}
 			if (worked) {
 				// Do nothing, but it makes the logic easier
 			} else if (object.getClass().isArray()) {
 				recurseNodes(branchBranch, (Object[]) object);
 			} else if (object.getClass().getName().equals(FrcBotSimProperties.class.getName())) {
 				recurseNodes(branchBranch, (FrcBotSimProperties) object);
 			} else if (FrcBotSimComponent.class.isAssignableFrom(object.getClass())) {
 				recurseNodes(branchBranch, (FrcBotSimComponent)object);
 			}
 		} catch (NullPointerException npe) {
 		}
 	}
 
 	public void initSimulator(String action) {
 		//// Initialize the simulator ////
 		try {
 			simulator = new Simulator(action);
 		} catch (ClassNotFoundException ex) {
 			logger.log(Level.SEVERE, null, ex);
 		} catch (ClassCastException ex) {
 			logger.log(Level.SEVERE, null, ex);
 		}
 		simulator.getLogger().addHandler(new GuiHandler(System.err));
 		try {
 			simulator.onStatusChange(SimulatorControlFrame.class.getMethod("simStateChange", Simulator.Status.class, Simulator.Status.class));
 		} catch (Exception e) {
 			logger.log(Level.WARNING, "Couldn't add a state change handler to the simulator", e);
 		}
 	}
 
 	public static void simStateChange(Simulator.Status status, Simulator.Status oldStatus) {
 		startButton.setText("Simulator status: " + status.toString());
 		if (status.equals(Simulator.Status.RUNNING)) {
 			startButton.setBackground(Color.GREEN);
 		} else if (status.equals(Simulator.Status.INITIALIZING)) {
 			startButton.setBackground(Color.BLUE);
 		} else if (status.equals(Simulator.Status.ERROR)) {
 			startButton.setBackground(Color.RED);
 		} else if (status.equals(Simulator.Status.WATCHDOG_NOT_FED)){
 			startButton.setBackground(Color.YELLOW);
 		} else{
 			startButton.setBackground(null);
 		}
 	}
 
 	public class TextAreaStream extends OutputStream {
 
 		protected JTextArea area;
 
 		public TextAreaStream(JTextArea textArea) {
 			area = textArea;
 		}
 
 		@Override
 		public void write(int i) throws IOException {
 			area.append(new String(Character.toChars(i)));
 			scrollBar.setValue(scrollBar.getMaximum() + scrollBar.getVisibleAmount());
 		}
 	}
 
 	private class ExampleSelectAction extends AbstractAction {
 
 		protected String midlet;
 
 		public ExampleSelectAction(String title, String midletClass) {
 			super(title);
 			midlet = midletClass;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent ae) {
 			if (simulator == null) {
 				midletName = midlet;
 				setTitle("Frc Simulator - " + midletName);
 			} else {
 				logger.warning("Simulator already running; cannot set class.");
 			}
 		}
 	}
 }
