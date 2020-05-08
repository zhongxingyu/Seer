 package fr.mercredymurderparty.ihm.fenetres;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.SpringLayout;
 import javax.swing.JButton;
 
 import fr.mercredymurderparty.ihm.composants.ComposantGestionIndices;
 import fr.mercredymurderparty.ihm.composants.ComposantCreerModifierJoueur;
 import fr.mercredymurderparty.ihm.composants.ComposantTchat;
 import fr.mercredymurderparty.outil.Fonctions;
 
 import java.awt.Dimension;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 @SuppressWarnings("serial")
 public class FenetreAdmin extends JFrame 
 {
 	
 // ----- ATTRIBUTS ----- //
 	
 	// ----- INTERFACE GRAPHIQUE ----- //
 	private Dimension screenSize;
 	
 	private JPanel contentPane;
 	private JButton btnArreterLaPartie;
 	
 	// ----- GESTION TCHAT ----- //
 	private ComposantTchat partieTchat;
 	private Fonctions fct;
 	
 	// ----- GESTION BDD ----- //
 	private ComposantCreerModifierJoueur partieCreerModifierJoueur;
 	private ComposantGestionIndices partieGestionIndices;
 	
 // ----- CONSTRUCTEUR ----- //
 
 	/**
 	 * Constructeur de la classe FenetreAdmin
 	 */
 	public FenetreAdmin() 
 	{
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
 		setBounds(100, 100, 699, 442);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setMinimumSize(new Dimension(800, 600));
 		setSize(new Dimension(800, 600));
 		screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
 		setLocation((screenSize.width-this.getWidth())/2, (screenSize.height-this.getHeight())/2);
 		setContentPane(contentPane);
 		final SpringLayout slContentPane = new SpringLayout();
 		contentPane.setLayout(slContentPane);
 		setExtendedState(getExtendedState()|JFrame.MAXIMIZED_BOTH);
 		
 		/**
 		 * Evenement lors de la fermeture de la fenetre
 		 */
 		addWindowListener(new WindowAdapter() 
 		{
 			@Override
 			public void windowClosing(WindowEvent e) {
 				fct = new Fonctions();
 				fct.envoyerFichierViaFtp("yakipe.com", "yakip263492", "M2F5YnPpoi",  "/sqlitemanager/murder_bdd.sqlite", fct.repertoireUtilisateur("res", "murder_bdd.sqlite"));
 			}
 		});
 		
 		JButton btnParamtres = new JButton("Paramtres");
 		slContentPane.putConstraint(SpringLayout.NORTH, btnParamtres, 0, SpringLayout.NORTH, contentPane);
 		slContentPane.putConstraint(SpringLayout.EAST, btnParamtres, 0, SpringLayout.EAST, contentPane);
 		contentPane.add(btnParamtres);
 		
 		btnArreterLaPartie = new JButton("Arr\u00EAter la partie");
 		slContentPane.putConstraint(SpringLayout.WEST, btnArreterLaPartie, 0, SpringLayout.WEST, contentPane);
 		btnArreterLaPartie.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent _e) 
 			{
 				partieTchat.arreterSession();
 			}
 		});
 		slContentPane.putConstraint(SpringLayout.NORTH, btnArreterLaPartie, 0, SpringLayout.NORTH, btnParamtres);
 		contentPane.add(btnArreterLaPartie);
 		
 		JButton btnCrerNouveauJoueur = new JButton("crer nouveau joueur");
 		slContentPane.putConstraint(SpringLayout.NORTH, btnCrerNouveauJoueur, 40, SpringLayout.NORTH, contentPane);
 		slContentPane.putConstraint(SpringLayout.EAST, btnCrerNouveauJoueur, 0, SpringLayout.EAST, contentPane);
 		btnCrerNouveauJoueur.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent arg0)
 			{
 				partieGestionIndices.estVisible(false);
 				
 				// montre le champ du chrono (utile si l'utilisateur clique d'abord sur modifier joueur ou supprimer joueur puis creer joueur)
 				partieCreerModifierJoueur.estVisibleComposantChrono(true);
 				
 				partieCreerModifierJoueur.setActionAEffectuer(1);
 				partieCreerModifierJoueur.estVisible(true);
 				partieCreerModifierJoueur.gestionJoueur();
 			}
 		});
 		contentPane.add(btnCrerNouveauJoueur);
 		
 		JButton btnModifierJoueur = new JButton("modifier joueur");
 		slContentPane.putConstraint(SpringLayout.NORTH, btnModifierJoueur, 0, SpringLayout.NORTH, btnCrerNouveauJoueur);
 		slContentPane.putConstraint(SpringLayout.EAST, btnModifierJoueur, -6, SpringLayout.WEST, btnCrerNouveauJoueur);
 		btnModifierJoueur.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent arg0)
 			{
 				partieGestionIndices.estVisible(false);
 				
 				// masque les informations liees au temps d'initialisation du chronometre. Ce n'est pas comme ca qu'on le modifie
 				partieCreerModifierJoueur.estVisibleComposantChrono(false);
 				
 				// destruction de la liste obligatoire car si le joueur clique deja sur supprimer joueur puis modifier, pb sur la liste
 				partieCreerModifierJoueur.detruireComposantListe();
 				
 				partieCreerModifierJoueur.setActionAEffectuer(2);
 				partieCreerModifierJoueur.estVisible(true);
 				partieCreerModifierJoueur.creerListe();
 				partieCreerModifierJoueur.gestionJoueur();
 			}
 		});
 		contentPane.add(btnModifierJoueur);
 		
 		JButton btnSupprimerJoueur = new JButton("supprimer joueur");
 		slContentPane.putConstraint(SpringLayout.NORTH, btnSupprimerJoueur, 0, SpringLayout.NORTH, btnCrerNouveauJoueur);
 		slContentPane.putConstraint(SpringLayout.EAST, btnSupprimerJoueur, -6, SpringLayout.WEST, btnModifierJoueur);
 		btnSupprimerJoueur.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent arg0)
 			{
 				partieGestionIndices.estVisible(false);
 				
 				// masque les informations liees au temps d'initialisation du chronometre. Ce n'est pas comme ca qu'on le modifie
 				partieCreerModifierJoueur.estVisibleComposantChrono(false);
 				
 				// destruction de la liste obligatoire car si le joueur clique deja sur modifier joueur puis supprimer, pb sur la liste
 				partieCreerModifierJoueur.detruireComposantListe();
 				
 				partieCreerModifierJoueur.setActionAEffectuer(3);
 				partieCreerModifierJoueur.estVisible(true);
 				partieCreerModifierJoueur.creerListe();
 				partieCreerModifierJoueur.gestionJoueur();
 			}
 		});
 		contentPane.add(btnSupprimerJoueur);
 		
 		partieCreerModifierJoueur = new ComposantCreerModifierJoueur(this, slContentPane);
 		partieCreerModifierJoueur.estVisible(false);
 		
 		//Bouton Crer indice
 		JButton btnGestionIndices = new JButton("Gestion des indices");
 		slContentPane.putConstraint(SpringLayout.NORTH, btnGestionIndices, 0, SpringLayout.NORTH, btnParamtres);
 		slContentPane.putConstraint(SpringLayout.EAST, btnGestionIndices, -6, SpringLayout.WEST, btnParamtres);
 		btnGestionIndices.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent arg0)
 			{
 				partieCreerModifierJoueur.estVisible(false);
 				partieGestionIndices.estVisible(true);
 			}
 		});
 		contentPane.add(btnGestionIndices);
 		
 		partieGestionIndices = new ComposantGestionIndices(this, slContentPane);
 		
 		partieGestionIndices.estVisible(false);
 		
 		partieTchat = new ComposantTchat(this, slContentPane, partieGestionIndices.getPanelGestionIndices());
 		partieTchat.demarrerSession(true);
 		partieTchat.changerNomJoueur("MaitreDeJeu");
 		partieTchat.estVisible(true);
 	}
 }
