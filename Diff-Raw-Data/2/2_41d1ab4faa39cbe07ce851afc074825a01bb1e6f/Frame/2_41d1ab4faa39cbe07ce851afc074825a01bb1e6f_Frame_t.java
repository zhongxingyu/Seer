 package qp.main.gui.frame;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.util.ArrayList;
 import java.util.Random;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import qp.main.entity.Enemy;
 import qp.main.entity.Player;
 import qp.main.entity.Projectile;
 import qp.main.listeners.KListener;
 import qp.main.threads.PlayerIntersect;
 import qp.main.threads.PlayerMovement;
 import qp.main.threads.ProjectileLauncher;
 
 public class Frame extends JFrame{
 	static Frame FRAME;
 	public static short WIDTH = 800;
 	public static short HEIGHT = 600;
 	private static Image img;
 	private static Graphics dbimg;
 	static Random rand = new Random();
 	public static boolean lost = false;
 	static Thread PLAYERINTERSECT = new Thread(new PlayerIntersect());
 	static Thread PROLAUNCHER = new Thread(new ProjectileLauncher());
 	static Thread PLAYERMOVEMENT = new Thread(new PlayerMovement());
 	static JPanel panel = new JPanel();
 	String TITLE = "Woo! Random!";
 	public Frame(){
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		setSize(WIDTH,HEIGHT);
 		setLocationRelativeTo(null);
 		setTitle(TITLE);
 		setResizable(false);
 		setVisible(true);
 		addKeyListener(new KListener());
 	}
 	public static void init(){
 		new Player(50,50,50,50);
 		for(int i = 0; i < 10; i++){
 			new Enemy(rand.nextInt(WIDTH/2) + WIDTH/2, rand.nextInt(HEIGHT/2) + HEIGHT/2, 50 , 50);
 		}
 		FRAME = new Frame();
 		initThreads();
 		new Thread(){
 			public void run(){
 				try{
 					byte interval = 15; 
 					long lastDraw=0;
 					while(true){
 						while(System.currentTimeMillis()-lastDraw<interval)
 							   Thread.sleep(1);
 							 
 							 lastDraw=System.currentTimeMillis();
 							 FRAME.repaint();
 					}
 				}catch(Exception e){
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 	private static void clearEntities(){
 		Enemy.getEnemies().clear();
 		Projectile.getProjectiles().clear();
 	}
 	public static void restart(){
 		lost = false;
 		initThreads();
 		clearEntities();
 		new Player(50,50,50,50);
 		for(int i = 0; i < 10; i++){
 			new Enemy(rand.nextInt(WIDTH/2) + WIDTH/2, rand.nextInt(HEIGHT/2) + HEIGHT/2, 50 , 50);
 		}
 	}
 	private static void initThreads(){
 		try{
 		if(!PLAYERMOVEMENT.isAlive()){
 			PLAYERMOVEMENT.start();
 		}
 		if(!PLAYERINTERSECT.isAlive()){
 			PLAYERINTERSECT.start();
 		}
 		if(!PROLAUNCHER.isAlive()){
 			PROLAUNCHER.start();
 		}
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	public void paint(Graphics g){
 		img = createImage(WIDTH,HEIGHT);
 		dbimg = img.getGraphics();
 		paintComponent(dbimg);
		g.drawImage(img, 0, 0, this);
 	}
 	private void paintComponent(Graphics g){
 		if(!lost){
 			//Player
 			g.setColor(Color.GREEN);
 			g.fillRect((int)Player.x, (int)Player.y, Player.width, Player.height);
 			g.setColor(Color.BLACK);
 			g.drawRect((int)Player.x, (int)Player.y, Player.width, Player.height);
 			//Enemies
 			for(Enemy e: (ArrayList<Enemy>) Enemy.getEnemies().clone()){
 				g.setColor(Color.RED);
 				g.fillRect((int)e.x, (int)e.y, e.width, e.height);
 				g.setColor(Color.BLACK);
 				g.drawRect((int)e.x, (int)e.y, e.width, e.height);
 			}
 			//Projectiles
 			for(Projectile p: (ArrayList<Projectile>) Projectile.getProjectiles().clone()){
 				try{
 					if(p!=null){
 					g.setColor(Color.YELLOW);
 					g.fillRect((int)p.x, (int)p.y, p.width, p.height);
 					g.setColor(Color.BLACK);
 					g.drawRect((int)p.x, (int)p.y, p.width, p.height);
 					}
 				}catch(Exception e){
 					e.printStackTrace();
 				}
 			}
 		}else{
 			g.fillRect(0, 0, WIDTH, HEIGHT);
 			g.setColor(Color.WHITE);
 			g.drawString("LOSER...Press R to restart", 50, 50);
 		}
 	}
 }
