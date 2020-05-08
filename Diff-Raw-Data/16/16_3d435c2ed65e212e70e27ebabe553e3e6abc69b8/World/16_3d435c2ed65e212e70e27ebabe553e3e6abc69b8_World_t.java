 import java.awt.Graphics;
 import java.util.ArrayList;
 
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 
 public class World extends JPanel {
 	public static ArrayList<Actor> actors = new ArrayList<Actor>();
 	
 	static final int GRID_SIZE = 32;
 	
	private int width;
	private int height;
 	
 	private ArrayList <Player> players;
 	
 	
 	public World(int width, int height,int numPlayers) {
 		
 		players = new ArrayList<Player>(numPlayers);
		this.width = width;
		this.height = height;
 		
 		for(int i =1;i<=numPlayers;i++){
 			Player x =  new Player(100*i,height-100);
 			players.add(x);
 			this.addChild(x);
 		}
 		
 		Block block = new Block(200, height-100,1);
 		
 		this.addChild(block);
 		
 		Block floor = new Block(0, height - GRID_SIZE*2, width/GRID_SIZE);
 		addChild(floor);
 	}
 	
 	public void update() {
 		
 		for (Actor a : actors){
 			a.move();
			/*if(a instanceof Obstaclep){
 				a.getCollisionBox();
 				for(Player b : players){
 					b.willCollideWith(a);
 				}
			}*/
 		}
 	}
 	
 	public void paint(Graphics g) {
		super.paintComponents(g);
		g.clearRect(0, 0, width, height);
 		for (Actor a : actors)
 			a.paint(g);
 	}
 	
 	public void addChild(Actor a) {
 		actors.add(a);
 	}
 	public ArrayList<Player> getPlayers(){
 		return players;
 	}
 }
