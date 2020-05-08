 package com.utbm.smallWorld.gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Rectangle;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 
 import com.utbm.smallWorld.Joueur;
 import com.utbm.smallWorld.Partie;
 import com.utbm.smallWorld.Peuple;
 import com.utbm.smallWorld.Plateau;
 import com.utbm.smallWorld.Pouvoir;
 import com.utbm.smallWorld.Territoire;
 
 public class Game extends JFrame {
 	/** Stub */
 	private static final long serialVersionUID = 1L;
 
 	/** Image de background de la fenêtre */
 	public static Icon BACKGROUND = null;
 	
 	/** Couleur de background des différents joueurs */
 	public static final Color[] JOUEUR_BACKGROUND = {Color.YELLOW, Color.PINK, Color.CYAN, Color.GREEN, Color.GRAY};
 
 	/** Couleur d'écriture des différents joueurs */
 	public static final Color[] JOUEUR_FOREGROUND = {Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK, Color.WHITE};
 	
 	/** Fenêtre en cours */
 	private static Game instance;
 	
 	/** Partie en cours */
 	private Partie partieEnCours;
 	
 	/** Zone contenant les informations sur le joueur en cours */
 	private JTextArea playerInfo;
 	
 	/** Entete de la section contenant les infos joueurs */
 	private JLabel headerJoueur;
 
 	/** Entete de la section contenant les différentes actions possibles */
 	private JLabel headerAction;
 	
 	/** Entete de la section contenant des informations, notamment sur les territoires */
 	private JLabel headerInfo;
 	
 	/** Section contenant des informations sur les territories */
 	private JPanel infoPanel;
 
 	/** Zone de texte contenant les informations sur les territoires */
 	private JTextArea infoTx;
 	
 	/** Liste des territoires de la map */
 	private List<TerritoireCase> territoires;
 	
 
 	/** Génération de l'image de background */
 	static {
 		try {
 			BACKGROUND = new ImageIcon(ImageIO.read(Game.class.getResource("/res/win/background.jpg")));
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 	};
 	
 	/**
 	 * Constructeur
 	 */
 	public Game() {
 		instance = this;
 		
 		// Config
 		setTitle("SmallWorld UTBM");
 		setSize(1280, 768);
 		setLocationRelativeTo(null);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setResizable(false);
 		setLayout(null);
 		
 		// Fond d'écran
 		JLabel background = new JLabel();
 		background.setIcon(Game.BACKGROUND);
 		background.setBounds(0, 0, 1274, 740);
 		
 		setContentPane(background);
 		
 		// Initialisation de la partie
 		partieEnCours = Partie.getInstance();
 		territoires = new LinkedList<TerritoireCase>();
 		
 		setVisible(true);
 		
 		// Affiche la fenêtre nombre de joueur
 		askNbJoueur();
 		
 		// Construction des éléments de la fenêtre
 		buildBackground();
 		buildPlayerPanel();
 		buildInfoPanel();
 		buildActionPanel();
 		buildTerritoires();
 		
 		// Mise à jour des informations affichées
 		majInfos();
 		
 		partieEnCours.nouveauTour();
 	}
 	
 	/**
 	 * Affiche la fenêtre de demande du nombre de joueur
 	 */
 	public void askNbJoueur() {
 		// Création de la fenêtre de choix
 		WinMenu nbMenu = new WinMenu("Combien de joueurs pour cette partie ?");
 
 		nbMenu.newItem("2 joueurs", 2);
 		nbMenu.newItem("3 joueurs", 3);
 		nbMenu.newItem("4 joueurs", 4);
 		
 		// Affiche la fenêtre de choix
 		int nbJoueur = nbMenu.open();
 		
 		// Si la fenêtre a été fermée, on termine le processus
 		if (nbJoueur < 0) {
 			System.exit(0);
 		}
 		
 		// On charge le plateau correspondant au nom de joueur sélectionné
 		loadPlateau(nbJoueur);
 		
 		// On crée les joueurs
 		for (int i = 0; i < nbJoueur; i++) {
 			String name;
 			
 			do {
 				name = Prompt.ask("Choissiez un nom pour le joueur n°" + (i + 1));
 			} while (name.length() == 0);
 			
 			Joueur j = new Joueur(name, Partie.DEFAULT_MONNAIE);
 			j.setIndice(i);
 			
 			partieEnCours.ajouterJoueur(j);
 		}
 	}
 	
 	
 	/**
 	 * Affiche la fenêtre de confirmation d'abandon d'un territoire
 	 */
 	public void askAbandon(Territoire t) {
 		// Création de la fenêtre de choix
 		WinMenu confMenu = new WinMenu("Abandonner le territoire ?");
 
 		confMenu.newItem("Oui", 0);
 		confMenu.newItem("Non", 1);
 		
 		// Affiche la fenêtre de choix
 		int conf = confMenu.open();
 
 		
 		if( conf == 0){
 			partieEnCours.getJoueurEnCours().getPeuple().abandonTerritoire(t);
 			majInfos();
 		}
 		
 		
 	}
 	
 	
 	/**
 	 * Affiche la fenêtre de confirmation d'attaque d'un territoire
 	 */
 	public void askAttaque(Territoire t) {
 		// Création de la fenêtre de choix
 		WinMenu confMenu = new WinMenu("Attaquer le territoire ?");
 
 		confMenu.newItem("Oui", 0);
 		confMenu.newItem("Non", 1);
 		
 		// Affiche la fenêtre de choix
 		int conf = confMenu.open();
 
 		
 		if( conf == 0){
 			partieEnCours.getJoueurEnCours().attaquer(t);
 			majInfos();
 		}
 		
 	}
 	
 
 	
 
 	/**
 	 * Mise à jours des informations affichées sur la fenêtre
 	 */
 	public void majInfos() {
 		// Un premier try au cas où on appelerait cette méthode un peu trop tot
 		try {
 			int i, 
 				argent = 0,
 				nbTerritoire = 0,
 				nbTerritoireDeclin = 0,
 				nbUnite = 0,
 				nbUniteEnMain = 0;
 			
 			String name = "Undefined";
 			
 			try {
 				// Récupération de l'indice du joueur en cours
 				i = partieEnCours.getIndexJoueurEnCours();
 				
 				// Récupération des informations sur le joueur en cours
 				Joueur j = partieEnCours.getJoueurEnCours();
 				
 				name = j.getNom();
 				argent = j.getArgent();
 				
 				// Sur son peuple
 				Peuple p = j.getPeuple();
 				
 				if (p != null) {
 					nbTerritoire = j.getPeuple().getTerritoiresOccupes().size();
 					nbUnite = j.getPeuple().getNbUnite();
 					nbUniteEnMain = j.getPeuple().getNbUniteEnMain();
 				}
 				
 				// Sur son peuple en déclin
 				p = j.getPeupleDeclin();
 				
 				if (p != null) {
 					nbTerritoireDeclin = p.getTerritoiresOccupes().size();
 				}
 			}
 			catch (Exception e) {
 				i = Game.JOUEUR_BACKGROUND.length - 1;
 			}
 			
 			// MAJ
 			playerInfo.setText("");
 			playerInfo.append("\n");
 			playerInfo.append("UVs.............. " + argent + "\n");
 			playerInfo.append("Territoires...... " + nbTerritoire + (nbTerritoireDeclin > 0 ? " (+ " + nbTerritoireDeclin + ")" : "") + "\n");
 			playerInfo.append("Unités totales... " + nbUnite + "\n");
 			playerInfo.append("Unités en main... " + nbUniteEnMain + "\n");
 			
 			// Mise à jour des couleurs
 			headerAction.setBackground(JOUEUR_BACKGROUND[i]);
 			headerAction.setForeground(JOUEUR_FOREGROUND[i]);
 			
 			headerInfo.setBackground(JOUEUR_BACKGROUND[i]);
 			headerInfo.setForeground(JOUEUR_FOREGROUND[i]);
 			
 			headerJoueur.setBackground(JOUEUR_BACKGROUND[i]);
 			headerJoueur.setForeground(JOUEUR_FOREGROUND[i]);
 			headerJoueur.setText(name);
 			
 			// Mise à jour des territoires
 			Iterator<TerritoireCase> it = territoires.iterator();
 			while (it.hasNext()) {
 				it.next().majInfos();
 			}
 			
 			// Useless?
 			repaint();
 		}
 		catch (Exception e) {}
 	}
 
 	
 	/**
 	 * Chargement du plateau correspondant au paramètre
 	 * @param nbJoueur nombre de joueur
 	 */
 	private void loadPlateau(int nbJoueur) {
 		// TODO
 		try {
 			Icon plIm = new ImageIcon(ImageIO.read(Game.class.getResource("/res/maps/map2p.png")));
 			
 			Plateau plateau = new Plateau(plIm);
 			
 			partieEnCours.setPlateau(plateau);
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	
 	/**
 	 * 
 	 */
 	public void selectionPeuple() {
 		try {
 			Joueur j = partieEnCours.getJoueurEnCours();
 			
 			WinMenu choixPeuple = new WinMenu(j.getNom() + " (" + j.getArgent() + " $), choisissez votre peuple :");
     		
     		int i = -1;
     		List<Class<? extends Peuple>> ppl = partieEnCours.getPeuplesDispo();
     		List<Class<? extends Pouvoir>> pov = partieEnCours.getPouvoirsDispo();
     		List<Integer> argent = partieEnCours.getArgentPeuple();
     		
     		Iterator<Class<? extends Peuple>> itPpl = ppl.iterator();
     		Iterator<Class<? extends Pouvoir>> itPov = pov.iterator();
     		Iterator<Integer> itArgent = argent.iterator();
     		
     		while (itPpl.hasNext() && ++i < 6) {
     			Class<? extends Peuple> clPpl = itPpl.next();
     			Class<? extends Pouvoir> clPov = itPov.next();
     			int arg = itArgent.next().intValue();
     			
     			Peuple insPpl = clPpl.newInstance();
     			Pouvoir insPov = clPov.newInstance();
     			
     			// TODO Description
     			JLabel it = choixPeuple.newItem(arg + "$ - " + insPpl.getNom() + " (" + insPpl.getNbUniteDepart() + " unit.) + " + insPov.getNom() + " (" + insPov.getNbUniteApporte() + " unit.)", i);
     			it.setHorizontalAlignment(JLabel.LEFT);
     		}
     		
     		// Affiche la fenêtre de choix
     		int indexPeuple;
     		int cout = 0;
     		
     		do {
     			indexPeuple = choixPeuple.open();
     			
     			if (indexPeuple >= 0) {
     				cout = indexPeuple - argent.get(indexPeuple).intValue();
     				
     				if (cout > j.getArgent()) {
     					choixPeuple.setHeadTitle("Vous n'avez pas assez d'argent ! (" + j.getArgent() + " $)");
     					indexPeuple = -1;
     				}
     			}
     		} while (indexPeuple < 0);
     		
     		// Création du peuple
     		Peuple pl = ppl.get(indexPeuple).newInstance();
     		pl.setPouvoir(pov.get(indexPeuple).newInstance());
     		pl.setNbUnite(pl.getNbUniteDepart() + pl.getPouvoir().getNbUniteApporte());
     		pl.setNbUniteEnMain(pl.getNbUnite());
     		
     		// Attribution du joueur
     		j.setPeuple(pl);
     		j.setArgent(j.getArgent() - cout);
     		
     		// Màj des listes
     		partieEnCours.getPeuplesPris().add(ppl.get(indexPeuple));
     		partieEnCours.getPouvoirsPris().add(pov.get(indexPeuple));
     		
     		ppl.remove(indexPeuple);
     		pov.remove(indexPeuple);
     		argent.remove(indexPeuple);
     		
     		// màj argent
     		for (int a = 0; a < indexPeuple; a++) {
     			argent.set(a, argent.get(a) + 1);
     		}
     		
     		majInfos();
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	/**
 	 * Création du fond de la fenêtre
 	 */
 	private void buildBackground() {
 		JLabel map = new JLabel();
 		map.setIcon(partieEnCours.getPlateau().getIcon());
 		map.setBounds(0, 0, 1274, 740);
 		
 		setContentPane(map);
 	}
 	
 	
 	/**
 	 * Création de la zone Information Player
 	 */
 	private void buildPlayerPanel() {
 		JPanel playerPanel = new JPanel();
 		playerPanel.setBounds(0, 450, 385, 200);
 		playerPanel.setPreferredSize(new Dimension(385, 200));
 		playerPanel.setBackground(new Color(31, 31, 31));
 		playerPanel.setForeground(Color.WHITE);
 		playerPanel.setOpaque(true);
 		
 		FlowLayout flow = new FlowLayout();
 		flow.setVgap(0);
 		playerPanel.setLayout(flow);
 		
 		headerJoueur = new JLabel("Joueur 1");
 		headerJoueur.setHorizontalAlignment(JLabel.CENTER);
 		headerJoueur.setPreferredSize(new Dimension(385, 30));
 		headerJoueur.setBackground(Color.YELLOW);
 		headerJoueur.setOpaque(true);
 		
 		playerPanel.add(headerJoueur);
 		
 		playerInfo = new JTextArea();
 		playerInfo.setPreferredSize(new Dimension(340, 150));
 		playerInfo.setForeground(Color.WHITE);
 		playerInfo.setOpaque(false);
 		playerInfo.setEditable(false);
 		playerInfo.setFont(new Font(Font.MONOSPACED, 0, 15));
 		
 		playerPanel.add(playerInfo);
 		
 		getContentPane().add(playerPanel);
 	}
 	
 	
 	/**
 	 * Création de la zone Actions
 	 */
 	private void buildActionPanel() {
 		JPanel actionPanel = new JPanel();
 		actionPanel.setBounds(0, 650, 385, 90);
 		actionPanel.setPreferredSize(new Dimension(385, 90));
 		actionPanel.setBackground(new Color(31, 31, 31));
 		actionPanel.setForeground(Color.WHITE);
 		actionPanel.setOpaque(true);
 		
 		FlowLayout flow = new FlowLayout();
 		flow.setVgap(0);
 		actionPanel.setLayout(flow);
 
 		headerAction = new JLabel("Actions");
 		headerAction.setHorizontalAlignment(JLabel.CENTER);
 		headerAction.setPreferredSize(new Dimension(385, 30));
 		headerAction.setBackground(Color.YELLOW);
 		headerAction.setOpaque(true);
 		
 		actionPanel.add(headerAction);
 		
 		JPanel actions = new JPanel(new GridBagLayout());
 		actions.setPreferredSize(new Dimension(385, 60));
 		actions.setOpaque(false);
 		
 		GridBagConstraints constraint = new GridBagConstraints();
 		
 		actions.add(new JButton("Déclin"), constraint);
 		actions.add(new JButton("Finir tour"), constraint);
 		actions.add(new JButton("Fin redéploiement"), constraint);
 		
 		actionPanel.add(actions);
 
 		getContentPane().add(actionPanel);
 	}
 	
 	
 	/**
 	 * Création de la zone Information Territoire
 	 */
 	private void buildInfoPanel() {
 		infoPanel = new JPanel();
 		infoPanel.setBounds(1274-400, 450, 400, 290);
 		infoPanel.setPreferredSize(new Dimension(400, 290));
 		infoPanel.setBackground(new Color(31, 31, 31));
 		infoPanel.setForeground(Color.WHITE);
 		infoPanel.setOpaque(true);
 		infoPanel.setVisible(false);
 		
 		FlowLayout flow = new FlowLayout();
 		flow.setVgap(0);
 		infoPanel.setLayout(flow);
 
 		headerInfo = new JLabel("Informations Territoire");
 		headerInfo.setHorizontalAlignment(JLabel.CENTER);
 		headerInfo.setPreferredSize(new Dimension(400, 30));
 		headerInfo.setBackground(Color.YELLOW);
 		headerInfo.setOpaque(true);
 		
 		infoPanel.add(headerInfo);
 		
 		infoTx = new JTextArea();
 		
 		infoTx.setPreferredSize(new Dimension(355, 240));
 		infoTx.setForeground(Color.WHITE);
 		infoTx.setOpaque(false);
 		infoTx.setEditable(false);
 		infoTx.setFont(new Font(Font.MONOSPACED, 0, 15));
 		
 		infoPanel.add(infoTx);
 		
 		getContentPane().add(infoPanel);
 	}
 	
 	
 	/**
 	 * Création des zones des territoires
 	 */
 	private void buildTerritoires() {
 		// TODO nombre de player
		List<TerritoireCase> territoires = SQLite.createTerritoires(2);
 		
 		for (TerritoireCase t : territoires) {
 			getContentPane().add(t);
 		}
 		
 		/*
 		getContentPane().add(new TerritoireCase(new Rectangle(5, 5, 191, 100)));
 		getContentPane().add(new TerritoireCase(new Rectangle(5, 105, 191, 173)));
 		
 		getContentPane().add(new TerritoireCase(new Rectangle(196, 91, 216, 150)));
 		getContentPane().add(new TerritoireCase(new Rectangle(412, 91, 60, 150)));
 		
 		getContentPane().add(new TerritoireCase(new Rectangle(472, 91, 137, 85)));
 		getContentPane().add(new TerritoireCase(new Rectangle(609, 91, 138, 85)));
 		getContentPane().add(new TerritoireCase(new Rectangle(747, 91, 190, 85)));
 
 		getContentPane().add(new TerritoireCase(new Rectangle(472, 176, 158, 65)));
 		getContentPane().add(new TerritoireCase(new Rectangle(630, 176, 157, 65)));
 		getContentPane().add(new TerritoireCase(new Rectangle(787, 176, 150, 65)));
 
 		getContentPane().add(new TerritoireCase(new Rectangle(937, 91, 165, 340)));
 		getContentPane().add(new TerritoireCase(new Rectangle(1102, 91, 160, 170)));
 		getContentPane().add(new TerritoireCase(new Rectangle(1102, 261, 160, 170)));
 
 		getContentPane().add(new TerritoireCase(new Rectangle(196, 241, 187, 145)));
 		getContentPane().add(new TerritoireCase(new Rectangle(383, 241, 108, 190)));
 		getContentPane().add(new TerritoireCase(new Rectangle(491, 241, 99, 190)));
 		getContentPane().add(new TerritoireCase(new Rectangle(590, 241, 99, 190)));
 		getContentPane().add(new TerritoireCase(new Rectangle(689, 241, 99, 190)));
 		getContentPane().add(new TerritoireCase(new Rectangle(788, 241, 149, 190)));
 
 		getContentPane().add(new TerritoireCase(new Rectangle(410, 431, 150, 104)));
 		getContentPane().add(new TerritoireCase(new Rectangle(410, 535, 150, 200)));
 		getContentPane().add(new TerritoireCase(new Rectangle(560, 431, 170, 290)));
 		 */
 	}
 	
 	
 	/**
 	 * Affiche la zone d'information
 	 * @param tx Texte à afficher
 	 */
 	public void showInfo(String tx) {
 		infoTx.setText("\n" + tx);
 		
 		infoPanel.setVisible(true);
 	}
 	
 	
 	/**
 	 * Masque la zone d'information
 	 */
 	public void hideInfo() {
 		infoPanel.setVisible(false);
 	}
 	
 	/**
 	 * @return the instance
 	 */
 	public static Game getInstance() {
 		return instance;
 	}
 
 	
 	
 	
 	public static void main(String[] args) {
 		new Game();
 	}
 
 }
