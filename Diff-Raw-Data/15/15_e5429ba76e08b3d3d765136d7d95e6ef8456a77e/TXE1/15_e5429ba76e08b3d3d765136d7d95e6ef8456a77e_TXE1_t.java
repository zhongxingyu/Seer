 import java.awt.*;
 import java.awt.event.*;
 import java.awt.Window;
 import java.io.*;
 
 import javax.swing.*;
 import javax.swing.text.*;
 import javax.swing.text.Highlighter.Highlight;
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.swing.text.DefaultEditorKit;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Vector;
 
 import javax.swing.*;
 import javax.swing.text.*;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 
 import java.awt.print.*;
 
 import javax.swing.AbstractAction;
 import javax.swing.AbstractButton;
 import javax.swing.Action;
 import javax.swing.ActionMap;
 import javax.swing.JFrame;
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.UIManager.LookAndFeelInfo;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.UndoableEditEvent;
 import javax.swing.event.UndoableEditListener;
 import javax.swing.text.DefaultStyledDocument;
 import javax.swing.text.StyleContext;
 import javax.swing.undo.CannotRedoException;
 import javax.swing.undo.UndoManager;
 
 import java.awt.AWTException;
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Rectangle;
 import java.awt.Robot;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.TreeModelEvent;
 import javax.swing.event.TreeModelListener;
 
 import java.awt.FlowLayout;
 
 import javax.swing.JButton;
 import javax.swing.JTextField;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 import javax.swing.JPasswordField;
 
 import java.awt.Color;
 import java.awt.Font;
 
 import javax.swing.BoxLayout;
 
 import java.awt.CardLayout;
 
 import javax.swing.JFileChooser;
 import javax.swing.JToolBar;
 import javax.swing.SwingConstants;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.SpringLayout;
 import javax.swing.JMenuBar;
 
 import java.awt.Toolkit;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.image.BufferedImage;
 
 import say.swing.JFontChooser;
 
 /**
  * 
  * @author ericzhu
  * 
  */
 @SuppressWarnings("unused")
 public class TXE1 extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 
 	public JTextArea TXEAREA = new JTextArea();
 
 	public JTextArea saveField = new JTextArea();
 
 	private JFileChooser dialog = new JFileChooser(
 			System.getProperty("home.dir"));
 
 	private String currentFile = "Untitled Document";
 
 	private boolean changed = false;
 
 	private Color color = (Color.WHITE);
 
 	public String changeLog = ("TXE 1.6.5 change log 1. New color buttons in the easy access bar  2.New about option 3.Colors are added  4.Menu Seperators are added  5.The scroll bars  auto hide  6. Added the Settings Tab 7. Minor bug fixes ");
 
 	public String DefualtText = ("Welcome To TXE. The  new innovative Text Editor. Type what ever you want. Updates coming soon! \r\n\r\n*Note* this is Beta Version 1.7.1\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\nCredits: Eric Zhu of Great Ark Studios and Turk4n of CodeCall.net Icons from http://www.visualpharm.com/");
 
 	public String currentText = TXEAREA.getText();
 
 	public String saveText = saveField.getText();
 
 	public JFrame panel;
 
 	public JTextField findText = new JTextField();
 
 	public JScrollPane scroll = new JScrollPane(TXEAREA,
 			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
 			JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 	FileSystemModel fsm = new FileSystemModel();
 
 	JTree tree = new JTree(fsm);
 
 	JScrollPane scrollTree = new JScrollPane(tree);
 
 	// DO NOT CHANGE
 	JSplitPane splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
 			new JScrollPane(tree), scroll);
 
 	UndoManager undoManager = null;
 
 	DefaultStyledDocument document;
 
 	StyleContext styleContext;
 
 	JMenuItem undo = new JMenuItem("Undo");
 	JMenuItem redo = new JMenuItem("Redo");
 
 	public TXE1() {
 
 		undoManager = new UndoManager();
 
 		styleContext = new StyleContext();
 
 		document = new DefaultStyledDocument(styleContext);
 
 		this.setSize(1000, 1000);
 		TXEAREA.setText(DefualtText);
 		this.setLocationRelativeTo(null);
 		TXEAREA.setFont(new Font("Times New Roman", Font.PLAIN, 12));
 
 		findText.setToolTipText("Type word to be found");
 		findText.setSize(50, 200);
 
 		this.getContentPane().add(splitpane);
 
 		splitpane.setResizeWeight(0.5);
 		splitpane.setOneTouchExpandable(true);
 		Dimension minimumSize = new Dimension(0, 0);
 		TXEAREA.setMinimumSize(minimumSize);
 		tree.setMinimumSize(minimumSize);
 		tree.setVisible(true);
 		tree.setSize(100, 1000);
 
 		JMenuItem About = new JMenuItem("About");
 		JMenu format = new JMenu("Format");
 		JMenu TXESettings = new JMenu("Tools");
 		JMenuItem settingsFrame = new JMenuItem("Settings");
 		JMenuItem addwebsite = new JMenuItem("Add Website Signature");
 		JMenuItem addname = new JMenuItem("Add Name Signature");
 		JMenuItem addcompany = new JMenuItem("Add Company Name");
 		JMenuItem bold = new JMenuItem("Bold Document");
 		JMenuItem italics = new JMenuItem("Italicize Document");
 		JMenuItem plain = new JMenuItem("Normal Style");
 		JMenuItem bI = new JMenuItem("Bold and Italicize Document");
 		JMenuItem Fr = new JMenuItem("Font");
 		Fr.setToolTipText("Choose a font");
 		JMenuItem bL = new JMenuItem("Blue");
 		bL.setToolTipText("Make text blue");
 		JMenuItem rD = new JMenuItem("Red");
 		rD.setToolTipText("Make text red");
 		JMenuItem gR = new JMenuItem("Green");
 		gR.setToolTipText("Make text green");
 		JMenuItem bLa = new JMenuItem("Normal");
 		bLa.setToolTipText("Make text black");
 		JMenuItem rsA = new JMenuItem("Text Align Right");
 		rsA.setToolTipText("Align text right");
 		JMenuItem lsA = new JMenuItem("Text Align Left");
 		lsA.setToolTipText("Align text left");
 		JMenuItem csA = new JMenuItem("Text Align Center");
 		csA.setToolTipText("Align text middle");
 		JMenuItem cL = new JMenuItem("Change Log");
 		JMenuItem caL = new JMenuItem("Calculator");
 		caL.setToolTipText("Do some math");
 		JMenuItem enC = new JMenuItem("Encryption");
 		enC.setToolTipText("Encrypt a message");
 		JMenuItem coL = new JMenuItem("Color Chooser");
 		coL.setToolTipText("Choose a color");
 		JMenuItem srenSht = new JMenuItem("Screenshot");
 		srenSht.setToolTipText("Take Screenshot");
 		JMenuItem nimbus = new JMenuItem("Nimbus Style");
 		JMenuItem metal = new JMenuItem("Metal Style");
 		JMenuItem natives = new JMenuItem("Native Style");
 		JMenuItem print = new JMenuItem("Print");
 		print.setToolTipText("Print current document");
 		JMenuItem date = new JMenuItem("Insert Date and Time");
 		JMenuItem sA = new JMenuItem("Select All");
 
 		JButton findButton = new JButton("Find");
 
 		document.addUndoableEditListener(new UndoableEditListener() {
 			@Override
 			public void undoableEditHappened(UndoableEditEvent e) {
 				undoManager.addEdit(e.getEdit());
 				updateUndoRedoMenu();
 			}
 
 		});
 
 		undo.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					undoManager.undo();
 				} catch (CannotRedoException ex) {
 					JOptionPane.showMessageDialog(rootPane,
 							"Exception: " + ex.getLocalizedMessage(),
 							"Undo Exception", JOptionPane.ERROR_MESSAGE);
 				}
 				updateUndoRedoMenu();
 			}
 		});
 		undo.setIcon(new ImageIcon(getClass().getResource(
 				"images/Undo_16x16.png")));
 
 		redo.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					undoManager.redo();
 				} catch (CannotRedoException ex) {
 					JOptionPane.showMessageDialog(rootPane,
 							"Exception: " + ex.getLocalizedMessage(),
 							"Redo Exception", JOptionPane.ERROR_MESSAGE);
 				}
 				updateUndoRedoMenu();
 			}
 		});
 		redo.setIcon(new ImageIcon(getClass().getResource(
 				"images/Redo_16x16.png")));
 
 		findButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {

				try {

					if (findText.getText().length() == 0) {
						JOptionPane.showMessageDialog(rootPane,
								"Please type something to be found", "WARNING",
								JOptionPane.INFORMATION_MESSAGE);
					}
					highlight(TXEAREA, findText.getText());
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(rootPane,
							"Please type something to be found", "WARNING",
							JOptionPane.INFORMATION_MESSAGE);
				}
 			}
 		});
 
 		sA.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				TXEAREA.selectAll();
 
 			}
 		});
 
 		date.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Date date = new Date();
 				SimpleDateFormat sdt = new SimpleDateFormat(
 						"E dd.MM.yyyy 'at' hh:mm:ss a zzz");
 				TXEAREA.insert(sdt.format(date), TXEAREA.getCaretPosition());
 
 			}
 		});
 
 		print.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					boolean print = TXEAREA.print();
 					if (print) {
 						JOptionPane
 								.showMessageDialog(null, "Printing is Done!");
 					} else {
 
 					}
 
 				} catch (Exception exc) {
 
 				}
 			}
 
 		});
 
 		natives.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					UIManager.setLookAndFeel(UIManager
 							.getSystemLookAndFeelClassName());
 				} catch (Exception exc) {
 
 				}
 			}
 
 		});
 
 		nimbus.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					for (LookAndFeelInfo feel : UIManager
 							.getInstalledLookAndFeels()) {
 						if ("Nimbus".equals(feel.getName())) {
 							UIManager.setLookAndFeel(feel.getClassName());
 							break;
 						}
 					}
 				} catch (Exception exc) {
 
 				}
 
 			}
 		});
 		srenSht.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				/**
 				 * TXESHOT txesh = new TXESHOT(); txesh.setVisible(true);
 				 **/
 
 				try {
 
 					Rectangle screenshotRect = new Rectangle(Toolkit
 							.getDefaultToolkit().getScreenSize());
 					BufferedImage Capture = new Robot()
 							.createScreenCapture(screenshotRect);
 					// JFrame frame = new JFrame();
 					// frame.setVisible(true);
 					// frame.setResizable(false);
 					// frame.setSize(500,50);
 					// frame.add(saveField);
 					// saveField.setText("Screenshot name.png");
 					ImageIO.write(Capture, "png",
 							new File("TXE Screenshot.png"));
 					JOptionPane
 							.showMessageDialog(
 									getParent(),
 									"Your file has been saved, please change the file name to prevent overwriting. It was saved under the name TXE Screenshot.png.");
 				} catch (Exception ex) {
 
 					JOptionPane.showMessageDialog(getParent(),
 							"Error processing image...");
 
 				}
 			}
 
 		});
 		coL.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				color = JColorChooser
 						.showDialog(null, "Pick Text Color", color);
 				TXEAREA.setForeground(color);
 
 			}
 
 		});
 		enC.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Encrypt enc = new Encrypt();
 				enc.setVisible(true);
 			}
 		});
 		lsA.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				TXEAREA.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
 			}
 		});
 		rsA.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				TXEAREA.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
 
 			}
 		});
 		settingsFrame.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Settings txesettings = new Settings();
 				txesettings.setVisible(true);
 			}
 		});
 		addwebsite.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Settings settingsWeb = new Settings();
 
 				TXEAREA.setText(currentText + settingsWeb.webText1);
 			}
 		});
 		addname.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Settings settingsWeb = new Settings();
 				;
 				TXEAREA.setText(currentText + settingsWeb.nameText);
 			}
 		});
 		addwebsite.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Settings settingsWeb = new Settings();
 
 				TXEAREA.setText(currentText + settingsWeb.companyText1);
 			}
 		});
 		cL.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				{
 					TXEAREA.setText(changeLog);
 				}
 			}
 
 		});
 		// add stuff to format
 		format.add(plain);
 		format.add(bold);
 		format.add(italics);
 		format.add(bI);
 		format.addSeparator();
 		format.add(Fr);
 		format.addSeparator();
 		format.add(coL);
 		format.add(bLa);
 		format.add(rD);
 		format.add(gR);
 		format.add(bL);
 		format.addSeparator();
 		format.add(rsA);
 		format.add(lsA);
 
 		italics.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				{
 					TXEAREA.setFont(new Font("Times New Roman", Font.ITALIC, 12));
 				}
 			}
 
 		});
 		plain.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				{
 					TXEAREA.setFont(new Font("Times New Roman", Font.PLAIN, 12));
 				}
 			}
 
 		});
 		bold.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				{
 					TXEAREA.setFont(new Font("Times New Roman", Font.BOLD, 12));
 				}
 			}
 
 		});
 		bI.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				{
 					TXEAREA.setFont(new Font("Times New Roman", Font.BOLD
 							+ Font.ITALIC, 12));
 				}
 			}
 
 		});
 		Fr.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				JFontChooser fontChooser = new JFontChooser();
 
 				int result = fontChooser.showDialog(TXEAREA);
 				if (result == JFontChooser.OK_OPTION) {
 					Font font = fontChooser.getSelectedFont();
 					TXEAREA.setFont(font);
 					System.out.println("Selected Font : " + font);
 				}
 
 			}
 		});
 		gR.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				TXEAREA.setForeground(Color.green);
 
 			}
 		});
 		bL.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				TXEAREA.setForeground(Color.blue);
 
 			}
 		});
 		rD.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				TXEAREA.setForeground(Color.red);
 
 			}
 		});
 		bLa.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				TXEAREA.setForeground(Color.black);
 
 			}
 		});
 		About.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				About about = new About();
 				about.setVisible(true);
 
 			}
 		});
 		caL.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				Calculator calculator = new Calculator();
 				calculator.setVisible(true);
 
 			}
 		});
 
 		JMenu ScrollSettings = new JMenu("Settings");
 		JMenuItem vsbA = new JMenuItem("Vertical Scroll Bar Always");
 		JMenuItem hsbA = new JMenuItem("Horizontal Scroll Bar Always");
 		JMenuItem vhsbA = new JMenuItem(
 				"Vertical And Horizontal Scroll Bar Always");
 		vsbA.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				JScrollPane scrollv = new JScrollPane(TXEAREA,
 						JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
 						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 
 			}
 		});
 		vsbA.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				JScrollPane scrollh = new JScrollPane(TXEAREA,
 						JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
 						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 
 			}
 		});
 		vhsbA.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				JScrollPane scrollvh = new JScrollPane(TXEAREA,
 						JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
 						JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 
 			}
 		});
 
 		ScrollSettings.add(vsbA);
 		ScrollSettings.add(hsbA);
 		ScrollSettings.add(vhsbA);
 		JMenuBar JMB = new JMenuBar();
 
 		setJMenuBar(JMB);
 		// make the file and edit
 		JMenu file = new JMenu("File");
 
 		JMenu edit = new JMenu("Edit");
 
 		// add menus
 		JMB.add(file);
 		JMB.add(edit);
 		JMB.add(format);
 		JMB.add(TXESettings);
 
 		// file menu items
 		file.add(About);
 		file.addSeparator();
 		/** file.add(cL); **/
 		file.add(New);
 		file.addSeparator();
 		file.add(Open);
 		file.addSeparator();
 		file.add(Save);
 		file.add(SaveAs);
 		file.addSeparator();
 		file.add(print);
 		file.addSeparator();
 		file.add(Quit);
 		// tools menu items
 		TXESettings.add(settingsFrame);
 		TXESettings.addSeparator();
 		TXESettings.add(srenSht);
 		TXESettings.addSeparator();
 		TXESettings.add(addwebsite);
 		TXESettings.add(addname);
 		TXESettings.add(addcompany);
 		TXESettings.addSeparator();
 		TXESettings.add(caL);
 		TXESettings.add(enC);
 		TXESettings.addSeparator();
 		TXESettings.add(nimbus);
 		TXESettings.add(natives);
 
 		for (int i = 0; i < 1; i++)
 
 			file.getItem(i).setIcon(null);
 
 		// file.getItem(1).setText("New");
 
 		// edit.addSeparator();
 		edit.add(Cut);
 		edit.add(Copy);
 		edit.add(Paste);
 		edit.addSeparator();
 		edit.add(undo);
 		edit.add(redo);
 		edit.addSeparator();
 		edit.add(date);
 		edit.addSeparator();
 		edit.add(sA);
 
 		edit.getItem(0).setText("Cut		");
 		edit.getItem(0).setIcon(
 				new ImageIcon(getClass().getResource("images/cut.gif")));
 
 		edit.getItem(1).setText("Copy			");
 		edit.getItem(1).setIcon(
 				new ImageIcon(getClass().getResource("images/copy.gif")));
 
 		edit.getItem(2).setText("Paste			");
 		edit.getItem(2).setIcon(
 				new ImageIcon(getClass().getResource("images/paste.gif")));
 
 		for (int n = 0; n < 0; n++)
 
 			edit.getItem(n).setIcon(null);
 
 		JToolBar TXEBAR = new JToolBar();
 
 		this.add(TXEBAR, BorderLayout.SOUTH);
 
 		TXEBAR.add(New);
 		TXEBAR.add(Open);
 		TXEBAR.add(Save);
 
 		TXEBAR.addSeparator();
 
 		JButton cut = TXEBAR.add(Cut), cop = TXEBAR.add(Copy), pas = TXEBAR
 				.add(Paste);
 
 		cut.setText(null);
 		cut.setIcon(new ImageIcon(getClass().getResource("images/cut.gif")));
 
 		cop.setText(null);
 		cop.setIcon(new ImageIcon(getClass().getResource("images/copy.gif")));
 
 		pas.setText(null);
 		pas.setIcon(new ImageIcon(getClass().getResource("images/paste.gif")));
 
 		TXEBAR.addSeparator();
 		TXEBAR.add(Normal);
 		TXEBAR.add(Bold);
 		TXEBAR.add(Italics);
 		TXEBAR.add(BI);
 		TXEBAR.addSeparator();
 		TXEBAR.add(bLaB);
 		TXEBAR.add(rDb);
 		TXEBAR.add(gRb);
 		TXEBAR.add(bLb);
 		TXEBAR.addSeparator();
 		TXEBAR.add(findButton);
 		TXEBAR.add(findText);
 
 		Save.setEnabled(false);
 
 		SaveAs.setEnabled(false);
 
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 
 		this.pack();
 
 		TXEAREA.addKeyListener(k1);
 
 		this.setTitle("TXE 1.7.1 Beta - " + currentFile);
 
 		this.setVisible(true);
 
 	}
 
 	public void updateUndoRedoMenu() {
 		undo.setEnabled(undoManager.canUndo());
 		redo.setEnabled(undoManager.canRedo());
 
 	}
 
 	private void setIcon() {
 		// setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("Italic.gif")));
 
 	}
 
 	private KeyListener k1 = new KeyAdapter() {
 
 		public void keyPressed(KeyEvent e) {
 
 			changed = true;
 
 			Save.setEnabled(true);
 
 			SaveAs.setEnabled(true);
 		}
 
 	};
 	Action gRb = new AbstractAction("Green", new ImageIcon(getClass()
 			.getResource("images/green.gif"))) {
 
 		/**
  * 
  */
 		private static final long serialVersionUID = 1L;
 
 		public void actionPerformed(ActionEvent e) {
 			{
 				TXEAREA.setForeground(Color.GREEN);
 			}
 		}
 
 	};
 	Action bLaB = new AbstractAction("Normal", new ImageIcon(getClass()
 			.getResource("images/black.gif"))) {
 
 		/**
  * 
  */
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			{
 				TXEAREA.setForeground(Color.black);
 			}
 		}
 
 	};
 	Action rDb = new AbstractAction("Red", new ImageIcon(getClass()
 			.getResource("images/red.gif"))) {
 
 		/**
  * 
  */
 		private static final long serialVersionUID = 1L;
 
 		public void actionPerformed(ActionEvent e) {
 			{
 				TXEAREA.setForeground(Color.red);
 			}
 		}
 
 	};
 	Action bLb = new AbstractAction("Red", new ImageIcon(getClass()
 			.getResource("images/blue.gif"))) {
 
 		/**
 	 * 
 	 */
 		private static final long serialVersionUID = 1L;
 
 		public void actionPerformed(ActionEvent e) {
 			{
 				TXEAREA.setForeground(Color.blue);
 			}
 		}
 
 	};
 
 	Action BI = new AbstractAction("Bold and Italics", new ImageIcon(getClass()
 			.getResource("images/Bold and Italic.gif"))) {
 
 		/**
 	 * 
 	 */
 		private static final long serialVersionUID = 1L;
 
 		public void actionPerformed(ActionEvent e) {
 			{
 				TXEAREA.setFont(new Font("Times New Roman", Font.BOLD
 						+ Font.ITALIC, 12));
 			}
 		}
 
 	};
 
 	Action Normal = new AbstractAction("Normal", new ImageIcon(getClass()
 			.getResource("images/normal.gif"))) {
 
 		/**
 	 * 
 	 */
 		private static final long serialVersionUID = 1L;
 
 		public void actionPerformed(ActionEvent e) {
 			{
 				TXEAREA.setFont(new Font("Times New Roman", Font.PLAIN, 12));
 			}
 		}
 
 	};
 
 	Action Bold = new AbstractAction("Bold", new ImageIcon(getClass()
 			.getResource("images/Bold.gif"))) {
 
 		/**
 	 * 
 	 */
 		private static final long serialVersionUID = 1L;
 
 		public void actionPerformed(ActionEvent e) {
 			{
 				TXEAREA.setFont(new Font("Times New Roman", Font.BOLD, 12));
 			}
 		}
 
 	};
 
 	Action Italics = new AbstractAction("Italics", new ImageIcon(getClass()
 			.getResource("images/Italic.gif"))) {
 
 		/**
 	 * 
 	 */
 		private static final long serialVersionUID = 1L;
 
 		public void actionPerformed(ActionEvent e) {
 			{
 				TXEAREA.setFont(new Font("Times New Roman", Font.ITALIC, 12));
 			}
 		}
 
 	};
 
 	Action Open = new AbstractAction("Open", new ImageIcon(getClass()
 			.getResource("images/open.gif"))) {
 
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 
 			saveOld();
 
 			if (dialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
 
 				readInFile(dialog.getSelectedFile().getAbsolutePath());
 
 			}
 
 			SaveAs.setEnabled(true);
 
 		}
 
 	};
 	Action New = new AbstractAction("New", new ImageIcon(getClass()
 			.getResource("images/new.gif"))) {
 
 		private static final long serialVersionUID = 1L;
 
 		public void actionPerformed(ActionEvent e) {
 
 			saveOld();
 
 			TXEAREA.setText("Type here");
 
 			currentFile = "Untitled Document";
 
 			setTitle("TXE 1.7.1 Beta - " + currentFile);
 
 			changed = false;
 
 			Save.setEnabled(false);
 
 			SaveAs.setEnabled(false);
 
 		}
 
 	};
 
 	Action Save = new AbstractAction("Save", new ImageIcon(getClass()
 			.getResource("images/save.gif"))) {
 
 		/**
 	 * 
 	 */
 		private static final long serialVersionUID = 1L;
 
 		public void actionPerformed(ActionEvent e) {
 
 			if (!currentFile.equals(currentFile))
 
 				saveFile(currentFile);
 
 			else
 
 				saveFileAs();
 
 		}
 
 	};
 
 	Action SaveAs = new AbstractAction("Save as") {
 
 		/**
 	 * 
 	 */
 		private static final long serialVersionUID = 1L;
 
 		public void actionPerformed(ActionEvent e) {
 
 			saveFileAs();
 
 		}
 
 	};
 
 	Action Quit = new AbstractAction("Quit") {
 
 		/**
 	 * 
 	 */
 		private static final long serialVersionUID = 1L;
 
 		public void actionPerformed(ActionEvent e) {
 
 			saveOld();
 
 			System.exit(0);
 
 		}
 
 	};
 
 	ActionMap m = TXEAREA.getActionMap();
 
 	Action Cut = m.get(DefaultEditorKit.cutAction);
 
 	Action Copy = m.get(DefaultEditorKit.copyAction);
 
 	Action Paste = m.get(DefaultEditorKit.pasteAction);
 
 	private void saveOld() {
 
 		if (changed) {
 
 			if (JOptionPane.showConfirmDialog(this, "Would you like to save "
 					+ currentFile + " ?", "Save", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
 
 				saveFile(currentFile);
 
 		}
 
 	}
 
 	public void readInFile(String fileName) {
 
 		try {
 
 			FileReader fr = new FileReader(fileName);
 
 			TXEAREA.read(fr, null);
 
 			fr.close();
 
 			currentFile = fileName;
 
 			setTitle("TXE 1.7.1 Beta - " + currentFile);
 
 			changed = false;
 
 		}
 
 		catch (IOException e) {
 
 			JOptionPane.showMessageDialog(this, "TXE can not find the file: "
 					+ fileName);
 
 		}
 
 	}
 
 	private void saveFile(String fileName) {
 
 		try {
 
 			FileWriter fw = new FileWriter(fileName);
 
 			TXEAREA.write(fw);
 
 			fw.close();
 
 			currentFile = fileName;
 			// important
 			setTitle("Txe 1.7.1 Beta - " + currentFile);
 
 			changed = false;
 
 			Save.setEnabled(false);
 
 		}
 
 		catch (IOException e) {
 
 		}
 
 	}
 
 	private void saveFileAs() {
 
 		if (dialog.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
 
 			saveFile(dialog.getSelectedFile().getAbsolutePath());
 
 	}
 
 	/**
 	 * 
 	 * @author http://www.java2s.com/
 	 * 
 	 */
 
 	class FileSystemModel implements TreeModel {
 		private String root; // The root identifier
 
 		private Vector listeners; // Declare the listeners vector
 
 		public FileSystemModel() {
 
 			root = System.getProperty("user.dir");
 			File tempFile = new File(root);
 			root = tempFile.getParent();
 
 			listeners = new Vector();
 		}
 
 		public Object getRoot() {
 			return (new File(root));
 		}
 
 		public Object getChild(Object parent, int index) {
 			File directory = (File) parent;
 			String[] directoryMembers = directory.list();
 			return (new File(directory, directoryMembers[index]));
 		}
 
 		public int getChildCount(Object parent) {
 			File fileSystemMember = (File) parent;
 			if (fileSystemMember.isDirectory()) {
 				String[] directoryMembers = fileSystemMember.list();
 				return directoryMembers.length;
 			}
 
 			else {
 
 				return 0;
 			}
 		}
 
 		public int getIndexOfChild(Object parent, Object child) {
 			File directory = (File) parent;
 			File directoryMember = (File) child;
 			String[] directoryMemberNames = directory.list();
 			int result = -1;
 
 			for (int i = 0; i < directoryMemberNames.length; ++i) {
 				if (directoryMember.getName().equals(directoryMemberNames[i])) {
 					result = i;
 					break;
 				}
 			}
 
 			return result;
 		}
 
 		public boolean isLeaf(Object node) {
 			return ((File) node).isFile();
 		}
 
 		public void addTreeModelListener(TreeModelListener l) {
 			if (l != null && !listeners.contains(l)) {
 				listeners.addElement(l);
 			}
 		}
 
 		public void removeTreeModelListener(TreeModelListener l) {
 			if (l != null) {
 				listeners.removeElement(l);
 			}
 		}
 
 		public void valueForPathChanged(TreePath path, Object newValue) {
 			// Does Nothing!
 		}
 
 		public void fireTreeNodesInserted(TreeModelEvent e) {
 			Enumeration listenerCount = listeners.elements();
 			while (listenerCount.hasMoreElements()) {
 				TreeModelListener listener = (TreeModelListener) listenerCount
 						.nextElement();
 				listener.treeNodesInserted(e);
 			}
 		}
 
 		public void fireTreeNodesRemoved(TreeModelEvent e) {
 			Enumeration listenerCount = listeners.elements();
 			while (listenerCount.hasMoreElements()) {
 				TreeModelListener listener = (TreeModelListener) listenerCount
 						.nextElement();
 				listener.treeNodesRemoved(e);
 			}
 
 		}
 
 		public void fireTreeNodesChanged(TreeModelEvent e) {
 			Enumeration listenerCount = listeners.elements();
 			while (listenerCount.hasMoreElements()) {
 				TreeModelListener listener = (TreeModelListener) listenerCount
 						.nextElement();
 				listener.treeNodesChanged(e);
 			}
 
 		}
 
 		public void fireTreeStructureChanged(TreeModelEvent e) {
 			Enumeration listenerCount = listeners.elements();
 			while (listenerCount.hasMoreElements()) {
 				TreeModelListener listener = (TreeModelListener) listenerCount
 						.nextElement();
 				listener.treeStructureChanged(e);
 			}
 
 		}
 	}
 
 	/**
 	 * 
 	 * @author ericzhu
 	 * 
 	 */
 	class highLight extends DefaultHighlighter.DefaultHighlightPainter {
 		public highLight(Color color) {
 			super(color);
 		}
 
 	}
 
 	Highlighter.HighlightPainter HighLight = new highLight(Color.yellow);
 
 	public void removeHighlight(JTextComponent comp) {
 		Highlighter highlighte = comp.getHighlighter();
 		Highlighter.Highlight[] higlite = highlighte.getHighlights();
 		for (int i = 0; i < higlite.length; i++) {
 			if (higlite[i].getPainter() instanceof highLight) {
 				highlighte.removeHighlight(higlite[i]);
 			}
 		}
 	}
 
 	public void highlight(JTextComponent comp, String pattern) {
 
 		removeHighlight(comp);
 
 		try {
 
 			Highlighter highlighte = comp.getHighlighter();
 			Document doc = comp.getDocument();
 			String text = doc.getText(0, doc.getLength());
 			int pos = 0;
 			while ((pos = text.toUpperCase()
 					.indexOf(pattern.toUpperCase(), pos)) >= 0) {
 				highlighte.addHighlight(pos, pos + pattern.length(), HighLight);
 				pos += pattern.length();
 			}
 		} catch (Exception ex) {
 
 		}
 	}
 
 	public static void main(String[] args) {
 		TXE1 txe1 = new TXE1();
 
 	}
 
 }
