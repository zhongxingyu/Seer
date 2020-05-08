 import java.awt.*;
 import java.awt.event.*;
 import java.lang.*;
 import java.rmi.*;
 import java.rmi.server.*;
 
 
 /**
  * Cette classe défini le traitant de communication du programme client.
  * Elle est utilisée par les classes connectListener,writeListener,whoListener,leaveListener 
  * du GUI pour effectuer les communications distante avec le forum.
 */
 public class IntervenantImpl {
   //TO DO
  
   private static IrcGui gui;
   
   /**
   * Référence distante vers un forum.
   */
   private static Forum forum=null; //ref vers forum   
   
   /**
  * Identification du client dans le forum. Cet identifiant est retourné 
  * lors de l'appel à la méthode enter sur le forum distant.
  */
   private int id;
   
   /**
  * nom de l'intervenant
  */
   private String nom;
   /**
  * prenom de l'intervenant
  */
   private String prenom;
   
   /**
  * constructeur de la classe IntervenantImpl. Le nom et le prenom de l'intervenant sont passés
  * en parametre du programme client (irc.java)
  * @param nom nom de l'intervenant
  * @param prenom prenom de l'intervenant
  */
   public  IntervenantImpl (String nom, String prenom) throws RemoteException {
   	super();
   	this.nom = nom;
   	this.prenom = prenom;
   }
   
  
     
   /**
  * Fixe une reference directe vers le gui (IrcGui). Cette reference est utilisée 
  * par le traitant de communication pour imprimer des message de chat dans le gui 
  * via la methode print definie dans IrcGui.
  * @param gui le GUI
  */
   public void setGUI(IrcGui gui){
   	this.gui = gui;
   }
   
   /**
  * Execute la methode enter sur le forum. Cette methode est appelé par le traitant 
  * writeListener défini dans IrcGui. Cette méthode doit utiliser un serveur de nom 
  * pour obtenir une référence distante vers le forum et exécuter la méthode enter 
  * dessus.
  * @param forum_name nom du forum 
  */
   public void enter (String forum_name) throws Exception {
   	
   	// TO DO
   }
 	
   
   /**
  * Execute la methode say sur le forum. Cette methode est appelé par le traitant 
  * writeListener défini dans IrcGui. Cette méthode doit utilise une référence distante 
  * vers le forum et exécuter la méthode say dessus.
  * @param msg message à envoyer aux intervenants enregistrer dans le forum. 
  */
   public void say (String msg) throws Exception {
   	
   		// TO DO
 	
   }
   
   /**
  * Cette methode est appelé par le forum pour imprimer un nouveau message de 
  * chat a l'intervenant. Cette impression est déléguée à la méthode print définie 
  * dans IrcGui. 
  * @param msg nouveau message à imprimer dans le gui.
  */
  public void listen (String msg) throws PreconditionException {
   	// TO DO
   } 
   
  
 
   public void  addNewClient(Intervenant i) throws RemoteException{
   	// TO DO
   } 
  
   public void delNewClient(Intervenant i) throws RemoteException{
   	// TO DO
   } 
 
   /**
  * Execute la methode leave sur le forum. Cette methode est appelé par le traitant 
  * leaveListener défini dans IrcGui. Cette méthode doit utilise une référence distante 
  * vers le forum et exécuter la méthode leave dessus.
  */
   public void leave() throws Exception {
 	// TO DO
   }
   
   /**
  * Execute la methode who sur le forum. Cette methode est appelé par le traitant 
  * whoListener défini dans IrcGui. Cette méthode doit utilise une référence distante 
  * vers le forum et exécuter la methode who dessus.
  */
   public String who() throws Exception {
 	// TO DO
 	return null; // CETTE LIGNE EST A CHANGER
   }
 }
