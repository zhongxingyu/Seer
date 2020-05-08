 package net.naprav.wardungeon;
 
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.image.BufferStrategy;
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBufferInt;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 
 import net.naprav.wardungeon.graphics.ClassTexture;
 import net.naprav.wardungeon.graphics.Display;
 import net.naprav.wardungeon.gui.UIListener;
 import net.naprav.wardungeon.gui.UIRender;
 import net.naprav.wardungeon.gui.WarDungeonGUI;
 import net.naprav.wardungeon.level.Level;
 import net.naprav.wardungeon.listen.Keyboard;
 import net.naprav.wardungeon.listen.Mouser;
 import net.naprav.wardungeon.player.ArcherClass;
 import net.naprav.wardungeon.player.KnightClass;
 import net.naprav.wardungeon.player.PlayerClass;
 import net.naprav.wardungeon.player.WizardClass;
 import net.naprav.wardungeon.sound.Music;
 
 public class WarDungeon extends Canvas implements Runnable {
 
 	private static final long serialVersionUID = 6601282971656374659L;
 
 	private static final String version = "Version: 0.1 Alpha";
 	
 	public static final int WIDTH = 460;
 	public static final int HEIGHT = 280;
 	private static final int SCALE = 2;
 	private final Dimension size = new Dimension((WIDTH * SCALE), (HEIGHT * SCALE));
 
 	private BufferedImage screen = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
 	public int[] pixels = ((DataBufferInt) screen.getRaster().getDataBuffer()).getData();
 
 	private Thread thread;
 	public static volatile boolean isRunning = false;
 	public static volatile boolean inGame = false;
 
 	public static char state = 'N';
 	public static char player_select = 'K';
 	public static char level_select = 'S';
 
 	JFrame frame;
 
 	Display display;
 	UIListener listen;
 	Keyboard key;
 	Mouser mouse;
 	Music music;
 
 	KnightClass knight;
 	WizardClass wizard;
 	ArcherClass archer;
 
 	/**
 	 * The main constructor. It's responsible for creating the JFrame and adding this canvas to it.
 	 */
 	public WarDungeon() {
 		frame = new JFrame("WarDungeon");
 
 		display = new Display(screen);
 		key = new Keyboard(200);
 		mouse = new Mouser();
 		listen = new UIListener(mouse);
 
 		addPlayerClasses();
 
 		knight.setDirection('S');
 		wizard.setDirection('S');
 		archer.setDirection('S');
 
 		setPreferredSize(size);
 		setMaximumSize(size);
 		setMinimumSize(size);
 		addKeyListener(key);
 		addMouseListener(mouse);
 
 		frame.setLayout(new BorderLayout());
 		frame.add(this, BorderLayout.CENTER);
 		frame.pack();
 
 		frame.setIconImage(new ImageIcon("res/wardungeon_logo.png").getImage());
 		frame.setLocationRelativeTo(null);
 		frame.setResizable(false);
 		frame.setVisible(true);
 
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 
 	/**
 	 * Responsible for starting the game, the most important part. It also requests the focus of the Canvas and creates a buffer strategy.
 	 */
 	public synchronized void begin() {
 		thread = new Thread(this, "WarDungeon");
 		this.createBufferStrategy(3);
 		this.requestFocus();
 
 		thread.start();
 		isRunning = true;
 	}
 
 	/**
 	 * Method responsible for stopping the thread that runs the game.
 	 * 
 	 * @throws InterruptedException
 	 */
 	public synchronized void finish() throws InterruptedException {
 		isRunning = false;
 		thread.join();
 	}
 
 	int xMove = 0, yMove = 0;
 
 	/**
 	 * This method is responsible for updating the logic behind the game, i.e. Mobs, time, AI, etc.
 	 */
 	private synchronized void tick() {
 		key.checkForKeys();
 
 		if (key.up) {
 			yMove--;
 			getPlayer().setDirection('N');
 		}
 
 		if (key.down) {
 			yMove++;
 			getPlayer().setDirection('S');
 		}
 
 		if (key.left) {
 			xMove--;
 			getPlayer().setDirection('W');
 		}
 
 		if (key.right) {
 			xMove++;
 			getPlayer().setDirection('E');
 		}
 		
 		if (key.escape) {
 			UIRender.renderInGameMenu(this);
 		}
 	}
 
 	public int frames = 0, updates = 0;
 
 	/**
 	 * Method responsible for rendering the logic in the "update()" method to the screen.
 	 */
 	private synchronized void render() {
 		BufferStrategy buffer = this.getBufferStrategy();
 		Graphics gfx = buffer.getDrawGraphics();
 
 		final int centerY = (HEIGHT / 2) - (48 / 2);
 		final int centerX = (WIDTH / 2) - (48 / 2);
 
 		display.clear();
 		getLevel().render(xMove, yMove, display);
 		display.renderPlayer(getPlayer(), centerX, centerY);
 
 		display.alignPixels(pixels);
 
 		gfx.drawImage(screen, 0, 0, getWidth(), getHeight(), null);
 
 		gfx.setFont(WarDungeonGUI.warDungeonFont());
 		gfx.setColor(new Color(230, 230, 230));
 		gfx.drawString(version, 3, 15);
 
 		if (key.show) {
 			key.showInfo(gfx, frames, updates);
 		}
 
 		gfx.dispose();
 		buffer.show();
 	}
 
 	public static void setState(char setState) {
 		state = setState;
 	}
 
 	private final void addPlayerClasses() {
 		knight = new KnightClass(ClassTexture.knight_south, 2, 5, 5);
 		wizard = new WizardClass(ClassTexture.wizard_south, 2, 6, 4);
 		archer = new ArcherClass(ClassTexture.archer_south, 4, 5, 3);
 	}
 	
 	public static void setPlayer(char selection) {
 		player_select = selection;
 		System.out.println("Playing as a " + player_select);
 	}
 
 	private final PlayerClass getPlayer() {
 		if (player_select == 'K') return knight;
 		if (player_select == 'W') return wizard;
 		if (player_select == 'A') return archer;
 		return knight;
 	}
 
 	public static void setLevel(char selection) {
 		level_select = selection;
 	}
 	
 	private final Level getLevel() {
 		if (level_select == 'C') return Level.classic;
 		return Level.survival;
 	}
 	
 	/**
 	 * The main "run()" method; needed for any class that implements the Runnable.java interface.
 	 */
 	public void run() {
 		long pastTime = System.nanoTime(), lastSecond = System.currentTimeMillis();
 
 		final float desig = 1_000_000_000F / 50F;
 		float single = 0;
 
 		// Remove to play actual game.
		//state = 50;
 
 		if (state != 50) {
 			Music.playTitleMusic();
 		}
 
 		while (isRunning == true) {
 			if (state == 'N') {
 				UIRender.renderNaprav(this);
 				if ((System.currentTimeMillis() - lastSecond) > 2000) {
 					// Wait 2 seconds and then go to the menu.
 					lastSecond += 1000;
 					state = 'M';
 				}
 			} else if (state == 'M') {
 				UIRender.renderMenu(this);
 				listen.listenInMenu();
 			} else if (state == 'O') {
 				UIRender.renderOptions(this);
 				listen.listenInOptions();
 			} else if (state == 'C') {
 				UIRender.renderCredits(this);
 				listen.listenInCredits();
 			} else if (state == 'S') {
 				UIRender.renderClassSelect(this);
 				listen.listenInClassSelection();
 			} else if (state == 'L') {
 				UIRender.renderLevelSelect(this);
 				listen.listenInLevelSelection();
 			} else if (state == 'Q') {
 				System.exit(0);
 			} else {
 				inGame = true;
				Music.stopTitleMusic();
 				break;
 			}
 		}
 
 		while (inGame == true) {
 			long currentTime = System.nanoTime();
 			single += (currentTime - pastTime) / desig;
 
 			pastTime = currentTime;
 			while (single >= 1) {
 				tick();
 				updates++;
 				single--;
 			}
 
 			frames++;
 			render();
 
 			if ((System.currentTimeMillis() - lastSecond) > 1000) {
 				lastSecond += 1000;
 				frame.setTitle("WarDungeon | FPS: " + frames + ", UPS: " + updates);
 
 				frames = 0;
 				updates = 0;
 			}
 		}
 	}
 
 	/**
 	 * The "main()" method; needed for every Java program.
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		//new Login();
 		WarDungeon dungeon = new WarDungeon();
 		dungeon.begin();
 	}
 }
