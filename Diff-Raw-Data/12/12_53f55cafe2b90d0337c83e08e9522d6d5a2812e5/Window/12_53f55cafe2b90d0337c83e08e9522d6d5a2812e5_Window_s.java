 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package view;
 
 import controller.Controller;
 import controller.RuleParameter;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.FlowLayout;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Locale;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JSeparator;
 import javax.swing.JSlider;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.JTree;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.border.Border;
 import javax.swing.border.EtchedBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 import model.gameoflife.ErrorIO;
 import model.gameoflife.Pattern;
 import model.image.ImageManager;
 
 public final class Window extends JFrame implements ActionListener, ChangeListener, WindowListener, MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener, KeyListener, FocusListener, Observer {
 
 	private RuleParameter _currentRuleParameter;
 	private JButton _btn_Pause;
 	private JButton _btn_Play;
 	private JButton _btn_Next;
 	private JButton _btn_RandomlyFill;
 	private JButton _btn_Empty;
 	/*private JButton _btn_Save;
 	private JButton _btn_Load;*/
 	private JButton _btn_RuleParameter;
 	private JComboBox _cbb_Speed;
 	//private JComboBox	_cbb_Rule;
 	private JTextField _txt_Column;
 	private JTextField _txt_Row;
 	private JSlider _sli_Column;
 	private JSlider _sli_Row;
 	private JLabel _lbl_Rule;
 	private JLabel _lbl_Search;
 	private JMenuBar _mnu_Bar;
 	private JMenu _mnu_File;
 	private JMenu _mnu_Edit;
 	private JMenu _mnu_Simulators;
 	private JMenu _mnu_View;
 	private JMenuItem _itm_Save;
 	private JMenuItem _itm_SavePattern;
 	private JMenuItem _itm_Open;
 	private JMenuItem _itm_Play;
 	private JMenuItem _itm_Next;
 	private JMenuItem _itm_Random;
 	private JMenuItem _itm_Empty;
 	private JMenuItem _itm_Speed_p;
 	private JMenuItem _itm_Speed_m;
 	private JMenuItem _itm_ThreadNumber;
 	//private JMenuItem _itm_size;
 	private JMenuItem _itm_Parameters;
 	private JMenuItem _itm_Plus;
 	private JMenuItem _itm_Moins;
 	private JMenuItem _itm_MoveUp;
 	private JMenuItem _itm_MoveDown;
 	private JMenuItem _itm_MoveRight;
 	private JMenuItem _itm_MoveLeft;
 	private JMenuItem _itm_RotateLeft;
 	private JMenuItem _itm_RotateRight;
 	private JMenuItem _itm_InvXAxis;
 	private JMenuItem _itm_InvYAxis;
 	private JCheckBoxMenuItem _itm_OptionsVisible;
 	Point _mousePosition;
 	private Field _field;
 	private Controller _controller;
 	private JTabbedPane _panel;
 	private JPanel main;
 	private ArrayList _patternsHexa;
 	private ArrayList _patternsTriangle;
 	private ArrayList _patternsNormal;
 	private ButtonGroup _bg_Patterns;
 	private JPanel _pnl_patterns;
 	private JPanel _pnl_BtnPatternsRotate;
 	private JPanel _pnl_BtnPatternsSymetric;
 	private JButton _btn_RotateLeftPatterns;
 	private JButton _btn_RotateRightPatterns;
 	private JButton _btn_ChangeXAxisPatterns;
 	private JButton _btn_ChangeYAxisPatterns;
 	private JTree _tree_Pattern;
 
 	public Window() {
 		
 		ImageManager manager = ImageManager.getInstance();
 		_controller = new Controller();
 		
 		
 		_field = new Field(8);
 		_field.setBackground(Color.black);
 		_field.addMouseListener(this);
 		_field.addMouseMotionListener(this);
 		_field.addMouseWheelListener(this);
 		_field.addComponentListener(this);
 
 		_mnu_Bar = new JMenuBar();
 		_mnu_File = new JMenu("File");
 		_mnu_File.setMnemonic(KeyEvent.VK_F);
 		_mnu_Edit = new JMenu("Edit");
 		_mnu_Edit.setMnemonic(KeyEvent.VK_E);
 		_mnu_Simulators = new JMenu("Simulator");
 		_mnu_Simulators.setMnemonic(KeyEvent.VK_S);
 		_mnu_View = new JMenu("View");
 		_mnu_View.setMnemonic(KeyEvent.VK_V);
 		_itm_Save = new JMenuItem("Save");
 		_itm_Save.addActionListener(this);
 		_itm_Save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
 		_itm_SavePattern = new JMenuItem("Save pattern");
 		_itm_SavePattern.addActionListener(this);
 		_itm_SavePattern.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK));
 		_itm_Open = new JMenuItem("Open");
 		_itm_Open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
 		_itm_Open.addActionListener(this);
 		_itm_Play = new JMenuItem("Play/Pause");
 		_itm_Play.setAccelerator(KeyStroke.getKeyStroke("SPACE"));
 		_itm_Play.addActionListener(this);
 		_itm_Next = new JMenuItem("Next Step");
 		_itm_Next.setAccelerator(KeyStroke.getKeyStroke("ENTER"));
 		_itm_Next.addActionListener(this);
 		_itm_Random = new JMenuItem("Random");
 		_itm_Random.setAccelerator(KeyStroke.getKeyStroke("R"));
 		_itm_Random.addActionListener(this);
 		_itm_Empty = new JMenuItem("Stop");
 		_itm_Empty.setAccelerator(KeyStroke.getKeyStroke("DELETE"));
 		_itm_Empty.addActionListener(this);
 		_itm_Speed_p = new JMenuItem("Speed +");
 		_itm_Speed_p.setAccelerator(KeyStroke.getKeyStroke("PAGE_UP"));
 		_itm_Speed_p.addActionListener(this);
 		_itm_Speed_m = new JMenuItem("Speed -");
 		_itm_Speed_m.setAccelerator(KeyStroke.getKeyStroke("PAGE_DOWN"));
 		_itm_Speed_m.addActionListener(this);
 		_itm_ThreadNumber = new JMenuItem("Set thread number");
 		_itm_ThreadNumber.setAccelerator(KeyStroke.getKeyStroke("T"));
 		_itm_ThreadNumber.addActionListener(this);
 		/*_itm_size = new JMenuItem("Size");
 		_itm_size.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_MASK));
 		_itm_size.addActionListener(this);*/
 		_itm_Parameters = new JMenuItem("Settings");
 		_itm_Parameters.setAccelerator(KeyStroke.getKeyStroke("S"));
 		_itm_Parameters.addActionListener(this);
 		_itm_Plus = new JMenuItem("Zoom +");
 		_itm_Plus.setAccelerator(KeyStroke.getKeyStroke("PLUS"));
 		_itm_Plus.addActionListener(this);
 		_itm_Moins = new JMenuItem("Zoom -");
 		_itm_Moins.setAccelerator(KeyStroke.getKeyStroke("MINUS"));
 		_itm_Moins.addActionListener(this);
 		_itm_MoveUp = new JMenuItem("Move up");
 		_itm_MoveUp.setAccelerator(KeyStroke.getKeyStroke("UP"));
 		_itm_MoveUp.addActionListener(this);
 		_itm_MoveDown = new JMenuItem("Move down");
 		_itm_MoveDown.setAccelerator(KeyStroke.getKeyStroke("DOWN"));
 		_itm_MoveDown.addActionListener(this);
 		_itm_MoveRight = new JMenuItem("Move right");
 		_itm_MoveRight.setAccelerator(KeyStroke.getKeyStroke("RIGHT"));
 		_itm_MoveRight.addActionListener(this);
 		_itm_MoveLeft = new JMenuItem("Move left");
 		_itm_MoveLeft.setAccelerator(KeyStroke.getKeyStroke("LEFT"));
 		_itm_MoveLeft.addActionListener(this);
 		_itm_RotateRight = new JMenuItem("Rotate right");
 		_itm_RotateRight.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.CTRL_MASK));
 		_itm_RotateRight.addActionListener(this);
 		_itm_RotateLeft = new JMenuItem("Rotate left");
 		_itm_RotateLeft.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.CTRL_MASK));
 		_itm_RotateLeft.addActionListener(this);
 		_itm_InvXAxis = new JMenuItem("Inversion X-axis");
 		_itm_InvXAxis.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, ActionEvent.CTRL_MASK));
 		_itm_InvXAxis.addActionListener(this);
 		_itm_InvYAxis = new JMenuItem("Inversion Y-axis");
 		_itm_InvYAxis.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, ActionEvent.CTRL_MASK));
 		_itm_InvYAxis.addActionListener(this);
 		_itm_OptionsVisible = new JCheckBoxMenuItem("Panel", true);
 		_itm_OptionsVisible.setAccelerator(KeyStroke.getKeyStroke("P"));
 		_itm_OptionsVisible.addActionListener(this);
 
 		_mnu_Bar.add(_mnu_File);
 		_mnu_Bar.add(_mnu_Edit);
 		_mnu_Bar.add(_mnu_Simulators);
 		_mnu_Bar.add(_mnu_View);
 		_mnu_File.add(_itm_Open);
 		_mnu_File.add(_itm_Save);
 		_mnu_File.add(_itm_SavePattern);
 		_mnu_Edit.add(_itm_Random);
 		_mnu_Edit.add(_itm_Empty);
 		_mnu_Edit.add(new JSeparator());
 		_mnu_Edit.add(_itm_RotateRight);
 		_mnu_Edit.add(_itm_RotateLeft);
 		_mnu_Edit.add(_itm_InvXAxis);
 		_mnu_Edit.add(_itm_InvYAxis);
 		_mnu_Edit.add(new JSeparator());
 		_mnu_Edit.add(_itm_Parameters);
 		_mnu_Simulators.add(_itm_Play);
 		_mnu_Simulators.add(_itm_Next);
 		_mnu_Simulators.add(new JSeparator());
 		_mnu_Simulators.add(_itm_Speed_p);
 		_mnu_Simulators.add(_itm_Speed_m);
 		_mnu_Simulators.add(new JSeparator());
 		_mnu_Simulators.add(_itm_ThreadNumber);
 		//_menu_parameters.add(_itm_size);
 		_mnu_View.add(_itm_Plus);
 		_mnu_View.add(_itm_Moins);
 		_mnu_View.add(new JSeparator());
 		_mnu_View.add(_itm_MoveUp);
 		_mnu_View.add(_itm_MoveDown);
 		_mnu_View.add(_itm_MoveRight);
 		_mnu_View.add(_itm_MoveLeft);
 		_mnu_View.add(new JSeparator());
 		_mnu_View.add(_itm_OptionsVisible);
 
 		setJMenuBar(_mnu_Bar);
 
 		_controller.addObserverToGame(_field);
 		_controller.addObserverToGame(this);
 		_controller.empty();
 
 		_currentRuleParameter = _controller.getRules()[0];
 
 		_mousePosition = new Point();
 
 		_btn_Pause = new JButton();
 		_btn_Pause.addActionListener(this);
 		_btn_Pause.addKeyListener(this);
 		_btn_Pause.setIcon(new ImageIcon(manager.get("src/resources/pause.png")));
 		_btn_Pause.setFocusable(false);
 
 		_btn_Play = new JButton();
 		_btn_Play.addActionListener(this);
 		_btn_Play.addKeyListener(this);
 		_btn_Play.setIcon(new ImageIcon(manager.get("src/resources/play.png")));
 		_btn_Play.setFocusable(false);
 
 		_btn_Next = new JButton();
 		_btn_Next.addActionListener(this);
 		_btn_Next.addKeyListener(this);
 		_btn_Next.setIcon(new ImageIcon(manager.get("src/resources/next.png")));
 		_btn_Next.setFocusable(false);
 
 		_btn_RandomlyFill = new JButton();
 		_btn_RandomlyFill.addActionListener(this);
 		_btn_RandomlyFill.addKeyListener(this);
 		_btn_RandomlyFill.setIcon(new ImageIcon(manager.get("src/resources/random.png")));
 		_btn_RandomlyFill.setFocusable(false);
 
 		_btn_Empty = new JButton();
 		_btn_Empty.addActionListener(this);
 		_btn_Empty.addKeyListener(this);
 		_btn_Empty.setIcon(new ImageIcon(manager.get("src/resources/empty.png")));
 		_btn_Empty.setFocusable(false);
 
 		/*_btn_Save = new JButton();
 		_btn_Save.addActionListener(this);
 		_btn_Save.addKeyListener(this);
 		_btn_Save.setIcon(new ImageIcon(manager.get("src/resources/save.png")));
 		
 		_btn_Load = new JButton();
 		_btn_Load.addActionListener(this);
 		_btn_Load.addKeyListener(this);
 		_btn_Load.setIcon(new ImageIcon(manager.get("src/resources/open.png")));*/
 
 
 		_btn_RuleParameter = new JButton();
 		_btn_RuleParameter.addActionListener(this);
 		_btn_RuleParameter.addKeyListener(this);
 		_btn_RuleParameter.setIcon(new ImageIcon(manager.get("src/resources/param.png")));
 		_btn_RuleParameter.setFocusable(false);
 
 		_lbl_Rule = new JLabel();
 		_lbl_Search = new JLabel();
 
 		_sli_Column = new JSlider(JSlider.HORIZONTAL, 1, 999, 50);
 		_sli_Column.addChangeListener(this);
 		_sli_Column.addKeyListener(this);
 		_sli_Column.setFocusable(false);
 
 		_sli_Row = new JSlider(JSlider.HORIZONTAL, 1, 999, 50);
 		_sli_Row.addChangeListener(this);
 		_sli_Row.addKeyListener(this);
 		_sli_Row.setFocusable(false);
 
 		_txt_Column = new JTextField(3);
 		_txt_Column.setText(Integer.toString(_sli_Row.getValue()));
 		_txt_Column.addActionListener(this);
 		_txt_Column.addFocusListener(this);
 
 		_txt_Row = new JTextField(3);
 		_txt_Row.setText(Integer.toString(_sli_Row.getValue()));
 		_txt_Row.addActionListener(this);
 		_txt_Row.addFocusListener(this);
 
 		_cbb_Speed = new JComboBox(this.getSpeeds());
 		_cbb_Speed.setSelectedIndex(1);
 		_cbb_Speed.addActionListener(this);
 		_cbb_Speed.addKeyListener(this);
 		_cbb_Speed.setFocusable(false);
 
 		/*_cbb_Rule = new JComboBox(_controller.getRules());
 		_cbb_Rule.setSelectedIndex(0);
 		_cbb_Rule.addActionListener(this);*/
 
 		_pnl_patterns = new JPanel();
 		_pnl_patterns.setLayout(new BorderLayout());
 		_pnl_BtnPatternsRotate = new JPanel();
 		_pnl_BtnPatternsRotate.setLayout(new BoxLayout(_pnl_BtnPatternsRotate, BoxLayout.X_AXIS));
 		_pnl_BtnPatternsSymetric = new JPanel();
 		_pnl_BtnPatternsSymetric.setLayout(new BoxLayout(_pnl_BtnPatternsSymetric, BoxLayout.X_AXIS));
 		_btn_RotateLeftPatterns = new JButton();
 		_btn_RotateLeftPatterns.setIcon(new ImageIcon(manager.get("src/resources/rotateLeft.png")));
 		_btn_RotateLeftPatterns.addActionListener(this);
 		_btn_RotateLeftPatterns.setFocusable(false);
 		_pnl_BtnPatternsRotate.add(_btn_RotateLeftPatterns);
 		_btn_RotateRightPatterns = new JButton();
 		_btn_RotateRightPatterns.setIcon(new ImageIcon(manager.get("src/resources/rotateRight.png")));
 		_btn_RotateRightPatterns.addActionListener(this);
 		_btn_RotateRightPatterns.setFocusable(false);
 		_pnl_BtnPatternsRotate.add(_btn_RotateRightPatterns);
 		_btn_ChangeXAxisPatterns = new JButton();
 		_btn_ChangeXAxisPatterns.setIcon(new ImageIcon(manager.get("src/resources/symmetryY.png")));
 		_btn_ChangeXAxisPatterns.addActionListener(this);
 		_btn_ChangeXAxisPatterns.setFocusable(false);
 		_pnl_BtnPatternsSymetric.add(_btn_ChangeXAxisPatterns);
 		_btn_ChangeYAxisPatterns = new JButton();
 		_btn_ChangeYAxisPatterns.setIcon(new ImageIcon(manager.get("src/resources/symmetryX.png")));
 		_btn_ChangeYAxisPatterns.addActionListener(this);
 		_btn_ChangeYAxisPatterns.setFocusable(false);
 		_pnl_BtnPatternsSymetric.add(_btn_ChangeYAxisPatterns);
 		_bg_Patterns = new ButtonGroup();
 		this.createPatternsList();
 
 
 		this.setTitle("Conway's game of life");
 		this.setSize(1000, 600);
 		this.setLocationRelativeTo(null);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 
 		JPanel player = new JPanel();
 		player.setLayout(new BoxLayout(player, BoxLayout.LINE_AXIS));
 		player.add(Box.createHorizontalGlue());
 		player.add(_btn_Play);
 		player.add(Box.createHorizontalGlue());
 		//player.add(_btn_Pause);
 		player.add(_btn_Next);
 		player.add(Box.createHorizontalGlue());
 
 		JPanel fieldAction = new JPanel();
 		fieldAction.setLayout(new BoxLayout(fieldAction, BoxLayout.LINE_AXIS));
 		fieldAction.add(Box.createHorizontalGlue());
 		fieldAction.add(_btn_RandomlyFill);
 		fieldAction.add(Box.createHorizontalGlue());
 		fieldAction.add(_btn_Empty);
 		fieldAction.add(Box.createHorizontalGlue());
 
 		/*JPanel xmlAction = new JPanel();
 		xmlAction.setLayout(new BoxLayout(xmlAction, BoxLayout.LINE_AXIS));
 		xmlAction.add(Box.createHorizontalGlue());
 		xmlAction.add(_btn_Save);
 		xmlAction.add(Box.createHorizontalGlue());
 		xmlAction.add(_btn_Load);
 		xmlAction.add(Box.createHorizontalGlue());*/
 
 		JPanel sizeLabel = new JPanel();
 		sizeLabel.setLayout(new FlowLayout());
 		sizeLabel.add(new JLabel("Field size : "));
 		sizeLabel.add(_txt_Column);
 		sizeLabel.add(new JLabel(" x "));
 		sizeLabel.add(_txt_Row);
 
 		JPanel sizeColumn = new JPanel();
 		sizeColumn.setLayout(new FlowLayout());
 		sizeColumn.add(new JLabel("Column : "));
 		sizeColumn.add(_sli_Column);
 
 		JPanel sizeRow = new JPanel();
 		sizeRow.setLayout(new FlowLayout());
 		sizeRow.add(new JLabel("Row : "));
 		sizeRow.add(_sli_Row);
 
 		JPanel speed = new JPanel();
 		speed.setLayout(new FlowLayout());
 		speed.add(new JLabel("Speed : "));
 		speed.add(_cbb_Speed);
 
 		/*JPanel type = new JPanel();
 		type.setLayout(new FlowLayout());
 		type.add(new JLabel("Type : "));
 		type.add(new JComboBox());
 		
 		JPanel search = new JPanel();
 		search.setLayout(new FlowLayout());
 		search.add(new JLabel("Search : "));
 		search.add(new JComboBox());*/
 
 		JPanel ruleBtn = new JPanel();
 		ruleBtn.setLayout(new FlowLayout());
 		ruleBtn.add(_btn_RuleParameter);
 
 		JPanel rule = new JPanel();
 		rule.setLayout(new FlowLayout());
 		rule.add(_lbl_Rule);
 
 		JPanel search = new JPanel();
 		search.setLayout(new FlowLayout());
 		search.add(_lbl_Search);
 
 		this.updateRuleLabel();
 		//rule.add(_cbb_Rule);
 		//rule.add(_btn_ruleParameter);
 
 		JPanel option = new JPanel();
 		option.setLayout(new BoxLayout(option, BoxLayout.PAGE_AXIS));
 		option.add(Box.createVerticalGlue());
 		option.add(player);
 		option.add(Box.createVerticalGlue());
 		option.add(fieldAction);
 		/*option.add(Box.createVerticalGlue());
 		option.add(xmlAction);*/
 		option.add(Box.createVerticalGlue());
 		option.add(new JSeparator());
 		option.add(speed);
 		option.add(new JSeparator());
 		option.add(sizeLabel);
 		option.add(sizeColumn);
 		option.add(sizeRow);
 		option.add(Box.createVerticalGlue());
 		option.add(new JSeparator());
 		/*option.add(type);
 		option.add(search);*/
 		option.add(rule);
 		option.add(search);
 		option.add(ruleBtn);
 		//option.add(Box.createVerticalGlue());
 
 		_panel = new JTabbedPane();
 		_panel.addTab("Option", option);
 		_panel.addTab("Patterns", _pnl_patterns);
 
 
 		main = new JPanel();
 		main.setLayout(new BorderLayout());
 		main.add(_field, BorderLayout.CENTER);
 		main.add(_panel, BorderLayout.WEST);
 		main.setBackground(Color.GRAY);
 
 
 		this.setContentPane(main);
 
 		//game.addObserver(field);
 		this.addKeyListener(this);
 		this.setFocusable(true);
 		this.addWindowListener(this);
 
 	}
 
 	public String[] getSpeeds() {
 		Integer[] int_Speeds = Controller.getSpeeds();
 		String[] str_Speeds = new String[int_Speeds.length];
 
 		for (int i = 0 ; i < str_Speeds.length ; ++i) {
 			str_Speeds[i] = Integer.toString(int_Speeds[i]) + " ms";
 		}
 
 		return str_Speeds;
 	}
 
 	public void createPatternsList() {
 		_pnl_patterns.removeAll();
 		_patternsHexa = _controller.patternList("Hexagone");
 		_patternsTriangle = _controller.patternList("Triangle");
 		_patternsNormal = _controller.patternList("Normal");
 		JPanel _pnl_BtnPattern = new JPanel();
 		_pnl_BtnPattern.setLayout(new BoxLayout(_pnl_BtnPattern, BoxLayout.Y_AXIS));
 		_pnl_BtnPattern.add(_pnl_BtnPatternsRotate);
 		_pnl_BtnPattern.add(_pnl_BtnPatternsSymetric);
 		_pnl_patterns.add(_pnl_BtnPattern, BorderLayout.NORTH);
 		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Patterns");
 		DefaultTreeModel treeModel = new DefaultTreeModel(root);
 		_tree_Pattern = new JTree(treeModel);
 		_tree_Pattern.setFocusable(false);
 		_tree_Pattern.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
 		DefaultMutableTreeNode base = new DefaultMutableTreeNode("None");
 		DefaultMutableTreeNode normal = new DefaultMutableTreeNode("Normal mode");
 		DefaultMutableTreeNode hexa = new DefaultMutableTreeNode("Hexagone mode");
 		DefaultMutableTreeNode triangle = new DefaultMutableTreeNode("Triangle mode");
 		for (int i = 0 ; i < _patternsHexa.size() ; i++) {
 			hexa.add(new DefaultMutableTreeNode(_patternsHexa.get(i).toString()));
 		}
 		for (int i = 0 ; i < _patternsTriangle.size() ; i++) {
 			triangle.add(new DefaultMutableTreeNode(_patternsTriangle.get(i).toString()));
 		}
 		for (int i = 0 ; i < _patternsNormal.size() ; i++) {
 			normal.add(new DefaultMutableTreeNode(_patternsNormal.get(i).toString()));
 		}
 		root.add(base);
 		root.add(normal);
 		root.add(hexa);
 		root.add(triangle);
 		for (int i = 0; i < _tree_Pattern.getRowCount(); i++) {
 			 _tree_Pattern.expandRow(i);
 		}
 		_pnl_patterns.add(_tree_Pattern, BorderLayout.CENTER);
 		_tree_Pattern.addMouseListener(this);
 		
 		this.revalidate();
 		this.repaint();
 	}
 
 	public void updateBtnPlay() {
 		if (_controller.isPlayed()) {
 			_btn_Play.setIcon(new ImageIcon(ImageManager.getInstance().get("src/resources/pause.png")));
 		}
 		else {
 			_btn_Play.setIcon(new ImageIcon(ImageManager.getInstance().get("src/resources/play.png")));
 		}
 	}
 
 	public void updateRuleLabel() {
 
 		String textRule = "Rule : ";
 
 		if (!_currentRuleParameter.getName().equals(_currentRuleParameter.getScientificName())) {
 			textRule += _currentRuleParameter.getName() + " - ";
 		}
 
 		textRule += _currentRuleParameter.getScientificName();
 
 		_lbl_Rule.setText(textRule);
 
 		String textSearch = "Search : " + _currentRuleParameter.getSearch();
 
 		if (_currentRuleParameter.isTorus()) {
 			textSearch += " - Torus";
 		}
 
 		_lbl_Search.setText(textSearch);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == _btn_Pause) {
 			_controller.pause();
 		}
 		else if (e.getSource() == _itm_MoveUp) {
 			_field.moveField(new Point(0, -10));
 		}
 		else if (e.getSource() == _itm_MoveDown) {
 			_field.moveField(new Point(0, 10));
 		}
 		else if (e.getSource() == _itm_MoveRight) {
 			_field.moveField(new Point(10, 0));
 		}
 		else if (e.getSource() == _itm_MoveLeft) {
 			_field.moveField(new Point(-10, 0));
 		}
 		else if (e.getSource() == _btn_Play || e.getSource() == _itm_Play) {
 			if (_controller.isPlayed()) {
 				_controller.pause();
 			}
 			else {
 				_controller.play();
 			}
 		}
 		else if (e.getSource() == _itm_OptionsVisible) {
 			_panel.setVisible(!_panel.isVisible());
 
 			this.revalidate();
 			this.repaint();
 		}
 		else if (e.getSource() == _itm_Speed_p) {
 			if (_cbb_Speed.getSelectedIndex() > 0) {
 				_cbb_Speed.setSelectedIndex(_cbb_Speed.getSelectedIndex() - 1);
 				_controller.setSpeed(Controller.getSpeeds()[_cbb_Speed.getSelectedIndex()]);
 			}
 		}
 		else if (e.getSource() == _itm_Speed_m) {
 			if (_cbb_Speed.getSelectedIndex() < _cbb_Speed.getItemCount() - 1) {
 				_cbb_Speed.setSelectedIndex(_cbb_Speed.getSelectedIndex() + 1);
 				_controller.setSpeed(Controller.getSpeeds()[_cbb_Speed.getSelectedIndex()]);
 			}
 		}
 		else if (e.getSource() == _itm_ThreadNumber) {
 			
 			String number = (String)JOptionPane.showInputDialog(
 											this,
 											"Set thread number:",
 											"Thread number",
 											JOptionPane.QUESTION_MESSAGE,
 											null,
 											null,
 											_controller.getThreadNumber()
 					);
 
 			
 			try {
 				if(number != null) {
 					int n = Integer.parseInt(number);
 
 					_controller.setThreadNumber(n);
 				}
 			}
 			catch(NumberFormatException exc) {
 				JOptionPane.showMessageDialog(this, "'" + number + "' is not a number", "Error", JOptionPane.ERROR_MESSAGE);
 			}
 			
 		}
 		else if (/*e.getSource() == _btn_Save || */e.getSource() == _itm_Save) {
 			boolean test = false;
 			JFileChooser fc = new JFileChooser();
 			fc.setSelectedFile(new File("Cellule.cells"));
 			do {
 				if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
 					String path = fc.getSelectedFile().getPath();
 					File f = new File(path);
 					if (!f.exists()) {
 						_controller.save(path);
 					}
 					else if (JOptionPane.showConfirmDialog(this, "This file already exists, overwrite it?", "Confirm overwriting", JOptionPane.OK_CANCEL_OPTION) == 0) {
 						_controller.save(path);
 					}
 					else {
 						test = true;
 					}
 				}
 				else {
 					test = false;
 				}
 			}
 			while (test);
 			
 			createPatternsList();
 			_field.setPattern(null);
 			_controller.setPattern(null);
 		}
 		else if (e.getSource() == _itm_SavePattern) {
 			String name = (String)JOptionPane.showInputDialog(
 											this,
 											"Pattern name:",
 											"Save pattern",
 											JOptionPane.QUESTION_MESSAGE,
 											null,
 											null,
 											"Custom"
 					);
 			
 			String path = "src/resources/Normal/" + name + ".cells";
 			File f = new File(path);
 			System.out.println(f.getAbsoluteFile());
 			if (!f.exists()) {
 				_controller.save(path);
 			}
 			else if (JOptionPane.showConfirmDialog(this, "This file already exists, overwrite it?", "Confirm overwriting", JOptionPane.OK_CANCEL_OPTION) == 0) {
 				_controller.save(path);
 			}
 			
 			this.createPatternsList();
 		}
 		else if (/*e.getSource() == _btn_Load || */e.getSource() == _itm_Open) {
 			JFileChooser fc = new JFileChooser();
 			boolean test;
 			do {
 				test = false;
 				if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
 					if (fc.getSelectedFile().getAbsolutePath().endsWith(".cells")) {
 						_controller.load(fc.getSelectedFile().getAbsolutePath());
 					}
 					else {
 						JOptionPane.showMessageDialog(this, "You must select a file .cells");
 						test = true;
 					}
 				}
 			}
 			while (test);
 		}
 		else if (e.getSource() == _btn_Next || e.getSource() == _itm_Next) {
 			_controller.next();
 		}
 		else if (e.getSource() == _btn_RandomlyFill || e.getSource() == _itm_Random) {
 			_controller.randomlyFill();
 		}
 		else if (e.getSource() == _btn_Empty || e.getSource() == _itm_Empty) {
 			_controller.empty();
 		}
 		else if (e.getSource() == _txt_Column) {
 
 			try {
 				int val = Integer.parseInt(_txt_Column.getText());
 				_sli_Column.setValue(val);
 			}
 			catch (NumberFormatException ex) {
 				Logger.getLogger(Window.class.getName()).log(Level.INFO, null, ex);
 				_txt_Column.setText(Integer.toString(_sli_Column.getValue()));
 			}
 
 		}
 		else if (e.getSource() == _txt_Row) {
 
 			try {
 				int val = Integer.parseInt(_txt_Row.getText());
 				_sli_Row.setValue(val);
 			}
 			catch (NumberFormatException ex) {
 				Logger.getLogger(Window.class.getName()).log(Level.INFO, null, ex);
 				_txt_Row.setText(Integer.toString(_sli_Row.getValue()));
 			}
 
 		}
 		else if (e.getSource() == _cbb_Speed) {
 			_controller.setSpeed(Controller.getSpeeds()[_cbb_Speed.getSelectedIndex()]);
 		} /*else if(e.getSource() == _cbb_Rule) {
 		try {
 		_controller.setRule(_controller.getRulesName()[_cbb_Rule.getSelectedIndex()]);
 		} catch (BadRuleNameException ex) {
 		Logger.getLogger(Window.class.getName()).log(Level.INFO, null, ex);
 		}
 		}*/ 
 		else if (e.getSource() == _btn_RuleParameter || e.getSource() == _itm_Parameters) {
 			RuleParameterDialog dialog = new RuleParameterDialog(this, _controller, _currentRuleParameter);
 			RuleParameter rp = dialog.showDialog();
 
 			if (rp != null) {
 				_currentRuleParameter = rp;
 				_controller.setRule(_currentRuleParameter);
 				this.updateRuleLabel();
 				_field.setNeighbors(_controller.getNeighborMaximumNumber(rp.getSearch()),_currentRuleParameter.isTorus());
 			}
 		}
 		else if (e.getSource() == _itm_Plus) {
 			int unit = -1;
 
 			_field.zoom(unit);
 		}
 		else if (e.getSource() == _itm_Moins) {
 			int unit = 1;
 
 			_field.zoom(unit);
 		}
 		else if (e.getSource() == _btn_ChangeYAxisPatterns || e.getSource() == _itm_InvYAxis){
 			_field.verticalSymmetry();
 			this.repaint();
 		}
 		else if (e.getSource() == _btn_ChangeXAxisPatterns || e.getSource() == _itm_InvXAxis){
 			_field.horizontalSymmetry();
 			this.repaint();
 		}
 		else if (e.getSource() == _btn_RotateLeftPatterns || e.getSource() == _itm_RotateLeft){
 			_field.rotateLeft();
 			this.repaint();
 		}
 		else if (e.getSource() == _btn_RotateRightPatterns || e.getSource() == _itm_RotateRight){
 			_field.rotateRight();
 			this.repaint();
 		}
 
 		this.updateBtnPlay();
 	}
 
 	@Override
 	public void windowClosing(WindowEvent we) {
 		if (we.getSource() == this) {
 			_controller.stop();
 //			_field.terminate();
 		}
 	}
 
 	@Override
 	public void windowOpened(WindowEvent we) {
 		//throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 	}
 
 	@Override
 	public void windowClosed(WindowEvent we) {
 		//throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 	}
 
 	@Override
 	public void windowIconified(WindowEvent we) {
 		//throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 	}
 
 	@Override
 	public void windowDeiconified(WindowEvent we) {
 		//throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 	}
 
 	@Override
 	public void windowActivated(WindowEvent we) {
 		//throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 	}
 
 	@Override
 	public void windowDeactivated(WindowEvent we) {
 		//throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent me) {
 		TreePath tp = _tree_Pattern.getPathForLocation(me.getX(), me.getY());
 		Pattern p = null;
 		if (tp != null){
 			boolean test = true;
 			int i = 0;
 			while(test && i < _patternsNormal.size()){
 				if(tp.getLastPathComponent().toString().equals(_patternsNormal.get(i).toString())){
 					test = false;
 					p = new Pattern();
 					p.addObserver(this);
 					p.loadPattern("Normal/"+_patternsNormal.get(i).toString());
 				}
 				i++;
 			}
 			i = 0;
 			while(test && i < _patternsTriangle.size()){
 				if(tp.getLastPathComponent().toString().equals(_patternsTriangle.get(i).toString())){
 					test = false;
 					p = new Pattern();
 					p.addObserver(this);
 					p.loadPattern("Triangle/"+_patternsTriangle.get(i).toString());
 				}
 				i++;
 			}
 			i = 0;
 			while(test && i < _patternsHexa.size()){
 				if(tp.getLastPathComponent().toString().equals(_patternsHexa.get(i).toString())){
 					test = false;
 					p = new Pattern();
 					p.addObserver(this);
 					p.loadPattern("Hexagone/"+_patternsHexa.get(i).toString());
 				}
 				i++;
 			}
			_field.setPattern(p);
			_controller.setPattern(p);
			this.repaint();
 		}
 	}
 
 	@Override
 	public void mousePressed(MouseEvent me) {
 
 		if (me.getModifiers() == MouseEvent.BUTTON1_MASK) {
 			Point indicator = _field.getIndicator();
 
 			if (indicator != null) {
 				_controller.toggleCell(indicator);
 			}
 		}
 		else if (me.getModifiers() == MouseEvent.BUTTON3_MASK) {
 			_mousePosition = me.getPoint();
 		}
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent me) {
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent me) {
 		//throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 	}
 
 	@Override
 	public void mouseExited(MouseEvent me) {
 		//throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent me) {
 
 		if (me.getModifiers() == MouseEvent.BUTTON1_MASK) {
 
 			Point oldIndicator = _field.getIndicator();
 
 			if (oldIndicator != null) {
 				oldIndicator = (Point) oldIndicator.clone();
 			}
 
 			mouseMoved(me);
 
 			Point indicator = _field.getIndicator();
 
 			if (indicator != null && !indicator.equals(oldIndicator)) {
 				_controller.toggleCell(indicator);
 			}
 		}
 		else if (me.getModifiers() == MouseEvent.BUTTON3_MASK) {
 
 			Point diff = me.getPoint();
 			diff.x -= _mousePosition.x;
 			diff.y -= _mousePosition.y;
 
 			_mousePosition = me.getPoint();
 
 			_field.moveField(diff);
 		}
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent me) {
 
 		Point position = me.getPoint();
 
 		_field.setIndicatorPosition(position);
 		if(!_field.isInsideTheField(position)){
 			this.repaint();
 		}
 	}
 
 	@Override
 	public void mouseWheelMoved(MouseWheelEvent mwe) {
 		int unit = mwe.getWheelRotation();
 		_field.zoom(unit);
 	}
 
 	@Override
 	public void componentResized(ComponentEvent ce) {
 
 		if (ce.getSource() == _field) {
 			_field.resize();
 			_field.repaint();
 		}
 
 	}
 
 	@Override
 	public void componentMoved(ComponentEvent ce) {
 //		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 	}
 
 	@Override
 	public void componentShown(ComponentEvent ce) {
 //		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 	}
 
 	@Override
 	public void componentHidden(ComponentEvent ce) {
 //		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 	}
 
 	@Override
 	public void stateChanged(ChangeEvent ce) {
 		if (ce.getSource() == _sli_Column) {
 			_txt_Column.setText(Integer.toString(_sli_Column.getValue()));
 			_controller.setFieldSize(
 					new Point(
 					_sli_Column.getValue(),
 					_sli_Row.getValue()));
 		}
 		else if (ce.getSource() == _sli_Row) {
 			_txt_Row.setText(Integer.toString(_sli_Row.getValue()));
 			_controller.setFieldSize(
 					new Point(
 					_sli_Column.getValue(),
 					_sli_Row.getValue()));
 		}
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {}
 
 	@Override
 	public void focusGained(FocusEvent e) {}
 
 	@Override
 	public void focusLost(FocusEvent e) {
 		if (e.getSource() == _txt_Column) {
 
 			try {
 				int val = Integer.parseInt(_txt_Column.getText());
 				_sli_Column.setValue(val);
 			}
 			catch (NumberFormatException ex) {
 				Logger.getLogger(Window.class.getName()).log(Level.INFO, null, ex);
 				_txt_Column.setText(Integer.toString(_sli_Column.getValue()));
 			}
 
 		}
 		else if (e.getSource() == _txt_Row) {
 
 			try {
 				int val = Integer.parseInt(_txt_Row.getText());
 				_sli_Row.setValue(val);
 			}
 			catch (NumberFormatException ex) {
 				//Logger.getLogger(Window.class.getName()).log(Level.INFO, null, ex);
 				_txt_Row.setText(Integer.toString(_sli_Row.getValue()));
 			}
 
 		}
 	}
 
 	@Override
 	public void update(Observable o, Object arg) {
 		if(arg instanceof ErrorIO){
 			ErrorIO err = (ErrorIO) arg;
 			JOptionPane.showMessageDialog(this, err.getErrorText());
 		}
 	}
 	
 	
 }
