 /**
  * Project : TowerDefense
  * By Aurélie Beauprez, Thomas Demenat, Keven Akyurek and Cecilia Lejeune
  * Copyright IMAC 2013 - All Rights Reserved
  *
  * File created on 24 avr. 2013
  */
 
 package Dispatcher;
 
 import java.util.ArrayList;
 
 import AI.AIManager;
 import GameEngine.*;
 import GameEngine.Player.PlayerType;
 import View.*;
 
 /**
  * Project - TowerDefense</br>
  * <b>Class - DispatcherManager</b></br>
  * <p>The DispatcherManager make the link between the ViewManager (GUI) and the GameManager (Engine)</p>
  * <b>Creation :</b> 24/04/2013</br>
  * @author K. Akyurek, A. Beauprez, T. Demenat, C. Lejeune - <b>IMAC</b></br>
  * @see TowerDefense.TowerDefense#main(String[]) 
  */
 public class DispatcherManager {
 	
 	private GameManager engine;
 	private ViewManager view;
 	private ArrayList<AIManager> aiEnemy;
 	private Thread threadEngine;
 	private Thread threadView;
 	private ArrayList<Thread> threadAI;
 	
 	/**
 	 * Constructor of the DisptacherManger class
 	 * @param engine - GameManager
 	 * @param view - ViewManager
 	 */
 	public DispatcherManager(GameManager engine,ViewManager view) {
 		super();
 		this.engine = engine;
 		this.view = view;
 		this.aiEnemy = new ArrayList<AIManager>();
 		this.threadAI = new ArrayList<Thread>();
 	}
 	
 	/**
 	 * Tell the engine that the game objects have to be created and initialized (according to the player choices).
 	 * @see View.ViewManager#play()
 	 */	
 	public void initiateGame(PlayerType humanType, int nbEnemies, ArrayList<PlayerType> enemiesType){
 		for (PlayerType pt:enemiesType){
 			aiEnemy.add(new AIManager(this,pt));
 		}
 		
 		engine.initiateGame(humanType, nbEnemies, enemiesType);	
 		
 	}
 	
 	/**
 	 * Tell the View that the SceneView components have to be created and initialized (according the game objects initialized by the engine). 
 	 * @param towerList - ArrayList of towers created by the engine
 	 * @param bases 
 	 * @see GameEngine.GameManager#initiateGame()
 	 */	
 	public void initiateGameView(ArrayList<Base> bases){
 		view.initiateGameView(bases);
 		for (AIManager ai:aiEnemy)
 			ai.initiateGameView(bases);
 	}
 
 	/**
 	 * Launch the game threads.
 	 * @see View.ViewManager#initiateGameInterface(ArrayList)
 	 */	
 	public void start(){
 		view.setRunning(true);
 		engine.setRunning(true);
 		threadView = new Thread(this.view);
 		threadEngine = new Thread(this.engine);
 		for (AIManager ai:aiEnemy) threadAI.add(new Thread(ai));
 		threadView.start();
 		threadEngine.start();
 		for (Thread t:threadAI) t.start();
 		//System.out.println("Dispatcher - Number of active threads : " + Thread.activeCount());
 	}
 	
 	/**
 	 * Stop the game threads.
 	 * @see View.ViewManager#mainMenu()
 	 */	
 	public void stop(){
 		view.setRunning(false);
 		engine.setRunning(false);
 		for (AIManager ai:aiEnemy) {
			if (ai.isRunning())
				ai.stop();
 		}
 		engine.endGame();
 		threadView.interrupt();
 		threadEngine.interrupt();
		for (Thread t:threadAI) {
			if (!t.interrupted())
				t.interrupt();
		}
		aiEnemy.clear();
		threadAI.clear();
 		//System.out.println("Dispatcher - Number of active threads : " + Thread.activeCount());
 	}
 	
 	/**
 	 * Add an Order to the engine ConcurrentLinkedQueue.
 	 * @param order - Order
 	 * @see  View.ViewManager#towerSuppressed(java.awt.Point, int)
 	 */
 	public void addOrderToEngine(Order order){
 		engine.addOrder(order);
 	}
 	
 	/**
 	 * Add an Order to the view ConcurrentLinkedQueue.
 	 * @param order - Order
 	 * @see GameEngine.GameManager#execute()
 	 */
 	public void addOrderToView(Order order){
 		view.addOrder(order);
 	}
 	
 	/**
 	 * Add an Order to all ai LinkedList.
 	 * @param order - Order
 	 */
 	public void addOrderToAI(Order order){
 		for (AIManager ai:aiEnemy){
 			if (ai.isRunning()) ai.addOrder(order);
 		}
 	}
 	
 	public void interruptAiThread(PlayerType aiType){
 		int index=0;
 		for (AIManager ai:aiEnemy){
 			if (aiType == ai.getPlayerType()){
 				aiEnemy.remove(index);
 				break;
 			}
 			index++;
 		}
 		threadAI.get(index).interrupt();
 		threadAI.remove(index);
 	}
 }
