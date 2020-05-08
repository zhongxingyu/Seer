 package com.vulcastudios.actors;
 
 
 import java.util.LinkedList;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.StateBasedGame;
 
 import com.vulcastudios.TestGame;
 import com.vulcastudios.util.ResourceManager;
 
 public class Player{
 	
 	public static final float DELTA_X = 0.25f;
 	public static final float DELTA_Y = 0.25f;
 
 	public static final int WIDTH = 50;
 	public static final int HEIGHT = 50;
 	
 	private float xPos = 0;
 	private float yPos = 0;
 	
 	private boolean alive;
 	
 	private LinkedList<ZombieMove> movementMap;
 
 	private ResourceManager rm;
 	
 	public Player(ResourceManager rm, int xPos, int yPos){
 		this.alive = true;
 		this.rm = rm;
 		this.xPos = xPos;
 		this.yPos = yPos;
 		this.movementMap = new LinkedList<ZombieMove>();
 	}
 
 	public void update(GameContainer container, StateBasedGame game, int delta){
 
 		float oldX = this.getXPos();
 		float oldY = this.getYPos();
 		
 		ZombieMove move = new ZombieMove();
 		// Handle player movement
 		
 		// Vertical
 		if(container.getInput().isKeyDown(Input.KEY_UP)){
 			this.setYPos(getYPos() - (Player.DELTA_Y * delta));
 			move.addUp();
 		}
 		if(container.getInput().isKeyDown(Input.KEY_DOWN)){
 			this.setYPos(getYPos() + (Player.DELTA_Y * delta));
 			move.addDown();
 		}
 		
 		if(this.getYPos() > container.getHeight() - Player.HEIGHT){
 			this.setYPos(container.getHeight() - Player.HEIGHT);
 			move.noDown();
 		}
 
 		if(this.getYPos() < 0){
 			this.setYPos(0);
 			move.noUP();
 		}
 
 		if(((TestGame)game).checkCollision(this)){
 			this.setYPos(oldY);
 			move.noUP();
 			move.noDown();
 		}
 				
 		
 		// Horizontal
 		if(container.getInput().isKeyDown(Input.KEY_LEFT)){
 			this.setXPos(getXPos() - (Player.DELTA_X * delta));
 			move.addLeft();
 		}
 		if(container.getInput().isKeyDown(Input.KEY_RIGHT)){
 			this.setXPos(getXPos() + (Player.DELTA_Y * delta));
 			move.addRight();
 		}
 
 		if(this.getXPos() < 0){
 			this.setXPos(0);
 			move.noLeft();
 		}
 
 
 		if(this.getXPos() > container.getWidth() - Player.WIDTH){
 			this.setXPos(container.getWidth() - Player.WIDTH);
 			move.noRight();
 		}
 		
 
 		if(((TestGame)game).checkCollision(this)){
 			this.setXPos(oldX);
 			move.noRight();
 			move.noLeft();
 		}
 
 		movementMap.add(move);
 
 	}
 
 	public void render(GameContainer container, StateBasedGame game, Graphics g){
 		Color prev = g.getColor();
 		g.setColor(Color.pink);
 		g.drawRect(this.getXPos(), this.getYPos(), Player.WIDTH, Player.HEIGHT);
 		g.drawImage(rm.getImage("player1"), this.getXPos(), this.getYPos());
 		g.setColor(prev);
 	}
 	
 
 	public float getXPos() {
 		return xPos;
 	}
 
 	public void setXPos(float xPos) {
 		this.xPos = xPos;
 	}
 
 	public float getYPos() {
 		return yPos;
 	}
 
 	public void setYPos(float yPos) {
 		this.yPos = yPos;
 	}
 	
 	public Rectangle getBounds(){
 		return new Rectangle(this.getXPos(), this.getYPos(), Player.WIDTH, Player.HEIGHT);
 	}
 
 	public LinkedList<ZombieMove> getMovementMap() {
 		return movementMap;
 	}
 	
 	public boolean isAlive(){
 		return this.alive;
 	}
 }
