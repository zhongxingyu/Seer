 package wars.dragon.engine;
 
 /* Test class */
 public class Grass extends GameField {
     
     public Grass() {
	super("Grass", 0.0);
     }
 
     public Boolean doesAcceptUnit(Unit unit) {
 	return true;
     }
 }
     
