 package fr.mercredymurderparty.client;
 
 import java.util.Observable;
 
 import fr.mercredymurderparty.outil.BaseDeDonnees;
 
 public class CompteurTempsJoueur extends Observable implements Runnable 
 {
 
 // ----- ATTRIBUTS ----- //
 
 	private int tempsAffiche; // le temps qui s'affiche, en secondes
 	private int tempsEcoule; // le temps coul depuis la connexion du joueur, en secondes
 	private int tempsMaj; //le temps entre chaque mj de temps du joueur
 	private int ancienTemps; // Ancien temps du joueur
 	private Thread threadCompteur; // tout est dans le titre
 	private Personnage perso; // Classe personnage
 	private CoeurClient coeurClient; // Classe CoeurClient
 
 	
 // ----- CONSTRUCTEUR ----- //
 
 	/**
 	 * Constructeur de la classe CompteurTemps
 	 * @param _temps le temps initial
 	 */
 	public CompteurTempsJoueur(Personnage _perso, int _tempsAffiche, CoeurClient _coeurClient) 
 	{
 		// On recupre l'instance du personnage
 		this.perso = _perso;
 		tempsAffiche = _tempsAffiche;
 		ancienTemps = _tempsAffiche;
 		tempsEcoule = 0;
 		tempsMaj = 20;
 		coeurClient = _coeurClient;
 	}
 
 	// ----- METHODES ----- //
 
 	/**
 	 * Mthode qui dmarre un nouveau thread pour le Compteur de temps
 	 */
 	final public void demarrer()
 	{
 		if (threadCompteur == null) 
 		{
 			threadCompteur = new Thread(this);
 			threadCompteur.start();
 		}
 	}
 	
 	/**
 	 * Mthode qui met  jour le temps du joueur sur la bdd
 	 */
 	final public void majTemps()
 	{
 		
 			// On va chercher le temps en BDD
 			int ancienTemps = perso.recupererTemps(perso.getLogin()); 
 			
 			// Mettre a jours le personnage avec le nouveau temps
 			BaseDeDonnees.envoyerInstruction("UPDATE personnage SET temps = " + (ancienTemps - tempsEcoule) + " WHERE login = '" + perso.getLogin() + "'", coeurClient);
 	}
 	
 	/**
 	 * Mthode qui stop le thread du Compteur de temps
 	 */
 	@SuppressWarnings("deprecation")
 	final public void arreter() 
 	{
 		if (threadCompteur != null) 
 		{
 			// On va chercher le temps en BDD
 //			int ancienTemps = perso.recupererTemps(perso.getLogin()); 
 			BaseDeDonnees.envoyerInstruction("UPDATE personnage SET temps = " + (ancienTemps - tempsEcoule%tempsMaj) + " WHERE login = '" + perso.getLogin() + "'", coeurClient);
 			threadCompteur.stop();//le .interrupt ne fonctionne pas.
 			threadCompteur = null;
 		}
 	}
 
 	/**
 	 * Mthode xcute par le thread de la classe ( dcrmentation du temps de 1 toutes les mille milisecondes )
 	 */
 	public void run() 
 	{
 		while (true) 
 		{
 			try 
 			{
 				Thread.sleep(1000); // attendre 1 seconde
 				if (tempsAffiche > 0)
 				{
 					tempsAffiche--;
 					tempsEcoule++;
 					setChanged(); // J'ai chang
 					notifyObservers(this); // Je prviens ceux qui m'observent que j'ai chang
 					if(tempsEcoule % tempsMaj == 0) //toutes les 20 secondes, je met le temps du joueur  jour
 					{
 						majTemps();
						System.out.println("Temps mis  jour" + tempsAffiche );
 					}
 				}
 				else if (coeurClient.estConnecte())
 				{
 					coeurClient.deconnexion();
 				}
 			} 
 			catch (Exception ex) 
 			{
 				System.out.println("Une erreur est survenue au niveau d'un compteur de temps : " + ex.getMessage());
 				arreter();
 			}
 		}
 		
 	}
 
 	/**
 	 * Mthode qui gnre une chaine de caractre en HHhMMminSSsec  partir de la valeur de l'attribut temps
 	 * @return une chaine de caractre pour l'affichage du temps.
 	 */
 	final public String chaineCaracTemps()
 	{
 		int heure = tempsAffiche/3600;
 		int min = (tempsAffiche%3600)/60;
 		int sec = (tempsAffiche%3600) % 60;
 		String stringHeure = Integer.toString(heure);
 		if (heure < 10)
 		{
 			stringHeure = "0"+stringHeure;
 		}
 		String stringMin = Integer.toString(min);
 		if (min < 10)
 		{
 			stringMin = "0"+stringMin;
 		}
 		String stringSec = Integer.toString(sec);
 		if (sec < 10)
 		{
 			stringSec = "0"+stringSec;
 		}
 		String chaineTemps = stringHeure+":"+stringMin+":"+stringSec;
 		return chaineTemps;
 	}
 
 	
 // ----- Getters & Setters -----
 
 	/**
 	 * Retourne la valeur du compteur
 	 * @return le valeur du compteur
 	 */
 	public int getTempsAffiche() 
 	{
 		return tempsAffiche;
 	}
 	
 }
