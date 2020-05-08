 package com.emn.fil.automaticdiscover.nmap;
 
 /** Enum qui définit quelques options utiles à la commande nmap.
  * 
  * @author Clement
  *
  */
 public enum OptionsNmap {
	PING("-Pn"), OS_DETECTION("-O"), HOST_NAME_DETECTION("--system-dns"), FASTER("-T Aggresive");
 	
 	private String commande;
 
 	private OptionsNmap(String commande) {
 		this.commande = commande;
 	}
 
 	public String getCommande() {
 		return this.commande;
 	}
 }
