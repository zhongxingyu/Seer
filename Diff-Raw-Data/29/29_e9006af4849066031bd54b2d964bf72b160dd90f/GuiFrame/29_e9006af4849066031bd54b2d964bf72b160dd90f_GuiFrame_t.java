 package fr.Lala2012;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JColorChooser;
 import javax.swing.JComboBox;
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.UIManager;
 
 import fr.Lala2012.GuiButton.ButtonType;
 
 
 public class GuiFrame extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 
 	private JPanel panelMain = new JPanel();
 
 	private JTabbedPane panelContent = new JTabbedPane();
 
 	private JPanel panelView = new JPanel();
 	private JEditorPane view = new JEditorPane();
 	private JScrollPane scrollView = new JScrollPane(view);
 
 	private JPanel panelRedac = new JPanel();
 	private static JTextArea ta = new JTextArea("", 30, 85);
 	private JScrollPane scrollRedac = new JScrollPane(ta);
 
 	private JPanel panelToolBar = new JPanel();
 	
 	private JPanel panelContentToolBar = new JPanel();
  	private GuiButton btnQuit = new GuiButton("Quitter", ButtonType.ERROR);
  	private JLabel insertionLabel = new JLabel("Formatter le texte : ");
 	private GuiButton btnInsert = new GuiButton("Insrer", ButtonType.DEFAULT);
  	private GuiButton btnUpdate = new GuiButton("Actualiser", ButtonType.SUBMIT);
 	 String[] tab = {"Couleur", "Gras", "Italique", "Titre 1", "Titre 2", "Titre 3", "Titre 4", "Titre 5", "Titre 6", "Citation", "Paragraphe", "Centr", "Taille", "Image", "E-m@il", "Lien", "Youtube", "Vido", "Code"};
  	private JComboBox comboFormat = new JComboBox(tab);
  	private GridLayout layout = new GridLayout(1, 5);
 	
 
 
 
 	private JMenuBar menuBar = new JMenuBar();
 	private JMenu fichier = new JMenu("Fichier");
 	private JMenu edition = new JMenu("Edition");
 
 	private JMenuItem ouvrir = new JMenuItem("Ouvrir");
 	private JMenuItem quitter = new JMenuItem("Fermer");
 	private JMenuItem save = new JMenuItem("Sauvegarder");
 	private JMenuItem saveAs = new JMenuItem("Sauvegarder sous");
 
 	public GuiFrame() {
 	    try
 	    {
 	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 	    }
 	    catch(Exception e) {}
 	    
 	    
 		ImageIcon imgIcon = null;
 		try {
 			imgIcon = new ImageIcon(ImageIO.read(GuiFrame.class.getResourceAsStream("res/icon.png")));
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		
 		
 		ImageIcon imgUpdate = null;
 		
 		try {
 			imgUpdate = new ImageIcon(ImageIO.read(GuiFrame.class.getResourceAsStream("res/update.png")));
 
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		
 		btnUpdate.setIcon(imgUpdate);
 		panelMain.setBackground(new Color(74, 109, 165));
 		panelMain.setLayout(new BorderLayout());
 
 		ImageIcon imgView = null;
 		try {
 			imgView = new ImageIcon(ImageIO.read(GuiFrame.class.getResourceAsStream("res/view.png")));
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		ImageIcon imgRedac = null;
 		try {
 			imgRedac = new ImageIcon(ImageIO.read(GuiFrame.class.getResourceAsStream("res/write.png")));
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		ta.setLineWrap(true);
 		scrollRedac
 				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		scrollRedac.setPreferredSize(new Dimension(950, 550));
 		panelRedac.setBackground(new Color(170, 170, 240));
 		panelRedac.add(scrollRedac);
 
 		view.setContentType("text/html");
 
 		view.setEditable(true);
 		scrollView
 				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		scrollView.setPreferredSize(new Dimension(950, 550));
 		scrollView.setMinimumSize(new Dimension(10, 10));
 		scrollView.setSize(500, 500);
 
 		panelView.setBackground(Color.ORANGE);
 		panelView.add(scrollView);
 
		panelContent.addTab("Aperu", imgView, panelView);
		panelContent.addTab("Rdac'", imgRedac, panelRedac);
 
 		panelMain.add(panelContent, BorderLayout.CENTER);
 
 		panelMain.add(panelToolBar, BorderLayout.SOUTH);
 
 		layout.setHgap(5);
 		layout.setVgap(1);
 		
 		panelContentToolBar.setLayout(layout);
 		panelContentToolBar.add(insertionLabel);	
 		panelContentToolBar.add(comboFormat);
 		panelContentToolBar.add(btnInsert);
 		panelContentToolBar.add(btnUpdate);
 
 		
 		panelToolBar.add(panelContentToolBar);
 		
 		
 		btnInsert.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				
 			Tag tag = null;
 			System.out.println(comboFormat.getSelectedIndex());
 			switch(comboFormat.getSelectedIndex())
 			{
 			case 0:
 				Util.performColor(ta.getSelectionStart(), ta.getSelectionEnd(), "#" + Integer.toHexString(JColorChooser.showDialog(TutoMaker.mainFrame, "Choisissez une couleur", Color.green).getRGB()).substring(2));
 				tag = Tag.COLOR;
 				break;
 			case 1:
 				tag = Tag.BOLD;
 				break;
 			case 2:
 				tag = Tag.ITALIC;
 				break;
 			case 3:
 				tag = Tag.HEADER1;
 				break;
 			case 4:
 				tag = Tag.HEADER2;
 				break;
 			case 5:
 				tag = Tag.HEADER3;
 				break;
 			case 6:
 				tag = Tag.HEADER4;
 				break;
 			case 7:
 				tag = Tag.HEADER5;
 				break;
 			case 8:
 				tag = Tag.HEADER6;
 				break;
 			case 9:
 				tag = Tag.QUOTE;
 				break;
 			case 10:
 				tag = Tag.PARAGRAPH;
 				break;
 			case 11:
 				tag = Tag.CENTER;
 				break;
 			case 12:
 				tag = Tag.SIZE;
 				String[] sexe = {"14", "16", "18", "20", "25", "30", "35", "40", "50"};
 			    String nom = (String)JOptionPane.showInputDialog(null, 
 			      "Choisissez un taille de police",
 			      "Sélectionnez un taille",
 			      JOptionPane.QUESTION_MESSAGE,
 			      null,
 			      sexe,
 			      sexe[2]);
 			    int sizeSelected = Integer.parseInt(nom);
 			    Util.performSize(ta.getSelectionStart(), ta.getSelectionEnd(), sizeSelected);
 				break;
 			case 13:
 				String imgUrl = JOptionPane.showInputDialog(null, "Saisissez l'url de l'image", "Insérer une image", JOptionPane.QUESTION_MESSAGE);
 				Util.performImage(ta.getSelectionStart(), ta.getSelectionEnd(), imgUrl);
 				
 				tag = Tag.IMG;
 			    
 				break;
 			case 14:
 				tag = Tag.EMAIL;
 				break;
 			case 15:
 				
 				String url = JOptionPane.showInputDialog(null, "Saisissez l'adresse du lien", "Insérer un lien", JOptionPane.QUESTION_MESSAGE);
 				Util.performUrl(ta.getSelectionStart(), ta.getSelectionEnd(), url);
 				
 				tag = Tag.URL;
 				break;
 			case 16:
 				tag = Tag.YOUTUBE;
 				break;
 			case 17:
 				tag = Tag.VIDEO;
 				break;
 			case 18:
 				tag = Tag.CODE;
 				break;
 			default:
 			}
 			Util.performChangeSelectedText(ta.getSelectionStart(), ta.getSelectionEnd(), tag);
 			
 			
 			}
 		});
 
 
 		quitter.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				System.exit(0);
 				
 			}
 		});
 
 		
 		// ----- Menu's Buttons -----
 		ouvrir.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				Util.openFile();
 			}
 		});
 
 		save.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				Util.saveInCurrentFile();
 			}
 		});
 
 		saveAs.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				Util.saveInNewFile();
 			}
 		});
 
 		// ----- Others Buttons -----
 		
 		btnQuit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				System.exit(0);
 			}
 		});
 		
 		btnUpdate.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				view.setText(formatRedacArea(ta.getText()));
 			}
 		});
 		
 
 
 		// On initialise nos menus
 		this.fichier.add(ouvrir);
 
 		
 		this.fichier.add(save);
 		this.fichier.add(saveAs);
 
 		// Ajout d'un séparateur
 		this.fichier.addSeparator();
 
 		this.fichier.add(quitter);
 
 		// L'ordre d'ajout va déterminer l'ordre d'apparition dans le menu de
 		// gauche à droite
 		// Le premier ajouté sera tout à gauche de la barre de menu et
 		// inversement pour le dernier
 		this.menuBar.add(fichier);
 		this.menuBar.add(edition);
 		this.setJMenuBar(menuBar);
 
 		this.setSize(1100, 700);
 		this.setContentPane(panelMain);
 		this.setIconImage(imgIcon.getImage());
 		this.setTitle("TutoMaker");
 		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
 		this.setVisible(true);
		
 
 
 
 	}
 
 	private String formatRedacArea(String forFormat) {
 		return "<html><head>    <script></script></head><body>" + Util.bbcode(forFormat)
 				+ "</body></html>";
 	}
 
 	public String getRedacText() {
 		return ta.getText();
 	}
 
 	public void setRedacText(String newText) {
 		ta.setText(newText);
 	}
 	
 	public String getSelectedText()
 	{
 		return ta.getSelectedText();
 	}
 
 }
