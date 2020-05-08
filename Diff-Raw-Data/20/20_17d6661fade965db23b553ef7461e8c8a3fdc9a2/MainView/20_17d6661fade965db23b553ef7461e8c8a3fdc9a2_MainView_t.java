 package View;
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
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
 
 import Model.*;
 import Model.Skills.*;
 
 public class MainView extends BasicGameState implements ActionListener {
 	
 	Image bg;
 	private String mouse = "No input yet";
 	
 	Player player;
 	Skill[] playerSkills;
 	Skill currentActiveSkill;
 	
 	int enemyHP = 100;
 	Image enemyImage;
 	float enemyX = 600;
 	float enemyY = 300;
 	int eWidth;
 	int eHeight;
 	
 
 	public MainView(int state) {
 		
 	}
 	
 	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException{
 		enemyImage = new Image("res/awesomePinkSquare.png");
 		player = new Player(190, 90);
 		playerSkills = player.getSkills();
 		currentActiveSkill = playerSkills[0];
 	}
 	
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException{
 		
 		//Background
 		bg = new Image("res/bg.png");
 		//Draw the background
 		g.drawImage(bg, 0, 0);
 		g.drawImage(bg, 500, 0);
 		g.drawImage(bg, 1000, 0);
 		g.drawImage(bg, 0, 500);
 		g.drawImage(bg, 500, 500);
 		g.drawImage(bg, 1000, 500);
 		//Show the coodinates for the mouse
 		g.drawString(mouse, 800, 10);
 		//Show the enemys hp
 		g.drawString("Enemy HP: "+enemyHP,500,500);
 		//Draw the player
 		g.drawImage(player.getImage(), player.getX(),player.getY());
 		
 		
 		//Draw the actionbar
 		for(int i=0; i<player.getSkills().length; i++){
 			g.setColor(Color.white);
 			g.fillRect(10 + i*50, 660, 50, 50);
 			g.setColor(Color.black);
 			g.drawString(""+playerSkills[i].checkCooldown(), 30 + i*50, 675);
 			if(playerSkills[i] != null){
 				if(playerSkills[i].checkCooldown() == playerSkills[i].getCoolDown())
 					switch (playerSkills[i].getName()) {
 						
 			            case "Slash":g.drawImage(new Image("res/slash.png"),10 + i*50, 660);
 			                     break;
 			            case "Fireball":g.drawImage(new Image("res/fireball.png"),10 + i*50, 660);
 			                     break;
 			            case "Firestorm":g.drawImage(new Image("res/Firestorm.png"),10 + i*50, 660);
 			                     break;
 			            case "SuperSlowTestSkill":g.drawImage(new Image("res/pbs4.png"),10 + i*50, 660);;
 			                     break;
 		        }
 			}
 			
 		}
 		
 		for(int i=0; i<playerSkills.length; i++){
 			if(playerSkills[i] != null){
 				if(playerSkills[i].isAttacking() && !playerSkills[i].isColliding() && !playerSkills[i].isEndState()){
 					g.drawImage(playerSkills[i].getAttImage(), playerSkills[i].getAttX(),playerSkills[i].getAttY());
 				}else if(playerSkills[i].isEndState()){
 					g.drawImage(playerSkills[i].getEndStateImage(), playerSkills[i].getAttX(),playerSkills[i].getAttY());
 					System.out.println("End State");
 				}
 				
 				if(isColliding(playerSkills[i]) && playerSkills[i].isColliding()){
 					playerSkills[i].setAttackingState(false);
 					System.out.println("Target hit with " + playerSkills[i].getName());
 					playerSkills[i].setCollidingState(false);
 					playerSkills[i].collidedShot();
 					enemyHP -= playerSkills[i].getDamage();
 				}
 			}
 		}
 		
 		
 
 		
 		if(enemyHP>0){
 			g.drawImage(enemyImage, enemyX, enemyY);
 		}else{
			enemyX=1000;
 			enemyY=-1000;
 		}
 	}
 	
 	float mouseXPosMove;
 	float mouseYPosMove;
 	
 	float mouseXPosAtt;
 	float mouseYPosAtt;
 	
 	Double findNaN;
 	
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException{
 		int xPos = Mouse.getX();
 		int yPos = 720 - Mouse.getY();
 		
 		mouse = "Mouse position: (" + xPos + "," + yPos + ")";
 		
 		Input input = gc.getInput();
 		if(input.isKeyDown(Input.KEY_1)){
 			if(playerSkills[0] != null)
 				currentActiveSkill = playerSkills[0];}
 		if(input.isKeyDown(Input.KEY_2)){
 			if(playerSkills[1] != null)
 				currentActiveSkill = playerSkills[1];}
 		if(input.isKeyDown(Input.KEY_3)){
 			if(playerSkills[2] != null)
 				currentActiveSkill = playerSkills[2];}
 		if(input.isKeyDown(Input.KEY_4)){
 			if(playerSkills[3] != null)
 				currentActiveSkill = playerSkills[3];}
 		if(input.isKeyDown(Input.KEY_5)){
 			if(playerSkills[4] != null)
 				currentActiveSkill = playerSkills[4];}
 		
 		if((190<xPos && xPos<290) && (250<yPos && yPos<350)){
 			if(input.isMouseButtonDown(0)){ // 0 = leftclick, 1 = rightclick
 				sbg.enterState(1);
 			}
 		}
 		//If left mousebutton is clicked, move the player
 		if(input.isMouseButtonDown(1)){
 			move();
 		}
 		//If right mousebutton is clicked, attack that point
 		if(input.isMouseButtonDown(0)){
 			if ((135<xPos && 250>xPos) && (225<yPos && 270>yPos)){
 				enemyHP= 100;
 				enemyX = 600;
 				enemyY = 300;	
 			}
 			attack();
 		}
 		if(player.isRunning()){
 			isRunning();
 		}
 		for(int i=0; i<playerSkills.length; i++){
 			if(playerSkills[i] != null && playerSkills[i].isAttacking()){
 				isAttacking(playerSkills[i]);
 			}
 		}
 	}
 	
 	private void isRunning(){
 		player.addX((float)(player.getXDirMove()*player.getMoveSpeed()));
 		player.addY((float)(player.getYDirMove()*player.getMoveSpeed()));
 		if(findNaN.isNaN()){
 //			imgX = mouseXPosMove;
 //			imgY = mouseYPosMove;
 			player.setRunningState(false);
 		}
 		player.incMoveCounter();
 		if(player.getMoveCounter()*player.getMoveSpeed() >= player.getGenDirMove())
 			player.setRunningState(false);
 	}
 	
 	private void isAttacking(Skill attackingSkill){
 			if(attackingSkill != null && !attackingSkill.isEndState()){
 				attackingSkill.addAttX((float)(attackingSkill.getXDirAtt()*attackingSkill.getAttSpeed()));
 				attackingSkill.addAttY((float)(attackingSkill.getYDirAtt()*attackingSkill.getAttSpeed()));
 				
 				attackingSkill.incAttCounter();
 				if(attackingSkill.getAttCounter()*attackingSkill.getAttSpeed() >= attackingSkill.getGenDirAtt()){
 					if(attackingSkill.isProjectile()){
 						attackingSkill.setAttackingState(false);
 					}else{
 						attackingSkill.activateEndState();
 					}
 				}
 				
			}else if(attackingSkill != null && attackingSkill.isEndState() && attackingSkill.checkEndStateTimer() == attackingSkill.getEndStateDuration()){
				attackingSkill.setEndState(false);
				attackingSkill.setAttackingState(false);
 			}
 	}
 	
 	public boolean isColliding(Skill skill) throws SlickException{
 		if(skill.getAttX() <= enemyX && skill.getAttX()+skill.getImgWidth() >= enemyX){
 			if(skill.getAttY() >= enemyY && skill.getAttY() <= enemyY+enemyImage.getHeight() 
 					|| skill.getAttY()+skill.getImgHeight() >= enemyY && skill.getAttY()+skill.getImgHeight() <= enemyY+enemyImage.getHeight()
 					|| skill.getAttY() <= enemyY && skill.getAttY()+skill.getImgHeight() >= enemyY+enemyImage.getHeight()){
 				System.out.println("Hit");
 				skill.setCollidingState(true);
 				return true;
 			}
 		}else if(skill.getAttY() <= enemyY && skill.getAttY()+skill.getImgHeight() >= enemyY){
 			if(skill.getAttX() >= enemyX && skill.getAttX() <= enemyX+enemyImage.getWidth() 
 					|| skill.getAttX()+skill.getImgWidth() >= enemyX && skill.getAttX()+skill.getImgWidth() <= enemyX+enemyImage.getWidth()
 					|| skill.getAttX() <= enemyX && skill.getAttX()+skill.getImgWidth() >= enemyX+enemyImage.getWidth()){
 				skill.setCollidingState(true);
 				return true;
 			}
 		}else if(skill.getAttX() <= enemyX+enemyImage.getWidth() && skill.getAttX()+skill.getImgWidth() >= enemyX+enemyImage.getWidth()){
 			if(skill.getAttY() >= enemyY && skill.getAttY() <= enemyY+enemyImage.getHeight() 
 					|| skill.getAttY()+skill.getImgHeight() >= enemyY && skill.getAttY()+skill.getImgHeight() <= enemyY+enemyImage.getHeight()
 					|| skill.getAttY() <= enemyY && skill.getAttY()+skill.getImgHeight() >= enemyY+enemyImage.getHeight()){
 				skill.setCollidingState(true);
 				return true;
 			}
 		}else if(skill.getAttY() <= enemyY+enemyImage.getHeight() && skill.getAttY()+skill.getImgHeight() >= enemyY+enemyImage.getHeight()){
 			if(skill.getAttX() >= enemyX && skill.getAttX() <= enemyX+enemyImage.getWidth() 
 					|| skill.getAttX()+skill.getImgWidth() >= enemyX && skill.getAttX()+skill.getImgWidth() <= enemyX+enemyImage.getWidth()
 					|| skill.getAttX() <= enemyX && skill.getAttX()+skill.getImgWidth() >= enemyX+enemyImage.getWidth()){
 				skill.setCollidingState(true);
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public void move(){
 		mouseXPosMove = Mouse.getX();
 		mouseYPosMove = 720 - Mouse.getY();
 		player.setXDirMove((mouseXPosMove - player.getX()));
 		player.setYDirMove((mouseYPosMove - player.getY()));
 		player.setGenDirMove((float)Math.sqrt(player.getXDirMove()*player.getXDirMove()+player.getYDirMove()*player.getYDirMove()));
 		findNaN = (double)player.getGenDirMove();
 		player.setXDirMove(player.getXDirMove()/player.getGenDirMove());
 		player.setYDirMove(player.getYDirMove()/player.getGenDirMove());
 		
 		player.resetMoveCounter();
 		
 		System.out.println("Running " + player.getGenDirMove() + " pixels");
 		player.setRunningState(true);
 	}
 	
 	public void attack(){
 		
 	//	playerSkills[0].run();
 		
 		//for(int i=0; i<playerSkills.length; i++){
 			if(currentActiveSkill != null && currentActiveSkill.checkCooldown() == currentActiveSkill.getCoolDown()){
 				
 				currentActiveSkill.activateSkill();
 				
 				mouseXPosAtt = Mouse.getX();
 				mouseYPosAtt = 720 - Mouse.getY();
 				
 				currentActiveSkill.resetShot(player);
 				
 				currentActiveSkill.setXDirAtt((mouseXPosAtt - currentActiveSkill.getAttX()));
 				currentActiveSkill.setYDirAtt((mouseYPosAtt - currentActiveSkill.getAttY()));
 				currentActiveSkill.setGenDirAtt((float)Math.sqrt(currentActiveSkill.getXDirAtt()*currentActiveSkill.getXDirAtt()+currentActiveSkill.getYDirAtt()*currentActiveSkill.getYDirAtt()));
 				currentActiveSkill.setXDirAtt(currentActiveSkill.getXDirAtt()/currentActiveSkill.getGenDirAtt());
 				currentActiveSkill.setYDirAtt(currentActiveSkill.getYDirAtt()/currentActiveSkill.getGenDirAtt());
 				
 				if(currentActiveSkill.getGenDirAtt() > currentActiveSkill.getAttackRange()){
 					currentActiveSkill.setGenDirAtt(currentActiveSkill.getAttackRange());
 				}
 				
 				currentActiveSkill.resetAttCounter();
 				
 				System.out.println("Attacking with " + currentActiveSkill.getName() + " at the range of " + currentActiveSkill.getGenDirAtt() + " pixels");
 				currentActiveSkill.setAttackingState(true);
 			}
 		//}
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
