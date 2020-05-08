 package com.doobs.java2d;
 
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.image.BufferStrategy;
 
 import javax.swing.JFrame;
 
 import com.doobs.java2d.gfx.BitmapLoader;
 import com.doobs.java2d.gfx.Screen;
 import com.doobs.java2d.input.InputHandler;
 
 public class Game2D extends Canvas implements Runnable{
 	private static final long serialVersionUID = 1L;
 
	private int width, height;
 	private int scale;
 	private String title;
 	
 	private JFrame frame;
 	private Screen screen;
 	
 	private BitmapLoader bitmapLoader;
 	
 	private Thread thread;
 	private boolean running;
 	
 	private Graphics graphics;
 	private BufferStrategy bufferStrategy;
 	
 	private InputHandler input;
 	
 	private int fps;
 	private boolean vSync;
 	private boolean printFps;
 	
 	private GameLoop gameLoop;
 	
 	public Game2D() {
 		
 	}
 	public Game2D(int width, int height) {
 		this(width, height, 1);
 	}
 	public Game2D(int width, int height, int scale) {
 		this(width, height, scale, null);
 	}
 	public Game2D(int width, int height, int scale, String title) {
 		this(width, height, scale, title, null);
 	}
 	public Game2D(int width, int height, int scale, String title, GameLoop gameLoop) {
		this.width = width;
		this.height = height;
 		this.scale = scale;
 		this.title = title;
 		this.gameLoop = gameLoop;
 		Dimension canvasSize = new Dimension(width * scale, height * scale);
 		setSize(canvasSize);
 		setPreferredSize(canvasSize);
 		setMinimumSize(canvasSize);
 		setMaximumSize(canvasSize);
 		screen = new Screen(width, height);
 		frame = new JFrame(title);
 		frame.setFocusable(true);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setLocationRelativeTo(null);
 		frame.setResizable(false);
 		frame.setLayout(new BorderLayout());
 		frame.add(this, BorderLayout.CENTER);
 		frame.pack();
 		frame.setVisible(true);
 		input = new InputHandler(this);
 		bitmapLoader = new BitmapLoader();
 	}
 	
 	public void start() {
 		if (!running) {
 			thread = new Thread(this);
 			running = true;
 			thread.start();
 		}
 	}
 	
 	public void run() {
 		preRender();
 		
 		long previousTime = System.nanoTime();
 		double secondsPerTick = 1 / 60.0;
 		double unprocessedSeconds = 0;
 		boolean ticked = false;
 		int fpsTemp = 0;
 		long tickCount = 0;
 		requestFocus();
 
 		while (running) {
 			long currentTime = System.nanoTime();
 			unprocessedSeconds += (currentTime - previousTime) / 1000000000.0;
 			previousTime = currentTime;
 			while (unprocessedSeconds >= secondsPerTick) {
 				unprocessedSeconds -= secondsPerTick;
 				gameLoop.tick(input);
 				ticked = true;
 				tickCount++;
 				if (tickCount % 60 == 0) {
 					fps = fpsTemp;
 					fpsTemp = 0;
 					if(printFps) {
 						gameLoop.printFPS();
 					}
 				}
 			}
 
 			if (ticked) {
 				render();
 				if (vSync)
 					ticked = false;
 				fpsTemp++;
 			}
 		}
 	}
 	
 	public void stop() {
 		if (running) {
 			running = false;
 			try {
 				thread.join();
 			} catch(InterruptedException e) {
 				e.printStackTrace();
 			} finally {
 				System.exit(0);
 			}
 		}
 	}
 	
 	public void preRender() {
 		createBufferStrategy(2);
 		bufferStrategy = getBufferStrategy();
 	}
 	
 	public void render() {
 		graphics = bufferStrategy.getDrawGraphics();
 		gameLoop.render(screen);
 		graphics.drawImage(screen.image, 0, 0, getWidth(), getHeight(), null);
 		graphics.dispose();
 		bufferStrategy.show();
 	}
 	
 	// Getters and Setters
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
 	public int getScale() {
 		return scale;
 	}
 	public void setScale(int scale) {
 		this.scale = scale;
		if(scale > 0) frame.setSize(width * scale, height * scale);
 	}
 	public String getTitle() {
 		return title;
 	}
 	public void setTitle(String title) {
 		this.title = title;
 	}
 	public BitmapLoader getBitmapLoader() {
 		return bitmapLoader;
 	}
 	public GameLoop getGameLoop() {
 		return gameLoop;
 	}
 	public void setGameLoop(GameLoop gameLoop) {
 		this.gameLoop = gameLoop;
 	}
 	public boolean isRunning() {
 		return running;
 	}
 	public Graphics getGraphics() {
 		return graphics;
 	}
 	public int getFps() {
 		return fps;
 	}
 	public boolean isVSync() {
 		return vSync;
 	}
 	public void setVSync(boolean vSync) {
 		this.vSync = vSync;
 	}
 	public boolean getPrintFps() {
 		return printFps;
 	}
 	public void setPrintFPS(boolean printFps) {
 		this.printFps = printFps;
 	}
 }
