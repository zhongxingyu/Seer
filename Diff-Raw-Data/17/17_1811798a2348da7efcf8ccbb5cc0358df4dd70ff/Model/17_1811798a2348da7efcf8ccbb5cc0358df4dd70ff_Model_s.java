 package warborn.model;
 
 import java.awt.Color;
 import java.awt.DisplayMode;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Observable;
 
 import warborn.main.ScreenManager;
 import warborn.main.TerritoryFactory;
 import warborn.map.*;
 
 public class Model extends Observable{
 	
 	private ArrayList<Player> players;
 	private Territory[] territories;
 	private IMap[] maps;
 	private Move move;
 	private Battle battle;
 	private int selectedMap = 0, currentPlayer = 0, selectedTerritory = -1;
 	private int state = 0, phase = 0;
 	private ScreenManager screen;
 	//Creates displayMode array over most common screen resolutions according to w3 statistics 2012
 	private DisplayMode[] displayModes = {
 			//new DisplayMode(1366, 768, 32, 0),
 			//new DisplayMode(1366, 768, 24, 0),
 			new DisplayMode(1024, 768, 32, 0),
 			new DisplayMode(1024, 768, 24, 0),
 			new DisplayMode(1280, 1024, 32, 0),
 			new DisplayMode(1280, 1024, 24, 0),
 			new DisplayMode(1280, 800, 32, 0),
 			new DisplayMode(1280, 720, 24, 0),
 			//and this should work on just about any computer
 			new DisplayMode(800, 600, 32, 0),
 			new DisplayMode(800, 600, 24, 0),
 			new DisplayMode(800, 600, 16, 0),
 	};
 	
 	public Model (){
 		this.players = new ArrayList<Player>();
 		addPlayer("Player 1", Color.BLUE);
 		addPlayer("Player 2", Color.RED);
 		this.territories = null;
 		initMaps();
 		screen = new ScreenManager();
 	}
 	
 
 	//Getters
 	
 	public Player[] getPlayers(){
 		return (Player[])players.toArray();
 	}
 	
 	public Territory[] getTerritories(){
 		return territories;
 	}
 	
 	public String[] getMapNames(){
 		String[] names = new String[maps.length];
 		for(int i = 0; i < maps.length; i++){
 			names[i] = maps[i].toString();
 		}
 		return names;
 	}
 	
 	public int getState(){
 		return state;
 	}
 	
 	public int getPhase(){
 		return phase;
 	}
 	
 	public IMap getMap(){
 		return maps[selectedMap];
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
 	
 	public DisplayMode[] getDisplayModes(){
 		return displayModes;
 	}
 	
 	public ScreenManager getScreenManager(){
 		return screen;
 	}
 	
 	//TODO implement
 	public int getWidth(){
		return 800;
 	}
 	
 	public int getHeight(){
		return 600;
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
 		battle.add(territories[id]);
 		move.add(territories[id]);
 		if(selectedTerritory != -1){
 			nextPhase();
 			selectedTerritory = -1;
 		}else{
 			selectedTerritory = id;
 		}
 	}
 	
 	//End Setters
 	
 	public void addPlayer(String name, Color colour){
 		players.add(new Player(name, players.size(), colour));
 		changed();
 	}
 	
 	public void nextState(){
 		this.state++;
 		if(this.state > 3){
 			this.state = 1;
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
 			territories = TerritoryFactory.getTerritories(getMap().toString());
 		} catch (IOException e) {
 			System.out.println("Selected Map does not exist!");
 		}
 		this.phase = 0;
 		nextState();
 	}
 	
 	public boolean attackCompatible(Territory t1, Territory t2){
 		if(!t1.hasConnection(t2) || t1.getOwner().getID() == t2.getOwner().getID()){
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean moveCompatible(Territory t1, Territory t2){
 		if(!t1.hasConnection(t2) || t1.getOwner().getID() != t2.getOwner().getID()){
 			return false;
 		}
 		return true;
 	}
 	
 	private void initMaps(){
 		selectedMap = 0;
 		maps = new IMap[1];
		maps[0] = new GothenburgMapView();
 	}
 	
 	
 	private void changed() {
 		hasChanged();
 		notifyObservers();
 	}
 
 }
