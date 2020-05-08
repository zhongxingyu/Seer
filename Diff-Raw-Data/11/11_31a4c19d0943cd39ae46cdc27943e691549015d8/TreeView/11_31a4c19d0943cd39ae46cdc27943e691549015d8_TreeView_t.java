 
 package ClassAdminFrontEnd;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 
 import java.awt.Image;
 
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.PopupMenu;
 
 import java.awt.Toolkit;
 import java.awt.dnd.DragSource;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Point2D;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.KeyStroke;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 
 import prefuse.Constants;
 import prefuse.Display;
 import prefuse.Visualization;
 import prefuse.action.Action;
 import prefuse.action.ActionList;
 import prefuse.action.ItemAction;
 import prefuse.action.RepaintAction;
 import prefuse.action.animate.ColorAnimator;
 import prefuse.action.animate.LocationAnimator;
 import prefuse.action.animate.QualityControlAnimator;
 import prefuse.action.animate.VisibilityAnimator;
 import prefuse.action.assignment.ColorAction;
 import prefuse.action.assignment.FontAction;
 import prefuse.action.filter.FisheyeTreeFilter;
 import prefuse.action.layout.CollapsedSubtreeLayout;
 import prefuse.action.layout.graph.NodeLinkTreeLayout;
 import prefuse.activity.SlowInSlowOutPacer;
 import prefuse.controls.ControlAdapter;
 import prefuse.controls.FocusControl;
 import prefuse.controls.PanControl;
 import prefuse.controls.WheelZoomControl;
 import prefuse.data.Table;
 import prefuse.data.Tree;
 import prefuse.data.Tuple;
 import prefuse.data.event.TupleSetListener;
 import prefuse.data.io.TreeMLReader;
 import prefuse.data.search.PrefixSearchTupleSet;
 import prefuse.data.tuple.TupleSet;
 import prefuse.render.AbstractShapeRenderer;
 import prefuse.render.DefaultRendererFactory;
 import prefuse.render.EdgeRenderer;
 import prefuse.render.LabelRenderer;
 import prefuse.util.ColorLib;
 import prefuse.util.FontLib;
 import prefuse.util.PrefuseLib;
 import prefuse.util.ui.JFastLabel;
 import prefuse.visual.VisualItem;
 import prefuse.visual.expression.InGroupPredicate;
 import prefuse.visual.sort.TreeDepthItemSorter;
 import ClassAdminBackEnd.EntityType;
 import ClassAdminBackEnd.Project;
 import ClassAdminBackEnd.SuperEntity;
 import Frames.FrmNewNode;
 import Frames.FrmTreeHelpDialog;
 
 
 public class TreeView extends Display {
 
 	private static final String tree = "tree";
 	private static final String treeNodes = "tree.nodes";
 	private static final String treeEdges = "tree.edges";
 
 	private static Tree myTree = null;
 	private LabelRenderer m_nodeRenderer;
 	private EdgeRenderer m_edgeRenderer;
 
 	private String m_label = "label";
 	private int m_orientation = Constants.ORIENT_LEFT_RIGHT;
 	static TreeView tview;
 	static Cursor dc = new Cursor(Cursor.DEFAULT_CURSOR);
 	static Cursor yd = DragSource.DefaultMoveDrop;
 	static JFastLabel title = new JFastLabel("                 ");
 	static JFastLabel lblSelectedParent = new JFastLabel("no parent selected");
 	static JFastLabel lblSelectedChild = new JFastLabel("no child selected");
 	static JFastLabel lblParent = new JFastLabel("Parent: ");
 	static JFastLabel lblChild = new JFastLabel("Child: ");
 	static JButton btnNew = new JButton("Add Node");
 	static JButton btnChange = new JButton("Change");
 	static JButton btnHelp = new JButton("?");
 	static JTextArea txtChange = new JTextArea();
 	static FrmNewNode newNode;
 
 	static private boolean bParent = false;
 	static private boolean bChild = false;
 	private static Project myProject = null;
 
 	static int iParent = -1;
 	static int iChild = -1;
 	static int selectedEntity = -1;
 
 	public void updateTree(Tree newTree) {
 		myTree = newTree;
 	}
 
 	public TreeView(Tree t, String label) {
 		super(new Visualization());
 
 		myTree = t;
 		m_label = label;
 		m_vis.add(tree, t);
 
 		m_nodeRenderer = new LabelRenderer(m_label);
 		m_nodeRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_FILL);
 		m_nodeRenderer.setHorizontalAlignment(Constants.LEFT);
 		m_nodeRenderer.setRoundedCorner(8, 8);
 		m_edgeRenderer = new EdgeRenderer(Constants.EDGE_TYPE_CURVE);
 
 		DefaultRendererFactory rf = new DefaultRendererFactory(m_nodeRenderer);
 		rf.add(new InGroupPredicate(treeEdges), m_edgeRenderer);
 		m_vis.setRendererFactory(rf);
 
 		// colors
 		ItemAction nodeColor = new NodeColorAction(treeNodes);
 		ItemAction textColor = new ColorAction(treeNodes, VisualItem.TEXTCOLOR, ColorLib.rgb(225, 225, 225));
 		m_vis.putAction("textColor", textColor);
 
 		ItemAction edgeColor = new ColorAction(treeEdges, VisualItem.STROKECOLOR, ColorLib.rgb(232, 232, 232));
 
 		// quick repaint
 		ActionList repaint = new ActionList();
 		repaint.add(nodeColor);
 		
 		repaint.add(new RepaintAction());
 		m_vis.putAction("repaint", repaint);
 
 		// full paint
 		ActionList fullPaint = new ActionList();
 		fullPaint.add(nodeColor);
 		m_vis.putAction("fullPaint", fullPaint);
 
 		// animate paint change
 		ActionList animatePaint = new ActionList(400);
 		animatePaint.add(new ColorAnimator(treeNodes));
 		animatePaint.add(new RepaintAction());
 		m_vis.putAction("animatePaint", animatePaint);
 
 		// create the tree layout action
 		NodeLinkTreeLayout treeLayout = new NodeLinkTreeLayout(tree, m_orientation, 50, 0, 8);
 		treeLayout.setLayoutAnchor(new Point2D.Double(25, 300));
 		m_vis.putAction("treeLayout", treeLayout);
 
 		CollapsedSubtreeLayout subLayout = new CollapsedSubtreeLayout(tree, m_orientation);
 		m_vis.putAction("subLayout", subLayout);
 
 		AutoPanAction autoPan = new AutoPanAction();
 
 		// create the filtering and layout
 		ActionList filter = new ActionList();
 		filter.add(new FisheyeTreeFilter(tree, 2));
 		filter.add(new FontAction(treeNodes, FontLib.getFont("Tahoma", 16)));
 		filter.add(treeLayout);
 		filter.add(subLayout);
 		filter.add(textColor);
 		filter.add(nodeColor);
 		filter.add(edgeColor);
 		m_vis.putAction("filter", filter);
 
 		// animated transition
 		ActionList animate = new ActionList(1000);
 		animate.setPacingFunction(new SlowInSlowOutPacer());
 		animate.add(autoPan);
 		animate.add(new QualityControlAnimator());
 		animate.add(new VisibilityAnimator(tree));
 		animate.add(new LocationAnimator(treeNodes));
 		animate.add(new ColorAnimator(treeNodes));
 		animate.add(new RepaintAction());
 		m_vis.putAction("animate", animate);
 		m_vis.alwaysRunAfter("filter", "animate");
 
 		// create animator for orientation changes
 		ActionList orient = new ActionList(2000);
 		orient.setPacingFunction(new SlowInSlowOutPacer());
 		orient.add(autoPan);
 		orient.add(new QualityControlAnimator());
 		orient.add(new LocationAnimator(treeNodes));
 		orient.add(new RepaintAction());
 		m_vis.putAction("orient", orient);
 
 		// ------------------------------------------------
 
 		// initialize the display
 		setSize(700, 600);
 		setItemSorter(new TreeDepthItemSorter());
 		// addControlListener(new ZoomToFitControl());
 		// addControlListener(new ZoomControl());
 		addControlListener(new WheelZoomControl());
 		addControlListener(new PanControl());
 		addControlListener(new FocusControl(1, "filter"));
 		addControlListener(new TreeViewControl());
 		addControlListener(new StudentViewControl());
 		registerKeyboardAction(new OrientAction(Constants.ORIENT_LEFT_RIGHT), "left-to-right", KeyStroke.getKeyStroke("ctrl 1"),
 				WHEN_FOCUSED);
 		registerKeyboardAction(new OrientAction(Constants.ORIENT_TOP_BOTTOM), "top-to-bottom", KeyStroke.getKeyStroke("ctrl 2"),
 				WHEN_FOCUSED);
 		registerKeyboardAction(new OrientAction(Constants.ORIENT_RIGHT_LEFT), "right-to-left", KeyStroke.getKeyStroke("ctrl 3"),
 				WHEN_FOCUSED);
 		registerKeyboardAction(new OrientAction(Constants.ORIENT_BOTTOM_TOP), "bottom-to-top", KeyStroke.getKeyStroke("ctrl 4"),
 				WHEN_FOCUSED);
 
 		// ------------------------------------------------
 
 		// filter graph and perform layout
 		setOrientation(m_orientation);
 		m_vis.run("filter");
 
 		TupleSet search = new PrefixSearchTupleSet();
 		m_vis.addFocusGroup(Visualization.SEARCH_ITEMS, search);
 		search.addTupleSetListener(new TupleSetListener() {
 			public void tupleSetChanged(TupleSet t, Tuple[] add, Tuple[] rem) {
 				m_vis.cancel("animatePaint");
 				m_vis.run("fullPaint");
 				m_vis.run("animatePaint");
 			}
 		});
 	}
 
 	// ------------------------------------------------------------------------
 
 	public void setOrientation(int orientation) {
 		NodeLinkTreeLayout rtl = (NodeLinkTreeLayout) m_vis.getAction("treeLayout");
 		CollapsedSubtreeLayout stl = (CollapsedSubtreeLayout) m_vis.getAction("subLayout");
 		switch (orientation) {
 		case Constants.ORIENT_LEFT_RIGHT:
 			m_nodeRenderer.setHorizontalAlignment(Constants.LEFT);
 			m_edgeRenderer.setHorizontalAlignment1(Constants.RIGHT);
 			m_edgeRenderer.setHorizontalAlignment2(Constants.LEFT);
 			m_edgeRenderer.setVerticalAlignment1(Constants.CENTER);
 			m_edgeRenderer.setVerticalAlignment2(Constants.CENTER);
 			break;
 		case Constants.ORIENT_RIGHT_LEFT:
 			m_nodeRenderer.setHorizontalAlignment(Constants.RIGHT);
 			m_edgeRenderer.setHorizontalAlignment1(Constants.LEFT);
 			m_edgeRenderer.setHorizontalAlignment2(Constants.RIGHT);
 			m_edgeRenderer.setVerticalAlignment1(Constants.CENTER);
 			m_edgeRenderer.setVerticalAlignment2(Constants.CENTER);
 			break;
 		case Constants.ORIENT_TOP_BOTTOM:
 			m_nodeRenderer.setHorizontalAlignment(Constants.CENTER);
 			m_edgeRenderer.setHorizontalAlignment1(Constants.CENTER);
 			m_edgeRenderer.setHorizontalAlignment2(Constants.CENTER);
 			m_edgeRenderer.setVerticalAlignment1(Constants.BOTTOM);
 			m_edgeRenderer.setVerticalAlignment2(Constants.TOP);
 			break;
 		case Constants.ORIENT_BOTTOM_TOP:
 			m_nodeRenderer.setHorizontalAlignment(Constants.CENTER);
 			m_edgeRenderer.setHorizontalAlignment1(Constants.CENTER);
 			m_edgeRenderer.setHorizontalAlignment2(Constants.CENTER);
 			m_edgeRenderer.setVerticalAlignment1(Constants.TOP);
 			m_edgeRenderer.setVerticalAlignment2(Constants.BOTTOM);
 			break;
 		default:
 			throw new IllegalArgumentException("Unrecognized orientation value: " + orientation);
 		}
 		m_orientation = orientation;
 		rtl.setOrientation(orientation);
 		stl.setOrientation(orientation);
 	}
 
 	public int getOrientation() {
 		return m_orientation;
 	}
 
 	// ------------------------------------------------------------------------
 
 	public static void createEntityTypeFrm(String label, Project project) {
 		myProject = project;
 		JFrame frame = new JFrame();
 
 		JComponent treeview = createPanelEntityTypeTreeView(label, myProject.getHeadEntityType(), frame);
 
 		frame.setContentPane(treeview);
 		frame.setSize(850,600);
 
 		// Get the size of the screen
 		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 		// Determine the new location of the window
 		int w = frame.getSize().width;
 		int h = frame.getSize().height;
 		int x = (dim.width - w) / 2;
 		int y = (dim.height - h) / 2;
 		// Move the window
 		frame.setLocation(x, y);
 		frame.setVisible(true);
 		
 		frame.setTitle("Structure Module");
 		
 		Image icon = Toolkit.getDefaultToolkit().getImage("icons/Tree.png");
 		frame.setIconImage(icon);
 		
 		newNode = new FrmNewNode(myTree, myProject, frame, tview);
 
 	}
 
 	public static void createStudentFrm(String label, SuperEntity treeHead, Project project) {
 
 		myProject = project;
 		JFrame frame = new JFrame();
 
 		JComponent treeview = createPanelTreeView(label, treeHead);
 
 		frame.setContentPane(treeview);
 		frame.setSize(850,600);
 
 		// Get the size of the screen
 		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 		// Determine the new location of the window
 		int w = frame.getSize().width;
 		int h = frame.getSize().height;
 		int x = (dim.width - w) / 2;
 		int y = (dim.height - h) / 2;
 		// Move the window
 		frame.setLocation(x, y);
 		frame.setVisible(true);
 		frame.setResizable(false);
 		
 		frame.setTitle("View Student");
 		
 		Image icon = Toolkit.getDefaultToolkit().getImage("icons/Tree.png");
 		frame.setIconImage(icon);
 		frame.setVisible(true);
 	}
 
 	public static JComponent createPanelEntityTypeTreeView(final String label, EntityType th, JFrame parentFrame) {
 		Color BACKGROUND = new Color(0x171717);
 		Color FOREGROUND = Color.white;
 		
 		final JFrame parentF = parentFrame;
 
 		myProject.getTreeLinkedList().clear();
 
 		String str = "<tree>" + "<declarations>" + "<attributeDecl name=\"name\" type=\"String\" />" + "</declarations>";
 
 		str += th.createTreeFromHead(myProject.getTreeLinkedList());
 
 		str += "</tree>";
 
 		try {
 			// Create file
 			FileWriter fstream = new FileWriter("out.xml");
 			BufferedWriter out = new BufferedWriter(fstream);
 			out.write(str);
 			// Close the output stream
 			out.close();
 		} catch (Exception e) {// Catch exception if any
 			System.err.println("Error: " + e.getMessage());
 		}
 
 		Tree t = null;
 		try {
 			t = (Tree) new TreeMLReader().readGraph("out.xml");
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 
 		File f1 = new File("out.xml");
 		boolean success = f1.delete();
 		if (!success) {
 			System.exit(0);
 		}
 
 		// create a new treemap
 		tview = new TreeView(t, label);
 		PopUpMenu p = new PopUpMenu();
 		p.setTreeView(tview, myTree, myProject, parentFrame);
 		tview.setBackground(BACKGROUND);
 		tview.setForeground(FOREGROUND);
 
 		title.setPreferredSize(new Dimension(200, 25));
 		title.setVerticalAlignment(SwingConstants.BOTTOM);
 		title.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
 		title.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 16));
 		title.setBackground(BACKGROUND);
 		title.setForeground(FOREGROUND);
 
 		lblParent.setVerticalAlignment(SwingConstants.TOP);
 		lblParent.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
 		lblParent.setFont(FontLib.getFont("Tahoma", Font.BOLD, 16));
 		lblParent.setBackground(BACKGROUND);
 		lblParent.setForeground(FOREGROUND);
 
 		lblSelectedParent.setPreferredSize(new Dimension(200, 20));
 		lblSelectedParent.setVerticalAlignment(SwingConstants.TOP);
 		lblSelectedParent.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
 		lblSelectedParent.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 16));
 		lblSelectedParent.setBackground(BACKGROUND);
 		lblSelectedParent.setForeground(FOREGROUND);
 
 		lblChild.setVerticalAlignment(SwingConstants.TOP);
 		lblChild.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
 		lblChild.setFont(FontLib.getFont("Tahoma", Font.BOLD, 16));
 		lblChild.setBackground(BACKGROUND);
 		lblChild.setForeground(FOREGROUND);
 
 		lblSelectedChild.setPreferredSize(new Dimension(200, 20));
 		lblSelectedChild.setVerticalAlignment(SwingConstants.TOP);
 		lblSelectedChild.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
 		lblSelectedChild.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 16));
 		lblSelectedChild.setBackground(BACKGROUND);
 		lblSelectedChild.setForeground(FOREGROUND);
 
 		Box topBox = new Box(BoxLayout.X_AXIS);
 		topBox.add(Box.createHorizontalStrut(10));
 		topBox.add(lblParent);
 		topBox.add(lblSelectedParent);
 		topBox.add(lblChild);
 		topBox.add(lblSelectedChild);
 		topBox.add(Box.createHorizontalGlue());
 		topBox.add(Box.createHorizontalStrut(3));
 		topBox.setBackground(BACKGROUND);
 
 		btnHelp.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				FrmTreeHelpDialog helper = new FrmTreeHelpDialog(parentF);
 				helper.showFrmTreeHelpDialog();
 			}
 		});
 		btnNew.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				newNode.showFrmNewNode(iParent);
 			}
 		});
 
 		Box box = new Box(BoxLayout.X_AXIS);
 		box.add(Box.createHorizontalStrut(10));
 		box.add(title);
 		box.add(Box.createHorizontalGlue());
 		box.add(btnNew);
 		box.add(btnHelp);
 		box.add(Box.createHorizontalStrut(3));
 		box.setBackground(BACKGROUND);
 
 		JPanel panel = new JPanel(new BorderLayout());
 		panel.setBackground(BACKGROUND);
 		panel.setForeground(FOREGROUND);
 		panel.add(topBox, BorderLayout.NORTH);
 		panel.add(tview, BorderLayout.CENTER);
 		panel.add(box, BorderLayout.SOUTH);
 
 		return panel;
 	}
 
 	public static JComponent createPanelTreeView(final String label, SuperEntity th) {
 		Color BACKGROUND = new Color(0x171717);
 		Color FOREGROUND = Color.white;
 
 		myProject.getStudentLinkedList().clear();
 
 		String str = "<tree>" + "<declarations>" + "<attributeDecl name=\"name\" type=\"String\" />" + "</declarations>";
 
 		str += th.createTreeFromHead(myProject.getStudentLinkedList());
 
 		str += "</tree>";
 
 		try {
 			// Create file
 			FileWriter fstream = new FileWriter("out.xml");
 			BufferedWriter out = new BufferedWriter(fstream);
 			out.write(str);
 			// Close the output stream
 			out.close();
 		} catch (Exception e) {// Catch exception if any
 			System.err.println("Error: " + e.getMessage());
 		}
 
 		Tree t = null;
 		try {
 			t = (Tree) new TreeMLReader().readGraph("out.xml");
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 
 		File f1 = new File("out.xml");
 		boolean success = f1.delete();
 		if (!success) {
 			System.exit(0);
 		}
 
 		// create a new treemap
 		tview = new TreeView(t, label);
 
 		tview.setBackground(BACKGROUND);
 		tview.setForeground(FOREGROUND);
 
 		title.setVerticalAlignment(SwingConstants.BOTTOM);
 		title.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
 		title.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 16));
 		title.setBackground(BACKGROUND);
 		title.setForeground(FOREGROUND);
 
 		btnChange.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 
 				if (myProject.getStudentLinkedList().get(selectedEntity).getDetails().getType().getIsTextField()) {
					myProject.getAudit().updateStudent(myProject.getStudentLinkedList().get(0).getValue(),
 							myProject.getStudentLinkedList().get(selectedEntity).getValue(), txtChange.getText(), false);
 					myProject.getStudentLinkedList().get(selectedEntity).setValue(txtChange.getText());
 					String nodeName = myTree.getNode(selectedEntity).getString("name");
 					nodeName =  nodeName.substring(0, nodeName.indexOf(":")+2) + txtChange.getText();
 					myTree.getNode(selectedEntity).set("name", nodeName);
 				} else {
 					try {
 						if (Double.parseDouble(txtChange.getText()) >= 0
 								&& myProject.getStudentLinkedList().get(selectedEntity).getType().getMaxValue() >= Double
 										.parseDouble(txtChange.getText())) {
							myProject.getAudit().updateStudent(myProject.getStudentLinkedList().get(0).getValue(),
 									Double.toString(myProject.getStudentLinkedList().get(selectedEntity).getMark()), txtChange.getText(),
 									true);
 							myProject.getStudentLinkedList().get(selectedEntity).setMark(Double.parseDouble(txtChange.getText()));
 							String nodeName = myTree.getNode(selectedEntity).getString("name");
 							nodeName =  nodeName.substring(0, nodeName.indexOf(":")+2) + txtChange.getText();
 							myTree.getNode(selectedEntity).set("name", nodeName);
 						}
 					} catch (Exception ex) {
 					}
 				}
 				myProject.updateTables();
 				txtChange.setText("");
 				txtChange.setPreferredSize(new Dimension(txtChange.getWidth(), txtChange.getHeight()));
 				txtChange.setLineWrap(true);
 				txtChange.setVisible(false);
 				btnChange.setVisible(false);
 				selectedEntity = -1;
 
 			}
 		});
 
 		// btnChange.registerKeyboardAction(btnChange.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,
 		// 0, false)), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0,false),
 		// txtChange.WHEN_FOCUSED);
 
 		txtChange.addKeyListener(new KeyListener() {
 
 			@Override
 			public void keyPressed(KeyEvent key) {
 				int id = key.getKeyCode();
 				if (id == KeyEvent.VK_ENTER) {
 
 					if (myProject.getStudentLinkedList().get(selectedEntity).getDetails().getType().getIsTextField()) {
						myProject.getAudit().updateStudent(myProject.getStudentLinkedList().get(0).getValue(),
 								myProject.getStudentLinkedList().get(selectedEntity).getValue(), txtChange.getText(), false);
 						myProject.getStudentLinkedList().get(selectedEntity).setValue(txtChange.getText());
 						String nodeName = myTree.getNode(selectedEntity).getString("name");
 						nodeName =  nodeName.substring(0, nodeName.indexOf(":")+2) + txtChange.getText();
 						myTree.getNode(selectedEntity).set("name", nodeName);
 					} else {
 						try {
 							if (Double.parseDouble(txtChange.getText()) >= 0
 									&& myProject.getStudentLinkedList().get(selectedEntity).getType().getMaxValue() >= Double
 											.parseDouble(txtChange.getText())) {
								myProject.getAudit().updateStudent(myProject.getStudentLinkedList().get(0).getValue(),
 										Double.toString(myProject.getStudentLinkedList().get(selectedEntity).getMark()), txtChange.getText(),
 										true);
 								myProject.getStudentLinkedList().get(selectedEntity).setMark(Double.parseDouble(txtChange.getText()));
 								String nodeName = myTree.getNode(selectedEntity).getString("name");
 								nodeName =  nodeName.substring(0, nodeName.indexOf(":")+2) + txtChange.getText();
 								myTree.getNode(selectedEntity).set("name", nodeName);
 							}
 						} catch (Exception ex) {
 						}
 					}
 					myProject.updateTables();
 					txtChange.setText("");
 					txtChange.setPreferredSize(new Dimension(txtChange.getWidth(), txtChange.getHeight()));
 					txtChange.setLineWrap(true);
 					txtChange.setVisible(false);
 					btnChange.setVisible(false);
 					selectedEntity = -1;
 				}
 
 			}
 
 			@Override
 			public void keyReleased(KeyEvent arg0) {
 
 			}
 
 			@Override
 			public void keyTyped(KeyEvent arg0) {
 			}
 		});
 
 		Box box = new Box(BoxLayout.X_AXIS);
 		box.add(Box.createHorizontalStrut(10));
 		box.add(title);
 		box.add(txtChange);
 		box.add(btnChange);
 		btnChange.setVisible(false);
 		txtChange.setVisible(false);
 
 		box.add(Box.createHorizontalGlue());
 		// box.add(search);
 		box.add(Box.createHorizontalStrut(3));
 		box.setBackground(BACKGROUND);
 
 		JPanel panel = new JPanel(new BorderLayout());
 		panel.setBackground(BACKGROUND);
 		panel.setForeground(FOREGROUND);
 		panel.add(tview, BorderLayout.CENTER);
 		panel.add(box, BorderLayout.SOUTH);
 
 		return panel;
 	}
 
 	// ------------------------------------------------------------------------
 
 	public class OrientAction extends AbstractAction {
 		private int orientation;
 
 		public OrientAction(int orientation) {
 			this.orientation = orientation;
 		}
 
 		public void actionPerformed(ActionEvent evt) {
 			setOrientation(orientation);
 			getVisualization().cancel("orient");
 			getVisualization().run("treeLayout");
 			getVisualization().run("orient");
 		}
 	}
 
 	public class AutoPanAction extends Action {
 		private Point2D m_start = new Point2D.Double();
 		private Point2D m_end = new Point2D.Double();
 		private Point2D m_cur = new Point2D.Double();
 		private int m_bias = 150;
 
 		public void run(double frac) {
 			TupleSet ts = m_vis.getFocusGroup(Visualization.FOCUS_ITEMS);
 			if (ts.getTupleCount() == 0)
 				return;
 
 			if (frac == 0.0) {
 				int xbias = 0, ybias = 0;
 				switch (m_orientation) {
 				case Constants.ORIENT_LEFT_RIGHT:
 					xbias = m_bias;
 					break;
 				case Constants.ORIENT_RIGHT_LEFT:
 					xbias = -m_bias;
 					break;
 				case Constants.ORIENT_TOP_BOTTOM:
 					ybias = m_bias;
 					break;
 				case Constants.ORIENT_BOTTOM_TOP:
 					ybias = -m_bias;
 					break;
 				}
 
 				VisualItem vi = (VisualItem) ts.tuples().next();
 				m_cur.setLocation(getWidth() / 2, getHeight() / 2);
 				getAbsoluteCoordinate(m_cur, m_start);
 				m_end.setLocation(vi.getX() + xbias, vi.getY() + ybias);
 			} else {
 				m_cur.setLocation(m_start.getX() + frac * (m_end.getX() - m_start.getX()),
 						m_start.getY() + frac * (m_end.getY() - m_start.getY()));
 				panToAbs(m_cur);
 			}
 		}
 	}
 
 	public static class NodeColorAction extends ColorAction {
 
 		public NodeColorAction(String group) {
 			super(group, VisualItem.FILLCOLOR);
 		}
 
 		public int getColor(VisualItem item) {
 			if (m_vis.isInGroup(item, Visualization.SEARCH_ITEMS))
 				return ColorLib.rgb(255, 222, 190);
 			else if (m_vis.isInGroup(item, Visualization.FOCUS_ITEMS))
 				return ColorLib.rgb(226, 117, 0);
 			else if (item.getDOI() > -1)
 				return ColorLib.rgb(203, 105, 0);
 			else
 				return ColorLib.rgba(0, 0, 0, 0);
 		}
 
 	} // end of inner class TreeMapColorAction
 
 	public class TreeViewControl extends ControlAdapter {
 		private VisualItem activeItem;
 		private Point2D down = new Point2D.Double();
 		private Point2D tmp = new Point2D.Double();
 		private boolean wasFixed, dragged;
 		private boolean repaint = false;
 
 		public void itemEntered(VisualItem item, MouseEvent e) {
 			activeItem = item;
 			wasFixed = item.isFixed();
 
 			String id = item.getClass().getName();
 			if (id.contains("Node"))
 				title.setText(" " + item.getString("name"));
 		}
 
 		public void itemExited(VisualItem item, MouseEvent e) {
 			if (activeItem == item) {
 				activeItem = null;
 				item.setFixed(wasFixed);
 			}
 			title.setText("");
 		}
 
 		public void itemReleased(VisualItem item, MouseEvent e) {
 			if (!SwingUtilities.isLeftMouseButton(e))
 				return;
 			if (dragged) {
 				activeItem = null;
 				item.setFixed(wasFixed);
 				dragged = false;
 			}
 			// clear the focus
 			Visualization vis = item.getVisualization();
 			vis.getFocusGroup(Visualization.FOCUS_ITEMS).clear();
 			vis.cancel("forces");
 		}
 
 		public void itemPressed(VisualItem item, MouseEvent e) {
 
 			Visualization vis = item.getVisualization();
 			vis.getFocusGroup(Visualization.FOCUS_ITEMS).setTuple(item);
 			item.setFixed(true);
 			dragged = false;
 			Display d = (Display) e.getComponent();
 			down = d.getAbsoluteCoordinate(e.getPoint(), down);
 			// vis.run("forces");
 
 			Table edgeTable = null;
 			Table nodeTable = null;
 			String name = null;
 
 			if (SwingUtilities.isLeftMouseButton(e)) {
 
 				String id = item.getClass().getName();
 
 				if (e.isShiftDown() && e.getClickCount() == 1) {
 
 					if (id.contains("Node")) {
 						nodeTable = myTree.getNodeTable();
 						name = item.getString("name");
 
 						iParent = item.getRow();
 						bParent = true;
 						lblSelectedParent.setText(name);
 					}// if
 				}// if
 
 				if (e.isControlDown() && e.getClickCount() == 1) {
 					if (bParent) {
 						if (id.contains("Node")) {
 							nodeTable = myTree.getNodeTable();
 							name = item.getString("name");
 
 							iChild = item.getRow();
 							bChild = true;
 							if (iChild == iParent) {
 								lblSelectedChild.setText("can't select same parent");
 								bChild = false;
 								iChild = -1;
 							} else
 								lblSelectedChild.setText(name);
 
 							edgeTable = myTree.getEdgeTable();
 
 							// set(row,col)
 
 							if (bChild) {
 								for (int r = 0; r < edgeTable.getRowCount(); r++) {
 									if ((edgeTable.get(r, 1).equals(iChild))) {
 										edgeTable.set(r, 0, iParent);
 										myProject.getTreeLinkedList().get(iChild).changeParent(myProject.getTreeLinkedList().get(iParent));
 										myProject.getAudit().moveNode(myProject.getTreeLinkedList().get(iChild).getName(),
 												myProject.getTreeLinkedList().get(iParent).getName());
 										bChild = false;
 										iChild = -1;
 										lblSelectedChild.setText("please select child");
 										break;
 									}// if
 								}// for
 							}// if
 
 						}// if
 
 					} else {
 						lblSelectedParent.setText("no parent selected");
 						bChild = false;
 						iChild = -1;
 						lblSelectedChild.setText("no child selcted");
 					}// else
 				}// if
 			}// if
 			else if (SwingUtilities.isRightMouseButton(e)) {
 
 				String id = item.getClass().getName();
 
 				if (e.isShiftDown() && e.getClickCount() == 1) {
 					if (id.contains("Node")) {
 						iParent = -1;
 						bParent = false;
 						lblSelectedParent.setText("no parent selected");
 						iChild = -1;
 						bChild = false;
 						lblSelectedChild.setText("no child selected");
 					}// if
 				}// if
 
 				if (e.isControlDown() && e.getClickCount() == 1) {
 					iChild = -1;
 					bChild = false;
 					lblSelectedChild.setText("no child selected");
 				}// if
 			}// else
 
 		}// itemPressed
 
 		public void itemDragged(VisualItem item, MouseEvent e) {
 			if (!SwingUtilities.isLeftMouseButton(e))
 				return;
 			dragged = true;
 			Display d = (Display) e.getComponent();
 			tmp = d.getAbsoluteCoordinate(e.getPoint(), tmp);
 			double dx = tmp.getX() - down.getX();
 			double dy = tmp.getY() - down.getY();
 
 			PrefuseLib.setX(item, null, item.getX() + dx);
 			PrefuseLib.setY(item, null, item.getY() + dy);
 			down.setLocation(tmp);
 			item.getVisualization().repaint();
 		}// itemDragged
 	}
 
 	public class StudentViewControl extends ControlAdapter {
 		private VisualItem activeItem;
 		private Point2D down = new Point2D.Double();
 		private Point2D tmp = new Point2D.Double();
 		private boolean wasFixed, dragged;
 		private boolean repaint = false;
 
 		public void itemEntered(VisualItem item, MouseEvent e) {
 			activeItem = item;
 			wasFixed = item.isFixed();
 
 			String id = item.getClass().getName();
 			if (id.contains("Node"))
 				title.setText(" " + item.getString("name"));
 		}
 
 		public void itemExited(VisualItem item, MouseEvent e) {
 			if (activeItem == item) {
 				activeItem = null;
 				item.setFixed(wasFixed);
 			}
 			title.setText("");
 		}
 
 		public void itemReleased(VisualItem item, MouseEvent e) {
 			if (!SwingUtilities.isLeftMouseButton(e))
 				return;
 			if (dragged) {
 				activeItem = null;
 				item.setFixed(wasFixed);
 				dragged = false;
 			}
 			// clear the focus
 			Visualization vis = item.getVisualization();
 			vis.getFocusGroup(Visualization.FOCUS_ITEMS).clear();
 			vis.cancel("forces");
 		}
 
 		public void itemPressed(VisualItem item, MouseEvent e) {
 
 			Visualization vis = item.getVisualization();
 			vis.getFocusGroup(Visualization.FOCUS_ITEMS).setTuple(item);
 			item.setFixed(true);
 			dragged = false;
 			Display d = (Display) e.getComponent();
 			down = d.getAbsoluteCoordinate(e.getPoint(), down);
 
 			if (SwingUtilities.isLeftMouseButton(e)) {
 				if (item.canGetString("name")) {
 					String id = item.getString("name");
 					if (e.getClickCount() == 2) {
 						selectedEntity = item.getRow();
 						txtChange.setVisible(true);
 						btnChange.setVisible(true);
 						txtChange.setText(id.substring(id.indexOf(":") + 2));
 						txtChange.requestFocus(true);
 						txtChange.selectAll();
 					}
 				}
 			}
 		}// itemPressed
 
 		public void itemDragged(VisualItem item, MouseEvent e) {
 		}// itemDragged
 	}
 
 } // end of class TreeMap
