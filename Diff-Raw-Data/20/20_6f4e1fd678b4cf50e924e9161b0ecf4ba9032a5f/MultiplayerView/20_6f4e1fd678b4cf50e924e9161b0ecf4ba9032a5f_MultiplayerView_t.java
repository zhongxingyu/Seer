 package View;
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 
 import javax.swing.JFrame;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import org.lwjgl.input.Mouse;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.tiled.TiledMap;
 
 import sshaclient.Constants;
 
 
 
 
 
 import Control.*;
 import Model.*;
 import Model.Classes.*;
 import Model.Obstacles.ObstaclePillar;
 import Model.Obstacles.Obstacle;
 import Model.Skills.*;
 import Model.Timers.AnimationTimer;
 
 
 public class MultiplayerView extends BasicGameState implements ActionListener {	
 	boolean shouldcalcgold;
 	long TimeRoundStart=System.currentTimeMillis();
 	
 	Image bg;
 	private String mouse = "No input yet";
 	
 	String test;
 	TiledMap map;
 	
 	Player[] playerList;
 	
 	String endRoundText = "";
 	String winningPlayer= "<Fix code to get> \n<the winning> \n<players name>";
 	Image nextRoundButton;
 	Image nextRoundBg;
 	boolean roundOver = false;
 	boolean firstTimeRoundOver;
 	Image roundOverAnimationImage;
 	
 	AnimationTimer victoryAnimation;
 	
 	private PlayerModel Control; 
 	Player player;
 	Skill[] playerSkills;
 	Skill activeSkill;
 	
 	Image playerPortrait;
 	
 	Image enemyImage;
 	Skill[] enemySkills;
 	
 	private int enemyPlayer = 1;
 	
 	private int activePlayer;
 	Skill[] activeSkillList;
 	PlayerModel currentActiveController;
 	private PlayerModel[] players = new PlayerModel[Constants.nbrOfPlayer];
 	private Obstacle[] obstacles = new Obstacle[100];
 	
 	Image userImage;
 	Image user;
 	Image move1;
 	Image move2;
 	
 	float mouseXPosMove;
 	float mouseYPosMove;
 	
 	float prevMouseXPosMove;
 	float prevMouseYPosMove;
 	
 	
 	float mouseXPosAtt;
 	float mouseYPosAtt;
 	
 	public MultiplayerView(int state) {
 		
 	}
 	
 	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException{
 		
 		map = new TiledMap("res/tileset/grassBackground.tmx");
 	//	initRound();
 	}
 	
 	public void initRound() throws SlickException{
 		TimeRoundStart = System.currentTimeMillis();
 		shouldcalcgold=true;
 		
 		Random obsGenerator = new Random();
 		for(int i=0; i<obsGenerator.nextInt(50); i++){
 			obstacles[i] = new ObstaclePillar(obsGenerator.nextInt(1280), obsGenerator.nextInt(719) + 1);
 		}
 		playerList = GlobalClassSelector.getController().getPlayers();
 		activePlayer = GlobalClassSelector.getController().getActivePlayerIndex();
 		switch(playerList[activePlayer].getType()){
 		case "Wizard":
 			playerPortrait = new Image("res/classImages/Portrait_Wizard.png");
 		break;
 		case "Hunter":
 			playerPortrait = new Image("res/classImages/Portrait_Hunter.png");
 		break;
 		case "Warrior":
 			playerPortrait = new Image("res/classImages/Portrait_Warrior.png");
 		break;
       	}
 		
 	//	players.add(new PlayerModel(playerList[activePlayer], obstacles));
 		for(int i=0; i<GlobalClassSelector.getController().getPlayers().length; i++){
 			
 			if(GlobalClassSelector.getController().getPlayer(i) != null){
 				System.out.println(GlobalClassSelector.getController().getPlayer(i).getName());
 				players[GlobalClassSelector.getController().getActivePlayerIndex()] = new PlayerModel(GlobalClassSelector.getController().getPlayer(i), obstacles);
 			}
 		}
 		
 		
 		for(int i=0; i<players.length; i++){
 			PlayerModel currentController = players[i];
 			if(currentController != null){
 				currentController.ressurectPlayer();
 				currentController.checkPlayerObstacleCollision(0, 0);
 			}
 		}
 		nextRoundButton = new Image("res/buttons/Ready.png");
 		nextRoundBg = new Image("res/miscImages/skillDescBg.png");
 		
 		Image[] victoryanimation = new Image[10];
 		
 		victoryanimation[0] = new Image("res/animations/victory/victory10.png");
 		victoryanimation[1] = new Image("res/animations/victory/victory9.png");
 		victoryanimation[2] = new Image("res/animations/victory/victory8.png");
 		victoryanimation[3] = new Image("res/animations/victory/victory7.png");
 		victoryanimation[4] = new Image("res/animations/victory/victory6.png");
 		victoryanimation[5] = new Image("res/animations/victory/victory5.png");
 		victoryanimation[6] = new Image("res/animations/victory/victory4.png");
 		victoryanimation[7] = new Image("res/animations/victory/victory3.png");
 		victoryanimation[8] = new Image("res/animations/victory/victory2.png");
 		victoryanimation[9] = new Image("res/animations/victory/victory1.png");
 		
 		victoryAnimation = new AnimationTimer(500,victoryanimation);
 		firstTimeRoundOver = true;
 		
 		GlobalClassSelector.getController().getPlayer(GlobalClassSelector.getController().getActivePlayerIndex()).setMode("arena");
 		for(int lol = 0; lol < GlobalClassSelector.getController().getPlayers().length; lol++) {
 			if(GlobalClassSelector.getController().getPlayers()[lol] != null) {
 				if(GlobalClassSelector.getController().getPlayers()[lol].getControlType().equals("server")) {
 					GlobalClassSelector.getController().getPlayerControllers()[lol].changePlayer(GlobalClassSelector.getController().getPlayers()[lol]);
 					System.out.println("Player " + lol + " is now changed.");
 				}
 				
 			
 			}
 		}
 		
 		for(int i=0; i<players.length; i++){
 			if(players[i] != null){
 				System.out.println(players[i].getPlayer().getName() + " " + players[i].getPlayer().getPlayerListIndex() + " " + i);
 			}
 		}
 		currentActiveController = players[activePlayer];
 		activeSkillList = currentActiveController.getPlayer().getSkillList();
 	}
 	
 	@Override
 	   public void enter(GameContainer container, StateBasedGame game)
 	         throws SlickException {
 	      // TODO Auto-generated method stub
 	      super.enter(container, game);
 	      initRound();
 
 	   }
 	
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException{
 		map.render(0,0);
 		
 		//Show the coodinates of the mouse
 		g.drawString(mouse, 900, 10);
 		
 		
 		//Draw Obstacles
 		for(int i=0; i<obstacles.length; i++){
 			if(obstacles[i] != null){
 				g.drawImage(obstacles[i].getCurrentImage(), obstacles[i].getX(), obstacles[i].getY());
 			}
 		}
 		
 		//Attempt to draw oval around player which displays max range (Currently not working correctly)
 //		g.drawOval(player.getX()-Control.getCurrentActiveSkill().getAttackRange()/2, player.getY()-Control.getCurrentActiveSkill().getAttackRange()/2, Control.getCurrentActiveSkill().getAttackRange(), Control.getCurrentActiveSkill().getAttackRange());
 //		System.out.println(Control.getCurrentActiveSkill().getAttackRange());
 		g.drawImage(playerPortrait, 20, 585);
 		//Draw the actionbar
 		Skill[] activePlayerSkills = playerList[activePlayer].getSkillList();
 		for(int j=0; j<activePlayerSkills.length; j++){
 		//	g.setColor(Color.white);
 		//	g.fillRect(140 + j*64, 640, 64, 64);
 		//	g.setColor(Color.black);
 			
 			if(activePlayerSkills[j] != null){
 				
 			//	if(activePlayerSkills[j].checkCooldown() == activePlayerSkills[j].getCoolDown()){
 					g.drawImage(activePlayerSkills[j].getSkillBarImage(),140 + j*64, 640);
 			//	}
 				g.drawString(""+activePlayerSkills[j].checkCooldown(), activePlayerSkills[j].getSkillBarImage().getWidth()/2 + 140 + j*64, 610);
 				
 			}
 		}
 		
 		
 		//Draw player stats and image
 		
 		for(int i=0; i<playerList.length; i++){
 			if(playerList[i] != null){
 				Player currentPlayer = playerList[i];
 				Skill[] currentSkillset = currentPlayer.getSkillList();
 				g.drawString(currentPlayer.getName() + "\nHP: "+currentPlayer.getHP() + "\nArmor: " + (int)(currentPlayer.getArmor()*100) 
 						+ "%\nKills: " + currentPlayer.getKills() + "\nMovement: " + currentPlayer.getMoveSpeed() 
 						+ "\nx:" + currentPlayer.getX() + " y: " + currentPlayer.getY(),900+150*i,25);
 				
 				
 				for(int j=0; j<currentSkillset.length; j++){
 					if(currentSkillset[j] != null){
 						if(currentSkillset[j].isAttacking() && !currentSkillset[j].isEndState()){
 							g.drawImage(currentSkillset[j].getAttImage(), currentSkillset[j].getAttX(),currentSkillset[j].getAttY());
 						}else if(currentSkillset[j].isEndState()){
 							g.drawImage(currentSkillset[j].getEndStateImage(), currentSkillset[j].getAttX(),currentSkillset[j].getAttY());
 						}
 					}
 				}
 				g.drawImage(currentPlayer.getImage(), currentPlayer.getX(),currentPlayer.getY());
 			}
 		}
 		if(roundOver){
 			if (shouldcalcgold){
 				goldreward();
 			}
 			shouldcalcgold=false;
 		//	System.out.println(player.getGold());
 			g.drawImage(nextRoundBg, 1280/2 - nextRoundBg.getWidth()/2, 200);
 			g.drawImage(nextRoundButton, 1280/2 - nextRoundButton.getWidth()/2, 200 + nextRoundBg.getHeight()/2);
 			g.drawString(endRoundText, 1280/2 - nextRoundBg.getWidth()/4, 210);
 			if(Control.getPlayer().getHP()>0){
 				
 				Image testAnimationImage = victoryAnimation.getCurrentAnimationImage();
 				if(testAnimationImage != null){
 					roundOverAnimationImage = testAnimationImage;
 				}
 				if(roundOverAnimationImage != null)
 					g.drawImage(roundOverAnimationImage, 1280/2 - nextRoundBg.getWidth()/2, 200-nextRoundBg.getHeight()/2);
 			}
 		}
 		//g.drawString("Multiplayer", 640, 200);
 	}
 	public void goldreward(){
 		System.out.println("hora");
 		if(System.currentTimeMillis()-TimeRoundStart<1000*60*1)
 			player.setGold(player.getGold()+1);
 		else if(System.currentTimeMillis()-TimeRoundStart<1000*60*2)
 			player.setGold(player.getGold()+5);
 		else if(System.currentTimeMillis()-TimeRoundStart<1000*60*3)
 			player.setGold(player.getGold()+15);
 		else if(System.currentTimeMillis()-TimeRoundStart<1000*60*4)
 			player.setGold(player.getGold()+25);
 		else if(System.currentTimeMillis()-TimeRoundStart<1000*60*5)
 			player.setGold(player.getGold()+50);
 	}
 	
 	
 	
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException{
 		
		System.out.println("GCS ID: " + GlobalClassSelector.getController().getActivePlayerIndex());
		System.out.println("Multiplayer ActivePlayer ID: " + activePlayer);
		System.out.println("Socket Player Active ID: " + GlobalClassSelector.getController().getSocketClient().getPlayer().getPlayerListIndex());
		
 		
 		
 		//Update current mouse position
 		int xPos = Mouse.getX();
 		int yPos = 720 - Mouse.getY();
 		mouse = "Mouse position: (" + xPos + "," + yPos + ")";
 		
 		for(int i=0; i<players.length; i++){
 			PlayerModel currentController = players[i];
 			
 			if(currentController != null){
 				//Checking status effects
 				currentController.checkStatusEffects();
 				
 				//Checking userImageChange
 				currentController.checkUserImageChange();
 				
 				//Checking collision from other players
 				for(int j=0; j<players.length; j++){
 					PlayerModel checkController;
 					//Check to see it is another player
 					
 					if(j != i && currentController.getPlayer().isAlive()){
 						checkController = players[j];
 						if(checkController != null){
 							currentController.checkCollision(checkController.getPlayer(), checkController.getPlayer().getSkillList());
 						}
 					}
 				}
 				
 				//Check if player is running to update positioning
 				if(currentController.getPlayer().isRunning()){
 					currentController.isRunning();
 				}
 				
 				
 				Skill[] currentSkillList = currentController.getPlayer().getSkillList();
 				for(int j=0; j<currentSkillList.length; j++){
 					//Check if players skills are in use to update positioning
 					if(currentSkillList[j] != null && currentSkillList[j].isAttacking()){
 						currentController.isAttacking(currentSkillList[j]);
 					}
 				}
 			
 			}
 		}
 		
 		Input input = gc.getInput();
 		if(input.isKeyDown(Input.KEY_1)){
 			if(activeSkillList[0] != null && !activeSkillList[0].isPassive()){
 				currentActiveController.setCurrentActiveSkill(0);
 			}
 		}
 		if(input.isKeyDown(Input.KEY_2)){
 			if(activeSkillList[1] != null && !activeSkillList[1].isPassive()){
 				currentActiveController.setCurrentActiveSkill(1);
 			}
 		}
 		if(input.isKeyDown(Input.KEY_3)){
 			if(activeSkillList[2] != null && !activeSkillList[2].isPassive()){
 				currentActiveController.setCurrentActiveSkill(2);
 			}
 		}
 		if(input.isKeyDown(Input.KEY_4)){
 			if(activeSkillList[3] != null && !activeSkillList[3].isPassive()){
 				currentActiveController.setCurrentActiveSkill(3);
 			}
 		}
 		if(input.isKeyDown(Input.KEY_5)){
 			if(activeSkillList[4] != null && !activeSkillList[4].isPassive()){
 				currentActiveController.setCurrentActiveSkill(4);
 			}
 		}
 		
 		
 		//If left mousebutton is clicked, move the player
 		if(input.isMouseButtonDown(1)){
 			currentActiveController.move(Mouse.getX(), 720 - Mouse.getY());
 			//enemyControl.move(generator.nextInt(1280), generator.nextInt(719) + 1);
 		}
 		//If right mousebutton is clicked, attack that point
 		if(input.isMouseButtonDown(0) && currentActiveController.getCurrentActiveSkill().checkCooldown() == currentActiveController.getCurrentActiveSkill().getCoolDown()){
 			currentActiveController.attack(Mouse.getX(), 720 - Mouse.getY());
 			
 		}
 		
 		
 		//Checks if round should be ended
 		int endRound = 0;
 		String winningPlayer = null;
 		for(int i=0; i<players.length; i++){
 			if(players[i] != null){
 				if(!players[i].getPlayer().isAlive()){
 					endRound++;
 					
 				}else{
 					winningPlayer = players[i].getPlayer().getName();
 				}
 			}
 		}
 		//Ends round if only 1 player is alive
 		//Added impossible pass for now
 		if (endRound >= players.length - 1 && false){
 			if(firstTimeRoundOver){
 				victoryAnimation.resetCounterAndTimer();
 				firstTimeRoundOver = false;
 				GlobalClassSelector.getController().getPlayer(GlobalClassSelector.getController().getActivePlayerIndex()).setMode("lobby");
 			}
 			roundOver = true;
 			endRoundText = winningPlayer + " " + "wins!";
 			if((640 - nextRoundButton.getWidth()/2<xPos && xPos<760 - nextRoundButton.getWidth()/2) && (200 + nextRoundBg.getHeight()/2<yPos && yPos<245 + nextRoundBg.getHeight()/2)){
 				nextRoundButton = new Image("res/buttons/ReadyOver.png");
 				if(input.isMousePressed(0)){
 					roundOver = false;
 					sbg.enterState(4);
 				}
 			}else{
 				nextRoundButton = new Image("res/buttons/Ready.png");
 			}
 		}
 		
 	}
 
 	
 		
 	//Returns the state of the game
 	public int getID(){
 		return 2;
 	}
 	
 	
 	//Test for sounds
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		String fileName = "res/sounds/takingDamage.wav";
 		if(e.getActionCommand() == "Taking damage"){
 			playSound("res/sounds/takingDamage.wav");
 		} else if(e.getActionCommand() == "Sword"){
 			playSound("res/sounds/swordAttack.wav");
 		} else if(e.getActionCommand() == "Bow"){
 			playSound("res/sounds/bowShot.wav");
 		} else if(e.getActionCommand() == "Fireball"){
 			playSound("res/sounds/fireballCast.wav");
 		} else if(e.getActionCommand() == "Music"){
 			playSound("res/sounds/tickerSound.wav");
 		}
 	}
 	
 	//Handling the soundfiles
 	public static synchronized void playSound(String filename) {
 
 		    try
 		    {
 		        Clip clip = AudioSystem.getClip();
 		        clip.open(AudioSystem.getAudioInputStream(new File(filename)));
 		        clip.start();
 		    }
 		    catch (Exception exc)
 		    {
 		        exc.printStackTrace(System.out);
 		    }	
 	}
 }
