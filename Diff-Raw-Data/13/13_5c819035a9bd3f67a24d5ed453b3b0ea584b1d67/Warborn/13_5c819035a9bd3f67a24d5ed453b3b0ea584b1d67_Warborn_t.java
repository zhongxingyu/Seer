 package warborn.model;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Random;
 
 import warborn.SupportClasses.MapData;
 
 
 public class Warborn extends Observable{
 
 	private ArrayList<Player> players;
 	private Territory[] territories;
 	private Move move;
 	private Battle battle;
 	private int selectedMap = 0, currentPlayer = 0, selectedTerritory = -1, nbrOfReinforcements = 0;
 	private int state = 0, phase = 0;
 	private Dimension dimension;
 
 
 	public Warborn (){
 		Toolkit kit = Toolkit.getDefaultToolkit();
 		dimension = kit.getScreenSize();
 		this.players = new ArrayList<Player>();
 		addPlayer("Player 1", Color.CYAN);
 		addPlayer("Player 2", Color.YELLOW);
 		try {
 			this.territories = TerritoryFactory.getTerritories("Gothenburg");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		for(int i = 0; i<territories.length; i++){
 			territories[i].setNbrOfUnits(4);
 		}
 		this.battle = new Battle(this);
 		this.move = new Move(this);
 		
 	}
 
 
 	//Getters
 
 	public Player[] getPlayers(){
 		return (Player[])players.toArray();
 	}
 
 	public Territory[] getTerritories(){
 		return territories;
 	}
 	
 	public int getMapIndex(){
 		return selectedMap;
 	}
 
 	/**
 	 * Retruns the current state of the model
 	 * @return 0 - if currently the program state is in <code>Menu</code><br>
 	 * 1 - if currently the program state is in <code>Unit Placement</code><br>
 	 * 2 - if currently the program state is in <code>Attack</code><br>
 	 * 3 - if currently the program state is in <code>Move</code>
 	 */
 	public int getState(){
 		return state;
 	}
 
 	/**
 	 * Retruns the current phase of the model
 	 * @return 0 - if currently the program phase is <code>normal</code><br>
 	 * 1 - if currently the program state is <code>action</code>
 	 */
 	public int getPhase(){
 		return phase;
 	}
 
 
 	public Player getCurrentPlayer(){
 		return players.get(currentPlayer);
 	}
 
 	public int getSelectedTerritoryIndex(){
 		return selectedTerritory;
 	}
 
 	public Move getMove(){
 		return move;
 	}
 
 	public Battle getBattle(){
 		return battle;
 	}
 
 	public int getWidth(){
 		return dimension.width;
 	}
 
 	public int getHeight(){
 		return dimension.height;
 	}
 
 	public Territory getTerritory(int i){
 		return territories[i];
 
 	}
 	
 	public int getNbrOfReinforcements(){
 		return nbrOfReinforcements;
 	}
 
 
 	//Setters:
 
 	public void setPlayers(String[] names, Color[] colours){
 		for(int i = 0; i < names.length; i++){
 			players.add(new Player(names[i], i, colours[i]));
 		}
 		changed();
 	}
 
 
 	public void setSelectedMap(int id){
 		this.selectedMap = id;
 		changed();
 	}
 
 	public void setSelectedTerritory(int id){
 		System.out.println(selectedTerritory + ": " + id);
 		if (selectedTerritory != id){
 			
 			if(state == 1 && players.get(currentPlayer) == territories[id].getOwner()){
 				territories[id].incrementUnit();
 				nbrOfReinforcements--;
 				System.out.println(nbrOfReinforcements);
 				if(nbrOfReinforcements < 1){
 					this.phase = 0;
 					nextState();
 				}
 			}else if(state != 1){
 				if(selectedTerritory == -1 && players.get(currentPlayer) != territories[id].getOwner()){
 					selectedTerritory = id;
 					
 				}else if((selectedTerritory == -1 || territories[selectedTerritory].getOwner() != players.get(currentPlayer)) &&
 											players.get(currentPlayer) == territories[id].getOwner()){
 					battle.add(territories[id]);
 					move.add(territories[id]);
 					selectedTerritory = id;
 					
 				}else if(state == 2 && attackCompatible(territories[selectedTerritory], territories[id])){
 					battle.add(territories[id]);
 					selectedTerritory = -1;
 					nextPhase();
 					
 				}else if(state == 3 && moveCompatible(territories[selectedTerritory], territories[id])){
 					move.add(territories[id]);
 					selectedTerritory = -1;
 					nextPhase();
 				}
 			}
 		}else{
 			selectedTerritory = -1;
 			battle.resetTerritories();
 			move.resetTerritories();
 		}
 		changed();
 	}
 
 	//End Setters
 
 	public void addPlayer(String name, Color color){
 		players.add(new Player(name, players.size(), color));
 		changed();
 	}
 
 	public void nextState(){
 		this.state++;
 		if(this.state > 3){
 			this.state = 1;
			this.currentPlayer = (++this.currentPlayer)%players.size();
 		}
 		if(this.state == 1){
 			System.out.println(players.get(currentPlayer) + "");
 			nbrOfReinforcements = players.get(currentPlayer).getNbrOfTerritories()/3;
 		}
 		changed();
 	}
 
 	public void nextPhase(){
 		this.phase = (this.phase+1)%2;
 		changed();
 	}
 
 	public void removePlayer(){
 		removePlayer(players.size()-1);
 		changed();
 	}
 
 	public void removePlayer(int id){
 		players.remove(id);
 		changed();
 	}
 
 	public void startGame(){
 		if(this.state != 0){
 			return;
 		}
 		try {
 			String[] mapName = MapData.getMapNames();
 			territories = TerritoryFactory.getTerritories(mapName[selectedMap]);
 		} catch (IOException e) {
 			System.out.println("Selected Map does not exist!");
 		}
 		Random rand = new Random();
 		int sum = 0;
 		int player = 0;
 		while(sum < territories.length){
 			int i = rand.nextInt(territories.length);
 			if(territories[i].getOwner() == null){
 				players.get(player).addTerritory(territories[i]);
 				territories[i].setNbrOfUnits(1);
 				sum++;
				System.out.println(player + "; " + players.size());
				player = (++player)%players.size();
 			}
 		}
 		this.phase = 0;
 		nextState();
 	}
 
 	public boolean attackCompatible(Territory t1, Territory t2){
 		if(!t1.hasConnection(t2) || t1.getOwner().getID() == t2.getOwner().getID() || t1.getNbrOfUnits() < 2){
 			return false;
 		}
 		return true;
 	}
 
 	public boolean moveCompatible(Territory t1, Territory t2){
 		if(!t1.hasConnection(t2) || t1.getOwner().getID() != t2.getOwner().getID() || (t1.getNbrOfUnits() < 2 && t2.getNbrOfUnits() < 2)){
 			return false;
 		}
 		return true;
 	}
 
 
 	protected void changed() {
 		setChanged();
 		notifyObservers();
 	}
 
 }
