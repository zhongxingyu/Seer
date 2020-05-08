 package aeroport.sgbag.kernel;
 
 import java.util.Iterator;
 
 import lombok.*;
 
 @AllArgsConstructor
 @NoArgsConstructor
 @EqualsAndHashCode(callSuper = false)
 public class Rail extends ElementCircuit {
 
 	@Getter
 	@Setter
 	private Noeud noeudSuivant;
 
 	@Getter
 	@Setter
 	private Noeud noeudPrecedent;
 
 	@Getter
 	@Setter
 	private int length;
 
 	public Boolean update() {
 
 		Iterator<Chariot> ite = listeChariot.descendingIterator();
 
 		while (ite.hasNext()) {
 			Chariot c = ite.next();
 
 			int newPosition = c.getPosition() + c.getMaxMoveDistance();
 			Chariot chariotSuivant;
 
 			try {
 				chariotSuivant = listeChariot.get(listeChariot.indexOf(c) + 1);
 			} catch (IndexOutOfBoundsException e) {
 				chariotSuivant = null;
 			}
 
 			if (chariotSuivant != null
 					&& c.willCollide(newPosition, chariotSuivant)) {
 				c.setPosition(chariotSuivant.getRearPosition() - 3);
 			} else {
 				if (newPosition >= length) { // Le Chariot sort
 					if (noeudSuivant.registerChariot(c)) {
 						ite.remove();
 					} else {
 						c.setPosition(length - c.getLength() / 2);
 					}
 				} else { // Cas nominal
 					if (newPosition > (length - c.getLength() / 2))
 						newPosition = length - c.getLength() / 2;
 						
 					c.setPosition(newPosition);
 				}
 			}
 		}
 
 		return true;
 	}
 }
