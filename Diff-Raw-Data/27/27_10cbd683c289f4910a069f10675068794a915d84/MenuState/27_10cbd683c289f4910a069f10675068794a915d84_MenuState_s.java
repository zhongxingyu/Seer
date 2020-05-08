 package gamestate;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 import java.awt.image.BufferedImage;
 
 import javax.imageio.ImageIO;
 
 import main.GamePanel;
 import main.Sound;
 import tilemap.Background;
 
 public class MenuState extends GameState {
 	private Background bg;
 
 	private static final int START = 0;
 	private static final int OPTIONS = 1;
 	private static final int HELP = 2;
 	private static final int QUIT = 3;
 
 	private String[] options = { "Nytt spel", "Alternativ", "Hjälp", "Avsluta" };
 	private String[] description = { "Släpp lös Mange!", "Det är valfritt", "Hjääääääälp!", "Avsluta möget" };
 
 	private BufferedImage mange;
 	private int currentChoice = 0;
 
 	private Color titleColor;
 	private Font titleFont1;
 	private Font titleFont2;
 	private String title1 = "The adventures of";
 	private String title2 = "Manger Danger";
 	private Font font;
 
 	public MenuState(GameStateManager manager) {
 		this.manager = manager;
 		try {
 			bg = new Background("/Backgrounds/menubg.gif", 1);
 			mange = ImageIO.read(getClass().getResourceAsStream("/Backgrounds/mange.png"));
			bg.setScroll(-0.1, 0);
 
 			titleColor = new Color(255, 0, 255);
 			titleFont1 = new Font("Calibri", Font.BOLD, 18);
 			titleFont2 = new Font("Century Gothic", Font.BOLD, 36);
 			font = new Font("Arial", Font.BOLD, 14);
 
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 		init();
 	}
 
 	public void init() {
 		Sound.music4.play(true);
 	}
 
 	public void update() {
 		bg.update();
 	}
 
 	public void draw(Graphics2D g) {
 		// Rita bakgrundsbilden
 		bg.draw(g);
 
 		// Rita en alltmer nyfiken mange beroende p� menyvalen
 		g.drawImage(mange, 130 + currentChoice * 25, 0, null);
 
 
 		g.setFont(titleFont1);
 		g.setColor(Color.BLACK);
 		g.drawString(title1, GamePanel.WIDTH / 2 - (title1.length() * 7 - 1), 41);
 		g.setColor(titleColor);
 		g.drawString(title1, GamePanel.WIDTH / 2 - (title1.length() * 7), 40);
 
 		g.setFont(titleFont2);
 		g.setColor(Color.BLACK);
 		g.drawString(title2, GamePanel.WIDTH / 2 - (title2.length() * 11 - 2), 72);
 		g.setColor(titleColor);
 		g.drawString(title2, GamePanel.WIDTH / 2 - (title2.length() * 11), 70);
 
 
 		// Rita ut menyvalen
 		g.setFont(font);
 		for (int i = 0; i < options.length; i++) {
 			if (i ==  currentChoice) {
 				g.setColor(Color.BLACK);
 				g.drawString("> " + options[i], 31, 171 + i * 15);
 				g.setColor(titleColor);
 				g.drawString("> " + options[i], 30, 170 + i * 15);
 			} else {
 				g.setColor(Color.BLACK);
 				g.drawString(options[i], 31, 171 + i * 15);
 				if (i != 0 && i != 3) {
 				g.setColor(Color.LIGHT_GRAY);
 				g.drawString(options[i], 30, 170 + i * 15);
 				} else {
 					g.setColor(Color.WHITE);
 					g.drawString(options[i], 30, 170 + i * 15);
 				}
 			}
 
 		}
 
 		try {
 			g.setColor(Color.BLACK);
 			g.setFont(new Font("Calibri", Font.ITALIC, 12));
 			g.drawString(description[currentChoice], 31 , 151);
 			g.setColor(Color.WHITE);
 			g.drawString(description[currentChoice], 30, 151);
 		} catch  (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public void keyPressed(int k) {
 		if (k == KeyEvent.VK_ENTER || k == KeyEvent.VK_E) {
 			select();
 		}
 
 		if (k == KeyEvent.VK_UP || k == KeyEvent.VK_W) {
 			currentChoice--;
 			Sound.menu.play();
 			if (currentChoice < 0) {
 				currentChoice = options.length - 1;
 			}
 		}
 
 		if (k == KeyEvent.VK_DOWN || k == KeyEvent.VK_S) {
 			currentChoice++;
 			Sound.menu.play();
 			if (currentChoice == options.length) {
 				currentChoice = 0;
 			}
 		}
 
 		if (k == KeyEvent.VK_ENTER) {
 			select();
 		}
 	}
 
 	private void select() {
 		switch (currentChoice) {
 		case START:
 			manager.setState(GameStateManager.LEVEL1STATE);
 			Sound.music4.stop();
 			Sound.music3.play(true);
 			break;
 		case OPTIONS:
 			break;
 		case HELP:
 			break;
 		case QUIT:
 			System.exit(0);
 			break;
 		}
 	}
 
 
 	public void keyReleased(int k) {
 	}
 
 }
