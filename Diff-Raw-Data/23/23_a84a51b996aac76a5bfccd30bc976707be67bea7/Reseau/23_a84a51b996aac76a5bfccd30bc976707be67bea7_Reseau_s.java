 import java.util.LinkedList;
 import java.util.Map;
 
 
 public class Reseau {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Admin ad = new Admin();
 		
 		//Test ajout d'utilisateur + affichage info + recuperation User par son nom
 		ad.addUser(20, "hjkl");
 		ad.addUser(20, "ghjk");
 		User ten = (User) ad.getSommet("hjkl");
 		System.out.println("Nom : " + ten.getName() + ", Age : " + ten.getAge());
 		
 		User u = new User(1, 19, "azerty");
 		ad.addUser(u);
 		
 		// Test ajout amis
 		ten.ajouter_ami(u);
 		ten.ajouter_ami(u);
 		System.out.println();
 		
 		//Test ajout page + like page
 		ad.addPage("Page1");
 		Page p = (Page) ad.getSommet("Page1");
 		ten.like_page(p);
 		
 		affichageArcs(ad);
 		
 		ad.deleteSommet(1);
		System.out.println("---");
 		affichageArcs(ad);
 		
 		ad.deleteSommet(1);
		System.out.println("---");
 		affichageArcs(ad);
 		
 		
 	}
 	
 	// recuperation des arcs et affichage
 	public static void affichageArcs(Admin ad){
 		for (Map.Entry<Sommet, LinkedList<Sommet>> entry : ad.getArc().entrySet()){
 		     System.out.print(entry.getKey().getName()); 
 		     LinkedList<Sommet> sommets = entry.getValue();
 			 for(Sommet s : sommets){
 				 System.out.print(" -> " + s.getName());
 			 }
 			 System.out.println();
 		}
 	}
 
 }
