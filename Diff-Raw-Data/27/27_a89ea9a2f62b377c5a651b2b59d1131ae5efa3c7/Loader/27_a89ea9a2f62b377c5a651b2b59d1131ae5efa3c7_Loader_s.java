 package level;
 
 import entities.BreakableWall;
 import entities.Wall;
 import game.Game;
 
 public class Loader {
 
 	public void addWalls(String S, int zeilen, int spalten) {
 
 		int i, j, l, k = 0;
 		int[][] arr = new int[zeilen][spalten];
		for (i = 0; i < spalten; i++) {
			for (j = 0; j < zeilen; j++) {
				arr[j][i] = Integer.parseInt("" + S.charAt(k));
				l = arr[j][i];
 
 				if (l == 1) {
					Game.entities.add(new BreakableWall(i * Game.BLOCK_SIZE, j
 							* Game.BLOCK_SIZE));
 				}
 				if (l == 2) {
					Game.entities.add(new Wall(i * Game.BLOCK_SIZE, j
 							* Game.BLOCK_SIZE));
 				}
 				k++;
 			}
 		}
 	}
 }
