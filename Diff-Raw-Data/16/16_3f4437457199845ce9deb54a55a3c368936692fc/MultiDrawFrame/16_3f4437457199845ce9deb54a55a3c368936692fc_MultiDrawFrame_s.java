 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.File;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.DefaultListModel;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JLayeredPane;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.KeyStroke;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingConstants;
 import javax.swing.border.EmptyBorder;
 
 import network.SendBuffer;
 import utils.Protocol;
 
 public class MultiDrawFrame extends JFrame {
 	private static final int RIGHT_PANEL_WIDTH = 200;
 	private static final int RIGHT_PANEL_HEIGHT = 800;
 	private static final int USERS_LIST_HEIGHT = 200;
 	private static final int CHATWINDOW_HEIGHT = 510;
 	private static final int LEFT_PANEL_HEIGHT = 800;
 	private static final int LEFT_PANEL_WIDTH = 80;
 	private static final int LEFT_ICON_BUTTONS_HEIGHT = 150;
 
 	private SendBuffer buffer;
 	private PaintPanel paintpanel;
 	private JTextPane chatWindow;
 	private JList<String> connectedUsersList;
 
 	/**
 	 * Blabla
 	 */
 	public MultiDrawFrame(SendBuffer buffer) {
 		this.buffer = buffer;
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setTitle("AOJA POWER ARTIST CANVAS EDITOR PRO");
 		this.setPreferredSize(new Dimension(1200, 850));
 		this.setMinimumSize(new Dimension(500, 500));
 		this.setResizable(true);
 
 		JPanel centerPanel = makeCenterPanel();
 		JPanel leftPanel = makeLeftPanel();
 		JPanel rightPanel = makeRightPanel();
 
 		JMenuBar menuBar = makeMenuBar();
 
 		this.setLayout(new BorderLayout());
 		this.setJMenuBar(menuBar);
 		this.add(leftPanel, BorderLayout.WEST);
 		this.add(centerPanel, BorderLayout.CENTER);
 		this.add(rightPanel, BorderLayout.EAST);
 
 		this.pack();
 		this.setVisible(true);
 	}
 
 	private JMenuBar makeMenuBar() {
 		JMenuBar menuBar = new JMenuBar();
 		JMenu fileMenu = new JMenu("File");
 		JMenuItem importPic = new JMenuItem("Import image...", KeyEvent.VK_T);
 		importPic.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.ALT_MASK));
 		importPic.getAccessibleContext().setAccessibleDescription("Import image...");
 		importPic.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JFileChooser fc = new JFileChooser();
 				int returnVal = fc.showOpenDialog(null);
 
 				if (returnVal == JFileChooser.APPROVE_OPTION) {
 					File file = fc.getSelectedFile();
 					paintpanel.sendFileForInsertingtoSever(file);
 				}
 			}
 		});
 		fileMenu.add(importPic);
 
 		JMenu editMenu = new JMenu("Edit");
 		JMenu aboutMenu = new JMenu("About");
 		menuBar.add(fileMenu);
 		menuBar.add(editMenu);
 		menuBar.add(aboutMenu);
 		return menuBar;
 	}
 
 	private JPanel makeCenterPanel() {
 		paintpanel = new PaintPanel(buffer);
 		JScrollPane scroller = new JScrollPane(paintpanel);
 		scroller.setPreferredSize(new Dimension(400, 400));
 		scroller.setBorder(BorderFactory.createEmptyBorder());
 
 		JPanel centerPanel = new JPanel();
 		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
 		centerPanel.add(scroller);
 		return centerPanel;
 	}
 
 	private JPanel makeRightPanel() {
 		JPanel rightPanel = new JPanel();
 		rightPanel.setPreferredSize(new Dimension(RIGHT_PANEL_WIDTH, RIGHT_PANEL_HEIGHT));
 		rightPanel.setBackground(new Color(230, 230, 230));
 		rightPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(200, 200, 200)));
 		rightPanel.setLayout(new BorderLayout());
 
 		JLabel users = new JLabel("Users");
 		users.setBorder(new EmptyBorder(2, 0, 3, 0));
 		users.setForeground(new Color(60, 60, 60));
 		users.setHorizontalAlignment(SwingConstants.CENTER);
 
 		JLabel chat = new JLabel("Chat");
 		chat.setBorder(new EmptyBorder(6, 0, 3, 0));
 		chat.setForeground(new Color(60, 60, 60));
 		chat.setHorizontalAlignment(SwingConstants.CENTER);
 
 		DefaultListModel<String> listModel = new DefaultListModel<String>();
 
 		connectedUsersList = new JList<String>(listModel);
 		connectedUsersList.setPreferredSize(new Dimension(RIGHT_PANEL_WIDTH - 8, USERS_LIST_HEIGHT));
 		connectedUsersList.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
 		connectedUsersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		connectedUsersList.setSelectedIndex(0);
 		connectedUsersList.setVisibleRowCount(5);
 
 		chatWindow = new JTextPane();
 		chatWindow.setPreferredSize(new Dimension(RIGHT_PANEL_WIDTH - 8, CHATWINDOW_HEIGHT));
 		chatWindow.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
 		chatWindow.setEditable(false);
 
 		JTextField chatInput = new JTextField();
 		chatInput.setPreferredSize(new Dimension(RIGHT_PANEL_WIDTH - 8, 25));
 		chatInput.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
 
 		JPanel rightTopPanel = new JPanel();
 		rightTopPanel.setLayout(new BorderLayout());
 		rightTopPanel.setBorder(new EmptyBorder(4, 4, 0, 4));
 		rightTopPanel.add(users, BorderLayout.NORTH);
 		rightTopPanel.add(connectedUsersList, BorderLayout.CENTER);
 		rightTopPanel.add(chat, BorderLayout.SOUTH);
 		
 		JPanel rightCenterPanel = new JPanel();
 		rightCenterPanel.setLayout(new BorderLayout());
 		rightCenterPanel.setBorder(new EmptyBorder(0, 4, 2, 4));
 		rightCenterPanel.add(chatWindow, BorderLayout.CENTER);
 		
 		JPanel rightBottomPanel = new JPanel();
 		rightBottomPanel.add(chatInput);
 		
 		rightPanel.add(rightTopPanel, BorderLayout.NORTH);
 		rightPanel.add(rightCenterPanel, BorderLayout.CENTER);
 		rightPanel.add(rightBottomPanel, BorderLayout.SOUTH);
 
 		chatInput.addKeyListener(new KeyListener() {
 			public void keyPressed(KeyEvent e) {
 				int keyCode = e.getKeyCode();
 				if (keyCode == KeyEvent.VK_ENTER) {
 					JTextField field = (JTextField) e.getSource();
 					buffer.put(Protocol.CHAT_MESSAGE + " " + field.getText());
 					field.setText("");
 				}
 			}
 
 			public void keyReleased(KeyEvent arg0) { /* IGNORED */ }
 
 			public void keyTyped(KeyEvent arg0) { /* IGNORED */ }
 		});
 
 		return rightPanel;
 	}
 
 	private JPanel makeLeftPanel() {
 		JPanel leftPanel = new JPanel();
 		leftPanel.setPreferredSize(new Dimension(LEFT_PANEL_WIDTH, LEFT_PANEL_HEIGHT));
 		leftPanel.setBackground(new Color(230, 230, 230));
 		leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 200, 200)));
 
 		// Tools label
 		JLabel toolsLabel = new JLabel("Tools");
 
 		// Brush size slider
 		BrushSizeSlider brushSizeSlider = new BrushSizeSlider(paintpanel);
 		leftPanel.add(brushSizeSlider);
 
 		// Tool palette
 		IconButtons iconButtons = new IconButtons(paintpanel);
 		iconButtons.setPreferredSize(new Dimension(LEFT_PANEL_WIDTH, LEFT_ICON_BUTTONS_HEIGHT));
 
 		// Color button
 		JLayeredPane layeredPane = new JLayeredPane();
         layeredPane.setPreferredSize(new Dimension(LEFT_PANEL_WIDTH, LEFT_PANEL_WIDTH));
         
 		JLabel colorLabel = new JLabel("Color");
 		colorLabel.setForeground(new Color(60, 60, 60));
 		
		ColorButton colorButton1 = new ColorButton("", paintpanel);
 		colorButton1.setBackground(Color.BLACK);
 		colorButton1.setBounds(12, 0, 40, 40);
 		
		ColorButton colorButton2 = new ColorButton("", paintpanel);
 		colorButton2.setBackground(Color.WHITE);
 		colorButton2.setBounds(27, 15, 40, 40);
 		
 		layeredPane.add(colorButton1, new Integer(2));
 		layeredPane.add(colorButton2, new Integer(1));
 
 		leftPanel.add(toolsLabel);
 		leftPanel.add(iconButtons);
 		leftPanel.add(colorLabel);
 		leftPanel.add(layeredPane);
 		return leftPanel;
 	}
 
 	public PaintPanel getPaintPanel() {
 		return paintpanel;
 	}
 
 	public JTextPane getChatPanel() {
 		return chatWindow;
 	}
 
 	public void updateUsersList(String[] words) {
 		DefaultListModel<String> model = (DefaultListModel<String>) connectedUsersList.getModel();
 		model.clear();
 		for (int i = 1; i < words.length; i++) {
 			model.addElement(words[i]);
 		}
 	}
 }
