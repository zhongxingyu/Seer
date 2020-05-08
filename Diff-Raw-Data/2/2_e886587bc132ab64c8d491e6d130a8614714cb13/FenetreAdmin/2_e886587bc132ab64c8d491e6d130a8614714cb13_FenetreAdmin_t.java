 package fr.mercredymurderparty.ihm.fenetres;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.SpringLayout;
 import javax.swing.JButton;
 
 import fr.mercredymurderparty.client.CoeurClient;
 import fr.mercredymurderparty.client.Personnage;
 import fr.mercredymurderparty.ihm.composants.ComposantAlerte;
 import fr.mercredymurderparty.ihm.composants.ComposantGestionChronoGeneral;
 import fr.mercredymurderparty.ihm.composants.ComposantGestionIndices;
 import fr.mercredymurderparty.ihm.composants.ComposantGestionJoueurs;
 import fr.mercredymurderparty.ihm.composants.ComposantGestionAlerte;
 import fr.mercredymurderparty.ihm.composants.ComposantHorloge;
 import fr.mercredymurderparty.ihm.composants.ComposantParametres;
 import fr.mercredymurderparty.ihm.composants.ComposantTchat;
 import fr.mercredymurderparty.outil.BaseDeDonnees;
 import fr.mercredymurderparty.outil.FichierTXT;
 import fr.mercredymurderparty.outil.Fonctions;
 import fr.mercredymurderparty.outil.Theme;
 import fr.mercredymurderparty.outil.FiltreSelFichier;
 import fr.mercredymurderparty.serveur.CoeurServeur;
 import fr.mercredymurderparty.serveur.CompteurTempsGeneral;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JLabel;
 import java.awt.Font;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseAdapter;
 
 @SuppressWarnings("serial")
 public class FenetreAdmin extends JFrame implements Observer
 {
 	
 // ----- ATTRIBUTS ----- //
 	
 	// ----- INTERFACE GRAPHIQUE ----- //
 	private Dimension screenSize;
 	private JPanel contentPane;
 	private JButton btnArreterLaPartie;
 	private JButton boutonChargerPartie;
 	private JButton boutonNouvellePartie;
 	private JButton btnGestionDesJoueurs;
 	private JButton btnGestionDesAlerte;
 	private JButton btngestionChronoGeneral;
 	private JButton btnGestionIndices;
 	private JButton btnParamtres;
 	private boolean partieLance = false;
 	private boolean partieChargeeCree = false; // vaudra vrai si la partie est charge ou cre
 	private JLabel lblTitreComposant;
 	
 	// ----- APPEL DE CLASSES ----- //
 	private Fonctions fct;
 	private FichierTXT txt;
 	
 	// ----- MESSAGE CONFIRMATION ----- //
 	private JPanel panelConfirmation;
 	private JLabel labelMessageConfirmation;
 	private JButton boutonAccepter;
 	private JButton boutonAnnuler;
 	
 	// ----- COMPOSANTS ----- //
 	private ComposantTchat partieTchat;
 	private ComposantGestionJoueurs partieGestionJoueurs;
 	private ComposantGestionAlerte partieGestionAlerte;
 	private ComposantGestionIndices partieGestionIndices;
 	private ComposantParametres partieParametres;
 	private ComposantGestionChronoGeneral partieGestionChronoGeneral;
 	private ComposantAlerte partieAlerte;
 	
 	// ----- PARTIE CLIENT DE l'APPLICATION ----- //
 	private CoeurClient coeurClient;
 	
 	// ----- MODELE DU SERVEUR ------ //
 	private CoeurServeur coeurServeur;
 	private CompteurTempsGeneral chronoGeneral;
 	private JLabel lblAideComposant;
 	
 	// ----- IMAGE DE FOND ----- //
 	private ImagePanel panelImageTitre;
 	
 // ----- CONSTRUCTEUR ----- //
 
 	/**
 	 * Constructeur de la classe FenetreAdmin
 	 */
 	public FenetreAdmin(CoeurClient _coeurClient, CoeurServeur _coeurServeur) 
 	{
 		fct = new Fonctions();
 		txt = new FichierTXT();
 		
 		coeurClient = _coeurClient;
 		coeurServeur = _coeurServeur;
 		coeurServeur.addObserver(this);
         
 		// Afficher les composants de l'interface utilisateur
 		afficherInterface();
 	}
 	
 	
 // ----- METHODES ----- //	
 	
 	/**
 	 * Procdure qui va afficher tous les composants de l'interface utilisateur
 	 */
 	private void afficherInterface()
 	{
 		setTitle("Murder Party - Interface Administrateur");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		contentPane = new JPanel();
 		setMinimumSize(new Dimension(1024, 768));
 		screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
 		setSize(new Dimension(1024, 768));
 		setExtendedState(MAXIMIZED_BOTH);
 		setLocation((screenSize.width-this.getWidth())/2, (screenSize.height-this.getHeight())/2);
 		setContentPane(contentPane);
 		final SpringLayout slContentPane = new SpringLayout();
 		contentPane.setLayout(slContentPane);
 		
 		/**
 		 * Boutton composant paramtres
 		 */
 		btnParamtres = new JButton("Paramtres");
 		btnParamtres.addMouseListener(new MouseAdapter() 
 		{
 			@Override
 			public void mouseExited(MouseEvent e) 
 			{
 				afficherMasquerAide();
 			}
 		});
 		btnParamtres.addMouseMotionListener(new MouseMotionAdapter() 
 		{
 			@Override
 			public void mouseMoved(MouseEvent arg0) 
 			{
 				if (!unComposantOuvert())
 				{
 					String html = txt.lireFichier(fct.repertoireUtilisateur("res/aides","parametres.html"));
 					lblTitreComposant.setText("Paramtres de l'application");
 					lblAideComposant.setText(html);
 				}
 				else 
 				{
 					lblAideComposant.setText(null);
 				}
 			}
 		});
 		btnParamtres.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent arg0) 
 			{
 				// Masquer les autres composants
 				partieGestionIndices.estVisible(false);
 				partieGestionJoueurs.estVisible(false);
 				partieGestionAlerte.estVisible(false);
 				partieGestionChronoGeneral.estVisible(false);
 				
 				// Afficher le composant paramtres
 				lblTitreComposant.setText("Paramtres de l'application");
 				lblAideComposant.setText(null);
 				partieParametres.estVisible(true);
 			}
 		});
 		contentPane.add(btnParamtres);
 		
 		/**
 		 * Affichage de l'horloge
 		 */
 		ComposantHorloge horloge = new ComposantHorloge(this, slContentPane, coeurClient, true);
 		horloge.estVisible(true);
 		
 		/**
 		 * Boutton composant arreter la partie
 		 */
 		btnArreterLaPartie = new JButton("Lancer la partie");
 		slContentPane.putConstraint(SpringLayout.NORTH, btnParamtres, 5, SpringLayout.NORTH, btnArreterLaPartie);
 		slContentPane.putConstraint(SpringLayout.WEST, btnArreterLaPartie, 5, SpringLayout.WEST, contentPane);
 		
 		btnArreterLaPartie.setEnabled(false);
 		btnArreterLaPartie.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent _e) 
 			{
 				if(partieLance == true)
 				{
 					// On arrte la partie
 					partieLance = false;
 					btnArreterLaPartie.setText("Lancer la partie");
 					boutonChargerPartie.setVisible(true);
 					boutonNouvellePartie.setVisible(true);
 					coeurServeur.setPartieLancee(false);
 					coeurServeur.getTempsGlobalRestant().arreter();
 					coeurServeur.decoGenerale();
 				}
 				else
 				{
 					// On lance la partie
 					if(partieChargeeCree == true)
 					{
 						partieLance = true;
 						btnArreterLaPartie.setText("Arrter la partie");
 						boutonChargerPartie.setVisible(false);
 						boutonNouvellePartie.setVisible(false);
 						coeurServeur.setPartieLancee(true);
 						coeurServeur.getTempsGlobalRestant().demarrer();
 					}
 				}
 			}
 		});
 		contentPane.add(btnArreterLaPartie);
 		
 		/**
 		 * Boutton gestion des indices
 		 */
 		btnGestionIndices = new JButton("Indices");
 		btnGestionIndices.addMouseListener(new MouseAdapter() 
 		{
 			@Override
 			public void mouseExited(MouseEvent arg0) 
 			{
 				afficherMasquerAide();
 			}
 		});
 		btnGestionIndices.addMouseMotionListener(new MouseMotionAdapter() 
 		{
 			@Override
 			public void mouseMoved(MouseEvent arg0) 
 			{
 				if (!unComposantOuvert())
 				{
 					String html = txt.lireFichier(fct.repertoireUtilisateur("res/aides","indices.html"));
 					lblTitreComposant.setText("Gestion des Indices");
 					lblAideComposant.setText(html);
 				}
 				else 
 				{
 					lblAideComposant.setText(null);
 				}
 			}
 		});
 		slContentPane.putConstraint(SpringLayout.NORTH, btnGestionIndices, 0, SpringLayout.NORTH, contentPane);
 		btnGestionIndices.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent arg0)
 			{
 				// Masquer les autres composants
 				partieGestionJoueurs.estVisible(false);
 				partieParametres.estVisible(false);
 				partieGestionAlerte.estVisible(false);
 				partieGestionChronoGeneral.estVisible(false);
 				
 				// Affiche la fenetre ou l'on gre des indices
 				lblTitreComposant.setText("Gestion des Indices");
 				lblAideComposant.setText(null);
				//partieGestionIndices.effacerFormulaire();
 				partieGestionIndices.chargerPersonnages();
 				partieGestionIndices.rafraichirListeIndices();
 				partieGestionIndices.estVisible(true);
 			}
 		});
 		contentPane.add(btnGestionIndices);
 		btnGestionIndices.setVisible(false);
 		
 		/**
 		 * Bouton gestion du chrono general
 		 */
 		btngestionChronoGeneral = new JButton("Chronom\u00E8tre G\u00E9n\u00E9ral");
 		btngestionChronoGeneral.addMouseListener(new MouseAdapter() 
 		{
 			@Override
 			public void mouseExited(MouseEvent e) 
 			{
 				afficherMasquerAide();
 			}
 		});
 		btngestionChronoGeneral.addMouseMotionListener(new MouseMotionAdapter() 
 		{
 			@Override
 			public void mouseMoved(MouseEvent e) 
 			{
 				if (!unComposantOuvert())
 				{
 					String html = txt.lireFichier(fct.repertoireUtilisateur("res/aides","chrono.html"));
 					lblTitreComposant.setText("Gestion du chronomtre");
 					lblAideComposant.setText(html);
 				}
 				else 
 				{
 					lblAideComposant.setText(null);
 				}
 			}
 		});
 		slContentPane.putConstraint(SpringLayout.NORTH, btngestionChronoGeneral, 0, SpringLayout.NORTH, btnGestionIndices);
 		slContentPane.putConstraint(SpringLayout.SOUTH, btngestionChronoGeneral, 0, SpringLayout.SOUTH, btnParamtres);
 		slContentPane.putConstraint(SpringLayout.EAST, btngestionChronoGeneral, -6, SpringLayout.WEST, btnParamtres);
 		btngestionChronoGeneral.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent arg0)
 			{
 				// Masquer les autres composants
 				partieGestionJoueurs.estVisible(false);
 				partieParametres.estVisible(false);
 				partieGestionAlerte.estVisible(false);
 				partieGestionIndices.estVisible(false);
 				partieGestionChronoGeneral.estVisible(true);
 					
 				// Afficher le composant paramtres
 				lblTitreComposant.setText("Gestion du chronomtre");
 				lblAideComposant.setText(null);
 			}
 		});
 		contentPane.add(btngestionChronoGeneral);
 		btngestionChronoGeneral.setVisible(false);
 		
 		/**
 		 * Bouton nouvelle partie
 		 */
 		boutonNouvellePartie = new JButton("Nouvelle partie");
 		slContentPane.putConstraint(SpringLayout.NORTH, boutonNouvellePartie, 5, SpringLayout.NORTH, contentPane);
 		slContentPane.putConstraint(SpringLayout.NORTH, btnArreterLaPartie, 2, SpringLayout.SOUTH, boutonNouvellePartie);
 		slContentPane.putConstraint(SpringLayout.WEST, boutonNouvellePartie, 0, SpringLayout.WEST, btnArreterLaPartie);
 		boutonNouvellePartie.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent _e) 
 			{
 				JOptionPane jopSaisie = new JOptionPane();
 				@SuppressWarnings("static-access")
 				String nom = jopSaisie.showInputDialog(null, "Nom de la nouvelle partie : ", "Rentrez un nom", JOptionPane.QUESTION_MESSAGE);
 				if (nom != null)
 				{
 					coeurServeur.setNomBDD(nom+".sqlite");
 					// "CREATION" DE LA BDD 
 					// ( en ralit on copie une bdd sqlite "vide" dans le rpertoire res et on lui donne le nom entr par l'utilisateur
 					File fichierSource = new File("res/modele/bdd_vide.sqlite"); // la bdd vide
 					File fichierDest = new File("res/"+nom+".sqlite"); // la bdd  "crer"
 					
 					try
 					{
 						// Declaration et ouverture des flux
 						java.io.FileInputStream sourceFile = new java.io.FileInputStream(fichierSource);
 	
 						try
 						{
 							java.io.FileOutputStream destinationFile = null;
 		
 							try
 							{
 								destinationFile = new FileOutputStream(fichierDest);
 								
 								// Lecture par segment de 0.5Mo 
 								byte buffer[] = new byte[512 * 1024];
 								int nbLecture;
 								
 								while ((nbLecture = sourceFile.read(buffer)) != -1)
 								{
 									destinationFile.write(buffer, 0, nbLecture);
 								}
 							} 
 							finally 
 							{
 								destinationFile.close();
 							}
 						} 
 						finally 
 						{
 							sourceFile.close();
 						}
 					} 
 					catch (IOException e)
 					{
 						e.printStackTrace();
 					}
 					
 					// MODIFICATION DE L'INTERFACE
 					partieGestionJoueurs.listeJoueurs(partieGestionJoueurs.getTableJoueurs(), null);
 					partieGestionIndices.rafraichirListeIndices();
 					partieGestionAlerte.rafraichirListeAlerte();
 					btnGestionIndices.setVisible(true);
 					btnGestionDesJoueurs.setVisible(true);
 					btnGestionDesAlerte.setVisible(true);
 					btngestionChronoGeneral.setVisible(true);
 					partieChargeeCree = true;
 					btnArreterLaPartie.setEnabled(true);
 					chronoGeneral = new CompteurTempsGeneral(coeurServeur);
 					coeurServeur.setTempsGlobalRestant(chronoGeneral);
 					coeurServeur.getTempsGlobalRestant().addObserver(partieAlerte);
 					partieGestionChronoGeneral.rafraichirChronomtre();
 				}
 			}
 		});
 		contentPane.add(boutonNouvellePartie);
 		
 		/**
 		 * Bouton charger partie
 		 */
 		boutonChargerPartie = new JButton("Charger partie");
 		slContentPane.putConstraint(SpringLayout.NORTH, boutonChargerPartie, 5, SpringLayout.NORTH, contentPane);
 		slContentPane.putConstraint(SpringLayout.WEST, boutonChargerPartie, 6, SpringLayout.EAST, boutonNouvellePartie);
 		boutonChargerPartie.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent _e) 
 			{
 				JFileChooser selectionneurFichier = new JFileChooser();
 				selectionneurFichier.setFileFilter(new FiltreSelFichier());
 				selectionneurFichier.setCurrentDirectory(new File(System.getProperty("user.dir") + File.separator + "res"));
 				File fichier;
 				if(selectionneurFichier.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
 				{
 					fichier = selectionneurFichier.getSelectedFile();
 					coeurServeur.setNomBDD(fichier.getName());
 					partieGestionJoueurs.listeJoueurs(partieGestionJoueurs.getTableJoueurs(), null);
 					partieGestionAlerte.listeAlerte(partieGestionAlerte.getTableAlerte(), null);
 					partieGestionIndices.rafraichirListeIndices();
 					partieGestionJoueurs.rafraichirListeJoueurs();
 					partieGestionAlerte.rafraichirListeAlerte();
 					btnGestionIndices.setVisible(true);
 					btnGestionDesJoueurs.setVisible(true);
 					btnGestionDesAlerte.setVisible(true);
 					btngestionChronoGeneral.setVisible(true);
 					partieChargeeCree = true;
 					btnArreterLaPartie.setEnabled(true);
 					chronoGeneral = new CompteurTempsGeneral(coeurServeur);
 					coeurServeur.setTempsGlobalRestant(chronoGeneral);
 					coeurServeur.getTempsGlobalRestant().addObserver(partieAlerte);
 					partieGestionChronoGeneral.rafraichirChronomtre();
 					partieGestionIndices.chargerPersonnages();
 					coeurServeur.setPartieChargee(true);
 				}
 			}
 		});
 		contentPane.add(boutonChargerPartie);
 		
 		/**
 		 * Partie message de confirmation pour les actions des joueurs
 		 */
 		panelConfirmation = new JPanel();
 		panelConfirmation.setOpaque(false);
 		panelConfirmation.setVisible(false);
 		SpringLayout slPanelConfirmation = new SpringLayout();
 		panelConfirmation.setLayout(slPanelConfirmation);
 		slContentPane.putConstraint(SpringLayout.NORTH, panelConfirmation, 5, SpringLayout.NORTH, contentPane);
 		slContentPane.putConstraint(SpringLayout.SOUTH, panelConfirmation, 70, SpringLayout.NORTH, contentPane);
 		slContentPane.putConstraint(SpringLayout.WEST, panelConfirmation, 10, SpringLayout.EAST, boutonChargerPartie);
 		slContentPane.putConstraint(SpringLayout.EAST, panelConfirmation, 180, SpringLayout.EAST, boutonChargerPartie);
 		
 		labelMessageConfirmation = new JLabel("Message de confirmation :");
 		slPanelConfirmation.putConstraint(SpringLayout.NORTH, labelMessageConfirmation, 0, SpringLayout.NORTH, panelConfirmation);
 		slPanelConfirmation.putConstraint(SpringLayout.WEST, labelMessageConfirmation, 5, SpringLayout.WEST, panelConfirmation);
 		panelConfirmation.add(labelMessageConfirmation);
 		
 		boutonAccepter = new JButton("Accepter");
 		boutonAccepter.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent arg0)
 			{
 				BaseDeDonnees sql = new BaseDeDonnees(coeurServeur);
 				
 				try
 				{
 					Personnage perso = new Personnage(coeurClient);
 					if (perso.recupererTemps(coeurServeur.getStructDonTemps().getDonneur()) >= coeurServeur.getStructDonTemps().getTemps())
 					{
 						int tempsDonne = perso.recupererTemps(coeurServeur.getStructDonTemps().getReceveur());
 						tempsDonne = tempsDonne + coeurServeur.getStructDonTemps().getTemps();
 						int tempsPris = perso.recupererTemps(coeurServeur.getStructDonTemps().getDonneur());
 						tempsPris = tempsPris - coeurServeur.getStructDonTemps().getTemps();
 						// Recuperer l'id du personnage joueur
 						sql.executerInstruction("UPDATE personnage SET temps = " + tempsDonne + " WHERE login = '" + coeurServeur.getStructDonTemps().getReceveur() + "'");
 						sql.executerInstruction("UPDATE personnage SET temps = " + tempsPris + " WHERE login = '" + coeurServeur.getStructDonTemps().getDonneur() + "'");
 					}
 					else
 					{
 						JOptionPane jop = new JOptionPane();
 						JOptionPane.showMessageDialog(jop, "Le donneur n'a pas assez de temps pour effectuer ce don !", "Message d'erreur", JOptionPane.ERROR_MESSAGE);
 					}
 				} 
 				catch (SQLException e)
 				{
 					e.printStackTrace();
 				}
 				finally
 				{
 					// Fermer la connexion SQLite
 					sql.fermerConnexion();
 				}
 				estVisibleConfirmation(false);
 			}
 		});
 		slPanelConfirmation.putConstraint(SpringLayout.NORTH, boutonAccepter, 10, SpringLayout.SOUTH, labelMessageConfirmation);
 		slPanelConfirmation.putConstraint(SpringLayout.WEST, boutonAccepter, 5, SpringLayout.WEST, panelConfirmation);
 		panelConfirmation.add(boutonAccepter);
 		
 		boutonAnnuler = new JButton("Annuler");
 		boutonAnnuler.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent arg0)
 			{
 				estVisibleConfirmation(false);
 			}
 		});
 		slPanelConfirmation.putConstraint(SpringLayout.NORTH, boutonAnnuler, 10, SpringLayout.SOUTH, labelMessageConfirmation);
 		slPanelConfirmation.putConstraint(SpringLayout.WEST, boutonAnnuler, 10, SpringLayout.EAST, boutonAccepter);
 		panelConfirmation.add(boutonAnnuler);
 		
 		contentPane.add(panelConfirmation);
 		
 		/**
 		 * Bouton gestion des joueurs
 		 */
 		
 		btnGestionDesJoueurs = new JButton("Joueurs");
 		btnGestionDesJoueurs.addMouseListener(new MouseAdapter() 
 		{
 			@Override
 			public void mouseExited(MouseEvent arg0) 
 			{
 				afficherMasquerAide();
 			}
 		});
 		btnGestionDesJoueurs.addMouseMotionListener(new MouseMotionAdapter() 
 		{
 			@Override
 			public void mouseMoved(MouseEvent arg0) 
 			{
 				if (!unComposantOuvert())
 				{
 					String html = txt.lireFichier(fct.repertoireUtilisateur("res/aides","joueurs.html"));
 					lblTitreComposant.setText("Gestion des Joueurs");
 					lblAideComposant.setText(html);
 				}
 				else
 				{
 					//lblTitreComposant.setText("Gestion des Joueurs");
 					lblAideComposant.setText(null);
 				}
 			}
 		});
 		slContentPane.putConstraint(SpringLayout.WEST, btnParamtres, 0, SpringLayout.WEST, btnGestionDesJoueurs);
 		slContentPane.putConstraint(SpringLayout.NORTH, btnGestionDesJoueurs, 0, SpringLayout.NORTH, btnGestionIndices);
 		slContentPane.putConstraint(SpringLayout.EAST, btnGestionDesJoueurs, -6, SpringLayout.WEST, btnGestionIndices);
 		btnGestionDesJoueurs.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent e) 
 			{
 				// Masquer les autres composants
 				partieGestionIndices.estVisible(false);
 				partieParametres.estVisible(false);
 				partieGestionAlerte.estVisible(false);
 				partieGestionChronoGeneral.estVisible(false);
 				
 				// Afficher le composant paramtres
 				lblTitreComposant.setText("Gestion des Joueurs");
 				lblAideComposant.setText(null);
 				partieGestionJoueurs.estVisible(true);
 			}
 		});
 		contentPane.add(btnGestionDesJoueurs);
 		btnGestionDesJoueurs.setVisible(false);
 		
 		btnGestionDesAlerte = new JButton("Alertes");
 		btnGestionDesAlerte.addMouseListener(new MouseAdapter() 
 		{
 			@Override
 			public void mouseExited(MouseEvent e) 
 			{
 				afficherMasquerAide();
 			}
 		});
 		btnGestionDesAlerte.addMouseMotionListener(new MouseMotionAdapter() 
 		{
 			@Override
 			public void mouseMoved(MouseEvent arg0) 
 			{
 				if (!unComposantOuvert())
 				{
 					String html = txt.lireFichier(fct.repertoireUtilisateur("res/aides","alertes.html"));
 					lblTitreComposant.setText("Gestion des Alertes");
 					lblAideComposant.setText(html);
 				}
 				else 
 				{
 					//lblTitreComposant.setText("Gestion des Alertes");
 					lblAideComposant.setText(null);
 				}
 			}
 		});
 		slContentPane.putConstraint(SpringLayout.EAST, btnParamtres, 0, SpringLayout.EAST, btnGestionDesAlerte);
 		slContentPane.putConstraint(SpringLayout.NORTH, btnGestionDesAlerte, 0, SpringLayout.NORTH, contentPane);
 		slContentPane.putConstraint(SpringLayout.EAST, btnGestionIndices, -6, SpringLayout.WEST, btnGestionDesAlerte);
 		slContentPane.putConstraint(SpringLayout.EAST, btnGestionDesAlerte, -10, SpringLayout.EAST, contentPane);
 		btnGestionDesAlerte.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent e) 
 			{
 				// Masquer les autres composants
 				partieGestionIndices.estVisible(false);
 				partieParametres.estVisible(false);
 				partieGestionJoueurs.estVisible(false);
 				partieGestionChronoGeneral.estVisible(false);
 				
 				// Afficher le composant paramtres
 				lblTitreComposant.setText("Gestion des Alertes");
 				lblAideComposant.setText(null);
 				partieGestionAlerte.estVisible(true);
 			}
 		});
 		contentPane.add(btnGestionDesAlerte);
 		btnGestionDesAlerte.setVisible(false);
 		
 		
 		// Instancier le composant : gestion des joueurs
 		partieGestionJoueurs = new ComposantGestionJoueurs(this, slContentPane, coeurServeur);
 		partieGestionJoueurs.estVisible(false);
 		
 		// Instancier le composant : gestion des indices
 		partieGestionIndices = new ComposantGestionIndices(this, slContentPane, coeurServeur);
 		partieGestionIndices.estVisible(false);
 		
 		// Instancier le composant : gestion du chrono gnral (rem : une partie doit tre lance pour avoir acces  la valeur du chronomtre principal)
 		partieGestionChronoGeneral = new ComposantGestionChronoGeneral(this, slContentPane, coeurServeur, coeurClient);
 		partieGestionChronoGeneral.estVisible(false);
 		partieGestionChronoGeneral.constructionIHM();
 		
 		// Instancier le composant : paramtres
 		partieParametres = new ComposantParametres(this, slContentPane);
 		partieParametres.estVisible(false);
 		
 		// Instancier le composant : gestion des alertes
 		partieGestionAlerte = new ComposantGestionAlerte(this, slContentPane, coeurServeur);
 		partieGestionAlerte.estVisible(false);
 		
 		// Instancier le composant : alertes
 		partieAlerte = new ComposantAlerte(coeurServeur);
 		
 		// Instancier le composant : partie tchat
 		partieTchat = new ComposantTchat(this, slContentPane, partieGestionIndices.getPanelGestionIndices(), coeurClient);
 		
 		lblTitreComposant = new JLabel("Bienvenue, Maitre de Jeu !");
 		slContentPane.putConstraint(SpringLayout.NORTH, lblTitreComposant, 44, SpringLayout.SOUTH, btngestionChronoGeneral);
 		slContentPane.putConstraint(SpringLayout.WEST, lblTitreComposant, 174, SpringLayout.WEST, contentPane);
 		slContentPane.putConstraint(SpringLayout.SOUTH, lblTitreComposant, 90, SpringLayout.SOUTH, btngestionChronoGeneral);
 		slContentPane.putConstraint(SpringLayout.EAST, lblTitreComposant, 764, SpringLayout.WEST, contentPane);
 		lblTitreComposant.setFont(new Font("SansSerif", Font.PLAIN, 25));
 		contentPane.add(lblTitreComposant);
 		
 		String html = txt.lireFichier(fct.repertoireUtilisateur("res/aides","bienvenue.html"));
 		lblAideComposant = new JLabel(html);
 		slContentPane.putConstraint(SpringLayout.NORTH, lblAideComposant, 91, SpringLayout.SOUTH, btnParamtres);
 		slContentPane.putConstraint(SpringLayout.WEST, lblAideComposant, 216, SpringLayout.WEST, contentPane);
 		slContentPane.putConstraint(SpringLayout.SOUTH, lblAideComposant, 300, SpringLayout.SOUTH, btnParamtres);
 		slContentPane.putConstraint(SpringLayout.EAST, lblAideComposant, 764, SpringLayout.WEST, contentPane);
 		contentPane.add(lblAideComposant);
 		partieTchat.demarrerCommunication(true);
 		partieTchat.changerNomJoueur("MaitreDeJeu");
 		partieTchat.estVisible(true);
 		
 		/*
 		 * Arrire-plan de l'IHM
 		 * 
 		 * @ImagePanel panelImageTitre
 		 */
 		Theme theme = new Theme();
 		panelImageTitre = new ImagePanel("themes/" + theme.definirFond(), (JPanel) getContentPane());
 		panelImageTitre.setLayout(new BorderLayout(0, 0));
 		slContentPane.putConstraint(SpringLayout.NORTH, panelImageTitre, 0, SpringLayout.NORTH, getContentPane());
 		slContentPane.putConstraint(SpringLayout.SOUTH, panelImageTitre, 0, SpringLayout.SOUTH, getContentPane());
 		slContentPane.putConstraint(SpringLayout.WEST, panelImageTitre, 0, SpringLayout.WEST, getContentPane());
 		slContentPane.putConstraint(SpringLayout.EAST, panelImageTitre, 0, SpringLayout.EAST, getContentPane());
 		getContentPane().add(panelImageTitre);
 	}
 	
 	
 // ----- METHODES ----- //
 	
 	/**
 	 * Fonction qui retourne vrai si au moins 1 composant est ouvert
 	 * @return Vrai ou Faux
 	 */
 	private boolean unComposantOuvert()
 	{
 		if (partieGestionJoueurs.isEnabled()
 				|| partieGestionAlerte.isEnabled()
 				|| partieGestionChronoGeneral.isEnabled()
 				|| partieGestionIndices.isEnabled()
 				|| partieParametres.isEnabled()
 				|| partieGestionAlerte.isEnabled())
 				return true;
 			{
 				return false;
 			}
 	}
 	
 	/**
 	 * Procdure qui affiche / masque l'aide contextuelle
 	 */
 	private void afficherMasquerAide()
 	{
 		if (!unComposantOuvert())
 			lblTitreComposant.setText(null);
 		lblAideComposant.setText(null);
 	}
 	
 	/**
 	 * Visibilit du panel de conformation
 	 * @param _visible
 	 */
 	private void estVisibleConfirmation(boolean _visible)
 	{
 		panelConfirmation.setVisible(_visible);
 	}
 
 	/**
 	 * Mise a jours de l'observation du panel de confirmation
 	 */
 	public void update(Observable _o, Object _arg) 
 	{
 		estVisibleConfirmation(true);
 		labelMessageConfirmation.setText("Don de " + coeurServeur.getStructDonTemps().getDonneur() + "  " + coeurServeur.getStructDonTemps().getReceveur() + " de " + coeurServeur.getStructDonTemps().getTemps() + " sec.");
 	}
 	
 
 // ----- Getters & Setter ----- //
 
 	/**
 	 * Getter du composant pour la gestion des indices
 	 * @return ComposantGestionIndices
 	 */
 	public ComposantGestionIndices getPartieGestionIndices() 
 	{
 		return partieGestionIndices;
 	}
 }
