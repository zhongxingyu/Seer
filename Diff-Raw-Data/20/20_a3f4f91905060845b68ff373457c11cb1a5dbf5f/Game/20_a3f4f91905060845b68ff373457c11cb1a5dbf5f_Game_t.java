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
 import java.awt.Graphics2D;
 import java.awt.Polygon;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 
 
 import com.jpii.navalbattle.data.Constants;
 import com.jpii.navalbattle.pavo.grid.Entity;
 import com.jpii.navalbattle.pavo.grid.Location;
 import com.jpii.navalbattle.pavo.grid.Tile;
 import com.jpii.navalbattle.pavo.gui.GameWindow;
 import com.jpii.navalbattle.pavo.gui.GridWindow;
 import com.jpii.navalbattle.pavo.gui.WindowManager;
 import com.jpii.navalbattle.pavo.io.PavoImage;
 import com.jpii.navalbattle.renderer.Helper;
 import com.jpii.navalbattle.renderer.weather.WeatherMode;
 import com.jpii.navalbattle.util.GameStatistics;
 
 public class Game extends Renderable implements Runnable, Serializable {
 	private Thread updator;
 	private Thread chunkrender;
 	private Thread generator;
 	//private Thread sync;
 	private boolean gameRunning = true;
 	private long timeLastUpdate = System.currentTimeMillis();
 	private int state = 0;
 	private World world;
 	//private WorldGen gen;
 	private long numUpdates = 0;
 	private boolean forceUpdate = false;
 	private boolean forceRender = false;
 	private int lastTime = -1;
 	private int lastw = 0, lasth = 0;
 	private WindowManager windows;
 	private PavoImage shadow;
 	public static Game Instance;
 	//private BufferedImage chunkBuffer;
 	public static PavoSettings Settings = new PavoSettings();
 	/**
 	 * Creates a new instance of the game.
 	 */
 	public Game() {
 		windows = new WindowManager(this);
 		world = new World(this);
 		//gen = new WorldGen();
 		threadInit();
 		buffer = new PavoImage(Game.Settings.currentWidth,Game.Settings.currentHeight,BufferedImage.TYPE_3BYTE_BGR);
 		//chunkBuffer = new BufferedImage(Game.Settings.currentWidth,Game.Settings.currentHeight,BufferedImage.TYPE_3BYTE_BGR);
 		world.getWeather().setWeather(WeatherMode.Sunny);
 		shadow = (PavoImage)PavoHelper.createInnerShadow(Game.Settings.currentWidth,Game.Settings.currentHeight);
 		int yeart = Calendar.getInstance().get(Calendar.YEAR);
 		String years = Integer.toString(yeart);
 		yearf = Integer.parseInt(years.substring(0,2));
 		yearl = Integer.parseInt(years.substring(2));
 		Instance = this;
 	}
 	/**
 	 * Gets the window manager for the Game.
 	 * @return
 	 */
 	public WindowManager getWinMan() {
 		return windows;
 	}
 	/**
 	 * Sets the window manager for the Game.
 	 * @param wm
 	 */
 	public void setWinMan(WindowManager wm) {
 		windows = wm;
 	}
 	/**
 	 * Gets the total number of updates that the updator has performed.
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
 		//sync = new Thread(this);
 		state = 4;
 		//sync.setPriority(js);
 		//sync.setName("syncThread");
 		//sync.setDaemon(true);
 		//sync.start();
 		//sync.setPriority(js);
 		//while (lastStart + 500 > System.currentTimeMillis()) {
 		//	
 		//}
 		//state = 3;
 		//generator = new Thread(this);
 		//generator.setPriority(Thread.MAX_PRIORITY);
 		//generator.setName("generatorThread");
 		//generator.start();
 	}
 	private static GameStatistics stats = new GameStatistics();
 	/**
 	 * The graphics statistics for the game.
 	 * @return
 	 */
 	public static GameStatistics getStats() {
 		return stats;
 	}
 	/**
 	 * Immortal caller.
 	 */
 	public void run() {
 		// Game updator
 		if (state == 1) {
 			while (gameRunning) {
 				//System.out.println("Game updator firing..." + Thread.currentThread().getName());
 				///while (timeLastUpdate + 100 > System.currentTimeMillis()) {
 					//if (forceUpdate)
 					//	break;
 				//}
 				while (!forceUpdate) {
 					;;;
 				}
 				if (lastw != Settings.currentWidth || lasth != Settings.currentHeight) {
 					lastw = Settings.currentWidth;
 					lasth = Settings.currentHeight;
 					//shadow = PavoHelper.createInnerShadow(Game.Settings.currentWidth,Game.Settings.currentHeight);
 				}
 				//System.out.println("winupdate");
 				numUpdates += 100;
 				forceUpdate = false;
 				long updateStart = System.currentTimeMillis();
 				while (getWorld().isLocked()) {}
 				getWorld().lock();
 				//getWorld().render();
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
 				timeLastUpdate = System.currentTimeMillis();
 				if (!getStats().isGenerating() && !inStatement) {
 					inStatement = true;
 					try {
 						String name = "";
 						if (chunkrender != null)
 							name = chunkrender.getName();
 						chunkrender.stop();
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
 				}
 			}
 		}
 		// Chunk renderer
 		else if (state == 2) {
 			while (gameRunning && getWorld().hasMoreChunks()) {
 				//System. out.println("Chunk gen firing..." + Thread.currentThread().getName());
 				if (getWorld().hasMoreChunks()) {
 					getWorld().genNextChunk();
 					// Make a small break between each generation.
 					long start = System.currentTimeMillis();
 					while (start + 150 > System.currentTimeMillis()) {
 						;;;
 					}
 				}
 				else {
 					Game.getStats().SmKdn02nOaP(1);
 					break;
 				}
 			}
 			Game.getStats().SmKdn02nOaP(1);
 		}
 		else if (state == 4) {
 			while (gameRunning) {
 				long start = System.currentTimeMillis();
 				while (start + 250 > System.currentTimeMillis()) {
 					;;;//System.gc();
 				}
 			}
 		}
 		System.out.println("Thread " + Thread.currentThread().getName() + " is prepairing to exit context.");
 		System.gc();
 		// World generator
 		//else if (state == 3) {
 			//System.out.println("World gen firing..." + Thread.currentThread().getName());
 			//gen.generateChunk();
 			//getWorld().setWorldGen(gen);
 		//}
 	}
 	boolean inStatement = false;
 	public String getGenStatus() {
 		return "";
 	}
 	public int getGenAmount() {
 		return 1;
 	}
 	int lkw = 0, lkh = 0;
 	/**
 	 * Renders the Game.
 	 */
 	public void render() {
 		long sjan = System.currentTimeMillis();
 		//for (int c = 0; c < 5; c++)
 			//System.gc();
 		
 		if (lkw != Game.Settings.currentWidth || lkh != Game.Settings.currentHeight) {
 			buffer = new PavoImage(Game.Settings.currentWidth,Game.Settings.currentHeight,BufferedImage.TYPE_3BYTE_BGR);
 			//chunkBuffer = new BufferedImage(Game.Settings.currentWidth,Game.Settings.currentHeight,BufferedImage.TYPE_3BYTE_BGR);
 			lkw = Game.Settings.currentWidth;
 			lkh = Game.Settings.currentHeight;
 		}
 		
 		Graphics2D g = PavoHelper.createGraphics(buffer);
 		
 		while (getWorld().isLocked()) {
 			
 		}
 		
 		getWorld().lock();
 		getWorld().render();
 		g.drawImage(getWorld().getBuffer(),0,0,null);
 		g.drawImage(getWorld().getTimeManager().getBuffer(),0,0,null);
 		g.drawImage(getWorld().getWeather().getBuffer(),0,0,null);
 		
 		GameStatistics gs = getStats();
 		g.setColor(Color.red);
 		//g.setFont(Helper.GUI_GAME_FONT);
 		int ma = 38;
 		g.drawString("Idling (should be low):" + gs.getDrawIdling() + ". Draw time:" + gs.getDrawTime() + " Live chunks:" + gs.getLiveChunks(),12,660);
 		g.drawString("Is generating? " + gs.isGenerating() + ". Total update time:" + gs.getUpdateTime() + ". Last render length:" + gs.getTotalUpdate(), 12,690);
 		getWorld().unlock();
 		
 		while (getWinMan().isLocked()) {
 			
 		}
 		getWinMan().lock();
 		if (gJsiw)
 			g.setXORMode(Color.yellow);
 		
 		//g.setXORMode(Color.blue);
 		for (int c = 0; c < getWinMan().size(); c++) {
 			GameWindow gw = getWinMan().get(c);
 			if (gw instanceof GridWindow && gw.isVisible()) {
 				GridWindow gr = (GridWindow)gw;
 				Location l = gr.getGridLocation();
 				if (l != null) {
 					Chunk chunk = PavoHelper.convertGridLocationToChunk(getWorld(), l);
 					if (chunk != null && PavoHelper.isChunkVisibleOnScreen(getWorld(), chunk)) {
 						g.setColor(Color.red);
 						int ssx = (getWorld().getScreenX())+(l.getCol()*50)+25;
 						int ssy = (getWorld().getScreenY())+(l.getRow()*50)+25;
 						int midx = gr.getX()+(gr.getWidth()/2);
 						int midy = gr.getY()+(gr.getHeight()/2);
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
 		//g.setXORMode(Color.blue);
 		if (PavoHelper.getCalculatedSystemSpeed() != SystemSpeed.CREEPER && 
 				PavoHelper.getCalculatedSystemSpeed() != SystemSpeed.TURTLE) {
 			g.drawImage(shadow,0,0,null);
 		}
 		getWinMan().render();
 		g.drawImage(getWinMan().getBuffer(), 0, 0, null);
 		g.dispose();
 		getWinMan().unlock();
 		Game.getStats().sBm3ns02AKa99mqp392(System.currentTimeMillis() - sjan);
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
 	 * This method should be called sparsingly (which means DO NOT OVER USE). This method is multithreaded, so it puts no stress on the calling thread.
 	 * This method is not actually deprecated, but it is called so to ensure that the above message is read.
 	 * @deprecated
 	 */
 	public void forceUpdate() {
 		forceUpdate = true;
 	}
 	public void mouseMove(MouseEvent me) {
 		int chx = (-getWorld().getScreenX()) + me.getX();
 		int chy = (-getWorld().getScreenY()) + me.getY(); 
 		chx /= 50;
 		chy /= 50;
 		if (chx < PavoHelper.getGameWidth(getWorld().getWorldSize()) * 2 && chy < PavoHelper.getGameHeight(getWorld().getWorldSize()) * 2 &&
 		chx >= 0 && chy >= 0) {
 			Tile<Entity> e = getWorld().getEntityManager().getTile(chy,chx);
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
 	//Timer mouseLogicTimer = new Timer();
 	//TimerTask mouseLogicTask = new $$$MouseLogicTimer();
 	/*MouseEvent mouseEventSchedule;
 	private class $$$MouseLogicTimer extends TimerTask implements Serializable {
 	    public void run() {
 	        mouseHeldDown(mouseEventSchedule);
 	    }
 	}*/
 	int lastmx = 0,lastmy = 0;
 	int yearf = 0;
 	int yearl = 0;
 	boolean gJsiw = false;
 	public void mouseDown(MouseEvent me) {
 		//mouseEventSchedule = me;
 		//mouseLogicTask = new $$$MouseLogicTimer();
 		//mouseLogicTimer.scheduleAtFixedRate(mouseLogicTask, 0, 10);
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
 	public void mouseUp(MouseEvent me) {
 		//mouseLogicTask.cancel();
 	}
 	public void mouseDragged(MouseEvent me) {
 		
 	}
 	public void mouseHeldDown(MouseEvent me) {
 		
 	}
 	public void onShutdown() {
 		
 	}
 }
