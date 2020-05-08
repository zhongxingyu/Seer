 package Game;
 
 import java.awt.event.KeyEvent;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import com.jogamp.graph.curve.opengl.Renderer;
 
 import GameObjects.GameChar;
 import Helpers.Color;
 import Maths.Vector2;
 
 /**
  * Updates the Menu 
  * @author Thuy-Anh Le
  * @author Chi-Wing Sit
  *
  */
 public class Menu {
 	
 	//Instantiates all the titles 
 	Renderer render;
 	GameChar createUser;
 	GameChar loadUser;
 	GameChar twoPlayer;
 	GameChar quit;
 	GameChar background;
 	static GameChar enterUsername;
 	static GameChar startGame;
 	static GameChar instructions;
 	static GameChar statistics;
 	static GameChar highScores;
 	GameChar instructionTable;
 	GameChar instructionFont;
 	GameChar easy;
 	GameChar medium;
 	GameChar hard;
 	GameChar resumeGame;
 	GameChar backToMenu;
 	GameChar quitPause;
 	GameChar statisticsFont;
 	GameChar highScoresFont;
 
 	public int loginSuccessful = 0;
 	public int usernameAvailable = 0;
 	public int counter = 0;
 	public int pressCount = 0;
 	public boolean back = false;
 
 	//Instantiates all the boolean variables required to indicate the state of the menu 
 	String inputUsername = "";
 	public boolean inEnterUsernameNew = false;
 	public boolean inEnterUsername = false;
 	public static boolean inGameMenu = false;
 	public boolean inInstructions = false;
 	public boolean showing = true;
 	public boolean showStats = true;
 	public boolean inHighScores = false;
 	public boolean inStatistics = false;
 	public boolean showHS = true;
 	public static boolean inStartMenu = true;
 	public static boolean inLevelMenu = false;
 	public static boolean isMedium = false;
 	public static boolean isHard = false;
 
 	public Menu(){ 
 		Controls.menuCounter = 0;
 		background = new GameChar();
 		createUser = new GameChar();
 		loadUser = new GameChar();
 		twoPlayer = new GameChar();
 		quit = new GameChar();
 		background.objectRenderer.SetTexture("background");
 		background.transform.size = new Vector2(60,50);
 		createUser.objectRenderer.SetTexture("createUser");
 		createUser.transform.size = new Vector2 (30,10);
 		createUser.transform.position = new Vector2 (0,22);
 		createUser.rigidBody.frictionCoefficient = 0.1f;
 		loadUser.objectRenderer.SetTexture("loadUser");
 		loadUser.transform.size = new Vector2 (30,10);
 		loadUser.transform.position = new Vector2 (0,10);
 		loadUser.rigidBody.frictionCoefficient = 0.1f;
 		twoPlayer.objectRenderer.SetTexture("twoPlayer");
 		twoPlayer.transform.size = new Vector2 (30,10);
 		twoPlayer.transform.position = new Vector2 (0,-3);
 		twoPlayer.rigidBody.frictionCoefficient = 0.1f;
 		quit.objectRenderer.SetTexture("quit");
 		quit.transform.position = new Vector2 (0,-8);
 		quit.transform.size = new Vector2 (30,10);
 		MainGame.controls.recordKey = true;
 
 	}
 	/** 
 	 * Updates all the states of the menu: the StartMenu, the GameMenu, the LevelMenu, the
 	 * the instructions, the statistics, the high scores and the enterUsername page. 
 	 */
 	public void Update() { 
 		
 		if(MainGame.inStartMenu){
 			initStartMenu();
 			MainGame.inStartMenu = false;
 			inStartMenu = true;
 		}
 		
 		if (inStartMenu){
 			updateStartMenu();
 		}
 		if(inGameMenu){
 			updateGameMenu();
 		}
 		if(inLevelMenu){
 			updateLevelMenu();
 		}
 		
 		//Instantiates the Pause Menu and then updates it
 		if(MainGame.inPauseGameMode){
 			if(counter == 0){
 				initPauseMenu();
 				loginSuccessful = 0;
 				counter++;
 			}
 			updatePauseMenu();
 		} 
 		
 		if (inEnterUsernameNew){
 			inEnterUsernameNew();
 		} 
 		if (inEnterUsername){
 			inEnterUsername();
 		}
 		if(inInstructions){
 			instructionFont.Update();
 			instructionTable.Update();
 		}
 
 		if (inHighScores){
 			showHS = true;
 			inHighScores();
 			highScoresFont.Update();
 		}
 		if (inStatistics){
 			showStats = true;
 			inStatistics();
 			statisticsFont.Update();
 		}
 
 		if(MainGame.controls.isPressed(KeyEvent.VK_BACK_SPACE) && pressCount==0){
 			back = true;
 			pressCount++;
 		}
 		if(!MainGame.controls.isPressed(KeyEvent.VK_BACK_SPACE)){
 			pressCount = 0;
 			back = false;
 		}
 
 		//Instantiates all the necessary menus when the back button is pressed according to which 
 		//state you are. 
 		if (back){
 
 			if(inGameMenu){
 				initStartMenu();
 				inGameMenu = false;
 				inEnterUsername = false;
 				inEnterUsernameNew = false;
 				inStartMenu = true;
 				back = false;
 			}
 		
 			if(inInstructions){
 				Controls.menuCounter = 0;
 				inInstructions = false;
 				inGameMenu = true;
 				back = false;
 				instructionTable.Delete();
 				instructionFont.Delete();
 				initGameMenu();
 			} 
 			if (inStatistics){
 				Controls.menuCounter = 0;
 				inStatistics = false;
 				showStats = false;
 				inGameMenu = true;
 				back = false;
 				statisticsFont.Delete();
 				initGameMenu();
 			}
 			if (inHighScores){
 				initGameMenu();
 				inHighScores = false;
 				inGameMenu = true;
 				Controls.menuCounter = 0;
 				highScoresFont.Delete();
 				showHS = false;
 				back = false;
 			}
 			if(inLevelMenu){
 				inLevelMenu = false;
 				inGameMenu = true;
 				back = false;
 				Controls.menuCounter = 0;
 				easy.Delete();
 				medium.Delete();
 				hard.Delete();
 				initGameMenu();
 			}
 
 		}
 	}
 	
 	/**
 	 * Updates the game objects (createUser, loadUser, twoPlayer and quit) of the StartMenu.
 	 */
 	private void updateStartMenu() {
 		switch(Controls.menuCounter){
 
 		// Create New User
 		case 0:
 			createUser.objectRenderer.SetTexture("createUserOnHover");
 			loadUser.objectRenderer.SetTexture("loadUser");
 			twoPlayer.objectRenderer.SetTexture("twoPlayer");
 			quit.objectRenderer.SetTexture("quit");
 			if (MainGame.controls.isPressed(KeyEvent.VK_ENTER)){
 				inStartMenu = false;
 				inEnterUsernameNew = true;
 				initEnterUsername();
 			}
 			break;
 
 			// Load User
 		case 1: 
 			createUser.objectRenderer.SetTexture("createUser");
 			loadUser.objectRenderer.SetTexture("loadUserOnHover");
 			twoPlayer.objectRenderer.SetTexture("twoPlayer");
 			quit.objectRenderer.SetTexture("quit");
 			if (MainGame.controls.isPressed(KeyEvent.VK_ENTER)){
 				inStartMenu = false;
 				inEnterUsername = true;
 				initEnterUsername();
 			}
 			break;
 
 			// Two Player Mode
 		case 2:
 			createUser.objectRenderer.SetTexture("createUser");
 			loadUser.objectRenderer.SetTexture("loadUser");
 			twoPlayer.objectRenderer.SetTexture("twoPlayerOnHover");
 			quit.objectRenderer.SetTexture("quit");
 			if (MainGame.controls.isPressed(KeyEvent.VK_ENTER)){
 				inStartMenu = false;
 				MainGame.inMenu = false;
 				MainGame.twoPlayerMode = true;
 				createUser.Delete();
 				loadUser.Delete();
 				twoPlayer.Delete();
 				quit.Delete();
 			}
 			break;
 
 			// Quit
 		case 3:
 			createUser.objectRenderer.SetTexture("createUser");
 			loadUser.objectRenderer.SetTexture("loadUser");
 			twoPlayer.objectRenderer.SetTexture("twoPlayer");
 			quit.objectRenderer.SetTexture("quitOnHover");
 			if (MainGame.controls.isPressed(KeyEvent.VK_ENTER)){
 				System.exit(0);
 			}
 			break;
 		}
 		background.Update();
 		createUser.Update();
 		loadUser.Update();
 		twoPlayer.Update();
 		quit.Update();
 	}
 	
 	/**
 	 * Updates the game objects(resumeGame, backToMenu, quitPause) of the PauseMenu
 	 */
 	private void updatePauseMenu(){
 		switch(Controls.menuCounter){
 		
 		//Resume Game
 		case 0:
 			resumeGame.objectRenderer.SetTexture("resumeGameOnHover");
 			backToMenu.objectRenderer.SetTexture("mainMenu");
 			quitPause.objectRenderer.SetTexture("quit");
 			if(MainGame.controls.isPressed(KeyEvent.VK_ENTER)){
 				MainGame.inMenu = false;
 				MainGame.update = true;
 				counter = 0;
 			}
 			break;
 			
 		//Back to Main Menu
 		case 1: 
 			resumeGame.objectRenderer.SetTexture("resumeGame");
 			backToMenu.objectRenderer.SetTexture("mainMenuOnHover");
 			quitPause.objectRenderer.SetTexture("quit");
 			if(MainGame.controls.isPressed(KeyEvent.VK_ENTER)){
 				initStartMenu();
 				inStartMenu = true;
 				MainGame.inPauseGameMode = false;
 				MainGame.update = true;
 				back = false;
 			}
 			break;
 			
 		//Quit
 		case 2:
 			resumeGame.objectRenderer.SetTexture("resumeGame");
 			backToMenu.objectRenderer.SetTexture("mainMenu");
 			quitPause.objectRenderer.SetTexture("quitOnHover");
 			if(MainGame.controls.isPressed(KeyEvent.VK_ENTER)){
 				System.exit(0);
 			}
 		}
 		resumeGame.Update();
 		backToMenu.Update();
 		quitPause.Update();
 	} 
 	/**
 	 * Updates the game objects(startGame, instructions, statistics and highScores) of the GameMenu
 	 */
 	private void updateGameMenu() {
 		switch(Controls.menuCounter){
 		
 		//Start Game
 		case 0:
 			startGame.objectRenderer.SetTexture("startGameOnHover");
 			instructions.objectRenderer.SetTexture("instructions");
 			statistics.objectRenderer.SetTexture("statistics");
 			highScores.objectRenderer.SetTexture("highScores");
 			if (MainGame.controls.isPressed(KeyEvent.VK_ENTER)){
 				inLevelMenu = true;
 				inGameMenu = false;
 				initLevelMenu();
 				
 			}
 			break;
 			
 		//Instructions
 		case 1: 
 			startGame.objectRenderer.SetTexture("startGame");
 			instructions.objectRenderer.SetTexture("instructionsOnHover");
 			statistics.objectRenderer.SetTexture("statistics");
 			highScores.objectRenderer.SetTexture("highScores");
 			if (MainGame.controls.isPressed(KeyEvent.VK_ENTER)){
 				inGameMenu = false;
 				inInstructions = true;
 				initInstructions();
 				instructionTable.Update();
 				instructionFont.Update();
 			}
 			break;
 			
 		//Statistics 
 		case 2:
 			startGame.objectRenderer.SetTexture("startGame");
 			instructions.objectRenderer.SetTexture("instructions");
 			statistics.objectRenderer.SetTexture("statisticsOnHover");
 			highScores.objectRenderer.SetTexture("highScores");
 			if (MainGame.controls.isPressed(KeyEvent.VK_ENTER)){
 				inGameMenu = false;
 				inStatistics = true;
 				initStatistics();
 			}
 			break;
 			
 		//High Scores
 		case 3:
 			startGame.objectRenderer.SetTexture("startGame");
 			instructions.objectRenderer.SetTexture("instructions");
 			statistics.objectRenderer.SetTexture("statistics");
 			highScores.objectRenderer.SetTexture("highScoresOnHover");
 			if (MainGame.controls.isPressed(KeyEvent.VK_ENTER)){
 				inGameMenu = false;
 				inHighScores = true;
 				initHighScores();
 			}
 			break;
 		}
 
 		startGame.Update();
 		instructions.Update();
 		statistics.Update();
 		highScores.Update();
 	}
 	/**
 	 * Updates the game objects (easy, medium and hard) of the LevelMenu
 	 */
 	private void updateLevelMenu() {
 		switch(Controls.menuCounter){
 		
 		//Easy
 		case 0: 
 			easy.objectRenderer.SetTexture("easyOnHover");
 			medium.objectRenderer.SetTexture("medium");
 			hard.objectRenderer.SetTexture("hard");
 			if(MainGame.controls.isPressed(KeyEvent.VK_ENTER)){
 				inGameMenu = false;
 				MainGame.inMenu = false;
 				MainGame.enterKeyPressed = true;
				isMedium = false;
				isHard = false;
				GameLogic.GameOver = false;
				
 				easy.Delete();
 				medium.Delete();
 				hard.Delete();
 			}
 			
 			break;
 		
 		//Medium
 		case 1: 
 			easy.objectRenderer.SetTexture("easy");
 			medium.objectRenderer.SetTexture("mediumOnHover");
 			hard.objectRenderer.SetTexture("hard");
 			if(MainGame.controls.isPressed(KeyEvent.VK_ENTER)){
 				//put medium level code
 				isMedium = true;
				isHard = false;
				GameLogic.GameOver = false;
				
 				inGameMenu = false;
 				MainGame.inMenu = false;
 				MainGame.enterKeyPressed = true;
 
 				easy.Delete();
 				medium.Delete();
 				hard.Delete();
 			}
 			break;
 			
 		//Hard
 		case 2: 
 			easy.objectRenderer.SetTexture("easy");
 			medium.objectRenderer.SetTexture("medium");
 			hard.objectRenderer.SetTexture("hardOnHover");
 			if(MainGame.controls.isPressed(KeyEvent.VK_ENTER)){
 				inGameMenu = false;
 				MainGame.inMenu = false;
 				MainGame.enterKeyPressed = true;
 				isHard = true;
				isMedium = false;
				GameLogic.GameOver = false;
 				
 				easy.Delete();
 				medium.Delete();
 				hard.Delete();
 				//put hard level code
 			}
 			break;
 		}
 		easy.Update();
 		medium.Update();
 		hard.Update();
 	}
 	/**
 	 * Instantiates the recordString function to get the username of the already existing player 
 	 */
 	private void inEnterUsername() {
 		
 		//Shows the inputUsername if the boolean Showing is set to true
 		if(showing){
 			MainGame.controls.recordKey = true;
 			inputUsername = MainGame.controls.recordString;
 			MainGame.render.DrawText(inputUsername,Vector2.zero(),Color.White,1f);
 		}
 		enterUsername.Update();
 		if(MainGame.controls.isPressed(KeyEvent.VK_ENTER)){
 			try {
 				
 				//Checks the CSV files for the inputUsername entered
 				if(CSV.LoginMenu.login(inputUsername)){
 					
 					//Instantiates the GameMenu if the username is correct
 					if (loginSuccessful == 0){
 						initGameMenu();
 						inEnterUsername = false;
 						MainGame.controls.recordKey = false;
 						inGameMenu = true;
 						showing = false;
 						loginSuccessful++;
 					}
 				
 				} else {
 					System.out.println("Not possible");
 				}
 			} catch (IOException e) {
 			}
 		}
 	}
 	
 	/**
 	 * Instantiates the statistics of the user 
 	 */
 	private void inStatistics() {
 		
 		//Instantiates the statistics if the the variable showStarts is set to true
 		if (showStats){
 			MainGame.render.DrawText("Username: " + CSV.LoginMenu.player.getUsername(),
 					new Vector2(-100, 80),Color.White,1f);
 			MainGame.render.DrawText("Average Score: "+CSV.LoginMenu.player.getAvgScore(),
 					new Vector2(-100, 50),Color.White,1f);
 			MainGame.render.DrawText("Games Played: "+CSV.LoginMenu.player.getNbGamesPlayed(),
 					new Vector2(-100, 20),Color.White,1f);
 			MainGame.render.DrawText("Best Score: "+CSV.LoginMenu.player.getBestScore(),
 					new Vector2(-100,-10),Color.White,1f);
 		}
 	}
 	/**
 	 * Instantiates the High Scores table
 	 */
 	private void inHighScores() {
 		try {
 			CSV.Highscore.getHighscores();
 		} catch (FileNotFoundException e) {
 		} catch (IOException e) {
 		}
 		
 		//Show HighScores if the boolean showHs is set to true
 		if (showHS){
 			for(int i =0; i < 10; i++){
 				MainGame.render.DrawText(CSV.Highscore.highscores.get(i)[0],
 						new Vector2(-100, 80 - i*30),Color.White,1f);
 				MainGame.render.DrawText(CSV.Highscore.highscores.get(i)[1],
 						new Vector2(40, 80 - i*30),Color.White,1f);
 			}
 		}
 	}
 
 	/**
 	 * Instantiates the recordString function to get the username of a new player 
 	 */
 	private void inEnterUsernameNew() {
 		
 		//Show the username input if showing is set to true
 		if(showing){
 			MainGame.controls.recordKey = true;
 			inputUsername = MainGame.controls.recordString;
 			MainGame.render.DrawText(inputUsername,Vector2.zero(),Color.White,1f);
 		}
 		if(MainGame.controls.isPressed(KeyEvent.VK_ENTER)){
 			
 			//Adds the new user to the CSV files 
 			try {
 				CSV.LoginMenu.addUser(inputUsername);
 				CSV.LoginMenu.login(inputUsername);
 			} catch (IOException e) {
 			}
 		}
 		enterUsername.Update();
 		
 		//Instantiates the GameMenu if the username is not already taken 
 		if(CSV.LoginMenu.available){
 			if (usernameAvailable == 0){
 				initGameMenu();
 				inGameMenu = true;
 				showing = false;
 				MainGame.controls.recordKey = false;
 				inEnterUsernameNew = false;
 				usernameAvailable++;
 			}
 		} else {
 			System.out.println("Not possible");
 		}
 	}
 
 	/**
 	 * Instantiates the new game objects of the EnterUsername page
 	 */
 	public void initEnterUsername() { 
 		createUser.Delete();
 		loadUser.Delete();
 		twoPlayer.Delete();
 		quit.Delete();
 		enterUsername = new GameChar();
 		enterUsername.objectRenderer.SetTexture("enterUsername");
 		enterUsername.transform.size = new Vector2(35,15);
 		enterUsername.transform.position = new Vector2(0,5);
 		MainGame.controls.keyPressed[KeyEvent.VK_ENTER] = false;
 	}
 	/**
 	 * Instantiates the game objects of the GameMenu
 	 */
 	public static void initGameMenu(){
 		Controls.menuCounter = 0;
 		if(!enterUsername.isDeleted){
 			enterUsername.Delete();
 		}
 		startGame = new GameChar();
 		instructions = new GameChar();
 		statistics = new GameChar();
 		highScores = new GameChar();
 		startGame.objectRenderer.SetTexture("startGame");
 		startGame.transform.size = new Vector2 (20,15);
 		startGame.transform.position = new Vector2 (0,20);
 		startGame.rigidBody.frictionCoefficient = 0.1f;
 		instructions.objectRenderer.SetTexture("instructions");
 		instructions.transform.size = new Vector2 (25,20);
 		instructions.transform.position = new Vector2 (0,0);
 		instructions.rigidBody.frictionCoefficient = 0.1f;
 		statistics.objectRenderer.SetTexture("statistics");
 		statistics.transform.size = new Vector2 (25,10);
 		statistics.transform.position = new Vector2 (0,-7);
 		statistics.rigidBody.frictionCoefficient = 0.1f;
 		highScores.objectRenderer.SetTexture("highScores");
 		highScores.transform.position = new Vector2 (0,-10);
 		highScores.transform.size = new Vector2 (25,10);
 		MainGame.controls.keyPressed[KeyEvent.VK_ENTER] = false;
 	}
 
 	/**
 	 * Instantiates the game object of the Instructions: instruction table and the title
 	 */
 	public void initInstructions(){ 
 		startGame.Delete();
 		instructions.Delete();
 		statistics.Delete();
 		highScores.Delete();
 		instructionTable = new GameChar(); 
 		instructionFont = new GameChar();
 		instructionFont.objectRenderer.SetTexture("instructionFont");
 		instructionFont.transform.size = new Vector2 (20,5);
 		instructionFont.transform.position = new Vector2 (0,15);
 		instructionTable.objectRenderer.SetTexture("instructionTable");
 		instructionTable.transform.size = new Vector2 (35,25);
 		instructionTable.transform.position = new Vector2 (0,-2);
 	}
 
 	/**
 	 * Instantiates the elements of the High Score table 
 	 */
 	public void initHighScores(){
 		startGame.Delete();
 		instructions.Delete();
 		statistics.Delete();
 		highScores.Delete();
 		highScoresFont = new GameChar();
 		highScoresFont.objectRenderer.SetTexture("highScores");
 		highScoresFont.transform.size = new Vector2 (30,10);
 		highScoresFont.transform.position = new Vector2 (0,10);
 	}
 	/**
 	 * Instantiates the title of the statistics page
 	 */
 	public void initStatistics() {
 		startGame.Delete();
 		instructions.Delete();
 		statistics.Delete();
 		highScores.Delete();
 		statisticsFont = new GameChar();
 		statisticsFont.objectRenderer.SetTexture("statistics");
 		statisticsFont.transform.size = new Vector2 (30,10);
 		statisticsFont.transform.position = new Vector2 (0,10);
 		
 	}
 	
 	/**
 	 * Instantiates the titles of the LevelMenu 
 	 */
 	public void initLevelMenu(){ 
 		Controls.menuCounter = 0;
 		startGame.Delete();
 		instructions.Delete();
 		statistics.Delete();
 		highScores.Delete();
 		easy = new GameChar();
 		medium = new GameChar();
 		hard = new GameChar();
 		easy.objectRenderer.SetTexture("easy");
 		easy.transform.size = new Vector2 (25,10);
 		easy.transform.position = new Vector2 (0,7);
 		easy.rigidBody.frictionCoefficient = 0.1f;
 		medium.objectRenderer.SetTexture("medium");
 		medium.transform.size = new Vector2 (25,10);
 		medium.transform.position = new Vector2 (0,-5);
 		medium.rigidBody.frictionCoefficient = 0.1f;
 		hard.objectRenderer.SetTexture("hard");
 		hard.transform.size = new Vector2 (25,10);
 		hard.transform.position = new Vector2 (0,-16);
 		hard.rigidBody.frictionCoefficient = 0.1f;
 		MainGame.controls.keyPressed[KeyEvent.VK_ENTER] = false;
 	}
 	
 	/**
 	 * Instantiates the game objects of the Pause Menu 
 	 */
 	public void initPauseMenu() { 
 		Controls.menuCounter = 0;
 		resumeGame = new GameChar();
 		backToMenu = new GameChar();
 		quitPause = new GameChar();
 		resumeGame.objectRenderer.SetTexture("resumeGame");
 		backToMenu.objectRenderer.SetTexture("mainMenu");
 		resumeGame.transform.size = new Vector2 (25,10);
 		resumeGame.transform.position = new Vector2 (0,20);
 		resumeGame.rigidBody.frictionCoefficient = 0.1f;
 		backToMenu.transform.size = new Vector2 (25,10);
 		backToMenu.transform.position = new Vector2 (0,0);
 		backToMenu.rigidBody.frictionCoefficient = 0.1f;
 		quitPause.objectRenderer.SetTexture("quit");
 		quitPause.transform.position = new Vector2 (0,-20);
 		quitPause.rigidBody.frictionCoefficient = 0.1f;
 		quitPause.transform.size = new Vector2 (25,10);
 		MainGame.controls.keyPressed[KeyEvent.VK_ENTER] = false;
 		
 	}
 	/**
 	 * Instantiates the titles of the StartMenu 
 	 */
 	public void initStartMenu(){
 		Controls.menuCounter = 0;
 		counter = 0;
 		MainGame.controls.recordString = "";
 		showing = true;
 		CSV.LoginMenu.login = false;
 		loginSuccessful = 0;
 		usernameAvailable = 0;
 		background = new GameChar();
 		createUser = new GameChar();
 		loadUser = new GameChar();
 		twoPlayer = new GameChar();
 		quit = new GameChar();
 		background.objectRenderer.SetTexture("background");
 		background.transform.size = new Vector2(60,50);
 		createUser.objectRenderer.SetTexture("createUser");
 		createUser.transform.size = new Vector2 (25,10);
 		createUser.transform.position = new Vector2 (0,20);
 		createUser.rigidBody.frictionCoefficient = 0.1f;
 		loadUser.objectRenderer.SetTexture("loadUser");
 		loadUser.transform.size = new Vector2 (25,10);
 		loadUser.transform.position = new Vector2 (0,10);
 		loadUser.rigidBody.frictionCoefficient = 0.1f;
 		twoPlayer.objectRenderer.SetTexture("twoPlayer");
 		twoPlayer.transform.size = new Vector2 (25,10);
 		twoPlayer.transform.position = new Vector2 (0,-5);
 		twoPlayer.rigidBody.frictionCoefficient = 0.1f;
 		quit.objectRenderer.SetTexture("quit");
 		quit.transform.position = new Vector2 (0,-10);
 		quit.transform.size = new Vector2 (30,10);
 		MainGame.controls.keyPressed[KeyEvent.VK_ENTER] = false;
 	}
 }
