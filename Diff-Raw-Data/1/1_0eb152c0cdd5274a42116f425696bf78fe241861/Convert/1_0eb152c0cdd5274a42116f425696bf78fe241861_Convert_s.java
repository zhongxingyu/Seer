 package util;
 
 import util.Constantes.ActionSpeciale;
 import util.Constantes.Couleur;
 
 public class Convert {
 
 	public static final Couleur stringToCouleur(String s) {
 		if ( Couleur.ROUGE.toString().equals(s)) {
 			return Couleur.ROUGE;
 		}		
 		if ( Couleur.JAUNE.toString().equals(s)) {
 			return Couleur.JAUNE;
 		}
 		if ( Couleur.VERT.toString().equals(s)) {
 			return Couleur.VERT;
 		}
 		if ( Couleur.BLEU_FONCE.toString().equals(s)) {
 			return Couleur.BLEU_FONCE;
 		}
 		if ( Couleur.MAGENTA.toString().equals(s)) {
 			return Couleur.MAGENTA;
 		}
 		if ( Couleur.BLEU_CIEL.toString().equals(s)) {
 			return Couleur.BLEU_CIEL;
 		}
 		if ( Couleur.VIOLET.toString().equals(s)) {
 			return Couleur.VIOLET;
 		}
 		if ( Couleur.ORANGE.toString().equals(s)) {
 			return Couleur.ORANGE;
 		}
 		if ( Couleur.NOIR.toString().equals(s)) {
 			return Couleur.NOIR;
 		} 
 		return Couleur.BLANC;
 	}
 
 	public static ActionSpeciale stringToAction(String string) {
 		if ( ActionSpeciale.ALLERENPRISON.toString().equals(string) ) {
 			return ActionSpeciale.ALLERENPRISON;
 		}
 		if ( ActionSpeciale.CAISSECOMMUNAUTE.toString().equals(string) ) {
 			return ActionSpeciale.CAISSECOMMUNAUTE;
 		}
 		if ( ActionSpeciale.CHANCE.toString().equals(string) ) {
 			return ActionSpeciale.CHANCE;
 		}
 		if ( ActionSpeciale.DEPART.toString().equals(string) ) {
 			return ActionSpeciale.DEPART;
 		}
 		if ( ActionSpeciale.IMPOTS.toString().equals(string) ) {
 			return ActionSpeciale.IMPOTS;
 		}
 		if ( ActionSpeciale.PARCGRATUIT.toString().equals(string) ) {
 			return ActionSpeciale.PARCGRATUIT;
 		}
 		return ActionSpeciale.VISITERPRISON;
 	}
 }
