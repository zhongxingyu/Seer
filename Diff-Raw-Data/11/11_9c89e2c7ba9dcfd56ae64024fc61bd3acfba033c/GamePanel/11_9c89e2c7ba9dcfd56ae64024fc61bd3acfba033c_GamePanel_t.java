 package Client;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import javax.imageio.ImageIO;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 
 import Editor.MainPanel;
 import Net.Client;
 import Net.packets.Packet00Login;
 import Net.packets.Packet02Move;
 import Net.packets.Packet03Map;
 import Net.packets.Packet05EnemyMove;
 import Server.GUI;
 
 /********************************************
  * Das GamePanel ist main Klasse des Spiels *
  ********************************************/
 public class GamePanel extends JPanel implements Runnable, ActionListener, KeyListener {
 
 	private static final long serialVersionUID = 1L;
 	private JButton b0;
 	private JButton b1;
 	private JButton b2;
 	private JButton b3;
 	private JButton b4;
 
 	JFrame frame;
 	private KeyEvent e;
 
 	long delta = 0;
 	long last = 0;
 	long gameover = 0;
 	long mreg;
 	long l1 = System.currentTimeMillis() ;
 	long l2 = System.currentTimeMillis();
 
 	
 	Thread th;
 	JTextArea npc1 = new JTextArea(5,20);
 	JTextArea shop = new JTextArea(5,20);
 	Client c;
 	Player hero;
 	public PlayerMP player1;
 	private PlayerMP player2;
 	Enemy ene, sli;
 	NPC npc, shopowner;
 	EnemyBoss bs;
 	Tile ground;
 	Tile ps;
 	Tile ex;
 	Tile wlb;
 	Tile check;
 	Tile plate1;
 	Tile plate2;
 	Tile plate3;
 	Tile plate4;
 	Item sword;
 	Item item;
 	TileBlock krist;
 	TileBlock thun;
 	TileBlock ball;
 	TileBlock wl;
 	TileBlock wt;
 	MagicBolt mbb;
 	MagicBolt mb;
 	public Vector<Sprite> actors;
 	Vector<Sprite> painter;
 	Vector<Sprite> collision;
 	Vector<Sprite> painter3;
 	Vector<Enviroment> enviroment;
 	Vector<Enviroment> painter2;
 	public List<PlayerMP> connectedPlayers = new ArrayList<PlayerMP>();
 	public List<Enemy> spawnedEnemys = new ArrayList<Enemy>();
 	
 	private static final String gameov = "Game Over!";
 	private static String finish = "Goal!";
 
 	public int[][] leveldata;
 	int lvl = 1;
 	int savelvl;
 	int EnemyCounter = 0;
 	int Coins = 0;
 	int xx = 0, yy = 0;
 
 	SoundLib soundlib;
 	public WindowHandler windowHandler;
 
 	double x;
 	double y;
 	double phealth = 130;
 	double armor = 100;
 	double deltaX, deltaY;
 	double deltaXs, deltaYs;
 
 	boolean up;
 	boolean down;
 	boolean left;
 	boolean right;
 	boolean started;
 	boolean status = false;
 	boolean dead = false;
 	boolean finished = false;
 	boolean OoM = false;
 	boolean ServerRunning = false;
 	boolean join = false;
 	boolean boss = false;
 	boolean showtext = false;
 	boolean checkp = false;
 	boolean sup, sdown, sleft, sright;
 	boolean init = true;
 	boolean p1, p2, p3, p4;
 	boolean bRoom;
 	boolean reset = false;
 	boolean walkthr = true;
 
 	int dis, diss;
 	int moveX, moveY, moveX2, moveY2;
 	int moveEX, moveEY;
 	int dir = 0;
 	int oldX, oldY;
 	int posx, posy;
 	int savex, savey;
 	int mana = 100;
 	int page = 0;
 	int life = 3;
 	static int Tilesize = 16;
 	static int Width = 15 * Tilesize;
 	static int Height = 15 * Tilesize;
 
 	public String Username;
 
 	BufferedImage[] floor = loadPics("res/pics/floor.gif", 1);
 	BufferedImage[] wall = loadPics("res/pics/wall.gif", 1);
 	BufferedImage[] pstart = loadPics("res/pics/pstart.png", 1);
 	BufferedImage[] water = loadPics("res/pics/water.gif", 2);
 	BufferedImage[] exit = loadPics("res/pics/door.png", 4);
 	BufferedImage[] player = loadPics("res/pics/player.png", 12);
 	public static BufferedImage[] enemy = loadPics("res/pics/Enemy.png", 1);
 	BufferedImage[] np = loadPics("res/pics/npc.png", 1);
 	BufferedImage[] BS = loadPics("res/pics/boss2.png", 1);
 	BufferedImage[] Bolt = loadPics("res/pics/Bolt.png", 3);
 	BufferedImage[] IT = loadPics("res/pics/item.png", 1);
 	BufferedImage[] cp = loadPics("res/pics/checkpoint.png", 2);
 	public static BufferedImage[] sl = loadPics("res/pics/slime.png", 4);
 	BufferedImage[] sw = loadPics("res/pics/sword.png", 13);
 	BufferedImage[] kr = loadPics("res/pics/kristall.png", 6);
 	BufferedImage[] thu = loadPics("res/pics/blitze.png", 5);
 	BufferedImage[] bl = loadPics("res/pics/ball.png", 1);
 	public BufferedImage[] pl2 = loadPics("res/pics/player2.png", 12);
 
 
 	public static void main(String[] args){
 		new GamePanel(Width, Height + 20);
 	}
 
 	private GamePanel(int w, int h) {
 		this.setPreferredSize(new Dimension(w, h));
 		this.setBackground(Color.BLACK);
 
 		frame = new JFrame("Hero Quest");
 		frame.setLocation(960 - (Width / 2), 600 - (Height));
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		b0 = new JButton("Start");
 		b1 = new JButton("Close");
 		b2 = new JButton("Server");
 		b3 = new JButton("Join");
 		b4 = new JButton("Editor");
 		b0.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				b0.setVisible(false);
 				b1.setVisible(false);
 				b2.setVisible(false);
 				b3.setVisible(false);
 				b4.setVisible(false);
 				frame.pack();
 				frame.requestFocus();
 				try {
 					startGame();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 
 			}
 		});
 		b1.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				frame.dispose();
 			}
 		});
 		b2.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				new GUI(50, 50, GamePanel.this);
 			}
 		});
 		b3.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				join = true;
 				b0.setVisible(false);
 				b1.setVisible(false);
 				b2.setVisible(false);
 				b3.setVisible(false);
 				b4.setVisible(false);
 				frame.pack();
 				frame.requestFocus();
 				try {
 					startGame();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		
 		b4.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				MainPanel.main(null);
 			}
 		});
 		
 		frame.add(b0, BorderLayout.EAST);
 		frame.add(b1, BorderLayout.WEST);
 		frame.add(b2, BorderLayout.NORTH);
 		frame.add(b3, BorderLayout.SOUTH);
 		add(BorderLayout.CENTER, b4);
 		frame.add(this);
 		frame.pack();
 		frame.addKeyListener(this);
 		frame.setResizable(false);
 		frame.setVisible(true);
 		TextBox();
 		add(npc1);
 		add(shop);
 		TRun();
 
 	}
 
 	private void TextBox(){
 				
 		npc1.setLineWrap(true);
 		npc1.setWrapStyleWord(true);
 		npc1.setBackground(Color.black);
 		npc1.setForeground(Color.white);
 		npc1.setLocation(Width/2-40, Height/2);
 		npc1.setVisible(false);
 		npc1.setEnabled(false);
 		shop.setLineWrap(true);
 		shop.setWrapStyleWord(true);
 		shop.setBackground(Color.black);
 		shop.setForeground(Color.white);
 		shop.setLocation(Width/2, Height/2);
 		shop.setVisible(false);
 		shop.setEnabled(false);
 		
 	}
 	
 	private synchronized void TRun() {
 		th = new Thread(this);
 		th.start();
 	}
 
 	private void doInitializations() {
 		if(join){
 		windowHandler = new WindowHandler(this);
 		}
 		last = System.nanoTime();
 		gameover = 0;
 		actors = new Vector<Sprite>();
 		collision = new Vector<Sprite>();
 		enviroment = new Vector<Enviroment>();
 		painter = new Vector<Sprite>();
 		painter2 = new Vector<Enviroment>();
 		painter3 = new Vector<Sprite>();
 
 		soundlib = new SoundLib();
 		soundlib.loadSound("test", "res/sounds/Test.wav");
 		if (GUI.running || join) {
 			Packet03Map mapPacket = new Packet03Map();
 			try {
 				c.getOutput().writeObject(mapPacket);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		while(leveldata == null){
 			try {
 				Thread.sleep(10);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		ground();
 		SpawnPlayer();
 		if (GUI.running || join) {
 			Packet00Login loginPacket = new Packet00Login(player1.getUsername());
 			c.userName = player1.getUsername();
 			try {
 				c.getOutput().writeObject(loginPacket);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		if(!join){
 			SpawnEnemy();
 			SpawnNPC();
 		}
 		started = true;
 
 	}
 
 	/**************************************************
 	 * Game-Loop die alle wichtige Funktionen aufruft *
 	 **************************************************/
 	@Override
 	public void run() {
 		
 		while (frame.isVisible()) {
 
 			computeDelta();
 
 			if (isStarted()) {
 				checkKeys();
 				doLogic();
 				moveObjects();
 				cloneVectors();
 				if(join){
 					requestECoor();
 				}
 			}
 			repaint();
 
 			try {
 				Thread.sleep(75);
 			} catch (InterruptedException e) {
 			}
 
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void cloneVectors() {
 		painter = (Vector<Sprite>) actors.clone();
 		painter2 = (Vector<Enviroment>) enviroment.clone();
 		painter3 = (Vector<Sprite>) collision.clone();
 	}
 
 	private void moveObjects() {
 
 		for (ListIterator<Sprite> it = actors.listIterator(); it.hasNext();) {
 			Sprite r = it.next();
 			r.move(delta);
 		}
 
 	}
 
 	private synchronized void doLogic() {	
 	if(!join){
 		if(npc != null){	
 			deltaX =Math.abs((npc.getX()/16)-(hero.getX()/16));
 			deltaY =Math.abs(npc.getY()/16-hero.getY()/16);
 		}if(shopowner != null){
 			deltaXs =Math.abs((shopowner.getX()/16)-(hero.getX()/16));
 			deltaYs =Math.abs(shopowner.getY()/16-hero.getY()/16);
 		}
 	}else{
 		if(npc != null){	
 			deltaX =Math.abs((npc.getX()/16)-(player1.getX()/16));
 			deltaY =Math.abs(npc.getY()/16-player1.getY()/16);
 		}if(shopowner != null){
 			deltaXs =Math.abs((shopowner.getX()/16)-(player1.getX()/16));
 			deltaYs =Math.abs(shopowner.getY()/16-player1.getY()/16);
 		}
 	}
 		
 		dis =(int) Math.sqrt((deltaX*deltaX)+(deltaY*deltaY));
 		diss =(int) Math.sqrt((deltaXs*deltaXs)+(deltaYs*deltaYs));
 		
 		for (ListIterator<Sprite> it = actors.listIterator(); it.hasNext();) {
 			Sprite r = it.next();
 			r.doLogic(delta);
 			if (r.remove) {
 				it.remove();
 			}
 		}
 		for (ListIterator<Enviroment> ev = enviroment.listIterator(); ev
 				.hasNext();) {
 			Enviroment r = ev.next();
 			r.doLogic(delta);
 		}
 		for (ListIterator<Sprite> co = collision.listIterator(); co.hasNext();) {
 			Sprite r = co.next();
 			r.doLogic(delta);
 		}
 		if(!join){
 			hero.setFrame(moveX * Tilesize, moveY * Tilesize - Tilesize, Tilesize,
 					Tilesize);
 		}
 		if(!join){
 		sword.setFrame(hero.getX()+xx, hero.getY()+yy, Tilesize, Tilesize);
 		}else{
 			sword.setFrame(player1.getX()+xx, player1.getY()+yy, Tilesize, Tilesize);
 		}
 		if(boss && bs.locked){
 			if(bs.getX()+bs.getWidth()/2<hero.getX()&& bs.getX()+bs.getWidth()!= getWidth()-16){
 					bs.MoveXEr();
 			}
 			if(bs.getX()+bs.getWidth()/2> hero.getX()+hero.getWidth()){
 					bs.MoveXEl();
 			}
 			if(hero.intersects(bs.t)){	
 				if(System.currentTimeMillis()-l1 > 600){
 					createBoltBoss(bs.getX()+bs.getWidth()/2-5, bs.getY());
 					l1 = System.currentTimeMillis();
 				}		
 			}				
 		}	
 		
 		if (leveldata[moveY][moveX] == 9) {
 			lvl++;
 			read();
 			Clear(false);
 			ground();
 			SpawnPlayer();
 			SpawnEnemy();
 			SpawnNPC();
 			SpawnBoss();
 			SpawnKristallRoom();
 			p1=false;p2=false;p3=false;p4=false;
 		}
 		if (leveldata[moveY][moveX] == 8) {
 			lvl--;
 			read();
 			actors.clear();
 			enviroment.clear();
 			collision.clear();
 			ground();
 			SpawnPlayer();
 			moveX = savex;
 			moveY = savey;
 			SpawnNPC();
 			SpawnKristallRoom();
 			EnemyCounter = 0;
 			p1=true;p2=true;p3=true;p4=true;
 		}
 		if (leveldata[moveY][moveX] == 42) {
 			finished = true;
 			stopGame();
 
 		}
 		if(check != null){
 			if(check.getX()== hero.getX()&& check.getY()== hero.getY()+16){
 				check.setLoop(1,1);
 				checkp = true;
 				savelvl = lvl;
 			}
 		}
 
 		for (int i = 0; i < actors.size(); i++) {
 			for (int n = i + 1; n < actors.size(); n++) {
 				Sprite s1 = actors.elementAt(i);
 				Sprite s2 = actors.elementAt(n);
 
 				s1.collidedWith(s2);
 			}
 		}
 		for (int i = 0; i < actors.size(); i++) {
 			for (int n = i + 1; n < collision.size(); n++) {
 				Sprite s1 = actors.elementAt(i);
 				Sprite s2 = collision.elementAt(n);
 
 				s1.collidedWith(s2);
 			}
 		}
 		
 		if(mana > 0){
 			OoM = false;
 		}
 		if(!join){
 			if (hero.remove && gameover == 0) {
 				
 				gameover = System.currentTimeMillis();
 			}
 		}else{
 			if (player1.remove && gameover == 0) {
 					
 				gameover = System.currentTimeMillis();
 			}
 		}
 
 		if (gameover > 0) {
 			if (System.currentTimeMillis() - gameover > 3000) {
 				if(checkp || life !=0){
 					soundlib.stopLoopingSound();
 					try {
 						startGame();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}else
 					stopGame();
 			}
 		}
 		
 		if(EnemyCounter != 0 || init){
 			ex.setLoop(0,0);
 		}else{
 			ex.setLoop(1, 3);
 			ex.setLoop(3, 3);
 		}
 		if(checkp){
 			check.setLoop(1,1);
 		}
 		if(lvl == 5){
 			walkthr = false;
 			ex.setLoop(0,0);
 			if(!p1){
 			if(hero.getX()==plate1.getX()&& hero.getY()+16==plate1.getY()){
 				if(!p1&&!p2&&!p3&&!p4){
 				p1 = true;
 				}else{
 					p1 = false; p2 = false; p3 = false; p4 = false;
 					plate1.setLoop(0,0);plate2.setLoop(0,0);plate3.setLoop(0,0);plate4.setLoop(0,0);
 					thun.setLoop(0,0);
 				}
 			}}
 			if(!p2){
 			if(hero.getX()==plate2.getX()&& hero.getY()+16==plate2.getY()){
 				if(p1&& !p2 && !p3 && !p4){
 					p2 = true;
 				}else{
 					p1 = false; p2 = false; p3 = false; p4 = false;
 					plate1.setLoop(0,0);plate2.setLoop(0,0);plate3.setLoop(0,0);plate4.setLoop(0,0);
 					thun.setLoop(0,0);
 				}
 			}}
 			if(!p3){
 			if(hero.getX()==plate3.getX()&& hero.getY()+16==plate3.getY()){
 				if(p1 && p2 && !p3 && !p4){
 					p3 = true;
 				}else{
 					p1 = false; p2 = false; p3 = false; p4 = false;
 					plate1.setLoop(0,0);plate2.setLoop(0,0);plate3.setLoop(0,0);plate4.setLoop(0,0);
 					thun.setLoop(0,0);
 				}
 			}}
 			if(!p4){
 			if(hero.getX()==plate4.getX()&& hero.getY()+16==plate4.getY()){
 				if(p1 && p2 && p3 && !p4){
 					p4 = true;
 				}else{
 					p1 = false; p2 = false; p3 = false; p4 = false;
 					plate1.setLoop(0,0);plate2.setLoop(0,0);plate3.setLoop(0,0);plate4.setLoop(0,0);
 					thun.setLoop(0,0);
 				}
 			}}
 			if(p1&&p2&&p3&&p4){
 				EnemyCounter =0;
 				ex.setLoop(1, 3);
 				ex.setLoop(3, 3);
 				walkthr = true;
 			}else
 				walkthr = false;
 			if(p1){
 				plate1.setLoop(1, 1);
 				thun.setLoop(1,1);
 			}
 			if(p2){
 				plate2.setLoop(1,1);
 				thun.setLoop(2,2);
 			}
 			if(p3){
 				plate3.setLoop(1,1);
 				thun.setLoop(3,3);
 			}
 			if(p4){
 				plate4.setLoop(1,1);
 				thun.setLoop(4,4);
 			}
 		}
 	}
 	
	public synchronized void Clear(boolean MP){
 		if(!MP){
 			actors.clear();
 			enviroment.clear();
 			collision.clear();
 			painter.clear();
 			painter2.clear();
 			painter3.clear();
 		}else{
 			int index = 0;
 			for(Sprite e : actors){
 				if(e instanceof Enemy){
 					actors.remove(index);
 				}
 				index++;
 			}
 			index = 0;
 			for(Sprite e: painter){
 				if(e instanceof Enemy){
 					painter.remove(index);
 				}
 				index++;
 			}
 			enviroment.clear();
 			painter2.clear();
 			collision.clear();
 			painter3.clear();
 		}
 	}
 	
	public synchronized void Draw(){
 		ground();
 	}
 
 	private void ground() {
 		for (int row = 0; row < leveldata.length; row++) {
 			for (int col = 0; col < leveldata[row].length; col++) {
 				posy = row;
 				posx = col;
 				int rc = leveldata[row][col];
 				if (rc == 0 || rc == 13 || rc == 16) {
 					ground = new Tile(floor, posx * Tilesize, posy * Tilesize,
 							1, this);
 					enviroment.add(ground);
 				} else if (rc == 1 || rc == 14) {
 					wl = new TileBlock(wall, posx * Tilesize, posy * Tilesize,
 							0, this);
 					collision.add(wl);
 				} else if (rc == 11) {
 					wlb = new Tile(wall, posx * Tilesize, posy * Tilesize,
 							0, this);
 					enviroment.add(wlb);
 				} else if (rc == 12) {
 					check = new Tile(cp, posx * Tilesize, posy * Tilesize,
 							0, this);
 					enviroment.add(check);
 					check.setLoop(0, 0);
 				} else if (rc == 2 || rc == 22) {
 					ps = new Tile(pstart, posx * Tilesize, posy * Tilesize, 0,
 							this);
 					enviroment.add(ps);
 				} else if (rc == 3) {
 					ground = new Tile(floor, posx * Tilesize, posy * Tilesize,
 							1, this);
 					enviroment.add(ground);
 				} else if (rc == 4) {
 					wt = new TileBlock(water, posx * Tilesize, posy * Tilesize,
 							500, this);
 					collision.add(wt);
 				} else if (rc == 7) {
 					ground = new Tile(floor, posx * Tilesize, posy * Tilesize,
 							0, this);
 					enviroment.add(ground);
 					savex = col;
 					savey = row;
 				} else if (rc == 8 || rc == 82) {
 					ps = new Tile(pstart, posx * Tilesize, posy * Tilesize, 0,
 							this);
 					enviroment.add(ps);
 				} else if (rc == 9 || rc == 92) {
 					ex = new Tile(exit, posx * Tilesize, posy * Tilesize, 0,
 							this);
 					enviroment.add(ex);
 				} else if (rc == 30 || rc == 31 || rc == 32 || rc ==33) {
 					ground = new Tile(floor, posx * Tilesize, posy * Tilesize,
 							1, this);
 					enviroment.add(ground);
 				} else if (rc == 42) {
 					ex = new Tile(exit, posx * Tilesize, posy * Tilesize, 1,
 							this);
 					enviroment.add(ex);
 				}
 				if (rc == 2 && lvl > 1) {
 					ground = new Tile(floor, posx * Tilesize, posy * Tilesize,
 							1, this);
 					enviroment.add(ground);
 				}
 			}
 		}
 
 	}
 
 	private void SpawnEnemy() {
 		for (int row = 0; row < leveldata.length; row++) {
 			for (int col = 0; col < leveldata[row].length; col++) {
 				if (leveldata[row][col] == 3) {
 					moveEX = col;
 					moveEY = row;
 					int rn = (int)(Math.random()*5);
 					if(rn >= 0 && rn <2){
 						ene = new Enemy(enemy, Tilesize * moveEX,
 								Tilesize * moveEY, 100, this, "melee", "arcane");
 						actors.add(ene);
 						EnemyCounter++;
 					}else if(rn >=2 && rn<=4){
 						moveEX = col;
 						moveEY = row;
 						sli = new Enemy(sl, Tilesize * moveEX,
 								Tilesize * moveEY, 100, this, "poison", "melee");
 						actors.add(sli);
 						EnemyCounter++;
 					}
 				}
 			}
 		}
 	}
 
 	private void SpawnPlayer() {
 		for (int row = 0; row < leveldata.length; row++) {
 			for (int col = 0; col < leveldata[row].length; col++) {
 				if (leveldata[row][col] == 2) {
 					moveY = row;
 					moveX = col;
 					if(join){
 						player1 = new PlayerMP(player, Tilesize * moveX,
 								(moveY * Tilesize), 100, this);
 						actors.add(player1);
 						this.connectedPlayers.add(player1);
 						player1.setLoop(0, 0);
 					}else{
 						hero = new Player(player, Tilesize * moveX,
 								(moveY * Tilesize), 100, this);
 						actors.add(hero);
 						hero.setLoop(0, 0);
 					}		
 					if(!join){
 						sword = new Item(sw, hero.getX(), hero.getY(), 100, this, false, "melee");
 						actors.add(sword);
 						sword.setLoop(0, 0);
 					}else{
 						sword = new Item(sw, player1.getX(), player1.getY(), 100, this, false, "melee");
 						actors.add(sword);
 						sword.setLoop(0, 0);
 					}
 					if(lvl == 1){
 						if(!join){
 							hero.phealth = 130;
 							hero.mana= 100;
 							hero.armor = 100;
 						}
 					}else{
 						if(!join){
 							hero.phealth = this.phealth;
 							hero.mana = this.mana;
 							hero.armor = this.armor;
 						}
 					}
 				}
 				
 			}
 		}
 	}
 	
 	private void SpawnNPC() {
 		for (int row = 0; row < leveldata.length; row++) {
 			for (int col = 0; col < leveldata[row].length; col++) {
 				if (leveldata[row][col] == 30) {
 					moveEX = col;
 					moveEY = row;
 					npc = new NPC(np, Tilesize * moveEX, Tilesize * moveEY-Tilesize, 100, this);
 					actors.add(npc);
 
 				}else if(leveldata[row][col] == 31){
 					moveEX = col;
 					moveEY = row;
 					shopowner = new NPC(np, Tilesize * moveEX, Tilesize * moveEY-Tilesize, 100, this);
 					actors.add(shopowner);
 				}
 			}
 		}
 	}
 	
 	private void SpawnBoss() {
 		for (int row = 0; row < leveldata.length; row++) {
 			for (int col = 0; col < leveldata[row].length; col++) {
 				if (leveldata[row][col] == 33) {
 					moveEX = col;
 					moveEY = row;
 					bs = new EnemyBoss(BS, Tilesize * moveEX-Tilesize, Tilesize * moveEY-Tilesize, 100, this);
 					actors.add(bs);
 
 				}
 			}
 		}
 	}
 	
 	private void SpawnKristallRoom(){
 		for (int row = 0; row < leveldata.length; row++) {
 			for (int col = 0; col < leveldata[row].length; col++) {
 				int rc = leveldata[row][col];
 				if(rc == 13){
 					krist = new TileBlock(kr, Tilesize*col-Tilesize/2, Tilesize*row-3*Tilesize, 100, this);
 					collision.add(krist);
 					EnemyCounter++;
 				}else if(rc == 14){
 					thun = new TileBlock(thu, Tilesize*col, Tilesize*row-Tilesize, 100, this);
 					collision.add(thun);
 					thun.setLoop(0,0);
 				}else if(rc == 16){
 					ball = new TileBlock(bl, Tilesize*col, Tilesize*row, 1,this);
 					collision.add(ball);
 				}else if(rc == 151){
 					plate1 = new Tile(cp, Tilesize*col, Tilesize*row, 1,this);
 					enviroment.add(plate1);
 					plate1.setLoop(0, 0);
 				}else if(rc == 152){
 					plate2 = new Tile(cp, Tilesize*col, Tilesize*row, 1,this);
 					enviroment.add(plate2);
 					plate2.setLoop(0, 0);
 				}else if(rc == 153){
 					plate3 = new Tile(cp, Tilesize*col, Tilesize*row, 1,this);
 					enviroment.add(plate3);
 					plate3.setLoop(0, 0);
 				}else if(rc == 154){
 					plate4 = new Tile(cp, Tilesize*col, Tilesize*row, 1,this);
 					enviroment.add(plate4);
 					plate4.setLoop(0,0);
 				}
 			}
 		}
 	}
 	
 	/*********************************************************************************************************************
 	 * Funktion wird aufgerufen wenn ein Gegner stirbt, das boolsche b gibt an ob es ein Boss drop ist oder normaler Mop *
 	 *********************************************************************************************************************/
 	public void SpawnItem(double x, double y, boolean b) {
 		
 		double xi = x;
 		double yi = y;
 		item = new Item(IT, xi, yi+16, 100, this, b, null);
 		actors.add(item);
 	}
 	
 	
 	/*********************************************
 	 * Boss Zauber mit dem Arcane Atribut mit 	 *
 	 * bergeben wird die x/y-Postion des Bosses *
 	 *********************************************/
 	public void createBoltBoss(double xb, double yb){	
 		
 		double x;
 		double y;
 		x=xb;		
 		y=yb+33;
 		mbb = new MagicBolt(Bolt, x, y, 100, this, true, "arcane");
 		actors.add(mbb);
 	}
 	
 	/****************************************
 	 * Player Zauber mit dem Arcane Atribut *
 	 ****************************************/
 	public void createBolt() {		
 		
 		if (dir == 1) { // hoch schieen
 			x = hero.getX();
 			y = hero.getY() - 8;
 		} else if (dir == 2) { // nach links schieen
 			x = hero.getX() - 8;
 			y = hero.getY() + Tilesize;
 		} else if (dir == 3) { // runter schieen
 			x = hero.getX();
 			y = hero.getY() + 24;
 		} else if (dir == 4) { // nach rechts schieen
 			x = hero.getX() + 8;
 			y = hero.getY() + Tilesize;
 		}
 		mb = new MagicBolt(Bolt, x, y, 100, this, false, "arcane");
 
 		ListIterator<Sprite> it = actors.listIterator();
 		it.add(mb);
 		hero.redMana(25);
 	}
 	
 	
 	/*********************************************************************
 	 * Diese Funktion wird aufgerufen wenn man ein Item aufgehoben hat.  *
 	 * boolean h gibt an ob das Item eine Health-Potion sein darf wrend *
 	 * int hp die Wahrscheinlich keit angibt. Das selbe gilt auch fr    *
 	 * boolean m und int mp, wobei es hier um Mana-Potion geht			 *
 	 *********************************************************************/
 	public void generateItem(boolean h, int hp, boolean m, int mp){
 		
 		
 		if(h == true){
 			int rn =(int) (Math.random()*hp);
 			System.out.println(rn);
 			if(rn%2 == 0 && rn >= 3){
 				hero.addHealth(10, this.phealth);
 			}
 		}
 		if(m == true){
 			int rn =(int) (Math.random()*mp);
 			System.out.println(rn);
 			if(rn%2 == 1 && rn >=3){
 				hero.addMana(10);
 			}
 		}
 	}
 	
 	
 	private void startGame() throws IOException {
 		if(checkp){
 			lvl = savelvl;
 		}else
 			lvl = 1;
 		if(!join){
 			read();
 		}
 		status = true;
 		if (status) {
 			if(!checkp){
 				if (GUI.running || join) {
 					c = new Client(InetAddress.getByName(JOptionPane.showInputDialog("Please enter the IP: ")), 1333, this);
 				}
 			}
 			if(checkp || life != 0){
 				EnemyCounter = 0;
 			}
 				doInitializations();
 			if(checkp||life != 0){
 				if(check != null)
 					check.setLoop(1, 1);
 				this.phealth = 130;
 				this.mana = 100;
 				this.armor = 100;
 			}
 			soundlib.loopSound("test");
 			setStarted(true);
 			dead = false;
 			finished = false;
 
 		}
 	}
 
 	
 	/****************************************
 	 * Funktion um ins Men zurck zukehren *
 	 ****************************************/
 	public void stopGame() {
 
 		
 		b0.setVisible(true);
 		b1.setVisible(true);
 		b2.setVisible(true);
 		b3.setVisible(true);
 		setStarted(false);
 		lvl = 1;
 		join = false;
 		OoM = false;
 		checkp = false;
 		phealth = 130;
 		mana = 100;
 		armor = 100;
 		Coins = 0;
 		Clear(false);
 		soundlib.stopLoopingSound();
 	}
 
 	private void checkKeys() {
 		if(!join){
 			if (up) {
 	
 				oldY = moveY;
 				oldX = moveX;
 	
 				if (leveldata[moveY - 1][moveX] == 1|| leveldata[moveY - 1][moveX] == 11|| leveldata[moveY - 1][moveX] == 16) {
 					moveY = oldY;
 				} else if (leveldata[moveY - 1][moveX] == 4 || leveldata[moveY - 1][moveX] == 30|| leveldata[moveY-1][moveX] == 31|| leveldata[moveY-1][moveX] == 99) {
 					moveY = oldY;
 				} else if(leveldata[moveY -1][moveX]==9 && EnemyCounter != 0){
 					moveY = oldY;
 				} else
 					moveY--;
 			}
 	
 			if (down) {
 	
 				oldY = moveY;
 				oldX = moveX;
 	
 				if (leveldata[moveY + 1][moveX] == 1|| leveldata[moveY + 1][moveX] == 11|| leveldata[moveY + 1][moveX] == 16) {
 					moveY = oldY;
 				} else if (leveldata[moveY + 1][moveX] == 4|| leveldata[moveY + 1][moveX] == 30|| leveldata[moveY+1][moveX] == 31|| leveldata[moveY+1][moveX] == 99) {
 					moveY = oldY;
 				} else if(leveldata[moveY +1][moveX]==9 && EnemyCounter != 0){
 					moveY = oldY;
 				}else
 					moveY++;
 			}
 	
 			if (left) {
 	
 				oldY = moveY;
 				oldX = moveX;
 	
 				if (leveldata[moveY][moveX - 1] == 1|| leveldata[moveY][moveX-1] == 11|| leveldata[moveY][moveX-1] == 16) {
 					moveX = oldX;
 				} else if (leveldata[moveY][moveX - 1] == 4|| leveldata[moveY][moveX-1] == 30|| leveldata[moveY][moveX-1] == 31|| leveldata[moveY][moveX-1] == 99) {
 					moveX = oldX;
 				} else if(leveldata[moveY][moveX-1]==9 && EnemyCounter != 0){
 					moveX = oldX;
 				}else
 					moveX--;
 			}
 	
 			if (right) {
 	
 				oldY = moveY;
 				oldX = moveX;
 	
 				if (leveldata[moveY][moveX + 1] == 1|| leveldata[moveY][moveX+1] == 11|| leveldata[moveY][moveX+1] == 16) {
 					moveX = oldX;
 				} else if (leveldata[moveY][moveX + 1] == 4|| leveldata[moveY][moveX+1] == 30|| leveldata[moveY][moveX+1] == 31|| leveldata[moveY][moveX+1] == 99) {
 					moveX = oldX;
 				} else if(leveldata[moveY][moveX+1]==9 && EnemyCounter != 0){
 					moveX = oldX;
 				}else
 					moveX++;
 			}
 		}
 		
 		
 
 	}
 	
 	/*********************************************
 	 * Verabreitung des Inputs der Tastatur      *
 	 * Im fall des MP erstellung der DatenPacket *
 	 *********************************************/
 	public void keyPressed(KeyEvent e){
 
 		
 		if (!dead) {
 			if (e.getKeyCode() == KeyEvent.VK_UP) {
 				up = true;
 				dir = 1;
 				down = false;
 				left = false;
 				right = false;
 				if (join) {
 					Packet02Move movePacket = new Packet02Move(player1.getUsername(), player1.x, player1.y, this.dir);
 					try {
 						c.getOutput().writeObject(movePacket);
 					} catch (IOException e1) {
 						e1.printStackTrace();
 					}
 				}
 			}
 
 			if (e.getKeyCode() == KeyEvent.VK_DOWN) {
 				down = true;
 				dir = 3;
 				up = false;
 				left = false;
 				right = false;
 				if (join) {
 					Packet02Move movePacket = new Packet02Move(player1.getUsername(), player1.x, player1.y, this.dir);
 					try {
 						c.getOutput().writeObject(movePacket);
 					} catch (IOException e1) {
 						e1.printStackTrace();
 					}
 				}
 			}
 
 			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
 				left = true;
 				dir = 2;
 				down = false;
 				up = false;
 				right = false;
 				if (join) {
 					Packet02Move movePacket = new Packet02Move(player1.getUsername(), player1.x, player1.y, this.dir);
 					try {
 						c.getOutput().writeObject(movePacket);
 					} catch (IOException e1) {
 						e1.printStackTrace();
 					}
 				}
 			}
 
 			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
 				right = true;
 				dir = 4;
 				down = false;
 				left = false;
 				up = false;
 				if (join) {
 					Packet02Move movePacket = new Packet02Move(player1.getUsername(), player1.x, player1.y, this.dir);
 					try {
 						c.getOutput().writeObject(movePacket);
 					} catch (IOException e1) {
 						e1.printStackTrace();
 					}
 				}
 			}
 			if(e.getKeyCode()== KeyEvent.VK_SPACE){
 				if(dir==1){
 					yy = -4;
 					xx = 0;
 					sword.setLoop(1,3);
 					sup = true;
 				}else if(dir == 2){
 					xx = -16;
 					yy = +16;
 					sword.setLoop(7,9);
 					sleft = true;
 				}else if(dir == 3){
 					yy = +32;
 					xx = 0;
 					sword.setLoop(4,6);
 					sdown = true;
 				}else if(dir == 4){
 					xx = +16;
 					yy = +16;
 					sword.setLoop(10,12);
 					sright = true;
 				}
 			}
 		}
 	}
 
 	/*******************************************************************************
 	 * In dieser Funtkion wird der Tastatur Input beim loslassen der Taste erfasst *
 	 *******************************************************************************/
 	public void keyReleased(KeyEvent e){
 
 		
 		if (e.getKeyCode() == KeyEvent.VK_UP) {
 			up = false;
 		}
 
 		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
 			down = false;			
 		}
 
 		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
 			left = false;			
 		}
 
 		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
 			right = false;
 		}
 
 		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
 			if (isStarted()) {
 				stopGame();
 			}
 		}
 		if(e.getKeyCode() == KeyEvent.VK_F1){
 			hero.addMana(100);
 		}
 		if (e.getKeyCode() == KeyEvent.VK_F) {
 			if (!dead) {
 				if (!OoM && mana >= 25) {
 					if (dir != 0) {
 						createBolt();
 					}
 				}
 			}
 		}
 		if(e.getKeyCode()== KeyEvent.VK_SPACE){
 			if(dir==1){
 				sword.setLoop(0,0);
 			}else if(dir == 2){
 				sword.setLoop(0,0);
 			}else if(dir == 3){
 				sword.setLoop(0,0);
 			}else if(dir == 4){
 				sword.setLoop(0,0);
 			}
 			sup = sleft = sright = sdown = false;
 		}
 			if(e.getKeyCode()== KeyEvent.VK_E && (npc != null && npc.dis == 1 && lvl==1)){
 				page++;
 				if(page>3)
 					page=0;
 			}else
 				page = 0;	
 	if(lvl>1){
 		if(shopowner != null && shopowner.dis == 1){
 			if(e.getKeyCode()==KeyEvent.VK_E){
 				page++;
 				if(page>1)
 					page=0;
 			}
 		}else
 			page = 0;
 		}
 
 		if(e.getKeyCode() == KeyEvent.VK_1 && Coins >=5 && showtext == true){
 			hero.addHealth(25, phealth);
 			Coins = Coins - 5;
 			page = 1;
 		}
 		if(e.getKeyCode() == KeyEvent.VK_1 && Coins <5 && showtext == true){
 			page = 2;
 		}
 		if(e.getKeyCode() == KeyEvent.VK_2 && Coins >=7 && showtext == true){
 			hero.addMana(25);
 			Coins = Coins - 7;
 		}
 		if(e.getKeyCode() == KeyEvent.VK_2 && Coins <7 && showtext == true){
 			page = 2;
 		}
 		if(e.getKeyCode() == KeyEvent.VK_3 && Coins >=30 && showtext == true){
 			Coins = Coins - 30;
 		}
 		if(e.getKeyCode() == KeyEvent.VK_3 && Coins <30 && showtext == true){
 			page = 2;
 		}
 	}
 	
 	public void keyTyped(KeyEvent e){
 	}	
 
 	private void computeDelta() {
 
 		delta = System.nanoTime() - last;
 		last = System.nanoTime();
 	}
 
 	@Override
 	protected void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		if (started) {
 			g.setColor(Color.white);
 			g.fillRect(0, Height, Width, frame.getHeight() - Height);
 			g.setColor(Color.red);
 			g.drawString("HP: " + (Math.round(phealth) * 100 / 130), 2, 253);
 			g.setColor(Color.blue);
 			g.drawString("Mana: " + mana, 50, 253);
 			g.setColor(Color.black);
 			g.drawString("Coins: " + Coins, 120, 253);
 			if(!join){
 				g.drawString("Def: "+ (int)hero.armor, 185, 253);
 			}
 		}
 
 		if (dead) {
 			g.setColor(Color.red);
 			g.drawString(gameov, 20, Height / 2);
 		}
 		if (finished) {
 			g.setColor(Color.red);
 			g.drawString(finish, 40, Height / 2);
 			repaint();
 		}
 		if (!started) {
 			return;
 		}
 		if(init == true){
 			if(npc != null){
 				if(npc.dis==1 && page != 0){
 						switch(page){
 						case 1:
 							npc1.setText("Hey listen! ");
 							break;
 						case 2:
 							npc1.setText("I used to be an adventurer, "+
 									 "just like you, until... ");
 							break;
 						case 3:
 							npc1.setText("It's dangerous outside! Take this.");
 							init = false;
 							break;
 						}
 						npc1.setVisible(true);
 					}else{
 						npc1.setVisible(false);
 					}
 			}
 		}
 		if(init == false){
 			if(npc != null){
 				if(npc.dis==1 && page != 0){
 					switch(page){
 					case 1:
 						npc1.setText("Be careful outside");
 						break;
 					case 2:
 						npc1.setText("Use \"Space\" to attack your enemies, "+
 								"or \"F\" to use Magic.");
 						break;
 					case 3:
 						npc1.setText("Now go.");
 						break;
 					}
 					npc1.setVisible(true);
 				}else{
 					npc1.setVisible(false);
 				}
 			}
 					
 		}
 		if(shopowner != null){
 			if(shopowner.dis==1 && page != 0){
 				switch(page){
 				case 1:
 					shop.setText("What do you wann buy?\n" +
 							"1) Health Potion: 5 Coins\n" +
 							"2) Mana Potion: 7 Coins\n" +
 							"3) Armor: 30 Coins");
 					showtext = true;
 					break;
 				case 2:
 					shop.setText("You dont have enough Coins.");
 				}
 				shop.setVisible(true);
 			}else{
 				shop.setVisible(false);
 				showtext = false;
 			}
 		}
 		
 		for (ListIterator<Enviroment> ev = painter2.listIterator(); ev
 				.hasNext();) {
 			Enviroment e = ev.next();
 			e.drawObjects(g);
 		}
 		for (ListIterator<Sprite> co = painter3.listIterator(); co.hasNext();) {
 			Sprite r = co.next();
 			r.drawObjects(g);
 		}
 		for (ListIterator<Sprite> it = painter.listIterator(); it.hasNext();) {
 			Sprite r = it.next();
 			r.drawObjects(g);
 		}
 
 	}
 
 	private static BufferedImage[] loadPics(String path, int pics) { // Methode bekommt
 																// Speicherort
 																// und Anzahl
 																// der
 																// Einzelbilder
 																// bergeben
 
 		BufferedImage[] anim = new BufferedImage[pics]; // Erzeugung des
 														// Image-Arrays mit der
 														// gre der
 														// Einzelbilder
 		BufferedImage source = null; // ldt das ganze Bild
 
 		try {
 			source = ImageIO.read(new File(path)); // Quellbild wird ber ImageIO
 											// geladen
 		} catch (IOException e) {
 		}
 
 		for (int x = 0; x < pics; x++) {
 			anim[x] = source.getSubimage(x * source.getWidth() / pics, 0,
 					source.getWidth() / pics, source.getHeight()); // die
 																	// Methode
 																	// getSubimage
 																	// zerlegt
 																	// das
 																	// Quellbild
 																	// in die
 																	// Anzahl
 																	// der
 																	// angegebenen
 																	// Einzelbilder.
 		}
 
 		return anim;
 	}
 
 	private boolean isStarted() {
 		return started;
 	}
 
 	private void setStarted(boolean started) {
 		this.started = started;
 	}
 
 	
 	@SuppressWarnings("resource")
 	private void read() {
 		try {
 			String sTemp;
 			String rest = null;
 			int i, j;
 			leveldata = new int[Height / Tilesize][Width / Tilesize];
 			switch (lvl) {
 			case 1:
 				rest = "lvl1.level";
 				break;
 			case 2:
 				rest = "lvl2.level";
 				break;
 			case 3:
 				rest = "lvl3.level";
 				break;
 			case 4:
 				rest = "lvl4.level";
 				break;
 			case 5:
 				rest = "lvl5.level";
 				break;
 			case 6:
 				rest = "lvl6.level";
 				break;
 			case 7:
 				rest = "lvl7.level";
 				break;
 			case 8:
 				rest = "lvl8.level";
 				break;
 			case 9:
 				rest = "lvl9.level";
 				break;
 			case 10:
 				rest = "lvlBoss1.level";
 				boss = true;
 				break;
 			default:
 				System.out.println("Level existiert nicht");
 				frame.dispose();
 				break;
 			}
 			BufferedReader oReader = new BufferedReader(new InputStreamReader(
 					new FileInputStream(new File("res/lvl/SP/" + rest)))); // Zeile
 																		// fr
 			// Zeile
 			// einlesen
 
 			i = 0;
 
 			while ((sTemp = oReader.readLine()) != null) {
 
 				// Zeile in Einzelteile zerlegen (wir trennen durch ;
 
 				java.util.StringTokenizer stWerte = new StringTokenizer(sTemp,
 						";");
 
 				j = 0;
 
 				// Nun eintragen in den Array. Es wird nicht berprft, ob
 				// die Grenzen berschritten werden!
 
 				while (stWerte.hasMoreTokens()) {
 
 					leveldata[i][j] = Integer.parseInt(stWerte.nextToken());
 					j++;
 				}
 				i++;
 			}
 			status = true;
 		} catch (FileNotFoundException e) {
 
 			e.printStackTrace(); // Fehler ausdrucken
 
 		} catch (IOException e) {
 
 			e.printStackTrace(); // Fehler ausdrucken
 
 		}
 	}
 	
 	
 	/*********************************************************************
 	 * Hier werden die eingehenden Bewegungsdaten vom Server verabreitet *
 	 *********************************************************************/
     public synchronized void movePlayer(String username, double x, double y, int movingDir) {
     
     	
     	if(username.equals(player1.Username)){
 	    	player1.x = x;
 	        player1.y = y;
 	        player1.setMovingDir(movingDir);
        }else{
     	   	player2.x = x;
 	        player2.y = y;
 	        player2.setMovingDir(movingDir);
        }
     }
 
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		
 	}
 	
 	/*********************************************
 	 * 2. Spieler hinzufgen und zeichnen lassen *
 	 *********************************************/
 	public synchronized void addPlayerMP(PlayerMP p) {		
 		player2 = p;
 		actors.add(player2);
         this.connectedPlayers.add(player2);
     }
 	
 	/********************************************
 	 * Das selbe wie addPlayerMP nur fr Gegner *
 	 ********************************************/
 	public synchronized void addEnemy(Enemy e){		
 		ene = e;
 		actors.add(ene);
 		this.spawnedEnemys.add(ene);		
 	}
 	
 	/**********************************************
 	 * Entfernt anhand der ID den passenden Gegner *
 	 **********************************************/
 	public synchronized void removeEnemy(int id){	
 		int index = 0;
 		for(Enemy e : this.spawnedEnemys){
 			if(e.getID() == id){
 				break;
 			}
 			index++;
 		}
 		this.spawnedEnemys.remove(index);
 		actors.remove(e);
 	}
 	
 	/*********************************************************
 	 * Verarbeitet die eingegangen Bewegungsdaten vom Server *
 	 * und passt die Lokalen Coordinaten der Gegner an.      *
 	 *********************************************************/
 	public synchronized void syncMove(int id, double x, double y){		
 		int index = 0;
 		for(Enemy e : this.spawnedEnemys){
 			if(e.getID() == id){
 				break;
 			}
 			index++;
 		}
 		this.spawnedEnemys.get(index).x = x;
 		this.spawnedEnemys.get(index).y = y;
 	}
 	
 	/*************************************************************
 	 * Wenn ein Spieler Disconnected wird er ber diese Funtkion *
 	 * bei den anderen Spieler aus dem Spiel gelscht            *
 	 *************************************************************/
 	public synchronized void removePlayerMP(String username) {
         int index = 0;
         for (PlayerMP p : this.connectedPlayers) {
             if (p.getUsername().equals(username)) {
                 break;
             }
             index++;
         }
         this.connectedPlayers.remove(index);
         actors.remove(player2);
     }
 	
 	/*******************************************************
 	 * Funktion zur initalisierung der Startposition im MP *
 	 *******************************************************/
 	public void setStart(String username, double x, double y, int id){		
     	if(username.equals(player1.Username)){
 	    	player1.x = x;
 	        player1.y = y;
 	        player1.id = id;
 	        
        }else{
     	   	player2.x = x;
 	        player2.y = y;
 	        player2.id = id;
        }	
 		
 	}
 	
 	private void requestECoor(){
 		
 		Packet05EnemyMove o = new Packet05EnemyMove();
 		try {
 			c.getOutput().writeObject(o);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void finish(String name){
 		finish = name +" wins";
 		finished = true;
 		stopGame();
 	}
 
 }
