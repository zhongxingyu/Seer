 package ihm;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.GridLayout;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JMenuBar;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.KeyStroke;
 import javax.swing.Timer;
 
 import vues.VueGenerale;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.io.IOException;
 
 import javax.swing.JLabel;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import noyau.Aeroport;
 import noyau.Chariot;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 
 import bibliotheques.SGBagFileFilter;
 
 /**
  * 
  * @author jeremy
  *
  */
 public class FenetrePrincipale extends JFrame{
 	
 	/**
 	 * Defaut serial version UID
 	 */
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Vue générale
 	 */
 	private VueGenerale vueGenerale;
 	
 	/**
 	 * Fichiers
 	 */
 	private JFileChooser jFileChooserXML = new JFileChooser();
 	
 	/**
 	 * Menu
 	 */
 	private JMenuBar menuBar = new JMenuBar();
 	private JMenu fileMenu = new JMenu("Fichier");
 	private JMenuItem menuItemOuvrir = new JMenuItem("Ouvrir");
 	private JMenu affichageMenu = new JMenu("Affichage");
 	private JMenuItem menuItemZoom100 = new JMenuItem("Zoom 100%");
 	private JMenuItem menuItemZoomArriere = new JMenuItem("Zoom +");
 	private JMenuItem menuItemZoomAvant = new JMenuItem("Zoom -");
 	private JMenuItem menuItemChristmas = new JMenuItem("Xmas style !");
 	private JMenu aideMenu = new JMenu("Aide");
 	private JMenuItem menuItemAPropos = new JMenuItem("A propos");
 	private JMenuItem menuItemQuitter = new JMenuItem("Quitter");
 
 	/** 
 	 * Panels
 	 */
 	private JPanel container = new JPanel();
 	private JPanel bandeauGeneral = new JPanel();
 	private JPanel bandeauTexteMode = new JPanel();
 	private JPanel bandeauActions = new JPanel();
 	private BandeauAjoutBagages bandeauAjoutBagages = new BandeauAjoutBagages();
 	private BandeauVitesseChariot bandeauVitesseChariot = new BandeauVitesseChariot();
 	private BandeauSortirChariot bandeauSortirChariot = new BandeauSortirChariot();
 	private JPanel panelBas = new JPanel();
 	private JPanel panelBoutons = new JPanel();
     private JPanel panelLabelInfo = new JPanel();
     
 	/**
 	 * Boutons
 	 */
 	private JButton boutonLecture = new JButton();
 	private JButton boutonArretUrgence = new JButton();
 	private JButton boutonMode = new JButton();
 	
 	/**
 	 * Label d'info
 	 */
 	private JLabel labelInfo = new JLabel("Label Info");
 	
 	/**
 	 * Label Mode en cours
 	 */
 	private JLabel labelMode = new JLabel();
 	
 	/**
 	 * Constantes
 	 */
 	String playString = "Play";
 	String pauseString = "Pause";
 	String auString = "Arret d'urgence";
 	String repriseAuString = "Reprise AU";
 	String sortirChariot = "Sortir un chariot";
 
 	/**
 	 * ImagesManager
 	 */
 	private ImagesManager imagesManager;
 	private ImagesManager christmasManager;
 	
 	/**
 	 * Enumérations
 	 */
 	private enum etatsLecture {
         PLAY, STOP
     }
 	
 	private etatsLecture etat = etatsLecture.STOP;
 	
 	/**
 	 * Clic sur Ouvrir : charge le fichier de configuration
 	 */
 	private ActionListener ouvrirListener = new ActionListener() {
 		public void actionPerformed(ActionEvent actionEvent) {
 			chargerConfiguration();
 		}
 	};
 	
 	private int setImage = 0;
 	
 	/**
 	 * Easter Egg !!!!
 	 */
 	private ActionListener eggListener = new ActionListener() {
 		public void actionPerformed(ActionEvent actionEvent) {
 			changeSkin();
 		}
 	};
 	
 	private void changeSkin(){
 		if (vueGenerale != null){
 			if (setImage == 0) {
 				vueGenerale.setImagesManager(christmasManager);
 				setImage = 1;
 			} else if (setImage == 1){
 				vueGenerale.setImagesManager(imagesManager);
 				setImage = 0;
 			}
 			vueGenerale.repaint();
 		}
 	}
 	
 	/**
 	 * Clic sur A Propos : ouvre une fenetre d'a propos
 	 */
 	private ActionListener aboutListener = new ActionListener() {
 		public void actionPerformed(ActionEvent actionEvent) {
 			aboutActionPerformed(actionEvent);
 		}
 	};
 	
 	/**
 	 * Quitte l'application
 	 */
 	private ActionListener quitterListener = new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 			System.exit(0);
 		}
 	};
 	
 	/**
 	 * Clic sur bouton play/pause
 	 */
 	private ActionListener playPauseListener = new ActionListener() {
 		public void actionPerformed(ActionEvent actionEvent) {
 			playPauseActionPerformed();
 		}
 	};
 	
 	
 	/**
 	 * Clic sur bouton du choix du mode
 	 */
 	private ActionListener modeListener = new ActionListener() {
 		public void actionPerformed(ActionEvent actionEvent) {
 			bandeauVitesseChariot.setVisible(false);
 			bandeauAjoutBagages.setVisible(false);
 			bandeauSortirChariot.setVisible(false);
 			procedureChangerMode();
 			
 		}
 	};
 	
 	/**
 	 * Procedure appelée lors du changement de mode
 	 * Elle désactive le bouton de changement pour attendre que les opérations
 	 * de calcul de chemins dans le noyau soient terminés
 	 */
 	private void procedureChangerMode() {
 		boutonMode.setEnabled(false);
 		new Thread(){
 			@Override
 			public void run() {
 				while (Aeroport.enCalcul)
 				{
 					try {
 						Thread.sleep(2000);
 					} catch (InterruptedException e) {
 						System.out.println("Erreur thread.sleep");
 						e.printStackTrace();
 					}
 				};
 				vueGenerale.changerMode();
 				boutonMode.setEnabled(true);
 				boutonMode.setText(vueGenerale.getModeBouton());
 				labelMode.setText(vueGenerale.getModeTexte());
 			}
 		}.start();
 		
 	}
 	
 	
 	/**
 	 * Clic sur Arret d'urgence
 	 */
 	private ActionListener arretUrgenceListener = new ActionListener() {
 		public void actionPerformed(ActionEvent actionEvent) {
 			vueGenerale.toggleAU();
 			if (boutonArretUrgence.getText().equals(auString))
 				boutonArretUrgence.setText(repriseAuString);
 			else
 				boutonArretUrgence.setText(auString);
 		}
 	};
 	
 	/**
 	 * Listener sur Panel général
 	 */
 	private MouseAdapter clicVueGenerale = new MouseAdapter() {
 		public void mouseClicked(MouseEvent e) {
 			clicSurVueGenerale(e);
 		}
 	};
 	
 	
 	/**
 	 * Timer du tick d'horloge
 	 */
 	private ActionListener taskPerformer = new ActionListener() {
 
         public void actionPerformed(ActionEvent evt) {
         	vueGenerale.avancerTemps();
         	vueGenerale.repaint();
         }
     };
 
     
     /**
      * Le Timer pour faire avancer la simulation
      */
     private Timer horloge = new Timer(Aeroport.lapsTemps, taskPerformer);
 
 
 	/**
 	 * Create the frame.
 	 */
 	public FenetrePrincipale() {
 		jInit(false);
 	}
 
 	private void jInit(boolean fichierCharge) {
 		
 		// 1er chargement
 		if (!fichierCharge) {
 			this.setJMenuBar(menuBar);
 			// Menu Fichier
 			menuBar.add(fileMenu);
 			menuItemOuvrir.addActionListener(ouvrirListener);
 			menuItemOuvrir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
 	                KeyEvent.CTRL_MASK));
 			fileMenu.add(menuItemOuvrir);
 			menuItemQuitter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
 	                KeyEvent.CTRL_MASK));
 			menuItemQuitter.addActionListener(quitterListener);
 			fileMenu.add(menuItemQuitter);
 			
 			// Menu Affichage
 			menuBar.add(affichageMenu);
 			menuItemChristmas.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
 	                KeyEvent.CTRL_MASK));
 			menuItemChristmas.addActionListener(eggListener);
 			affichageMenu.add(menuItemChristmas);
 			menuItemZoom100.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0,
 	                KeyEvent.CTRL_MASK));
 			affichageMenu.add(menuItemZoom100);
 			menuItemZoomArriere.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS,
 	                KeyEvent.CTRL_MASK));
 			affichageMenu.add(menuItemZoomArriere);
 			menuItemZoomAvant.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,
 	                KeyEvent.CTRL_MASK));
 			affichageMenu.add(menuItemZoomAvant);
 			
 			// Menu Aide
 			menuBar.add(aideMenu);
 			menuItemAPropos.addActionListener(aboutListener);
 			aideMenu.add(menuItemAPropos);
 			
 		}
 		
 		// Chargement des images
 		imagesManager = new ImagesManager(getToolkit(), 0);
 		christmasManager = new ImagesManager(getToolkit(), 1);
 		setIconImage(imagesManager.getImgIcon());
 					
 		// UI
 		container = new JPanel();
 		panelBas = new JPanel();
 		panelBoutons = new JPanel();
 	    boutonLecture = new JButton();
 		boutonArretUrgence = new JButton();
 		boutonMode = new JButton();
 		
 		// Frame properties
 		Dimension dimBandeau = new Dimension(this.getWidth(), 80);
 		Dimension dimPanelBas = new Dimension(this.getWidth(), 40);
 		this.setTitle("SGBag - Simulation");
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         this.setLocationRelativeTo(null);
 		this.setBounds(100, 100, 1024, 768);
 		this.setResizable(false);
 		this.setFocusable(true);
 		this.requestFocus();
 		
 		etat = etatsLecture.STOP;
 		
 		bandeauGeneral.setVisible(true);
 		bandeauGeneral.setPreferredSize(dimBandeau);
 		bandeauGeneral.setLayout(new BorderLayout());
 		
 		GridLayout gridBoutons = new GridLayout(1, 3, 5, 3);
 		panelBoutons.setLayout(gridBoutons);
 		// Bouton d'arret d'urgence
 		boutonArretUrgence.setText(auString);
 		boutonArretUrgence.addActionListener(arretUrgenceListener);
 		boutonArretUrgence.setEnabled(false);
 		panelBoutons.add(boutonArretUrgence);
 		
 		// Bouton de lecture
 		boutonLecture.setText("Play");
 		boutonLecture.addActionListener(playPauseListener);
 		boutonLecture.setEnabled(false);
 		panelBoutons.add(boutonLecture);
 		
 		// Bouton du choix du mode
 		boutonMode.setText("Mode");
 		boutonMode.addActionListener(modeListener);
 		boutonMode.setEnabled(false);
 		panelBoutons.add(boutonMode);
 		
 		labelInfo.setText("Bienvenue dans le système de gestion de bagages SGBag");
 		panelLabelInfo.add(labelInfo);
 		
 		panelBas.setLayout(new GridLayout());
 		panelBas.add(panelBoutons, BorderLayout.WEST);
 		panelBas.add(panelLabelInfo, BorderLayout.EAST);
 		panelBas.setPreferredSize(dimPanelBas);
 		
 		// Panel Parametres
 		bandeauAjoutBagages.setVisible(false);
 		bandeauVitesseChariot.setVisible(false);
 		bandeauSortirChariot.setVisible(false);
 
 		bandeauActions.add(bandeauAjoutBagages);
 		bandeauActions.add(bandeauVitesseChariot);
 		bandeauActions.add(bandeauSortirChariot);
 		
 		bandeauTexteMode.add(labelMode);
 		
 		bandeauGeneral.add(bandeauTexteMode, BorderLayout.NORTH);
 		bandeauGeneral.add(bandeauActions, BorderLayout.CENTER);
 		
 		// Ajout des panels, structuration de la fenetre
 		container.setLayout(new BorderLayout());
 		container.setBackground(Color.white);
 		container.add(panelBas, BorderLayout.SOUTH);
 		container.add(bandeauGeneral, BorderLayout.NORTH);
 		if (fichierCharge) {
 			vueGenerale.addMouseListener(clicVueGenerale);
 			container.add(vueGenerale, BorderLayout.CENTER);
 		}
 		
 		this.setContentPane(container);
 		
 	}
 	
 	/**
 	 * Clic sur A Propos
 	 * @param e : actionEvent
 	 */
 	private void aboutActionPerformed(ActionEvent ae) {
         JOptionPane.showMessageDialog(this, new FenetreAbout(), "A Propos", JOptionPane.PLAIN_MESSAGE);
     }
 	
 	/**
 	 * Appui sur bouton play/pause
 	 */
 	private void playPauseActionPerformed() {
 		if (etat == etatsLecture.PLAY) {
 			horloge.stop();
 			etat = etatsLecture.STOP;
 			boutonLecture.setText(playString);
 		} else if (etat == etatsLecture.STOP) {
 			horloge.start();
 			etat = etatsLecture.PLAY;
 			boutonLecture.setText(pauseString);
 		}
 	}
 	
 	/**
 	 * Clic sur le panel vueGenerale
 	 * @param e : mouseEvent pour récupérer la position du clic
 	 */
 	
 	private void clicSurVueGenerale(MouseEvent e) {
 		if (vueGenerale != null)
 			vueGenerale.clic(e.getX(), e.getY());
 	}
 	
 	
 	/**
 	 * 
 	 * @param vueCadreDOMElement
 	 * @return
 	 */
 	public int construireToutAPartirDeXML(Element aeroportElement)
 	{
 		// On crée l'élément Aéroport et la vue qui lui est associée
 		Aeroport aeroport = new Aeroport();
 
         if (aeroport.construireAPartirDeXML(aeroportElement) != Aeroport.PARSE_OK)
         {
             return Aeroport.PARSE_ERROR;
         }
         
         vueGenerale = new VueGenerale(bandeauAjoutBagages, 
         		bandeauVitesseChariot, bandeauSortirChariot, labelInfo, aeroport, imagesManager);
        horloge.stop();
         
         return Aeroport.PARSE_OK;
     }
 	
 	/**
 	 * Chargement de la configuration
 	 */
 	public void chargerConfiguration()
 	{
         SGBagFileFilter filter = new SGBagFileFilter();
         filter.addExtension("xml");
         filter.setDescription("Fichier XML");
         jFileChooserXML.setFileFilter(filter);
         jFileChooserXML.setFileSelectionMode(JFileChooser.FILES_ONLY);
 
         int returnVal = jFileChooserXML.showOpenDialog(null);
         if (returnVal == JFileChooser.APPROVE_OPTION) {
             try {
                 DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
                 DocumentBuilder constructeur = fabrique.newDocumentBuilder();
                 File xml = new File(jFileChooserXML.getSelectedFile().getAbsolutePath());
                 Document document = constructeur.parse(xml);
 
                 Element racine = document.getDocumentElement();
 
                 if (racine.getNodeName().equals("Aeroport"))
                 {
                 	try {
 	                	if (construireToutAPartirDeXML(racine) == Aeroport.PARSE_OK) {
 	                		jInit(true);
 	                		if (vueGenerale != null) {
 	            				vueGenerale.repaint();
 	            			}
 	                		labelInfo.setText("Bienvenue dans le système de gestion de bagages SGBag");
 	                        labelMode.setText(vueGenerale.getModeTexte());
 	                		bandeauAjoutBagages.setVueGenerale(vueGenerale);
 	                        bandeauVitesseChariot.setVueGenerale(vueGenerale);
 	                        bandeauSortirChariot.setVueGenerale(vueGenerale);
 	            	        boutonLecture.setEnabled(true);
 	            	        etat = etatsLecture.STOP;
 	            	        boutonLecture.setText(playString);
 	            	        boutonArretUrgence.setText(auString);
 	            	        boutonArretUrgence.setEnabled(true);
 	            	        boutonMode.setEnabled(true);
 	            	        boutonMode.setText(vueGenerale.getModeBouton());
 	            	        bandeauVitesseChariot.setValuesSlider(Chariot.VIT_MIN, 
 	                            	Chariot.VIT_MAX);
 	                	}
                 	}
                 	catch (Exception e) {
                 		labelInfo.setText("Erreur lors du chargement XML");
                 		
                 	}
                 }
                 
             } catch (ParserConfigurationException pce) {
                 System.out.println("Erreur de configuration du parseur DOM");
                 System.out.println("lors de l'appel a fabrique.newDocumentBuilder();");
             } catch (SAXException se) {
                 System.out.println("Erreur lors du parsing du document");
                 System.out.println("lors de l'appel a construteur.parse(xml)");
             } catch (IOException ioe) {
                 System.out.println("Erreur d'entree/sortie");
                 System.out.println("lors de l'appel a construteur.parse(xml)");
             }
         }  
 	}
 	
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					FenetrePrincipale frame = new FenetrePrincipale();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 }
