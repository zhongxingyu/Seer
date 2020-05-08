 package main;
 import game.Game;
 import interaction.Input;
 import interaction.Renderer;
 import interaction.WindowUtilities;
 
 import java.util.Random;
 
 import javax.swing.JFrame;
 
 import util.Line2D;
 import util.Vector2D;
 
 import engine.Field;
 import engine.MainLoop;
 
 
 public class Battlepath {
 	static Random rand = new Random();
 	
 	public static void main(String[] args) {
 
 		int tileSize = 20;
 		int fieldWidth = 60;
 		int fieldHeight = 30;
 		Field f = new Field(fieldWidth, fieldHeight, tileSize);
		randomCircles(f, fieldWidth*fieldHeight/20, 0.15);
 	
 		Vector2D start;
 		start = new Vector2D(rand.nextDouble()*fieldWidth, rand.nextDouble()*fieldHeight);
 		while(f.tileValueAt(start) == 1)
 			start = new Vector2D(rand.nextDouble()*fieldWidth, rand.nextDouble()*fieldHeight);
 		
 		
 		JFrame frame = WindowUtilities.openFrame(f.tilesX*tileSize,f.tilesY*tileSize);
 		
 
 		Game game = new Game(f,start);
 
 		Renderer renderer = new Renderer(game,tileSize,frame);
 		new Input(game, frame, renderer);
 
 		MainLoop.startLoop(renderer, game);
 
 	}
 	
 	public static void randomCircles(Field f, int n, double maxr) {
 		for(int i=0; i<n; i++) {
			f.createCricle(new Vector2D(rand.nextDouble()*f.tilesX/f.tileSize, rand.nextDouble()*f.tilesY/f.tileSize), rand.nextDouble()*maxr);
 		}
 	}
 }
