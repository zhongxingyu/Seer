 package com.se1by.ETM.Entities;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.tiled.TiledMap;
 
 import com.se1by.ETM.util.Vector2i;
 
 
 public class Player implements LivingEntity {
 	
 	Vector2i pos;
 	Image image;
 	TiledMap map; //FUCKING DAMN UGLY WAY BUT SHOULD DO THE TRICK
 	
 	public Player(TiledMap map){
 		this.map = map;
 	}
 
 	@Override
 	public void setPosition(Vector2i pos) {
 		this.pos = pos;
 	}
 
 	@Override
 	public Vector2i getPosition() {
 		return pos;
 	}
 	
 	@Override
 	public void addPosition(Vector2i vec) {
 		pos.add(vec);
 	}
 
 	@Override
 	public void setImage(Image image) {
 		this.image = image;
 	}
 
 	@Override
 	public Image getImage() {
 		return image;
 	}
 
 	@Override
 	public void render(GameContainer con, Graphics g) throws SlickException {
 		g.drawImage(image, pos.getX(), pos.getY());
 	}
 
 	@Override
 	public void update(GameContainer con, int delta) throws SlickException {
 		Vector2i oldTilePos = getCurrentTilePos();
 		
 		//INPUT
 		Input input = con.getInput();
 		
 		if(input.isKeyDown(Input.KEY_W)){
 			up();
 		}
 		if(input.isKeyDown(Input.KEY_S)){
 			down();
 		}
 		if(input.isKeyDown(Input.KEY_A)){
 			left();
 		}
 		if(input.isKeyDown(Input.KEY_D)){
 			right();
 		}
 		
 		//SCREEN COLLISION
 		if(pos.getX() + image.getWidth() > con.getWidth()){
 			setPosition(new Vector2i(con.getWidth() - image.getWidth() - 1, pos.getY()));
 		}
 		else if(pos.getX() < 0){
 			setPosition(new Vector2i(0, pos.getY()));
 		}
 		if(pos.getY() + image.getHeight() > con.getHeight()){
 			setPosition(new Vector2i(pos.getX(), con.getHeight() - image.getHeight() - 1));
 		}
 		else if(pos.getY() < 0){
 			setPosition(new Vector2i(pos.getX(), 0));
 		}
 		
 		//WALL/TERRAIN COLLISION
 		if(inBlocked()){
 			
 			
 			setPosition(oldTilePos.multiply(map.getTileWidth(), map.getTileHeight()));
 			System.out.println("blocked");
 		}
 		
 	}
 
 	@Override
 	public void init(GameContainer con) throws SlickException {
 		setImage(new Image("res/player.png"));
 		setPosition(new Vector2i(50,50));
 	}
 	
 	private void right() {
		addPosition(new Vector2i(1,0));
 	}
 
 	private void left() {
		addPosition(new Vector2i(-1,0));
 	}
 
 	private void down() {
		addPosition(new Vector2i(0,1));
 	}
 
 	private void up() {
		addPosition(new Vector2i(0,-1));
 	}
 	
 	private int getTileIDAtPlayerPos(){
 		return map.getTileId(getPosition().getX()/map.getTileWidth(), getPosition().getY()/map.getTileHeight(), 0);
 	}
 	
 	private Vector2i getCurrentTilePos(){
 		int x = getPosition().getX()/map.getTileWidth();
 		int y = getPosition().getY()/map.getTileHeight();
 		
 		return new Vector2i(x, y);
 	}
 	
 	private boolean inBlocked(){
 		//upper left
 		if(getTileIDAtPlayerPos() == 1){
 			System.out.println("by ul");
 			return true;
 		}
 		
 		//upper right
 		int x = (getPosition().getX() + getImage().getWidth())/map.getTileWidth();
 		System.out.println("x: " + x);
 		System.out.println("tw: " + map.getTileWidth());
 		System.out.println("w: " + map.getWidth());

 		while(x >= map.getWidth()){
 			x--;
 			System.out.println("new x = " + x);
 		}
 		if(map.getTileId(x, getPosition().getY()/map.getTileHeight() , 0) == 1){
 			System.out.println("by ur");
 			return true;
 		}
 		
 		//lower left
		if(map.getTileId(getPosition().getX()/map.getTileWidth(), (getPosition().getY())/map.getTileHeight(), 0) == 1){
 			System.out.println("by ll");
 			return true;
 		}
 		
 		//lower right
		if(map.getTileId((getPosition().getX())/map.getTileWidth(), (getPosition().getY() + getImage().getHeight())/map.getTileHeight(), 0) == 1){
 			System.out.println("by lr");
 			return true;
 		}
 		
 		return false;
 	}
 
 }
