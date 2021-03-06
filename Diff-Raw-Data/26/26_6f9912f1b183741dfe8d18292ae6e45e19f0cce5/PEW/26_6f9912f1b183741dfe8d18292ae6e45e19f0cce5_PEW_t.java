 package processing.test.pew;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Random;
 
 import processing.core.PApplet;
 import processing.core.PFont;
 import processing.core.PImage;
 import android.content.res.AssetManager;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.SoundPool;
 import android.content.res.AssetFileDescriptor;
 
 public class PEW extends PApplet{
 
 	AssetManager assetManager;// needed for sounds, has to be up in rank
 	SoundPool soundPool;
 
 	PlayerShip player;
 	Ship enemy;
 
 	PFont f;
 
 	ArrayList<Ship> enemyShips = new ArrayList<Ship>();// the player is not
 														// included
 	ArrayList<Projectile> enemyBullets = new ArrayList<Projectile>();
 	ArrayList<Projectile> playerBullets = new ArrayList<Projectile>();
 	ArrayList<Item> items = new ArrayList<Item>();// misc stuff and items
 	ArrayList<PowerUp> activePowerUps = new ArrayList<PowerUp>();
 
 	PFont fontG;
 	String highscoreFile = "highscore.txt";
 	final String GO = "Game Over";
 	PrintWriter output;
 	BufferedReader reader;
 	int highscoretop, highscoremid, highscorebottom, points;
 
 	Random gen = new Random();
 
 	boolean psychedelicMode = false;
 	boolean playGame, showMenu, showCredits, showHighScore, showDeath,
 			showInstructions, showOptions;
 	boolean musicReady = false, startUp = true;
 	public Menu menu;
 
 	ArrayList<PImage> loadedPics = new ArrayList<PImage>();
 
 	public void loadImages() {
 		loadedPics.add(loadImage("bullet.png"));// #0
 		loadedPics.add(loadImage("playerbullet.png"));// #1
 		loadedPics.add(loadImage("coin.png"));// #2
 		loadedPics.add(loadImage("RocketE.png"));// #3
 		loadedPics.add(loadImage("Drone.png"));// #4
 		loadedPics.add(loadImage("Cruiser.png"));// #5
 		loadedPics.add(loadImage("Turret.png"));// #6
 		loadedPics.add(loadImage("spaceship.png"));// #7
 		loadedPics.add(loadImage("PowerUp.png"));// #8
 		loadedPics.add(loadImage("HerpADerp.png"));// #9
 		loadedPics.add(loadImage("bomb.png"));// #10
 		loadedPics.add(loadImage("Drone-hit.png"));// *11
 		loadedPics.add(loadImage("MainMenu.png"));// #12
 		loadedPics.add(loadImage("PsychedelicPowerUp1.png"));// #13
 		
 
 	}
 
 	BackgroundHandler bghandel = new BackgroundHandler();
 
 	Sounds sound = new Sounds();
 	MediaPlayer mediaPlayer = new MediaPlayer();
 
 	public void setup() {
 		startUp = false;
 		assetManager = this.getAssets();// needed for sounds, has to be up in
 										// rank
 		soundPool = new SoundPool(20, AudioManager.STREAM_MUSIC, 0);
 		// mediaPlayer =
 		// MediaPlayer.create(getApplicationContext(),R.raw.bitswithbyte);
 
 		sound.setUp();
 
 		fontG = createFont("Constantia", 48);
 
 		importHighscore();
 
 		loadImages();
 		menu = new Menu();
 		showMenu = true;
 		playGame = false;
 		bghandel.setBG("spaceBackground.png");
 
 		imageMode(CENTER);
 		smooth();
 		noStroke();
 		fill(255);
 		rectMode(CENTER); // This sets all rectangles to draw from the center
 							// point
 		player = new PlayerShip(displayWidth / 2, (4 * displayHeight) / 5);
 		orientation(PORTRAIT);
 		frameRate(30);
 
 		f = createFont("Impact", 24, true);
 		textFont(f, 24);
 		fill(255);
 
 	}
 
 	int tick = 1, spawnNum;
 	Spawner spawner = new Spawner();
 	boolean spawning = false;
 
 	public void draw() {
 
 		if (!psychedelicMode) {
 			background(0xff000000);
 			bghandel.scroll();
 		}
 
 		if (playGame == false) {
 			if (showCredits)
 				printCredits();
 			if (showMenu)
 				menu.showMenu();
 			if (showInstructions)
 				printInstructions();
 			if (showHighScore)
 				printHighScores();
 			if (showOptions)
 				printOptions();
 			if (showDeath)
 				printDeath();
 		} else {
 			spawning = true;
 
 			for (int j = 0; j < playerBullets.size(); j++) {
 				Projectile p = (Projectile) playerBullets.get(j);
 				p.move();
 			}
 			for (int j = 0; j < enemyShips.size(); j++) {
 				enemyShip s = (enemyShip) enemyShips.get(j);
 				s.act();
 			}
 
 			for (int j = 0; j < enemyBullets.size(); j++) {
 				Projectile p = (Projectile) enemyBullets.get(j);
 				p.move();
 			}
 
 			for (int j = 0; j < items.size(); j++) {
 				Item p = (Item) items.get(j);
 				p.move();
 			}
 			for (int i = activePowerUps.size() - 1; i >= 0; i--) {
 				PowerUp p = activePowerUps.get(i);
 				p.increment();
 			}
 
 			text("Score: " + player.getScore(), displayWidth / 20,
 					displayHeight / 20);
 			// text("Bullet Count: " + (enemyBullets.size() +
 			// playerBullets.size()), 10, 50);
 			// text("Ship Count: " + enemyShips.size(), 10, 75);
 
 			collisionDetection();
 
 			if (tick == 100) {
 				ArrayList<Turret> guns = new ArrayList<Turret>();
 				guns.add(new Turret(new TestGun(), 50));
 				guns.add(new Turret(new TestGun(), 50));
 				guns.add(new Turret(new TestGun(), 50));
 				guns.add(new Turret(new TestGun(), 50));
 				guns.add(new Turret(new TestGun(), 50));
 				guns.add(new Turret(new TestGun(), 50));
 				guns.add(new Turret(new TestGun(), 50));
 				guns.add(new Turret(new TestGun(), 50));
 				guns.add(new Turret(new TestGun(), 50));
 				guns.add(new Turret(new TestGun(), 50));
 				guns.add(new Turret(new TestGun(), 50));
 				guns.add(new Turret(new TestGun(), 50));
 				guns.add(new Turret(new TestGun(), 50));
 				guns.add(new Turret(new TestGun(), 50));
 				guns.add(new Turret(new TestGun(), 50));
 				guns.add(new Turret(new TestGun(), 50));
 
 				enemyShips.add(new Cruiser(guns));
 			}
 
 			if (tick % 500 == 0) {
 				// !spawning;
 				spawnNum = gen.nextInt(5);
 			}
 
 			if (spawning)
 				spawner.spawn(spawnNum);
 
 			tick++;
 			if (tick == 100000)
 				tick = 0;
 
 			if (mousePressed) {
 				player.move();
 				if (tick % 3 == 0)
 					player.shoot();
 			}
 			player.display();
 		}
 	}
 
 	public void collisionDetection() {
 
 		for (int i = 0; i < playerBullets.size(); i++) {
 			Projectile p = (Projectile) playerBullets.get(i);
 			for (int j = 0; j < enemyShips.size(); j++) {
 				Ship s = (Ship) enemyShips.get(j);
 				if (p.isTouching(s)) {
 					s.hit();
 					p.removeSelf();
 				}
 				if (s instanceof Cruiser) {
 					Cruiser t = (Cruiser) s;
 					ArrayList<Turret> guns = t.getTurretList();
 					for (int k = 0; k < guns.size(); k++) {
 						Turret g = guns.get(k);
 						if (p.isTouching(g)) {
 							g.hit();
 							p.removeSelf();
 						}
 					}
 				}
 			}
 		}
 
 		for (int i = 0; i < enemyBullets.size(); i++) {
 			Projectile p = (Projectile) enemyBullets.get(i);
 			p.move();
 			if (p.isTouching(player)) {
				//player.blowUp();
				//p.removeSelf();
				player.hit();
 				updateHighscore();
 			}
 		}
 
 		for (int i = 0; i < items.size(); i++) {
 			Item p = (Item) items.get(i);
 			p.move();
 			if (p.isTouching(player)) {
 				p.act();
 			}
 		}
 		for (int i = 0; i < enemyShips.size(); i++) {
 			Ship p = (Ship) enemyShips.get(i);
 			if (p.isTouching(player)) {
 				p.blowUp();
 				player.blowUp();
 				updateHighscore();
 				;
 			}
 		}
 	}
 
 	public void printCredits() {
 		textAlign(CENTER);
 		image(loadImage("Back.png"), displayWidth / 2, displayHeight / 12,
 				displayWidth, displayHeight / 6);
 		text("Lead Designer: Justin \"Nalta\" Sybrandt", displayWidth / 2,
 				displayHeight / 5);
 		text("Code Monkey: Caelan \"DarkFire16\" Mayberry", displayWidth / 2,
 				displayHeight * (2 / 5.0f));
 		text("Chill Meister: Mike \"Reason\" Boom", displayWidth / 2,
 				displayHeight * (3 / 5.0f));
 		text("Art Master: Josh \"Fa1seEcho\" Walton", displayWidth / 2,
 				displayHeight * (4 / 5.0f));
 
 		// BULD A BACK BUTTON AT TOP OF SCREEN
 		if (mousePressed && mouseY < displayHeight / 6) {
 			showCredits = false;
 			showMenu = true;
 			playGame = false;
 		}
 	}
 
 	public void printInstructions() {
 		textAlign(CENTER);
 		image(loadImage("Back.png"), displayWidth / 2, displayHeight / 12,
 				displayWidth, displayHeight / 6);
 		text("SHOOT STUFF", displayWidth / 2, displayHeight / 3);
 
 		if (mousePressed && mouseY < displayHeight / 6.0f) {
 			showCredits = false;
 			showMenu = true;
 			playGame = false;
 			showInstructions = false;
 		}
 	}
 
 	public void printHighScores() {
 		for (int i = 1; i == 1; i++) {
 			importHighscore();
 
 		}
 		textAlign(CENTER);
 		image(loadImage("Back.png"), displayWidth / 2, displayHeight / 12,
 				displayWidth, displayHeight / 6);
 		text(" " + highscoretop, displayWidth / 2, displayHeight / 4);
 
 		text("AND YOU SHOULD FEEL BAD\nREALLY BAD\nGET OUT\n(0.0)",
 				displayWidth / 2, displayHeight / 3);
 
 		if (mousePressed && mouseY < displayHeight / 6.0f) {
 			showMenu = true;
 			showHighScore = false;
 		}
 	}
 
 	public void printOptions() {
 		textAlign(CENTER);
 		image(loadImage("Back.png"), displayWidth / 2, displayHeight / 12,
 				displayWidth, displayHeight / 6);
 		text("YOU AINT SEEN NOTHING YET!", displayWidth / 2, displayHeight / 4);
 
 		text("...seriously, we have yet to code this...", displayWidth / 2,
 				displayHeight / 3);
 
 		if (mousePressed && mouseY < displayHeight / 6.0f) {
 
 			showMenu = true;
 			showOptions = false;
 		}
 	}
 
 	public void printDeath() {
 		GameOverMessage(GO);
 	}
 
 
 	public void onStop() {
 		if (soundPool != null) { // must be checked because or else crash when
 			// return from landscape mode
 			soundPool.release(); // release the player
 			if(musicReady)
 			{
 			mediaPlayer.stop();
 			mediaPlayer.release();
 			mediaPlayer.reset();
 			musicReady=false;
 			}
 		}
 		super.onStop();
 	}
 	
 	public void onPause() {
 		if (soundPool != null) { // must be checked because or else crash when
 			// return from landscape mode
 			soundPool.release(); // release the player
 			if(musicReady)
 			{
 			mediaPlayer.stop();
 			mediaPlayer.release();
 			mediaPlayer.reset();
 			musicReady=false;
 			}
 		}
 		super.onPause();
 	}
 
 	public void onResume()
 	{
 		if(!startUp && !musicReady)
 		{
 		sound.setUp();
 		}
 		super.onResume();
 	}
 
 
 	abstract class Actor {
 		int locX, locY, radius, speed;
 		boolean dir;
 		PImage img;
 
 		public void move() {
 		}
 
 		public boolean isTouching(Actor b) {
 			if (sqrt(pow(locX - b.locX, 2) + pow(locY - b.locY, 2)) < (radius + b.radius))
 				return true;
 			else
 				return false;
 		}
 
 		public void display() {
 			image(img, locX, locY);
 		}
 
 		public int getLocX() {
 			return locX;
 		}
 
 		public int getLocY() {
 			return locY;
 		}
 	}
 
 	public class BackgroundHandler {
 		PImage bgimg, upcomingImg;
 		int scrolly, upcomingScrolly;
 		boolean needFlip;
 
 		BackgroundHandler() {
 
 		}
 
 		public void setBG(String img) {
 			bgimg = loadImage(img);
 			upcomingImg = bgimg;
 			scrolly = displayHeight;
 			upcomingScrolly = 0;
 		}
 
 		public void scroll() {
 			image(bgimg, displayWidth / 2, scrolly - bgimg.height / 2);
 			scrolly += 5;
 			if (scrolly - bgimg.height >= 0) {
 				scrollInNew();
 			}
 		}
 
 		public void scrollInNew() {
 			image(upcomingImg, displayWidth / 2, upcomingScrolly
 					- upcomingImg.height / 2);
 			upcomingScrolly += 5;
 			if (upcomingScrolly >= displayHeight)
 				flip();
 		}
 
 		public void loadNewImg(String img) {
 			upcomingImg = loadImage(img);
 			upcomingScrolly = 0;
 		}
 
 		public void flip() {
 			needFlip = false;
 			bgimg = upcomingImg;
 			scrolly = upcomingScrolly;
 			upcomingScrolly = 0;
 		}
 
 	}
 
 	public class Bomb extends Projectile {
 		int count;
 		int lifeSpan;
 		int speed;
 
 		Bomb(int locX, int locY) {
 			super(locX, locY, 10, 0, 5);
 			count = 0;
 			lifeSpan = 75;
 			speed = 5;
 			enemyBullets.add(this);
 		}
 
 		Bomb(int locX, int locY, int dispx, int dispy) {
 			super(locX, locY, 10, dispx, dispy);
 			count = 0;
 			lifeSpan = 25;
 			speed = 5;
 			enemyBullets.add(this);
 		}
 
 		public void move() {
 			super.move();
 			count++;
 			if (count > lifeSpan) {
 				detonate();
 				removeSelf();
 			}
 		}
 
 		public void detonate() {
 			for (float degree = 0; degree < 2 * PI; degree += PI / 12) {
 				int dispx = (int) (speed * sin(degree));
 				int dispy = (int) (speed * cos(degree));
 				new Bullet(locX, locY, dispx, dispy);
 				// print(""+dispx +","+ dispy);
 			}
 		}
 	}
 
 	public class BombLauncher extends Gun {
 		BombLauncher() {
 		}
 
 		public void shoot(int locX, int locY) {
 			new Bomb(locX, locY);
 		}
 	}
 
 	class Bullet extends Projectile {
 		Bullet(int xpos, int ypos, int h, int s) {
 			super(xpos, ypos, 0, h, s);
 			enemyBullets.add(this);
 			radius = 7;
 		}
 
 		Bullet(int xpos, int ypos) {
 			super(xpos, ypos, 0, 0, 4);
 			enemyBullets.add(this);
 			radius = 7;
 		}
 
 	}
 
 	public class Cruiser extends enemyShip {
 		ArrayList<Turret> guns;
 		int[] turretsX = { -600, -530, -470, -420, -376, -340, -296, -150, -90,
 				-30, 30, 100, 220, 300, 370, 450 };
 		int[] turretsY = { 136, 136, 136, 90, 18, 90, 18, 162, 162, 162, 162,
 				166, 82, 82, 82, 82 };
 		int count;
 		boolean moving, shooting;
 		int activeGun;
 		Turret activeTurret;
 		int destinationX, destinationY;
 
 		Cruiser(ArrayList<Turret> g) {
 			super(displayWidth / 2, -100, 3, 5, 7, 1000, 1000);
 			guns = g;
 			prepairTurrets();
 			count = 0;
 			destinationX = locX;
 			destinationY = displayHeight / 4;
 			moving = true;
 			shooting = false;
 			selectNewGun();
 
 		}
 
 		public void prepairTurrets() {
 			double gunRange = 0;
 			if (guns.size() > 0)
 				gunRange = 800 / guns.size();
 			for (int i = 0; i < guns.size(); i++) {
 				guns.get(i).moveTo(locX + turretsX[i], locY + turretsY[i]);
 			}
 		}
 
 		public void act() {
 			display();
 			displayTurrets();
 			checkTurrentHealths();
 			if (moving)
 				move();
 			else if (shooting)
 				shoot();
 			else if (guns.size() > 0)
 				selectNewGun();
 			else
 				super.blowUp();
 
 		}
 
 		public void selectNewGun() {
 			int selection = gen.nextInt(guns.size());
 			activeTurret = guns.get(selection);
 			destinationX = displayWidth / 2 - activeTurret.getLocX();
 			// destinationY = activeTurret.getLocY();
 			activeGun = selection;
 			activeTurret = guns.get(selection);
 			print("" + selection + " at desintation " + destinationX + "\n");
 			moving = true;
 		}
 
 		public void move() {
 			int delX = 0, delY = 0;
 			if (abs(destinationY - locY) > 5) {
 				if (destinationY > locY) {
 					locY += speed;
 					delY = speed;
 
 				} else {
 					locY -= speed;
 					delY = -speed;
 				}
 			} else {
 				if (destinationX > 0) {
 					locX += speed;
 					delX = speed;
 					destinationX -= speed;
 				} else {
 					locX -= speed;
 					delX = -speed;
 					destinationX += speed;
 				}
 
 			}
 
 			moveTurrets(delX, delY);
 			print("%%" + (destinationX - locX));
 
 			if (abs(destinationY - locY) < 10 && abs(destinationX) < 10) {
 				moving = false;
 				shooting = true;
 				count = 0;
 				print("From Moving to Shooting.");
 			}
 		}
 
 		public void shoot() {
 			for (int i = 0; i < guns.size(); i++) {
 				if (guns.get(i).getLocX() > 0
 						&& guns.get(i).getLocX() < displayWidth)// aka on screen
 				{
 					guns.get(i).act();
 				}
 			}
 			count++;
 			if (count > 1000) {
 				shooting = false;
 			}
 		}
 
 		public void moveTurrets(int delX, int delY) {
 			for (int i = 0; i < guns.size(); i++) {
 				guns.get(i).increment(delX, delY);
 			}
 		}
 
 		public void displayTurrets() {
 			for (int i = 0; i < guns.size(); i++) {
 				guns.get(i).display();
 			}
 		}
 
 		public void checkTurrentHealths() {
 			for (int i = guns.size() - 1; i >= 0; i--) {
 				if (guns.get(i).getHealth() < 1) {
 					if (guns.get(i) == activeTurret)
 						shooting = false;
 
 					guns.remove(i);
 
 				}
 			}
 			if (guns.size() < 1) {
 				super.blowUp();
 			}
 		}
 
 		public ArrayList<Turret> getTurretList() {
 			return guns;
 		}
 
 		public void hit() {
 			// do nothing;
 		}
 	}
 
 	public class DinkyGun extends Gun {
 		public void shoot(int locX, int locY) {
 			new Bullet(locX, locY);
 		}
 	}
 
 	class Drone extends enemyShip {
 		Gun weapon;
 
 		boolean flip = true;
 
 		Drone(int startx, int starty, int speed, int imageIndex, int f, int h,
 				int p) {
 			super(startx, starty, speed + 10, imageIndex, f, h, p);
 			weapon = new DinkyGun();
 		}
 
 	}
 
 	void GameOverMessage(String msg) {
 		image(loadImage("Back.png"), displayWidth / 2, displayHeight / 12,
 				displayWidth, displayHeight / 6);
 		textFont(fontG);
 		fill(110, 50, 255);
 		textAlign(CENTER);
 		text(msg + "\nScore: " + points + "\nHigh Score: " + highscoretop,
 				displayWidth / 2, displayHeight / 2);
 
 		if (mousePressed && mouseY < displayHeight / 6) {
 			enemyShips.clear();
 			enemyBullets.clear();
 			playerBullets.clear();
 			items.clear();
 			activePowerUps.clear();
 			textAlign(LEFT);
 			textFont(f, 24);
 			fill(255);
 			showDeath = false;
 			showMenu = true;
 			playGame = false;
 			points = 0;
 			tick = 0;
 		}
 	}
 
 	abstract class Gun {
 		public void shoot(int xpos, int ypos) {
 		}
 	}
 
 	public class Hallucinate extends PowerUp {
 		Hallucinate(int locX, int locY) {
 			super(locX, locY,13);
 
 		}
 
 		public void doEffect() {
 			psychedelicMode = true;
 		}
 
 		public void removeEffect() {
 			psychedelicMode = false;
 		}
 
 	}
 
 	public class HelixGun extends Gun {
 		HelixGun() {
 		}
 
 		boolean left = false;
 
 		public void shoot(int xpos, int ypos) {
 			left = !left;
 			if (left)
 				new SinShot(xpos, ypos - 10, 2, 2);
 			else
 				new SinShot(xpos, ypos + 10, -2, 2);
 		}
 	}
 
 	class HelixShip extends enemyShip {
 		boolean flip, shooting;
 
 		HelixShip(int startx, int starty) {
 			super(startx, starty, 3, 9, 10, 40, 9);
 			flip = false;
 
 			weapon = new HelixGun();
 		}
 
 		public void shoot() {
 			if (flip)
 				weapon.shoot(locX + 40, locY);
 			else
 				weapon.shoot(locX - 40, locY);
 			flip = !flip;
 		}
 	}
 
 	public void importHighscore() {
 		// Open the file from the createWriter()
 		reader = createReader(highscoreFile);
 		if (reader == null) {
 			highscoretop = 0;
 			return;
 		}
 		String line;
 		try {
 			line = reader.readLine();
 		} catch (IOException e) {
 			e.printStackTrace();
 			line = null;
 		}
 		if (line != null) {
 			highscoretop = PApplet.parseInt(line);
 			println(highscoretop);
 		}
 		try {
 			reader.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void updateHighscore() {
 		if (highscoretop < points) {
 			highscoretop = points;
 			// Create a new file in the sketch directory
 			output = createWriter(highscoreFile);
 			output.println(highscoretop);
 			output.close(); // Writes the remaining data to the file & Finishes
 							// the file
 		}
 	}
 
 	abstract class Item extends Actor {
 		int worth;
 		int speed = 3;
 
 		Item(int posx, int posy, int imageIndex) {
 			locX = posx;
 			locY = posy;
 			img = loadedPics.get(imageIndex);
 			items.add(this);
 		}
 
 		public void move() {
 			locY += speed;
 			if (locY > displayHeight + 100)
 				removeSelf();
 			image(img, locX, locY);
 		}
 
 		public void removeSelf() {
 			for (int i = items.size() - 1; i >= 0; i--) {
 				Item p = (Item) items.get(i);
 				if (p == this) {
 					items.remove(i);
 					break;
 				}
 			}
 		}
 
 		public void act() {
 		}
 	}
 
 	public void keyPressed() {
 		if (key == ' ')
 			player.shoot();
 
 		if (key == 'c') // Clear highscore
 		{
 			highscoretop = 0;
 			// Create a new file in the sketch directory
 			output = createWriter(highscoreFile);
 			output.println(highscoretop);
 			output.close(); // Writes the remaining data to the file & Finishes
 							// the file
 		}
 
 		// this gets switched around when porting to android
 		// if(key == 'r')
 		if (key == CODED && keyCode == android.view.KeyEvent.KEYCODE_MENU) {
 			playGame = false;
 			showMenu = true;
 			showCredits = false;
 		}
 		if (key == CODED && keyCode == android.view.KeyEvent.KEYCODE_HOME) {
 			playGame = false;
 			showMenu = false;
 			showCredits = false;
 		}
 
 	}
 
 	public class Wave {
 		Wave(int n, int d) {
 			tick = 0;
 			wavedone = false;
 			wavenum = n;
 			difficulty = d;
 		}
 
 		boolean wavedone;
 		int wavenum;
 		int difficulty;
 		int tick;
 
 		public void spawn() {
 
 		}
 	}
 
 	public class Level {
 		Level(int n) {
 			wavedone = false;
 			levelnumber = n;
 			bgimg = 1 + gen.nextInt(6);
 			for (int i = 0; i < levelnumber % 5 * 2 + 1; i++) {
 				waves.add(new Wave(i, levelnumber / 5));
 			}
 			// bghandel.loadNewImage();
 		}
 
 		boolean wavedone;
 		int levelnumber;
 		int bgimg;
 		ArrayList<Wave> waves = new ArrayList<Wave>();
 	}
 
 	public class Menu {
 
 		PImage MenuImage;// = loadImage("MainMenu.png");;
 		float PlayX, PlayY, playSizeY, playSizeX; // Position of square button
 		float creditsX, creditsY, creditsSizeX, creditsSizeY;
 		float scoreX, scoreY, scoreSizeX, scoreSizeY;
 		float tutX, tutY, tutSizeX, tutSizeY;
 		float opX, opY, opSizeX, opSizeY;
 
 		Menu() {
 			playGame = false;
 			MenuImage = loadedPics.get(12);
 
 			PlayX = displayWidth / 13.2f;
 			PlayY = (displayHeight / 1.865f);
 			playSizeY = displayHeight / 7.8f;// Diameter of
 			playSizeX = displayWidth / 1.201f;
 
 			scoreX = displayWidth * (1 / 2.0f);
 			scoreY = displayHeight * (5 / 7.4f);
 			scoreSizeX = displayWidth / 2.402f;
 			scoreSizeY = displayHeight / 7.8f;
 
 			creditsX = displayWidth * (1 / 2.0f);
 			creditsY = displayHeight * (6 / 7.4f);
 			creditsSizeX = displayWidth / 2.402f;
 			creditsSizeY = displayHeight / 7.8f;
 
 			tutX = displayWidth * (0.07f);
 			tutY = displayHeight * (5 / 7.4f);
 			tutSizeX = displayWidth / 2.402f;
 			tutSizeY = displayHeight / 7.8f;
 
 			opX = displayWidth * (0.07f);
 			opY = displayHeight * (6 / 7.4f);
 			opSizeX = displayWidth / 2.402f;
 			opSizeY = displayHeight / 7.8f;
 
 		}
 
 		public void showMenu() {
 			if(musicReady)
 			mediaPlayer.start();
 			else
 				sound.buildPlayer();
 
 			spawning = false;
 			playGame = false;
 
 			image(MenuImage, displayWidth / 2, displayHeight / 2, displayWidth,
 					displayHeight);
 
 			if (overBox(PlayX, PlayY, playSizeX, playSizeY)) {
 				if (mousePressed == true) {
 
 					showMenu = false;
 					playGame = true;
 					
 				}
 			}
 			if (overBox(creditsX, creditsY, creditsSizeX, creditsSizeY)) {
 				if (mousePressed == true) {
 					showCredits = true;
 					showMenu = false;
 
 				}
 			}
 			if (overBox(tutX, tutY, tutSizeX, tutSizeY)) {
 				if (mousePressed == true) {
 					showInstructions = true;
 					showMenu = false;
 				}
 			}
 			if (overBox(opX, opY, opSizeX, opSizeY)) {
 				if (mousePressed == true) {
 					showOptions = true;
 					showMenu = false;
 				}
 			}
 			if (overBox(scoreX, scoreY, scoreSizeX, scoreSizeY)) {
 				if (mousePressed == true) {
 					showHighScore = true;
 					showMenu = false;
 				}
 			}
 
 		}
 
 		public boolean overBox(float a, float b, float w, float h) {
 			return (mouseX >= a && mouseX <= a + w && mouseY >= b && mouseY <= b
 					+ h);
 		}
 
 	}
 
 	class Money extends Item {
 		Money(int posx, int posy, int w) {
 			super(posx, posy, 2);
 			worth = w;
 			radius = 10;
 		}
 
 		public void act() {
 			this.removeSelf();
 			player.addMoney(worth);
 			println("+" + worth);
 		}
 	}
 
 	public class PlayerGunLev1 extends Gun {
 
 		boolean flip = false;
 
 		public void shoot(int xpos, int ypos) {
 			// Sounds.play(0);
 			if (flip)
 				new PlayerBullet(xpos + 12, ypos, 0, -20);
 			else
 				new PlayerBullet(xpos - 12, ypos, 0, -20);
 			flip = !flip;
 		}
 	}
 	public class PlayerGunLev2 extends Gun {
 		public void shoot(int xpos, int ypos) {
 			
				new PlayerBullet(xpos + 12, ypos, 1, -20);
 				new PlayerBullet(xpos , ypos, 0, -20);
				new PlayerBullet(xpos - 12, ypos, -1, -20);
 		}
 	}
 	
 	public class PlayerGunLev3 extends Gun {
 		public void shoot(int xpos, int ypos) {
 			new PlayerBullet(xpos - 12, ypos, -2, -20);
			new PlayerBullet(xpos + 12, ypos, 2, -20);
			new PlayerBullet(xpos +12, ypos, 0, -20);
			new PlayerBullet(xpos - 12, ypos, 0, -20);
 		}
 	}
 
 	public class PlayerGunLev4 extends Gun {
 		public void shoot(int xpos, int ypos) {
 			sound.play(sound.pew);
 			new PlayerBullet(xpos, ypos, 0, -20);
 			new PlayerBullet(xpos + 12, ypos, 3, -20);
 			new PlayerBullet(xpos - 12, ypos, -3, -20);
 			new PlayerBullet(xpos + 12, ypos, 6, -20);
 			new PlayerBullet(xpos - 12, ypos, -6, -20);
 		}
 	}
 	public class PlayerGunLev5 extends Gun {
 		public void shoot(int xpos, int ypos) {
 			sound.play(sound.pew);
 			new PlayerBullet(xpos, ypos, 1, -20);
 			new PlayerBullet(xpos, ypos, -1, -20);
 			new PlayerBullet(xpos + 12, ypos, 3, -20);
 			new PlayerBullet(xpos - 12, ypos, -3, -20);
 			new PlayerBullet(xpos + 12, ypos, 6, -20);
 			new PlayerBullet(xpos - 12, ypos, -6, -20);
 		}
 	}
 
 	public class PlayerBullet extends Projectile {
 		PlayerBullet(int locX, int locY, int xdisp, int ydisp) {
 			super(locX, locY, 1, xdisp, ydisp);
 			playerBullets.add(this);
 		}
 
 		public void removeSelf() {
 			for (int i = playerBullets.size() - 1; i >= 0; i--) {
 				Projectile p = (Projectile) playerBullets.get(i);
 				if (p == this) {
 					playerBullets.remove(i);
 					break;
 				}
 			}
 		}
 	}
 
 	class PlayerShip extends Ship {
 		int gunLev;
 		public PlayerShip(int xpos, int ypos) {
 			super(7);
 			dir = true;
 			radius = 25;
 			locX = xpos;
 			locY = ypos;
 			speed = 25;
 			weapon = new PlayerGunLev1();
 			gunLev = 1;
 		}
 
 		public void move() {
 			boolean flag = true;
 			int dX, dY;
 			float magnitude;
 			dX = mouseX - locX;
 			dY = mouseY - 80 - locY;
 			if (abs(dX) > 15 || abs(dY) > 15) {
 				magnitude = sqrt(dX * dX + dY * dY);
 				locX += PApplet.parseInt(speed * dX / (magnitude));
 				locY += PApplet.parseInt(speed * dY / (magnitude));
 			} else {
 				locX += dX;
 				locY += dY;
 			}
 			if (locX < 0)
 				locX = 0;
 			if (locX > displayWidth)
 				locX = displayWidth;
 			if (locY < 0)
 				locY = 0;
 			if (locY > displayHeight)
 				locY = displayHeight;
 
 			image(img, locX, locY);
 		}
 
 		boolean left = false;
 		public void hit()
 		{
 			incrementGunLev(-1);
 		}
 		public void incrementGunLev(int i)
 		{
 			gunLev += i;
 			if(gunLev <= 0)
 				this.blowUp();
 			else if(gunLev==1)
 				weapon = new PlayerGunLev1();
 			else if(gunLev==2)
 				weapon = new PlayerGunLev2();
 			else if(gunLev==3)
 				weapon = new PlayerGunLev3();
 			else if(gunLev==4)
 				weapon = new PlayerGunLev4();
 			else if(gunLev==5)
 				weapon = new PlayerGunLev5();
 			else gunLev -=i;
 		}
 		public void shoot() {
 			weapon.shoot(locX, locY);
 		}
 
 		public void blowUp() {
 			println("THE PLAYER HAS DIED");
 			super.blowUp();
 			showDeath = true;
 			playGame = false;
 		}
 
 		public void addMoney(int p) {
 			points += p;
 		}
 
 		public int getScore() {
 			return points;
 		}
 	}
 
 	public class PowerUp extends Item {
 		int counter, lifeSpan;
 
 		PowerUp(int posx, int posy,int imgIndex) {
 			super(posx, posy, imgIndex);
 			radius = 10;
 			activePowerUps.add(this);
 			counter = 0;
 			lifeSpan = 250;
 
 		}
 
 		public void act() {
 			this.removeSelf();
 			doEffect();
 		}
 
 		public void increment() {
 			counter++;
 			if (counter > lifeSpan) {
 				for (int i = activePowerUps.size() - 1; i >= 0; i--) {
 					PowerUp p = activePowerUps.get(i);
 					if (p == this) {
 						activePowerUps.remove(i);
 						removeEffect();
 						break;
 					}
 				}
 			}
 		}
 
 		public void doEffect() {
 		}
 
 		public void removeEffect() {
 		}
 	}
 	public class GunUp extends PowerUp
 	{
 		GunUp(int posX, int posY)
 		{
 			super(posX,posY,8);
 			lifeSpan = 0;
		}
		public void act()
		{
 			player.incrementGunLev(1);
 		}
		
		
 	}
 	
 	void makeRandPowerUp(int i, int j)
 	{
 		int b = gen.nextInt(2);
 		if (b == 0)
 			new Hallucinate(i,j);
 		if (b==1)
 			new GunUp(i,j);
 	}
 	
 	abstract class Projectile extends Actor {
 		int xdisp, ydisp;
 
 		Projectile(int xpos, int ypos, int imageIndex, int delx, int dely) {
 			locX = xpos;
 			locY = ypos;
 			this.img = loadedPics.get(imageIndex);
 			xdisp = delx;
 			ydisp = dely;
 			radius = 7;
 		}
 
 		public void move() {
 			locY += ydisp;
 			locX += xdisp;
 
 			if (locY < -100 || locY > displayHeight + 100 || locX < -100
 					|| locX > displayWidth + 100)
 				removeSelf();
 
 			display();
 		}
 
 		public void removeSelf() {
 			for (int i = enemyBullets.size() - 1; i >= 0; i--) {
 				Projectile p = (Projectile) enemyBullets.get(i);
 				if (p == this) {
 					enemyBullets.remove(i);
 					break;
 				}
 			}
 
 		}
 	}
 
 	public class Rocket extends Projectile {
 
 		Rocket(int xpos, int ypos, int h, int s) {
 			super(xpos, ypos, 3, h, s);
 			enemyBullets.add(this);
 		}
 	}
 
 	abstract class Ship extends Actor {
 		int health = 1;
 		boolean dir = false; // up cor. to true
 		Gun weapon;
 
 		Ship(int imageIndex) {
 			radius = 25;
 			img = loadedPics.get(imageIndex);
 		}
 
 		public void display() {
 			image(img, locX, locY);
 		}
 
 		public void move() {
 		}
 
 		public void move(int t)// used to pass the tick count to enemy ships
 		{
 		}
 
 		public void hit() {
 			health--;
 			if (health == 0)
 				blowUp();
 		}
 
 		public void shoot() {
 			weapon.shoot(locX, locY);
 		}
 
 		public void blowUp() {
 			println("BOOM");// we can add in animation here
 			removeSelf();
 		}
 
 		public void removeSelf() {
 			for (int i = enemyShips.size() - 1; i >= 0; i--) {
 				Ship p = (Ship) enemyShips.get(i);
 				if (p == this) {
 					enemyShips.remove(i);
 					break;
 				}
 			}
 		}
 	}
 
 	class SinShot extends Projectile {
 		int xinit, yinit;
 		boolean flip = false;
 
 		SinShot(int xpos, int ypos, int dispx, int dispy) {
 			super(xpos, ypos, 0, dispx, dispy);
 			xinit = xpos;
 			yinit = ypos;
 			enemyBullets.add(this);
 		}
 
 		public void move() {
 			locY += ydisp;
 			if (flip)
 				locX += xdisp;
 			else
 				locX -= xdisp;
 			if (locX > xinit + 50 || locX < xinit - 50)
 				flip = !flip;
 			if (locY < -100 || locY > displayHeight + 100 || locX < -100
 					|| locX > displayWidth + 100)
 				removeSelf();
 			display();
 		}
 
 	}
 
 	public class Sounds {
 		public int pew, bgsong;
 
 		public void setUp() {
 			try { // loading these files can throw an exception and therefore
 					// you HAVE to have a way to handle those events
 				pew = soundPool.load(assetManager.openFd("pew.ogg"), 0); // load
 																			// the
 																			// files
 				bgsong = soundPool.load(
 						assetManager.openFd("bitswithbyte.ogg"), 0);
 			} catch (IOException e) {
 				print("OOOOPPPPPPPPPPPPPPPPPPPSSSSSSSSSSSSSSSS");
 				e.printStackTrace(); // you can leave this empty...or use some
 										// other way to notify the
 										// user/developer something went wrong
 				buildPlayer();
 			}
 		}
 
 		public void play(int sound) {
 			soundPool.play(sound, 1, 1, 0, 0, 1);// no idea why this is to be
 													// quite honest
 		}
 
 		public void buildPlayer() {
 			AssetFileDescriptor fd = null;
 
 			try {
 				fd = assetManager.openFd("bitswithbyte.ogg");
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			try {
 				mediaPlayer.setDataSource(fd.getFileDescriptor(),
 						fd.getStartOffset(), fd.getLength());
 			} catch (IllegalArgumentException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalStateException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
 
 			mediaPlayer.setLooping(true);
 			try {
 				mediaPlayer.prepare();
 				musicReady = true;
 			} catch (IllegalStateException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	public class Spawner {
 		boolean flip = false;
 		int xLoc = 0, yLoc = 0;
 
 		public void spawn(int l)// l for level , t for ticks
 		{
 
 			if (l == 0)
 				if (tick % 50 == 0) {
 
 					if (flip)
 						enemyShips.add(new Drone(displayWidth / 4, 0, 3, 4, gen
 								.nextInt(50) + 50, 5, 2));
 					else
 						enemyShips.add(new Drone(3 * displayWidth / 4, 0, 3, 4,
 								gen.nextInt(50) + 50, 5, 1));
 					flip = !flip;
 				}
 
 			if (l == 1)
 				if (tick % 25 == 0) {
 
 					if (flip)
 						enemyShips.add(new Drone(displayWidth / 4, 0, 5, 4, gen
 								.nextInt(50) + 50, 1, 2));
 					else
 						enemyShips.add(new Drone(3 * displayWidth / 4, 0, 5, 4,
 								gen.nextInt(50) + 50, 1, 1));
 					flip = !flip;
 				}
 			if (l == 2)
 				if (tick % 25 == 0) {
 					enemyShips.add(new Drone(displayWidth / 4, 0, 5, 4, gen
 							.nextInt(50) + 50, 1, 8));
 					enemyShips.add(new Drone(3 * displayWidth / 4, 0, 5, 4, gen
 							.nextInt(50) + 50, 1, 7));
 				}
 			if (l == 3)
 				if (tick % 20 == 0) {
 					xLoc += 30;
 					enemyShips.add(new Drone(xLoc, 0, 5, 4,
 							gen.nextInt(50) + 50, 1, 0));
 					if (xLoc > displayWidth)
 						xLoc = 0;
 				}
 			if (l == 4)// figure this out its garbage
 				if (tick % 40 == 0) {
 					yLoc = gen.nextInt(200) + 50;
 					enemyShips.add(new HelixShip(0, yLoc));
 				}
 
 		}
 	}
 
 	public class SpiralGun extends Gun {
 		float degree = 0;
 		int speed = 5;
 		int dispx, dispy;
 
 		SpiralGun() {
 		}
 
 		public void shoot(int locX, int locY) {
 			degree += PI / 12;
 			dispx = (int) (speed * sin(degree));
 			dispy = (int) (speed * cos(degree));
 			new Bullet(locX, locY, dispx, dispy);
 			// print(""+dispx +","+ dispy);
 		}
 	}
 
 	class SpiralShip extends enemyShip {
 		Gun weapon;
 		int count = 0;
 		boolean shooting = false;
 		boolean flip = true;
 
 		SpiralShip(int startx, int starty, int speed, int imageIndex, int f,
 				int h, int p) {
 			super(startx, starty, speed, imageIndex, 1, h, p);
 			weapon = new SpiralGun();
 		}
 
 		public void shoot() {
 			if (count % 100 == 0)
 				shooting = !shooting;
 			if (shooting)
 				weapon.shoot(locX, locY);
 		}
 	}
 
 	class SpreadGunE extends Gun {
 		public void shoot(int xpos, int ypos) {
 			new Bullet(xpos, ypos, 0, 5);
 			new Bullet(xpos, ypos, 1, 4);
 			new Bullet(xpos, ypos, -1, 4);
 			new Bullet(xpos, ypos, 2, 3);
 			new Bullet(xpos, ypos, -2, 3);
 		}
 	}
 
 	public class StarGun extends Gun {
 		float degree = 0;
 		int speed = 5;
 		int dispx, dispy;
 
 		StarGun() {
 		}
 
 		public void shoot(int locX, int locY) {
 			for (degree = 0; degree < 2 * PI; degree += PI / 12) {
 				dispx = (int) (speed * sin(degree));
 				dispy = (int) (speed * cos(degree));
 				new Bullet(locX, locY, dispx, dispy);
 				// print(""+dispx +","+ dispy);
 			}
 		}
 	}
 
 	class TestGun extends Gun {
 		public void shoot(int xpos, int ypos) {
 			new Bullet(xpos, ypos, 0, 5);
 		}
 	}
 
 	public class Turret extends Actor {
 		int health, freq, count;
 		Gun weapon;
 
 		Turret(Gun g, int hlth) {
 			health = hlth;
 			radius = 15;
 			weapon = g;
 			img = loadedPics.get(6);
 			freq = gen.nextInt(10) + 10;
 			count = 0;
 		}
 
 		public void act() {
 			count++;
 			if (count % freq == 0)
 				shoot();
 		}
 
 		public void increment(int delX, int delY) {
 			locX += delX;
 			locY += delY;
 		}
 
 		public void moveTo(int newx, int newy) {
 			locX = newx;
 			locY = newy;
 		}
 
 		public void shoot() {
 			weapon.shoot(locX, locY);
 
 		}
 
 		public int getHealth() {
 			return health;
 		}
 
 		public void hit() {
 			// s.play(2);
 			health--;
 		}
 	}
 
 	abstract class enemyShip extends Ship {
 		int path, count = 0, lifeTime = 500, freq, speed, xinit, yinit,
 				imageIndex;
 		boolean flip = false, flashing = false;
 
 		enemyShip(int startx, int starty, int s, int imgIndex, int f, int h,
 				int p) {
 			super(imgIndex);
 			imageIndex = imgIndex;
 			locX = startx;
 			locY = starty;
 			xinit = startx;
 			yinit = starty;
 			speed = s;
 			freq = f;
 			path = p;
 			health = h;
 			weapon = new DinkyGun();
 		}
 
 		public void act() {
 			count++;
 			display();
 
 			if (count > lifeTime)
 				flyAway();
 			else {
 				move();
 				if (count % freq == 0)
 					shoot();
 			}
 
 			if (flashing) {
 				revert();
 			}
 			if (locY < -400 || locY > displayHeight + 400 || locX < -400
 					|| locX > displayWidth + 400)
 				removeSelf();
 		}
 
 		public void move() {
 			if (path == 0) {
 				locY += speed;
 			}
 			if (path == 1) {
 				if (locY < displayHeight / 3) {
 					locY += speed;
 				} else {
 					locY += speed / sqrt(2);
 					locX -= speed / sqrt(2);
 				}
 			}
 			if (path == 2) {
 				if (locY < displayHeight / 3) {
 					locY += speed;
 				} else {
 					locY += speed / sqrt(2);
 					locX += speed / sqrt(2);
 				}
 			}
 			if (path == 3) {
 				if (count % 3 == 0)
 					locY += 1;
 				if (flip)
 					locX += speed;
 				else
 					locX -= speed;
 				if (locX > 8 * displayWidth / 9 || locX < displayWidth / 9) {
 					locY += speed * 3;
 					flip = !flip;
 				}
 			}
 			if (path == 4) {
 				if (count < 100) {
 					locX += speed / 2;
 					locY += speed;
 				} else {
 					locX -= speed / 2;
 					locY += speed;
 				}
 			}
 			if (path == 5) {
 				if (count < 50) {
 					locY += speed;
 					locX -= speed / 2;
 				} else if (count < 125) {
 					locY += speed;
 					locX += speed / 2;
 				} else {
 					locY += speed / 2;
 				}
 			}
 			if (path == 6) {
 				locY += speed;
 				locX += sin(count * 3.14f / 6) * 5;
 			}
 			if (path == 7) {
 				locY += speed;
 				if (flip)
 					locX += speed;
 				else
 					locX -= speed;
 				if (locX < xinit - 200 || locX > xinit + 200)
 					flip = !flip;
 			}
 			if (path == 8) {
 				locY += speed;
 				if (flip)
 					locX -= speed;
 				else
 					locX += speed;
 				if (locX < xinit - 200 || locX > xinit + 200)
 					flip = !flip;
 			}
 			if (path == 9) {
 				locX += speed;
 			}
 
 		}
 
 		public void flyAway() {
 			locY += speed * 2;
 			this.display();
 		}
 
 		public void blowUp() {
 			int w = gen.nextInt(20) + 1;
 			new Money(locX, locY, w);
 			int randomInt = gen.nextInt(10);
 			if (randomInt == 1) {
 				makeRandPowerUp(locX,locY);
 			}
 			removeSelf();
 		}
 
 		public void shoot() {
 			weapon.shoot(locX, locY);
 		}
 
 		public void setGun(Gun g) {
 			weapon = g;
 		}
 
 		public void hit() {
 			flash();
 			health--;
 			if (health == 0)
 				blowUp();
 		}
 
 		public void flash() {
 			img = loadedPics.get(11);
 			flashing = true;
 			// s.play(1);
 		}
 
 		public void revert() {
 			img = loadedPics.get(imageIndex);
 			flashing = false;
 		}
 	}
 
 	// SoundPool soundPool;
 	// AssetManager assetManager;
 	// static public int PEW;
 	//
 	// public void setUp()
 	// {
 	// soundPool = new SoundPool(20, AudioManager.STREAM_MUSIC, 0); //(max #of
 	// streams, stream type, source quality) - see the android reference for
 	// details
 	// assetManager = this.getAssets();
 	// try
 	// {
 	// pew = soundPool.load(assetManager.openFd("\\sounds\\pew.mp3"), 0); //load
 	// the files
 	// }
 	// catch (IOException e) {
 	// print("Opps..");
 	// e.printStackTrace(); //you can leave this empty...or use some other way
 	// to notify the user/developer something went wrong
 	// }
 	// }
 	// public void play(int i)
 	// {
 	// // soundPool.play(i, 1, 1, 0, 0, 1);//no idea why this is to be quite
 	// honest
 	// }
 	//
 	// vdsv
 
 	public int sketchWidth() {
 		return displayWidth;
 	}
 
 	public int sketchHeight() {
 		return displayHeight;
 	}
 
 }
