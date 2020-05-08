 package common;
 
 /**
  * @author omar different etats possible pour une partie
  */
 public enum Etat{
 	SAUVEGARDEE, EN_ATTENTE_JOUEUR, COMMENCEE;
 	
 	public static Etat get(String _etat){
 		for (Etat etat : Etat.values()){
			if (etat.equals(_etat)){
 				return etat;
 			}
 		}
 		return EN_ATTENTE_JOUEUR;
 	}
 }
