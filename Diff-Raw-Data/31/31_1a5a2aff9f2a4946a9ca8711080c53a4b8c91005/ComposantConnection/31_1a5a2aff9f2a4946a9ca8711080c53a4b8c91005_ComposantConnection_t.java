 package fr.mercredymurderparty.ihm.composants;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 import javax.swing.SpringLayout;
 
 import fr.mercredymurderparty.client.CoeurClient;
 import fr.mercredymurderparty.client.CompteurTempsJoueur;
 import fr.mercredymurderparty.client.Personnage;
 import fr.mercredymurderparty.ihm.fenetres.FenetreJoueur;
 
 @SuppressWarnings("serial")
 public class ComposantConnection extends JPanel
 {
 	
 // ----- ATTRIBUTS ----- //
 	
 	// ----- INTERFACE GRAPHIQUE ----- //
 	private JPanel panelAuthentification;
 	private JLabel labelIdentifiant;
 	private JLabel labelMotDePasse;
 	private JTextField champIdentifiant;
 	private JPasswordField champMotDePasse;
 	private JButton boutonSeConnecter;
 	private JLabel labelMessageErreur;
 	
 	// ----- CLASSES ----- //
 	private ComposantJoueur partieJoueur;
 	private ComposantTchat partieTchat;
 	private Personnage perso;
 	
 	// ----- MODELE ----- //
 	private CoeurClient coeurClient;
 	
 	
 // ----- CONSTRUCTEUR ----- //
 	
 	/**
 	 * Le constructeur de la partie de la zone de connection
 	 * @param _fenetre : "Fenetre" la fentre parent
 	 * @param _springLayout : "SpringLayout" le type de mise en page de la fentre parent
 	 */
 	
 	public ComposantConnection(final FenetreJoueur _fenetre, final SpringLayout _springLayout, CoeurClient _coeurClient)
 	{
 		coeurClient = _coeurClient;
 		// Appeler la classe Personnage
 		perso = new Personnage(coeurClient);
 		
 		/*
 		 * Partie de l'IHM pour l'authentification d'un utilisateur ( CENTRE )
 		 * 
 		 * @conteneur panelAuthentification
 		 * @label labelIdentifiant
 		 * @label labelMotDePasse
 		 * @champ_texte champIdentifiant
 		 * @champ_texte champMotDePasse
 		 * @bouton boutonSeConnecter
 		 * @label labelMessageErreur
 		 */
 		
 		panelAuthentification = new JPanel();
 		SpringLayout slPanelAuthentification = new SpringLayout();
 		panelAuthentification.setLayout(slPanelAuthentification);
 		panelAuthentification.setOpaque(false);
 		_springLayout.putConstraint(SpringLayout.NORTH, panelAuthentification, -150, SpringLayout.VERTICAL_CENTER, _fenetre.getContentPane());
 		_springLayout.putConstraint(SpringLayout.SOUTH, panelAuthentification, 20, SpringLayout.VERTICAL_CENTER, _fenetre.getContentPane());
 		_springLayout.putConstraint(SpringLayout.WEST, panelAuthentification, -125, SpringLayout.HORIZONTAL_CENTER, _fenetre.getContentPane());
 		_springLayout.putConstraint(SpringLayout.EAST, panelAuthentification, 125, SpringLayout.HORIZONTAL_CENTER, _fenetre.getContentPane());
 		_fenetre.getContentPane().add(panelAuthentification);
 		
 		labelIdentifiant = new JLabel("Identifiant");
 		labelIdentifiant.setForeground(Color.WHITE);
 		slPanelAuthentification.putConstraint(SpringLayout.NORTH, labelIdentifiant, 10, SpringLayout.NORTH, panelAuthentification);
 		slPanelAuthentification.putConstraint(SpringLayout.WEST, labelIdentifiant, 10, SpringLayout.WEST, panelAuthentification);
 		panelAuthentification.add(labelIdentifiant);
 		
 		labelMotDePasse = new JLabel("Mot de passe");
 		labelMotDePasse.setForeground(Color.WHITE);
 		slPanelAuthentification.putConstraint(SpringLayout.NORTH, labelMotDePasse, 50, SpringLayout.NORTH, panelAuthentification);
 		slPanelAuthentification.putConstraint(SpringLayout.WEST, labelMotDePasse, 10, SpringLayout.WEST, panelAuthentification);
 		panelAuthentification.add(labelMotDePasse);
 		
 		champIdentifiant = new JTextField();
 		slPanelAuthentification.putConstraint(SpringLayout.NORTH, champIdentifiant, 10, SpringLayout.NORTH, panelAuthentification);
 		slPanelAuthentification.putConstraint(SpringLayout.EAST, champIdentifiant, -10, SpringLayout.EAST, panelAuthentification);
 		panelAuthentification.add(champIdentifiant);
 		champIdentifiant.setColumns(10);
 		
 		champMotDePasse = new JPasswordField();
 		slPanelAuthentification.putConstraint(SpringLayout.NORTH, champMotDePasse, 50, SpringLayout.NORTH, panelAuthentification);
 		slPanelAuthentification.putConstraint(SpringLayout.EAST, champMotDePasse, -10, SpringLayout.EAST, panelAuthentification);
 		panelAuthentification.add(champMotDePasse);
 		champMotDePasse.setColumns(10);
 		
 		boutonSeConnecter = new JButton("Se connecter");
 		boutonSeConnecter.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent _e)
 			{
 				// Vrifier les informations saisie et lancer l'interface client si tout est bon
 				if (perso.connexion(champIdentifiant.getText(), champMotDePasse.getPassword(), labelMessageErreur))
 				{
 					// Dfinir les identifiants du joueur dans la classe personnage
 					perso.setId(perso.recupererIdentifiant(champIdentifiant.getText()));
 					perso.setLogin(champIdentifiant.getText());
 					// Afficher les indices
 					partieJoueur.getPartieIndices().afficherIndices();
 					
 					// Passer d'invit  joueur connect
 					partieJoueur.changerNomJoueur(perso.getLogin());
 					partieTchat.changerNomJoueur(perso.getLogin());
 					coeurClient.setLogin(perso.getLogin());
 					// Arrter la session invit
 					//partieTchat.arreterCommunication();
 					
 					// Dmarr la session du joueur connect
 					//partieTchat.demarrerCommunication(true);
 					
 					// Suppression du contenu de la messagerie
 					partieJoueur.getPartieTchat().setMessagesTchat("");
										
 					/*
 					 * AJOUT TEMPORAIRE DE CLMENT
 					 * FIXME : Je ne sais pas si c'est une bonne chose de faire ces oprations ici,
 					 * cela dit la personne ayant ralis ces modules n'tant pas joignable je prfre 
 					 * faire a ici et en discuter aprs avec vous en runion.
 					 */
 					int temps = perso.recupererTemps(perso.getLogin());
 					partieJoueur.getHorlogeJoueur().setCompteurJoueur(new CompteurTempsJoueur(perso, temps, coeurClient)); // cration du thread du compteur de temps
 					coeurClient.setCompteurJoueur(partieJoueur.getHorlogeJoueur().getCompteurJoueur());
 					partieJoueur.getHorlogeJoueur().getCompteurJoueur().addObserver(partieJoueur.getHorlogeJoueur()); // Ajout du composant Horloge Joueur comme observateur du Thread
 					partieJoueur.getHorlogeJoueur().getLabelCompteurJoueur().setText(partieJoueur.getHorlogeJoueur().getCompteurJoueur().chaineCaracTemps());
 					partieJoueur.getHorlogeJoueur().getCompteurJoueur().demarrer(); // On lance le compteur
 					/*
 					 * FIN AJOUT TEMPORAIRE DE CLMENT
 					 */
 					
 					partieJoueur.estVisible(true);
 					partieJoueur.getComposantDonTemps().getJoueur().setLogin(champIdentifiant.getText());
 					partieJoueur.getComposantDonTemps().estVisible(false);
 					partieJoueur.setActiverDonTemps(false);
 					
 					//partieJoueur.getLabelIdentifiant().setText(champIdentifiant.getText());
 					estVisible(false);
 					coeurClient.setEstConnecte(true);
					
					// Suppression du contenu des champs identifiant et mot de passe
					champIdentifiant.setText("");
					champMotDePasse.setText("");
 				}
 			}
 		});
 		boutonSeConnecter.setOpaque(true);
 		slPanelAuthentification.putConstraint(SpringLayout.NORTH, boutonSeConnecter, 90, SpringLayout.NORTH, _fenetre.getContentPane());
 		slPanelAuthentification.putConstraint(SpringLayout.EAST, boutonSeConnecter, -10, SpringLayout.EAST, panelAuthentification);
 		panelAuthentification.add(boutonSeConnecter);
 		labelMessageErreur = new JLabel();
 		labelMessageErreur.setForeground(Color.ORANGE);
 		coeurClient.setLabelMessageErreur(labelMessageErreur);
 		slPanelAuthentification.putConstraint(SpringLayout.WEST, labelMessageErreur, 0, SpringLayout.WEST, panelAuthentification);
 		slPanelAuthentification.putConstraint(SpringLayout.SOUTH, labelMessageErreur, -10, SpringLayout.SOUTH, panelAuthentification);
 		panelAuthentification.add(labelMessageErreur);
 		
 		partieTchat = new ComposantTchat(_fenetre, _springLayout, this.getPanelAuthentification(), coeurClient);
 		partieTchat.demarrerCommunication(false);
 		
 		// On masque les messages d'erreur
 		labelMessageErreur.setVisible(false);
 		
 		// On place les lments de l'interface du joueur
 		partieJoueur = new ComposantJoueur(this, _fenetre, _springLayout, partieTchat, coeurClient);
 		partieJoueur.estVisible(false);
 	}
 	
 	
 // ----- METHODES ----- //
 	
 	/**
 	 * La mthode estVisible permet d'afficher ou non le conteneur de connection
 	 * @param _visible : "boolean" affiche ou masque ce conteneur
 	 */
 	final public void estVisible(boolean _visible)
 	{
 		labelMessageErreur.setText(null);
 		panelAuthentification.setVisible(_visible);
 	}
 	
 	/**
 	 * Getter du conteneur de connection du joueur
 	 * @return Recuperer le conteneur de la connection 
 	 */
 	final public JPanel getPanelAuthentification()
 	{
 		return panelAuthentification;
 	}
 	
 	/**
 	 * Setter du conteneur de connection du joueur
 	 * @param _panelAuthentification le conteneur de la connection
 	 */
 	final public void setPanelAuthentification(JPanel _panelAuthentification)
 	{
 		this.panelAuthentification = _panelAuthentification;
 	}
 	
 	/**
 	 * Setter du label message erreur
 	 * @param _labelMessageErreur le nouveau label message erreur
 	 */
 	final public void setLabelMessageErreur(JLabel _labelMessageErreur) {
 		this.labelMessageErreur = _labelMessageErreur;
 	}
 }
