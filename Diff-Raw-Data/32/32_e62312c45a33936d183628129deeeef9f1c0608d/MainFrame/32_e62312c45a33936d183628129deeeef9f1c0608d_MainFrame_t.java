 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.awt.Dimension;
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 import java.util.*;
 
 
 public class MainFrame extends Core implements KeyListener, MouseMotionListener,MouseListener{
 	Graphics g;
 	BufferedImage easyMap = null;
 	private int mouseX;
 	private int mouseY;
 	Rectangle New, Load, Exit;
 	
 	public void init(){
 		super.init();
 		
 		setFocusTraversalKeysEnabled(false);
 		addMouseListener(this);
 		addMouseMotionListener(this);
 		addKeyListener(this);
 		
 		setResizable(false);
 		setSize(720,572);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setLayout(new BorderLayout());
 		turretPanel.setPreferredSize(new Dimension(100,100));
 		getContentPane().add(panel, BorderLayout.CENTER);
 		getContentPane().add(turretPanel, BorderLayout.EAST);
 		setVisible(true);
 		
 		New = new Rectangle(179, 251, 349, 44);
 		Load = new Rectangle(179, 304, 349, 44);
 		Exit = new Rectangle(179, 359, 349, 44);
 		
 		File f = new File("./resources/EasyMap.png");
 		try {
 			easyMap = ImageIO.read(f);
 		} catch (IOException e) {
 			// catch block
 			e.printStackTrace();
 		}
 		g = this.getGraphics();
 		Tower_Controler.g = this.getGraphics();
 		Monster_Controller.g = this.getGraphics();
 		
 	}
 	
 	public static void main(String[] args){
 		new MainFrame().run();
 	}
 
 	public void draw() {
 		g.setColor(Color.BLUE);
 		g.fillRect(0,0,getWidth(), getHeight());
 		g.drawImage(easyMap, 100,100,150,150,null);
 		g.setColor(Color.WHITE);
 		g.drawString("EASY MAP", 100, 125);
 		g.setFont(new Font("SERIF", 10, 30));
 		g.setColor(Color.RED);
 		g.drawString("TURRET DEFENSE", 200, 80);
 	}
 	
 	public void mouseClicked(MouseEvent e) {
 		Rectangle rect = new Rectangle(e.getX(), e.getY(), 1, 1);
 		
 		if(!isMenu()){
 			if(New.contains(rect)){
 				newGame(1);
 				exitMenu();
 				Tower_Controler.live = true;
                                 //monsters.start();
 				
 			}else if(Load.contains(rect)){
 //				loadGame();
 			}else if(Exit.contains(rect)){
 				exitGame();
 			}
 		}
 		else{
 			Tower_Controler.recieve_position(e.getX(),e.getY());
 		}
 	}
 	
 	private void newGame(int num){
 		Data.loadMap(num);
                 Monster_Map monster_map = new Monster_Map(num);;
                 //Hack to span monster for now
                 Monster_Controller.spawnMonster(0,220,g,100,50);
 	}
 	
 	private void loadGame(int num){
 		Save.load(Integer.toString(num)+".map");
                 Monster_Map monster_map = new Monster_Map(num);;
 	}
 	
 	private void exitGame(){
 		System.exit(0);
 	}
 	
 	public void drawPauseMenu(){
 		g.setColor(Color.BLUE);
 		g.fillRect(0, 0, getWidth(), getHeight());
 		
 	}
 
 	public void mouseEntered(MouseEvent e) {
 		
 	}
 
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void mousePressed(MouseEvent e) {
 	
 	}
 
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void mouseDragged(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void mouseMoved(MouseEvent e) {
 		mouseX = e.getX();
 		mouseY = e.getY();	
 	}
 
 	public void keyPressed(KeyEvent e) {
 		if(e.getKeyCode() ==  KeyEvent.VK_ESCAPE){
 			exitMenu();
 		}		
 	}
 
 	public void keyReleased(KeyEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void keyTyped(KeyEvent e) {		
 	}
 	
 	public void update(long timePassed){
	//Tower_Controler.update();
 	Monster_Controller.update();
 	}
 }
