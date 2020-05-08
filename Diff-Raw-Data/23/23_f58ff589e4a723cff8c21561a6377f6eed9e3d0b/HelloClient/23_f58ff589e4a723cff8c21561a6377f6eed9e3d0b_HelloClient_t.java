 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package chien_client;
 import Commun.Chaterface;
 import Commun.Requete;
 import java.net.InetAddress;
 
 /**
  *
  * @author Sebastien
  */
 import java.rmi.*;
 import java.util.Scanner;
 public class HelloClient {
 public static void main(String args[]) {
         String URL;
         int port=8888;
         int ClientID=-1;
         
             try {
                 // Récupération d'un stub sur l'objet serveur.
                 // Calcul de l’URL du serveur
               /* URL = "//192.168.177.1:"
                        + port + "/mon_serveur";*/
                 URL = "//" + InetAddress.getLocalHost().getHostName() + ":"
                 + port + "/mon_serveur";
                 Chaterface obj = (Chaterface) Naming.lookup(URL);
                 
                threadAffichage affichageFenetre = new threadAffichage("affichage", obj);
                
                
                 while(true) {
                     // Appel d'une méthode sur l'objet distant.
                     Scanner sc = new Scanner(System.in);
                     System.out.println("Saisissez un message : ");
                     String message = sc.nextLine();  
                     threadAffichage affichageFenetre = new threadAffichage("affichage", obj);
                     
                     Requete req = new Requete(ClientID, message);
                     String reponse = obj.requeteClient(req);
                     if (reponse.startsWith("Connecté :")) {
                         ClientID = Integer.parseInt(reponse.substring(11));
                         affichageFenetre.start();
                     }
                     else if (reponse.startsWith("Good bye")) {
                         ClientID=-1; //déconnection superficielle du client par id mis à -1
                         affichageFenetre.arret();
                         // Fermer le programme après 2 sec
                         try {
                            Thread.sleep(2000);
                         } 
                         catch (InterruptedException e) {
                            e.printStackTrace();
                         }
                         System.exit(0);
                     }
 
                     System.out.println(reponse);
                     
                     
                 }
             } catch (Exception exc) {
                 exc.printStackTrace();;
             }
     } 
 
     private static void affichage(Chaterface obj) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 }
