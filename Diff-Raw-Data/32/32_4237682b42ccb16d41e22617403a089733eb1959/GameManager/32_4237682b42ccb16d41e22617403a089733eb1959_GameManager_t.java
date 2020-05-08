 package core;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import javax.swing.JFrame;
 
 import map.*;
 import menu.MenuMain;
 import characters.*;
 import javax.swing.Timer;
 
 public class GameManager extends JFrame implements Runnable, GameObjects, ActionListener{
 
 	ScheduledThreadPoolExecutor playerScheduler = new ScheduledThreadPoolExecutor(3);
 	ScheduledThreadPoolExecutor menuScheduler = new ScheduledThreadPoolExecutor(2);
 	ScheduledThreadPoolExecutor mapScheduler = new ScheduledThreadPoolExecutor(3);
 	ScheduledThreadPoolExecutor enemyScheduler = new ScheduledThreadPoolExecutor(3);
 	
 	Thread menuMainThread = new Thread(menuMain);
 	
 	Thread overWorldMapThread = new Thread(overWorldMap);
 	Thread dungeonCollisionThread = new Thread(dungeonCollision);
 	Thread dungeonNavigatorThread = new Thread(dungeonNavigator);
 	Thread playerThread = new Thread(player);
 	Thread playerInterfaceThread = new Thread(playerInterface);
 	Thread collisionDetectionThread = new Thread(collisionDetection);
 	Thread dynamicMapAnimationThread = new Thread(dynamicMapAnimation);
 	Thread goombaThread = new Thread(goomba);
 	
 	
 	Timer timer;
 	
 	public static boolean printMsg = false;
 	public static boolean win = false;
 	public static boolean gameOver = false;
 	
 	public static boolean repaintNow = false;
 	public static boolean paintBounds = false;
 	
	public static boolean menu = true;
	public static boolean ingame = false;
 	
 	public static int musicVolume = 50;
 	public static int soundVolume = 50;
 	
 	public static boolean switchGameState = true;
 	public static boolean changeMapModus = false;
 	
 	public GameManager(){
 		
 		add(GameObjects.board);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setSize (810, 653);
 		//setLocationRelativeTo(null);
 		setTitle("DC - Prototype");
 		setResizable(false);
 		setVisible(true);
 		
 		Timer timer = new Timer(10,this);
 		timer.start();
 
 		//initiate start dungeon
 		if(false){
 			dungeonNavigator.setAreaID(1);
 			dungeonNavigator.setX(0); dungeonNavigator.setY(3);
 			OverWorldMap.overWorld = false;
 			dungeonNavigator.setDungeon(true);
 			
 			player.setX(230); 
 			player.setY(500);
 			player.setLastDirection(1);
 		}
 	
 		
 		
 		//initiate start overWorld
 		if(true){
 			OverWorldMap.overWorld = true;
 			dungeonNavigator.setDungeon(false);
 			overWorldMap.setCameraX(0); overWorldMap.setCameraY(0);
 			player.setAbsoluteX(0);player.setAbsoluteY(0);
 			player.setX(110); player.setY(20);
 			
 		}
 		
 		
 		
 	}
 	
 	public void run(){
 		
 		//System.out.println("GameManager.run");
 		
 		if(switchGameState){
 			switchGameState = false;
 			
 			
 			playerScheduler.scheduleWithFixedDelay(playerThread, 400, 10,TimeUnit.MILLISECONDS);
 			playerScheduler.scheduleWithFixedDelay(playerInterface, 600, 10, TimeUnit.MILLISECONDS);
 			playerScheduler.scheduleWithFixedDelay(collisionDetectionThread, 450, 10, TimeUnit.MILLISECONDS);
 	
 			mapScheduler.scheduleWithFixedDelay(dungeonNavigatorThread, 30, 50, TimeUnit.MILLISECONDS);
 			mapScheduler.scheduleWithFixedDelay(dungeonCollision, 20, 50,TimeUnit.MILLISECONDS);
 			mapScheduler.scheduleWithFixedDelay(overWorldMapThread, 50, 50,TimeUnit.MILLISECONDS);
 			mapScheduler.scheduleWithFixedDelay(dynamicMapAnimationThread,100,50,TimeUnit.MILLISECONDS);
 				
 			enemyScheduler.scheduleWithFixedDelay(goombaThread,200,15,TimeUnit.MILLISECONDS);
 			menuScheduler.scheduleWithFixedDelay(menuMainThread,10,10,TimeUnit.MILLISECONDS);
 			
 			
 			/*TODO
 			
 			//menu
 			if(menu){
 				System.err.println("start menu");
 				
 				if(!playerScheduler.isShutdown() && !mapScheduler.isShutdown() && !enemyScheduler.isShutdown()){
 					playerScheduler.shutdown();
 					mapScheduler.shutdown();
 					enemyScheduler.shutdown();
 					
 					System.err.println("shutdown ingame");
 				}
 	
 				if(menuScheduler.isShutdown())
 					menuScheduler.scheduleWithFixedDelay(menuMainThread,10,20,TimeUnit.MILLISECONDS);	
 			}
 			
 			//ingame
 			if(ingame && !menu){
 				System.err.println("start ingame");
 				
 				if(!menuScheduler.isShutdown()){
 					menuScheduler.shutdown();
 				
 				playerScheduler.scheduleWithFixedDelay(playerThread, 400, 10,TimeUnit.MILLISECONDS);
 				playerScheduler.scheduleWithFixedDelay(playerInterface, 600, 10, TimeUnit.MILLISECONDS);
 				playerScheduler.scheduleWithFixedDelay(collisionDetectionThread, 450, 10, TimeUnit.MILLISECONDS);
 				
 					
 				mapScheduler.scheduleWithFixedDelay(dungeonNavigatorThread, 30, 50, TimeUnit.MILLISECONDS);
 				mapScheduler.scheduleWithFixedDelay(dungeonCollision, 20, 50,TimeUnit.MILLISECONDS);
 				mapScheduler.scheduleWithFixedDelay(overWorldMapThread, 50, 50,TimeUnit.MILLISECONDS);
 				mapScheduler.scheduleWithFixedDelay(dynamicMapAnimationThread,100,50,TimeUnit.MILLISECONDS);
 					
 				enemyScheduler.scheduleWithFixedDelay(goombaThread,200,15,TimeUnit.MILLISECONDS);
 				}
 			}
 		*/
 		}
 		
 	}
 	
 	/*
 	public void changeMapThreads(){
 		if(dungeonNavigator.dungeon){
 			mapScheduler.remove(overWorldMapThread);
 			
 			mapScheduler.scheduleWithFixedDelay(dungeonNavigatorThread, 50, 50, TimeUnit.MILLISECONDS);
 			mapScheduler.scheduleWithFixedDelay(dungeonBuilderThread, 20, 50,TimeUnit.MILLISECONDS);
 			
 		}
 		
 		if(overWorldMap.overWorld){
 			mapScheduler.remove(dungeonNavigatorThread);
 			mapScheduler.remove(dungeonBuilderThread);
 			
 			mapScheduler.scheduleWithFixedDelay(overWorldMapThread, 50, 50,TimeUnit.MILLISECONDS);
 		}
 	}
 	*/
 
 	
 	//TODO
 	public void actionPerformed(ActionEvent arg0) {
 		
 		//if(win || gameOver){
 			/*
 			playerScheduler.shutdown();
 			playerScheduler.shutdown();
 			playerScheduler.shutdown();
 			
 			mapScheduler.shutdown();
 			mapScheduler.shutdown();
 			mapScheduler.shutdown();
 			mapScheduler.shutdown();
 			
 			enemyScheduler.shutdown();
 			menuScheduler.shutdown();
 			*/
 		//}
 		
 	}
 	
 	public static void resetGame(){
 		OverWorldMap.overWorld = true;
 		dungeonNavigator.setDungeon(false);
 		dungeonNavigator.setX(0);
 		dungeonNavigator.setY(0);
 		dungeonNavigator.spawnEnemy();
 		overWorldMap.setCameraX(0); overWorldMap.setCameraY(0);
 		player.setAbsoluteX(0);player.setAbsoluteY(0);
 		player.setX(110); player.setY(20);
 		player.setVisible(true);
 		player.setMoveable(true);
 		player.setLife(3);
		player.setLastDirection(1);
 		
 	}
 
 }
 
 
 
