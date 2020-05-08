 package joaogl.d4rk.ld27.graphics;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
import joaogl.d4rk.ld27.util.Util;

 public class Gui {
 
 	public static Screen screen;
 
 	public static BufferedImage myCursor1;
 	public static BufferedImage cursor = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
 	static Color color = new Color(182, 182, 182, 155);
 	static Color scolor = new Color(182, 182, 182, 105);
 
 	public Gui(Screen screeN) {
 		screen = screeN;
 	}
 
 	public static void load() {
 		try {
			myCursor1 = ImageIO.read(new File(Util.replaceWithSystemFileSeparator("\\res\\textures\\cursors\\Cursor1.png", "\\")));
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 	}
 
 	public static void drawButton(final int ID, Graphics g, int x, int y, int sizeX, int sizeY, String text, int arc, int border, int XtextAxis, int YtextAxis, int Textsize) {
 		g.setColor(Color.LIGHT_GRAY);
 		g.fillRoundRect(x, y, sizeX, sizeY, arc, arc);
 		g.setColor(Color.GRAY);
 		g.fillRoundRect(x + border, y + border, sizeX - (border * 2), sizeY - (border * 2), arc, arc);
 		if (Game.mouseX < x + sizeX && Game.mouseX > x && Game.mouseY < y + sizeY && Game.mouseY > y && MouseEngine.one) {
 			MouseEngine.clear();
 			SoundEngine.onSoundOptionsChanged();
 			SoundEngine.playSound(1);
 			new Thread(new Runnable() {
 				public void run() {
 					try {
 						Thread.sleep(100);
 						menuButtonClick(ID);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			}).start();
 		}
 		g.setColor(Color.BLUE);
 		g.setFont(new Font("Arial", Font.BOLD, Textsize));
 		g.drawString(text, x + XtextAxis, y + YtextAxis);
 	}
 
 	public static void drawArrowButton(final int ID, Graphics g) {
 		if (GameValues.hasStatusBar) {
 			int xpoints[] = { (GameValues.width - statusBar.getWidth() + 5) + offset, (GameValues.width - statusBar.getWidth() + 15) + offset, (GameValues.width - statusBar.getWidth() + 5) + offset };
 			int ypoints[] = { 17, 27, 36 };
 			g.setColor(Color.RED);
 			g.fillPolygon(xpoints, ypoints, 3);
 		} else {
 			int xpoints[] = { (GameValues.width - statusBar.getWidth() + 15) + offset, (GameValues.width - statusBar.getWidth() + 5) + offset, (GameValues.width - statusBar.getWidth() + 15) + offset };
 			int ypoints[] = { 17, 27, 36 };
 			g.setColor(Color.RED);
 			g.fillPolygon(xpoints, ypoints, 3);
 		}
 		if (Game.mouseX < ((GameValues.width - statusBar.getWidth() + 15) + offset) && Game.mouseX > ((GameValues.width - statusBar.getWidth() + 5) + offset) && Game.mouseY < 36 && Game.mouseY > 17 && MouseEngine.one) {
 			MouseEngine.clear();
 			SoundEngine.onSoundOptionsChanged();
 			SoundEngine.playSound(1);
 			new Thread(new Runnable() {
 				public void run() {
 					try {
 						Thread.sleep(100);
 						menuButtonClick(ID);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			}).start();
 		}
 	}
 
 	public static void drawSlider(final Graphics g) {
 		// 1
 		if (Game.mouseX < sliderX + 50 && Game.mouseX > sliderX && Game.mouseY < (GameValues.height / 4) + 20 && Game.mouseY > (GameValues.height / 4) && MouseEngine.one) {
 			MouseEngine.clear();
 			SoundEngine.onSoundOptionsChanged();
 			SoundEngine.playSound(2); // Slider in sound
 			new Thread(new Runnable() {
 				public void run() {
 					try {
 						Thread.sleep(10);
 						MouseEngine.inSlidermusic = true;
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			}).start();
 		}
 		g.setColor(Color.LIGHT_GRAY);
 		g.fillRoundRect(sliderX, (GameValues.height / 4), 20, 20, 50, 50);
 		g.setColor(Color.BLUE);
 		g.fillRoundRect(sliderX + 3, (GameValues.height / 4) + 3, 15, 15, 50, 50);
 
 		// 2
 		if (Game.mouseX < sliderX2 + 50 && Game.mouseX > sliderX2 && Game.mouseY < (GameValues.height / 4) + 20 && Game.mouseY > (GameValues.height / 4) && MouseEngine.one) {
 			MouseEngine.clear();
 			SoundEngine.onSoundOptionsChanged();
 			SoundEngine.playSound(2); // Slider in sound
 			new Thread(new Runnable() {
 				public void run() {
 					try {
 						Thread.sleep(10);
 						MouseEngine.inSlidersound = true;
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			}).start();
 		}
 		g.setColor(Color.LIGHT_GRAY);
 		g.fillRoundRect(sliderX2, (GameValues.height / 4), 20, 20, 50, 50);
 		g.setColor(Color.BLUE);
 		g.fillRoundRect(sliderX2 + 3, (GameValues.height / 4) + 3, 15, 15, 50, 50);
 
 	}
 
 	public static void renderDebug(Graphics g) {
 		g.setColor(Color.BLUE);
 		g.setFont(new Font("Arial", Font.BOLD, 24));
 		g.drawString(Game.mouseX + "," + Game.mouseY, Game.mouseX, Game.mouseY);
 	}
 
 	public static void renderPercentage(Graphics g) {
 		if (MouseEngine.inSlidermusic) {
 			g.setColor(Color.BLUE);
 			g.setFont(new Font("Arial", Font.BOLD, 24));
 			g.drawString(SoundEngine.getSound() + "%", Game.mouseX + 10, Game.mouseY - 10);
 		} else if (MouseEngine.inSlidersound) {
 			g.setColor(Color.BLUE);
 			g.setFont(new Font("Arial", Font.BOLD, 24));
 			g.drawString(SoundEngine.getMusic() + "%", Game.mouseX + 10, Game.mouseY - 10);
 		}
 	}
 
 	public static void debug(Graphics g, int xstatus) {
 		int x = GameValues.pXLoc;
 		int y = GameValues.pYLoc;
 		String coord = "X: " + x + "(" + (x / GameValues.TILE_SIZE) + ")" + "Y: " + y + "(" + (y / GameValues.TILE_SIZE) + ")";
 		drawText(g, "Debug: ", xstatus - 80, GameValues.height - 180, 15, "Arial", Color.RED);
 		drawText(g, "Version: " + GameValues.GAME_VERSION, xstatus - 80, GameValues.height - 165, 10, "Arial", Color.BLACK);
 		drawText(g, "FPS: " + GameValues.fps + " UPS: " + GameValues.ups, xstatus - 80, GameValues.height - 150, 10, "Arial", Color.BLACK);
 		drawText(g, coord, xstatus - 80, GameValues.height - 135, 10, "Arial", Color.BLACK);
 		drawText(g, "Walking Speed: " + GameValues.pSpeed, xstatus - 80, GameValues.height - 120, 10, "Arial", Color.BLACK);
 		drawText(g, "FullTime: " + TimeManager.getFullTime(), xstatus - 80, GameValues.height - 105, 10, "Arial", Color.BLACK);
 		drawText(g, "Brightness: " + TimeManager.getBrightness(), xstatus - 80, GameValues.height - 90, 10, "Arial", Color.BLACK);
 		if (GameValues.debugMenupreview) drawText(g, "Coursor: " + GameValues.cursoridpreview + " World: " + Player.getWorld(), xstatus - 80, GameValues.height - 75, 10, "Arial", Color.BLACK);
 		else drawText(g, "Coursor: " + GameValues.cursorid + " World: " + Player.getWorld(), xstatus - 80, GameValues.height - 75, 10, "Arial", Color.BLACK);
 		drawText(g, "Sound Volume: " + SoundEngine.getSound() + "% Menu: " + GameValues.menu, xstatus - 80, GameValues.height - 60, 10, "Arial", Color.BLACK);
 		drawText(g, "Music Volume: " + SoundEngine.getMusic() + "%", xstatus - 80, GameValues.height - 45, 10, "Arial", Color.BLACK);
 	}
 
 	/*
 	 * case 2: GameValues.menu = 1; GameValues.isGameRunning = true; GameValues.starting = true; GameValues.reset = true; break; case 3: FileStream.gameLoad(); GameValues.menu = 1; GameValues.isGameRunning = true; GameValues.loading = true; GameValues.starting = true; GameValues.reset = true;
 	 */
 
 	public static void menuButtonClick(int ID) {
 		switch (ID) {
 		case 1:
 			GameValues.menu = 1;
 		break;
 		case 2:
 			GameValues.menu = 1;
 			GameValues.isGameRunning = true;
 			MainMethod.startGame();
 		break;
 		case 3:
 			if (GameValues.hasStatusBar) {
 				GameValues.hasStatusBar = false;
 				offset = 185;
 			} else {
 				GameValues.hasStatusBar = true;
 				offset = 0;
 			}
 		break;
 		case 4:
 			GameValues.menu = 3;
 		break;
 		case 5:
 			GameValues.menu = 4;
 		break;
 		case 6:
 			GameValues.menu = 2;
 			SoundEngine.onSoundOptionsChanged();
 		break;
 		case 7:
 			Player.setHealth(100);
 			GameValues.respawning = true;
 			GameValues.menu = 1;
 		break;
 		case 8:
 			MainMethod.closing();
 			FileStream.saveSettings();
 			GameValues.menu = 7;
 		break;
 		case 9:
 			GameValues.menu = 6;
 			GameValues.cursoridpreview = GameValues.cursorid;
 			GameValues.debugCoordspreview = GameValues.debugCoords;
 			GameValues.debugMenupreview = GameValues.debugMenu;
 			GameValues.volumepreview = GameValues.volume;
 			GameValues.musicpreview = GameValues.music;
 			GameValues.defaultSizepreview = GameValues.defaultSize;
 			maxX = (float) (((GameValues.width / 8) * 7) - 10);
 			minX = (float) (((GameValues.width / 8) * 5) + 8);
 
 			maxX2 = (float) (((GameValues.width / 8) * 3) - 10);
 			minX2 = (float) ((GameValues.width / 8) + 8);
 			sliderX = (int) ((((GameValues.volume * ((maxX - 1.0) - minX)) / SoundEngine.getMaxVolume()) + minX) - 9.0);
 			sliderX2 = (int) ((((GameValues.music * ((maxX2 - 1.0) - minX2)) / SoundEngine.getMaxVolume()) + minX2) - 9.0);
 		break;
 		case 10:
 			GameValues.closing = true;
 			GameValues.close = "Closing connections.";
 			Side.message = "disc";
 			while (GameValues.connected) {
 			}
 			SoundEngine.close();
 			System.exit(0);
 		break;
 		case 11:
 			GameValues.menu = 2;
 		break;
 		case 12:
 			if (GameValues.cursoridpreview != 0) GameValues.cursoridpreview--;
 			else GameValues.cursoridpreview = GameValues.cursormax;
 		break;
 		case 13:
 			if (GameValues.cursoridpreview != GameValues.cursormax) GameValues.cursoridpreview++;
 			else GameValues.cursoridpreview = 0;
 		break;
 		case 14:
 			GameValues.menu = 2;
 			GameValues.cursorid = GameValues.cursoridpreview;
 			GameValues.volume = GameValues.volumepreview;
 			GameValues.music = GameValues.musicpreview;
 			GameValues.debugMenu = GameValues.debugMenupreview;
 			GameValues.debugCoords = GameValues.debugCoordspreview;
 			GameValues.defaultSize = GameValues.defaultSizepreview;
 			FileStream.saveSettings();
 		break;
 		case 15:
 			if (GameValues.debugMenupreview) GameValues.debugMenupreview = false;
 			else GameValues.debugMenupreview = true;
 		break;
 		case 16:
 			if (GameValues.debugCoordspreview) GameValues.debugCoordspreview = false;
 			else GameValues.debugCoordspreview = true;
 		break;
 		case 17:
 			if (GameValues.defaultSizepreview) GameValues.defaultSizepreview = false;
 			else GameValues.defaultSizepreview = true;
 		break;
 		case 18:
 			MainMethod.setWindowState(false, 1200);
 		break;
 		}
 	}
 
 	public static void drawText(Graphics g, String text, int X, int Y, int size, String font, Color color) {
 		g.setFont(new Font(font, Font.BOLD, size));
 		g.setColor(color);
 		g.drawString(text, X, Y);
 	}
 
 	public static void statusBar(Graphics g, int X, int Y, int xstatus) {
 		Color health = new Color(213, 31, 8);
 		Color armor = new Color(113, 217, 62);
 		Color stamina = new Color(58, 96, 193);
 		g.setFont(new Font("Arial", Font.PLAIN, 12));
 		g.drawImage(statusBar, GameValues.width - statusBar.getWidth() + +offset, 0, statusBar.getWidth(), statusBar.getHeight(), null);
 
 		drawArrowButton(3, g);
 
 		g.setColor(health);
 		g.fillRoundRect(X + 2 + offset, Y + 2, Player.getHealth() / 20, 18, 7, 7);
 		g.setColor(armor);
 		g.fillRoundRect(X + 2 + offset, (Y + 40) + 2, Player.getArmor() / 20, 18, 7, 7);
 		g.setColor(stamina);
 		g.fillRoundRect(X + 2 + offset, (Y + 78) + 2, Player.getStamina() / 40 / 2, 18, 7, 7);
 
 		g.setColor(Color.BLACK);
 		g.drawString("Health:", X + offset, Y - 4);
 		g.drawString(Player.getHealth() / 20 + "/" + Player.maxHealth() / 20, X + 28 + offset, Y + 15);
 		g.drawString("Armor:", X + offset, (Y + 40) - 4);
 		g.drawString(Player.getArmor() / 20 + "/" + Player.maxArmor() / 20, X + 28 + offset, (Y + 40) + 15);
 		g.drawString("Stamina:", X + offset, (Y + 78) - 4);
 		g.drawString(Player.getStamina() / 40 / 2 + "/" + Player.maxStamina() / 80, X + 28 + offset, (Y + 78) + 15);
 
 		if (GameValues.menu == 6 && GameValues.debugMenupreview) debug(g, xstatus + offset);
 		else if (GameValues.menu != 6 && GameValues.debugMenu) debug(g, xstatus + offset);
 
 		int size = (int) (GameValues.pName.length() * 3.655333333333);
 		drawText(g, "" + GameValues.pName, xstatus - size + offset, 190, 13, "Arial", Color.BLACK);
 		drawText(g, "" + Player.getMoney(), xstatus - 50 + offset, 354, 25, "Arial", Color.BLACK);
 		drawText(g, "" + Player.getBankMoney(), xstatus - 50 + offset, 391, 25, "Arial", Color.BLACK);
 		drawText(g, "" + TimeManager.getTime(), xstatus - 30 + offset, 41, 25, "Arial", Color.CYAN);
 	}
 
 	public static void mainMenu(Graphics g) {
 		int size = 55;
 		int height = (GameValues.height / 2) - (size + 30);
 		if (GameValues.isGameRunning) {
 			drawBackGround(g, true, true);
 			drawButton(1, g, GameValues.widthscaled / 2 - 125, height, 250, 50, "Resume Game", 15, 5, 46, 33, 24);
 			drawButton(5, g, GameValues.widthscaled / 2 - 125, height + (size), 250, 50, "Help", 15, 5, 95, 33, 24);
 			drawButton(9, g, GameValues.widthscaled / 2 - 125, height + (size * 2), 250, 50, "Settings", 15, 5, 75, 33, 24);
 			drawButton(8, g, GameValues.widthscaled / 2 - 125, height + (size * 3), 250, 50, "Quit Game", 15, 5, 62, 33, 24);
 		} else {
 			drawBackGround(g, false, true);
 			drawButton(2, g, GameValues.widthscaled / 2 - 125, height + (size), 250, 50, "Play", 15, 5, 100, 33, 24);
 			drawButton(4, g, GameValues.widthscaled / 2 - 125, height + (size * 2), 250, 50, "About", 15, 5, 89, 33, 24);
 			drawButton(5, g, GameValues.widthscaled / 2 - 125, height + (size * 3), 250, 50, "Help", 15, 5, 95, 33, 24);
 			drawButton(9, g, GameValues.widthscaled / 2 - 125, height + (size * 4), 250, 50, "Settings", 15, 5, 75, 33, 24);
 			drawButton(8, g, GameValues.widthscaled / 2 - 125, height + (size * 5), 250, 50, "Quit Game", 15, 5, 62, 33, 24);
 		}
 	}
 
 	public static void renderGameGui(Graphics g) {
 		if (g instanceof Graphics2D) {
 			Graphics2D g2 = (Graphics2D) g;
 			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 		}
 		if (GameValues.menu == 1 || GameValues.menu == 2 || GameValues.menu == 5 || GameValues.menu == 6 || GameValues.menu == 7) {
 			g.drawImage(Game.image, 0, 0, GameValues.widthscaled, GameValues.heightscaled, null);
 			if (GameValues.menu == 1 || GameValues.menu == 2 || GameValues.menu == 5 || GameValues.menu == 6 || GameValues.menu == 7) {
 				statusBar(g, GameValues.width - (statusBar.getWidth() / 2) - 40, 215, GameValues.width - (statusBar.getWidth() / 2) + 10);
 				if (GameValues.menu == 2) mainMenu(g);
 			}
 		}
 		if (GameValues.menu == 2) mainMenu(g);
 		else if (GameValues.menu == 3) {
 			String[] about = { "This game is being develop by John Green and Joo Loureno.", "For now there's no objective to play this game if you'r not a beta tester.", "If you have found a bug or issue you can report it, to one of the game", "developers. ", "", "To do it the easiest way is to join our teamspeak server at", "ts.mastermine.eu:9986 and contact us.", "", "We hope you like the game, Have Fun!" };
 			drawStuff(g, about, 1);
 			drawButton(6, g, GameValues.widthscaled / 2 - (150 / 2), GameValues.height - 100, 150, 50, "Back", 15, 5, 48, 33, 24);
 		} else if (GameValues.menu == 4) {
 			String[] help = { "Theres nothing to help just try to survive xD." };
 			drawStuff(g, help, 1);
 			drawButton(6, g, GameValues.widthscaled / 2 - (150 / 2), GameValues.height - 100, 150, 50, "Back", 15, 5, 48, 33, 24);
 		} else if (GameValues.menu == 5) drawDeaths(g);
 		else if (GameValues.menu == 6) drawSettings(g);
 		else if (GameValues.menu == 7) drawStuff(g, null, 3);
 		else if (GameValues.menu == 8) drawCharacterSelect(g);
 		else if (GameValues.menu == 9) {
 			drawStuff(g, null, 2);
 		}
 		if (GameValues.menu == 1) drawText(g, "Zoom: " + MouseEngine.getZoom() + "x", GameValues.width - statusBar.getWidth() - 40 + offset, GameValues.height - 45, 12, "Arial", Color.RED);
 	}
 
 	public static void drawBackGround(Graphics g, boolean ingame, boolean usename) {
 		if (ingame) {
 			g.setColor(scolor);
 			g.fillRect(0, 0, GameValues.width, GameValues.height);
 		} else g.drawImage(backgroundNN, 0, 0, GameValues.width, GameValues.height, null);
 		if (usename) g.drawImage(name, (GameValues.width / 2) - ((GameValues.width / 2) - 5), (name.getHeight() / 4), GameValues.width - 30, name.getHeight(), null);
 	}
 
 	public static void drawStuff(Graphics g, String[] text, int id) {
 		if (id == 1) {
 			drawBackGround(g, false, true);
 			for (int i = 0; i < text.length; i++)
 				if (GameValues.width < 905) screen.renderText(text[i], 20, ((i * 30) + 230), 20, 0, 16777215);
 				else screen.renderText(text[i], 20, ((i * 30) + 230), 24, 0, 16777215);
 		} else if (id == 2) {
 			drawBackGround(g, false, true);
 			if (bounce <= 5) doubleText(g, "Loading", ((GameValues.width / 2) - 90), (GameValues.height / 2), 40, "Arial");
 			else if (bounce <= 10 && bounce >= 5) doubleText(g, "Loading.", ((GameValues.width / 2) - 90), (GameValues.height / 2), 40, "Arial");
 			else if (bounce <= 15 && bounce >= 10) doubleText(g, "Loading..", ((GameValues.width / 2) - 90), (GameValues.height / 2), 40, "Arial");
 			else if (bounce <= 20 && bounce >= 15) doubleText(g, "Loading...", ((GameValues.width / 2) - 90), (GameValues.height / 2), 40, "Arial");
 			else if (bounce <= 25 && bounce >= 20) bounce = 0;
 			doubleText(g, "FPS: " + GameValues.fps, (GameValues.width - 100), (GameValues.height - 45), 20, "Arial");
 			bounce++;
 		} else if (id == 3) {
 			if (GameValues.isGameRunning) drawBackGround(g, true, true);
 			else drawBackGround(g, false, true);
 			if (GameValues.close == "Closing connections.") doubleText(g, GameValues.close, ((GameValues.width / 2) - 180), (GameValues.height / 2), 40, "Arial");
 			else doubleText(g, GameValues.close, ((GameValues.width / 2) - 300), (GameValues.height / 2), 40, "Arial");
 			if (!GameValues.closing) drawButton(10, g, GameValues.widthscaled / 2 - 125, (GameValues.height / 2) + 30, 100, 50, "Yes", 15, 5, 32, 33, 24);
 			if (!GameValues.closing) drawButton(6, g, GameValues.widthscaled / 2, (GameValues.height / 2) + 30, 100, 50, "No", 15, 5, 35, 33, 24);
 		}
 	}
 
 	public static void doubleText(Graphics g, String text, int x, int y, int size, String font) {
 		drawText(g, text, x + 1, y + 1, size, font, Color.WHITE);
 		drawText(g, text, x, y, size, font, Color.RED);
 	}
 
 	public static void drawSettings(Graphics g) {
 		if (GameValues.isGameRunning) drawBackGround(g, true, false);
 		else drawBackGround(g, false, false);
 		int size = 40;
 		doubleText(g, "Settings Menu", (GameValues.width / 2) - 135, (GameValues.height / 15), size, "Arial");
 		drawButton(14, g, GameValues.widthscaled / 2 - 125, ((GameValues.height / 15) * 12), 100, 50, "Save", 15, 5, 22, 33, 24);
 		drawButton(6, g, GameValues.widthscaled / 2, ((GameValues.height / 15) * 12), 100, 50, "Cancel", 15, 5, 11, 33, 24);
 
 		// Settings volume
 		drawText(g, "Music Volume", ((GameValues.width / 40) * 10) - 130, ((GameValues.height / 15) * 3), size, "Arial", Color.RED);
 		drawText(g, "Sound Volume", ((GameValues.width / 40) * 30) - 130, ((GameValues.height / 15) * 3), size, "Arial", Color.RED);
 		drawVolumePoly(g);
 		drawSlider(g);
 
 		// Settings cursor
 		drawText(g, "Cursor", ((GameValues.width / 50) * 12) - 72, ((GameValues.height / 25) * 9), size, "Arial", Color.RED);
 		drawText(g, "Screen", ((GameValues.width / 50) * 38) - 72, ((GameValues.height / 25) * 9), size, "Arial", Color.RED);
 
 		int x = (GameValues.width / 4) - 45;
 		int y = ((GameValues.height / 25) * 10);
 		drawButton(12, g, x + 60, y, 50, 50, "-", 15, 5, 20, 31, 24);
 		g.setColor(Color.LIGHT_GRAY);
 		g.fillRoundRect(x, y, 50, 50, 15, 15);
 		drawButton(13, g, x - 60, y, 50, 50, "+", 15, 5, 20, 33, 24);
 
 		if (GameValues.cursoridpreview > 9) drawText(g, "" + GameValues.cursoridpreview, x + 3, y + 40, size, "Arial", Color.BLACK);
 		else drawText(g, "" + GameValues.cursoridpreview, x + 15, y + 40, size, "Arial", Color.BLACK);
 
 		x = ((GameValues.height / 4) * 3) + 30;
 		drawButton(17, g, x, y, 300, 45, "Use default screen size: " + GameValues.defaultSizepreview, 15, 5, 13, 30, 20);
 		drawButton(18, g, x + 50, y + 50, 191, 45, "Reset default size", 15, 5, 13, 30, 20);
 
 		// Debug
 		drawText(g, "Debug", (GameValues.width / 2) - 70, ((GameValues.height / 15) * 9), size, "Arial", Color.RED);
 		drawButton(15, g, (GameValues.width / 2) - 270, ((GameValues.height / 15) * 10), 245, 50, "Debug Mode: " + GameValues.debugMenupreview, 15, 5, 20, 31, 24);
 		drawButton(16, g, (GameValues.width / 2), ((GameValues.height / 15) * 10), 235, 50, "Use Coords: " + GameValues.debugCoordspreview, 15, 5, 20, 31, 24);
 	}
 
 	public static void drawCharacterSelect(Graphics g) {
 		if (GameValues.isGameRunning) drawBackGround(g, true, false);
 		else drawBackGround(g, false, false);
 		int size = 40;
 		doubleText(g, "Settings Menu", (GameValues.width / 2) - 135, (GameValues.height / 15), size, "Arial");
 		drawButton(14, g, GameValues.widthscaled / 2 - 125, ((GameValues.height / 15) * 12), 100, 50, "Save", 15, 5, 22, 33, 24);
 		drawButton(6, g, GameValues.widthscaled / 2, ((GameValues.height / 15) * 12), 100, 50, "Cancel", 15, 5, 11, 33, 24);
 
 		// Settings volume
 		drawText(g, "Music Volume", ((GameValues.width / 40) * 10) - 130, ((GameValues.height / 15) * 3), size, "Arial", Color.RED);
 		drawText(g, "Sound Volume", ((GameValues.width / 40) * 30) - 130, ((GameValues.height / 15) * 3), size, "Arial", Color.RED);
 		drawVolumePoly(g);
 		drawSlider(g);
 
 		// Settings cursor
 		drawText(g, "Cursor", ((GameValues.width / 50) * 12) - 72, ((GameValues.height / 25) * 9), size, "Arial", Color.RED);
 		drawText(g, "Screen", ((GameValues.width / 50) * 38) - 72, ((GameValues.height / 25) * 9), size, "Arial", Color.RED);
 
 		int x = (GameValues.width / 4) - 45;
 		int y = ((GameValues.height / 25) * 10);
 		drawButton(12, g, x + 60, y, 50, 50, "-", 15, 5, 20, 31, 24);
 		g.setColor(Color.LIGHT_GRAY);
 		g.fillRoundRect(x, y, 50, 50, 15, 15);
 		drawButton(13, g, x - 60, y, 50, 50, "+", 15, 5, 20, 33, 24);
 
 		if (GameValues.cursoridpreview > 9) drawText(g, "" + GameValues.cursoridpreview, x + 3, y + 40, size, "Arial", Color.BLACK);
 		else drawText(g, "" + GameValues.cursoridpreview, x + 15, y + 40, size, "Arial", Color.BLACK);
 
 		x = ((GameValues.height / 4) * 3) + 30;
 		drawButton(17, g, x, y, 300, 45, "Use default screen size: " + GameValues.defaultSizepreview, 15, 5, 13, 30, 20);
 		drawButton(18, g, x + 50, y + 50, 191, 45, "Reset default size", 15, 5, 13, 30, 20);
 
 		// Debug
 		drawText(g, "Debug", (GameValues.width / 2) - 70, ((GameValues.height / 15) * 9), size, "Arial", Color.RED);
 		drawButton(15, g, (GameValues.width / 2) - 270, ((GameValues.height / 15) * 10), 245, 50, "Debug Mode: " + GameValues.debugMenupreview, 15, 5, 20, 31, 24);
 		drawButton(16, g, (GameValues.width / 2), ((GameValues.height / 15) * 10), 235, 50, "Use Coords: " + GameValues.debugCoordspreview, 15, 5, 20, 31, 24);
 	}
 
 	public static void drawDeaths(Graphics g) {
 		drawBackGround(g, true, false);
 		doubleText(g, "You Died!", (GameValues.widthscaled / 2) - 300, (GameValues.height / 4), 140, "Arial");
 		drawButton(7, g, GameValues.widthscaled / 2 - (250 / 2), GameValues.height - 150, 250, 50, "Respawn", 15, 5, 70, 33, 24);
 	}
 
 	private static void drawVolumePoly(Graphics g) {
 		// 1
 		int xpoints1[] = { (GameValues.width / 8), ((GameValues.width / 8) - 20), (GameValues.width / 8) };
 		int ypoints1[] = { (GameValues.height / 4), (GameValues.height / 4) + 10, (GameValues.height / 4) + 20 };
 		g.setColor(Color.LIGHT_GRAY);
 		g.fillPolygon(xpoints1, ypoints1, 3);
 
 		int xpoints11[] = { (GameValues.width / 8) * 3, ((GameValues.width / 8) * 3) + 20, (GameValues.width / 8) * 3 };
 		int ypoints11[] = { (GameValues.height / 4), (GameValues.height / 4) + 10, (GameValues.height / 4) + 20 };
 		g.fillPolygon(xpoints11, ypoints11, 3);
 
 		g.drawLine((GameValues.width / 8), (GameValues.height / 4) + 10, (GameValues.width / 8) * 3, (GameValues.height / 4) + 10);
 
 		// 2
 		int xpoints2[] = { (GameValues.width / 8) * 5, ((GameValues.width / 8) * 5) - 20, (GameValues.width / 8) * 5 };
 		int ypoints2[] = { (GameValues.height / 4), (GameValues.height / 4) + 10, (GameValues.height / 4) + 20 };
 		g.setColor(Color.LIGHT_GRAY);
 		g.fillPolygon(xpoints2, ypoints2, 3);
 
 		int xpoints22[] = { (GameValues.width / 8) * 7, ((GameValues.width / 8) * 7) + 20, (GameValues.width / 8) * 7 };
 		int ypoints22[] = { (GameValues.height / 4), (GameValues.height / 4) + 10, (GameValues.height / 4) + 20 };
 		g.fillPolygon(xpoints22, ypoints22, 3);
 
 		g.drawLine((GameValues.width / 8) * 5, (GameValues.height / 4) + 10, (GameValues.width / 8) * 7, (GameValues.height / 4) + 10);
 	}
 
 	public static void update(Graphics g) {
 		if (MouseEngine.inSlidermusic) {
 			maxX = (float) (((GameValues.width / 8) * 7) - 10);
 			minX = (float) (((GameValues.width / 8) * 5) + 8);
 			if (Game.mouseX < maxX && Game.mouseX > minX) sliderX = Game.mouseX - 10;
 			else {
 				if (Game.mouseX > maxX) sliderX = (int) maxX - 10;
 				if (Game.mouseX < minX) sliderX = (int) minX - 9;
 			}
 			GameValues.volumepreview = (float) ((((sliderX + 9.0) - minX) * SoundEngine.getMaxVolume()) / ((maxX - 1.0) - minX));
 			SoundEngine.onSoundOptionsChanged();
 		} else if (MouseEngine.inSlidersound) {
 			maxX2 = (float) (((GameValues.width / 8) * 3) - 10);
 			minX2 = (float) ((GameValues.width / 8) + 8);
 			if (Game.mouseX < maxX2 && Game.mouseX > minX2) sliderX2 = Game.mouseX - 10;
 			else {
 				if (Game.mouseX > maxX2) sliderX2 = (int) maxX2 - 10;
 				if (Game.mouseX < minX2) sliderX2 = (int) minX2 - 9;
 			}
 			GameValues.musicpreview = (float) ((((sliderX2 + 9.0) - minX2) * SoundEngine.getMaxVolume()) / ((maxX2 - 1.0) - minX2));
 			SoundEngine.onSoundOptionsChanged();
 		}
 	}
 }
