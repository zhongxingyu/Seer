 package core;
 
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
 
 import characters.Goomba;
 import characters.Player;
 import characters.PlayerInterface;
 
 import map.DungeonBuilder;
 import map.DungeonCollision;
 import map.DungeonNavigator;
 import map.OverWorldCamera;
 import map.OverWorldMap;
 import menu.MenuMain;
 
 
 
 
 public class Board extends JPanel implements ActionListener, FileLink{
 	
 	private MenuMain menuMain;
 	
 	private Player player;
 	private PlayerInterface playerInterface;
 	private OverWorldMap overWorldMap;
 	private DungeonNavigator dungeonNavigator;
 	private DungeonBuilder dungeonBuilder;
 	private CollisionDetection collisionDetection;
 	private Goomba goomba;
 
 	private Timer repaintTimer;
 	static Graphics2D g2d;
 
 	public Board(MenuMain menuMain, Player player,PlayerInterface playerInterface,OverWorldMap overWorldMap,DungeonNavigator dungeonNavigator,DungeonBuilder dungeonBuilder,CollisionDetection collisionDetection,Goomba goomba){
 		this.menuMain = menuMain;
 		
 		this.player = player;
 		this.playerInterface = playerInterface;
 		this.overWorldMap = overWorldMap;
 		this.dungeonNavigator = dungeonNavigator;
 		this.dungeonBuilder = dungeonBuilder;
 		this.collisionDetection = collisionDetection;
 		this.goomba = goomba;
 
 		
 		setDoubleBuffered(true);
 		setFocusable(true);
 		setBackground(Color.DARK_GRAY);
 		
 		
 		//Listeners
 		this.addMouseListener(new MAdapter());
 		this.addKeyListener(new KAdapter());
 		
 		//Timer
 		repaintTimer = new Timer(5, this);
 		repaintTimer.start();
 		
 
 			
 		System.out.println("->Board");
 		start();
 	}
 	
 	public void paint(Graphics g) {
 		//System.out.println("paintBoard");
 		
 		super.paint(g);
 		g2d = (Graphics2D) g;
 		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		
 		
 		
 		if (GameManager.menu){
 			//paint MenuMain
 			menuMain.paintComponents(g2d);
 		}
 	
 		if(GameManager.gameOver){
 			//try{
 				player.setVisible(false);
 				//Thread.sleep(800);
 			//} catch (InterruptedException ie) { }
 			
 			GameManager.menu = true;
 			g2d.setColor(Color.red);
 			Font textFont = new Font("Arial", Font.BOLD, 75);  
 			g.setFont(textFont);  
 			g2d.drawString("Game Over",220,300);
 		}
 		
 		if(GameManager.win){
 			//try{
 				player.setVisible(false);
 				//Thread.sleep(800);
 			//} catch (InterruptedException ie) { }
 				
 			GameManager.menu = true;
 			g2d.setColor(Color.yellow);
 			Font textFont = new Font("Arial", Font.BOLD, 75);  
 			g.setFont(textFont);  
 			g2d.drawString("Win",330,330);
 		}
 		
 		
 		//TODO
 		if (GameManager.ingame && !GameManager.menu && !GameManager.gameOver && !GameManager.win){
		
 			if(OverWorldMap.overWorld)
 				overWorldMap.paintComponents(g2d);
 			if(dungeonNavigator.getDungeon())
 				dungeonBuilder.paintComponents(g2d);
 			
 			player.paintComponents(g2d);
 			playerInterface.paintComponents(g2d);
 			goomba.paintComponents(g2d);
 			//paint player interface
 			
 			
 			if(GameManager.paintBounds){
 			//if(false){
 				g2d.setColor(Color.red);
 				g2d.draw(player.getBoundN());g2d.draw(player.getBoundE());g2d.draw(player.getBoundW());
 				g2d.setColor(Color.cyan);
 				g2d.draw(player.getBoundDirection());
 				g2d.setColor(Color.green);
 				g2d.draw(player.getBoundS());
 		        g2d.setColor(Color.blue); //Map Bounds
 		        g2d.draw(CollisionDetection.BoundN);g2d.draw(CollisionDetection.BoundE);g2d.draw(CollisionDetection.BoundS);g2d.draw(CollisionDetection.BoundW);
 		        g2d.setColor(Color.yellow); //Attack Bounds
 		        player.setAttackBounds();
 		        g2d.draw(player.getAttackBound());
 		        
 		        g2d.setColor(Color.orange); //Navigation Bounds
 		        overWorldMap.setDungeonBounds();
 		        if(OverWorldMap.overWorld){
 		        	g2d.draw(overWorldMap.getDungeonBounds());
 		        }
 		        
 		        if(dungeonNavigator.getDungeon()){
 		        	g2d.draw(dungeonNavigator.getToExit());
 		        	g2d.draw(dungeonNavigator.getToNorth());g2d.draw(dungeonNavigator.getToEast());g2d.draw(dungeonNavigator.getToSouth());g2d.draw(dungeonNavigator.getToWest());
 		        	g2d.draw(dungeonNavigator.getToNorth2());g2d.draw(dungeonNavigator.getToEast2());g2d.draw(dungeonNavigator.getToSouth2());g2d.draw(dungeonNavigator.getToWest2());
 		        	
 		        	//wall1
 		        	int layer = 3;
 		        	for(int yTile = 0; yTile < 7; yTile++){
 			        	for(int xTile = 0; xTile < 9; xTile++){
 			        		g2d.draw(dungeonNavigator.getObjectN(layer, xTile, yTile));g2d.draw(dungeonNavigator.getObjectE(layer, xTile, yTile));g2d.draw(dungeonNavigator.getObjectS(layer, xTile, yTile));g2d.draw(dungeonNavigator.getObjectW(layer, xTile, yTile));
 			        	}
 			        }
 		        	layer = 2; //door
 		        	g2d.setColor(Color.white);
 		        	for(int yTile = 0; yTile < 7; yTile++){
 			        	for(int xTile = 0; xTile < 9; xTile++){
 			        		g2d.draw(dungeonNavigator.getObjectN(layer, xTile, yTile));g2d.draw(dungeonNavigator.getObjectE(layer, xTile, yTile));g2d.draw(dungeonNavigator.getObjectS(layer, xTile, yTile));g2d.draw(dungeonNavigator.getObjectW(layer, xTile, yTile));
 			        	}
 			        }
 		        	layer = 5; //floor1
 		        	g2d.setColor(Color.MAGENTA);
 		        	for(int yTile = 0; yTile < 7; yTile++){
 			        	for(int xTile = 0; xTile < 9; xTile++){
 			        		g2d.draw(dungeonNavigator.getObjectN(layer, xTile, yTile));g2d.draw(dungeonNavigator.getObjectE(layer, xTile, yTile));g2d.draw(dungeonNavigator.getObjectS(layer, xTile, yTile));g2d.draw(dungeonNavigator.getObjectW(layer, xTile, yTile));
 			        	}
 			        }
 		        	layer = 6; //floor2
 		        	g2d.setColor(Color.MAGENTA);
 		        	for(int yTile = 0; yTile < 7; yTile++){
 			        	for(int xTile = 0; xTile < 9; xTile++){
 			        		g2d.draw(dungeonNavigator.getObjectN(layer, xTile, yTile));g2d.draw(dungeonNavigator.getObjectE(layer, xTile, yTile));g2d.draw(dungeonNavigator.getObjectS(layer, xTile, yTile));g2d.draw(dungeonNavigator.getObjectW(layer, xTile, yTile));
 			        	}
 			        }
 		        	
 		        	 g2d.setColor(Color.pink); //EnemyBounds
 				        g2d.draw(goomba.getBoundMain());
 				        g2d.setColor(Color.green);
 				        g2d.draw(goomba.getBoundS());
 		        }
 
 			}
 			
 		}
 		
 		
 		
 		g.dispose();
 	}
 	
 	public void start(){
 	
 	}
 	
 	//Timer loop
 	public void actionPerformed (ActionEvent aE){
 		
 		//repaint(player.getX()-150, player.getY()-200,600,800);
 		repaint();
 		if (GameManager.repaintNow == true){
 			//System.out.println("repaintNow:" +GameManager.repaintNow);
 			GameManager.repaintNow = false;
 			repaint();
 			
 		}
 		
 	}
 	
 	public static Graphics2D getG2D(){
 		return g2d;
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
 	      public void mouseClicked(MouseEvent mE) {
 	    	  menuMain.mouseClicked(mE);
 	      } 
 	 }
 
 }
