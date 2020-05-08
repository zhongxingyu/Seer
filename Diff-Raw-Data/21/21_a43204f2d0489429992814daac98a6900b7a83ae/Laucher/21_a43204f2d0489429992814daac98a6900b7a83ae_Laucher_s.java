 package general;
 
 import ihm.fenetres.FenetrePrincipale;
 
 import java.awt.EventQueue;
 
 import modele.Billeterie;
 
 public class Laucher {
 	/**
 	 * Lance l'application
 	 * @param args arguments de la main 
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					FenetrePrincipale frame = new FenetrePrincipale(new Billeterie("database.sqlite"));
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 }
