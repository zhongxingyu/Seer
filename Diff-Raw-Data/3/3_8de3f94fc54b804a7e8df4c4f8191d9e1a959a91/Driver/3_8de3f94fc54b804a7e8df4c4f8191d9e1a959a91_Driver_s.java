 package driver;
 
 import world.Grid;
 import gui.GameDisplay;
 
 public class Driver {
 
 	public static void main(String[] args) {
 		Grid g = new Grid();
 		GameDisplay display = new GameDisplay(g);
 		long s = System.currentTimeMillis();
 		while ((s - System.currentTimeMillis()) < 0){
			s = System.currentTimeMillis();
 			if ((System.currentTimeMillis() - s) >= 1000){
 				display.redraw(g);
 			}
 		}
 	}
 
 }
