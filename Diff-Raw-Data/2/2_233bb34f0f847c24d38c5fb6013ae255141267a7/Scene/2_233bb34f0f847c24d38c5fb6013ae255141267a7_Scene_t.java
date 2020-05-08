 package states;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Random;
 
 import javax.swing.JOptionPane;
 
 import sound.AudioClip;
 import sound.ResourceLoader;
 import actors.Bike;
 import actors.Player;
 import background.Background;
 import core.Actor;
 import core.LevelLoader;
 import core.ScoreManager;
 import core.State;
 import core.StateManager;
 import foreground.Foreground;
 import global.GlobalSettings;
 
 /**
  * Scene class. -/-Base class for Scenes to hold and display actors.-/-
  * update 1: Class to represent the main panel.
  */
 @SuppressWarnings("serial")
 public class Scene extends State {
 
 	
 	
 	// classic mode
 	public boolean classic_mode = false;
 	
 	// initial scroll speed
 	//0.012
 	private double xscrollspeed = 0.010;
 	// scroll increment each round
 	// 0.0025
 	private double xscrollinc = 0.0000;
 	// steps to perform scrolling in (for collision)
 	private double xscrollsteps = 0.002;
 	// value that holds current scroll speed (for stepwise movement)
 	private double xscrolltmp = 0;
 	
 	// with of the scene
 	public double xsize = 2.3;
 	
 	private long score = 0;
 	private int scoredivisor = 10;
 	
 	// level nr.
 	private int round = 1;
 	// if game is paused (actually if player is dead)
 	private boolean paused = false;
 	// if the game is actually paused
 	private boolean stopped = false;
 
 	private boolean isSpacePressed = false;
 	
 	
 	private double update_sec = 0;
 	private int frames = 0;
 	private double five_sec = 0;
 	private double diff = 0;
 
 	
 	private Background bg;
 	private Foreground fg;
 	private Actor player;
 	private LevelLoader lloader = null;
 	
 	private AudioClip backgroundmusic = null;
 	
 	private BufferedImage img_rse = (BufferedImage) ResourceLoader.load("res/menu/l_rse.png");
 	
 	private Random rand = new Random(184877+System.currentTimeMillis());
 	
 	private Color backgroundcolor = new Color(180, 220, 250);
 	
 	public Scene(StateManager parent, GlobalSettings settings) {
 		super(parent, settings);
 		
 		final Scene s = this;
 		
 		this.addKeyListener(new KeyListener() {
 			@Override
 			public void keyTyped(KeyEvent e) {
 			}
 			@Override
 			public void keyPressed(KeyEvent e) {
 				switch(e.getKeyCode()) {
 				case KeyEvent.VK_P: {
 					// pause/unpause the game
 					if(!stopped)
 						stopped = true;
 					else stopped = false;
 				}break;
 				case KeyEvent.VK_C: {
 					// toggle classic mode
 					if(!classic_mode)
 						classic_mode = true;
 					else classic_mode = false;
 				}break;
 				case KeyEvent.VK_R: {
 					// reset
 					resetGame();
 				}break;
 				case KeyEvent.VK_K: {
 					paused = true;
 					((Player)player).kill();
 				}break;
 				case KeyEvent.VK_M: {
 					// music mute
 					if(!s.settings.getMusicMuted()) {
 						s.settings.setMusicMuted(true);
 						if(backgroundmusic!=null)backgroundmusic.stop();
 					}
 					else {
 						s.settings.setMusicMuted(false);
 						playSong();
 					}
 				}break;
 				case KeyEvent.VK_N: {
 					if(!s.settings.getSoundeffectsMuted()) {
 						s.settings.setSoundeffectsMuted(true);
 					}
 					else {
 						s.settings.setSoundeffectsMuted(false);
 					}
 				}break;
 				case KeyEvent.VK_E: {
 					if(backgroundmusic!=null)backgroundmusic.stop();
 					s.parent.popState();
 				}break;
 				case KeyEvent.VK_S: {
 					if(paused) {
 						//backgroundmusic.stop();
 						stopped = true;
 						
 						StringBuilder score = new StringBuilder();
 						int place = ScoreManager.testScore((int)getScore());
 						if(place > 0) {
 							score.append("Place "+place+"");
 							if(place == 1) score.append(" - HIGHSCORE!");
 							else score.append("!");
 							score.append(" Input your name: ");
 							String a = JOptionPane.showInputDialog(s.parent, score.toString(), "Score", JOptionPane.QUESTION_MESSAGE);
 							if(a != null && a.length() > 0) {
 								ScoreManager.setScore(a, (int)getScore());
								if(backgroundmusic!=null)backgroundmusic.stop();
 								s.parent.popState();
 							}
 						}
 						else JOptionPane.showMessageDialog(s.parent, "Your score isn't high enough for the scoreboard.", "Score", JOptionPane.INFORMATION_MESSAGE);
 					}
 				}break;
 				default: {
 					// jump
 					isSpacePressed = true;
 					if(paused||stopped) {
 					}
 					else {
 						((Player)player).jump();
 					}
 				}break;
 				}
 			}
 			@Override
 			public void keyReleased(KeyEvent e) {
 				isSpacePressed = false;
 			}
 		});
 		
 		initGame();
 		
 		this.addActor(new Actor(this, 0.3, 0, 0.5,0.5){
 			private BufferedImage bimage = ( BufferedImage) ResourceLoader.load("res/bg/n.png");
 			@Override public void paintComponent(Graphics g) {
 				((Graphics2D)g).drawImage(bimage ,parent.getCoordX(x), parent.getCoordY(y), parent.getWidth(w), parent.getHeight(h), null);
 			}
 		});
 		addActor(new Bike(this, 0.8, 0.77));
 		
 		if(!settings.getMusicMuted()) playSong();
 	}
 	
 	private void playSong() {
 		backgroundmusic = (AudioClip) ResourceLoader.load("res/sound/bgm"+(rand.nextInt(4)+1)+".wav", false);
 		backgroundmusic.start();
 	}
 	
 	/**
 	 * Initialize the game.
 	 */
 	public void initGame() {		
 		xposition = 0;
 		childs = new HashSet<>();
 		bg = new Background(this);
 		fg = new Foreground(this);
 		round = 1;
 		fg.setSecretProbability(0.04);
 		setScore(0);
 		xscrollspeed = 0.01;
 		lloader = new LevelLoader(this, "res/levels/");
 		lloader.start();
 		player = new Player(this);
 		paused = false;
 		stopped = false;
 	}
 	/**
 	 * Reset the game.
 	 */
 	public void resetGame() {
 		// method 1?
 		//((StateManager)parent).replaceState(new Scene(parent, settings));
 		// method 2
 		initGame(); 
 	}
 	
 	/**
 	 * Add to the score.
 	 * @param add
 	 * The value to be added to the score.
 	 */
 	public void addScore(long add) {
 		score += add * scoredivisor;
 		if(!settings.getSoundeffectsMuted()) ((AudioClip) ResourceLoader.load("res/coin.wav")).start();
 	}
 	/**
 	 * Returns the score.
 	 * @return
 	 * The current score.
 	 */
 	public double getScore() {
 		return new Double(score) / scoredivisor;
 	}
 	/**
 	 * Sets the score.
 	 * @param score
 	 * The current score.
 	 */
 	public void setScore(long score) {
 		this.score = score * scoredivisor;
 	}
 	
 	/**
 	 * Returns the x scroll speed.
 	 * @return
 	 * The x scroll speed, scroll step dependent.
 	 */
 	public double getScrollSpeed() { return xscrolltmp ;}//<xscrollsteps?xscrolltmp:xscrollsteps; }
 	/**
 	 * Returns the static x scroll speed.
 	 * @return
 	 * The x static scroll speed, scroll step independent. 
 	 */
 	public double getStaticScrollSpeed() { return this.xscrollspeed; }
 	/**
 	 * Sets the static x scroll speed.
 	 */
 	public void setStaticScrollSpeed(double xs) { this.xscrollspeed = xs; }
 
 	/**
 	 * Returns if space (jump) is pressed.
 	 * @return
 	 * If space (jump) is pressed.
 	 */
 	public boolean getSpaceState(){
 		return isSpacePressed;
 	}
 	
 	/**
 	 * Returns if game is paused/over.
 	 * @return
 	 * If game is paused/over.
 	 */
 	public boolean getPaused() {
 		return paused;
 	}
 	
 	@Override
 	public void paintComponent(Graphics g) {
 		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);	
 		
 		if(!settings.getMusicMuted()) {
 			if(!backgroundmusic.isRunning()) playSong();
 		}
 		
 		
 		//--clear bg--
 		g.setColor(backgroundcolor);
 		g.fillRect(0, 0, getWidth(), getHeight());
 		//--/clear bg--
 		
 		//--update--
 		if(!paused && !stopped) {
 			
 			score += 1;
 			
 			// Load next level when scrolled past the current one.
 			if(xposition + getXWidth() >= xsize + 0.1) {
 				lloader.start();
 				xscrollspeed += xscrollinc;
 				round += 1;
 			}
 			
 			// fixed update
 			{
 				// update the actors (movement)
 				player.fixedUpdate();
 				
 				for(Actor c: childs) {
 					if(c.x > xposition - 1 && c.x < xposition + getXWidth()) { // only update near actors
 						c.fixedUpdate();
 					}
 				}
 			}
 		
 			// smooth fast movement so intersections aren't skipped
 			// (notice this might cause lag at high movement speed)
 			
 			int z = (int)(xscrollspeed / xscrollsteps);
 			double r = xscrollspeed%xscrollsteps; 
 			
 			for(int i = 0; i < z; ++i) {
 				xscrolltmp = xscrollsteps;
 				xposition += xscrollsteps;
 				
 				if(i == z-1) {
 					xscrolltmp += r;
 					xposition += r;
 				}
 
 				// update the actors (movement)
 				player.update();
 				
 				
 				for(Actor c: childs) {
 					if(c.x > xposition - 1 && c.x <= xposition + getXWidth()) { // only update near actors
 						c.update();
 					}
 				}
 				if(((Player)player).dead) {
 					paused = true;
 					break;
 				}
 			}
 			xscrolltmp = 0;
 			
 			bg.update();
 			fg.update();
 		}
 		//--/update--
 		
 		//--paint--
 		{
 			{
 				
 				g.setColor(Color.red);
 				((Graphics2D)g).drawString("Round: "+round, 10, 20);
 				((Graphics2D)g).drawString("Speed:  "+((int)(xscrollspeed*1000.0)) , 10, 40);
 				
 				StringBuilder score = new StringBuilder();
 				score.append("Score:   "+getScore());
 				int place = ScoreManager.testScore((int)getScore());
 				if(place > 0) {
 					score.append(" - Place "+place);
 					if(place == 1) score.append(" HIGHSCORE!");
 				}
 				((Graphics2D)g).drawString(score.toString(), 10, 60);
 			}
 			
 			if(!classic_mode) bg.paintComponent(g); // paint background
 			
 			player.paintComponent(g); // paint player
 			{
 				ArrayList<Actor> removees = new ArrayList<>();
 				for(Actor c: childs) {
 					if(c.x + c.w < xposition) {
 						removees.add(c);
 					}
 					else if(c.x > xposition - getXWidth() && c.x < xposition + getXWidth() + c.w) // only paint near actors
 						c.paintComponent(g); // paint level
 				}
 				for(Actor rem: removees) {
 					childs.remove(rem); // remove actors that are out of view (<|)
 				}
 				removees.clear();
 			}
 			
 			if(!classic_mode) fg.paintComponent(g); // paint foreground
 			
 			((Graphics2D) g).drawString("1.0.1-final", getCoordXFixed(0.85), getCoordY(0.9));
 			if(paused) {
 				g.setColor(Color.red);
 				//((Graphics2D) g).drawString("GAME OVER", getCoordXFixed(0.48), getCoordY(0.48));
 				
 				((Graphics2D) g).drawImage(img_rse, this.getCoordXFixed(0.5)-getWidth(0.5)/2, getCoordY(0.0), getWidth(0.5), getHeight(0.5), null);
 			}
 			
 			
 			double ct = System.currentTimeMillis();
 			diff += (ct - update_sec);
 			update_sec = ct;
 			++frames;
 			if(frames == 5) {
 				five_sec = diff / 5;
 				diff = 0;
 				frames = 0;
 			}
 			g.setColor(Color.red);
 			((Graphics2D)g).drawString("FPS:      "+(double)((int)((1000/five_sec)*100))/100, 10, 80);
 			
 		}
 		
 		//--/paint--
 	}
 }
