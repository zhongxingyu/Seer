 package game;
 
 /**
  * Client implementation of the game state
  * 
  * @author Jacob Charles
  */
 import java.awt.Color;
 import java.awt.Graphics;
 
 public class ClientGameState extends GameState {
 	
 	/**
 	 * Generic constructor
 	 */
 	public ClientGameState(){
 		super();
 	}
 	
 	/**
 	 * Clone constructor from generic GameState
 	 * 
 	 * @param g
 	 * 		GameState to clone from
 	 */
 	public ClientGameState(GameState g){
 		super(g);
 	}
 	
 	/**
 	 * Draw everything
 	 * 
 	 * @param g
 	 */
 	public void draw(Graphics g) {
 		drawBackground(g);
 		drawLevel(g);
 		drawFighters(g);
 		drawBullets(g);
 		//TODO: Draw items and special effects
 		drawStatus(g);
 	}
 	
 	/**
 	 * Draw a background
 	 * 
 	 * @param g
 	 * 		graphics object to draw through
 	 */
 	private void drawBackground(Graphics g) {
 		//TODO: Clear with a background image instead
 		g.setColor(Color.WHITE);
 		g.fillRect(0, 0, WIDTH, HEIGHT);
 	}
 	
 	/**
 	 * Draw the fighters
 	 * 
 	 * @param g
 	 * 		graphics object to draw through
 	 */
 	private void drawFighters(Graphics g) {
 		for (Actor a : getFighters()) {
 			draw(a, g);
 		}
 	}
 	
 	/**
 	 * Draw the level objects
 	 * 
 	 * @param g
 	 * 		graphics object to draw through
 	 */
 	private void drawLevel(Graphics g) {
 		for (Land l : getLevel()) {
 			draw(l, g);
 		}
 	}
 	
 	/**
 	 * Draw the projectiles
 	 * 
 	 * @param g
 	 * 		graphics object to draw through
 	 */
 	private void drawBullets(Graphics g) {
 		for (Shot s : getBullets()) {
 			draw(s, g);
 		}
 	}
 	
 	/**
 	 * Draw character statuses
 	 * 
 	 * @param g
 	 * 		graphics object to draw through
 	 */
 	private void drawStatus(Graphics g) {
 		for (int i = 0; i < getNumberOfPlayers(); i++) {
 			g.setColor(Color.BLACK);
 			g.fillRoundRect(40+i*WIDTH/4, 10, 50, 35, 10, 10);
 			g.setColor(Color.WHITE);
 			if (getMode() == STOCK) {
 				g.drawString("x"+getPlayer(i).getLives(), 70+i*WIDTH/4, 35);
 			}
 			if (getMode() == TIME) {
 				g.drawString(" "+getPlayer(i).getScore(), 70+i*WIDTH/4, 35);
 			}
 			//draw the character's current state in box
 			draw(getPlayer(i), 48+i*WIDTH/4, 21, g);
 		}
 		if (getMode() == TIME) {
 			//TODO: Adjust font and centering
 			g.setColor(Color.BLACK);
 			g.fillRoundRect(WIDTH/2-25, 10, 50, 35, 10, 10);
 			g.setColor(Color.WHITE);
 			int sec = getTimeLeft()/50; //TODO: Keep this accurate to the frame rate
 			int min = sec/60;
 			sec %= 60;
 			g.drawString(min+":"+sec, WIDTH/2-20, 35);
 		}
 	}
 	
 	/**
 	 * Draw an actor to the designated graphics object
 	 * 
 	 * @param a
 	 * 		Actor to be drawn
 	 * @param g
 	 * 		graphics object to draw through
 	 */
 	private void draw(Actor a, Graphics g) {
 		//TODO: Make this draw an image with transparency instead
 		//don't draw dead guys
 		if (a.isDead()) return;
 		//make invulnerable characters blink
 		if (a.isArmored() && a.getDeadTime() % 8 < 4) return;
 		
		//temporary colors
 		Color c[] = {Color.GREEN, Color.BLUE, Color.YELLOW, Color.BLACK, Color.MAGENTA, Color.LIGHT_GRAY};
 		g.setColor(c[a.getSkin()]);
 		
 		//temporary shape
 		g.fillOval((int)a.getLeftEdge(), (int)a.getTopEdge(), 16, 16);
 		g.fillRect((int)a.getHCenter(), (int)a.getTopEdge(), -a.getW()*a.getDir()/2, a.getH());
		g.fillRect((int)a.getLeftEdge(), (int)a.getTopEdge(), a.getW()*a.getDir()/2, a.getH());
 	}
 	
 	/**
 	 * Draw an actor to the designated graphics object
 	 * 
 	 * @param a
 	 * 		Actor to be drawn
 	 * @param g
 	 * 		graphics object to draw through
 	 */
 	private void draw(Actor a, int x, int y, Graphics g) {
 		//TODO: Make this draw an image with transparency instead
 		
 		//temporary colors
 		Color c[] = {Color.GREEN, Color.BLUE, Color.YELLOW, Color.BLACK, Color.MAGENTA, Color.LIGHT_GRAY};
 		g.setColor(c[a.getSkin()]);
 		
 		//temporary shape
 		g.fillOval(x, y, 16, 16);
 		g.fillRect((int)x+a.getW()/2, y, -a.getW()*a.getDir()/2, a.getH());
 		g.fillRect(x, y, a.getW()*a.getDir()/2, a.getH());
 	}
 
 	/**
 	 * Draw a land to the designated graphics object
 	 * 
 	 * @param l
 	 * 		Land to be drawn
 	 * @param g
 	 * 		graphics object to draw through
 	 */
 	private void draw(Land l, Graphics g) {
 		//TODO: Draw composite images instead
 		g.setColor(Color.BLACK);
 		if (l.isDanger()) {
 			g.setColor(Color.RED);
 			g.fillRect((int)l.getLeftEdge(), (int)l.getTopEdge(), l.getW()+1, l.getH()+1);
 		}
 		else if (l.isBounce()) {
 			g.setColor(Color.GREEN);
 			g.fillRoundRect((int)l.getLeftEdge(), (int)l.getTopEdge(), l.getW()+1, l.getH()+1, 12, 12);
 		}
 		else if (l.isSolid()) {
 			g.fillRect((int)l.getLeftEdge(), (int)l.getTopEdge(), l.getW()+1, l.getH()+1);
 		}
 		else if (l.isPlatform()) {
 			g.drawRect((int)l.getLeftEdge(), (int)l.getTopEdge(), l.getW()+1, l.getH()+1);
 		}
 	}
 	
 	/**
 	 * Draw a shot to the designated graphics object
 	 * 
 	 * @param s
 	 * 		Shot to be drawn
 	 * @param g
 	 * 		graphics object to draw through
 	 */
 	private void draw(Shot s, Graphics g) {
 		g.setColor(Color.RED);
 		g.fillRect((int)s.getLeftEdge(), (int)s.getTopEdge(), s.getW()+1, s.getH()+1);
 	}
 }
