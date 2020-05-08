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
 			//printInfo();
 			attackBehavior();
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
 							towers.add(new AttackTower(((AddTowerOrder) o).getId(), new Point(0,0), aiType));
 						}
 						
 						else if (((AddTowerOrder) o).getTowerType()==TowerTypes.SUPPORTTOWER){
 							towers.add(new SupportTower(((AddTowerOrder) o).getId(), new Point(0,0), aiType));
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
						if (bases.isEmpty()) stop();
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
 					System.out.println("AI : Tower ID="+t.getId());
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
 	
 	/**
 	 * Create an AI Tower at a specific position
 	 * @param position - Tower's position
 	 * @param type - Tower's type
 	 */
 	private void placeTower(Point position, TowerTypes type){
 		dispatcher.addOrderToEngine(new AddTowerOrder(-1, aiType, position, type));
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
