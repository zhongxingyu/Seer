 package pipe.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.net.URI;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.ImageIcon;
 import javax.swing.InputMap;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JToggleButton;
 import javax.swing.JToolBar;
 import javax.swing.JViewport;
 import javax.swing.KeyStroke;
 import javax.swing.UIManager;
 import javax.swing.border.BevelBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import pipe.common.dataLayer.Arc;
 import pipe.common.dataLayer.ArcFactory;
 import pipe.common.dataLayer.DataLayer;
 import pipe.common.dataLayer.DataLayerInterface;
 import pipe.common.dataLayer.DataLayerWriter;
 import pipe.common.dataLayer.GroupTransition;
 import pipe.common.dataLayer.Marking;
 import pipe.common.dataLayer.PNMLTransformer;
 import pipe.common.dataLayer.PetriNetObject;
 import pipe.common.dataLayer.TNTransformer;
 import pipe.common.dataLayer.TokenClass;
 import pipe.common.dataLayer.Transition;
 import pipe.common.dataLayer.Unfolder;
 import pipe.gui.TokenClassPanel.TableModel;
 import pipe.gui.action.GuiAction;
 import pipe.gui.action.model.ModelGuideAction;
 import pipe.gui.action.model.ModelImportAction;
 import pipe.gui.undo.UndoableEdit;
 import pipe.gui.widgets.ButtonBar;
 import pipe.gui.widgets.EscapableDialog;
 import pipe.gui.widgets.FileBrowser;
 import pipe.io.JarUtilities;
 
 /**
  * @author Edwin Chung changed the code so that the firedTransitions array list
  *         is reset when the animation mode is turned off
  * 
  * @author Ben Kirby, 10 Feb 2007: Changed the saveNet() method so that it calls
  *         new DataLayerWriter class and passes in current net to save.
  * 
  * @author Ben Kirby, 10 Feb 2007: Changed the createNewTab method so that it
  *         loads an XML file using the new PNMLTransformer class and
  *         createFromPNML DataLayer method.
  * 
  * @author Edwin Chung modifed the createNewTab method so that it assigns the
  *         file name of the newly created DataLayer object in the dataLayer
  *         class (Mar 2007)
  * 
  * @author Oliver Haggarty modified initaliseActions to fix a bug that meant not
  *         all example nets were loaded if there was a non .xml file in the
  *         folder
  *         
  *  王瑞杰  2011年7月13日14:45:19  更新此类   汉化相关的部分
  */
 public class GuiFrame extends JFrame implements ActionListener, Observer {
 
 	// for zoom combobox and dropdown
 	private final String[] zoomExamples = { "40%", "60%", "80%", "100%",
 			"120%", "140%", "160%", "180%", "200%", "300%" };
 	private String frameTitle; // Frame title
 	private DataLayer appModel;
 	private GuiFrame appGui;
 	private GuiView appView;
 	private int mode, prev_mode, old_mode; // *** mode WAS STATIC ***
 	private int newNameCounter = 1;
 	private JTabbedPane appTab;
 	private StatusBar statusBar;
 	private JMenuBar menuBar;
 	private JToolBar animationToolBar, drawingToolBar;
 
 	// private Map actions = new HashMap();
 	private JComboBox zoomComboBox;
 	// Combo box to edit Token Classes
 	private JComboBox tokenClassComboBox;
 
 	// private LinkedList<TokenClass> tokenClasses = new
 	// LinkedList<TokenClass>();
 
 	private FileAction createAction, openAction, closeAction, saveAction,
 			saveAsAction, exitAction, printAction, exportPNGAction,
 			exportTNAction, exportPSAction, importAction;
 
 	private EditAction copyAction, cutAction, pasteAction, undoAction,
 			redoAction;
 	
 	private ModelGuideAction modelGuideAction;
 	private ModelImportAction modelImportAction;
 	
 	private GridAction toggleGrid;
 	private ZoomAction zoomOutAction, zoomInAction, zoomAction;
 	private DeleteAction deleteAction;
 	private TypeAction annotationAction, arcAction, inhibarcAction,
 			placeAction, transAction, timedtransAction, determintransAction, tokenAction,
 			selectAction, rateAction, deleteTokenAction,
 			dragAction;
 	private AnimateAction startAction, stepforwardAction, stepbackwardAction,
 			randomAction, randomAnimateAction;
 	private TokenClassAction specifyTokenClasses;
 	private GroupTransitionsAction groupTransitions;
 	private UnfoldAction unfoldAction;
 	private UngroupTransitionsAction ungroupTransitions;
 	private ChooseTokenClassAction chooseTokenClassAction;
 
 	public boolean dragging = false;
 
 	private HelpBox helpAction;
 
 	private boolean editionAllowed = true;
 
 	private CopyPasteManager copyPasteManager;
 
 	public GuiFrame(String title) {
 		// HAK-arrange for frameTitle to be initialized and the default file
 		// name
 		// to be appended to basic window title
 		frameTitle = title;
 		setTitle(null);
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception exc) {
 			System.err.println("Error loading L&F: " + exc);
 		}
 
 		this.setIconImage(new ImageIcon(Thread.currentThread()
 				.getContextClassLoader().getResource(
 						CreateGui.imgPath + "icon.png")).getImage());
 
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		this.setSize(screenSize.width * 80 / 100, screenSize.height * 80 / 100);
 		this.setLocationRelativeTo(null);
 
 		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
 
 		buildMenus();
 
 		// Status bar...
 		statusBar = new StatusBar();
 		getContentPane().add(statusBar, BorderLayout.PAGE_END);
 
 		// Build menus
 		buildToolbar();
 
 		addWindowListener(new WindowHandler());
 
 		//
 		copyPasteManager = new CopyPasteManager();
 
 		this.setForeground(java.awt.Color.BLACK);
 		this.setBackground(java.awt.Color.WHITE);
 	}
 
 	/**
 	 * This method does build the menus.
 	 * 
 	 * @author unknown
 	 * 
 	 * @author Dave Patterson - fixed problem on OSX due to invalid character in
 	 *         URI caused by unescaped blank. The code changes one blank
 	 *         character if it exists in the string version of the URL. This way
 	 *         works safely in both OSX and Windows. I also added a
 	 *         printStackTrace if there is an exception caught in the setup for
 	 *         the "Example nets" folder.
 	 **/
 	private void buildMenus() {
 		menuBar = new JMenuBar();
 
 		JMenu fileMenu = new JMenu("文件(F) ");
 		fileMenu.setMnemonic('F');
 
 		addMenuItem(fileMenu, createAction = new FileAction("新建",
 				"新建一个Petri网模型", "ctrl N"));
 		addMenuItem(fileMenu, openAction = new FileAction("打开...", "打开已有的Petri网模型",
 				"ctrl O"));
 		addMenuItem(fileMenu, closeAction = new FileAction("关闭",
 				"关闭当前页", "ctrl W"));
 		fileMenu.addSeparator();
 		addMenuItem(fileMenu, saveAction = new FileAction("保存", "保存模型",
 				"ctrl S"));
 		addMenuItem(fileMenu, saveAsAction = new FileAction("另存为...",
 				"模型另存为...", "shift ctrl S"));
 
 		fileMenu.addSeparator();
 		addMenuItem(fileMenu, importAction = new FileAction("导入...",
 				"导入已有模型", "ctrl I"));
 		// Export menu
 		JMenu exportMenu = new JMenu("导出");
 		exportMenu.setIcon(new ImageIcon(Thread.currentThread()
 				.getContextClassLoader().getResource(
 						CreateGui.imgPath + "Export.png")));
 		addMenuItem(exportMenu, exportPNGAction = new FileAction("PNG",
 				"将模型导出为PNG格式", "ctrl G"));
 		/*addMenuItem(exportMenu, exportPSAction = new FileAction("PostScript",
 				"Export the net to PostScript format", "ctrl T"));*/
 		addMenuItem(exportMenu, exportTNAction = new FileAction("eDSPN",
 				"将模型导出为Timenet格式(eDSPN)", "ctrl E"));
 		fileMenu.add(exportMenu);
 		fileMenu.addSeparator();
 		addMenuItem(fileMenu, printAction = new FileAction("打印", "打印",
 				"ctrl P"));
 		fileMenu.addSeparator();
 
 		// Example files menu
 		try {
 			URL examplesDirURL = Thread.currentThread().getContextClassLoader()
 					.getResource(
 							"Example nets"
 									+ System.getProperty("file.separator"));
 
 			if (JarUtilities.isJarFile(examplesDirURL)) {
 
 				JarFile jarFile = new JarFile(JarUtilities
 						.getJarName(examplesDirURL));
 
 				ArrayList<JarEntry> nets = JarUtilities.getJarEntries(jarFile,
 						"Example nets");
 
 				Arrays.sort(nets.toArray(), new Comparator() {
 					public int compare(Object one, Object two) {
 						return ((JarEntry) one).getName().compareTo(
 								((JarEntry) two).getName());
 					}
 				});
 
 				if (nets.size() > 0) {
 					JMenu exampleMenu = new JMenu("Example nets");
 					exampleMenu.setIcon(new ImageIcon(Thread.currentThread()
 							.getContextClassLoader().getResource(
 									CreateGui.imgPath + "Example.png")));
 					int index = 0;
 					for (int i = 0; i < nets.size(); i++) {
 						if (nets.get(i).getName().toLowerCase()
 								.endsWith(".xml")) {
 							addMenuItem(exampleMenu, new ExampleFileAction(nets
 									.get(i), (index < 10) ? ("ctrl " + index)
 									: null));
 							index++;
 						}
 					}
 					fileMenu.add(exampleMenu);
 					fileMenu.addSeparator();
 				}
 			} else {
 				File examplesDir = new File(examplesDirURL.toURI());
 				/**
 				 * The next block fixes a problem that surfaced on Mac OSX with
 				 * PIPE 2.4. In that environment (and not in Windows) any blanks
 				 * in the project name in Eclipse are property converted to
 				 * '%20' but the blank in "Example nets" is not. The following
 				 * code will do nothing on a Windows machine or if the logic on
 				 * OSX changess. I also added a stack trace so if the problem
 				 * occurs for another environment (perhaps multiple blanks need
 				 * to be manually changed) it can be easily fixed. DP
 				 */
 				// examplesDir = new File(new URI(examplesDirURL.toString()));
 				String dirURLString = examplesDirURL.toString();
 				int index = dirURLString.indexOf(" ");
 				if (index > 0) {
 					StringBuffer sb = new StringBuffer(dirURLString);
 					sb.replace(index, index + 1, "%20");
 					dirURLString = sb.toString();
 				}
 
 				examplesDir = new File(new URI(dirURLString));
 
 				File[] nets = examplesDir.listFiles();
 
 				Arrays.sort(nets, new Comparator() {
 					public int compare(Object one, Object two) {
 						return ((File) one).getName().compareTo(
 								((File) two).getName());
 					}
 				});
 
 				// Oliver Haggarty - fixed code here so that if folder contains
 				// non
 				// .xml file the Example x counter is not incremented when that
 				// file
 				// is ignored
 				if (nets.length > 0) {
 					JMenu exampleMenu = new JMenu("Example nets");
 					exampleMenu.setIcon(new ImageIcon(Thread.currentThread()
 							.getContextClassLoader().getResource(
 									CreateGui.imgPath + "Example.png")));
 					int k = 0;
 					for (int i = 0; i < nets.length; i++) {
 						if (nets[i].getName().toLowerCase().endsWith(".xml")) {
 							addMenuItem(exampleMenu, new ExampleFileAction(
 									nets[i], (k < 10) ? "ctrl " + (k++) : null));
 						}
 					}
 					fileMenu.add(exampleMenu);
 					fileMenu.addSeparator();
 				}
 			}
 		} catch (Exception e) {
 			System.err.println("Error getting example files:" + e);
 			e.printStackTrace();
 		}
 		addMenuItem(fileMenu, exitAction = new FileAction("退出",
 				"退出软件", "ctrl Q"));
 
 		JMenu editMenu = new JMenu("编辑(E) ");
 		editMenu.setMnemonic('E');
 		addMenuItem(editMenu, undoAction = new EditAction("撤销",
 				"撤销 (Ctrl-Z)", "ctrl Z"));
 		addMenuItem(editMenu, redoAction = new EditAction("恢复",
 				"恢复 (Ctrl-Y)", "ctrl Y"));
 		editMenu.addSeparator();
 		addMenuItem(editMenu, cutAction = new EditAction("剪切", "剪切 (Ctrl-X)",
 				"ctrl X"));
 		addMenuItem(editMenu, copyAction = new EditAction("复制",
 				"复制 (Ctrl-C)", "ctrl C"));
 		addMenuItem(editMenu, pasteAction = new EditAction("粘贴",
 				"粘贴 (Ctrl-V)", "ctrl V"));
 		addMenuItem(editMenu, deleteAction = new DeleteAction("删除",
 				"删除所选", "DELETE"));
 
 		JMenu drawMenu = new JMenu("绘制(D) ");
 		drawMenu.setMnemonic('D');
 		addMenuItem(drawMenu, selectAction = new TypeAction("选择",
 				Constants.SELECT, "选择", "S", true));
 		// [traduir] Alternativa per el mode select
 		// this.get
 		// System.out.println(""+ selectAction.getKeys()..toString());
 		// System.out.println("" + menuBar.getActionMap().keys().toString());
 		// Keymap parent = area.getKeymap();
 		/*
 		 * addKeyListener(new KeyListener() { public void keyPressed(KeyEvent e)
 		 * { System.out.println( "tester"); }
 		 * 
 		 * public void keyReleased(KeyEvent e) { System.out.println("2test2"); }
 		 * 
 		 * public void keyTyped(KeyEvent e) { System.out.println("3test3"); }
 		 * });
 		 * 
 		 * this.addKeyListener(new KeyListener) //drawMenu.add // ((Component)
 		 * this).getInputMap().put(KeyStroke.getKeyStroke("F2"),
 		 * "selectAction"); //escAction = new TypeAction("Select",
 		 * Constants.SELECT, "Select components","K",true);
 		 */
 
 		Action actionListener = new AbstractAction() {
 			public void actionPerformed(ActionEvent actionEvent) {
 				if (appGui.isEditionAllowed()) {
 					selectAction.actionPerformed(null);
 				}
 			}
 		};
 		KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");
 		InputMap inputMap = rootPane
 				.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
 		inputMap.put(stroke, "ESCAPE");
 
 		rootPane.getActionMap().put("ESCAPE", selectAction);
 
 		drawMenu.addSeparator();
 		addMenuItem(drawMenu, placeAction = new TypeAction("库所",
 				Constants.PLACE, "添加库所", "P", true));
 		addMenuItem(drawMenu, transAction = new TypeAction(
 				"瞬时变迁", Constants.IMMTRANS,
 				"添加一个瞬时变迁", "I", true));
 		addMenuItem(drawMenu, timedtransAction = new TypeAction(
 				"随机变迁", Constants.TIMEDTRANS,
 				"添加一个随机变迁", "T", true));
 		addMenuItem(drawMenu, determintransAction = new TypeAction(
 				"确定变迁", Constants.DETERMINRANS,
 				"添加一个确定变迁", "D", true));
 		addMenuItem(drawMenu, arcAction = new TypeAction("弧", Constants.ARC,
 				"添加一条弧", "A", true));
 		addMenuItem(drawMenu, inhibarcAction = new TypeAction("抑制弧",
 				Constants.INHIBARC, "添加一条抑制弧", "H", true));
 		addMenuItem(drawMenu, annotationAction = new TypeAction("注释",
 				Constants.ANNOTATION, "添加注释", "N", true));
 		drawMenu.addSeparator();
 		addMenuItem(drawMenu, tokenAction = new TypeAction("添加标记",
 				Constants.ADDTOKEN, "添加标记", "ADD", true));
 		addMenuItem(drawMenu, deleteTokenAction = new TypeAction(
 				"删除标记", Constants.DELTOKEN, "删除标记",
 				"SUBTRACT", true));
 		addMenuItem(drawMenu, specifyTokenClasses = new TokenClassAction(
 				"标记分类", "标记分类", "shift ctrl T"));
 		addMenuItem(drawMenu, groupTransitions = new GroupTransitionsAction(
 				"groupTransitions", "Group any possible transitions", "shift ctrl G"));
 		addMenuItem(drawMenu, ungroupTransitions = new UngroupTransitionsAction(
 				"ungroupTransitions", "Ungroup any possible transitions", "shift ctrl H"));
 		addMenuItem(drawMenu, unfoldAction = new UnfoldAction(
 				"unfoldAction", "Unfold Petri Net", "shift ctrl U"));
 		drawMenu.addSeparator();
 		addMenuItem(drawMenu, rateAction = new TypeAction("Rate Parameter",
 				Constants.RATE, "Rate Parameter", "R", true));
 		
 		JMenu modelMenu = new JMenu("建模(M) ");
 		modelMenu.setMnemonic('M');
 	//	JMenuItem modelGuide = modelMenu.add("建模向导     ");
 		addMenuItem(modelMenu, modelGuideAction = new ModelGuideAction("建模向导     ","以向导的方式对航电系统进行Petri网建模", "G")); 
 		addMenuItem(modelMenu, modelImportAction = new ModelImportAction("模型导入并转换     ","导入UML模型，并转换为Petri网", "I")); 
 		
 		
 
 		JMenu viewMenu = new JMenu("查看(V) ");
 		viewMenu.setMnemonic('V');
 
 		JMenu zoomMenu = new JMenu("调整大小");
 		zoomMenu.setIcon(new ImageIcon(Thread.currentThread()
 				.getContextClassLoader().getResource(
 						CreateGui.imgPath + "Zoom.png")));
 		addZoomMenuItems(zoomMenu);
 
 		addMenuItem(viewMenu, zoomOutAction = new ZoomAction("缩小",
 				"缩小10% ", "ctrl MINUS"));
 
 		addMenuItem(viewMenu, zoomInAction = new ZoomAction("放大",
 				"放大10% ", "ctrl PLUS"));
 		viewMenu.add(zoomMenu);
 
 		viewMenu.addSeparator();
 		addMenuItem(viewMenu, toggleGrid = new GridAction("调整网格",
 				"调整网格大小", "G"));
 		/*addMenuItem(viewMenu, dragAction = new TypeAction("Drag",
 				Constants.DRAG, "Drag the drawing", "D", true));*/
 
 		JMenu animateMenu = new JMenu("仿真(S) "); 
 		animateMenu.setMnemonic('S');
 		addMenuItem(animateMenu, startAction = new AnimateAction(
 				"手动仿真模式", Constants.START, "手动仿真",
 				"Ctrl A", true));
 		animateMenu.addSeparator();
 		addMenuItem(animateMenu, stepbackwardAction = new AnimateAction("后退一步",
 				Constants.STEPBACKWARD, "后退一步", "4"));
 		addMenuItem(animateMenu, stepforwardAction = new AnimateAction(
 				"前进一步", Constants.STEPFORWARD, "前进一步", "6"));
 		addMenuItem(animateMenu, randomAction = new AnimateAction("随机触发",
 				Constants.RANDOM, "随机触发一个变迁", "5"));
 		addMenuItem(animateMenu, randomAnimateAction = new AnimateAction(
 				"批量触发", Constants.ANIMATE,
 				"随机触发一定数量的变迁", "7", true));
 
 		JMenu helpMenu = new JMenu("帮助(H) ");
 		helpMenu.setMnemonic('H');
 		helpAction = new HelpBox("帮助", "查看帮助", "F1",
 				"index.htm");
 		addMenuItem(helpMenu, helpAction);
 
 		JMenuItem aboutItem = helpMenu.add("关于本软件");
 		aboutItem.addActionListener(this); // Help - About is implemented
 		// differently
 
 		URL iconURL = Thread.currentThread().getContextClassLoader()
 				.getResource(CreateGui.imgPath + "About.png");
 		if (iconURL != null) {
 			aboutItem.setIcon(new ImageIcon(iconURL));
 		}
 
 		menuBar.add(fileMenu);
 		menuBar.add(editMenu);
 		menuBar.add(viewMenu);
 		menuBar.add(drawMenu);
 		menuBar.add(modelMenu);
 		menuBar.add(animateMenu);
 		menuBar.add(helpMenu);
 		setJMenuBar(menuBar);
 
 	}
 
 	private void buildToolbar() {
 		// Create the toolbar
 		JToolBar toolBar = new JToolBar();
 		toolBar.setFloatable(false);// Inhibit toolbar floating
 
 		addButton(toolBar, createAction);
 		addButton(toolBar, openAction);
 		addButton(toolBar, saveAction);
 		addButton(toolBar, saveAsAction);
 		addButton(toolBar, closeAction);
 		toolBar.addSeparator();
 		addButton(toolBar, printAction);
 		toolBar.addSeparator();
 		addButton(toolBar, cutAction);
 		addButton(toolBar, copyAction);
 		addButton(toolBar, pasteAction);
 		addButton(toolBar, deleteAction);
 		addButton(toolBar, undoAction);
 		addButton(toolBar, redoAction);
 		toolBar.addSeparator();
 
 		addButton(toolBar, zoomOutAction);
 		addZoomComboBox(toolBar, zoomAction = new ZoomAction("Zoom",
 				"Select zoom percentage ", ""));
 		addButton(toolBar, zoomInAction);
 		toolBar.addSeparator();
 		addButton(toolBar, toggleGrid);
 		//addButton(toolBar, dragAction);
 		addButton(toolBar, startAction);
 
 		drawingToolBar = new JToolBar();
 		drawingToolBar.setFloatable(false);
 
 		toolBar.addSeparator();
 		addButton(drawingToolBar, selectAction);
 		drawingToolBar.addSeparator();
 		addButton(drawingToolBar, placeAction);// Add Draw Menu Buttons
 		addButton(drawingToolBar, transAction);
 		addButton(drawingToolBar, timedtransAction);
 		addButton(drawingToolBar, determintransAction);
 		addButton(drawingToolBar, arcAction);
 		addButton(drawingToolBar, inhibarcAction);
 		addButton(drawingToolBar, annotationAction);
 		drawingToolBar.addSeparator();
 		addButton(drawingToolBar, tokenAction);
 		addButton(drawingToolBar, deleteTokenAction);
 		addTokenClassComboBox(drawingToolBar,
 				chooseTokenClassAction = new ChooseTokenClassAction(
 						"chooseTokenClass", "选择标记分类", null));
 		addButton(drawingToolBar, specifyTokenClasses);
 		addButton(drawingToolBar, groupTransitions);
 		addButton(drawingToolBar, ungroupTransitions);
 		addButton(drawingToolBar, unfoldAction);
 		drawingToolBar.addSeparator();
 		addButton(drawingToolBar, rateAction);
 
 		toolBar.add(drawingToolBar);
 
 		animationToolBar = new JToolBar();
 		animationToolBar.setFloatable(false);
 		addButton(animationToolBar, stepbackwardAction);
 		addButton(animationToolBar, stepforwardAction);
 		addButton(animationToolBar, randomAction);
 		addButton(animationToolBar, randomAnimateAction);
 
 		toolBar.add(animationToolBar);
 		animationToolBar.setVisible(false);
 
 		toolBar.addSeparator();
 		addButton(toolBar, helpAction);
 
 		for (int i = 0; i < toolBar.getComponentCount(); i++) {
 			toolBar.getComponent(i).setFocusable(false);
 		}
 
 		getContentPane().add(toolBar, BorderLayout.PAGE_START);
 	}
 
 	private void addButton(JToolBar toolBar, GuiAction action) {
 
 		if (action.getValue("selected") != null) {
 			toolBar.add(new ToggleButton(action));
 		} else {
 			toolBar.add(action);
 		}
 	}
 
 	/**
 	 * @author Ben Kirby Takes the method of setting up the Zoom menu out of the
 	 *         main buildMenus method.
 	 * @param JMenu
 	 *            - the menu to add the submenu to
 	 */
 	private void addZoomMenuItems(JMenu zoomMenu) {
 		for (int i = 0; i <= zoomExamples.length - 1; i++) {
 			JMenuItem newItem = new JMenuItem(new ZoomAction(zoomExamples[i],
 					"Select zoom percentage", i < 10 ? "ctrl shift " + i : ""));
 			zoomMenu.add(newItem);
 		}
 	}
 
 	/**
 	 * @author Ben Kirby Just takes the long-winded method of setting up the
 	 *         ComboBox out of the main buildToolbar method. Could be adapted
 	 *         for generic addition of comboboxes
 	 * @param toolBar
 	 *            the JToolBar to add the button to
 	 * @param action
 	 *            the action that the ZoomComboBox performs
 	 */
 	private void addZoomComboBox(JToolBar toolBar, Action action) {
 		Dimension zoomComboBoxDimension = new Dimension(65, 28);
 		zoomComboBox = new JComboBox(zoomExamples);
 		zoomComboBox.setEditable(true);
 		zoomComboBox.setSelectedItem("100%");
 		zoomComboBox.setMaximumRowCount(zoomExamples.length);
 		zoomComboBox.setMaximumSize(zoomComboBoxDimension);
 		zoomComboBox.setMinimumSize(zoomComboBoxDimension);
 		zoomComboBox.setPreferredSize(zoomComboBoxDimension);
 		zoomComboBox.setAction(action);
 		toolBar.add(zoomComboBox);
 	}
 
 	/**
 	 * @author Alex Charalambous (June 2010): Creates a combo box used to choose
 	 *         the current token class to be used.
 	 */
 	private void addTokenClassComboBox(JToolBar toolBar, Action action) {
 		LinkedList<TokenClass> tokenClasses;
 		if (appModel == null) {
 			tokenClasses = new LinkedList<TokenClass>();
 			TokenClass tc = new TokenClass(true, "默认(黑)", Color.black);
 //			TokenClass tc = new TokenClass(true, "Default", Color.black);
 			tokenClasses.add(tc);
 		} else {
 			tokenClasses = CreateGui.getModel().getTokenClasses();
 		}
 		int noEnabledTokenClasses = 0;
 		for (TokenClass tc : tokenClasses) {
 			if (tc.isEnabled())
 				noEnabledTokenClasses++;
 		}
 
 		// Create an array that can be passed to DefaultComboBox below
 		// The array contains any specified token classes so they can be
 		// displayed as options in the combo box
 		String[] tokenClassChoices = new String[noEnabledTokenClasses];
 		int noTokenClasses = tokenClasses.size();
 		int arrayPos = 0;
 		for (int i = 0; i < noTokenClasses; i++) {
 			if (tokenClasses.get(i).isEnabled()) {
 				tokenClassChoices[arrayPos] = tokenClasses.get(i).getID();
 				arrayPos++;
 			}
 		}
 		DefaultComboBoxModel model = new DefaultComboBoxModel(tokenClassChoices);
 		tokenClassComboBox = new JComboBox(model);
 		tokenClassComboBox.setEditable(true);
 		tokenClassComboBox.setSelectedItem(tokenClassChoices[0]);
 		tokenClassComboBox.setMaximumRowCount(100);
 		tokenClassComboBox.setMaximumSize(new Dimension(125, 100));
 		tokenClassComboBox.setEditable(false);
 		tokenClassComboBox.setAction(action);
 		toolBar.add(tokenClassComboBox);
 	}
 
 	private JMenuItem addMenuItem(JMenu menu, Action action) {
 		JMenuItem item = menu.add(action);
 		KeyStroke keystroke = (KeyStroke) action
 				.getValue(Action.ACCELERATOR_KEY);
 
 		if (keystroke != null) {
 			item.setAccelerator(keystroke);
 		}
 		return item;
 	}
 
 	/* sets all buttons to enabled or disabled according to status. */
 	private void enableActions(boolean status) {
 
 		saveAction.setEnabled(status);
 		saveAsAction.setEnabled(status);
 
 		placeAction.setEnabled(status);
 		arcAction.setEnabled(status);
 		inhibarcAction.setEnabled(status);
 		annotationAction.setEnabled(status);
 		transAction.setEnabled(status);
 		timedtransAction.setEnabled(status);
 		determintransAction.setEnabled(status);
 		tokenAction.setEnabled(status);
 		deleteAction.setEnabled(status);
 		selectAction.setEnabled(status);
 		deleteTokenAction.setEnabled(status);
 		rateAction.setEnabled(status);
 
 		// toggleGrid.setEnabled(status);
 
 		if (status) {
 			startAction.setSelected(false);
 			randomAnimateAction.setSelected(false);
 			stepbackwardAction.setEnabled(!status);
 			stepforwardAction.setEnabled(!status);
 			drawingToolBar.setVisible(true);
 			animationToolBar.setVisible(false);
 		}
 		randomAction.setEnabled(!status);
 		randomAnimateAction.setEnabled(!status);
 
 		if (!status) {
 			drawingToolBar.setVisible(false);
 			animationToolBar.setVisible(true);
 			pasteAction.setEnabled(status);
 			undoAction.setEnabled(status);
 			redoAction.setEnabled(status);
 		} else {
 			pasteAction.setEnabled(getCopyPasteManager().pasteEnabled());
 		}
 		copyAction.setEnabled(status);
 		cutAction.setEnabled(status);
 		deleteAction.setEnabled(status);
 	}
 
 	// set frame objects by array index
 	private void setObjects(int index) {
 		appModel = CreateGui.getModel(index);
 		appView = CreateGui.getView(index);
 	}
 
 	// HAK set current objects in Frame
 	public void setObjects() {
 		appModel = CreateGui.getModel();
 		appView = CreateGui.getView();
 	}
 
 	private void setObjectsNull(int index) {
 		CreateGui.removeTab(index);
 	}
 
 	// set tabbed pane properties and add change listener that updates tab with
 	// linked model and view
 	public void setTab() {
 
 		appTab = CreateGui.getTab();
 		appTab.addChangeListener(new ChangeListener() {
 
 			public void stateChanged(ChangeEvent e) {
 
 				if (appGui.getCopyPasteManager().pasteInProgress()) {
 					appGui.getCopyPasteManager().cancelPaste();
 				}
 
 				int index = appTab.getSelectedIndex();
 				setObjects(index);
 				if (appView != null) {
 					appView.setVisible(true);
 					appView.repaint();
 					updateZoomCombo();
 
 					enableActions(!appView.isInAnimationMode());
 					// CreateGui.getAnimator().restoreModel();
 					// CreateGui.removeAnimationHistory();
 
 					setTitle(appTab.getTitleAt(index));
 
 					// TODO: change this code... it's ugly :)
 					if (appGui.getMode() == Constants.SELECT) {
 						appGui.init();
 					}
 
 				} else {
 					setTitle(null);
 				}
 				// Different tabs use different token classes. This should be
 				// updated in the choose token class comboBox
 				if (appModel != null)
 					refreshTokenClassChoices();
 
 			}
 
 		});
 		appGui = CreateGui.getApp();
 		appView = CreateGui.getView();
 	}
 
 	// Less sucky yet far, far simpler to code About dialogue
 	public void actionPerformed(ActionEvent e) {
 
 		JOptionPane
 				.showMessageDialog(
 						this,
 						"AMAP: AFDX Model and Analysis based Petri Net Tool\n\n"
 								+ "本软件在开源软件PIPE(http://pipe2.sourceforge.net/)的基础上进行二次开发\n"
 								+ "主要针对航空电子系统，进行Petri网建模及仿真。\n\n",
 								 
 						"关于本软件", JOptionPane.INFORMATION_MESSAGE);
 	}
 
 	// HAK Method called by netModel object when it changes
 	public void update(Observable o, Object obj) {
 		if ((mode != Constants.CREATING) && (!appView.isInAnimationMode())) {
 			appView.setNetChanged(true);
 		}
 	}
 
 	public void saveOperation(boolean forceSaveAs) {
 
 		if (appView == null) {
 			return;
 		}
 
 		File modelFile = CreateGui.getFile();
 		if (!forceSaveAs && modelFile != null) { // ordinary save
 			/*
 			 * //Disabled as currently ALWAYS prevents the net from being saved
 			 * - Nadeem 26/05/2005 if (!appView.netChanged) { return; }
 			 */
 
 			saveNet(modelFile);
 		} else { // save as
 			String path = null;
 			if (modelFile != null) {
 				path = modelFile.toString();
 			} else {
 				path = appTab.getTitleAt(appTab.getSelectedIndex());
 			}
 			String filename = new FileBrowser(path).saveFile();
 			if (filename != null) {
 				saveNet(new File(filename));
 			}
 		}
 	}
 
 	private void saveNet(File outFile) {
 		try {
 			// BK 10/02/07:
 			// changed way of saving to accomodate new DataLayerWriter class
 			DataLayerWriter saveModel = new DataLayerWriter(appModel);
 			saveModel.savePNML(outFile);
 			// appModel.savePNML(outFile);
 
 			CreateGui.setFile(outFile, appTab.getSelectedIndex());
 			appView.setNetChanged(false);
 			appTab.setTitleAt(appTab.getSelectedIndex(), outFile.getName());
 			setTitle(outFile.getName()); // Change the window title
			appView.getUndoManager().clear(); 
 			undoAction.setEnabled(false);
 			redoAction.setEnabled(false);
 		} catch (Exception e) {
 			System.err.println(e);
 			e.printStackTrace();
 			JOptionPane.showMessageDialog(GuiFrame.this, e.toString(),
 					"File Output Error", JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 	}
 
 	/**
 	 * Creates a new tab with the selected file, or a new file if filename==null
 	 * 
 	 * @param filename
 	 *            Filename of net to load, or <b>null</b> to create a new, empty
 	 *            tab
 	 */
 	public void createNewTab(File file, boolean isTN) {
 		int freeSpace = CreateGui.getFreeSpace();
 		String name = "";
 
 		// if we are in the middle of a paste action, we cancel it because we
 		// will
 		// create a new tab now
 		if (this.getCopyPasteManager().pasteInProgress()) {
 			this.getCopyPasteManager().cancelPaste();
 		}
 
 		setObjects(freeSpace);
 
 		appModel.addObserver((Observer) appView); // Add the view as Observer
 		appModel.addObserver((Observer) appGui); // Add the app window as
 		// observer
 
 		if (file == null) {
 //			name = "New Petri net " + (newNameCounter++) + ".xml";
 			name = "模型 " + (newNameCounter++) + ".xml";
 		} else {
 			try {
 				// BK 10/02/07: Changed loading of PNML to accomodate new
 				// PNMLTransformer class
 				CreateGui.setFile(file, freeSpace);
 				if (isTN) {
 					TNTransformer transformer = new TNTransformer();
 					appModel.createFromPNML(transformer.transformTN(file
 							.getPath()));
 				} else {
 					// ProgressBar pb = new ProgressBar("test");
 					PNMLTransformer transformer = new PNMLTransformer();
 					appModel.createFromPNML(transformer.transformPNML(file
 							.getPath()));
 					appView.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
 				}
 
 				CreateGui.setFile(file, freeSpace);
 				name = file.getName();
 			} catch (Exception e) {
 				JOptionPane.showMessageDialog(GuiFrame.this,
 						"Error loading file:\n" + name + "\nGuru meditation:\n"
 								+ e.toString(), "File load error",
 						JOptionPane.ERROR_MESSAGE);
 				e.printStackTrace();
 				return;
 			}
 		}
 
 		appView.setNetChanged(false); // Status is unchanged
 
 		JScrollPane scroller = new JScrollPane(appView);
 		// make it less bad on XP
 		scroller.setBorder(new BevelBorder(BevelBorder.LOWERED));
 		appTab.addTab(name, null, scroller, null);
 		appTab.setSelectedIndex(freeSpace);
 
 		appView.updatePreferredSize();
 		// appView.add( new ViewExpansionComponent(appView.getWidth(),
 		// appView.getHeight());
 
 		setTitle(name);// Change the program caption
 		appTab.setTitleAt(freeSpace, name);
 		selectAction.actionPerformed(null);
 	}
 
 	/*
 	 * public class ExperimentRunner extends Thread{
 	 * 
 	 * private String path;
 	 * 
 	 * public ExperimentRunner(String path){ this.path=path; }
 	 * 
 	 * public void run(){ Experiment exp = new Experiment(path,appModel); try{
 	 * exp.Load(); } catch(org.xml.sax.SAXParseException spe) { //if the
 	 * experiment file does not fit the schema. String message =
 	 * spe.getMessage().replaceAll("\\. ",".\n"); message =
 	 * message.replaceAll(",",",\n");
 	 * JOptionPane.showMessageDialog(GuiFrame.this,
 	 * "The Experiment file is not valid."+
 	 * System.getProperty("line.separator")+ "Line "+spe.getLineNumber()+": "+
 	 * message, "Experiment Input Error", JOptionPane.ERROR_MESSAGE); }
 	 * catch(pipe.experiment.validation.NotMatchingException nme) { //if the
 	 * experiment file does not match with the current net.
 	 * JOptionPane.showMessageDialog(GuiFrame.this,
 	 * "The Experiment file is not valid."+
 	 * System.getProperty("line.separator")+ nme.getMessage(),
 	 * "Experiment Input Error", JOptionPane.ERROR_MESSAGE); }
 	 * catch(pipe.experiment.InvalidExpressionException iee) {
 	 * JOptionPane.showMessageDialog(GuiFrame.this,
 	 * "The Experiment file is not valid."+
 	 * System.getProperty("line.separator")+ iee.getMessage(),
 	 * "Experiment Input Error", JOptionPane.ERROR_MESSAGE); } } }
 	 */
 
 	/**
 	 * If current net has modifications, asks if you want to save and does it if
 	 * you want.
 	 * 
 	 * @return true if handled, false if cancelled
 	 */
 	private boolean checkForSave() {
 
 		if (appView.getNetChanged()) {
 			int result = JOptionPane.showConfirmDialog(GuiFrame.this,
 //					"Current file has changed. Save current file?",
 //					"Confirm Save Current File",
 					
 					"是否将更改保存?",
 					"AMAP",
 					JOptionPane.YES_NO_CANCEL_OPTION,
 					JOptionPane.WARNING_MESSAGE);
 			switch (result) {
 			case JOptionPane.YES_OPTION:
 				saveOperation(false);
 				break;
 			case JOptionPane.CLOSED_OPTION:
 			case JOptionPane.CANCEL_OPTION:
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * If current net has modifications, asks if you want to save and does it if
 	 * you want.
 	 * 
 	 * @return true if handled, false if cancelled
 	 */
 	private boolean checkForSaveAll() {
 		// Loop through all tabs and check if they have been saved
 		for (int counter = 0; counter < appTab.getTabCount(); counter++) {
 			appTab.setSelectedIndex(counter);
 			if (checkForSave() == false) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public void setRandomAnimationMode(boolean on) {
 		if (on == false) {
 			stepforwardAction.setEnabled(CreateGui.getAnimationHistory()
 					.isStepForwardAllowed());
 			stepbackwardAction.setEnabled(CreateGui.getAnimationHistory()
 					.isStepBackAllowed());
 		} else {
 			stepbackwardAction.setEnabled(false);
 			stepforwardAction.setEnabled(false);
 		}
 		randomAction.setEnabled(!on);
 		randomAnimateAction.setSelected(on);
 	}
 
 	private void setAnimationMode(boolean on) {
 		randomAnimateAction.setSelected(false);
 		CreateGui.getAnimator().setNumberSequences(0);
 		startAction.setSelected(on);
 		CreateGui.getView().changeAnimationMode(on);
 		if (on) {
 			CreateGui.getAnimator().storeModel();
 			CreateGui.currentPNMLData().setEnabledTransitions();
 			CreateGui.getAnimator().highlightEnabledTransitions();
 			CreateGui.addAnimationHistory();
 			enableActions(false);// disables all non-animation buttons
 			setEditionAllowed(false);
 			statusBar.changeText(statusBar.textforAnimation);
 		} else {
 			setEditionAllowed(true);
 			statusBar.changeText(statusBar.textforDrawing);
 			CreateGui.getAnimator().restoreModel();
 			CreateGui.removeAnimationHistory();
 			enableActions(true); // renables all non-animation buttons
 		}
 	}
 
 	public void resetMode() {
 		setMode(old_mode);
 	}
 
 	public void enterFastMode(int _mode) {
 		old_mode = mode;
 		setMode(_mode);
 	}
 
 	public int getOldMode() { // NOU-PERE
 		return old_mode;
 	}
 
 	public void setMode(int _mode) {
 		// Don't bother unless new mode is different.
 		if (mode != _mode) {
 			prev_mode = mode;
 			mode = _mode;
 		}
 	}
 
 	public int getMode() {
 		return mode;
 	}
 
 	public void restoreMode() {
 		mode = prev_mode;
 		placeAction.setSelected(mode == Constants.PLACE);
 		transAction.setSelected(mode == Constants.IMMTRANS);
 		timedtransAction.setSelected(mode == Constants.TIMEDTRANS);
 		determintransAction.setSelected(mode == Constants.DETERMINRANS);
 		arcAction.setSelected(mode == Constants.ARC);
 		inhibarcAction.setSelected(mode == Constants.INHIBARC);
 		tokenAction.setSelected(mode == Constants.ADDTOKEN);
 		deleteTokenAction.setSelected(mode == Constants.DELTOKEN);
 		rateAction.setSelected(mode == Constants.RATE);
 		selectAction.setSelected(mode == Constants.SELECT);
 		annotationAction.setSelected(mode == Constants.ANNOTATION);
 	}
 
 	public void setTitle(String title) {
 		super
 				.setTitle((title == null) ? frameTitle : frameTitle + ": "
 						+ title);
 	}
 
 	public boolean isEditionAllowed() {
 		return editionAllowed;
 	}
 
 	public void setEditionAllowed(boolean flag) {
 		editionAllowed = flag;
 	}
 
 	public void setUndoActionEnabled(boolean flag) {
 		undoAction.setEnabled(flag);
 	}
 
 	public void setRedoActionEnabled(boolean flag) {
 		redoAction.setEnabled(flag);
 	}
 
 	public CopyPasteManager getCopyPasteManager() {
 		return copyPasteManager;
 	}
 
 	public void init() {
 		// Set selection mode at startup
 		setMode(Constants.SELECT);
 		selectAction.actionPerformed(null);
 	}
 
 	/**
 	 * @author Ben Kirby Remove the listener from the zoomComboBox, so that when
 	 *         the box's selected item is updated to keep track of ZoomActions
 	 *         called from other sources, a duplicate ZoomAction is not called
 	 */
 	public void updateZoomCombo() {
 		ActionListener zoomComboListener = (zoomComboBox.getActionListeners())[0];
 		zoomComboBox.removeActionListener(zoomComboListener);
 		zoomComboBox.setSelectedItem(String.valueOf(appView.getZoomController()
 				.getPercent())
 				+ "%");
 		zoomComboBox.addActionListener(zoomComboListener);
 	}
 
 	public StatusBar getStatusBar() {
 		return statusBar;
 	}
 
 	private Component c = null; // arreglantzoom
 	private Component p = new BlankLayer(this);
 
 	/* */
 	void hideNet(boolean doHide) {
 		if (doHide) {
 			c = appTab.getComponentAt(appTab.getSelectedIndex());
 			appTab.setComponentAt(appTab.getSelectedIndex(), p);
 		} else {
 			if (c != null) {
 				appTab.setComponentAt(appTab.getSelectedIndex(), c);
 				c = null;
 			}
 		}
 		appTab.repaint();
 	}
 
 	class AnimateAction extends GuiAction {
 
 		private int typeID;
 		private AnimationHistory animBox;
 
 		AnimateAction(String name, int typeID, String tooltip, String keystroke) {
 			super(name, tooltip, keystroke);
 			this.typeID = typeID;
 		}
 
 		AnimateAction(String name, int typeID, String tooltip,
 				String keystroke, boolean toggleable) {
 			super(name, tooltip, keystroke, toggleable);
 			this.typeID = typeID;
 		}
 
 		public void actionPerformed(ActionEvent ae) {
 			if (appView == null) {
 				return;
 			}
 
 			animBox = CreateGui.getAnimationHistory();
 
 			switch (typeID) {
 			case Constants.START:
 				try {
 					setAnimationMode(!appView.isInAnimationMode());
 					if (!appView.isInAnimationMode()) {
 						restoreMode();
 						PetriNetObject.ignoreSelection(false);
 					} else {
 						setMode(typeID);
 						PetriNetObject.ignoreSelection(true);
 						// Do we keep the selection??
 						appView.getSelectionObject().clearSelection();
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 					JOptionPane.showMessageDialog(GuiFrame.this, e.toString(),
 							"Animation Mode Error", JOptionPane.ERROR_MESSAGE);
 					startAction.setSelected(false);
 					appView.changeAnimationMode(false);
 				}
 				stepforwardAction.setEnabled(false);
 				stepbackwardAction.setEnabled(false);
 				break;
 
 			case Constants.RANDOM:
 				animBox.clearStepsForward();
 				CreateGui.getAnimator().doRandomFiring();
 				stepforwardAction.setEnabled(animBox.isStepForwardAllowed());
 				stepbackwardAction.setEnabled(animBox.isStepBackAllowed());
 				break;
 
 			case Constants.STEPFORWARD:
 				animBox.stepForward();
 				CreateGui.getAnimator().stepForward();
 				stepforwardAction.setEnabled(animBox.isStepForwardAllowed());
 				stepbackwardAction.setEnabled(animBox.isStepBackAllowed());
 				break;
 
 			case Constants.STEPBACKWARD:
 				animBox.stepBackwards();
 				CreateGui.getAnimator().stepBack();
 				stepforwardAction.setEnabled(animBox.isStepForwardAllowed());
 				stepbackwardAction.setEnabled(animBox.isStepBackAllowed());
 				break;
 
 			case Constants.ANIMATE:
 				Animator a = CreateGui.getAnimator();
 
 				if (a.getNumberSequences() > 0) {
 					a.setNumberSequences(0); // stop animation
 					setSelected(false);
 				} else {
 					stepbackwardAction.setEnabled(false);
 					stepforwardAction.setEnabled(false);
 					randomAction.setEnabled(false);
 					setSelected(true);
 					animBox.clearStepsForward();
 					CreateGui.getAnimator().startRandomFiring();
 				}
 				break;
 
 			default:
 				break;
 			}
 		}
 
 	}
 
 	class ExampleFileAction extends GuiAction {
 
 		private File filename;
 
 		ExampleFileAction(File file, String keyStroke) {
 			super(file.getName(), "Open example file \"" + file.getName()
 					+ "\"", keyStroke);
 			filename = file;// .getAbsolutePath();
 			putValue(SMALL_ICON, new ImageIcon(Thread.currentThread()
 					.getContextClassLoader().getResource(
 							CreateGui.imgPath + "Net.png")));
 		}
 
 		ExampleFileAction(JarEntry entry, String keyStroke) {
 			super(entry.getName().substring(
 					1 + entry.getName().indexOf(
 							System.getProperty("file.separator"))),
 					"Open example file \"" + entry.getName() + "\"", keyStroke);
 			putValue(SMALL_ICON, new ImageIcon(Thread.currentThread()
 					.getContextClassLoader().getResource(
 							CreateGui.imgPath + "Net.png")));
 
 			filename = JarUtilities.getFile(entry);// .getPath();
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			createNewTab(filename, false);
 		}
 
 	}
 
 	class DeleteAction extends GuiAction {
 
 		DeleteAction(String name, String tooltip, String keystroke) {
 			super(name, tooltip, keystroke);
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			appView.getUndoManager().newEdit(); // new "transaction""
 			appView.getUndoManager().deleteSelection(
 					appView.getSelectionObject().getSelection());
 			appView.getSelectionObject().deleteSelection();
 		}
 
 	}
 
 	class TypeAction extends GuiAction {
 
 		private int typeID;
 
 		TypeAction(String name, int typeID, String tooltip, String keystroke) {
 			super(name, tooltip, keystroke);
 			this.typeID = typeID;
 		}
 
 		TypeAction(String name, int typeID, String tooltip, String keystroke,
 				boolean toggleable) {
 			super(name, tooltip, keystroke, toggleable);
 			this.typeID = typeID;
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			// if (!isSelected()){
 			this.setSelected(true);
 
 			// deselect other actions
 			if (this != placeAction) {
 				placeAction.setSelected(false);
 			}
 			if (this != transAction) {
 				transAction.setSelected(false);
 			}
 			if (this != timedtransAction) {
 				timedtransAction.setSelected(false);
 			}
 			if (this != determintransAction) {
 				determintransAction.setSelected(false);
 			}
 			if (this != arcAction) {
 				arcAction.setSelected(false);
 			}
 			if (this != inhibarcAction) {
 				inhibarcAction.setSelected(false);
 			}
 			if (this != tokenAction) {
 				tokenAction.setSelected(false);
 			}
 			if (this != deleteTokenAction) {
 				deleteTokenAction.setSelected(false);
 			}
 			if (this != rateAction) {
 				rateAction.setSelected(false);
 			}
 			if (this != selectAction) {
 				selectAction.setSelected(false);
 			}
 			if (this != annotationAction) {
 				annotationAction.setSelected(false);
 			}
 			/*if (this != dragAction) {
 				dragAction.setSelected(false);
 			}*/
 
 			if (appView == null) {
 				return;
 			}
 
 			appView.getSelectionObject().disableSelection();
 			// appView.getSelectionObject().clearSelection();
 
 			setMode(typeID);
 			statusBar.changeText(typeID);
 
 			if ((typeID != Constants.ARC) && (appView.createArc != null)) {
 				appView.createArc.delete();
 				appView.createArc = null;
 				appView.repaint();
 			}
 
 			if (typeID == Constants.SELECT) {
 				// disable drawing to eliminate possiblity of connecting arc to
 				// old coord of moved component
 				statusBar.changeText(typeID);
 				appView.getSelectionObject().enableSelection();
 				appView.setCursorType("arrow");
 			} else if (typeID == Constants.DRAG) {
 				appView.setCursorType("move");
 			} else {
 				appView.setCursorType("crosshair");
 			}
 		}
 		// }
 
 	}
 
 	class GridAction extends GuiAction {
 
 		GridAction(String name, String tooltip, String keystroke) {
 			super(name, tooltip, keystroke);
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			Grid.increment();
 			repaint();
 		}
 
 	}
 
 	/**
 	 * @author Alex Charalambous, June 2010: Handles the combo box used to
 	 *         select the current token class in use.
 	 **/
 	class ChooseTokenClassAction extends GuiAction {
 		ChooseTokenClassAction(String name, String tooltip, String keystroke) {
 			super(name, tooltip, keystroke);
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			String currentSelection = (String) tokenClassComboBox
 					.getSelectedItem();
 			LinkedList<TokenClass> tokenClasses = CreateGui.getModel()
 					.getTokenClasses();
 			for (TokenClass tc : tokenClasses) {
 				if (tc.getID().equals(currentSelection)) {
 					CreateGui.getModel().setActiveTokenClass(tc);
 					break;
 				}
 			}
 		}
 	}
 
 	/**
 	 * @author Alex Charalambous, June 2010: Groups any transitions that have
 	 *         the same inputs and outputs. Only does anything if this is a
 	 *         coloured petri net
 	 **/
 	class GroupTransitionsAction extends GuiAction {
 
 		GroupTransitionsAction(String name, String tooltip, String keystroke) {
 			super(name, tooltip, keystroke);
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			/* NOTE: With the current implementation we must clear the undo
 			 * history before performing this action otherwise undoing a 
 			 * previous grouping could cause a mixup. In the future this
 			 * should all be done as a single undo transaction.
 			 */
 			CreateGui.getView().getUndoManager().clear();
 			LinkedList<GroupTransition> newGroupTransitions = new LinkedList<GroupTransition>();
 			System.out.println(CreateGui.getModel().getTokenClasses().size());
 			if (CreateGui.getModel().getTokenClasses().size() > 1) {
 				GuiView view = CreateGui.getView();
 				DataLayerInterface model = CreateGui.getModel();
 				Transition[] transitions = model.getTransitions();
 				Boolean firstAddition;
 				for (int i = 0; i < transitions.length - 1; i++) {
 					GroupTransition newGroupTransition = new GroupTransition(transitions[i],
 							transitions[i].getPositionX(), transitions[i]
 									.getPositionY());
 					firstAddition = true;
 					if (!transitions[i].isGrouped()) {
 						for (int j = i + 1; j < transitions.length; j++) {
 							if (!transitions[j].isGrouped()) {
 								Iterator arcsFromT1 = transitions[i]
 										.getArcsFrom();
 								Iterator arcsFromT2;
 								Iterator arcsToT1 = transitions[i].getArcsTo();
 								Iterator arcsToT2;
 								boolean allSourcesAndTargetsMatch = true;
 								while (arcsFromT1.hasNext()) {
 									Arc arcFromT1 = (Arc) (arcsFromT1.next());
 									arcsFromT2 = transitions[j].getArcsFrom();
 									boolean thisPairMatches = false;
 									while (arcsFromT2.hasNext()) {
 										Arc arcFromT2 = (Arc) (arcsFromT2
 												.next());
 										if (arcFromT2.getTarget().equals(
 												arcFromT1.getTarget())) {
 											thisPairMatches = true;
 											break;
 										}
 									}
 									if (!thisPairMatches) {
 										allSourcesAndTargetsMatch = false;
 										break;
 									}
 								}
 								Arc arcToT1 = (Arc) (arcsToT1.next());
 								arcsToT2 = transitions[j].getArcsTo();
 								boolean thisPairMatches = false;
 								while (arcsToT2.hasNext()) {
 									Arc arcToT2 = (Arc) (arcsToT2
 											.next());
 									if (arcToT2.getSource().equals(
 											arcToT1.getSource())) {
 										thisPairMatches = true;
 										break;
 									}
 								}
 								if (!thisPairMatches) {
 									allSourcesAndTargetsMatch = false;
 									break;
 								}
 								if (allSourcesAndTargetsMatch) {
 									// First entry in groupTransition. Hence initialize.
 									if (!transitions[i].isGrouped()) {
 										// Add new input arcs to our new Grouped transition
 										Iterator arcsTo = transitions[i].getArcsTo();
 										Arc tempArc;
 										while (arcsTo.hasNext()) {
 											tempArc = (Arc)(Arc)arcsTo.next();
 											
 											Arc newArc = ArcFactory.createNormalArc(
 													tempArc.getStartPositionX(), tempArc.getStartPositionY(),
 													tempArc.getArcPath().getPoint(1).getX(), tempArc.getArcPath().getPoint(1).getY(),
 													tempArc.getSource(), newGroupTransition,
 													new LinkedList<Marking>(), "", false);
 											newGroupTransition.addConnectTo(newArc);
 											tempArc.getSource().addConnectFrom(newArc);
 											newArc.addToView(view);
 										}
 										// Add new output arcs to our new Grouped transition
 										Iterator arcsFrom = transitions[i].getArcsFrom();
 										while (arcsFrom.hasNext()) {
 											tempArc = (Arc)(Arc)arcsFrom.next();
 											Arc newArc = ArcFactory.createNormalArc(
 													tempArc.getStartPositionX(), tempArc.getStartPositionY(),
 													tempArc.getArcPath().getPoint(1).getX(), tempArc.getArcPath().getPoint(1).getY(),
 													newGroupTransition, tempArc.getTarget(),
 													new LinkedList<Marking>(), "", false);
 											newGroupTransition.addConnectFrom(newArc);
 											tempArc.getTarget().addConnectTo(newArc);
 											newArc.addToView(view);
 										}
 										newGroupTransition
 												.addTransition(transitions[i]);
 										if(firstAddition){
 											newGroupTransition.setName(transitions[i].getId());
 											firstAddition = false;
 										}
 										else{
 											newGroupTransition.setName(newGroupTransition.getName() + 
 													"_" + transitions[i].getId());
 										}
 										transitions[i]
 												.bindToGroup(newGroupTransition);
 										newGroupTransitions.add(newGroupTransition);
 									}
 									newGroupTransition
 											.addTransition(transitions[j]);
 									if(firstAddition){
 										newGroupTransition.setName(transitions[j].getId());
 										firstAddition = false;
 									}
 									else{
 										newGroupTransition.setName(newGroupTransition.getName() + 
 												"_" + transitions[j].getId());
 									}
 									transitions[j]
 											.bindToGroup(newGroupTransition);
 								}
 							}
 
 						}
 					}
 				}
 				for(GroupTransition groupTransition:newGroupTransitions){
 					for (Transition t : groupTransition.getTransitions()) {
 						t.hideFromCanvas();
 						t.hideAssociatedArcs();
 					}
 					model.addPetriNetObject(groupTransition);
 					view.addNewPetriNetObject(groupTransition);
 					groupTransition.setVisible(true);
 				}
 				
 			}
 		}
 	}
 
 	/**
 	 * @author Alex Charalambous, June 2010: Ungroups any transitions that have
 	 *         been previously grouped. Only does anything if this is a
 	 *         coloured petri net
 	 **/
 	class UngroupTransitionsAction extends GuiAction {
 
 		UngroupTransitionsAction(String name, String tooltip, String keystroke) {
 			super(name, tooltip, keystroke);
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			/* NOTE: With the current implementation we must clear the undo
 			 * history before performing this action otherwise undoing a 
 			 * previous grouping could cause a mixup. In the future this
 			 * should all be done as a single undo transaction.
 			 */
 			CreateGui.getView().getUndoManager().clear();
 			LinkedList<GroupTransition> transitionsToUngroup = new LinkedList<GroupTransition>();
 			if (CreateGui.getModel().getTokenClasses().size() > 1) {
 				DataLayerInterface model = CreateGui.getModel();
 				Transition[] transitions = model.getTransitions();
 				for(int i = 0; i < transitions.length; i++){
 					if(transitions[i].isGrouped()){
 						if(!transitionsToUngroup.contains(transitions[i].getGroup()))
 							transitionsToUngroup.add(transitions[i].getGroup());
 					}
 				}
 				for(GroupTransition groupTransition:transitionsToUngroup){
 		        	UndoableEdit edit = groupTransition.ungroupTransitions();
 		        	CreateGui.getView().getUndoManager().addNewEdit(edit);
 		        	groupTransition.deleteAssociatedArcs();
 		        	groupTransition.setVisible(false);
 		    		groupTransition.getNameLabel().setVisible(false);
 				}
 				
 			}
 		}
 	}
 	
 	/**
 	 * @author Alex Charalambous, June 2010: Unfolds a coloured Petri net
 	 * to an ordinary Petri net.
 	 **/
 	class UnfoldAction extends GuiAction {
 
 		UnfoldAction(String name, String tooltip, String keystroke) {
 			super(name, tooltip, keystroke);
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			Unfolder unfolder = new Unfolder(CreateGui.getModel());
 			DataLayerInterface unfolded = unfolder.unfold();
 			createNewTab(unfolder.saveAsXml(unfolded),false);
 		}
 	}
 	/**
 	 * @author Alex Charalambous, June 2010: Handles the dialog that comes up
 	 *         when the Specify Token Classes button is pressed.
 	 **/
 	class TokenClassAction extends GuiAction {
 		private JDialog guiDialog;
 		private TokenClassPanel dialogContent;
 
 		TokenClassAction(String name, String tooltip, String keystroke) {
 			super(name, tooltip, keystroke);
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			guiDialog = new TokenClassDialog(CreateGui.getApp(),
 					//"Token Classes", true);
 					"标记分类", true);
 			guiDialog.setSize(600, 300);
 			guiDialog.setLocationRelativeTo(null);
 			dialogContent = new TokenClassPanel();
 			dialogContent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,
 					10));
 			dialogContent.setOpaque(true);
 
 			JPanel buttonPane = new JPanel();
 			buttonPane
 					.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
 			buttonPane
 					.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
 			buttonPane.add(Box.createHorizontalGlue());
 //			JButton ok = new JButton("OK");
 			JButton ok = new JButton("确定");
 			ok.addActionListener((ActionListener) guiDialog);
 			buttonPane.add(ok);
 			buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
 //			JButton cancel = new JButton("Cancel");
 			JButton cancel = new JButton("取消");
 			cancel.addActionListener((ActionListener) guiDialog);
 			buttonPane.add(cancel);
 
 			guiDialog.add(dialogContent, BorderLayout.CENTER);
 			guiDialog.add(buttonPane, BorderLayout.PAGE_END);
 			dialogContent.setVisible(true);
 
 			guiDialog.setVisible(true);
 			TableModel x = (TableModel) dialogContent.table.getModel();
 			int rows = x.getRowCount();
 			// If OK was pressed
 			if (((TokenClassDialog) guiDialog).shouldAcceptChanges()) {
 				LinkedList<TokenClass> tokenClasses = new LinkedList<TokenClass>();
 				for (int i = 0; i < rows; i++) {
 					// Update token classes using data entered from the user
 					TokenClass tc = new TokenClass(
 							(Boolean) x.getValueAt(i, 0), (String) x
 									.getValueAt(i, 1), (Color) x.getValueAt(i,
 									2));
 					tokenClasses.add(tc);
 				}
 				CreateGui.getModel().setTokenClasses(tokenClasses);
 				refreshTokenClassChoices();
 
 			}
 		}
 	}
 
 	/* This method can be used for simulating button clicks during testing
 	 * 
 	 */
 	public void executeAction(String action){
 		if(action.equals("toggleAnimation")){
 			startAction.actionPerformed(null);
 		}
 		else if(action.equals("groupTransitionsAction")){
 			groupTransitions.actionPerformed(null);
 		}
 		else if(action.equals("ungroupTransitionsAction")){
 			ungroupTransitions.actionPerformed(null);
 		}
 		else if(action.equals("exit")){
 			dispose();
 			System.exit(0);
 		}
 	}
 	public void refreshTokenClassChoices() {
 		LinkedList<TokenClass> tokenClasses = appModel.getTokenClasses();
 		int noEnabledTokenClasses = 0;
 		boolean refreshedActiveTokenClass = false;
 		// Find out how many token classes are enabled and set the
 		// first enabled token class as the default combo box selection
 		for (TokenClass tc : tokenClasses) {
 			if (tc.isEnabled()) {
 				noEnabledTokenClasses++;
 				if (!refreshedActiveTokenClass) {
 					refreshedActiveTokenClass = true;
 					appModel.setActiveTokenClass(tc);
 				}
 			}
 		}
 
 		// Options for choosing token class are based on all enabled token
 		// classes
 		String[] tokenClassChoices = new String[noEnabledTokenClasses];
 		int noTokenClasses = tokenClasses.size();
 		int arrayPos = 0;
 		for (int i = 0; i < noTokenClasses; i++) {
 			if (tokenClasses.get(i).isEnabled()) {
 				tokenClassChoices[arrayPos] = tokenClasses.get(i).getID();
 				arrayPos++;
 			}
 		}
 		// Update tokenClassComboBox choices to reflect user changes
 		DefaultComboBoxModel model = new DefaultComboBoxModel(tokenClassChoices);
 		tokenClassComboBox.setModel(model);
 	}
 
 	class ZoomAction extends GuiAction {
 
 		ZoomAction(String name, String tooltip, String keystroke) {
 			super(name, tooltip, keystroke);
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			boolean doZoom = false;
 			try {
 				String actionName = (String) getValue(NAME);
 				ZoomController zoomer = appView.getZoomController();
 				JViewport thisView = ((JScrollPane) appTab
 						.getSelectedComponent()).getViewport();
 				String selection = null, strToTest = null;
 
 				double midpointX = ZoomController.getUnzoomedValue(thisView
 						.getViewPosition().x
 						+ (thisView.getWidth() * 0.5), zoomer.getPercent());
 				double midpointY = ZoomController.getUnzoomedValue(thisView
 						.getViewPosition().y
 						+ (thisView.getHeight() * 0.5), zoomer.getPercent());
 
 				if (actionName.equals("放大")) {
 					doZoom = zoomer.zoomIn();
 				} else if (actionName.equals("缩小")) {
 					doZoom = zoomer.zoomOut();
 				} else {
 					if (actionName.equals("Zoom")) {
 						selection = (String) zoomComboBox.getSelectedItem();
 					}
 					if (e.getSource() instanceof JMenuItem) {
 						selection = ((JMenuItem) e.getSource()).getText();
 					}
 					strToTest = validatePercent(selection);
 
 					if (strToTest != null) {
 						// BK: no need to zoom if already at that level
 						if (zoomer.getPercent() == Integer.parseInt(strToTest)) {
 							return;
 						} else {
 							zoomer.setZoom(Integer.parseInt(strToTest));
 							doZoom = true;
 						}
 					} else {
 						return;
 					}
 				}
 				if (doZoom == true) {
 					updateZoomCombo();
 					appView.zoomTo(new java.awt.Point((int) midpointX,
 							(int) midpointY));
 				}
 			} catch (ClassCastException cce) {
 				// zoom
 			} catch (Exception ex) {
 				ex.printStackTrace();
 			}
 		}
 
 		private String validatePercent(String selection) {
 
 			try {
 				String toTest = selection;
 
 				if (selection.endsWith("%")) {
 					toTest = selection.substring(0, (selection.length()) - 1);
 				}
 
 				if (Integer.parseInt(toTest) < Constants.ZOOM_MIN
 						|| Integer.parseInt(toTest) > Constants.ZOOM_MAX) {
 					throw new Exception();
 				} else {
 					return toTest;
 				}
 			} catch (Exception e) {
 				zoomComboBox.setSelectedItem("");
 				return null;
 			}
 		}
 
 	}
 
 	class FileAction extends GuiAction {
 
 		// constructor
 		FileAction(String name, String tooltip, String keystroke) {
 			super(name, tooltip, keystroke);
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			if (this == saveAction) {
 				saveOperation(false); // code for Save operation
 			} else if (this == saveAsAction) {
 				saveOperation(true); // code for Save As operations
 			} else if (this == openAction) { // code for Open operation
 				File filePath = new FileBrowser(CreateGui.userPath).openFile();
 				if ((filePath != null) && filePath.exists()
 						&& filePath.isFile() && filePath.canRead()) {
 					CreateGui.userPath = filePath.getParent();
 					createNewTab(filePath, false);
 				}
 				if ((filePath != null) && (!filePath.exists())) {
 					String message = "File \"" + filePath.getName()
 							+ "\" does not exist.";
 					JOptionPane.showMessageDialog(null, message, "Warning",
 							JOptionPane.WARNING_MESSAGE);
 				}
 			} else if (this == importAction) {
 				File filePath = new FileBrowser(CreateGui.userPath).openFile();
 				if ((filePath != null) && filePath.exists()
 						&& filePath.isFile() && filePath.canRead()) {
 					CreateGui.userPath = filePath.getParent();
 					createNewTab(filePath, true);
 					appView.getSelectionObject().enableSelection();
 				}
 			} else if (this == createAction) {
 				createNewTab(null, false); // Create a new tab
 			} else if ((this == exitAction) && checkForSaveAll()) {
 				dispose();
 				System.exit(0);
 			} else if ((this == closeAction) && (appTab.getTabCount() > 0)
 					&& checkForSave()) {
 				setObjectsNull(appTab.getSelectedIndex());
 				appTab.remove(appTab.getSelectedIndex());
 			} else if (this == exportPNGAction) {
 				Export.exportGuiView(appView, Export.PNG, null);
 			} else if (this == exportPSAction) {
 				Export.exportGuiView(appView, Export.POSTSCRIPT, null);
 			} else if (this == exportTNAction) {
 				Export.exportGuiView(appView, Export.TN, appModel);
 			} else if (this == printAction) {
 				Export.exportGuiView(appView, Export.PRINTER, null);
 			}
 		}
 
 	}
 
 	class EditAction extends GuiAction {
 
 		EditAction(String name, String tooltip, String keystroke) {
 			super(name, tooltip, keystroke);
 		}
 
 		public void actionPerformed(ActionEvent e) {
 
  				if (this == cutAction) {
 					ArrayList selection = appView.getSelectionObject()
 							.getSelection();
 					appGui.getCopyPasteManager().doCopy(selection, appView);
 					appView.getUndoManager().newEdit(); // new "transaction""
 					appView.getUndoManager().deleteSelection(selection);
 					appView.getSelectionObject().deleteSelection();
 					pasteAction.setEnabled(appGui.getCopyPasteManager()
 							.pasteEnabled());
 				} else if (this == copyAction) {
 					appGui.getCopyPasteManager().doCopy(
 							appView.getSelectionObject().getSelection(),
 							appView);
 					pasteAction.setEnabled(appGui.getCopyPasteManager()
 							.pasteEnabled());
 				} else if (this == pasteAction) {
 					appView.getSelectionObject().clearSelection();
 					appGui.getCopyPasteManager().showPasteRectangle(appView);
 				} else if (this == undoAction) {
 					appView.getUndoManager().doUndo();
 				} else if (this == redoAction) {
 					appView.getUndoManager().doRedo();
 				}
  		}
 	}
 	
 	/**
 	 * 
 	 * @author Jerry-wang
 	 * 2011年7月14日 新加 主要处理建模button下面相关
 	 *
 	 */
 	class ModelAction extends GuiAction{
 
 		ModelAction(String name, String tooltip, String keystroke) {
 			super(name, tooltip, keystroke);
 		}
 
 		public void actionPerformed(ActionEvent e) {
 		//	if (this == guideAction) {
  				 // Build interface
 			      EscapableDialog guiDialog =
 			              new EscapableDialog(CreateGui.getApp(), "建模向导", true);
 
 			      // 1 Set layout
 			      Container contentPane = guiDialog.getContentPane();
 			      contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
 
 			      
 			      
 			      // 4 Add button
 			      contentPane.add(new ButtonBar("生成模型", classifyButtonClick,
 			              guiDialog.getRootPane()));
 
 			      // 5 Make window fit contents' preferred size
 			      guiDialog.pack();
 
 			      // 6 Move window to the middle of the screen
 			      guiDialog.setLocationRelativeTo(null);
 
 			      guiDialog.setVisible(true);
 				//}  
 			 
 		}
 		
 		ActionListener classifyButtonClick = new ActionListener() {
 
 		      public void actionPerformed(ActionEvent arg0) {
 		          System.out.println("here");
 		          
 		      }
 		   };
 	}
 
 	class ValidateAction extends GuiAction {
 
 		ValidateAction(String name, String tooltip, String keystroke) {
 			super(name, tooltip, keystroke);
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			// call the function to run validate.....
 			appModel.validTagStructure();
 		}
 	}
 
 	/**
 	 * A JToggleButton that watches an Action for selection change
 	 * 
 	 * @author Maxim
 	 * 
 	 *         Selection must be stored in the action using
 	 *         putValue("selected",Boolean);
 	 */
 	class ToggleButton extends JToggleButton implements PropertyChangeListener {
 
 		public ToggleButton(Action a) {
 			super(a);
 			if (a.getValue(Action.SMALL_ICON) != null) {
 				// toggle buttons like to have images *and* text, nasty
 				setText(null);
 			}
 			a.addPropertyChangeListener(this);
 		}
 
 		public void propertyChange(PropertyChangeEvent evt) {
 			if (evt.getPropertyName() == "selected") {
 				Boolean b = (Boolean) evt.getNewValue();
 				if (b != null) {
 					setSelected(b.booleanValue());
 				}
 			}
 		}
 
 	}
 
 	class WindowHandler extends WindowAdapter {
 		// Handler for window closing event
 		public void windowClosing(WindowEvent e) {
 			exitAction.actionPerformed(null);
 		}
 	}
 
 }
