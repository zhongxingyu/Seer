 package joueurs;
 
 import carte.bottes.Botte;
 import jeu.PartieDeJeu;
 /**
  * Represente les actions des humains
  * @author Damien
  *
  */
 public class Humain extends Joueur {
 
 
 	
 	public Humain(String nomJoueur, int numPassage) {
 		super(nomJoueur, numPassage);
 				
 	}
 	
 	@Override
 	public synchronized void jouer() { 
 	
 		try {
 			wait();
 		} catch (InterruptedException e) {
 			
 			e.printStackTrace();
 		}
 
 	}
 	
 	/**
 	 * Notifie les coup fourrés
 	 * @param botte
 	 */
 	@Override
	public  void coupFourre(Botte botte) {
 		this.setChanged();
 		this.notifyObservers("coupFourree");
		
 		
 	}
 
 
 }
