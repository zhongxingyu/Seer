 package gfx;
 
 import entity.Tank;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics2D;
 
 import blueMaggot.GameState;
 
 public class GameOverlay {
 
 	private final Font font = new Font("Consolas", Font.BOLD, 20);
 
 	public void paintOverlay(Graphics2D g) {
 
 		Tank playerOne;
 		Tank playerTwo;
 
 		int width = 850;
 		int height = 45;
 		int border = 4;
 		int alignMent = (GameState.getInstance().getWidth() / 2 - (width) / 2);
 
 		g.setColor(Menu.blue);
 
 		if (GameState.getInstance().isRunning()) {
 			playerOne = GameState.getInstance().getPlayers().get(0);
 			playerTwo = GameState.getInstance().getPlayers().get(1);
 
 			String statsOne = String.format("%s|%s|%s", playerOne.getNick(), playerOne.getScore(), playerOne.getCurrentWeaponName());
 			String statsTwo = String.format("%s|%s|%s", playerTwo.getCurrentWeaponName(), playerTwo.getScore(), playerTwo.getNick());
 
 			FontMetrics fm = g.getFontMetrics(font);
 
 			g.fillRect(alignMent, 0, width, height);
 			g.setColor(Color.black);
 			g.fillRect(width - border + alignMent, 0, border, height);
 			g.fillRect(alignMent, 0, border, height);
 			g.fillRect(alignMent, height - border, width, border);
 
 			g.setFont(font);
 			g.setColor(Menu.pink);
 			g.fillRect(border + border + alignMent, border, (width / 2 - border * 2) - border, (height - border * 2) - border);
 			g.fillRect(width / 2 + border + alignMent, border, (width / 2 - border * 2) - border, (height - border * 2) - border);
 			g.setColor(Color.black);
 			g.drawString(statsOne, 15 + alignMent, 26);
 			g.drawString(statsTwo, width - fm.stringWidth(statsTwo) - 15 + alignMent, 26);
 			for (int i = 0; i < playerOne.getLife(); i++) {
 				g.drawImage(ResourceManager.getInstance().HEART.getRgbBufferedImage(), GameState.getInstance().getWidth() / 2 - 20 - i * 14, 15, null);
 			}
 			for (int i = 0; i < playerTwo.getLife(); i++) {
 				g.drawImage(ResourceManager.getInstance().HEART.getRgbBufferedImage(), GameState.getInstance().getWidth() / 2 + 7+ i * 14, 15, null);
 			}
 		}
 	}
 }
