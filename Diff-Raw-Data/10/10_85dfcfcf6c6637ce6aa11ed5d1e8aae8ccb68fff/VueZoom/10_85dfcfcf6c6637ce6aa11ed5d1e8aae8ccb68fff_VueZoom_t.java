 
 package crazyimage;
 /******************************************************
  Cours :             LOG-121
  Session :           Automne 2012
  Groupe :            4
  Projet :            Laboratoire #2
 tudiant(e)(s) : 	Philippe Charbonneau
 				 	Patrice Robitaille
 				 	Mathieu Battah
  Code(s) perm. :    CHAP07110906
                     ROBP2002805 
                     BATM19038902 
  Professeur :        Groucho Marx
  Date cre :        2002-05-28
  Date dern. modif. : 2012-10-11
  
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
                 
   2012-09-21    Patrice Robitaille:
   
   				Ajout d'une fentre dialog qui gre le input de connexion
   				pour le serveur. Le script fait galement une validation
   				 la source et rcupre le nom du serveur ainsi que le numro
   				de port  l'aide du dlimiteur ':'.
   				
   				Ajout de 2 variables contenant le nom du serveur et le numro de port
   				 valider.
   				
   2012-09-28	Mathieu Battah
   
   				Ajout de l'utilisation de GestionForme pour grer la liste de formes.
   				
   2012-10-11	Mathieu Battah
   
   				Modification des menus pour le lab 2.
               
 
  La distribution originale se trouve  
  https://cours.ele.etsmtl.ca/academique/log120/notesdecours/exemples/lab/lab1/ApplicationSwing.zip
 ********************************************************/
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GraphicsEnvironment;
 import java.awt.Point;
 import java.awt.RenderingHints;
 
 
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 
 import javax.swing.JScrollPane;
 import javax.swing.KeyStroke;
 
 import modele.Image;
 import modele.PerspectiveModel;
 import controller.Zoom;
 
 import core.ApplicationSupport;
 
 public class VueZoom extends AbstractVue {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 4829602594348697503L;
 
 	/* - Constructeur - Crer le cadre dans lequel les formes sont dessines. */
 	public VueZoom() {
 		getContentPane().add(new JScrollPane(new CustomCanvas()));
 	}
 	
 	/**
 	 *  Crer le panneau sur lequel les formes sont dessines. 
 	 */
 	class CustomCanvas extends JPanel {
 		private static final long serialVersionUID = 1L;
 
 		public CustomCanvas() {
 			setSize(getPreferredSize());
 			setMinimumSize(getPreferredSize());
 			CustomCanvas.this.addMouseWheelListener(new Zoom("Zoom"));
 			CustomCanvas.this.setBackground(Color.white);
 		}
		
 
 		public Dimension getPreferredSize() {
 			return new Dimension(CANEVAS_LARGEUR, CANEVAS_HAUTEUR);
 		}
 
 		public void paintComponent(Graphics graphics) {
 			super.paintComponent(graphics);
 			Graphics2D g2d = (Graphics2D) graphics;
 			try{//On dessine l'image
 				g2d.drawImage(PerspectiveModel.getInstance().getImg(), PerspectiveModel.getInstance().getPosX(), 
 							  PerspectiveModel.getInstance().getPosY(), PerspectiveModel.getInstance().getWidth(), 
 							  PerspectiveModel.getInstance().getHeigth(), null);
 			}catch(Exception ex){
 				ex.getMessage();
 			}
 			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 					RenderingHints.VALUE_ANTIALIAS_ON);	
 
 		}
 	}
 
 	/* Crer le menu "Ordre". */
 	protected JMenu creerMenuOperation() {
 		JMenu menu = new JMenu(ApplicationSupport.getResource(ORDRE_TITRE));
 		menu.setMnemonic(ZOOM_RACC);
 		
 		/* Cration de JRadtioButtonMenuItem. */
 		JMenuItem zoom = new JMenuItem(new ListeOperations(ApplicationSupport.getResource(ORDRE_NOSEQASC), Ordre.NOSEQASC));
 		
 		
 		/* Ajout des raccourcis spcifiques  chaque bouton radio. */
 		zoom.addActionListener(new Zoom("Zoom"));
 		zoom.setAccelerator(KeyStroke.getKeyStroke(ZOOM_RACC, CTRL_MASK));
 		zoom.setMnemonic(ZOOM_RACC);
 
 		
 		/* Ajout des boutons radio au menu. */
 		menu.add(zoom);
 
 		return menu;
 	}
 	
 	public static void lancer(){
 
 		VueZoom zoom = new VueZoom();
 		
 		JMenuBar barreMenu = new JMenuBar();
 		barreMenu.add(zoom.creerMenuFichier());
 		barreMenu.add(zoom.creerMenuOperation());
 		barreMenu.add(zoom.creerMenuAide());
 		zoom.setJMenuBar(barreMenu);
 		Point centre = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
 
 		//Ajout de la vue comme observateur du modle.
 		PerspectiveModel.getInstance().addObserver(zoom);
 		Image.getInstance().addObserver(zoom);
 
 		/* Lancer l'application. */
 		ApplicationSupport.launch(zoom, ApplicationSupport
 				.getResource("app.frame.titleZoom"), (centre.x - (CANEVAS_LARGEUR / 2)), (centre.y - (CANEVAS_HAUTEUR / 2)), CANEVAS_LARGEUR
 				+ MARGE_H, CANEVAS_HAUTEUR + MARGE_V);
 	}
 	
 	public void update(){
 		repaint();
 		validate();
 	}
 		
 }
 
