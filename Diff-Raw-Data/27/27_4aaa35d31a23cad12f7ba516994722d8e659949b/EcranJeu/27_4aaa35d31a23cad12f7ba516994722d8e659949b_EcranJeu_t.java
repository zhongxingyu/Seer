 package client.view.jeu;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Rectangle;
 
 import javax.swing.JLabel;
 import javax.swing.JLayeredPane;
 import javax.swing.JPanel;
 
 import client.Client;
 import client.JeuPanel;
 import client.controller.EcranJeuListener;
 import client.view.NamedJPanel;
 
 import common.Constante;
 import common.ElementPlateau;
 import common.Joueur;
 import common.TypeElementPlateau;
 import common.partie.batiment.TypeBatiment;
 import common.partie.plateau.Case;
 import common.partie.unite.TypeUnite;
 import common.partie.unite.Unite;
 
 
 public class EcranJeu extends NamedJPanel{
 	private static final long serialVersionUID = Constante.NUMERO_DE_VERSION;
 	private JLayeredPane layeredPane;
 	
 	private JPanel fond = new FondJeu();
 	private OngletPanel ongletJoueur;
 	private OngletPanel ongletVille;
 	private OngletPanel ongletMenu;
 	private OngletPanel ongletBatiment;
 	private OngletPanel ongletUnite;
 	
 	private EcranPlateau ecranPlateau;
 	private EcranAttenteTour ecranAttenteTour;
 	
 	private EcranSauvegardePartie ecranSauvegardePartie;
 	
 	private JLabel labelCouleurJoueur = new JLabel();
 	private JLabel labelArgent = new JLabel("ARGENT :",JLabel.RIGHT);
 	private Font font = new Font("Serif",Font.BOLD,30);
 	
 	
 	// label qui est affiché (et qui suit la souris) lors du mode création d'un batiment ou d'une unité
 	private JLabel labelEnConstruction = new JLabel();
 	private TypeElementPlateau typeElementEnConstruction;
 	
 	private EcranAffichageDeplacement ecranAffichageDeplacement;
 	private Unite uniteEnDeplacement;// unité en cours de deplacement qui est a affiché
 	
 	private ToolTipInfo tooltip;
	private boolean premierePartie; // permet de gerer correctement l'affichage en cas de nouvelle partie solo apres avoir quitté une
									// premiere partie
 	
 	
 	public EcranJeu(JeuPanel jeu,JLayeredPane layeredPane){
 		super("ecranJeu",jeu);
 		this.layeredPane = layeredPane;
 		
 		ongletJoueur = new OngletJoueurPanel(jeu);
 		ongletVille = new OngletVillePanel(jeu);
 		ongletMenu = new OngletMenuPanel(jeu);
 		ongletBatiment = new OngletBatimentPanel(jeu);
 		ongletUnite = new OngletUnitePanel(jeu);
 		ecranAttenteTour = new EcranAttenteTour(jeu);
 		ecranPlateau = new EcranPlateau(jeu);
 		ecranAffichageDeplacement = new EcranAffichageDeplacement(jeu);
 		ecranSauvegardePartie = new EcranSauvegardePartie(jeu);
 		tooltip = new ToolTipInfo();
 		cacherEcranAttente();
 		premierePartie = true;
 	}
 	
 	public void afficherPlateau(){
		if (premierePartie){
 			layeredPane.add(fond,new Integer(-3000));
 			layeredPane.add(labelArgent,new Integer(-2000));
 			layeredPane.add(ecranPlateau,new Integer(-1000));
 			
 			layeredPane.add(labelCouleurJoueur,new Integer(-500));
 			
 			layeredPane.add(tooltip,new Integer(-100));
 			
 			layeredPane.add(ongletJoueur,new Integer(0));
 			layeredPane.add(ongletVille,new Integer(0));
 			layeredPane.add(ongletMenu,new Integer(0));
 			layeredPane.add(ongletBatiment,new Integer(0));
 			layeredPane.add(ongletUnite,new Integer(0));
 			
 			layeredPane.add(labelEnConstruction,new Integer(10));
 			tooltip.setVisible(false);
 			
 			layeredPane.add(ecranAttenteTour,new Integer(200));
 			
 			layeredPane.add(ecranSauvegardePartie,new Integer(250));
 			ecranSauvegardePartie.setVisible(false);
 			
 			layeredPane.add(ecranAffichageDeplacement,new Integer(100));
 			ecranAffichageDeplacement.setVisible(false);
 			
 			labelEnConstruction.setBounds(Constante.LARGEUR_FENETRE_PRINCIPALE / 2,Constante.HAUTEUR_FENETRE_PRINCIPALE / 2,50,50);
 			
 			update();
 			labelArgent.setFont(font);
 			labelArgent.setBounds(Constante.LARGEUR_FENETRE_PRINCIPALE - 350,Constante.HAUTEUR_FENETRE_PRINCIPALE - 80,300,40);
 			
 			Joueur joueur = jeu.getClient().getJoueur();
 			int indJoueur = jeu.getClient().getPartie().getListeParticipants().indexOf(joueur);
 			Color couleurJoueur = Constante.COLORS[indJoueur];
 			labelCouleurJoueur.setOpaque(true);
 			labelCouleurJoueur.setBackground(couleurJoueur);
 			labelCouleurJoueur.setBounds(Constante.LARGEUR_FENETRE_PRINCIPALE - 350,Constante.HAUTEUR_FENETRE_PRINCIPALE - 80,40,40);
 			
 			cacherTousLesOngets();
 			
			EcranJeuListener ejl = new EcranJeuListener(jeu,this);
 			addMouseListener(ejl);
 			addMouseMotionListener(ejl);
 			
 			// on met le flag a faux
 			premierePartie = false;
 		}else{
 			labelArgent.setVisible(true);
 			labelCouleurJoueur.setVisible(true);
 			fond.setVisible(true);
 			ecranPlateau.setVisible(true);
 		}
 		
 	}
 	
 	public void cacherTousLesOngets(){
 		ongletJoueur.setVisible(false);
 		ongletVille.setVisible(false);
 		ongletMenu.setVisible(false);
 		ongletBatiment.setVisible(false);
 		ongletUnite.setVisible(false);
 	}
 	
 	public void afficherOngletJoueur(){
 		if (ongletJoueur.isVisible()){
 			ongletJoueur.setVisible(false);
 		}else{
 			cacherTousLesOngets();
 			ongletJoueur.setVisible(true);
 		}
 	}
 	
 	public void afficherOngletVille(){
 		if (ongletVille.isVisible()){
 			ongletVille.setVisible(false);
 		}else{
 			cacherTousLesOngets();
 			ongletVille.setVisible(true);
 		}
 	}
 	
 	public void afficherOngletMenu(){
 		if (ongletMenu.isVisible()){
 			ongletMenu.setVisible(false);
 		}else{
 			cacherTousLesOngets();
 			ongletMenu.setVisible(true);
 		}
 	}
 	
 	public void afficherOngletBatiment(){
 		if (ongletBatiment.isVisible()){
 			ongletBatiment.setVisible(false);
 		}else{
 			cacherTousLesOngets();
 			ongletBatiment.setVisible(true);
 		}
 	}
 	
 	public void afficherOngletUnite(){
 		if (ongletUnite.isVisible()){
 			ongletUnite.setVisible(false);
 		}else{
 			cacherTousLesOngets();
 			ongletUnite.setVisible(true);
 		}
 	}
 	
 	public void afficherEcranAttente(){
 		ecranAttenteTour.setVisible(true);
 	}
 	
 	public void cacherEcranAttente(){
 		ecranAttenteTour.setVisible(false);
 	}
 	
 	public void afficherEcranSauvegardePartie(){
 		ecranSauvegardePartie.setVisible(true);
 	}
 	
 	public void cacherEcranSauvegardePartie(){
 		ecranSauvegardePartie.setVisible(false);
 	}
 	
 	@Override
 	public void setJeu(JeuPanel jeu){
 		this.jeu = jeu;
 	}
 	
 	public void update(){
 		Client client = jeu.getClient();
 		Joueur joueur = client.getJoueur();
 		int argent = joueur.getArgent();
 		labelArgent.setText(Constante.formatArgent(argent));
 		
 		ongletJoueur.updateContent();
 		ongletVille.updateContent();
 		ongletMenu.updateContent();
 		ongletBatiment.updateContent();
 		ongletUnite.updateContent();
 		
 		ecranAttenteTour.repaint();
 	}
 	
 	public void afficherModeCreation(TypeElementPlateau type){
 		cacherTousLesOngets();
 		typeElementEnConstruction = type;
 		labelEnConstruction.setIcon(type.getIconMin());
 		labelEnConstruction.setVisible(true);
 	}
 	
 	public void cacherModeCreation(){
 		typeElementEnConstruction = null;
 		labelEnConstruction.setIcon(null);
 		labelEnConstruction.setVisible(false);
 	}
 	
 	public boolean isModeCreationBatiment(){
 		return (typeElementEnConstruction instanceof TypeBatiment);
 	}
 	
 	public boolean isModeCreationUnite(){
 		return (typeElementEnConstruction instanceof TypeUnite);
 	}
 	
 	public void setPositionSouris(int x,int y){
 		if (uniteEnDeplacement != null){
 			x -= Constante.DECALAGE_PLATEAU_X;
 			y -= Constante.DECALAGE_PLATEAU_Y;
 			
 			ecranAffichageDeplacement.setDeplacementAutorise(uniteEnDeplacement.deplacementPossibleVersPosition(x,y));
 			ecranAffichageDeplacement.setPositionSouris(x,y);
 		}
 		
 		Rectangle rect = labelEnConstruction.getBounds();
 		rect.setBounds(x - Constante.LARGEUR_CASE * 2 / 3,y - Constante.HAUTEUR_CASE * 2 / 3,(int)rect.getWidth(),(int)rect.getHeight());
 		labelEnConstruction.setBounds(rect);
 	}
 	
 	public TypeElementPlateau getTypeElementEnConstruction(){
 		return typeElementEnConstruction;
 	}
 	
 	public boolean isModeDeplacementUnite(){
 		return (uniteEnDeplacement != null);
 	}
 	
 	public void afficherModeDeplacementUnite(Unite unite){
 		uniteEnDeplacement = unite;
 		Case position = unite.getPosition();
 		int x = position.getX();
 		int y = position.getY();
 		ecranAffichageDeplacement.setPositionUnite(x,y);
 		ecranAffichageDeplacement.setPositionSouris(x,y);
 		ecranAffichageDeplacement.setVisible(true);
 	}
 	
 	public void cacherModeDeplacementUnite(){
 		uniteEnDeplacement = null;
 		ecranAffichageDeplacement.setVisible(false);
 	}
 	
 	public Unite getUniteEnDeplacement(){
 		return uniteEnDeplacement;
 	}
 	
 	public void afficherToolTip(ElementPlateau elt,int niveau){
 		tooltip.setElement(elt,niveau);
 		tooltip.setVisible(true);
 	}
 	
 	public void cacherToolTip(){
 		tooltip.setVisible(false);
 	}
 	
 	public void cacheTousLesEcrans(){
 		cacherTousLesOngets();
 		cacherModeCreation();
 		cacherModeDeplacementUnite();
 		cacherEcranAttente();
 		cacherEcranSauvegardePartie();
 	}
 	
 	public void cacherPlateau(){
 		
 		cacheTousLesEcrans();
		
 		tooltip.setVisible(false);
 		labelArgent.setVisible(false);
 		labelCouleurJoueur.setVisible(false);
 		fond.setVisible(false);
 		ecranPlateau.setVisible(false);
 	}
 }
