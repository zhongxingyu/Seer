 package baseGame;
 
 import javax.swing.JFrame;
 
 import networking.NetworkObjectType;
 
 import blueMaggot.Game;
 
 public class derp extends JFrame {
 	Game game;
 	public static int playerNumber;
 
 	public derp() {
 		setVisible(true);
 		game = new Game();
 		add(game);
 	}
 
 	public void init() {
 
 	
 
 //	game.initConnection(true,"");playerNumber =2;
	game.initConnection(false,"127.0.0.1");playerNumber = 1;	
 //	game.initConnection(false, "78.91.9.98");
 
 	
 		
 
 
 	}
 
 	public static void main(String[] args) {
 		new derp().init();
 	}
 }
