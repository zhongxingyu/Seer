 package game;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 public class Paint extends JPanel {
 
 	private static Paint paint;
 	private GUI gui = GUI.getInstance();
 	private Character character = Character.getInstance();
 	private Missions missions = Missions.getInstance();
 	private Functions functions = Functions.getInstance();
 	private Messages messages = Messages.getInstance();
 
 	public static Paint getInstance() {
 		if (paint == null) {
 			paint = new Paint();
 		}
 		return paint;
 	}
 
 	private int lastTime2, currentTime, fps;
 	private int lastTime = 0;
 
 	public void paintComponent(Graphics g) {
 
 		lastTime2 = currentTime;
 		currentTime = (int) System.currentTimeMillis();
 		if (currentTime - lastTime > 500) {
 			lastTime = currentTime;
 			fps = 1000 / (currentTime - lastTime2);
 		}
 
 		super.paintComponent(g);
 
 		// DRAW MENU BUTTONS
 
 		for (int x = 0; x < 840; x += 120) {
 
 			g.setColor(Color.black);
 
 			g.drawRect(x, 20, 120, 30);
 
 			g.setColor(Color.lightGray);
 
 			if (gui.getmY() < 76 && gui.getmY() > 47 && gui.getmX() > x + 3
 					&& gui.getmX() < x + 123 && gui.isMenu() == false) {
 				g.setColor(Color.gray);
 
 				if (gui.isClicked() == true && gui.getmX() > 720) {
 					gui.setMenu(true);
 				} else {
 					gui.setMenu(false);
 				}
 
 			}
 
 			g.fillRect(x + 1, 21, 119, 29);
 
 		}
 
 		// DRAW MENU BUTTON TEXT
 		g.setColor(Color.BLACK);
 		g.drawString("Key Text", 25, 40);
 		g.drawString("Attunement", 125, 40);
 		g.drawString("Cultralia", 245, 40);
 		g.drawString("Navigation", 365, 40);
 		g.drawString("Verba", 485, 40);
 		g.drawString("Gramattica", 605, 40);
 		g.drawString("Menu", 725, 40);
 
 		// DRAW TOP MENU TEXT
 		g.setColor(Color.BLACK);
 		g.drawString(
 				"Name: " + character.getName() + "          Hints: "
 						+ character.getHints() + "          Points: "
 						+ character.getPoints() + "          Level: "
 						+ "          " + missions.getMissionName()
 						+ "                    FPS: " + fps, 5, 15);
 
 		// DRAW MAIN WINDOW
 		g.setColor(Color.BLACK);
 		g.fillRect(0, 50, 795, 495);
 
 		g.setColor(Color.GREEN);
 		g.drawString(functions.getOut(), 5, 65);
 
 		// DRAW BOTTOM INPUT
 
 		// g.setColor(Color.black);
 		// g.drawString(gui.getInput(), 5, 555);
 
 		// DRAW MENU
 		if (gui.isMenu() == true) {
 			g.setColor(Color.darkGray);
 			g.drawRect(300, 122, 200, 310);
 			g.setColor(Color.lightGray);
 			g.fillRect(301, 123, 199, 309);
 
 			for (int y = 140; y < 420; y += 40) {
 
 				g.setColor(Color.black);
 				g.drawRect(330, y, 140, 30);
 
				if (gui.getmY() > y + 30 && gui.getmY() < y && gui.getmX() > 330 && gui.getmX() < 330 + 140) {
 					g.setColor(Color.gray);
 				} else {
 					g.setColor(Color.lightGray);
 				}
 
 				g.fillRect(331, y + 1, 139, 29);
 
 			}
 
 			g.setColor(Color.black);
 			g.drawString("Settings", 360, 160);
 			// ADD UPDATE CHECKER IN THE OPTIONS MENU
 			g.drawString("Load Game", 360, 200);
 			g.drawString("Save Game", 360, 240);
 			g.drawString("New Game", 360, 280);
 			g.drawString("Help", 360, 320);
 			g.drawString("Exit", 360, 360);
 			g.drawString("Back to Game", 360, 400);
 
 		}
 
 		if (messages.isShowMessage() == true) {
 
 			BufferedImage logo = null;
 
 			try {
 				logo = ImageIO.read(new File("src/resource/demiurge.png"));
 			} catch (Exception e) {
 				JOptionPane.showMessageDialog(null, "lol");
 			}
 
 			g.setColor(Color.darkGray);
 			g.drawRect(240, 200, 350, 180);
 			g.setColor(Color.lightGray);
 			g.fillRect(241, 201, 349, 179);
 			g.setColor(Color.black);
 			g.drawString(messages.getMessageText(), 310, 210);
 			
 			g.drawImage(logo, 250, 210, 100, 100, this);
 
 		}
 
 	}
 
 }
