 package warborn.model;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Random;
 
 import warborn.constants.MapData;
 import warborn.constants.PlayerData;
 import warborn.model.spells.SpellTargetable;
 
 public class Warborn extends Observable implements SpellTargetable{
 	
 	private static final int MAINMENU = -1, PREPARATION = 0, REINFORCEMENTPHASE = 1, BATTLEPHASE = 2, MOVEPHASE = 3;
	private static final String[] NAMES = {"Erez", "Metho", "Rioru", "Innura"};
 	private ArrayList<Player> players;
 	private Territory[] territories;
 	private Move move;
 	private Battle battle;
 	private int selectedMap = 0, currentPlayer = 0, selectedTerritory = -1, nbrOfReinforcements = 0;
 	private int state = -1, phase = 0, startPhases;
 	private Dimension dimension;
 	private SoulTomb deck;
 	private boolean spellLoaded;
 
 
 	public Warborn (){
 		this.deck = new SoulTomb();
 		this.players = new ArrayList<Player>();
 		
 		this.battle = new Battle(this);
 		this.move = new Move(this);
 		
 	}
 
 
 	//Getters
 
 	public Player[] getPlayers(){
 		return players.toArray(new Player[players.size()]);
 	}
 	
 	public Player getPlayer(int index){
 		return players.get(index);
 	}
 	
 	public int getNumberOfPlayers(){
 		return players.size();
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
 	
 	public Territory getSelectedTerritory(){
 		return territories[getSelectedTerritoryIndex()];
 	}
 	
 	public int getNbrOfReinforcements(){
 		return nbrOfReinforcements;
 	}
 	
 	public boolean isSpellLoaded(){
 		return spellLoaded;
 	}
 	
 	
 	//Setters:
 	
 	public void setNbrOfReinforcements(int nbrOfReinforcements){
 		this.nbrOfReinforcements = nbrOfReinforcements;
 	}
 
 	public void setDimensions(int width, int height){
 		setDimensions(new Dimension(width, height));
 	}
 	
 	public void setDimensions(Dimension dim){
 		dimension = dim;
 	}
 	
 	public void setPlayers(String[] names, Color[] colors, int[] race, int[] background){
 		players.clear();
 		for(int i = 0; i < names.length; i++){
 			players.add(new Player(names[i], i, colors[i], race[i], background[i]));
 		}
 		changed();
 	}
 
 
 	public void setSelectedMap(int id){
 		this.selectedMap = id;
 		changed();
 	}
 
 	public void setSelectedTerritory(int id){
 		
 		if (selectedTerritory != id){
 			if(spellLoaded){
 				selectedTerritory = id;
 			}else if(state==REINFORCEMENTPHASE && players.get(currentPlayer) == territories[id].getOwner() && nbrOfReinforcements > 0){
 				territories[id].incrementUnit();
 				nbrOfReinforcements--;
 			}
 			else if(state == PREPARATION && players.get(currentPlayer) == territories[id].getOwner()){
 				territories[id].incrementUnit();
 				this.currentPlayer = (++this.currentPlayer)%players.size();
 				startPhases--;
 				if (startPhases<0){
 					nextState();
 				}
 			}else if(state == PREPARATION && players.get(currentPlayer) != territories[id].getOwner()){
 				;
 			}else if(state != REINFORCEMENTPHASE){
 				if(selectedTerritory == -1 && players.get(currentPlayer) != territories[id].getOwner()){
 					if (state != MOVEPHASE){
 						selectedTerritory = id;
 					}
 				}else if((selectedTerritory == -1 || territories[selectedTerritory].getOwner() != players.get(currentPlayer)) &&
 											players.get(currentPlayer) == territories[id].getOwner()){
 					battle.add(territories[id]);
 					move.add(territories[id]);
 					selectedTerritory = id;
 					
 				}else if(state == BATTLEPHASE && attackCompatible(territories[selectedTerritory], territories[id])){
 					battle.add(territories[id]);
 					selectedTerritory = -1;
 					nextPhase();
 					
 				}else if(state == MOVEPHASE && moveCompatible(territories[selectedTerritory], territories[id])){
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
 		changed(0);
 	}
 	
 	public void setSpellLoaded(boolean loaded){
 		spellLoaded = loaded;
 	}
 	
 	public void resetSelectedTerritory(){
 		selectedTerritory = -1;
 	}
 
 	//End Setters
 
 	public void addPlayer(String name, Color color, int race, int background){
 		players.add(new Player(name, players.size(), color, race, background));
 		changed();
 	}
 
 	public void nextState(){
 		this.state++;
 		if(this.state > MOVEPHASE){
 			this.state = 1;
 			for(Player player : players){
 				boolean alive = false;
 				for(Territory terry : territories){
 					if(terry.getOwner() == player){
 						alive = true;
 						break;
 					}
 				}
 				if(!alive){
 					player.defeated(true);
 				}
 			}
 			if(players.get(currentPlayer).hasConquered()){
 				players.get(currentPlayer).addCard(deck.drawCard());
 			}
 			this.currentPlayer = (++this.currentPlayer)%players.size();
 			for(int i = 0; i < players.size(); i++){
 				if(players.get(currentPlayer).isDefeated()){
 					this.currentPlayer = (++this.currentPlayer)%players.size();
 				}
 			}
 			players.get(currentPlayer).conquered(false);
 		}
 		if(this.state == REINFORCEMENTPHASE){
 			nbrOfReinforcements = Math.max(players.get(currentPlayer).getNbrOfTerritories()/3, 3);
 			for(Territory terry : territories){
 				if(terry.getOwner() == players.get(currentPlayer)){
 					terry.setProtected(false);
 				}
 			}
 		}else if(this.state == PREPARATION){
 			startPhases--;
 		}
 		changed(1);
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
 		if(this.state != MAINMENU){
 			return;
 		}
 		
 		try {
 			String[] mapName = MapData.getMapNames();
 			territories = TerritoryFactory.getTerritories(mapName[selectedMap]);
 		} catch (IOException e) {
 			e.printStackTrace();
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
 				player = (++player)%players.size();
 			}
 		}
 		this.startPhases = 10*this.getNumberOfPlayers();
 		this.phase = 0;
 		nextState();
 	}
 	
 	public void quickStart(){
 		//Going to make random players instead
		addPlayer(NAMES[0], new Color(0, 0, 200), (int)(Math.random()*PlayerData.getNumberOfRaces()), (int)(Math.random()*PlayerData.getNumberOfGods()));
		addPlayer(NAMES[1], Color.RED, (int)(Math.random()*PlayerData.getNumberOfRaces()), (int)(Math.random()*PlayerData.getNumberOfGods()));
 		startGame();
 	}
 
 	public boolean attackCompatible(Territory t1, Territory t2){
 		if(!t1.hasConnection(t2) || t1.getOwner().getID() == t2.getOwner().getID() || t1.getNbrOfUnits() < 2 || t2.isProtected()){
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
 	
 	public void exchangeSouls(){
 		int[] nbrOfCards = new int[SoulTomb.getMaxValue()];
 		for (int i=0; i<players.get(currentPlayer).getNumberOfCards(); i++){
 			nbrOfCards[players.get(currentPlayer).getCards()[i].getIndex()]++;
 		}
 		if (nbrOfCards[0]!=0 && nbrOfCards[1]!=0 && nbrOfCards[2]!=0){
 			players.get(currentPlayer).changeMana(10);
 			players.get(currentPlayer).removeCard(0);
 			players.get(currentPlayer).removeCard(1);
 			players.get(currentPlayer).removeCard(2);
 		}else if (nbrOfCards[2]>2){
 			players.get(currentPlayer).changeMana(8);
 			for (int i=0; i<3; i++){
 				players.get(currentPlayer).removeCard(2);
 			}
 		}else if (nbrOfCards[1]>2){
 			players.get(currentPlayer).changeMana(6);
 			for (int i=0; i<3; i++){
 				players.get(currentPlayer).removeCard(1);
 			}
 		}else{
 			players.get(currentPlayer).changeMana(4);
 			for (int i=0; i<3; i++){
 				players.get(currentPlayer).removeCard(0);
 			}
 		}
 		this.changed();
 	}
 
 
 	public void changed() {
 		setChanged();
 		notifyObservers();
 	}
 	
 	protected void changed(int stateChange) {
 		setChanged();
 		notifyObservers(stateChange);
 	}
 
 }
