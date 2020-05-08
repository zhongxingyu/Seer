 package states;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import entities.Camera;
 import entities.Player;
 import entities.Projectile;
 import entities.Tank;
 import entities.World;
 import game.GUI;
 import game.GunsAndHats;
 import game.PixelPos;
 
 public class GameState extends BasicGameState{
 
 	private int stateID;
 	private World world;
 	private Player[] players;
 	private ArrayList<Projectile> projectiles;
 	private int currentPlayer;
 	private long timeStarted;
 	private int roundsPlayed;
 	private int numberOfPlayers;
 	private int tanksPerPlayer;
 	private Camera camera;
 	private GUI gui;
 	private String winner; // Only non-empty if there is actually a winner
 	private boolean winnerChosen; // True if and only if there is a winner
 	private int currentTank; // Keeps track of how many tanks have been played - only used in keeping track of how many rounds have been played.
 	
 	
 	public GameState(int id, Camera camera){
 		stateID = id;
 		this.camera = camera;
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame game)
 			throws SlickException {
 //		numberOfPlayers = 2; // Placeholder for testing
 //		world = new World(0); // ID 0 - Test Level   ID 1 - Possible New Level
 //		players = new Player[numberOfPlayers];
 //		projectiles = new ArrayList<Projectile>();
 //		timeStarted = System.nanoTime();
 //		roundsPlayed = 0;
 //		winner = "";
 //		winnerChosen = false;
 //		
 ////		// Quick Fix - for testing.
 ////		players[0] = new Player("Player1", new Tank[] {new Tank(1,600,200),new Tank(1,500,200)});
 ////		players[1] = new Player("Player2", new Tank[] {new Tank(1,200,200),new Tank(1,100,200)});
 //		
 //		tanksPerPlayer = players[0].getTanks().length;
 //		currentPlayer = 0;
 //		currentTank = 1;
 //		players[currentPlayer].setFocus(this);
 //		
 //		gui = new GUI();
 //		gui.setCamera(camera);
 //		gui.setGameState(this);
 //		gui.setPlayers(players);
 //		gui.setWorld(world);
 //		
 //		camera.setFocus(world);
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame game, Graphics g) throws SlickException {
 		g.setColor(Color.white); // So that all text in the game is rendered in white
 				
 		// Render World, then Projectiles, then Players.
 		world.render(gc,game,g,camera);
 		for (int i = 0; i < projectiles.size(); i++) projectiles.get(i).render(gc,game,g,camera);
 		for (int i = 0; i < players.length; i++) players[i].render(gc,game,g,camera);
 		gui.render(gc, game, g);
 		
 		if (winnerChosen) displayWinner(winner, g);
 		
 		if (gc.isShowingFPS()) debugRender(g);
 	}
 	
 	public void startGame(World world, Player[] players){
 		this.world = world;
 		this.players = players;
 		
 		projectiles = new ArrayList<Projectile>();
 		timeStarted = System.nanoTime();
 		roundsPlayed = 0;
 		winner = "";
 		winnerChosen = false;
 		
 		tanksPerPlayer = players[0].getTanks().length;
 		currentPlayer = 0;
 		currentTank = 1;//shouldn't this be 0?
 		players[currentPlayer].setFocus(this);
 		
 		gui = new GUI();
 		gui.setCamera(camera);
 		gui.setGameState(this);
 		gui.setPlayers(players);
 		gui.setWorld(world);
 		
 		camera.setFocus(world);
 	}
 
 	private void addToHistory() {
 		// TODO Auto-generated method stub
 	}
 
 	private void displayWinner(String win, Graphics g) {
 		
 		g.drawString(win + " is the WINNER!", 300, 300);
 		g.drawString("Press ENTER to exit", 300, 315);
 	}
 
 	private void debugRender(Graphics g) {
 		g.drawString("Rounds Played: " + roundsPlayed, 10, 550);
 		g.drawString("Current Player: " + currentPlayer, 10, 565);	
 		g.drawString("Current Tank: " + players[currentPlayer].getCurrentTankNo(), 10,580);
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame game, int delta)
 			throws SlickException {
 		Input in = gc.getInput();
 		if (winner != "") winnerChosen = true; // Check to see if there is a winner yet
 		
 		if (!winnerChosen) { // If there is no winner yet..
 			// Update World, then Projectiles, then Players.
 			world.update(gc, game, delta);
 			for (int i = 0; i < projectiles.size(); i++) projectiles.get(i).update(gc, game, delta, world, this);
 			for (int i = 0; i < players.length; i++) players[i].update(gc, game, delta, world, this);
 			
 			if (players[currentPlayer].getCurrentTank().hasShot()){
 				if (projectiles.isEmpty()) {
 					players[currentPlayer].getCurrentTank().setHasShot(false);
 					nextPlayer();
 				}		
 			}
 			
 			// If any player has no tanks left alive, set them as a loser.
 			for (int i = 0; i < players.length; i++) {
 				if (players[i].getAliveTanks() == 0)
 					players[i].setLoser();
 			}
 
 			// If only one player isn't a loser, display them as the winner, then end the game.
 			boolean loserTest = false;
 			String winnerName = "";
 			for (int i = 0; i < players.length; i++) {
 				loserTest = loserTest || players[i].isLoser();
 				if (!players[i].isLoser())
 					winnerName = players[i].getPlayerName();
 			}
 			if (loserTest) {
 				winner = winnerName;
 			}
 		} else { // If there is a winner!
 			if (in.isKeyPressed(Input.KEY_ENTER)) {
 				addToHistory(); // Add the game to history.txt
 				game.getState(GunsAndHats.ENDGAMESCREEN).init(gc, game);
 				game.enterState(GunsAndHats.ENDGAMESCREEN);
 			}
 		}
 		
 		if(in.isKeyPressed(Input.KEY_P)) camera.setFocusScale(camera.getFocusScale()*1.1f);
 		if(in.isKeyPressed(Input.KEY_O)) camera.setFocusScale(camera.getFocusScale()*0.9f);
 		
 		camera.update(delta);
 		
 		// Debug Mode Toggle
 		if (in.isKeyPressed(Input.KEY_F12)) gc.setShowFPS(!gc.isShowingFPS());
 	}
 
 	public static boolean checkCollision(Tank tank, World world){
 //		HashSet<String> maskTank = getMask(tank.getPos(), tank.getImage());
 //		HashSet<String> maskWorld = world.getPixelMap();
 		HashSet<PixelPos> maskTank = getMask(tank.getPos(), tank.getImage());
 		HashSet<PixelPos> maskWorld = world.getPixelMap();
 		maskTank.retainAll(maskWorld); // Only keep those pixels that overlap.
 		if (maskTank.size() > 0) return true; // Collides
 		return false; // Doesn't Collide
 	}
 
 	public static boolean checkCollision(Projectile proj, World world){
 //		HashSet<String> maskProj = getMask(proj.getPos(), proj.getImage());
 //		HashSet<String> maskWorld = world.getPixelMap();
 		HashSet<PixelPos> maskProj = getMask(proj.getPos(), proj.getImage());
 		HashSet<PixelPos> maskWorld = world.getPixelMap();
 		maskProj.retainAll(maskWorld); // Only keep those pixels that overlap.
 		if (maskProj.size() > 0) return true; // Collides
 		return false; // Doesn't Collide
 	}
 	
 	public static boolean checkCollision(Projectile proj, Tank tank){
 		float tx1 = tank.getPos().getX();
 		float tx2 = tank.getPos().getX() + tank.getImage().getWidth();
 		float ty1 = tank.getPos().getY();
 		float ty2 = tank.getPos().getY() + tank.getImage().getHeight();
 		
 		float px = proj.getPos().getX();
 		float py = proj.getPos().getY();
 		
 		if (px > tx1 && px < tx2 && py > ty1 && py < ty2){
 //			HashSet<String> maskProj = getMask(proj.getPos(), proj.getImage());
 //			HashSet<String> maskTank = getMask(tank.getPos(), tank.getImage());
 			HashSet<PixelPos> maskProj = getMask(proj.getPos(), proj.getImage());
 			HashSet<PixelPos> maskTank = getMask(tank.getPos(), tank.getImage());
 			maskProj.retainAll(maskTank); // Only keep those pixels that overlap.
 			if (maskProj.size() > 0) return true; // Collides
 			return false; // Doesn't Collide
 		}
 		return false;
 	}
 
 	@Override
 	public int getID(){
 		return stateID ;
 	}
 
 	public void addProjectile(Projectile proj) {
 		projectiles.add(proj);
 	}
 	
 	public void destroyProjectile(Projectile proj){
 		projectiles.remove(proj);
 	}
 	
 	public static HashSet<PixelPos> getMask(Vector2f pos, Image img) {
 		HashSet<PixelPos> mask = new HashSet<PixelPos>();
 		
 		for(int i = 0; i < img.getWidth(); i++) {
 			for(int j = 0; j < img.getHeight(); j++) {
 				if(img.getColor(i, j).getAlpha() != 0) { //is non transparent
 					//mask.add((Math.floor(pos.getX())+i) + "," + (Math.round(pos.getY())+j));
 					mask.add(new PixelPos((int)Math.floor(pos.getX())+i, (int)Math.floor(pos.getY())+j));
 				}
 			}
 		}
 		
 		return mask;
 	}
 	
 	public void nextPlayer() {
 		players[currentPlayer].nextTank(); // Move to next tank on old player's team
 		players[currentPlayer].removeFocus(); // Remove focus from old player
 		
 		// Move to next player
 		if (currentPlayer + 1 == numberOfPlayers) {
 			currentPlayer = 0;
 			if (currentTank + 1 == (numberOfPlayers * tanksPerPlayer)){
 				currentTank = 1;
 				roundsPlayed++;
 				world.randomizeWind();
 			}
 		} else {
 			currentPlayer++; 
 			currentTank++;
 		}
 
 		players[currentPlayer].setFocus(this); // Give focus to the new player
 	}
 
 	public Player getCurrentPlayer() {
 		return players[currentPlayer];
 	}
 	
 	public Player[] getPlayers(){
 		return players;
 	}
 
 	public long getTimeStarted() {
 		return timeStarted;
 	}
 
 	public void setTimeStarted(long timeStarted) {
 		this.timeStarted = timeStarted;
 	}
 
 	public int getRoundsPlayed() {
 		return roundsPlayed;
 	}
 
 	public void setRoundsPlayed(int roundsPlayed) {
 		this.roundsPlayed = roundsPlayed;
 	}
 
 	public void damagePlayers(float blastRadius, Vector2f pos, int baseDamage) {
 		for (int i = 0; i < players.length; i++) players[i].damageTanks(blastRadius, pos, baseDamage);
 	}
 
 	public Player getPlayer(int i) {
 		return players[i];
 	}
 
 	public int getCurrentPlayerNo() {
 		return currentPlayer;
 	}
 	
 }
