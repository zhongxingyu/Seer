public class Ident implements YakaConstants {
 	protected int offset = 0;
 	protected int type;	
 	
 	public int getType() {
 		return type;
 	}
 	
 	public void yvm() {
 		
 	}
 	
 	/**
 	 * Methode pour obtenir l'offset de l'ident
 	 * Si l'ident est un IdConst, retourne 0
 	 */
 	public int getOffset() {
 		return offset;
 	}
 	
 	/**
 	 * Lorsque l'on affecte une valeur a un Ident, on utilise setAffecte afin d'informer que l'Ident a bien ete
 	 * initialise. Par contre, si l'ident n'est pas un IdVar, elle affiche un message d'erreur
 	 * @param affecte
 	 */
 	public void setAffecte(boolean affecte) {
		if (Yaka.tabIdent.chercheIdent(YakaTokenManager.identLu).getType() != ERREUR)
 		System.out.println("ERREUR ligne " + Yaka.ligne + " : '" + YakaTokenManager.identLu + "' n'est pas une variable");
 	}
 }
