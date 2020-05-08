 
 import java.awt.Image;
 import java.awt.MediaTracker;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.sound.midi.MidiSystem;
 import javax.sound.midi.Sequencer;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.swing.JOptionPane;
 import javax.swing.Timer;
 
 import javax.swing.JFrame;
 
 public class Bomberman extends JFrame implements KeyListener, ActionListener {
 	private static final long serialVersionUID = 1L;
 	boolean isRunning;
 	boolean isFinished;
 	boolean music = true;
 	boolean sound = true;
 	Stage stage;
 	Player player;
 	List<Bomb> bombList;
 	Player player2;
 	Enemy enemy;
 	Timer timer;
 	List<Integer> keyCodes;
 	List<Integer> keyReleasedCodes;
 	Sequencer backgroundSequencer;
 	Map<String, Image> imageMap;
 	Map<String, URL> musicMap;
 	Map<String, URL> soundMap;
 	public Bomberman() {
 		setTitle("Bomberman");
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		
 		
 		setJMenuBar(new MenuBar(this));
 		
 		stage = new Stage(this);
 		add(stage);
 
 		setResizable(false);
 		pack();
 		setLocationRelativeTo(null);
 		setVisible(true);
 		
 		// Daten laden
 		loadImages();
 		loadMusic();
 		loadSounds();
 		
 		// Spieler hinzufgen
 		player = new Player(this,1);
 		player2 = new Player(this,2);
 		
 		// Bomben hinzufgen
 		bombList = new ArrayList<Bomb>();
 		for (int i = 0; i < 8; i++)
 			bombList.add(new Bomb(this));
 		
 		// 25ms Timer hinzufgen
 		timer = new Timer(25, this);
 		timer.start();
 		
 		// Sequencer fr Hintergrundmusik
 		try {
 			backgroundSequencer = MidiSystem.getSequencer();
 			backgroundSequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
 			backgroundSequencer.open();
 		}
 		catch (Exception ex) {}
 		
 		showStartScreen();
 		
 		keyCodes = new ArrayList<Integer>();
 		keyReleasedCodes = new ArrayList<Integer>();
 		
 		addKeyListener(this);
 		
 		
 	}
 	
 	/**
 	 * Zeigt den Startbildschirm an
 	 */
 	public void showStartScreen() {
 		isRunning = false;
 		stopMusic();
 		if(music)playMusic(musicMap.get("start"));
 		else stopMusic();
 	}
 	
 	/**
 	 * Ldt alle Bilder in eine Map
 	 */
 	public void loadImages() {
 		imageMap = new HashMap<String, Image>();
 
 		imageMap.put("start", getToolkit().getImage(getClass().getClassLoader().getResource("res/Bom.jpg")));
 		imageMap.put("block", getToolkit().getImage(getClass().getClassLoader().getResource("res/block.jpg")));
 		imageMap.put("floor", getToolkit().getImage(getClass().getClassLoader().getResource("res/Level.jpg")));
 		imageMap.put("player_left", getToolkit().getImage(getClass().getClassLoader().getResource("res/m2.jpg")));
 		imageMap.put("player_right", getToolkit().getImage(getClass().getClassLoader().getResource("res/m1.jpg")));
 		imageMap.put("player2_left", getToolkit().getImage(getClass().getClassLoader().getResource("res/m21.png")));
 		imageMap.put("player2_right", getToolkit().getImage(getClass().getClassLoader().getResource("res/m11.png")));
 		imageMap.put("bomb", getToolkit().getImage(getClass().getClassLoader().getResource("res/bomb.png")));
 		imageMap.put("explosion", getToolkit().getImage(getClass().getClassLoader().getResource("res/b2.png")));
 		imageMap.put("gate", getToolkit().getImage(getClass().getClassLoader().getResource("res/Gate2.png")));
 		imageMap.put("box", getToolkit().getImage(getClass().getClassLoader().getResource("res/box.jpg")));
 		imageMap.put("bup", getToolkit().getImage(getClass().getClassLoader().getResource("res/bup.jpg")));
 		imageMap.put("pup", getToolkit().getImage(getClass().getClassLoader().getResource("res/pup.jpg")));
 		
 		// Bilder vorladen
 		MediaTracker mTracker = new MediaTracker(this);
 		int i = 0;
 		for (Image image : imageMap.values()) {
 			mTracker.addImage(image, i);
 			i++;
 		}
 		try {
 			mTracker.waitForAll();
 		}
 		catch (Exception ex) {}
 	}
 	
 	/**
 	 * Ldt alle Musikdateien in eine Map
 	 */
 	public void loadMusic() {
 		musicMap = new HashMap<String, URL>();
 		
 		try {
 			musicMap.put("start", getClass().getClassLoader().getResource("res/play3.mid"));
 			musicMap.put("game", getClass().getClassLoader().getResource("res/play2.mid"));
 		}
 		catch (Exception ex) {}
 	}
 	
 	/**
 	 * Ldt alle Sounddateien in eine Map
 	 */
 	public void loadSounds() {
 		soundMap = new HashMap<String, URL>();
 		
 		try {			
 			soundMap.put("bomb", getClass().getClassLoader().getResource("res/bomb.wav"));
 			soundMap.put("ticktock", getClass().getClassLoader().getResource("res/ticktock.wav"));
 		}
 		catch (Exception ex) {}
 	}
 	
 	/**
 	 * Spielt eine eine Musikdatei im Hintergrund ab
 	 * @param file Musikdatei
 	 */
 	public void playMusic(URL url) {
 		try {
 			backgroundSequencer.setSequence(MidiSystem.getSequence(url));
 			backgroundSequencer.start();
 	        
 		} catch(Exception ex) {}
 	}
 	
 	/**
 	 * Stopt die Hintergrundmusik
 	 */
 	public void stopMusic() {
 		backgroundSequencer.stop();
 	}
 	
 	/**
 	 * Spielt eine Sounddatei ab
 	 * @param file Sounddatei
 	 */
 	public void playSound(URL url) {
 		try {
 	        Clip clip = AudioSystem.getClip();
 	        clip.stop();
 	        clip.open(AudioSystem.getAudioInputStream(url));
 	        clip.setFramePosition(0);
 	        clip.start();
 	        
 		}
 		catch (Exception ex) {}
 	}
 	
 	
 	/**
 	 * Startet das Spiel
 	 * @param mit wie vielen spielern das spiel gestartet wird(max 2 im moment)
 	 */
 	public void startGame(int playerCount) {		
 		if(music) playMusic(musicMap.get("game"));
 		else stopMusic();
 		//enemy.exists = true;
 		isFinished = false;
 		isRunning = true;
 		player.setActiveBombs(0);
 		player2.setActiveBombs(0);
 		
 		player.isDead = false;
 		player2.isDead = true;
 		if(playerCount == 2){
 		player2.isDead = false;
 		}
 		
 		for (Bomb bomb : bombList) {
 			bomb.isVisible = false;
 			bomb.isExploded = false;
 		}
 		
 		stage.loadStage("res/level02x.txt");
 		
 		stage.repaint();
 	}
 	
 	/**
 	 * Beendet das Spiel
 	 */
 	public void endGame() {
 		keyCodes.clear();
 		keyReleasedCodes.clear();
 		stopMusic();
 		stage.repaint();
 		isFinished = true;
 		Bomb.radius = 4;     // setzt Power-ups zurxk
 		player.maxBombs = 1;   // "
 		Object[] options = { "1 Player", "2 Players", "Main Menu" };
 		int dialogResult = JOptionPane.showOptionDialog(this, "Noch ein Spiel?", (isFinished)?"Gewonnen!":"Verloren ...", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
 		if (dialogResult == JOptionPane.YES_OPTION) {
 			startGame(1);
 		}
 		else if (dialogResult == JOptionPane.NO_OPTION) {
 			startGame(2);
 		}
 		else {
 			showStartScreen();
 		}
 	}
 	
 	public void keyPressed(KeyEvent e) {
 		Integer key = new Integer(e.getKeyCode());
 		if (!keyCodes.contains(key))
 			keyCodes.add(key);
 	}
 	
 	public void keyReleased(KeyEvent e) {
 		Integer key = new Integer(e.getKeyCode());
 		if (keyCodes.contains(key)){
 			keyCodes.remove(key);
 			if (!keyReleasedCodes.contains(key))
 			keyReleasedCodes.add(key);
 		}
 		
 	}
 	
 	public void keyTyped(KeyEvent e) {
 	}
 	
 	/**
 	 * Prft ob die Taste keyCode gerade gedrckt wird
 	 * @param keyCode Taste
 	 * @return
 	 */
 	public boolean isPressing(int keyCode) {
 		for (Integer key : keyCodes) {
 			if (key.equals(keyCode)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Prft ob die Taste keyCode gedrckt und losgelassen wurde
 	 * @param keyCode Taste
 	 * @return
 	 */
 	 public boolean hasReleased(int keyCode){
 		 for(Integer key : keyReleasedCodes){
 			 if(key.equals(keyCode)){
 				 keyReleasedCodes.remove(key);
 				 return true;
 			 }
 				 
 		 }
 		 return false;
 	 }
 	
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == timer && isRunning && !isFinished) {
 			// Wenn das Gate erreicht wurde
 			if (stage.isPointOnField(player.getStagePosition(), Stage.GATE)) {
 				endGame();
 			}
 			// Alle Bomben prfen
 			for (Bomb bomb : bombList) {
 				bomb.process();
 				if (bomb.isVisible && bomb.isExploded) {
 					for (Point p : bomb.explosionArray) {
 						// Wenn der Spieler in der Explosion ist
 						if (player.getStagePosition().equals(p)) {
 							player.isDead = true;
 							endGame();
 							break;

 						}else if (player2.getStagePosition().equals(p) && !player2.isDead) {
 								player2.isDead = true;
 								endGame();
 								break;
 						}
 						
 						// Kettenreaktion der bomben
 						for (Bomb bomb2 : bombList) {
 							if(bomb2.isVisible && !bomb2.isExploded && p.equals(bomb2.position)){
 								bomb2.count = 2000/25;
 							}
 						}
 						
 					}
 				}
 			}
 			if (hasReleased(KeyEvent.VK_B)) {
 				/// Bombe legen
 				
 					for (Bomb bomb : bombList) {
 						if (!bomb.isVisible && player.getActiveBombs() < player.maxBombs && bomb.canLayOn(player.getStagePosition())) {
 							bomb.setToPlayerPos();
 							bomb.setOwner(player);
 							player.setActiveBombs(player.getActiveBombs()+1);
 							break;
 						}
 					}
 				
 			}
 			
 			if (hasReleased(KeyEvent.VK_L)) {
 				/// Bombe legen
 				if(player2.isDead == false) {
 					for (Bomb bomb : bombList) {
 						if (!bomb.isVisible && player2.getActiveBombs() < player2.maxBombs && bomb.canLayOn(player2.getStagePosition())) {
 							bomb.setToPlayer2Pos();
 							bomb.setOwner(player2);
 							player2.setActiveBombs(player2.getActiveBombs()+1);
 							break;
 						}
 					}
 				}
 			}
 			/*
 			 *  Spieler Steuerung
 			 */
 			
 			if (isPressing(KeyEvent.VK_A)) {
 				player.moveLeft();
 			}
 			if (isPressing(KeyEvent.VK_D)) {
 				player.moveRight();
 			}
 			if (isPressing(KeyEvent.VK_W)) {
 				player.moveUp();
 			}
 			if (isPressing(KeyEvent.VK_S)) {
 				player.moveDown();
 			}
 			
 			//player 2
 			
 				
 
 			if (isPressing(KeyEvent.VK_LEFT)) {
 				player2.moveLeft();
 			}
 			if (isPressing(KeyEvent.VK_RIGHT)) {
 				player2.moveRight();
 			}
 			if (isPressing(KeyEvent.VK_UP)) {
 				player2.moveUp();
 			}
 			if (isPressing(KeyEvent.VK_DOWN)) {
 				player2.moveDown();
 				
 			}
 			
 			// Feld neu zeichnen
 			stage.repaint();
 		}
 		
 	}
 	
 	public static void main(String args[]) {
 		new Bomberman();
 	}
 }
