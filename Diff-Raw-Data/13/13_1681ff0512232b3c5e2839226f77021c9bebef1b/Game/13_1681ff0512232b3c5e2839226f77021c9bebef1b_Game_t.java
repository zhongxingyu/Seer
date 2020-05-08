 import java.util.ArrayList;
 
 public class Game {
 	public static void main(String[] args) {
 		ArrayList<GameObject> objects = new ArrayList<GameObject>();
 		
		GameObject o = new GameObject(new Vec2D(1,0,0,1), 1);
		GameObject o2= new GameObject(new Vec2D(0,0,90,1), 2);
 		objects.add(o);
		objects.add(o2);
 		
 		GameBoard g = new GameBoard(5,5);
 		
 		int turns = 10;
 		for (int i = 0; i < turns; i++) {
 			// Put the objects on the board
 			g.placeObjects(objects);
 			
 			// Print out the board
 			System.out.println("Turn " + i);
 			System.out.println(g);
 			
 			// Move the objects
 			for (GameObject ob : objects) {
 				ob.move();
 			}
 		}
 		
 	}
 }
