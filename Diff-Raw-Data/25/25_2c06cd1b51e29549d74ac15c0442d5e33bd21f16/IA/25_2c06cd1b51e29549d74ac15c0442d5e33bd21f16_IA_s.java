 import java.util.*;
 import java.io.*;
 import java.lang.*;
 
 public class IA {
 	// temps : java.util.*; <= Timer	
 	// vkaenemi@ee.ethz.ch - ssilvan@ee.ethz.ch
 	
 	long iterator;
 	
 /**
  * permettra de calculer l'efficacite...
  */ 
 	long tempsDeReflexion;
 	
 /**
  * coefficients
  */ 
 	private double ejection = 3.0;
 	private double position = 2.0;
 	
 /**
  * numero de joueur
  */ 
 	public int numJoueur 	= 0;
 	
 /**
  * son petit nom
  */
 	private String nom;
 /**
  * sur quelle profondeur l'ia effectuera-t-elle ses recherches.
  */
 
 	private int profondeur;
 	/** Niveau de difficulte de l'IA*/
 	private int niveauIA;
 /**
  * la strategie a appliquer : on stockera des coefficient (des poids) associes a chacun des facteurs.
  */
 	private byte heuristiques[];	
 /**
 * facteurs permettant d'attribuer un score a un coup : grace a ces facteurs on pourra evaluer un plateau.
  *   
  *	connectivite
  *	ejection : le poids de ejection augmente avec le nombre de billes qu'on a deja ejectees
  *   	
  *   
  *   
  *
  *    
  */	
  	float[] facteursDeterminants;
  /**
   * il faut une fonction qui permet de recuperer l'ensemble des mouvements possibles par rapport a un Plateau envoye en parametre : on utilisera la fonction d'evaluation dessus.
   */
  	Mouvement[] mouvementsPossibles;
 	Plateau plateau;
  	
 
 /**
  * evaluation du joueurs ? permet de savoir s'il est plutot du genre defensif ou offensif afin de determiner une heuristique.
  */
 	
 	
 	
 	public IA(int numJoueur) {

 		this.profondeur = 2;
 		this.nom  = "neuneu";
 		this.numJoueur = numJoueur;
 		this.niveauIA = 1;
 		this.iterator = 0;
 	}
 /*
 	public IA(String nom) {
 		this.nom = nom;
 	}
 */
 	
     public Mouvement jouer(Plateau p) {
 
 		long tempsAvt=0;
         long tempsApres=0;
         
         long tempsDeReflexion=0; // normalement ici ca devrait etre this mais comme jouer est static on peut pas...
         
         tempsAvt = System.currentTimeMillis();
         Mouvement meilleurMouvement = meilleurCoup(new Plateau(p), p.mouvementsValides(this.numJoueur)); // on appelle minimax sur chaque mouvement possible, dans un premier temps...
         tempsApres = System.currentTimeMillis();
         
         tempsDeReflexion = tempsApres-tempsAvt;
         
 		System.out.println("temps avant:"+tempsAvt+" apres:"+tempsApres+" temps mis:"+tempsDeReflexion);
 		
 		
 		
 		
         return meilleurMouvement;
     }
 	
 	private Mouvement meilleurCoup(Plateau p, Vector<Mouvement> mouvementsValides) {
 
 
 
  	    // on evalue chaque racine.
 		Mouvement meilleur = mouvementsValides.get(0);
 		double meilleurScore = 0.0;
 		double scoreActuel = 0.0;
 		for(int i = 0; i < mouvementsValides.size(); i++) {	
 			Plateau temporaire = new Plateau(p);		
 			temporaire.effectuer(mouvementsValides.get(i));
 			temporaire.setJoueur();
 			temporaire.setNumCoup();
 			temporaire.setScore();
 
 			
 /*			Node n = new Node(new Mouvement((byte)1, (byte)1, (byte)1), temporaire);
 			Node meilleur = new Node();
 			meilleur = n.negamax(1);
 			System.out.println(meilleur);
 */			
 
 			scoreActuel = negamax(temporaire, this.profondeur, Double.MIN_VALUE, Double.MAX_VALUE);
 			System.out.println("scoreActuel"+scoreActuel);
 			
 			
 			if(scoreActuel > meilleurScore) {
 				meilleurScore = scoreActuel;
 				meilleur = mouvementsValides.get(i);
 			}
 		}
 	
 		System.out.println("meilleur:"+meilleurScore);
 		System.out.println(meilleur);
 		return meilleur;		
 		
 		
 	}
 	
 	
 	private double negamax(Plateau p, int profondeur, double alpha, double beta) {
 		double valeur, best;
 		Vector<Mouvement> mouvementsValides = new Vector<Mouvement>();
 				
 
 
 		
 		if(profondeur == 0) {
 			return fonctionEvaluation(p);
 		}
 		
 		Plateau temporaire = new Plateau(p);
 		mouvementsValides = temporaire.mouvementsValides(p.getJoueurActuel());
 
 		if(/*temporaire.getJoueurActuel() != this.numJoueur*/profondeur%2 == 1)
 			best = Double.MAX_VALUE;
 		else
 			best = Double.MIN_VALUE;
 		
 		for(int i = 0 ; i < mouvementsValides.size() ; i++) {
 			temporaire.effectuer(mouvementsValides.get(i));
 			temporaire.setJoueur();
 			temporaire.setNumCoup();
 			temporaire.setScore();
 			valeur = negamax(temporaire, profondeur-1, alpha, beta);
 			
 			if(/*temporaire.getJoueurActuel() != this.numJoueur*/profondeur%2 == 1) { 
 				if(valeur < best) { // on minimise
 					best = valeur;
 					if(best<beta) {
 						beta=best;
 						if(alpha>beta)
 							return best; // coupure alpha
 					}
 				}
 			}
 			else if (valeur > best) { // on maximise
 				best = valeur;
 				if(best>alpha) {
 					alpha=best;
 					if(alpha>beta)
 						return best; // coupure beta
 				}
 			}
 		}
 		//System.out.println("best"+best);
 		return best;
 	}
 	
 	
 /*	
 	private double negamax(Plateau p, int profondeur, double alpha,  double beta) {
 		Plateau temporaire;
 		
 	
 		System.out.println("negamax()");
 		if(profondeur == 0) {
 			return fonctionEvaluation(p); // fonction d'evaluation les coups de l'ia donc on doit avoir une profondeur qui termine avec les coups de l'ia donc ici on doit tester avec deep = 2
 		}
 		else {
 			System.out.println("->dans le else");
 			Plateau temporaire = new Plateau(p);		
 			Vector<Mouvement> mouvementsValides = temporaire.mouvementsValides(p.getJoueurActuel());
 			for(int i = 0; i < mouvementsValides.size(); i++) {
 				System.out.println("->dans le for : "+i);
 				temporaire.effectuer(mouvementsValides.get(i));
 				temporaire.setJoueur();
 				temporaire.setNumCoup();
 				temporaire.setScore();
 				alpha = max(alpha, -negamax(temporaire, profondeur-1, -alpha, -beta));
 				System.out.println("alpha vs negamax : "+alpha+" "+p.getJoueurActuel());
 				if (alpha > beta) { // alpha-beta
 					return alpha;
 				}
 			}
 		}
 		System.out.println("negamax a choisi le plateau ayant la valeur "+alpha);
 		return alpha;
 	}
 	
 	private double max(double a, double b) {
 		return a>b?a:b;
 	}
 	
 /*	
 	private double minimax(Plateau temporaire, int profondeur, int numJoueur) { // le plateau  passer doit tre un temporaire (pas le vrai) !
 		System.out.println("minimax()");
 		System.out.println("profondeur->"+profondeur);
 		System.out.println("numJoueur->"+numJoueur);		
 		System.out.println("mon nomJoueur->"+numJoueur);
 		double score = 0;
 		double minimax = 0;
 		
 		// on recupere les mouvements valides pour ce node.
 		Vector<Mouvement> mouvementsValides = temporaire.mouvementsValides(numJoueur-1);
 		System.out.println(mouvementsValides.size());
 		System.out.println(numJoueur);
 		for(int i=0; i<mouvementsValides.size();i++) {
 			if(profondeur > 0) { // on fait remonter les valeurs !
 				System.out.println("on va effectuer un mouvement !");
 				Plateau temporaire2 = new Plateau(temporaire);
 				temporaire2.effectuer(mouvementsValides.get(i));
 				temporaire2.setScore();
 				if (temporaire2.partie.getNbBillesAEjecter() ==temporaire2.getScore(this.numJoueur)) {
 					if(numJoueur == this.numJoueur)
 						return Double.MAX_VALUE;
 					else
 						return Double.MIN_VALUE;
 				}
 				else
 				{
 				
 					// on continue de descendre vers les feuilles...
 					score=minimax(temporaire2,profondeur-1,(numJoueur+1)%2);
 					if(numJoueur == this.numJoueur)
 					{
 						if(score<minimax)
 						{
 							  score=minimax;
 						}
 					}
 					else
 					{
 						if(score>minimax)
 						{
 							  score=minimax;
 						}
 					}
                 }
             }
 			else { // on doit evaluer les feuilles
 					score = fonctionEvaluation(new Plateau(temporaire), mouvementsValides.get(i));
 					System.out.println(i+":"+score);
 			}			
 		}
 		System.out.println("score->"+score);
 		return score; // on doit retourner le pire score puisqu'on commence avec les coups adverses ?
 	}
 */	
 	
 	private double fonctionEvaluation(Plateau p/*, Mouvement m*/) {	 // sera private et pas static : elle evalue un plateau
 
 		double 	position	= 1.1;
 		
 		
 		
 		// correspond au score cumule de toutes les billes.
 		// le score positionnel d'une bille correspond au minimum des 6 distances vis a vis du bord : un deplacement correspond a score+=1.
 		
 /*		
 		int typePartie = 0; // 0 => debut de partie, 1 => midgame, 2 => fin de partie
 		
 		if (typePartie == 0) {
 			
 		}
 */
 		Plateau temporaire = new Plateau(p);
 /*		temporaire.effectuer(m);
 		temporaire.setJoueur();
 		temporaire.setNumCoup();
 		temporaire.setScore();
 */		double valeurPlateau = 1;
 		if(temporaire.cases[12].getContenu()!=0)
 			valeurPlateau = 55;
 		
 /*		
 		if(temporaire.score[temporaire.getJoueurActuel()] != p.score[temporaire.getJoueurActuel()]) // sortie d'une bille 
 			valeurPlateau*=this.ejection;
 */		
 
 //		System.out.println("fonctionEvaluation()->"+valeurPlateau);	
 		return valeurPlateau;
 		
 	}
 
 	private double evaluerSumito(Mouvement m) {
 	/*
 		if(
 	
 		// determine si l'adversaire pourra s'en sortir au prochain coup.
 		if() {
 			scoreSumito *= 2;
 		}
 	
 	*/
 		return 1.0;
 	}
 	
 	private double evaluerSecurite() {
 		return 1.0;
 	}
 	
 	private double trouverBillesInutiles() {
 		return 1.0;
 	}
 	
 /*	
 	private regroupementAuCentre() {
 		double position = 5.0; // on augmente de faon considerable le coefficient associe a la position.
 	}
 */	
 	
 	
 /**
  * rechercher les billes en danger (lorsque l'adversaire n'a qu'une seule bille a ejecter on regarde d'abord si il a des billes a ejecter pour ne pas perdre de temps)
  */ 
 
 	private Vector billesEnDanger (Plateau p) {
 		Vector billesEnDanger = new Vector();
 /*
 	ici on doit simplement ajouter au vecteur toute bille impliquee par un mouvement de l'adversaire qui amene une bille dans cases[0]
 */		
 		return billesEnDanger;
 	}
 
 
 
 
 
 	
 	
 /**
  * alpha-beta
  */
 
  
  
  
  
  
 /**
  * minamax
  */
  /*
 
 	private Mouvement minimax(Plateau p, int profondeur, int numJoueur) {
 	
 		Plateau temporaire = new Plateau(p);
 		
 		if(profondeur > 0) { // on fait remonter les valeurs !
 			// on recupere les mouvements valides pour ce node.
 			Vector<Mouvement> mouvementsValides = temporaire.mouvementsValides(numJoueur);
 			int[] scores = new int[mouvementsValides.size()];
 		}
 		else { // on doit valuer les feuilles
 			
 		}
 		return meilleurMouvement; // on doit retourner le meilleur Mouvement
 	} 
 
     private int minimax(Plateau p, int profondeur, int numJoueur)
     {
         int max;
         int minimax = 0;
         Plateau temporaire = new Plateau(p);
 		Vector<Mouvement> mouvementsValides = temporaire.mouvementsValides(numJoueur);
         
         if(tour == this.numJoueur)
             max=Integer.MIN_VALUE;
         else
             max=Integer.MAX_VALUE;
 
         if (profondeur>0)
         {
             for(int i=0; i<mouvementsValides.size();i++)
             {
                     boolean gagne = false;
                     if (temporaire.partie.getNbBillesAEjecter() == )
                         gagne = true;
                     if(gagne)
                     {
                         if(tour == this.numJoueur)
                             return Integer.MAX_VALUE;
                         else
                             return Integer.MIN_VALUE;
 
                     }
                     else
                     {
                         minimax=minimax(,profondeur-1,(numJoueur+1)%2);
                         if(tour == this.numJoueur)
                         {
                             if(max<minimax)
                             {
                                   max=minimax;
            
                             }
                         }
                         else
                         {
                             if(max>minimax)
                             {
                                   max=minimax;
                                   
                             }
                         }
 
 
                     }
 
                  }
              
              }
         else
             max=fonctionEvaluation(p, numJoueur);
         
         return max;
     }
 */
    
 
  
 /**
  * genetique : on garde les poids (associes aux facteurs) du vainqueur et on genere son nouveau challenger en prenant ses poids comme base (on applique une tres legere evolution)
  * il existe differentes manieres d'evoluer donc il nous faudra differentes branches d'evolution (une partie est declaree nulle au bout de 300 coups, et est consideree comme une defaite pour l'ia : ainsi on aura une approche offensive)
  */ 
  
 /**
  * recherche du coup gagnant sur une certaine profondeur
  */
  
  
 /**
  * strategies de jeu : heuristique.
  *   defini les coefficients associes aux facteurs.
  *  
  * algos :
  * 1/ tente de trouver le meilleur moyen de regrouper les billes au centre (cela peut passer par une pseudo-attaque si le style du joueur!="defenseur" ou par un deplacement simple)
  * 2/ 
  *
  *	
  * chercher la victoire en x coups : explore des branches en mettant ses parametres concentres sur la recherche de sortie de bille (utile en fin de partie).
  * 
  * 
  */	
 /* 
 	private score setHeuristique(byte h) {
 		this.heuristique = h;
 	}
 */	
 
 	
 	
 	
 	
 /**
  * methodes AVANT une partie.
  */
 /* 
 	public preparerPartie() {
 		// les infos de la partie : variante, (blitz), .	
 	}
 	
 	public creerPartie() {
 		// commme un joueur : on attend	une connexion  la partie.
 		
 	}
 	
 	public chercherPartie() {
 		// comme un joueur : on veut recuperer la liste des parties en attente de joueur.	
 	}
 */	
 /**
  * opening : lorsque la fonction d'evaluation sera pas mal il serait interessant de generer les ouvertures sur une profondeur importante.
  */
  
  
 /**
  * il faut une methode pour forcer l'ia a jouer si elle est consideree trop lente : la methode est donc public
  * on va simplement parcourir la liste des resultats des evaluations et
  */
  /*
  	public joue() {
  		foreach(JTree) {
  			
  		}
  	}
  */
 }
