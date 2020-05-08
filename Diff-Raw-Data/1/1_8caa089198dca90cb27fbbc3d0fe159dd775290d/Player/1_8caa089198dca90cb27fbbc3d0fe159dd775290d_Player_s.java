 package mapobjects;
 
 import imageloader.ImageLoader;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.util.Vector;
 
 import mapobjects.Upgrade.CMListener;
 
 /**
  * Player object is the player controlled by the player
  * 
  * @author eik
  * 
  */
 public class Player extends MoveableObject implements CMListener {
 	/**
 	 * player id , for multiplayer
 	 */
 	private int ID;
 	/**
 	 * Variable used to determine if this playerobject is controlled remotely
 	 */
 	private boolean remotePlayer = false;
 	/**
 	 * maximal number of bombs that the player could use
 	 */
 	private int maxbombs = 1;
 	/**
 	 * number of active bombs on the playground
 	 */
 	private int currentbombs = 0;
 	/**
 	 * radius of the bomb
 	 */
 	private int bombradius = 1;
 	/**
 	 * set if player is alive
 	 */
 	private boolean alive;
 	/**
 	 * Set true if upgrade bomb remote has been collected.
 	 */
 	private boolean bombRemote = false;
 
 	private boolean shieldProtection = false, immortal = false;
 	/**
 	 * List of currently planted bombs
 	 */
 	private Vector<Bomb> remoteBombs = new Vector<Bomb>();
 
 	private boolean maxBombs_used = false, bombRadius_used = false;
 
 	private boolean shieldEqu = false, shieldHit = false;
 
 	private double shieldStartTime, shieldTime = 500000000, immortalStartTime,
 			immortalTime = 6000000000L;
 
 	private boolean inMultiplayerMode = false;
 
 	private boolean bombsDeactivated = false;
 
 	/**
 	 * constructor
 	 * 
 	 * @param x
 	 *            x-position
 	 * @param y
 	 *            y-position
 	 * @param v
 	 *            set visibility
 	 * @param d
 	 *            set destroyable
 	 * @param c
 	 *            set collisionable
 	 * @param p
 	 *            image url
 	 * @param gr
 	 *            imageloader of the map
 	 */
 	public Player(int x, int y, boolean v, boolean d, boolean c, String p,
 			ImageLoader gr) {
 		super(x, y, v, d, c, p, gr);
 		alive = true;
 	}
 
 	public boolean getBombsDeactivated() {
 		return bombsDeactivated;
 	}
 
 	public void disableBombs() {
 		bombsDeactivated = true;
 		maxbombs = 0;
 	}
 
 	public void setMultiplayerModeTo(boolean mode) {
 		inMultiplayerMode = mode;
 	}
 
 	@Override
 	public void move() {
 		if (direction.isUp()) {
 			if (hasObjectCollision(posX, posY - speed, map.getCollisionMap(),
 					"UP")) {
 			} else {
 				posY -= speed;
 			}
 			if (shieldProtection) {
 				animation.change("playerUp_bubble");
 			} else if (immortal) {
 				animation.change("playerUp_immortal");
 			} else {
 				animation.change("playerUp");
 			}
 		}
 
 		if (direction.isDown()) {
 			if (hasObjectCollision(posX, posY + speed, map.getCollisionMap(),
 					"DOWN")) {
 			} else {
 				posY += speed;
 			}
 			if (shieldProtection) {
 				animation.change("playerDown_bubble");
 			} else if (immortal) {
 				animation.change("playerDown_immortal");
 			} else {
 				animation.change("playerDown");
 			}
 		}
 
 		if (direction.isLeft()) {
 			if (hasObjectCollision(posX - speed, posY, map.getCollisionMap(),
 					"LEFT")) {
 			} else {
 				posX -= speed;
 			}
 			if (shieldProtection) {
 				animation.change("playerLeft_bubble");
 			} else if (immortal) {
 				animation.change("playerLeft_immortal");
 			} else {
 				animation.change("playerLeft");
 			}
 		}
 
 		if (direction.isRight()) {
 			if (hasObjectCollision(posX + speed, posY, map.getCollisionMap(),
 					"RIGHT")) {
 			} else {
 				posX += speed;
 			}
 			if (shieldProtection) {
 				animation.change("playerRight_bubble");
 			} else if (immortal) {
 				animation.change("playerRight_immortal");
 			} else {
 				animation.change("playerRight");
 			}
 		}
 	}
 
 	/**
 	 * Adds bomb to the mapObjects. If Upgrade bombRemote has been collected the
 	 * bomb object will be added to a list of actual planted bombs.
 	 * 
 	 * @param cm
 	 *            collision map
 	 */
 	public void plantBomb(BufferedImage cm) {
 		if (!reachedMaxBombs()) {
 			Bomb bomb = new Bomb(getPosX(), getPosY(), true, false, false,
 					"simplebomb", map.getGraphics(), bombradius, cm, ID);
 			bomb.setMap(getMap());
 			map.getMapObjects().get(1).add(bomb);
 			addBomb();
 
 			if (!bombRemote) {
 				bomb.activateBomb();
 			} else {
 				remoteBombs.add(bomb);
 			}
 		}
 	}
 
 	/**
 	 * abstract method to check for collisions
 	 * 
 	 * @param x
 	 *            current x position
 	 * @param y
 	 *            current y position
 	 * @param cm
 	 *            current collision map
 	 * @param dir
 	 *            current direction
 	 * @return returns true if object has a collision else false
 	 */
 	public boolean hasObjectCollision(int x, int y, BufferedImage cm, String dir) {
 		if (x < 0 || y < 0 || x > cm.getWidth() - 50 || y > cm.getHeight() - 50) {
 			return true;
 		}
 		int t = 20;
 		boolean upleft = false;
 		boolean upright = false;
 		boolean downleft = false;
 		boolean downright = false;
 		BufferedImage collTest = cm.getSubimage(x, y, 50, 50);
 		for (int i = 0; i < collTest.getWidth(); i++) {
 			for (int j = 0; j < collTest.getHeight(); j++) {
 				Color test = new Color(collTest.getRGB(i, j));
 				if (test.equals(Color.yellow)) {
 					map.finishMap();
 					/*
 					 * } else if (test.equals(Color.orange)) { this.die();
 					 * map.finishMap(); } else if (test.equals(Color.red)) {
 					 * this.die(); map.finishMap(); }
 					 */
 				} else if (test.equals(Color.black) || test.equals(Color.gray)) {
 					if (i < t && j < t) {
 						upleft = true;
 					} else if (i > 50 - t && j < t) {
 						upright = true;
 					} else if (i < t && j > 50 - t) {
 						downleft = true;
 					} else if (i > 50 - t && j > 50 - t) {
 						downright = true;
 					} else {
 						return true;
 					}
 				}
 			}
 		}
 
 		if (upleft ^ upright ^ downleft ^ downright) {
 			if (upleft) {
 				if (dir.equals("UP")) {
 					if (simpleHasColl(posX + 1, posY, cm, Color.black,
 							Color.gray)) {
 						return true;
 					} else {
 						this.posX += 1;
 						return true;
 					}
 				}
 
 				if (dir.equals("LEFT")) {
 					if (simpleHasColl(posX, posY + 1, cm, Color.black,
 							Color.gray)) {
 						return true;
 					} else {
 						this.posY += 1;
 						return true;
 					}
 				}
 			}
 
 			if (upright) {
 				if (dir.equals("UP")) {
 					if (simpleHasColl(posX - 1, posY, cm, Color.black,
 							Color.gray)) {
 						return true;
 					} else {
 						this.posX -= 1;
 						return true;
 					}
 
 				}
 
 				if (dir.equals("RIGHT")) {
 					if (simpleHasColl(posX, posY + 1, cm, Color.black,
 							Color.gray)) {
 						return true;
 					} else {
 						this.posY += 1;
 						return true;
 					}
 				}
 			}
 
 			if (downleft) {
 				if (dir.equals("DOWN")) {
 					if (simpleHasColl(posX + 1, posY, cm, Color.black,
 							Color.gray)) {
 						return true;
 					} else {
 						this.posX += 1;
 						return true;
 					}
 				}
 
 				if (dir.equals("LEFT")) {
 					if (simpleHasColl(posX, posY - 1, cm, Color.black,
 							Color.gray)) {
 						return true;
 					} else {
 						this.posY -= 1;
 						return true;
 					}
 				}
 			}
 
 			if (downright) {
 				if (dir.equals("DOWN")) {
 					if (simpleHasColl(posX - 1, posY, cm, Color.black,
 							Color.gray)) {
 						return true;
 					} else {
 						this.posX -= 1;
 						return true;
 					}
 				}
 
 				if (dir.equals("RIGHT")) {
 					if (simpleHasColl(posX, posY - 1, cm, Color.black,
 							Color.gray)) {
 						return true;
 					} else {
 						this.posY -= 1;
 						return true;
 					}
 
 				}
 			}
 
 		}// else{return true;}
 
 		return false;
 	}
 
 	/**
 	 * checks for enemy/bomb collison , kills player if true
 	 * 
 	 * @cm collision map
 	 */
 	@Override
 	public void update(BufferedImage cm) {
 		if (simpleHasColl(posX, posY, map.getCollisionMap(), Color.orange,
 				Color.red)) {
 			if (!immortal) {
 				if (!shieldProtection) {
 					if (!remotePlayer) {
 						this.die();
 						if (!inMultiplayerMode)
 							map.finishMap();
 					}
 				} else {
 					if (!shieldHit) {
 						shieldStartTime = System.nanoTime();
 						shieldHit = true;
 						shieldEqu = false;
 						System.out.println("Shield hit");
 					}
 				}
 			} else {
 				System.out.println("Hit, but immortal. Muhaha");
 			}
 		}
 		if (shieldHit) {
 			if (shieldStartTime + shieldTime <= System.nanoTime()) {
 				shieldProtection = false;
 				shieldHit = false;
 				animation.setCurrentAnimation(animation.getCurrentImagePath());
 				System.out.println("Shield off");
 			}
 		}
 		if (immortal) {
 			if (immortalStartTime + immortalTime <= System.nanoTime()) {
 				immortal = false;
 				animation.setCurrentAnimation(animation.getCurrentImagePath());
 				System.out.println("Now you are not immortal anymore");
 			}
 		}
 		animation.animate();
 		move();
 		// checkUpgradeCollision(cm); wird durch handleUpgrades ersetzt
 	}
 
 	/**
 	 * Causes all planted bombs to explode. Method is called by pressing a key,
 	 * but only if the upgrade bombRemote has been collected.
 	 */
 	public void bombExplode() {
 		for (Bomb bomb : remoteBombs) {
 			bomb.explode(map.getCollisionMap());
 		}
 	}
 
 	public boolean activateShield() {
 		if (shieldEqu && !shieldProtection) {
 			shieldProtection = true;
 			animation.setCurrentAnimation("playerDown_bubble");
 			System.out.println("Shield activated");
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Handles the upgrade given by the giveUpgrade function
 	 * 
 	 * @param upgrade
 	 */
 	private void handleUpgrades(Upgrade upgrade) {
 		Color tmpColor = upgrade.getColor();
 		if ((tmpColor == Color.pink) && !maxBombs_used) {
 			if (maxbombs < 4) {
 				maxbombs++;
 				maxBombs_used = true;
 			}
 		}
 		if ((tmpColor == Color.blue) && !bombRadius_used) {
 			if (bombradius < 3) {
 				bombradius++;
 				bombRadius_used = true;
 			}
 		}
 		if (tmpColor == Color.cyan) {
 			if (!shieldEqu) {
 				shieldEqu = true;
 			}
 		}
 		if (tmpColor == Color.magenta) {
 			bombRemote = true;
 		}
 		if (tmpColor == Color.lightGray) {
 			if (!immortal) {
 				immortal = true;
 				shieldProtection = false;
 				animation.setCurrentAnimation("playerDown_immortal");
 				immortalStartTime = System.nanoTime();
 			}
 		}
 
 		if (maxBombs_used) {
 			if (tmpColor == Color.pink) {
 				maxBombs_used = false;
 			}
 		}
 
 		if (bombRadius_used) {
 			if (tmpColor == Color.blue) {
 				bombRadius_used = false;
 			}
 		}
 	}
 
 	@Override
 	public void draw(Graphics2D g2d, ImageLoader gr, Graphics2D cm) {
 		g2d.drawImage(animation.getCurrentImage(), posX, posY, null);
 
 		if (!immortal) {
 			cm.setPaint(Color.green);
 			cm.fillRect(posX, posY, 50, 50);
 		} else {
 			cm.setPaint(Color.darkGray);
 			cm.fillRect(posX, posY, 50, 50);
 		}
 	}
 
 	/**
 	 * 
 	 * @return true if max bomb level is reached else false
 	 */
 	public boolean reachedMaxBombs() {
 		return (currentbombs + 1 > maxbombs);
 	}
 
 	/**
 	 * increase bombcounter
 	 */
 	public void addBomb() {
 		currentbombs++;
 	}
 
 	/**
 	 * decrease bombcounter
 	 */
 	public void removeBomb() {
 		currentbombs--;
 	}
 
 	/**
 	 * Initializes this playerobject as a remote controlled player
 	 */
 	public void makeRemote() {
 		remotePlayer = true;
 	}
 
 	/**
 	 * Returns a player's identification number.
 	 * 
 	 * @return current ID
 	 */
 	public int getID() {
 		return ID;
 	}
 
 	/**
 	 * Sets a player's identification number.
 	 * 
 	 * @param iD
 	 *            new ID
 	 */
 	public void setID(int iD) {
 		ID = iD;
 	}
 
 	/**
 	 * Allows checking for a player's 'alive'-status.
 	 * 
 	 * @return alive flag
 	 */
 	public boolean isAlive() {
 		return alive;
 	}
 
 	/**
 	 * Kills player object (i.e. sets its 'alive' flag to false).
 	 */
 	public void die() {
 		alive = false;
 	}
 
 	public boolean hasRemoteBombs() {
 		return bombRemote;
 	}
 
 	public int getBombRadius() {
 		return bombradius;
 	}
 
 	public int getCurrentBombs() {
 		return currentbombs;
 	}
 
 	public int getMaxBombs() {
 		return maxbombs;
 	}
 
 	public void setMaxBombs(int maxbombs) {
 		if (!bombsDeactivated)
 			this.maxbombs = maxbombs;
 
 	}
 
 	public void setBombRemote(boolean bombRemote) {
 		this.bombRemote = bombRemote;
 
 	}
 
 	public void setBombRadius(int bombradius) {
 		this.bombradius = bombradius;
 	}
 
 	public boolean isShieldEqu() {
 		return shieldEqu;
 	}
 
 	public void setShieldEqu(boolean shieldEqu) {
 		this.shieldEqu = shieldEqu;
 	}
 
 	public PlayerData getPlayerData() {
 		return PlayerData.extractData(this);
 	}
 
 	public void restorePlayerToData(PlayerData data) {
 		data.restorePlayer(this);
 	}
 
 	/**
 	 * Data container for all important player status variables. Used to create
 	 * Savegames.
 	 * 
 	 * @author tohei
 	 * 
 	 */
 	public static class PlayerData {
 
 		private static final int id = 0;
 		int maxbombs;
 		int bombradius;
 		boolean bombRemote;
 		int speed; // Shield
 
 		public PlayerData(int maxbombs, int bombradius, boolean remoteBombs,
 				int speed) {
 			super();
 			this.maxbombs = maxbombs;
 			this.bombradius = bombradius;
 			this.bombRemote = remoteBombs;
 			this.speed = speed; // TODO Shield
 		}
 
 		public static PlayerData extractData(Player player) {
 			return new PlayerData(player.getMaxBombs(), player.getBombRadius(),
 					player.hasRemoteBombs(), player.getSpeed());
 		}
 
 		public void restorePlayer(Player player) {
 			player.setMaxBombs(maxbombs);
 			player.setBombRadius(bombradius);
 			player.setBombRemote(bombRemote);
 			player.setSpeed(speed); // TODO Shield
 		}
 
 		public static PlayerData extractDataFromString(String input) {
 			String[] inputData = input.split(";");
 			int mbombs = 0, bombr = 0, mvSpeed = 0;
 			boolean remote = false;
 
 			String name = null;
 			for (int i = 1; i < inputData.length; i++) {
 				String[] data = inputData[i].split("=");
 				if (data[0].equals("mb")) {
 					mbombs = Integer.parseInt(data[1]);
 				}
 				if (data[0].equals("brad")) {
 					bombr = Integer.parseInt(data[1]);
 				}
 				if (data[0].equals("brem")) {
 					remote = Boolean.parseBoolean(data[1]);
 				}
 				if (data[0].equals("ps")) {
 					mvSpeed = Integer.parseInt(data[1]);
 				}
 			}
 			return new PlayerData(mbombs, bombr, remote, mvSpeed);
 		}
 
 		public String writeDataToString() {
 			StringBuilder sb = new StringBuilder();
 			sb.append("player_data");
 			sb.append(";id=").append(id);
 			sb.append(";mb=").append(maxbombs);
 			sb.append(";brad=").append(bombradius);
 			sb.append(";brem=").append(bombRemote);
 			sb.append(";ps=").append(speed); // TODO Shield
 			return sb.toString();
 		}
 	}
 
 	@Override
 	public void giveUpgrade(Upgrade upgrade) {
 		handleUpgrades(upgrade);
 		upgrade.setDestroyed(true);
 	}
 
 	@Override
 	public boolean collidesWithListener(Upgrade upgrade) {
 		if ((Math.abs(upgrade.getPosX() - this.posX) < 50)
 				&& (Math.abs(upgrade.getPosY() - this.posY) < 50)) {
 			return true;
 		}
 		return false;
 	}
 
 }
