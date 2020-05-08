 package modele;
 
 import java.awt.Color;
 import java.util.ArrayList;
 
 import vue.FenetreMetro;
 
 /*
  * Un metro est un ensemble de Ligne
  */
 public class Metro {
 	
 	private ArrayList<Ligne> metro;
 	private ArrayList<Station> stations;
 	/** Liste des stations visitees utilise uniquement pour la recherche de chemin */
 	private ArrayList<Station> stationsVisitees;
 	
 	public Metro(){
 		stations = new ArrayList<Station>();
 		this.metro = initialize();	
 		stationsVisitees = new ArrayList<Station>();
 		new FenetreMetro(this);
 	}
 	
 	/*
 	 * methode qui va cree le squelette du reseau
 	 */
 	public ArrayList<Ligne> initialize(){
 		
 		//instanciation des stations (pour le moment chaque arret dure 10 min)
 		Station A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P;
 		
 		A = new Station("Monceau", 50, 50, 10);
 		B = new Station("Champs de Mars", 200, 100, 10);
 		C = new Station("Ecole Militaire", 300, 200, 10);
 		D = new Station("Porte de Vincent", 400, 300, 10);
 		E = new Station("Quatre-Septembre", 100, 200, 10);
 		F = new Station("Richard-Lenoir", 100, 300, 10);
 		G = new Station("Rue des Boulets", 400, 100, 10);
 		H = new Station("Maison Blanche", 500, 200, 10);
 		I = new Station("Rue de la Pompe", 300, 400, 10);
 		J = new Station("Invalides", 500, 400, 10);
 		K = new Station("Liberté", 650, 450, 10);
 		L = new Station("Rome", 50, 400, 10);
 		M = new Station("Duroc", 300, 300, 10);
 		N = new Station("Alésia", 600, 50, 10);
 		O = new Station("Alexandre Dumas", 650, 250, 10);
 		P = new Station("Dupleix", 700, 150, 10);
 		
 		
 		stations.add(A);
 		stations.add(B);
 		stations.add(C);
 		stations.add(D);
 		stations.add(E);
 		stations.add(F);
 		stations.add(G);
 		stations.add(H);
 		stations.add(I);
 		stations.add(J);
 		stations.add(K);
 		stations.add(L);
 		stations.add(M);
 		stations.add(N);
 		stations.add(O);
 		stations.add(P);
 		
 		
 		//instanciation des voies (pour le moment chaque voie dure 20 min)
 		A.addVoie(B, 1, 20);
 		B.addVoie(C, 2, 20);
 		C.addVoie(D, 3, 20);
 		D.addVoie(J, 4, 20);
 		E.addVoie(F, 5, 20);
 		F.addVoie(I, 6, 20);
 		I.addVoie(J, 7, 20);
 		E.addVoie(B, 8, 20);
 		B.addVoie(G, 9, 20);
 		G.addVoie(H, 10, 20);
 		H.addVoie(O, 11, 20);
 		J.addVoie(K, 12, 20);
 		J.addVoie(O, 13, 20);
 		L.addVoie(F, 14, 20);
 		F.addVoie(M, 15, 20);
 		M.addVoie(D, 16, 20);
 		D.addVoie(H, 17, 20);
 		H.addVoie(N, 18, 20);
 		M.addVoie(C, 19, 20);
 		C.addVoie(G, 20, 20);
 		G.addVoie(N, 21, 20);
 		N.addVoie(P, 22, 20);
 		P.addVoie(O, 23, 20);
 		
 		
 		//instanciation des lignes
 		Ligne un, deux, trois, quatre, cinq;
 		un = new Ligne(1, null, Color.green);
 		deux = new Ligne(2, null, Color.orange);
 		trois = new Ligne(3, null, Color.blue);
 		quatre = new Ligne(4, null, Color.red);
 		cinq = new Ligne(5, null, Color.black);
 		
 		un.addStation(A);
 		un.addStation(B);
 		un.addStation(C);
 		un.addStation(D);
 		un.addStation(J);
 		un.addStation(O);
 		deux.addStation(E);
 		deux.addStation(F);
 		deux.addStation(I);
 		deux.addStation(J);
 		deux.addStation(K);
 		trois.addStation(E);
 		trois.addStation(B);
 		trois.addStation(G);
 		trois.addStation(H);
 		trois.addStation(O);
 		quatre.addStation(L);
 		quatre.addStation(F);
 		quatre.addStation(M);
 		quatre.addStation(D);
 		quatre.addStation(H);
 		quatre.addStation(N);
 		cinq.addStation(M);
 		cinq.addStation(C);
 		cinq.addStation(G);
 		cinq.addStation(N);
 		cinq.addStation(P);
 		cinq.addStation(O);
 		
 		ArrayList<Ligne> tmp = new ArrayList<Ligne>();
 		tmp.add(un);
 		tmp.add(deux);
 		tmp.add(trois);
 		tmp.add(quatre);
 		tmp.add(cinq);
 		
 		return tmp;
 	}
 	
 	/**
 	 * @return liste des lignes du metro
 	 */
 	public ArrayList<Ligne> getMetro() {
 		return metro;
 	}
 	
 	/**
 	 * Liste l'ensemble des stations du metro
 	 * @return liste des stations du metro
 	 */
 	public ArrayList<Station> getStations() {
 		return stations;
 	}
 	
 	/**
 	* Fonction de recherche de la station la plus proche de l'utilisateur (avec geolocalisation préalable)
 	* @param x
 	* @param y
 	* @return Station la plus proche
 	*/
 	public Station rechercheProcheStation(int x, int y){
 		//Plus grande distance possible dans le metro
 		double distance=500;
 		Station stationProche = null;
 			for (Ligne line : metro){
 				for(Station st : line.getStations()){
 				if(distance > Math.sqrt(Math.pow(x - st.getX(), 2.0) + Math.pow(y - st.getY(), 2.0))){
 					distance = Math.sqrt(Math.pow(x - st.getX(), 2.0) + Math.pow(y - st.getY(), 2.0));
 					stationProche = st;
 				}
 			}
 		}
 		return stationProche;
 	}
 	
 	
 	public String toString(){
 		return "Métro : " + metro ;
 	}
 	
 	/**
 	 * Retourne le(s) numero(s) de ligne(s) associe(s) a la station donnee en parametre
 	 * @param la station dont on veut connaitre les lignes auquels elle appartient
 	 * @return liste des numeros de ligne auquel la station appartient
 	 */
 	public ArrayList<Integer> getNumStation(Station s){
 		ArrayList<Integer> res = new ArrayList<Integer>();
 		for(Ligne ligne : metro) {
 			if(ligne.existeStation(s)) res.add(new Integer(ligne.getIdLigne()));
 		}
 		return res;
 	}
 	
 	/**
 	 * Retourne toutes les lignes sur lesquelle la station donne en parametre se trouve
 	 * @param s la station dont on veut connaitre les lignes
 	 * @return liste de ligne contenant le station
 	 */
 	public ArrayList<Ligne> getLignesStation(Station s) {
 		if(s == null || metro == null) return null;
 		ArrayList<Ligne> res = new ArrayList<Ligne>();
 		for(Ligne ligne : metro) {
 			if(ligne.existeStation(s)) res.add(ligne);
 		}
 		return res;
 	}
 	
 	/**
 	 * Ajout d'une ligne dans le metro
 	 * @param l ligne a ajouter
 	 */
 	public void ajouterLigne(Ligne l) {
 		if(l != null) metro.add(l);
 	}
 
 	/**
 	 * Donne le chemin ayant le temps de trajet le plus court entre 2 stations
 	 * @param depart la station de depart
 	 * @param arrivee la station d'arrivee
 	 * @return le chemin ou null s'il n'y en a pas
 	 */
 	public Chemin getPlusCourtChemin(Station depart, Station arrivee) {
 		ArrayList<Chemin> chemins = getChemins(depart, arrivee, new Chemin(this));
 		Chemin res = null;
 		for(Chemin c : chemins) {
 			if(res == null) res = c;
 			else if(c.getDureeChemin() < res.getDureeChemin()) res = c;
 		}
 		stationsVisitees.clear();
 		return res;
 	}
 	
 	/**
 	 * Donne le chemin ayant le moins de changement entre 2 stations
 	 * @param depart station de depart
 	 * @param arrivee station d'arrivee
 	 * @return le chemin ou null si'il n'y en a pas
 	 */
 	public Chemin getMoinsChangementChemin(Station depart, Station arrivee) {
 		ArrayList<Chemin> chemins = getChemins(depart, arrivee, new Chemin(this));
 		Chemin res = null;
 		for(Chemin c : chemins) {
 			if(res == null) res = c;
 			else if(c.getNbChangement() < res.getNbChangement()) res = c;
 		}
 		stationsVisitees.clear();
 		return res;
 	}
 	
 	/**
 	 * Permet d'obtenir la liste des chemins permettant d'aller d'une station ˆ une autre
 	 * @param depart station de depart du chemin
 	 * @param arrivee station d'arrive du chemin
 	 * @param chemin le chemin, il doit �tre initialement vide
 	 * @return liste des chemins permettant d'aller de depart a arrivee
 	 */
 	public ArrayList<Chemin> getChemins(Station depart, Station arrivee, Chemin chemin) {
 		if(depart == null || arrivee == null || chemin == null) return null;
 		if(!depart.isOuvert()) return null;
 		chemin.ajouterStation(depart);
 		
 		// Condition de fin on a trouve la station d'arrivee
 		if(depart.equals(arrivee)) {
 			ArrayList<Chemin> res = new ArrayList<Chemin>();
 			res.add(chemin);
 			return res;
 		} else {	// Sinon on relance la recherche a partir de chaque station liee a celle de depart
 			stationsVisitees.add(depart);
 			ArrayList<Chemin> res = new ArrayList<Chemin>();
 			for(Voie v : depart.getVoies()) {
 				if(!stationsVisitees.contains(v.getDestination()) && v.isEnCirculation()) {
 					ArrayList<Chemin> chemins = getChemins(v.getDestination(), arrivee, chemin.clone());
 					if(chemins != null) res.addAll(chemins);
 				}
 			}
 			return res;
 		}
 	}
 	
 	public static void main(String[]args){
 		Metro m = new Metro();
 //		System.out.println(m);
 		int x = 410;
 		int y = 120;
 //		System.out.println("Station la plus proche de (" + x +", " + y + ") : " + m.rechercheProcheStation(x, y));
 	}
 	
 }
