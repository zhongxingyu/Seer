 package controleur;
 
 import java.awt.event.MouseEvent;
 
 import modele.Boule;
 import modele.Case;
 import modele.Couleur;
 import modele.Direction;
 import modele.Joueur;
 import modele.Modele;
 import modele.Plateau;
 import vue.BoutonRond;
 import vue.FenetreAbalone;
 import vue.PanneauJeu;
 import Utilitaire.Coord;
 import Utilitaire.CoordDouble;
 
 public class SuperController {
 
 	public static final int CLICGAUCHE = MouseEvent.BUTTON1;
 	public static final int CLICDROIT = MouseEvent.BUTTON3;
 	public static final int ips = 50;
 	public static final int temps = 200;
 	public static int nbBoulesDeplac = 0;
 
 	private Coord b1;
 	private Coord b2;
 	private Coord b3;
 	private FenetreAbalone fenetre;
 	private Modele modele;
 	private Etat etat;
 	private Coord sensDeuxBoules;
 
 	public void setEtat(controleur.Etat etat) {
 		this.etat = etat;
 	}
 
 	public Etat getEtat() {
 		return this.etat;
 	}
 
 	public SuperController(Modele modele) {
 		this.modele = modele;
 	}
 
 	public int compteCouleur(Direction delta, Coord depart, Couleur couleur) {
 		return this.compteCouleur(delta, depart.getY(), depart.getX(), couleur);
 	}
 
 	public int compteCouleur(Direction delta, int iDep, int jDep, Couleur couleur) {
 		int nbCoul = 0;
 
 		Coord parcours = new Coord(jDep, iDep);
 
 		Plateau plateau = modele.getPlateau();
 
 		while (plateau.getCase(parcours) != null && plateau.getCase(parcours).estOccupee()
 				&& plateau.getCase(parcours).getBoule().getCouleur() == couleur) {
 
 			nbCoul++;
 			parcours.setX(parcours.getX() + delta.getX());
 			parcours.setY(parcours.getY() + delta.getY());
 		}
 		return nbCoul;
 	}
 
 	public void verifierBoules() {
 		Plateau plateau = modele.getPlateau();
 		// verification boule hors jeu
 		for (int i = 0; i < Plateau.HEIGHT; i++) {
 			for (int j = 0; j < Plateau.WIDTH; j++) {
 				if (plateau.getCase(i, j).getBord() && plateau.getCase(i, j).estOccupee()) {
 					for (Joueur joueur : modele.getTabJoueurs()) {
 						if (plateau.getCase(i, j).getBoule().getCouleur() == joueur.getCouleur()) {
 							joueur.setBoulesDuJoueurEjectees(joueur.getBoulesDuJoueurEjectees() + 1);
 						}
 					}
 					plateau.getCase(i, j).setBoule(null);
 					modele.verifierVictoire();
 				}
 			}
 		}
 	}
 
 	public void sourisRelachee(MouseEvent e) {
 
 		BoutonRond bouton = ((BoutonRond) e.getSource());
 		PanneauJeu panneau = fenetre.getPanneau();
 		Plateau plateau = modele.getPlateau();
 
 		System.out.println("clic : ligne " + bouton.getCoordI() + ", colonne " + bouton.getCoordJ());
 
 		Direction[] lesDir = Direction.values();
 
 		BoutonRond tmp; /* tous les boutons temporaires de parcours circulaire */
 
 		switch (etat) {
 		case NORMAL:
 			switch (e.getButton()) {
 			case CLICGAUCHE:
 				// coordonnees depart
 				b1 = bouton.getCoord();
 				etat = Etat.SELECTIONLIGNE;
 				System.out.println("etat : DEPLACEMENTLIGNE");
 
 				// cacher
 				panneau.cacherBoutons();
 
 				// afficher cercle voisins
 				for (Direction dir : lesDir) {
 					Coord dest = new Coord(bouton.getCoordJ() + dir.getX(), bouton.getCoordI() + dir.getY());
 
 					tmp = plateau.getCase(dest).getBouton();
 					if (tmp != null && !plateau.getCase(dest).getBord()) {
 						tmp.setCliquableGauche(true);
 						tmp.setVisible(true);
 					}
 				}
 				break;
 			case CLICDROIT:
 				// coordonnees depart
 				b1 = bouton.getCoord();
 
 				panneau.cacherBoutons();
 
 				BoutonRond boutonDep = plateau.getCase(b1).getBouton();
 				boutonDep.setVisible(true);
 				boutonDep.setSelectionne(true);
 
 				// afficher cercle voisins
 				for (Direction dir : lesDir) {
 					Case caseDest = plateau.getCase(bouton.getCoordI() + dir.getY(), bouton.getCoordJ() + dir.getX());
 
 					tmp = caseDest.getBouton();
 					if (tmp != null && !caseDest.getBord() && caseDest.estOccupee()
 							&& caseDest.getBoule().getCouleur() == plateau.getCase(b1).getBoule().getCouleur()) {
 						tmp.setCliquableDroit(true);
 						tmp.setVisible(true);
 					}
 				}
 
 				etat = Etat.SELECTIONLATERAL;
 				System.out.println("etat : SELECTIONLATERAL");
 				break;
 			default:
 				break;
 			}
 			break;
 		case SELECTIONLIGNE:
 			switch (e.getButton()) {
 			case CLICGAUCHE:
 				Coord arrive = bouton.getCoord();
 				sensDeuxBoules = new Coord(arrive.getX() - b1.getX(), arrive.getY() - b1.getY());
 				Direction dir = Direction.toDirection(sensDeuxBoules);
 
 				/* premiere ligne de boules */
 				Couleur couleurDepart = plateau.getCase(b1).getBoule().getCouleur();
 				int nbCouleurActuelle = compteCouleur(dir, b1, couleurDepart);
 				int nbCouleurOpposee = 0;
 
 				Case caseDeFinCouleurActuelle = plateau.getCase(b1.getY() + nbCouleurActuelle * sensDeuxBoules.getY(),
 						b1.getX() + nbCouleurActuelle * sensDeuxBoules.getX());
 
 				/* seconde ligne de boules */
 				if (caseDeFinCouleurActuelle.estOccupee()) {
 					Couleur couleurArr = caseDeFinCouleurActuelle.getBoule().getCouleur();
 					nbCouleurOpposee = compteCouleur(dir, b1.getY() + nbCouleurActuelle * sensDeuxBoules.getY(),
 							b1.getX() + nbCouleurActuelle * sensDeuxBoules.getX(), couleurArr);
 				}
 
 				nbBoulesDeplac = nbCouleurActuelle + nbCouleurOpposee;
 				boolean deplacementPossible = true;
 
 				// verification de la case qui suit la derniere
 				Case caseFinale = plateau.getCase(b1.getY() + nbBoulesDeplac * sensDeuxBoules.getY(), b1.getX()
 						+ nbBoulesDeplac * sensDeuxBoules.getX());
 				if (caseFinale != null && caseFinale.estOccupee()) {
 					deplacementPossible = false;
 				}
 
 				System.out.println("nbCouleurAcuelle = " + nbCouleurActuelle + ", nbCouleurOpposee = "
 						+ nbCouleurOpposee);
 
 				// deplacement reel
 				if (nbCouleurActuelle <= Plateau.MAXDEPLACEMENT && nbCouleurOpposee < nbCouleurActuelle
 						&& deplacementPossible) {
 					deplacerLigneBoules(nbBoulesDeplac, sensDeuxBoules);
 					verifierBoules();
 				}
 
 				/* nettoyage et reaffichage */
 				panneau.visibiliteBoutonVide();
 
 				System.out.println("etat : NORMAL");
 				etat = Etat.NORMAL;
 				break;
 			default:
 				break;
 			}
 			break;
 		case SELECTIONLATERAL:
 			switch (e.getButton()) {
 			case CLICDROIT:
 				if (bouton.isSelectionne())
 					break;
 
 				Coord arrivee = new Coord(bouton.getCoordJ(), bouton.getCoordI());
 				sensDeuxBoules = new Coord(arrivee.getX() - b1.getX(), arrivee.getY() - b1.getY());
 
 				panneau.cacherBoutons();
 
 				/* caseDecalDepart - caseDepart - caseArrivee - caseDecalArrivee */
 				Case caseDecalArrivee = plateau.getCase(arrivee.getY() + sensDeuxBoules.getY(), arrivee.getX()
 						+ sensDeuxBoules.getX());
 				Case caseDecalDepart = plateau.getCase(b1.getY() - sensDeuxBoules.getY(),
 						b1.getX() - sensDeuxBoules.getX());
 				Case caseDepart = plateau.getCase(b1);
 				Case caseArrivee = plateau.getCase(arrivee);
 
 				/* affichage des boutons decal */
 
 				if (caseDecalArrivee.estOccupee()
 						&& caseDecalArrivee.getBoule().getCouleur() == caseArrivee.getBoule().getCouleur()) {
 					caseDecalArrivee.getBouton().setCliquableDroit(true);
 					caseDecalArrivee.getBouton().setVisible(true);
 				}
 
 				if (caseDecalDepart.estOccupee()
 						&& caseDecalDepart.getBoule().getCouleur() == caseDepart.getBoule().getCouleur()) {
 					caseDecalArrivee.getBouton().setCliquableDroit(true);
 					caseDecalDepart.getBouton().setVisible(true);
 				}
 
 				Case caseDest;
 
 				/* affichage des boutons lateraux */
 				for (Direction dir : lesDir) {
 
 					caseDest = plateau.getCase(caseDepart.getBouton().getCoordI() + dir.getY(), caseDepart.getBouton()
 							.getCoordJ() + dir.getX());
 
 					tmp = caseDest.getBouton();
 					if (tmp != null && !caseDest.getBord() && !caseDest.estOccupee()
 							&& !caseDest.getBouton().getCoord().equals(caseDecalArrivee.getBouton().getCoord())
 							&& !caseDest.getBouton().getCoord().equals(caseDecalDepart.getBouton().getCoord())) {
 						tmp.setCliquableGauche(true);
 						tmp.setVisible(true);
 					}
 
 					caseDest = plateau.getCase(caseArrivee.getBouton().getCoordI() + dir.getY(), caseArrivee
 							.getBouton().getCoordJ() + dir.getX());
 
 					tmp = caseDest.getBouton();
 					if (tmp != null && !caseDest.getBord() && !caseDest.estOccupee()
 							&& !caseDest.getBouton().getCoord().equals(caseDecalArrivee.getBouton().getCoord())
 							&& !caseDest.getBouton().getCoord().equals(caseDecalDepart.getBouton().getCoord())) {
 						tmp.setCliquableGauche(true);
 						tmp.setVisible(true);
 					}
 				}
 
 				System.out.println("etat : SELECTIONLATERAL2");
 				etat = Etat.SELECTIONLATERAL2;
 
 				break;
 			default:
 				break;
 			}
 			break;
 		case SELECTIONLATERAL2:
 			switch (e.getButton()) {
 			case CLICGAUCHE:
				// for (Direction dir : lesDir) {
 				Case caseDest = plateau.getCase(bouton.getCoordI() + sensDeuxBoules.getY(), bouton.getCoordJ()
 						+ sensDeuxBoules.getX());

 				tmp = caseDest.getBouton();
				if (tmp != null && !caseDest.getBord() && tmp.isMouseOver() && !caseDest.estOccupee()) {
					System.out.println("je suis dans le if");
					/* déplacer les 2 boules */
 
 					panneau.cacherBoutons();
 					panneau.visibiliteBoutonVide();
 					System.out.println("etat : NORMAL");
 					etat = Etat.NORMAL;
 
 					break;
 				}
				// }
 
 				break;
 			case CLICDROIT:
 				Coord coordBoule3 = bouton.getCoord();
 				Case caseBoule3 = plateau.getCase(coordBoule3);
 				tmp = caseBoule3.getBouton();
 
 				System.out.println("etat : SELECTIONLATERAL3");
 				etat = Etat.SELECTIONLATERAL3;
 				break;
 			default:
 				break;
 			}
 			break;
 		case SELECTIONLATERAL3:
 			switch (e.getButton()) {
 			case CLICGAUCHE:
 				etat = Etat.NORMAL;
 				break;
 			default:
 				break;
 			}
 			break;
 		default:
 			break;
 
 		}
 	}
 
 	private void deplacerLigneBoules(int nbBoules, Coord delta) {
 
 		nbBoulesDeplac = nbBoules;
 		System.out.println(nbBoules + " boule(s) a deplacer");
 		// la derniere boule est la premiere deplacee
 		Coord coordDepla = new Coord(b1.getX() + (nbBoules - 1) * delta.getX(), b1.getY() + (nbBoules - 1)
 				* delta.getY());
 		while (nbBoules > 0) {
 			nbBoules--;
 			try {
 				deplacerBouleDirection(Direction.toDirection(delta), coordDepla);
 
 			} catch (DeplacementException e1) {
 				e1.printStackTrace();
 			}
 
 			coordDepla.setX(coordDepla.getX() - delta.getX());
 			coordDepla.setY(coordDepla.getY() - delta.getY());
 		}
 	}
 
 	public void sourisEntree(MouseEvent e) {
 		BoutonRond bouton = ((BoutonRond) e.getSource());
 		bouton.setMouseOver(true);
 		if (etat == Etat.SELECTIONLATERAL2) {
 			Coord coordDepla = new Coord(bouton.getCoord().getX() + sensDeuxBoules.getX(), bouton.getCoord().getY()
 					+ sensDeuxBoules.getY());
 			if (!modele.getPlateau().getCase(coordDepla).getBouton().isVisible()) {
 				coordDepla = new Coord(bouton.getCoord().getX() - sensDeuxBoules.getX(), bouton.getCoord().getY()
 						- sensDeuxBoules.getY());
 			}
 			modele.getPlateau().getCase(coordDepla).getBouton().setMouseOver(true);
 			fenetre.repaint();
 		}
 		if (etat == Etat.SELECTIONLATERAL3) {
 			// TODO On verra pus tard...
 		}
 	}
 
 	public void sourisSortie(MouseEvent e) {
 		// BoutonRond bouton = ((BoutonRond) e.getSource());
 		for (int i = 0; i < Plateau.HEIGHT; i++) {
 			for (int j = 0; j < Plateau.WIDTH; j++) {
 				BoutonRond bout = fenetre.getModele().getPlateau().getCase(i, j).getBouton();
 				if (bout != null) {
 					bout.setMouseOver(false);
 				}
 			}
 		}
 		fenetre.repaint();
 	}
 
 	public void setVue(FenetreAbalone fenetre) {
 		this.fenetre = fenetre;
 	}
 
 	public void deplacerBouleDirection(Direction dir, Coord coordCase) throws DeplacementException {
 		System.out.println("deplacement de (" + coordCase.getX() + ";" + coordCase.getY() + ") en direction ("
 				+ dir.getX() + ";" + dir.getY() + ")");
 
 		if (coordCase.getX() < 0 || coordCase.getX() >= Plateau.WIDTH || coordCase.getY() < 0
 				|| coordCase.getY() >= Plateau.HEIGHT) {
 			throw new DeplacementException("case debut invalide (<0 | >" + Plateau.HEIGHT + ")");
 		}
 
 		Case caseActuelle = modele.getPlateau().getCase(coordCase);
 		Coord coordCaseSuivante = modele.getPlateau().coordCaseSuivant(dir, coordCase);
 
 		if (!caseActuelle.estOccupee()) {
 			throw new DeplacementException("case debut non occupee");
 		}
 		if (modele.getPlateau().getCase(coordCaseSuivante).estOccupee()) {
 			throw new DeplacementException("case arrivee occcupee");
 		}
 
 		Boule bouleADeplacer = caseActuelle.getBoule();
 
 		int periode = temps / nbBoulesDeplac / ips;
 
 		CoordDouble delta = new CoordDouble((double) (dir.getX()) / ips, (double) (dir.getY()) / ips);
 
 		for (int i = 0; i < ips; i++) {
 
 			bouleADeplacer.getCoord().setCoord(bouleADeplacer.getCoord().getY() + delta.getY(),
 					bouleADeplacer.getCoord().getX() + delta.getX());
 
 			fenetre.repaint();
 
 			fenetre.getPanneau().paintImmediately(0, 0, fenetre.getPanneau().getWidth(),
 					fenetre.getPanneau().getHeight());
 
 			try {
 				Thread.sleep(periode);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		}
 
 		bouleADeplacer.getCoord().setCoord(coordCaseSuivante.getY(), coordCaseSuivante.getX());
 		modele.getPlateau().getCase(coordCaseSuivante).setBoule(caseActuelle.getBoule());
 		caseActuelle.setBoule(null);
 
 	}
 }
