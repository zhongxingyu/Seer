 package model;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import model.action.*;
 import model.element.LightGrenade;
 import model.element.Player;
 import model.element.Wall;
 
 public class Game {
 	private Grid grid;
 	private List<Player> players;
 	private Player activePlayer;
 	private boolean isFinished;
 
 	public Game(int horLength, int verLength) {
 		isFinished = false;
 		players = new ArrayList<Player>();
 		players.add(new Player(new Position(0, horLength - 1)));
 		players.add(new Player(new Position(verLength - 1, 0)));		
 		setActivePlayer(players.get(0));
 		getActivePlayer().setRemainingTurnsToThree();
 		grid = new Grid(horLength, verLength);
 		initialiseGrid();
 	}
 	
 	private void initialiseGrid() {
 		placePlayers();
 		System.out.println("PLAYERS");
 		buildWalls();
 		System.out.println("WALLS");
 		placeItems();
 		System.out.println("ITEMS");
 	}
 	
 	public Grid getGrid() {
 		return grid;
 	}
 	
 	public List<Player> getPlayers() {
 		return players;
 	}
 	
 	public int getPlayerNumber(Player p) {
 		return players.indexOf(p);
 	}
 
 	private void placePlayers(){
 		for (Player p : players) {
 			grid.addElement(p);
 		}
 	}
 	
 	private void buildWalls() {
 		Random random = new Random();
 		
		int procentWalls = random.nextInt(21);
 		while(grid.getWallCoverage() < procentWalls && (procentWalls - grid.getWallCoverage()) > 2){
 			int directionInt = random.nextInt(2);
 			int randomLength = 1;
 			int randomX = 0;
 			int randomY = 0;
 			Direction dir;
 			if(directionInt == 0) {
 				randomLength = 2 + random.nextInt((grid.getHorLength() / 2) - 2);
 				int margin = grid.getHorLength() - randomLength;
 				randomX = random.nextInt(margin);
 				randomY = random.nextInt(grid.getVerLength());
 				dir = Direction.HORIZONTAL;
 			} else{
 				randomLength = 2 + random.nextInt((grid.getVerLength() / 2) - 2);
 				int margin = grid.getVerLength() - randomLength;
 				randomY = random.nextInt(margin);
 				randomX = random.nextInt(grid.getHorLength());
 				dir = Direction.VERTICAL;
 			}
 			
 			Wall newW = new Wall(new Position(randomX, randomY), randomLength, dir);
 			if(grid.canHaveAsWall(newW) && grid.getWallCoverage(randomLength) <= procentWalls){
 				grid.addElement(newW);
 			}
 		}
 	}
 	
 	
 	private void placeItems() {
 		placeLightGrenadeNearPlayer();
 		System.out.println("NEAR PLAYER");
 		placeLightGrenades();
 	}
 
 	private void placeLightGrenadeNearPlayer() {
 		for (Player p : players) {
 			boolean placed = false;
 			int minX = p.getPosition().getxCoordinate() - 2;
 			int maxX = p.getPosition().getxCoordinate() + 2;
 			int minY = p.getPosition().getyCoordinate() - 2;
 			int maxY = p.getPosition().getyCoordinate() + 2;
 			
 			if (minX < 0)
 				minX = 0;
 			if (minY < 0)
 				minY = 0;
 			
 			while (!placed) {
 				int randomX = minX + (int) Math.round(Math.random() * maxX);
 				int randomY = minY + (int) Math.round(Math.random() * maxY);
 				System.out.println("1." + minX + " - " + maxX);
 				System.out.println("2." + minY + " - " + maxY);
 				System.out.println("3." + randomX + " - " + randomY);
 				LightGrenade lg = new LightGrenade(new Position(randomX, randomY));
 				if (0 < randomX && randomX < 11 && 0 < randomY && randomY < 11 && grid.canHaveAsLightGrenade(lg)){			
 					grid.addElement(lg);
 					placed = true;
 				}
 			}
 		}
 		
 		
 	}
 
 	private void placeLightGrenades() {
 		while(grid.getLightGrenadeCoverage() < 5){
 			int randomX = (int) Math.round(Math.random() * grid.getHorLength());
 			int randomY = (int) Math.round(Math.random() * grid.getVerLength());
 			
 			LightGrenade lg = new LightGrenade(new Position(randomX, randomY));
 			if (grid.canHaveAsLightGrenade(lg)) {
 				grid.addElement(lg);
 			}
 		}
 	}
 
 	public Player getActivePlayer() {
 		return activePlayer;
 	}
 
 	public void setActivePlayer(Player activePlayer) {
 		this.activePlayer = activePlayer;
 	}
 	
 	public void activePlayerDoAction(Action actionToDo) throws InvalidActionException{
 		actionToDo.doAction(getActivePlayer(), getGrid());	
 		if(actionToDo instanceof EndTurnAction)
 			activePlayerEndTurn();
 		
 		if (getActivePlayer().getRemainingTurns() == 0)
 			activePlayerEndTurn();
 	}
 	
 	
 	public void activePlayerEndTurn(){
 		int lastIndexOfActivePlayer = getPlayerNumber(getActivePlayer());
 		lastIndexOfActivePlayer++;
 		if (lastIndexOfActivePlayer >= players.size()){
 			lastIndexOfActivePlayer = 0;
 		}	
 		setActivePlayer(players.get(lastIndexOfActivePlayer));
 		getActivePlayer().setRemainingTurnsToThree();
 	}
 
 	public boolean isFinished() {
 		return isFinished;
 	}
 
 	public void setFinished(boolean isFinished) {
 		this.isFinished = isFinished;
 	}
 	
 	
 }
