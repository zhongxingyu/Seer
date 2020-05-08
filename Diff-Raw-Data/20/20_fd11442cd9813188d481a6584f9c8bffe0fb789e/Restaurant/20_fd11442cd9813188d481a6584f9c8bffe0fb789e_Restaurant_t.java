 package Systeme;
 
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Scanner;
 
 
 public class Restaurant {
 	
 	private static ArrayList<Commande> listeCommande = new ArrayList<Commande>();
 	private static String[] listeTable = new String[] {"Table1", "Table2", "Table3", "Table4"};
 	
 	//Initialis au dpart, donne l'id par lequel commenc la numrotation des commandes, pour l'archivage
 	public static int lastId;
 	
 	public static void creerCommande(String table){
		lastId = lastId + 1;
 		listeCommande.add(new Commande(table, lastId));	
 	}
 
 	public static String[] getListeTable(){
 		return listeTable;
 	}
 	
 	public static ArrayList<Commande> getListeCommandePourTable(String table){
 		ArrayList<Commande> listeCommandeTable = new ArrayList<Commande>();
 		
 		for (int i=0; i<listeCommande.size(); i++){
 			if (listeCommande.get(i).getTable() == table){
 				listeCommandeTable.add(listeCommande.get(i));
 			}
 		}
 		
 		return listeCommandeTable;
 	}
 	
 	public static Object[][] creerTableToutesLigneCommandeEnAttente(){
 		Object[][] tableauLigneCommande;
 		ArrayList<LigneCommande> listeToutesLigneCommande = new ArrayList<LigneCommande>();
 		for(int i = 0; i<listeCommande.size(); i++){
 			Commande commandeActive = listeCommande.get(i);
 			for(int j = 0; j<commandeActive.getListeLigneCommande().size(); j++){
 				LigneCommande ligneActive = commandeActive.getListeLigneCommande().get(j);
 				if(ligneActive.getEtat() == "En attente"){
 					listeToutesLigneCommande.add(ligneActive);
 				}
 			}
 		}
 		tableauLigneCommande = new Object[listeToutesLigneCommande.size()][6];
 		for(int i=0; i<listeToutesLigneCommande.size(); i++){
 			LigneCommande ligneComm = listeToutesLigneCommande.get(i);
 			tableauLigneCommande[i][3] = ligneComm.getEtat();
 			tableauLigneCommande[i][0] = ligneComm;
 			tableauLigneCommande[i][1] = ligneComm.getQuantite()+"";
 			tableauLigneCommande[i][2] = ligneComm.getSousTotal()+"";
 			tableauLigneCommande[i][4] = ligneComm.getParent();
 		}
 		return tableauLigneCommande;
 	}
 	
 	public static void supprimerCommande(Commande commandeSupp){
 		listeCommande.remove(commandeSupp);
 	}
 
 }
