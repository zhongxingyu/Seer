 package iaws.NBMR.services.impl;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 
 import iaws.NBMR.domaines.Utilisateur;
 import iaws.NBMR.service.DataService;
 
 public class DataServiceImpl implements DataService{
 
 	private static DataServiceImpl instance = null;
 	
 	private Map<String, Utilisateur> listeUtilisateurs;
 	
 	private DataServiceImpl(){
 		listeUtilisateurs = new HashMap<String, Utilisateur>();
 	}
 	
 	public static DataServiceImpl getInstance(){
 		if(instance == null)
 			instance = new DataServiceImpl();
 		return instance;
 	}
 	
 	
 	public void saveUtilisateur(Utilisateur utilisateur) {
 		this.listeUtilisateurs.put(utilisateur.getEmail(), utilisateur);
 	}
 	
 	public Utilisateur findUtilisateurByEmail(String email){
 		return listeUtilisateurs.get(email);
 	}
 
 	public List<Utilisateur> findUtilisateurACoteDe(String email, int distance) {
 		
 		List<Utilisateur> toReturn = new ArrayList<Utilisateur>();
 		Utilisateur reference = this.findUtilisateurByEmail(email);
 		Iterator<Utilisateur> it = this.listeUtilisateurs.values().iterator();
 		while(it.hasNext()){
 			Utilisateur current = it.next();
 			if(current.equals(reference)) continue;
 			if(null == current.getCoordonnees()) continue;
 		
 			if(current.getCoordonnees().getDistanceEnMetreAvec(reference.getCoordonnees())<= distance){
 				// On a un match
 				toReturn.add(current);
 			}
 		}
 		
 		return toReturn;
 	}
 
 	public void print() {
 		
 		System.out.println("==== Etat de la base ====");
 		Iterator<Utilisateur> it = this.listeUtilisateurs.values().iterator();
 		while(it.hasNext()){
 			Utilisateur current = it.next();
 			System.out.println(current);
 		}
 	}
 
 }
