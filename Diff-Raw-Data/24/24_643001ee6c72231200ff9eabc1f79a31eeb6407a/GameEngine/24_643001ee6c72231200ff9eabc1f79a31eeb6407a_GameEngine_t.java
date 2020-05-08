 package edu.id.meteordodge;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 
 public class GameEngine {
 	private static ArrayList<Meteor> meteors;
 	private static ArrayList<FloatingPeople> people;
 	private static ArrayList<Smoke> smoke;
 	private static ArrayList<GodBullets> bullets;
 	private static Player john;
 	private Timer randomTimer = new Timer();
 	private Timer randomTimer2 = new Timer();
 	private Timer randomTimer3 = new Timer();
 	private Timer randomTimer4 = new Timer();
 	private Timer randomTimer5 = new Timer();
 	private static int tiltyThing;
 	private static int timer;
 	private int run = 0;
 	private int turns;
 	private boolean initialPerson;
 	private int score;
 	private String scoreText;
 	private int lives;
 	private String livesText;
 	private boolean playerActive;
 	private boolean smokeA;
 	private boolean godTierAvailable;
 	private boolean godTierActive;
 	private int gT;
 	private int gTD;
 	private String picture;
 	private String chargeText;
 	private int bulletsRemaining;
 	private String bulletText;
 	/**
 	 * Sound.
 	 */
 	public void playSong(){
 		MP3 song = new MP3("Music.mp3");
 		song.play();
 	}
 	public void runTimers(){
 		//Smoke Timer
 		randomTimer.schedule(new TimerTask(){
 			public void run() {
 				if(godTierActive == false){
 					smoke.add(new Smoke(john));
 					smokeA = true;
 				}
 			}
 		}, 0, 175);
 		//God Tier Timer
 		randomTimer2.scheduleAtFixedRate(new TimerTask(){
 			public void run() {
 				gT += 5;
 				if (gT<=100)
 					chargeText=("" + gT);
 				if (gT >= 100)
 					godTierAvailable = true;
 			}
 		}, 0, 1000);
 		//God Tier Countdown
 		randomTimer3.scheduleAtFixedRate(new TimerTask(){
 			public void run() {
 				if (godTierActive == true){
 					gTD -= 10;
 					chargeText = (""+gTD);
 				}
 				if (gTD<=0){
 					godTierActive = false;
 					gT = 0;
 					gTD = 100;
 					bulletText = "25";
 				}
 			}
 		}, 0, 1000);
 		//RandomPerson Timer
 		randomTimer4.scheduleAtFixedRate(new TimerTask(){
 			public void run() {
 				people.remove(0);
 				people.add(new FloatingPeople(playerActive));
 				initialPerson = false;
 			}
 		}, 5000, 30000);
 		//Score Timer
 		randomTimer5.scheduleAtFixedRate(new TimerTask(){
 			public void run() {
 				if (playerActive == true){
 					score ++;
 					if (playerActive == true)
 						scoreText = "" + score;
 				}
 			}
 		}, 1000, 1000);
 	}
 	public void reset(){
 		meteors = new ArrayList<Meteor>();
 		people = new ArrayList<FloatingPeople>();
 		smoke = new ArrayList<Smoke>();
 		bullets = new ArrayList<GodBullets>();
 		john = new Player();
 		tiltyThing = 0;
 		timer = 100;
 		turns = 0;
 		initialPerson = true;
 		score = 0;
 		scoreText = "";
 		lives = 2;
 		livesText = "2";
 		playerActive = true;
 		smokeA = true;
 		godTierAvailable = false;
 		godTierActive = false;
 		gT = 0;
 		gTD = 100;
 		picture = "";
 		chargeText = "";
 		bulletsRemaining = 25;
 		bulletText = "25";
 	}
 	/**
 	 * Constructor
 	 */
 	public GameEngine(){
 	}
 
 
 	/**
 	 * Runs the actual Game.
 	 */
 	public void playGame(){
 		if (run == 1)
 			reset();
 		Font font = new Font("Sans Serif", Font.BOLD, 55);
 		StdDraw.setCanvasSize(1600, 900);
 		StdDraw.setXscale(0,1600);
 		StdDraw.setYscale(0,900);
 		StdDraw.setFont(font);
 		people.add(new FloatingPeople(playerActive));
 		smoke.add(new Smoke(john));
 		while (true) {
 			//Meteor spawn timer.
 			if (timer>=20){
 				meteors.add(new Meteor());
 				timer = 0;
 			}
 			//Move/tilt john.
 			tiltyThing = john.movePlayer();
 			john.checkBounds();
 
 			//Print background/ random floating people.
 			StdDraw.clear();
 			StdDraw.picture(800,450, "LOWAS.png", 1800, 1100);
 			people.get(0).runPeople(turns, initialPerson);
 			turns ++;
 
 			//Activates meteor/player spawns/movements while John is alive.
 			if (checkMeteors(john) == false) {
 				lives--;
 				livesText = "" + (lives);
 				if (lives == 0)
 					livesText = "Last life!";
 				if (lives == -1)
 					livesText = "None. GAME OVER.";
 			}
 			if (john.godTier(godTierAvailable) == true){
 				godTierActive = true;
 				godTierAvailable = false;
 				bulletsRemaining = 25;
 			}
 			if (godTierActive == true)
 				picture = "GodTierJohn.png";
 			if (playerActive == true && john.fire() == true && godTierActive == true && bulletsRemaining > 0){
 				bullets.add(new GodBullets(john));
 				bulletsRemaining--;
 				bulletText = ("" + bulletsRemaining);
 			}
 			else if (godTierActive == false)
 				picture = "player.png";
 			if ((lives>=0) && (playerActive == true)){
 				if (tiltyThing == 0){
 					StdDraw.picture(john.getX(), john.getY(), picture, 105, 130);
 				}
 				else if (tiltyThing == 1)
 					StdDraw.picture(john.getX(), john.getY(), picture, 105, 130, 10);
 				else if (tiltyThing == 2)
 					StdDraw.picture(john.getX(), john.getY(), picture, 105, 130, -10);
 				checkOutOfBounds();
 				//Smoke Deleter
 				for (int i =0; i<smoke.size(); i++){
 					if (smokeA == true){
 						smokeCheck(i, smoke);
 						smokeA = false;
 					}
 					smoke.get(i).runSmoke();
 				}
 				checkBullets();
 				moveMeteors();
 				printMeteors();
 				for (int i =0; i<bullets.size(); i++){
 					bullets.get(i).moveBullet();
 					bullets.get(i).printBullet();
 				}
 				displayScore();
 				displayLives();
 				displayCharge();
 				if (godTierActive == true)
 					displayBullets();
 				StdDraw.show(38);
 				timer += (int) (Math.random() * 10);
 			}
 			//Cuts off all movement when John dies.
 			else{
 				playerActive = false;
 				StdDraw.clear();
 				StdDraw.picture(800,450, "LOWAS.png", 1800, 1100);
 				//people.get(0).runPeople(turns, initialPerson);
 				//turns ++;
 				displayScore();
 				displayLives();
 				StdDraw.text(790, 650, "Congratulations!");
 				StdDraw.text(800, 590, ("You survived for " + scoreText + " seconds!"));
 				StdDraw.text(790, 530, "Thanks for playing!");
 				//
 				StdDraw.picture(790, 230, "Play Again Button.png", 1000, 470);
 				//
 				StdDraw.picture(1500, 100, "Exit Game Button.png", 190, 130);
 				StdDraw.show(1);
 				if (StdDraw.mousePressed() == true && StdDraw.mouseX()<1290 && StdDraw.mouseX()>290 && StdDraw.mouseY()<465 && StdDraw.mouseY()> -5){
					StdDraw.mousePressed = false;
 					run = 1;
 					playGame();
 				}
 				if (StdDraw.mousePressed() == true && StdDraw.mouseX()<1595 && StdDraw.mouseX()>1405 && StdDraw.mouseY()<165 && StdDraw.mouseY()> -35){
					StdDraw.mousePressed = false;
 					System.exit(0);
 				}
 			}
 		}
 	}
 
 
 
 
 	/**
 	 * Moves all meteors in list meteors down by FALL_RATE.
 	 */
 	public void moveMeteors(){
 		for (int i = 0; i<meteors.size(); i++){
 			meteors.get(i).move();
 		}
 	}
 
 	/**
 	 * Prints all meteors in list meteors.
 	 */
 	public void printMeteors(){
 		for (int i = 0; i<meteors.size(); i++){
 			StdDraw.picture(meteors.get(i).getX(), meteors.get(i).getY(), "meteor.png", 160, 160);
 		}
 	}
 
 	/**
 	 * Checks all meteors in list meteors for collision with player.
 	 * @param john
 	 * @return
 	 */
 	public boolean checkMeteors(Player john){
 		for (int i = 0; i<meteors.size(); i++){
 			if (meteors.get(i).checkCollision(john) == true){
 				meteors.remove(i);
 				return false;
 			}
 		}
 		return true;
 	}
 	public void checkBullets(){
 		for (int i = 0; i<bullets.size(); i++){
 			int a = 0;
 			for (int j = 0; j<meteors.size(); j++){
 				if (a == 0){
 					if (bullets.get(i).checkCollision(meteors.get(j))== true){
 						meteors.remove(j);
 						bullets.remove(i);
 						a = 1;
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Deletes off-screen meteors.
 	 */
 	public void checkOutOfBounds(){
 		for(int i = 0; i<meteors.size(); i ++){
 			if (meteors.get(i).getY()+80 < 0){
 				meteors.remove(i);
 			}
 		}
 	}
 
 	public void checkOutOfBoundsBullets(){
 		for(int i = 0; i<bullets.size(); i ++){
 			if (bullets.get(i).getY()-80 > 990){
 				bullets.remove(i);
 			}
 		}
 	}
 
 	/**
 	 * Prints Score.
 	 */
 	public void displayScore(){
 		StdDraw.setPenColor(Color.DARK_GRAY);
 		StdDraw.filledRectangle(800, 905, 1600, 40);
 		if (godTierActive == true)
 			StdDraw.filledRectangle(825, 830, 340, 50);
 		StdDraw.setPenColor(Color.WHITE);
 		StdDraw.textRight(1575, 900, ("Score: " + scoreText));
 	}
 	public void displayLives(){
 		StdDraw.textLeft(-20, 900, ("Lives: " + livesText));
 	}
 	public void displayCharge(){
 		StdDraw.text(830, 900, ("God Tier Charge: " + chargeText + "%"));
 	}
 	public void displayBullets(){
 		StdDraw.text(865, 815, ("Wind Remaining: " + bulletText));
 		StdDraw.picture(530, 825, "breath.png", 70, 46);
 	}
 	public void smokeCheck(int i, ArrayList<Smoke> smoke){
 		if (smoke.get(i).getY() < 80)
 			smoke.remove(i);
 	}
 }
