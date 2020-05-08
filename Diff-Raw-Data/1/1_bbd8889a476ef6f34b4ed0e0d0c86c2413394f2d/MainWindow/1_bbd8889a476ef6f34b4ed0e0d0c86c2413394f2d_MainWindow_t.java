 package webApplication.grafica;
 
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 
 import java.awt.event.InputEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.EventObject;
 import java.util.Iterator;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JEditorPane;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JTree;
 import javax.swing.KeyStroke;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.LineBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.text.Document;
 import javax.swing.text.JTextComponent;
 
 import webApplication.business.Componente;
 import webApplication.business.ComponenteAlternative;
 import webApplication.business.ComponenteComposto;
 import webApplication.business.ComponenteSemplice;
 import webApplication.business.Immagine;
 import webApplication.business.Link;
 import webApplication.business.Testo;
 
 import webApplication.grafica.TreePanel;
 
 
 import java.awt.TextField;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.Vector;
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import javax.swing.BoxLayout;
 import java.util.Collections;
 
 public class MainWindow extends JFrame {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private static JPanel contentPane;
 	private static JTextField textField_Name;
 	private static JTextField textField_Type;
 	private static JTextField textField_Category;
 	private static JComboBox comboBox_Importance;
 	private static JComboBox comboBox_Emphasize;
 	private static JTextArea editorPane_text;
 	private static JTextField textField_imagepath;
 	private static JPanel content_panel;
 	private static JList list_composite;
 	private static JList list_alternative;
 	private static JPanel panel_composite;
 	private static PannelloAlternative pannello_alterplus;
 	private static JButton button_deleteFromComp;
 	private static JButton button_delFromAlt;
 	private static JButton button_addExistComp;
 	private static JButton button_addNewComp;
 	private static JButton button_AddExisAlt;
 	private static JButton button_up;
 	private static JButton button_down;
 	private static JPanel errorePath;
 	private static JPanel erroreTestoLink;
 	private static JPanel erroreUrl;
 	private AListenerRemoveFromAlt actionAlternative;
 	
 	public static final int LOADSAVE = 0;
 	public static final int IMAGE = 1;
 	public static final int TEXT = 2;
 	
 	private static Options frameOptions;
 	
 	private static Componente focused;
 
 	private static Testo focusedTxt;
 	private static Immagine focusedImg;
 	private static Link focusedLnk;
 	private static ComponenteComposto focusedCmp;
 	private static ComponenteAlternative focusedAlt;
 
 	private static final String URL_REGEX =
             "^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?";
     private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);
 	
 	
 	private static final String[] categorie = { "Necessary", "Indifferent", "Expendable"}; //FIXME Andrebbero rese globali per tutte le classi??
 	private static final String[] importanze = { "Greatly", "Normally", "Not at all"}; //FIXME Andrebbero rese globali per tutte le classi?? E ne mancano 2 che non ricordo
 
 	private static JTextField textField_linktext;
 	private static JTextField textField_url;
 	//TODO le due stringhe andrebbero esportate da qualche altra parte
 	
 	private static final String PANEL_TXT="panel_text";
 	private static final String PANEL_IMG="panel_image";
 	private static final String PANEL_LNK="panel_link";
 	public static final String PANEL_ALT="panel_alternative";
 	private static final String PANEL_CMP="panel_composite";
 	
 	public static final int MOVE_UP = -1;
 	public static final int MOVE_DOWN = +1;
 
 	
 	
 	public MainWindowData data = new MainWindowData();
 	private static TreePanel albero;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					MainWindow frame = new MainWindow();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	public MainWindow() {
 		setTitle("EUD-MAMBA");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 728, 502);
 		setResizable(false);
 		data.setThisWindow(this);
 
 		JMenuBar menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 
 		JMenu mnFile = new JMenu("File");
 		menuBar.add(mnFile);
 
 		JMenuItem mntmNew = new JMenuItem("New");
 		mntmNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
 		mntmNew.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent e) {
 				String newPath = fileChooser(LOADSAVE);
 				
 				
 				if (newPath.length()>0)
 					//TODO aprire un JDialog per chiedere di salvare se il vecchio proj e' stato modificato
 					data.setCurrentProject(newPath);
 					//TODO creare nuovo JTree
 				}
 		});
 		mntmNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
 				InputEvent.CTRL_MASK));
 		mnFile.add(mntmNew);
 
 		JMenuItem mntmOpen = new JMenuItem("Open");
 		mntmOpen.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent e) {
 				String newPath = fileChooser(LOADSAVE);
 
 				
 				if (newPath.length()>0)
 					//TODO aprire un JDialog per chiedere di salvare se il vecchio proj e' stato modificato
 					//TODO controllare che il nuovo file esista, e sia corretto
 					data.setCurrentProject(newPath);
 				}
 		});
 		mnFile.add(mntmOpen);
 
 		JMenuItem mntmSave = new JMenuItem("Save");
 		mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
 				InputEvent.CTRL_MASK));
 		mnFile.add(mntmSave);
 
 		JSeparator separator = new JSeparator();
 		mnFile.add(separator);
 
 		JMenuItem mntmExit = new JMenuItem("Exit");
 		mntmExit.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent e) {
 				//TODO controllare che non ci sia qualcosa da salvare prima di chiudere
 				dispose();
 				 System.exit(0);
 			}
 		});
 		mnFile.add(mntmExit);
 
 		JMenu mnEdit = new JMenu("Edit");
 		mnEdit.setEnabled(false);
 		menuBar.add(mnEdit);
 
 		JMenuItem mntmUndo = new JMenuItem("Undo");
 		mntmUndo.setEnabled(false);
 		mnEdit.add(mntmUndo);
 
 		JMenuItem mntmRedo = new JMenuItem("Redo");
 		mnEdit.add(mntmRedo);
 		mntmRedo.setEnabled(false);
 
 		JSeparator separator_1 = new JSeparator();
 		mnEdit.add(separator_1);
 
 		JMenuItem mntmCut = new JMenuItem("Cut");
 		mnEdit.add(mntmCut);
 		mntmCut.setEnabled(false);
 
 		JMenuItem mntmCopy = new JMenuItem("Copy");
 		mnEdit.add(mntmCopy);
 		mntmCopy.setEnabled(false);
 
 		JMenuItem mntmPaste = new JMenuItem("Paste");
 		mnEdit.add(mntmPaste);
 		mntmPaste.setEnabled(false);
 
 		JMenu mnOptions = new JMenu("TODO Options");
 		menuBar.add(mnOptions);
 
 		JMenuItem mntmOptions = new JMenuItem("Image directory");
 		
 		mntmOptions.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 
 				try {
 					setEnabled(false);
 					//TIP qua probabilmente c'e' la sol http://castever.wordpress.com/2008/07/31/how-to-create-your-own-events-in-java/
 					if (frameOptions== null)
 						frameOptions = new Options();
 					frameOptions.addWindowListener(new WindowAdapter(){
 						@Override
 						public void windowClosing(WindowEvent e) {
 							frameOptions.dispose();
 							setEnabled(true);
 						}
 						
 						
 					});
 
 					frameOptions.setVisible(true);
 					frameOptions.addEventListener(new MyEventClassListener(){
 
 						@Override
 						public void handleMyEventClassEvent(
 								EventObject e) {
 							setEnabled(true);
 
 						}
 
 						@Override
 						public void handleMyEventClassEvent(MyEventClass e) {
 							// TODO Auto-generated method stub
 							
 						}});
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				//options.menu();
 			}
 		});
 		mnOptions.add(mntmOptions);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 
 		JPanel panel = new JPanel();
 		panel.setBounds(5, 0, 710, 37);
 		panel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING,
 				TitledBorder.TOP, null, null));
 		contentPane.add(panel);
 		panel.setLayout(null);
 
 		JButton button = new JButton("");
 		button.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				setFocus(data.getTxt());
 			}
 		});
 		button.setBounds(12, 4, 30, 30);
 		panel.add(button);
 		button.setToolTipText("Open");
 		button.setIcon(new ImageIcon(
 				MainWindow.class
 						.getResource("/com/sun/java/swing/plaf/motif/icons/TreeOpen.gif")));
 
 		JButton button_3 = new JButton("");
 		button_3.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				setFocus(data.getImg());
 			}
 		});
 		button_3.setToolTipText("Open");
 		button_3.setBounds(45, 4, 30, 30);
 		panel.add(button_3);
 
 		JButton button_4 = new JButton("");
 		button_4.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				setFocus(data.getLnk());
 			}
 		});
 		button_4.setToolTipText("Open");
 		button_4.setBounds(78, 4, 30, 30);
 		panel.add(button_4);
 
 		JButton button_1 = new JButton("");
 		button_1.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				setFocus(data.getAlt());
 			}
 		});
 		button_1.setToolTipText("Open");
 		button_1.setBounds(120, 4, 30, 30);
 		panel.add(button_1);
 
 		JButton button_2 = new JButton("");
 		button_2.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				setFocus(data.getCmp());
 			}
 		});
 		button_2.setToolTipText("Open");
 		button_2.setBounds(153, 4, 30, 30);
 		panel.add(button_2);
 
 		JButton button_5 = new JButton("");
 		button_5.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				setEnabled(false);
 				//TIP qua probabilmente c'e' la sol http://castever.wordpress.com/2008/07/31/how-to-create-your-own-events-in-java/
 				if (data.getMyWizard()== null)
 					data.setMyWizard(new Wizard());
 				data.getMyWizard().setVisible(true);
 				data.getMyWizard().addEventListener(new MyEventClassListener(){
 
 					@Override
 					public void handleMyEventClassEvent(
 							EventObject e) {
 								setEnabled(true);
 								data.setMyWizard(null);
 						// TODO Auto-generated method stub
 						
 					}
 
 					@Override
 					public void handleMyEventClassEvent(MyEventClass e) {
 						// TODO Auto-generated method stub
 						
 					}});
 			}
 		});
 		button_5.setIcon(new ImageIcon("/home/enrico/Documenti/PSI/icons/list-add-md.png"));
 		button_5.setToolTipText("Open");
 		button_5.setBounds(195, 4, 30, 30);
 		panel.add(button_5);
 
 		JButton button_6 = new JButton("");
 		button_6.setToolTipText("Open");
 		button_6.setBounds(228, 4, 30, 30);
 		panel.add(button_6);
 
 		JButton addButton = new JButton("");
 		addButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				addNewWizard();
 			}
 		});
 		addButton.setIcon(new ImageIcon(MainWindow.class.getResource("/webApplication/grafica/add_icon.gif")));
 		addButton.setToolTipText("Open");
 		addButton.setBounds(277, 4, 30, 30);
 		panel.add(addButton);
 
 		JButton button_8 = new JButton("");
 		button_8.setIcon(new ImageIcon(MainWindow.class.getResource("/com/sun/java/swing/plaf/gtk/resources/gtk-cancel-4.png")));
 		button_8.setToolTipText("Open");
 		button_8.setBounds(310, 4, 30, 30);
 		panel.add(button_8);
 
 		JButton btnGenerateWebsite = new JButton("GENERATE WEBSITE");
 		
 		btnGenerateWebsite.setToolTipText("Open");
 		btnGenerateWebsite.setBounds(365, 4, 187, 30);
 		boldify(btnGenerateWebsite);
 		panel.add(btnGenerateWebsite);
 
 		JPanel properties = new JPanel();
 		properties.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229), 1, true), " Properties ", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
 
 		properties.setLayout(null);
 		properties.setBounds(249, 49, 466, 392);
 		contentPane.add(properties);
 
 		JPanel id_panel = new JPanel();
 		id_panel.setBounds(12, 18, 193, 125);
 		properties.add(id_panel);
 		id_panel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207,
 				229)), " ID ", TitledBorder.LEADING, TitledBorder.TOP, null,
 				new Color(51, 51, 51)));
 		id_panel.setLayout(null);
 
 		JLabel lblName = new JLabel("Name:");
 		lblName.setBounds(12, 40, 51, 15);
 		id_panel.add(lblName);
 
 		textField_Name = new JTextField();
 		textField_Name.setToolTipText("name");
 		
 		// TODO mettere check che il nome non sia gia' esistente
 		textField_Name.getDocument().addDocumentListener(new DocumentListener() {
 			public void changedUpdate(DocumentEvent e) {
 				setFocusedName();
 				albero.getTree().repaint();
 			}
 			public void removeUpdate(DocumentEvent e) {
 				setFocusedName();
 				albero.getTree().repaint();
 			}
 			public void insertUpdate(DocumentEvent e) {
 				setFocusedName();
 				albero.getTree().repaint();
 			}
 			});
 		textField_Name.setBounds(67, 40, 114, 19);
 		id_panel.add(textField_Name);
 		textField_Name.setColumns(10);
 
 		JLabel lblType = new JLabel("Type:");
 		lblType.setBounds(12, 70, 51, 15);
 		id_panel.add(lblType);
 
 		textField_Type = new JTextField();
 		textField_Type.setEditable(false);
 		textField_Type.setColumns(10);
 		textField_Type.setBounds(67, 70, 114, 19);
 		id_panel.add(textField_Type);
 
 		JPanel presentation_panel = new JPanel();
 		presentation_panel.setBorder(new TitledBorder(new LineBorder(new Color(
 				184, 207, 229)), " Presentation ", TitledBorder.LEADING,
 				TitledBorder.TOP, null, new Color(51, 51, 51)));
 		presentation_panel.setBounds(217, 18, 237, 125);
 		properties.add(presentation_panel);
 		presentation_panel.setLayout(null);
 
 		JLabel lblCategory = new JLabel("Category:");
 		lblCategory.setBounds(12, 24, 81, 15);
 		presentation_panel.add(lblCategory);
 
 		JLabel lblEmphasize = new JLabel("Emphasize:");
 		lblEmphasize.setBounds(12, 54, 81, 14);
 		presentation_panel.add(lblEmphasize);
 
 		comboBox_Importance = new JComboBox(importanze);
 		comboBox_Importance.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				setFocusedImpo();
 			}
 		});
 		
 		comboBox_Importance.setBounds(111, 49, 112, 24);
 		presentation_panel.add(comboBox_Importance);
 
 		textField_Category = new JTextField();
 		textField_Category.getDocument().addDocumentListener(new DocumentListener() {
 			public void changedUpdate(DocumentEvent e) {
 				setFocusedCategory();
 			}
 			public void removeUpdate(DocumentEvent e) {
 				setFocusedCategory();
 			}
 			public void insertUpdate(DocumentEvent e) {
 				setFocusedCategory();
 			}
 			});
 		
 		textField_Category.setColumns(10);
 		textField_Category.setBounds(109, 22, 114, 19);
 		presentation_panel.add(textField_Category);
 
 		JLabel lblImportance = new JLabel("Importance:");
 		lblImportance.setBounds(12, 86, 97, 15);
 		presentation_panel.add(lblImportance);
 
 		comboBox_Emphasize = new JComboBox(categorie);
 		comboBox_Emphasize.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setFocusedEmph();
 			}
 		});
 		
 		comboBox_Emphasize.setBounds(111, 85, 112, 24);
 		presentation_panel.add(comboBox_Emphasize);
 
 		// TODO Gestire i diversi contenuti per i vari tipi di oggetto
 		// (soprattutto i composite e alternative)
 		// TODO mettere immagine priorità per alternative, up e down
 		content_panel = new JPanel();
 		content_panel.setBorder(new TitledBorder(new LineBorder(new Color(184,
 				207, 229)), " Content ", TitledBorder.LEADING,
 				TitledBorder.TOP, null, new Color(51, 51, 51)));
 		content_panel.setBounds(12, 155, 442, 225);
 		properties.add(content_panel);
 		content_panel.setLayout(new CardLayout(0, 0));
 
 		JPanel panel_image = new JPanel();
 		content_panel.add(panel_image, PANEL_IMG);
 		panel_image.setLayout(null);
 
 		JLabel label_2 = new JLabel("File path:");
 		label_2.setBounds(22, 12, 91, 14);
 		panel_image.add(label_2);
 				
 		textField_imagepath = new JTextField();
 		textField_imagepath.setToolTipText("Path of the image file");
 		textField_imagepath.getDocument().addDocumentListener(new DocumentListener() {
 			public void changedUpdate(DocumentEvent e) {
 				
 				updateImagePath();
 			}
 			public void removeUpdate(DocumentEvent e) {
 				updateImagePath();
 			// text was deleted
 			}
 			public void insertUpdate(DocumentEvent e) {
 				updateImagePath();
 
 			// text was inserted
 			}
 			});
 		
 		textField_imagepath.setBounds(22, 42, 292, 22);
 		panel_image.add(textField_imagepath);
 		
 		JButton button_browseImg = new JButton("Browse\r\n");
 		button_browseImg.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				fileChooser(IMAGE, textField_imagepath);
 				
 			}
 		});
 		button_browseImg.setBounds(331, 39, 89, 29);
 		panel_image.add(button_browseImg);
 		
 		errorePath = new JPanel();
 		errorePath.setToolTipText("The file doesn't exist or is not readable");
 		errorePath.setBorder(new LineBorder(Color.RED));
 		errorePath.setBounds(19, 38, 300, 30);
 		panel_image.add(errorePath);
 		
 		panel_composite = new JPanel();
 /*=======
 
 		JButton button_17 = new JButton("Browse\r\n");
 		button_17.setBounds(298, 25, 89, 29);
 		panel_image.add(button_17);
 
 		JPanel panel_composite = new JPanel();
 >>>>>>> refs/remotes/org.eclipse.jgit.transport.RemoteConfig@1aa9a7bb/testing*/
 		content_panel.add(panel_composite, PANEL_CMP);
 		panel_composite.setLayout(null);
 
 		JLabel label_1 = new JLabel("Elements:");
 		label_1.setBounds(12, 0, 91, 13);
 		panel_composite.add(label_1);
 
 		list_composite = new JList();
 		//TODO aggiungere un bottone o un menu contestuale per vedere i dettagli degli elementi
 		
 		list_composite.setBounds(12, 25, 408, 132);
 		panel_composite.add(list_composite);
 		
 		//Aggiunta la scroll bar
 		//scrollPane_composite = new JScrollPane(list_composite);
 		
 		button_deleteFromComp = new JButton("Delete");
 		button_deleteFromComp.addActionListener(new java.awt.event.ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO fare un controllo sul nome dell'oggetto
 				removeElementFromComposto(list_composite.getSelectedIndices());
 				
 			}
 		});
 		button_deleteFromComp.setBounds(12, 162, 91, 27);
 		panel_composite.add(button_deleteFromComp);
 		
 		button_addExistComp = new JButton("Add existing");
 		button_addExistComp.setBounds(195, 162, 121, 27);
 		panel_composite.add(button_addExistComp);
 		
 		button_addNewComp = new JButton("Add new");
 		button_addNewComp.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setEnabled(false);
 				AddNew nuovo = new AddNew();
 				nuovo.addWindowListener(new WindowAdapter(){
 					@Override
 					public void windowClosing(WindowEvent e) {
 						setEnabled(true);
 					}
 				});
 				nuovo.addEventListener(new MyEventClassListener(){
 
 					@Override
 					public void handleMyEventClassEvent(MyEventClass e) {
 								setEnabled(true);
 								if(e != null){
 									addElementToComposite((ComponenteSemplice) e.getComponente());
 								}
 					}
 
 					@Override
 					public void handleMyEventClassEvent(EventObject e) {
 						// TODO Auto-generated method stub
 						
 					}
 
 					
 
 					});
 
 				nuovo.setVisible(true);
 			}
 		});
 		button_addNewComp.setBounds(320, 162, 100, 27);
 		panel_composite.add(button_addNewComp);
 		
 		/*panel_alternative = new JPanel();
 		list_alternative = new JList();
 		button_down = new JButton("v");
 		button_up = new JButton("^");
 		button_delFromAlt = new JButton("Delete");
 		button_AddExisAlt = new JButton("Add existing");*/
 		pannello_alterplus = new PannelloAlternative(this);
 		//buildPanelAlternative(panel_alternative, button_up, button_down, button_delFromAlt, button_AddExisAlt, list_alternative);
 		content_panel.add(pannello_alterplus, PANEL_ALT);
 
 		JPanel panel_link = new JPanel();
 		content_panel.add(panel_link, PANEL_LNK);
 		panel_link.setLayout(null);
 
 		JLabel lbl_linktxt = new JLabel("Link text:");
 		lbl_linktxt.setBounds(13, 14, 78, 15);
 		panel_link.add(lbl_linktxt);
		
 
 		textField_linktext = new JTextField();
 		textField_linktext.getDocument().addDocumentListener(new DocumentListener() {
 			public void changedUpdate(DocumentEvent e) {
 
 				updateLinkText();
 			}
 			public void removeUpdate(DocumentEvent e) {
 				updateLinkText();
 				// text was deleted
 			}
 			public void insertUpdate(DocumentEvent e) {
 				updateLinkText();
 
 				// text was inserted
 			}
 		});
 		
 		textField_linktext.setColumns(10);
 		textField_linktext.setBounds(93, 12, 313, 19);
 		panel_link.add(textField_linktext);
 
 		JLabel lbl_url = new JLabel("URL:");
 		lbl_url.setBounds(12, 47, 78, 15);
 		panel_link.add(lbl_url);
 		
 		textField_url = new JTextField();
 		
 		textField_url.getDocument().addDocumentListener(new DocumentListener() {
 			public void changedUpdate(DocumentEvent e) {
 				updateLinkUrl();
 			}
 			public void removeUpdate(DocumentEvent e) {
 				updateLinkUrl();
 			}
 			public void insertUpdate(DocumentEvent e) {
 				updateLinkUrl();
 
 			}
 		});
 		textField_url.setColumns(10);
 		textField_url.setBounds(93, 45, 313, 19);
 		panel_link.add(textField_url);
 		
 		erroreTestoLink = new JPanel();
 		erroreTestoLink.setBorder(new LineBorder(Color.RED));
 		erroreTestoLink.setToolTipText("");
 		erroreTestoLink.setBounds(90, 9, 319, 24);
 		panel_link.add(erroreTestoLink);
 		
 		erroreUrl = new JPanel();
 		erroreUrl.setBorder(new LineBorder(Color.RED));
 		erroreUrl.setToolTipText("");
 		erroreUrl.setBounds(90, 42, 319, 24);
 		panel_link.add(erroreUrl);
 		
 
 		JPanel panel_text = new JPanel();
 		content_panel.add(panel_text, PANEL_TXT);
 		panel_text.setLayout(null);
 
 		JLabel label_namecontent = new JLabel("Name:");
 		label_namecontent.setBounds(12, 5, 45, 15);
 		panel_text.add(label_namecontent);
 		
 		editorPane_text = new JTextArea();
 		JScrollPane scrollingArea = new JScrollPane(editorPane_text);
 		scrollingArea.setBounds(12, 32, 408, 156);
 		editorPane_text.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusLost(FocusEvent e) {
 				updateTextContent();
 			}
 		});
 		//editorPane_text.setBounds(12, 32, 408, 156);
 		panel_text.add(scrollingArea);
 
 		albero = new TreePanel(this);
 		albero.setBounds(15, 63, 222, 378);
 		contentPane.add(albero);
 		albero.setLayout(new BoxLayout(albero, BoxLayout.X_AXIS));
 		
 		
 
 		// TODO rimuovere invocazione a testing concluso
 		popolaOggetti();
 
 //>>>>>>> refs/remotes/org.eclipse.jgit.transport.RemoteConfig@1aa9a7bb/testing
 	}
 	//invocazione: panel_alternative, button_up, button_down, button_delFromAlt, button_AddExisAlt, list_alternative
 	private void buildPanelAlternative(JPanel panelAlt, JButton b_up, JButton b_down, JButton b_del, JButton b_addExist, JList l_alt) {
 
 		panelAlt.setLayout(null);
 		
 		//TODO cambiare le icone terribili dei bottoni up e down
 
 		
 		b_up.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				moveAlternativeElements(MainWindow.MOVE_UP);}});
 		b_up.setToolTipText("Click here to increase the priority of selected element");
 		b_up.setBounds(12, 0, 46, 53);
 		panelAlt.add(b_up);
 
 		JScrollPane scrollPane = new JScrollPane();
 		scrollPane.setLocation(70, 0);
 		scrollPane.setSize(350, 149);
 		panelAlt.add(scrollPane);
 		
 		scrollPane.setViewportView(l_alt);
 
 		b_down.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				moveAlternativeElements(MainWindow.MOVE_DOWN);
 		}});
 		b_down.setToolTipText("Click here to decrease the priority of selected element");
 		b_down.setBounds(12, 96, 46, 53);
 		panelAlt.add(b_down);
 
 		
 		b_del.setBounds(65, 161, 90, 27);
 		actionAlternative = new AListenerRemoveFromAlt(l_alt);
 		b_del.addActionListener(actionAlternative);
 		panelAlt.add(b_del);
 		
 		//TODO se non ne esistono di esistenti disabilitare
 		
 		b_addExist.setBounds(198, 161, 121, 27);
 		panelAlt.add(b_addExist);
 
 		JButton button_addNewAlter = new JButton("Add new");
 		button_addNewAlter.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setEnabled(false);
 				AddNew nuovo = new AddNew();
 				
 				nuovo.addWindowListener(new WindowAdapter(){
 					@Override
 					public void windowClosing(WindowEvent e) {
 						setEnabled(true);
 				}});
 				
 				nuovo.addEventListener(new MyEventClassListener(){
 
 					@Override
 					public void handleMyEventClassEvent(
 							MyEventClass e) {
 								setEnabled(true);
 								if(e != null){
 									addElementToAlternative((ComponenteSemplice) e.getComponente());
 									}}
 
 					@Override
 					public void handleMyEventClassEvent(EventObject e) {}
 					});
 
 				nuovo.setVisible(true);
 		}});
 		button_addNewAlter.setBounds(322, 161, 98, 27);
 		panelAlt.add(button_addNewAlter);
 	}
 
 	private void addNewWizard() {
 		//TODO agganciarci il wizard e non l'addnew
 		//TODO andrebbe creata una classe e tolto il codice da qui
 
 		setEnabled(false);
 		Wizard nuovo = new Wizard();
 		nuovo.addWindowListener(new WindowAdapter(){
 			@Override
 			public void windowClosing(WindowEvent e) {
 				setEnabled(true);
 			}
 		});
 		nuovo.addEventListener(new MyEventClassListener(){
 
 			@Override
 			public void handleMyEventClassEvent(
 					MyEventClass e) {
 						setEnabled(true);
 						if(e != null){
 							addElementToTree(e.getComponente());
 							
 						}
 			}
 
 			@Override
 			public void handleMyEventClassEvent(EventObject e) {
 				// TODO Auto-generated method stub
 				
 			}});
 
 		nuovo.setVisible(true);
 	}
 
 	protected void addElementToTree(Componente componente) {
 		if(focused == null || focused.getType()==Testo.TEXTTYPE || focused.getType() == Link.LINKTYPE || focused.getType()== Immagine.IMAGETYPE)
 			albero.addNode(null, componente);
 		//TODO gestire l'inserimento in un componente composto
 	}
 
 	private static void setGenerici(Componente selected, String type) {
 		textField_Name.setText(selected.getNome());
 
 		textField_Category.setText(selected.getCategoria());
 
 		textField_Type.setText(type);
 
 		// TODO verificare che l'ordine sia giusto (che il numero restituito dal
 		// getenfasi corrisp a quello del menu a tendina)
 		comboBox_Emphasize.setSelectedIndex(selected.getEnfasi());
 		comboBox_Importance.setSelectedIndex(selected.getVisibilita());
 
 	}
 
 	private static void popolaProperties(Testo selected) {
 		setGenerici(selected, "Text");
 		editorPane_text.setText(selected.getTesto());
 
 		setContentLayout(PANEL_TXT);
 	}
 
 	private static void popolaProperties(Immagine selected) {
 		setGenerici(selected, "Image");
 		textField_imagepath.setText(selected.getPath());
 
 		setContentLayout(PANEL_IMG);
 
 	}
 	
 	private static void popolaProperties(Link selected){
 		setGenerici(selected,"Link");
 		textField_url.setText(selected.getUri());
 
 		textField_linktext.setText(selected.getTesto());
 
 		setContentLayout(PANEL_LNK);
 	}
 
 	private static void popolaProperties(ComponenteAlternative selected) {
 		setGenerici(selected, "Alternative");
 		
 		pannello_alterplus.setAlternativeComponent(selected);
 		setContentLayout(PANEL_ALT);
 		/*
 		Container listContainer= list_alternative.getParent();
 		
 		if(list_alternative != null && listContainer!=null)
 			listContainer.remove(list_alternative);
 		
 		
 		
 		
 		list_alternative = new JList(Utils.extractNomiComponenti(selected.getAlternative()));
 		
 		listContainer.add(list_alternative);
 		
 		Utils.buttonDeleteMgmt(list_alternative,button_delFromAlt);
 		buttonUpDownMgmt();
 		
 		list_addFocusList(list_alternative);
 
 		listContainer.repaint();*/
 
 	}
 	
 	private static void buttonUpDownMgmt() {
 		int num_elem = list_alternative.getModel().getSize();
 		int max_selected = list_alternative.getMaxSelectionIndex(); // -1 se nessun elemento selezionato
 		int min_selected = list_alternative.getMinSelectionIndex();
 		
 		if(num_elem < 2 || max_selected == -1 || (min_selected == 0 && max_selected == num_elem-1)){
 			button_down.setEnabled(false);
 			button_up.setEnabled(false);
 		}
 		else if(min_selected == 0){
 			button_down.setEnabled(true);
 			button_up.setEnabled(false);
 		}
 		else if(max_selected == num_elem-1){
 			button_down.setEnabled(false);
 			button_up.setEnabled(true);
 		}
 		else {
 			button_down.setEnabled(true);
 			button_up.setEnabled(true);
 		}
 	}
 	
 	private void moveAlternativeElements(int upOrDown) {
 		int shift;
 		ComponenteAlternative comp = ((ComponenteAlternative)focused); 
 		Vector<ComponenteSemplice> listaAlternative = comp.getAlternative();
 		int i;
 		int[] toMove = list_alternative.getSelectedIndices();
 		
 		if(upOrDown == MOVE_UP){
 			shift=MOVE_UP;
 			for (i=0; i<toMove.length; i++){
 				Collections.swap(listaAlternative,toMove[i], toMove[i]+shift);
 				toMove[i]= toMove[i]+shift;
 			}
 		}
 		else if(upOrDown == MOVE_DOWN){
 			shift = MOVE_DOWN;
 			for (i=toMove.length-1; i>=0; i--){
 				Collections.swap(listaAlternative,toMove[i], toMove[i]+shift);
 				toMove[i]= toMove[i]+shift;
 			}
 		}
 		else 
 			return;
 		
 		//FIXME bisognerebbe controllare che l'elemento col focus sia davvero un alternativa e in caso di errore sollevare un eccezione
 		
 		
 		comp.setAlternative(listaAlternative);
 		popolaProperties((ComponenteAlternative)focused);
 		list_alternative.setSelectedIndices(toMove);
 	}
 
 	private static void popolaProperties(ComponenteComposto selected){
 		//TODO il list_composite non ha le scrollbar
 		//TODO aggiungere le iconcine in parte ai nomi
 		//TODO disabilitare l'add existing quando non esistono elementi da aggiungere
 		
 		if(list_composite != null && panel_composite!=null)
 			panel_composite.remove(list_composite);
 
 		setGenerici(selected,"Composite");
 		
 		setContentLayout(PANEL_CMP);
 			
 		list_composite = new JList(Utils.extractNomiComponenti(selected.getComponenti()));
 		list_composite.setBounds(12, 25, 408, 132);
 		
 		panel_composite.add(list_composite);
 	
 		Utils.buttonDeleteMgmt(list_composite,button_deleteFromComp);
 
 		list_addFocusList(list_composite);
 		
 		panel_composite.repaint();
 	}
 	
 	
 	
 	private static void list_addFocusList(JList list){
 		list.addListSelectionListener(new ListSelectionListener() {
 			
 
 			@Override
 			public void valueChanged(ListSelectionEvent arg0) {
 				if(arg0.getSource()==list_composite)
 					Utils.buttonDeleteMgmt(list_composite,button_deleteFromComp);
 				else if (arg0.getSource()==list_alternative){
 					Utils.buttonDeleteMgmt(list_alternative,button_delFromAlt);
 					buttonUpDownMgmt();
 				}
 				
 			}
 		});
 	}
 	
 
 
 	
 	public static void setContentLayout(String panel){
 		CardLayout cl = (CardLayout)(content_panel.getLayout());
         cl.show(content_panel, panel);
 	}
 	
 	public static void removeElementFromComposto(int[] daRimuovere){
 		int i;
 		//ciclo for al contrario: rimuovere gli elementi dall'ultimo a scendere altrimenti va in crash
 		if(focused.getType()== ComponenteComposto.COMPOSTOTYPE){
 			for(i=daRimuovere.length-1; i>=0; i--){
 				((ComponenteComposto)focused).cancellaComponenteS(daRimuovere[i]);
 			}
 			popolaProperties((ComponenteComposto)focused);
 		}
 		else if (focused.getType()== ComponenteAlternative.ALTERNATIVETYPE){
 			for(i=daRimuovere.length-1; i>=0; i--){
 				((ComponenteAlternative)focused).cancellaAlternativa(daRimuovere[i]);
 			}
 			popolaProperties((ComponenteAlternative)focused);
 		}
 		
 		//FIXME sarebbe meglio fare anche un controllo sul nome e non solo sul numero di indice
 		//TODO tenere traccia della rimorzione
 	}
 
 
 
 	 //metodo per popolare oggetti per farci prove
 	 private void popolaOggetti()	{ data.setImg(new Immagine("immagineeee", "immagini!",
 	 1,0,"/questo/e/un/path")); data.getImg().setNome("immagineeee");
 	 data.getImg().setCategoria("immagini!"); data.getImg().setPath("/questo/e/un/path");
 	 data.getImg().setVisibilita(2); data.getImg().setEnfasi(0);
 	 data.setLnk(new Link("", "!", 0,0,"", "")); data.getLnk().setNome("linkozzo");
 	 data.getLnk().setCategoria("altra"); data.getLnk().setVisibilita(1);
 	 data.getLnk().setUri("www.url.it"); data.getLnk().setTesto("clicca qui"); data.getLnk().setEnfasi(2);
 	 data.setTxt(new Testo("", "", 0, 0, "")); data.getTxt().setNome("testo");
 	 data.getTxt().setCategoria("testi!"); data.getTxt().setVisibilita(0); data.getTxt().setEnfasi(1);
 	 data.getTxt().setTesto("scriviamoci tanta roba");
 	 data.setAlt(new ComponenteAlternative("alternativa", "alterniamoci", 2, 0));
 	 data.setCmp(new ComponenteComposto("Compostato", "compi", 1, 1));
 	 data.getCmp().aggiungiComponenteS(data.getImg()); data.getCmp().aggiungiComponenteS(data.getLnk());
 	 data.getCmp().aggiungiComponenteS(data.getTxt());
 	 }
 	 
 
 	// TODO agganciare i metodi setfocus al click nelle foglie sull'albero...
 
 	// metodo per togliere il focus all'oggetto precedente
 	private static void unFocus() {
 		focused = null;
 		focusedTxt = null;
 		focusedImg = null;
 		focusedLnk = null;
 		focusedCmp = null;
 		focusedAlt = null;
 	}
 
 	public static void setFocus(Componente selected){
 		if (selected.getType()==Testo.TEXTTYPE)
 			setFocus((Testo)selected);
 		else if (selected.getType()==Immagine.IMAGETYPE)
 			setFocus((Immagine)selected);
 		else if (selected.getType()==ComponenteComposto.COMPOSTOTYPE)
 			setFocus((ComponenteComposto)selected);
 		else if (selected.getType()==ComponenteAlternative.ALTERNATIVETYPE)
 			setFocus((ComponenteAlternative)selected);
 		else if (selected.getType()==Link.LINKTYPE)
 			setFocus((Link)selected);
 	}
 	
 	private static void setFocus(Immagine selected) {
 		unFocus();
 		focusedImg = selected;
 		setFocusGeneric(selected);
 		popolaProperties(selected);
 
 	}
 
 	private static void setFocus(Testo selected) {
 		unFocus();
 		focusedTxt = selected;
 		setFocusGeneric(selected);
 		popolaProperties(selected);
 
 	}
 
 	private static void setFocus(Link selected) {
 		unFocus();
 		focusedLnk = selected;
 		setFocusGeneric(selected);
 		popolaProperties(selected);
 
 	}
 
 	private static void setFocus(ComponenteComposto selected) {
 		unFocus();
 		focusedCmp = selected;
 		setFocusGeneric(selected);
 		popolaProperties(selected);
 		// TODO implementare parte specifica
 
 	}
 
 	private static void setFocus(ComponenteAlternative selected) {
 		unFocus();
 		focusedAlt = selected;
 		setFocusGeneric(selected);
 		popolaProperties(selected);
 		// TODO implementare parte specifica
 
 	}
 
 	private static void setFocusGeneric(Componente comp) {
 		focused = comp;
 	}
 
 	// TODO verificare se va
 	private void setFocusedName() {
 		if (focused != null)
 			focused.setNome(textField_Name.getText());
 	}
 
 	private void setFocusedCategory() {
 		if (focused != null)
 			focused.setCategoria(textField_Category.getText());
 	}
 
 	private void setFocusedEmph() {
 		if (focused != null)
 			focused.setEnfasi(comboBox_Emphasize.getSelectedIndex());//
 	}
 
 	private void setFocusedImpo() {
 		if (focused != null)
 			focused.setVisibilita(comboBox_Importance.getSelectedIndex());// focused.setVisibilita
 	}
 
 	private void updateTextContent() {
 		if (focusedTxt != null)
 			focusedTxt.setTesto(editorPane_text.getText());
 		// TODO finire updatecontent per i vari tipi di oggetto
 
 	}
 	
 	private boolean checkLinkText(){
 		//TODO bisognerebbe controllare i caratteri da escapare
 		if(textField_linktext.getText().trim().length()!=0){
 			textField_linktext.setToolTipText("Text of the link"); //TODO tooltip inutile al 100%
 			erroreTestoLink.setVisible(false);
 			return true;
 		}
 		else {
 			erroreTestoLink.setVisible(true); 
 			textField_linktext.setToolTipText("Link text can't be empty");
 		}
 		return false;
 	}
 	
 	private void updateLinkText(){
 		if(focusedLnk!= null)
 
 			focusedLnk.setTesto(textField_linktext.getText());
 		checkLinkText();
 			//TODO sarebbe bello sollevare un'eccezione ogni volta che questo if non si verifica (e anche per tutti gli altri metodi di update)
 		}
 	
 	private void updateLinkUrl(){
 		if(focusedLnk!= null)
 			focusedLnk.setUri(textField_url.getText());
 		checkLinkUrl();
 
 	}
 	
 	private boolean checkLinkUrl(){
 		if(isUrlCorrect(textField_url.getText())){
 			erroreUrl.setVisible(false);
 			textField_url.setToolTipText("URL of the link");
 			return true;
 		}
 		else {
 			erroreUrl.setVisible(true);
 			textField_url.setToolTipText("The URL is not correct");
 		}
 		return false;
 	}
 	
 	private boolean isUrlCorrect(String text) {
 		//TODO serve una regex per controllare le url!
 		Matcher urlMatcher = URL_PATTERN.matcher(text);
         if (!urlMatcher.matches()){
             return false;}
 		return true;
 	}
 
 
 	private void updateImagePath() {
 		// TODO Evitare di salvare se il path e' errato? Se si sostituire il metodo con il commento qua sotto
 		/* if(focusedImg!= null && checkImagePath())
 		 *	 focusedImg.setPath(textField_imagepath.getText());
 		 */
 		if(focusedImg!= null)
 
 			focusedImg.setPath(textField_imagepath.getText());
 
 		checkImagePath();
 	}
 
 	private boolean checkImagePath() {
 		if(isPathCorrect(textField_imagepath.getText())){
 			errorePath.setVisible(false);
 			textField_imagepath.setToolTipText("Path of the image file");
 			return true;
 		}
 		else {
 			errorePath.setVisible(true);
 			textField_imagepath.setToolTipText("The file doesn't exist or is not readable");
 		}
 		return false;
 	}
 	
 	
 	static void chooseFile(int chooserValue, JFileChooser fc, JTextField target){
 		//TODO settare le cartelle di default
 		if (chooserValue == JFileChooser.APPROVE_OPTION) {
             target.setText(fc.getSelectedFile().getAbsolutePath());
         } 
 	}
 	
 	static String chooseFile(int chooserValue, JFileChooser fc){
 		//TODO settare le cartelle di default
 		String output = "";
 		if (chooserValue == JFileChooser.APPROVE_OPTION) {
 			output = fc.getSelectedFile().getAbsolutePath();
         } 
 		return output;
 	}
 	
 	public static boolean isPathCorrect(String path){
 		//TODO fare prove con files e dir verificare che funzioni
 		File daControllare = new File(path);
 		if(daControllare.isFile() && daControllare.canRead())
 			return true;
 		return false;
 	}
 	
 	private void boldify(JButton button) {
 		Font newButtonFont = new Font(button.getFont().getName(), Font.ITALIC
 				+ Font.BOLD, button.getFont().getSize() + 1);
 		button.setFont(newButtonFont);
 	}
 	
 	public static String fileChooser(int i){
 		JFileChooser filec=buildFileChooser(i);
 		if(filec==null)
 			return "";
 		if (filec.getSelectedFile()==null){
 			return "";
 		}
 		return filec.getSelectedFile().getAbsolutePath();
 	}
 	
 	private void addElementToComposite(ComponenteSemplice componente) {
 		
 		int[] selected = list_composite.getSelectedIndices();
 		((ComponenteComposto)focusedCmp).aggiungiComponenteS(componente);
 		popolaProperties(focusedCmp);
 		list_composite.setSelectedIndices(selected);
 		Utils.buttonDeleteMgmt(list_composite,button_deleteFromComp);
 		
 		
 	}
 	
 	private void addElementToAlternative(ComponenteSemplice componente) {
 		int[] selected = list_alternative.getSelectedIndices();
 		((ComponenteAlternative)focusedAlt).aggiungiAlternativa(componente);
 		popolaProperties(focusedAlt);
 		list_alternative.setSelectedIndices(selected);
 		Utils.buttonDeleteMgmt(list_alternative,button_delFromAlt);
 		buttonUpDownMgmt();
 	}
 	
 	
 	
 	
 	
 	private static JFileChooser buildFileChooser (int i){
 		JFileChooser fileChooser=null;
 		if(frameOptions != null){
 			if(i== LOADSAVE && frameOptions.getDefDirLoadSave()!= null && frameOptions.getDefDirLoadSave().length()>0)
 				fileChooser = new JFileChooser(frameOptions.getDefDirLoadSave()); 
 			else if (i == TEXT && frameOptions.getDefDirText()!= null && frameOptions.getDefDirText().length()>0)
 				fileChooser = new JFileChooser(frameOptions.getDefDirText()); 
 			else if (i == IMAGE && frameOptions.getDefDirImage()!= null && frameOptions.getDefDirImage().length()>0)
 				fileChooser = new JFileChooser(frameOptions.getDefDirImage()); 
 			else {
 				return null;
 			}
 		}
 		else
 			fileChooser = new JFileChooser();
 		chooseFile(fileChooser.showOpenDialog(contentPane), fileChooser); 
 		return fileChooser;
 
 	}
 	
 	public static File getFileFromChooser(int i){
 		return buildFileChooser(i).getSelectedFile();
 	}
 	
 	private void fileChooser(int i, JTextField target){
 		String path;
 		path=fileChooser(i);
 		if (path!= null && path.length()>0)
 			target.setText(path);
 	}
 	
 	private void setChangeListener (JTextComponent toAttachListener){
 		
 		JTextComponent textComponent_imagepath=toAttachListener;
 		textComponent_imagepath.getDocument().addDocumentListener(new DocumentListener() {
 			public void changedUpdate(DocumentEvent e) {
 				
 			}
 			public void removeUpdate(DocumentEvent e) {
 				
 			}
 			public void insertUpdate(DocumentEvent e) {
 				
 			}
 			});
 		
 		
 	}
 	
 
 
 }
