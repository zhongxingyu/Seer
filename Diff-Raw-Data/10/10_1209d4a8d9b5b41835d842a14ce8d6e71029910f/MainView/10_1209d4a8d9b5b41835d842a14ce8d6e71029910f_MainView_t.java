 package View;
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
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
 
 import Model.*;
 import Model.Classes.*;
 import Model.Obstacles.ObstaclePillar;
 import Model.Obstacles.Obstacle;
 import Model.Skills.*;
 
 import Control.*;
 
 public class MainView extends BasicGameState implements ActionListener {
 	
 	Image bg;
 	private String mouse = "No input yet";
 	
 	private PlayerController Control; 
 	private PlayerController enemyControl;
 	Player player;
 	Player enemy;
 	Skill[] playerSkills;
 	Skill activeSkill;
 	
 	TiledMap map;
 	
 	Image enemyImage;
 	Skill[] enemySkills;
 	
 	Obstacle[] obstacles = new Obstacle[100];
 	
 /*	int eWidth;
 	int eHeight;*/
 	
 	
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
 	
 	public MainView(int state) {
 		
 	}
 	
 	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException{
 		
 		map = new TiledMap("res/tileset/bg.tmx");
 		
 	/*	slash = new Image("res/slash.png");
 		fireball = new Image("res/fireball.png");
 		firestorm = new Image("res/Firestorm.png");
 		iceneedle = new Image("res/iceneedle.png");
 		pedobear = new Image("res/pbs4.png");*/
 		
 		
 		
 		
 		initRound();
 		
 	}
 	
 	public void initRound(){
 		Random obsGenerator = new Random();
 		for(int i=0; i<obsGenerator.nextInt(50); i++){
 			obstacles[i] = new ObstaclePillar(obsGenerator.nextInt(1280), obsGenerator.nextInt(719) + 1);
 		}
 		
 		Control = new PlayerController(GlobalClassSelector.getController().getPlayer(), obstacles);
 	//	player = Control.getPlayer();
 		player = GlobalClassSelector.getController().getPlayer();
 		playerSkills = Control.getPlayerSkills();
 		
 		Control.ressurectPlayer();
 
 		userImage = player.getImage();
 		
 //		if(Control.checkObstacleCollision(x, y));
 		
		enemyControl = new PlayerController(new ClassWizard("Enemy", obsGenerator.nextInt(1280), obsGenerator.nextInt(719) + 1), obstacles);
 		
 
 		enemy = enemyControl.getPlayer();
 		enemySkills = enemyControl.getPlayerSkills();
 		enemyImage = enemy.getImage();
 		enemyControl.ressurectPlayer();
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
 		
 		//Show the coodinates for the mouse
 		g.drawString(mouse, 800, 10);
 		//Show the enemys hp
 		g.drawString(enemy.getName() + " HP: "+enemy.getHP(),500,500);
 		g.drawString(player.getName() + " HP: "+player.getHP(),500,515);
 		//Draw the player
 		g.drawImage(userImage, player.getX(),player.getY());
 	
 		
 		
 		for(int i=0; i<obstacles.length; i++){
 			if(obstacles[i] != null){
 				g.drawImage(obstacles[i].getImage(), obstacles[i].getX(), obstacles[i].getY());
 			}
 		}
 		//Random Comment
 		//Draw the actionbar
 		for(int i=0; i<player.getSkills().length; i++){
 			g.setColor(Color.white);
 			g.fillRect(10 + i*50, 660, 50, 50);
 			g.setColor(Color.black);
 			g.drawString(""+playerSkills[i].checkCooldown(), 30 + i*50, 675);
 			if(playerSkills[i] != null){
 				if(playerSkills[i].checkCooldown() == playerSkills[i].getCoolDown()){
 				//	System.out.println(playerSkills[i].getName());
 			/*		switch (playerSkills[i].getName()) {
 						
 			            case "Slash":g.drawImage(slash,10 + i*50, 660);
 			                     break;
 			            case "Fireball":g.drawImage(fireball,10 + i*50, 660);
 			                     break;
 			            case "Firestorm":g.drawImage(firestorm,10 + i*50, 660);
 			                     break;
 			            case "IceNeedle":g.drawImage(iceneedle,10 + i*50, 660);
 			                     break;
 			            case "SuperSlowTestSkill":g.drawImage(pedobear,10 + i*50, 660);
 			                     break;
 			        }*/
 					g.drawImage(playerSkills[i].getSkillBarImage(),10 + i*50, 660);
 				}
 			}
 		}
 		
 		for(int i=0; i<playerSkills.length; i++){
 			if(playerSkills[i] != null){
 				if(playerSkills[i].isAttacking()/* && !playerSkills[i].isColliding() */&& !playerSkills[i].isEndState()){
 					g.drawImage(playerSkills[i].getAttImage(), playerSkills[i].getAttX(),playerSkills[i].getAttY());
 				}else if(playerSkills[i].isEndState()){
 					g.drawImage(playerSkills[i].getEndStateImage(), playerSkills[i].getAttX(),playerSkills[i].getAttY());
 				}
 			}
 		}
 
 		
 	//	if(enemy.getHP()>0){
 			g.drawImage(enemyImage, enemy.getX(), enemy.getY());
 			
 			//Copy for enemy attack render
 			for(int i=0; i<enemySkills.length; i++){
 				if(enemySkills[i] != null){
 					if(enemySkills[i].isAttacking()/* && !playerSkills[i].isColliding() */&& !enemySkills[i].isEndState()){
 						g.drawImage(enemySkills[i].getAttImage(), enemySkills[i].getAttX(),enemySkills[i].getAttY());
 					}else if(enemySkills[i].isEndState()){
 						g.drawImage(enemySkills[i].getEndStateImage(), enemySkills[i].getAttX(),enemySkills[i].getAttY());
 					}
 				}
 			}
 		//}
 	}
 	
 	
 	
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException{
 		int xPos = Mouse.getX();
 		int yPos = 720 - Mouse.getY();
 	/*	if(GlobalClassSelector.getController().checkPlayerAddition()){
 			player.changePlayerClass(GlobalClassSelector.getController().getPlayer());
 		}*/
 		
 		Control.checkCollision(enemySkills);
 		enemyControl.checkCollision(playerSkills);
 		
 		
 		mouse = "Mouse position: (" + xPos + "," + yPos + ")";
 		
 		Input input = gc.getInput();
 		if(input.isKeyDown(Input.KEY_Q)){player.addY(-1);}
 		if(input.isKeyDown(Input.KEY_S)){player.addY(1);}
 		if(input.isKeyDown(Input.KEY_A)){player.addX(-1);}
 		if(input.isKeyDown(Input.KEY_D)){player.addX(1);}
 		
 		if(input.isKeyDown(Input.KEY_1)){
 			if(playerSkills[0] != null){
 				Control.setCurrentActiveSkill(0);
 				enemyControl.setCurrentActiveSkill(0);
 			}
 		}
 		if(input.isKeyDown(Input.KEY_2)){
 			if(playerSkills[1] != null){
 				Control.setCurrentActiveSkill(1);
 				enemyControl.setCurrentActiveSkill(1);
 			}
 		}
 		if(input.isKeyDown(Input.KEY_3)){
 			if(playerSkills[2] != null){
 				Control.setCurrentActiveSkill(2);
 				enemyControl.setCurrentActiveSkill(2);
 			}
 		}
 		if(input.isKeyDown(Input.KEY_4)){
 			if(playerSkills[3] != null){
 				Control.setCurrentActiveSkill(3);
 				enemyControl.setCurrentActiveSkill(3);
 			}
 		}
 		if(input.isKeyDown(Input.KEY_5)){
 			if(playerSkills[4] != null){
 				Control.setCurrentActiveSkill(4);
 				enemyControl.setCurrentActiveSkill(4);
 			}
 		}
 		
 		Random generator = new Random();
 		//If left mousebutton is clicked, move the player
 		if(input.isMouseButtonDown(1)){
 			Control.move(Mouse.getX(), 720 - Mouse.getY());
 			enemyControl.move(generator.nextInt(1280), generator.nextInt(719) + 1);
 		}
 		//If right mousebutton is clicked, attack that point
 		if(input.isMouseButtonDown(0)){
 			if ((135<xPos && 250>xPos) && (225<yPos && 270>yPos)){
 				enemyControl.ressurectPlayer();
 				Control.ressurectPlayer();
 			}
 			Control.attack(Mouse.getX(), 720 - Mouse.getY());
 			enemyControl.attack((int)player.getX(), (int)player.getY());
 		}
 		if(player.isRunning()){
 			Control.isRunning();
 			if(userImage == player.getImage() || userImage == player.getSecondStepImage())
 				userImage = player.getFirstStepImage();
 			else if(userImage == player.getImage() || userImage == player.getFirstStepImage())
 				userImage = player.getSecondStepImage();
 		} else {
 			userImage = player.getImage();
 		}
 		
 		//Copy for enemy running
 		if(enemy.isRunning()){
 			enemyControl.isRunning();
 			if(enemyImage == enemy.getImage() || enemyImage == enemy.getSecondStepImage())
 				enemyImage = enemy.getFirstStepImage();
 			else if(enemyImage == enemy.getImage() || enemyImage == enemy.getFirstStepImage())
 				enemyImage = enemy.getSecondStepImage();
 		} else {
 			enemyImage = enemy.getImage();
 		}
 		
 		
 		
 		
 		
 		for(int i=0; i<playerSkills.length; i++){
 			if(playerSkills[i] != null && playerSkills[i].isAttacking()){
 				Control.isAttacking(playerSkills[i]);
 			}
 		}
 		
 		//Copy for enemy attacking
 		for(int i=0; i<enemySkills.length; i++){
 			if(enemySkills[i] != null && enemySkills[i].isAttacking()){
 				enemyControl.isAttacking(enemySkills[i]);
 			}
 		}
 		if (!enemy.isAlive()){
 		//	enemy
 			sbg.enterState(4);
 		}
 	}
 	
 	
 	
 		
 	//Returns the state of the game
 	public int getID(){
 		return 1;
 	}
 	
 	
 	//Test for sounds
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		String fileName = "res/154561__ecfike__hurt-argh-1.wav";
 		if(e.getActionCommand() == "Taking damage"){
 			playSound("res/154561__ecfike__hurt-argh-1.wav");
 		} else if(e.getActionCommand() == "Sword"){
 			playSound("res/77611__joelaudio__sfx-attack-sword-001.wav");
 		} else if(e.getActionCommand() == "Bow"){
 			playSound("res/65733__erdie__bow01.wav");
 		} else if(e.getActionCommand() == "Fireball"){
 			playSound("res/77691__joelaudio__sfx-magic-fireball-001.wav");
 		} else if(e.getActionCommand() == "Music"){
 			playSound("res/126427__cabeeno-rossley__timer-first-half-loop.wav");
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
