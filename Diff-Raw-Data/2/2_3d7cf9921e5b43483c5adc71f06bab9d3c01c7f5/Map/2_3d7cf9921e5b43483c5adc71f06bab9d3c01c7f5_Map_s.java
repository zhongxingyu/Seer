 //package wars.dragon.engine;
 
 import java.util.*;
 
 public class Map {
 
     List< List<GameField> > fields;
     
     public Map(List< List<GameField> > fields) {
 	this.fields = fields;
     }
     
     public Boolean isInstantiated() {
 	return fields != null;
     }
     
     public GameField getField(Position position) {
 	return getField(position.getX(), position.getY());
     }
     
     public GameField getField(Integer x, Integer y) {
 	return fields.get(x).get(y);
     }
 
     public String toString() {
 	String m = "";
	for (ArrayList<GameField> agf : this.fields) {
 	    for (GameField gf : agf) {
 		m += gf.toString().charAt(0);
 	    }
 	    m += '\n';
 	}
 	return m;
     }
 }
