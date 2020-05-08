 package com.mbl111.ld26;
 
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.image.BufferStrategy;
 
 import javax.swing.JFrame;
 
 import com.mbl111.ld26.input.Input;
 import com.mbl111.ld26.input.InputHandler;
 import com.mbl111.ld26.screen.PlayerView;
 import com.mbl111.ld26.screen.Screen;
 import com.mbl111.ld26.world.World;
 
 public class Game extends Canvas implements Runnable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 7047831301158704485L;
 	public static final int WIDTH = 360;
 	public static final int HEIGHT = WIDTH * 3 / 4;
 	public static final String NAME = "";
	private static int SCALE = 3;
 	public static Game instance;
 	public static int tickCount = 0;
 
 	private JFrame frame;
 	public boolean running = true;
 	public int fps = 0;
 	private Screen screen;
 	private World world;
 	private PlayerView view;
 	private InputHandler inputHandler;
 	private Input input = null;
 
 	public Game() {
 		instance = this;
 	}
 
 	public void start() {
		Thread t = new Thread(this, "Main Game Thread");
		t.setPriority(Thread.MAX_PRIORITY);
		t.start();
 	}
 
 	public void stop() {
 		this.running = false;
 	}
 
 	public void rescale(int scale) {
 		if (this.running == false) {
 			throw new UnsupportedOperationException("Can't rescale the window while the game is not running. ");
 		}
 		this.SCALE = scale;
 		this.setMinimumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
 		this.setMaximumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
 		this.setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
 		frame.pack();
 		frame.setLocationRelativeTo(null);
 	}
 
 	private void init() {
 		System.out.println("Init");
 		screen = new Screen(WIDTH, HEIGHT);
 		view = new PlayerView(WIDTH, HEIGHT);
 		world = new World(32, 32);
 		inputHandler = new InputHandler(this);
 	}
 
 	public void run() {
 		long lastTime = System.nanoTime();
 		double unprocessed = 0;
 		double nsPerTick = 1000000000.0 / 60.0;
 		int frames = 0;
 		int ticks = 0;
 		long lastTimer1 = System.currentTimeMillis();
 		init();
 		while (running) {
 			long now = System.nanoTime();
 			unprocessed += (now - lastTime) / nsPerTick;
 			lastTime = now;
 			boolean shouldRender = true;
 
 			while (unprocessed >= 1) {
 				ticks++;
 				tick();
 				unprocessed -= 1;
 				shouldRender = true;
 			}
 
 			// try {
 			// Thread.sleep(1L);
 			// } catch (InterruptedException e) {
 			// e.printStackTrace();
 			// }
 
 			{
 				if (shouldRender) {
 					frames++;
 					render();
 				}
 			}
 
 			if (System.currentTimeMillis() - lastTimer1 > 1000) {
 				lastTimer1 += 1000;
 				this.fps = frames;
 				System.out.println(ticks + " ticks - " + frames + " fps");
 				frames = 0;
 				ticks = 0;
 			}
 		}
 
 		System.exit(0);
 	}
 
 	private void tick() {
 		tickCount++;
 		input = inputHandler.updateMouseStatus(SCALE);
 		this.view.tick();
 
 		if (this.hasFocus()) {
 		}
 	}
 
 	private void render() {
 		BufferStrategy bs = this.getBufferStrategy();
 		if (bs == null) {
 			this.createBufferStrategy(3);
 			return;
 		}
 		screen.clear(0xFFFFFFFF);
 		view.render(screen);
 
 		Graphics g = bs.getDrawGraphics();
 		g.fillRect(0, 0, getWidth(), getHeight());
 		int ww = WIDTH * SCALE;
 		int hh = HEIGHT * SCALE;
 		int xo = (getWidth() - ww) / 2;
 		int yo = (getHeight() - hh) / 2;
 		g.drawImage(screen.image, xo, yo, ww, hh, null);
 		g.dispose();
 		bs.show();
 	}
 
 	public static void main(String[] args) {
 		Game game = new Game();
 		game.setMinimumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
 		game.setMaximumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
 		game.setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
 
 		game.frame = new JFrame(NAME);
 		game.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		game.frame.setLayout(new BorderLayout());
 		game.frame.add(game, BorderLayout.CENTER);
 		game.frame.pack();
 		game.frame.setResizable(false);
 		game.frame.setLocationRelativeTo(null);
 		game.frame.setVisible(true);
 
 		game.start();
 	}
 
 	public World getWorld() {
 		return world;
 	}
 
 	public Input getInput() {
 		return input;
 	}
 
 	public PlayerView getPlayerView() {
 		return view;
 	}
 
 }
