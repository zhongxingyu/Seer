 import java.net.Socket;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 //Player class to hold image, position, drunk_level;
 class Player {
 	float x,y;
 	int drunk_level;
 	boolean is_jumping; //physics
 	Rectangle hitbox;
 	Image sprite;
 	
 	public Player() throws SlickException{
 		x = 500.0f;
 		y = 1.0f; //set random starting location
 		drunk_level = 0;
 		is_jumping = false;
 		sprite = new Image("img/ball.png");
 		hitbox = new Rectangle(x,y,sprite.getWidth(),sprite.getHeight());
 	}
 	
 	public float getX(){
 		return x;
 	}
 	
 	public float getY(){
 		return y;
 	}
 	
 	public void setX(float newx){
 		x = newx;
 		hitbox.setX(newx);
 	}
 	
 	public void setY(float newy){
 		y = newy;
 		hitbox.setY(newy);
 	}
 	
 	public Image getImage(){
 		return sprite;
 	}
 	
 	public Rectangle getHitbox(){
 		return hitbox;
 	}
 }
 
 class PlayerThread extends Thread {
 	Socket socket;
 	MyConnection conn;
 	String msg;
 	int active, id; // active = number of connected players
 	Player[] players; //reference to the players in the Client class
 	
 	public PlayerThread(Socket socket, Player[] players) {
 		this.socket = socket;
 		this.conn = new MyConnection(socket);
 		this.msg = "connected!";
 		this.active = 0;
 		this.players = players;
 	}
 	
 	//TODO manage protocols here 
 	public void run() {
 		while (true) {
 			msg = conn.getMessage();
 			System.out.println("Message: " + msg);
 
 			if (msg.substring(0, 7).equals("Active ")) {
 				active = Integer.parseInt(msg.substring(7));
 			} 
 			else if (msg.substring(0, 7).equals("Number:")) {
 				id = Integer.parseInt(msg.substring(8));
 			}
 			//PROTOCOL: MOVE <PLAYERID> <X> <Y>
 			//example: MOVE 2 356 45
 			else if(msg.substring(0,4).equals("MOVE")){
 				System.out.println("Moveing " + msg.charAt(5));
 				int player_id = msg.charAt(5) - 48;
 				float x,y;
 				String temp_x = "", temp_msg = msg.substring(7);
 				//PARSE THE <X> AND <Y>
 				temp_x = temp_msg.substring(0, temp_msg.indexOf(' '));
 				x = Float.parseFloat(temp_x);
 				y = Float.parseFloat(temp_msg.substring(temp_msg.indexOf(' ')));
 				players[player_id].setX(x);
 				players[player_id].setY(y);
 			}
 		}
 	}
 }
 
 public class Client extends BasicGameState {
 
 	Socket socket;
 	PlayerThread thread;
 	Player[] players = new Player[4];
 	Rectangle[] platforms = new Rectangle[3];
 	float moveSpeed = 1.0f; //finer movement
 	int id, active;
 	
 	public Client(int state) {
 		try {
 			socket = new Socket("127.0.0.1", 8888);
 		} catch (Exception e) {}
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg)
 			throws SlickException {;
 		for (int i = 0; i < 4; i++) players[i] = new Player();
		platforms[0] = new Rectangle(200,100,300,40);
		platforms[1] = new Rectangle(200,300,300,40);
		platforms[2] = new Rectangle(200,500,300,40);
 		thread = new PlayerThread(socket, players);
 		thread.start();
 		active = 1;
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {
 		g.drawString(" " + thread.msg, 20, 20);
 		g.drawString("ID: " + id, 700, 20);
 		//value of thread.active?
 		//thread.active = 1;
 		//render all the images of the players
 		//System.out.println("Thread.active: " + thread.active);
 		for (int i = 0; i < active; i++){
 			g.drawImage(players[i].getImage(), players[i].getX(), players[i].getY());
 		}
 		for (int i = 0; i < 3; i++){
 			g.draw(platforms[i]);
 		}
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta)
 			throws SlickException {
 		active = thread.active;
 		id = thread.id;
 		float past_x = players[id].getX();
 		float past_y = players[id].getY();
 		
 		//set new X and Y only if intersects is false
 		Input input = gc.getInput();
 		if (input.isKeyDown(Input.KEY_DOWN)) {
 			players[id].setY(players[id].getY() + moveSpeed * delta);
 		}
 		if (input.isKeyDown(Input.KEY_RIGHT)) {
 			players[id].setX(players[id].getX() + moveSpeed * delta);
 		}
 		if (input.isKeyDown(Input.KEY_LEFT)) {
 			players[id].setX(players[id].getX() - moveSpeed * delta);
 		}
 		if (input.isKeyDown(Input.KEY_UP)) {
 			players[id].setY(players[id].getY() - moveSpeed * delta);
 		}
 
 		//check for collision for the player and platforms
 		for(int i=0; i<3; i++){
 			if(platforms[i].intersects(players[id].getHitbox())){
				if (past_y<platforms[i].getY()) {
 					players[id].setY(platforms[i].getY()-1-players[id].getHitbox().getHeight());
 				}
				if (past_x<platforms[i].getX()) {
 					players[id].setX(platforms[i].getX()-1-players[id].getHitbox().getWidth());
 				}
				if (past_x>(platforms[i].getX()+platforms[i].getWidth())) {
 					players[id].setX(platforms[i].getX()+platforms[i].getWidth()+1);
 				}
				if (past_y>(platforms[i].getY()+platforms[i].getHeight())) {
 					players[id].setY(platforms[i].getY()+platforms[i].getHeight()+1);
 				}
 				break;
 			}
 		}
 		
 		//check for changes in coordinates then broadcast the MOVE message
 		if(past_x!=players[id].getX() || past_y!=players[id].getY()){
 			thread.conn.sendMessage("MOVE "+players[id].getX()+" "+players[id].getY());
 		}
 	}
 
 	@Override
 	public int getID() {
 		return 2;
 	}
 }
