 package gameData;
 
 import gameView.GameWindow;
 import gameView.ingame.datatypes.Entity;
 
 import java.awt.Graphics;
 
 import recources.ImageCache;
 import singleton.GameProperties;
import singleton.SingletonWorker;
 
 public class Player extends Entity {
 
 	public static final int CLASS_BRUTE = 0;
 	public static final int CLASS_MAGE = 1;
 	public static final int CLASS_ARCHER = 2;
 	public static final String CLASS_PIC_BRUTE = "brute.png";
 	public static final String CLASS_PIC_MAGE = "mage.png";
 	public static final String CLASS_PIC_ARCHER = "archer.png";
 	public static final String CLASS_ATTACK_PIC_BRUTE = "brute_attack.png";
 	public static final String CLASS_ATTACK_PIC_MAGE = "mage_attack.png";
 	public static final String CLASS_ATTACK_PIC_ARCHER = "archer_attack.png";
 	public static final int STEP_DURATION_MILLIS = 500;
 
 	private int health, mana, strength, intelligence, agility, atk, def,
 	magRes, playerClass;
 	private boolean attacking = false;
 	//	private int attackAnimationStep = 0;
 	//	private int attackPartProgressMillis = Integer.MAX_VALUE;
 	private int attackX, attackY;
 	private int mouseX, mouseY; 
 	//	private int moveAnimationStep = 0;
 	//	private int movePartProgressMillis = Integer.MAX_VALUE;
 	private String name;
 
 	public Player(int health, int mana, int strength, int intelligence,
 			int agility, int atk, int def, int magRes, int playerClass,
 			int speed, int xPos, int yPos, String name) {
 		super(xPos, yPos);
 		this.health = health;
 		this.mana = mana;
 		this.strength = strength;
 		this.intelligence = intelligence;
 		this.agility = agility;
 		this.atk = atk;
 		this.def = def;
 		this.magRes = magRes;
 		this.name = name;
 		this.playerClass = playerClass;
 		this.updateImageDirection(0);
 		this.setSpeedPxlsPerSec(speed);
 	}
 	
 	public String getName(){
 		return name;
 	}
 
 	@Override
 	public void draw(Graphics g) {
 
 		double xp = (this.getXPos())
 				+(GameWindow.getWindowWidth() / 2)-GameProperties.GRAPHICS_SIZE_CHAR_WIDTH/2-GameProperties.playerx;
 
 		double yp = (this.getYPos())
 				+(GameWindow.getWindowHeight() / 2)-GameProperties.GRAPHICS_SIZE_CHAR_HEIGHT*2-10-GameProperties.playery;
 		g.drawImage(this.getImage(),(int)xp,(int)yp, null);
		g.setFont(SingletonWorker.gameProperties().smallgameFont());
		g.drawString(getName(), (int)xp,(int)yp-10);
		g.setFont(SingletonWorker.gameProperties().gameFont());
		
 
 	}
 
 	@Override
 	public void process(long duration) {
 		super.process(duration);
 	}
 
 	public void attack(int x, int y) {
 		this.attacking = true;
 		this.attackX = x;
 		this.attackY = y;
 	}
 
 	//	private void attack() {
 	//		// TODO Angriff bzw. Schaden machen! (Projektil oder einfacher Treffer
 	//		// an Gegner)
 	//	}
 
 	public double calculateNextXPosition(long duration) {
 		return this.getXPos()
 				+ ((double) duration * (double) this.getXPerSec()) / 1000d;
 	}
 
 	public double calculateNextYPosition(long duration) {
 		return this.getYPos()
 				+ ((double) duration * (double) this.getYPerSec()) / 1000d;
 	}
 
 	@Override
 	public void updateImageDirection(long duration) {
 		//		this.updateDirection();
 
 		//		if (!attacking) {
 
 		// moveAnimationStep = 0, movePartProgressMillis
 
 		this.setImage(ImageCache.getPlayerImage(this.getDirection()));
 		//		} else {
 		//
 		//			Image fullAttackImage = SpriteManager.getSprite(
 		//					Player.getAttackAnimation(this.playerClass), false)
 		//					.getImage();
 		//			int heightBlocks = fullAttackImage.getHeight(null)
 		//					/ GameProperties.GRAPHICS_SIZE_CHAR_HEIGHT;
 		//
 		//			// TODO attackspeed berechnung verebssern; attacksPerSecond oder so
 		//			if (this.attackPartProgressMillis > ((10000 / this.agility) / heightBlocks)) {
 		//				this.attackPartProgressMillis = 0;
 		//
 		//				this.attackSprite.setImage(cutCurrentAttackingFrame(
 		//						fullAttackImage, this.attackAnimationStep));
 		//
 		//				this.attackAnimationStep++;
 		//				if (this.attackAnimationStep >= heightBlocks) {
 		//					this.attackAnimationStep = 0;
 		//					this.attacking = false;
 		//					this.attackPartProgressMillis = Integer.MAX_VALUE;
 		//					this.attack();
 		//				}
 		//
 		//			} else {
 		//				this.attackPartProgressMillis += duration;
 		//			}
 		//		}
 	}
 
 
 
 	//	private void updateDirection() {
 	//		int mouseXDiff = (int) (this.mouseX - this.getXPos());
 	//		int mouseYDiff = (int) (this.mouseY - this.getYPos());
 	//
 	//		if (mouseXDiff < 0 && Math.abs(mouseXDiff) > Math.abs(mouseYDiff)) {
 	//			this.setDirection(Direction.left);
 	//		} else if (mouseXDiff > 0
 	//				&& Math.abs(mouseXDiff) > Math.abs(mouseYDiff)) {
 	//			this.setDirection(Direction.right);
 	//		} else if (mouseYDiff > 0
 	//				&& Math.abs(mouseYDiff) > Math.abs(mouseXDiff)) {
 	//			this.setDirection(Direction.down);
 	//		} else if (mouseYDiff < 0
 	//				&& Math.abs(mouseYDiff) > Math.abs(mouseXDiff)) {
 	//			this.setDirection(Direction.up);
 	//		}
 	//	}
 
 	public static String getMovementAnimation(int playerClass) {
 		switch (playerClass) {
 		case 0:
 			return Player.CLASS_PIC_BRUTE;
 		case 1:
 			return Player.CLASS_PIC_MAGE;
 		case 2:
 			return Player.CLASS_PIC_ARCHER;
 		}
 		return null;
 	}
 
 	public static String getAttackAnimation(int playerClass) {
 		switch (playerClass) {
 		case 0:
 			return Player.CLASS_ATTACK_PIC_BRUTE;
 		case 1:
 			return Player.CLASS_ATTACK_PIC_MAGE;
 		case 2:
 			return Player.CLASS_ATTACK_PIC_ARCHER;
 		}
 		return null;
 	}
 
 	public int getHealth() {
 		return health;
 	}
 
 	public void setHealth(int health) {
 		this.health = health;
 	}
 
 	public int getMana() {
 		return mana;
 	}
 
 	public void setMana(int mana) {
 		this.mana = mana;
 	}
 
 	public int getStrength() {
 		return strength;
 	}
 
 	public void setStrength(int strength) {
 		this.strength = strength;
 	}
 
 	public int getIntelligence() {
 		return intelligence;
 	}
 
 	public void setIntelligence(int intelligence) {
 		this.intelligence = intelligence;
 	}
 
 	public int getAgility() {
 		return agility;
 	}
 
 	public void setAgility(int agility) {
 		this.agility = agility;
 	}
 
 	public int getAtk() {
 		return atk;
 	}
 
 	public void setAtk(int atk) {
 		this.atk = atk;
 	}
 
 	public int getDef() {
 		return def;
 	}
 
 	public void setDef(int def) {
 		this.def = def;
 	}
 
 	public int getMagRes() {
 		return magRes;
 	}
 
 	public void setMagRes(int magRes) {
 		this.magRes = magRes;
 	}
 
 	public int getPlayerClass() {
 		return playerClass;
 	}
 
 	public void setPlayerClass(int playerClass) {
 		this.playerClass = playerClass;
 	}
 
 	public boolean isAttacking() {
 		return attacking;
 	}
 
 	public void setAttacking(boolean attacking) {
 		this.attacking = attacking;
 	}
 
 	public int getAttackX() {
 		return attackX;
 	}
 
 	public void setAttackX(int attackX) {
 		this.attackX = attackX;
 	}
 
 	public int getAttackY() {
 		return attackY;
 	}
 
 	public void setAttackY(int attackY) {
 		this.attackY = attackY;
 	}
 
 	public int getMouseY() {
 		return mouseY;
 	}
 
 	public void setMouseY(int mouseY) {
 		this.mouseY = mouseY;
 	}
 
 	public int getMouseX() {
 		return mouseX;
 	}
 
 	public void setMouseX(int mouseX) {
 		this.mouseX = mouseX;
 	}
 
 }
