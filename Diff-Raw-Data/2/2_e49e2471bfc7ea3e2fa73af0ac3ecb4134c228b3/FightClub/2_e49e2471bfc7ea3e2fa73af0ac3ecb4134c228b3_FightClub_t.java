 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 public class FightClub extends JPanel implements Runnable {
 
 	/**
 	 * 
 	 */
 
 	// public static variables
 	private static final long serialVersionUID = 1L;
 	private static boolean paused = false;
 	public static final int NUMKEYS = 8;
 	public static ArrayList<Bullet> bullets = new ArrayList<Bullet>();
 	public static ArrayList<Player> players = new ArrayList<Player>();
 	public static ArrayList<Enemy> enemies = new ArrayList<Enemy>();
 	public static double lasttime = 0;
 	public static int HEIGHT = 500;
 	public static int WIDTH = 500;
 
 	// default non-static variables variables
 	Player player;
 
 	// private non-static variables
 	private Menu menu;
 	private InputHandler inHandler;
 	private ComponentHandler compHandler;
 	private long currentTime;
 	private long oldTime = 0;
 	// private long lastTime = 0;
 	// private long timeBetweenFrames = 0;
 	private long spawningTime = 0;
 	private boolean a[] = new boolean[NUMKEYS];
 	private double b = 0;
 
 	/**
 	 * @param args
 	 * 
 	 */
 
 	public FightClub() {
 		Image imgp1 = Toolkit.getDefaultToolkit().getImage(
 				getClass().getResource("img/player.png"));
 		// makes new player and adds it to the game
 		player = new Player(50, 50, 50, 50, imgp1, 10, false);
 		FireArm arm = FireArm.createGun();
 		player.add(arm);
 		players.add(player);
 		// makes new listener (ComponentHandler and InputHandler) and add them
 		// to the game
 		inHandler = new InputHandler();
 		compHandler = new ComponentHandler();
 		addKeyListener(inHandler);
 		addComponentListener(compHandler);
 		// sets the menu to the IntroScreenMenu (at the begin of the game)
 		setMenu(new IntroScreenMenu());
 
 		// start the game thread (run-method)
 		new Thread(this).start();
 	}
 
 	// main render logic of the game
 	public void paintComponent(Graphics g) {
 
 		// makes Graphics to Graphics2D
 		Graphics2D g2 = (Graphics2D) g;
 
 		// over draws the old image
 		g2.setColor(Color.black);
 		g2.fillRect(0, 0, WIDTH, HEIGHT);
 
 		// if there is no menu to render it renders the game self
 		if (menu == null) {
 			// draws all enemies
 			for (int w = 0; w < enemies.size(); w++) {
 				Enemy enim = (Enemy) enemies.get(w);
 				enim.render(g2);
 			}
 
 			// draws all bullets and checks if they are still in the frame
 			for (int w = 0; w < bullets.size(); w++) {
 				Bullet bul = (Bullet) bullets.get(w);
 
 				if (bul.getX_Point() > WIDTH || bul.getX_Point() < 0
 						|| bul.getY_Point() > HEIGHT || bul.getY_Point() < 0) {
 					bullets.remove(w);
 				} else {
 					// System.out.println("Works: "+w);
 					bul.render(g2);
 				}
 			}
 			// draw the player
 			player.render(g2);
 		} else {
 			// if there is a menu, the menu is drawn, not the game itself
 			menu.render(g2);
 
 		}
 	}
 
 	// gets the focus from the frame to the panel
 	public void addNotify() {
 		super.addNotify();
 		requestFocus();
 	}
 
 	// Main-method
 	public static void main(String[] args) {
 		// opens new frame
 		JFrame frame = new JFrame();
 		frame.setSize(500, 500);
 		// game paused false
 		paused = false;
 		// make a new JPanel (this FightClub class) and adds it to the frame
 		final FightClub mainP = new FightClub();
 		frame.add(mainP);
 		frame.setVisible(true);
 		frame.setBackground(Color.black);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 	}
 
 	// the main game cycle
 	public void run() {
 		while (!paused) {
 
 			currentTime = System.nanoTime();
 			// timeBetweenFrames = 1000000000 / (currentTime - lastTime);
 			// System.out.println(timeBetweenFrames);
 			// lastTime = currentTime;
 			// System.out.println(1000/timeBetweenFrames);
 
 			update();
 			repaint(); // to paintcomponent
 
 			try {
 				Thread.sleep((long) 1.0);
 			} catch (InterruptedException e) {
 				System.out.println("Coulden't start sleep:" + e);
 				e.printStackTrace();
 			}
 
 		}
 	}
 
 	// origin of all updates in the game
 	public void update() {
 		if (menu == null) {
 			player.setVisible(true); // TODO belongs not here
 			if (currentTime > (oldTime + 10000000)) { // 1/100 sec
 
 				// gets the array for the keys
 				getA();
 
 				// updates the player
 				player.update(a, b, this);
 				// goes through all enemies in the list
 				for (int w = 0; w < enemies.size(); w++) {
 					Enemy enim = (Enemy) enemies.get(w);
 					// updates object of enemies
 					enim.update();
 					// checks if the enemie hits something
 					enemyHittest(enim);
 				}
 				// goes through all bullets in the list
 				for (int w = 0; w < bullets.size(); w++) {
 					Bullet bul = (Bullet) bullets.get(w);
 					// updates object of bullet
 					bul.update();
 					// checks, if the bullet hits something
 					bulletHittest(bul);
 				}
 				// DEBUGGING KEY
 				if (a[7] == true) {
 					player.setLife(0);
 				}
 
 				for (int i = 0; i < NUMKEYS; i++) {
 					a[i] = false;
 
 				}
 
 				// checks, if the player has lifes left
 				// otherwise it removes the resets the game
 				if (player.getLife() <= 0) {
 					setMenu(new LooseMenu());
 					resetGame();
 
 				}
 				oldTime = currentTime;
 
 				// if enough time has gone by, a new enemy is born
 				if (currentTime > (spawningTime + 3000000000L)) { // 3 sec
 
 					spawnEnemies();
 					spawningTime = currentTime;
 				}
 
 			}
 			
 			// Sets Pause, if Esc is pressed
 			if (a[6]) { // a[6] = Esc
 				setMenu(new PauseMenu());
 				
 			}
 
 		} else {
 			// if there is a menu to serve, the menu is updated
 			if (currentTime > (oldTime + 100000000)) { // every 10th sec
 				menu.update(this, getA());
 
 				oldTime = currentTime;
 			}
 
 		}
 	}
 
 	// added reset-method, to reset game after the player has lost
 	private void resetGame() {
 		players.remove(player);
 		Image imgp1 = Toolkit.getDefaultToolkit().getImage(
 				getClass().getResource("img/player.png"));
 		player = new Player(50, 50, 50, 50, imgp1, 10, false);
		FireArm arm = FireArm.createGun();
		player.add(arm);
 		players.add(player);
 
 		// clears the enemies list
 		if (!enemies.isEmpty()) {
 			for (int i = 0; i < enemies.size(); i++) {
 				enemies.remove(i);
 			}
 		}
 		// clears the bullet list
 		if (!bullets.isEmpty()) {
 			for (int i = 0; i < bullets.size(); i++) {
 				bullets.remove(i);
 			}
 		}
 	}
 
 	// checks if an enemy hits the player
 	private void enemyHittest(Enemy enim) {
 		if (player.getRot().createTransformedShape(player.getRect())
 				.contains(enim.getPoint())) {
 
 			player.setLife(player.getLife() - 1);
 			enemies.remove(enim);
 		}
 
 	}
 
 	// checks if a bullet hits the player or the an enemy
 	private void bulletHittest(Bullet bullet) {
 		if (player.getRot().createTransformedShape(player.getRect())
 				.contains(bullet.getPoint())) {
 
 			player.setLife(player.getLife() - 1);
 		}
 
 		for (int i = 0; i < enemies.size(); i++) {
 			Enemy enim = (Enemy) enemies.get(i);
 
 			if (enim.getRect().intersects(bullet.getRect())) {
 				// method to remove lifes, when hit
 				// the number of removed lifes is exactly the number of the
 				// bullet power
 				enim.setLife(enim.getLife() - bullet.getPower());
 				bullets.remove(bullet);
 
 			}
 			// if lives equals zero, the enemy is removed
 			if (enim.getLife() == 0) {
 				enemies.remove(enim);
 			}
 
 		}
 
 	}
 
 	// method to spawn the enmies
 	private void spawnEnemies() {
 		Image imge1 = Toolkit.getDefaultToolkit().getImage(
 				getClass().getResource("img/enemy.png"));
 		int x = 0;
 		int y = 0;
 		double o = Math.random();
 		// Determinate where to spawn the enemies with a random double
 		if (o < 0.25) {
 			x = (int) (Math.random() * WIDTH);
 			y = HEIGHT;
 		} else if (o < 0.5) {
 			x = (int) (Math.random() * WIDTH);
 			y = 0;
 		} else if (o < 0.75) {
 			y = (int) (Math.random() * HEIGHT);
 			x = WIDTH;
 		} else if (o < 1) {
 			y = (int) (Math.random() * HEIGHT);
 			x = 0;
 		}
 		// makes a new enemy and adds it to the list
 		ShooterEnemy enim = new ShooterEnemy(x, y, 30, 30, imge1, 2, true);
 		enemies.add(enim);
 
 	}
 
 	public static ArrayList<Bullet> getBullets() {
 		return bullets;
 	}
 
 	public static double getLasttime() {
 		return lasttime;
 	}
 
 	public static void setLasttime(double lasttime) {
 		FightClub.lasttime = lasttime;
 	}
 
 	// get the pressed keys and converts them into an array
 	public boolean[] getA() {
 
 		if (inHandler.getKeys()[KeyEvent.VK_W]) {
 			a[0] = true;
 
 		}
 		if (inHandler.getKeys()[KeyEvent.VK_S]) {
 			a[1] = true;
 
 		}
 		if (inHandler.getKeys()[KeyEvent.VK_A]) {
 			a[2] = true;
 
 		}
 		if (inHandler.getKeys()[KeyEvent.VK_D]) {
 			a[3] = true;
 
 		}
 
 		if (inHandler.getKeys()[KeyEvent.VK_UP]) {
 			b = b + 0.02;
 
 		}
 
 		if (inHandler.getKeys()[KeyEvent.VK_DOWN]) {
 			b = b - 0.02;
 
 		}
 
 		if (inHandler.getKeys()[KeyEvent.VK_SPACE]) {
 			a[4] = true;
 
 		}
 		if (inHandler.getKeys()[KeyEvent.VK_ENTER]) {
 			a[5] = true;
 		}
 
 		if (inHandler.getKeys()[KeyEvent.VK_ESCAPE]) {
 			a[6] = true;
 		}
 		if (inHandler.getKeys()[KeyEvent.VK_L]) {
 			a[7] = true;
 		}
 
 		return a;
 	}
 
 	public static Player getNearestPlayer() {
 		Player play = null;
 		for (int i = 0; i < players.size(); i++) {
 			play = (Player) players.get(i);
 
 		}
 		return play;
 	}
 
 	public void setMenu(Menu menu) {
 		this.menu = menu;
 
 	}
 
 	public void setA(boolean[] a) {
 		this.a = a;
 	}
 
 	// removed comments: also hinzugefgt
 }
