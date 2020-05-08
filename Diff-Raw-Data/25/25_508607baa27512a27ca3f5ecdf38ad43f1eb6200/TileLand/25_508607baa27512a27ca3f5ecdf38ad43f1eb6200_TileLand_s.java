 package net.calzoneman.TileLand;
 
 import java.awt.AWTException;
 import java.awt.BufferCapabilities;
 import java.awt.Color;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsEnvironment;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.TexturePaint;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferStrategy;
 import java.awt.image.BufferedImage;
 import java.io.File;
 
 import javax.swing.JFrame;
 
 public class TileLand {
 
 	private JFrame frame;
 	private Screen screen;
 	private InputHandler input;
 	private Level level;
 	private Player ply;
 	
 	public static void main(String[] args) {
 		TileDefinitions.init();
 		StartupGUI s = new StartupGUI();
		while(!s.isReady());
 		Level lvl;
 		if(s.makeNewLevel) lvl = new Level(s.newMapSize.width, s.newMapSize.height, s.selectedMapName);
 		else lvl = new Level(s.selectedMapName);
		s.dispose();
		new TileLand(lvl, s.playerName).run();
 	}
 	
 	public TileLand() {
 		
 	}
 	
 	public TileLand(Level lvl, String plyName) {
 		this.input = new InputHandler();
 		this.level = lvl;
 		this.ply = new Player(plyName, level, level.getSpawnpoint(), input);
 	}
 	
 	public void run() {
 		// Set up an application frame
 		frame = new JFrame("TileLand");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		if(input == null) {
 			input = new InputHandler();
 		}
 		
 		// Set up a rendering canvas
 		screen = new Screen();
 		screen.addMouseListener(input);
 		screen.addMouseMotionListener(input);
 		screen.addKeyListener(input);
 		screen.addFocusListener(input);
 		screen.requestFocusInWindow();
 		
 		frame.getContentPane().add(screen);
 		frame.pack();
 		frame.setVisible(true);
 		
 		GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
 		BufferCapabilities bufCapabilities = config.getBufferCapabilities();
 		try {
 			// Double buffer to prevent flicker
 			screen.createBufferStrategy(2, bufCapabilities);
 		} 
 		catch (AWTException ex) {
 			System.err.println("Error creating BufferStrategy");
 			return;
 		} 
 		
 		BufferStrategy bufStrategy = screen.getBufferStrategy();
 		
 		double nsPerTick = 1000000000.0 / 30; // 30 ticks per second
 		long lastTime = System.nanoTime();
 		long lastLog = System.currentTimeMillis();
 		double unprocessed = 0;
 		long ticks = 0;
 		if(level == null) level = new Level(512, 512);
 		
 		if(ply == null) {
 			ply = new Player("Player", level, level.getSpawnpoint(), input);
 		}
 		//ply.setLevelDelta(new Point(ply.getLevelDelta().x - screen.getWidth() / level.TILESIZE / 2,
 		//		ply.getLevelDelta().y - screen.getHeight() / level.TILESIZE / 2));
 		
 		// Main loop
 		while(true) {
 			// Limit tick rate
 			long now = System.nanoTime();
 			unprocessed += (now - lastTime) / nsPerTick;
 			lastTime = now;
 			while(unprocessed >= 1) {
 				// Poll for user input
 				input.poll();
 				ply.handleInput();
 				unprocessed--;
 				ticks++;
 			}
 			// Render
 			long rStart = System.nanoTime();
 			
 			Graphics g = bufStrategy.getDrawGraphics();
 
 			// Render the game
 			g.clearRect(0, 0, screen.getWidth(), screen.getHeight());
 			render(g, ply, ticks);
 			double rDiff = (System.nanoTime() - rStart) / 1000000.0;
 			String fpsString = ("FPS: " + (int)(1000.0/rDiff));
 			drawString(g, fpsString, 0, 0, Color.WHITE, Color.BLACK);
 			g.dispose();
 			bufStrategy.show();
 			if(System.currentTimeMillis() - lastLog > 1000) {
 				System.out.println(ticks + " ticks");
 				ticks = 0;
 				lastLog = System.currentTimeMillis();
 			}
 			try {
 				Thread.sleep(2);
 			}
 			catch(Exception e) {
 				
 			}
 		}
 	}
 
 	public void render(Graphics g, Player ply, long ticks) {
 		Graphics2D g2d = (Graphics2D)g;
 		Level level = ply.getLevel();
 		Point levelDelta = new Point(ply.getPosition().x - screen.getWidth() / level.TILESIZE / 2, 
 									  ply.getPosition().y - screen.getHeight() / level.TILESIZE / 2);
 		if(!(levelDelta.x == ply.getLevelDelta().x && levelDelta.y == ply.getLevelDelta().y)) {
 			ply.setLevelDelta(levelDelta);
 			level.setNeedsRedraw(true);
 		}
 		if(level != null) {
 			level.render(g, levelDelta.x, levelDelta.y, screen.getWidth() / level.TILESIZE, screen.getHeight() / level.TILESIZE);
 		}
 		// Render player's current tile
 			Point mPos = input.getMousePosition();
 			boolean canPlace = ply.canPlace(new Point(mPos.x / level.TILESIZE + levelDelta.x, mPos.y / level.TILESIZE + levelDelta.y));
 			// Round to the nearest TILESIZE
 			mPos.x = (mPos.x / level.TILESIZE) * level.TILESIZE;
 			mPos.y = (mPos.y / level.TILESIZE) * level.TILESIZE;
 			
 			BufferedImage im = null;
 			if(ply.isEditingFg()) {
 				im = ply.getCurrentFgTile().getTextureClone();
 			}
 			else {
 				im = ply.getCurrentBgTile().getTextureClone();
 			}
 			
 			if(im != null) {
 				Graphics2D im2d = (Graphics2D)im.getGraphics();
 				if(canPlace) {
 					im2d.setColor(new Color(0, 255, 0, 80));
 				}
 				else {
 					im2d.setColor(new Color(255, 0, 0, 80));
 				}
 				im2d.fillRect(0, 0, im.getWidth(), im.getHeight());
 				g2d.drawImage(im, null, mPos.x, mPos.y);
 				g2d.drawRect(mPos.x, mPos.y, level.TILESIZE, level.TILESIZE);
 			}
 		// Render player's sprite
 		g2d.drawImage(ply.getSprite(), null, (ply.getOffsetPosition().x) * level.TILESIZE, (ply.getOffsetPosition().y) * level.TILESIZE);
 		// Render player's name
 		int x = (int)((ply.getOffsetPosition().x + .5) * level.TILESIZE) - (int)getStringBounds(g, ply.getName()).getWidth() / 2;
 		int y = (ply.getOffsetPosition().y) * level.TILESIZE - (int)getStringBounds(g, ply.getName()).getHeight();
 		drawString(g, ply.getName(), x, y, Color.BLACK, Color.WHITE);
 	}
 
 	public Rectangle2D getStringBounds(Graphics g, String str) {
 		return g.getFontMetrics().getStringBounds(str, g);
 	}
 	
 	public void drawString(Graphics g, String str, int x, int y, Color fg, Color bg) {
 		FontMetrics fm = g.getFontMetrics();
 		Rectangle2D bounds = fm.getStringBounds(str, g);
 		g.setColor(bg);
 		g.fillRect(x, y, (int)bounds.getWidth(), (int)bounds.getHeight());
 		g.setColor(fg);
 		g.drawString(str, x, y + (int)bounds.getHeight());
 	}
 }
