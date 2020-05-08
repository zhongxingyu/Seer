 package blueMaggot;
 
 import entity.Tank;
 import entity.weapon.Gun;
 import gfx.Menu;
 import gfx.ResourceManager;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 
 public class GameOverlay {
 	
 	private int player1Score;
 	private int player2Score;
 	private Gun player1Wep;
 	private Gun player2Wep;
 	private final Font font =  new Font("Consolas", Font.BOLD, 20);
 	
 	public void paintOverlay(Graphics2D g,int score1,int score2,Gun wep1,Gun wep2){
 
 		
 		Tank playerOne;
 		Tank playerTwo;
 		int width = 700;
 		int height = 45;
 		int border =3;
 		
 		g.setColor(new Color(0x3db7ff));
 		//Underlag
 		g.fillRect(290,0, width, height);
 		g.setColor(Color.black);
 		g.fillRect(width - border + 290, 0, border, height);
 		g.fillRect(290, 0, border, height);
 		g.fillRect(290, height - border, width , border);
 		
 		if (GameState.getInstance().isRunning() &&(player1Score!= score1 || player2Score !=score2 || player1Wep != wep1 || player2Wep != wep2)) {
 			playerOne = GameState.getInstance().players.get(0);
 			playerTwo = GameState.getInstance().players.get(1);
 			g.setFont(font);
 			String statsOne = playerOne.getCurrentWeaponName() + "|" + playerOne.getScore() + "|" + playerOne.getNick();
 			g.setColor(Menu.pink);
 			g.fillRect(border+border + 290, border, (width / 2 - border * 2) - border, (height - border * 2) - border);
 			g.fillRect(width / 2 + border + 290, border, (width / 2 - border * 2) - border, (height - border * 2) - border);
 			g.setColor(Color.black);
 			g.drawString(statsOne, 15 + 290, 26);
 			
 //			g.drawImage(mainCanvas, 0, 0, canvasWidth, canvasHeight, Color.BLACK, null);
 			for(int i = 0; i < playerOne.getLife(); i++){
 				g.drawImage(ResourceManager.getInstance().HEART.getRgbBufferedImage(), 550 + i*14, 15, null);
 			}
 			for(int i = 0; i < playerTwo.getLife(); i++){
 				g.drawImage(ResourceManager.getInstance().HEART.getRgbBufferedImage(), 720 - i*14, 15, null);
 			}
 			String statsTwo = playerTwo.getNick() + "|" + playerTwo.getScore() + "|" + playerTwo.getCurrentWeaponName();
 			g.drawString(statsTwo, width - (statsTwo.length() * 11) - 15 + 290, 26);
 		}
 	}
 }
