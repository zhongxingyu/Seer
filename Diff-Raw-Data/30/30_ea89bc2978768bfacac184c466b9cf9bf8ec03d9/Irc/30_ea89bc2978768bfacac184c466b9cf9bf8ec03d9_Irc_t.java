 import java.awt.*;
 import java.awt.event.*;
 import java.lang.*;
 
 
 /**
 * Cette classe est la classe principale constituant le programme client.
 * Elle est composé d'une fonction main qui à pour rôle d'instancier 
 * l'interface graphique associée au client (IrcGui) ainsi qu'un objet 
 * gérant les communications avec le forum (IntervenantImpl).
 * 
 */
 
 public class Irc {
          
     public static void main(String args[]) {			
 	try{
 	
 	// TO DO !!!
         // create the GUI
         // instanciate the intervenant implementation
		System.out.println("Client running");
 	} catch (Exception e) {
           System.out.println("ERROR : " + e) ;
 	  e.printStackTrace(System.out);
 	  }
     }
 }
 
