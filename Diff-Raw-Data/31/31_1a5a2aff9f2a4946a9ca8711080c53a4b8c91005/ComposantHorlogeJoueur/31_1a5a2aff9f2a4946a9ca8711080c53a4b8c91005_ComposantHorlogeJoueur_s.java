 package fr.mercredymurderparty.ihm.composants;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SpringLayout;
 
 import fr.mercredymurderparty.client.CompteurTempsJoueur;
 import fr.mercredymurderparty.ihm.fenetres.FenetreJoueur;
 
 public class ComposantHorlogeJoueur implements Observer
 {
 	
 // ----- ATTRIBUTS ----- //
 	
 	// ----- INTERFACE GRAPHIQUE ----- //
 	private JPanel panelCompteurJoueur;
 	private JLabel labelCompteurJoueur;
 	private JLabel labelArretJoueur;
 	
 	// ----- GESTION COMPTEUR ----- //
 	/*
 	 * FIXME a ne me plais pas d'avoir un mettre un thread dans un composant, 
 	 * a n'est pas sa place et je suis sur qu'il y a d'autres solutions ... 
 	 * mais bon, personne n'est disponnible pour en discuter, donc en attendant ...
 	 */
 	private CompteurTempsJoueur compteurJoueur;
 		
 		
 // ----- CONSTRUCTEUR ----- //
 		
 	/**
 	 * Le constructeur de la partie pour les chronomtres
 	 * @param _fenetre : "Fenetre" la fentre parent
 	 * @param _springLayout : "SpringLayout" le type de mise en page de la fentre parent
 	 */
 	
 	public ComposantHorlogeJoueur(FenetreJoueur _fenetre, SpringLayout _springLayout)
 	{
 		/*
 		 * Partie de l'IHM pour l'affichage du compteur gnral ( HAUT-DROITE )
 		 *
 		 * @conteneur panelCompteurJoueur
 		 * @label labelCompteurJoueur
 		 * @label labelArretJoueur (invisible par dfaut)
 		 */
 
 		panelCompteurJoueur = new JPanel();
 		SpringLayout slPanelCompteur = new SpringLayout();
 		panelCompteurJoueur.setLayout(slPanelCompteur);
 		panelCompteurJoueur.setOpaque(false);
 		_springLayout.putConstraint(SpringLayout.NORTH, panelCompteurJoueur, 50, SpringLayout.NORTH, _fenetre.getContentPane());
 		_springLayout.putConstraint(SpringLayout.WEST, panelCompteurJoueur, -147, SpringLayout.EAST, _fenetre.getContentPane());
 		_springLayout.putConstraint(SpringLayout.SOUTH, panelCompteurJoueur, 150, SpringLayout.NORTH, _fenetre.getContentPane());
 		_springLayout.putConstraint(SpringLayout.EAST, panelCompteurJoueur, 0, SpringLayout.EAST, _fenetre.getContentPane());
 		_fenetre.getContentPane().add(panelCompteurJoueur);
 
 		labelCompteurJoueur = new JLabel("00:00:00");
 		labelCompteurJoueur.setForeground(Color.WHITE);
 		slPanelCompteur.putConstraint(SpringLayout.NORTH, labelCompteurJoueur, 10, SpringLayout.NORTH, panelCompteurJoueur);
 		slPanelCompteur.putConstraint(SpringLayout.WEST, labelCompteurJoueur, 10, SpringLayout.WEST, panelCompteurJoueur);
 		labelCompteurJoueur.setFont(new Font("Tahoma", Font.PLAIN, 30));
 		panelCompteurJoueur.add(labelCompteurJoueur);
 
 		labelArretJoueur = new JLabel("ARRET");
 		labelArretJoueur.setForeground(Color.WHITE);
 		labelArretJoueur.setVisible(false);
 		slPanelCompteur.putConstraint(SpringLayout.NORTH, labelArretJoueur, 6, SpringLayout.SOUTH, labelCompteurJoueur);
 		slPanelCompteur.putConstraint(SpringLayout.WEST, labelArretJoueur, 10, SpringLayout.WEST, labelCompteurJoueur);
 		labelArretJoueur.setFont(new Font("Tahoma", Font.PLAIN, 30));
 		panelCompteurJoueur.add(labelArretJoueur);
 	}
 		
 		
 // ----- METHODES ----- //
 		
 	/**
 	 * La mthode estVisible permet d'afficher ou non le conteneur de connection
 	 * @param _visible : "boolean" affiche ou masque ce conteneur
 	 */
 	final public void estVisible(boolean _visible)
 	{
 		panelCompteurJoueur.setVisible(_visible);
 	}
 	
 	/**
 	 * Mise  jour du label affichant le temps
 	 * @param _chaineTemps : "String" la chaine  afficher
 	 */
 	final public void majCompteur(String _chaineTemps)
 	{
 		labelCompteurJoueur.setText(_chaineTemps);
 	}
 
 	/**
 	 * Ce que fait la classe quand l'objet observ change ( ici quand le compteur s'incrmente )
 	 */
 	public void update(Observable arg0, Object arg1)
 	{
 		labelCompteurJoueur.setText(compteurJoueur.chaineCaracTemps());	
		System.out.println(compteurJoueur.chaineCaracTemps());
 	}
 	
 	public void arreterChrono()
 	{
 		compteurJoueur.arreter();
 	}
 		
 	
 // ----- Getters & Setters ----- //
 		
 	/**
 	 * Getter du compteur de temps du joueur
 	 * @return le compteur de temps du joueur
 	 */
 	public CompteurTempsJoueur getCompteurJoueur()
 	{
 		return compteurJoueur;
 	}
 
 	/**
 	 * Setter du compteur de temps du joueur
 	 * @param _compteurJoueur le compteur de temps du joueur
 	 */
 	public void setCompteurJoueur(CompteurTempsJoueur _compteurJoueur)
 	{
 		this.compteurJoueur = _compteurJoueur;
 	}
 
 	/**
 	 * Getter du label du compteur du joueur
 	 * @return Le composant label
 	 */
 	public JLabel getLabelCompteurJoueur()
 	{
 		return labelCompteurJoueur;
 	}
 }
