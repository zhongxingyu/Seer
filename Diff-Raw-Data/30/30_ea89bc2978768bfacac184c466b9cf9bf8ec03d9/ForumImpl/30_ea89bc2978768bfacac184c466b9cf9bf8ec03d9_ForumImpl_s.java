 import java.util.*;
 import java.io.*;
 import java.rmi.*;
 import java.rmi.server.*;
 /**
  * classe représentant l'objet servant du forum 
  */
public class ForumImpl {
     // TO DO
     public ForumImpl() throws RemoteException {
      super();
     }
 
     /**
       la structure de mémoristion des intervenants
     */
     protected HashMap intervenants = new HashMap();
     /**
     * l'identifiant unique d'intervenant
     */	
     protected Integer id = new Integer(0);
 
     
   /**
  * Enregistre un intervanant dans la structure de mémoristion des intervenants. Cette méthode est
  * appelée par le traitant de communication du programme client (IntervenantImpl) 
  * @param intervenant une reference distante vers l'intervenant
  * @param nom nom de l'intervenant
  * @param prenom prenom de l'intervenant
  * @return un identifiant interne representant l'intervenant 
  * dans la structure de mémoristion des intervenants
  */
  public synchronized HashMap enter (Intervenant intervenant, String prenom, String nom)throws RemoteException;{
   	  	
   	// TO DO
   	return null; // CETTE LIGNE EST A CHANGER
   }
   
    /**
  * De-enregistre un intervanant dans la structure de mémoristion des intervenants. Cette méthode est
  * appelée par le traitant de communication du programme client (IntervenantImpl) 
  * @param id identification de l'intervenant retourne lors de l'appel à la methode enter.
  */
  public synchronized void leave(int id) throws RemoteException;{
 	// TO DO
 		
   }
   
   
   
   
 }
 
