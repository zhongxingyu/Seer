 import java.util.ArrayList;
 import java.util.Collections;
 
 public class Jeu
 {
     // le sac contient des cubes
     private ArrayList<Cube> sac = new ArrayList<Cube> ();
     // La pioche contient des cartes de Type Ballon
     private ArrayList<Ballon> pioche = new ArrayList<Ballon>();
     // La dfausse contient des cartes de Type ballon
     private ArrayList<Ballon> defausse = new ArrayList<Ballon>();
     // Je ne sais pas si cela sera utile
     private ArrayList<Ballon> main = new ArrayList<Ballon>();
     
     private final int NBCARTE = 45;
     private final int NBCUBE = 45;
     private final int TAILLE_BALLON = 13;
     
     private int[][] tabBallon = {   { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },         // rouge
                                     { 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1 },         // jaune
                                     { 1, 1, 0, 1, 0, 1, 1, 1, 0, 1, 0, 1, 1 },         // vert
                                     { 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1 },         // bleu
                                     { 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1 },         // gris
                                 };
 
     // on initialise la pioche dans l'ordre
     public void initialiserPioche()
     {
         int j = 0;
         for (int i = 1; i <= TAILLE_BALLON; ++i)
         {
             for(Couleur coul : Couleur.values())
             {
                 if(tabBallon[j][i-1] == 1)
                    pioche.add (new Ballon (i, coul.getLibelle()));
                 j++;
             }
             j = 0;
         }
     }
     
     public void melangerPioche()
     {
         Collections.shuffle(pioche);
     }
     
     public void donnerCarte()
     {
         /*
         for (int i = 0, int j = pioche.size()-1 ; i < 8; ++i, --j)
         {
             main.add (pioche
             */
     }
     
     public void initialiserSac()
     {
         for (int i = 0; i < 13; ++i)
             sac.add (new Cube ("Rouge"));
         
         for (int i = 0; i < 11; ++i)
             sac.add (new Cube ("Jaune"));
             
         for (int i = 0; i < 9; ++i)
             sac.add (new Cube ("Vert"));
             
         for (int i = 0; i < 7; ++i)
             sac.add (new Cube ("Bleu"));
             
         for (int i = 0; i < 7; ++i)
             sac.add (new Cube ("Gris"));
             
     }
     
     public void melangerSac()
     {
         Collections.shuffle(sac);
     }
     
     public boolean defausseVersPioche()
     {
        if (pioche.isEmpty())
             return false;
             
 		pioche.addAll(defausse);
 		defausse.clear();
 		melangerPioche();
         
         return true;
     }
     
     // pour test
     public void afficherPioche()
     {
         for ( Ballon b: pioche ) System.out.println (b);
     }
     
     public static void main (String[] args)
     {
         Jeu j = new Jeu();
 		
 		j.initialiserPioche();
         j.afficherPioche();

        
        j.defausseVersPioche();
 		
		System.out.println("Apres defausse :");
        j.afficherPioche();
     }
 }       
