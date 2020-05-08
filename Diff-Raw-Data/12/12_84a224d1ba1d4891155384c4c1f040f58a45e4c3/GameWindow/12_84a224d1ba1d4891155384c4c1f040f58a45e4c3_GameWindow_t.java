 /**
  * @author Kiljacken (Emil Laurdisen)
  */
 package graphics;
 
 import java.applet.Applet;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.event.KeyEvent;
 import java.awt.image.RenderedImage;
 import java.io.File;
 import java.io.IOException;
 import javax.imageio.ImageIO;
 
 
 import blocks.BlockIdentifier;
 import blocks.bases.Block;
 import ui.ItemBar;
 import utils.KeyboardInput;
 import utils.MixedUtils;
 import utils.MouseInput;
 import utils.MouseWheelInput;
 
 /**
  * The core of Life as a Head
  * @version 0.2a
  */
 public class GameWindow extends Applet implements Runnable {
 	private static final long serialVersionUID = 1L;
 	static Thread t;
 	Image offscreenImage;
 	Graphics2D offscr;
 	BlockWorld world;
 	Character player;
 	KeyboardInput keyboard;
 	MouseInput mouse;
 	MouseWheelInput mouseWheel;
 	long startTime;
 	long start;
 	int currentFPS;
 	int FPS;
 	boolean printscreen; // Printscreen flag
 	ItemBar inv = new ItemBar(this); // Inventory bar
 	private boolean debug=false;
 	File savegameFolder;
 	File screenshotFolder;
 	
 	public void init() {
 		// Make required game folders
 		savegameFolder = MixedUtils.getApplicationData("life_as_a_head", "savegame");
 		screenshotFolder = MixedUtils.getApplicationData("life_as_a_head", "screenshots");
 		
 		// Here we initialize all our variables
 		keyboard = new KeyboardInput(); // Keyboard polling
 		mouse = new MouseInput(); // Mouse polling
 		mouseWheel = new MouseWheelInput(); // Mouse Wheel polling
 		startTime = System.currentTimeMillis()/1000;
 		start = System.currentTimeMillis();
 		world = new BlockWorld(1024, 64, this);
 		printscreen = false;
 	    
 	    // Set the window size
 	    setSize(800, 680);
 	    
 	    // Add keyboard listener
 	    addKeyListener(keyboard);
 	    
 	    // Add mouse listeners
 	    addMouseListener(mouse);
 	    addMouseMotionListener(mouse);
 	    
 	    // Add mouse listeners
 	    addMouseWheelListener(mouseWheel);
 		
 	    // Start player late, because he is a thread
 		player = new Character(this); // Our player
 		
 		// Initialize our thread
 		t = new Thread(this);
 	    t.start();
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Runnable#run()
 	 */
 	@Override
 	public void run() {
 		// Run 1 iteration of the graphics loop
 		while(true)
 		{
 			// Poll the keyboard
 			keyboard.poll();
 			
 			// Poll the mouse
 	        mouse.poll();
 	        
 	        // Delete / Add block on mouse click
 //	        Point3D hit = RayCaster.raytrace(player.getX()*25, player.getY()*25, world.mousePointsAt(mouse.getPosition(), player, 800, 600), world);
 	        Point hit = world.mousePointsAt(mouse.getPosition(), player, 800, 600);
 	        if (mouse.buttonDownOnce(1)) {
 	        	int dx = player.getX()-hit.x;
 	        	int dy = player.getY()-hit.y;
 	        	int distance = Math.abs((int) Math.round(Math.sqrt(dx*dx+dy*dy)));
 	        	
 	        	if (distance<=5) {
 //	        		Block drop = world.remove(hit.x+player.getX(), hit.y+player.getY());
 	        		Block drop = world.remove(hit.x, hit.y);
 	        		if (drop.id!=253) {
 	        			inv.addBlock(BlockIdentifier.toBlock(drop.dropId), 1);
 	        		}
 	        	}
 	        }
 	        if (mouse.buttonDownOnce(3)) {
 	        	// FIXME: Can't place blocks further into the world
 	        	int dx = player.getX()-hit.x;
 	        	int dy = player.getY()-hit.y;
 	        	int distance = Math.abs((int) Math.round(Math.sqrt(dx*dx+dy*dy)));
 
 	        	if (distance<=5) {
	        		if (world.get(hit.y, hit.x).id==253) {
 	        			Block blc=inv.removeBlock(inv.selected, 1);
 	        			if (blc.id!=253) {
 	        				boolean success = world.add(hit.x, hit.y, blc);
 	        				if (!success) {
 	        					inv.addBlock(blc, 1);
 	        				}
 	        			}
 	        		}
 //	        		System.out.println(hit.z);
 //	        		if (blc.id!=253) {
 //	        			System.out.println((hit.x+player.getX())+", "+(hit.y+player.getY()));
 //	        			if (hit.z==1) {
 //	        				if (world.get(hit.x+player.getX()-1, hit.y+player.getY()).id==253) {world.add(hit.x+player.getX()-1, hit.y+player.getY(), blc);}
 //	        			} else if (hit.z==2) {
 //	        				if (world.get(hit.x+player.getX(), hit.y+player.getY()-1).id==253) {world.add(hit.x+player.getX(), hit.y+player.getY()-1, blc);}
 //	        			} else if (hit.z==3) {
 //	        				if (world.get(hit.x+player.getX()+1, hit.y+player.getY()).id==253) {world.add(hit.x+player.getX()+1, hit.y+player.getY(), blc);}
 //	        			} else if (hit.z==4) {
 //	        				if (world.get(hit.x+player.getX(), hit.y+player.getY()+1).id==253) {world.add(hit.x+player.getX(), hit.y+player.getY()+1, blc);}
 //	        			} else {
 //	        				if (blc.id!=253) inv.addBlock(blc, 1);
 //	        			}
 //	        		}
 	        	}
 	        }
 	        
 	        if (keyboard.keyDownOnce(KeyEvent.VK_F1)) {
 	        	System.out.println("Saving game..");
 	        	world.save(savegameFolder, "world1");
 	        	player.save(savegameFolder.getAbsolutePath()+"player.nbt");
 	        	inv.save(savegameFolder.getAbsolutePath()+"inventory.nbt");
 	        }
 	        if (keyboard.keyDownOnce(KeyEvent.VK_F2)) {
 	        	try {
 	        		System.out.println("Loading game..");
 	        		world.load(savegameFolder, "world1");
 	        		player.load(savegameFolder.getAbsolutePath()+"player.nbt");
 	        		inv.load(savegameFolder.getAbsolutePath()+"inventory.nbt");
 	        	} catch (Throwable e) {
 	        		System.out.println("You currently have no saved game to load!");
 	        		e.printStackTrace();
 	        	}
 	        }
 	        if (keyboard.keyDownOnce(KeyEvent.VK_F3)) {
 	        	System.out.println("Initiating screen print..");
 	        	printscreen = true; // Screen will be printed in next frame
 	        }
 	        if (keyboard.keyDownOnce(KeyEvent.VK_F4)) {
 	        	System.out.println("Toggling debug..");
 	        	debug = !debug;
 	        }
 	        if (keyboard.keyDownOnce(KeyEvent.VK_N)) {
 	        	if (!player.noclip) {
 	        		player.noclip = true;
 	        	} else {
 	        		player.noclip = false;
 	        	}
 	        }
 	        if (keyboard.keyDownOnce(KeyEvent.VK_F)) {
 	        	if (!player.fly) {
 	        		player.fly = true;
 	        	} else {
 	        		player.fly = false;
 	        	}
 	        }
 	        if (keyboard.keyDownOnce(KeyEvent.VK_ESCAPE)) {
 	        	System.exit(0);
 	        }
 	        
 	      
 	        if (mouseWheel.change > 0) {
 	        	inv.select(inv.selected+1);
 	        	mouseWheel.reset();
 	        } else if (mouseWheel.change < 0) {
 	        	inv.select(inv.selected-1);
 	        	mouseWheel.reset();
 	        }
 			
 			// Redraw graphics
 			repaint();
 			
 			// Calculate FPS
 	        currentFPS++;
 	        if(System.currentTimeMillis() - start >= 1000) {
 	            FPS = currentFPS;
 	            currentFPS = 0;
 	            start = System.currentTimeMillis();
 	        }
 
 			try {
 				Thread.sleep(1000/60);
 			} catch (InterruptedException e) { ; }
 	    }
 	}
 
 	public void paint(Graphics g)
 	{
 		// Initialize our buffer
 		int width = this.getWidth();
 		int height = this.getHeight();
 		offscreenImage = createImage(width, height);
 		offscr = (Graphics2D) offscreenImage.getGraphics();
 		offscr.setColor(Color.white);
 		offscr.fillRect(0, 0, width, height);
 		
 		// Draw our world
 		world.draw(offscr, player, 800, 600);
 		
 		// Draw line from player to mouse
 		offscr.setColor(Color.cyan);
 		offscr.drawLine(world.px*25+13, world.py*25+13, mouse.getPosition().x, mouse.getPosition().y);
 		
 		// Mark block player looks at
 		offscr.setColor(Color.red);
 		//Point3D hit = RayCaster.raytrace(player.getX()*25, player.getY()*25, world.mousePointsAt(mouse.getPosition(), player, 800, 600), world);
 		Point hit = world.mousePointsAt(mouse.getPosition(), player, 800, 600);
 		if (!(hit.x==42) || !(hit.y==42)) {
 			offscr.drawRect((hit.x-world.x)*25-1, (hit.y-world.y)*25, 25, 25);
 //			offscr.drawRect((hit.x+world.px)*25-1, (hit.y+world.py)*25, 25, 25);
 		}
 		
 		// Draw debug info
 		offscr.setColor(Color.red);
 		offscr.drawString ("Life as a Head", 600, 20);
 		if (debug) {
 			offscr.drawString ("FPS: "+Integer.toString(FPS), 600, 40);
 			offscr.drawString ("Time since start: "+(System.currentTimeMillis()/1000-startTime), 600, 60);
 			offscr.drawString ("Player Position (x,y): "+player.getX()+","+player.getY(), 600, 80);
 			offscr.drawString ("Player Relative Position (x,y): "+world.px+","+world.py, 600, 100);
 			offscr.drawString ("World Pushed (x,y): "+world.x+","+world.y, 600, 120);
 		}
 		
 		// Draw the players inventory bar
 		inv.draw(offscr, 0, height-80);
 		
 		// Print the screen
 		if (printscreen) {
 			System.out.println("Printing screen..");
 			File file = new File(screenshotFolder.getAbsolutePath(), "laah-" + System.currentTimeMillis() + ".png");
 	        try {
 	            ImageIO.write((RenderedImage) offscreenImage, "png", file);  // ignore returned boolean
 	        } catch(IOException e) {
 	            System.out.println("Write error for " + file.getPath() +
 	                               ": " + e.getMessage());
 	        }
 	        printscreen = false;
 		}
 		
 		// Draw to screen
 		g.drawImage(offscreenImage, 0, 0, this);
 		
 		// Sync the window with window manager
 		getToolkit().sync();
 	}
 	
 	public void update(Graphics g)
 	{
 		paint(g);
 	}
 	
 	public Dimension getPreferredSize() {
 		return this.getSize();
 	}
 }
