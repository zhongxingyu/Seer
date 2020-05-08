 package managers;
 
 import java.util.Random;
 
 import com.me.mygdxgame.Mule;
 
 import gameObjects.Map;
 import gameObjects.Player;
 import gameObjects.Resource;
 import gameObjects.Tile;
 import gameObjects.Buildings.Store;
 
 /**
  * This class will manage the game logic as well as contain the PlayerManager for the entire game.
  * @author antonio
  *
  */
 public class GameManager {
 	
 	public static enum Difficulty {
 		BEGINNER, STANDARD, TOURNAMENT 	
 	};
 	
 	public static final int[] FOOD_REQUIREMENTS = {3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5};
 	public static final int[] ROUND_BONUS = {50, 50, 50, 100, 100, 100, 100, 150, 150, 150, 150, 200};
 	
 	private final Difficulty DEFAULT_DIFFICULTY= Difficulty.STANDARD;
 	
 	private PlayerManager players;
 	
 	private Difficulty difficulty;
 	
 	private Map map;
 
 	private Random generator = new Random();
 	
 	private final int GUI_HEIGHT = 100;
 	
 	private final float MAP_PPU_X = Mule.WIDTH / 9;
 	private final float MAP_PPU_Y = (Mule.HEIGHT - GUI_HEIGHT) / 5;
 	
 	private int currentRound;
 	
 	private float currentPlayerTime;
 	private int roundTime = 50;
 	
 	private Mule game;
 	
 	private StoreInventory currentStoreInventory;
 	
 	public GameManager(Mule game){
 		super();
 		this.game = game;
 		players = new PlayerManager();
 		currentRound = 0;
 		currentPlayerTime = 0;
 		currentStoreInventory = new StoreInventory();
 	}
 	
 	public GameManager(Mule game, PlayerManager pm){
 		this(game);
 		players = pm;
 	}
 	
 	public GameManager(Mule game, PlayerManager pm, int roundNumber, Map map,String difficult){
 		this(game,pm);
 		this.map = map;
 		currentRound = roundNumber;
 		difficulty = DEFAULT_DIFFICULTY;
 	}
 	
 	public float getCurrentPlayerTime(){
 		return currentPlayerTime;
 	}
 	
 	public void incrementCurrentPlayerTime(float delta){
 		currentPlayerTime += delta;
 	}
 	
 	public int getRoundTime(){
 		return roundTime;
 	}
 	
 	public void setRound(int round){
 		currentRound = round;
 	}
 	
 	public void setRoundTime(){
 		int requirements = GameManager.FOOD_REQUIREMENTS[currentRound];
 		int time = 50;
 		int food = players.getCurrentPlayer().getNumberOfResource(Resource.RESOURCE_FOOD);
 		if(food == 0){
 			time = 5;
 		} else if(food < requirements){
 			time = 30;
 		}
 		roundTime = time;
 	}
 	
 	public int getNumberOfPlayers(){
 		return players.getNumberOfPlayers();
 	}
 	
 	public void setDifficulty(String s){
 		if(s.toUpperCase().equals("TOURNAMENT")){
 			difficulty = Difficulty.TOURNAMENT;
 		} else if (s.toUpperCase().equals(Difficulty.BEGINNER)){
 			difficulty = Difficulty.BEGINNER;
 		} else {
 			difficulty = DEFAULT_DIFFICULTY;
 		}
 	}
 	
 	public void setMap(String s){
 		if(s.toLowerCase().equals("random")){
 			map = new Map(true);
 		} else{
 			map = new Map(false);
 		}
 	}
 	
 	public void setPPU(){
 		map.setPPU(MAP_PPU_X, MAP_PPU_Y);
 	}
 	
 	public void setMap(Map m){
 		map = m;
 	}
 	
 	public Map getMap(){
 		return map;
 	}
 	
 	public String toString(){
 		String s = "";
 		s += "DIFFICULTY : " + difficulty + "\n";
 		s += "MAP : \n" + map.toString() + "\n";
 		s += players.toString();
 		return s;
 	}
 	
 	public boolean buyTile(int x, int y, Player p){
 		Tile t = map.getMouseClickedTile(x, y);
 		if(t == null){
 			return false;
 		}
 		if(t.getTileType() == 2){
 			return false; // can't buy town tile
 		}
 		if(t.isOwned()){
 			return false;
 		}
 		t.setOwner(p);
 		return true;
 	}
 	
 	/**
 	 * This method buys a tile. (NOTE NOT A FREE TILE)
 	 * @param x mouse click x pos
 	 * @param y mouse click y pos
 	 * @param p the player buying
 	 * @param subtractMoney whether the player needs the money for it
 	 * @return
 	 */
 	public boolean buyTile(int x, int y, Player p, boolean subtractMoney){
 		Tile t = map.getMouseClickedTile(x, y);
 		if(t == null){
 			return false;
 		}
 		if(t.getTileType() == 2){
 			return false; // can't buy town tile
 		}
 		if(t.isOwned()){
 			return false;
 		}
 		if( !p.canBuy(t)){
 			return false;
 		}
 		t.setOwner(p, subtractMoney);
 		return true;
 	}
 	
 	public Player getPlayer(int i){
 		return players.getPlayer(i);
 	}
 	
 	/**
 	 * This ends the current player's turn and therefore will give the player money and goes to the next player
 	 */
 	public void endTurnPub(){
 		int timeLeft = (int)(getRoundTime()-currentPlayerTime);
 		if (timeLeft <= 0) timeLeft = 1; //avoid error for generating random int with nonpositive value
 		int addScore = GameManager.ROUND_BONUS[currentRound] + generator.nextInt(timeLeft*4);
 		players.getCurrentPlayer().gainResources(Resource.RESOURCE_MONEY, addScore);	
 		nextPlayer();
 	}
 	
 	/**
 	 * This method will switch the current players turn and then call the method which will
 	 * restart the actual turn and reset various variables.
 	 */
 	public void nextPlayer(){
 		//int a = Mule.pm.getCurrentPlayerNumber();
 		//int b = Mule.pm.getNumberOfPlayers();
 		boolean roundOver = false;
 		if(Mule.pm.getCurrentPlayerNumber() == Mule.pm.getNumberOfPlayers() - 1){
 			/*
 			 * DO TURN END Round STUFF HERE. Such as random events + reordering players + auction + other stuffs
 			 */
 			roundOver = true;
 			System.out.println("ENDING TURN");
 			//AuctionManger am = new AuctionManager();
 			//Mule.swapScreen(AUCTIONSCREEN);
 			Mule.pm.updatePlayerOrder();
 			//startRandomEvent();
 			//Mule.swapScreen(SELECTTILESSCREEN);
 			game.setScreen(Mule.SELECTTILESSCREEN);
 
 			//production
 
 			for(int i=0;i<map.getXSize();i++){
 				for(int j=0;j<map.getYSize();j++){
 					map.produce(i,j);
 				}
 			}
 			eat();
 		}
 		currentPlayerTime = 0;
 		Mule.pm.nextPlayer();
 		setRoundTime();
 		if(!roundOver) randomEvent();
 	}
 
 	public void eat(){
 		for(int i=0;i<players.getNumberOfPlayers();i++){
 			Player pla=players.getPlayer(i);
 			if(pla.spendResources(1,FOOD_REQUIREMENTS[currentRound])==0)
 				pla.loseResources(1,pla.getNumberOfResource(1));
 		}
 	}
 	
 	public void randomEvent() {
 		Player currentPlayer = Mule.pm.getCurrentPlayer();
 		if(generator.nextFloat()<=0.27f){
 			int m = GameManager.ROUND_BONUS[currentRound]/2;
 			int event;
 			if(Mule.pm.isLosingPlayer(currentPlayer)){
 				event = generator.nextInt(4); //only select good events if losing player
 			}else{
 				event = generator.nextInt(7);
 			}
 			String message = "You're feeling lucky.";
 			switch(event) {
 				case 0: message = "YOU JUST RECEIVED A PACKAGE FROM THE GT ALUMNI CONTAINING 3 FOOD AND 2 ENERGY UNITS.";
 						currentPlayer.gainResources(Resource.RESOURCE_FOOD, 3);
 						currentPlayer.gainResources(Resource.RESOURCE_ENERGY, 2);
 						break;
 				case 1: message = "A WANDERING TECH STUDENT REPAID YOUR HOSPITALITY BY LEAVING TWO BARS OF ORE.";
 						currentPlayer.gainResources(Resource.RESOURCE_ORE, 2);
 						break;
 				case 2: message = "THE MUSEUM BOUGHT YOUR ANTIQUE PERSONAL COMPUTER FOR $" + 8*m + ".";
 						currentPlayer.incrementMoney(8*m);
 						break;
 				case 3: message = "YOU FOUND A DEAD MOOSE RAT AND SOLD THE HIDE FOR $" + 2*m + ".";
 						currentPlayer.incrementMoney(2*m);
 						break;
 				case 4: message = "FLYING CAT-BUGS ATE THE ROOF OFF YOUR HOUSE. REPAIRS COST $" + 4*m + ".";
 						currentPlayer.incrementMoney(-4*m);
 						break;
 				case 5: message = "MISCHIEVOUS UGA STUDENTS BROKE INTO YOUR STORAGE SHED AND STOLE HALF YOUR FOOD.";
 						currentPlayer.gainResources(Resource.RESOURCE_FOOD, (int)(-.5*currentPlayer.getNumberOfResource(Resource.RESOURCE_FOOD)));
 						break;
 				case 6: message = "YOUR SPACE GYPSY INLAWS MADE A MESS OF THE TOWN. IT COST YOU $" + 6*m + " TO CLEAN IT UP.";
 						currentPlayer.incrementMoney(-6*m);
 						break;
 			}
 			map.createDialog(message);
 		}
 	}
 
 	public String getDifficulty(){
 		if(difficulty.equals(Difficulty.STANDARD)){
 			return "STANDARD";
 		} else if(difficulty.equals(Difficulty.TOURNAMENT)){
 			return "TOURNAMENT";
 		}
 		return "STANDARD";
 	}
 	
 	public int getCurrentRound(){
 		return currentRound;
 	}
 	
 	public void setStoreInventory(StoreInventory s){
 		currentStoreInventory = s;
 	}
 	
 	public StoreInventory getStoreInventory(){
 		return currentStoreInventory;
 	}
 }
