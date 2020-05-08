 package models;
 
 import java.util.Random;
 
 import models.tiles.LBlock;
 import models.tiles.LinePiece;
 import models.tiles.RevLBlock;
 import models.tiles.RevSquigeling;
 import models.tiles.Square;
 import models.tiles.Squigeling;
 import models.tiles.TBlock;
 import models.tiles.Tile;
 
 public class Dice {
 	private Random ran;
 	
 	public Dice(){
 		this.ran = new Random();
 	}
 	
 	public Tile roll(){
 		Tile rolledFigure = null;
 		int i = ran.nextInt() % 7;
 				
 		switch (i) {
 		case 0:
 			rolledFigure = new LinePiece();
 			break;
 			
 		case 1:
 			rolledFigure = new TBlock();
 			break;
 			
 		case 2:
 			rolledFigure = new Square();
 			break;
 			
 		case 3:
 			rolledFigure = new Squigeling();
 			break;
 			
 		case 4:
 			rolledFigure = new RevSquigeling();
 			break;
 			
 		case 5:
 			rolledFigure = new LBlock();
 			break;
 			
 		case 6:
 			rolledFigure = new RevLBlock();
 			break;
 		}
 		
 		return rolledFigure;
 		
 	}
 }
