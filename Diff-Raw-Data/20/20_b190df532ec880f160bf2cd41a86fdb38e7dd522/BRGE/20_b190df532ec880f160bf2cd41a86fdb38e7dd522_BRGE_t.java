 package brwyatt.brge;
 
 import brwyatt.brge.graphics.GameWindow;
 import brwyatt.brge.graphics.ScreenObjects;
 
 public class BRGE { //Main class
 	private static int width=800;
 	private static int height=600;
 	private static GameWindow gw;
 	private static ScreenObjects screenObjects;
 	private static Game theGame;
 	
 	public static void main(String args[]){
		start(new TestGame(new ScreenObjects())); //change this to whichever game you want!
 	}
 	
 	public static void start(Game game){
 		if(theGame==null){ //only start once
 			theGame=game;
			screenObjects=game.getScreenObjects();
 			gw=new GameWindow(screenObjects);
 			(new Thread(gw)).start(); //Load GameWindow and start rendering
 			
 			theGame.startGame();//start the game!
 		}
 	}
 	
 	public static String getGameTitle(){
 		return theGame.getGameTitle();
 	}
 	public static int getWidth(){
 		return width;
 	}
 	public static int getHeight(){
 		return height;
 	}
 	public static void keyPressed(int key){
 		theGame.keyPressed(key);
 	}
 	public static void keyReleased(int key){
 		theGame.keyReleased(key);
 	}
 	public static void keyTyped(int key){
 		theGame.keyTyped(key);
 	}
 	public static void exit(){
 		//cleanup and exit
 		System.exit(0);//exit
 	}
 }
