 /* ThreadUtilisateur.java
  * Blux
  * 03/04/12
  *
  * Thread 4 : Gère les demandes de l'utilisateur du service
  */
 import java.util.Hashtable;
 import java.io.File;
 import java.io.RandomAccessFile;
 import java.io.IOException;
 import java.util.*;
 import java.util.Scanner;
 import java.net.SocketException;
 import java.net.Socket;
 import java.io.OutputStream;
 import java.io.InputStream;
 import java.net.UnknownServiceException;
 import java.io.CharConversionException;
 
 
 //   *TODO* *MEMENTO* compter la longueur des fichiers en bytes pas en String !!!
 
 
 public class ThreadUtilisateur extends ToolsTelechargementThread {
 
     public ThreadUtilisateur(String name, FichierConfiguration fichierConf, Hashtable<String,Fichier> hash){
 	super(name,fichierConf,hash);
     }
     
     public void run(){
 	// Pour ne pas surcharger out 
 	try{Thread.sleep(100);}
 	catch(InterruptedException ite){ 
 	    ite.printStackTrace(); 
 	}		
 	System.out.println("Bienvenue dans le programme utilisateur !");
 	Scanner scan = new Scanner(System.in);
 	String question = "", reponse = "" , compteur = "";
 	int ret = -1, i = 0, j, resultat = -1;
 	String key_to_dl , name_file_to_dl;
 	Collecte co;
 	InfoPair [] tab;
 	String [] info;
 	boolean bool;
 	int port_tmp, taille_tmp, taille_piece_tmp;
 
 	while (ret != 0){
 	    while (! (0 <= ret && ret <= 2) ){
 		System.out.println("0 :\tQuitter");
 		System.out.println("1 :\tRechercher par nom de fichier");
 		System.out.println("2 :\tRechercher par clé");
 		while (!scan.hasNextInt())
 		    compteur = scan.next();
 		ret = scan.nextInt();
 	    }
 	    if (ret == 0){
 		// On quitte ce thread
 		return ;
 	    }
 	    try {
 		
 		if (ret == 1){
 		    System.out.println("tapez le nom du fichier recherché :");
 		    compteur = scan.next();
 		    question = "look [filename=\"" + compteur + "\"]";
 		}
 		else if (ret == 2){
 		    System.out.println("tapez la clé du fichier recherché :");
 		    compteur = scan.next();
 		    question = "look [key=\"" + compteur + "\"]";
 		}
 		ret = -1;
 
 		reponse = demandeCrochet(_fichierConf.getIp(), _fichierConf.getPort(), question, 5, 1000);
 	    
 		if (0 != reponse.indexOf("list"))
 		    throw new CharConversionException("réponse inapropriée :\n>> " + question + "\n<< " + reponse);
 
 		co = new Collecte(reponse);
 		tab = co.getTab();
 	    
 
 		//  *TODO*
 		while (! (0<=resultat && resultat <= co.getTab().length+1)){ 
		    if (co.getTab().length < 1) {
 			System.out.println("Voici les fichiers correspondant à vos critères de recherche:");
 			for (i=0; i<co.getTab().length ; i++)
 			    System.out.println((1+i) + ":\t" + co.getTab()[i]);
 			System.out.println("Tapez 'n' pour commencer le téléchargement ou '0' pour annuler.");
 			
 			while (!scan.hasNextInt())
 			    compteur = scan.next();
 			resultat = scan.nextInt();
 		    }
		    else
 			resultat = 0;
		    
 		}
 		if (resultat != 0){
 		    resultat--;
 		    key_to_dl = co.getTab()[resultat].getKey();
 		    name_file_to_dl = co.getTab()[resultat].getName();
 		    taille_tmp = co.getTab()[resultat].getTaille();
 		    taille_piece_tmp = _fichierConf.getTaille();
 		
 		    co.epurer(key_to_dl);
 		    // on recherche le fichier de clé key_to_dl
 		    question = "getfile " + key_to_dl;
 		
 		    reponse = demandeCrochet(_fichierConf.getIp(), _fichierConf.getPort(), question, 5 , 1000);
 		
 		    if (0 != reponse.indexOf("peers") || !key_to_dl.equals(reponse.split(" ")[1]))
 			throw new CharConversionException("réponse inapropriée :\n>> " + question + "\n<< " + reponse);
 
 		    info = reponse.substring(1+reponse.indexOf("["),reponse.indexOf("]")).split(" ");
 		    boolean [][] tabMasque = createTabMasque(info,key_to_dl);
 		
 		    //    System.out.println("pouet!");
 		    for ( i=0 ; i<tabMasque[0].length ; i++) {
 			bool = true;
 			for ( j=0 ; j<tabMasque.length ; j++){
 			    if (bool && tabMasque[j][i])
 				bool = false;
 			    else
 				tabMasque[j][i] = false;
 			}
 		    }
 		    System.out.println("Création du fichier Download/" + name_file_to_dl);
 		    Fichier newfichier = new Fichier(name_file_to_dl , key_to_dl, taille_tmp , taille_piece_tmp);
 		    _hash.put(key_to_dl, newfichier);
 		    System.out.println("Fichier crée.");
 
 		    System.out.println("APOCALIIIIPSE ! : " + tabMasque.length);
 		    
 		    for ( i=0 ; i<tabMasque.length ; i++ ){
 			port_tmp = Integer.parseInt(info[i].split(":")[1]);
 			telecharger(info[i].split(":")[0], port_tmp, key_to_dl, tabMasque[i]);    
 			// sauve les données ?
 		    }
 		
 		    System.out.println("Fichier téléchargé");
 		}
 		resultat = -1;
 		
 	    } catch (UnknownServiceException se) {
 		System.out.println("Un serveur n'as pas pu être trouvé : " + se);
 		se.printStackTrace();
 		//      return ;
 	    } catch (CharConversionException ce) {
 		System.out.println("Réponse d'un serveur mal formée : " + ce);
 		ce.printStackTrace();
 		//	    return ;
 	    } catch (Exception e) {
 		System.out.println("Exception : " + e);
 		e.printStackTrace();
 	    }
 	}		
     }    
 }
 
