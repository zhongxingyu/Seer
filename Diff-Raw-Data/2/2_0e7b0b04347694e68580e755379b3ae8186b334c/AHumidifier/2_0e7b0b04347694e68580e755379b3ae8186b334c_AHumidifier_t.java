 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.geom.RoundRectangle2D;
 import java.awt.image.ImageObserver;
 import java.io.IOException;
 import java.net.URL;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 
 import org.rsbot.event.listeners.PaintListener;
 import org.rsbot.script.GEItemInfo;
 import org.rsbot.script.Script;
 import org.rsbot.script.ScriptManifest;
 import org.rsbot.script.Skills;
 import org.rsbot.script.wrappers.RSInterfaceChild;
 
 /**
  * A. Humidifier (Allometry Humidifier)
  * 
  * This script is designed for RuneDev and is intended for filling vials with
  * the lunar humidify spell at any bank, including the grand exchange.
  * 
  * Before starting this script, ensure that your bank is currently showing both
  * empty and filled vials on the same tab. This script will not look for your
  * bank items!
  * 
  * In addition, it is recommended that you sell your vials at or above market
  * price. This script is capable of fluctuating market prices and a steep
  * decrease in filled vial price would make this script non-profitable.
  * 
  * Copyright (c) 2010
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  * 
  * @author allometry
  * @version 1.0
  * @since 0.1
  */
 
 @ScriptManifest(authors = { "Allometry" }, category = "Magic", name = "A. Humidifier", version = 1.0,
 		description = "" +
 				"<html>" +
 				"<head>" +
 				"<style type=\"text/css\">" +
 				"body {background: #000 url(http://scripts.allometry.com/app/webroot/img/gui/window.jpg);" +
 				"font-family: Georgia, 'Times New Roman', Times, serif;" +
 				"font-size: 12px;font-weight: normal;" +
 				"padding: 50px 10px 45px 10px;}" +
 				"</style>" +
 				"</head>" +
 				"<body>" +
 				"<p>Allometry Humidifier</p>" +
 				"<p>Supports Fire, Water and Steam staffs</p>" +
 				"<p>Astrals in inventory. Empty vials visible in bank!</p>" +
 				"<p>For more info, visit the" +
 				"thread on the RuneDev forums!</p>" +
 				"</body>" +
 				"</html>")
 public class AHumidifier extends Script implements PaintListener {
 	private boolean isVerbose = true;
 	private boolean hasFireStaff = false, hasSteamStaff = false, hasWaterStaff = false;
 	private boolean isCameraRotating = false, isScriptLoaded = false, isThreadsRunning = true;
 	
 	private double grossProduct, grossCost, netProduct;
 	
 	private int emptyVialID = 229, filledVialID = 227;
 	private int astralRuneID = 9075, fireRuneID = 554, waterRuneID = 555;
 	private int fireStaffID = 1387, steamStaffID = 11736, waterStaffID = 1383;
 	private int astralRuneMarketPrice = 0, emptyVialMarketPrice = 0, filledVialMarketPrice = 0;
 	private int accumulatedHumidifyCasts = 0, accumulatedFilledVials = 0;
 	private int startingMagicEP = 0, startingMagicLevel = 0, currentMagicEP = 0, currentMagicLevel = 0;
 	private int currentGrossProductWidgetIndex = 0, currentGrossCostWidgetIndex = 0, currentNetProductWidgetIndex = 0;
 	private int humidifyCastsWidgetIndex = 0, vialsFilledWidgetIndex = 0;
 	private int currentRuntimeWidgetIndex = 0, magicEPEarnedWidgetIndex = 0;
 	
 	private long startingTime = 0, failsafeTimeout = 0;
 	
 	private Antiban antiban = new Antiban();
 	
 	private Image coinsImage, coinsAddImage, coinsDeleteImage, cursorImage, drinkImage, sumImage, timeImage, weatherImage;
 	private ImageObserver observer;
 	
 	private Monitor monitor = new Monitor();
 	
 	private NumberFormat numberFormatter = NumberFormat.getNumberInstance(Locale.US);
 	
 	private Scoreboard bottomLeftScoreboard, topLeftScoreboard, topRightScoreboard;
 	
 	private ScoreboardWidget currentGrossProduct, currentGrossCost, currentNetProduct;
 	private ScoreboardWidget humidifyCasts, vialsFilled;
 	private ScoreboardWidget currentRuntime, magicEPEarned;
 	
 	private String magicEPEarnedWidgetText = "";
 	
 	private Thread antibanThread, monitorThread;
 	
 	@Override
 	public boolean onStart(Map<String,String> args) {
 		try {
 			log.info("Attempting to read image resources from the web...");
 			
 			coinsImage = ImageIO.read(new URL("http://scripts.allometry.com/icons/coins.png"));
 			coinsAddImage = ImageIO.read(new URL("http://scripts.allometry.com/icons/coins_add.png"));
 			coinsDeleteImage = ImageIO.read(new URL("http://scripts.allometry.com/icons/coins_delete.png"));
 			cursorImage = ImageIO.read(new URL("http://scripts.allometry.com/app/webroot/img/cursors/cursor-01.png"));
 			drinkImage = ImageIO.read(new URL("http://scripts.allometry.com/icons/drink.png"));
 			sumImage = ImageIO.read(new URL("http://scripts.allometry.com/icons/sum.png"));
 			timeImage = ImageIO.read(new URL("http://scripts.allometry.com/icons/time.png"));
 			weatherImage = ImageIO.read(new URL("http://scripts.allometry.com/icons/weather_rain.png"));
 			
 			log.info("Success! All image resources have been loaded...");
 		} catch (IOException e) {
 			log.warning("There was an issue trying to read the image resources from the web...");
 		}
 		
 		try {
 			log.info("Attempting to get the latest market prices...");
 			
 			GEItemInfo astralRuneItem = grandExchange.loadItemInfo(astralRuneID);
 			GEItemInfo emptyVialItem = grandExchange.loadItemInfo(emptyVialID);
 			GEItemInfo filledVialItem = grandExchange.loadItemInfo(filledVialID);
 			
 			astralRuneMarketPrice = astralRuneItem.getMarketPrice();
 			emptyVialMarketPrice = emptyVialItem.getMarketPrice();
 			filledVialMarketPrice = filledVialItem.getMarketPrice();
 			
 			log.info("Success! The astral rune is " + astralRuneMarketPrice + "gp");
 			log.info("Success! The empty vial price is " + emptyVialMarketPrice + "gp");
 			log.info("Success! The filled vial price is " + filledVialMarketPrice + "gp");
 		} catch (Exception e) {
 			log.warning("There was an issue trying to read the filled vial price from the web...");
 		}
 		
 		try {
 			failsafeTimeout = System.currentTimeMillis() + 5000;
 			while(getCurrentTab() != TAB_EQUIPMENT && System.currentTimeMillis() < failsafeTimeout) {
 				openTab(TAB_EQUIPMENT);
 				wait(1000);
 			}
 			
 			if(getCurrentTab() == TAB_EQUIPMENT)
 				if(equipmentContains(fireStaffID))
 					hasFireStaff = true;
 				if(equipmentContains(steamStaffID))
 					hasSteamStaff = true;
 				if(equipmentContains(waterStaffID))
 					hasWaterStaff = true;			
 		} catch (Exception e) {
 			log.warning("There was an issue checking for player equpiment...");
 		}
 		
 		try {
 			//Assemble Bottom Left Widgets
 			currentGrossProduct = new ScoreboardWidget(coinsAddImage, "");
 			currentGrossCost = new ScoreboardWidget(coinsDeleteImage, "");
 			currentNetProduct = new ScoreboardWidget(coinsImage, "");
 			
 			//Assemble Top Left Widgets
 			humidifyCasts = new ScoreboardWidget(weatherImage, "");
 			vialsFilled = new ScoreboardWidget(drinkImage, "");
 			
 			//Assemble Top Right Widgets 
 			currentRuntime = new ScoreboardWidget(timeImage, "");
 			magicEPEarned = new ScoreboardWidget(sumImage, "");
 			
 			//Assemble Bottom Left Scoreboard
 			bottomLeftScoreboard = new Scoreboard(Scoreboard.BOTTOM_LEFT, 128, 5);		
 			bottomLeftScoreboard.addWidget(currentGrossProduct);
 			currentGrossProductWidgetIndex = 0;
 			
 			bottomLeftScoreboard.addWidget(currentGrossCost);
 			currentGrossCostWidgetIndex = 1;
 			
 			bottomLeftScoreboard.addWidget(currentNetProduct);
 			currentNetProductWidgetIndex = 2;
 			
 			//Assemble Top Left Scoreboard
 			topLeftScoreboard = new Scoreboard(Scoreboard.TOP_LEFT, 128, 5);
 			topLeftScoreboard.addWidget(humidifyCasts);
 			humidifyCastsWidgetIndex = 0;
 			
 			topLeftScoreboard.addWidget(vialsFilled);
 			vialsFilledWidgetIndex = 1;
 			
 			//Assemble Top Right Scoreboard
 			topRightScoreboard = new Scoreboard(Scoreboard.TOP_RIGHT, 128, 5);
 			topRightScoreboard.addWidget(currentRuntime);
 			currentRuntimeWidgetIndex = 0;
 			
 			topRightScoreboard.addWidget(magicEPEarned);
 			magicEPEarnedWidgetIndex = 1;
 		} catch (Exception e) {
 			log.warning("There was an issue creating the scoreboard...");
 		}
 		
 		try {
 			startingMagicEP = skills.getCurrentSkillExp(Skills.getStatIndex("Magic"));
 			startingMagicLevel = skills.getCurrSkillLevel(Skills.getStatIndex("Magic"));
 			startingTime = System.currentTimeMillis();
 		} catch (Exception e) {
 			log.warning("There was an issue instantiating some or all objects...");
 		}
 		
 		antibanThread = new Thread(antiban);
 		monitorThread = new Thread(monitor);
 		
 		antibanThread.start();
 		monitorThread.start();
 		
 		isScriptLoaded = true;
 		
 		return true;
 	}
 	
 	@Override
 	public int loop() {
 		if(isPaused || isCameraRotating || !isLoggedIn() || isWelcomeScreen() || isLoginScreen()) return 1;
 		
 		calculateStatistics();
 		
 		if(!canCastHumidify())
 			stopScript(true);
 		
 		if(inventoryEmptyExcept(astralRuneID)) {
 			verbose("#01 Inventory only contains astral runes. We're going to get empty vials from the bank!");
 			
 			verbose("#02 Opening inventory tab...");
 			failsafeTimeout = System.currentTimeMillis() + 5000;
 			do {
 				openTab(TAB_INVENTORY);
 			} while(getCurrentTab() != TAB_INVENTORY && System.currentTimeMillis() < failsafeTimeout);
 			verbose("#03 Inventory tab " + ((getCurrentTab() == TAB_INVENTORY) ? "opened" : "didn't open") + "!");
 			
 			verbose("#04 Moving to open the bank...");
 			failsafeTimeout = System.currentTimeMillis() + 5000;
 			do {
 				bank.open(true);
 				wait(500);
			} while(!bank.isOpen() && System.currentTimeMillis() < failsafeTimeout);
 			verbose("#05 Bank " + ((bank.isOpen()) ? "is open" : "didn't open") + "!");
 			
 			if(bank.isOpen()) {
 				verbose("#06 Withdrawing empty vials...");
 				failsafeTimeout = System.currentTimeMillis() + 5000;				
 				while(!isInventoryFull() && System.currentTimeMillis() < failsafeTimeout) {
 					if(bank.getCount(emptyVialID) <= getInventoryCount() && bank.getCount(emptyVialID) > 1) {
 						bank.withdraw(emptyVialID, bank.getCount(emptyVialID) - 1);
 						wait(random(700, 1000));
 					} else if(bank.getCount(emptyVialID) <= 1) {
 						stopScript(true);
 					} else {
 						bank.withdraw(emptyVialID, 0);
 						wait(random(700, 1000));
 					}
 				}
 			}
 			
 			return 1;
 		}
 		
 		if(getInventoryCount(emptyVialID) > 0) {
 			if(bank.isOpen()) bank.close();
 			
 			int emptyVialsInventory = getInventoryCount(emptyVialID);
 
 			verbose("#07 Opening magic tab...");
 			failsafeTimeout = System.currentTimeMillis() + 10000;
 			openTab(TAB_MAGIC);
 			do {
 				wait(1);
 			} while(getCurrentTab() != TAB_MAGIC && System.currentTimeMillis() < failsafeTimeout);
 			verbose("#08 Magic tab " + ((getCurrentTab() == TAB_MAGIC) ? "opened" : "didn't open") + "!");
 			
 			if(getCurrentTab() == TAB_MAGIC) {
 				RSInterfaceChild humidifyInterface = getInterface(430, 29);
 				
 				verbose("#09 Moving mouse to cast humidify...");
 				failsafeTimeout = System.currentTimeMillis() + 5000;
 				moveMouse(humidifyInterface.getAbsoluteX() + random(4, 8), humidifyInterface.getAbsoluteY() + random(4, 8));
 				do {
 					wait(1);
 				} while(!isMouseInArea(humidifyInterface.getArea()) && System.currentTimeMillis() < failsafeTimeout);
 				verbose("#10 Mouse is hovering over the humidfy spell!");
 				
 				if(isMouseInArea(humidifyInterface.getArea())) {
 					verbose("#11 Casting spell...");
 					if(atInterface(humidifyInterface)) {
 						verbose("#12 Bot says it casted the spell...");
 						
 						verbose("#13 Waiting for spell to succeed...");
 						failsafeTimeout = System.currentTimeMillis() + 5000;
 						do {
 							wait(1);
 						} while(getInventoryCount(filledVialID) != emptyVialsInventory && System.currentTimeMillis() < failsafeTimeout);
 						
 						if(getInventoryCount(filledVialID) == emptyVialsInventory) {
 							verbose("#14 Spell succeeded!");
 							accumulatedFilledVials += getInventoryCount(filledVialID);
 							accumulatedHumidifyCasts++;
 						} else {
 							verbose("#15 Spell failed!");
 						}
 					}
 				}
 			}
 			return 1;
 		}
 		
 		if(isInventoryFull()) {
 			verbose("#16 Opening inventory tab...");
 			failsafeTimeout = System.currentTimeMillis() + 5000;
 			do {
 				openTab(TAB_INVENTORY);
 			} while(getCurrentTab() != TAB_INVENTORY && System.currentTimeMillis() < failsafeTimeout);
 			verbose("#17 Inventory tab " + ((getCurrentTab() == TAB_INVENTORY) ? "opened" : "didn't open") + "!");
 			
 			verbose("#18 Moving to open the bank...");
 			failsafeTimeout = System.currentTimeMillis() + 5000;
 			do {
 				bank.open(true);
 				wait(500);
 			} while(!bank.isOpen() && System.currentTimeMillis() < failsafeTimeout);
 			verbose("#19 Bank " + ((bank.isOpen()) ? "is open" : "didn't open") + "!");
 			
 			if(bank.isOpen()) {
 				verbose("#20 Inventory is full of filled vials, banking everything except runes...");
 				failsafeTimeout = System.currentTimeMillis() + 5000;
 				do {
 					bank.depositAllExcept(astralRuneID, fireRuneID, waterRuneID);
 				} while((!inventoryEmptyExcept(astralRuneID) || !inventoryEmptyExcept(astralRuneID, fireRuneID) || !inventoryEmptyExcept(astralRuneID, fireRuneID, waterRuneID)) && System.currentTimeMillis() < failsafeTimeout);
 				verbose("#21 Banking finished!");
 			}
 			
 			return 1;
 		}
 		
 		return 1;
 	}
 	
 	@Override
 	public void onFinish() {
 		log.info("Stopping threads...");
 		
 		//Gracefully stop threads
 		while(monitorThread.isAlive() && antibanThread.isAlive()) {
 			isThreadsRunning = false;
 		}
 		
 		log.info("Threads stopped...");
 		
 		//Gracefully release threads and runnable objects
 		antibanThread = null;
 		monitorThread = null;
 		
 		antiban = null;
 		monitor = null;
 				
 		return ;
 	}
 
 	@Override
 	public void onRepaint(Graphics g2) {
 		if(isPaused || !isLoggedIn()) return ;
 		
 		Graphics2D g = (Graphics2D)g2;
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 		
 		if(!isScriptLoaded) {
 			Scoreboard loadingBoard = new Scoreboard(Scoreboard.BOTTOM_RIGHT, 128, 5);
 			loadingBoard.addWidget(new ScoreboardWidget(timeImage, "Loading..."));
 			loadingBoard.drawScoreboard(g);
 			
 			return ;
 		}
 		
 		//Draw Custom Mouse Cursor
 		g.drawImage(cursorImage, getMouseLocation().x - 16, getMouseLocation().y - 16, observer);
 		
 		//Draw Bottom Left Scoreboard
 		bottomLeftScoreboard.getWidget(currentGrossProductWidgetIndex).setWidgetText("$" + numberFormatter.format(grossProduct));
 		bottomLeftScoreboard.getWidget(currentGrossCostWidgetIndex).setWidgetText("$(" + numberFormatter.format(grossCost) + ")");
 		bottomLeftScoreboard.getWidget(currentNetProductWidgetIndex).setWidgetText("$" + numberFormatter.format(netProduct));
 		bottomLeftScoreboard.drawScoreboard(g);
 		
 		//Draw Top Left Scoreboard
 		topLeftScoreboard.getWidget(humidifyCastsWidgetIndex).setWidgetText(numberFormatter.format(accumulatedHumidifyCasts));
 		topLeftScoreboard.getWidget(vialsFilledWidgetIndex).setWidgetText(numberFormatter.format(accumulatedFilledVials));
 		topLeftScoreboard.drawScoreboard(g);
 		
 		//Draw Top Right Scoreboard
 		topRightScoreboard.getWidget(currentRuntimeWidgetIndex).setWidgetText(millisToClock(System.currentTimeMillis() - startingTime));
 		topRightScoreboard.getWidget(magicEPEarnedWidgetIndex).setWidgetText(magicEPEarnedWidgetText);
 		topRightScoreboard.drawScoreboard(g);
 		
 		//Draw Magic Progress Bar
 		RoundRectangle2D progressBackground = new RoundRectangle2D.Float(
 				Scoreboard.gameCanvasRight - 128,
 				topRightScoreboard.getHeight() + 30,
 				128,
 				8,
 				5,
 				5);
 		
 		Double percentToWidth = new Double(skills.getPercentToNextLevel(Skills.getStatIndex("Magic")));
 		RoundRectangle2D progressBar = new RoundRectangle2D.Float(
 				Scoreboard.gameCanvasRight - 128,
 				topRightScoreboard.getHeight() + 31,
 				percentToWidth.intValue(),
 				7,
 				5,
 				5);
 		
 		g.setColor(new Color(0, 0, 0, 127));
 		g.draw(progressBackground);
 		
 		g.setColor(new Color(0, 0, 200, 191));
 		g.fill(progressBar);
 		
 		return ;
 	}
 	
 	/**
 	 * Calculates the gross product, cost and net product of vials and runes.
 	 * 
 	 * @since 1.0
 	 */
 	private void calculateStatistics() {
 		int vialsPerInventory = getInventoryCountExcept(emptyVialID, filledVialID);
 		
 		grossProduct = accumulatedFilledVials * filledVialMarketPrice;
 		grossCost = (vialsPerInventory * emptyVialMarketPrice) + (astralRuneMarketPrice * accumulatedHumidifyCasts);
 		netProduct = grossProduct - grossCost;
 	}
 	
 	/**
 	 * Verbose method is a log.info wrapper that succesfully executes if the ifVerbose variable is true.
 	 * 
 	 * @since 1.1
 	 */
 	private void verbose(String message) {
 		if(isVerbose) log.info(message);
 	}
 	
 	/**
 	 * Checks to see if we are able to cast the lunar humidify spell.
 	 * 
 	 * @return							true if player can cast humidify
 	 * @since 0.1
 	 */
 	private boolean canCastHumidify() {
 		boolean hasAstralRune = false, hasFireRune = false, hasWaterRune = false;
 
 		if(getInventoryCount(astralRuneID) >= 1)
 			hasAstralRune = true;
 
 		if(getInventoryCount(fireRuneID) >= 1 || hasFireStaff || hasSteamStaff)
 			hasFireRune = true;
 
 		if(getInventoryCount(waterRuneID) >= 3 || hasWaterStaff || hasSteamStaff)
 			hasWaterRune = true;
 
 		return (hasAstralRune && hasFireRune && hasWaterRune);
 	}
 	
 	/**
 	 * Checks to see if the mouse is in a defined rectangular area.
 	 * 
 	 * @param inArea					Rectangle representing a pixel area that the mouse should be in
 	 * 									@see java.awt.Rectangle
 	 * @return							true if the mouse is within the bounds of inArea
 	 * @since 0.1
 	 */
 	private boolean isMouseInArea(Rectangle inArea) {
 		int x = getMouseLocation().x, y = getMouseLocation().y;
 		return (x >= inArea.x && x <= (inArea.x + inArea.width) && y >= inArea.y && y <= (inArea.y + inArea.height));
 	}
 	
 	/**
 	 * Formats millisecond time into HH:MM:SS
 	 * 
 	 * @param milliseconds				milliseconds that should be converted into
 	 * 									the HH:MM:SS format
 	 * 									@see java.lang.System
 	 * @return							formatted HH:MM:SS string
 	 * @since 0.1
 	 */
 	private String millisToClock(long milliseconds) {
 		long seconds = (milliseconds / 1000), minutes = 0, hours = 0;
 		
 		if (seconds >= 60) {
 			minutes = (seconds / 60);
 			seconds -= (minutes * 60);
 		}
 		
 		if (minutes >= 60) {
 			hours = (minutes / 60);
 			minutes -= (hours * 60);
 		}
 		
 		return (hours < 10 ? "0" + hours + ":" : hours + ":")
 				+ (minutes < 10 ? "0" + minutes + ":" : minutes + ":")
 				+ (seconds < 10 ? "0" + seconds : seconds);
 	}
 	
 	/**
 	 * Very simple antiban runnable class.
 	 * 
 	 * @version 1.0
 	 * @since 1.0
 	 */
 	public class Antiban implements Runnable {
 		@Override
 		public void run() {
 			while(isThreadsRunning) {
 				while(isLoggedIn() && !isPaused && isThreadsRunning) {
 					switch(random(1, 11) % 2) {
 					case 1:
 						if(random(1,11) % 2 == 0) {
 							isCameraRotating = true;
 							setCameraRotation(random(1,360));
 							isCameraRotating = false;
 						}
 
 						long c1Timeout = System.currentTimeMillis() + random(30000, 60000);
 						while(System.currentTimeMillis() < c1Timeout && isThreadsRunning) {}
 
 						break;
 
 					default:
 						long c2Timeout = System.currentTimeMillis() + random(30000, 60000);
 						while(System.currentTimeMillis() < c2Timeout && isThreadsRunning) {}
 
 						break;
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Monitor class assembles and updates all experience points and levels gained. The
 	 * class also maintains strings for the onRepaint method.
 	 * 
 	 * @author allometry
 	 * @version 1.1
 	 * @since 1.0
 	 */
 	public class Monitor implements Runnable {
 		@Override
 		public void run() {
 			while(isThreadsRunning) {
 				while(isLoggedIn() && !isPaused && isThreadsRunning) {
 					currentMagicEP = skills.getCurrentSkillExp(Skills.getStatIndex("Magic"));
 					currentMagicLevel = skills.getCurrSkillLevel(Skills.getStatIndex("Magic"));
 					
 					if(currentMagicLevel > startingMagicLevel)
 						magicEPEarnedWidgetText = numberFormatter.format((currentMagicEP - startingMagicEP)) + " (+" + (currentMagicLevel - startingMagicLevel) + ")";
 					else
 						magicEPEarnedWidgetText = numberFormatter.format((currentMagicEP - startingMagicEP));
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Scoreboard is a class for assembling individual scoreboards with widgets
 	 * in a canvas space.
 	 * 
 	 * @author allometry
 	 * @version 1.0
 	 * @since 1.0
 	 */
 	public class Scoreboard {
 		public static final int TOP_LEFT = 1, TOP_RIGHT = 2, BOTTOM_LEFT = 3, BOTTOM_RIGHT = 4;
 		public static final int gameCanvasTop = 25, gameCanvasLeft = 25, gameCanvasBottom = 309, gameCanvasRight = 487;
 
 		private ImageObserver observer = null;
 
 		private int scoreboardLocation, scoreboardX, scoreboardY, scoreboardWidth,
 				scoreboardHeight, scoreboardArc;
 
 		private ArrayList<ScoreboardWidget> widgets = new ArrayList<ScoreboardWidget>();
 		
 		/**
 		 * Creates a new instance of Scoreboard.
 		 * 
 		 * @param scoreboardLocation	the location of where the scoreboard should be drawn on the screen
 		 * 								@see Scoreboard.TOP_LEFT
 		 * 								@see Scoreboard.TOP_RIGHT
 		 * 								@see Scoreboard.BOTTOM_LEFT
 		 * 								@see Scoreboard.BOTTOM_RIGHT
 		 * @param width					the pixel width of the scoreboard
 		 * @param arc					the pixel arc of the scoreboard rounded rectangle
 		 * @since 1.0
 		 */
 		public Scoreboard(int scoreboardLocation, int width, int arc) {
 			this.scoreboardLocation = scoreboardLocation;
 			scoreboardHeight = 10;
 			scoreboardWidth = width;
 			scoreboardArc = arc;
 
 			switch (scoreboardLocation) {
 			case 1:
 				scoreboardX = gameCanvasLeft;
 				scoreboardY = gameCanvasTop;
 				break;
 
 			case 2:
 				scoreboardX = gameCanvasRight - scoreboardWidth;
 				scoreboardY = gameCanvasTop;
 				break;
 
 			case 3:
 				scoreboardX = gameCanvasLeft;
 				break;
 
 			case 4:
 				scoreboardX = gameCanvasRight - scoreboardWidth;
 				break;
 			}
 		}
 		
 		/**
 		 * Adds a ScoreboardWidget to the Scoreboard.
 		 * 
 		 * @param widget				an instance of a ScoreboardWidget containing an image
 		 * 								and text
 		 * 								@see ScoreboardWidget
 		 * @return						true if the widget was added to Scoreboard
 		 * @since 1.0
 		 */
 		public boolean addWidget(ScoreboardWidget widget) {
 			return widgets.add(widget);
 		}
 		
 		/**
 		 * Gets a ScoreboardWidget by it's index within Scoreboard.
 		 * 
 		 * @param widgetIndex			the index of the ScoreboardWidget
 		 * @return						an instance of ScoreboardWidget
 		 * @since 1.0
 		 */
 		public ScoreboardWidget getWidget(int widgetIndex) {
 			try {
 				return widgets.get(widgetIndex);
 			} catch (Exception e) {
 				log.warning("Warning: " + e.getMessage());
 				return null;
 			}
 		}
 		
 		/**
 		 * Gets the Scoreboard widgets.
 		 * 
 		 * @return						an ArrayList filled with ScoreboardWidget's
 		 */
 		public ArrayList<ScoreboardWidget> getWidgets() {
 			return widgets;
 		}
 		
 		/**
 		 * Draws the Scoreboard and ScoreboardWidget's to an instances of Graphics2D.
 		 * 
 		 * @param g						an instance of Graphics2D
 		 * @return						true if Scoreboard was able to draw to the Graphics2D instance and false if it wasn't
 		 * @since 1.0
 		 */
 		public boolean drawScoreboard(Graphics2D g) {
 			try {
 				if(scoreboardHeight <= 10) {
 					for (ScoreboardWidget widget : widgets) {
 						scoreboardHeight += widget.getWidgetImage().getHeight(observer) + 4;
 					}
 				}
 
 				if (scoreboardLocation == 3 || scoreboardLocation == 4) {
 					scoreboardY = gameCanvasBottom - scoreboardHeight;
 				}
 
 				RoundRectangle2D scoreboard = new RoundRectangle2D.Float(
 						scoreboardX, scoreboardY, scoreboardWidth,
 						scoreboardHeight, scoreboardArc, scoreboardArc);
 
 				g.setColor(new Color(0, 0, 0, 127));
 				g.fill(scoreboard);
 
 				int x = scoreboardX + 5;
 				int y = scoreboardY + 5;
 				for (ScoreboardWidget widget : widgets) {
 					widget.drawWidget(g, x, y);
 					y += widget.getWidgetImage().getHeight(observer) + 4;
 				}
 
 				return true;
 			} catch (Exception e) {
 				return false;
 			}
 		}
 		
 		/**
 		 * Returns the height of the Scoreboard with respect to it's contained ScoreboardWidget's.
 		 * 
 		 * @return						the pixel height of the Scoreboard
 		 * @since 1.0 
 		 */
 		public int getHeight() {
 			return scoreboardHeight;
 		}
 	}
 	
 	/**
 	 * ScoreboardWidget is a container intended for use with a Scoreboard. Scoreboards contain
 	 * an image and text, which are later drawn to an instance of Graphics2D.
 	 * 
 	 * @author allometry
 	 * @version 1.0
 	 * @since 1.0
 	 * @see Scoreboard
 	 */
 	public class ScoreboardWidget {
 		private ImageObserver observer = null;
 		private Image widgetImage;
 		private String widgetText;
 		
 		/**
 		 * Creates a new instance of ScoreboardWidget.
 		 * 
 		 * @param widgetImage			an instance of an Image. Recommended size is 16x16 pixels
 		 * 								@see java.awt.Image
 		 * @param widgetText			text to be shown on the right of the widgetImage
 		 * @since 1.0
 		 */
 		public ScoreboardWidget(Image widgetImage, String widgetText) {
 			this.widgetImage = widgetImage;
 			this.widgetText = widgetText;
 		}
 		
 		/**
 		 * Gets the widget image.
 		 * 
 		 * @return						the Image of ScoreboardWidget
 		 * 								@see java.awt.Image
 		 * @since 1.0
 		 */
 		public Image getWidgetImage() {
 			return widgetImage;
 		}
 		
 		/**
 		 * Sets the widget image.
 		 * 
 		 * @param widgetImage			an instance of an Image. Recommended size is 16x16 pixels
 		 * 								@see java.awt.Image
 		 * @since 1.0
 		 */
 		public void setWidgetImage(Image widgetImage) {
 			this.widgetImage = widgetImage;
 		}
 		
 		/**
 		 * Gets the widget text.
 		 * 
 		 * @return						the text of ScoreboardWidget
 		 * @since 1.0
 		 */
 		public String getWidgetText() {
 			return widgetText;
 		}
 		
 		/**
 		 * Sets the widget text.
 		 * 
 		 * @param widgetText			text to be shown on the right of the widgetImage
 		 * @since 1.0
 		 */
 		public void setWidgetText(String widgetText) {
 			this.widgetText = widgetText;
 		}
 		
 		/**
 		 * Draws the ScoreboardWidget to an instance of Graphics2D.
 		 * 
 		 * @param g						an instance of Graphics2D
 		 * @param x						horizontal pixel location of where to draw the widget 
 		 * @param y						vertical pixel location of where to draw the widget
 		 * @since 1.0
 		 */
 		public void drawWidget(Graphics2D g, int x, int y) {
 			g.setColor(Color.white);
 			g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
 
 			g.drawImage(widgetImage, x, y, observer);
 			g.drawString(widgetText, x + widgetImage.getWidth(observer) + 4, y + 12);
 		}
 	}
 }
