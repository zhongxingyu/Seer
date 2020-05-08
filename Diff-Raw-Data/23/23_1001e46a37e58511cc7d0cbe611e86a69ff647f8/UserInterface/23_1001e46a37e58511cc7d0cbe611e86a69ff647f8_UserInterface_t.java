 package pgk_Game;
 
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemListener;
 //import java.awt.event.FocusListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.ImageObserver;
 import java.net.URL;
 import java.util.HashMap;
 
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.event.MenuKeyListener;
 
 import java.awt.Image;
 import javax.imageio.ImageIO;
 
 import pkg_Command.Parser;
 
 import java.awt.Graphics;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * This class implements a simple graphical user interface with a text entry
  * area, a text output area and an optional image.
  * 
  * @author Ngocky & Thibault
  * @version 0.00001
  */
 public class UserInterface implements ActionListener 
 {
 	private GameEngine engine;
 	private JFrame myFrame;
 	private JTextField entryField;
 	private JScrollPane listScroller;
 	private HashMap<String, JButton> buttons;
 	private HashMap<String, JPanel>  panels;
 	private HashMap<String, JMenuItem> menus;
 	private JComboBox liste;
 	private JTextArea log;
 	private JLabel image;
 	private Parser parser;
 	private Graphics g;
 
 	/**
 	 * Construct a UserInterface. As a parameter, a Game Engine (an object
 	 * processing and executing the game commands) is needed.
 	 * 
 	 * @param gameEngine
 	 *            The GameEngine object implementing the game logic.
 	 */
 	public UserInterface(GameEngine gameEngine) 
 	{
 		engine = gameEngine;
 		createGUI();
 		colorButton();
 	}
 
 	/**
 	 * Actionlistener interface for entry textfield.
 	 */
 	public void actionPerformed(ActionEvent e) 
 	{
 		
 		if (e.getSource() == buttons.get("bouton1")) 
 		{
 			engine.interpretCommand("go");
 		} 
 		else if (e.getSource() == buttons.get("bouton2")) 
 		{
 			engine.interpretCommand("go nord");
 		} 
 		else if (e.getSource() == buttons.get("bouton3")) 
 		{
 			engine.interpretCommand("go monter");
 		} 
 		else if (e.getSource() == buttons.get("bouton4")) 
 		{
 			engine.interpretCommand("go ouest");
 		}
 		// else if(e.getSource() == bouton5)
 		// {
 		// engine.interpretCommand();
 		// }
 		else if (e.getSource() == buttons.get("bouton6")) 
 		{
 			engine.interpretCommand("go est");
 		} 
 		else if (e.getSource() == buttons.get("bouton7")) 
 		{
 			engine.interpretCommand("back");
 		} 
 		else if (e.getSource() == buttons.get("bouton8")) 
 		{
 			engine.interpretCommand("go sud");
 		}
 		 else if(e.getSource() == buttons.get("bouton9"))
 		 {
 			 createGameOver("sante");
 		 }
 		else if (e.getSource() == buttons.get("bouton10")) 
 		{
 			engine.interpretCommand("drink potion");
 		} 
 		else if (e.getSource() == buttons.get("bouton11")) 
 		{
 			engine.interpretCommand("drink soin");
 		} 
 		else if (e.getSource() == buttons.get("bouton12")) 
 		{
 			engine.interpretCommand("look");
 		} 
 		else if (e.getSource() == buttons.get("bouton13")) 
 		{
 			engine.printInventaire();
 		} 
 		else if (e.getSource() == menus.get("newAction")) 
 		{
 			engine.newGame();
 		}
 		else if (e.getSource() == menus.get("quitAction")) 
 		{
 			engine.interpretCommand("quit");
 		}
 		else if (e.getSource() == menus.get("helpAction")) 
 		{
 			createHelp();
 		}
 		else if (e.getSource() == menus.get("authorAction")) 
 		{
 			createCredits();
 		}
 		else if (e.getSource() == menus.get("copyrightAction")) 
 		{
 			createCopyright();
 		}
 		else if (e.getSource() == buttons.get("boutonParler")) 
 		{
 			engine.interpretCommand("parler");
 		}
 		else if (e.getSource() == buttons.get("boutonAttaque")) 
 		{
 			engine.interpretCommand("attaque");
 		}
 		else if (e.getSource() == buttons.get("boutonNext")) 
 		{
 			engine.interpretCommand("parler");
 		}
 		else if (e.getSource() == buttons.get("boutonHelp")) 
 		{
 			engine.interpretCommand("parler");
 		}
 		else if (e.getSource() == buttons.get("boutonNotHelp")) 
 		{
 			createGameOver("creeper not help");
 		}
 
 		else
 			processCommand();
 		
 		//e.getActionCommand(String)
 	}
 
 	public void closeDialogue()
 	{
 		myFrame.remove(panels.get("panelDialogue"));
 		listScroller = new JScrollPane(log);
 		log.setFont(new Font("Verdana", Font.LAYOUT_LEFT_TO_RIGHT, 13));
 		listScroller.setPreferredSize(new Dimension(600, 280));
 		listScroller.setMinimumSize(new Dimension(10, 10));
 		panels.get("sspanel2").add(listScroller, BorderLayout.NORTH);
 		panels.get("sspanel2").add(entryField, BorderLayout.SOUTH);
 		panels.get("panel2").add(panels.get("sspanel1"));
 		panels.get("panel2").add(panels.get("sspanel2"));
 		panels.get("panel2").add(panels.get("sspanel3"));
 		myFrame.add(panels.get("panel2"));
 		resetTextPanel();
 	}
 
 	/**Cette méthode permet de désactiver les boutons quand la sortie correspondante n'est pas disponible et
 	 * la réactive dans le cas contraire
 	 */
 	public void colorButton()
 	{
 		if(engine.getPlayer().getRoom().getExit("nord") == null)
 			buttons.get("bouton2").setEnabled(false);
 		else
 			buttons.get("bouton2").setEnabled(true);
 		
 		if(engine.getPlayer().getRoom().getExit("haut") == null)
 			buttons.get("bouton3").setEnabled(false);
 		else
 			buttons.get("bouton3").setEnabled(true);
 		
 		if(engine.getPlayer().getRoom().getExit("ouest") == null)
 			buttons.get("bouton4").setEnabled(false);
 		else
 			buttons.get("bouton4").setEnabled(true);
 		
 		if(engine.getPlayer().getRoom().getExit("est") == null)
 			buttons.get("bouton6").setEnabled(false);
 		else
 			buttons.get("bouton6").setEnabled(true);
 		
 		if(engine.getPlayer().getRoom().getExit("sud") == null)
 			buttons.get("bouton8").setEnabled(false);
 		else
 			buttons.get("bouton8").setEnabled(true);
 		
 		panels.get("panel2").repaint();
 	}
 
 	/**Créer la fenêtre Copyright
 	 * 
 	 */
 	public void createCopyright()
 	{
 		JFrame gpl = new JFrame("Copyright");
 		gpl.setSize(800, 1000);
 	    gpl.setVisible(true);
 	    
 	    JEditorPane editorPane = new JEditorPane();
         editorPane.setEditable(false);
         java.net.URL helpURL = this.getClass().getClassLoader().getResource("TextSamplerDemoHelp.html");
         if (helpURL != null) {
             try {
                 editorPane.setPage(helpURL);
             } catch (IOException e) {
                 System.err.println("Attempted to read a bad URL: " + helpURL);
             }
         } else {
             System.err.println("Couldn't find file: TextSampleDemoHelp.html");
         }
         
         JScrollPane editorScrollPane = new JScrollPane(editorPane);
         editorScrollPane.setVerticalScrollBarPolicy(
                         JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
         editorScrollPane.setPreferredSize(new Dimension(250, 145));
         editorScrollPane.setMinimumSize(new Dimension(10, 10));
         editorPane.setEditable(false);
         
         gpl.setContentPane(editorScrollPane);
 	}
 
 	/**Créer la fenêtre About Water Games
 	 * 
 	 */
 	public void createCredits()
 	{
 		JFrame credits = new JFrame("About Water Games");
 		credits.setSize(800, 1000);
 	    credits.setVisible(true);
 	    
 	    JEditorPane editorPane = new JEditorPane();
         editorPane.setEditable(false);
         java.net.URL helpURL = this.getClass().getClassLoader().getResource("TextSamplerDemoHelp.html");
         if (helpURL != null) {
             try {
                 editorPane.setPage(helpURL);
             } catch (IOException e) {
                 System.err.println("Attempted to read a bad URL: " + helpURL);
             }
         } else {
             System.err.println("Couldn't find file: TextSampleDemoHelp.html");
         }
         
         JScrollPane editorScrollPane = new JScrollPane(editorPane);
         editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
         editorScrollPane.setPreferredSize(new Dimension(250, 145));
         editorScrollPane.setMinimumSize(new Dimension(10, 10));
         editorPane.setEditable(false);
         
         credits.setContentPane(editorScrollPane);
 	}
 
 	// public void eat()
 	// {
 	// System.out.println("Tu as déjà mangé, tu n'as plus faim");
 	// }
 
 	/**Cette méthode crée la fenêtre quand le joueur a perdu. L'image montré est choisie en fonction de la raison
 	 * pour laquelle le joueur est mort
 	 * @param raison
 	 */
 	 public void createGameOver(String raison)
 	 {
 		 myFrame.remove(panels.get("panel2"));
 		 if(panels.get("panelDialogue2") != null)
 			{
 				myFrame.remove(panels.get("panelDialogue2"));
 			}
 		 
 		 switch(raison)
 		 {
 		  	case "eau" : 
 			{
 				  showImage("images/Mortnoye.png");
 				  break;
 			}
 		  	case "sante" :
 		  	{
 		  		showImage("images/gameover.jpg");
 		  		break;
 		  	}
 		  	case "creeper" :
 		  	{
 		  		showImage("images/Mortcreeper.png");
 		  		break;
 		  	}
 		  	case "creeper not help" :
 		  	{
 		  		showImage("images/Mortcreepersansaide.png");
 		  		break;
 		  	}
 		  	case "enderman" :
 		  	{
 		  		showImage("images/Mortenderman.png");
 		  		break;
 		  	}
 		  	case "blaze" :
 		  	{
 		  		showImage("images/Mort-bllaze.png");
 		  		break;
 		  	}
 		  	default:
 		  		showImage("images/gameover.jpg");
 		 }
 	 }
 
 	/**
 	 * Gérer User Interface La fenêtre du jeux comporte 2 panneaux, celui en
 	 * haut sert à afficher l'image Le panneau en bas comporte 3 sous-panneaux,
 	 * dont le panneau des boutons de directions, la partie texte et le panneau
 	 * des boutons d'options
 	 */
 	public void createGUI() 
 	{
 		
 		buttons = new HashMap<String, JButton>();
 		panels = new HashMap<String, JPanel>();
 		menus = new HashMap<String, JMenuItem>();
 		
 		/*************************** Créer une nouvelle fenêtre *******************************/
 		myFrame = new JFrame("Water Game");
 		myFrame.setResizable(true);
 		
 		/***************************** Créer un menu bar **************************************/
 		JMenuBar menubar = new JMenuBar();
 		myFrame.setJMenuBar(menubar);
 		// Créer les boutons du menu
 		JMenu options = new JMenu("Options");
 		JMenu credits = new JMenu("Aide");
 		menubar.add(options);
 		menubar.add(credits);
 		// Créer les boutons du dropdown menu
 		JMenuItem newAction = new JMenuItem("Nouvelle partie");
 		JMenuItem saveAction = new JMenuItem("Sauvegarder la partie");
 		JMenuItem quitAction = new JMenuItem("Quitter le jeu");
 		JMenuItem helpAction = new JMenuItem("Liste des commandes");
 		JMenuItem authorAction = new JMenuItem("About Water Games");
 		JMenuItem copyrightAction = new JMenuItem("Copyright");
 		
 		menus.put("newAction", newAction);
 		menus.put("saveAction", saveAction);
 		menus.put("quitAction", quitAction);
 		menus.put("helpAction", helpAction);
 		menus.put("authorAction", authorAction);
 		menus.put("copyrightAction", copyrightAction);
 		
 		// Ajouter ces boutons au dropdown
 		options.add(newAction);
 		options.add(saveAction);
 		options.addSeparator();
 		options.add(quitAction);
 		credits.add(helpAction);
 		credits.addSeparator();
 		credits.add(authorAction);
 		credits.add(copyrightAction);
 
 		/**************************** JComboBox ******************************************************/
 		liste = new JComboBox();
 		liste.addItem("truc");
 
 		/********************************* L'entrée de text *****************************************/
 		entryField = new JTextField(34);
 
 		/*********************************** Affichage du text du jeu *****************************/
 		log = new JTextArea();
 		log.setEditable(false);
 		// Envelopper la partie texte pour que quand on a un texte trop longue, le
 		// texte se recadre automatiquement
 		log.setLineWrap(true);
 		log.setWrapStyleWord(true);
 		log.setOpaque(false);
 		log.setFocusable(false);		
 		
 		log.setFont(new Font("Verdana", Font.LAYOUT_LEFT_TO_RIGHT, 13));
 
 		listScroller = new JScrollPane(log);
 		listScroller.setPreferredSize(new Dimension(600, 280));
 		listScroller.setMinimumSize(new Dimension(10, 10));
 
 		/**************************** Image des salles ************************************/
 		image = new JLabel();
 		image.setPreferredSize(new Dimension(1200, 600));
 
 		/********************************** Tous les boutons du jeu *******************************/
 		JButton bouton1 = new JButton();
 		bouton1.setEnabled(false);
 		JButton bouton2 = new JButton("Nord");
 		JButton bouton3 = new JButton("Haut");
 		JButton bouton4 = new JButton("Ouest");
 		JButton bouton5 = new JButton();
 		bouton5.setEnabled(false);
 		JButton bouton6 = new JButton("Est");
 		JButton bouton7 = new JButton("Back");
 		JButton bouton8 = new JButton("Sud");
 		JButton bouton9 = new JButton();
 		bouton9.setEnabled(false);
 		JButton bouton10 = new JButton("Potion");
 		JButton bouton11 = new JButton("Soin");
 		JButton bouton12 = new JButton("Regarder");
 		JButton bouton13 = new JButton("Inventaire");
 		
 		JButton buttonParler = new JButton("Parler");
 		buttonParler.setPreferredSize(new Dimension(300, 20));
 		JButton buttonAttaque = new JButton("Attaquer");
 		buttonAttaque.setPreferredSize(new Dimension(300, 20));
 		
 		JButton buttonNext = new JButton("Suivant");
 		buttonNext.setPreferredSize(new Dimension(100,300));
 		JButton buttonHelp = new JButton("Aider Creeper");
 		buttonHelp.setPreferredSize(new Dimension(300,150));
 		JButton buttonNotHelp = new JButton("Je ne veux pas l'aider");
 		buttonNotHelp.setPreferredSize(new Dimension(300,150));
 		
 		buttons.put("bouton1", bouton1);
 		buttons.put("bouton2", bouton2);
 		buttons.put("bouton3", bouton3);
 		buttons.put("bouton4", bouton4);
 		buttons.put("bouton5", bouton5);
 		buttons.put("bouton6", bouton6);
 		buttons.put("bouton7", bouton7);
 		buttons.put("bouton8", bouton8);
 		buttons.put("bouton9", bouton9);
 		buttons.put("bouton10", bouton10);
 		buttons.put("bouton11", bouton11);
 		buttons.put("bouton12", bouton12);
 		buttons.put("bouton13", bouton13);
 		buttons.put("boutonNext", buttonNext);
 		buttons.put("boutonParler", buttonParler);
 		buttons.put("boutonAttaque", buttonAttaque);
 		buttons.put("boutonHelp", buttonHelp);
 		buttons.put("boutonNotHelp", buttonNotHelp);
 		
 		/********************************* Les panels *************************************/
 		// Panel 1 contient l'image en haut de la fenêtre
 		JPanel panel = new JPanel();
 		panel.setPreferredSize(new Dimension(1200, 600));
 		panel.setLayout(new BorderLayout());
 		panel.add(image, BorderLayout.CENTER);
 
 		// Panel 2 corps qui comporte les boutons de navigation, la partie texte
 		// et les boutons de commandes
 		JPanel panel2 = new JPanel();
 		// Sous panel navigation
 		JPanel sspanel1 = new JPanel();
 		sspanel1.setPreferredSize(new Dimension(300, 300));
 		sspanel1.setLayout(new GridLayout(3, 3));
 		sspanel1.add(bouton1);
 		sspanel1.add(bouton2);
 		sspanel1.add(bouton3);
 		sspanel1.add(bouton4);
 		sspanel1.add(bouton5);
 		sspanel1.add(bouton6);
 		sspanel1.add(bouton7);
 		sspanel1.add(bouton8);
 		sspanel1.add(bouton9);
 
 //		sspanel1.add(liste);
 
 		// Sous panel texte
 		JPanel sspanel2 = new JPanel();
 		sspanel2.setLayout(new BorderLayout());
 		sspanel2.setPreferredSize(new Dimension(600, 300));
 		sspanel2.add(listScroller, BorderLayout.NORTH);
 		sspanel2.add(entryField, BorderLayout.SOUTH);
 		// Sous panel boutons
 		JPanel sspanel3 = new JPanel();
 		sspanel3.setPreferredSize(new Dimension(300, 300));
 		sspanel3.setLayout(new GridLayout(2, 2));
 		sspanel3.add(bouton10);
 		sspanel3.add(bouton11);
 		sspanel3.add(bouton12);
 		sspanel3.add(bouton13);
 
 		panel2.setLayout(new BoxLayout(panel2, BoxLayout.LINE_AXIS));
 		panel2.add(sspanel1);
 		panel2.add(sspanel2);
 		panel2.add(sspanel3);
 		
 		
 		panels.put("panel1", panel);
 		panels.put("panel2", panel2);
 		panels.put("sspanel1", sspanel1);
 		panels.put("sspanel2", sspanel2);
 		panels.put("sspanel3", sspanel3);
 		
 		JPanel panel3 = new JPanel();
 		panels.put("panel3", panel3);
 		
 		JPanel panelDialogue = new JPanel();
 		JPanel panelDialogue2 = new JPanel();
 		
 		panels.put("panelDialogue", panelDialogue);
 		panels.put("panelDialogue2", panelDialogue2);
 		
 
 //		// Panel 3 box qui contient les 2 autres panels
 //		JPanel panel3 = new JPanel();
 //		panel3.setLayout(new BoxLayout(panel3, BoxLayout.PAGE_AXIS));
 //		panel3.add(panel);
 //		panel3.add(panel2);
 
 		// Ajouter le panel box à notre fenêtre de jeu
 		myFrame.setLayout(new BorderLayout());
 		myFrame.add(panel, BorderLayout.NORTH);
 		myFrame.add(panel2, BorderLayout.SOUTH);
 
 		/****************************** add some event listeners to some components *****************************/
 		myFrame.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				killFrame();
 			}
 		});
 
 		entryField.addActionListener(this);
 		
 		bouton1.addActionListener(this);
 		bouton2.addActionListener(this);
 		bouton3.addActionListener(this);
 		bouton4.addActionListener(this);
 		bouton5.addActionListener(this);
 		bouton6.addActionListener(this);
 		bouton7.addActionListener(this);
 		bouton8.addActionListener(this);
 		bouton9.addActionListener(this);
 		bouton10.addActionListener(this);
 		bouton11.addActionListener(this);
 		bouton12.addActionListener(this);
 		bouton13.addActionListener(this);
 		
 		buttonParler.addActionListener(this);
 		buttonAttaque.addActionListener(this);
 		buttonHelp.addActionListener(this);
 		buttonNotHelp.addActionListener(this);
 		buttonNext.addActionListener(this);
 
 		quitAction.addActionListener(this);
 		newAction.addActionListener(this);
 		helpAction.addActionListener(this);
 		authorAction.addActionListener(this);
 		copyrightAction.addActionListener(this);
 		
 
 		/****************************************************************/
 		liste.getSelectedItem();
 		/******************************************************************/
 
 		myFrame.pack();
 		myFrame.setVisible(true);
 		entryField.requestFocus();
 	}
 
 	/**Créer la fenêtre des commandes disponibles
 	 * 
 	 */
 	public void createHelp()
 	{
 		JFrame help = new JFrame("Liste des commandes");
 		help.setSize(800, 1000);
 	    help.setVisible(true);
 	    
 	    JEditorPane editorPane = new JEditorPane();
         editorPane.setEditable(false);
         java.net.URL helpURL = this.getClass().getClassLoader().getResource("TextSamplerDemoHelp.html");
         if (helpURL != null) {
             try {
                 editorPane.setPage(helpURL);
             } catch (IOException e) {
                 System.err.println("Attempted to read a bad URL: " + helpURL);
             }
         } else {
             System.err.println("Couldn't find file: TextSampleDemoHelp.html");
         }
         
         JScrollPane editorScrollPane = new JScrollPane(editorPane);
         editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
         editorScrollPane.setPreferredSize(new Dimension(250, 145));
         editorScrollPane.setMinimumSize(new Dimension(10, 10));
         editorPane.setEditable(false);
         
         help.setContentPane(editorScrollPane);
 	}
 
 	/**Cette méthode crée les boutons qui permettent d'intéragir avec les bots (boutons Attaquer et Parler)
 	 * Les boutons sont retirées si aucun bot n'est présent dans l'endroit
 	 */
 	public void createInteractionBot()
 	{
 		panels.get("panel3").setLayout(new BorderLayout());
 		panels.get("panel3").add(buttons.get("boutonParler"), BorderLayout.WEST);
 		panels.get("panel3").add(buttons.get("boutonAttaque"), BorderLayout.EAST);
 		
 		//S'il y a un bot
 		if(engine.getPlayer().getRoom().getBot() != null)
 		{
 			panels.get("sspanel2").setLayout(new BorderLayout());
 				panels.get("sspanel2").removeAll();
 				panels.get("sspanel2").add(listScroller, BorderLayout.NORTH);
				panels.get("sspanel2").add(panels.get("panel3"), BorderLayout.SOUTH);
				panels.get("sspanel2").updateUI();
 		}
 		//S'il n'y a pas de bot
 		else
 		{
 			if(panels.get("panel3") != null)
 			{
 				panels.get("sspanel2").setLayout(new BorderLayout());
 				panels.get("sspanel2").removeAll();
 				panels.get("sspanel2").add(entryField, BorderLayout.SOUTH);
 				panels.get("sspanel2").add(listScroller, BorderLayout.NORTH);
				panels.get("sspanel2").updateUI();
 			}
 		}
 	}
 	 
 	 public void createWinGame()
 	 {
 		 myFrame.remove(panels.get("panel2"));
 		 if(panels.get("panelDialogue2") != null)
 			{
 				myFrame.remove(panels.get("panelDialogue2"));
 				showImage("images/victory.jpg");
 			}
 	 }
 	 
 
 	 /**
 	 * Enable or disable input in the input field.
 	 */
 	public void enable(boolean on) {
 		entryField.setEditable(on);
 		if (!on)
 			entryField.getCaret().setBlinkRate(0);
 	}
 	
 	public JFrame getFrame()
 	{
 		return myFrame;
 	}
 	
 	public JTextArea getJTextArea()
 	{
 		return log;
 	}
 	
 	public void killFrame() {
 		myFrame.setVisible(false);
 		myFrame.dispose();
 	}
 	
 	/**
 	 * Print out some text into the text area.
 	 */
 	public void print(String text) {
 		log.append(text);
 		log.setCaretPosition(log.getDocument().getLength());
 	}
 	
 	/**
 	 * Print out some text into the text area, followed by a line break.
 	 */
 	public void println(String text) {
 		log.append(text + "\n");
 		log.setCaretPosition(log.getDocument().getLength());
 	}
 	
 	/**
 	 * A command has been entered. Read the command and do whatever is necessary
 	 * to process it.
 	 */
 	public void processCommand() 
 	{
 		String input = entryField.getText();
 		entryField.setText("");
 
 		engine.interpretCommand(input);
 	}
 	
 	/**Cette méthode permet d'actualiser le panneau de texte à chaque fois qu'une commande est rentré, ainsi
 	  * on a pas le texte de tous les mouvements du joueur affiché
 	  */
 	public void resetTextPanel()
 	{
 		log.setText("");
 		print("\n");
 		engine.printLocationInfo();
 		entryField.requestFocus();
 	}
 	
 	/**Cette méthode permet de passer du jeu en mode normal en mode dialogue
 	 * c'est-à-dire enlever les boutons, afficher le texte du dialogue et les boutons pour l'avancement du dialogue
 	 * 
 	 */
 	public void showDialogue(int mode)
 	{
 		switch(mode)
 		{
 			case 1 :
 			{
 				myFrame.remove(panels.get("panel2"));
 				if(panels.get("panelDialogue2") != null)
 				{
 					myFrame.remove(panels.get("panelDialogue2"));
 				}
 				
 				
 				panels.get("panelDialogue").setLayout(new BorderLayout());
 				panels.get("panelDialogue").add(log, BorderLayout.CENTER);
 				myFrame.add(panels.get("panelDialogue"));
 				log.setText("");
 				log.setFont(new Font("Verdana", Font.BOLD, 13));
 				print("\n");
 				
 				panels.get("panelDialogue").add(buttons.get("boutonNext"), BorderLayout.EAST);
 				panels.get("panelDialogue").repaint();
 				break;
 			}
 			case 2:
 			{
 				myFrame.remove(panels.get("panelDialogue"));
 				
 				panels.get("panelDialogue2").setLayout(new BorderLayout());
 				panels.get("panelDialogue2").add(log, BorderLayout.CENTER);
 				myFrame.add(panels.get("panelDialogue2"));
 				log.setText("");
 				log.setFont(new Font("Verdana", Font.BOLD, 13));
 				print("\n");
 				
 				JPanel help = new JPanel();
 				help.setLayout(new BorderLayout());
 				help.add(buttons.get("boutonHelp"), BorderLayout.NORTH);
 				help.add(buttons.get("boutonNotHelp"), BorderLayout.SOUTH);
 				panels.get("panelDialogue2").add(help, BorderLayout.EAST);
 				panels.get("panelDialogue2").repaint();
 				break;
 			}
 			default: {}
 			
 			
 		}
 		
 	}
 	
 	/**
 	 * Afficher une image dans l'interface
 	 */
 	public void showImage(String imageName) 
 	{
 		URL imageURL = this.getClass().getClassLoader().getResource(imageName);
 		if (imageURL == null)
 			System.out.println("Image non trouvé");
 		else 
 		{
 			ImageIcon icon = new ImageIcon(imageURL);
 			image.setIcon(icon);
 			myFrame.pack();
 		}
 	}
 	
 	
 }
