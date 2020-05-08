 /******************************************************
  * Cours:  LOG121
  * Projet: Squelette du laboratoire #1
  * Nom du fichier: CommBase.java
  * Date créé: 2013-05-03
  *******************************************************
  * Historique des modifications
  *******************************************************
  * @author Patrice Boucher
  * 2013-05-03 Version initiale
  *
  * @author Charles Levesque
  * 2013-10-01 Version finale TP1
  *
  * @author Mathieu Lachance
  * 2013-10-09 : Ajout des fonctions connect et disconnect
  ********************************************************/
 
 package main;
 
 import javax.swing.*;
 import java.beans.PropertyChangeListener;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 
 /**
  * Base d'une communication via un fil d'exécution parallèle.
  */
 public class CommBase {
     private final String OPTIONPANE_MESSAGE_BADSERVERNAME = "app.optionPane.message.badServerName";
     private final String OPTIONPANE_MESSAGE_STARTERROR = "app.optionPane.message.startError";
     private final String OPTIONPANE_MESSAGE_STOPERROR = "app.optionPane.message.stopError";
     private final String OPTIONPANE_MESSAGE_CONNEXTIONINTERRUPTED = "app.optionPane.message.connexionInterrupted";
     private final String OPTIONPANE_TITLE_ERROR = "app.optionPane.title.error";
     private final String OPTIONPANE_TITLE_WARNING = "app.optionPane.title.warning";
 
 	private final int DELAI = 1000;
 	private SwingWorker threadComm = null;
 	private PropertyChangeListener listener = null;
 	private boolean isActif = false;
     private boolean isLimited = false;
     private int limite;
 	private int PORT_SERVEUR = -1;
 	private String ADRESSE_SERVEUR = null;
 	private final String END_TRAME = "END";
 	private final String GET_TRAME = "GET";
 	private Socket client = null;
 	private PrintWriter fluxEcriture = null;
 	private BufferedReader fluxLecture = null;
 
     private boolean isConnected = false;
 
     /**
      * @return le socket client
      */
     public Socket getClient() {
         return client;
     }
 
     /**
      * Constructeur
      */
     public CommBase(String serverLocation){
         try{
             this.ADRESSE_SERVEUR = serverLocation.split(":")[0];
             this.PORT_SERVEUR = Integer.parseInt(serverLocation.split(":")[1]);
         } catch (Exception ex) {
             JOptionPane.showMessageDialog(
                     null,
                     LangueConfig.getResource(OPTIONPANE_MESSAGE_BADSERVERNAME),
                     LangueConfig.getResource(OPTIONPANE_TITLE_ERROR),
                     JOptionPane.ERROR_MESSAGE
             );
             System.exit(1);
         }
     }
 
     /**
      * Definir le recepteur de l'information recue dans la communication avec le serveur
      * @param listener sera alerté lors de l'appel de "firePropertyChanger" par le SwingWorker
      */
     public void setPropertyChangeListener(PropertyChangeListener listener){
         this.listener = listener;
     }
 
     /**
      * Démarre la communication.
      * Verifie si l'application est connecte au serveur.
      * Si non, elle la connecte a ce dernier et commence a envoyer les requetes pour les formes.
      */
     public void start(){
         if(client==null || !isConnected)
             connect();
         creerCommunication();
         isConnected = true;
 	}
 
     /**
      * Démarre la communication avec une limite de formes à recevoir.
      * @param limite Nombre de formes à recevoir.
      */
     public void start(int limite){
         this.limite = limite;
         isLimited = true;
         start();
     }
 	
 	/**
 	 * Arrête la communication (pause)
 	 */
 	public void stop(){
         isActif = false;
         isConnected = false;
         isLimited = false;
 	}
 
     /**
      * Connecte l'application client au serveur sans que celle-ci envoie de
      * requete afin d'afficher les formes.
      */
     public void connect() {
         try {
             client = new Socket(ADRESSE_SERVEUR, PORT_SERVEUR);
             fluxLecture = new BufferedReader(new InputStreamReader(client.getInputStream()));
             fluxEcriture = new PrintWriter(client.getOutputStream(), true);
         } catch(Exception ex){
             JOptionPane.showMessageDialog(
                     null,
                     "\n" + String.format(LangueConfig.getResource(OPTIONPANE_MESSAGE_STARTERROR) + "\n" + ex.getMessage(), this.ADRESSE_SERVEUR, this.PORT_SERVEUR),
                     LangueConfig.getResource(OPTIONPANE_TITLE_ERROR),
                     JOptionPane.ERROR_MESSAGE
             );
         }
     }
 
     /**
      * Deconnecte l'application client du serveur en envoyant une commande END à celui-ci.
      */
     public void disconnect() {
         if(threadComm!=null)
             threadComm.cancel(true);
         isActif = false;
         isConnected = false;
         if (client != null && client.isConnected()){
             try{
                 fluxEcriture.println(END_TRAME);
                 if (!client.isClosed())
                     client.close();
             }catch(IOException ex){
                 JOptionPane.showMessageDialog(
                         null,
                         LangueConfig.getResource(OPTIONPANE_MESSAGE_STOPERROR).concat(ex.getMessage()),
                         LangueConfig.getResource(OPTIONPANE_TITLE_ERROR),
                         JOptionPane.ERROR_MESSAGE
                 );
             }
         }
     }
 	
 	/**
 	 * Créer le nécessaire pour la communication avec le serveur
 	 */
 	protected void creerCommunication(){		
 		// Crée un fil d'exécusion parallèle au fil courant,
 		threadComm = new SwingWorker(){
 			@Override
 			protected Object doInBackground() throws Exception {
 				System.out.println("\nLe fils d'execution parallele est lance");
                 int compteurFormes = 0;
 
 				while(isConnected()){
                     try{
                         Thread.sleep(DELAI);
                         fluxEcriture.println(GET_TRAME);
                         fluxLecture.readLine();
                         String trame = fluxLecture.readLine();
                         compteurFormes++;
 
                             //La méthode suivante alerte l'observateur
                             if(listener!=null){
                                 firePropertyChange("ENVOIE-TEST", null, (Object) ".");
                                 firePropertyChange("NOUVELLE-TRAME", null, (Object) trame);
                             }
                        }
 
                         if (isLimited && compteurFormes == limite)
                             disconnect();
                     } catch (Exception ex){
                         JOptionPane.showMessageDialog(
                                 null,
                                 LangueConfig.getResource(OPTIONPANE_MESSAGE_CONNEXTIONINTERRUPTED),
                                 LangueConfig.getResource(OPTIONPANE_TITLE_WARNING),
                                 JOptionPane.WARNING_MESSAGE
                         );
                         disconnect();
                     }
                 }
                 return null;
             }
         };
         if(listener!=null)
             threadComm.addPropertyChangeListener(listener); // La méthode "propertyChange" de ApplicationFormes sera donc appelée lorsque le SwinkWorker invoquera la méthode "firePropertyChanger"
         threadComm.execute(); // Lance le fil d'exécution parallèle.
         isActif = true;
     }
 
     /**
      * @return si le fil d'exécution parallèle est actif
      */
     public boolean isActif(){
         return isActif;
     }
 
     /**
      * @return si l'application est connectee au serveur
      */
     public boolean isConnected() {
         return isConnected;
     }
 }
