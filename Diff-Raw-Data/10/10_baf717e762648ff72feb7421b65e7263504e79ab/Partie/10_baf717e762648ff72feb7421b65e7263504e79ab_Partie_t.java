 package gomoku.jeu;
 
 import java.lang.Integer;
 import java.util.Set;
 import java.util.Scanner;
 import java.util.Iterator;
 import gomoku.regles.Variante;
 import gomoku.regles.RegleCoup;
 import gomoku.regles.RegleAlignement;
 import gomoku.jeu.Grille;
 import gomoku.jeu.PierreCoordonnees;
 
 public class Partie {
 
   private JoueurHumain jNoir;
   private JoueurHumain jBlanc;
  private Plateau grille;
   private boolean premierCoup = true;
   private int doisJouer = Joueur.NOIR;
   private int gagnant;
 
   public Partie(JoueurHumain jNoir, JoueurHumain jBlanc, Plateau grille) {
     this.jNoir = jNoir;
     this.jBlanc = jBlanc;
     this.grille = grille;
   }
 
   public boolean coupAjouer() {
     while (!(this.jNoir.getNbCoups() == 0
           && this.jBlanc.getNbCoups() == 0))
       return true;
     return false;
   }
 
   public Coordonnees demanderCoor() {
       return this.aLaMain(Joueur.BLANC) ?
         demanderCoorJoueur(Joueur.BLANC) :
         this.demanderCoorJoueur(Joueur.NOIR);
   }
 
   public Coordonnees demanderCoorJoueur(int couleur) {
     String str = this.couleurIntToString(couleur);
     Scanner sc = new Scanner(System.in);
     System.out.println("\nJoueur " + str + "\n");
     System.out.println("Saisir la coordonnée x : ");
     int coorX = sc.nextInt();
     System.out.println("Saisir la coordonnée y : ");
     int coorY = sc.nextInt();
     return new PierreCoordonnees(coorX, coorY);
   }
 
   public String couleurIntToString(int couleur) {
     return couleur == Joueur.NOIR ? "NOIR" : "BLANC";
   }
 
   private boolean aLaMain(int couleur) {
     return couleur == this.doisJouer ? true : false;
   }
 
   public void placerPierreAuxCoor(Coordonnees c) {
    Variante v = ((Grille)grille).getVariante();
     RegleCoup r = v.verifCoup();
     if (this.premierCoup) {
       this.premierCoup = false;
       grille.placer(c, this.doisJouer);
       this.donnerLaMain();
     } else if (r.estValide(c, grille) &&
         grille.contenu(c) == Joueur.VIDE)
     {
       grille.placer(c, this.doisJouer);
       this.donnerLaMain();
     } else
       System.out.println("Vous ne pouvez pas placer ici.\n");
   }
 
   private void donnerLaMain() {
     this.doisJouer = this.aLaMain(Joueur.NOIR) ?
       Joueur.BLANC : Joueur.NOIR;
   }
 
   public void rafraichirGrille() {
     System.out.println(grille);
   }
 
   /*
    * Méthode estGagne.
    * Methode retournant un booléen lorsque la partie
    * est gagné par l'un des joueurs.
    * Une partie est gagné lorsque :
    *
    */
   public boolean estGagne() {
    Variante v = ((Grille)this.grille).getVariante();
     RegleAlignement regle = v.verifAlignement();
     Set<Alignement> align = grille.rechercherAlignements( this.doisJouer,
         regle.tailleMin());
     Iterator<Alignement> it = align.iterator();
     while (it.hasNext())
       if (regle.estGagnant(it.next(), this.grille)) return true;
     return false;
   }
 
   public String joueurGagnant() {
     return this.aLaMain(Joueur.NOIR) ? "BLANC" : "NOIR";
   }
 
 }
