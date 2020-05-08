 
 public class IdParam extends Ident{
	private static int prochain_offset = 0;
 	private boolean affecte;
 	private static int compteur = 0;
 	
 	public IdParam (int t){
 		super();
 		super.type = t;
 		super.offset = prochain_offset;
 		prochain_offset += 2;
 		affecte = false;
 		compteur++;
 	}
 	
 	public void setAffecte(boolean affecte) {
 		this.affecte = affecte;
 	}
 	
	public static void raz(){prochain_offset = 0;}
 	
 	public String toString() {
 		return "( Parametre : type : " + type + " ; offset : " + offset + ")";
 	}
 	
 	public static int getCompteur() {
 		return compteur;
 	}
 	public static void razCompteur() {
 		compteur = 0;
 	}
 }
