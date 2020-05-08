 import javax.imageio.ImageIO;
 import javax.swing.JPanel;
 
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import javax.swing.Timer;
 
 public class Board extends JPanel implements ActionListener, FileLink{
 	//instantiates classes
 	final static MenuIngame ingameMenu = new MenuIngame();
 	final static MenuMain mainMenu = new MenuMain();
 	final static OverWorldMap overWorldMap = new OverWorldMap();
 	final static Player player = new Player();
 	final static Enemy enemy = new Enemy(0,0);
 	final static Camera camera = new Camera();
 	final static DungeonNavigator dungeonNavigator = new DungeonNavigator();
 	final static CollisionDetection collisionDetection = new CollisionDetection();
 	final static DungeonBuilder dungeonBuilder = new DungeonBuilder();
 	final static PlayerInterface playerInterface = new PlayerInterface();
 	
 	
 	//threads
 	static int ingameThreadCounter = 5;
 	static int menuThreadCounter = 5;
 	final static ScheduledThreadPoolExecutor ingameScheduler = new ScheduledThreadPoolExecutor(ingameThreadCounter);
 	final static ScheduledThreadPoolExecutor menuScheduler = new ScheduledThreadPoolExecutor(menuThreadCounter);
 	final static Thread ingameMenuThread = new Thread(ingameMenu);
 	final static Thread mainMenuThread = new Thread(mainMenu);
 	final static Thread mapThread = new Thread(overWorldMap);
 	final static Thread dungeonBuilderThread = new Thread(dungeonBuilder);
 	final static Thread dungeonNavigatorThread = new Thread(dungeonNavigator);
 	final static Thread playerThread = new Thread(player);
 	final static Thread enemyThread = new Thread(enemy);
 	final static Thread playerInterfaceThread = new Thread(playerInterface);
 	
 	final static Thread cameraThread = new Thread(camera);
 	final static Thread collisionDetectionThread = new Thread(collisionDetection);
 	
 	//instance variables
 	static boolean repaintNow = false;
 	static boolean menuThread = false, ingameThread = false;
 	static int clickCount = 0;
 	
 	/*switch menu/ingame with M*/	static boolean ingame = true;
 									static boolean menu = false;
 									static boolean gameOver = false;
 									static boolean win = false;
 	
 	/*sound/music volume*/			static int musicVolume = 50;
 									static int soundVolume = 50;
 								
 	/*Debug tmpVaribles*/			static boolean paintBounds = false;
 									static boolean printMsg = false;
 	
 	private Timer repaintTimer;
 	private Graphics2D g2d;
 
 	public Board(){
 		
 		setDoubleBuffered(true);
 		setFocusable(true);
 		setBackground(Color.DARK_GRAY);
 		
 		
 		//Listeners
 		this.addMouseListener(new MAdapter());
 		this.addKeyListener(new KAdapter());
 		
 		//Timer
 		repaintTimer = new Timer(5, this);
 		repaintTimer.start();
 		
 		//initiate Threads
 		if (ingame){
 			ingameThread = true;
 			menuThread = false;
 		} else {
 			ingameThread = false;
 			menuThread = true;
 		}
 
 		//initiate overWorld/dungeon
 		OverWorldMap.overWorld = false;
 		DungeonNavigator.dungeon = true;
 		
 		//start point
 
 		Player.x = 230; Player.y = 500;
 		Player.lastDirection = 1;
 		//Player.absoluteX = Player.x = 405-20*3;
 		//Player.absoluteY = Player.y = 315-15*3;
 		//Camera.cameraX = Player.absoluteX;
 		//Camera.cameraY = Player.absoluteY;		
 		System.out.println("->Board");
 		start();
 	}
 	
 	public void paint(Graphics g) {
 
 		super.paint(g);
 		g2d = (Graphics2D) g;
 		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		
 	
 		
 		if (!ingame){
 			//paint MenuMain
 			mainMenu.paintComponents(g2d);
 		}
 		
 		if(ingame && menu){
 			ingameMenu.paintComponents(g2d);
 			
 		}
 		
 		if(gameOver){
 			ingame = false; menu = true;
 			g2d.setColor(Color.red);
 			Font textFont = new Font("Arial", Font.BOLD, 75);  
 			g.setFont(textFont);  
 			g2d.drawString("Game Over",220,300);
 		}
 		
 		if(win){
 			ingame = false; menu = true;
 			g2d.setColor(Color.yellow);
 			Font textFont = new Font("Arial", Font.BOLD, 75);  
 			g.setFont(textFont);  
 			g2d.drawString("Win",330,330);
 		}
 		
 		
 		
 		if (ingame && !menu && !gameOver && !win){
 			if(OverWorldMap.overWorld)
 				overWorldMap.paintComponents(g2d);
 			if(DungeonNavigator.dungeon)
 				dungeonBuilder.paintComponents(g2d);
 			
 			player.paintComponents(g2d);
 			playerInterface.paintComponents(g2d);
 			//paint player interface
 			
 			
 			
 			
 			if(paintBounds){
 				g2d.setColor(Color.red); //PlayerBounds
 		        g2d.drawRect(Player.x+10,Player.y+10,60,10);g2d.drawRect(Player.x+10,Player.y+90,60,10);g2d.drawRect(Player.x+10,Player.y+10,10,90);g2d.drawRect(Player.x+60,Player.y+10,10,90);
 		        g2d.setColor(Color.blue); //Map Bounds
 		        g2d.draw(OverWorldMap.BoundN);g2d.draw(OverWorldMap.BoundE);g2d.draw(OverWorldMap.BoundS);g2d.draw(OverWorldMap.BoundW);
 		        g2d.setColor(Color.yellow); //Attack Bounds
 		        Player.setAttackBounds();
 		        g2d.draw(Player.attackBound);
 		        g2d.setColor(Color.orange); //Navigation Bounds
 		        OverWorldMap.setBounds();
 		        if(OverWorldMap.overWorld){
 		        	g2d.draw(OverWorldMap.Over1Dungeon1);
 		        }
 		        if(DungeonNavigator.dungeon){
 		        	g2d.draw(DungeonNavigator.toExit);
 		        	g2d.draw(DungeonNavigator.toNorth);g2d.draw(DungeonNavigator.toEast);g2d.draw(DungeonNavigator.toSouth);g2d.draw(DungeonNavigator.toWest);
 		        	g2d.draw(DungeonNavigator.toNorth2);g2d.draw(DungeonNavigator.toEast2);g2d.draw(DungeonNavigator.toSouth2);g2d.draw(DungeonNavigator.toWest2);
 		        }
 		        
 		        for(int yTile = 0; yTile < 7; yTile++){
 		        	for(int xTile = 0; xTile < 9; xTile++){
 		        		g2d.draw(DungeonCollision.wallN[xTile][yTile]);g2d.draw(DungeonCollision.wallE[xTile][yTile]);g2d.draw(DungeonCollision.wallS[xTile][yTile]);g2d.draw(DungeonCollision.wallW[xTile][yTile]);
 		        		g2d.draw(DungeonCollision.wallNE[xTile][yTile]);g2d.draw(DungeonCollision.wallSE[xTile][yTile]);g2d.draw(DungeonCollision.wallSW[xTile][yTile]);g2d.draw(DungeonCollision.wallNW[xTile][yTile]);
 		        		
 		        	}
 		        }
 		        
 		        //DungeonCollison.readWalls();
 		        //g2d.draw(DungeonCollision.wall1[0][0]);
 			}
 			
 		}
 		
 		
 		
 		g.dispose();
 	}
 	
 	public void start(){
 	
 	}
 	//Timer loop
 	public void actionPerformed (ActionEvent aE){
 		start();
 		//repaint(Player.x-150, Player.y-200,600,800);
 		repaint();
 		if (repaintNow == true){
 			//System.out.println("repaintNow:" +repaintNow);
 			repaintNow = false;
 			repaint();	
 		}
 		
 		
 		if(ingameThread && ingame && !menu){
 			System.out.println("ingame Threads start:"+ingameThread);
 			menuThread = false;
 			ingameThread = false;
 			
 			ingameScheduler.scheduleWithFixedDelay(dungeonBuilderThread, 500, 50,TimeUnit.MILLISECONDS);
 			ingameScheduler.scheduleWithFixedDelay(mapThread, 50, 50,TimeUnit.MILLISECONDS);
 			ingameScheduler.scheduleWithFixedDelay(playerThread, 400, 10,TimeUnit.MILLISECONDS);
 			ingameScheduler.scheduleWithFixedDelay(cameraThread, 300, 5,TimeUnit.MILLISECONDS);
 			ingameScheduler.scheduleWithFixedDelay(collisionDetectionThread, 450, 10, TimeUnit.MILLISECONDS);
 			//ingameScheduler.scheduleWithFixedDelay(enemyThread,600,10,TimeUnit.MILLISECONDS);
 			ingameScheduler.scheduleWithFixedDelay(dungeonNavigatorThread, 600, 50, TimeUnit.MILLISECONDS);
 			ingameScheduler.scheduleWithFixedDelay(playerInterface, 600, 50, TimeUnit.MILLISECONDS);
 			
 			//shutdown menuThreads
 			if(!menuScheduler.isShutdown())
 				System.out.println("menu shuts down:"+!menuScheduler.isShutdown());
 				//menuScheduler.shutdownNow();
 		}
 		
 		
 		if(menuThread && menu){
 			System.out.println("menu Threads start:"+menuThread);
 			menuThread = false;
 			ingameThread = false;
 			
 			
 			if(ingame){
 				System.out.println("ingameMenu Threads start");
 				menuScheduler.scheduleWithFixedDelay(ingameMenuThread, 10, 10,TimeUnit.MILLISECONDS);
 			}
 				
 			if(!ingame){
 				System.out.println("MainMenu Threads start");
 				menuScheduler.scheduleWithFixedDelay(mainMenuThread, 10, 10,TimeUnit.MILLISECONDS);
 				
 			}			
 		}
 		
 		if(!menu && !ingame){
 			System.out.println("Game shutdown");
 			System.exit(0);
 		}
 		
 	}
 	
 	
 	private class KAdapter extends KeyAdapter {
 		public void keyReleased(KeyEvent kE){
 			player.keyReleased(kE);
 		}	
 		public void keyPressed(KeyEvent kE){
 			player.keyPressed(kE);
 		}
 	}
 	
 	private class MAdapter extends MouseAdapter{ 
 	      public void mouseClicked( MouseEvent e ) { 
 	    	  clickCount++;
 	    	  System.out.println(clickCount);
 	      } 
 	 }
 	
 	
 }
