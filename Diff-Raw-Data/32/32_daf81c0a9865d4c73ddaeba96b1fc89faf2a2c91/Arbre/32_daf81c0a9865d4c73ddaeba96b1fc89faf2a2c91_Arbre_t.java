 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ift615tp1;
 
 import connect5.Grille;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Antoine
 */
   public class Arbre {
     
     public static class Noeud {
             public Grille g;
             private int l = -1; //ligne du coup joué
             private int c = -1; //colonne du coup joué
             private Noeud parent;
             public Noeud voisinDroite;
             public List<Noeud> enfants;
         
             public int getLigne()
             {
                 return this.l;
             }
             public int getColonne()
             {
                 return this.c;
             }    
 	}
 
 	public Noeud racine;
 	public Noeud dernierEtage;
 
 	public Arbre(Grille grilleRacine, int profondeur, int joueur) {
 		racine = new Noeud();
 		racine.g = grilleRacine;
 		racine.enfants = new ArrayList<Noeud>();
 		genererEnfants(joueur, racine, profondeur);
 	}
 
 	public void ajouterProfondeur(int increment) {
 		Noeud noeud = dernierEtage; 
 
 		int joueur = (racine.g.nbLibre()%2==0)?'1':'2';
 
 		while (true) {
 			//if temps limite ? 
            			//break;
 			genererEnfants(joueur, noeud, increment);
 
 			if (noeud.voisinDroite == null)
 				break;
 			noeud = noeud.voisinDroite;
 		}
 	}
 
 	public void genererEnfants(int joueur, Noeud noeud, int profondeur) {
 		List vides = new ArrayList();
 		int nbligne = noeud.g.getData().length;
 		int nbcol = noeud.g.getData()[0].length;
 		Noeud voisinGauche = new Noeud();
 		for(int l=0;l<nbligne;l++) {
 			boolean premierNoeud = true;
 			for(int c=1;c<nbcol;c++) {
 				if(noeud.g.getData()[l][c]==0) {
 					Grille gg = noeud.g;
 					gg.set(l, c, joueur);
 
 					Noeud n = new Noeud();
 					n.parent = noeud;
 					n.l = l;
 					n.c = c;
 
 					racine.enfants.add(n);
 					if (premierNoeud) {
 						premierNoeud=false;
 						dernierEtage=n;
 					}
 					else {
 						voisinGauche.voisinDroite = n;
 					}
 					voisinGauche=n;
 
 					int nextJoueur = (joueur==1)?2:1; // prochain joueur avec différente couleur
 					genererEnfants(nextJoueur, n, profondeur-1);
 				}
 			}
 		}
 	}
}    
