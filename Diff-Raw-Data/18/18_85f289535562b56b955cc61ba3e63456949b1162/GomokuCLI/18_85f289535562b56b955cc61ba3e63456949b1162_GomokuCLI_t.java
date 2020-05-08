 package gomoku.cli;
 
 import gomoku.jeu.Grille;
 import gomoku.regles.Gomoku;
 import gomoku.jeu.JoueurHumain;
 import gomoku.jeu.Joueur;
 import gomoku.jeu.Partie;
 import gomoku.jeu.Coordonnees;
 
 public class GomokuCLI {
 
   public static void main (String[] args) {
     Partie partie = new Partie(new JoueurHumain(Joueur.NOIR),
                           new JoueurHumain(Joueur.BLANC),
                           new Grille(new Gomoku()));
     while (partie.coupAjouer()) {
       Coordonnees c = partie.demanderCoor();
      if (!partie.placerPierreAuxCoor(c))
        System.out.println("Vous ne pouvez pas placer ici.");
       partie.rafraichirGrille();
     }
     if (partie.estGagne()) {
       System.out.println("félicitation joueur " +
           partie.joueurGagnant());
     } else {
       System.out.println("Partie nulle !");
     }
   }
 }
