 package game;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 public class GameFrame extends JPanel {
 	private static final long serialVersionUID = 1L;
 
 	private boolean gameStarted = false, preGame = false, gameOver = false;
 	private Rectangle startButton = new Rectangle(325, 240, 150, 50);
 	private Rectangle quitButton = new Rectangle(325, 320, 150, 50);
 	private Rectangle contButton = new Rectangle(325, 320, 150, 50);
 	private Color backgroundColor = Color.BLACK;
 	private GameControl control;
 
 
 	public GameFrame(GameControl control) {
 		this.control = control;
 		this.setDoubleBuffered(true);
 		this.setSize(800, 480);
 		this.setBackground(backgroundColor);
 		this.addMouseListener(new MouseHandler());
 		this.addMouseMotionListener(new MouseHandler());
 	}
 
 	public void preGame() {
 		preGame = true;
 		repaint();
 
 	}
 	public void startGame() {
 		gameStarted = true;
 		gameOver = false;
 		control.gameOver = false;
 		control.startGame();
 		repaint();
 	}
 
 	public void gameOver() {
 		preGame = false;
 		gameStarted = false;
 		gameOver = true;
 		control.gameOver = true;
 		repaint();
 	}
 
 	@Override
 	public void paint(Graphics canvas) {
 		SpriteSheet spriteSheet = new SpriteSheet();
 		Image sprite = spriteSheet.getSprite(1, 1);
 		canvas.setColor(Color.BLACK);
 		canvas.fillRect(0, 0, 800, 960);
 		if(!gameStarted) {
 			//menu
 			if (!preGame) {
 				if (canvas instanceof Graphics2D) {
 					Graphics2D g2 = (Graphics2D) canvas;
 					g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
 							RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 				}
 				canvas.setColor(Color.WHITE);
 				canvas.setFont(new Font("Comic Sans MS", Font.BOLD, 64));
 				if (!gameOver) {
 					canvas.drawString("heArT bEaT", 220, 200);
 					canvas.setColor(Color.MAGENTA);
 					canvas.fillRect(startButton.x, startButton.y, startButton.width, startButton.height);
 					canvas.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
 					canvas.setColor(Color.WHITE);
 					canvas.drawString("Start Game", startButton.x + 16, startButton.y + 32);
 					sprite = spriteSheet.getSprite(0, 0);
 					canvas.drawImage(sprite, 150, 137, null);
 				} else {
 					canvas.drawString("Game Over", 230, 150);
 					canvas.setFont(new Font("Comic Sans MS", Font.BOLD, 30));
 					canvas.drawString("Final Score: " + control.score.getScore(), 280, 200);
 					canvas.setColor(Color.MAGENTA);
 					canvas.fillRect(startButton.x, startButton.y, startButton.width, startButton.height);
 					canvas.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
 					canvas.setColor(Color.WHITE);
 					canvas.drawString("Retry", startButton.x + 43, startButton.y + 32);
					canvas.drawImage(sprite, 150, 137, null);
 				}
 				canvas.setColor(Color.CYAN);
 				canvas.fillRect(quitButton.x, quitButton.y, quitButton.width, quitButton.height);
 				canvas.setColor(Color.WHITE);
 				canvas.drawString("Exit Game", quitButton.x + 25, quitButton.y + 32);
 				
 			} else {
 				if (canvas instanceof Graphics2D) {
 					Graphics2D g2 = (Graphics2D) canvas;
 					g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
 							RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 				}
 				canvas.setColor(Color.WHITE);
 				canvas.setFont(new Font("Comic Sans MS", Font.BOLD, 64));
 				canvas.drawString("heArT bEaT", 220, 200);
 				canvas.drawImage(sprite, 150, 137, null);
 				canvas.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
 				canvas.drawString("Press space to jump! Sync the heartbeat with the song...", 130, 260);
 				canvas.setColor(Color.GRAY);
 				canvas.fillRect(contButton.x, contButton.y, contButton.width, contButton.height);
 				canvas.setColor(Color.WHITE);
 				canvas.drawString("Continue", contButton.x + 32, contButton.y + 32);
 				sprite = spriteSheet.getSprite(0, 1);
 				canvas.drawImage(sprite, 150, 137, null);
 			}
 		} else {
 			//canvas.clearRect(0, 0, getWidth(), getHeight());
 			control.paint(canvas.create(0,0,getWidth(),getHeight()));
 		}
 
 	}
 
 	public class MouseHandler extends MouseAdapter {
 		@Override
 		public void mouseMoved(MouseEvent e){
 
 		}
 		@Override
 		public void mousePressed(MouseEvent e){
 			int mx = e.getX();
 			int my = e.getY();
 			if (mx > startButton.x && mx < startButton.x + startButton.width 
 					&& my > startButton.y && my <startButton.y + startButton.height && !preGame) {
 				preGame();
 			}
 			if (mx > contButton.x && mx < contButton.x + contButton.width 
 					&& my > contButton.y && my < contButton.y + contButton.height && preGame && !gameStarted) {
 				gameOver = false;
 				startGame();
 			}
 			if (mx > quitButton.x && mx < quitButton.x + quitButton.width 
 					&& my > quitButton.y && my < quitButton.y + quitButton.height && !preGame) {
 				System.exit(0);
 			}
 		}
 	}	
 
 }
