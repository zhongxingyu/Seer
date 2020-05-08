 package com.jpii.navalbattle.game;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.*;
 import java.util.*;
 
 import com.jpii.dagen.*;
 import com.jpii.navalbattle.NavalBattle;
 import com.jpii.navalbattle.data.Constants;
 import com.jpii.navalbattle.game.entity.Entity;
 import com.jpii.navalbattle.renderer.*;
 
 /**
  * <b>THE</b> game.
  * @author MKirkby
  *
  */
 public class Game implements Runnable {
     private Grid grid;
     private ArrayList < ChunkRenderer > chunks;
     private Engine eng;
     private EntityRenderer eRender;
     private BufferedImage map;
     private BufferedImage buffer;
     private BufferedImage clouds;
     private BufferedImage shadow;
     //private int zoom;
     private CloudRelator cr;
     private int msax, msay;
     private Thread CHUNK_OVERHEAD;
     private OmniMap omniMap;
     FlyingRenderer bird0;
     Random simpleSeeder;
     @SuppressWarnings("unused")
 	private String timeStatus = "Night";
     /**
      * Creates <b>THE</b> game.
      */
     public Game() {
         msax = Constants.WINDOW_WIDTH * 2;
         msay = Constants.WINDOW_HEIGHT * 2;
         grid = new Grid();
         eRender = new EntityRenderer(grid);
         chunks = new ArrayList < ChunkRenderer > ();
         map = new BufferedImage(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT,
         BufferedImage.TYPE_INT_RGB);
         buffer = new BufferedImage(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT,
         BufferedImage.TYPE_INT_RGB);
        if (RenderConstants.OPT_CLOUDS_ON)
        	clouds = new BufferedImage(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT, BufferedImage.TYPE_INT_ARGB);
         eng = new Engine(Constants.WINDOW_WIDTH * 4,
         Constants.WINDOW_HEIGHT * 4);
         shadow = Helper.genInnerShadow(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
         eng.setSmoothFactor(5);
         eng.generate(Constants.MAIN_SEED, RenderConstants.GEN_TERRAIN_ROUGHNESS);
        eng.setWaterLevel(RenderConstants.GEN_WATER_HEIGHT);
        eng.reRunStats();
        while (eng.getStats().getPercentWater() < 70) {
        	Console.getInstance().printError("Seed " + Constants.MAIN_SEED + " has too much land ("
        			+ eng.getStats().getPercentWater() + "% water). Prepairing to regenerate.");
        	Constants.MAIN_SEED = (int)(Math.random() * Integer.MAX_VALUE);
        	eng.generate(Constants.MAIN_SEED, RenderConstants.GEN_TERRAIN_ROUGHNESS);
            eng.reRunStats();
        }
         simpleSeeder = new Random(Constants.MAIN_SEED);
         
         Console.getInstance().printError("Saving mode disabled. SaveMe API not functional.");
         
        /*try {
         for (int c = 0; c < 10; c++) {
         	Random rand = new Random(Constants.MAIN_SEED+c-23);
         	int ox = (rand.nextInt(eng.getWidth() - 200) + 300)/3;
         	int oy = (rand.nextInt(200))/3;//rand.nextInt(eng.getHeight() - 200) + 100;
     		double[][] points = new double[eng.getWidth()/3][eng.getHeight()/3];
     		points[ox][oy] = 1;
     		int lastx = ox;
     		int lasty = oy;
     		int ppx = 0;
     		int ppy = 0;
     		for (int v = 0; v < 200; v++)
     		{
     			int nx = lastx + rand.nextInt(5)-2;
     			int ny = lasty + rand.nextInt(2)+1;
     			if (nx >= eng.getWidth()/3)
     				nx = (eng.getWidth()/3)-1;
     			if (ny >= eng.getHeight()/3)
     				ny = (eng.getHeight()/3)-1;
     			if (nx < 1)
     				nx = 1;
     			if (ny < 1)
     				ny = 1;
     			if (ppx != 0 && ppy != 0 && Math.random() < 0.1)
     			{
     				ppx = nx;
     				ppy = ny;
     			}
     			points[nx][ny] = 1.0;
     			if (nx != lastx && ny != lasty) {
     			SimpleBrownian.drawLine(nx,ny,lastx,lasty,eng.getWidth()/3,eng.getHeight()/3,points);
     			SimpleBrownian.drawLine(nx-1,ny-1,lastx-1,lasty-1,eng.getWidth()/3,eng.getHeight()/3,points);
     			SimpleBrownian.drawLine(nx+1,ny+1,lastx+1,lasty+1,eng.getWidth()/3,eng.getHeight()/3,points);
     			SimpleBrownian.drawLine(nx-1,ny-1,lastx+1,lasty+1,eng.getWidth()/3,eng.getHeight()/3,points);
     			SimpleBrownian.drawLine(nx+1,ny+1,lastx-1,lasty-1,eng.getWidth(),eng.getHeight(),points);
     			}
     			lastx = nx;
     			lasty = ny;
     		}
     		for (int x = 0; x < eng.getWidth()/3; x++) {
     			for (int y = 0; y < eng.getHeight()/3; y++) {
     				if (points[x][y] == 1.0) {
     					if (eng.getPoint(x,y) >= RenderConstants.GEN_WATER_HEIGHT) {
     						eng.setPoint(x,y,(RenderConstants.GEN_WATER_HEIGHT - 0.3));
     					//eng.setPoint(x,y,1);
     					}
     				}
     			}
     		}
     		Runtime.getRuntime().gc();
         }
        }
        catch (Exception ex) {
        	
        }*/
         bird0 = new FlyingRenderer(this,simpleSeeder.nextInt(25)+75,450);
         
         NavalBattle.getDebugWindow().printInfo("Generated map. Size: " + (4*Constants.WINDOW_WIDTH/50) + "x" +
         (4*Constants.WINDOW_HEIGHT/50) + ". Used seed: " + Constants.MAIN_SEED);
         
         Console.getInstance().setSeed(Constants.MAIN_SEED);
         
         omniMap = new OmniMap(eng, 100, 100);
         omniMap.px = Constants.WINDOW_WIDTH - 120;
         omniMap.py = 5;
 
         repaint(RepaintType.REPAINT_OMNIMAP);
 
         if (RenderConstants.OPT_CLOUDS_ON) cr = new CloudRelator();
 
         for (int x = 0; x < Constants.WINDOW_WIDTH / Constants.CHUNK_SIZE; x++) {
             for (int z = 0; z < Constants.WINDOW_HEIGHT / Constants.CHUNK_SIZE; z++) {
                 ChunkRenderer cr = new ChunkRenderer(eng, Constants.MAIN_SEED, x, z,
                 Constants.CHUNK_SIZE, Constants.CHUNK_SIZE,
                 RenderConstants.GEN_TERRAIN_ROUGHNESS);
                 //cr.setState(ChunkState.STATE_GENERATE);
                 //cr.run();
                 if (x == 0 && z == 0)
                 	cr.setLighting(true);
                 cr.setState(ChunkState.STATE_RENDER);
                 cr.run();
                 chunks.add(cr);
             }
         }
         repaint(RepaintType.REPAINT_INDV_ENTITIES);
         
         Console.getInstance().printInfo("Generator finished. Game constructor finished.");
     }
     /**
      * Gets the game grid.
      * @return The grid.
      */
     public Grid getGrid() {
         return grid;
     }
     /**
      * Updates the game.
      */
     public void update() {
     	int le = RenderConstants.DAYNIGHT_LENGTH_IN_SECONDS;
     	if (RenderConstants.CURRENT_TIME_OF_DAY < le)
     		RenderConstants.CURRENT_TIME_OF_DAY += (le/1000.0);
     	else
     		RenderConstants.CURRENT_TIME_OF_DAY = 0;
     	
     	bird0.update();
     	
     	int alph = Helper.ComputeTime();
     	RenderConstants.TIME_OVERLAY = new BufferedImage(Constants.CHUNK_SIZE,Constants.CHUNK_SIZE,BufferedImage.TYPE_INT_ARGB);
     	Graphics timeO = RenderConstants.TIME_OVERLAY.getGraphics();
     	if (alph != 0)
     	{
     		timeO.setColor(new Color(0,0,0,alph));
     		timeO.fillRect(0,0,Constants.CHUNK_SIZE,Constants.CHUNK_SIZE);
     		timeStatus = "Night";
     	}
     	else
     		timeStatus = "Day";
     	setStatus(GameStatus.STATUS_CHUNK_RENDER);
     	//run();
     	boolean allowOverhead = false;
     	if (allowOverhead && (CHUNK_OVERHEAD == null || !CHUNK_OVERHEAD.isAlive() || CHUNK_OVERHEAD.getState() == Thread.State.TERMINATED)) {
 	    	CHUNK_OVERHEAD = new Thread(this);
 	    	CHUNK_OVERHEAD.setPriority(Thread.MAX_PRIORITY);
 	    	CHUNK_OVERHEAD.start();
     	}
     	else
     		run();
     	
     	setStatus(GameStatus.STATUS_CHUNK_UPDATES);
     	run();
         if (!RenderConstants.OPT_CLOUDS_ON) return;
         	cr.run();
        	
        Runtime.getRuntime().gc();
     }
     /**
      * Fired when the mouse moves.
      * @param me
      */
     public void mouseMoved(MouseEvent me) {
     	lastmx = me.getX();
     	lastmy = me.getY();
         if (RenderConstants.OPT_CLOUDS_ON)
         	cr.updateMouse(me.getX(), me.getY());
         omniMap.mouse(me);
         if (!omniMap.entireWorldMode)
         	omniMap.update();
         
         setStatus(GameStatus.STATUS_CHUNK_EVENTS);
         run();
     }
     /**
      * Fired when the mouse is clicked.
      * @param me The mouse event.
      */
     public void mouseClick(MouseEvent me) {
         omniMap.mouseClick(me);
         omniMap.update();
     }
     /**
      * Runs the OmniMap updater and chunk updator.
      */
     public void run() {
     	
     	if (getStatus() == GameStatus.STATUS_CHUNK_RENDER) {
     		omniMap.msax = msax;
     		omniMap.msay = msay;
     		if (omniMap.entireWorldMode)
     			omniMap.update();
     		repaint(RepaintType.REPAINT_CHUNKS);
     		repaint(RepaintType.REPAINT_MAP);
     		repaint(RepaintType.REPAINT_INDV_ENTITIES);
     		repaint(RepaintType.REPAINT_BUFFERS);
     	}
     	else if (getStatus() == GameStatus.STATUS_CHUNK_UPDATES) {
     		/*for (int x = msax; x < (Constants.WINDOW_WIDTH)+msax; x += 50) {
     			for (int y = msay; y < (Constants.WINDOW_WIDTH)+msay; y += 50) {
     				Location l = new Location(x,y);
     				if (isLocationInScreen(l)) {
     					Entity e = getGrid().getEntity(l.getCol(),l.getRow());
     					if (e != null) {
     						//e.invokeUpdate();
     					}
     				}
     				//Point p = pointToScreen(x,y);
     				//Location l = pointToGridLocation(p.x,p.y);
     				//this.
     				//if (Location.validate(l)) {
     					//Entity ti = getGrid().getEntity(l.getRow(),l.getCol());
     					//ti.invokeUpdate();
     				//}
     			}
     		}*/
     	}
     	else if (getStatus() == GameStatus.STATUS_CHUNK_EVENTS) {
     		Point mp = mouseToGrid();
     		Location l = new Location(mp.y,mp.x);
     		if (Location.validate(l)) {
     			Entity e = getGrid().getEntity(l.getCol(), l.getRow());
     			if (e != null) {
     				e.onMouseHover(0,0);
     			}
     		}
     		/*for (int x = 0; x < grid.getWidth(); x++) {
     			for (int y = 0; y < grid.getHeight(); y++) {
     				Entity ent = grid.getEntity(x, y);
     				//Location l = pointToGridLocation(lastmx,lastmy);
     				Location
     				if (l.getCol() == ent.getLocation().getCol() && l.getRow() == ent.getLocation().getRow())
     					ent.onMouseHover(lastmx,lastmy);
     			}
     		}*/
     	}
     }
     public void setStatus(GameStatus state) {
     	this.state = state;
     }
     public GameStatus getStatus() {
     	return state;
     }
     int lastmx = -1;
     int lastmy = -1;
     boolean mouseDown = false;
     GameStatus state;
 
     /**
      * Fired when the mouse is dragged. 
      */
     public void mouseDrag(MouseEvent me) {
         if (msax > -200 && msay > -200 && msax < (Constants.WINDOW_WIDTH * 4) - 600 && msay < (Constants.WINDOW_HEIGHT * 4) - 400) {
             if (!RenderConstants.OPT_INVERSE_MOUSE) {
                 msax += (me.getX() - (Constants.WINDOW_WIDTH / 2)) / 8;
                 msay += (me.getY() - (Constants.WINDOW_HEIGHT / 2)) / 8;
             } else {
                 msax -= (me.getX() - (Constants.WINDOW_WIDTH / 2)) / 8;
                 msay -= (me.getY() - (Constants.WINDOW_HEIGHT / 2)) / 8;
             }
         } else {
             if (msax <= -200) msax = -198;
             if (msay <= -200) msay = -198;
             if (msax >= (Constants.WINDOW_WIDTH * 4) - 600) msax = (Constants.WINDOW_WIDTH * 4) - 602;
             if (msay >= (Constants.WINDOW_HEIGHT * 4) - 400) msay = (Constants.WINDOW_HEIGHT * 4) - 402;
         }
         //run();
     }
     /**
      * Repaints the specified portion of the game.
      * @param type The area of the game to repaint. See RepaintType.java for more details.
      */
     public void repaint(RepaintType type) {
         if (type == RepaintType.REPAINT_BUFFERS) {
             buffer = new BufferedImage(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT,
             BufferedImage.TYPE_INT_RGB);
             Graphics2D g = (Graphics2D)buffer.getGraphics();
             g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
             g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
             g.drawImage(map, 0, 0, null);
             g.drawImage(eRender.getBuffer(), 0, 0, null);
             //g.drawImage(grid.getFeasibleGrid(),0,0,null);
             g.drawImage(clouds, 0, 0, null);
             bird0.draw(g);
             g.drawImage(shadow, 0, 0, null);
             g.drawImage(omniMap.getBuffer(), omniMap.px, omniMap.py, null);
         }
         if (type == RepaintType.REPAINT_CHUNKS) {
             for (int v = 0; v < chunks.size(); v++) {
                 ChunkRenderer cr = chunks.get(v);
                 cr.setLocation(msax, msay);
                 cr.setState(ChunkState.STATE_RENDER);
                 cr.run();
             }
         }
         if (type == RepaintType.REPAINT_MAP) {
             Graphics g = map.getGraphics();
             g.setColor(Color.black);
             g.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
             for (int v = 0; v < chunks.size(); v++) {
                 ChunkRenderer cr = chunks.get(v);
                 g.drawImage(cr.getChunkBuffer(), cr.getX() * Constants.CHUNK_SIZE, cr.getZ() * Constants.CHUNK_SIZE, null);
                 //g.drawImage(cr.getChunkBuffer(),0,0,null);
             }
         }
         if (type == RepaintType.REPAINT_INDV_ENTITIES) {
             eRender.render(this);
         }
         if (type == RepaintType.REPAINT_CLOUDS) {
             if (!RenderConstants.OPT_CLOUDS_ON) return;
             clouds = cr.buffer;
         }
         if (type == RepaintType.REPAINT_OMNIMAP) {
             omniMap.update();
         }
     }
     /**
      * Gets the game buffer.
      * @return A buffered image containg the game.
      */
     public BufferedImage getBuffer() {
         return buffer;
     }
     /**
      * Gets the current mouse coordinates. MAY NOT WORK.
      * @deprecated May not work.
      * @return
      */
     public Point getMouseSet() {
         return new Point(msax, msay);
     }
     
     /**
      * Converts a grid location to screen coordinates. MAY NOT WORK.
      * @param l The location to convert.
      * @param g The game.
      * @return A point on the screen (its possible that it might not be on the screen).
      * @deprecated May not work.
      */
     /*public Point gridLocationToScreen(Location l) {
         Point p = gridLocationToPoint(l);
         Point s = pointToScreen(p.x, p.y);
         return s;
     }*/
     /**
      * Converts a grid location into a point (not on screen, so it may not be useful at all). MAY NOT WORK.
      * @param l The location to convert.
      * @return The point in the world.
      * @deprecated May not work.
      */
     public Point gridLocationToPoint(Location l) {
         int px = l.getRow();
         int py = l.getCol();
         int x = px * Constants.CHUNK_SIZE;
         int y = py * Constants.CHUNK_SIZE;
         return new Point(x, y);
     }
     /**
      * Converts a point in the world, into a point in the screen. MAY NOT WORK.
      * @param px The x point in the world.
      * @param py The y point in the world.
      * @param g The game.
      * @deprecated May not work.
      * @return A point. (May not physically be on the screen, and may be negative.)
      */
     public Point pointToScreen(int px, int py) {
         Point p = getMouseSet();
         Point y = new Point((px / 2) - p.x, (py / 2) - p.y);
         return y;
     }
     
     /**
      * Gets the mouse point in the World. (A Mouse at (60,70) could return something like (660,670)). Works fine.
      * @return The mouse point in the World.
      */
     public Point mouseToPoint() {
     	Point y = new Point(msax+lastmx,msay+lastmy);
     	return y;
     }
     
     /**
      * Gets the location of the mouse in the grid. Should return a Location. Works fine.
      * @return The location of the mouse.
      */
     public Point mouseToGrid() {
     	Point world = mouseToPoint();
     	Point w = new Point(world.x/50,world.y/50);
     	return w;
     }
     
     /**
      * Converts a grid location, to a point on the screen. Works fine.
      * @param l The location to convert.
      * @return A point that may or may not actually be on the screen.
      */
     public Point gridLocationToScreen(Location l) {
     	return new Point((l.getRow() * (Constants.CHUNK_SIZE / 2)) - msax,(l.getCol() * (Constants.CHUNK_SIZE / 2)) - msay);
     }
     
    // public boolean isLocationInScreen(Location l) {
     //	return true;
     	/*
     	if (!Location.validate(l))
     		return false;
     	
     	int px = l.getCol() * Constants.CHUNK_SIZE / 2;
     	int py = l.getRow() * Constants.CHUNK_SIZE / 2;
     	px -= msax;
     	py -= msay;
     	if (px < Constants.WINDOW_WIDTH && py < Constants.WINDOW_HEIGHT && px > 0 && py > 0)
     		return true;
     	else
     		return false;
     		*/
     //}
 }
