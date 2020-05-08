 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import sun.audio.*;
 import javax.swing.ImageIcon;
 import javax.swing.JPanel;
 
 
 public class Board extends JPanel implements Runnable {
 
 	/**
      * 
      */
 	private static final long serialVersionUID = -5438534791450927749L;
 
 	// Global Variables
 
 	private final int DELAY = 50;
 
 	// Thread for the Board Class to run in seperately from everything else
 	private Thread animator;
 
 	static Tower pendingTower;
 
 	Util.TrashType[] types = { Util.TrashType.paper, Util.TrashType.plastic };
 	private int TRASH_SPEED = 2;
 
 	// Images
 	private Image background;
 	private Image landFill;
 
 	// Fonts
 	private Font bigfont = new Font("Helvetica", Font.BOLD, 25);
 	private Font smallfont = new Font("Comic Sans", Font.BOLD, 16);
 
 	// Audio Player
 	AudioStream as;
 	private static Boolean muted = true;
 
 	/* Game State Variables */
 
 	// Game State Booleans
 	public static boolean inBetweenLevels = true;
 	private boolean ingame = true;
 
 	// Game State Variables
 	private Integer budget = 3000;
 	private Double airQual = 1000.0;
 	private Integer level = 1;
 	private Integer landFillScore = 0;
 
 	// Game State Lists
 	static ArrayList<ImageIcon> messages=new ArrayList<ImageIcon>();
 	ArrayList<Trash> trash = new ArrayList<Trash>();
 	ArrayList<Tower> towers = new ArrayList<Tower>(); // not sure if this should be static but am trying to add towers on button press
 	ArrayList<Integer> pathX = new ArrayList<Integer>();
 	ArrayList<Integer> pathY = new ArrayList<Integer>();
 
 	public Board() {
 
 		setDoubleBuffered(true);
 
 		makeBoard(pathX, pathY);
 
 		// My computer wants Board capitalized. -JJ
 		ImageIcon ii = new ImageIcon(this.getClass().getResource(
 				"pics/Board.png"));
 		background = ii.getImage();
 
 		ii = new ImageIcon(this.getClass().getResource("pics/landfill.png"));
 		landFill = ii.getImage();
 
 		WaveGen Wave = new WaveGen(20, 35, TRASH_SPEED, Util.pathPad, types);
 
 		trash = Wave.getWave();
 		messages=Wave.getMessages(level);
 		
 	}
 
 	public void addNotify() {
 		super.addNotify();
 		animator = new Thread(this);
 		animator.start();
 	}
 
 	public void makeBoard(ArrayList<Integer> pathX, ArrayList<Integer> pathY) {
 
 		pathX.add(0);
 		pathY.add(Util.pathPad);
 
 		pathX.add(Util.boardWidth - Util.pathPad - Util.pathWidth);
 		pathY.add(Util.pathPad);
 
 		pathX.add(Util.boardWidth - Util.pathPad - Util.pathWidth);
 		pathY.add(Util.pathPad + Util.pathHeight + Util.gapPad);
 
 		pathX.add(Util.pathPad);
 		pathY.add(Util.pathPad + Util.pathHeight + Util.gapPad);
 
 		pathX.add(Util.pathPad);
 		pathY.add(Util.pathPad + Util.pathHeight * 2 + Util.gapPad * 2);
 
 		pathX.add(Util.boardWidth - Util.pathPad - Util.pathWidth);
 		pathY.add(Util.pathPad + Util.pathHeight * 2 + Util.gapPad * 2);
 
 		pathX.add(Util.boardWidth - Util.pathPad - Util.pathWidth);
 		pathY.add(Util.boardHeight + Util.pathHeight);
 
 		pathX.add(-Util.pathWidth);
 		pathY.add(Util.boardHeight + Util.pathHeight);
 
 		pathX.add(-Util.pathWidth);
 		pathY.add(Util.pathPad);
 	}
 
 	public void paint(Graphics g) {
 		super.paint(g);
 		Graphics2D g2d = (Graphics2D) g;
 		g2d.drawImage(background, 0, 0, this);
 
 		if (ingame) {
 
 			g2d.drawImage(landFill, 0, 0, this);
 			// Draw each piece of trash in our trash array onto the frame
 			for (int i = 0; i < trash.size(); i++) {
 				Trash curTrash = trash.get(i);
 				g2d.drawImage(curTrash.getImage(), (int) curTrash.getX(),
 						(int) curTrash.getY(), this);
 			}
 
 			for (int i = 0; i < towers.size(); i++) {
 				Tower curTower = towers.get(i);
 				g2d.drawImage(curTower.getArmImage(), (int) curTower.getArmX(),
 						(int) curTower.getArmY(), this);
 				g2d.drawImage(curTower.getBaseImage(), (int) curTower.getX(),
 						(int) curTower.getY(), this);
 
 			}
 
 			// JJ
 			if (pendingTower != null
 					&& (pendingTower.getX() != Integer.MIN_VALUE || pendingTower
 							.getY() != Integer.MIN_VALUE)) {
 				g2d.drawImage(pendingTower.getBaseImage(),
 						(int) pendingTower.getX(), (int) pendingTower.getY(),
 						this);
 			}
 		
 
 		
 			if(inBetweenLevels)
 			{
 				if(messages.size()!=0)
 				g2d.drawImage(messages.get(0).getImage(),135,135
 						,this);
 			}
 
 		} else {
 			g2d.setFont(bigfont);
 			g2d.drawString("GAME OVER", 250, 300);
 
 		}
 		g2d.setFont(smallfont);
 		g2d.setColor(Color.RED);
 		g2d.drawString("Air Quality: " + airQual.toString() + " |  Budget: $"
 				+ budget.toString() + "  | Level: " + level.toString()
 				+ " | Trash Left: " + trash.size() + "00 lbs" + " | Landfill "
 				+ landFillScore.toString() + "% Full", 5, 615);
 
 		Toolkit.getDefaultToolkit().sync();
 		g.dispose();
 
 		if (!AudioPlayer.player.isAlive()) {
 			startMusic();
 		}
 	}
 
 	public static void startWave() {
 		inBetweenLevels = false;
 	}
 
 	public void removeMoney(int cost) {
 
 		budget -= cost;
 	}
 
 	public int getBudget() {
 		return budget;
 	}
 
 	public void startMusic() {
 		String path = System.getProperty("user.dir");
 		try {
 
 			path += "/Resources/audio/Menu.au";
 			InputStream in = new FileInputStream(path);
 			as = new AudioStream(in);
 			AudioPlayer.player.start(as);
 
 		} catch (FileNotFoundException e1) {
 			e1.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		muted = false;
 	}
 
 	public boolean isMuted() {
 		return muted;
 	}
 
 	public void stopMusic() {
 		AudioPlayer.player.stop((as));
 		muted = true;
 	}
 
 	public void run() {
 
 		long beforeTime, timeDiff, sleep;
 
 		beforeTime = System.currentTimeMillis();
 
 		int counter = 0;
 
 		// startMusic();
 
 		ingame = true;
 
 		while (true) {
 
 			while (trash.size() > 0) {
 				counter++;
 
 				long pause = 0;
 				while (inBetweenLevels) {
 					repaint();
 				}
 
 				for (int i = 0; i < trash.size(); i++) {
 
 					trash.get(i).followPath(pathX, pathY);
 
 					for (int j = 0; j < towers.size(); j++) {
 						// Windmills don't need to check for collisions
 						if (towers.get(j).getType() == Util.TowerType.windmill) {
 							break;
 						}
 						//towers.get(j).setFiring(true);
 						//System.out.println(!trash.get(i).isKilled() +" "+ !towers.get(j).getFiring() +" "+trash.get(i).detectCollisions(towers.get(j),pathX, pathY));
 						if (!trash.get(i).isKilled() && !towers.get(j).getFiring() && trash.get(i).detectCollisions(towers.get(j),pathX, pathY)) {
 							//System.out.println("SHIT");
 							calculateScore(towers.get(j),trash.get(i));
							towers.get(j).setFiring(true);
 							trash.get(i).setKilled();
 						}
 
						if (towers.get(j).getFireCounter() >= Util.pathWidth && trash.get(i).isKilled()) {
							trash.get(i).removeImage();
						}
 
 					}
 					
 
 					// Check to make sure trash hasn't exited the board
 					if (trash.get(i).getY() + 30 > Util.boardHeight) {
 						trash.remove(i);
 						landFillScore += 1;
 					}
 					if(trash.get(i).getImage()==null){
 						trash.remove(i);
 					}
 				}
 				
 				for(int i=0; i<towers.size();i++){
 					if (towers.get(i).getFiring()){
 						towers.get(i).fire();
 					}
 				}
 
 				repaint();
 
 				timeDiff = System.currentTimeMillis() - beforeTime;
 				sleep = DELAY - timeDiff;
 
 				if (sleep < 0)
 					sleep = 2;
 				try {
 					Thread.sleep(sleep);
 				} catch (InterruptedException e) {
 					System.out.println("interrupted");
 				}
 
 				beforeTime = System.currentTimeMillis();
 			}
 
 			// Wave has now ended
 			//resetTowers();
 
 			// Check for windmills
 			for (int g = 0; g < towers.size(); g++) {
 				if (towers.get(g).getType() == Util.TowerType.windmill) {
 					budget += 150;
 				}
 			}
 
 			repaint();
 			level++;
 
 			WaveGen Wave = new WaveGen(48, 35, 1, Util.pathPad, types);
 			trash = Wave.getWave();
 			messages=Wave.getMessages(level);
 			inBetweenLevels = true;
 
 			if (level > 5) {
 				break;
 			}
 
 		}
 
 		ingame = false;
 
 	}
 	
 	private void calculateScore(Tower tower, Trash trash){
 		
 		if(tower.type==Util.TowerType.incenerator){
 			budget+=15;
 			airQual-=15;
 		}else{
 			budget+=25;
 		}
 		
 		
 	}
 
 	private void resetTowers() {
 		for (int i = 0; i < towers.size(); i++) {
 			towers.get(i).resetTower();
 		}
 	}
 
 	// Method to determine if the user is adding the tower on top of the path
 	public boolean inPath(int x, int y) {
 
 		// Inside actual game board
 		if ((x > 0 && x > Util.boardWidth) || (y > 0 && y > Util.boardHeight)) {
 			return true;
 		}
 
 		// First row
 		if ((x > 0 && x < Util.boardWidth - Util.pathPad
 				+ (Util.towerWidth / 2))
 				&& (y > Util.pathPad - (Util.towerWidth / 2) && y < Util.pathPad
 						+ Util.pathWidth + (Util.towerWidth / 2))) {
 			return true;
 			// First down
 		} else if ((x < Util.boardWidth - Util.pathPad + (Util.towerWidth / 2) && x > (Util.boardWidth
 				- Util.pathPad - Util.pathWidth - (Util.towerWidth / 2)))
 				&& (y > Util.pathPad - (Util.towerWidth / 2) && (y < Util.pathPad
 						+ Util.gapPad + (Util.towerWidth / 2)))) {
 			return true;
 			// First Switch Back
 		} else if (x < Util.boardWidth - Util.pathPad + (Util.towerWidth / 2)
 				&& x > Util.pathPad - (Util.towerWidth / 2)
 				&& y > Util.pathPad + Util.pathWidth + Util.gapPad
 						- (Util.towerWidth / 2)
 				&& y < Util.pathPad + (2 * Util.pathWidth) + Util.gapPad
 						+ (Util.towerWidth / 2)) {
 			return true;
 			// Second Down
 		} else if (x > Util.pathPad - (Util.towerWidth / 2)
 				&& x < Util.pathPad + Util.pathWidth + (Util.towerWidth / 2)
 				&& y > Util.pathPad + Util.pathWidth + Util.gapPad
 						- (Util.towerWidth / 2)
 				&& y < Util.pathPad + 2 * Util.pathWidth + 2 * Util.gapPad
 						+ (Util.towerWidth / 2)) {
 			return true;
 			// Second Switch Back
 		} else if (x > Util.pathPad - (Util.towerWidth / 2)
 				&& x < Util.boardWidth - Util.pathPad + (Util.towerWidth / 2)
 				&& y > Util.pathPad + 2 * Util.pathWidth + 2 * Util.gapPad
 						- (Util.towerWidth / 2)
 				&& y < Util.pathPad + (3 * Util.pathWidth) + 2 * Util.gapPad
 						+ (Util.towerWidth / 2)) {
 			return true;
 			// Third Down
 		} else if ((x < Util.boardWidth - Util.pathPad + (Util.towerWidth / 2) && x > (Util.boardWidth
 				- Util.pathPad - Util.pathWidth - (Util.towerWidth / 2)))
 				&& y > Util.pathPad + (3 * Util.pathWidth) + 2 * Util.gapPad
 						+ (Util.towerWidth / 2)) {
 			return true;
 		}
 
 		return false;
 	}
 
 	// JJ
 	public void addTower(Tower t) {
 		towers.add(t);
 	}
 
 	public static void addPendingTower(Tower t) {
 		pendingTower = t;
 	}
 
 	public static void removePendingTower() {
 		pendingTower = null;
 	}
 
 	public int getLevel() {
 		return level;
 	}
 }
