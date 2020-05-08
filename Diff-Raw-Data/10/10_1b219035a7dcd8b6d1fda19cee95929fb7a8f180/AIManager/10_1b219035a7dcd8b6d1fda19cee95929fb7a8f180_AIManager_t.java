 /**
  * Project : TowerDefense
  * By Aurélie Beauprez, Thomas Demenat, Keven Akyurek and Cecilia Lejeune
  * Copyright IMAC 2013 - All Rights Reserved
  *
  * File created on 10 mai 2013
  */
 package AI;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 import Dispatcher.AddTowerOrder;
 import Dispatcher.AddUnitOrder;
 import Dispatcher.ChangeOwnerOrder;
 import Dispatcher.DispatcherManager;
 import Dispatcher.Order;
 import GameEngine.AttackTower;
 import GameEngine.Base;
 import GameEngine.TerritoryMap;
 import GameEngine.Player.PlayerType;
 import GameEngine.SupportTower;
 import GameEngine.Tower;
 import GameEngine.TowerManager.TowerTypes;
 
 
 public class AIManager implements Runnable {
 
 	private DispatcherManager dispatcher;
 	
 	private ArrayList<Base> bases;
 	private ArrayList<Base> enemyBases;
 	private ArrayList<Tower> towers;
 	
 	private LinkedList<Order> orderQueue;
 	
 	private PlayerType aiType;
 	private int timeToSleep;
 	private int money;
 	private boolean running;
 	private int mapHeight;
 	private int mapWidth;
 
 	public AIManager(DispatcherManager dispatcher, PlayerType aiType) {
 		this.dispatcher = dispatcher;
 		this.timeToSleep = 2000;
 		
 		bases = new ArrayList<Base>();
 		enemyBases = new ArrayList<Base>();
 		towers = new ArrayList<Tower>();
 		
 		orderQueue = new LinkedList<Order>();
 		this.aiType = aiType;
 	}
 	
 	/**
 	 * Get the initial bases on screen
 	 * @param bases
 	 */
 	public void initiateGameView(ArrayList<Base> bases){
 		for (Base b:bases){
 			if (b.getPlayerType().equals(aiType)){
 				this.bases.add(b);
 			}
 			else{
 				enemyBases.add(b);
 			}
 		}
 		
 		if (!bases.isEmpty()){
 			mapHeight = bases.get(0).getTerritoryMap().getHeight();
 			mapWidth = bases.get(0).getTerritoryMap().getWidth();
 		}
 		
 	}
 
 	/**
 	 * Main loop of the thread
 	 */
 	@Override
 	public void run() {
 		running = true;
 		while(running){
 			try {
 				Thread.sleep(timeToSleep);
 			} catch (InterruptedException e) {
 				running = false;
 			}
 			refreshInfo();
			printInfo();
 			attackBehavior();
 			towerBehavior();
 		}
 	}
 
 	/**
 	 * Refresh the new infos emited by the GameManager
 	 */
 	private void refreshInfo(){
 		//Retrieve the current size of the queue
 		int size = orderQueue.size();
 		
 		//Execute and remove the "size" first orders of the queue
 		if(size>0){
 			for(int i = 0;i<size; i++){
 				//Retrieve and remove the head of the queue
 				Order o = orderQueue.poll();
 				
 				if(o instanceof AddTowerOrder){
 					//If a ai tower is correctly added in the GameEngine
 					if (((AddTowerOrder) o).getPlayerType()==aiType){
 						//We add it in the ai list
 						if(((AddTowerOrder) o).getTowerType()==TowerTypes.ATTACKTOWER){
 							towers.add(new AttackTower(((AddTowerOrder) o).getId(), ((AddTowerOrder) o).getPosition(), aiType));
 						}
 						
 						else if (((AddTowerOrder) o).getTowerType()==TowerTypes.SUPPORTTOWER){
 							towers.add(new SupportTower(((AddTowerOrder) o).getId(), ((AddTowerOrder) o).getPosition(), aiType));
 						}
 					}
 				}
 				
 				if (o instanceof ChangeOwnerOrder){
 					if (((ChangeOwnerOrder)o).getNewPlayerType()==aiType){
 						int index=0;
 						for (Base b:enemyBases){
 							if (b.getId()==o.getId()){
 								bases.add(enemyBases.remove(index));
 								break;
 							}
 							index++;
 						}
 					}
 					else{
 						int index=0;
 						for (Base b:bases){
 							if (b.getId()==o.getId()){
 								enemyBases.add(bases.remove(index));
 								break;
 							}
 							index++;
 						}
 						if (bases.isEmpty()) {
 							stop();
 							interrupt();
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Print the infos of the GameEngine the AI can access
 	 */
 	private void printInfo(){
 		if (!bases.isEmpty() || !enemyBases.isEmpty() || !towers.isEmpty()){
 			System.out.println("-----------------AI "+aiType+"--------------------");
 			if(!bases.isEmpty()){
 				for(Base b:bases){
 					System.out.println("AI : AIBase ID="+b.getId()+" Amount="+b.getAmount());
 				}
 			}
 			
 			if(!enemyBases.isEmpty()){
 				for(Base b:enemyBases){
 					System.out.println("AI : EnemyBase ID="+b.getId()+" Type="+b.getPlayerType()+" Amount="+b.getAmount());
 				}
 			}
 			
 			if(!towers.isEmpty()){
 				for(Tower t:towers){
 					TowerTypes type = null;
 					if (t instanceof AttackTower)
 						type = TowerTypes.ATTACKTOWER;
 					if (t instanceof SupportTower)
 						type = TowerTypes.SUPPORTTOWER;
 					System.out.println("AI : Tower ID="+t.getId()+" Position="+t.getPosition() + " Type="+type);
 				}
 			}
 			System.out.println("---------------------------------------------");
 		}
 	}
 	
 	/**
 	 * How the AI choose who to attack 
 	 */
 	private void attackBehavior(){
 		//Search which ai base have the most amount of units
 		if (!bases.isEmpty() && !enemyBases.isEmpty()){
 			Base baseAttacking = null;
 			int amountOfAiUnit=0;
 			for (Base b:bases){
 				if (b.getAmount()>amountOfAiUnit){
 					baseAttacking = b;
 					amountOfAiUnit = b.getAmount();
 				}
 			}
 			
 			//Search which enemy base is the closest to his
 			Base baseAttacked = null;
 			double distance=Double.MAX_VALUE;
 			for (Base b:enemyBases){
 				if (distanceBetween(baseAttacking,b)<distance){
 					baseAttacked = b;
 					distance = distanceBetween(baseAttacking,b);
 				}
 			}
 			
 			//Test if there are enough units to get it
 			if ((baseAttacking.getAmount()*0.7)>baseAttacked.getAmount()){
 				sendUnit(baseAttacking.getId(),baseAttacked.getId(),80);
 			}
 		}
 	}
 	
 	/**
 	 * Returns the distance between two bases
 	 * @param b1 - First base
 	 * @param b2 - Second base
 	 * @return distance between them in int
 	 */
 	private int distanceBetween(Base b1, Base b2){
 		return b1.getProximityMap().getPixel(b2.getPosition().x, b2.getPosition().y);
 	}
 	
 	/**
 	 * Stop the loop in the Thread
 	 * @see #run()
 	 */
 	public void stop(){
 		System.out.println("AI "+aiType+" IS NOW DEAD");
 		bases.clear();
 		towers.clear();
 		enemyBases.clear();
 		this.running = false;
 	}
 	
 	private void interrupt(){
 		dispatcher.interruptAiThread(aiType);
 	}
 	
 	/**
 	 * Add a new order in the LinkedList of the AIManager
 	 * @param order
 	 * @see Dispatcher#DispatcherManager
 	 */
 	public void addOrder(Order order){
 		orderQueue.add(order);
 	}
 	
 	/**
 	 * Send unit toward an enemy base
 	 * @param idBaseSrc - unit's starting point
 	 * @param idBaseDst - unit's destination
 	 * @param amount - troops sent
 	 */
 	private void sendUnit(int idBaseSrc, int idBaseDst, int amount){
 		//TODO Changer constructeur AddUnitOrder
 		dispatcher.addOrderToEngine(new AddUnitOrder(-1, idBaseSrc, idBaseDst, amount));	
 		System.out.println("Order send : attack from="+idBaseSrc+" to="+idBaseDst+" with "+amount+"% of his power");
 	}
 	
 	private void towerBehavior(){
 		
 		//TODO if MONEY > 200
 		if (towers.size()<3){	
 			LinkedList<Point> availablePositions = new LinkedList<Point>();
 			
 			//Get all the available positions for placing tower
 			for (int y = 32; y < mapHeight-32;y++){ 
 				for (int x = 32; x < mapWidth-32;x++){
 					Point p = new Point (x,y);
 					if (canPlaceTowerAt(p)) availablePositions.add(p);
 				}
 			}
 			
 			if (!availablePositions.isEmpty()){
 				
 				//Place the tower the nearest possible position to his main base
 				Point nearestPosition = null;
 				double prevDistance = Double.MAX_VALUE;
 				for (Point p:availablePositions){
 					 double distance = Math.sqrt(Math.pow((bases.get(0).getPosition().x - p.x), 2)+Math.pow((bases.get(0).getPosition().y - p.y), 2));
 					 if (prevDistance>distance){
 						 nearestPosition = p;
 						 prevDistance = distance;
 					 }
 				}
 				if (nearestPosition != null)
 				{	
 					if (Math.random()<0.5) placeTower(nearestPosition, TowerTypes.ATTACKTOWER);
 					else placeTower(nearestPosition, TowerTypes.SUPPORTTOWER);
 				}
 				else{
 					System.out.println("AI TOWER BEHAVIOR ERROR : NEAREST POSITION = NULL");
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Check if the AI can place a tower at a given position
 	 * @param position - position checked
 	 * @return true if it can, false otherwise
 	 */
 	private boolean canPlaceTowerAt(Point position){
 		
 		//Let half of the sprite height's and half of the sprite's width exceed the limits of territory
 		int spriteQuart = 15; //Size of sprite : 64x64
 		
 		int zoneId = getTerritoryMapValue(position.x,position.y);
 		
 		int supRightZoneId = getTerritoryMapValue(position.x+spriteQuart,position.y-spriteQuart);
 		int supLeftZoneId = getTerritoryMapValue(position.x-spriteQuart, position.y-spriteQuart);
 		int infRightZoneId = getTerritoryMapValue(position.x+spriteQuart, position.y+spriteQuart);
 		int infLeftZoneId = getTerritoryMapValue(position.x-spriteQuart, position.y+spriteQuart);
 		
 		if(supRightZoneId == zoneId && supLeftZoneId == zoneId && 
 			infLeftZoneId == zoneId && infRightZoneId == zoneId){
 			
 			//Compare the zone with the order of types in the playerTypes array 
 			//-1 : null, 0: plain, 1 to 5 : territories
 			if(zoneId >0 && zoneId<6){
 				if (towers.isEmpty())
 					return true;
 				else{
 					//Check if there is already a tower at this position
 					for (Tower t:towers){
						if (towersCollide(position,t.getPosition())){
							return false;
 						}
 					}
					return true;
 				}
 			}
 			else return false;
 		}
 		else return false;
 	}
 
 	/**
 	 * Get the pixel value of the AI territory maps at a given position
 	 * @param x - position x of the pixel
 	 * @param y - position y of the pixel
 	 * @return value of the pixel at position (x,y)
 	 */
 	private int getTerritoryMapValue(int x, int y){
 		for (Base b:bases){
 			if (b.getTerritoryMap().getPixel(x,y)!=-1){
 				return b.getTerritoryMap().getPixel(x,y);
 			}	
 		}
 		return -1;
 	}
 	
 	/**
 	 * Check if two towers t1 and t2 are colliding
 	 * @param t1 - position of tower t1
 	 * @param t2 - position of tower t2
 	 * @return true if they collide, false otherwise
 	 */
 	private boolean towersCollide(Point t1, Point t2){
 		int spriteHalf = 32;
 		
 		int maxgauche = Math.max(t1.x-spriteHalf, t2.x-spriteHalf);
 		int mindroit = Math.min(t1.x+spriteHalf,t2.x+spriteHalf);
 		int maxbas = Math.max(t1.y-spriteHalf, t2.y-spriteHalf);
 		int minhaut = Math.min(t1.y+spriteHalf, t2.y+spriteHalf);
 		
 		if (maxgauche<mindroit && maxbas<minhaut) return true;
 		else return false;
 	}
 	
 	/**
 	 * Create an AI Tower at a specific position
 	 * @param position - Tower's position
 	 * @param type - Tower's type
 	 */
 	private void placeTower(Point position, TowerTypes type){
 		dispatcher.addOrderToEngine(new AddTowerOrder(-1, aiType, position, type, -1));
 	}
 	
 	/**
 	 * Upgrate an AI Tower 
 	 * @param idTower - ID's tower
 	 */
 	private void upgradeTower(int idTower){
 		//dispatcher.addOrderToEngine(new ......)
 	}
 	
 	/**
 	 * Check if the Ai is still active
 	 * @return boolean
 	 */
 	public boolean isRunning(){
 		return running;
 	}
 	
 	public PlayerType getPlayerType(){
 		return aiType;
 	}
 }
