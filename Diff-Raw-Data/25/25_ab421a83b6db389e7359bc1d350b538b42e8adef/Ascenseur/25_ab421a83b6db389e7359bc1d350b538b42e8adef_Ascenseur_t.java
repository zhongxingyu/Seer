 package ascenseur;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 public class Ascenseur {
 	
 	/**
 	 * @return
 	 */
 	public Ascenseur(int idAscenseur, int positionActuelle,int xtemps) {
 		// TODO Auto-generated constructor stub
 		this.idAscenseur = idAscenseur;
 		this.positionActuelle = positionActuelle;
 		this.positionRepo = positionActuelle;
 		
 		this.nbPersonne = 0;
 		this.arret = true; //Arret
 		this.consommation = 0;
 		this.monte = false; // Descend
 		this.tempsParcoursAscenseur = 0;
 		this.xtemps = xtemps;
 		this.enRepositionnement = false;
 		
 		this.tabAppelAtraiter = new ArrayList<Appel>();
 		this.tabAppelsTraites = new ArrayList<Appel>();
 		this.tabDestination = new ArrayList<Integer>();
 		this.tabDestinationTemp = new HashSet<Integer>();
 		
 		System.out.println("Ascenseur "+ idAscenseur +" :Etage " + positionActuelle);
 		
 	}
 	
 	/**
 	 * @param args
 	 */
 	// VARIABLES
 	private int idAscenseur;
 	private boolean arret; //0 arret; 1 en deplacement
 	private ArrayList<Appel> tabAppelAtraiter;
 	private ArrayList<Appel> tabAppelsTraites;
 	private ArrayList<Integer> tabDestination;
 	private HashSet<Integer> tabDestinationTemp;
 	private int positionActuelle;
 	private int nbPersonne;
 	private int consommation;
 	private boolean monte; //0 monte ; 1 descend
 	private int positionRepo;
 	private boolean enRepositionnement;
 	private int tempsParcoursAscenseur;
 	private int xtemps;
 	
 	// GETTER ET SETTER
 	public HashSet<Integer> getTabDestinationTemp() {
 		return tabDestinationTemp;
 	}
 	public void setTabDestinationTemp(HashSet<Integer> tabDestinationTemp) {
 		this.tabDestinationTemp = tabDestinationTemp;
 	}
 	public boolean isEnRepositionnement() {
 		return enRepositionnement;
 	}
 	public void setEnRepositionnement(boolean enRepositionnement) {
 		this.enRepositionnement = enRepositionnement;
 	}
 	public int getTempsParcoursAscenseur() {
 		return tempsParcoursAscenseur;
 	}
 	public void setTempsParcoursAscenseur(int tempsParcoursAscenseur) {
 		this.tempsParcoursAscenseur = tempsParcoursAscenseur;
 	}
 	public void setPositionRepo(int positionRepo) {
 		this.positionRepo = positionRepo;
 	}
 	public int getPositionRepo() {
 		return positionRepo;
 	}
 
 	public int getIdAscenseur() {
 		return idAscenseur;
 	}
 	public int getPositionActuelle() {
 		return positionActuelle;
 	}
 	public ArrayList<Appel> getTabAppelsTraites() {
 		return tabAppelsTraites;
 	}
 	public int getConsommation() {
 		return consommation;
 	}
 
 	public int getNbPersonne() {
 		return nbPersonne;
 	}
 	public ArrayList<Appel> getTabAppelAtraiter() {
 		return tabAppelAtraiter;
 	}
 	public ArrayList<Integer> getTabDestination() {
 		return tabDestination;
 	}
 	public void setConsommation(int consommation) {
 		this.consommation = consommation;
 	}
 
 	public void setNbPersonne(int nbPersonne) {
 		this.nbPersonne = nbPersonne;
 	}
 	public void setTabDestination(ArrayList<Integer> tabDestination) {
 		this.tabDestination = tabDestination;
 	}
 	public void setTabAppelAtraiter(ArrayList<Appel> tabAppelAtraiter) {
 		this.tabAppelAtraiter = tabAppelAtraiter;
 	}
 
 	public void setIdAscenseur(int idAscenseur) {
 		this.idAscenseur = idAscenseur;
 	}
 	public void setPositionActuelle(int positionActuelle) {
 		this.positionActuelle = positionActuelle;
 	}
 	public void setTabAppelsTraites(ArrayList<Appel> tabAppelsTraites) {
 		this.tabAppelsTraites = tabAppelsTraites;
 	}
 	public boolean isArret() {
 		return arret;
 	}
 	public boolean isMonte() {
 		return monte;
 	}
 	public void setArret(boolean arret) {
 		this.arret = arret;
 	}
 	public void setMonte(boolean monte) {
 		this.monte = monte;
 	}
 	public int getXtemps() {
 		return xtemps;
 	}
 	public void setXtemps(int xtemps) {
 		this.xtemps = xtemps;
 	}
 	// FONCTIONS
 
 	/**
 	 * Fonction permettant de dplacer un ascenseur d'un tage  un autre.
 	 * Deux cas possible : Appel ou Repositionnement
 	 * @return
 	 * @throws InterruptedException
 	 */
 	public void deplacement() throws InterruptedException {
 		// TODO Auto-generated method stub
 		
 		int nbEtageAparcourir;
 		
 		if(tabAppelAtraiter.size() != 0){ // DEPLACEMENT CAR APPEL
 			//ascenseur passe en mouvement
			if(positionActuelle==tabDestination.get(0)){
				tabDestination.remove(0);
			}
 			arret=false;
 			
 			//calcul nombre etage  parcourir
 			nbEtageAparcourir = tabDestination.get(0) - positionActuelle;
 			nbEtageAparcourir = Math.abs(nbEtageAparcourir);
 			System.out.println("Ascenseur "+ idAscenseur +" :Etage " + positionActuelle);
 			//calcul temps du parcours
 			sleepParcours(nbEtageAparcourir);
 			
 			//changement de l'etage
 			positionActuelle=tabDestination.get(0);
 			System.out.println("Ascenseur "+ idAscenseur +" :Etage " + positionActuelle);
 			
 			//ascenseur passe  l'arret
 			arret=true;
 			
 			//les appels correspondant  cet etages passe en trait
 			traitementAppel();	
 			System.out.println("Ascenseur "+ idAscenseur +" :Appel trait!");
 			Thread.sleep((5*1000)/xtemps);
 		}
 		else{ // REPOSITIONNEMENT
 			//ascenseur passe en mouvement
 			arret=false;
 			System.out.println("Ascenseur "+ idAscenseur +" :Je vais me repositionner");
 			//calcul nombre etage  parcourir
 			nbEtageAparcourir = positionRepo - positionActuelle;
 			nbEtageAparcourir = Math.abs(nbEtageAparcourir);
 			
 			
 			//Sleep suivant le nombre d'tage  parcourir
 			sleepParcours(nbEtageAparcourir);
 						
 			//changement de l'etage
 			positionActuelle=positionRepo;
 			System.out.println("Ascenseur" + idAscenseur + " :Etage Repo " + positionActuelle);
 			
 			//ascenseur passe  l'arret
 			arret=true;
 
 		}
 	}
 	
 	/**
 	 * Fonction permettant de rechercher les appels correspondant  l'tage pass en paramtre
 	 * @param etage
 	 * @return
 	 */
 	public ArrayList<Appel> rechercheAppel(Integer etage){
 		ArrayList<Appel> tabAppelTrouve = new ArrayList<Appel>();
 		int i=0;
 		while(i<this.tabAppelAtraiter.size()){
 			 if(this.tabAppelAtraiter.get(i).getDestAppel() == etage)
 				 tabAppelTrouve.add(this.tabAppelAtraiter.get(i));
 			 i++;
 		}
 		return tabAppelTrouve;
 	}
 	
 	/**
 	 * Fonction permettant de rechercher les Sources correspondant  l'tage pass en paramtre dans tabDestination
 	 * @param etage
 	 * @return
 	 */
 	public ArrayList<Integer> rechercheSource(Integer etage){
 		ArrayList<Integer> tabSourceTrouve = new ArrayList<Integer>();
 		int i=0;
 		while(i<this.tabDestination.size()){
 			 if(this.tabDestination.get(i) == etage)
 				 tabSourceTrouve.add(etage);
 			 i++;
 		}
 		return tabSourceTrouve;
 	}
 	
 	
 	/**
 	 * Fonction permettant de dire que nous avons trait un appel
 	 * @return
 	 */
 	public void traitementAppel() {
 		// TODO Auto-generated method stub
 		ArrayList<Appel> tabAppelAsupprimer = new ArrayList<Appel>();
 		ArrayList<Integer> tabSourceAsupprimer = new ArrayList<Integer>();
 		// Recherche les appels correspondant  l'etage actuel.
 		tabAppelAsupprimer = rechercheAppel(this.positionActuelle);
 		tabSourceAsupprimer = rechercheSource(this.positionActuelle);
 		
 		//Dplace les appels de Atraiter vers Traiter
 		tabAppelsTraites.addAll(tabAppelAsupprimer);
 		tabAppelAtraiter.removeAll(tabAppelAsupprimer);
 		tabDestination.removeAll(tabSourceAsupprimer);
 		//System.out.println("tabAppelAsupprimer" + tabAppelAsupprimer);
 		//System.out.println("tabAppelsATraiter" + tabAppelAtraiter);
 		//System.out.println("tabAppelsTraites" + tabAppelsTraites);
 		//System.out.println("tabDestination" + tabDestination);
 	}
 	
 	/**
 	 * Fonction permettant de trier les appels se trouvant dans AppelsAtraiter afin de les organiser dans l'ordre o l'ascenseur va s'arrter. (tabDestination)
 	 * @return
 	 */
 	public void triAppel() {
 		//Fonction triant les appels en attente
 		
 		//remplissage d'un tableau avec les destinations des appels
 		for (Appel appel : this.tabAppelAtraiter) {
 			this.tabDestination.add(appel.getDestAppel());
 			this.tabDestination.add(appel.getSourceAppel());
 		}
 		
 		//Suppression des doublons
 		tabDestinationTemp.addAll(tabDestination);
 		tabDestination.removeAll(tabDestination);
 		tabDestination.addAll(tabDestinationTemp);
 		
 		//Algorithme de tri du precedent tableau
 		if(this.monte == true){ //Si on monte
 			Collections.sort(this.tabDestination);
 		}
 		else{ //Si on descend
 			Collections.sort(this.tabDestination, Collections.reverseOrder());
 		}
 
 	}
 	
 	/**
 	 * Fonction permettant de savoir combien de temps va mettre l'ascenseur avant de traiter l'appel pass en paramtre
 	 * @return
 	 */
 	public int calculDureeTraitementAppel(Appel unAppel, boolean monte){
 		int i=0;
 		int nbEtageAparcourir;
 		tempsParcoursAscenseur=0;
 		
 		// pour toutes les destinations sauf la dernire car pris dans la comparaison
 		for(i=0;i<tabDestination.size();i++){
 			//Test si on a pass l'tage
 			if(monte){
 				if(unAppel.getDestAppel()<tabDestination.get(i)){
 					//Calcul le nombre d'etage a parcourir entre deux appels conscutifs
 					nbEtageAparcourir = tabDestination.get(i+1)-tabDestination.get(i);
 					nbEtageAparcourir = Math.abs(nbEtageAparcourir);
 					
 					// Calcul du temps  attendre entre le parcours de ces deux etages cumul avec l'ancien
 					if(nbEtageAparcourir >= 4){
 						tempsParcoursAscenseur = tempsParcoursAscenseur + ((10+(nbEtageAparcourir-4))*1000)/xtemps;
 					}
 					else if(nbEtageAparcourir == 3){
 						tempsParcoursAscenseur = tempsParcoursAscenseur + ((8*1000)/xtemps);
 					}
 					else if(nbEtageAparcourir == 2){
 						tempsParcoursAscenseur = tempsParcoursAscenseur + ((6*1000)/xtemps);
 					}
 					else if(nbEtageAparcourir == 1){
 						tempsParcoursAscenseur = tempsParcoursAscenseur + ((3*1000)/xtemps);
 					}
 					// ajout 5 secondes d'arrt
 					tempsParcoursAscenseur = tempsParcoursAscenseur + ((5*1000)/xtemps);
 				}
 			}
 			else if(!monte){
 				if(unAppel.getDestAppel()>tabDestination.get(i)){
 					//Calcul le nombre d'etage a parcourir entre deux appels conscutifs
 					nbEtageAparcourir = tabDestination.get(i+1)-tabDestination.get(i);
 					nbEtageAparcourir = Math.abs(nbEtageAparcourir);
 					
 					// Calcul du temps  attendre entre le parcours de ces deux etages cumul avec l'ancien
 					if(nbEtageAparcourir >= 4){
 						tempsParcoursAscenseur = tempsParcoursAscenseur + ((10+(nbEtageAparcourir-4))*1000)/xtemps;
 					}
 					else if(nbEtageAparcourir == 3){
 						tempsParcoursAscenseur = tempsParcoursAscenseur + ((8*1000)/xtemps);
 					}
 					else if(nbEtageAparcourir == 2){
 						tempsParcoursAscenseur = tempsParcoursAscenseur + ((6*1000)/xtemps);
 					}
 					else if(nbEtageAparcourir == 1){
 						tempsParcoursAscenseur = tempsParcoursAscenseur + ((3*1000)/xtemps);
 					}
 					// ajout 5 secondes d'arrt
 					tempsParcoursAscenseur = tempsParcoursAscenseur + ((5*1000)/xtemps);
 				}
 			}
 		
 		}
 		return tempsParcoursAscenseur;
 	}
 	
 	/**
 	 * Fonction permettant de savoir combien de temps va mettre l'ascenseur avant de traiter tout les appels
 	 * @return
 	 */
 	public int calculDureeTraitementToutAppels(){
 		int i=0;
 		int nbEtageAparcourir;
 		tempsParcoursAscenseur=0;
 		
 		// pour toutes les destinations sauf la dernire car pris dans la comparaison
 		for(i=0;i<tabDestination.size();i++){
 			
 			//Calcul le nombre d'etage a parcourir entre deux appels conscutifs
 			nbEtageAparcourir = tabDestination.get(i+1)-tabDestination.get(i);
 			nbEtageAparcourir = Math.abs(nbEtageAparcourir);
 			
 			// Calcul du temps  attendre entre le parcours de ces deux etages cumul avec l'ancien
 			if(nbEtageAparcourir >= 4){
 				tempsParcoursAscenseur = tempsParcoursAscenseur + ((10+(nbEtageAparcourir-4))*1000)/xtemps;
 			}
 			else if(nbEtageAparcourir == 3){
 				tempsParcoursAscenseur = tempsParcoursAscenseur + ((8*1000)/xtemps);
 			}
 			else if(nbEtageAparcourir == 2){
 				tempsParcoursAscenseur = tempsParcoursAscenseur + ((6*1000)/xtemps);
 			}
 			else if(nbEtageAparcourir == 1){
 				tempsParcoursAscenseur = tempsParcoursAscenseur + ((3*1000)/xtemps);
 			}
 		// ajout 5 secondes d'arrt
 		tempsParcoursAscenseur = tempsParcoursAscenseur + ((5*1000)/xtemps);
 		}
 		return tempsParcoursAscenseur;
 	}
 	
 	
 	/**
 	 * Fonctione permettant de gerer le temps de parcours entre les etages via des sleep
 	 * @param nbEtageAparcourir
 	 * @throws InterruptedException
 	 * @return
 	 */
 	public void sleepParcours(int nbEtageAparcourir) throws InterruptedException{
 		int nbEtage = nbEtageAparcourir;
 		int i=0;
 		if(nbEtage >= 4){
 			for(i=0;i<nbEtage;i++){
 				if(i==0)
 					Thread.sleep((3*1000)/xtemps);
 				if(i==1)
 					Thread.sleep((2*1000)/xtemps);
 				if(i>1 && i<nbEtage-2)
 					Thread.sleep((1*1000)/xtemps);
 				if(i==nbEtage-2)
 					Thread.sleep((2*1000)/xtemps);
 				if(i==nbEtage-1)
 					Thread.sleep((3*1000)/xtemps);
 				if(monte==true)
 					positionActuelle++;
 				if(monte==false)
 					positionActuelle--;
 				System.out.println("Etage: " + positionActuelle);
 			}
 		}
 		else if(nbEtage == 3){
 			for(i=0;i<nbEtage;i++){
 				if(i==0)
 					Thread.sleep((3*1000)/xtemps);
 				if(i==1)
 					Thread.sleep((2*1000)/xtemps);
 				if(i==2)
 					Thread.sleep((3*1000)/xtemps);
 				if(monte==true)
 					positionActuelle++;
 				if(monte==false)
 					positionActuelle--;
 				System.out.println("Etage: " + positionActuelle);
 			}
 		}
 		else if(nbEtage == 2){
 			for(i=0;i<nbEtage;i++){
 				if(i==0)
 					Thread.sleep((3*1000)/xtemps);
 				if(i==1)
 					Thread.sleep((3*1000)/xtemps);
 				if(monte==true)
 					positionActuelle++;
 				if(monte==false)
 					positionActuelle--;
 				System.out.println("Etage: " + positionActuelle);
 			}
 		}
 		else if(nbEtage == 1){
 			Thread.sleep((3*1000)/xtemps);
 			if(monte==true)
 				positionActuelle++;
 			if(monte==false)
 				positionActuelle--;
 			System.out.println("Etage: " + positionActuelle);
 			
 		}
 	}
 	
 	/**
 	 * fonction permettant d'ajouter un appel dans le tableau d'appels  traiter	
 	 * @param unAppel
 	 */
 	public void addAppel(Appel unAppel)
 	{
 		this.tabAppelAtraiter.add(unAppel);
 	}
 	
 	
 	/**
 	 * Fonction permettant de s'avoir si un ascenseur est plein ou pas.
 	 * @return
 	 */
 	public boolean isFull(){
 		boolean isFull;
 		if(tabAppelAtraiter.size()==15){
 			isFull=true;
 		}
 		else{ 
 			isFull=false;
 		}
 		return isFull;
 	}
 }
