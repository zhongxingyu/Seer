 /**
  *  Copyright 2013 Phantom Coder (Richard Tester)
  */
 package com.spectregames.concentration;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.image.BufferStrategy;
 
 import javax.swing.JFrame;
 
 import com.spectregames.concentration.enums.GameState;
 import com.spectregames.concentration.gfx.Renderer;
 import com.spectregames.concentration.input.MouseInput;
 import com.spectregames.concentration.libs.Reference;
 import com.spectregames.concentration.screens.MainMenu;
 import com.spectregames.concentration.screens.SplashScreen;
 import com.spectregames.concentration.utils.ResourceLoader;
 
 /**
  *  Project: Concentration
  *  
  *  Class: Game
  *  
  *  @author Phantom Coder
  *	
  */
 public class Game extends Canvas implements Runnable {
 	private static final long serialVersionUID = -1572409472404955765L;
 	private static JFrame frame= new JFrame(Reference.TITLE);
 	private static Game game = new Game();
 	private boolean isRunning = false;
 	
 	public static GameState state = GameState.MENU;
 	private Thread game_thread;
 	private Renderer gfx;
 	
 	public MainMenu menu;
 	public SplashScreen splash;
 		
 	public static Game getInstance(){
 		return game;
 	}
 	
 	public void init(){
 		ResourceLoader.loadImages();
 		splash = new SplashScreen();
 		menu = new MainMenu();
 		gfx = new Renderer();
 		MouseInput mouse = new MouseInput();
 	    this.addMouseListener(mouse);
 	    this.addMouseMotionListener(mouse);
 
 	}
 	
 	public void tick(){
 		
 	}
 	
 	public void render(){
 		BufferStrategy bs = this.getBufferStrategy();
 		if (bs == null) {
 			createBufferStrategy(3);
 			return;
 		}
 		
 		Graphics g = bs.getDrawGraphics();
 		g.setColor(new Color(6,0,40));
 		g.fillRect(0, 0, Reference.WIDTH, Reference.HEIGHT);
 		
 		///////////////////////////////////////////////////
 		gfx.renderBackground(g);
 		gfx.renderForeground(g);
 		
 		g.dispose();
 		bs.show();
 		
 	}
 	
 	@Override
 	public void run() {
 		init();
 		long lastTime = System.nanoTime();
 		final double numTicks = 60.0;
 		double n = 1000000000 / numTicks;
 		double delta = 0;
 		int frames = 0;
 		int ticks = 0;
 		long timer = System.currentTimeMillis();
 		
 		while (isRunning){
 			long currentTime = System.nanoTime();
 			delta += (currentTime - lastTime) / n;
 			lastTime = currentTime;
 			
 			if (delta >= 1){
 				tick();
 				ticks++;
 				delta--;
 			}
 			
 			render();
 			frames++;
 			if (System.currentTimeMillis() - timer > 1000){
 				timer+=1000;
 				
 				//if (Reference.isDebugMode){
 					System.out.println("UDS: " + ticks + ", FPS: " + frames);
 					frame.setTitle(Reference.TITLE + "          " + ticks + " UDS, FPS: " + frames);
 				//}
 				
 			ticks=0;
 			frames=0;
 			}
 		}
 		stop();
 	}
 	
 	public static void main(String[] args) {
 		
 		// Sets the game window size.
 		game.setPreferredSize(new Dimension(Reference.WIDTH, Reference.HEIGHT));
 		game.setMinimumSize(new Dimension(Reference.WIDTH, Reference.HEIGHT));
 		game.setMaximumSize(new Dimension(Reference.WIDTH, Reference.HEIGHT));
 		
 		// Changes the default mouse cursor.
 		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Image image = toolkit.getImage(Reference.CURSORS + "hand_cursor.gif");
 		Point hotSpot = new Point(0,0);
 		Cursor cursor = toolkit.createCustomCursor(image, hotSpot, "Hand");
 		
 		//Changes the default java program icon to a custom one.
		Image icon = Toolkit.getDefaultToolkit().getImage(Reference.ICONS + "icon.png");
 		
 		frame.add(game);
 		frame.setCursor(cursor);
 		frame.setIconImage(icon);
 		frame.setSize(Reference.WIDTH, Reference.HEIGHT);
 		frame.setResizable(false);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setFocusable(true);
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 		frame.pack();
 				
 		game.start();
 	}
 	
 	private synchronized void start(){
 		if (isRunning)
 			return;
 		isRunning = true;
 		game_thread = new Thread(this);
 		game_thread.start();
 	}
 	
 	private synchronized void stop(){
 		if (!isRunning)
 			return;
 		isRunning = false;
 		
 		try {
 			game_thread.join();
 		} catch (InterruptedException e) { e.printStackTrace(); }
 		
 		System.exit(1);
 	}	
 }
