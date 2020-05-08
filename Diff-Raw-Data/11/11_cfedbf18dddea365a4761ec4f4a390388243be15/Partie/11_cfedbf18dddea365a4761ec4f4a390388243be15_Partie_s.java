 package fr.iutvalence.java.projets.tetris;
 
 import java.util.*;
 
 /**
  *  
  * Classe centrale de l'application : début du jeu 
  * TODO (différents selon des paramètrse choisis précédemment et fournis) 
  * affichage de la Map, des Formes, interaction avec le joueur 
  * @author duplanm
  */
 public class Partie
 {
 	/*TODO redéfinir l'affichage de la map en ascii (en incluant formeCourante, ce qui n'est pour 
 	 l'instant pas le cas dans toString de Map*/
 	/**
 	 * Appel de la classe Map avant initialisation et lancement de la partie
 	 */
 	public Map map;
 	
 	/**
 	 * Une forme, définie aléatoirement au cours du jeu
 	 */
 	private Forme formeCourante;
 
 	/**
 	 * Constructeur de la classe Partie
 	 * Crée une map vide avant l'initialisation et le lancement de la partie.
 	 */
 	public Partie()
 	{
 		this.map = new Map();
 	}
 	
 	/**
 	 * @param ms nombre de millisecondes à attendre
 	 */
 	private static void attendre(final int ms)
 	{
 		try
 		{
 			Thread.sleep(ms);
 		}
 		catch (InterruptedException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Méthode principale de Partie. 
 	 */
 	public void start()
 	{	
	/*	for (int x = 0 ; x < Map.LARGEUR_MAP - 4; x++)// pour le test
 			{
 				this.map.setCouleur(x, Map.HAUTEUR_MAP - 1, Couleur.ORANGE);
 				this.map.setCouleur(x, Map.HAUTEUR_MAP - 2, Couleur.ORANGE);
 			}
		this.map.setCouleur(0, Map.HAUTEUR_MAP - 3, Couleur.ORANGE);*/
 		
 		boolean perdu = false;
 		while (!perdu)
 		{
			this.formeCourante = randForme(3, 1);
 			if (this.formeCourante != null)
 			{
 				while (this.peutBouger(this.formeCourante, Deplacements.BAS))
 				{
 					System.out.println(this.toString());
 					// récupérer choix utilisateur
 					// executer choix utilisateur (d,g,b,r)	
 					this.formeCourante.descendre();
					Partie.attendre(100);
 				}
 				this.map.poserForme(this.formeCourante);
 				this.formeCourante = null;
 				this.map.supprLignes();				
 			}
 			else
 				perdu = true;
 		}
 		
 		System.out.println(this.toString());
 		System.out.println("PERDU ! ^^ ");
 	}
 	
 	/**
 	 * Renvoie une Forme aléatoire
 	 * @param x abscisses
 	 * @param y ordonnées
 	 * @return renvoie la forme générée aléatoirement ou null si il est impossible de générer cette forme à l'endroit indiqué
 	 */
 	private Forme randForme(int x, int y)
 	{
 		Forme f;
 		
 		int rand = (int) (Math.random() * 6);
 
 
 		switch (rand)
 		{
 			case 0:
 				f = new Barre(new Position(x,y));
 				break;
 			case 1:
 				f = new Cube(new Position(x,y));
 				break;
 			case 2:
 				f = new L(new Position(x,y));
 				break;
 			case 3:
 				f = new LInverse(new Position(x,y));
 				break;
 			case 4:
 				f = new S(new Position(x,y));
 				break;
 			case 5:
 				f = new Z(new Position(x,y));
 				break;
 			case 6:
 				f = new T(new Position(x,y));
 				break;
 			default:
 				f = new T(new Position(x,y));
 				break;
 		}
 		for (int i = 0 ; i < 4 ; i++)
 		{
 			if (this.map.getCouleur(f.getXBloc(i), f.getYBloc(i)) != Couleur.RIEN)
 				return null;
 		}
 		return f;
 	}
 	
 	/**
 	 * Renvoie vrai si f peut bouger dans une direction donnée (ni au bord de la map ni de bloc qui gène)
 	 * @param f la forme à tester
 	 * @param dir Quel déplacement cherche-t-on à faire ? Utilisé par un switch afin d'adapter le test en fonction
 	 * @return peut-elle descendre ?
 	 */
 	private boolean peutBouger(Forme f, Deplacements dir)
 	{
 		int x,y;
 		
 		for (int i = 0 ; i < 4 ; i++)
 		{
 			x = f.getXBloc(i);
 			y = f.getYBloc(i);
 
 			switch (dir)
 			{
 				case BAS:
 					if (y == Map.HAUTEUR_MAP - 1 || this.map.getCouleur(x, y + 1) != Couleur.RIEN)
 						return false;
 					break;
 				case GAUCHE:
 					if (x == 0 || this.map.getCouleur(x - 1, y) != Couleur.RIEN)
 						return false;
 					break;
 				case DROITE:
 					if (x == Map.LARGEUR_MAP - 1 || this.map.getCouleur(x + 1, y) != Couleur.RIEN)
 						return false;
 					break;
 				default :
 					return false;
 			}			
 		}
 		return true;
 	}
 	/**
 	 * renvoie une String contenant l'affichage ASCII de la map en incluant la forme courante 
 	 * Pour l'instant, tous les blocs sont représentés par le même caractère (à changer éventuellement)
 	 */
 	public String toString()
 	{
 		String res = "";
 		Position pos;
 		boolean unBlocDeformeCouranteEstSurLaPos;
 		for (int y = 0 ; y < Map.HAUTEUR_MAP ; y++)
 		{
 			for (int x = 0 ; x < Map.LARGEUR_MAP ; x++)
 			{
 				unBlocDeformeCouranteEstSurLaPos = false;
 				if (this.formeCourante != null)
 				{
 					for (int i = 0 ; i < 4 ; i++)
 					{
 						pos = new Position(this.formeCourante.getXBloc(i), this.formeCourante.getYBloc(i));
 						
 						if (pos.getX() == x && pos.getY() == y)
 						{
 							res += '#';
 							unBlocDeformeCouranteEstSurLaPos = true;
 						}											
 					}
 				}
 				if (!unBlocDeformeCouranteEstSurLaPos)
 				{
 					if (this.map.getCouleur(x, y) == Couleur.RIEN)
 						res += '°';
 					else
 						res += '#'; // l'éventuelle ligne à changer lorsque l'esthétique aura pris plus d'importance
 				}
 			}
 			res += '\n';			
 		}
 		return res;
 		
 	}
 	
 }
 
