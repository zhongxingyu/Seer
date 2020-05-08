 import java.util.ArrayList;
 
 
 public class Game {
 	
 	private static ArrayList<Player> players;
 	private static int playerIndex = 0;
 	public static GUI home;
 	private static int unownedPies = 0;
 //	private static Pie[][] map;
 //	private static int size;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
		
 		
 		//size = 5;
 		//map = Initiate.generateMap(size);
 		
 		home = new GUI();
 
 	}
 	
 //	public Game(Player[] p, )
 	
 	public static void newGame(){
 		unownedPies = 0;
 		playerIndex = 0;
		home.newGame();
 		for(Player p : players){
 			p.unlose();
 		}
 		ExplosionManager.reset();
 	}
 
 	public static Player getCurrentPlayer(){
 		return players.get(playerIndex);
 	}
 	
 	public static void nextPlayer(){
 		if(++playerIndex == players.size()) playerIndex = 0;
 		getCurrentPlayer().checkLoss();
 		if(getCurrentPlayer().hasLost()) nextPlayer();
 	}
 	
 	/**
 	 * ALWAYS RUN AT START
 	 */
 	public static void setPlayers(ArrayList<Player> p){
 		players = p;
 	}
 	
 	public static void addUnownedPie(){
 		unownedPies++;
 	}
 	
 	public static void removeUnownedPie(){
 		unownedPies--;
 	}
 	
 	
 	public static int getUnownedPies(){
 		return unownedPies;
 	}
 	
 	public static void checkGameEnd(){
 		Player unlostPlayer = null;
 		for(Player p : players){
 			if(!p.hasLost()){
 				if(unlostPlayer != null) return;
 				unlostPlayer = p;
 			}
 		}
 		win(unlostPlayer);
 	}
 	
 	private static void win(Player p){
 		ExplosionManager.flush();
 		String message = "";
 		if(p == null) message = "There is no winner...";
 		else message = p.getColor()+" wins!";
 		javax.swing.JOptionPane.showMessageDialog(null, message);
 	}
 	
 }
