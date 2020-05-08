 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.LinkedList;
 
 public class Admin implements GestionGraphe{
 	Graphe graphe;
 
 	public Admin(){
 		this.graphe = new Graphe();
 	}
 	
 	@Override
 	public int getNbSommet() {
 		return graphe.getNbSommet();
 	}
 
 	@Override
 	public int getNbArc() {
 		int nbArc = 0;
 		for(Sommet s : graphe.getListeSommet()){
 			nbArc += s.getV_sortant().size();
 		}
 		return nbArc;
 	}
 
 	@Override
 	public ArrayList<Sommet> getSommet() {
 		return graphe.getListeSommet();
 	}
 
 	@Override
 	public ArrayList<Sommet> getSommetByName() {
 		ArrayList<Sommet> ordered = graphe.getListeSommet();
 		Collections.sort(ordered, new NameComparator());
 		return ordered;
 	}
 
 	@Override
 	public ArrayList<Sommet> getSommetByDegre() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	//Renvois les arcs sous forme de LinkedList<Sommet>. Le premier de la liste est l'user  l'origine des liens
 	@Override
 	public Hashtable<Sommet, LinkedList<Sommet>> getArc() {
 		Hashtable<Sommet, LinkedList<Sommet>> list = new Hashtable<Sommet, LinkedList<Sommet>>(); 
 		
 		for(Sommet s : graphe.getListeSommet()){
 			if(s instanceof User){
 				LinkedList<Sommet> temp = s.getV_sortant();
 				list.put(s, temp);
 			}
 			
 		}
 		
 		return list;
 	}
 
 	@Override
 	public Sommet getSommet(String name) {
 		
 		for(Sommet s : graphe.getListeSommet()){
 			if(s.getName().equals(name)){
 				return s;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public int getNbUser() {
 		return graphe.getNbUser();
 	}
 
 	@Override
 	public int getNbPage() {
 		this.getNbPage();
 		return 0;
 	}
 
 	@Override
 	public float getAgeMoyen() {
 		int ageM = 0;
 		for(Sommet s : graphe.getListeSommet()){
 			if(s instanceof User){
 				ageM += ((User) s).getAge();
 			}
 		}
 		return ageM/graphe.getNbUser();
 	}
 
 	@Override
 	public HashSet<User> getAdminUsers() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void addUser(int age, String name) {
 		graphe.addUser(age, name);
 		
 	}
 	
 	public void addUser(User u){
 		graphe.addUser(u.getAge(), u.getName());
 	}
 
 	@Override
 	public void addPage(String name) {
 		graphe.addPage(name);
 		
 	}
 
 	@Override
 	public void deleteSommet(int id) {
 		for(Sommet s : graphe.getListeSommet()){
			int idLien = -1;
 			for(Sommet s2 : s.getV_sortant()){
 				if(s2.getName().equals(graphe.getListeSommet().get(id).getName())){
					idLien = s2.getNumero();
 				}
 			}
			if(idLien != -1)
				s.getV_sortant().remove(idLien);
 		}
 		graphe.getListeSommet().remove(id);
 		
 	}
 
 	@Override
 	public void writeGraphe(Graphe g, String fileName) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public Graphe loadGraphe(String fileName) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void deleteArc(Sommet orig, Sommet cible) {
 		
 		
 	}
 
 }
