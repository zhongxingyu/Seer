 package tetrix.sound;
 
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 
 public class SoundEffects {
 	private static Sound sfxShot;
 	private static Sound sfxBlowUp;
 	private static Sound sfxStartGame;
 	private static Sound sfxMenuClick;
 	private static Sound sfxHighScore;
 	private static Sound sfxDeath;
 
 	public SoundEffects() throws SlickException{
 		sfxShot = new Sound("Sound/music.wav");		// Sound of the bullet when you shoot
 		sfxBlowUp = new Sound("Sound/test.wav");	// Sound of a block blown up
 		sfxStartGame = new Sound("Sound/test.wav");	// Sound when you start the game 
 		sfxMenuClick = new Sound("Sound/test.wav");	// Sound of a menu click
 		sfxHighScore = new Sound("Sound/test.wav");	// Sound when you break a new High Score
 		sfxDeath = new Sound("Sound/test.wav");		// Sound when it is Game Over
 	}
<<<<<<< HEAD
 
 	public void shotPlay(){
 		sfxShot.play();
 	}
 
 	public void shotLoop(){
 		sfxShot.loop();
 	}
 
 	public void shotStop(){
 		sfxShot.stop();
 	}
 
 	public void blowUpPlay(){
 		sfxBlowUp.play();
 	}
 
 	public void blowUpLoop(){
 		sfxBlowUp.loop();
=======
	public void play(){
		System.out.println("Ljud OK");
		sfx.play();
		
>>>>>>> 6bec99df6d9e6a1b58755413cae2c8ca377661aa
 	}
 
 	public void blowUpStop(){
 		sfxBlowUp.stop();
 	}
 
 	public void startGamePlay(){
 		sfxStartGame.play();
 	}
 
 	public void startGameLoop(){
 		sfxStartGame.loop();
 	}
 
 	public void startGameStop(){
 		sfxStartGame.stop();
 	}
 
 	public void menuClickPlay(){
 		sfxMenuClick.play();
 	}
 
 	public void menuClickLoop(){
 		sfxMenuClick.loop();
 	}
 
 	public void menuClickStop(){
 		sfxMenuClick.stop();
 	}
 
 	public void highScorePlay(){
 		sfxHighScore.play();
 	}
 
 	public void highScoreLoop(){
 		sfxHighScore.loop();
 	}
 
 	public void highScoreStop(){
 		sfxHighScore.stop();
 	}
 
 	public void deathPlay(){
 		sfxDeath.play();
 	}
 
 	public void deathLoop(){
 		sfxDeath.loop();
 	}
 
 	public void deathStop(){
 		sfxDeath.stop();
 	}
 

	public static void main(String[]args) throws SlickException{
		SoundEffects se = new SoundEffects();
	}
 }
