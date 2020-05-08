 package projectmayhem;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Polygon;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import maps.*;
 
 public class MapHandler extends BasicGameState{
 	
 	private static int ID;
 	public TestMap map;
 	private float delta;
 	private boolean showStats;
 	private Block collisionBlock;
 	private Player player1,  player2;
 	
 	private final float HORISONTAL_VELOCITY = 0.25f;
 	private final float VERTICAL_STARTING_VELOCITY = 1.5f;
 	private final float VERTICAL_GRAVITY = 0.007f;
 	
 	public MapHandler(int ID){
 		this.ID = ID;
 	}
 	
 	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
 		player1 = new Player("Player 1");
 		player2 = new Player("Player 2");
 		
 		map = new TestMap("graphics/maps/test.tmx");
 		showStats = true;
 		
 		//RUN PLAYER METHODS
 		player1.setIsJumping(false);
 		player2.setIsJumping(false);
 		
 		player1.setMoveRightKey(Input.KEY_RIGHT);
 		player1.setMoveLeftKey(Input.KEY_LEFT);
 		player1.setJumpKey(Input.KEY_UP);
 		player1.setAttack1Key(Input.KEY_SPACE);
 		player2.setMoveRightKey(Input.KEY_D);
 		player2.setMoveLeftKey(Input.KEY_A);
 		player2.setJumpKey(Input.KEY_W);
 		player2.setAttack1Key(Input.KEY_LCONTROL);
 		
 		player1.setMaxHealth(100);
 		player1.setCurrentHealth(100);
 		player2.setMaxHealth(100);
 		player2.setCurrentHealth(100);
 		
 		player1.getAnimation().setAutoUpdate(true);
 		player2.getAnimation().setAutoUpdate(true);
 		//----------------------------
 
 		// SET STABLE FRAMERATE
 		gc.setTargetFrameRate(100);
 		//-------------------------
 		
 		player1.setX(map.getSpawn1(0)); player1.setY(map.getSpawn1(1)); //TODO This should be independent of player character size
 		player2.setX(map.getSpawn2(0)); player2.setY(map.getSpawn2(1));
 	}
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
 		g.setAntiAlias(true);
 		// RENDER CHARACTER AND MAP
 		map.map.render(0, 0);
 		player1.getAnimation().draw(player1.getX(), player1.getY());
 		player2.getAnimation().draw(player2.getX(), player2.getY());
 		g.setColor(Color.white);
 		g.drawString(player1.getName(), player1.getCenterX() - (int)(g.getFont().getWidth(player1.getName())/2), player1.getY() - g.getFont().getLineHeight());
 		g.drawString(player2.getName(), player2.getCenterX() - (int)(g.getFont().getWidth(player2.getName())/2), player2.getY() - g.getFont().getLineHeight());
 		//-------------------------
 		
 		// RENDER OPTIONAL STATS
 		
 		if(showStats){
 			//RENDER PLAYER 1 STATS
 			g.setColor(Color.green);
 			g.draw(player1.getAttack1Polygon());
 			g.setColor(Color.magenta);
 			g.draw(player1.getPolygon());
 			g.drawString("Player 1: " + String.format("%.3f", player1.getX()) + " CharY: " + String.format("%.3f", player1.getY()), 0, 30);
 			g.drawString("yVelocity: " + player1.getYVelocity(), 0, 50);
 			g.drawString("isJumping: " + player1.isJumping(), 0, 70);
 			g.drawString("playerPoly Y: " + player1.getPolyY(), 0, 90);
 			g.drawString("isAlive: " + player1.isAlive(), 0, 110);
 			g.drawString(player1.getAnimationName() + ": " + player1.getAnimation().getFrame(), 0, 130);
 			g.drawString("isAttacking1: " + player1.isAttacking1(), 0, 150);
 			g.drawString("Facing Direction: " + player1.getFacingDirection(), 0, 170);
 			g.drawString("Health: " + player1.getCurrentHealth(), 0, 190);
 			//----------------------------------
 			
 			//RENDER PLAYER 2 STATS
 			g.setColor(Color.green);
 			g.draw(player2.getAttack1Polygon());
 			g.setColor(Color.magenta);
 			g.draw(player2.getPolygon());
 			g.drawString("Player 2: " + String.format("%.3f", player2.getX()) + " CharY: " + String.format("%.3f", player2.getY()), 600, 30);
 			g.drawString("yVelocity: " + player2.getYVelocity(), 600, 50);
 			g.drawString("isJumping: " + player2.isJumping(), 600, 70);
 			g.drawString("playerPoly Y: " + player2.getPolyY(), 600, 90);
 			g.drawString("isAlive: " + player2.isAlive(), 600, 110);
 			g.drawString("Health: " + player2.getCurrentHealth(), 600, 130);
 			//----------------------------------
 			
 			//RENDER MISC STATS
 			g.drawString("delta: "  + this.delta, 400, 30);
 			//----------------------------------
 			
 			for(int i = 0; i < map.getBlock().size(); i++){
 				Block b = (Block)map.getBlock().get(i);
 				g.draw(b.blockPoly);
 			}
 		}
 	}
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
 		this.delta = delta;
 		Input input = gc.getInput();
 		
 		// TOGGLE STATS
 		if(input.isKeyPressed(Input.KEY_S)){
 			if(showStats){
 				showStats = false;
 			}
 			else{
 				showStats = true;
 			}
 		}
 		//-------------------------------
 		
 		//CHECK IF PLAYERS ARE ALIVE
 		if(player1.getY() >= map.getKillY()){
 			player1.setAlive(false);
 		}
 		if(player2.getY() >= map.getKillY()){
 			player2.setAlive(false);
 		}
 		//--------------------------------
 		
 		//REVIVE PLAYERS
 		if(player1.isAlive() == false){
 			if(player1.getReviveTime() >= 1000){
 				player1.setAlive(true);
 				player1.setX(map.getSpawn1(0));
 				player1.setY(map.getSpawn1(1));
 				player1.setReviveTime(0);
 				player1.setCurrentHealth(100);
 			}
 			player1.setReviveTime(player1.getReviveTime() + delta);
 		}
 		if(player2.isAlive() == false){
 			if(player2.getReviveTime() >= 1000){
 				player2.setAlive(true);
 				player2.setX(map.getSpawn2(0));
 				player2.setY(map.getSpawn2(1));
 				player2.setReviveTime(0);
				player1.setCurrentHealth(100);
 			}
 			player2.setReviveTime(player2.getReviveTime() + delta);
 		}
 		//--------------------------------------
 		
 		// PLAYER 1 MOVEMENT AND ATTACKS
 		if(input.isKeyPressed(player1.getAttack1Key()) && player1.isAttacking1() == false){
 			player1.triggerAttack1();
 		}
 		
 		if(player1.isAttacking1() && player2.getHasBeenAttacked() == false){
 			if(player1.getAnimation().getFrame() == player1.getFrameOfAttack1()){
 				if(attackCollision(player1.getAttack1Polygon())){
 					player2.setCurrentHealth(player2.getCurrentHealth() -10);
 					player2.setHasBeenAttacked(true);
 					if(player2.getCurrentHealth() <= 0){
 						player2.setAlive(false);
 					}
 				}
 			}
 			if(player1.getAnimation().getFrame() == player1.getFrameOfAttack1()){  //If the animation has movement before attack
 				if(player1.getFacingDirection() == 1){
 					player1.setAttack1PolyX(player1.getX() + player1.getPolygon().getWidth());
 				}
 				else{
 					player1.setAttack1PolyX(player1.getX() - player1.getAttack1Polygon().getWidth());
 				}
 			}
 		}
 		if(player1.isAttacking1()){ //Reset polygon after attacking
 			if(player1.getAnimation().getFrame() == player1.getAnimation().getFrameCount() - 1){
 				player1.setAnimationProtected(false);
 				player1.setAnimation(Player.IDLE);
 				player1.resetAttack1PolyPosition();
 				player1.setAttacking1(false);
 				player2.setHasBeenAttacked(false);
 			}
 		}
 		if(input.isKeyDown(player1.getMoveRightKey())){
 			moveRight(player1);
 		}
 		if(input.isKeyDown(player1.getMoveLeftKey())){
 			moveLeft(player1);
 		}
 		// START JUMP FOR PLAYER 1
 		if(input.isKeyPressed(Input.KEY_UP) && player1.isJumping() == false){ //you can jump as long as you're not airborne
 			player1.setIsJumping(true);
 			player1.setYVelocity(VERTICAL_STARTING_VELOCITY);
 		}
 		// STARTS THE JUMP LOOP
 		if(player1.isJumping()){
 			Jump(player1);
 		}
 		//---------------------------
 		
 		// CHECKS IF THE CHARACTER IS ON THE GROUND
 		player1.setPolyY(player1.getY() + 1); //check if player is only 1 pixel above ground
 		if(entityCollision(player1.getPolygon())){
 			player1.setIsJumping(false);
 			player1.setYVelocity(0);
 			player1.setY(collisionBlock.blockPoly.getMinY() - (player1.getPolygon().getHeight()+1));
 		} // IF NOT, ACCELERATE DOWNWARD
 		else if(player1.isJumping() == false){ 
 			player1.setYVelocity(player1.getYVelocity() + VERTICAL_GRAVITY*delta);
 			player1.setY(player1.getY() + player1.getYVelocity()*delta);
 		}
 		//---------------------------------
 		
 		// PLAYER 2 MOVEMENT AND ATTACKS
 		if(input.isKeyPressed(player2.getAttack1Key()) && player2.isAttacking1() == false){
 			player2.triggerAttack1();
 		}
 		
 		if(player2.isAttacking1() && player1.getHasBeenAttacked() == false){
 			if(player2.getAnimation().getFrame() == player2.getFrameOfAttack1()){
 				if(attackCollision(player2.getAttack1Polygon())){
 					player1.setCurrentHealth(player1.getCurrentHealth() -10);
 					player1.setHasBeenAttacked(true);
 					if(player1.getCurrentHealth() <= 0){
 						player1.setAlive(false);
 					}
 				}
 			}
 			if(player2.getAnimation().getFrame() == player2.getFrameOfAttack1()){  //If the animation has movement before attack
 				if(player2.getFacingDirection() == 1){
 					player2.setAttack1PolyX(player2.getX() + player2.getPolygon().getWidth());
 				}
 				else{
 					player1.setAttack1PolyX(player2.getX() - player2.getAttack1Polygon().getWidth());
 				}
 			}
 		}
 		if(player2.isAttacking1()){ //Reset polygon after attacking
 			if(player2.getAnimation().getFrame() == player1.getAnimation().getFrameCount() - 1){
 				player2.setAnimationProtected(false);
 				player2.setAnimation(Player.IDLE);
 				player2.resetAttack1PolyPosition();
 				player2.setAttacking1(false);
 				player1.setHasBeenAttacked(false);
 			}
 		}
 		
 		if(input.isKeyDown(player2.getMoveRightKey())){
 			moveRight(player2);
 		}
 		if(input.isKeyDown(player2.getMoveLeftKey())){
 			moveLeft(player2);
 		}
 		// START JUMP FOR PLAYER 2
 		if(input.isKeyPressed(player2.getJumpKey()) && player2.isJumping() == false){ //you can jump as long as you're not airborne
 			player2.setIsJumping(true);
 			player2.setYVelocity(VERTICAL_STARTING_VELOCITY);
 		}
 		// STARTS THE JUMP LOOP
 		if(player2.isJumping()){
 			Jump(player2);
 		}
 		//---------------------------
 		
 		// CHECKS IF THE CHARACTER IS ON THE GROUND
 		player2.setPolyY(player2.getY() + 1); //check if player is only 1 pixel above ground
 		if(entityCollision(player2.getPolygon())){
 			player2.setIsJumping(false);
 			player2.setYVelocity(0);
 			player2.setY(collisionBlock.blockPoly.getMinY() - (player2.getPolygon().getHeight()+1));
 		} // IF NOT, ACCELERATE DOWNWARD
 		else if(player2.isJumping() == false){ 
 			player2.setYVelocity(player2.getYVelocity() + VERTICAL_GRAVITY*delta);
 			player2.setY(player2.getY() + player2.getYVelocity()*delta);
 		}
 	}
 	
 	public void moveRight(Player player) throws SlickException{
 		if(player.getAnimationName().equals("Right") == false && player.isAnimationProtected() == false){
 			player.setAnimation(Player.RIGHT);
 			player.getAnimation().restart();
 		}
 		
 		player.setX(player.getX() + HORISONTAL_VELOCITY*delta);
 		player.setAnimation(Player.RIGHT);
 		if(entityCollision(player.getPolygon())){
 			player.setX(player.getX() - HORISONTAL_VELOCITY*delta);
 		}
 	}
 	public void moveLeft(Player player) throws SlickException{
 		if(player.getAnimationName().equals("Left") == false && player.isAnimationProtected() == false){
 			player.setAnimation(Player.LEFT);
 			player.getAnimation().restart();
 		}
 		
 		player.setX(player.getX() - HORISONTAL_VELOCITY*delta);
 		if(entityCollision(player.getPolygon())){
 		player.setX(player.getX() + HORISONTAL_VELOCITY*delta);
 		}
 	}
 	public void Jump(Player player) throws SlickException{
 		if(player.getAnimationName().equals("Jump") == false && player.isAnimationProtected() == false){
 			player.setAnimation(Player.JUMP);
 			player.getAnimation().restart();
 		}
 		
 		player.setYVelocity(player.getYVelocity() - VERTICAL_GRAVITY*delta);
 		player.setY(player.getY() - player.getYVelocity()*delta);
 		if(entityCollision(player.getPolygon())){
 			if(player.getYVelocity() > 0){//if the player is rising
 				player.setY(collisionBlock.blockPoly.getMaxY());
 			}
 			else{
 				player.setY(collisionBlock.blockPoly.getMinY() - (player.getPolygon().getHeight()+1));
 				player.setIsJumping(false);
 			}
 			player.setYVelocity(0);
 		}
 	}
 	
 	public boolean entityCollision(Polygon object) throws SlickException{
 		for(int i = 0; i < map.getBlock().size(); i++){
 			Block entity = (Block)map.getBlock().get(i);
 			if(object.intersects(entity.blockPoly)){
 				collisionBlock = (Block)map.getBlock().get(i);
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean attackCollision(Polygon poly) throws SlickException{
 		if(poly == player1.getAttack1Polygon()){ //If poly is player 1s attack poly
 			if(poly.intersects(player2.getPolygon())){
 				if(player1.isAttacking1()){
 					return true;
 				}	
			}
 		else if((player1.getX() >= player2.getX()) && (player1.getAttack1Polygon().getX() <= player2.getX() + player2.getPolygon().getWidth()) && (player1.getAttack1Polygon().getX() - player1.getAttack1Polygon().getWidth() >= player2.getX())){ //check if player1 is to the right of player2
 				return true;
 			}
 		else if((player1.getX() <= player2.getX()) && (player1.getAttack1Polygon().getX() >= player2.getX()) && (player1.getAttack1Polygon().getX() <= player2.getX() + player2.getPolygon().getWidth())){ //check if player1 is to the right of player2
 				return true;
 			}
 		}
 		else if (poly == player1.getAttack1Polygon()){
 			if(poly.intersects(player1.getPolygon())){
 				if(player2.isAttacking1()){
 					return true;
				}	
 			}
 		}
 		return false;
 	}
 		
 	public int getID() {
 		return ID;
 	}
 
 }
