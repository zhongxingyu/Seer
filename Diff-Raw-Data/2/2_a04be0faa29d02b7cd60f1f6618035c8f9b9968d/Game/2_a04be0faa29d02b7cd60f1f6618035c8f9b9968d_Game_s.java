 /*
  * Copyright (C) 2012 JPII and contributors
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.jpii.navalbattle.pavo;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Polygon;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 import java.io.Serializable;
 import java.util.Calendar;
 
 
 import com.jpii.navalbattle.NavalBattle;
 import com.jpii.navalbattle.game.NavalClient;
 import com.jpii.navalbattle.game.NavalServer;
 import com.jpii.navalbattle.pavo.grid.Entity;
 import com.jpii.navalbattle.pavo.grid.GridedEntityTileOrientation;
 import com.jpii.navalbattle.pavo.grid.Location;
 import com.jpii.navalbattle.pavo.grid.Tile;
 import com.jpii.navalbattle.pavo.gui.GridWindow;
 import com.jpii.navalbattle.pavo.gui.NewWindowManager;
 import com.jpii.navalbattle.pavo.gui.controls.PWindow;
 import com.jpii.navalbattle.pavo.io.PavoClient;
 import com.jpii.navalbattle.pavo.io.PavoImage;
 import com.jpii.navalbattle.pavo.io.PavoServer;
 import com.jpii.navalbattle.util.FileUtils;
 import com.jpii.navalbattle.util.GameStatistics;
 
 public class Game extends Renderable implements Runnable, Serializable {
 	private static final long serialVersionUID = 1L;
 	private Thread updator;
 	private Thread chunkrender;
 	private Thread renderer;
 	private boolean gameRunning = true;
 	private int state = 0;
 	private World world;
 	private long numUpdates = 0;
 	private boolean forceUpdate = false;
 	private int lastTime = -1;
 	private int lastw = 0, lasth = 0;
 	private NewWindowManager windowsnt;
 	private PavoImage shadow;
 	public static Game Instance;
 	private NavalClient client;
 	private PavoServer server;
 	private boolean isClient = false;
 	public static PavoSettings Settings = new PavoSettings();
 	
 	/**
 	 * Creates a new instance of the game.
 	 */
 	public Game(WorldSize ws) {
 		server = new NavalServer(this);
		System.out.println("Server status: " + server.start());
 		windowsnt = new NewWindowManager(this);
 		world = new World(this,ws);
 		threadInit();
 		buffer = new PavoImage(Game.Settings.currentWidth,Game.Settings.currentHeight,BufferedImage.TYPE_3BYTE_BGR);
 		shadow = (PavoImage)PavoHelper.createInnerShadow(Game.Settings.currentWidth,Game.Settings.currentHeight);
 		int yeart = Calendar.getInstance().get(Calendar.YEAR);
 		String years = Integer.toString(yeart);
 		yearf = Integer.parseInt(years.substring(0,2));
 		yearl = Integer.parseInt(years.substring(2));
 		Instance = this;
 		NavalBattle.getWindowHandler().getToasterManager().setDisplayTime(8000);
 		NavalBattle.getWindowHandler().getToasterManager().showToaster(
 				"Sucessfully started a server instance.\n\nYour IP address:" + server.getSelfIP());
 	}
 	
 	/**
 	 * Creates a new instance of the game with given
 	 * startup parameters.
 	 * @param pos The type of state to open the game
 	 * in.
 	 * @param flags The flags/parameters for the
 	 * <code>PavoOpenState</code>.
 	 */
 	public Game(WorldSize ws,PavoOpenState pos, String flags) {
 		if (pos == PavoOpenState.OPEN_SERVER) {
 			client = new NavalClient(this,flags);
 			System.out.println("Client status: " + client.start());
 			isClient = true;
 			//client.send("HELLO");
 			//client.send("HELLO");
 			//client.send("HELLO");
 			while (client.getSeed() == Long.MIN_VALUE) {
 				
 			}
 			akamaideli3242very();
 			Game.Settings.seed = client.getSeed();
 			NavalBattle.getWindowHandler().getToasterManager().setDisplayTime(8000);
 			NavalBattle.getWindowHandler().getToasterManager().showToaster("Sucessfully connected to the server.");
 		}
 		windowsnt = new NewWindowManager(this);
 		world = new World(this,ws);
 		threadInit();
 		buffer = new PavoImage(Game.Settings.currentWidth,Game.Settings.currentHeight,BufferedImage.TYPE_3BYTE_BGR);
 		shadow = (PavoImage)PavoHelper.createInnerShadow(Game.Settings.currentWidth,Game.Settings.currentHeight);
 		int yeart = Calendar.getInstance().get(Calendar.YEAR);
 		String years = Integer.toString(yeart);
 		yearf = Integer.parseInt(years.substring(0,2));
 		yearl = Integer.parseInt(years.substring(2));
 		Instance = this;
 	}
 	boolean isConnected = false;
 	boolean ranCheckup = false;
 	/**
 	 * Determines if currently connected to a client, or
 	 * to server.
 	 * @return
 	 */
 	public boolean isConnectedToClientOrServer() {
 		return isConnected;
 	}
 	
 	/**
 	 * No touching.
 	 */
 	public void akamaideli3242very() {
 		isConnected = true;
 	}
 	
 	/**
 	 * Gets the client (if the current workstation
 	 * is acting as a client).
 	 * 
 	 * Use <code></code>
 	 * @return
 	 */
 	public PavoClient getSelfClient() {
 		return client;
 	}
 	
 	/**
 	 * Gets the server (if the current workstation us
 	 * acting as a server).
 	 * 
 	 * Use <code></code>
 	 * @return
 	 */
 	public PavoServer getSelfServer() {
 		return server;
 	}
 	
 	/**
 	 * Determines whether the current workstation is
 	 * acting as a client or as a server.
 	 * 
 	 * @return true if the workstation is a client,
 	 * false if the workstation is a server.
 	 */
 	public boolean isAClient() {
 		return isClient;
 	}
 	
 	public void shutdown() {
 		if (isAClient())
 			this.getSelfClient().halt();
 		else
 			this.getSelfServer().stop();
 		
 		gameRunning = false;
 		try {
 			Thread.sleep(250);
 		}
 		catch (Throwable t)  {
 			
 		}
 	}
 	
 	/**
 	 * Gets the window manager for the Game.
 	 * @return
 	 */
 	public NewWindowManager getWindows() {
 		return windowsnt;
 	}
 	
 	/**
 	 * Gets the total number of updates that the updater has performed.
 	 * @return
 	 */
 	public long getNumUpdates() {
 		return numUpdates;
 	}
 	/**
 	 * Don't play with.
 	 */
 	private void threadInit() {
 		int js = 0;
 		if (Settings.OverClock)
 			js = Thread.MAX_PRIORITY;
 		else
 			js = Thread.NORM_PRIORITY;
 		updator = new Thread(this);
 		state = 1;
 		updator.setPriority(js);
 		updator.setName("updatorThread");
 		updator.setDaemon(true);
 		updator.start();
 		updator.setPriority(js);
 		long lastStart = System.currentTimeMillis();
 		while (lastStart + 500 > System.currentTimeMillis()) {
 			
 		}
 		chunkrender = new Thread(this);
 		state = 2;
 		chunkrender.setPriority(js);
 		chunkrender.setName("chunkGenThread");
 		chunkrender.setDaemon(true);
 		chunkrender.start();
 		chunkrender.setPriority(js);
 		lastStart = System.currentTimeMillis();
 		while (lastStart + 500 > System.currentTimeMillis()) {
 			
 		}
 		state = 4;
 		renderer = new Thread(this);
 		state = 5;
 		renderer.setPriority(js);
 		renderer.setName("worldRenderingThread");
 		renderer.setDaemon(true);
 		renderer.start();
 		renderer.setPriority(js);
 	}
 	
 	private static GameStatistics stats = new GameStatistics();
 	/**
 	 * The graphics statistics for the game.
 	 * @return
 	 */
 	public static GameStatistics getStats() {
 		return stats;
 	}
 	BufferedImage loadMotionImage = null;
 	boolean moveStandard = false;
 	/**
 	 * Immortal caller.
 	 */
 	public void run() {
 		if (state == 1) {
 			while (gameRunning) {
 				while (!forceUpdate) {
 					;;;
 				}
 				if (lastw != Settings.currentWidth || lasth != Settings.currentHeight) {
 					lastw = Settings.currentWidth;
 					lasth = Settings.currentHeight;
 					getWindows().$akafre();
 				}
 				numUpdates += 100;
 				forceUpdate = false;
 				long updateStart = System.currentTimeMillis();
 				
 				while (getWorld().isLocked()) {}
 				getWorld().lock();
 				getWorld().update();
 				getWorld().unlock();
 				TimeManager tim = getWorld().getTimeManager();
 				if (tim.getState() != lastTime) {
 					lastTime = tim.getState();
 					if (lastTime == 3) {
 						becomingDay();
 					}
 					else if (lastTime == 2) {
 						becomingSunrise();
 					}
 					else if (lastTime == 1) {
 						becomingNight();
 					}
 					else if (lastTime == 0) {
 						becomingSunset();
 					}
 				}
 				update();
 				long updateFinish = System.currentTimeMillis() - updateStart;
 				getStats().SmSK280K99(updateFinish);
 				if (!getStats().isGenerating() && !inStatement) {
 					inStatement = true;
 					try {
 						String name = "";
 						if (chunkrender != null)
 							name = chunkrender.getName();
 						chunkrender.interrupt();	
 						chunkrender = null;
 						System.gc();
 						System.out.println("Thread " + name + " is probably dead.");
 					}
 					catch (Throwable t) {
 						
 					}
 					for (int c = 0; c < 5; c++) {
 						System.gc();
 					}
 					getWorld().getEntityManager().gameDoneGenerating();
 					if (isAClient()) {
 						this.getSelfClient().send("Chunk gen has been complete!");
 					}
 					else {
 						this.getSelfServer().send("Chunk gen has been complete on the server!");
 					}
 				}
 			}
 		}
 		else if (state == 2) {
 			while (gameRunning && getWorld().hasMoreChunks()) {
 				if (getWorld().hasMoreChunks()) {
 					getWorld().genNextChunk();
 					long start = System.currentTimeMillis();
 					while (start + 150 > System.currentTimeMillis()) {
 						;;;
 					}
 				}
 				else {
 					break;
 				}
 			}
 			for (int c = 0; c < getWorld().getTotalChunks(); c++) {
 				Chunk chunk = getWorld().getChunk(c);
 				getWorld().getEntityManager().AQms03KampOQ9103nmJMs((chunk.getZ()*2), (chunk.getX()*2), chunk.water00);
 				getWorld().getEntityManager().AQms03KampOQ9103nmJMs((chunk.getZ()*2)+1, (chunk.getX()*2), chunk.water01);
 				getWorld().getEntityManager().AQms03KampOQ9103nmJMs((chunk.getZ()*2), (chunk.getX()*2)+1, chunk.water10);
 				getWorld().getEntityManager().AQms03KampOQ9103nmJMs((chunk.getZ()*2)+1, (chunk.getX()*2)+1, chunk.water11);
 			}
 			getWorld().getWorldStatus().NOTOUCH_930202894(0);
 			Game.getStats().SmKdn02nOaP(1);
 		}
 		else if (state == 4) {
 			while (gameRunning) {
 				long start = System.currentTimeMillis();
 				while (start + 250 > System.currentTimeMillis()) {
 					PavoHelper.threadSleep();
 				}
 			}
 		}
 		else if (state == 5) {
 			while (gameRunning && Settings.isUsingMultithreadedRenderer) {
 				while (getWorld().isLocked()) {
 				}
 				getWorld().lock();
 				getWorld().render();
 				getWorld().unlock();
 			}
 		}
 		System.out.println("Thread " + Thread.currentThread().getName() + " is prepairing to exit context.");
 		System.gc();
 	}
 	boolean inStatement = false;
 	public String getGenStatus() {
 		return "";
 	}
 	/**
 	 * Unknown
 	 * @return
 	 * @deprecated
 	 */
 	public int getGenAmount() {
 		return 1;
 	}
 	int lkw = 0, lkh = 0;
 	/**
 	 * Renders the Game.
 	 */
 	public void render() {
 		long sjan = System.currentTimeMillis();
 		if (lkw != Game.Settings.currentWidth || lkh != Game.Settings.currentHeight) {
 			buffer = new PavoImage(Game.Settings.currentWidth,Game.Settings.currentHeight,BufferedImage.TYPE_3BYTE_BGR);
 			lkw = Game.Settings.currentWidth;
 			lkh = Game.Settings.currentHeight;
 		}
 		
 		Graphics2D g = PavoHelper.createGraphics(buffer);
 		
 		while (getWorld().isLocked()) {
 			
 		}
 		
 		getWorld().lock();
 		if (!Settings.isUsingMultithreadedRenderer)
 			getWorld().render();
 //		g.translate(-640,-480);
 //		g.scale(1,0.75f);
 //		g.shear(0.45f,0);
 		g.drawImage(getWorld().getBuffer(),0,0,null);
 //		g.shear(-0.45f,0);
 //		g.scale(1,1.333333333333333f);
 //		g.translate(640,480);
 		g.drawImage(getWorld().getTimeManager().getBuffer(),0,0,null);
 		
 		GameStatistics gs = getStats();
 		g.setColor(Color.red);
 		g.drawString("Idling (should be low):" + gs.getDrawIdling() + ". Draw time:" + gs.getDrawTime() + " Live chunks:" + gs.getLiveChunks(),12,660);
 		g.drawString("Is generating? " + gs.isGenerating() + ". Total update time:" + gs.getUpdateTime()
 				+ ". Last render length:" + gs.getTotalUpdate() + ". Current network state: " + Game.Settings.currentNetworkState, 12,690);
 		getWorld().unlock();
 		
 		while (getWindows().isLocked()) {			
 		}
 		getWindows().lock();
 		if (gJsiw)
 			g.setXORMode(Color.yellow);
 		for (int c = 0; c < getWindows().size(); c++) {
 			PWindow gw = getWindows().get(c);
 			if (gw instanceof GridWindow && gw.isVisible()) {
 				GridWindow gr = (GridWindow)gw;
 				Location l = gr.getGridLocation();
 				if (l != null) {
 					Chunk chunk = PavoHelper.convertGridLocationToChunk(getWorld(), l);
 					if (chunk != null && PavoHelper.isChunkVisibleOnScreen(getWorld(), chunk)) {
 						g.setColor(Color.red);
 						int ssx = (getWorld().getScreenX())+(l.getCol()*50)+25;
 						int ssy = (getWorld().getScreenY())+(l.getRow()*50)+25;
 						int midx = gr.getLocX()+(gr.getWidth()/2);
 						int midy = gr.getLocY()+(gr.getHeight()/2);
 						if (Math.sqrt(Math.pow(ssx-midx,2)+Math.pow(ssy-midy,2)) <= gr.getDistanceConstraint()) {
 							Polygon p = new Polygon();
 							p.addPoint(ssx,ssy);
 							p.addPoint(midx-10,midy-10);
 							p.addPoint(midx+10,midy+10);
 							p.addPoint(ssx,ssy);
 							g.fillPolygon(p);
 							g.setColor(Color.black);
 							g.drawPolygon(p);
 						}
 					}
 				}
 			}
 		}
 		
 		int tol = 20;
 		if (motionEnt != null && motionEnt.readyForMove) {
 			Entity e = motionEnt;
 			
 			if (loadMotionImage == null) {
 				motionDest = PavoHelper.convertLocationToScreen(getWorld(),e.getLocation());
 			}
 			else {
 				Point p = PavoHelper.convertLocationToScreen(getWorld(),motionDestiny);
 				if ((p.x - tol < motionDest.x && p.x + tol > motionDest.x &&
 						p.y - tol < motionDest.y - tol && p.y + tol > motionDest.y)
 						|| (motionDest.x < 3 && motionDest.y < 3)) {
 					// its there!
 					motionEnt = null;
 					loadMotionImage = null;
 					motionDest = null;
 					e.moveTo(motionDestiny);
 					e = null;
 					motionDestiny = null;
 				}
 				else {
 					motionDest = movePointTowards(motionDest,p,2);
 					//motionDest = new Point(motionDest.x,motionDest.y+4);
 				}
 			}
 		}
 		
 		
 		if (motionEnt != null && motionEnt.readyForMove) {
 			Entity e = motionEnt;
 			if (e != null)
 				g.drawImage(this.loadMotionImage, motionDest.x,motionDest.y,null);
 		}
 		
 		if (PavoHelper.getCalculatedSystemSpeed() != SystemSpeed.CREEPER && 
 				PavoHelper.getCalculatedSystemSpeed() != SystemSpeed.TURTLE) {
 			g.drawImage(shadow,0,0,null);
 		}
 		g.drawImage(getWindows().getBuffer(),0,0,null);
 		g.dispose();
 		getWindows().unlock();
 		Game.getStats().sBm3ns02AKa99mqp392(System.currentTimeMillis() - sjan);
 	}
 	Entity motionEnt;
 	float motionSpeed = 2.35f;
 	Point motionDest = new Point(0,0);
 	Location motionDestiny = Location.Unknown;
 	public void onWorldChange() {
 		if (motionEnt != null) {
 			motionEnt.moveTo(motionDestiny);
 		}
 		motionEnt = null;
 		loadMotionImage = null;
 	}
 	
 	public boolean NOTOUCH_primitivesbeinginvoked() {
 		if (motionEnt == null) {
 			return false;
 		}
 		else
 			return true;
 	}
 	
 	public void setAnimatedMotion(Entity motionEntity,Location destiny) {
 		if (motionEnt != null) {
 			motionEnt.moveTo(motionDestiny);
 			motionEnt = null;
 			loadMotionImage = null;
 		}
 		
 		String w6 = motionEntity.imgLocation;
 		if (motionEntity.getCurrentOrientation() == GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM) {
 			w6 = w6.replace(".png", "_S.png");
 		}
 		motionEnt = motionEntity;
 		motionDestiny = destiny;
 		motionDest = PavoHelper.convertLocationToScreen(getWorld(),motionEntity.getLocation());
 		loadMotionImage = FileUtils.getImage(w6);
 		Graphics g = loadMotionImage.getGraphics();
 		Color copy = PavoHelper.changeAlpha(PavoHelper.convertByteToColor(motionEnt.getTeamColor()), 100);
 		for (int x = 0; x < loadMotionImage.getWidth(); x++) {
 			for (int y = 0; y < loadMotionImage.getHeight(); y++) {
 				int pixel = loadMotionImage.getRGB(x,y);
 				if((pixel>>24) != 0x00 ) {
 					g.setColor(copy);
 					g.drawLine(x,y,x,y);
 				}
 			}
 		}
 		g.dispose();
 	}
 	
 	public Point movePointTowards(Point a, Point b, int distance)
 	{
 	    /*Point vector = new Point(b.x - a.x, b.y - a.y);
 	    int length = (int)Math.sqrt(vector.x * vector.x + vector.y * vector.y);
 	    Point unitVector = new Point(vector.x / length, vector.y / length);
 	    return new Point(a.x + unitVector.x * distance, a.y + unitVector.y * distance);*/
 		//nextLocation = a + UnitVector(a, b) * stepSize
 		Point2D.Double vector = new Point2D.Double(b.x - a.x, b.y - a.y);
 		double length = Math.sqrt(vector.x * vector.x + vector.y * vector.y);
 		Point2D.Double unitVector = new Point2D.Double(vector.x / length, vector.y / length);
 		Point move = new Point((int)(a.x + unitVector.x * 3.9),(int)(a.y + unitVector.y * 3.9));
 		//move.translate(Math.sqrt(Math));
 		return move;
 	}
 	/**
 	 * Gets the active world for the Game.
 	 * @return
 	 */
 	public World getWorld() {
 		return world;
 	}
 	/**
 	 * Occurs when the game is going into sunset stage.
 	 */
 	public void becomingSunset() {
 		
 	}
 	/**
 	 * Occurs when the game is going into sunrise stage.
 	 */
 	public void becomingSunrise() {
 		
 	}
 	/**
 	 * Occurs when the game is going into night stage.
 	 */
 	public void becomingNight() {
 		
 	}
 	/**
 	 * Occurs when the game is going into day time stage.
 	 */
 	public void becomingDay() {
 		
 	}
 	/**
 	 * This method should be called sparingly (which means DO NOT OVER USE). This method is multithreaded, so it puts no stress on the calling thread.
 	 * This method is not actually deprecated, but it is called so to ensure that the above message is read.
 	 */
 	public void forceUpdate() {
 		forceUpdate = true;
 	}
 	
 	/**
 	 * Occurs when the mouse is moved.
 	 * @param me The mouse event for the motion.
 	 */
 	public void mouseMove(MouseEvent me) {
 		if (getWindows().mouseMove(me)) {
 			
 			return;
 		}
 		int chx = (-getWorld().getScreenX()) + me.getX();
 		int chy = (-getWorld().getScreenY()) + me.getY(); 
 		chx /= 50;
 		chy /= 50;
 		if (chx < PavoHelper.getGameWidth(getWorld().getWorldSize()) * 2 && chy < PavoHelper.getGameHeight(getWorld().getWorldSize()) * 2 &&
 		chx >= 0 && chy >= 0) {
 			Tile<Entity> e = (getWorld().getEntityManager().getTile(chy,chx));
 			if (e != null) {
 				int acuratex = (-getWorld().getScreenX()) + me.getX() - (chx*50);
 				int acuratey = (-getWorld().getScreenY()) + me.getY() - (chy*50);
 				Location l = e.getEntity().getLocation();
 				acuratex += (chx - l.getCol())*50;
 				acuratey += (chy - l.getRow())*50;
 				e.getEntity().onMouseMove(acuratex,acuratey);
 			}
 		}
 		lastmx = me.getX();
 		lastmy = me.getY();
 	}
 	
 	public void keyDown(KeyEvent ke) {
 		
 	}
 	
 	public void keyUp(KeyEvent ke) {
 		
 	}
 	
 	public void keyTyped(KeyEvent ke) {
 		
 	}
 	
 	/**
 	 * Occurs when the mouse wheel is changed.
 	 * @param mwe The mouse event for the
 	 * method.
 	 */
 	public void mouseWheelChange(MouseWheelEvent mwe) {
 		
 	}
 	int lastmx = 0,lastmy = 0;
 	int yearf = 0;
 	int yearl = 0;
 	boolean gJsiw = false;
 	public boolean guiUsedMouseDown = false, guiUsedMouseUp = false, guiUsedMouseDrag = false;
 	
 	/**
 	 * Occurs when a mouse button is pushed.
 	 * @param me The mouse event for the
 	 * indicator.
 	 */
 	public void mouseDown(MouseEvent me) {
 		guiUsedMouseDown = false;
 		if (getWindows().mouseDown(me)) {
 			guiUsedMouseDown = true;
 			return;
 		}
 		int chx = (-getWorld().getScreenX()) + lastmx;
 		int chy = (-getWorld().getScreenY()) + lastmy; 
 		chx /= 50;
 		chy /= 50;
 		if (chx < PavoHelper.getGameWidth(getWorld().getWorldSize()) * 2 && chy < PavoHelper.getGameHeight(getWorld().getWorldSize()) * 2 &&
 		chx >= 0 && chy >= 0) {
 			Tile<Entity> e = getWorld().getEntityManager().getTile(chy,chx);
 			if (e != null) {
 				int acuratex = (-getWorld().getScreenX()) + lastmx - (chx*50);
 				int acuratey = (-getWorld().getScreenY()) + lastmy - (chy*50);
 				Location l = e.getEntity().getLocation();
 				acuratex += (chx - l.getCol())*50;
 				acuratey += (chy - l.getRow())*50;
 				e.getEntity().onMouseDown(acuratex,acuratey,me.getButton() == MouseEvent.BUTTON1);
 			}
 		}
 		if (chx == yearf && chy == yearl) {
 			gJsiw = !gJsiw;
 		}
 	}
 	
 	/**
 	 * Occurs when a mouse button is released.
 	 * @param me The mouse event for the
 	 * indicator.
 	 */
 	public void mouseUp(MouseEvent me) {
 		guiUsedMouseUp = false;
 		if (getWindows().mouseUp(me)) {
 			guiUsedMouseUp = true;
 			return;
 		}
 	}
 	
 	/**
 	 * Occurs when the mouse is dragged.
 	 * @param me The mouse event for the motion.
 	 */
 	public void mouseDragged(MouseEvent me) {
 		guiUsedMouseDrag = false;
 		if (getWindows().mouseDragged(me)) {
 			guiUsedMouseDrag = true;
 			return;
 		}
 	}
 	
 	/**
 	 * Occurs when the Game is shutting down.
 	 */
 	public void onShutdown() {
 		
 	}
 }
