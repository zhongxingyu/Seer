 package Projet.Jeu;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import Projet.Liste.*;
 import Projet.Carte.*;
 import Projet.Joueur.*;
 import Projet.Couleur.*;
 import Projet.Plateau.*;
 
 public class Jeu
 {
     /*********************/
     /***** ATTRIBUTS *****/
     /*********************/
     
     // Les cartes Trophes
     private ArrayList<Trophee> listeTrophee = new ArrayList<Trophee>();
 
     private Pioche pioche       = new Pioche();
     private Defausse defausse   = new Defausse();
     private Sac sac             = new Sac();
     private Joueur joueur1      = new Joueur();
     private Joueur joueur2      = new Joueur();
     private Plateau plateau     = new Plateau();
     
     private final int NB_TUILE = 4;
     private final int NB_CARTE_PAR_JOUEUR = 8;
     
     private int[][] tabBallon = {   { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },         // rouge
                                     { 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1 },         // jaune
                                     { 1, 1, 0, 1, 0, 1, 1, 1, 0, 1, 0, 1, 1 },         // vert
                                     { 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1 },         // bleu
                                     { 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1 },         // gris
                                 };
 
     public void initialiserJeu()
     {
         // initialisation de la pioche
         pioche.initialiserPioche(tabBallon);
         pioche.melangerPioche();
         pioche.afficherPioche();
         System.out.println("\n");
         
         // initialisation du sac
         sac.initialiserSac(tabBallon);
         sac.melangerSac();
         
         // initialisation de la liste des trophes
         initialiserListeTrophee();
         
         // donne des cartes au joueurs
         donnerCarteAuxJoueurs();
         joueur1.afficherMain();
         System.out.println("\n");
         joueur2.afficherMain();
         
         // on initialise le plateau
         initialiserPlateau();
         initialiserCubeSurTuile();
     }
     
     /*********************************/
     /*** METHODES POUR INITIALISER ***/
     /*********************************/
     
     public void donnerCarteAuxJoueurs()
     {
         for (int i = 0; i < NB_CARTE_PAR_JOUEUR; ++i)
         {
             pioche.distribuerCarte(joueur1);
             pioche.distribuerCarte(joueur2);
         }
     }
     
     public void initialiserPlateau()
     {
         for (int i = 0; i < NB_TUILE; ++i)
         {
             if ( i % 2 != 0)
                 plateau.ajouterTuile(new Tuile(new Paysage("Plaine", "Montage")));
             else
                 plateau.ajouterTuile(new Tuile(new Paysage("Montage", "Plaine")));
         }
     }
     
     public void initialiserCubeSurTuile()
     {
         for (int i = 0; i < NB_TUILE; ++i)
         {
             plateau.getTuile().ajouterCubeSurPaysage(sac.getDernierElement());
             sac.supprimerDernierElement();
         }
     }
     
     public void afficherVosCartes(Joueur joueur)
     {
         joueur.afficherMain();
     }
     
     public boolean DefausseVersPioche()
     {
         if (!pioche.estVide())
             return false;
             
         ArrayList<Ballon> inter = new ArrayList<Ballon> (defausse.getDefausse());
         for (Ballon b: inter)
             pioche.ajouterElement(b);
             
         return true;
     }
     
     /*********************************/
     /*** INITIALISATION DES LISTES ***/
     /*********************************/
     
     public void initialiserListeTrophee()
     {
         listeTrophee.add( new Trophee(3, Couleur.GRIS.getLibelle()));
         listeTrophee.add( new Trophee(4, Couleur.BLEU.getLibelle()));
         listeTrophee.add( new Trophee(5, Couleur.VERT.getLibelle()));
         listeTrophee.add( new Trophee(6, Couleur.JAUNE.getLibelle()));
         listeTrophee.add( new Trophee(7, Couleur.ROUGE.getLibelle()));
     }
     
     /***********/
     /** AUTRE **/
     /***********/
     
     public void donnerCubeAuJoueur(Joueur joueur)
     {
         for (int i = 0; i < plateau.getTuile().getAttribut(); ++i)
         {
             joueur.ajouterCube(plateau.getTuile().getPaysage().getElement());
             plateau.getTuile().getPaysage().supprimerElement();
         }
     }
     
     public boolean PeutAvoirUnTrophee(Joueur joueur,Couleur couleur)
     {
 		for (Trophee t: listeTrophee)
 			if (t.getCouleur().equals(couleur.getLibelle()))
				if (t.getNumero() == joueur.getNbCube(couleur))
 					return true;
 
 		return false;
     }       
     
     public void CubeEnTrophee()
     {
         
     }
     
     public void afficherMenu()
 	{
 		System.out.println ( "\n\n\n" );
 		System.out.println ( "*******************************" );
 		System.out.println ( "**           MENU            **" );
 		System.out.println ( "*******************************" );
 		
 		System.out.println ();
         
         System.out.println ( " 1. Regarder Vos Cartes "         );
         System.out.println ( " 2. Dfausser "                   );
         System.out.println ( " 0. Quitter"                      );
         
         System.out.println ();
 		System.out.print   ( "      votre choix : "            );
     }
     
     
 
     // pour test
     public void listerTrophee()     {   for (Trophee t: listeTrophee)   System.out.println (t);  }
     
     /************/
     /*** MAIN ***/
     /************/
     
     public static void main (String[] args)
     {
         Jeu j = new Jeu();
         j.initialiserJeu();
     }
 }       
