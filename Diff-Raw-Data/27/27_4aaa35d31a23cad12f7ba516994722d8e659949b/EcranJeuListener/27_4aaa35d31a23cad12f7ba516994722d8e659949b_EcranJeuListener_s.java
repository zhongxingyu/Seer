 package client.controller;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.SwingUtilities;
 
 import client.JeuPanel;
 import client.view.jeu.EcranJeu;
 
 import common.Constante;
 import common.Joueur;
 import common.Partie;
 import common.Translator;
 import common.ia.JoueurIA;
 import common.partie.batiment.Batiment;
 import common.partie.batiment.TypeBatiment;
 import common.partie.plateau.Case;
 import common.partie.unite.TypeUnite;
 import common.partie.unite.Unite;
 
 /**
  * @author omar
  */
 public class EcranJeuListener implements MouseListener,MouseMotionListener{
 	private JeuPanel jeu;
	private Joueur joueur;
 	private EcranJeu ecranJeu;
 	
	public EcranJeuListener(JeuPanel jeu,Joueur joueur,EcranJeu ecranJeu){
 		super();
 		this.jeu = jeu;
		this.joueur = joueur;
 		this.ecranJeu = ecranJeu;
 	}
 	
 	@Override
 	public void mouseDragged(MouseEvent e){
 		ecranJeu.setPositionSouris(e.getX(),e.getY());
 	}
 	
 	@Override
 	public void mouseMoved(MouseEvent e){
 		ecranJeu.setPositionSouris(e.getX(),e.getY());
 	}
 	
 	@Override
 	public void mouseClicked(MouseEvent e){
 	}
 	
 	@Override
 	public void mousePressed(MouseEvent e){
 		int x = e.getX();
 		int y = e.getY();
 		
 		final Partie partie = jeu.getClient().getPartie();
		Joueur joueurCourant = partie.getJoueurCourant();
 		
 		if (joueurCourant.equals(joueur)){
 			// si c'est a nous de jouer
 			
 			if (SwingUtilities.isRightMouseButton(e)){
 				// s'il y a un double clic droit
 				ecranJeu.cacherToolTip();
 				
 				// il y a un decalage entre la fenetre et le plateau d'où le Constante.DECALAGE_PLATEAU_X et le
 				// Constante.DECALAGE_PLATEAU_Y
 				x -= Constante.DECALAGE_PLATEAU_X;
 				y -= Constante.DECALAGE_PLATEAU_Y;
 				
 				// on recupere la case la plus proche du clic
 				Case position = partie.getPlateau().getCasePlusProche(x,y);
 				
 				// on recupere l'unite eventuellement presente sur la case
 				Unite unite = joueur.getUniteSurCase(position);
 				if (unite != null){
 					ecranJeu.afficherToolTip(unite,joueur.getNiveau(unite.getType()));
 				}else{
 					// sinon on essaye de recuperer le batiment sur lequel il a cliquer
 					Batiment batiment = joueur.getBatimentSurCase(position);
 					if (batiment != null){
 						ecranJeu.afficherToolTip(batiment,joueur.getNiveau(batiment.getType()));
 					}
 				}
 			}else if (e.getButton() == MouseEvent.BUTTON1){
 				ecranJeu.cacherToolTip();
 				
 				// clic gauche
 				// on test d'abord si l'utilisateur a cliquer sur le plateau, ou s'il a cliquer en dehors du plateau de jeu
 				if (x > Constante.DECALAGE_PLATEAU_X && x < (Constante.LARGEUR_PLATEAU + Constante.DECALAGE_PLATEAU_X)
 						&& y > Constante.DECALAGE_PLATEAU_Y && y < (Constante.HAUTEUR_PLATEAU + Constante.DECALAGE_PLATEAU_Y)){
 					// cas 1: l'utilisateur a cliquer sur le plateau de jeu
 					
 					// il y a un decalage entre la fenetre et le plateau d'où le Constante.DECALAGE_PLATEAU_X et le
 					// Constante.DECALAGE_PLATEAU_Y
 					x -= Constante.DECALAGE_PLATEAU_X;
 					y -= Constante.DECALAGE_PLATEAU_Y;
 					
 					// récuperation de la case du clic
 					// on recupere la case la plus proche du clic et on y ajoute le nouveau batiment
 					Case position = partie.getPlateau().getCasePlusProche(x,y);
 					
 					if (ecranJeu.isModeCreationBatiment()){
 						if (partie.peutConstruireBatimentPosition(position) && joueur.aUniteConstructionProche(position)){
 							TypeBatiment type = (TypeBatiment)ecranJeu.getTypeElementEnConstruction();
 							int montant = type.getPrix(joueur.getNiveau(type));
 							Batiment batiment = new Batiment(type,position);
 							joueur.ajouterBatiment(batiment);
 							joueur.decrementArgent(montant);
 						}else{
 							ecranJeu.cacherModeCreation();
 							jeu.notificationJoueur(Translator.translate("ZoneImpossibleConstruire"));
 						}
 					}else if (ecranJeu.isModeCreationUnite()){
 						if (joueur.presenceDeBatimentAProximitePosition(position)){
 							TypeUnite type = (TypeUnite)ecranJeu.getTypeElementEnConstruction();
 							int montant = type.getPrix(joueur.getNiveau(type));
 							Unite unite = new Unite(type,position);
 							joueur.ajouterUnite(unite);
 							joueur.decrementArgent(montant);
 						}else{
 							ecranJeu.cacherModeCreation();
 							jeu.notificationJoueur(Translator.translate("AbsenceBatimentAProximitePourCreeUnite"));
 						}
 					}else{
 						// le joueur est en train de selectionner une unité pour la déplacer
 						Unite unite = joueur.getUniteSurCase(position);
 						if (unite != null){
 							// on active le mode deplacement unite
 							ecranJeu.afficherModeDeplacementUnite(unite);
 						}
 					}
 					ecranJeu.cacherModeCreation();
 					jeu.getClient().update();
 				}else{
 					// cas 2 : l'utilisateur a cliquer en dehors du plateau de jeu
 					ecranJeu.cacherModeCreation();
 					ecranJeu.cacherModeDeplacementUnite();
 					
 					if (y <= 46){
 						// test des trois liens du haut
 						if (x >= 90 && x <= 275){
 							// lien joueur
 							ecranJeu.afficherOngletJoueur();
 						}else if (x >= 420 && x <= 605){
 							// lien ville
 							ecranJeu.afficherOngletVille();
 						}else if (x >= 750 && x <= 930){
 							// lien menu
 							ecranJeu.afficherOngletMenu();
 						}
 					}else if (x <= 60){
 						// test des deux liens à gauche
 						if (y >= 85 && y <= 275){
 							// lien batiement
 							ecranJeu.afficherOngletBatiment();
 						}else if (y >= 320 && y <= 510){
 							// lien unités
 							ecranJeu.afficherOngletUnite();
 						}
 					}else if (y >= 570){
 						// lien en bas du plateau
 						if (x >= 80 && x <= 330){
 							// lien passer tour
 							ecranJeu.cacheTousLesEcrans();
 							ecranJeu.afficherEcranAttente();
 							
 							if (partie.isSolo()){
 								// s'il s'agit d'une partie en mode solo
 								// on fait jouer l'IA
 								partie.passerTour();
 								JoueurIA joueurIA = (JoueurIA)partie.getJoueurCourant();
 								joueurIA.majTour();
 								joueurIA.jouer(partie);
 								
 								int random = (int)(3000 * Math.random()) + 2000;
 								Timer timer = new Timer();
 								timer.schedule(new TimerTask(){
 									@Override
 									public void run(){
 										// on fait passer le tour de l'IA
 										// apres un delais aléatoire
 										joueur.majTour();
 										partie.passerTour();
 										ecranJeu.cacheTousLesEcrans();
 										ecranJeu.update();
 									}
 								},random);
 							}else{
 								// s'il s'agit d'une partie en reseau
 								// on envois la partie au server
 							}
 							
 						}
 					}
 				}
 			}else{
 				// autre clic
 				ecranJeu.cacherModeCreation();
 				ecranJeu.cacherModeDeplacementUnite();
 				ecranJeu.cacherToolTip();
 			}
 		}else{
 			// ce n'est pas au tour du joueur courant du jouer
 			jeu.notificationJoueur(Translator.translate("PasAVotreTourDeJouer"));
 		}
 	}
 	
 	@Override
 	public void mouseReleased(MouseEvent e){
 		int x = e.getX();
 		int y = e.getY();
 		
 		// on verifie que le laché de clic s'est effectuer sur le plateau de jeu
 		if (x > Constante.DECALAGE_PLATEAU_X && x < (Constante.LARGEUR_PLATEAU + Constante.DECALAGE_PLATEAU_X)
 				&& y > Constante.DECALAGE_PLATEAU_Y && y < (Constante.HAUTEUR_PLATEAU + Constante.DECALAGE_PLATEAU_Y)){
 			
 			// il y a un decalage entre la fenetre et le plateau d'où le Constante.DECALAGE_PLATEAU_X et le
 			// Constante.DECALAGE_PLATEAU_Y
 			x -= Constante.DECALAGE_PLATEAU_X;
 			y -= Constante.DECALAGE_PLATEAU_Y;
 			
 			if (ecranJeu.isModeDeplacementUnite()){
 				// on été en train de deplacer une unite
 				// récuperation de la case du lacher du clic
 				Case position = jeu.getClient().getPartie().getPlateau().getCasePlusProche(x,y);
 				Unite unite = ecranJeu.getUniteEnDeplacement();
 				if (unite.deplacementPossibleVersPosition(x,y)){
 					int distance = (int)(position.getDistance(unite.getPosition()) / Constante.LARGEUR_CASE);
 					unite.decrementDeplacementRestant(distance);
 					unite.setPosition(position);
 				}
 				ecranJeu.cacherModeDeplacementUnite();
 				ecranJeu.update();
 			}
 		}else{
 			ecranJeu.cacherModeDeplacementUnite();
 		}
 	}
 	
 	@Override
 	public void mouseEntered(MouseEvent e){
 	}
 	
 	@Override
 	public void mouseExited(MouseEvent e){
 	}
 	
 }
