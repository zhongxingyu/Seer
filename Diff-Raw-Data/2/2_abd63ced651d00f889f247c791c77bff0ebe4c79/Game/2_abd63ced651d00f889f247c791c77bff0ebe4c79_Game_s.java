 package project;
 
 public class Game {
 	
 	public static int N_ROWS = 9;
 	public enum Directions {
 		UP, 
 		RIGHT, 
 		DOWN, 
 		LEFT
 	};
 	
	public boolean isValid (Player p, int x) {
 		int pos;
 		if (x==Directions.UP) {
 			pos = p.getYpos()+1;
 		} else if (x==Directions.DOWN) {
 			pos = p.getYpos()-1;
 		} else if (x==Directions.LEFT) {
 			pos = p.getXpos()-1;
 		} else if (x==Directions.RIGHT) {
 			pos = p.getXpos()+1;
 		} else {
 			return false;
 		}
 		if ((pos>=9)||(pos<0)) {
 			return false;
 		}
 		return true;
 	}
 
 }
