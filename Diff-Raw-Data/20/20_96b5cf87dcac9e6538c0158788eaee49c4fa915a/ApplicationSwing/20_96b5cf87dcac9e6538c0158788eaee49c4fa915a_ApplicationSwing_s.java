 /*
 *******************************************************
  Historique des modifications
 *******************************************************
   2002-05-28	Cris Fuhrman : Version initiale
   
   2004-03-07	Cris Fuhrman : Intgration de SwingWorker 
                 requierant la classe additionnelle 
                 SwingWorker.java, utilisation des variables 
                 constantes, formatage de code source, 
                 organisation des imports, etc.
 
   2005-05-01	Cris Fuhrman : Intgration de ApplicationSupport
   				requierant la classe additionnelle
   				ApplicationSupport.java et les fichiers
   				prefs.properties, app_xx.properties (o xx est le
   				code de la langue, p. ex. fr = franais, en = anglais).
   				Suppression de l'interface Shape.
   				
   2006-05-03	Sbastien Adam :
   
                 Uniformisation et maintenance du code.
 
                 Ajout des classes pour la gestion des
                 items de menu. Un couteur ajout pour chaque item 
                 (DemarrerListener, ArreterListener, QuitterListener, 
                 AProposDeListener).  
                 
                 La classe ApplicationSwing n'implmente plus ActionListener. 
                 Elle dlgue la gestion des items.
                 
                 Plus besion d'un "if else if" dans la methode actionPerformed pour 
                 excuter l'action associe  un item. Le code est plus
                 simple  comprendre, lire et maintenir.	
 
  La distribution originale se trouve  
  https://cours.ele.etsmtl.ca/academique/log120/notesdecours/exemples/lab/lab1/ApplicationSwing.zip
 */
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.KeyEvent;
 import java.awt.geom.Ellipse2D;
 
 // Exceptions
 import java.lang.NumberFormatException;
 import java.net.UnknownHostException;
 import java.net.ConnectException; 
 import java.lang.ArrayIndexOutOfBoundsException;
 import java.io.IOException;
 
 import javax.swing.JOptionPane;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.KeyStroke;
 
 
 /**
  * <code>ApplicationSwing</code> est un exemple d'une
  * application en Java qui fournit une interface Swing, avec un simple
  * menu et un dessin.
  *
  * <h4>References</h4> 
  * <ul> 
  *
  * <li>C. Fuhrman, &quot;Notes de cours de LOG120,&quot; &Eacute;cole
  * de technologie sup&eacute;rieure, Montr&eacute;al, Qu&eacute;bec,
  * Canada, 2002
  *
  * <li>Xemacs (for generation of the initial template), <a target="_top" 
  * href="http://www.xemacs.org">www.xemacs.org</a>, 2002 
  *
  * <li><a target="_top" 
  * href="http://java.sun.com/docs/books/tutorial/uiswing/painting/overview.html">Overview
  * of Custom Painting</a>, une partie du tutoriel Java de Sun, 2002.
  *
  * <li>Java Software, <a target="_top" 
  * href="http://java.sun.com/j2se/javadoc/writingdoccomments/index.html">&quot;How
  * to Write Doc Comments for the Javadoc<sup>TM</sup> Tool,&quot;</a>
  * 2002
  *
  * </ul>
  *
  * Distribution originale &agrave; partir du 
  * <a target="_top" href="https://cours.ele.etsmtl.ca/academique/log120/">site Web</a>
  * du cours LOG120.
  * 
  * Created: Tue May 28 11:31:18 2002
  *
  * @author <a href="mailto:christopher.fuhrman@etsmtl.ca">Christopher Fuhrman</a>
  *
  * @version 1.1
  */
 
 public class ApplicationSwing extends JFrame {
 	
 	private boolean workerActif;
 	private JMenuItem arreterMenuItem, demarrerMenuItem;
   // The canvas is handles the ShapesBuffer and display shapes
   private ShapeCanvas canvas;
   // Client for the Shape protocol
   // This client should be multithreaded for sending and receiving
   private ClientShape client;
   // Used to allow reference to the this pointer in internal classes
   private JFrame pointer;
 	
   /*
    * Handles About event
    */
 	class AProposDeListener implements ActionListener {
 		
     public void actionPerformed(ActionEvent arg0) {
 			JOptionPane.showMessageDialog(null, ApplicationSupport
 					.getResource(MESSAGE_DIALOGUE_A_PROPOS), ApplicationSupport
 					.getResource(MENU_AIDE_PROPOS),
 					JOptionPane.INFORMATION_MESSAGE);
 		}
 
 	}
 
 	/*
    * Handles Stop requesting shapes event
    */
 	class ArreterListener implements ActionListener {
 		
     public void actionPerformed(ActionEvent arg0) {
       client.stop();
       workerActif = false;
 			rafraichirMenus();
 		}
 
 	}
 	
   /*
    * Handles Start requesting shapes event
    */
   class DemarrerListener implements ActionListener {
     
     public void actionPerformed(ActionEvent arg0) {
       boolean validServer = false;
       client = ClientShape.getInstance();
   
       while (!validServer) {
         try {
            // ask user for address
           String address = JOptionPane.showInputDialog(
              "Quel est nom d'hte et le port du serveur de formes?", "localhost:10000");
     
           if (address == null) {
             // don't like memory leaks even with a gc
             client = null;
             return;
           }
     
           String[] infos = address.split(":");
           String hostname = infos[0];
           int port = Integer.parseInt(infos[1]);
           client.init(hostname, port);
           validServer = true;
          }
          catch (ArrayIndexOutOfBoundsException aiooe) {
           JOptionPane.showMessageDialog(pointer, "L'adresse entre est invalide. (hostname:port)");
          }
          catch (NumberFormatException nfe) {
            // parsing int error
           JOptionPane.showMessageDialog(pointer, "Le port spcifi est invalide.");
          }
          catch (UnknownHostException uhe) {
            // dns error
           JOptionPane.showMessageDialog(pointer, "Le nom d'hte spcifi est introuvable.");
          }
          catch (ConnectException ce) {
            // server not present on host
           JOptionPane.showMessageDialog(pointer, "L'hte ne rponds pas sur le port spcifi.");
          }
          catch (IOException ioe) {
            ioe.printStackTrace();
          }
          catch (Exception e) {
           JOptionPane.showMessageDialog(pointer, "Erreur gnrique (" + e.getMessage() + ").");
         }
       }
   
       client.setCanvas(canvas);
    
       // This worker listens to the socket input for incoming commands
       final SwingWorker workerReceiver = new SwingWorker() {
         public Object construct() {
           try {
             client.receive();
           }
           catch (Exception e) {
            JOptionPane.showMessageDialog(pointer, "Terminaison imprvue du serveur.");
             client.stop();
           }
           workerActif = false;
           rafraichirMenus();
           return new Integer(0);
         }
       };
   
       // This worker sends GET requests on a timer
       final SwingWorker workerSender = new SwingWorker() {
         public Object construct() {
           client.send();
           workerActif = false;
           rafraichirMenus();
           return new Integer(0);
         }
       };
    	  workerActif = true;
       // We start the receiver first because it must be ready to receive something before
       // we any request is sent!
       workerReceiver.start();
       workerSender.start();
   
    	  rafraichirMenus();
     }
   }
 	
   /*
    * Handles Quit event
    */
 	class QuitterListener implements ActionListener {
 		public void actionPerformed(ActionEvent arg0) {
 			if (workerActif) {
         client.stop();
 				try {
 					Thread.sleep(DELAI_QUITTER_MSEC);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 			System.exit(0);
 		}
 	}
 
   /*
    * Default Constructor
    */
 	public ApplicationSwing() {
     canvas = new ShapeCanvas(10);
     canvas.setPreferredSize(new Dimension(CANEVAS_LARGEUR, CANEVAS_HAUTEUR));
     getContentPane().add(new JScrollPane(canvas));
     pointer = this;
     addWindowListener(new WindowAdapter() {
       public void windowClosing(WindowEvent e)
       {
         if (workerActif) {
           client.stop();
           try {
             Thread.sleep(1000);
           }
           catch (InterruptedException ie) {
             // do nothing
           }
         }
         dispose();
         System.exit(0);
       }
     });
     init();
 	}
 
   /*
    * Initialises the menu, filling it with submenus and actions
    */
   private void init() {
 		creerMenuFichier();
 		creerMenuDessiner();
 		creerMenuAide();
 		rafraichirMenus();
 		setLocationRelativeTo(null);
   }
 
   /*
    * Creates the Draw menu
    * @return A JMenu containing the Start and Stop actions bound to their respective ActionListener
    */
 	private JMenu creerMenuDessiner() {
 		JMenu menu = ApplicationSupport.addMenu(this, MENU_DESSIN_TITRE,
 				new String[] { MENU_DESSIN_DEMARRER, MENU_DESSIN_ARRETER });
 
 		demarrerMenuItem = menu.getItem(0);
 		demarrerMenuItem.addActionListener(new DemarrerListener());
 		demarrerMenuItem.setAccelerator(KeyStroke.getKeyStroke(
 				MENU_DESSIN_DEMARRER_TOUCHE_RACC,
 				MENU_DESSIN_DEMARRER_TOUCHE_MASK));
 
 		arreterMenuItem = menu.getItem(1);
 		arreterMenuItem.addActionListener(new ArreterListener());
 		arreterMenuItem.setAccelerator(KeyStroke.getKeyStroke(
 				MENU_DESSIN_ARRETER_TOUCHE_RACC,
 				MENU_DESSIN_ARRETER_TOUCHE_MASK));
 
 		return menu;
 	}
 
   /*
    * Creates the File menu
    * @return A JMenu containing the Quit action bound to an ActionListener
    */
 	private JMenu creerMenuFichier() {
 		JMenu menu = ApplicationSupport.addMenu(this, MENU_FICHIER_TITRE,
 				new String[] { MENU_FICHIER_QUITTER });
 
 		menu.getItem(0).addActionListener(new QuitterListener());
 		menu.getItem(0).setAccelerator(
 				KeyStroke.getKeyStroke(MENU_FICHIER_QUITTER_TOUCHE_RACC,
 						MENU_FICHIER_QUITTER_TOUCHE_MASK));
 
 		return menu;
 	}
 
 	/*
    * Creates the Help menu
    * @return A JMenu containing the About action bound to an ActionListener
    */
 	private JMenu creerMenuAide() {
 		JMenu menu = ApplicationSupport.addMenu(this, MENU_AIDE_TITRE,
 				new String[] { MENU_AIDE_PROPOS });
 
 		menu.getItem(0).addActionListener(new AProposDeListener());
 
 		return menu;
 	}
 
   /*
    * Refresh the menu selection to reflect the current operation state
    */
 	private void rafraichirMenus() {
 		demarrerMenuItem.setEnabled(!workerActif);
 		arreterMenuItem.setEnabled(workerActif);
 	}
 	
 
   /*
    * Constants
    */
   private static final int CANEVAS_HAUTEUR = 500;                                     
   private static final int CANEVAS_LARGEUR = 500;                                     
   private static final int DELAI_ENTRE_FORMES_MSEC = 1000;                            
   private static final int DELAI_QUITTER_MSEC = 200;                                  
   private static final int FORME_MAX_HAUTEUR = 200;                                   
   private static final int FORME_MAX_LARGEUR = 200;                                   
   private static final int MARGE_H = 50;                                              
   private static final int MARGE_V = 60;                                              
   private static final int MENU_DESSIN_ARRETER_TOUCHE_MASK = ActionEvent.CTRL_MASK;   
   private static final char MENU_DESSIN_ARRETER_TOUCHE_RACC = KeyEvent.VK_A;          
   private static final int MENU_DESSIN_DEMARRER_TOUCHE_MASK = ActionEvent.CTRL_MASK;  
   private static final char MENU_DESSIN_DEMARRER_TOUCHE_RACC = KeyEvent.VK_D;         
   private static final int MENU_FICHIER_QUITTER_TOUCHE_MASK = ActionEvent.CTRL_MASK;  
   private static final char MENU_FICHIER_QUITTER_TOUCHE_RACC = KeyEvent.VK_Q;         
   private static final String                                                         
   		MENU_FICHIER_TITRE = "app.frame.menus.file.title",                              
   		MENU_FICHIER_QUITTER = "app.frame.menus.file.exit",                             
   		MENU_DESSIN_TITRE = "app.frame.menus.draw.title",                               
   		MENU_DESSIN_DEMARRER = "app.frame.menus.draw.start",                            
   		MENU_DESSIN_ARRETER = "app.frame.menus.draw.stop",                              
   		MENU_AIDE_TITRE = "app.frame.menus.help.title",                                 
   		MENU_AIDE_PROPOS = "app.frame.menus.help.about";                                
   private static final String MESSAGE_DIALOGUE_A_PROPOS = "app.frame.dialog.about";   
   private static final int NOMBRE_DE_FORMES = 150;                                    
   private static final long serialVersionUID = 1L;                                    
  
 	public static void main(String[] args) {
 		ApplicationSwing frame = new ApplicationSwing();
 
 		ApplicationSupport.launch(frame, ApplicationSupport
 				.getResource("app.frame.title"), 0, 0, CANEVAS_LARGEUR
 				+ MARGE_H, CANEVAS_HAUTEUR + MARGE_V);
 	}
 }
