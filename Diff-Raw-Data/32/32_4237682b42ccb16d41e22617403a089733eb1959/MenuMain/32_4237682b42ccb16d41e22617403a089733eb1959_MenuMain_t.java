 package menu;
 /*===> MainMenu mit 3 Buttons: Start, Options, Quit
  *Ausgangspunkt frs Men ist Board.ingame = false; Board.menu = true;
  *
  *"Start" 				Setzt Board.ingame = true und Board.menu = false
  *"Options" 		 	Sound Volume, Music Volume, Difficulty, Save, Load, Back
  *					 	Diese mssen erstmal nur als Regler/Buttons ohne Funktion 
  *						implementiert werden, die folgende static Varibles setzen:
  *						Sound/Music als Schieberegler mit Zuweisung von int Werten
  *						Board.musicVolume/Board.soundVolume zwischen 0 und 100.
  *  
  *"Quit"				System.exit(0);
  * 
  */
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 
 import java.io.IOException;
 import javax.imageio.ImageIO;
 import javax.swing.JComponent;
 
 import map.DungeonNavigator;
 import map.OverWorldMap;
 
 import characters.Goomba;
 import characters.Player;
 
 import core.Board;
 import core.FileLink;
 import core.GameManager;
 import core.GridInterface;
 
 import java.awt.event.*; 
 
 
 
 public class MenuMain extends JComponent implements Runnable, FileLink, GridInterface{
 
 	private static Player player;
 	private static Goomba goomba;
 	
 	Graphics g2d;
 	BufferedImage menuMain;
 	BufferedImage buttonMain;
 	Rectangle startButton = new Rectangle(C4.getX(),C4.getY(),B4.getX(),B4.getY());
 	Rectangle optionsButton = new Rectangle(D4.getX(),D4.getY(),B4.getX(),B4.getY());
 	Rectangle quitButton = new Rectangle(F4.getX(),F4.getY(),B4.getX(),B4.getY());
 	
 	Rectangle easyButton = new Rectangle(C2.getX(), C2.getY(), B4.getX(), B4.getY());
 	Rectangle hardButton = new Rectangle(C6.getX(), C6.getY(), B4.getX(), B4.getY());
 	boolean options = false;
 	
 	public MenuMain(){
 		System.err.println("->MainMenu");
 		
 		
 		try {
 			menuMain = ImageIO.read(menu);
 			buttonMain = ImageIO.read(button);
 			} catch (IOException e) {
 				System.err.println("map not found");
 				System.exit(0);
 			}
 
 	}
 	
 	public MenuMain(Player player, Goomba goomba){
 		this.player = player;
 		this.goomba = goomba;
 	}
 	
 	
 	public void paintComponents(Graphics g){
 		g2d = (Graphics2D) g;
 		
 		g2d.drawImage(menuMain, 0, 0, null);
 		
 		if(!GameManager.win && !GameManager.gameOver){
 			
 			if(!options){
 				g2d.drawImage(buttonMain, C4.getX(), C4.getY(), null);
 				if(!GameManager.ingame)
 					g2d.drawImage(buttonMain, D4.getX(), D4.getY(), null);
 				g2d.drawImage(buttonMain, F4.getX(), F4.getY(), null);
 			}
 			
 			if(options){
 				g2d.drawImage(buttonMain, C2.getX(), C2.getY(), null);
 				g2d.drawImage(buttonMain, C6.getX(), C6.getY(), null);
 				g2d.drawImage(buttonMain, F4.getX(), F4.getY(), null);
 			}
 		} 
 		
 		if((GameManager.win || GameManager.gameOver) && GameManager.ingame){
 			g2d.drawImage(buttonMain, F4.getX(), F4.getY(), null);
 		}
 		
 	}
 	
 	public void run(){
 		if (GameManager.printMsg)
 			System.out.println("MainMenu.run");
 	}
 	
 	
 	public void mouseClicked(MouseEvent mE){
 		int clickX = mE.getX();
 		int clickY = mE.getY();
 		
 		if(!GameManager.win && !GameManager.gameOver){
 			System.out.println("PaintMenu");
 			//MenuMain
 			if(GameManager.menu && !GameManager.ingame && !options){
 				
 				if(startButton.contains(clickX, clickY)){
 					System.out.println("clickStart");
 					GameManager.ingame = true;
 					GameManager.menu = false;
 										
 				}
 				
 				if(optionsButton.contains(clickX, clickY)){
 					System.out.println("clickOptions");
 					options = true;
 				}
 				
 				if(quitButton.contains(clickX, clickY)){
 					System.out.println("clickExit");
 					System.exit(0);
 				}
 			}//MenuMain
 			
 			//MenuIngame
 			if(GameManager.menu && GameManager.ingame && !options){
 				
 				if(startButton.contains(clickX, clickY)){
 					System.out.println("Back to Game");
 					GameManager.ingame = true;
 					GameManager.menu = false;
 				}
 				
 				
 				if(quitButton.contains(clickX, clickY)){
 					System.out.println("clickExit");
 					System.exit(0);
 				}
 			}//MenuIngame
 			
 			//Options
 			if(GameManager.menu && options){
 				if(easyButton.contains(clickX, clickY)){
 					System.out.println("Difficulty: Easy");
 					player.setLife(5);
 					DungeonNavigator.setGoombaLife(2);
 					goomba.setSpeed(1);
 					
 				}
 				
 				if(hardButton.contains(clickX, clickY)){
 					System.out.println("Difficulty: Hard");
 					player.setLife(2);
 					DungeonNavigator.setGoombaLife(4);
 					goomba.setSpeed(2);
 				}
 				
 				if(quitButton.contains(clickX, clickY)){
 					System.out.println("Back to Menu");
 					options = false;
 				}
 			}
 		}
 		
 		if((GameManager.win || GameManager.gameOver) && GameManager.ingame){
 			
 			if(quitButton.contains(clickX, clickY)){
 				
 				System.out.println("Back MainMenu");
 				
 				GameManager.win = false;
 				GameManager.gameOver = false;
 				GameManager.ingame = false;
 				GameManager.menu = true;
 				player.setLife(3);
 				GameManager dummy = new GameManager();
 				GameManager.resetGame();
 			}
 		}
 	}
 	
 
 	
 	public BufferedImage getImage(){
 		return menuMain;
 	}
 }
