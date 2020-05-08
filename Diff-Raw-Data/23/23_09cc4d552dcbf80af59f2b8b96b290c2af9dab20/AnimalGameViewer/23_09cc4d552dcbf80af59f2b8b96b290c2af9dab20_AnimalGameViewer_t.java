 import javax.swing.*;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 public class AnimalGameViewer extends JFrame {
 
 	public static String URL = "http://www.cs.duke.edu/courses/cps100/spring10/assign/twentyq/logs.txt";
 
 	protected JTextArea myOutput;
 	protected IAnimalModel myModel;
 	protected String myTitle;
 	protected JTextField myMessage;
 	protected ArrayList<JComponent> myClickables;
 	protected JPanel myCenterPanel;
 	protected JTree myTree;
 	protected JEditorPane myPane;
 
 	protected static JFileChooser ourChooser = new JFileChooser(System
 			.getProperties().getProperty("user.dir"));
 
 	public AnimalGameViewer(String title) {
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 
 		myClickables = new ArrayList<JComponent>();
 
 		JPanel panel = (JPanel) getContentPane();
 		panel.setLayout(new BorderLayout());
 		setTitle(title);
 		myTitle = title;
 
 		panel.add(makeInput(), BorderLayout.NORTH);
 		// panel.add(makeOutput(), BorderLayout.CENTER);
 		// panel.add(null,BorderLayout.CENTER);
 		addTree(null);
 		panel.add(makeMessage(), BorderLayout.SOUTH);
 		makeMenus();
 		setEnabled(false);
 		pack();
 		setVisible(true);
 	}
 
 	public void setModel(IAnimalModel model) {
 		myModel = model;
 		myModel.setView(this);
 	}
 
 	public void reTree() {
 		myCenterPanel.revalidate();
 		myCenterPanel.repaint();
 	}
 
 	public void addTree(DefaultMutableTreeNode root) {
 		myTree = new JTree(root);
 		JScrollPane tview = new JScrollPane(myTree);
 		JPanel p = new JPanel(new BorderLayout());
 		myPane = new JEditorPane();
 		myPane.setEditable(false);
 		JScrollPane lower = new JScrollPane(myPane);
 		lower.setBorder(BorderFactory.createTitledBorder("game info"));
 		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
 		split.setTopComponent(tview);
 		split.setBottomComponent(lower);
 		split.setDividerLocation(200);
 		p.add(split, BorderLayout.CENTER);
 		JPanel main = (JPanel) getContentPane();
 		if (myCenterPanel != null) {
 			main.remove(myCenterPanel);
 		}
 
 		myTree.addTreeSelectionListener(new TreeSelectionListener() {
 
 			public void valueChanged(TreeSelectionEvent e) {
 				DefaultMutableTreeNode node = (DefaultMutableTreeNode) myTree
 						.getLastSelectedPathComponent();
 				if (node != null) {
 					// processWebSelection(node);
 					showMessage(node.toString());
 				}
 			}
 
 		});
 		myTree.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent evt) {
 				if (evt.getClickCount() == 2) {
 					TreePath path = myTree.getPathForLocation(evt.getX(),
 							evt.getY());
 					if (path != null) {
 						DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
 								.getLastPathComponent();
 						if (node != null) {
 							processWebSelection(node);
 						}
 					}
 				}
 			}
 		});
 
 		myCenterPanel = p;
 		main.add(p, BorderLayout.CENTER);
 		reTree();
 	}
 
 	protected void processWebSelection(DefaultMutableTreeNode node) {
 		if (node == null)
 			return;
 
 		Object nodeInfo = node.getUserObject();
 		if (node.isLeaf()) {
 			WebGameInfo game = (WebGameInfo) nodeInfo;
 			URL url = null;
 			try {
 				url = new URL(game.getURL());
 				myModel.initialize(new Scanner(url.openStream()));
 			} catch (MalformedURLException e) {
 				showError("bad url " + game);
 			} catch (IOException e) {
 				showError("error reading " + url);
 			}
 		}
 	}
 
 	protected JPanel makeMessage() {
 		JPanel p = new JPanel(new BorderLayout());
 		myMessage = new JTextField(30);
 		p.setBorder(BorderFactory.createTitledBorder("message"));
 		p.add(myMessage, BorderLayout.CENTER);
 		return p;
 	}
 
 	protected JPanel makeInput() {
 		JPanel p = new JPanel(new BorderLayout());
 		JButton yes = new JButton("YES");
 		p.add(yes, BorderLayout.WEST);
 		yes.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				myModel.processYesNo(true);
 			}
 		});
 		JButton no = new JButton("NO");
 		p.add(no, BorderLayout.EAST);
 		no.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				myModel.processYesNo(false);
 			}
 		});
 		myClickables.add(yes);
 		myClickables.add(no);
 		return p;
 	}
 
 	protected JPanel makeOutput() {
 		JPanel p = new JPanel(new BorderLayout());
 		myOutput = new JTextArea(20, 40);
 		p.setBorder(BorderFactory.createTitledBorder("game info"));
 		p.add(new JScrollPane(myOutput), BorderLayout.CENTER);
 		return p;
 
 	}
 
 	protected JMenu makeFileMenu() {
 		JMenu fileMenu = new JMenu("File");
 
 		fileMenu.add(new AbstractAction("Open File") {
 			public void actionPerformed(ActionEvent ev) {
 				int retval = ourChooser.showOpenDialog(null);
 				if (retval == JFileChooser.APPROVE_OPTION) {
 					File file = null;
 					try {
 						file = ourChooser.getSelectedFile();
 						myModel.initialize(new Scanner(file));
 					} catch (FileNotFoundException e) {
 						AnimalGameViewer.this.showError("could not open "
 								+ file.getName());
 					}
 				}
 			}
 		});
 
 		fileMenu.add(new AbstractAction("Open URL") {
 			public void actionPerformed(ActionEvent ev) {
 				String urlName = JOptionPane.showInputDialog(null, "Enter URL", URL);
 				if (urlName != null) {
 
 					URL url;
 					try {
 						url = new URL(urlName);
 						doTree(new Scanner(url.openStream()));
 					} catch (MalformedURLException e) {
 						showError("bad url " + urlName);
 					} catch (IOException e) {
 						showError("trouble reading url " + urlName);
 					}
 				}
 			}
 
 		});
 
 		fileMenu.add(new AbstractAction("Save") {
 			public void actionPerformed(ActionEvent ev) {
 				doSave();
 			}
 		});
 
 		fileMenu.add(new AbstractAction("New game") {
 			public void actionPerformed(ActionEvent ev) {
 				doNewGame();
 			}
 		});
 		
 		JMenuItem newGameItem = fileMenu.getItem(fileMenu
 				.getMenuComponentCount() - 1);
 		myClickables.add(newGameItem);
 
 		fileMenu.add(new AbstractAction("Quit") {
 			public void actionPerformed(ActionEvent ev) {
 				System.exit(0);
 			}
 		});
 		return fileMenu;
 	}
 
 	private void doTree(Scanner scanner) {
 		DefaultMutableTreeNode root = new DefaultMutableTreeNode("games");
 		HashMap<String, DefaultMutableTreeNode> map = 
 				new HashMap<String, DefaultMutableTreeNode>();
 		while (scanner.hasNextLine()) {
 			String[] line = scanner.nextLine().split(",");
 			String type = line[1];
 			String url = line[3];
 			String login = line[0];
 			String file = url.substring(url.lastIndexOf("/") + 1);
 			if (!map.containsKey(type)) {
 				map.put(type, new DefaultMutableTreeNode(type));
 			}
 			map.get(type).add(
 					new DefaultMutableTreeNode(new WebGameInfo(login + " "
 							+ file, url)));
 		}
 		for (DefaultMutableTreeNode sub : map.values()) {
 			root.add(sub);
 		}
 		addTree(root);
 	}
 
 	public void setEnabled(boolean value) {
 		for (JComponent comp : myClickables) {
 			comp.setEnabled(value);
 		}
 	}
 
 	protected void makeMenus() {
 		JMenuBar bar = new JMenuBar();
 		bar.add(makeFileMenu());
 		setJMenuBar(bar);
 	}
 
 	protected void doNewGame() {
 		myModel.newGame();
 	}
 
 	private void doSave() {
 		int result = ourChooser.showSaveDialog(this);
 		if (result == JFileChooser.APPROVE_OPTION) {
 			File file = ourChooser.getSelectedFile();
 			try {
 				FileWriter fw = new FileWriter(file);
 				myModel.write(fw);
 				fw.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void update(Collection elements) {
 		StringBuffer sb = new StringBuffer();
 
 		// myOutput.setText("");
 		for (Object o : elements) {
 			// myOutput.append(o+"\n");
 			sb.append(o.toString() + "\n");
 		}
 		myPane.setText(sb.toString());
 	}
 
 	public void update(String s) {
 		myPane.setText(s);
 	}
 
 	public void showMessage(String s) {
 		myMessage.setText(s);
 	}
 
 	public void showDialog(String s) {
 		JOptionPane.showMessageDialog(this, s, "Game Update",
 				JOptionPane.INFORMATION_MESSAGE);
 	}
 
 	public void showError(String s) {
 		JOptionPane.showMessageDialog(this, s, "Game Update",
 				JOptionPane.INFORMATION_MESSAGE);
 	}
 
 	public void getNewInfoLeaf() {
 		String s = "Please read the main window.\n"
 				+ "You may need to move this dialog so you can\n"
 				+ "read the question in the window.\n\n"
 				+ "Type your answer to that question below.";
 		String response = JOptionPane.showInputDialog(
 				SwingUtilities.getWindowAncestor(this), s,
 				"New Information Needed", JOptionPane.QUESTION_MESSAGE);
 		if (response != null) {
			myModel.addNewKnowledge(response);
 		} else {
 			doNewGame();
 		}
 	}
 
 	public void getDifferentiator() {
 		String s = "Please read the main window.\n"
 				+ "You may need to move this dialog so you can\n"
 				+ "read the question in the window.\n\n"
 				+ "Type your answer to that question below.";
 		String response = JOptionPane.showInputDialog(this, s,
 				"New Question Needed", JOptionPane.QUESTION_MESSAGE);
 		if (response != null) {
			myModel.addNewQuestion(response);
 		} else {
 			doNewGame();
 		}
 	}
 }
